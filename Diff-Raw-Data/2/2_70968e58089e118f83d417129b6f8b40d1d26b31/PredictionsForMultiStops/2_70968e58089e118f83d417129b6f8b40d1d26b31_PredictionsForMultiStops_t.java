 package ma.eugene.nextbus;
 
 import java.util.List;
 import java.util.LinkedList;
 
 public class PredictionsForMultiStops extends Predictions {
     private List<Request> requests = new LinkedList<Request>();
 
     private class Request {
         Route route;
         Direction dir;
         Stop stop;
 
         Request(Route route, Direction dir, Stop stop) {
             this.route = route;
             this.dir = dir;
             this.stop = stop;
         }
     }
 
     public PredictionsForMultiStops(Agency agency) {
        super(agency, ""); // dummy stopId
     }
 
     public void addRequest(Route route, Direction dir, Stop stop) {
         Request r = new Request(route, dir, stop);
         requests.add(r);
     }
 
     @Override
     protected String getURL() {
         StringBuilder sb = new StringBuilder(baseURL());
         sb.append("?command=predictionsForMultiStops");
         sb.append("&a=" + getAgency().tag);
         for (Request r : requests) {
             sb.append("&stops=");
             sb.append(r.route.tag + "|");
             sb.append(r.dir.tag + "|");
             sb.append(r.stop.tag);
         }
         return sb.toString();
     }
 }
