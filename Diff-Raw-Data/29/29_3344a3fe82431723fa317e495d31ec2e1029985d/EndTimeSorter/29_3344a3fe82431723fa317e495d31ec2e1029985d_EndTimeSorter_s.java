 package processing;
 import java.util.ArrayList;
 import java.util.List;
 import model.Event;
 import org.joda.time.DateTimeComparator;
 
 public class EndTimeSorter implements ISorter {
 	
 	public boolean compareEndTime(Event event1, Event event2) {
 		DateTimeComparator comparator = DateTimeComparator.getInstance();
 		return !(comparator.compare(event1.getEndTime(), event2.getEndTime()) > 0);
 	}
 	
 	public List<Event> sort(List<Event> eventList) {
 		List<Event> filteredEvents = new ArrayList<Event>();
 		for (Event event : eventList) {
 			for (int index = 0; index <= filteredEvents.size(); index++) {
 				if (index == filteredEvents.size() || compareEndTime(event, filteredEvents.get(index))) {
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
 				if (index == filteredEvents.size() || !compareEndTime(event, filteredEvents.get(index))) {
 					filteredEvents.add(index, event);
 					break;
 				}
 			}
 		}
 		return filteredEvents;
 	}
 }
