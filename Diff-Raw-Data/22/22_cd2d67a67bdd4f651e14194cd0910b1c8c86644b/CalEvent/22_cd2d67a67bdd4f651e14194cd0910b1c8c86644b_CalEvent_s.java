 package events;
 
 import java.util.Date;
 import java.util.HashMap;
 
 import client.model.EventConfigModel.ViewType;
 
 import chronos.Person;
 import chronos.Room;
 import chronos.Singleton;
 
 /**
  * Calendar event used for all calendar related thing (update, delete, add etc).
  * Holds a list over all participants (if any)
  */
 public class CalEvent extends NetworkEvent implements Comparable<CalEvent> {
 	public enum CalEventType {
 		UPDATE, NEW, DELETE;
 	}
 
 	private HashMap<String, Person> participants;
 
 	private Date startDate;
 	private int duration;
 	private CalEventType type;
 	private final Person creator;
 	private String title;
 	private String description;
 	private final long timestampPrimaryKey;
 	private Room room;
 
 	public CalEvent(Date startDate, int duration) {
		this(null, startDate, duration, Singleton.getInstance().getSelf(), null);
 	}
 
	public CalEvent(String title, Date startDate, int duration, Person creator, String description) {
 		this(title, startDate, duration, creator, description, 0);
 	}
 
 	public CalEvent(String title, Date startDate, int duration, Person creator, String description, long timestampPrimaryKey) {
 		super(EventType.CALENDAR);
 		setState(CalEventType.NEW);
 		this.title = title;
 		this.creator = creator;
 		this.startDate = startDate;
 		this.duration = duration;
 		participants = new HashMap<String, Person>();
 		this.description = description;
 
 		this.timestampPrimaryKey = (timestampPrimaryKey == 0) ? System.currentTimeMillis() : timestampPrimaryKey;
 		addParticipant(creator);
 	}
 
	public CalEvent update(String title, Date start, int duration, String description) {
 		setState(CalEventType.UPDATE);
 		this.title = title;
 		this.startDate = start;
 		this.duration = duration;
 		this.description = description;
 		return this;
 	}
 
 	public CalEvent addParticipant(Person... person) {
 		for (int i = 0; i < person.length; i++)
 			participants.put(person[i].getUsername(), person[i]);
 		return this;
 	}
 
 	public CalEvent addParticipants(HashMap<String, Person> participants) {
 		for (Person person : participants.values())
 			addParticipant(person);
 		return this;
 	}
 
 	@Override
 	public String toString() {
 		String people = null;
 		for (String person : participants.keySet()) {
 			people += person + ", ";
 		}
 		if (people != null)
 			people = people.subSequence(0, people.length() - 3).toString();
 		return "[CalEvent] Participants: " + people;
 	}
 
 	public HashMap<String, Person> getParticipants() {
 		return participants;
 	}
 
 	public CalEvent setState(CalEventType type) {
 		this.type = type;
 		return this;
 	}
 
 	public CalEventType getState() {
 		return type;
 	}
 
 	public Date getStart() {
 		return startDate;
 	}
 
 	public int getDuration() {
 		return duration;
 	}
 
 	public Person getCreator() {
 		return creator;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public Room getRoom() {
 		return room;
 	}
 
 	public CalEvent setRoom(Room room) {
 		this.room = room;
 		return this;
 	}
 
 	public void setDuration(int duration) {
 		this.duration = duration;
 	}
 
 	public void setStart(Date start) {
 		this.startDate = start;
 	}
 
 	/**
 	 * used as primary key in the db, HATERS GONNA' HATE
 	 */
 	public long getTimestamp() {
 		return timestampPrimaryKey;
 	}
 
 	public CalEvent setParticipants(HashMap<String, Person> participants) {
 		this.participants = participants;
 		return this;
 	}
 
 	@Override
 	public int compareTo(CalEvent otherEvent) {
 		return (int) ((startDate.getTime() - otherEvent.getStart().getTime()) / 1000);
 	}
 
 	public CalEvent setState(ViewType viewType) {
 		if (viewType == ViewType.UPDATE)
 			setState(CalEventType.UPDATE);
 		else if (viewType == ViewType.NEW)
 			setState(CalEventType.NEW);
 		return this;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 }
