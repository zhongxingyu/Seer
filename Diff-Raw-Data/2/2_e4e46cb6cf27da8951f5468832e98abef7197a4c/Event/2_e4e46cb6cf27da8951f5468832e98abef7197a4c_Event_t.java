 package com.appspot.iclifeplanning.events;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import com.appspot.analyser.BaseCalendarSlot;
 import com.appspot.analyser.IEvent;
 import com.appspot.analyser.Pair;
 import com.appspot.datastore.SphereName;
 import com.google.gdata.data.DateTime;
 import com.google.gdata.data.TextConstruct;
 import com.google.gdata.data.calendar.CalendarEventEntry;
 
 // TODO (amadurska): Ensure keywords come from both title & description
 // TODO (amadurska): Keywords should be really replaced by spheres.
 public class Event extends BaseCalendarSlot implements IEvent {
 	private CalendarEventEntry calendarEventEntry;
 	private DateTime startTime;
 	private DateTime endTime;
 	private TextConstruct description;
 	private Set<String> keywords = new HashSet<String>();
 	private Set<Event> childEvents;
 	private String calendarTitle;
 	private String id;
 	private boolean canReschedule;
 	private boolean isRecurring;
 	private static final Logger log = Logger.getLogger("EventStore");
 
 	public Event(CalendarEventEntry calendarEventEntry) {
 		super(calendarEventEntry);
 		this.calendarEventEntry = calendarEventEntry;
 		childEvents = null;
 		id = calendarEventEntry.getId();
 		canReschedule = calendarEventEntry.getCanEdit();
 		isRecurring = calendarEventEntry.getRecurrence() != null;
 		parseKeywords(description);
 		log.severe("Sphere: " + getSpheres());
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	private void parseKeywords(TextConstruct description) {
 		String[] words = description.getPlainText().split("[\\s]+");
 		
 		for(int i = 0; i < words.length; i++) {
 			if (isKeyword(words[i])) {
 				keywords.add(words[i]);
 			}
 		}
 	}
 
 	public Set<String> getKeywords() {
 		return keywords;
 	}
 
 	public void setKeywords(Set<String> keywords) {
 		this.keywords = keywords;
 	}
 
 	public Set<Event> getChildEvents() {
 		return childEvents;
 	}
 
 	public void setChildEvents(Set<Event> childEvents) {
 		this.childEvents = childEvents;
 	}
 
 	private boolean isKeyword(String string) {
 		return true;
 	}
 //zobaczyc czy dziala
 	public Map<SphereName, Double> getSpheres() {
 		Map<String, Double> tmp = UClasifier.analyse(description);
 		Map<SphereName, Double> res = new HashMap<SphereName, Double>();
 		for(String key : tmp.keySet()){		
 			for(SphereName name : SphereName.values()){
 				if(key.equalsIgnoreCase(name.toString())){
 					res.put(name, tmp.get(key));
 					break;
 				}
 			}
 		}
 		return res;
 	}
 
 	public boolean canReschedule() {
 		return canReschedule;
 	}
 
 	public boolean isRecurring() {
 		return isRecurring;
 	}
 
 	public double minDuration() {
 		return 0;
 	}
 
 	public double maxDuration() {
 		return 0;
 	}
 
 	@Override
	public Pair<Double, Double> getDurationInterval() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void makePersistent() {
 		// TODO Auto-generated method stub
 		
 	}
 }
