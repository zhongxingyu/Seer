 package net;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 import model.Event;
 import model.Group;
 import model.HaveCalendar;
 import model.Person;
 import model.Room;
 
 public class DBMethods {
 	
 	private Connection connection = null;
 	private Statement statement = null;
 	
 	public void setConnection(Connection con){
 		connection = con;
 	}
 	
 	public void setStatement(Statement statm){
 		statement = statm;
 	}
 	
 	public Event createEvent(String createdBy, Timestamp startTime, Timestamp endTime, String eventName, 
 			String description, String place, String invitedGroups, String roomNr) throws SQLException{
 		
 		statement = connection.createStatement();
 		String sql = "INSERT INTO Event (createdBy_username, startTime, endTime, eventName, " +
 				"description, place, invitedGroups, roomNr) VALUES ('"+ createdBy 
 				+ "', '" + startTime + "', '" + endTime + "', '" + eventName + "', '" + description + "', '" +
 						place + "', '" + invitedGroups + "', '" + roomNr + "')";
 		statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
 		ResultSet resultSet = statement.getGeneratedKeys();
 		resultSet.beforeFirst();
 		resultSet.next();
 		int eventId = resultSet.getInt(1);
 		return new Event(eventId, getPerson(createdBy),startTime,endTime, eventName, description,place,getRoom(roomNr), getInvitedToEvent(eventId));
 	}
 	
 	public void updateEvent(int eventId, Timestamp startTime, Timestamp endTime, String eventName, 
 			String description, String place, String roomNr) throws SQLException{
 		statement = connection.createStatement();
 		String sql = "UPDATE Event SET startTime = '" + startTime + "', endTime = '" + endTime + "', eventName = '" +
 		eventName + "', description = '" + description + "', place = '" + place + "', roomNr = '" + roomNr + "' WHERE eventID = " + eventId;
 		System.out.println(sql);
 		statement.executeUpdate(sql);
 	}
 	
 	public void invitePersons(int eventId, ArrayList<HaveCalendar> invitedPersons) throws SQLException{
 		for (Object o : invitedPersons){
 			if (o instanceof Person){
 				updateInvited(((Person) o).getName(), eventId);	
 			}
 			if (o instanceof Group){
 				for (Object g : invitedPersons){
 					String persons = getPersonsFromGroup(Integer.parseInt(((Group) g).getName()));
 					for (String p : persons.split(" ")){
 						updateInvited(p,eventId);
 					}
 				}
 			}
 		}
 	}
 	
 	public void updateInvitedToEvent(int eventId, ArrayList<HaveCalendar> invited) throws SQLException{
 		ArrayList<HaveCalendar> prevInvited = getInvitedToEvent(eventId);
 		for (Object o: prevInvited){
     		if(invited.contains(o)){
     			invited.remove(o);
     		}
     	}
 		for (HaveCalendar s : invited ){
 			updateInvited(s.getName(), eventId);
 		}
 	}
 	
 	public void setSubGroup(int yourGroup, int subGroup) throws SQLException{
 		statement = connection.createStatement();
 		String sql = "UPDATE `Group` SET subGroups =" + subGroup + " WHERE groupID = " + yourGroup;
 		statement.executeUpdate(sql);
 	}
 	
 	public ArrayList<HaveCalendar> getInvitedToEvent(int eventId) throws SQLException{
 		statement = connection.createStatement();
 		String sql = "SELECT * FROM Invited WHERE eventID = " + eventId;
 		ResultSet resultSet = statement.executeQuery(sql);
 		ArrayList<HaveCalendar> invitedPersons = new ArrayList<HaveCalendar>();
 		while(resultSet.next()){
 			invitedPersons.add(getPerson(resultSet.getString(1)));
 		}
 		return invitedPersons;
 	}
 	
 	public ArrayList<String> getIsGoing(int eventID) throws SQLException{
 		statement = connection.createStatement();
 		ArrayList<String> isGoing = new ArrayList<String>();
 		String sql = "SELECT username FROM Invited WHERE eventID = " + eventID + " AND isGoing = 1";
 		ResultSet resultSet = statement.executeQuery(sql);
 		while(resultSet.next()){
 			isGoing.add(resultSet.getString(1));
 		}	
 		return isGoing;
 	}
 	
 	public ArrayList<String> getIsNotGoing(int eventID) throws SQLException{
 		statement = connection.createStatement();
 		String sql = "SELECT username FROM Invited WHERE eventID  = " + eventID + " AND isGoing = 0";
 		ArrayList<String> isNotGoing = new ArrayList<String>();
 		ResultSet resultSet = statement.executeQuery(sql);
 		while(resultSet.next()){
 			isNotGoing.add(resultSet.getString(1));
 		}
 		return isNotGoing;
 	}
 	
 	public ArrayList<String> getHasNotReplied(int eventID) throws SQLException{
 		statement = connection.createStatement();
 		String sql = "SELECT username FROM Invited WHERE eventID = " + eventID + " AND isGoing IS NULL";
 		ArrayList<String> hasNotReplied = new ArrayList<String>(); 
 		ResultSet resultSet = statement.executeQuery(sql);
 		while(resultSet.next()){
 			hasNotReplied.add(resultSet.getString(1));
 		}
 		return hasNotReplied;
 	}
 	
 //	public void inviteGroup(String invitedGroups, int eventId) throws NumberFormatException, SQLException{
 //		for (String g : invitedGroups.split(" ")){
 //			String persons = getPersonsFromGroup(Integer.parseInt(g));
 //			for (String p : persons.split(" ")){
 //				updateInvited(p,eventId);
 //			}
 //		}
 //	}
 	
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
 	
 	private void updateInvited(String username, int eventId) throws SQLException{
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
     	statement = connection.createStatement();
     	String sql = "SELECT * FROM Event WHERE eventID = " + eventId;
     	ResultSet resultSet = statement.executeQuery(sql);
     	resultSet.beforeFirst();
     	resultSet.next();
     	int id = resultSet.getInt(1);
     	String createdBy = resultSet.getString(2);
     	Timestamp start = resultSet.getTimestamp(3);
     	Timestamp end = resultSet.getTimestamp(4);
     	String eventName = resultSet.getString(5);
     	String description = resultSet.getString(6);
     	String place = resultSet.getString(7); 
     	String roomNr = resultSet.getString(9);
     	System.out.println("Trying to get event created by: " + createdBy);
     	return new Event(id, getPerson(createdBy), start, end, eventName, description, place, getRoom(roomNr), null);
     }
  
     public void answerInvite(String username, int eventId, int isGoing) throws SQLException{
     	statement = connection.createStatement();
     	String sql = "UPDATE Invited SET isGoing =" + isGoing + " WHERE username = '" + username + "' "
     			+ "AND eventID = " + eventId;
     	statement.executeUpdate(sql);
     }
     
     public boolean isExcistingUser(String username) throws SQLException{
     	statement = connection.createStatement();
     	String sql = "SELECT COUNT(*) FROM Person WHERE username = '" + username + "'";
     	ResultSet resultSet = statement.executeQuery(sql);
     	resultSet.next();
     	return resultSet.getInt(1) == 0 ? false : true;
 		
     }
     
     public void setAlarm(int eventId, String username, Timestamp time) throws SQLException{
     	statement = connection.createStatement();
     	String sql = "INSERT INTO Alarm (time, eventId, username) VALUES ('" + time + "'," + eventId + ", '" + username + "')";
     	statement.executeUpdate(sql); 
     }
     
     public HashMap<String, Timestamp> getAlarms() throws SQLException {
 		
     	HashMap<String, Timestamp> map = new HashMap<String, Timestamp>();
     	statement = connection.createStatement();
     	
     	String sql = "SELECT time, username FROM Alarm;";
     	ResultSet  rs = statement.executeQuery(sql);
     	
     	while(rs.next()) {
     		map.put(rs.getString("username"), rs.getTimestamp("time"));
     	}
     	
     	return map;
     }
     
     
     public ArrayList<Event> loadEvents(String username) throws SQLException{
     	statement  = connection.createStatement();
     	String sql = "SELECT * FROM Event WHERE createdBy_username = '" + username + "'";
     	ResultSet resultSet = statement.executeQuery(sql);
     	ArrayList<Event> events = new ArrayList<Event>();
     	while(resultSet.next()){
     		int eventId = resultSet.getInt(1);
     		String createdBy = resultSet.getString(2);
     		Timestamp startTime = resultSet.getTimestamp(3);
     		Timestamp endTime = resultSet.getTimestamp(4);
     		String eventName = resultSet.getString(5);
     		String description = resultSet.getString(6);
     		String place = resultSet.getString(7);
     		String roomNr = resultSet.getString(9);
     		Event e = new Event(eventId,getPerson(createdBy),startTime,endTime,eventName,
     				description,place, getRoom(roomNr), null);
     		e.setIsGoing(getIsGoing(eventId));
     		e.setIsNotGoing(getIsNotGoing(eventId));
     		e.setHasNotReplied(getHasNotReplied(eventId));
     		events.add(e);
     	}
     	String sql2 = "SELECT * FROM Invited WHERE username = '" + username + "'";
     	resultSet = statement.executeQuery(sql2);
     	while(resultSet.next()){
     		int eventId = resultSet.getInt("eventID");
     		events.add(getEvent(eventId));
     	}
     	return events;
     }
     
     public Person getPerson(String username) throws SQLException{
     	statement = connection.createStatement();
     	String sql = "SELECT * FROM Person WHERE username = '" + username + "'";
     	ResultSet resultSet = statement.executeQuery(sql);
     	resultSet.next();
     	return new Person(resultSet.getString(1), resultSet.getString(4), resultSet.getString(5));
     }
     
     public Room getRoom(String roomNr) throws SQLException{
     	//if (roomNr == null) return null;
     	statement = connection.createStatement();
     	String sql = "SELECT * FROM Room WHERE RoomNr = '" + roomNr + "'";
     	ResultSet resultSet = statement.executeQuery(sql);
     	resultSet.next();
     	return new Room(resultSet.getString(1), resultSet.getInt(2));
     }
     
     public void deleteEvent(int eventId) throws SQLException{
     	statement = connection.createStatement();
     	String sql = "DELETE FROM Event WHERE eventID = " + eventId;
     	statement.executeUpdate(sql);
     }
     
     public ArrayList<Room> getAllRooms() throws SQLException{
     	statement = connection.createStatement();
     	ArrayList<Room> rooms = new ArrayList<Room>();
     	String sql  = "SELECT * FROM Room";
     	ResultSet resultSet = statement.executeQuery(sql);
     	while(resultSet.next()){
     		rooms.add(new Room(resultSet.getString(1), resultSet.getInt(2)));
     	}
     	return rooms;
     }
     
     public HashMap<String, Person> getAllPersons() throws SQLException{
     	statement = connection.createStatement();
     	HashMap<String, Person> persons = new HashMap<String, Person>();
     	String sql = "SELECT * FROM Person";
     	ResultSet resultSet = statement.executeQuery(sql);
     	while(resultSet.next()){
     		persons.put(resultSet.getString(1), getPerson(resultSet.getString(1)));
     	}
 
     	return persons;
     }
     
     public ArrayList<Person> getAllPersonsList() throws SQLException{
     	statement = connection.createStatement();
     	ArrayList<Person> persons = new ArrayList<Person>();
     	String sql = "SELECT * FROM Person";
     	ResultSet resultSet = statement.executeQuery(sql);
     	while(resultSet.next()){
     		persons.add(new Person(resultSet.getString(1), resultSet.getString(4), resultSet.getString(5)));
     	}
     	return persons;
     }
     
     public ArrayList<Group> getAllGroups() throws SQLException{
     	statement = connection.createStatement();
     	String sql = "SELECT * FROM `Group`";
     	ArrayList<Group> groups = new ArrayList<Group>();
     	ResultSet resultSet = statement.executeQuery(sql);
     	while(resultSet.next()){
     		Group g = new Group();
     		g.setName(resultSet.getString(4));
     		g.setGroupId(resultSet.getInt(1));
     		ArrayList<Person> p = new ArrayList<Person>();
     		for (String s : resultSet.getString(2).split(" ")){
     			System.out.println(s);
     			p.add(getPerson(s));
     		}
     		g.setMembers(p);
     		groups.add(g);
     	}
     	return groups;
     }
     
     public ArrayList<HaveCalendar> getAllInvitable() throws SQLException{
     	ArrayList<HaveCalendar> invitable = new ArrayList<HaveCalendar>();
     	for (HaveCalendar hc : getAllGroups()){
     		invitable.add(hc);
     	}
     	for (HaveCalendar h : getAllPersonsList()){
     		invitable.add(h);
     	}
     	return invitable;
     }
     
     public HashMap<Integer, Integer> getNotifications(String username) throws SQLException{
     	statement = connection.createStatement();
    	String sql = "SELECT * FROM Notifications WHERE username = '" + username + "'";
     	//<eventId, notificationType>: 0 = Delete, 1 = Update, 2 = Invite
     	HashMap<Integer,Integer> result = new HashMap<Integer, Integer>();
     	ResultSet resultSet = statement.executeQuery(sql);
     	while (resultSet.next()){
     		result.put(resultSet.getInt(1), resultSet.getInt(3));
     	}
     	return result;
     }
     
     public void setNotification(int eventId, String username, int notification) throws SQLException{
     	statement = connection.createStatement();
     	String sql = "INSERT INTO Notification VALUES (" + eventId + ", '" + username + "', " + notification + ")";
     	statement.executeUpdate(sql);
     }
     
     
 }
