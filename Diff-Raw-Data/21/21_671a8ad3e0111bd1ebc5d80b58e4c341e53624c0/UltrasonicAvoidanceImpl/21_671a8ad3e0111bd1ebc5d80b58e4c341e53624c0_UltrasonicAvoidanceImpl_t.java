 package ca.mcgill.dpm.winter2013.group6.avoidance;
 
 import lejos.nxt.UltrasonicSensor;
 import ca.mcgill.dpm.winter2013.group6.navigator.Navigator;
 import ca.mcgill.dpm.winter2013.group6.odometer.Odometer;
 import ca.mcgill.dpm.winter2013.group6.util.PlayingField;
 
 /**
  * Ultrasonic obstacle avoider. Our current hardware version of the robot has an
  * ultrasonic sensor that will allow it to detect obstacles in front of it.
  * 
  * @author Alex Selesse
  * 
  */
 public class UltrasonicAvoidanceImpl extends AbstractObstacleAvoider {
   protected UltrasonicSensor ultrasonicSensor;
   private final int THRESHOLD_DISTANCE = 20;
 
   public UltrasonicAvoidanceImpl(Odometer odometer, Navigator navigator, PlayingField playingField,
       UltrasonicSensor ultrasonicSensor) {
     super(odometer, navigator, playingField);
     this.ultrasonicSensor = ultrasonicSensor;
   }
 
   @Override
   public void avoidObstacles() {
     if (thereIsAnObstacleInFrontOfUs() && !isCurrentlyAvoiding) {
       this.isCurrentlyAvoiding = true;
       moveAwayFromTheObstacle();
       this.isCurrentlyAvoiding = false;
     }
   }
 
   @Override
   public void run() {
     // set the ultrasonic sensor to be continuous rather than pinging
     ultrasonicSensor.continuous();
     super.run();
   }
 
   private void moveAwayFromTheObstacle() {
     // we should turn left if we currently have a greater x coordinate value
     // than our heading does
     boolean weShouldTurnLeft;
 
     // if we don't have a current heading, don't do anything
     if (navigator.getCurrentHeading() == null) {
       return;
     }
     else {
       weShouldTurnLeft = odometer.getX() > navigator.getCurrentHeading().getX();
     }
 
     double turningAngle = 70;
     if (weShouldTurnLeft) {
       turningAngle = -70;
     }
 
    turningAngle = getBoundaryBasedTurningAngle(turningAngle);
 
     navigator.travelStraight(-10);
     navigator.turnTo(turningAngle);
     navigator.travelStraight(38);
   }
 
   private boolean thereIsAnObstacleInFrontOfUs() {
     return (ultrasonicSensor.getDistance() - THRESHOLD_DISTANCE) < 0;
   }
 
 }
