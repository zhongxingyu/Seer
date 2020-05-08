 /**
  * Framework Web Archive
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
 package com.photon.phresco.framework.rest.api.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.KeyStore;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLException;
 import javax.net.ssl.SSLSocket;
 import javax.net.ssl.SSLSocketFactory;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.TrustManagerFactory;
 import javax.net.ssl.X509TrustManager;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.StringUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonIOException;
 import com.google.gson.JsonSyntaxException;
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.BuildInfo;
 import com.photon.phresco.commons.model.CertificateInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.configuration.ConfigReader;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.FrameworkConfiguration;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.api.ApplicationManager;
 import com.photon.phresco.framework.api.ProjectManager;
 import com.photon.phresco.framework.commons.ApplicationsUtil;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Model.Modules;
 import com.phresco.pom.util.PomProcessor;
 
 public class FrameworkServiceUtil implements Constants, FrameworkConstants {
 	
 	/**
 	 * To get the application info of the given appDirName
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public static ApplicationInfo getApplicationInfo(String appDirName) throws PhrescoException {
 		try {
 			ProjectInfo projectInfo = getProjectInfo(appDirName);
 			ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 			return applicationInfo;
 		} catch (JsonIOException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	/**
 	 * To get the project info of the given appDirName
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public static ProjectInfo getProjectInfo(String appDirName) throws PhrescoException {
 		StringBuilder builder  = new StringBuilder();
 		builder.append(Utility.getProjectHome())
 		.append(appDirName)
 		.append(File.separatorChar)
 		.append(DOT_PHRESCO_FOLDER)
 		.append(File.separatorChar)
 		.append(PROJECT_INFO_FILE);
 		try {
 			BufferedReader bufferedReader = new BufferedReader(new FileReader(builder.toString()));
 			Gson gson = new Gson();
 			ProjectInfo projectInfo = gson.fromJson(bufferedReader, ProjectInfo.class);
 			return projectInfo;
 		} catch (JsonSyntaxException e) {
 			throw new PhrescoException(e);
 		} catch (JsonIOException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	/**
 	 * To get the PomProcessor instance for the given application
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public static PomProcessor getPomProcessor(String appDirName) throws PhrescoException {
 		try {
 			StringBuilder builder  = new StringBuilder();
 			builder.append(Utility.getProjectHome())
 			.append(appDirName)
 			.append(File.separatorChar)
 			.append(POM_NAME);
 			return new PomProcessor(new File(builder.toString()));
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * To get the application home for the given appDirName
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public static String getApplicationHome(String appDirName) throws PhrescoException {
         StringBuilder builder = new StringBuilder(Utility.getProjectHome());
         builder.append(appDirName);
         return builder.toString();
 	}
 	
 	public static String getAppPom(String appDirName) throws PhrescoException {
 		StringBuilder builder = new StringBuilder(getApplicationHome(appDirName));
 		builder.append(File.separator);
         builder.append(getPomFileName(getApplicationInfo(appDirName)));
 		return builder.toString();
 	}
 	
 	 public static String getPomFileName(ApplicationInfo appInfo) {
 	    	File pomFile = new File(Utility.getProjectHome() + appInfo.getAppDirName() + File.separator + appInfo.getPomFile());
 	    	if(pomFile.exists()) {
 	    		return appInfo.getPomFile();
 	    	}
 	    	return Constants.POM_NAME;
 	    }
 
 	/**
 	 * To get the modules of the given application
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public static List<String> getProjectModules(String appDirName) throws PhrescoException {
     	try {
             PomProcessor processor = getPomProcessor(appDirName);
     		Modules pomModule = processor.getPomModule();
     		if (pomModule != null) {
     			return pomModule.getModule();
     		}
     	} catch (PhrescoPomException e) {
     		 throw new PhrescoException(e);
     	}
     	
     	return null;
     }
 	
 	/**
 	 * To get the war project modules of the given application
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public static List<String> getWarProjectModules(String appDirName) throws PhrescoException {
     	try {
 			List<String> projectModules = getProjectModules(appDirName);
 			List<String> warModules = new ArrayList<String>(5);
 			if (CollectionUtils.isNotEmpty(projectModules)) {
 				for (String projectModule : projectModules) {
 					PomProcessor processor = getPomProcessor(appDirName);
 					String packaging = processor.getModel().getPackaging();
 					if (StringUtils.isNotEmpty(packaging) && WAR.equalsIgnoreCase(packaging)) {
 						warModules.add(projectModule);
 					}
 				}
 			}
 			return warModules;
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
     }
 	
 	/**
 	 * To ge the unit test directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getUnitTestDir(String appDirName) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appDirName).getProperty(POM_PROP_KEY_UNITTEST_DIR);
     }
 	
 	/**
 	 * To get the functional test directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getFunctionalTestDir(String appDirName) throws PhrescoException, PhrescoPomException {
 		return getPomProcessor(appDirName).getProperty(POM_PROP_KEY_FUNCTEST_DIR);
 	}
 	
 	/**
 	 * To get the component test directory
 	 * @param appinfo
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getComponentTestDir(String appDirName) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appDirName).getProperty(POM_PROP_KEY_COMPONENTTEST_DIR);
     }
 	
 	/**
 	 * To get the load test directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getLoadTestDir(String appDirName) throws PhrescoException, PhrescoPomException {
     	return getPomProcessor(appDirName).getProperty(POM_PROP_KEY_LOADTEST_DIR);
     }
 	
 	
 	/**
 	 * To get the load test report directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getLoadTestReportDir(String appDirName) throws PhrescoException, PhrescoPomException {
     	return getPomProcessor(appDirName).getProperty(POM_PROP_KEY_LOADTEST_RPT_DIR);
     }
 	
 	/**
 	 * To get the load test result file extension
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getLoadResultFileExtension(String appDirName) throws PhrescoException, PhrescoPomException {
 			return getPomProcessor(appDirName).getProperty(Constants.POM_PROP_KEY_LOADTEST_RESULT_EXTENSION);
 	}
 	
 	/**
 	 * To get the performance test directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getPerformanceTestDir(String appDirName) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appDirName).getProperty(POM_PROP_KEY_PERFORMANCETEST_DIR);
     }
 	
 	/**
 	 * To get the performance jmx upload directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getPerformanceUploadJmxDir(String appDirName) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appDirName).getProperty(POM_PROP_KEY_PERFORMANCETEST_JMX_UPLOAD_DIR);
     }
 	
 	/**
 	 * To get the performance test result directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getPerformanceTestReportDir(String appDirName) throws PhrescoException, PhrescoPomException {
 			return getPomProcessor(appDirName).getProperty(Constants.POM_PROP_KEY_PERFORMANCETEST_RPT_DIR);
 	}
 	
 	/**
 	 * To get the performance test result file extension
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getPerformanceResultFileExtension(String appDirName) throws PhrescoException, PhrescoPomException {
 			return getPomProcessor(appDirName).getProperty(Constants.POM_PROP_KEY_PERFORMANCETEST_RESULT_EXTENSION);
 	}
 	/**
 	 * To get the manual test directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getManualTestDir(String appDirName) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appDirName).getProperty(POM_PROP_KEY_MANUALTEST_RPT_DIR);
     }
 	
 	/**
 	 * To get the build test directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public static String getBuildDir(String appDirName) throws PhrescoException {
 		StringBuilder builder = new StringBuilder(getApplicationHome(appDirName))
 		.append(File.separator)
 		.append(BUILD_DIR);
         return builder.toString();
     }
 	
 	public static String getFunctionalTestFramework(String appDirName) throws PhrescoException, PhrescoPomException {
 		return getPomProcessor(appDirName).getProperty(POM_PROP_KEY_FUNCTEST_SELENIUM_TOOL);
 	}
 	
 	public static List<ApplicationInfo> getAppInfos(String customerId, String projectId) throws PhrescoException {
 		try {
 			ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 			ProjectInfo projectInfo = projectManager.getProject(projectId, customerId);
 			if(projectInfo != null) {
 				return projectInfo.getAppInfos();
 			}
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}	
 		return new ArrayList<ApplicationInfo>();
 	}
 	
 	public static String getConfigFileDir(String appDirName) {
 		StringBuilder builder = new StringBuilder();
 		builder.append(Utility.getProjectHome())
 		.append(appDirName)
 		.append(File.separatorChar)
 		.append(Constants.DOT_PHRESCO_FOLDER)
 		.append(File.separatorChar)
 		.append(Constants.CONFIGURATION_INFO_FILE);
 		return builder.toString();
 	}
 	
 	 //get server Url for sonar
     public static String getSonarURL(HttpServletRequest request) throws PhrescoException {
     	FrameworkConfiguration frameworkConfig = PhrescoFrameworkFactory.getFrameworkConfig();
     	String serverUrl = getSonarHomeURL(request);
 	    String sonarReportPath = frameworkConfig.getSonarReportPath();
 	    String[] sonar = sonarReportPath.split("/");
 	    serverUrl = serverUrl.concat(FORWARD_SLASH + sonar[1]);
 	    return serverUrl;
     }
     
     //get server Url for sonar
     public static String getSonarHomeURL(HttpServletRequest request) throws PhrescoException {
     	FrameworkConfiguration frameworkConfig = PhrescoFrameworkFactory.getFrameworkConfig();
     	String serverUrl = "";
     	
 	    if (StringUtils.isNotEmpty(frameworkConfig.getSonarUrl())) {
 	    	serverUrl = frameworkConfig.getSonarUrl();
 	    } else {
 	    	serverUrl = request.getRequestURL().toString();
 	    	StringBuilder tobeRemoved = new StringBuilder();
 	    	tobeRemoved.append(request.getContextPath());
 	    	tobeRemoved.append(request.getServletPath());
 	    	tobeRemoved.append(request.getPathInfo());
 
 	    	Pattern pattern = Pattern.compile(tobeRemoved.toString());
 	    	Matcher matcher = pattern.matcher(serverUrl);
 	    	serverUrl = matcher.replaceAll("");
 	    }
 	    return serverUrl;
     }
     
     private static String getBuildInfosFilePath(String appDirName) throws PhrescoException {
     	return getApplicationHome(appDirName) + FILE_SEPARATOR + BUILD_DIR + FILE_SEPARATOR +BUILD_INFO_FILE_NAME;
     }
     
 	
 	/**
 	 * To the phresco plugin info file path based on the goal
 	 * @param goal
 	 * @return
 	 * @throws PhrescoException 
 	 */
 	public static String getPhrescoPluginInfoFilePath(String goal, String phase, String appDirName) throws PhrescoException {
 		StringBuilder sb = new StringBuilder(getApplicationHome(appDirName));
 		sb.append(File.separator);
 		sb.append(FOLDER_DOT_PHRESCO);
 		sb.append(File.separator);
 		sb.append(PHRESCO_HYPEN);
 		// when phase is CI, it have to take ci info file for update dependency
 		if (PHASE_CI.equals(phase)) {
 			sb.append(phase);
 		} else if (StringUtils.isNotEmpty(goal) && goal.contains(FUNCTIONAL)) {
 			sb.append(PHASE_FUNCTIONAL_TEST);
 		} else if (PHASE_RUNGAINST_SRC_START.equals(goal)|| PHASE_RUNGAINST_SRC_STOP.equals(goal) ) {
 			sb.append(PHASE_RUNAGAINST_SOURCE);
 		} else {
 			sb.append(phase);
 		}
 		sb.append(INFO_XML);
 
 		return sb.toString();
 	}
 
 	
 	public static List<Parameter> getMojoParameters(MojoProcessor mojo, String goal) throws PhrescoException {
 		com.photon.phresco.plugins.model.Mojos.Mojo.Configuration mojoConfiguration = mojo.getConfiguration(goal);
 		if (mojoConfiguration != null) {
 			return mojoConfiguration.getParameters().getParameter();
 		}
 
 		return null;
 	}
 	
 	public static List<String> getMavenArgCommands(List<Parameter> parameters) throws PhrescoException {
 		List<String> buildArgCmds = new ArrayList<String>();	
 		if(CollectionUtils.isEmpty(parameters)) {
 			return buildArgCmds;
 		}
 		for (Parameter parameter : parameters) {
 			if (parameter.getPluginParameter()!= null && PLUGIN_PARAMETER_FRAMEWORK.equalsIgnoreCase(parameter.getPluginParameter())) {
 				List<MavenCommand> mavenCommand = parameter.getMavenCommands().getMavenCommand();
 				for (MavenCommand mavenCmd : mavenCommand) {
 					if (StringUtils.isNotEmpty(parameter.getValue()) && parameter.getValue().equalsIgnoreCase(mavenCmd.getKey())) {
 						buildArgCmds.add(mavenCmd.getValue());
 					}
 				}
 			}
 		}
 		return buildArgCmds;
 	}
 	
 	public static String getSettingsPath(String customerId) {
 		return Utility.getProjectHome() + customerId + FrameworkConstants.SETTINGS_XML;
 	}
 
 	public static List<CertificateInfo> getCertificate(String host, int port) throws PhrescoException {
 		List<CertificateInfo> certificates = new ArrayList<CertificateInfo>();
 		CertificateInfo info;
 		try {
 			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
 			SSLContext context = SSLContext.getInstance("TLS");
 			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
 			tmf.init(ks);
 			X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
 			SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
 			context.init(null, new TrustManager[] { tm }, null);
 			SSLSocketFactory factory = context.getSocketFactory();
 			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
 			socket.setSoTimeout(10000);
 			try {
 				socket.startHandshake();
 				socket.close();
 			} catch (SSLException e) {
 
 			}
 			X509Certificate[] chain = tm.chain;
 			for (int i = 0; i < chain.length; i++) {
 				X509Certificate x509Certificate = chain[i];
 				String subjectDN = x509Certificate.getSubjectDN().getName();
 				String[] split = subjectDN.split(",");
 				info = new CertificateInfo();
 				info.setSubjectDN(subjectDN);
 				info.setDisplayName(split[0]);
 				info.setCertificate(x509Certificate);
 				certificates.add(info);
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return certificates;
 	}
 
 	public static void addCertificate(CertificateInfo info, File file) throws PhrescoException {
 		char[] passphrase = "changeit".toCharArray();
 		InputStream inputKeyStore = null;
 		OutputStream outputKeyStore = null;
 		try {
 			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
 			keyStore.load(null);
 			keyStore.setCertificateEntry(info.getDisplayName(), info.getCertificate());
 			if (!file.exists()) {
 				file.getParentFile().mkdirs();
 				file.createNewFile();
 			}
 			outputKeyStore = new FileOutputStream(file);
 			keyStore.store(outputKeyStore, passphrase);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeStream(inputKeyStore);
 			Utility.closeStream(outputKeyStore);
 		}
 	}
 	
 	public static ActionResponse checkForConfigurations(ActionResponse actionresponse, String appDirName, String environmentName, String customerId) throws PhrescoException {
 		ConfigManager configManager = null;
 		List<String> errorMsg = new ArrayList<String>();
 		try {
 			File baseDir = new File(Utility.getProjectHome() + appDirName);
 			File configFile = new File(baseDir +  File.separator + Constants.DOT_PHRESCO_FOLDER + File.separator + Constants.CONFIGURATION_INFO_FILE);
 			File settingsFile = new File(Utility.getProjectHome()+ customerId + SETTINGS_INFO_FILE_NAME);
 			List<String> selectedEnvs = csvToList(environmentName);
 			List<String> selectedConfigTypeList = getSelectedConfigTypeList(baseDir.getName());
 			List<String> nullConfig = new ArrayList<String>();
 			if (settingsFile.exists()) {
 				configManager = PhrescoFrameworkFactory.getConfigManager(settingsFile);
 				List<Environment> environments = configManager.getEnvironments();
 				for (Environment environment : environments) {
 					if (selectedEnvs.contains(environment.getName())) {
 						if (CollectionUtils.isNotEmpty(selectedConfigTypeList)) {
 							for (String selectedConfigType : selectedConfigTypeList) {
 								if(CollectionUtils.isEmpty(configManager.getConfigurations(environment.getName(), selectedConfigType))) {
 									nullConfig.add(selectedConfigType);
 								}
 							}
 						}
 					} if(CollectionUtils.isNotEmpty(nullConfig)) {
 						String errMsg = environment.getName() + " environment in global settings doesnot have "+ nullConfig + " configurations";
 						actionresponse.setErrorFound(true);
 						errorMsg.add(errMsg);
 						actionresponse.setConfigErrorMsg(errorMsg);
 					}
 				} 
 			} 
 			configManager = PhrescoFrameworkFactory.getConfigManager(configFile);
 			List<Environment> environments = configManager.getEnvironments();
 			for (Environment environment : environments) {
 				if (selectedEnvs.contains(environment.getName())) {
 					if (CollectionUtils.isNotEmpty(selectedConfigTypeList)) {
 						for (String selectedConfigType : selectedConfigTypeList) {
 							if(CollectionUtils.isEmpty(configManager.getConfigurations(environment.getName(), selectedConfigType))) {
 								nullConfig.add(selectedConfigType);
 							}
 						}
 					}
 				} if(CollectionUtils.isNotEmpty(nullConfig)) {
 					String errMsg = environment.getName() + " environment in " + baseDir.getName() + " doesnot have "+ nullConfig + " configurations";
 					actionresponse.setErrorFound(true);
 					errorMsg.add(errMsg);
 					actionresponse.setConfigErrorMsg(errorMsg);
 				}
 			} 
 			return actionresponse;
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		} catch (ConfigurationException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	public static List<String> csvToList(String csvString) {
 		List<String> envs = new ArrayList<String>();
 		if (StringUtils.isNotEmpty(csvString)) {
 			String[] temp = csvString.split(",");
 			for (int i = 0; i < temp.length; i++) {
 				envs.add(temp[i]);
 			}
 		}
 		return envs;
 	}
 	
 	private static List<String> getSelectedConfigTypeList(String  baseDir) throws PhrescoException {
 		try {
 			ApplicationInfo appInfo = getApplicationInfo(baseDir);
 			List<String> selectedList = new ArrayList<String>();
 			if(CollectionUtils.isNotEmpty(appInfo.getSelectedServers())) {
 				selectedList.add("Server");
 			}
 			if(CollectionUtils.isNotEmpty(appInfo.getSelectedDatabases())) {
 				selectedList.add("Database");
 			}
 			if(CollectionUtils.isNotEmpty(appInfo.getSelectedWebservices())) {
 				selectedList.add("WebService");
 			}
 			return selectedList;
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	public List<Configuration> configurationList(String configType) throws PhrescoException {
 		try {
 			InputStream stream = null;
 			stream = this.getClass().getClassLoader().getResourceAsStream(Constants.CONFIGURATION_INFO_FILE);
 			ConfigReader configReader = new ConfigReader(stream);
 			String environment = System.getProperty("SERVER_ENVIRONMENT");
 			if (environment == null || environment.isEmpty() ) {
 				environment = configReader.getDefaultEnvName();
 			}
 			return configReader.getConfigurations(environment, configType);
 		} catch (Exception e) {
 		    throw new PhrescoException(e);
 		}
 	}
 
 	public ActionResponse mandatoryValidation(ActionResponse actionresponse, HttpServletRequest request, String goal, String appDirName) throws PhrescoException {
 		List<String> errorMsg = new ArrayList<String>();
 		try {
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			List<BuildInfo> builds = applicationManager.getBuildInfos(new File(getBuildInfosFilePath(appDirName)));
 			File infoFile = new File(getPhrescoPluginInfoFilePath(goal, null ,appDirName));
 			MojoProcessor mojo = new MojoProcessor(infoFile);
 			List<Parameter> parameters = getMojoParameters(mojo, goal);
 			List<String> eventDependencies = new ArrayList<String>();
 			List<String> dropDownDependencies = null;
 			Map<String, List<String>> validateMap = new HashMap<String, List<String>>();
 			if (CollectionUtils.isNotEmpty(parameters)) {
 				for (Parameter parameter : parameters) {
 					if (TYPE_BOOLEAN.equalsIgnoreCase(parameter.getType()) && StringUtils.isNotEmpty(parameter.getDependency())) {
 						//To validate check box dependency controls
 						eventDependencies = Arrays.asList(parameter.getDependency().split(CSV_PATTERN));
 						validateMap.put(parameter.getKey(), eventDependencies);//add checkbox dependency keys to map
 						if (request.getParameter(parameter.getKey()) != null && dependentParamMandatoryChk(mojo, eventDependencies, goal, request, actionresponse ,errorMsg)) {
 							break;//break from loop if error exists
 						}
 					} else if (TYPE_LIST.equalsIgnoreCase(parameter.getType()) &&  !Boolean.parseBoolean(parameter.getMultiple())
 							&& parameter.getPossibleValues() != null) {
 						//To validate (Parameter type - LIST) single select list box dependency controls
 						if (StringUtils.isNotEmpty(request.getParameter(parameter.getKey()))) {
 							List<Value> values = parameter.getPossibleValues().getValue();
 							String allPossibleValueDependencies = fetchAllPossibleValueDependencies(values);
 							eventDependencies = Arrays.asList(allPossibleValueDependencies.toString().split(CSV_PATTERN));
 							validateMap.put(parameter.getKey(), eventDependencies);//add psbl value dependency keys to map
 							for (Value value : values) {
 								dropDownDependencies = new ArrayList<String>();
 								if (value.getKey().equalsIgnoreCase(request.getParameter((parameter.getKey())))
 										&& StringUtils.isNotEmpty(value.getDependency())) {
 									//get currently selected option's dependency keys to validate and break from loop
 									dropDownDependencies = Arrays.asList(value.getDependency().split(CSV_PATTERN));
 									break;
 								}
 							}
 							if (dependentParamMandatoryChk(mojo, dropDownDependencies, goal, request, actionresponse, errorMsg)) {
 								//break from loop if error exists
 								break;
 							}
 						}
 					} else if (Boolean.parseBoolean(parameter.getRequired())) {
 						//comes here for other controls
 						boolean alreadyValidated = fetchAlreadyValidatedKeys(validateMap, parameter);
 						if ((parameter.isShow() || !alreadyValidated)) {
 							ActionResponse paramsMandatoryCheck = paramsMandatoryCheck(parameter, request, actionresponse, errorMsg);
 							if (paramsMandatoryCheck.isErrorFound()) {
 								break;
 							}
 						}
 					} else if(TYPE_STRING.equalsIgnoreCase(parameter.getType()) && BUILD_NAME.equalsIgnoreCase(parameter.getKey())) {
 						List<String> platforms = new ArrayList<String>(); 
 						String buildName = request.getParameter((parameter.getKey()));
 						String platform = request.getParameter((PLATFORM));
 						if (StringUtils.isNotEmpty(platform)) {
 							String[] split = platform.split(COMMA);
 							for (String plaform : split) {
 								platforms.add(plaform.replaceAll("\\s+", "") + METRO_BUILD_SEPARATOR + buildName);
 							}
 						}
 
 						if(!buildName.isEmpty()) {
 							for (BuildInfo build : builds) {
 								String bldName = FilenameUtils.removeExtension(build.getBuildName());
 								if (bldName .contains(METRO_BUILD_SEPARATOR) && CollectionUtils.isNotEmpty(platforms)) {	
 									for (String name : platforms) {
 										if (name.equalsIgnoreCase(bldName)) {	
 											actionresponse.setErrorFound(true);
 											errorMsg.add("Build Name Already Exsist");
 											actionresponse.setConfigErrorMsg(errorMsg);
 										}
 									}
 								} else if(buildName.equalsIgnoreCase(FilenameUtils.removeExtension(build.getBuildName()))) {
 									actionresponse.setErrorFound(true);
 									errorMsg.add("Build Name Already Exsist");
 									actionresponse.setConfigErrorMsg(errorMsg);
 								}
 							}
 						}
 					} else if(TYPE_NUMBER.equalsIgnoreCase(parameter.getType()) && BUILD_NUMBER.equalsIgnoreCase(parameter.getKey())) {
 						String buildNumber = request.getParameter(parameter.getKey());
 						if(!buildNumber.isEmpty()) {
 							for (BuildInfo build : builds) {
 								if(Integer.parseInt(buildNumber) == build.getBuildNo()) {
 									actionresponse.setErrorFound(true);
 									errorMsg.add("Build Number Already Exsist");
 									actionresponse.setConfigErrorMsg(errorMsg);
 								}
 							}
 						}
 					} 
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 
 		return actionresponse;
 	}
 
 	/**
 	 * Gets the port no.
 	 *
 	 * @param path the path
 	 * @return the port no
 	 * @throws PhrescoException the phresco exception
 	 */
 	public static String getJenkinsPortNo() throws PhrescoException {
 		String portNo = "";
 		try {
 			String jenkinsHome = Utility.getJenkinsHome();
 			StringBuilder path = new StringBuilder(jenkinsHome);
 			Document document = ApplicationsUtil.getDocument(new File(path.toString() + File.separator + POM_FILE));
 			String portNoNode = CI_TOMCAT_HTTP_PORT;
 			NodeList nodelist = org.apache.xpath.XPathAPI.selectNodeList(document, portNoNode);
 			portNo = nodelist.item(0).getTextContent();
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return portNo;
 	}
 	
 	private boolean fetchAlreadyValidatedKeys(Map<String, List<String>> validateMap, Parameter parameter) {
 		boolean alreadyValidated = false;
 		Set<String> keySet = validateMap.keySet();
 		for (String key : keySet) {
 			List<String> valueList = validateMap.get(key);
 			if (valueList.contains(parameter.getKey())) {
 				alreadyValidated = true;
 			}
 		}
 		return alreadyValidated;
 	}
 	
 	private String fetchAllPossibleValueDependencies(List<Value> values) {
 		StringBuilder sb = new StringBuilder();
 		String sep = "";
 		for (Value value : values) {
 			if (StringUtils.isNotEmpty(value.getDependency())) {
 				sb.append(sep);
 				sb.append(value.getDependency());
 				sep = COMMA;
 			}
 		}
 
 		return sb.toString();
 	}
 	private static boolean dependentParamMandatoryChk(MojoProcessor mojo, List<String> eventDependencies, String goal, HttpServletRequest request, ActionResponse validateinfo, List<String> errorMsg) {
 		boolean flag = false;
 		if (CollectionUtils.isNotEmpty(eventDependencies)) {
 			for (String eventDependency : eventDependencies) {
 				Parameter dependencyParameter = mojo.getParameter(goal, eventDependency);
 				ActionResponse paramsMandatoryCheck = paramsMandatoryCheck(dependencyParameter, request, validateinfo, errorMsg);
 				if (Boolean.parseBoolean(dependencyParameter.getRequired()) && paramsMandatoryCheck.isErrorFound()) {
 					flag = true;
 					break;
 				}
 			}
 		}
 		return flag;
 	}
 	   
 	   private static ActionResponse paramsMandatoryCheck (Parameter parameter, HttpServletRequest request, ActionResponse validateinfo, List<String> errorMsg) {
 		   String lableTxt =  getParameterLabel(parameter);
 			if (TYPE_STRING.equalsIgnoreCase(parameter.getType()) || TYPE_NUMBER.equalsIgnoreCase(parameter.getType())
 					|| TYPE_PASSWORD.equalsIgnoreCase(parameter.getType())
 					|| TYPE_DYNAMIC_PARAMETER.equalsIgnoreCase(parameter.getType()) && !Boolean.parseBoolean(parameter.getMultiple())
 					|| (TYPE_LIST.equalsIgnoreCase(parameter.getType()) && !Boolean.parseBoolean(parameter.getMultiple()))
 					|| (TYPE_FILE_BROWSE.equalsIgnoreCase(parameter.getType()))) {
 				
 				if (FROM_PAGE_EDIT.equalsIgnoreCase(parameter.getEditable())) {//For editable combo box
 					validateinfo = editableComboValidate(parameter, lableTxt, request, validateinfo, errorMsg);
 				} else {//for text box,non editable single select list box,file browse
 					validateinfo = textSingleSelectValidate(parameter,lableTxt, request, validateinfo, errorMsg);
 				}
 			} else if (TYPE_DYNAMIC_PARAMETER.equalsIgnoreCase(parameter.getType()) && Boolean.parseBoolean(parameter.getMultiple()) || 
 					(TYPE_LIST.equalsIgnoreCase(parameter.getType()) && Boolean.parseBoolean(parameter.getMultiple()))) {
 				validateinfo = multiSelectValidate(parameter, lableTxt, request, validateinfo, errorMsg);//for multi select list box
 			} else if (parameter.getType().equalsIgnoreCase(TYPE_MAP)) {
 				validateinfo = mapControlValidate(parameter, request, validateinfo, errorMsg);//for type map
 			}
 			return validateinfo;
 		}
 	   
 	   private static String getParameterLabel(Parameter parameter) {
 			String lableTxt = "";
 			List<com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Name.Value> labels = parameter.getName().getValue();
 			for (com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Name.Value label : labels) {
 				if (label.getLang().equals("en")) {	//to get label of parameter
 					lableTxt = label.getValue();
 				    break;
 				}
 			}
 			return lableTxt;
 		}
 	   
 	   private static ActionResponse textSingleSelectValidate(Parameter parameter, String lableTxt, HttpServletRequest request, ActionResponse validateinfo, List<String> errorMsg) {
 			if (StringUtils.isEmpty(request.getParameter(parameter.getKey()))) {
 				validateinfo.setErrorFound(true);
 				errorMsg.add(lableTxt + " " + "is missing");
 				validateinfo.setConfigErrorMsg(errorMsg); 
 			}
 			return validateinfo;
 		}
 		
 		private static ActionResponse editableComboValidate(Parameter parameter, String lableTxt, HttpServletRequest request, ActionResponse validateinfo, List<String> errorMsg) {
 			String value = request.getParameter(parameter.getKey());
 			value = value.replaceAll("\\s+", "").toLowerCase();
 			
 			if (StringUtils.isEmpty(value) || "typeorselectfromthelist".equalsIgnoreCase(value)) {
 				validateinfo.setErrorFound(true);
 				errorMsg.add(lableTxt + " " + "is missing");
 				validateinfo.setConfigErrorMsg(errorMsg);
 			} 
 			return validateinfo;
 		}
 		
 		/**
 		 * To validate key value pair control
 		 * @param parameter
 		 * @param returnFlag
 		 * @return
 		 */
 		private static ActionResponse mapControlValidate(Parameter parameter, HttpServletRequest request, ActionResponse validateinfo, List<String> errorMsg) {
 			List<Child> childs = parameter.getChilds().getChild();
 			String[] keys = request.getParameterValues(childs.get(0).getKey());
 			String[] values = request.getParameterValues(childs.get(1).getKey());
 			String childLabel = "";
 			for (int i = 0; i < keys.length; i++) {
 				if (StringUtils.isEmpty(keys[i]) && Boolean.parseBoolean(childs.get(0).getRequired())) {
 					childLabel = childs.get(0).getName().getValue().getValue();
 					validateinfo.setErrorFound(true);
 					errorMsg.add(childLabel + " " + "is missing");
 					validateinfo.setConfigErrorMsg(errorMsg);
 					break;
 				} else if (StringUtils.isEmpty(values[i]) && Boolean.parseBoolean(childs.get(1).getRequired())) {
 					childLabel = childs.get(1).getName().getValue().getValue();
 					validateinfo.setErrorFound(true);
 					errorMsg.add(childLabel + " " + "is missing");
 					validateinfo.setConfigErrorMsg(errorMsg);
 					break;
 				}
 			}
 			return validateinfo;
 		}
 
 		/**
 		 * To validate multi select list box
 		 * @param parameter
 		 * @param returnFlag
 		 * @param lableTxt
 		 * @return
 		 */
 		private static ActionResponse multiSelectValidate(Parameter parameter, String lableTxt, HttpServletRequest request, ActionResponse validateinfo, List<String> errorMsg) {
 			if (request.getParameterValues(parameter.getKey()) == null) {//for multi select list box
 				validateinfo.setErrorFound(true);
 				errorMsg.add(lableTxt + " " + "is missing");
 				validateinfo.setConfigErrorMsg(errorMsg);
 			}
 			return validateinfo;
 		}
 		
 }
 
 class SavingTrustManager implements X509TrustManager {
 
 	private final X509TrustManager tm;
 	X509Certificate[] chain;
 
 	SavingTrustManager(X509TrustManager tm) {
 		this.tm = tm;
 	}
 
 	public X509Certificate[] getAcceptedIssuers() {
 		throw new UnsupportedOperationException();
 	}
 
 	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
 		throw new UnsupportedOperationException();
 	}
 
 	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
 		this.chain = chain;
 		tm.checkServerTrusted(chain, authType);
 	}
 }
