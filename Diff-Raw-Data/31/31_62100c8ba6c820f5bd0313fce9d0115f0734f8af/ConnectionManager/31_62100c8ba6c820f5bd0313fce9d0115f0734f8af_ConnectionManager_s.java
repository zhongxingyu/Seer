 package com.cffreedom.utils.db;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cffreedom.beans.DbConn;
 import com.cffreedom.beans.DbType;
 import com.cffreedom.exceptions.DbException;
 import com.cffreedom.exceptions.FileSystemException;
 import com.cffreedom.exceptions.InfrastructureException;
 import com.cffreedom.utils.Convert;
 import com.cffreedom.utils.SystemUtils;
 import com.cffreedom.utils.Utils;
 import com.cffreedom.utils.file.FileUtils;
 import com.cffreedom.utils.security.SecurityCipher;
 
 /**
  * Automated layer for accessing DB Connections that should guarantee that
  * the user is not prompted for any information.
  * 
  * Original Class: com.cffreedom.utils.db.ConnectionManager
  * @author markjacobsen.net (http://mjg2.net/code)
  * Copyright: Communication Freedom, LLC - http://www.communicationfreedom.com
  * 
  * Free to use, modify, redistribute.  Must keep full class header including 
  * copyright and note your modifications.
  * 
  * If this helped you out or saved you time, please consider...
  * 1) Donating: http://www.communicationfreedom.com/go/donate/
  * 2) Shoutout on twitter: @MarkJacobsen or @cffreedom
  * 3) Linking to: http://visit.markjacobsen.net
  * 
  * Changes:
  * 2013-05-06 	markjacobsen.net 	Created
  * 2013-06-25 	markjacobsen.net 	Added connection pooling to getConnection()
  * 2013-07-15	markjacobsne.net 	Replaced the use of KeyValueFileMgr with a properties file
  * 2013-07-15 	markjacobsen.net 	Added support for commons-dbcp
  * 2013-07-17 	markjacobsen.net 	Added support for the dbconn.properties file being on the classpath
  * 2013-09-09	markjacobsen.net 	printKeys() prints keys in sorted order
  * 2013-09-20	markjacobsen.net 	Updates to testConnection()
  * 2013-11-08	MarkJacobsen.net	Enhanced connection pooling
  */
 public class ConnectionManager
 {
 	public static final String PROP_FILE = "dbconn.properties";
 	public static final String DEFAULT_FILE = SystemUtils.getDirConfig() + SystemUtils.getPathSeparator() + PROP_FILE;
 	public static final boolean CREATE_FILE = false;
 	private static final Logger logger = LoggerFactory.getLogger("com.cffreedom.utils.db.ConnectionManager");
 	private HashMap<String, DbConn> conns = new HashMap<String, DbConn>();
 	private Hashtable<String, BasicDataSource> pools = null;
 	private String file = null;
 	private SecurityCipher cipher = new SecurityCipher("abasickeyyoushouldnotchange");
 	
 	public ConnectionManager() throws FileSystemException, InfrastructureException
 	{
 		this(ConnectionManager.DEFAULT_FILE);
 	}
 	
 	/**
 	 * Load and store connection properties in the file specified. If you pass null, the properties 
 	 * will NOT be persisted
 	 * @param file File to load/store connection properties
 	 * @throws FileSystemException
 	 * @throws InfrastructureException
 	 */
 	public ConnectionManager(String file) throws FileSystemException, InfrastructureException
 	{
 		this(file, ConnectionManager.CREATE_FILE);
 	}
 	
 	public ConnectionManager(String file, boolean createPropFileIfNew) throws FileSystemException, InfrastructureException
 	{		
 		this.loadFile(file, createPropFileIfNew);
 	}
 	
 	/**
 	 * Use to enable commons-dbcp connection pooling. Note that this value
 	 * and connection pooling will only be done via this class if it is 
 	 * unable to get a JNDI connection. In other words, if this class can get
 	 * a JNDI connection it will, and we'll never setup an internal pool
 	 * @param enable True to enable, false to disable
 	 */
 	public void enableConnectionPooling(boolean enable)
 	{
 		if (enable == true){
 			if (this.pools == null){
 				logger.info("Turning on Connection Pooling");
 				this.pools = new Hashtable<String, BasicDataSource>();
 			}
 		}else{
 			logger.info("Turning off Connection Pooling");
 			this.pools = null;
 		}
 	}
 	
 	public void loadFile(String file) throws FileSystemException, InfrastructureException { this.loadFile(file, ConnectionManager.CREATE_FILE); }
 	@SuppressWarnings("resource")
 	public void loadFile(String file, boolean createPropFileIfNew) throws FileSystemException, InfrastructureException
 	{
 		InputStream inputStream = null;
 		Properties props = new Properties();
 		
 		try
 		{
 			this.file = file;
 		
 			if ((this.file != null) && (FileUtils.fileExists(this.file) == false) && (createPropFileIfNew == true))
 			{
 				logger.debug("Attempting to create file: {}", this.file);
 				this.save();
 			}
 			
 			if (FileUtils.fileExists(this.file) == true)
 			{
 				logger.info("Loading from passed in file: {}", this.file);
 				inputStream = new FileInputStream(this.file);
 			}
 			else
 			{
 				logger.info("Attempting to find file on classpath: {}", ConnectionManager.PROP_FILE);
 				inputStream = this.getClass().getClassLoader().getResourceAsStream(ConnectionManager.PROP_FILE);
 			}
 			
 			if (inputStream == null)
 			{
 				if ((this.file == null) || (createPropFileIfNew == false))
 				{
 					logger.warn("No connection file. Creating memory based ConnectionManager");
 				}
 				else
 				{
 					throw new InfrastructureException("Invalid connection file or no default file \""+ConnectionManager.PROP_FILE+"\" found on the classpath");
 				}
 			}
 			else
 			{
 				logger.debug("Loading property file");
 				
 				props.load(inputStream);
 				inputStream.close();
 				
 				if (props.getProperty("keys") == null)
 				{
 					logger.warn("No \"keys\" property exists so nothing will be read");
 				}
 				else
 				{
 					String[] keys = props.getProperty("keys").split(",");
 					
 					for (String key : keys)
 					{
 						logger.debug("Loading: {}", key);
 						String type = props.getProperty(key + ".type");
 						String host = props.getProperty(key + ".host");
 						String db = props.getProperty(key + ".db");
 						String port = props.getProperty(key + ".port");
 						String user = props.getProperty(key + ".user");
 						String password = props.getProperty(key + ".password");
 						String jndi = props.getProperty(key + ".jndi");
 						
 						DbType dbType = null;
 						if (Utils.hasLength(type) == true)
 						{
 							dbType = DbType.valueOf(type);
							if (dbType == null)
							{
								// Make backward compatible
								if (type.equalsIgnoreCase("DB2_JCC") == true) { dbType = DbType.DB2; }
								else if (type.equalsIgnoreCase("DB2_APP") == true) { dbType = DbType.DB2; }
							}
 						}
 						if ((port == null) || (port.trim().length() == 0)) { port = "0"; }
 						
 						DbConn dbconn = new DbConn(DbUtils.getDefaultDriver(dbType),
 												DbUtils.getUrl(dbType, host, db, Convert.toInt(port)), 
 												dbType,
 												host,
 												db,
 												Convert.toInt(port));
 						
 						if (Utils.hasLength(user) == true) { dbconn.setUser(user); }
 						if (Utils.hasLength(password) == true) { dbconn.setPassword(this.cipher.decrypt(password)); }
 						if (Utils.hasLength(jndi) == true) { dbconn.setJndi(jndi); }
 		
 						this.conns.put(key, dbconn);
 					}
 				}
 			}
 			
 			logger.debug("Loaded {} connections", this.conns.size());
 		}
 		catch (FileNotFoundException e)
 		{
 			throw new FileSystemException("FileNotFound", e);
 		}
 		catch (IOException e)
 		{
 			throw new FileSystemException("IOException", e);
 		}
 	}
 	
 	private boolean save()
 	{
 		if (this.file == null)
 		{
 			logger.warn("No file to save to");
 			return false;
 		}
 		else
 		{
 			ArrayList<String> lines = new ArrayList<String>();
 			logger.debug("Saving to file {}", this.getConnectionFile());
 			
 			lines.add("#--------------------------------------------------------------------------------------");
 			lines.add("# While it is not recommended, you can put usernames and passwords into this file.");
 			lines.add("# Passwords do need to be encrypted using the SecurityUtils class.");
 			lines.add("# It is suggested that you use the DbConnManager app in cffreedom-cl-apps to maintain");
 			lines.add("# this file.");
 			lines.add("#--------------------------------------------------------------------------------------");
 			lines.add("");
 			
 			if (this.conns.size() <= 0)
 			{
 				logger.warn("No DbConn objects cached so no actual values will be written");
 				lines.add("# No connections to save");
 			}
 			else
 			{
 				lines.add("keys=" + Convert.toDelimitedString(this.conns.keySet(), ","));
 				lines.add("");
 				
 				for (String entry : this.conns.keySet())
 				{
 					logger.trace(entry);
 					DbConn conn = this.getDbConn(entry);
 					lines.add(entry + ".db=" + this.getPropFileValue(conn.getDb()));
 					lines.add(entry + ".type=" + this.getPropFileValue(conn.getType().value));
 					lines.add(entry + ".host=" + this.getPropFileValue(conn.getHost()));
 					lines.add(entry + ".port=" + this.getPropFileValue(Convert.toString(conn.getPort())));
 					lines.add(entry + ".user=" + this.getPropFileValue(conn.getUser()));
 					lines.add(entry + ".password=" + this.getPropFileValue(conn.getPassword(), true));
 					lines.add(entry + ".jndi=" + this.getPropFileValue(conn.getJndi()));
 					lines.add("");
 				}
 			}
 			
 			return FileUtils.writeLinesToFile(this.getConnectionFile(), lines);
 		}
 	}
 	
 	private String getPropFileValue(String val) { return getPropFileValue(val, false); }
 	private String getPropFileValue(String val, boolean encrypt)
 	{
 		if (val == null){
 			return "";
 		}else{
 			if (encrypt == true){
 				return this.cipher.encrypt(val);
 			}else{
 				return val;
 			}
 		}
 	}
 	
 	public void close()
 	{
 		if (this.cacheConnections() == true)
 		{
 			for (String key : this.pools.keySet())
 			{
 				BasicDataSource bds = this.pools.get(key);
 				try
 				{
 					logger.debug("Closing pool: ", key);
 					bds.close();
 					this.pools.remove(key);
 				}
 				catch (SQLException e)
 				{
 					logger.error("Error closing pool: ", e.getMessage());
 				}
 			}
 		}
 	}
 	
 	public String getConnectionFile() { return this.file; }
 	
 	public boolean keyExists(String key)
 	{
 		return this.conns.containsKey(key);
 	}
 	
 	public DbConn getDbConn(String key)
 	{
 		return this.conns.get(key);
 	}
 	
 	public boolean cacheConnections() { if (this.pools != null){ return true; }else{ return false; } }
 		
 	/**
 	 * Get a DB Connection for the key passed in. The methods used to try and get a Connection
 	 * are executed in this order:
 	 *  1) Try and get a connection by the JNDI name
 	 *  2) Try getting a connection from a pool if connection pooling has been enabled
 	 *  3) Try getting a connection by driver and url
 	 *  4) Try to get a connection by JNDI using the key passed in
 	 * @param key
 	 * @param user Override user in DbConn if not null
 	 * @param pass Override password in DbConn if not null
 	 * @return
 	 */
 	public Connection getConnection(String key, String user, String pass)
 	{
 		Connection conn = null;
 		DbConn dbconn = this.getDbConn(key);
 		
 		if (dbconn != null)
 		{
 			// Set / override username and password if passed in
 			if (user != null) { dbconn.setUser(user); }
 			if (pass != null) { dbconn.setPassword(pass); }
 		}
 		else
 		{
 			logger.warn("A DbConn does not exist for key: {}", key);
 			// Note: Not throwing an error here, because the last thing we'll try to do
 			// if we couldn't get a connection is try to get a connection via JNDI
 			// using the key.
 		}
 		
 		// Default to a JNDI connection if one exists
 		if ((dbconn != null) && (dbconn.getJndi() != null) && (dbconn.getJndi().length() > 0))
 		{
 			logger.trace("Getting JNDI connection: {}", key);
 			try
 			{
 				conn = DbUtils.getConnectionJNDI(dbconn.getJndi());
 			}
 			catch (DbException | InfrastructureException e)
 			{
 				logger.warn("Unable to get JNDI connection");
 			}
 		}
 		
 		// Next use connection pooling if configured
 		if ((conn == null) && (this.cacheConnections() == true))
 		{
 			if (this.pools.containsKey(key) == false)
 			{
 				logger.debug("Initializing connection pool: {}", key);				
 				BasicDataSource bds = new BasicDataSource();
 			    bds.setDriverClassName(dbconn.getDriver());
 			    bds.setUrl(dbconn.getUrl());
 			    bds.setUsername(dbconn.getUser());
 			    bds.setPassword(dbconn.getPassword());
 			    bds.setPoolPreparedStatements(true);
 			    bds.setMaxOpenPreparedStatements(30);
 			    bds.setMaxWait(30 * 1000); // seconds * 1000 = milliseconds
 			    
 			    String validationSql = DbUtils.getTestSql(dbconn.getType());
 			    if (Utils.hasLength(validationSql) == true)
 			    {
 			    	bds.setValidationQuery(validationSql); 
 			    	bds.setTestOnBorrow(true);
 			    }
 				
 				this.pools.put(key, bds);
 			}
 			
 			try
 			{
 				logger.trace("Getting pooled connection: {}", key);
 				conn = this.pools.get(key).getConnection();
 			}
 			catch (SQLException e)
 			{
 				logger.error("Error getting pooled connection");
 			}
 		}
 		
 		// Then try getting a non-pooled connection
 		if ((conn == null) && (dbconn != null))
 		{
 			logger.trace("Getting non-pooled connection: {}", key);
 			try
 			{
 				conn = DbUtils.getConnection(dbconn.getDriver(), dbconn.getUrl(), dbconn.getUser(), dbconn.getPassword());
 			}
 			catch (DbException | InfrastructureException e)
 			{
 				logger.error(e.getClass().getSimpleName() + " getting non-pooled connection: " + e.getMessage());
 			}
 		}
 		
 		// Finally make a last ditch attempt to just get a jndi connection from the passed in key
 		if (conn == null)
 		{
 			logger.warn("Making last ditch attempt to get JNDI connection: {}", key);
 			try
 			{
 				conn = DbUtils.getConnectionJNDI(key);
 			}
 			catch (DbException | InfrastructureException e)
 			{
 				logger.error(e.getClass().getSimpleName() + " attempting to get last ditch JNDI connection: " + e.getMessage());
 			}
 		}
 		
 		return conn;
 	}
 	
 	public boolean addConnection(String key, DbConn dbconn)
 	{
 		if (this.conns.containsKey(key) == false)
 		{
 			logger.debug("Adding: {}", key);
 			this.conns.put(key, dbconn);
 			this.save();
 			return true;
 		}
 		else
 		{
 			logger.error("A connection named {} already exists", key);
 			return false;
 		}
 	}
 	
 	public boolean updateConnection(String key, DbConn dbconn)
 	{
 		logger.debug("Updating: {}", key);
 		deleteConnection(key);
 		return addConnection(key, dbconn);
 	}
 	
 	public boolean deleteConnection(String key)
 	{
 		if (this.conns.containsKey(key) == true)
 		{
 			logger.debug("Deleting: {}", key);
 			this.conns.remove(key);
 			this.save();
 			return true;
 		}
 		else
 		{
 			logger.error("A connection named {} does not exist");
 			return false;
 		}
 	}
 	
 	/**
 	 * Print all the DB Connection keys contained in the ConnectionManager in sorted order
 	 */
 	public void printKeys()
 	{
 		Utils.output("Keys");
 		Utils.output("======================");
 		if ((this.conns != null) && (this.conns.size() > 0))
 		{
 			Map<String, Object> sorted = new TreeMap<String, Object>(this.conns); // Convert to TreeMap for sorting
 			for(String key : sorted.keySet())
 			{
 				Utils.output(key);
 			}
 		}
 	}
 	
 	/**
 	 * Print the connection details for the passed in connection key
 	 * @param key Key to display details for
 	 */
 	public void printKey(String key)
 	{
 		DbConn dbconn = getDbConn(key);
		Utils.output("");
		Utils.output("Key = " + key);
		Utils.output("Type = " + dbconn.getType());
		Utils.output("DB = " + dbconn.getDb());
		Utils.output("Host = " + dbconn.getHost());
		Utils.output("Port = " + dbconn.getPort());
 	}
 	
 	public boolean testConnection(String key, String user, String pass)
 	{
 		DbConn dbconn = getDbConn(key);
 		boolean success = true;
 		
 		try
 		{
 			if (dbconn == null)
 			{
 				throw new InfrastructureException("dbconn is null for key: " + key);
 			}
 			DbUtils.testConnection(dbconn, user, pass);
 			logger.info("Test SQL succeeded for {}", key);
 		}
 		catch (Exception e)
 		{
 			logger.error(e.getClass().getSimpleName() + " running test SQL for {}, {}", key, e.getMessage());
 			e.printStackTrace();
 			success = false;
 		}
 		return success;
 	}
 }
