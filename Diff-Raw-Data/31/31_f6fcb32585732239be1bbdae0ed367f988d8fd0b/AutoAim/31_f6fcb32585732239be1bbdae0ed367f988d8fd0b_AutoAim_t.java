 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.badrobots.y2012.technetium.commands;
 
 import com.badrobots.y2012.technetium.subsystems.Hermes;
 import com.sun.squawk.util.MathUtils;
 
 
 /*
  * @author 1014 Programming Team
  */
 public class AutoAim extends CommandBase 
 {
     static double power = 23; //(mph)

    /*
     *
     *
     *
     *
     *
     * THIS SHOULD BE DELETED
     *
     *
     *
     *DO NOT USE
     */


    public AutoAim(double DONTUSEME)
     {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(shooter);
         requires(sensors);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() 
     {
        /*//TODO: add autoaim code
         double motion = 0;//Hermes.getInstance().getMovement();
         
         double work = 400 - ((power * power) + (motion * motion));
         work /= (-2 * power * work);
         
         double theta = MathUtils.atan(work);
         theta *= (180/Math.PI); // convert to degreees!
         theta = 180 - theta;    // useful angle
         
        System.out.println("This should not run");*/
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
         return false;
     }
 
     // Called once after isFinished returns true
     protected void end() {
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
     }
 }
