 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.rmr662.frc2012.component;
 
 import com.rmr662.frc2012.generic.Component;
 import edu.wpi.first.wpilibj.Servo;
 
 /**
  *
  * @author RMR Programming
  */
 public class Camera extends Component{
     
     //private static final int PAN = 10;
     private static final int TILT = 5;
     
   // private static final double DEFAULT_PAN = .5d;
    private static final double DEFAULT_TILT = .5d;
 
     //private Servo servoPan;
     //private double currentPanState;
     //private double targetPanState;
     private Servo servoTilt;
     private double currentTiltState;
     private double targetTiltState;
     
     private static Camera instance = null;
     
     /**
      * Creates a CameraComponent with servos on the default channels
      */
     public Camera(){
       //  servoPan = new Servo(PAN);
         servoTilt = new Servo(TILT);
     }
     
     public static Camera getInstance(){
         if(instance == null){
             instance = new Camera();
         }
         return instance;
     }
     
     public void update() {
         double localPanTarget, localTiltTarget;
         synchronized(this) {
           //  localPanTarget = targetPanState;
             localTiltTarget = targetTiltState;
         }
        // servoPan.set(localPanTarget);  
         servoTilt.set(localTiltTarget);
     }
     
     public void reset() {
        // targetPanState = DEFAULT_PAN;
         targetTiltState = DEFAULT_TILT;
     }
     
     /**
      * gives the value Camera Component value for getRMRName
      * @return 
      */
     public String getRMRName() {
         return "Camera";
     }
     
     /**
      * Gets a value for Pan State.
      * @return 
      */
 //    public double getPanState(){
 //        currentPanState = servoPan.get();
 //        return currentPanState;
 //    }
 //    
     /**
      * Sets the pan target state
      * @param goalPanState the desired pan target state
      */
 //    public synchronized void setPanTargetState(double goalPanState){
 //        targetPanState = goalPanState;
 //    }
     
     /**
      * Gets the current tilt state from the servo
      * @return the current tilt state
      */
     public double getTiltState(){
         currentTiltState = servoTilt.get();        
         return currentTiltState;
     }
     
     /**
      * Sets the tilt target state
      * @param goalTiltState the desired tilt target state
      */
     public synchronized void setTiltTargetState(double goalTiltState){
         targetTiltState = goalTiltState;
     }
 }
