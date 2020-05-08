 package driver;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.DatabaseMetaData;
 
 public class DbClass {
//	public String sUrl; // for advertising and debug purposes
 	public String sDbUrl;
 	private String sDriverName = "org.sqlite.JDBC";
 //	private String sDriver;
 	private Connection conn = null;
 	private Statement stmt = null;
	private String sDriver = "jdbc:sqlite"; // moved this here, because as long
 	
 	
 	public DbClass(String sDatabaseToUse_) throws Exception {
 		String sDatabaseToUse = sDatabaseToUse_;
		String sDbUrl = sDriver + ":" + sDatabaseToUse;
 //		sDriverName = getDriverString(sDbUrl); // this function will split the driver string from the Url
 		
 		setConnection();
 	}
 	
 	private void setConnection() throws Exception {
 		try {
 			Class.forName(sDriverName);
 		} catch (Exception e) {
 			// connection failed
 			System.out.println("DriverName: " + sDriver + " was not available");
 			System.err.println(e);
 			throw e;
 		}
 		// create database connection
 		conn = DriverManager.getConnection(sDbUrl);
 		try {
 			stmt = conn.createStatement();
 		} catch (Exception e) {
 			try { conn.close(); } catch (Exception ignore) {}
 			conn = null;
 		}
 	}
 	
 	// to close the database connection in the main program, call this method:
 	public void closeConnection() {
 		if (stmt != null) { try { stmt.close(); } catch (Exception ignore) {} }
 		if (conn != null) { try { conn.close(); } catch (Exception ignore) {} }
 	}
 	
 	public ResultSet executeQuery(String instruction) throws SQLException {
 		return stmt.executeQuery(instruction);
 	}
 	
 	public void execute(String instruction) throws SQLException {
 		stmt.executeUpdate(instruction);
 	}
 }
