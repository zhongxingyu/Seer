 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.rmr662.frc2012.component;
 
 import com.rmr662.frc2012.generic.Component;
 import com.rmr662.frc2012.physical.RMRSolenoidSystem;
 import edu.wpi.first.wpilibj.Solenoid;
 
 /**
  *
  * @author RMR Programming
  */
 public class BallBucket extends Component {
 
     private static BallBucket instance;
     private static final int NORMAL_CHANNEL_ELBOW = 1;
     private static final int INVERTED_CHANNEL_ELBOW = 2;
 //    private static final int NORMAL_CHANNEL_WRIST = 3;
 //    private static final int INVERTED_CHANNEL_WRIST = 4;
     private static final boolean DEFAULT_STATE_ELBOW = false;
 //    private static final boolean DEFAULT_STATE_WRIST = false;
     private RMRSolenoidSystem elbowSolenoid;
 //    private RMRSolenoidSystem wristSolenoid;
     private boolean targetElbowValue;
 //    private boolean targetWristValue;
 
     /**
      * creates and assigns default states for solenoid variables
      */
     public BallBucket() {
         elbowSolenoid = new RMRSolenoidSystem(new Solenoid(NORMAL_CHANNEL_ELBOW), new Solenoid(INVERTED_CHANNEL_ELBOW));
         elbowSolenoid.set(DEFAULT_STATE_ELBOW);
 
 //        wristSolenoid = new RMRSolenoidSystem(new Solenoid(NORMAL_CHANNEL_WRIST), new Solenoid(INVERTED_CHANNEL_WRIST));
 //        wristSolenoid.set(DEFAULT_STATE_WRIST);
     }
 
     /**
      * synchronizes localTarget Elbow- and WristValue with target Elbow and
      * WristValue sets target values for elbow- and wristSolenoid as
      * localTargetElbowValue and localTargetWristValue
      */
     public void update() {
         boolean localTargetElbowValue;
 //        boolean localTargetWristValue;
 
         synchronized (this) {
             localTargetElbowValue = targetElbowValue;
 //            localTargetWristValue = targetWristValue;
         }
         elbowSolenoid.set(localTargetElbowValue);
 //        wristSolenoid.set(localTargetWristValue);
     }
 
     /**
      * resets targetElbow- and targetWristValue to their default states
      */
     public void reset() {
         synchronized (this) {
             targetElbowValue = DEFAULT_STATE_ELBOW;
 //            targetWristValue = DEFAULT_STATE_WRIST;
         }
     }
 
     public String getRMRName() {
         return "Ball Bucket";
     }
 
     /**
      * creates the instance for the ball bucket if none is currently existent
      * prevents more than one ball bucket instance from being created
      *
      * @return
      */
     public static BallBucket getInstance() {
         if (instance == null) {
             instance = new BallBucket();
         }
         return instance;
     }
 
     /**
      * sets target values as down
      *
      * @param down
      */
     public synchronized void setElbowTarget(boolean down) {
         targetElbowValue = down;
     }
 //    /**
 //     * sets target values as down
 //     * @param down 
 //     */
 //
 //    public synchronized void setWristTarget(boolean down) {
 //        targetWristValue = down;
 //    }
 }
