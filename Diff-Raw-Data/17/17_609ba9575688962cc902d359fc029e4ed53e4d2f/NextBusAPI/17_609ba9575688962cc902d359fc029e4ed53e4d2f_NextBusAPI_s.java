 /**
  * Interface for NextBus live XML feed NextBusAPI.
  * http://www.actransit.org/rider-info/nextbus-xml-data/
  * Author: Eugene Ma (github.com/edma2)
  */
 package ma.eugene.nextbusapi;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.io.Reader;
 import java.util.HashSet;
 import java.util.List;
 
 import org.xml.sax.XMLReader;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.XMLReaderFactory;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.InputSource;
 
 public class NextBusAPI {
     private final String URL = "http://webservices.nextbus.com/service/publicXMLFeed";
     private String agency;
 
     public NextBusAPI(String agency) {
         this.agency = agency;
     }
 
     /**
     * Returns a List of Routes representing routes which stop at the stop
      * specified by stopId.
      *
      * @param stopId    the stopId used by the agency to identify the stop
     * @return          the List of Routes
      */
    public List<Route> getPredictions(int stopId)
                         throws IOException, SAXException {
         StringBuilder sb = new StringBuilder(URL + "?");
         sb.append("command=predictions");
         sb.append("&a=" + agency);
         sb.append("&stopId=" + stopId);
         PredictionsParser parser = new PredictionsParser();
         parseXml(sb.toString(), parser);
         return parser.routes;
     }
 
     /**
     * Returns a List of Stops which the specified specified route will stop
      * at.
      *
      * @param routeTitle    the name of the route
     * @return              the List of Stops
      */
    public List<Stop> getRouteConfig(String routeTitle)
                         throws IOException, SAXException {
         StringBuilder sb = new StringBuilder(URL + "?");
         sb.append("command=routeConfig");
         sb.append("&a=" + agency);
         sb.append("&r=" + routeTitle);
         RouteConfigParser parser = new RouteConfigParser();
         parseXml(sb.toString(), parser);
         return parser.stops;
     }
 
     /**
      * Returns a List of names of all routes in use by this agency.
      *
      * @return  all route names
      */
     public List<String> getRouteList()
                         throws IOException, SAXException {
         StringBuilder sb = new StringBuilder(URL + "?");
         sb.append("command=routeList");
         sb.append("&a=" + agency);
         RouteListParser parser = new RouteListParser();
         parseXml(sb.toString(), parser);
         return parser.tags;
     }
 
     private void parseXml(String url, DefaultHandler parser)
                         throws SAXException, IOException {
         XMLReader xr = XMLReaderFactory.createXMLReader();
         xr.setContentHandler(parser);
         xr.parse(new InputSource(retrieve(url)));
     }
 
     /**
      * Returns the resource located at a the given URL with query string
      * parameters. The data received is assumed to be encoded as UTF-8 by
      * default, and a Reader pointing to it is returned.
      */
     private Reader retrieve(String url) throws IOException {
         HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
         conn.setRequestMethod("GET");
         conn.setDoOutput(true);
         conn.connect();
         return new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF8"));
     }
 
     public static void main(String[] args) {
         HashSet<Integer> stopIds = new HashSet<Integer>();
         NextBusAPI api = new NextBusAPI("actransit");
 
         try {
             for (String route : api.getRouteList())
                 System.out.println(route);
         } catch (IOException ex) {
             ex.printStackTrace();
         } catch (SAXException ex1) {
             ex1.printStackTrace();
         }
     }
 }
