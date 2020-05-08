 package sorting;
 import java.util.ArrayList;
 import java.util.List;
 import model.Event;
 
 public abstract class AbstractSorter {
 	
 	public abstract boolean checkSortCondition(Event event1, Event event2);
 	
 	public List<Event> sort(List<Event> eventList, Object ... args) {
 		List<Event> sortedEvents = new ArrayList<Event>();
 		sortedEvents.add(eventList.get(0));
 		for (Event event : eventList) {
 			for (int index = 0; index <= sortedEvents.size(); index++ ) {
 				if (index == sortedEvents.size() || checkSortCondition(event, sortedEvents.get(index))) {
 					sortedEvents.add(index, event);
 				}
 			}
 		}
 		return sortedEvents;
 	}
 	
 	public List<Event> reverseSort(List<Event> eventList, Object ... args) {
 		List<Event> sortedEvents = new ArrayList<Event>();
 		sortedEvents.add(eventList.get(0));
 		for (Event event : eventList) {
 			for (int index = 0; index <= sortedEvents.size(); index++ ) {
 				if (index == sortedEvents.size() || !checkSortCondition(event, sortedEvents.get(index))) {
 					sortedEvents.add(event);
 				}
 			}
 		}
 		return sortedEvents;
 	}
 }
