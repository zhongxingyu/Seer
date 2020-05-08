 package com.edinarobotics.zed.commands;
 
 import com.edinarobotics.zed.Controls;
 import edu.wpi.first.wpilibj.command.Command;
 
 public class ToggleDrivetrainDirectionCommand extends Command {
     public ToggleDrivetrainDirectionCommand() {
     }
     
     protected void initialize() {
        Controls.driveStateForward = !Controls.driveStateForward;
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
