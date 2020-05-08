 package stars;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.GradientPaint;
 import java.awt.Graphics2D;
 import java.awt.geom.Ellipse2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.WritableRaster;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 import javax.imageio.ImageIO;
 
 public class Stars {
 
   private static int minIntensity = 64;
   private static String inputFileName = "stars.bmp";
   // right now these are very uneducated guesses...
   public static final int INTENSITY_THRESHOLD = 200;
   public static final int GRADIENT_THRESHOLD = 45;
 
   public static void main(String[] args) {
     //Variables
     int width, height, pixelTotal;
     BufferedImage constellationImg;
 
     ////////Reading Input Files//////// 
     File in = new File(inputFileName);
     BufferedImage img = null;
     try {
       img = ImageIO.read(in);
     } catch (IOException ioException) {
       System.out.println("Input File Could Not Be Found.");
       System.exit(0);
     }
     //Creating Constellations Map
     ColorModel cm = img.getColorModel();
     boolean alpha = cm.isAlphaPremultiplied();
     WritableRaster raster = img.copyData(null);
     constellationImg = new BufferedImage(cm, raster, alpha, null);
 
     //Gather basic file information
     width = img.getWidth();
     height = img.getHeight();
     pixelTotal = width * height;
 
     Map<Long, Node> nodes = new HashMap<Long, Node>();
     PriorityQueue<Edge> edges = new PriorityQueue<Edge>(width * height, new Comparator<Edge>() {
       public int compare(Edge e1, Edge e2) {
         return e1.weight - e2.weight;
       };
     });
     //Iterate through buffered image
     for (int x = 0; x < width; x++) {
       for (int y = 0; y < height; y++) {				
         //The first node is created on its current iteration otherwise all nodes are created in the node above them.
         Node n = null;
 
         //Create the first node
         if(x == 0 && y == 0){
           n = new Node(x, y, img.getRGB(x, y));
           //Hashmap (16 bits: x, 16 bits: y)
           nodes.put(n.getCoordinate(), n);
 
           Node right = new Node(x+1, y, img.getRGB(x+1, y));
           nodes.put(right.getCoordinate(), right);
           Edge e = new Edge(n, right, n.intensity + right.intensity);
           if (edgeNeeded(e))
             edges.add(e);
 
           Node down = new Node(x, y+1, img.getRGB(x, y+1));
           nodes.put(down.getCoordinate(), down);
           e = new Edge(n, right, n.intensity + down.intensity);
           if (edgeNeeded(e))
             edges.add(e);
 
         }
         //Checks the first row
         else if(y==0){
           //This will be used to make the edge nodes
           n = nodes.get(Node.makeCoordinate(x, y));
 
           //Check if we have anything on the right
           if(x+1 != width){
             //This could be moved in the previous if block
             //The first row will not have been created vs all other rows would have been.
             Node down = new Node(x, y+1, img.getRGB(x, y+1));
             nodes.put(down.getCoordinate(), down);
             Edge e = new Edge(n, down, n.intensity + down.intensity);
             if (edgeNeeded(e))
               edges.add(e);
 
             Node right = new Node(x+1, y, img.getRGB(x, y));
             nodes.put(right.getCoordinate(), right);
             e = new Edge(n, right, n.intensity + right.intensity);
             if (edgeNeeded(e))
               edges.add(e);
           }
           //If we are at the edge, don't add the right edge, duh.
           else{
             Node down = new Node(x, y+1, img.getRGB(x, y+1));
             nodes.put(down.getCoordinate(), down);
             Edge e = new Edge(n, down, n.intensity + down.intensity);
             if (edgeNeeded(e))
               edges.add(e);
           }
         }
         //Covers the bottom row
         else if(y + 1 == height){
           if(x+1 != width){
             n  = nodes.get(Node.makeCoordinate(x, y));
             Node right = new Node(x+1, y, img.getRGB(x, y));
             nodes.put(right.getCoordinate(), right);
             Edge e = new Edge(n, right, n.intensity + right.intensity);
             if (edgeNeeded(e))
               edges.add(e);
           }
         }
         //Covers the right most column
         else if(x + 1 == width){
           n  = nodes.get(Node.makeCoordinate(x, y));
           Node down = new Node(x, y+1, img.getRGB(x, y+1));
           nodes.put(down.getCoordinate(), down);
           Edge e = new Edge(n, down, n.intensity + down.intensity);
           if (edgeNeeded(e))
             edges.add(e);
 
         }
         //Creates all the non first, bottom, or right nodes and relationships
         else{
           n  = nodes.get(Node.makeCoordinate(x, y));
 
           Node right = new Node(x+1, y, img.getRGB(x, y));
           nodes.put(right.getCoordinate(), right);
           Edge e = new Edge(n, right, n.intensity + right.intensity);
           if (edgeNeeded(e))
             edges.add(e);
 
 
           Node down = new Node(x, y+1, img.getRGB(x, y+1));
           nodes.put(down.getCoordinate(), down);
           e = new Edge(n, down, n.intensity + down.intensity);
           if (edgeNeeded(e))
             edges.add(e);
         }
         System.out.println("Finished (" + x + ", " + y + ")");
       }
     }
 
     HashSet<Node> starNodes = new HashSet<Node>();
     int edgesSize = edges.size();
     // Kruskal's algorithm
     while (edges.size() > 0) {
       Edge e = edges.poll();
       starNodes.add(e.nodeA);
       starNodes.add(e.nodeB);
 
       if (e == null)
         continue; // wo, what happened here...
 
       if (UnionFind.find(e.nodeA) != UnionFind.find(e.nodeB)) {
         UnionFind.union(e.nodeA, e.nodeB);
       }
     }
 
     List<Star> stars = new ArrayList<Star>();
     // Go through all star nodes and create list of Stars (ie cluster of nodes)
     for (Node n : starNodes) {
       Star s = new Star(n);
 
       int i = stars.indexOf(s);
       // if star object is already created for this cluster
       if (i != -1)
         s = stars.get(i);
 
       s.addPixel(n);
 
       // if this is a brand new star
       if (i == -1) 
         stars.add(s);
     }
 
     System.out.println("Number of Stars: " + stars.size());
     System.out.println("Width: " + width + " Height: " + height);
    System.out.println("Amount of the Night Sky Representing Stars: " + Math.rint(( (double) (edgesSize)) / ((double)(width*height))*100) + "%");
     System.out.println("Average Star Size: " +  nodes.size() / stars.size());
     System.out.println("Total Pixels: " + width * height);
 
     //Color the nodes
     Graphics2D g2 = constellationImg.createGraphics();
     BasicStroke bs = new BasicStroke(2);
     g2.setStroke(bs);
     g2.setColor(Color.RED);
     for(Star s: stars){
       g2.setPaint(Color.red);
       g2.fill (new Ellipse2D.Double(s.getCenterX(), s.getCenterY(), s.getRadiusX(), s.getRadiusY()));
 
     }
     try {
       // retrieve image
       File outputfile = new File("constellation.bmp");
       ImageIO.write(constellationImg, "bmp", outputfile);
     } catch (IOException e) {
       System.out.println("Could not write output file.");
       System.exit(0);
     }
   }
 
   private static boolean edgeNeeded(Edge e) {
     // check that average intensity is high enough to be a star
     if ((e.nodeA.intensity + e.nodeB.intensity) / 2 < INTENSITY_THRESHOLD)
       return false;
 
     // check that difference between pixels isn't high indicating a star edge
     if (Math.abs(e.nodeA.intensity - e.nodeB.intensity) > GRADIENT_THRESHOLD)
       return false;
 
     return true;
   }
 }
