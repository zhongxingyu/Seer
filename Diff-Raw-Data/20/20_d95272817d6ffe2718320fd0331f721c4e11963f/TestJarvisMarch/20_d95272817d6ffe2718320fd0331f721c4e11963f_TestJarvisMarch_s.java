 package test.GraphAlgorithms.ConvexHull;
 import java.awt.geom.Point2D;
 import java.util.*;
 import main.GraphAlgorithms.ConvexHull.JarvisMarch;
 
 public class TestJarvisMarch {
 	public static boolean success;
 	
 	public static void main(String[] args) {
 		success = true;
 		
 		testOne(true);
 		testTwo(true);
 		testThree(true);
 		
 		if (!success) {
 			System.out.println("FAILED: TestJarvisMarch");
 			System.exit(1);
 		}
 		else {
 			System.exit(0);
 		}
 	}
 	
 	public static void testOne(boolean printDebug) {
 		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
 		
 		points.add(new Point2D.Double(4, 3));
 		points.add(new Point2D.Double(-5, 0));
 		points.add(new Point2D.Double(-5, 2));
 		points.add(new Point2D.Double(-3, 1));
 		points.add(new Point2D.Double(-1, 4));
 		points.add(new Point2D.Double(-1, -4));
 		points.add(new Point2D.Double(0, -3));
 		points.add(new Point2D.Double(1, 3));
 		points.add(new Point2D.Double(2, 1));
 		points.add(new Point2D.Double(3, 4));
 		
 		points.add(new Point2D.Double(5, -2));
 		points.add(new Point2D.Double(5, 1));
 		
 		Collections.shuffle(points);
 		
 		JarvisMarch jm = new JarvisMarch();
 		ArrayList<Point2D.Double> answer = jm.doJarvis(points);
 		
 		if (answer.size() != 8) {
 			success = false;
 			return;
 		}
 	}
 	
 	public static void testTwo(boolean printDebug) {
 		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
 		
 		points.add(new Point2D.Double(0, 0));
 		points.add(new Point2D.Double(1, 1));
 		points.add(new Point2D.Double(5, 1));
 		points.add(new Point2D.Double(3, 2));
 		points.add(new Point2D.Double(2, 3));
 		points.add(new Point2D.Double(4, 3));
 		points.add(new Point2D.Double(1, 4));
 		points.add(new Point2D.Double(6, 4));
 		points.add(new Point2D.Double(3, 5));
 		points.add(new Point2D.Double(8, 5));
 		points.add(new Point2D.Double(4, 7));
 		
 		Collections.shuffle(points);
 		
 		JarvisMarch jm = new JarvisMarch();
 		ArrayList<Point2D.Double> answer = jm.doJarvis(points);
 		
 		if (answer.size() != 5) {
 			success = false;
 			return;
 		}
 	}
 	
 	public static void testThree(boolean printDebug) {
 		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
 		
 		points.add(new Point2D.Double(0, 0));
 		points.add(new Point2D.Double(1, 1));
 		points.add(new Point2D.Double(2, 2));
 		points.add(new Point2D.Double(-1, -1));
 		points.add(new Point2D.Double(-2, -2));
 		points.add(new Point2D.Double(1, -1));
 		points.add(new Point2D.Double(-1, 1));
 		points.add(new Point2D.Double(-2, 2));
 		points.add(new Point2D.Double(2, -2));
 		
 		//right
		points.add(new Point2D.Double(3, -3));
 		points.add(new Point2D.Double(3, -2));
		points.add(new Point2D.Double(3, 3));
 		//top
 		points.add(new Point2D.Double(3, 3));
 		points.add(new Point2D.Double(1, 3));
 		points.add(new Point2D.Double(-3, 3));
 		//bot
 		points.add(new Point2D.Double(3, -3));
 		points.add(new Point2D.Double(0, -3));
 		points.add(new Point2D.Double(-3, -3));
 		//left
		points.add(new Point2D.Double(-3, -3));
 		points.add(new Point2D.Double(-3, -2));
		points.add(new Point2D.Double(-3, 3));
 		
 		Collections.shuffle(points);
 		
 		JarvisMarch jm = new JarvisMarch();
 		ArrayList<Point2D.Double> answer = jm.doJarvis(points);
 		
 		if (answer.size() != 8) {
 			success = false;
 			return;
 		}
 	}
 }
