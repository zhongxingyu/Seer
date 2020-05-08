 package dbhandle;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Properties;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 
 import xmlhandle.Xmlhandle;
 
 import dbconnection.DBConnection;
 
 public class Dbhandle {
 	
 	//This class communicates with the database and returns exactly what the user wants.
 	//Since I don't quite know what will be needed just yet, I have just made a lot of different methods that fetches and adds all kinds
 	//of information.
 	
 	//NOTE: Passwords are not needed when fetching for example the meeting leader. Exclude it to improve security?
 	
 	private DBConnection conn;
 	
 	//Adds a new user event to the database
 	public void addUserEvent(int user_ID, int event_ID) {
 		String update = String.format("INSERT INTO user_event VALUES ('%d','%d')", user_ID, event_ID);
 		this.dbUpdate(update);
 	}
 	
 	//Adds a new meeting leader to the database
 	public void addMeetingLeader(int meeting_ID, int user_ID) {
 		String update = String.format("INSERT INTO meeting_leader VALUES ('%d','%d')", meeting_ID, user_ID);
 		this.dbUpdate(update);
 		
 	}
 	
 	//Adds a new meeting event to the database
 	public void addMeetingEvent(int meeting_ID, int event_ID) {
 		String update = String.format("INSERT INTO meeting_event VALUES ('%d','%d')", meeting_ID, event_ID);
 		this.dbUpdate(update);
 	}
 	
 	//Adds a new event meeting room to the database
 	public void addEventMeetingRoom(int event_ID, int meeting_room_ID) {
 		String update = String.format("INSERT INTO event_meeting_room VALUES ('%d', '%d')", event_ID, meeting_room_ID);
 		this.dbUpdate(update);
 	}
 	
 	//Adds a new user to the database
 	public void addUser(User user) {
 		String update = String.format("INSERT INTO user (username,password,name) VALUES('%s','%s','%s')", 
 				user.getUsername(), user.getPassword(), user.getName());
 		this.dbUpdate(update);
 	}
 	
 	//Adds a new meeting to the database
 	//TODO: How do you add an entity without any values?
 	public int addMeeting(String name) throws SQLException{
 		String update = String.format("INSERT INTO meeting (name) VALUES('%s')", name);
 		this.dbUpdate(update);
 		
 		String query = String.format("SELECT * FROM meeting ORDER BY meeting_ID DESC");
 		ResultSet rs = dbQuery(query);
 		
 		if (rs.next()) {
 			return rs.getInt("meeting_ID");
 		} else {
 			return -1;
 		}
 	}
 	
 	//Adds a new event to the database
 	public int addEvent(Event event) throws SQLException{
 		String update = String.format("INSERT INTO event (start,end,location,description,status) VALUES('%s','%s','%s','%s','%s')", 
 				event.getStart(), event.getEnd(), event.getLocation() ,event.getDescription(), event.getStatus().toString());
 		this.dbUpdate(update);
 		
 		String query = String.format("SELECT * FROM event ORDER BY event_ID DESC");
 		ResultSet rs = dbQuery(query);
 		
 		if (rs.next()) {
 			return rs.getInt("event_ID");
 		} else {
 			return -1;
 		}
 	}
 	
 	//Adds a new meeting room to the database
 	public void addMeetingRoom(int meeting_room_ID, String name) {
 		String update = String.format("INSERT INTO meeting_room VALUES ('%d', '%s')", meeting_room_ID, name);
 		this.dbUpdate(update);
 	}
 	
 	//Fetches the event identified by username from the database 
 	//Note: Do we also need to fetch using a user_ID?
 	public User fetchUser(String username) {
 		
 		int user_ID = 0; String password = null; String name = null;
 		
 		String query = String.format("SELECT * FROM user WHERE username='%s'",username);
 		ResultSet rs = this.dbQuery(query);
 		try {
 			rs.next();
 			user_ID = rs.getInt("user_ID");
 			username = rs.getString("username");
 			password = rs.getString("password");
 			name = rs.getString("name");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		if (user_ID != 0) {
 		return new User(user_ID,username,password,name);
 		} else {
 			System.out.println("Failed to fetch user");
 			return null;
 		}	
 	}
 	
 public User fetchUser(int user_ID) {
 		
 		String username = null; String password = null; String name = null;
 		
 		String query = String.format("SELECT * FROM user WHERE user_ID='%d'",user_ID);
 		ResultSet rs = this.dbQuery(query);
 		try {
 			rs.next();
 			user_ID = rs.getInt("user_ID");
 			username = rs.getString("username");
 			password = rs.getString("password");
 			name = rs.getString("name");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		if (user_ID != 0) {
 		return new User(user_ID,username,password,name);
 		} else {
 			System.out.println("Failed to fetch user");
 			return null;
 		}	
 	}
 
 	public List<User> fetchUsersFollowed(int user_ID) throws SQLException {
 		
 		String username = null; String password = null; String name = null;
 		
		String query = String.format("SELECT * FROM (other_user_event JOIN user ON user.user_ID = other_user_event.other_user_ID) WHERE user.user_ID='%d'",user_ID);
 		ResultSet rs = this.dbQuery(query);
 		List<User> userList = new ArrayList<User>();
 		
 		while(rs.next()) {
 			user_ID = rs.getInt(3);
 			username = rs.getString("username");
 			name = rs.getString("name");
 			User user = new User(user_ID,username, password, name);
 			userList.add(user);
 		}
 		
 		return userList;
 		
 	}
 	
 	public List<User> fetchAllUsers() throws SQLException {
 	
 		int user_ID = -1; String username = null; String password = null; String name = null;
 		
 		String query = String.format("SELECT * FROM user");
 		ResultSet rs = this.dbQuery(query);
 		List<User> userList = new ArrayList<User>();
 		
 		while(rs.next()) {
 			user_ID = rs.getInt("user_ID");
 			username = rs.getString("username");
 			name = rs.getString("name");
 			User user = new User(user_ID,username, password, name);
 			userList.add(user);
 		}
 		
 		return userList;
 	}
 	
 	//Fetches the event identified by event_ID from the database
 	public Event fetchEvent(int event_ID) {
 		int id = 0; Timestamp start = null; Timestamp end = null; String location = null; String description = null; Status status = null;
 		
 		String query = String.format("SELECT * FROM event WHERE event_ID='%d'", event_ID);
 		ResultSet rs = this.dbQuery(query);
 		
 		try {
 			rs.next();
 			id = rs.getInt("event_ID");
 			start = Timestamp.valueOf(rs.getString("start"));
 			end = Timestamp.valueOf(rs.getString("end"));
 			location = rs.getString("location");
 			description = rs.getString("description");
 			status = Status.valueOf(rs.getString("status"));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		if (id != 0) {
 		return new Event(id,start,end,location,description,status); 
 		} else {
 			System.out.println("Failed to fetch event");
 			return null;
 		}
 	}
 	
 	//Fetches all the events that belongs to the given username and returns an array containing them
 	public List<Event> fetchUserEvents(String username) throws SQLException {
 		
 		int id = 0; Timestamp start = null; Timestamp end = null; String location = null; String description = null; Status status = null;
 		
 		List<Event> eventList = new ArrayList<Event>();
 		
 		String query = String.format("SELECT * FROM ((user_event JOIN user on user.user_ID = user_event.user_ID)" +
 				" JOIN event ON event.event_ID = user_event.event_ID) WHERE username='%s'",username);
 				
 		ResultSet rs = this.dbQuery(query);
 		
 		while(rs.next()) {
 			id = rs.getInt("event_ID");
 			start = Timestamp.valueOf(rs.getString("start"));
 			end = Timestamp.valueOf(rs.getString("end"));
 			location = rs.getString("location");
 			description = rs.getString("description");
 			status = Status.valueOf(rs.getString("status"));
 			Event event = new Event(id,start,end,location,description,status);
 			eventList.add(event);
 		}
 		
 		return eventList;
 	}
 	
 	//Fetches the meeting leader
 	public User fetchMeetingLeader (int meeting_ID) throws SQLException{
 		int user_ID = 0; String username = null; String password = null; String name = null;
 		
 		String query = String.format("SELECT * FROM (meeting_leader JOIN user ON meeting_leader.user_ID = user.user_ID) WHERE meeting_ID = '%d' ", meeting_ID);
 		
 		ResultSet rs = this.dbQuery(query);
 		
 		if (rs.next()) {
 			user_ID = rs.getInt("user_ID");
 			username = rs.getString("username");
 			password = rs.getString("password");
 			name = rs.getString("name");
 			return new User(user_ID, username, password, name);
 		}
 		
 		return null;
 	}
 	
 	public Event fetchUserEventInMeeting(int user_ID, int meeting_ID) throws SQLException {
 		int id = 0; Timestamp start = null; Timestamp end = null; String location = null; String description = null; Status status = null;
 		
 		String query = String.format("SELECT * FROM ((user_event JOIN event ON user_event.event_ID = event.event_ID)" +
 				" JOIN meeting_event ON event.event_ID = meeting_event.event_ID) WHERE user_ID = '%d' AND meeting_ID = '%d' ", user_ID, meeting_ID);
 		
 		ResultSet rs = this.dbQuery(query);
 		
 		if (rs.next()) {
 			id = rs.getInt("event_ID");
 			start = Timestamp.valueOf(rs.getString("start"));
 			end = Timestamp.valueOf(rs.getString("end"));
 			location = rs.getString("location");
 			description = rs.getString("description");
 			status = Status.valueOf(rs.getString("status"));
 			Event event = new Event(id,start,end,location,description,status);
 			return event;
 		}
 		
 		return null;
 		
 	}
 	
 	//Fetches all the meetings the given user is a leader of
 	public List<Meeting> fetchLeaderOfMeetings (String username) throws SQLException{
 		int id = 0;
 		
 		List<Meeting> meetingList= new ArrayList<Meeting>();
 		
 		String query = String.format("SELECT * FROM (meeting_leader JOIN meeting ON meeting_leader.user_ID = user.user_ID) WHERE username='%s'",username);
 		ResultSet rs = this.dbQuery(query);
 		
 		while(rs.next()) {
 			id = rs.getInt("meeting_ID");
 			Meeting meeting = new Meeting(id);
 			meetingList.add(meeting);
 		}
 		
 		return meetingList;
 		
 	}
 	
 	//Fetches all the events that are linked to a meeting
 	public List<Event> fetchEventsInMeeting (int meeting_ID) {
 		
 		return null;
 	}
 	
 	//Fetches the owner of the event
 	public int fetchEventOwnerID(int event_ID) throws SQLException {
 		
 		String query = String.format("SELECT * FROM user_event WHERE event_ID='%d'",event_ID);
 		ResultSet rs = this.dbQuery(query);
 		
 		if (rs.next()) {
 			return rs.getInt("user_ID");
 		} else {
 			return -1;
 		}
 	}
 	
 	//Fetches the meeting the given event is part of
 	public Meeting fetchMeetingOfEvent (int event_ID) throws SQLException {
 		
 		String query = String.format("SELECT * FROM (meeting_event JOIN meeting ON meeting.meeting_ID = meeting_event.meeting_ID) " +
 				"WHERE event_ID='%d'",event_ID);
 		ResultSet rs = this.dbQuery(query);
 		
 		if (rs.next()) {
 			int id = rs.getInt("meeting_ID");
 			String name = rs.getString("name");
 			Meeting meeting = new Meeting(id,name);
 			return meeting;
 		} else {
 			return null;
 		}
 		
 	}
 	
 	//Fetches the meeting room the event is using. If its not using a meeting room, the method will return null.
 	public MeetingRoom fetchEventsMeetingRoom(int event_ID) {
 		
 		return null;
 	}
 	
 	//Updates the values of a meeting
 	public void updateEvent(Event event) {
 		
 		String update = String.format("UPDATE event SET start='%s',end='%s',location='%s',description='%s',status='%s' WHERE event_ID='%d'",
 				event.getStart(), event.getEnd(), event.getLocation(),event.getDescription(),event.getStatus(),event.getEvent_ID());
 				
 		this.dbUpdate(update);
 	}
 	
 	public void updateNameOfUser(int userID,String newName) {
 		
 		String update = String.format("UPDATE user SET name='%s' WHERE user_ID='%d'",newName,userID);
 		
 		this.dbUpdate(update);
 	}
 	
 	public void updateUserPassword(int userID, String newPassword) {
 		
 		String update = String.format("UPDATE user SET password='%s' WHERE user_ID='%d'",newPassword,userID);
 		
 		this.dbUpdate(update);
 	}
 	
 	//Helper method to perform an update
 	private void dbUpdate(String update) {
 		try {
 			this.conn.makeUpdate(update);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	//Helper method to perform a query
 	private ResultSet dbQuery(String query) {
 		try {
 			return this.conn.makeSingleQuery(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	//constructor that creates a new DBConnection
 	public Dbhandle() {
 		
 		//Loads the properties.properties file, which contains the driver and server info.
 		Properties props=new Properties();
 		try {
 			props.load(new FileInputStream(new File("Properties.properties")));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		conn = null;
 		try {
 			conn = new DBConnection(props);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} 
 		
 	}
 }
 
