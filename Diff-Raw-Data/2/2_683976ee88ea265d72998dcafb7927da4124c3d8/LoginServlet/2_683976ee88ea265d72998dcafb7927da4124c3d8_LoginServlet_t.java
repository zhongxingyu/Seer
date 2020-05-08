 /**
  * Copyright (C) 2003 FEIDE
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package no.feide.moria.servlet;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Timer;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import no.feide.moria.BackendException;
 import no.feide.moria.Configuration;
 import no.feide.moria.ConfigurationException;
 import no.feide.moria.Credentials;
 import no.feide.moria.NoSuchSessionException;
 import no.feide.moria.Session;
 import no.feide.moria.SessionException;
 import no.feide.moria.SessionStore;
 import no.feide.moria.SessionStoreTask;
 import no.feide.moria.stats.StatsStore;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.apache.velocity.exception.ParseErrorException;
 import org.apache.velocity.exception.ResourceNotFoundException;
 
 
 /**
  * Presents the actual login page.
  * @author Lars Preben S. Arnesen l.p.arnesen@usit.uio.no
  * @version $Id$
  */
 public class LoginServlet extends MoriaServlet {
     
     /** Used for logging. */
     private static Logger log = Logger.getLogger(LoginServlet.class.toString());
     /** Statistics */
     private StatsStore stats = StatsStore.getInstance();
 
     /** Constant for property lookup. */
     private static String NOSESSION  = "nosession";
 
     /** Constant for property lookup. */
     private static String MAXLOGIN   = "maxlogin";
 
     /** Constant for property lookup. */
     private static String AUTHFAILED = "auth";
 
     /** Constant for property lookup. */
     private static String NOORG    = "noorg";
 
 	/** Constant for property lookup. */
 	private static String ERRORG    = "errorg";
 
     /** Constant for property lookup. */
     private static String GENERIC    = "generic";
 
 
     /** The URL the user should post it's username and password to. */
     private String loginURL;
 
     /** Local pointer to session store. */
     private SessionStore sessionStore;
     
     /** Default language */
     private static String defaultLang;
 
     /** Available languages */
     private HashMap availableLanguages;
 
     /** Timer for the session time out service. */
     Timer sessionTimer = new Timer(true);
 
 
     /**
      * Some basic initialization.
      * @throws ServletException If a <code>SessionException</code> or a
      *                          <code>ConfigurationException</code> is caught.
      */
     public void init()
     throws ServletException {
         log.finer("init()");
         
         try {
             
             /* Set default language */ 
             defaultLang = Configuration.getProperty("no.feide.moria.defaultLanguage");
 
             /* Login URL */
             loginURL = Configuration.getProperty("no.feide.moria.LoginURL");
 
             /* Initialize session timeout timer */
             int sessionDelaySec = new Integer(Configuration.getProperty("no.feide.moria.SessionTimerDelay")).intValue();
             log.config("Starting time out service. Repeat every "+sessionDelaySec+" seconds.");
             sessionTimer.scheduleAtFixedRate(new SessionStoreTask(), new Date(), sessionDelaySec*1000);
         
             /* Set local pointer to session store. */
             sessionStore = SessionStore.getInstance();
 
             /* Get available languages */
             availableLanguages = Configuration.getLanguages();
 
         } catch (SessionException e) {
             log.severe("SessionException caught and re-thrown as ServletException");
             throw new ServletException("SessionException caught", e);
         } catch (ConfigurationException e) {
             log.severe("ConfigurationException caught and re-thrown as ServletException");
             throw new ServletException("ConfigurationException caught", e);
         }
     }
 
 
     /**
      * Stops the background maintenance thread.
      */
     public void destroy() {
         log.finer("destroy()");
         sessionTimer.cancel();
     }
    
 
 
 
     /**
      * Handles all http requests from the client. A login page is
      * returned for GET-requests and POST requests are considered as
      * login attempts.
      * @param request  The http request
      * @param response The http response
      * @param context  The Velocity contex
      * @return Template to be used for request
      */   
     public Template handleRequest( HttpServletRequest request,
 	HttpServletResponse response, Context context ) throws ServletException {        
         
         log.finer("handleRequest(HttpServletRequest, HttpServletResponse, Context)");
 
 
         /* A GET should only return the login page. POST is used for
          * login attempts.*/ 
         try {
             if (request.getMethod().equals("GET")) 
                 return loginPage(request, response, context);
                         
             else if (request.getMethod().equals("POST"))
                 return attemptLogin(request, response, context);
 
             else {
                 log.severe("Unsupported http request: "+request.getMethod());
                 return genLoginTemplate(request, response, context, null, GENERIC);
             }
         }
 
         catch( ParseErrorException e ) {
             log.severe("Parse error. " + e);
             throw new ServletException(e);
         }
 
         catch( ResourceNotFoundException e ) {
             log.severe("Template file not found. " + e);
             throw new ServletException(e);
         }
 
         catch( Exception e ) {
             StringWriter stackTrace = new StringWriter();
             e.printStackTrace(new PrintWriter(stackTrace));
             log.severe("Unspecified error during template parsing: \n" + stackTrace.toString());
             throw new ServletException(e);
         }
         
     }
 
 
 
     /**
      *  Creates a Template based on the login tamplate file. If an
      *  error message is supplied, the error message is displayed. If
      *  no sessionID is supplied the login login form is not displayed.
      */
     private Template genLoginTemplate(HttpServletRequest request, HttpServletResponse response, 
 									  Context context, Session session, String errorType) 
     		throws ParseErrorException, ResourceNotFoundException, MissingResourceException, Exception {
 
         String sessionID = null;
         ResourceBundle bundle = null;
 		String selectedLanguage = null;
 		String wsDefaultLang = Configuration.getProperty("no.feide.moria.defaultLanguage"); 
  		
         if (session != null) {
         	if (session.getWebService().getDefaultLang() != null)
         		wsDefaultLang = session.getWebService().getDefaultLang();
         	sessionID = session.getID();
         }
         
         if (selectedLanguage == null)
         	selectedLanguage = wsDefaultLang;
  
         if (selectedLanguage == null)
         	selectedLanguage = defaultLang; 
  
         context.put("availableLanguages", availableLanguages);
 
         HashMap bundleData = getBundle("login", request, response, defaultLang, wsDefaultLang);
         bundle = (ResourceBundle) bundleData.get("bundle");
         selectedLanguage = (String) bundleData.get("selectedLanguage");
 
         context.put("selectedLanguage", selectedLanguage);
 
         String wsName = null;
         String wsURL  = null;
 
         if (session != null) {
             wsName = session.getWebService().getName();
             wsURL  = session.getWebService().getUrl();
         }
 
         loadBundleIntoContext(bundle, context, wsName, wsURL);
 
         /* Get preselected realm first from URL parameter, then cookie and finally get it
          * from the web service configuration */
         String realm = "";
         realm = request.getParameter("realm"); 
         
         if (realm == null) {
             realm = getCookieValue("realm", request);
         }
        
        if ((realm == null || realm.equals("")) && session != null) {
         	realm = session.getWebService().getDefaultOrg();
         }
       	
         context.put("selectedRealm", realm);
 
         /* List of organizations (for drop down menu) */
         HashMap orgShorts = Configuration.getOrgShorts(selectedLanguage);
         if (orgShorts == null) 
             orgShorts = Configuration.getOrgShorts(defaultLang);
 
         if (orgShorts == null)
         	log.severe("Unable to get organization names from configuration.");
         
         String[] sortedOrgNames = (String[]) orgShorts.keySet().toArray(new String[orgShorts.size()]); 
         Arrays.sort(sortedOrgNames);
 
         context.put("orgShorts", orgShorts);
         context.put("sortedOrgNames", sortedOrgNames);
         
 
         /* Set or reset error messages */
         if (errorType != null) {
             context.put("errorMessage", context.get("error_"+errorType));
             context.put("errorDescription", context.get("error_"+errorType+"_desc"));
         }
      
         else {
             context.remove("errorMessage");
             context.remove("errorDescription");
         }
 
 
         if (sessionID != null) { 
             context.put("loginURL", loginURL+"?id="+sessionID);
 
             String secLevel = session.getAttributesSecLevel().toLowerCase();
             context.put("expl_data", context.get("expl_data_"+secLevel));
 
             /* Detailed list of attributes */
 			if (request.getParameter("showAttrs") != null) {
               	Vector attrNames = new Vector();
                 String[] attributes = session.getRequestedAttributes();
                 for (int i = 0; i < attributes.length; i++) {
                     attrNames.add(context.get("ldap_"+attributes[i]));
                 }
                 context.put("attrNames", attrNames);
 			}
          }
         else 
             /* If no sessionID then remove loginURL */
             context.remove("loginURL");
 
         
         return getTemplate("login.vtl");
     }
 
 
     
 
     /**
      *  Generates a template for the loginPage. The request should
      *  contain a valid Moria sessionID.
      *  @param request  The http request
      *  @param response The http response
      *  @param context  The Velocity context
      *  @return Template, a login form or an error message if the
      *  session is invalid.
      */
     private Template loginPage(HttpServletRequest request, HttpServletResponse response, Context context) throws ParseErrorException, ResourceNotFoundException, Exception {
 
         log.finer("loginPage(HttpServletRequest, HttpServletResponse, Context");
 
         /* Get session ID */
         String id = request.getParameter("id");
         log.fine("SessionID: "+id);
 
 
 
 
         /* Try to use SSO */
         Session existingSession = null; 
         HttpSession httpSession = 
             ((HttpServletRequest)request).getSession(true);
             
         /* Find existing session */
         String existingSessionID = (String) httpSession.getAttribute("moriaID");
 
         try {
             existingSession = sessionStore.getSessionSSO(existingSessionID);
         }
 
         catch (NoSuchSessionException e) {
             /* If no old session exist, then SSO is impossible.
              * Continue with normal authentication. */
             log.fine("Did not find SSO session: "+existingSessionID);
             existingSession = null;
         }
 
         try {
             Session session = sessionStore.getSessionLogin(id);
 
             httpSession.setAttribute("moriaID", session.getID());
 
             
             if (existingSession != null) {
                 log.fine("Existing SSO session found.");
 
                 /* Session has to be authenticated and locked to be
                    used in SSO. If not locked another web service is
                    using the session. */
                 if (existingSession.isAuthenticated() && existingSession.isLocked()) {
                     HashMap cachedAttributes = existingSession.getCachedAttributes();
                     if (cachedAttributes != null && cachedAttributes.size() > 0) {
 
                         if (session.getAllowSso()) {
                             log.info("Redirect to WebService (SSO), "+session.getWebService().getName());
                             String wsID = session.getWebService().getId();
                             stats.incStatsCounter(wsID, "loginSSO");
                             session.setCachedAttributes(cachedAttributes);
                             sessionStore.deleteSession(existingSession);
  //                           stats.decStatsCounter(wsID, "activeSessions");
                             stats.decreaseCounter("sessionsSSOActive");
                             session.unlock(existingSession.getBackendInstance());
                             redirectToWebService(response, session);
                             return null;
                         }
                     }
                 }
             }
 
             session.initiateAuthentication();
 
             return genLoginTemplate(request, response, context, session, null);
         }
         
         catch (NoSuchSessionException e) {
             log.warning("Request Login page: DENIED, Session not found: "+id);
             return genLoginTemplate(request, response, context, null, NOSESSION);
         }
 
         catch (SessionException e) {
             log.warning("Session exception:\n"+e);
             return genLoginTemplate(request, response, context, null, GENERIC);
         }
     }
 
 
 
 	/**
      *  Authenticates the user based on the http request. The request
      *  should be supplied with parameters (from the login form) with
      *  username and password. The user is authenticated and
      *  redirected back to the originating web service if the
      *  authentication is successful. If not the user is presented
      *  with a new login form and an error message.
      *  @param request  The http request containing username and password
      *  @param response The http response
      *  @param contex   The Velocity context
      *  @return Template, the login form
      */
     private Template attemptLogin(HttpServletRequest request, HttpServletResponse response, Context context) throws ParseErrorException, ResourceNotFoundException, Exception {
 
         log.finer("attemptLogin(HttpServletRequest, HttpServletResponse, Context)");
 
         Session session = null;
         String id       = request.getParameter("id");
         String username = request.getParameter("username");
         String realm    = request.getParameter("realm");
         String password = request.getParameter("password");
 
 
         /* Get session */
         try {
             session = sessionStore.getSessionLogin(id);
         }
         
         catch (NoSuchSessionException e) {
             return genLoginTemplate(request, response, context, null, NOSESSION);
         }
 
         catch (SessionException e) {
             return genLoginTemplate(request, response, context, null, GENERIC);
         }
 
         if (username.indexOf("@") != -1) {
             realm = username.substring(username.indexOf("@")+1, username.length());
         }
 
         if (realm != null) {
             /* Error message if user has not selected organization */
             if (realm.equals("null"))
                 return genLoginTemplate(request, response, context, session, NOORG);
             
             /* Concatinate realm with username */
             if (!realm.equals("") && (username != null && username.indexOf("@") == -1))
                 username += "@"+realm;
         }
 
 		/* Check validity of realm */
 		if (!Configuration.getOrgNames("nb").containsKey(realm))
 			return genLoginTemplate(request, response, context, session, ERRORG);
 
         String log_prefix = "Authentication attempt from "+username+": ";
 
         if (!session.authenticationInitiated()) {
             log.warning(log_prefix+"DENIED, Login page not requested SID="+session.getID());
             throw new Exception("Premature login attempt.");
         }
 
 
 
         /* Authenticate */
         try {
             Credentials c = new Credentials(username, password);
             if (!session.authenticateUser(c)) {
                 log.info(log_prefix+"FAILED");
                 stats.incStatsCounter(session.getWebService().getId(), "loginFailed");
         
                 /* If the user has exceeded the maximum login
                 attempts, the session is now gone. */
                 try {
                     session = sessionStore.getSessionLogin(id);
                 }
                 catch (NoSuchSessionException e) {
                     return genLoginTemplate(request, response, context, null, MAXLOGIN);
                 }
 
                 return genLoginTemplate(request, response, context, 
                                         session, AUTHFAILED);
             }
         } 
         
 
         catch (BackendException e) {
             
             log.severe("BackendException caught and re-thrown as ServletException\n"+e);
 			//return genLoginTemplate(request, response, context, null, GENERIC);
 			// TODO: Throw other than BackedException when catching a known exception.
             /**
              * TODO: Nothing
              */ 
             // TODO: Nothing
 			return genLoginTemplate(request, response, context, null, AUTHFAILED);
         } 
 
         catch (SessionException e) {
             log.severe("SessionException caught and re-thrown as ServletException\n"+e);
                 return genLoginTemplate(request, response, context, null, GENERIC);
         }
 
 
         /* Success; redirect to the original session URL and
          * include the updated session ID in URL and HttpSession. */
         
         log.info(log_prefix+"SUCCESS");
         stats.incStatsCounter(session.getWebService().getId(), "loginSuccessful");
 
         HttpSession httpSession = 
             ((HttpServletRequest)request).getSession(true);
         
 
         httpSession.setAttribute("moriaID", session.getID());
 
         /* Remember realm (cookie). */
         setCookieValue("realm", realm, response);
 
         redirectToWebService(response, session);
     
         return null; // Do not use template for redirect.
     }
     
     /**
      * Set response to 302 (redirect) and location header so that the
      * user is redirected back to the web service.
      * @throws ConfigurationException If unable to get the session's redirect
      *                                URL.
      */ 
     private void redirectToWebService(HttpServletResponse response, Session session)
     throws ConfigurationException {
         response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);  
         response.setHeader("Location", session.getRedirectURL());
         log.info("Authenticated user, redirected back to: "+session.getRedirectURL());
     }
 
 
 
     /**
      * Return a requested cookie value
      * @param cookieName Name of the cookie
      * @param request The Http request
      * @return Requested value, empty string if not found
      */
     private String getCookieValue(String cookieName, HttpServletRequest request) {
         String value = "";
         Cookie[] cookies = request.getCookies();
 
         if (cookies != null) {
             for (int i = 0; i < cookies.length; i++) {
                 if (cookies[i].getName().equals(cookieName)) {
                     value = cookies[i].getValue();
                 }
             }
         }
 
         return value;
     }
 
     
     
     /**
      * Add a cookie to the response.
      * @param cookieName Name of the cookie
      * @param cookieValue Value to be set
      * @param response The http response
      */
     private void setCookieValue(String cookieName, String cookieValue, HttpServletResponse response) throws ConfigurationException{
         Cookie cookie = new Cookie(cookieName, cookieValue);
         int validDays = new Integer(Configuration.getProperty("no.feide.moria.servlet.cookieValidDays")).intValue();
         cookie.setMaxAge(validDays*24*60*60); // Days to seconds
         cookie.setVersion(0); // IE won't store cookies if using version 1 (RFC 2109).
         response.addCookie(cookie);
     }
 }
