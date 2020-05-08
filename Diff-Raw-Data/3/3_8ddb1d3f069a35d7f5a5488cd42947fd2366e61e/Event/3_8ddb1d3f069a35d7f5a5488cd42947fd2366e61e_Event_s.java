 package model;
 
 import java.sql.Time;
 import java.util.ArrayList;
 
 public class Event {
 	
 	private String eventId, name, description, place;
 	private Room room;
 	private Time time;
 	private Boolean isActive;
 	private ArrayList<HaveCalendar> attenders;
 	
 	public String getEventId() {
 		return eventId;
 	}
 
 	public void setEventId(String eventId) {
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
 
 	public Time getTime() {
 		return time;
 	}
 
 	public void setTime(Time time) {
 		this.time = time;
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
 		return attenders;
 	}
 	
 	public void addAttender(HaveCalendar haveCalendar){
 		attenders.add(haveCalendar); 
 	}
 	
 	public void removeAttender(HaveCalendar haveCalendar){
 		attenders.remove(haveCalendar); 
 	}
 	
 	public int getNumberOfAttenders(){
 		return attenders.size();
 	}
 
 	public Event() {
 		
 	}
 	
 	
 }
