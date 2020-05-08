 package models;
 
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.HashSet;
 
 /** This class provides methods for event management.
  * Every instance of this class contains a reference to the associated set of events.
  */
 public class EventManager implements Serializable{
 	
 	private static final long serialVersionUID = -2897014193380647665L;
 	private final EventSet events;
 
 	/** Constructs a new event manager with an empty event set.
      */
     public EventManager() {
         this.events = new EventSet(this);
     }
 
     /** Creates and returns new event set containing all events in the associated event set.
      * @see EventSet#all()
      */
     public EventSet all() {
         return events.all();
     }
 
     /** Returns an EventSet of Events that are between startTime and endTime.
      * @see EventSet#between(Calendar, Calendar)
      */
     public EventSet between(Calendar startTime, Calendar endTime) {
         return events.between(startTime, endTime);
     }
 
     /** Returns a subset of the associated EventSet that overlaps with whole-day interval given by parameters.
      * @see EventSet#overlapping(Calendar, Calendar)
      */
     public EventSet overlapping(Calendar startTime, Calendar endTime) {
         return events.overlapping(startTime, endTime);
     }
 
     /** Adds a given event to the associated event set.
      * @param e the event to be added.
      * @return true if the set did not already contain the specified event.
      * @see HashSet#add(Object)
      */
     public boolean add(Event e) {
         return events.add(e);
     }
 
     /** Removes a given event from the associated event set.
      * @param e the event to be removed.
      * @see EventSet#remove(Object)
      */
 	public void removeEvent(Event e) {
 		events.remove(e);
 	}
 	
	/** Tries to add an EventSet to this manager. All overlapping events will be returned.
	 * @param newEvents Events to add to this manager.
	 * @return EventSet of overlapping events.
	 */
 	public EventSet addSet(EventSet newEvents){
 		EventSet leftovers = new EventSet();
 		
 		Event[] currSet = events.getSortedArray(),
 				newSet = newEvents.getSortedArray();
 		
 		for(int i = 0, j = 0; i < newSet.length ; ++i){
 			while(	j < currSet.length - 1 && 
 					currSet[j+1].getStartTime().getTimeInMillis() <= 
 					newSet[i].getStartTime().getTimeInMillis())
 				j++;
 			if(j == currSet.length - 1){
 				if(newSet[i].overlaps(currSet[j].getStartTime(), currSet[j].getEndTime()))
 					leftovers.add(newSet[i]);
 				else
 					this.add(newSet[i]);
 			}
 			else{
				if(j < currSet.length && newSet[i].overlaps(currSet[j].getStartTime(), currSet[j].getEndTime())
 						|| newSet[i].overlaps(currSet[j+1].getStartTime(), currSet[j+1].getEndTime()))
 					leftovers.add(newSet[i]);
 				else
 					this.add(newSet[i]);
 			}
 		}
 		
 		return leftovers;
 	}
 	
 	/** Imports event set from file with a given path.
 	 * @param pathname the given path.
 	 * @return imported event set if reading was successful, null otherwise.
 	 */
 	public static EventSet importEventSet(String pathname){
     	EventSet res = null;
     	try {
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pathname));
 			res = (EventSet) ois.readObject();
 			ois.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	return res;
     }
    
 	/** Returns string representation of this event manager.
      * @return string representation of this event manager.
      */
     public String toString(){
     	String res = "EventMananger[";
     	res += events;
     	res += "]";
     	return res;
     }
 
 }
