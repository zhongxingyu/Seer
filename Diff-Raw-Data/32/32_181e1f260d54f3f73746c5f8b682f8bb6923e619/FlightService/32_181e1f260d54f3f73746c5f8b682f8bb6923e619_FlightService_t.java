 
 package cz.muni.fi.pa165.airportmanager.backend.JPAs.services;
 
 import cz.muni.fi.pa165.airportmanager.backend.entities.Flight;
 import cz.muni.fi.pa165.airportmanager.backend.entities.to.FlightTO;
 import java.util.List;
 import org.springframework.dao.DataAccessException;
 
 /**
  * service layer that uses DAO layer
  * 
  * @author Filip
  */
 public interface FlightService {
     
     /**
      * Creates Flight in DB.
      * 
      * @param flightTO flightTO to be created
      * @throws ServiceDataAccessException
      */
     void createFlight(FlightTO flightTO) throws ServiceDataAccessException;
     
     /**
      * Updates Flight in DB.
      * 
      * @param flightTO flightTO to be removed
      * @throws ServiceDataAccessException
      */
     void updateFlight(FlightTO flightTO) throws ServiceDataAccessException;
     
     /**
      * Removes Flight from DB
      * 
      * @param flightTO flightTO to be removed
      * @throws ServiceDataAccessException
      */
     void removeFlight(FlightTO flightTO) throws ServiceDataAccessException;
     
     /**
      * Finds and return flightTO in DB according to id
      * 
      * @param id id of flightTO
      * @return found flight
      * @throws ServiceDataAccessException
      */
     FlightTO getFlight(Long id) throws ServiceDataAccessException;
     
     /**
      * Finds all flights in DB
      * 
      * @return List of FlightTOs in DB
     * @throws ServiceDataAccessException
      */
     List<FlightTO> getAllFlights() throws ServiceDataAccessException;
     
 }
