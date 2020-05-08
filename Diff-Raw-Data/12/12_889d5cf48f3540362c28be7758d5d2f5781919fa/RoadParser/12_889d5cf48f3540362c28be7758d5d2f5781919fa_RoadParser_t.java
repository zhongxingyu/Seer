 package dk.itu.kf04.g4tw.util;
 
 import dk.itu.kf04.g4tw.model.AddressParser;
 import dk.itu.kf04.g4tw.model.MapModel;
 import dk.itu.kf04.g4tw.model.Node;
 import dk.itu.kf04.g4tw.model.Road;
 
 import java.awt.geom.Point2D;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.*;
 import java.util.logging.Logger;
 
 /**
  * A parser for Krak map-data.
  */
 public class RoadParser {
 
     /**
      * The logger for the class
      */
     static Logger Log = Logger.getLogger(RoadParser.class.getName());
     
     /**
      * Initializes the RoadParser and loads the map-data into the MapModel.
      * 
      * @param nodes  The file to load the Nodes from.
      * @param edges  The file to load the edges from.
      * @see MapModel
      * @see RoadParser
      */
     public static void load(File nodes, File edges) {
         try {
             HashMap<Integer, Node> nodeMap = parseNodes(nodes);
             parseEdges(edges, nodeMap);
         } catch (FileNotFoundException e) {
             Log.warning("Exception while processing map-data: ");
             e.printStackTrace();
         }
     }
     
     /**
      * Iterates through a file to the edges as Nodes and add them to the model.
      *
      * @param file  The file to parse the edges from
      * @param nodeMap  The nodeMap to receive the data from
      * @throws FileNotFoundException If the given file could not be found
      */
     protected static void parseEdges(File file, HashMap<Integer, Node> nodeMap) throws FileNotFoundException {
         // Start the id-counter
         int id = 0;
 
         // Load the file
         Scanner scanner = new Scanner(new FileInputStream(file), "ISO8859_1");
 
         // Hashmap of the roads with a name
         HashMap<String, DynamicArray<Road>> namedRoads = new HashMap<String, DynamicArray<Road>>();
 
         // TODO: Find et dynamisk fix til at sætte størrelse
         Road[] edges = new Road[812302];
         HashMap<Point2D.Double, ArrayList<Integer>> nodeRoadPair = new HashMap<Point2D.Double, ArrayList<Integer>>();
 
         // Fills in the data in the map
         scanner.nextLine();
         while (scanner.hasNextLine()) {
             // Split the line on everything except blocks with ['...,...']
             String[] nextLine = scanner.nextLine().split("((?<=\\d)|'),('|(?=\\d))");//split(",(?=([^']*'[^']*')*[^']*$)");
             //System.out.println(Arrays.toString(nextLine));
             String name = nextLine[6];
             // removes ' at the beginning and the end of the name
             name = name.replace("'", "");
 
             int a = Integer.parseInt(nextLine[0]);
             int b = Integer.parseInt(nextLine[1]);
 
             Node nodeA = nodeMap.get(a);
             Node nodeB = nodeMap.get(b);
 
             Point2D.Double pointA = new Point2D.Double(nodeA.x,nodeA.y);
             Point2D.Double pointB = new Point2D.Double(nodeB.x,nodeB.y);
 
             // Use the id's of the dynamic array so we promise consistency
             //int id = Integer.parseInt(nextLine[3]); // DAV_DK-ID
             //int id2 = Integer.parseInt(nextLine[4]); // DAV_DK-ID
             int type = Integer.parseInt(nextLine[5]);
             double speed = Double.parseDouble(nextLine[25]);
             double length = Double.parseDouble(nextLine[2]);
 
             int startNumber = 0, endNumber = 0;
             String startLetter = null, endLetter = null;
 
             // Only if the road has a name, it house numbers and letters should be saved
             if(!name.equals(" ") && !name.isEmpty()) {
                 int left = Integer.parseInt(nextLine[7]);
                 int right = Integer.parseInt(nextLine[9]);
                 if(left > 0) {
                     if(right > 0) {
                         if(left < right) startNumber = left;
                         else startNumber = right;
                     } else startNumber = left;
                 } else if(right > 0) startNumber = right;
 
                 left = Integer.parseInt(nextLine[8]);
                 right = Integer.parseInt(nextLine[9]);
                 if(left > 0) {
                     if(right > 0) {
                         if(left > right) endNumber = left;
                         else endNumber = right;
                     } else endNumber = left;
                 } else if(right > 0) endNumber = right;
 
 
                 if(!nextLine[11].equals("''")) {
                     if(!nextLine[13].equals("''")) {
                         if(nextLine[11].compareTo(nextLine[13]) < 0) startLetter = nextLine[11];
                         else startLetter = nextLine[13];
                     } else startLetter = nextLine[11];
                 } else if (!nextLine[13].equals("''")) startLetter = nextLine[13];
 
                startLetter = startLetter.toLowerCase();

                 if(!nextLine[12].equals("''")) {
                     if(!nextLine[14].equals("''")) {
                         if(nextLine[12].compareTo(nextLine[14]) > 0) endLetter = nextLine[12];
                         else endLetter = nextLine[14];
                     } else endLetter = nextLine[12];
                 } else if(!nextLine[14].equals("''")) endLetter = nextLine[14];

                endLetter = endLetter.toLowerCase();
             }
 
             type = MapModel.getRoadTypeFromId(type);
 
             // Create the road and setup connections/edges
             Road tmp = new Road(id++, name, pointA, pointB, type, speed, length, startNumber, endNumber, startLetter, endLetter);
 
             // TODO: Bruges de to næste linjer til noget!?
             nodeA.connectTo(tmp);
             nodeB.connectTo(tmp);
 
             // If the road has a name
             if(name.length() > 2) {
                 name = name.toLowerCase();
                 // If the road-name is not yet in the namesRoads-hashmap, add it
                 if(!namedRoads.containsKey(name))
                     namedRoads.put(name, new DynamicArray<Road>());
 
                 // Add the road to the corresponding collection
                 namedRoads.get(name).add(tmp);
             }
 
             // If the points are not yet in the nodeRoadPair-hashmap, add them.
             if(!nodeRoadPair.containsKey(pointA)) nodeRoadPair.put(pointA, new ArrayList<Integer>());
             if(!nodeRoadPair.containsKey(pointB)) nodeRoadPair.put(pointB, new ArrayList<Integer>());
 
             // Add the new road as an edge to all other roads that shares the same points
             // Add all other roads with same points to the new road
             // --> Creates a UNDIRECTED graph!
             for(int i : nodeRoadPair.get(pointA)) {
                 edges[i].addEdge(tmp);
                 tmp.addEdge(edges[i]);
             }
 
             for(int i : nodeRoadPair.get(pointB)) {
                 edges[i].addEdge(tmp);
                 tmp.addEdge(edges[i]);
             }
 
             // Add the new roads ID to the hashmap
             nodeRoadPair.get(pointA).add(id);
             nodeRoadPair.get(pointB).add(id);
 
             // Add the road to the edges collection
             edges[id] = tmp;
         }
         // Close the scanner
         scanner.close();
 
         // Directs the graph
         edges = trim(edges);
 
         // Set the namedRoads hashmap in AddressParser
         AddressParser.setNamedRoads(namedRoads);
 
         // Add the road to the MapModel
         for(Road road : edges)
             if(road != null)
                 MapModel.addRoad(road);
 
         // Log success
         Log.fine("Successfully loaded Map edges into model.");
     }
 
     /**
      * Directs the graph, by following the turn.txt file
      * @param arr Array of the roads (the undirected graph)
      * @return Array of the roads (now as an directed graph)
      */
     protected static Road[] trim(Road[] arr)
     {
         Scanner scanner = null;
         try {
             scanner = new Scanner(new FileReader("turn.txt"));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
 
         scanner.nextLine();
         while(scanner.hasNextLine())
         {
             String[] nextLine = scanner.nextLine().split(",");
             int fID = Integer.parseInt(nextLine[2]);
             int tID = Integer.parseInt(nextLine[3]);
 
             // Make the graph directed
             Iterator<Road> it = arr[fID].iterator();
             while(it.hasNext())
             {
                 if(it.next().getId() == tID) {
                     it.remove();
                     break;
                 }
             }
         }
         scanner.close();
         return arr;
     }
 
     /**
      * Tries to parse a HashMap of Nodes from a given file.
      * @param file  The file to take data from
      * @return  The HashMap of nodes
      * @throws FileNotFoundException  If the file could not be found
      */
     protected static HashMap<Integer, Node> parseNodes(File file) throws FileNotFoundException {
         // Load the file
         Scanner scanner = new Scanner(new FileReader(file));
 
         // Create The HashMap
         HashMap<Integer, Node> nodeMap = new HashMap<Integer, Node>();
         
         // Try to load the data
         scanner.nextLine();
         while(scanner.hasNextLine()) {
             String[] nextLine = scanner.nextLine().split(",");
             // Find the id and positions
             int id = Integer.parseInt(nextLine[2]);
             double xPos = Double.parseDouble(nextLine[3]);
             double yPos = Double.parseDouble(nextLine[4]);
             
             // Create the node
             Node node = new Node(xPos, yPos);
 
             // Insert it into the map
             nodeMap.put(id, node);
         }
         scanner.close();
 
         Log.fine("Successfully parsed Map nodes.");
         
         return nodeMap;
     }
 }
