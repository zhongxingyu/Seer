 package model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class EventModel {
 	
 	private Event event; 
 	private Person user;
 	private boolean editable;
 	private PropertyChangeSupport pcs; 
 	private ArrayList<HaveCalendar> inviteList;
 	
 	public EventModel(Event event, Person user){
 		this.event = event;
 		this.user = user;
 		editable = false;
 		pcs = new PropertyChangeSupport(this); 
 	}
 
 	public Event getEvent(){
 		return this.event;
 	}
 	
 	public String getStartTime(){
 		return getTimeString(event.getStartTime()); 
 	}
 	public void setStartTime(String startTimeString){
 		event.setStartTime(getTimestamp(startTimeString));
 	}
 	
 	public String getEndTime(){
 		return getTimeString(event.getEndTime()); 
 	}
 	public void setEndTime(String endTimeString){
 		event.setEndTime(getTimestamp(endTimeString));
 	}
 	
 	public Timestamp getTimestamp(String timeString) {
 		
 		String[] time_date = timeString.split(" ");
 		String[] time = time_date[0].split(":");
 		String[] date = time_date[1].split("\\.");
 
 		
 		int year = Integer.parseInt(date[2])-1900;
 		int month = Integer.parseInt(date[1])-1;
 		int day = Integer.parseInt(date[0]);
 		
 		int hour = Integer.parseInt(time[0]); 
 		int minute = Integer.parseInt(time[1]);
 		
 		Timestamp timestamp = new Timestamp(year, month, day, hour, minute, 0, 0); 
 		return timestamp;
 	}
 	
 	public String getPlace(){
 		if (event.getRoom() != null){
 			return "Rom " + event.getRoom().getRoomNr();
 		}else{
 			return event.getPlace(); 
 		}
 	}
 	
 	public RoomListModel getRoomListModel(){
 		ArrayList<Room> roomArray = new ArrayList<Room>();
 		roomArray.add(new Room("100",40));
 		roomArray.add(new Room("110",19));
 		roomArray.add(new Room("101",5));
 		
 		RoomListModel rlm = new RoomListModel(roomArray, event);
 		
 		return rlm;
 	}
 	
 	public String getName(){
 		return event.getName();
 	}
 	public void setName(String name){
 		event.setName(name); 
 	}
 	
 	public String getDescription() {
 		return event.getDescription();
 	}
 	public void setDescription(String description){
 		event.setDescription(description); 
 	}
 	
 	public String getAlarm(){
 		System.out.println(event.getAlarms());
 		if (event.getAlarms() != null){
 			int alarm = event.getAlarms().get(user);
 			return Integer.toString(alarm);
 		}
 		else{
 			return "No Alarm";
 		}
 	}
 	
 	public void setAlarm(int alarm){
 		event.setAlarm(user, alarm);
 	}
 	
 	public HashMap<Person, Timestamp> getMapAlarms(){
         if(event.getMapAlarm() == null){
                 return null;
         }
 
         return event.getMapAlarm();
 }       
                 
 	public void setMapAlarm(Person person, Timestamp alarm){
         event.setMapAlarm(person, alarm);
 	}	    
 
 
 	public ArrayList<HaveCalendar> getInviteList(){
 //		ArrayList<HaveCalendar> test = new ArrayList<HaveCalendar>();
 //		
 //		for (HaveCalendar hc : event.getAttenders()){
 //			test.add(hc);
 //		}
 //		
 //		Person p1 = new Person(); 
 //		p1.setName("Torstein"); 
 //		
 //		Person p2 = new Person(); 
 //		p2.setName("Adam");
 //		
 //		Person p3 = new Person(); 
 //		p3.setName("Per"); 
 //		
 //		Person p4 = new Person(); 
 //		p4.setName("Simon");
 //		
 //		Person p5 = new Person(); 
 //		p5.setName("Kristoffer");
 //		
 //		test.add(p1);
 //		test.add(p2);
 //		test.add(p3);
 //		test.add(p4);
 //		test.add(p5);
 		if (inviteList == null){
 			return new ArrayList<HaveCalendar>();
 		}
 		return inviteList;
 	}
 	
 	public void setInviteList(ArrayList<HaveCalendar> inviteList){
 		this.inviteList = inviteList; 
 	}
 
 
 	public ArrayList<HaveCalendar> getAttenders(){
 		
 		return event.getAttenders();
 		
 //		ArrayList<Person> test = new ArrayList<Person>();
 //		Person p1 = new Person(); 
 //		p1.setName("Per"); 
 //		
 //		Person p2 = new Person(); 
 //		p2.setName("Simon");
 //		
 //		Person p3 = new Person(); 
 //		p3.setName("Kristoffer");
 //		
 //		test.add(p1);
 //		test.add(p2);
 //		test.add(p3);
 //		return test;
 	}
 
 	public boolean getEditable(){
 		return editable;
 	}
 	
 	public void setEditeble(boolean editeble){
 //		if (this.editable == editeble){
 //			return;
 //		}
 		this.editable = editeble;
 		pcs.firePropertyChange("editeble", !editeble, editeble);
 	}
 
 
 	private String getTimeString(Timestamp date){
 		if (date == null){
 			return ""; 
 		}
 
 		String dateString = "";
 		
 		dateString += date.getHours()+":";
 		dateString += date.getMinutes()+" ";
 		dateString += date.getDate()+".";
 		dateString += (date.getMonth()+1)+".";
 		dateString += (date.getYear()+1900);
 		return dateString;
 	}
 	
 	public void addAttender(HaveCalendar hc){
 		event.addAttender(hc);
 		pcs.firePropertyChange("AddAttender", null, hc);
 	}
 	public void removeAttender(HaveCalendar hc) {
 		event.removeAttender(hc);
 		pcs.firePropertyChange("RemoveAttender", null, hc);
 	}
 	
 	public void addPropertyChangeListener(PropertyChangeListener listener){
 		pcs.addPropertyChangeListener(listener); 
 	}
 	public void removePropertyChangeListener(PropertyChangeListener listener){
 		pcs.removePropertyChangeListener(listener); 
 	}
 	
 	
 	public int getNumberOfAttenders(){
 		return event.getIsGoing().size(); 
 	}
 	public int getNumberOfNotAnsward(){
 		return event.getHasNotReplied().size();
 	}
 	public int getNumberOfDeclines(){
 		return event.getIsNotGoing().size(); 
 	}
 }
