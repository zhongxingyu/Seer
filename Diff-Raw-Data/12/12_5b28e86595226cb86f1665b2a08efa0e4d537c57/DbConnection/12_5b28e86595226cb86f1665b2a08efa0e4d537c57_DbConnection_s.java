 package server;
 
 
 import java.sql.*;//jconnector
 import java.util.ArrayList;
 import model.*;
 
 public class DbConnection {
 
 		private java.sql.Connection connection = null;
 		private Statement statement = null;
 		private PreparedStatement preparedStatement = null;     
         private String url, user, password;
 		
         /**
 		 * DbConnection constructor
 		 * @param url The url for the desired
 		 * @param user Username for the database
 		 * @param password Password for the database
 		 */
         public DbConnection(String url, String user, String password){
         	this.url = url;
         	this.user = user;
         	this.password = password;
         	
         }
         
 
 		public boolean connect ()
 		{
 			try
 			{
 				Class.forName("com.mysql.jdbc.Driver").newInstance();
 				connection = DriverManager.getConnection(url, user, password);
 				statement = connection.createStatement();
 				if(connection != null)
 					return true;
 			} 
 			catch (Exception e) 
 			{
 				System.out.println("Connection failed: " + e.getMessage());
 			}
 			return false; 
 		}
 
         public void setConnection(java.sql.Connection connection){
             this.connection = connection;
         }
 
         public java.sql.Connection getConnection(){
             return this.connection;
         }
       
         
         
         public byte[] getStoredHash(String email, String collumnName)throws Exception
         {
         	String sql = "SELECT * FROM User WHERE email = '" + email +"'";
         	ResultSet resultSet = statement.executeQuery(sql);
         	resultSet.next();
         	byte[] hash = resultSet.getBytes(collumnName);
         	return hash;
         }
         
         /**
          * Closes the connection to the database
          */
         public void closeConnection(){
         	try{
         		if (connection != null) 
         			connection.close();
         	} catch (SQLException e){
         		System.out.println(e.getMessage());
         	}
         }
 
         
         private int getIdFromEmail(String email) throws Exception
         {
  	       String sql = "SELECT * FROM User WHERE Mail = '" + email + "'"; 
  	       ResultSet resultSet = statement.executeQuery(sql);
  	       resultSet.next();
  	       return resultSet.getInt("UserID");
         }
         
 
         //
         //
         //  Collections from database
         //
         //
         
         
         public ArrayList<Alarm> getAlarms(User u) throws SQLException{
         	ArrayList<Alarm> result = new ArrayList<Alarm>();
         	
         	String query = String.format("SELECT id from Alarm where user_id = %s and alarm_time < NOW()", u.getUserId());
         	ResultSet res = statement.executeQuery(query);
         	while( res.next() ){
         		result.add(getAlarm(res.getInt("user_id")));
         	}
         	return result;
         }
 //        
         
         public ArrayList<Event> getCancellations(User u)throws SQLException{
         	return null;
         }
         
         
         public ArrayList<Room> getAvailableRooms(Timestamp start, Timestamp end) throws SQLException{
 
         	String query = String.format("select * from Room where id not in " +
         			"(select roomid from Appointment a where (a.start between '%s' and '%s')  OR (a.end between '%s' and '%s'))",
         			start.toString(), 
         			end.toString(),
         			start.toString(),
         			end.toString());
         	System.out.println(query);
         	ArrayList<Room> result = new ArrayList<Room>();
         	
         	ResultSet res = statement.executeQuery(query);
         	while (res.next()){
         		int id = res.getInt("id");
         		int roomNumber = res.getInt("roomnr");
         		int roomSize = res.getInt("size");
         		String location = res.getString("location");
         		Room room = new Room(id, roomNumber, location, roomSize);
         		result.add(room);
         	}
         	return result;
         }
        
         public ArrayList<Invitation> getInvites(User u) throws SQLException{
         	ArrayList<Integer> ids = new ArrayList<Integer>();
         	ArrayList<Invitation> result = new ArrayList<Invitation>();
         	
         	String query = String.format("SELECT id from Invitation i where user_id = %s", u.getUserId());
         	ResultSet res = statement.executeQuery(query);
         	while( res.next() ){
         		ids.add(res.getInt("id"));
         	}
         	for(Integer i : ids)	
         		result.add(getInvitation(i));
         	return result;
         }
         
 
         public ArrayList<Invitation> getInvitationsByEvent(int eventId) throws SQLException{
         	ArrayList<Invitation> list = new ArrayList<Invitation>();
         	String query = String.format("SELECT id from Invitation where appointment_id = %s", eventId);
         	ResultSet res = statement.executeQuery(query);
         	while( res.next() ){
      		   list.add(getInvitation(res.getInt("id")));
      	   }
      	   return list;
         }
         
         
         public ArrayList<Event> getEventsCreatedByUser(User u) throws SQLException{
         	ArrayList<Integer> ids = new ArrayList<Integer>();
         	ArrayList<Event> list = new ArrayList<Event>();
         	String query = String.format("SELECT id from Appointment where owner = %s AND deleted = 0", u.getUserId());
         	ResultSet res = statement.executeQuery(query);
         	while( res.next() ){
      		   ids.add(res.getInt("id"));
      	   	}
         	for(Integer i : ids)	
         		list.add(getEvent(i));
         	return list;
         }
         
         public ArrayList<User> getUsers() throws SQLException
         {
         	ArrayList<User> list = new ArrayList<User>();
         	String query = String.format("SELECT * from User");
         	ResultSet res = statement.executeQuery(query);
         	while( res.next() )
         	{
         		User user = new User();
         		user.setEmail(res.getString("email"));
      		   	user.setName(res.getString("name"));
      		   	user.setUserId(res.getInt("id"));
      		   	list.add(user);
      	   	}
         	return list;
         }
         
         
         //
         // Single entries from database
         //
         
        public Event getEvent(int eventId) throws SQLException{
     	   Event event = new Event();    	   
     	   String query = String.format("SELECT * from Appointment a where id = %s", eventId);
     	   PreparedStatement stmt = connection.prepareStatement(query);
     	   stmt.setMaxRows(1);
     	   ResultSet res = stmt.executeQuery();
     	   res.next();
     	   
     	   event.setEventId(eventId);
     	   event.setCreatedBy(getUser(res.getInt("owner")));
     	   event.setDescription(res.getString("description"));
     	   event.setStart(Timestamp.valueOf(res.getString("start")));
     	   event.setEnd(Timestamp.valueOf(res.getString("end")));
     	   event.setTitle(res.getString("name"));
     	   event.setRoom(getRoom(res.getInt("roomid")));
     	   return event;
        }
         
        public User getUser(int uid) throws SQLException{
     	   User u = new User();
     	   String query = String.format("SELECT * from User where id = %s", uid);
     	   PreparedStatement stmt = connection.prepareStatement(query);
     	   stmt.setMaxRows(1);
     	   ResultSet res = stmt.executeQuery();
     	   res.next();
     	   
     	   u.setEmail(res.getString("email"));
     	   u.setName(res.getString("name"));
     	   u.setUserId(uid);
     	   return u;
        }
        
        public User getUser(String email) throws SQLException{
     	   User u = new User();
     	   String query = "SELECT * from User where email = ?";
     	   PreparedStatement stmt = connection.prepareStatement(query);
     	   stmt.setMaxRows(1);
     	   stmt.setString(1, email);
     	   ResultSet res = stmt.executeQuery();
     	   res.next();
     	   
     	   u.setEmail(res.getString("email"));
     	   u.setName(res.getString("name"));
     	   u.setUserId(res.getInt("id"));
     	   return u;
        }
        
        public Room getRoom(int rid) throws SQLException{
     	   String query = String.format("SELECT * from Room where id = %s", rid);
     	   PreparedStatement stmt = connection.prepareStatement(query);
     	   stmt.setMaxRows(1);
     	   ResultSet res = stmt.executeQuery();
     	   if(res.next())
     	   {
 	    	   int id = res.getInt("id");
 	    	   int roomNumber = res.getInt("roomnr");
 	    	   int roomSize = res.getInt("size");
 	    	   String location = res.getString("location");
 	    	   Room room = new Room(id, roomNumber, location, roomSize);
 	    	   room.setId(rid);
 	    	   return room;
     	   }
     	   return null;
        }
        
        public int getCancellation(int id){
     	   return 0;
        }
        
        public Invitation getInvitation(int invitationId) throws SQLException{
     	   Invitation inv = new Invitation();
     	   String query = String.format("SELECT * from Invitation where id = %s", invitationId);
     	   PreparedStatement stmt = connection.prepareStatement(query);
     	   stmt.setMaxRows(1);
     	   ResultSet res = stmt.executeQuery();
     	   res.next();
     	   
     	   inv.setId(invitationId);
     	   if(res.getString("alarm") != null)
     		   inv.setAlarm( Timestamp.valueOf(res.getString("alarm")));
     	   inv.setCreated(Timestamp.valueOf(res.getString("created")));
     	   inv.setStatus(InvitationAnswer.valueOf(res.getString("status")));
     	   inv.setTo(getUser(res.getInt("user_id")));
     	   inv.setEvent(getEvent(res.getInt("appointment_id")));
     	   
     	   return inv;
        }
        
        public Alarm getAlarm(int id) throws SQLException{
     	   Alarm alarm = new Alarm();
     	   String query = String.format("SELECT * from Alarm where id = %s", id);
     	   PreparedStatement stmt = connection.prepareStatement(query);
     	   stmt.setMaxRows(1);
     	   ResultSet res = stmt.executeQuery();
     	   res.next();
     	   
     	   alarm.setEvent(getEvent(res.getInt("appointmentid")));
     	   alarm.setTime(Timestamp.valueOf(res.getString("alarm_time")));
     	   alarm.setUser(getUser(res.getInt("userid")));
     	   return alarm;
        }
        
        
        //
        // Creation methods
        //
        
        public Event createAppointment(Event e) throws SQLException{
     	   String sql = "INSERT INTO Appointment (name, start, end, description, roomid, owner)  VALUES (?, ?, ?, ?, ?, ?)";
     	   PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
     	   // name, start, end, description, roomid, owner
     	   stmt.setString(1, e.getTitle());
     	   stmt.setTimestamp(2, e.getStart());
     	   stmt.setTimestamp(3, e.getEnd());
     	   stmt.setString(4, e.getDescription());
     	   if(e.getRoom() != null)
     		   stmt.setInt(5, e.getRoom().getId());
     	   else
     		   stmt.setNull(5, java.sql.Types.INTEGER);
     	   stmt.setInt(6, e.getCreatedBy().getUserId());
     	   stmt.executeUpdate();
     	   ResultSet res = stmt.getGeneratedKeys();
     	   res.next();
     	   int id = res.getInt(1);
     	   return getEvent(id);
        }
        
        public void createInvitation(Invitation inv) throws SQLException{
     	   String query = "INSERT INTO Invitation (appointment_id, user_id, status) VALUES (?,?,?)";
     	   PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
     	   
     	   // appointmentID, timestamp created, toUser, alarm, status
     	   stmt.setInt(1, inv.getEvent().getEventId());
<<<<<<< HEAD
     	   stmt.setInt(2, inv.getTo().getUserId());
     	   stmt.setString(3, inv.getStatus().toString());
=======
//    	   System.out.println("Event id: " + inv.getEvent().getEventId());
    	   stmt.setInt(2, inv.getTo().getUserId());
    	   stmt.setString(3, inv.getStatus().toString());
//    	   System.out.println("Status: " + inv.getStatus().toString());
>>>>>>> FIXED: UpdateAppointment
     	   
     	   stmt.executeUpdate();
     	   
    	   
    	   
    	   
 //    	   ResultSet res = stmt.getGeneratedKeys();
 //    	   res.next();
 //    	   int id = res.getInt(1);
 //    	   return getInvitation(id);
        }
        
        public Alarm createAlarm (Alarm alarm) throws SQLException{
     	   String query = "INSERT INTO Alarm (appointmentid, userid, alarm_time) VALUES (?,?,?)";
     	   PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
     	   
     	   // appointmentID, UserID, timeOfalarm
     	   stmt.setInt(1, alarm.getEvent().getEventId());
     	   stmt.setInt(2, alarm.getUser().getUserId());
     	   stmt.setTimestamp(3, alarm.getTime());
     	   
     	   int newID = stmt.executeUpdate();
     	   
     	   return getAlarm(newID);
        }
        
        public Room createRoom(Room room) throws SQLException{
     	   String qurey = "INSERT INTO Room	(roomnr, location, size) VALUES (?,?,?)";
     	   PreparedStatement stmt = connection.prepareStatement(qurey, Statement.RETURN_GENERATED_KEYS);
     	   
     	   //RoomNumber, Location, Size
     	   stmt.setInt(1, room.getRoomNumber());
     	   stmt.setString(2, room.getLocation());
     	   stmt.setInt(3, room.getRoomSize());
     	   
     	   int newID = stmt.executeUpdate();
     	   
     	   return getRoom(newID);
     	   
        }
        
        
        /**
         * 
         * @param name The name of the user
         * @param email The email of the user
         * @param farm	The farm of the user
         * @param phonenumber	The phonenumber of the user
         * @param salt The salt that is used on the hashed password
         * @param password The password of the user
         * @throws Exception
         */
        
        public void createUser (User u, byte[] password, byte [] salt)  throws Exception
        {
        	String sql = "INSERT INTO User (email, name, password, pw_hash)  VALUES (?, ?, ?, ?)";
 
        	PreparedStatement ps = connection.prepareStatement(sql);
        	
        	ps.setString(1, u.getEmail());
        	ps.setString(2, u.getName());
        	ps.setBytes(3, password);
        	ps.setBytes(4, salt);
        	ps.executeUpdate();
 
        }
        
        
        //
        // Update methods
        //
        
        
        public void updateInvitations(int eventID, ArrayList<Invitation> updatedList) throws SQLException{
     	   
     	   ArrayList<Invitation> inDatabase = getInvitationsByEvent(eventID);
     	   
     	   for (Invitation updated : updatedList) {
     		   if(!inDatabase.contains(updated))
     			   createInvitation(updated);
     	   }
        }
        
        public void updateAppointment(int eventId, String columnname, String value) throws SQLException{
     	   String query = "UPDATE Appointment SET ? = ? WHERE id = ?";
     	   PreparedStatement stmt = connection.prepareStatement(query);
     	   stmt.setString(1, columnname);
     	   stmt.setString(2, value);
     	   stmt.setInt(3, eventId);
     	   
     	   stmt.executeUpdate();
        }
        
        
       
        // Deletion methods
        //
        
        public void deleteAppointment(int id) throws SQLException{
     	   String sql = "UPDATE Appointment set deleted=1 where id = ?";
     	   PreparedStatement ps = connection.prepareStatement(sql);
     	   ps.setInt(1, id);
     	   ps.executeUpdate();
        }
        
        public void deleteInvitations(int id) throws SQLException{
     	   String query = "DELETE FROM Invitation WHERE appointment_id = ?";
     	   PreparedStatement stmt = connection.prepareStatement(query);
     	   stmt.setInt(1, id);
     	   
     	   stmt.executeUpdate();
        }
        
        
 }
 
 
 
 
 
 
 
 
