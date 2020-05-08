 package test.GraphAlgorithms.ConvexHull;
 import java.util.*;
 import main.GraphAlgorithms.ConvexHull.GrahamScan;
 
 public class TestGrahamScan {
 	public static boolean success = false;
 	
 	public static void main(String[] args) {
 		success = true;
 		
		//testOne(true);
		//testTwo(true);
 		testThree(true);
 		
 		if (!success) {
 			System.out.println("FAILED: TestGrahamScan");
 			System.exit(1);
 		}
 		else {
 			System.exit(0);
 		}
 	}
 	
 	public static void testOne(boolean printDebug) {
 		GrahamScan gs = new GrahamScan();
 		ArrayList<GrahamScan.Point> points = new ArrayList<GrahamScan.Point>();
 		
 		
 		points.add(gs.new Point(4, 3));
 		points.add(gs.new Point(-5, 0));
 		points.add(gs.new Point(-5, 2));
 		points.add(gs.new Point(-3, 1));
 		points.add(gs.new Point(-1, 4));
 		points.add(gs.new Point(-1, -4));
 		points.add(gs.new Point(0, -3));
 		points.add(gs.new Point(1, 3));
 		points.add(gs.new Point(2, 1));
 		points.add(gs.new Point(3, 4));
 		
 		points.add(gs.new Point(5, -2));
 		points.add(gs.new Point(5, 1));
 		
 		//Collections.shuffle(points);
 		
 		ArrayList<GrahamScan.Point> answer = gs.doGraham(points);
 		
 		if (answer.size() != 8) {
 			success = false;
 			return;
 		}
 	}
 	
 	public static void testTwo(boolean printDebug) {
 		ArrayList<GrahamScan.Point> points = new ArrayList<GrahamScan.Point>();
 		GrahamScan gs = new GrahamScan();
 		
 		points.add(gs.new Point(0, 0));
 		points.add(gs.new Point(1, 1));
 		points.add(gs.new Point(5, 1));
 		points.add(gs.new Point(3, 2));
 		points.add(gs.new Point(2, 3));
 		points.add(gs.new Point(4, 3));
 		points.add(gs.new Point(1, 4));
 		points.add(gs.new Point(6, 4));
 		points.add(gs.new Point(3, 5));
 		points.add(gs.new Point(8, 5));
 		points.add(gs.new Point(4, 7));
 		
 		//Collections.shuffle(points);
 		
 		ArrayList<GrahamScan.Point> answer = gs.doGraham(points);
 		
 		if (answer.size() != 5) {
 			success = false;
 			return;
 		}
 	}
 	
 	public static void testThree(boolean printDebug) {
 		ArrayList<GrahamScan.Point> points = new ArrayList<GrahamScan.Point>();
 		GrahamScan gs = new GrahamScan();
 		
 		points.add(gs.new Point(0, 0));
 		points.add(gs.new Point(1, 1));
 		points.add(gs.new Point(2, 2));
 		points.add(gs.new Point(-1, -1));
 		points.add(gs.new Point(-2, -2));
 		points.add(gs.new Point(1, -1));
 		points.add(gs.new Point(-1, 1));
 		points.add(gs.new Point(-2, 2));
 		points.add(gs.new Point(2, -2));
 		
 		//right
 		points.add(gs.new Point(3, -1));
 		//top
 		points.add(gs.new Point(3, 3));
 		points.add(gs.new Point(1, 3));
 		points.add(gs.new Point(-3, 3));
 		//bot
 		points.add(gs.new Point(3, -3));
 		points.add(gs.new Point(0, -3));
 		points.add(gs.new Point(-3, -3));
 		//left
 		points.add(gs.new Point(-3, -2));
 		
 		//Collections.shuffle(points);
 		
 		ArrayList<GrahamScan.Point> answer = gs.doGraham(points);
 		
 		if (answer.size() != 8) {
 			success = false;
 			
 			return;
 		}
 	}
 }
