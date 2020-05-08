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
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
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
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonIOException;
 import com.google.gson.JsonSyntaxException;
 import com.photon.phresco.api.ApplicationProcessor;
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.ResponseCodes;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.BuildInfo;
 import com.photon.phresco.commons.model.CIJob;
 import com.photon.phresco.commons.model.CertificateInfo;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.RepoInfo;
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
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.rest.api.QualityService;
 import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.PhrescoDynamicLoader;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Model.Modules;
 import com.phresco.pom.util.PomProcessor;
 
 public class FrameworkServiceUtil implements Constants, FrameworkConstants, ResponseCodes {
 	
 	/**
 	 * To get the application info of the given appDirName
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public static ApplicationInfo getApplicationInfo(String appDirName) throws PhrescoException {
 		try {
 			ProjectInfo projectInfo = getProjectInfo(appDirName);
 			if (projectInfo != null) {
 				ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 				return applicationInfo;
 			}
 		} catch (JsonIOException e) {
 			throw new PhrescoException(e);
 		}
 		return null;
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
 		BufferedReader bufferedReader = null;
 		try {
 			File projectInfoFile = new File(builder.toString());
 			if (!projectInfoFile.exists()) {
 				return null;
 			}
 			bufferedReader = new BufferedReader(new FileReader(builder.toString()));
 			Gson gson = new Gson();
 			ProjectInfo projectInfo = gson.fromJson(bufferedReader, ProjectInfo.class);
 			return projectInfo;
 		} catch (JsonSyntaxException e) {
 			throw new PhrescoException(e);
 		} catch (JsonIOException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		} finally {
 			if (bufferedReader != null) {
 				try {
 					bufferedReader.close();
 				} catch (Exception e) {
 					throw new PhrescoException(e);
 				}
 			}
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
 			ApplicationInfo applicationInfo = getApplicationInfo(appDirName);
 			StringBuilder builder  = new StringBuilder();
 			String pomFileName = Utility.getPhrescoPomFile(applicationInfo);
 			builder.append(Utility.getProjectHome())
 			.append(appDirName)
 			.append(File.separatorChar)
 			.append(pomFileName);
 			return new PomProcessor(new File(builder.toString()));
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	public static PomProcessor getSourcePomProcessor(String appDirName) throws PhrescoException {
 		try {
 			ApplicationInfo applicationInfo = getApplicationInfo(appDirName);
 			StringBuilder builder  = new StringBuilder();
 			String pomFileName = Utility.getPhrescoPomFile(applicationInfo);
 			builder.append(Utility.getProjectHome())
 			.append(appDirName)
 			.append(File.separatorChar)
 			.append(pomFileName);
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
         builder.append(Utility.getPhrescoPomFile(getApplicationInfo(appDirName)));
 		return builder.toString();
 	}
 	
 	/**
 	 * To get the modules of the given application
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public static List<String> getProjectModules(String appDirName) throws PhrescoException {
     	try {
     		PomProcessor pomProcessor = Utility.getPomProcessor(Utility.getProjectHome() + appDirName, "");
     		Modules pomModule = pomProcessor.getPomModule();
     		if (pomModule != null) {
     			return pomModule.getModule();
     		}
     	} catch (PhrescoPomException e) {
     		 throw new PhrescoException(e);
     	}
     	
     	return null;
     }
 	
 	public static List<String> getProjectModules(String rootModulePath, String subModule) throws PhrescoException {
     	try {
     		ProjectInfo projectInfo = Utility.getProjectInfo(rootModulePath, subModule);
     		File sourceFolderLocation = Utility.getSourceFolderLocation(projectInfo, rootModulePath, subModule);
     		PomProcessor processor = new PomProcessor(new File(sourceFolderLocation + File.separator + Constants.POM_NAME));
 //			Modules pomModules = processor.getPomModule();
 			
 //            PomProcessor processor = getSourcePomProcessor(appDirName);
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
 					PomProcessor processor = getSourcePomProcessor(appDirName);
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
 	public static String getUnitTestDir(String rootModulePath, String subModule) throws PhrescoException, PhrescoPomException {
         return Utility.getPomProcessor(rootModulePath, subModule).getProperty(POM_PROP_KEY_UNITTEST_DIR);
     }
 	
 	/**
 	 * To get the functional test directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getFunctionalTestDir(String rootModulePath, String subModule) throws PhrescoException, PhrescoPomException {
 		return Utility.getPomProcessor(rootModulePath, subModule).getProperty(POM_PROP_KEY_FUNCTEST_DIR);
 	}
 	
 	/**
 	 * To get the component test directory
 	 * @param appinfo
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getComponentTestDir(String rootModulePath, String subModule) throws PhrescoException, PhrescoPomException {
         return Utility.getPomProcessor(rootModulePath, subModule).getProperty(POM_PROP_KEY_COMPONENTTEST_DIR);
     }
 	
 	/**
 	 * To get the load test directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getLoadTestDir(String rootModulePath, String subModule) throws PhrescoException, PhrescoPomException {
     	return Utility.getPomProcessor(rootModulePath, subModule).getProperty(POM_PROP_KEY_LOADTEST_DIR);
     }
 	
 	
 	/**
 	 * @param projectCode
 	 * @return
 	 */
 	public static String getGlobalConfigFileDir(String projectCode) {
 		StringBuilder builder = new StringBuilder();
 		builder.append(Utility.getProjectHome())
 		.append(projectCode)
 		.append(Constants.STR_HYPHEN)
 		.append(Constants.CONFIGURATION_FILE);
 		return builder.toString();
 	}
 	
 	
 	
 	/**
 	 * To get the load test report directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getLoadTestReportDir(String rootModulePath, String subModule) throws PhrescoException, PhrescoPomException {
     	return Utility.getPomProcessor(rootModulePath, subModule).getProperty(POM_PROP_KEY_LOADTEST_RPT_DIR);
     }
 	
 	/**
 	 * To get the load test result file extension
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getLoadResultFileExtension(String rootModulePath, String subModule) throws PhrescoException, PhrescoPomException {
 			return Utility.getPomProcessor(rootModulePath, subModule).getProperty(Constants.POM_PROP_KEY_LOADTEST_RESULT_EXTENSION);
 	}
 	
 	/**
 	 * To get the load jmx upload directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getLoadUploadJmxDir(String appDirName) throws PhrescoException, PhrescoPomException {
         return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(POM_PROP_KEY_LOADTEST_JMX_UPLOAD_DIR);
     }
 	
 	/**
 	 * To get the performance test directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getPerformanceTestDir(String rootModulePath, String subModule) throws PhrescoException, PhrescoPomException {
         return Utility.getPomProcessor(rootModulePath, subModule).getProperty(POM_PROP_KEY_PERFORMANCETEST_DIR);
     }
 	
 	/**
 	 * To get the performance jmx upload directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getPerformanceUploadJmxDir(String appDirName) throws PhrescoException, PhrescoPomException {
         return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(POM_PROP_KEY_PERFORMANCETEST_JMX_UPLOAD_DIR);
     }
 	
 	/**
 	 * To get the performance test result directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getPerformanceTestReportDir(String rootModulePath, String subModule) throws PhrescoException, PhrescoPomException {
 		return Utility.getPomProcessor(rootModulePath, subModule).getProperty(Constants.POM_PROP_KEY_PERFORMANCETEST_RPT_DIR);
 	}
 	
 	/**
 	 * To get the performance test result file extension
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getPerformanceResultFileExtension(String rootModulePath, String subModule) throws PhrescoException, PhrescoPomException {
 			return Utility.getPomProcessor(rootModulePath, subModule).getProperty(Constants.POM_PROP_KEY_PERFORMANCETEST_RESULT_EXTENSION);
 	}
 	/**
 	 * To get the manual test directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException
 	 */
 	public static String getManualTestDir(String rootModulePath, String subModule) throws PhrescoException, PhrescoPomException {
         return Utility.getPomProcessor(rootModulePath, subModule).getProperty(POM_PROP_KEY_MANUALTEST_RPT_DIR);
     }
 	
 	/**
 	 * To get the build test directory
 	 * @param appDirName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public static String getBuildDir(String rootModulePath, String subModuleName) throws PhrescoException {
 		File getpomFileLocation = Utility.getPomFileLocation(rootModulePath, subModuleName);
 		StringBuilder builder = new StringBuilder(getpomFileLocation.getParent())
 		.append(File.separator)
 		.append(BUILD_DIR);
         return builder.toString();
     }
 	
 	public static String getFunctionalTestFramework(String rootModulePath, String subModule) throws PhrescoException, PhrescoPomException {
 		return  Utility.getPomProcessor(rootModulePath, subModule).getProperty(POM_PROP_KEY_FUNCTEST_SELENIUM_TOOL);
 	}
 
 	public static String getConfigFileDir(String appDirName, String moduleName) {
 		StringBuilder builder = new StringBuilder();
 		builder.append(Utility.getProjectHome());
 		builder.append(appDirName);
 		builder.append(File.separatorChar);
 		if(StringUtils.isNotEmpty(moduleName)) {
 			builder.append(moduleName);
 			builder.append(File.separatorChar);
 		}
 		builder.append(Constants.DOT_PHRESCO_FOLDER);
 		builder.append(File.separatorChar);
 		builder.append(Constants.CONFIGURATION_INFO_FILE);
 		return builder.toString();
 	}
 	
 	public static String getnonEnvConfigFileDir(String appDirName) {
 		StringBuilder builder = new StringBuilder();
 		builder.append(Utility.getProjectHome())
 		.append(appDirName)
 		.append(File.separatorChar)
 		.append(Constants.DOT_PHRESCO_FOLDER)
 		.append(File.separatorChar)
 		.append(FrameworkConstants.PHRESCO_CONFIG_FILE_NAME);
 		return builder.toString();
 	}
 	
     public static String getPluginInfoPath(String appDirName) throws PhrescoException {
     	StringBuilder builder = new StringBuilder(Utility.getProjectHome());
     	builder.append(appDirName);
     	builder.append(File.separator);
     	builder.append(FOLDER_DOT_PHRESCO);
     	builder.append(File.separator);
     	builder.append(Constants.APPLICATION_HANDLER_INFO_FILE);
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
     
 	// get server Url for sonar
 	public static String getSonarHomeURL(HttpServletRequest request) throws PhrescoException {
 		FrameworkConfiguration frameworkConfig = PhrescoFrameworkFactory.getFrameworkConfig();
 		String serverUrl = "";
 		StringBuffer sb = null;
 		try {
 			if (StringUtils.isNotEmpty(frameworkConfig.getSonarUrl())) {
 				serverUrl = frameworkConfig.getSonarUrl();
 			} else {
 				serverUrl = request.getRequestURL().toString();
 				URL url = new URL(serverUrl);
 				InetAddress ip = InetAddress.getLocalHost();
 
 				sb = new StringBuffer();
 				sb.append(url.getProtocol());
 				sb.append(PROTOCOL_POSTFIX);
 				sb.append(ip.getHostAddress());
 				sb.append(COLON);
 				sb.append(url.getPort());
 				serverUrl = sb.toString();
 			}
 		} catch (Exception e) {
 		}
 		return serverUrl;
 	}
     
     public static String getBuildInfosFilePath(String appDirName) throws PhrescoException {
     	return getApplicationHome(appDirName) + FILE_SEPARATOR + BUILD_DIR + FILE_SEPARATOR +BUILD_INFO_FILE_NAME;
     }
     
     public static String getBuildInfosFilePath(String rootModulePath, String subModuleName) throws PhrescoException {
     	File pomFile =  Utility.getPomFileLocation(rootModulePath, subModuleName);
     	return pomFile.getParent() + FILE_SEPARATOR + BUILD_DIR + FILE_SEPARATOR + BUILD_INFO_FILE_NAME;
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
 		if (PHASE_CI.equals(goal)) {
 			sb.append(CI_HYPHEN);
 			sb.append(phase);
 		} else if (StringUtils.isNotEmpty(goal) && goal.contains(FUNCTIONAL)) {
 			sb.append(PHASE_FUNCTIONAL_TEST);
 		} else if (PHASE_RUNGAINST_SRC_START.equals(goal)|| PHASE_RUNGAINST_SRC_STOP.equals(goal) ) {
 			sb.append(PHASE_RUNAGAINST_SOURCE);
 		} else {
 			sb.append(goal);
 		}
 		sb.append(INFO_XML);
 
 		return sb.toString();
 	}
 	
 	public static String getPhrescoPluginInfoFilePath(String goal, String phase, String rootModulePath, String subModule) throws PhrescoException {
 		String dotPhrescoFolderPath = Utility.getDotPhrescoFolderPath(rootModulePath, subModule);
 		StringBuilder sb = new StringBuilder(dotPhrescoFolderPath);
 		sb.append(File.separator);
 		sb.append(PHRESCO_HYPEN);
 		// when phase is CI, it have to take ci info file for update dependency
 		if (PHASE_CI.equals(goal)) {
 			sb.append(CI_HYPHEN);
 			sb.append(phase);
 		} else if (StringUtils.isNotEmpty(goal) && goal.contains(FUNCTIONAL)) {
 			sb.append(PHASE_FUNCTIONAL_TEST);
 		} else if (PHASE_RUNGAINST_SRC_START.equals(goal)|| PHASE_RUNGAINST_SRC_STOP.equals(goal) ) {
 			sb.append(PHASE_RUNAGAINST_SOURCE);
 		} else {
 			sb.append(goal);
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
 	
 	public static ActionResponse checkForConfigurations( String rootModulePath, String subModule, String environmentName, String customerId) throws PhrescoException {
 		ConfigManager configManager = null;
 		ActionResponse actionresponse = new ActionResponse();
 		try {
 			String dotPhrescoFolderPath = Utility.getDotPhrescoFolderPath(rootModulePath, subModule);
 			ProjectInfo projectInfo = Utility.getProjectInfo(rootModulePath, subModule);
 			File configFile = new File(dotPhrescoFolderPath + File.separator + Constants.CONFIGURATION_INFO_FILE);
 			File settingsFile = new File(Utility.getProjectHome()+ File.separator + projectInfo.getProjectCode() + Constants.SETTINGS_XML);
 			if (StringUtils.isNotEmpty(environmentName)) {
 				List<String> selectedEnvs = csvToList(environmentName);
 				List<String> selectedConfigTypeList = getSelectedConfigTypeList(projectInfo.getAppInfos().get(0));
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
 							actionresponse.setConfigErr(true);
 							actionresponse.setConfigErrorMsg(errMsg);
 							actionresponse.setStatus(RESPONSE_STATUS_FAILURE);
 							actionresponse.setResponseCode(PHR710020);
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
 						String errMsg = environment.getName() + " environment in " + projectInfo.getAppInfos().get(0).getAppDirName() + " doesnot have "+ nullConfig + " configurations";
 						actionresponse.setErrorFound(true);
 						actionresponse.setConfigErr(true);
 						actionresponse.setConfigErrorMsg(errMsg);
 						actionresponse.setStatus(RESPONSE_STATUS_FAILURE);
 						actionresponse.setResponseCode(PHR710021);
 
 					}
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
 	
 	private static List<String> getSelectedConfigTypeList(ApplicationInfo  appInfo) throws PhrescoException {
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
 
 	public ActionResponse mandatoryValidation( HttpServletRequest request, String goal, String rootModulePath, String subModuleName) throws PhrescoException {
 		ActionResponse actionresponse = new ActionResponse();
 		ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 		try {
 			List<BuildInfo> builds = applicationManager.getBuildInfos(new File(getBuildInfosFilePath(rootModulePath, subModuleName)));
 			File infoFile = new File(getPhrescoPluginInfoFilePath(goal, null ,rootModulePath, subModuleName));
 			MojoProcessor mojo = new MojoProcessor(infoFile);
 			if (Constants.PHASE_FUNCTIONAL_TEST.equals(goal)) {
 				String functionalTestFramework = FrameworkServiceUtil.getFunctionalTestFramework(rootModulePath, subModuleName);
 				goal = goal + HYPHEN + functionalTestFramework;
 			}
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
 						if (request.getParameter(parameter.getKey()) != null && dependentParamMandatoryChk(mojo, eventDependencies, goal, request, actionresponse)) {
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
 							if (dependentParamMandatoryChk(mojo, dropDownDependencies, goal, request, actionresponse)) {
 								//break from loop if error exists
 								break;
 							}
 						}
 					} else if (Boolean.parseBoolean(parameter.getRequired())) {
 						//comes here for other controls
 						boolean alreadyValidated = fetchAlreadyValidatedKeys(validateMap, parameter);
 						if ((parameter.isShow() || !alreadyValidated)) {
 							ActionResponse paramsMandatoryCheck = paramsMandatoryCheck(parameter, request, actionresponse);
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
 											actionresponse.setParameterKey("buildName");
 											actionresponse.setErrorFound(true);
 											actionresponse.setConfigErrorMsg("Build Name Already Exsist");
 											actionresponse.setStatus(RESPONSE_STATUS_FAILURE);
 											actionresponse.setResponseCode(PHR710018);
 										}
 									}
 								} else if(buildName.equalsIgnoreCase(FilenameUtils.removeExtension(build.getBuildName()))) {
 									actionresponse.setErrorFound(true);
 									actionresponse.setParameterKey("buildName");
 									actionresponse.setConfigErrorMsg("Build Name Already Exsist");
 									actionresponse.setStatus(RESPONSE_STATUS_FAILURE);
 									actionresponse.setResponseCode(PHR710018);
 								}
 							}
 						}
 					} else if(TYPE_NUMBER.equalsIgnoreCase(parameter.getType()) && BUILD_NUMBER.equalsIgnoreCase(parameter.getKey())) {
 						String buildNumber = request.getParameter(parameter.getKey());
 						if(!buildNumber.isEmpty()) {
 							for (BuildInfo build : builds) {
 								if(Integer.parseInt(buildNumber) == build.getBuildNo()) {
 									actionresponse.setParameterKey(BUILD_NUMBER);
 									actionresponse.setErrorFound(true);
 									actionresponse.setConfigErrorMsg("Build Number Already Exsist");
 									actionresponse.setStatus(RESPONSE_STATUS_FAILURE);
 									actionresponse.setResponseCode(PHR710019);
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
   
 	   private static boolean dependentParamMandatoryChk(MojoProcessor mojo, List<String> eventDependencies, String goal, HttpServletRequest request, ActionResponse actionResponse) {
 			boolean flag = false;
 			if (CollectionUtils.isNotEmpty(eventDependencies)) {
 				for (String eventDependency : eventDependencies) {
 					Parameter dependencyParameter = mojo.getParameter(goal, eventDependency);
 					ActionResponse paramsMandatoryCheck = paramsMandatoryCheck(dependencyParameter, request, actionResponse);
 					if (Boolean.parseBoolean(dependencyParameter.getRequired()) && paramsMandatoryCheck.isErrorFound()) {
 						flag = true;
 						break;
 					}
 				}
 			}
 			return flag;
 		}
 	   
 	   private static ActionResponse paramsMandatoryCheck (Parameter parameter, HttpServletRequest request, ActionResponse actionResponse) {
 		   String lableTxt =  getParameterLabel(parameter);
 			if (TYPE_STRING.equalsIgnoreCase(parameter.getType()) || TYPE_NUMBER.equalsIgnoreCase(parameter.getType())
 					|| TYPE_PASSWORD.equalsIgnoreCase(parameter.getType())
 					|| TYPE_DYNAMIC_PARAMETER.equalsIgnoreCase(parameter.getType()) && !Boolean.parseBoolean(parameter.getMultiple())
 					|| (TYPE_LIST.equalsIgnoreCase(parameter.getType()) && !Boolean.parseBoolean(parameter.getMultiple()))
 					|| (TYPE_FILE_BROWSE.equalsIgnoreCase(parameter.getType()))) {
 				
 				if (FROM_PAGE_EDIT.equalsIgnoreCase(parameter.getEditable())) {//For editable combo box
 					actionResponse = editableComboValidate(parameter, lableTxt, request, actionResponse);
 				} else { //for text box,non editable single select list box,file browse
 					actionResponse = textSingleSelectValidate(parameter,lableTxt, request, actionResponse);
 				}
 			} else if (TYPE_DYNAMIC_PARAMETER.equalsIgnoreCase(parameter.getType()) && Boolean.parseBoolean(parameter.getMultiple()) || 
 					(TYPE_LIST.equalsIgnoreCase(parameter.getType()) && Boolean.parseBoolean(parameter.getMultiple()))) {
 				actionResponse = multiSelectValidate(parameter, lableTxt, request, actionResponse);//for multi select list box
 			} else if (parameter.getType().equalsIgnoreCase(TYPE_MAP)) {
 				actionResponse = mapControlValidate(parameter, request, actionResponse);//for type map
 			}
 			return actionResponse;
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
 	   
 	   private static ActionResponse textSingleSelectValidate(Parameter parameter, String lableTxt, HttpServletRequest request, ActionResponse actionResponse) {
 			String paramValue = request.getParameter(parameter.getKey());
 			if (!REQ_CONFIGURATIONS.equalsIgnoreCase(parameter.getKey()) && StringUtils.isEmpty(paramValue) && Boolean.parseBoolean(parameter.getRequired())) {
 				actionResponse.setErrorFound(true);
 				actionResponse.setConfigErrorMsg(lableTxt + " " + "is missing"); 
 				actionResponse.setParameterKey(parameter.getKey());
 				actionResponse.setStatus(RESPONSE_STATUS_SUCCESS);
 				actionResponse.setResponseCode(PHR8C00001);
 			}
 			return actionResponse;
 		}
 		
 		private static ActionResponse editableComboValidate(Parameter parameter, String lableTxt, HttpServletRequest request, ActionResponse actionResponse) {
 			String value = request.getParameter(parameter.getKey());
 			value = value.replaceAll("\\s+", "").toLowerCase();
 			if ((StringUtils.isEmpty(value) || "typeorselectfromthelist".equalsIgnoreCase(value)) && Boolean.parseBoolean(parameter.getRequired())) {
 				actionResponse.setErrorFound(true);
 				actionResponse.setConfigErrorMsg(lableTxt + " " + "is missing");
 				actionResponse.setParameterKey(parameter.getKey());
 				actionResponse.setStatus(RESPONSE_STATUS_SUCCESS);
 				actionResponse.setResponseCode(PHR8C00001);
 			} 
 			return actionResponse;
 		}
 		
 		/**
 		 * To validate key value pair control
 		 * @param parameter
 		 * @param returnFlag
 		 * @return
 		 */
 		private static ActionResponse mapControlValidate(Parameter parameter, HttpServletRequest request, ActionResponse actionResponse) {
 			List<Child> childs = parameter.getChilds().getChild();
 			String[] keys = request.getParameterValues(childs.get(0).getKey());
 			String[] values = request.getParameterValues(childs.get(1).getKey());
 			String childLabel = "";
 			for (int i = 0; i < keys.length; i++) {
 				if (StringUtils.isEmpty(keys[i]) && Boolean.parseBoolean(childs.get(0).getRequired())) {
 					childLabel = childs.get(0).getName().getValue().getValue();
 					actionResponse.setErrorFound(true);
 					actionResponse.setConfigErrorMsg(childLabel + " " + "is missing");
 					actionResponse.setParameterKey(parameter.getKey());
 					actionResponse.setStatus(RESPONSE_STATUS_SUCCESS);
 					actionResponse.setResponseCode(PHR8C00001);
 					break;
 				} else if (StringUtils.isEmpty(values[i]) && Boolean.parseBoolean(childs.get(1).getRequired())) {
 					childLabel = childs.get(1).getName().getValue().getValue();
 					actionResponse.setErrorFound(true);
 					actionResponse.setConfigErrorMsg(childLabel + " " + "is missing");
 					actionResponse.setParameterKey(parameter.getKey());
 					actionResponse.setStatus(RESPONSE_STATUS_SUCCESS);
 					actionResponse.setResponseCode(PHR8C00001);
 					break;
 				}
 			}
 			return actionResponse;
 		}
 
 		/**
 		 * To validate multi select list box
 		 * @param parameter
 		 * @param returnFlag
 		 * @param lableTxt
 		 * @return
 		 */
 		private static ActionResponse multiSelectValidate(Parameter parameter, String lableTxt, HttpServletRequest request, ActionResponse actionResponse) {
 			if (request.getParameterValues(parameter.getKey()) == null && Boolean.parseBoolean(parameter.getRequired())) {//for multi select list box
 				actionResponse.setErrorFound(true);
 				actionResponse.setConfigErrorMsg(lableTxt + " " + "is missing");
 				actionResponse.setParameterKey(parameter.getKey());
 				actionResponse.setStatus(RESPONSE_STATUS_SUCCESS);
 				actionResponse.setResponseCode(PHR8C00001);
 			}
 			return actionResponse;
 		}
 		
 	    public ApplicationProcessor getApplicationProcessor(String appDirName, String customerId, ServiceManager serviceManager,  String rootModulePath,String subModuleName) throws PhrescoException {
 	        ApplicationProcessor applicationProcessor = null;
 	        try {
 	            Customer customer = serviceManager.getCustomer(customerId);
 	            RepoInfo repoInfo = customer.getRepoInfo();
 	            String dotPhrescoFolderPath = Utility.getDotPhrescoFolderPath(rootModulePath, subModuleName);
 	            StringBuilder sb = new StringBuilder(dotPhrescoFolderPath)
 	            .append(File.separator)
 	            .append(Constants.APPLICATION_HANDLER_INFO_FILE);
 	            MojoProcessor mojoProcessor = new MojoProcessor(new File(sb.toString()));
 	            ApplicationHandler applicationHandler = mojoProcessor.getApplicationHandler();
 	            if (applicationHandler != null) {
 	                List<ArtifactGroup> plugins = setArtifactGroup(applicationHandler);
 	                PhrescoDynamicLoader dynamicLoader = new PhrescoDynamicLoader(repoInfo, plugins);
 	                applicationProcessor = dynamicLoader.getApplicationProcessor(applicationHandler.getClazz());
 	            }
 	        } catch (PhrescoException e) {
 	           throw new PhrescoException(e);
 	        }
 
 	        return applicationProcessor;
 	    }
 	    
 	    private List<ArtifactGroup> setArtifactGroup(ApplicationHandler applicationHandler) {
 	        List<ArtifactGroup> plugins = new ArrayList<ArtifactGroup>();
 	        ArtifactGroup artifactGroup = new ArtifactGroup();
 	        artifactGroup.setGroupId(applicationHandler.getGroupId());
 	        artifactGroup.setArtifactId(applicationHandler.getArtifactId());
 	        List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 	        ArtifactInfo artifactInfo = new ArtifactInfo();
 	        artifactInfo.setVersion(applicationHandler.getVersion());
 	        artifactInfos.add(artifactInfo);
 	        artifactGroup.setVersions(artifactInfos);
 	        plugins.add(artifactGroup);
 	        return plugins;
 	    }
 	    
 	public  boolean isSonarReportAvailable(FrameworkUtil frameworkUtil,
 			HttpServletRequest request, String rootModulePath, String subModule) throws PhrescoException {
 		boolean isSonarReportAvailable = false;
 		try {
 			// get module appInfo
 //			String isIphone = frameworkUtil.isIphoneTagExists(appDirName);
 			PomProcessor pomProcessor = Utility.getPomProcessor(rootModulePath, subModule);
 			String isIphone = pomProcessor.getProperty(PHRESCO_CODE_VALIDATE_REPORT);
 			if (StringUtils.isEmpty(isIphone)) {
 				FrameworkConfiguration frameworkConfig = PhrescoFrameworkFactory.getFrameworkConfig();
 				String serverUrl = FrameworkServiceUtil.getSonarURL(request);
 				String sonarReportPath = frameworkConfig.getSonarReportPath().replace(
 						FrameworkConstants.FORWARD_SLASH + SONAR, "");
 				serverUrl = serverUrl + sonarReportPath;
 				// sub module pom processor
 //				PomProcessor processor = frameworkUtil.getPomProcessor(appDirName);
 				Modules pomModules = pomProcessor.getPomModule();
 				List<String> modules = null;
 				if (pomModules != null) {
 					modules = pomModules.getModule();
 				}
 				
 				// check multimodule or not
 				List<String> sonarProfiles = frameworkUtil.getSonarProfiles(rootModulePath, subModule);
 				if (CollectionUtils.isEmpty(sonarProfiles)) {
 					sonarProfiles.add(SONAR_SOURCE);
 				}
 				sonarProfiles.add(FUNCTIONAL);
 				boolean isSonarUrlAvailable = false;
 				if (CollectionUtils.isNotEmpty(modules)) {
 					for (String module : modules) {
 						for (String sonarProfile : sonarProfiles) {
 							isSonarUrlAvailable = checkSonarModuleUrl(sonarProfile, serverUrl, module, frameworkUtil, rootModulePath, subModule);
 							if (isSonarUrlAvailable) {
 								isSonarReportAvailable = true;
 								break;
 							}
 						}
 					}
 				} else {
 					for (String sonarProfile : sonarProfiles) {
 						isSonarUrlAvailable = checkSonarUrl(sonarProfile, serverUrl, frameworkUtil, rootModulePath, subModule);
 						if (isSonarUrlAvailable) {
 							isSonarReportAvailable = true;
 							break;
 						}
 					}
 				}
 			} else {
 				File pomFile = Utility.getPomFileLocation(rootModulePath, subModule);
 				StringBuilder sb = new StringBuilder(pomFile.getParent()).append(
 						File.separatorChar).append(DO_NOT_CHECKIN_DIR).append(File.separatorChar).append(
 						STATIC_ANALYSIS_REPORT);
 				File indexPath = new File(sb.toString());
 				if (indexPath.exists() && indexPath.isDirectory()) {
 					File[] listFiles = indexPath.listFiles();
 					for (int i = 0; i < listFiles.length; i++) {
 						File file = listFiles[i];
 						File htmlFileCheck = new File(file, INDEX_HTML);
 						if (htmlFileCheck.exists()) {
 							isSonarReportAvailable = true;
 						}
 					}
 				}
 			}
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 		return isSonarReportAvailable;
 	}
 
 	/**
 	 * Check sonar module url.
 	 * 
 	 * @param sonarProfile
 	 *            the sonar profile
 	 * @param serverUrl
 	 *            the server url
 	 * @param module
 	 *            the module
 	 * @param frameworkUtil
 	 *            the framework util
 	 * @param appInfo
 	 *            the app info
 	 * @return true, if successful
 	 * @throws PhrescoException
 	 *             the phresco exception
 	 */
 	private boolean checkSonarModuleUrl(String sonarProfile, String serverUrl, String module,
 			FrameworkUtil frameworkUtil, String rootModulePath, String subModule) throws PhrescoException {
 		boolean isSonarReportAvailable = false;
 		try {
 			if (StringUtils.isNotEmpty(module)) {
 				PomProcessor processor = Utility.getPomProcessor(rootModulePath, subModule);
 				StringBuilder builder = new StringBuilder();
 //				builder.append(appDirName);
 //				builder.append(File.separatorChar);
 
 //				if (!FUNCTIONALTEST.equals(sonarProfile)) {
 //					builder.append(module);
 //				}
 				if (StringUtils.isNotEmpty(sonarProfile) && FUNCTIONALTEST.equals(sonarProfile)) {
 //					PomProcessor processor = Utility.getPomProcessor(rootModulePath, subModule);
 					ProjectInfo projectInfo = Utility.getProjectInfo(rootModulePath, subModule);
 					File testFolderLocation = Utility.getTestFolderLocation(projectInfo, rootModulePath, subModule);
 					String funcDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 //					builder.append(frameworkUtil.getFunctionalTestDir(appDirName));
 					builder.append(testFolderLocation.toString());
 					builder.append(funcDir);
 					builder.append(File.separatorChar);
 					builder.append(Constants.POM_NAME);
 				} else {
 				File getpomFileLocation = Utility.getPomFileLocation(rootModulePath, module);
 				builder.append(getpomFileLocation.getPath());
 				}
 
 //				builder.append(File.separatorChar); 
 //				ApplicationInfo applicationInfo = getApplicationInfo(appDirName);
 //				String workingDirectoryPath = Utility.getWorkingDirectoryPath(appDirName);
 //				String pomFileName = Utility.getPhrescoPomFromWorkingDirectory(applicationInfo, new File(workingDirectoryPath));
 //				builder.append(pomFileName);
 				File pomPath = new File(builder.toString());
 				StringBuilder sbuild = new StringBuilder();
 				if (pomPath.exists()) {
 					PomProcessor pomProcessor = new PomProcessor(pomPath);
 					String groupId = pomProcessor.getModel().getGroupId();
 					String artifactId = pomProcessor.getModel().getArtifactId();
 
 					sbuild.append(groupId);
 					sbuild.append(FrameworkConstants.COLON);
 					sbuild.append(artifactId);
 					if (!REQ_SRC.equals(sonarProfile)) {
 						sbuild.append(FrameworkConstants.COLON);
 						sbuild.append(sonarProfile);
 					}
 
 					String artifact = sbuild.toString();
 					String url = serverUrl + artifact;
 					if (isSonarAlive(url)) {
 						isSonarReportAvailable = true;
 					}
 				}
 			}
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 		return isSonarReportAvailable;
 	}
 
 	/**
 	 * Check sonar url.
 	 * 
 	 * @param sonarProfile
 	 *            the sonar profile
 	 * @param serverUrl
 	 *            the server url
 	 * @param frameworkUtil
 	 *            the framework util
 	 * @param appInfo
 	 *            the app info
 	 * @return true, if successful
 	 * @throws PhrescoException
 	 *             the phresco exception
 	 */
 	private boolean checkSonarUrl(String sonarProfile, String serverUrl, FrameworkUtil frameworkUtil, String rootModulePath, String subModule) throws PhrescoException {
 		boolean isSonarReportAvailable = false;
 		try {
 			if (StringUtils.isNotBlank(sonarProfile)) {
 				PomProcessor processor = Utility.getPomProcessor(rootModulePath, subModule);
 				// get sonar report
 				StringBuilder builder = new StringBuilder();
 //				builder.append(appDirName);
 //				builder.append(File.separatorChar);
 
 				if (StringUtils.isNotEmpty(sonarProfile) && FUNCTIONALTEST.equals(sonarProfile)) {
 					ProjectInfo projectInfo = Utility.getProjectInfo(rootModulePath, subModule);
 					File testFolderLocation = Utility.getTestFolderLocation(projectInfo, rootModulePath, subModule);
 					String funcDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 //					builder.append(frameworkUtil.getFunctionalTestDir(appDirName));
 					builder.append(testFolderLocation.toString());
 					builder.append(funcDir);
 					builder.append(File.separatorChar);
 					builder.append(Constants.POM_NAME);
 				} else {
 					File pomFile = Utility.getPomFileLocation(rootModulePath, subModule);
 					builder.append(pomFile.getPath());
 				}
 
 //				builder.append(File.separatorChar);
 				
 //				ApplicationInfo applicationInfo = getApplicationInfo(appDirName);
 //				String workingDirectoryPath = Utility.getWorkingDirectoryPath(appDirName);
 //				String pomFileName = Utility.getPhrescoPomFromWorkingDirectory(applicationInfo, new File(workingDirectoryPath));
 				
 //				builder.append(pomFileName);
 				File pomPath = new File(builder.toString());
 				StringBuilder sbuild = new StringBuilder();
 				if (pomPath.exists()) {
 					PomProcessor pomProcessor = new PomProcessor(pomPath);
 					String groupId = pomProcessor.getModel().getGroupId();
 					String artifactId = pomProcessor.getModel().getArtifactId();
 
 					sbuild.append(groupId);
 					sbuild.append(FrameworkConstants.COLON);
 					sbuild.append(artifactId);
 
 					if (!SOURCE_DIR.equals(sonarProfile)) {
 						sbuild.append(FrameworkConstants.COLON);
 						sbuild.append(sonarProfile);
 					}
 				}
 
 				String artifact = sbuild.toString();
 				String url = serverUrl + artifact;
 				if (isSonarAlive(url)) {
 					isSonarReportAvailable = true;
 				}
 			}
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 		return isSonarReportAvailable;
 	}
 
 	/**
 	 * Checks if is sonar alive.
 	 * 
 	 * @param url
 	 *            the url
 	 * @return true, if is sonar alive
 	 */
 	private boolean isSonarAlive(String url) {
 		boolean xmlResultsAvailable = false;
 		try {
 			URL sonarURL = new URL(url);
 			HttpURLConnection connection = null;
 			connection = (HttpURLConnection) sonarURL.openConnection();
 			int responseCode = connection.getResponseCode();
 			if (responseCode != 200) {
 				xmlResultsAvailable = false;
 			} else {
 				xmlResultsAvailable = true;
 			}
 		} catch (Exception e) {
 			xmlResultsAvailable = false;
 		}
 		return xmlResultsAvailable;
 	}
 
 	public  boolean isTestReportAvailable(FrameworkUtil frameworkUtil, String rootModulePath, String subModuleName)
 			throws PhrescoException {
 		boolean xmlResultsAvailable = false;
 		File file = null;
 //		StringBuilder sb = new StringBuilder();
 //		sb.append(appDirName);
 		try {
 //			String isIphone = frameworkUtil.isIphoneTagExists(appDirName);
 			PomProcessor pomProcessor = Utility.getPomProcessor(rootModulePath, subModuleName);
 			String isIphone = pomProcessor.getProperty(PHRESCO_CODE_VALIDATE_REPORT);
 			String unitDir = pomProcessor.getProperty(POM_PROP_KEY_UNITTEST_RPT_DIR);
 			ProjectInfo projectInfo = Utility.getProjectInfo(rootModulePath, subModuleName);
 			File testFolder = Utility.getTestFolderLocation(projectInfo, rootModulePath, subModuleName);
 			List<String> moduleNames = new ArrayList<String>();
 			// unit xml check
 			if (!xmlResultsAvailable) {
 				File sourceFolderLocation = Utility.getSourceFolderLocation(projectInfo, rootModulePath, subModuleName);
 				File testDir = Utility.getPomFileLocation( rootModulePath, subModuleName);
 				PomProcessor processor = new PomProcessor(new File(sourceFolderLocation + File.separator + Constants.POM_NAME));
 				Modules pomModules = processor.getPomModule();
 //				List<String> modules = null;
 				// check multimodule or not
 				if (pomModules != null && StringUtils.isEmpty(subModuleName)) {
 //					modules = FrameworkServiceUtil.getProjectModules(appDirName);
 					for (String module : pomModules.getModule()) {
 						if (StringUtils.isNotEmpty(module)) {
 							moduleNames.add(module);
 						}
 					}
 					for (String moduleName : moduleNames) {
 						File testFolderLocation = Utility.getPomFileLocation(rootModulePath, moduleName);
 						String moduleXmlPath = testFolderLocation.getParent() + File.separator +  unitDir;
 						file = new File(moduleXmlPath);
 						xmlResultsAvailable = xmlFileSearch(file, xmlResultsAvailable);
 					} 
 				} else if(StringUtils.isNotEmpty(subModuleName)) {
 					for (String module : pomModules.getModule()) {
 						if (StringUtils.isNotEmpty(module)) {
 							moduleNames.add(module);
 						}
 					}
 					for (String moduleName : moduleNames) {
 						if(moduleName.equals(subModuleName)) {
 							File testFolderLocation = Utility.getPomFileLocation(rootModulePath, moduleName);
 							String moduleXmlPath = testFolderLocation.getParent() + File.separator +  unitDir;
 							file = new File(moduleXmlPath);
 							xmlResultsAvailable = xmlFileSearch(file, xmlResultsAvailable);
 						}
 					} 
 				} else {
 					if (StringUtils.isNotEmpty(isIphone)) {
 //						String unitIphoneTechReportDir = frameworkUtil.getUnitTestReportDir(appDirName);
 						file = new File(testDir.getParent() + File.separator + unitDir);
 					} else {
 //						String unitTechReports = frameworkUtil.getUnitTestReportOptions(appDirName);
 						String unitTechReports = pomProcessor.getProperty(PHRESCO_UNIT_TEST);
 						if (StringUtils.isEmpty(unitTechReports)) {
 							file = new File(testDir.getParent() + File.separator + unitDir);
 						} else {
 							List<String> unitTestTechs = Arrays.asList(unitTechReports.split(","));
 							for (String unitTestTech : unitTestTechs) {
 								unitTechReports = pomProcessor.getProperty(POM_PROP_KEY_UNITTEST_RPT_DIR_START + unitTestTech.toLowerCase() + POM_PROP_KEY_UNITTEST_RPT_DIR_END);
 //								unitTechReports = frameworkUtil.getUnitTestReportDir(appDirName, unitTestTech);
 								if (StringUtils.isNotEmpty(unitTechReports)) {
 									file = new File(testDir.getParent() + File.separator + unitTechReports);
 									xmlResultsAvailable = xmlFileSearch(file, xmlResultsAvailable);
 								}
 							}
 						}
 					}
 					xmlResultsAvailable = xmlFileSearch(file, xmlResultsAvailable);
 				}
 			}
 
 			// functional xml check
 			if (!xmlResultsAvailable) {
 				if(moduleNames != null && StringUtils.isEmpty(subModuleName)) {
 					for (String moduleName : moduleNames) {
 						if (!xmlResultsAvailable) {
 							File moduleTestFolder = Utility.getTestFolderLocation(projectInfo, rootModulePath, moduleName);
 							PomProcessor modulePomProcessor = Utility.getPomProcessor(rootModulePath, moduleName);
 							String funcDir = modulePomProcessor.getPropertyValue(POM_PROP_KEY_FUNCTEST_RPT_DIR);
 							if (funcDir.contains(PROJECT_BASEDIR)) {
 								funcDir = funcDir.replace(PROJECT_BASEDIR, moduleTestFolder.toString());
 							}
 							file = new File(funcDir);
 							xmlResultsAvailable = xmlFileSearch(file, xmlResultsAvailable);
 						}
 					}
 				} else {
 					String funcDir = pomProcessor.getPropertyValue(POM_PROP_KEY_FUNCTEST_RPT_DIR);
 					if (funcDir.contains(PROJECT_BASEDIR)) {
 						funcDir = funcDir.replace(PROJECT_BASEDIR, testFolder.toString());
 					}
 					file = new File(funcDir);
 					xmlResultsAvailable = xmlFileSearch(file, xmlResultsAvailable);
 				}
 				
 			}
 
 			// component xml check
 			if (!xmlResultsAvailable) {
 				if(moduleNames != null && StringUtils.isEmpty(subModuleName)) {
 					for (String moduleName : moduleNames) {
 						if (!xmlResultsAvailable) {
 							File moduleTestFolder = Utility.getTestFolderLocation(projectInfo, rootModulePath, moduleName);
 							PomProcessor modulePomProcessor = Utility.getPomProcessor(rootModulePath, moduleName);
 							String componentDir = modulePomProcessor.getProperty(POM_PROP_KEY_COMPONENTTEST_RPT_DIR);
 	//						String componentDir = frameworkUtil.getComponentTestReportDir(appDirName);
 							if (StringUtils.isNotEmpty(componentDir)) {
 								file = new File(moduleTestFolder + File.separator + componentDir);
 								xmlResultsAvailable = xmlFileSearch(file, xmlResultsAvailable);
 							}
 						}
 					}
 				} else {
 					String componentDir = pomProcessor.getProperty(POM_PROP_KEY_COMPONENTTEST_RPT_DIR);
 //					String componentDir = frameworkUtil.getComponentTestReportDir(appDirName);
 					if (StringUtils.isNotEmpty(componentDir)) {
 						file = new File(testFolder + File.separator + componentDir);
 						xmlResultsAvailable = xmlFileSearch(file, xmlResultsAvailable);
 					}
 				}
 				
 			}
 
 			// performance xml check
 			if (StringUtils.isEmpty(isIphone)) {
 				if (!xmlResultsAvailable) {
					if(moduleNames != null && StringUtils.isEmpty(subModuleName)) {
 						for (String moduleName : moduleNames) {
 							if (!xmlResultsAvailable) {
 								QualityService qualityService = new QualityService();
 								String phrescoPluginInfoFilePath = FrameworkServiceUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_PERFORMANCE_TEST, Constants.PHASE_PERFORMANCE_TEST, rootModulePath, moduleName);
 								MojoProcessor mojo = new MojoProcessor(new File(phrescoPluginInfoFilePath));
 								List<String> testAgainsts = new ArrayList<String>();
 								Parameter testAgainstParameter = mojo.getParameter(Constants.PHASE_PERFORMANCE_TEST, REQ_TEST_AGAINST);
 								if (testAgainstParameter != null && TYPE_LIST.equalsIgnoreCase(testAgainstParameter.getType())) {
 									List<Value> values = testAgainstParameter.getPossibleValues().getValue();
 									for (Value value : values) {
 										testAgainsts.add(value.getKey());
 									}
 								}
 								// refactor quality service --------------
 								xmlResultsAvailable = qualityService.testResultAvail(rootModulePath, moduleName, testAgainsts, Constants.PHASE_PERFORMANCE_TEST);
 							}
 						}
 					} else {
 						QualityService qualityService = new QualityService();
 						String phrescoPluginInfoFilePath = FrameworkServiceUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_PERFORMANCE_TEST, Constants.PHASE_PERFORMANCE_TEST, rootModulePath, subModuleName);
 						MojoProcessor mojo = new MojoProcessor(new File(phrescoPluginInfoFilePath));
 						List<String> testAgainsts = new ArrayList<String>();
 						Parameter testAgainstParameter = mojo.getParameter(Constants.PHASE_PERFORMANCE_TEST, REQ_TEST_AGAINST);
 						if (testAgainstParameter != null && TYPE_LIST.equalsIgnoreCase(testAgainstParameter.getType())) {
 							List<Value> values = testAgainstParameter.getPossibleValues().getValue();
 							for (Value value : values) {
 								testAgainsts.add(value.getKey());
 							}
 						}
 						// refactor quality service --------------
 						xmlResultsAvailable = qualityService.testResultAvail(rootModulePath, subModuleName, testAgainsts, Constants.PHASE_PERFORMANCE_TEST);
 					
 					}
 				}
 			}
 
 			// load xml check
 			if (StringUtils.isEmpty(isIphone)) {
 				if (!xmlResultsAvailable) {
					if(moduleNames != null && StringUtils.isEmpty(subModuleName)) {
 						for (String moduleName : moduleNames) {
 							if (!xmlResultsAvailable) {
 								String phrescoPluginInfoFilePath = FrameworkServiceUtil.getPhrescoPluginInfoFilePath(
 										Constants.PHASE_LOAD_TEST, Constants.PHASE_LOAD_TEST, rootModulePath, moduleName);
 								if(new File(phrescoPluginInfoFilePath).exists()) {
 									MojoProcessor mojo = new MojoProcessor(new File(phrescoPluginInfoFilePath));
 									Parameter testAgainstParameter = mojo.getParameter(Constants.PHASE_LOAD_TEST, REQ_TEST_AGAINST);
 									List<String> testAgainsts = new ArrayList<String>();
 									if (testAgainstParameter != null && TYPE_LIST.equalsIgnoreCase(testAgainstParameter.getType())) {
 										List<Value> values = testAgainstParameter.getPossibleValues().getValue();
 										for (Value value : values) {
 											testAgainsts.add(value.getKey());
 										}
 									}
 									QualityService qualityService = new QualityService();
 									xmlResultsAvailable = qualityService.testResultAvail(rootModulePath, moduleName, testAgainsts, Constants.PHASE_LOAD_TEST);
 								}
 							}
 						}
 					} else {
 						String phrescoPluginInfoFilePath = FrameworkServiceUtil.getPhrescoPluginInfoFilePath(
 								Constants.PHASE_LOAD_TEST, Constants.PHASE_LOAD_TEST, rootModulePath, subModuleName);
 						if(new File(phrescoPluginInfoFilePath).exists()) {
 							MojoProcessor mojo = new MojoProcessor(new File(phrescoPluginInfoFilePath));
 							Parameter testAgainstParameter = mojo.getParameter(Constants.PHASE_LOAD_TEST, REQ_TEST_AGAINST);
 							List<String> testAgainsts = new ArrayList<String>();
 							if (testAgainstParameter != null && TYPE_LIST.equalsIgnoreCase(testAgainstParameter.getType())) {
 								List<Value> values = testAgainstParameter.getPossibleValues().getValue();
 								for (Value value : values) {
 									testAgainsts.add(value.getKey());
 								}
 							}
 							QualityService qualityService = new QualityService();
 							xmlResultsAvailable = qualityService.testResultAvail(rootModulePath, subModuleName, testAgainsts, Constants.PHASE_LOAD_TEST);
 						}
 					}
 				}
 			}
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 		return xmlResultsAvailable;
 	}
 
 	public boolean loadTestResultAvail(ApplicationInfo appInfo) throws PhrescoException {
 		boolean isResultFileAvailable = false;
 		try {
 			String baseDir = Utility.getProjectHome() + appInfo.getAppDirName();
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			List<String> testResultsTypes = new ArrayList<String>();
 			testResultsTypes.add("server");
 			testResultsTypes.add("webservice");
 			for (String testResultsType : testResultsTypes) {
 				StringBuilder sb = new StringBuilder(baseDir.toString());
 				String loadReportDir = frameworkUtil.getLoadTestReportDir(appInfo);
 				if (StringUtils.isNotEmpty(loadReportDir) && StringUtils.isNotEmpty(testResultsType)) {
 					Pattern p = Pattern.compile("dir_type");
 					Matcher matcher = p.matcher(loadReportDir);
 					loadReportDir = matcher.replaceAll(testResultsType);
 					sb.append(loadReportDir);
 				}
 				File file = new File(sb.toString());
 				File[] children = file.listFiles(new XmlNameFileFilter(FILE_EXTENSION_XML));
 				if (!ArrayUtils.isEmpty(children)) {
 					isResultFileAvailable = true;
 					break;
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 
 		return isResultFileAvailable;
 	}
 
 	public boolean performanceTestResultAvail(ApplicationInfo appInfo) throws PhrescoException {
 		boolean isResultFileAvailable = false;
 		try {
 			String baseDir = Utility.getProjectHome() + appInfo.getAppDirName();
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			List<String> testResultsTypes = new ArrayList<String>();
 			testResultsTypes.add("server");
 			testResultsTypes.add("database");
 			testResultsTypes.add("webservice");
 			for (String testResultsType : testResultsTypes) {
 				StringBuilder sb = new StringBuilder(baseDir.toString());
 				String performanceReportDir = frameworkUtil.getPerformanceTestReportDir(appInfo);
 				if (StringUtils.isNotEmpty(performanceReportDir) && StringUtils.isNotEmpty(testResultsType)) {
 					Pattern p = Pattern.compile("dir_type");
 					Matcher matcher = p.matcher(performanceReportDir);
 					performanceReportDir = matcher.replaceAll(testResultsType);
 					sb.append(performanceReportDir);
 				}
 				File file = new File(sb.toString());
 				String resultExtension = FrameworkServiceUtil
 						.getPerformanceResultFileExtension(appInfo.getAppDirName(), "");
 				if (StringUtils.isNotEmpty(resultExtension)) {
 					File[] children = file.listFiles(new XmlNameFileFilter(resultExtension));
 					if (!ArrayUtils.isEmpty(children)) {
 						isResultFileAvailable = true;
 						break;
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 
 		return isResultFileAvailable;
 	}
 
 	private boolean xmlFileSearch(File file, boolean xmlResultsAvailable) {
 		File[] children = file.listFiles(new XmlNameFileFilter(FILE_EXTENSION_XML));
 		if (children != null && children.length > 0) {
 			xmlResultsAvailable = true;
 		}
 		return xmlResultsAvailable;
 	}
 
 	/**
 	 * The Class XmlNameFileFilter.
 	 */
 	public class XmlNameFileFilter implements FilenameFilter {
 		
 		/** The filter_. */
 		private String filter_;
 
 		/**
 		 * Instantiates a new xml name file filter.
 		 *
 		 * @param filter the filter
 		 */
 		public XmlNameFileFilter(String filter) {
 			filter_ = filter;
 		}
 		
 		public boolean accept(File dir, String name) {
 			return name.endsWith(filter_);
 		}
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
