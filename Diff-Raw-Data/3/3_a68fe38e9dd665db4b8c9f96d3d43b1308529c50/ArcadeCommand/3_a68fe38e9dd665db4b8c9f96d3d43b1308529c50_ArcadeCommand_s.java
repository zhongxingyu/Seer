 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.usfirst.frc2022.commands;
 import org.usfirst.frc2022.Joysticks.Xbox360;
 import org.usfirst.frc2022.Utils;
 
 
 /**
 *
 * @author Michael
  */
 public class ArcadeCommand extends CommandBase{
 
     //declare variables
     Xbox360 controller;
     
     /**
      * The constructor. It requires pwmGeneric from CommandBase.java
      * 
      * @author Titan Robotics (2022)
      * @param
      * @return
      */
     public ArcadeCommand(){
         requires(pwmGeneric);
         
     }
     
     /**
      * This function is called when the command starts.
      * It registers the controller and sets the speeds.
      * 
      * @author Titan Robotics (2022)
      * @param
      * @return 
      */
     protected void initialize() {
         controller = oi.getXbawks();
     }
 
     /**
      * The primary loop for the arcade drive. Gets controller input
      * and does sends commands to PWM_Generic.java to move the bot.
      * 
      * @author Titan Robotics (2022)
      * @param
      * @return 
      */
     protected void execute() {
         
         //The direction and magnitude and rotation
         double x = Utils.clamp(controller.GetLeftX(),1,-1);
         double y = Utils.clamp(controller.GetLeftY(),1,-1);
 	
         pwmGeneric.drive((y+x), (y-x));
     }
 
     protected boolean isFinished() {
         return false;
     }
     
     /**
      * This function is called when the command ends.
      * 
      * @author Titan Robotics (2022)
      * @param
      * @return 
      */
     protected void end() {
         pwmGeneric.stop();
     }
 
     /**
      * This function is called when the command is interrupted.
      * 
      * @author Titan Robotics (2022)
      * @param
      * @return 
      */
     protected void interrupted() {
         pwmGeneric.stop();
     }
 
 }
