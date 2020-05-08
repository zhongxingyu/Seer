 /**** RobotMap.java
  * 
  * This holds all global constants and variables.
  * DO NOT PUT FUNCTIONS IN HERE.
  *****/
 package edu.ames.frc.robot;
 
 public class RobotMap {
     //Setting go here
     protected final static boolean debugmode = true;
     /*Drive motor pins.
      * For managment reasons we will track each wheel motor based on the side of the traingle/hexagon it is located at
      * Each corner is labeled as either A,B, or C. A being the front wheel, B being the back right, and C being the back left.
      * This nameing trend is reflected in the pin assignments in this class and should continue to be reflected in other parts
      * of the overall robot system.
      */
     
     
     //Pin Assignments go here!
     protected final static int Apin = 1; // Pin assignment for the front motor A
     protected final static int Bpin = 2;
     protected final static int Cpin = 3;
     protected final static int gyroport = 1;// gyro is on analog port 1
     protected final static int climbpin = 6;
     protected final static int assistclimb = 7;
     protected final static int pushpin = 9;
     protected final static int tiltpin = 5;
     
     //Joystick specific pins below
     protected final static int clmpin = 8;
     //protected final static int manpivotpin = 2;
     //protected final static int forcefire = 4;
    protected final static int tiltupbutton = 4;
    protected final static int tiltdownbutton = 2;
     protected final static int realignpin = 5;
     protected final static int autotarg = 10;
     protected final static int speedboost = 3;
     
     //Values go here!
     protected final static int expo_ramp = 3;
     protected final static double pivotconstant = 0.07; // because the robot drift-pivots
     protected final static double speedcap = 0.5; // speed the robot should usually move
     protected final static double climberspeed = 1.0;
     protected final static double deadzone = .05;
 } 
