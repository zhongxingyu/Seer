 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.badrobots.y2012.technetium.subsystems;
 
 import com.badrobots.y2012.technetium.OI;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import com.badrobots.y2012.technetium.RobotMap;
 import com.badrobots.y2012.technetium.buttons.ShootBallTrigger;
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.buttons.Button;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.camera.AxisCameraException;
 import edu.wpi.first.wpilibj.image.BinaryImage;
 import edu.wpi.first.wpilibj.image.ColorImage;
 import edu.wpi.first.wpilibj.image.NIVisionException;
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 import edu.wpi.first.wpilibj.smartdashboard.SendablePIDController;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 
 /*
  * @author 1014 Programming Team
  */
 public class Artemis extends Subsystem
 {
     private static Artemis instance;
     private static Victor right, left;
     private static Victor turnTable; //TODO Change to correct speed controller
     private static Ultrasonic ranger;
     
    private static OpticalSensorPID shooterGearTooth;
     private static Encoder turnTableEncoder;
     
     protected SoftPID shooterPIDOutput;
     protected SoftPID turnTablePIDOutput;
     
     protected double speedOfShooter = 0; 
     
     protected SendablePIDController shooterController;
     protected SendablePIDController turnTableController; 
    
     public static final int MAX_SPEED = 3600;
 
     public static final double SHOOTER_P = .01;
     public static final double SHOOTER_I = 0;
     public static final double SHOOTER_D = 0;
     
     public static final double TURNTABLE_P = .01;
     public static final double TURNTABLE_I = 0;
     public static final double TURNTABLE_D = 0;
     
     public static Artemis getInstance()
     {
         if (instance == null)
         {
             instance = new Artemis();
         }
 
         return instance;
     }
 
     private Artemis()
     {
         super();
         right = new Victor (RobotMap.rightShooter); // initialize the motor
         left = new Victor (RobotMap.leftShooter);
         
         if (OI.shooterPIDOn)
         {
            //we will need to change this variable name, but it's fine
            shooterGearTooth = new OpticalSensorPID();//GearToothPID(RobotMap.shooterGearTooth);
            shooterGearTooth.start();
             shooterPIDOutput = new SoftPID();
             shooterController = new SendablePIDController(SHOOTER_P, SHOOTER_I, SHOOTER_D, shooterGearTooth, shooterPIDOutput);  
            shooterController.setInputRange(0, 1);
            shooterController.setOutputRange(0, 1);
            shooterController.enable();
             
             SmartDashboard.putData("ShooterPID", shooterController);
         }
        
         
         turnTableEncoder = new Encoder(RobotMap.turnTableEncoderAChannel, RobotMap.turnTableEncoderBChannel);
         turnTableEncoder.start();
         turnTableEncoder.reset();  
         
         if (OI.turnTablePIDOn)
         {
             turnTablePIDOutput = new SoftPID();
             turnTableController = new SendablePIDController(TURNTABLE_P, TURNTABLE_I, TURNTABLE_D, turnTableEncoder, turnTablePIDOutput);
             turnTableController.setTolerance(5);
             
             SmartDashboard.putData("TurnTablePID", turnTableController);
         }
         
         turnTable = new Victor(RobotMap.turnTable);
     }
 
     
     /*
      * Turns the turnTable/turreter
      * 
      * @param speed from -1 to 1, where -1 is full speed counterclockwise and 
      * 1 is full speed clockwise
      */
     public void turn(double speed)
     {
         clampMotorValues(speed);
         turnTable.set(-speed);
     }
 
     public void setGearPIDRunning(boolean b)
     {
         if(shooterGearTooth != null)
             shooterGearTooth.setRunning(b);
         //System.out.println("RunningPID: " + b);
     }
 
     public void shootMiddle() // read distance from kinect/ultrasonic
     {
         //determine shooter speed through regression equation determined through testing
         int speed = 0;
         run(speed);
     }
 
     public void shootHigh() // read distance from kinect/ultrasonic
     {
         //determine shooter speed through regression equation determined through testing
         double speed = .45;
         run(speed);
     }
 
     public void shootLow() // read distance from kinect/ultrasonic
     {
         //determine shooter speed through regression equation determined through testing
         int speed = 0;
         run(speed);
     }
     
     /*
      * Uses the PIDGearTooth to run the shooter more efficiently
      */
     public void PIDRun(double speed)
     { 
         //the setpoint is set to the max RPMs (which is 3600 RPM) times 6 (number of bolts in gearbox)
        shooterController.setSetpoint((1/MAX_SPEED)/speed);
         
         double speedToSet = shooterPIDOutput.getValue();
         speedToSet = clampMotorValues(speedToSet);
         
        System.out.println("speed to set  " + speedToSet);
         right.set(-speedToSet);
         left.set(speedToSet);
     }
     
     /*
      * Runs both motors at 'speed' speed
      */
     public void run(double speed)
     {
         clampMotorValues(speed);
         
        if (OI.shooterPIDOn)
         {
             PIDRun(speed);
         }
         
         else
         {
             right.set(-speed);
             left.set(speed);
         }
 
         SmartDashboard.putDouble("ShooterRate", shooterGearTooth.pidGet());
         
     }
 
     private double clampMotorValues(double scaledStrafe)
     {
         if (scaledStrafe > 1)
         {
             scaledStrafe = 1;
         }
         if (scaledStrafe < -1)
         {
             scaledStrafe = -1;
         }
         return scaledStrafe;
     }
 
     public void resetEncoder()
     {
         turnTableEncoder.reset();
     }
 
     public int encoderValue()
     {
         return turnTableEncoder.get();
     }
 
     /*
      * Turns the shooter to a specific encoder value
      */
     public boolean turnByEncoderTo(int value)
     {
         if (OI.turnTablePIDOn)
         {
             return turnWithPID(value);
         }
         
         int currentAngle = turnTableEncoder.get();
 
         if(Math.abs(currentAngle - value) <= 8)
         {
             turn(0);
             return true;
         }
         else if(currentAngle - value > 0)
             turn(.50);
         else
             turn(-.50);
         return false;
     }
     
     /*
      * Uses PID to turn the turntable to a setpoint specified in the @parameter value
      */
     public boolean turnWithPID(int value)
     {
         turnTableController.setSetpoint(value);
         turn(turnTableController.get());
         
         //if the turnTable is within the point you tell it to go to, return true
         if (turnTableController.get() < .05)
             return true;
         
         return false;
     }
     
     public double distanceToWall()
     {
         if (ranger != null)
             return ranger.getRangeMM();
         
         return -1;
     }
     
 
     protected void initDefaultCommand()
     {
         System.out.println("DefaultCommandArtemis");
         //super.setDefaultCommand(new GatherBalls);
     }
 }
