 
 package edu.wpi.first.wpilibj.templates.commands;
 import edu.wpi.first.wpilibj.command.CommandGroup;
 import edu.wpi.first.wpilibj.templates.commands.Go;
 import edu.wpi.first.wpilibj.templates.commands.Spinning;
 import edu.wpi.first.wpilibj.templates.commands.Aim;
 import edu.wpi.first.wpilibj.templates.commands.LoadBall;
 import edu.wpi.first.wpilibj.templates.commands.ShootBall;
 import edu.wpi.first.wpilibj.templates.subsystems.DriveTrain;
 import edu.wpi.first.wpilibj.templates.commands.ShmackDown;
 
 /**
  * A Command that extends CommandGroup to make an Autonomous program for the left position, 
  * aiming for the top hoop.
  * @author Jessie
  */
 public class AutonomousLeftTop extends CommandGroup {
 
     /**
      * Creates an Autonomous Command that uses the directions for aiming using the
      * AutonomousStopTop and moving in the AutonomousLeftDirections.
      */
     public AutonomousLeftTop() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         
        //addSequential(new AutonomousStopTop());
         addSequential(new AutonomousLeftDirections());
         
     }
 }
