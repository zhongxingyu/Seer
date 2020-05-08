 package cz.cvut.fel.beans;
 
 import cz.cvut.fel.utils.DateUtils;
 import lombok.Data;
 
 import javax.faces.bean.ViewScoped;
 import javax.inject.Named;
 import javax.validation.constraints.AssertTrue;
 import javax.validation.constraints.NotNull;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.TimeZone;
 
 /**
  * <p>Set of view parameters required for flights overview.</p>
  *
  * @author Karel Cemus
  */
 @Data
 @ViewScoped
 @Named( "overviewConfiguration" )
 public class FlightOverviewConfiguration implements Serializable {
 
    private static final TimeZone TIMEZONE = TimeZone.getDefault();
 
     // in default look at today
     @NotNull
     private Date dateFrom = DateUtils.date( 1, 1, 2012 );
 
     // initialize default look 1 month forward
     @NotNull
     private Date dateTo = DateUtils.addMonths( dateFrom, 1 );
 
     private String departureFrom;
 
     private String arrivalTo;
 
    public static TimeZone getTimezone() {
         return TIMEZONE;
     }
 
     @AssertTrue( message = "At least one destination has too be set" )
     public boolean isDestinationSet() {
         return ( departureFrom != null && !departureFrom.isEmpty() ) || ( arrivalTo != null && !arrivalTo.isEmpty() );
     }
 }
