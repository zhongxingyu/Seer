 package com.badrobots.y2012.technetium;
 
 /**
  * The RobotMap is a mapping from the ports sensors and actuators are wired into
  * to a variable name. This provides flexibility changing wiring, makes checking
  * the wiring easier and significantly reduces the number of magic numbers
  * floating around.
  */
 
 /*
  * @author 1014 Programming Team
  */
 public class RobotMap
 {
 
     public static final int cRIOsidecar = 1; // port from the cRIO to the digital
     // sidecar
     public static final int lFront = 4; // Mike-- 4
     public static final int rFront = 2; // 2
     public static final int lBack = 3; // 3
     public static final int rBack = 1; // 1
     public static final int leftJoystick = 1;
     public static final int rightJoystick = 2;
     public static final int controller = 1;
     public static final int controller2 = 2;
     public static final int shooterJoystick = 4;
     public static final int ultrasonicOut = 3; //2 scotty 3 mike
     public static final int ultrasonicIn = 2; //3 
     public static final int topSensor = 9; //5 Scotty
     public static final int bottomSensor = 4; //7
     public static final int camera = 4;
     public static final int horizontalGyro = 1;
     //public static final int verticalGyro = 10;
     public static final int conveyor = 1;
     public static final int bottomRoller = 2;
     public static final int leftShooter = 5;
     public static final int rightShooter = 6;
     public static final int bridgingTool = 8;
     public static final int turnTable = 9;
     public static final int turnTableEncoderAChannel = 9;
     public static final int turnTableEncoderBChannel = 8;
     public static final int shooterGearTooth = 5;
 }
