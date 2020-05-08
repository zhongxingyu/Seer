 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.edinarobotics.zephyr.autonomous;
 import com.edinarobotics.utils.autonomous.*;
 import com.edinarobotics.zephyr.Zephyr;
 
 /**
 * This class idles {@link Zephyr} in a stopped state.
 * It calls {@link Zephyr#stop()} forever.
  */
 public class IdleStep extends AutonomousStep
 {
    Zephyr robot;
 
     /**
     * Constructs a new IdleStep for the given {@link Zephyr}.
     * @param robot {@link Zephyr}.
      */
     public IdleStep(Zephyr robot)
     {
         this.robot = robot;
     }
     
     /**
     * Calls {@link Zephyr#stop()} each time it is called.
      */
     public void run()
     {
         robot.stop();
     }
 
     /**
     * Returns {@code false}. This step runs forever.
     * @return {@code false}
      */
     public boolean isFinished()
     {
         return false;
     }
 
 }
