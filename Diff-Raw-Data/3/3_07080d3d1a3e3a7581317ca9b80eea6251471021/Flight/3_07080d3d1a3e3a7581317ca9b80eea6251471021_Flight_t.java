 package dat076.frukostklubben.model;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.NamedQuery;
 import javax.persistence.Temporal;
import javax.validation.constraints.NotNull;
 
 /**
  *
  * @author fredrik
  */
 @Entity
 @NamedQuery(name = "findAllFlights", query = "SELECT f FROM Flight f")
 public class Flight implements Serializable {
     @Id @GeneratedValue
     private long id;
     private String name;
     private String fromAirport;
     private String toAirport;
     private Double cost;
    @NotNull(message="Please select date an time.")
     @Temporal(javax.persistence.TemporalType.DATE)
     private Date departureTime;
 
     public Flight() {
     }
 
     /**
      *
      * @param id
      * @param name
      * @param fromAirport
      * @param toAirport
      * @param departureTime
      */
     public Flight(String name, String fromAirport, String toAirport, Date departureTime) {
         this.name = name;
         this.fromAirport = fromAirport;
         this.toAirport = toAirport;
         this.departureTime = departureTime;
     }
     public long getId(){
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getFromAirport() {
         return fromAirport;
     }
 
     public void setFromAirport(String fromAirport) {
         this.fromAirport = fromAirport;
     }
 
     public String getToAirport() {
         return toAirport;
     }
 
     public void setToAirport(String toAirport) {
         this.toAirport = toAirport;
     }
 
     public Date getDepartureTime() {
         return departureTime;
     }
 
     /**
      *
      * @param departureTime
      */
     public void setDepartureTime(Date departureTime) {
         this.departureTime = departureTime;
     }
 
     public Double getCost() {
         return cost;
     }
 
     public void setCost(Double cost) {
         this.cost = cost;
     }
 }
