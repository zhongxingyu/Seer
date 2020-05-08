 package cz.osu.poletucha.db;
 
 
 public class FlightResolved extends Flight {
 
     private String fromName;
     private String toName;
     private String planeName;
 
     public FlightResolved (Flight f, Airport from,
                            Airport to, Plane p) {
         super (f.getCode (), f.getAirportFrom (),
                f.getAirportTo (), f.getIdPlane (),
                f.getDeparture (), f.getArrival (),
                f.getCapacity ());
         this.setId (f.getId ());
         this.setFromName (from.getName ());
         this.setToName (to.getName ());
         this.setPlaneName (p.getName ());
     }
 
     public void setFromName (String name) {
         this.fromName = name;
     }
 
     public void setToName (String name) {
         this.toName = name;
     }
 
     public void setPlaneName (String name) {
        this.planeName = name;
     }
 
     public String getFromName () {
         return this.fromName;
     }
 
     public String getToName () {
         return this.toName;
     }
 
     public String getPlaneName () {
         return this.planeName;
     }
 
 }
