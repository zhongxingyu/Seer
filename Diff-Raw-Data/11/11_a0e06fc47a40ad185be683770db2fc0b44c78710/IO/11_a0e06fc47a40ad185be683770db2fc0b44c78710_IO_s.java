 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.team4159.frc2013;
 
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.DoubleSolenoid;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.HiTechnicColorSensor;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.SensorBase;
 import edu.wpi.first.wpilibj.Talon;
 import org.team4159.support.CombinedMotor;
 
 /**
  *
  * @author gavin
  */
 public class IO
 {
 	// To avoid changes, make sure all declarations are "public static final".
 	
 	/****************************************
 	 * JOYSTICKS                            *
 	 ****************************************/
 	public static final Joystick joystick1 = new Joystick (1);
 	public static final Joystick joystick2 = new Joystick (2);
 	
 	/****************************************
 	 * MOTORS                               *
 	 ****************************************/
 	public static final Talon driveLeftFront = new Talon (1);
 	public static final Talon driveLeftRear = new Talon (2);
 	public static final Talon driveRightFront = new Talon (3);
 	public static final Talon driveRightRear = new Talon (4);
 	
 	public static final CombinedMotor driveLeftCombined =
 		new CombinedMotor (driveLeftFront, driveLeftRear);
 	public static final CombinedMotor driveRightCombined =
 		new CombinedMotor (driveRightFront, driveRightRear);
 	
 	/****************************************
 	 * RELAYS                               *
 	 ****************************************/
 	public static final Relay pneumaticPump = new Relay (1, Relay.Direction.kForward);
 	
 	/****************************************
 	 * SOLENOIDS                            *
 	 ****************************************/
 	public static final DoubleSolenoid driveLeftGearbox = new DoubleSolenoid (1, 2);
 	public static final DoubleSolenoid driveRightGearbox = new DoubleSolenoid (3, 4);
 	
 	/****************************************
 	 * SENSORS                              *
 	 ****************************************/
 	public static final Encoder driveLeftEncoder = new Encoder (1, 2);
 	public static final Encoder driveRightEncoder = new Encoder (3, 4);
 	static {
 		final double distancePerPulse = 0.0001; // FIX ME!!!!!!
 		driveLeftEncoder.setDistancePerPulse (distancePerPulse);
 		driveRightEncoder.setDistancePerPulse (distancePerPulse);
 		driveLeftEncoder.start ();
 		driveRightEncoder.start ();
 	}
 	
 	public static final DigitalInput pressureSwitch = new DigitalInput (5);
 	
 	public static final HiTechnicColorSensor frisbeeColorSensor =
 		new HiTechnicColorSensor (SensorBase.getDefaultDigitalModule ());
 }
