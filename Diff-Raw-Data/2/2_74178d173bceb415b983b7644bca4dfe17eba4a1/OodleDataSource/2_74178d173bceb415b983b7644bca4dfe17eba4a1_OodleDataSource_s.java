 package oodles.DBC;
 
 import java.io.PrintWriter;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.regex.*;
 
 import javax.sql.DataSource;
 
 
 /**
  * <p>
  * A DataSource implementation used to create connections to
  * a remote OodleDB database.
  * </p>
  * 
  * <strong>JDBC DISCLAIMER:</strong>
  * 
  * <p>
  * The packages java.sql and javax.sql are collectively known as 
  * "JDBC"
  * </p>
  * 
  * <p>
  * JDBC's interfaces include many, many methods which provide lots of
  * wonderfully intricate functionality which have been deemed totally
  * unecessary for OodleDBC. These methods will throw SQLExceptions when
  * invoked, and have been marked as "Unsupported"
  * </p>
  * 
  * @author mitch
  *
  */
 public class OodleDataSource implements DataSource {
 	
 	/**
 	 * The database connection string used to connect to the database
 	 */
 	protected String hostName;
 	protected String databaseName;
 	
 	/**
 	 * Constructs a new OodleDataSource that creates connetions
 	 * for a specific OodleDB connection string.
 	 * 
 	 * <p>
 	 * 
 	 * 	<strong>Connection String Format</strong> <br /><br />
 	 * 
 	 * 	<code>oodles:<var>[hostname]</var></code>, or <br />
 	 * 	<code>oodles:<var>[hostname]</var>/<var>[database_name]</var></code> <br /><br/>
 	 * 
 	 *  Where <var>[hostname]</var> matches the regular expression <code>/(\w\d.)+/</code>,
 	 *  and <var>[database_name]</var> matches the regular expression <code>/\w+/</code>.
 	 *
 	 * </p>
 	 * 
 	 * <p>
 	 * 
 	 * 	<strong>Examples</strong> <br />
 	 * 
 	 * 	<code>oodles:localhost</code> connects to the OodleDB server on the current machine. <br />
 	 * 
 	 * 	<code>oodles:localhost/myDatabase</code> connects to the OodleDB server on the current machine,
 	 * 	and uses the database "myDatabase"
 	 * 
 	 * </p> 	
 	 *  
	 * @param dbConnetionString The connection string to use when creating connections.
 	 * 
 	 * @throws IllegalArgumentException if the connection string is invalid.
 	 */
 	public OodleDataSource(String dbConnectionString) throws IllegalArgumentException {
 		
 		/* Validate the connection string */
 		
 		Pattern connectionString = Pattern.compile("oodles:([.\\w\\d]+)(/(\\w+))?");
 		
 		Matcher m = connectionString.matcher(dbConnectionString);
 		
 		if (m.matches() == false) {
 			
 			throw new IllegalArgumentException("Invalid connection string: '" + dbConnectionString + "'.");
 			
 		}
 		
 		/* Parse the connection string */
 		
 		this.hostName = m.group(1);
 		this.databaseName = m.group(3);
 		
 	}
 	
 
 	/**
 	 * Returns a new connection to the datasource.
 	 * 
 	 * @return A new connection to the datasource
 	 * 
 	 * @throws SQLException
 	 */
 	public Connection getConnection() throws SQLException {
 		return new OodleConnection(hostName, databaseName);
 	}
 
 	
 	/*
 	 * And now to make JDBC happy...
 	 */
 	
 		
 	/** Unsupported */
 	public Connection getConnection(String username, String password)
 		throws SQLException {
 		throw new SQLException();
 	}
 
 	
 	/** Unsupported */
 	public PrintWriter getLogWriter() throws SQLException {
 		throw new SQLException();
 	}
 
 	/** Unsupported */
 	public int getLoginTimeout() throws SQLException {
 		throw new SQLException();
 	}
 
 	/** Unsupported */
 	public void setLogWriter(PrintWriter arg0) throws SQLException {
 		throw new SQLException();
 	}
 
 	/** Unsupported */
 	public void setLoginTimeout(int arg0) throws SQLException {
 		throw new SQLException();
 	}
 
 }
