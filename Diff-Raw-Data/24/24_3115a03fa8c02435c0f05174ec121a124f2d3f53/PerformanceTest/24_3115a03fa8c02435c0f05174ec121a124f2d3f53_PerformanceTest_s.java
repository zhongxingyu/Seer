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
 
 import static org.junit.Assert.*;
 import static com.mtgi.test.unitils.tomcat.EmbeddedTomcatManager.getDeployableResource;
 import static java.util.Arrays.asList;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.lang.management.ThreadMXBean;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.httpclient.NameValuePair;
 import org.junit.Test;
 
 import com.gargoylesoftware.htmlunit.FormEncodingType;
 import com.gargoylesoftware.htmlunit.SubmitMethod;
 import com.gargoylesoftware.htmlunit.TextPage;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.WebRequestSettings;
 import com.mtgi.analytics.test.AbstractPerformanceTestCase;
 import com.mtgi.analytics.test.InstrumentedTestCase;
 import com.mtgi.test.unitils.tomcat.EmbeddedTomcatServer;
 
 /**
  * Performs some timed tests to verify that behavior tracking doesn't
  * interfere too much with application performance.
  */
 public class PerformanceTest extends AbstractPerformanceTestCase {
 
 	//we allow slightly greater overhead on servlet measurements than on method measurements, since
 	//these events should be comparatively fewer in number (by an order of magnitude or so)
 	private static final long AVERAGE_OVERHEAD_NS = 200000;
 	private static final long WORST_OVERHEAD_NS = 100000;
 	
 	private static final long TIME_BASIS = 400000;
 	
 	public PerformanceTest() {
 		super(1, 200, TIME_BASIS, AVERAGE_OVERHEAD_NS, WORST_OVERHEAD_NS);
 	}
 	
 	@Test
 	public void testPerformance() throws Throwable {
 		TestJob basisJob = new TestJob("/basis", "com/mtgi/analytics/servlet/PerformanceTest-web.xml");
 		TestJob testJob = new TestJob("/test", "com/mtgi/analytics/servlet/PerformanceTest-web-instrumented.xml");
 		testPerformance(basisJob, testJob);
 	}
 	
 	public static class TestServlet extends HttpServlet {
 
 		private static final long serialVersionUID = 7753583670712327245L;
 
 		@Override
 		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 			String count = req.getParameter("count");
 			resp.setContentType("text/plain");
 			resp.getWriter().print("success[" + count + "]");
 		}
 		
 	}
 	
 	/** record CPU time for request processing and send back to the client */
 	public static class TimingFilter implements Filter {
 
 		public void destroy() {}
 		public void init(FilterConfig cfg) {}
 
 		public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
 			ThreadMXBean mxb = ManagementFactory.getThreadMXBean();
 			assertTrue(mxb.isCurrentThreadCpuTimeSupported());
 			long start = mxb.getCurrentThreadCpuTime();
 			try {
 				chain.doFilter(req, res);
 			} finally {
 				long runtime = mxb.getCurrentThreadCpuTime() - start;
 				((HttpServletResponse)res).addHeader("beet-runtime", String.valueOf(runtime));
 			}
 		}
 		
 	}
 	
 	public static class TestJob implements InstrumentedTestCase {
 		
 		private static final long serialVersionUID = 3130490842011512211L;
 
 		private String deploymentDir;
 		private String contextPath;
 
 		private transient URL url;
 		private transient EmbeddedTomcatServer tomcat;
 		
 		private transient volatile int iteration = 0;
 		private transient WebClient webClient;
 		private transient WebRequestSettings request;
 		private transient NameValuePair countParam;
 		private transient Long runtime;
 		
		public TestJob(String contextPath, String deploymentDir) throws MalformedURLException {
 			this.contextPath = contextPath;
 			this.deploymentDir = deploymentDir;
 		}
 
 		public void setUp() throws Exception {
			url = new URL("http://localhost:8888" + contextPath + "/ping");
			
 			//startup embedded tomcat server with test application
			File dir = getDeployableResource(deploymentDir);
 			tomcat.deployDescriptor(contextPath, dir);
 			tomcat.start();
 
 			//configure client to send ping requests to server
 			webClient = new WebClient();
 			webClient.setJavaScriptEnabled(false);
 			request = new WebRequestSettings(url);
 			request.setEncodingType(FormEncodingType.URL_ENCODED);
 			request.setSubmitMethod(SubmitMethod.POST);
 			NameValuePair[] parameters = {
 					new NameValuePair("param1", "hello"),
 					new NameValuePair("param1", "world"),
 					new NameValuePair("param2", String.valueOf(System.currentTimeMillis())),
 					new NameValuePair("param3", "72<"),
 					countParam = new NameValuePair("count", "")
 			};
 			request.setRequestParameters(asList(parameters));
 		}
 		
 		public void tearDown() throws Exception {
 			try {
 				tomcat.destroy();
 			} finally {
 				tomcat = null;
 				url = null;
 				webClient = null;
 				request = null;
 				runtime = null;
 			}
 		}
 
 		public long getLastRuntimeNanos() {
 			return runtime;
 		}
 		
 		public void run() {
 			runtime = null;
 			String count = String.valueOf(++iteration);
 			countParam.setValue(count);
 			try {
 				//send ping request, retrieving the measured CPU time from filter response
 				//header
 				TextPage result = (TextPage)webClient.getPage(request);
 				assertEquals("servlet loaded successfully", "success[" + count + "]", result.getContent());
 
 				String runtime = result.getWebResponse().getResponseHeaderValue("beet-runtime");
 				assertNotNull("timing filter was activated", runtime);
 				this.runtime = Long.parseLong(runtime);
 				
 			} catch (Throwable e) {
 				throw new RuntimeException(e.getMessage(), e);
 			}
 		}
 	}
 }
