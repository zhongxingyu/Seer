 package uk.co.acuminous.julez.event.handler;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import uk.co.acuminous.julez.event.Event;
 import uk.co.acuminous.julez.event.repository.BaseEventRepository;
 
 public class InMemoryEventRepository extends BaseEventRepository {
 
     protected List<Event> events = Collections.synchronizedList(new ArrayList<Event>());
 
     @Override
     public int count() {
         return events.size();
     }
         
     @Override
     public List<Event> list() {
        return events;
     }
 
     @Override
     public void raise() {
         for(Event event : events) {
             handler.onEvent(event);
         }        
     }
     
     @Override
     public void onEvent(Event event) {
         events.add(event);
     }
 }
