 package net;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Time;
 import java.util.HashMap;
 import java.util.Properties;
 
 import model.Event;
 
 public class DBMethods {
 	
 	private Connection connection = null;
 	private Statement statement = null;
 	
 	public void setConnection(Connection con){
 		connection = con;
 	}
 	
 	public void setStatement(Statement statm){
 		statement = statm;
 	}
 	

 	public Event createEvent(String createdBy, Time startTime, Time endTime, String eventName, 
 			String description, String place, String invitedPersons, String invitedGroups, String roomNr) throws SQLException{
 		
 		statement = connection.createStatement();
 		String sql = "INSERT INTO Event (createdBy_username, startTime, endTime, eventName, " +
 				"description, place, invitedPersons, invitedGroups, roomNr) VALUES ('"+ createdBy 
 				+ "', '" + startTime + "', '" + endTime + "', '" + eventName + "', '" + description + "', '" +
 						place + "', '" + invitedPersons + "', '" + invitedGroups + "', '" + roomNr + "')";
 		statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
 		ResultSet resultSet = statement.getGeneratedKeys();
 		resultSet.beforeFirst();
 		resultSet.next();
 		int eventId = resultSet.getInt(1);
 		if (!invitedPersons.equals("")){
 			for (String person : invitedPersons.split(" ")){
 				updateInvited(person, eventId);
 			}			
 		}
 		if(!invitedGroups.equals("")){
 			for (String g : invitedGroups.split(" ")){
 				String persons = getPersonsFromGroup(Integer.parseInt(g));
 				for (String p : persons.split(" ")){
 					updateInvited(p,eventId);
 				}
 			}
 		}
 		return new Event();
 	}
 	
 	public String getPersonsFromGroup(int groupNr) throws SQLException{
 		statement = connection.createStatement();
 		String sql = "SELECT persons FROM `Group` WHERE groupID = " + groupNr; 
 		ResultSet resultSet = statement.executeQuery(sql);
 		String persons = "";
 		resultSet.beforeFirst();
 		while(resultSet.next()){
 			persons += resultSet.getString("persons") + " ";
 		}	
 		return persons;
 	}
 	
 	public void updateInvited(String username, int eventId) throws SQLException{
 		statement = connection.createStatement();
 		String sql = "INSERT INTO Invited (username, eventId) VALUES ('" + username + "', " + eventId + ")" ;
 		statement.executeUpdate(sql);
 	}
 	
     public void createUser (String username, String email, String name,
     		byte [] salt, byte [] password)  throws Exception{
     	String sql = "INSERT INTO Person (username, password, name, email, salt)"
     			+ " VALUES ('" + username + "', ?, '" + name + "', '" + email + "', ?)";
  
     	PreparedStatement ps = (PreparedStatement) connection.prepareStatement(sql);
     	ps.setBytes(1, salt);
     	ps.setBytes(2, password);
     	ps.executeUpdate();
     	System.out.println("User created...");
     }
     
     public byte[] getStoredHash(String username, String collumnName) throws SQLException{
     	String sql = "SELECT * FROM Person WHERE username = '" + username +"'";
     	ResultSet resultSet = statement.executeQuery(sql);
     	resultSet.next();
     	byte[] hash = resultSet.getBytes(collumnName);
     	return hash;
     }
     

     public Event getEvent(int eventId) throws SQLException{
     	Event event = new Event();
     	String sql = "SELECT * FROM Event WHERE eventId = " + eventId;
     	ResultSet rs = statement.executeQuery(sql);
     	
     	return event;
     }
 }

