 package ca.mcgill.dpm.winter2013.group6.navigator;
 
 import java.util.ArrayList;
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
 public class ObstacleNavigator extends AbstractNavigator {
   protected UltrasonicSensor ultrasonicSensor;
   protected TouchSensor leftTouchSensor;
   protected TouchSensor rightTouchSensor;
   protected List<ObstacleAvoider> obstacleAvoiders;
  protected final static int PERIOD = 1000;
 
   public ObstacleNavigator(Odometer odometer, NXTRegulatedMotor leftMotor,
       NXTRegulatedMotor rightMotor, UltrasonicSensor ultrasonicSensor, TouchSensor leftTouchSensor,
       TouchSensor rightTouchSensor) {
     super(odometer, leftMotor, rightMotor);
     this.ultrasonicSensor = ultrasonicSensor;
     this.leftTouchSensor = leftTouchSensor;
     this.rightTouchSensor = rightTouchSensor;
     this.obstacleAvoiders = new ArrayList<ObstacleAvoider>();
   }
 
   @Override
   public void travelTo(double x, double y) {
     double turningAngle = getTurningAngle(x, y);
     turnTo(turningAngle);
 
     // Travel straight.
 
     // Keep running until we're within an acceptable threshold.
     double lastUpdate = System.currentTimeMillis();
     while (((x - odometer.getX() > THRESHOLD || x - odometer.getX() < -THRESHOLD))
         || ((y - odometer.getY() > THRESHOLD || y - odometer.getY() < -THRESHOLD))) {
       ObstacleAvoider avoider = getObstacleAvoider();
       if (avoider != null) {
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
        if (lastUpdate + PERIOD > System.currentTimeMillis()) {
           turningAngle = getTurningAngle(x, y);
           turnTo(turningAngle);
           lastUpdate = System.currentTimeMillis();
         }
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
