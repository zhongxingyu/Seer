 /**
  * Database Handler
  * Abstract superclass for all subclass database files.
  * 
  * Date Created: 2011-08-26 19:08
  * @author PatPeter
  */
 package lib.PatPeter.SQLibrary;
 
 /*
  *  MySQL
  */
 // import java.net.MalformedURLException;
 
 /*
  *  SQLLite
  */
 //import java.io.File;
 //import java.sql.DatabaseMetaData;
 
 /*
  *  Both
  */
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 //import java.sql.DriverManager;
 import java.sql.ResultSet;
 //import java.sql.SQLException;
 //import java.sql.Statement;
 import java.util.logging.Logger;
 
 public abstract class DatabaseHandler {
 	protected Logger log;
 	protected final String PREFIX;
 	protected final String DATABASE_PREFIX;
 	protected boolean connected;
 	protected Connection connection;
 	protected enum Statements {
 		SELECT, INSERT, UPDATE, DELETE, DO, REPLACE, LOAD, HANDLER, CALL, // Data manipulation statements
 		CREATE, ALTER, DROP, TRUNCATE, RENAME,  // Data definition statements
 		
 		// H2-specific
 		SCRIPT,
 		
 		// MySQL-specific
 		START, COMMIT, ROLLBACK, SAVEPOINT, LOCK, UNLOCK, // MySQL Transactional and Locking Statements
 		PREPARE, EXECUTE, DEALLOCATE, // Prepared Statements
 		SET, SHOW, // Database Administration
 		DESCRIBE, EXPLAIN, HELP, USE, // Utility Statements
 		
 		// SQLite-specific
 		ANALYZE, ATTACH, BEGIN, DETACH, END, INDEXED, ON, PRAGMA, REINDEX, RELEASE, VACUUM
 	}
 
 	/*
 	 *  MySQL, SQLLite
 	 */
 
 	public DatabaseHandler(Logger log, String prefix, String dp) {
 		this.log = log;
 		this.PREFIX = prefix;
 		this.DATABASE_PREFIX = dp;
 		this.connected = false;
 		this.connection = null;
 	}
 
 	/**
 	 * <b>writeInfo</b><br>
 	 * <br>
 	 * &nbsp;&nbsp;Writes information to the console.
 	 * <br>
 	 * <br>
 	 * @param toWrite - the <a href="http://download.oracle.com/javase/6/docs/api/java/lang/String.html">String</a>
 	 * of content to write to the console.
 	 */
 	protected void writeInfo(String toWrite) {
 		if (toWrite != null) {
 			this.log.info(this.PREFIX + this.DATABASE_PREFIX + toWrite);
 		}
 	}
 
 	/**
 	 * <b>writeError</b><br>
 	 * <br>
 	 * &nbsp;&nbsp;Writes either errors or warnings to the console.
 	 * <br>
 	 * <br>
 	 * @param toWrite - the <a href="http://download.oracle.com/javase/6/docs/api/java/lang/String.html">String</a>
 	 * written to the console.
 	 * @param severe - whether console output should appear as an error or warning.
 	 */
 	protected void writeError(String toWrite, boolean severe) {
 		if (toWrite != null) {
 			if (severe) {
 				this.log.severe(this.PREFIX + this.DATABASE_PREFIX + toWrite);
 			} else {
 				this.log.warning(this.PREFIX + this.DATABASE_PREFIX + toWrite);
 			}
 		}
 	}
 
 	/**
 	 * <b>initialize</b><br>
 	 * <br>
 	 * &nbsp;&nbsp;Used to check whether the class for the SQL engine is installed.
 	 * <br>
 	 * <br>
 	 */
 	protected abstract boolean initialize();
 
 	/**
 	 * <b>open</b><br>
 	 * <br>
 	 * &nbsp;&nbsp;Opens a connection with the database.
 	 * <br>
 	 * <br>
 	 * @return the success of the method.
 	 */
 	public abstract Connection open();
 
 	/**
 	 * <b>close</b><br>
 	 * <br>
 	 * &nbsp;&nbsp;Closes a connection with the database.
 	 * <br>
 	 * <br>
 	 */
 	public abstract void close();
 
 	/**
 	 * <b>getConnection</b><br>
 	 * <br>
 	 * &nbsp;&nbsp;Gets the connection variable 
 	 * <br>
 	 * <br>
 	 * @return the <a href="http://download.oracle.com/javase/6/docs/api/java/sql/Connection.html">Connection</a> variable.
 	 */
 	public abstract Connection getConnection();
 
 	/**
 	 * <b>checkConnection</b><br>
 	 * <br>
 	 * Checks the connection between Java and the database engine.
 	 * <br>
 	 * <br>
 	 * @return the status of the connection, true for up, false for down.
 	 */
 	public abstract boolean checkConnection();
 
 	/**
 	 * <b>query</b><br>
 	 * &nbsp;&nbsp;Sends a query to the SQL database.
 	 * <br>
 	 * <br>
 	 * @param query - the SQL query to send to the database.
 	 * @return the table of results from the query.
 	 */
 	public abstract ResultSet query(String query);
 
 	/**
 	 * <b>prepare</b><br>
 	 * &nbsp;&nbsp;Prepares to send a query to the database.
 	 * <br>
 	 * <br>
 	 * @param query - the SQL query to prepare to send to the database.
 	 * @return the prepared statement.
 	 */
 	public abstract PreparedStatement prepare(String query);
 
 	/**
 	 * <b>getStatement</b><br>
 	 * &nbsp;&nbsp;Determines the name of the statement and converts it into an enum.
 	 * <br>
 	 * <br>
 	 * @throws SQLException 
 	 */
 	protected Statements getStatement(String query) throws SQLException {
 		String trimmedQuery = query.trim();
 		for (Statements s : Statements.values()){
			if (trimmedQuery.startsWith(s.name().toLowerCase()))
 				return s;
 		}
 		throw new SQLException("Not a valid statement!");
 		/*if (trimmedQuery.length()>=6){
 			if (trimmedQuery.substring(0,6).equalsIgnoreCase("SELECT"))
 				return Statements.SELECT;
 			else if (trimmedQuery.substring(0,6).equalsIgnoreCase("INSERT"))
 				return Statements.INSERT;
 			else if (trimmedQuery.substring(0,6).equalsIgnoreCase("UPDATE"))
 				return Statements.UPDATE;
 			else if (trimmedQuery.substring(0,6).equalsIgnoreCase("DELETE"))
 				return Statements.DELETE;
 			else if (trimmedQuery.substring(0,6).equalsIgnoreCase("CREATE"))
 				return Statements.CREATE;
 		}
 		if (trimmedQuery.length()>=5){
 			if (trimmedQuery.substring(0,5).equalsIgnoreCase("ALTER"))
 				return Statements.ALTER;
 		}
 		if (trimmedQuery.length()>=4){
 			if (trimmedQuery.substring(0,4).equalsIgnoreCase("DROP"))
 				return Statements.DROP;
 		}
 		if (trimmedQuery.length()>=8){
 			if (trimmedQuery.substring(0,8).equalsIgnoreCase("TRUNCATE"))
 				return Statements.TRUNCATE;
 		}
 		if (trimmedQuery.length()>=6){
 			if (trimmedQuery.substring(0,6).equalsIgnoreCase("RENAME"))
 				return Statements.RENAME;
 		}
 		if (trimmedQuery.length()>=2){
 			if (trimmedQuery.substring(0,2).equalsIgnoreCase("DO"))
 				return Statements.DO;
 		}
 		if (trimmedQuery.length()>=7){
 			if (trimmedQuery.substring(0,7).equalsIgnoreCase("REPLACE"))
 				return Statements.REPLACE;
 		}
 		if (trimmedQuery.length()>=4){
 			if (trimmedQuery.substring(0,4).equalsIgnoreCase("LOAD"))
 				return Statements.LOAD;
 		}
 		if (trimmedQuery.length()>=7){
 			if (trimmedQuery.substring(0,7).equalsIgnoreCase("HANDLER"))
 				return Statements.HANDLER;
 		}
 		if (trimmedQuery.length()>=4){
 			if (trimmedQuery.substring(0,4).equalsIgnoreCase("CALL"))
 				return Statements.CALL;
 		}
 		return Statements.SELECT;*/
 	}
 
 	/**
 	 * <b>createTable</b><br>
 	 * <br>
 	 * &nbsp;&nbsp;Creates a table in the database based on a specified query.
 	 * <br>
 	 * <br>
 	 * @param query - the SQL query for creating a table.
 	 * @return the success of the method.
 	 */
 	public abstract boolean createTable(String query);
 
 	/**
 	 * <b>checkTable</b><br>
 	 * <br>
 	 * &nbsp;&nbsp;Checks a table in a database based on the table's name.
 	 * <br>
 	 * <br>
 	 * @param table - name of the table to check.
 	 * @return success of the method.
 	 */
 	public abstract boolean checkTable(String table);
 
 	/**
 	 * <b>wipeTable</b><br>
 	 * <br>
 	 * &nbsp;&nbsp;Wipes a table given its name.
 	 * <br>
 	 * <br>
 	 * @param table - name of the table to wipe.
 	 * @return success of the method.
 	 */
 	public abstract boolean wipeTable(String table);
 }
