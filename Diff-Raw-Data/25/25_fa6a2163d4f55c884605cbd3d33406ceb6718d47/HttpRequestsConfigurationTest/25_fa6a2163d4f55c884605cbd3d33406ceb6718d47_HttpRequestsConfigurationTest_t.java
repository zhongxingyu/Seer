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
  
 package com.mtgi.analytics.aop.config.v11;
 
 import static java.util.Arrays.asList;
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 
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
 import org.junit.runner.RunWith;
 import org.springframework.context.ApplicationContext;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 import org.unitils.UnitilsJUnit4TestClassRunner;
 
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.FormEncodingType;
 import com.gargoylesoftware.htmlunit.SubmitMethod;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.WebRequestSettings;
 import com.mtgi.analytics.BehaviorEvent;
 import com.mtgi.analytics.BehaviorTrackingManager;
 import com.mtgi.analytics.BehaviorTrackingManagerImpl;
 import com.mtgi.analytics.MockSessionContext;
 import com.mtgi.test.unitils.tomcat.annotations.DeployDescriptor;
 import com.mtgi.test.unitils.tomcat.annotations.EmbeddedTomcat;
 
 @DeployDescriptor(contextRoot="/app", webXml="com/mtgi/analytics/aop/config/v11/HttpRequestsConfigurationTest-web.xml")
 @RunWith(UnitilsJUnit4TestClassRunner.class)
 public class HttpRequestsConfigurationTest {
 
 	private static TestServlet servlet;
 	private WebClient webClient;
 	
 	@EmbeddedTomcat(start=true)
 	private String baseUrl;
 	
 	@Before
 	public void setUp() {
 		webClient = new WebClient();
 	}
 	
 	@After
 	public void tearDown() {
 		servlet = null;
 		webClient = null;
 	}
 	
 	/** Test basic logging of a GET request */
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testGetRequest() throws Exception {
 	    webClient.getPage(baseUrl + "/app/test/path?param1=hello&param1=world&param2&param3=72%3C");
 	    assertNotNull("Servlet was hit", servlet);
 	    webClient.getPage(baseUrl + "/app/test/also.traq?param1=hello&param1=world&param2&param3=72%3C&dispatch=dang");
 	    webClient.getPage(baseUrl + "/app/test/also.traq?param1=nodispatch&param1=world&param2&param3=72%3C");
 
 	    Collection<BehaviorTrackingManagerImpl> managers = servlet.context.getBeansOfType(BehaviorTrackingManager.class).values();
 	    for (BehaviorTrackingManagerImpl m : managers)
 	    	m.flush();
 	    
 	    //first manager only gets events matching supplied filter patterns, and has
 	    //a custom event type configured.  second manager gets all events.
 	    EventKey[] expected = {
 	    	new EventKey("first", "req", "/app/test/also.traq?dispatch=dang"),
 	    	new EventKey("first", "req", "/app/test/also.traq"),
 	    	new EventKey("first", "behavior-tracking", "flush"),
 	    	new EventKey("second", "http-request", "/app/test/path"),
 	    	new EventKey("second", "http-request", "/app/test/also.traq"),
 	    	new EventKey("second", "http-request", "/app/test/also.traq"),
 	    	new EventKey("second", "behavior-tracking", "flush")
 	    };
 	    
 	    TestPersister persister = (TestPersister)servlet.context.getBean("persister");
 	    assertEquals("expected events persisted", expected.length, persister.count());
 
 	    //hash out the events so that we can verify logging.
 	    ArrayList<EventKey> events = new ArrayList<EventKey>();
 	    for (BehaviorEvent be : persister.events())
 	    	events.add(new EventKey(be));
 	    
 	    for (int i = 0; i < expected.length; ++i)
 	    	assertNotNull("received event[" + i + "] in correct order", events.remove(expected[i]));
 	    assertEquals("all receivedevents accounted for", 0, events.size());
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
 	}
 	
 	/** Verify that tracking events are logged for extension-mapped servlet paths */
 	@Test
 	public void testExtensionMapping() throws Exception {
 	    webClient.getPage(baseUrl + "/app/foo.ext?param1=hello&param1=world&param2&param3=72%3C");
 	    assertNotNull("Servlet was hit", servlet);
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
 	
 	public static class Service {
 		public boolean foo(String arg) {
 			return "7".equals(arg);
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
 		
 		ApplicationContext context;
 		
 		@Override
 		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 			doGet(request, response);
 		}
 
 		@Override @SuppressWarnings("deprecation")
 		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 			servlet = this;
 			context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
 			
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
						if (status >= 400) {
 							response.sendError(status, message);
						} else
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
 	
 	public static class EventKey {
 
 		private String application;
 		private String eventType;
 		private String eventName;
 		
 		public EventKey(BehaviorEvent event) {
 			this(event.getApplication(), event.getType(), event.getName());
 		}
 		
 		public EventKey(String application, String eventType,
 				String eventName) {
 			super();
 			this.application = application;
 			this.eventType = eventType;
 			this.eventName = eventName;
 		}
 		
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result
 					+ ((application == null) ? 0 : application.hashCode());
 			result = prime * result
 					+ ((eventName == null) ? 0 : eventName.hashCode());
 			result = prime * result
 					+ ((eventType == null) ? 0 : eventType.hashCode());
 			return result;
 		}
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			EventKey other = (EventKey) obj;
 			if (application == null) {
 				if (other.application != null)
 					return false;
 			} else if (!application.equals(other.application))
 				return false;
 			if (eventName == null) {
 				if (other.eventName != null)
 					return false;
 			} else if (!eventName.equals(other.eventName))
 				return false;
 			if (eventType == null) {
 				if (other.eventType != null)
 					return false;
 			} else if (!eventType.equals(other.eventType))
 				return false;
 			return true;
 		}
 		
 		
 	}
 }
