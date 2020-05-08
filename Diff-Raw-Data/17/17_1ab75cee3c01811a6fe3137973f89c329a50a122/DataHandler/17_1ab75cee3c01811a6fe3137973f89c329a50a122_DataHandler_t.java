 package com.studentersamfundet.app;
 
 import java.util.LinkedList;
 
 public class DataHandler {
 	private LinkedList<Event> events = new LinkedList<Event>();
 
 	public void insert(String id, String title, String description, String date, String location, String text, String category) {
 		int intId = Integer.parseInt(id);
 		if (get(intId) != null)
 			return;
 		
 		Event e = new Event(intId, title, description, date, location, text, category);
 		events.add(e);
 	}
 	
 	public Event get(int id) {
 		for (Event e : events) {
 			if (e.id == id)
 				return e;
 		}
 		return null;
 	}
 	
 	public Event[] populateList(String category) {
 		LinkedList<Event> sorted = new LinkedList<Event>();
 
		if (category.equals("all")) {
 			return events.toArray(new Event[events.size()]);
		}
 
		for (Event event : events) {
			if (category.equals(event.category))
				sorted.add(event);
 		}
		return sorted.toArray(new Event[sorted.size()]);
     }
 }
