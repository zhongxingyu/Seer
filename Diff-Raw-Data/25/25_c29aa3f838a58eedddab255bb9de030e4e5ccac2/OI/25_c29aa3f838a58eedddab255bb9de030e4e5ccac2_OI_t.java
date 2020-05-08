 package org.team751;
 
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 import org.team751.commands.bridgepush.BridgePushMomentaryExtend;
 import org.team751.commands.bridgepush.BridgePushMomentaryRetract;
 import org.team751.commands.drivetrain.DisableClosedLoopDrive;
 import org.team751.commands.drivetrain.EnableClosedLoopDrive;
 import org.team751.commands.nommer2.Feed;
 import org.team751.commands.nommer2.NommerEject;
 import org.team751.commands.nommer2.NommerIntake;
 import org.team751.commands.shooter.*;
 
 /**
  * Contains structure and mappings for the operator interface.<br />
  *
  * Shooter theory of operation:
  * <ol>
  * <li>Drive to the shooting position</li>
  * <li>Press the forward button to automatically set target speed &amp; angle</li>
  * <li>Start holding down the back button to turn the shooter on</li>
  * <li>Pull the trigger to shoot</li>
  * <li>Release the back button to turn the shooter off</li>
  * </ol>
  */
 public class OI {
     // Process operator interface input here.
 
 
     /** The joystick used for driving */
     private Joystick driveJoystick = new Joystick (1);
 	/** The joystick used for system operation */
     private Joystick operatorJoystick = new Joystick(2);//Changed to 2 from 3
 
 
     /**
      * Get a reference to the joystick used for driving.
      * @return The joystick
      */
     public Joystick getDriveJoystick() {
         return driveJoystick;
     }
 
     /**
      * Get a reference to the joystick used for system operation.
      * @return The joystick
      */
     public Joystick getOperatorJoystick() {
         return operatorJoystick;
     }
 
 
 	//Shoot: Trigger
     private JoystickButton shoot = new JoystickButton (driveJoystick, 1);
 	//Power up: right side button on top
     private JoystickButton powerUp = new JoystickButton (operatorJoystick, 5);
 	//Power down: left side button on top
     private JoystickButton powerDown = new JoystickButton (operatorJoystick, 4);
 	//Set auto power: forward button on top
     private JoystickButton autoPower = new JoystickButton (operatorJoystick, 3);
 
 	//Closed-loop drive enable: forward button on top
     private JoystickButton closedLoopEnable = new JoystickButton(driveJoystick, 3);
 	//Closed-loop drive diable: back button on top
     private JoystickButton closedLoopDisable = new JoystickButton(driveJoystick,2);
 
 	//Nommer on/intake: base left side forward button
     private JoystickButton nommerOn = new JoystickButton(operatorJoystick, 6);
 	//Nommer off/reject: base left side back button
     private JoystickButton nommerOff = new JoystickButton(operatorJoystick, 7);
 	//Shooter button: back button on top (Hold down to turn shooter on, release to turn it off)
 	private JoystickButton shooterOn = new JoystickButton(operatorJoystick, 2);
        
        private JoystickButton leftTrigger = new JoystickButton(operatorJoystick, 1);
 
 	//Shooter angle controls
 	//Shooter angle up: Base right side back button
 	private JoystickButton shooterAngleUp = new JoystickButton(operatorJoystick, 11);
 	//Shooter angle down: Base right side forward button
 	private JoystickButton shooterAngleDown = new JoystickButton(operatorJoystick, 10);
 
 	private JoystickButton bridgeDown = new JoystickButton(driveJoystick, 6);
 	private JoystickButton bridgeUp = new JoystickButton(driveJoystick, 7);
 
     /**
      * Construct the operator interface and link buttons to commands
      */
     public OI (){
         powerUp.whenPressed(new ManualShooterPowerUp());
         powerDown.whenPressed(new ManualShooterPowerDown());
         autoPower.whenPressed(new AutomaticShooterSet());
        shoot.whileHeld(new Feed());//Right stick trigger
 
         closedLoopEnable.whenPressed(new EnableClosedLoopDrive());
         closedLoopDisable.whenPressed(new DisableClosedLoopDrive());
 
        nommerOn.whileHeld(new NommerIntake());//Left stick button 6
        nommerOff.whileHeld(new NommerEject());//Left stick button 7
        leftTrigger.whileHeld(new Feed());
 
 		shooterOn.whenPressed(new ShooterOn());
 		shooterOn.whenReleased(new ShooterOff());
 
 		shooterAngleUp.whileHeld(new ManualShooterAngleUp());
 		shooterAngleDown.whileHeld(new ManualShooterAngleDown());
 
 		bridgeDown.whileHeld(new BridgePushMomentaryExtend());
 		bridgeUp.whileHeld(new BridgePushMomentaryRetract());
 
     }
 
 }
 
