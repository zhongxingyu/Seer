 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.util;
 
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.jbundle.base.db.BaseDatabase;
 import org.jbundle.base.db.BaseTable;
 import org.jbundle.base.db.Record;
 import org.jbundle.base.db.util.SyncTable;
 import org.jbundle.base.field.PropertiesField;
 import org.jbundle.base.field.ReferenceField;
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.model.DBParams;
 import org.jbundle.base.model.RecordOwner;
 import org.jbundle.base.model.ResourceConstants;
 import org.jbundle.base.model.Utility;
 import org.jbundle.base.thread.BaseProcess;
 import org.jbundle.model.DBException;
 import org.jbundle.model.PropertyOwner;
 import org.jbundle.model.Task;
 import org.jbundle.model.main.db.MenusModel;
 import org.jbundle.model.main.user.db.UserControlModel;
 import org.jbundle.model.main.user.db.UserGroupModel;
 import org.jbundle.model.main.user.db.UserInfoModel;
 import org.jbundle.model.main.user.db.UserLogModel;
 import org.jbundle.model.main.user.db.UserLogTypeModel;
 import org.jbundle.model.main.user.db.UserRegistrationModel;
 import org.jbundle.model.message.MessageManager;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.db.Converter;
 import org.jbundle.thin.base.db.Params;
 import org.jbundle.thin.base.remote.RemoteTask;
 import org.jbundle.thin.base.thread.AutoTask;
 import org.jbundle.thin.base.util.base64.Base64;
 import org.jbundle.thin.main.db.Menus;
 import org.jbundle.thin.main.user.db.UserInfo;
 import org.jbundle.util.osgi.finder.ClassServiceUtility;
 
 
 /**
  * A MainApplication encompasses all of a single user's apps.
  * For example, a standalone app, an applet, an ongoing or stateless HTML MainApplication,
  * a user's server MainApplication, or a user's EJB app server MainApplication.
  */
 public class MainApplication extends BaseApplication
 {
     /**
      * All system files open for this user.
      */
     protected RecordOwner m_systemRecordOwner = null;
 
     private Map<String,Map<String,Object>> mapDomainPropertyCache = null;
 
     /**
      * Default constructor.
      */
     public MainApplication()
     {
         super();
     }
     /**
      * Constructor.
      * @param env The Environment.
      * @param strURL The application parameters as a URL.
      * @param args The application parameters as an initial arg list.
      * @param applet The application parameters coming from an applet.
      */
     public MainApplication(Object env, Map<String,Object> properties, Object applet)
     {
         this();
         this.init(env, properties, applet);
     }
     /**
      * Initializes the MainApplication.
      * Usually you pass the object that wants to use this sesssion.
      * For example, the applet or MainApplication.
      * @param env The Environment.
      * @param strURL The application parameters as a URL.
      * @param args The application parameters as an initial arg list.
      * @param applet The application parameters coming from an applet.
      */
     public void init(Object env, Map<String,Object> properties, Object applet)
     {
         super.init(env, properties, applet);
 
         Task task = new AutoTask(this, null, null);	// This is the base task for this application
         m_systemRecordOwner = (RecordOwner)ClassServiceUtility.getClassService().makeObjectFromClassName(ResourceConstants.BASE_PROCESS_CLASS);
         m_systemRecordOwner.init(task, null, null);
         
 //x        this.readUserInfo();
         String strUser = this.getProperty(Params.USER_ID);
         if (strUser == null)
             strUser = this.getProperty(Params.USER_NAME);
         if (this.login(task, strUser, this.getProperty(Params.PASSWORD), this.getProperty(Params.DOMAIN)) != DBConstants.NORMAL_RETURN) // Note, even if the username is null, I need to log in.
             this.login(task, null, null, this.getProperty(Params.DOMAIN)); // If bad login, do anonymous login
     }
     /**
      * Release the user's preferences.
      */
     public void free()
     {
         Record recUserRegistration = (Record)m_systemRecordOwner.getRecord(UserRegistrationModel.USER_REGISTRATION_FILE);
         if (recUserRegistration != null)
         {
             for (UserProperties regKey : m_htRegistration.values())
             {
                 regKey.free();  // Remove yourself                
             }
             recUserRegistration.free();
             recUserRegistration = null;
         }
 
         Record recUserInfo = (Record)this.getUserInfo();
         if (recUserInfo != null)
         {
             if (recUserInfo.isModified(false))
             {
                 try   {
                     if (recUserInfo.getEditMode() == Constants.EDIT_ADD)
                         recUserInfo.add();
                     else
                         recUserInfo.set();
                 } catch (DBException ex)    {
                     ex.printStackTrace();
                 }
             }
             recUserInfo.free();
             recUserInfo = null;
         }
 
         super.free();
     }
     /**
      * Get the system record owner.
      * This is used to keep from indefinite recursion.
      */
     public PropertyOwner getSystemRecordOwner()
     {
         return m_systemRecordOwner;
     }
     /**
      * Get the domain properties for this domain.
      * Note: This method caches all calls to speed up access
      * @param strDomain
      * @return The domain properties.
      */
     public Map<String,Object> getDomainProperties(String strDomain)
     {
         if (m_systemRecordOwner != null)
         	if (strDomain != null)
         {   // Always
         	if (mapDomainPropertyCache != null)
         		if (mapDomainPropertyCache.get(strDomain) != null)
         			return mapDomainPropertyCache.get(strDomain);
             if (mapDomainPropertyCache == null)
                 mapDomainPropertyCache = new HashMap<String,Map<String,Object>>();
 	        Record recMenus = (Record)m_systemRecordOwner.getRecord(MenusModel.MENUS_FILE);
 	        if (recMenus == null)
 	            if (this.getProperty(DBConstants.SYSTEM_NAME) == null)
 	                if (this.getProperty(DBConstants.DEFAULT_SYSTEM_NAME) == null)
 	                    if (m_systemRecordOwner != null)
 	                            if (m_systemRecordOwner.getProperty(DBConstants.SYSTEM_NAME) == null)    // Note: This is where the user properties are.
 	                                m_systemRecordOwner.setProperty(DBConstants.SYSTEM_NAME, this.getDefaultSystemName());  // Very first time, need to see if there is a system name default
 	        
 	        Map<String,Object> mapDomainProperties = null;
             if (recMenus == null)
             {
                 recMenus = Record.makeRecordFromClassName(MenusModel.THICK_CLASS, m_systemRecordOwner);
                 BaseTable table = recMenus.getTable();
                table = new SyncTable(table.getDatabase(), recMenus);	// Fix concurrency issues
                 recMenus.setTable(table);     // This is necessary to link-up ResourceTable
             }
             recMenus.setKeyArea(MenusModel.CODE_KEY);
 
             String strSubDomain = strDomain;
             while (strSubDomain.length() > 0)
             {
                 mapDomainProperties = this.addMenuProperties(strSubDomain, recMenus, mapDomainProperties);
                 if (strSubDomain.indexOf('.') == strSubDomain.lastIndexOf('.'))
                     break;  // xyz.com = stop looking
                 strSubDomain = strSubDomain.substring(strSubDomain.indexOf('.') + 1);  // Remove the next top level domain (ie., www)
             }
 
             if (mapDomainProperties != null)
                 if ((mapDomainProperties.size() > 0) || (mapDomainPropertyCache.get(strDomain) == null))
                     mapDomainPropertyCache.put(strDomain, mapDomainProperties); // Add this domain to the cache
 	        return mapDomainProperties;
         }
         return null;
     }
     /**
      * Read the default system name.
      * @return
      */
     public String getDefaultSystemName()
     {   // No system name set, check the base menus for domain or default properties
         Environment env = this.getEnvironment();
         Map<String, Object> properties = new HashMap<String, Object>();
         properties.put(DBConstants.SYSTEM_NAME, "base");
         BaseApplication app = new MainApplication(env, properties, null);
         try {
             Task task = new AutoTask(app, null, properties);
             RecordOwner recordOwner = new BaseProcess(task, null, properties);
             Record menus = Record.makeRecordFromClassName(MenusModel.THICK_CLASS, recordOwner);
             menus.getField(Menus.CODE).setString(ResourceConstants.DEFAULT_RESOURCE);
             menus.setKeyArea(MenusModel.CODE_KEY);
             if (menus.seek(null))
                 return ((PropertiesField)menus.getField(Menus.PARAMS)).getProperty(DBConstants.SYSTEM_NAME);
         } catch (DBException e) {
             e.printStackTrace();
         } finally {
             env.removeApplication(app);
             app.setEnvironment(null);
             app.free();
         }
         return Utility.DEFAULT_SYSTEM_SUFFIX;
     }
     /**
      * Add the properies from this menu item.
      * @param strSubDomain The menu item
      * @param recMenus The menu record
      * @param mapDomainProperties The properties to add to.
      * @return The new properties (actually mapDomainProperties updated)
      */
     public Map<String,Object> addMenuProperties(String strSubDomain, Record recMenus, Map<String,Object> mapDomainProperties)
     {
         try {
             recMenus.getField(Menus.CODE).setString(strSubDomain);
             if (recMenus.seek("="))
             {
                 Map<String,Object> properties = ((PropertiesField)recMenus.getField(Menus.PARAMS)).getProperties();
                 if (properties == null)
                     properties = new HashMap<String,Object>();
                 if (properties.get(DBParams.HOME) == null)
                     properties.put(DBParams.HOME, strSubDomain);
                 Map<String,Object> oldProperties = m_systemRecordOwner.getProperties();
                 m_systemRecordOwner.setProperties(properties);
                 // All I want are the db (global) properties.
                 properties = BaseDatabase.addDBProperties(null, m_systemRecordOwner, null);
                 m_systemRecordOwner.setProperties(oldProperties);
                 if (mapDomainProperties == null)
                     mapDomainProperties = properties;
                 else
                 {
                     properties.putAll(mapDomainProperties);
                     mapDomainProperties = properties;
                 }
             }
         } catch (DBException ex)    {
             ex.printStackTrace();
         }
         return mapDomainProperties;
     }
     /**
      * Change the current user to this user and (optionally) validate password.
      * @param strPassword
      * @param strUser
      * @return normal_return if successful
      */
     public int login(Task task, String strUserName, String strPassword, String strDomain)
     {
         boolean bCreateServer = false;
         if ((this.getProperty(DBParams.REMOTE_HOST) != null) && (this.getProperty(DBParams.REMOTE_HOST).length() > 0))
             bCreateServer = true;
         org.jbundle.thin.base.remote.RemoteTask remoteTask = (org.jbundle.thin.base.remote.RemoteTask)this.getRemoteTask(null, strUserName, bCreateServer);
         if (remoteTask != null)
             return super.login(task, strUserName, strPassword, strDomain); // Remote client - have server log me in
         else
         {   // If I'm not remote, I need to read the user info.
             Map<String,Object> mapDomainProperties = this.setDomainProperties(task, strDomain);
             this.setProperty(Params.USER_ID, null);
             this.setProperty(Params.USER_NAME, null);
             if (Utility.isNumeric(strUserName))
                 this.setProperty(Params.USER_ID, strUserName);
             else
                 this.setProperty(Params.USER_NAME, strUserName);
             if ((this.getProperty(Params.USER_ID) == null) || (this.getProperty(Params.USER_ID).length() == 0))
                 if ((this.getProperty(Params.USER_NAME) == null) || (this.getProperty(Params.USER_NAME).length() == 0))
             {   // If user is not specified (either for no user, or logout), use the anonymous user
                 Record recUserControl = null;
                 if (m_systemRecordOwner != null)
                 {   // Always
                     recUserControl = (Record)m_systemRecordOwner.getRecord(UserControlModel.USER_CONTROL_FILE);
                     if (recUserControl == null)
                         recUserControl = Record.makeRecordFromClassName(UserControlModel.THICK_CLASS, m_systemRecordOwner);
                     String strUserID = recUserControl.getField(UserControlModel.ANON_USER_INFO_ID).getString();
                     if ((strUserID == null) || (strUserID.length() == 0))
                         strUserID = DBConstants.ANON_USER_ID;
                     this.setProperty(Params.USER_ID, strUserID);
                 }
             }
             if (this.readUserInfo(false, true) == true)
             {
                 UserInfoModel recUserInfo = this.getUserInfo();
                 if ((strPassword != null) && (strPassword.length() > 0))
                 {
                     while (strPassword.endsWith("%3D")) {
                         strPassword = strPassword.substring(0, strPassword.length() - 3) + "="; // Often converted when passing URLs
                     }
                     if (!strPassword.equals(recUserInfo.getField(UserInfoModel.PASSWORD).toString()))
                     {
                         return task.setLastError(this.getString("User name and password do not match"));
                     }
                     // Create and save the remote authorization token.
                     String strToken = Long.toString((long)(Math.random() * Long.MAX_VALUE));
                     strToken = Base64.encode(strToken);
                     this.setProperty(DBParams.AUTH_TOKEN, strToken);
                     // todo(don) HACK - For now, authentication is the SHA password! See ServletTask/777
                     this.setProperty(DBParams.AUTH_TOKEN, strPassword);    // NO NO NO                    
                 }
 
                 Record recUserGroup = ((ReferenceField)recUserInfo.getField(UserInfoModel.USER_GROUP_ID)).getReference();
                 String strSecurityMap = null;
                 Map<String,Object> mapUserProperties = ((PropertiesField)recUserInfo.getField(UserInfoModel.PROPERTIES)).getProperties();
                 Map<String,Object> mapGroupProperties = null;
                 if (recUserGroup != null)
                 {
                     strSecurityMap = recUserGroup.getField(UserGroupModel.ACCESS_MAP).toString();
                     mapGroupProperties = ((PropertiesField)recUserGroup.getField(UserGroupModel.PROPERTIES)).getProperties();
                 }
                 if (strSecurityMap == null)
                     strSecurityMap = DBConstants.BLANK;     // The signals the the user is signed on
                 this.setProperty(Params.SECURITY_MAP, strSecurityMap);
                 this.setProperty(Params.SECURITY_LEVEL, ((strPassword == null) || (strPassword.length() == 0)) ? Integer.toString(Constants.LOGIN_USER) : Integer.toString(Constants.LOGIN_AUTHENTICATED));
                 if ((DBConstants.ANON_USER_ID.equals(recUserInfo.getField(UserInfoModel.ID).toString()))
                         || (recUserInfo == null))
                     this.setProperty(Params.SECURITY_LEVEL, Integer.toString(Constants.LOGIN_USER));   // Special case - If user is anonymous, level is always anonymous
                 Map<String,Object> properties = new Hashtable<String,Object>();
                 if (mapDomainProperties != null)
                     properties.putAll(mapDomainProperties);   // Merge the properties
                 if (mapGroupProperties != null)
                     properties.putAll(mapGroupProperties);   // Merge the properties
                 if (mapUserProperties != null)
                     properties.putAll(mapUserProperties);   // Merge the properties
                 if (recUserInfo != null)
                 {
                     this.setProperty(DBParams.USER_ID, recUserInfo.getField(UserInfoModel.ID).toString());
                     if (!recUserInfo.getField(UserInfoModel.USER_NAME).isNull())
                         this.setProperty(DBParams.USER_NAME, recUserInfo.getField(UserInfoModel.USER_NAME).toString());
                     if (properties != null)
                     {
                         properties.put(DBParams.USER_ID, recUserInfo.getField(UserInfo.ID).toString());
                         if (!recUserInfo.getField(UserInfoModel.USER_NAME).isNull())
                             properties.put(DBParams.USER_NAME, recUserInfo.getField(UserInfoModel.USER_NAME).toString());
                         if (this.getProperty(DBParams.AUTH_TOKEN) != null)
                             properties.put(DBParams.AUTH_TOKEN, this.getProperty(DBParams.AUTH_TOKEN));
                     }
                 }
                 m_systemRecordOwner.setProperties(properties);  // These are the user properties
 
                 UserLogModel recUserLog = null;
                 if (m_systemRecordOwner != null)
                 {   // Always
                     recUserLog = (UserLogModel)m_systemRecordOwner.getRecord(UserLogModel.USER_LOG_FILE);
                     if (recUserLog == null)
                         recUserLog = (UserLogModel)Record.makeRecordFromClassName(UserLogModel.THICK_CLASS, m_systemRecordOwner);
                 }
                 if (recUserLog != null)
                     if (this.getProperty(DBParams.USER_ID) != null)
                         if (this.getProperty(DBParams.USER_ID).length() > 0)
                             if (!DBConstants.ANON_USER_ID.equals(this.getProperty(DBParams.USER_ID)))
                 {
                     try {
                         int iUserID = Integer.parseInt(this.getProperty(DBParams.USER_ID));
                         int iUserLogTypeID = UserLogTypeModel.LOGIN;
                         String strMessage = "Login";
                         recUserLog.log(iUserID, iUserLogTypeID, strMessage);
                     } catch (NumberFormatException e) {
                         // Ignore
                     }
                 }
 
                 return Constants.NORMAL_RETURN;
             }
             else if (task != null)
                 return task.setLastError(this.getString("User name and password do not match"));
             else
                 return DBConstants.ERROR_RETURN;
         }
     }
     /**
      * If the domain changes, change the properties to the new domain.
      * @param task
      * @param strDomain
      * @return
      */
     public Map<String,Object> setDomainProperties(Task task, String strDomain)
     {
         Map<String,Object> mapDomainProperties = null;
         if (strDomain != null)
         {   // Special case - Main URL menu (Save properties)
             if (m_systemRecordOwner != null)
             {   // Always
                 mapDomainProperties = this.getDomainProperties(strDomain);
                 if (mapDomainProperties != null)
                 	if (((mapDomainProperties.get(DBConstants.DB_USER_PREFIX) != null) && (!mapDomainProperties.get(DBConstants.DB_USER_PREFIX).equals(m_systemRecordOwner.getProperty(DBConstants.DB_USER_PREFIX))))
                     	|| ((m_systemRecordOwner.getProperty(DBConstants.DB_USER_PREFIX) != null) && (!m_systemRecordOwner.getProperty(DBConstants.DB_USER_PREFIX).equals(mapDomainProperties.get(DBConstants.DB_USER_PREFIX)))))
             	{
             		m_systemRecordOwner.free();
             		m_databaseCollection.free();
                     m_databaseCollection = new DatabaseCollection(this);
                     m_systemRecordOwner = (RecordOwner)ClassServiceUtility.getClassService().makeObjectFromClassName(ResourceConstants.BASE_PROCESS_CLASS);
                     m_systemRecordOwner.init(task, null, null);
             	}
                 m_systemRecordOwner.setProperties(mapDomainProperties);// Note: I know this is overwritten at the end of this method, BUT I DO NEED these properties NOW if Db_prefix was changed
             }
         }
         return mapDomainProperties;
     }
     /**
      * Does the current user have permission to access this resource.
      * @param classResource The resource to check the permission on.
      * @return NORMAL_RETURN if access is allowed, ACCESS_DENIED or LOGIN_REQUIRED otherwise.
      */
     public int checkSecurity(String strClassResource)
     {
         return super.checkSecurity(strClassResource);    // Everything is okay        
     }
     /**
      * Read the user info record for the current user.
      * @param bRefreshOnChange If true, don't use the cached value
      */
     public boolean readUserInfo(boolean bRefreshOnChange, boolean bForceRead)
     {
         String strUserID = this.getProperty(DBParams.USER_ID);
         if (strUserID == null)
             strUserID = this.getProperty(DBParams.USER_NAME);
         if (strUserID == null)
             strUserID = Constants.BLANK;
         Record recUserInfo = (Record)this.getUserInfo();
         if (recUserInfo == null)
             recUserInfo = Record.makeRecordFromClassName(UserInfoModel.THICK_CLASS, m_systemRecordOwner);
         else
         {
             try {
                 if (recUserInfo.getEditMode() == DBConstants.EDIT_IN_PROGRESS)
                     if (recUserInfo.isModified())
                 {
                     int iID = (int)recUserInfo.getCounterField().getValue();
                     recUserInfo.set();  // Update the current record.
                     if (iID != recUserInfo.getCounterField().getValue())
                         this.setProperty(DBParams.USER_ID, recUserInfo.getCounterField().toString());
                 }
             } catch (DBException ex)    {
                 ex.printStackTrace();
             }
         }
         boolean bFound = ((UserInfoModel)recUserInfo).getUserInfo(strUserID, bForceRead);    // This will read using either the userID or the user name
         if (!bFound)
         { // Not found, add new
             int iOpenMode = recUserInfo.getOpenMode();
             if (bRefreshOnChange)
                 recUserInfo.setOpenMode(recUserInfo.getOpenMode() | DBConstants.OPEN_REFRESH_AND_LOCK_ON_CHANGE_STRATEGY);    // Refresh if new, lock if current
             else
                 recUserInfo.setOpenMode(recUserInfo.getOpenMode() & ~DBConstants.OPEN_REFRESH_AND_LOCK_ON_CHANGE_STRATEGY);    // Refresh if new, lock if current
             try   {
                 recUserInfo.addNew(); // new user
                 if ((strUserID != null) && (strUserID.length() > 0))
                     if (!Utility.isNumeric(strUserID))    // Can't have a numeric userid.
                         recUserInfo.getField(UserInfoModel.USER_NAME).setString(strUserID);
             } catch (DBException ex)    {
                 bFound = false;
             } finally {
                 recUserInfo.setOpenMode(iOpenMode);
             }
         }
         return bFound;
     }
     /**
      * Set this property in the user's property area.
      * @return The user id.
      */
     public String getUserID()
     {
         Record recUserInfo = (Record)this.getUserInfo();
         if (recUserInfo != null)
             return Converter.stripNonNumber(recUserInfo.getField(UserInfoModel.ID).toString());
         else
             return super.getUserID();
     }
     /**
      * Set this property in the user's property area.
      * @return The user name.
      */
     public String getUserName()
     {
         String strUserName = null;
         Record recUserInfo = (Record)this.getUserInfo();
         if (recUserInfo != null)
             strUserName = recUserInfo.getField(UserInfoModel.USER_NAME).toString();
         if ((strUserName == null) || (strUserName.length() == 0))
             strUserName = super.getUserName();
         return strUserName;
     }
     /**
      * Set this property in the user's property area.
      * @param strLanguage The language code.
      */
     public void setLanguage(String strLanguage)
     {
         Record recUserInfo = (Record)this.getUserInfo();
         if (recUserInfo != null)
         {
         	boolean flag = recUserInfo.getField(UserInfoModel.PROPERTIES).isModified();
         	boolean[] brgEnabled = recUserInfo.getField(UserInfoModel.PROPERTIES).setEnableListeners(false);
             ((PropertiesField)recUserInfo.getField(UserInfoModel.PROPERTIES)).setProperty(DBParams.LANGUAGE, strLanguage);
             recUserInfo.getField(UserInfoModel.PROPERTIES).setModified(flag);
             recUserInfo.getField(UserInfoModel.PROPERTIES).setEnableListeners(brgEnabled);
         }
         super.setLanguage(strLanguage);
     }
     /**
      * Set this property in the user's property area.
      * @return The language code.
      */
     public String getLanguage(boolean bCheckLocaleAlso)
     {
         Record recUserInfo = (Record)this.getUserInfo();
         String strLanguage = null;
         if (recUserInfo != null)
             strLanguage = ((PropertiesField)recUserInfo.getField(UserInfoModel.PROPERTIES)).getProperty(DBParams.LANGUAGE);
         if ((strLanguage == null) || (strLanguage.length() == 0))
             strLanguage = super.getLanguage(bCheckLocaleAlso);
         return strLanguage;
     }
     /**
      * Set this property in the user's property area.
      * @param strProperty The key to lookup.
      * @return The property for this key.
      */
     public String getProperty(String strProperty)
     {
         String strValue = super.getProperty(strProperty);
         if ((strValue == null) || (strValue.length() == 0))
         {
             if (m_systemRecordOwner != null)
                 if (m_systemRecordOwner.getProperty(strProperty) != null)
                     strValue = m_systemRecordOwner.getProperty(strProperty);    // Note: This is where the user properties are.
         }
 /*        if ((strValue == null) || (strValue.length() == 0))
         {
             if (!DBParams.USER_NAME.equalsIgnoreCase(strProperty))
                 if (!DBParams.USER_ID.equalsIgnoreCase(strProperty))
                     if (!DBParams.HOME.equalsIgnoreCase(strProperty))
             if (this.getEnvironment().getDefaultApplication() != null)
                 if (this.getEnvironment().getDefaultApplication() != this)
                     strValue = this.getEnvironment().getDefaultApplication().getProperty(strProperty);
         }
 */        return strValue;
     }
     /**
      * Set this property.
      * @param strProperty The property key.
      * @param strValue The property value.
      */
     public void setProperty(String strProperty, String strValue)
     {
         if (this.getSystemRecordOwner() != null)
             this.getSystemRecordOwner().setProperty(strProperty, strValue);    // Note: This is where the user properies are.
         super.setProperty(strProperty, strValue);
     }
     /**
      * The collection of active user registration objects for this user/application.
      */
     private static Hashtable<String,UserProperties> m_htRegistration = new Hashtable<String,UserProperties>();
     /**
      * Retrieve/Create a user properties record with this lookup key.
      * User properties are accessed in two ways.
      * <br/>First, if you pass a null, you get the default top-level user properties (such
      * as background color, fonts, etc.).
      * <br/>Second, if you pass a registration key, you get a property database for that
      * specific key (such as screen field default values, specific screen sizes, etc).
      * @param strPropertyCode The key I'm looking up (null for the default user's properties).
      * @return The UserProperties for this registration key.
      */
     public PropertyOwner retrieveUserProperties(String strRegistrationKey)
     {
         if ((strRegistrationKey == null) || (strRegistrationKey.length() == 0))
             return this;        // Use default user properties
         UserProperties regKey = (UserProperties)m_htRegistration.get(strRegistrationKey);
         if (regKey == null)
             regKey = new UserProperties(this, strRegistrationKey);
         regKey.bumpUseCount(+1);
         return regKey;
     }
     /**
      * Get the user registration record.
      * @param userProperties The user properties to add.
      */
     public void addUserProperties(UserProperties userProperties)
     {
         m_htRegistration.put(userProperties.getKey(), userProperties);
     }
     /**
      * Remove this user registration record.
      * @param userProperties The user properties to remove.
      */
     public void removeUserProperties(UserProperties userProperties)
     {
         if (m_htRegistration.get(userProperties.getKey()) != null)
             m_htRegistration.remove(userProperties.getKey());
     }
     /**
      * Get the user registration record.
      * And create it if it doesn't exist yet.
      * @return The user registration record.
      */
     public Record getUserRegistration()
     {
         Record recUserRegistration = (Record)m_systemRecordOwner.getRecord(UserRegistrationModel.USER_REGISTRATION_FILE);
         if (recUserRegistration == null)
             recUserRegistration = Record.makeRecordFromClassName(UserRegistrationModel.THICK_CLASS, m_systemRecordOwner);
         return recUserRegistration;
     }
     /**
      * Get the user information record.
      * @return The userinfo record.
      */
     public UserInfoModel getUserInfo()
     {
         UserInfoModel recUserInfo = null;
         if (m_systemRecordOwner != null)
             recUserInfo = (UserInfoModel)m_systemRecordOwner.getRecord(UserInfoModel.USER_INFO_FILE);
         return recUserInfo;
     }
     /**
      * Connect to the remote server and get the remote server object.
      * @param strServer The (rmi) server.
      * @param The remote application name in jndi index.
      * @return The remote server object.
      */
     public RemoteTask createRemoteTask(String strServer, String strRemoteApp, String strUserID, String strPassword)
     {
         RemoteTask remoteTask = super.createRemoteTask(strServer, strRemoteApp, strUserID, strPassword);
         return remoteTask;
     }
     /**
      * Get this Message Queue (or create one if this name doesn't exist).
      */
     public MessageManager getMessageManager()
     {
         boolean createMessageManager = true;
     	if (DBConstants.FALSE.equalsIgnoreCase(this.getProperty(DBParams.MESSAGE_SERVER)))
     	    createMessageManager = false;
 		return this.getMessageManager(createMessageManager);
     }
 }
