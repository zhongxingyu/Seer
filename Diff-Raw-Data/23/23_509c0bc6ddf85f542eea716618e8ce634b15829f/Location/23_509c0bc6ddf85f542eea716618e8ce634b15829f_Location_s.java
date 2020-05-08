 package models;
 
 import javax.persistence.Entity;
 import javax.persistence.OneToOne;
 
 import play.db.jpa.Model;
 
 @Entity
 public class Location extends Model
 {
 
 	private Incident incident;
 	private String mLat;
     private String mLong;
     
     @OneToOne(mappedBy = "location")
     public Incident getIncident() {
 		return incident;
 	}
 
 	public void setIncident(Incident incident) {
 		this.incident = incident;
 	}
 
	/**
     * @param aLat
     * @param aLong
     */
    public Location(String aLat, String aLong)
     {
         mLat = aLat;
         mLong = aLong;
     }
 
     /**
      * @return the lat
      */
     public String getLat()
     {
         return mLat;
     }
 
     /**
      * @param aLat the lat to set
      */
     public void setLat(String aLat)
     {
         mLat = aLat;
     }
 
     /**
      * @return the long
      */
     public String getLong()
     {
         return mLong;
     }
 
     /**
      * @param aL the long to set
      */
     public void setLong(String aL)
     {
         mLong = aL;
     }
 
 }
