 import java.sql.*;
 
 public class DBConn {
 	
	public static Connection getConnection() throws Exception
 	{
 		Class.forName("com.mysql.jdbc.Driver"); 
 		String url = "jdbc:mysql://localhost/test"; 
 		String username = "root"; 
 		String pwd = ""; 
 		
 		Connection conn = DriverManager.getConnection(url, username, pwd);
 		return conn; 
 		
 	}
 	
 }
