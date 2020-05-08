 /*
  * ###
  * windows-phone-maven-plugin Maven Mojo
  * 
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 package com.photon.phresco.plugins.windows;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
import org.apache.commons.io.FileUtils;
 import org.codehaus.plexus.util.StringUtils;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.model.WP8PackageInfo;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.plugins.util.PluginPackageUtil;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
 import com.photon.phresco.util.Utility;
 
 
 public class Package implements PluginConstants {
 
 	private File baseDir;
 	private String environmentName;
 	private String buildName;
 	private String buildNumber;
 	private String type;
 	private String config;
 	private String platform;
 	private int buildNo;
 
 	private File buildDir;
 	private File buildInfoFile;
 	private File tempDir;
 	private int nextBuildNo;
 	private String zipName;
 	private Date currentDate;
 	private String sourceDirectory = "\\source";
 	private File[] solutionFile;
 	private File[] csprojFile;
 	private WP8PackageInfo packageInfo;
 	private File rootDir;
 	private Log log;
 	private PluginPackageUtil util;
 	
 	public void pack(Configuration configuration, MavenProjectInfo mavenProjectInfo, Log log) throws PhrescoException {
 		
 		this.log = log;
 		baseDir = mavenProjectInfo.getBaseDir();
         Map<String, String> configs = MojoUtil.getAllValues(configuration);
         environmentName = configs.get(ENVIRONMENT_NAME);
         buildName = configs.get(BUILD_NAME);
         buildNumber = configs.get(BUILD_NUMBER);
         platform = configs.get(PLATFORM);
         config = configs.get(CONFIG);
         type = configs.get(WINDOWS_PLATFORM_TYPE);
         util = new PluginPackageUtil();
         
 		try {
 			init();
 			if(type.equalsIgnoreCase(WP8_PLATFORM)) {
 				try {
 					generateWP8Package();
 				} catch (PhrescoException e) {				
 				}
 			} else {
 				generateWP7Package();
 			}
 			boolean buildStatus = build();
 			writeBuildInfo(buildStatus);
 			cleanUp();
 		} catch (MojoExecutionException e) {
 			throw new PhrescoException(e);
 		}
 		
 	}
 
 
 	private void init() throws MojoExecutionException {
 		try {
			if (StringUtils.isEmpty(environmentName) || StringUtils.isEmpty(type)) {
 				callUsage();
 			}
 			
 			getSolutionFile();
 			
 			if(type.equalsIgnoreCase(WP8_PLATFORM)) {
 				getProjectRoot();
 				getCSProjectFile();
 				packageInfo = new WP8PackageInfo(rootDir);
 			}
 			
 			buildDir = new File(baseDir.getPath() + BUILD_DIRECTORY);
 			if (!buildDir.exists()) {
 				buildDir.mkdirs();
 //				log.info("Build directory created..." + buildDir.getPath());
 			}
 			buildInfoFile = new File(buildDir.getPath() + BUILD_INFO_FILE);
 			nextBuildNo = util.generateNextBuildNo(buildInfoFile);
 			currentDate = Calendar.getInstance().getTime();
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	private void callUsage() throws MojoExecutionException {
 		log.error("Invalid usage.");
 		log.info("Usage of Package Goal");
 		log.info("mvn windows-phone:package -DenvironmentName=\"Multivalued evnironment names\"" 
 					+ " -Dtype=\"Windows Phone platform\"");
 		throw new MojoExecutionException("Invalid Usage. Please see the Usage of Package Goal");
 	}
 	
 	private void getSolutionFile() throws MojoExecutionException {
 		try {
 			// Get .sln file from the source folder
 			File sourceDir = new File(baseDir.getPath() + sourceDirectory);
 			solutionFile = sourceDir.listFiles(new FilenameFilter() { 
 				public boolean accept(File dir, String name) { 
 					return name.endsWith(WP_SLN);
 				}
 			});			
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	
 	private void getCSProjectFile() throws MojoExecutionException {
 		try {
 			// Get .csproj file from the source folder
 			File projRootDir = new File(rootDir.getPath());
 			csprojFile = projRootDir.listFiles(new FilenameFilter() { 
 				public boolean accept(File dir, String name) { 
 					return name.endsWith(WP_CSPROJ);
 				}
 			});			
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private void getProjectRoot() throws MojoExecutionException {
 		try {
 			// Get the source/<ProjectRoot> folder
			rootDir = new File(baseDir.getPath() + sourceDirectory + File.separator + WP_PROJECT_ROOT);
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	private void generateWP7Package() throws MojoExecutionException {
 		BufferedReader in = null;
 		try {
 			log.info("Building project ...");
 						
 			// MSBuild MyApp.sln /t:Rebuild /p:Configuration=Release
 			StringBuilder sb = new StringBuilder();
 			sb.append(WP_MSBUILD_PATH);
 			sb.append(STR_SPACE);
 			sb.append(baseDir.getPath() + sourceDirectory);
 			sb.append(WINDOWS_STR_BACKSLASH);
 			sb.append(solutionFile[0].getName());
 			sb.append(STR_SPACE);
 			sb.append(WP_STR_TARGET);
 			sb.append(WP_STR_COLON);
 			sb.append("Rebuild");
 			sb.append(STR_SPACE);
 			sb.append(WP_STR_PROPERTY);
 			sb.append(WP_STR_COLON);
 			sb.append(WP_STR_CONFIGURATION + "=" + config);
 			Commandline cl = new Commandline(sb.toString());
 			cl.setWorkingDirectory(baseDir.getPath() + sourceDirectory);
 			Process process = cl.execute();
 			in = new BufferedReader(new InputStreamReader(process.getInputStream()));
 			while ((in.readLine()) != null) {
 			}
 		} catch (CommandLineException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} finally {
 			Utility.closeStream(in);
 		}
 	}
 	
 	private void generateWP8Package() throws MojoExecutionException, PhrescoException {
 		BufferedReader in = null;
 		try {
 			
 			checkPackageVersionNo();
 			
 			log.info("Building project ...");
 			
 			// MSBuild MyApp.sln /t:Rebuild /p:Configuration=Release
 			StringBuilder sb = new StringBuilder();
 			sb.append(WP_MSBUILD_PATH);
 			sb.append(STR_SPACE);
 			sb.append(baseDir.getPath() + sourceDirectory);
 			sb.append(WINDOWS_STR_BACKSLASH);
 			sb.append(solutionFile[0].getName());
 			sb.append(STR_SPACE);
 			sb.append(WP_STR_TARGET);
 			sb.append(WP_STR_COLON);
 			sb.append("Rebuild");
 			sb.append(STR_SPACE);
 			sb.append(WP_STR_PROPERTY);
 			sb.append(WP_STR_COLON);
 			sb.append(WP_STR_CONFIGURATION + "=" + config);
 			sb.append(WP_STR_SEMICOLON);
 			sb.append(WP_STR_PLATFORM + "=" + WP_STR_DOUBLEQUOTES + platform + WP_STR_DOUBLEQUOTES);
 			Commandline cl = new Commandline(sb.toString());
 			cl.setWorkingDirectory(baseDir.getPath() + sourceDirectory);
 			Process process = cl.execute();
 			in = new BufferedReader(new InputStreamReader(process.getInputStream()));
 			while ((in.readLine()) != null) {
 			}
 		} catch (CommandLineException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeStream(in);
 		}
 	}
 
 	
 	private void checkPackageVersionNo() throws PhrescoException {
 		try {
 			SAXBuilder builder = new SAXBuilder();
 			File path = new File (rootDir.getPath() + File.separator + csprojFile[0].getName());
 			
 			Document doc = (Document) builder.build(path);
 			Element rootNode = doc.getRootElement();
 			Namespace ns = rootNode.getNamespace();
 			elementIdentifier(rootNode, WP_PROPERTYGROUP, ns);
 
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void elementIdentifier(Element rootNode, String elementName,	Namespace ns) {
 		List child = rootNode.getChildren(elementName, ns);
 		for (int i = 0; i < child.size(); i++) {
 			Object object = child.get(i);
 			Element project = (Element) object;
 			List children = project.getChildren();
 			for (Object object2 : children) {
 				Element propertyGroup = (Element) object2;
 				findChild(propertyGroup, ns);
 			}
 		}
 	}
 
 	private void findChild(Element element, Namespace ns) {				
 		try {
 			if (element.getName().equalsIgnoreCase(WP_AUTO_INCREMENT_PKG_VERSION_NO) && element.getValue().trim().equalsIgnoreCase("true")) {
 				packageInfo.incrementPackageVersionNo();
 			}
 		} catch (Exception e) {
 		}
 	}
 	
 	private boolean build() throws MojoExecutionException {
 		boolean isBuildSuccess = true;
 		try {
 			createPackage();
 		} catch (Exception e) {
 			isBuildSuccess = false;
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 		return isBuildSuccess;
 	}
 
 	private void createPackage() throws MojoExecutionException {
 		try {
 			zipName = util.createPackage(buildName, buildNumber, nextBuildNo, currentDate);
 			String zipFilePath = buildDir.getPath() + File.separator + zipName;
 			if(type.equalsIgnoreCase(WP8_PLATFORM)) {
 				String packageVersion = packageInfo.getPackageVersion();
 				String tempFilePath = rootDir.getPath() + WP_APP_PACKAGE + File.separator + WP_PROJECT_ROOT + STR_UNDERSCORE + packageVersion + STR_UNDERSCORE + (platform.equalsIgnoreCase("any cpu")?"AnyCPU":platform) + (config.equalsIgnoreCase("debug")? STR_UNDERSCORE + config : "") + WP_TEST;
 				tempDir = new File(tempFilePath);
 			} else if(type.equalsIgnoreCase("wp7")) {
 				String packageFolder = solutionFile[0].getName().substring(0, solutionFile[0].getName().length() - 4);
				tempDir = new File(baseDir + sourceDirectory + File.separator  + packageFolder + WP7_BIN_FOLDER + WP7_RELEASE_FOLDER);	
 			}
 			ArchiveUtil.createArchive(tempDir.getPath(), zipFilePath, ArchiveType.ZIP);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getErrorMessage(), e);
 		}
 	}
 
 	private void writeBuildInfo(boolean isBuildSuccess) throws MojoExecutionException {
 		util.writeBuildInfo(isBuildSuccess, buildName, buildNumber, nextBuildNo, environmentName, buildNo, currentDate, buildInfoFile);
 	}
 
 	private void cleanUp() throws MojoExecutionException {
 		try {
 			FileUtils.deleteDirectory(tempDir);
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 
 	}
 }
