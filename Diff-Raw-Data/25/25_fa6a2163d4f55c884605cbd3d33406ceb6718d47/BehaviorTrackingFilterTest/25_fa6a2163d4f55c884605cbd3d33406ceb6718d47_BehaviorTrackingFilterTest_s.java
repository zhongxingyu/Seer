 /* 
  * Copyright 2008-2009 the original author or authors.
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  */
  
 package com.mtgi.analytics.servlet;
 
 import static java.util.Arrays.asList;
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.net.URL;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import junit.framework.AssertionFailedError;
 
 import org.apache.commons.httpclient.NameValuePair;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.FormEncodingType;
 import com.gargoylesoftware.htmlunit.SubmitMethod;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.WebRequestSettings;
 import com.mtgi.analytics.BehaviorTrackingManager;
 import com.mtgi.analytics.BehaviorTrackingManagerImpl;
 import com.mtgi.analytics.JdbcEventTestCase;
 import com.mtgi.analytics.MockSessionContext;
 import com.mtgi.test.unitils.tomcat.annotations.DeployDescriptor;
 import com.mtgi.test.unitils.tomcat.annotations.EmbeddedTomcat;
 
 @DeployDescriptor(contextRoot="/app", webXml="com/mtgi/analytics/servlet/web.xml")
 public class BehaviorTrackingFilterTest extends JdbcEventTestCase {
 
 	private static TestServlet servlet;
 	private static BehaviorTrackingManagerImpl manager;
 	private WebClient webClient;
 	
 	@EmbeddedTomcat(start=true)
 	private String baseUrl;
 
 	@Before
 	public void setUp() {
 		webClient = new WebClient();
 	}
 	
 	@After
 	public void tearDown() {
 		manager = null;
 		servlet = null;
 		webClient = null;
 	}
 	
 	/** Test basic logging of a GET request */
 	@Test
 	public void testGetRequest() throws Exception {
 	    webClient.getPage(baseUrl + "/app/test/path?param1=hello&param1=world&param2&param3=72%3C");
 	    assertNotNull("Servlet was hit", servlet);
 
 	    //make sure all events are written to database, and verify the db contents.
 	    manager.flush();
 	    assertEventDataMatches("BehaviorTrackingFilterTest.testGetRequest-result.xml");
 	}
 
 	/** Same as testGetRequest, but with POST form parameters instead of URL query string */
 	@Test
 	public void testPostRequest() throws Exception {
 		NameValuePair[] parameters = {
 			new NameValuePair("param1", "hello"),
 			new NameValuePair("param1", "world"),
 			new NameValuePair("param2", ""),
 			new NameValuePair("param3", "72<")
 		};
 		WebRequestSettings request = new WebRequestSettings(new URL(baseUrl + "/app/test/path"));
 		request.setEncodingType(FormEncodingType.URL_ENCODED);
 		request.setSubmitMethod(SubmitMethod.POST);
 		request.setRequestParameters(asList(parameters));
 		
 		webClient.getPage(request);
 		assertNotNull("servlet was hit", servlet);
 	    manager.flush();
 	    assertEventDataMatches("BehaviorTrackingFilterTest.testPostRequest-result.xml");
 	}
 	
 	/** Test the optional inclusion of key parameters with event name */
 	@Test
 	public void testNameParameters() throws Exception {
 		NameValuePair[] parameters = {
 			new NameValuePair("dispatch", "stuff"),
 			new NameValuePair("param1", "hello"),
 			new NameValuePair("param1", "world"),
 			new NameValuePair("param2", ""),
 			new NameValuePair("param3", "72<"),
 			new NameValuePair("dispatch2", "v1")
 		};
 		WebRequestSettings request = new WebRequestSettings(new URL(baseUrl + "/app/test/path"));
 		request.setEncodingType(FormEncodingType.URL_ENCODED);
 		request.setSubmitMethod(SubmitMethod.POST);
 		request.setRequestParameters(asList(parameters));
 		
 		webClient.getPage(request);
 		assertNotNull("servlet was hit", servlet);
 	    manager.flush();
 	    assertEventDataMatches("BehaviorTrackingFilterTest.testNameParameters-result.xml");
 	}
 	
 	/** Verify that tracking events are logged for extension-mapped servlet paths */
 	@Test
 	public void testExtensionMapping() throws Exception {
 	    webClient.getPage(baseUrl + "/app/foo.ext?param1=hello&param1=world&param2&param3=72%3C");
 	    assertNotNull("Servlet was hit", servlet);
 
 	    manager.flush();
 	    assertEventDataMatches("BehaviorTrackingFilterTest.testExtensionMapping-result.xml");
 	}
 	
 	/** verify that server error codes are logged in the event data */
 	@Test
 	public void testErrorStatus() throws Exception {
 		//first test sendError(int, String)
 		TestServlet.status = 403;
 		TestServlet.message = "Authorized personnel only";
 		try {
 			webClient.getPage(baseUrl + "/app/foo.ext?param1=secret");
 			fail("non-OK status should have been sent back by test servlet");
 		} catch (FailingHttpStatusCodeException expected) {
 			assertEquals("Server status code sent back", 403, expected.getStatusCode());
			assertEquals("Authorized personnel only", expected.getStatusMessage());
 		    assertNotNull("Servlet was hit", servlet);
 		}
 		
 		//now sendError(int)
 		TestServlet.status = 401;
 		try {
 			webClient.getPage(baseUrl + "/app/foo.ext?param1=secret");
 			fail("non-OK status should have been sent back by test servlet");
 		} catch (FailingHttpStatusCodeException expected) {
 			assertEquals("Server status code sent back", 401, expected.getStatusCode());
 		    assertNotNull("Servlet was hit", servlet);
 		}
 
 		//now setStatus(int);
 		webClient.setRedirectEnabled(false);
 	    TestServlet.status = 301;
 		try {
 		    webClient.getPage(baseUrl + "/app/redirect.ext");
 			fail("non-OK status should have been sent back by test servlet");
 		} catch (FailingHttpStatusCodeException expected) {
 			assertEquals("Server status code sent back", 301, expected.getStatusCode());
 		    assertNotNull("Servlet was hit", servlet);
 		}
 
 	    //finally setStatus(int,string)
 	    TestServlet.status = 302;
 	    TestServlet.message = "I don't remember what 302 means exactly";
 		try {
 		    webClient.getPage(baseUrl + "/app/status.ext");
 			fail("non-OK status should have been sent back by test servlet");
 		} catch (FailingHttpStatusCodeException expected) {
 			assertEquals("Server status code sent back", 302, expected.getStatusCode());
 		    assertNotNull("Servlet was hit", servlet);
 		}
 
 	    //verify all events logged accordingly.
 	    manager.flush();
 	    assertEventDataMatches("BehaviorTrackingFilterTest.testErrorStatus-result.xml");
 	}
 
 	/** test the graceful handling of various runtime errors by the filter */
 	@Test
 	public void testExceptionHandling() throws Exception {
 		TestServlet.servletException = new ServletException("servlet boom");
 		try {
 			webClient.getPage(baseUrl + "/app/foo.ext?param1=SE");
 			fail("non-OK status should have been sent back by test servlet");
 		} catch (FailingHttpStatusCodeException expected) {
 		    assertNotNull("Servlet was hit", servlet);
 			assertEquals("Server status code sent back", 500, expected.getStatusCode());
 		}
 
 		TestServlet.ioException = new IOException("IO boom");
 		try {
 			webClient.getPage(baseUrl + "/app/foo.ext?param1=IOE");
 			fail("non-OK status should have been sent back by test servlet");
 		} catch (FailingHttpStatusCodeException expected) {
 		    assertNotNull("Servlet was hit", servlet);
 			assertEquals("Server status code sent back", 500, expected.getStatusCode());
 		}
 
 		TestServlet.runtimeException = new NullPointerException("Should have written a unit test");
 		try {
 			webClient.getPage(baseUrl + "/app/foo.ext?param1=RE");
 			fail("non-OK status should have been sent back by test servlet");
 		} catch (FailingHttpStatusCodeException expected) {
 		    assertNotNull("Servlet was hit", servlet);
 			assertEquals("Server status code sent back", 500, expected.getStatusCode());
 		}
 
 		TestServlet.error = new AssertionFailedError("Might as well try to trap these");
 		try {
 			webClient.getPage(baseUrl + "/app/foo.ext?param1=E");
 			fail("non-OK status should have been sent back by test servlet");
 		} catch (FailingHttpStatusCodeException expected) {
 		    assertNotNull("Servlet was hit", servlet);
 			assertEquals("Server status code sent back", 500, expected.getStatusCode());
 		}
 
 		//verify correct event data written to the DB
 	    manager.flush();
 	    assertEventDataMatches("BehaviorTrackingFilterTest.testExceptionHandling-result.xml");
 	}
 	
 	/** test filter init parameters for configuration */
 	@Test 
 	@DeployDescriptor(
 		contextRoot="/configured", 
 		webXml="com/mtgi/analytics/servlet/BehaviorTrackingFilterTest.testConfiguration-web.xml"
 	)
 	public void testConfiguration() throws Exception {
 	    webClient.getPage(baseUrl + "/configured/test/path?param1=hello&param1=world&param2&param3=72%3C");
 	    assertNotNull("Servlet was hit", servlet);
 	    manager.flush();
 	    assertEventDataMatches("BehaviorTrackingFilterTest.testConfiguration-result.xml");
 	}
 	
 	public static class ContextFilter implements Filter {
 
 		private ServletContext servletContext;
 		
 		public void init(FilterConfig cfg) {
 			this.servletContext = cfg.getServletContext();
 		}
 		public void destroy() {}
 
 		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 			WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
 			MockSessionContext sc = (MockSessionContext)context.getBean("sessionContext", MockSessionContext.class);
 			sc.setContextSessionId("ABCDEF123");
 			sc.setContextUserId("testUser");
 			chain.doFilter(request, response);
 		}
 
 	}
 	
 	public static class TestServlet extends HttpServlet {
 
 		private static final long serialVersionUID = 1961028788386946684L;
 
 		static ServletException servletException;
 		static IOException ioException;
 		static RuntimeException runtimeException;
 		static Error error;
 
 		static Integer status;
 		static String message;
 		
 		@Override
 		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 			doGet(request, response);
 		}
 
 		@Override @SuppressWarnings("deprecation")
 		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 			servlet = this;
 			
 			WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
 			manager = (BehaviorTrackingManagerImpl)context.getBean("behaviorTrackingManager", BehaviorTrackingManager.class);
 			
 			//wait a little bit to simulate something actually happening on the server side.
 			try {
 				Thread.sleep(20);
 			} catch (InterruptedException ie) {
 				throw new RuntimeException(ie);
 			}
 			
 			//simulate various error conditions that the filter will have to handle.
 			try {
 				if (servletException != null)
 					throw servletException;
 				if (ioException != null)
 					throw ioException;
 				if (runtimeException != null)
 					throw runtimeException;
 				if (error != null)
 					throw error; 
 				
 				if (status != null) {
 					//test multiple API methods for setting response code
 					if (message == null) {
 						if (status >= 400)
 							response.sendError(status);
 						else
 							response.setStatus(status);
 					} else {
 						if (status >= 400)
 							response.sendError(status, message);
 						else
 							response.setStatus(status, message);
 					}
 				}
 			} finally {
 				//reset error fields for next test run.
 				servletException = null;
 				ioException = null;
 				error = null;
 				runtimeException = null;
 				status = null;
 				message = null;
 			}
 		}
 		
 	}
 	
 }
