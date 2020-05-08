 package client.model;
 
 import java.util.Date;
 import java.util.HashMap;
 
 import chronos.DateManagement;
 import chronos.Person;
 import chronos.Room;
 import chronos.Singleton;
 import client.ClientController;
 import client.gui.view.ChronosWindow;
 import client.gui.view.eventConfig.EventWindow;
 import events.CalEvent;
 import events.NetworkEvent;
 import events.CalEvent.CalEventType;
 
 public class EventConfigModel extends ChronosModel {
 	public enum ViewType {
 		OTHER, ADMIN, PARTICIPANT, NEW, INVITE, UPDATE;
 	}
 
 	/**
 	 * Input variables from view
 	 */
 	private Room room;
 	private String eventDescription;
 	private String eventName;
 	private Date startDate;
 	private int duration;
 	private Person creator;
 
 	private String startTime;
 
 	private CalEvent event;
 
 	private HashMap<String, Person> participants;
 	private HashMap<ViewType, EventWindow> eventViews;
 
 	private String description;
 
 	private String formattedStartDate;
 
 	private boolean alert;
 
 	public EventConfigModel(ClientController controller) {
 		super(controller, ChronosType.EVENT_CONFIG);
 		eventViews = new HashMap<ViewType, EventWindow>();
 	}
 
 	// State of window
 
 	private boolean validateInput(EventWindow view) {
 		String eventName = view.getEventNameField().getText();
 		String eventDescription = view.getEventDescriptionArea().getText();
 		Date startDate = DateManagement.getDateFromString(view.getStartDateField().getText());
 		// startTime =
 		// DateManagement.getDateFromString(view.getStartTimeArray().getText());
 		Room room = null;
 		try {
 			room = new Room(null, Integer.parseInt(view.getRoomNumberField().getText()), null);
 		} catch (Exception e) {
 		}
 		int duration = -1;
 		try {
 			// duration = Integer.parseInt(view.getDurationField().getText());
 			duration = 10;
 		} catch (Exception e) {
 		}
 
 		view.getEventNameField().setBackground(eventName == null ? Singleton.LIGHT_RED : Singleton.LIGHT_GREEN);
 		view.getEventDescriptionArea().setBackground(eventDescription == null ? Singleton.LIGHT_RED : Singleton.LIGHT_GREEN);
 		view.getStartDateField().setBackground(startDate == null ? Singleton.LIGHT_RED : Singleton.LIGHT_GREEN);
 
 		return eventName != null && eventDescription != null && startDate != null && duration != -1;
 	}
 
 	public void setDefaultModel() {
 		setEventName("Event name");
 		setDescription("Description");
 		setFormattedStartDate(DateManagement.getFormattedDate(new Date()));
 		setAlert(false);
 		setDuration(1);
 		setStartTime("12:00");
 		setParticipants(new HashMap<String, Person>());
 	}
 
 	public void setCalEvent(CalEvent event) {
 		this.event = event;
 		setEventName(event.getTitle());
 		setDescription(event.getDescription());
 		setStartDate(event.getStart());
 		setAlert(event.getAlert());
 		setDuration(event.getDuration());
 		setFormattedStartDate(DateManagement.getFormattedDate(event.getStart()));
 		setStartTime(DateManagement.getTimeOfDay(event.getStart()));
 		setParticipants(event.getParticipants());
 		setCreator(event.getCreator());
 	}
 
 	private void updateViews() {
 		for (EventWindow view : eventViews.values()) {
 			view.getEventNameField().setText(getEventName());
 			view.getEventNameField().setBackground(Singleton.BACKGROUND);
 			view.getEventDescriptionArea().setText(getEventDescription());
 			view.getEventDescriptionArea().setBackground(Singleton.BACKGROUND);
 			view.getStartDateField().setBackground(Singleton.BACKGROUND);
 			view.getStartTimeField().setText(getStartTime());
 			view.getStartTimeField().setBackground(Singleton.BACKGROUND);
 			view.setParticipants(getParticipants());
 			view.getStartDateField().setText(getFormattedStartDate());
 			view.getDuration().setSelectedItem(getDuration());
			view.getCreatorField().setText(getCreator().toString());
 		}
 	}
 
 	public void updatePerticipants() {
 		for (EventWindow view : eventViews.values()) {
 			view.setParticipants(getParticipants());
 		}
 	}
 
 	public void updateModel(EventWindow view) {
 		setEventName(event.getTitle());
 		setDescription(event.getDescription());
 		setStartDate(event.getStart());
 		setAlert(event.getAlert());
 		setDuration(event.getDuration());
 		setStartTime(DateManagement.getTimeOfDay(event.getStart()));
 		setParticipants(event.getParticipants());
 	}
 
 	public void newCalEvent(EventWindow view) {
 		Person creator = Singleton.getInstance().getSelf();
 		String title = view.getEventNameField().getText();
 		String description = view.getEventDescriptionArea().getText();
 		Date startDate = DateManagement.getDateFromString(view.getStartDateField().getText());
 		int duration = (int) view.getDuration().getSelectedItem();
 
 		creator.setStatus(Person.Status.ACCEPTED);
 		if (view.getViewType() == ViewType.UPDATE) {
 			event.update(title, startDate, duration, description).setParticipants(getParticipants());
 		} else
 			event = new CalEvent(title, startDate, duration, creator, description).addParticipants(getParticipants());
 		if (validateInput(view)) {
 			fireNetworkEvent(event);
 			view.setVisible(false);
 		}
 	}
 
 	private Date getStartDate() {
 		return DateManagement.getDateFromString(getFormattedStartDate());
 	}
 
 	@Override
 	public void receiveNetworkEvent(NetworkEvent event) {
 	}
 
 	public void setView(ChronosWindow view, ViewType type) {
 		eventViews.put(type, (EventWindow) view);
 	}
 
 	public String getFormattedStartDate() {
 		return formattedStartDate;
 	}
 
 	public void removeEvent(EventWindow view) {
 		event.setState(CalEventType.DELETE);
 		view.setVisible(false);
 		fireNetworkEvent(event);
 	}
 
 	public void setParticipants(HashMap<String, Person> participants) {
 		this.participants = participants;
 	}
 
 	public void setView(ViewType type) {
 		updateViews();
 		eventViews.get(type).setVisible(true);
 		hideAllBut(type);
 	}
 
 	private void hideAllBut(ViewType type) {
 		for (EventWindow view : eventViews.values()) {
 			if (view.getViewType() != type)
 				view.setVisible(false);
 		}
 	}
 
 	private void setAlert(boolean alert) {
 		this.alert = alert;
 	}
 
 	public void setFormattedStartDate(String formattedStartDate) {
 		this.formattedStartDate = formattedStartDate;
 	}
 
 	private void setStartDate(Date startDate) {
 		this.startDate = startDate;
 	}
 
 	private void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getEventName() {
 		return eventName;
 	}
 
 	public void setEventName(String eventName) {
 		this.eventName = eventName;
 	}
 
 	public String getEventDescription() {
 		return eventDescription;
 	}
 
 	public void setEventDescription(String eventDescription) {
 		this.eventDescription = eventDescription;
 	}
 
 	public String getStartTime() {
 		return startTime;
 	}
 
 	public void setStartTime(String string) {
 		this.startTime = string;
 	}
 
 	public int getDuration() {
 		return duration;
 	}
 
 	public void setDuration(int duration) {
 		this.duration = duration;
 	}
 
 	public CalEvent getCalEvent() {
 		return event;
 	}
 
 	public HashMap<ViewType, EventWindow> getEventViews() {
 		return eventViews;
 	}
 
 	@Override
 	public void setView(ChronosWindow view) {
 	}
 
 	public HashMap<String, Person> getParticipants() {
 		return participants;
 	}
 
 	public Person getCreator() {
 		return creator;
 	}
 
 	public void setCreator(Person creator) {
 		this.creator = creator;
 	}
 }
