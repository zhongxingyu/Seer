 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.edinarobotics.zephyr.autonomous;
 import com.edinarobotics.utils.autonomous.*;
 import com.edinarobotics.zephyr.Zephyr;
 
 /**
 *
 * @author GreenMachine
  */
 public class IdleStep extends AutonomousStep
 {
    // Instantiate a robot
    Zephyr robot = new Zephyr();
 
     /**
     * Pass in the robot
     * @param robot
      */
     public IdleStep(Zephyr robot)
     {
         this.robot = robot;
     }
     
     /**
     * This method will stop moving the robot as the final step
      */
     public void run()
     {
         robot.stop();
     }
 
     /**
     * Never returns a true because this is the final step
     * @return (@code false)
      */
     public boolean isFinished()
     {
         return false;
     }
 
 }
