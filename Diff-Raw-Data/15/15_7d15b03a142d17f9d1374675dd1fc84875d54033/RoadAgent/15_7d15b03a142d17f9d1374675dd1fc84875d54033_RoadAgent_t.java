 /**
  * See http://www.presage2.info/ for more details on Presage2
  */
 package uk.ac.imperial.dws04;
 
 import java.util.Set;
 import java.util.UUID;
 
 import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.messaging.Input;
 import uk.ac.imperial.presage2.core.simulator.SimTime;
 import uk.ac.imperial.presage2.util.location.CellMove;
 import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
 
 /**
  * @author dws04
  * 
  */
 public class RoadAgent extends AbstractParticipant {
 
 	/**
 	 * @author dws04
 	 *
 	 */
 	protected enum MoveType { CONSTANT, LEFT, RIGHT, ACCEL, ACCELMAX, DECEL, DECELMAX, RANDOM	}
 
 	protected Driver driver;
 	protected RoadLocation myLoc;
 	protected int mySpeed;
 	protected final RoadAgentGoals goals;
 	
 	// Variable to store the location service.
 	ParticipantRoadLocationService locationService;
 	ParticipantSpeedService speedService;
 	/*RoadEnvironmentService environmentService;*/
 	 
 	RoadAgent(UUID id, String name, RoadLocation myLoc, int mySpeed, RoadAgentGoals goals) {
 		super(id, name);
 		this.myLoc = myLoc;
 		this.mySpeed = mySpeed;
 		this.goals = goals;
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
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		/*// get the RoadEnvironmentService.
 		try {
 			this.environmentService = getEnvironmentService(RoadEnvironmentService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 		}*/
 	}
 	
 	@Override
 	public void execute() {
 		myLoc = (RoadLocation) locationService.getAgentLocation(getID());
 		mySpeed = speedService.getAgentSpeed(getID());
 	 
 		logger.info("My location is: "+ this.myLoc + " and my speed is " + this.mySpeed);
 		logger.info("I can see the following agents:" + locationService.getNearbyAgents());
 		saveDataToDB();
 	 
 		CellMove move = createMove();
 	 
 		submitMove(move);
 	}
 	
 	/**
 	 * @return a reasoned move
 	 */
 	private CellMove createMove() {
 		int newSpeed;
 		
 		// get agent in front
 		UUID target = this.locationService.getAgentToFront(myLoc.getLane());
 		// if there is someone there
 		if (target!=null) {
 			// get agent in front's stopping distance
 			int targetStopDist = speedService.getConservativeStoppingDistance(target);
 			// work out what speed you can be at to stop in time
 			int stoppingSpeed = speedService.getSpeedToStopInDistance(targetStopDist);
 			// if this is more than your preferred speed, aim to go at your preferred speed instead
 			if (stoppingSpeed > goals.getSpeed()) {
 				newSpeed = goals.getSpeed();
 				logger.debug("Agent " + getName() + " deciding to move at preferred speed of " + newSpeed);
 			}// otherwise, aim to go at that speed
 			else {
 				newSpeed = stoppingSpeed; 
 				logger.debug("Agent " + getName() + " deciding to move at safe speed of " + newSpeed);
 			}
 		}
 		// if there isn't anyone there, aim to go at your preferred speed
 		else {
 			newSpeed = goals.getSpeed();
 			logger.debug("Agent " + getName() + " deciding to move at preferred speed of " + newSpeed);
 		}
 		// if newSpeed is negative then it's not possible for you to move at a safe speed, so move out of lane
 		// TODO 
 		// get the difference between it and your current speed
 		int speedDelta = mySpeed-newSpeed;
 		// if there isn't a difference, chill
 		if (speedDelta == 0) {
 			logger.debug("Agent " + getName() + " attempting move at constant speed of " + newSpeed);
 			return driver.constantSpeed();
 		}
 		// if it's greater than your current speed, accelerate
 		else if (speedDelta < 0) {
 			// work out if you can change to that speed now
 			if (speedDelta < speedService.getMaxAccel()) {
 				// if you can, do so
 				logger.debug("Agent " + getName() + " attempting to accelerate by " + Math.abs(speedDelta));
				return driver.accelerate(Math.abs(speedDelta));
 			}
 			else {
 				// if not, just accel as much as you can, and you'll make it up
 				logger.debug("Agent " + getName() + " attempting to accelerate as much as possible to meet speedDelta of " + Math.abs(speedDelta));
 				return driver.accelerateMax();
 			}
 		}
 		// if it's less than your current speed, decelerate
 		else {
 			// work out if you can change to that speed now
 			if (speedDelta < speedService.getMaxDecel()) {
 				// if you can, do so
 				logger.debug("Agent " + getName() + " attempting to decelerate by " + Math.abs(speedDelta));
				return driver.decelerate(Math.abs(speedDelta));
 			}
 			else {
 				// if not, PANIC ! (just decel max and hope for the best ? maybe change lanes...)
 				logger.debug("Agent " + getName() + " attempting to decelerate as much as possible to meet speedDelta of " + Math.abs(speedDelta));
 				return driver.decelerateMax();
 			}
 		}
 		
 		//return this.driver.randomValid();
 	}
 
 	/**
 	 * @return a random move
 	 */
 	private CellMove randomMove() {
 		return this.driver.random();
 	}
 	
 	/**
 	 * @return a move
 	 */
 	private CellMove explicitMove(MoveType type, int n) {
 		switch (type) {
 		case CONSTANT : return driver.constantSpeed();
 		case ACCEL : return driver.accelerate(n);
 		case ACCELMAX : return driver.accelerateMax();
 		case DECEL : return driver.decelerate(n);
 		case DECELMAX : return driver.decelerateMax();
 		case LEFT : return driver.changeLaneLeft();
 		case RIGHT : return driver.changeLaneRight();
 		case RANDOM : return driver.randomValid();
 		default : return driver.random();
 		}
 	}
 
 	/**
 	 * @param move
 	 */
 	private void submitMove(CellMove move) {
 		// submit move action to the environment.
 		try {
 			logger.debug("Agent " + getName() + " attempting move: " + move);
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
 		logger.info("Agent " + this.getName() + " processing input: " + arg0.toString());
 	}
 
 }
