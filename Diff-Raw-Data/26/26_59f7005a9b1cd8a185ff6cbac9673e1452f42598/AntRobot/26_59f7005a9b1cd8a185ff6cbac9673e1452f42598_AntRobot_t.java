 package robot;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import data.Position;
 import de.unihamburg.informatik.tams.project.communication.Barrel;
 import de.unihamburg.informatik.tams.project.communication.MapPosition;
 import de.unihamburg.informatik.tams.project.communication.exploration.Grid;
 import de.unihamburg.informatik.tams.project.communication.exploration.GridPosition;
 import de.unihamburg.informatik.tams.project.communication.network.CommunicationFactory;
 import device.Device;
 import device.Localize;
 import device.external.IGripperListener;
 import device.external.ILocalizeListener;
 import device.external.IPlannerListener;
 
 public class AntRobot extends PatrolRobot {
 
 	private Random rand = new Random();
 	
 	private Grid grid;
 	GridPosition prevGpos;
 	GridPosition gpos;
 	GridPosition goal;
 	ArrayList<GridPosition> positions;
 	Localize localize;
 	private int onTheWayCounter = 0;
 	private double previousMean = 0;
 	private double movingMean = 0;
 	private int nthMean = 1;
 
 	@Override
 	public void doStep() {
 
 		if(ownPosition.equals(new Position(0,0,0))) {
 			ownPosition = this.getPosition();
 		}
 		
 		if(state == RobotState.ON_THE_WAY) {
 			onTheWayCounter += 1;
 		}
 		
 		System.out.println("Robotstate: "+state);
 		System.out.println("Planner aktiv " + planner.isActive());
 		System.out.println("Planner aktuelles Ziel " + planner.getGoal());
 		System.out.println("Planner aktuelles Ziel valid " + planner.isValidGoal());
 		System.out.println("Eigene Position " + ownPosition);
 		System.out.println("Moving mean " + movingMean);
 		System.out.println(nthMean + ". Ziel");
 		System.out.println();
 		
 		if(state == RobotState.NEEDS_NEW_GOAL && !ownPosition.equals(new Position(0,0,0))) {
 			onTheWayCounter = 0;
 			position = new MapPosition(ownPosition.getX(), ownPosition.getY());
 			map.setRobotMapPosition(position);
 			prevGpos = gpos;
 //			gpos = new GridPosition(ownPosition.getX(), ownPosition.getY());
 			// TODO till fixed
 			gpos = grid.getOwnRobotPosition();
 			System.out.println("GridPosition: " + gpos);
 
 			// TODO wird ersetzt durch neighbours Methode des Grids
 			positions = new ArrayList<GridPosition>();
 			positions.add(new GridPosition(gpos.getxPosition()-1, gpos.getyPosition())); // north
 			positions.add(new GridPosition(gpos.getxPosition(), gpos.getyPosition()-1)); // west
 			positions.add(new GridPosition(gpos.getxPosition()+1, gpos.getyPosition())); // south
 			positions.add(new GridPosition(gpos.getxPosition(), gpos.getyPosition()+1)); // east
 			
 			if(!ownPosition.equals(new Position(0,0,0))) {
 				goal = choose(positions, grid);
 			}
 			
 			if (goal != null) {
 				planner.addIsDoneListener(new IPlannerListener() {
 					@Override
 					public void callWhenIsDone() {
 						state = RobotState.NEEDS_NEW_GOAL;
 						// TODO wieder aktivieren, wenn es keine Nullpointerexception mehr gibt
 //						grid.setOwnPosition(goal);
 						planner.removeIsDoneListener(this);
 						if (prevGpos == null) {	
 							grid.increaseToken(1, gpos);
 						} else {
 							grid.increaseToken(
 								(Math.max(grid.getToken(prevGpos), grid.getToken(gpos)) + 1) - grid.getToken(gpos), gpos);
 						}
 						
 						// Statistics
 						double tempMovingMean = movingMean;
 						movingMean = (onTheWayCounter + (nthMean-1)*previousMean) / nthMean;
 						previousMean = tempMovingMean;
 						nthMean += 1;
 						onTheWayCounter = 0;
 					}
 
 					@Override
 					public void callWhenAbort() {
 						/** Set the goal again. */
 						state = RobotState.NEEDS_NEW_GOAL;
 						planner.removeIsDoneListener(this);
 						onTheWayCounter = 0;
 						//robot.setGoal(robot.getGoal());
 						logger.info("Aborted");
 					}
 
 					@Override
 					public void callWhenNotValid() {
 						state = RobotState.NEEDS_NEW_GOAL;
 						planner.removeIsDoneListener(this);
 						onTheWayCounter = 0;
 						logger.info("No valid path");
 					}
 				});
 				// TODO Fix wenn Grid richtig funktioniert
 				Position goalPos = new Position(goal.getxPosition(), 
 																				goal.getyPosition(), 
 																				calculateGoalYawn(goal.getxPosition(), goal.getyPosition()));
 				this.setGoal(goalPos);
 				System.out.println("Dieses Ziel gesetzt " + goalPos);
 				grid.setRobotOnWayTo(this, goal);
 				
 				state = RobotState.ON_THE_WAY;
 			}
 		}
 
 		checkForNewBarrels();
 	}	
 	
 	public AntRobot(Device[] roboDevList) {
 		super(roboDevList);
 		state = RobotState.NEEDS_NEW_GOAL;
 		getGripper().close(new IGripperListener(){
 			@Override
 			public void whenOpened() {}
 			@Override
 			public void whenClosed() {
 				gripperState = RobotState.GRIPPER_CLOSE;
 			}
 			@Override
 			public void whenLifted() {}
 			@Override
 			public void whenReleased() {}
 			@Override
 			public void whenClosedLifted() {}
 			@Override
 			public void whenReleasedOpened() {}
 			@Override
 			public void whenError() {}
 			});
 		planner = getPlanner();
 		barrelPositions = Collections.synchronizedList(new ArrayList<double[]>());
		startRobotEyes(barrelPositions);
 		knownBarrels = new ArrayList<Barrel>();
 		localizer = getLocalizer();
 		localizer.addListener(new ILocalizeListener()
         {
             public void newPositionAvailable(Position newPose)
             {
                 ownPosition = newPose;
 //                System.err.println("New position: "+newPose.toString());
             }
         });
 		CommunicationFactory cf = new CommunicationFactory();
 		try {
 			map = cf.getSlaveMap(server, this);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		grid = map.getGrid();
 	}
 	
 	public void setGrid(Grid grid) {
 		this.grid = grid;
 	}
 	
 	public void setPositions(ArrayList<GridPosition> positions) {
 		this.positions = positions;
 	}
 
 	/**
 	 * Gets a list of grid positions and chooses the one with the fewest tokens, where no
 	 * other robot is driving to.
 	 *
 	 * @param positions The grid positions (neighbors)
 	 * @param grid The grid
 	 * @return The grid position with fewest tokens that no other robot is driving to.
 	 */
 	public GridPosition choose(ArrayList<GridPosition> positions, Grid grid) {
 		List<GridPosition> result = new ArrayList<GridPosition>();
 		for(GridPosition gpos : positions) {
 			// TODO Commented bis isRobotOnWayToToken gefixt ist
 //			if(grid.isRobotOnWayToToken(gpos)) {
 //				System.out.println("Robot on the way");
 //				continue;
 //			} else 
 			if(result.size() == 0) {
 				result.add(gpos);
 			} else if(grid.getToken(gpos) == grid.getToken(result.get(0))) {
 				result.add(gpos);
 			} else if(grid.getToken(gpos) < grid.getToken(result.get(0))) {
 				result = new ArrayList<GridPosition>();
 				result.add(gpos);
 			}
 		}
 		if (result.size() != 0) {
 			System.out.println("Resultsize " + result.size());
 			return result.get(rand.nextInt(result.size()));
 		} else {
 			return null;
 		}
 	}
 }
