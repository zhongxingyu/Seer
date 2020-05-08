package adg.red.controllers;

 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 public class Database 
 {
 	static String USER_ID = "root";
 	static String PASSWORD = "";
 	static String CONNECT_URL = "jdbc:mysql://localhost/db_MVB"; 
 	
 	public Database() {}
 
 	public static void dbRegister()
 	{
 		try 
 		{
 			// Load the MySQL JDBC driver
 			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			System.exit(-1);
 		}
 	}
 	
 	public static Connection dbConnect(String db_connect_string, 
 			String db_userid, String db_password)
 	{
 		dbRegister();
 		
 		try
 		{
 			Connection conn = DriverManager.getConnection(db_connect_string, db_userid, db_password);
 			System.out.println("db connected");
 			return conn;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
