 package com.teamgrau.altourism.util.data;
 
 import java.util.List;
 
 import android.location.Location;
 import com.teamgrau.altourism.util.data.model.POI;
 /**
  * Ein StoryProvider gibt Zugriff auf die interessanten Punkte in der
  * Umgebung.
  * @author simon
  *
  */
 public interface StoryProvider {
 
 	/**
 	 * Returns a list of POI's lying in the circle-area defined by position
 	 * and radius
 	 * 
 	 * @param position Center of the area 
 	 * @param radius Radius of the circle-area
 	 * @return List of POI's lying in the circle
 	 */
 	List<POI> listPOIs( Location position, double radius );
 
     /**
      * Asyncronous method for storyproviders who use network communications
      * Returns a list of POI's lying in the circle-area defined by position
      * and radius
      *
      * @param position Center of the area
      * @param radius Radius of the circle-area
      */
     void listPOIs( Location position, double radius, OnStoryProviderFinishedListener l );
 
}

 
     /**
      * Returns the POI object that corresponds to the given position
      * and radius
      *
      * @param position Center of the area
      * @return List of POI's lying in the circle
      */
     POI getPOI( Location position);
 }
