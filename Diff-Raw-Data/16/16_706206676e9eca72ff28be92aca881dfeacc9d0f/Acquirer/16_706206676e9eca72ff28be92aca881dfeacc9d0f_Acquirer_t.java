 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy.subsystems;
 
 import edu.stuy.Constants;
 import edu.stuy.util.Gamepad;
 import edu.wpi.first.wpilibj.Victor;
 
 /**
  *
  * @author R4D4
  */
 public class Acquirer {
 
     private static Acquirer instance;
     private Victor acquirer;
    
    private static double FWD_VAL = -1;
    private static double REV_VAL = 1;
    private static double STOP_VAL = 0;
 
     private Acquirer() {
         acquirer = new Victor(Constants.ACQUIRER_CHANNEL);
     }
 
     public static Acquirer getInstance() {
         if (instance == null) {
             instance = new Acquirer();
         }
         return instance;
     }
     /*
      * Sets the speed for the acquirer.
      */
     private void spin(double speed) {
         acquirer.set(speed);
     }
 
     public void acquire() {
        spin(FWD_VAL);
     }
 
     public void acquireReverse() {
        spin(REV_VAL);
     }
 
     public void stop() {
        spin(STOP_VAL);
     }
     /*
      * Returns the speed of the acquirer.
      */
     public double getRollerSpeed() {
         return acquirer.get();
     }
 
     public boolean isAcquiring() {
        return getRollerSpeed() == FWD_VAL;
     }
 
     public void manualAcquirerControl(Gamepad gamepad) {
         if (gamepad.getLeftTrigger()) {
             acquireReverse();
         }
         else if (gamepad.getRightTrigger()) {
             acquire();
         }
         else {
             stop();
         }
 
     }
 }
