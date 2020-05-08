 package org.team751;
 
 
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.command.CommandGroup;
 import edu.wpi.first.wpilibj.command.Scheduler;
 import org.team751.commands.Autonomous;
 import org.team751.commands.CommandBase;
 import org.team751.sensors.SerialUltrasonic;
 import org.team751.util.Utilities;
 import org.team751.util.logging.LogLevel;
 import org.team751.util.logging.Logger;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class Robot2012 extends IterativeRobot {
 
     CommandGroup autonomous = new Autonomous();
 
 	SerialUltrasonic ultrasonic;
 
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
 
         // Initialize all subsystems
         CommandBase.init();
         Utilities.printStartupMessage();
 
     }
 
	/*
	 * Important note to everyone:
	 * disabledContinuous(), autonomousContinuous(), and telopContinuous()
	 * are called as quickly as possible. If no waiting happens when one
	 * of these is called, it will result in 100% CPU usage on the cRIO.
	 * This is not helpful. The default superclass implementations of
	 * these functions involve pauses to keep CPU usage down.
	 *
	 * The autonomousContinuous function that was here didn't wait, so
	 * it caused 100% CPU usage when the robot was disabled. That was
	 * observed in an actual match.
	 *
	 * So: Don't use the continous methods unless you know what you're doing.
	 * Use the periodic methods.
	 */
 
     public void disabledInit() {
         Logger.getInstance().log("Robot disabled.", LogLevel.kStatus);
     }
     public void disabledPeriodic() {
 		log();
 
     }
 
     public void autonomousInit() {
         Logger.getInstance().log("Autonomous mode starting.", LogLevel.kStatus);
 		//Only run if driver station input 1 is high. Change it back when the autonomous works well.
         if(DriverStation.getInstance().getDigitalIn(1)){
 			autonomous.start();
 		}
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
         Scheduler.getInstance().run();
     }
 
     public void teleopInit() {
         Logger.getInstance().log("Teleoperated mode starting.", LogLevel.kStatus);
         Utilities.printAllianceStatusMessage();
         autonomous.cancel();//Ensure that autonomous does not continue
     }
 
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {
         Scheduler.getInstance().run();
 		log();
     }
 
 
 	private void log(){
 		Logger.getInstance().log("Distance: " + CommandBase.shooterWheels.getUltrasonicDistance() + " Angle: "+CommandBase.shooterTread.getTargetAngle() + " Power: "+CommandBase.shooterWheels.getOpenLoopSpeed(), LogLevel.kDebug);
 		//Logger.getInstance().log("Actual distance: "+CommandBase.shooterWheels.getUltrasonicDistance()+" Actual angle: "+CommandBase.shooterTread.getActualVoltage()/*+" Actual rate: "+CommandBase.shooterWheels.getActualRate()+" Top limit switch "+CommandBase.bridgePush.topLimitSwitchPressed()+" bottom limit switch "+CommandBase.bridgePush.bottomLimitSwitchPressed()*/, LogLevel.kDebug);
 		//Logger.getInstance().log("Instantaneous: " + CommandBase.shooterWheels.getInstantaneousSpeed() + " Rate: "+CommandBase.shooterWheels.getActualRate() + " Target: "+CommandBase.shooterWheels.getRate(), LogLevel.kDebug);
 	}
 }
