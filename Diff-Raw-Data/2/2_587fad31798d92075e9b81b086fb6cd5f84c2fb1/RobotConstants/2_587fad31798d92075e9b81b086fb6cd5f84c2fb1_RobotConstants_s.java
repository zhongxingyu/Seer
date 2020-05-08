 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.northwestrobotics.frc2013;
 
 /**
  * This class is responsible for containing all the constants for the robot.
  *
  * @author soggy.potato
  */
 public class RobotConstants {
 
     public static final class Drive {
 
         private Drive() {
         }
         public static final double THRESHOLD = .05;
         public static final int MOVE_CONTROLLER = 1;
         public static final double MAX_MOTOR_SPEED = 1;
         //Motors
         /* Front
          * T3 V4
          * V1 T2
          * Back
          * where T = Talon, and V = Victor
          */
         public static final int FRONT_LEFT_MOTOR = 3;
         public static final int FRONT_RIGHT_MOTOR = 4;
         public static final int BACK_LEFT_MOTOR = 1;
         public static final int BACK_RIGHT_MOTOR = 2;
     }
 
     public static final class Shooting {
 
         private Shooting() {
         }
         public static final int AIMING_CONTROLLER = 2;
         // on Controller 2
         public static final int SHOOT_BUTTON = 1;
         public static final int TOGGLE_SHOOT_MOTOR_BUTTON = 2;
         public static final int AIM_UP_BUTTON = 3;
         // Motors
         public static final int PITCH_MOTOR = 6;
         public static final int SHOOT_MOTOR = 5;
         // Shoot
         public static final double EXPIRATION_TIME = 10;
         public static final double MOTOR_SPEED = .5;
         public static final double PITCH_FACTOR = .5;
         // Feeder
         // soggy.potato
         // TODO: Finalize the motor speed
         public static final double SHOOT_MOTOR_SPEED = 1;
         public static final double SHOOT_MOTOR_DEACTIVATION_TIME = 3;// seconds
     }
 
     public static final class Pneumatics {
 
         private Pneumatics() {
         }
         public static final boolean MAX_PRESSURE = true;
         public static final int COMPRESSOR_RELAY = 5;
         public static final int PRESSURE_SWITCH_VALUE = 6;
         public static final int FEEDER_CHANNEL = 7;
         /**
          * Amount of time to wait to retract the arm
          *
          * @author AgentOrange
          * @author soggy.potato
          */
        public static final double FEEDER_WAIT_TIME = 1;// second
     }
 
     public static final class Loader {
 
         private Loader() {
         }
         public static final boolean MAX_PRESSURE = false;
         public static final int SUCKTIONCUP = 9;
         public static final int ARMRETRACTOR = 9;
         public static final int AIRCOMPRESSOR = 9;
         public static final int ARM = 9;
         public static final int COUNTER = 56;
     }
 
     public static final class TestDrive {
 
         private TestDrive() {
         }
         public static final int ACTIVATE_FRONT_LEFT_MOTOR_BUTTON = 6;
         public static final int ACTIVATE_FRONT_RIGHT_MOTOR_BUTTON = 11;
         public static final int ACTIVATE_BACK_LEFT_MOTOR_BUTTON = 7;
         public static final int ACTIVATE_BACK_RIGHT_MOTOR_BUTTON = 10;
     }
 }
