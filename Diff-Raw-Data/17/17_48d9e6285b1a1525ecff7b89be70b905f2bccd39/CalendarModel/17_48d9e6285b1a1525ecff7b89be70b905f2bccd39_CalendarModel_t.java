 package data;
 
 import java.awt.Color;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 
 import client.Program;
 
 import framePackage.DefaultView;
 
 
 public class CalendarModel implements Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1762790448612918057L;
 	private List<Person> persons;
 	private ArrayList<Meeting> meetings;
 	private ArrayList<Boolean> selected;
 	private PropertyChangeSupport pcs;
 	private ArrayList<Notification> notifications;
 	private ArrayList<Alarm> alarms;
 	private String username;
 	private Person user;
 	private ArrayList<MeetingRoom> meetingRooms;
 	private GregorianCalendar calendar;
 	private static final Color[] colors = { Color.red, Color.blue,
 			Color.yellow, Color.orange, Color.magenta, Color.gray, Color.pink };
 	public static final String SELECTED_Property = "SELECTED",
 			MEETINGS_CHANGED_Property = "MEETINGS",
 			NOTIFICATIONS_CHANGED_Property = "NNOTI",
 			CALENDAR_LOADED_Property = "LOADED",
 			PERSONS_ADDED_Property = "PERSONS",
 			ALARMS_CHANGED_Property = "ALARMA!",
 			DATE_CHANGED_Property = "DATE";
 
 
 	public CalendarModel() {
 		pcs = new PropertyChangeSupport(this);
 		calendar = new GregorianCalendar();
 	}
 	public void init(String username) {
 		System.out.println();
 		this.username = username;
 		persons = new ArrayList<Person>();
 		meetings = new ArrayList<Meeting>();
 		selected = new ArrayList<Boolean>();
 		notifications = new ArrayList<Notification>();
 		alarms = new ArrayList<Alarm>();
 		meetingRooms = new ArrayList<MeetingRoom>();
 		requestAllPersons();
 	}
 	
 	public ArrayList<Meeting> getAllMeetingsOfPerson(Person person, boolean attending) {
 		ArrayList<Meeting> allMeetings = new ArrayList<Meeting>();
 		allMeetings.addAll(getAppointments());
 		allMeetings.addAll(getMeetings(person, attending));
 		return allMeetings;
 	}
 	public ArrayList<Meeting> getMeetings(Person person, boolean attending) {
 		ArrayList<Meeting> allMeetings = new ArrayList<Meeting>();
 		for (Notification n : notifications) {
 			if(n.getPerson().getUsername().equals(person.getUsername()) && (n.getApproved() == 'y' || !attending)) {
 				allMeetings.add(n.getMeeting());
 			}
 		}
 		return allMeetings;
 	}
 	public ArrayList<Meeting> getAppointments() {
 		ArrayList<Meeting> appointments = new ArrayList<Meeting>();
 		for (Meeting meeting : meetings) {
 			if(meeting.getTeam() == null && meeting.getCreator().getUsername().equals(user.getUsername())) {
 				appointments.add(meeting);
 			}
 		}
 		return appointments;
 	}
 	public ArrayList<Notification> getAllNotificationsOfPerson(Person person) {
 		ArrayList<Notification> notis = new ArrayList<Notification>();
 		for (Notification n : notifications) {
 			if(n.getPerson().getUsername().equals(person.getUsername())) {
 				notis.add(n);
 			}
 		}
 		return notis;
 	}
 	
 	public ArrayList<Notification> getAllNotificationsOfMeeting(Meeting meeting) {
 		ArrayList<Notification> notis = new ArrayList<Notification>();
 		for (Notification n : notifications) {
 			if(n.getMeeting().getMeetingID() == meeting.getMeetingID()) {
 				notis.add(n);
 			}
 		}
 		return notis;
 	}
 	
 	/**
 	 * Gets ALL of the meetings of a person in the given time interval
 	 * @param person the person whose meetings to get
 	 * @param start the minimum start time of the meeting
 	 * @param end the maximum end time of the meeting
 	 * @return all the meetings of the given person within the given time interval.
 	 */
 //	TODO
 //	public ArrayList<Meeting> getMeetings(Person person, long start, long end) {
 //		ArrayList<Meeting> meetings = meetings.get(person);
 //		ArrayList<Meeting> newMeetings = new ArrayList<Meeting>();
 //		for (Meeting meeting : meetings) {
 //			if (meeting.getStartTime() >= start && meeting.getEndTime() < end) {
 //				newMeetings.add(meeting);
 //			}
 //		}
 //		return newMeetings;
 //	}
 	
 	public List<Person> getPersons() {
 		return persons;
 	}
 	//TODO
 //	public HashMap<Person, ArrayList<Meeting>> getHasjmap() {
 //		return meetings;
 //	}
 
 	public ArrayList<Boolean> getSelected() {
 		return selected;
 	}
 
 	public void setAllSelected(ArrayList<Boolean> selected) {
 		this.selected = selected;
 	}
 	
 	public void setSelected(Person person, boolean sel) {
 		selected.set(persons.indexOf(person), sel);
		System.out.println("EEE WADDAFUK");
		for (int i = 0; i < pcs.getPropertyChangeListeners(SELECTED_Property).length; i++) {
			System.out.println(pcs.getPropertyChangeListeners(SELECTED_Property)[i].getClass());
		}
 		pcs.firePropertyChange(SELECTED_Property, person, person);
 	}
 	private void requestEverything() {
 		requestAllMeetings();
 		requestAllNotifications();
 		requestAlarmsOfUser();
 		requestAllRooms();
 		
 	}
 	private void requestAllRooms() {
 		// TODO Auto-generated method stub
 		
 	}
 	private void requestAlarmsOfUser() {
 		// TODO Auto-generated method stub
 		
 	}
 	private void requestAllNotifications() {
 		// TODO Auto-generated method stub
 		
 	}
 	private void requestAllPersons() {
 		try {
 			if(Program.reqHandler != null){
 				Program.reqHandler.sendGetAllPersonsRequest();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void requestAllMeetings() {
 		try {
 			Program.reqHandler.sendGetEvryMeetingRequest();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Sets all the persons of the model.
 	 * This method will only be called once by the server at startup
 	 * @param persons
 	 */
 	public void setAllPersons(List<Person> persons) {
 		this.persons = persons;
 		for (Person person : persons) {
 			if(person.getUsername().equals(username)) {
 				user = person;
 				selected.add(true);
 			} else {
 				selected.add(false);
 				
 			}
 		}
 		pcs.firePropertyChange(PERSONS_ADDED_Property, null, persons);
 		requestEverything();
 	}
 	
 	public void setAllMeetings(List<Meeting> meetings) {
 		this.meetings = (ArrayList<Meeting>) meetings;
 		pcs.firePropertyChange(CALENDAR_LOADED_Property, null, meetings);
 	}
 	public void setAllRooms(List<MeetingRoom> rooms) {
 		meetingRooms = (ArrayList<MeetingRoom>) rooms;
 	}
 	public void setAlarmsOfUser(List<Alarm> alarms) {
 		this.alarms = (ArrayList<Alarm>) alarms;
 	}
 	public void setAllNotifications(List<Notification> notifications) {
 		this.notifications = (ArrayList<Notification>) notifications;
 	}
 	
 	public List<Person> getSelectedPersons() {
 		List<Person> selectedPersons = new ArrayList<Person>();
 		for (int i = 0; i < selected.size(); i++) {
 			if(selected.get(i)) {
 				selectedPersons.add(persons.get(i));
 			}
 		}
 		return selectedPersons;
 	}
 	
 	public Color getColorOfPerson(Person person) {
 		return colors[persons.indexOf(person)];
 	}
 	public void pushMeeting(Meeting meeting) {
 		try {
 			Program.reqHandler.sendCreateMeetingRequest(meeting);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	public void changeMeeting(Meeting meeting) {
 		//TODO
 	}
 	public void removeMeeting(String meetingID) {
 		//TODO
 	}
 	public ArrayList<MeetingRoom> getRooms(){
 		return meetingRooms;
 	}
 //	TODO
 //	public ArrayList<MeetingRoom> getAvailableRooms(long startTime, long endTime){
 //		ArrayList<MeetingRoom> rooms = new ArrayList<MeetingRoom>();
 //		rooms.addAll(meetingRooms);
 //		for(ArrayList<Meeting> meetings: meetings.values()){
 //			for (int i = 0; i < meetings.size(); i++) {
 //				long meetStart = meetings.get(i).getStartTime();
 //				long meetEnd = meetings.get(i).getEndTime();
 //				if ((meetStart >= startTime && meetStart < endTime) || (meetEnd > startTime && meetEnd < endTime)) {
 //					rooms.remove(meetings.get(i).getRoom());
 //				}
 //			}
 //		}
 //		return rooms;
 //	}
 	public Person getUser() {
 		return user;
 	}
 	
 	public GregorianCalendar getCalendar() {
 		return calendar;
 	}
 	public void changeDate() {
 		pcs.firePropertyChange(DATE_CHANGED_Property, null, null);
 	}
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		pcs.addPropertyChangeListener(listener);
 	}
 }
