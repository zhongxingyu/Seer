 package processing;
 import java.util.ArrayList;
 import java.util.List;
 import org.joda.time.DateTimeComparator;
 import model.Event;
 
 public class StartTimeSorter implements ISorter {
 	
	//if event1 start time occurs before event2 start time return true; else false.
 	public boolean compareStartTime(Event event1, Event event2) {
 		DateTimeComparator comparator = DateTimeComparator.getInstance();
 		return !(comparator.compare(event1.getStartTime(), event2.getStartTime()) > 0);
 	}
 	
 	public List<Event> sort(List<Event> eventList) {
 		List<Event> filteredEvents = new ArrayList<Event>();
 		for (Event event : eventList) {
 			for (int index = 0; index <= filteredEvents.size(); index++) {
 				if (index == filteredEvents.size() || compareStartTime(event, filteredEvents.get(index))) {
 					filteredEvents.add(index, event);
 					break;
 				}
 			}
 		}
 		return filteredEvents;
 	}   
 	
 	public List<Event> reverseSort(List<Event> eventList) {
 		List<Event> filteredEvents = new ArrayList<Event>();
 		for (Event event : eventList) {
 			for (int index = 0; index <= filteredEvents.size(); index++) {
 				if (index == filteredEvents.size() || !compareStartTime(event, filteredEvents.get(index))) {
 					filteredEvents.add(index, event);
 					break;
 				}
 			}
 		}
 		return filteredEvents;
 	}   
 	
 }
