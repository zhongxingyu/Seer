 package model;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry.Entry;
 
 import net.DBMethods;
 
 import model.HaveCalendar;
 import model.Person;
 import model.Room;
 
 public class Event implements java.io.Serializable {
 	
 	private String name, description, place;
 	private Person createdBy;
 	private int eventId;
 	private Room room;
 	private Timestamp startTime, endTime;
 	private Boolean isActive;
 	private ArrayList<HaveCalendar> attenders;
 	private ArrayList<String> isGoing, isNotGoing, hasNotReplied = new ArrayList<String>();
 	private HashMap<Person, Integer> alarms;
 	
 	private HashMap<Person, Timestamp> mapAlarms;
 	
 	public Event(int eventId, Person createdBy, Timestamp startTime, Timestamp endTime, String name, 
 	String description, String place, Room room, ArrayList<HaveCalendar> attenders) {
 		this.eventId = eventId;
 		this.createdBy = createdBy;
 		this.startTime = startTime;
 		this.endTime = endTime;
 		this.name = name;
 		this.description = description;
 		this.place = place;
 		this.room = room;
 		this.attenders = attenders;
 		this.alarms = new HashMap<Person, Integer>();
 	}
 	
 	public Event(){
 		
 	}
 	
 	public void setIsGoing(ArrayList<String> isGoing){
 		this.isGoing = isGoing;
 	}
 	public void setIsNotGoing(ArrayList<String> isNotGoing){
 		this.isNotGoing = isNotGoing;
 	}
 	public void setHasNotReplied(ArrayList<String> hasNotReplied){
 		this.hasNotReplied = hasNotReplied;
 	}
 	public ArrayList<String> getIsGoing(){
 		return isGoing;
 	}
 	public ArrayList<String> getIsNotGoing(){
 		return isNotGoing;
 	}
 	public ArrayList<String> getHasNotReplied(){
 		return hasNotReplied;
 	}
 	
 	
 	public void addIsGoing(String p){
 		isGoing.add(p);
 	}
 	
 	public void setCreatedBy(Person createdBy){
 		this.createdBy = createdBy;
 	}
 	public Person getCreatedBy(){
 		return createdBy;
 	}
 	
 	public int getEventId() {
 		return eventId;
 	}
 
 	public void setEventId(int eventId) {
 		this.eventId = eventId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getPlace() {
 		return place;
 	}
 
 	public void setPlace(String place) {
 		this.place = place;
 	}
 
 	public Room getRoom() {
 		return room;
 	}
 
 	public void setRoom(Room room) {
 		this.room = room;
 	}
 
 	public Timestamp getStartTime() {
 		return startTime;
 	}
 
 	public void setStartTime(Timestamp time) {
 		this.startTime = time;
 	}
 	public Timestamp getEndTime(){
 		return endTime;
 	}
 	public void setEndTime(Timestamp time){
 		this.endTime = time;
 	}
 
 	public Boolean getIsActive() {
 		return isActive;
 	}
 
 	public void setIsActive(Boolean isActive) {
 		this.isActive = isActive;
 	}
 	
 	public void setAttenders(ArrayList<HaveCalendar> attenders){
 		this.attenders = attenders;
 	}
 	
 	public ArrayList<HaveCalendar> getAttenders(){
 		if (attenders != null){
 			return attenders;
 		}
 		else{
 			return new ArrayList<HaveCalendar>();
 		}
 	}
 	
 	public void addAttender(HaveCalendar haveCalendar){
 		if (haveCalendar == null){
 			return;
 		}
 		if (attenders == null){
 			attenders = new ArrayList<HaveCalendar>();
 		}
 		attenders.add(haveCalendar); 
 		if (hasNotReplied == null){
 			hasNotReplied = new ArrayList<String>();
 		}
 		System.out.println("Legger til i hasNotReplied (Event)");
 		hasNotReplied.add(haveCalendar.getName());
 	}
 	
 	public void removeAttender(HaveCalendar haveCalendar){
 		if (haveCalendar == null){
 			return;
 		}
		if (attenders == null){
			attenders = new ArrayList<HaveCalendar>();
		}
 		attenders.remove(haveCalendar);
 		String name = haveCalendar.toString();
 		if (isNotGoing.contains(name)){
 			isNotGoing.remove(name); 
 		}else if (isGoing.contains(name)){
 			isGoing.remove(name);
 		}else if (hasNotReplied.contains(name)){
 			hasNotReplied.remove(name);
 		}else {
 			System.out.println("Trying to remove an haveCalendar, that dosnt exsist, from string lists (model.event.removeAttender())");
 		}
 	}
 	
 	public int getNumberOfAttenders(){
 		if (attenders != null){
 			return attenders.size();
 		}
 		else{
 			return 0;
 		}
 	}
 
 	public void setAlarm(Person person, int alarm) {
 		       alarms.put(person, alarm);
 		      }
 	
 	public HashMap<Person, Integer> getAlarms() {
 	      return alarms;
 	}
 	
 	public void setMapAlarm(Person person, Timestamp alarm) {
         if (mapAlarms == null){
                 mapAlarms = new HashMap<Person, Timestamp>();
         }
         mapAlarms.put(person, alarm);
 }
 
 	public HashMap<Person, Timestamp> getMapAlarm() {
 
         return (mapAlarms == null) ? null:mapAlarms;
 
 	}
 	
 	public ArrayList<String> getAttendingList(){
 		ArrayList<String> attendingList = new ArrayList<String>(); 
 		
 		if(hasNotReplied == null){
 			hasNotReplied = new ArrayList<String>();
 		}
 		if (isGoing == null){
 			isGoing = new ArrayList<String>();
 		}
 		if (isNotGoing == null){
 			isNotGoing = new ArrayList<String>();
 		}
 		
 		System.out.println(hasNotReplied);
 		
 		
 		attendingList.addAll(hasNotReplied); 
 		attendingList.addAll(isGoing); 
 		attendingList.addAll(isNotGoing); 
 		
 		return attendingList; 
 	}
 
 	
 	
 }
