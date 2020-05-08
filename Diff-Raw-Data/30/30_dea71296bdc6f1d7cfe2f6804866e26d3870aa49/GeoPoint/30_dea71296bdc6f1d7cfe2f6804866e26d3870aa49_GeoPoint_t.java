 package no.finntech.geocluster;
 
 /**
  * <p>Represents a point on the surface of a sphere. (The Earth is almost spherical.)</p> <p/> <p>To create an instance, call one of the
  * static methods fromDegrees() or fromRadians().</p> <p/> <p>This code was originally published at <a
  * href="http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates#Java"> http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates#Java</a>.</p>
  *
  * @author Jan Philip Matuschek
  * @version 22 September 2010
  */
 public class GeoPoint {
     private static final double MIN_LAT = Math.toRadians(-90d);  // -PI/2
     private static final double MAX_LAT = Math.toRadians(90d);   //  PI/2
     private static final double MIN_LON = Math.toRadians(-180d); // -PI
     private static final double MAX_LON = Math.toRadians(180d);  //  PI
     private static final double EARTH_RADIUS = 6371.01d;
 
     private double radLat;  // latitude in radians
     private double radLon;  // longitude in radians
 
     private double degLat;  // latitude in degrees
     private double degLon;  // longitude in degrees
 
     private Object tag = null;
 
     private GeoPoint(double degLat, double degLon) {
         this(degLat, degLon, false);
     }
 
     private GeoPoint(double latitude, double longitude, boolean radians) {
         if (radians) {
             this.radLat = latitude;
             this.radLon = longitude;
             this.degLat = Math.toDegrees(latitude);
             this.degLon = Math.toDegrees(longitude);
         } else {
             this.radLat = Math.toRadians(latitude);
             this.radLon = Math.toRadians(longitude);
             this.degLat = latitude;
             this.degLon = longitude;
         }
 
         checkBounds();
     }
 
     private GeoPoint() {
     }
 
     /**
      * @param latitude  the latitude, in degrees.
      * @param longitude the longitude, in degrees.
      */
     public static GeoPoint fromDegrees(double latitude, double longitude) {
         return new GeoPoint(latitude, longitude, false);
     }
 
     /**
      * @param latitude  the latitude, in radians.
      * @param longitude the longitude, in radians.
      */
     public static GeoPoint fromRadians(double latitude, double longitude) {
         return new GeoPoint(latitude, longitude, true);
     }
 
     public static GeoPoint from(GeoPoint point) {
         return new GeoPoint(point.degLat, point.degLon);
     }
 
     private void checkBounds() {
         if (radLat < MIN_LAT || radLat > MAX_LAT ||
                 radLon < MIN_LON || radLon > MAX_LON) {
             throw new IllegalArgumentException();
         }
     }
 
     /**
      * @return the latitude, in degrees.
      */
     public double getLat() {
         return degLat;
     }
 
     /**
      * @return the longitude, in degrees.
      */
     public double getLon() {
         return degLon;
     }
 
     @Override
     public String toString() {
         return "(" + degLat + "\u00B0, " + degLon + "\u00B0) = (" +
                 radLat + " rad, " + radLon + " rad)";
     }
 
     /**
      * Destination point given distance and bearing from start point
      *
      * Given a start point, initial bearing, and distance, this will calculate the destination point and final bearing travelling along a (shortest distance) great circle arc.
      *
      * @param distance
      * @param bearing
      * @param unit
      * @return
      */
     public GeoPoint offsetBy(double distance, double bearing, GeoDistanceUnit unit) {
        double d = unit.toKm(distance) / EARTH_RADIUS;
         double b = Math.toRadians(bearing);
 
         double lat = Math.asin(Math.sin(radLat) * Math.cos(d) +
                 Math.cos(radLat) * Math.sin(d) * Math.cos(b));
         double lon = radLon + Math.atan2(Math.sin(b) * Math.sin(d) * Math.cos(radLat),
                 Math.cos(d) - Math.sin(radLat) * Math.sin(lat));
 
         lon = (lon + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
 
         return GeoPoint.fromRadians(lat, lon);
 
     }
 
     /**
      * Computes the great circle distance between this GeoPoint instance and {point} argument.
      *
      * @param radius the radius of the sphere, e.g. the average radius for a spherical approximation of the figure of the Earth is
      *               approximately 6371.01 kilometers.
      * @return the distance, measured in the same unit as the radius argument.
      */
     public double distanceTo(GeoPoint point, double radius, GeoDistanceUnit unit) {
         double rad = Math.sin(radLat) * Math.sin(point.radLat) +
                 Math.cos(radLat) * Math.cos(point.radLat) *
                         Math.cos(radLon - point.radLon);
 
         // Valid result is in range -1.0..+1.0
         rad = (rad < -1.0) ? -1.0 : (rad > 1.0) ? 1.0 : rad;
 
        return unit.fromKm(Math.acos(rad) * radius);
     }
 
     /* Same as above, but we assume that we're referring to coordinates on planet earth. */
     public double distanceTo(GeoPoint point, GeoDistanceUnit unit) {
         return distanceTo(point, EARTH_RADIUS, unit);
     }
 
     /**
      * <p>Computes the bounding coordinates of all points on the surface of a sphere that have a great circle distance to the point
      * represented by this GeoPoint instance that is less or equal to the distance argument.</p> <p>For more information about the formulae
      * used in this method visit <a href="http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates">
      * http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates</a>.</p>
      *
      * @param distance the distance from the point represented by this GeoPoint instance. Must me measured in the same unit as the radius
      *                 argument.
      * @param radius   the radius of the sphere, e.g. the average radius for a spherical approximation of the figure of the Earth is
      *                 approximately 6371.01 kilometers.
      * @return an array of two GeoPoint objects such that:<ul> <li>The latitude of any point within the specified distance is greater or
      * equal to the latitude of the first array element and smaller or equal to the latitude of the second array element.</li> <li>If the
      * longitude of the first array element is smaller or equal to the longitude of the second element, then the longitude of any point
      * within the specified distance is greater or equal to the longitude of the first array element and smaller or equal to the longitude
      * of the second array element.</li> <li>If the longitude of the first array element is greater than the longitude of the second element
      * (this is the case if the 180th meridian is within the distance), then the longitude of any point within the specified distance is
      * greater or equal to the longitude of the first array element <strong>or</strong> smaller or equal to the longitude of the second
      * array element.</li> </ul>
      */
     public GeoPoint[] boundingCoordinates(double distance, double radius, GeoDistanceUnit unit) {
        distance = unit.toKm(distance);
        radius = unit.toKm(distance);
 
         if (radius < 0d || distance < 0d)
             throw new IllegalArgumentException();
 
         // angular distance in radians on a great circle
         double radDist = distance / radius;
 
         double minLat = radLat - radDist;
         double maxLat = radLat + radDist;
 
         double minLon, maxLon;
         if (minLat > MIN_LAT && maxLat < MAX_LAT) {
             double deltaLon = Math.asin(Math.sin(radDist) /
                     Math.cos(radLat));
             minLon = radLon - deltaLon;
             if (minLon < MIN_LON) {
                 minLon += 2d * Math.PI;
             }
             maxLon = radLon + deltaLon;
             if (maxLon > MAX_LON) {
                 maxLon -= 2d * Math.PI;
             }
         } else {
             // a pole is within the distance
             minLat = Math.max(minLat, MIN_LAT);
             maxLat = Math.min(maxLat, MAX_LAT);
             minLon = MIN_LON;
             maxLon = MAX_LON;
         }
 
         return new GeoPoint[]{fromRadians(minLat, minLon),
                 fromRadians(maxLat, maxLon)};
     }
 
     /* Same as above, but we assume that we're referring to coordinates on planet earth. */
     public GeoPoint[] boundingCoordinates(double distance, GeoDistanceUnit unit) {
         return boundingCoordinates(distance, EARTH_RADIUS, unit);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof GeoPoint)) return false;
 
         GeoPoint geoPoint = (GeoPoint) o;
 
         if (Double.compare(geoPoint.degLat, degLat) != 0) return false;
         if (Double.compare(geoPoint.degLon, degLon) != 0) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result;
         long temp;
         temp = Double.doubleToLongBits(degLat);
         result = (int) (temp ^ (temp >>> 32));
         temp = Double.doubleToLongBits(degLon);
         result = 31 * result + (int) (temp ^ (temp >>> 32));
         return result;
     }
 
     public Object getTag() {
         return tag;
     }
 
     public void setTag(Object tag) {
         this.tag = tag;
     }
 }
