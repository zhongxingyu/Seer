 package start;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class MySQL {
 
 	private Connection con = null;
 	
 	public MySQL () {
 		final String driver = "com.mysql.jdbc.Driver";
 		final String connection = "jdbc:mysql://localhost:3306/sm";
 		final String user = "root";
 		final String password = "";	
 		
 		
 		try {
 			Class.forName(driver);
 			con = DriverManager.getConnection(connection, user, password);
 			
 			execute("CREATE TABLE IF NOT EXISTS stocks (id int NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name tinytext, stockID tinytext, price int, basePrice int, maxPrice int, minPrice int, volatility int)");
 			execute("CREATE TABLE IF NOT EXISTS players (id int NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name tinytext)");
 			
			close();
 		}
 		catch (Exception e) {
			close();
 			e.printStackTrace();
 		}
 	}
 	
 	public ResultSet query (PreparedStatement stmt) {
 		ResultSet rs = null;
 
 		try {
 			rs = stmt.executeQuery();
 		} catch (SQLException e4) {
 			e4.printStackTrace();
 		}
 		
 		return rs;
 	}
 	
 public void execute (PreparedStatement stmt) {
 	
 		try {
 			stmt.execute();
 		} catch (SQLException e4) {
 			e4.printStackTrace();
 		}
 	}
 	
 	public void execute (String s) {
 		
 		PreparedStatement stmt = prepareStatement(s);
 		
 		try {
 			stmt.execute();
 		} catch (SQLException e4) {
 			e4.printStackTrace();
 		}
 	}
 	
 	public void close() {
 		try {
 			con.close();
 		} catch (SQLException e) {
 			System.out.println("A bad call to mysql.close() occurred");	
 			e.printStackTrace();
 		}
 	}
 	
 	public PreparedStatement prepareStatement(String s) {
 		PreparedStatement stmt = null;
 		try {
 			stmt = con.prepareStatement(s);
 		} catch (SQLException e) {
 			System.out.println("An unexpected error occurred while generating a PreparedStatement"); 
 			e.printStackTrace();
 		}
 
 		return stmt;
 	}
 }
