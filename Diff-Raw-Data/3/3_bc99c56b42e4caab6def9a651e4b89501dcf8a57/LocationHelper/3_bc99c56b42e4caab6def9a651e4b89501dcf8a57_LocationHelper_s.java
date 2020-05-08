 /**
  * 
  */
 package fr.utc.nf33.ins.location;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import android.database.Cursor;
 
 /**
  * 
  * @author
  * 
  */
 public final class LocationHelper {
   //
   private static final double DEGREES_TO_RADIANS = Math.PI / 180.0;
   //
   private static final double RADIUS_OF_THE_EARTH = 6371.009;
   //
   public static final byte SNR_THRESHOLD = 35;
   //
   private static final double SQUARED_MAX_DISTANCE = 100.0;
   //
   private static final double SQUARED_RADIUS_OF_THE_EARTH = RADIUS_OF_THE_EARTH
       * RADIUS_OF_THE_EARTH;
 
   //
   static final List<Building> getCloseBuildings(final double latitude, final double longitude,
       Cursor cursor) {
     List<Building> closeBuildings = new ArrayList<Building>();
 
     int _idIdx = cursor.getColumnIndexOrThrow("_id");
     int bNameIdx = cursor.getColumnIndexOrThrow("bName");
     int epNameIdx = cursor.getColumnIndexOrThrow("epName");
     int epFloorIdx = cursor.getColumnIndexOrThrow("epFloor");
     int epLatitudeIdx = cursor.getColumnIndexOrThrow("epLatitude");
     int epLongitudeIdx = cursor.getColumnIndexOrThrow("epLongitude");
 
     Building currentBuilding = null;
     int currentId = -1;
     while (cursor.moveToNext()) {
       double epLatitude = cursor.getDouble(epLatitudeIdx);
       double epLongitude = cursor.getDouble(epLongitudeIdx);
       double sqDist = squaredDistanceBetween(latitude, longitude, epLatitude, epLongitude);
       if (sqDist <= SQUARED_MAX_DISTANCE) {
         int id = cursor.getInt(_idIdx);
         if (currentId != id) {
           currentId = id;
           String bName = cursor.getString(bNameIdx);
           currentBuilding = new Building(bName, new ArrayList<EntryPoint>());
           closeBuildings.add(currentBuilding);
         }
 
         String epName = cursor.getString(epNameIdx);
         byte epFloor = (byte) cursor.getInt(epFloorIdx);
         currentBuilding.getEntryPoints().add(
             new EntryPoint(epName, epFloor, epLatitude, epLongitude));
       }
     }
 
     for (Building b : closeBuildings) {
       List<EntryPoint> entryPoints = b.getEntryPoints();
       Collections.sort(entryPoints, new Comparator<EntryPoint>() {
         @Override
         public final int compare(EntryPoint lhs, EntryPoint rhs) {
           double lhsSqDist =
               squaredDistanceBetween(latitude, longitude, lhs.getLatitude(), lhs.getLongitude());
           double rhsSqDist =
               squaredDistanceBetween(latitude, longitude, rhs.getLatitude(), rhs.getLongitude());
           if (lhsSqDist < rhsSqDist)
             return -1;
           else if (lhsSqDist == rhsSqDist)
             return 0;
           else
             return 1;
         }
       });
     }
     Collections.sort(closeBuildings, new Comparator<Building>() {
       @Override
       public final int compare(Building lhs, Building rhs) {
         double lhsSqDist =
             squaredDistanceBetween(latitude, longitude, lhs.getEntryPoints().get(0).getLatitude(),
                 lhs.getEntryPoints().get(0).getLongitude());
         double rhsSqDist =
             squaredDistanceBetween(latitude, longitude, rhs.getEntryPoints().get(0).getLatitude(),
                 rhs.getEntryPoints().get(0).getLongitude());
         if (lhsSqDist < rhsSqDist)
           return -1;
         else if (lhsSqDist == rhsSqDist)
           return 0;
         else
           return 1;
       }
     });
 
     return closeBuildings;
   }
 
   /**
    * 
    * @param snr
    * @param closeBuildings
    * @return
    */
   public static final boolean shouldGoIndoor(float snr, List<Building> closeBuildings) {
    if ((snr >= SNR_THRESHOLD) || (closeBuildings.size() != 1)) return false;
 
     return closeBuildings.get(0).getEntryPoints().size() == 1;
   }
 
   /**
    * 
    * @param snr
    * @return
    */
   public static final boolean shouldGoOutdoor(float snr) {
     return snr >= SNR_THRESHOLD;
   }
 
   /**
    * Computes the approximate squared distance in meters between two locations.
    * 
    * @param startLatitude the starting latitude
    * @param startLongitude the starting longitude
    * @param endLatitude the ending latitude
    * @param endLongitude the ending longitude
    * @return the computed squared distance
    */
   private static final double squaredDistanceBetween(double startLatitude, double startLongitude,
       double endLatitude, double endLongitude) {
     double sLat = startLatitude * DEGREES_TO_RADIANS;
     double sLon = startLongitude * DEGREES_TO_RADIANS;
     double eLat = endLatitude * DEGREES_TO_RADIANS;
     double eLon = endLongitude * DEGREES_TO_RADIANS;
 
     double diffLat = eLat - sLat;
     double diffLon = eLon - sLon;
     double meanLat = (sLat + eLat) / 2.0;
 
     double a = Math.cos(meanLat) * diffLon;
 
     return SQUARED_RADIUS_OF_THE_EARTH * ((diffLat * diffLat) + (a * a)) * 1000;
   }
 
   // Suppress default constructor for noninstantiability.
   private LocationHelper() {
 
   }
 }
