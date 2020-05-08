 package cz.cvut.fel.beans;
 
 import cz.cvut.fel.model.Flight;
 import cz.cvut.fel.service.FlightService;
 import lombok.EqualsAndHashCode;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
import javax.validation.Valid;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 
 /** @author Karel Cemus */
 @RequestScoped
 @Named( "flights" )
 @EqualsAndHashCode( callSuper = false )
 public class FlightsBean extends BeanBase implements Serializable {
 
     @Inject
     private FlightService service;
 
    @Valid
     @Inject
     private FlightOverviewConfiguration configuration;
 
     private Collection<Flight> flights;
 
     public Collection<Flight> getFlights() {
         if ( flights == null ) {
             flights = filter();
         }
         return flights;
     }
 
 
     public Collection<Flight> filter() {
 
         Date dateFrom = configuration.getDateFrom();
         Date dateTo = configuration.getDateTo();
         String departureFrom = configuration.getDepartureFrom();
         String arrivalTo = configuration.getArrivalTo();
 
         // neither destination is set
         if ( departureFrom == null && arrivalTo == null ) {
             return Collections.emptyList();
         }
 
         if ( departureFrom == null || departureFrom.isEmpty() ) {
             return service.findFlightsTo( dateFrom, dateTo, arrivalTo );
         } else if ( arrivalTo == null || arrivalTo.isEmpty() ) {
             return service.findFlightsFrom( dateFrom, dateTo, departureFrom );
         } else {
             return service.findFlights( dateFrom, dateTo, departureFrom, arrivalTo );
         }
     }
 
     public String remove( String number ) {
         try {
             service.delete( number );
             addInformation( "Flight was removed." );
         } catch ( Throwable ex ) {
             addError( "No such flight exists." );
         }
 
         return "flights";
     }
 }
