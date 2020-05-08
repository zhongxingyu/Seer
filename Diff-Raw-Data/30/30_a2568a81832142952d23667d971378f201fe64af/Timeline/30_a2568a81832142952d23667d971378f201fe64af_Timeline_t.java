 package codesample.timeline;
 
 import java.util.*;
 
 import org.joda.time.DateTime;
 
 /**
  * This class implements an ordered collection that holds Event objects.
  *
  */
 public class Timeline implements Collection<Event> 
 {
 //	public static final DateTimeFormatter fmt = DateTimeFormat.forPattern("mm/dd/yyyy  kk:mm"); //"month/day/year  24hour:minutes of the hour"
 	
 	private TreeMap<DateTime, HashSet<Event>> startTimeEventMap = new TreeMap<DateTime, HashSet<Event>>();
 	private TreeMap<DateTime, HashSet<Event>> endTimeEventMap = new TreeMap<DateTime, HashSet<Event>>();
 	
 	public boolean add(Event e) {
 		addEndTime(endTimeEventMap,e);
 		return addStartTime(startTimeEventMap,e);
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.util.Collection#addAll(java.util.Collection)
 	 */
 	public boolean addAll(Collection<? extends Event> c) {
 		
 		if (c !=null)
 		{
 			Iterator<? extends Event> eventIter = c.iterator();
 			while (eventIter.hasNext())
 				this.add(eventIter.next());
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#clear()
 	 */
 	public void clear() {
 		startTimeEventMap.clear(); // Clears the map
 		endTimeEventMap.clear();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#contains(java.lang.Object)
 	 */
 	public boolean contains(Object o) {
 		
 		if (o==null || !(o instanceof Event))
 		{
 			return false;
 		}
 		
 		Event myEvent = (Event)o;
 		DateTime startTime = myEvent.getStart();
 		
 		if (startTimeEventMap.containsKey(startTime)) // Check to see if the key is there.
 		{
 			// Then check if the event is there
 			if (startTimeEventMap.get(startTime).contains(myEvent))
 			{
 				return true;
 			}
 			else
 				return false;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#containsAll(java.util.Collection)
 	 */
 	public boolean containsAll(Collection<?> c) {
 		
 		if (c !=null)
 		{
 			Iterator<?> eventIter = c.iterator();
 			while (eventIter.hasNext())
 			{
 				if(!this.contains(eventIter.next()))
 				{
 					return false;
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#isEmpty()
 	 */
 	public boolean isEmpty(){
 		if (startTimeEventMap.isEmpty() && endTimeEventMap.isEmpty()) // returns true if the map is empty
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#iterator()
 	 */
 	public Iterator<Event> iterator()
 	{
 		return new TimelineIterator();
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.util.Collection#remove(java.lang.Object)
 	 */
 	public boolean remove(Object o) {
 		if (o==null || !(o instanceof Event)) //if the object is null or not event return false, cannot remove
 		{
 			return false;
 		}
 		
 		removeEndTime(o);
 		
 		Event myEvent = (Event)o;
 		DateTime startTime = myEvent.getStart();
 		
 		if (!startTimeEventMap.containsKey(startTime)) // if the eventMap does not contain the key return false, cannot remove
 		{
 			return false;
 		}
 		
 		if (startTimeEventMap.get(myEvent.getStart()).size()==1) // If the size of the set contains one event, remove the key
 		{
 			startTimeEventMap.remove(myEvent.getStart());
 			return true;
 		}
 				
 		HashSet<Event> events = startTimeEventMap.get(startTime); // else, remove the event located at a given key
 		return events.remove(myEvent);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#removeAll(java.util.Collection)
 	 */
 	public boolean removeAll(Collection<?> c) {
 		if (c !=null)
 		{
 			Iterator<?> eventIter = c.iterator(); // iterate through the collection
 			while (eventIter.hasNext())           // remove all instances in eventMap from collection
 				this.remove(eventIter.next());
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#retainAll(java.util.Collection)
 	 */
 	public boolean retainAll(Collection<?> c) {
 		
 		if(c!=null)
 		{
 			TreeMap<DateTime, HashSet<Event>> newStartEventMap = new TreeMap<DateTime, HashSet<Event>>();
 			TreeMap<DateTime, HashSet<Event>> newEndEventMap = new TreeMap<DateTime, HashSet<Event>>();
 
 			
 			for (Object e: c)
 			{
 				if (e instanceof Event && this.contains(e))
 				{
 					addStartTime(newStartEventMap, (Event)e);
 				}
 			}
 
 			if (sizeEventMap(startTimeEventMap) == sizeEventMap(newStartEventMap))
 			{
 				return false;
 			}
 			
 			startTimeEventMap= newStartEventMap;
 			endTimeEventMap = newEndEventMap;
 			return true;
 		}		
 		return false;	
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#size()
 	 */
 	public int size() {	
 		return sizeEventMap(startTimeEventMap);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#toArray()
 	 */
 	public Object[] toArray() {
 		if (!startTimeEventMap.isEmpty())
 		{
 			ArrayList<Event> list = new ArrayList<Event>();
 			for(DateTime key: startTimeEventMap.keySet())
 			{
 				for(Event e: startTimeEventMap.get(key))
 				{		
 					list.add(e);	
 				}
 			}	
 			return list.toArray();
 		}
 	
 		Object [] result = null;
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#toArray(java.lang.Object[])
 	 */
 	public <T> T[] toArray(T[] a) {
 		if (!startTimeEventMap.isEmpty())
 		{
 			ArrayList<Event> list = new ArrayList<Event>();
 			
 			for(DateTime key: startTimeEventMap.keySet())
 			{
 				for(Event e: startTimeEventMap.get(key))
 				{
 					for(int i=0; i<sizeEventMap(startTimeEventMap); i++)
 					{
 						System.out.println("[");
 						list.add(e);
 						System.out.println("]");
 					}
 				}
 			}	
 			return list.toArray(a);
 		}
 	
 		T [] result = null;
 		return result;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append("StartTime Events: \n");
 		for (DateTime key: startTimeEventMap.keySet()) //the keys.
 		{
 			sb.append("[<").append(key).append(">: ");
 			for (Event e: startTimeEventMap.get(key)) //gets each individual event  
 			{
 				sb.append(e);
 			}
 			sb.append("\n");
 		}
 		
 		sb.append("\nEndTime Events \n");
 		for (DateTime key: endTimeEventMap.keySet()) //the keys.
 		{
 			sb.append("[<").append(key).append(">: ");
 			for (Event e: endTimeEventMap.get(key)) //gets each individual event  
 			{
 				sb.append(e);
 			}
 			sb.append("\n");
 		}
 		
 		return sb.toString();
 	}
 	
 	
 	/**
 	 * Adds an Event to a specified map of DateTimes to sets of Events.
 	 * 
 	 * @param mapOfEvents The map of DateTimes to sets of Events to add the event to
 	 * @param e Event to add to the passed-in TreeMap of events
 	 * @return
 	 */
 	private boolean addStartTime(TreeMap<DateTime, HashSet<Event>> set, Event e) {
 		if (e != null)
 		{
 			if(!set.containsKey(e.getStart()))   // If the DateTime is not there create a new key and hash set
 			{
 				set.put(e.getStart(), new HashSet<Event>()); // Places the DateTime and an empty Set into the map
 			}
 				set.get(e.getStart()).add(e); // Adds the new event to the empty Set in the given DateTime
 				return true;
 		}
 			return false;
 	}
 	
 	// Private add End Time method for any TreeMap 
 	private boolean addEndTime(TreeMap<DateTime, HashSet<Event>> set, Event e) {
 		if (e != null)
 		{
 			if(!set.containsKey(e.getEnd()))   // If the DateTime is not there create a new key and hash set
 			{
 				set.put(e.getEnd(), new HashSet<Event>()); // Places the DateTime and an empty Set into the map
 			}
 				set.get(e.getEnd()).add(e); // Adds the new event to the empty Set in the given DateTime
 				return true;
 		}
 			return false;
 	}
 	
 	private boolean removeEndTime(Object o)
 	{
 		Event myEvent = (Event)o;
 		DateTime endTime = myEvent.getEnd();
 		
 		if (!endTimeEventMap.containsKey(endTime)) // if the eventMap does not contain the key return false, cannot remove
 		{
 			return false;
 		}
 		
 		if (endTimeEventMap.get(myEvent.getEnd()).size()==1) // If the size of the set contains one event, remove the key
 		{
 			endTimeEventMap.remove(myEvent.getEnd());
 			return true;
 		}
 				
 		HashSet<Event> events = endTimeEventMap.get(endTime); // else, remove the event located at a given key
 		
 		return events.remove(myEvent);
 		
 	}
 	
 	/**
 	 * Calculates the total size of a map of DateTimes to sets of Events
 	 * 
 	 * @param mapOfEvents The map of DateTimes to sets of Events to add the event to
 	 * @return 0 if empty, total number of events otherwise
 	 */
 	private int sizeEventMap(TreeMap<DateTime, HashSet<Event>> mapOfEvents) {
 		
 		int count =0; // Count variable to keep track of all the values in eventMap
 
 		if (!mapOfEvents.isEmpty())
 		{
 			for(HashSet<Event> set: mapOfEvents.values())
 			{
 				count += set.size();
 			}
 				return count;
 		}
 		return 0;
 	}
 	
 	
 	
 	/**
      * This Iterator is designed to walk through the Timeline as if it were an
      * asymmetrical 2-dimensional array. To do this, we're only keeping track of
      * indexKey and indexValue and doing the navigation through the underlying
      * eventMap to determine what will actually work.
      * 
      * Does not implement the optional add, remove, or set operations
      *
      */
     private class TimelineIterator implements ListIterator<Event>
 
     {
         private int eventSetIndex = 0;  // Keeps track of which set you are currently on
         private int eventIndex = 0;     // Keeps track of which index inside the set you are on
         private int eventsPosition = 1; // Keeps track of nextIndex()/previousIndex()
         
         private HashSet<Event> eventSet; // Represents each individual set
         private Event[] events;          // Represents an array of all sets for printing in next()/previous()
         
         Object[] eventSets = new Object[0];
         
         /**
          *  No-argument protected constructor.
          */
         protected TimelineIterator()
         {
             if (startTimeEventMap != null && !startTimeEventMap.isEmpty())
             {
                 Collection<HashSet<Event>> valueCollection = startTimeEventMap.values();
                 if (valueCollection != null)
                 {
                     eventSets = valueCollection.toArray();
                 }
             }            
         }
         
 
         /* (non-Javadoc)
          * @see java.util.ListIterator#hasNext()
          */
 		public boolean hasNext() 
         {
         	
         	if (eventSetIndex >= eventSets.length) { // If there are no more sets, retufn false
         		return false;
         	}
         	
         	eventSet = (HashSet<Event>)eventSets[eventSetIndex];
        	        	
             if (eventIndex < eventSet.size()) // if the current index in set is less than the size, return true
             {
                return true;
             }
             if(eventSets.length-1 == eventSetIndex && eventIndex==eventSet.size()) // If you are on the last element, return false
             {
             	return false;
             }
             else {
             	if (eventSetIndex < eventSets.length) {
             		return true;
             	}
             	else {
             		return false;
             	}
             }
             
         }
 
         
         /* (non-Javadoc)
          * @see java.util.ListIterator#next()
          */
 		public Event next()
         {
         	eventSet = (HashSet<Event>)eventSets[eventSetIndex];
         	events = (Event[]) eventSet.toArray(new Event[0]);
         	Event nextEvent = null;
       
             if (eventIndex < events.length)
             {
                nextEvent = events[eventIndex];
                eventIndex++;
                eventsPosition++;
             } else {
             	eventIndex = 0;
             	eventSetIndex ++;
             	eventsPosition++;
             	if (eventSetIndex < eventSets.length) {
             		HashSet<Event> eventSet = (HashSet<Event>)eventSets[eventSetIndex];
             		Event[] events = (Event[]) eventSet.toArray(new Event[0]);
             		nextEvent = events[eventIndex];
             		eventIndex++;
             		eventsPosition++;
             	} else {
                     throw new NoSuchElementException();
             	}
             }
             
             if(eventSets.length-1 == eventSetIndex && eventIndex==eventSet.size()) // If you are on the very last element, stop
             {
             	return nextEvent;
             }
               
             if (eventIndex >= events.length) { // If you moved to the end of a set, move to the next set
             	eventIndex = 0;
             	eventSetIndex ++;
             }
             return nextEvent;
         }
 
         /* (non-Javadoc)
          * @see java.util.ListIterator#hasPrevious()
          */
         public boolean hasPrevious()
         {
         	
         	if (eventSetIndex==0 && eventIndex==0)
         	{
         		return false;
         	}
         	
             if (eventIndex > 0)
             {
                return true;
             }
             else {
             	if (eventSetIndex > 0) { // there is a previous set
             		return true;
             	}
             	else {
             		return false;
             	}
             }
         }
 
         /* (non-Javadoc)
          * @see java.util.ListIterator#previous()
          */
 		public Event previous() 
         {
 			eventSet = (HashSet<Event>)eventSets[eventSetIndex];
         	events = (Event[]) eventSet.toArray(new Event[0]);
         	Event previousEvent = null;
         	
         	if (eventSetIndex==0 && eventIndex==0)
         	{
         		throw new NoSuchElementException();
         	}
       
             if (eventIndex > 0) // If you are anywhere that isnt the first element, move back one
             {
                eventIndex--;
                eventsPosition--;
                previousEvent = events[eventIndex];
             } else {
             	eventSetIndex --;
             	eventSet = (HashSet<Event>)eventSets[eventSetIndex];
             	eventIndex = eventSet.size();
             	if (eventSetIndex > 0) {
             		eventSet = (HashSet<Event>)eventSets[eventSetIndex];
             		events = (Event[]) eventSet.toArray(new Event[0]);
             		eventIndex--;
             		eventsPosition--;
             		previousEvent = events[eventIndex];
             	}
             }
             
             if (eventSetIndex > 0 && eventIndex == 0) { // If after you moved back you are on the first element of a set, go back to the last element of the previous set
             	eventSetIndex --;
             	eventSet = (HashSet<Event>)eventSets[eventSetIndex];
             	eventIndex = eventSet.size();     	
             }
             return previousEvent;
         }
         
 
         /* (non-Javadoc)
          * @see java.util.ListIterator#nextIndex()
 	     */
         public int nextIndex() //of the all the events or just the events in a particular set.
 		{
         	return eventsPosition;
         }
 
 		
         /* (non-Javadoc)
          * @see java.util.ListIterator#previousIndex()
          */
         public int previousIndex() {
             return eventsPosition-1; // Since eventsPosition is declared at 1 subtract one when going backwards
         }
 
         /* (non-Javadoc)
          * @see java.util.ListIterator#remove()
          */
         public void remove()
         {
             throw new UnsupportedOperationException(
                     "We're not implementing remove!");
         }
 
         /** Not implemented by Timeline
          *
          */
         public void set(Event e)
         {
             throw new UnsupportedOperationException(
                     "We're not implementing set!");
         }
 
         /** Not implemented by Timeline
         *
         */
        public void add(Event e)
         {
         	throw new UnsupportedOperationException(
                     "We're not implementing add!");
         }
     }
     
     /**
      * *
      * Query methods
      * 
      * 
      */
     
     public TreeMap<DateTime, HashSet<Event>> startedDuring(DateTime startTime, DateTime endTime)
     {
 		TreeMap<DateTime, HashSet<Event>> newStartEventMap = new TreeMap<DateTime, HashSet<Event>>();
 
 		for(DateTime key: startTimeEventMap.keySet())
 		{
 			for(Event e: startTimeEventMap.get(key))
 			{	
 				if (key.equals(startTime))
 				{
 					addStartTime(newStartEventMap,e);
 				}
 				
 				if (key.isAfter(startTime) && key.isBefore(endTime))
 				{
 					addStartTime(newStartEventMap,e);
 				}
 			}
 		}
 		return newStartEventMap;
     }
     
     public TreeMap<DateTime, HashSet<Event>> endedDuring(DateTime startTime, DateTime endTime)
     {
     	TreeMap<DateTime, HashSet<Event>> newEndEventMap = new TreeMap<DateTime, HashSet<Event>>();
 
 		for(DateTime key: endTimeEventMap.keySet())
 		{
 			for(Event e: endTimeEventMap.get(key))
 			{	
 				if (key.equals(endTime))
 				{
 					addStartTime(newEndEventMap,e);
 				}
 				
 				if (key.isAfter(endTime) && key.isBefore(endTime))
 				{
 					addStartTime(newEndEventMap,e);
 				}
 			}
 		}
 		return newEndEventMap;
     }
     
     public Timeline occurredBefore(DateTime startTime, DateTime endTime)
     {	
     	Timeline occurredBeforeTimeline = new Timeline();
     	
			for(Event e: this)
 			{	
				if (e.getEnd().equals(startTime) || e.getEnd().isBefore(startTime))
 				{
 					occurredBeforeTimeline.add(e);
 				}
 			}
	
 		return occurredBeforeTimeline;
     }
     
     public Timeline occurredAfter(DateTime startTime, DateTime endTime)
     {
 
     	Timeline occurredAfterTimeline = new Timeline();
				
		for (Event e: this)
 		{
			if(e.getStart().equals(endTime) || e.getStart().isAfter(endTime))
			{
				occurredAfterTimeline.add(e);

 			}
		}
 		return occurredAfterTimeline;
     }
     
     public Timeline occurredDuring(DateTime startTime, DateTime endTime)
     {
     	Timeline duringTimeline = new Timeline();
     	
     	for( Event e: this)
     	{
     		if (e.getStart().equals(startTime) && e.getEnd().equals(endTime))
     		{
     			duringTimeline.add(e);
     		} 
     		
     		if (e.getStart().isAfter(startTime) && e.getEnd().isBefore(endTime))
     		{
     			duringTimeline.add(e);
     		}
     	}
     	return duringTimeline;
     }
     
     public Timeline partiallyContained(DateTime startTime, DateTime endTime)
     {
     	Timeline partialTimeline = new Timeline();
     	
     	for (Event e: this)
     	{
     		if(e.getStart().isBefore(startTime) && e.getEnd().isAfter(startTime) && e.getEnd().isBefore(endTime))
     		{
     			partialTimeline.add(e);
     		}
     		
     		if (e.getStart().isAfter(startTime) && e.getStart().isBefore(endTime) && e.getEnd().isAfter(endTime))
     		{
     			partialTimeline.add(e);
     		}
     	}
     	
     	return partialTimeline;
     }
     
     public Timeline includedInQuery(DateTime startTime, DateTime endTime)
     {
     	Timeline includedTimeline = new Timeline();
     	
     	for (Event e: this)
     	{
     		if (e.getStart().isBefore(startTime) && e.getEnd().isAfter(endTime))
     		{
     			includedTimeline.add(e);
     		}
     		
     		if (e.getStart().equals(startTime) && e.getEnd().equals(endTime))
     		{
     			includedTimeline.add(e);
     		}
     		
     		if(e.getStart().isBefore(startTime) && e.getEnd().isAfter(startTime) && e.getEnd().isBefore(endTime))
     		{
     			includedTimeline.add(e);
     		}
     		
     		if (e.getStart().isAfter(startTime) && e.getStart().isBefore(endTime) && e.getEnd().isAfter(endTime))
     		{
     			includedTimeline.add(e);
     		}
     		
     		if (e.getStart().isAfter(startTime) && e.getEnd().isBefore(endTime))
         	{
         		includedTimeline.add(e);
         	}
     	}
     	return includedTimeline;
     }
     
     public Timeline overlapsQuery(DateTime startTime, DateTime endTime)
     {
     	Timeline overlapsTimeline = new Timeline();
     	
     	for (Event e: this)
     	{
     		if (e.getStart().isBefore(startTime) && e.getEnd().isAfter(endTime))
     		{
     			overlapsTimeline.add(e);
     		}
     	}
     	
     	return overlapsTimeline;
     }
     
     public Timeline same(DateTime startTime, DateTime endTime)
     {
     	Timeline sameTimeline = new Timeline();
     	
     	for(Event e: this)
     	{
     		if (e.getStart().equals(startTime) && e.getEnd().equals(endTime))
     		{
     			sameTimeline.add(e);
     		}
     	}
 
     	return sameTimeline;
     }
 }
