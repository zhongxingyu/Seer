 package cz.muni.fi.pa165.airportmanager.backend.JPAs;
 
 import cz.muni.fi.pa165.airportmanager.backend.daos.FlightDAO;
 import cz.muni.fi.pa165.airportmanager.backend.entities.Flight;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Query;
 
 /**
  * JPA implementation of FlightDAO.
  * 
  * @author Filip
  */
 //@Transactional
 public class FlightDAOImpl implements FlightDAO {
     
 //    @PersistenceContext
     private EntityManagerFactory factory;
 
     public void setEntityManagerFactory(EntityManagerFactory factory) {
         this.factory = factory;
     }
     
    @Override
     public void createFlight(Flight flight) throws IllegalArgumentException {
         if(flight == null) {
             throw new IllegalArgumentException("Flight is null.");
         } else if(flight.getId() != null) {
             throw new IllegalArgumentException("Flight id is already assigned.");
         } else if(flight.getDepartureTime() == null || flight.getArrivalTime() == null) {
             throw new IllegalArgumentException("Arrival or departure time is null.");
         } else if(flight.getStewardList() == null) {
             throw new IllegalArgumentException("Steward list is null.");
         } else if(flight.getOrigin() == null || flight.getTarget() == null) {
             throw new IllegalArgumentException("Origin or target destination is null.");
         } else if(flight.getAirplane() == null) {
             throw new IllegalArgumentException("Airplane is null.");
         } else if(flight.getDepartureTime().after(flight.getArrivalTime()) || flight.getArrivalTime().before(flight.getDepartureTime())) {
             throw new IllegalArgumentException("Invalid Departure/Arrival time, wrong order.");
         } else {
             EntityManager em = factory.createEntityManager();
             em.getTransaction().begin();
             em.persist(flight);
             em.getTransaction().commit();
             em.close();
         }
     }
     
    @Override
     public void updateFlight(Flight flight) throws JPAException, IllegalArgumentException {
         if(flight == null) {
             throw new IllegalArgumentException("Flight is null.");
         } else if(flight.getId() == null) {
             throw new IllegalArgumentException("Flight id is not assigned.");
         } else if(flight.getDepartureTime() == null || flight.getArrivalTime() == null) {
             throw new IllegalArgumentException("Arrival or departure time is null.");
         } else if(flight.getStewardList() == null) {
             throw new IllegalArgumentException("Steward list is null.");
         } else if(flight.getOrigin() == null || flight.getTarget() == null) {
             throw new IllegalArgumentException("Origin or target destination is null.");
         } else if(flight.getAirplane() == null) {
             throw new IllegalArgumentException("Airplane is null.");
         } else if(flight.getDepartureTime().after(flight.getArrivalTime()) || flight.getArrivalTime().before(flight.getDepartureTime())) {
             throw new IllegalArgumentException("Invalid Departure/Arrival time, wrong order.");
         } else {
             EntityManager em = factory.createEntityManager();
             if(em.find(Flight.class, flight.getId()) == null) {
                 throw new JPAException("Flight in database is null.");
             }
             em.getTransaction().begin();
             em.merge(flight);
             em.getTransaction().commit();
             em.close();
         }
     }
     
    @Override
     public void removeFlight(Flight flight) throws JPAException, IllegalArgumentException {
         if(flight == null) {
             throw new IllegalArgumentException("Flight flight to be removed is null.");
         } else if(flight.getId() == null) {
             throw new IllegalArgumentException("Flight flight to be removed id is not assigned.");
         } else {
             EntityManager em = factory.createEntityManager();
             Flight flightToBeDeleted = em.find(Flight.class, flight.getId());
             if (flightToBeDeleted == null) {
                 throw new JPAException("Flight in database is null.");
             }
             em.getTransaction().begin();
             em.remove(flightToBeDeleted);
             em.getTransaction().commit();
             em.close();
         }
     }
     
    @Override
     public Flight getFlight(Long id) throws JPAException, IllegalArgumentException {
         if(id == null) {
             throw new IllegalArgumentException("Id is null.");
         }
         EntityManager em = factory.createEntityManager();
         Flight toReturn = em.find(Flight.class, id);
         if(toReturn == null) {
             throw new JPAException("Flight is not in database.");
         }
         em.close();
         return toReturn;
     }
     
    @Override
     public List<Flight> getAllFlight() throws JPAException{
         EntityManager em = factory.createEntityManager();
         Query query = em.createQuery("SELECT p FROM Flight p ");
         List<Flight> allFlights = query.getResultList();
         em.close();
         return allFlights;
     }
 }
