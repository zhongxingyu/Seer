 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package vikingrobotics.commands.autonomous;
 
 import vikingrobotics.commands.arm.ArmRun;
 import vikingrobotics.commands.drivetrain.DriveStraight;
 import vikingrobotics.misc.Constants;
 import edu.wpi.first.wpilibj.command.CommandGroup;
 import edu.wpi.first.wpilibj.command.PrintCommand;
 import edu.wpi.first.wpilibj.command.WaitCommand;
 
 /**
  * Autonomous setting 2
  * 
 * 	-> Drive towards the bridge
  *  -> Run the arm and latch it
  *  -> Drive slowly towards the bridge to tip it
  *  -> Wait 3 seconds
  *  -> Drive away from the bridge
  *  
  * @author Neal
  */
 public class Auton2 extends CommandGroup implements Constants {
 	
 	private static double forwards = -1;
 	private static double backwards = 1;
 	private double DRIVE_SPEED = 0.7;
 	private double DRIVE_TIMEOUT = 2.0;
 	private double DRIVE_SPEED2 = 0.3;
 	private double DRIVE_TIMEOUT2 = 3.0;
 	private double DRIVE_SPEED3 = 0.3;
 	private double DRIVE_TIMEOUT3 = 2.0;
 	private double WAIT_ONE_SECOND = 1.0;
 	
 	public Auton2() {
 		super("Auton2");
 		/*
 		 * Drive backwards to the bridge at 0.7 speed for 2 seconds and wait for 1 second
 		 */
 		addSequential(new PrintCommand("[auton2] Driving back to the bridge at speed: " + DRIVE_SPEED + " and timeout: " + DRIVE_TIMEOUT));
 		addSequential(new DriveStraight(backwards * DRIVE_SPEED, DRIVE_TIMEOUT));
 		addSequential(new WaitCommand(WAIT_ONE_SECOND));
 		/*
 		 * Run the arm and latch it
 		 */
 		addSequential(new PrintCommand("[auton2] Running ArmRun()"));
 		addSequential(new ArmRun());
 		/*
 		 * Drive backwards slowly to tip the bridge
 		 */
 		addSequential(new PrintCommand("[auton2] Driving towards the bridge at speed: " + DRIVE_SPEED2 + " and timeout: " + DRIVE_TIMEOUT2));
 		addSequential(new DriveStraight(backwards * DRIVE_SPEED2, DRIVE_TIMEOUT2));
 		addSequential(new WaitCommand(3));
 		/*
 		 * Drive away from bridge slowly
 		 */
 		addSequential(new PrintCommand("[auton2] Driving away from bridge at speed: " + DRIVE_SPEED3 + " and timeout: " + DRIVE_TIMEOUT3));
 		addSequential(new DriveStraight(forwards * DRIVE_SPEED3, DRIVE_TIMEOUT3));
 		addSequential(new WaitCommand(WAIT_ONE_SECOND));
 	}
 
 }
