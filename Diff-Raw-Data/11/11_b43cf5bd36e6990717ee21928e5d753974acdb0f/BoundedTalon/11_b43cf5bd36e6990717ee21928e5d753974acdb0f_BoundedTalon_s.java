 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy.util;
 
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Talon;
 
 /**
  *
  * @author Danny
  */
 public class BoundedTalon extends Talon {
     
     DigitalInput fwdSwitch;
     DigitalInput revSwitch;
     
     public BoundedTalon(int port, int fwdLimitSwitchChannel, int revLimitSwitchChannel) {
         super(port);
        fwdSwitch = new DigitalInput(port, fwdLimitSwitchChannel);
        revSwitch = new DigitalInput(port, revLimitSwitchChannel);
     }
 
     /**
      * Checks limit switches to allow for soft bounds for motion.
      * @param value
      */
     public void set(double value) {
         if ((isFwdSwitchTriggered() && value < 0) || (isRevSwitchTriggered() && value > 0)) {
             super.set(value);
         } else {
             super.set(0);
         }
     }
     
     /**
      * Checks if the limit switch for forward motion is triggered.
      * @return if the forward switch is triggered
      */
     public boolean isFwdSwitchTriggered() {
         return !fwdSwitch.get();
     }
     
     /**
      * Checks if the limit switch for backward motion is triggered.
      * @return if the backward switch is triggered
      */
     public boolean isRevSwitchTriggered() {
         return !revSwitch.get();
     }
 }
