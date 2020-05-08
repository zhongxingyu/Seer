 package repository;
 
 /**
  * @author Xuesong Meng&Yidu Liang
  * @author Joanne Zhuo
  */
 import java.io.File;
 import java.io.FileReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Properties;
 
 public class DbManager {
 
 	/**
 	 * Hard-coded database access information
 	 */
 	private static final String SERVER = "localhost:3306";
 	private static final String USER = "root";
 	// Modified by Xuesong Meng, No password.
	//private static final String PASSWORD = "12345";
 	private static final String PASSWORD = "1234";
 	private static final String DATABASE = "clubuml";
 	// as long as you're using mysql, leave this alone.
 	private static final String DATABASE_TYPE = "mysql";
 
 	/* ------------- SQL Variables ------------- */
 	/**
 	 * The SQL connection to the database
 	 */
 	static Connection con;
 
 	/**
 	 * Gets a connection to for the manager and makes a good effort to make sure
 	 * it is open
 	 * 
 	 * @return either an open connection or null
 	 */
 	static synchronized Connection getConnection() {
 		try {
 			if (con != null && con.isClosed()) {
 				con = null;
 			}
 		} catch (SQLException e) {
 			con = null;
 		}
 		connect();
 		return con;
 	}
 
 	/**
 	 * Closes the database connection
 	 */
 	public static void disconnect() {
 		if (con == null) {
 			return;
 		}
 
 		try {
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		con = null;
 	}
 
 	/**
 	 * Utility method to validate connection is valid.
 	 * 
 	 * @return true if DATABASE is available; false otherwise.
 	 */
 	public static boolean isConnected() {
 		if (con == null) {
 			return false;
 		}
 
 		try {
 			return !con.isClosed();
 		} catch (SQLException e) {
 			e.printStackTrace();
 
 			return false;
 		}
 	}
 
 	/**
 	 * Establish a connection
 	 * 
 	 * @return true if success; false if fail
 	 */
 	public static boolean connect() {
 		// already connected.
 		if (con != null) {
 			return true;
 		}
 
 		// Register the JDBC driver for MySQL. Simply accessing the class
 		// will properly initialize everything.
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 		} catch (ClassNotFoundException cnfe) {
 			System.err.println("Unable to locate mySQL drivers");
 			return false;
 		}
 
 		try {
 
 			// Get a connection to the database for a
 			// user with the given user name and password.
 			if (System.getProperty("os.name").equals("Linux") && System.getProperty("user.name").equals("tomcat7")) {
 				// Connection for rho (need to move this to a file. outside of webapps folder for security)
 				// Modified by Xuesong Meng
 				//con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/ClubUML","csye7945", "north7945");
 				con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/clubuml","localhost", "");
 			} else {
 				String url = "jdbc:" + DATABASE_TYPE + "://" + SERVER + "/" + DATABASE;
 				con = DriverManager.getConnection(url, USER, PASSWORD);				
 			}
 			
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 }
