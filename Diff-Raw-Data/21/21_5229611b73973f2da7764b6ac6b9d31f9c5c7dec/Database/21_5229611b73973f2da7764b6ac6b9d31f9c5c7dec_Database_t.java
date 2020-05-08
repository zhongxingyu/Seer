 
 package Server;
 
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import org.apache.log4j.Logger;
 
 
 
 
 
 
 /** Class provides connection with predefined database and allow inserts data into it.
  * <p>
  * Class is made only for this program.
  * 
  * @author Filip Ogurcak
  * @version 1.0 */
 final public class Database
 {
 
 	private static String connect_string = "jdbc:mysql://localhost/events";
 
 	private static String userid = "events";
 
 	private static String password = "udalosti";
 
 	private static Connection conn = null;
 
 	private static Statement stmt = null;
 
 	private static Logger logger = Logger.getLogger(Database.class.getName());
 
 
 
 
 
 
 	/** Default constructor */
 	public Database()
 	{
 
 	}
 
 
 
 
 
 
 	/** Function to connect database by predefined path, user and password. */
 	static public void connect() {
 
 		try {
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
 
 			conn = DriverManager.getConnection(connect_string, userid, password);
 			stmt = conn.createStatement();
 
 			logger.info("Database is ready.");
 		} catch (SQLException e) {
 			logger.error(e.getMessage() + " :Database connection problem.");
 		} catch (InstantiationException e) {
 			logger.error(e.getMessage() + " :Database connection problem.");
 		} catch (IllegalAccessException e) {
 			logger.error(e.getMessage() + " :Database connection problem.");
 		} catch (ClassNotFoundException e) {
 			logger.error(e.getMessage() + " :Database connection problem.");
 		}
 	}
 
 
 
 
 
 
 	/** Insert data to database with query written as string.
 	 * 
 	 * @param query
 	 *              string with predefined SQL structure
 	 * @throws SQLException
 	 *               If an exception during insert to database occurred */
 	static public void insert(String query) throws SQLException {
 
		if (!stmt.getConnection().isValid(20)) {
 			connect();
 			logger.info("Database reconnected.");
 		}
 		if (stmt != null) {
 			stmt.executeUpdate(query);
 			logger.info("Saved to database.");
 		} else
 			logger.error("Database connection problem.");
 	}
 
 
 
 
 
 
 	/** Insert data to database by prepared statement query.
 	 * 
 	 * @param statement
 	 *              prepared statement
 	 * @throws SQLException
 	 *               If an exception during insert to database occurred */
 	static public void insert(PreparedStatement statement) throws SQLException {
 
		if (!conn.isValid(20)) {
			connect();
			logger.info("Database reconnected.");
		}
 		if (stmt != null) {
 			statement.execute();
 			logger.info("Saved to database.");
 		} else
 			logger.error("Database connection problem.");
 	}
 
 
 
 
 
 
 	/** Return connection to database, or null if connection was't established.
 	 * 
 	 * @return established connection */
 	static public Connection getConnection() {
 
		try {
			if (conn.isValid(20))
				return conn;
			else {
				connect();
				logger.info("Database reconnected.");
				return conn;
			}
		} catch (SQLException e) {
			logger.error("Connection check error: " + e);
		}
		return null;
 	}
 
 
 }
