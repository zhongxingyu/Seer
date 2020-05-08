 package org.usfirst.frc4682.Audacity.subsystems;
 import org.usfirst.frc4682.Audacity.RobotMap;
 import org.usfirst.frc4682.Audacity.commands.*;
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.command.Subsystem;
 
 public class Chassis extends Subsystem {
     SpeedController leftMotor = RobotMap.chassisleftMotor;
     SpeedController rightMotor = RobotMap.chassisrightMotor;
     public RobotDrive drive = RobotMap.chassisDrive;
     // Put methods for controlling this subsystem
     // here. Call these from Commands.
     public void initDefaultCommand() {
 	
         // Set the default command for a subsystem here.
         setDefaultCommand(new DriveWithJoysticks());
     }
     
     public void driveForward() {
        drive.setSafetyEnabled(false);
         drive.drive(0.5, 0);
         Timer.delay(2);
         drive.drive(0, 0);
     }
     
     public void tankDrive(double left, double right) {
         drive.tankDrive(left, right);
         // Should this be in a while loop?
         // Should we set setEnabled to False?
     }
 }
