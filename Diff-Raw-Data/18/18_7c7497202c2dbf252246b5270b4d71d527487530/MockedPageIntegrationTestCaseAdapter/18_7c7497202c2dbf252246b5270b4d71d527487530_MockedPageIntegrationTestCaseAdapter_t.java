 package com.polopoly.ps.test;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletResponse;
 import javax.servlet.http.Cookie;
 
 import org.apache.velocity.app.Velocity;
 import org.jdom.Document;
 
 import com.mockrunner.base.NestedApplicationException;
 import com.mockrunner.mock.web.MockHttpServletRequest;
 import com.mockrunner.mock.web.MockServletConfig;
 import com.mockrunner.mock.web.MockServletContext;
 import com.mockrunner.mock.web.WebMockObjectFactory;
 import com.mockrunner.servlet.BasicServletTestCaseAdapter;
 import com.polopoly.application.servlet.ApplicationServletUtil;
 import com.polopoly.cm.servlet.dispatcher.impl.DispatcherPreparatorExtended;
 import com.polopoly.cm.servlet.velocity.VelocityServletContextListener;
 import com.polopoly.siteengine.dispatcher.SiteEngineApplicationImpl;
 import com.polopoly.siteengine.dispatcher.SiteEngineDispatcherServlet;
 import com.polopoly.util.client.PolopolyContext;
 
 /**
  * The class is abstract so that JUnit will not try to run it.
  */
 public abstract class MockedPageIntegrationTestCaseAdapter extends BasicServletTestCaseAdapter {
     private static final Logger LOGGER = Logger.getLogger(MockedPageIntegrationTestCaseAdapter.class.getName());
 
     private static final String DEFAULT_WEBAPP_PROJECT = "../web";
     private static final String WEBAPP_PROJECT_PROPERTY = "webapp.project.dir";
     private static final String APPLICATION_ATTRIBUTE_NAME = "siteEngineApplication";
 
     private static final String WEBAPP_DIR_PROPERTY = "webapp.dir";
     private static final String DEFAULT_WEBAPP_DIR = "/src/main/webapp";
 
     /**
      * Must be reused or it will try to register mbeans on every call.
      */
     private static SiteEngineDispatcherServlet dispatcherServlet = new SiteEngineDispatcherServlet();
     private static DispatcherPreparatorExtended filter;
     private static SiteEngineApplicationImpl siteEngineApplication;
 
     private File webappDir;
 	private File webappTestResourcesDir;
	private File webappTestOutputDir;
     private PolopolyContext context;
     private String requestUri;
 
     private boolean stackTraceExpected;
 
 
     public MockedPageIntegrationTestCaseAdapter(String requestUri, PolopolyContext context) {
         this.requestUri = requestUri;
         this.context = context;
 
         findWebappProject();
     }
 
 	private void findWebappProject() {
         String webappProjectRootProperty = System.getProperty(WEBAPP_PROJECT_PROPERTY);
         String webappProjectRoot;
         
 		if (webappProjectRootProperty == null) {
             webappProjectRoot = getDefaultWebappProjectDir();
         } else {
         	webappProjectRoot = webappProjectRootProperty;
         }
 		
 		String webappDirProperty = System.getProperty(WEBAPP_DIR_PROPERTY);
         
         if (webappDirProperty == null) {
             webappDirProperty = getDefaultWebappDir();
         }
 		
 		webappDir = new File(webappProjectRoot + webappDirProperty);
         webappTestResourcesDir = new File(webappProjectRoot + "/src/test/resources");
        webappTestOutputDir = new File(webappProjectRoot + "/target/test-classes");
 
         if (!webappDir.exists()) {
             throw new RuntimeException("The Maven project containing the webapp (" + webappDir.getAbsolutePath()
                     + ") could not be found. You can specify the project directory explicitly using the property "
                     + WEBAPP_PROJECT_PROPERTY + ".");
         }
 	}
 
     protected String getDefaultWebappProjectDir() {
 		return DEFAULT_WEBAPP_PROJECT;
 	}
     
     private String getDefaultWebappDir() {
         return DEFAULT_WEBAPP_DIR;
     }
 
     @Override
     public ServletResponse getFilteredResponse() {
         return super.getFilteredResponse();
     }
 
     @Override
     protected WebMockObjectFactory createWebMockObjectFactory() {
         return new PageIntegrationTestWebMockObjectFactory(webappDir);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         initFilterConfig();
 
         MockServletContext servletContext = getWebMockObjectFactory().getMockServletContext();
         servletContext.setInitParameter("p.applicationName", "preview");
         servletContext.setInitParameter("polopoly.logging.url", "/l.gif");
 
         Properties props = new Properties();
 
         InputStream velocityProperties = getVelocityPropertiesStream();
         
         try {
         	props.load(velocityProperties);
         } finally {
             if (velocityProperties != null) {
                 velocityProperties.close();
             }
         }
         
         if (!webappDir.exists()) {
         	throw new RuntimeException("Expected the web application root to be at " + webappDir.getCanonicalPath() + " but it did not exist.");
         }
         
         props.setProperty("fileloader.resource.loader.path", webappDir.getCanonicalPath());
 
         String macroLibrary = props.getProperty("velocimacro.library");
 
         // if there's an absolute path to the macro definition that's because it
         // is in fact a resource URI within the webapp. If we make it relative
         // it will be relative to the file root specified above and resolving it
         // will work. In practice, this only happens for page integration tests
         // in the web module itself since these confuse the two
         // velocity.properties in their classpath and may use the web server one
         // rather than the one for tests.
         if (macroLibrary != null && macroLibrary.startsWith("/")) {
             props.setProperty("velocimacro.library", macroLibrary.substring(1));
         }
 
         Velocity.setApplicationAttribute(VelocityServletContextListener.CM_SERVER_PROP, context.getPolicyCMServer());
         Velocity.setApplicationAttribute(VelocityServletContextListener.SERVLET_CONTEXT_PROP, servletContext);
         Velocity.init(props);
 
         ApplicationServletUtil.setApplication(servletContext, context.getApplication());
 
         if (siteEngineApplication == null) {
             siteEngineApplication = new SiteEngineApplicationImpl(context.getApplication(), context.getCmClient(),
                     servletContext);
         }
 
         servletContext.setAttribute(APPLICATION_ATTRIBUTE_NAME, siteEngineApplication);
 
         addRequestFilters();
 
         setUpServlet();
 
         MockHttpServletRequest request = getWebMockObjectFactory().getMockRequest();
         request.setRequestURI(requestUri);
         request.setRequestURL("http://localhost" + requestUri);
         request.setContextPath(getContextPath());
 
         if (requestUri.startsWith(getContextPath() + getServletPath())) {
             request.setServletPath(getServletPath());
             String pathInfo = requestUri.substring(getContextPath().length() + getServletPath().length());
 
             if (pathInfo.length() > 0) {
                 request.setPathInfo(pathInfo);
             }
         }
     }
 
 	private InputStream getVelocityPropertiesStream() {
 		InputStream result = getClass().getResourceAsStream("/velocity.properties");
 		
 		if (result != null) {
 			return result;
 		}
 
		File velocityPropertiesFile = new File(webappTestOutputDir.getAbsolutePath() + "/velocity.properties");
    	
    	if (!velocityPropertiesFile.exists()) {
    	    velocityPropertiesFile = new File(webappTestResourcesDir.getAbsolutePath() + "/velocity.properties");
    	}
     	
     	try {
			return new FileInputStream(velocityPropertiesFile);
 		} catch (FileNotFoundException e) {
         	throw new RuntimeException("No velocity.properties found in the classpath or " +
        			"in the webapp project's test resources (" + velocityPropertiesFile.getAbsolutePath() +
 				"). There should be a one available that is only used for tests.");
 		}
 	}
 
     protected void addRequestFilters() {
         if (filter == null) {
             filter = new DispatcherPreparatorExtended();
             addFilter(filter, true);
         } else {
             addFilter(filter, false);
         }
     }
 
     protected String getServletPath() {
         return "/cm";
     }
 
     protected String getContextPath() {
         return "";
     }
 
     protected void setUpServlet() throws Exception {
         MockServletConfig servletConfig = getWebMockObjectFactory().getMockServletConfig();
         servletConfig.setInitParameter("defaultPath", "/");
         servletConfig.setInitParameter("hideServletExceptions", "false");
         servletConfig.setInitParameter("handleTranslationException", "false");
 
         getServletTestModule().setServlet(dispatcherServlet, true);
         setDoChain(true);
     }
 
     private void initFilterConfig() {
         Map<String, String> preparatorParameters = new HashMap<String, String>();
         preparatorParameters.put("siteAware", "false");
         preparatorParameters.put("defaultContentType", "text/html;charset=utf-8");
         preparatorParameters.put("pathCreator", "com.polopoly.cm.path.PolicyLimitedContentPathCreator");
 
         preparatorParameters.put("pathCreator.stopPolicyClassName", "com.polopoly.siteengine.structure.SiteRoot");
 
         preparatorParameters.put("pathTranslator",
                 "com.polopoly.cm.path.ContentFilePathTranslator");
         preparatorParameters.put("pathTranslator.rootContentId", "p.siteengine.Sites.d");
 
         preparatorParameters.put("pathTranslator.contentListNames", "polopoly.Department,default,pages,articles,feeds");
 
         preparatorParameters.put("pathTranslator.contentNameFallback", "false");
         preparatorParameters.put("pathTranslator.ignoreCaseInPathSegment", "true");
         preparatorParameters.put("filePathTranslator", "com.polopoly.cm.path.ContentFilePathTranslator");
         preparatorParameters.put("urlBuilder", "com.polopoly.siteengine.dispatcher.sitealias.SiteAliasUrlBuilder");
         preparatorParameters.put("urlBuilder.defaultParameters", "cache");
         preparatorParameters.put("dispatcher", "com.polopoly.siteengine.mvc.dispatcher.cache.CachingRenderDispatcher");
         preparatorParameters.put("dispatcher.defaultMode", "www");
 
         getWebMockObjectFactory().getMockFilterConfig().setInitParameters(preparatorParameters);
     }
 
     @Override
     protected void doGet() {
         MockHttpServletRequest request = getWebMockObjectFactory().getMockRequest();
 
         LOGGER.log(Level.INFO, "Loading page " + requestUri + "...");
 
         ServletRequestThreadLocal.storeRequest(request);
 
         try {
             super.doGet();
         } finally {
             ServletRequestThreadLocal.storeRequest(null);
         }
     }
 
     @Override
     protected void doPost() {
         MockHttpServletRequest request = getWebMockObjectFactory().getMockRequest();
 
         LOGGER.log(Level.INFO, "Loading page " + requestUri + " with Parameter: " + request.getParameterMap());
 
         ServletRequestThreadLocal.storeRequest(request);
 
         try {
             super.doPost();
         } catch (NestedApplicationException e) {
             if (!isStackTraceExpected()) {
                 e.printStackTrace();
             }
 
             getWebMockObjectFactory().getMockResponse().setStatus(500);
         } finally {
             ServletRequestThreadLocal.storeRequest(null);
         }
     }
 
     @Override
     public BufferedReader getOutputAsBufferedReader() {
         return super.getOutputAsBufferedReader();
     }
 
     @Override
     protected Document getOutputAsJDOMDocument() {
         return super.getOutputAsJDOMDocument();
     }
 
     public void addCookie(String name, String value) {
         MockHttpServletRequest request = getWebMockObjectFactory().getMockRequest();
 
         request.addCookie(new Cookie(name, value));
     }
 
     @SuppressWarnings("unchecked")
     public Cookie getCookie(String name) {
         List<Cookie> cookies = getWebMockObjectFactory().getMockResponse().getCookies();
 
         for (Cookie cookie : cookies) {
             if (cookie.getName().equals(name)) {
                 return cookie;
             }
         }
 
         return null;
     }
 
     @Override
     protected void addRequestParameter(String key, String value) {
         super.addRequestParameter(key, value);
     }
 
     @SuppressWarnings("unchecked")
     protected Map<String, String> getRequestParameters() {
         return getWebMockObjectFactory().getMockRequest().getParameterMap();
     }
 
     protected void setQueryString(String queryString) {
         getWebMockObjectFactory().getMockRequest().setQueryString(queryString);
     }
 
     public String getResponseHeader(String key) {
         return getWebMockObjectFactory().getMockResponse().getHeader(key);
     }
 
     public int getResponseStatusCode() {
         return getWebMockObjectFactory().getMockResponse().getStatusCode();
     }
 
     public boolean wasForwardedToURI(String path) {
         return getWebMockObjectFactory().getMockServletContext().getRequestDispatcherMap().containsKey(path);
     }
 
     public boolean wasForwardedToURIContaining(String uriPart) {
         for (Object path : getWebMockObjectFactory().getMockServletContext().getRequestDispatcherMap().keySet()) {
             String pathString = (String) path;
             if (pathString.contains(uriPart)) {
                 return true;
             }
         }
         return false;
     }
 
     public void setStackTraceExpected(boolean stackTraceExpected) {
         this.stackTraceExpected = stackTraceExpected;
     }
 
     public boolean isStackTraceExpected() {
         return this.stackTraceExpected;
     }
 
     protected File getWebappDir() {
         return webappDir;
     }
 
 }
