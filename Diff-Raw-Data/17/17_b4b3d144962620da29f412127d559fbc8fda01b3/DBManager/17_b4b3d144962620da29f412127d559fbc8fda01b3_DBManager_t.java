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
 import twitz.testing.UserTest;
 
 /**
  *
  * @author mistik1
  */
 public class DBManager {
 
 	//USERS TABLE
 	public static final String	USER_TABLE = "users";
 	public static final String USER_NAME = "screenname"; //userid, screenname, fullname, picture_url
 	public static final String USER_ID = "userid";
 	public static final String USER_FULLNAME = "fullname";
 	public static final String USER_PICTURE = "picture_url";
 	public static final String USER_NAME_INDEX = "screenname_index";
 	public static final String USER_ID_INDEX = "userid_index";
 
 	//SESSION TABLE (name)
 	public static final String SESSION_TABLE = "login_session";
 	public static final String SESSION_NAME = "session_name";
 	public static final String SESSION_INDEX = "session_index";
 
 	//CONFIG_TABLE (name, value, sessionid, cfgdesc, cfgtype)
 	public static final String CONFIG_TABLE = "settings";
 	public static final String CONFIG_NAME = "setting";
 	public static final String CONFIG_VALUE = "value";
 	public static final String CONFIG_ID = "sessionid";
 	public static final String CONFIG_DESC = "cfdesc";
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
 
 	private static File FILE_DIR = SettingsManager.getConfigDirectory();
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
 		logger.info("Entering configureDb() firstrun = "+firstrun);
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
 			}, SqlJetTransactionMode.WRITE);
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
 		String sessionTableQuery = "CREATE TABLE "+ SESSION_TABLE +" ("+ SESSION_NAME +" TEXT NOT NULL)";
 		String configTableQuery = "CREATE TABLE "+ CONFIG_TABLE + " ("+ CONFIG_NAME +" TEXT NOT NULL PRIMARY KEY, "+
 				CONFIG_VALUE +" TEXT, "+ CONFIG_ID +" INTEGER NOT NULL, "+ CONFIG_DESC +" INTEGER, "+
 				CONFIG_TYPE +" INTEGER)";
 		String configTypeQuery = "CREATE TABLE "+ TYPE_TABLE +" ("+ TYPE_ID +" INTEGER NOT NULL,"+
 				TYPE_VALUE +" TEXT NOT NULL)";
 		String configDescQuery = "CREATE TABLE "+ DESC_TABLE +" ("+ DESC_ID +" INTEGER NOT NULL, "+
 				DESC_VALUE +" TEXT)";
 		String userNameIndex = "CREATE INDEX " + USER_NAME_INDEX + " ON " + USER_TABLE + "(" +  USER_NAME + ")";
 		String userIdIndex = "CREATE INDEX " + USER_ID_INDEX + " ON " + USER_TABLE + "(" +  USER_ID + ")";
 		String configIndex = "CREATE INDEX " + CONFIG_INDEX + " ON " + CONFIG_TABLE + "(" +  CONFIG_ID + ")";
 		String configNameIndex = "CREATE INDEX " + CONFIG_NAME_INDEX + " ON " + CONFIG_TABLE + "(" +  CONFIG_NAME + ","+ CONFIG_ID +")";
 		String configValueIndex = "CREATE INDEX " + CONFIG_VALUE_INDEX + " ON " + CONFIG_TABLE + "(" +  CONFIG_VALUE + ")";
 		String configDescIndex = "CREATE INDEX " + DESC_INDEX + " ON " + DESC_TABLE + "(" +  DESC_ID + ")";
 		String configTypeIndex = "CREATE INDEX " + TYPE_INDEX + " ON " + TYPE_TABLE + "(" +  TYPE_ID + ")";
 		String sessionIndex = "CREATE INDEX " + SESSION_INDEX + " ON "+ SESSION_TABLE +"("+ SESSION_NAME +")";
 
 //		if(logdebug)
 //		{
 			logger.log(Level.INFO, userTableQuery);
 			logger.log(Level.INFO, configTableQuery);
 			logger.log(Level.INFO, configTypeQuery);
 			logger.log(Level.INFO, configDescQuery);
 //		}
 
 		try
 		{
 			db.open();
 			db.beginTransaction(SqlJetTransactionMode.WRITE);
 			
 			db.createTable(userTableQuery);
 			db.createIndex(userNameIndex);
 			db.createIndex(userIdIndex);
 			db.createTable(sessionTableQuery);
 			db.createIndex(sessionIndex);
 			db.createTable(configTableQuery);
 			db.createIndex(configIndex);
 			db.createIndex(configNameIndex);
 			db.createIndex(configValueIndex);
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
 		catch (SqlJetException ex)
 		{
 			logger.log(Level.SEVERE, ex.getMessage());
 		}
 
 //		String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" + SECOND_NAME_FIELD + " TEXT NOT NULL PRIMARY KEY , " + FIRST_NAME_FIELD + " TEXT NOT NULL, " + DOB_FIELD + " INTEGER NOT NULL)";
 //		String createFirstNameIndexQuery = "CREATE INDEX " + FULL_NAME_INDEX + " ON " + TABLE_NAME + "(" +  FIRST_NAME_FIELD + "," + SECOND_NAME_FIELD + ")";
 //		String createDateIndexQuery = "CREATE INDEX " + DOB_INDEX + " ON " + TABLE_NAME + "(" +  DOB_FIELD + ")";
 	}//}}}
 
 	private void populateDefaultSettingsTable(String name) throws SqlJetException//{{{
 	{
 		if(name == null || name.equals(""))
 			throw new IllegalArgumentException("name must NOT be null or blank string");
 		
 		db.open();
 		
 		db.beginTransaction(SqlJetTransactionMode.WRITE);
 		ISqlJetTable ut = db.getTable(USER_TABLE);
 		ISqlJetTable session = db.getTable(SESSION_TABLE);
 		ISqlJetTable config = db.getTable(CONFIG_TABLE);
 		ISqlJetTable type = db.getTable(TYPE_TABLE);
 		ISqlJetTable desc = db.getTable(DESC_TABLE);
 
 		try
 		{//(name, value, sessionid, desc, type)
 			if(firstrun)
 			{
 				type.insert(0, "Password");
 				type.insert(1, "File");
 				type.insert(2, "Boolean");
 				type.insert(3, "String");
 				type.insert(4, "Theme");
 				type.insert(5, "Internal");
 
 				desc.insert(0, "Twitter ID");
 				desc.insert(1, "Twitter Password");
 				desc.insert(2, "Twitter Profile Image");
 				desc.insert(3, "Twitter Proxy?");
 				desc.insert(4, "Twitter Proxy Port");
 				desc.insert(5, "Twitter Proxy Host");
 				desc.insert(6, "Twitter Proxy User");
 				desc.insert(7, "Twitter Proxy Password");
 				desc.insert(8, "Minimize on Startup");
 				desc.insert(9, "Start Undecorated");
 				desc.insert(10, "Twitz Theme");
 				desc.insert(11, "Enable Friends Tab");
 				desc.insert(12, "Enable Blocked Tab");
 				desc.insert(13, "Enable Following Tab");
 				desc.insert(14, "Enable Followers Tab");
 				desc.insert(15, "Enable Search Tab");
 				desc.insert(16, "Internal Setting");
 			}
 
 			long sid = session.insert(name);
 			config.insert("twitter.id", "changeme", sid, 0, 3);
 			config.insert("twitter.password", "changeme", sid, 1, 0);
 			config.insert("twitter.picture", "", sid, 2, 1);
 			//Proxy settings
 			config.insert("twitter.use_proxy", "false", sid, 3, 2);
 			//Port (Must have a value if use_proxy is enalbed)
 			config.insert("twitter.proxy_port", "8080", sid, 4, 3);
 			//Host (Must have a value if use_proxy is enalbed)
 			config.insert("twitter.proxy_host", "", sid, 5, 3);
 			//User (Can be blank)
 			config.insert("twitter.proxy_user", "", sid, 6, 3);
 			//Password (Can be blank)
 			config.insert("twitter.proxy_password", "", sid, 7, 0);
 			//Minimize settings
 			config.insert("minimize.startup", "false", sid, 8, 2);
 			//Decoration settings
 			config.insert("twitz.undecorated", "false", sid, 9, 2);
 			config.insert("twitz.skin", "MistAqua", sid, 10,4);
 			//This is a null entry AKA an internally managed property
 			//This keeps the mini/full state of the program between restarts
 			config.insert("minimode", "false", sid, 16, 5);
 			//Internal Size settings
 			config.insert("twitz.last.height", "640", sid, 16, 5);
 			//Tab positions
 			config.insert("tab.position", "north", sid, 16, 5);
 			//Friends Tab settings
 			config.insert("tab.friends", "true", sid, 11, 2);
 			//Blocked Tab settings
 			config.insert("tab.blocked", "false", sid, 12, 5);
 			//Following tab settings
 			config.insert("tab.following", "false", sid, 13, 5);
 			//Followers tab settings
 			config.insert("tab.followers", "false", sid, 14, 5);
 			//Search tab settings
 			config.insert("tab.search", "true", sid, 15, 2);
 		}
 		finally
 		{
 			db.commit();
 			db.close();
 		}
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
 			if(sc.getRowCount() >= 1) //sessions will have unique names
 			{
 				sid = sc.getRowId();
 				ISqlJetCursor cc = config.lookup(CONFIG_INDEX, sid);
 				if(!cc.eof())
 				{
 					do
 					{
 						String key = cc.getString(CONFIG_NAME);
 						String val = cc.getString(CONFIG_VALUE);
 						ISqlJetCursor tc = type.lookup(TYPE_INDEX, cc.getInteger(CONFIG_TYPE));
 						ISqlJetCursor dc = desc.lookup(DESC_INDEX, cc.getInteger(CONFIG_DESC));
 						rv.setProperty(key, val);
 						if(!tc.eof())
 							rv.setProperty(key+".cfgtype", tc.getString(TYPE_VALUE));
 						if(!dc.eof())
 							rv.setProperty(key+".cfgdesc", dc.getString(DESC_VALUE));
 					}
 					while(cc.next());
 				}
 			}
 		}
 		finally
 		{
 			db.commit();
 			db.close();
 		}
 		//System.out.println("loading properties "+rv);
 		return rv;
 	}
 
	public synchronized void updateSettings(String name, Properties prop) throws SqlJetException
 	{
 		try
 		{
 			db.open();
 			db.beginTransaction(SqlJetTransactionMode.WRITE);
 			ISqlJetTable session = db.getTable(SESSION_TABLE);
 			ISqlJetTable config = db.getTable(CONFIG_TABLE);
 			long sid = -1;
 			ISqlJetCursor sc = session.lookup(SESSION_INDEX, name);
 			if(!sc.eof())
 			{
 				sid = sc.getRowId();
 				Enumeration e = prop.propertyNames();
 				while (e.hasMoreElements()) {
 					String key = (String)e.nextElement();
 					if(key.endsWith(".cfgdesc") || key.endsWith(".cfgtype"))
 						continue;
 					//setProperty(key, list.getProperty(key));
 					//System.out.println("got: "+key+" = "+ list.getProperty(key));
 
 					ISqlJetCursor cc = config.lookup(CONFIG_NAME_INDEX, key, sid);
 					if (!cc.eof())//(name, value, sessionid, desc, type)
 					{
 						cc.update(cc.getString(CONFIG_NAME), prop.getProperty(key), cc.getInteger(CONFIG_ID),
 								cc.getInteger(CONFIG_DESC), cc.getInteger(CONFIG_TYPE));
 					}
 				}
 			}
 		}
 		finally
 		{
 			db.commit();
 			db.close();
 		}
 	}
 
	public synchronized User lookupUser(String username)
 	{
 		return new UserTest();
 	}
 
	public synchronized void registerUser(User user) throws SqlJetException
 	{
 		try
 		{//USER TABLE (userid, screenname, fullname, picture_url)
 			db.open();
 			db.beginTransaction(SqlJetTransactionMode.WRITE);
 			ISqlJetTable ut = db.getTable(USER_TABLE);
 			ISqlJetCursor uc = ut.lookup(USER_NAME_INDEX, user.getScreenName());
 			if(uc.eof()) //only add if a matchin user is not there
 			{
 				ut.insert(user.getId(), user.getScreenName(), user.getName(), user.getProfileImageURL().toString());
 			}
 		}
 		finally
 		{
 			db.commit();
 			db.close();
 		}
 		if(!users.contains(user))
 			users.addElement(user);
 	}
 
	public synchronized Vector<User> getRegisteredUsers() throws SqlJetException
 	{
 		Vector<User> rv = new Vector<User>();
 		try
 		{//USER TABLE (userid, screenname, fullname, picture_url)
 			db.open();
 			db.beginTransaction(SqlJetTransactionMode.WRITE);
 			ISqlJetTable ut = db.getTable(USER_TABLE);
 			ISqlJetCursor uc = ut.order(USER_NAME_INDEX);
 			if(!uc.eof())
 			{
 				do
 				{
 //					Map<String, String> map = Collections.synchronizedMap(new TreeMap<String, String>());
 //					map.put(USER_ID, uc.getInteger(USER_ID)+"");
 //					map.put(USER_NAME, uc.getString(USER_NAME));
 //					map.put(USER_FULLNAME, uc.getString(USER_FULLNAME));
 //					map.put(USER_PICTURE, uc.getString(USER_PICTURE));
 					rv.addElement(new UserTest((int) uc.getInteger(USER_ID),uc.getString(USER_NAME), uc.getString(USER_FULLNAME), uc.getString(USER_PICTURE)));
 				}
 				while(uc.next());
 			}
 		}
 		finally
 		{
 			db.commit();
 			db.close();
 		}
 		//return (Vector<User>)users.clone();
 		return rv;
 	}
 
	public synchronized List<User> getRegisteredUsersAsList() throws SqlJetException
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
