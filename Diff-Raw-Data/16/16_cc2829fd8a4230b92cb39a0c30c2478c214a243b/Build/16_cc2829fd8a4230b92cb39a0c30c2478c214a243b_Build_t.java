 /*
  * ###
  * Framework Web Archive
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
 package com.photon.phresco.framework.actions.applications;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.google.gson.Gson;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.BuildInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.configuration.ConfigurationInfo;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.api.ActionType;
 import com.photon.phresco.framework.api.ApplicationManager;
 import com.photon.phresco.framework.api.Project;
 import com.photon.phresco.framework.api.ProjectAdministrator;
 import com.photon.phresco.framework.api.ProjectManager;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.commons.LogErrorReport;
 import com.photon.phresco.framework.model.DependantParameters;
 import com.photon.phresco.framework.model.PluginProperties;
 import com.photon.phresco.framework.model.SettingsInfo;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.TechnologyTypes;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.android.AndroidProfile;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Plugin;
 import com.phresco.pom.model.PluginExecution;
 import com.phresco.pom.model.PluginExecution.Configuration;
 import com.phresco.pom.model.PluginExecution.Goals;
 import com.phresco.pom.util.AndroidPomProcessor;
 import com.phresco.pom.util.PomProcessor;
 
 public class Build extends DynamicParameterAction implements Constants {
 
 	private static final long serialVersionUID = -9172394838984622961L;
 	private static final Logger S_LOGGER = Logger.getLogger(Build.class);
 	private static Boolean debugEnabled = S_LOGGER.isDebugEnabled();
 	private String database = null;
 	private String server = null;
 	private String email = null;
 	private String webservice = null;
 	private String importSql = null;
 	private String showError = null;
 	private String hideLog = null;
 	private String skipTest = null;
 	private String showDebug = null;
 	private InputStream fileInputStream;
 	private String fileName = "";
 	private String connectionAlive = "false";
 	private String projectCode = null;
 	private String sdk = null;
 	private String target = "";
 	private String mode = null;
 	private String androidVersion = null;
 	private String environments = "";
 	private String serialNumber = null;
 	private String proguard = null;
 	private String projectModule = null;
 	private String mainClassName = null;
 	private String jarName = null;
 	
 	// Iphone deploy option
 	private String deployTo = "";
 	private String userBuildName = null;
 	private String userBuildNumber = null;
 
 	// Create profile
 	private String keystore = null;
 	private String storepass = null;
 	private String keypass = null;
 	private String alias = null;
 	private boolean profileCreationStatus = false;
 	private String profileCreationMessage = null;
 	private String profileAvailable = null;
 	private String signing = null;
 	private List<String> databases = null;
 	private List<String> sqlFiles = null;
 	private static Map<String, List<String>> projectModuleMap = Collections
 			.synchronizedMap(new HashMap<String, List<String>>(8));
 	
 	/* minify */
 	private String minifyAll = "";
 	private String selectedFilesToMinify = "";
 	private String compressName = "";
 	private List<String> minifyFileNames = null;
 	private String minifiedFiles = "";
 	private String fileType = "";
 	private String fileOrFolder = "";
 	private String browseLocation = "";
 	private String projectId = "";
 	
 	//iphone family
 	private String family = ""; 
 	
 	//Windows family
 	private String configuration = ""; 
 	private String platform = "";
 	
 	private static Map<String, String> sqlFolderPathMap = new HashMap<String, String>();
 
 	// DbWithSqlFiles
 	private String fetchSql = null;
 	static {
 		initDbPathMap();
 	}
 	
 	//Dynamic parameter related
 	private String from = "";
 	private List<Value> dependentValues = null; //Value for dependancy parameters
 	private String dependantKey = ""; 
 	private String dependantValue = "";
 	private String goal = "";
 	
 	public String view() throws PhrescoException {
 		if (debugEnabled)
 			S_LOGGER.debug("Entering Method  Build.view()");
 		try {
 		    removeSessionAttribute(getAppId() + SESSION_APPINFO);//To remove the appInfo from the session
 		   	ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 		   	ApplicationInfo applicationInfo = applicationManager.getApplicationInfo(getCustomerId(), getProjectId(), getAppId());
 		   	setReqAttribute(REQ_APPINFO, applicationInfo);
 			String readLogFile = "";
 			boolean tempConnectionAlive = false;
 			int serverPort = 0;
 			String serverProtocol = (String) getSessionAttribute(getAppId() + SESSION_SERVER_PROTOCOL_VALUE);
 			String serverHost = (String) getSessionAttribute(getAppId() + SESSION_SERVER_HOST_VALUE);
 			String serverPortStr = (String) getSessionAttribute(getAppId() + SESSION_SERVER_PORT_VALUE);
 			if (StringUtils.isEmpty(serverProtocol) && StringUtils.isEmpty(serverHost) && StringUtils.isEmpty(serverPortStr)) {
 				String runAgainstInfoEnv = readRunAgainstInfo();
 				if (StringUtils.isNotEmpty(runAgainstInfoEnv)) {
 					com.photon.phresco.api.ConfigManager configManager = PhrescoFrameworkFactory
 							.getConfigManager(new File(Utility.getProjectHome() + getApplicationInfo().getAppDirName() + File.separator
 									+ Constants.DOT_PHRESCO_FOLDER + File.separator + Constants.CONFIGURATION_INFO_FILE));
 					List<com.photon.phresco.configuration.Configuration> configurations = configManager
 							.getConfigurations(runAgainstInfoEnv, Constants.SETTINGS_TEMPLATE_SERVER);
 					if (CollectionUtils.isNotEmpty(configurations)) {
 						for (com.photon.phresco.configuration.Configuration serverConfiguration : configurations) {
 							serverProtocol = serverConfiguration.getProperties().getProperty(Constants.SERVER_PROTOCOL);
 							serverHost = serverConfiguration.getProperties().getProperty(Constants.SERVER_HOST);
 							serverPortStr = serverConfiguration.getProperties().getProperty(Constants.SERVER_PORT);
 							readLogFile = "";
 						}
 					}
 				}
 		 }
 				if (StringUtils.isNotEmpty(serverPortStr)) {
 					serverPort = Integer.parseInt(serverPortStr);
 				}
 				
 				if (StringUtils.isNotEmpty(serverProtocol) && StringUtils.isNotEmpty(serverHost) && serverPort != 0) {
 					tempConnectionAlive = Utility.isConnectionAlive(serverProtocol, serverHost, serverPort);
 					setSessionAttribute(getAppId() + SESSION_SERVER_STATUS, tempConnectionAlive);
 				}
 				if (tempConnectionAlive) {
 					readLogFile = readRunAgsSrcLogFile();
 					File runAgsLogfile = new File(getLogFolderPath() + File.separator + RUN_AGS_LOG_FILE) ;
 					if (runAgsLogfile.exists()) {
 						setReqAttribute(REQ_LOG_FILE_EXISTS, true);
 					}
 				} else {
 					deleteLogFile();
 					readLogFile = "";
 					setReqAttribute(REQ_LOG_FILE_EXISTS, false);
 				}
 				setReqAttribute(REQ_SERVER_LOG, readLogFile);
 
 		} catch (ConfigurationException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.view()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			return showErrorPopup(new PhrescoException(e), getText("excep.hdr.proj.view"));
 		}
 		return APP_BUILD;
 	}
 	
 	public String checkForConfiguration() {
 		
 		return SUCCESS;
 	}
 
 	/**
 	 * To show generate build popup with loaded dynamic parameters 
 	 */
 	public String showGenerateBuildPopup() throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  Build.showGenerateBuildPopup()");
 		}
 		try {
 		    ApplicationInfo appInfo = getApplicationInfo();
             removeSessionAttribute(appInfo.getId() + PHASE_PACKAGE + SESSION_WATCHER_MAP);
             setProjModulesInReq();
             Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>(8);
 //            List<Parameter> parameters = getDynamicParameters(appInfo, PHASE_PACKAGE);
             
             MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_PACKAGE)));
             List<Parameter> parameters = getMojoParameters(mojo, PHASE_PACKAGE);
             
             setPossibleValuesInReq(mojo, appInfo, parameters, watcherMap, PHASE_PACKAGE);
             setSessionAttribute(appInfo.getId() + PHASE_PACKAGE + SESSION_WATCHER_MAP, watcherMap);
             setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
             setReqAttribute(REQ_GOAL, PHASE_PACKAGE);
             setReqAttribute(REQ_PHASE, PHASE_PACKAGE);
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_BUILD_POPUP));
 		}
 		
 		return APP_GENERATE_BUILD;
 	}
 	
 	private void setProjModulesInReq() throws PhrescoException {
         List<String> projectModules = getProjectModules(getApplicationInfo().getAppDirName());
         setReqAttribute(REQ_PROJECT_MODULES, projectModules);
     }
 	
 	/**
 	 * To show Run Against Source  popup with loaded dynamic parameters 
 	 */
 	public String showrunAgainstSourcePopup() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  Build.showrunAgainstSourcePopup()");
 		}
 		try {
 			ApplicationInfo appInfo = getApplicationInfo();
             removeSessionAttribute(appInfo.getId() + PHASE_RUNGAINST_SRC_START + SESSION_WATCHER_MAP);
             setProjModulesInReq();
             Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>(8);
 
             MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_RUNGAINST_SRC_START)));
             List<Parameter> parameters = getMojoParameters(mojo, PHASE_RUNGAINST_SRC_START);
 
             setPossibleValuesInReq(mojo, appInfo, parameters, watcherMap, PHASE_RUNGAINST_SRC_START);
             setSessionAttribute(appInfo.getId() + PHASE_RUNGAINST_SRC_START + SESSION_WATCHER_MAP, watcherMap);
             setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
             setReqAttribute(REQ_GOAL, PHASE_RUNGAINST_SRC_START);
             setReqAttribute(REQ_PHASE, PHASE_RUNGAINST_SRC_START);
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText("excep.hdr.runagainstsource.popup"));
 		}
 
 		return APP_GENERATE_BUILD;
 	}
 
 	/*
 	 * To show deploy popup with loaded dynamic parameters
 	 */
 	public String showDeployPopup() throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  Build.showDeployPopup()");
 		}
 		try {
 		    ApplicationInfo appInfo = getApplicationInfo();
             removeSessionAttribute(appInfo.getId() + PHASE_DEPLOY + SESSION_WATCHER_MAP);
             setProjModulesInReq();
             Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>(8);
 //            List<Parameter> parameters = getDynamicParameters(appInfo, PHASE_DEPLOY);
 
             MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_DEPLOY)));
             List<Parameter> parameters = getMojoParameters(mojo, PHASE_DEPLOY);
 
             setPossibleValuesInReq(mojo, appInfo, parameters, watcherMap, PHASE_DEPLOY);
             
             setSessionAttribute(appInfo.getId() + PHASE_DEPLOY + SESSION_WATCHER_MAP, watcherMap);
             setReqAttribute(REQ_DEPLOY_BUILD_NUMBER, getReqParameter(BUILD_NUMBER));
             setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
             setReqAttribute(REQ_GOAL, PHASE_DEPLOY);
             setReqAttribute(REQ_PHASE, PHASE_DEPLOY);
             setProjModulesInReq();
             setReqAttribute(REQ_FROM, getFrom());
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_DEPLOY_POPUP));
 		} 
 
 		return APP_GENERATE_BUILD;
 	}
 	
 	public String builds() throws PhrescoException {
 		if (debugEnabled)
 			S_LOGGER.debug("Entering Method  Build.builds()");
 
 		try {
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			
 			List<BuildInfo> builds = applicationManager.getBuildInfos(new File(getBuildInfosFilePath(applicationInfo)));
 			
 			Collections.sort(builds, new BuildComparator());
 
 			setReqAttribute(REQ_CUSTOMER_ID, getCustomerId());
 			setReqAttribute(REQ_PROJECT_ID, getProjectId());
 			setReqAttribute(REQ_BUILD, builds);
 			setReqAttribute(REQ_APPINFO, applicationInfo);
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.builds()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			return showErrorPopup(e, getText(EXCEPTION_BUILDS_LIST));
 		}
 		
 		return APP_BUILDS;
 	}
 	
 	public String build() throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  Build.build()");
 		}
 		try {
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ProjectInfo projectInfo = getProjectInfo();
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_PACKAGE)));
 			persistValuesToXml(mojo, PHASE_PACKAGE);
 			
 			//To get maven build arguments
 			List<Parameter> parameters = getMojoParameters(mojo, PHASE_PACKAGE);
 			List<String> buildArgCmds = getMavenArgCommands(parameters);
			buildArgCmds.add("-N");
 			String workingDirectory = getAppDirectoryPath(applicationInfo);
 			getApplicationProcessor().preBuild(getApplicationInfo());
 			BufferedReader reader = applicationManager.performAction(projectInfo, ActionType.BUILD, buildArgCmds, workingDirectory);
 			setSessionAttribute(getAppId() + REQ_BUILD, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, REQ_BUILD);
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.build()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			return showErrorPopup(e, getText(EXCEPTION_BUILD_GENERATE));
 		}
 
 		return APP_ENVIRONMENT_READER;
 	}
 	
 	public String processBuild() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  processBuild()");
 		}
 		try {
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ProjectInfo projectInfo = getProjectInfo();
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_PROCESS_BUILD)));
 			persistValuesToXml(mojo, PHASE_PROCESS_BUILD);
 			
 			String workingDirectory = getAppDirectoryPath(applicationInfo);
 			BufferedReader reader = applicationManager.performAction(projectInfo, ActionType.PROCESS_BUILD, null, workingDirectory);
 			setSessionAttribute(getAppId() + REQ_PROCESS_BUILD, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, REQ_PROCESS_BUILD);
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of buildProcess()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			return showErrorPopup(e, getText(EXCEPTION_BUILD_GENERATE));
 		}
 
 		return APP_ENVIRONMENT_READER;
 	}
 	
 	public String deploy() throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  Build.deploy()");
 		}
 		try {
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ProjectInfo projectInfo = getProjectInfo();
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_DEPLOY)));
 			
 			persistValuesToXml(mojo, PHASE_DEPLOY);
 			//To get maven build arguments
 			List<Parameter> parameters = getMojoParameters(mojo, PHASE_DEPLOY);
 			List<String> buildArgCmds = getMavenArgCommands(parameters);
 			String workingDirectory = getAppDirectoryPath(applicationInfo);
 			BufferedReader reader = applicationManager.performAction(projectInfo, ActionType.DEPLOY, buildArgCmds, workingDirectory);
 			setSessionAttribute(getAppId() + REQ_FROM_TAB_DEPLOY, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, REQ_FROM_TAB_DEPLOY);
 			
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.deploy()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			return showErrorPopup(e, getText(EXCEPTION_DEPLOY_GENERATE));
 		}
 
 		return APP_ENVIRONMENT_READER;
 	}
 
 	private File isFileExists(Project project) throws IOException {
 		StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 		builder.append(project.getApplicationInfo().getCode());
 		builder.append(File.separator);
 		builder.append("do_not_checkin");
 		builder.append(File.separator);
 		builder.append("temp");
 		File tempFolder = new File(builder.toString());
 		if (!tempFolder.exists()) {
 			tempFolder.mkdir();
 		}
 		builder.append(File.separator);
 		builder.append("importsql.property");
 		File configFile = new File(builder.toString());
 		if (!configFile.exists()) {
 			configFile.createNewFile();
 		}
 		return configFile;
 	}
 
 	private void updateImportSqlConfig(Project project) throws PhrescoException {
 
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method Build.checkImportsqlConfig(Project project)");
 		}
 		if (debugEnabled) {
 			S_LOGGER.debug("adaptImportsqlConfig ProjectInfo = " + project.getApplicationInfo());
 		}
 		InputStream is = null;
 		FileWriter fw = null;
 
 		try {
 			File configFile = isFileExists(project);
 
 			is = new FileInputStream(configFile);
 			PluginProperties configProps = new PluginProperties();
 			configProps.load(is);
 			fw = new FileWriter(configFile);
 			fw.write("build.import.sql.first.time=" + importSql);
 			fw.flush();
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			try {
 				if (fw != null) {
 					fw.close();
 				}
 				if (is != null) {
 					is.close();
 				}
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	private void importSqlFlag(Project project) throws PhrescoException {
 		String technology = project.getApplicationInfo().getTechInfo().getVersion();
 		InputStream is = null;
 		String importSqlElement;
 		try {
 
 			StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 			builder.append(project.getApplicationInfo().getCode());
 			builder.append(File.separator);
 			builder.append(DO_NOT_CHECKIN_DIR);
 			builder.append(File.separator);
 			builder.append(TEMP_FOLDER);
 			builder.append(File.separator);
 			builder.append(IMPORT_PROPERTY);
 			File configFile = new File(builder.toString());
 			if (!configFile.exists() && !TechnologyTypes.IPHONES.contains(technology)
 					|| !TechnologyTypes.ANDROIDS.contains(technology)) {
 				getHttpRequest().setAttribute(REQ_IMPORT_SQL, Boolean.TRUE.toString());
 			}
 
 			if (configFile.exists()) {
 				is = new FileInputStream(configFile);
 				PluginProperties configProps = new PluginProperties();
 				configProps.load(is);
 				@SuppressWarnings("rawtypes")
 				Enumeration enumProps = configProps.keys();
 
 				while (enumProps.hasMoreElements()) {
 					importSqlElement = (String) enumProps.nextElement();
 					String importSqlProps = (String) configProps.get(importSqlElement);
 					getHttpRequest().setAttribute(REQ_IMPORT_SQL, importSqlProps);
 				}
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public String delete() throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  Build.delete()");
 		}
 
 		String[] buildNumbers = getReqParameterValues(REQ_BUILD_NUMBER);
 		
 		int[] buildInts = new int[buildNumbers.length];
 		for (int i = 0; i < buildNumbers.length; i++) {
 			if (debugEnabled) {
 				S_LOGGER.debug("To be deleted build numbers " + buildNumbers[i]);
 			}
 			buildInts[i] = Integer.parseInt(buildNumbers[i]);
 		}
 
 		try {
 			ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 			ProjectInfo project = projectManager.getProject(getProjectId(), getCustomerId(), getAppId());
 			
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			applicationManager.deleteBuildInfos(project, buildInts);
 			setReqAttribute(REQ_PROJECT, project);
 			addActionMessage(getText(SUCCESS_BUILD_DELETE));
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.delete()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			new LogErrorReport(e, "Deleting build");
 		}
 
 		setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 		return view();
 	}
 
 	public String download() throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method Build.download()");
 		}
 		try {
 			String buildNumber = getReqParameter(REQ_DEPLOY_BUILD_NUMBER);
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			if (StringUtils.isEmpty(buildNumber)) {
 				throw new PhrescoException("Build Number is empty ");
 			}
 			
 			BuildInfo buildInfo = applicationManager.getBuildInfo(Integer.parseInt(buildNumber), getBuildInfosFilePath(applicationInfo));
 			String deliverables = buildInfo.getDeliverables();
 			StringBuilder builder = new StringBuilder();
 			fileName = buildInfo.getBuildName();
 			if (StringUtils.isEmpty(deliverables)) {
 				builder.append(getApplicationHome());
 				builder.append(File.separator);
 				String moduleName = buildInfo.getModuleName();
 				if (StringUtils.isNotEmpty(moduleName)) {
 					builder.append(moduleName);
 					builder.append(File.separator);
 				}
 				builder.append(BUILD_DIR);
 				builder.append(File.separator);
 				builder.append(buildInfo.getBuildName());
 			} else {
 				builder.append(buildInfo.getDeliverables());
 				fileName = fileName.substring(fileName.lastIndexOf(FORWARD_SLASH) + 1);
 				boolean status = fileName.endsWith(APKLIB) || fileName.endsWith(APK);
 				if (status) {
 					fileName = fileName.substring(0, fileName.lastIndexOf(".")) + ARCHIVE_FORMAT ;
 				} else {
 					fileName = fileName.split(SPLIT_DOT)[0] + ARCHIVE_FORMAT;
 				}
 			}
 			if (debugEnabled) {
 				S_LOGGER.debug("Download build number " + buildNumber + " Download location " + builder.toString());
 			}
 			fileInputStream = new FileInputStream(new File(builder.toString()));
 			return SUCCESS;
 		} catch (Exception e) {
     		if (debugEnabled) {
     			S_LOGGER.error("Entered into catch block of Code.code()"+ FrameworkUtil.getStackTraceAsString(e));
     		}
     		return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_BUILD_DOWNLOAD_NOT_AVAILABLE));
 
 		} 
 	}
 
 	public String downloadIpa() throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method Build.downloadIPA()");
 		}
 		try {
 			String buildNumber = getReqParameter(REQ_DEPLOY_BUILD_NUMBER);
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ProjectInfo projectInfo = getProjectInfo();
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			if (StringUtils.isEmpty(buildNumber)) {
 				throw new PhrescoException("Build Number is empty ");
 			}
 			String ipaFileName = applicationInfo.getName();
 			String buildName = applicationManager.getBuildInfo(Integer.parseInt(buildNumber), getBuildInfosFilePath(applicationInfo)).getBuildName();
 			String workingDirectory = getAppDirectoryPath(applicationInfo);
 			String buildNameSubstring = buildName.substring(0, buildName.lastIndexOf(FILE_SEPARATOR));
 			String appBuildName = buildNameSubstring.substring(buildNameSubstring.lastIndexOf(FILE_SEPARATOR) + 1);
 			
 			List<String> buildArgCmds = new ArrayList<String>();
 			buildArgCmds.add("-Dapplication.name=" + ipaFileName);
 			buildArgCmds.add("-Dapp.path=" + buildName);
 			buildArgCmds.add("-Dbuild.name=" + appBuildName);
 			BufferedReader reader = applicationManager.performAction(projectInfo, ActionType.IPA_DOWNLOAD, buildArgCmds, workingDirectory);
 			while (reader.readLine() != null) {
 				System.out.println(reader.readLine());
 			}
 			String ipaPath = applicationManager.getBuildInfo(Integer.parseInt(buildNumber), getBuildInfosFilePath(applicationInfo)).getBuildName();
 			ipaPath = ipaPath.substring(0, ipaPath.lastIndexOf(FILE_SEPARATOR)) + FILE_SEPARATOR + ipaFileName + IPA_FORMAT;
 			fileInputStream = new FileInputStream(new File(ipaPath));
 			fileName = ipaFileName + IPA_FORMAT;
 			return SUCCESS;
 		} catch (FileNotFoundException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.download()" + e);
 			}
 			new LogErrorReport(e, "Download builds");
 
 		} catch (Exception e1) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.downloadIpa()"
 						+ FrameworkUtil.getStackTraceAsString(e1));
 			}
 			new LogErrorReport(e1, "Download buildsIPA");
 		}
 		return view();
 	}
 	
 	/**
 	 * Run against source Execution For java , Nodejs and HTML technologies.
 	 *
 	 */
 	public String runAgainstSource() throws IOException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method Build.javaRunAgainstSource()");
 		}
 		
 		try {
 			BufferedReader compileReader = compileSource();
 			String line = compileReader.readLine();
 			while (StringUtils.isNotEmpty(line) && !line.startsWith("[INFO] BUILD FAILURE")) {
 				line = compileReader.readLine();
 			}
 			BufferedReader reader = null;
 			if (StringUtils.isNotEmpty(line) && line.startsWith("[INFO] BUILD FAILURE")) {
 				reader = new BufferedReader(new FileReader(getLogFilePath()));
 			} else {
 				reader = startServer();
 			}
 			setSessionAttribute(getAppId() + REQ_START, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, REQ_START);
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.runAgainstSource()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			return showErrorPopup(e, getText(EXCEPTION_RUNAGNSRC_SERVER_START));
 		}
 
 		return APP_ENVIRONMENT_READER;
 
 	}
 	
 	/**
 	 * Stops the server  For java , Nodejs and HTML technologies.
 	 */
 	public String stopServer() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method Build.handleStopServer()");
 		}
 		
 		try {
 			setSessionAttribute(getAppId() + REQ_START, null);
 			BufferedReader reader = handleStopServer(false);
 			removeSessionAttribute(getAppId() + SESSION_SERVER_STATUS);
 			setSessionAttribute(getAppId() + REQ_STOP, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, REQ_STOP);
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.stopServer()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			return showErrorPopup(e, getText(EXCEPTION_RUNAGNSRC_SERVER_STOP));
 		}
 
 		return APP_ENVIRONMENT_READER;
 	}
 
 	/**
 	 * Restarts the server For java , Nodejs and HTML technologies.
 	 *
 	 */
 	public String restartServer() throws IOException  {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method Build.restartServer()");
 		}
 		
 		try {
 			handleStopServer(true);
 			BufferedReader compileReader = compileSource();
 			String line = compileReader.readLine();
 			while (line != null && !line.startsWith("[INFO] BUILD FAILURE")) {
 				line = compileReader.readLine();
 			}
 			BufferedReader reader = null;
 			if (line != null && line.startsWith("[INFO] BUILD FAILURE")) {
 				reader = new BufferedReader(new FileReader(getLogFilePath()));
 			} else {
 				reader = startServer();
 			}
 			setSessionAttribute(getAppId() + REQ_START, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, REQ_START);
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_RUNAGNSRC_SERVER_RESTART));
 		}
 
 		return APP_ENVIRONMENT_READER;
 	}
 
 	private BufferedReader startServer() throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method Build.startServer()");
 		}
 		
 		BufferedReader reader = null;
 		try {
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_RUNGAINST_SRC_START)));
 			persistValuesToXml(mojo, PHASE_RUNGAINST_SRC_START);
 			com.photon.phresco.api.ConfigManager configManager = PhrescoFrameworkFactory
 					.getConfigManager(new File(Utility.getProjectHome() + getApplicationInfo().getAppDirName()
 							+ File.separator + Constants.DOT_PHRESCO_FOLDER + File.separator
 							+ Constants.CONFIGURATION_INFO_FILE));
 			com.photon.phresco.plugins.model.Mojos.Mojo.Configuration configuration = mojo.getConfiguration(PHASE_RUNGAINST_SRC_START);
 			Map<String, String> configs = MojoUtil.getAllValues(configuration);
 			String environmentName = configs.get(ENVIRONMENT_NAME);
 			List<com.photon.phresco.configuration.Configuration> configurations = configManager.getConfigurations(
 					environmentName, Constants.SETTINGS_TEMPLATE_SERVER);
 			String serverHost = "";
 			String serverProtocol = "";
 			int serverPort = 0;
 			if (CollectionUtils.isNotEmpty(configurations)) {
 				for (com.photon.phresco.configuration.Configuration serverConfiguration : configurations) {
 					serverHost = serverConfiguration.getProperties().getProperty(Constants.SERVER_HOST);
 					serverProtocol = serverConfiguration.getProperties().getProperty(Constants.SERVER_PROTOCOL);
 					serverPort = Integer.parseInt(serverConfiguration.getProperties()
 							.getProperty(Constants.SERVER_PORT));
 				}
 			}
 			// TODO: delete the server.log and create empty server.log file
 			deleteLogFile();
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ProjectInfo projectInfo = getProjectInfo();
 			String workingDirectory = getAppDirectoryPath(applicationInfo);
 			reader = applicationManager.performAction(projectInfo, ActionType.RUNAGAINSTSOURCE, null, workingDirectory);
 			boolean connectionAlive = Utility.isConnectionAlive(serverProtocol, serverHost, serverPort);
 			setSessionAttribute(getAppId() + SESSION_SERVER_STATUS, connectionAlive);
 			setSessionAttribute(getAppId() + SESSION_SERVER_PROTOCOL_VALUE, serverProtocol);
 			setSessionAttribute(getAppId() + SESSION_SERVER_HOST_VALUE, serverHost);
 			setSessionAttribute(getAppId() + SESSION_SERVER_PORT_VALUE, new Integer(serverPort).toString());
 		} catch (ConfigurationException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.startServer()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			throw new PhrescoException(e);
 		}
 		
 		return reader;
 	}
 
 	private BufferedReader compileSource() throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method Build.compileSource()");
 		}
 		
 		BufferedReader reader = null;
 		try {
 			Commandline cl = new Commandline("mvn clean compile");
 			String projectDir = Utility.getProjectHome() + getApplicationInfo().getAppDirName();
 			cl.setWorkingDirectory(projectDir);
 			Process process = cl.execute();
 			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
 			writeLog(reader);
 			reader = new BufferedReader(new FileReader(getLogFilePath()));
 		} catch (FileNotFoundException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.compileSource()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 			}
 			throw new PhrescoException(e);
 		} catch (CommandLineException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.compileSource()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 			}
 			throw new PhrescoException(e);
 		}
 		
 		return reader;
 	}
 
 	private BufferedReader handleStopServer(boolean readData) throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method Build.handleStopServer()");
 		}
 		
 		BufferedReader reader = null;
 		try {
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			String workingDirectory = getAppDirectoryPath(applicationInfo);
 			reader = applicationManager.performAction(getProjectInfo(), ActionType.STOPSERVER, null, workingDirectory);
 			if (readData) {
 				while (StringUtils.isNotEmpty(reader.readLine())) {}
 			}
 			deleteLogFile();
 		} catch (Exception e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.handleStopServer()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 			}
 		}
 		
 		return reader;
 	}
 	
 	private String readRunAgsSrcLogFile() throws PhrescoException {
 		BufferedReader reader = null;
 		try {
 			File runAgsLogfile = new File(getLogFolderPath() + File.separator + RUN_AGS_LOG_FILE) ;
 			if (runAgsLogfile.exists()) {
 				reader = new BufferedReader(new FileReader(runAgsLogfile));
 				String text = "";
 				StringBuffer contents = new StringBuffer();
 				while ((text = reader.readLine()) != null) {
 					contents.append(text).append(System.getProperty(LINE_SEPERATOR));
 				}
 				return contents.toString();
 			}
 		} catch (FileNotFoundException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.readRunAgsSrcLogFile()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 			}
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.readRunAgsSrcLogFile()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 			}
 			throw new PhrescoException(e);
 		} finally { 
 			Utility.closeReader(reader);
 		}
 		
 		return "";
 	}
 
 	
 	private void writeLog(BufferedReader in) throws PhrescoException {
 		FileWriter fstream = null;
 		BufferedWriter out = null;
 		try {
 			String logFolderPath = getLogFolderPath();
 			File logfolder = new File(logFolderPath);
 			if (!logfolder.exists()) {
 				logfolder.mkdirs();
 			}
 			fstream = new FileWriter(getLogFilePath());
 			out = new BufferedWriter(fstream);
 			String line = "";
 			while ((line = in.readLine()) != null) {
 				out.write(line + "\n");
 				out.flush();
 			}
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeWriter(out);
 			try {
 				if (fstream != null) {
 					fstream.close();
 				}
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 		}
 	}
 
 	private String getLogFilePath() throws PhrescoException {
 		StringBuilder builder = new StringBuilder(getLogFolderPath());
 		builder.append(File.separator);
 		builder.append(LOG_FILE);
 		return builder.toString();
 	}
 
 	private String getLogFolderPath() throws PhrescoException {
 		StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 		builder.append(getApplicationInfo().getAppDirName());
 		builder.append(File.separator);
 		builder.append(DO_NOT_CHECKIN_DIR);
 		builder.append(File.separator);
 		builder.append(LOG_DIR);
 		return builder.toString();
 	}
 
 	public void deleteLogFile() throws PhrescoException {
 		try {
 			File logFile = new File(getLogFilePath());
 			File infoFile = new File(getLogFolderPath() + File.separator + RUN_AGS_LOG_FILE);
 			if (logFile.isFile() && logFile.exists()) {
 				logFile.delete();
 			} 
 			if(infoFile.isFile() && infoFile.exists()) {
 				infoFile.delete();
 			}
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	public void waitForTime(int waitSec) {
 		long startTime = 0;
 		startTime = new Date().getTime();
 		while (new Date().getTime() < startTime + waitSec * 1000) {
 			// Dont do anything for some seconds. It waits till the log is
 			// written to file
 		}
 	}
 
 	private String readRunAgainstInfo() throws PhrescoException {
 		String env = null;
 		BufferedReader reader = null;
 		try {
 			ConfigurationInfo info = new ConfigurationInfo();
 			StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 			builder.append(getApplicationInfo().getAppDirName());
 			builder.append(File.separator);
 			builder.append(FOLDER_DOT_PHRESCO);
 			builder.append(File.separator);
 			builder.append(RUN_AGS_ENV_FILE);
 			File infoFile = new File(builder.toString());
 			if(infoFile.exists()) {
 				reader = new BufferedReader(new FileReader(builder.toString()));
 				Gson gson = new Gson();
 				info = gson.fromJson(reader, ConfigurationInfo.class);
 				env = info.getEnvironment();
 			}
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeReader(reader);
 		}
 
 		return env;
 	}
 
 	/* minification starts */
 	public String minifyPopup() throws PhrescoException, PhrescoPomException {
 		ApplicationInfo applicationInfo = getApplicationInfo();
 		String pomPath = getAppDirectoryPath(applicationInfo) + File.separator + POM_FILE;
 		PomProcessor pomProcessor = new PomProcessor(new File(pomPath));
 		com.phresco.pom.model.Plugin.Configuration pluginConfig = pomProcessor.getPlugin(MINIFY_PLUGIN_GROUPID,MINIFY_PLUGIN_ARTFACTID).getConfiguration();
 		//To check for availability of minification plugin in pom.xml
 		if (pluginConfig != null) {
 			List<Element> elements = pluginConfig.getAny();
 			if (elements != null) {
 				for (Element element : elements) {
 					includesFiles(element, applicationInfo);//To read already minified details from pom
 				}
 			}
 		}
 		
 		return SUCCESS;
 	}
 	
 	private void includesFiles(Element element, ApplicationInfo applicationInfo) {
 		try {
 			Map<String, Map<String, String>> jsMap = new HashMap<String, Map<String, String>>();
 			Map<String, Map<String, String>> cssMap = new HashMap<String, Map<String, String>>();
 			String opFileLoc = "";
 			if (POM_AGGREGATIONS.equals(element.getNodeName())) {
 				NodeList aggregationList = element.getElementsByTagName(POM_AGGREGATION);
 				for (int i = 0; i < aggregationList.getLength(); i++) {
 					Element childNode = (Element) aggregationList.item(i);
 					NodeList includeList = childNode.getElementsByTagName(POM_INCLUDES).item(0).getChildNodes();
 					StringBuilder csvFileNames = new StringBuilder(); 
 					String sep = "";
 					for (int j = 0; j < includeList.getLength()-1; j++) {//To convert select files to Comma seperated value
 						Element include = (Element) includeList.item(j);
 						String file = include.getTextContent().substring(include.getTextContent().lastIndexOf(FILE_SEPARATOR)+1);
 						csvFileNames.append(sep);
 						csvFileNames.append(file);
 						sep = COMMA;
 					}
 					Element outputElement = (Element) childNode.getElementsByTagName(POM_OUTPUT).item(0);
 					//To get compressed name with extension
 					String opFileName = outputElement.getTextContent().substring(outputElement.getTextContent().lastIndexOf(FILE_SEPARATOR)+1);
 					String compressName = opFileName.substring(0, opFileName.indexOf("."));//To get only the compressed name without extension
 					String compressedExtension = opFileName.substring(opFileName.lastIndexOf(DOT)+1);//To get extension of compressed file
 					opFileLoc = outputElement.getTextContent().substring(0, outputElement.getTextContent().lastIndexOf(FILE_SEPARATOR)+1);
 					opFileLoc = opFileLoc.replace(MINIFY_OUTPUT_DIRECTORY, getAppDirectoryPath(applicationInfo).replace(File.separator, FORWARD_SLASH));
 					
 					if (JS.equals(compressedExtension)) {//if extension is js , add minified details to jsMap
 						Map<String, String> jsValuesMap = new HashMap<String, String>();
 						jsValuesMap.put(csvFileNames.toString().replace(HYPHEN_MIN, ""), opFileLoc);//add minifed files list and its location details to jsValuesMap
 						jsMap.put(compressName, jsValuesMap);	
 					} else {//if extension is CSS , add minified details to cssMap
 						Map<String, String> cssValuesMap = new HashMap<String, String>();
 						cssValuesMap.put(csvFileNames.toString().replace(HYPHEN_MIN, ""), opFileLoc);//add minifed files list and its location to cssValuesMap
 						cssMap.put(compressName, cssValuesMap);
 					}
 				}
 			}
 			getHttpRequest().setAttribute(REQ_JS_MINIFY_MAP, jsMap);
 			getHttpRequest().setAttribute(REQ_CSS_MINIFY_MAP, cssMap);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String minifyBrowseFileTree() throws PhrescoException {
 		setReqAttribute(FILE_TYPES, getFileType());
 		setReqAttribute(FILE_BROWSE, getFileOrFolder());
 		setReqAttribute(REQ_FROM, getFrom());
 		setReqAttribute(REQ_COMPRESS_NAME, getCompressName());
 		setReqAttribute(REQ_MINIFIED_FILES, getMinifiedFiles());
 		ApplicationInfo applicationInfo = getApplicationInfo();
 		setReqAttribute(REQ_PROJECT_LOCATION, getAppDirectoryPath(applicationInfo).replace(File.separator, FORWARD_SLASH));
 
 		return SUCCESS;
 	}
 	
 	public String selectFilesToMinify() {
 		String[] selectedFiles = getHttpRequest().getParameterValues(FILES_TO_MINIFY);
 		StringBuilder files = new StringBuilder();
 		String sep = "";
 		for (String selectedFile : selectedFiles) {
 			files.append(sep);
 			files.append(selectedFile);
 		    sep = ",";
 		}
 		setSelectedFilesToMinify(files.toString());
 		
 		return SUCCESS;
 	}
 	
 	public String minification() {
 		try {
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			ProjectInfo projectInfo = getProjectInfo();
 			String pomPath = Utility.getProjectHome() + File.separator + applicationInfo.getAppDirName() + File.separator + POM_FILE;
 			PomProcessor pomProcessor = new PomProcessor(new File(pomPath));
 			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 			List<Element> configList = new ArrayList<Element>();
 			List<String> files = getMinifyFileNames();
 			createExcludesTagInPom(doc, configList);
 			if (Boolean.parseBoolean(getMinifyAll()) && CollectionUtils.isEmpty(files)) { // Only Minify all is selected
 				configList.add(createElement(doc, POM_OUTPUTDIR, POM_SOURCE_DIRECTORY));
 			} else if (CollectionUtils.isNotEmpty(files)) {
 				String dynamicIncludeDir = "";
 				if (Boolean.parseBoolean(getMinifyAll())) {//if Minify all is selected
 					dynamicIncludeDir = POM_SOURCE_DIRECTORY;
 					configList.add(createElement(doc, POM_OUTPUTDIR, POM_SOURCE_DIRECTORY));
 				} else {//if Minify all not is selected
 					dynamicIncludeDir = POM_OUTPUT_DIRECTORY;
 					configList.add(createElement(doc, POM_OUTPUTDIR, POM_OUTPUT_DIRECTORY));
 				}
 				createAggregationTagInPom(applicationInfo, doc, configList, files, dynamicIncludeDir);
 			}
 			pomProcessor.addConfiguration(MINIFY_PLUGIN_GROUPID, MINIFY_PLUGIN_ARTFACTID, configList);
 			pomProcessor.save();
 			
 			String workingDirectory = getAppDirectoryPath(applicationInfo);
 			
 			BufferedReader reader = applicationManager.performAction(projectInfo, ActionType.MINIFY, null, workingDirectory);
 			setSessionAttribute(getAppId() + REQ_MINIFY, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, REQ_MINIFY);
 		} catch (Exception e) {
 				S_LOGGER.error("Entered into catch block of Build.minification()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 				new LogErrorReport(e, "Building ");
 			}
 		
 		return SUCCESS;
 	}
 
 	/**
 	 * To create Aggregations tag in Pom.xml with minification details
 	 * @param applicationInfo
 	 * @param doc
 	 * @param configList
 	 * @param files
 	 * @param dynamicIncludeDir
 	 */
 	private void createAggregationTagInPom(ApplicationInfo applicationInfo, Document doc, List<Element> configList, List<String> files,
 			String dynamicIncludeDir) {
 		Element aggregationsElement = doc.createElement(POM_AGGREGATIONS);
 		for (String file : files) {
 			String newFileName = ""; 
 			String extension = "";
 			String csvJsFile = getHttpRequest().getParameter(file);
 			List<String> chosenFiles = Arrays.asList(csvJsFile.split(CSV_PATTERN));
 			Element agrigationElement = appendChildElement(doc, aggregationsElement, POM_AGGREGATION, null);
 			appendChildElement(doc, agrigationElement, POM_INPUTDIR, dynamicIncludeDir);
 			Element includesElement = doc.createElement(POM_INCLUDES);
 			for (String chosenFile : chosenFiles) {
 				int lastDot = chosenFile.lastIndexOf(DOT);
 				newFileName = chosenFile.substring(0, lastDot);
 				extension = chosenFile.substring(lastDot + 1, chosenFile.length());
 				appendChildElement(doc, includesElement, POM_INCLUDE, "**/" + newFileName + HYPEN_MIN_DOT + extension);
 				agrigationElement.appendChild(includesElement);
 			}
 			String location = getHttpRequest().getParameter(file + MINIFY_FILE_LOCATION);
 			String[] splitted = location.split(applicationInfo.getAppDirName());
 			String minificationDir = splitted[1];
 			appendChildElement(doc, agrigationElement, POM_OUTPUT, MINIFY_OUTPUT_DIRECTORY + minificationDir + file + DOT_MIN_DOT + extension);
 		}
 		configList.add(aggregationsElement);
 	}
 
 	/**
 	 * To create excludes (files to be excluded while minification) tag in Pom.xml
 	 * @param doc
 	 * @param configList
 	 */
 	private void createExcludesTagInPom(Document doc, List<Element> configList) {
 		configList.add(createElement(doc, POM_SOURCEDIR, POM_SOURCE_DIRECTORY));
 		configList.add(createElement(doc, POM_FORCE, POM_VALUE_TRUE));
 		configList.add(createElement(doc, POM_JS_WARN, POM_VALUE_FALSE));
 		configList.add(createElement(doc, POM_LINE_BREAK, POM_LINE_MAX_COL_COUNT));
 		
 		Element excludesElement = doc.createElement(POM_EXCLUDES);
 		appendChildElement(doc, excludesElement, POM_EXCLUDE, POM_EXCLUDE_JS);
 		appendChildElement(doc, excludesElement, POM_EXCLUDE, POM_EXCLUDE_MIN_JS);
 		appendChildElement(doc, excludesElement, POM_EXCLUDE, POM_EXCLUDE_MINIFIED_JS);
 		appendChildElement(doc, excludesElement, POM_EXCLUDE, POM_EXCLUDE_MIN_CSS);
 		appendChildElement(doc, excludesElement, POM_EXCLUDE, POM_EXCLUDE_MINIFIED_CSS);
 		configList.add(excludesElement);
 	}
 	
 	private Element createElement(Document doc, String elementName, String textContent) {
 		Element element = doc.createElement(elementName);
 		if (StringUtils.isNotEmpty(textContent)) {
 			element.setTextContent(textContent);
 		}
 		return element;
 	}
 	
 	private Element appendChildElement(Document doc, Element parent, String elementName, String textContent) {
 		Element childElement = createElement(doc, elementName, textContent);
 		parent.appendChild(childElement);
 		return childElement;
 	}
 	/* minification ends */
 	
 	public String advancedBuildSettings() {
 		S_LOGGER.debug("Entering Method Build.advancedBuildSettings()");
 		AndroidProfile androidProfile = null;
 		try {
 			StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 			builder.append(projectCode);
 			builder.append(File.separatorChar);
 			builder.append(POM_XML);
 			File pomPath = new File(builder.toString());
 			AndroidPomProcessor processor = new AndroidPomProcessor(pomPath);
 			if (pomPath.exists() && processor.hasSigning()) {
 				String signingProfileid = processor.getSigningProfile();
 				androidProfile = processor.getProfileElement(signingProfileid);
 			}
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of  Build.advancedBuildSettings()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 		}
 		getHttpRequest().setAttribute("projectCode", projectCode);
 		getHttpRequest().setAttribute(REQ_ANDROID_PROFILE_DET, androidProfile);
 		getHttpRequest().setAttribute(REQ_FROM_TAB, REQ_FROM_TAB_DEPLOY);
 		return SUCCESS;
 	}
 
 	public String createAndroidProfile() throws IOException {
 		S_LOGGER.debug("Entering Method Build.createAndroidProfile()");
 		boolean hasSigning = false;
 		try {
 			StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 			builder.append(projectCode);
 			builder.append(File.separatorChar);
 			builder.append(POM_XML);
 			File pomPath = new File(builder.toString());
 
 			AndroidPomProcessor processor = new AndroidPomProcessor(pomPath);
 			hasSigning = processor.hasSigning();
 			String profileId = PROFILE_ID;
 			String defaultGoal = GOAL_INSTALL;
 			Plugin plugin = new Plugin();
 			plugin.setGroupId(ANDROID_PROFILE_PLUGIN_GROUP_ID);
 			plugin.setArtifactId(ANDROID_PROFILE_PLUGIN_ARTIFACT_ID);
 			plugin.setVersion(ANDROID_PROFILE_PLUGIN_VERSION);
 
 			PluginExecution execution = new PluginExecution();
 			execution.setId(ANDROID_EXECUTION_ID);
 			Goals goal = new Goals();
 			goal.getGoal().add(GOAL_SIGN);
 			execution.setGoals(goal);
 			execution.setPhase(PHASE_PACKAGE);
 			execution.setInherited(TRUE);
 
 			AndroidProfile androidProfile = new AndroidProfile();
 			androidProfile.setKeystore(keystore);
 			androidProfile.setStorepass(storepass);
 			androidProfile.setKeypass(keypass);
 			androidProfile.setAlias(alias);
 			androidProfile.setVerbose(true);
 			androidProfile.setVerify(true);
 
 			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 
 			List<Element> executionConfig = new ArrayList<Element>();
 			executionConfig.add(doc.createElement(ELEMENT_ARCHIVE_DIR));
 			Element removeExistSignature = doc.createElement(ELEMENT_REMOVE_EXIST_SIGN);
 			Element includeElement = doc.createElement(ELEMENT_INCLUDES);
 			Element doNotCheckInBuildInclude = doc.createElement(ELEMENT_INCLUDE);
 			doNotCheckInBuildInclude.setTextContent(ELEMENT_BUILD);
 			Element doNotCheckinTargetInclude = doc.createElement(ELEMENT_INCLUDE);
 			doNotCheckinTargetInclude.setTextContent(ELEMENT_TARGET);
 			includeElement.appendChild(doNotCheckInBuildInclude);
 			includeElement.appendChild(doNotCheckinTargetInclude);
 			executionConfig.add(includeElement);
 			removeExistSignature.setTextContent(TRUE);
 			executionConfig.add(removeExistSignature);
 
 			// verboss
 			Element verbos = doc.createElement(ELEMENT_VERBOS);
 			verbos.setTextContent(TRUE);
 			executionConfig.add(verbos);
 			// verify
 			Element verify = doc.createElement(ELEMENT_VERIFY);
 			verbos.setTextContent(TRUE);
 			executionConfig.add(verify);
 
 			Configuration configValues = new Configuration();
 			configValues.getAny().addAll(executionConfig);
 			execution.setConfiguration(configValues);
 			List<Element> additionalConfigs = new ArrayList<Element>();
 			processor.setProfile(profileId, defaultGoal, plugin, androidProfile, execution, null, additionalConfigs);
 			processor.save();
 			profileCreationStatus = true;
 			if (hasSigning) {
 				profileCreationMessage = getText(PROFILE_UPDATE_SUCCESS);
 			} else {
 				profileCreationMessage = getText(PROFILE_CREATE_SUCCESS);
 			}
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of  Build.createAndroidProfile()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			profileCreationStatus = false;
 			if (hasSigning) {
 				profileCreationMessage = getText(PROFILE_UPDATE_ERROR);
 			} else {
 				profileCreationMessage = getText(PROFILE_CREATE_ERROR);
 			}
 		}
 		return SUCCESS;
 	}
 
 	public String getSqlDatabases() {
 		String dbtype = "";
 		try {
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 			databases = new ArrayList<String>();
 			List<SettingsInfo> databaseDetails = administrator.getSettingsInfos(Constants.SETTINGS_TEMPLATE_DB,
 					projectCode, environments);
 			if (CollectionUtils.isNotEmpty(databaseDetails)) {
 				for (SettingsInfo databasedetail : databaseDetails) {
 					dbtype = databasedetail.getPropertyInfo(Constants.DB_TYPE).getValue();
 					if (!databases.contains(dbtype)) {
 						databases.add(dbtype);
 					}
 				}
 			}
 		} catch (PhrescoException e) {
 			S_LOGGER.error("Entered into catch block of  Build.configSQL()" + FrameworkUtil.getStackTraceAsString(e));
 		}
 		return SUCCESS;
 	}
 	
 	public String fetchSQLFiles() {
 		String dbtype = null;
 		String dbversion = null;
 		String path = null;
 		String sqlFileName = null;
 		try {
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 			Project project = administrator.getProject(projectCode);
 			sqlFiles = new ArrayList<String>();
 			String techId = project.getApplicationInfo().getTechInfo().getVersion();
 			String selectedDb = getHttpRequest().getParameter("selectedDb");
 			String sqlPath = sqlFolderPathMap.get(techId);
 			List<SettingsInfo> databaseDetails = administrator.getSettingsInfos( Constants.SETTINGS_TEMPLATE_DB,
 					projectCode, environments);
 			for (SettingsInfo databasedetail : databaseDetails) {
 				dbtype = databasedetail.getPropertyInfo(Constants.DB_TYPE).getValue();
 				if (selectedDb.equals(dbtype)) { 
 					dbversion = databasedetail.getPropertyInfo(Constants.DB_VERSION).getValue();
 					File[] dbSqlFiles = new File(Utility.getProjectHome() + projectCode + sqlPath + selectedDb
 							+ File.separator + dbversion).listFiles(new DumpFileNameFilter());
 					for (int i = 0; i < dbSqlFiles.length; i++) {
 						if (!dbSqlFiles[i].isDirectory()) {
 						 sqlFileName = dbSqlFiles[i].getName();
 						path = sqlPath + selectedDb + FILE_SEPARATOR +  dbversion + "#SEP#" +  sqlFileName ;
 						sqlFiles.add(path);
 					}
 				  }
 				}
 			}
 			
 		} catch (PhrescoException e) {
 			S_LOGGER.error("Entered into catch block of  Build.getSQLFiles()" + FrameworkUtil.getStackTraceAsString(e));
 		}
 		return SUCCESS;
 	}
 	
 	class DumpFileNameFilter implements FilenameFilter {
 
 		public boolean accept(File dir, String name) {
 			return !(name.startsWith("."));
 		}
 	}
 	
 	
 
 	private static void initDbPathMap() {
 		sqlFolderPathMap.put(TechnologyTypes.PHP, "/source/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.PHP_DRUPAL6, "/source/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.PHP_DRUPAL7, "/source/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, "/source/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, "/src/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, "/src/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.HTML5_WIDGET, "/src/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.JAVA_WEBSERVICE, "/src/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.WORDPRESS, "/source/sql/");
 	}
 
 
 	public InputStream getFileInputStream() {
 		return fileInputStream;
 	}
 
 	public String getFileName() {
 		return fileName;
 	}
 
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 
 	public String getServer() {
 		return server;
 	}
 
 	public void setServer(String server) {
 		this.server = server;
 	}
 
 	public String getDatabase() {
 		return database;
 	}
 
 	public void setDatabase(String database) {
 		this.database = database;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getWebservice() {
 		return webservice;
 	}
 
 	public void setWebservice(String webservice) {
 		this.webservice = webservice;
 	}
 
 	public String getImportSql() {
 		return importSql;
 	}
 
 	public void setImportSql(String importSql) {
 		this.importSql = importSql;
 	}
 
 	public String getShowError() {
 		return showError;
 	}
 
 	public void setShowError(String showError) {
 		this.showError = showError;
 	}
 
 	public String getConnectionAlive() {
 		return connectionAlive;
 	}
 
 	public void setConnectionAlive(String connectionAlive) {
 		this.connectionAlive = connectionAlive;
 	}
 
 	public String getProjectCode() {
 		return projectCode;
 	}
 
 	public void setProjectCode(String projectCode) {
 		this.projectCode = projectCode;
 	}
 
 	public String getSdk() {
 		return sdk;
 	}
 
 	public void setSdk(String sdk) {
 		this.sdk = sdk;
 	}
 
 	public String getMode() {
 		return mode;
 	}
 
 	public void setMode(String mode) {
 		this.mode = mode;
 	}
 
 	public String getAndroidVersion() {
 		return androidVersion;
 	}
 
 	public void setAndroidVersion(String androidVersion) {
 		this.androidVersion = androidVersion;
 	}
 
 	public String getEnvironments() {
 		return environments;
 	}
 
 	public void setEnvironments(String environments) {
 		this.environments = environments;
 	}
 
 	public String getSerialNumber() {
 		return serialNumber;
 	}
 
 	public void setSerialNumber(String serialNumber) {
 		this.serialNumber = serialNumber;
 	}
 
 	public String getTarget() {
 		return target;
 	}
 
 	public void setTarget(String target) {
 		this.target = target;
 	}
 
 	public String getProguard() {
 		return proguard;
 	}
 
 	public void setProguard(String proguard) {
 		this.proguard = proguard;
 	}
 
 	public String getHideLog() {
 		return hideLog;
 	}
 
 	public void setHideLog(String hideLog) {
 		this.hideLog = hideLog;
 	}
 
 	public String getProjectModule() {
 		return projectModule;
 	}
 
 	public void setProjectModule(String projectModule) {
 		this.projectModule = projectModule;
 	}
 
 	public String getDeployTo() {
 		return deployTo;
 	}
 
 	public void setDeployTo(String deployTo) {
 		this.deployTo = deployTo;
 	}
 
 	public String getSkipTest() {
 		return skipTest;
 	}
 
 	public void setSkipTest(String skipTest) {
 		this.skipTest = skipTest;
 	}
 
 	public String getShowDebug() {
 		return showDebug;
 	}
 
 	public void setShowDebug(String showDebug) {
 		this.showDebug = showDebug;
 	}
 
 	public String getUserBuildName() {
 		return userBuildName;
 	}
 
 	public void setUserBuildName(String userBuildName) {
 		this.userBuildName = userBuildName;
 	}
 
 	public String getUserBuildNumber() {
 		return userBuildNumber;
 	}
 
 	public void setUserBuildNumber(String userBuildNumber) {
 		this.userBuildNumber = userBuildNumber;
 	}
 
 	public String getMainClassName() {
 		return mainClassName;
 	}
 
 	public void setMainClassName(String mainClassName) {
 		this.mainClassName = mainClassName;
 	}
 
 	public String getJarName() {
 		return jarName;
 	}
 
 	public void setJarName(String jarName) {
 		this.jarName = jarName;
 	}
 
 	public String getKeystore() {
 		return keystore;
 	}
 
 	public void setKeystore(String keystore) {
 		this.keystore = keystore;
 	}
 
 	public String getStorepass() {
 		return storepass;
 	}
 
 	public void setStorepass(String storepass) {
 		this.storepass = storepass;
 	}
 
 	public String getKeypass() {
 		return keypass;
 	}
 
 	public void setKeypass(String keypass) {
 		this.keypass = keypass;
 	}
 
 	public String getAlias() {
 		return alias;
 	}
 
 	public void setAlias(String alias) {
 		this.alias = alias;
 	}
 
 	public boolean isProfileCreationStatus() {
 		return profileCreationStatus;
 	}
 
 	public void setProfileCreationStatus(boolean profileCreationStatus) {
 		this.profileCreationStatus = profileCreationStatus;
 	}
 
 	public String getProfileCreationMessage() {
 		return profileCreationMessage;
 	}
 
 	public void setProfileCreationMessage(String profileCreationMessage) {
 		this.profileCreationMessage = profileCreationMessage;
 	}
 
 	public String getProfileAvailable() {
 		return profileAvailable;
 	}
 
 	public void setProfileAvailable(String profileAvailable) {
 		this.profileAvailable = profileAvailable;
 	}
 
 	public String getSigning() {
 		return signing;
 	}
 
 	public void setSigning(String signing) {
 		this.signing = signing;
 	}
 
 	public List<String> getSqlFiles() {
 		return sqlFiles;
 	}
 
 	public void setSqlFiles(List<String> sqlFiles) {
 		this.sqlFiles = sqlFiles;
 	}
 
 	public List<String> getDatabases() {
 		return databases;
 	}
 
 	public void setDatabases(List<String> databases) {
 		this.databases = databases;
 	}
 
 	public String getFileType() {
 		return fileType;
 	}
 
 	public void setFileType(String fileType) {
 		this.fileType = fileType;
 	}
 
 	public void setBrowseLocation(String browseLocation) {
 		this.browseLocation = browseLocation;
 	}
 
 	public String getBrowseLocation() {
 		return browseLocation;
 	}
 
 	public String getFamily() {
 		return family;
 	}
 
 	public void setFamily(String family) {
 		this.family = family;
 	}
 
 	public String getConfiguration() {
 		return configuration;
 	}
 
 	public void setConfiguration(String configuration) {
 		this.configuration = configuration;
 	}
 
 	public String getPlatform() {
 		return platform;
 	}
 
 	public void setPlatform(String platform) {
 		this.platform = platform;
 	}
 
 	public List<Value> getDependentValues() {
 		return dependentValues;
 	}
 
 	public void setDependentValues(List<Value> dependentValues) {
 		this.dependentValues = dependentValues;
 	}
 
 	public String getDependantKey() {
 		return dependantKey;
 	}
 
 	public void setDependantKey(String dependantKey) {
 		this.dependantKey = dependantKey;
 	}
 
 	public String getDependantValue() {
 		return dependantValue;
 	}
 
 	public void setDependantValue(String dependantValue) {
 		this.dependantValue = dependantValue;
 	}
 
 	public String getGoal() {
 		return goal;
 	}
 
 	public void setGoal(String goal) {
 		this.goal = goal;
 	}
 
 	public void setFrom(String from) {
 		this.from = from;
 	}
 
 	public String getFrom() {
 		return from;
 	}
 
 	public String getFetchSql() {
 		return fetchSql;
 	}
 
 	public void setFetchSql(String fetchSql) {
 		this.fetchSql = fetchSql;
 	}
 
 	public void setMinifyAll(String minifyAll) {
 		this.minifyAll = minifyAll;
 	}
 
 	public String getMinifyAll() {
 		return minifyAll;
 	}
 
 	public String getSelectedFilesToMinify() {
 		return selectedFilesToMinify;
 	}
 
 	public void setSelectedFilesToMinify(String selectedFilesToMinify) {
 		this.selectedFilesToMinify = selectedFilesToMinify;
 	}
 
 	public void setCompressName(String compressName) {
 		this.compressName = compressName;
 	}
 
 	public String getCompressName() {
 		return compressName;
 	}
 
 	public void setMinifyFileNames(List<String> minifyFileNames) {
 		this.minifyFileNames = minifyFileNames;
 	}
 
 	public List<String> getMinifyFileNames() {
 		return minifyFileNames;
 	}
 
 	public void setMinifiedFiles(String minifiedFiles) {
 		this.minifiedFiles = minifiedFiles;
 	}
 
 	public String getMinifiedFiles() {
 		return minifiedFiles;
 	}
 
 	public String getFileOrFolder() {
 		return fileOrFolder;
 	}
 
 	public void setFileOrFolder(String fileOrFolder) {
 		this.fileOrFolder = fileOrFolder;
 	}
 
 	public String getProjectId() {
 		return projectId;
 	}
 
 	public void setProjectId(String projectId) {
 		this.projectId = projectId;
 	}
 }
 
 class BuildComparator implements Comparator<BuildInfo> {
 	public int compare(BuildInfo buildInfo1, BuildInfo buildInfo2) {
 		DateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss");
 		Date  buildTime1 = new Date();
 		Date buildTime2 = new Date();
 		try {
 			buildTime1 = (Date)formatter.parse(buildInfo1.getTimeStamp());
 			buildTime2 = (Date)formatter.parse(buildInfo2.getTimeStamp());
 		} catch (ParseException e) {
 		}
 		
 		return buildTime2.compareTo(buildTime1);
 	}
 }
