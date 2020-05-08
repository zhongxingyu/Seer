 package com.edinarobotics.zed.commands;
 
 import com.edinarobotics.zed.Controls;
 import edu.wpi.first.wpilibj.command.Command;
 
 public class ToggleDrivetrainDirectionCommand extends Command {
    private Controls controls;
    
     public ToggleDrivetrainDirectionCommand() {
        controls = Controls.getInstance();
     }
     
     protected void initialize() {
        controls.driveStateForward = !controls.driveStateForward;
     }
 
     protected void execute() {
     }
 
     protected boolean isFinished() {
         return true;
     }
 
     protected void end() {
     }
 
     protected void interrupted() {
         end();
     }
     
 }
