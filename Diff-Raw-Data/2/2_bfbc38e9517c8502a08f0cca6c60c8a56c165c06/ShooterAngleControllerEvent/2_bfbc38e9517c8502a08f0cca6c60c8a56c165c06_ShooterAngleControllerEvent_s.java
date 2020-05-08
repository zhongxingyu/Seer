 package org.usfirst.frc1923.events;
 
 import org.usfirst.frc1923.components.ShooterAngleController;
 /**
  * An event to handle the Angle of the shooter
  * @author Pavan Hegde, Nabeel Rangwala
  * @version 1.0
  * @since 1/26/13
  */
 public class ShooterAngleControllerEvent implements Event {
 	private boolean canShoot;
 	private ShooterAngleController motor;
 	
 	/**
 	 * Constructor to create the event
 	 * @param motor the Shooter Angle Controller
 	 * @param canShoot boolean to define which direction to go
 	 */
 	public ShooterAngleControllerEvent(ShooterAngleController motor, boolean canShoot)  {
 		this.canShoot = canShoot;
 		this.motor = motor;
 	}
 	
 	/**
 	 * Moves up or down depending on canShoot
 	 */
 	public void run() {
 		if (canShoot){
 			motor.up(motor.getXboxLeftJoystick());
 		}
 		else {
			motor.down(motor.getXboxLeftJoystick());
 		}
 	}
 
 	/**
 	 * destroys motor
 	 */
 	public void reset() {
 		motor.destroy();
 	}
 }
