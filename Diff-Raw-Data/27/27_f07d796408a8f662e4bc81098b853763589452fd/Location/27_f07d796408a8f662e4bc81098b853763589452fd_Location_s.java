 package tk.hysteriaproject.games.api;
 
 /**
  * Describes a point in a 3-Dimensional plane system (X, Y, Z)
  * Creator: KingBowser
  * Date: 2/14/13
  * Time: 11:57 AM
  * Refer to LICENSE for licensing information
  */
public interface Location {
 
     /**
      * Get the X-Location
      *
      * @return X-Plane point
      */
    public double getXLocation();
 
     /**
      * Get the Y-Location
      *
      * @return Y-Plane point
      */
    public double getYLocation();
 
     /**
      * Get the Z-Location
      *
      * @return Z-Plane point
      */
    public double getZLocation();
 
 }
