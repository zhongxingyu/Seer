 package com.teamgrau.altourism.util.data;
 
 import java.util.List;
 import com.google.android.gms.maps.model.LatLng;
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
 	List<POI> listPOIs( LatLng position, double radius );
 }
