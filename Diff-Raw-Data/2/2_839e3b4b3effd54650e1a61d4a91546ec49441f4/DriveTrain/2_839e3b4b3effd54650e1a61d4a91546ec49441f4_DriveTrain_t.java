 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.technobots.subsystems;
 
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.technobots.RobotMap;
 import edu.wpi.first.wpilibj.technobots.commands.DriveWithXbox;
 
 /**
  *
  * @author miguel
  */
 public class DriveTrain extends Subsystem {
 
     RobotDrive motors;
 
     public DriveTrain() {
 
         super("DriveTrain");
 
         motors = new RobotDrive(RobotMap.upperLeft, RobotMap.lowerLeft, RobotMap.upperRight, RobotMap.lowerRight);
 
         motors.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
         motors.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
     }
 
     public void initDefaultCommand() {
         // Set the default command for a subsystem here.
         setDefaultCommand(new DriveWithXbox());
     }
 
    public void MecanumDrive(double magnitude, double direction, double rotation) {
 
          motors.mecanumDrive_Polar(magnitude, direction, rotation);
     }
 }
