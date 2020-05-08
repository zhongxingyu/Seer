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
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonIOException;
 import com.photon.phresco.commons.FileListFilter;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.BuildInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.FrameworkConfiguration;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.api.ActionType;
 import com.photon.phresco.framework.api.ApplicationManager;
 import com.photon.phresco.framework.api.Project;
 import com.photon.phresco.framework.api.ProjectAdministrator;
 import com.photon.phresco.framework.api.ProjectRuntimeManager;
 import com.photon.phresco.framework.commons.ApplicationsUtil;
 import com.photon.phresco.framework.commons.DiagnoseUtil;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.commons.LogErrorReport;
 import com.photon.phresco.framework.commons.PBXNativeTarget;
 import com.photon.phresco.framework.commons.QualityUtil;
 import com.photon.phresco.framework.model.DependantParameters;
 import com.photon.phresco.framework.model.PerformanceDetails;
 import com.photon.phresco.framework.model.PerformanceTestResult;
 import com.photon.phresco.framework.model.PropertyInfo;
 import com.photon.phresco.framework.model.SettingsInfo;
 import com.photon.phresco.framework.model.TestCase;
 import com.photon.phresco.framework.model.TestCaseError;
 import com.photon.phresco.framework.model.TestCaseFailure;
 import com.photon.phresco.framework.model.TestResult;
 import com.photon.phresco.framework.model.TestSuite;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.HubConfiguration;
 import com.photon.phresco.util.IosSdkUtil;
 import com.photon.phresco.util.IosSdkUtil.MacSdkType;
 import com.photon.phresco.util.NodeConfiguration;
 import com.photon.phresco.util.TechnologyTypes;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Model.Modules;
 import com.phresco.pom.util.PomProcessor;
 
 public class Quality extends DynamicParameterAction implements Constants {
 
     private static final long serialVersionUID = -2040671011555139339L;
     private static final Logger S_LOGGER = Logger.getLogger(Quality.class);
     private static Boolean s_debugEnabled  =S_LOGGER.isDebugEnabled();
     
     private List<SettingsInfo> serverSettings = null;
     private String showSettings = "";
     private String testSuite = "";
     private String failures = "";
     private String errs = "";
     private String tests = "";
     private String setting = "";
     private String projectCode = "";
     private String testType = "";
     private String testResultFile = "";
 	private String techReport = "";
     private String testModule = "";
 	private String showError = "";
     private String hideLog = "";
     private String showDebug = "";
     private String jarLocation = "";
     private String testAgainst = "";
     private String jarName = "";
     private File systemPath = null;
     private String resolution = ""; 
     
 	private List<String> configName = null;
 	private List<String> buildInfoEnvs = null;
     
 	private String settingType = "";
     private String settingName = "";
     private String caption = "";
     private List<String> testResultFiles = new ArrayList<String>();
     private List<TestSuite> testSuites = null;
     private List<String> testSuiteNames = null;
     private boolean validated = false;
 	private String testResultsType = "";
 
 	//Below variables gets the value of performance test Url, Context and TestName
 	private String resultJson = "";
 	private PerformanceDetails performanceDetails = null;
 	private List<String> name = null;
     private List<String> context = null;
     private List<String> contextType = null;
     private List<String> contextPostData = null;
     private List<String> encodingType = null;
     private String testName = "";
     
     //Below variables get the value of PerformanceTest for Db
     private List<String> dbPerName = null;
 	private String Database = "";
 	private List<String> queryType = null;
     private List<String> query = null;
 
     //Thread group details
     private String noOfUsers = "";
     private String rampUpPeriod = "";
     private String loopCount = "";
 
     //jmeterTestAgainst radio button value
     private String jmeterTestAgainst = "";
 
     private boolean resultFileAvailable = false;
 
     // android performance tag name
     private String testResultDeviceId = "";
     private Map<String, String> deviceNames = null;
     private String serialNumber = "";
     
     // iphone unit test
     private String sdk = "";
 	private String target = "";
 	private String fromPage = "";
 	
 	//perfromance DB
 	private String hostValue = "";
     private String portNo = "";
     private String pwd = "";
     private String dbType = "";
     private String schema = "";
     private String uname = "";
     private String dbUrl = "";
     private String driver = "";
     
     private String showGraphFor = "";
 	
     // report generation 
     private String reportName = "";
     private String reoportLocation = "";
     private String reportDataType = "";
     private String sonarUrl = "";
     
     // download report
 	private InputStream fileInputStream;
 	private String fileName = "";
 	private String reportFileName = "";
 	
 	//ios test type iosTestType
 	private String iosTestType = "";
 	
 	private String projectModule = "";
 	
     boolean connectionAlive = false;
     boolean updateCache;
 	
 	private static Map<String, Map<String, NodeList>> testSuiteMap = Collections.synchronizedMap(new HashMap<String, Map<String, NodeList>>(8));
 	
 	public String unit() {
 	    if (s_debugEnabled) {
 	        S_LOGGER.debug("Entering Method Quality.unit()");
 	    }
 	    
 	    try {
 	        ApplicationInfo appInfo = getApplicationInfo();
 	        FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 	        setReqAttribute(PATH, frameworkUtil.getUnitTestDir(appInfo));
             setReqAttribute(REQ_APPINFO, appInfo);
             setProjModulesInReq();
             // get unit test report options
             setUnitReportOptions();
 	    } catch (Exception e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.unit()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_QUALITY_UNIT_LOAD));
         }
 	    
 	    return APP_UNIT_TEST;
 	}
 	
 	private void setUnitReportOptions() throws PhrescoException {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.setUnitReportOptions");
         }
 		try {
 	        ApplicationInfo appInfo = getApplicationInfo();
 	        FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 	        String unitTestReportOptions = frameworkUtil.getUnitTestReportOptions(appInfo);
 	        if (StringUtils.isNotEmpty(unitTestReportOptions)) {
 	        	List<String> asList = Arrays.asList(unitTestReportOptions.split(","));
 	        	setReqAttribute(REQ_UNIT_TEST_REPORT_OPTIONS, asList);
 	        }
 		} catch (Exception e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.setUnitReportOptions()" + FrameworkUtil.getStackTraceAsString(e));
             }
 			throw new PhrescoException(e);
 		}
 	}
 	
 	public String fetchUnitTestSuites() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.fetchUnitTestSuites");
         }
         
         try {
             ApplicationInfo appInfo = getApplicationInfo();
             String testResultPath = getUnitTestResultPath(appInfo, null);
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             String testSuitePath = "";
             if (StringUtils.isNotEmpty(getTechReport())) {
             	testSuitePath = frameworkUtil.getUnitTestSuitePath(appInfo, getTechReport());
 		    } else {
 		    	testSuitePath = frameworkUtil.getUnitTestSuitePath(appInfo);
 		    }
             List<String> resultTestSuiteNames = getTestSuiteNames(testResultPath, testSuitePath);
             if (CollectionUtils.isEmpty(resultTestSuiteNames)) {
                 setValidated(true);
                 setShowError(getText(ERROR_UNIT_TEST));
                 return SUCCESS;
             }
             setTestSuiteNames(resultTestSuiteNames);
         } catch (Exception e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.fetchUnitTestSuites()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_QUALITY_UNIT_TESTSUITES));
         }
         
         return SUCCESS;
     }
 	
 	private String getUnitTestResultPath(ApplicationInfo appInfo, String testResultFile) throws PhrescoException, JAXBException, IOException, PhrescoPomException {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.getUnitTestResultPath()");
         }
         
         StringBuilder sb = new StringBuilder(getApplicationHome());
         if (StringUtils.isNotEmpty(getProjectModule())) {
             sb.append(File.separatorChar);
             sb.append(getProjectModule());
         }
         // TODO Need to change this
         StringBuilder tempsb = new StringBuilder(sb);
         if (JAVASCRIPT.equals(getTechReport())) {
             tempsb.append(UNIT_TEST_QUNIT_REPORT_DIR);
             File file = new File(tempsb.toString());
             if (file.isDirectory() && file.list().length > 0) {
                 sb.append(UNIT_TEST_QUNIT_REPORT_DIR);
             } else {
                 sb.append(UNIT_TEST_JASMINE_REPORT_DIR);
             }
         } else {
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             if (StringUtils.isNotEmpty(getTechReport())) {
             	sb.append(frameworkUtil.getUnitTestReportDir(appInfo, getTechReport()));
             } else {
             	sb.append(frameworkUtil.getUnitTestReportDir(appInfo));
             }
         }
         return sb.toString();
     }
     
 	public String showUnitTestPopUp() {
 	    if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.showUnitTestPopUp()");
         }
 	    
 	    try {
 	    	ApplicationInfo appInfo = getApplicationInfo();
             removeSessionAttribute(appInfo.getId() + PHASE_UNIT_TEST + SESSION_WATCHER_MAP);
             setProjModulesInReq();
             Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>(8);
 
             MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_UNIT_TEST)));
             List<Parameter> parameters = getMojoParameters(mojo, PHASE_UNIT_TEST);
 
             setPossibleValuesInReq(mojo, appInfo, parameters, watcherMap);
             setSessionAttribute(appInfo.getId() + PHASE_UNIT_TEST + SESSION_WATCHER_MAP, watcherMap);
             setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
             setReqAttribute(REQ_GOAL, PHASE_UNIT_TEST);
             setReqAttribute(REQ_PHASE, PHASE_UNIT_TEST);
     	    setProjModulesInReq();
 	    } catch (PhrescoException e) {
 	        if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.fetchUnitTestSuites()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_QUALITY_UNIT_PARAMS));
 	    }
 	    
 	    return SUCCESS;
 	}
 
     private void setProjModulesInReq() throws PhrescoException {
         List<String> projectModules = getProjectModules(getApplicationInfo().getAppDirName());
         setReqAttribute(REQ_PROJECT_MODULES, projectModules);
     }
 
 	public String runUnitTest() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.runUnitTest()");
         }
         
         try {
             ApplicationInfo appInfo = getApplicationInfo();
             StringBuilder workingDirectory = new StringBuilder(getAppDirectoryPath(appInfo));
             if (StringUtils.isNotEmpty(getProjectModule())) {
                 workingDirectory.append(File.separator);
                 workingDirectory.append(getProjectModule());
             }
             MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_UNIT_TEST)));
             persistValuesToXml(mojo, PHASE_UNIT_TEST);
             ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
             BufferedReader reader = applicationManager.performAction(getProjectInfo(), ActionType.UNIT_TEST, null, workingDirectory.toString());
             setSessionAttribute(getAppId() + UNIT, reader);
             setReqAttribute(REQ_APP_ID, getAppId());
             setReqAttribute(REQ_ACTION_TYPE, UNIT);
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.runUnitTest()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_QUALITY_UNIT_RUN));
         }
         
         return APP_ENVIRONMENT_READER;
     }
 	
 	public String functional() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.functional()");
         }
         
         try {
             ApplicationInfo appInfo = getApplicationInfo();
             setProjModulesInReq();
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             setReqAttribute(PATH, frameworkUtil.getFunctionalTestDir(appInfo));
             setReqAttribute(REQ_FUNCTEST_SELENIUM_TOOL, frameworkUtil.getSeleniumToolType(appInfo));
             setReqAttribute(REQ_APPINFO, appInfo);
         } catch (Exception e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.functional()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_QUALITY_FUNCTIONAL_LOAD));
         }
 
         return APP_FUNCTIONAL_TEST;
     }
 	
 	public String fetchFunctionalTestSuites() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.fetchFunctionalTestSuites()");
         }
         
         try {
             ApplicationInfo appInfo = getApplicationInfo();
             String testResultPath = getFunctionalTestResultPath(appInfo, null);
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             String testSuitePath = frameworkUtil.getFunctionalTestSuitePath(appInfo);
             List<String> resultTestSuiteNames = getTestSuiteNames(testResultPath, testSuitePath);
             if (CollectionUtils.isEmpty(resultTestSuiteNames)) {
                 setValidated(true);
                 setShowError(getText(ERROR_FUNCTIONAL_TEST));
                 return SUCCESS;
             }
             setTestSuiteNames(resultTestSuiteNames);
         } catch (Exception e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.fetchFunctionalTestSuites()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_QUALITY_FUNCTIONAL_TESTSUITES));
         }
         
         return SUCCESS;
     }
 	
 	private String getFunctionalTestResultPath(ApplicationInfo appInfo, String testResultFile) throws PhrescoException {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.getFunctionalTestResultPath()");
         }
         
         StringBuilder sb = new StringBuilder();
         try {
             sb.append(getApplicationHome());
             if (StringUtils.isNotEmpty(getProjectModule())) {
                 sb.append(File.separatorChar);
                 sb.append(getProjectModule());
             }
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             sb.append(frameworkUtil.getFunctionalTestReportDir(appInfo));
         } catch (PhrescoException e) {
             throw new PhrescoException(e);
         } catch (PhrescoPomException e) {
             throw new PhrescoException(e);
         }
         
         return sb.toString();
     }
 	
 	public String showFunctionalTestPopUp() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.showFunctionalTestPopUp()");
         }
         try {
             ApplicationInfo appInfo = getApplicationInfo();
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             String seleniumToolType = frameworkUtil.getSeleniumToolType(appInfo);
             removeSessionAttribute(appInfo.getId() + PHASE_FUNCTIONAL_TEST + SESSION_WATCHER_MAP);
             setProjModulesInReq();
             Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>(8);
 
             MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_FUNCTIONAL_TEST)));
             List<Parameter> parameters = getMojoParameters(mojo, PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType);
 
             setPossibleValuesInReq(mojo, appInfo, parameters, watcherMap);
             setSessionAttribute(appInfo.getId() + PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType + SESSION_WATCHER_MAP, watcherMap);
             setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
             setReqAttribute(REQ_PHASE, PHASE_FUNCTIONAL_TEST);
             setReqAttribute(REQ_GOAL, PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType);
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.showFunctionalTestPopUp()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_PARAMS));
         } catch (PhrescoPomException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.showFunctionalTestPopUp()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_QUALITY_FUNCTIONAL_PARAMS));
         }
         
         return SUCCESS;
     }
 	
 	public String runFunctionalTest() {
 	    if (s_debugEnabled) {
 	        S_LOGGER.debug("Entering Method Quality.runFunctionalTest()");
 	    }
 	    
 	    try {
 	        ApplicationInfo appInfo = getApplicationInfo();
 	        StringBuilder workingDirectory = new StringBuilder(getAppDirectoryPath(appInfo));
 	        if (StringUtils.isNotEmpty(getProjectModule())) {
 	            workingDirectory.append(File.separator);
 	            workingDirectory.append(getProjectModule());
 	        }
 	        MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_FUNCTIONAL_TEST)));
 	        FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             String seleniumToolType = frameworkUtil.getSeleniumToolType(appInfo);
 	        persistValuesToXml(mojo, PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType);
 	        ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 	        BufferedReader reader = applicationManager.performAction(getProjectInfo(), ActionType.FUNCTIONAL_TEST, null, workingDirectory.toString());
 	        setSessionAttribute(getAppId() + FUNCTIONAL, reader);
 	        setReqAttribute(REQ_APP_ID, getAppId());
 	        setReqAttribute(REQ_ACTION_TYPE, FUNCTIONAL);
 	    } catch (PhrescoException e) {
 	    	if (s_debugEnabled) {
 	    		S_LOGGER.error("Entered into catch block of Quality.runFunctionalTest()"+ FrameworkUtil.getStackTraceAsString(e));
 	    	}
 	        return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_RUN));
 	    } catch (PhrescoPomException e) {
 	        if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.runFunctionalTest()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_QUALITY_FUNCTIONAL_RUN));
         }
 
 	    return APP_ENVIRONMENT_READER;
 	}
 	
 	public String checkForHub() {
 	    if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.checkForHub()");
         }
 	    
 	    BufferedReader reader = null;
 	    try {
 	        FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 	        String functionalTestDir = frameworkUtil.getFunctionalTestDir(getApplicationInfo());
 	        StringBuilder sb = new StringBuilder(getApplicationHome());
 	        sb.append(functionalTestDir)
 	        .append(File.separator)
 	        .append("hubconfig.json");
 	        File hubConfigFile = new File(sb.toString());
 	        Gson gson = new Gson();
             reader = new BufferedReader(new FileReader(hubConfigFile));
             HubConfiguration hubConfig = gson.fromJson(reader, HubConfiguration.class);
             if (hubConfig != null) {
                 String host = hubConfig.getHost();
                 int port = hubConfig.getPort();
                 setConnectionAlive(DiagnoseUtil.isConnectionAlive(HTTP_PROTOCOL, host, port));
             }
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.checkForHub()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_HUB_CONNECTION));
         } catch (PhrescoPomException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.checkForHub()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_QUALITY_FUNCTIONAL_HUB_CONNECTION));
         } catch (FileNotFoundException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.checkForHub()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_QUALITY_FUNCTIONAL_HUB_CONNECTION));
         } finally {
             Utility.closeStream(reader);
         }
 	    
 	    return SUCCESS;
 	}
 	
 	public String showStartedHubLog() {
 	    if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.showStartedHubLog()");
         }
 	    
 	    try {
 	        StringBuilder sb = new StringBuilder(getApplicationHome());
 	        sb.append(File.separator)
 	        .append(DO_NOT_CHECKIN_DIR)
 	        .append(File.separator)
 	        .append(LOG_DIR)
 	        .append(File.separator)
 	        .append(HUB_LOG);
 	        BufferedReader reader = new BufferedReader(new FileReader(sb.toString()));
 	        setSessionAttribute(getAppId() + START_HUB, reader);
 	        setReqAttribute(REQ_APP_ID, getAppId());
 	        setReqAttribute(REQ_ACTION_TYPE, START_HUB);
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.showStartedHubLog()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_HUB_LOG));
         } catch (FileNotFoundException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.showStartedHubLog()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_QUALITY_FUNCTIONAL_HUB_LOG));
         }
 	    
 	    return APP_ENVIRONMENT_READER;
 	}
 	
 	public String showStartHubPopUp() throws PhrescoPomException, FileNotFoundException {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.showStartHubPopUp()");
         }
         
         try {
         	MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_START_HUB)));
             List<Parameter> parameters = getMojoParameters(mojo, PHASE_START_HUB);
             setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
             setReqAttribute(REQ_GOAL, PHASE_START_HUB);
         } catch (PhrescoException e) {
         	if (s_debugEnabled) {
 	    		S_LOGGER.error("Entered into catch block of Quality.showStartHubPopUp()"+ FrameworkUtil.getStackTraceAsString(e));
 	    	}
             return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_START_HUB_PARAMS));
         }
         
         return SUCCESS;
     }
 
 	public String startHub() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method Quality.startHub()");
 		}
 
 		try {
 			ApplicationInfo appInfo = getApplicationInfo();
 			MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_START_HUB)));
 			persistValuesToXml(mojo, PHASE_START_HUB);
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ProjectInfo projectInfo = getProjectInfo();
 			String workingDirectory = getAppDirectoryPath(appInfo);
 			BufferedReader reader = applicationManager.performAction(projectInfo, ActionType.START_HUB, null, workingDirectory);
 			setSessionAttribute(getAppId() + START_HUB, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, START_HUB);
 		} catch (PhrescoException e) {
 			if (s_debugEnabled) {
 	    		S_LOGGER.error("Entered into catch block of Quality.startHub()"+ FrameworkUtil.getStackTraceAsString(e));
 	    	}
 			return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_START_HUB));
 		}
 
 		return APP_ENVIRONMENT_READER;
 	}
 	
 	public String stopHub() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.stopHub()");
         }
 
         try {
             ApplicationInfo appInfo = getApplicationInfo();
             ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
             ProjectInfo projectInfo = getProjectInfo();
             String workingDirectory = getAppDirectoryPath(appInfo);
             applicationManager.performAction(projectInfo, ActionType.STOP_HUB, null, workingDirectory);
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.stopHub()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_STOP_HUB));
         }
 
         return SUCCESS;
     }
 	
 	public String checkForNode() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.checkForNode()");
         }
         
         BufferedReader reader = null;
         try {
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             String functionalTestDir = frameworkUtil.getFunctionalTestDir(getApplicationInfo());
             StringBuilder sb = new StringBuilder(getApplicationHome());
             sb.append(functionalTestDir)
 			.append(File.separator)
 			.append(Constants.NODE_CONFIG_JSON);
             File hubConfigFile = new File(sb.toString());
             Gson gson = new Gson();
             reader = new BufferedReader(new FileReader(hubConfigFile));
             NodeConfiguration nodeConfiguration = gson.fromJson(reader, NodeConfiguration.class);
             if (nodeConfiguration != null) {
                 String host = nodeConfiguration.getConfiguration().getHost();
                 int port = nodeConfiguration.getConfiguration().getPort();
                 setConnectionAlive(DiagnoseUtil.isConnectionAlive(HTTP_PROTOCOL, host, port));
             }
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.checkForNode()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_NODE_CONNECTION));
         } catch (PhrescoPomException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.checkForNode()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_QUALITY_FUNCTIONAL_NODE_CONNECTION));
         } catch (FileNotFoundException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.checkForNode()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_QUALITY_FUNCTIONAL_NODE_CONNECTION));
         } finally {
             Utility.closeStream(reader);
         }
         
         return SUCCESS;
     }
 	
 	public String showStartedNodeLog() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.showStartedNodeLog()");
         }
         
         try {
             StringBuilder sb = new StringBuilder(getApplicationHome());
             sb.append(File.separator)
             .append(DO_NOT_CHECKIN_DIR)
             .append(File.separator)
             .append(LOG_DIR)
             .append(File.separator)
             .append(Constants.NODE_LOG);
             BufferedReader reader = new BufferedReader(new FileReader(sb.toString()));
             setSessionAttribute(getAppId() + START_NODE, reader);
             setReqAttribute(REQ_APP_ID, getAppId());
             setReqAttribute(REQ_ACTION_TYPE, START_NODE);
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.showStartedNodeLog()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_NODE_LOG));
         } catch (FileNotFoundException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.showStartedNodeLog()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_QUALITY_FUNCTIONAL_NODE_LOG));
         }
         
         return APP_ENVIRONMENT_READER;
     }
 	
 	public String showStartNodePopUp() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.showStartNodePopUp()");
         }
         
         try {
         	MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_START_NODE)));
             List<Parameter> parameters = getMojoParameters(mojo, PHASE_START_NODE);
             setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
             setReqAttribute(REQ_GOAL, PHASE_START_NODE);
         } catch (PhrescoException e) {
         	if (s_debugEnabled) {
 	    		S_LOGGER.error("Entered into catch block of Quality.showStartNodePopUp()"+ FrameworkUtil.getStackTraceAsString(e));
 	    	}
             return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_START_NODE_PARAMS));
         }
         
         return SUCCESS;
     }
 	
 	public String startNode() {
 		if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.startNode()");
         }
 		
         try {
         	ApplicationInfo appInfo = getApplicationInfo();
 			MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_START_NODE)));
 			persistValuesToXml(mojo, PHASE_START_NODE);
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ProjectInfo projectInfo = getProjectInfo();
 			String workingDirectory = getAppDirectoryPath(appInfo);
 			BufferedReader reader = applicationManager.performAction(projectInfo, ActionType.START_NODE, null, workingDirectory);
 			setSessionAttribute(getAppId() + START_NODE, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, START_NODE);
         } catch (PhrescoException e) {
         	if (s_debugEnabled) {
 	    		S_LOGGER.error("Entered into catch block of Quality.startNode()"+ FrameworkUtil.getStackTraceAsString(e));
 	    	}
         	return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_START_NODE));
         }
         
         return APP_ENVIRONMENT_READER;
     }
 	
 	public String stopNode() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.stopNode()");
         }
 
         try {
             ApplicationInfo appInfo = getApplicationInfo();
             ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
             ProjectInfo projectInfo = getProjectInfo();
             String workingDirectory = getAppDirectoryPath(appInfo);
             applicationManager.performAction(projectInfo, ActionType.STOP_NODE, null, workingDirectory);
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.stopNode()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_STOP_NODE));
         }
 
         return SUCCESS;
     }
 	
     public String performance() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.performance()");
         }
 
         try {
             ApplicationInfo appInfo = getApplicationInfo();
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             getHttpRequest().setAttribute(PATH, frameworkUtil.getPerformanceTestDir(appInfo));
             setReqAttribute(REQ_APPINFO, appInfo);
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.performance()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_STOP_NODE));
         } catch (PhrescoPomException e) {
             // TODO Auto-generated catch block
         }
 
         return APP_PERFORMANCE_TEST;
     }
     
     public String showPerformanceTestPopUp() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.showPerformanceTestPopUp()");
         }
         
         try {
         	ApplicationInfo appInfo = getApplicationInfo();
         	removeSessionAttribute(appInfo.getId() + PHASE_PERFORMANCE_TEST + SESSION_WATCHER_MAP);
         	Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>(8);
 
         	MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_PERFORMANCE_TEST)));
         	List<Parameter> parameters = getMojoParameters(mojo, PHASE_PERFORMANCE_TEST);
         	setPossibleValuesInReq(mojo, appInfo, parameters, watcherMap);
         	setSessionAttribute(appInfo.getId() + PHASE_PERFORMANCE_TEST + SESSION_WATCHER_MAP, watcherMap);
         	setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
         	setReqAttribute(REQ_GOAL, PHASE_PERFORMANCE_TEST);
         	setReqAttribute(REQ_PHASE, PHASE_PERFORMANCE_TEST);
         } catch (PhrescoException e) {
         	if (s_debugEnabled) {
         		S_LOGGER.error("Entered into catch block of Quality.showFunctionalTestPopUp()" + FrameworkUtil.getStackTraceAsString(e));
         	}
         	return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_PARAMS));
         }
 
         return SUCCESS;
     }
 	
 	private List<String> getTestSuiteNames(String testResultPath, String testSuitePath) throws FileNotFoundException, ParserConfigurationException,
             SAXException, IOException, TransformerException, PhrescoException, PhrescoPomException {
         String testSuitesMapKey = getAppId() + getTestType() + getProjectModule() + getTechReport();
         Map<String, NodeList> testResultNameMap = testSuiteMap.get(testSuitesMapKey);
         List<String> resultTestSuiteNames = null;
         if (MapUtils.isEmpty(testResultNameMap) || updateCache) { //  || StringUtils.isNotEmpty(fromPage) when the user clicks on close button, newly generated report shoud be displayed
             File[] resultFiles = getTestResultFiles(testResultPath);
             if (!ArrayUtils.isEmpty(resultFiles)) {
                 QualityUtil.sortResultFile(resultFiles);
                 updateCache(resultFiles, testSuitePath);
             }
             testResultNameMap = testSuiteMap.get(testSuitesMapKey);
         }
         if (testResultNameMap != null) {
         	resultTestSuiteNames = new ArrayList<String>(testResultNameMap.keySet());
         }
         return resultTestSuiteNames;
     }
 	
     private String getTestResultPath(ApplicationInfo appInfo, String testResultFile) throws ParserConfigurationException, 
             SAXException, IOException, TransformerException, PhrescoException, JAXBException, PhrescoPomException {
     	S_LOGGER.debug("Entering Method Quality.getTestDocument(Project project, String testResultFile)");
     	S_LOGGER.debug("getTestDocument() ProjectInfo = "+appInfo);
     	S_LOGGER.debug("getTestDocument() TestResultFile = "+testResultFile);
     	
         FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
         StringBuilder sb = new StringBuilder(getApplicationHome());
 
         if (FUNCTIONAL.equals(getTestType())) {
         	if (StringUtils.isNotEmpty(projectModule)) {
                 sb.append(File.separatorChar);
                 sb.append(projectModule);
     		}
             sb.append(frameworkUtil.getFunctionalTestReportDir(appInfo));
         } else if (UNIT.equals(getTestType())) {
         	if (StringUtils.isNotEmpty(getProjectModule())) {
                 sb.append(File.separatorChar);
                 sb.append(getProjectModule());
     		}
         	
         	StringBuilder tempsb = new StringBuilder(sb);
         	if ("javascript".equals(getTechReport())) {
         		tempsb.append(UNIT_TEST_QUNIT_REPORT_DIR);
         		File file = new File(tempsb.toString());
                 if (file.isDirectory() && file.list().length > 0) {
                 	sb.append(UNIT_TEST_QUNIT_REPORT_DIR);
                 } else {
                 	sb.append(UNIT_TEST_JASMINE_REPORT_DIR);
                 }
         	} else {
                 if (StringUtils.isNotEmpty(getTechReport())) {
                 	sb.append(frameworkUtil.getUnitTestReportDir(appInfo, getTechReport()));
 		        } else {
 		         	sb.append(frameworkUtil.getUnitTestReportDir(appInfo));
 		        }
         	}
         } else if (LOAD.equals(getTestType())) {
         	sb.append(frameworkUtil.getLoadTestReportDir(appInfo));
             sb.append(File.separator);
             sb.append(testResultFile);
         } else if (PERFORMACE.equals(getTestType())) {
             String performanceTestReportDir = frameworkUtil.getPerformanceTestReportDir(appInfo);
             Pattern p = Pattern.compile(TEST_DIRECTORY);
             Matcher matcher = p.matcher(performanceTestReportDir);
             if (StringUtils.isNotEmpty(performanceTestReportDir) && matcher.find()) {
                 performanceTestReportDir = matcher.replaceAll(getTestResultsType());
             }
             sb.append(performanceTestReportDir);
             sb.append(File.separator);
             sb.append(testResultFile);
         }
         return sb.toString();
     }
 
 	private Document getDocument(File resultFile) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
 		InputStream fis = null;
         DocumentBuilder builder = null;
         try {
         	S_LOGGER.debug("Report path" + resultFile.getAbsolutePath());
 //            fis = new FileInputStream(getTestResultFile(sb.toString()));
         	fis = new FileInputStream(resultFile);
             DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
             domFactory.setNamespaceAware(false);
             builder = domFactory.newDocumentBuilder();
             Document doc = builder.parse(fis);
             return doc;
 
         } finally {
             if(fis != null) {
                 try {
                     fis.close();
                 } catch (IOException e) {
                 	S_LOGGER.error("Entered into catch block of Quality.getTestDocument()"+ e);
                 }
             }
         }
 	}
 	
 	private File[] getTestResultFiles(String path) {
 		File testDir = new File(path);
         if (testDir.isDirectory()) {
             FilenameFilter filter = new FileListFilter("", "xml");
             return testDir.listFiles(filter);
         }
         return null;
 	}
 
 	public String fillTestResultFiles() throws PhrescoException, ParserConfigurationException, SAXException, IOException, TransformerException, JAXBException, PhrescoPomException {
     	S_LOGGER.debug("Entering Method Quality.getTestSuites(Project project)");
 
         try {
         	Map<String, NodeList> mapTestResultName = null;
         	mapTestResultName = testSuiteMap.get(projectCode + testType + projectModule + techReport);
         	
 //        	String resultPath = "";
         	
     		String testResultPath = getTestResultPath(getApplicationInfo(), null);
             /*if (StringUtils.isNotEmpty(projectModule)) {
                 StringBuilder sb = new StringBuilder();
                 sb.append(Utility.getProjectHome());
                 sb.append(project.getProjectInfo().getCode());
                 sb.append(File.separatorChar);
                 sb.append(projectModule);
 				sb.append(resultPath);
                 testResultPath = sb.toString();
             } else {
             	testResultPath = getTestResultPath(project, null);
             }*/
             
         	if (MapUtils.isEmpty(mapTestResultName) || StringUtils.isNotEmpty(fromPage)) {
         		File[] resultFiles = getTestResultFiles(testResultPath);
         		if (resultFiles != null) {
         			QualityUtil.sortResultFile(resultFiles);
 //        			updateCache(resultFiles);
         		} else {
         			setValidated(true);
         			if(UNIT.equals(testType)) {
         				setShowError(getText(ERROR_UNIT_TEST));
         			} else {
         				setShowError(getText(ERROR_FUNCTIONAL_TEST));
         			}
         			return SUCCESS;
         		}
 
             	String testSuitesMapKey = projectCode + testType + projectModule + techReport;
             	mapTestResultName = testSuiteMap.get(testSuitesMapKey);
         	} 
         	
         	
         	List<String> resultFileNames = new ArrayList<String>(mapTestResultName.keySet());
         	if (CollectionUtils.isEmpty(resultFileNames)) {
         		setValidated(true);
     			setShowError(getText(ERROR_UNIT_TEST));
     			return SUCCESS;
         	}
         	
         	setTestType(testType);
         	setTestResultFiles(resultFileNames);
         	return SUCCESS;
         } catch (PhrescoException e) {
         	S_LOGGER.error("Entered into catch block of Quality.getTestSuites()"+ e);
         }
 		return null;
     }
 	
 	public String fillTestSuites() {
 		S_LOGGER.debug("Entering Method Quality.fillTestSuites");
 		try {
 	    	String testSuitesMapKey = getAppId() + getTestType() + getProjectModule() + getTechReport();
 	    	Map<String, NodeList> mapTestResultName = testSuiteMap.get(testSuitesMapKey);
 	    	
 			String testResultPath = getTestResultPath(getApplicationInfo(), null);
 	    	if (MapUtils.isEmpty(mapTestResultName) || StringUtils.isNotEmpty(fromPage)) {
 	    		File[] resultFiles = getTestResultFiles(testResultPath);
 	    		if (resultFiles != null) {
 	    			QualityUtil.sortResultFile(resultFiles);
 //	    			updateCache(resultFiles);
 	    		} else {
 	    			setValidated(true);
 	    			if(UNIT.equals(testType)) {
 	    				setShowError(getText(ERROR_UNIT_TEST));
 	    			} else {
 	    				setShowError(getText(ERROR_FUNCTIONAL_TEST));
 	    			}
 	    			return SUCCESS;
 	    		}
 	
 	        	mapTestResultName = testSuiteMap.get(testSuitesMapKey);
 	    	} 
 	    	
 	    	List<String> resultTestSuiteNames = new ArrayList<String>(mapTestResultName.keySet());
 	    	if (CollectionUtils.isEmpty(resultTestSuiteNames)) {
 	    		setValidated(true);
 	    		if(UNIT.equals(testType)){
 	    			setShowError(getText(ERROR_UNIT_TEST));
 	    		} else {
 	    			setShowError(getText(ERROR_FUNCTIONAL_TEST));
 	    		}
 				return SUCCESS;
 	    	}
 	    	
 	    	setTestType(testType);
 	    	setTestSuiteNames(resultTestSuiteNames);
 		} catch(SAXParseException e) {
     		setValidated(true);
 			setShowError(getText(ERROR_PARSE_EXCEPTION));
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Quality.fillTestSuites()");
 		}
 		return SUCCESS;
 	}
     
     private void updateCache(File[] resultFiles, String testSuitePath) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException, PhrescoException, PhrescoPomException {
 		Map<String, NodeList> mapTestSuites = new HashMap<String, NodeList>(10);
     	for (File resultFile : resultFiles) {
 			try {
 				Document doc = getDocument(resultFile);
 				NodeList testSuiteNodeList = evaluateTestSuite(doc, testSuitePath);
 				if (testSuiteNodeList.getLength() > 0) {
 					List<TestSuite> testSuites = getTestSuite(testSuiteNodeList);
 					for (TestSuite testSuite : testSuites) {
 						mapTestSuites.put(testSuite.getName(), testSuiteNodeList);
 					}
 				}
 			} catch (PhrescoException e) {
 				// continue the loop to filter the testResultFile
             	S_LOGGER.error("Entered into catch block of Quality.updateCache()"+ e);
 			} catch (XPathExpressionException e) {
 				// continue the loop to filter the testResultFile
             	S_LOGGER.error("Entered into catch block of Quality.updateCache()"+ e);
 			} catch (SAXException e) {
 				// continue the loop to filter the testResultFile
             	S_LOGGER.error("Entered into catch block of Quality.updateCache()"+ e);
 			}
 		}
 	    String testSuitesKey = getAppId() + getTestType() + getProjectModule() + getTechReport();
 	    
 		testSuiteMap.put(testSuitesKey, mapTestSuites);
     }
     
     private NodeList evaluateTestSuite(Document doc, String testSuitePath) throws PhrescoException, XPathExpressionException, PhrescoPomException {
     	XPath xpath = XPathFactory.newInstance().newXPath();
 		XPathExpression xPathExpression = xpath.compile(testSuitePath);
 		NodeList testSuiteNode = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);
 		return testSuiteNode;
     }
 
     private List<TestSuite> getTestSuite(NodeList nodelist) throws TransformerException, PhrescoException {
     	S_LOGGER.debug("Entering Method Quality.getTestSuite(NodeList nodelist)");
 
     	List<TestSuite> testSuites = new ArrayList<TestSuite>(2);
 		TestSuite testSuite = null;
 		for (int i = 0; i < nodelist.getLength(); i++) {
 		    testSuite =  new TestSuite();
 		    Node node = nodelist.item(i);
 		    NamedNodeMap nameNodeMap = node.getAttributes();
 
 		    for (int k = 0; k < nameNodeMap.getLength(); k++) {
 		        Node attribute = nameNodeMap.item(k);
 		        String attributeName = attribute.getNodeName();
 		        String attributeValue = attribute.getNodeValue();
 		        if (ATTR_ASSERTIONS.equals(attributeName)) {
 		            testSuite.setAssertions(attributeValue);
 		        } else if (ATTR_ERRORS.equals(attributeName)) {
 		            testSuite.setErrors(Float.parseFloat(attributeValue));
 		        } else if (ATTR_FAILURES.equals(attributeName)) {
 		            testSuite.setFailures(Float.parseFloat(attributeValue));
 		        } else if (ATTR_FILE.equals(attributeName)) {
 		            testSuite.setFile(attributeValue);
 		        } else if (ATTR_NAME.equals(attributeName)) {
 		            testSuite.setName(attributeValue);
 		        } else if (ATTR_TESTS.equals(attributeName)) {
 		            testSuite.setTests(Float.parseFloat(attributeValue));
 		        } else if (ATTR_TIME.equals(attributeName)) {
 		            testSuite.setTime(attributeValue);
 		        }
 		    }
 		    testSuites.add(testSuite);
 		}
 		
 		return testSuites;
     }
 
     private List<TestCase> getTestCases(NodeList testSuites, String testSuitePath, String testCasePath) throws TransformerException, PhrescoException, PhrescoPomException {
     	S_LOGGER.debug("Entering Method Quality.getTestCases(Document doc, String testSuiteName)");
         
     	try {
             S_LOGGER.debug("Test suite path " + testSuitePath);
             S_LOGGER.debug("Test suite path " + testCasePath);
             
             StringBuilder sb = new StringBuilder(); //testsuites/testsuite[@name='yyy']/testcase
             sb.append(testSuitePath);
             sb.append(NAME_FILTER_PREFIX);
             sb.append(getTestSuite());
             sb.append(NAME_FILTER_SUFIX);
             sb.append(testCasePath);
 
             XPath xpath = XPathFactory.newInstance().newXPath();
             NodeList nodeList = (NodeList) xpath.evaluate(sb.toString(), testSuites.item(0).getParentNode(), XPathConstants.NODESET);
             
             // For tehnologies like php and drupal duoe to plugin change xml testcase path modified
             if (nodeList.getLength() == 0) {
                 StringBuilder sbMulti = new StringBuilder();
                 sbMulti.append(testSuitePath);
                 sbMulti.append(NAME_FILTER_PREFIX);
                 sbMulti.append(getTestSuite());
                 sbMulti.append(NAME_FILTER_SUFIX);
                 sbMulti.append(XPATH_TESTSUTE_TESTCASE);
                 nodeList = (NodeList) xpath.evaluate(sbMulti.toString(), testSuites.item(0).getParentNode(), XPathConstants.NODESET);
             }
             
             // For technology sharepoint
             if (nodeList.getLength() == 0) {
                 StringBuilder sbMulti = new StringBuilder(); //testsuites/testsuite[@name='yyy']/testcase
                 sbMulti.append(XPATH_MULTIPLE_TESTSUITE);
                 sbMulti.append(NAME_FILTER_PREFIX);
                 sbMulti.append(getTestSuite());
                 sbMulti.append(NAME_FILTER_SUFIX);
                 sbMulti.append(testCasePath);
                 nodeList = (NodeList) xpath.evaluate(sbMulti.toString(), testSuites.item(0).getParentNode(), XPathConstants.NODESET);
             }
 
             List<TestCase> testCases = new ArrayList<TestCase>();
             
         	StringBuilder screenShotDir = new StringBuilder(getApplicationHome());
         	screenShotDir.append(File.separator);
         	FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
         	screenShotDir.append(frameworkUtil.getFunctionalTestReportDir(getApplicationInfo()));
         	screenShotDir.append(File.separator);
         	screenShotDir.append(SCREENSHOT_DIR);
         	screenShotDir.append(File.separator);
         	
         	int failureTestCases = 0;
             int errorTestCases = 0;
             for (int i = 0; i < nodeList.getLength(); i++) {
                 Node node = nodeList.item(i);
                 NodeList childNodes = node.getChildNodes();
                 NamedNodeMap nameNodeMap = node.getAttributes();
                 TestCase testCase = new TestCase();
                 for (int k = 0; k < nameNodeMap.getLength(); k++) {
                     Node attribute = nameNodeMap.item(k);
                     String attributeName = attribute.getNodeName();
                     String attributeValue = attribute.getNodeValue();
                     if (ATTR_NAME.equals(attributeName)) {
                         testCase.setName(attributeValue);
                     } else if (ATTR_CLASS.equals(attributeName) || ATTR_CLASSNAME.equals(attributeName)) {
                         testCase.setTestClass(attributeValue);
                     } else if (ATTR_FILE.equals(attributeName)) {
                         testCase.setFile(attributeValue);
                     } else if (ATTR_LINE.equals(attributeName)) {
                         testCase.setLine(Float.parseFloat(attributeValue));
                     } else if (ATTR_ASSERTIONS.equals(attributeName)) {
                         testCase.setAssertions(Float.parseFloat(attributeValue));
                     } else if (ATTR_TIME.equals(attributeName)) {
                         testCase.setTime(attributeValue);
                     }
                 }
                 
                 if (childNodes != null && childNodes.getLength() > 0) {
                     for (int j = 0; j < childNodes.getLength(); j++) {
                         Node childNode = childNodes.item(j);
                         if (ELEMENT_FAILURE.equals(childNode.getNodeName())) {
                         	failureTestCases++;
                             TestCaseFailure failure = getFailure(childNode);
                             if (failure != null) {
                             	File file = new File(screenShotDir.toString() + testCase.getName() + DOT + IMG_PNG_TYPE);
                             	if (file.exists()) {
                             		failure.setHasFailureImg(true);
                             	}
                                 testCase.setTestCaseFailure(failure);
                             } 
                         }
 
                         if (ELEMENT_ERROR.equals(childNode.getNodeName())) {
                         	errorTestCases++;
                             TestCaseError error = getError(childNode);
                             if (error != null) {
                             	File file = new File(screenShotDir.toString() + testCase.getName() + DOT + IMG_PNG_TYPE);
                             	if (file.exists()) {
                             		error.setHasErrorImg(true);
                             	}
                                 testCase.setTestCaseError(error);
                             }
                         }
                     }
                 }
                 testCases.add(testCase);
             }
             setReqAttribute(REQ_TESTSUITE_FAILURES, failureTestCases + "");
             setReqAttribute(REQ_TESTSUITE_ERRORS, errorTestCases + "");
             setReqAttribute(REQ_TESTSUITE_TESTS, nodeList.getLength() + "");
 			
             return testCases;
         } catch (PhrescoException e) {
         	S_LOGGER.error("Entered into catch block of Quality.getTestCases()"+ e);
             throw e;
         } catch (XPathExpressionException e) {
         	S_LOGGER.error("Entered into XPathExpressionException catch block of Quality.getTestCases()"+ e);
             throw new PhrescoException(e);
 		}
     }
     
     private static TestCaseFailure getFailure(Node failureNode) throws TransformerException {
            S_LOGGER.debug("Entering Method Quality.getFailure(Node failureNode)");
            S_LOGGER.debug("getFailure() NodeName = "+failureNode.getNodeName());
         TestCaseFailure failure = new TestCaseFailure();
 
         try {
             failure.setDescription(failureNode.getTextContent());
             failure.setFailureType(REQ_TITLE_EXCEPTION);
             NamedNodeMap nameNodeMap = failureNode.getAttributes();
 
             if (nameNodeMap != null && nameNodeMap.getLength() > 0) {
                 for (int k = 0; k < nameNodeMap.getLength(); k++){
                     Node attribute = nameNodeMap.item(k);
                     String attributeName = attribute.getNodeName();
                     String attributeValue = attribute.getNodeValue();
 
                     if (ATTR_TYPE.equals(attributeName)) {
                         failure.setFailureType(attributeValue);
                     }
                 }
             }
         } catch (Exception e) {
                S_LOGGER.error("Entered into catch block of Quality.getFailure()"+ e);
         }
         return failure;
     }
 
     private static TestCaseError getError(Node errorNode) throws TransformerException {
            S_LOGGER.debug("Entering Method Quality.getError(Node errorNode)");
            S_LOGGER.debug("getError() Node = "+errorNode.getNodeName());
         TestCaseError tcError = new TestCaseError();
         try {
             tcError.setDescription(errorNode.getTextContent());
             tcError.setErrorType(REQ_TITLE_ERROR);
             NamedNodeMap nameNodeMap = errorNode.getAttributes();
             if (nameNodeMap != null && nameNodeMap.getLength() > 0) {
                 for (int k = 0; k < nameNodeMap.getLength(); k++){
                     Node attribute = nameNodeMap.item(k);
                     String attributeName = attribute.getNodeName();
                     String attributeValue = attribute.getNodeValue();
 
                     if (ATTR_TYPE.equals(attributeName)) {
                         tcError.setErrorType(attributeValue);
                     }
                 }
             }
         } catch (Exception e) {
                S_LOGGER.error("Entered into catch block of Quality.getError()"+ e);
         }
         return tcError;
     }
 
     private List<TestResult> getLoadTestResult(ApplicationInfo appInfo, String testResultFile) throws TransformerException, PhrescoException, ParserConfigurationException, SAXException, IOException {
            S_LOGGER.debug("Entering Method Quality.getLoadTestResult(Project project, String testResultFile)");
            S_LOGGER.debug("getTestResult() ProjectInfo = " + appInfo);
            S_LOGGER.debug("getTestResult() TestResultFile = " + testResultFile);
         List<TestResult> testResults = new ArrayList<TestResult>(2);
         try {
         	String testResultPath = getTestResultPath(appInfo, testResultFile);
             Document doc = getDocument(new File(testResultPath)); 
             NodeList nodeList = org.apache.xpath.XPathAPI.selectNodeList(doc, XPATH_TEST_RESULT);
 
             TestResult testResult = null;
 
             for (int i = 0; i < nodeList.getLength(); i++) {
                 testResult =  new TestResult();
                 Node node = nodeList.item(i);
                 //	            NodeList childNodes = node.getChildNodes();
                 NamedNodeMap nameNodeMap = node.getAttributes();
 
                 for (int k = 0; k < nameNodeMap.getLength(); k++) {
                     Node attribute = nameNodeMap.item(k);
                     String attributeName = attribute.getNodeName();
                     String attributeValue = attribute.getNodeValue();
 
                     if (ATTR_JM_TIME.equals(attributeName)) {
                         testResult.setTime(Integer.parseInt(attributeValue));
                     } else if (ATTR_JM_LATENCY_TIME.equals(attributeName)) {
                         testResult.setLatencyTime(Integer.parseInt(attributeValue));
                     } else if (ATTR_JM_TIMESTAMP.equals(attributeName)) {
                         Date date = new Date(Long.parseLong(attributeValue));
                         DateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT);
                         String strDate = format.format(date);
                         testResult.setTimeStamp(strDate);
                     } else if (ATTR_JM_SUCCESS_FLAG.equals(attributeName)) {
                         testResult.setSuccess(Boolean.parseBoolean(attributeValue));
                     } else if (ATTR_JM_LABEL.equals(attributeName)) {
                         testResult.setLabel(attributeValue);
                     } else if (ATTR_JM_THREAD_NAME.equals(attributeName)) {
                         testResult.setThreadName(attributeValue);
                     }
                 }
                 testResults.add(testResult);
             }
         } catch (Exception e) {
                S_LOGGER.error("Entered into catch block of Quality.getLoadTestResult()"+ e);
         }
         return testResults;
     }
 
     public String testType() {
     	S_LOGGER.debug("Entering Method Quality.testType()");
         
     	try {
     	    ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
     	    ApplicationInfo appInfo = getApplicationInfo();
             String testType = getHttpRequest().getParameter(REQ_TEST_TYPE);
             String techId = appInfo.getTechInfo().getId();
             
             // Show warning message for Android technology in quality page when build is not available
             //TODO:Need to handle
 //            if (TechnologyTypes.ANDROIDS.contains(techId)) {
 //            	int buildSize = administrator.getBuildInfos(project).size();
 //                getHttpRequest().setAttribute(REQ_BUILD_WARNING, buildSize == 0);
 //            } else {
 //            	getHttpRequest().setAttribute(REQ_BUILD_WARNING, false);
 //            }
             
             S_LOGGER.debug("Test type() test type " + testType);
             if (testType != null && (APP_UNIT_TEST.equals(testType) || APP_FUNCTIONAL_TEST.equals(testType))) {
                 try {
                 	S_LOGGER.debug("Test type() test type unit  and Functional test");
                     FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
                     if (APP_UNIT_TEST.equals(testType)) {
                         getHttpRequest().setAttribute(PATH, 
                                 frameworkUtil.getUnitTestDir(getApplicationInfo()));
                     } else if (APP_FUNCTIONAL_TEST.equals(testType) && !TechnologyTypes.IPHONE_HYBRID.equals(techId)) {
 //                        ActionType actionType = ActionType.STOP_SELENIUM_SERVER;
                         StringBuilder builder = new StringBuilder(Utility.getProjectHome());
                         builder.append(appInfo.getAppDirName());
 //                        String funcitonalTestDir = frameworkUtil.getFuncitonalTestDir(techId);
 //                        builder.append(funcitonalTestDir);
 //                        actionType.setWorkingDirectory(builder.toString());
                         ProjectRuntimeManager runtimeManager = PhrescoFrameworkFactory.getProjectRuntimeManager();
 //                        runtimeManager.performAction(project, actionType, null, null);
 //                        getHttpRequest().setAttribute(PATH, 
 //                                frameworkUtil.getFuncitonalTestDir(techId));
                     }
 
                     getHttpRequest().setAttribute(REQ_FROM_PAGE, fromPage);
                     List<String> projectModules = getProjectModules(projectCode);
                     if (CollectionUtils.isNotEmpty(projectModules)) {
                     	getHttpRequest().setAttribute(REQ_PROJECT_MODULES, projectModules);
                     }
                     
                 } catch (Exception e) {
                     getHttpRequest().setAttribute(REQ_ERROR_TESTSUITE, getText(ERROR_FUNCTIONAL_TEST));
                 }
                 getHttpRequest().setAttribute(REQ_APP_INFO, appInfo);
                 getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
                 getHttpRequest().setAttribute(REQ_TEST_TYPE, testType);
                 List<String> projectModules = getProjectModules();
                 getHttpRequest().setAttribute(REQ_PROJECT_MODULES, projectModules);
                 return testType;
             }
 
             if (testType != null && APP_LOAD_TEST.equals(testType)) {
                    S_LOGGER.debug("Test type() test type load test");
                 FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
                 StringBuilder sb = new StringBuilder();
                 sb.append(Utility.getProjectHome());
                 sb.append(appInfo.getAppDirName());
                 getHttpRequest().setAttribute(PATH,	frameworkUtil.getLoadTestReportDir(appInfo));
                 sb.append(frameworkUtil.getLoadTestReportDir(appInfo));
                    S_LOGGER.debug("test type load  test Report directory " + sb.toString());
                 File file = new File(sb.toString());
                 File[] children = file.listFiles(new XmlNameFileFilter(FILE_EXTENSION_XML));
                 
                 if(children != null) {
                 	QualityUtil.sortResultFile(children);
                     getHttpRequest().setAttribute(REQ_JMETER_REPORT_FILES, children);
                 } else {
                     getHttpRequest().setAttribute(REQ_ERROR_TESTSUITE, getText(ERROR_LOAD_TEST));
                 }
                 getHttpRequest().setAttribute(REQ_TEST_TYPE, testType);
                 getHttpRequest().setAttribute(REQ_APP_INFO, appInfo);
                 getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
                 return testType;
             }
 
             if (testType != null && APP_PERFORMANCE_TEST.equals(testType)) {
                    S_LOGGER.debug("Test type() test type performance test");
                 FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 //                getHttpRequest().setAttribute(PATH,	frameworkUtil.getPerformanceTestDir(techId));
                 getHttpRequest().setAttribute(REQ_TEST_TYPE, testType);
                 getHttpRequest().setAttribute(REQ_APP_INFO, appInfo);
                 getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
                 return testType;
             }
 
         } catch (Exception e) {
                S_LOGGER.error("Entered into catch block of Quality.testType()"+ e);
             getHttpRequest().setAttribute(REQ_ERROR_TESTSUITE, ERROR_TEST_SUITE);
         }
 
         return APP_UNIT_TEST;
     }
 
     public String performanceTesting() {
         
            S_LOGGER.debug("Entering Method Quality.performance()");
         
         String environment = getHttpRequest().getParameter(REQ_ENVIRONMENTS);
         getHttpRequest().setAttribute(REQ_TEST_TYPE_SELECTED, REQ_TEST_PERFORMANCE);
         BufferedReader reader = null;
         Writer writer = null;
         try {
         	ActionType actionType = null;
             String jmeterTestAgainst = getHttpRequest().getParameter("jmeterTestAgainst");
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             Project project = administrator.getProject(projectCode);
             String techId = project.getApplicationInfo().getTechInfo().getVersion();
             SettingsInfo selectedSettings = null;
             String requestHeaders = getHttpRequest().getParameter("requestHeaders");
             //To add the request header to the jmx
             Map<String, String> headersMap = new HashMap<String, String>(2);
             if (StringUtils.isNotEmpty(requestHeaders)) {
             	String[] headers = requestHeaders.split("#SEP#");
             	if (!ArrayUtils.isEmpty(headers)) {
             		for (String header : headers) {
 						String[] nameAndValue = header.split("#VSEP#");
 						headersMap.put(nameAndValue[0], nameAndValue[1]);
 					}
             	}
             }
             
             Map<String, String> settingsInfoMap = new HashMap<String, String>(2);
             if (TechnologyTypes.ANDROIDS.contains(techId)) {
                 String[] connectedDevices = getHttpRequest().getParameterValues(ANDROID_DEVICE);
                 String devices = FrameworkUtil.convertToCommaDelimited(connectedDevices);
                 settingsInfoMap.put(ANDROID_DEVICE_LIST, devices);
 //                actionType = ActionType.ANDROID_TEST_COMMAND;
 //                if (SHOW_ERROR.equals(showError)) {
 //                	actionType.setShowError(true);
 //            	} else {
 //            		actionType.setShowError(false);
 //            	}
 //                
 //                if (HIDE_LOG.equals(hideLog)) {
 //                	actionType.setHideLog(true);
 //            	} else {
 //            		actionType.setHideLog(false);
 //            	}
 //                
 //                if (SHOW_DEBUG.equals(showDebug)) {
 //                	actionType.setShowDebug(true);
 //            	} else {
 //            		actionType.setShowDebug(false);
 //            	}
                 
                 
                    S_LOGGER.debug("Load method ANDROIDS type settingsInfoMap value " + settingsInfoMap);
                    S_LOGGER.debug("Performance test method ANDROIDS type settingsInfoMap value " + settingsInfoMap);
                
             } else {
 //            	actionType = ActionType.TEST;
                 if (Constants.SETTINGS_TEMPLATE_SERVER.equals(jmeterTestAgainst)) {
                     String serverSetting = getHttpRequest().getParameter(Constants.SETTINGS_TEMPLATE_SERVER);
                     selectedSettings = administrator.getSettingsInfo(serverSetting, Constants.SETTINGS_TEMPLATE_SERVER, project.getApplicationInfo().getCode(), environment);
                 }
 
                 if (Constants.SETTINGS_TEMPLATE_WEBSERVICE.equals(jmeterTestAgainst)) {
                     String webServiceSetting = getHttpRequest().getParameter(Constants.SETTINGS_TEMPLATE_WEBSERVICE);
                     selectedSettings = administrator.getSettingsInfo(webServiceSetting, Constants.SETTINGS_TEMPLATE_WEBSERVICE, project.getApplicationInfo().getCode(), environment);
                 }
                 
                 if (Constants.SETTINGS_TEMPLATE_DB.equals(jmeterTestAgainst)) {
                     String dbSetting = getHttpRequest().getParameter(Constants.SETTINGS_TEMPLATE_DB);
                     selectedSettings = administrator.getSettingsInfo(dbSetting, Constants.SETTINGS_TEMPLATE_DB, project.getApplicationInfo().getCode(), environment);
                    
                    PropertyInfo host = selectedSettings.getPropertyInfo(Constants.DB_HOST);
                    PropertyInfo port = selectedSettings.getPropertyInfo(Constants.DB_PORT);
                    PropertyInfo password = selectedSettings.getPropertyInfo(Constants.DB_PASSWORD);
                    PropertyInfo type = selectedSettings.getPropertyInfo(Constants.DB_TYPE);
                    PropertyInfo dbname = selectedSettings.getPropertyInfo(Constants.DB_NAME);
                    PropertyInfo username = selectedSettings.getPropertyInfo(Constants.DB_USERNAME);
                    
                    hostValue = host.getValue();
                    portNo = port.getValue();
                    pwd = password.getValue();
                    dbType = type.getValue();
                    schema = dbname.getValue();
                    uname = username.getValue();
 
                    if(dbType.contains("mysql")){
                     	dbUrl = "jdbc:mysql://" +hostValue +":" +portNo+"/"+schema;
                     	driver = "com.mysql.jdbc.Driver";
                     }
                     if(dbType.contains("oracle")){
                     	dbUrl = "jdbc:oracle:thin:@"+hostValue +":" +portNo+":"+schema;
                     	driver = "oracle.jdbc.driver.OracleDriver";
                     }
                     if(dbType.contains("mssql")){
                     	dbUrl = "jdbc:sqlserver://"+hostValue +":" +portNo+";databaseName="+schema;
                     	driver = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
                     }
                     if(dbType.contains("db2")){
                     	dbUrl = "jdbc:db2://"+hostValue +":" +portNo+"/"+schema;
                     	driver = "com.ibm.db2.jcc.DB2Driver";
                     }
                     
                     
                 }
                 settingsInfoMap.put(TEST_PARAM, TEST_PARAM_VALUE);
                    S_LOGGER.debug("Performance test method settingsInfoMap value " + settingsInfoMap);
             }
 
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             StringBuilder builder = new StringBuilder(Utility.getProjectHome());
             builder.append(project.getApplicationInfo().getCode());
 //            String performanceTestDir = frameworkUtil.getPerformanceTestDir(techId);
             
 //               S_LOGGER.debug("Performance test directory path from framework util " + frameworkUtil.getPerformanceTestDir(techId));
 //               builder.append(performanceTestDir);
                S_LOGGER.debug("Performance test directory path " + builder.toString());
             if (!TechnologyTypes.ANDROIDS.contains(techId)) {
             	if ("WebService".equals(jmeterTestAgainst)) {
             		jmeterTestAgainst = "webservices";
             	}
 	            builder.append(jmeterTestAgainst.toLowerCase());
 	            QualityUtil.changeTestName(builder.toString(), testName);
 	            QualityUtil.adaptTestConfig(builder.toString(), selectedSettings);
 	            if (Constants.SETTINGS_TEMPLATE_DB.equals(jmeterTestAgainst)) {
 	            	QualityUtil.adaptDBPerformanceJmx(builder.toString(), dbPerName, Database, queryType, query, Integer.parseInt(noOfUsers), Integer.parseInt(rampUpPeriod), Integer.parseInt(loopCount), dbUrl, driver, uname, pwd);
 	            } else {
 	            	QualityUtil.adaptPerformanceJmx(builder.toString(), name, context, contextType, contextPostData, encodingType, Integer.parseInt(noOfUsers), Integer.parseInt(rampUpPeriod), Integer.parseInt(loopCount), headersMap);
 	            }
 	            
 	            String filepath = builder.toString() + File.separator + testName + ".json";
 	            
             }
 //            actionType.setWorkingDirectory(builder.toString());
             ProjectRuntimeManager runtimeManager = PhrescoFrameworkFactory.getProjectRuntimeManager();
             reader = runtimeManager.performAction(project, actionType, settingsInfoMap, null);
             getHttpSession().setAttribute(projectCode + PERFORMACE, reader);
             getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
             getHttpRequest().setAttribute(REQ_TEST_TYPE,PERFORMACE );
         } catch(Exception e) {
             if (e instanceof FileNotFoundException) {
                 getHttpRequest().setAttribute(REQ_ERROR_TESTSUITE, getText(ERROR_PERFORMANCE_TEST));
     			StringReader sb = new StringReader("Test is not available for this project");
     			reader = new BufferedReader(sb);
                 getHttpSession().setAttribute(projectCode + PERFORMACE, reader);
                 getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
                 getHttpRequest().setAttribute(REQ_TEST_TYPE,PERFORMACE );
             }
             S_LOGGER.error("Entered into catch block of Quality.performance()"+ e);
             if (writer != null) {
             	try {
 					writer.close();
 				} catch (IOException e1) {
 					S_LOGGER.error("Entered into catch block of Quality.performance() Finally "+ e);
 				}
             }
             new LogErrorReport(e, "Quality Performance test");
         }
         getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
         return APP_ENVIRONMENT_READER;
     }
     
     public String performanceTest() throws PhrescoException {
     	try {
     		FileOutputStream fop = null;
     		ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
     		ProjectInfo projectInfo = getProjectInfo();
     		ApplicationInfo applicationInfo = getApplicationInfo();
     		MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_PERFORMANCE_TEST)));
     		persistValuesToXml(mojo, PHASE_PERFORMANCE_TEST);
 
     		//To get maven build arguments
     		List<Parameter> parameters = getMojoParameters(mojo, PHASE_PERFORMANCE_TEST);
     		List<String> buildArgCmds = getMavenArgCommands(parameters);
     		String workingDirectory = getAppDirectoryPath(applicationInfo);
 
     		StringBuilder filepath = new StringBuilder(Utility.getProjectHome());
     		filepath.append(applicationInfo.getAppDirName()).append(File.separator).append(TEST_SLASH_PERFORMANCE).append(getTestAgainst())
     		.append(File.separator).append(getTestName()).append(DOT_JSON);
     		/*String className = getHttpRequest().getParameter(REQ_OBJECT_CLASS);//get the bean class
     		ClassLoader classLoader = Quality.class.getClassLoader();*/
     		File file = new File(filepath.toString());
 			fop = new FileOutputStream(file);
 			if (!file.exists()) {
 				file.createNewFile();
 			}
 			byte[] contentInBytes = getResultJson().getBytes();
 			 
 			fop.write(contentInBytes);
 			fop.flush();
 			fop.close();	
     		BufferedReader reader = applicationManager.performAction(projectInfo, ActionType.PERFORMANCE_TEST, buildArgCmds, workingDirectory);
     		setSessionAttribute(getAppId() + PERFORMANCE_TEST, reader);
     		setReqAttribute(REQ_APP_ID, getAppId());
     		setReqAttribute(REQ_ACTION_TYPE, PERFORMANCE_TEST);
     	} catch (JsonIOException e) {
     		throw new PhrescoException(e);
     	} catch (FileNotFoundException e) {
     		throw new PhrescoException(e);
     	} catch (IOException e) {
     		throw new PhrescoException(e); 
     	}
 
     	return SUCCESS;
     }
     
     public String load() throws JAXBException, IOException, PhrescoPomException {
 	    
     	if (s_debugEnabled) {
 	        S_LOGGER.debug("Entering Method Quality.load()");
 	    } 
 	    
     	try {
     		 ApplicationInfo appInfo = getApplicationInfo();	
     		 FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
              setReqAttribute(PATH, frameworkUtil.getLoadTestDir(appInfo));
              File file = new File(Utility.getProjectHome() + appInfo.getAppDirName()+ frameworkUtil.getLoadTestReportDir(appInfo));
              File[] children = file.listFiles(new XmlNameFileFilter(FILE_EXTENSION_XML));
              if(children != null) {
              	QualityUtil.sortResultFile(children);
                  getHttpRequest().setAttribute(REQ_JMETER_REPORT_FILES, children);
              } else {
                  getHttpRequest().setAttribute(REQ_ERROR_TESTSUITE, getText(ERROR_LOAD_TEST));
              }
 
              getHttpRequest().setAttribute(REQ_APP_INFO, appInfo);
     	} catch(Exception e){
         }
     	
     	return "load";
     }
     
     public String showLoadTestPopup() throws PhrescoException{
     	
     	try {
     		ApplicationInfo appInfo = getApplicationInfo();
             removeSessionAttribute(appInfo.getId() + PHASE_LOAD_TEST + SESSION_WATCHER_MAP);
             setProjModulesInReq();
             Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>(8);
 
             MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_LOAD_TEST)));
             List<Parameter> parameters = getMojoParameters(mojo, PHASE_LOAD_TEST);
 
             setPossibleValuesInReq(mojo, appInfo, parameters, watcherMap);
             setSessionAttribute(appInfo.getId() + PHASE_LOAD_TEST + SESSION_WATCHER_MAP, watcherMap);
             setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
             setReqAttribute(REQ_GOAL, PHASE_LOAD_TEST);
             setReqAttribute(REQ_PHASE, PHASE_LOAD_TEST);
     	} catch(PhrescoException e) {
     		if (s_debugEnabled) {
 	    		S_LOGGER.error("Entered into catch block of Quality.showLoadTestPopup()"+ FrameworkUtil.getStackTraceAsString(e));
 	    	}
 			return showErrorPopup(e, getText(EXCEPTION_QUALITY_LOAD_PARAMS));
     	}
     	
     	return "Success";
    }
     
     public String runLoadTest() {
     	if (s_debugEnabled) {
 	        S_LOGGER.debug("Entering Method Quality.runLoadTest()");
 	    } 
     	try {
     		ApplicationInfo appInfo = getApplicationInfo();
 	        StringBuilder workingDirectory = new StringBuilder(getAppDirectoryPath(appInfo));
 	        MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_LOAD_TEST)));
             persistValuesToXml(mojo, PHASE_LOAD_TEST);
             ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
             BufferedReader reader = applicationManager.performAction(getProjectInfo(), ActionType.LOAD_TEST, null, workingDirectory.toString());
             setSessionAttribute(getAppId() + LOAD, reader);
             setReqAttribute(REQ_APP_ID, getAppId());
             setReqAttribute(REQ_ACTION_TYPE, LOAD);
     	} catch(PhrescoException e) {
     		if (s_debugEnabled) {
 	    		S_LOGGER.error("Entered into catch block of Quality.runLoadTest()"+ FrameworkUtil.getStackTraceAsString(e));
 	    	}
 			return showErrorPopup(e, getText(EXCEPTION_QUALITY_LOAD_RUN));
     	}
     	return APP_ENVIRONMENT_READER;
     }
     
     public String load1() {
         
            S_LOGGER.debug("Entering Method Quality.load()");
         
         getHttpRequest().setAttribute(REQ_TEST_TYPE_SELECTED, REQ_TEST_LOAD);
         try{
         	ActionType actionType = null;
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             Project project = administrator.getProject(projectCode);
             String techId = project.getApplicationInfo().getTechInfo().getVersion();
             List<SettingsInfo> serverSettings = null;
             String requestHeaders = getHttpRequest().getParameter("requestHeaders");
             //To add the request header to the jmx
             Map<String, String> headersMap = new HashMap<String, String>(2);
             if (StringUtils.isNotEmpty(requestHeaders)) {
             	String[] headers = requestHeaders.split("#SEP#");
             	if (!ArrayUtils.isEmpty(headers)) {
             		for (String header : headers) {
 						String[] nameAndValue = header.split("#VSEP#");
 						headersMap.put(nameAndValue[0], nameAndValue[1]);
 					}
             	}
             }
             
             Map<String, String> settingsInfoMap = new HashMap<String, String>(2);
             if (TechnologyTypes.ANDROIDS.contains(techId)) {
                 String device = getHttpRequest().getParameter(REQ_ANDROID_DEVICE);
                 settingsInfoMap.put(DEPLOY_ANDROID_DEVICE_MODE, device); //TODO: Need to be changed
                 settingsInfoMap.put(DEPLOY_ANDROID_EMULATOR_AVD, REQ_ANDROID_DEFAULT);
 //                actionType = ActionType.ANDROID_TEST_COMMAND;
                 
                    S_LOGGER.debug("Load method ANDROIDS type settingsInfoMap value " + settingsInfoMap);
                 
             } else {
 //            	actionType = ActionType.TEST;
             	String environment = getHttpRequest().getParameter(REQ_ENVIRONMENTS);
             	String type = getHttpRequest().getParameter("jmeterTestAgainst");
                 if(serverSettings == null) {
                     serverSettings = administrator.getSettingsInfos(type, projectCode, environment);
                 }
                 settingsInfoMap.put(TEST_PARAM, TEST_PARAM_VALUE);
                    S_LOGGER.debug("Load method settingsInfoMap value " + settingsInfoMap);
             }
 
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             StringBuilder builder = new StringBuilder(Utility.getProjectHome());
             builder.append(project.getApplicationInfo().getCode());
             /*String loadTestDirPath = frameworkUtil.getLoadTestDir(techId);*/
              /*  S_LOGGER.debug("Load test directory path " + loadTestDirPath + "Test Name " + testName);
             
             builder.append(loadTestDirPath);*/
             QualityUtil.changeTestName(builder.toString(), testName);
             for (SettingsInfo serverSetting : serverSettings) {
             	QualityUtil.adaptTestConfig(builder.toString(), serverSetting);
 			}
             QualityUtil.adaptLoadJmx(builder.toString(), Integer.parseInt(noOfUsers), Integer.parseInt(rampUpPeriod), Integer.parseInt(loopCount), headersMap);
 //            actionType.setWorkingDirectory(builder.toString());
             ProjectRuntimeManager runtimeManager = PhrescoFrameworkFactory.getProjectRuntimeManager();
             BufferedReader reader = runtimeManager.performAction(project, actionType, settingsInfoMap, null);
             
             getHttpSession().setAttribute(projectCode + LOAD, reader);
             getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
             getHttpRequest().setAttribute(REQ_TEST_TYPE, LOAD);
 
         } catch(Exception e) {
             if (e instanceof FileNotFoundException) {
                 getHttpRequest().setAttribute(REQ_ERROR_TESTSUITE, getText(ERROR_LOAD_TEST));
             }
                S_LOGGER.error("Entered into catch block of Quality.load()"+ e);
             
             new LogErrorReport(e, "Quality Load test");
         }
         getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
         return APP_ENVIRONMENT_READER;
     }
     
     public String loadTestResult() {
         
            S_LOGGER.debug("Entering Method Quality.loadTestResult()");
         
         try {
             String testResultFile = getHttpRequest().getParameter(REQ_TEST_RESULT_FILE);
             List<TestResult> testResults = getLoadTestResult(getApplicationInfo(), testResultFile);
             getHttpRequest().setAttribute(REQ_TEST_RESULT, testResults);
             Gson gson = new Gson();
             StringBuilder jSon = new StringBuilder();
             StringBuilder data = new StringBuilder();
             jSon.append(GRAPH_JSON);
             data.append(SQUARE_OPEN);
             for (TestResult testResult : testResults) {
                 jSon.append(gson.toJson(testResult));
                 data.append(SQUARE_OPEN);
                 data.append(testResults.indexOf(testResult));
                 data.append(COMMA);
                 data.append(testResult.getTime());
                 data.append(SQUARE_CLOSE);
                 if(testResults.indexOf(testResult) < testResults.size() - 1) {
                     jSon.append(COMMA);
                     data.append(COMMA);
                 }
             }
             jSon.append(SQUARE_CLOSE);
             jSon.append(SEMI_COLON);
             data.append(SQUARE_CLOSE);
             data.append(SEMI_COLON);
             StringBuilder script = new StringBuilder();
             script.append(SCRIPT_START);
             script.append(jSon.toString());
             script.append(GRAPH_DATA);
             script.append(data.toString());
             script.append(GRAPH_VOLUME_DATA);
             script.append(data.toString());
             script.append(GRAPH_SUMMARY_DATA);
             script.append(data.toString());
             //script.append("var flagData = [[3, 'Login as u1']];");
             script.append("var flagData = '';");
             script.append(SCRIPT_END);
             
                S_LOGGER.debug("Test result java script constructed for load test" + script.toString());
             
             getHttpSession().setAttribute(SESSION_GRAPH_SCRIPT, script.toString());
 
         } catch (Exception e) {
                S_LOGGER.error("Entered into catch block of Quality.loadTestResult()"+ e);
         }
 
         getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
         return "loadTestResult";
     }
 
     public String performanceTestResultAvail() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.performanceTestResultAvail()");
         }
 
         try {
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             List<String> testResultsTypes = new ArrayList<String>();
             testResultsTypes.add("server");
             testResultsTypes.add("database");
             testResultsTypes.add("webservices");
             for (String testResultsType: testResultsTypes) {
                 StringBuilder sb = new StringBuilder(getApplicationHome());
                 String performanceReportDir = frameworkUtil.getPerformanceTestReportDir(getApplicationInfo());
                 if (StringUtils.isNotEmpty(performanceReportDir) && StringUtils.isNotEmpty(testResultsType)) {
                     Pattern p = Pattern.compile("dir_type");
                     Matcher matcher = p.matcher(performanceReportDir);
                     performanceReportDir = matcher.replaceAll(testResultsType);
                     sb.append(performanceReportDir); 
                 }
                 File file = new File(sb.toString());
                 File[] children = file.listFiles(new XmlNameFileFilter(FILE_EXTENSION_XML));
                 if (!ArrayUtils.isEmpty(children)) {
                     setResultFileAvailable(true);
                     break;
                 }
             }
         } catch(Exception e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.performanceTestResultAvail()"+ FrameworkUtil.getStackTraceAsString(e));
             }
         }
 
         return SUCCESS;
     }
 	
     public String fetchPerformanceTestResultFiles() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.fetchPerformanceTestResultFiles()");
         }
 
         try {
             StringBuilder sb = new StringBuilder(getApplicationHome());
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             String performanceReportDir = frameworkUtil.getPerformanceTestReportDir(getApplicationInfo());
 
             if (s_debugEnabled) {
                 S_LOGGER.debug("test type performance test Report directory " + performanceReportDir);
             }
 
             if (StringUtils.isNotEmpty(performanceReportDir) && StringUtils.isNotEmpty(getTestResultsType())) {
                 Pattern p = Pattern.compile(TEST_DIRECTORY);
                 Matcher matcher = p.matcher(performanceReportDir);
                 performanceReportDir = matcher.replaceAll(getTestResultsType());
                 sb.append(performanceReportDir);
             }
 
             if (s_debugEnabled) {
                 S_LOGGER.debug("test type performance test Report directory & Type " + sb.toString() + " Type " + getTestResultsType());
             }
 
             File file = new File(sb.toString());
             File[] resultFiles = file.listFiles(new XmlNameFileFilter(FILE_EXTENSION_XML));
             if (!ArrayUtils.isEmpty(resultFiles)) {
                 QualityUtil.sortResultFile(resultFiles);
                 for (File resultFile : resultFiles) {
                     if (resultFile.isFile()) {
                         testResultFiles.add(resultFile.getName());
                     }
                 }
             }
         } catch(PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.fetchPerformanceTestResultFiles()"+ FrameworkUtil.getStackTraceAsString(e));
             }
         } catch (PhrescoPomException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Quality.fetchPerformanceTestResultFiles()"+ FrameworkUtil.getStackTraceAsString(e));
             }
         }
 
         return SUCCESS;
     }
 	
     public String fetchPerformanceTestResult() {
         if (s_debugEnabled) {
            S_LOGGER.debug("Entering Method Quality.fetchPerformanceTestResult()");
         }
         
         try {
             ApplicationInfo appInfo = getApplicationInfo();
 //            String deviceId = getHttpRequest().getParameter("deviceId"); // android device id for display
             String techId = appInfo.getTechInfo().getId();
             if (s_debugEnabled) {
                S_LOGGER.debug("Performance test file name " + getTestResultFile());
             }
             if (StringUtils.isNotEmpty(getTestResultFile())) {
             	String testResultPath = getPerformanceTestResultPath(appInfo, getTestResultFile());
                 Document document = getDocument(new File(testResultPath)); 
                 Map<String, PerformanceTestResult> performanceTestResultMap = QualityUtil.getPerformanceReport(document, getHttpRequest(), techId, testResultDeviceId); // need to pass tech id and tag name
                 setReqAttribute(REQ_TEST_RESULT, performanceTestResultMap);
 
                 Set<String> keySet = performanceTestResultMap.keySet();
                 StringBuilder graphData = new StringBuilder("[");
                 StringBuilder label = new StringBuilder("[");
                 
                 List<Float> allMin = new ArrayList<Float>();
                 List<Float> allMax = new ArrayList<Float>();
                 List<Float> allAvg = new ArrayList<Float>();
                 int index = 0;
                 for (String key : keySet) {
                     PerformanceTestResult performanceTestResult = performanceTestResultMap.get(key);
                     if (REQ_TEST_SHOW_THROUGHPUT_GRAPH.equals(getShowGraphFor())) {
                         graphData.append(performanceTestResult.getThroughtPut());	//for ThroughtPut
                     } else if (REQ_TEST_SHOW_MIN_RESPONSE_GRAPH.equals(getShowGraphFor())) {
                         graphData.append(performanceTestResult.getMin());	//for min response time
                     } else if (REQ_TEST_SHOW_MAX_RESPONSE_GRAPH.equals(getShowGraphFor())) {
                         graphData.append(performanceTestResult.getMax());	//for max response time
                     } else if (REQ_TEST_SHOW_RESPONSE_TIME_GRAPH.equals(getShowGraphFor())) {
                    	 	graphData.append(performanceTestResult.getAvg());	//for responseTime
                     } else if (REQ_TEST_SHOW_ALL_GRAPH.equals(getShowGraphFor())) {
                     	graphData.append(performanceTestResult.getThroughtPut());	//for ThroughtPut
                     	allMin.add((float)performanceTestResult.getMin()/1000);
                     	allMax.add((float)performanceTestResult.getMax()/1000);
                     	allAvg.add((float) (performanceTestResult.getAvg())/1000);
                     }
                     
                     label.append("'");
                     label.append(performanceTestResult.getLabel());
                     label.append("'");
                     if (index < performanceTestResultMap.size() - 1) {
                         graphData.append(",");
                         label.append(",");
                     }
                     index++;
                 }
                 label.append("]");
                 graphData.append("]");
                 setReqAttribute(REQ_GRAPH_DATA, graphData.toString());
                 setReqAttribute(REQ_GRAPH_LABEL, label.toString());
                 setReqAttribute(REQ_GRAPH_ALL_DATA, allMin +", "+ allAvg +", "+ allMax);
                 setReqAttribute(REQ_SHOW_GRAPH, getShowGraphFor());
                 setReqAttribute(REQ_APP_INFO, appInfo);
             } else {
                 setReqAttribute(REQ_ERROR_TESTSUITE, ERROR_TEST_SUITE);
             }
         } catch (Exception e) {
             setReqAttribute(REQ_ERROR_DATA, ERROR_ANDROID_DATA);
             if (s_debugEnabled) {
                S_LOGGER.error("Entered into catch block of Quality.performanceTestResult()"+ e);
             }
         }
 
         return SUCCESS;
     }
     
     private String getPerformanceTestResultPath(ApplicationInfo appInfo, String testResultFile) throws PhrescoException, JAXBException, IOException, PhrescoPomException {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.getFunctionalTestResultPath()");
         }
         
         StringBuilder sb = new StringBuilder(getApplicationHome());
         
         //To change the dir_type based on the selected type
         FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
         String performanceTestReportDir = frameworkUtil.getPerformanceTestReportDir(appInfo);
         Pattern p = Pattern.compile(TEST_DIRECTORY);
         Matcher matcher = p.matcher(performanceTestReportDir);
         if (StringUtils.isNotEmpty(performanceTestReportDir) && matcher.find()) {
             performanceTestReportDir = matcher.replaceAll(getTestResultsType());
         }
         
         sb.append(performanceTestReportDir);
         sb.append(File.separator);
         sb.append(testResultFile);
         
         return sb.toString();
     }
 
     public String quality() {
         try {
             removeSessionAttribute(getAppId() + SESSION_APPINFO);//To remove the appInfo from the session
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             Project project = administrator.getProject(projectCode);
             setReqAttribute(REQ_PROJECT, project);
         } catch (Exception e) {
                S_LOGGER.error("Entered into catch block of Quality.quality()"+ e);
             new LogErrorReport(e, "Quality");
         }
         return APP_QUALITY;
     }
 
     public String generateFunctionalTest() throws PhrescoException {
     	S_LOGGER.debug("Entering Method Quality.generateTest()");
         
     	List<Environment> environments = null;
         try {
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             Project project = administrator.getProject(projectCode);
             String technology = project.getApplicationInfo().getTechInfo().getVersion();
             getHttpRequest().setAttribute(REQ_PROJECT, project);
             List<BuildInfo> buildInfos = administrator.getBuildInfos(project);
             environments = administrator.getEnvironments(project);
             
             List<String> screenResolution = new ArrayList<String>();
             screenResolution.add(FrameworkConstants._320_480);
             screenResolution.add(FrameworkConstants._1024_768);
             screenResolution.add(FrameworkConstants._1280_800);
             screenResolution.add(FrameworkConstants._1280_960);
             screenResolution.add(FrameworkConstants._1280_1024);
             screenResolution.add(FrameworkConstants._1360_768);
             screenResolution.add(FrameworkConstants._1440_900);
             screenResolution.add(FrameworkConstants._1600_900);
             getHttpRequest().setAttribute(REQ_RESOLUTIONS, screenResolution);
             
             getFunctionalTestBrowsers(technology);
 
             getHttpRequest().setAttribute(REQ_TEST_BUILD_INFOS, buildInfos);
         } catch (Exception e) {
         	S_LOGGER.error("Entered into catch block of Quality.generateTest()"+ e);
         }
         
         List<String> projectModules = getProjectModules(projectCode);
         getHttpRequest().setAttribute(REQ_PROJECT_MODULES, projectModules);
         getHttpRequest().setAttribute(REQ_ENVIRONMENTS, environments);
         getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
         return APP_GENERATE_TEST;
     }
 
 	public void getFunctionalTestBrowsers(String technology) {
 		String osType = getOsName();
 		if (WINDOWS.equals(osType)) {
 		    Map<String, String> windowsBrowsersMap = new HashMap<String, String>();
 		    if (TechnologyTypes.PHP.equals(technology) || TechnologyTypes.PHP_DRUPAL6.equals(technology) || TechnologyTypes.PHP_DRUPAL7.equals(technology) || TechnologyTypes.WORDPRESS.equals(technology)) {
 		    	windowsBrowsersMap.put(WIN_BROWSER_FIREFOX_KEY, BROWSER_FIREFOX_VALUE);
 		    	windowsBrowsersMap.put(WIN_BROWSER_OPERA_KEY, BROWSER_OPERA_VALUE);
 		        windowsBrowsersMap.put(WIN_BROWSER_WEB_DRIVER_INTERNET_EXPLORER_KEY, BROWSER_INTERNET_EXPLORER_VALUE);
 		        windowsBrowsersMap.put(WIN_BROWSER_SAFARI_KEY, BROWSER_SAFARI_VALUE);
 		    } else if (!TechnologyTypes.SHAREPOINT.equals(technology) && !TechnologyTypes.DOT_NET.equals(technology)) {
 		        windowsBrowsersMap.put(WIN_BROWSER_FIREFOX_KEY, BROWSER_FIREFOX_VALUE);
 		        windowsBrowsersMap.put(WIN_BROWSER_CHROME_KEY, BROWSER_CHROME_VALUE);
 		        windowsBrowsersMap.put(WIN_BROWSER_OPERA_KEY, BROWSER_OPERA_VALUE);
 		        windowsBrowsersMap.put(WIN_BROWSER_INTERNET_EXPLORER_KEY, BROWSER_INTERNET_EXPLORER_VALUE);
 		        windowsBrowsersMap.put(WIN_BROWSER_SAFARI_KEY, BROWSER_SAFARI_VALUE);
 		    } else {
 		    	windowsBrowsersMap.put(WIN_BROWSER_OPERA_KEY, BROWSER_OPERA_VALUE);
 		        windowsBrowsersMap.put(WIN_BROWSER_INTERNET_EXPLORER_KEY, BROWSER_INTERNET_EXPLORER_VALUE);
 		        windowsBrowsersMap.put(WIN_BROWSER_SAFARI_KEY, BROWSER_SAFARI_VALUE);
 		    }
 		    S_LOGGER.debug("Windows machine browsers list " + windowsBrowsersMap);
 		    getHttpRequest().setAttribute(REQ_TEST_BROWSERS, windowsBrowsersMap);
 		}
 
 		if (MAC.equals(osType)) {
 		    Map<String, String> macBrowsersMap = new HashMap<String, String>();
 		    if (TechnologyTypes.PHP.equals(technology) || TechnologyTypes.PHP_DRUPAL6.equals(technology) || TechnologyTypes.PHP_DRUPAL7.equals(technology) || TechnologyTypes.WORDPRESS.equals(technology)) {
 		    	macBrowsersMap.put(MAC_BROWSER_FIREFOX_KEY, BROWSER_FIREFOX_VALUE);
 		    	macBrowsersMap.put(MAC_BROWSER_OPERA_KEY, BROWSER_OPERA_VALUE);
 		    	macBrowsersMap.put(MAC_BROWSER_SAFARI_KEY, BROWSER_SAFARI_VALUE);
 		    } else if (!TechnologyTypes.SHAREPOINT.equals(technology) && !TechnologyTypes.DOT_NET.equals(technology)) {
 		        macBrowsersMap.put(MAC_BROWSER_FIREFOX_KEY, BROWSER_FIREFOX_VALUE);
 		        macBrowsersMap.put(MAC_BROWSER_CHROME_KEY, BROWSER_CHROME_VALUE);
 		        macBrowsersMap.put(MAC_BROWSER_OPERA_KEY, BROWSER_OPERA_VALUE);
 		        macBrowsersMap.put(MAC_BROWSER_SAFARI_KEY, BROWSER_SAFARI_VALUE);
 		    } else {
 		        macBrowsersMap.put(WIN_BROWSER_INTERNET_EXPLORER_KEY, BROWSER_INTERNET_EXPLORER_VALUE);
 		        macBrowsersMap.put(MAC_BROWSER_SAFARI_KEY, BROWSER_SAFARI_VALUE);
 		    }
 		    S_LOGGER.debug("Mac machine browsers list " + macBrowsersMap);
 		    getHttpRequest().setAttribute(REQ_TEST_BROWSERS, macBrowsersMap);
 		}
 
 		if (LINUX.equals(osType)) {
 		    Map<String, String> linuxBrowsersMap = new HashMap<String, String>();
 		    if (TechnologyTypes.PHP.equals(technology) || TechnologyTypes.PHP_DRUPAL6.equals(technology) || TechnologyTypes.PHP_DRUPAL7.equals(technology) || TechnologyTypes.WORDPRESS.equals(technology)) {
 		    	linuxBrowsersMap.put(LINUX_BROWSER_FIREFOX_KEY, BROWSER_FIREFOX_VALUE);
 		    	linuxBrowsersMap.put(LINUX_BROWSER_OPERA_KEY, BROWSER_OPERA_VALUE);
 		    	linuxBrowsersMap.put(LINUX_BROWSER_SAFARI_KEY, BROWSER_SAFARI_VALUE);
 		    } else if (!TechnologyTypes.SHAREPOINT.equals(technology) && !TechnologyTypes.DOT_NET.equals(technology)) {
 		        linuxBrowsersMap.put(LINUX_BROWSER_FIREFOX_KEY, BROWSER_FIREFOX_VALUE);
 		        linuxBrowsersMap.put(LINUX_BROWSER_CHROME_KEY,BROWSER_CHROME_VALUE);
 		        linuxBrowsersMap.put(WIN_BROWSER_OPERA_KEY, BROWSER_OPERA_VALUE);
 		        linuxBrowsersMap.put(LINUX_BROWSER_SAFARI_KEY, BROWSER_SAFARI_VALUE);
 		    } else {
 		        linuxBrowsersMap.put(WIN_BROWSER_INTERNET_EXPLORER_KEY, BROWSER_INTERNET_EXPLORER_VALUE);
 		        linuxBrowsersMap.put(LINUX_BROWSER_SAFARI_KEY, BROWSER_SAFARI_VALUE);
 		    }
 		    
 		    S_LOGGER.debug("Linux machine browsers list " + linuxBrowsersMap);
 		    getHttpRequest().setAttribute(REQ_TEST_BROWSERS, linuxBrowsersMap);
 		}
 	}
     
     public String generateUnitTest() {
     	S_LOGGER.debug("Entering Method Quality.generateUnitTest()");
         
         try {
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             Project project = administrator.getProject(projectCode);
             getHttpRequest().setAttribute(REQ_PROJECT, project);
             List<String> projectModules = getProjectModules();
             getHttpRequest().setAttribute(REQ_PROJECT_MODULES, projectModules);
             getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
         } catch (Exception e) {
         	S_LOGGER.error("Entered into catch block of Quality.generateTest()"+ e);
         }
         return APP_GENERATE_UNIT_TEST;
     }
     
     private List<String> getProjectModules() {
     	try {
             StringBuilder builder = new StringBuilder(Utility.getProjectHome());
             builder.append(projectCode);
             builder.append(File.separatorChar);
             builder.append(POM_XML);
     		File pomPath = new File(builder.toString());
     		PomProcessor processor = new PomProcessor(pomPath);
     		Modules pomModule = processor.getPomModule();
     		if (pomModule != null) {
     			return pomModule.getModule();
     		}
     	} catch (PhrescoPomException e) {
     	}
     	return null;
     }
 
     // This method returns what type of OS we are using
     public String getOsName() {
         String OS = null;
         String osType = null;
         if(OS == null) {
             OS = System.getProperty(OS_NAME).toLowerCase(); 
         }
         if (OS.indexOf(WINDOWS_CHECK) >= 0) {
             osType = WINDOWS;
         }
         if (OS.indexOf(MAC_CHECK) >= 0) {
             osType = MAC;
         }
         if (OS.indexOf(LINUX_CHECK) >= 0) {
             osType = LINUX;
         }
         return osType;
     }
     
     public String fetchUnitTestReport() throws TransformerException, PhrescoPomException {
         try {
             ApplicationInfo appInfo = getApplicationInfo();
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             String testSuitePath = "";
             if (StringUtils.isNotEmpty(getTechReport())) {
             	testSuitePath = frameworkUtil.getUnitTestSuitePath(appInfo, getTechReport());
 		    } else {
 		    	testSuitePath = frameworkUtil.getUnitTestSuitePath(appInfo);
 		    }
             String testCasePath = "";
             if (StringUtils.isNotEmpty(getTechReport())) {
             	testSuitePath = frameworkUtil.getUnitTestCasePath(appInfo, getTechReport());
 		    } else {
 		    	testSuitePath = frameworkUtil.getUnitTestCasePath(appInfo);
 		    }
             return testReport(testSuitePath, testCasePath);
         } catch (PhrescoException e) {
             // TODO: handle exception
         }
         
         return null;
     }
     
     public String fetchFunctionalTestReport() throws TransformerException, PhrescoPomException {
         try {
             ApplicationInfo appInfo = getApplicationInfo();
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             String testSuitePath = frameworkUtil.getFunctionalTestSuitePath(appInfo);
             String testCasePath = frameworkUtil.getFunctionalTestCasePath(appInfo);
             
             return testReport(testSuitePath, testCasePath);
         } catch (PhrescoException e) {
             // TODO: handle exception
         }
         
         return null;
     }
 
     private String testReport(String testSuitePath, String testCasePath) throws TransformerException, PhrescoPomException {
     	S_LOGGER.debug("Entering Method Quality.testReport()");
     	try {
     		String testSuitesMapKey = getAppId() + getTestType() + getProjectModule() + getTechReport();
         	Map<String, NodeList> testResultNameMap = testSuiteMap.get(testSuitesMapKey);
             NodeList testSuites = testResultNameMap.get(getTestSuite());
     		if (ALL.equals(getTestSuite())) {
     			Map<String, String> testSuitesResultMap = new HashMap<String, String>();
     			float totalTestSuites = 0;
     			float successTestSuites = 0;
     			float failureTestSuites = 0;
     			float errorTestSuites = 0;
     			// get all nodelist of testType of a project
     			Collection<NodeList> allTestResultNodeLists = testResultNameMap.values();
     			for (NodeList allTestResultNodeList : allTestResultNodeLists) {
         			if (allTestResultNodeList.getLength() > 0 ) {
     	    			List<TestSuite> allTestSuites = getTestSuite(allTestResultNodeList);
     	    			if (CollectionUtils.isNotEmpty(allTestSuites)) {
     		    			for (TestSuite tstSuite : allTestSuites) {
     		    				//testsuite values are set before calling getTestCases value
     							setTestSuite(tstSuite.getName());
     							getTestCases(allTestResultNodeList, testSuitePath, testCasePath);
     				            float tests = 0;
     				            float failures = 0;
     				            float errors = 0;
     				            tests = Float.parseFloat((String) getReqAttribute(REQ_TESTSUITE_TESTS));
     				            failures = Float.parseFloat((String) getReqAttribute(REQ_TESTSUITE_FAILURES));
     				            errors = Float.parseFloat((String) getReqAttribute(REQ_TESTSUITE_ERRORS));
     				            float success = 0;
     				            
     				            if (failures != 0 && errors == 0) {
     				                if (failures > tests) {
     				                    success = failures - tests;
     				                } else {
     				                    success = tests - failures;
     				                }
     				            } else if (failures == 0 && errors != 0) {
     				                if (errors > tests) {
     				                    success = errors - tests;
     				                } else {
     				                    success = tests - errors;
     				                }
     				            } else if (failures != 0 && errors != 0) {
     				                float failTotal = (failures + errors);
     				                if (failTotal > tests) {
     				                    success = failTotal - tests;
     				                } else {
     				                    success = tests - failTotal;
     				                }
     				            } else {
     				            	success = tests;
     				            }
     				            
     				            totalTestSuites = totalTestSuites + tests;
     				            failureTestSuites = failureTestSuites + failures;
     				            errorTestSuites = errorTestSuites + errors;
     				            successTestSuites = successTestSuites + success;
     				            String rstValues = tests + "," + success + "," + failures + "," + errors;
     				            testSuitesResultMap.put(tstSuite.getName(), rstValues);
     						}
     	    			}
         			}
 				}
     			setReqAttribute(REQ_ALL_TESTSUITE_MAP, testSuitesResultMap);
 				setReqAttribute(REQ_APP_DIR_NAME, getApplicationInfo().getAppDirName());
     			return APP_ALL_TEST_REPORT; 
     		} else {
 	            if (testSuites.getLength() > 0 ) {
 	            	List<TestCase> testCases = getTestCases(testSuites, testSuitePath, testCasePath);
 	            	if (CollectionUtils.isEmpty(testCases)) {
 	            		setReqAttribute(REQ_ERROR_TESTSUITE, ERROR_TEST_CASE);
 	            	} else {
 	            		setReqAttribute(REQ_TESTCASES, testCases);
 	            	}
 	            }
     		}
         } catch (PhrescoException e) {
         	S_LOGGER.error("Entered into catch block of Quality.testSuite()"+ e);
         }
 
 		return APP_TEST_REPORT;
     }
     
     public String testAndroid(){
         HttpServletRequest request = getHttpRequest();
         String testType = request.getParameter("testType");
         request.setAttribute(REQ_FROM_TAB, REQ_FROM_TAB_TEST);
         request.setAttribute("testType", testType);
         return APP_TEST_ANDROID;
     }
 
     public String generateJmeter() {
            S_LOGGER.debug("Entering Method Quality.generateJmeter()");
 
         String technology = null;
         try {
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             Project project = administrator.getProject(projectCode);
             technology = project.getApplicationInfo().getTechInfo().getVersion();
             List<Environment> environments = administrator.getEnvironments(project);
 			
             getHttpRequest().setAttribute(REQ_ENVIRONMENTS, environments);
 	        getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 	        getHttpRequest().setAttribute("jmeterTestAgainst", jmeterTestAgainst);
 	        getHttpRequest().setAttribute(REQ_TEST_TYPE_SELECTED, testType);
 	        getHttpRequest().setAttribute("technology", technology);
 	        if (TechnologyTypes.ANDROIDS.contains(technology) && testType.equals(REQ_TEST_PERFORMANCE)) {
 	        	QualityUtil util = new QualityUtil();
 	        	try {
 					ArrayList<String> connAndroidDevices = util.getConnAndroidDevices("adb devices");
 					getHttpRequest().setAttribute(REQ_ANDROID_CONN_DEVICES, connAndroidDevices);
 					testType = ANDROID_PERFORMACE;
 				} catch (Exception e) {
 				}
 	        }
         } catch (PhrescoException e) {
                S_LOGGER.error("Entered into catch block of Quality.generateJmeter()"+ e);
         }
         return testType;
     }
     
     public String fetchPerfTestJSONData() {
 		 if (s_debugEnabled) {
 		       S_LOGGER.debug("Entering Method Quality.fetchPerfTestJSONData()");
 		 }
 		 Reader read = null;
 		 try {
 			 ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 	         Project project = administrator.getProject(projectCode);
 	         FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 	         StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 	         builder.append(project.getApplicationInfo().getCode());
 //	         String performanceTestDir = frameworkUtil.getPerformanceTestDir(project.getApplicationInfo().getTechInfo().getVersion());
 //			 builder.append(performanceTestDir);
 			 if(WEBSERVICE.equals(jmeterTestAgainst)) {
 				 builder.append(WEBSERVICES_DIR);
 			 } else {
 				 builder.append(jmeterTestAgainst);
 			 }
 			 builder.append(File.separator);
 			 builder.append(testName);
 			 builder.append(".json");
 			 Gson gson = new Gson();
 		     read = new InputStreamReader(new FileInputStream(builder.toString()));
 		     performanceDetails = gson.fromJson(read, PerformanceDetails.class);
 		 } catch(Exception e){
 			 S_LOGGER.error("Entered into catch block of Quality.fetchPerfTestJSONData()"+ e);
 		 } finally {
 			 if (read != null) {
 				 try {
 					read.close();
 				} catch (IOException e) {
 					S_LOGGER.error("Entered into catch block of Quality.fetchPerfTestJSONData() finally"+ e);
 				}
 			 }
 		 }
 	 	 return SUCCESS;
     }
     
     public class XmlNameFileFilter implements FilenameFilter {
         private String filter_;
         public XmlNameFileFilter(String filter) {
             filter_ = filter;
         }
 
         public boolean accept(File dir, String name) {
             return name.endsWith(filter_);
         }
     }
     
     public class FileNameFileFilter implements FilenameFilter {
         private String filter_;
         private String startWith_;
         public FileNameFileFilter(String filter, String startWith) {
             filter_ = filter;
             startWith_ = startWith;
         }
 
         public boolean accept(File dir, String name) {
             return name.endsWith(filter_) && name.startsWith(startWith_);
         }
     }
 
     public String getSettingCaption() {
            S_LOGGER.debug("Entering Method Quality.getSettingCaption()");
         try {
 
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             Project project = administrator.getProject(projectCode);
             String envs = getHttpRequest().getParameter(REQ_ENVIRONMENTS);
             getHttpRequest().setAttribute(REQ_PROJECT, project); 
             SettingsInfo Settings = null;
             List<PropertyInfo> propertyInfos = null;
             String protocol = "";
             String host = "";
             String port = "";
             String context = "";
             if(StringUtils.isNotEmpty(settingType) && StringUtils.isNotEmpty(settingName)) {
 	            if (settingType.equals("server")) {
 	            	Settings = administrator.getSettingsInfo(settingName, Constants.SETTINGS_TEMPLATE_SERVER, projectCode, envs);
 	                propertyInfos = Settings.getPropertyInfos();
 	                for (PropertyInfo propertyInfo : propertyInfos) { 
 	                    if (propertyInfo.getKey().equals(Constants.SERVER_PROTOCOL)) {
 	                        protocol = propertyInfo.getValue();
 	                    }
 	                    if (propertyInfo.getKey().equals(Constants.SERVER_HOST)) {
 	                        host = propertyInfo.getValue();
 	                    }
 	                    if (propertyInfo.getKey().equals(Constants.SERVER_PORT)) {
 	                        port = propertyInfo.getValue();
 	                    } 
 	                    if (propertyInfo.getKey().equals(Constants.SERVER_CONTEXT)) {
 	                        context = propertyInfo.getValue();
 	                    }
 	                }
 	            }
 	            if (settingType.equals("webservices")) {
 	            	Settings = administrator.getSettingsInfo(settingName, Constants.SETTINGS_TEMPLATE_WEBSERVICE, projectCode, envs);
 	                propertyInfos = Settings.getPropertyInfos();
 	                for (PropertyInfo propertyInfo : propertyInfos) { 
 	                    if (propertyInfo.getKey().equals(Constants.WEB_SERVICE_PROTOCOL)) {
 	                        protocol =propertyInfo.getValue();
 	                    }
 	                    if (propertyInfo.getKey().equals(Constants.WEB_SERVICE_HOST)) {
 	                        host = propertyInfo.getValue();
 	                    }
 	                    if (propertyInfo.getKey().equals(Constants.WEB_SERVICE_PORT)) {
 	                        port = propertyInfo.getValue();
 	                    }
 	                    if (propertyInfo.getKey().equals(Constants.WEB_SERVICE_CONTEXT)) {
 	                        context = propertyInfo.getValue();
 	                    }
 	                }
 	            }
 	            if (settingType.equals("database")) {
 	            	Settings = administrator.getSettingsInfo(settingName, Constants.SETTINGS_TEMPLATE_DB, projectCode, envs);
 	            	propertyInfos = Settings.getPropertyInfos();
 	            	for (PropertyInfo propertyInfo : propertyInfos) { 
 	            		if (propertyInfo.getKey().equals(Constants.DB_PROTOCOL)) {
 	            			//protocol =propertyInfo.getValue();
 	            			protocol = Constants.DB_PROTOCOL;
 	            		}
 	            		if (propertyInfo.getKey().equals(Constants.DB_HOST)) {
 	            			host = propertyInfo.getValue();
 	            		}
 	            		if (propertyInfo.getKey().equals(Constants.DB_PORT)) {
 	            			port = propertyInfo.getValue();
 	            		}
 	            		if (propertyInfo.getKey().equals(Constants.DB_NAME)) {
 	            			context = propertyInfo.getValue();
 	            		}
 	            	}
 	            }
 	            
             }
 
             if (StringUtils.isNotEmpty(port)) {
                 port = ":" +  port;
             }
 
             if (StringUtils.isNotEmpty(context)) {
                 context = "/" + context;
             }
             caption = protocol + "://" + host + port + "" + context + "/";
             
         } catch (Exception e) {
                S_LOGGER.error("Entered into catch block of Quality.getSettingCaption()"+ FrameworkUtil.getStackTraceAsString(e));
         }
         return SUCCESS;
     }
    
 	public String tstResultFiles() {
            S_LOGGER.debug("Entering Method Quality.perTstResultFiles()");
         
         try {
         	String testDirPath = null;
         	testResultFiles =  new ArrayList<String>();
         	ApplicationInfo appInfo = getApplicationInfo();
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             StringBuilder sb = new StringBuilder();
             sb.append(Utility.getProjectHome());
             sb.append(appInfo.getAppDirName());
             if(StringUtils.isEmpty(settingType)) {
             	testDirPath = frameworkUtil.getLoadTestReportDir(appInfo);
             	sb.append(testDirPath);
             } else {
                 testDirPath = frameworkUtil.getPerformanceTestReportDir(getApplicationInfo());
                 if (StringUtils.isNotEmpty(testDirPath) && StringUtils.isNotEmpty(settingType)) {
                     Pattern p = Pattern.compile("dir_type");
                     Matcher matcher = p.matcher(testDirPath);
                     if(WEBSERVICE.equals(settingType)) {
                     	testDirPath = matcher.replaceAll(WEBSERVICES_DIR);
                     } else {
                     	testDirPath = matcher.replaceAll(settingType);
                     }
                     sb.append(testDirPath);
                 }
             }
 
             File file = new File(sb.toString());
             File[] children = file.listFiles(new XmlNameFileFilter(FILE_EXTENSION_XML));
             if(children != null && children.length > 0) {
                 for (File resultFile : children) {
                     if (resultFile.isFile()) {
                         testResultFiles.add(resultFile.getName());
                     }
                 }
 
             }
         } catch(Exception e) {
                S_LOGGER.error("Entered into catch block of Quality.perTstResultFiles"+ FrameworkUtil.getStackTraceAsString(e));
         }
         return SUCCESS;
     }
 	
 	public String devices() {
 		S_LOGGER.debug("Entering Method Quality.devices()");
         try {
             String testResultFile = getHttpRequest().getParameter(REQ_TEST_RESULT_FILE);
             if (!testResultFile.equals("null")) {
             	String testResultPath = getTestResultPath(getApplicationInfo(), testResultFile);
                 Document document = getDocument(new File(testResultPath)); 
         		deviceNames = QualityUtil.getDeviceNames(document);
             }
         } catch(Exception e) {
         	S_LOGGER.error("Entered into catch block of Quality.devices()"+ FrameworkUtil.getStackTraceAsString(e));
         }
         return SUCCESS;
 	}
 	
     public String testIphone() {
            S_LOGGER.debug("Entering Method Quality.testIPhone()");
         try {
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             Project project = administrator.getProject(projectCode);
             String testType = getHttpRequest().getParameter(REQ_TEST_TYPE);
             getHttpRequest().setAttribute(REQ_PROJECT, project);
             getHttpRequest().setAttribute(REQ_FROM_TAB, REQ_FROM_TAB_TEST); // test
             getHttpRequest().setAttribute(REQ_TEST_TYPE, testType);
             if(FUNCTIONAL.equals(testType)) {
                 List<BuildInfo> buildInfos = administrator.getBuildInfos(project);
                 getHttpRequest().setAttribute(REQ_TEST_BUILD_INFOS, buildInfos);
             }
             if(UNIT.equals(testType)) {
 				// Get xcode targets
 				List<PBXNativeTarget> xcodeConfigs = ApplicationsUtil.getXcodeConfiguration(projectCode);
 				getHttpRequest().setAttribute(REQ_XCODE_CONFIGS, xcodeConfigs);
 				// get list of sdks
 				List<String> iphoneSdks = IosSdkUtil.getMacSdks(MacSdkType.iphoneos);
 				iphoneSdks.addAll(IosSdkUtil.getMacSdks(MacSdkType.iphonesimulator));
 				iphoneSdks.addAll(IosSdkUtil.getMacSdks(MacSdkType.macosx));
 				getHttpRequest().setAttribute(REQ_IPHONE_SDKS, iphoneSdks);
             }
         } catch (Exception e) {
                S_LOGGER.error("Entered into catch block of Quality.testIPhone()"+ e);
         }
         return SUCCESS;
     }
     
     public String configNames() throws PhrescoException {
     	try {
 	    	String envName = getHttpRequest().getParameter("envName");
 	    	String type = getHttpRequest().getParameter("type");
 	    	ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 	    	List<SettingsInfo> settingsInfos = administrator.getSettingsInfos(type, projectCode, envName);
 	    	List<String> settingsInfoNames = new ArrayList<String>();
 	    	for (SettingsInfo settingsInfo : settingsInfos) {
 	    		settingsInfoNames.add(settingsInfo.getName());
 			}
 	    	configName = settingsInfoNames;
     	} catch(Exception e) {
     		throw new PhrescoException(e);
     	}
     	return SUCCESS;
     }
     
     public String fetchBuildInfoEnvs() {
     	try {
 	    	ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 	    	Project project = administrator.getProject(projectCode);
 	    	String buildNumber = getHttpRequest().getParameter(REQ_TEST_BUILD_ID);
 	    	buildInfoEnvs = administrator.getBuildInfo(project, Integer.parseInt(buildNumber)).getEnvironments();
     	} catch (PhrescoException e) {
 			// TODO: handle exception
 		}
     	return SUCCESS;
     }
     
     public String showGeneratePdfPopup() {
         S_LOGGER.debug("Entering Method Quality.printAsPdfPopup()");
         try {
         	boolean isReportAvailable = true;
 			ApplicationInfo appInfo = getApplicationInfo();
 			setReqAttribute(REQ_APPINFO, appInfo);
 			String sonarUrl = "";
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			sonarUrl = frameworkUtil.getSonarURL();
 //        	ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 //        	ApplicationInfo appInfo = getApplicationInfo();
 //            FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 //            String technology = appInfo.getTechInfo().getId();
 //            // is sonar report available
 //            isReportAvailable = isSonarReportAvailable(frameworkUtil, technology);
 //            
 //            // is test report available
 //            S_LOGGER.debug("sonar report avail !!!!!!!! " + isReportAvailable);
 //    	    if (!isReportAvailable) {
 //    	    	isReportAvailable = isTestReportAvailable(frameworkUtil, technology, appInfo);
 //    	    }
             
     	    S_LOGGER.debug(" Xml Results Available ====> " + isReportAvailable);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_PROJECT_ID, getProjectId());
 			setReqAttribute(REQ_CUSTOMER_ID, getCustomerId());
 			setReqAttribute(REQ_FROM_PAGE, getFromPage());
 			setReqAttribute(REQ_TEST_EXE, isReportAvailable);
 			setReqAttribute(REQ_SONAR_URL, sonarUrl);
         	List<String> existingPDFs = getExistingPDFs();
     		if (existingPDFs != null) {
     			setReqAttribute(REQ_PDF_REPORT_FILES, existingPDFs);
     		}
         } catch (Exception e) {
             S_LOGGER.error("Entered into catch block of Quality.printAsPdfPopup()"+ e);
         }
         setReqAttribute(REQ_TEST_TYPE, fromPage);
         return SUCCESS;
     }
 
 	private boolean isSonarReportAvailable(FrameworkUtil frameworkUtil, String technology) throws PhrescoException, MalformedURLException, JAXBException,
 			IOException, PhrescoPomException {
 		boolean isSonarReportAvailable = false;
 		// check for sonar alive
 		if (!TechnologyTypes.MOBILES.contains(technology)) {
 			List<String> sonarProfiles = frameworkUtil.getSonarProfiles();
 			if (CollectionUtils.isEmpty(sonarProfiles)) {
 				sonarProfiles.add(SONAR_SOURCE);
 			}
 			sonarProfiles.add(FUNCTIONAL);
 			FrameworkConfiguration frameworkConfig = PhrescoFrameworkFactory.getFrameworkConfig();
 			String serverUrl = frameworkUtil.getSonarURL();
 			String sonarReportPath = frameworkConfig.getSonarReportPath().replace(FORWARD_SLASH + SONAR, "");
 			serverUrl = serverUrl + sonarReportPath;
 			S_LOGGER.debug("serverUrl with report path " + serverUrl);
 			for (String sonarProfile : sonarProfiles) {
 				//get sonar report
 				StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 	        	builder.append(projectCode);
 	        	
 	            if (FUNCTIONALTEST.equals(sonarProfile)) {
 //	                builder.append(frameworkUtil.getFuncitonalTestDir(technology));
 	            }
 	            
 	            builder.append(File.separatorChar);
 	        	builder.append(POM_XML);
 	        	File sonarPomPath = new File(builder.toString());
 	        	
 	        	PomProcessor processor = new PomProcessor(sonarPomPath);
 	        	String groupId = processor.getModel().getGroupId();
 	        	String artifactId = processor.getModel().getArtifactId();
 
 	        	StringBuilder sbuild = new StringBuilder();
 	        	sbuild.append(groupId);
 	        	sbuild.append(COLON);
 	        	sbuild.append(artifactId);
 	        	
 	        	if (!SOURCE_DIR.equals(sonarProfile)) {
 	        		sbuild.append(COLON);
 	        		sbuild.append(sonarProfile);
 	        	}
 	        	
 	        	String artifact = sbuild.toString();
 	        	String sonarUrl = serverUrl + artifact;
 	        	S_LOGGER.debug("sonarUrl... " + sonarUrl);
 	        	if (isSonarAlive(sonarUrl)) {
 	        		isSonarReportAvailable = true;
 	        		break;
 	        	}
 			}
 			
 		}
 		    
 		if (TechnologyTypes.IPHONES.contains(technology)) {
 			StringBuilder codeValidatePath = new StringBuilder(Utility.getProjectHome());
 			codeValidatePath.append(projectCode);
 			codeValidatePath.append(File.separatorChar);
 			codeValidatePath.append(DO_NOT_CHECKIN_DIR);
 			codeValidatePath.append(File.separatorChar);
 			codeValidatePath.append(STATIC_ANALYSIS_REPORT);
 			codeValidatePath.append(File.separatorChar);
 			codeValidatePath.append(INDEX_HTML);
 		    File indexPath = new File(codeValidatePath.toString());
 		    if (indexPath.exists()) {
 		    	isSonarReportAvailable = true;
 		    }
 		}
 		return isSonarReportAvailable;
 	}
 
 	private boolean isSonarAlive(String url) {
 		boolean XmlResultsAvailable = false;
 	    try {
 			URL sonarURL = new URL(url);
 			HttpURLConnection connection = null;
 	    	connection = (HttpURLConnection) sonarURL.openConnection();
 	    	int responseCode = connection.getResponseCode();
 	    	if (responseCode != 200) {
 	    		XmlResultsAvailable = false;
 	        } else {
 	        	XmlResultsAvailable = true;
 	        }
 	    } catch(Exception e) {
 	    	XmlResultsAvailable = false;
 	    }
 	    return XmlResultsAvailable;
 	}
 	
 	private boolean isTestReportAvailable(FrameworkUtil frameworkUtil, String technology, ApplicationInfo appInfo) throws PhrescoPomException, PhrescoException {
 		//check unit and functional are executed already or not
         StringBuilder sb = new StringBuilder();
         sb.append(Utility.getProjectHome());
         sb.append(appInfo.getAppDirName());
 		boolean XmlResultsAvailable = false;
             if(!XmlResultsAvailable) {
             	S_LOGGER.debug("Unit dir " + sb.toString() + frameworkUtil.getUnitTestReportDir(appInfo));
             	File file = null;
             	if (StringUtils.isNotEmpty(getTechReport())) {
 //            		sb.append(frameworkUtil.getUnitTestReportDir(appInfo, getTechReport()));
             		file = new File(sb.toString() + frameworkUtil.getUnitTestReportDir(appInfo, getTechReport()));
             	} else {
             		file = new File(sb.toString() + frameworkUtil.getUnitTestReportDir(appInfo));
             		sb.append(frameworkUtil.getUnitTestReportDir(appInfo));
             	}
                 
 	            File[] children = file.listFiles(new XmlNameFileFilter(FILE_EXTENSION_XML));
 	            if(children != null && children.length > 0) {
 	            	XmlResultsAvailable = true;
 	            }
             }
             
             if(!XmlResultsAvailable) {
 //            	S_LOGGER.debug("Fucntional dir " + sb.toString() + frameworkUtil.getFunctionalReportDir(technology));
 //	            File file = new File(sb.toString() + frameworkUtil.getFunctionalReportDir(technology));
 //	            File[] children = file.listFiles(new XmlNameFileFilter(FILE_EXTENSION_XML));
 //	            if(children != null && children.length > 0) {
 //	            	XmlResultsAvailable = true;
 //	            }
             }
             
             if(!XmlResultsAvailable) {
 	            performanceTestResultAvail();
 	        	if(isResultFileAvailable()) {
 	        		S_LOGGER.debug("Check on performance for report");
 	        		XmlResultsAvailable = true;
 	        	}
             }
             
             if(!XmlResultsAvailable) {
             	S_LOGGER.debug("Load dir " + sb.toString() + frameworkUtil.getLoadTestReportDir(appInfo));
 	            File file = new File(sb.toString() + frameworkUtil.getLoadTestReportDir(appInfo));
 	            File[] children = file.listFiles(new XmlNameFileFilter(FILE_EXTENSION_XML));
 	            if(children != null && children.length > 0) {
 	            	XmlResultsAvailable = true;
 	            }
             }
 		return XmlResultsAvailable;
 	}
 
 	private List<String> getExistingPDFs() throws PhrescoException {
 		S_LOGGER.debug("Entering Method Quality.getExistingPDFs()");
 		List<String> pdfFiles = new ArrayList<String>();
 		// popup showing list of pdf's already created
 		String pdfDirLoc = "";
 		String fileFilterName = "";
 		if (StringUtils.isEmpty(fromPage) || FROMPAGE_ALL.equals(fromPage)) {
 			pdfDirLoc = Utility.getProjectHome() + getApplicationInfo().getAppDirName() + File.separator + DO_NOT_CHECKIN_DIR + File.separator + ARCHIVES + File.separator + CUMULATIVE;
 			fileFilterName = getApplicationInfo().getAppDirName();
 		} else {
 			pdfDirLoc = Utility.getProjectHome() + getApplicationInfo().getAppDirName() + File.separator + DO_NOT_CHECKIN_DIR + File.separator + ARCHIVES + File.separator + fromPage;
 			fileFilterName = fromPage;
 		}
 		File pdfFileDir = new File(pdfDirLoc);
 		if(pdfFileDir.isDirectory()) {
 		    File[] children = pdfFileDir.listFiles(new FileNameFileFilter(DOT + PDF, fileFilterName));
 		    QualityUtil util = new QualityUtil();
 		    if(children != null) {
 		    	util.sortResultFile(children);
 		    }
 			for (File child : children) {
 				String fileNameWithType = child.getName().replace(DOT + PDF, "").replace(fileFilterName + UNDERSCORE, "");
 				String[] fileWithType = fileNameWithType.split(UNDERSCORE);
 				pdfFiles.add(fileWithType[0] + UNDERSCORE + fileWithType[1]);
 			}
 		}
 		return pdfFiles;
 	}
     
     public String printAsPdf () {
         S_LOGGER.debug("Entering Method Quality.printAsPdf()");
         try {
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ProjectInfo projectInfo = getProjectInfo();
 			
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_PDF_REPORT)));
 			List<Parameter> parameters = getMojoParameters(mojo, PHASE_PDF_REPORT);
 			String sonarUrl = (String) getReqAttribute(REQ_SONAR_URL);
 	        if (CollectionUtils.isNotEmpty(parameters)) {
 	            for (Parameter parameter : parameters) {
 	            	String key = parameter.getKey();
 	            	if (REQ_REPORT_TYPE.equals(key)) {
 	            		parameter.setValue(reportDataType);
 	            	} else if (REQ_TEST_TYPE.equals(key)) {
 	            		if (StringUtils.isEmpty(fromPage)) {
 	            			setFromPage(FROMPAGE_ALL);
 	            		}
 	            		parameter.setValue(getFromPage());
 	            	} else if (REQ_SONAR_URL.equals(key)) {
 	            		parameter.setValue(sonarUrl);
 	            	}
 	            }
 	        }
 	        mojo.save();
 	        
 			List<String> buildArgCmds = getMavenArgCommands(parameters);
 			String workingDirectory = getAppDirectoryPath(applicationInfo);
 			BufferedReader reader = applicationManager.performAction(projectInfo, ActionType.PDF_REPORT, buildArgCmds, workingDirectory);
 			String line;
 			line = reader.readLine();
 			while (line != null) {
 				line = reader.readLine();
 				System.out.println("Restart Start Console : " + line);
 			}
 			setReqAttribute(REQ_APPINFO, applicationInfo);
 			setReqAttribute(REQ_FROM_PAGE, getFromPage());
             setReqAttribute(REQ_REPORT_STATUS, getText(SUCCESS_REPORT_STATUS));
         } catch (Exception e) {
         	S_LOGGER.error("Entered into catch block of Quality.printAsPdf()"+ e);
         	if (e.getLocalizedMessage().contains(getText(ERROR_REPORT_MISSISNG_FONT_MSG))) {
         		setReqAttribute(REQ_REPORT_STATUS, getText(ERROR_REPORT_MISSISNG_FONT));
         	} else {
         		setReqAttribute(REQ_REPORT_STATUS, getText(ERROR_REPORT_STATUS));
         	}
         }
         return showGeneratePdfPopup();
     }
 
     public void reportGeneration(Project project, String testType) throws PhrescoException {
     	S_LOGGER.debug("Entering Method Quality.reportGeneration()");
     	try {
     		PhrescoReportGeneration prg = new PhrescoReportGeneration();
             if (StringUtils.isEmpty(testType)) { 
             	prg.cumulativePdfReport(project, testType, reportDataType);
             } else {
             	prg.generatePdfReport(project, testType, reportDataType);
             }
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Quality.reportGeneration()" + e);
 			throw new PhrescoException(e);
 		}
 
     }
     
     public String downloadReport() {
         S_LOGGER.debug("Entering Method Quality.downloadReport()");
         try {
         	String fromPage = getReqParameter(REQ_FROM_PAGE);
         	String pdfLOC = "";
         	String archivePath = getApplicationHome() + File.separator + DO_NOT_CHECKIN_DIR + File.separator + ARCHIVES + File.separator;
         	if ((FrameworkConstants.ALL).equals(fromPage)) {
         		pdfLOC = archivePath + CUMULATIVE + File.separator + getApplicationInfo().getAppDirName() + UNDERSCORE + reportFileName + DOT + PDF;
         	} else {
         		pdfLOC = archivePath + fromPage + File.separator + fromPage + UNDERSCORE + reportFileName + DOT + PDF;
         	}
             File pdfFile = new File(pdfLOC);
             if (pdfFile.isFile()) {
     			fileInputStream = new FileInputStream(pdfFile);
     			fileName = reportFileName.split(UNDERSCORE)[1];
             }
         } catch (Exception e) {
             S_LOGGER.error("Entered into catch block of Quality.downloadReport()" + e);
         }
         return SUCCESS;
     }
     
     public String deleteReport() {
         S_LOGGER.debug("Entering Method Quality.deleteReport()");
         try {
         	String fromPage = getReqParameter(REQ_FROM_PAGE);
         	String pdfLOC = "";
         	String archivePath = getApplicationHome() + File.separator + DO_NOT_CHECKIN_DIR + File.separator + ARCHIVES + File.separator;
         	if ((FrameworkConstants.ALL).equals(fromPage)) {
         		pdfLOC = archivePath + CUMULATIVE + File.separator + getApplicationInfo().getAppDirName() + UNDERSCORE + reportFileName + DOT + PDF;
         	} else {
         		pdfLOC = archivePath + fromPage + File.separator + fromPage + UNDERSCORE + reportFileName + DOT + PDF;
         	}
             File pdfFile = new File(pdfLOC);
             if (pdfFile.isFile()) {
             	boolean reportDeleted = pdfFile.delete();
             	S_LOGGER.info("Report deleted " + reportDeleted);
             	if(reportDeleted) {
             		setReqAttribute(REQ_REPORT_DELETE_STATUS, getText(SUCCESS_REPORT_DELETE_STATUS));
             	} else {
             		setReqAttribute(REQ_REPORT_DELETE_STATUS, getText(ERROR_REPORT_DELETE_STATUS));
             	}
             }
         } catch (Exception e) {
             S_LOGGER.error("Entered into catch block of Quality.downloadReport()" + e);
         }
         return showGeneratePdfPopup();
     }
     
     public List<SettingsInfo> getServerSettings() {
         return serverSettings;
     }
 
     public void setServerSettings(List<SettingsInfo> serverSettings) {
         this.serverSettings = serverSettings;
     }
 
     public String getTestSuite() {
         return testSuite;
     }
 
     public void setTestSuite(String testSuite) {
         this.testSuite = testSuite;
     }
 
     public String getTests() {
         return tests;
     }
 
     public void setTests(String tests) {
         this.tests = tests;
     }
 
     public String getErrs() {
         return errs;
     }
 
     public void setErrs(String errs) {
         this.errs = errs;
     }
 
     public String getFailures() {
         return failures;
     }
 
     public void setFailures(String failures) {
         this.failures = failures;
     }
 
 
     public String getSetting() {
         return setting;
     }
 
     public void setSetting(String setting) {
         this.setting = setting;
     }
 
     public String getProjectCode() {
         return projectCode;
     }
 
     public void setProjectCode(String projectCode) {
         this.projectCode = projectCode;
     }
 
     public String getTestType() {
         return testType;
     }
 
     public void setTestType(String testType) {
         this.testType = testType;
     }
 
     public String getTestResultFile() {
         return testResultFile;
     }
 
     public void setTestResultFile(String testResultFile) {
         this.testResultFile = testResultFile;
     }
 
     public String getSettingType() {
         return settingType;
     }
 
     public void setSettingType(String settingType) {
         this.settingType = settingType;
     }
 
     public String getSettingName() {
         return settingName;
     }
 
 	public void setSettingName(String settingName) {
         this.settingName = settingName;
     }
 
     public void setCaption(String caption) {
         this.caption = caption;
     }
 
     public String getCaption() {
         return caption;
     }
 
     public List<String> getTestResultFiles() {
         return testResultFiles;
     }
 
     public void setTestResultFiles(List<String> testResultFiles) {
         this.testResultFiles = testResultFiles;
     }
 
     public List<String> getName() {
         return name;
     }
 
     public void setName(List<String> name) {
         this.name = name;
     }
 
     public List<String> getContext() {
         return context;
     }
 
     public void setContext(List<String> context) {
         this.context = context;
     }
 
     public String getTestResultsType() {
         return testResultsType;
     }
 
     public void setTestResultsType(String testResultsType) {
         this.testResultsType = testResultsType;
     }
 
     public String getNoOfUsers() {
         return noOfUsers;
     }
 
     public void setNoOfUsers(String noOfUsers) {
         this.noOfUsers = noOfUsers;
     }
 
     public String getRampUpPeriod() {
         return rampUpPeriod;
     }
 
     public void setRampUpPeriod(String rampUpPeriod) {
         this.rampUpPeriod = rampUpPeriod;
     }
 
     public String getLoopCount() {
         return loopCount;
     }
 
     public void setLoopCount(String loopCount) {
         this.loopCount = loopCount;
     }
 
     public String getJmeterTestAgainst() {
         return jmeterTestAgainst;
     }
 
     public void setJmeterTestAgainst(String jmeterTestAgainst) {
         this.jmeterTestAgainst = jmeterTestAgainst;
     }
     
     public String getSonarUrl() {
 		return sonarUrl;
 	}
 
 	public void setSonarUrl(String sonarUrl) {
 		this.sonarUrl = sonarUrl;
 	}
 
 	public String getTestName() {
 		return testName;
 	}
 
 	public void setTestName(String testName) {
 		this.testName = testName;
 	}
 
 	public boolean isResultFileAvailable() {
 		return resultFileAvailable;
 	}
 
 	public void setResultFileAvailable(boolean isAtleastOneFileAvail) {
 		this.resultFileAvailable = isAtleastOneFileAvail;
 	}
     
 	public String getShowError() {
 		return showError;
 	}
 
 	public void setShowError(String showError) {
 		this.showError = showError;
 	}
 
 	public String getTestResultDeviceId() {
 		return testResultDeviceId;
 	}
 
 	public void setTestResultDeviceId(String testResultDeviceId) {
 		this.testResultDeviceId = testResultDeviceId;
 	}
 
 	public Map<String, String> getDeviceNames() {
 		return deviceNames;
 	}
 
 	public void setDeviceNames(Map<String, String> deviceNames) {
 		this.deviceNames = deviceNames;
 	}
 
 	public List<String> getContextType() {
 		return contextType;
 	}
 
 	public void setContextType(List<String> contextType) {
 		this.contextType = contextType;
 	}
 
 	public List<String> getContextPostData() {
 		return contextPostData;
 	}
 
 	public void setContextPostData(List<String> contextPostData) {
 		this.contextPostData = contextPostData;
 	}
 
 	public List<String> getEncodingType() {
 		return encodingType;
 	}
 
 	public void setEncodingType(List<String> encodingType) {
 		this.encodingType = encodingType;
 	}
 
 	public String getSerialNumber() {
 		return serialNumber;
 	}
 
 	public void setSerialNumber(String serialNumber) {
 		this.serialNumber = serialNumber;
 	}
 
 	public List<String> getConfigName() {
 		return configName;
 	}
 
 	public void setConfigName(List<String> configName) {
 		this.configName = configName;
 	}
 
 	public String getTarget() {
 		return target;
 	}
 
 	public void setTarget(String target) {
 		this.target = target;
 	}
 	
 	public List<String> getBuildInfoEnvs() {
 		return buildInfoEnvs;
 	}
 
 	public void setBuildInfoEnvs(List<String> buildInfoEnvs) {
 		this.buildInfoEnvs = buildInfoEnvs;
 	}
 	
 	public String getHideLog() {
 		return hideLog;
 	}
 
 	public void setHideLog(String hideLog) {
 		this.hideLog = hideLog;
 	}
 
 	public String getSdk() {
 		return sdk;
 	}
 
 	public void setSdk(String sdk) {
 		this.sdk = sdk;
 	}
 	
 	public String getDatabase() {
 		return Database;
 	}
 
 	public void setDatabase(String database) {
 		Database = database;
 	}
 	
 	public List<String> getDbPerName() {
 		return dbPerName;
 	}
 
 	public void setDbPerName(List<String> dbPerName) {
 		this.dbPerName = dbPerName;
 	}
 	
 	public List<String> getQueryType() {
 		return queryType;
 	}
 
 	public void setQueryType(List<String> queryType) {
 		this.queryType = queryType;
 	}
 
 	public List<String> getQuery() {
 		return query;
 	}
 
 	public void setQuery(List<String> query) {
 		this.query = query;
 	}
 	
     public String getFromPage() {
 		return fromPage;
 	}
 
 	public void setFromPage(String fromPage) {
 		this.fromPage = fromPage;
 	}
 	
     public String getProjectModule() {
 		return projectModule;
 	}
 
 	public void setProjectModule(String projectModule) {
 		this.projectModule = projectModule;
 	}
 	
 	public List<TestSuite> getTestSuites() {
 		return testSuites;
 	}
 
 	public void setTestSuites(List<TestSuite> testSuites) {
 		this.testSuites = testSuites;
 	}
 	
 	public boolean getValidated() {
 		return validated;
 	}
 
 	public void setValidated(boolean validated) {
 		this.validated = validated;
 	}
 	
 	public String getTestModule() {
 		return testModule;
 	}
 
 	public void setTestModule(String testModule) {
 		this.testModule = testModule;
 	}
 
     public String getTechReport() {
 		return techReport;
 	}
 
 	public void setTechReport(String techReport) {
 		this.techReport = techReport;
 	}
   
 	public PerformanceDetails getPerformanceDetails() {
 		return performanceDetails;
 	}
 
 	public void setPerformanceDetails(PerformanceDetails performanceDetails) {
 		this.performanceDetails = performanceDetails;
 	}
 	
 	public List<String> getTestSuiteNames() {
 		return testSuiteNames;
 	}
 
 	public void setTestSuiteNames(List<String> testSuiteNames) {
 		this.testSuiteNames = testSuiteNames;
 	}
 	
 	public String getShowDebug() {
 		return showDebug;
 	}
 
 	public void setShowDebug(String showDebug) {
 		this.showDebug = showDebug;
 	}
 
     public String getReportName() {
         return reportName;
     }
 
     public void setReportName(String reportName) {
         this.reportName = reportName;
     }
 
     public String getReoportLocation() {
         return reoportLocation;
     }
 
     public void setReoportLocation(String reoportLocation) {
         this.reoportLocation = reoportLocation;
     }
 
 	public InputStream getFileInputStream() {
 		return fileInputStream;
 	}
 
 	public void setFileInputStream(InputStream fileInputStream) {
 		this.fileInputStream = fileInputStream;
 	}
 
 	public String getFileName() {
 		return fileName;
 	}
 
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 
 	public String getReportFileName() {
 		return reportFileName;
 	}
 
 	public void setReportFileName(String reportFileName) {
 		this.reportFileName = reportFileName;
 	}
 
 	public String getReportDataType() {
 		return reportDataType;
 	}
 
 	public void setReportDataType(String reportDataType) {
 		this.reportDataType = reportDataType;
 	}
     
 	public String getJarLocation() {
 		return jarLocation;
 	}
 
 	public void setJarLocation(String jarLocation) {
 		this.jarLocation = jarLocation;
 	}
 
 	public String getTestAgainst() {
 		return testAgainst;
 	}
 
 	public void setTestAgainst(String testAgainst) {
 		this.testAgainst = testAgainst;
 	}
 
 	public String getIosTestType() {
 		return iosTestType;
 	}
 
 	public void setIosTestType(String iosTestType) {
 		this.iosTestType = iosTestType;
 	}
 
 	public String getResolution() {
 		return resolution;
 	}
 
 	public void setResolution(String resolution) {
 		this.resolution = resolution;
 	}
 
     public boolean isConnectionAlive() {
         return connectionAlive;
     }
 
     public void setConnectionAlive(boolean connectionAlive) {
         this.connectionAlive = connectionAlive;
     }
 
     public String getShowGraphFor() {
         return showGraphFor;
     }
 
     public void setShowGraphFor(String showGraphFor) {
         this.showGraphFor = showGraphFor;
     }
 
 	public boolean isUpdateCache() {
 		return updateCache;
 	}
 
 	public void setUpdateCache(boolean updateCache) {
 		this.updateCache = updateCache;
 	}
 
 	public void setResultJson(String resultJson) {
 		this.resultJson = resultJson;
 	}
 
 	public String getResultJson() {
 		return resultJson;
 	}
 }
