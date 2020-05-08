 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.util;
 
 /**
  * @(#)Environment.java   1.16 95/12/14 Don Corley
  *
  * Copyright © 2012 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  *
  */
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Vector;
 
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.model.DBParams;
 import org.jbundle.base.model.LockManager;
 import org.jbundle.base.model.MessageApp;
 import org.jbundle.base.model.Utility;
 import org.jbundle.model.App;
 import org.jbundle.model.Env;
 import org.jbundle.model.PropertyOwner;
 import org.jbundle.model.main.msg.db.MessageInfoModel;
 import org.jbundle.model.message.MessageManager;
 import org.jbundle.model.util.Util;
 import org.jbundle.thin.base.db.mem.base.PhysicalDatabaseParent;
 import org.jbundle.thin.base.db.model.ThinPhysicalDatabaseParent;
 import org.jbundle.thin.base.remote.RemoteException;
 import org.jbundle.thin.base.remote.RemoteTask;
 import org.jbundle.thin.base.util.Application;
 import org.jbundle.util.osgi.finder.ClassServiceUtility;
 
 
 /**
  * Environment - Collection of databases for this application.
  * For Example, a single app might have a JDBC remote database, and a Vector local database.
  * Instantiate in app/application - Only one per applet/application.<br>
  * Override getDatabase()
  *
  * @version 1.0.0
  * @author    Don Corley
  */
 public class Environment extends Object
     implements Env
 {
     /**
      * The default database type for local files.
      */
     public static final String DEFAULT_LOCAL_DB = DBParams.CLIENT;
     /**
      * The default database type for remote files.
      */
     public static final String DEFAULT_REMOTE_DB = DBParams.CLIENT;
     /**
      * The default database type for local files.
      */
     public static final String DEFAULT_TABLE_DB = DBParams.PROXY;
     /**
      * Global environment - DO NOT USE THIS or the getEnvironment() static call if at all possible.
      */
     private static Environment gEnv = null;
     /**
      * Database properties.
      */
     protected Map<String,Object> m_properties = null;
     /**
      * List of applications for this Environment.
      */
     protected Vector<App> m_vApplication = null;
     /**
      * The default application (usually the startup application).
      */
     protected App m_applicationDefault = null;
     /**
      * Initial startup arguments.
      */
     protected String[] m_args = null;
     /**
      * The (optional) raw-data PDatabaseOwner (see PDatabaseManager).
      */
     protected ThinPhysicalDatabaseParent m_PhysicalDatabaseParent = null;
     /**
      * The client lock manger
      */
     protected LockManager m_lockManager = null;
 
     /**
      * Constructor.
      */
     public Environment()
     {
         super();
     }
     /**
      * Constructor.
      * Constructs a new Environment (The one and only).
      * @param args The initial arguments. local=the Local Database prefix.
      * remote=the Remote Database prefix.
      */
     public Environment(Map<String,Object> properties)
     {
         this();
         this.init(properties);
         if (gEnv == null)
         	gEnv = this;    // The one and only reference to this
     }
     /**
      * Initialize this object.
      * @param args The initial arguments.
      * local=the Local Database prefix.
      * remote=the Remote Database prefix.
      * table=the table db prefix.
      */
     public void init(Map<String,Object> properties)
     {
         if (properties == null)
             m_properties = new Hashtable<String,Object>();
         else
             m_properties = properties;
         m_vApplication = new Vector<App>();
     }
     /**
      * Free this object.
      */
     public void free()
     {
         MessageApp messageApplication  = this.getMessageApplication(null, null);
     	if (messageApplication != null)
         {
             messageApplication.setEnvironment(null);  // So it doesn't try to free me.
             this.removeApplication(messageApplication);
             messageApplication.free();
         }
         
         if (m_vApplication != null)
         {
             for (int i = this.getApplicationCount() - 1; i >= 0;  i--)
             {
                 BaseApplication app = (BaseApplication)this.m_vApplication.get(i);
                 app.setEnvironment(null); // So it doesn't try to free me.
                 app.free();
             }
             m_vApplication = null;
         }
         
         m_applicationDefault = null;    // Will have been freed
         
         if (m_PhysicalDatabaseParent != null)
         {
             m_PhysicalDatabaseParent.free();
             m_PhysicalDatabaseParent = null;
         }
         
         if (gEnv == this)
         	gEnv = null;
         
         ClassServiceUtility.getClassService().shutdownService(this);	// Careful of circular calls
     }
     /**
      * Free this environment if you don't have any more user applications.
      * For server or servlet environments, set FREEIFDONE to false, then the Environment
      * will not be freed until the default application calls free().
      */
     public void freeIfDone()
     {
         boolean bValidAppFound = false;
         for (int i = 0; i < this.getApplicationCount(); i++)
         {
             Application application = (Application)this.m_vApplication.get(i);
             if (!DBConstants.FALSE.equalsIgnoreCase(this.getProperty(DBParams.FREEIFDONE)))
                 if (application == this.getDefaultApplication())
                     continue;
             if (application instanceof MessageApp)
                 continue;
             bValidAppFound = true;
         }
         if (!bValidAppFound)
             this.free();
     }
     /**
      * Get/Create the one and only Environment.
      * Try NOT to call this method, as it requires the only static variable in the system.
      * @param args The initial args, such as local=prefix remote=prefix.
      * @return The system environment.
      */
       public static Environment getEnvironment(Map<String,Object> properties)
     {
         if (gEnv == null)	// TODO(don) Possible concurrency issue
             gEnv = new Environment(properties); // Create the Environment (using defalt database(s))
         //+else
         //+	Utility.getLogger().warning("getEnvironmentCalled");
         return gEnv;
     }
     /**
      * Get the environment.
      * From the database owner interface.
      * @return The Environment.
      */
     public Environment getEnvironment()
     {
         return this;
     }
     /**
      * Set this property.
      * NOTE: also looks in the default application's properties.
      * @param strProperty The property key.
      * @return The property value.
      */
     public void setProperty(String strProperty, String strValue)
     {
         if (strValue != null)
             m_properties.put(strProperty, strValue);
         else
             m_properties.remove(strProperty);
     }
     /**
      * Get this property.
      * NOTE: also looks in the default application's properties.
      * @param strProperty The property key.
      * @return The property value.
      */
     public String getProperty(String strProperty)
     {
         String strValue = null;
         if (m_properties.get(strProperty) != null)
             strValue = m_properties.get(strProperty).toString();
         if ((strValue == null) || (strValue.length() == 0))
             if (this.getDefaultApplication() != null)
                 strValue = this.getDefaultApplication().getProperty(strProperty);
         return strValue;
     }
     /**
      * Set the Environment properties object.
      * @param properties The properties object.
      */
     public void setProperties(Map<String, Object> properties)
     {
         m_properties = properties;
     }
     /**
      * Get the Environment properties object.
      * @return The properties object.
      */
     public Map<String, Object> getProperties()
     {
         return m_properties;
     }
     /**
      * Get the Environment properties object.
      * @param strPropertyCode The key I'm looking for the owner to.
      * @return The owner of this property key.
      */
     public PropertyOwner retrieveUserProperties(String strRegistrationKey)
     {
         if (this.getDefaultApplication() != null)
             if (this.getDefaultApplication() != null)
                 return this.getDefaultApplication().retrieveUserProperties(strRegistrationKey);
         return null;
     }
     /**
      * Get the default application.
      * @return The default application.
      */
     public App getDefaultApplication()
     {
         return m_applicationDefault;
     }
     /**
      * Set the default application.
      * @param application The default application.
      */
     public void setDefaultApplication(App application)
     {
         m_applicationDefault = application;
         if (m_applicationDefault != null)
         {
             if (m_args != null)
                 Util.parseArgs(m_properties, m_args);
             if (this.getProperty(DBParams.LOCAL) == null)
                 m_properties.put(DBParams.LOCAL, DEFAULT_LOCAL_DB);
             if (this.getProperty(DBParams.REMOTE) == null)
                 m_properties.put(DBParams.REMOTE, DEFAULT_REMOTE_DB);
             if (this.getProperty(DBParams.TABLE) == null)
                 m_properties.put(DBParams.TABLE, DEFAULT_TABLE_DB);
         }
     }
     /**
      * Add an Application to this environment.
      * If there is no default application yet, this is set to the default application.
      * @param application The application to add.
      */
     public void addApplication(App application)
     {
 Utility.getLogger().info("addApp: " + application);
         m_vApplication.add(application);
         ((BaseApplication)application).setEnvironment(this);
         if (m_applicationDefault == null)
             this.setDefaultApplication(application);        // Initialization app
     }
     /**
      * Remove this application from the Environment.
      * @param application The application to remove.
      * @return Number of remaining applications still active.
      */
     public int removeApplication(App application)
     {
 Utility.getLogger().info("removeApp: " + application);
         m_vApplication.remove(application);     // Add code here
         if (m_applicationDefault == application)
             m_applicationDefault = null;        // Initialization app
         return m_vApplication.size();
     }
     /**
      * Get the application count.
      * @return Number of remaining application still active.
      */
     public int getApplicationCount()
     {
     	if (m_vApplication == null)
     		return 0;
         return m_vApplication.size();
     }
     /**
      * Get the application count.
      * @return Number of remaining application still active.
      */
     public App getApplication(int i)
     {
         return m_vApplication.get(i);
     }
     /**
      * Get this Message Queue (or create one if this name doesn't exist).
      * @param application The parent application
      * @param bCreateIfNotFound Create the manager if not found?
      * @return The message manager.
      */
     public MessageManager getMessageManager(App application, boolean bCreateIfNotFound)
     {
     	if (application == null)
     		application = this.getDefaultApplication();
         if (this.getMessageApplication(bCreateIfNotFound, application.getProperties()) != null)
             return this.getMessageApplication(bCreateIfNotFound, application.getProperties()).getThickMessageManager();
         return null;
     }
     /**
      * Find the message application.
      * @param strDBPrefix The database prefix.
      * @param strDomain The domain to get the message app for (or default/best guess if null)
      * @return The message manager or null if no match.
      */
     public MessageApp getMessageApplication(String strDBPrefix, String strSubSystem)
     {
         MessageApp messageApplication = null;
 		for (int i = 0; i < this.getApplicationCount(); i++)
 		{
 			App app = this.getApplication(i);
 			if (app instanceof MessageApp)
 			{
 				String strAppDBPrefix = app.getProperty(DBConstants.DB_USER_PREFIX);
 				String strAppSubSystem = app.getProperty(DBConstants.SUB_SYSTEM_LN_SUFFIX);
 				boolean bDBMatch = false;
 				if ((strAppDBPrefix == null) && (strDBPrefix == null))
 						bDBMatch = true;
 				if (strAppDBPrefix != null)
 					if (strAppDBPrefix.equalsIgnoreCase(strDBPrefix))
 						bDBMatch = true;
 				boolean bSubSystemMatch = false;
 				if ((strAppSubSystem == null) && (strSubSystem == null))
 					bSubSystemMatch = true;
 				if (strAppSubSystem != null)
 					if (strAppSubSystem.equalsIgnoreCase(strSubSystem))
 						bSubSystemMatch = true;
 				if ((bDBMatch) && (bSubSystemMatch))
 					messageApplication = (MessageApp)app;
 			}
 		}
 		return messageApplication;
     }
     /**
      * Get this Message Queue (or create one if this name doesn't exist).
      * @param bCreateIfNotFound Create the manager if not found?
      * @param properties Initial properties for the application
      * @return The message manager.
      */
     public MessageApp getMessageApplication(boolean bCreateIfNotFound, Map<String, Object> properties)
     {
     	String strDBPrefix = null;
     	String strSubSystem = null;
     	String strDomain = null;
     	if (properties != null)
     		strDomain = (String)properties.get(DBParams.DOMAIN);
     	Map<String,Object> propDomain = null;
 		if (this.getDefaultApplication() instanceof MainApplication)
 		{
 	    	if (strDomain == null)
 	    		strDomain = this.getDefaultApplication().getProperty(DBParams.DOMAIN);	// Default domain
 			propDomain = ((MainApplication)this.getDefaultApplication()).getDomainProperties(strDomain);
 			if (propDomain != null)
 			{
 				strDBPrefix = (String)propDomain.get(DBConstants.DB_USER_PREFIX);
 				strSubSystem = (String)propDomain.get(DBConstants.SUB_SYSTEM_LN_SUFFIX);
 			}
 		}    			
 		MessageApp messageApplication = this.getMessageApplication(strDBPrefix, strSubSystem);
         if (bCreateIfNotFound)
     		if (messageApplication == null)
         {
     		MessageApp defaultMessageApplication = this.getMessageApplication(null, null);
             synchronized (this)
             {
             	messageApplication = this.getMessageApplication(strDBPrefix, strSubSystem);
         		if (messageApplication != null)
         			return messageApplication;
             	Map<String,Object> propTemp = new HashMap<String,Object>();
     			if (defaultMessageApplication != null)
     				if (defaultMessageApplication.getProperties() != null)
     					propTemp.putAll(defaultMessageApplication.getProperties());
         		if (properties != null)
 					propTemp.putAll(properties);
     			if (propDomain != null)
 					propTemp.putAll(propDomain);
     			properties = propTemp;
     			String className = MessageInfoModel.THICK_APPLICATION;
     			messageApplication = (MessageApp)ClassServiceUtility.getClassService().makeObjectFromClassName(className);
     			messageApplication.init(this, properties, null);
                 //messageApplication = new MessageInfoApplication(this, properties, null);
                 if (this.getDefaultApplication() != null)
                     if (this.getDefaultApplication() != messageApplication)
                         if (!DBConstants.TRUE.equalsIgnoreCase(messageApplication.getProperty(DBParams.JMSSERVER))) // JMSServer has no server
                 {
                     RemoteTask server = (RemoteTask)messageApplication.getRemoteTask(null);
                     RemoteTask appServer = (RemoteTask)this.getDefaultApplication().getRemoteTask(null, null, false);
                     if ((server != null) && (appServer != null))
                     {
                         try {
                             // Tell the remote session who my main session is
                             // so it can know where not to send server record
                             // messages (to eliminate echos in the client).
                             appServer.setRemoteMessageTask(server); // Should have done all the apps in this env!
                         } catch (RemoteException ex)    {
                             ex.printStackTrace();
                         }
                     }
                 }
             }
         }
         return messageApplication;
     }
     /**
      * Get the (optional) raw data database manager.
      * @return The pDatabaseOwner (returns an object, so this package isn't dependent on PDatabaseOwner).
      */
     public ThinPhysicalDatabaseParent getPDatabaseParent(Map<String,Object> properties, boolean bCreateIfNew)
     {
         if (m_PhysicalDatabaseParent == null)
             if (bCreateIfNew)
         {
             Map<String,Object> map = new Hashtable<String,Object>();
             if (properties != null)
                 map.putAll(properties);
             if (map.get(PhysicalDatabaseParent.APP) == null)
                 map.put(PhysicalDatabaseParent.APP, this.getDefaultApplication()); // Access to the server, etc.
             m_PhysicalDatabaseParent = new PhysicalDatabaseParent(map);
         }
         if (properties != null)
         {
             for (String strKey : properties.keySet())
             {
                 m_PhysicalDatabaseParent.setProperty(strKey, properties.get(strKey));
             }
         }
         return m_PhysicalDatabaseParent;
     }
     /**
      * Get the lock manager (or create one if it doesn't exist).
      * @return The lock manager.
      */
     public LockManager getLockManager()
     {
         if (m_lockManager == null)
         {
             m_lockManager = (LockManager)ClassServiceUtility.getClassService().makeObjectFromClassName(LockManager.CLIENT_LOCK_MANAGER_CLASS);
             m_lockManager.init(this);
         }
         return m_lockManager;
     }
     /**
      * Set the initial database properties.
      */
     public Map<String,String> getCachedDatabaseProperties(String strDBName)
     {
         return m_mapDBProperties.get(strDBName);   // Entry already in cache
     }
     /**
      * Set the initial database properties.
      */
     public void cacheDatabaseProperties(String strDBName, Map<String,String> map)
     {
         m_mapDBProperties.put(strDBName, map);   // Cache for next time
     }
     protected Map<String,Map<String,String>> m_mapDBProperties = new HashMap<String,Map<String,String>>();
     public static final Map<String,String> DATABASE_DOESNT_EXIST = new HashMap<String,String>(0);
 }
