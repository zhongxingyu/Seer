 package com.photon.phresco.plugins.nodejs;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.apache.commons.io.FileUtils;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.plugins.util.PluginsUtil;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
 
 public class Package implements PluginConstants {
 
 	private MavenProject project;
 	private File baseDir;
 	private String environmentName;
 	private String buildName;
 	private String buildNumber;
 	private int buildNo;
 
 	private File srcDir;
 	private File targetDir;
 	private File buildDir;
 	private File buildInfoFile;
 	private int nextBuildNo;
 	private Date currentDate;
 	private String sourceDirectory = "/source";
 	private Log log;
 	private PluginsUtil util;
 	
 	public void pack(Configuration configuration, MavenProjectInfo mavenProjectInfo, Log log) throws PhrescoException {
 		this.log = log;
 		baseDir = mavenProjectInfo.getBaseDir();
 		project = mavenProjectInfo.getProject();
         Map<String, String> configs = MojoUtil.getAllValues(configuration);
         environmentName = configs.get(ENVIRONMENT_NAME);
         buildName = configs.get(BUILD_NAME);
         buildNumber = configs.get(USER_BUILD_NUMBER);
         util = new PluginsUtil();
         
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
 			srcDir = new File(baseDir.getPath() + File.separator + sourceDirectory);
 			buildDir = new File(baseDir.getPath() + BUILD_DIRECTORY);
 			if (!buildDir.exists()) {
 				buildDir.mkdir();
 				log.info("Build directory created..." + buildDir.getPath());
 			}
 			targetDir = new File(project.getBuild().getDirectory());
 			buildInfoFile = new File(buildDir.getPath() + BUILD_INFO_FILE);
 			nextBuildNo = util.generateNextBuildNo(buildInfoFile);
 			currentDate = Calendar.getInstance().getTime();
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private boolean build() throws MojoExecutionException {
 		boolean isBuildSuccess = true;
 		try {
 			configure();
			FileUtils.copyDirectoryStructure(srcDir, targetDir);
 			createPackage();
 		} catch (IOException e) {
 			isBuildSuccess = false;
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 
 		return isBuildSuccess;
 	}
 	
 	private void createPackage() {
 		String zipName = util.createPackage(buildName, buildNumber, nextBuildNo, currentDate);
 		String zipFilePath = buildDir.getPath() + File.separator + zipName;
 		try {
 			ArchiveUtil.createArchive(targetDir.getPath(), zipFilePath, ArchiveType.ZIP);
 		} catch (PhrescoException e) {
 		}
 	}
 
 	private void configure() throws MojoExecutionException {
 		log.info("Configuring the project....");
 		File configfile = new File(baseDir + NODE_CONFIG_FILE);
 		String basedir = baseDir.getName();
 		PluginUtils pu = new PluginUtils();
 		pu.executeUtil(environmentName, basedir, configfile);
 	}
 
 	private void writeBuildInfo(boolean isBuildSuccess) throws MojoExecutionException {
 		util.writeBuildInfo(isBuildSuccess, buildName, buildNumber, nextBuildNo, environmentName, buildNo, currentDate, buildInfoFile);
 	}
 }
