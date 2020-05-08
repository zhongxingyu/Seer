 package com.cse769.EJB.Service;
 
 import java.util.Date;
 import java.util.Set;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import com.cse769.EJB.Entity.Event;
 import com.cse769.EJB.Entity.EventCategory;
 import com.cse769.EJB.Entity.Ticket;
 import com.cse769.EJB.Entity.Venue;
 
 
 
 
 @Stateless
 public class EventService {
 
 	@PersistenceContext(unitName="examples-769-EJB")
 	EntityManager em;
 	
 	public void createEvent(String name, Set<EventCategory> category, String description, double cost, Date date, int quantity, Venue venue, Set<Ticket> tickets) {
 		Event event = new Event();
 		event.setName(name);
 		event.setCategory(category);
 		event.setDescription(description);
 		event.setCost(cost);
 		event.setDate(date);
 		event.setQuantity(quantity);
 		event.setVenue(venue);
 		event.setTickets(tickets);
 		em.persist(event);
 	}
 	
 	public Event getEventById(Long id) {
 		return em.find(Event.class, id);
 	}
 	
 	public void removeEvent(Long id) {
 		Event ev = em.find(Event.class, id);
 		em.remove(ev);
 	}
 	
 	public void updateEvent(Event ev) {
		em.getTransaction().begin();
		Event newEvent = em.find(Event.class, ev.getEventId());
 		newEvent.setCategory(ev.getCategory());
 		newEvent.setCost(ev.getCost());
 		newEvent.setDate(ev.getDate());
 		newEvent.setDescription(ev.getDescription());
 		newEvent.setName(ev.getName());
 		newEvent.setQuantity(ev.getQuantity());
 		newEvent.setTickets(ev.getTickets());
 		newEvent.setVenue(ev.getVenue());
		em.getTransaction().commit();
 	}
 }
