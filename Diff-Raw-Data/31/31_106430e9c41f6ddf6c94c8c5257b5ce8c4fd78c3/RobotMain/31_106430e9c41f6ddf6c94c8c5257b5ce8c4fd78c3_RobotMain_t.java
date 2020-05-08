 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.command.Scheduler;
 import edu.wpi.first.wpilibj.livewindow.LiveWindow;
 import edu.wpi.first.wpilibj.templates.commands.*;
 import edu.wpi.first.wpilibj.templates.debugging.DebugInfo;
 import edu.wpi.first.wpilibj.templates.debugging.DebugInfoGroup;
 import edu.wpi.first.wpilibj.templates.debugging.DebugPortInfo;
 import edu.wpi.first.wpilibj.templates.debugging.RobotDebugger;
 import edu.wpi.first.wpilibj.templates.variablestores.VstM;
 
 /**
  * Main Robot Class.
  */
 public class RobotMain extends IterativeRobot {
 
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
         // Initialize all subsystems
         CommandBase.init();
         pushPorts();
     }
 
     private void pushPorts() {
         //Tell the person on the Console/SmartDashboard what ports things should be.
         DebugInfo[] infoList = new DebugInfo[9];
         infoList[0] = new DebugPortInfo("LeftMotor", VstM.PWM.LEFT_MOTOR_PORT);
         infoList[1] = new DebugPortInfo("RightMotor", VstM.PWM.RIGHT_MOTOR_PORT);
         infoList[2] = new DebugPortInfo("ClimberMotor", VstM.PWM.CLIMBER_MOTOR_PORT);
         infoList[3] = new DebugPortInfo("FirstShooterMotor", VstM.PWM.FIRST_SHOOTER_MOTOR_PORT);
         infoList[4] = new DebugPortInfo("SecondShooterMotor", VstM.PWM.SECOND_SHOOTER_MOTOR_PORT);
         infoList[5] = new DebugPortInfo("ClimberBottomSwitch", VstM.Digital.CLIMBER_BOTTOM);
         infoList[6] = new DebugPortInfo("ClimberTopSwitch", VstM.Digital.CLIMBER_TOP);
         infoList[7] = new DebugPortInfo("ShooterSoenoid1", VstM.Solenoids.SHOOTER_SOLENOID_SIDE_1);
         infoList[8] = new DebugPortInfo("ShooterSoenoid2", VstM.Solenoids.SHOOTER_SOLENOID_SIDE_2);
         DebugInfoGroup debugInfoGroup = new DebugInfoGroup(infoList);
         RobotDebugger.push(debugInfoGroup);
     }
 
     public void autonomousInit() {
         ImageProcess ip = new ImageProcess();
         ip.start();
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
         Scheduler.getInstance().run();
     }
 
     public void teleopInit() {
     }
 
     /**
      * This function is called periodically during operator control.
      */
     public void teleopPeriodic() {
         Scheduler.getInstance().run();
     }
 
     /**
      * This function is called periodically during test mode.
      */
     public void testPeriodic() {
         LiveWindow.run();
     }
 
     public void disabledPeriodic() {
     }
 
     public void disabledInit() {
        RobotDebugger.clearMap();
     }
 }
