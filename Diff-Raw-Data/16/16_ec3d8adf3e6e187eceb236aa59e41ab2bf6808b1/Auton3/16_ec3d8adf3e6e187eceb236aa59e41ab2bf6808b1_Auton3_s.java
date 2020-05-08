 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package vikingrobotics.commands.autonomous;
 
 import vikingrobotics.commands.drivetrain.DriveStraight;
 import vikingrobotics.misc.Constants;
 import edu.wpi.first.wpilibj.command.CommandGroup;
 import edu.wpi.first.wpilibj.command.PrintCommand;
 import edu.wpi.first.wpilibj.command.WaitCommand;
 
 /**
  * Autonomous setting 3
  * 
 * 	-> Drive backwards for 3 seconds
  *  
  * @author Neal
  */
 public class Auton3 extends CommandGroup implements Constants {
 	
 	private static double forwards = -1;
 	private static double backwards = 1;
 	private double DRIVE_SPEED = 0.7;
 	private double DRIVE_TIMEOUT = 3.0;
 	private double WAIT_ONE_SECOND = 1.0;
 	
 	public Auton3() {
 		super("Auton3");
 		/*
 		 * Drive backwards at 0.7 speed for 3 seconds
 		 */
 		addSequential(new PrintCommand("[auton2] Driving backwards at speed: " + DRIVE_SPEED + " and timeout: " + DRIVE_TIMEOUT));
 		addSequential(new DriveStraight(backwards * DRIVE_SPEED, DRIVE_TIMEOUT));
 		addSequential(new WaitCommand(WAIT_ONE_SECOND));
 	}
 
 }
