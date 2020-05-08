 package org.usfirst.frc1923.components;
 
 import org.usfirst.frc1923.Component;
 
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.SpeedController;
 
 /**
  * A basic component for managing drive commands.
  * @author Aayush Sharma, Nabeel Rangwala, Pavan Hegde, Aaron Weiss
  * @version 1.3
  * @since 1/12/13
  */
 public class DriveComponent implements Component {
 	private int currentState = Component.ComponentState.COMPONENT_STOPPED;
 	private RobotDrive robotDrive;
 
 	/**
 	 * Creates a <code>DriveComponent</code> using the desired <code>SpeedControllers</code>. 
 	 * @param leftController the controller for the left side of the drive train
 	 * @param rightController the controller for the right side of the drive train
 	 */
 	public DriveComponent(SpeedController leftController, SpeedController rightController) {
 		robotDrive = new RobotDrive(leftController, rightController);
 	}
 	
 	/**
 	 * Drives the robot in one direction with the specified curvature.
 	 * @param outputMagnitude the magnitude by which to move forward
 	 * @param curve the desired curvature for the movement
 	 */
 	public void drive(double outputMagnitude, double curve) {
 		robotDrive.drive(outputMagnitude, curve);
 	}
 
 	/**
 	 * Drives the robot using respective powers for each side.
 	 * @param leftMagnitude the magnitude for the left side
 	 * @param rightMagnitude the magnitude for the right side
 	 */
 	public void tankDrive(double leftMagnitude, double rightMagnitude) {
 		robotDrive.tankDrive(leftMagnitude, rightMagnitude);
		System.out.println("left: " + leftMagnitude + " right:" + rightMagnitude);
 	}
 
 	/**
 	 * Limits the maximum output of the drive train.
 	 * @param maxOutput the desired maximum speed
 	 */
 	public void setMaxOutput(double maxOutput) {
 		robotDrive.setMaxOutput(maxOutput);
 	}
 
 	/**
 	 * Sets whether or not the drive train requires a <code>Watchdog</code>.
 	 * @param enabled whether or not to use the <code>Watchdog</code>
 	 */
 	public void setSafety(boolean enabled) {
 		robotDrive.setSafetyEnabled(enabled);
 	}
 
 	/**
 	 * Stops the drive train.
 	 */
 	public void stop() {
 		robotDrive.stopMotor();
 	}
 
 	public int getState() {
 		return this.currentState;
 	}
 
 	public void destroy() {
 		this.stop();
 		robotDrive.free();
 	}
 }
