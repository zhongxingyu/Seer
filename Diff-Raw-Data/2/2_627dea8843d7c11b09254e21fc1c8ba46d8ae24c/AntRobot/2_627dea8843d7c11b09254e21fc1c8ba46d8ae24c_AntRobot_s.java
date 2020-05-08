 package robot;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import de.unihamburg.informatik.tams.project.communication.MapPosition;
 import de.unihamburg.informatik.tams.project.communication.State;
 import de.unihamburg.informatik.tams.project.communication.exploration.Exploration;
 import de.unihamburg.informatik.tams.project.communication.exploration.Grid;
 import de.unihamburg.informatik.tams.project.communication.exploration.GridPosition;
 import device.Device;
 import device.external.IPlannerListener;
 import data.Position;
 
 public class AntRobot extends NavRobot implements Exploration {
 
 	private Random rand = new Random();
 	
 	private data.Position ownPosition = this.getPosition();
 	
 	private Grid grid;
 	private MapPosition position;
 	
 	GridPosition gpos;
 	GridPosition goal;
 	List<GridPosition> positions;
 	
 	RobotState state;
 	
 	public void doStep() {
 		if(state == RobotState.NEEDS_NEW_GOAL) {
 			ownPosition = this.getPosition();
 			position = new MapPosition((int)ownPosition.getX(), (int)ownPosition.getY());
 			gpos = grid.getOwnPosition(position);
 
 			positions = new ArrayList<GridPosition>();
 		
 			positions.add(new GridPosition(gpos.getxPosition()-1, gpos.getyPosition())); // north
 			positions.add(new GridPosition(gpos.getxPosition(), gpos.getyPosition()-1)); // west
 			positions.add(new GridPosition(gpos.getxPosition()+1, gpos.getyPosition())); // south
 			positions.add(new GridPosition(gpos.getxPosition(), gpos.getyPosition()+1)); // east
 			
 			goal = choose();
 			grid.setRobotOnWayTo(this, goal);
 			getPlanner().setGoal(new Position(goal.getxPosition(), goal.getyPosition(), 0));
 			grid.increaseToken(grid.getToken(gpos)+1, gpos);
 			state = RobotState.ON_THE_WAY;
 			
 			getPlanner().addIsDoneListener(new IPlannerListener() {
 				@Override public void callWhenIsDone() {
 					state = RobotState.NEEDS_NEW_GOAL;
 				}
 
 				@Override public void callWhenAbort() {
 					/** Set the goal again. */
 					//robot.setGoal(robot.getGoal());
 					logger.info("Aborted");
 				}
 
 				@Override public void callWhenNotValid() {
 					logger.info("No valid path");
 				}
 			});
 		}
 	}	
 	
 	public AntRobot(Device[] roboDevList) {
 		super(roboDevList);
 		state = RobotState.NEEDS_NEW_GOAL;
 		doStep();
 	}
 	
 	public void setGrid(Grid grid) {
 		this.grid = grid;
 	}
 
 	private GridPosition choose() {
 		List<GridPosition> result = new ArrayList<GridPosition>();
 		for(GridPosition gpos : positions) {
 			if(result.size() == 0) {
 				result.add(gpos);
 			} else if(!grid.isRobotOnWayToToken(gpos) && grid.getToken(gpos) == grid.getToken(result.get(0))) {
 				result.add(gpos);
 			} else if(!grid.isRobotOnWayToToken(gpos) && grid.getToken(gpos) < grid.getToken(result.get(0))) {
 				result = new ArrayList<GridPosition>();
 				result.add(gpos);
 			}
 		}
 		return result.get(rand.nextInt(result.size()));
 	}
 
 		
 	@Override
 	public boolean hasGripper() {
 		return super.getGripper() != null;
 	}
 
 	@Override
 	public State getState() {
 		State result = null;
 		if(state == RobotState.NEEDS_NEW_GOAL || state == RobotState.ON_THE_WAY) {
			result = State.EXPLORATING;
 		} else if(state == RobotState.TRANSPORTING_BARREL) {
 			result = State.TRANSPORTING;
 		}
 		return result;
 	}
 
 	@Override
 	public MapPosition getMapPosition() {
 		return position;
 	}
 
 	@Override
 	public void transportBarrelTo(MapPosition currentPositionOfBarrel,
 			MapPosition targetPositionOfBarrel) {
 		// TODO Auto-generated method stub
 		
 	}
 }
