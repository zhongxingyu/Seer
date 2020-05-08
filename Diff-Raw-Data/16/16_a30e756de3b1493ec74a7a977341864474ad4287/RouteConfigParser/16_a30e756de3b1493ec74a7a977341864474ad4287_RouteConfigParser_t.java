 package ma.eugene.nextbus;
 
 import java.util.LinkedList;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class RouteConfigParser extends DefaultHandler {
     public LinkedList<RouteConfigInfo> stops = new LinkedList<RouteConfigInfo>();
 
     public void startElement(java.lang.String uri, java.lang.String localName,
             java.lang.String qName, Attributes attributes) throws SAXException {
         String tag = localName;
         if (tag.equals("stop")) {
             String stopId = attributes.getValue("stopId");
             if (stopId == null)
                 return;
             String title = attributes.getValue("title");
             String lon = attributes.getValue("lon");
             String lat = attributes.getValue("lat");
            stops.add(new RouteConfigInfo(title, new Integer(stopId),
                        new Double(lat), new Double(lon)));
         }
     }
 }
