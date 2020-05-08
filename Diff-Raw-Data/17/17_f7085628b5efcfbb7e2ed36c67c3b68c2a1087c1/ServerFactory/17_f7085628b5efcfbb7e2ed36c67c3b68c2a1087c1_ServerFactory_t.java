 package server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.Properties;
 import java.sql.PreparedStatement;
 
 import db.Appointment;
 import db.MeetingPoint;
 import db.User;
 
 
 public class ServerFactory {
 	// Database connection
 	DBConnection db;
 	Properties p;
 	
 	public ServerFactory() {
 		p = new Properties();
 		try {
 			p.load(new FileInputStream(new File("resources/database.properties")));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		try {
 			db = new DBConnection(p);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}	
 	}
 	public static void main(String args[]) {
 		ServerFactory sf = new ServerFactory();
 		System.out.println("created serverFactory");
 		
		User u = new User(1, "espen", "master@commander.net", "hunter2");
		ArrayList<Appointment> al = sf.getAllAppointments(u);
 		/*Appointment a = new Appointment(10, 1, "title", new GregorianCalendar(), new GregorianCalendar(), "first test meeting", true);
 		a.setMeetingPoint(new MeetingPoint(1, "mordi", 200));
 		a.addParticipant(new User(1, "espen", "master@commander.net", "hunter2"));
 		a.addParticipant(new User(8, "aleksander", "email", "passord"));
 		boolean result = sf.deleteAppointment(a);
 		//System.out.println(a.getMeetingPoint().getId());
 		//User result = sf.login(new User("aleksander", "email", "passord"));
 	//	Appointment result = sf.insertAppointment(a);
 //		String result = sf.login(new User("aleksander", "email", "passord"));
 		System.out.println(result);
 //		System.out.println(result.get(1).getParticipants());
 //		System.out.println(result.get(1).getPlace()); */
 	}
 	
 	public User login(User u) {
 		PreparedStatement prest;
 		User result = null;
 		try {
 			System.out.println("preparing to check user");
 			//System.out.println(p);
 			db.initialize();
 			
 			prest = db.preparedStatement("SELECT * FROM sids.user WHERE sids.user.name = ? AND sids.user.hashedPassword = ? LIMIT 1;");
 			prest.setString(1, u.getName());
 			prest.setString(2, u.getPassword());
 			
 			ResultSet rs = prest.executeQuery();
 			
 			while (rs.next()) {
 				result = new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("hashedPassword"));
 			}
 			System.out.println("found user " + result.getName());
 				
 			db.close();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("something fucked up");
 		}
 		return result;
 	}
 	
 	public User createUser(String name, String password, String email) throws ClassNotFoundException, SQLException {
 
 		PreparedStatement prest;
 		ResultSet generatedKeys;
 		int userId = 0;
 		
 		db.initialize();
 		
 		prest = db.preparedStatement("INSERT INTO sids.user (name, hashedPassword, email) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
 		prest.setString(1, name);
 		prest.setString(2, password);
 		prest.setString(3, email);
 		prest.executeUpdate();
 		
 		generatedKeys = prest.getGeneratedKeys();
 		if (generatedKeys.next()) {
 			userId = generatedKeys.getInt(1);
 		}
 		
 		db.close();
 		
 		System.out.println("finished!");
 		
 		if (userId != 0) {
 			return new User((int)userId, name, password, email);
 		} else {
 			return null;
 		}
 		
 	}
 	
 	public ArrayList<User> getAllUsers() {
 		PreparedStatement prest;
 		ResultSet users;
 		ArrayList<User> results = new ArrayList<User>();
 		User temp;
 		try {
 			System.out.println("preparing to get users");
 			db.initialize();
 			prest = db.preparedStatement("Select * FROM sids.user");
 			users = prest.executeQuery();
 			
 			while (users.next()) {
 				temp = new User(users.getInt("id"), users.getString("name"), users.getString("email"), users.getString("hashedPassword"));
 				results.add(temp);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("fucked up while getting users");
 		}
 		return results;
 	}
 
 	public ArrayList<Appointment> getAllAppointments(User u) {
 		PreparedStatement prest;
 		ResultSet apps;
 		ArrayList<Appointment> results = new ArrayList<Appointment>();
 		try {
 			System.out.println("preparing to check user");
 			//send query to db
 			db.initialize();
			//prest = db.preparedStatement("SELECT * FROM sids.appointment, sids.user_appointment,sids.user WHERE sids.user.id =?;");
			prest = db.preparedStatement(
					"SELECT a.id, a.creatorUserId, a.start, a.end, a.description, a.isMeeting " +
					"FROM sids.user AS u, sids.user_appointment AS ua, sids.appointment AS a " +
					"WHERE u.id = ua.userId " +
					"AND ua.appointmentId = a.id " +
					"AND u.id = ?"
					);
 			prest.setInt(1, u.getId());
 			System.out.println(prest);
 			//returns query
 			apps = prest.executeQuery();
 			GregorianCalendar start;
 			GregorianCalendar end;
 			//makes query to a appointment object
 			while (apps.next()) {
 				start = new GregorianCalendar();
 				start.setTime(apps.getTimestamp("start"));
 				end = new GregorianCalendar();
 				end.setTime(apps.getTimestamp("end"));
				Appointment temp = new Appointment(apps.getInt("id"), apps.getInt("creatorUserId"), apps.getString("description"), start, end, apps.getString("description"), apps.getBoolean("isMeeting"));
 				if(temp.isMeeting()){
 					temp.setParticipants(getParticipants(temp));
 					temp.setMeetingPoint(getMeetingPoint(temp));
 				}
 				results.add(temp);
 			}
 			db.close();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("something fucked up while getting appointments");
 		}
 		return results;
 	}
 	
 	private MeetingPoint getMeetingPoint(Appointment app) throws ClassNotFoundException, SQLException {
 		DBConnection conn = new DBConnection(p);
 		PreparedStatement prest;
 		ResultSet mPoint;
 		MeetingPoint results=null;
 		try {
 			System.out.println("preparing to check appointment for the meeting place");
 			//send query to db
 			conn.initialize();
 			prest = conn.preparedStatement("SELECT meetingpoint.id,name,capacity FROM " +
 					"((sids.appointment JOIN sids.appointment_meetingpoint ON appointment.id=?)JOIN sids.meetingpoint ON meetingpoint.id=meetingpointId);");
 			prest.setInt(1, app.getId());
 			System.out.println(prest);
 			//returns query
 			mPoint = prest.executeQuery();
 			//makes query to a MeetingPoint object
 			while (mPoint.next()) {
 				results = new MeetingPoint(mPoint.getInt("id"), mPoint.getString("name"), mPoint.getInt("capacity"));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("something fucked up while getting meeting place");
 		}
 		System.out.println(results);
 		conn.close();
 		return results;
 	}
 	private ArrayList<User> getParticipants(Appointment app) throws ClassNotFoundException, SQLException{
 		DBConnection conn = new DBConnection(p);
 		PreparedStatement prest;
 		ResultSet ppants;
 		ArrayList<User> results = new ArrayList<User>();
 		try {
 			System.out.println("preparing to check appointment for participants");
 			//send query to db
 			conn.initialize();
 			prest = conn.preparedStatement("SELECT user.id, name, email, hashedPassword " +
 					"FROM ((sids.appointment JOIN sids.user_appointment ON appointment.id=?)JOIN sids.user ON user.id=userId);");
 			prest.setInt(1, app.getId());
 			System.out.println(prest);
 			//returns query
 			ppants = prest.executeQuery();
 			//makes query to a list of User
 			while (ppants.next()) {
 				results.add(new User(ppants.getInt("id"), ppants.getString("name"), ppants.getString("email"), ppants.getString("hashedPassword")));
 				}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("something fucked up while getting participants");
 		}
 		System.out.println(results);
 		conn.close();
 		return results;		
 	}
 	
 	public Appointment insertAppointment(Appointment appointment) {
 		PreparedStatement prest;
 		ResultSet generatedKeys;
 		int appointmentId = 0;
 		try {
 			db.initialize();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		try {
 			// create and insert new appointment into database
 			prest = db.preparedStatement("INSERT INTO sids.appointment (creatorUserId, title, start, end, description, isMeeting)"+
 			" VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
 			prest.setInt(1, appointment.getId());
 			prest.setString(2, appointment.getTitle());
 			prest.setTimestamp(3, new Timestamp(appointment.getStart().getTimeInMillis()));
 			prest.setTimestamp(4, new Timestamp(appointment.getEnd().getTimeInMillis()));
 			prest.setString(5, appointment.getDescription());
 			prest.setBoolean(6, appointment.isMeeting());
 			System.out.println("executing appointment insert");
 			prest.executeUpdate();
 			
 			generatedKeys = prest.getGeneratedKeys();
 			if (generatedKeys.next()) { // if successfully inserted
 				appointmentId = generatedKeys.getInt(1);
 				
 				// add appointment to creator
 				prest = db.preparedStatement("INSERT INTO sids.user_appointment (userId, appointmentId) VALUES (?, ?)");
 				prest.setInt(1, appointment.getCreatorUserId());
 				prest.setInt(2, appointmentId);
 				prest.executeUpdate();
 				
 				if (appointment.isMeeting()) { 
 					prest = db.preparedStatement("INSERT INTO sids.user_appointment (userId, appointmentId) VALUES (?, ?)");
 					ArrayList<User> participants = appointment.getParticipants();
 					
 					// create user-appointment connection for all users
 					for (int i = 0; i < participants.size(); i++) { 
 						System.out.println(" executing userAppointment nr. " + i + " " + participants.get(i).getName());
 						prest.setInt(1, participants.get(i).getId());
 						prest.setInt(2, appointmentId);
 						prest.executeUpdate();
 					}
 					// create appointment - room connection
 					System.out.println("executing appointmentMeetingpoint");
 					prest = db.preparedStatement("INSERT INTO sids.appointment_meetingpoint (appointmentId, meetingpointId) VALUES(?, ?)");
 					prest.setInt(1, appointmentId);
 					prest.setInt(2, appointment.getMeetingPoint().getId());
 					System.out.println("executing appointmentMeetingPoint");
 					prest.executeUpdate();
 					
 				}
 			}
 			
 			System.out.println("finished inserting");
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			db.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		if (appointmentId != 0) {
 			appointment.setId(appointmentId);
 			return appointment;
 		} else {
 			return null;
 		}
 	}
 	
 	public boolean deleteAppointment(Appointment appointment) {
 		
 		PreparedStatement prest;
 		int mPoint;
 	
 		try {		
 			System.out.println("preparing to check delete appointment from user_appointment");
 			//send query to db
 			db.initialize();
 			prest = db.preparedStatement("DELETE FROM sids.user_appointment WHERE ? = appointmentId;");
 			prest.setInt(1, appointment.getId());
 			System.out.println(prest);
 			mPoint = prest.executeUpdate();
 			//returns query
 			System.out.println("preparing to check delete appointment from appointment_meetingpoint");
 			//send query to db
 			prest = db.preparedStatement("DELETE FROM sids.appointment_meetingpoint WHERE ? = appointmentId;");
 			prest.setInt(1, appointment.getId());
 			System.out.println(prest);
 			mPoint = prest.executeUpdate();
 			
 			System.out.println("preparing to check delete appointment");
 			//send query to db
 			prest = db.preparedStatement("DELETE FROM sids.appointment WHERE id = ?;");
 			prest.setInt(1, appointment.getId());
 			System.out.println(prest);
 			//returns query
 			mPoint = prest.executeUpdate();
 			
 			db.close();
 			
 			//makes query for deleting appointments from user_appointment, appointment_meetingpoint and appointment
 	
 		}
 		
 		
 		catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("something fucked up while deleting appointment");
 		}
 		
 		try {
 			db.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return true;
 		
 	}
 	
 	public boolean editAppointment(Appointment appointment) {
 		PreparedStatement prest;
 		ResultSet app;
 		MeetingPoint results=null;
 		try {
 			System.out.println("preparing to edit appointemnt");
 			//send query to db
 			db.initialize();
 			prest = db.preparedStatement("SELECT meetingpoint.id,name,capacity FROM " +
 					"((sids.appointment JOIN sids.appointment_meetingpoint ON appointment.id=?)JOIN sids.meetingpoint ON meetingpoint.id=meetingpointId);");
 			prest.setInt(1, appointment.getId());
 			System.out.println(prest);
 			//returns query
 			prest.executeUpdate();
 			app = prest.executeQuery();
 			//makes query to a MeetingPoint object
 			while (app.next()) {
 				results = new MeetingPoint(app.getInt("id"), app.getString("name"), app.getInt("capacity"));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("something fucked up while getting meeting place");
 		}
 		System.out.println(results);
 		try {
 			db.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return false;
 		
 	}
 	
 	public User logOut(User user) {
 		return user;
 		
 	}
 	//Need method for something with the notifications. Not sure what, though.
 	
 }
