 package com.cffreedom.utils.db;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import com.cffreedom.beans.DbConn;
 import com.cffreedom.exceptions.DbException;
 import com.cffreedom.utils.ConversionUtils;
 import com.cffreedom.utils.KeyValueFileMgr;
 import com.cffreedom.utils.LoggerUtil;
 import com.cffreedom.utils.SystemUtils;
 import com.cffreedom.utils.Utils;
 import com.cffreedom.utils.file.FileUtils;
 
 /**
  * Automated layer for accessing DB Connections that should guarantee that
  * the user is not prompted for any information.
  * 
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
  */
 public class ConnectionManager
 {
 	public static final String DEFAULT_FILE = SystemUtils.getMyCFConfigDir() + SystemUtils.getPathSeparator() + "dbconn.dat";
 	private final LoggerUtil logger = new LoggerUtil(LoggerUtil.FAMILY_UTIL, this.getClass().getPackage().getName() + "." + this.getClass().getSimpleName());
 	private KeyValueFileMgr kvfm = null;
 	private String file = null;
 	private boolean cacheConnections = false;
 	private ConnectionFactory connFactory = new ConnectionFactory();
 	
 	public ConnectionManager() throws DbException
 	{
 		this(ConnectionManager.DEFAULT_FILE);
 	}
 	
 	public ConnectionManager(String file) throws DbException
 	{
 		this(file, false);
 	}
 	
 	public ConnectionManager(boolean cacheConnections) throws DbException
 	{
 		this(ConnectionManager.DEFAULT_FILE, cacheConnections);
 	}
 	
 	public ConnectionManager(String file, boolean cacheConnections) throws DbException
 	{
 		this.loadConnectionFile(file);
 		this.cacheConnections = cacheConnections;
 	}
 	
 	public void loadConnectionFile(String file) throws DbException
 	{
 		final String METHOD = "loadFile";
 		
 		if (FileUtils.fileExists(file) == true)
 		{
 			logger.logDebug(METHOD, "Loading file: " + file);
 			this.file = file;
 			this.kvfm = new KeyValueFileMgr(this.file);  // init so we can use it from other apps
 		}
 		else
 		{
 			throw new DbException(METHOD, "File does not exist: " + file);
 		}
 	}
 	
 	public void close()
 	{
 		try{ this.connFactory.close(); } catch (Exception e){}
 	}
 	
 	public String getConnectionFile() { return this.file; }
 	
 	public boolean keyExists(String key)
 	{
 		return this.kvfm.keyExists(key);
 	}
 	
 	public DbConn getDbConn(String key)
 	{
 		String[] entryVals = this.kvfm.getEntryAsString(key).split("\\|");
 		String type = entryVals[0];
 		String host = entryVals[1];
 		String db = entryVals[2];
 		int port = 0;
 		if (Utils.isInt(entryVals[3]) == true)
 		{
 			port = ConversionUtils.toInt(entryVals[3]);
 		}
 		DbConn dbconn = new DbConn(BaseDAO.getDriver(type),
									BaseDAO.getUrl(type, host, db),
 									type,
 									host,
 									db,
 									port);
 		return dbconn;
 	}
 	
 	public boolean cacheConnections() { return this.cacheConnections; }
 		
 	public Connection getConnection(String key, String user, String pass)
 	{
 		if (this.cacheConnections() == true)
 		{
 			if (this.connFactory.containsPool(key) == false)
 			{
 				DbConn dbconn = this.getDbConn(key);
 				dbconn.setUser(user);
 				dbconn.setPassword(pass);
 				this.connFactory.addPool(key, dbconn);
 			}
 			
 			return this.connFactory.getConnection(key);
 		}
 		else
 		{
 			DbConn dbconn = this.getDbConn(key);
 			return BaseDAO.getConn(dbconn.getDriver(), dbconn.getUrl(), user, pass);
 		}
 	}
 	
 	private String buildValString(DbConn dbconn)
 	{
 		return dbconn.getType() + "|" + dbconn.getDb() + "|" + dbconn.getHost() + "|" + dbconn.getPort();
 	}
 	
 	public boolean addConnection(String key, DbConn dbconn)
 	{
 		return this.kvfm.addEntry(key, this.buildValString(dbconn));
 	}
 	
 	public boolean updateConnection(String key, DbConn dbconn)
 	{
 		return this.kvfm.updateEntry(key, this.buildValString(dbconn));
 	}
 	
 	public boolean deleteConnection(String key)
 	{
 		return this.kvfm.removeEntry(key);
 	}
 	
 	public void printKeys()
 	{
 		this.kvfm.printEntryKeys();
 	}
 	
 	public void printConnInfo(String key)
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
 		boolean success = DbUtils.testConnection(dbconn, user, pass);
 		if (success == true)
 		{
 			Utils.output("Test SQL succeeded for " + key);
 		}
 		else
 		{
 			Utils.output("ERROR: Running test SQL for " + key);
 		}
 		return success;
 	}
 }
