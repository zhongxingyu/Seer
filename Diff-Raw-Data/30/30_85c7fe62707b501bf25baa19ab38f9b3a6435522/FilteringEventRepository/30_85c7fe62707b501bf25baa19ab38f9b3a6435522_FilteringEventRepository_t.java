 package uk.co.acuminous.julez.event.repository;
 
 import java.util.List;
 
 import uk.co.acuminous.julez.event.Event;
 import uk.co.acuminous.julez.event.filter.EventFilter;
 
 public interface FilteringEventRepository extends EventRepository {    
     int count(EventFilter filter);
    List<Event> list(EventFilter filter);
 }
