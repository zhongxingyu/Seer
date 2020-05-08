 
 package edu.wpi.first.wpilibj.templates.commands;
 import edu.wpi.first.wpilibj.command.CommandGroup;
 
 /**
  * A Command that extends CommandGroup to make an Autonomous program for the Center position, 
  * aiming for the top hoop.
  * @author Jessie
  */
 public class AutonomousCenterTop extends CommandGroup {
 
     /**
      * Creates an Autonomous Command that uses the directions for aiming using the
      * AutonomousStopTop and moving in the AutonomousCenterDirections.
      */
     public AutonomousCenterTop() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         
        //addSequential(new AutonomousStopTop());
         addSequential(new AutonomousCenterDirections());
     }
 }
