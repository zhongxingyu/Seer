 package net.lala.CouponCodes.api;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import net.lala.CouponCodes.CouponCodes;
 import net.lala.CouponCodes.sql.options.DatabaseOptions;
 
 /**
  * SQLAPI.java - Provides abstract handling for subclasses
  * @author mike101102
  */
 public abstract class SQLAPI {
 
 	private CouponCodes plugin;
 	
 	public SQLAPI(CouponCodes plugin) {
 		this.plugin = plugin;
 	}
 	
 	public void send(String message) {
 		plugin.send(message);
 	}
 	
 	public void sendErr(String message) {
 		plugin.sendErr(message);
 	}
 	
 	/**
 	 * Returns the database options the connection is using
 	 * @return DatabaseOptions
 	 */
 	public abstract DatabaseOptions getDatabaseOptions();
 	
 	/**
 	 * Returns the current connection
 	 * @return Connection
 	 */
 	public abstract Connection getConnection();
 	
 	/**
 	 * Opens the SQL Connection
 	 * @return true if connection is successful
 	 * @throws SQLException
 	 */
 	public abstract boolean open() throws SQLException;
 	
 	/**
 	 * Closes the SQL Connection
 	 * @param disable - True if the plugin is disabling, then the event won't fire
 	 * @throws SQLException
 	 */
	public abstract void close() throws SQLException;
 	
 	/**
 	 * Reloads the SQL Connection, returning its success
 	 * @return true if reload was successful
 	 * @throws SQLException
 	 */
 	public abstract boolean reload() throws SQLException;
 	
 	/**
 	 * Queries the given query into the database
 	 * @param query
 	 * @return ResultSet from the query
 	 * @throws SQLException
 	 */
 	public abstract ResultSet query(String query) throws SQLException;
 	
 	/**
 	 * Queries the given query to form a table, returning its success
 	 * @param table
 	 * @return true if table was created sucessfully
 	 * @throws SQLException
 	 */
 	public abstract boolean createTable(String table) throws SQLException;
 }
