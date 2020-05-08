 package files;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import controller.Controller;
 import dataStructure.Connection;
 import dataStructure.ConnectionQuadTree;
 import dataStructure.Point;
 import dataStructure.RoadType;
 import dataStructure.TernarySearchTries;
 
 /**
  * 
  * @author Claus L. Henriksen clih@itu.dk
  * @author Phillip Hugmann Olesen
  * @author Morten Drescher Salling
  * @author Pacmans
  * @version 10. April 2012
  */
 public class FileLoaderConnectionOnly {
 
   private Controller controller = Controller.getInstance();
   private double xMin = 750000;
   private double xMax;
   private double yMin = 750000;
   private double yMax;
   private double Scale = 750;
   volatile private ConnectionQuadTree highwaysQT;
   volatile private ConnectionQuadTree expresswaysQT;
   volatile private ConnectionQuadTree primaryQT;
   volatile private ConnectionQuadTree secondaryQT;
   volatile private ConnectionQuadTree normalQT;
   volatile private ConnectionQuadTree smallQT;
   volatile private ConnectionQuadTree pathsQT;
   volatile private Connection[] connections;
   volatile private TernarySearchTries<Integer> tst;
   volatile private Point[] points = new Point[675902];
 
   public FileLoaderConnectionOnly() throws IOException {
     controller.setStatus("Loading data");
     initialise();
     loadPoints();
     loadConnections();
   }
 
   private void initialise() {
     highwaysQT = controller.getHighwaysQT();
     expresswaysQT = controller.getExpresswaysQT();
     primaryQT = controller.getPrimaryQT();
     secondaryQT = controller.getSecondaryQT();
     normalQT = controller.getNormalQT();
     smallQT = controller.getSmallQT();
     pathsQT = controller.getPathsQT();
     controller.setConnections(new Connection[812302]);
     connections = controller.getConnections();
     tst = controller.getTst();
   }
 
   private void loadConnections() throws IOException {
     Thread highways = new Thread(new FileLoaderThread("highways", points,
         connections, highwaysQT, tst));
     Thread expressways = new Thread(new FileLoaderThread("expressways", points,
         connections, expresswaysQT, tst));
     Thread primary = new Thread(new FileLoaderThread("primary", points,
         connections, primaryQT, tst));
     Thread secondary = new Thread(new FileLoaderThread("secondary", points,
         connections, secondaryQT, tst));
 
     highways.start();
     expressways.start();
     primary.start();
     secondary.start();
     try {
       highways.join();
       expressways.join();
       primary.join();
       secondary.join();
     } catch (InterruptedException e) {
     	Controller.catchException(e);
     }
     
     System.out.println("4 first qaudtrees done");
     controller.getGUI().setupMap();
     
     Thread normal = new Thread(new FileLoaderThread("normal", points,
         connections, normalQT, tst));
     Thread small = new Thread(new FileLoaderThread("small",
         points, connections, smallQT, tst));
     Thread paths = new Thread(new FileLoaderThread("paths", points,
         connections, pathsQT, tst));
 
     paths.start();
     try{
       normal.join();
       paths.join();
     }catch(Exception e){
     	Controller.catchException(e);
     }
     
     normal.start();
     try{
       normal.join();
     }catch(Exception e){
       Controller.catchException(e);
     }
     
     small.start();
     try{
       small.join();
     }catch(Exception e){
       Controller.catchException(e);
     }
    controller.setStatus("Data loaded");
   }
 
   /**
    * creates Points from file "kdv_node_unload" and calculates min and max
    * coordinates
    * 
    * @throws IOException
    */
   /**
    * creates Points from file "kdv_node_unload" and calculates min and max
    * coordinates
    * 
    * @throws IOException
    */
   private void loadPoints() throws IOException {
     // loads the file "kdv_node_unload"
     // File pointsFile = new File("./src/files/kdv_node_unload.txt");
     InputStream pointFile = getClass().getResourceAsStream(
         "kdv_node_unload.txt");
     BufferedReader pointInput = new BufferedReader(new InputStreamReader(
         pointFile));
 
     String line = null;
     double x, y;
     int index = -1;
     if (pointInput.ready()) { // if file is loaded
       while ((line = pointInput.readLine()) != null) {
         if (index >= 0) { // does nothing at the first line
           // creates the point
           String[] info = line.split(",");
           x = (Double.parseDouble(info[3]) / Scale);
           y = (Double.parseDouble(info[4]) / Scale);
           points[index] = new Point(index + 1, x, y);
 
           // sets max and min coordinates
           if (x < xMin)
             xMin = x;
           if (x > xMax)
             xMax = x;
           if (y < yMin)
             yMin = y;
           if (y > yMax)
             yMax = y;
         }
         index++;
       }
     }
     controller.setxMax(xMax);
     controller.setxMin(xMin);
     controller.setyMax(yMax);
     controller.setyMin(yMin);
   }
 
   public double getxMax() {
     return xMax;
   }
 
   public double getyMax() {
     return yMax;
   }
 
   public Connection[] getConnections() {
     return connections;
   }
 
   public double getxMin() {
     return xMin;
   }
 
   public double getyMin() {
     return yMin;
   }
 
   public ConnectionQuadTree getHighwaysQT() {
     return highwaysQT;
   }
 
   public ConnectionQuadTree getExpresswaysQT() {
     return expresswaysQT;
   }
 
   public ConnectionQuadTree getPrimaryQT() {
     return primaryQT;
   }
 
   public ConnectionQuadTree getSecondaryQT() {
     return secondaryQT;
   }
 
   public ConnectionQuadTree getNormalQT() {
     return normalQT;
   }
 
   public ConnectionQuadTree getTrailsStreetsQT() {
     return smallQT;
   }
 
   public ConnectionQuadTree getPaths() {
     return pathsQT;
   }
 
   public TernarySearchTries<Integer> getTST() {
     return tst;
   }
 }
