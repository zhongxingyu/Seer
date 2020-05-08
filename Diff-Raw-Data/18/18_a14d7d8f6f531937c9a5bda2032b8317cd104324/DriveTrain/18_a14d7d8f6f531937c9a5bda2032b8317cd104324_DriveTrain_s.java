 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.deca.decabotz.subsystems;
 
 import edu.wpi.first.wpilibj.CANJaguar;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import org.deca.decabotz.RobotMap;
 import org.deca.decabotz.commands.DualStickDrive;
 
 /**
  *
  * @author decabots
  */
 public class DriveTrain extends Subsystem {
     // Put methods for controlling this subsystem
     // here. Call these from Commands.
 
     private RobotDrive drive;
 
     public DriveTrain() throws CANTimeoutException {
         CANJaguar leftJag;
         CANJaguar rightJag;
 
         try {
             leftJag = new CANJaguar(RobotMap.leftJagDriveID);
             rightJag = new CANJaguar(RobotMap.rightJagDriveID);
             drive = new RobotDrive(leftJag, rightJag);
             drive.setSafetyEnabled(false);
         } catch (CANTimeoutException e) {
             throw e;
         }
     }
 
     public void initDefaultCommand() {
         // Set the default command for a subsystem here.
         //setDefaultCommand(new MySpecialCommand());
         setDefaultCommand(new DualStickDrive());
     }
 
     public void tankDrive(double leftStick, double rightStick) {
         drive.tankDrive(leftStick, rightStick);
     }
 }
