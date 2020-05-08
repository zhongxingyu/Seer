 package Server;
 
 import java.sql.SQLException;
import java.util.ArrayList; 
 import java.util.List;    
 
 import Logic.Appointment;
 import Logic.Date;
 import Logic.Notification;
 import Logic.Room;
 import Logic.User;
import Server.Database;
 
 public class Server {
 	
 	static List<String> commandList;
 	
 	
 	public static void main(String[] args) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
 		commandList = new ArrayList<String>();
 		commandList.add("login");
 		commandList.add("addappointment");
 		commandList.add("delappointment");
 		commandList.add("editappointment");
 		commandList.add("setNotificationRead");
 		
 		
 //		interpretInput("login#Tandberg,123");
 //		interpretInput("addappointment#2012-09-03 08:00,2012-09-03 16:00,Styremte,beskrivelse av mte,Vegard-vegard.holter@gmail.com-vegaholt,F1-200,0");
 //		interpretInput("delappointment#2012-09-03 08:00,2012-09-03 16:00,Styremte,beskrivelse av mte,Vegard-vegard.holter@gmail.com-vegaholt,F1-200,0");
 //		interpretInput("editappointment#2012-09-03 08:00,2012-09-03 16:00,Styremte,beskrivelse av mte,Vegard-vegard.holter@gmail.com-vegaholt,F1-200,0,2012-09-03 15:00,2012-09-03 20:00,Bespisning,Mat,Vegard-vegard.holter@gmail.com-vegaholt,Kjel-200,0");
 		interpretInput("setNotificationRead#ystein Tandberg-tandeey@gmail.com-tandberg,halla");
 	}
 	static void interpretInput(String input) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
 		//Splitter command til string og args til string[]
 		String[] args = input.split("#");
 		String command = args[0];
 		args = args[1].split(",");
 		print(command, args);
 		
 		
 		//Finner metodeindex
 		int commandIndex = 0;
 		for(int i = 0; i < commandList.size(); i++){
 			if(command.equals(commandList.get(i))) commandIndex = i;
 		}
 		
 		switch(commandIndex){
 		case 0 :{ //login
 			Database.login(args[0], args[1]);
 			break;
 		}
 		case 1 :{ //addappointment
 			Date start = Date.toDate(args[0]);
 			Date end = Date.toDate(args[1]);
 			String title = args[2];
 			String description = args[3];
 			User owner = User.toUser(args[4]);
 			Room room = Room.toRoom(args[5]);
 			boolean hidden = Boolean.parseBoolean(args[6]);
 			Appointment appointment = new Appointment(room, start, end, owner, title, description, hidden);
 			
 			Database.addAppointment(appointment);
 			break;
 		}
 		case 2 :{//delappointment#starttime,endtime,title,desctiption,owner,room_id,private
 			Date start = Date.toDate(args[0]);
 			Date end = Date.toDate(args[1]);
 			String title = args[2];
 			String description = args[3];
 			User owner = User.toUser(args[4]);
 			Room room = Room.toRoom(args[5]);
 			boolean hidden = Boolean.parseBoolean(args[6]);
 			Appointment appointment = new Appointment(room, start, end, owner, title, description, hidden);
 			
 			Database.delAppointment(appointment);
 			break;
 		}
 		case 3: {
 			Date OLDstart = Date.toDate(args[0]);
 			Date OLDend = Date.toDate(args[1]);
 			String OLDtitle = args[2];
 			String OLDdescription = args[3];
 			User OLDowner = User.toUser(args[4]);
 			Room OLDroom = Room.toRoom(args[5]);
 			boolean OLDhidden = Boolean.parseBoolean(args[6]);
 			Appointment OLDappointment = new Appointment(OLDroom, OLDstart, OLDend, OLDowner, OLDtitle, OLDdescription, OLDhidden);
 					
 			Date start = Date.toDate(args[7]);
 			Date end = Date.toDate(args[8]);
 			String title = args[9];
 			String description = args[10];
 			User owner = User.toUser(args[11]);
 			Room room = Room.toRoom(args[12]);
 			boolean hidden = Boolean.parseBoolean(args[13]);
 			Appointment appointment = new Appointment(room, start, end, owner, title, description, hidden);
 			
 			Database.editAppointment(OLDappointment, appointment);
 			break;
 		}
 		case 4: {
 			User user = User.toUser(args[0]);
 			String tekst = args[1];
 			Notification notification = new Notification(user, tekst);
 			Database.setNotificationRead(notification, true);
 			break;
 		}
 		}
 	}
 	
 	static void print(String command, String[] args){
 		System.out.println("Command: " + command);
 		System.out.println("Args: ");
 		for(int i = 0; i < args.length; i++){
 			System.out.println(args[i]);
 		}
 	}
 }
