 package balle.world.objects;
 
 import balle.misc.Globals;
 import balle.world.Coord;
 import balle.world.Line;
 import balle.world.Orientation;
 import balle.world.Velocity;
 
 public class Robot extends RectangularObject {
 
     public Robot(Coord position, Velocity velocity, Orientation orientation) {
         super(position, velocity, orientation, Globals.ROBOT_WIDTH,
                 Globals.ROBOT_LENGTH);
     }
 
     /**
      * Returns true if the robot is in possession of the ball. That is if the
      * ball is close enough to the kicker that it can kick it.
      * 
      * @param ball
      * @return true, if robot is in possession of the ball
      */
     public boolean possessesBall(Ball ball) {
         if ((ball.getPosition() == null) || (getPosition() == null))
             return false;
 
         double distance = getFrontSide().dist(ball.getPosition());
         return distance <= Globals.ROBOT_POSSESS_DISTANCE + ball.getRadius();
     }
 
     /**
      * Returns the line that would represents the path of the ball if the robot
      * kicked it
      * 
      * @param ball
      * @return
      */
     public Line getBallKickLine(Ball ball) {
         double x0, y0, x1, y1;
         x0 = ball.getPosition().getX();
         y0 = ball.getPosition().getY();
 
         Coord target = new Coord(Globals.ROBOT_MAX_KICK_DISTANCE, 0);
         target = target.rotate(getOrientation());
 
         x1 = x0 + target.getX();
         y1 = y0 + target.getY();
 
         return new Line(x0, y0, x1, y1);
     }
 
     /**
      * Returns that represents the robot's facing direction
      * 
      * @return the facing line
      */
     public Line getFacingLine() {
         double x0, y0, x1, y1;
         x0 = getPosition().getX();
         y0 = getPosition().getY();
 
         Coord target = new Coord(Globals.PITCH_WIDTH, 0);
         target = target.rotate(getOrientation());
 
         x1 = x0 + target.getX();
         y1 = y0 + target.getY();
 
         return new Line(x0, y0, x1, y1);
     }
 
     /**
      * Gets the facing line of the robot. Similar to the getFacingLine but the
      * line returned stretches both forward and backward from the robot.
      * 
      * @return the facing line both ways
      */
     public Line getFacingLineBothWays() {
         double x0, y0, x1, y1;
 
         Coord target = new Coord(Globals.PITCH_WIDTH, 0);
         target = target.rotate(getOrientation());
 
         x0 = getPosition().getX() - target.getX();
         y0 = getPosition().getY() - target.getY();
         x1 = getPosition().getX() + target.getX();
         y1 = getPosition().getY() + target.getY();
 
         return new Line(x0, y0, x1, y1);
     }
 
     /**
      * Checks if the robot can score from this position. That is if it is in
      * possession of the ball and facing the goal and other robot is is not
      * blocking the path to the goal.
      * 
      * @param ball
      * @param goal
      * @param otherRobot
      * @return true, if is in scoring position
      */
     public boolean isInScoringPosition(Ball ball, Goal goal, Robot otherRobot) {
         return possessesBall(ball) && isFacingGoal(goal)
                && ((otherRobot.getPosition() != null) && (!otherRobot
                        .intersects(getFacingLine())));
     }
 
     /**
      * Returns true if robot is facing the goal. Similar to isInScoringPosition
      * but does not check whether a robot has a ball and whether it is blocked
      * by other robot.
      * 
      * @param goal
      * @return
      */
     public boolean isFacingGoal(Goal goal) {
 
         if (getPosition() == null)
             return false;
 
         Line goalLine = goal.getGoalLine();
         Line facingLine = getFacingLine();
 
         return facingLine.intersects(goalLine);
 
     }
 
     /**
      * TODO write test
      * 
      * @return True, if robot is facing left.
      */
     public boolean isFacingLeft() {
         if (getOrientation() == null)
             return false;
 
         return (getOrientation().degrees() > 90)
                 && (getOrientation().degrees() < 270);
     }
 
     /**
      * TODO write test
      * 
      * @return True, if the robot is facing right
      */
     public boolean isFacingRight() {
         if (getOrientation() == null)
             return false;
 
         return (getOrientation().degrees() < 90)
                 || (getOrientation().degrees() > 270);
     }
 
     /**
      * Returns true if this robot is facing to the half of the pitch that this
      * goal is present.
      * 
      * TODO write test
      * 
      * @param goal
      * @return
      */
     public boolean isFacingGoalHalf(Goal goal) {
         return isFacingLeft() == goal.isLeftGoal();
     }
 
     /**
      * Returns the angle required to turn using atan2 style radians. Positive
      * angle means to turn CCW this much radians, whereas negative means turning
      * CW that amount of radians.
      * 
      * @param currentOrientation
      *            current orientation of the robot
      * @param targetOrientation
      *            target orientation
      * @return the angle to turn
      */
     public double getAngleToTurn(Orientation targetOrientation) {
         Orientation currentOr = getOrientation();
         if ((currentOr == null) || (targetOrientation == null))
             return 0;
 
         double angleToTarget = targetOrientation.atan2styleradians();
         double currentOrientation = currentOr.atan2styleradians();
 
         double turnLeftAngle, turnRightAngle;
         if (angleToTarget > currentOrientation) {
             turnLeftAngle = angleToTarget - currentOrientation;
             turnRightAngle = currentOrientation + (2 * Math.PI - angleToTarget);
         } else {
             turnLeftAngle = (2 * Math.PI) - currentOrientation + angleToTarget;
             turnRightAngle = currentOrientation - angleToTarget;
         }
 
         double turnAngle;
 
         if (turnLeftAngle < turnRightAngle)
             turnAngle = turnLeftAngle;
         else
             turnAngle = -turnRightAngle;
 
         return turnAngle;
     }
 
     /**
      * Returns the angle the robot has to turn to face the target coordinate
      * 
      * @param targetCoord
      *            the target coordinate
      * @return the angle to turn to target
      */
     public double getAngleToTurnToTarget(Coord targetCoord) {
         Coord currentPosition = getPosition();
         if ((currentPosition == null) || (targetCoord == null))
             return 0;
 
         return getAngleToTurn(targetCoord.sub(currentPosition).orientation());
     }
 
     public Coord getFrontLeftCornerCoord() {
         Coord leftSide = new Coord(getHeight() / 2, -getWidth() / 2);
         leftSide = leftSide.rotate(getOrientation());
         return getPosition().add(leftSide);
     }
 
     public Coord getFrontRightCornerCoord() {
         Coord rightSide = new Coord(getHeight() / 2, getWidth() / 2);
         rightSide = rightSide.rotate(getOrientation());
         return getPosition().add(rightSide);
     }
 
     public boolean canReachTargetInStraightLine(FieldObject target, FieldObject obstacle) {
         if (getPosition() == null)
             return false;
 
         Line pathToTarget1 = new Line(getPosition(), target.getPosition());
         Line pathToTarget2 = new Line(getFrontLeftCornerCoord(),
                 target.getPosition());
         Line pathToTarget3 = new Line(getFrontRightCornerCoord(),
                 target.getPosition());
 
         // Check if it is blocking our path
         return (!obstacle.intersects(pathToTarget1)
                 && !obstacle.intersects(pathToTarget2) && !obstacle
                 .intersects(pathToTarget3));
     }
 
     public boolean isApproachingTargetFromCorrectSide(FieldObject target,
             Goal opponentsGoal) {
         if (getPosition() == null)
             return false;
         if (target.getPosition() == null)
             return true;
 
         Orientation robotToTargetOrientation = target.getPosition()
                 .sub(getPosition()).orientation();
 
         if (opponentsGoal.isLeftGoal()
                 && (robotToTargetOrientation.degrees() > 90 + Globals.OVERSHOOT_ANGLE_EPSILON)
                 && (robotToTargetOrientation.degrees() < 270 - Globals.OVERSHOOT_ANGLE_EPSILON)) {
             return true;
         } else if ((opponentsGoal.isRightGoal())
                 && ((robotToTargetOrientation.degrees() < 90 - Globals.OVERSHOOT_ANGLE_EPSILON) || (robotToTargetOrientation
                         .degrees() > 270 + Globals.OVERSHOOT_ANGLE_EPSILON))) {
             return true;
         } else
             return false;
 
     }
 
     /**
      * TODO TEST!!!!!!
      * 
      * @param ourRobot
      * @param ball
      * @param cw
      *            Clock-wise rotation if true, CCW if false.
      * @return
      */
     public Orientation findMaxRotationMaintaintingPossession(Ball ball,
             boolean cw) {
         Coord fl = new Coord(10, 0);
         fl = fl.rotate(getOrientation()).add(getPosition());
         Orientation max, o = getPosition().angleBetween(fl, ball.getPosition());
 
         if (cw) {
             max = (new Coord(0, 0)).angleBetween(new Coord(10, 0), new Coord(
                     Globals.ROBOT_LENGTH, Globals.ROBOT_WIDTH));
         } else {
             max = (new Coord(0, 0)).angleBetween(new Coord(10, 0), new Coord(
                     Globals.ROBOT_LENGTH, -Globals.ROBOT_WIDTH));
         }
         System.out.println("max = " + max + ",\to = " + o + ",\tm-o = "
                 + max.sub(o));
 
         return max.sub(o);
 
     }
 }
