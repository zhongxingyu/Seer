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
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
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
 import com.photon.phresco.plugins.parser.TRXParser;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.plugins.util.PluginPackageUtil;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
 import com.photon.phresco.util.Utility;
 
 
 public class UnitTest implements PluginConstants {
 
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
 	private String winStoreUnitTestDirectory = "\\source\\test\\" + WINSTORE_UNIT_TEST_PROJECT_ROOT;
 	private String wpUnitTestDirectory = "\\source\\test\\" + WP_UNIT_TEST_PROJECT_ROOT;
 	private File buildFile;
 	private File[] solutionFile;
 	private File[] csprojFile;
 	private WP8PackageInfo packageInfo;
 	private File rootDir;
 	private Log log;
 	private PluginPackageUtil util;
 	private File unitDestPath;
 	
 	public void runUnitTest(Configuration configuration, MavenProjectInfo mavenProjectInfo, Log log) throws PhrescoException {
 		
 		this.log = log;
 		baseDir = mavenProjectInfo.getBaseDir();
 		
         Map<String, String> configs = MojoUtil.getAllValues(configuration);
         /*environmentName = configs.get(ENVIRONMENT_NAME);
         buildName = configs.get(BUILD_NAME); */
         buildNumber = configs.get(BUILD_NUMBER); 
         platform = configs.get(PLATFORM);
         config = configs.get(CONFIG);
         type = configs.get(WINDOWS_PLATFORM_TYPE);
         util = new PluginPackageUtil();
         
 		try {
 			executePackage();
 			executeUnitTest();
 			
 		} catch (MojoExecutionException e) {
 			throw new PhrescoException(e);
 		}
 		
 	}
 
 	private void executePackage() throws MojoExecutionException {
 		packageInit();
 		if(type.equalsIgnoreCase(WIN_STORE)) {
 			try {
 				Boolean status = generateWP8UnitTestPackage();
 				writeBuildInfo(status);
 			} catch (PhrescoException e) {				
 			}
 		} else if (type.equalsIgnoreCase(WIN_PHONE)) {
 			Boolean status = generateWP7UnitTestPackage();
 			writeBuildInfo(status);
 		}
 	}
 
 	private void executeUnitTest() throws MojoExecutionException {
 		try {
 			if (type.equalsIgnoreCase(WIN_STORE)) {
 				generateBuild();
 				extractBuild();
 				String extension = APPX_EXTENSION;
 				runUnitTest(extension);
 				parseTrx();
 			} else if (type.equalsIgnoreCase(WIN_PHONE)) {
 				generateBuild();
 				extractBuild();
 				String extension = XAP_EXTENSION;
 				runUnitTest(extension);
 				parseTrx();
 			} 
 		} catch (MojoExecutionException e) {
 			throw new MojoExecutionException(e.getMessage());
 		} catch (PhrescoException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void parseTrx() throws PhrescoException {
 		TRXParser parser = new TRXParser();
 		File trxFile = new File(unitDestPath + File.separator + TEST_RESULTS);
 		File[] files = trxFile.listFiles(new FilenameFilter() {
 			public boolean accept(File dir, String fileName) {
 				return  fileName.endsWith(TRX_EXTENSION);
 			}
 		});
 
 		for (File file : files) {
 			trxFile = new File(trxFile.getPath() + File.separator + file.getName());
 			parser.parsingTrx(trxFile, buildDir);
 		}
 	}
 
 	/*
 	 * Methods related to Package
 	 */
 	private void packageInit() throws MojoExecutionException {
 		try {
 			
 			if (StringUtils.isEmpty(type) || (!type.equals(WIN_PHONE) && !type.equals(WIN_STORE))) {
 				packageCallUsage();
 			}
 			
 			getProjectRoot();
 			getCSProjectFile();
 			log.info("csproject file name = " + csprojFile[0].getName());
 			
 			if(type.equalsIgnoreCase(WIN_STORE)) {
 				packageInfo = new WP8PackageInfo(rootDir);
 				buildDir = new File(rootDir.getParent() + BUILD_DIRECTORY);
 				if (buildDir.exists()) {
 					FileUtils.deleteDirectory(buildDir);
 				}
 				buildDir.mkdirs();
 			} else if(type.equalsIgnoreCase(WIN_PHONE)) {
 				buildDir = new File(rootDir.getParent() + BUILD_DIRECTORY);
 				if (buildDir.exists()) {
 					FileUtils.deleteDirectory(buildDir);
 					log.info("Build directory deleted..");
 				}
 				buildDir.mkdirs();
 			}
 			
 			
 			buildInfoFile = new File(buildDir.getPath() + BUILD_INFO_FILE);
 			
 			nextBuildNo = util.generateNextBuildNo(buildInfoFile);
 			currentDate = Calendar.getInstance().getTime();
 			
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	private void generateBuild() {
 		File configurationInfo = null;
 		try {
 			if(type.equalsIgnoreCase(WIN_STORE)) {
 				String packageVersion = packageInfo.getPackageVersion();
 				configurationInfo = new File(rootDir.getPath() + WP_APP_PACKAGE + File.separator + WINSTORE_UNIT_TEST_PROJECT_ROOT + STR_UNDERSCORE + packageVersion + STR_UNDERSCORE + (platform.equalsIgnoreCase("anycpu")?"AnyCPU":platform) + (config.equalsIgnoreCase("debug")? STR_UNDERSCORE + config : "") + WP_TEST);
 			} else if (type.equalsIgnoreCase(WIN_PHONE)) {
				if (platform.equalsIgnoreCase(WP_X86)) {
					configurationInfo = new File(rootDir.getPath() + File.separator +  BIN + File.separator + platform + File.separator + config);
				} else if (platform.equalsIgnoreCase(WP_ANY_CPU)) {
					configurationInfo = new File(rootDir.getPath() + File.separator +  BIN + File.separator + config);
				}
 			}
 			String zipName = util.createPackage(buildName, buildNumber, nextBuildNo, currentDate);
 			File destPath = new File(buildDir.getPath() +File.separator + zipName );
 			ArchiveUtil.createArchive(configurationInfo.getPath(), destPath.getPath(), ArchiveType.ZIP);
 			deleteDir();
 		} catch (PhrescoException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void deleteDir() throws IOException {
 		FileUtils.deleteDirectory(new File(rootDir.getPath() + File.separator + BIN));
 		FileUtils.deleteDirectory(new File(rootDir.getPath() + File.separator + OBJ));
		File appDir = new File(rootDir.getPath() + WP_APP_PACKAGE);
 		if (appDir.exists()) {
 			FileUtils.deleteDirectory(appDir);
 		}
 	}
 
 	private void packageCallUsage() throws MojoExecutionException {
 		log.error("Invalid usage.");
 		log.info("Usage of UnitTest Goal");
 		log.info("mvn windows-phone:unit-test -Dtype=\"Windows Phone platform\"");
 		throw new MojoExecutionException("Invalid Usage. Please see the Usage of UnitTest Goal");
 	}
 	
 	private void getProjectRoot() throws MojoExecutionException {
 		try {
 			// Get the source/<ProjectRoot> folder
 			if(type.equalsIgnoreCase(WIN_STORE)) {
 				rootDir = new File(baseDir.getPath() + winStoreUnitTestDirectory);
 			} else if (type.equalsIgnoreCase(WIN_PHONE)) {
 				rootDir = new File(baseDir.getPath() + wpUnitTestDirectory);
 			}
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	private void getCSProjectFile() throws MojoExecutionException {
 		try {
 			// Get .csproj file from the source folder
 			csprojFile = rootDir.listFiles(new FilenameFilter() { 
 				public boolean accept(File dir, String name) { 
 					return name.endsWith(WP_CSPROJ);
 				}
 			});	
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	private Boolean generateWP7UnitTestPackage() throws MojoExecutionException {
 		BufferedReader in = null;
 		boolean isBuildSuccess = true;
 		try {
 			log.info("Building unit test project ...");
 			// MSBuild UnitTest.csproj /t:Rebuild /p:configuration=Release
 			StringBuilder sb = new StringBuilder();
 			sb.append(WP_MSBUILD_PATH);
 			sb.append(STR_SPACE);
 			sb.append(csprojFile[0].getName());
 			sb.append(STR_SPACE);
 			sb.append(WP_STR_TARGET);
 			sb.append(WP_STR_COLON);
 			sb.append("Rebuild");
 			sb.append(STR_SPACE);
 			sb.append(WP_STR_PROPERTY);
 			sb.append(WP_STR_COLON);
 			sb.append(WP_STR_CONFIGURATION + "=" + config);
			if (platform.equalsIgnoreCase("AnyCPU")) {
				sb.append(WP_STR_SEMICOLON);
				sb.append(WP_STR_PLATFORM + "=" + WP_STR_DOUBLEQUOTES + platform + WP_STR_DOUBLEQUOTES);
			}
 			log.info("Command = "+ sb.toString());
 			Commandline cl = new Commandline(sb.toString());
 			cl.setWorkingDirectory(rootDir.getPath());
 			Process process = cl.execute();
 			in = new BufferedReader(new InputStreamReader(process.getInputStream()));
 			while ((in.readLine()) != null) {
 //				log.info("while = "+ in.readLine());
 			}
 		} catch (CommandLineException e) {
 			isBuildSuccess = false;
 			e.printStackTrace();
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (IOException e) {
 			isBuildSuccess = false;
 			e.printStackTrace();
 			throw new MojoExecutionException(e.getMessage(), e);
 		} finally {
 			Utility.closeStream(in);
 		}
 		return isBuildSuccess;
 	}
 	
 	private Boolean generateWP8UnitTestPackage() throws MojoExecutionException, PhrescoException {
 		BufferedReader in = null;
 		boolean isBuildSuccess = true;
 		try {
 			String packageVersion = packageInfo.getPackageVersion();
 			checkPackageVersionNo();
 			log.info("Building unit test project ...");
 			
 			// MSBuild MyApp.sln /t:Rebuild /p:Configuration=Release;Platform="Any CPU"
 			StringBuilder sb = new StringBuilder();
 			sb.append(WP_MSBUILD_PATH);
 			sb.append(STR_SPACE);
 			sb.append(csprojFile[0].getName());
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
 			
 			log.info("Command = "+ sb.toString());
 			Commandline cl = new Commandline(sb.toString());
 			cl.setWorkingDirectory(rootDir.getPath());
 			
 			Process process = cl.execute();
 			in = new BufferedReader(new InputStreamReader(process.getInputStream()));
 			while ((in.readLine()) != null) {
 			}
 		} catch (CommandLineException e) {
 			isBuildSuccess = false;
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (IOException e) {
 			isBuildSuccess = false;
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (PhrescoException e) {
 			isBuildSuccess = false;
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeStream(in);
 		}
 		return isBuildSuccess;
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
 
 	private void writeBuildInfo(boolean isBuildSuccess) throws MojoExecutionException {
 		util.writeBuildInfo(isBuildSuccess, buildName, buildNumber, nextBuildNo, environmentName, buildNo, currentDate, buildInfoFile);
 	}
 
 	private void extractBuild() throws MojoExecutionException {
 		try {
 			String zipName = util.createPackage(buildName, buildNumber, nextBuildNo, currentDate);
 			File sourcePath = new File(buildDir.getPath() + File.separator + zipName);
 			String removeExtension = FilenameUtils.removeExtension(sourcePath.getAbsolutePath());
 			unitDestPath = new File(removeExtension);
 			ArchiveUtil.extractArchive(sourcePath.getPath(), unitDestPath.getPath(), ArchiveType.ZIP);
 		} catch (PhrescoException e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		} 
 	}
 	
 	private void runUnitTest(final String extension) throws MojoExecutionException {
 		BufferedReader in = null;
 		try {
 			log.info("Running unit tests ...");
 			File[] appxFile = unitDestPath.listFiles(new FilenameFilter() { 
 				public boolean accept(File dir, String name) { 
 					return name.endsWith(extension);
 				}
 			});
 			// vstest.console <fileName>.appx /InIsolation /Logger:trx
 			StringBuilder sb = new StringBuilder();
 			sb.append(WINDOWS_UNIT_TEST_VSTEST_CONSOLE);
 			sb.append(STR_SPACE);
 			sb.append(appxFile[0].getName());
 			sb.append(STR_SPACE);
 			sb.append(WINDOWS_UNIT_TEST_INISOLATION);
 			sb.append(STR_SPACE);
 			sb.append(WINDOWS_UNIT_TEST_LOGGER);
 			log.info("UnitTest Command = "+ sb.toString());
 			Commandline cl = new Commandline(sb.toString());
 			cl.setWorkingDirectory(unitDestPath.getPath());
 			Process process = cl.execute();
 			in = new BufferedReader(new InputStreamReader(process.getInputStream()));
 			while ((in.readLine()) != null) {
 			}
 		} catch (CommandLineException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}  finally {
 			Utility.closeStream(in);
 		}
 	}
 }
