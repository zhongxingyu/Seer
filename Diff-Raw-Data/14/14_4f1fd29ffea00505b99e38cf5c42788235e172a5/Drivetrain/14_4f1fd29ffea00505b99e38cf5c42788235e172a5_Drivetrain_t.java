 package com.edinarobotics.zed.subsystems;
 
 import edu.wpi.first.wpilibj.RobotDrive;
 
 public class Drivetrain {
     private RobotDrive robotDrive;
     private double magnitude;
     private double direction;
     private double rotation;
     
     public Drivetrain(int frontLeft, int frontRight, int backLeft, int backRight) {
         this.robotDrive = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
        robotDrive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
        robotDrive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, false);
        robotDrive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
        robotDrive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, false);
     }
     
     public void mecanumPolarStrafe(double magnitude, double direction){
         this.magnitude = magnitude;
         this.direction = direction;
         update();
     }
     
     public void mecanumPolarRotation(double rotation){
         this.rotation = rotation;
         update();
     }
     
     public void update(){
         robotDrive.mecanumDrive_Polar(magnitude, direction, rotation);
     }
 }
