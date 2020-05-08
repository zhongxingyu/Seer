 package ma.eugene.nextbusapi;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.xml.sax.SAXException;
 
 public class NextBus {
     private String agency;
 
     /* For mapping stopIds to unique stops */
     private HashMap<Integer, BusStop> stops = new HashMap<Integer, BusStop>();
 
     public NextBus(String agency) {
         this.agency = agency;
     }
 
     public void collectStops() throws IOException, org.xml.sax.SAXException {
         API api = new API("actransit");
         for (String route : api.getRouteList()) {
             for (RouteConfigInfo i : api.getRouteConfig(route)) {
                 BusStop stop = new BusStop(agency, i.stopId, i.latitude,
                         i.longitude);
                 stops.put(i.stopId, stop);
             }
         }
     }
 
     /**
      * Calculates the great-circle distance between two points using the
      * Haversine forumla.
      *
      * @return  distance in meters
      */
    private double distance(double lat1, double long1, double lat2,
            double long2) {
         int earthRadius = 6372797;
         double dlat = Math.toRadians(lat2-lat1);
         double dlong = Math.toRadians(long2-long1);
         double a = Math.sin(dlat/2) * Math.sin(dlat/2) +
             Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
             Math.sin(dlong/2) * Math.sin(dlong/2);
         double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
         double d = earthRadius * c;
         return d;
     }
 }
