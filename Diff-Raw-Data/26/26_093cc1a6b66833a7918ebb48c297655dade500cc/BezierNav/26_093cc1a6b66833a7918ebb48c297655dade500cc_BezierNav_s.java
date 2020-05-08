 package balle.strategy.bezierNav;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.World;
 
 import balle.controller.Controller;
 import balle.main.drawable.Circle;
 import balle.main.drawable.Dot;
 import balle.main.drawable.Drawable;
 import balle.misc.Globals;
 import balle.simulator.SnapshotPredictor;
 import balle.simulator.WorldSimulator;
 import balle.strategy.ConfusedException;
 import balle.strategy.FactoryMethod;
 import balle.strategy.curve.Curve;
 import balle.strategy.curve.CustomCHI;
 import balle.strategy.executor.movement.MovementExecutor;
 import balle.strategy.executor.movement.OrientedMovementExecutor;
 import balle.strategy.pathFinding.ForwardAndReversePathFinder;
 import balle.strategy.pathFinding.PathFinder;
 import balle.strategy.pathFinding.SimplePathFinder;
 import balle.strategy.pathFinding.ValidPathNotFoundException;
 import balle.strategy.pathFinding.path.Path;
 import balle.strategy.planner.SimpleGoToBallFaceGoal;
 import balle.world.BasicWorld;
 import balle.world.Coord;
 import balle.world.Orientation;
 import balle.world.Snapshot;
 import balle.world.objects.FieldObject;
 import balle.world.objects.Robot;
 
 public class BezierNav implements OrientedMovementExecutor, MovementExecutor {
 
 
 	// this is how far away from the target that the robot (center) should land
 	// the default causes the robot to stop just in front of the target.
 	// If this was 0 and the target was the ball, the robot would be prone
 	// to crashing into the ball b4 obtainning the correct orientation.
 	private final double TARGET_PERIMETER = (Math.sqrt(Math.pow(
 			Globals.ROBOT_LENGTH, 2) + Math.pow(Globals.ROBOT_WIDTH, 2)) / 2) + 0.01;
 
     public static final Logger LOG = Logger.getLogger(BezierNav.class);
 
 	private final double TARGET_OFF_CENTER_TOLERANCE = 0.01; // (ROBOTWIDTH/2)-
 														// TARGET_OFF_CENTER_TOLERANCE
 														// = max arrival
 														// distance of the
 														// target from the
 														// center front of the
 														// robot
 
 	private static final double SUBTARGET_RADIUS = 0.08; // how close the robot
 															// has
 														// to be to intermitent
 														// points befor it try
 														// to get to the next
 														// point
 	private static final double CHANGEPATH_IMPROVEMENT_THRESHHOLD = 1.05; // only
 																			// change
 																			// paths
 																			// if
 																			// there
 																			// is
 																			// an
 																			// improvement
 																			// above
 																			// this
 																			// percentage
 
 	// these 2 must moth be satisfied before this movement is finished
 	private double stopDistance = 0.03; // distance to target (centre of robot
 										// to adjusted p3)
 	private double stopAngle = Math.PI / 6; // angle of robot vs desired final
 												// angle (orient)
 
 
 	WorldSimulator simulator;
 	BasicWorld world;
 
 	private int lastPathIndexUsed = -1;
 
 	private PathFinder pathfinder;
 	private Curve c;
 	private ArrayList<Curve> altCurves = new ArrayList<Curve>();
 
     private FieldObject         target;
 
 	private Orientation orient;
 	private Coord pi, pf;
 
 	private ArrayList<ControllerHistoryElement> controllerHistory; // newer
 																	// snapshots
 																	// at the
 																	// end
 	private boolean endInfront;
 
     @FactoryMethod(designator = "BezierNav", parameterNames = {})
 	public static SimpleGoToBallFaceGoal bezierNavFactory() {
 		return new SimpleGoToBallFaceGoal(new BezierNav(
 				new SimplePathFinder(
 				new CustomCHI()), true));
 	}
 
 	@FactoryMethod(designator = "BezierNav (Forwards and backwards)", parameterNames = {})
 	public static SimpleGoToBallFaceGoal bezierNavFactoryFAB() {
 		return new SimpleGoToBallFaceGoal(new BezierNav(
 				new ForwardAndReversePathFinder(
 				new CustomCHI()), true));
 	}
 
 	public BezierNav(PathFinder pathfinder) {
 		this(pathfinder, false);
 	}
 
 	public BezierNav(PathFinder pathfinder, boolean endInfront) {
 		this.pathfinder = pathfinder;
 		this.endInfront = endInfront;
 		simulator = new WorldSimulator(false);
 		simulator.setWorld(new World(new Vec2(), true));
 		simulator.initWorld();
 		simulator.setVisionDelay(0);
 		controllerHistory = new ArrayList<ControllerHistoryElement>();
 	}
 
 	private boolean isBackwards(Robot bot) {
 		double da = Math.abs(bot.getVelocity().getUnitCoord()
 				.angleBetween(bot.getOrientation().getUnitCoord())
 				.atan2styleradians());
 		return da > Math.PI / 2;
 	}
 
 	private boolean isMoveStraitFinished(Robot bot) {
 		Orientation o = bot.getOrientation();
 		if(isBackwards(bot)) {
 			o = o.getOpposite();
 		}
 		Coord n = o.getUnitCoord()
 				.rotate(new Orientation(Math.PI / 2));
 		double da = Math.abs(bot.getOrientation().angleToatan2Radians(orient));
 		double dd = Math
 				.abs(n.dot(target.getPosition().sub(bot.getPosition())));
 		return (da <= stopAngle && dd < (Globals.ROBOT_WIDTH / 2)
 				- TARGET_OFF_CENTER_TOLERANCE);
 	}
 
 	@Override
 	public boolean isFinished(Snapshot snapshot) {
 		if (pi == null || pf == null) {
 			// haven't even started
 			return false;
 		}
 		return snapshot.getBalle().getPosition().dist(getAdjustedP3()) <= stopDistance
 				&& isMoveStraitFinished(snapshot.getBalle());
 	}
 
 	@Override
 	public boolean isPossible(Snapshot snapshot) {
 		return true;
 	}
 
 	@Override
 	public void step(Controller controller, Snapshot snapshot)
 			throws ConfusedException {
 
 		// adjust for latency
 		SnapshotPredictor sp = snapshot.getSnapshotPredictor();
 		snapshot = sp.getSnapshotAfterTime(System.currentTimeMillis()
 				- snapshot.getTimestamp());
 
 		Robot r = snapshot.getBalle();
 		if (isMoveStraitFinished(r)) {
 			if (r.getPosition().dist(snapshot.getOpponentsGoal().getPosition()) > 0.3) {
 				if (isBackwards(r)) {
 					controller.backward(Globals.MAXIMUM_MOTOR_SPEED);
 				} else {
 					controller.forward(Globals.MAXIMUM_MOTOR_SPEED);
 				}
 			} else {
 				controller.stop();
 			}
 			return;
 		}
 
 		// Get orientation.
 		Orientation angle = this.orient;
 		if (angle == null) {
 			// If orient is null, then behave like a a movement executor
 			Coord c = target.getPosition().sub(
 					snapshot.getBalle().getPosition());
 			angle = c.getOrientation();
 		}
 
 		if (isFinished(snapshot)) {
 			stop(controller);
 			return;
 		}
 
 		// calculate bezier points 0 to 3
 		Robot robot = snapshot.getBalle();
 		Coord rP = robot.getPosition(), tP = getAdjustedP3();
 		if (rP == null || tP == null) {
 			return;
 		}
 
 		pi = rP;
 		pf = tP;
 
 		// // if we are close to the target and facing the correct orientation
 		// // (orient)
 		// // just go strait to ball
 		// Coord n = robot.getOrientation().getUnitCoord()
 		// .rotate(new Orientation(Math.PI / 2));
 		// double da = Math
 		// .abs(robot.getOrientation()
 		// .angleToatan2Radians(angle));
 		// double dd = Math.abs(n.dot(target.getPosition().sub(pi)));
 		// if ((da <= Math.PI / 2 && dd < (Globals.ROBOT_WIDTH / 2)
 		// - TARGET_OFF_CENTER_TOLERANCE)) {
 		// pf = target.getPosition();
 		// }
 
 		// decide on a path to take
 		Path pathToTake = null;
 		try {
 			pathToTake = getBestPath(snapshot, angle);
 			c = pathToTake.getCurve();
 		} catch (ValidPathNotFoundException e) {
 			throw new ConfusedException(e);
 		}
 
 		// calculate wheel speeds/powers
 		int[] pows = pathToTake.getPowers(robot, 0);
 		// int[] stepPows = pathToTake.getPowers(robot,
 		// pathToTake.realTimeToCurveTime(0.1, robot));
 		// pows[0] *= stepPows[0] / 2;
 		// pows[1] *= stepPows[1] / 2;
 		int left, right;
 		left = pows[0]; // Globals.velocityToPower((float) (max *
 						// getMinVelocityRato(r)));
 		right = pows[1]; // Globals.velocityToPower((float) max);
 
 
 		LOG.trace(left + "," + right);
 		controller.setWheelSpeeds(left, right);
 		controllerHistory.add(new ControllerHistoryElement((int) left,
 				(int) right, System.currentTimeMillis()));
 	}
 	
 	private Path getBestPath(Snapshot s, Orientation finalOrient)
 			throws ValidPathNotFoundException {
 		// get candidate paths
 		List<Path> paths = pathfinder.getPaths(s, pf, finalOrient);
 
 		// for (Path path : paths) {
 		// path = new WheelAccelerationAwarePath(path);
 		// }
 
 		// Sorry don't know what your doing.
 
 		// boolean hasLastPathUsed = lastPathIndexUsed != -1;
 		// Path best = paths.get(hasLastPathUsed ? lastPathIndexUsed : 0);
 
 		Robot bot = s.getBalle();
 
 		// // look for the quickest time path
 		// double bestTime = best.getTimeToDrive(bot)
 		// * (hasLastPathUsed ? (1 / CHANGEPATH_IMPROVEMENT_THRESHHOLD)
 		// : 1); // make it seem
 		// // better than it is
 		// // to amke the path
 		// // choice more
 		// // consistent
 		// for (int i = 1; i < paths.size(); i++) {
 		// Path curr = paths.get(i);
 		// if (curr != null) {
 		// double currTime = curr.getTimeToDrive(bot);
 		// if (bestTime > currTime) {
 		// best = curr;
 		// bestTime = currTime;
 		// lastPathIndexUsed = i;
 		// }}}
 
 		Path best = null;
 		for (Path each : paths) {
 			if (best == null
 					|| each.getTimeToDrive(bot) < best.getTimeToDrive(bot))
 				best = each;
 		}
 
 		// save the unused paths for drawing
 		altCurves.clear();
 		for (Path p : paths) {
 			if (p != best) {
 				altCurves.add(p.getCurve());
 			}
 		}
 		return best;
 	}
 
 	/**
 	 * Use the simulator to predict where the robot is after the latency
 	 * Globals.SIMULATED_VISON_DELAY. This relies on controllerHistory being up
 	 * to date;
 	 * 
 	 * @param s
 	 *            Snapshot representing the world Globals.SIMULATED_VISON_DELAY
 	 *            ms ago
 	 * @return Estimated snapshot of where the robots actually are
 	 */
 	@Deprecated
 	private Snapshot getLatencyAdjustedSnapshot(Snapshot s) {
 		if(world == null) {
 			world = new BasicWorld(true, false, s.getPitch());
 			simulator.getReader().addListener(world);
 			simulator.getReader().propagateGoals();
 			simulator.getReader().propagatePitchSize();
 			controllerHistory.add(new ControllerHistoryElement(0, 0, 0));
 
 		}
 		long currentTime = System.currentTimeMillis();
 		long simulatorTime = s.getTimestamp();
 		// System.out.println(currentTime + "  " + simulatorTime);
 
 		// clean up the history (ensure there is at least one element left in
 		// history)
 		while (controllerHistory.size() > 1
 				&& controllerHistory.get(0).getTimestamp() < simulatorTime) {
 			controllerHistory.remove(0);
 		}
 
 		// setup a simulator using the current snapshot (assume we are blue)
 		float lastLPower = 0, lastRPower = 0;
 		if (controllerHistory.size() > 0) {
 			ControllerHistoryElement lastState = controllerHistory.get(0);
 			lastLPower = lastState.getPowerLeft();
 			lastRPower = lastState.getPowerRight();
 		}
 		simulator.setRobotStatesFromSnapshot(s, true, lastLPower,
 				lastRPower);
 		simulator.setStartTime(simulatorTime);
 
 		// use the controllerHistory to simulate the wheelspeeds
 		//// run a simulation while adjusting wheel speeds
 		long maxTD = 50; // low values may make this less accurate
 		for (int i = 0; i < controllerHistory.size(); i++) {
 			ControllerHistoryElement curr = controllerHistory.get(i);
 			long nextTime = currentTime;
 
 
 			if(i < controllerHistory.size() - 1)
 				nextTime = controllerHistory.get(i + 1).getTimestamp();
 			for (long tD = maxTD; simulatorTime < nextTime; tD = Math.min(
 					simulatorTime + tD, nextTime) - simulatorTime) {
 				//simulator.getBlueSoft().setWheelSpeeds(900, 900);
 				simulator.getBlueSoft().setWheelSpeeds(curr.getPowerLeft(),
 						curr.getPowerRight());
 				simulator.update(tD);
 				simulator.getWorld().step(tD / 1000f, 8, 3);
 				simulator.getReader().update();
 				simulator.getReader().propagate();
 				// System.out.println(tD);
 				// System.out
 				// .println(world.getSnapshot().getBalle().getPosition());
 				simulatorTime += tD;
 			}
 		}
 
         LOG.error("BezierNav is broken -- Saulius. TODO: FIXME");
 		return null;
 		// retrieve the new snapshot
 		//return simulator.getSnapshot(s.getTimestamp(), s.getPitch(),
         // s.getOpponentsGoal(), s.getOwnGoal(), true);
 	}
 
 	@Override
 	public ArrayList<Drawable> getDrawables() {
 		ArrayList<Drawable> l = new ArrayList<Drawable>();
 		if (pi == null) {
 			return l;
 		}
 
 		l.addAll(pathfinder.getDrawables());
 
 		if (c != null) {
 			l.add(c);
 			Coord center = c.cor(0);
 			l.add(new Dot(center, Color.BLACK));
 			// l.add(new Circle(center, center
 			// .dist(snapshot.getBalle().getPosition()),
 			// Color.yellow));
 		}
 		l.addAll(altCurves);
 		for(Curve c : altCurves) {
 			l.add(c);
 			l.add(new Dot(c.pos(0.1), Color.RED));
 		}
 
 		l.add(new Circle(pi, 0.03, Color.pink));
 		l.add(new Circle(pf, 0.03, Color.pink));
 
 		return l;
 	}
 
 
 	/**
 	 * P3 (target) must be adjusted so that the front of the robot doesn't bump
 	 * into the actual target
 	 * @return
 	 */
 	private Coord getAdjustedP3() {
 		if (endInfront) {
 			return target.getPosition().add(
 				new Coord(-TARGET_PERIMETER, 0).rotate(orient));
 		}
 		return target.getPosition();
 	}
 
 	@Override
 	public void setStopDistance(double stopDistance) {
 		this.stopDistance = stopDistance;
 	}
 
 	@Override
 	public void stop(Controller controller) {
 		controller.stop();
 	}
 
 	@Override
     public void updateTarget(FieldObject target, Orientation o) {
 		this.target = target;
 		this.orient = o;
 	}
 
 	@Override
 	public void updateTarget(FieldObject target) {
 		this.target = target;
         // TODO: Fix FIX FIX
         LOG.error("Beziernav::updateTarget");
 		this.orient = null;
 	}
 }
