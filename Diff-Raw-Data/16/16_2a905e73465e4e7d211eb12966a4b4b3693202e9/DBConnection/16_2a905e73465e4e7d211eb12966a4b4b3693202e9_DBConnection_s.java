 package models;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class DBConnection {
 	private static String account = "ccs108tjsavage";
 	private static String password = "ieyeikoo";
 	private static String server = "mysql-user-master.stanford.edu";
 	private static String database = "c_cs108_tjsavage";
 	
 	private Connection connection;
 		
 	public DBConnection() {
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			
 			connection = DriverManager.getConnection("jdbc:mysql://" + server, account, password);
 		
 			Statement stmt = connection.createStatement();
 			stmt.executeQuery("USE " + database);
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 				
 	public ResultSet performQuery(String query) {
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet rs =  stmt.executeQuery(query);
 			
 			return rs;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public int insert(String query) {
 		Statement stmt;
 		try {
 			stmt = connection.createStatement();
			stmt.executeUpdate(query);
 			
 			ResultSet generatedKeys = stmt.getGeneratedKeys();
 			if (generatedKeys.next()) {
 				return generatedKeys.getInt(1);
 			} else {
 				throw new SQLException("Didn't get back a key for the insert");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 	
 }
