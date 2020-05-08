 package com.shsrobotics.library;
 
 
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.DriverStation.Alliance;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.livewindow.LiveWindow;
 
 /**
  * Improved IterativeRobot.
  * @author Team 2412 <first.robototes.com, github.com/robototes>
  */
 public class FRCRobot extends IterativeRobot {
 	private boolean firstRun;
 	private boolean startup;
 	
	@Override
 	public void robotInit() {
 		DriverStation ds = DriverStation.getInstance();
 		Alliance alliance = ds.getAlliance();
 		int location = ds.getLocation();
 		double voltage = ds.getBatteryVoltage();
 		
 		System.out.println("	==== STARTING INITIALIZATION PROCEDURES ====");
 			Timer.delay(0.15);
 		System.out.println("		" + alliance.name.toUpperCase() + " ALLIANCE, POSITION " + location);
 			Timer.delay(0.2);
 		System.out.println("		RUNNING SYSTEM CHECKS");
 			Timer.delay(0.1);
 		System.out.println("			VOLTAGE: " + voltage);
 			Timer.delay(0.05);
 		if (ds.isFMSAttached()) {
 			System.out.println("			FMS ATTAHCED");
 			Timer.delay(0.2);
 		}
 		
 		if (voltage < 12.0) {
 			System.out.println();
 			System.out.println("!!-- WARNING: LOW VOLTAGE --!!");
 			System.out.println();
 		}
 		
 		System.out.println();
 		System.out.println("	=======================");
 		System.out.println("	==== ROBOT IS READY ===");
 		System.out.println("	=======================");
 			
 		startup = true;
 	}
 	
	@Override
 	public void autonomousInit() {
 		System.out.println("	==== STARTING AUTONOMOUS MODE ====");
 		firstRun = true;
 	}
 	
	@Override
 	public void autonomousPeriodic() {
 		if (firstRun) {
 			System.out.println("		No autonomous code.");
 			firstRun = false;
 		}
 		
 		Timer.delay(0.05);
 	}
 	
	@Override
 	public void teleopInit() {
 		System.out.println("	==== STARTING TELE-OPERATED MODE ====");
 		firstRun = true;
 	}
 	
	@Override
 	public void teleopPeriodic() {
 		if (firstRun) {
 			System.out.println("		No tele-operated code.");
 			firstRun = false;
 		}
 		Timer.delay(0.05);
 	}
 	
	@Override
 	public void testInit() {
 		System.out.println("	==== TEST MODE STARTING ====");
 	}
 	
	@Override
 	public void testPeriodic() {
 		LiveWindow.run();
 		Timer.delay(0.005);
 	}
 	
	@Override
 	public void disabledInit() {
 		if (!startup) {
 			System.out.println("	==== ROBOT DISABLED ====");
 		} else {
 			startup = false;
 		}
 	}
 	
 	public void disabledPeriodic() {
 		Timer.delay(0.1);
 	}
 }
