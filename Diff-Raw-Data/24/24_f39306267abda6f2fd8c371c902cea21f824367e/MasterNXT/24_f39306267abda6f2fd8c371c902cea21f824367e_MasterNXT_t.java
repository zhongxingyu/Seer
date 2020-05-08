 /**
  * 
  */
 package master;
 
 import java.io.IOException;
 
 import common.IDefender;
 import common.Instructions;
 import common.ParseInstructions;
 import common.PlayerRole;
 
 import lejos.nxt.Button;
 import lejos.nxt.LCD;
 import lejos.nxt.SensorPort;
 import lejos.nxt.Sound;
 import lejos.nxt.UltrasonicSensor;
 import master.USLocalizer.LocalizationType;
 
 /**
  * @author alex
  * 
  */
 public class MasterNXT {
 	
 	private static SensorPort usPort = SensorPort.S4;
 	private static SensorPort leftLsPort = SensorPort.S1;
 	private static SensorPort rightLsPort = SensorPort.S2;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		Instructions instructions = new Instructions();
 		IRobot robot = new YARRobot();
 		UltrasonicSensor us = new UltrasonicSensor(usPort);
 		USPoller poller = new USPoller(us);
 		Odometer odo = new Odometer(robot, true, poller);
 		OdometryDisplay disp = new OdometryDisplay(odo);
 		INavigator nav = odo.getNavigator();
 		USLocalizer localizer = new USLocalizer(odo, us, LocalizationType.FALLING_EDGE);
 
 		// clear the display
 		LCD.clear();
 
 		// ask the user whether the motors should drive in a square or
 		// float
 		LCD.drawString("Press any button ", 0, 0);
 		LCD.drawString("    to start     ", 0, 1);
 
 		Button.waitForAnyPress();
 		disp.start();
 
 		// Receive instructions from server
 		// TODO uncomment ParseInstructions.parse(null); // Replace null with the data input
 										// stream
 		// Send instructions to slave brick
 		instructions.w1 = 54;
 //		MasterBluetoothCommunicator.InitializeConnection();
 //		try {
 //			MasterBluetoothCommunicator.sendInstructions(instructions);
 //		} catch (IOException e) {
 //			Sound.buzz();
 //		}
 
 		// Localize robot and go to the center of the corner tile
 		localizer.doLocalization();
		nav.turnTo(0);
 		//nav.travelTo(66, 13);
 		//nav.travelTo(60, 60);
 		
 		if (instructions.role == PlayerRole.ATTACKER) {
 			// On offense
 			// Obtain launch position (send request to other brick)
 			// Offensive loop
 			while (true) {
 				// Navigate to the ball dispenser
 				// Load ball
 				// Navigate to the launch position
 				// Launch (send request to other brick)
 			}
 		} else {
 			// Calculate defense position
 			// Navigate to defender position
 			// Orient robot
 			// Defensive loop
 			while (true) {
 				// Defend
 			}
 
 		}
 
 	}
 
 }
