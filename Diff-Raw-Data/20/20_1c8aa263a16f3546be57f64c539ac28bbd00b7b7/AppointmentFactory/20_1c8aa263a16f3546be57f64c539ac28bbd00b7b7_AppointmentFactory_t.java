 package fellesp;
 
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Time;
 import java.util.Properties;
 
 public class AppointmentFactory {
 	
 
 	private static DBConnection db;
 
 	
 	public AppointmentFactory(Properties properties) throws ClassNotFoundException, SQLException
 	{
 		 db = new DBConnection(properties);
 	}
 	
 
 	public static Appointment createAppointment(Date date, Time startTime, Time endTime, String place,
 			String description, boolean meeting, String owner) throws ClassNotFoundException, SQLException{
 
 		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
 		java.sql.Time sqlStartTime = new java.sql.Time(startTime.getTime());
 		java.sql.Time sqlEndTime = new java.sql.Time(endTime.getTime());
 		
 		int id = getNextId();
 		int meetingInt = 0;
 		if (meeting) {
 			meetingInt = 1;
 		}
 		
 
 		Appointment a= new Appointment(id, date, startTime, endTime, place, description, meeting, owner);
 		String query=String.format("INSERT INTO Appointment " 
 				+ "(appointmentID, date, startTime, endTime, place, description, meeting, owner) VALUES ('" 
 				+ id + "','" + sqlDate + "','" + sqlStartTime +"','" + sqlEndTime +"','" + place + "','" + description + "','" 
 				+ meetingInt + "','" + owner + "');" );
 
 		db.initialize();
 		db.makeSingleUpdate(query);
 		db.close();
 		return a;
 	}
 	
 
 	public static Appointment getAppointment(int id) throws SQLException, ClassNotFoundException{
 		String query = String.format("SELECT * FROM Appointmet WHERE id =" + id + ";" );
 
 		db.initialize();
 		ResultSet rs = db.makeSingleQuery(query);
 		
 		Date date = null;
 		Time startTime = null;
 		Time endTime = null;
 		String place = null;
 		String description = null;
 		boolean meeting = true; // "1" avtale og "2" er mte
 		String owner = null;
 		
 		while (rs.next()){
 			date = rs.getDate(2);
 			startTime = rs.getTime(3);
 			endTime = rs.getTime(4);
 			place = rs.getString(5);
 			description = rs.getString(6);
 			meeting = rs.getBoolean(8);
 			owner = rs.getString(9);
 		}
 		
 		Appointment a= new Appointment(id, date, startTime, endTime, place, description, meeting, owner);
 		db.close();
 		
 		return a;
 	}
 	
 	public static int getNextId() throws ClassNotFoundException, SQLException{
 		String query= "SELECT MAX(appointmentID) FROM Appointment ;";
 
 		db.initialize();
 		ResultSet rs = db.makeSingleQuery(query);
 		int nextId = 0;
 		while( rs.next() ){
 			nextId = rs.getInt(1);
 		}
 		rs.close();
 		db.close();
 		nextId = nextId + 1;
 		
 		return nextId;
 	}
 	
 	public static void deleteAppointment(String owner) throws ClassNotFoundException, SQLException{
 		String query =String.format("DELETE FROM Appintment WHERE owner ='" + owner + "';");
 		db.initialize();
 		db.makeSingleUpdate(query);
 		db.close();
 	}
 	
 	public static void updateAppointmentFromQuery(int id, String query) throws ClassNotFoundException, SQLException{
 		db.initialize();
 		db.makeSingleUpdate(query);
 		db.close();
 	}
 	
 	public static void updateAppointmentDate(int id, Date date) 
 			throws ClassNotFoundException, SQLException{
 		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
 		String query = String.format("UPDATE Appointment SET date ='" + sqlDate + "'WHERE appointmentID = '%d'",id);
 		updateAppointmentFromQuery(id,query);
 	}
 	
 	public static void updateAppointmentStartTime(int id, Time startTime) 
 			throws ClassNotFoundException, SQLException{
 		java.sql.Time sqlStartTime = new java.sql.Time(startTime.getTime());
		String query = String.format("UPDATE Appointment SET startTime ='" + sqlStartTime + "'WHERE appointmentID = '%d'", id);
 		updateAppointmentFromQuery(id,query);
 	}
 	
 	public static void updateAppointmentEndTime(int id, Time endTime) 
 			throws ClassNotFoundException, SQLException{
 		java.sql.Time sqlEndTime = new java.sql.Time(endTime.getTime());
		String query = String.format("UPDATE Appointment SET startTime ='" + sqlEndTime + "'WHERE appointmentID = '%d'", id);
 		updateAppointmentFromQuery(id,query);
 	}
 	
 	public static void updateAppointmentPlace(int id, String place) 
 			throws ClassNotFoundException, SQLException{
 		String query = String.format("UPDATE Appointment SET place = '%s' WHERE appointmentID = '%d'",place,id);
 		updateAppointmentFromQuery(id,query);
 	}
 	
 	public static void updateAppointmentDescription(int id, String description) 
 			throws ClassNotFoundException, SQLException{
 		String query = String.format("UPDATE Appointment SET description = '%s' WHERE appointmentID = '%d'",description,id);
 		updateAppointmentFromQuery(id,query);
 	}
 	
 	public static void updateAppointmentMeeting(int id, boolean meeting) 
 			throws ClassNotFoundException, SQLException{
 		String query = String.format("UPDATE Appointment SET meeting = '%b'  WHERE appointmentID = '%d'",meeting,id);
 		updateAppointmentFromQuery(id,query);
 	}
 	
 	// Har ikke update for owner fordi denne skal ikke kunne endres!
 	
 }
 
