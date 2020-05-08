 package managers;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 import javafiles.Event;
 
 import resource.ResourceStrings;
 
 public class DBManager {
 
 	private Connection conn;
         private Statement statement;
 	private String host;
 	private String db_userid;
 	private String db_password;
 
 	public DBManager() throws FileNotFoundException, IOException {
         //public String initialise() throws SQLException{
 		host =  "" + ResourceStrings.DB_HOSTNAME;
 		db_userid = "" + ResourceStrings.DB_USERNAME;
 		db_password = "";
 		conn = dbConnect(host, db_userid, db_password);
                 String output = "connected";
                 //return output;
         }
 	//}
 
 	public Connection dbConnect(String db_connect_string, String db_userid,
 			String db_password) {
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/group20", "root", "");
 
 			System.out.println("connected");
 			return conn;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public void addUser(String userName, String firstName, String surName, int admin, String email) {
 		
 		String sqlQuery = "INSERT INTO `group20`.`users`(`username`,`fname`,`lname`,`admin`,`email`)" +
 						  "VALUES('" +
                                                   userName + "','" +
 						  firstName + "','" +
 						  surName + "','" +
 						  admin + "','" +
 						  email + "');";
                 System.out.println(sqlQuery);
 
 		try {
 			statement  = (Statement) conn.createStatement();
 			statement.execute(sqlQuery);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		
 	}
         
         public String getUserEmail(String user){
             	String sqlQuery = "SELECT * FROM users WHERE username='"+user+"';";
                 System.out.println(sqlQuery);
                 String output = "";
 		try {
 			statement  = (Statement) conn.createStatement();
 			ResultSet rs = statement.executeQuery(sqlQuery);
                         while(rs.next())
                         {
                             output += rs.getString("email");
                         }
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return output;
         }
         
         public ArrayList<Event> getEventsForUser(String user)
         {
             ArrayList<Event> events = new ArrayList<Event>();
             
             String sqlQuery = "SELECT * FROM events WHERE invitiees LIKE '%" + user + "%';";
             
 		try {
                         statement = null;
 			statement = (Statement) conn.createStatement();
 			ResultSet rs = statement.executeQuery(sqlQuery);
                         
                         while(rs.next()) {
                             events.add(new Event(rs.getString("name"),
                                                 rs.getString("description"),
                                                 "" + rs.getInt("startdateday") + "/" + rs.getInt("startdatemonth") + "/" + rs.getInt("startdateyear"), 
                                                 "" + rs.getInt("enddateday") + "/" + rs.getInt("enddatemonth") + "/" + rs.getInt("enddatemonth"), 
                                                 rs.getInt("startTime"), 
                                                 rs.getInt("endTime"),
                                                 rs.getString("creator"),
                                                 rs.getString("invitiees"),
                                                 rs.getString("frequency")));
                         }
                         
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
                 
             return events;
         }
         
         public void addEvent(Event event)
         {
             String sqlQuery = "INSERT INTO `group20`.`events`"
                     + "(`name`,`description`,"
                     + "`startDateDay`,`startDateMonth`,`startDateYear`,"
                     + "`endDateDay`,`endDateMonth`,`endDateYear`,"
                     + "`starttime`,`endtime`,`creator`,`frequency`,`invitiees`)"
                     + "VALUES('"
                     + event.getName() + "','"
                     + event.getDescription() + "','"
                     + event.getStartDateDay() + "','"
                     + event.getStartDateMonth() + "','"
                     + event.getStartDateYear() + "','"
                     + event.getEndDateDay() + "','"
                     + event.getEndDateMonth() + "','"
                     + event.getEndDateYear() + "','"
                     + event.getStartTime() + "','"
                     + event.getEndTime() + "','"
                     + event.getCreator() + "','"
                     + event.getFrequency() + "','"
                     + event.getInviteesString()
                     + "');";
             
 		try {
                         statement = null;
 			statement = (Statement) conn.createStatement();
                         statement.execute(sqlQuery);
 
                         
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
         }
         public boolean containsUser(String userid)
         {
             String sqlQuery = "SELECT * FROM users WHERE username='" + userid + "';";
             int rowCount = 0;  
 		try {
                         statement = null;
 			statement = (Statement) conn.createStatement();
 			ResultSet rs = statement.executeQuery(sqlQuery);
                         
                         while ( rs.next() )  
                         {   
                             rowCount++;  
                         } 
                         
                 } catch (SQLException e) {
                         e.printStackTrace();
 		}
                 if(rowCount > 0){return true;}
                 return false;
                 
             
         }
         
         
         
         
 
 }
 
         
