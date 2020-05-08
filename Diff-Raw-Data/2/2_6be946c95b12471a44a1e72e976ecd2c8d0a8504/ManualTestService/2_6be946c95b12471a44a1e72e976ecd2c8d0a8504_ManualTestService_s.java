 package com.photon.phresco.framework.rest.api;
 
 import java.io.File;
 import java.io.InputStream;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.ResponseCodes;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.model.ManualTestResult;
 import com.photon.phresco.framework.model.TestSuite;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.ServiceConstants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 import com.sun.jersey.api.client.ClientResponse.Status;
 
 
 @Path(ServiceConstants.REST_API_MANUAL)
 public class ManualTestService extends RestBase implements ServiceConstants, FrameworkConstants, ResponseCodes {
 	
 	@GET
 	@Path(REST_API_MANUALTEMPLATE)
 	@Produces(MediaType.APPLICATION_OCTET_STREAM)
 	public Response getManualTemplate(@QueryParam(REST_QUERY_FILETYPE) String fileType) throws PhrescoException {
 		ResponseInfo info = new ResponseInfo();
         try {
         	InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream("phtn_phresco - 3.2.0 manual_test_template." + fileType);
         	if(resourceStream == null) {
         		return Response.status(Status.OK).header("Access-Control-Allow-Origin", "*").entity(resourceStream).build();
         	}
         	
        	return Response.status(Status.OK).entity(resourceStream).header("Content-Disposition", "attachment; filename=helios_manul_test_template." + fileType).build();
         } catch (Exception e) {
         	ResponseInfo finalOutput = responseDataEvaluation(info, e,
 					null, RESPONSE_STATUS_ERROR, PHRQ410001);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 	
 	@POST
 	@Path(REST_API_UPLOADTEMPLATE)
 	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response uploadManualTemplate(@Context HttpServletRequest request, @QueryParam(REST_QUERY_APPDIR_NAME) String appDirName) 
 		throws PhrescoException {
 		ResponseInfo response = new ResponseInfo();
         try {
         	String uploadedFileName = request.getHeader(FrameworkConstants.X_FILE_NAME);
         	String fileName = URLDecoder.decode(uploadedFileName, "UTF-8");
         	InputStream inputStream = request.getInputStream();
         	String moduleName = request.getHeader("moduleName");
         	String rootModulePath = "";
 			String subModuleName = "";
 			if (StringUtils.isNotEmpty(moduleName)) {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 				subModuleName = moduleName;
 			} else {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 			}
 			
 			String manualTestPath = getManualTestReportDir(rootModulePath, subModuleName);
         	ProjectInfo projectinfo = Utility.getProjectInfo(rootModulePath, subModuleName);
         	File manualDir = Utility.getTestFolderLocation(projectinfo, rootModulePath, subModuleName);
         	StringBuilder builder = new StringBuilder(manualDir.toString());
     		builder.append(manualTestPath);
     		File existingFile = new File(builder.toString());
     		FileUtils.cleanDirectory(existingFile); 
     		File file = new File(builder.toString() + File.separator + fileName);
     		FileUtils.copyInputStreamToFile(inputStream, file);
         } catch (Exception e) {
         	ResponseInfo finalOutput = responseDataEvaluation(response, e,
 					null, RESPONSE_STATUS_ERROR, PHRQ410002);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
         ResponseInfo responseData = responseDataEvaluation(response, null, null, RESPONSE_STATUS_SUCCESS, PHRQ400001);
 		return Response.status(Status.OK).header("Access-Control-Allow-Origin", "*").entity(responseData).build();
 	}
 	
 	@GET
 	@Path(REST_API_TESTSUITES)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getTestSuites(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName) throws PhrescoException {
 		ResponseInfo<List<TestSuite>> responseData = new ResponseInfo<List<TestSuite>>();
 		List<TestSuite> readManualTestSuiteFile = new ArrayList<TestSuite>();
 		ManualTestResult createManualTestResult = null;
 		ResponseInfo<List<TestSuite>> finalOutput = null;
 		try {
 			String rootModulePath = "";
 			String subModuleName = "";
 			if (StringUtils.isNotEmpty(moduleName)) {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 				subModuleName = moduleName;
 			} else {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 			}
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			ProjectInfo projectinfo = Utility.getProjectInfo(rootModulePath, subModuleName);
         	File manualDir = Utility.getTestFolderLocation(projectinfo, rootModulePath, subModuleName);
 			String manualTestDir = getManualTestReportDir(rootModulePath, subModuleName);
 			StringBuilder sb = new StringBuilder(manualDir.toString()).append(manualTestDir);
 			File manualFilePath = new File(sb.toString());
 			if (!manualFilePath.exists()) {
 				finalOutput = responseDataEvaluation(responseData, null, createManualTestResult, RESPONSE_STATUS_FAILURE, PHRQ000003);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 			}
 			readManualTestSuiteFile = frameworkUtil.readManualTestSuiteFile(sb.toString());
 			createManualTestResult = createManualTestResult(readManualTestSuiteFile);
 			finalOutput = responseDataEvaluation(responseData, null, createManualTestResult, RESPONSE_STATUS_SUCCESS, PHRQ000003);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (Exception e) {
 			finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHRQ410004);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} 
 	}
 	
 	@DELETE
 	@Path(REST_API_TESTSUITES_DELETE)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response deleteTestSuites(@QueryParam(REST_API_TESTSUITE) String testSuiteName, @QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_MODULE_NAME) String moduleName) throws PhrescoException {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		ManualTestResult createManualTestResult = null;
 		ResponseInfo<Boolean> finalOutput = null;
 		try {
 			String rootModulePath = "";
 			String subModuleName = "";
 			if (StringUtils.isNotEmpty(moduleName)) {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 				subModuleName = moduleName;
 			} else {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 			}
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			ProjectInfo projectinfo = Utility.getProjectInfo(rootModulePath, subModuleName);
         	File manualDir = Utility.getTestFolderLocation(projectinfo, rootModulePath, subModuleName);
 			String manualTestDir = getManualTestReportDir(rootModulePath, subModuleName);
 			StringBuilder sb = new StringBuilder(manualDir.toString()).append(manualTestDir);
 			File manualFilePath = new File(sb.toString());
 			if (!manualFilePath.exists()) {
 				finalOutput = responseDataEvaluation(responseData, null, createManualTestResult, RESPONSE_STATUS_FAILURE, PHRQ000003);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 			}
 			boolean deleteManualTestSuiteFile = frameworkUtil.deleteManualTestSuiteFile(sb.toString(), testSuiteName);
 			finalOutput = responseDataEvaluation(responseData, null, deleteManualTestSuiteFile, RESPONSE_STATUS_SUCCESS, PHRQ400008);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (Exception e) {
 			finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHRQ410004);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} 
 	}
 	
 	@GET
 	@Path(REST_API_TESTCASES)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getTestCase(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, 
 			@QueryParam("testSuiteName") String testsuitename, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName) throws PhrescoException {
 		ResponseInfo<List<com.photon.phresco.commons.model.TestCase>> responseData = new 
 			ResponseInfo<List<com.photon.phresco.commons.model.TestCase>>();
 		List<com.photon.phresco.commons.model.TestCase> readTestCase = null;
 		try {
 			String rootModulePath = "";
 			String subModuleName = "";
 			if (StringUtils.isNotEmpty(moduleName)) {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 				subModuleName = moduleName;
 			} else {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 			}
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			ProjectInfo projectinfo = Utility.getProjectInfo(rootModulePath, subModuleName);
         	File manualDir = Utility.getTestFolderLocation(projectinfo, rootModulePath, subModuleName);
 			String manualTestDir = getManualTestReportDir(rootModulePath, subModuleName);
 			StringBuilder sb = new StringBuilder(manualDir.toString()).append(manualTestDir);
 			readTestCase = frameworkUtil.readManualTestCaseFile(sb.toString(), testsuitename, null);
 			ResponseInfo<List<com.photon.phresco.commons.model.TestCase>> finalOutput = 
 				responseDataEvaluation(responseData, null, readTestCase, RESPONSE_STATUS_SUCCESS, PHRQ400003);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (Exception e) {
 			ResponseInfo<List<com.photon.phresco.commons.model.TestCase>> finalOutput = 
 				responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHRQ410005);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		}
 	}
 	
 	@POST
 	@Path(REST_API_TESTSUITES)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response createTestSuite(@QueryParam(REST_API_TESTSUITE) String testSuiteName, 
 			@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName) throws PhrescoException {
 		ResponseInfo responseData = new ResponseInfo();
 		try {
 			String rootModulePath = "";
 			String subModuleName = "";
 			if (StringUtils.isNotEmpty(moduleName)) {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 				subModuleName = moduleName;
 			} else {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 			}
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			ProjectInfo projectinfo = Utility.getProjectInfo(rootModulePath, subModuleName);
         	File manualDir = Utility.getTestFolderLocation(projectinfo, rootModulePath, subModuleName);
 			String manualTestDir = getManualTestReportDir(rootModulePath, subModuleName);
 			StringBuilder sb = new StringBuilder(manualDir.toString()).append(manualTestDir);
 			String cellValue[] = {"", "", testSuiteName, "", "", "", "", "", "", "", "", "", ""};
 			frameworkUtil.addNew(sb.toString(), testSuiteName, cellValue);
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_SUCCESS, PHRQ400004);
 			return Response.status(Status.OK).entity(finalOutput).build();
 		} catch(PhrescoException e) {
 			e.printStackTrace();
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHRQ410006);
 			return Response.status(Status.OK).entity(finalOutput).build();
 		}
 	}
 	
 	@POST
 	@Path(REST_API_TESTCASES)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes (MediaType.APPLICATION_JSON)
 	public Response createTestCase(com.photon.phresco.commons.model.TestCase testCase, @QueryParam("testSuiteName") String testSuiteName,
 			@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName) throws PhrescoException {
 		ResponseInfo responseData = new ResponseInfo();
 		try {
 			String rootModulePath = "";
 			String subModuleName = "";
 			if (StringUtils.isNotEmpty(moduleName)) {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 				subModuleName = moduleName;
 			} else {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 			}
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			ProjectInfo projectinfo = Utility.getProjectInfo(rootModulePath, subModuleName);
         	File manualDir = Utility.getTestFolderLocation(projectinfo, rootModulePath, subModuleName);
 			String manualTestDir = getManualTestReportDir(rootModulePath, subModuleName);
 			StringBuilder sb = new StringBuilder(manualDir.toString()).append(manualTestDir);
 			String cellValue[] = {"", testCase.getFeatureId(), "",testCase.getTestCaseId(), testCase.getDescription(), testCase.getPreconditions(),testCase.getSteps(), "", "",
 					testCase.getExpectedResult(), testCase.getActualResult(), testCase.getStatus(), testCase.getBugComment()};
 			frameworkUtil.addNewTestCase(sb.toString(), testSuiteName,cellValue, testCase.getStatus());
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, null, testCase, RESPONSE_STATUS_SUCCESS, PHRQ400005);
 			return Response.status(Status.OK).entity(finalOutput).build();
 		} catch(PhrescoException e) {
 			e.printStackTrace();
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHRQ410007);
 			return Response.status(Status.OK).entity(finalOutput).build();
 		}
 	}
 	
 	@PUT
 	@Path("/testcases")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes (MediaType.APPLICATION_JSON)
 	public Response updateTestCase(com.photon.phresco.commons.model.TestCase testCase, @QueryParam("testSuiteName") String testSuiteName,
 			@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName) throws PhrescoException {
 		ResponseInfo responseData = new ResponseInfo();
 		try {
 			String rootModulePath = "";
 			String subModuleName = "";
 			if (StringUtils.isNotEmpty(moduleName)) {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 				subModuleName = moduleName;
 			} else {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 			}
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			ProjectInfo projectinfo = Utility.getProjectInfo(rootModulePath, subModuleName);
         	File manualDir = Utility.getTestFolderLocation(projectinfo, rootModulePath, subModuleName);
 			String manualTestDir = getManualTestReportDir(rootModulePath, subModuleName);
 			StringBuilder sb = new StringBuilder(manualDir.toString()).append(manualTestDir);
 			frameworkUtil.readManualTestCaseFile(sb.toString(), testSuiteName, testCase);
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, null, testCase, RESPONSE_STATUS_SUCCESS, PHRQ400006);
 			return Response.status(Status.OK).entity(finalOutput).build();
 		} catch(PhrescoException e) {
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHRQ410008);
 			return Response.status(Status.OK).entity(finalOutput).build();			
 		}
 	}
 	
 
 	@DELETE
 	@Path(REST_API_TESTCASE_DELETE)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response deleteTestCase(@QueryParam(REST_API_TESTSUITE) String testSuiteName, @QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_TESTCASE_NAME) String testCaseId, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName) throws PhrescoException {
 			ResponseInfo<String> responseData = new ResponseInfo<String>();
 			List<com.photon.phresco.commons.model.TestCase> readTestCase = null;
 			boolean hasTrue = false;
 			try {
 				String rootModulePath = "";
 				String subModuleName = "";
 				if (StringUtils.isNotEmpty(moduleName)) {
 					rootModulePath = Utility.getProjectHome() + appDirName;
 					subModuleName = moduleName;
 				} else {
 					rootModulePath = Utility.getProjectHome() + appDirName;
 				}
 				FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 				ProjectInfo projectinfo = Utility.getProjectInfo(rootModulePath, subModuleName);
 	        	File manualDir = Utility.getTestFolderLocation(projectinfo, rootModulePath, subModuleName);
 				String manualTestDir = getManualTestReportDir(rootModulePath, subModuleName);
 				StringBuilder sb = new StringBuilder(manualDir.toString()).append(manualTestDir);
 				hasTrue = frameworkUtil.deleteManualTestCaseFile(sb.toString(), testSuiteName, testCaseId);
 				ResponseInfo<Boolean> finalOutput = 
 					responseDataEvaluation(responseData, null, hasTrue, RESPONSE_STATUS_SUCCESS, PHRQ400009);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 			} catch (Exception e) {
 				ResponseInfo<Boolean> finalOutput = 
 					responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHRQ410010);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 			}
 	}
 	
 	@GET
 	@Path(REST_API_TESTCASE_VALIDATION)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response testCaseValidation(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_TESTCASE_NAME) String testCaseId,
 			@QueryParam("testSuiteName") String testsuitename, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName) throws PhrescoException {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		List<com.photon.phresco.commons.model.TestCase> readTestCase = null;
 		boolean hasError = true;
 		try {
 			String rootModulePath = "";
 			String subModuleName = "";
 			if (StringUtils.isNotEmpty(moduleName)) {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 				subModuleName = moduleName;
 			} else {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 			}
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			ProjectInfo projectinfo = Utility.getProjectInfo(rootModulePath, subModuleName);
         	File manualDir = Utility.getTestFolderLocation(projectinfo, rootModulePath, subModuleName);
 			String manualTestDir = getManualTestReportDir(rootModulePath, subModuleName);
 			StringBuilder sb = new StringBuilder(manualDir.toString()).append(manualTestDir);
 			readTestCase = frameworkUtil.readManualTestCaseFile(sb.toString(), testsuitename, null);
 			for (com.photon.phresco.commons.model.TestCase testCase : readTestCase) {
 				if(testCase.getTestCaseId().equalsIgnoreCase(testCaseId)) {
 					hasError = false;
 					break;
 				}
 			}
 			
 			ResponseInfo<Boolean> finalOutput = 
 				responseDataEvaluation(responseData, null, hasError, RESPONSE_STATUS_SUCCESS, PHRQ400007);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (Exception e) {
 			ResponseInfo<Boolean> finalOutput = 
 				responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHRQ410009);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		}
 	}
 	
 	
 	private ManualTestResult createManualTestResult(List<TestSuite> readManualTestSuiteFile) {
 		ManualTestResult mtr = new ManualTestResult();
 		float totalTestSuccess = 0;
 		float totalFailure = 0;
 		float totalNotApplicable = 0;
 		float totalBlocked = 0;
 		float totalNotExecuted = 0;
 		float total = 0;
 		float totalTestCoverage = 0;
 		for (TestSuite testSuite : readManualTestSuiteFile) {
 			totalTestSuccess =  totalTestSuccess + testSuite.getSuccess();
 			totalFailure = totalFailure + testSuite.getFailures();
 			totalNotApplicable = totalNotApplicable + testSuite.getNotApplicable();
 			totalBlocked = totalBlocked + testSuite.getBlocked();
 			totalNotExecuted = totalNotExecuted + testSuite.getNotExecuted();
 			total = total + testSuite.getTotal();
 			totalTestCoverage = totalTestCoverage + testSuite.getTestCoverage();
 		}
 		mtr.setTestSuites(readManualTestSuiteFile);
 		mtr.setTotalSuccess(totalTestSuccess);
 		mtr.setTotalFailure(totalFailure);
 		mtr.setTotalNotApplicable(totalNotApplicable);
 		mtr.setTotalBlocked(totalBlocked);
 		mtr.setTotalNotExecuted(totalNotExecuted);
 		mtr.setTotal(total);
 		mtr.setTotalTestCoverage(totalTestCoverage);
 		return mtr;
 	}
 
 	private String getManualTestReportDir(String rootModulePath, String subModuleName) throws PhrescoException {
         try {
         	PomProcessor pomProcessor = Utility.getPomProcessor(rootModulePath, subModuleName);
         	String manualDir = pomProcessor.getProperty(Constants.POM_PROP_KEY_MANUALTEST_RPT_DIR);
         	return manualDir;
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
     }
 }
