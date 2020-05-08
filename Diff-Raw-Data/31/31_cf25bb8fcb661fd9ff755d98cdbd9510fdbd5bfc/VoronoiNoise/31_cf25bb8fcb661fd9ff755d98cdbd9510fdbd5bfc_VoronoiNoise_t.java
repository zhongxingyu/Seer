 package com.xpansive.bukkit.worldgen;
 
 
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Stack;
 
 class VoronoiNoise {
 	private ArrayList<Point> voronoiPoints = new ArrayList<Point>();
 	private Random random;
 	private HashMap<Point, Boolean> generatedChunks = new HashMap<Point, Boolean>();
 
 	public VoronoiNoise(Random r) {
 		random = r;
 	}
 
 	public void GenChunks(int x, int y, int width, int height, int numPoints) {
 		GenVoronoi(width, height, numPoints, x - width, y - height);
 		GenVoronoi(width, height, numPoints, x, y - height);
 		GenVoronoi(width, height, numPoints, x + width, y - height);
 
 		GenVoronoi(width, height, numPoints, x - width, y);
 		GenVoronoi(width, height, numPoints, x, y);
 		GenVoronoi(width, height, numPoints, x + width, y);
 
 		GenVoronoi(width, height, numPoints, x - width, y + height);
 		GenVoronoi(width, height, numPoints, x, y + height);
 		GenVoronoi(width, height, numPoints, x + width, y + height);
 	}
 
 	private void GenVoronoi(int width, int height, int numPoints, int xOff,
 			int yOff) {
 		Point p = new Point(xOff / width, yOff / height);
 		if (generatedChunks.containsKey(p)) // already generated
 			return;
 
 		generatedChunks.put(p, true);
 
 		for (int index = 0; index < numPoints; index++) {
 			voronoiPoints.add(new Point(random(xOff, xOff + width), random(yOff, yOff + height)));
 		}
 		// Collections.sort(voronoiPoints);
 	}
 
 	private Stack<Point> closestPoints = new Stack<Point>();
 	private boolean forceAllPoints;
 	private boolean justCalculated;
 	private int lastLowestDistance;
 
 	private int numCalcAll, numCalcClosest;
 	
 	private int random(int min, int max) {
 		return min + (int)(Math.random() * ((max - min) + 1));
 	}
 
 	public int Voronoi(int x, int y) {
 		int lowestDistance = Integer.MAX_VALUE;
		if (true || forceAllPoints) {//(lastLowestDistance < 20 &&!justCalculated) || y <= 2 ||
 			numCalcAll++;
 			forceAllPoints = false;
 			closestPoints.clear();
 
 			for (Point point : voronoiPoints) {
 				int temp = Distance(new Point(x, y), point);
 
 				if (temp > lowestDistance)
 					continue;
 				lowestDistance = temp;
 				closestPoints.push(point);
 			}
 
 			justCalculated = true; // prevent going through the whole list again
 									// for the next pixel
 			lastLowestDistance = 0;
 			return lowestDistance;
 		}
 		numCalcClosest++;
 
 		for (Point point : closestPoints) {
 			int temp = Distance(new Point(x, y), point);
 
 			if (temp > lowestDistance)
 				continue;
 
 			lowestDistance = temp;
 		}
 		justCalculated = !justCalculated;
 		lastLowestDistance = lowestDistance;
 		return lowestDistance;
 	}
 
 	private static int Distance(Point p1, Point p2) {
 		int x = (int) (p2.x - p1.x), y = (int) (p2.y - p1.y);
 		// return (int)Math.Sqrt(x * x + y * y); //Length
 		return x * x + y * y; // Length2
 		// return (int)(Math.Abs(x) + Math.Abs(y)); //Manhattan
 		// return (int)Math.Pow(x * x * x * x + y * y * y * y, 0.25);
 		// //Minkowski4
 	}
 }
