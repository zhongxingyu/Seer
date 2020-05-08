 /*
  * Copyright 2010-2013, CloudBees Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.cloudbees.genapp.tomcat8;
 
 import com.cloudbees.genapp.Files2;
 import com.cloudbees.genapp.metadata.Metadata;
 import com.google.common.base.Charsets;
 import com.google.common.base.Preconditions;
 
 import javax.annotation.Nonnull;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.nio.file.*;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
  */
 public class Setup2 {
     public static final String DEFAULT_JAVA_VERSION = "1.7";
     final Path appDir;
     /**
      * ./tomcat8
      */
     final Path catalinaHome;
     final Path genappDir;
     final Path controlDir;
     final Path clickstackDir;
     /**
      * ./server
      */
     final Path catalinaBase;
     final Path warFile;
     final Path logDir;
     final Path tmpDir;
     final Path agentLibDir;
 
     /**
      * @param appDir        parent folder of the instantiated app with {@code catalina.home}, {@code catalina.base}, ...
      * @param clickstackDir parent folder of the tomcat8-clickstack
      * @param packageDir    parent folder of {@code app.war}
      */
     public Setup2(@Nonnull Path appDir, @Nonnull Path clickstackDir, @Nonnull Path packageDir) throws IOException {
         this.appDir = appDir;
 
         this.genappDir = Files.createDirectories(appDir.resolve(".genapp"));
 
         this.controlDir = Files.createDirectories(genappDir.resolve("control"));
         this.logDir = Files.createDirectories(genappDir.resolve("log"));
         Files2.chmodReadWrite(logDir);
 
         this.catalinaHome = Files.createDirectories(appDir.resolve("tomcat8"));
 
         this.catalinaBase = Files.createDirectories(appDir.resolve("server"));
 
         this.agentLibDir = Files.createDirectories(catalinaHome.resolve("agent-lib"));
 
 
         this.tmpDir = Files.createDirectories(appDir.resolve("tmp"));
         Files2.chmodReadWrite(tmpDir);
 
         this.clickstackDir = clickstackDir;
         Preconditions.checkState(Files.exists(clickstackDir) && Files.isDirectory(clickstackDir));
 
         this.warFile = packageDir.resolve("app.war");
         Preconditions.checkState(Files.exists(warFile) && !Files.isDirectory(warFile));
     }
 
    public void installTomcatJavaOpts() throws IOException {
        Path optsFile = controlDir.resolve("java-opts-20-tomcat-opts");

        String opts = "" +
                "-Djava.io.tmpdir=\"" + tmpDir + "\" " +
                "-Dcatalina.home=\"" + catalinaHome + "\" " +
                "-Dcatalina.base=\"" + catalinaBase + "\" " +
                "-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager " +
                "-Djava.util.logging.config.file=\"" + catalinaBase + "/conf/logging.properties\"";

        Files.write(optsFile, Collections.singleton(opts), Charsets.UTF_8);
    }

     public void installCatalinaHome() throws Exception {
 
         // echo "Installing tomcat8"
         Files2.unzip(clickstackDir.resolve("lib/tomcat8.zip"), catalinaHome);
         // echo "Installing external libraries"
         Path targetLibDir = Files.createDirectories(catalinaHome.resolve("lib"));
         Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "cloudbees-web-container-extras", targetLibDir);
         Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "genapp-setup-tomcat8", targetLibDir);
 
         // JDBC Drivers
         Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "mysql-connector-java", targetLibDir);
         Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "postgresql", targetLibDir);
 
         // Mail
         Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "mail", targetLibDir);
         Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "activation", targetLibDir);
 
         Files2.chmodReadOnly(catalinaHome);
     }
 
     public void installCatalinaBase() throws IOException {
         Files2.copyDirectoryContent(clickstackDir.resolve("server"), catalinaBase);
 
         Path workDir = Files.createDirectories(catalinaBase.resolve("work"));
         Files2.chmodReadWrite(workDir);
 
         Path logsDir = Files.createDirectories(catalinaBase.resolve("logs"));
         Files2.chmodReadWrite(logsDir);
 
         Path rootWebAppDir = Files.createDirectories(catalinaBase.resolve("webapps/ROOT"));
         Files2.unzip(warFile, rootWebAppDir);
         Files2.chmodReadWrite(rootWebAppDir);
     }
 
     public void installJmxTransAgent() throws IOException {
 
 
         Path jmxtransAgentJarFile = Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "jmxtrans-agent", agentLibDir);
         Path jmxtransAgentConfigurationFile = catalinaBase.resolve("conf/tomcat8-metrics.xml");
         Preconditions.checkState(Files.exists(jmxtransAgentConfigurationFile), "File %s does not exist", jmxtransAgentConfigurationFile);
         Path jmxtransAgentDataFile = logDir.resolve("tomcat8-metrics.data");
 
         Path agentOptsFile = controlDir.resolve("java-opts-60-jmxtrans-agent");
 
         String agentOptsFileData =
                 "-javaagent:" + jmxtransAgentJarFile.toString() + "=" + jmxtransAgentConfigurationFile.toString() +
                         " -Dtomcat8_metrics_data_file=" + jmxtransAgentDataFile.toString();
 
         Files.write(agentOptsFile, Collections.singleton(agentOptsFileData), Charsets.UTF_8);
     }
 
     public void installCloudBeesJavaAgent() throws IOException {
         Path cloudbeesJavaAgentJarFile = Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "run-javaagent", this.agentLibDir);
         Path agentOptsFile = controlDir.resolve("java-opts-20-java-agent");
 
         String agentOptsFileData = "-javaagent:" +
                 cloudbeesJavaAgentJarFile +
                 "=sys_prop:" + controlDir.resolve("env");
 
         Files.write(agentOptsFile, Collections.singleton(agentOptsFileData), Charsets.UTF_8);
     }
 
     public void writeJavaOpts(Metadata metadata) throws IOException {
         Path javaOptsFile = controlDir.resolve("java-opts-10-core");
         String javaOpts = metadata.getRuntimeParameter("java", "opts", "");
         Files.write(javaOptsFile, Collections.singleton(javaOpts), Charsets.UTF_8);
     }
 
     public void writeConfig(Metadata metadata, String appPort) throws IOException {
 
         Path configFile = controlDir.resolve("config");
         PrintWriter writer = new PrintWriter(Files.newOutputStream(configFile));
 
         writer.println("app_dir=\"" + appDir + "\"");
         writer.println("app_tmp=\"" + appDir.resolve("tmp") + "\"");
         writer.println("log_dir=\"" + logDir + "\"");
         writer.println("catalina_home=\"" + catalinaHome + "\"");
         writer.println("catalina_base=\"" + catalinaBase + "\"");
 
         writer.println("port=" + appPort);
 
         Path javaPath = findJava(metadata);
         writer.println("java=\"" + javaPath + "\"");
         Path javaHome = findJavaHome(metadata);
         writer.println("JAVA_HOME=\"" + javaHome + "\"");
         writer.println("genapp_dir=\"" + genappDir + "\"");
 
         writer.println("catalina_opts=\"-Dport.http=" + appPort + "\"");
 
         // We installed additional libraries in $tomcat8_dir/lib at the
         // install_tomcat8 step, which means that we have to add them to the CP.
         String classpath = "" +
                 catalinaHome.resolve("bin/bootstrap.jar") + ":" +
                 catalinaHome.resolve("bin/tomcat-juli.jar") + ":" +
                 catalinaHome.resolve("lib");
         writer.println("java_classpath=\"" + classpath + "\"");
 
         writer.close();
     }
 
     public void installControlScripts() throws IOException {
         Files2.copyDirectoryContent(clickstackDir.resolve("control"), controlDir);
         Files2.chmodReadExecute(controlDir);
 
         Path genappLibDir = genappDir.resolve("lib");
         Files.createDirectories(genappLibDir);
 
         Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "cloudbees-jmx-invoker", genappLibDir);
     }
 
     public Path findJava(Metadata metadata) {
         Path javaPath = findJavaHome(metadata).resolve("bin/java");
         Preconditions.checkState(Files.exists(javaPath), "Java executable %s does not exist");
         Preconditions.checkState(!Files.isDirectory(javaPath), "Java executable %s is not a file");
 
         return javaPath;
     }
 
     public Path findJavaHome(Metadata metadata) {
        String javaVersion = metadata.getRuntimeParameter("java", "version", DEFAULT_JAVA_VERSION);
         Map<String, String> javaHomePerVersion = new HashMap<>();
         javaHomePerVersion.put("1.6", "/opt/java6");
         javaHomePerVersion.put("1.7", "/opt/java7");
         javaHomePerVersion.put("1.8", "/opt/java8");
 
         String javaHome = javaHomePerVersion.get(javaVersion);
         if (javaHome == null) {
             javaHome = javaHomePerVersion.get(DEFAULT_JAVA_VERSION);
         }
         Path javaHomePath = FileSystems.getDefault().getPath(javaHome);
         Preconditions.checkState(Files.exists(javaHomePath), "JavaHome %s does not exist");
         Preconditions.checkState(Files.isDirectory(javaHomePath), "JavaHome %s is not a directory");
         return javaHomePath;
     }
 }
