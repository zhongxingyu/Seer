 package dk.itu.grp11.data;
 
 import java.awt.geom.Point2D;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Locale;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import dk.itu.grp11.enums.MapBound;
 import dk.itu.grp11.enums.RoadType;
 import dk.itu.grp11.enums.TrafficDirection;
 import dk.itu.grp11.files.ResourceGetter;
 import dk.itu.grp11.route.Network;
 import dk.itu.grp11.util.LatLonToUTM;
 import dk.itu.grp11.util.QuadTree;
 
 
 /**
  * Parses information about a road network.
  * 
  * @author Group 11
  * 
  */
 public class Parser {
   private static Parser ps = null;
   
   private static InputStream pointFile;
   private static InputStream roadFile;
   private static InputStream postalCodesFile;
   private static InputStream coastFiles[];
   
   private EnumMap<MapBound, Double> mapBounds;
   private HashMap<Integer, Point> points;
   private HashMap<RoadType, QuadTree<Double, Road>> roads = new HashMap<RoadType, QuadTree<Double, Road>>();
   private HashMap<Integer, String> postalCodes;
   private SortedMap<String, Road> roadNames;
   private HashSet<LinkedList<Integer>> coastline = new HashSet<LinkedList<Integer>>(); 
   private Network graph;
   private static int pointsOffset = 900000; // Coast line point ID's are added this offset to not interfere with id's from Krak data.
   private int mapBoundOffset = 50000; // Offset to make sure the map is centered.
 
   /**
    * The parser class is responsible for parsing all our data. While parsing points it 
    * keeps track of the minimum and maximum coordinates for drawing our viewbox.
    * It parses points, roads, coastline and postalcodes for use in map.
    * 
    * The class is a singleton, as only one instance should ever be created of it.
    * 
    * @param pointFile the file containing points
    * @param roadFile the file containing roads
    * @param postalCodesFile the file containing postal code information
    * @param coastFiles the file(s) containing coast line data
    */
   private Parser(InputStream pointFile, InputStream roadFile, InputStream postalCodesFile, InputStream... coastFiles) {
     Parser.pointFile = pointFile;
     Parser.roadFile = roadFile;
     Parser.postalCodesFile = postalCodesFile;
     Parser.coastFiles = coastFiles;
     
     mapBounds = new EnumMap<MapBound, Double>(MapBound.class);
     mapBounds.put(MapBound.MINX, 1000000.0);
     mapBounds.put(MapBound.MAXX, 0.0);
     mapBounds.put(MapBound.MINY, 100000000.0);
     mapBounds.put(MapBound.MAXY, 0.0);
   }
 
   public static Parser getParser() {
     if (ps == null) {
       InputStream points = ResourceGetter.class.getResourceAsStream("kdv_node_unload.txt");
       InputStream roads = ResourceGetter.class.getResourceAsStream("kdv_unload.txt");
       InputStream zip = ResourceGetter.class.getResourceAsStream("postNR.csv");
       InputStream coastFileDenmark = ResourceGetter.class.getResourceAsStream("coastLine.osm");
       InputStream coastFileSweden = ResourceGetter.class.getResourceAsStream("coastLineSweden.osm");
       InputStream coastFileGermany = ResourceGetter.class.getResourceAsStream("coastLineGermany.osm");
       ps = new Parser(points, roads, zip, coastFileDenmark, coastFileSweden, coastFileGermany);
       ps.parsePoints();
       ps.parsePostalCodes();
       ps.parseRoads(ps.points());
       ps.parseCoastline();
     }
     return ps;
   }
 
   /**
    * (For test purposes only)
    * A parser with custom file input to be used for testing.
    * If null is given as any parameter argument, the default file will be parsed.
    * 
    * @param points point file
    * @param roads road file
    * @param zip postal code file
    * @param coast coast files
    * @return a parser instantiated with the files given
    */
   public static Parser getTestParser(InputStream points, InputStream roads, InputStream zip, InputStream... coastFiles) {
     if(points == null) points = ResourceGetter.class.getResourceAsStream("null.txt");
     if(roads == null) roads = ResourceGetter.class.getResourceAsStream("null.txt");
    if(zip == null) zip = ResourceGetter.class.getResourceAsStream("postNR.csv");
     if(coastFiles == null) {
       coastFiles = new InputStream[3];
       coastFiles[0] = ResourceGetter.class.getResourceAsStream("null.txt");
       coastFiles[1] = ResourceGetter.class.getResourceAsStream("null.txt");
       coastFiles[2] = ResourceGetter.class.getResourceAsStream("null.txt");
       //coastFiles[3] = new File(ResourceGetter.class.getResource("coastTest.osm").getFile()); //Test file, not needed
     }
     
     Parser ps_tmp = new Parser(points, roads, zip, coastFiles);
     ps_tmp.parsePoints();
     ps_tmp.parsePostalCodes();
     ps_tmp.parseRoads(ps_tmp.points());
     ps_tmp.parseCoastline();
     return ps_tmp;
   }
 
   /**
    * Parses all points in the network and puts it in a HashMap with point-ID as key,
    * and the point as value, as well as establishing the highest and lowest x and y coordinates.
    * It is then stored in a HashMap<Integer, Point>
    */
   private void parsePoints() {
     System.out.println("- Parsing points");
     points = new HashMap<Integer, Point>();
     try(BufferedReader input = new BufferedReader(new InputStreamReader(pointFile))) {  
       String line = null;
       /*
        * readLine is a bit quirky : it returns the content of a line MINUS the
        * newline. it returns null only for the END of the stream. it returns
        * an empty String if two newlines appear in a row.
        */
       input.readLine(); //Skip first line
       while ((line = input.readLine()) != null) {
         Point p = createPoint(line);
         points.put(p.getID(), p);
 
         // Finding maximum and minimum x and y coordinates
         updateMapBounds(p.getX(), p.getY());
       }
      
     	  
       }catch (IOException ex) {
       ex.printStackTrace();
     }
   }
   
   /**
    * Parses the coastline data using a SaxParser. This parser runs through all elements and
    * calls the methods startElement and endElement for start and end elements respectively. The entire handler
    * is stored in a variable called handler, that is passed to the SaxParsers parse method
    */
   private void parseCoastline() {
     try {
       System.out.println("- Parsing coastline points and lines");
       SAXParserFactory factory = SAXParserFactory.newInstance();
       final Double maxX = mapBounds.get(MapBound.MAXX);
       final Double maxY = mapBounds.get(MapBound.MAXY);
       final Double minX = mapBounds.get(MapBound.MINX);
       final Double minY = mapBounds.get(MapBound.MINY);
       SAXParser saxParser = factory.newSAXParser();
       DefaultHandler handler = new DefaultHandler() {
         boolean inWay = false;
         LinkedList<Integer> currWay;
         
         public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
           if (qName.equalsIgnoreCase("NODE")) { //Element node
             int id = -1;
             double lat = -1, lon = -1;
             for (int i=0; i<attributes.getLength(); i++) { //Runs through all attributes of the element
               if (attributes.getQName(i).equalsIgnoreCase("LAT")) lat = Double.parseDouble(attributes.getValue(i));
               if (attributes.getQName(i).equalsIgnoreCase("LON")) lon = Double.parseDouble(attributes.getValue(i));
               if (attributes.getQName(i).equalsIgnoreCase("ID")) id = Integer.parseInt(attributes.getValue(i));
             }
             if (id > 0 && lat > 0 && lon > 0) {
 	            Point2D coords = LatLonToUTM.convert(lat, lon);
             	if (coords.getX() < maxX && coords.getX() > minX && coords.getY() < maxY && coords.getY() > minY) { //Is the point in our map
             	  Point p = new Point(id, coords.getX(), coords.getY());
   	              points.put(p.getID()+pointsOffset, p);
             	}
             }
           } else if (qName.equalsIgnoreCase("WAY")) { //Element way
             inWay = true;
             currWay = new LinkedList<Integer>();
           } else if (qName.equalsIgnoreCase("ND") && inWay) { //Element nd, but only if we are in a way element.
             int currID = -1;
             for (int i=0; i<attributes.getLength(); i++) {
               if (attributes.getQName(i).equalsIgnoreCase("REF")) {
                 currID = Integer.parseInt(attributes.getValue(i));
               }
             }
             if (currID > 0) {
             	if (points.containsKey(currID+pointsOffset)) {
             		currWay.add(currID);
             	}
             }
           }
         }
         
         public void endElement(String uri, String localName, String qName) throws SAXException {
           if (qName.equalsIgnoreCase("WAY")) { //Now we are not in way element anymore
             inWay = false;
             coastline.add(currWay);
           }
         }
       };
       for(int i = 0; i < coastFiles.length; i++) {
         try {
           saxParser.parse(coastFiles[i], handler);
         } catch (IllegalArgumentException e) {
           continue; //If file does not contain start XML tag, ignore it.
         }
       }
     } catch (SAXException|ParserConfigurationException|IOException e) {
       System.out.println("Could not parse Coastline points: "+e);
     }
   }
 
   /**
    * Parses all roads in the network and puts it in a DimensionalTree sorted by
    * the x and y coordinate of the road and road type.
    */
   private void parseRoads(HashMap<Integer, Point> points) {
     System.out.println("- Parsing roads");
     HashSet<Road> roadsForGraph = new HashSet<>(); //Temporary set for building the Graph
     roadNames = new TreeMap<>();
     try(BufferedReader input = new BufferedReader(new InputStreamReader(roadFile, "ISO8859_1"))) {
       String line = null;
       /*
        * readLine is a bit quirky : it returns the content of a line MINUS the
        * newline. it returns null only for the END of the stream. it returns
        * an empty String if two newlines appear in a row.
        */
       input.readLine();
       while ((line = input.readLine()) != null) {
         Road r = createRoad(line);
         Double xS = points.get(r.getFrom()).getX();
         Double yS = points.get(r.getFrom()).getY();
         Double xE = points.get(r.getTo()).getX();
         Double yE = points.get(r.getTo()).getY();
         if (!roads.containsKey(r.getType())) roads.put(r.getType(), new QuadTree<Double, Road>());
         roads.get(r.getType()).insert(xS, yS, r);
         roads.get(r.getType()).insert(xE, yE, r);
         roadsForGraph.add(r);
       }
       System.out.println("- Creating network graph");
       graph = new Network(points.size(), roadsForGraph);
     } catch (IOException ex) {
       ex.printStackTrace();
     }
   }
 
   /**
    * Splits a line from the kdv_unload.txt document and then
    * creates a Road object from the information in the string.
    * 
    * @param input
    *          A line from the kdv_unload.txt document.
    * @return A Road object containing the information from the line.
    */
   private Road createRoad(String input) {
     String[] inputSplit = input.split(",");
     Road r = new Road(
         Integer.parseInt(inputSplit[0]),                                    // 0 = id of from point
         Integer.parseInt(inputSplit[1]),                                    // 1 = id of to point
         inputSplit[6].substring(1, inputSplit[6].length()-1),               // 6 = name
         Integer.parseInt(inputSplit[17]),                                   // 17 = from zip code
         Integer.parseInt(inputSplit[18]),                                   // 18 = to zip code
         RoadType.getById(Integer.parseInt(inputSplit[5])),                  // 5 = road type
         TrafficDirection.getDirectionById(inputSplit[27].replace("'", "")), // 27 = traffic direction
         Double.parseDouble(inputSplit[2]),                                  // 2 = length
         Double.parseDouble(inputSplit[26]));                                // 26 = time
     
     //Adding the road to the road neame collection
     if(r.getFromZip() != 0 && r.getToZip() != 0) {
       String from = ""+r.getFromZip();
       String to = ""+r.getToZip();
       if(postalCodes.get(r.getFromZip()) != null) //If we have the name for the postal code
         from = postalCodes.get(r.getFromZip());   // - then replace it
       if(postalCodes.get(r.getToZip()) != null)   //If we have the name for the postal code
         to = postalCodes.get(r.getToZip());       // - then replace it
       
       String fromString = r.getName().toLowerCase(new Locale("ISO8859_1")) + ", " + from.toLowerCase(new Locale("ISO8859_1"));
       String toString = r.getName().toLowerCase(new Locale("ISO8859_1")) + ", " + to.toLowerCase(new Locale("ISO8859_1"));
       
       roadNames.put(fromString, r);
       roadNames.put(toString, r);
     }
     return r;
   }
 
   /**
    * Splits a line from the kdv_node_unload.txt document and then
    * creates a Point object from the information in the string.
    * 
    * @param input
    *          A line from the kdv_node_unload.txt document.
    * @return A Point object containing the information from the line.
    */
   private Point createPoint(String input) {
     String[] inputSplit = input.split(",");
     return new Point(
         Integer.parseInt(inputSplit[2]),    //2 = id of the point
         Double.parseDouble(inputSplit[3]),  //3 = x coordinate
         Double.parseDouble(inputSplit[4])); //4 = y coordinate
   }
 
   /**
    * Parses the postal codes from the .csv file into a HashMap where the postal code is the Key and the city name is the value.
    */
   private void parsePostalCodes(){
     System.out.println("- Parsing postal codes");
     postalCodes = new HashMap<Integer, String>();
     
     try(BufferedReader input = new BufferedReader(new InputStreamReader(postalCodesFile, "ISO8859_1"))) {  
       String line = null;
       /*
        * readLine is a bit quirky : it returns the content of a line MINUS the
        * newline. it returns null only for the END of the stream. it returns
        * an empty String if two newlines appear in a row.
        */
       input.readLine();
       input.readLine(); //Skip first 2 lines
       while ((line = input.readLine()) != null) {
         String[] inputSplit = line.split(";");
         if(Integer.parseInt(inputSplit[5]) == 1) {
           postalCodes.put(Integer.parseInt(inputSplit[0]),  // Postal code
                           inputSplit[1]);                   // City name
         }
       }
     } catch (IOException ex) {
       ex.printStackTrace();
     }
   }
   
   /**
    * This function is called when our points are being parsed, to establish the minimum and maximum values for x and y.
    * @param x the x value to check.
    * @param y the y value to check
    */
   private void updateMapBounds(double x, double y) {
     if (x > mapBounds.get(MapBound.MAXX)-mapBoundOffset)
       mapBounds.put(MapBound.MAXX, x+mapBoundOffset);
     if (x < mapBounds.get(MapBound.MINX)+mapBoundOffset)
       mapBounds.put(MapBound.MINX, x-mapBoundOffset);
     if (y > mapBounds.get(MapBound.MAXY)-mapBoundOffset)
       mapBounds.put(MapBound.MAXY, y+mapBoundOffset);
     if (y < mapBounds.get(MapBound.MINY)+mapBoundOffset)
       mapBounds.put(MapBound.MINY, y-mapBoundOffset);
   }
   
   /**
    * Returns a sub map of the given map matching the prefix.
    * The output will be a in jQuery syntax.
    * 
    * @param prefix the prefix
    * @return the sub map
    */
   public String roadsWithPrefix(String prefix) {
     if(prefix.length() > 0) {
         prefix = prefix.toLowerCase(new Locale("ISO8859_1"));
         char nextChar = (char) (prefix.charAt(prefix.length() - 1) + 1);
         String end = prefix.substring(0, prefix.length()-1) + nextChar;
         return mapToJquery(roadNames.subMap(prefix, end));
     }
     return mapToJquery(roadNames);
   }
   
   /**
    * Creates jQuery syntax from a map. The map will have to contain road names
    * and the Road object itself.
    * 
    * @param roadNames
    *          the map of road names and Road objects
    * @return the jQuery string
    */
   private String mapToJquery(SortedMap<String, Road> roadNames) {
     String jq = "[ ";
     for(Entry<String, Road> e : roadNames.entrySet()) {
       Road r = e.getValue();
       String from = ""+r.getFromZip();
       if(postalCodes.get(r.getFromZip()) != null)
         from = postalCodes.get(r.getFromZip());
       String to = ""+r.getToZip();
       if(postalCodes.get(r.getToZip()) != null)
         to = postalCodes.get(r.getToZip());
       
       jq += ", \"" + e.getValue().getName() + ", " + from + "\"";
       if(r.getFromZip() != r.getToZip()) jq += ", \"" + e.getValue().getName() + ", " +  to + "\"";
     }
     return jq.replaceFirst(",", "") + " ]";
   }
   
   
   /*
    * Below are "getter" functions for all data contained within the parser that the rest of our application uses.
    */
   public static int getPointsOffset() {
     return pointsOffset;
   }
   
   public HashMap<Integer, Point> points() {
     return points;
   }
   
   public HashMap<RoadType, QuadTree<Double, Road>> roads() {
     return roads;
   }
   
   public HashSet<LinkedList<Integer>> coastline() {
     return coastline;
   }
   
   public Network network() {
     return graph;
   }
   
   public HashMap<Integer, String> postalCodes() {
     return postalCodes;
   }
   
   public SortedMap<String, Road> roadNames() {
     return roadNames;
   }
   
   public String zipToCity(int zip) {
     return postalCodes.get(zip);
   }
   
   public double mapBound(MapBound mb) {
     return mapBounds.get(mb);
   }
 
   public int numPoints() {
     return points.size();
   }
 
   public int numRoads() {
     int count = 0;
     for (QuadTree<Double, Road> tree : roads.values()) {
       count += tree.size();
     }
     return count;
   }
 }
