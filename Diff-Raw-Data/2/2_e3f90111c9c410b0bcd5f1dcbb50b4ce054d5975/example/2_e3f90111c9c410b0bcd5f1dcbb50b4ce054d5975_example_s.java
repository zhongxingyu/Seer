 public class example {
     private static final double RADIUS = 6371;
 
     private static double bearingTo(double lat1, double lon1, double lat2, double lon2) {
         double dLon = Math.toRadians(lon2 - lon1);
 
         lat1 = Math.toRadians(lat1);
         lat2 = Math.toRadians(lat2);
 
         double y = Math.sin(dLon) * Math.cos(lat2);
         double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
 
         double bearing = Math.toDegrees(Math.atan2(y, x));
 
         if (bearing < 0) {
             bearing += 360;
         }
 
         return bearing;
     }
 
     private static double bearingFrom(double lat1, double lon1, double lat2, double lon2) {
         double bearing = bearingTo(lat1, lon1, lat2, lon2);
 
         bearing += 180;
         if (bearing > 360) {
             bearing -= 360;
         }
 
         return bearing;
     }
 
     private static double haversine(double lat1, double lon1, double lat2, double lon2) {
         double dLat = Math.toRadians(lat2 - lat1);
         double dLon = Math.toRadians(lon2 - lon1);
 
         double a, c, distance;
 
         lat1 = Math.toRadians(lat1);
         lat2 = Math.toRadians(lat2);
 
         a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
         c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
         distance = RADIUS * c;
 
         return distance;
     }
 
     public static void main(String[] args) {
         if (args.length != 4) {
            System.out.println("Usage: haverMath.sine <latitude1> <longitude1> <latitute2> <longitude2>");
             return;
         }
 
         double lat1 = Double.parseDouble(args[0]);
         double lon1 = Double.parseDouble(args[1]);
         double lat2 = Double.parseDouble(args[2]);
         double lon2 = Double.parseDouble(args[3]);
 
         System.out.println("Distance: " + haversine(lat1, lon1, lat2, lon2) + " km");
         System.out.println("Initial bearing: " + bearingTo(lat1, lon1, lat2, lon2) + "°");
         System.out.println("Final bearing: " + bearingFrom(lat2, lon2, lat1, lon1) + "°");
     }
 }
