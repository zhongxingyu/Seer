 package balle.strategy.bezierNav;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Arrays;
 
import org.apache.log4j.Logger;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.World;
 
 import balle.controller.Controller;
 import balle.main.drawable.Circle;
 import balle.main.drawable.Dot;
 import balle.main.drawable.Drawable;
 import balle.misc.Globals;
 import balle.simulator.WorldSimulator;
 import balle.strategy.FactoryMethod;
 import balle.strategy.curve.Curve;
 import balle.strategy.curve.CustomCHI;
 import balle.strategy.executor.movement.OrientedMovementExecutor;
 import balle.strategy.pathfinding.PathFinder;
 import balle.strategy.pathfinding.SimplePathFinder;
 import balle.strategy.planner.SimpleGoToBallFaceGoal;
 import balle.world.BasicWorld;
 import balle.world.Coord;
 import balle.world.Orientation;
 import balle.world.Snapshot;
 import balle.world.objects.FieldObject;
 import balle.world.objects.Robot;
 
 public class BezierNav implements OrientedMovementExecutor {
 
 
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
 	private final double MIN_SAFE_RADIUS = 0; // the smallest turning radius
 												// where moving at maximum speed
 												// is ok (0.05)
 	private final double SAFER_SPEED_RATIO = 0.5; // ratio of (max
 													// speed)/((minimum)safe
 													// speed). when making sharp
 													// turns the speed will be
 													// slowed toward max/this
 	private final double MAX_VELOCITY = Globals
 .powerToVelocity(600); // the
 																		// maximum
 																// wheel
 															// velocity to use
 	private final double DAMPENING_POWERCHANGE = 0.7;
 	private final double DAMPENING_POWERRATIO = 0; // increase towards 1 to make
 													// the robot move more
 													// strait
 
 	private static final double SUBTARGET_RADIUS = 0.08; // how close the robot
 															// has
 														// to be to intermitent
 														// points befor it try
 														// to get to the next
 														// point
 
 	// these 2 must moth be satisfied before this movement is finished
 	private double stopDistance = 0.03; // distance to target (centre of robot
 										// to adjusted p3)
 	private double stopAngle = Math.PI / 13; // angle of robot vs desired final
 												// angle (orient)
 
 	private PID pid = new PID(11, 0.25, 4, 1);
 	private final boolean USE_PID = false;
 
 	WorldSimulator simulator;
 	BasicWorld world;
 
 	private Orientation lastAngle;
 	private long lastAngleTime;
 
 
 
 	private ArrayList<Coord> targetPoints = new ArrayList<Coord>(
 			Arrays.asList(new Coord[] {
  new Coord(0.5, 0.3), new Coord(1, 1),
 					new Coord(0.2, 0.4), new Coord(0.5, 0.3),
 
 			}));
 	
 	private PathFinder pathfinder;
 	private Curve c;
 
     private FieldObject         target;
 
 	private Orientation orient;
 	private Coord p0, p3;
 
 	private ArrayList<ControllerHistoryElement> controllerHistory; // newer
 																	// snapshots
 																	// at the
 																	// end
 
 	@FactoryMethod(designator = "BezierNav")
 	public static SimpleGoToBallFaceGoal bezierNavFactory() {
 		return new SimpleGoToBallFaceGoal(new BezierNav(new SimplePathFinder(
 				new CustomCHI())));
 	}
 
 	public BezierNav(PathFinder pathfinder) {
 		this.pathfinder = pathfinder;
 		simulator = new WorldSimulator(false);
 		simulator.setWorld(new World(new Vec2(), true));
 		simulator.initWorld();
 		simulator.setVisionDelay(0);
 		controllerHistory = new ArrayList<ControllerHistoryElement>();
 	}
 
 	private boolean isMoveStraitFinished(Orientation botOrient) {
 		Coord n = botOrient.getUnitCoord()
 				.rotate(new Orientation(Math.PI / 2));
 		double da = Math
 				.abs(botOrient.angleToatan2Radians(orient));
 		double dd = Math.abs(n.dot(target.getPosition().sub(p0)));
 		return (da <= stopAngle && dd < (Globals.ROBOT_WIDTH / 2)
 				- TARGET_OFF_CENTER_TOLERANCE);
 	}
 
 	@Override
 	public boolean isFinished(Snapshot snapshot) {
 		if (p0 == null || p3 == null) {
 			// haven't even started
 			return false;
 		}
 		return snapshot.getBalle().getPosition().dist(getAdjustedP3()) <= stopDistance
 				&& isMoveStraitFinished(snapshot.getBalle().getOrientation());
 	}
 
 	@Override
 	public boolean isPossible(Snapshot snapshot) {
 		return true;
 	}
 
 	@Override
 	public void step(Controller controller, Snapshot snapshot) {
 		if (!USE_PID) {
 			snapshot = getLatencyAdjustedSnapshot(snapshot);
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
 
 		p0 = rP;
 		p3 = tP;
 
 		// if we are close to the target and facing the correct orientation
 		// (orient)
 		// just go strait to ball
 		Coord n = robot.getOrientation().getUnitCoord()
 				.rotate(new Orientation(Math.PI / 2));
 		double da = Math
 				.abs(robot.getOrientation()
 				.angleToatan2Radians(orient));
 		double dd = Math.abs(n.dot(target.getPosition().sub(p0)));
 		if ((da <= Math.PI / 2 && dd < (Globals.ROBOT_WIDTH / 2)
 				- TARGET_OFF_CENTER_TOLERANCE)) {
 			p3 = target.getPosition();
 		}
 
 		// remove current target point if close
 		if (targetPoints.size() > 0
 				&& p0.dist(targetPoints.get(0)) <= SUBTARGET_RADIUS) {
 			targetPoints.remove(0);
 		}
 
 		Coord[] tpa = new Coord[targetPoints.size() + 2];
 		tpa[0] = p0;
 		tpa[tpa.length - 1] = p3;
 		for (int i = 0; i < targetPoints.size(); i++) {
 			tpa[i + 1] = targetPoints.get(i);
 		}
 
 		c = pathfinder.getPath(snapshot, robot.getPosition(),
 				robot.getOrientation(), tP, orient);
 		
 		// calculate turning radius
 		Coord a = c.acc(0);
 		boolean isLeft = new Coord(0, 0).angleBetween(
 				robot.getOrientation().getUnitCoord(), a)
 				.atan2styleradians() > 0;
 		double r = c.rad(0);
 
 		// throttle speed (slow when doing sharp turns)
 		double max = MAX_VELOCITY;
 		// // maximum speed is ok
 		if (r < MIN_SAFE_RADIUS) {
 			double min = max * SAFER_SPEED_RATIO;
 			max = min + ((r / MIN_SAFE_RADIUS) * (max - min));
 		}
 
 		// calculate wheel speeds/powers
 		double v1, v2, left, right;
 		v1 = Globals.velocityToPower((float) (max * getMinVelocityRato(r)));
 		v2 = Globals.velocityToPower((float) max);
 		left = isLeft ? v1 : v2;
 		right = isLeft ? v2 : v1;
 		// find current wheel powers
 		long dT = snapshot.getTimestamp() - lastAngleTime;
 		if (USE_PID && dT > 0) {
 			if (lastAngle != null) {
 				// this uses the angular and linear velocity of the robot to
 				// find the estimated powers to the wheels
 				double dA = lastAngle.angleToatan2Radians(robot
 						.getOrientation());
 				double basicV = (dA * Globals.ROBOT_TRACK_WIDTH / 2) / dT;
 				int flipper = robot.getVelocity().dot(
 						robot.getOrientation().getUnitCoord()) <= 0 ? -1 : 1;
 				double curentLeftP = Globals
 						.velocityToPower((float) ((flipper * robot
 								.getVelocity().abs()) - basicV));
 				double curentRightP = Globals
 						.velocityToPower((float) ((flipper * robot
 								.getVelocity().abs()) + basicV));
 				// use PID
 				left = pid.convert(left, curentLeftP);
 				right = pid.convert(right, curentRightP);
 			}
 			lastAngleTime = snapshot.getTimestamp();
 			lastAngle = robot.getOrientation();
 		}
 
 		// dampen
 		if (controllerHistory.size() > 0) {
 			double invDampening = 1 - DAMPENING_POWERCHANGE;
 
 			left = (invDampening * left)
 					+ (DAMPENING_POWERCHANGE * controllerHistory.get(
 							controllerHistory.size() - 1).getPowerLeft());
 			right = (invDampening * right)
 					+ (DAMPENING_POWERCHANGE * controllerHistory.get(
 							controllerHistory.size() - 1).getPowerRight());
 		}
 
 		System.out.println(left + "\t\t" + right);
 
 		controller.setWheelSpeeds((int) left, (int) right);
 		controllerHistory.add(new ControllerHistoryElement((int) left,
 				(int) right, System.currentTimeMillis()));
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
 		System.out.println(currentTime + "  " + simulatorTime);
 
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
 		if (p0 == null) {
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
 
 		l.add(new Circle(p0, 0.03, Color.pink));
 		l.add(new Circle(p3, 0.03, Color.pink));
 
 		return l;
 	}
 
 
 	/**
 	 * P3 (target) must be adjusted so that the front of the robot doesn't bump
 	 * into the actual target
 	 * @return
 	 */
 	private Coord getAdjustedP3() {
 		return target.getPosition().add(
 				new Coord(-TARGET_PERIMETER, 0).rotate(orient));
 	}
 
 	@Override
 	public void setStopDistance(double stopDistance) {
 		this.stopDistance = stopDistance;
 	}
 
 	private double getMinVelocityRato(double radius) {
 		double rtw = Globals.ROBOT_TRACK_WIDTH / 2;
 		double ratio = ((radius - rtw) / (radius + rtw));
 		return ratio + ((1 - ratio) * DAMPENING_POWERRATIO);
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
 }
