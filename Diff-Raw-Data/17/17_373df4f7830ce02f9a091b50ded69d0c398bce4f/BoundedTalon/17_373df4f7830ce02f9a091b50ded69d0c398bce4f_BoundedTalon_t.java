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
         fwdSwitch = new DigitalInput(fwdLimitSwitchChannel);
         revSwitch = new DigitalInput(revLimitSwitchChannel);
     }
 
     /**
      * Checks limit switches to allow for soft bounds for motion. If both
      * switches appear to be actuated at the same time, assume that they're not
      * installed and allow the motor to run normally.
      * @param value
      */
     public void set(double value) {
         // ^ is exclusive or
         if ((isFwdSwitchTriggered() && value < 0) || (isRevSwitchTriggered() && value > 0)) {
            super.set(0);
         }
         else {
            super.set(value);
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
