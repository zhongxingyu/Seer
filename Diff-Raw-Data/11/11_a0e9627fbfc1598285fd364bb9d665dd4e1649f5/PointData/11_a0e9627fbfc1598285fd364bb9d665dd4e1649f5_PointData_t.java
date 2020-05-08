 package KML;
 
 /**
  *
  * @author al
  */
 public class PointData {
     double latitude = 0.0;
     double longitude = 0.0;
 
     public PointData(String theCoords) {
         String[] splitCoords = theCoords.split(",");
         
         if(splitCoords.length > 1){
            longitude = Double.parseDouble(splitCoords[0]);
            latitude = Double.parseDouble(splitCoords[1]);
         }
     }
 
     public double getLatitude() {
         return latitude;
     }
 
     public double getLongitude() {
         return longitude;
     }
 }
