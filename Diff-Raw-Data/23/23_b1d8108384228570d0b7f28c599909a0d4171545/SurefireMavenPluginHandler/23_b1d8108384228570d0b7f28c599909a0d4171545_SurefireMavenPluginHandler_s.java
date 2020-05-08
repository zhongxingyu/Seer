 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2010 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.jacoco;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jacoco.core.runtime.AgentOptions;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.maven.MavenPlugin;
 import org.sonar.api.batch.maven.MavenPluginHandler;
 import org.sonar.api.batch.maven.MavenSurefireUtils;
 import org.sonar.api.resources.Project;
 import org.sonar.api.utils.HttpDownloader;
 import org.sonar.api.utils.SonarException;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class SurefireMavenPluginHandler implements MavenPluginHandler {
   private static final String ARG_LINE_PARAMETER = "argLine";
 
   private static File agentJarFile;
   private HttpDownloader downloader;
 
   public SurefireMavenPluginHandler(HttpDownloader downloader) {
     this.downloader = downloader;
   }
 
   public String getGroupId() {
     return MavenSurefireUtils.GROUP_ID;
   }
 
   public String getArtifactId() {
     return MavenSurefireUtils.ARTIFACT_ID;
   }
 
   public String getVersion() {
     return MavenSurefireUtils.VERSION;
   }
 
   public boolean isFixedVersion() {
     return false;
   }
 
   public String[] getGoals() {
     return new String[]{"test"};
   }
 
   protected File getAgentJarFile(Project project) {
     if (agentJarFile == null) {
       agentJarFile = downloadAgent(project);
     }
     return agentJarFile;
   }
 
   protected String getDownloadUrl(Project project) {
     String host = project.getConfiguration().getString("sonar.host.url", "http://localhost:9000");
     host = StringUtils.chomp(host, "/");
     return host + "/deploy/plugins/sonar-jacoco-plugin/agent-all-0.4.0.20100604151516.jar";
   }
 
   private File downloadAgent(Project project) {
     try {
       URI uri = new URI(getDownloadUrl(project));
       File agent = File.createTempFile("jacocoagent", ".jar");
       downloader.download(uri, agent);
       FileUtils.forceDeleteOnExit(agent);
       LoggerFactory.getLogger(getClass()).info("Agent: {}", agent);
       return agent;
     } catch (IOException e) {
       throw new SonarException(e);
     } catch (URISyntaxException e) {
       throw new SonarException(e);
     }
   }
 
   public void configure(Project project, MavenPlugin surefirePlugin) {
    String argLine = surefirePlugin.getParameter(ARG_LINE_PARAMETER);
 
     Configuration configuration = project.getConfiguration();
     AgentOptions options = new AgentOptions();
    options.setDestfile(JaCoCoSensor.getPath(project));
     String includes = configuration.getString(JaCoCoPlugin.INCLUDES_PROPERTY);
     if (StringUtils.isNotBlank(includes)) {
       options.setIncludes(includes);
     }
     String excludes = configuration.getString(JaCoCoPlugin.EXCLUDES_PROPERTY);
     if (StringUtils.isNotBlank(excludes)) {
       options.setExcludes(excludes);
     }

     String argument = options.getVMArgument(getAgentJarFile(project));
     argLine = StringUtils.isBlank(argLine) ? argument : argument + " " + argLine;
    LoggerFactory.getLogger(getClass()).info("JVM options: {}", argLine);
     surefirePlugin.setParameter(ARG_LINE_PARAMETER, argLine);
   }
 
 }
