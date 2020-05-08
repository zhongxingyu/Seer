 /**
  * dotnet-phresco-plugin
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
 package com.photon.phresco.plugins.dotnet;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.plugins.util.PluginPackageUtil;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
 import com.photon.phresco.util.Utility;
 
 public class Package implements PluginConstants {
 	
 	private MavenProject project;
 	private File baseDir;
 	private String environmentName;
 	private String buildName;
 	private String buildNumber;
 	private String configuaration;
 	private int buildNo;
 	
 	private File buildDir;
 	private File targetDir;
 	private File srcDir;
 	private File buildInfoFile;
 	private int nextBuildNo;
 	private String zipName;
 	private Date currentDate;
 	private Log log;
 	private PluginPackageUtil util;
 	
 	public void pack(Configuration configuration, MavenProjectInfo mavenProjectInfo, Log log) throws PhrescoException {
 		this.log = log;
 		baseDir = mavenProjectInfo.getBaseDir();
 		project = mavenProjectInfo.getProject();
         Map<String, String> configs = MojoUtil.getAllValues(configuration);
         environmentName = configs.get(ENVIRONMENT_NAME);
         buildName = configs.get(BUILD_NAME);
         buildNumber = configs.get(BUILD_NUMBER);
         configuaration = configs.get(BUILD_CONFIG);
         
         util = new PluginPackageUtil();
         PluginUtils.checkForConfigurations(baseDir, environmentName);
 		try {
 			init();
 			boolean buildStatus = build();
 			writeBuildInfo(buildStatus);
 		} catch (MojoExecutionException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void init() throws MojoExecutionException {
 		try {
 			srcDir = new File(project.getBuild().getSourceDirectory());
 			buildDir = new File(baseDir.getPath() + PluginConstants.BUILD_DIRECTORY);
 			if (!buildDir.exists()) {
 				buildDir.mkdirs();
 				log.info("Build directory created..." + buildDir.getPath());
 			}
 			buildInfoFile = new File(buildDir.getPath() + PluginConstants.BUILD_INFO_FILE);
 			targetDir = new File(project.getBuild().getDirectory());
 			nextBuildNo = util.generateNextBuildNo(buildInfoFile);
 			currentDate = Calendar.getInstance().getTime();
 		} catch (Exception e) {
 			log.error(e);
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	private boolean build() throws MojoExecutionException {
 		boolean isBuildSuccess = true;
 		try {
			executeMSBuildCmd();
 			createPackage();
 		} catch (Exception e) {
 			isBuildSuccess = false;
 			log.error(e);
 		}
 		return isBuildSuccess;
 	}
 
 	private void executeMSBuildCmd() throws MojoExecutionException {
 		BufferedReader in = null;
 		boolean errorParam = false;
 		try {
 			String[] list = srcDir.list(new CsFileNameFilter());
 			StringBuilder sb = new StringBuilder();
 			sb.append(MS_BUILD);
 			sb.append(STR_SPACE);
 			sb.append(list[0]);
 			sb.append(STR_SPACE);
 			sb.append(WP_STR_PROPERTY);
 			sb.append(COLON);
 			sb.append(WP_STR_CONFIGURATION + EQUAL + configuaration);
 			sb.append(WP_STR_SEMICOLON);
 			sb.append(DEPLOY_ON_BUILD);
 			sb.append(WP_STR_SEMICOLON);
 			sb.append(DEPLOY_TARGET);
 			sb.append(WP_STR_SEMICOLON);
 			sb.append(DEPLOY_TEMP_DIR);
 			log.info("executeMSBuildCmd() : " + sb.toString());
 //			sb.append("/p:Configuration=Release;DeployOnBuild=true;DeployTarget=Package;_PackageTempDir=..\\..\\..\\do_not_checkin\\target");
 			String line = null;
 			in = Utility.executeCommand(sb.toString(), srcDir.getPath());
 			while ((line = in.readLine()) != null) {
 				if (line.contains("Build FAILED")) {
 					errorParam = true;
 				}
 				System.out.println(line); // do not use getLog() here as this line already contains the log type.
 			}
 			if (errorParam) {
 				throw new MojoExecutionException("resolve above errros ..");
 			}
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} finally {
 			Utility.closeStream(in);
 		}
 	}
 
 	private void createPackage() throws MojoExecutionException {
 		try {
 			zipName = util.createPackage(buildName, buildNumber, nextBuildNo, currentDate);
 			String zipFilePath = buildDir.getPath() + File.separator + zipName;
 			File packageInfoFile = new File(baseDir.getPath() + File.separator + DOT_PHRESCO_FOLDER + File.separator + PHRESCO_PACKAGE_FILE);
 			if(packageInfoFile.exists()) {
 				PluginUtils.createBuildResources(packageInfoFile, baseDir, targetDir);
 			}
 			ArchiveUtil.createArchive(targetDir.getPath(), zipFilePath, ArchiveType.ZIP);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getErrorMessage(), e);
 		}
 	}
 	
 	private void writeBuildInfo(boolean isBuildSuccess) throws MojoExecutionException {
 		util.writeBuildInfo(isBuildSuccess, buildName, buildNumber, nextBuildNo, environmentName, buildNo, currentDate, buildInfoFile);
 	}
 	
 	class CsFileNameFilter implements FilenameFilter {
 
 		public boolean accept(File dir, String name) {
 			return name.endsWith(PluginConstants.WP_SLN);
 		}
 	}
 }
