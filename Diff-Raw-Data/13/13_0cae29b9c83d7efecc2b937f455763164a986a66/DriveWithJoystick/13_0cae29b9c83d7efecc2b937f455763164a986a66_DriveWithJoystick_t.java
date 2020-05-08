 package RobotsAreCool.commands;
 
 public class DriveWithJoystick extends CommandBase 
 {
     
     public DriveWithJoystick() 
     {
        requires(chassis);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() 
     {
        //Stopping Motor output to ensure correct input on Joysticks
        chassis.getDrive().setLeftRightMotorOutputs(0, 0);
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() 
     {
//        double leftAxis = oi.jLeftWheel.getRawAxis(oi.leftAxis);
//        double rightAxis = oi.jRightWheel.getRawAxis(oi.rightAxis);
        
        //chassis.driveWithJoystick(leftAxis, rightAxis);
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() 
     {
         return false;
     }
 
     // Called once after isFinished returns true
     protected void end() 
     {
     
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() 
     {
         
     }
 }
