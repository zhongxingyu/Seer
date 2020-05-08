 package models;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.ObjectOutputStream;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Comparator;
 import java.util.HashSet;
 
 /** A set of events.
  * @see HashSet
  */
 public class EventSet extends HashSet<Event> {
 	private static final long serialVersionUID = 5541922632969721520L;
 	
 	private final EventHandler eventHandler;
 
	/**
	 * Creates a new, empty event set.
	 */
 	public EventSet(){
 		eventHandler = null;
 	}
 	
	/**
	 * Creates a new, empty event set associated with a given event handler.
	 * @param eventHandler the associated event handler.
	 */
     public EventSet(EventHandler eventHandler) {
         this.eventHandler = eventHandler;
     }
     
     /** This method returns this EventSet in a form of an sorted array.
      * @return a sorted by starting points array of events in this EventSet.
      */
     public Event[] getSortedArray(){
     	Event[] array = new Event[this.size()];
     	int it = 0;
 		for(Event e : this){
 			array[it] = e;
 			it++;
 		}
 		Comparator<Event> c = new Comparator<Event>(){
 			@Override
 			public int compare(Event o1, Event o2) {
 				if(o1.getStartTime().getTimeInMillis() < o2.getStartTime().getTimeInMillis())
 					return -1;
 				else if(o1.getStartTime().getTimeInMillis() == o2.getStartTime().getTimeInMillis())
 					return 0;
 				else return 1;
 			}
 		};
 		
 		Arrays.sort(array, c);
     	return array;
     }
 
     /** Returns an EventSet of Events that are between _startTime and _endTime.
      * @param _startTime Calendar type start of the interval.
      * @param _endTime Calendar type end of the interval.
      * @return Set of Events.
      */
     public EventSet between(Calendar _startTime, Calendar _endTime) {
         EventSet ret = new EventSet(eventHandler);
         Calendar startTime = (Calendar) _startTime.clone();
         Calendar endTime = (Calendar) _endTime.clone();
         startTime.set(Calendar.HOUR_OF_DAY, 0);
         startTime.set(Calendar.MINUTE, 0);
         endTime.set(Calendar.HOUR_OF_DAY, 23);
         endTime.set(Calendar.MINUTE, 59);
         for ( Event e : this )
             if ( e.isBetween(startTime, endTime) )
                 ret.add(e);
         return ret;
     }
 
     /** Returns a subset of this EventSet that overlaps with whole-day interval given by parameters.
      * @param _startTime start of the interval(will be extended to the 00:00).
      * @param _endTime end of the interval(will be extended to 23:59).
      * @return A subset that overlaps with given interval.
      */
     public EventSet overlapping(Calendar _startTime, Calendar _endTime) {
         EventSet ret = new EventSet(eventHandler);
         Calendar startTime = (Calendar) _startTime.clone();
         Calendar endTime = (Calendar) _endTime.clone();
         startTime.set(Calendar.HOUR_OF_DAY, 0);
         startTime.set(Calendar.MINUTE, 0);
         endTime.set(Calendar.HOUR_OF_DAY, 23);
         endTime.set(Calendar.MINUTE, 59);
         for ( Event e : this )
             if ( e.overlaps(startTime, endTime) )
                 ret.add(e);
         return ret;
     }
 
 
     /** Creates and returns new event set containing all events in this set.
      * @return event set containing all events in this set.
      */
     public EventSet all() {
         EventSet ret = new EventSet(eventHandler);
         for ( Event e : this )
             ret.add(e);
         return ret;
     }
     
     /** Exports this EventSet to a given location to a file of a given name.
      * @param exportPath external path.
      * @param name destination file.
      */
     public void exportEventSet(String exportPath, String name){
     	try {
     		String filename = exportPath + "/" + name;
     		Files.deleteIfExists(Paths.get(filename));
     		new File(exportPath).mkdirs();
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
 			oos.writeObject(this);
 			oos.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
     }
     
     /** Returns string representation of this event set.
      * @return string representation of this event set.
      */
     public String toString(){
     	String res = "EventSet[";
     	for ( Event e : this )
             res += e;
     	res += "]";
     	return res;
     }
 }
