 package client.model;
 
 import java.awt.Color;
 import java.util.Date;
 
 import chronos.DateManagement;
 import chronos.Person;
 import chronos.Room;
 import chronos.Singleton;
 import client.ClientController;
 import client.gui.view.ChronosWindow;
 import client.gui.view.EventConfigWindow;
 import events.CalEvent;
 import events.NetworkEvent;
 import events.CalEvent.CalEventType;
 
 public class EventConfigModel extends ChronosModel {
 
 	private EventConfigWindow eventConfigWindow;
 
 	private ConfigState state;
 	private EventConfigWindow view;
 
 	/**
 	 * Input variables from view
 	 */
 	private Room room;
 	private String eventDescription;
 	private String eventName;
 	private Date startDate;
 	private int duration;
 
 	private String startTime;
 
 	private CalEvent event;
 
 	public enum ConfigState {
 		EDIT, NEW, VIEW;
 	}
 
 	public EventConfigModel(ClientController controller) {
 		super(controller, ChronosType.EVENT_CONFIG);
 	}
 
 	public void setCalEvent(CalEvent event) {
 		this.event = event;
 		view.getEventNameField().setText(event.getTitle());
 		view.getEventDescriptionArea().setText(event.getDescription());
 		view.getStartDateField().setText(DateManagement.getFormattedDate(event.getStart()));
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
 
 	public Date getStartTime() {
 		return startDate;
 	}
 
 	public void setStartTime(Date start) {
 		this.startDate = start;
 	}
 
 	public int getDuration() {
 		return duration;
 	}
 
 	public void setDuration(int duration) {
 		this.duration = duration;
 	}
 
 	// State of window
 	public void setState(ConfigState state) {
 		this.state = state;
 	}
 
 	public ConfigState getState() {
 		return state;
 	}
 
 	public void editEvent() {
 		setState(ConfigState.EDIT);
 	}
 
 	private boolean validateInput() {
 		eventName = view.getEventNameField().getText();
 		eventDescription = view.getEventDescriptionArea().getText();
 		startDate = DateManagement.getDateFromString(view.getStartDateField().getText());
 		// startTime =
 		// DateManagement.getDateFromString(view.getStartTimeArray().getText());
 		room = null;
 		try {
 			room = new Room(null, Integer.parseInt(view.getRoomNumberField().getText()), null);
 		} catch (Exception e) {
 		}
 		duration = -1;
 		try {
 			// duration = Integer.parseInt(view.getDurationField().getText());
 			duration = 10;
 		} catch (Exception e) {
 		}
 		boolean alert = view.getAlert().isSelected();
 
 		view.getEventNameField().setBackground(eventName == null ? Color.red : Color.green);
 		view.getEventDescriptionArea().setBackground(eventDescription == null ? Color.red : Color.green);
 		view.getStartDateField().setBackground(startDate == null ? Color.red : Color.green);
 		// view.getStartTimeArray().setBackground(startTime == null ? Color.red
 		// : Color.green);
 		// view.getDurationArray().setBackground(duration == -1 ? Color.red :
 		// Color.green);
 		// ArrayList<Person> participants = view.getParticipantList();
 
 		return eventName != null && eventDescription != null && startDate != null && duration != -1;
 	}
 
 	public void newCalEvent() {
 		// Person user1 = new Person("herp");
 		// Person user2 = new Person("derp");
 		// Person user3 = new Person("snerp");
 		Person creator = Singleton.getInstance().getSelf();
 		creator.setStatus(Person.Status.ACCEPTED);
 		CalEvent event;
 		if (validateInput()) {
 			event = new CalEvent(getEventName(), getStartTime(), getDuration(), creator, getEventDescription()).setState(CalEventType.NEW);
 			fireNetworkEvent(event);
 		}
 	}
 
 	@Override
 	public void receiveNetworkEvent(NetworkEvent event) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void setView(ChronosWindow view) {
 		this.view = (EventConfigWindow) view;
 	}
 
 	public void setDefaultModel() {
 		view.getEventNameField().setText("Event name");
 		view.getEventNameField().setBackground(Color.white);
 
 		// view.getDurationArray().setText("10");
 		// view.getDurationArray().setBackground(Color.white);
 
 		view.getEventDescriptionArea().setText("Description");
 		view.getEventDescriptionArea().setBackground(Color.white);
 
 		view.getStartDateField().setText(DateManagement.getFormattedDate(new Date()));
 		view.getStartDateField().setBackground(Color.white);
 
 		// view.getStartTimeArray().setText("10");
 		// view.getStartTimeArray().setBackground(Color.white);
 
 		view.getAlert().setSelected(false);
 	}
 
 	public void removeEvent() {
 		event.setState(CalEventType.DELETE);
 		view.setVisible(false);
 		fireNetworkEvent(event);
 	}
 }
