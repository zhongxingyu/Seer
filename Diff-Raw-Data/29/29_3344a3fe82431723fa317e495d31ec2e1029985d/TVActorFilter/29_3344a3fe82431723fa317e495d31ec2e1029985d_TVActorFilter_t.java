 package processing;
 
 import java.util.ArrayList;
 import java.util.List;
 import model.Event;
 
 public class TVActorFilter implements IFilter {
 	
 	public List<Event> filter(List<Event> eventList, Object ... args) {
 		return filterByActors(eventList, (String) args[0]);
 	}
 	
 	public List<Event> filterByActors(List<Event> eventList, String actor) {
 		List<Event> filteredList = new ArrayList<Event>();
 		for (Event event : eventList) {
				if (event.getProperty("actors").contains(actor) && event.getProperty("actors") != null) {
 					filteredList.add(event);
 				}
 			}
 		return filteredList;
 	}
 }
