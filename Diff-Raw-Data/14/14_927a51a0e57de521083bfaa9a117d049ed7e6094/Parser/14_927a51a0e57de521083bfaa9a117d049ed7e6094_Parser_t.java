 package dk.itu.grp11.data;
 
 import java.awt.geom.Point2D;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
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
 import dk.itu.grp11.route.Network;
 import dk.itu.grp11.util.DimensionalTree;
 import dk.itu.grp11.util.LatLonToUTM;
 
 
 /**
  * Parses information about a road network.
  * 
  * @author Group 11
  * 
  */
 public class Parser {
   private static Parser ps = null;
   
   private static File pointFile;
   private static File roadFile;
   private static File postalCodesFile;
   private static File coastFile;
   private static File coastTest;
   private static File coastFileSweden;
   
   private static EnumMap<MapBound, Double> mapBounds;
   private static HashMap<Integer, Point> points;
   private static DimensionalTree<Double, RoadType, Road> roads;
   private static HashMap<Integer, String> postalCodes;
   private static SortedMap<String, Road> roadNames;
   private static HashSet<LinkedList<Integer>> coastline = new HashSet<LinkedList<Integer>>();
   private static Network graph;
   public static int pointsOffset = 800000;
   public static int mapBoundOffset = 50000;
 
   /**
    * 
    * @param pointFile A java.File object referencing the file containing nodes.
    * @param roadFile A java.File object referencing the file containing connections.
    */
   public Parser(File pointFile, File roadFile, File postalCodesFile, File coastFile, File coastTest, File coastFileSweden) {
     Parser.pointFile = pointFile;
     Parser.roadFile = roadFile;
     Parser.postalCodesFile = postalCodesFile;
     Parser.coastFile = coastFile;
     Parser.coastTest = coastTest;
     Parser.coastFileSweden = coastFileSweden;
     
     mapBounds = new EnumMap<MapBound, Double>(MapBound.class);
     mapBounds.put(MapBound.MINX, 1000000.0);
     mapBounds.put(MapBound.MAXX, 0.0);
     mapBounds.put(MapBound.MINY, 100000000.0);
     mapBounds.put(MapBound.MAXY, 0.0);
   }
 
   public static Parser getParser() {
     if (ps == null) {
       File points = new File("src\\dk\\itu\\grp11\\files\\kdv_node_unload.txt");
       File roads = new File("src\\dk\\itu\\grp11\\files\\kdv_unload.txt");
       File zip = new File("src\\dk\\itu\\grp11\\files\\postNR.csv");
       File coast = new File("src\\dk\\itu\\grp11\\files\\coastLine.osm");
       File coastTest = new File("src\\dk\\itu\\grp11\\files\\coastTest.osm");
       File coastFileSweden = new File("src\\dk\\itu\\grp11\\files\\coastLineSweden.osm");
       ps = new Parser(points, roads, zip, coast, coastTest, coastFileSweden);
       ps.parsePoints();
       ps.parsePostalCodes();
       ps.parseRoads(ps.points());
       ps.parseCoastline();
     }
     return ps;
   }
 
   /**
    * For test purposes only
    * 
    * @param nodeFile
    * @param connectionFile
    * @return
    */
   public static Parser getTestParser(File points, File roads, File zip, File coast, File sweden) {
     if (ps == null) {
       if(points == null) points = new File("src\\dk\\itu\\grp11\\files\\kdv_node_unload.txt");
       if(roads == null) roads = new File("src\\dk\\itu\\grp11\\files\\kdv_unload.txt");
       if(zip == null) zip = new File("src\\dk\\itu\\grp11\\files\\postNR.csv");
       if(coast == null) coast = new File("src\\dk\\itu\\grp11\\files\\coastLine.osm");
       if(sweden == null) sweden = new File("src\\dk\\itu\\grp11\\files\\coastLineSweden.osm");
       
       ps = new Parser(points, roads, zip, coast, coastTest, sweden);
       ps.parsePoints();
       ps.parsePostalCodes();
       ps.parseRoads(ps.points());
       ps.parseCoastline();
       
     }
     return ps;
   }
 
   /**
    * Parses all points in the network and puts it in a HashMap with point-ID as key,
    * and the point as value.
    */
   private void parsePoints() {
     System.out.println("- Parsing points");
     points = new HashMap<Integer, Point>();
     try(BufferedReader input = new BufferedReader(new FileReader(pointFile))) {  
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
           if (qName.equalsIgnoreCase("NODE")) {
             int id = -1;
             double lat = -1, lon = -1;
             for (int i=0; i<attributes.getLength(); i++) {
               if (attributes.getQName(i).equalsIgnoreCase("LAT")) lat = Double.parseDouble(attributes.getValue(i));
               if (attributes.getQName(i).equalsIgnoreCase("LON")) lon = Double.parseDouble(attributes.getValue(i));
               if (attributes.getQName(i).equalsIgnoreCase("ID")) id = Integer.parseInt(attributes.getValue(i));
             }
             if (id > 0 && lat > 0 && lon > 0) {
 	            Point2D coords = LatLonToUTM.convert(lat, lon);
             	if (coords.getX() < maxX && coords.getX() > minX && coords.getY() < maxY && coords.getY() > minY) {
             	  Point p = new Point(id, coords.getX(), coords.getY());
   	              points.put(p.getID()+pointsOffset, p);
             	}
             }
           } else if (qName.equalsIgnoreCase("WAY")) {
             inWay = true;
             currWay = new LinkedList<Integer>();
           } else if (qName.equalsIgnoreCase("ND") && inWay) {
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
           if (qName.equalsIgnoreCase("WAY")) {
             inWay = false;
             coastline.add(currWay);
           }
         }
 
         
       };
       saxParser.parse(coastFile, handler);
       saxParser.parse(coastFileSweden, handler);
       
       
 
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
     roads = new DimensionalTree<Double, RoadType, Road>();
     try(BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(roadFile), "ISO8859_1"))) {
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
         roads.insert(xS, yS, r.getType(), r);
         roads.insert(xE, yE, r.getType(), r);
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
   //TODO Fix stuff - Anders
   private static Road createRoad(String input) {
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
   private static Point createPoint(String input) {
     String[] inputSplit = input.split(",");
     return new Point(
         Integer.parseInt(inputSplit[2]),    //2 = id of the point
         Double.parseDouble(inputSplit[3]),  //3 = x coordinate
         Double.parseDouble(inputSplit[4])); //4 = y coordinate
   }
 
   private void parsePostalCodes(){
     System.out.println("- Parsing postal codes");
     postalCodes = new HashMap<Integer, String>();
     
     try(BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(postalCodesFile), "ISO8859_1"))) {  
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
   
   public SortedMap<String, Road> roadsWithPrefix(String prefix) {
     if(prefix.length() > 0) {
        prefix = prefix.toLowerCase(new Locale("ISO8859_1"));
         char nextLetter = (char) (prefix.charAt(prefix.length() - 1) + 1);
         String end = prefix.substring(0, prefix.length()-1) + nextLetter;
         return roadNames.subMap(prefix, end);
     }
     return roadNames;
   }
   
   public static String mapToJquery(SortedMap<String, Road> map) {
     String jq = "[ ";
     for(Entry<String, Road> e : map.entrySet()) {
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
   
   public static int getPointsOffset() {
     return pointsOffset;
   }
   
   public HashMap<Integer, Point> points() {
     return points;
   }
   
   public DimensionalTree<Double, RoadType, Road> roads() {
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
     return roads.count();
   }
 }
