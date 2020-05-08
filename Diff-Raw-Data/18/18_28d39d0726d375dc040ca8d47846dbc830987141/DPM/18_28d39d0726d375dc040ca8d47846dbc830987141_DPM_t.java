 package T07;
 
 
 import lejos.nxt.*;
 import lejos.nxt.comm.RConsole;
 import lejos.util.Delay;
 import bluetooth.*;
 
 public class DPM {
 	
 	public enum USSensor {MIDDLE, RIGHT};
 	public enum LSensor {LEFT, MIDDLE, RIGHT};
 	static private final double LEFT_RADIUS = 2.70;
 	static private final double RIGHT_RADIUS = 2.70;
 	static private final double WIDTH = 15.8;
 	static private final NXTRegulatedMotor LEFTMOTOR = Motor.A;
 	static private final NXTRegulatedMotor RIGHTMOTOR = Motor.B;
 	static private final LightSensor LEFT_LIGHT_SENSOR = new LightSensor(SensorPort.S1);
 	static private final LightSensor RIGHT_LIGHT_SENSOR = new LightSensor(SensorPort.S2);
	static private final LightSensor MIDDLE_LIGHT_SENSOR = new LightSensor(SensorPort.S4); //TODO: Fix this, exception here
 	static private final UltrasonicSensor MIDDLE_ULTRASONIC_SENSOR = new UltrasonicSensor(SensorPort.S3);
 	static private final UltrasonicSensor RIGHT_ULTRASONIC_SENSOR = new UltrasonicSensor(SensorPort.S4);
 	
 	public static void main(String[] args){
 		
 		// Instantiate classes for basic components testing
 		TwoWheeledRobot robot = new TwoWheeledRobot(LEFTMOTOR, RIGHTMOTOR, MIDDLE_ULTRASONIC_SENSOR, RIGHT_ULTRASONIC_SENSOR, 
 				LEFT_LIGHT_SENSOR, RIGHT_LIGHT_SENSOR, MIDDLE_LIGHT_SENSOR, WIDTH, LEFT_RADIUS, RIGHT_RADIUS);
 		Odometer odo = new Odometer(robot);
 		// Navigation navi = new Navigation(odo);
 		//start to get connection with the client brick
		//CommunicationServer communicationServer = new CommunicationServer();
 		
 		//start to get connection with Bluetooth server provided by TA
 		BTReceiver btReceiver = new BTReceiver();
 		
 				
 		
 		
 		int buttonChoice;
 		//RConsole.openBluetooth(20000);
 		//RConsole.println("Connected!");
 		do {
 			LCD.clear();
 			
 			LCD.drawString(" Left  |  Right ", 0, 0);
 			LCD.drawString("                ", 0, 1);
 			LCD.drawString("       To       ", 0, 2);
 			LCD.drawString("      start     ", 0, 3);
 			LCD.drawString("     program    ", 0, 4);
 			
 			buttonChoice = Button.waitForAnyPress();
 			
 		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);
 		
 		
 		
 		USPoller usPoller = new USPoller(robot);
 		LightPoller lp1 = new LightPoller(robot, LSensor.LEFT);
 		LightPoller lp2 = new LightPoller(robot, LSensor.RIGHT);
 		LightPoller lp3 = new LightPoller(robot, LSensor.MIDDLE);
 		
 		//starting communication with the client brick
		//CommunicationController communicationController = new CommunicationController(lp3,communicationServer);
 
 		
 		LCDInfo lcd = new LCDInfo(odo, usPoller, lp1, lp2);
 		
 		try {
 			Thread.sleep(1000);
 		} catch (Exception e) {
 			
 		}
 		
 		// sets the basics for localization
 		Navigation navi = new Navigation(odo, usPoller);	
 		USLocalizer usLocalizer = new USLocalizer(odo, navi, usPoller);		
 		LightLocalizer lightLocalizer = new LightLocalizer(odo, lp1, lp2, navi); 
 		
 		// performs us and light localization subroutines
 		usLocalizer.doLocalization();
 		lightLocalizer.doLocalization();
 		
 		// search algorithm, will search for the light source TODO: need to integrate with the second brick... HOW?
 		int x = 0; // TODO pass the x and y values to the searcher so it knows where the searching starts...
 		int y = 0;
		Searcher search = new Searcher(odo, navi, lp3, usPoller, /*communicationController,*/ x, y);
 		
 		search.findBeacon();
 		
 		// once the escape button is pressed, the robot will 
 		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
 		System.exit(0);
 	}
 
 }
