 package org.usfirst.frc348;
 
 import org.usfirst.frc348.auton.Autonomous;
 
 import edu.wpi.first.wpilibj.DriverStationEnhancedIO.EnhancedIOException;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.SmartDashboard;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 
 public class JagBot extends IterativeRobot {
     private String SIG = "[JagBot] ";
     boolean DEBUG = true; int debugCounter = 0;
     public BreakoutBox breakout;
     public Joystick leftJoy, rightJoy;
     public DriveTrain dt;
     public Arm arm;
     protected Deployment deployment;
     public Autonomous auton;
     public AxisCamera ac;
     
     public JagBot() throws CANTimeoutException {
 	System.out.println(SIG+"Creating JagBot");
 	DriverStationLCD.getInstance().println(DriverStationLCD.Line.kMain6, 1, 
 		"Not Ready --- Initializing...");
 	DriverStationLCD.getInstance().updateLCD();
 	
 	breakout = new BreakoutBox();
 	leftJoy = new Joystick(1);
 	rightJoy = new Joystick(2);
 	
 	dt = new DriveTrain(3, 4);
 	arm = new Arm(2, 1, 1, 2);
 	deployment = new Deployment(2, 0.4);
 	auton = new Autonomous(this, 4, 3);
 
 	ac = AxisCamera.getInstance();
 	updateDashboard();
     }
             
     public void autonomousInit() {
     	auton.restart();
     }
     
     public void autonomousPeriodic() {
     	auton.periodic();
 	arm.periodic();
 	updateDashboard();
     }
 
     public long startTime;
     public void teleopInit() {
 	startTime = System.currentTimeMillis();
 	deployment.disable();
     }
     
     public void teleopPeriodic() {
     	try {
 	    breakout.update();
 	} catch (EnhancedIOException e2) { e2.printStackTrace(); }
     	
 	// Joystick values are backwards
 	double left = -leftJoy.getY();
 	double right = -rightJoy.getY();
 	try {
 	    dt.drive(left, right);
 	} catch (CANTimeoutException e1) { e1.printStackTrace(); }
 
 	if (breakout.closeGrabber || breakout.openGrabber) {
 	    arm.open();
 	} else {
 	    arm.close();
 	}
 	
 	try {
 	    if (!breakout.armControlMode) {
 		arm.setMagicMode();
 		if (breakout.openGrabber) {
 		    arm.placePiece();
 		} else {
 		    arm.moveToPosition(breakout.armPos);
 		}
 	    } else {
 		arm.setManualMode();
 		arm.manualMove(breakout.armPos);
 	    }
 	} catch (CANTimeoutException e) { e.printStackTrace(); }
 
 	if (breakout.releaseMinibot) {
 	    deployment.enable();
 	} else {
 	    deployment.disable();
 	}
 	deployment.periodic();
 
 	arm.periodic();
 	//	updateDashboard();
 //	 debug();
     }
     
     public void disabledPeriodic() {
     	updateDashboard();
     }
 
     public void updateDashboard() {
 	dt.updateDashboard();
 	arm.updateDashboard();
 
 	SmartDashboard.log((System.currentTimeMillis() - startTime)/1000, "Time");
 
 	DriverStationLCD.getInstance().println(DriverStationLCD.Line.kMain6, 1, 
 			"Ready                      ");
 	DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser3, 1, 
 			"Autonomous Stage: "+auton.current);
 	DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser2, 1, 
 			"Mode: "+auton.getAutonomousName());
 	DriverStationLCD.getInstance().updateLCD();
     }
     
     public void debug() {
 	debugCounter += 1;
 	if (DEBUG && debugCounter%10 == 0) {
 		
 	    try {
 		System.out.println("Left Encoder: "+dt.leftJag.getPosition()+"/"+dt.leftJag.getSpeed()+
 				   " --- Right Encoder: "+dt.rightJag.getPosition()+"/"+dt.rightJag.getSpeed());
 	    } catch (Exception e) {
 		e.printStackTrace();
 	    }			
 	}
     }
 }
