 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.team811.vars;
 
 /**
  *
  * @author saswat
  */
 public interface Config {
     //Pyramid Dumping Hook
     int PYRAMID_DUMP_HOOK_PORT = 1;
 
     //Drive
     int LEFT_DRIVE_PORT = 1;
     int RIGHT_DRIVE_PORT = 2;
     double DRIVE_INPUT_NOISE_THRESHOLD = 0.26;//if xbox joystick is below this, the input is ignored
     int DRIVE_MAX_SPEED = 250;//max speed for the robot
     int DRIVE_SLOW_MAX_SPEED = 100;//max speed for slow mode
     double DRIVE_PID_P = 0.001;//PID Constant P for Encoder Drive
     double DRIVE_PID_I = 0.001;//PID Constant I for Encoder Drive
     double DRIVE_PID_D = 0.001;//PID Constant D for Encoder Drive
     
     //Encoder
     int LEFT_ENCODER_A_PORT = 1;
     int LEFT_ENCODER_B_PORT = 2;
     int RIGHT_ENCODER_A_PORT = 3;
     int RIGHT_ENCODER_B_PORT = 4;
     double DISTANCE_PER_PULSE = 1.5 / 9.9;
     
     //Joysticks
     int XBOX_1_PORT = 1;
     int XBOX_2_PORT = 2;
     
     //Climber
     int BIG_CLIMBER_PORT = 5;
     int SMALL_CLIMBER_PORT = 6;
     int BIG_ANGLE_PORT = 4;
     int SMALL_ANGLE_PORT = 3;
     
     //Controls
     int DRIVE_MOVE_AXIS = 2;   //Axis - left stick y
     int DRIVE_ROTATE_AXIS = 4; //Axis - right stick x
     int DRIVE_SLOW_MODE = 5;   //Button - left bumper
     int CLIMB_BIG_ANGLE_AXIS = 1;
     int CLIMB_BIG_CLIMBER_AXIS = 2;
     int CLIMB_SMALL_ANGLE_AXIS = 4;
     int CLIMB_SMALL_CLIMBER_AXIS = 5;
     
 }
