 package no.ntnu.fp.cli;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Scanner;
 
 import no.ntnu.fp.model.Appointment;
 import no.ntnu.fp.model.Meeting;
 import no.ntnu.fp.model.Meetingrequest;
 import no.ntnu.fp.model.Meetingroom;
 import no.ntnu.fp.model.Message;
 import no.ntnu.fp.model.Person;
 import no.ntnu.fp.model.AbstractAppointment;
 import no.ntnu.fp.model.Project;;
 
 public class Klient {
 
 	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 	static Project p = new Project();
 	static Scanner in = new Scanner(System.in);
 
 	static String username;
 	static String password;
 	static String menuspacer = "-----------------------------------";
 
 	static int menuCounter = -1;
 
 	//hovedmetoden som bytter mellom  ikke vre innlogget, og  vise hovedmenyen
 	public static void main(String [] args) throws IOException{
 		while (menuCounter != 0){
 
 			switch (menuCounter){
 
 			case -1: LogIn();
 			break;
 
 			case 1: mainMenu();
 			break;
 
 			default: 
 				System.out.print("Invalid value. Choose a value from 1-x. Type 0 if you want to log out");
 				menuCounter = in.nextInt();
 			break;
 			}
 		}
 	}
 
 	// metode for  logge inn. vil kjres helt til brukernavn/passord er riktig.
 	public static void LogIn() throws IOException{
 		System.out.print("Type username: ");
 		username = br.readLine();
 
 		System.out.print("Enter your password: ");
 		password = br.readLine();
 
 		if (p.login(username, password)){
 			menuCounter = 1;
 		}
 		else{
 			System.out.println("Wrong username / password. Please try again. ");
 		}
 	}
 
 	public static void mainMenu() throws IOException{
 		System.out.println(menuspacer);
 		System.out.println("***MENU***");
 		System.out.println("1.  Show my calendar");
 		System.out.println("2.  Show an other persons calendar"); 
 		System.out.println("3.  Create a new meeting");
 		System.out.println("4.  Show all of your created meetings");
 		System.out.println("5.  Create a new appointment");
 		System.out.println("6.  Show all of your created appointments");
 		System.out.println("7.  Show meeting requests. ( " + p.CountNewRequests() + " new)");
 		System.out.println("8.  Show messages (" + p.countMessages() + " new)");
 		System.out.println("0.  Log out");
 
 
 		System.out.print("\nChoose a value from the menu: ");
 
 		menuCounter = in.nextInt(); 
 
 		Meeting m;
 
 		switch (menuCounter){
 			// viser egen kalender
 			case 1: calendarView(null);break;
 
 			// viser en annen valgt person sin kalender
 			case 2: 
 				System.out.println("Choose one of the following persons"); 
 				for (int j = 0; j < p.getPersonList().size(); j++){
 					System.out.println(j + ".  " + p.getPersonList().get(j));
 				}
 				int i = in.nextInt();
 				calendarView(p.getPersonList().get(i));
 			break;
 
 			// oppretter et mte
 			case 3: createMeeting();break;
 
 			// viser alle mter som bruker har opprette
 			case 4: showAllCreatedMeetings();break;
 
 		// oppretter en avtale
 		case 5: createAppointment();break;
 
 
 		case 6: showAllCreatedAppointments();break;
 
 
 		case 7: showMeetingRequests();break;
 		case 8: showMessages(); break;
 
 		case 0: 
 			p.logout();
 			menuCounter = -1;
 		break;
 
 		default: 
 			System.out.println("You have not chosen a vaild number. Please try again.");
 			menuCounter = in.nextInt();
 		break;
 
 		}
 		if(menuCounter != -1) menuCounter = 1;
 	}
 	
 
 	
 	/**
 	 * 
 	 * @param p: null er sin egen kalender
 	 */
 	private static void calendarView(Person pers) {
 		String[] dagnavn = {"Sunday","Monday", "Tuesday", "Wednesday", "Thursday", "Friday","Saturday"};
 		int uke = 1;
 		while(uke!=-1) {
 			System.out.println("The week calendar for " +((pers == null) ? ("you") : (pers.getName())) + " " + ((uke == 1) ? ("this week") : (uke - 1 + " week(s) from now")) );
 			System.out.println("From\tTo\tType\t\tPlace\t\tDescription");
 			ArrayList<AbstractAppointment> liste = p.showCalendar(pers, uke);
 			Iterator itr = liste.iterator();
 			int dag = 0;
 			while(itr.hasNext()) {
 				
 				AbstractAppointment a = (AbstractAppointment) itr.next();
 				if(a.getStartTime().getDay()!= dag){ // Printer ut dagen
 					dag = a.getStartTime().getDay();
 					System.out.println("--- " + dagnavn[dag] +  " ---");
 				}
 			
 				System.out.print(a.getStartTime().getHours() + ":" + ((a.getStartTime().getMinutes() == 0)? ("00") : a.getStartTime().getMinutes()));
 				System.out.print("\t" + a.getEndTime().getHours() + ":" + ((a.getEndTime().getMinutes() == 0) ? ("00") : a.getEndTime().getMinutes() ));
 				if (a instanceof Appointment){
 					Appointment ap = (Appointment)a;
 					System.out.print("\tAppointment");
 					System.out.print("\t" + ap.getPlace());
 				}
 				else{
 					Meeting m = (Meeting) a;
 					System.out.print("\tMeeting\t");
 					System.out.print("\t" + ((m.getMeetingRoom() != null) ? m.getMeetingRoom().getName() : "Uten rom"));
 				}
 				System.out.println("\t" + a.getDescription());
 			}
 			System.out.println(menuspacer);
 			if (uke != 1) System.out.println("1.\tLast Week");
 			System.out.println("2.\tNext Week");
 			System.out.println("0.\tBack to main menu");
 			System.out.print("Valg: ");
 			int valg = in.nextInt();
 			if(valg == 1 && uke != 1) uke--;
 			if(valg == 2) uke++;
 			if(valg == 0) break;
 		}
 	}
 
 	public static void createMeeting() throws IOException{
 		Meeting m = null;
 		System.out.println("Create a meeting");
 		System.out.print("Type date (yyyy-mm-dd): ");
 		String date = br.readLine();
 
 		System.out.print("Type start time (hh:mm): ");
 		String startTime = br.readLine();
 
 		System.out.print("Type end time (hh:mm): ");
 		String endTime = br.readLine();
 
 		System.out.print("Add a description: ");
 		String descr = br.readLine();
 
 		System.out.print("Do you want to book a room? (y/n): ");
 		String a = br.readLine();
 
 		if(a.startsWith("y") || a.startsWith("Y")) {
 			Meetingroom meetingroom = chooseMeetingroom(null, date + " " + startTime, date + " " + endTime);
 			m = p.createMeeting(date + " " + startTime, date + " " + endTime, descr, meetingroom);
 		} else {
 			m = p.createMeeting(date + " " + startTime, date + " " + endTime, descr, null);
 		}
 		
 		System.out.print("Would you like to add people to the meeting? (y/n): ");
 		a = br.readLine();
 		if(a.startsWith("y") || a.startsWith("Y")) {
 			addParticipants(m);
 		} 
 	}
 
 	private static void addParticipants(Meeting m) throws IOException {
 		
 		String a = "y";
 		while(a.equalsIgnoreCase("y")) {
 			
 			System.out.println("The following people are available to join this meeting: "); 
 			ArrayList<Person> persons = p.getAvailablePersons(m);
 			for (int j = 0; j < persons.size(); j++){
 				System.out.println(j + ".  " + persons.get(j));
 			}
 
 			// bruker velger en person den nsker  legge til mtet
 			System.out.print("Choose a person: ");
 			int o = in.nextInt();
 			m.addParticipant(persons.get(o));
 			p.addMeetingrequest(m, persons.get(0));
 			Meetingrequest r = new Meetingrequest(m, p.getPersonList().get(o));
 			System.out.print("Would you like to add another participant? (y/n): ");
 			a = br.readLine();
 		}
 	
 	}
 	
 	public static Meetingroom chooseMeetingroom(Meeting m, String start, String end) {
 		System.out.println("How many seats do you need?");
 		int nbr = in.nextInt();
 		ArrayList<Meetingroom> rooms = p.getAvailableMeetingrooms(start, end,nbr);
 		if (rooms.size() == 0) {
 			
 		} else {
 			System.out.println("The following meetingrooms are available"); 
 			for (int j = 0; j < rooms.size(); j++){
 				System.out.println(j + ".  " + rooms.get(j).getName());
 			}
 			System.out.print("Choose a room: ");
 			int i = in.nextInt();
 			if (i<rooms.size()) {
 				return rooms.get(i);
 			}	
 		}
 		return null;
 	}
 		
 	private static void showAllCreatedMeetings() throws IOException {
 		ArrayList<Meeting> l = p.showAllCreatedMeetings();
 		if (l.size() == 0) {
 			System.out.print("Ingen mter.");
 			return;
 		}
 		for (int i = 0; i< l.size(); i++){
 			String df1=new SimpleDateFormat("yyyy-MMM-dd hh:mm").format(l.get(i).getStartTime());
 			String df2=new SimpleDateFormat("yyyy-MMM-dd hh:mm").format(l.get(i).getEndTime());
 
			System.out.println(i + ". Start: " + df1 + "     End: " + df2 + "     Description: " + l.get(i).getDescription() + "     Room: " + l.get(i).getMeetingRoom().getName());
 		}
 
 		System.out.print("Would you like to cancel a meeting? (y/n): ");
 		String a = br.readLine();
 
 		switch(a){
 			case "y": 
 				System.out.print("Which one? Type a number from the list above: ");
 				int k = in.nextInt();
 				cancelMeeting(l.get(k));
 				showAllCreatedMeetings();
 			break;
 
 			case "n": 
 				System.out.print("Would you like to change a meeting? (y/n): ");
 				a  = br.readLine();
 				if (a.equals("y")){
 					System.out.print("Which one? Type a number from the list above: ");
 					int j = in.nextInt();
 					changeMeeting(l.get(j)); 
 					showAllCreatedMeetings();
 				}
 			break;
 		}
 	}
 
 	public static void changeMeeting(Meeting meeting) throws IOException{
 		System.out.println("Now changing meeting");
 		System.out.print("New date (yyyy-mm-dd): ");
 		String date = br.readLine();
 
 		System.out.print("New start time (hh:mm): ");
 		String st = br.readLine();
 
 		System.out.print("New end time (hh:mm): ");
 		String et = br.readLine();
 		
 		p.changeMeeting(meeting, date + " " + st,date + " " + et);
 	}
 
 	public static void cancelMeeting(Meeting m) throws IOException{
 		System.out.print("Write an explanation for why the meeting was cancelled: ");
 		String a = br.readLine();
 		p.removeMeeting(m, a);
 	}
 	
 	private static void showAllCreatedAppointments() throws IOException {
 		ArrayList<Appointment> app = p.showAllCreatedAppointments();
 
 
 		for (int i = 0; i< app.size(); i++){
 			String df1=new SimpleDateFormat("yyyy-MMM-dd hh:mm").format(app.get(i).getStartTime());
 			String df2=new SimpleDateFormat("yyyy-MMM-dd hh:mm").format(app.get(i).getEndTime());
 
 			System.out.println(i +". Start: " + df1 + "     End: " + df2 + "     Description: " + app.get(i).getDescription() + "     Place: " + app.get(i).getPlace());
 		}
 
 		System.out.print("Would you like to cancel an appointment? (y/n): ");
 		String a = br.readLine();
 
 		switch(a){
 			case "y": 
 				System.out.print("Which one? Type a number from the list above: ");
 				int i = in.nextInt();
 				cancelAppointment(app.get(i));
 				showAllCreatedAppointments();
 			break;
 
 			case "n": 
 				System.out.print("Would you like to change an appointment? (y(n): ");
 				a  = br.readLine();
 				if (a.equals("y")){
 					System.out.print("Which one? Type a number from the list above: ");
 					int j = in.nextInt();
 					changeAppointment(app.get(j)); 
 					showAllCreatedAppointments();
 				}
 			break;
 		}
 	}
 
 	private static void changeAppointment(Appointment appointment) throws IOException {
 
 		System.out.print("Would you like to change date and time? (y/n): ");
 		String a = br.readLine();
 		if (a.equals("y")){
 			System.out.print("New date (yyyy-mm-dd): ");
 			String date = br.readLine();
 
 			System.out.println("New start time (hh:mm): ");
 			String st = br.readLine();
 			
 
 			System.out.println("New start time (hh:mm): ");
 			String et = br.readLine();
 			if(!p.changeAppointment(appointment, date + " " + st, date + " " + et)) {
 				System.out.println("The given time collides with another appointment/meeting");
 				changeAppointment(appointment);
 				return;
 			}
 		}
 
 		System.out.print("Would you like to change description? (y/n): ");
 		a = br.readLine();
 		if (a.equals("y")){
 			System.out.print("New description: ");
 			String descr = br.readLine();
 			appointment.setDescription(descr);
 		}
 
 		System.out.println("Would you like to change place? (y/n): ");
 		a = br.readLine();
 		if (a.equals("y")){
 			System.out.print("New place: ");
 			String place = br.readLine();
 			appointment.setPlace(place);
 		}
 	}
 
 	private static void cancelAppointment(Appointment appointment) {
 		p.removeAppointment(appointment);
 	}
 
 	private static void createAppointment() throws IOException {
 		System.out.print("Type date (yyyy-mm-dd): ");
 		String date = br.readLine();
 
 		System.out.print("Type start time (hh:mm): ");
 		String startTime = br.readLine();
 
 		System.out.print("Type end time (hh:mm): ");
 		String endTime = br.readLine();
 		
 		System.out.print("Add a description: ");
 		String descr = br.readLine();
 
 		System.out.print("Where?: ");
 		String where = br.readLine();
 
 		p.addAppointment(descr, where, date + " " + startTime, date + " " + endTime);
 	}
 
 	private static void showMeetingRequests() throws IOException {
 		ArrayList<Meetingrequest> mrs= p.getMeetingrequests();
 		System.out.println("Unanswered meeting requests");
 		for (int i = 0; i < mrs.size(); i++) {
 			System.out.println(i + ". " + mrs.get(i).getMeeting().getMeetingLeader().getName() + " has invited you to " + mrs.get(i).getMeeting().getDescription());
 		}
 		
 		System.out.print("Would you like to respond to a request? (y/n): ");
 		String a = br.readLine();
 		
 		if (a.startsWith("y") || a.startsWith("Y")) {
 			System.out.print("Write in what request you want to respond to: ");
 			int nr = in.nextInt();
 			System.out.print("Will you attend this meeting? (y/n): ");
 			a = br.readLine();
 			if (a.startsWith("y") || a.startsWith("Y")) {
 				p.answerMeetingrequest(mrs.get(nr), true);
 				
 			} else p.answerMeetingrequest(mrs.get(nr), false);
 		}
 	}
 	
 	private static void showMessages() {
 		ArrayList<Message> msgs = p.getMessages();
 		System.out.println("Meldinger: ");
 		for (int i = msgs.size()-1; i >= 0; i--) {
 			System.out.println("Avsender: " + msgs.get(i).getSender().getName() + "\nBeskjed: "+ msgs.get(i).getText() + "\n" + menuspacer);
 		}		
 	}
 
 
 //	public static Meeting chooseRoom(Date st, Date et, String descr) throws IOException{
 //		Meeting m = null;
 //
 //		System.out.println("How many seats do you need?");
 //		int nbr = in.nextInt();
 //
 //		ArrayList<Meetingroom> availableRooms =	p.generateAvailableRooms(st, et, nbr);
 //
 //		if (availableRooms.size() == 0){
 //			System.out.println("No room is available in your specified period. Do you want to try another period" +
 //					"(type x), or do you just want to create meeting without roomreservation (type y)?");
 //
 //			String a = br.readLine();
 //
 //			switch(a){
 //			case "x": m = p.createMeeting(st, et, descr);
 //			break;
 //			case "y": m = createMeeting();
 //			}
 //		}
 //
 //		else{
 //			System.out.println("\nThe following rooms are available in your specified period: ");
 //
 //			for (int i = 0; i < availableRooms.size(); i++){
 //				System.out.println("Room number: " + i + ", " + availableRooms.get(i).getName());
 //			}
 //
 //			System.out.println("\n Type a room number: ");
 //			int i = in.nextInt();
 //
 //			m = p.createMeeting(st, et, descr, availableRooms.get(i));	
 //			availableRooms.get(i).addMeetingToList(m);
 //		}
 //		return m;
 //	}
 
 	public static void changeRoom(Meeting m, Date st, Date et ) throws IOException{
 
 		System.out.println("How many seats do you need?");
 		int nbr = in.nextInt();
 
 		ArrayList<Meetingroom> availableRooms =	p.generateAvailableRooms(st, et, nbr);
 
 		if (availableRooms.size() == 0){
 			System.out.println("No room is available in your specified period. Do you want to try another period" +
 					"(type x), or do you just want to create meeting without roomreservation (type y)?");
 
 			String a = br.readLine();
 
 			switch(a){
 			case "x": changeMeeting(m);
 			break;
 
 			case "y": m.getMeetingRoom().getMeetings().remove(m);
 			m.setMeetingRoom(null);
 
 			}
 		}
 
 		else{
 			System.out.println("\nThe following rooms are available in your specified period: ");
 
 			for (int i = 0; i < availableRooms.size(); i++){
 				System.out.println("Room number: " + i + ", " + availableRooms.get(i).getName());
 			}
 
 			System.out.println("\n Type a room number: ");
 			int i = in.nextInt();
 
 			// fjerner mtet fra det tidligere mterommets liste
 			m.getMeetingRoom().getMeetings().remove(m);
 
 			// legger til det nye mterommet.
 			m.setMeetingRoom(availableRooms.get(i));
 			availableRooms.get(i).addMeetingToList(m);
 		}
 
 	}
 
 	
 
 	public static Date stringToDate(String date, String time){
 		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm");
 
 		Date c = null;
 		try {
 			c = sdf.parse(date + " " + time);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 
 		return c;
 
 	}
 }
