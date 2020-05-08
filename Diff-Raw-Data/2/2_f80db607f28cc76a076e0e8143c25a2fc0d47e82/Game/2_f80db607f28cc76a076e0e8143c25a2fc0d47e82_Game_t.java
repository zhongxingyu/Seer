 package balle.strategy.friendly1;
 
 import org.apache.log4j.Logger;
 
 import balle.controller.Controller;
 import balle.strategy.FactoryMethod;
 import balle.strategy.Strategy;
 import balle.strategy.executor.movement.GoToObjectPFN;
 import balle.strategy.executor.turning.IncFaceAngle;
 import balle.strategy.executor.turning.RotateToOrientationExecutor;
 import balle.strategy.planner.AbstractPlanner;
 import balle.strategy.planner.KickFromWall;
 import balle.world.Coord;
 import balle.world.Orientation;
 import balle.world.Snapshot;
 import balle.world.objects.Ball;
 import balle.world.objects.Goal;
 import balle.world.objects.Pitch;
 import balle.world.objects.Robot;
 
 public class Game extends AbstractPlanner {
 
     private static final Logger LOG = Logger.getLogger(Game.class);
     // Strategies that we will need make sure to updateState() for each of them
     // and stop() each of them
     Strategy defensiveStrategy;
     Strategy goToBallStrategy;
     Strategy pickBallFromWallStrategy;
     RotateToOrientationExecutor turningExecutor;
 
     public Game() {
         defensiveStrategy = new DefensiveStrategy(new GoToObjectPFN(0.15f));
         // TODO: implement a new strategy that inherits from GoToBall but always
         // approaches the ball from correct angle. (This can be done by always
         // pointing robot
         // to a location that is say 0.2 m before the ball in correct direction
         // and then, once the robot reaches it, pointing it to the ball itself
         // so it reaches it.
         goToBallStrategy = new GoToBall(new GoToObjectPFN(0.15f), true);
         // TODO: UPDATE THIS
         pickBallFromWallStrategy = new KickFromWall(new GoToObjectPFN(0.15f));
         turningExecutor = new IncFaceAngle();
     }
 
     @Override
     public void stop(Controller controller) {
         defensiveStrategy.stop(controller);
         goToBallStrategy.stop(controller);
     }
 
     @Override
     public void onStep(Controller controller, Snapshot snapshot) {
 
         Robot ourRobot = snapshot.getBalle();
        if (ourRobot.getPosition() == null)
            return;
         Robot opponent = snapshot.getOpponent();
         Ball ball = snapshot.getBall();
         Goal ownGoal = snapshot.getOwnGoal();
         Goal opponentsGoal = snapshot.getOpponentsGoal();
         Pitch pitch = snapshot.getPitch();
 
         Orientation targetOrientation = ball.getPosition()
                 .sub(ourRobot.getPosition()).orientation();
 
         if (ourRobot.possessesBall(ball)) {
             // Kick if we are facing opponents goal
             if (!ourRobot.isFacingGoalHalf(ownGoal)) {
                 LOG.info("Kicking the ball");
                 controller.kick();
                 // Slowly move forward as well in case we're not so close
                 controller.setWheelSpeeds(200, 200);
             } else {
 
                 // TODO: turn the robot slightly so we face away from our
                 // own goal.
                 // Implement a turning executor that would use
                 // setWheelSpeeds to some arbitrary low
                 // number (say -300,300 and 300,-300) to turn to correct
                 // direction and use it here.
                 // it has to be similar to FaceAngle executor but should not
                 // use the controller.rotate()
                 // command that is blocking.
 
                 Coord r, b, g;
                 r = ourRobot.getPosition();
                 b = ball.getPosition();
                 g = ownGoal.getPosition();
 
                 if (r.angleBetween(g, b).atan2styleradians() < 0) {
                     // Clockwise.
                     Orientation orien = ourRobot
                             .findMaxRotationMaintaintingPossession(ball, true);
                     System.out.println(orien);
                     turningExecutor.setTargetOrientation(orien);
                     turningExecutor.step(controller, snapshot);
                     if (ourRobot.findMaxRotationMaintaintingPossession(ball,
                             true).degrees() < 10)
                         controller.kick();
                 } else {
                     // Anti-Clockwise
                     Orientation orien = ourRobot
                             .findMaxRotationMaintaintingPossession(ball, false);
                     System.out.println(orien);
                     turningExecutor.setTargetOrientation(orien);
                     turningExecutor.step(controller, snapshot);
                     if (ourRobot.findMaxRotationMaintaintingPossession(ball,
                             false).degrees() > -10)
                         controller.kick();
                 }
             }
         } else if ((opponent.possessesBall(ball))
                 && (opponent.isFacingGoal(ownGoal))) {
             LOG.info("Defending");
             // Let defensiveStrategy deal with it!
             defensiveStrategy.step(controller, snapshot);
             addDrawables(defensiveStrategy.getDrawables());
         } else if (ball.isNearWall(pitch)) {
             pickBallFromWallStrategy.step(controller, snapshot);
             addDrawables(pickBallFromWallStrategy.getDrawables());
         } else if (ball.isNear(ourRobot)
                 && (ourRobot.isApproachingTargetFromCorrectSide(ball,
                         snapshot.getOpponentsGoal()))) {
             if (Math.abs(ourRobot.getAngleToTurn(targetOrientation)) > (Math.PI / 4)) {
                 LOG.info("Ball is near our robot, turning to it");
                 turningExecutor.setTargetOrientation(targetOrientation);
                 turningExecutor.step(controller, snapshot);
             } else {
                 // Go forward!
                 controller.setWheelSpeeds(400, 400);
             }
         } else {
             // Approach ball
             goToBallStrategy.step(controller, snapshot);
             addDrawables(goToBallStrategy.getDrawables());
 
         }
 
     }
 
     @FactoryMethod(designator = "Friendly1", parameterNames = {})
     public static Game factoryMethod() {
         return new Game();
     }
 }
