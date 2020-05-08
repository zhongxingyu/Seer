 package turningTable;
 
 import common.Robot;
 import common.Strategy;
 import common.StrategyBehavior;
 
 import turningTable.TableTurner;
 
 import lejos.robotics.subsumption.Behavior;
 import lejos.robotics.subsumption.Arbitrator;
 
 public class EnterBox extends StrategyBehavior {
 	private Robot robot;
 	private boolean passedBox;
 	
 	public EnterBox(Robot robot, Strategy parent) {
 		super(parent);
 		this.robot = robot;
 		this.passedBox = false;
 	}
 
 	@Override
 	public boolean wantsToWork() {
 		return !passedBox && robot.sonar.getDistance() < 40 && !robot.isLineBeneath();
 	}
 
 	@Override
 	public void work() {
		robot.arm.alignLeft();
 		int leftPreDistance = robot.sonar.getDistance();
		robot.arm.alignRight();
 		int rightPreDistance = robot.sonar.getDistance();
 		
 		robot.pilot.forward();
 		while (!suppressed && !robot.leftTouch.isPressed() && !robot.rightTouch.isPressed());
 		robot.pilot.stop();
 		robot.pilot.travel(-7);
 		robot.pilot.steer(200, 180);
		robot.arm.alignCenter();
 		if (robot.sonar.getDistance() == 255) {
			robot.arm.alignLeft();
 			TableTurner.client.turnClockwise(45);
 			robot.pilot.setTravelSpeed(4);
 			robot.pilot.forward();
 			while (!suppressed && robot.sonar.getDistance() > );
 			robot.pilot.stop();
 			
 			robot.pilot.setTravelSpeed(robot.pilot.getMaxTravelSpeed());
 			TableTurner.client.turnClockwise(45);
 			while (!suppressed && !robot.isLineBeneath() && robot.sonar.getDistance() != 255);
 			robot.pilot.forward();
 			passedBox = true;
 		}
 		else {
 			robot.pilot.travel(10.0);
 			robot.pilot.travel(-10.0);
 			parent.stop();
 		}
 	}
 }
