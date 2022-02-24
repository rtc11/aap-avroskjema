import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask
import com.github.davidmc24.gradle.plugin.avro.GenerateAvroProtocolTask
import com.github.davidmc24.gradle.plugin.avro.GenerateAvroSchemaTask

plugins {
    `maven-publish`
    kotlin("jvm")
    id("com.github.davidmc24.gradle.plugin.avro")
}

dependencies {
    api("org.apache.avro:avro:1.11.0")
    testImplementation(kotlin("test"))
}

tasks {
    val generateProtocol = task("generateProtocol", GenerateAvroProtocolTask::class) {
        source("main")
        setOutputDir(file("$buildDir/generated/avpr"))
    }

    val generateSchema = task("generateSchema", GenerateAvroSchemaTask::class) {
        dependsOn(generateProtocol)
        source("$buildDir/generated/avpr")
        setOutputDir(file("$buildDir/generated/avsc"))
    }

    val generateAvro = task("generateAvro", GenerateAvroJavaTask::class) {
        dependsOn(generateSchema)
        source("$buildDir/generated/avsc")
        setOutputDir(file("$buildDir/generated/avro"))
    }

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        source(generateAvro)
    }

    withType<Test> {
        useJUnitPlatform()
    }
}

java.sourceSets["main"].java.srcDirs("main")
sourceSets["main"].java.srcDirs("$buildDir/generated/avro")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

publishing {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/aap-avro")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                name.set("aap.avro-medlem")
                artifactId = "medlem"
                description.set("Avro skjema for kommunikasjon mellom aap og LovMe")
                url.set("https://github.com/navikt/aap-avro")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/navikt/aap-avro.git")
                    developerConnection.set("scm:git:ssh://github.com/navikt/aap-avro.git")
                    url.set("https://github.com/navikt/aap-avro")
                }
                developers {
                    developer {
                        organization.set("NAV (Arbeids- og velferdsdirektoratet) - The Norwegian Labour and Welfare Administration")
                        organizationUrl.set("https://www.nav.no")
                    }
                }
            }
            from(components["java"])
        }
    }
}
