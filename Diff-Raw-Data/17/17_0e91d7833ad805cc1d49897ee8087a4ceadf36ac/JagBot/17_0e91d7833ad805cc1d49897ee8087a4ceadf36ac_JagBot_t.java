 package org.usfirst.frc348;
 
 import edu.wpi.first.wpilibj.AnalogModule;
 import edu.wpi.first.wpilibj.Gyro;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.CANJaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.Dashboard;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.AnalogChannel;
 
 public class JagBot extends IterativeRobot {
     private String SIG = "[JagBot] ";
     boolean DEBUG = true; int debugCounter = 0;
     protected BreakoutBox breakout;
     protected Joystick leftJoy, rightJoy;
     protected DriveTrain dt;
     protected Arm arm;
     protected Gyro gyro;
     
    public JagBot() throws CANTimeoutException {
 	System.out.println(SIG+"Creating JagBot");
 	breakout = new BreakoutBox();
 	leftJoy = new Joystick(1);
 	rightJoy = new Joystick(2);
 	
 	dt = new DriveTrain(4, 3);
 	arm = new Arm(2, 1, 1);
 	gyro = new Gyro(2);
     }
     
     public void autonomousPeriodic() {
 	
     }
     
     public void teleopPeriodic() {
 	// Joystick values are backwards
 	double left = -leftJoy.getY();
 	double right = -rightJoy.getY();
 	try {
 	    if (leftJoy.getTrigger() || rightJoy.getTrigger()) {
 		dt.stop();
 		dt.turn180(gyro);
 	    } else {
 		dt.running = false;
 		dt.drive(left, right, gyro);
 	    }
 	} catch (CANTimeoutException e1) { e1.printStackTrace(); }
     }
     
     public void debug() {
 	debugCounter += 1;
 	if (DEBUG && debugCounter%10 == 0) {
 	    try {
 		// System.out.println("Speed: "+dt.getSpeed()+" Gyro: "+gyro.getAngle()+" Turn Rate: "+dt.turnSpeed);
 		// System.out.println("Left Currents: "+dt.leftJag.getOutputCurrent()+
 		// 				"--- Right Current: "+dt.rightJag.getOutputCurrent()+"/");
 		System.out.println("Left Encoder: "+dt.leftJag.getPosition()+"/"+dt.leftJag.getSpeed()+
 				   " --- Right Encoder: "+dt.rightJag.getPosition()+"/"+dt.rightJag.getSpeed());
 	    } catch (Exception e) {
 		e.printStackTrace();
 	    }			
 	}
     }
 }
