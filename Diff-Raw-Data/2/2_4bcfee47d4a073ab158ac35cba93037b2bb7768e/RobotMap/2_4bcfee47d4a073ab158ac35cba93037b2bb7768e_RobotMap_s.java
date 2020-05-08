 /**** RobotMap.java
  * 
  * This holds all global constants and variables.
  * DO NOT PUT FUNCTIONS IN HERE.
  *****/
 package edu.ames.frc.robot;
 
 public class RobotMap {
     protected final static boolean debugmode = true;
     protected final static double deadzone = .05;
     /*Drive motor pins.
      * For managment reasons we will track each wheel motor based on the side of the traingle/hexagon it is located at
     * Each corner is labeled as either A,B, or C. A being the front wheel, B being the back right, and C being the right left.
      * This nameing trend is reflected in the pin assignments in this class and should continue to be reflected in other parts
      * of the overall robot system.
      */
     protected final static int Apin = 1; // Pin assignment for the front motor A
     protected final static int Bpin = 2;
     protected final static int Cpin = 3;
     
     protected final static int gyroport = 1; // gyro is on analog port 1
     
     protected final static int expo_ramp = 3;
 } 
