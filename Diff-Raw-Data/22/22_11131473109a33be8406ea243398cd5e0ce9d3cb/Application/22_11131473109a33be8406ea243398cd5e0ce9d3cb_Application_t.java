 import ohm.roth.lbs.*;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 /**
  * User: mischarohlederer
  * Date: 27.10.11
  * Time: 11:51
  */
 public class Application {
     private Scanner scan = new Scanner(System.in);
     private NavGraph graph = new NavGraph();
     private String dbhost = "geo.informatik.fh-nuernberg.de";
     private int dbport = 5432;
     private String dbuser = "dbuser";
     private String dbpasswd = "dbuser";
     private String dbname = "deproDBMittelfranken";
    private String graphname = "NBG2";
     private TSP.tspType defaultTspType = TSP.tspType.GENETIC_GOOD;
 
     public Application(String dbhost, int dbport, String dbuser, String dbpasswd, String dbname, String graphname, int debug) {
         this.dbhost = dbhost;
         this.dbport = dbport;
         this.dbuser = dbuser;
         this.dbpasswd = dbpasswd;
         this.dbname = dbname;
         this.graphname = graphname;
         graph = new NavGraph();
         scan = new Scanner(System.in);
     }
 
     public void mainMenu() {
         System.out.println("============================================================");
         System.out.println("Roth LBS Aufgabe 2011");
         System.out.println("Fuch Daniel, Rohlederer Mischa");
         System.out.println("============================================================");
 
         double time_global = System.currentTimeMillis();
         double w = 1;
 
         // Zu benutzender Kartenausschnitt
         Position topleft = new Position(-11.18493167, 49.49496333);
         Position botright = new Position(-10.99845000, 49.39349500);
 
         // Pfad einstellungen
         String pathName = "Aufgabe 01";
 
         // Datenbank zugang
         File file = new File(graphname + ".DAT");
         if (file.exists()) {
             loadGraph_File(graphname);
         } else {
             loadGraph_Database(topleft, botright, graphname);
         }
 
         boolean quit = false;
         do {
             System.out.println();
             System.out.println("============================================================");
             System.out.println("Please Make a selection:");
             System.out.println("============================================================");
             System.out.println("[1] Build Path");
             System.out.println("[2] Graph Settings");
             System.out.println("[3] Change Graph");
             System.out.println("[4] Benchmarks");
             System.out.println("[0] exit");
             System.out.print("Select: ");
             int menu = scan.nextInt();
             switch (menu) {
                 case 1:
                     pathMenu();
                     break;
                 case 2:
                     graphMenu();
                     break;
                 case 3:
 
                     break;
                 case 4:
                     benchmarkMenu();
                     break;
                 case 0:
                     quit = true;
                     break;
             }
         }
 
         while (!quit);
         System.exit(0);
 
 
     }
 
     // Pfad generator
     public void pathMenu() {
         boolean quit = false;
         do {
             System.out.println("==================================================");
             System.out.println("Building Path Menu");
             System.out.println("==================================================");
             System.out.println();
             System.out.println("Please Make a selection:");
             System.out.println("[1] From File");
             System.out.println("[0] back");
 
             System.out.println("Selection: ");
             int menu = scan.nextInt();
             switch (menu) {
                 case 1:
                     System.out.print("Name: ");
                     String name = scan.next();
                     System.out.print("Filename: ");
                     String filename = scan.next();
                     System.out.print("W: ");
                     double w = scan.nextDouble();
                     findPath(filename, 0.5, 1, name);
                     break;
                 case 0:
                     quit = true;
                     break;
                 default:
                     System.out.println("Invalid Entry!");
             }
         }
         while (!quit);
     }
 
     public void findPath(String filename, double w, int debug, String name) {
         System.out.println("==================================================");
         System.out.println("Finding Path");
         System.out.println("Filename: " + filename);
         System.out.println("==================================================");
         List<Waypoint> waypoints;
         try {
             waypoints = CSVParse.readWaypointList(filename);
         } catch (Exception e) {
             e.printStackTrace();
             return;
         }
         if (waypoints.size() == 0) return;
         AStarAlgorithm astar = new AStarAlgorithm(w, debug);
         List<Waypoint> orderdWaypoints;
 
 
         // Rundweg
         if (waypoints.size() > 2) {
             double time_tsp = System.currentTimeMillis();
             System.out.println("Running TSP with " + (waypoints.size()) + " Points");
             orderdWaypoints = TSP.sort(waypoints, defaultTspType);
             time_tsp = (System.currentTimeMillis() - time_tsp) / 1000;
             System.out.println("TSP took " + time_tsp + " Secs");
         } else {
             orderdWaypoints = waypoints;
         }
 
         try {
             FileWriter fstream = new FileWriter(name + "-TSP.GPX");
             BufferedWriter out = new BufferedWriter(fstream);
             out.write(GPXBuilder.build("", orderdWaypoints, ""));
             out.close();
         } catch (IOException e) {
             System.out.println("Error writing GPX file");
         }
 
 
         // A-Stern
         double globalLength = 0;
         Path path = new Path(name);
         double time_astar = System.currentTimeMillis();
         System.out.println("Running A-Star " + (orderdWaypoints.size() - 1) + " times");
         if (orderdWaypoints.size() > 2) {
             // A Stern im Rundweg
             try {
                 for (int i = 0; i <= orderdWaypoints.size() - 1; i++) {
                     Node start = graph.findClosest2(orderdWaypoints.get(i));
                     Node end = graph.findClosest2(orderdWaypoints.get((i + 1) % (orderdWaypoints.size())));
                     AStarAlgorithm.AStarResult result = astar.search(graph, start, end, orderdWaypoints.get(i).getName()+" - "+orderdWaypoints.get((i + 1) % (orderdWaypoints.size())).getName());
                     result.print();
                     globalLength += result.getLength();
                     path.addSegment(result.getPath());
                 }
             } catch (Exception e) {
                 System.out.println("Error running A-Star: " + e.toString());
                 e.printStackTrace();
                 System.exit(1);
             }
         } else {
             try {
                 Node start = graph.findClosest(orderdWaypoints.get(0));
                 Node end = graph.findClosest(orderdWaypoints.get(1));
                 AStarAlgorithm.AStarResult result = astar.search(graph, start, end, orderdWaypoints.get(0).getName()+" - "+orderdWaypoints.get(1).getName());
                 result.print();
                 globalLength += result.getLength();
                 path.addSegment(result.getPath());
                 System.out.println(result.getPath().getLength());
             } catch (Exception e) {
                 System.out.println("Error running A-Star: " + e.toString());
                 e.printStackTrace();
                 System.exit(1);
             }
         }
         time_astar = (System.currentTimeMillis() - time_astar) / 1000;
         System.out.println();
         System.out.println("A-Star took " + time_astar + " Secs");
         System.out.println("Global Length: " + globalLength + " Meter");
         try {
             FileWriter fstream = new FileWriter(name + "-Topo.GPX");
             BufferedWriter out = new BufferedWriter(fstream);
             out.write(GPXBuilder.build("", path));
             out.close();
         } catch (IOException e) {
             System.out.println("Error writing GPX file");
         }
     }
 
     // Benchmark A-Stern
     public void benchmarkMenu() {
         List<Position> places = new ArrayList<Position>();
         places.add(new Position(-10.99845000, 49.39349500));
         places.add(new Position(-11.18493167, 49.49496333));
         boolean quit = false;
         do {
             System.out.println("==================================================");
             System.out.println("Benchmarks");
             System.out.println("==================================================");
             System.out.println();
             System.out.println("Please Make a selection:");
             System.out.println("[1] Weighted A-Star");
             System.out.println("[2] TSP");
             System.out.println("[0] exit");
 
             System.out.println("Selection: ");
             System.out.print("Please enter your choice: ");
             System.out.println();
             int menu = scan.nextInt();
             switch (menu) {
                 case 1:
                     System.out.print("Start W: ");
                     double wStart = scan.nextDouble();
                     System.out.print("End W: ");
                     double wEnd = scan.nextDouble();
                     System.out.print("Steps: ");
                     double wStep = scan.nextDouble();
                     compareAStarW(graph, places, wStart, wEnd, wStep);
                     break;
                 case 2:
                     compareTSP();
                     break;
                 case 0:
                     quit = true;
                     break;
                 default:
                     System.out.println("Invalid Entry!");
             }
         }
         while (!quit);
     }
 
     public void compareAStarW(NavGraph graph, List<Position> places, double start, double end, double step) {
         if (end < start) {
             double tmp = end;
             end = start;
             start = tmp;
         }
         Node startNode = graph.findClosest(places.get(0));
         Node endNode = graph.findClosest(places.get((1)));
         System.out.println("==================================================");
         System.out.println("Benchmark A-Star with weighted h");
         System.out.println("From " + start + " to " + end + " steps " + step);
         System.out.println(distance.calcDist(startNode, endNode));
         System.out.println("==================================================");
         System.out.println();
 
         // Vergleichs A-Stern mit w = 1
         AStarAlgorithm.AStarResult compareResult;
         AStarAlgorithm astar = new AStarAlgorithm(1, 0);
         try {
             compareResult = astar.search(graph, startNode, endNode, "Compare");
         } catch (Exception e) {
             return;
         }
 
         //
         double curr;
         for (curr = start; curr <= end; curr += step) {
             Path path = new Path("");
             astar = new AStarAlgorithm(curr, 0);
             AStarAlgorithm.AStarResult result;
             try {
                 result = astar.search(graph, startNode, endNode, "Compare " + curr);
                 path.addSegment(result.getPath());
             } catch (Exception e) {
                 return;
             }
             try {
                 FileWriter fstream = new FileWriter(curr + ".gpx");
                 BufferedWriter out = new BufferedWriter(fstream);
                 String gpx = GPXBuilder.build("", path);
                 out.write(gpx);
                 out.close();
                 fstream.close();
             } catch (Exception e) {
                 return;
             }
 
             System.out.println("Result for w=" + curr);
             System.out.println("Expanded Nodes: " + result.getExpandedNodes() + " (" + (result.getExpandedNodes() - compareResult.getExpandedNodes()) + ")");
             System.out.println("Path Length: " + result.getPath().getLength() + " (" + (result.getPath().getLength() - compareResult.getPath().getLength()) + ")");
             System.out.println("Runtime: " + result.getRuntime() + " (" + (result.getRuntime() - compareResult.getRuntime()) + ")");
             System.out.println();
         }
 
 
     }
 
     public void compareTSP() {
         List<Waypoint> waypoints;
         List<Waypoint> orderdWaypoints;
         try {
             waypoints = CSVParse.readWaypointList("01.csv");
         } catch (Exception e) {
             e.printStackTrace();
             return;
         }
         System.out.println("==================================================");
         System.out.println("TSP Compare");
         System.out.println("Number of Waypoints: " + waypoints.size());
         System.out.println("==================================================");
         System.out.println();
         for (TSP.tspType currTSP : TSP.tspType.values()) {
             double time_tsp = System.currentTimeMillis();
             System.out.println("Running " + currTSP + " with " + (waypoints.size()) + " Points");
             try {
                 orderdWaypoints = TSP.sort(waypoints, currTSP);
                 time_tsp = (System.currentTimeMillis() - time_tsp) / 1000;
                 FileWriter fstream;
                 try {
                     fstream = new FileWriter(currTSP.toString() + ".GPX");
                     BufferedWriter out = new BufferedWriter(fstream);
                     out.write(GPXBuilder.build("", orderdWaypoints, currTSP.toString()));
                     out.close();
                 } catch (IOException e) {
                     System.out.println("Error writing GPX File");
                 }
                 System.out.println("Path length: " + distance.listLength(orderdWaypoints));
                 System.out.println(currTSP + " took " + time_tsp + " Secs");
                 System.out.println();
             } catch (IllegalArgumentException e) {
                 System.out.println(e.getMessage());
                 System.out.println();
             }
         }
         System.out.println();
     }
 
     // Graph Optionen
     public void graphMenu() {
         boolean quit = false;
         do {
             System.out.println("==================================================");
             System.out.println("Graph Options");
             System.out.println("Loaded Graph: " + graph.getName());
             System.out.println("==================================================");
             System.out.println();
             System.out.println("Please Make a selection:");
             System.out.println("[1] Graph Infromation");
             System.out.println("[2] Change Graph");
             System.out.println("[0] back");
 
             System.out.println("Selection: ");
             int menu = scan.nextInt();
             switch (menu) {
                 case 1:
                     graphInfo();
                     break;
                 case 2:
                     graphChange();
                     break;
                 case 0:
                     quit = true;
                     break;
                 default:
                     System.out.println("Invalid Entry!");
             }
         }
         while (!quit);
     }
 
     public void graphInfo() {
         System.out.println("==================================================");
         System.out.println("Graph Information");
         System.out.println("==================================================");
         System.out.println("Node Count: " + graph.nodeList.size());
         System.out.println("Edge Count: " + graph.edgeList.size());
     }
 
     public void graphChange() {
         Scanner scan = new Scanner(System.in);
         boolean quit = false;
         do {
             System.out.println("==================================================");
             System.out.println("Change Graph");
             System.out.println("==================================================");
             System.out.println();
             System.out.println("Please Make a selection:");
             System.out.println("[1] From file");
             System.out.println("[2] From Database");
             System.out.println("[0] back");
             System.out.println("Selection: ");
             int menu = scan.nextInt();
             switch (menu) {
                 case 1:
                     System.out.print("Graphname: ");
                     String filename = scan.next();
                     break;
                 case 2:
                     System.out.println("Top Left:");
                     System.out.println("Lon:");
                     double lon1 = scan.nextDouble();
                     System.out.println("Lat:");
                     double lat1 = scan.nextDouble();
                     System.out.println("Lon:");
                     double lon2 = scan.nextDouble();
                     System.out.println("Lat:");
                     double lat2 = scan.nextDouble();
                     loadGraph_Database(lon1, lon2, lat1, lat2, "F00.DAT");
                     break;
                 case 3:
 
                     break;
                 case 4:
 
                     break;
                 case 0:
                     quit = true;
                     break;
                 default:
                     System.out.println("Invalid Entry!");
             }
         }
         while (!quit);
     }
 
     // Graph loader
     public void loadGraph_File(String filename) {
         // Graph aus Datei laden
         double time_graph = System.currentTimeMillis();
         filename = filename + ".DAT";
         System.out.println("============================================================");
         System.out.println("Fetching Data from " + filename + " and building graph");
         try {
             FileInputStream fis = new FileInputStream(filename);
             ObjectInputStream obj_in = new ObjectInputStream(fis);
             Object obj = obj_in.readObject();
             if (obj instanceof NavGraph) {
                 this.graph = (NavGraph) obj;
             } else {
                 System.out.println("Saved Data != NavGraph");
             }
             obj_in.close();
             fis.close();
             time_graph = (System.currentTimeMillis() - time_graph) / 1000;
             System.out.println("Fetching Data and building graph took: " + time_graph + "sec");
             System.out.println("============================================================");
         } catch (Exception e) {
             System.out.println("Error: " + e.toString());
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     public void loadGraph_Database(Position pos1, Position pos2, String name) {
         loadGraph_Database(pos1.getLon(), pos2.getLon(), pos1.getLat(), pos2.getLat(), name);
     }
 
     public void loadGraph_Database(double lon1, double lon2, double lat1, double lat2, String name) {
         // Graph aus Datei laden
         double time_graph = System.currentTimeMillis();
         System.out.println("============================================================");
         geoDatabaseConnection connection = new geoDatabaseConnection(dbhost, dbport, dbuser, dbpasswd, dbname);
 
         // Graph generieren
         File file = new File(name + ".DAT");
         // Graph von DB laden
         System.out.println("Fetching Data from DB and building graph");
         try {
             connection.connect();
             graph = connection.getGraph(new Position(lon1, lat1), new Position(lon2, lat2), name);
             FileOutputStream fos = new FileOutputStream(file.getName());
             ObjectOutputStream out2 = new ObjectOutputStream(fos);
             out2.writeObject(graph);
             out2.close();
             fos.close();
             time_graph = (System.currentTimeMillis() - time_graph) / 1000;
             System.out.println("Fetching Data and building graph took: " + time_graph + "sec");
             System.out.println("============================================================");
         } catch (Exception e) {
             System.out.println("Error: " + e.toString());
             e.printStackTrace();
             System.exit(1);
         }
     }
 }
