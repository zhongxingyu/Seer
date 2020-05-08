 package edu.wpi.first.wpilibj.templates.commands;
 
 import edu.wpi.first.wpilibj.templates.DisableNotifable;
 import edu.wpi.first.wpilibj.templates.RobotMain;
 import edu.wpi.first.wpilibj.templates.debugging.DebugInfo;
 import edu.wpi.first.wpilibj.templates.debugging.DebugInfoGroup;
 import edu.wpi.first.wpilibj.templates.debugging.DebugLevel;
 import edu.wpi.first.wpilibj.templates.debugging.DebugOutput;
 import edu.wpi.first.wpilibj.templates.debugging.DebugStatus;
 import edu.wpi.first.wpilibj.templates.debugging.Debuggable;
 import edu.wpi.first.wpilibj.templates.debugging.InfoState;
 import edu.wpi.first.wpilibj.templates.debugging.RobotDebugger;
 import edu.wpi.first.wpilibj.templates.variablestores.DashboardStore;
 import edu.wpi.first.wpilibj.templates.vstj.VstJ;
 
 /**
  * This Command runs the climber motors (The ones powering the "cart" that goes
  * up/down the ladder) according to a set speed, that is increased/decreased
  * with buttons.
  *
  * Same kind of drive as RunShooterMotors, but with different buttons.
  */
 public class RunClimber extends CommandBase implements Debuggable, DisableNotifable {
 
     private boolean isEnabled;
     private boolean limitSwitchEnabled = false;
     private double speed = 0;
     private boolean extendButtonLastPressed = false;
     private boolean retractButtonLastPressed = false;
     private boolean lowerPressed;
     private boolean upperPressed;
 
     public RunClimber() {
         requires(climber);
         requires(climberLimitSwitch);
         DashboardStore.initClimber();
         RobotMain.addDisableNotifable(this);
     }
 
     protected void initialize() {
         climber.stop();
 
     }
 
     protected void execute() {
         checkEnabled();
         checkLimitSwitch();
         if (isEnabled) {
             if (VstJ.getClimberExtendButtonValue() != extendButtonLastPressed) {
                 if (!extendButtonLastPressed) {
                    if (speed + 0.1 < 1) {
                        speed += 0.1;
                     } else {
                         speed = 1;
                     }
                    extendButtonLastPressed = !extendButtonLastPressed;
                 }
             }
             if (VstJ.getClimberRetractButtonValue() != retractButtonLastPressed) {
                 if (!retractButtonLastPressed) {
                    if (speed - 0.1 > -1) {
                        speed -= 0.1;
                     } else {
                         speed = -1;
                     }
                 }
                 retractButtonLastPressed = !retractButtonLastPressed;
             }
             if (upperPressed && speed > 0) {
                 speed = 0;
             }
             if (lowerPressed && speed < 0) {
                 speed = 0;
             }
         } else {
             speed = 0;
         }
        //climber.runLadder(speed);
         RobotDebugger.push(this);
         RobotDebugger.push(climber);
         RobotDebugger.push(climberLimitSwitch);
     }
 
     protected boolean isFinished() {
         return false;
     }
 
     protected void end() {
         climber.stop();
         speed = 0;
         isEnabled = false;
         RobotDebugger.push(this);
         RobotDebugger.push(climber);
         RobotDebugger.push(climberLimitSwitch);
 
     }
 
     protected void interrupted() {
         this.end();
     }
 
     private void checkEnabled() {
         isEnabled = DashboardStore.getClimberEnabled();
     }
 
     private void checkLimitSwitch() {
         if (limitSwitchEnabled) {
             upperPressed = climberLimitSwitch.readUpper();
             lowerPressed = climberLimitSwitch.readLower();
         } else {
             upperPressed = false;
             lowerPressed = false;
         }
     }
 
     public DebugOutput getStatus() {
         DebugInfo[] infoList = new DebugInfo[3];
         infoList[0] = new InfoState("Climber", isEnabled ? "Enabled" : "Disabled", DebugLevel.HIGHEST);
         infoList[1] = new DebugStatus("ClimberShouldBe", speed, DebugLevel.LOW);
         infoList[2] = new DebugStatus("ClimberLimitSwitchEnabled", limitSwitchEnabled, DebugLevel.LOW);
         return new DebugInfoGroup(infoList);
     }
 
     public void disable() {
         speed = 0;
         isEnabled = false;
         RobotDebugger.push(this);
         RobotDebugger.push(climber);
         RobotDebugger.push(climberLimitSwitch);
     }
 }
