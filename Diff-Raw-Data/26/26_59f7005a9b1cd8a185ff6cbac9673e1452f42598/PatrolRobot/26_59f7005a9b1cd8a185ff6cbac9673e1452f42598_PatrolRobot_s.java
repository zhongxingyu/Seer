 package robot;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import roboteyes.RobotEyes;
 import data.Position;
 import de.unihamburg.informatik.tams.project.communication.Barrel;
 import de.unihamburg.informatik.tams.project.communication.BarrelColor;
 import de.unihamburg.informatik.tams.project.communication.MapPosition;
 import de.unihamburg.informatik.tams.project.communication.RobotMap;
 import de.unihamburg.informatik.tams.project.communication.State;
 import de.unihamburg.informatik.tams.project.communication.exploration.Exploration;
 import device.Device;
 import device.Planner;
 import device.external.IGripperListener;
 import device.external.IPlannerListener;
 
 public abstract class PatrolRobot extends Pioneer implements Exploration {
 
 	protected MapPosition position;
 
 	protected RobotState state;
 
 	protected RobotState plannerState;
 
 	protected RobotState gripperState;
 
 	protected Planner planner;
 	
 	protected List<double[]> barrelPositions;
 	
 	protected ArrayList<Barrel> knownBarrels;
 
 	protected data.Position ownPosition = new Position(0,0,0);
 
 	protected String server = "";
 	
 	protected RobotEyes eyes;
 
 	public void setServer(String server) {
 		this.server = server;
 	}
 
 	protected RobotMap map;
 
 	public PatrolRobot(Device[] devList) {
 		super(devList);
		// TODO Commented till Kinectdrivers work
//		eyes = new RobotEyes(barrelPositions);
 	}
 
 	public boolean hasGripper() {
 		return super.getGripper() != null;
 	}
 
 	public State getState() {
 		State result = null;
 		if (state == RobotState.NEEDS_NEW_GOAL
 				|| state == RobotState.ON_THE_WAY) {
 			result = State.EXPLORING;
 		} else if (state == RobotState.TRANSPORTING_BARREL) {
 			result = State.TRANSPORTING;
 		}
 		return result;
 	}
 
 	/**
 	 * Sets the state of the robot
 	 * @param state The robot state
 	 */
 	public void setState(RobotState state) {
 		this.state = state;
 	}
 
 	public MapPosition getMapPosition() {
 		return position;
 	}
 	
 	/*
 	 * Auf bestimmten gripperState warten
 	 */
 	public void waitForGripperState(RobotState rs) {
 		while (gripperState != rs) {
 			try {
 				Thread.sleep(500); // 500ms warten bis planerState erneut abgefragt wird
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/*
 	 * Auf bestimmten plannerState warten
 	 */
 	public void waitForPlannerState(RobotState rs) {
 		while (plannerState != rs) {
 			try {
 				Thread.sleep(500); // 500ms warten bis gripperState erneut abgefragt wird
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void transportBarrelTo(Barrel barrel, MapPosition targetPositionOfBarrel) {
 		// state setzen
 		state = RobotState.TRANSPORTING_BARREL;
 		MapPosition currentPositionOfBarrel = barrel.getPosition();
 		
 		// Position der Barrel und des Ziels erzeugen
 		Position barrelPos = new Position(currentPositionOfBarrel.getxPosition(), currentPositionOfBarrel.getyPosition(), 0);
 		Position targetPos = new Position(targetPositionOfBarrel.getxPosition(), targetPositionOfBarrel.getyPosition(), 0);
 
 		// IGripperListener für den Gripper
 		IGripperListener gl = new IGripperListener() {
 			@Override
 			public void whenOpened() {
 				gripperState = RobotState.GRIPPER_OPEN;
 				gripper.removeIsDoneListener(this);
 			}
 			@Override
 			public void whenClosed() {}
 			@Override
 			public void whenLifted() {}
 			@Override
 			public void whenReleased() {}
 			@Override
 			public void whenClosedLifted() {
 				gripperState = RobotState.GRIPPER_CLOSE;
 				gripper.removeIsDoneListener(this);
 			}
 			@Override
 			public void whenReleasedOpened() {
 				gripperState = RobotState.GRIPPER_OPEN;
 				gripper.removeIsDoneListener(this);
 			}
 			@Override
 			public void whenError() {}
 		};
 		// Gripper öffnen
 		gripperState = RobotState.OPENING_GRIPPER;
 		getGripper().open(gl);
 		// Warten bis Gripper offen
 		waitForGripperState(RobotState.GRIPPER_OPEN);
 
 		// Zur Barrel fahren
 		plannerState = RobotState.DRIVING_TO_BARREL;
 		planner.addIsDoneListener(new IPlannerListener() {
 			@Override
 			public void callWhenIsDone() {
 				plannerState = RobotState.BARREL_REACHED;
 			}
 			@Override
 			public void callWhenAbort() {}
 			@Override
 			public void callWhenNotValid() {}
 		});
 		planner.setGoal(barrelPos);
 		// Warten bis Barrel erreicht wurde
 		waitForPlannerState(RobotState.BARREL_REACHED);
 
 		// Gripper schliessen
 		gripperState = RobotState.CLOSING_GRIPPER;
 		getGripper().closeLift(gl);
 		// Warten bis Gripper geschlossen
 		waitForGripperState(RobotState.GRIPPER_CLOSE);
 
 		// Barrel zum Ziel fahren
 		plannerState = RobotState.TRANSPORTING_BARREL;
 		planner.addIsDoneListener(new IPlannerListener() {
 			@Override
 			public void callWhenIsDone() {
 				plannerState = RobotState.BARREL_TARGET_REACHED;
 			}
 			@Override
 			public void callWhenAbort() {}
 			@Override
 			public void callWhenNotValid() {
 				logger.info("No valid path");
 			}
 		});
 		planner.setGoal(targetPos);
 		// Warten bis Ziel erreicht wurde
 		waitForPlannerState(RobotState.BARREL_TARGET_REACHED);
 
 		// Barrel absetzen
 		gripperState = RobotState.OPENING_GRIPPER;
 		getGripper().releaseOpen(gl);
 		// Warten bis Gripper offen
 		waitForGripperState(RobotState.GRIPPER_OPEN);
 		
 		// Barrel aus Liste löschen
 		knownBarrels.remove(barrel);
 
 		// Etwas Abstand von der Barrel nehmen
 		Position p = this.getPosition();
 		double yaw = Math.toDegrees(p.getYaw());
 		int x = 0;
 		int y = 0;
 		if (yaw <= 90) {
 			x = -20;
 			y = 20;
 		}
 		else if (yaw > 90 && yaw <= 180) {
 			x = 20;
 			y = 20;
 		}
 		else if (yaw > 180 && yaw <= 270) {
 			x = 20;
 			y = -20;
 		}
 		else if (yaw > 270) {
 			x = -20;
 			y = -20;
 		}
 		Position newPos = new Position(p.getX()+x, p.getY()+y, p.getYaw());
 		planner.addIsDoneListener(new IPlannerListener() {
 			@Override
 			public void callWhenIsDone() {
 				state = RobotState.NEEDS_NEW_GOAL;
 			}
 			@Override
 			public void callWhenAbort() {}
 			@Override
 			public void callWhenNotValid() {
 				logger.info("No valid path");
 				state = RobotState.NEEDS_NEW_GOAL;
 			}
 		});
 		planner.setGoal(newPos);
 	}
 
 
 	/**
 	 * This method get periodically invoked. Depending on the current state it governs the robots behavior.
 	 */
 	public abstract void doStep();
 	
 	/**
 	 * Calculates from the distances given by the Kinect the world coordinates of the barrel.
 	 *
 	 * @param xcoord The distance from the middle of the robot to the middle of the barrel
 	 * @param ycoord The sidewise distance of the barrel. Positive is to the right
 	 * @return The absolute world coordinates of the barrel
 	 */
 	private MapPosition barrelCoordToWorldCoord(double xcoord, double ycoord) {
 		
 		// Drehung um ownPosition als Drehzentrum, um den Winkel ownPosition.getYawn()
 		double x0 = ownPosition.getX();
 		double y0 = ownPosition.getY();
 		double a = ownPosition.getYaw();
 		double x = ownPosition.getX() + ycoord/100;
 		double y = ownPosition.getY() + (-xcoord/100);
 		MapPosition barrelPosition = new MapPosition(x0 + (x - x0)*Math.cos(a) - (y - y0)*Math.sin(a),
 																								 y0 + (x - x0)*Math.sin(a) + (y - y0)*Math.cos(a));
 		return barrelPosition;
 	}
 	
 	/**
 	 * Sends all barrels in the barrelPositions list to the map
 	 */
 	protected void checkForNewBarrels() {
 		synchronized (barrelPositions) {
			if (barrelPositions.size() != 0) {
 				for (double[] barrel : barrelPositions) {
 					Barrel currentBarrel;
 					BarrelColor color = null;
 					MapPosition position = barrelCoordToWorldCoord(barrel[1], barrel[2]);
 
 					switch ((int) barrel[0]) {
 					case 0:
 						color = BarrelColor.BLUE;
 						break;
 					case 1:
 						color = BarrelColor.GREEN;
 						break;
 					case 2:
 						color = BarrelColor.YELLOW;
 						break;
 					default:
 						break;
 					}
 
 					currentBarrel = new Barrel(color, position);
 					map.setBarrel(currentBarrel);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Calculates the yawn the robot would have at the target position, if
 	 * it could drive there in a straight line.
 	 *
 	 * @param xGoal X-coordinate of the goal
 	 * @param yGoal Y-coordinate of the goal
 	 * @return The yawn at the target position
 	 */
 	protected double calculateGoalYawn(double xGoal, double yGoal) {
 		double y = yGoal - ownPosition.getY();
 		double x = xGoal - ownPosition.getX();
 		double degree = Math.acos(x / Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
 		if(yGoal < ownPosition.getY()) {
 			degree = degree * (-1);
 		}
 		return degree; 
 	}
	
 }
