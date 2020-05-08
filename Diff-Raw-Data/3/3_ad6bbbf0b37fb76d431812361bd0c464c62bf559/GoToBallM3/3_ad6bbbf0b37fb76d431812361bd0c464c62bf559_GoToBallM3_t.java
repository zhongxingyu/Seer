 package balle.strategy.planner;
 
 import java.awt.Color;
 
 import org.apache.log4j.Logger;
 
 import balle.controller.Controller;
 import balle.main.drawable.DrawableLine;
 import balle.misc.Globals;
 import balle.strategy.executor.movement.GoToObject;
 import balle.strategy.executor.movement.GoToObjectPFN;
 import balle.strategy.executor.movement.MovementExecutor;
 import balle.strategy.executor.turning.FaceAngle;
 import balle.strategy.executor.turning.RotateToOrientationExecutor;
 import balle.world.Coord;
 import balle.world.Line;
 import balle.world.Snapshot;
 import balle.world.objects.Ball;
 import balle.world.objects.Goal;
 import balle.world.objects.Pitch;
 import balle.world.objects.Point;
 import balle.world.objects.Robot;
 import balle.world.objects.StaticFieldObject;
 
 public class GoToBallM3 extends GoToBall {
     private int stage;
     private static final double BALL_SAFE_GAP = 0.25;
     private RotateToOrientationExecutor turnExecutor = new FaceAngle();
 
     private static Logger LOG = Logger.getLogger(GoToBallM3.class);
 
     public GoToBallM3() {
         super(new GoToObjectPFN(0), true);
         stage = 0;
     }
 
     @Override
 	protected StaticFieldObject getTarget(Snapshot snapshot) {
 		Ball ball = snapshot.getBall();
 		Goal targetGoal = snapshot.getOpponentsGoal();
 
         Line targetLine = new Line(targetGoal.getPosition(), ball.getPosition());
 
         if (stage < 2)
         {
 
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
 			Line ballTargetLine = new Line(snapshot.getBall()
                     .getPosition(), targetCoord);
 			Coord ourPos = snapshot.getBalle().getPosition();
 
             if (ballTargetLine.contains(ourPos)) {
                 LOG.warn("Were fucked, TODO get out!");
             }
         }
         addDrawable(new DrawableLine(targetLine, Color.ORANGE));
         return new Point(targetLine.getB());
     }
 
     public void setAppropriateMovementStrategy()
     {
         if (stage == 1)
         {
             LOG.info("Going to BALL_SAFE target");
             setExecutorStrategy(new GoToObjectPFN(0));
         }
         else
         {
             LOG.info("Going to the ball");
             MovementExecutor strategy = new GoToObject(new FaceAngle());
 			strategy.setStopDistance(0.00);
             setExecutorStrategy(strategy);
         }
     }
 
 	public void changeStages(Controller controller, Snapshot snapshot) {
         Robot ourRobot = snapshot.getBalle();
         Coord ourPosition = ourRobot.getPosition();
         
         if (ourPosition == null)
             return;
 
 		if (stage == 1
 				&& ourRobot.containsCoord(getTarget(snapshot).getPosition())) {
             stage = 2;
             setAppropriateMovementStrategy();
         }
         if (stage == 2) {
 			if (ourRobot.getPosition().dist(snapshot.getBall().getPosition()) < (Globals.ROBOT_LENGTH + 0.05)
 					&& ourRobot.getFacingLine().intersects(
 							snapshot.getOpponentsGoal().getGoalLine())) {
                 LOG.info("Kicking");
                 controller.kick();
 				controller.setWheelSpeeds(200, 200);
                 return;
             }
         }
     }
 
 	@Override
 	protected void onStep(Controller controller, Snapshot snapshot) {
         if (stage == 0)
         {
 			Robot ourRobot = snapshot.getBalle();
             if ((ourRobot.getPosition() == null)
                     || (ourRobot.getOrientation() == null))
                 return;
 
 			Coord targetCoord = getTarget(snapshot).getPosition();
 
             double angleToFaceTarget = ourRobot
                     .getAngleToTurnToTarget(targetCoord);
             
             if (Math.abs(angleToFaceTarget) > Math.PI / 8) {
 				turnExecutor.setTargetOrientation(targetCoord.sub(
 						ourRobot.getPosition()).orientation());
                 LOG.info("Turning to target");
 				turnExecutor.step(controller, snapshot);
             } else {
                 LOG.info("Facing the target correctly");
                 turnExecutor.stop(controller);
                 stage = 1;
             }
         } else {
 			changeStages(controller, snapshot);
 			super.onStep(controller, snapshot);
         }
     }
 
 }
