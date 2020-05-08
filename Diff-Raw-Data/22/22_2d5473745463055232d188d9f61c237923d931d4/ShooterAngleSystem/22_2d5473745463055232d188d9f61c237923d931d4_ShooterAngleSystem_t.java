 package org.usfirst.frc1923.systems;
 
 import edu.wpi.first.wpilibj.Solenoid;
 
 /**
  * A system for controlling the angle of the shooter.
  * 
  * @author Aaron Weiss
  * @version 1.3
  * @since 2/9/13
  */
 public class ShooterAngleSystem implements System {
 	private Solenoid angleControllerOne, angleControllerTwo;
 	private boolean state = false;
 
 	/**
 	 * Creates a <code>ShooterAngleSystem</code>.
 	 * 
 	 * @param angleController
 	 * 				the <code>Solenoid</code> to control the angle
 	 */
 	public ShooterAngleSystem(Solenoid angleControllerOne, Solenoid angleControllerTwo) {
 		this.angleControllerOne = angleControllerOne;
 		this.angleControllerTwo = angleControllerTwo;
 	}
 
 	/**
 	 * Sets the state of the angle controller.
 	 * @param on
 	 * 				the state of the angle controller
 	 */
 	public void set(boolean on) {
 		this.state = on;
 		if (on) {
 			this.angleControllerTwo.set(!on);
 			this.angleControllerOne.set(on);
 		} else {
			this.angleControllerOne.set(on);
			this.angleControllerTwo.set(!on);
 		}
 	}
 
 	/**
 	 * Toggles the state of the angle controller.
 	 */
 	public void toggle() {
 		if (this.state)
 			this.set(false);
 		else
 			this.set(true);
 	}
 
 	/**
 	 * Completely stops the robot system.
 	 */
 	public void stop() {
 	}
 }
