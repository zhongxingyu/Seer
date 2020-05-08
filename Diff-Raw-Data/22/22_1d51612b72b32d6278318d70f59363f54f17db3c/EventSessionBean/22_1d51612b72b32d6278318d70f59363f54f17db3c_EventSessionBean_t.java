 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package CEMS.session;
 
 import CEMS.entity.BookingEntity;
 import CEMS.entity.EventEntity;
 import CEMS.entity.ServiceEntity;
 import CEMS.entity.VenueEntity;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.ejb.LocalBean;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 /**
  *
  * @author Diana Wang
  */
 @Stateless
 @LocalBean
 public class EventSessionBean {
     @PersistenceContext(unitName = "IRMS-ejbPU")
     private EntityManager em;
     
     BookingEntity be;
     EventEntity ee;
     VenueEntity ve;
     ServiceEntity se;
 
     public void persist(Object object) {
         em.persist(object);
     }
     
     public EventSessionBean(){}
     
     public EventEntity makeReservation(String eventName,String eventType,
             String eventContact,String title,String name, String email,int eventScale,Date startDate,Date endDate)
     {
         ee = new EventEntity();
         
             ee.setEmail(email);
             ee.setEndDate(endDate);
             ee.setEventContact(eventContact);
             ee.setEventName(eventName);
             ee.setEventScale(eventScale);
             ee.setEventType(eventType);
             ee.setName(name);
             ee.setStartDate(startDate);
             ee.setTitle(title);
             System.out.println("EventSessionBean: makeReservation: event has been created!"+ee.getEventId()+ee.getEventName());
             
             ee.setStatus("In Progress: move to booking");
             em.persist(ee);
             System.out.println("EventSessionBean: makeReservation: event status has been changed "+ee.getEventId()+ee.getStatus());
 
         return ee;
     }
     
     public EventEntity getReservation(Long eventId)
     {
         ee = em.find(EventEntity.class,eventId);
         if(ee!=null)
         {
             System.out.println("EventSessionBean: getReservation: event has been found "+ee.getEventId()+ee.getStatus());
             return ee;
         }
         else
         {
             System.out.println("EventSessionBean: getReservation: event does not exist! "+eventId);
             return null;
         }
     }
     
     public boolean deleteReservation(Long eventId)
     {
         ee = em.find(EventEntity.class,eventId);
         if(ee!=null)
         {
            System.out.println("EventSessionBean: getReservation: event has been found "+ee.getEventId()+ee.getStatus());
            em.remove(ee);
            return true;
         }
         else
         {
             System.out.println("EventSessionBean: getReservation: event does not exist! "+eventId);
             return false;
         }
     }
 
     public VenueEntity venueBooking(Long eventId, Long venueId,Date startDate,Date endDate)
     {
         System.out.println("EventSessionBean:venueBooking: start booking! "+eventId);
         ee = em.find(EventEntity.class, eventId);
         if(ee!=null)
         {
             be = new BookingEntity();
             ve = em.find(VenueEntity.class, venueId);
             if(ve==null)
             {
                System.out.println("EventSessionBean:venueBooking: venue does not exist! "+venueId); 
                return null;
             }
             
             System.out.println("EventSessionBean:venueBooking: venue has been found! "+venueId);
                be.setBookingDate(startDate);
                be.setEndingDate(endDate);
                be.setEvent(ee);
                be.setVenue(ve);
                em.persist(be);
             System.out.println("EventSessionBean:venueBooking: booking has been persistd! "+be.getId());
             
             List <BookingEntity> bookings = new ArrayList<BookingEntity> ();
                 bookings = ee.getBookings();
                 bookings.add(be);
                 ee.setBookings(bookings);
                 em.merge(ee);
             System.out.println("EventSessionBean:venueBooking: event has been modified and merged! "+ee.getEventId());
         
             return ve;
         }
         else
         {
             System.out.println("EventSessionBean:venueBooking: event does not exist! "+eventId);
             return null;
         }
     }
     
     public List <Date> checkVenueAvailability(Long venueId,Integer numberPeople)
     {
         ve = em.find(VenueEntity.class, venueId);
         if(ve!=null){
             if(ve.getVenueCapacity()< numberPeople){
                System.out.println("EventSessionBean:CheckVenueAvailability: venue quota reached! ");
                 return null;
             }
             Query q = em.createQuery("SELECT b FROM BookingEntity b WHERE b.venue.venueId ='"+venueId+"'");
             List<Date> unavailDates = new ArrayList<Date>();
             
             for(Object o : q.getResultList())
             {
                 BookingEntity booking = (BookingEntity)o;
                 Date startDate = booking.getBookingDate();
                 Date endDate = booking.getEndingDate();
                 
                 Date unavailDate = startDate;
                 while(unavailDate.before(endDate))
                 {
                     unavailDates.add(unavailDate);
                     System.out.println("EventSessionBean:CheckVenueAvailability: date unavailable is "
                             +unavailDate);
                     unavailDate.setTime(unavailDate.getTime()+1*24*60*60*1000);
                     System.out.println("EventSessionBean:CheckVenueAvailability: date has been incremented to "
                             +unavailDate);
                     
                 }
                 System.out.println("EventSessionBean:CheckVenueAvailability: date list has been partially retrieved "
                             +unavailDates.size());
             }
                 System.out.println("EventSessionBean:CheckVenueAvailability: date list has been fully retrieved "
                             +unavailDates.size());
                 return unavailDates;
             
         }
         else
             {
             System.out.println("EventSessionBean:checkVenueAvailability: The venue does not exist! "+venueId);
             return null;
             }
         
     }
    
     public boolean checkVenueCapacity(Long venueId,Integer numberPeople)
     {
         ve = em.find(VenueEntity.class, venueId);
         if(ve!=null)
         {
             if(numberPeople>ve.getVenueCapacity())
                 return false;
             else
                 return true;
         }
         else
         {
           System.out.println("EventSessionBean: checkVenueCapacity: venue does not exist! "+venueId); 
           return false;
         }
     }
     
     public List <VenueEntity> searchVenue(Integer venueCapacity,String venueFunction)
     {
         Query q = em.createQuery("SELECT v FROM VenueEntity v WHERE v.venueCapacity >= '"+venueCapacity+"'");
         System.out.println("EventSessionBean:SearchVenue: qualified venues have been retrieved 1");
         List<VenueEntity> qualifedVenues = new ArrayList<VenueEntity>();
         
         for(Object o: q.getResultList())
         {
             VenueEntity venue = (VenueEntity)o;
             List<String> functions = venue.getVenueFunction();
             if(functions.contains(venueFunction))
             { 
                 qualifedVenues.add(venue);
                 System.out.println("EventSessionBean:SearchVenue: one qualified venues have been added!");
             }
             }
         System.out.println("EventSessionBean:SearchVenue: qualified venues have been retrieved 2");
         return qualifedVenues;
         }
     
    public List<EventEntity> listEvents()
     {
        Query q = em.createQuery("SELECT e FROM EventEntity e");
        List <EventEntity> events = new ArrayList<EventEntity>();
         
        for(Object o:q.getResultList())
        {
            ee = (EventEntity)o;
            events.add(ee);
            System.out.println("EventSessionBean:listEvents: a new event has been added!");
        }
        System.out.println("EventSessionBean:listEvents: all events have been fully retrieved!"+events.size());
        return events;
    }
     }
 
    

 
