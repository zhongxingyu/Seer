 package ca.mcgill.dpm.winter2013.group6;
 
 import lejos.nxt.Button;
 import lejos.nxt.LCD;
 import lejos.nxt.Motor;
 import lejos.nxt.MotorPort;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.SensorPort;
 import lejos.nxt.TouchSensor;
 import lejos.nxt.UltrasonicSensor;
 import lejos.util.Timer;
 import ca.mcgill.dpm.winter2013.group6.avoidance.ObstacleAvoider;
 import ca.mcgill.dpm.winter2013.group6.avoidance.TouchAvoidanceImpl;
 import ca.mcgill.dpm.winter2013.group6.launcher.BallLauncher;
 import ca.mcgill.dpm.winter2013.group6.launcher.BallLauncherImpl;
 import ca.mcgill.dpm.winter2013.group6.navigator.ObstacleNavigator;
 import ca.mcgill.dpm.winter2013.group6.odometer.Odometer;
 import ca.mcgill.dpm.winter2013.group6.util.Coordinate;
 import ca.mcgill.dpm.winter2013.group6.util.InfoDisplay;
 import ca.mcgill.dpm.winter2013.group6.util.Robot;
 
 /**
  * Entrypoint to the application. Will start the robot to be either attacker or
  * defender.
 *
  * @author Alex Selesse
 *
  */
 public class Main {
   public static void main(String[] args) {
     int buttonChoice;
     NXTRegulatedMotor ballThrowingMotor = new NXTRegulatedMotor(MotorPort.C);
     NXTRegulatedMotor leftMotor = new NXTRegulatedMotor(MotorPort.A);
     NXTRegulatedMotor rightMotor = new NXTRegulatedMotor(MotorPort.B);
 
     UltrasonicSensor ultrasonicSensor = new UltrasonicSensor(SensorPort.S1);
     TouchSensor leftTouchSensor = new TouchSensor(SensorPort.S3);
     TouchSensor rightTouchSensor = new TouchSensor(SensorPort.S4);
 
     Robot patBot = new Robot(2.71, 2.71, 16.2, leftMotor, rightMotor);
 
     do {
       // clear the display
       LCD.clear();
 
       LCD.drawString("< Left | Right >", 0, 0);
       LCD.drawString("  Mode | Mode   ", 0, 1);
       LCD.drawString("       |        ", 0, 2);
       LCD.drawString("       |        ", 0, 3);
 
       buttonChoice = Button.waitForPress();
     }
     while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT
         && buttonChoice != Button.ID_ESCAPE);
 
     // start the odometer
     Odometer odometer = new Odometer(patBot);
     InfoDisplay display = new InfoDisplay(odometer, new UltrasonicSensor(SensorPort.S1),
         new TouchSensor(SensorPort.S3), new TouchSensor(SensorPort.S4));
     Timer displayTimer = new Timer(25, display);
 
     Thread odometerThread = new Thread(odometer);
     odometerThread.start();
     displayTimer.start();
 
     if (buttonChoice == Button.ID_LEFT) {
       Motor.A.flt(true);
       Motor.B.flt(false);
     }
     else if (buttonChoice == Button.ID_RIGHT) {
      Navigator navigator = new ObstacleNavigator(odometer, leftMotor, rightMotor,
           ultrasonicSensor, leftTouchSensor, rightTouchSensor);
       navigator.setCoordinates(new Coordinate[] { new Coordinate(30, 30), new Coordinate(0, 30) });
 
       ObstacleAvoider touchAvoidance = new TouchAvoidanceImpl(odometer, navigator, leftTouchSensor,
           rightTouchSensor);
       ObstacleAvoider ultrasonicAvoidance = new UltrasonicAvoidanceImpl(odometer, navigator,
           ultrasonicSensor);

       Thread touchThread = new Thread(touchAvoidance);
       // Thread ultrasonicSensorThread = new Thread(ultrasonicAvoidance);
       Thread navigatorThread = new Thread(navigator);
 
       touchThread.start();
       // ultrasonicSensorThread.start();
       navigatorThread.start();
 
       touchThread.run();
       navigatorThread.run();
     }
     else if (buttonChoice != Button.ID_ESCAPE) {
       BallLauncher launcher = new BallLauncherImpl(ballThrowingMotor, 10.0);
       new Thread(launcher).start();
     }
     else {
       System.exit(0);
     }
 
     while (Button.waitForPress() != Button.ID_ESCAPE) {
       ;
     }
   }
 }
