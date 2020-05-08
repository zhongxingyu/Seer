 /**
  * See http://www.presage2.info/ for more details on Presage2
  */
 package uk.ac.imperial.dws04.Presage2Experiments;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.log4j.Level;
 
 import uk.ac.imperial.dws04.utils.MathsUtils.MathsUtils;
 import uk.ac.imperial.dws04.utils.record.Pair;
 import uk.ac.imperial.dws04.utils.record.PairBDescComparator;
 import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.messaging.Input;
 import uk.ac.imperial.presage2.core.simulator.SimTime;
 import uk.ac.imperial.presage2.util.fsm.Action;
 import uk.ac.imperial.presage2.util.fsm.EventTypeCondition;
 import uk.ac.imperial.presage2.util.fsm.FSM;
 import uk.ac.imperial.presage2.util.fsm.FSMDescription;
 import uk.ac.imperial.presage2.util.fsm.FSMException;
 import uk.ac.imperial.presage2.util.fsm.StateType;
 import uk.ac.imperial.presage2.util.fsm.Transition;
 import uk.ac.imperial.presage2.util.fsm.TransitionCondition;
 import uk.ac.imperial.presage2.util.location.CellMove;
 import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
 
 /**
  * @author dws04
  * 
  */
 public class RoadAgent extends AbstractParticipant {
 
 	private enum OwnChoiceMethod {SAFE, PLANNED};
 	private enum NeighbourChoiceMethod {WORSTCASE, GOALS, INSTITUTIONAL};
 
 	
 	protected Driver driver;
 	protected RoadLocation myLoc;
 	protected int mySpeed;
 	protected final RoadAgentGoals goals;
 	private Integer junctionsLeft;
 	
 	/**
 	 * FSM Stuff
 	 */
 	/**
 	 * Event to trigger the change in an FSM indicating the agent should move to the exit
 	 * @author dws04
 	 *
 	 */
 	private class MoveToExitEvent {
 		MoveToExitEvent(){
 		}
 	}
 	private FSMDescription fsmDesc = FSM.description();
 	private FSM fsm;
 
 	
 	// Variable to store the location service.
 	ParticipantRoadLocationService locationService;
 	ParticipantSpeedService speedService;
 	/*RoadEnvironmentService environmentService;*/
 	 
	public RoadAgent(UUID id, String name, RoadLocation myLoc, int mySpeed, RoadAgentGoals goals) {
 		super(id, name);
 		this.myLoc = myLoc;
 		this.mySpeed = mySpeed;
 		this.goals = goals;
 		if (this.goals.getDest()!=null) {
 			// you want to pass one fewer than your destination
 			this.junctionsLeft = goals.getDest()-1;
 		}
 		else {
 			this.junctionsLeft = null;
 		}
 		//this.driver = new Driver(id, locationService, speedService/*, environmentService*/);
 		
 	}
 	
 	@Override
 	protected Set<ParticipantSharedState> getSharedState() {
 		Set<ParticipantSharedState> ss = super.getSharedState();
 		ss.add(ParticipantRoadLocationService.createSharedState(getID(), myLoc));
 		ss.add(ParticipantSpeedService.createSharedState(getID(), mySpeed));
 		return ss;
 	}
 	
 	@Override
 	public void initialise() {
 		super.initialise();
 		// get the ParticipantRoadLocationService.
 		try {
 			this.locationService = getEnvironmentService(ParticipantRoadLocationService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 		}
 		// get the ParticipantRoadSpeedService.
 		try {
 			this.speedService = getEnvironmentService(ParticipantSpeedService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 		}
 		try {
 			this.driver = new Driver(getID(), this);
 		} catch (UnavailableServiceException e) {
 			e.printStackTrace();
 		}
 		/*// get the RoadEnvironmentService.
 		try {
 			this.environmentService = getEnvironmentService(RoadEnvironmentService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 		}*/
 		// Init the FSM
 		try {
 			fsmDesc.addState("IDLE", StateType.START)
 				.addState("MOVE_TO_EXIT", StateType.ACTIVE)
 				.addTransition("IDLE_TO_MOVE", new EventTypeCondition(MoveToExitEvent.class), "IDLE", "MOVE_TO_EXIT", Action.NOOP);
 			fsm = new FSM(fsmDesc, null);
 			if ( (junctionsLeft!=null) && (junctionsLeft<=1) ){
 				fsm.applyEvent(new MoveToExitEvent());
 			}
 		} catch (FSMException e) {
 			logger.warn("You can't initialise the FSM like this:" + e);
 		}
 	}
 	
 	@Override
 	public void execute() {
 		/*
 		 * Get physical state
 		 */
 		myLoc = (RoadLocation) locationService.getAgentLocation(getID());
 		mySpeed = speedService.getAgentSpeed(getID());
 		Integer junctionDist = this.locationService.getDistanceToNextJunction();
 		
 		/*
 		 * Retrieve (in case we want to change them...) macrogoals
 		 * FIXME TODO 
 		 */
 		
 		/*
 		 * Get IPCon info
 		 *  - get the RIC the agent is in
 		 *  - get the current state for each (ie, cheat :P - maybe this should rely on memory ?)
 		 *  - - this is only to get indication of required speed and spacing
 		 * FIXME TODO
 		 */
 		
 		/*
 		 * Get obligations
 		 * Get permissions
 		 * Use permissions to instantiate obligations
 		 * Check for conflicting obligations/permissions
 		 * Take note of permission to vote
 		 * Add all relevant actions to queue of actions
 		 * FIXME TODO
 		 */
 		
 		/*
 		 * Derive microgoals to fulfil macrogoals
 		 *  - take into account distance to exit
 		 *  - time to get to exit
 		 *  - fuel economy ?
 		 *  - IPCon agreed speed etc
 		 * Reason actions to fulfil microgoals
 		 * Check for conflicts
 		 * All all relevant actions to queue of actions
 		 * FIXME TODO
 		 */
 		
 		
 		
 		// FIXME TODO implement these then choose them properly :P
 		NeighbourChoiceMethod neighbourChoiceMethod = NeighbourChoiceMethod.WORSTCASE;
 		OwnChoiceMethod ownChoiceMethod = OwnChoiceMethod.SAFE;
 	 
 		logger.info("[" + getID() + "] My location is: " + this.myLoc + 
 										", my speed is " + this.mySpeed + 
 										", my goalSpeed is " + this.goals.getSpeed() + 
 										", and I have " + junctionsLeft + " junctions to pass before my goal of " + goals.getDest() +
 										", so I am in state " + fsm.getState());
 		logger.info("I can see the following agents:" + locationService.getNearbyAgents());
 		saveDataToDB();
 
 		
 		CellMove move;
 		// Check to see if you want to turn off, then if you can end up at the junction in the next timecycle, do so
 		if (	(fsm.getState().equals("MOVE_TO_EXIT")) && (junctionDist!=null) ) {
 			//move = driver.turnOff();
 			move = createExitMove(junctionDist, neighbourChoiceMethod);
 		}
 		else {
 			move = createMove(ownChoiceMethod, neighbourChoiceMethod);
 		}
 		if ((junctionDist!=null) && (junctionDist <= move.getYInt())) {
 			passJunction();
 		}
 		submitMove(move);
 	}
 	
 	private void passJunction(){
 		if (junctionsLeft!=null) {
 			junctionsLeft--;
 			if ((junctionsLeft<1)&& (!fsm.getState().equals("MOVE_TO_EXIT"))) {
 				try {
 					fsm.applyEvent(new MoveToExitEvent());
 					logger.info("[" + getID() + "] Agent " + getName() + " will move towards the exit in the next cycle.");
 				} catch (FSMException e) {
 					logger.warn("FSM can't handle event type MoveToExitEvent:" + e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Create a safe move with the intention of heading towards the exit
 	 * @return
 	 */
 	private CellMove createExitMove(int nextJunctionDist, NeighbourChoiceMethod neighbourChoiceMethod) {
 		CellMove result = null;
 		if (myLoc.getLane()==0) {
 			if (	(nextJunctionDist>= Math.max((mySpeed-speedService.getMaxDecel()), 1)) &&
 					(nextJunctionDist<= Math.min((mySpeed+speedService.getMaxAccel()), speedService.getMaxSpeed())) ) {
 				result = driver.turnOff();
 				logger.debug("[" + getID() + "] Agent " + getName() + " turning off in " + nextJunctionDist);
 			}
 			else {
 				// FIXME TODO
 				// try to make it so you can end a cycle on the right cell
 				// find a safe move in this lane; this gives you the max safe speed you can move at
 				Pair<CellMove, Integer> maxSpeedMove = createMoveFromNeighbours(myLoc.getLane(), neighbourChoiceMethod);
 				int maxSpeed = maxSpeedMove.getA().getYInt();
 				if (maxSpeedMove.getB().equals(Integer.MAX_VALUE)) {
 					// get a safe move to the exit in the lane
 					result = driver.moveIntoLaneAtSpeed(myLoc.getLane(), safeMoveSpeedToExit(nextJunctionDist, maxSpeed, myLoc.getLane()));
 				}
 				else {
 					logger.debug("[" + getID() + "] Agent " + getName() + " couldn't find a safe move in lane " + (myLoc.getLane()) + " to turn towards the exit, so checking the next lane.");
 					Pair<CellMove, Integer> maxSpeedMove2 = createMoveFromNeighbours(myLoc.getLane()+1, neighbourChoiceMethod);
 					int maxSpeed2 = maxSpeedMove2.getA().getYInt();
 					if ( (maxSpeedMove2.getB().equals(Integer.MAX_VALUE)) || (maxSpeedMove2.getB()>maxSpeedMove.getB())) {
 						// if you can change lanes right, do so.
 						logger.debug("[" + getID() + "] Agent " + getName() + " found a safe(r) move in lane " + (myLoc.getLane()+1) + " so is moving out in hope.");
 						result = driver.moveIntoLaneAtSpeed(myLoc.getLane()+1, safeMoveSpeedToExit(nextJunctionDist, maxSpeed2, myLoc.getLane()+1));
 					}
 					else {
 						// if not, slow down
 						logger.debug("[" + getID() + "] Agent " + getName() + " couldn't find a safe move in lane " + (myLoc.getLane()+1) + ", so is staying in lane with move " + maxSpeedMove.getA());
 						result = maxSpeedMove.getA();
 					}
 				}
 			}
 		}
 		else {
 			// you're not in lane0 (check validity anyway)
 			if (locationService.isValidLane(myLoc.getLane()-1)) {
 				Pair<CellMove, Integer> maxSpeedMove = createMoveFromNeighbours(myLoc.getLane()-1, neighbourChoiceMethod);
 				int maxSpeed = maxSpeedMove.getA().getYInt();
 				if (maxSpeedMove.getB().equals(Integer.MAX_VALUE)) {
 					// if you can change lanes left, do so.
 					logger.debug("[" + getID() + "] Agent " + getName() + " found a safe move in lane " + (myLoc.getLane()-1) + " so is moving towards the exit.");
 					result = driver.moveIntoLaneAtSpeed(myLoc.getLane()-1, safeMoveSpeedToExit(nextJunctionDist, maxSpeed, myLoc.getLane()-1));
 				}
 				else {
 					// if not, work out speed for current lane
 					logger.debug("[" + getID() + "] Agent " + getName() + " couldn't find a safe move in lane " + (myLoc.getLane()-1) + " to turn towards the exit, so is checking the current lane.");
 					Pair<CellMove, Integer> maxSpeedMove2 = createMoveFromNeighbours(myLoc.getLane(), neighbourChoiceMethod);
 					int maxSpeed2 = maxSpeedMove2.getA().getYInt();
 					if (maxSpeedMove2.getB().equals(Integer.MAX_VALUE)) {
 						// if you canstay in current lane, do so.
 						logger.debug("[" + getID() + "] Agent " + getName() + " found a safe move in lane " + (myLoc.getLane()) + " so is moving to the exit.");
 						result = driver.moveIntoLaneAtSpeed(myLoc.getLane(), safeMoveSpeedToExit(nextJunctionDist, maxSpeed2, myLoc.getLane()));
 					}
 					else {
 						// if not, slow down
 						logger.debug("[" + getID() + "] Agent " + getName() + " couldn't find a safe move in lane " + (myLoc.getLane()) + ", so is slowing down in hope.");
 						result = driver.decelerateMax();
 					}
 				}
 			}
 			else {
 				// skip, not a valid lane - this should never happen !
 				logger.warn("[" + getID() + "] Agent " + getName() + " tried to check invalid lane " + (myLoc.getLane()-1) + " for a safe move");
 				result = null;
 			}
 		}
 		if (result==null){
 			logger.warn("Shouldn't get here.");
 			result = driver.decelerateMax();
 		}
 		return result;
 	}
 
 	/**
 	 * @param nextJunctionDist
 	 * @param result
 	 * @param temp
 	 * @return
 	 */
 	private int safeMoveSpeedToExit(int nextJunctionDist, int maxSpeed, int lane) {
 		 logger.debug("[" + getID() + "] Agent " + getName() + " the maximum safe speed in lane " + lane + " is " + maxSpeed);
 		// check to see if nextJunctionDist is a multiple of this speed (mod(nJD,speed)==0)
 		if (MathsUtils.mod(nextJunctionDist,maxSpeed)==0) {
 			// if it is, yay
 			return maxSpeed;
 		}
 		else {
 			// otherwise, check all the speeds between maxSpeed and yourSpeed-maxDecell for the same thing
 			for (int i = maxSpeed-1; i>=mySpeed-speedService.getMaxDecel(); i--) {
 				if (MathsUtils.mod(nextJunctionDist,i)==0) {
 					// if it is, yay
 					logger.debug("[" + getID() + "] Agent " + getName() + " found a good move in lane " + lane + " at speed " + i);
 					return i;
 				}
 				else {
 					//Level lvl = logger.getLevel();
 					//logger.setLevel(Level.TRACE);
 					logger.trace("[" + getID() + "] Agent " + getName() + " checking speed " + i);
 					//logger.setLevel(lvl);
 				}
 			}
 			// if none of them are good, then decelMax
 			logger.debug("[" + getID() + "] Agent " + getName() + " couldn't find a good speed in lane " + lane + " so is decelerating");
 			return driver.decelerateToCrawl().getYInt();
 		}
 	}
 	
 	@SuppressWarnings("unused")
 	private CellMove createMove(OwnChoiceMethod ownChoiceMethod, NeighbourChoiceMethod neighbourChoiceMethod){
 		Pair<CellMove, Integer> temp = null;
 		// This is an indirect assumption of only three lanes
 		//  - yes we only want to check in lanes we can move into, but
 		//  - we should also take into account agents not in those lanes which might move into them ahead of us.
 		ArrayList<Integer> availableLanes = new ArrayList<Integer>(3);
 		LinkedList<Pair<CellMove,Integer>> actions = new LinkedList<Pair<CellMove,Integer>>();
 		availableLanes.add(myLoc.getLane());
 		availableLanes.add(myLoc.getLane()+1);
 		availableLanes.add(myLoc.getLane()-1);
 		Level lvl = logger.getLevel();
 		//logger.setLevel(Level.TRACE);
 		logger.trace("list of lanes is: " + availableLanes);
 		//logger.setLevel(lvl);
 		
 		for (int i = 0; i <=availableLanes.size()-1; i++) {
 			if (locationService.isValidLane(availableLanes.get(i))) {
 				temp = createMoveFromNeighbours(availableLanes.get(i), neighbourChoiceMethod);
 				if (temp.getB().equals(Integer.MAX_VALUE)) {
 					logger.debug("[" + getID() + "] Agent " + getName() + " found a safe move in lane " + availableLanes.get(i)); 
 				}
 				else {
 					logger.debug("[" + getID() + "] Agent " + getName() + " couldn't find a safe move in lane " + availableLanes.get(i));
 				}
 				actions.add(new Pair<CellMove,Integer>(new CellMove((availableLanes.get(i)-myLoc.getLane()), (int)temp.getA().getY()), temp.getB()));
 			}
 			else {
 				// skip, not a valid lane
 			}
 		}
 //		if (temp==null) {
 //			logger.warn("[" + getID() + "] Agent " + getName() + " doesn't think there is a safe move to make ! Decelerating as much as possible...");
 //			return driver.decelerateMax();
 //		}
 //		logger.error("You should never get here (deadcode), but Eclipse is insisting on a return here, so let's pass out a null to throw some exceptions later :D");
 		return chooseFromSafeMoves(actions, ownChoiceMethod);
 	}
 
 	/**
 	 * @param actions list of safe actions to make
 	 * @param ownChoiceMethod Should be OwnChoiceMethod.SAFE, OwnChoiceMethod.PLANNED
 	 * @return 
 	 */
 	private CellMove chooseFromSafeMoves(LinkedList<Pair<CellMove, Integer>> actions, OwnChoiceMethod ownChoiceMethod) {
 		Pair<CellMove, Integer> result;
 		if (actions.isEmpty()) {
 			logger.error("[" + getID() + "] Agent " + getName() + " couldn't find any moves at all ! Totally shouldn't be here, so slowing as much as possible.");
 			return driver.decelerateToCrawl();
 		}
 		else {
 			switch (ownChoiceMethod) {
 			case SAFE :  {
 				logger.trace("[" + getID() + "] Agent " + getName() + " choosing a safe action...");
 				Collections.sort(actions, new PairBDescComparator<Integer>());
 				result = actions.getFirst();
 				if (result.getB().equals(Integer.MAX_VALUE)) {
 					logger.debug("[" + getID() + "] Agent " + getName() + " attempting safe move: " + result.getA());
 					if (result.getA().getX()!=0) {
 						logger.debug("Agent is going to change lanes.");
 					}
 				}
 				else {
 					logger.warn("[" + getID() + "] Agent " + getName() + " doesn't think there is a safe move to make ! Decelerating as much as possible...");
 					if (result.getA().getX()!=0) {
 						logger.debug("Agent is going to change lanes.");
 					}
 				}
 				return result.getA();
 			}
 			case PLANNED : {
 				//TODO FIXME do this :P
 			}
 			default : {
 				logger.error("[" + getID() + "] Agent " + getName() + " tried to choose a " + ownChoiceMethod.toString() + " which doesn't exist, so slowing as much as possible.");
 				return driver.decelerateToCrawl();
 			}
 			}
 		}
 	}
 	
 	/**
 	 * @param neighbourChoiceMethod TODO
 	 * @return a reasoned move/viability pair. Viability is Integer.Max_VALUE if the move is safe, or not if otherwise.
 	 */
 	private Pair<CellMove,Integer> createMoveFromNeighbours(int lane, NeighbourChoiceMethod neighbourChoiceMethod) {
 		switch (neighbourChoiceMethod) {
 		case WORSTCASE : return worstCaseNeighbourChoice(lane);
 		case GOALS : // FIXME TODO
 		case INSTITUTIONAL : // FIXME TODO
 		default : {
 			logger.error("[" + getID() + "] Agent " + getName() + " tried to predict neighbour moves by " + neighbourChoiceMethod.toString() + " which doesn't exist.");
 			return new Pair<CellMove, Integer>(driver.decelerateToCrawl(), 0);
 		}
 		}
 	}
 	
 	/**
 	 * @return a reasoned move/viability pair. Viability is Integer.Max_VALUE if the move is safe, or not if otherwise.
 	 */
 	private Pair<CellMove,Integer> worstCaseNeighbourChoice(int lane) {
 		int newSpeed;
 		// this doesn't really show if something isn't safe, but it does show if something is safe
 		// (ie, only true if you know you can go at your preferred speed instead of one for safety's sake.
 		boolean safe = false;
 		Integer reqStopDist = null; // init to null incase we get to needing it and it hasnt been assigned..
 		
 		logger.debug("[" + getID() + "] Agent " + getName() + " is checking lane " + lane + " for a valid move");
 		// get agent in front
 		UUID target = this.locationService.getAgentToFront(lane);
 		// if there is someone there
 		if (target!=null) {
 			// get agent in front's stopping distance
 			logger.debug("[" + getID() + "] Agent " + getName() + " saw agent " + target + " at " + (RoadLocation)locationService.getAgentLocation(target));
 			int targetStopDist = speedService.getStoppingDistance(target);
 			logger.debug("[" + getID() + "] Agent " + getName() + " thinks that target's stopping distance is " + targetStopDist);
 			// add the distance between you and their current location (then minus 1 to make sure you can stop BEFORE them)
 			reqStopDist = targetStopDist + (locationService.getOffsetDistanceBetween(myLoc, (RoadLocation)locationService.getAgentLocation(target)))-1;
 			logger.debug("[" + getID() + "] Agent " + getName() + " got a reqStopDist of " + reqStopDist
 					+ " ( distanceBetween(" + myLoc + "," + (RoadLocation)locationService.getAgentLocation(target) +")= " + (locationService.getOffsetDistanceBetween(myLoc, (RoadLocation)locationService.getAgentLocation(target))) + ") ");
 			// work out what speed you can be at to stop in time
 			int stoppingSpeed = speedService.getSpeedToStopInDistance(reqStopDist);
 			logger.debug("[" + getID() + "] Agent " + getName() + " thinks they need to travel at " + stoppingSpeed + " to stop in " + reqStopDist);
 			if ( (stoppingSpeed <0) || ( (mySpeed>stoppingSpeed) && (mySpeed-speedService.getMaxDecel() > stoppingSpeed) ) ) {
 				logger.debug("[" + getID() + "] Agent " + getName() + " doesn't think they can stop in time in lane " + lane);
 				return new Pair<CellMove,Integer>(driver.decelerateMax(), reqStopDist);
 			}
 			// if this is more than your preferred speed, aim to go at your preferred speed instead
 			if (stoppingSpeed > goals.getSpeed()) {
 				newSpeed = goals.getSpeed();
 				logger.debug("[" + getID() + "] Agent " + getName() + " deciding to move at preferred speed of " + newSpeed);
 				safe = true;
 			}// otherwise, aim to go at that speed
 			else {
 				newSpeed = stoppingSpeed; 
 				logger.debug("[" + getID() + "] Agent " + getName() + " deciding to move at safe speed of " + newSpeed);
 			}
 		}
 		// if there isn't anyone there, aim to go at your preferred speed
 		else {
 			newSpeed = goals.getSpeed();
 			logger.debug("[" + getID() + "] Agent " + getName() + " deciding to move at preferred speed of " + newSpeed);
 			safe = true;
 		}
 		// get the difference between it and your current speed
 		int speedDelta = mySpeed-newSpeed;
 		// if there isn't a difference, chill
 		if (speedDelta == 0) {
 			logger.debug("[" + getID() + "] Agent " + getName() + " attempting move at constant speed of " + newSpeed);
 			return new Pair<CellMove,Integer>(driver.constantSpeed(), Integer.MAX_VALUE);
 		}
 		// if it's greater than your current speed, accelerate
 		else if (speedDelta < 0) {
 			// you know which you're in, so now abs() it...
 			speedDelta = Math.abs(speedDelta);
 			// if you're at maxSpeed, don't try and speed up...
 			if (mySpeed == speedService.getMaxSpeed()) {
 				return new Pair<CellMove,Integer>(driver.constantSpeed(), Integer.MAX_VALUE);
 			}
 			else {
 				// work out if you can change to that speed now
 				if (speedDelta <= speedService.getMaxAccel()) {
 					// if you can, do so
 					if (mySpeed+speedDelta > speedService.getMaxSpeed()) {
 						logger.debug("[" + getID() + "] Agent " + getName() + " adjusted acceleration from " + speedDelta + " to move at maxSpeed.");
 						return new Pair<CellMove,Integer>(driver.moveAt(speedService.getMaxSpeed()), Integer.MAX_VALUE);
 					}
 					else {
 						logger.debug("[" + getID() + "] Agent " + getName() + " attempting to accelerate by " + speedDelta);
 						return new Pair<CellMove,Integer>(driver.accelerate(speedDelta), Integer.MAX_VALUE);
 					}
 				}
 				else {
 					// if not, just accel as much as you can, and you'll make it up
 					if (mySpeed+speedService.getMaxAccel() > speedService.getMaxSpeed()) {
 						logger.debug("[" + getID() + "] Agent " + getName() + " adjusted acceleration from maxAccel to move at maxSpeed.");
 						return new Pair<CellMove,Integer>(driver.moveAt(speedService.getMaxSpeed()), Integer.MAX_VALUE);
 					}
 					else {
 						logger.debug("[" + getID() + "] Agent " + getName() + " attempting to accelerate as much as possible to meet speedDelta of " + speedDelta);
 						return new Pair<CellMove,Integer>(driver.accelerateMax(), Integer.MAX_VALUE);
 					}
 				}
 			}
 		}
 		// if it's less than your current speed, decelerate
 		else {
 			// you know which you're in, so now abs() it...
 			speedDelta = Math.abs(speedDelta);
 			// if your current speed is 0, then don't even try attempting to decelerate...
 			if (mySpeed == 0) {
 				logger.debug("[" + getID() + "] Agent " + getName() + " is at zero already...");
 				return new Pair<CellMove,Integer>(driver.constantSpeed(), Integer.MAX_VALUE);
 			}
 			else {
 				// work out if you can change to that speed now
 				if (speedDelta <= speedService.getMaxDecel()) {
 					// if you can, do so (checking to make sure it won't take you below 0)
 					int temp = mySpeed-speedDelta;
 					if (temp<=0) {
 						// if you're going to go below 0, then set your decel to hit 0
 						logger.debug("[" + getID() + "] Agent " + getName() + " adjusting decel from " + speedDelta + " to move at zero.");
 						return new Pair<CellMove,Integer>(driver.moveAt(0), Integer.MAX_VALUE);
 					}
 					else {
 						logger.debug("[" + getID() + "] Agent " + getName() + " attempting to decelerate by " + speedDelta);
 						return new Pair<CellMove,Integer>(driver.decelerate(speedDelta), Integer.MAX_VALUE);
 					}
 				}
 				else {
 					// if not, PANIC ! (just decel max and hope for the best ? maybe change lanes...)
 					if (mySpeed-speedService.getMaxDecel() < 0){
 						logger.debug("[" + getID() + "] Agent " + getName() + " would decelMAx but adjusted to move at zero.");
 						return new Pair<CellMove,Integer>(driver.moveAt(0), Integer.MAX_VALUE);
 					}
 					else {
 						// If you *know* you're safe, then chill.
 						if (!safe) {
 							logger.debug("[" + getID() + "] Agent " + getName() + " doesn't think they can meet speedDelta of " + speedDelta);
 							// not convince we'll get here...
 							return new Pair<CellMove,Integer>(driver.decelerateMax(), reqStopDist);
 						}
 						else {
 							logger.debug("[" + getID() + "] Agent " + getName() + " doesn't think they can meet speedDelta of " + speedDelta + " so decellerating as much as they can");
 							return new Pair<CellMove,Integer>(driver.decelerateMax(), Integer.MAX_VALUE);
 						}
 					}
 				}
 			}
 		}
 		
 		//return this.driver.randomValid();
 	}
 
 	/**
 	 * @param move
 	 */
 	private void submitMove(CellMove move) {
 		// submit move action to the environment.
 		try {
 			logger.debug("[" + getID() + "] Agent " + getName() + " attempting move: " + move);
 			environment.act(move, getID(), authkey);
 		} catch (ActionHandlingException e) {
 			logger.warn("Error trying to move", e);
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void saveDataToDB() {
 		// get current simulation time
 		int time = SimTime.get().intValue();
 		// check db is available
 		if (this.persist != null) {
 			// save our location for this timestep
 			this.persist.getState(time).setProperty("location", this.myLoc.toString());
 			this.persist.getState(time).setProperty("speed", ((Integer)(this.mySpeed)).toString());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.presage2.util.participant.AbstractParticipant#processInput(uk.ac.imperial.presage2.core.messaging.Input)
 	 */
 	@Override
 	protected void processInput(Input arg0) {
 		// TODO Auto-generated method stub
 		logger.info("[" + getID() + "] Agent " + getName() + " not processing input: " + arg0.toString());
 	}
 
 }
