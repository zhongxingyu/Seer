 package control;
 
 import java.sql.*;
 
 public class DbDAO {
 
 	private String url="server1.cornipshosting.nl", user="tho5_user", pass="Nlnbx71q", db="tho5_db";
	private int port=3306,rstU;
 	private ResultSet rst;
 	private Connection con;
 	private Statement stmt;
 
 	public DbDAO() {
 		super();
 		
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			this.url = "jdbc:mysql://" + url + ":" + port + "/" + db + "?user="
 					+ user + "&password=" + pass;
 
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void makeConnection() {
 		try {
 			con = DriverManager.getConnection(url);
 			stmt = con.createStatement();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void makeResultSet(String query) {
 		try {
 			rst = stmt.executeQuery(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void makeResultSet(String query, boolean update) {
 		try {
 			if (update)
 				rstU = stmt.executeUpdate(query);
 			else
 				rst = stmt.executeQuery(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void closeConnectRst() {
 		try {
 			rst.close();
 			stmt.close();
 			con.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
