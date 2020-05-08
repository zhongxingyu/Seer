 package connection;
 
 import gui.GUI;
 import gui.GuiController;
 
 import java.util.List;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.dom4j.DocumentException;
 
 import dbhandle.Status;
 
 import Models.Event;
 import Models.Meeting;
 import Models.Meetingroom;
 import Models.User;
 import xmlhandle.Xmlhandle;
 
 public class Client implements ActionListener{
 	ClientConnection clientConnection;
 	Xmlhandle xmlHandle = new Xmlhandle();
 	private GuiController guicontroller;
 	private User user;
 	private ArrayList<User> allUsers;
 	private ArrayList<User> myUsers;
 	private ArrayList<Meetingroom> meetingrooms;
 	private ArrayList<Meeting> meetings;
 	private int shownWeek;
 	private int shownYear;
 	private Timestamp startOfWeek = new Timestamp(new Date().getTime()- getDayOfWeek()*(24*60*60*1000));
 	private Timestamp endOfWeek = new Timestamp(new Date().getTime()+(8-getDayOfWeek())*(24*60*60*1000));
 	private final long WEEKLENGTH = 7*24*60*60*1000; //in ms
 	private String testUsername = "Henning";
 	private String testPassword = "henning";
 	private boolean editing;
 	private boolean waitingForServerRespons = false;
 	
 	public static void main(String[] args) {
 //		System.out.println(getDayOfWeek());
 		Client client = new Client();
 	}
 	
 	public Client() {
 		clientConnection = new ClientConnection();
 		clientConnection.addReceiveListener(this);
 		xmlHandle.addListener(this);
 		System.out.println("logging in");
 		
 		xmlHandle.createLoginRequest(testUsername,testPassword);
 //		waitingForServerRespons = true;
 //		while(waitingForServerRespons);
 		
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		System.out.println("logged in");
 		
 		guicontroller = new GuiController();
 		guicontroller.addListener(this);
 		editing = false;
 //		guicontroller.setNewEvent(new Event(user));
 		
 		meetingrooms = new ArrayList<Meetingroom>();
 		meetingrooms.add(new Meetingroom((int)(Math.random()*10000), "Obsidian Eathershrine"));
 		meetingrooms.add(new Meetingroom((int)(Math.random()*10000), "Minestery of Awesome"));
 		
 		meetings = new ArrayList<Meeting>();
 		
 		
 		initializeTimeThings();  // Initializes the time things (variables)
 		addCalendars(); //loads the users imported calendars into the GUI
 		updateFields();
 
 //		guicontroller = new GUI();
 //		guicontroller.addListener(this);
 	}
 	
 
 	private void initializeTimeThings() {
 		shownWeek = getWeekNumber();
 		shownYear = getYearNumber();
 
 		startOfWeek.setHours(23);
 		startOfWeek.setMinutes(59);
 		startOfWeek.setSeconds(59);
 		startOfWeek.setNanos(999999999);
 		endOfWeek.setHours(0);
 		endOfWeek.setMinutes(0);
 		endOfWeek.setSeconds(0);
 		endOfWeek.setNanos(0);
 	}
 
 	public void showHideCalendarsAction() {
 		System.out.println(guicontroller.getActiveCalendars().size());
 		updateCalendar(shownWeek, shownYear);
 	}
 	public void meetingroomSearchAction() {
 		String search = guicontroller.getMeetingroomSearch();
 		ArrayList<Meetingroom> validMeetingrooms = new ArrayList<Meetingroom>();
 		for(Meetingroom meetingroom:meetingrooms) {
 			boolean valid = true;
 			for(int i=0;i<search.length()&&i<meetingroom.getRoomName().length();i++) {
 				if(!(search.charAt(i)==meetingroom.getRoomName().charAt(i))) valid = false;
 			}
 			if(valid) validMeetingrooms.add(meetingroom);
 		}
 		guicontroller.setAvailableMeetingrooms(validMeetingrooms);
 	}
 	public void personsSearchAction() {
 		String search = guicontroller.getPersonSearch();
 		ArrayList<User> validPersons = new ArrayList<User>();
 		for(User loopingUser:allUsers) {
 			boolean valid = true;
 			for(int i=0;i<loopingUser.getName().length() && i<search.length();i++) {
 				if(!(search.charAt(i)==loopingUser.getName().charAt(i))) valid = false;
 			}
 			if(valid) validPersons.add(loopingUser);
 		}
 		guicontroller.setAvailablePersons(validPersons);
 	}
 
 	public void addEventButtonAction(String title, String startDate, String startTime, String endDate, String endTime, 
 									 String description, Meetingroom room, ArrayList<User> participants) {
 		if(!editing){			
 			Timestamp start = Timestamp.valueOf(startDate + " " + startTime + ":00");
 			Timestamp end = Timestamp.valueOf(endDate + " " + endTime + ":00");
 			
 			dbhandle.Event event = new dbhandle.Event(start, end, "somewhere", description, Status.ACCEPTED);
 			int meetingroomid = 1;
 			
 			participants = new ArrayList<User>();
 			participants.add(user);
 			
 			List<Integer> listParticipants = new ArrayList<Integer>();
 			for(User user:participants) {
 				listParticipants.add(user.getUSER_ID());
 			}
 			System.out.println(user.getEvents().size());
 			xmlHandle.createAddMeetingRequest(listParticipants, event, meetingroomid, title, user.getUSERNAME());
 			clearNewEvent();			
 			waitingForServerRespons = true;
 			while(waitingForServerRespons);
 			updateCalendar(shownWeek, shownYear);
 			System.out.println(user.getEvents().size());
 		}
 		
 //		guicontroller.setNewEvent(new Meeting(event));
 //		public void addEventButtonAction() {
 
 	}
 	private void clearNewEvent() {
 		editing = false;
 		guicontroller.clearNewEvent();
 	}
 
 	public void changeNameButtonAction() {
 		System.out.println(user.getName());
 		System.out.println("changing the name of the game");
 		String newName = guicontroller.getNewName();
 		user.setName(newName);
 		xmlHandle.createEditNameOfUserRequest(newName, user.getUSERNAME());
 	}
 	public void changePasswordButtonAction() {
 		String oldPassword = guicontroller.getOldPassword();
 		String newPassword = guicontroller.getNewPassword();
 		String newRepeatedPassword = guicontroller.getRepeatedNewPasword();
 		System.out.println("old"+ oldPassword +"new"+ newPassword +"newrep"+ newRepeatedPassword);
 		if(newPassword.equals(newRepeatedPassword)) {			
 			xmlHandle.createEditUserPasswordRequest(oldPassword, newPassword, user.getUSERNAME());
 		}
 	}
 	public void yourCalendarsSearchAction() {
 		//når det trykkes på en knapp i usersearchtextfield
 		//done
 	}
 	public void yourCalendarsAction(User user) {
 		//når det trykkes på en kalender i usercalendars
 	}
 	public void availableCalendarsSearchAction() {
 		//når det trykkes på en knapp i availiblesearchtextfield
 		//done
 	}
 	public void avaliableCalendarsAction(User user) {
 		//når det trykkes på en kalender i availiblecalendars
 	}
 	public void meetingAcceptAction(Event event) {
 		event.setStatus(dbhandle.Status.ACCEPTED);
 		updateMessages();
 //		System.out.println(event.getTitle());
 	}
 	public void meetingDeclineAction(Event event) {
 		//trykker avslå på et møte
 		//ActionListener finnes, den bare virker ikke
 		event.setStatus(dbhandle.Status.ACCEPTED);
 		updateMessages();
 	}
 	public void calendarEventAction(Event event) {
 		//når det trykkes på en event i kalenderen
 		Meeting clickedMeeting = null;
 		for(Meeting meeting:meetings) {
 			if(meeting.getParticipants().contains(event)) clickedMeeting = meeting;
 		}
 		guicontroller.setNewEvent(clickedMeeting);
 	}
 	public void nextWeekButtonAction() {
 		changeWeek(1);
 		updateFields();
 	}
 	public void lastWeekButtonAction() {
 		changeWeek(-1);
 		updateFields();
 	}
 	private void updateFields() {
 		updateCalendar(shownWeek, shownYear);
 		updateDashboard();
 		createNewEvent();
 		updateSettings();
 		updateMessages();
 	}
 
 	private void updateCalendar(int shownWeek, int shownYear) {
 		System.out.println("updating calendar");
 		guicontroller.setCalendarEntries(getCalendarEntries(shownWeek));
 		guicontroller.setCalendarTitle("Uke " + shownWeek + " - " + shownYear);
 	}
 
 	//ONE LINERS HELL YEA !!!
	private static int getDayOfWeek() {return ((new GregorianCalendar().get(Calendar.DAY_OF_WEEK))-1)%7+1;}
 	private static int getWeekNumber() {return new GregorianCalendar().get(Calendar.WEEK_OF_YEAR);}
 	private static int getYearNumber() {return 	new GregorianCalendar().get(Calendar.YEAR);} 
 
 	private ArrayList<ArrayList<Event>> getCalendarEntries(int weekNumber) {
 		ArrayList<ArrayList<Event>> calendarEntries = new ArrayList<ArrayList<Event>>();
 		ArrayList<User> activeCalendars = guicontroller.getActiveCalendars();
 		for(User otheruser:user.getImportedCalendars()) {
 			if(activeCalendars.contains(otheruser)){  //Checks if the calendar is active				
 				ArrayList<Event> otherUsersCalendar = new ArrayList<Event>();
 				for(Event event:otheruser.getEvents()) {
 //					System.out.println("checking event");
 					if(event.getStartTime().after(startOfWeek) && event.getStartTime().before(endOfWeek)) { //Checks if the event is in the right week
 						otherUsersCalendar.add(event);  //Adds the event
 //						System.out.println(event.getStartTime().getDate());
 					}
 				}
 				calendarEntries.add(otherUsersCalendar); //Adds the list with the users events that week
 			}
 		}
 //		user.getEvents();
 //		System.out.println(calendarEntries.get(0).size() + " " + calendarEntries.get(1).size() + " " + calendarEntries.get(2).size());
 		return calendarEntries;
 	}
 
 	private void updateDashboard() {
 		updateAgenda();
 	}
 
 	private void updateAgenda() {
 		ArrayList<Event> agenda = new ArrayList<Event>();
 		for(Event event:user.getEvents()) {
 			if(event.getStartTime().after(getNow())){
 				agenda.add(event);
 			}
 		}
 		guicontroller.setAgenda(agenda);
 	}
 
 	private Timestamp getNow() {
 		return new Timestamp(new Date().getTime());
 	}
 
 	private void createNewEvent() {
 		guicontroller.setNewEvent(new Meeting(new Event(user)));
 	}
 
 	private void updateSettings() {
 		guicontroller.setYourCalendars(user.getImportedCalendars()); //setter hvilke kalendere som kan velges
 		guicontroller.setAvailableCalendars(allUsers); //setter hvilke kalendere som kan velges
 	}
 	
 	private void updateMessages() {
 		ArrayList<Event> messages = new ArrayList<Event>();
 		for(Event event:user.getEvents()){
 			if(event.getStartTime().after(getNow()) && event.getStatus()==dbhandle.Status.NOT_RESPONDED) {
 				messages.add(event);
 			}
 		}
 		guicontroller.setMessages(messages);
 	}
 
 	private void addCalendars() {
 		System.out.println(user.getImportedCalendars().size());
 		for(User calendar:user.getImportedCalendars()) {
 			System.out.println("adding: " + calendar.getName());
 			guicontroller.addCalendar(calendar);
 		}
 	}
 	
 	private void changeWeek(int weeks) {
 		System.out.println(startOfWeek.getDate());
 		startOfWeek.setTime(startOfWeek.getTime()+WEEKLENGTH*weeks);
 		endOfWeek.setTime(endOfWeek.getTime()+WEEKLENGTH*weeks);
 		System.out.println(startOfWeek.getDate());
 		shownWeek += weeks;
 		if(shownWeek<1) {
 			shownWeek = 52;
 			shownYear -= 1;
 		}
 		if(shownWeek>52) {
 			shownWeek = 1;
 			shownYear += 1;
 		}
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {	
 		
 		System.out.println(e.getSource().getClass());
 		System.out.println(clientConnection.getClass());
 		if(e.getSource()==clientConnection) {
 			//System.out.println("Received at serverConnection: " + e.getActionCommand());
 			clientConnectionAction(e.getActionCommand());
 		}
 		if(e.getSource()==xmlHandle) {
 			xmlHandleAction((Xmlhandle) e.getSource());
 		}
 		if(user!=null && guicontroller!=null) updateFields();
 	}
 	
 	private void clientConnectionAction(String msg) {
 		System.out.println("Message received from server: "+ msg);
 		
 		try {
 			try {
 				xmlHandle.interpretMessageData(Xmlhandle.stringToXML(msg), this);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		}
 		waitingForServerRespons = false;
 	}
 	
 	private void xmlHandleAction(Xmlhandle xmlHandle) {
 		clientConnection.send(xmlHandle.getMsgForSending());
 	}
 	public User getUser() {
 		return user;
 	}
 	
 	public ArrayList<User> getAllUsers() {
 		return allUsers;
 	}
 
 	public void setAllUsers(ArrayList<User> allUsers) {
 		this.allUsers = allUsers;
 	}
 
 	public ArrayList<User> getMyUsers() {
 		return myUsers;
 	}
 
 	public void setMyUsers(ArrayList<User> myUsers) {
 		this.myUsers = myUsers;
 	}
 
 	public ArrayList<Meetingroom> getMeetingrooms() {
 		return meetingrooms;
 	}
 
 	public void setMeetingrooms(ArrayList<Meetingroom> meetingrooms) {
 		this.meetingrooms = meetingrooms;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 	public ArrayList<Meeting> getMeetings() {
 		return meetings;
 	}
 
 	public void setMeetings(ArrayList<Meeting> meetings) {
 		this.meetings = meetings;
 	}
 
 }
