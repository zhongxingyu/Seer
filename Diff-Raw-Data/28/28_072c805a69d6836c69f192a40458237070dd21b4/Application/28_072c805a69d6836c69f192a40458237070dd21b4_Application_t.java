 package org.jbundle.thin.base.util;
 
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.NoSuchElementException;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NameNotFoundException;
 import javax.naming.ServiceUnavailableException;
 import javax.rmi.PortableRemoteObject;
 import javax.swing.JApplet;
 
 import org.jbundle.model.App;
 import org.jbundle.model.BaseAppletReference;
 import org.jbundle.model.PropertyOwner;
 import org.jbundle.model.Task;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.db.Params;
 import org.jbundle.thin.base.message.BaseMessage;
 import org.jbundle.thin.base.message.BaseMessageHeader;
 import org.jbundle.thin.base.message.BaseMessageManager;
 import org.jbundle.thin.base.message.MapMessage;
 import org.jbundle.thin.base.message.remote.RemoteMessageManager;
 import org.jbundle.thin.base.remote.ApplicationServer;
 import org.jbundle.thin.base.remote.RemoteTask;
 import org.jbundle.thin.base.remote.proxy.ApplicationProxy;
 import org.jbundle.thin.base.thread.AutoTask;
 import org.jbundle.thin.base.thread.TaskScheduler;
 import org.jbundle.util.muffinmanager.MuffinManager;
 
 /**
  * A Application contains all of a single user's apps.
  * For client apps, there is only one Application class, for server apps there
  * will be an Application class for each client.
  * For example, a standalone app, an applet, an ongoing or stateless HTML Application,
  * a user's server Application, or a user's EJB app server Application.
  * <p/>
  * Some of the params that an Application uses are:
  * <pre>
  * user=nameOrID
  * remoteapp=tourapp (don't set this)
  * server=www.tourapp.com (You will need to do this for standalone apps).
  * codebase=classes (shouldn't need this)
  * resource=MyResources
  * lanugage=es
  * </pre>
  */
 public abstract class Application extends Object
     implements App, PropertyOwner
 {
     /**
      * The initial unique instance of this Applet/application.
      */
     private static BaseAppletReference gbaseApplet = null;
     /**
      * If multiple tasks are running, this is the scheduler.
      */
     protected TaskScheduler gTaskScheduler = null;
     /**
      * Current properties for this application.
      */
     protected Map<String,Object> m_properties = null;
     /**
      * Basic local resource for this application.
      */
     protected ResourceBundle m_resources = null;
     /**
      * Remote application server.
      */
     protected RemoteTask m_mainRemoteTask = null;
     /**
      * This Application's remote servers and tasks.
      */
     protected Map<Task, RemoteTask> m_mapTasks = null;
     /**
      * Main task
      */
     protected Task m_taskMain = null;
     /**
      * If application was started from the initial applet, allows me to call getProperty().
      */
     private JApplet m_sApplet = null;
     /**
      * The muffin manager for JNLP environments.
      */
     protected MuffinManager m_muffinManager = null;
     /**
      * The mailto protocol.
      */
     public static final String MAIL_TO = "mailto";
     /**
      * Remote connection types.
      */
     public static final int RMI = 1;
     public static final int PROXY = 2;
     public static final int LOCAL_SERVICE = 3;	// OSGi service
     public static final int REMOTE_SERVICE = 4;	// OSGi service
     public static final int DEFAULT_CONNECTION_TYPE = RMI;
 
     /**
      * Default constructor.
      */
     public Application()
     {
         super();
     }
     /**
      * Constructor.
      * Pass in the possible initial parameters.
      * @param env Environment is ignored in the thin context.
      * @param strURL The application parameters as a URL.
      * @param args The application parameters as an initial arg list.
      * @param applet The application parameters coming from an applet.
      */
     public Application(Object env, Map<String,Object> properties, JApplet applet)
     {
         this();
         this.init(env, properties, applet); // The one and only
     }
     /**
      * Initialize the Application.
      * @param env Environment is ignored in the thin context.
      * @param strURL The application parameters as a URL.
      * @param args The application parameters as an initial arg list.
      * @param applet The application parameters coming from an applet.
      */
     public void init(Object env, Map<String,Object> properties, JApplet applet)
     {
         if (properties == null)
             properties = new Hashtable<String,Object>();
         if (m_properties == null)
             m_properties = properties;
         else
             m_properties.putAll(properties);
         m_mapTasks = new HashMap<Task,RemoteTask>();
         if (applet != null)
             m_sApplet = applet;
         try   {
             Class.forName("javax.jnlp.PersistenceService"); // Test if this exists
             this.setMuffinManager(new MuffinManager(this));
             if (this.getMuffinManager().isServiceAvailable())
                 if (this.getProperty(Params.REMOTE_HOST) == null)
                     this.setProperty(Params.REMOTE_HOST, this.getAppServerName());
         } catch (Exception ex)  { // Test for JNLP
         }
         String strUser = this.getProperty(Params.USER_ID);
         if (strUser == null) // if (strUser.length() == 0) then you don't want the current sys username
             if (this.getProperty(Params.USER_NAME) == null)
                 if (this.getMuffinManager() != null)
         {
             strUser = this.getMuffinManager().getMuffin(Params.USER_ID);
             this.setProperty(Params.USER_ID, strUser);   // User name is required
         }
         if (strUser == null) // if (strUser.length() == 0) then you don't want the current sys username
             if (this.getProperty(Params.USER_NAME) == null)
         {
             try   {
                 strUser = System.getProperties().getProperty("user.name");
                 this.setProperty(Params.USER_NAME, strUser);   // User name is required
             } catch (java.security.AccessControlException ex) {
                 // Ignore this, I'm probably runing in an Applet
             }
         }
     }
     /**
      * Free all the resources belonging to this application.
      */
     public void free()
     {
         if (m_mapTasks != null)
         {
             while (m_mapTasks.size() > 0)
             {
                 for (Task task : m_mapTasks.keySet())
                 {
                     if (task != null)
                     {
                     	int iCount = 0;
                         while (task.isRunning())  // This will also remove this from the list
                         {
                         	if (iCount++ == 10)
                         	{
                                 Util.getLogger().warning("Shutting down a running task");   // Ignore and continue
                         		break;
                         	}
                         	try {
 								Thread.sleep(100);
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
                         }
                        this.removeTask(task);	// Make sure this doesn't free me (if all tasks are freed, app will be freed)
                        task.setApplication(null);
                         task.stopTask();
                     }
                     else
                     {
                         try {
                             RemoteTask remoteTask = m_mapTasks.remove(task);
                             if (remoteTask != null)
                                 remoteTask.freeRemoteSession();
                         } catch (RemoteException ex)    {
                             ex.printStackTrace();
                         }
                     }
                     break;  // YES, This only loops through the FIRST one, because it is removed and I would get a concurrent mod error
                 }
             }
         }
         m_mapTasks = null;
     }
     /**
     * Get the system record owner.
     * This is used to keep from indefinite recursion.
     * This is used in overriding apps
     */
     public PropertyOwner getSystemRecordOwner()
     {
         return null;
     }
     /**
      * Get the main task
      */
     public Task getMainTask()
     {
         return m_taskMain;
     }
     /**
      * Get the task at this index.
      * @param index The index of the task to retrieve.
      * @return the task.
      */
     public Map<Task, RemoteTask> getTaskList()
     {
         return m_mapTasks;
     }
     /**
      * Add this session, screen, or task that belongs to this application.
      * @param objSession Session to remove.
      * @return Number of remaining sessions still active.
      */
     public int addTask(Task task, RemoteTask remoteTask)
     {
         if (m_taskMain == null)
         {
             if (task != null)
                 if (task.isMainTaskCandidate())
                     m_taskMain = task;
         }
         m_mapTasks.put(task, remoteTask);
         return m_mapTasks.size();
     }
     /**
      * Remove this session, screen, or task that belongs to this application.
      * @param objSession Session to remove.
      * @return True if all the potential main tasks have been removed.
      */
     public boolean removeTask(Task task)
     {
         if (!m_mapTasks.containsKey(task))
             Util.getLogger().warning("Attempt to remove non-existent task");
         RemoteTask remoteTask = m_mapTasks.remove(task);
         if (remoteTask != null)
         {
             try   {
                 remoteTask.freeRemoteSession();
             } catch (RemoteException ex)    {
                 ex.printStackTrace();
             }
         }
         synchronized (this)
         {
 	        boolean bEndOfTasks = true;
 	        if (m_taskMain == task)
 	        {
 	            if (m_mapTasks.size() == 0)
 	                m_taskMain = null;
 	            else
 	            {
 	                m_taskMain = null;
 	                for (Task task2 : m_mapTasks.keySet())
 	                {
 	                    if (task2.isMainTaskCandidate())
 	                    {
 	                        m_taskMain = task2; // Main task can only be an applet
 	                        break;  // Preferred
 	                    }
 	                    if (task2.isRunning())
 	                    {
 	                    	bEndOfTasks = false;    // There are more main tasks to go!
 	                    	continue;
 	                    }
 	                }
 	            }
 	        }
 	        if (m_taskMain != null)
 	            return false;    // There are more main tasks to go!
 	        return bEndOfTasks;    // All Main task candidates are GONE!
         }
     }
     /**
      * Get the task scheduler for this application.
      * Create the default job scheduler if it doesn't exist.
      * @return The task scheduler.
      */
     public TaskScheduler getTaskScheduler()
     {
         if (gTaskScheduler == null)
             gTaskScheduler = new TaskScheduler(this, 0);    // Create a default scheduler
         return gTaskScheduler;
     }
     /**
      * Set the task scheduler.
      * @param taskScheduler The task scheduler.
      * @return The task scheduler.
      */
     public TaskScheduler setTaskScheduler(TaskScheduler taskScheduler)
     {
         gTaskScheduler = taskScheduler;
         return gTaskScheduler;
     }
     /**
      * Get a connection to the server for this applet.
      * @param localTaskOwner The task that will own this remote task (or application) server) [If null, get the app server].
      * @return The server object (application defined).
      */
     public RemoteTask getRemoteTask(Task localTaskOwner)
     {
         return this.getRemoteTask(localTaskOwner, null, true);
     }
     /**
      * Get the connection to the server for this applet.
      * Optionally create the server connection.
      * @param localTaskOwner The task that will own this remote task (or application) server) [If null, get the app server].
      * @param strUserID The user id (or name) to initialize the server's application to.
      * @param bCreateIfNotFound If the server is null, initialize the server.
      * @return The server object (application defined).
      */
     public RemoteTask getRemoteTask(Task localTaskOwner, String strUserID, boolean bCreateIfNotFound)
     {
         return this.getRemoteTask(localTaskOwner, strUserID, null, bCreateIfNotFound);
     }
     /**
      * Get the connection to the server for this applet.
      * Optionally create the server connection.
      * @param localTaskOwner The task that will own this remote task (or application) server) [If null, get the app server].
      * @param strUserID The user id (or name) to initialize the server's application to.
      * @param bCreateIfNotFound If the server is null, initialize the server.
      * @return The server object (application defined).
      */
     public RemoteTask getRemoteTask(Task localTaskOwner, String strUserID, String strPassword, boolean bCreateIfNotFound)
     {
         if (localTaskOwner == null)
             localTaskOwner = m_taskMain;   // No task = main task
         RemoteTask server = m_mapTasks.get(localTaskOwner);
         if (server == null)
             if (bCreateIfNotFound)
         {
             String strServer = this.getAppServerName();
             String strRemoteApp = this.getProperty(Params.REMOTEAPP);
             if ((strRemoteApp == null) || (strRemoteApp.length() == 0))
                 strRemoteApp = Params.DEFAULT_REMOTE_APP;
             if (strUserID == null)
                 strUserID = this.getProperty(Params.USER_ID);
             if (strPassword == null)
                 strPassword = this.getProperty(Params.PASSWORD);
             server = this.createRemoteTask(strServer, strRemoteApp, strUserID, strPassword);
             if (localTaskOwner == null)
             	localTaskOwner = new AutoTask(this, null, null);	// Default task = server task
             this.addTask(localTaskOwner, server);	// NOTE: IF autotask was just created, I re-add this with a remote pointer
         }
         return server;
     }
     /**
      * Connect to the remote server and get the remote server object.
      * @param strServer The (rmi) server.
      * @param The remote application name in jndi index.
      * @return The remote server object.
      */
     public RemoteTask createRemoteTask(String strServer, String strRemoteApp, String strUserID, String strPassword)
     {
         RemoteTask remoteTask = null;
         if (Params.NONE.equalsIgnoreCase(strServer))
             return null;    // No server specified, don't use one.
         try {
             if (strUserID == null)
                 strUserID = this.getProperty(Params.USER_ID);
             String strLanguage = this.getLanguage(true);
             Map<String,Object> properties = this.getServerProperties();
             if (strUserID != null)
                 properties.put(Params.USER_ID, strUserID);
             if (strLanguage != null)
 	            if (strLanguage.length() > 0)
     	            properties.put(Params.LANGUAGE, strLanguage);
             if (properties.get(Params.DOMAIN) == null)
             	if (this.getProperty(Params.DOMAIN) != null)
             		properties.put(Params.DOMAIN, this.getProperty(Params.DOMAIN));
             if (m_mainRemoteTask == null)
             {
                 ApplicationServer appServer = null;
                 int iConnectionType = DEFAULT_CONNECTION_TYPE;
                 if (this.getMuffinManager() != null)
                     if (this.getMuffinManager().isServiceAvailable())
                         iConnectionType = PROXY;    // HACK - Webstart gives a warning when using RMI
                 String strConnectionType = this.getProperty("connectionType");
                 if (strConnectionType != null)
                 {
                 	if ("proxy".equalsIgnoreCase(strConnectionType))
                 		iConnectionType = PROXY;
                 	if ("rmi".equalsIgnoreCase(strConnectionType))
                 		iConnectionType = RMI;
                 	if (Util.isNumeric(strConnectionType))
                 		iConnectionType = Integer.parseInt(strConnectionType);
                 }
                 if (iConnectionType == RMI)
                 {
                     try {
                         Hashtable<String,String> env = new Hashtable<String,String>();
                         env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
                         env.put(Context.PROVIDER_URL, "rmi://" + strServer);    // + ":1099");  // The RMI server port
                         Context initial = new InitialContext(env);
                         Object objref = initial.lookup(strRemoteApp);
                         if (objref == null)
                             return null;    // Ahhhhh, The app is not registered.
             
                         appServer = (ApplicationServer)PortableRemoteObject.narrow(objref, org.jbundle.thin.base.remote.ApplicationServer.class);
                     } catch (NameNotFoundException ex) {
                         return null;    // Error - not found
                     } catch (ServiceUnavailableException ex) {
                         return null;    // Error - not found
                     } catch (Exception ex) {
                         ex.printStackTrace();
             //        } catch (java.net.ConnectException ex) {
                         // Try tunneling through http
                         iConnectionType = PROXY;
                     }
                 }
                 if (iConnectionType == PROXY)
                 {   // Use HTTP proxy instead of RMI
                     String strBaseServletPath = this.getBaseServletPath();
                     appServer = new ApplicationProxy(strServer, strBaseServletPath, strRemoteApp);
                 }
                 if (iConnectionType == LOCAL_SERVICE)
                 {   // Use local OSGi service instead of RMI
                 	if (Util.getClassFinder() != null)
                 		appServer = (ApplicationServer)Util.getClassFinder().getClassBundleService(null, "org.jbundle.base.remote.rmiserver.RemoteSessionActivator");
                 }
                 remoteTask = appServer.createRemoteTask(properties);
                 m_mainRemoteTask = remoteTask;
             }
             else
             {
                 remoteTask = m_mainRemoteTask.createRemoteTask(properties);
             }
         } catch (RemoteException ex)    {
             ex.printStackTrace();
         }
         return remoteTask;
     }
     /**
      * Get the path to the base servlet.
      * NOTE: This DOES NOT get the servlet path, it gets the path up to the servlet.
      * ie., if the servlet is http://www.xyz.com:3433/abc/tourapp I return http://www.xyz.com:3433/abc.
      * This is useful for accessing the proxy or report generator.
      * @return The servlet path (NOT including the trailing '/'.
      */
     public String getBaseServletPath()
     {
         URL urlCodeBase = this.getCodeBase(null);
         String strCodeBase = urlCodeBase.toString();
         if (strCodeBase != null)
             if (strCodeBase.endsWith("/"))
                 strCodeBase = strCodeBase.substring(0, strCodeBase.length() - 1);
         return strCodeBase;
     }
     /**
      * Get the base properties to pass to the server.
      * @return The base server properties
      */
     public Map<String,Object> getServerProperties()
     {
         return new Hashtable<String,Object>();      // Override this to add more
     }
     /**
      * Get the URL of the server.
      * @return The remote server name.
      */
     public String getAppServerName()
     {
         String strServer = this.getProperty(Params.REMOTE_HOST);
         if ((strServer == null) || (strServer.length() == 0))
         {
             URL urlCodeBase = this.getCodeBase();
             if (urlCodeBase != null)
                 strServer = urlCodeBase.getHost();
         }
         return strServer;
     }
     /**
      * Convert this filename to a valid URL.
      * @param filename The filename to find.
      * @param applet The (optional) applet.
      * @return A URL to this filename (or null if not found).
      */
     private ClassLoader classLoader = null;
     private URL urlCodeBase = null;
     public URL getResourceURL(String filepath, BaseAppletReference task)
     {
    // Get current classloader
         if (classLoader == null)
         	classLoader = this.getClass().getClassLoader();
         if (urlCodeBase == null)
             urlCodeBase = this.getCodeBase(task);
         // Create icons
         URL url = Util.getResourceFromPathName(filepath, this.getMainTask(), false, urlCodeBase, classLoader);
         return url;
     }
     /**
      * Get the codebase for this application.
      * @return A URL for this codebase (or null if not found).
      */
     public URL getCodeBase()
     {
         return this.getCodeBase(null);
     }
     /**
      * Get the codebase for this application.
      * <pre>
      * Tries to get the codebase in the following order:
      * 1. From the applet codebase.
      * 2. From the jnlp codebase.
      * 3. From the codebase param.
      * 4. From the server param
      * 5. From the current host name
      * 6. Localhost
      * </pre>
      * @param applet The (optional) applet.
      * @return A URL for this codebase (or null if not found).
      */
     public URL getCodeBase(BaseAppletReference applet)
     {
         URL urlCodeBase = null;
         if (applet != null)
             applet = applet.getApplet();    // Get the Applet if this is an applet.
         else
             if (Application.getRootApplet() != null)   // If they didn't pass me an applet, get the applet from the static reference.
                 applet = Application.getRootApplet().getApplet();
         if (applet != null)
             urlCodeBase = applet.getCodeBase();   // In an applet window.
         else
         { // Standalone.
             if (this.getMuffinManager() != null)
                 if (this.getMuffinManager().isServiceAvailable())
                     urlCodeBase = this.getMuffinManager().getCodeBase();
         }
         if (urlCodeBase == null)
         {
             String strCodeBase = this.getProperty(Params.CODEBASE);
             if ((strCodeBase == null) || (strCodeBase.length() == 0))
             {   // Now we're really guessing - Try the server for a codebase, then try the localhost
                 strCodeBase = this.getProperty(Params.REMOTE_HOST);
                 if ((strCodeBase == null) || (strCodeBase.length() == 0))
                 {
                     try   {
                         strCodeBase = InetAddress.getLocalHost().getHostName();
                     } catch (Exception ex)  {
                         strCodeBase = "localhost";
                     }
                 }
             }
             if (strCodeBase != null)
             {
                 if ((strCodeBase.startsWith("/")) || (strCodeBase.startsWith(System.getProperty("file.separator"))))
                     strCodeBase = "file:" + strCodeBase;
                 else if (!strCodeBase.startsWith("http://"))
                     strCodeBase = "http://" + strCodeBase;
                 try   {
                     urlCodeBase = new URL(strCodeBase);
                 } catch (MalformedURLException ex)  {
                     ex.printStackTrace();
                 }
             }
         }
         return urlCodeBase;
     }
     /**
      * Display this URL in a web browser.
      * Uses the applet or jnlp context.
      * @param strURL The local URL to display (not fully qualified).
      * @param iOptions ThinMenuConstants.HELP_WINDOW_CHANGE If help pane is already displayed, change to this content.
      * @param The applet (optional).
      * @return True if successfully displayed.
      */
     public abstract boolean showTheDocument(String strURL, BaseAppletReference applet, int iOptions);
     /**
      * Get this standard resource.
      * This is a utility method for loading and caching resources.
      * The returned Resources object can be called just like a resource object with some
      * extra functions.
      * @param strResourceName The class name of the resource file to load.
      * @param bReturnKeyOnMissing Return the string key if the resource is missing (typically, pass true).
      * @return The Resources object.
      */
     public ResourceBundle getResources(String strResourceName, boolean bReturnKeyOnMissing)
     {
         ResourceBundle resources = null;
         try   {
             Locale currentLocale = this.getLocale();    //Locale.getDefault();
             strResourceName = this.getResourcePath(strResourceName);
             if (m_resources != null)
                 if (strResourceName.equals(m_resources.getClass().getName()))
                     return m_resources;
             Task task = null;	// TODO ?? this.getMainTask() ??
             resources = Util.getResourceBundle(strResourceName, currentLocale, task, false, this.getClass().getClassLoader());
         } catch (MissingResourceException ex) {
         	Util.getLogger().warning("Missing resource " + strResourceName + " locale: " + this.getLocale());
             resources = null;
         }
         m_resources = resources;    // Last accessed
         return resources;
     }
     /**
      * Fix this resource name to point to the resources.
      * @param strResourceName The name to fix.
      * @return The full class name of the resource to get.
      */
     public String getResourcePath(String strResourceName)
     {
         if (strResourceName == null)
             strResourceName = THIN_RES_PATH + "Menu"; // ResourceConstants.MENU_RESOURCE;
         if ((!strResourceName.endsWith(RESOURCES))
             && (!strResourceName.endsWith(BUNDLE))
                 && (!strResourceName.endsWith(PROPERTIES)))
                     strResourceName = strResourceName + "Resources";
         if (strResourceName.indexOf('.') == -1)
             strResourceName = getResPackage() + strResourceName;
         if (strResourceName.indexOf('.') == 0)
             strResourceName = Constants.ROOT_PACKAGE + strResourceName.substring(1);
         return strResourceName;
     }
     /**
      * Get the location of the default resources.
      * @return the default resources.
      */
     public String getResPackage()
     {
         return THIN_RES_PATH;
     }
     public static final String THIN_RES_PATH = ".res.thin.base.screen.";
     public static final String RESOURCES = "Resources";
     public static final String BUNDLE = "Bundle";
     public static final String PROPERTIES = ".properties";    
     /**
      * Set the current resource file.
      * @param resources The resource object to set.
      */
     public void setResourceBundle(ResourceBundle resources)
     {
         m_resources = resources;
     }
     /**
      * Get the current resource file.
      * @return resources The resource object to set.
      */
     public ResourceBundle getResourceBundle()
     {
         return m_resources;
     }
     /**
      * Look this key up in the current resource file and return the string in the local language.
      * If no resource file is active, return the string.
      * @param string The key to lookup.
      * @return The local string.
      */
     public String getString(String string)
     {
         String strReturn = null;
         try   {
             if (m_resources == null)
                 this.setLanguage(null);  // If the resource file is not here, add the default file.
             if (m_resources != null)
                 if (string != null)
                     strReturn = m_resources.getString(string);
         } catch (MissingResourceException ex) {
             strReturn = null;
         }
         if ((strReturn != null) && (strReturn.length() > 0))
             return strReturn;
         else
             return string;
     }
     /**
      * Set the current language.
      * Change the current resource bundle to the new language.
      * <p>In your overriding code you might convert the actual language names to the two letter code:
      * <code>
      * if (language != null) if (language.length() > 2) if (language.indexOf('_') == -1)
      *  language = this.findLocaleFromLanguage(language);
      * </code>
      * <p>In your code, you should update all the current display fields when the language is changed.
      * @param language java.lang.String The language code.
      */
     public void setLanguage(String language)
     {
         Locale currentLocale = null;
         if (language == null)
             language = this.getProperty(Params.LANGUAGE);
         if (language != null)
         {
             currentLocale = new Locale(language, Constants.BLANK);
             m_resources = null;
         }
         if (currentLocale == null)
             currentLocale = Locale.getDefault();
         ResourceBundle resourcesOld = m_resources;
         String strResourcesName = this.getProperty(Params.RESOURCE);
         m_resources = this.getResources(strResourcesName, true);
         if (m_resources == null)
             m_resources = resourcesOld;
         this.setProperty(Params.LANGUAGE, language);
     }
     /**
      * Get the current language.
      * @param bCheckLocaleAlso If true, and language has not been set, return the system's language
      * @return The current language code.
      */
     public String getLanguage(boolean bCheckLocaleAlso)
     {
         String strLanguage = this.getProperty(Params.LANGUAGE);
         if ((strLanguage == null) || (strLanguage.length() == 0))
             if (bCheckLocaleAlso)
                 return Locale.getDefault().getLanguage();
         return strLanguage;
     }
     /**
      * Get the current locale.
      * (Based on the current language).
      * @return The current locale.
      */
     public Locale getLocale()
     {
         String strLanguage = this.getProperty(Params.LANGUAGE);
         if ((strLanguage != null) && (strLanguage.length() > 0))
         {
             Locale locale = Locale.getDefault();
             if (!strLanguage.equalsIgnoreCase(locale.getLanguage()))
                 locale = new Locale(strLanguage, "");
             if (locale != null)
             {
                 try {
                     Locale.setDefault(locale);
                 } catch (java.security.AccessControlException ex) {
                     Util.getLogger().warning("Can't change locale");   // Ignore and continue
                 }
             }
         }
         return Locale.getDefault();
     }
     /**
      * Set the application properties object.
      * @param properties The properties object.
      */
     public void setProperties(Map<String, Object> properties)
     {
         m_properties = properties;
     }
     /**
      * Get the properties.
      * @return A <b>copy</b> of the properties in the propertyowner.
      */
     public Map<String, Object> getProperties()
     {
         return m_properties;
     }
     /**
      * Get the value of this property key.
      * @param strParam The key to lookup.
      * @return The value for this key.
      */
     public String getProperty(String strProperty)
     {
         String strValue = null;
         if (m_properties != null)
             if (m_properties.get(strProperty) != null)
                 strValue = m_properties.get(strProperty).toString();
         if (strValue == null)
         	if (m_sApplet != null)	// Try applet properties if an Applet
             strValue = m_sApplet.getParameter(strProperty);   // Passed in as an applet param?
         return strValue;
     }
     /**
      * Set this property.
      * @param strProperty The property key.
      * @param strValue The property value.
      */
     public void setProperty(String strParam, String strValue)
     {
         if (strValue != null)
             m_properties.put(strParam, strValue);
         else
             m_properties.remove(strParam);
     }
     /**
      * Change the current user to this user and (optionally) validate password.
      * @param strPassword
      * @param strDomain The domain
      * @param strUser
      * @return normal_return if successful
      */
     public int login(Task task, String strUserName, String strPassword, String strDomain)
     {
         boolean bCreateServer = false;
         if (this.getProperty(Params.REMOTE_HOST) != null)
             bCreateServer = true;
         org.jbundle.thin.base.remote.RemoteTask remoteTask = (org.jbundle.thin.base.remote.RemoteTask)this.getRemoteTask(null, strUserName, bCreateServer);
         if (remoteTask == null)
         {
             if (task != null)
                 return task.setLastError(this.getResources("Error", true).getString("User remote session does not exist"));
             return Constants.ERROR_RETURN;
         }
         try {
             Map<String,Object> mapLoginInfo = remoteTask.login(strUserName, strPassword, strDomain);
             String strSecurityMap = (String)mapLoginInfo.get(Params.SECURITY_MAP);
             Map<String,Object> mapReturnParams = (Map)mapLoginInfo.get(Params.USER_PROPERTIES);      
 
             if (this.getSystemRecordOwner() != null)
                 this.getSystemRecordOwner().setProperties(mapReturnParams); // Thick implementation
             else
             {
                 if (this.getProperties() == null)
                     this.setProperties(mapReturnParams);    // Thin implementation
                 else
                     this.getProperties().putAll(mapReturnParams);
             }
             if (Util.isNumeric(strUserName))
                 strUserName = null;
             if (mapReturnParams.get(Params.USER_NAME) != null)
                 strUserName = (String)mapReturnParams.get(Params.USER_NAME);
             this.setProperty(Params.USER_NAME, strUserName);
             this.setProperty(Params.USER_ID, (String)mapReturnParams.get(Params.USER_ID));
             this.setProperty(Params.AUTH_TOKEN, (String)mapReturnParams.get(Params.AUTH_TOKEN)); // Save the remote authorization token
             this.setProperty(Params.CONTACT_ID, (String)mapReturnParams.get(Params.CONTACT_ID));
             this.setProperty(Params.CONTACT_TYPE, (String)mapReturnParams.get(Params.CONTACT_TYPE)); // Save the remote authorization token
             
             this.setProperty(Params.SECURITY_MAP, strSecurityMap);
             this.setProperty(Params.SECURITY_LEVEL, ((strPassword == null) || (strPassword.length() == 0)) ? Integer.toString(Constants.LOGIN_USER) : Integer.toString(Constants.LOGIN_AUTHENTICATED));
             if (("1"/*DBConstants.ANON_USER_ID*/.equals(this.getProperty(Params.USER_ID)))
                     || (this.getProperty(Params.USER_ID) == null))
                 this.setProperty(Params.SECURITY_LEVEL, Integer.toString(Constants.LOGIN_USER));   // Special case - If user is anonymous, level is always anonymous
             
             return Constants.NORMAL_RETURN;
         } catch (RemoteException ex) {
             if (task != null)
             {
             	String message = ex.getMessage();
             	if (ex.getCause() != null)
             		message = ex.getCause().getMessage();
                 return task.setLastError(message);
             }
             return Constants.ERROR_RETURN;            
         }
     }
     /**
      * Change the password.
      */
     public int changePassword(Task task, String strUserName, String strCurrentPassword, String strNewPassword)
     {
         int iErrorCode = Constants.NORMAL_RETURN;
         org.jbundle.thin.base.remote.RemoteTask remoteTask = (org.jbundle.thin.base.remote.RemoteTask)this.getRemoteTask(null, strUserName, false);
         Map<String,Object> properties = new HashMap<String,Object>();
         properties.put(Params.USER_NAME, strUserName);
         properties.put(Params.PASSWORD, strCurrentPassword);
         properties.put("newPassword", strNewPassword);
         try {
             Object objReturn = remoteTask.doRemoteAction(ThinMenuConstants.CHANGE_PASSWORD, properties);
             if (objReturn instanceof Integer)
                 iErrorCode = ((Integer)objReturn).intValue();
         } catch (Exception ex) {
             return task.setLastError(ex.getMessage());
         }
         return iErrorCode;
     }
     /**
      * Change the password.
      */
     public int createNewUser(Task task, String strUserName, String strCurrentPassword, String strNewPassword)
     {
         int iErrorCode = Constants.NORMAL_RETURN;
         org.jbundle.thin.base.remote.RemoteTask remoteTask = (org.jbundle.thin.base.remote.RemoteTask)this.getRemoteTask(null, strUserName, false);
         Map<String,Object> properties = new HashMap<String,Object>();
         properties.put(Params.USER_NAME, strUserName);
         properties.put(Params.PASSWORD, strCurrentPassword);
         try {
             Object objReturn = remoteTask.doRemoteAction(ThinMenuConstants.CREATE_NEW_USER, properties);
             if (objReturn instanceof Integer)
                 iErrorCode = ((Integer)objReturn).intValue();
         } catch (Exception ex) {
             return task.setLastError(ex.getMessage());
         }
         return iErrorCode;
     }
     /**
      * Does the current user have permission to access this resource.
      * @param classResource The resource to check the permission on.
      * @return NORMAL_RETURN if access is allowed, ACCESS_DENIED or LOGIN_REQUIRED if they need to change level or READ_ACCESS if they can do read only access.
      */
     public int checkSecurity(String strClassResource)
     {
         int iLevel = Constants.LOGIN_USER;
         try {
             iLevel = Integer.parseInt(this.getProperty(Params.SECURITY_LEVEL)); // This is my current login level
         } catch (NumberFormatException ex) {
         }
         String strSecurityMap = this.getProperty(Params.SECURITY_MAP);
         if (strSecurityMap == null)
         {
             this.setProperty(Params.SECURITY_MAP, Constants.BLANK); // This flag say I'm already signed on
             // Note, even if the username is null, I need to log in.
             String strUser = this.getProperty(Params.USER_ID);
             if (strUser == null)
                 strUser = this.getProperty(Params.USER_NAME);
             if (this.login(null, strUser, this.getProperty(Params.PASSWORD), null) != Constants.NORMAL_RETURN)       // Get the security access information.
                 if (this.login(null, strUser, null, null) != Constants.NORMAL_RETURN)       // Get the security access information.
                     this.login(null, null, null, null);  // Last chance = anonymous login
             strSecurityMap = this.getProperty(Params.SECURITY_MAP);
         }
         
         int iAccessAllowed = this.checkSecurity(strClassResource, strSecurityMap, Constants.WRITE_ACCESS, iLevel);
         
         if ((iAccessAllowed != Constants.NORMAL_RETURN) && (iAccessAllowed != Constants.CREATE_USER_REQUIRED))
         {
             if (("1"/*DBConstants.ANON_USER_ID*/.equals(this.getProperty(Params.USER_ID)))
                     || (this.getProperty(Params.USER_ID) == null))
             {
                 if (this.checkSecurity(strClassResource, strSecurityMap, Constants.WRITE_ACCESS, Constants.LOGIN_USER) == Constants.NORMAL_RETURN)
                     iAccessAllowed = Constants.LOGIN_REQUIRED;
                 else
                     iLevel = Constants.LOGIN_USER;  // See if authentication would get them what they need.
             }
             if (iLevel == Constants.LOGIN_USER)
             {
                 if (this.checkSecurity(strClassResource, strSecurityMap, Constants.WRITE_ACCESS, Constants.LOGIN_AUTHENTICATED) == Constants.NORMAL_RETURN)
                     iAccessAllowed = Constants.AUTHENTICATION_REQUIRED;
             }
             if ((iAccessAllowed == Constants.ACCESS_DENIED) ||
             		((iLevel == Constants.LOGIN_USER) && (iAccessAllowed == Constants.LOGIN_REQUIRED) || (iAccessAllowed == Constants.AUTHENTICATION_REQUIRED)))
             {   // Can they get read access with this security?
                 if (this.checkSecurity(strClassResource, strSecurityMap, Constants.READ_ACCESS, iLevel) == Constants.NORMAL_RETURN)
                     iAccessAllowed = Constants.READ_ACCESS;     // At this level, they are allowed read access
                 else if (iLevel == Constants.LOGIN_AUTHENTICATED)   // If they can't access at authentication level, try login level.
                     iAccessAllowed = this.checkSecurity(strClassResource, strSecurityMap, Constants.WRITE_ACCESS, Constants.LOGIN_USER);
             }
         }
         return iAccessAllowed;
     }
     /**
      * Get the error text for this security error.
      */
     public String getSecurityErrorText(int iErrorCode)
     {
         String key = "Error " + Integer.toString(iErrorCode);
         if (iErrorCode == Constants.ACCESS_DENIED)
             key = "Access Denied";
         if (iErrorCode == Constants.LOGIN_REQUIRED)
             key = "Login required";
         if (iErrorCode == Constants.AUTHENTICATION_REQUIRED)
             key = "Authentication required";
         if (iErrorCode == Constants.CREATE_USER_REQUIRED)
             key = "Create user required";
         key = this.getResources(ThinResourceConstants.ERROR_RESOURCE, true).getString(key);
         return key;
     }
     /**
      * Does the current user have permission to access this resource.
      * @param classResource The resource to check the permission on.
      * @return NORMAL_RETURN if access is allowed, ACCESS_DENIED or LOGIN_REQUIRED otherwise.
      */
     private int checkSecurity(String strClassResource, String strSecurityMap, int iAccessType, int iLevel)
     {
         int iAccessAllowed = Constants.ACCESS_DENIED;
 
         if (strSecurityMap == null)
             return iAccessAllowed;  // Denied
         if (LOGIN_REQUIRED.equalsIgnoreCase(strSecurityMap))
             return Constants.LOGIN_REQUIRED;  // Denied
         if (CREATE_USER_REQUIRED.equalsIgnoreCase(strSecurityMap))
             return Constants.CREATE_USER_REQUIRED;  // Denied
     	int startThin = strClassResource.indexOf(Constants.THIN_SUBPACKAGE, 0);
     	if (startThin != -1)	// Remove the ".thin" reference
     		strClassResource = strClassResource.substring(0, startThin) + strClassResource.substring(startThin + Constants.THIN_SUBPACKAGE.length());
         
         if (this.classMatch(strClassResource, BASE_CLASS))
             return Constants.NORMAL_RETURN;   // Allow access to all base classes
         StringTokenizer tokenizer = new StringTokenizer(strSecurityMap, TOKENS);
         try {
             while (tokenizer.hasMoreTokens())
             {
                 String strClass = tokenizer.nextToken();
                 String strAccess = tokenizer.nextToken();
                 int iClassAccessType = Constants.READ_ACCESS;
                 iClassAccessType = Integer.parseInt(strAccess);
                 String strLevel = tokenizer.nextToken();
                 int iClassLevel = Constants.LOGIN_USER;
                 iClassLevel = Integer.parseInt(strLevel);
                 
                 if (this.classMatch(strClassResource, strClass))
                     if (iAccessType <= iClassAccessType)
                         if (iLevel >= iClassLevel)
                 {
                     iAccessAllowed = Constants.NORMAL_RETURN;
                 }
             }
         } catch (NoSuchElementException ex) {
             // Done
         } catch (NumberFormatException ex) {
             ex.printStackTrace();
         }
         
         if (("1"/*DBConstants.ANON_USER_ID*/.equals(this.getProperty(Params.USER_ID)))
                 || (this.getProperty(Params.USER_ID) == null))
             if (iAccessAllowed == Constants.ACCESS_DENIED)
                 iAccessAllowed = Constants.LOGIN_REQUIRED;  // For anonymous access, logging in will always get you more
         
         return iAccessAllowed;
     }
     public static final String CREATE_USER_REQUIRED = "CREATE_USER";
     public static final String LOGIN_REQUIRED = "LOGIN_REQUIRED";
     public static final String TOKENS = "\t\n";
     /**
      * Is this target class in the template class pattern?
      * @param targetClass
      * @param templateClass
      * @return true If it matches.
      */
     public boolean classMatch(String targetClass, String templateClass)
     {
 //x        if (targetClass.startsWith(Constants.ROOT_PACKAGE)) // Always
 //x            targetClass = targetClass.substring(Constants.ROOT_PACKAGE.length() - 1);
 //x        if (templateClass.startsWith(Constants.ROOT_PACKAGE)) // Never
 //x            templateClass = templateClass.substring(Constants.ROOT_PACKAGE.length() - 1);
 //x        if (targetClass.startsWith(Constants.THIN_SUBPACKAGE))
 //x            if (!templateClass.startsWith(THIN_CLASS))
 //x                targetClass = targetClass.substring(THIN_CLASS.length());   // Include thin classes
         if (targetClass.startsWith(BASE_CLASS))
             return true;    // Allow access to all base classes
         if (templateClass.endsWith("*"))
         {
             if (templateClass.startsWith("*"))
             	return targetClass.indexOf(templateClass.substring(1, templateClass.length() - 1)) != -1;
             return targetClass.startsWith(templateClass.substring(0, templateClass.length() - 1));
         }
         else
         {   // Exact match
             return templateClass.equalsIgnoreCase(targetClass);
         }
     }
     public static final String BASE_CLASS = Constants.ROOT_PACKAGE + "base.";
     /**
      * Read the user info record for the current user.
      */
 //x    public boolean readUserInfo()
 //x    {
 //x        return false;   // Override this for functionality
 //x    }
     /**
      * Retrieve/Create a user properties record with this lookup key.
      * This is implemented in the thick model, here it is just a shell.
      * @param strPropertyCode The key I'm looking up.
      * @return The UserProperties for this registration key.
      */
     public PropertyOwner retrieveUserProperties(String strRegistrationKey)
     {
         return null;        // Override this for functionality
     }
     /**
      * Set the (optional) muffin manager.
      * @param muffinManager The muffin manager.
      */
     public void setMuffinManager(MuffinManager muffinManager)
     {
         m_muffinManager = muffinManager;
     }
     /**
      * Get the muffin manager.
      * @return The muffin manager.
      */
     public MuffinManager getMuffinManager()
     {
         return m_muffinManager;
     }
     /**
      * Register this unique application so there will be only one running.
      * @param strQueueName The (optional) Queue that this app services.
      * @param strQueueType The (optional) QueueType for this queue.
      * @return NORMAL_RETURN If I registered the app okay, ERROR_RETURN if not (The app is already running)
      */
     public int registerUniqueApplication(String strQueueName, String strQueueType)
     {
         // HACK - This works if you are careful about starting the server... DONT TRUST THIS CODE!
         // todo(don) - THIS DOES NOT WORK CORRECTLY (need to register this app with jini or write an entry in a table or use the lock server or something)
         Map<String,Object> properties = new HashMap<String,Object>();
         properties.put("removeListener", Boolean.TRUE);
         BaseMessage message = new MapMessage(new BaseMessageHeader(strQueueName, strQueueType, this, properties), properties);
         RemoteMessageManager.getMessageManager(this).sendMessage(message);  // Make sure there is no listener that will start this server up.
         return Constants.NORMAL_RETURN;
     }
     /**
      * Get this Message Queue (or create one if this name doesn't exist).
      */
     public BaseMessageManager getMessageManager()
     {
         return this.getMessageManager(true);
     }
     /**
      * Get this Message Queue (or create one if this name doesn't exist).
      * @param bCreateIfNone
      * NOTE: This is overridden for anything but thin.
      */
     public BaseMessageManager getMessageManager(boolean bCreateIfNone)
     {
         return RemoteMessageManager.getMessageManager(this, bCreateIfNone);
     }
     /**
      * Add this user's params to the URL, so when I submit this to the server it can authenticate me.
      * @param strURL
      * @return
      */
     public String addUserParamsToURL(String strURL)
     {
         strURL = Util.addURLParam(strURL, Constants.SUB_SYSTEM_LN_SUFFIX, this.getProperty(Constants.SUB_SYSTEM_LN_SUFFIX), false);
         strURL = Util.addURLParam(strURL, Params.USER_ID, this.getProperty(Params.USER_ID));
         if (this.getProperty(Params.AUTH_TOKEN) != null)
             strURL = Util.addURLParam(strURL, Params.AUTH_TOKEN, this.getProperty(Params.AUTH_TOKEN));
     	return strURL;
     }
     /**
      * The top-level JApplet.
      * @return
      */
     public static BaseAppletReference getRootApplet()
     {
     	return gbaseApplet;
     }
     /**
      * The top-level JApplet.
      * @param
      */
     public static void setRootApplet(BaseAppletReference baseApplet)
     {
     	gbaseApplet = baseApplet;	// The one and only
     }
 }
