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
 package org.picketbox.http.filters;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.picketbox.core.authentication.AuthenticationManager;
 import org.picketbox.core.authentication.PicketBoxConstants;
 import org.picketbox.core.authentication.impl.CertificateMechanism;
 import org.picketbox.core.authentication.impl.DigestMechanism;
 import org.picketbox.core.authentication.impl.UserNamePasswordMechanism;
 import org.picketbox.core.authentication.manager.DatabaseAuthenticationManager;
 import org.picketbox.core.authentication.manager.LDAPAuthenticationManager;
 import org.picketbox.core.authentication.manager.PropertiesFileBasedAuthenticationManager;
 import org.picketbox.core.authentication.manager.SimpleCredentialAuthenticationManager;
 import org.picketbox.core.authorization.AuthorizationManager;
 import org.picketbox.core.authorization.impl.SimpleAuthorizationManager;
 import org.picketbox.core.exceptions.AuthenticationException;
 import org.picketbox.http.PicketBoxHTTPMessages;
 import org.picketbox.http.PicketBoxManager;
 import org.picketbox.http.authentication.HTTPAuthenticationScheme;
 import org.picketbox.http.authentication.HTTPBasicAuthentication;
 import org.picketbox.http.authentication.HTTPClientCertAuthentication;
 import org.picketbox.http.authentication.HTTPDigestAuthentication;
 import org.picketbox.http.authentication.HTTPFormAuthentication;
 import org.picketbox.http.config.PicketBoxConfiguration;
 import org.picketbox.http.resource.HTTPProtectedResourceManager;
 
 /**
  * A {@link Filter} that delegates to the PicketBox Security Infrastructure
  *
  * @author anil saldhana
  * @since Jul 10, 2012
  */
 public class DelegatingSecurityFilter implements Filter {
     private PicketBoxManager securityManager;
 
     private FilterConfig filterConfig;
 
     private HTTPAuthenticationScheme authenticationScheme;
 
     @Override
     public void init(FilterConfig fc) throws ServletException {
         this.filterConfig = fc;
 
         ServletContext sc = filterConfig.getServletContext();
 
         Map<String, Object> contextData = new HashMap<String, Object>();
         contextData.put(PicketBoxConstants.SERVLET_CONTEXT, sc);
 
         // Let us try the servlet context
         String authValue = sc.getInitParameter(PicketBoxConstants.AUTHENTICATION_KEY);
         AuthorizationManager authorizationManager = null;
         AuthenticationManager am = null;
 
        if (authValue != null && authValue.isEmpty()) {
             // Look for auth mgr also
             String authMgrStr = sc.getInitParameter(PicketBoxConstants.AUTH_MGR);
             // Look for auth mgr also
             String authzMgrStr = sc.getInitParameter(PicketBoxConstants.AUTHZ_MGR);
 
             if (authzMgrStr != null) {
                 authorizationManager = getAuthzMgr(authzMgrStr);
                 authorizationManager.start();
                 contextData.put(PicketBoxConstants.AUTHZ_MGR, authorizationManager);
             }
 
             am = getAuthMgr(authMgrStr);
 
             contextData.put(PicketBoxConstants.AUTH_MGR, am);
 
             authenticationScheme = getAuthenticationScheme(authValue, contextData);
         } else {
             String loader = filterConfig.getInitParameter(PicketBoxConstants.AUTH_SCHEME_LOADER);
             if (loader == null) {
                 throw PicketBoxHTTPMessages.MESSAGES.missingRequiredInitParameter(PicketBoxConstants.AUTH_SCHEME_LOADER);
             }
             String authManagerStr = filterConfig.getInitParameter(PicketBoxConstants.AUTH_MGR);
             if (authManagerStr != null && !authManagerStr.isEmpty()) {
                 am = getAuthMgr(authManagerStr);
                 contextData.put(PicketBoxConstants.AUTH_MGR, am);
             }
             String authzManagerStr = filterConfig.getInitParameter(PicketBoxConstants.AUTHZ_MGR);
             if (authzManagerStr != null && authzManagerStr.isEmpty()) {
                 authorizationManager = getAuthzMgr(authzManagerStr);
                 authorizationManager.start();
                 contextData.put(PicketBoxConstants.AUTHZ_MGR, authorizationManager);
             }
             authenticationScheme = (HTTPAuthenticationScheme) SecurityActions.instance(getClass(), loader);
         }
 
         PicketBoxConfiguration configuration = new PicketBoxConfiguration();
 
         configuration.authentication().addMechanism(new UserNamePasswordMechanism()).addMechanism(new DigestMechanism())
                 .addMechanism(new CertificateMechanism());
 
         configuration.authentication().addAuthManager(am);
         configuration.authorization(authorizationManager);
 
         configuration.setProtectedResourceManager(new HTTPProtectedResourceManager());
 
         this.securityManager = configuration.buildAndStart();
 
         authenticationScheme.setPicketBoxManager(this.securityManager);
 
         sc.setAttribute(PicketBoxConstants.PICKETBOX_MANAGER, this.securityManager);
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
             ServletException {
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpServletResponse httpResponse = (HttpServletResponse) response;
 
         logout(httpRequest, httpResponse);
 
         authenticate(httpRequest, httpResponse);
 
         authorize(httpRequest, httpResponse);
 
         if (!response.isCommitted()) {
             chain.doFilter(httpRequest, response);
         }
     }
 
     private void authorize(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
         boolean authorize = this.securityManager.authorize(httpRequest, httpResponse);
 
         if (!authorize) {
             if (!httpResponse.isCommitted()) {
                 httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
             }
         }
     }
 
     private void authenticate(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException {
         if (httpResponse.isCommitted()) {
             return;
         }
 
         try {
             // this.securityManager.authenticate(httpRequest, httpResponse);
             this.authenticationScheme.authenticate(httpRequest, httpResponse);
         } catch (AuthenticationException e) {
             throw new ServletException(e);
         }
     }
 
     private void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException {
         this.securityManager.logout(httpRequest, httpResponse);
     }
 
     @Override
     public void destroy() {
         this.filterConfig = null;
         if (this.securityManager != null) {
             this.securityManager.stop();
         }
     }
 
     private HTTPAuthenticationScheme getAuthenticationScheme(String value, Map<String, Object> contextData)
             throws ServletException {
         if (value.equals(PicketBoxConstants.BASIC)) {
             return new HTTPBasicAuthentication();
         }
         if (value.equals(PicketBoxConstants.DIGEST)) {
             return new HTTPDigestAuthentication();
         }
         if (value.equals(PicketBoxConstants.CLIENT_CERT)) {
             return new HTTPClientCertAuthentication();
         }
 
         return new HTTPFormAuthentication();
     }
 
     private AuthenticationManager getAuthMgr(String value) {
         if (value.equalsIgnoreCase("Credential")) {
             return new SimpleCredentialAuthenticationManager();
         } else if (value.equalsIgnoreCase("Properties")) {
             return new PropertiesFileBasedAuthenticationManager();
         } else if (value.equalsIgnoreCase("Database")) {
             return new DatabaseAuthenticationManager();
         } else if (value.equalsIgnoreCase("Ldap")) {
             return new LDAPAuthenticationManager();
         }
         if (value == null || value.isEmpty()) {
             return new PropertiesFileBasedAuthenticationManager();
         }
 
         return (AuthenticationManager) SecurityActions.instance(getClass(), value);
     }
 
     private AuthorizationManager getAuthzMgr(String value) {
         if (value.equalsIgnoreCase("Drools")) {
             return (AuthorizationManager) SecurityActions.instance(getClass(),
                     "org.picketbox.drools.authorization.PicketBoxDroolsAuthorizationManager");
         } else if (value.equalsIgnoreCase("Simple")) {
             return new SimpleAuthorizationManager();
         }
 
         return (AuthorizationManager) SecurityActions.instance(getClass(),
                 "org.picketbox.drools.authorization.PicketBoxDroolsAuthorizationManager");
     }
 }
