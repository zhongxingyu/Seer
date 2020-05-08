 package controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 
 import javax.swing.JComponent;
 
 import visualization.FileLoader;
 import visualization.OurMapImpl;
 import visualization.FileLoaderImpl;
 import dataStructure.Connection;
 import dataStructure.Interval;
 import dataStructure.Interval2D;
 import dataStructure.Point;
 import dataStructure.PointQuadTree;
 
 import GUI.GUI;
 
 /**
  * 
  * @author Claus L. Henriksen clih@itu.dk
  * @author Pacmans
  * @version 29. Marts 2012
  *
  */
 public final class Controller {
   private static Controller instance; //singleton
   private static GUI gui; //singleton
   private static OurMapImpl map; //singleton
   private static FileLoader fileLoader;
   private Point[] points;
   private Connection[] connections;
   private PointQuadTree qt;
   
   /**
    * Constructor for this class loads connections and points fomr FileLoader
    * @see FileLoaderImpl
    */
   public Controller(){
     if(instance == null) instance = this;
     try{
       fileLoader = new FileLoaderImpl();
       connections = fileLoader.getConnections();
       points = fileLoader.getCords();
     }catch(IOException e){
       System.out.println("Fileloader: " + e);
     }
   }
   
   /**
    * 
    * @return Returns an instance of the singleton Controller (this class)
    */
   public static Controller getInstance(){
     if(instance == null) return new Controller(); //should not happen
     return instance;
   }
   
   /**
    * Private method creates quad tree and inserts all points
    * @see PointQuadTree
    * @see Point
    */
   private void initialiseQt() {
     qt = new PointQuadTree();
     for(Point point : points){
       qt.inset(point);
     }
   }
   
   /**
    * 
    * @return Returns a quad tree of Points
    * @see Point
    * @see PointQuadTree
    */
   public PointQuadTree getPointQuadTree(){
     if(qt == null) initialiseQt();
     return qt;
   }
 
   /**
    * 
    * @return Returns instance of the singleton class GUI
    * @see GUI-class
    */
   public static GUI getGUI(){
     if(gui == null) gui = new GUI();
     return gui;
   }
   
   /**
    * 
    * @return Returns instance of the singleton class Map which paints the map
    * @see OurMapImpl
    */
   public static JComponent getMap(){
     if(map == null) map = new OurMapImpl();
     return map;
   }
   
   public static FileLoader getFileLoader(){
     try{
       if(fileLoader == null) fileLoader = new FileLoaderImpl();
       return fileLoader;
     }catch(IOException e){
       System.out.println("FileLoader: " + e);
       return null;
     }
   }
   
   /**
    * Get array of all points
    * @return Array of all points
    * @see Point
    */
   public Point[] getPoints(){
     return points;
   }
   
   /**
    * Get all points within rectangle
    * @param x1 
    * @param y1
    * @param x2
    * @param y2
    * @return ArrayList of points within rectangle
    */
   public ArrayList<Point> getPoints(int x1, int y1, int x2, int y2){
     if(qt == null)  initialiseQt();
     return qt.getPoints(new Interval2D(new Interval(x1, x2), new Interval(y1, y2)));
   }
   
   /**
    * Get all connections within rectangle
    * @param x1
    * @param y1
    * @param x2
    * @param y2 ArrayList of connections within rectangle
    * @return
    */
   public ArrayList<Connection> getConnections(int x1, int y1, int x2, int y2){
     HashSet<Integer> cons = qt.getConnections(new Interval2D(new Interval(x1, x2), new Interval(y1, y2)));
     ArrayList<Connection> cs = new ArrayList<Connection>();
    Arrays.sort(connections); //TODO Delete this when implemented in a better place
     for(Integer i : cons){
       cs.add(connections[Arrays.binarySearch(connections, i)]);
     }
     return cs;
   }
   
   /**
    * Get array of all connections
    * @return Array of all connections
    * @see Connection
    */
   public Connection[] getConnections(){
     return connections;
   }
   
   /**
    * Show or hide a type of road
    * @param n type_id
    * @param b To show or not to show
    */
   public void updateMap(int n, boolean b){
     map.updateFilter(n, b);
   }
   
   /**
    * Main method creates a new GUI
    * @see GUI-class
    * @param args
    */
   public static void main(String[] args) {
     GUI gui = new GUI();
   }
 }
