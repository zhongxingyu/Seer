 package com.edinarobotics.zed.commands;
 
 import com.edinarobotics.zed.Components;
 import com.edinarobotics.zed.subsystems.Lifter;
 import edu.wpi.first.wpilibj.command.Command;
 
 public class LiftShooterToAngleCommand extends Command {
     private Lifter lifter;
     private double angle;
     private boolean isOnTarget = false;
     
     private final double TOLERANCE = 5;
     
     public LiftShooterToAngleCommand(double angle) {
         super("LiftShooterToAngle");
         this.lifter = Components.getInstance().lifter;
         this.angle = angle;
     }
     
     protected void initialize() {
     }
 
     protected void execute() {
         if(angle > (lifter.getShooterAngle() - TOLERANCE)) {
             isOnTarget = false;
             lifter.setLifterDirection(Lifter.LifterDirection.LIFTER_UP);
         } else if(angle < (lifter.getShooterAngle() + TOLERANCE)) {
             isOnTarget = false;
             lifter.setLifterDirection(Lifter.LifterDirection.LIFTER_DOWN);
         } else {
             isOnTarget = true;
             lifter.setLifterDirection(Lifter.LifterDirection.LIFTER_STOP);
         }
     }
 
     protected boolean isFinished() {
         return isOnTarget;
     }
 
     protected void end() {
        lifter.setLifterDirection(Lifter.LifterDirection.LIFTER_STOP);
     }
 
     protected void interrupted() {
         end();
     }
 }
