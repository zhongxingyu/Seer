 package data;
 
 import java.awt.Color;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 
 import client.Program;
 
 import framePackage.DefaultView;
 
 public class CalendarModel implements Serializable {
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
 			DATE_CHANGED_Property = "DATE", ROOMS_CHANGED_Property = "ROOMS";
 
 	/**
 	 * Constructs the calendar model.
 	 */
 	public CalendarModel() {
 		pcs = new PropertyChangeSupport(this);
 		calendar = new GregorianCalendar();
 	}
 
 	/**
 	 * Initiate CalendarModel
 	 * @param username
 	 */
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
 
 	/**
 	 * Returns all meetings of the person.
 	 * @param person
 	 * @param attending
 	 * @return allMeetings
 	 */
 	public ArrayList<Meeting> getAllMeetingsOfPerson(Person person,
 			boolean attending) {
 		ArrayList<Meeting> allMeetings = new ArrayList<Meeting>();
 		allMeetings.addAll(getAppointments());
 		allMeetings.addAll(getMeetings(person, attending));
 		return allMeetings;
 	}
 
 	/**
 	 * Returns something... DAVID!??
 	 * @param person
 	 * @param attending
 	 * @return
 	 */
 	public ArrayList<Meeting> getMeetings(Person person, boolean attending) {
 		ArrayList<Meeting> allMeetings = new ArrayList<Meeting>();
 		for (Notification n : notifications) {
 			if (n.getPerson().getUsername().equals(person.getUsername())
 					&& (n.getApproved() == 'y' || !attending)) {
 				allMeetings.add(n.getMeeting());
 			}
 		}
 		return allMeetings;
 	}
 
 	/**
 	 * Returns a list of meetings.
 	 * @return appointments
 	 */
 	public ArrayList<Meeting> getAppointments() {
 		ArrayList<Meeting> appointments = new ArrayList<Meeting>();
 		for (Meeting meeting : meetings) {
 			if (meeting.getTeam() == null
 					&& meeting.getCreator().getUsername()
 							.equals(user.getUsername())) {
 				appointments.add(meeting);
 			}
 		}
 		return appointments;
 	}
 
 	/**
 	 * Returns all notifications of a person.
 	 * @param person
 	 * @return notifications
 	 */
 	public ArrayList<Notification> getAllNotificationsOfPerson(Person person) {
 		ArrayList<Notification> notis = new ArrayList<Notification>();
		for (Notification n : notis) {
 			if (n.getPerson().getUsername().equals(person.getUsername())) {
 				notis.add(n);
 			}
 		}
 		return notis;
 	}
 
 	/**
 	 * Returns unanswered notifications of user.
 	 * @return unanswered
 	 */
 	public ArrayList<Notification> getUnansweredNotificationsOfUser() {
 		ArrayList<Notification> unanswered = new ArrayList<Notification>();
 		for (Notification n : notifications) {
 			if (n.getApproved() == 'w') {
 				unanswered.add(n);
 			}
 		}
 		return unanswered;
 	}
 
 	/**
 	 * Returns all notifications of a meeting
 	 * @param meeting
 	 * @return notifications
 	 */
 	public ArrayList<Notification> getAllNotificationsOfMeeting(Meeting meeting) {
 		ArrayList<Notification> notis = new ArrayList<Notification>();
 		for (Notification n : notifications) {
 			if (n.getMeeting().getMeetingID() == meeting.getMeetingID()) {
 				notis.add(n);
 			}
 		}
 		return notis;
 	}
 
 	/**
 	 * Returns alarma by a meeting.
 	 * @param meeting
 	 * @return alarm
 	 */
 	public Alarm getAlarmByMeeting(Meeting meeting) {
 		for (Alarm alarm : alarms) {
 			if (alarm.getMeeting().getMeetingID() == meeting.getMeetingID()) {
 				return alarm;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets ALL of the meetings of a person in the given time interval
 	 * 
 	 * @param person
 	 *            the person whose meetings to get
 	 * @param start
 	 *            the minimum start time of the meeting
 	 * @param end
 	 *            the maximum end time of the meeting
 	 * @return all the meetings of the given person within the given time
 	 *         interval.
 	 */
 	// TODO
 	// public ArrayList<Meeting> getMeetings(Person person, long start, long
 	// end) {
 	// ArrayList<Meeting> meetings = meetings.get(person);
 	// ArrayList<Meeting> newMeetings = new ArrayList<Meeting>();
 	// for (Meeting meeting : meetings) {
 	// if (meeting.getStartTime() >= start && meeting.getEndTime() < end) {
 	// newMeetings.add(meeting);
 	// }
 	// }
 	// return newMeetings;
 	// }
 
 	public List<Person> getPersons() {
 		return persons;
 	}
 
 	// TODO
 	// public HashMap<Person, ArrayList<Meeting>> getHasjmap() {
 	// return meetings;
 	// }
 
 	public ArrayList<Boolean> getSelected() {
 		return selected;
 	}
 
 	public void setAllSelected(ArrayList<Boolean> selected) {
 		this.selected = selected;
 	}
 
 	public void setSelected(Person person, boolean sel) {
 		selected.set(persons.indexOf(person), sel);
 		System.out.println("Set selected (Model)");
 		pcs.firePropertyChange(SELECTED_Property, null, null);
 	}
 
 	private void requestEverything() {
 		try {
 			requestAllMeetings();
 			requestAllNotifications();
 			requestAlarmsOfUser();
 			requestAllRooms();
 		} catch (IOException e) {
 			System.out.println("Requests failed");
 			e.printStackTrace();
 		}
 
 	}
 
 	private void requestAllMeetings() throws IOException {
 		Program.reqHandler.sendGetEvryMeetingRequest();
 	}
 
 	private void requestAllNotifications() throws IOException {
 		Program.reqHandler.sendGetAllNotificationsRequest();
 	}
 
 	private void requestAlarmsOfUser() throws IOException {
 		Program.reqHandler.sendGetAlarmsByPersonRequest(user);
 	}
 
 	private void requestAllRooms() throws IOException {
 		Program.reqHandler.sendGetAllMeetingroomsRequest();
 	}
 
 	private void requestAllPersons() {
 		try {
 			if (Program.reqHandler != null) {
 				Program.reqHandler.sendGetAllPersonsRequest();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Sets all the persons of the model. This method will only be called once
 	 * by the server at startup
 	 * 
 	 * @param persons
 	 */
 	public void setAllPersons(List<Person> persons) {
 		this.persons = persons;
 		for (Person person : persons) {
 			if (person.getUsername().equals(username)) {
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
 		pcs.firePropertyChange(ROOMS_CHANGED_Property, null, null);
 	}
 
 	public void setAlarmsOfUser(List<Alarm> alarms) {
 		this.alarms = (ArrayList<Alarm>) alarms;
 		pcs.firePropertyChange(ALARMS_CHANGED_Property, null, null);
 	}
 
 	public void setAllNotifications(List<Notification> notifications) {
 		this.notifications = (ArrayList<Notification>) notifications;
 		System.out.println("setting all notifications");
 		pcs.firePropertyChange(NOTIFICATIONS_CHANGED_Property, null, null);
 	}
 
 	public List<Person> getSelectedPersons() {
 		List<Person> selectedPersons = new ArrayList<Person>();
 		for (int i = 0; i < selected.size(); i++) {
 			if (selected.get(i)) {
 				selectedPersons.add(persons.get(i));
 			}
 		}
 		return selectedPersons;
 	}
 
 	public Color getColorOfPerson(Person person) {
 		int index = -2;
 		for (int i = 0; i < persons.size(); i++) {
 			if (person.getUsername().equals(persons.get(i).getUsername())) {
 				index = i;
 			}
 		}
 		return colors[index];
 	}
 
 	public void setStatus(char c, Notification notification) {
 		try {
 			System.out.println("setting status: "+c);
 			Program.reqHandler.sendUpdateNotificationRequest(
 					new Notification(Calendar.getInstance().getTimeInMillis(), c, notification
 							.getKind(), notification.getMeeting(), notification.getPerson()));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void pushMeeting(Meeting meeting) {
 		System.out.println("Trying to push meeting");
 		try {
 			Program.reqHandler.sendCreateMeetingRequest(meeting);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void changeMeeting(Meeting meeting) {
 		try {
 			Program.reqHandler.sendUpdateMeetingRequest(meeting);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void removeMeeting(Meeting meeting) {
 		try {
 			Program.reqHandler.sendDeleteMeetingRequest(meeting);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public ArrayList<MeetingRoom> getRooms() {
 		return meetingRooms;
 	}
 
 	public ArrayList<MeetingRoom> getAvailableRooms(long startTime, long endTime) {
 		ArrayList<MeetingRoom> rooms = new ArrayList<MeetingRoom>();
 		rooms.addAll(meetingRooms);
 		for (Meeting meeting : meetings) {
 			long meetStart = meeting.getStartTime();
 			long meetEnd = meeting.getEndTime();
 			if (meeting.getRoom() != null
 					&& (meetStart >= startTime && meetStart < endTime)
 					|| (meetEnd > startTime && meetEnd < endTime)) {
 				rooms.remove(meeting.getRoom());
 			}
 		}
 		return rooms;
 	}
 
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
