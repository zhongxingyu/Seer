 package uk.hux;
 
 public class Location extends UserStatus
 {
   public final double latitude;
   public final double longitude;
   public final double accuracy; //metres on android
   
   public Location(long user, long time, double lat, double lon, double accuracy)
   {
    this.userID = user;
    this.timestamp = time;
     this.latitude = lat;
     this.longitude = lon;
     this.accuracy = accuracy;
   }
   
   public String toString()
   {
     return userID+","+timestamp+","+latitude+","+longitude+","+accuracy+"m";
   }
 }
