 import java.util.List;
 import java.util.LinkedList;
 
 import org.xml.sax.Attributes;
 
 public class Predictions extends Command {
     protected List<Prediction> predictions;
     private int stopId;
 
     public Predictions(String agency, int stopId) {
         super(agency);
         this.stopId = stopId;
     }
 
     public Prediction[] getPredictions() {
         return predictions.toArray(new Prediction[0]);
     }
 
     @Override
     protected String getURL() {
         StringBuilder sb = new StringBuilder(baseURL());
         sb.append("?command=predictions");
         sb.append("&a=" + getAgency());
         sb.append("&stopId=" + stopId);
         return sb.toString();
     }
 
     @Override
     protected NextBusHandler getHandler() {
         return new NextBusHandler() {
             String routeTitle = "";
             String stopTitle = "";
             Prediction p;
 
             public void handleElement(String tag, Attributes attributes) {
                 if (tag.equals("body")) {
                     predictions = new LinkedList<Prediction>();
                 } else if (tag.equals("predictions")) {
                     routeTitle = attributes.getValue("routeTitle");
                     stopTitle = attributes.getValue("stopTitle");
                 } else if (tag.equals("direction")) {
                     String dirTitle = attributes.getValue("title");
                     p = new Prediction(routeTitle, stopTitle, dirTitle);
                     predictions.add(p);
                 } else if (tag.equals("prediction")) {
                     int seconds =
                         Integer.parseInt(attributes.getValue("seconds"));
                    p.arrivalTimes.add(seconds);
                 }
             }
         };
     }
 }
