 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.badrobots.y2012.technetium.buttons;
 
 import com.badrobots.y2012.technetium.OI;
 import com.badrobots.y2012.technetium.commands.Balance;
 import com.badrobots.y2012.technetium.commands.GatherBallsAndAutoShoot;
 import com.badrobots.y2012.technetium.commands.GatherBallsAndManualShoot;
 import edu.wpi.first.wpilibj.buttons.Button;
 
 /**
  *
  * @author Jon Buckley
  */
 public class TrackingButton extends Button
 {
 //NEEDS TESTING
     public TrackingButton()
     {
         
         super.whenPressed(new GatherBallsAndAutoShoot());
     }
 
     public boolean get()
     {
         if (OI.secondXboxBButton())
             return true;
        new GatherBallsAndManualShoot();
         return false;
     }
 
 
 }
