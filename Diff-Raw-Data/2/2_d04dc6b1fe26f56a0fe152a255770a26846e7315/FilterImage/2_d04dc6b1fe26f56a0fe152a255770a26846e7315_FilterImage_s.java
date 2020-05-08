 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.slidellrobotics.reboundrumble.commands;
 
 import com.slidellrobotics.reboundrumble.subsystems.TrackingCamera;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.command.CommandGroup;
 
 /**
  *
  * @author Allister Wright
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
         
         /* Sets a Counter to Delay Processing to Reduce Lag */
         thisTime = Timer.getFPGATimestamp();
         timeLapse = thisTime - lastTime;
         
         if(timeLapse >= 1.0) {  //  If one second has passed since the last picture processing
             addSequential(new GetImage());  //  Get a picture and filter to a Binary Image
            if (TrackingCamera.reports.length > 0) {   //  If one or more goals are found
                 /* Perform the remaining three processes */
                 addSequential(new SelectGoal());
                 addSequential(new FindAngle());
                 addSequential(new FindDistance());
             } else {    //  If no goals are found
                 System.out.println("Goal Selection and Analysis Aborted");  //  Print a notifier
             }
             lastTime = thisTime;    //  Reset the Counter for the Delay
         }
     }
 }
