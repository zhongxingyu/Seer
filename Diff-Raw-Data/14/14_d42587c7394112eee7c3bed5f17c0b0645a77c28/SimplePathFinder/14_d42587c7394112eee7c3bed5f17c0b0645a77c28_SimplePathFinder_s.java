 package balle.strategy.pathFinding;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Stack;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import balle.main.drawable.Dot;
 import balle.main.drawable.Drawable;
 import balle.strategy.curve.Curve;
 import balle.strategy.curve.Interpolator;
 import balle.strategy.curve.Spline;
 import balle.strategy.pathFinding.path.MaxSpeedPath;
 import balle.strategy.pathFinding.path.Path;
 import balle.world.Coord;
 import balle.world.Orientation;
 import balle.world.Snapshot;
 import balle.world.objects.Pitch;
 import balle.world.objects.Robot;
 
 public class SimplePathFinder implements PathFinder {
 
 	// Logger
 	public static final Logger LOG = Logger.getLogger(SimplePathFinder.class);
 
 	// Instance variables.
 	protected Interpolator interpolator;
 
 	// Temporary
 	protected Coord start, end;
 	protected Orientation startAngle, endAngle;
 
 	public SimplePathFinder(Interpolator i) {
 		this.interpolator = i;
 	}
 
 	@Override
 	public Path[] getPaths(Snapshot s, Coord end,
 			Orientation endAngle) {
 		
 		Robot robot = s.getBalle();
 		Coord start = robot.getPosition();
 		Orientation startAngle = robot.getOrientation();
 
 		return getPaths(s, start, startAngle, end, endAngle);
 	}
 
 	protected Path[] getPaths(Snapshot s, Coord start,
 			Orientation startAngle, Coord end, Orientation endAngle) {
 		
 		drawables = new ArrayList<Drawable>();
 		// Initialise temporary variables.
 		this.start = start;
 		this.startAngle = startAngle;
 		this.end = end;
 		this.endAngle = endAngle;
 
 		Stack<Coord> list = getPath(start, end, s);
 
 		// remove duplicates (in sequence)
 		Coord last = list.get(0);
 		for (int i = 1; i < list.size(); i++) {
 			if (last.equals(list.get(i))) {
 				list.remove(i);
 				i--;
 			}
 			last = list.get(i);
 		}
 
 		// Convert to a path.
 		return new Path[] { new MaxSpeedPath(getCurve(list)) };
 	}
 
 	/**
 	 * Given the start/end waypoints for a path, modify it by avoiding
 	 * obstacles.
 	 * 
 	 * @param pathStart
 	 *            Coord of the start of the path
 	 * @param pathEnd
 	 *            Coord of the end of the path
 	 * @param s
 	 *            current snapshot
 	 * @return new waypoints avoiding obstacles (includes pathStart and pathEnd)
 	 */
 	public Stack<Coord> getPath(Coord pathStart, Coord pathEnd, Snapshot s) {
 		return getPath(pathStart, pathEnd, s, 0);
 	}
 
 	private Stack<Coord> getPath(Coord pathStart, Coord pathEnd, Snapshot s,
 			int currentDepth) {
 
 		// get the curve without avoiding obstacles
 		Stack<Coord> currentPathStack = new Stack<Coord>();
 		int currentPathLength = currentPathStack.size();
 
 		currentPathStack.push(pathStart);
 		currentPathStack.push(pathEnd);
 		Curve currentCurve = getCurve(currentPathStack);
 
 		// If path has already collided with an obstacle
 		if (currentPathLength > 0
 				&& isClearSoFar(currentCurve, s, currentPathLength) != null)
 			return new Stack<Coord>();
 
 		// find next obstacle
 		// Check for intersections.
 		ArrayList<Obstacle> obsticle = isClear(currentCurve, s);
 
 		// if no obstacls of the path is getting too long, just stop
 		if (obsticle.size() == 0 || currentDepth > 4) {
 			return currentPathStack;
 		} else {
 
 			// get new waypoint(s) possibilities
 			ArrayList<Coord[]> possibleNextWaypoints = new ArrayList<Coord[]>();
 
 			for (Obstacle each : obsticle)
 				for (Coord[] pnw : each.getWaypoint(s, pathStart, currentCurve))
 					possibleNextWaypoints.add(pnw);
 
 			// add all options to the possible paths list
 			ArrayList<Stack<Coord>> possiblePaths = new ArrayList<Stack<Coord>>();
 			for (Coord[] waypoits : possibleNextWaypoints) {
 
 				// reconstruct the path from the pathStart to the final waypoint
 				Stack<Coord> newPath = new Stack<Coord>();
 				newPath.push(pathStart);
 				for (Coord wp : waypoits)
 					newPath.push(wp);
 
 				// recursivelly fill the gap from last waypoint to pathEnd
 				Coord recCallStart = newPath.peek();
 				Coord recCallEnd = pathEnd;
 				Stack<Coord> pathToAppend = getPath(recCallStart, recCallEnd,
 						s, currentDepth + 1);
 				// // appends the new part
 				while (pathToAppend.size() > 0)
 					newPath.push(pathToAppend.remove(0));
 
 				// finish for this possible path
 				possiblePaths.add(newPath);
 
 				// // add drawables
 				for (Coord each : waypoits) {
 					drawables.add(new Dot(each, Color.CYAN));
 				}
 			}
 
 			// return best possible path
 			return best(possiblePaths);
 		}
 	}
 
 	protected ArrayList<Obstacle> isClearSoFar(Curve c, Snapshot s, int len) {
 		Spline curve = (Spline) c;
 		return isClear(curve.getSubSpline(0, len), s);
 	}
 
 	protected ArrayList<Obstacle> isClear(Curve c, Snapshot s) {
		ArrayList<Obstacle> out = new ArrayList<Obstacle>();
 
		// TODO: David says "Review"
		// James says "Yeah, this changes
		// behaviour with more obstacles"
		if (s.getOpponent().getPosition() == null)
			return out;
 
 		Coord pos = s.getBalle().getPosition();
 		for (Obstacle o : getObstacles(s)) {
 			Obstacle adding = null;
 
 			// if we are inside the clearance area, just assume the obstical is
 			// in the way
 			// This is not applicable if the end point is also within the
 			// clearance area
 			// this is not really an obstacle if our path is already moving away
 			if (!o.clear(start) && o.isMovingTowards(c)) {
 				adding = o;
 			}
 
 			// (o.getSource() instanceof Pitch)
 
 			// check that the current path does not go from out of
 			// the object clearance to into the clearance
 			if (adding == null) {
 				boolean in = !o.clear(pos);
 				for (double i = 0; i < 1; i += 0.01) {
 					boolean newIn = !o.clear(c.pos(i));
 					if (!in && newIn) {
 						adding = o;
 						break;
 					}
 				}
 			}
 
 			if (adding != null)
 				out.add(adding);
 		}
 		return out;
 	}
 
 	//
 	// protected Coord getWaypoint(Snapshot s, Obstacle obs, Orientation o) {
 	// Line l, out, walls[];
 	// walls = s.getPitch().getWalls();
 	// l = new Line(obs, obs.add(new Coord(0, 10).rotate(o)));
 	//
 	// Coord wallIntersect = null;
 	// for (Line wall : walls) {
 	// wallIntersect = l.getIntersect(wall);
 	// if (wallIntersect != null)
 	// break;
 	// }
 	//
 	// out = new Line(wallIntersect, obs);
 	// drawables.add(new DrawableLine(out, Color.RED));
 	//
 	// Coord c = new Coord(0, obs.getClearance());
 	// c = c.rotate(o).add(obs);
 	//
 	// return c;
 	// }
 
 	protected Curve getCurve(Vector<Coord> path) {
 		// Convert to array.
 		Coord[] out = new Coord[path.size()];
 		for (int i = 0; i < out.length; i++)
 			out[i] = path.get(i);
 		return interpolator.getCurve(out, startAngle, endAngle);
 	}
 
 	protected Stack<Coord> best(ArrayList<Stack<Coord>> hopefulls) {
 		Stack<Coord> currBest = null;
 		for (Stack<Coord> each : hopefulls) {
 
 			if (currBest == null
 					|| getCurve(currBest).length() > getCurve(each).length())
 				currBest = each;
 		}
 		return currBest;
 
 	}
 
 	// Visual Output \\
 
 	ArrayList<Drawable> drawables = new ArrayList<Drawable>();
 
 	public ArrayList<Drawable> getDrawables() {
 		return drawables;
 	}
 
 	// Collisions
 
 	public Obstacle[] getObstacles(Snapshot s) {
 
 		return new Obstacle[] {
 
 				new Obstacle(s.getOpponent(), Math.min(
 						Obstacle.ROBOT_CLEARANCE, s.getOpponent().getPosition()
 								.dist(end))) {
 					@Override
 					public boolean clear(Coord c) {
 						return c.dist(getPosition()) > clearance;
 					}
 				},
 
 				new Obstacle(s.getPitch(), Obstacle.WALL_CLEARANCE) {
 					@Override
 					public boolean clear(Coord c) {
 						Pitch p = (Pitch) getSource();
 						if (c.getX() < p.getMinX() + clearance)
 							return false;
 						if (c.getX() > p.getMaxX() - clearance)
 							return false;
 						if (c.getY() < p.getMinY() + clearance)
 							return false;
 						if (c.getY() > p.getMaxY() - clearance)
 							return false;
 						return true;
 					}
 
 					private double distance(Coord c) {
 						double d, x = c.getX(), y = c.getY();
 
 						Pitch p = (Pitch) getSource();
 						d = Math.abs(p.getMinX() - x);
 						d = Math.min(d, Math.abs(p.getMaxX() - x));
 						d = Math.min(d, Math.abs(p.getMaxY() - y));
 						d = Math.min(d, Math.abs(p.getMinY() - y));
 						return d;
 					}
 
 					@Override
 					protected boolean isMovingTowards(Curve c) {
 						double initD = distance(c.pos(0));
 						for (double i = 0.01; i <= 1; i += 0.01)
 							if (distance(c.pos(i)) < initD)
 								return true;
 						return false;
 					}
 
 					public Coord constrain(Coord c, Pitch p) {
 						double x = c.getX(), y = c.getY(),
 
 						xmin = p.getMinX() + getClearance(), xmax = p.getMaxX()
 								- getClearance(), ymin = p.getMinY()
 								+ getClearance(), ymax = p.getMaxY()
 								- getClearance();
 
 						return new Coord(Math.min(xmax, Math.max(xmin, x)),
 								Math.min(ymax, Math.max(ymin, y)));
 					}
 
 					@Override
 					public Coord[][] getWaypoint(Snapshot s, Coord curr, Curve c) {
 
 						Pitch pitch = (Pitch) getSource();
 
 						ArrayList<Coord> waypoints = new ArrayList<Coord>();
 						for (double t = 0; t <= 1; t += 0.01) {
 							Coord point = c.pos(t);
 							Coord cons = constrain(point, pitch);
 							if (!point.equals(cons)) {
 								waypoints.add(cons);
 							}
 						}
 
 						if (waypoints.size() == 0) {
 							System.out
 									.println("aaaaaaaaaaaaaaaaaaaaaaaaaaa34dss7");
 							return new Coord[0][0];
 						}
 						if (waypoints.size() == 1) {
 							System.out
 									.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbw45732");
 							return new Coord[][] { new Coord[] { waypoints
 									.get(0) } };
 						}
 						Coord[] wpa = new Coord[] { waypoints.get(0),
 								waypoints.get(waypoints.size() - 1) };
 						return new Coord[][] { wpa };
 					}
 				}
 
 		// new Obstical(s.getBall().getPosition(),
 		// Obstical.ROBOT_CLEARANCE)
 		};
 	}
 
 }
