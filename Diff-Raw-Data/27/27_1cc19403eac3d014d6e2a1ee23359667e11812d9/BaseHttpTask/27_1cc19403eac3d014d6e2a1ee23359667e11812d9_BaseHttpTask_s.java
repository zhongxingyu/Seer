 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.screen.control.servlet;
 
 /**
  * @(#)DBServlet.java 0.00 12-Feb-97 Don Corley
  *
  * Copyright © 2012 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.security.NoSuchAlgorithmException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.jbundle.base.db.BaseDatabase;
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.model.DBParams;
 import org.jbundle.base.model.HtmlConstants;
 import org.jbundle.base.model.Utility;
 import org.jbundle.base.util.BaseApplication;
 import org.jbundle.base.util.Environment;
 import org.jbundle.base.util.MainApplication;
 import org.jbundle.model.App;
 import org.jbundle.model.PropertyOwner;
 import org.jbundle.model.RecordOwnerParent;
 import org.jbundle.model.Task;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.db.Converter;
 import org.jbundle.thin.base.db.Params;
 import org.jbundle.thin.base.thread.RecordOwnerCollection;
 import org.jbundle.thin.base.util.Application;
 import org.jbundle.thin.base.util.base64.Base64;
 
 
 /**
  * ServletTask.
  * 
  * This servlet is the main servlet.
  * <p>
  * The possible params are:
  * <pre>
  *  record - Create a default HTML screen for this record (Display unless "move" param)
  *  screen - Create this HTML screen
  *  limit - For Displays, limit the records displayed
  *  form - If "yes" display the imput form above the record display
  *  move - HTML Input screen - First/Prev/Next/Last/New/Refresh/Delete
  *  applet - applet, screen=applet screen
  *              applet params: archive/id/width/height/cabbase
  *  menu - Display this menu page
  * </pre>
  */
 public class BaseHttpTask extends Object
     implements Task
 {
     /**
      * The extra task properties.
      */
     protected Map<String,Object> m_properties = null;
     /**
      * My parent application.
      */
     protected App m_application = null;
     /**
      * The servlet that called me.
      */
     protected BasicServlet m_servlet = null;
     /**
      * 
      */
     protected BasicServlet.SERVLET_TYPE m_servletType = null;
     /**
      *
      */
     private static Map<BasicServlet.SERVLET_TYPE, Map<String,Object>> gApplicationProperties = new Hashtable<BasicServlet.SERVLET_TYPE, Map<String,Object>>();
     /**
      * Last display message.
      */
     protected String m_strCurrentStatus = null;
     /**
      * Last display level.
      */
     protected int m_iCurrentWarningLevel = Constants.INFORMATION;
     /**
      * Length of time to save cookie.
      */
     public static final int COOKIE_AGE = 31 * 24 * 60 * 60;
     /**
      * Children record owners.
      */
     protected RecordOwnerCollection m_recordOwnerCollection = null;
 
     /**
      * Constructor.
      */
     public BaseHttpTask()
     {
         super();
     }
     /**
      * Constructor.
      */
     public BaseHttpTask(BasicServlet servlet, BasicServlet.SERVLET_TYPE servletType)
     {
         this();
         this.init(servlet, servletType);
     }
     /**
      * Constructor.
      */
     public void init(BasicServlet servlet, BasicServlet.SERVLET_TYPE servletType)
     {
         m_servlet = servlet;
         m_servletType = servletType;
         m_recordOwnerCollection = new RecordOwnerCollection(this);
         m_properties = BaseHttpTask.getInitialProperties(servlet, servletType);
     }
     /**
      * Is this task shared between all sessions on this server?
      * If so, I can't use the properties, since they are not unique to this user's task.
      * Note: By default, assume they are not shared.
      * @return true If is is shared.
      */
     public boolean isShared()
     {
         return true;   // A servlet task is unique for each http session.
     }
     /**
      * init method.
      */
     public static Map<String,Object> getInitialProperties(javax.servlet.Servlet servlet, BasicServlet.SERVLET_TYPE servletType)
     {
         Map<String,Object> properties = new Hashtable<String,Object>();
         Enumeration<?> enumeration = servlet.getServletConfig().getInitParameterNames();
         while (enumeration.hasMoreElements())
         {
             String strParamName = (String)enumeration.nextElement();
             String strValue = servlet.getServletConfig().getInitParameter(strParamName);
             properties.put(strParamName, strValue);
         }
 
         properties.put(DBParams.SERVLET, DBConstants.TRUE); // Flag - yes this is a servlet
         if (properties.get(DBParams.FREEIFDONE) == null)
             properties.put(DBParams.FREEIFDONE, DBConstants.FALSE);   // Don't free when only the last app is running.
         if (properties.get(DBParams.LOCAL) == null)
             properties.put(DBParams.LOCAL, DBParams.JDBC); // Servlets uses Jdbc by default
         if (properties.get(DBParams.REMOTE) == null)
             properties.put(DBParams.REMOTE, DBParams.JDBC);
         if (properties.get(DBParams.TABLE) == null)
             properties.put(DBParams.TABLE, DBParams.JDBC);
         
         return properties;
     }
     /**
      * init method.
      */
     public static void initServlet(javax.servlet.Servlet servlet, BasicServlet.SERVLET_TYPE servletType)
     {
         Map<String,Object> properties = BaseHttpTask.getInitialProperties(servlet, servletType);
 
         // Since this is called only once, make sure the logger is the servlet's logger.
         Utility.setLogger(Logger.getLogger(DBConstants.ROOT_PACKAGE.substring(0, DBConstants.ROOT_PACKAGE.length() - 1)));
 
         Utility.getLogger().info("ServletTask init()");
 
         Environment env = Environment.getEnvironment(properties);  // Make sure environment is inited
         if (env.getDefaultApplication() == null)        // First time?
         {
             env.setDefaultApplication(new MainApplication(null, properties, null));   // This app handles user info reads, etc.
         }
         gApplicationProperties.put(servletType, properties);
     }
     /**
      * Destroy this Servlet and any active applications.
      * This is only called when all users are done using this Servlet.
      */
     public static void destroyServlet()
     {
         Utility.getLogger().info("ServletTask destroy()");
 //+     if (m_application != null)
         {   // Never
 //+         m_application.removeTask(this);
 //+         m_application = null; // Do not free the application... the HttpSession will
         }
         Environment env = null;
 //+     if (m_application != null)
 //+         env = m_application.getEnvironment();
         if (env == null)
             env = Environment.getEnvironment(null);   // Make sure environment is inited
         // If all the applications are free (except for the MainApplication), free the environment.
         Utility.getLogger().info("App count " + env.getApplicationCount());
 //+        env.free();     // Done with environment if there are no more apps
     }
     /**
      * Free all the resources belonging to this applet. If all applet screens are closed, shut down the applet.
      */
     public void free()
     {
     	if (m_recordOwnerCollection != null)
     		m_recordOwnerCollection.free();
     	m_recordOwnerCollection = null;
         if (m_application != null)
             m_application.removeTask(this);   // Remove this session from the list
     }
     /**
      * Process an HTML get or post.
      * @exception ServletException From inherited class.
      * @exception IOException From inherited class.
      */
     public void doProcess(BasicServlet servlet, HttpServletRequest req, HttpServletResponse res, PrintWriter out) 
         throws ServletException, IOException
     {
         // Override this
     }
     /**
      * Clean-up the param string
      */
     public static String cleanParam(String[] strParams, boolean bSetDefault, String strDefault)
     {
         if ((strParams == null) || (strParams.length == 0))
             strParams = new String[1];
         if (strParams[0] == null) if (bSetDefault)
             strParams[0] = strDefault;
         if (strParams[0] == null)
             return null;
         try   {
             return URLDecoder.decode(strParams[0], DBConstants.URL_ENCODING);
         } catch (java.io.UnsupportedEncodingException ex) {
             return strParams[0];
         }
     }
     /**
      * Return the 
      */
     public HttpServletRequest getServletRequest()
     {
         return null;
     }
     /**
      * Get the first occurence of this parameter.
      */
     public static String getParam(HttpServletRequest req, String strParam)
     {
         return BaseHttpTask.getParam(req, strParam, Constants.BLANK);
     }
     /**
      * Get the first occurrence of this parameter.
      */
     public static String getParam(HttpServletRequest req, String strParam, String strDefault)
     {
         String strParams[] = null;
         if (req == null)
             return null;
         strParams = req.getParameterValues(strParam);              // Menu page
         if (DBParams.URL.equals(strParam))
         {
             strParams = new String[1];
             strParams[0] = req.getRequestURL().toString();        // Special param (url).
         }
         if (DBParams.CODEBASE.equals(strParam))
         {
             strParams = new String[1];
             strParams[0] = req.getRequestURL().toString();        // Special param (url).
             if (strParams[0].endsWith(Constants.DEFAULT_SERVLET))
             	strParams[0] = strParams[0].substring(0, strParams[0].length() - Constants.DEFAULT_SERVLET.length());
             if (!strParams[0].endsWith("/"))
             	strParams[0] = strParams[0] + "/";	// Codebase should always end with this
         }
         if (DBParams.DOMAIN.equals(strParam))
         {
             strParams = new String[1];
             strParams[0] = req.getRequestURL().toString();        // Special param (url).
             return Utility.getDomainFromURL(strParams[0], null);
         }
         if (strParams == null)
             if (DBParams.DATATYPE.equals(strParam))
         {
             strParams = new String[1];
             strParams[0] = req.getRequestURL().toString();        // Special param (url).
             if ((strParams[0] != null)
                 && (strParams[0].lastIndexOf('/') != -1))
             {
                 strParams[0] = strParams[0].substring(strParams[0].lastIndexOf('/') + 1);
                 if (strParams[0].indexOf('.') != -1)
                     strParams[0] = strParams[0].substring(0, strParams[0].indexOf('.'));
                 if ((!DBParams.TABLE_PARAM.equalsIgnoreCase(strParams[0]))
                     && (!DBParams.WEBSTART_PARAM.equalsIgnoreCase(strParams[0]))
                     && (!DBParams.WEBSTART_APPLET_PARAM.equalsIgnoreCase(strParams[0]))
                     && (!DBParams.WSDL_PARAM.equalsIgnoreCase(strParams[0]))
                     && (!DBParams.IMAGE_PATH.equalsIgnoreCase(strParams[0])))
                         strParams = null;   // Must be one of these                 
             }
             else
                 strParams = null;
         }
 
         if (strParams == null)
             return strDefault;
         if (strParams.length == 0)
             return Constants.BLANK;
         if (strParams[0] == null)
             return Constants.BLANK;
         return strParams[0];
     }
     /**
      * Get this property.
      */
     public String getProperty(String strProperty)
     {
         String strValue = null;
         if (m_properties != null)
         {   // Try local properties
             if (m_properties.get(strProperty) != null)
                 strValue = m_properties.get(strProperty).toString();
             if ((strValue != null) && (strValue.length() > 0))
                 return strValue;
         }
         if (this.getServletRequest() != null)
             strValue = BaseHttpTask.getParam(this.getServletRequest(), strProperty, null);
         if (strValue != null)
             return strValue;
         if (DBParams.SERVLET.equalsIgnoreCase(strProperty))
         {   // Special case - looking for the name of the servlet
             if (this.getServletRequest() != null)
             {
                 String strContextPath = this.getServletRequest().getRequestURI();
                 if (strContextPath != null)
                     if (strContextPath.lastIndexOf('/') != -1)
                         strContextPath = strContextPath.substring(strContextPath.lastIndexOf('/') + 1);
                 if (strContextPath != null)
                     return strContextPath;
             }
         }
         if (this.getApplication() != null)
             strValue = this.getApplication().getProperty(strProperty);  // Try app
         return strValue;
     }
     /**
      * Retrieve/Create a user properties record with this lookup key.
      * @param strPropertyCode The key I'm looking up.
      * @return The UserProperties for this registration key.
      */
     public PropertyOwner retrieveUserProperties(String strRegistrationKey)
     {
         if (this.getApplication() != null)
             return this.getApplication().retrieveUserProperties(strRegistrationKey);
         return null;
     }
     /**
      * Get the parent application.
      * @return The application.
      */
     public App getApplication()
     {
         return m_application;
     }
     /**
      * This is task's parent application.
      */
     public void setApplication(App application)
     {
         m_application = (Application)application;
     }
     /**
      * Set the properties.
      * @param strProperties The properties to set.
      */
     public void setProperties(Map<String, Object> properties)
     {
         m_properties = properties;
     }
     /**
      * Set the properties.
      * @param strProperties The properties to set.
      */
     public Map<String, Object> getProperties()
     {
         return m_properties;
     }
     /**
      * Set this property.
      * @param strProperty The property key.
      * @param strValue The property value.
      */
     public void setProperty(String strProperty, String strValue)
     {
         if (m_properties == null)
             m_properties = new Hashtable<String,Object>();
         if (strValue != null)
             m_properties.put(strProperty, strValue);
         else
             m_properties.remove(strProperty);
     }
     /**
      * Get all the parameters passed in the URL or POST as a properties object.
      * NOTE: Remember to URLDecoder.decode if the data was encoded (Should have been!)
      * @param bIncludeURL Include the URL as a baseURL property?
      * @return The properties.
      */
     public Map<String,Object> getRequestProperties(HttpServletRequest req, boolean bIncludeURL)
     {
         Map<String,Object> propOut = new Hashtable<String,Object>();
         Map<?,?> properties = req.getParameterMap();
         for (Map.Entry<?,?> entry : properties.entrySet())
         {
             String strKey = (String)entry.getKey();
             String strValue = req.getParameter(strKey);
             propOut.put(strKey, strValue);
         }
         
         if (bIncludeURL)
         {
             String strURL = req.getRequestURL().toString();
             if (strURL != null)
             {
                 propOut.put(DBParams.URL, strURL);
                 propOut.put(DBParams.DOMAIN, Utility.getDomainFromURL(strURL, null));  // Program will probably need domain name
                 String strCodeBase = Utility.getDomainFromURL(strURL, req.getContextPath(), true);
                 if (strCodeBase == null)
                     strCodeBase = DBConstants.BLANK;
                 else if (!strCodeBase.startsWith("http://"))
                     strCodeBase = "http://" + strCodeBase;
                 propOut.put(DBParams.BASE_URL, Utility.encodeXML(strCodeBase)); // I don't call it DBParams.CODEBASE, because it is often passed as parameter
             }
         }
 
         return propOut;
     }
     /**
      * This is a special method that runs some code when this screen is opened as a task.
      */
     public void run()
     {
         // This will never be called for a Servlet.
     }
     /**
      * This is a special method that stops the code when this screen is opened as a task.
      */
     public void stopTask()
     {
        Utility.getLogger().warning("error: stop() can never be called for a Servlet");
        new Exception().printStackTrace();
     }
     /**
      * Is this task currently involved in computations?
      * @return True if the task is currently active.
      */
     public boolean isRunning()
     {
     	return true;
     }
     /**
      * If this task object was created from a class name, call init(xxx) for the task.
      * You may want to put logic in here that checks to make sure this object was not already inited.
      * Typically, you init a Task object and pass it to the job scheduler. The job scheduler
      * will check to see if this task is owned by an application... if not, initTask() is called.
      */
     public void initTask(App application, Map<String, Object> properties)
     {
         Utility.getLogger().warning("error: initTask() can never be called for a Servlet");
         new Exception().printStackTrace();
     }
     protected String m_strLastError = Constants.BLANK;  // Last Error message
     protected static int m_iLastErrorCode = -2;
     /**
      * Get the last error code.
      * This call clears the last error code.
      * @param iErrorCode Pass the error code of the last error code or 0 to get the last error.
      */
     public String getLastError(int iErrorCode)
     {
         if ((m_strLastError == null) || ((iErrorCode != 0) && (iErrorCode != m_iLastErrorCode)))
             return Constants.BLANK;
         String string = m_strLastError;
         m_strLastError = null;
         return string;
     }
     /**
      * Set the last (next) error code to display.
      */
     public int setLastError(String strLastError)
     {
         m_strLastError = strLastError;
         if (m_iLastErrorCode > -2)
             m_iLastErrorCode = -2;
         return --m_iLastErrorCode;
     }
     /**
      * Display this status message in the status box or at the bottom of the browser.
      */
     public void setStatusText(String strStatus)
     {
         this.setStatusText(strStatus, Constants.INFORMATION);
     }
     /**
      * Display this status message in the status box or at the bottom of the browser.
      * @param bWarning If true, display a message box that the user must dismiss.
      */
     public void setStatusText(String strStatus, int iWarningLevel)
     {
         if (strStatus == null)
             strStatus = Constants.BLANK;
         m_strCurrentStatus = strStatus;
         m_iCurrentWarningLevel = iWarningLevel;
     }
     /**
      * Get the last status message if it is at this level or above.
      * Typically you do this to see if the current message you want to display can
      * be displayed on top of the message that is there already.
      * Calling this method will clear the last status text.
      * @param iWarningLevel The maximum warning level to retrieve.
      * @return The current message if at this level or above, otherwise return null.
      */
     public String getStatusText(int iWarningLevel)
     {
         String strStatus = m_strCurrentStatus;
         if (m_iCurrentWarningLevel < iWarningLevel)
             strStatus = null;
         return strStatus;
     }
     /**
      * Get the remote server for this task.
      */
     public Object getRemoteTask()
     {
         return  this.getApplication().getRemoteTask(this);
     }
     /**
      * See if this user has a persisent userID cookie and set up preferences.
      * Note: This method is only called if the user does not have a current session.
      * The username and password are used for authentication here:
      * user=null, password=null - No change
      * user=user, password=null - Sign-on (login level)
      * user=user, password=pass - Sign-on (authenticated level)
      * user=blank,password=null - Sign-off (anonymous user)
      * username can be user(name) or userid.
      * password can be password or auth(entication) token.
      */
     public App findApplication(HttpServletRequest req, HttpServletResponse res)
     {
         ServletApplication application = null;
         String strDatatype = BaseHttpTask.getParam(req, DBParams.DATATYPE, null); // Raw data (such as in image from the DB)
         if (strDatatype != null)    // NOTE: I hope there isn't an issue with getting a param before setting the data type, but I have no choice.
         { // Special case - if asking for a datatype, don't need a session, use a default application!
             return this.getNonUserApplication();
         }
 
     // First, see if you are in a current session, if not create one
         HttpSession session = req.getSession();
         String strUserNameOrID = BaseHttpTask.cleanParam(req.getParameterValues(DBParams.USER_NAME), false, null);
         if (strUserNameOrID == null)
             strUserNameOrID = BaseHttpTask.cleanParam(req.getParameterValues(DBParams.USER_ID), false, null);
         if (!session.isNew())
         {   // Session in progress
             application = (ServletApplication)session.getAttribute(DBParams.APPLICATION);    // Get the currently instantiate app getValue(APPLICATION);  //
             if (application != null)
             { // Always
                 if (strUserNameOrID != null)    // user= means new user
                 {
                     boolean bNewUser = false;
                     if ((strUserNameOrID.length() == 0) && (BaseHttpTask.cleanParam(req.getParameterValues(DBParams.PASSWORD), false, null) == null)) // This means logout
                         bNewUser = true;  // Different user or up the security level
                     if (strUserNameOrID.length() > 0)
                     {
                         if ((BaseHttpTask.cleanParam(req.getParameterValues(DBParams.PASSWORD), false, null) != null)
                                 && (BaseHttpTask.cleanParam(req.getParameterValues(DBParams.PASSWORD), false, null).length() > 0))
                                     bNewUser = true;    // Supplying a password = sign on to new
                         if ((BaseHttpTask.cleanParam(req.getParameterValues(DBParams.AUTH_TOKEN), false, null) != null)
                                 && (BaseHttpTask.cleanParam(req.getParameterValues(DBParams.AUTH_TOKEN), false, null).length() > 0))
                                     bNewUser = true;    // Supplying authentication = sign on to new
                         if ((!strUserNameOrID.equalsIgnoreCase(application.getProperty(DBParams.USER_NAME)))
                             && (!strUserNameOrID.equalsIgnoreCase(application.getProperty(DBParams.USER_ID))))
                                 bNewUser = true;
                     }
                     if (bNewUser)
                     { // They want to change users
                         session.removeAttribute(DBParams.APPLICATION); // This also frees the application (through the xxx interface)    //removeValue(APPLICATION);   //
                         application = null;
                     }
                 }
                 if (application != null)
                 {
                     application = this.changeCookie(application, req, res);
                     application.addTask(this, null);      // Will be removed on exit
                     this.setApplication(application);
 
                     return application;     // Return the current application
                 }
             }
         }
         Utility.getLogger().info("New session");
     // Next, see if they are changing their userID/username
         if (strUserNameOrID != null)    // Note user= means "new user"
         { // Special logic - change User name/ID
             Map<String,Object> properties = new Hashtable<String,Object>();
             Map<String,Object> appProperties = this.getApplicationProperties(false);
             if (appProperties != null)
                 properties.putAll(appProperties);
             this.addDatabaseProperties(properties);
             properties.remove(Params.USER_ID);	// Being careful
             properties.put(Params.USER_NAME, strUserNameOrID);
             application = new ServletApplication(null, properties, null);    // Read/Create the new user ID/Name
             if (this.getApplication() == null)
             {
             	application.addTask(this, null);
             	this.setApplication(application);
             }
             int iLoginError = DBConstants.NORMAL_RETURN;
             if ((strUserNameOrID.length() > 0)
                 && (!strUserNameOrID.equalsIgnoreCase(application.getProperty(DBParams.USER_ID)))
                     && (!strUserNameOrID.equalsIgnoreCase(application.getProperty(DBParams.USER_NAME))))
                 iLoginError = DBConstants.ERROR_RETURN;   // Invalid user - Show the sign-up screen
             else
             {
                 String strPassword = BaseHttpTask.cleanParam(req.getParameterValues(DBParams.PASSWORD), false, null);
                 if ((strPassword != null) && (strPassword.length() > 0))
                 {
                     try {
                         byte[] bytes = strPassword.getBytes(Base64.DEFAULT_ENCODING);
                         bytes = Base64.encodeSHA(bytes); // TODO (don) Fix this
                         char[] chars = Base64.encode(bytes);
                         strPassword = new String(chars);
                     } catch (NoSuchAlgorithmException ex) {
                         ex.printStackTrace();
                         strPassword = null;
                     } catch (UnsupportedEncodingException ex) {
                         ex.printStackTrace();
                         strPassword = null;
                     }
                 }
                 if ((strPassword == null) || (strPassword.length() == 0))
                 {
                     strPassword = BaseHttpTask.cleanParam(req.getParameterValues(DBParams.AUTH_TOKEN), false, null);
                     strPassword = application.authenticateToken(strPassword);	// TODO(don) HACK - For now, authentication is the SHA password! See MainApplication/139
                 }
                 if ((strPassword != null) && (strPassword.length() > 0))
                 {
                     String strDomain = Utility.getDomainFromURL(BaseHttpTask.getParam(req, DBParams.URL), null);
                     iLoginError = application.login(this, strUserNameOrID, strPassword, strDomain);
                 }
             }
             if (iLoginError != DBConstants.NORMAL_RETURN)
             {
                 application.login(this, null, null, null);
                 application.setProperty(Params.SECURITY_LEVEL, Integer.toString(Constants.LOGIN_USER));    // This will cause a security error, which will display the login screen
                 if (iLoginError == DBConstants.ERROR_RETURN)
                     application.setProperty(Params.SECURITY_MAP, Application.CREATE_USER_REQUIRED);
                 else
                     application.setProperty(Params.SECURITY_MAP, Application.LOGIN_REQUIRED);
             }
         }
         
         application = this.changeCookie(application, req, res);
         
         session.setAttribute(DBParams.APPLICATION, application);   // Cache the preferences, so I can save my session putValue(APPLICATION, application);
         // Note: Application will be notified of session termination in valueUnbound method
         application.addTask(this, null);      // Will be removed on session destroy
         this.setApplication(application);
         return application;
     }
     /**
      * 
      * @return
      */
     public Map<String, Object> getApplicationProperties(boolean bCreateIfNew)
     {
         Map<String,Object> properties = gApplicationProperties.get(m_servletType);
         if (properties == null)
             if (bCreateIfNew)
         {
             properties = new Hashtable<String,Object>();
             gApplicationProperties.put(m_servletType, properties);
         }
         return properties;
     }
     /**
      * addDatabaseProperties
      * @param properties
      */
     public void addDatabaseProperties(Map<String,Object> properties)
     {
         Map<String,Object> propRequest = this.getRequestProperties(this.getServletRequest(), false);
         if (propRequest != null)
         {   // Also move any application params up.
             for (String key : propRequest.keySet())
             {   // Move these params to the application
                 if ((key.endsWith(BaseDatabase.DBSHARED_PARAM_SUFFIX))
             		|| (key.endsWith(BaseDatabase.DBUSER_PARAM_SUFFIX))
                     || (key.equals(DBConstants.SUB_SYSTEM_LN_SUFFIX)))
                 		properties.put(key, propRequest.get(key));
             }
         }        
     }
     /**
      * 
      * @param application
      * @param req
      * @param res
      * @param bClearCookie
      * @param bSetCookie
      * @param strUserName
      * @return
      */
     public ServletApplication changeCookie(ServletApplication application, HttpServletRequest req, HttpServletResponse res)
     {
         boolean bClearCookie = false;
         boolean bSetCookie = false;
         if (DBConstants.YES.equalsIgnoreCase(BaseHttpTask.cleanParam(req.getParameterValues("saveuser"), false, null)))
             bSetCookie = true;
         String strUserName = BaseHttpTask.cleanParam(req.getParameterValues(DBParams.USER_NAME), false, null);
         if (strUserName != null)    // user= means new user
             if (bSetCookie == false)
                 bClearCookie = true;
         boolean bVerifyUser = false;
         if (application != null)
             if (m_servletType == BasicServlet.SERVLET_TYPE.COCOON)
                 bVerifyUser = true; // XML ajax javascript may have changed the user
         if ((!bSetCookie) && (!bClearCookie) && (!bVerifyUser) && (application != null))
             return application;
        // First, see if they have my cookie
         Cookie cookies[] = null;
         Cookie cookie = null;
         boolean bNewCookie = true;
         // We do all the cookie work before we start writing output,
         // since once we start writing data the headers (with or
         // without cookies) can get flushed at any time.
         if ((cookies = req.getCookies()) != null)
         {
             for (int i = 0; i < cookies.length; i++)
             {
                 if (cookies[i].getName().equals(HtmlConstants.COOKIE_NAME))
                 {
                     // clone this cookie to keep using the browser's
                     // version of the cookie protocol
                     cookie = (Cookie)cookies[i].clone();
                     bNewCookie = false;
                     break;
                 }
             }
         }
         if (application != null)
         {   // Set cookie to new user
             if (bVerifyUser)
             {   // This fixes a weird problem. If cocoon displays my initial page and I logon under ajax, I need to change the user to match the new cookie
                 String strUserID = Converter.stripNonNumber(application.getUserID());
                 if (cookie != null)
                 {
                     String strNewUserID = cookie.getValue();
                     String strDomain = cookie.getDomain();
                     if (strDomain == null)
                     {
                         Map<String,Object> properties = this.getInitialServletAppProperties(req);
                     	strDomain = (String)properties.get(DBParams.DOMAIN);
                     }
                     if (((strUserID != null) && (!strUserID.equals(strNewUserID)))
                         || ((strUserID == null) && (strNewUserID != null)))
                             application.login(this, strNewUserID, null, strDomain);   // Change to new user
                 }
                 if ((!bSetCookie) && (!bClearCookie))
                     return application;
             }
             if (bSetCookie)
             {
                 String strUserID = Converter.stripNonNumber(application.getUserID());
                 if (cookie == null)
                     cookie = new Cookie(HtmlConstants.COOKIE_NAME, strUserID);   // Set cookie
                 else
                     cookie.setValue(strUserID);
             }
         }
         else
         {
             if (cookie != null)
             {   // Get the user associated with this cookie
                 String strUserID = cookie.getValue();
             // In a future version, I would search though the cached applications for this user
                 Map<String,Object> properties = this.getInitialServletAppProperties(req);
                 properties.put(Params.USER_ID, strUserID);
                 application = new ServletApplication(null, properties, null);  // Find this User's profile
     
                 strUserID = Converter.stripNonNumber(strUserID);
                 int iOldUserID = -1;
                 try   {   // First see if strUser is the UserID in string format
                     if ((strUserID != null) && (strUserID.length() > 0))
                         iOldUserID = Integer.parseInt(strUserID);
                     if (iOldUserID == 0)
                         iOldUserID = -1;
                 } catch (NumberFormatException e) {
                     iOldUserID = -1;
                 }
                 String strNewUserID = Converter.stripNonNumber(application.getUserID());
                 int iNewUserID = -2;
                 if (strNewUserID.length() > 0)
                     iNewUserID = Integer.parseInt(strNewUserID);
     
                 try {
                     if (iOldUserID != iNewUserID)
                     {
                         cookie.setValue(strNewUserID); // Previous cookie's profile was invalid, use new cookie value
                         bSetCookie = true;
                     }
                 } catch (NumberFormatException e) {
                     cookie = null; // should never happen ...
                 }
             }
             else
             {
                 Map<String,Object> properties = this.getInitialServletAppProperties(req);
                 properties.put(Params.USER_NAME, DBConstants.BLANK);
                 application = new ServletApplication(null, properties, null);  // New User's profile                
             }
         }
         if ((bSetCookie) || (bClearCookie && (cookie != null)))
         {
             if (cookie == null)
             {   // No cookie exists, create a new one
                 String strUserID = Converter.stripNonNumber(application.getUserID());
                 cookie = new Cookie(HtmlConstants.COOKIE_NAME, strUserID);   // Set cookie
             }
             cookie.setComment("Saves your UserID for this site");
             cookie.setMaxAge(COOKIE_AGE);        // 1 year
             cookie.setPath("/");
             String strDomain = cookie.getDomain();
             if (strDomain != null)
                 if (strDomain.indexOf('.') > 0)
                     if (strDomain.indexOf('.') != strDomain.lastIndexOf('.'))
                         cookie.setDomain(strDomain.substring(strDomain.indexOf('.')));    // Entire domain
             if (res != null)
             {
                 if (((DBConstants.ANON_USER_ID.equalsIgnoreCase(cookie.getValue())) || (cookie.getValue().length() == 0))
                     || (bClearCookie))
                 {
                     if (bNewCookie)
                         cookie = null;
                     else
                     {
                         cookie.setValue(DBConstants.BLANK);
                         cookie.setMaxAge(-1);   // Expire when the browser is closed
                     }
                 }
                 if (cookie != null)
                     res.addCookie(cookie);
             }
         }
         return application;
     }
     public Map<String,Object> getInitialServletAppProperties(HttpServletRequest req)
     {
         Map<String,Object> properties = new Hashtable<String,Object>();
         String strDomain = Utility.getDomainFromURL(BaseHttpTask.getParam(req, DBParams.URL), null);
         if (strDomain != null)
             properties.put(Params.DOMAIN, strDomain);
         String strCodeBase = this.getRealPath(req, DBConstants.BLANK);
         if (strCodeBase != null)
         	if (!strCodeBase.endsWith(System.getProperty("file.separator")))
         		strCodeBase = strCodeBase + System.getProperty("file.separator");
         if (strCodeBase != null)
             properties.put(Params.CODEBASE, strCodeBase);
         return properties;
     }
     /**
      * Get the basic servlet.
      */
     public BasicServlet getBasicServlet()
     {
         return m_servlet;
     }
     /**
      * Get the physical path for this internet path.
      * @param request The request
      */
     public String getRealPath(HttpServletRequest request, String path)
     {
         if (m_servlet != null)
             return m_servlet.getRealPath(request, path);
         return path;
     }
     /**
      * A utility method to get an Input stream from a filename or URL string.
      * @param filename The filename or url to open as an Input Stream.
      * @return The imput stream (or null if there was an error).
      */
     @SuppressWarnings("resource")
     public InputStream getInputStream(String filename)
     {
         InputStream streamIn = null;
         if (filename.indexOf(':') == -1)
         {
             String filepath = ((BaseHttpTask)this.getTask()).getRealPath(this.getServletRequest(), filename);
             if (filepath != null)
             {
 	            File file = new File(filepath);
 	            try   {
 	                streamIn = new FileInputStream(file);
 	            } catch (FileNotFoundException ex)  {
 	                streamIn = null;
 	            }
             }
         }
         if (streamIn == null)
             streamIn = Utility.getInputStream(filename, this.getApplication());
         return streamIn;
     }
     /**
      * Get the task for this record owner parent.
      * If this is a RecordOwner, return the parent task. If this is a Task, return this.
      * @return Record owner's environment, or null to use the default enviroment.
      */
     public Task getTask()
     {
         return this;
     }
     /**
      * For requests that do not require a user, return an application using me as the user.
      * pend(don) Is this cool to use the default application?
      */
     public App getNonUserApplication()
     {
         Environment env = null;
         if (this.getApplication() != null)
             env = ((BaseApplication)this.getApplication()).getEnvironment();
         if (env == null)
             env = Environment.getEnvironment(null);   // Make sure environment is inited
         return env.getDefaultApplication();
     }
     /**
      * Convert this key to a localized string.
      * In thin, this just calls the getString method in application,
      * in thick, a local resource can be saved.
      * @param strKey The key to lookup in the resource file.
      * @return The localized key.
      */
     public String getString(String strKey)
     {
         if (this.getApplication() != null)
             return this.getApplication().getString(strKey);
         return strKey;
     }
     /**
      * Can this task be the main task?
      * @return true If it can.
      */
     public boolean isMainTaskCandidate()
     {
         return true;    // All servlet sessions are candidates for the main task.
     }
     /**
      * Get the default lock strategy to use for this type of table.
      * @return The lock strategy.
      */
     public int getDefaultLockType(int iDatabaseType)
     {
         if ((iDatabaseType & Constants.REMOTE) != 0)
             return Constants.OPEN_LAST_MOD_LOCK_TYPE;
         if ((iDatabaseType & Constants.LOCAL) != 0)
             return Constants.OPEN_NO_LOCK_TYPE;
         return Constants.OPEN_NO_LOCK_TYPE;
     }
     /**
      * Add this record owner to my list.
      * @param recordOwner The recordowner to add
      */
     public boolean addRecordOwner(RecordOwnerParent recordOwner)
     {
     	return m_recordOwnerCollection.addRecordOwner(recordOwner);
     }
     /**
      * Remove this record owner to my list.
      * @param recordOwner The recordowner to remove.
      */
     public boolean removeRecordOwner(RecordOwnerParent recordOwner)
     {
     	return m_recordOwnerCollection.removeRecordOwner(recordOwner);
     }
 }
