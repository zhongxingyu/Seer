 package edu.first.team2903.robot.commands;
 //TODO Make the robot drivable so that we can test the drivetrain and such.
 
 import edu.first.team2903.robot.subsystems.Joysticks;
 import edu.first.team2903.robot.subsystems.Drivetrain;
 
 
 public class Drive extends CommandBase{
 
     public Drive() {
     requires(drivetrain);
     requires(joysticks);
     }
 
     
     protected void initialize() {
     }
 
     protected void execute() {// v Ghetto code v
        if (joysticks.leftJoy.getY() > 0 && joysticks.rightJoy.getY()>0) {
 	                drivetrain.leftMotor.set(joysticks.leftJoy.getY());
	                drivetrain.rightMotor.set(joysticks.rightJoy.getY());
 	}
         else {
 	                drivetrain.leftMotor.set(joysticks.leftJoy.getY());
	                drivetrain.rightMotor.set(joysticks.leftJoy.getY());
         }
     
    
     }
     
    
     protected boolean isFinished() {
     return false;
     }
 
     protected void end() {
     }
 
     protected void interrupted() {
     }
     
 }
