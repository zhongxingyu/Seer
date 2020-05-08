 package fellesp;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Date;
 import java.sql.SQLException;
 import java.sql.Time;
 import java.util.Properties;
 
 public class AppointmentTest {
 	/**
 	 * @param args
 	 */
 	@SuppressWarnings("deprecation")
 	
 	public static void main(String[] args) {
 		Properties prop = new Properties();
		Date date = new Date(10, 04, 15);
 		Time startTime = new Time(15, 30, 0);
 		Time endTime = new Time(16, 15, 0);
 		String description = "Test Appointment";
 		boolean meeting = false; 
 		String owner = "Foo";
		String place = "p15";
 		
 		try {
 			prop.load(new FileInputStream("./Properties.properties"));
 			AppointmentFactory factory = new AppointmentFactory(prop);
 			// Tester utfres her
			//factory.createAppointment(date, startTime, endTime, place, description, meeting, owner);
			//factory.updateAppointmentDescription(3, "WOHO!");
			factory.updateAppointmentPlace(0, "Pair-A-Dice");
 			
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 
 	}
 
 }
