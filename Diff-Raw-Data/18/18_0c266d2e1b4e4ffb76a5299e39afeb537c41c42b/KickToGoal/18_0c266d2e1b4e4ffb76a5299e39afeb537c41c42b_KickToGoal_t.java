 package balle.strategy.planner;
 
 import org.apache.log4j.Logger;
 
 import balle.controller.Controller;
 import balle.world.Snapshot;
 import balle.world.objects.Ball;
 import balle.world.objects.Goal;
 import balle.world.objects.Robot;
 
 /**
  * Strategy that tries to kick the ball into opponents goal
  */
 public class KickToGoal extends AbstractPlanner {
     private static Logger LOG = Logger.getLogger(KickToGoal.class);
     public KickToGoal() {
         // TODO Auto-generated constructor stub
     }
 
     @Override
     protected void onStep(Controller controller, Snapshot snapshot) {
         Robot ourRobot = snapshot.getBalle();
         Robot opponent = snapshot.getOpponent();
         Ball ball = snapshot.getBall();
         Goal goal = snapshot.getOpponentsGoal();
 
         // Skip potential nullpointer exceptions
         if ((ourRobot == null) || (ball == null)) {
             return;
         }
         
         if (!ourRobot.isFacingGoalHalf(goal)) {
             LOG.info("Robot is not facing the correct goal, turning");
             // TODO: turn as much as possible while still maintaining possession
             // or back off and go around the ball
         } else {
             if ((ourRobot.isFacingGoal(goal) && (opponent == null))
                     || (ourRobot.isInScoringPosition(ball, goal, opponent))) {
                 LOG.info("We can score, kicking");
                 controller.kick();
                 // Slowly move towards the ball if we missed it
                 controller.setWheelSpeeds(200, 200);
             } else if (ourRobot.isFacingGoal(goal)) {
                 LOG.info("We are facing goal, but opponent is blocking the shot");
                 // TODO: Turn slightly here (a few degrees) to trigger the
                 // bounce from wall
                 // kick in the else statement below
                 // Use IncFaceAngle executor here
                 // SOme code that could be useful:
 
                 // TODO: turn the robot slightly so we face away from our
                 // own goal.
                 // Implement a turning executor that would use
                 // setWheelSpeeds to some arbitrary low
                 // number (say -300,300 and 300,-300) to turn to correct
                 // direction and use it here.
                 // it has to be similar to FaceAngle executor but should not
                 // use the controller.rotate()
                 // command that is blocking.
 
                 // Coord r, b, g;
                 // r = ourRobot.getPosition();
                 // b = ball.getPosition();
                 // g = ownGoal.getPosition();
                 //
                 // if (r.angleBetween(g, b).atan2styleradians() < 0) {
                 // // Clockwise.
                 // Orientation orien = ourRobot
                 // .findMaxRotationMaintaintingPossession(ball, true);
                 // System.out.println(orien);
                 // turningExecutor.setTargetOrientation(orien);
                 // turningExecutor.step(controller, snapshot);
                 // if (ourRobot.findMaxRotationMaintaintingPossession(ball,
                 // true).degrees() < 10)
                 // controller.kick();
                 // } else {
                 // // Anti-Clockwise
                 // Orientation orien = ourRobot
                 // .findMaxRotationMaintaintingPossession(ball, false);
                 // System.out.println(orien);
                 // turningExecutor.setTargetOrientation(orien);
                 // turningExecutor.step(controller, snapshot);
                 // if (ourRobot.findMaxRotationMaintaintingPossession(ball,
                 // false).degrees() > -10)
                 // controller.kick();
                 // }
                 //
             } else {
                 LOG.info("We're not facing goal");
                 // TODO: Check if it is better to kick the ball from this
                 // position
                 // and hopefully bounce from the wall, or whether it is better
                 // to turn towards the goal
             }
            LOG.warn("Kicking anyway");
            // TODO: remove
            controller.kick();
         }
 
     }
 
 }
