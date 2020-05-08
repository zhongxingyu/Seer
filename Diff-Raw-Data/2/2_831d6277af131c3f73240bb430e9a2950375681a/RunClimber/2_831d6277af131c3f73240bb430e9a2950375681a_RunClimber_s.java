 package edu.wpi.first.wpilibj.templates.commands;
 
 import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import edu.wpi.first.wpilibj.templates.debugging.DebugInfoGroup;
 import edu.wpi.first.wpilibj.templates.debugging.Debuggable;
 import edu.wpi.first.wpilibj.templates.debugging.InfoState;
 import edu.wpi.first.wpilibj.templates.debugging.RobotDebugger;
 import edu.wpi.first.wpilibj.templates.variablestores.VstM;
 import edu.wpi.first.wpilibj.templates.vstj.VstJ;
 
 /**
  *
  */
 public class RunClimber extends CommandBase implements Debuggable {
 
     private SendableChooser isClimbing;
 
     public RunClimber() {
         requires(climber);
         isClimbing = new SendableChooser();
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
         climber.stop();
         isClimbing.addDefault("Disable Climber", Boolean.FALSE);
         isClimbing.addObject("Enable Climber", Boolean.TRUE);
         SmartDashboard.putData("Climber", isClimbing);
 
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
         checkEnabled();
         if (isEnabled) {
             double climbSpeed = VstJ.getDefaultJoystick().getRawAxis(VstJ.getClimberAxisNumber());
             climbSpeed *= 0.25;
             //The Following Code Won't Affect Anything If The Pressure Switches Are Not Attached/Not Pressed.
             if (climbSpeed != 0) {
                 if (((VstM.Climber.climberState() == -1) != (climbSpeed < 0))
                         || ((VstM.Climber.climberState() == 1) != climbSpeed > 0)) {
                     climbSpeed *= VstM.Climber.climberState();
                 }
             }
             climber.runLadder(climbSpeed);
         }
         RobotDebugger.push(climber);
         RobotDebugger.push(this);
     }
 // Make this return true when this Command no longer needs to run execute()
 
     protected boolean isFinished() {
         return false;
     }
 
     // Called once after isFinished returns true
     protected void end() {
         climber.stop();
         RobotDebugger.push(climber);
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
         this.end();
         isEnabled = false;
         RobotDebugger.push(this);
     }
 
     public DebugInfoGroup getStatus() {
         return new DebugInfoGroup(new InfoState("ClimberEnabled", isEnabled() ? "Enabled" : "Disabled"));
     }
     private boolean isEnabled;
 
     private void checkEnabled() {
         isEnabled = Boolean.TRUE == isClimbing.getSelected();
     }
 
     private boolean isEnabled() {
         return isEnabled;
     }
 }
