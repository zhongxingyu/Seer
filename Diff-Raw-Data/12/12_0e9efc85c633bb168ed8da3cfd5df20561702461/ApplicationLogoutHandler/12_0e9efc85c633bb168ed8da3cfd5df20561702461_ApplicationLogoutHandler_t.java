 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: ApplicationLogoutHandler.java,v 1.4 2008-04-02 19:00:00 huacui Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 
 package com.sun.identity.agents.filter;
 
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.sun.identity.agents.arch.AgentException;
 import com.sun.identity.agents.arch.Manager;
 import com.sun.identity.agents.common.ICookieResetHelper;
 
 /**
  * <p>
  * This Task handler is the Default HTTP Request Pre-processing handler for an
  * incoming request.
  * </p>
  */
 public class ApplicationLogoutHandler extends AmFilterTaskHandler
 implements IApplicationLogoutHandler {
 
     public ApplicationLogoutHandler(Manager manager){
         super(manager);
     }
 
     public void initialize(ISSOContext context, AmFilterMode mode)
     throws AgentException {
         super.initialize(context, mode);
         setIntroSpectRequestAllowedFlag(getConfigurationBoolean(
                 CONFIG_LOGOUT_INTROSPECT_ENABLE,
                 DEFAULT_LOGOUT_INTROSPECT_ENABLE));
         setIsActiveFlag();
     }
 
     /**
      * Checks to see if the incoming request is for a logout event and take the
      * necessary steps if a logout event is detected.
      *
      * @param ctx
      *            the <code>AmFilterRequestContext</code> that carries
      *            information about the incoming request and response objects.
      *
      * @return <code>AmFilterResult</code> if the processing of this task
      *         resulted in a particular action to be taken for the incoming
      *         request. The return could be <code>null</code> if no action is
      *         necessary for this request.
      *
      * @throws AgentException
      *             if the processing resulted in an unrecoverable error
      *             condition
      */
     public AmFilterResult process(AmFilterRequestContext ctx)
             throws AgentException {
 
         AmFilterResult result = null;
 
         if (detectNeedForLogout(ctx.getHttpServletRequest())) {
             if (isLogMessageEnabled()) {
                 logMessage(
                         "ApplicationLogoutHandler: Detected need to logout.");
             }
 
             // Call the container specific logout handler in ALL or J2EE_POLICY
             // mode
             if (ctx.getFilterMode().equals(AmFilterMode.MODE_J2EE_POLICY)
                     || ctx.getFilterMode().equals(AmFilterMode.MODE_ALL)) {
                 invokeApplicationLogoutHandler(ctx);
             }
 
             // Even if logout handler fails, we will Destroy local session and
             // redirect to AM
             if (isLogMessageEnabled()) {
                 logMessage(
                     "ApplicationLogoutHandler : Invalidating HTTP Session.");
             }
 
             HttpSession session = ctx.getHttpServletRequest().getSession(false);
             if (session != null) {
                 session.invalidate();
             }
             doCookiesReset(ctx);
             String logoutURL = getLogoutURL(ctx);
             result = new AmFilterResult(
                     AmFilterResultStatus.STATUS_REDIRECT,
                     logoutURL);
         }
 
         return result;
     }
 
     /**
      * Method declaration
      *
      *
      * @param ctx
      *            the <code>AmFilterRequestContext</code> that carries
      *            information about the incoming request and response objects.
      *
      * @return
      * @throws AgentException
      *
      * @see
      */
     private boolean invokeApplicationLogoutHandler(AmFilterRequestContext ctx)
             throws AgentException {
 
         boolean result = false;
 
         try {
             String appName = getApplicationName(ctx.getHttpServletRequest());
 
             IJ2EELogoutHandler localAuthHandler =
                     getApplicationLogoutHandler(appName);
 
             if (localAuthHandler != null) {
                 if (isLogMessageEnabled()) {
                     logMessage(
                         "ApplicationLogoutHandler : " +
                             "Invoking Local Logout handler");
                 }
 
                 localAuthHandler.logout(ctx.getHttpServletRequest(), ctx
                         .getHttpServletResponse(), null);
                 result = true;
             }
         } catch (Exception ex) {
             throw new AgentException(
                     "ApplicationLogoutHandler.invokeApplicationLogoutHandler()"
                          + " failed to invoke Local Logout with exception", ex);
         }
 
         return result;
     }
 
     private String getApplicationEntryURL(AmFilterRequestContext ctx) {
         HttpServletRequest request = ctx.getHttpServletRequest();
         String entryURI = getApplicationEntryURI(request);
         if (entryURI == null || entryURI.trim().length() == 0) {
             entryURI = "/";
         }
         return ctx.getBaseURL() + entryURI;
     }
 
     private String getApplicationEntryURI(HttpServletRequest request) {
         String appName = getApplicationName(request);
         String result = (String) getEntryURIs().get(appName);
         if (result == null) {
             Map entryMap = getConfigurationMap(CONFIG_LOGOUT_ENTRY_URI_MAP);
             result = (String) entryMap.get(appName);
             if (result == null) {
                 if (isLogMessageEnabled()) {
                     logMessage("ApplicationLogoutHandler: no entry URI "
                             + "specified for app: " + appName
                             + ". Using appcontext URI");
                 }
                 result = request.getContextPath();
             }
             getEntryURIs().put(appName, result);
         }
         return result;
     }
 
     /**
      * Method getApplicationLogoutHandler
      *
      * @param appName
      *            Application Name
      *  @ return IJ2EELogoutHandler Mapped Local Logout Handler
      *
      * @see Returns the Application Logout Handler for the context URI. If the
      *      application does not have an entry in the configuration.
      *
      */
     private IJ2EELogoutHandler getApplicationLogoutHandler(String appName)
             throws AgentException {
         IJ2EELogoutHandler localLogoutHandlerClass = null;
 
         if ((appName != null) && (appName.length() > 0)) {
             localLogoutHandlerClass =
                     (IJ2EELogoutHandler) getApplicationLogoutHandlers()
                     .get(appName);
 
             if (localLogoutHandlerClass == null) {
                 String localLogoutHandlerClassName = (String) getManager()
                         .getConfigurationMap(
                                 CONFIG_LOGOUT_APPLICATION_HANDLER_MAP).get(
                                 appName);
 
                 if ((localLogoutHandlerClassName != null)
                         && (localLogoutHandlerClassName.length() > 0)) {
                     try {
                         localLogoutHandlerClass = (IJ2EELogoutHandler) Class
                                 .forName(localLogoutHandlerClassName)
                                 .newInstance();
 
                         getApplicationLogoutHandlers().put(appName,
                                 localLogoutHandlerClass);
                         if (isLogMessageEnabled()) {
                             logMessage(
                                 "ApplicationLogoutHandler: Application Name = "
                                 + appName
                                 + " registering"
                                 + " Local Logout Handler = "
                                 + localLogoutHandlerClass);
                         }
                     } catch (Exception ex) {
                         throw new AgentException(
                                 "Failed to load Local Logout Handler "
                                         + "for Application = " + appName
                                         + " with exception :", ex);
                     }
                 }
             }
         }
 
         return localLogoutHandlerClass;
     }
 
     /**
      * Method getApplicationLogoutHandlers
      *
      * @return
      */
     private Hashtable getApplicationLogoutHandlers() {
         return _localLogoutHandlers;
     }
 
     /**
      * Detect the need for logout event
      */
     private boolean detectNeedForLogout(HttpServletRequest request)
             throws AgentException {
         String appName = null;
         boolean result = false;
 
         try {
             appName = getApplicationName(request);
 
             // Check for logout URI match
             result = matchLogoutURI(request, appName);
 
             // Check for logout param in the request body and query string
             if (!result) {
                 result = searchForLogoutParam(request, appName);
             }
         } catch (Exception ex) {
             throw new AgentException(
                     "ApplicationLogoutHandler.process() failed "
                         + " to process incoming request with exception", ex);
         }
 
         if (isLogMessageEnabled()) {
             logMessage("ApplicationLogoutHandler : Need to logout = "
                     + result);
         }
 
         return result;
     }
 
     /**
      *
      * Returns a boolean value indicating if a match for logout parameter was
      * found
      *
      * @param request
      * @param appName
      * @return boolean true or false
      */
     private boolean searchForLogoutParam(HttpServletRequest request,
             String appName) {
         boolean result = false;
         String logoutParam = (String) getManager().getConfigurationMap(
                 CONFIG_LOGOUT_REQUEST_PARAM_MAP).get(appName);
 
         if ((logoutParam != null) && (logoutParam.length() > 0)) {
 
             // First look to see if introspection in the request is allowed
             // If allowed, first look into HTTP request to short circuit the
             // rest
             if (getIntroSpectRequestAllowedFlag()) {
                 if (isLogMessageEnabled()) {
                     logMessage("ApplicationLogoutHandler : Looking for "
                             + "request parameter =" + logoutParam
                             + " in request body.");
                 }
 
                 String requestParam = request.getParameter(logoutParam);
                 if ((requestParam != null) && (requestParam.length() > 0)) {
 
                     result = true;
                     if (isLogMessageEnabled()) {
                         logMessage("ApplicationLogoutHandler : App Name = "
                                 + appName + "has a match for logout Param  ="
                                 + requestParam + " as a request parameter."
                                 + "Need to logout =" + result);
                     }
                 } else {
                     if (isLogMessageEnabled()) {
                         logMessage(
                                 "ApplicationLogoutHandler : Request Parameter ="
                                 + requestParam
                                 + " not found in HTTP request body.");
                     }
                 }
             } else { // look into query string
                 String queryString = request.getQueryString();
                 result = getLogoutParamMatchResult(logoutParam, queryString,
                         appName);
 
             }
         }
 
         return result;
     }
 
     /**
      *
      * Returns a boolean value if logout param is in query string, try the two
      * variations ?param_name=, &param_name=
      *
      * @param logoutParam
      * @param queryString
      * @param appName
      *            Application Name
      * @return boolean true or false
      */
 
     private boolean getLogoutParamMatchResult(String logoutParam,
             String queryString, String appName) {
        String firstVariant = logoutParam + "=";
         String secondVariant = "&" + logoutParam + "=";
         boolean result = false;
 
         if ((queryString != null) && (queryString.length() > 0)) {
            if (queryString.startsWith(firstVariant)
                     || (queryString.indexOf(secondVariant) > 0)) {
                result = true;
                 if (isLogMessageEnabled()) {
                     logMessage("ApplicationLogoutHandler : App Name = "
                             + appName + "has a match for logout Param  ="
                             + logoutParam + " in the Request query string ."
                             + "Need to logout =" + result);
                 }
             }
         }
 
         return result;
 
     }
 
     /**
      *
      * Returns a boolean value indicating if a match for logout URI was found
      *
      * @param request
      * @param appName
      * @return boolean true or false
      */
     private boolean matchLogoutURI(HttpServletRequest request, String appName) {
         boolean result = false;
 
         if ((appName != null) && (appName.length() > 0)) {
             String logoutURI = (String) getManager().getConfigurationMap(
                     CONFIG_LOGOUT_URI_MAP).get(appName);
 
             if ((logoutURI != null) && (logoutURI.length() > 0)) {
                 if (request.getRequestURI().equals(logoutURI)) {
                     result = true;
 
                     if (isLogMessageEnabled()) {
                         logMessage("ApplicationLogoutHandler : App Name = "
                                 + appName + "has a match for logout URI ="
                                 + logoutURI + " with the request URI."
                                 + "Need to logout =" + result);
                     }
                 } else {
                     if (isLogMessageEnabled()) {
                         logMessage("ApplicationLogoutHandler : Request URI = "
                                 + request.getRequestURI()
                                 + " did not match with logout URI = "
                                 + logoutURI + " specified in configuration.");
                     }
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Caches the isActive flag
      *
      * @return
      */
     private void setIsActiveFlag() {
 
         Map logoutUrlMap = getManager().getConfigurationMap(
                 CONFIG_LOGOUT_URI_MAP);
         Map requestParamMap = getManager().getConfigurationMap(
                 CONFIG_LOGOUT_REQUEST_PARAM_MAP);
 
         if ((logoutUrlMap != null && logoutUrlMap.size() > 0)
                 || (requestParamMap != null && requestParamMap.size() > 0)) {
             _isActiveFlag = true;
         }
     }
 
     /**
      * Returns the cached the isActive flag
      *
      * @return
      */
     private boolean getIsActiveFlag() {
         return _isActiveFlag;
     }
 
     /**
      * Detects if the handler is active or not
      *
      * @return boolean true if active, false if inactive
      */
     public boolean isActive() {
         return (!isModeNone()) && getIsActiveFlag();
     }
 
     /**
      * Returns a String that can be used to identify this task handler
      *
      * @return the name of this task handler
      */
     public String getHandlerName() {
         return AM_APP_LOGOUT_HANDLER_NAME;
     }
 
     /**
      * Returns the logout URL.
      *
      * @param request
      * @return
      */
     private String getLogoutURL(AmFilterRequestContext ctx)
     throws AgentException {
         String result = null;
         String loginURL = ctx.getAuthRedirectURL(getApplicationEntryURL(ctx));
         StringBuffer buff = null;
         String loginStr = "Login";
         String logoutStr = "Logout";
 
         // Replace "Login" by "Logout" in the redirection url
         // to the entry_uri page
         int iEnd = loginURL.indexOf("?");
         if (iEnd > -1) {
             int iStart = iEnd-loginStr.length();
             if (loginStr.compareToIgnoreCase(loginURL.substring(iStart,iEnd)) == 0) {
                 buff= new StringBuffer(loginURL.substring(0,iStart));
                 buff.append(logoutStr).append(loginURL.substring(iEnd));
                 result = buff.toString();
             }
         }
         if (result == null) {
             if (isLogWarningEnabled()) {
                 logWarning("ApplicationLogoutHandler: Could not get logout url. "
                 + "Redirecting to login page");
             }
             buff = new StringBuffer(loginURL);
             buff.append('&').append(ARG_NEW_SESSION_PARAMETER);
             result = buff.toString();
         }
         if (isLogMessageEnabled()) {
             logMessage("ApplicationLogoutHandler: Logout URL is: " + result);
         }
 
         return result;
     }
 
 
     /**
      * Method declaration
      *
      *
      * @return
      * @see
      */
     private boolean getIntroSpectRequestAllowedFlag() {
         return _introSpectRequestAllow;
     }
 
     /**
      * Method declaration
      *
      *
      * @return
      * @see
      */
     private void setIntroSpectRequestAllowedFlag(boolean allowed) {
         _introSpectRequestAllow = allowed;
         if (isLogMessageEnabled()) {
             logMessage("ApplicationLogoutHandler: request introspect: "
                     + allowed);
         }
     }
 
 
     private void setEntryURL(String url) {
         _entryURL = url;
         if (url == null || url.trim().length() == 0) {
             if (isLogWarningEnabled()) {
                 logWarning("ApplicationLogoutHandler: Entry URL is null");
             }
             _entryURL = null;
         } else {
             _entryURL = url;
             if (isLogMessageEnabled()) {
                 logMessage("ApplicationLogoutHandler: entry url: " + url);
             }
         }
     }
 
     private Hashtable getEntryURIs() {
         return _entryURIs;
     }
 
     private void doCookiesReset(AmFilterRequestContext ctx) {
         HttpServletRequest request = ctx.getHttpServletRequest();
         HttpServletResponse response = ctx.getHttpServletResponse();
         ICookieResetHelper cookieResetHelper =
             getSSOContext().getCookieResetHelper();
         if (cookieResetHelper != null && cookieResetHelper.isActive()) {
             cookieResetHelper.doCookiesReset(request, response);
         }
     }
 
     private String _entryURL;
     private boolean _isActiveFlag;
     private boolean _introSpectRequestAllow;
     private Hashtable _localLogoutHandlers = new Hashtable();
     private Hashtable _entryURIs = new Hashtable();
 }
 
