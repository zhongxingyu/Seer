 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Jordan.bau5.FRC2115.subsystems;
 
 import Jordan.bau5.FRC2115.commands.DriveWithJoystick;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.command.Subsystem;
 
 /**
  *
  * @author Jordan Giacone
  */
 public class Chassis extends Subsystem 
 {
     public RobotDrive drive;
     // Put methods for controlling this subsystem
     // here. Call these from Commands.
 
     
    public Chassis()
     {
        drive = new RobotDrive(1, 2);
         drive.setSafetyEnabled(false);
     }
     
     public void Straight()
     {
         drive.tankDrive(1, 1);
     }
     
     public void Right()
     {
         drive.tankDrive(1, -1);
     }
     
     public void Left()
     {
         drive.tankDrive(-1, 1);
     }
     
     public void driveWithJoystick(Joystick j1, Joystick j2)
     {
         drive.tankDrive(j1, j2);
     }
     
     public void initDefaultCommand() 
     {
         setDefaultCommand(new DriveWithJoystick());
         // Set the default command for a subsystem here.
         //setDefaultCommand(new MySpecialCommand());
     }
 }
