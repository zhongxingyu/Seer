 package models;
 
 import javax.persistence.Entity;
 
 @Entity
 public class TripOffer extends Trip {
 
     private static final long serialVersionUID = -386674185196292407L;
    public static Finder<Integer, TripOffer> find = new Finder<Integer, TripOffer>(Integer.class, TripOffer.class);
 
 
     public TripOffer() {
         super();
     }
 
 
 }
