 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.rmr662.frc2012.component;
 
 import com.rmr662.frc2012.generic.Component;
 import com.rmr662.frc2012.physical.RMRLimitSwitch;
 import edu.wpi.first.wpilibj.Solenoid;
 
 /**
  *
  * @author ROBOTics
  */
 public class BallLoader extends Component {
     
     private static final int READY_SWITCH_CHANNEL = 5; // TODO
     private static final int SOLENOID_CHANNEL = 7; // TODO
     
     private RMRLimitSwitch readySwitch;
     private Solenoid solenoid;
     
     private boolean targetState = false; // true is up
     
     private static BallLoader instance;
     
     public BallLoader() {
         readySwitch = new RMRLimitSwitch(READY_SWITCH_CHANNEL, false);
         solenoid = new Solenoid(SOLENOID_CHANNEL);
     }
     
     public void update() {
         boolean localTargetState;
         synchronized(this) {
             localTargetState = targetState;
         }
         if (!ShooterArm.getInstance().isShooting()) {
             solenoid.set(localTargetState);
         }
     }
     
     public synchronized void reset() {
         targetState = true;
     }
     
     public String getRMRName() {
         return "Ball Pusher";
     }
     
     public boolean isReadyToFire() {
         return readySwitch.get();
     }
     
     public synchronized void setTargetState(boolean up) {
         this.targetState = up;
     }
     
     public static BallLoader getInstance() {
         if (instance == null) {
             instance = new BallLoader();
         }
         return instance;
     }
     
 }
