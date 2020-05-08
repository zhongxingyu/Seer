 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package vikingrobotics.commands.autonomous;
 
 import vikingrobotics.commands.drivetrain.DriveStraight;
 import vikingrobotics.commands.grabber.GrabberRun;
 import vikingrobotics.commands.shooter.ShooterFeed;
 import vikingrobotics.commands.shooter.ShooterRun;
 import vikingrobotics.misc.Constants;
 import edu.wpi.first.wpilibj.command.CommandGroup;
 import edu.wpi.first.wpilibj.command.PrintCommand;
 import edu.wpi.first.wpilibj.command.WaitCommand;
 
 /**
  * Autonomous setting 1
  * 
 *  -> Drive forwards until fender
  *  -> Run the shooter at speed 0.305
  *  -> Wait 5 seconds
  *  -> Shoot 1 ball
  *  -> Get the other ball to shooter
  *  -> Shoot the other ball
  *  -> Drive back
  *  
  * @author Neal
  */
 public class Auton1 extends CommandGroup implements Constants {
 	
 	private static double forwards = -1;
 	private static double backwards = 1;
 	private double SHOOTER_SPEED = 0.305;
 	private double DRIVE_SPEED = 0.7;
 	private double DRIVE_TIMEOUT = 3.0;
 	private double SHOOTER_WAIT_TIMEOUT = 5.0;
 	private double GRABBER_TIMEOUT = 2.5;
 	private double WAIT_ONE_SECOND = 1.0;
 	private double DRIVE_BACK_SPEED = 0.9;
 	private double DRIVE_BACK_TIMEOUT = 3.0;
 	
 	public Auton1() {
 		super("Auton1");
 		/*
 		 * Run shooter at 0.305 speed
 		 */
 		addSequential(new PrintCommand("[auton1] Running shooter at speed: " + SHOOTER_SPEED));
 		addParallel(new ShooterRun(SHOOTER_SPEED));
 		/*
 		 * Drive forwards to the fender at 0.7 speed for 3 seconds
 		 */
 		addSequential(new PrintCommand("[auton1] Driving to fender at speed: " + DRIVE_SPEED + " and timeout: " + DRIVE_TIMEOUT));
 		addSequential(new DriveStraight(forwards * DRIVE_SPEED, DRIVE_TIMEOUT));
 		/*
 		 * Run shooter again, in case it didn't before :/
 		 */
 		addSequential(new PrintCommand("[auton1] Running shooter again at speed: " + SHOOTER_SPEED));
 		addParallel(new ShooterRun(SHOOTER_SPEED));
 		/*
 		 * Wait for 5 seconds until the shooter gets ready
 		 */
 		addSequential(new PrintCommand("[auton1] Waiting " + SHOOTER_WAIT_TIMEOUT + " seconds for the shooter to speed up"));
 		addSequential(new WaitCommand(SHOOTER_WAIT_TIMEOUT));
 		/*
 		 * Feed one ball
 		 */
 		addSequential(new PrintCommand("[auton1] Feeding one ball"));
 		addSequential(new ShooterFeed(kTimeFeedOneBall));
 		/*
 		 * Run grabber for 3.5 seconds to get the other ball to the shooter
 		 */
 		addSequential(new PrintCommand("[auton1] Running ball grabber at timeout: " + GRABBER_TIMEOUT));
 		addSequential(new GrabberRun(), GRABBER_TIMEOUT);
 		/*
 		 * Feed another ball
 		 */
 		addSequential(new PrintCommand("[auton1] Feeding another ball"));
 		addSequential(new ShooterFeed(kTimeFeedOneBall));
 		/*
 		 * Wait one second
 		 */
 		addSequential(new PrintCommand("[auton1] Waiting " + WAIT_ONE_SECOND + " second(s)"));
 		addSequential(new WaitCommand(WAIT_ONE_SECOND));
 		/*
 		 * Drive back as far as it can (time up)
 		 * TODO: Why doesn't the DriveStraight command stop after timeout?
 		 */
 		addSequential(new PrintCommand("[auton1] Driving back at speed: " + DRIVE_BACK_SPEED + " and timeout: " + DRIVE_BACK_TIMEOUT));
 		addSequential(new DriveStraight(backwards * DRIVE_BACK_SPEED, DRIVE_BACK_TIMEOUT));
 		/*
 		 * Maybe helps stop the previous command?
 		 */
 		addSequential(new PrintCommand("[auton1] Waiting " + WAIT_ONE_SECOND + " second(s)"));
 		addSequential(new WaitCommand(WAIT_ONE_SECOND));
 	}
 
 }
