 package balle.strategy;
 
 import balle.controller.Controller;
 import balle.world.AbstractWorld;
 import balle.world.Coord;
 import balle.world.FieldObject;
 import balle.world.Orientation;
 import balle.world.Robot;
 
 public class GoToBall extends AbstractStrategy {
 
     private static final int    TURN_SPEED_DEGREES = 180;
     private static final double TURN_SPEED_RADIANS = (TURN_SPEED_DEGREES * Math.PI) / 180;
     private boolean             isMoving           = false;
     private long                startedTurning     = 0;
     private long                timeToTurn         = 0;
     private final static double DISTANCE_THRESHOLD = 0.2;
     private final static double EPSILON            = 0.00001;
     private final static double DISTANCE_DIFF      = 0.3;
    private final static int    SPEED_CONSTANT     = 500;
 
     public GoToBall(Controller controller, AbstractWorld world) {
         super(controller, world);
     }
 
     @Override
     protected void aiStep() {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public String toString() {
         return "Go to ball";
     }
 
     @Override
     protected void aiMove(Controller controller) {
         // Assume ball is static
         if (getSnapshot() == null)
             return;
 
         FieldObject ball = getSnapshot().getBall();
         Coord target = (ball != null) ? ball.getPosition() : null;
         Robot robot = getSnapshot().getBalle();
 
         Coord currentPosition = robot != null ? robot.getPosition() : null;
         if ((target == null) || (currentPosition == null)) {
             System.out.println("Cannot see ball or self");
             return;
         }
         // System.out.println("Ball " + target.getX() + " " + target.getY());
         if ((currentPosition.dist(target) - DISTANCE_THRESHOLD) < EPSILON) {
             if (isMoving) {
                 System.out.println("Target reached");
                 controller.stop();
                 controller.kick(); // For the lulz
                 isMoving = false;
             }
             return;
         } else {
 
             double angleToTarget = target.sub(currentPosition).orientation();
 
             Orientation currentOr = robot.getOrientation();
             if (currentOr == null) {
                 System.out.println("getOrientation is null");
                 if (isMoving) {
                     controller.stop();
                     isMoving = false;
                 }
                 return;
             }
             double currentOrientation = currentOr.atan2styleradians();
 
             double turnLeftAngle, turnRightAngle;
             if (angleToTarget > currentOrientation) {
                 turnLeftAngle = angleToTarget - currentOrientation;
                 turnRightAngle = currentOrientation
                         + (2 * Math.PI - angleToTarget);
             } else {
                 turnLeftAngle = (2 * Math.PI) - currentOrientation
                         + angleToTarget;
                 turnRightAngle = currentOrientation - angleToTarget;
             }
 
             double turnAngle;
 
             if (turnLeftAngle < turnRightAngle)
                 turnAngle = turnLeftAngle;
             else
                turnAngle = -turnRightAngle; // TODO these should be flipped in
                                             // controlelr!!
 
             double dist = target.dist(robot.getPosition());
             double distDiffFromTarget = Math.abs(Math.sin(turnAngle) * dist);
 
            if (Math.abs(distDiffFromTarget) > DISTANCE_DIFF) {
                // if (Math.abs(turnAngle) > Math.PI / 8) {
                 if (isMoving) {
                     controller.stop();
                     isMoving = false;
                 }
                 if (System.currentTimeMillis() - startedTurning > timeToTurn) {
                     timeToTurn = Math.round(Math.abs(turnAngle)
                             / (TURN_SPEED_RADIANS * 0.001))
                             + SPEED_CONSTANT;
 
                     System.out.println("Turning " + turnAngle + " should take "
                             + timeToTurn + " ms");
                     controller.rotate((int) (turnAngle * 180 / Math.PI), 180);
                     startedTurning = System.currentTimeMillis();
                 }
             } else {
                 if (!isMoving) {
                     startedTurning = 0;
                     controller.forward(500);
                     isMoving = true;
                 }
             }
 
            System.out.println("Angle to target unadjusted: "
                    + (((angleToTarget * 180) / Math.PI)));
            System.out.println("Current orientation: "
                    + (robot.getOrientation().atan2styledegrees()));
            System.out.println("Turn angle " + ((turnAngle * 180)) / Math.PI);
 
         }
 
     }
 }
