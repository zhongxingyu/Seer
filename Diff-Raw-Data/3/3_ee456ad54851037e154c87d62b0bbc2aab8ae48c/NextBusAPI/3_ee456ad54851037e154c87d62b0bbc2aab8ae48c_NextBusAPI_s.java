 /**
  * Interface for NextBus live XML feed API.
  * http://www.actransit.org/rider-info/nextbus-xml-data/
  * Author: Eugene Ma (github.com/edma2)
  */
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.util.HashMap;
 
 public class NextBusAPI {
     private final String URL = "http://webservices.nextbus.com/service/publicXMLFeed";
 
     public String getPredictions(int stopId) throws IOException {
         HashMap<String, String> params = new HashMap<String, String>();
         params.put("command", "predictions");
         params.put("a", "actransit");
         params.put("stopId", Integer.toString(stopId));
         String url = urlFromParams(params);
         String xml = retrieve(url);
         return xml;
     }
 
     /**
      * Returns a URL string with appended query parameters.
      */
     private String urlFromParams(HashMap<String, String> params) {
         String url = URL + "?";
         for (String key : params.keySet()) {
             url = url + key + "=" + params.get(key) + "&";
         }
         return url;
     }
 
     /**
      * Returns the resource located at a the given URL with query string
      * parameters. The data received is assumed to be encoded as UTF-8 by
     * default, and is returned as a String. Returns null on 404.
      */
     private String retrieve(String url) throws IOException {
         HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
         conn.setRequestMethod("GET");
         conn.setDoOutput(true);
         conn.connect();
         if (!conn.getResponseMessage().equals("OK"))
             return null;
         BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF8"));
         String response = "";
         while (reader.ready())
             response += (char)reader.read();
         return response;
     }
 
     public static void main(String[] args) {
         NextBusAPI api = new NextBusAPI();
         try {
             String xml = api.getPredictions(58558);
             System.out.println(xml);
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 }
