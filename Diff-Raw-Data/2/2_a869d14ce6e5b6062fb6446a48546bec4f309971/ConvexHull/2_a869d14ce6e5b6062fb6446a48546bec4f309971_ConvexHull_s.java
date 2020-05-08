 package convexhull.main;
 
 import convexhull.algorithms.AklToussaintHeuristic;
 import convexhull.algorithms.Algorithm;
 import convexhull.algorithms.GiftWrapping;
 import convexhull.algorithms.GrahamScan;
 import convexhull.algorithms.QuickHull;
 import convexhull.datastructures.LinkedList;
 import convexhull.datastructures.LinkedListNode;
 import convexhull.graphics.PointPrinter;
 import java.awt.geom.Point2D;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.Scanner;
 import javax.swing.JFrame;
 
 /**
  *
  * @author Heikki Haapala
  */
 public class ConvexHull {
 
     private static long startTime;
 
     /**
      *
      * @param args[0] input filename
      * @param args[1] at or noat to choose whether to use the Akl-Toussaint
      * @param args[2] amount of iterations to do
      * @param args[3] algorithm chooser string (gift/quick/graham)
      * @param args[4] output filename, print to "print" to console or "noout" to
      * do nothing
      * @param args[5] draw or nodraw to draw points on screen
      */
     public static void main(String[] args) {
         LinkedList allPoints = null;
 
         Scanner in = new Scanner(System.in);
         String input;
         boolean ok = false;
 
         if (args.length >= 1) {
             input = args[0];
         } else {
             input = "";
         }
 
         while (!ok) {
             // parsing
             try {
                 allPoints = parseFile(input);
                 ok = true;
                 System.out.println("Points read from file: " + input);
                 System.out.println("Input: a list of " + allPoints.getLength() + " points.\n");
             } catch (Exception ex) {
                 System.out.println("Could not read input file. Filename was: \"" + input + "\"");
                 System.out.print("Input a filename to open: ");
                 input = in.nextLine();
             }
             System.out.println();
         }
 
         if (args.length >= 2) {
             input = args[1];
         } else {
             input = "";
         }
         ok = false;
 
         LinkedList aklPoints = allPoints;
 
         while (!ok) {
             // Akl-Toussaint heuristic
             if (input.equals("at")) {
                 System.out.println("Using Akl-Toussaint heuristic.");
                 Algorithm akltoussaint = new AklToussaintHeuristic();
                 startTimer();
                 aklPoints = akltoussaint.useAlgorithm(allPoints);
                 System.out.println("Akl-Toussaint heuristic ran in " + stopTimer() + "ms.");
                 ok = true;
             } else if (input.equals("noat")) {
                 System.out.println("Not using Akl-Toussaint heuristic.");
                 ok = true;
             } else {
                 System.out.println("Bad argument: \"" + input + "\"");
                 System.out.println("Valid arguments:");
                 System.out.println("at : use Akl-Toussaint heuristic.");
                 System.out.println("noat : don't use Akl-Toussaint heuristic.");
                 System.out.print("Input new argument: ");
                 input = in.nextLine();
             }
             System.out.println();
         }
 
         if (args.length >= 3) {
             input = args[2];
         } else {
             input = "";
         }
         ok = false;
 
         int iterations = 0;
 
         while (!ok) {
             try {
                 iterations = Integer.parseInt(input);
                 if (iterations < 1) {
                     throw new IllegalArgumentException();
                 }
                 ok = true;
             } catch (Exception e) {
                 System.out.println("Bad argument: \"" + input + "\"");
                 System.out.print("Input new argument: ");
                 input = in.nextLine();
             }
         }
 
         if (args.length >= 4) {
             input = args[3];
         } else {
             input = "";
         }
         ok = false;
 
         Algorithm algorithmToUse = null;
 
         // algorithm picking
         while (!ok) {
             if (input.equals("gift")) {
                 System.out.println("Using Gift Wrapping algorithm.");
                 algorithmToUse = new GiftWrapping();
                 ok = true;
             } else if (input.equals("quick")) {
                 System.out.println("Using QuickHull algorithm.");
                 algorithmToUse = new QuickHull();
                 ok = true;
             } else if (input.equals("graham")) {
                 System.out.println("Using Graham scan algorithm.");
                 algorithmToUse = new GrahamScan();
                 ok = true;
             } else {
                 System.out.println("Bad argument: \"" + input + "\"");
                 System.out.println("Valid arguments:");
                 System.out.println("gift : use Gift Wrapping algorithm.");
                 System.out.println("quick : use QuickHull algorithm.");
                 System.out.println("graham : use Graham scan algorithm.");
                 System.out.print("Input new argument: ");
                 input = in.nextLine();
             }
             System.out.println();
         }
         
         LinkedList hullPoints = aklPoints;
         
         System.out.println(iterations + " iterations.");
         startTimer();
         for (int i = 0; i < iterations; i++) {
             hullPoints = algorithmToUse.useAlgorithm(aklPoints);
         }
         double totalTime = stopTimer();
         System.out.println("Total run time: " + totalTime + " ms.");
         System.out.println("Average run time: " + (totalTime / iterations) + " ms.");
 
         System.out.println("Output: a list of " + hullPoints.getLength() + " points.");
         System.out.println();
 
         if (args.length >= 5) {
             input = args[4];
         } else {
             input = "";
         }
         ok = false;
 
         while (!ok) {
             if (input.equals("print")) {
                 System.out.println("Printing hull points to console (x y).");
                 LinkedListNode node = hullPoints.getHead();
                 while (node != null) {
                     Point2D.Double point = node.getPoint();
                     System.out.println(point.getX() + " " + point.getY());
                     node = node.getNext();
                 }
                 ok = true;
             } else if (input.equals("noout")) {
                 // do nothing
                 System.out.println("Not saving or printing points.");
                 ok = true;
             } else {
                 try {
                     saveToFile(input, hullPoints);
                     ok = true;
                     System.out.println("Points saved to file: " + input);
                 } catch (Exception ex) {
                     System.out.println("Could not write to output file. Filename was: \"" + input + "\"");
                     System.out.print("Input a new filename or print to print to console: ");
                     input = in.nextLine();
                 }
             }
             System.out.println();
         }
 
         if (args.length >= 6) {
             input = args[5];
         } else {
             input = "";
         }
         ok = false;
 
         while (!ok) {
             if (input.equals("draw")) {
                 System.out.println("Drawing points on screen.");
                 drawOnScreen(allPoints, aklPoints ,hullPoints);
                 ok = true;
             } else if (input.equals("nodraw")) {
                 System.out.println("Not drawing points on screen.");
                 ok = true;
             } else {
                 System.out.println("Bad argument: \"" + input + "\"");
                 System.out.println("Valid arguments:");
                 System.out.println("draw : draw points on screen.");
                 System.out.println("nodraw : don't draw points on screen.");
                 System.out.print("Input new argument: ");
                 input = in.nextLine();
             }
             System.out.println();
         }
     }
 
     /**
      *
      * @param filename
      * @return
      * @throws Exception
      */
     private static LinkedList parseFile(String filename) throws Exception {
         File file = new File(filename);
         LinkedList points = new LinkedList();
 
         // open file
         BufferedReader br = new BufferedReader(new FileReader(file));
 
         String line;
         while ((line = br.readLine()) != null) {
             // remove leading and trailing white spaces
             line = line.trim();
             // ignore lines that start with # or /
             if (line.charAt(0) != '#' && line.charAt(0) != '/') {
                 String[] split = line.split(" ");
                 double x = Double.parseDouble(split[0]);
                 double y = Double.parseDouble(split[1]);
                 Point2D.Double newPoint = new Point2D.Double(x, y);
                 // add to list
                 points.insert(newPoint);
             }
         }
         // close file
         br.close();
 
         return points;
     }
 
     /**
      *
      * @param filename
      * @param points
      * @throws Exception
      */
     private static void saveToFile(String filename, LinkedList points) throws Exception {
         File file = new File(filename);
 
         // open file
         BufferedWriter vw = new BufferedWriter(new FileWriter(file));
 
         // write to file
         LinkedListNode node = points.getHead();
         while (node != null) {
             Point2D.Double point = node.getPoint();
             vw.append(point.getX() + " " + point.getY());
             vw.newLine();
             node = node.getNext();
         }
 
         // close file
         vw.close();
     }
 
     /**
      *
      */
     private static void startTimer() {
         startTime = System.currentTimeMillis();
     }
 
     /**
      *
      * @return
      */
     private static long stopTimer() {
         long endTime = System.currentTimeMillis();
         return endTime - startTime;
     }
 
     private static void drawOnScreen(LinkedList allPoints, LinkedList aklPoints, LinkedList hullPoints) {
         JFrame frame = new JFrame("Points");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.add(new PointPrinter(allPoints, aklPoints, hullPoints));
         frame.setSize(800, 800);
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
     }
 }
