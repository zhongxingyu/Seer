 import lejos.nxt.Button;
 import lejos.robotics.subsumption.Arbitrator;
 import lejos.robotics.subsumption.Behavior;
 import position.CurrentPositionBox;
 import position.PositionFinder;
 import robot.Robot;
 import behaviors.Exit;
 import behaviors.FindBall;
 import behaviors.Kick;
 import behaviors.RotateToGoal;
 import behaviors.UpdatePosition;
 
 
 public class CompetitionType3 {
 	public static void main(String[] args) {
 		Robot robot = new Robot();
 		CurrentPositionBox positionBox = new CurrentPositionBox();
 		
 		robot.initialize(positionBox, new PositionFinder(robot).findPosition());
 		
 		Behavior behaviors[] = new Behavior[5];
 		behaviors[0] = new Kick(robot);
 		behaviors[1] = new RotateToGoal(robot, positionBox);
		behaviors[2] = new FindBall(robot);
 		behaviors[3] = new UpdatePosition(robot, positionBox);
 		behaviors[4] = new Exit();
 
 		Arbitrator arbiter = new Arbitrator(behaviors);
 		
 		Button.waitForAnyPress();
 		
 		arbiter.start();
 		
 	}
 }
