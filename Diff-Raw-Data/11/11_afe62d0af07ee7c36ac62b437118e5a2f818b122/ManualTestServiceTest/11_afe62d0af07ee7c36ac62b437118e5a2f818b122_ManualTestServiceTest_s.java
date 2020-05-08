 package com.photon.phresco.framework.rest.api;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import javax.servlet.ServletInputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.core.Response;
 
 import junit.framework.Assert;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Test;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 
 import com.photon.phresco.commons.model.TestCase;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.model.TestSuite;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.util.PomProcessor;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 public class ManualTestServiceTest extends RestBaseTest {
 	
 	private final String testSuiteName = "manualtesting";
 	
 	@Test
 	public void testGetManualTemplate() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response manualTemplate = service.getManualTemplate("xls");
 		InputStream entity = (InputStream) manualTemplate.getEntity();
 		Assert.assertNotNull(entity);
 		try {
 			Assert.assertEquals(entity.read(), 208);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void getManualTemplateTest() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response manualTemplate = service.getManualTemplate("");
 		InputStream entity = (InputStream) manualTemplate.getEntity();
 	}
 	
 	@Test
 	public void testUploadManualTemplate() throws PhrescoException {
 		try {
 			MockHttpServletRequest request = new MockHttpServletRequest();
 			request.setServletPath("/rest/api");
 			request.setContextPath("/framework");
 			request.setPathInfo("/manual/uploadTemplate");
 			HttpServletRequest httpServletRequest = (HttpServletRequest)request;
 			
 			ManualTestService service = new ManualTestService();
 			PomProcessor pomProcessor = FrameworkUtil.getInstance().getPomProcessor(appDirName);
 	    	String manualTestReportPath = pomProcessor.getProperty("phresco.manualTest.testcase.path");
 	    	StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 			builder.append(appDirName);
 			builder.append(File.separator);
 			builder.append(manualTestReportPath);
			InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream("helios_manul_test_template.xls");
			File file = new File(builder.toString() + "/helios_manul_test_template.xls");
 			FileUtils.copyInputStreamToFile(resourceStream, file);
 			service.uploadManualTemplate(httpServletRequest, manualTestReportPath);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 //	@Test
 //	public void uploadManualTemplateTest() throws PhrescoException {
 //		try {
 //			MockHttpServletRequest request = new MockHttpServletRequest();
 //			request.setServletPath("/rest/api");
 //			request.setContextPath("/framework");
 //			request.setPathInfo("/manual/uploadTemplate");
 //			HttpServletRequest httpServletRequest = (HttpServletRequest)request;
 //			MockHttpServletResponse response = new MockHttpServletResponse();
 //			HttpServletResponse httpServletResponse = (HttpServletResponse)response;
 //			httpServletResponse.setHeader("X-File-Name", "helios_manul_test_template.xls");
 //			
 //			
 //			
 //			ManualTestService service = new ManualTestService();
 //			PomProcessor pomProcessor = FrameworkUtil.getInstance().getPomProcessor(appDirName);
 //	    	String manualTestReportPath = pomProcessor.getProperty("phresco.manualTest.testcase.path");
 //	    	StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 //			builder.append(appDirName);
 //			builder.append(File.separator);
 //			builder.append(manualTestReportPath);
 //			InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream("helios_manul_test_template.xls");
 //			
 ////			httpServletResponse.set
 //			
 //			File file = new File(builder.toString() + "/helios_manul_test_template.xls");
 //			FileUtils.copyInputStreamToFile(resourceStream, file);
 //			service.uploadManualTemplate(httpServletRequest, manualTestReportPath);
 //		} catch (Exception e) {
 //			throw new PhrescoException(e);
 //		}
 //	}
 	
 	@Test
 	public void testGetTestSuites() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response testSuites = service.getTestSuites(appDirName, "");
 		ResponseInfo<List<TestSuite>> response = (ResponseInfo<List<TestSuite>>) testSuites.getEntity();
 		
 		Assert.assertEquals(200, testSuites.getStatus());
 		Assert.assertEquals("PHRQ000003", response.getResponseCode());
 	}
 
 	@Test
 	public void getTestSuitesTest() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response testSuites = service.getTestSuites("", "");
 		ResponseInfo<List<TestSuite>> response = (ResponseInfo<List<TestSuite>>) testSuites.getEntity();
 		Assert.assertEquals(200, testSuites.getStatus());
 		Assert.assertEquals("PHRQ410004", response.getResponseCode());
 	}
 
 	
 	@Test
 	public void testGetTestCase() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response testCase = service.getTestCase(appDirName, "Login", "");
 		ResponseInfo<List<com.photon.phresco.commons.model.TestCase>> response = (ResponseInfo<List<TestCase>>) testCase.getEntity();
 		List<TestCase> data = response.getData();
 		
 		Assert.assertEquals(200, testCase.getStatus());
 		Assert.assertEquals("PHRQ400003", response.getResponseCode());
 		Assert.assertNotNull(data);
 	}
 
 	@Test
 	public void getTestCaseTest() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response testCase = service.getTestCase("", "Login", "");
 		ResponseInfo<List<com.photon.phresco.commons.model.TestCase>> response = (ResponseInfo<List<TestCase>>) testCase.getEntity();
 		Assert.assertEquals(200, testCase.getStatus());
 		Assert.assertEquals("PHRQ410005", response.getResponseCode());
 	}
 	
 	@Test
 	public void testCreateTestSuite() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response response = service.createTestSuite(testSuiteName, appDirName, "");
 		ResponseInfo responseInfo = (ResponseInfo) response.getEntity();
 		Assert.assertEquals(200, response.getStatus());
 		Assert.assertEquals("PHRQ400004", responseInfo.getResponseCode());
 	}
 
 
 	@Test
 	public void createTestSuiteTest() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response response = service.createTestSuite("", "", "");
 		ResponseInfo responseInfo = (ResponseInfo) response.getEntity();
 		Assert.assertEquals("PHRQ410006", responseInfo.getResponseCode());
 	}
 	
 	@Test
 	public void testValidateTestCase() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response response = service.testCaseValidation(appDirName, "Manual Test TestCase ID", testSuiteName, null);
 		ResponseInfo responseInfo = (ResponseInfo) response.getEntity();
 		Assert.assertEquals("success", responseInfo.getStatus());
 	}
 	
 	@Test
 	public void validateTestCaseTest() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response response = service.testCaseValidation("", "Manual Test TestCase ID", "", null);
 		ResponseInfo responseInfo = (ResponseInfo) response.getEntity();
 		Assert.assertEquals("PHRQ410009", responseInfo.getResponseCode());
 	}
 	
 	
 	@Test
 	public void testCreateTestCase() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response response = service.createTestCase(createTestCase() , testSuiteName, appDirName, "");
 		ResponseInfo responseInfo = (ResponseInfo) response.getEntity();
 		Assert.assertEquals(200, response.getStatus());
 		Assert.assertEquals("PHRQ400005", responseInfo.getResponseCode());
 	}
 
 	@Test
 	public void createTestCaseTest() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response response = service.createTestCase(createTestCase() , testSuiteName, "", "");
 		ResponseInfo responseInfo = (ResponseInfo) response.getEntity();
 		Assert.assertEquals("PHRQ410007", responseInfo.getResponseCode());
 	}
 	
 	@Test
 	public void testUpdateTestCase() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		TestCase testCase = createTestCase();
 		testCase.setDescription("Manual Testing Description Updated");
 		testCase.setExpectedResult("Manual Test Expected Result Updated");
 		testCase.setFeatureId("Manual Test Feature ID Updated");
 		testCase.setTestCaseId("Manual Test TestCase ID");
 		Response response = service.updateTestCase(testCase, testSuiteName, appDirName, "");
 		ResponseInfo responseInfo = (ResponseInfo) response.getEntity();
 		
 		Assert.assertEquals(200, response.getStatus());
 		Assert.assertEquals("PHRQ400006", responseInfo.getResponseCode());
 	}
 	
 	@Test
 	public void updateTestCaseTest() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		TestCase testCase = createTestCase();
 		testCase.setDescription("Manual Testing Description Updated");
 		testCase.setExpectedResult("Manual Test Expected Result Updated");
 		testCase.setFeatureId("Manual Test Feature ID Updated");
 		testCase.setTestCaseId("Manual Test TestCase ID");
 		Response response = service.updateTestCase(testCase, testSuiteName, "", "");
 		ResponseInfo responseInfo = (ResponseInfo) response.getEntity();
 		Assert.assertEquals("PHRQ410008", responseInfo.getResponseCode());
 	}
 	
 	@Test
 	public void deleteTestCase() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response deleteTestCase = service.deleteTestCase(testSuiteName, appDirName, "Manual Test TestCase ID", null);
 		ResponseInfo responseInfo = (ResponseInfo) deleteTestCase.getEntity();
 		Assert.assertEquals("success", responseInfo.getStatus());
 	}
 	
 	@Test
 	public void deleteTestCaseTest() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response deleteTestCase = service.deleteTestCase(testSuiteName, "", "Manual Test TestCase ID", null);
 		ResponseInfo responseInfo = (ResponseInfo) deleteTestCase.getEntity();
 		Assert.assertEquals("PHRQ410010",responseInfo.getResponseCode());
 	}
 	
 	@Test
 	public void deleteTestSuite() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response deleteTestSuites = service.deleteTestSuites(testSuiteName, appDirName, null);
 		ResponseInfo responseInfo = (ResponseInfo) deleteTestSuites.getEntity();
 		Assert.assertEquals("success", responseInfo.getStatus());
 	}
 	
 	@Test
 	public void deleteTestSuiteTest() throws PhrescoException {
 		ManualTestService service = new ManualTestService();
 		Response deleteTestSuites = service.deleteTestSuites(testSuiteName, "", null);
 		ResponseInfo responseInfo = (ResponseInfo) deleteTestSuites.getEntity();
 		Assert.assertEquals("PHRQ410004", responseInfo.getResponseCode());
 	}
 	
 	private TestCase createTestCase() {
 		TestCase testCase = new TestCase();
 		testCase.setBugComment("Manual Testing Comment");
 		testCase.setDescription("Manual Testing Description");
 		testCase.setExpectedResult("Manual Test Expected Result");
 		testCase.setFeatureId("Manual Test Feature ID");
 		testCase.setTestCaseId("Manual Test TestCase ID");
 		testCase.setRequirementId("Manual Test Req Id");
 		testCase.setStatus("Success");
 		testCase.setSteps("Manual Test Steps");
 		return testCase;
 	}
 
 }
