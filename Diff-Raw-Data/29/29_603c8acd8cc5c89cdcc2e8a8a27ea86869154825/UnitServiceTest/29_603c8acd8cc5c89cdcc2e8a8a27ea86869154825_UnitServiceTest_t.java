 	package com.photon.phresco.framework.rest.api;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Response;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.springframework.mock.web.MockHttpServletRequest;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.model.TestCase;
 import com.photon.phresco.framework.rest.api.util.ActionResponse;
 
 public class UnitServiceTest extends RestBaseTest {
 
 	QualityService service = new QualityService();
 	ActionService actionService = new ActionService();
 
 	@Test
 	public void testUnitTestReportOption() throws PhrescoException, IOException {
 		Response response = service.unit(appDirName, userId, "");
 		ResponseInfo<List<String>> responseInfo = (ResponseInfo<List<String>>) response.getEntity();
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	
 	@Test
 	public void testUnitTestReportOptionWithoutDirName() throws PhrescoException, IOException {
 		Response response = service.unit("", userId, "");
 		ResponseInfo<List<String>> responseInfo = (ResponseInfo<List<String>>) response.getEntity();
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	@Test
 	public void trigerTest() throws PhrescoException {
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		request.setParameter("testAgainst", "java");
 		request.setParameter("customerId", customerId);
 		request.setParameter("projectId", projectId);
 		request.setParameter("appId", appCode);
 		request.setParameter("username", userId);
 		request.setParameter("appDirName", "TestProject");
 		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
 		Response response = actionService.runUnitTest(httpServletRequest);
 		ActionResponse entity = (ActionResponse) response.getEntity();
 		String uniqueKey = entity.getUniquekey();
 		readLog(uniqueKey);
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	@Test
 	public void trigerTestWithoutAppId() throws PhrescoException {
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		request.setParameter("testAgainst", "java");
 		request.setParameter("customerId", customerId);
 		request.setParameter("projectId", projectId);
 		request.setParameter("appId", "");
 		request.setParameter("username", userId);
 		request.setParameter("appDirName", "TestProject");
 		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
 		Response response = actionService.runUnitTest(httpServletRequest);
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	
 	@Test
 	public void trigerTestWithoutCustomerId() throws PhrescoException {
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		request.setParameter("testAgainst", "java");
 		request.setParameter("customerId", "");
 		request.setParameter("projectId", projectId);
 		request.setParameter("appId", appCode);
 		request.setParameter("username", userId);
 		request.setParameter("appDirName", "TestProject");
 		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
 		Response response = actionService.runUnitTest(httpServletRequest);
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	@Test
 	public void trigerTestWithoutProjectId() throws PhrescoException {
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		request.setParameter("testAgainst", "java");
 		request.setParameter("customerId", customerId);
 		request.setParameter("projectId", "");
 		request.setParameter("appId", appCode);
 		request.setParameter("username", userId);
 		request.setParameter("appDirName", "TestProject");
 		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
 		Response response = actionService.runUnitTest(httpServletRequest);
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	@Test
 	public void trigerTestWithoutUserName() throws PhrescoException {
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		request.setParameter("testAgainst", "java");
 		request.setParameter("customerId", customerId);
 		request.setParameter("projectId", "");
 		request.setParameter("appId", appCode);
 		request.setParameter("username", "");
 		request.setParameter("appDirName", "TestProject");
 		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
 		Response response = actionService.runUnitTest(httpServletRequest);
 		Assert.assertEquals(200, response.getStatus());
 	} 
 	
 
 	@Test
 	public void testGetTestSuites() throws PhrescoException, IOException {
		Response response = service.getTestSuites(appDirName, "unit", "java", "", "");
 		ResponseInfo<List<String>> responseInfo =  (ResponseInfo<List<String>>) response.getEntity();
		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	
 	@Test
 	public void testGetTestSuitesWithOutAppDir() throws PhrescoException, IOException {
		Response response = service.getTestSuites("", "unit", "java", "", "");
 		ResponseInfo<List<String>> responseInfo =  (ResponseInfo<List<String>>) response.getEntity();
		Assert.assertEquals(200, response.getStatus());
 	}
 
 	
 	@Test
 	public void testGetTestSuitesWithOutTestType() throws PhrescoException, IOException {
		Response response = service.getTestSuites(appDirName, "", "java", "", "");
 		ResponseInfo<List<String>> responseInfo =  (ResponseInfo<List<String>>) response.getEntity();
		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	
 	@Test
 	public void testGetTestSuitesWithOutTestReport() throws PhrescoException, IOException {
		Response response = service.getTestSuites(appDirName, "unit", "", "", "");
 		ResponseInfo<List<String>> responseInfo =  (ResponseInfo<List<String>>) response.getEntity();
		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	
 	
 	@Test
 	public void testGetTestReports() throws PhrescoException, IOException {
		Response response = service.getTestReports(appDirName, "unit", "java", "", "com.photon.phresco.service.TestCase", "");
 		ResponseInfo<List<TestCase>> responseInfo =  (ResponseInfo<List<TestCase>>) response.getEntity();
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	@Test
 	public void testGetTestReportsAll() throws PhrescoException, IOException {
		Response response = service.getTestReports(appDirName, "unit", "java", "", "All", "");
 		ResponseInfo<List<TestCase>> responseInfo =  (ResponseInfo<List<TestCase>>) response.getEntity();
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 }
