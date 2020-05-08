 package com.photon.phresco.plugins;
 
 import java.io.*;
 import java.util.*;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.*;
 import org.apache.maven.plugin.logging.*;
 import org.apache.maven.project.*;
 import org.codehaus.plexus.util.*;
 
 import com.google.gson.*;
 import com.photon.phresco.commons.model.*;
 import com.photon.phresco.exception.*;
 import com.photon.phresco.plugin.commons.*;
 import com.photon.phresco.plugins.util.*;
 import com.photon.phresco.util.*;
 
 public class PreBuildStep  implements PluginConstants {
 	
     private MavenProject project;
     private File baseDir;
     private Log log;
     private PluginsUtil util;
     
 	public void performCIPreBuildStep(String jobName, MavenProjectInfo mavenProjectInfo, Log log) throws PhrescoException {
 		log.debug("CI prebuild step execution reached " + jobName);
 		log.info("CI prebuild step execution started " + jobName);
 		try {
 	        this.log = log;
 	        baseDir = mavenProjectInfo.getBaseDir();
 	        project = mavenProjectInfo.getProject();
 	        
 	        // getting jenkins workspace job dir
 	        String jenkinsJobDirPath = getJenkinsJobDirPath(jobName);
 	        log.info("jenkinsJobDirPath ... " + jenkinsJobDirPath);
 			File jenkinsJobDir = new File(jenkinsJobDirPath);
 			
 			// get projects plugin info file path
 			File projectInfo = new File(baseDir, DOT_PHRESCO_FOLDER + File.separator + PROJECT_INFO_FILE);
 			if (!projectInfo.exists()) {
 				throw new MojoExecutionException("Project info file is not found in jenkins workspace dir " + baseDir.getCanonicalPath());
 			}
 			log.info("projectInfo path ... " + projectInfo.getCanonicalPath());
 			ApplicationInfo appInfo = getApplicationInfo(projectInfo);
 			if (appInfo == null) {
 				throw new MojoExecutionException("AppInfo value is Null ");
 			}
 			
 			// get execution project's appid and dirname and search in phresco project workspace name and get phrescoPluginInfo file path
 			String appId = appInfo.getId();
 			log.info("appId ... " + appId);
 			String appDirName = appInfo.getAppDirName();
 			log.info("appDirName ... " + appDirName);
 			if (StringUtils.isEmpty(appDirName)) {
 				throw new MojoExecutionException("appDirName is empty");
 			}
 			File phrescoPluginInfoFile = getPhrescoPluginInfoFile(appDirName);
 			log.info("phresco Plugin Info File in phresco projects workspace ... " + phrescoPluginInfoFile.getCanonicalPath());
 			
 			FileUtils.copyFileToDirectory(phrescoPluginInfoFile, jenkinsJobDir);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	// it get plugininfo file from phresco workspace
 	private File getPhrescoPluginInfoFile(String appDirName)  throws MojoExecutionException {
 		log.info("getPhrescoPluginInfoFile method called  ... " + appDirName);
 		String projectHome = Utility.getProjectHome();
 		if (StringUtils.isEmpty(projectHome)) {
 			throw new MojoExecutionException("Phresco project home not found ");
 		}
 		StringBuilder sb = new StringBuilder(projectHome);
 		sb.append(appDirName);
 		sb.append(File.separator);
 		sb.append(DOT_PHRESCO_FOLDER);
 		sb.append(File.separator);
		sb.append(PLUGIN_INFO_FILE);
 		File pluginInfoFile = new File(sb.toString());
 		log.info("getPhrescoPluginInfoFile method fiel path  ... " + sb.toString());
 		if (!pluginInfoFile.exists()) {
 			throw new MojoExecutionException("Plugin info file not found in phresco projects workspace for appDirName... " + appDirName + " Searched in... " + sb.toString());
 		}
 		return pluginInfoFile;
 	}
 	
 	private ApplicationInfo getApplicationInfo(File projectInfoFile) throws MojoExecutionException {
 		log.info("getApplicationInfo method called  ... " + projectInfoFile);
 		try {
 	        Gson gson = new Gson();
 	        BufferedReader reader = null;
 	        reader = new BufferedReader(new FileReader(projectInfoFile));
 	        ProjectInfo projectInfo = gson.fromJson(reader, ProjectInfo.class);
 	        log.info("projectInfo name ... " + projectInfo.getName());
 	        List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 	        for (ApplicationInfo appInfo : appInfos) {
 	        	log.info("appInfo dir name ... " + appInfo.getAppDirName());
 				return appInfo;
 			}
 		} catch (Exception e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 		return null;
 	}
 	
 	private String getJenkinsJobDirPath(String jobName) throws MojoExecutionException {
 		log.info("getJenkinsJobDirPath method called ... ");
 		try {
 			String jenkinsHomePath = System.getenv(JENKINS_HOME);
 			if (StringUtils.isEmpty(jenkinsHomePath)) {
 				throw new PhrescoException("Jenkins Home not found in environemt variable ");
 			}
 			StringBuilder builder = new StringBuilder(jenkinsHomePath);
 			builder.append(File.separator);
 	        builder.append(WORKSPACE_DIR);
 	        builder.append(File.separator);
 	        builder.append(jobName);
 	        builder.append(File.separator);
 	        builder.append(DOT_PHRESCO_FOLDER);
 	        builder.append(File.separator);
 	        FileUtils.mkdir(builder.toString());
 	        log.info("Jenkins JobDir Path is  ... " + builder.toString());
 	        return builder.toString();
 		} catch (Exception e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 }
