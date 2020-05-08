 package com.cse769.EJB.Service;
 
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import com.cse769.EJB.Entity.Event;
 import com.cse769.EJB.Entity.EventCategory;
 
 @Stateless
 public class EventCategoryService {
 	
 
 	@PersistenceContext(unitName="examples-769-EJB")
 	EntityManager em;
 	
 	public void createEventCategory(String name, List<Event> events) {
 		EventCategory eventCategory = new EventCategory();
 		eventCategory.setEvents(events);
 		eventCategory.setCategory(name);
 		em.persist(eventCategory);
 	}
 	
 	public EventCategory getEventCategoryById(Long id) {
 		return em.find(EventCategory.class, id);
 	}
 	
 	public void removeEventCategory(Long id) {
 		EventCategory ec = em.find(EventCategory.class, id);
 		em.remove(ec);
 	}
 	
 	public void updateEventCategory(EventCategory ec) {
		EventCategory newEventCategory = new EventCategory();
		newEventCategory.setName(ec.getName());
 	}
 }
