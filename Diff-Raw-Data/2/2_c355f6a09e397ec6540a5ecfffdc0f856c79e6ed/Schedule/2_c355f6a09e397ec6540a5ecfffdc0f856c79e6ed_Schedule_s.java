 package fi.helsinki.cs.scheduler3000.model;
 
 /**
  * @author Team TA's
  */
 
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import fi.helsinki.cs.scheduler3000.model.Weekday.Day;
 
 public class Schedule implements Serializable {
 	
     private String period;
     private HashMap<Day, ArrayList<Event>> schedule;
 
     // CONSTRUCTORS
     
     public Schedule(){ 
     	this.period = null;
     	this.schedule = null;
     }
     
     public Schedule(ArrayList<Day> week){
         this.setSchedule(week);
         this.period = null;
     }
     
     public Schedule(ArrayList<Day> week, String period){
     	this.setSchedule(week);
     	this.period = period;
     }
 
     // GETTERS AND SETTERS
     
  
     public void setSchedule(ArrayList<Day> newSchedule){
     	if (this.schedule == null){
     		this.schedule = new HashMap<Day, ArrayList<Event>>();
     	}
     	
     	// build schedule from given week, initialize empty event-arrays
         for (Day day : newSchedule){
         	this.schedule.put(day, new ArrayList<Event>());
         }
     }
     
     public void setSchedule(HashMap<Day, ArrayList<Event>> newSchedule){
     	this.schedule = newSchedule;
     }
     
     public void setSchedule(Schedule newSchedule) {
 		this.schedule = newSchedule.getSchedule();
 		this.period = newSchedule.getPeriod();
 	}
 
     private HashMap<Day, ArrayList<Event>> getSchedule(){
 		return this.schedule; 
 	}
     
     public Collection<Event> getEventsOn(Day day) {
    	if( this.schedule.containsKey(day) ) {
     		throw new IllegalArgumentException("Date not in this schedule");
     	}
     	return this.schedule.get(day);
     }
     
     public Collection<Day> getDays() {
     	return this.schedule.keySet();
     }
 
 	public String getPeriod() {
 		return period;
 	}
 
 	public void setPeriod(String period) {
 		this.period = period;
 	}
     
     // OTHER
 	public void addEvent(Event event) {
 		Weekday.Day eventDay = event.getDay();
 		if (!this.schedule.containsKey(eventDay)){ // if adding an event to non-existent date
 			throw new IllegalArgumentException("No such date in the schedule");
 		}
 		this.schedule.get(eventDay).add(event);
 	}
 
 
 }
