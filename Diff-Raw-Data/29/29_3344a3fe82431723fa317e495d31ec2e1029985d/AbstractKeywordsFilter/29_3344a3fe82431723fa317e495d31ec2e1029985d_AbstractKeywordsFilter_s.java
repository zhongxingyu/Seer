 package processing;
 import model.Event;
 import java.util.*;
import java.util.ArrayList;
 
 public abstract class AbstractKeywordsFilter implements IFilter {
 	
 	public abstract List<Event> filterByKeywords(List<Event> eventList, String keyword);
 	
 	public List<Event> filter(List<Event> eventList, Object ...args) {
 		return filterByKeywords(eventList, (String) args[0]);
 	}
 	
 	public boolean containsKeywords(Event event, String keyword) {
 		return isInTitle(event, keyword) || isInLocation(event, keyword) || isInDescription(event, keyword);
 	}
 	
 	public boolean isInTitle(Event event, String keyword) {
 		return event.getTitle().toLowerCase().contains((keyword.toLowerCase()));	
 	}
 	
 	public boolean isInLocation(Event event, String keyword) {
 		return event.getLocation().toLowerCase().contains((keyword.toLowerCase()));	
 	}
 	
 	public boolean isInDescription(Event event, String keyword) {
 		return event.getDescription().toLowerCase().contains((keyword.toLowerCase()));	
 	}
 	
 }
