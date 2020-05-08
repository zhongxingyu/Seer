 package org.team751;
 
 import edu.wpi.first.wpilibj.CANJaguar;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class Shooter_A_EncoderTest extends IterativeRobot {
 
 	private CANJaguar firstJaguar;
 
 	private CANJaguar secondJaguar;
 
 	private Encoder firstEncoder = new Encoder(
 			DigitalChannels.SHOOTER_FIRST_ENCODER_A,
 											   DigitalChannels.SHOOTER_FIRST_ENCODER_B);
 
 	private Encoder secondEncoder = new Encoder(
 			DigitalChannels.SHOOTER_SECOND_ENCODER_A,
 												DigitalChannels.SHOOTER_SECOND_ENCODER_B);
 
 	private Joystick stick1 = new Joystick(1);
 
 	private Joystick stick2 = new Joystick(2);
 
 	/**
 	 * This function is run when the robot is first started up and should be
 	 * used for any initialization code.
 	 */
 	public void robotInit() {
 		try {
 			firstJaguar = new CANJaguar(CANJaguarIDs.SHOOTER_FIRST);
 			secondJaguar = new CANJaguar(CANJaguarIDs.SHOOTER_SECOND);
 		} catch (CANTimeoutException ex) {
 			ex.printStackTrace();
 		}
 
 		firstEncoder.start();
 		secondEncoder.start();
 	}
 
 	public void disabledPeriodic() {
 		doDebug();
 	}
 
 	/**
 	 * This function is called periodically during autonomous
 	 */
 	public void autonomousPeriodic() {
 	}
 
 	/**
 	 * This function is called periodically during operator control
 	 */
 	public void teleopPeriodic() {
 		double power1 = stick1.getY();
 		double power2 = stick2.getY();
 
 		//Limit to >= 0
 		if (power1 < 0) {
 			power1 = 0;
 		}
 		if (power2 < 0) {
 			power2 = 0;
 		}
 
 		SmartDashboard.putNumber("Power 1", power1);
 		SmartDashboard.putNumber("Power 2", power2);
 
 		try {
 			firstJaguar.setX(power1);
 			secondJaguar.setX(power2);
 		} catch (CANTimeoutException ex) {
 			ex.printStackTrace();
 		}
		
		doDebug();
 	}
 
 	/**
 	 * This function is called periodically during test mode
 	 */
 	public void testPeriodic() {
 	}
 
 	private void doDebug() {
 
 		int firstCount = firstEncoder.get();
 		int secondCount = secondEncoder.get();
 
 		double firstRate = firstEncoder.getRate();
 		double secondRate = secondEncoder.getRate();
 
 		SmartDashboard.putNumber("First count", firstCount);
 		SmartDashboard.putNumber("Second count", secondCount);
 		SmartDashboard.putNumber("First rate, counts/sec", firstRate);
 		SmartDashboard.putNumber("Second rate, counts/sec", secondRate);
 
 		System.out.println(
 				"First: count " + firstCount + " rate " + firstRate + " Second: count " + secondCount + " rate " + secondRate);
 	}
 }
