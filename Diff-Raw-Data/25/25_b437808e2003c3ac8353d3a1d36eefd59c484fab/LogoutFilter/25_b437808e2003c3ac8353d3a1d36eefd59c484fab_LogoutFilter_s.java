 /*
  * LogoutFilter.java
  *
  * Created on Feb 9, 2007 
  *
  * Copyright (C) 2006, 2007 Carl E Harris, Jr.
  * 
  * This library is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or (at
  * your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
  * License for more details.
  */
 package org.soulwing.cas.filter;
 
 import java.io.IOException;
 import java.util.Enumeration;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.soulwing.cas.client.ProtocolConfiguration;
 import org.soulwing.cas.client.ServiceValidationResponse;
 import org.soulwing.cas.client.SimpleUrlGenerator;
 import org.soulwing.cas.support.ValidationUtils;
 
 /**
  * A filter for removing CAS-related session state.  This filter monitors
  * incoming requests for requests with a servlet path that matches its
  * <code>logoutPath</code> property.  When such a request is detected, after
  * passing control down the filter chain, this fitler removes all 
  * CAS-related attributes from the session.
  * 
  * Additionally, this filter supports several configurable properties:
  * <ul>
  * <li><code>applicationLogout</code> &mdash; if <code>true</code> (the default)
  *   the logout request will be forwarded down the filter chain towards the 
  *   application servlet, to allow cleanup of non-CAS session state</li>
  * <li><code>globalLogout</code> &mdash; if <code>true</code> (default is
  *   <code>false</code>) then after the request has been (optionally)
  *   processed by any downstream filters and the application itself, a
  *   redirect to the CAS logout URL will be sent on the response.</li>
  * <li><code>redirectUrl</code> &mdash; if configured, then after the 
  *   request has been (optionally) processed by downstream filters and the
  *   application itself, and after CAS global logout has been performed (if
  *   enabled), then the browser will be directed to this URL.</li>
  * <li><code>protocolConfiguration</code> &mdash; CAS protocol configuration
  *   bean, required only when <code>globalLogout</code> is enabled.  Will be
  *   obtained from ProtocolConfigurationFilter if this filter is wired into
  *   the application's deployment descriptor (web.xml) as opposed to being
  *   used as a simple bean (e.g. with FilterToBeanProxy)</li>
  *    
  * @author Carl Harris
  */
 public class LogoutFilter implements Filter {
 
   public static final String GLOBAL_LOGOUT_DEFAULT = "false";
   public static final String APPLICATION_LOGOUT_DEFAULT = "true";
 
   private static final Log log = LogFactory.getLog(LogoutFilter.class);
   private String logoutPath;
   private String redirectUrl;
   private boolean applicationLogout = true;
   private boolean globalLogout = false;
   private ProtocolConfiguration protocolConfiguration;
 
   /**
    * Gets the servlet path that should be interpreted by this filter
    * as a logout request.
    * @return <code>String</code> servlet path this filter should interpret
    *    as a logout request.
    */
   public String getLogoutPath() {
     return logoutPath;
   }
 
   /**
    * Sets the servlet path that should be interpreted by this filter as
    * a logout request.
    * @param logoutPath <code>String</code> servlet path that this filter
    *    should interpret as a logout request.
    */
   public void setLogoutPath(String logoutPath) {
     this.logoutPath = logoutPath;
   }
 
   /**
    * Gets the <code>applicationLogout</code> flag.
    * @return <code>true</code> if application logout is required
    */
   public boolean isApplicationLogout() {
     return applicationLogout;
   }
 
   /**
    * Sets the <code>applicationLogout</code> flag.
    * @param applicationLogout <code>true</code> if a request matching
    *    <code>logoutPath</code> should be forwarded down the filter chain
    *    towards the application servlet; <code>false</code> to shunt the
    *    logout request at this filter.
    */
   public void setApplicationLogout(boolean applicationLogout) {
     this.applicationLogout = applicationLogout;
   }
 
   /**
    * Gets the <code>globalLogout</code> flag.
    * @return <code>true</code> if CAS global logout should be invoked 
    */
   public boolean isGlobalLogout() {
     return globalLogout;
   }
 
   /**
    * Sets the <code>globalLogout</code> flag.
    * @param globalLogout <code>true</code> if a request matching
    *    <code>logoutPath</code> should be redirected to the CAS global logout
    *    URL.  
    *    
    * Note: the <code>protocolConfiguration</code> property must be set if
    *    this flag is set.
    *    
    * Note: if <code>applicationLogout</code> is also set, the redirect to
    *    CAS global logout will occur only if no downstream filter or servlet
    *    has committed the response.
    */
   public void setGlobalLogout(boolean globalLogout) {
     this.globalLogout = globalLogout;
   }
 
   /**
    * Gets the URL which will be used to send a redirect after logout is
    * complete.
    * @return <code>String</code> URL.
    */  
   public String getRedirectUrl() {
     return redirectUrl;
   }
 
   /**
    * Sets the URL which will be used to send a redirect after logout is
    * completed.
    * @param redirectUrl
    *
    * Note: if <code>applicationLogout</code> is also set, the redirect 
    *    will occur only if no downstream filter or servlet has committed 
    *    the response.
    *    
    * Note: if <code>globalLogout</code> is also set, this URL will be passed
    *    as a parameter to the CAS logout operation.  The CAS server will
    *    be responsible for the final redirect to the URL specified here.
    */
   public void setRedirectUrl(String redirectUrl) {
     this.redirectUrl = redirectUrl;
   }
 
   /**
    * Gets the CAS protocol configuration bean.
    */
   public ProtocolConfiguration getProtocolConfiguration() {
     return protocolConfiguration;
   }
 
   /**
    * Sets the CAS protocol configuration bean.
    */
   public void setProtocolConfiguration(
       ProtocolConfiguration protocolConfiguration) {
     this.protocolConfiguration = protocolConfiguration;
   }
   
   /**
    * Initializes this filter.  When this filter is being used as a bean in
    * a dependency injection framework (e.g. Spring), this method should be 
    * invoked after all dependencies have been set.
    * @throws Exception
    */
   public void init() throws Exception {
     if (getLogoutPath() == null) {
       throw new IllegalStateException("must set logoutPath property");
     }
     if (isGlobalLogout() && getProtocolConfiguration() == null) {
       throw new IllegalStateException(
           "globalLogout requires that the protocolConfiguration property be set");
     }
   }
   
   /*
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig filterConfig) throws ServletException {
     FilterConfigurator fc = new FilterConfigurator(filterConfig);
     setLogoutPath(fc.getRequiredParameter(FilterConstants.LOGOUT_PATH));
     setApplicationLogout(Boolean.parseBoolean(
         fc.getParameter(FilterConstants.APPLICATION_LOGOUT, 
             APPLICATION_LOGOUT_DEFAULT)));
     setGlobalLogout(Boolean.parseBoolean(
         fc.getParameter(FilterConstants.GLOBAL_LOGOUT,
             GLOBAL_LOGOUT_DEFAULT)));
     setRedirectUrl(new FilterConfigurator(filterConfig)
         .getParameter(FilterConstants.REDIRECT_URL));
     setProtocolConfiguration(ProtocolConfigurationFilter.getConfiguration());
     try {
       init();
     }
     catch (Exception ex) {
       throw new ServletException(ex);
     }
   }
   
   /* 
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy() {
     // not needed
   }
 
   /* 
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest request, ServletResponse response,
       FilterChain filterChain) throws IOException, ServletException {
     try {
       doHttpFilter(
           (HttpServletRequest) request, 
           (HttpServletResponse) response,
           filterChain);
     }
     catch (ClassCastException ex) {
       log.info("Filter supports HTTP only");
       filterChain.doFilter(request, response);
     }
   }
 
   protected void doHttpFilter(HttpServletRequest request, 
       HttpServletResponse response, FilterChain filterChain) 
      throws IOException, ServletException { 

    if (isApplicationLogout()) {
      filterChain.doFilter(request, response);
    }
     if (request.getServletPath().equals(logoutPath)) {
       removeSessionState(request);
     }
    if (isGlobalLogout()) {
      doGlobalLogout(response);
    }
    else if (getRedirectUrl() != null) {
      doLogoutRedirect(response);
     }
   }
 
   protected void removeSessionState(HttpServletRequest request) {
     HttpSession session = request.getSession(false);
     if (session == null) {
       return;
     }
     ServiceValidationResponse validation =
         ValidationUtils.getServiceValidationResponse(request);
     Enumeration names = session.getAttributeNames();
     while (names.hasMoreElements()) {
       String name = (String) names.nextElement();
       if (name.startsWith(FilterConstants.ATTRIBUTE_PREFIX)) {
         log.debug("Removing attribute " + name + " from session");
         session.removeAttribute(name);
       }
     }
     if (validation != null) {
       log.info("User " + validation.getUserName() + " has logged out");
     }
   }
 
   private void doGlobalLogout(HttpServletResponse response) throws IOException {
     if (!response.isCommitted()) {
       SimpleUrlGenerator urlGenerator = 
           new SimpleUrlGenerator(getProtocolConfiguration());
       response.sendRedirect(urlGenerator.getLogoutUrl(getRedirectUrl()));
     }
     else {
       log.debug("skipping CAS global logout because response is committed");
     }
   }
   
   private void doLogoutRedirect(HttpServletResponse response) throws IOException {
     if (!response.isCommitted()) {
       response.sendRedirect(getRedirectUrl());
     }
     else {
       log.debug("skipping logout redirect because response is committed");
     }
   }
   
 }
