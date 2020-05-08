 package ca.mcgill.dpm.winter2013.group6.avoidance;
 
 import lejos.nxt.Sound;
 import lejos.nxt.TouchSensor;
 import ca.mcgill.dpm.winter2013.group6.navigator.Navigator;
 import ca.mcgill.dpm.winter2013.group6.odometer.Odometer;
 
 public class TouchAvoidanceImpl extends AbstractObstacleAvoider {
   protected TouchSensor leftTouchSensor;
   protected TouchSensor rightTouchSensor;
 
   public TouchAvoidanceImpl(Odometer odometer, Navigator navigator, TouchSensor leftTouchSensor,
       TouchSensor rightTouchSensor) {
     super(odometer, navigator);
     this.leftTouchSensor = leftTouchSensor;
     this.rightTouchSensor = rightTouchSensor;
   }
 
   @Override
   public void avoidObstacles() {
     if (obstacleDetected() && !isAvoiding) {
       this.isAvoiding = true;
       navigator.stop();
       moveBackAndTurnAway();
       this.isAvoiding = false;
     }
   }
 
   private void moveBackAndTurnAway() {
     boolean isTurningLeft = leftTouchSensor.isPressed();
     double turningAngle = 45;
 
     if (!isTurningLeft) {
       turningAngle = -turningAngle;
     }
 
     Sound.beep();
    navigator.travelStraight(-10);
 
     Sound.beep();
 
     navigator.turnTo(turningAngle);
 
    navigator.travelStraight(10);
     Sound.beep();
 
   }
 
   private boolean obstacleDetected() {
     return leftTouchSensor.isPressed() || rightTouchSensor.isPressed();
   }
 }
