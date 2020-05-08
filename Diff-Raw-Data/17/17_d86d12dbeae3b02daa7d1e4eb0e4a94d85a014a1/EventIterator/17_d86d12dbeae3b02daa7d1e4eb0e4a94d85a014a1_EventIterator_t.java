 package fi.helsinki.cs.oato.model;
 
 import java.util.*;
 
 import fi.helsinki.cs.oato.model.Event;
 import org.joda.time.DateTime;
 import static fi.helsinki.cs.oato.Strings.*;
 
 /**
  * Iterator over a collection of events. Returns events in ascending order by
  * the starting date.
  */
 class EventIterator implements Iterator<Event> {
     private Event firstEvent;
     private Iterator<Event> innerIterator;
     private boolean discardPast;
 
     /**
      * Constructs a new <code>EventIterator</code> for the specified collection.
      *
      * @param events the event collection
      * @param discardPast if <code>true</code>, does not return past events;
      *                    if <code>false</code>, returns past events
      */
     public EventIterator(Collection<Event> events, boolean discardPast) {
         List<Event> sortedEvents = new ArrayList<Event>(events);
         Collections.sort(sortedEvents);
 
         this.discardPast   = discardPast;
         this.innerIterator = sortedEvents.iterator();
 
         if (this.discardPast) {
             rewindToFirstUpcomingEvent();
         }
     }
 
     public boolean hasNext() {
         return (this.firstEvent != null) || this.innerIterator.hasNext();
     }
 
     /**
      * Gets whether the iterator is set up to discard past events.
      * @return <code>true</code> if this iterator discards past events;
      *         <code>false</code> otherwise
      */
     public boolean isPastDiscarded() {
         return discardPast;
     }
 
     public Event next() {
         Event nextEvent;
 
         if (this.firstEvent != null) {
             nextEvent = this.firstEvent;
             this.firstEvent = null;
         } else {
             nextEvent = this.innerIterator.next();
         }
 
         return nextEvent;
     }
 
     /**
      * @throws UnsupportedOperationException always
      */
     public void remove() {
         throw new UnsupportedOperationException(
                 localize("remove() operation is not supported by EventIterator"));
     }
 
     /**
      * Rewinds the inner iterator to first upcoming event and stores the
      * event to <code>firstEvent</code> instance variable.
      */
     private void rewindToFirstUpcomingEvent() {
         DateTime now = new DateTime();
         Event event = null;
        
         while (this.innerIterator.hasNext()) {
        	event = this.innerIterator.next();
        	
             if (event.compareTo(now) >= 0) {
                 break;
             }
            
            
        }
        
        // XXX hack
        // sometimes the latest event is not actually before the given date, 
        // but just happens to be the last element in the list => iterator can't go further
        if( event.compareTo(now) < 0 ) {
        	event = null;
         }
 
         if (event != null) {
             this.firstEvent = event;
         }
     }
 }
