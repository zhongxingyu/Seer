 package ma.eugene.nextbus;
 
 /*
  * Information about a stop.
  */
 public class RouteConfigInfo {
     public String title;
     public int stopId;
     public double longitude;
     public double latitude;
 
    public RouteConfigInfo(String title, int stopId, double longitude,
                                double latitude) {
         this.title = title;
         this.stopId = stopId;
        this.longitude = longitude;
         this.latitude = latitude;
     }
 }
