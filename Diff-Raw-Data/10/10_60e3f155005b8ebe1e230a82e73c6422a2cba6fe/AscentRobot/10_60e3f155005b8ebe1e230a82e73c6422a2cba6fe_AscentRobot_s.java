 package org.usfirst.frc1923;
 
 import org.usfirst.frc1923.event.DriveGearDownEvent;
 import org.usfirst.frc1923.event.DriveGearUpEvent;
 import org.usfirst.frc1923.event.RingLightActivateEvent;
 import org.usfirst.frc1923.event.RingLightDeactivateEvent;
 import org.usfirst.frc1923.event.ShooterActuatorEvent;
 import org.usfirst.frc1923.event.ShooterGearDownEvent;
 import org.usfirst.frc1923.event.ShooterGearUpEvent;
 import org.usfirst.frc1923.event.ShooterStartEvent;
 import org.usfirst.frc1923.event.ShooterStopEvent;
 import org.usfirst.frc1923.utils.DefaultConfiguration;
 import org.usfirst.frc1923.utils.XboxController;
 
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Relay;
 
 /**
  * The core <code>IterativeRobot</code> for the Ultimate Ascent robot.
  * 
  * @author Aaron Weiss
  * @version 2.1
  * @since 2/9/13
  */
 public class AscentRobot extends IterativeRobot {
 	private boolean[] justPressed = new boolean[14];
 	private boolean[] triggers = new boolean[2];
 
 	/**
 	 * Initializes the robot.
 	 */
 	public void robotInit() {
 		// Components.networkTable.putBoolean("~S A V E~", true);
 	}
 
 	/**
 	 * Initializes the robot for teleop.
 	 */
 	public void teleopInit() {
 		Components.driveGearbox.setGear(0);
 		Components.shooterGearbox.setGear(0);
 	}
 
 	/**
 	 * Provides tele-operated functionality.
 	 */
 	public void teleopPeriodic() {
 		{ // Driving Scope
 			if (Components.preferences.getBoolean("experimental_drive", DefaultConfiguration.EXPERIMENTAL_DRIVE)) {
 				double forwardMagnitude = -Components.leftDriveStick.getCoalescedY();
 				double curvature = Components.rightDriveStick.getCoalescedX();
 				double attachment = Components.rightDriveStick.getCoalescedZ();
 				if (Math.abs(attachment) > 0.1 && Components.preferences.getBoolean("experimental_drive_attachment", DefaultConfiguration.EXPERIMENTAL_DRIVE_ATTACHMENT)) {
 					Components.driveSystem.drive(-attachment, attachment, false);
				} else if (curvature < 0.1) { // Turning left
 					Components.driveSystem.drive(-(forwardMagnitude + curvature), -forwardMagnitude, false);
				} else if (curvature > 0.1) { // Turning right
 					Components.driveSystem.drive(-forwardMagnitude, -(forwardMagnitude - curvature), false);
 				} else { // Going straight
 					Components.driveSystem.drive(forwardMagnitude, forwardMagnitude, true);
 				}
 			} else {
 				Components.driveSystem.drive(Components.leftDriveStick.getCoalescedY(), Components.rightDriveStick.getCoalescedY());
 			}
 
 			// Left Stick Trigger -- Driving gear down
 			if (Components.leftDriveStick.getTrigger() && !this.triggers[0]) {
 				Components.eventBus.push(new DriveGearDownEvent());
 				this.triggers[0] = true;
 			} else if (!Components.leftDriveStick.getTrigger()) {
 				this.triggers[0] = false;
 			}
 
 			// Right Stick Trigger -- Driving gear up
 			if (Components.rightDriveStick.getTrigger() && !this.triggers[1]) {
 				Components.eventBus.push(new DriveGearUpEvent());
 				this.triggers[1] = true;
 			} else if (!Components.rightDriveStick.getTrigger()) {
 				this.triggers[1] = false;
 			}
 		} // End Driving Scope
 
 		{ // Shooter Scope
 			XboxController xbc = Components.operatorController;
 
 			// Right Bumper (RB) -- Gear up
 			if (xbc.getButton(XboxController.Button.RB) && !justPressed[XboxController.Button.RB.value]) {
 				Components.eventBus.push(new ShooterGearUpEvent());
 				justPressed[XboxController.Button.RB.value] = true;
 			} else if (!xbc.getButton(XboxController.Button.RB)) {
 				justPressed[XboxController.Button.RB.value] = false;
 			}
 
 			// Left Bumper (LB) -- Gear down
 			if (xbc.getButton(XboxController.Button.LB) && !justPressed[XboxController.Button.LB.value]) {
 				Components.eventBus.push(new ShooterGearDownEvent());
 				justPressed[XboxController.Button.LB.value] = true;
 			} else if (!xbc.getButton(XboxController.Button.LB)) {
 				justPressed[XboxController.Button.LB.value] = false;
 			}
 
 			// A Button -- Fire shooter (1 disque)
 			if (xbc.getButton(XboxController.Button.A) && !justPressed[XboxController.Button.A.value]) {
 				Components.eventBus.push(new ShooterActuatorEvent());
 				justPressed[XboxController.Button.A.value] = true;
 			} else if (!xbc.getButton(XboxController.Button.A)) {
 				justPressed[XboxController.Button.A.value] = false;
 			}
 
 			// B Button -- Fire shooter (2 disques)
 			if (xbc.getButton(XboxController.Button.B) && !justPressed[XboxController.Button.B.value]) {
 				Components.eventBus.push(new ShooterActuatorEvent(2));
 				justPressed[XboxController.Button.B.value] = true;
 			} else if (!xbc.getButton(XboxController.Button.B)) {
 				justPressed[XboxController.Button.B.value] = false;
 			}
 
 			// Y Button -- Fire shooter (3 disques)
 			if (xbc.getButton(XboxController.Button.Y) && !justPressed[XboxController.Button.Y.value]) {
 				Components.eventBus.push(new ShooterActuatorEvent(3));
 				justPressed[XboxController.Button.Y.value] = true;
 			} else if (!xbc.getButton(XboxController.Button.Y)) {
 				justPressed[XboxController.Button.Y.value] = false;
 			}
 
 			// X Button -- Fire shooter (4 disques)
 			if (xbc.getButton(XboxController.Button.X) && !justPressed[XboxController.Button.X.value]) {
 				Components.eventBus.push(new ShooterActuatorEvent(4));
 				justPressed[XboxController.Button.X.value] = true;
 			} else if (!xbc.getButton(XboxController.Button.X)) {
 				justPressed[XboxController.Button.X.value] = false;
 			}
 
 			// Right Thumb Click -- Ring Light
 			if (xbc.getButton(XboxController.Button.RightClick) && !justPressed[XboxController.Button.RightClick.value]) {
 				Components.eventBus.push(new RingLightActivateEvent());
 				justPressed[XboxController.Button.RightClick.value] = true;
 			} else if (!xbc.getButton(XboxController.Button.RightClick)) {
 				Components.eventBus.push(new RingLightDeactivateEvent());
 				justPressed[XboxController.Button.RightClick.value] = false;
 			}
 
 			// Shooter state control options
 			if (!Components.preferences.getBoolean("shooter_always_on", DefaultConfiguration.SHOOTER_ALWAYS_ON)) {
 				// Back -- Stop shooter
 				if (xbc.getButton(XboxController.Button.Back) && !justPressed[XboxController.Button.Back.value]) {
 					Components.eventBus.push(new ShooterStopEvent());
 					justPressed[XboxController.Button.Back.value] = true;
 				} else if (!xbc.getButton(XboxController.Button.Back)) {
 					justPressed[XboxController.Button.Back.value] = false;
 				}
 
 				// Start -- Start shooter
 				if (xbc.getButton(XboxController.Button.Start) && !justPressed[XboxController.Button.Start.value]) {
 					Components.eventBus.push(new ShooterStartEvent());
 					justPressed[XboxController.Button.Start.value] = true;
 				} else if (!xbc.getButton(XboxController.Button.Start)) {
 					justPressed[XboxController.Button.Start.value] = false;
 				}
 			} else {
 				Components.shooterSystem.set(1);
 			}
 		} // End Shooter Scope
 
 		{ // Shooter Angle Scope
 			Components.shooterAngleSystem.set(Components.operatorController.getAxis(1, 2));
 		} // End Shooter Angle Scope
 
 		{ // Event Bus Scope
 			Components.eventBus.next();
 		} // End Event Bus Scope
 
 		{ // Compressor Support Scope
 			if (Components.preferences.getBoolean("compressor_on", DefaultConfiguration.COMPRESSOR_ON)) {
 				if (!Components.compressorSafety.get()) {
 					Components.compressor.set(Relay.Value.kOn);
 				} else {
 					Components.compressor.set(Relay.Value.kOff);
 				}
 			}
 		} // End Compressor Support Scope
 	}
 }
