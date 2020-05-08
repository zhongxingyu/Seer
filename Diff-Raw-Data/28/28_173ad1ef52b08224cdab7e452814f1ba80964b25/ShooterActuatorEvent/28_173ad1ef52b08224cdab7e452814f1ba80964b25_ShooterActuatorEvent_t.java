 package org.usfirst.frc1923.event;
 
 import org.usfirst.frc1923.Components;
 import org.usfirst.frc1923.utils.DefaultConfiguration;
 
 import edu.wpi.first.wpilibj.Timer;
 
 /**
  * An event that actuates the shooter pneumatically.
  * 
  * @author Aaron Weiss
  * @version 1.0
  * @since 2/9/13
  */
 public class ShooterActuatorEvent extends Event {
 	private final int disques;
 
 	/**
 	 * Creates the event.
 	 */
 	public ShooterActuatorEvent() {
 		this(1);
 	}
 
 	/**
 	 * Creates the event.
 	 * @param disques
 	 * 				the number of disques to fire.
 	 */
 	public ShooterActuatorEvent(int disques) {
 		super(true);
 		this.disques = disques;
 	}
 
 	/**
 	 * Performs the event's contents.
 	 */
 	protected void event() {
 		for (int i = 0; i < disques; i++) {
 			Components.pneumaticActuatorTwo.set(false);
 			Components.pneumaticActuatorOne.set(true);
 			Timer.delay(Components.preferences.getDouble("shooter_pneumatic_time", DefaultConfiguration.SHOOTER_PNEUMATIC_TIME));
			if (i == 3) {
				Components.shooterGearbox.gearUp();
			}
 			Components.shooterGearbox.gearUp();
 			Components.pneumaticActuatorOne.set(false);
 			Components.pneumaticActuatorTwo.set(true);
 			Timer.delay(Components.preferences.getDouble("shooter_pneumatic_time", DefaultConfiguration.SHOOTER_PNEUMATIC_TIME));
 		}
 		for (int i = 0; i < disques; i++) {
			if (i == 3)
				Components.shooterGearbox.gearDown();
 			Components.shooterGearbox.gearDown();
 		}
 	}
 }
