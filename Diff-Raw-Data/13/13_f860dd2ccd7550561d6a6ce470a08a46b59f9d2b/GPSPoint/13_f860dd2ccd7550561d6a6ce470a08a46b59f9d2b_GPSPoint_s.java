 package org.jdleesmiller.trackjd.data;
 
 import android.location.Location;
 
 /**
  * A GPS measurement. Accuracy, altitude, bearing and speed are NaN if they
  * were not recorded. Number of satellites is Short.MIN_VALUE if it was not
  * recorded.
  */
 public class GPSPoint extends AbstractPoint {
   private final double latitude;
   private final double longitude;
   private final float accuracy;
   private final double altitude;
   private final float bearing;
   private final float speed;
  private final short numSatellites;
 
   public GPSPoint(Location location) {
     super(location.getTime());
 
     this.latitude = location.getLatitude();
     this.longitude = location.getLongitude();
 
     if (location.hasAccuracy()) {
       this.accuracy = location.getAccuracy();
     } else {
       this.accuracy = Float.NaN;
     }
 
     if (location.hasAltitude()) {
       this.altitude = location.getAltitude();
     } else {
       this.altitude = Double.NaN;
     }
 
     if (location.hasBearing()) {
       this.bearing = location.getBearing();
     } else {
       this.bearing = Float.NaN;
     }
 
     if (location.hasSpeed()) {
       this.speed = location.getSpeed();
     } else {
       this.speed = Float.NaN;
     }
 
    // TODO is this really a short?
     if (location.getExtras().containsKey("satellites")) {
      this.numSatellites = location.getExtras().getShort("satellites");
     } else {
      this.numSatellites = Short.MIN_VALUE;
     }
   }
 
   @Override
   public String getTag() {
     return "gps_v1";
   }
 
   @Override
   public void printCsvHeader(StringBuilder sb) {
     sb.append("latitude,longitude,accuracy,altitude,bearing,speed,numSatellites");
   }
   
   @Override
   public void printCsvData(StringBuilder sb) {
     sb.append(latitude);
     sb.append(',');
     sb.append(longitude);
     sb.append(',');
     sb.append(accuracy);
     sb.append(',');
     sb.append(altitude);
     sb.append(',');
     sb.append(bearing);
     sb.append(',');
     sb.append(speed);
     sb.append(',');
     sb.append(numSatellites);
   }
 }
