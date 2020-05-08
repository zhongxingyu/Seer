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
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.jacoco;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.sonar.api.BatchExtension;
 import org.sonar.api.utils.HttpDownloader;
 import org.sonar.api.utils.SonarException;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class JaCoCoAgentDownloader extends HttpDownloader implements BatchExtension {
 
   private static File agentJarFile;
   private String host;
 
   public JaCoCoAgentDownloader(Configuration configuration) {
     host = StringUtils.chomp(configuration.getString("sonar.host.url", "http://localhost:9000"), "/");
   }
 
   protected String getDownloadUrl() {
     return host + "/deploy/plugins/sonar-jacoco-plugin/agent-all-0.4.0.20100604151516.jar";
   }
 
  protected File getAgentJarFile() {
     if (agentJarFile == null) {
       agentJarFile = downloadAgent();
     }
     return agentJarFile;
   }
 
   protected File downloadAgent() {
     try {
       URI uri = new URI(getDownloadUrl());
       File agent = File.createTempFile("jacocoagent", ".jar");
       download(uri, agent);
       FileUtils.forceDeleteOnExit(agent);
       JaCoCoUtils.LOG.info("JaCoCo agent downloaded: {}", agent);
       return agent;
     } catch (IOException e) {
       throw new SonarException(e);
     } catch (URISyntaxException e) {
       throw new SonarException(e);
     }
   }
 
 }
