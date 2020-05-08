 package Server;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import Logic.Appointment;
 import Logic.Date;
 import Logic.Room;
 import Logic.User;
 import Logic.Notification;
 
 public class Database {
 	private static Connection con;
 	private static String start;
 	private static String end;
 	private static String title;
 	private static String description;
 	private static String user;
 	private static String room_id;
 	private static int privat;
 	public static void connect() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
 		final String url = "jdbc:mysql://mysql.stud.ntnu.no/" +
 		  		"thomagje_gruppe41";
 		  final String username = "thomagje_bruker";
 		  final String password = "Gruppe41";
 		  Class.forName("com.mysql.jdbc.Driver").newInstance();
 		  con = DriverManager.getConnection(url,username,password);
 		  System.out.println("Connected");
 	}
 	public static void close() {
 		try {
 			con.close();
 		} catch (SQLException e) {
 			try {
 				con.close();
 			} catch (SQLException e1) {
 				//Give up
 			}
 		}
 	}
 	public static void addAppointment(Appointment appointment) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
 		connect();
 		setAppointmentVars(appointment);
 		Statement s = con.createStatement();
 		s.executeUpdate("INSERT INTO appointment (start,end,title,description, owner, room_id,private) VALUES ('" + start + "', '" + end + "', '" + title + "', '" + description + "', '" + user + "', '" + room_id + "', " + privat + ")");
 		if(appointment.getAttendies().size() > 0) {
 			ResultSet rs = s.executeQuery("SELECT id FROM appointment WHERE owner='" + user + "' ORDER BY id DESC");
 			rs.first();
 			int id = rs.getInt(1);
 			for(int i = 0; i< appointment.getAttendies().size();i++) {
 				s.executeUpdate("INSERT INTO user_has_appointment (user_username, appointment_id) VALUES ('" + appointment.getAttendies().get(i).getUsername() + "', " + id + ")");
 			}
 		}
 		close();
 	}
 	public static void delAppointment(Appointment appointment) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
 	connect();
 	Statement s = con.createStatement();
 	s.executeUpdate("DELETE FROM appointment WHERE id='" + getAppointmentId(appointment) + "'");
 	close();
 	}
 	private static void setAppointmentVars(Appointment appointment) {
 		start = appointment.getStart().getTimeString();
 		end = appointment.getEnd().getTimeString();
 		title = appointment.getTitle();
 		description = appointment.getDescription();
 		user = appointment.getOwner().getUsername();
 		room_id = appointment.getRoom().getName();
 		if(appointment.isHidden()) {
 			privat = 1;
 		}
 		else {
 			privat = 0;
 		}
 	}
 	private static String getAppointmentId(Appointment appointment) throws SQLException {
 		setAppointmentVars(appointment);
 		Statement s = con.createStatement();
 		ResultSet rs = s.executeQuery("SELECT id FROM appointment WHERE start='" + start + "' AND end='" + end + "' AND title='" + title + "' AND description='" + description + "' AND owner='" + user + "' AND room_id='" + room_id + "' AND private='" + privat + "'");
 		
 		if(rs.next()) {
 			return rs.getString(1);
 		}
 		return null;
 	}
 	public static void editAppointment(Appointment oldAppointment, Appointment newAppointment) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
 		connect();
 		setAppointmentVars(newAppointment);
 		int newPrivate;
 		if(newAppointment.isHidden()) {
 			newPrivate = 1;
 		}
 		else {
 			newPrivate = 0;
 		}
 		Statement s = con.createStatement();
 		s.executeUpdate("UPDATE appointment SET start='" + start + "', end='" + end + "', title='" + title + "', description='" + description + "', owner='" + user + "', room_id='" + room_id + "', private='" + privat + "' WHERE start='" + oldAppointment.getStart().getTimeString() + "' AND end='" + oldAppointment.getEnd().getTimeString() + "' AND title='" + oldAppointment.getTitle() + "' AND description='" + oldAppointment.getDescription() + "' AND owner='" + oldAppointment.getOwner().getUsername() + "' AND room_id='" + oldAppointment.getRoom().getName() + "' AND private='" + newPrivate + "'");
 		close();
 	}
 	public static void addNotification(User user, String notification) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
 		connect();
 		addNotificationStatus(user, notification, 0);
 		close();
 	}
 	public static void setNotificationRead(Notification notification, boolean read) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
 		connect();
 		Statement s = con.createStatement();
 		int readInt;
 		if(read) {
 			readInt = 1;
 		}
 		else {
 			readInt = 0;
 		}
 		s.executeUpdate("DELETE FROM notification WHERE user_username='" + notification.getUser().getUsername() + "' AND text='" + notification.getText() + "'");
 		addNotificationStatus(notification.getUser(), notification.getText(), readInt);
 		close();
 	}
 	private static void addNotificationStatus(User user, String notification, int read) throws SQLException {
 		Statement s = con.createStatement();
 		s.executeUpdate("INSERT INTO notification (`user_username`, `text`, `read`) VALUES ('" + user.getUsername() + "', '" + notification + "', " + read + ")");
 	}
 	public static boolean login(String username, String passwd) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
 		connect();
 		Statement s = con.createStatement();
 		ResultSet rs = s.executeQuery("SELECT * FROM user WHERE username='" + username + "' AND password='" + passwd + "'");
 		if(rs.next()) {
 			close();
 			return true;
 		}
 		else {
 			close();
 			return false;
 		}
 	}
 	public static ArrayList<Appointment> getAppointmentsForUser(String username) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
 		connect();
 		ArrayList<Appointment> output = new ArrayList<Appointment>();
 		Statement s = con.createStatement();
 		ResultSet rs = s.executeQuery("SELECT * FROM appointment, user_has_appointment, room WHERE user_has_appointment.appointment_id=appointment.id AND room.id= appointment.room_id AND user_username='" + username + "'");
 		while(rs.next()) {
 			Appointment a = new Appointment();
 			a.setRoom(new Room(rs.getString("room_id"),rs.getInt("capacity")));
 			a.setStart(Date.toDate(rs.getString("start")));
 			a.setEnd(Date.toDate(rs.getString("end")));
 			a.setOwner(getUser(rs.getString("user_username")));
 			a.setTitle(rs.getString("title"));
 			a.setDescription(rs.getString("description"));
 			if(rs.getString("private").equals("1")) {
 				a.setHidden(true);
 			}
 			else {
 				a.setHidden(false);
 			}
 			String id = rs.getString("id");
 			ArrayList<User> al = getUsersByAppointment(id);
 			for(int i = 0;i<al.size();i++) {
 				a.addAttending(al.get(i));
 			}
 			output.add(a);
 		}
 		close();
 		return output;
 	}
 	private static ArrayList<User> getUsersByAppointment(String id) throws SQLException {
 		Statement s = con.createStatement();
 		ResultSet rs = s.executeQuery("SELECT username, name, email FROM user,user_has_appointment WHERE user_username=username AND appointment_id='" + id + "'");
 		ArrayList<User> output = new ArrayList<User>();
 		while(rs.next()) {
 			User user = new User(rs.getString("name"), rs.getString("email"), rs.getString("username"));
			
 			output.add(user);
 		}
 		return output;
 	}
 	private static User getUser(String username) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
 		Statement s = con.createStatement();
 		ResultSet rs = s.executeQuery("SELECT * FROM user WHERE username='" + username + "'");
 		if(rs.next()) {
 			User user = new User(rs.getString("name"), rs.getString("email"),rs.getString("username"));
 			return user;
 		}
 		return null;
 	}
 	public static ArrayList<Notification> getNotifications(String username) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
 		connect();
 		Statement s = con.createStatement();
 		ResultSet rs = s.executeQuery("SELECT * FROM notification WHERE user_username='" + username + "' AND `read`='0'");
 		ArrayList<Notification> output = new ArrayList<Notification>();
 		while(rs.next()) {
 			User user = getUser(rs.getString("user_username"));
 			Notification note = new Notification(user,rs.getString("text"));
 			output.add(note);
 		}
 		close();
 		return output;
 	}
 	public static ArrayList<Appointment> getUnansweredAppointmentsForUser(String username) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
 		connect();
 		ArrayList<Appointment> output = new ArrayList<Appointment>();
 		Statement s = con.createStatement();
 		ResultSet rs = s.executeQuery("SELECT * FROM appointment, user_has_appointment, room WHERE user_has_appointment.appointment_id=appointment.id AND room.id= appointment.room_id AND user_username='" + username + "' AND attending IS NULL");
 		while(rs.next()) {
 			Appointment a = new Appointment();
 			a.setRoom(new Room(rs.getString("room_id"),rs.getInt("capacity")));
 			a.setStart(Date.toDate(rs.getString("start")));
 			a.setEnd(Date.toDate(rs.getString("end")));
 			a.setOwner(getUser(rs.getString("user_username")));
 			a.setTitle(rs.getString("title"));
 			a.setDescription(rs.getString("description"));
 			if(rs.getString("private").equals("1")) {
 				a.setHidden(true);
 			}
 			else {
 				a.setHidden(false);
 			}
 			String id = rs.getString("id");
		
 			ArrayList<User> al = getUsersByAppointment(id);
 			for(int i = 0;i<al.size();i++) {
 				a.addAttending(al.get(i));
 			}
 			output.add(a);
 		}
 		close();
 		return output;
 	}
 	public static ArrayList<User> getAllUsers() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
 		connect();
 		Statement s = con.createStatement();
 		ResultSet rs = s.executeQuery("SELECT * FROM user");
 		ArrayList<User> output = new ArrayList<User>();
 		while(rs.next()) {
 			User user = new User(rs.getString("name"),rs.getString("email"), rs.getString("username"));
 			output.add(user);
 		}
 		close();
 		return output;
 	}
 	public static ArrayList<Room> getAvailableRooms(int capacity,Date starttime, Date endtime) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
 		connect();
 		Statement s = con.createStatement();
 		ResultSet rs = s.executeQuery("SELECT  * FROM room WHERE id NOT IN ( SELECT room.id FROM room, appointment WHERE appointment.room_id = room.id AND start >= '" + starttime.getTimeString() +"' AND end <= '" + endtime.getTimeString() +  "') AND capacity>='" + capacity + "'");
 		ArrayList<Room> output = new ArrayList<Room>();
 		while(rs.next()) {
 			Room room = new Room(rs.getString("id"),rs.getInt("capacity"));
 			output.add(room);
 		}
 		close();
 		return output;
 	}
 	public static void addUser(User user, String password) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
 		connect();
 		Statement s = con.createStatement();
 		s.executeUpdate("INSERT INTO user (username,name,email,password) VALUES ('" + user.getUsername() + "', '" + user.getName() + "', '" + user.getEmail() + "', '" + password + "')");
 		close();
 	}
 	public static void addRoom(Room room) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
 		connect();
 		Statement s = con.createStatement();
 		s.executeUpdate("INSERT INTO room (id,capacity) VALUES ('" + room.getName() + "', '" + room.getCapasity() + "')");
 		close();
 	}
 	
 }
