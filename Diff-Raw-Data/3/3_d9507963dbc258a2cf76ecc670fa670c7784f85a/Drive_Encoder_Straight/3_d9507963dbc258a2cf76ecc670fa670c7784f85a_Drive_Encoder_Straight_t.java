 package org.buzzrobotics.commands;
 import org.buzzrobotics.commands.CommandBase;
 /**
  * Drives Autonomously, straight, to the amount of inches the encoder spins.
  * This is AMAZING. Drives straight using GYROSCOPE.
  * @author Kyle Deane
  */
 public class Drive_Encoder_Straight extends CommandBase{
 
     private double inches_to_travel;
     public double direction;
     public Drive_Encoder_Straight(double inches) {
         requires(drivetrain);
         inches_to_travel = inches;
         if (inches > 0){
             drivetrain.setForward();
         }else if (inches < 0){
             drivetrain.setBackwards();
         }
        setInterruptible(true); //Might cause issues
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
         drivetrain.resetEncoders();
         drivetrain.setForward();
         drivetrain.driveStraight();
         drivetrain.initController(); // Enables "drive straight" controller
     }
 
     // Called repeatedly when this Command is scheduled to run
     public void execute(){
         System.out.println("DRIVING." + drivetrain.getAvgDistance() + "  InchesToTravel: " + inches_to_travel);     
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
        
         return Math.abs(drivetrain.getAvgDistance()) > Math.abs(inches_to_travel); // Check if we have traveled the right distance by encoder measure
     }
 
     // Called once after isFinished returns true
     protected void end() {
         drivetrain.endController();
         drivetrain.tankDrive(0, 0);
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
        end();
     }
 }
