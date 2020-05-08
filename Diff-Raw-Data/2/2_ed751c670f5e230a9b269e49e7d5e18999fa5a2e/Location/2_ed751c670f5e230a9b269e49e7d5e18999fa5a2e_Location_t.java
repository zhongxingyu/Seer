 package models;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jurrestender
  * Date: 10/22/12
  * Time: 3:36 PM
  */
 public class Location {
     private double longitude;
     private double latitude;
     private String address;
 
     public Location(double longitude, double latitude, String address) {
         this.longitude = longitude;
         this.latitude = latitude;
         this.address = address;
     }
 
     public Location(double longitude, double latitude) {
         this.longitude = longitude;
         this.latitude = latitude;
     }
 
     public double getLongitude() {
         return longitude;
     }
 
     public void setLongitude(double longitude) {
         this.longitude = longitude;
     }
 
     public double getLatitude() {
         return latitude;
     }
 
     public void setLatitude(double latitude) {
         this.latitude = latitude;
     }
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     public String getLongLatString() {
        return latitude + "," + longitude;
     }
 
     @Override
     public boolean equals(Object object) {
         if (object instanceof Location) {
             if (((Location) object).getLongitude() == this.getLongitude() && ((Location) object).getLatitude() == this.getLatitude()) {
                 return true;
             }
         }
         return false;
     }
 }
