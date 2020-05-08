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
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
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
 import com.photon.phresco.framework.api.ProjectRuntimeManager;
 import com.photon.phresco.framework.commons.DiagnoseUtil;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.commons.LogErrorReport;
 import com.photon.phresco.framework.model.DependantParameters;
 import com.photon.phresco.framework.model.PluginProperties;
 import com.photon.phresco.framework.model.SettingsInfo;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.plugins.util.MojoProcessor;
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
 	private String fileType = null;
 	private String fileorfolder = null;
 	private String selectedJs = null;
 	private String jsFinalName = null;
 	private String browseLocation = null;
 	private String fileLocation = null;
 	
 	//iphone family
 	private String family = ""; 
 	
 	//Windows family
 	private String configuration = ""; 
 	private String platform = "";
 	
 	private static Map<String, String> sqlFolderPathMap = new HashMap<String, String>();
 
 	// DbWithSqlFiles
 	private String DbWithSqlFiles = null;
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
 					tempConnectionAlive = DiagnoseUtil.isConnectionAlive(serverProtocol, serverHost, serverPort);
 					setSessionAttribute(getAppId() + SESSION_SERVER_STATUS, tempConnectionAlive);
 				}
 				if (tempConnectionAlive) {
 					readLogFile = readRunAgsSrcLogFile();
 				} else {
 					deleteLogFile();
 					readLogFile = "";
 
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
             List<Parameter> parameters = getDynamicParameters(appInfo, PHASE_PACKAGE);
             setPossibleValuesInReq(appInfo, parameters, watcherMap);
             setSessionAttribute(appInfo.getId() + PHASE_PACKAGE + SESSION_WATCHER_MAP, watcherMap);
             setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
             setReqAttribute(REQ_GOAL, PHASE_PACKAGE);
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
 			Map<String, Object> runAgainstSrcMap = new HashMap<String, Object>();
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			runAgainstSrcMap.put(REQ_APP_INFO, applicationInfo);
 			List<Parameter> parameters = getDynamicParameters(applicationInfo, PHASE_RUNGAINST_SRC_START);
 //            setPossibleValuesInReq(parameters, runAgainstSrcMap);
 			
 			setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
 			setReqAttribute(REQ_APPINFO, applicationInfo);
 			setReqAttribute(REQ_FROM, getFrom());
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
 //            watcherMap.put(REQ_CUSTOMER_ID, getCustomerId());
             String buildNumber = getReqParameter(REQ_DEPLOY_BUILD_NUMBER);
 //            watcherMap.put(REQ_DEPLOY_BUILD_NUMBER, buildNumber);
             List<Parameter> parameters = getDynamicParameters(appInfo, PHASE_DEPLOY);
             setPossibleValuesInReq(appInfo, parameters, watcherMap);
             setSessionAttribute(appInfo.getId() + PHASE_DEPLOY + SESSION_WATCHER_MAP, watcherMap);
             setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
             setReqAttribute(REQ_GOAL, PHASE_DEPLOY);
             setProjModulesInReq();
             setReqAttribute(REQ_FROM, getFrom());
            setReqAttribute(REQ_DEPLOY_BUILD_NUMBER, buildNumber);
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
 			MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(applicationInfo)));
 			persistValuesToXml(mojo, PHASE_PACKAGE);
 			
 			//To get maven build arguments
 			List<Parameter> parameters = getMojoParameters(mojo, PHASE_PACKAGE);
 			List<String> buildArgCmds = getMavenArgCommands(parameters);
 			String workingDirectory = getAppDirectoryPath(applicationInfo);
 			
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
 	
 	public String deploy() throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  Build.deploy()");
 		}
 		try {
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ProjectInfo projectInfo = getProjectInfo();
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(applicationInfo)));
 			
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
 
 		String[] buildNumbers = getHttpRequest().getParameterValues(REQ_BUILD_NUMBER);
 		if (buildNumbers == null || buildNumbers.length == 0) {
 			// TODO: Warn the user
 		}
 
 		int[] buildInts = new int[buildNumbers.length];
 		for (int i = 0; i < buildNumbers.length; i++) {
 			if (debugEnabled) {
 				S_LOGGER.debug("To be deleted build numbers " + buildNumbers[i]);
 			}
 			buildInts[i] = Integer.parseInt(buildNumbers[i]);
 		}
 
 		try {
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 			Project project = administrator.getProject(projectCode);
 			administrator.deleteBuildInfos(project, buildInts);
 			getHttpRequest().setAttribute(REQ_PROJECT, project);
 			addActionMessage(getText(SUCCESS_BUILD_DELETE));
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.delete()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			new LogErrorReport(e, "Deleting build");
 		}
 
 		getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 		return view();
 	}
 
 	public String download() throws PhrescoException {
 
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method Build.download()");
 		}
 		String buildNumber = getHttpRequest().getParameter(REQ_DEPLOY_BUILD_NUMBER);
 		try {
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 			Project project = administrator.getProject(projectCode);
 			StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 			builder.append(project.getApplicationInfo().getCode());
 			builder.append(File.separator);
 			String moduleName = administrator.getBuildInfo(project, Integer.parseInt(buildNumber)).getModuleName();
 			if (StringUtils.isNotEmpty(moduleName)) {
 				builder.append(moduleName);
 				builder.append(File.separator);
 			}
 			builder.append(BUILD_DIR);
 			builder.append(File.separator);
 			builder.append(administrator.getBuildInfo(project, Integer.parseInt(buildNumber)).getBuildName());
 			if (debugEnabled) {
 				S_LOGGER.debug("Download build number " + buildNumber + " Download location " + builder.toString());
 			}
 			if (TechnologyTypes.IPHONES.contains(project.getApplicationInfo().getTechInfo().getVersion())) {
 				String path = administrator.getBuildInfo(project, Integer.parseInt(buildNumber)).getDeliverables();
 				fileInputStream = new FileInputStream(new File(path));
 				fileName = administrator.getBuildInfo(project, Integer.parseInt(buildNumber)).getBuildName();
 				fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
 			} else {
 				fileInputStream = new FileInputStream(new File(builder.toString()));
 				fileName = administrator.getBuildInfo(project, Integer.parseInt(buildNumber)).getBuildName();
 			}
 			return SUCCESS;
 		} catch (FileNotFoundException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.download()" + e);
 			}
 			new LogErrorReport(e, "Download builds");
 
 		} catch (Exception e1) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Build.download()" + FrameworkUtil.getStackTraceAsString(e1));
 			}
 			new LogErrorReport(e1, "Download builds");
 		}
 		return view();
 	}
 
 	public String downloadIpa() throws PhrescoException {
 
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method Build.downloadIPA()");
 		}
 		String buildNumber = getHttpRequest().getParameter(REQ_DEPLOY_BUILD_NUMBER);
 		try {
 //			ActionType actionType = ActionType.IPHONE_DOWNLOADIPA_COMMAND;
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 			ProjectRuntimeManager runtimeManager = PhrescoFrameworkFactory.getProjectRuntimeManager();
 			Project project = administrator.getProject(projectCode);
 			StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 			builder.append(project.getApplicationInfo().getCode());
 			builder.append(File.separator);
 			builder.append(BUILD_DIR);
 			builder.append(File.separator);
 			builder.append(administrator.getBuildInfo(project, Integer.parseInt(buildNumber)).getBuildName());
 			String buildName = administrator.getBuildInfo(project, Integer.parseInt(buildNumber)).getBuildName();
 			String buildNameSubstring = buildName.substring(0, buildName.lastIndexOf("/"));
 			String appBuildName = buildNameSubstring.substring(buildNameSubstring.lastIndexOf("/") + 1);
 			Map<String, String> valuesMap = new HashMap<String, String>(2);
 			valuesMap.put("application.name", projectCode);
 			valuesMap
 					.put("app.path", administrator.getBuildInfo(project, Integer.parseInt(buildNumber)).getBuildName());
 			valuesMap.put("build.name", appBuildName);
 //			BufferedReader reader = runtimeManager.performAction(project, actionType, valuesMap, null);
 //			while (reader.readLine() != null) {
 //			}
 			String ipaPath = administrator.getBuildInfo(project, Integer.parseInt(buildNumber)).getBuildName();
 			ipaPath = ipaPath.substring(0, ipaPath.lastIndexOf("/")) + FILE_SEPARATOR + projectCode + ".ipa";
 			fileInputStream = new FileInputStream(new File(ipaPath));
 			fileName = projectCode + ".ipa";
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
 			com.photon.phresco.api.ConfigManager configManager = PhrescoFrameworkFactory
 					.getConfigManager(new File(Utility.getProjectHome() + getApplicationInfo().getAppDirName()
 							+ File.separator + Constants.DOT_PHRESCO_FOLDER + File.separator
 							+ Constants.CONFIGURATION_INFO_FILE));
 			List<com.photon.phresco.configuration.Configuration> configurations = configManager.getConfigurations(
 					"Production", Constants.SETTINGS_TEMPLATE_SERVER);
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
 			boolean connectionAlive = DiagnoseUtil.isConnectionAlive(serverProtocol, serverHost, serverPort);
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
 		
 		return null;
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
 
 	private void getValueFromJavaStdAlonePom() throws PhrescoException {
 		try {
 			File file = new File(Utility.getProjectHome() + File.separator + getApplicationInfo().getAppDirName() + File.separator + POM_FILE);
 			PomProcessor pomProcessor = new PomProcessor(file);
 			String finalName = pomProcessor.getFinalName();
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document document = dBuilder.parse(file);
 			document.getDocumentElement().normalize();
 			NodeList nodeList = document.getElementsByTagName(JAVA_POM_MANIFEST);
 			String mainClassValue = "";
 			for (int temp = 0; temp < nodeList.getLength(); temp++) {
 				Node node = nodeList.item(temp);
 				if (node.getNodeType() == Node.ELEMENT_NODE) {
 					Element mainClassElement = (Element) node;
 					mainClassValue = mainClassElement.getElementsByTagName(JAVA_POM_MAINCLASS).item(0).getTextContent();
 					break;
 				}
 			}
 			getHttpRequest().setAttribute(FINAL_NAME, finalName);
 			getHttpRequest().setAttribute(MAIN_CLASS_VALUE, mainClassValue);
 
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} catch (ParserConfigurationException e) {
 			throw new PhrescoException(e);
 		} catch (SAXException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	/* minification  */
 	public String jsFileBrowser() {
 		try {
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 	        Project project = administrator.getProject(projectCode);
 	        String technology = project.getApplicationInfo().getTechInfo().getVersion();
 	        getHttpRequest().setAttribute(REQ_TECHNOLOGY, technology);
 			getHttpRequest().setAttribute(FILE_TYPES, fileType);
 			getHttpRequest().setAttribute(FILE_BROWSE, fileorfolder);
 			String projectLocation = Utility.getProjectHome() + projectCode;
 			getHttpRequest().setAttribute(REQ_PROJECT_LOCATION, projectLocation.replace(File.separator, FORWARD_SLASH));
 			getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
 			getHttpRequest().setAttribute(REQ_BUILD_FROM, getHttpRequest().getParameter(REQ_BUILD_FROM));
 			getHttpRequest().setAttribute(REQ_COMPRESS_NAME,getHttpRequest().getParameter(REQ_COMPRESS_NAME));
 			getHttpRequest().setAttribute(REQ_SELECTED_FILES,getHttpRequest().getParameter(REQ_SELECTED_FILES));
 		} catch (Exception e){
 			S_LOGGER.error("Entered into catch block of  Build.jsFileBrowser()"	+ FrameworkUtil.getStackTraceAsString(e));
 		}
 		return SUCCESS;
 	}
 	
 	public String selectJsFilesToMinify() {
 		try {
 			String[] jsFiles = getHttpRequest().getParameterValues(REQ_CHECKED_FILE_LIST);
 			StringBuilder sb = new StringBuilder();
 			String sep = "";
 			for (String jsFile : jsFiles) {
 				sb.append(sep);
 				sb.append(jsFile);
 			    sep = ",";
 			}
 
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 	        Project project = administrator.getProject(projectCode);
 	        String techId = project.getApplicationInfo().getTechInfo().getVersion();
 	        StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 	        builder.append(project.getApplicationInfo().getCode());
 	        getHttpRequest().setAttribute(REQ_BUILD_FROM, getHttpRequest().getParameter(REQ_BUILD_FROM));
 	        getHttpRequest().setAttribute(REQ_TECHNOLOGY, techId);
 	        setSelectedJs(sb.toString());
 		} catch (Exception e){
 			S_LOGGER.error("Entered into catch block of  Build.selectJsFilesToMinify()"	+ FrameworkUtil.getStackTraceAsString(e));
 		}
 
 		return SUCCESS;
 	}
 	
 	private void minification(){
 		try {
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 			Project project = administrator.getProject(projectCode);
 			
 			StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 	        builder.append(project.getApplicationInfo().getCode());
 	        File systemPath = new File(builder.toString() + File.separator + POM_FILE);
 	        
 	        PomProcessor processor = new PomProcessor(systemPath);
 	        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 			List<Element> configList = new ArrayList<Element>();
 			
 			String[] jsFileNames = getHttpRequest().getParameterValues(REQ_SELECTED_FILE_NAMES);
 
 			if(!ArrayUtils.isEmpty(jsFileNames)) {
 				configList.add(createElement(doc, POM_SOURCEDIR, POM_SOURCE_DIRECTORY));
 				configList.add(createElement(doc, POM_OUTPUTDIR, POM_OUTPUT_DIRECTORY));
 				configList.add(createElement(doc, POM_FORCE, POM_VALUE_TRUE));
 				configList.add(createElement(doc, POM_JS_WARN, POM_VALUE_FALSE));
 				configList.add(createElement(doc, POM_NO_SUFFIX, POM_VALUE_TRUE));
 				configList.add(createElement(doc, POM_LINE_BREAK, POM_LINE_MAX_COL_COUNT));
 				
 				Element excludesElement = doc.createElement(POM_EXCLUDES);
 				appendChildElement(doc, excludesElement, POM_EXCLUDE, POM_EXCLUDE_CSS);
 				appendChildElement(doc, excludesElement, POM_EXCLUDE, POM_EXCLUDE_JS);
 				configList.add(excludesElement);
 				
 				Element aggregationsElement = doc.createElement(POM_AGGREGATIONS);
 				
 				for (String jsFileName : jsFileNames) {
 					String csvJsFile = getHttpRequest().getParameter(jsFileName);
 					List<String> jsFiles = Arrays.asList(csvJsFile.split("\\s*,\\s*"));
 				
 					Element agrigationElement = appendChildElement(doc, aggregationsElement, POM_AGGREGATION, null);
 					appendChildElement(doc, agrigationElement, POM_INPUTDIR, POM_INPUT_DIRECTORY);
 					Element includesElement = doc.createElement(POM_INCLUDES);
 					for (String jsFile : jsFiles) {
 						appendChildElement(doc, includesElement, POM_INCLUDE, "**/" + jsFile);
 						agrigationElement.appendChild(includesElement);
 					}
 					String[] splitted = fileLocation.split(projectCode);
 					String minificationDir = "";
 					minificationDir = splitted[1];
 					appendChildElement(doc, agrigationElement, POM_OUTPUT, MINIFY_OUTPUT_DIRECTORY + minificationDir + jsFileName + MINIFY_FILE_EXT);
 				}
 				configList.add(aggregationsElement);
 		        
 			} else {
 				configList.add(createElement(doc, POM_SKIP, POM_VALUE_TRUE));
 			}
 			processor.addConfiguration(MINIFY_PLUGIN_GROUPID, MINIFY_PLUGIN_ARTFACTID, configList);
 			processor.save();
 			
 		} catch (Exception e) {
 				S_LOGGER.error("Entered into catch block of Build.minification()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 				new LogErrorReport(e, "Building ");
 			}
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
 	/* minification end */
 	
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
 
 	public void configureSqlExecution() {
 		Map<String, List<String>> dbsWithSqlFiles = new HashMap<String, List<String>>();
 		String[] dbWithSqlFiles = DbWithSqlFiles.split("#SEP#");
 		for (String dbWithSqlFile : dbWithSqlFiles) {
 			String[] sqlFiles = dbWithSqlFile.split("#VSEP#");
 			String[] sqlFileWithName = sqlFiles[1].split("#NAME#");
 			if (dbsWithSqlFiles.containsKey(sqlFiles[0])) {
 				List<String> sqlFilesList = dbsWithSqlFiles.get(sqlFiles[0]);
 				sqlFilesList.add(sqlFileWithName[0]);
 				dbsWithSqlFiles.put(sqlFiles[0], sqlFilesList);
 			} else {
 				List<String> sqlFilesList = new ArrayList<String>();
 				sqlFilesList.add(sqlFileWithName[0]);
 				dbsWithSqlFiles.put(sqlFiles[0], sqlFilesList);
 			}
 		}
 		FileWriter writer = null;
 		try {
 			File sqlInfoFilePath = new File(Utility.getProjectHome() + projectCode + Constants.JSON_PATH);
 			String json = new Gson().toJson(dbsWithSqlFiles);
 			writer = new FileWriter(new File(sqlInfoFilePath.getPath()));
 			writer.write(json);
 		} catch (IOException e) {
 			S_LOGGER.error("Entered into catch block of  Build.configureSqlExecution()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 		} finally {
 			try {
 				if (writer != null) {
 					writer.close();
 				}
 			} catch (IOException e) {
 				S_LOGGER.error("Entered into catch block of  Build.configureSqlExecution()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 			}
 		}
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
 
 	public String getDbWithSqlFiles() {
 		return DbWithSqlFiles;
 	}
 
 	public void setDbWithSqlFiles(String dbWithSqlFiles) {
 		DbWithSqlFiles = dbWithSqlFiles;
 	}
 
 	public String getFileType() {
 		return fileType;
 	}
 
 	public void setFileType(String fileType) {
 		this.fileType = fileType;
 	}
 
 	public String getFileorfolder() {
 		return fileorfolder;
 	}
 
 	public void setFileorfolder(String fileorfolder) {
 		this.fileorfolder = fileorfolder;
 	}
 	
 	public String getSelectedJs() {
 		return selectedJs;
 	}
 
 	public void setSelectedJs(String selectedJs) {
 		this.selectedJs = selectedJs;
 	}
 
 	public String getJsFinalName() {
 		return jsFinalName;
 	}
 
 	public void setJsFinalName(String jsFinalName) {
 		this.jsFinalName = jsFinalName;
 	}
 
 	public void setBrowseLocation(String browseLocation) {
 		this.browseLocation = browseLocation;
 	}
 
 	public String getBrowseLocation() {
 		return browseLocation;
 	}
 
 	public void setFileLocation(String fileLocation) {
 		this.fileLocation = fileLocation;
 	}
 
 	public String getFileLocation() {
 		return fileLocation;
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
 }
