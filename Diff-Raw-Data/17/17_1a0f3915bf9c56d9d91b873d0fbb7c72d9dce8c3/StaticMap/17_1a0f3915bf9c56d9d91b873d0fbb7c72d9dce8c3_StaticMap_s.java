 package web.utils;
 
 import com.javadocmd.simplelatlng.LatLng;
 import java.util.List;
 import web.model.Property;
 import web.model.SearchResult;
 
 /**
  * @author Bernard <bernard.debecker@gmail.com>, R. FONCIER <ro.foncier@gmail.com>
  */
 public class StaticMap {
 
     private final static String BASE_URL = "http://maps.googleapis.com/maps/api/staticmap?";
     private final static String ZOOM = "zoom=13";
     private final static String SIZE = "size=640x400";
     private final static String SCALE = "scale=1";
     private final static String KEY = "key=AIzaSyCIE1oJyR-XDU30PRTTMhadZtRVI1Spf7I";
     private final static String MARKER = "markers=";
     private final static String MARKER_SEPARATOR = "%7C";
     private final static String COLOR_RED = "color:red";
     private final static String LABEL = "label:";
     private final static String SENSOR = "sensor=false";
     private final static String SEPARATOR = "&";
 
     public StaticMap() {
     }
 
     public static String buildMapURL(Object data, String preferedSize) {
         StringBuilder url = new StringBuilder();
         url.append(BASE_URL);
         url.append(SEPARATOR);
         url.append((preferedSize != null) ? "size=" + preferedSize : SIZE);
         url.append(SEPARATOR);
         url.append(SCALE);
         url.append(SEPARATOR);
         url.append(KEY);
         url.append(SEPARATOR);
         // If we send only one property, send only lat/lng, otherwise send a list of SearchResult
         // or a list of Property.
        url.append((data instanceof LatLng) ? addMarker(data) : addMarkers((List<Object>) data));
         url.append(SEPARATOR);
         url.append(SENSOR);
         return url.toString();
     }
     
    private static String addMarker(Object data) {
        LatLng coord = (LatLng) data;
         StringBuilder string = new StringBuilder();
        string.append(MARKER).append(coord.getLatitude()).append(",").append(coord.getLongitude());
         return string.toString();
     }
     
     private static String addMarkers(List<Object> data) {
         StringBuilder string = new StringBuilder();
         for (int i = 0; i < data.size(); i++) {
             if (i > 0) {
                 string.append(SEPARATOR);
             }
             string.append(MARKER);
             string.append(COLOR_RED);
             string.append(MARKER_SEPARATOR);
             string.append(LABEL);
             string.append(i + 1);
             string.append(MARKER_SEPARATOR);
             if (data.get(i) instanceof Property) {
                 LatLng coord = ((Property) data.get(i)).getCoordinates();
                 string.append(coord.getLatitude()).append(",").append(coord.getLongitude());
             } else {
                 // Only available case : List of SearchResult
                 SearchResult s = (SearchResult) data.get(i);
                 string.append(s.getLatitude()).append(",").append(s.getLongitude());
             }
         }
         return string.toString();
     }
 }
