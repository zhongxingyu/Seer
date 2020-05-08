 /**
  * See http://www.presage2.info/ for more details on Presage2
  */
 package uk.ac.imperial.dws04;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 
 import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 import uk.ac.imperial.presage2.core.participant.Participant;
 import uk.ac.imperial.presage2.util.location.Location;
 import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
 
 /**
  * @author dws04
  *
  */
 public class ParticipantRoadLocationService extends ParticipantLocationService {
 
 	private final double perceptionRange;
 	
 	public ParticipantRoadLocationService(
 			Participant p,
 			EnvironmentSharedStateAccess sharedState,
 			EnvironmentServiceProvider serviceProvider) {
 		super(p, sharedState, serviceProvider);
 		this.perceptionRange = calculatePerceptionRange(sharedState);
 	}
 	
 	/**
 	 * Perception range should be stopping distance at maxSpeed.
 	 * @param sharedState
 	 * @return
 	 */
 	private double calculatePerceptionRange(EnvironmentSharedStateAccess sharedState) {
 		double mD = ((Integer)sharedState.getGlobal("maxDecel"));
 		double mS = ((Integer)sharedState.getGlobal("maxSpeed"));
 		double n = (mS / mD);
 		return ((((n+1)*n)/2)*mD) + mS;
 	}
 
 	/**
 	 * Get the agents who are visible to me at this time and their
 	 * {@link Location}s.
 	 * 
 	 * FIXME make this more efficient - do it with mod ?
 	 * 
 	 * @return {@link HashMap} of agent's {@link UUID} to {@link Location}
 	 */
 	@Override
 	public Map<UUID, Location> getNearbyAgents() {
 		if (this.membersService == null) {
 			throw new UnsupportedOperationException();
 		} else if (this.getAreaService() != null && this.getAreaService().isCellArea())  {
 			final Map<UUID, Location> agents = new HashMap<UUID, Location>();
 			RoadLocation myLoc = (RoadLocation) super.getAgentLocation(myID);
 			double range = this.perceptionRange;
 
 			for (int x = Math.max(0, (int) (myLoc.getX() - range)); x < Math.min(getAreaService()
 					.getSizeX(), (int) (myLoc.getX() + range)); x++) {
 				for (int y = Math.max(0, (int) (myLoc.getY() - range)); y < Math.min(
 						getAreaService().getSizeY(), (int) (myLoc.getY() + range)); y++) {
 					RoadLocation c = new RoadLocation(x, y);
 					for (UUID a : getAreaService().getCell(x, y, 0)) {
 						agents.put(a, c);
 					}
 				}
 			}
 			/*
 			 *  Need to wrap perception.
 			 *   myLoc.getY()+range > getAreaService().getSizeY()
 			 *   getAreaService().getMaxY() - myLoc.getY() > range
 			 *   { diff = getAreaService().getSizeY() - myLoc.getY(); if (diff>range) then also check...
 			 *   for (int y = 0; y < diff; y++) 
 			 */
 			int diff = (int) (getAreaService().getSizeY() - myLoc.getY());
 			if (diff > range) {
 				for (int x = Math.max(0, (int) (myLoc.getX() - range)); x < Math.min(getAreaService()
 						.getSizeX(), (int) (myLoc.getX() + range)); x++) {
 					for (int y = 0; y < diff; y++) {
 						RoadLocation c = new RoadLocation(x, y);
 						for (UUID a : getAreaService().getCell(x, y, 0)) {
 							agents.put(a, c);
 						}
 					}
 				}
 			}
 			
 			
 			agents.remove(this.myID);
 			return agents;
 		} else {
 			throw new UnsupportedOperationException("Not a cell area !");
 		}
 	}
 	
 	/**
 	 * @param lane to check 
 	 * @return UUID of closest agent to front, or null if there wasn't one
 	 */
 	public UUID getAgentToFront(int lane){
 		Entry <UUID,Location> result = null;
 		for (Entry<UUID, Location> entry : getNearbyAgents().entrySet()) {
 			if (((RoadLocation)(entry.getValue())).getLane() == lane) {
 				if ((result==null) || ((((RoadLocation)(entry.getValue())).getOffset()) < (((RoadLocation)(result.getValue())).getOffset())) ) {
 					result = entry;
 				}
 			}
 		}
 		if (result!=null) {
 			return result.getKey();
 		}
 		else {
 			return null;
 		}
 			
 		
 	}
 
 	/**
 	 * 
 	 * @param locA
 	 * @param locB
 	 * @return the distance that must be travelled between A.offset and B.offset (ie, it ignores difference in lanes)
 	 */
 	public int getOffsetDistanceBetween(RoadLocation locA, RoadLocation locB) {
 		// check locations aren't the same
 		if (locA.getOffset() == locB.getOffset()) {
 			return 0;
 		}
 		// check for simple case (no wrap)
 		else if (locB.getOffset() > locA.getOffset()) {
 			return (locB.getOffset() - locA.getOffset());
 		}
 		// check for wrap
 		else {
 			int distanceToEnd = (this.getAreaService().getSizeY()-1) - locA.getOffset();
 			return distanceToEnd + locB.getOffset();
 		}
 	}
 	
 	public int getLanes() {
 		return this.getAreaService().getSizeX()-1;
 	}
 	
 	public boolean isValidLane(int lane) {
 		return (lane>=0 && lane<=getLanes());
 	}
 
 }
