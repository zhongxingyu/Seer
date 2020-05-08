 package com.edinarobotics.zephyr.parts;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Relay;
 
 /**
  *Contains functions to modify the left and right driving motors along with the
  * super shifters
  */
 public class DrivingComponents {
     private Jaguar leftMotors;
     private Jaguar rightMotors;
     private Relay shifters;
     /**
      * Initializes leftMotors, rightMotors and shifters
      * @param left is used to initialize leftMotors.
      * @param right is used to initialize rightMotors.
      * @param shifters is used to initialize shifters.
      */
     public DrivingComponents(int left, int right, int shifters){
         leftMotors = new Jaguar(left);
         rightMotors = new Jaguar(right);
         this.shifters = new Relay(shifters);
     }
     /**
      * Set the jaguars.
      * @param left sets the leftMotors jaguar.
      * @param right sets the rightMotors jaguar
      */
     public void setDrivingSpeed(double left, double right){
         leftMotors.set(left);
        rightMotors.set(right);
     }
     /**
      * shift the shifters.
      * @param high determines the state, true raises them, false lowers them.
      */
     public void shift(boolean high){
         shifters.set((high?Relay.Value.kReverse:Relay.Value.kForward));
     }
     
 }
