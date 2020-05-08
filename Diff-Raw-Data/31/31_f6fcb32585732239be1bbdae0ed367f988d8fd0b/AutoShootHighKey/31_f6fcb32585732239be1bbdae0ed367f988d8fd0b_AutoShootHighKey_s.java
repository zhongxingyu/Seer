 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.badrobots.y2012.technetium.commands;
 
 import edu.wpi.first.wpilibj.command.CommandGroup;
 
 /**
  *
  * @author 1014
  */
 public class AutoShootHighKey extends CommandGroup {
 
     public AutoShootHighKey()//NEEDS TESTING
     {
        addSequential(new AutoAim());
         addSequential(new AutoShootAndGather(.45, true));
     }
 }
