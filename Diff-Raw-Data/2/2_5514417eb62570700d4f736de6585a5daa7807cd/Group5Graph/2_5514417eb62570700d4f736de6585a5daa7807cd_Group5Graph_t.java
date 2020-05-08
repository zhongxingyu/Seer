 package mosquito.g0;
 
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Point2D.Double;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 
 import org.apache.log4j.Logger;
 
 import mosquito.sim.Collector;
 import mosquito.sim.Light;
 import mosquito.sim.MoveableLight;
 
 public class Group5Graph extends mosquito.sim.Player  {
 
 	private int numLights;
 	private Point2D.Double lastLight;
 	private static final double LIGHTRADIUS = 10.0;
 	private static final double BOARDSIZE = 100.0;
 	private Point2D collectorLocation;
 	private MoveableLight collectorLight;
 	private ArrayList<Point2D.Double> vertices = new ArrayList<Point2D.Double> ();
 	private HashMap<Point2D.Double, ArrayList<Point2D.Double>> graph = new HashMap<Point2D.Double, ArrayList<Point2D.Double>>();
 	private HashMap<Point2D.Double, Point2D.Double> edges = new HashMap<Point2D.Double, Point2D.Double> ();
 	private HashMap<Point2D.Double, ArrayList<Point2D.Double>> mst = new HashMap<Point2D.Double, ArrayList<Point2D.Double>> ();
 	private HashMap<Light, ArrayList<Point2D>> paths = new HashMap<Light, ArrayList<Point2D>>();
 	private HashMap<Light, ArrayList<Point2D.Double>> astarPaths = new HashMap<Light, ArrayList<Point2D.Double>>();
 	private HashMap<MoveableLight, Point2D.Double> greedyLights = new HashMap<MoveableLight, Point2D.Double> ();
 	
 	private Logger logger =  Logger.getLogger(this.getClass()); 
 	private AStar astar;
 
 	
 	@Override
 	public String getName() {
 		return "G5Player Graph";
 	}
 	
 	private Set<Light> lights;
 	private Set<Line2D> walls;
 	private Set<Line2D> extendedwalls;	
 	
 	@Override
 	public ArrayList<Line2D> startNewGame(Set<Line2D> walls, int numLights) {
 		logger.trace("logging works");
 		this.numLights = numLights;
 		this.walls = new HashSet<Line2D>();
 		this.extendedwalls = new HashSet<Line2D>();
 		
 		for (Line2D w: walls) {
 			ArrayList<Line2D> extended = extend(w);
 			this.extendedwalls.addAll(extended);
 		}
 		
 		this.walls = walls;
 		
 		this.collectorLocation = new Point2D.Double(95,95);
 		this.astar = new AStar(this.walls);
 		
 		ArrayList<Line2D> lines = new ArrayList<Line2D>();
 		Line2D line = new Line2D.Double(30, 30, 80, 80);
 		lines.add(line);
 		return lines;
 	}
 	
 	
 	private static boolean withinLightRadius(Point2D startPoint, Point2D testPoint) {
		return (startPoint.distance(testPoint)) <= (2 * LIGHTRADIUS);
 	
 	}
 	
 	private boolean isValidDestination(Point2D point) {
 	boolean valid = true;	
 	// check all current light positions and current light locations as valid
 	
 	for (Light l: lights) {
 		MoveableLight ml = (MoveableLight)l;
 		Point2D currentLocation = ml.getLocation();
 		if (withinLightRadius(currentLocation, point)) {
 	        valid = false; 
 			break;
 		}
 		
 		if (greedyLights.containsKey(ml)) {
 			Point2D currentDestination = greedyLights.get(ml);
 			if (withinLightRadius(currentDestination, point)) {
 				valid = false;
 				break;
 			   }
 		    }
 		}
 
 	
 		return valid;
 	}
 	
 	private Point2D greedyLocation(int [][] board) {
 		Point2D.Double location = new Point2D.Double(50,50);
 		Point2D.Double validLocation = null;
 		int max = 0;
 		int validmax = 0;
 		
 		for(int i = 0; i < 100; i+=10) {
 			for(int j = 0; j < 100; j+=10) {
 				//reset the sum for this chunk
 				int sum = 0;
 				for (int x = 0; x < 10; x++){
 					for(int y= 0; y < 10; y++)
 					{
 						sum += board[i+x][j+y];
 					}
 				}
 				
 				if(sum > max) {
 					location = new Point2D.Double(i + 5, j + 5);
 					max = sum;
 				}
 				
 				if (sum > validmax && isValidDestination(location)) {
 					validLocation = new Point2D.Double(i+5,j+5);
 					validmax = sum;
 				}
 			}
 		}
 		
 		// if we haven't found a valid location, pick the max populated location
 		if (validLocation == null) {
 			validLocation = location;
 		}
 		
 		return notOnWall(validLocation);
 	}
 	
 	
 	private ArrayList<Line2D> extend (Line2D startLine) {
 		ArrayList<Line2D> extended = new ArrayList<Line2D>();
 		Point2D p1 = startLine.getP1();
 		Point2D p2 = startLine.getP2();
 		double extension = 5;
 		double verticalMove = 5;
 		double deltaX = p2.getX() - p1.getX();
 		double deltaY = p2.getY() - p1.getY();
 		
 		boolean horizontal = (deltaY == 0);
 		boolean vertical = (deltaX == 0);
 		
 		double slope = 0; 
 		double intercept = 0;	
 		if (vertical) { 
 			double vxInside = Math.max(1, p1.getX()- verticalMove);
 			double vxOutside = Math.min(BOARDSIZE - 1, p1.getX() + verticalMove);
 			
 			double v1y = Math.max(1, p1.getY() - extension);
 			double v2y = Math.min(BOARDSIZE - 1, p2.getY() + extension);
 			
 			Point2D insideStart = new Point2D.Double(vxInside, v1y);
 			Point2D insideEnd = new Point2D.Double(vxInside, v2y);
 			
 			Point2D outsideStart = new Point2D.Double(vxOutside, v1y);
 			Point2D outsideEnd = new Point2D.Double(vxOutside, v2y);
 			
 			extended.add(new Line2D.Double(insideStart, insideEnd));
 			extended.add(new Line2D.Double(insideStart, outsideStart));
 			extended.add(new Line2D.Double(insideEnd, outsideEnd));
 			extended.add(new Line2D.Double(outsideStart, outsideEnd));
 		}
 		
 		else {
 			slope = deltaY/deltaX;
 			intercept = p2.getY() - slope * p2.getX();
 			double e1x = Math.max(1, p1.getX() - extension);
 			double e1y = e1x * slope + intercept;
 			e1y = Math.max(1, e1y);
 			e1y = Math.min(BOARDSIZE -1 , e1y);
 			
 			Point2D e1 = new Point2D.Double(e1x, e1y);	
 			
 			double e2x = Math.min(BOARDSIZE -1, p2.getX() + extension);
 			double e2y = e2x * slope + intercept;
 			e2y = Math.max(1, e2y);
 			e2y = Math.min(BOARDSIZE -1 , e2y);
 			
 			Point2D e2 = new Point2D.Double(e2x, e2y);
 			extended.add(new Line2D.Double(e1, e2));
 		}
 		
 		return extended;
 	}
 	
 	
 	private static HashMap<MoveableLight, ArrayList<Point2D.Double>> history = new HashMap<MoveableLight, ArrayList<Point2D.Double>> ();
 
 	private static boolean stuck (MoveableLight ml, Point2D.Double currentPosition){
 			if(history.containsKey(ml))
 			{
 				ArrayList<Point2D.Double> h = history.get(ml);
 				h.add(currentPosition);
 
 				if(h.size() >= 3 && (h.get(h.size()-3).equals(h.get(h.size()-2)) && h.get(h.size()-2).equals(h.get(h.size()-1)))){
 					return true;
 				}
 				
 				history.put(ml, h);
 			}
 			else {
 				ArrayList<Point2D.Double> h = new ArrayList<Point2D.Double>();
 				h.add(currentPosition);
 				history.put(ml, h);
 			}
 			return false;
 		}
 	
 	
 	private void computePaths() {
 		for (Light l : lights) {
 			Point2D.Double u = (Point2D.Double)l.getLocation();
 			LinkedList<Point2D> queue = new LinkedList<Point2D>();
 			queue.push(u);
 			ArrayList <Point2D> path = new ArrayList<Point2D>();
 			path.add(u);
 			HashSet<Point2D> visited = new HashSet<Point2D>();
 			
 			
 			while (queue.isEmpty() == false && u.equals(collectorLocation) == false) {
 				u = (Point2D.Double)queue.pop();
 				visited.add(u);
 				ArrayList<Point2D.Double> adjacent = graph.get(u);
 		
 				for (Point2D.Double v:adjacent) {
 					if (visited.contains(v) == false) {
 						queue.push(v);
 						Point2D.Double lastPoint = (Point2D.Double)path.get(path.size() - 1);
 						boolean intersects = isObstructed(lastPoint, v);
 						if (!intersects) {
 							path.add(v);
 							break;
 						}
 					}
 				}
 			}
 			
 			path.add(collectorLocation);
 			paths.put(l, path);
 		}
 	}
 	
 
 	
 	private void buildGraph() {
 		//populate vertices
 		for (int i = 0; i < 100; i = i+20) {
 			for (int j = 0; j < 100; j = j+20) {
 				Point2D.Double newPoint = new Point2D.Double(i + 5, j + 5);
 				vertices.add(newPoint);
 			}
 		}
 		
 		// populate edges based on adjacency on the board
 		for(Point2D.Double s:vertices) {
 			ArrayList<Point2D.Double> adjacent = new ArrayList<Point2D.Double>();
 			
 			// LEFT
 			if (s.getX() - 20 >=0) {
 				Point2D.Double left = new Point2D.Double(s.getX() - 20, s.getY());
 				if (vertices.contains(left)) {
 					adjacent.add(left);
 				}
 			}
 			
 			// RIGHT
 			if (s.getX() + 20 <= BOARDSIZE) {
 				Point2D.Double right = new Point2D.Double(s.getX() + 20, s.getY());	
 				if (vertices.contains(right)) {
 					adjacent.add(right);
 				}
 			}
 			
 			// UP
 			if (s.getY() - 20 >=0) {
 				Point2D.Double up = new Point2D.Double(s.getX(), s.getY() - 20);
 				if (vertices.contains(up)) {
 					adjacent.add(up);
 				}
 			}
 			
 			// DOWN
 			if (s.getY() + 20 <= BOARDSIZE) {
 				Point2D.Double down = new Point2D.Double(s.getX(), s.getY() + 20);
 				if (vertices.contains(down)) {
 					adjacent.add(down);
 				}
 			}
 			
 			for(int i = 0; i < adjacent.size(); i++) {
 				Point2D.Double t = adjacent.get(i);
 				Iterator<Line2D> wallIterator = walls.iterator();
 				
 				while(wallIterator.hasNext()) {
 					Line2D w = wallIterator.next();
 					if (w.intersectsLine(new Line2D.Double(s,t))) {
 						adjacent.remove(t);
 					}
 				}
 			}
 			
 			graph.put(s, adjacent);
 		}
 	}
 	
 	
 	
 	/*
 	 *  computes a zig zag path throughout the board
 	 */
 	private void zigZagPaths() {
 		ArrayList<Point2D> zigzag = zigZagPath();
 		int pathSize = zigzag.size()/(numLights - 1);
 		for (Light l: lights) {
 			int index = 0;
 			ArrayList<Point2D> nextPath = new ArrayList<Point2D>();
 			while(zigzag.get(index).equals(l.getLocation()) == false) {
 				index++;
 			}
 			
 			for (int i = 0; i < pathSize; i++) {
 				nextPath.add(zigzag.get(index + i));
 			}
 			
 			nextPath.add(collectorLocation);
 			paths.put(l, nextPath);
 		}
 	}
 	
 	/*
 	 * returns true if the distance from point to line is < 0.01 and false otherwise
 	 */
 	private boolean isOnLine(Line2D line, Point2D point) {
 		boolean onLine = false;
 	    double distance =  line.ptLineDist(point);
 		onLine = (Math.abs(distance) < 1);
 		return onLine;
 	}
 	
 	
 	private boolean intersectsWall(Point2D.Double point) {
 		boolean intersects = false;
 		for (Line2D w: walls) {
 			if (isOnLine(w, point)) {
 				intersects = true;
 				break;
 			}
 		}
 		
 		return intersects;
 	}
 	
 	private Point2D notOnWall(Point2D.Double point) {
 		Point2D freepoint = point;
 		for (Line2D w : walls) {
 			if (isOnLine(w, freepoint)) {
 				if (intersectsWall(new Point2D.Double(point.getX() - 1, point.getY())) == false) {
 					freepoint = new Point2D.Double(point.getX() - 1, point.getY()); 
 					break;
 				}
 				else if (intersectsWall(new Point2D.Double(point.getX() + 1, point.getY())) == false) {
 					freepoint = new Point2D.Double(point.getX() + 1, point.getY());
 					break;
 				}
 				else if (intersectsWall(new Point2D.Double(point.getX(), point.getY() - 1)) == false) {
 					freepoint = new Point2D.Double(point.getX(), point.getY() - 1);
 					break;
 				}
 				else if (intersectsWall(new Point2D.Double(point.getX(), point.getY() + 1)) == false) {
 					freepoint = new Point2D.Double(point.getX(), point.getY() + 1);
 					break;
 				}
 				else {
 					Random rand = new Random();
 					int direction = rand.nextInt(4);
 					switch (direction) {
 					case 0:
 						if (freepoint.getX() > 1) {
 							freepoint = notOnWall(new Point2D.Double(freepoint.getX() - 1, freepoint.getY()));
 						}
 						else 
 							freepoint = notOnWall(new Point2D.Double(freepoint.getX() + 1, freepoint.getY()));
 						break;
 					case 1:
 						if (freepoint.getX() < BOARDSIZE) {
 							freepoint = notOnWall(new Point2D.Double(freepoint.getX() + 1, freepoint.getY()));
 						}
 						else 
 							freepoint = notOnWall(new Point2D.Double(freepoint.getX() - 1, freepoint.getY()));
 						break;
 					case 2:
 						if (freepoint.getY() > 1) {
 							freepoint = notOnWall(new Point2D.Double(freepoint.getX(), freepoint.getY() - 1));
 						}
 						else 
 							freepoint = notOnWall(new Point2D.Double(freepoint.getX(), freepoint.getY() + 1));
 						break;
 					case 3:
 						if (freepoint.getY() < BOARDSIZE) {
 							freepoint = notOnWall(new Point2D.Double(freepoint.getX(), freepoint.getY() + 1));
 						}
 						else 
 							freepoint = notOnWall(new Point2D.Double(freepoint.getX(), freepoint.getY() - 1));
 						break;
 					default:
 						if (freepoint.getY() < BOARDSIZE) {
 							freepoint = notOnWall(new Point2D.Double(freepoint.getX(), freepoint.getY() + 1));
 						}
 						else 
 							freepoint = notOnWall(new Point2D.Double(freepoint.getX(), freepoint.getY() - 1));
 						break;
 					}
 					
 				}
 			}
 		}
 		
 		return freepoint;
 	}
 	
 	private ArrayList<Point2D> zigZagPath() {
 		ArrayList<Point2D> path = new ArrayList<Point2D>();
 		boolean movingRight = true;
 		for (int i = 5; i < BOARDSIZE; i+= 20) {
 			int j = (movingRight) ? 5 : 85;
 			int step = (movingRight) ? 20 : -20;
 			while (j < BOARDSIZE && j > 0) {
 				Point2D.Double nextPoint = new Point2D.Double(i, j);
 				path.add(notOnWall(nextPoint));
 				j+= step;
 			}
 			movingRight = !movingRight;
 		}
 		
 		path.add(collectorLocation);
 		return path;
 	}
 	
 	
 	private void computeMST() {
 		HashSet<Point2D.Double> seen = new HashSet<Point2D.Double>();
 		//compute mst		
 		while(seen.size() != vertices.size()) {
 			for(Point2D.Double p:vertices) {
 				ArrayList<Point2D.Double> adjacent = graph.get(p);
 				seen.add(p);
 				
 				for (Point2D.Double a:adjacent) {
 					if (!seen.contains(a)) {
 						ArrayList<Point2D.Double> pAdjacent;
 						ArrayList<Point2D.Double> aAdjacent;
 
 						if (mst.containsKey(p)) {
 							pAdjacent = mst.get(p);
 						}
 						else {
 							pAdjacent = new ArrayList<Point2D.Double>();
 						}
 
 						pAdjacent.add(a);
 						mst.put(p, pAdjacent);
 						seen.add(a);
 
 						if (mst.containsKey(a)) {
 							aAdjacent = mst.get(a);
 						}
 						else {
 							aAdjacent = new ArrayList<Point2D.Double>();
 						}
 
 						aAdjacent.add(p);
 						mst.put(a, aAdjacent);
 					}
 				}
 			}	
 		}
 	}
 	
 	private static Point2D lineIntersect(Line2D l1, Line2D l2){
 		double denom = (l2.getY2() - l2.getY1()) * (l1.getX2() - l1.getX1()) - (l2.getX2() - l2.getX1()) * (l1.getY2() - l1.getY1());
 		//lines are parallel
 		if(denom == 0.0d)
 			return null;
 		double ua = ((l2.getX2() - l2.getX1()) * (l1.getY1() - l2.getY1()) - (l2.getY2() - l2.getY1()) * (l1.getX1() - l2.getX1()))/denom;
 		double ub = ((l1.getX2() - l1.getX1()) * (l1.getY1() - l2.getY1()) - (l1.getY2() - l1.getY1()) * (l1.getX1() - l2.getX1()))/denom;
 		if(ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f){
 			return new Point2D.Double(l1.getX1() + ua*(l1.getX2() - l1.getX1()), l1.getY1() + ub*(l1.getY2() - l1.getY1()));
 		}
 		return null;
 	}
 	
 	boolean isObstructedExtended(Point2D.Double startPoint, Point2D.Double endPoint) {
 		Iterator<Line2D> wallIterator = extendedwalls.iterator();		
 		Line2D.Double testLine = new Line2D.Double(startPoint, endPoint);
 		
 		boolean intersects = false;
 		while (wallIterator.hasNext()) {
 			Line2D.Double wall = (Line2D.Double)wallIterator.next();
 			if (lineIntersect(testLine, wall) != null) {
 					return true;
 				}
 			
 		}
 		
 		return intersects;
 	}
 	
 	
 	boolean isObstructed(Point2D.Double startPoint, Point2D.Double endPoint) {
 		Iterator<Line2D> wallIterator = walls.iterator();		
 		Line2D.Double testLine = new Line2D.Double(startPoint, endPoint);
 		
 		boolean intersects = false;
 		while (wallIterator.hasNext()) {
 			Line2D.Double wall = (Line2D.Double)wallIterator.next();
 			if (lineIntersect(testLine, wall) != null) {
 					return true;
 				}
 			
 		}
 		
 		return intersects;
 	}
 	
 
 	
 	
 //	boolean isObstructed(Point2D.Double startPoint, Point2D.Double endPoint) {
 //		Iterator<Line2D> wallIterator = walls.iterator();
 //		ArrayList<Line2D.Double> testLines = new ArrayList<Line2D.Double>();
 //		Point2D.Double leftStart = new Point2D.Double(Math.max(0, startPoint.getX() - 1), startPoint.getY());
 //		Point2D.Double leftEnd =  new Point2D.Double(Math.max(0, endPoint.getX() - 1), endPoint.getY());
 //		
 //		Line2D.Double left = new Line2D.Double(leftStart, leftEnd);
 //		
 //		leftStart = new Point2D.Double(Math.max(0, startPoint.getX() - 2), startPoint.getY());
 //		leftEnd =  new Point2D.Double(Math.max(0, endPoint.getX() - 2), endPoint.getY());
 //		
 //		Line2D.Double leftleft = new Line2D.Double(leftStart, leftEnd);
 //		testLines.add(left);
 //		testLines.add(leftleft);
 //		
 //		Point2D.Double rightStart = new Point2D.Double(Math.min(BOARDSIZE, startPoint.getX() + 1), startPoint.getY());
 //		Point2D.Double rightEnd = new Point2D.Double(Math.min(BOARDSIZE, endPoint.getX() + 1), endPoint.getY());
 //		
 //		Line2D.Double right = new Line2D.Double(rightStart, rightEnd);
 //		
 //		rightStart = new Point2D.Double(Math.min(BOARDSIZE, startPoint.getX() + 2), startPoint.getY());
 //		rightEnd =  new Point2D.Double(Math.min(BOARDSIZE, endPoint.getX() + 2), endPoint.getY());
 //		
 //		Line2D.Double rightright = new Line2D.Double(rightStart, rightEnd);
 //		testLines.add(rightright);
 //		
 //		Point2D.Double aboveStart = new Point2D.Double(startPoint.getX(), Math.max(0, startPoint.getY() - 1));
 //		Point2D.Double aboveEnd = new Point2D.Double(endPoint.getX(), Math.max(0, endPoint.getY() - 1));
 //		
 //		Line2D.Double above = new Line2D.Double(aboveStart, aboveEnd);
 //	    aboveStart = new Point2D.Double(startPoint.getX(), Math.max(0, startPoint.getY() - 2));
 //		aboveEnd = new Point2D.Double(endPoint.getX(), Math.max(0, endPoint.getY() - 2));
 //		
 //	    Line2D.Double aboveabove = new Line2D.Double(aboveStart, aboveEnd);
 //		testLines.add(above);
 //		testLines.add(aboveabove);
 //		
 //		
 //		Point2D.Double belowStart =  new Point2D.Double(startPoint.getX(), Math.min(BOARDSIZE, startPoint.getY() + 1));
 //		Point2D.Double belowEnd =  new Point2D.Double(endPoint.getX(), Math.min(BOARDSIZE, endPoint.getY() + 1));
 //		Line2D.Double below = new Line2D.Double(belowStart,belowEnd);
 //		
 //		
 //		testLines.add(below);
 //		
 //
 //	    belowStart =  new Point2D.Double(startPoint.getX(), Math.min(BOARDSIZE, startPoint.getY() + 2));
 //		belowEnd =  new Point2D.Double(endPoint.getX(), Math.min(BOARDSIZE, endPoint.getY() + 2));
 //		
 //		Line2D.Double belowbelow = new Line2D.Double(belowStart, belowEnd);
 //		testLines.add(belowbelow);
 //		
 //		Line2D.Double testLine = new Line2D.Double(startPoint, endPoint);
 //		testLines.add(testLine);
 //		
 //		boolean intersects = false;
 //		while (wallIterator.hasNext()) {
 //			Line2D.Double wall = (Line2D.Double)wallIterator.next();
 //			for (Line2D test : testLines) {
 //				if (lineIntersect(test, wall) != null) {
 //					return true;
 //				}
 //			}
 //		}
 //		
 //		return intersects;
 //	}
 //	
 	private boolean captured(Point2D.Double p) {
 		boolean withinLightRad = false;
 		for (Light l : lights) {
 			Point2D.Double lightLocation = (Point2D.Double)l.getLocation();
 			if (withinLightRadius(lightLocation, p) && isObstructed(lightLocation, p) == false) {
 				withinLightRad = true;
 				break;
 			}
 		}
 		
 		return withinLightRad;
 	}
 	
 	private boolean allMosquitosCaptured(int[][] board) {
 		for (int i = 0; i < board.length; i ++) {
 			for (int j = 0; j < board[0].length; j ++) {
 				if (board[i][j] > 0) {
 					if (!captured(new Point2D.Double(i, j))) {
 							return false;
 						}
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	public Set<Light> getLights(int[][] board) {
 		lights = new HashSet<Light>();
 		buildGraph();	
 		
 		ArrayList<Point2D> zigzag = zigZagPath();
 		int pathSize = zigzag.size()/(numLights - 1);
 		
 		// initialize lights
 		for(int i = 0; i < numLights-1; i ++) {
 			Point2D nextstart = zigzag.get(i * pathSize);
 			Light l = new MoveableLight(nextstart.getX(), nextstart.getY(), true);
 			lights.add(l);
 		}
 		
 		computeMST();
 //		computePaths();
 		
 //		for (Light l: lights) {
 //			ArrayList<Point2D> zigzag = zigZagPath();
 //			paths.put(l, zigzag);
 //		}
 		zigZagPaths();
 		
 		collectorLight = new MoveableLight(collectorLocation.getX() - 1, collectorLocation.getY(), true);
 		lights.add(collectorLight);
 		
 		return lights;
 	}
 	
 	public Set<Light> updateLights(int[][] board) {		
 		//for each light
 		for (Light l : lights) {
 			
 			//standard setup
 			MoveableLight ml = (MoveableLight)l;
 			Point2D.Double dest = null;
 
 			// don't move collector light
 			if (ml.equals(collectorLight)) {
 				if (ml.getLocation().equals(collectorLocation) == false) {
 					ml.moveTo(collectorLocation.getX(), collectorLocation.getY());
 				}	
 				continue;
 			}
 			
 			else if (ml.getLocation().distance(collectorLocation) < 10 && 
 						!isObstructed((Point2D.Double) ml.getLocation(), (Point2D.Double)collectorLocation)) {
 				ml.turnOff();
 			}
 			
 			else if (ml.getLocation().distance(collectorLocation) > 20 && !ml.isOn()) {
 				ml.turnOn();
 			}
 			
 			Point2D.Double p = (Point2D.Double)ml.getLocation();			
 			ArrayList<Point2D> path = paths.get(l);
 			if (path.size() == 1 && !allMosquitosCaptured(board)) {
 				Point2D newPoint = greedyLocation(board);
 				if (newPoint.distance(p) > 1) {
 					path.add(0, newPoint);
 				}
 				paths.put(l, path);
 			}
 			
 			dest = (Point2D.Double)path.get(0);
 			
 			if (allMosquitosCaptured(board)) {
 				path = new ArrayList<Point2D>();
 				path.add(collectorLocation);
 				paths.put(ml, path);
 				dest = (Point2D.Double) collectorLocation;
 				
 				if (astarPaths.containsKey(ml)) {
 					ArrayList<Point2D.Double> astarPath = astarPaths.get(ml);
 					int lastIndex  = astarPath.size() - 1; 
 					if (astarPath.size() == 0 || (astarPath.get(lastIndex).equals(collectorLocation) == false)) {
 						astarPaths.remove(ml);
 					}
 				}
 			}
 			
 			// if we're at the destination, get the next destination point
 			if (p.getX() == dest.getX() && p.getY() == dest.getY() && path.size() > 1) {
 				path.remove(0);
 				Point2D.Double nextPoint = (Point2D.Double)(path.get(0));
 				
 				// create an astar path if the next destination is obstructed
 				if (isObstructed(p, nextPoint)) {
 					try {
 						ArrayList<Point2D.Double> astarPath = astar.getPath(ml, nextPoint, board);
 						Point2D.Double firstPoint = astarPath.get(0);
 						if (p.distance(firstPoint) > 1) {
 							System.out.println("illegal move here");
 						}
 						
 						ml.moveTo(firstPoint.getX(), firstPoint.getY());
 						astarPath.remove(0);
 						astarPaths.put(ml, astarPath);
 					}
 					catch (Exception e) {
 						logger.trace("caught exception 1");
 					}
 				}
 				
 				else {
 					moveTowards(ml, nextPoint);
 				}
 			}
 		
 			// if this light is moving astar, get the next point
 			else if (astarPaths.containsKey(ml)) {
 				ArrayList<Point2D.Double> astarPath = astarPaths.get(ml);
 				if (astarPath.size() > 0) {
 					Point2D.Double nextPoint = astarPath.get(0);
 					astarPath.remove(0);
 					if (p.distance(nextPoint) > 1) {
 						System.out.println("illegal move here");
 					}
 					
 					ml.moveTo(nextPoint.getX(), nextPoint.getY());
 					astarPaths.put(ml, astarPath);
 				}
 				
 				else {
 					astarPaths.remove(ml);
 				}
 			}
 			
 			else if (isObstructed(p, dest)) {
 				try {
 					ArrayList<Point2D.Double> astarPath = astar.getPath(ml, dest, board);
 					Point2D.Double firstPoint = astarPath.get(0);
 					if (p.distance(firstPoint) > 1) {
 						System.out.println("illegal move here");
 					}
 					ml.moveTo(firstPoint.getX(), firstPoint.getY());
 					astarPath.remove(0);
 					astarPaths.put(ml, astarPath);
 				}
 				catch (Exception e) {
 					logger.trace("caught exception 2");
 				}
 			}
 			
 			else {
 					moveTowards(ml, dest);
 			}
 			
 		}
 		return lights;
 	}
 
 
 	private boolean moveTowards(MoveableLight l, Point2D.Double dest) {
 		Point2D.Double current = (Point2D.Double) l.getLocation();
 		boolean moved = false;
 		double xdiff = current.getX() - dest.getX();
 		double ydiff = current.getY() - dest.getY();
 		
 		if (this.isObstructedExtended(current, dest)) {
 			int buffer = 10;
 			Point2D.Double below = new Point2D.Double(current.getX(), Math.min(BOARDSIZE, current.getY() + buffer));
 			Point2D.Double above = new Point2D.Double(current.getX(), Math.max(0, current.getY() - buffer));
 			Point2D.Double left = new Point2D.Double(Math.max(0,current.getX() - buffer), current.getY());
 			Point2D.Double right = new Point2D.Double(Math.min(BOARDSIZE,current.getX() + buffer), current.getY());
 			if (isObstructedExtended(above, dest) == false && ydiff > 0) {
 				l.moveUp();
 				return true;
 			}
 			else if (isObstructedExtended(below, dest) == false && ydiff < 0) {
 				l.moveDown();
 				return true;
 			}
 			else if (isObstructedExtended(left, dest) == false && xdiff > 0) {
 				l.moveLeft();
 				return true;
 			}
 			else if (isObstructedExtended(right, dest) == false && xdiff < 0) {
 				l.moveRight();
 				return true;
 			}
 		}
 		
 
 		if (Math.abs(xdiff) > Math.abs(ydiff)) {
 			if (current.getX() > dest.getX()) {
 				l.moveLeft();
 				return true;
 			}
 			else if (current.getX() < dest.getX()){
 				l.moveRight();
 				return true;
 			}
 		}
 		else {
 			if (current.getY() > dest.getY()) {
 				l.moveUp();
 				return true;
 			}
 			else if (current.getY() < dest.getY()) {
 				l.moveDown();
 				return true;
 			}
 		}
 		
 		return moved;
 	}
 	
 	
 	@Override
 	public Collector getCollector() {
 		// this one just places a collector next to the last light that was added
 		Collector c = new Collector(collectorLocation.getX(), collectorLocation.getY());
 		return c;
 	}
 	
 }
 
 
