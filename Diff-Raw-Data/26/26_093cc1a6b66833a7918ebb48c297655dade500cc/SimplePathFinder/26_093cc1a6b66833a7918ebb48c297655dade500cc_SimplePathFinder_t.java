 package balle.strategy.pathFinding;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
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
 	public List<Path> getPaths(Snapshot s, Coord end, Orientation endAngle)
 			throws ValidPathNotFoundException {
 		
 		Robot robot = s.getBalle();
 		Coord start = robot.getPosition();
 		Orientation startAngle = robot.getOrientation();
 
 		List<Path> out = getPaths(s, start, startAngle, end, endAngle);
 		return out;
 	}
 
 	protected List<Path> getPaths(Snapshot s, Coord start,
 			Orientation startAngle, Coord end, Orientation endAngle)
 			throws ValidPathNotFoundException {
 		
 		drawables = new ArrayList<Drawable>();
 		// Initialise temporary variables.
 		this.start = start;
 		this.startAngle = startAngle;
 		this.end = end;
 		this.endAngle = endAngle;
 
 		Spline spline = getPath(start, end, s);
 		Stack<Coord> list = spline.getComponents();
 
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
 		ArrayList<Path> out = new ArrayList<Path>();
 		out.add(new MaxSpeedPath(getCurve(list)));
 		return out;
 	}
 
 	/**
 	 * Given the start/end way-points for a path, modify it by avoiding
 	 * obstacles.
 	 * 
 	 * @param pathStart
 	 *            Coord of the start of the path
 	 * @param pathEnd
 	 *            Coord of the end of the path
 	 * @param s
 	 *            current snapshot
 	 * @return new waypoints avoiding obstacles (includes pathStart and pathEnd)
 	 * @throws ValidPathNotFoundException
 	 */
 	public Spline getPath(Coord pathStart, Coord pathEnd, Snapshot s)
 			throws ValidPathNotFoundException {
 		Stack<Coord> startPath = new Stack<Coord>();
 		startPath.add(pathStart);
 		return getPath(startPath, pathEnd, s, 0);
 	}
 
 	/**
 	 * GET PATH.
 	 * 
 	 * @param pathSoFar
 	 *            Preceding path to this decision. [ This is necessary for
 	 *            correct collision detection ]
 	 * 
 	 * @param pathEnd
 	 *            Desired end-point of path.
 	 * 
 	 * @param s
 	 *            Current Snapshot.
 	 * 
 	 * @param currentDepth
 	 *            How many recursions have happened already.
 	 * 
 	 * @return Best possible route from here onwards, null if no routes are
 	 *         valid.
 	 * 
 	 * @throws ValidPathNotFoundException
 	 *             When there are no possible routes.
 	 */
 	private Spline getPath(Stack<Coord> pathSoFar, Coord pathEnd, Snapshot s,
 			int currentDepth) throws ValidPathNotFoundException {
 
 		// Get current curve.
 		pathSoFar.push(pathEnd);
 		Spline currentCurve = getCurve(pathSoFar);
 		pathSoFar.pop();
 
 		// drawables.add(currentCurve);
 
 		// If path has already collided with an obstacle
 		ArrayList<Obstacle> obstacles = isClearSoFar(currentCurve, s,
 				pathSoFar.size());
 		if (obstacles.size() > 0)
 			throw new ValidPathNotFoundException("Already collided with "
 					+ obstacles.get(0));
 
 		// Find obstacles, if any.
 		ArrayList<Obstacle> obsticle = isClearGoingForward(currentCurve, s,
 				pathSoFar.size());
 
 		if (obsticle.size() == 0) {
 
 			// END POINT! ______
 			// This is a valid path, SWEET!
 			return currentCurve;
 			// _________________
 
 		} else if (currentDepth > 4) {
 			throw new ValidPathNotFoundException("Current depth exceeds limit.");
 
 		} else {
 
 			// Get new way-point(s) possibilities.
 			ArrayList<Coord[]> possibleNextWaypoints = new ArrayList<Coord[]>();
 
 			for (Obstacle each : obsticle)
 				for (Coord[] pnw : each.getWaypoint(s, pathSoFar.peek(),
 						currentCurve))
 					possibleNextWaypoints.add(pnw);
 
 			// Add all options to the possible paths list.
 			ArrayList<Spline> possiblePaths = new ArrayList<Spline>();
 			ArrayList<ValidPathNotFoundException> failures = new ArrayList<ValidPathNotFoundException>();
 			for (Coord[] waypoints : possibleNextWaypoints) {
 
 				// Recreate pathSoFar and add all way-points ready for next
 				// iteration.
 				@SuppressWarnings("unchecked")
 				Stack<Coord> newPath = (Stack<Coord>) pathSoFar.clone();
 				for (Coord wp : waypoints)
 					newPath.push(wp);
 
 				try {
 
 					// RECURSIVE CALL!_____________________________
 					// Depth first search, COMPLETE path comes out.
 					Spline validCurve = getPath(newPath, pathEnd, s,
 							currentDepth + 1);
 					// _____________________________________________
 
 					// Add valid curve to hopefuls.
 					possiblePaths.add(validCurve);
 
 				} catch (ValidPathNotFoundException e) {
 					// Ignore path, hope others fill gap
 					// otherwise exception will be thrown again later
 					// [ :S fingers crossed ]
 					failures.add(e);
 				}
 			}
 
 			// These should be complete, going to the end.
 			Spline contender = best(possiblePaths);
 
 			// Debugging information.
 			if (contender == null) {
 				String errors = "";
 				for (ValidPathNotFoundException failure : failures)
 					errors += " " + failure.getMessage();
 
 				throw new ValidPathNotFoundException("This many failures "
 						+ failures.size()
 						+ ", and this many possible solutions "
 						+ possiblePaths.size() + ".\n" + errors);
 			}
 
 			return contender;
 		}
 	}
 
 	protected ArrayList<Obstacle> isClearSoFar(Spline c, Snapshot s, int len) {
 		if (len <= 1)
 			return new ArrayList<Obstacle>();
 
 		Spline sub = c.getSubSpline(0, len);
 
 		return isClear(sub, s, false);
 	}
 
 	protected ArrayList<Obstacle> isClearGoingForward(Spline c, Snapshot s,
 			int len) {
 		Curve sub = c.getLastCurve();
 
 		ArrayList<Obstacle> ans = isClear(sub, s, true);
 		if (ans.size() > 0)
 			drawables.add(sub);
 
 		return ans;
 
 	}
 
 	protected ArrayList<Obstacle> isClear(Curve c, Snapshot s, boolean leniency) {
 		ArrayList<Obstacle> out = new ArrayList<Obstacle>();
 
 		Coord pos = c.getStart(); // s.getBalle().getPosition();
 		for (Obstacle o : getObstacles(s)) {
 
 			// if we are inside the clearance area, just assume the obstacle is
 			// in the way
 			// This is not applicable if the end point is also within the
 			// clearance area
 			// this is not really an obstacle if our path is already moving away
 			if (!o.clear(end, pos, leniency) && o.isMovingTowards(c)) {
 				out.add(o);
 				continue;
 			}
 
 			// (o.getSource() instanceof Pitch)
 
 			// check that the current path does not go from out of
 			// the object clearance to into the clearance
 			Obstacle adding = null;
 			boolean in = !o.clear(end, pos, leniency);
 			for (double i = 0; i < 1; i += 0.01) {
 				boolean newIn = !o.clear(end, c.pos(i), leniency);
 				if (!in && newIn) {
 					adding = o;
 					break;
 				}
 			}
 
			if (adding != null)
 				out.add(adding);
 		}
 		return out;
 	}
 
 	/**
 	 * Convert a list of way-points into a spline.
 	 * 
 	 * @param path
 	 *            List of way-points.
 	 * @return Spline interpolated from path.
 	 */
 	protected Spline getCurve(Vector<Coord> path) {
 		// Convert to array.
 		Coord[] out = new Coord[path.size()];
 		for (int i = 0; i < out.length; i++)
 			out[i] = path.get(i);
 		return interpolator.getCurve(out, startAngle, endAngle);
 	}
 
 	/**
 	 * CHOOSE BEST PATH OUT OF CURRENT SELECTION.
 	 * 
 	 * @param hopefulls
 	 *            Path possibilities
 	 * @return Best path out of this selection.
 	 * 
 	 */
 	protected Spline best(ArrayList<Spline> hopefulls) {
 		Spline currBest = null;
 		for (Spline each : hopefulls)
 			if (currBest == null || currBest.length() > each.length())
 				currBest = each;
 		return currBest;
 	}
 
 	// Visual Output \\
 
 	ArrayList<Drawable> drawables = new ArrayList<Drawable>();
 
 	public ArrayList<Drawable> getDrawables() {
 		return drawables;
 	}
 
 	// Collisions
 
 	public List<Obstacle> getObstacles(Snapshot s) {
 		ArrayList<Obstacle> output = new ArrayList<Obstacle>();
 
 		// Add opponent as obstacle if its on the pitch.
 		if (s.getOpponent().getPosition() != null)
 			output.add(new Obstacle(s.getOpponent(), Math.min(
 				Obstacle.ROBOT_CLEARANCE,
 				s.getOpponent().getPosition().dist(end))) {
 
 			@Override
 				public boolean clear(Coord tar, Coord crd, boolean leniency) {
					double clr, distTar;
					distTar = getSource().getPosition().dist(tar) * 0.8;
					if (!leniency) {
						distTar *= 0.8;
					}
					clr = Math.min(getClearance(leniency), distTar);

					return crd.dist(getPosition()) > clr;
 			}
 
 		});
 
 		output.add(new Obstacle(s.getPitch(), Obstacle.WALL_CLEARANCE) {
 
 			@Override
 			public boolean clear(Coord strt, Coord c, boolean leniency) {
 				Pitch p = (Pitch) getSource();
 				if (c.getX() < p.getMinX() + getClearance(leniency))
 					return false;
 				if (c.getX() > p.getMaxX() - getClearance(leniency))
 					return false;
 				if (c.getY() < p.getMinY() + getClearance(leniency))
 					return false;
 				if (c.getY() > p.getMaxY() - getClearance(leniency))
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
 
 				xmin = p.getMinX() + getClearance(true), xmax = p.getMaxX()
 						- getClearance(true), ymin = p.getMinY()
 						+ getClearance(true), ymax = p.getMaxY()
 						- getClearance(true);
 
 				return new Coord(Math.min(xmax, Math.max(xmin, x)), Math.min(
 						ymax, Math.max(ymin, y)));
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
 					System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaa34dss7");
 					return new Coord[0][0];
 				}
 				if (waypoints.size() == 1) {
 					System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbw45732");
 					return new Coord[][] { new Coord[] { waypoints.get(0) } };
 				}
 				Coord[] wpa = new Coord[] { waypoints.get(0),
 						waypoints.get(waypoints.size() - 1) };
 				return new Coord[][] { wpa };
 			}
 		});
 
 		// new Obstical(s.getBall().getPosition(),
 		// Obstical.ROBOT_CLEARANCE)
 		
 		return output;
 	}
 
 }
