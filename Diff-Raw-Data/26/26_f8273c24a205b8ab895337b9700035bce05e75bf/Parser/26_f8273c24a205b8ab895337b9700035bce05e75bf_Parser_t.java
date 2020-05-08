 package dk.itu.grp11.main;
 
 import java.io.*;
 import java.util.HashMap;
 
 import dk.itu.grp11.contrib.DimensionalTree;
 import dk.itu.grp11.contrib.DynArray;
 import dk.itu.grp11.enums.MinMax;
 import dk.itu.grp11.enums.RoadType;
 import dk.itu.grp11.exceptions.DataNotInitializedException;
 
 /**
  * The Parser class is used to parse the information from the two documents
  * kdv_node_unload.txt and kdv_unload.txt into arrays of Points and Roads, which
  * are then used by our Map class.
  * 
  * @param nodes
  *          A java.File object referencing the kdv_node_unload.txt file.
  * @param connections
  *          A java.File object referencing the kdv_unload.txt file.
  * 
  * @author Asus
  * 
  */
 public class Parser {
   private boolean pointsInit = false;
   private File nodes;
   private File connections;
   private double minX = 1000000;
   private double maxX = 0;
   private double minY = 100000000.0;
   private double maxY = 0;
   
   // the constructor takes 2 arguments, the kdv.node_unload.txt file and the
   // kdv_unload.txt file.
   public Parser(File nodeFile, File connectionFile) {
     nodes = nodeFile;
     connections = connectionFile;
   }
 
   /**
    * The parsePoints() function runs through the "nodes" file, and creates a new
    * Point object for each line, and puts it into the Array to be returned.
    * 
    * @return An array of all Point objects from the information given in the
    *         kdv_node_unload.txt file.
    */
   public HashMap<Integer, Point> parsePoints() {
     HashMap<Integer, Point> tmp = new HashMap<Integer, Point>();
     try {
       BufferedReader input = new BufferedReader(new FileReader(nodes));
       try {
         String line = null;
         /*
          * readLine is a bit quirky : it returns the content of a line MINUS the
          * newline. it returns null only for the END of the stream. it returns
          * an empty String if two newlines appear in a row.
          */
         input.readLine();
         while ((line = input.readLine()) != null) {
           Point p = createPoint(line);
           if (p.getX()>maxX){
             maxX=p.getX();
           }
           if (p.getX()<minX){
             minX=p.getX();
           }
           if (p.getY()>maxY){
             maxY=p.getY();
           }
           if (p.getY()<minY){
             minY=p.getY();
           }
           tmp.put(p.getID(), p);
         }
       } finally {
         input.close();
       }
     } catch (IOException ex) {
       ex.printStackTrace();
     }
     pointsInit = true;
     return tmp;
   }
 
   /**
    * The parseRoads() function runs through the "connections" file, and creates
    * a new Road object for each line, and puts it into the Array to be returned.
    * 
    * @return An array of all Road objects from the information given in the
    *         kdv_unload.txt file.
    */
   public DimensionalTree<Double, Integer, Road> parseRoads(HashMap<Integer, Point> points) {
    DimensionalTree<Double, Integer, Road> tmp = new DimensionalTree<Double, Integer, Road>(Road[].class);    
      try(BufferedReader input = new BufferedReader(new FileReader(connections))) {
         String line = null;
         /*
          * readLine is a bit quirky : it returns the content of a line MINUS the
          * newline. it returns null only for the END of the stream. it returns
          * an empty String if two newlines appear in a row.
          */
         input.readLine();
         while ((line = input.readLine()) != null) {
           String[] split = splitRoadInput(line);
           if (split.length == 4) {
             Double xS = points.get(Integer.parseInt(split[0])).getX();
             Double yS = points.get(Integer.parseInt(split[0])).getY();
             Double xE = points.get(Integer.parseInt(split[1])).getX();
             Double yE = points.get(Integer.parseInt(split[1])).getY();
             Integer type = Integer.parseInt(split[3]);
             Road value = new Road(Integer.parseInt(split[0]), Integer.parseInt(split[1]), split[2], RoadType.getById(type));
             tmp.insert(xS, yS, type, value);
             tmp.insert(xE, yE, type, value);
           }
        } 
     } catch (IOException ex) {
       ex.printStackTrace();
     }
     return tmp;
   }
 
   // Creates a point, to be put in the array and parsed.
   /**
    * createPoint splits a line from the kdv_node_unload.txt document and then
    * creates a Point object from the information in the string.
    * 
    * @param input
    *          A line from the kdv_node_unload.txt document.
    * @return A Point object containing the information from the line.
    */
   private static Point createPoint(String input) {
     Point tmp;
     String[] inputSplit = input.split(",");
     tmp = new Point(Integer.parseInt(inputSplit[2]),
         Double.parseDouble(inputSplit[3]), Double.parseDouble(inputSplit[4]));
 
     return tmp;
   }
 
   /**
    * createPoint splits a line from the kdv_unload.txt document and then creates
    * a Road object from the information in the string.
    * 
    * @param input
    *          A line from the kdv_unload.txt document.
    * @return A Road object containing the information from the line.
    */
   private static String[] splitRoadInput(String input) {
     String[] tmp = new String[4];
 
     String[] inputSplit = input.split(",");
     if (Integer.parseInt(inputSplit[0]) != Integer.parseInt(inputSplit[1])) {
 
       tmp[0] = inputSplit[0];
       tmp[1] = inputSplit[1];
       tmp[2] = inputSplit[6];
       tmp[3] = inputSplit[5];
 
       return tmp;
     } else return new String[0];
     
   }
   /**
    * Returns an array containing 4 values, representing the smallest and largest x and y coordinates of all Points.
    * [0]=minX
    * [1]=maxX
    * [2]=minY
    * [3]=maxY
    * 
    * CAN ONLY BE CALLED IF THE PARSEPOINTS() FUNCTION HAS ALREADY BEEN CALLED!
    * @return A double array of size 4.
    */
   public double[] getMinMaxValues() {
     if(!pointsInit) throw new DataNotInitializedException(); //TODO Checks if data has been initializes (parsed)
     double[] tmp = new double[4];
     tmp[MinMax.MINX.id()]=minX;
     tmp[MinMax.MAXX.id()]=maxX;
     tmp[MinMax.MINY.id()]=minY;
     tmp[MinMax.MAXY.id()]=maxY;
     return tmp;
   }
 
 }
