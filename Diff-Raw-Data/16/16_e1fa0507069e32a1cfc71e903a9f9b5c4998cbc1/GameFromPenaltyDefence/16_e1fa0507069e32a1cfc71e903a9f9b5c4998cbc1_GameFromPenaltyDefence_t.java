 package balle.strategy;
 
 import balle.controller.Controller;
 import balle.world.objects.Ball;
 import balle.world.objects.Goal;
 import balle.world.objects.Robot;
 
 public class GameFromPenaltyDefence extends Game {
 	double lastknownxballpos;
 
 	public GameFromPenaltyDefence() throws UnknownDesignatorException {
 		super();
 	}
 
 	/**
 	 * Checks if robot is is still defending the penalty;
 	 * 
 	 * @return
 	 */
 	public boolean isStillInPenaltyDefence() {
 		Ball ball = getSnapshot().getBall();
 		Goal ownGoal = getSnapshot().getOwnGoal();
 		// Robot opponent = getSnapshot().getOpponent();
 		// fix if boolean expression for when penalty saved or scored
 		if (!ball.intersects(ownGoal.getGoalLine())
 				&& !(ball.getVelocity().getX() > 0 || ball.getVelocity().getY() > 0)) {
 			return true;
 		} else {
 
 			return false;
 		}
 	}
 
 	@Override
 	public void step(Controller controller) {
		double ourypossition = 0, bcrosspointy = 0, y = 0, travely = 0;
 		if (isStillInPenaltyDefence()) {
 			Robot ourRobot = getSnapshot().getBalle();
 			Robot opponent = getSnapshot().getOpponent();
 			Ball ball = getSnapshot().getBall();
 			Goal ownGoal = getSnapshot().getOwnGoal();
 
			ourypossition = ourRobot.getPosition().getY();
 
			bcrosspointy = ourypossition;
 			if (ourRobot.getFacingLineBothWays().intersects(
 					opponent.getBallKickLine(ball))) {
 				bcrosspointy = ourRobot.getFacingLineBothWays()
 						.getIntersect(opponent.getBallKickLine(ball)).getY();
 			}
			y = ourRobot.getFacingLineBothWays()
 					.getIntersect(opponent.getBallKickLine(ball)).getY();
 
			travely = (bcrosspointy * 10 - ourypossition * 10);
 
 			if (bcrosspointy >= ourRobot.getPosition().getY()
 					&& bcrosspointy <= ourRobot.getPosition().getY() + 0.02) {
 				controller.stop();
 			} else {
 				if (y > ownGoal.getMinY() && y < ownGoal.getMaxY()) {
 					// add if robot betwee y of goal
 					if (travely > 0) {
 
 						controller.forward(580);
 
 					} else {
 
 						controller.backward(580);
 
 					}
 				} else {
 					controller.stop();
 				}
 
 			}
 
 		} else {
 			super.step(controller);
 		}
 	}
 }
