 package balle.strategy.planner;
 
 import java.awt.Color;
 
 import org.apache.log4j.Logger;
 
 import balle.controller.Controller;
 import balle.main.drawable.DrawableLine;
 import balle.main.drawable.Label;
 import balle.misc.Globals;
 import balle.strategy.FactoryMethod;
 import balle.strategy.executor.movement.GoToObjectPFN;
 import balle.strategy.executor.movement.MovementExecutor;
 import balle.strategy.executor.turning.FaceAngle;
 import balle.strategy.executor.turning.RotateToOrientationExecutor;
 import balle.world.Coord;
 import balle.world.Line;
 import balle.world.Orientation;
 import balle.world.Snapshot;
 import balle.world.objects.Ball;
 import balle.world.objects.FieldObject;
 import balle.world.objects.Goal;
 import balle.world.objects.Pitch;
 import balle.world.objects.Point;
 import balle.world.objects.Robot;
 
 public class GoToBallSafe extends GoToBall {
     private static final double BALL_SPEED_THRESHOLD = 0.01 / 1000; // m/ms
     private static final double BALL_SAFE_GAP = 0.4;
     private static final double ANGLE_THRESHOLD = Math.PI / 32;
     private final RotateToOrientationExecutor turnExecutor;
 
     private static Logger LOG = Logger.getLogger(GoToBallSafe.class);
 
     public GoToBallSafe() {
         super(new GoToObjectPFN(0), true);
         turnExecutor = new FaceAngle(ANGLE_THRESHOLD);
         setExecutorStrategy(new GoToObjectPFN(0));
     }
 
     protected int getStage(Snapshot snapshot) {
         FieldObject originalTarget = getOriginalTarget(snapshot);
 
         Line targetGoalLine = new Line(snapshot.getBalle().getPosition(),
                 originalTarget.getPosition());
         targetGoalLine = targetGoalLine.extend(Globals.PITCH_WIDTH);
 
         int stage;
         if ((originalTarget.getPosition().dist(
                 snapshot.getBalle().getPosition()) < BALL_SAFE_GAP * 1.25)
             &&
                 (targetGoalLine.intersects(snapshot.getOpponentsGoal()
                         .getGoalLine()))) {
             boolean facingBall = snapshot.getBalle().getAngleToTurnToTarget(
                     originalTarget.getPosition()) < ANGLE_THRESHOLD;
             boolean ballIsMovingFast = originalTarget.getVelocity().abs() > BALL_SPEED_THRESHOLD;
 
             if (ballIsMovingFast)
                 LOG.warn("Ball is moving too fast to approach");
             if ((!facingBall && !ballIsMovingFast)
                 || (turnExecutor.isTurning()))
                 stage = 2;
             else
                 stage = 3;
             
         }
         else
             stage = 1;
 
         addDrawable(new Label("Stage: " + Integer.toString(stage), new Coord(0,
                 -0.1), Color.RED));
         return stage;
     }
 
     protected FieldObject getOriginalTarget(Snapshot snapshot) {
         return snapshot.getBall();
     }
 
     @Override
     protected FieldObject getTarget(Snapshot snapshot) {
         FieldObject ball = getOriginalTarget(snapshot);
         if (ball.getPosition() == null) {
             LOG.warn("Cannot see the ball");
             return null;
         }
         Goal targetGoal = snapshot.getOpponentsGoal();
 
         Line targetLine = new Line(targetGoal.getPosition(), ball.getPosition());
 
         int stage = getStage(snapshot);
         if (stage < 3) {
             Pitch pitch = snapshot.getPitch();
             double ballSafeGap = BALL_SAFE_GAP;
             Line newTargetLine = targetLine.extend(ballSafeGap);
             while (ballSafeGap > 0.01
                     && !pitch.containsCoord(newTargetLine.extend(
                             Globals.ROBOT_LENGTH).getB())) {
                 ballSafeGap *= 0.95;
                 newTargetLine = targetLine.extend(ballSafeGap);
             }
             targetLine = newTargetLine;
         }
 
         if (stage == 1) {
             Coord targetCoord = targetLine.getB();
             Line ballTargetLine = new Line(snapshot.getBall().getPosition(),
                     targetCoord);
             Coord ourPos = snapshot.getBalle().getPosition();
 
             if (ballTargetLine.contains(ourPos)) {
                 LOG.warn("Were fucked, TODO get out!");
             }
         }
         addDrawable(new DrawableLine(targetLine, Color.ORANGE));
         if (stage == 3)
             return ball; // Return the ball itself so we can use the velocity
                          // information
         else
             return new Point(targetLine.getB());
     }
 
     public void setAppropriateMovementStrategy(boolean correctAngle, int stage) {
         if (stage == 1) {
             LOG.info("Going to BALL_SAFE target");
             setApproachTargetFromCorrectSide(correctAngle);
            setExecutorStrategy(new GoToObjectPFN(0, true));
         } else if (stage == 3) {
             LOG.info("Going to the ball");
             MovementExecutor executor = new  GoToObjectPFN(0, true);
             executor.setStopDistance(0);
             setExecutorStrategy(executor);
             setApproachTargetFromCorrectSide(false);
         }
     }
 
     private void changeStage(int newStage, boolean correctAngle) {
         setAppropriateMovementStrategy(correctAngle, newStage);
     }
 
     private void changeStage(int newStage) {
         changeStage(newStage, true);
     }
 
     @Override
     protected void onStep(Controller controller, Snapshot snapshot) {
         Robot ourRobot = snapshot.getBalle();
         Ball ball = snapshot.getBall();
 
         if ((ourRobot.getPosition() == null)
                 || (ourRobot.getOrientation() == null)
                 || (ball.getPosition() == null))
             return;
 
         int stage = getStage(snapshot);
         changeStage(stage);
         if (stage == 2) {
             if (turnExecutor.isTurning()) {
                 turnExecutor.step(controller, snapshot);
                 LOG.trace("Still turning");
                 return;
             } else {
                 Orientation targetOrientation = ball
                         .getPosition().sub(ourRobot.getPosition())
                         .orientation();
                 LOG.info("Turning towards ball!");
                 turnExecutor.setTargetOrientation(targetOrientation);
                 turnExecutor.step(controller, snapshot);
                 return;
             }
 
        } else if ((stage == 1) || (stage == 3)) {
             if (ourRobot.isApproachingTargetFromCorrectSide(ball,
                     snapshot.getOpponentsGoal())) {
                 setAppropriateMovementStrategy(false, stage);
             } else {
                 setAppropriateMovementStrategy(true, stage);
             }
 
         }
         super.onStep(controller, snapshot);
     }
 
     @FactoryMethod(designator = "GoToBallSafe")
     public static GoToBallSafe factoryMethod() {
         return new GoToBallSafe();
     }
 
 }
