 package edu.wpi.first.wpilibj.templates.subsystems;
 
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.CANJaguar;
 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 import edu.wpi.first.wpilibj.templates.RobotMap;
 import edu.wpi.first.wpilibj.Victor;
 import edu.wpi.first.wpilibj.templates.commands.ShootOff;
 
 /**
  * A Subsystem that contains methods to control the Shooter.  
  * @author Emma and Jessie
  */
 public class Shooter extends Subsystem {
     // Put methods for controlling this subsystem
     // here. Call these from Commands.
 
     Victor shootMotor = new Victor(RobotMap.shootMotor);
     Victor shootMotor2 = new Victor(RobotMap.shootMotor2);
     public boolean atSpeed = false;
 
     public Shooter() {
     }
 
     /**
      * Default: calls ShootOff, so the shooter speed is set to zero.  
      */
     public void initDefaultCommand() {
         // Set the default command for a subsystem here.
         //setDefaultCommand(new MySpecialCommand());
        
     }
 
     /**
      * Sets the speed of the two Shooter motors.
      * @param speed a speed between -1 and 1.  
      */
     public void setSpeed(double speed) {
         shootMotor.set(-speed);
         shootMotor2.set(-speed);
     }
 
     /**
      * Gets the average of the two Shooter motor speeds.
      * @return the average of the two Shooter motor speeds.  
      */
     public double getShooterSpeed() {
         return (-shootMotor.get() + -shootMotor2.get()) / 2;
     }
 
     /**
      * Determines if the Shooter is up to speed.  
      * @return true if the Shooter is up to speed, false if it is not.  
      */
     public boolean upToSpeed() {
         return atSpeed;
     }
 }
