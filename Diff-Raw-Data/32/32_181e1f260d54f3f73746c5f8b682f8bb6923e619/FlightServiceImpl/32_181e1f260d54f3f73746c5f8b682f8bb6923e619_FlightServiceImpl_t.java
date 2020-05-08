 package cz.muni.fi.pa165.airportmanager.backend.JPAs.services;
 
 import cz.muni.fi.pa165.airportmanager.backend.JPAs.JPAException;
 import cz.muni.fi.pa165.airportmanager.backend.daos.AirplaneDAO;
 import cz.muni.fi.pa165.airportmanager.backend.daos.DestinationDAO;
 import cz.muni.fi.pa165.airportmanager.backend.daos.FlightDAO;
 import cz.muni.fi.pa165.airportmanager.backend.daos.StewardDAO;
 import cz.muni.fi.pa165.airportmanager.backend.entities.Flight;
 import cz.muni.fi.pa165.airportmanager.backend.entities.to.EntityDTOTransformer;
 import static cz.muni.fi.pa165.airportmanager.backend.entities.to.EntityDTOTransformer.*;
 import cz.muni.fi.pa165.airportmanager.backend.entities.to.FlightTO;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 /**
  *
  * @author Filip
  */
 @Service
 public class FlightServiceImpl implements FlightService {
 
     @Autowired
     private FlightDAO flightDAO;
     
     @Autowired
     private StewardDAO stewDAO;
     
     @Autowired
     private DestinationDAO destDAO;
     
     @Autowired
     private AirplaneDAO airplaneDAO;
 
     public void setFlightDAO(FlightDAO flightDAO) {
         this.flightDAO = flightDAO;
     }
 
     @Override
     public void createFlight(FlightTO flightTO) throws ServiceDataAccessException {
         if(flightTO == null) {
             throw new ServiceDataAccessException("FlightTO is null.");
         }
         Flight flight = EntityDTOTransformer.flightTOConvert(flightTO);
         if(flight.getStewardList() == null) {
             throw new ServiceDataAccessException("Flight StewardList is null.");
         } 
         if(flight.getAirplane() == null) {
             throw new ServiceDataAccessException("Flight Airplane is null.");
         }
         if(flight.getOrigin()==null) {
             throw new ServiceDataAccessException("Flight Origin Destination is null.");
         }
         if(flight.getTarget()==null) {
             throw new ServiceDataAccessException("Flight Target Destination is null.");
         }
         for(int i=0; i<flight.getStewardList().size(); i++){
             try {
                 stewDAO.createSteward(flight.getStewardList().get(i));
                 flightTO.getStewList().get(i).setId(flight.getStewardList().get(i).getId());
             } catch (JPAException | IllegalArgumentException ex) {
                 throw new ServiceDataAccessException("Error when creating steward (service).", ex);
             }
         }
         airplaneDAO.createAirplane(flight.getAirplane());
         flightTO.getAirplaneTO().setId(flight.getAirplane().getId());
         destDAO.createDestination(flight.getOrigin());
         flightTO.getOrigin().setId(flight.getOrigin().getId());
         destDAO.createDestination(flight.getTarget());
         flightTO.getTarget().setId(flight.getTarget().getId());
         flightDAO.createFlight(flight);
         flightTO.setId(flight.getId());
     }
 
     @Override
     public void updateFlight(FlightTO flightTO) throws ServiceDataAccessException {
         if(flightTO == null) {
             throw new ServiceDataAccessException("flightTO is null.");
         }
        try {
            flightDAO.updateFlight(EntityDTOTransformer.flightTOConvert(flightTO));
        } catch (JPAException | IllegalArgumentException ex) {
            throw new ServiceDataAccessException("Error when updating flight (service).", ex);
        }
     }
 
     @Override
     public void removeFlight(FlightTO flightTO) throws ServiceDataAccessException {
         if(flightTO == null) {
             throw new ServiceDataAccessException("flightTO is null.");
         }
        try {
            flightDAO.removeFlight(EntityDTOTransformer.flightTOConvert(flightTO));
        } catch (JPAException | IllegalArgumentException ex) {
            throw new ServiceDataAccessException("Error when removing flight (service).", ex);
        }
     }
 
     @Override
     public FlightTO getFlight(Long id) throws ServiceDataAccessException {
         if(id == null) {
             throw new ServiceDataAccessException("Id is null.");
         }
         try {
             return EntityDTOTransformer.flightConvert(flightDAO.getFlight(id));
         } catch (JPAException | IllegalArgumentException ex) {
             throw new ServiceDataAccessException("Error when obtaining flight (service).", ex);
         }
     }
 
     @Override
     public List<FlightTO> getAllFlights() throws ServiceDataAccessException {
         try {
             List<Flight> flights = flightDAO.getAllFlight();
             return flightListConvert(flights);
         } catch (JPAException ex) {
             throw new ServiceDataAccessException("Error when obtaining all flights (service).", ex);
         }
     }
 }
