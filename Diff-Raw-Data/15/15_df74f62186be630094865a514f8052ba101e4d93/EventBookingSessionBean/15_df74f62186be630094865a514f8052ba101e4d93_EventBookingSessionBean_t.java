 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package CEMS.session;
 
 import CEMS.entity.EventBookingEntity;
 import CEMS.entity.VenueEntity;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.ejb.LocalBean;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 /**
  *
  * @author Ser3na
  */
 @Stateless
 @LocalBean
 public class EventBookingSessionBean {
 
     @PersistenceContext(unitName = "IRMS-ejbPU")
     private EntityManager em;
     List<EventBookingEntity> eventBookings = new ArrayList<EventBookingEntity>();
     List<EventBookingEntity> bookingList;
     EventBookingEntity eventBooking;
 
     public EventBookingSessionBean() {
     }
 
     public List<EventBookingEntity> getEventBookings(VenueEntity venue) {
         Query q = em.createQuery("SELECT e FROM EventBookingEntity e");
         bookingList = new ArrayList<EventBookingEntity>();
         bookingList = q.getResultList();
         Iterator<EventBookingEntity> itr = bookingList.iterator();
         eventBooking = new EventBookingEntity();
         eventBookings = new ArrayList<EventBookingEntity>();
         while (itr.hasNext()) {
             eventBooking = itr.next();
             System.err.println("Booking List Size: " + bookingList.size());
             System.err.println("event booking name: " + eventBooking.getEvent().getEventName());
             System.err.println("venue id:" + venue.getVenueId());
             if (eventBooking.getVenue().getVenueId().equals(venue.getVenueId())) {
                 eventBookings.add(eventBooking);
                 System.err.println(eventBooking + " added to eventBookings list");
             }
         }
         return eventBookings;
     }
 
     public List<EventBookingEntity> getManagerEvent(String userId) {
         Query q = em.createQuery("SELECT e FROM EventBookingEntity e");
 //        System.err.println("ID: "+userId);
         bookingList = new ArrayList<EventBookingEntity>();
         bookingList = q.getResultList();
         Iterator<EventBookingEntity> itr = bookingList.iterator();
         eventBookings = new ArrayList<EventBookingEntity>();
         while (itr.hasNext()) {
            eventBooking = new EventBookingEntity();
             eventBooking = itr.next();
//            System.err.println("Compare:" + eventBooking.getEvent().getStatus() + eventBooking.getEvent().getEventManagerId() + eventBooking.getEvent().getEventId());
            if ((eventBooking.getEvent().getStatus().equalsIgnoreCase("Confirmed")) && (eventBooking.getEvent().getEventManagerId() != null) && (eventBooking.getEvent().getEventManagerId().equals(userId))) {
                 eventBookings.add(eventBooking);
//                System.err.println("Count: "+eventBookings.size());
//                System.err.println("getManagerEvent: " + eventBooking.getEvent().getStatus() + eventBooking.getEvent().getEventManagerId() + eventBooking.getEvent().getEventId());
             }
         }
//        System.out.println("Count2:"+eventBookings.size());
        return eventBookings;
     }
 
     public EventBookingEntity addEventBooking(EventBookingEntity eventBooking) {
         em.persist(eventBooking);
         return eventBooking;
     }
 }
