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
 package com.photon.phresco.framework.rest.api;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.google.gson.Gson;
 import com.photon.phresco.commons.FileListFilter;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.ResponseCodes;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.commons.QualityUtil;
 import com.photon.phresco.framework.model.PerformancResultInfo;
 import com.photon.phresco.framework.model.PerformanceTestResult;
 import com.photon.phresco.framework.model.TestCase;
 import com.photon.phresco.framework.model.TestCaseError;
 import com.photon.phresco.framework.model.TestCaseFailure;
 import com.photon.phresco.framework.model.TestReportResult;
 import com.photon.phresco.framework.model.TestResult;
 import com.photon.phresco.framework.model.TestSuite;
 import com.photon.phresco.framework.rest.api.util.FrameworkServiceUtil;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.HubConfiguration;
 import com.photon.phresco.util.NodeConfiguration;
 import com.photon.phresco.util.ServiceConstants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Plugin;
 import com.phresco.pom.util.PomProcessor;
 import com.sun.jersey.api.client.ClientResponse.Status;
 
 /**
  * The Class QualityService.
  */
 @Path("/quality")
 public class QualityService extends RestBase implements ServiceConstants, FrameworkConstants, ResponseCodes {
 	/** The test suite map. */
 	private static Map<String, Map<String, NodeList>> testSuiteMap = Collections
 			.synchronizedMap(new HashMap<String, Map<String, NodeList>>(8));
 	
 	/** The set failure test cases. */
 	private int setFailureTestCases;
 	
 	/** The error test cases. */
 	private int errorTestCases;
 	
 	/** The node length. */
 	private int nodeLength;
 	
 	/** The test suite. */
 	private String testSuite = "";
 
 	/**
 	 * Unit.
 	 *
 	 * @param appDirName the app dir name
 	 * @param userId the user id
 	 * @return the response
 	 */
 	@GET
 	@Path(REST_API_UNIT)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response unit(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_USERID) String userId, @QueryParam(REST_QUERY_MODULE_NAME) String module) {
 		ResponseInfo<Map> responseData = new ResponseInfo<Map>();
 		try {
 			Map<String, List<String>> unitTestOptionsMap = new HashMap<String, List<String>>();
 			String rootModule = appDirName;
 			if (StringUtils.isNotEmpty(module)) {
 				appDirName = appDirName + File.separator + module;
 			}
 			List<String> unitReportOptions = getUnitReportOptions(appDirName);
 			if (StringUtils.isEmpty(module)) {
 				List<String> projectModules = FrameworkServiceUtil.getProjectModules(rootModule);
 				unitTestOptionsMap.put(PROJECT_MODULES, projectModules);
 				if (CollectionUtils.isNotEmpty(projectModules)) {
 					unitReportOptions = getUnitReportOptions(appDirName + File.separator + projectModules.get(0));
 				}
 			}
 			unitTestOptionsMap.put(REPORT_OPTIONS, unitReportOptions);
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, null,
 					unitTestOptionsMap, RESPONSE_STATUS_SUCCESS, PHRQ100001);
 			return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHRQ110001);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
 					.build();
 		}
 	}
 	
 	@GET
 	@Path("techOptions")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response reportOptions(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			 @QueryParam(REST_QUERY_MODULE_NAME) String module) {
 		ResponseInfo<List<String>> responseData = new ResponseInfo<List<String>>();
 		try {
 			List<String> unitReportOptions = getUnitReportOptions(appDirName + File.separator + module);
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, null,
 					unitReportOptions, RESPONSE_STATUS_SUCCESS, PHRQ100001);
 			return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHRQ110001);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
 					.build();
 		}
 	}
 
 	/**
 	 * Gets the test suites.
 	 *
 	 * @param appDirName the app dir name
 	 * @param testType the test type
 	 * @param techReport the tech report
 	 * @param moduleName the module name
 	 * @return the test suites
 	 * @throws PhrescoException the phresco exception
 	 */
 	@GET
 	@Path(REST_API_TEST_SUITES)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getTestSuites(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_TEST_TYPE) String testType, @QueryParam(REST_QUERY_TECH_REPORT) String techReport,
 			@QueryParam(REST_QUERY_MODULE_NAME) String moduleName, @QueryParam(REST_QUERY_PROJECT_CODE) String projectCode) throws PhrescoException {
 		ResponseInfo<List<TestSuite>> responseData = new ResponseInfo<List<TestSuite>>();
 		try {
 			
 			if (StringUtils.isNotEmpty(projectCode)) {
 				appDirName = projectCode + INTEGRATION_TEST;
 			}
 			// TO kill the Process
 			String rootModule = appDirName;
 			if (StringUtils.isNotEmpty(moduleName)) {
 				appDirName = appDirName + File.separator + moduleName;
 			}
 			String baseDir = Utility.getProjectHome() + appDirName;
 			Utility.killProcess(baseDir, testType);
 			String testSuitePath = getTestSuitePath(appDirName, testType, techReport);
 			String testCasePath = getTestCasePath(appDirName, testType, techReport);
 			List<TestSuite> testSuites = testSuites(rootModule, moduleName, testType, moduleName, techReport,
 					testSuitePath, testCasePath, ALL);
 			if (CollectionUtils.isEmpty(testSuites)) {
 				ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, null,
 						testSuites, RESPONSE_STATUS_SUCCESS, PHRQ000003);
 				return Response.status(Status.OK).entity(finalOuptut).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 			}
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, null,
 					testSuites, RESPONSE_STATUS_SUCCESS, PHRQ000001);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 		} catch (Exception e) {
 			ResponseInfo<List<TestSuite>> finalOutput = responseDataEvaluation(responseData, e,	null, RESPONSE_STATUS_ERROR, PHRQ010002);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 		}
 	}
 
 	/**
 	 * Test suites.
 	 *
 	 * @param appDirName the app dir name
 	 * @param moduleName the module name
 	 * @param testType the test type
 	 * @param module the module
 	 * @param techReport the tech report
 	 * @param testSuitePath the test suite path
 	 * @param testCasePath the test case path
 	 * @param testSuite the test suite
 	 * @return the list
 	 * @throws PhrescoException the phresco exception
 	 */
 	private List<TestSuite> testSuites(String appDirName, String moduleName, String testType, String module, String techReport, String testSuitePath, String testCasePath, String testSuite) throws PhrescoException {
 		setTestSuite(testSuite);
 		List<TestSuite> suites = new ArrayList<TestSuite>();
 		try {
 			String mapKey = constructMapKey(appDirName, moduleName);
 			String testSuitesMapKey = mapKey + testType + module + techReport;
 			String testResultPath = getTestResultPath(appDirName, moduleName, testType, techReport);
 			File[] testResultFiles = getTestResultFiles(testResultPath);
 			if (ArrayUtils.isEmpty(testResultFiles)) {
 				return null;
 			}
 			getTestSuiteNames(appDirName, testType, moduleName, techReport, testResultPath, testSuitePath);
 			Map<String, NodeList> testResultNameMap = testSuiteMap.get(testSuitesMapKey);
 			if (MapUtils.isEmpty(testResultNameMap)) {
 				return null;
 			}
 			
 			// get all nodelist of testType of a project
 			Collection<NodeList> allTestResultNodeLists = testResultNameMap.values();
 			for (NodeList allTestResultNodeList : allTestResultNodeLists) {
 				if (allTestResultNodeList.getLength() > 0) {
 					List<TestSuite> allTestSuites = getTestSuite(allTestResultNodeList);
 					if (CollectionUtils.isNotEmpty(allTestSuites)) {
 						for (TestSuite tstSuite : allTestSuites) {
 							// testsuite values are set before calling
 							// getTestCases value
 							setTestSuite(tstSuite.getName());
 							float tests = 0;
 							float failures = 0;
 							float errors = 0;
 							setNodeLength(Math.round(tstSuite.getTests()));
 							setSetFailureTestCases(Math.round(tstSuite.getFailures()));
 							setErrorTestCases(Math.round(tstSuite.getErrors()));
 							
 							tests = Float.parseFloat(String.valueOf(getNodeLength()));
 							failures = Float.parseFloat(String.valueOf(getSetFailureTestCases()));
 							errors = Float.parseFloat(String.valueOf(getErrorTestCases()));
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
 
 							TestSuite suite = new TestSuite();
 							suite.setName(tstSuite.getName());
 							suite.setSuccess(success);
 							suite.setErrors(errors);
 							suite.setFailures(tstSuite.getFailures());
 							suite.setTime(tstSuite.getTime());
 							suite.setTotal(tstSuite.getTests());
 							suite.setTestCases(tstSuite.getTestCases());
 							suites.add(suite);
 						}
 					}
 				}
 			}
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 		return suites;
 	}
 
 	private String constructMapKey(String appDirName, String moduleName) {
 		String key = appDirName;
 		if (StringUtils.isNotEmpty(moduleName)) {
 			key = appDirName + moduleName;
 		}
 		return key;
 	}
 
 	/**
 	 * Gets the test reports.
 	 *
 	 * @param appDirName the app dir name
 	 * @param testType the test type
 	 * @param techReport the tech report
 	 * @param moduleName the module name
 	 * @param testSuite the test suite
 	 * @return the test reports
 	 * @throws PhrescoException the phresco exception
 	 */
 	@GET
 	@Path(REST_API_TEST_REPORTS)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getTestReports(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_TEST_TYPE) String testType, @QueryParam(REST_QUERY_TECH_REPORT) String techReport,
 			@QueryParam(REST_QUERY_MODULE_NAME) String moduleName, @QueryParam(REST_QUERY_TEST_SUITE) String testSuite, @QueryParam(REST_QUERY_PROJECT_CODE) String projectCode)
 			throws PhrescoException {
 		String testSuitePath = "";
 		String testCasePath = "";
 		try {
 			if (StringUtils.isNotEmpty(projectCode)) {
 				appDirName = projectCode + INTEGRATION_TEST;
 			}
 			String rootModule = appDirName;
 			if (StringUtils.isNotEmpty(moduleName)) {
 				appDirName = appDirName + File.separator + moduleName;
 			}
 			testSuitePath = getTestSuitePath(appDirName, testType, techReport);
 			testCasePath = getTestCasePath(appDirName, testType, techReport);
 			return testReport(rootModule, moduleName, testType, moduleName, techReport, testSuitePath, testCasePath,
 					testSuite);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the functional test framework.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the functional test framework
 	 */
 	@GET
 	@Path(REST_API_FUNCTIONAL_FRAMEWORK)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getFunctionalTestFramework(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String module) {
 		ResponseInfo<Map<String, Object>> responseData = new ResponseInfo<Map<String, Object>>();
 		try {
 			if (StringUtils.isNotEmpty(module)) {
 				appDirName = appDirName + File.separator + module;
 			}
 			Map<String, Object> map = new HashMap<String, Object>();
 			String functionalTestFramework = FrameworkServiceUtil.getFunctionalTestFramework(appDirName);
 			map.put(FUNCTIONAL_FRAMEWORK, functionalTestFramework);
 			if (SELENIUM_GRID.equalsIgnoreCase(functionalTestFramework)) {
 				HubConfiguration hubConfig = getHubConfiguration(appDirName);
 				InetAddress ip = InetAddress.getLocalHost();
 				if (hubConfig != null) {
 					String host = ip.getHostAddress();
 					int port = hubConfig.getPort();
 					boolean isConnectionAlive = Utility.isConnectionAlive(HTTP_PROTOCOL, host, port);
 					map.put(HUB_STATUS, isConnectionAlive);
 				}
 				NodeConfiguration nodeConfig = getNodeConfiguration(appDirName);
 				if (nodeConfig != null) {
 					String host = ip.getHostAddress();
 					int port = nodeConfig.getConfiguration().getPort();
 					boolean isConnectionAlive = Utility.isConnectionAlive(HTTP_PROTOCOL, host, port);
 					map.put(NODE_STATUS, isConnectionAlive);
 				}
 			}
 			ResponseInfo<Map<String, Object>> finalOutput = responseDataEvaluation(responseData, null,
 					map, RESPONSE_STATUS_SUCCESS, PHRQ300001);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 		} catch (Exception e) {
 			ResponseInfo<Map<String, Object>> finalOutput = responseDataEvaluation(responseData, e,
 					null, PHRQ310001);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
 					.build();
 		}
 	}
 	
 	/**
 	 * Gets the status of the Hub/Node
 	 * @param appDirName
 	 * @param fromPage
 	 * @return
 	 */
 	@GET
 	@Path("/connectionAliveCheck")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response connectionAliveCheck(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_FROM_PAGE) String fromPage, 
 			 @QueryParam(REST_QUERY_MODULE_NAME) String module) {
 		boolean connection_status = false;
 		try {
 			if (StringUtils.isNotEmpty(module)) {
 				appDirName = appDirName + File.separator + module;
 			}
 			InetAddress ip = InetAddress.getLocalHost();
 			if (HUB_STATUS.equals(fromPage)) {
 				HubConfiguration hubConfig = getHubConfiguration(appDirName);
 				if (hubConfig != null) {
 					String host = ip.getHostAddress();
 					int port = hubConfig.getPort();
 					connection_status = Utility.isConnectionAlive(HTTP_PROTOCOL, host, port);
 				}
 			} else if (NODE_STATUS.equals(fromPage)) {
 				NodeConfiguration nodeConfig = getNodeConfiguration(appDirName);
 				if (nodeConfig != null) {
 					String host = ip.getHostAddress();
 					int port = nodeConfig.getConfiguration().getPort();
 					connection_status = Utility.isConnectionAlive(HTTP_PROTOCOL, host, port);
 				}
 			}
 		} catch (Exception e) {
 			return Response.status(Status.OK).entity(null).header("Access-Control-Allow-Origin", "*").build();
 		}
 
 		return Response.status(Status.OK).entity(connection_status).header("Access-Control-Allow-Origin", "*").build();
 	}
 
 	/**
 	 * Gets the hub configuration.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the hub configuration
 	 * @throws PhrescoException the phresco exception
 	 */
 	private HubConfiguration getHubConfiguration(String appDirName) throws PhrescoException {
 		BufferedReader reader = null;
 		HubConfiguration hubConfig = null;
 		try {
 			String functionalTestDir = FrameworkServiceUtil.getFunctionalTestDir(appDirName);
 			StringBuilder sb = new StringBuilder(FrameworkServiceUtil.getApplicationHome(appDirName));
 			sb.append(functionalTestDir).append(File.separator).append(Constants.HUB_CONFIG_JSON);
 			File hubConfigFile = new File(sb.toString());
 			Gson gson = new Gson();
 			reader = new BufferedReader(new FileReader(hubConfigFile));
 			hubConfig = gson.fromJson(reader, HubConfiguration.class);
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeReader(reader);
 		}
 
 		return hubConfig;
 	}
 	
 	/**
 	 * Gets the node configuration.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the node configuration
 	 * @throws PhrescoException the phresco exception
 	 */
 	private NodeConfiguration getNodeConfiguration(String appDirName) throws PhrescoException {
 		BufferedReader reader = null;
 		NodeConfiguration nodeConfig = null;
 		try {
 			String functionalTestDir = FrameworkServiceUtil.getFunctionalTestDir(appDirName);
 			StringBuilder sb = new StringBuilder(FrameworkServiceUtil.getApplicationHome(appDirName));
 			sb.append(functionalTestDir).append(File.separator).append(Constants.NODE_CONFIG_JSON);
 			File nodeConfigFile = new File(sb.toString());
 			Gson gson = new Gson();
 			reader = new BufferedReader(new FileReader(nodeConfigFile));
 			nodeConfig = gson.fromJson(reader, NodeConfiguration.class);
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeReader(reader);
 		}
 
 		return nodeConfig;
 	}
 
 	/**
 	 * Gets the test case path.
 	 *
 	 * @param appDirName the app dir name
 	 * @param testType the test type
 	 * @param techReport the tech report
 	 * @return the test case path
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getTestCasePath(String appDirName, String testType, String techReport) throws PhrescoException {
 		String testCasePath = "";
 		if (testType.equals(UNIT)) {
 			if (StringUtils.isNotEmpty(techReport)) {
 				testCasePath = getUnitTestCasePath(appDirName, techReport);
 			} else {
 				testCasePath = getUnitTestCasePath(appDirName);
 			}
 		} else if (testType.equals(FUNCTIONAL)) {
 			testCasePath = getFunctionalTestCasePath(appDirName);
 		} else if (testType.equals(COMPONENT)) {
 			testCasePath = getComponentTestCasePath(appDirName);
 		} else if(testType.equals(INTEGRATION)) {
 			testCasePath = getIntegrationTestCasePath(appDirName);
 		}
 		return testCasePath;
 	}
 
 	/**
 	 * Test report.
 	 *
 	 * @param appDirName the app dir name
 	 * @param moduleName the module name
 	 * @param testType the test type
 	 * @param module the module
 	 * @param techReport the tech report
 	 * @param testSuitePath the test suite path
 	 * @param testCasePath the test case path
 	 * @param testSuite the test suite
 	 * @return the response
 	 * @throws PhrescoException the phresco exception
 	 */
 	private Response testReport(String appDirName, String moduleName, String testType, String module, String techReport, String testSuitePath, String testCasePath, String testSuite) throws PhrescoException {
 		setTestSuite(testSuite);
 		ResponseInfo<TestReportResult> responseDataAll = new ResponseInfo<TestReportResult>();
 		ResponseInfo<List<TestCase>> responseData = new ResponseInfo<List<TestCase>>();
 		try {
 			String mapKey = constructMapKey(appDirName, moduleName);
 			String appDirWithModule = appDirName;
 			if (StringUtils.isNotEmpty(moduleName)) {
 				appDirWithModule = appDirName + File.separator + moduleName;
 			}
 			String testSuitesMapKey = mapKey + testType + module + techReport;
 			if (MapUtils.isEmpty(testSuiteMap)) {
 				String testResultPath = getTestResultPath(appDirName, moduleName, testType, techReport);
 				getTestSuiteNames(appDirName, testType, moduleName, techReport, testResultPath, testSuitePath);
 			}
 			Map<String, NodeList> testResultNameMap = testSuiteMap.get(testSuitesMapKey);
 			NodeList testSuites = testResultNameMap.get(testSuite);
 			if (ALL.equals(testSuite)) {
 				Map<String, String> testSuitesResultMap = new HashMap<String, String>();
 				float totalTestSuites = 0;
 				float successTestSuites = 0;
 				float failureTestSuites = 0;
 				float errorTestSuites = 0;
 				// get all nodelist of testType of a project
 				Collection<NodeList> allTestResultNodeLists = testResultNameMap.values();
 				for (NodeList allTestResultNodeList : allTestResultNodeLists) {
 					if (allTestResultNodeList.getLength() > 0) {
 						List<TestSuite> allTestSuites = getTestSuite(allTestResultNodeList);
 						if (CollectionUtils.isNotEmpty(allTestSuites)) {
 							for (TestSuite tstSuite : allTestSuites) {
 								// testsuite values are set before calling
 								// getTestCases value
 								setTestSuite(tstSuite.getName());
 								getTestCases(appDirWithModule, allTestResultNodeList, testSuitePath, testCasePath, testType);
 								float tests = 0;
 								float failures = 0;
 								float errors = 0;
 								tests = Float.parseFloat(String.valueOf(getNodeLength()));
 								failures = Float.parseFloat(String.valueOf(getSetFailureTestCases()));
 								errors = Float.parseFloat(String.valueOf(getErrorTestCases()));
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
 								String rstValues = tests + Constants.COMMA + success + Constants.COMMA + failures + Constants.COMMA + errors;
 								testSuitesResultMap.put(tstSuite.getName(), rstValues);
 							}
 						}
 					}
 				}
 				TestReportResult result = new TestReportResult();
 				result.setTestReports(testSuitesResultMap);
 				createTestReportResult(testSuitesResultMap, result);
 				ResponseInfo<TestReportResult> finalOutput = responseDataEvaluation(responseDataAll, null,
 						result, RESPONSE_STATUS_SUCCESS, PHRQ000002);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
 						.build();
 			} else {
 				if (testSuites.getLength() > 0) {
 					List<TestCase> testCases;
 					testCases = getTestCases(appDirWithModule, testSuites, testSuitePath, testCasePath, testType);
 					if (CollectionUtils.isEmpty(testCases)) {
 						ResponseInfo<List<TestCase>> finalOutput = responseDataEvaluation(responseData, null,
 								testCases, RESPONSE_STATUS_SUCCESS, PHRQ000004);
 						return Response.status(Status.OK).entity(finalOutput)
 								.header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 					} else {
 						boolean isClassEmpty = false;
 						// to check whether class attribute is there or not
 						for (TestCase testCase : testCases) {
 							if (testCase.getTestClass() == null) {
 								isClassEmpty = true;
 							}
 						}
 						ResponseInfo<List<TestCase>> finalOutput = responseDataEvaluation(responseData, null,
 								testCases, RESPONSE_STATUS_SUCCESS, PHRQ000002);
 						return Response.status(Status.OK).entity(finalOutput)
 								.header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 					}
 				}
 			}
 		} catch (PhrescoException e) {
 			ResponseInfo<List<TestCase>> finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHRQ010004);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 		}
 		return null;
 	}
 
 	/**
 	 * Creates the test report result.
 	 *
 	 * @param testSuitesResultMap the test suites result map
 	 * @param result the result
 	 */
 	private void createTestReportResult(Map<String, String> testSuitesResultMap, TestReportResult result) {
 		Set<String> keySet = testSuitesResultMap.keySet();
 		int totalValue = keySet.size();
 		// All testSuite total colum value calculation
 		int totalTstCases = 0;
 		int totalSuccessTstCases = 0;
 		int totalFailureTstCases = 0;
 		int totalErrorTstCases = 0;
 
 		for (String key : keySet) {
 			String csvResults = testSuitesResultMap.get(key);
 			String[] results = csvResults.split(Constants.COMMA);
 			float total = Float.parseFloat(results[0]);
 			float success = Float.parseFloat(results[1]);
 			float failure = Float.parseFloat(results[2]);
 			float error = Float.parseFloat(results[3]);
 			totalTstCases = totalTstCases + (int) total;
 			totalSuccessTstCases = totalSuccessTstCases + (int) success;
 			totalFailureTstCases = totalFailureTstCases + (int) failure;
 			totalErrorTstCases = totalErrorTstCases + (int) error;
 		}
 		result.setTotalTestError(totalErrorTstCases);
 		result.setTotalTestFailure(totalFailureTstCases);
 		result.setTotalTestSuccess(totalSuccessTstCases);
 		result.setTotalTestResults(totalValue);
 	}
 
 	/**
 	 * Gets the test cases.
 	 *
 	 * @param appDirName the app dir name
 	 * @param testSuites the test suites
 	 * @param testSuitePath the test suite path
 	 * @param testCasePath the test case path
 	 * @return the test cases
 	 * @throws PhrescoException the phresco exception
 	 */
 	private List<TestCase> getTestCases(String appDirName, NodeList testSuites, String testSuitePath, String testCasePath, String testType) throws PhrescoException {
 		InputStream fileInputStream = null;
		StringBuilder screenShotDir = new StringBuilder();
 		try {
 			StringBuilder sb = new StringBuilder(); 
 			sb.append(testSuitePath);
 			sb.append(NAME_FILTER_PREFIX);
 			sb.append(getTestSuite());
 			sb.append(NAME_FILTER_SUFIX);
 			sb.append(testCasePath);
 
 			XPath xpath = XPathFactory.newInstance().newXPath();
 			NodeList nodeList = (NodeList) xpath.evaluate(sb.toString(), testSuites.item(0).getParentNode(),
 					XPathConstants.NODESET);
 			// For tehnologies like php and drupal duoe to plugin change xml
 			// testcase path modified
 			if (nodeList.getLength() == 0) {
 				StringBuilder sbMulti = new StringBuilder();
 				sbMulti.append(testSuitePath);
 				sbMulti.append(NAME_FILTER_PREFIX);
 				sbMulti.append(getTestSuite());
 				sbMulti.append(NAME_FILTER_SUFIX);
 				sbMulti.append(XPATH_TESTSUTE_TESTCASE);
 				nodeList = (NodeList) xpath.evaluate(sbMulti.toString(), testSuites.item(0).getParentNode(),
 						XPathConstants.NODESET);
 			}
 
 			// For technology sharepoint
 			if (nodeList.getLength() == 0) {
 				StringBuilder sbMulti = new StringBuilder(); 
 				sbMulti.append(XPATH_MULTIPLE_TESTSUITE);
 				sbMulti.append(NAME_FILTER_PREFIX);
 				sbMulti.append(getTestSuite());
 				sbMulti.append(NAME_FILTER_SUFIX);
 				sbMulti.append(testCasePath);
 				nodeList = (NodeList) xpath.evaluate(sbMulti.toString(), testSuites.item(0).getParentNode(),
 						XPathConstants.NODESET);
 			}
 
 			List<TestCase> testCases = new ArrayList<TestCase>();
 			if(testType.equals(FUNCTIONAL)) {
 			 screenShotDir = screenShotDir(appDirName);
 			}
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
 								File file = new File(screenShotDir.toString() + testCase.getName()
 										+ FrameworkConstants.DOT + IMG_PNG_TYPE);
 								if (file.exists()) {
 									failure.setHasFailureImg(true);
 									fileInputStream = new FileInputStream(file);
 									if (fileInputStream != null) {
 										byte[] imgByte = null;
 										imgByte = IOUtils.toByteArray(fileInputStream);
 										byte[] encodedImage = Base64.encodeBase64(imgByte);
 										failure.setScreenshotPath(new String(encodedImage));
 									}
 								}
 								testCase.setTestCaseFailure(failure);
 							}
 						}
 
 						if (ELEMENT_ERROR.equals(childNode.getNodeName())) {
 							errorTestCases++;
 							TestCaseError error = getError(childNode);
 							if (error != null) {
 								File file = new File(screenShotDir.toString() + testCase.getName()
 										+ FrameworkConstants.DOT + IMG_PNG_TYPE);
 								if (file.exists()) {
 									error.setHasErrorImg(true);
 									fileInputStream = new FileInputStream(file);
 									if (fileInputStream != null) {
 										byte[] imgByte = null;
 										imgByte = IOUtils.toByteArray(fileInputStream);
 										byte[] encodedImage = Base64.encodeBase64(imgByte);
 										error.setScreenshotPath(new String(encodedImage));
 									}
 								}
 								testCase.setTestCaseError(error);
 							}
 						}
 					}
 				}
 				testCases.add(testCase);
 			}
 			setSetFailureTestCases(failureTestCases);
 			setErrorTestCases(errorTestCases);
 			setNodeLength(nodeList.getLength());
 			return testCases;
 		} catch (PhrescoException e) {
 			throw e;
 		} catch (XPathExpressionException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			try {
 				if (fileInputStream != null) {
 					fileInputStream.close();
 				}
 			} catch (IOException e) {
 
 			}
 		}
 	}
 
 	private StringBuilder screenShotDir(String appDirName) throws PhrescoException {
 		StringBuilder screenShotDir = new StringBuilder(Utility.getProjectHome() + appDirName);
 		screenShotDir.append(File.separator);
 		String sceenShotDir = getSceenShotDir(appDirName);
 		if (StringUtils.isEmpty(sceenShotDir)) {
 			screenShotDir.append(getFunctionalTestReportDir(appDirName));
 			screenShotDir.append(File.separator);
 			screenShotDir.append(SCREENSHOT_DIR);
 		} else {
 			screenShotDir.append(sceenShotDir);
 		}
 		screenShotDir.append(File.separator);
 		return screenShotDir;
 	}
 
 	/**
 	 * Gets the failure.
 	 *
 	 * @param failureNode the failure node
 	 * @return the failure
 	 */
 	private static TestCaseFailure getFailure(Node failureNode) {
 		TestCaseFailure failure = new TestCaseFailure();
 		failure.setDescription(failureNode.getTextContent());
 		failure.setFailureType(REQ_TITLE_EXCEPTION);
 		NamedNodeMap nameNodeMap = failureNode.getAttributes();
 
 		if (nameNodeMap != null && nameNodeMap.getLength() > 0) {
 			for (int k = 0; k < nameNodeMap.getLength(); k++) {
 				Node attribute = nameNodeMap.item(k);
 				String attributeName = attribute.getNodeName();
 				String attributeValue = attribute.getNodeValue();
 
 				if (ATTR_TYPE.equals(attributeName)) {
 					failure.setFailureType(attributeValue);
 				}
 			}
 		}
 		return failure;
 	}
 
 	/**
 	 * Gets the error.
 	 *
 	 * @param errorNode the error node
 	 * @return the error
 	 */
 	private static TestCaseError getError(Node errorNode) {
 		TestCaseError tcError = new TestCaseError();
 		tcError.setDescription(errorNode.getTextContent());
 		tcError.setErrorType(REQ_TITLE_ERROR);
 		NamedNodeMap nameNodeMap = errorNode.getAttributes();
 		if (nameNodeMap != null && nameNodeMap.getLength() > 0) {
 			for (int k = 0; k < nameNodeMap.getLength(); k++) {
 				Node attribute = nameNodeMap.item(k);
 				String attributeName = attribute.getNodeName();
 				String attributeValue = attribute.getNodeValue();
 
 				if (ATTR_TYPE.equals(attributeName)) {
 					tcError.setErrorType(attributeValue);
 				}
 			}
 		}
 		return tcError;
 	}
 
 	/**
 	 * Gets the unit report options.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the unit report options
 	 * @throws PhrescoException the phresco exception
 	 */
 	private List<String> getUnitReportOptions(String appDirName) throws PhrescoException {
 		try {
 			String unitTestReportOptions = getUnitTestReportOptions(appDirName);
 			if (StringUtils.isNotEmpty(unitTestReportOptions)) {
 				return Arrays.asList(unitTestReportOptions.split(Constants.COMMA));
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the test result path.
 	 *
 	 * @param appDirName the app dir name
 	 * @param moduleName the module name
 	 * @param testType the test type
 	 * @param techReport the tech report
 	 * @return the test result path
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getTestResultPath(String appDirName, String moduleName, String testType, String techReport)
 			throws PhrescoException {
 		if(StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName; 
 		}
 		String testResultPath = "";
 		if (testType.equals(UNIT)) {
 			testResultPath = getUnitTestResultPath(appDirName, moduleName, techReport);
 		} else if (testType.equals(FUNCTIONAL)) {
 			testResultPath = getFunctionalTestResultPath(appDirName, moduleName);
 		} else if (testType.equals(COMPONENT)) {
 			testResultPath = getComponentTestResultPath(appDirName, moduleName);
 		} else if (testType.equals(INTEGRATION)) {
 			testResultPath = getIntegraionTestResultPath(appDirName);
 		}
 		
 		return testResultPath;
 	}
 
 	/**
 	 * Gets the test suite path.
 	 *
 	 * @param appDirName the app dir name
 	 * @param testType the test type
 	 * @param techReport the tech report
 	 * @return the test suite path
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getTestSuitePath(String appDirName, String testType, String techReport) throws PhrescoException {
 		String testSuitePath = "";
 		if (testType.equals(UNIT)) {
 			if (StringUtils.isNotEmpty(techReport)) {
 				testSuitePath = getUnitTestSuitePath(appDirName, techReport);
 			} else {
 				testSuitePath = getUnitTestSuitePath(appDirName);
 			}
 		} else if (testType.equals(COMPONENT)) {
 			testSuitePath = getComponentTestSuitePath(appDirName);
 		} else if (testType.equals(FUNCTIONAL)) {
 			testSuitePath = getFunctionalTestSuitePath(appDirName);
 		} else if(testType.equals(INTEGRATION)) {
 			testSuitePath = getIntegrationTestSuitePath(appDirName);
 		}
 		return testSuitePath;
 	}
 
 	/**
 	 * Gets the unit test result path.
 	 *
 	 * @param appDirName the app dir name
 	 * @param moduleName the module name
 	 * @param techReport the tech report
 	 * @return the unit test result path
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getUnitTestResultPath(String appDirName, String moduleName, String techReport)
 			throws PhrescoException {
 		StringBuilder sb = new StringBuilder(Utility.getProjectHome() + appDirName);
 		// TODO Need to change this
 		StringBuilder tempsb = new StringBuilder(sb);
 		if (FrameworkConstants.JAVASCRIPT.equals(techReport)) {
 			tempsb.append(FrameworkConstants.UNIT_TEST_QUNIT_REPORT_DIR);
 			File file = new File(tempsb.toString());
 			if (file.isDirectory() && file.list().length > 0) {
 				sb.append(FrameworkConstants.UNIT_TEST_QUNIT_REPORT_DIR);
 			} else {
 				sb.append(FrameworkConstants.UNIT_TEST_JASMINE_REPORT_DIR);
 			}
 		} else {
 			if (StringUtils.isNotEmpty(techReport)) {
 				sb.append(getUnitTestReportDir(appDirName, techReport));
 			} else {
 				sb.append(getUnitTestReportDir(appDirName));
 			}
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Gets the functional test result path.
 	 *
 	 * @param appDirName the app dir name
 	 * @param moduleName the module name
 	 * @return the functional test result path
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getFunctionalTestResultPath(String appDirName, String moduleName) throws PhrescoException {
 
 		StringBuilder sb = new StringBuilder();
 		try {
 			sb.append(Utility.getProjectHome() + appDirName);
 			sb.append(getFunctionalTestReportDir(appDirName));
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 
 		return sb.toString();
 	}
 	
 	
 	private String getIntegraionTestResultPath(String appDirName) throws PhrescoException {
 
 		StringBuilder sb = new StringBuilder();
 		try {
 			sb.append(Utility.getProjectHome() + appDirName);
 			sb.append(getIntegrationTestReportDir(appDirName));
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 
 		return sb.toString();
 	}
 	/**
 	 * Gets the component test result path.
 	 *
 	 * @param appDirName the app dir name
 	 * @param moduleName the module name
 	 * @return the component test result path
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getComponentTestResultPath(String appDirName, String moduleName) throws PhrescoException {
 
 		StringBuilder sb = new StringBuilder();
 		try {
 			sb.append(Utility.getProjectHome() + appDirName);
 			sb.append(getComponentTestReportDir(appDirName));
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 
 		return sb.toString();
 	}
 
 	/**
 	 * Gets the test suite names.
 	 *
 	 * @param appDirName the app dir name
 	 * @param testType the test type
 	 * @param moduleName the module name
 	 * @param techReport the tech report
 	 * @param testResultPath the test result path
 	 * @param testSuitePath the test suite path
 	 * @return the test suite names
 	 * @throws PhrescoException the phresco exception
 	 */
 	private List<String> getTestSuiteNames(String appDirName, String testType, String moduleName, String techReport,
 			String testResultPath, String testSuitePath) throws PhrescoException {
 		String mapKey = constructMapKey(appDirName, moduleName);
 		String testSuitesMapKey = mapKey + testType + moduleName + techReport;
 		Map<String, NodeList> testResultNameMap = testSuiteMap.get(testSuitesMapKey);
 		List<String> resultTestSuiteNames = null;
 		//if (MapUtils.isEmpty(testResultNameMap)) {
 			File[] resultFiles = getTestResultFiles(testResultPath);
 			if (!ArrayUtils.isEmpty(resultFiles)) {
 				QualityUtil.sortResultFile(resultFiles);
 				updateCache(appDirName, testType, moduleName, techReport, resultFiles, testSuitePath);
 			}
 			testResultNameMap = testSuiteMap.get(testSuitesMapKey);
 		//}
 		if (testResultNameMap != null) {
 			resultTestSuiteNames = new ArrayList<String>(testResultNameMap.keySet());
 		}
 		return resultTestSuiteNames;
 	}
 
 	/**
 	 * Gets the unit test report dir.
 	 *
 	 * @param appDirName the app dir name
 	 * @param option the option
 	 * @return the unit test report dir
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getUnitTestReportDir(String appDirName, String option) throws PhrescoException {
 		try {
 			PomProcessor pomProcessor = FrameworkUtil.getInstance().getPomProcessor(appDirName);
 			return pomProcessor.getProperty(Constants.POM_PROP_KEY_UNITTEST_RPT_DIR_START + option
 					+ Constants.POM_PROP_KEY_UNITTEST_RPT_DIR_END);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the unit test report dir.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the unit test report dir
 	 * @throws PhrescoException the phresco exception
 	 */
 	public String getUnitTestReportDir(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(
 					Constants.POM_PROP_KEY_UNITTEST_RPT_DIR);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the unit test suite path.
 	 *
 	 * @param appDirName the app dir name
 	 * @param option the option
 	 * @return the unit test suite path
 	 * @throws PhrescoException the phresco exception
 	 */
 	public String getUnitTestSuitePath(String appDirName, String option) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(
 					Constants.POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH_START + option
 							+ Constants.POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH_END);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the unit test suite path.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the unit test suite path
 	 * @throws PhrescoException the phresco exception
 	 */
 	public String getUnitTestSuitePath(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(
 					Constants.POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	
 	public String getIntegrationTestReportDir(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessorForIntegration(appDirName).getProperty(
 					Constants.POM_PROP_KEY_INTGRATIONTEST_RPT_DIR);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the Integration test suite path.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the unit test suite path
 	 * @throws PhrescoException the phresco exception
 	 */
 	public String getIntegrationTestSuitePath(String appDirName) throws PhrescoException {
 		try {
 		return FrameworkUtil.getInstance().getPomProcessorForIntegration(appDirName).getProperty(
 					Constants.POM_PROP_KEY_INTEGRATIONTEST_TESTSUITE_XPATH);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Update cache.
 	 *
 	 * @param appDirName the app dir name
 	 * @param testType the test type
 	 * @param moduleName the module name
 	 * @param techReport the tech report
 	 * @param resultFiles the result files
 	 * @param testSuitePath the test suite path
 	 * @throws PhrescoException the phresco exception
 	 */
 	private void updateCache(String appDirName, String testType, String moduleName, String techReport,
 			File[] resultFiles, String testSuitePath) throws PhrescoException {
 		Map<String, NodeList> mapTestSuites = new HashMap<String, NodeList>(10);
 		for (File resultFile : resultFiles) {
 			Document doc = getDocument(resultFile);
 			NodeList testSuiteNodeList = evaluateTestSuite(doc, testSuitePath);
 			if (testSuiteNodeList.getLength() > 0) {
 				List<TestSuite> allTestSuites = getTestSuite(testSuiteNodeList);
 				for (TestSuite tstSuite : allTestSuites) {
 					mapTestSuites.put(tstSuite.getName(), testSuiteNodeList);
 				}
 			}
 		}
 		String mapKey = constructMapKey(appDirName, moduleName);
 		String testSuitesKey = mapKey + testType + moduleName + techReport;
 		testSuiteMap.put(testSuitesKey, mapTestSuites);
 	}
 
 	/**
 	 * Gets the document.
 	 *
 	 * @param resultFile the result file
 	 * @return the document
 	 * @throws PhrescoException the phresco exception
 	 */
 	private Document getDocument(File resultFile) throws PhrescoException {
 		InputStream fis = null;
 		DocumentBuilder builder = null;
 		try {
 			fis = new FileInputStream(resultFile);
 			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
 			domFactory.setNamespaceAware(false);
 			builder = domFactory.newDocumentBuilder();
 			Document doc = builder.parse(fis);
 			return doc;
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		} catch (ParserConfigurationException e) {
 			throw new PhrescoException(e);
 		} catch (SAXException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			if (fis != null) {
 				try {
 					fis.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * Evaluate test suite.
 	 *
 	 * @param doc the doc
 	 * @param testSuitePath the test suite path
 	 * @return the node list
 	 * @throws PhrescoException the phresco exception
 	 */
 	private NodeList evaluateTestSuite(Document doc, String testSuitePath) throws PhrescoException {
 		XPath xpath = XPathFactory.newInstance().newXPath();
 		XPathExpression xPathExpression;
 		NodeList testSuiteNode = null;
 		try {
 			xPathExpression = xpath.compile(testSuitePath);
 			testSuiteNode = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			throw new PhrescoException(e);
 		}
 		return testSuiteNode;
 	}
 
 	/**
 	 * Gets the test suite.
 	 *
 	 * @param nodelist the nodelist
 	 * @return the test suite
 	 * @throws PhrescoException the phresco exception
 	 */
 	private List<TestSuite> getTestSuite(NodeList nodelist) throws PhrescoException {
 		List<TestSuite> allTestSuites = new ArrayList<TestSuite>(2);
 		TestSuite tstSuite = null;
 		for (int i = 0; i < nodelist.getLength(); i++) {
 			tstSuite = new TestSuite();
 			Node node = nodelist.item(i);
 			NamedNodeMap nameNodeMap = node.getAttributes();
 
 			for (int k = 0; k < nameNodeMap.getLength(); k++) {
 				Node attribute = nameNodeMap.item(k);
 				String attributeName = attribute.getNodeName();
 				String attributeValue = attribute.getNodeValue();
 				if (FrameworkConstants.ATTR_ASSERTIONS.equals(attributeName)) {
 					tstSuite.setAssertions(attributeValue);
 				} else if (FrameworkConstants.ATTR_ERRORS.equals(attributeName)) {
 					tstSuite.setErrors(Float.parseFloat(attributeValue));
 				} else if (FrameworkConstants.ATTR_FAILURES.equals(attributeName)) {
 					tstSuite.setFailures(Float.parseFloat(attributeValue));
 				} else if (FrameworkConstants.ATTR_FILE.equals(attributeName)) {
 					tstSuite.setFile(attributeValue);
 				} else if (FrameworkConstants.ATTR_NAME.equals(attributeName)) {
 					tstSuite.setName(attributeValue);
 				} else if (FrameworkConstants.ATTR_TESTS.equals(attributeName)) {
 					tstSuite.setTests(Float.parseFloat(attributeValue));
 				} else if (FrameworkConstants.ATTR_TIME.equals(attributeName)) {
 					tstSuite.setTime(attributeValue);
 				}
 			}
 			allTestSuites.add(tstSuite);
 		}
 		return allTestSuites;
 	}
 
 	/**
 	 * Gets the test result files.
 	 *
 	 * @param path the path
 	 * @return the test result files
 	 */
 	private File[] getTestResultFiles(String path) {
 		File testDir = new File(path);
 		if (testDir.isDirectory()) {
 			FilenameFilter filter = new FileListFilter("", XML);
 			return testDir.listFiles(filter);
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the functional test suite path.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the functional test suite path
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getFunctionalTestSuitePath(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(
 					Constants.POM_PROP_KEY_FUNCTEST_TESTSUITE_XPATH);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the component test suite path.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the component test suite path
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getComponentTestSuitePath(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(
 					Constants.POM_PROP_KEY_COMPONENTTEST_TESTSUITE_XPATH);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the functional test report dir.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the functional test report dir
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getFunctionalTestReportDir(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(
 					Constants.POM_PROP_KEY_FUNCTEST_RPT_DIR);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the component test report dir.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the component test report dir
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getComponentTestReportDir(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(
 					Constants.POM_PROP_KEY_COMPONENTTEST_RPT_DIR);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the unit test report options.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the unit test report options
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getUnitTestReportOptions(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(Constants.PHRESCO_UNIT_TEST);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the unit test case path.
 	 *
 	 * @param appDirName the app dir name
 	 * @param option the option
 	 * @return the unit test case path
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getUnitTestCasePath(String appDirName, String option) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(
 					Constants.POM_PROP_KEY_UNITTEST_TESTCASE_PATH_START + option
 							+ Constants.POM_PROP_KEY_UNITTEST_TESTCASE_PATH_END);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the unit test case path.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the unit test case path
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getUnitTestCasePath(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(
 					Constants.POM_PROP_KEY_UNITTEST_TESTCASE_PATH);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the functional test case path.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the functional test case path
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getFunctionalTestCasePath(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(
 					Constants.POM_PROP_KEY_FUNCTEST_TESTCASE_PATH);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private String getIntegrationTestCasePath(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessorForIntegration(appDirName).getProperty(
 					Constants.POM_PROP_KEY_INTEGRATIONTEST_TESTCASE_PATH);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the component test case path.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the component test case path
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getComponentTestCasePath(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(
 					Constants.POM_PROP_KEY_COMPONENTTEST_TESTCASE_PATH);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the sceen shot dir.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the sceen shot dir
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getSceenShotDir(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(
 					Constants.POM_PROP_KEY_SCREENSHOT_DIR);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	/**
 	 * Performance.
 	 *
 	 * @param appDirName the app dir name
 	 * @param userId the user id
 	 * @return the response
 	 */
 	@GET
 	@Path(REST_API_PERFORMANCE)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response performance(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String module) {
 		List<String> testResultFiles = null;
 		ResponseInfo<Map> responseData = new ResponseInfo<Map>();
 		try {
 			if (StringUtils.isNotEmpty(module)) {
 				appDirName = appDirName + File.separator + module;
 			}
 			Map<String, Object> performanceMap = new HashMap<String, Object>();
             MojoProcessor mojo = new MojoProcessor(new File(FrameworkServiceUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_PERFORMANCE_TEST, 
 									Constants.PHASE_PERFORMANCE_TEST, appDirName)));
             List<String> testAgainsts = new ArrayList<String>();
             Parameter testAgainstParameter = mojo.getParameter(Constants.PHASE_PERFORMANCE_TEST, REQ_TEST_AGAINST);
             if (testAgainstParameter != null && TYPE_LIST.equalsIgnoreCase(testAgainstParameter.getType())) {
             	List<Value> values = testAgainstParameter.getPossibleValues().getValue();
             	for (Value value : values) {
             		testAgainsts.add(value.getKey());
 				}
             }
             boolean resutlAvailable = testResultAvail(appDirName, testAgainsts, Constants.PHASE_PERFORMANCE_TEST);
             boolean showDevice = Boolean.parseBoolean(getPerformanceTestShowDevice(appDirName));
             if (resutlAvailable) {
             	testResultFiles = testResultFiles(appDirName, testAgainsts, showDevice, Constants.PHASE_PERFORMANCE_TEST);
             }
             
             List<String> devices = new ArrayList<String>();
             if (showDevice && CollectionUtils.isNotEmpty(testResultFiles) && StringUtils.isNotEmpty(testResultFiles.get(0))) { 
         		String testResultPath = getLoadOrPerformanceTestResultPath(appDirName, "", testResultFiles.get(0), Constants.PHASE_PERFORMANCE_TEST);
         		Document document = getDocument(new File(testResultPath)); 
         		devices = QualityUtil.getDeviceNames(document);
             }
             
             performanceMap.put(TEST_AGAINSTS, testAgainsts);
             performanceMap.put(RESULT_AVAILABLE, resutlAvailable);
             performanceMap.put(TEST_RESULT_FILES, testResultFiles);
             performanceMap.put(SHOW_DEVICE, showDevice);
             performanceMap.put(DEVICES, devices);
             
 			ResponseInfo<Map> finalOutput = responseDataEvaluation(responseData, null,
 					performanceMap, RESPONSE_STATUS_SUCCESS, PHRQ500001);
 			return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 		} catch (Exception e) {
 			ResponseInfo<Map> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHRQ510001);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
 					.build();
 		}
 	}
 	
 	@POST
 	@Path(REST_API_TEST_RESULT_FILES)
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getTypeFiles(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_ACTION_TYPE) String actionType, List<String> testAgainsts, @QueryParam(REST_QUERY_MODULE_NAME) String module) {
 		List<String> testResultFiles = null;
 		boolean resutlAvailable = false;
 		ResponseInfo<List<String>> responseData = new ResponseInfo<List<String>>();
 		try {
 			if (StringUtils.isNotEmpty(module)) {
 				appDirName = appDirName + File.separator + module;
 			}
 			if (actionType.equalsIgnoreCase(Constants.PHASE_PERFORMANCE_TEST)) {
 				resutlAvailable = testResultAvail(appDirName, testAgainsts, Constants.PHASE_PERFORMANCE_TEST);
 			} else if (actionType.equalsIgnoreCase(Constants.PHASE_LOAD_TEST)) {
 				resutlAvailable = testResultAvail(appDirName, testAgainsts, Constants.PHASE_LOAD_TEST);
 			}
 			boolean showDevice = Boolean.parseBoolean(getPerformanceTestShowDevice(appDirName));
 			if (resutlAvailable) {
 				testResultFiles = testResultFiles(appDirName, testAgainsts, showDevice, actionType);
 				ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, null,
 						testResultFiles, RESPONSE_STATUS_SUCCESS, PHRQ500002);
 				return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 			}
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, null,
 					testResultFiles, RESPONSE_STATUS_FAILURE, PHRQ510002);
 			return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHRQ510003);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
 					.build();
 		}
 	}
 	
 	@GET
 	@Path(REST_API_DEVICES)
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response devices(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, 
 			@QueryParam(REST_QUERY_RESULT_FILE_NAME) String resultFileName, @QueryParam(REST_QUERY_MODULE_NAME) String module) throws PhrescoException {
 		ResponseInfo<List<String>> responseData = new ResponseInfo<List<String>>();
 		List<String> devices = new ArrayList<String>();
 		try {
 			if (StringUtils.isNotEmpty(module)) {
 				appDirName = appDirName + File.separator + module;
 			}
 			String testResultPath = getLoadOrPerformanceTestResultPath(appDirName, "", resultFileName, Constants.PHASE_PERFORMANCE_TEST);
 			Document document = getDocument(new File(testResultPath));
 			devices = QualityUtil.getDeviceNames(document);
 
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, null,
 					devices, RESPONSE_STATUS_SUCCESS, PHRQ500003);
 			return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 		} catch (Exception e) {
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHRQ510004);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
 			.build();
 		}
 	}
 	
 	public boolean testResultAvail(String appDirName, List<String> testAgainsts, String action) throws PhrescoException {
 		boolean resultAvailable = false;
         try {
         	String reportDir = "";
         	String resultExtension = "";
         	if (Constants.PHASE_PERFORMANCE_TEST.equals(action)) {
         		reportDir =FrameworkServiceUtil.getPerformanceTestReportDir(appDirName);
         		resultExtension = FrameworkServiceUtil.getPerformanceResultFileExtension(appDirName);
         	} else {
         		reportDir = FrameworkServiceUtil.getLoadTestReportDir(appDirName);
         		resultExtension = FrameworkServiceUtil.getLoadResultFileExtension(appDirName);
         	}
             for (String testAgainst: testAgainsts) {
             	StringBuilder sb = new StringBuilder(FrameworkServiceUtil.getApplicationHome(appDirName));
             	if (Constants.PHASE_PERFORMANCE_TEST.equals(action)) {
             		reportDir =FrameworkServiceUtil.getPerformanceTestReportDir(appDirName);
             	} else {
             		reportDir = FrameworkServiceUtil.getLoadTestReportDir(appDirName);
             	}
                
                 if (StringUtils.isNotEmpty(reportDir) && StringUtils.isNotEmpty(testAgainst)) {
                     Pattern p = Pattern.compile(TEST_DIRECTORY);
                     Matcher matcher = p.matcher(reportDir);
                     reportDir = matcher.replaceAll(testAgainst);
                     sb.append(reportDir); 
                 }
                 
                 File file = new File(sb.toString());
                 if (StringUtils.isNotEmpty(resultExtension) && file.exists()) {
                 	File[] children = file.listFiles(new XmlNameFileFilter(resultExtension));
                 	if (!ArrayUtils.isEmpty(children)) {
                 		resultAvailable = true;
                 		break;
                 	}
                 }
             }
             
             if (CollectionUtils.isEmpty(testAgainsts) && Constants.PHASE_PERFORMANCE_TEST.equals(action) 
             			&& StringUtils.isNotEmpty(reportDir)) {
             	 StringBuilder sb = new StringBuilder(FrameworkServiceUtil.getApplicationHome(appDirName));
             	 sb.append(reportDir);
             	 File file = new File(sb.toString());
                  if (StringUtils.isNotEmpty(resultExtension)) {
                  	File[] children = file.listFiles(new XmlNameFileFilter(resultExtension));
                  	if (!ArrayUtils.isEmpty(children)) {
                  		resultAvailable = true;
                  	}
                  }
             }
             
         } catch(Exception e) {
         	throw new PhrescoException(e);
         }
 
         return resultAvailable;
     }
 	
 	private List<String> testResultFiles(String appDirName, List<String> testAgainsts, boolean showDevice, String action) throws PhrescoException {
 		List<String> testResultFiles = new ArrayList<String>();
 		String reportDir = "";
     	String resultExtension = "";
 		try {
             StringBuilder sb = new StringBuilder(FrameworkServiceUtil.getApplicationHome(appDirName));
         	if (Constants.PHASE_PERFORMANCE_TEST.equals(action)) {
         		reportDir =FrameworkServiceUtil.getPerformanceTestReportDir(appDirName);
         		resultExtension = FrameworkServiceUtil.getPerformanceResultFileExtension(appDirName);
         	} else {
         		reportDir = FrameworkServiceUtil.getLoadTestReportDir(appDirName);
         		resultExtension = FrameworkServiceUtil.getLoadResultFileExtension(appDirName);
         	}
         	
             //test against will be available 
             if (StringUtils.isNotEmpty(reportDir) && CollectionUtils.isNotEmpty(testAgainsts)) {
                 Pattern p = Pattern.compile(TEST_DIRECTORY);
                 Matcher matcher = p.matcher(reportDir);
                 reportDir = matcher.replaceAll(testAgainsts.get(0));
                 sb.append(reportDir);
             }
             
             //for android - test type will not be available --- to get device id from result xml
             if (Constants.PHASE_PERFORMANCE_TEST.equals(action) && showDevice) {
             	sb.append(reportDir);
             }
             
             File file = new File(sb.toString());
 
             if (StringUtils.isNotEmpty(resultExtension)) {
             	File[] resultFiles = file.listFiles(new XmlNameFileFilter(resultExtension));
             	if (!ArrayUtils.isEmpty(resultFiles)) {
             		QualityUtil.sortResultFile(resultFiles);
             		for (File resultFile : resultFiles) {
             			if (resultFile.isFile()) {
             				testResultFiles.add(resultFile.getName());
             			}
             		}
             	}
             }
         } catch(Exception e) {
         	throw new PhrescoException(e);
         } 
 
         return testResultFiles;
     }
 	
 	private List<String> getScreenShot(String testAgainst, String resultFile, String appDirName, String from) throws PhrescoException {
 		List<String> imgSources = new ArrayList<String>();
 		try {
 			String testDir = "";
 			if (PERFORMACE.equals(from)) {
 				testDir = FrameworkServiceUtil.getPerformanceTestDir(appDirName);
 			} else {
 				testDir = FrameworkServiceUtil.getLoadTestDir(appDirName);
 			}
 			
 			StringBuilder sb = new StringBuilder(FrameworkServiceUtil.getApplicationHome(appDirName));
 			sb.append(testDir).append(File.separator).append(testAgainst);
 			int lastDot = resultFile.lastIndexOf(".");
 			String resultName = resultFile.substring(0, lastDot);
 
 			File testPomFile = new File(sb.toString() + File.separator + POM_XML);
 			PomProcessor pomProcessor = new PomProcessor(testPomFile);
 			Plugin plugin = pomProcessor.getPlugin(COM_LAZERYCODE_JMETER, JMETER_MAVEN_PLUGIN);
 			com.phresco.pom.model.Plugin.Configuration jmeterConfiguration = plugin.getConfiguration();
 			List<Element> jmeterConfigs = jmeterConfiguration.getAny();
 			for (Element element : jmeterConfigs) {
 				if (PLUGIN_TYPES.equalsIgnoreCase(element.getTagName()) && element.hasChildNodes()) {
 					NodeList types = element.getChildNodes();
 					for (int i = 0; i < types.getLength(); i++) {
 						Node pluginType = types.item(i);
 						if (StringUtils.isNotEmpty(pluginType.getTextContent())) {
 							File imgFile = new File(sb.toString() + RESULTS_JMETER_GRAPHS + resultName + HYPHEN + pluginType.getTextContent() + PNG);
 							if (imgFile.exists()) {
 								InputStream imageStream = new FileInputStream(imgFile);
 								String imgSrc = new String(Base64.encodeBase64(IOUtils.toByteArray(imageStream)));
 								imgSources.add(imgSrc + "#NAME_SEP#" + resultName + HYPHEN + pluginType.getTextContent());
 							}
 						}
 					}
 				}
 			}
 
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		
 		return imgSources;
 	}
 	/**
 	 * Performance.
 	 *
 	 * @param appDirName the app dir name
 	 * @param userId the user id
 	 * @return the response
 	 */
 	@GET
 	@Path(REST_API_PERFORMANCE_RESULTS)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response performanceTestResults(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_TEST_AGAINST) String testAgainst , 
 			@QueryParam(REST_QUERY_RESULT_FILE_NAME) String resultFileName, @QueryParam(REST_QUERY_DEVICE_ID) String deviceId, @QueryParam(REST_QUERY_SHOW_GRAPH_FOR) String showGraphFor, 
 			@QueryParam("from") String from, @QueryParam(REST_QUERY_MODULE_NAME) String module) {
 		if (StringUtils.isNotEmpty(module)) {
 			appDirName = appDirName + File.separator + module;
 		}
 		ResponseInfo<List<PerformanceTestResult>> responseData = new ResponseInfo<List<PerformanceTestResult>>();
 		PerformancResultInfo performanceResultInfo = null;
 		 StringBuilder graphData = new StringBuilder("[");
          StringBuilder label = new StringBuilder("[");
          int index = 0;
          List<Float> allMin = new ArrayList<Float>();
          List<Float> allMax = new ArrayList<Float>();
          List<Float> allAvg = new ArrayList<Float>();
         try {
             ApplicationInfo appInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
             String techId = appInfo.getTechInfo().getId();
             if (StringUtils.isNotEmpty(resultFileName)) {
             	String testResultPath = getLoadOrPerformanceTestResultPath(appDirName, testAgainst, resultFileName, from);
                 Document document = getDocument(new File(testResultPath)); 
                 performanceResultInfo = QualityUtil.getPerformanceReport(document, techId, deviceId);
                 List<PerformanceTestResult> perfromanceTestResult = performanceResultInfo.getPerfromanceTestResult();
                 for (PerformanceTestResult performanceTestResult : perfromanceTestResult) {
                     if (REQ_TEST_SHOW_THROUGHPUT_GRAPH.equals(showGraphFor)) {
                     	//for ThroughtPut
                         graphData.append(performanceTestResult.getThroughtPut());	
                     } else if (REQ_TEST_SHOW_MIN_RESPONSE_GRAPH.equals(showGraphFor)) {
                     	//for min response time
                         graphData.append(performanceTestResult.getMin());	
                     } else if (REQ_TEST_SHOW_MAX_RESPONSE_GRAPH.equals(showGraphFor)) {
                     	//for max response time
                         graphData.append(performanceTestResult.getMax());	
                     } else if (REQ_TEST_SHOW_RESPONSE_TIME_GRAPH.equals(showGraphFor)) {
                     	//for responseTime
                    	 	graphData.append(performanceTestResult.getAvg());	
                     } else if (REQ_TEST_SHOW_ALL_GRAPH.equals(showGraphFor)) {
                     	//for ThroughtPut
                     	graphData.append(performanceTestResult.getThroughtPut());	
                     	allMin.add((float)performanceTestResult.getMin()/1000);
                     	allMax.add((float)performanceTestResult.getMax()/1000);
                     	allAvg.add((float) (performanceTestResult.getAvg())/1000);
                     }
                     
                     label.append("'");
                     label.append(performanceTestResult.getLabel());
                     label.append("'");
                     if (index < perfromanceTestResult.size() - 1) {
                         graphData.append(Constants.COMMA);
                         label.append(Constants.COMMA);
                     }
                     index++;
                 }
                 label.append("]");
                 graphData.append("]");
             }
             List<String> screenShots = new ArrayList<String>();
             if (StringUtils.isNotEmpty(testAgainst)) {
             	if (Constants.PHASE_PERFORMANCE_TEST.equals(from)) {
             		screenShots = getScreenShot(testAgainst, resultFileName, appDirName, PERFORMACE);
             	} else {
             		screenShots = getScreenShot(testAgainst, resultFileName, appDirName, LOAD);
             	}
             }
             performanceResultInfo.setGraphData(graphData.toString());
             performanceResultInfo.setLabel(label.toString());
             performanceResultInfo.setGraphAlldata(allMin +", "+ allAvg +", "+ allMax);
             performanceResultInfo.setGraphFor(showGraphFor);
             performanceResultInfo.setImages(screenShots);
             
             ResponseInfo<List<PerformanceTestResult>> finalOutput = responseDataEvaluation(responseData, null,
 					performanceResultInfo, RESPONSE_STATUS_SUCCESS, PHRQ500004);
 			return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 		} catch (Exception e) {
 			ResponseInfo<List<PerformanceTestResult>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHRQ510005);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
 					.build();
 		}
     }
 	
 	private String getLoadOrPerformanceTestResultPath(String appDirName, String testAgainst, String resultFileName, String action) throws PhrescoException {
 		try {
 	        StringBuilder sb = new StringBuilder(FrameworkServiceUtil.getApplicationHome(appDirName));
 	        
 	        String reportDir = "";
         	if (Constants.PHASE_PERFORMANCE_TEST.equals(action)) {
         		reportDir =FrameworkServiceUtil.getPerformanceTestReportDir(appDirName);
         	} else {
         		reportDir = FrameworkServiceUtil.getLoadTestReportDir(appDirName);
         	}
 	        //To change the dir_type based on the selected type
 	        Pattern p = Pattern.compile(TEST_DIRECTORY);
 	        Matcher matcher = p.matcher(reportDir);
 	        if (StringUtils.isNotEmpty(reportDir) && StringUtils.isNotEmpty(testAgainst) && matcher.find()) {
 	        	reportDir = matcher.replaceAll(testAgainst);
 	        }
 	        sb.append(reportDir);
 	        sb.append(File.separator);
 	        sb.append(resultFileName);
 
 	        return sb.toString();
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private String getPerformanceTestShowDevice(String appDirName) throws PhrescoException {
 		try {
 			return FrameworkUtil.getInstance().getPomProcessor(appDirName).getProperty(Constants.POM_PROP_KEY_PERF_SHOW_DEVICE);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
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
 	
 	
 	/**
 	 * load.
 	 *
 	 * @param appDirName the app dir name
 	 * @param userId the user id
 	 * @return the response
 	 */
 	@GET
 	@Path(REST_API_LOAD)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response load(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String module) {
 		ResponseInfo<Map> responseData = new ResponseInfo<Map>();
 		try {
 			if (StringUtils.isNotEmpty(module)) {
 				appDirName = appDirName + File.separator + module;
 			}
 			Map<String, Object> loadMap = new HashMap<String, Object>();
             MojoProcessor mojo = new MojoProcessor(new File(FrameworkServiceUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_LOAD_TEST, 
 									Constants.PHASE_LOAD_TEST, appDirName)));
             List<String> testAgainsts = new ArrayList<String>();
             Parameter testAgainstParameter = mojo.getParameter(Constants.PHASE_LOAD_TEST, REQ_TEST_AGAINST);
             if (testAgainstParameter != null && TYPE_LIST.equalsIgnoreCase(testAgainstParameter.getType())) {
             	List<Value> values = testAgainstParameter.getPossibleValues().getValue();
             	for (Value value : values) {
             		testAgainsts.add(value.getKey());
 				}
             }
             boolean resutlAvailable = testResultAvail(appDirName, testAgainsts, Constants.PHASE_LOAD_TEST);
             List<String> testResultFiles = new ArrayList<String>();
             if (resutlAvailable) {
             	testResultFiles = testResultFiles(appDirName, testAgainsts, false, Constants.PHASE_LOAD_TEST);
             }
             
             loadMap.put(TEST_AGAINSTS, testAgainsts);
             loadMap.put(RESULT_AVAILABLE, resutlAvailable);
             loadMap.put(TEST_RESULT_FILES, testResultFiles);
             
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, null,
 					loadMap, RESPONSE_STATUS_SUCCESS, PHRQ600001);
 			return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 		} catch (Exception e) {
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHRQ610001);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
 					.build();
 		}
 	}
 	
 	/**
 	 * load.
 	 *
 	 * @param appDirName the app dir name
 	 * @param userId the user id
 	 * @return the response
 	 */
 	@GET
 	@Path(REST_API_LOAD_RESULTS)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response loadTestResults(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_TEST_AGAINST) String testAgainst, 
 			@QueryParam(REST_QUERY_RESULT_FILE_NAME) String resultFileName) {
 		ResponseInfo<List<TestResult>> responseData = new ResponseInfo<List<TestResult>>();
 		try {
 			List<TestResult> testResults = getLoadTestResult(appDirName, testAgainst, resultFileName, Constants.PHASE_LOAD_TEST);
 			Gson gson = new Gson();
 			StringBuilder jSon = new StringBuilder();
 			StringBuilder data = new StringBuilder();
 			jSon.append(GRAPH_JSON);
 			data.append(SQUARE_OPEN);
 			for (TestResult testResult : testResults) {
 				jSon.append(gson.toJson(testResult));
 				data.append(SQUARE_OPEN);
 				data.append(testResults.indexOf(testResult));
 				data.append(Constants.COMMA);
 				data.append(testResult.getTime());
 				data.append(SQUARE_CLOSE);
 				if(testResults.indexOf(testResult) < testResults.size() - 1) {
 					jSon.append(Constants.COMMA);
 					data.append(Constants.COMMA);
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
 			script.append("var flagData = '';");
 			script.append(SCRIPT_END);
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, null,
 					testResults, RESPONSE_STATUS_SUCCESS, PHRQ600002);
 			return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
 		} catch (Exception e) {
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHRQ610002);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
 					.build();
 		}
 	}
 
 	private List<TestResult> getLoadTestResult(String appDirName, String testAgainst, String resultFileName, String action) throws TransformerException, PhrescoException, ParserConfigurationException, SAXException, IOException {
 		List<TestResult> testResults = new ArrayList<TestResult>(2);
 		try {
 			String testResultPath = getLoadOrPerformanceTestResultPath(appDirName, testAgainst, resultFileName, action);
 			
 			if (!new File(testResultPath).exists()) {
 				throw new PhrescoException(RESULT_FILE_NOT_FOUND);
 			} 
 			
 			Document doc = getDocument(new File(testResultPath)); 
 			NodeList nodeList = org.apache.xpath.XPathAPI.selectNodeList(doc, XPATH_TEST_RESULT);
 
 			TestResult testResult = null;
 
 			for (int i = 0; i < nodeList.getLength(); i++) {
 				testResult =  new TestResult();
 				Node node = nodeList.item(i);
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
 			throw new PhrescoException(e);
 		}
 		return testResults;
 	}
 	
 	/**
 	 * Sets the sets the failure test cases.
 	 *
 	 * @param setFailureTestCases the new sets the failure test cases
 	 */
 	public void setSetFailureTestCases(int setFailureTestCases) {
 		this.setFailureTestCases = setFailureTestCases;
 	}
 
 	/**
 	 * Gets the sets the failure test cases.
 	 *
 	 * @return the sets the failure test cases
 	 */
 	public int getSetFailureTestCases() {
 		return setFailureTestCases;
 	}
 
 	/**
 	 * Sets the error test cases.
 	 *
 	 * @param errorTestCases the new error test cases
 	 */
 	public void setErrorTestCases(int errorTestCases) {
 		this.errorTestCases = errorTestCases;
 	}
 
 	/**
 	 * Gets the error test cases.
 	 *
 	 * @return the error test cases
 	 */
 	public int getErrorTestCases() {
 		return errorTestCases;
 	}
 
 	/**
 	 * Sets the node length.
 	 *
 	 * @param nodeLength the new node length
 	 */
 	public void setNodeLength(int nodeLength) {
 		this.nodeLength = nodeLength;
 	}
 
 	/**
 	 * Gets the node length.
 	 *
 	 * @return the node length
 	 */
 	public int getNodeLength() {
 		return nodeLength;
 	}
 
 	/**
 	 * Gets the test suite.
 	 *
 	 * @return the test suite
 	 */
 	public String getTestSuite() {
 		return testSuite;
 	}
 
 	/**
 	 * Sets the test suite.
 	 *
 	 * @param testSuite the new test suite
 	 */
 	public void setTestSuite(String testSuite) {
 		this.testSuite = testSuite;
 	}
 }
