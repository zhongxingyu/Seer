 package ca.mcgill.dpm.winter2013.group6.navigator;
 
 import java.util.List;
 
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.Sound;
 import lejos.nxt.TouchSensor;
 import lejos.nxt.UltrasonicSensor;
 import ca.mcgill.dpm.winter2013.group6.avoidance.ObstacleAvoider;
 import ca.mcgill.dpm.winter2013.group6.odometer.Odometer;
 
 /**
  * {@link Navigator} implementation which takes into consideration obstacles.
  *
  * @author Alex Selesse
  *
  */
 public class ObstacleNavigator extends NoObstacleNavigator {
   protected UltrasonicSensor ultrasonicSensor;
   protected TouchSensor leftTouchSensor;
   protected TouchSensor rightTouchSensor;
   protected List<ObstacleAvoider> obstacleAvoiders;
 
   public ObstacleNavigator(Odometer odometer, NXTRegulatedMotor leftMotor,
       NXTRegulatedMotor rightMotor, UltrasonicSensor uSensor, TouchSensor leftSensor,
       TouchSensor rightSensor) {
     super(odometer, leftMotor, rightMotor);
     this.ultrasonicSensor = uSensor;
     this.leftTouchSensor = leftSensor;
     this.rightTouchSensor = rightSensor;
   }
 
   @Override
   public void travelTo(double x, double y) {
     double turningAngle = getTurningAngle(x, y);
     turnTo(turningAngle);
 
     // Travel straight.
 
     // Keep running until we're within an acceptable threshold.
     while (((x - odometer.getX() > THRESHOLD || x - odometer.getX() < -THRESHOLD))
         || ((y - odometer.getY() > THRESHOLD || y - odometer.getY() < -THRESHOLD))) {
       if (getObstacleAvoider() != null) {
         ObstacleAvoider avoider = getObstacleAvoider();
         stop();
         while (avoider.isAvoiding()) {
           try {
             Thread.sleep(100);
           }
           catch (InterruptedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
           }
         }
         Sound.buzz();
         turningAngle = getTurningAngle(x, y);
         turnTo(turningAngle);
       }
 
       else {
         leftMotor.setSpeed(robot.getForwardSpeed());
         rightMotor.setSpeed(robot.getForwardSpeed());
         leftMotor.forward();
         rightMotor.forward();
 
       }
     }
     stop();
   }
 
   public void setAvoiderList(List<ObstacleAvoider> obstacleAvoiders) {
     this.obstacleAvoiders = obstacleAvoiders;
   }
 
   public ObstacleAvoider getObstacleAvoider() {
    if (obstacleAvoiders == null) { 
        return null;
    }
     for (ObstacleAvoider avoider : obstacleAvoiders) {
       if (avoider.isAvoiding()) {
         return avoider;
       }
     }
     return null;
   }
 }
