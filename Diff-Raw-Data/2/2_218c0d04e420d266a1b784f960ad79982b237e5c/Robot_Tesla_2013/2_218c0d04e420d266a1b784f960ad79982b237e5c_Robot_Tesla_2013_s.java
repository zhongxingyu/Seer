 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 
 //import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Compressor;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.SimpleRobot;
 import edu.wpi.first.wpilibj.SpeedController;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Victor;
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the SimpleRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class Robot_Tesla_2013 extends SimpleRobot 
 {
     /**
      *  Constants for tuning/adjusting the robot
      */
     
     private static final int DIO_SLOT = 1; //Only a single slot
     private static final int LEFT_MOTOR_CHANNEL = 1;
     private static final int RIGHT_MOTOR_CHANNEL = 2;
     private static final int MOTOR_CHANNEL_3 = 3; //Rename
     private static final int MOTOR_CHANNEL_4 = 4;
     private static final int MOTOR_CHANNEL_5 = 5;
     
     SpeedController m_LeftDriveMotor;
     SpeedController m_RightDriveMotor;
     SpeedController m_Motor_3; //Rename
     SpeedController m_Motor_4;
     SpeedController m_Motor_5;
     
     private static final int LEFT_X = 1;
     private static final int LEFT_Y = 2;
     private static final int UNKNOWN = 3; //triggers?
     private static final int RIGHT_X = 4;
     private static final int RIGHT_Y = 5;
     
     Joystick m_Driver; //Driver controller
     Joystick m_Secondary; //Shooter and Secondary controller
     
     RobotDrive m_RobotDrive;
     
     Compressor m_Compressor;
     
     protected void robotInit() 
     {
         m_LeftDriveMotor = new Victor(DIO_SLOT, LEFT_MOTOR_CHANNEL); //Digtial I/O,Relay
         m_RightDriveMotor = new Victor(DIO_SLOT, RIGHT_MOTOR_CHANNEL);
         m_Motor_3 = new Victor(DIO_SLOT, MOTOR_CHANNEL_3);
         m_Motor_4 = new Victor(DIO_SLOT, MOTOR_CHANNEL_4);
         m_Motor_5 = new Victor(DIO_SLOT, MOTOR_CHANNEL_5);
         
         m_Driver = new Joystick(1); //USB Port
         m_Secondary = new Joystick(2); 
         m_RobotDrive = new RobotDrive(m_LeftDriveMotor, m_RightDriveMotor);
         
         m_Compressor = new Compressor(DIO_SLOT,1); //Digtial I/O,Relay
         m_Compressor.start(); 
      
     }   
     
     public void drive()
     {
        m_RobotDrive.tankDrive(m_Driver.getRawAxis(LEFT_Y), m_Driver.getRawAxis(RIGHT_Y), false);
         //m_Driver.getRawAxis()*-1 to invert
         
     }
     
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
     public void autonomous() 
     {
         getWatchdog().setEnabled(false);
     }
 
     /**
      * This function is called once each time the robot enters operator control.
      */
     public void operatorControl() 
     {
         getWatchdog().setEnabled(true);
         while (isOperatorControl() && isEnabled()) // loop during enabled teleop mode
             {             
             drive(); //Call drive function
             getWatchdog().feed(); //Feed the dog
             Timer.delay(0.005); //Delay loop
             }     
     }
     
     /**
      * This function is called once each time the robot enters test mode.
      */
     public void test() 
     {
     
     }
    
 }
