 package edu.kit.curiosity;
 
 import lejos.nxt.Motor;
 import lejos.robotics.subsumption.Behavior;
 import lejos.robotics.subsumption.CustomArbitrator;
 import edu.kit.curiosity.behaviors.DriveForward;
 import edu.kit.curiosity.behaviors.MotorAStall;
 import edu.kit.curiosity.behaviors.SensorHeadPosition;
 import edu.kit.curiosity.behaviors.bridge.AbyssDetected;
 import edu.kit.curiosity.behaviors.bridge.DriveUntilAbyss;
 import edu.kit.curiosity.behaviors.gate.RollFloor;
 import edu.kit.curiosity.behaviors.maze.BeginMaze;
 import edu.kit.curiosity.behaviors.maze.FollowWall;
 import edu.kit.curiosity.behaviors.maze.FoundEndLine;
 import edu.kit.curiosity.behaviors.maze.HitWall;
 import edu.kit.curiosity.behaviors.maze.SwampDetected;
 import edu.kit.curiosity.behaviors.maze.SwampLeft;
 import edu.kit.curiosity.behaviors.race.Race;
 import edu.kit.curiosity.behaviors.race.RaceDrive;
 import edu.kit.curiosity.behaviors.tape.GapFound;
 import edu.kit.curiosity.behaviors.tape.LineFollow;
 import edu.kit.curiosity.behaviors.tape.ObstacleFound;
 import edu.kit.curiosity.behaviors.tape.TapeLost;
 
 /**
  * This class manages the different arbitrators for all the different levels.
  * 
  * @author Curiosity
  */
 public class ArbitratorManager {
 	private CustomArbitrator arbitrator;
 	private Thread thread = new Thread();
 	private RobotState curState;
 
 	/**
 	 * Race behavior and arbitrator
 	 */
 	private Behavior r1 = new RaceDrive();
 	private Behavior r2 = new Race();
 	private Behavior r3 = new SensorHeadPosition();
 	private Behavior r4 = new MotorAStall();
 	private Behavior[] raceBehavior = { r1, r2, r3, r4 };
 
 	/**
 	 * Bridge behavior and arbitrator
 	 */
 	private Behavior b1 = new DriveUntilAbyss();
 	private Behavior b2 = new AbyssDetected();
 	private Behavior b3 = new SensorHeadPosition();
 	private Behavior b4 = new MotorAStall();
 	private Behavior[] bridgeBehavior = { b1, b2, b3, b4 };
 
 	/**
 	 * Maze behavior and arbitrator
 	 */
 	private Behavior m1 = new DriveForward();
 	private Behavior m2 = new FollowWall(12);
 	private Behavior m3 = new BeginMaze();
 	private Behavior m4 = new HitWall();
 	private Behavior m5 = new SensorHeadPosition();
 	private Behavior m6 = new SwampDetected();
 	private Behavior m7 = new SwampLeft();
 	private Behavior m8 = new FoundEndLine();
 	private Behavior m9 = new MotorAStall();
 	private Behavior[] mazeBehavior = { m1, m2, m3, m4, m5, m6, m7, m8, m9 };
 
 	/**
 	 * Gate behavior and arbitrator
 	 */
 	private Behavior g1 = new DriveForward();
 	private Behavior g2 = new RollFloor();
 	private Behavior g3 = new SensorHeadPosition();
 	private Behavior g4 = new MotorAStall();
 	private Behavior[] gateBehavior = { g1, g2, g3, g4 };
 
 	/**
 	 * Tape behavior and arbitrator
 	 */
 	private Behavior t1 = new LineFollow();
 	private Behavior t2 = new TapeLost();
 	private Behavior t3 = new GapFound(); // >130
 	private Behavior t4 = new ObstacleFound();
 	private Behavior t5 = new SensorHeadPosition();
 	private Behavior t6 = new MotorAStall();
 	private Behavior[] tapeBehavior = { t1, t2, t3, t4, t5, t6 };
 
 	/**
 	 * Instantiate an {@code ArbitratorManager}
 	 */
 	public ArbitratorManager() {
 		init();
 	}
 
 	/**
 	 * Instantiate an {@code ArbitratorManager} with a given {@code RobotState
 	 * }.
 	 * 
 	 * @param state
 	 *            given {@code RobotState} to instantiate the
 	 *            {@code ArbitratorManager} with
 	 */
 	public ArbitratorManager(RobotState state) {
 		init(state);
 	}
 
 	/**
 	 * initializes the {@code CustomArbitrator}.
 	 */
 	private void init() {
 		updateArbitrator(Settings.FIRST_LEVEL);
 	}
 
 	/**
 	 * initializes the {@code CustomArbitrator} with a given {@code RobotState}
 	 * 
 	 * @param state
 	 *            given {@code RobotState} to initialize arbitrator with
 	 */
 	private void init(RobotState state) {
 		updateArbitrator(state);
 	}
 
 	/**
 	 * Changes the current arbitrator, according to the given {@code RobotState}
 	 * .
 	 * 
 	 * @param state
 	 *            given {@code RobotState} to change arbitrator one
 	 */
 	public void changeState(RobotState state) {
 		this.curState = state;
 		updateArbitrator(this.curState);
 	}
 
 	/**
 	 * Update the current arbitrator to a level specific one, depending on the
 	 * passed {@code RobotState}.
 	 * 
 	 * @param state
 	 *            given {@code RobotState} to change the arbitrator to
 	 */
 	private void updateArbitrator(RobotState state) {
 		switch (state) {
 			case RACE:
 				System.out.println("Race arbitrator selected");
 				Settings.PILOT.setTravelSpeed(Settings.PILOT.getMaxTravelSpeed());
 				Settings.PILOT.setRotateSpeed(Settings.PILOT.getMaxRotateSpeed() / 4);
 				Motor.A.setSpeed(Motor.A.getMaxSpeed() / 5);
 				Settings.motorAAngle = 90;
 
 				// Not in first row
 				if (Settings.SONIC.getDistance() < 20) {
 					Settings.inFirstRow = false;
 
 					// In first row
 				} else {
 					Settings.inFirstRow = true;
 				}
 
 				this.arbitrator = new CustomArbitrator(raceBehavior);
 				break;
 			case MAZE:
 				System.out.println("Maze arbitrator selected");
 				Settings.PILOT.setTravelSpeed(Settings.PILOT.getMaxTravelSpeed() / 2);
 				Settings.PILOT.setRotateSpeed(Settings.PILOT.getMaxRotateSpeed() / 4);
 				Motor.A.setSpeed(Motor.A.getMaxSpeed() / 5);
 				Settings.motorAAngle = 0;
 				this.arbitrator = new CustomArbitrator(mazeBehavior);
 				break;
 			case TAPE:
 				System.out.println("Tape arbitrator selected");
 				Motor.A.setSpeed(Motor.A.getMaxSpeed() / 5);
 				Settings.PILOT.setTravelSpeed(Settings.PILOT.getMaxTravelSpeed() * 0.75);
 				Settings.PILOT.setRotateSpeed(Settings.PILOT.getMaxRotateSpeed() * 0.15);
 				Settings.motorAAngle = 90;
 				this.arbitrator = new CustomArbitrator(tapeBehavior);
 				break;
 			case GATE:
 				System.out.println("Gate arbitrator selected");
 				Settings.PILOT.setTravelSpeed(Settings.PILOT.getMaxTravelSpeed() / 1.5);
 				Settings.PILOT.setRotateSpeed(Settings.PILOT.getMaxRotateSpeed() / 4);
 				Motor.A.setSpeed(Motor.A.getMaxSpeed() / 5);
 				Settings.motorAAngle = 90;
 				this.arbitrator = new CustomArbitrator(gateBehavior);
 				break;
 			case BRIDGE:
 				System.out.println("Bridge arbitrator selected");
 				Motor.A.setSpeed(Motor.A.getMaxSpeed() / 5);
 				Settings.PILOT.setRotateSpeed(Settings.PILOT.getMaxRotateSpeed() / 5);
 				Settings.PILOT.setTravelSpeed(30);
 				Settings.motorAAngle = 0;
 				this.arbitrator = new CustomArbitrator(this.bridgeBehavior);
 				break;
 			default:
 				System.out.println("No arbitrator selected! Error! Error!");
 				break;
 		}
 
 		// update the thread to run the selected arbitrator
 		this.thread = new Thread(this.arbitrator);
 		this.thread.start();
 	}
 }
