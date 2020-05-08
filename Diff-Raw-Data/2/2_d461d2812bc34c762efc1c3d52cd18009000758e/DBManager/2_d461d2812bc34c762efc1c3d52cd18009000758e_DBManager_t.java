 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package twitz.util;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.SwingWorker;
 //import org.apache.log4j.Logger;
 import org.tmatesoft.sqljet.core.SqlJetException;
 import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
 import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
 import org.tmatesoft.sqljet.core.table.ISqlJetTable;
 import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
 import org.tmatesoft.sqljet.core.table.SqlJetDb;
 import twitter4j.User;
 import twitz.TwitzApp;
 import twitz.testing.UserTest;
 
 /**
  *
  * @author mistik1
  */
 public class DBManager {
 
 	//USERS TABLE
 	public static final String USER_TABLE = "users";
 	public static final String USER_NAME = "screenname"; //userid, screenname, fullname, picture_url
 	public static final String USER_ID = "userid";
 	public static final String USER_FULLNAME = "fullname";
 	public static final String USER_PICTURE = "picture_url";
 	public static final String USER_NAME_INDEX = "screenname_index";
 	public static final String USER_ID_INDEX = "userid_index";
 
 	//SESSION TABLE (name)
 	public static final String SESSION_TABLE = "login_session";
 	public static final String SESSION_NAME = "session_name";
 	public static final String SESSION_TWITTER_ID = "twitter_id";
 	public static final String SESSION_TWITTER_PASSWORD = "twitter_password";
 	public static final String SESSION_TWITTER_PICTURE_URL = "twitter_picture";
 	public static final String SESSION_TWITTER_USE_PROXY = "twitter_use_proxy";
 	public static final String SESSION_TWITTER_PROXY_PORT = "twitter_proxy_port";
 	public static final String SESSION_TWITTER_PROXY_HOST = "twitter_proxy_host";
 	public static final String SESSION_TWITTER_PROXY_USER = "twitter_proxy_user";
 	public static final String SESSION_TWITTER_PROXY_PASSWORD = "twitter_proxy_password";
 	public static final String SESSION_MINIMIZE_ON_STARTUP = "minimize_startup";
 	public static final String SESSION_TWITZ_UNDECORATED = "twitz_undecorated";
 	public static final String SESSION_TWITZ_SKIN = "twitz_skin";
 	public static final String SESSION_TWITZ_MINIMODE = "minimode";
 	public static final String SESSION_TWITZ_LAST_HEIGHT = "twitz_last_height";
 	public static final String SESSION_TAB_POSITION = "tab_position";
 	public static final String SESSION_TAB_FRIENDS = "tab_friends";
 	public static final String SESSION_TAB_BLOCKED = "tab_blocked";
 	public static final String SESSION_TAB_FOLLOWING = "tab_following";
 	public static final String SESSION_TAB_FOLLOWERS = "tab_followers";
 	public static final String SESSION_TAB_SEARCH = "tab_search";
 	public static final String SESSION_DEFAULT = "session_default";
 	public static final String SESSION_AUTOLOAD = "session_autoload";
 	public static final String SESSION_DEFAULT_INDEX = "session_default_index";
 	public static final String SESSION_AUTOLOAD_INDEX = "session_autoload_index";
 	public static final String SESSION_INDEX = "session_index";
 
 	//CONFIG_TABLE (name, value, sessionid, cfgdesc, cfgtype)
 	public static final String CONFIG_TABLE = "settings";
 	public static final String CONFIG_NAME = "setting";
 	public static final String CONFIG_ID = "sessionid";
 	public static final String CONFIG_DESC = "cfgdesc";
 	public static final String CONFIG_TYPE = "cfgtype";
 	public static final String CONFIG_INDEX = "config_index";
 	public static final String CONFIG_NAME_INDEX = "config_name_index";
 	public static final String CONFIG_VALUE_INDEX = "config_value_index";
 
 	//CONFIG_DESC_TABLE (configid, description)
 	public static final String DESC_TABLE = "settings_desc";
 	public static final String DESC_ID = "configid";
 	public static final String DESC_VALUE = "description";
 	public static final String DESC_INDEX = "desc_configid_index";
 
 	//CONFIG_TYPE_TABLE (configid, type)
 	public static final String TYPE_TABLE = "settings_type";
 	public static final String TYPE_ID = "configid";
 	public static final String TYPE_VALUE = "type";
 	public static final String TYPE_INDEX = "type_configid_index";
 
 	private static final String DBFILE = "twitz.db";
 
 	private static final File FILE_DIR = TwitzApp.getConfigDirectory();
 	private static File dbFile = new File(FILE_DIR, DBFILE);
 	private boolean firstrun = !dbFile.exists();
 	private final SqlJetDb db = new SqlJetDb(dbFile, true);
 	private static final Logger logger = Logger.getLogger("twitz.util.DBManager");
 	//private static final boolean logdebug = logger.isDebugEnabled();
 	private static DBManager instance;
 	
 	private static Vector<User> users = new Vector<User>();
 
 	/*Prevents Instantiation*/
 	private DBManager()
 	{
 		
 	}
 
 	@SuppressWarnings("static-access")
 	private void configureDb()
 	{
 		logger.log(Level.INFO, "Entering configureDb() firstrun = {0}", firstrun);
 		if(!firstrun)
 			return;
 		try
 		{
 			db.open();
 			db.getOptions().setAutovacuum(true);
 			db.runTransaction(new ISqlJetTransaction(){
 				public Object run(SqlJetDb db)
 				{
 					try
 					{
 						db.getOptions().setUserVersion(1);
 					}
 					catch (SqlJetException ex)
 					{
 						logger.log(Level.SEVERE, "Error Setting transaction options", ex);
 					}
 					return true;
 				}
 			}, SqlJetTransactionMode.EXCLUSIVE);
 			//db.open();
 		}
 		catch (SqlJetException ex)
 		{
 			logger.log(Level.SEVERE, "Error while configuring db engine",ex);
 		}
 		finally
 		{
 			try
 			{
 				//db.commit();
 				db.close();
 			}
 			catch (SqlJetException ex)
 			{
 				Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		}
 		if(firstrun)
 			createSchema();
 	}
 
 	private void createSchema()//{{{
 	{
 		//USER TABLE (userid, screenname, fullname, picture_url)
 		//CONFIG_TABLE (name, value, sessionid, desc, type)
 		//CONFIG_DESC_TABLE (configid, description)
 		//CONFIG_TYPE_TABLE (configid, type)
 //		USER_TABLE
 //		USER_NAME
 //		USER_ID
 //		USER_FULLNAME
 //		USER_PICTURE
 
 		String userTableQuery = "CREATE TABLE "+ USER_TABLE + " (" + USER_ID + " INTEGER NOT NULL, " +
 				USER_NAME + " TEXT NOT NULL PRIMARY KEY, " + USER_FULLNAME + " TEXT NOT NULL, " +
 				USER_PICTURE +" TEXT NOT NULL)";
 		String sessionTableQuery = "CREATE TABLE " + SESSION_TABLE + " (" + SESSION_NAME + " TEXT NOT NULL, "
 				+ SESSION_TWITTER_ID + " TEXT NOT NULL, " + SESSION_TWITTER_PASSWORD + " TEXT NOT NULL, "
 				+ SESSION_TWITTER_PICTURE_URL + " TEXT, " + SESSION_TWITTER_USE_PROXY + " INTEGER NOT NULL DEFAULT 0,  "
 				+ SESSION_TWITTER_PROXY_PORT + " INTEGER DEFAULT 8080, "
 				+ SESSION_TWITTER_PROXY_HOST + " TEXT, " + SESSION_TWITTER_PROXY_USER + " TEXT, "
 				+ SESSION_TWITTER_PROXY_PASSWORD + " TEXT, " + SESSION_MINIMIZE_ON_STARTUP + " INTEGER NOT NULL DEFAULT 0, "
 				+ SESSION_TWITZ_UNDECORATED + " INTEGER NOT NULL DEFAULT 0, " + SESSION_TWITZ_SKIN + " TEXT NOT NULL DEFAULT 'MistAqua', "
 				+ SESSION_TWITZ_MINIMODE + " INTEGER NOT NULL DEFAULT 0, " + SESSION_TWITZ_LAST_HEIGHT + " INTEGER DEFAULT 640, "
 				+ SESSION_TAB_POSITION + " TEXT NOT NULL DEFAULT 'north', " + SESSION_TAB_FRIENDS + " INTEGER NOT NULL DEFAULT 1, "
 				+ SESSION_TAB_BLOCKED + " INTEGER NOT NULL DEFAULT 0, " + SESSION_TAB_FOLLOWING + " INTEGER NOT NULL DEFAULT 0, "
 				+ SESSION_TAB_FOLLOWERS + " INTEGER NOT NULL DEFAULT 0, " + SESSION_TAB_SEARCH + " INTEGER NOT NULL DEFAULT 1, "
 				+ SESSION_DEFAULT + " INTEGER NOT NULL DEFAULT 0, " + SESSION_AUTOLOAD + " INTEGER NOT NULL DEFAULT 0)";
 
 //		String sessionTableQuery = "CREATE TABLE "+ SESSION_TABLE +" ("+ SESSION_NAME +" TEXT NOT NULL, " +
 //				SESSION_DEFAULT + " INTEGER NOT NULL DEFAULT 0, "+ SESSION_AUTOLOAD + " INTEGER NOT NULL DEFAULT 0)";
 		String configTableQuery = "CREATE TABLE "+ CONFIG_TABLE + " ("+ CONFIG_ID + " INTEGER NOT NULL PRIMARY KEY, "
 				+ CONFIG_NAME +" TEXT NOT NULL, " + CONFIG_DESC +" INTEGER, "+ CONFIG_TYPE +" INTEGER)";
 		String configTypeQuery = "CREATE TABLE "+ TYPE_TABLE +" ("+ TYPE_ID +" INTEGER NOT NULL,"+
 				TYPE_VALUE +" TEXT NOT NULL)";
 		String configDescQuery = "CREATE TABLE "+ DESC_TABLE +" ("+ DESC_ID +" INTEGER NOT NULL, "+
 				DESC_VALUE +" TEXT)";
 		String userNameIndex = "CREATE INDEX " + USER_NAME_INDEX + " ON " + USER_TABLE + "(" +  USER_NAME + ")";
 		String userIdIndex = "CREATE INDEX " + USER_ID_INDEX + " ON " + USER_TABLE + "(" +  USER_ID + ")";
 		String configIndex = "CREATE INDEX " + CONFIG_INDEX + " ON " + CONFIG_TABLE + "(" +  CONFIG_ID + ")";
 		String configNameIndex = "CREATE INDEX " + CONFIG_NAME_INDEX + " ON " + CONFIG_TABLE + "(" +  CONFIG_NAME + ","+ CONFIG_ID +")";
 		//String configValueIndex = "CREATE INDEX " + CONFIG_VALUE_INDEX + " ON " + CONFIG_TABLE + "(" +  CONFIG_VALUE + ")";
 		String configDescIndex = "CREATE INDEX " + DESC_INDEX + " ON " + DESC_TABLE + "(" +  DESC_ID + ")";
 		String configTypeIndex = "CREATE INDEX " + TYPE_INDEX + " ON " + TYPE_TABLE + "(" +  TYPE_ID + ")";
 		String sessionIndex = "CREATE INDEX " + SESSION_INDEX + " ON "+ SESSION_TABLE +"("+ SESSION_NAME +")";
 		String sessionDefaultsIndex = "CREATE INDEX " + SESSION_DEFAULT_INDEX + " ON "+ SESSION_TABLE +"("+ SESSION_DEFAULT +")";
 		String sessionAutoIndex = "CREATE INDEX " + SESSION_AUTOLOAD_INDEX + " ON "+ SESSION_TABLE +"("+ SESSION_AUTOLOAD +")";
 
 //		if(logdebug)
 //		{
 			logger.log(Level.INFO, userTableQuery);
 			logger.log(Level.INFO, configTableQuery);
 			logger.log(Level.INFO, configTypeQuery);
 			logger.log(Level.INFO, configDescQuery);
 			logger.log(Level.INFO, sessionTableQuery);
 //		}
 
 		try
 		{
 			db.open();
 			db.beginTransaction(SqlJetTransactionMode.EXCLUSIVE);
 			
 			db.createTable(userTableQuery);
 			db.createIndex(userNameIndex);
 			db.createIndex(userIdIndex);
 			db.createTable(sessionTableQuery);
 			db.createIndex(sessionIndex);
 			db.createIndex(sessionDefaultsIndex);
 			db.createIndex(sessionAutoIndex);
 			db.createTable(configTableQuery);
 			db.createIndex(configIndex);
 			db.createIndex(configNameIndex);
 			//db.createIndex(configValueIndex);
 			db.createTable(configTypeQuery);
 			db.createIndex(configTypeIndex);
 			db.createTable(configDescQuery);
 			db.createIndex(configDescIndex);
 		}
 		catch(SqlJetException e)
 		{
 			logger.log(Level.SEVERE, e.getMessage());
 		}
 		finally
 		{
 			try
 			{
 				db.commit();
 				db.close();
 			}
 			catch (SqlJetException ex)
 			{
 				logger.log(Level.SEVERE, ex.getMessage());
 			}
 		}
 		try
 		{
 			populateDefaultSettingsTable("Default");
 		}
 		catch (Exception ex)
 		{
 			logger.log(Level.SEVERE, ex.getMessage());
 		}
 
 //		String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" + SECOND_NAME_FIELD + " TEXT NOT NULL PRIMARY KEY , " + FIRST_NAME_FIELD + " TEXT NOT NULL, " + DOB_FIELD + " INTEGER NOT NULL)";
 //		String createFirstNameIndexQuery = "CREATE INDEX " + FULL_NAME_INDEX + " ON " + TABLE_NAME + "(" +  FIRST_NAME_FIELD + "," + SECOND_NAME_FIELD + ")";
 //		String createDateIndexQuery = "CREATE INDEX " + DOB_INDEX + " ON " + TABLE_NAME + "(" +  DOB_FIELD + ")";
 	}//}}}
 
 	public boolean populateDefaultSettingsTable(String name) throws SqlJetException, Exception//{{{
 	{
 		boolean rv = true;
 		if(name == null || name.equals(""))
 			throw new IllegalArgumentException("name must NOT be null or blank string");
 		long sid = -1;
 		db.open();
 		if (db.isOpen())
 		{
 			db.beginTransaction(SqlJetTransactionMode.EXCLUSIVE);
 			if (db.isInTransaction())
 			{
 				ISqlJetTable ut = db.getTable(USER_TABLE);
 				ISqlJetTable session = db.getTable(SESSION_TABLE);
 				ISqlJetTable config = db.getTable(CONFIG_TABLE);
 				ISqlJetTable type = db.getTable(TYPE_TABLE);
 				ISqlJetTable desc = db.getTable(DESC_TABLE);
 
 				try
 				{//(name, value, sessionid, desc, type)
 					if (firstrun)
 					{
 						org.jdesktop.application.ResourceMap resource = twitz.TwitzApp.getContext().getResourceMap(twitz.TwitzMainView.class);
 						type.insert(1, "Password");
 						type.insert(2, "File");
 						type.insert(3, "Boolean");
 						type.insert(4, "String");
 						type.insert(5, "Theme");
 						type.insert(6, "Internal");
 
 						desc.insert(1, resource.getString("config_"+SESSION_TWITTER_ID));
 						desc.insert(2, resource.getString("config_"+SESSION_TWITTER_PASSWORD));
 						desc.insert(3, resource.getString("config_"+SESSION_TWITTER_PICTURE_URL));
 						desc.insert(4, resource.getString("config_"+SESSION_TWITTER_USE_PROXY));
 						desc.insert(5, resource.getString("config_"+SESSION_TWITTER_PROXY_PORT));
 						desc.insert(6, resource.getString("config_"+SESSION_TWITTER_PROXY_HOST));
 						desc.insert(7, resource.getString("config_"+SESSION_TWITTER_PROXY_USER));
 						desc.insert(8, resource.getString("config_"+SESSION_TWITTER_PROXY_PASSWORD));
 						desc.insert(9, resource.getString("config_"+SESSION_MINIMIZE_ON_STARTUP));
 						desc.insert(10, resource.getString("config_"+SESSION_TWITZ_UNDECORATED));
 						desc.insert(11, resource.getString("config_"+SESSION_TWITZ_SKIN));
 						desc.insert(12, resource.getString("config_"+SESSION_TAB_FRIENDS));
 						desc.insert(13, resource.getString("config_"+SESSION_TAB_BLOCKED));
 						desc.insert(14, resource.getString("config_"+SESSION_TAB_FOLLOWING));
 						desc.insert(15, resource.getString("config_"+SESSION_TAB_FOLLOWERS));
 						desc.insert(16, resource.getString("config_"+SESSION_TAB_SEARCH));
 						desc.insert(17, resource.getString("config_internal"));
 						
 						config.insert(1, SESSION_TWITTER_ID, 1, 4);
 						config.insert(2, SESSION_TWITTER_PASSWORD, 2, 1);
 						config.insert(3, SESSION_TWITTER_PICTURE_URL, 3, 2);
 						//Proxy settings
 						config.insert(4, SESSION_TWITTER_USE_PROXY, 4, 3);
 						//Port (Must have a value if use_proxy is enalbed)
 						config.insert(5, SESSION_TWITTER_PROXY_PORT, 5, 4);
 						//Host (Must have a value if use_proxy is enalbed)
 						config.insert(6, SESSION_TWITTER_PROXY_HOST,  6, 4);
 						//User (Can be blank)
 						config.insert(7, SESSION_TWITTER_PROXY_USER, 7, 4);
 						//Password (Can be blank)
 						config.insert(8, SESSION_TWITTER_PROXY_PASSWORD, 7, 1);
 						//Minimize settings
 						config.insert(9, SESSION_MINIMIZE_ON_STARTUP, 9, 3);
 						//Decoration settings
 						config.insert(10, SESSION_TWITZ_UNDECORATED, 10, 3);
 						config.insert(11, SESSION_TWITZ_SKIN, 11, 5);
 						//This is a null entry AKA an internally managed property
 						//This keeps the mini/full state of the program between restarts
 						config.insert(12, SESSION_TWITZ_MINIMODE, 17, 6);
 						//Internal Size settings
 						config.insert(13, SESSION_TWITZ_LAST_HEIGHT, 17, 6);
 						//Tab positions
 						config.insert(14, SESSION_TAB_POSITION, 17, 6);
 						//Friends Tab settings
 						config.insert(15, SESSION_TAB_FRIENDS, 12, 3);
 						//Blocked Tab settings
 						config.insert(16, SESSION_TAB_BLOCKED, 13, 6);
 						//Following tab settings
 						config.insert(17, SESSION_TAB_FOLLOWING, 14, 6);
 						//Followers tab settings
 						config.insert(18, SESSION_TAB_FOLLOWERS, 15, 6);
 						//Search tab settings
 						config.insert(19, SESSION_TAB_SEARCH, 16, 3);
 						config.insert(20, SESSION_DEFAULT, 17, 6);
 						config.insert(21, SESSION_AUTOLOAD, 17, 6);
 						//sid = session.insert(name, 1, 1);
 						logger.log(Level.INFO, "Firstrun name: {0}", name);
						session.insert(name, "changeme", "changeme", "", 0, 8080, "", "", "", 0, 0, "MistAqua", 0, 640, "north", 1, 0, 0, 0, 1, 1, 0);
 						firstrun = false;
 					}
 					else
 					{
 						//(name, twitter_id=changeme, twitter_password=changeme, picture_url="", use_proxy=0, proxy_port=8080,
 						// proxy_host="", proxy_user="", proxy_passwd="", minimize_statup=0, undecorated=0, skin=MistAqua, minimode=0, last_height=640,
 						//tab_position=north, tab_friends=1, tab_blocked=0, tab_following=0, tab_followers=0, tab_search=1, session_default=0, session_autoload=0)
 						logger.log(Level.INFO, "Profile name: {0}", name);
 						ISqlJetCursor sc = session.lookup(SESSION_INDEX, name);
 						if(sc.eof())
 						{
 							session.insert(name, "changeme", "changeme", "", 0, 8080, "", "", "", 0, 0, "MistAqua", 0, 640, "north", 1, 0, 0, 0, 1, 0, 0);
 						}
 						else
 						{
 							throw new Exception("A profile by that name already exists"); //TODO needs I18N
 						}
 						//sid = session.insert(name);
 					}
 				}
 				finally
 				{
 					//if(db.isInTransaction())
 						db.commit();
 					//if(db.isOpen())
 						db.close();
 				}
 			}
 			else
 			{
 				rv = false;
 			}
 		}
 		else
 		{
 			rv = false;
 		}
 		return rv;
 	}//}}}
 
 
 	public static synchronized DBManager getInstance()//{{{
 	{
 
 		if(instance == null)
 		{
 			instance = new DBManager();
 			instance.configureDb();
 		}
 		return instance;
 	}//}}}
 
 	public synchronized Properties lookupSettingsForSession(String name) throws SqlJetException
 	{
 		Properties rv = new Properties();
 		db.open();
 		db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
 		try
 		{
 			ISqlJetTable session = db.getTable(SESSION_TABLE);
 			ISqlJetTable config = db.getTable(CONFIG_TABLE);
 			ISqlJetTable type = db.getTable(TYPE_TABLE);
 			ISqlJetTable desc = db.getTable(DESC_TABLE);
 
 			long sid = -1;
 			ISqlJetCursor sc = session.lookup(SESSION_INDEX, name);
 			if(!sc.eof()) //sessions will have unique names
 			{
 				sid = sc.getRowId();
 				String twitter_id = sc.getString(SESSION_TWITTER_ID);
 				ISqlJetCursor cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TWITTER_ID);
 				ISqlJetCursor tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				ISqlJetCursor dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TWITTER_ID, twitter_id);
 				if(!tc.eof())
 					rv.setProperty(SESSION_TWITTER_ID+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TWITTER_ID+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				String twitter_password = sc.getString(SESSION_TWITTER_PASSWORD);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TWITTER_PASSWORD);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TWITTER_PASSWORD, twitter_password);
 				if(!tc.eof())
 					rv.setProperty(SESSION_TWITTER_PASSWORD+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TWITTER_PASSWORD+".cfgdesc", dc.getString(DESC_VALUE));
 
 				String twitter_picture = sc.getString(SESSION_TWITTER_PICTURE_URL);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TWITTER_PICTURE_URL);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TWITTER_PICTURE_URL, twitter_picture == null ? "" : twitter_picture);
 				if(!tc.eof())
 					rv.setProperty(SESSION_TWITTER_PICTURE_URL+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TWITTER_PICTURE_URL+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				boolean twitter_use_proxy = (sc.getInteger(SESSION_TWITTER_USE_PROXY) == 1);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TWITTER_USE_PROXY);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TWITTER_USE_PROXY, twitter_use_proxy+"");
 				if(!tc.eof())
 					rv.setProperty(SESSION_TWITTER_USE_PROXY+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TWITTER_USE_PROXY+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				long twitter_proxy_port = sc.getInteger(SESSION_TWITTER_PROXY_PORT);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TWITTER_PROXY_PORT);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TWITTER_PROXY_PORT, twitter_proxy_port+"");
 				if(!tc.eof())
 					rv.setProperty(SESSION_TWITTER_PROXY_PORT+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TWITTER_PROXY_PORT+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				String twitter_proxy_host = sc.getString(SESSION_TWITTER_PROXY_HOST);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TWITTER_PROXY_HOST);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TWITTER_PROXY_HOST, twitter_proxy_host == null ? "" : twitter_proxy_host);
 				if(!tc.eof())
 					rv.setProperty(SESSION_TWITTER_PROXY_HOST+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TWITTER_PROXY_HOST+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				String twitter_proxy_user = sc.getString(SESSION_TWITTER_PROXY_USER);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TWITTER_PROXY_USER);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TWITTER_PROXY_USER, twitter_proxy_user == null ? "" : twitter_proxy_user);
 				if(!tc.eof())
 					rv.setProperty(SESSION_TWITTER_PROXY_USER+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TWITTER_PROXY_USER+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				String twitter_proxy_password = sc.getString(SESSION_TWITTER_PROXY_PASSWORD);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TWITTER_PROXY_PASSWORD);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TWITTER_PROXY_PASSWORD, twitter_proxy_password == null ? "" : twitter_proxy_password);
 				if(!tc.eof())
 					rv.setProperty(SESSION_TWITTER_PROXY_PASSWORD+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TWITTER_PROXY_PASSWORD+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				boolean minimize_startup = (sc.getInteger(SESSION_MINIMIZE_ON_STARTUP) == 1);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_MINIMIZE_ON_STARTUP);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_MINIMIZE_ON_STARTUP, minimize_startup+"");
 				if(!tc.eof())
 					rv.setProperty(SESSION_MINIMIZE_ON_STARTUP+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_MINIMIZE_ON_STARTUP+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				boolean twitz_undecorated = (sc.getInteger(SESSION_TWITZ_UNDECORATED) == 1);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TWITZ_UNDECORATED);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TWITZ_UNDECORATED, twitz_undecorated+"");
 				if(!tc.eof())
 					rv.setProperty(SESSION_TWITZ_UNDECORATED+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TWITZ_UNDECORATED+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				String twitz_skin = sc.getString(SESSION_TWITZ_SKIN);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TWITZ_SKIN);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TWITZ_SKIN, twitz_skin);
 				if(!tc.eof())
 					rv.setProperty(SESSION_TWITZ_SKIN+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TWITZ_SKIN+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				boolean minimode = (sc.getInteger(SESSION_TWITZ_MINIMODE) == 1);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TWITZ_MINIMODE);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TWITZ_MINIMODE, minimode+"");
 				if(!tc.eof())
 					rv.setProperty(SESSION_TWITZ_MINIMODE+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TWITZ_MINIMODE+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				long twitz_last_height = sc.getInteger(SESSION_TWITZ_LAST_HEIGHT);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TWITZ_LAST_HEIGHT);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TWITZ_LAST_HEIGHT, twitz_last_height+"");
 				if(!tc.eof())
 					rv.setProperty(SESSION_TWITZ_LAST_HEIGHT+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TWITZ_LAST_HEIGHT+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				String tab_position = sc.getString(SESSION_TAB_POSITION);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TAB_POSITION);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TAB_POSITION, tab_position);
 				if(!tc.eof())
 					rv.setProperty(SESSION_TAB_POSITION+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TAB_POSITION+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				boolean tab_friends = (sc.getInteger(SESSION_TAB_FRIENDS) == 1);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TAB_FRIENDS);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TAB_FRIENDS, tab_friends+"");
 				if(!tc.eof())
 					rv.setProperty(SESSION_TAB_FRIENDS+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TAB_FRIENDS+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				boolean tab_blocked = (sc.getInteger(SESSION_TAB_BLOCKED) == 1);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TAB_BLOCKED);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TAB_BLOCKED, tab_blocked+"");
 				if(!tc.eof())
 					rv.setProperty(SESSION_TAB_BLOCKED+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TAB_BLOCKED+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				boolean tab_following = (sc.getInteger(SESSION_TAB_FOLLOWING) == 1);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TAB_FOLLOWING);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TAB_FOLLOWING, tab_following+"");
 				if(!tc.eof())
 					rv.setProperty(SESSION_TAB_FOLLOWING+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TAB_FOLLOWING+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				boolean tab_followers = (sc.getInteger(SESSION_TAB_FOLLOWERS) == 1);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TAB_FOLLOWERS);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TAB_FOLLOWERS, tab_followers+"");
 				if(!tc.eof())
 					rv.setProperty(SESSION_TAB_FOLLOWERS+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TAB_FOLLOWERS+".cfgdesc", dc.getString(DESC_VALUE));
 				
 				boolean tab_search = (sc.getInteger(SESSION_TAB_SEARCH) == 1);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_TAB_SEARCH);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_TAB_SEARCH, tab_search+"");
 				if(!tc.eof())
 					rv.setProperty(SESSION_TAB_SEARCH+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_TAB_SEARCH+".cfgdesc", dc.getString(DESC_VALUE));
 
 				boolean session_default = (sc.getInteger(SESSION_DEFAULT) == 1);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_DEFAULT);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_DEFAULT, session_default+"");
 				if(!tc.eof())
 					rv.setProperty(SESSION_DEFAULT+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_DEFAULT+".cfgdesc", dc.getString(DESC_VALUE));
 
 				boolean session_autoload = (sc.getInteger(SESSION_AUTOLOAD) == 1);
 				cc = config.lookup(CONFIG_NAME_INDEX, SESSION_AUTOLOAD);
 				tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 				dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 				rv.setProperty(SESSION_AUTOLOAD, session_autoload+"");
 				if(!tc.eof())
 					rv.setProperty(SESSION_AUTOLOAD+".cfgtype", tc.getString(TYPE_VALUE));
 				if(!dc.eof())
 					rv.setProperty(SESSION_AUTOLOAD+".cfgdesc", dc.getString(DESC_VALUE));
 
 //				ISqlJetCursor cc = config.lookup(CONFIG_INDEX, sid);
 //				if(!cc.eof())
 //				{
 //					do
 //					{
 //						String key = cc.getString(CONFIG_NAME);
 //						String val = cc.getString(CONFIG_VALUE);
 //						ISqlJetCursor tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 //						ISqlJetCursor dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 //						rv.setProperty(key, val);
 //						if(!tc.eof())
 //							rv.setProperty(key+".cfgtype", tc.getString(TYPE_VALUE));
 //						if(!dc.eof())
 //							rv.setProperty(key+".cfgdesc", dc.getString(DESC_VALUE));
 //					}
 //					while(cc.next());
 //				}
 			}
 		}
 		finally
 		{
 			db.commit();
 			//if(!db.isInTransaction())
 				db.close();
 		}
 		//System.out.println("loading properties "+rv);
 		return rv;
 	}
 
 	public synchronized Vector<Map<String, Object>> lookupSessions() throws Exception
 	{
 		Vector<Map<String, Object>> rv = new Vector<Map<String, Object>>();
 		Map<String, Object> map = null;
 		db.open();
 		if(db.isOpen())
 		{
 			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
 			if(db.isInTransaction())
 			{
 				try
 				{
 					ISqlJetTable session = db.getTable(SESSION_TABLE);
 
 					ISqlJetCursor sc = session.order(SESSION_INDEX);
 					if(!sc.eof())
 					{
 						do
 						{
 							map = Collections.synchronizedMap(new TreeMap<String, Object>());
 							map.put(SESSION_NAME, sc.getString(SESSION_NAME));
 							map.put(SESSION_DEFAULT, (sc.getInteger(SESSION_DEFAULT) == 1));
 							map.put(SESSION_AUTOLOAD, (sc.getInteger(SESSION_AUTOLOAD) == 1));
 							map.put("sessionid", sc.getRowId());
 							rv.addElement(map);
 						}
 						while(sc.next());
 					}
 				}
 				finally
 				{
 					db.commit();
 					//if(!db.isInTransaction())
 						db.close();
 				}
 			}
 		}
 		return rv;
 	}
 
 	public synchronized boolean isSessionDefault(String name) throws Exception
 	{
 		boolean rv = false;
 		db.open();
 		if(db.isOpen())
 		{
 			try
 			{
 				db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
 				if (db.isInTransaction())
 				{
 					ISqlJetTable session = db.getTable(SESSION_TABLE);
 					ISqlJetCursor sc = session.lookup(SESSION_INDEX, name);
 					if (!sc.eof())
 					{
 						rv = (sc.getInteger(SESSION_DEFAULT) == 1); //1 == TRUE / 0 == FALSE
 					}
 				}
 			}
 			finally
 			{
 				db.commit();
 				db.close();
 			}
 		}
 		return rv;
 	}
 
 	public synchronized void updateSettings(String name, Properties prop) throws SqlJetException, Exception
 	{
 		try
 		{
 			//System.out.println(prop);
 			db.open();
 			if(db.isOpen())
 			{
 				db.beginTransaction(SqlJetTransactionMode.WRITE);
 				if (db.isInTransaction())
 				{
 					ISqlJetTable session = db.getTable(SESSION_TABLE);
 					ISqlJetTable config = db.getTable(CONFIG_TABLE);
 					long sid = -1;
 					ISqlJetCursor sc = session.lookup(SESSION_INDEX, name);
 					if (!sc.eof())
 					{
 						sid = sc.getRowId();
 						Map<String, Object> map = Collections.synchronizedMap(new TreeMap<String, Object>());
 						Enumeration e = prop.propertyNames();
 						while (e.hasMoreElements())
 						{
 							String key = (String) e.nextElement();
 							if (key.endsWith(".cfgdesc") || key.endsWith(".cfgtype"))
 							{
 								continue;
 							}
 							if(prop.getProperty(key).equals("false"))
 							{
 								map.put(key, 0);
 								continue;
 							}
 							else if(prop.getProperty(key).equals("true"))
 							{
 								map.put(key, 1);
 								continue;
 							}
 							if(isNumber(prop.getProperty(key)))
 							{
 								map.put(key, Long.parseLong(prop.getProperty(key)));
 								continue;
 							}
 							//i should only be a String at this point.
 							map.put(key, prop.getProperty(key));
 							//setProperty(key, list.getProperty(key));
 							//System.out.println("got: "+key+" = "+ list.getProperty(key));
 
 //							ISqlJetCursor cc = config.lookup(CONFIG_NAME_INDEX, key, sid);
 //							if (!cc.eof())//(name, value, sessionid, desc, type)
 //							{
 //								cc.update(cc.getString(CONFIG_NAME), prop.getProperty(key), cc.getInteger(CONFIG_ID),
 //										cc.getInteger(CONFIG_DESC), cc.getInteger(CONFIG_TYPE));
 //							}
 						}
 						
 						if(!map.isEmpty())
 						{
 							map.put(SESSION_NAME, sc.getString(SESSION_NAME));
 							//System.out.println(map);
 							sc.updateByFieldNames(map);
 						}
 					}
 				}
 				else
 					throw new Exception("Unable to gain transaction on database");
 			}
 			else
 				throw new Exception("Unable to open database");
 		}
 		finally
 		{
 			db.commit();
 			db.close();
 		}
 	}
 
 	private boolean isNumber(String val)
 	{
 		boolean rv = false;
 		try
 		{
 			Long.parseLong(val);
 			rv = true;
 		}
 		catch (NumberFormatException nfe)
 		{
 		}
 		if(rv = false)
 		{
 			try
 			{
 				Integer.parseInt(val);
 				rv = true;
 			}
 			catch(NumberFormatException e){}
 		}
 		return rv;
 	}
 
 	public synchronized User lookupUser(String username) throws SqlJetException
 	{
 		UserTest rv = null;
 		try
 		{//USER TABLE (userid, screenname, fullname, picture_url)
 			db.open();
 			if (db.isOpen())
 			{
 				db.beginTransaction(SqlJetTransactionMode.WRITE);
 				if (db.isInTransaction())
 				{
 					ISqlJetTable ut = db.getTable(USER_TABLE);
 					ISqlJetCursor uc = ut.lookup(USER_NAME_INDEX, username);
 					if (!uc.eof()) //only add if a matchin user is not there
 					{//UserTest(int userid, String screenName, String fullname, String avatar)
 						rv = new UserTest((int)uc.getInteger(USER_ID), uc.getString(USER_NAME), uc.getString(USER_FULLNAME), uc.getString(USER_PICTURE));
 						//ut.insert(user.getId(), user.getScreenName(), user.getName(), user.getProfileImageURL().toString());
 					}
 				}
 			}
 		}
 		finally
 		{
 			//if(db.isInTransaction())
 				db.commit();
 			//if(db.isOpen())
 				db.close();
 		}
 		return rv;
 	}
 
 	public synchronized void registerUser(User user) throws SqlJetException, Exception
 	{
 		try
 		{//USER TABLE (userid, screenname, fullname, picture_url)
 			db.open();
 			if (db.isOpen())
 			{
 				db.beginTransaction(SqlJetTransactionMode.WRITE);
 				if (db.isInTransaction())
 				{
 					ISqlJetTable ut = db.getTable(USER_TABLE);
 					ISqlJetCursor uc = ut.lookup(USER_NAME_INDEX, user.getScreenName());
 					if (uc.eof()) //only add if a matchin user is not there
 					{
 						ut.insert(user.getId(), user.getScreenName(), user.getName(), user.getProfileImageURL().toString());
 					}
 				}
 				else
 				throw new Exception("Unable to gain transaction on database");
 			}
 			else
 				throw new Exception("Unable to open database");
 		}
 		finally
 		{
 			//if(db.isInTransaction())
 				db.commit();
 			//if(db.isOpen())
 				db.close();
 		}
 	}
 
 	public synchronized Vector<User> getRegisteredUsers() throws SqlJetException, Exception
 	{
 		Vector<User> rv = new Vector<User>();
 		try
 		{//USER TABLE (userid, screenname, fullname, picture_url)
 			db.open();
 			if (db.isOpen())
 			{
 				db.beginTransaction(SqlJetTransactionMode.WRITE);
 				if (db.isInTransaction())
 				{
 					ISqlJetTable ut = db.getTable(USER_TABLE);
 					ISqlJetCursor uc = ut.order(USER_NAME_INDEX);
 					if (!uc.eof())
 					{
 						do
 						{
 							rv.addElement(new UserTest((int) uc.getInteger(USER_ID), uc.getString(USER_NAME), uc.getString(USER_FULLNAME), uc.getString(USER_PICTURE)));
 						}
 						while (uc.next());
 					}
 				}
 
 			}
 		}
 		finally
 		{
 			if(db.isInTransaction())
 				db.commit();
 			if(db.isOpen())
 				db.close();
 		}
 		return rv;
 	}
 
 	public synchronized List<User> getRegisteredUsersAsList() throws SqlJetException, Exception
 	{
 		Vector<User> v = getRegisteredUsers();
 		return v.subList(0, v.size());
 	}
 
 	private static class DBQuery extends SwingWorker<Vector, Object>
 	{
 		@Override
 		protected Vector doInBackground() throws Exception
 		{
 			throw new UnsupportedOperationException("Not supported yet.");
 		}
 
 		@Override
 		public void done()
 		{
 
 		}
 	}
 }
