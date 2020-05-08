 package cmput391;
 
 import java.sql.*;
 
 public class UserConnection {
 
 	public static UserConnection userConnection = null;
 	public static final String driverName = "oracle.jdbc.driver.OracleDriver";
 	public static final String dbstring = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
 	private Connection conn = null;
 	private String userName = "";
 	private String userPassword = "";
 
 
 	protected UserConnection() {
 		try {
 			Class drvClass = Class.forName(driverName);
 			DriverManager.registerDriver((Driver) drvClass.newInstance());
 		}
 		catch(Exception ex) {
 			System.out.println("<hr>" + ex.getMessage() + "<hr>");
 		}
		try {
			conn = DriverManager.getConnection(dbstring, userName, userPassword);
			conn.setAutoCommit(false);
		}
		catch(Exception ex) {
			System.out.println("<hr>" + ex.getMessage() + "<hr>");
		}
 	}
 
 	public static UserConnection getConnection() {
 		if(userConnection == null) {
 			userConnection = new UserConnection();
 		}
 		return userConnection;
 	}
 
 	public Connection getConn() {
 		return conn;
 	}
 	
 	public void closeConn() {
 		try {
 			conn.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
