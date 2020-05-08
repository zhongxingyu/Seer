 /*
  * jGuard is a security framework based on top of jaas (java authentication and authorization security).
  * it is written for web applications, to resolve simply, access control problems.
  * version $Name$
  * http://sourceforge.net/projects/jguard/
  *
  * Copyright (C) 2004-2011  Charles Lescot
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *
  * jGuard project home page:
  * http://sourceforge.net/projects/jguard/
  */
 
 package net.sf.jguard.jee.authentication.http;
 
 import com.google.inject.Injector;
 import com.google.inject.servlet.GuiceFilter;
 import com.mycila.testing.junit.MycilaJunitRunner;
 import junit.framework.Assert;
 import net.sf.jguard.core.authentication.manager.AbstractAuthenticationManager;
 import net.sf.jguard.core.test.JGuardTestFiles;
 import net.sf.jguard.jee.HttpConstants;
 import net.sf.jguard.jee.JGuardJEETest;
 import net.sf.jguard.jee.SecurityTestCase;
 import net.sf.jguard.jee.listeners.ContextListener;
 import net.sf.jguard.jee.listeners.DummyContextListener;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.mock.web.*;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * @author <a href="mailto:diabolo512@users.sourceforge.net">Charles Lescot</a>
  */
 @RunWith(MycilaJunitRunner.class)
 public class AccessFilterTest extends JGuardJEETest implements SecurityTestCase {
 
     protected MockFilterChain filterChain;
     protected MockServletContext context;
     protected MockHttpSession session;
     protected static final String GET = "GET";
     protected static final String POST = "POST";
     protected static final String ADMIN = "admin";
     protected static final String GUEST = "guest";
 
 
     @SuppressWarnings({"PublicMethodNotExposedInInterface"})
 
     protected static final String WELCOME_DO = "/Welcome.do";
     protected static final String LOGON_DO = "/Logon.do";
     protected static final String LOGON_PROCESS_DO = "/LogonProcess.do";
     protected static final String LOGOFF_DO = "/Logoff.do";
     protected static final String UNKNOWN_DO = "/Unknown.do";
     protected static final String LOGIN = "login";
     protected static final String PASSWORD = "password";
 
     protected static final String WEIRD_JSP = "/weird.jsp";
     protected static final int HTTP_CODE_401 = 401;
     protected static final int HTTP_CODE_403 = 403;
     protected static final int HTTP_CODE_200 = 200;
 
     protected static final String RICK_SPACE_DO = "/RickSpace.do";
 
 
     public Injector injector;
     protected static final String FIXED_HTTP_SESSION_ID = "47";
     protected static final String DUMMY = "dummy";
     protected static final String AUTHENTICATION_FAILED = "/AuthenticationFailed.do";
     protected static final String AUTHENTICATION_SUCCEED = "/index.jsp";
     protected static final String ACCESS_DENIED = "/AccessDenied.do";
 
     protected static String authorizationXmlFileLocation = JGuardTestFiles.J_GUARD_AUTHORIZATION_XML.getLabel();
     protected static String authenticationXmlFileLocation = JGuardTestFiles.J_GUARD_USERS_PRINCIPALS_XML.getLabel();
     protected static String filterLocation = JGuardTestFiles.J_GUARD_FILTER_XML.getLabel();
     protected static String authenticationConfigurationLocation = JGuardTestFiles.J_GUARD_AUTHENTICATION_XML.getLabel();
     protected static final String JGUARD_STRUTS_EXAMPLE = JGuardTestFiles.JGUARD_STRUTS_EXAMPLE.getLabel();
     protected static final String J_GUARD_AUTHENTICATION_XML = JGuardTestFiles.J_GUARD_AUTHENTICATION_XML.getLabel();
     protected static final String J_GUARD_AUTHORIZATION_XML = JGuardTestFiles.J_GUARD_AUTHORIZATION_XML.getLabel();
 
 
     public MockHttpServletRequest request = new MockHttpServletRequest(context, GET, WELCOME_DO);
     protected GuiceFilter guiceFilter = new GuiceFilter();
     public HttpServletResponse response = new MockHttpServletResponse();
 
     public DummyContextListener dummyContextListener;
 
     private static final Logger logger = LoggerFactory.getLogger(AccessFilterTest.class.getName());
 
 
     @Before
     public void setUp() throws ServletException {
         context = buildContext();
         dummyContextListener = new DummyContextListener();
 
         ServletContextEvent servletContextEvent = new ServletContextEvent(context);
         dummyContextListener.contextInitialized(servletContextEvent);
         injector = dummyContextListener.getBuiltInjector();
 
         filterChain = new MockFilterChain();
         MockFilterConfig filterConfig = new MockFilterConfig(context);
         guiceFilter.init(filterConfig);
 
     }
 
 
     @After
     public void tearsDown() {
         guiceFilter.destroy();
         ServletContextEvent servletContextEvent = new ServletContextEvent(context);
         dummyContextListener.contextDestroyed(servletContextEvent);
     }
 
 
     private static MockServletContext buildContext() {
         MockServletContext context = new MockServletContext();
         context.setServletContextName(JGUARD_STRUTS_EXAMPLE);
         context.setContextPath('/' + JGUARD_STRUTS_EXAMPLE);
         context.addInitParameter(ContextListener.FILTER_LOCATION, filterLocation);
         context.addInitParameter(AbstractAuthenticationManager.AUTHENTICATION_XML_FILE_LOCATION, authenticationXmlFileLocation);
         context.addInitParameter(HttpConstants.AUTHORIZATION_CONFIGURATION_LOCATION, authorizationXmlFileLocation);
         context.addInitParameter(HttpConstants.AUTHENTICATION_CONFIGURATION_LOCATION, authenticationConfigurationLocation);
         context.addInitParameter(HttpConstants.AUTHENTICATION_CONFIGURATION_LOCATION, J_GUARD_AUTHENTICATION_XML);
         context.addInitParameter(HttpConstants.AUTHORIZATION_CONFIGURATION_LOCATION, J_GUARD_AUTHORIZATION_XML);
         return context;
     }
 
 
     @Test
     public void testInitWithConfigurationParameters() {
 
         //all stuff is done in setUp method
     }
 
     @Test
     public void testAccessToAuthorizedResourceGrantedToGuestSubject() {
 
         request = new MockHttpServletRequest(context, GET, WELCOME_DO);
         request.setServletPath(WELCOME_DO);
         try {
             guiceFilter.doFilter(request, response, filterChain);
             assertTrue(HttpServletResponse.SC_OK == ((MockHttpServletResponse) response).getStatus());
         } catch (Throwable e) {
             fail(e.getMessage());
         }
 
     }
 
     /**
      * this test assert that the response will be a redirect to a logon form,
      * and not a 401 HTTP code, because we use a stateful PEP
      */
     @Test
     public void testAccessToUnauthorizedResourceWithNoSubject() {
         request = new MockHttpServletRequest(context, GET, WEIRD_JSP);
         request.setServletPath(WEIRD_JSP);
         response = new MockHttpServletResponse();
         try {
             guiceFilter.doFilter(request, response, filterChain);
             assertTrue("response doesn't contain a forward url to " + LOGON_DO, LOGON_DO.equals(((MockHttpServletResponse) response).getForwardedUrl()));
         } catch (Throwable e) {
             fail(e.getMessage());
         }
     }
 
     protected MockHttpSession authenticateAsAdminSuccessfully() throws IOException, ServletException {
         return authenticate(ADMIN, ADMIN);
     }
 
     protected HttpSession authenticateAsGuestSuccessfully() throws IOException, ServletException {
         return authenticate(GUEST, GUEST);
     }
 
 
     protected MockHttpSession authenticate(String login, String password) throws IOException, ServletException {
         //get the login FORM
         MockHttpServletRequest request = new MockHttpServletRequest(context, GET, LOGON_DO);
         request.setServletPath(LOGON_DO);
         MockHttpSession session = new MockHttpSession(context, FIXED_HTTP_SESSION_ID);
         request.setSession(session);
         HttpServletResponse response = new MockHttpServletResponse();
         guiceFilter.doFilter(request, response, filterChain);
         Cookie[] cookies = ((MockHttpServletResponse) response).getCookies();
 
         //submit authentication credentials through the login FORM
         MockHttpServletRequest requestLogonProcess = new MockHttpServletRequest(context, POST, LOGON_PROCESS_DO);
         requestLogonProcess.setServletPath(LOGON_PROCESS_DO);
         requestLogonProcess.setSession(session);
         requestLogonProcess.addParameter(LOGIN, login);
         requestLogonProcess.addParameter(PASSWORD, password);
         MockHttpServletResponse responseLogonProcess = new MockHttpServletResponse();
         filterChain = new MockFilterChain();
         guiceFilter.doFilter(requestLogonProcess, responseLogonProcess, filterChain);
         Cookie[] cookies2 = responseLogonProcess.getCookies();
         return (MockHttpSession) requestLogonProcess.getSession(true);
     }
 
 
     @Test
     public void testAccessToUnauthorizedResourceWithSubject() {
         try {
             HttpSession session = authenticateAsGuestSuccessfully();
             MockHttpServletRequest request = new MockHttpServletRequest(context, GET, UNKNOWN_DO);
             request.setServletPath(UNKNOWN_DO);
             request.setSession(session);
             MockHttpServletResponse response = new MockHttpServletResponse();
             filterChain = new MockFilterChain();
 
             guiceFilter.doFilter(request, response, filterChain);
 
            assertTrue("HTTP status code is not 200 but " + response.getStatus(), HTTP_CODE_403 == response.getStatus());
         } catch (IOException e) {
             fail(e.getMessage());
         } catch (ServletException e) {
             fail(e.getMessage());
         }
 
     }
 
     @Test
     public void testAccessToAuthorizedResourceWithSubject() {
         try {
             HttpSession session = authenticateAsAdminSuccessfully();
             MockHttpServletRequest request = new MockHttpServletRequest(context, GET, RICK_SPACE_DO);
             request.setServletPath(RICK_SPACE_DO);
             request.setSession(session);
             MockHttpServletResponse response = new MockHttpServletResponse();
             filterChain = new MockFilterChain();
 
             guiceFilter.doFilter(request, response, filterChain);
             assertTrue("status code is not 200 OK but " + response.getStatus(), HttpServletResponse.SC_OK == response.getStatus());
 
 
         } catch (IOException e) {
             fail(e.getMessage());
         } catch (ServletException e) {
             fail(e.getMessage());
         }
 
     }
 
 
     /**
      * Welcome.do is authorized for all users (granted to 'guest' user).
      */
     @Test
     public void testAccessToAuthorizedResourceWithNoSubject() {
         try {
 
             MockHttpServletRequest request = new MockHttpServletRequest(context, GET, WELCOME_DO);
             request.setServletPath(WELCOME_DO);
             request.setSession(session);
             MockHttpServletResponse response = new MockHttpServletResponse();
             filterChain = new MockFilterChain();
 
             guiceFilter.doFilter(request, response, filterChain);
             assertTrue("status code is not 200 OK but " + response.getStatus(), HttpServletResponse.SC_OK == response.getStatus());
 
 
         } catch (IOException e) {
             fail(e.getMessage());
         } catch (ServletException e) {
             fail(e.getMessage());
         }
 
     }
 
 
     @Test
     public void testUnsuccessfulAuthentication() throws IOException, ServletException {
         //get the login FORM
         MockHttpServletRequest request = new MockHttpServletRequest(context, GET, LOGON_DO);
         request.setServletPath(LOGON_DO);
         HttpSession session = new MockHttpSession(context, FIXED_HTTP_SESSION_ID);
         request.setSession(session);
         HttpServletResponse response = new MockHttpServletResponse();
         guiceFilter.doFilter(request, response, filterChain);
         Cookie[] cookies = ((MockHttpServletResponse) response).getCookies();
 
         //submit authentication credentials through the login FORM
         MockHttpServletRequest requestLogonProcess = new MockHttpServletRequest(context, POST, LOGON_PROCESS_DO);
         requestLogonProcess.setServletPath(LOGON_PROCESS_DO);
         requestLogonProcess.setSession(session);
         requestLogonProcess.addParameter(LOGIN, DUMMY);
         requestLogonProcess.addParameter(PASSWORD, DUMMY);
         MockHttpServletResponse responseLogonProcess = new MockHttpServletResponse();
         filterChain = new MockFilterChain();
         guiceFilter.doFilter(requestLogonProcess, responseLogonProcess, filterChain);
         Cookie[] cookies2 = responseLogonProcess.getCookies();
         assertTrue(HttpServletResponse.SC_OK == responseLogonProcess.getStatus());
         //default dispatch mode is forward.redirect can be set in the permission.
         assertTrue(AUTHENTICATION_FAILED.equals(responseLogonProcess.getForwardedUrl().toString()));
 
     }
 
     @Test
     public void testSuccessFulAuthentication() throws IOException, ServletException {
         //get the login FORM
         MockHttpServletRequest request = new MockHttpServletRequest(context, GET, LOGON_DO);
         request.setServletPath(LOGON_DO);
         HttpSession session = new MockHttpSession(context, FIXED_HTTP_SESSION_ID);
         request.setSession(session);
         HttpServletResponse response = new MockHttpServletResponse();
         guiceFilter.doFilter(request, response, filterChain);
         Cookie[] cookies = ((MockHttpServletResponse) response).getCookies();
 
         //submit authentication credentials through the login FORM
         MockHttpServletRequest requestLogonProcess = new MockHttpServletRequest(context, POST, LOGON_PROCESS_DO);
         requestLogonProcess.setServletPath(LOGON_PROCESS_DO);
         requestLogonProcess.setSession(session);
         requestLogonProcess.addParameter(LOGIN, ADMIN);
         requestLogonProcess.addParameter(PASSWORD, ADMIN);
         MockHttpServletResponse responseLogonProcess = new MockHttpServletResponse();
         filterChain = new MockFilterChain();
         guiceFilter.doFilter(requestLogonProcess, responseLogonProcess, filterChain);
         assertTrue("response status to a logonProcess request is not OK (200) but " + responseLogonProcess.getStatus(), HttpServletResponse.SC_OK == responseLogonProcess.getStatus());
         assertTrue(AUTHENTICATION_SUCCEED.equals(responseLogonProcess.getForwardedUrl()));
     }
 
 
     @Test
     public void testLogoff() throws IOException, ServletException {
         logger.info("before authenticating as admin");
         MockHttpSession session = authenticateAsAdminSuccessfully();
         logger.info("after authenticating as admin");
         MockHttpServletRequest logoffRequest = new MockHttpServletRequest(context, GET, LOGOFF_DO);
         logoffRequest.setServletPath(LOGOFF_DO);
         logoffRequest.setSession(session);
         MockHttpServletResponse response = new MockHttpServletResponse();
 
         Assert.assertFalse(session.isInvalid());
         filterChain = new MockFilterChain();
         guiceFilter.doFilter(logoffRequest, response, filterChain);
         Assert.assertTrue(session.isInvalid());
 
     }
 
 }
 
