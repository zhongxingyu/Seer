 package database;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 import com.sun.org.apache.bcel.internal.Constants;
 
 import core.GlobalConstants;
 
 public class DatabaseQuerys {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Establishes connection to database TODO make this happen in a seperate
 	 * thread -
 	 */
 	public static boolean testConnection(String password) {
 
 		try {
 			Statement stmt;
 
 			// Register the JDBC driver for MySQL.
 			Class.forName(GlobalConstants.DB_MSQLREGISTRY);
 
 			// Define URL of database server for
 			// database named mysql on the localhost
 			// with the default port number 3306.
 			String url = GlobalConstants.DB_URL;
 
 			// Get a connection to the database for a
 			// user named root with a blank password.
 			// This user is the default administrator
 			// having full privileges to do anything.
 			Connection con = DriverManager.getConnection(url,
 					GlobalConstants.DB_USERNAME, HandlePasswords.getPassword());
 
 			// Display URL and connection information
 			System.out.println("URL: " + url);
 			System.out.println("Connection established successfully: " + con);
 
 			// Do nothing in this test app - just check connection
 
 			// Tidy up
 			System.out.println("Closing connection: " + con);
 			con.close();
 		} catch (Exception e) {
 			return false;
 		}
 		return true;
 	}
 
 	/***
 	 * Gets the contents of the specified table. Choose on of the final strings
 	 * in the {@link GlobalConstants} class as the table name
 	 * 
 	 * @param tableName
 	 * @return a results set object
 	 */
 	public synchronized static ResultSet pullTableContents(
 			String tableName) {
 		ResultSet tableContents = null;
 		try {
			
		
 			Statement stmt;
 
 			// Register the JDBC driver for MySQL.
 			Class.forName(GlobalConstants.DB_MSQLREGISTRY);
 
 			// Define URL of database server for
 			// database named mysql on the localhost
 			// with the default port number 3306.
 			String url = GlobalConstants.DB_URL;
 
 			// Get a connection to the database for a
 			// user named root with a blank password.
 			// This user is the default administrator
 			// having full privileges to do anything.
 			Connection con = DriverManager.getConnection(url,
 					GlobalConstants.DB_USERNAME, HandlePasswords.getPassword());
 
 			// Display URL and connection information
 			System.out.println("URL: " + url);
 			System.out.println("Connection established successfully: " + con);
 
 			// try and get the table
 			stmt = con.createStatement();
 			String query = "select all from " + tableName + " ;";
 			tableContents = stmt.executeQuery(query);
 
 			// Tidy up
 			System.out.println("Closing connection: " + con);
 			con.close();
 		} catch (Exception e) {
 			return null;
 		}
 		return tableContents;
 	}
 
 }
