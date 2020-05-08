 /**
  * java-phresco-plugin
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.plugins.java;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.ListUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.StringUtils;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.BuildInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.DatabaseUtil;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Dependency;
 import com.phresco.pom.util.PomProcessor;
 
 public class Deploy implements PluginConstants {
 	
 	private static final String CARGO_MAVEN2_PLUGIN = "cargo-maven2-plugin";
 	private static final String CODEHAUS_CARGO_PLUGIN = "org.codehaus.cargo";
 	private MavenProject project;
 	private File baseDir;
 	private String buildNumber;
 	private File buildDir;
 	private String environmentName;
 	private boolean importSql;
 	private File buildFile;
 	private File tempDir;
 	private String context;
 	private Map<String, String> serverVersionMap = new HashMap<String, String>();
 	private Log log;
 	private String sqlPath;
 	private PluginUtils pUtil;
 	private String servertype;
 	private String pomFile;
 	private String subModule = "";
 	private File workingDirectory;
 	
 	public void deploy(Configuration configuration, MavenProjectInfo mavenProjectInfo, Log log) throws PhrescoException {
 		this.log = log;
 		baseDir = mavenProjectInfo.getBaseDir();
 		project = mavenProjectInfo.getProject();
         Map<String, String> configs = MojoUtil.getAllValues(configuration);
         environmentName = configs.get(ENVIRONMENT_NAME);
         buildNumber = configs.get(BUILD_NUMBER);
         importSql = Boolean.parseBoolean(configs.get(EXECUTE_SQL));
         sqlPath = configs.get(FETCH_SQL);
         pUtil = new PluginUtils();
         subModule = mavenProjectInfo.getModuleName();
         if (StringUtils.isNotEmpty(subModule)) {
         	workingDirectory = new File(baseDir.getPath() + File.separator + subModule);
         } else {
         	workingDirectory = new File(baseDir.getPath());
         }
         pomFile = getPomFile().getName();
 		try {
 			init();
 			initMap();
 			updateFinalName();
 			createDb();
 			extractBuild();
 			deployToServer();
 			cleanUp();
 		} catch (MojoExecutionException e) {
 			throw new PhrescoException(e);
 		}
 		
 	}
 	
 	private void init() throws MojoExecutionException {
 		try {
 			if (StringUtils.isEmpty(buildNumber) || StringUtils.isEmpty(environmentName)) {
 				callUsage();
 			}
 			BuildInfo buildInfo = pUtil.getBuildInfo(Integer.parseInt(buildNumber), workingDirectory.getPath());
 			buildDir = new File(workingDirectory.getPath() + PluginConstants.BUILD_DIRECTORY);// build dir
 			buildFile = new File(buildDir.getPath() + File.separator + buildInfo.getBuildName());// filename
 			tempDir = new File(buildDir.getPath() + TEMP_DIR);// temp dir
 			tempDir.mkdirs();
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	private void initMap() {
 		serverVersionMap.put("tomcat-7.0.x", "tomcat7x");
 		serverVersionMap.put("tomcat-6.0.x", "tomcat6x");
 		serverVersionMap.put("tomcat-5.5.x", "tomcat5x");
 		serverVersionMap.put("jboss-7.1.x", "jboss71x");
 		serverVersionMap.put("jboss-7.0.x", "jboss7x");
 		serverVersionMap.put("jboss-6.1.x", "jboss61x");
 		serverVersionMap.put("jboss-6.0.x", "jboss6x");
 		serverVersionMap.put("jboss-5.1.x", "jboss51x");
 		serverVersionMap.put("jboss-5.0.x", "jboss5x");
 		serverVersionMap.put("jboss-4.2.x", "jboss42x");
 		serverVersionMap.put("jboss-4.0.x", "jboss4x");
 		serverVersionMap.put("jetty-6.x", "jetty6x");
 		serverVersionMap.put("jetty-7.x", "jetty7x");
 		serverVersionMap.put("jetty-8.x", "jetty8x");
 		serverVersionMap.put("jetty-9.x", "jetty9x");
 	}
 
 	private void callUsage() throws MojoExecutionException {
 		log.error("Invalid usage.");
 		log.info("Usage of Deploy Goal");
 		log.info(
 				"mvn java:deploy -DbuildName=\"Name of the build\""
 						+ " -DenvironmentName=\"Multivalued evnironment names\""
 						+ " -DimportSql=\"Does the deployment needs to import sql(TRUE/FALSE?)\"");
 		throw new MojoExecutionException("Invalid Usage. Please see the Usage of Deploy Goal");
 	}
 	
 	private void updateFinalName() throws MojoExecutionException {
 		try {
 			PomProcessor pomprocessor = new PomProcessor(getPomFile());
 			List<com.photon.phresco.configuration.Configuration> configurations = pUtil.getConfiguration(workingDirectory, environmentName, Constants.SETTINGS_TEMPLATE_SERVER);
 			if (CollectionUtils.isEmpty(configurations)) {
 				throw new MojoExecutionException("Configuration is not available ");
 			}
 			for (com.photon.phresco.configuration.Configuration configuration : configurations) {
 				context = configuration.getProperties().getProperty(Constants.SERVER_CONTEXT);
 				break;
 			}
 			pomprocessor.setFinalName(context);
 			pomprocessor.save();
 
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (PhrescoPomException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private void createDb() throws MojoExecutionException {
 		DatabaseUtil util = new DatabaseUtil();
 		try {
 			util.fetchSqlConfiguration(sqlPath, importSql, workingDirectory, environmentName);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private void extractBuild() throws MojoExecutionException {
 		try {
 			ArchiveUtil.extractArchive(buildFile.getPath(), tempDir.getPath(), ArchiveType.ZIP);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getErrorMessage(), e);
 		}
 	}
 
 	private void deployToServer() throws MojoExecutionException, PhrescoException {
 		try {
 			List<com.photon.phresco.configuration.Configuration> configurations = pUtil.getConfiguration(workingDirectory, environmentName, Constants.SETTINGS_TEMPLATE_SERVER);
 			for (com.photon.phresco.configuration.Configuration configuration : configurations) {
 				deploy(configuration);
 			}			
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getErrorMessage(), e);
 		}
 	}
 	
 	private File getPomFile() throws PhrescoException {
 		ApplicationInfo appInfo = pUtil.getAppInfo(workingDirectory);
 		String pomFileName = Utility.getPomFileNameFromWorkingDirectory(appInfo, workingDirectory);
 		File pom = new File(workingDirectory.getPath() + File.separator + pomFileName);
 		
 		return pom;
 	}
 	
 	private void deploy(com.photon.phresco.configuration.Configuration configuration) throws MojoExecutionException, PhrescoException {
 		if (configuration == null) {
 			return;
 		}
 		String serverhost = configuration.getProperties().getProperty(Constants.SERVER_HOST);
 		String serverport = configuration.getProperties().getProperty(Constants.SERVER_PORT);
 		String serverprotocol = configuration.getProperties().getProperty(Constants.SERVER_PROTOCOL);
 		String serverusername = configuration.getProperties().getProperty(Constants.SERVER_ADMIN_USERNAME);
 		String serverpassword = configuration.getProperties().getProperty(Constants.SERVER_ADMIN_PASSWORD);
 		String version = configuration.getProperties().getProperty(Constants.SERVER_VERSION);
 		servertype = configuration.getProperties().getProperty(Constants.SERVER_TYPE);
 		context = configuration.getProperties().getProperty(Constants.SERVER_CONTEXT);
 		String remotedeploy = configuration.getProperties().getProperty(Constants.SERVER_REMOTE_DEPLOYMENT);
 		String certificatePath = configuration.getProperties().getProperty(Constants.CERTIFICATE);
 		
 		String containerId = "";
 		renameWar(context);
 		
 		// local deployment
 		if (remotedeploy.equals("false")) {
 			deployLocal();
 			return;
 		}
 
 		// remote deployment
 		if (servertype.contains(TYPE_TOMCAT)) {
 			removeCargoDependency();
 			containerId = serverVersionMap.get("tomcat-" + version);
 			deployToServer(serverprotocol, serverhost, serverport, serverusername, serverpassword, containerId, certificatePath);
 		} else if (servertype.contains(TYPE_JBOSS)) {
 			addCargoDependency(version);
 			containerId = serverVersionMap.get("jboss-" + version);
 			deployToServer(serverprotocol, serverhost, serverport, serverusername, serverpassword, containerId, certificatePath);
 		} else if (servertype.contains(TYPE_JETTY)) {
 			removeCargoDependency();
 			containerId = serverVersionMap.get("jetty-" + version);
 			deployToServer(serverprotocol, serverhost, serverport, serverusername, serverpassword, containerId, certificatePath);
 		} else if (servertype.contains(TYPE_WEBLOGIC) && ((version.equals(Constants.WEBLOGIC_12c)) || (version.equals(Constants.WEBLOGIC_11gR1)))) {
 			deployToWeblogicServer(serverprotocol, serverhost, serverport, serverusername, serverpassword, version);
 		} 
 	}
 
 	private void addCargoDependency(String version) throws PhrescoException {
 		try {
			PomProcessor processor = new PomProcessor(project.getFile());
 			processor.deletePluginDependency(CODEHAUS_CARGO_PLUGIN, CARGO_MAVEN2_PLUGIN);
 			
 			//For Jboss4 dependency is not needed
 			if (version.startsWith("5.") || version.startsWith("6.")) {
 				addJBoss5xDependency(processor);
 			} else if (version.startsWith("7.")) {
 				addJBoss7xDependency(processor);
 			}
 			
 			processor.save();
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private void addJBoss7xDependency(PomProcessor processor) throws PhrescoPomException {
 		Dependency dependency = new Dependency();
 		dependency.setGroupId("org.jboss.as");
 		dependency.setArtifactId("jboss-as-controller-client");
 		dependency.setVersion("7.0.2.Final");
 		
 		processor.addPluginDependency(CODEHAUS_CARGO_PLUGIN, CARGO_MAVEN2_PLUGIN, dependency);	
 	}
 
 	private void addJBoss5xDependency(PomProcessor processor) throws PhrescoPomException {
 		Dependency dependency = new Dependency();
 		dependency.setGroupId("org.jboss.integration");
 		dependency.setArtifactId("jboss-profileservice-spi");
 		dependency.setVersion("5.1.0.GA");
 
 		processor.addPluginDependency(CODEHAUS_CARGO_PLUGIN, CARGO_MAVEN2_PLUGIN, dependency);
 		
 		Dependency dependency2 = new Dependency();
 		dependency2.setGroupId("org.jboss.jbossas");
 		dependency2.setArtifactId("jboss-as-client");
 		dependency2.setVersion("5.1.0.GA");
 		dependency2.setType("pom");
 
 		processor.addPluginDependency(CODEHAUS_CARGO_PLUGIN, CARGO_MAVEN2_PLUGIN, dependency2);
 	}
 
 	private void removeCargoDependency() throws PhrescoException {
 		try {
 			PomProcessor processor = new PomProcessor(getPomFile());
 			processor.deletePluginDependency(CODEHAUS_CARGO_PLUGIN, CARGO_MAVEN2_PLUGIN);
 			processor.save();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void renameWar(String context) throws MojoExecutionException {
 		String contextName = context + ".war";
 		String warFileName = "";
 		String[] list = tempDir.list(new JDWarFileNameFilter());
 		if (list.length > 0) {
 			warFileName = list[0];
 			if (!warFileName.equals(contextName)) {
 				File oldWar = new File(tempDir.getPath() + "/" + warFileName);
 				File newWar = new File(tempDir.getPath() + "/" + contextName);
 				oldWar.renameTo(newWar);
 			}
 		}
 	}
 	
 	
 	private void deployToServer(String serverprotocol, String serverhost, String serverport,
 			String serverusername, String serverpassword, String containerId, String certificatePath) throws MojoExecutionException {
 		BufferedReader bufferedReader = null;
 		boolean errorParam = false;
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append(MVN_CMD);
 			sb.append(STR_SPACE);
 			sb.append(JBOSS_GOAL);
 			sb.append(STR_SPACE);
 			sb.append(SERVER_PROTOCOL);
 			sb.append(serverprotocol);
 			sb.append(STR_SPACE);
 			sb.append(SERVER_HOST);
 			sb.append(serverhost);
 			sb.append(STR_SPACE);
 			sb.append(SERVER_PORT);
 			sb.append(serverport);
 			sb.append(STR_SPACE);
 			sb.append(SERVER_USERNAME);
 			sb.append(serverusername);
 			sb.append(STR_SPACE);
 			sb.append(SERVER_PASSWORD);
 			sb.append(serverpassword);
 			sb.append(STR_SPACE);
 			sb.append(SERVER_CONTAINER_ID);
 			sb.append(containerId);
 			sb.append(STR_SPACE);
 			sb.append(SREVER_CONTEXT);
 			sb.append(context);
 			sb.append(STR_SPACE);
 			sb.append(SKIP_TESTS);
 			if(!Constants.POM_NAME.equals(pomFile)) {
 				sb.append(STR_SPACE);
 				sb.append(Constants.HYPHEN_F);
 				sb.append(STR_SPACE);
 				sb.append(pomFile);
 			}
 			
 			if (serverprotocol.equals(HTTPS) && certificatePath != null) {
 				File certificateFile = null;
 				if (new File(workingDirectory, certificatePath).exists()) {
 					certificateFile = new File(certificatePath);
 				} else {
 					certificateFile = new File("../" + certificatePath);
 				}
 				sb.append(STR_SPACE);
 				sb.append(JAVAX_TRUSTSTORE);
 				sb.append(certificateFile.getPath());
 				sb.append(STR_SPACE);
 				sb.append(JAVAX_TRUSTSTORE_PWD);
 				sb.append(DEFAULT_PWD);
 				sb.append(STR_SPACE);
 				sb.append(JAVAX_KEYSTORE);
 				sb.append(certificateFile.getPath());
 				sb.append(STR_SPACE);
 				sb.append(JAVAX_KEYSTORE_PWD);
 				sb.append(DEFAULT_PWD);
 			}
 
 			bufferedReader = Utility.executeCommand(sb.toString(), workingDirectory.getPath());
 			String line = null;
 			if(!servertype.equalsIgnoreCase("Jetty")) {
 			while ((line = bufferedReader.readLine()) != null) {
 				if (line.startsWith("[ERROR]")) {
 					System.out.println(line); //do not use getLog() here as this line already contains the log type.
 					errorParam = true;
 				}
 			} 
 		} else {
 			while ((line = bufferedReader.readLine()) != null) {
 				if (line.startsWith("[ERROR]") && line.contains("Unexpected error when trying to start the webapp")) {
 					errorParam = false;
 					break;
 				} else if (line.startsWith("[ERROR]")) {
 					System.out.println(line); //do not use getLog() here as this line already contains the log type.
 					errorParam = true;
 				}
 			}
 		}
 			if (errorParam) {
 				throw new MojoExecutionException("Remote Deployment Failed ");
 			} else {
 				log.info(
 						" Project is Deploying into " + serverprotocol + "://" + serverhost + ":" + serverport + "/"
 								+ context);
 			}
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}  finally {
 			Utility.closeStream(bufferedReader);
 		}
 	}
 
 
 	private void deployToWeblogicServer(String serverprotocol, String serverhost, String serverport, String serverusername,
 			String serverpassword, String version) throws MojoExecutionException {
 		BufferedReader bufferedReader = null;
 		boolean errorParam = false;
 		try {
 			String webLogicVersion = "";
 			if (version.equals(Constants.WEBLOGIC_12c)) {
 				webLogicVersion = Constants.WEBLOGIC_12c_PLUGIN_VERSION;
 			} else if (version.equals(Constants.WEBLOGIC_11gR1)) {
 				webLogicVersion = Constants.WEBLOGIC_11gr1c_PLUGIN_VERSION;
 			} 
 			StringBuilder sb = new StringBuilder();
 			sb.append(MVN_CMD);
 			sb.append(STR_SPACE);
 			sb.append(WEBLOGIC_GOAL + webLogicVersion + WEBLOGIC_REDEPLOY);
 			sb.append(STR_SPACE);
 			sb.append(SERVER_HOST);
 			sb.append(serverhost);
 			sb.append(STR_SPACE);
 			sb.append(SERVER_PORT);
 			sb.append(serverport);
 			sb.append(STR_SPACE);
 			sb.append(SERVER_USERNAME);
 			sb.append(serverusername);
 			sb.append(STR_SPACE);
 			sb.append(SERVER_PASSWORD);
 			sb.append(serverpassword);
 			sb.append(STR_SPACE);
 			sb.append(SKIP_TESTS);
 			
 						
 			 bufferedReader = Utility.executeCommand(sb.toString(), workingDirectory.getPath());
 				String line = null;
 				while ((line = bufferedReader.readLine()) != null) {
 					if (line.startsWith("[ERROR]")) {
 						System.out.println(line);  //do not use getLog() here as this line already contains the log type.
 						errorParam = true;
 					}
 				}
 				if (errorParam) {
 					throw new MojoExecutionException("Remote Deploy Failed ");
 				} else {
 					log.info(
 							" Project is Deploying into " + serverprotocol + "://" + serverhost + ":" + serverport + "/"
 									+ context);
 				}
 			} catch (IOException e) {
 				throw new MojoExecutionException(e.getMessage(), e);
 			} finally {
 				Utility.closeStream(bufferedReader);
 			}
 	}
 	
 	private void deployLocal() throws MojoExecutionException {
 		String deployLocation = "";
 		try {
 			List<com.photon.phresco.configuration.Configuration> configurations = pUtil.getConfiguration(workingDirectory, environmentName, Constants.SETTINGS_TEMPLATE_SERVER);
 			for (com.photon.phresco.configuration.Configuration configuration : configurations) {
 				deployLocation = configuration.getProperties().getProperty(Constants.SERVER_DEPLOY_DIR);
 				break;
 			}		
 			File deployDir = new File(deployLocation);
 			if (!deployDir.exists()) {
 				throw new MojoExecutionException(" Deploy Directory " + deployLocation + " Does Not Exists ");
 			}
 			log.info("Project is deploying into " + deployLocation);
 			String[] list = tempDir.list(new JDWarFileNameFilter());
 			if (list.length > 0) {
 				File warFile = new File(tempDir.getPath() + File.separator + list[0]);
 				FileUtils.copyFileToDirectory(warFile, deployDir);
 			}
 			if(ArrayUtils.isEmpty(list)) {
 				File file = tempDir.listFiles()[0];
 				org.apache.commons.io.FileUtils.copyDirectoryToDirectory(file, deployDir);
 			}
 //			FileUtils.copyDirectoryStructure(tempDir.getAbsoluteFile(), deployDir);
 			log.info("Project is deployed successfully");
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	public class PhrescoDirFilter implements FilenameFilter {
 
         public boolean accept(File dir, String name) {
             return name.equals(DOT_PHRESCO_FOLDER);
         }
     }
 	
 	private void cleanUp() throws MojoExecutionException {
 		try {
 			FileUtils.deleteDirectory(tempDir);
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 }
 
 class JDWarFileNameFilter implements FilenameFilter {
 
 	public boolean accept(File dir, String name) {
 		return name.endsWith(".war");
 	}
 
 }
