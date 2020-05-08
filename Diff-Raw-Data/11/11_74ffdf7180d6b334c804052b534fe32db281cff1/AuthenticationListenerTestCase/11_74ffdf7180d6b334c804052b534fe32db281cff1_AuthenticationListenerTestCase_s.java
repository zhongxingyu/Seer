 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.picketbox.solder.test.authentication;
 
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.servlet.FilterChain;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequestEvent;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import junit.framework.Assert;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.Filters;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.solder.servlet.event.ServletEventBridgeFilter;
 import org.jboss.solder.servlet.event.ServletEventBridgeListener;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mockito;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.picketbox.core.authentication.PicketBoxConstants;
 import org.picketbox.solder.PicketBoxListener;
 import org.picketbox.solder.authentication.AuthenticationScheme;
 import org.picketbox.solder.test.TestUtil;
 
 /**
  * <p>
  * Tests the authentication process using the {@link PicketBoxListener}
  * </p>
  *
  * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
  *
  */
 @RunWith(Arquillian.class)
 public class AuthenticationListenerTestCase {
 
     private static final String J_SESSIONID = "JIAS912323123123123";
     private static final String USERNAME = "admin";
     private static final String PASSWORD = "admin";
 
     @Inject
     private ServletEventBridgeListener servletEventListener;
 
     @Inject
     private ServletEventBridgeFilter filter;
 
     @Deployment
     public static Archive<?> createTestArchive() {
         WebArchive deployment = TestUtil.createBasicTestArchive("seam-beans-auth-test.xml");
 
         deployment.addPackages(true, Filters.exclude(Package.getPackage("org.picketbox.solder.test")),
                 AuthenticationScheme.class.getPackage());
 
         return deployment;
     }
 
     /**
      * <p>
      * Tests the authentication process.
      * </p>
      */
     @Test
     public void testHTTPFormAuthentication() throws Exception {
         ServletContext ctx = mock(ServletContext.class);
         HttpSession session = mock(HttpSession.class);
         FilterChain chain = mock(FilterChain.class);
 
         servletEventListener.contextInitialized(new ServletContextEvent(ctx));
 
         // lets send a first request and initialize the authentication process
         performPreAuthentication(ctx, session, chain);
 
         // lets send a authentication request with some credentiais
         performAuthentication(ctx, session, chain);
     }
 
     protected void performAuthentication(ServletContext ctx, HttpSession session, FilterChain chain) throws IOException,
             ServletException {
         HttpServletRequest request = mock(HttpServletRequest.class);
         HttpServletResponse response = mock(HttpServletResponse.class);
 
         configureMockRequest(request, PicketBoxConstants.HTTP_FORM_J_SECURITY_CHECK, ctx, session);
 
         when(request.getParameter(PicketBoxConstants.HTTP_FORM_J_USERNAME)).thenReturn(USERNAME);
         when(request.getParameter(PicketBoxConstants.HTTP_FORM_J_PASSWORD)).thenReturn(PASSWORD);
 
         final Map<String, Object> sessionAttributes = new HashMap<String, Object>();
 
         Mockito.doAnswer(new Answer<Object>() {
 
             @Override
             public Object answer(InvocationOnMock invocation) throws Throwable {
                 sessionAttributes.put(invocation.getArguments()[0].toString(), invocation.getArguments()[1]);
                 return null;
             }
         }).when(session).setAttribute(anyString(), anyObject());
 
         servletEventListener.requestInitialized(new ServletRequestEvent(ctx, request));
         filter.doFilter(request, response, chain);
 
         Assert.assertNotNull(sessionAttributes.get(PicketBoxConstants.SUBJECT));
     }
 
     protected void performPreAuthentication(ServletContext ctx, HttpSession session, FilterChain chain) throws IOException,
             ServletException {
         HttpServletRequest request = mock(HttpServletRequest.class);
         HttpServletResponse firstResponse = mock(HttpServletResponse.class);
         RequestDispatcher dispatcher = mock(RequestDispatcher.class);
 
         configureMockRequest(request, "/test/index.jsp", ctx, session);
         when(ctx.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);
 
         servletEventListener.requestInitialized(new ServletRequestEvent(ctx, request));
         filter.doFilter(request, firstResponse, chain);
     }
 
     /**
      * <p>
      * Configures a mocked {@link HttpServletRequest} with some basic configurations.
      * </p>
      */
     private void configureMockRequest(HttpServletRequest request, String uri, ServletContext ctx, HttpSession session) {
         when(request.getServletContext()).thenReturn(ctx);
         when(request.getSession()).thenReturn(session);
         when(request.getSession().getId()).thenReturn(J_SESSIONID);
         when(request.getSession(true)).thenReturn(session);
         when(request.getSession(false)).thenReturn(session);
         when(request.getHeaderNames()).thenReturn(Collections.enumeration(new ArrayList<String>()));
         when(request.getCookies()).thenReturn(new ArrayList<Cookie>().toArray(new Cookie[] {}));
         when(request.getParameterMap()).thenReturn(new HashMap<String, String[]>());
         when(request.getRequestURI()).thenReturn(uri);
     }
 
 }
