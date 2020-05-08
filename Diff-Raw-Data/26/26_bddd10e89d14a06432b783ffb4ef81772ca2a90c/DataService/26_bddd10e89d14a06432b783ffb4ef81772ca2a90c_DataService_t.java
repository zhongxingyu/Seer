 package com.eventu.webtier;
 
 import geohash.GeoHash;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 
 public class DataService {
 
 	Connection conn;
 	String dbName;
 	
 	public DataService(String dbName){
 		
 		
 		//TODO		
         Properties connectionProps = new Properties();
         connectionProps.put("user", "root");
         connectionProps.put("password", "mypass");
 
         try {
         	conn =
 			      DriverManager.getConnection("jdbc:" + "mysql" + "://" + "localhost" +
 			                                  ":" + "3306" + "/" + dbName,
 			                                  connectionProps);
 	        
 		} catch (SQLException e) {
 			e.printStackTrace();
 	
 		}
 	}
 
 	public boolean userEmailExist(String uEmail) {
 		
 		try {
 			//TODO fix connection, make it object
 			Statement stmt = conn.createStatement();
 			String query = "SELECT * FROM UserAccount WHERE userEmail = '" + uEmail + "'";
 			ResultSet ret = stmt.executeQuery(query);
 
 			if (ret.next()) 
 				return true;
 			else
 				return false;
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return true;
 		
 	}
 
 	public int registerUser(String uEmail, String uPass) {
 		
 		System.out.println("HERE!!!!!");
 		try {
 			Statement stmt = conn.createStatement();
			String query = "INSERT INTO UserInfo (userEmail ) VALUES ('" + uEmail + "')";
			int userID = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
 
			query = "INSERT INTO UserAccount (userEmail, userPassword, userID) VALUES ('" + uEmail + "','" +uPass+"','" + userID+ "')";
			stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			
			return userID;
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	public Integer login(String uEmail, String uPass) {
 		
 		try {
 			Statement stmt = conn.createStatement();
 			String query = " SELECT userPassword, userID FROM UserAccount WHERE userEmail='" + uEmail + "'";
 			ResultSet ret = stmt.executeQuery(query);
 
 			if (ret.next()) {
 				String dbPass = ret.getString("userPassword");
 				if(dbPass == uPass)
 					return ret.getInt("userID");
 			}
 
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return -1;
 	}
 
 	public int addFriend(Integer userID, Integer friendID) {
 		
 		// TODO Auto-generated method stub
 		//add to request table
 		//TODO two way invitation conflict 
 		//first check conflict
 		
 		try {
 			Statement stmt = conn.createStatement();
 			
 			//TODO add request
 			/*
 			String query = " SELECT requestID FROM Requests WHERE receiverID='" + userID + "' AND senderID='" + friendId + "'";
 			ResultSet ret = stmt.executeQuery(query);
 
 			if (ret.next()) {
 				//already exist
 				String dbPass = ret.getString("requestID");
 				
 				return ret.getInt("userID");
 			}else{
 				
 			}
 			*/
 			
 			String query = "INSERT INTO UserFriend (userID, friendID) VALUES ('" + userID + "','" + friendID +"'); " +
 					"INSERT INTO UserFriend (userID, friendID) VALUES ('" + friendID + "','" + userID +"') ;";
 			
 			ResultSet ret = stmt.executeQuery(query);
 
 			while (ret.next()) {
 				System.out.println(ret.toString());
 			}
 			
 			return 0;
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return -1;
 	}
 
 	public static int updateLocation(Integer userID, Long latitude,
 			Long longitude, GeoHash geoHash) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public static int removeFriend(Integer userID, Integer friendId) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public int createEvent(Integer userID, String eventName,
 			int eventPremission, String eventTime, Long latitude,
 			Long longitude, GeoHash geoHash) {
 		
 		
 		try {
 			Statement stmt = conn.createStatement();
 			String query = "INSERT INTO EventInfo (eventName, time, public, lat, long, geohash) VALUES " +
 					"('" + eventName + "','" +eventTime+"','" + eventPremission + "','" +latitude+"','" + longitude +"','"+ geoHash.toBase32()+"')";
 			int ret = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
 			
 			query = "INSERT INTO EventUser (eventID, userID, accessibility) VALUES" +
 					"('" + ret + "','" +userID+"','" + eventPremission + "', 0 )";
 			ResultSet ret2 = stmt.executeQuery(query);
 			
 			return 0;//TODO
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return -1;
 		
 	}
 
 	
 	
 }
