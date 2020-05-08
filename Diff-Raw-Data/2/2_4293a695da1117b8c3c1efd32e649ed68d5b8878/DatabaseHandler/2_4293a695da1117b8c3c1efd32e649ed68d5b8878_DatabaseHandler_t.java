 package com.NFCGeo;
 import java.sql.*;
 
 import android.app.Activity;
 
 
 public class DatabaseHandler{
 	
 	private static String address;
 	private String username;
 	private String password;
 	private static Connection con;
 	
 	public DatabaseHandler(String url, String un, String pw){
		address = "jdbc:mysql://10.30.74.157:3306/nfcgeo";
 		username = "root";
 		password = "rootpw";
 	}
 	
 	public String isValid(int t) throws SQLException{
 		return Boolean.toString(con.isValid(t));
 	}
 	
 	public void openDB() throws Exception{
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
 			con = DriverManager.getConnection(address, username, password);
 	}
 	
 	public String userAuth(String username) throws SQLException{
 		Statement s = con.createStatement();
 		ResultSet data = s.executeQuery("SELECT * FROM auth WHERE user=\'" + username + "\'");
 		data.first();
 		String temp = null;
 		data.getString("hash");
 		return temp;
 	}
 	
 //	public boolean newUser(String username, String hash) throws SQLException{
 //		Statement s = con.createStatement();
 //		ResultSet data = s.executeQuery("INSERT INTO auth(user, hash) VALUES(/'" + username + "/',/'" + hash + "/')");
 //		if(r.next()) return true;
 //		else return false;
 //	}
 	
 	public String queryColumn(String query, String select) throws SQLException{
 		Statement s = con.createStatement();
 		ResultSet data = s.executeQuery(query);
 		return data.getString(select);
 	}
 	
 	public ResultSet queryTable(String query) throws SQLException{
 		Statement s = con.createStatement();
 		return s.executeQuery(query);
 	}
 	
 	public void addDB(String add) throws SQLException{
 		Statement s = con.createStatement();
 		s.execute(add);
 	}
 	
 	public int deleteDB(String delete) throws SQLException{
 		Statement s = con.createStatement();
 		try {
 			s.execute(delete);
 			return 1;	//Success
 		} catch (SQLException e){
 			return 0;	//Entry is not in the db
 		}
 		
 	}
 }
