 package tk.hysteriaproject.games.api;
 
 /**
  * A location modifiable outside of its implementation
  * Creator: KingBowser
  * Date: 2/14/13
  * Time: 12:30 PM
  * Refer to LICENSE for licensing information
  */
public interface LocationSettable extends Location {
 
     /**
      * Set the new X-Plane location
      *
      * @param newXLocation new X-Plane location
      */
    public void setXLocation(int newXLocation);
 
     /**
      * Set the new Y-Plane location
      *
      * @param newYLocation new Y-Plane location
      */
    public void setYLocation(int newYLocation);
 
     /**
      * Set the new Z-Plane location
      *
      * @param newZLocation new Z-Plane location
      */
    public void setZLocation(int newZLocation);
 
 }
