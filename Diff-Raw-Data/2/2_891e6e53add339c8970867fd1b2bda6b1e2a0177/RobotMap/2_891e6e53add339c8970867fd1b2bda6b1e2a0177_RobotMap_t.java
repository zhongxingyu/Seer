 package edu.wpi.first.wpilibj.technobots;
 
 /**
  * The RobotMap is a mapping from the ports sensors and actuators are wired into
  * to a variable name. This provides flexibility changing wiring, makes checking
  * the wiring easier and significantly reduces the number of magic numbers
  * floating around.
  */
 public class RobotMap {
 
    //Jaguars
     public static final int lowerRight = 1,
                             lowerLeft  = 2,
                             upperRight = 3,
                             upperLeft  = 4;
 
     /**
      * Victors
      *
      * Sweeper is reverse
      * Shooter is reverse
      */
     public static final int shooterRight = 5,
                             shooterLeft  = 6,
                             sweeper      = 7,
                             elbow        = 8;
     //Sensors
     public static final int pot         = 1,
                             rangeFinder = 2;
 
     //Solenoid
     public static final int ballStopper1Up   = 1,
                             ballStopper1Down = 2;
 }
