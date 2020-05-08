 /***************************************************************************
  * Name: Uros Slana
  * Email: urossla(at)gmail.com
  *
  * Compilation: javac ConvexHull.java
  * 
  * Dependencies: Point.java
  * 
  * Description: The algorithm finds the convex hull of a set of n points in O(n lg n) 
  *
  * % java ConvexHull set_of_points.txt NUMBER_OF_POINTS
  * 0.1 0.5
  * 1.5 9.3
  * 0.4 93.1
  * 112 39.3
  * 22  39.0
  * 19  0.45
  * ...
  *************************************************************************/
 
 import java.util.Arrays;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.LinkedList;
 import java.lang.System;
 import java.util.Scanner;
 
 public class ConvexHull {
 
 	private int N;
 	private Point[] polarSort;
 
 	public ConvexHull(Point[] points) {
 
 		int min = 0;;
 		N = points.length;
 		polarSort = new Point[N];
 
 		for (int i = 0; i < N; i++) {
 			polarSort[i]  = points[i];
 		}
 		
		Arrays.sort(polarSort, Point.Y_ORDER);
		Arrays.sort(polarSort, 1, N,  polarSort[0].POLAR_ORDER);
 	}
 	
 	public Iterable<Point> GrahamScan() {
 
 		LinkedList<Point> hull = new LinkedList<Point>();
 
 		int i = 1;
 		while (Point.CCW(polarSort[0], polarSort[i], polarSort[i+1]) == 0) {
 			i++;
 		}
 
 		hull.addFirst(polarSort[0]);
 		hull.addFirst(polarSort[i]);
 		hull.addFirst(polarSort[i+1]);
 
 		for (i = i+2; i < N; i++) {
 			while (Point.CCW(hull.get(1), hull.getFirst(), polarSort[i]) != -1) {
 				hull.remove();
 			}
 			hull.addFirst(polarSort[i]);
 		}
 
 		return hull;
 	}
 	
 	public static void main(String[] args) {
 
 		// java ConvexHull arg1 arg2
 		if (args.length < 2) {
 			System.out.println("Incorrect usage.");
 			return;
 		}
 		
 		int i = 0;
 		int n = Integer.parseInt(args[1]);
 		File fileP = new File(args[0]);
 		Point[] points = new Point[n];
 		Scanner scanPoint;
 		ConvexHull convexHull;
 
 		try {
 			scanPoint = new Scanner(fileP);
 			while(scanPoint.hasNextDouble()) {
 				double x = scanPoint.nextDouble();
 				double y = scanPoint.nextDouble();
 				points[i++] = new Point(x, y); 
 			}
 		}catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		}
 
 		convexHull = new ConvexHull(points);
 		for (Point p: convexHull.GrahamScan()) {
 			System.out.println(p);
 		}
 	}
 }
