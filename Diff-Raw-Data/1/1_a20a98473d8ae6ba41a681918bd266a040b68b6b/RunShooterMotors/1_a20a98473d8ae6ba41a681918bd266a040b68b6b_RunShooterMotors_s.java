 package edu.wpi.first.wpilibj.templates.commands;
 
 import edu.wpi.first.wpilibj.templates.debugging.RobotDebugger;
 import edu.wpi.first.wpilibj.templates.vstj.VstJ;
 
 /**
  *
  */
 public class RunShooterMotors extends CommandBase {
 
     public RunShooterMotors() {
         requires(shooterMotors);
     }
 
     protected void initialize() {
         shooterMotors.setSpeed(0);
     }
     private boolean motorsToggled = false;
     private boolean buttonPressedLast = false;
 
     protected void execute() {
         setToggled();
         setMotors();
         RobotDebugger.push(shooterMotors);
     }
 
     private void setToggled() {
         if (VstJ.getShooterMotorToggleButtonValue() != buttonPressedLast) {
             buttonPressedLast = !buttonPressedLast;
             motorsToggled = !motorsToggled;
         }
     }
 
     private void setMotors() {
         shooterMotors.setSpeed(motorsToggled ? 1 : 0);
     }
 
     protected boolean isFinished() {
         return false;
     }
 
     protected void end() {
         shooterMotors.setSpeed(0);
     }
 
     protected void interrupted() {
         end();
     }
 }
