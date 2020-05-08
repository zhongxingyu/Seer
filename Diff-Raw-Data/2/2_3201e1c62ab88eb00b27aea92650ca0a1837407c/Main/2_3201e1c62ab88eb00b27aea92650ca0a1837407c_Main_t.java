 package ca.mcgill.dpm.winter2013.group6;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import lejos.nxt.Button;
 import lejos.nxt.LCD;
 import lejos.nxt.LightSensor;
 import lejos.nxt.MotorPort;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.SensorPort;
 import lejos.nxt.Sound;
 import lejos.nxt.TouchSensor;
 import lejos.nxt.UltrasonicSensor;
 import lejos.nxt.comm.Bluetooth;
 import lejos.util.Delay;
 import ca.mcgill.dpm.winter2013.group6.avoidance.ObstacleAvoider;
 import ca.mcgill.dpm.winter2013.group6.avoidance.TouchAvoidanceImpl;
 import ca.mcgill.dpm.winter2013.group6.avoidance.UltrasonicAvoidanceImpl;
 import ca.mcgill.dpm.winter2013.group6.bluetooth.BluetoothReceiver;
 import ca.mcgill.dpm.winter2013.group6.bluetooth.PlayerRole;
 import ca.mcgill.dpm.winter2013.group6.bluetooth.Transmission;
 import ca.mcgill.dpm.winter2013.group6.defender.RobotUltrasonicDefender;
 import ca.mcgill.dpm.winter2013.group6.defender.RobotUltrasonicDefenderImpl;
 import ca.mcgill.dpm.winter2013.group6.launcher.BallLauncher;
 import ca.mcgill.dpm.winter2013.group6.launcher.BallLauncherImpl;
 import ca.mcgill.dpm.winter2013.group6.localization.LightLocalizer;
 import ca.mcgill.dpm.winter2013.group6.localization.Localizer;
 import ca.mcgill.dpm.winter2013.group6.localization.SmartLightLocalizer;
 import ca.mcgill.dpm.winter2013.group6.localization.UltrasonicLocalizer;
 import ca.mcgill.dpm.winter2013.group6.navigator.Navigator;
 import ca.mcgill.dpm.winter2013.group6.navigator.ObstacleNavigator;
 import ca.mcgill.dpm.winter2013.group6.odometer.Odometer;
 import ca.mcgill.dpm.winter2013.group6.util.Coordinate;
 import ca.mcgill.dpm.winter2013.group6.util.InfoDisplay;
 import ca.mcgill.dpm.winter2013.group6.util.PlayingField;
 import ca.mcgill.dpm.winter2013.group6.util.Robot;
 
 /**
  * Entry point to the application.
  * 
  * @author Alex Selesse
  * 
  */
 public class Main {
   private static NXTRegulatedMotor leftMotor;
   private static NXTRegulatedMotor rightMotor;
   private static NXTRegulatedMotor ballThrowingMotor;
   private static UltrasonicSensor ultrasonicSensor;
   private static LightSensor lightSensor;
   private static TouchSensor leftTouchSensor;
   private static TouchSensor rightTouchSensor;
   private static Robot patBot;
   private static Odometer odometer;
   private static InfoDisplay infoDisplay;
   private static Navigator navigator;
   private static BluetoothReceiver bluetooth;
   private static ObstacleAvoider touchAvoidance;
   private static ObstacleAvoider ultrasonicAvoidance;
   private static PlayingField playingField;
   private static Localizer lightLocalizer;
   private static Localizer ultrasonicLocalizer;
   private static Localizer smartLightLocalizer;
   private static RobotUltrasonicDefender defender;
   private static Thread odometerThread;
   private static Thread infoDisplayThread;
   private static Thread navigatorThread;
   private static Thread bluetoothThread;
   private static Thread touchAvoidanceThread;
   private static Thread ultrasonicAvoidanceThread;
   private static Thread lightLocalizerThread;
   private static Thread ultrasonicLocalizerThread;
   private static Thread ballLauncherThread;
   private static Thread smartLightLocalizerThread;
   private static Thread defenderThread;
   private static BallLauncher ballLauncher;
 
   public static void main(String[] args) {
     int buttonChoice;
 
     // wait for user input
     do {
       LCD.clear();
 
       LCD.drawString("< Test | Compe  ", 0, 0);
       LCD.drawString("  Mode | tition ", 0, 1);
       LCD.drawString("       | Mode   ", 0, 2);
       LCD.drawString("       |        ", 0, 3);
 
       buttonChoice = Button.waitForPress();
     }
     while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT
         && buttonChoice != Button.ID_ESCAPE);
 
     initializeMotorsAndSensors();
     initializeComponents();
     initializeComponentThreads();
     initializeObstacleAvoiders();
 
     if (buttonChoice == Button.ID_LEFT) {
       performLeftButtonAction();
     }
     else if (buttonChoice == Button.ID_RIGHT) {
       performCompetitionAction();
     }
 
     while (Button.waitForPress() != Button.ID_ESCAPE) {
       ;
     }
   }
 
   public static void performLeftButtonAction() {
     competitionTest();
   }
 
   private static void competitionTest() {
     try {
       odometerThread.start();
       infoDisplayThread.start();
 
       int ballDispenserX = -1;
       int ballDispenserY = 4;
       Transmission transmission = new Transmission();
       transmission.setForwardLineDistanceFromGoal(5);
 
       Coordinate ballDispenserCoordinate = Coordinate.getCoordinateFromBlock(ballDispenserX,
           ballDispenserY);
       Coordinate throwingLocation = Coordinate.pickBallLauncherLocation(transmission);
       Coordinate goalLocation = Coordinate.getCoordinateFromBlock(5, 10);
 
       navigator.setCoordinates(new Coordinate[] { Coordinate
           .getCoordinateFromBallDispenserLocation(ballDispenserX, ballDispenserY) });
       navigatorThread.start();
       navigatorThread.join();
 
       smartLightLocalizer = new SmartLightLocalizer(odometer, navigator, lightSensor,
           Coordinate.getCoordinateFromBallDispenserLocation(ballDispenserX, ballDispenserY));
       smartLightLocalizerThread = new Thread(smartLightLocalizer);
       smartLightLocalizerThread.start();
       smartLightLocalizerThread.join();
 
       navigator.turnTo(ballDispenserCoordinate.getX(), ballDispenserCoordinate.getY());
       navigator.turnTo(180);
 
       navigator.travelStraight(-0.8 * 30.5);
       Delay.msDelay(2000);
       navigator.travelStraight(0.8 * 30.5);
 
       navigator.setCoordinates(new Coordinate[] { throwingLocation });
       navigatorThread = new Thread(navigator);
       navigatorThread.start();
       navigatorThread.join();
 
       navigator.turnTo(goalLocation);
       Delay.msDelay(100);
 
       ballLauncher = new BallLauncherImpl(ballThrowingMotor, 5);
       ballLauncherThread = new Thread(ballLauncher);
       ballLauncherThread.start();
 
      Sound.beep();

       Delay.msDelay(500);
     }
     catch (Exception e) {
     }
   }
 
   public static void testUltrasonicDefense() {
     navigator.face(-90);
     Sound.beepSequence();
     Delay.msDelay(500);
     navigator.face(0);
     Sound.beepSequence();
     Delay.msDelay(500);
     navigator.face(90);
     Sound.beepSequence();
   }
 
   public static void testObstacleAvoidanceBoundaries() {
     odometerThread.start();
     infoDisplayThread.start();
 
     ultrasonicSensor.continuous();
     touchAvoidanceThread.start();
     ultrasonicAvoidanceThread.start();
 
     navigator.setCoordinates(new Coordinate[] { new Coordinate(-10, 100), new Coordinate(30, 30) });
     navigatorThread.start();
   }
 
   public static void performCompetitionAction() {
     try {
       odometerThread.start();
       infoDisplayThread.start();
       bluetoothThread.start();
       bluetoothThread.join();
 
       Transmission transmission = bluetooth.getTransmission();
 
       // power off bluetooth, we've got what we need
       Bluetooth.setPower(false);
 
       if (transmission.getRole() == PlayerRole.ATTACKER) {
         attack(transmission);
       }
       else {
         defender = new RobotUltrasonicDefenderImpl(navigator, ultrasonicSensor, transmission);
         defenderThread.start();
       }
     }
     catch (InterruptedException e) {
       // this thread won't be interrupted
     }
   }
 
   private static void attack(Transmission transmission) {
     try {
       ballLauncher = new BallLauncherImpl(ballThrowingMotor, 1);
       ballLauncherThread = new Thread(ballLauncher);
       lightLocalizer = new LightLocalizer(odometer, navigator, lightSensor, transmission
           .getStartingCorner().getId());
       lightLocalizerThread = new Thread(lightLocalizer);
       ultrasonicLocalizer = new UltrasonicLocalizer(odometer, navigator, ultrasonicSensor,
           transmission.getStartingCorner().getId());
       ultrasonicLocalizerThread = new Thread(ultrasonicLocalizer);
 
       // start and finish ultrasonic localization
       ultrasonicLocalizerThread.start();
       ultrasonicLocalizerThread.join();
 
       navigator.travelTo(15, 15);
 
       // start and finish light localization
       lightLocalizerThread.start();
       lightLocalizerThread.join();
 
       ultrasonicSensor.continuous();
       // start the touch avoidance and the ultrasonic avoidance threads
       touchAvoidanceThread.start();
       ultrasonicAvoidanceThread.start();
 
       Coordinate ballDispenserCoordinate = Coordinate.getCoordinateFromBallDispenserLocation(
           transmission.getBallDispenserX(), transmission.getBallDispenserY());
       Coordinate launchingCoordinate = Coordinate.pickBallLauncherLocation(null);
 
       navigator.setCoordinates(new Coordinate[] { ballDispenserCoordinate });
 
       navigatorThread.start();
       navigatorThread.join();
 
       smartLightLocalizer = new SmartLightLocalizer(odometer, navigator, lightSensor,
           ballDispenserCoordinate);
       smartLightLocalizerThread = new Thread(smartLightLocalizer);
       smartLightLocalizerThread.start();
       smartLightLocalizerThread.join();
 
       navigator.turnTo(ballDispenserCoordinate.getX(), ballDispenserCoordinate.getY());
       navigator.turnTo(180);
 
       ballThrowingMotor.rotate(5);
       ballThrowingMotor.flt(true);
 
       navigator.travelStraight(-1.2 * 30.5 + 1.5);
       Thread.sleep(3000);
       navigator.travelStraight(1.2 * 30.5 + 1.5);
 
       ballThrowingMotor.rotate(-5);
       ballThrowingMotor.flt(true);
 
       navigator.setCoordinates(new Coordinate[] { launchingCoordinate });
       navigatorThread = new Thread(navigator);
       navigatorThread.start();
       navigatorThread.join();
 
       Thread.sleep(1000);
 
       Coordinate goalCoordinate = Coordinate.getCoordinateFromBlock(5, 10);
       navigator.turnTo(goalCoordinate.getX(), goalCoordinate.getY());
 
       Thread.sleep(250);
 
       // start the ball launching thread, wait for it to finish
       ballLauncherThread.start();
       ballLauncherThread.join();
     }
     catch (InterruptedException e) {
     }
   }
 
   private static void initializeMotorsAndSensors() {
     leftMotor = new NXTRegulatedMotor(MotorPort.A);
     rightMotor = new NXTRegulatedMotor(MotorPort.B);
     ballThrowingMotor = new NXTRegulatedMotor(MotorPort.C);
 
     ultrasonicSensor = new UltrasonicSensor(SensorPort.S1);
     lightSensor = new LightSensor(SensorPort.S2);
     leftTouchSensor = new TouchSensor(SensorPort.S3);
     rightTouchSensor = new TouchSensor(SensorPort.S4);
   }
 
   private static void initializeComponents() {
     patBot = new Robot(2.70, 2.70, 15.6, leftMotor, rightMotor);
     playingField = new PlayingField(10, 10);
     odometer = new Odometer(patBot);
     infoDisplay = new InfoDisplay(odometer, ultrasonicSensor, leftTouchSensor, rightTouchSensor);
     navigator = new ObstacleNavigator(odometer, leftMotor, rightMotor, ultrasonicSensor,
         leftTouchSensor, rightTouchSensor);
     bluetooth = new BluetoothReceiver();
     touchAvoidance = new TouchAvoidanceImpl(odometer, navigator, playingField, leftTouchSensor,
         rightTouchSensor);
     ultrasonicAvoidance = new UltrasonicAvoidanceImpl(odometer, navigator, playingField,
         ultrasonicSensor);
   }
 
   private static void initializeComponentThreads() {
     odometerThread = new Thread(odometer);
     infoDisplayThread = new Thread(infoDisplay);
     navigatorThread = new Thread(navigator);
     bluetoothThread = new Thread(bluetooth);
     touchAvoidanceThread = new Thread(touchAvoidance);
     ultrasonicAvoidanceThread = new Thread(ultrasonicAvoidance);
     smartLightLocalizerThread = new Thread(smartLightLocalizer);
     defenderThread = new Thread(defender);
   }
 
   private static void initializeObstacleAvoiders() {
     List<ObstacleAvoider> obstacleAvoiders = new ArrayList<ObstacleAvoider>();
     obstacleAvoiders.add(touchAvoidance);
     obstacleAvoiders.add(ultrasonicAvoidance);
     ((ObstacleNavigator) navigator).setAvoiderList(obstacleAvoiders);
   }
 
   private static void calibrate() {
     navigator.travelStraight(30.5 * 2);
     navigator.face(180);
     navigator.travelStraight(30.5 * 2);
     navigator.face(0);
 
   }
 }
