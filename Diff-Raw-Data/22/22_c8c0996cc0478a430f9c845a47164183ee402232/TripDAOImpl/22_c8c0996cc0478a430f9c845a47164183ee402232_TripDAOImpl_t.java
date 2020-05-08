 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa165.jtravelagency.dao;
 
 import cz.muni.fi.pa165.jtravelagency.entity.Trip;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
 import javax.persistence.TypedQuery;
 import org.joda.time.LocalDate;
 import org.springframework.stereotype.Repository;
 
 /**
  *
  * @author Jakub Marecek (404364)
  */
 @Repository
 public class TripDAOImpl implements TripDAO {
 
    @PersistenceContext(type = PersistenceContextType.EXTENDED)
     private EntityManager em;
 
     public TripDAOImpl() {
     }
      
     public TripDAOImpl(EntityManager em) {
         if (em == null) {
             throw new IllegalArgumentException("Entity Manager can not be null");
         }
         this.em = em;
     }
     
     
     public void createTrip(Trip trip) {
         if (trip == null) {
             throw new IllegalArgumentException("Trip can not be null");
         }
         if (trip.getId() != null) {
             throw new IllegalArgumentException("Trip id has to bed null when new trip is created");
         }
         validateTrip(trip);
         em.persist(trip);
         em.flush();
         em.detach(trip);
     }
 
     public Trip getTrip(Long id) {
         if (id == null) {
             throw new IllegalArgumentException("Trip can not be null when getting trip");
         }
         return em.find(Trip.class, id);
     }
 
     public void updateTrip(Trip trip) {
         if (trip == null) {
             throw new IllegalArgumentException("Trip can not be null when updated");
         }
         if (trip.getId() == null) {
             throw new IllegalArgumentException("Trip id can not be null when trip is updated");
         }
         validateTrip(trip);
         em.merge(trip);
         em.flush();
         em.detach(trip);
     }
 
     public void deleteTrip(Trip trip) {
         if (trip == null) {
             throw new IllegalArgumentException("Trip can not be null when updated");
         }
         if (trip.getId() == null) {
             throw new IllegalArgumentException("Trip id can not be null when trip is updated");
         }
         validateTrip(trip);
         em.remove(trip);
         em.flush();
         em.detach(trip);        
     }
 
     public List<Trip> getAllTrips() {
         TypedQuery<Trip> query = em.createNamedQuery(
                 "getAllTrips", Trip.class);
         return (List<Trip>) query.getResultList();
     }
 
     public List<Trip> findTripsByDateRange(LocalDate from, LocalDate to) {
         TypedQuery<Trip> query = em.createNamedQuery(
                 "findTripsByDateRange", Trip.class);        
         query.setParameter("from", from);
         query.setParameter("to", to);
         return (List<Trip>) query.getResultList();
     }
 
     public List<Trip> findTripsByDestination(String destination) {
         TypedQuery<Trip> query = em.createNamedQuery(
                 "findTripsByDestination", Trip.class);        
         query.setParameter("destination", destination);
         return (List<Trip>) query.getResultList();
     }
     
     private void validateTrip(Trip trip) {
         if (trip.getDateFrom() == null) {
             throw new IllegalArgumentException("Trip date from can not be null");
         }
         if (trip.getDateTo() == null) {
             throw new IllegalArgumentException("Trip date to can not be null");
         }
         if (trip.getDestination() == null) {
             throw new IllegalArgumentException("Trip destination can not be null");
         }
     }
 }
