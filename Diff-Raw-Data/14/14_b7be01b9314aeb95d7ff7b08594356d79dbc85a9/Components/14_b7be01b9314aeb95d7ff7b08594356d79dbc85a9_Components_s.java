 package org.usfirst.frc1923;
 
 import org.usfirst.frc1923.systems.DriveSystem;
 import org.usfirst.frc1923.systems.ShooterAngleSystem;
 import org.usfirst.frc1923.systems.ShooterSystem;
 import org.usfirst.frc1923.systems.attachments.Gearbox;
 import org.usfirst.frc1923.utils.EnjoyStick;
 import org.usfirst.frc1923.utils.MotorGroup;
 import org.usfirst.frc1923.utils.XboxController;
 
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Preferences;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.Victor;
 import edu.wpi.first.wpilibj.networktables.NetworkTable;
 
 /**
  * A grouping of managed system components.
  * @author Aaron Weiss, Pavan Hegde, Bhavish Yalamanchi
  * @version 2.0
  * @since 2/9/13
  */
 public class Components {
	/**
	 * A static scope for preparing components when necessary.
	 */
	static {
		NetworkTable.setTeam(1923);
	}
	
 	// Java Setup
 	public static final NetworkTable networkTable = NetworkTable.getTable("midknight"); 
 	public static final Preferences preferences = Preferences.getInstance();
 	public static final EventBus eventBus = EventBus.getInstance();
 	
 	// Controllers
 	public static final EnjoyStick leftDriveStick = new EnjoyStick(1);
 	public static final EnjoyStick rightDriveStick = new EnjoyStick(2);
 	public static final XboxController operatorController = new XboxController(3);
 	
 	// Victors (Speed Controllers)
 	public static final Victor leftDriveOne = new Victor(3), leftDriveTwo = new Victor(4);
 	public static final Victor rightDriveOne = new Victor(1), rightDriveTwo = new Victor(2);
 	public static final Victor leftShooterOne = new Victor(5), leftShooterTwo = new Victor(6);
 	public static final Victor rightShooterOne = new Victor(7), rightShooterTwo = new Victor(8);
 	public static final Victor shooterAngleOne = new Victor(9);
 	
 	// Relays (Spikes)
 	public static final Relay compressor = new Relay(7);
 	public static final Relay ringLight = new Relay(8);
 	
 	// Digital Inputs
 	public static final DigitalInput compressorSafety = new DigitalInput(1);
 	
 	// Pneumatic Solenoids
 	public static final Solenoid pneumaticActuator = new Solenoid(1);
 	
 	// Motor Groups
 	public static final MotorGroup leftDriveGroup = new MotorGroup(leftDriveOne, leftDriveTwo);
 	public static final MotorGroup rightDriveGroup = new MotorGroup(rightDriveOne, rightDriveTwo);
 	public static final MotorGroup leftShooterGroup = new MotorGroup(leftShooterOne, leftShooterTwo);
 	public static final MotorGroup rightShooterGroup = new MotorGroup(rightShooterOne, rightShooterTwo);
 	public static final MotorGroup shooterAngleGroup = new MotorGroup(shooterAngleOne);
 	
 	// Systems
 	public static final DriveSystem driveSystem = new DriveSystem(leftDriveGroup, rightDriveGroup);
 	public static final ShooterSystem shooterSystem = new ShooterSystem(leftShooterGroup, rightShooterGroup);
 	public static final ShooterAngleSystem shooterAngleSystem = new ShooterAngleSystem(shooterAngleGroup);
 
 	// System Attachments
 	public static final Gearbox driveGearbox = new Gearbox(new double[]{0.65, 0.85, 1.0}, driveSystem);
 	public static final Gearbox shooterGearbox = new Gearbox(0.6, 1.0, 0.05, shooterSystem);
 }
