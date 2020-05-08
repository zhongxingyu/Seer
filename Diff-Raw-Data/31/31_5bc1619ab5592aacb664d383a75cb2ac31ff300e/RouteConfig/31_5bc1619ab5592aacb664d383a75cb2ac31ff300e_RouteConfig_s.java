 import java.io.IOException;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Stack;
 import java.util.EmptyStackException;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class RouteConfig extends Command {
     private String title; 
     private List<Stop> stops = new LinkedList<Stop>();
     private Map<String, List<Stop>> paths = new HashMap<String, List<Stop>>();
 
     private class Stop {
         int stopId;
         String title;
         double latitude;
         double longitude;
 
         public Stop(int stopId, String title, double latitude, double longitude) {
             this.stopId = stopId;
             this.title = title;
             this.latitude = latitude;
             this.longitude = longitude;
         }
     }
 
     public RouteConfig(String agency, String title) {
         super(agency);
         this.title = title;
     }
 
     @Override
     protected String getURL() {
         StringBuilder sb = new StringBuilder(super.getURL());
         sb.append("?command=routeConfig");
         sb.append("&a=" + getAgency());
         sb.append("&r=" + title);
         return sb.toString();
     }
 
     @Override
     protected DefaultHandler getHandler() {
         return new DefaultHandler() {
             Map<String, Stop> stops = new HashMap<String, Stop>();
            Stack<String> parentTags = new Stack<String>();
             String prevDirection = "";
 
             public void startElement(
                     java.lang.String uri,
                     java.lang.String localName,
                     java.lang.String qName,
                     Attributes attributes) throws SAXException {
                 String tag = qName; // TODO: qName or localName?
                String parentTag;
                try {
                    parentTag = parentTags.peek();
                } catch (EmptyStackException ex) {
                    parentTag = "";
                }
                 if (tag.equals("body")) {
                     // <body>
                     stops.clear();
                     paths.clear();
                 } else if (parentTag.equals("route") && tag.equals("stop")) {
                     // <body>
                     //   <route>
                     //     <stop>
                     String stopId = attributes.getValue("stopId");
                     Stop stop = new
                         Stop(stopId == null ? 0 : Integer.parseInt(stopId),
                                 attributes.getValue("title"),
                                 Double.parseDouble(attributes.getValue("lon")),
                                 Double.parseDouble(attributes.getValue("lat")));
                     String stopTag = attributes.getValue("tag");
                     RouteConfig.this.stops.add(stop);
                     stops.put(stopTag, stop);
                 } else if (parentTag.equals("route") && tag.equals("direction")) {
                     // <body>
                     //   <route>
                     //     <direction>
                     String direction = attributes.getValue("title");
                     paths.put(direction, new LinkedList<Stop>());
                     prevDirection = direction;
                 } else if (parentTag.equals("direction") && tag.equals("stop")) {
                     // <body>
                     //   <route>
                     //     <direction>
                     //       <stop>
                     String stopTag = attributes.getValue("tag");
                     Stop stop = stops.get(stopTag);
                     paths.get(prevDirection).add(stop);
                 }
                parentTags.push(tag);
             }
 
             public void endElement(
                     java.lang.String uri,
                     java.lang.String localName,
                     java.lang.String qName) throws SAXException {
                parentTags.pop();
             }
         };
     }
 
     public static void main(String[] args) {
         RouteConfig rc = new RouteConfig("actransit", "B");
         rc.execute();
         for (String direction : rc.paths.keySet()) {
             System.out.println(direction);
             System.out.println("=============");
             for (Stop s : rc.paths.get(direction))
                 System.out.println(s.title);
         }
     }
 }
