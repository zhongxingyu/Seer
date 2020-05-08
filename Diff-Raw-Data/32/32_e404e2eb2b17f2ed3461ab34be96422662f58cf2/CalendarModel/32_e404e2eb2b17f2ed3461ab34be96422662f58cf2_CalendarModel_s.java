 package client.model;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import chronos.DateManagement;
 import chronos.Person;
 import chronos.Person.Status;
 import chronos.Singleton;
 import client.ClientController;
 import client.gui.view.CalendarWindow;
 import client.gui.view.ChronosWindow;
 import events.CalEvent;
 import events.NetworkEvent;
 import events.QueryEvent.QueryType;
 import events.QueryEvent;
 
 public class CalendarModel extends ChronosModel {
 
 	public enum Weekday {
 		SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, NONE;
 
 		public static Weekday getWeekday(int ordinal) {
 			for (Weekday weekday : Weekday.values()) {
 				if ((weekday.ordinal() + 1) == ordinal)
 					return weekday;
 			}
 			return null;
 		}
 	}
 
 	private CalendarWindow calendarWindow;
 	private HashMap<String, ArrayList<CalEvent>> selectedPersonsEvents;
 
 	private HashMap<String, Person> selectedPersons;
 	private int currentDisplayedWeek;
 	private Date currentDisplayedDate;
 	private HashMap<String, Color> personColors;
 
 	public CalendarModel(ClientController controller) {
 		super(controller, ChronosType.CALENDAR);
 		selectedPersonsEvents = new HashMap<String, ArrayList<CalEvent>>();
 		selectedPersons = new HashMap<String, Person>();
 		currentDisplayedDate = DateManagement.getMondayOfWeek(new Date());
 		currentDisplayedWeek = DateManagement.getWeek(currentDisplayedDate);
 		personColors = new HashMap<String, Color>();
 	}
 
 	public String getCurrentDisplayedDateIntervall() {
 		return DateManagement.getFormattedDateIntervall(currentDisplayedDate);
 	}
 
 	public Date getCurrentDisplayedDate() {
 		return currentDisplayedDate;
 	}
 
 	public String getCurrentDisplayedWeek() {
 		return Integer.toString(currentDisplayedWeek);
 	}
 
 	/**
 	 * method that adds events for a specified person, the person lies in the
 	 * QueryEvent
 	 * 
 	 * @param queryEvent
 	 */
 
 	private void addEvents(QueryEvent queryEvent) {
 		ArrayList<CalEvent> calEvents = (ArrayList<CalEvent>) queryEvent.getResults();
 		Person person = queryEvent.getPerson();
 		String username = person.getUsername();
 		selectedPersonsEvents.put(username, calEvents);
 		selectedPersons.put(username, person);
 		update();
 	}
 
 	private void addEventsArrayList(ArrayList<CalEvent> calEvents, String username, Color personColor) {
		calendarWindow.setNotifications(0);
 		for (CalEvent calEvent : calEvents) {
 
 			if (personIsAttending(calEvent, username)) {
 				Date startDate = calEvent.getStart();
 				int eventWeek = DateManagement.getWeek(startDate);
 				int eventYear = DateManagement.getYear(startDate);
 				int currentDisplayedYear = DateManagement.getYear(currentDisplayedDate);
 				if (currentDisplayedWeek == eventWeek && eventYear == currentDisplayedYear) {
 					calendarWindow.addEvent(calEvent, DateManagement.getWeekday(startDate), personColor);
 				} else {
 					calendarWindow.addEvent(calEvent, Weekday.NONE, personColor);
 				}
			} else if (Singleton.getInstance().getSelf().getUsername().equals(username) && statusIsWaiting(calEvent, username)) {
				calendarWindow.addNotification(calEvent);
			}
 		}
		calendarWindow.getTabbedPane().setTitleAt(1, "Invites (" + calendarWindow.getNotifications() + ")");
 	}
 
 	private boolean statusIsWaiting(CalEvent event, String username) {
 		HashMap<String, Person> participants = event.getParticipants();
 		Person participant = participants.get(username);
 		try {
 			if (participant.getStatus() == Status.WAITING)
 				return true;
 
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		return false;
 	}
 
 	private boolean personIsAttending(CalEvent event, String username) {
 		HashMap<String, Person> participants = event.getParticipants();
 		Person participant = participants.get(username);
 		try {
 			if (participant.getStatus() == Status.ACCEPTED)
 				return true;
 
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		return false;
 	}
 
 	private void addOtherPersons(QueryEvent queryEvent) {
 		ArrayList<Person> persons = (ArrayList<Person>) queryEvent.getResults();
 		calendarWindow.removePersonCheckBoxes();
 		for (Person person : persons) {
 			if (selectedPersons.containsKey(person.getUsername())) {
 				calendarWindow.addOtherPerson(person, true);
 			} else {
 				calendarWindow.addOtherPerson(person, false);
 			}
 		}
 		calendarWindow.getFrame().pack();
 	}
 
 	private void getPersonEvents(Person person) {
 		QueryEvent event = new QueryEvent(QueryType.CALEVENTS, person);
 		fireNetworkEvent(event);
 	}
 
 	public void addSelectedPerson(Person person) {
 		getPersonEvents(person);
 	}
 
 	public void removeSelectedPerson(Person person) {
 		selectedPersonsEvents.remove(person.getUsername());
 		selectedPersons.remove(person.getUsername());
 //		personColors.remove(person.getUsername());
 	}
 
 	public void update() {
 		calendarWindow.removeEvents();
 		calendarWindow.updateLabels();
 		for (String username : selectedPersonsEvents.keySet()) {
 			Color color;
 			if(username.equals(Singleton.getInstance().getSelf().getUsername())) {
 				color = Singleton.SELF_COLOR;
 			}
 			else if(personColors.containsKey(username)) {
 				color = personColors.get(username);				
 			} else {
 				color = Singleton.COLOR_ARRAY[personColors.size()%Singleton.COLOR_ARRAY.length];
 				personColors.put(username, color);
 			}
 			addEventsArrayList(selectedPersonsEvents.get(username), username, color);
 		}
 	}
 
 	public void nextWeek() {
 		if (currentDisplayedWeek < 52) {
 			currentDisplayedWeek++;
 		} else
 			currentDisplayedWeek = 1;
 		currentDisplayedDate = DateManagement.getDateFromString(DateManagement.getNextWeek(currentDisplayedDate));
 	}
 
 	public void prevWeek() {
 		if (currentDisplayedWeek > 1) {
 			currentDisplayedWeek--;
 		} else
 			currentDisplayedWeek = 52;
 		currentDisplayedDate = DateManagement.getDateFromString(DateManagement.getPrevWeek(currentDisplayedDate));
 	}
 
 	@Override
 	public void receiveNetworkEvent(NetworkEvent event) {
 		switch (event.getType()) {
 
 		case QUERY:
 			evaluateQueryEvent((QueryEvent) event);
 			break;
 		}
 	}
 
 	private void evaluateQueryEvent(QueryEvent queryEvent) {
 		switch (queryEvent.getQueryType()) {
 		case CALEVENTS:
 			addEvents(queryEvent);
 			break;
 		case PERSONS:
 			addOtherPersons(queryEvent);
 			break;
 		}
 	}
 
 	@Override
 	public void setView(ChronosWindow calendarWindow) {
 		this.calendarWindow = (CalendarWindow) calendarWindow;
 	}
 }
