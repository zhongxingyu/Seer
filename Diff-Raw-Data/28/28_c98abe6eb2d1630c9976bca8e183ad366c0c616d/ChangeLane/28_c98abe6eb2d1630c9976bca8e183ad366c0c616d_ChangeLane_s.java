 package siver.agents.boat.actions;
 
import repast.simphony.space.continuous.NdPoint;
 import siver.agents.boat.CoxAgent;
 import siver.river.River.NoLaneFound;
 import siver.river.lane.Lane;
 import siver.river.lane.Lane.NoNextNode;
 import siver.river.lane.LaneChangeEdge;
import siver.river.lane.LaneEdge;
 import siver.river.lane.LaneNode;
import siver.river.lane.TemporaryLaneNode;
 
 public abstract class ChangeLane extends SingleTickAction {
 	final protected static int nodes_ahead_to_aim_for = 6;
 	protected Lane targetLane;
 	protected Lane startLane;
 	
 	
 	public ChangeLane(CoxAgent cox) {
 		super(cox);
 	}
 	
 	protected void setTargetLane(Lane tl) {
 		targetLane = tl;
 	}
 	
 	protected void setStartLane(Lane sl) {
 		startLane = sl;
 	}
 	
 	public Lane getTargetLane() {
 		return targetLane;
 	}
 	
 	public Lane getStartLane() {
 		return startLane;
 	}
 	
 	@Override
 	public void doExecute() {
 		if(location.changingLane()) return;
 		
 		setStartLane(location.getLane());
 		try {
 			directionSpecificSetup();
 		} catch (NoLaneFound e) {
 			return;
 		}
 		
 		LaneChangeEdge<LaneNode> edge;
 		
 		try {
 			edge = LaneChangeEdge.createLaneChangeBranch(boat.getLocation(), location.getEdge(), location.headingUpstream(), targetLane);
 		} catch (NoNextNode e) {
 			return;
 		}
 		
 		location.updateEdge(edge, false);
 	}
 	
 	
 	
 	protected abstract void directionSpecificSetup() throws NoLaneFound;
 
 }
