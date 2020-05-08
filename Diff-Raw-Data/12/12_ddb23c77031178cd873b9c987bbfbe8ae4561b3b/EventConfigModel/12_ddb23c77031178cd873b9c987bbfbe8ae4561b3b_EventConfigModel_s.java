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
 	private boolean alert;
 	private int duration;
 	private Date startDate;
 	private String description;
 	private String title;
 	private Person creator;
 
 	private CalEvent event;
 
 	private HashMap<String, Person> participants;
 	private HashMap<ViewType, EventWindow> eventViews;
 
 	public EventConfigModel(ClientController controller) {
 		super(controller, ChronosType.EVENT_CONFIG);
 		eventViews = new HashMap<ViewType, EventWindow>();
 	}
 
 	// State of window
 
 	private boolean validateInput(EventWindow view) {
 		String eventName = view.getEventNameField().getText();
 		String eventDescription = view.getEventDescriptionArea().getText();
 		Room room = null;
 		try {
 			room = new Room(null, Integer.parseInt(view.getRoomNumberField().getText()), null);
 		} catch (Exception e) {
 		}
 
 		view.getEventNameField().setBackground(eventName == null ? Singleton.RED : Singleton.GREEN);
 		view.getEventDescriptionArea().setBackground(eventDescription == null ? Singleton.RED : Singleton.GREEN);
 
 		return eventName != null && eventDescription != null;
 	}
 
 	public void setDefaultModel() {
 		setTitle("Event name");
 		setDescription("Description");
 		setAlert(false);
 		setDuration(1);
 		setStartDate(new Date());
 		setParticipants(new HashMap<String, Person>());
 	}
 
 	public void setCalEvent(CalEvent event) {
 		this.event = event;
 		setTitle(event.getTitle());
 		setDescription(event.getDescription());
 		try {
 			setAlert(event.getParticipants().get(Singleton.getInstance().getSelf().getUsername()).getAlert());
 		} catch (Exception e) {
 		}
 		setDuration(event.getDuration());
 		setStartDate(event.getStart());
 		setParticipants(event.getParticipants());
 		setCreator(event.getCreator());
 	}
 
 	private void updateViews() {
 		for (EventWindow view : eventViews.values()) {
 			view.getEventNameField().setText(getTitle());
 			view.getEventNameField().setBackground(Singleton.BACKGROUND);
 			view.getEventDescriptionArea().setText(getDescription());
 			view.getEventDescriptionArea().setBackground(Singleton.BACKGROUND);
 			view.setParticipants(getParticipants());
 			view.getStartTime().setValue(getStartDate());
 			view.getStartDate().setValue(DateManagement.stripClock(getStartDate()));
 			view.getDuration().setSelectedItem(getDuration());
 			view.getRoomNumberField().setText(getRoom() == null ? "" : getRoom().getName());
 			view.getAlert().setSelected(getAlert());
 			if (getCreator() != null)
 				view.getCreatorField().setText(getCreator().toString());
 		}
 	}
 
 	public void updatePerticipants() {
 		for (EventWindow view : eventViews.values()) {
 			view.setParticipants(getParticipants());
 		}
 	}
 
 	public void updateRoom() {
 		for (EventWindow view : eventViews.values())
 			view.getRoomNumberField().setText(getRoom() == null ? "" : getRoom().getName());
 	}
 
 	public void updateModel(EventWindow view) {
 		setTitle(event.getTitle());
 		setDescription(event.getDescription());
 		setAlert(event.getParticipants().get(Singleton.getInstance().getSelf().getUsername()).getAlert());
 		setDuration(event.getDuration());
 		setStartDate(event.getStart());
 		setParticipants(event.getParticipants());
 	}
 
 	public void newCalEvent(EventWindow view) {
 		creator = Singleton.getInstance().getSelf();
 		creator.setStatus(Person.Status.ACCEPTED);
 		title = view.getEventNameField().getText();
 		description = view.getEventDescriptionArea().getText();
 		duration = (int) view.getDuration().getSelectedItem();
 
 		Date date = (Date) view.getStartDate().getValue();
 		Date time = (Date) view.getStartTime().getValue();
 		long clock = DateManagement.getClockInSeconds(time);
		event.getParticipants().get(Singleton.getInstance().getSelf().getUsername()).setAlert(view.getAlert().isSelected());
 
 		startDate = new Date(date.getTime() + clock);
 
 		if (view.getViewType() == ViewType.UPDATE || view.getViewType() == ViewType.PARTICIPANT) {
 			event.update(title, startDate, duration, description).setParticipants(getParticipants());
 		} else
 			event = new CalEvent(title, startDate, duration, creator, description).addParticipants(getParticipants());
 		if (validateInput(view)) {
 			fireNetworkEvent(event);
 			view.setVisible(false);
 		}
 	}
 
 	@Override
 	public void receiveNetworkEvent(NetworkEvent event) {
 	}
 
 	public void setView(ChronosWindow view, ViewType type) {
 		eventViews.put(type, (EventWindow) view);
 	}
 
 	public void removeEvent(EventWindow view) {
 		event.setState(CalEventType.DELETE);
 		event.setSender(Singleton.getInstance().getSelf());
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
 
 	public boolean getAlert() {
 		return alert;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public void setDescription(String eventDescription) {
 		this.description = eventDescription;
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
 
 	public Date getStartDate() {
 		return startDate;
 	}
 
 	public void setStartDate(Date startDate) {
 		this.startDate = startDate;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getTitle() {
 		return title;
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
 
 	public Room getRoom() {
 		return room;
 	}
 
 	public void setRoom(Room room) {
 		this.room = room;
 	}
 
 	@Override
 	public void setView(ChronosWindow view) {
 	}
 }
