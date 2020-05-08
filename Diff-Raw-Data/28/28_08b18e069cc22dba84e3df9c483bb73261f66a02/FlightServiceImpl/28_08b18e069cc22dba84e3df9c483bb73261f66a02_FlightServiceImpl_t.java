 package cz.muni.fi.pa165.airportmanager.backend.services.impl;
 
 import cz.muni.fi.pa165.airportmanager.backend.daos.AirplaneDAO;
 import cz.muni.fi.pa165.airportmanager.backend.daos.DestinationDAO;
 import cz.muni.fi.pa165.airportmanager.backend.daos.FlightDAO;
 import cz.muni.fi.pa165.airportmanager.backend.daos.StewardDAO;
 import cz.muni.fi.pa165.airportmanager.backend.entities.Flight;
 import cz.muni.fi.pa165.airportmanager.backend.entities.EntityDTOTransformer;
 import static cz.muni.fi.pa165.airportmanager.backend.entities.EntityDTOTransformer.*;
 import cz.muni.fi.pa165.airportmanager.services.FlightService;
 import cz.muni.fi.pa165.airportmanager.transferobjects.FlightTO;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Filip
  */
 @Service
 @Transactional
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
 
     public void setStewDAO(StewardDAO stewDAO) {
         this.stewDAO = stewDAO;
     }
 
     public void setDestDAO(DestinationDAO destDAO) {
         this.destDAO = destDAO;
     }
 
     public void setAirplaneDAO(AirplaneDAO airplaneDAO) {
         this.airplaneDAO = airplaneDAO;
     }
 
     @Override
     public void createFlight(FlightTO flightTO) throws DataAccessException {
         Flight flight = EntityDTOTransformer.flightTOConvert(flightTO);
        if (flight != null) {
            for(int i = 0; i < flight.getStewardList().size(); i++){
                stewDAO.createSteward(flight.getStewardList().get(i));
                flightTO.getStewList().get(i).setId(flight.getStewardList().get(i).getId());
            }
        }
        if (flight.getAirplane() != null) {
            airplaneDAO.createAirplane(flight.getAirplane());
            flightTO.getAirplaneTO().setId(flight.getAirplane().getId());
        }
        if (flight.getOrigin() != null) {
            destDAO.createDestination(flight.getOrigin());
            flightTO.getOrigin().setId(flight.getOrigin().getId());
        }
        if (flight.getTarget() != null) {
            destDAO.createDestination(flight.getTarget());
            flightTO.getTarget().setId(flight.getTarget().getId());
         }
         flightDAO.createFlight(flight);
         flightTO.setId(flight.getId());
     }
 
     @Override
     public void updateFlight(FlightTO flightTO) throws DataAccessException {
         flightDAO.updateFlight(EntityDTOTransformer.flightTOConvert(flightTO));
     }
 
     @Override
     public void removeFlight(FlightTO flightTO) throws DataAccessException {
         flightDAO.removeFlight(EntityDTOTransformer.flightTOConvert(flightTO));
     }
 
     @Override
     public FlightTO getFlight(Long id) throws DataAccessException {
         return EntityDTOTransformer.flightConvert(flightDAO.getFlight(id));
     }
 
     @Override
     public List<FlightTO> getAllFlights() throws DataAccessException {
         List<Flight> flights = flightDAO.getAllFlight();
         return flightListConvert(flights);
     }
 }
