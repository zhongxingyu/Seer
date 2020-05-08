 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.slidellrobotics.reboundrumble.commands;
 
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.command.CommandGroup;
 
 /**
  *
  * @author 10491477
  */
 public class FilterImage extends CommandGroup {
     private static double lastTime = 0;
     private static double thisTime;
     private static double timeLapse;
     
     public FilterImage() {
         // Add Commands here:
         // e.g. addSequential(new Command1());
         //      addSequential(new Command2());
         // these will run in order.
 
         // To run multiple commands at the same time,
         // use addParallel()
         // e.g. addParallel(new Command1());
         //      addSequential(new Command2());
         // Command1 and Command2 will run in parallel.
 
         // A command group will require all of the subsystems that each member
         // would require.
         // e.g. if Command1 requires chassis, and Command2 requires arm,
         // a CommandGroup containing them would require both the chassis and the
         // arm.
         thisTime = Timer.getFPGATimestamp();
         timeLapse = thisTime - lastTime;
         if(timeLapse >= 1.0) {
             addSequential(new GetImage());
            addSequential(new SelectGoal());
            addSequential(new FindAngle());
            addSequential(new FindDistance());
             lastTime = thisTime;
         }
     }
 }
