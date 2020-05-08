 package map;
 
 import de.lmu.ifi.dbs.utilities.Math2;
 import de.locked.signalcoverage.DataDAO;
 import java.net.UnknownHostException;
 import java.util.Collection;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 @Path("/features")
 public class Features {
 
     private static final Logger LOG = Logger.getLogger(Features.class.getName());
     private static final int MAX_ZOOM_LEVEL = 18;
 
     // http://wiki.openstreetmap.org/wiki/DE:Zoom_levels
     @GET
     @Produces({MediaType.TEXT_PLAIN})
     @Path("/map")
     public String getFeatures(
             @QueryParam("z") int z,
             @QueryParam("l") double l,
             @QueryParam("t") double t,
             @QueryParam("r") double r,
             @QueryParam("b") double b) {
         if (l == 0 || t == 0 || r == 0 | b == 0) {
             return "";
         }
         z = (int) Math2.bind(0, z, MAX_ZOOM_LEVEL);
        StringBuilder result = new StringBuilder();
        result.append("lat	lon	icon	iconSize	iconOffset	title	description	popupSize\n");
         // 47.7591000	11.5611000	icons/r.png	16,16	8,8	Schlecht	foo bar foobar foo	200,80
         final String template = "%f	%f	mapicons/%s.png	5,5	0,-5	Signal	%s	200,80\n";
         try {
             Collection<Cluster> inBox = new DataDAO().getInBox(z, l, t, r, b);
             LOG.info("clusters in zoomlevel " + z + ": " + inBox.size());
             for (Cluster cluster : inBox) {
                 String entries = "";
                 int minSignal = Integer.MAX_VALUE;
                 for (Cluster.Measure measure : cluster.measure) {
                     minSignal = Math.min(minSignal, measure.getAvg());
                    if (!entries.isEmpty()) {
                        entries += "<br>";
                    }
                    entries += measure.carrier + ": " + measure.getAvg();
                 }
                result.append(String.format(Locale.US, template, cluster.lat, cluster.lon, getIcon(minSignal), entries));
             }
         } catch (UnknownHostException ex) {
            LOG.log(Level.SEVERE, null, ex);
         }
        return result.toString();
     }
 
     private String getIcon(double signal) {
         String icon = "r";
         if (signal > 4) {
             icon = "y";
         }
         if (signal > 20) {
             icon = "g";
         }
         return icon;
     }
 }
