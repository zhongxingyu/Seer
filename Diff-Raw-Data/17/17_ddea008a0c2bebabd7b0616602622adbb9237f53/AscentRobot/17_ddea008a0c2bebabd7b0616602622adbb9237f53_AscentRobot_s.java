 
 package org.usfirst.frc1923;
 
 import org.usfirst.frc1923.events.EventBus;
import org.usfirst.frc1923.events.ShooterStartEvent;
import org.usfirst.frc1923.events.ShooterStopEvent;
 import org.usfirst.frc1923.utils.Configuration;
 import org.usfirst.frc1923.utils.XboxController;
 
 import edu.wpi.first.wpilibj.IterativeRobot;
 
 /**
  * The core <code>IterativeRobot</code> for the Ultimate Ascent robot.
  * @author Aaron Weiss, Bhavish Yalamanchi, Pavan Hegde
  * @version 1.0.1
  * @since 1/8/13
  */
 public class AscentRobot extends IterativeRobot {
 	private final EventBus eventBus = new EventBus();
 	private boolean[] justPressed = new boolean[13];
 	
 	public void robotInit() {
 		Components.driveGears.setGear(0);
 	}
 
 	public void disabledInit() {
 		// TODO: this
 	}
 
 	public void autonomousInit() {
 		// TODO: this
 	}
 
 	public void teleopInit() {
 		// TODO: this
 	}
 
 	public void disabledPeriodic() {
 		// TODO: this
 	}
 
 	public void autonomousPeriodic() {
 		// TODO: this
 	}
 
 	public void teleopPeriodic() {	
 		if (Components.controller.getButton(XboxController.Button.A)) {
 			
 			this.justPressed[XboxController.Button.A.value] = true;
 		} else {
 			this.justPressed[XboxController.Button.A.value] = false;
 		}
 		if (Components.controller.getButton(XboxController.Button.B)) {
 			
 			this.justPressed[XboxController.Button.B.value] = true;
 		} else {
 			this.justPressed[XboxController.Button.B.value] = false;
 		}
 		if (Components.controller.getButton(XboxController.Button.X)) {
 			
 			this.justPressed[XboxController.Button.X.value] = true;
 		} else {
 			this.justPressed[XboxController.Button.X.value] = false;
 		}
 		if (Components.controller.getButton(XboxController.Button.Y)) {
 			
 			this.justPressed[XboxController.Button.Y.value] = true;
 		} else {
 			this.justPressed[XboxController.Button.Y.value] = false;
 		}
 		if (Components.controller.getButton(XboxController.Button.RB)) {
 			//this.eventBus.addEvent(new ShooterStartEvent(Components.shooter, Components.shooterGears));
 			Components.shooter.set(.50, .50);
 			this.justPressed[XboxController.Button.RB.value] = true;
 		} else {
 			this.justPressed[XboxController.Button.RB.value] = false;
 		}
 		if (Components.controller.getButton(XboxController.Button.LB)) {
 			//this.eventBus.addEvent(new ShooterStopEvent(Components.shooter));
 			Components.shooter.stop();
 			this.justPressed[XboxController.Button.LB.value] = true;
 		} else {
 			this.justPressed[XboxController.Button.LB.value] = false;
 		}
 		if (Components.controller.getButton(XboxController.Button.RightClick)) {
 			
 			this.justPressed[XboxController.Button.RightClick.value] = true;
 		} else {
 			this.justPressed[XboxController.Button.RightClick.value] = false;
 		}
 		
 		if (Components.leftDriveStick.getTrigger() && !this.justPressed[11]) {
 			Components.driveGears.gearDown();
 			this.justPressed[11] = true;
 		} else if (!Components.leftDriveStick.getTrigger()) {
 			this.justPressed[11] = false;
 		}
 		
 		if (Components.rightDriveStick.getTrigger() && !this.justPressed[12]) {
 			Components.driveGears.gearUp();
 			this.justPressed[12] = true;
 		} else if (!Components.rightDriveStick.getTrigger()) {
 			this.justPressed[12] = false;
 		}
 	}
 
 	public void disabledContinuous() {
 		// TODO: this
 	}
 
 	public void autonomousContinuous() {
 		// TODO: this
 	}
 
 	public void teleopContinuous() {
 		if (Configuration.EXPERIMENTAL_DRIVE) {
 			double forwardMagnitude = Components.leftDriveStick.getCoalescedY();
 			double curvature = Components.rightDriveStick.getCoalescedX();
			if ( curvature < 0.1) { //Turns left
 				Components.driveSystem.tankDrive(forwardMagnitude, forwardMagnitude - curvature);
			} else if (curvature > 0.1) { //Turns right
 				Components.driveSystem.tankDrive(forwardMagnitude + curvature, forwardMagnitude);
 			} else {
 				Components.driveSystem.tankDrive(forwardMagnitude, forwardMagnitude);
 			}
 		} else {
 			Components.driveSystem.tankDrive(Components.leftDriveStick.getCoalescedY(), Components.rightDriveStick.getCoalescedY());
 		}
 		//this.eventBus.next();
 	}
 }
