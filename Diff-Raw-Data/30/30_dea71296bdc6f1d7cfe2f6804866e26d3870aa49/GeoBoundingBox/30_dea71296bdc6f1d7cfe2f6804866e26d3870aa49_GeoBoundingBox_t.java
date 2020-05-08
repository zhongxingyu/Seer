 package no.finntech.geocluster;
 
 import java.util.Arrays;
 
 public class GeoBoundingBox {
     private final GeoPoint topLeft, bottomRight;
 
     public GeoBoundingBox(GeoPoint point) {
         this(point, point);
     }
 
     public GeoBoundingBox(GeoPoint topLeft, GeoPoint bottomRight) {
         this.topLeft = topLeft;
         this.bottomRight = bottomRight;
     }
 
     public GeoPoint topLeft() {
         return topLeft;
     }
 
     public GeoPoint bottomRight() {
         return bottomRight;
     }
 
     public boolean contains(GeoPoint point) {
         return point.getLat() <= topLeft.getLat() && point.getLat() >= bottomRight.getLat() &&
                 point.getLon() >= topLeft.getLon() && point.getLon() <= bottomRight.getLon();
     }
 
     public GeoBoundingBox extend(GeoPoint point) {
         return extend(point, point);
     }
 
     public GeoBoundingBox extend(GeoBoundingBox bounds) {
         return extend(bounds.topLeft(), bounds.bottomRight());
     }
 
     public GeoBoundingBox extend(double factor) {
         double distance = size(GeoDistanceUnit.KILOMETERS) * factor;
         return extend(distance, GeoDistanceUnit.KILOMETERS);
     }
 
     public GeoBoundingBox extend(double distance, GeoDistanceUnit unit) {
        double offsetBy = unit.toKm(distance);
         return new GeoBoundingBox(topLeft().offsetBy(offsetBy, 315.0, unit), bottomRight().offsetBy(offsetBy, 135.0, unit));
     }
 
     private GeoBoundingBox extend(GeoPoint topLeft, GeoPoint bottomRight) {
         return contains(topLeft) && contains(bottomRight) ? this : new GeoBoundingBox(
                 GeoPoint.fromDegrees(Math.max(topLeft().getLat(), topLeft.getLat()), Math.min(topLeft().getLon(), topLeft.getLon())),
                 GeoPoint.fromDegrees(Math.min(bottomRight().getLat(), bottomRight.getLat()), Math.max(bottomRight().getLon(), bottomRight.getLon())));
     }
 
     public double size(GeoDistanceUnit unit) {
         return topLeft.distanceTo(bottomRight, unit);
     }
 
     @Override
     public boolean equals(Object that) {
         return that instanceof GeoBoundingBox &&
                 equals((GeoBoundingBox) that);
     }
 
     private boolean equals(GeoBoundingBox that) {
         return GeoPoints.equals(topLeft, that.topLeft()) &&
                 GeoPoints.equals(bottomRight, that.bottomRight());
     }
 
     @Override
     public int hashCode() {
         return hashCode(topLeft.toString(), bottomRight.toString());
     }
 
     private static int hashCode(Object... objects) {
         return Arrays.hashCode(objects);
     }
 
     @Override
     public String toString() {
         return GeoPoints.toString(topLeft) + ".." + GeoPoints.toString(bottomRight);
     }
 }
