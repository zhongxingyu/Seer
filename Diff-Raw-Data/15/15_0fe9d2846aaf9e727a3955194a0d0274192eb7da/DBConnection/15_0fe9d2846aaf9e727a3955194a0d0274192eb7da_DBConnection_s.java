 package server;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Calendar;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import client.model.TransferableModel;
 
 /**
  * A wrapper around the java sql framework
  * 
  * @author Runar B. Olsen <runar.b.olsen@gmail.com>
  */
 public class DBConnection {
 
 	private static Logger LOGGER = Logger.getLogger("DBConnection");	
 
 	private Connection connection;
 	
 	/**
 	 * Create a DBConnection
 	 * 
 	 * @param p parameters with atleast fp.database.url, fp.database.user and
 	 * 	fp.database.pass
 	 * @throws SQLException on a connection error
 	 */
 	public DBConnection(Properties p) throws SQLException {
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 	 			
 			connection = DriverManager.getConnection(
 				p.getProperty("fp.database.url"),
 				p.getProperty("fp.database.user"),
 				p.getProperty("fp.database.pass")
 			);
 		} catch(ClassNotFoundException e) {
 			LOGGER.severe("Missing MySQL connector");
 			LOGGER.severe(e.toString());
 			System.exit(1);
 		}
 	}
 	
 	/**
 	 * Preforms a query returning the ResultSet
 	 * 
 	 * @param query
 	 * @return
 	 */
 	public ResultSet preformQuery(String query) throws SQLException {
 		Statement st = connection.createStatement();
 		return st.executeQuery(query);
 	}
 	
 	/**
 	 * Execute a update statement, returns the number of affected rows
 	 * 
 	 * @param query
 	 * @return
 	 * @throws SQLException
 	 */
 	public int preformUpdate(String query) throws SQLException {
 		Statement st = connection.createStatement();
 		return st.executeUpdate(query);
 	}
 	
 	/**
 	 * Create a statement
 	 * 
 	 * @param query
 	 * @return
 	 * @throws SQLException
 	 */
 	public Statement createStatement() throws SQLException {
 		return connection.createStatement();
 	}
 	
 	/**
 	 * Close the database connection
 	 * 
 	 */
 	public void close() {
 		try {
 			connection.close();
 		} catch(SQLException e) {
 			// Ignore
 		}
 	}
 	
 	/**
 	 * Format a Calendar for MySQL's DATETIME field
 	 * 
 	 * @param c
 	 * @return
 	 */
 	public static String getFormattedDate(Calendar c) {
 		return String.format(
				"%d-%d-%d %d:%d:%d", 
				c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
 				c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND)				
 		);
 	}
 	
 }
