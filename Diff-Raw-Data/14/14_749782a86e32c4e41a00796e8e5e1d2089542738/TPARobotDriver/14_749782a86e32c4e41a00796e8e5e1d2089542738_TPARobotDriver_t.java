 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.SpeedController;
 
 /**
  *
  * @author Gennaro
  */
 public class TPARobotDriver extends RobotDrive {
     
     public static double kDefaultMinOutput=0.1; //Min value for the Speed Multiplier
     public SpeedController theFrontLeftJaguar;           //Front left motor
     public SpeedController theRearLeftJaguar;            //Rear left motor
     public SpeedController theFrontRightJaguar;          //Front right motor
     public SpeedController theRearRightJaguar;           //Rear right motor
 
     //Constructors:
     TPARobotDriver(final int leftMotorChannel, final int rightMotorChannel){
         super(leftMotorChannel, rightMotorChannel);
        /* Need to implement jaguar for other mechanum wheels
         theFrontLeftJaguar = new Jaguar(leftMotorChannel);
         theRearLeftJaguar = new Jaguar(leftMotorChannel);
         theFrontRightJaguar = new Jaguar(rightMotorChannel);
        theRearRightJaguar = new Jaguar(rightMotorChannel);*/
     }
     
     TPARobotDriver(final int frontLeftMotor, final int rearLeftMotor,
                    final int frontRightMotor, final int rearRightMotor) {
         super(frontLeftMotor, rearLeftMotor, frontRightMotor, rearRightMotor);
        // Implements the motor channels as jaguars for mecanum compatibility
        theFrontLeftJaguar = new Jaguar(frontLeftMotor);
        theRearLeftJaguar = new Jaguar(rearLeftMotor);
        theFrontRightJaguar = new Jaguar(frontRightMotor);
        theRearRightJaguar = new Jaguar(rearRightMotor);
     }
     
     TPARobotDriver(SpeedController leftMotor, SpeedController rightMotor) {
         super(leftMotor, rightMotor);
         //theFrontLeftJaguar = leftMotor;
         //theRearLeftJaguar = leftMotor;
         //theFrontRightJaguar = rightMotor;
         //theRearRightJaguar = rightMotor;
         //Need to implement Speed Controllers for brake
     }
     
     TPARobotDriver(SpeedController frontLeftMotor, SpeedController rearLeftMotor,
                    SpeedController frontRightMotor, SpeedController rearRightMotor) {
         super(frontLeftMotor, rearLeftMotor, frontRightMotor, rearRightMotor);
         //theFrontLeftJaguar = frontLeftMotor;
         //theRearLeftJaguar = rearLeftMotor;
         //theFrontRightJaguar = frontRightMotor;
         //theRearRightJaguar = rearRightMotor;
         //Need to implement Speed Controllers for brake
     }
     /*--------------------------------------------------------------------------*/
     
     /*
      * Author:  Gennaro De Luca
      * Date:    11/26/11 (Gennaro De Luca)
      * Purpose: Tests the input value to make sure that it is a safe value, then
      *          sets that multiplier.
      * Inputs:  double aMaxSpeed - wanted value for the speed multiplier 
      * Outputs: None
      */
     
     public void setMaxSpeed(double aMaxSpeed) {
         
         if(aMaxSpeed<kDefaultMinOutput)  {      //Tests to see if value is less than min allowed
             aMaxSpeed=kDefaultMinOutput;
         }
         else if(aMaxSpeed>kDefaultMaxOutput) {  //Tests to see if value is more than max allowed
             aMaxSpeed=kDefaultMaxOutput;
         }
         m_maxOutput=aMaxSpeed;                  //sets the multiplier to be the tested value
     }
    /*--------------------------------------------------------------------------*/
     
        /*--------------------------------------------------------------------------*/
    /*
     * Author:  Marissa Beene
     * Date:    10/30/2011 (Marissa Beene)
     * Purpose: To use the motors to brake the robot. Takes the speed from the 
     *          each motor and sends the reverse signal back.
     * Inputs:  Double aSpeedRight - the speed of the right motor
     *          Double aSpeedLeft - the speed of the left motor
     *          RobotDrive aRobotDrive - the drive system of the robot
     * Outputs: None
     */
     
     public void twoMotorBrake(double aSpeedLeft, double aSpeedRight, RobotDrive aRobotDrive){
         aRobotDrive.tankDrive(-aSpeedLeft, -aSpeedRight); //drive the robot at opposite values
     }
     /*--------------------------------------------------------------------------*/
    
     
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    1/14/2012
      * Purpose: To use the motors to brake the robot when it is using mecanum wheels.
      *          Takes the speed from each motor and sends the reverse signal back.
      * Inputs:  None
      * Outputs: None
      */
     
     public void mecanumBrake(double aSpeedFrontLeft, double aSpeedRearLeft, double aSpeedFrontRight, double aSpeedRearRight){
         theFrontLeftJaguar.set(-aSpeedFrontLeft);
         theFrontRightJaguar.set(-aSpeedFrontRight);
         theRearLeftJaguar.set(-aSpeedRearLeft);
         theRearRightJaguar.set(-aSpeedRearRight);
     }
 
     /*--------------------------------------------------------------------------*/
 
     
     
 }
