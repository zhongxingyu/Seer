 package tk.hysteriaproject.games.api;
 
 /**
  * A location modifiable outside of its implementation
  * Creator: KingBowser
  * Date: 2/14/13
  * Time: 12:30 PM
  * Refer to LICENSE for licensing information
  */
public interface LocationSettable<N extends Number> extends Location<N> {
 
     /**
      * Set the new X-Plane location
      *
      * @param newXLocation new X-Plane location
      */
    public void setXLocation(N newXLocation);
 
     /**
      * Set the new Y-Plane location
      *
      * @param newYLocation new Y-Plane location
      */
    public void setYLocation(N newYLocation);
 
     /**
      * Set the new Z-Plane location
      *
      * @param newZLocation new Z-Plane location
      */
    public void setZLocation(N newZLocation);
 
 }
