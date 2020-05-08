 /**
  * Bus stop state.
  * Knows prediction information for each incoming route in either direction.
  */
 package ma.eugene.nextbusapi;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 
 import org.xml.sax.SAXException;
 
 public class BusStop {
     private class Route {
         private String title;
         private String direction;
 
         public Route(String title, String direction) {
             this.title = title;
             this.direction = direction;
         }
     }
    private HashMap<Route, List<Integer>> predictions = new HashMap<Route,
            List<Integer>>();
 
     public BusStop(String agency, int stopId, int lat, int lon)
                           throws IOException, org.xml.sax.SAXException {
         API api = new API(agency);
         for (PredictionsInfo p : api.getPredictions(stopId))
             predictions.put(new Route(p.title, p.direction), p.times);
     }
 }
