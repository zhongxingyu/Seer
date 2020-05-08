 /**
  * 
  */
 package balle.strategy.planner;
 
 import java.awt.Color;
 
 import org.apache.log4j.Logger;
 
 import balle.controller.Controller;
 import balle.main.drawable.Dot;
 import balle.strategy.executor.movement.MovementExecutor;
 import balle.world.Coord;
 import balle.world.Line;
 import balle.world.Orientation;
 import balle.world.objects.Pitch;
 import balle.world.objects.Point;
 import balle.world.objects.Robot;
 import balle.world.objects.StaticFieldObject;
 
 /**
  * @author s0909773
  * 
  */
 public class GoToBall extends AbstractPlanner {
 
 	protected static final Logger LOG = Logger.getLogger(GoToBall.class);
 
 	MovementExecutor executorStrategy;
 
 	private static final double AVOIDANCE_GAP = 0.5; // Meters
 	private static final double OVERSHOOT_GAP = 0.5; // Meters
 	private static final double DIST_DIFF_THRESHOLD = 0.2; // Meters
 	private static final double OVERSHOOT_ANGLE_EPSILON = 30; // Degrees
 
 	private boolean approachTargetFromCorrectSide;
 
 	public GoToBall(MovementExecutor movementExecutor) {
 		executorStrategy = movementExecutor;
 		approachTargetFromCorrectSide = false;
 	}
 
 	public MovementExecutor getExecutorStrategy() {
 		return executorStrategy;
 	}
 
 	public void setExecutorStrategy(MovementExecutor executorStrategy) {
 		this.executorStrategy = executorStrategy;
 	}
 
 	/**
 	 * Instantiates a new go to ball strategy.
 	 * 
 	 * @param movementExecutor
 	 *            the movement executor
 	 * @param approachTargetFromCorrectSide
 	 *            whether to always appraoch target from correct side
 	 */
 	public GoToBall(MovementExecutor movementExecutor,
 			boolean approachTargetFromCorrectSide) {
 		executorStrategy = movementExecutor;
 		this.approachTargetFromCorrectSide = approachTargetFromCorrectSide;
 	}
 
 	public boolean shouldApproachTargetFromCorrectSide() {
 		return approachTargetFromCorrectSide;
 	}
 
 	public void setApproachTargetFromCorrectSide(
 			boolean approachTargetFromCorrectSide) {
 		this.approachTargetFromCorrectSide = approachTargetFromCorrectSide;
 	}
 
 	protected StaticFieldObject getTarget() {
 		return getSnapshot().getBall();
 	}
 
 	protected Color getTargetColor() {
 		return Color.CYAN;
 	}
 
 	protected Coord calculateAvoidanceCoord(double gap, boolean belowPoint) {
 		int side = 1;
 		if (belowPoint) {
 			side = -1;
 		}
 
 		Robot robot = getSnapshot().getBalle();
 		Coord point = getSnapshot().getOpponent().getPosition();
 
 		// Gets the angle and distance between the robot and the ball
 		double robotObstacleAngle = point.sub(robot.getPosition())
 				.orientation().atan2styleradians();
 		double robotObstacleDistance = point.dist(robot.getPosition());
 		// Calculate the distance between the robot and the destination point
 		double hyp = Math.sqrt((robotObstacleDistance * robotObstacleDistance)
 				+ (gap * gap));
 
 		// Calculate the angle between the robot and the destination point
 		double robotPointAngle = Math.asin(gap / hyp);
 		// Calculate the angle between the robot and the destination point.
 		// Side is -1 if robot is below the ball, so will get the angle needed
 		// for a point
 		// below the ball, whereas side = 1 will give a point above the ball
 		double angle = robotObstacleAngle + (side * robotPointAngle);
 
 		// Offsets are in relation to the robot
 		double xOffset = hyp * Math.cos(angle);
 		double yOffset = hyp * Math.sin(angle);
 
 		return new Coord(robot.getPosition().getX() + xOffset, robot
 				.getPosition().getY() + yOffset);
 	}
 
 	protected Coord calculateOvershootCoord(StaticFieldObject target,
 			double gap, boolean belowPoint) {
 		int side = 1;
 		if (belowPoint) {
 			side = -1;
 		}
 
 		Robot robot = getSnapshot().getBalle();
 		Coord point = getSnapshot().getBall().getPosition();
 
 		// Gets the angle and distance between the robot and the ball
 		double robotTargetOrientation = point.sub(robot.getPosition())
 				.orientation().atan2styleradians();
 		double robotTargetDistance = point.dist(robot.getPosition());
 		// Calculate the distance between the robot and the destination point
 		double hyp = Math.sqrt((robotTargetDistance * robotTargetDistance)
 				+ (gap * gap));
 
 		// Calculate the angle between the robot and the destination point
 		double robotPointAngle = Math.asin(gap / hyp);
 		// Calculate the angle between the robot and the destination point.
 		// Side is -1 if robot is below the ball, so will get the angle needed
 		// for a point
 		// below the ball, whereas side = 1 will give a point above the ball
 		double angle = robotTargetOrientation + (side * robotPointAngle);
 
 		// Offsets are in relation to the robot
 		double xOffset = hyp * Math.cos(angle);
 		double yOffset = hyp * Math.sin(angle);
 
 		return new Coord(robot.getPosition().getX() + xOffset, robot
 				.getPosition().getY() + yOffset);
 	}
 
 	/**
 	 * Returns the target location the robot should go to in order to overshoot
 	 * the target and face the opponents goal from correct angle.
 	 * 
 	 * @param target
 	 *            the target
 	 * @return the overshoot target
 	 */
 	protected StaticFieldObject getOvershootTarget(StaticFieldObject target) {
 		boolean belowBall = true;
 		Robot robot = getSnapshot().getBalle();
 		Pitch pitch = getSnapshot().getPitch();
 
 		if (robot.getPosition().getY() > target.getPosition().getY()) {
 			belowBall = false;
 		}
 
 		if (getSnapshot().getOpponentsGoal().isRightGoal())
 			belowBall = !belowBall;
 
 		Coord overshootCoord = calculateOvershootCoord(target, OVERSHOOT_GAP,
 				belowBall);
 
 		// If the point is in the pitch
 		if (pitch.containsCoord(overshootCoord))
 			// Return it as a new target
 			return new Point(overshootCoord);
 		// If its not in the pitch, pick a new one
 
 		overshootCoord = calculateOvershootCoord(target, OVERSHOOT_GAP,
 				!belowBall);
 		if (pitch.containsCoord(overshootCoord))
 			return new Point(overshootCoord);
 		// If the target is *still* out of pitch, go to the original target at
 		// least
 		else
 			return target;
 
 	}
 
 	protected boolean isApproachingTargetFromCorrectSide(
 			StaticFieldObject target) {
 		Robot robot = getSnapshot().getBalle();
 
 		Orientation robotToTargetOrientation = target.getPosition()
 				.sub(robot.getPosition()).orientation();
 
 		if ((getSnapshot().getOpponentsGoal().isLeftGoal())
 				&& (robotToTargetOrientation.degrees() > 90 + OVERSHOOT_ANGLE_EPSILON)
 				&& (robotToTargetOrientation.degrees() < 270 - OVERSHOOT_ANGLE_EPSILON)) {
 			return true;
 		} else if ((getSnapshot().getOpponentsGoal().isRightGoal())
 				&& ((robotToTargetOrientation.degrees() < 90 - OVERSHOOT_ANGLE_EPSILON) || (robotToTargetOrientation
 						.degrees() > 270 + OVERSHOOT_ANGLE_EPSILON))) {
 			return true;
 		} else
 			return false;
 
 	}
 
 	protected Point getAvoidanceTarget() {
 		Coord pointAbove = calculateAvoidanceCoord(AVOIDANCE_GAP, true);
 		Coord pointBelow = calculateAvoidanceCoord(AVOIDANCE_GAP, false);
 		Pitch pitch = getSnapshot().getPitch();
 
 		Coord currentPosition = getSnapshot().getBalle().getPosition();
 		if (pitch.containsCoord(pointAbove) && pitch.containsCoord(pointBelow)) {
 			// If both points happen to be in the pitch, return the closest one
 			double distToPointAbove = currentPosition.dist(pointAbove);
 			double distToPointBelow = currentPosition.dist(pointBelow);
 			double distDiff = Math.abs(distToPointAbove - distToPointBelow);
 
 			// If distances differ by much:
 			if (distDiff > DIST_DIFF_THRESHOLD) {
 				// Pick the shorter one
 				if (distToPointAbove < distToPointBelow)
 					return new Point(pointAbove);
 				else
 					return new Point(pointBelow);
 			} else {
 				double angleToTurnPointAbove = getSnapshot().getBalle()
 						.getAngleToTurnToTarget(pointAbove);
 				double angleToTurnPointBelow = getSnapshot().getBalle()
 						.getAngleToTurnToTarget(pointBelow);
 
 				if (Math.abs(angleToTurnPointAbove) < Math
 						.abs(angleToTurnPointBelow)) {
 					return new Point(pointAbove);
 				} else
 					return new Point(pointBelow);
 
 			}
 
 		} else if (pitch.containsCoord(pointAbove)) {
 			// Else if pitch contains only pointAbove, return it
 			return new Point(pointAbove);
 		} else
 			// if it doesn't contain pointAbove, it should contain pointBelow
 			return new Point(pointBelow);
 		// TODO: what happens if it does not contain both points?
 		// (it will return pointBelow now, but some other behaviour might be
 		// desired)
 
 	}
 
 	@Override
 	public void step(Controller controller) {
 		StaticFieldObject target = getTarget();
 
 		if ((getSnapshot() == null)
 				|| (getSnapshot().getBalle().getPosition() == null)
 				|| (target == null))
 			return;
 
 		// Update the current state of executor strategy
 		executorStrategy.updateState(getSnapshot());
 
 		if (shouldApproachTargetFromCorrectSide()
 				&& (!isApproachingTargetFromCorrectSide(target))) {
 			LOG.info("Approaching target from wrong side, calculating overshoot target");
 			target = getOvershootTarget(target);
 		}
 
 		// If we see the opponent
		if (getSnapshot().getOpponent() != null) {
 			Line pathToTarget = new Line(
 					getSnapshot().getBalle().getPosition(),
 					target.getPosition());
 			// Check if it is blocking our path
 			if (getSnapshot().getOpponent().intersects(pathToTarget)) {
 				// pick a new target then
 				LOG.info("Opponent is blocking the target, avoiding it");
 				target = getAvoidanceTarget();
 			}
 		}
 
 		// Update the target's location in executorStrategy (e.g. if target
 		// moved)
 		executorStrategy.updateTarget(target);
 		// Draw the target
 		if (target.getPosition() != null)
 			addDrawable(new Dot(target.getPosition(), getTargetColor()));
 
 		// If it says it is not finished, tell it to do something for a step.
 		if (!executorStrategy.isFinished()) {
 			executorStrategy.step(controller);
 		} else {
 			// Tell the strategy to stop doing whatever it was doing
 			executorStrategy.stop(controller);
 		}
 	}
 
 	@Override
 	public void stop(Controller controller) {
 		if (!executorStrategy.isFinished())
 			executorStrategy.stop(controller);
 
 	}
 }
