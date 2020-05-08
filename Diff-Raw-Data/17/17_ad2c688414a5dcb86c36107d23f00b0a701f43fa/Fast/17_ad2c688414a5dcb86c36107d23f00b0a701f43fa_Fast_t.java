 import java.util.Arrays;
 import java.util.Vector;
 import java.util.HashSet;
 import java.util.Iterator;
 
 public class Fast {
     private static Point[] pointArray;
     private static HashSet<String> slopeChecker;
    
     private static final int POINTNUM = 3;
     public Fast() {
 
     }
     
     
     private static String slopeString(Point p, double slope) {
         return p.toString()+new Double(slope).toString();
     }
     
 
     
     
     private static void findPoints() {
         int j;
         
         slopeChecker = new HashSet<String>();
         Vector<Point> vector = new Vector<Point>();
         
         for (int i = 1; i <= pointArray.length - POINTNUM; i++) {
             
             Point[] sortArray = new Point[pointArray.length];
             // Copy the point array for sorting
             for (j = 0; j < pointArray.length; j++) {
                 sortArray[j] = pointArray[j];
             }
 
             Point base = pointArray[i-1];
             
             Arrays.sort(sortArray, i, pointArray.length,
                     base.SLOPE_ORDER);
 
             vector.add(sortArray[i]);
             double slope = base.slopeTo(sortArray[i]);
             double curSlope = slope;
             
            
             for (j = i + 1; j < pointArray.length; j++) {
                 curSlope = base.slopeTo(sortArray[j]);
                 String ps = slopeString(base, curSlope);
                //StdOut.println("Check "+ps);
                if (slopeChecker.contains(ps)) {
                    //StdOut.println("It is there !");
                     continue;
                }
                 if (slope == curSlope) {
                     vector.add(sortArray[j]);
                 }
                 else {
                     if (vector.size() >= POINTNUM) {
                        //int num = vector.size() + 1;
                         //StdOut.print(num+": ");
                         StdOut.print(base.toString()+" -> ");
                        printPoints(vector, slope);
                         base.drawTo(vector.elementAt(vector.size()-1));
 
                     }
                     slope = curSlope;
                     vector.clear();
                     vector.add(sortArray[j]);
                 }
                     
             }
             
             if (vector.size() >= POINTNUM) {
                //int num = vector.size() + 1;
                 //StdOut.print(num+": ");
                 StdOut.print(base.toString()+" -> ");
                 printPoints(vector, slope);
                 base.drawTo(vector.elementAt(vector.size()-1));
             }
             
             vector.clear();
         }
         
     }
     
     private static void printPoints(Vector<Point> vector, double slope) {
     
         Iterator<Point> iter = vector.iterator();
         
         while (iter.hasNext()) {
             Point point = iter.next();
             StdOut.print(point.toString());
             String ps = slopeString(point, slope);
             slopeChecker.add(ps);

             if (iter.hasNext())
                 StdOut.print(" -> ");
         }
         StdOut.println();
         
     }
     
     public static void main(String[] args) {
         if (args.length < 1)
             return;
         // rescale coordinates and turn on animation mode
         StdDraw.setXscale(0, 32768);
         StdDraw.setYscale(0, 32768);
         StdDraw.show(0);
         
         String filename = args[0];
         
         Point[] pArray;
         
         In in = new In(filename);
         int N = in.readInt();
         pArray = new Point[N];
         for (int i = 0; i < N; i++) {
             int x = in.readInt();
             int y = in.readInt();
             Point p = new Point(x, y);
             pArray[i] = p;
         }
 
         
         if (pArray == null || pArray.length < 4)
             return;
         
         Arrays.sort(pArray);
         
         pointArray = pArray;
                    
         findPoints();
         
 
         // display to screen all at once
         StdDraw.show(0);
     }
 }
