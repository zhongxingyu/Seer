 package uk.co.acuminous.julez.test;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import uk.co.acuminous.julez.event.Event;
 
 public class TestUtils {
 	public static int countEvents(Iterable<Event> events) {
 		int ret = 0;
 		for (@SuppressWarnings("unused") Event event : events) {
 			++ret;
 		}
 		
 		return ret;
 	}
 
 	public static boolean checkEvents(List<Event> expected, Iterable<Event> repo) {
 		Iterator<Event> actual = repo.iterator();
 		for (Event event : expected) {
 			if (!actual.hasNext()) return false;
 			if (!event.equals(actual.next())) return false;
 		}
 		return !actual.hasNext();
 	}
 
 	public static boolean checkEvents(Event[] expected, Iterable<Event> repo) {
 		return checkEvents(Arrays.asList(expected), repo);
 	}
 	
 	public static Event getEvent(Iterable<Event> repo, int i) {
     	int sofar = 0;
     	for (Event event : repo) {
     		if (sofar == i) return event;
     	}
         return null;
 	}
 	
 	public static Event first(Iterable<Event> repo) {
 		return repo.iterator().next();
 	}
 }
