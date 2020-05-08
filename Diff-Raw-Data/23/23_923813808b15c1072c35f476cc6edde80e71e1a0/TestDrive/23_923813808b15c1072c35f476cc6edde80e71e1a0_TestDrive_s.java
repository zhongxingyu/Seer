 package ma.eugene.nextbus;
 
 import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
 import org.xml.sax.SAXException;
 
 public class TestDrive extends NextBus {
     private double latitude = 37.873464;
     private double longitude = -122.271481;
 
     public TestDrive(String agency) throws IOException, SAXException {
         super(agency);
     }
 
     public TestDrive(String agency, String s) {
         super(agency, s);
     }
 
     @Override
     protected float stopDistance(BusStop bs) {
         return distance(bs.getLatitude(), bs.getLongitude(), latitude,
                 longitude);
     }
 
     /**
      * Calculates the great-circle distance between two points
      * using the Haversine forumla.
      *
      * @return  distance in meters
      */
     private float distance(double lat1, double long1, double lat2,
             double long2) {
         int earthRadius = 6372797;
         double dlat = Math.toRadians(lat2-lat1);
         double dlong = Math.toRadians(long2-long1);
         double a = Math.sin(dlat/2) * Math.sin(dlat/2) +
             Math.cos(Math.toRadians(lat1)) *
             Math.cos(Math.toRadians(lat2)) * Math.sin(dlong/2) *
             Math.sin(dlong/2);
         double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
         double d = earthRadius * c;
         return (float)d;
     }
 
     public static void main(String[] args) {
         try {
            FileInputStream fis = new FileInputStream("stops");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = br.readLine()) != null)
                sb.append(s + "\n");
            if (sb.charAt(sb.length()-1) == '\n')
                sb.deleteCharAt(sb.length()-1);

            TestDrive td = new TestDrive("actransit", sb.toString());
            System.out.println(td);
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 }
