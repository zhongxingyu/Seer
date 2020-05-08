 package controller;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import exceptions.ExceptionController;
 import files.FileLoaderConnectionOnly;
 import graph.Graph;
 import gui.GUI;
 
 import visualization.MapComponent;
 import dataStructure.AddressParser;
 
 import visualization.SliderComponent;
 import dataStructure.Connection;
 import dataStructure.ConnectionQuadTree;
 import dataStructure.DynArray;
 import dataStructure.Interval;
 import dataStructure.Interval2D;
 import dataStructure.Point;
 import dataStructure.TernarySearchTries;
 
 /**
  * 
  * @author Claus L. Henriksen clih@itu.dk
  * @author Pacmans
  * @version 10. April 2012
  * 
  */
 public final class Controller {
 
   private static Controller instance; // singleton
   private GUI gui; //singleton
   private MapComponent map; //singleton
   private SliderComponent slider; //singleton
   private Graph graph; //singleton
   private AddressParser parser; //Parses human written address
   volatile private TernarySearchTries<Integer> tst; //Trie for finding roads based on street name
   volatile private Connection[] connections; //Array of all connections
   volatile private Point[] points; //Array of all points
   volatile private ConnectionQuadTree highwaysQT; // 1
   volatile private ConnectionQuadTree expresswaysQT; // 2
   volatile private ConnectionQuadTree primaryQT; // 3
   volatile private ConnectionQuadTree secondaryQT; // 4
   volatile private ConnectionQuadTree normalQT; // 5
   volatile private ConnectionQuadTree smallQT; // 6
   volatile private ConnectionQuadTree pathsQT; // 7
   private double xMin, yMin, xMax, yMax; //Boundary limit values used to center map on view
   private HashMap<String, String> postal = new HashMap<String, String>(); //zip, city
 
   /**
    * Constructor for this class loads connections and points from FileLoader
    * @see FileLoader
    */
   public Controller() {
 	parser = new AddressParser();
     instance = this;
     gui = new GUI();
     try {
       new FileLoaderConnectionOnly();
     } catch (Exception e) {
       ExceptionController.recieveException(e);
     }
     
     setStatus("Creating graph");
     graph = new Graph();
     setStatus("Graph created. All done");
     gui.enableSearch(true);
   }
   
   /**
    * Main method creates a new Controller
    * 
    * @see GUI-class
    * @see MapComponent
    * @see FileLoaderConnectionOnly
    */
   public static void main(String[] args) {
     new Controller();
   }
 
   /**
    * @return Returns an instance of the singleton Controller (this class)
    */
   public static Controller getInstance() {
     if (instance == null) {
       new Controller(); // Constructor in Controller updates instance
     }
     return instance;
   }
 
   /**
    * @return Returns instance of the singleton class GUI
    * @see GUI
    */
   public GUI getGUI() {
     if (gui == null) {
       gui = new GUI();
     }
     return gui;
   }
 
   /**
    * @return Returns instance of the singleton class MapComponent which paints the map
    * @see MapComponent
    */
   public MapComponent getMap() {
     if (map == null) {
       map = new MapComponent();
     }
     return map;
   }
   
   /**
    * @return Returns instance of the singleton class Graph used for route finding
    * @see Graph
    */
   public Graph getGraph(){
     if(graph == null) graph = new Graph();
     return graph;
   }
   
   /**
    * @return Returns instance of the singleton class SliderComponent which paints the slider
    * @see MapComponent
    */
   public SliderComponent getSlider() {
     if (slider == null) {
       slider = new SliderComponent();
     }
     return slider;
   }
   
   /**
    * @return Returns instance of the singleton class TernarySearchTries used for finding roads by street name
    * @see TernarySearchTries
    */
   public TernarySearchTries<Integer> getTst() {
     if(tst == null) tst = new TernarySearchTries<Integer>();
     return tst;
   }
   
   /**
    * Get array of all connections
    * 
    * @return Array of all connections
    * @see Connection
    */
    synchronized public Connection[] getConnections() {
     return connections;
   }
    
    /**
     * 
     * @return Array of all points
     * @see Point
     */
    synchronized public Point[] getPoints(){
      return points;
    }
 
   /**
    * Set status label on GUI
    * 
    * @param s Status to be shown
    */
   public void setStatus(String s) {
     gui.setStatus(s);
   }
   
   /**
    * Catch an exception and send it 
    * to the ExceptionController
    * @param e
    */
   public static void catchException(Exception e) {
     ExceptionController.recieveException(e);
   }
   
   /**
    * Show or hide a type of road
    * @param n type_id
    * @param b To show or not to show
    */
   public synchronized void updateMap(int n, boolean m) {
     map.updateRoadTypes(n, m);
   }
 
   /**
    * Zoom out and center map
    */
   public void showAll() {
     map.resetZoom();
   }
 
   /**
    * Getter for boundary limit value
    * @return Smallest x-value on map
    */
   public double getxMin() {
     return xMin;
   }
 
   /**
    * Getter for boundary limit value
    * @return Smallest y-value on map
    */
   public double getyMin() {
     return yMin;
   }
 
   /**
    * Getter for boundary limit value
    * @return Biggest x-value on map
    */
   public double getxMax() {
     return xMax;
   }
 
   /**
    * Getter for boundary limit value
    * @return Biggest y-value on map
    */
   public double getyMax() {
     return yMax;
   }
   
 
   /**
    * Setter for boundary limit value
    * @param xMin the xMin to set
    */
   public synchronized void setxMin(double xMin) {
     this.xMin = xMin;
   }
 
   /**
    * Setter for boundary limit value
    * @param yMin the yMin to set
    */
   public synchronized void setyMin(double yMin) {
     this.yMin = yMin;
   }
 
   /**
    * Setter for boundary limit value
    * @param xMax the xMax to set
    */
   public synchronized void setxMax(double xMax) {
     this.xMax = xMax;
   }
 
   /**
    * Setter for boundary limit value
    * @param yMax the yMax to set
    */
   public synchronized void setyMax(double yMax) {
     this.yMax = yMax;
   }
 
   /**
    * Getters for QuadTrees holding connection-IDs
    * @return the highwaysQT
    */
   public synchronized ConnectionQuadTree getHighwaysQT() {
     if(highwaysQT == null) highwaysQT = new ConnectionQuadTree();
     return highwaysQT;
   }
 
   /**
    * Getters for QuadTrees holding connection-IDs
    * @return the expresswaysQT
    */
   public synchronized ConnectionQuadTree getExpresswaysQT() {
     if(expresswaysQT == null) expresswaysQT = new ConnectionQuadTree();
     return expresswaysQT;
   }
 
   /**
    * Getters for QuadTrees holding connection-IDs
    * @return the primaryQT
    */
   public synchronized ConnectionQuadTree getPrimaryQT() {
     if(primaryQT == null) primaryQT = new ConnectionQuadTree();
     return primaryQT;
   }
 
   /**
    * Getters for QuadTrees holding connection-IDs
    * @return the secondaryQT
    */
   public synchronized ConnectionQuadTree getSecondaryQT() {
     if(secondaryQT == null) secondaryQT = new ConnectionQuadTree();
     return secondaryQT;
   }
 
   /**
    * Getters for QuadTrees holding connection-IDs
    * @return the normalQT
    */
   public synchronized ConnectionQuadTree getNormalQT() {
     if(normalQT == null) normalQT = new ConnectionQuadTree();
     return normalQT;
   }
 
   /**
    * Getters for QuadTrees holding connection-IDs
    * @return the smallQT
    */
   public synchronized ConnectionQuadTree getSmallQT() {
     if(smallQT == null) smallQT = new ConnectionQuadTree();
     return smallQT;
   }
 
   /**
    * Getters for QuadTrees holding connection-IDs
    * @return the pathsQT
    */
   public synchronized ConnectionQuadTree getPathsQT() {
     if(pathsQT == null) pathsQT = new ConnectionQuadTree();
     return pathsQT;
   }
 
   /** 
    * Set the array of connections held by Controller
    * @param connections 
    */
   public synchronized void setConnections(Connection[] connections) {
     this.connections = connections;
   }
   
   /**
    * Set the array of points held by Controller
    * @param points
    */
   public synchronized void setPoints(Point[] points){
     this.points = points;
   }
   
   /**
    * 
    * @return HashMap with <k, v> <Zip, City> as Strings
    */
   public HashMap<String, String> getPostal(){
     return postal;
   }
 
   /**
    * Get all connections within rectangle
    * @param x1
    * @param y1
    * @param x2
    * @param y2
    * @return ArrayList of connections within rectangle
    */
   public synchronized Connection[] getConnections(int type, double x1, double y1, double x2, double y2) {
     //Determine road type
     ConnectionQuadTree qt = new ConnectionQuadTree();
     switch (type) {
     case 1:
       qt = highwaysQT;
       break;
     case 2:
       qt = expresswaysQT;
       break;
     case 3:
       qt = primaryQT;
       break;
     case 4:
       qt = secondaryQT;
       break;
     case 5:
       qt = normalQT;
       break;
     case 6:
       qt = smallQT;
       break;
     case 7:
       qt = pathsQT;
       break;
     default:
       qt = pathsQT;
       break;
     }
     // get array of connection IDs from QuadTree
     DynArray<Integer> cons = qt.getConnections(new Interval2D(new Interval(x1,
         x2), new Interval(y1, y2)));
     Connection[] cs = new Connection[cons.size()];
     int size = 0;
     //Parse IDs to actual connections
     for (Integer i : cons) { // this is slow
       cs[size++] = connections[i];
     }
     return cs;
   }
 
   /**
   * Get Roads method takes a String and sends it to address parser. The address parser chops it up into an array of strings
   * it then sends the first string (road name) to TST and ask it to find keys with the given prefix
   * then it creates an array of strings containing roads with the given prefix.
   * @param key		the test written in LiveSearchBox.
   * @return		array of String that can be placed in the drop down menu in the LiveSearchBox.
    */
   public String[] getRoads(String key) {
 	  String[] address = null;
 	try {		address = parser.parseAddress(key);
 	} catch (Exception e1) {
 		e1.printStackTrace();
 	}
 	  String[] roads = new String[10];
 	  Iterator<Integer> tmp = null;
 	  try{
 	   tmp = tst.keysWithPrefix(address[0]).iterator();
 	  } catch (NullPointerException e){// if there is no roads starting with the given string
 		  roads[0] = "(none)";
 		  return roads;
 	  }
 	  Connection q;
 	  for(int i = 0; i < roads.length; i++) //doing a for loop over the road array
 	  {
 		  if(tmp.hasNext()){ // doing it as long as the tmp still got more elements.
 			  q = connections[tmp.next()]; //sets q to the next value from the tmp.
 			  if(Integer.parseInt(address[1]) != 0){ // does it have a house number?
 				  if(q.getName().equalsIgnoreCase(address[0]) && address[3] != null && ((""+q.getLeft().getZip()).startsWith(address[3]) && address[3].length() > 3)){
 					  //only here if there is a zip code.
 					  roads[i] = q.getName()+" "+address[1]+address[2]+", "+q.getLeft().getZip()+" "+address[4];
 				  }
 			  else if(q.getName().equalsIgnoreCase(address[0])){ //if the string does not have a zip code
 				  roads[i] = q.getName()+" "+address[1]+address[2]+", "+q.getLeft().getZip()+" "+getPostal().get(""+q.getLeft().getZip());
 			  }
 			  
 			  }else{ //if there is no house number
 			  if(getPostal().get(""+q.getLeft().getZip()) != null){ //if we are not in Sweden
 				  roads[i] = q.getName()+" "+address[1]+", "+q.getLeft().getZip()+" "+getPostal().get(""+q.getLeft().getZip());
 			  }
 			  else roads[i] = q.getName()+" "+address[1]+", "+q.getLeft().getZip()+" Sweden";
 			  }
 		  }
 		  if(roads[i] == null) roads[i] = " "; //removing all null values from the array
 	  }
 	  Arrays.sort(roads); //sort the array so we dont get empty rooms before at the end
 	  for(int i = 0; i < 5; i++){ // turning the array around.
 		  String t = roads[i];
 		  roads[i] = roads[9-i];
 		  roads[9-i] = t;
 	  }
 	  return roads;
   }
   
   /**
   * THis methods gets 2 String representing a from and a to, it then gives them to the address parser (to chop up the string), 
    * then to the TST (to get the corresponding connection ID), then to Graph (to calculated the shortest route) and then to map
    * to paint the route.
   * that calculates the shortest route.
    * @param from		the "from" String
    * @param to			the "to" String
    */
   public void getRoadPlan(String from, String to){
 	  String[] address1 = null;
 	  int tzip = 0, fzip = 0;
 		try { // try sending them to the address parser
 			address1 = parser.parseAddress(from);
 			from = address1[0];
 			fzip =  Integer.parseInt(address1[3]);
 			address1 = parser.parseAddress(to);
 			to = address1[0];
 			tzip =  Integer.parseInt(address1[3]);
		} catch (Exception e1) {
			e1.printStackTrace();
 		}
 		Point start = connections[tst.get(from, fzip)].getLeft(); //get the connection using the TST to get the value.
 		Point finish = connections[tst.get(to, tzip)].getLeft();
 		try{// try finding the value in graph and use them to paint the route.
 		  Connection[] con = getGraph().shortestPath(start, finish);
 		  map.setRoute(con, graph.getXmin(), graph.getYmin(), graph.getXmax(), graph.getYmax()); 
 		} catch (RuntimeException e){ 
 		  ExceptionController.recieveException(e);
 		}
   }
 }
