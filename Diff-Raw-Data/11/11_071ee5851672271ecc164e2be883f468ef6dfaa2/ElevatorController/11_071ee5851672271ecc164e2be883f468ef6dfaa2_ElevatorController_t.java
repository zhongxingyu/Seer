 package logic;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import logic.algorithm.BetterFiFoAlgorithm;
 import logic.algorithm.BetterPickupFifoAlgorithm;
 import logic.algorithm.EveryLevelPickUpAlgorithm;
 import logic.algorithm.FiFoAlgorithm;
 import logic.algorithm.PickUpFifoAlgorithm;
 
 import org.apache.log4j.Logger;
 
 import definition.Action;
 import definition.ActionObserver;
 import definition.Algorithm;
 import definition.Building;
 import definition.Controller;
 import definition.VerticalTransporter;
 import exceptions.IllegalActionException;
 import exceptions.MaxLevelActionException;
 import exceptions.MinLevelActionException;
 
 public class ElevatorController implements Controller {
 
 	// Logger
 	static Logger log4j = Logger.getLogger("ch.bfh.proj1.elevator");
 
 	// Building attached to the controller2144398321443983
 	private Building building;
 	private Algorithm algorithm;
 	private List<Action> actions;
 	private List<Action> doneActions;
 	private Simulation simulation;
 	private int simulationSpeed = 1;
 
 	/**
 	 * Constructor for the controller. Each controller is responsible for
 	 * exactly one building.
 	 * 
 	 * @param building
 	 *            The attached building
 	 */
 	public ElevatorController(Building building, String algorithmName) {
 		super();
 		this.actions = new LinkedList<Action>();
 		this.doneActions = new ArrayList<Action>();
 		this.building = building;
 		this.simulation = new Simulation(this);
 		try {
 			changeAlgorithm(algorithmName);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void changeAlgorithm(String name) throws Exception {
 		
 		
 		this.algorithm = null;
 		if (algorithm != null){
 			this.algorithm.stop();
 		}
 
 		if (name.equalsIgnoreCase("PickUpFifoAlgorithm")) {
 			this.algorithm = new PickUpFifoAlgorithm(building, this);
 		} else if (name.equalsIgnoreCase("BetterPickupFifoAlgorithm")) {
 			this.algorithm = new BetterPickupFifoAlgorithm(building, this);
 		} else if (name.equalsIgnoreCase("BetterFifoAlgorithm")) {
 			this.algorithm = new BetterFiFoAlgorithm(building, this);
 		} else if (name.equalsIgnoreCase("EveryLevelPickUpAlgorithm")) {
 			this.algorithm = new EveryLevelPickUpAlgorithm(building, this);
 		} else if (name.equalsIgnoreCase("FiFoAlgorithm")) {
 			this.algorithm = new FiFoAlgorithm(building, this);
 		} else {
 			throw new Exception("Algorithm not known: " + name);
 		}
 	}
 
 	/**
 	 * 
 	 * @return The attached building
 	 */
 	public Building getBuilding() {
 		return building;
 	}
 
 	@Override
 	public void performAction(Action action) throws IllegalActionException {
 		if (action.getStartLevel() < building.getMinLevel()) {
 			throw new MinLevelActionException(action);
 		}
 		if (action.getEndLevel() > building.getMaxLevel()) {
 			throw new MaxLevelActionException(action);
 		}
 		// set the current timestamp to the action
 		action.setTimestampElevatorCalled(new Date());
 
 		action.addActionObserver(new ActionObserver() {
 
 			@Override
 			public void actionStarted(Action action) {
 				
 			}
 
 			@Override
 			public void actionPerformed(Action action) {
 				synchronized (doneActions) {
 					doneActions.add(action);
 				}
 			}
 
 			@Override
 			public void actionPeopleLoaded(Action action) {
 				
 
 			}
 		});
 
 		log4j.debug("Adding action " + action);
 
 		// add to datastructure
 		synchronized (actions) {
 			this.actions.add(action);
 		}
 	}
 
 	@Override
 	public void startController() {
 		Thread t = new Thread(algorithm);
 		t.start();
 
 	}
 
 	@Override
 	public void stopController() {
 		algorithm.stop();
 		log4j.debug("stopController call!");
 		while(!algorithm.isEnded()){
 			try {
 				Thread.sleep(200);
				log4j.debug("stopController waiting.....!");
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		log4j.debug(algorithm.isRunning());
 	}
 
 	@Override
 	public void performActions(List<Action> actions)
 			throws IllegalActionException {
 		for (Action a : actions) {
 			performAction(a);
 		}
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public Action getActionWithHighestPriority() {
 		synchronized (actions) {
 			if (actions.isEmpty()) {
 				return null;
 			}
 			return actions.remove(0);
 		}
 	}
 
 	@Override
 	public List<Action> getActions(int startLevel, int endlevel, int maxPerson) {
 		List<Action> actionsOfLevel = new LinkedList<Action>();
 		boolean up = startLevel < endlevel;
 		int remainingCapacity = maxPerson;
 
 		Action splitedAction = null;
 		synchronized (actions) {
 			for (Action action : actions) {
 				// break if the elevator is full
 				if (remainingCapacity < 1) {
 					break;
 				}
 				// ensure that action comes from the same startLevel
 				if (action.getStartLevel() == startLevel) {
 					// ensure that action goes in the same directory as the
 					// elevator
 					if ((action.getStartLevel() < action.getEndLevel()) == up) {
 						// Ensure elevator range
 						if ((action.getEndLevel() <= endlevel && up)
 								|| (action.getEndLevel() >= endlevel && !up)) {
 
 							// ensure that the elevator is not overloaded
 							if (action.getPeopleAmount() > remainingCapacity) {
 								log4j.debug("Remaining capacity"
 										+ remainingCapacity);
 								// Split action so that only a few people move
 								int splitedPeopleAmount = action
 										.getPeopleAmount() - remainingCapacity;
 								splitedAction = new ElevatorAction(
 										action.getStartLevel(),
 										action.getEndLevel(),
 										splitedPeopleAmount);
 								action.setPeopleAmount(remainingCapacity);
 							}
 							// Descend the free spaces in the elevator
 							remainingCapacity -= action.getPeopleAmount();
 							actionsOfLevel.add(action);
 						}
 					}
 				}
 			}
 			// Remove actions which are processed
 			synchronized (actions) {
 				this.actions.removeAll(actionsOfLevel);
 			}
 			
 		}
 		if (splitedAction != null) {
 			try {
 				this.performAction(splitedAction);
 			} catch (IllegalActionException e) {
 				log4j.error(e.getMessage(), e);
 			}
 		}
 		return actionsOfLevel;
 	}
 
 	/*
 	 * removes an elevator by its id
 	 */
 	public boolean removeElevator(int removeId) {
 		try {
 			building.removeElevator(building.getElevators().get(removeId));
 			return true;
 		} catch (Exception ex) {
 			return false;
 		}
 	}
 
 	@Override
 	public List<Action> getDoneActions() {
 		return doneActions;
 	}
 
 	@Override
 	public void addDoneAction(Action a) {
 		synchronized (doneActions) {
 			this.doneActions.add(a);
 		}	
 	}
 
 	@Override
 	public void addElevator(Elevator e) {
 		e.setSimulationSpeed(this.simulationSpeed);
 		building.addElevator(e);
 	}
 
 	@Override
 	public void setBuilding(Building b) {
 		building = b;
 	}
 
 	@Override
 	public void startRandomSimulation(int amount) {
 		simulation = new Simulation(this);
 		simulation.generateActions(amount);
 		simulation.start();
 	}
 
 	@Override
 	public void stopRandomSimulation() {
 		simulation.stopSimulation();
 		simulation.clearActions();
 
 	}
 
 	@Override
 	public void startSimulation(String path, int speed, List<Action> actions) {
 		try {
 			//stopController();
 			simulationSpeed = speed;
 			simulation = new Simulation(this);
 			simulation.setSimulationSpeed(this.simulationSpeed);
 			simulation.setPath(path);
 			building.setSimulationSpeed(simulationSpeed);
 			simulation.addAction(actions);
 			simulation.start();
 			//startController();
 		} catch (IllegalActionException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see definition.Controller#setSimulationSpeed(int) sets the speed only if
 	 * valid 0 < speed <= 100
 	 */
 	public void setSimulationSpeed(int speed) {
 
 		if (speed > 0 && speed <= 100) {
 			simulationSpeed = speed;
 			if (simulation != null) {
 				simulation.setSimulationSpeed(speed);
 			}
 			building.setSimulationSpeed(speed);
 		}
 
 	}
 
 	/*
 	 * Reset the statistics of each elevator
 	 */
 	public void resetLiftEvaluation() {
 		for (VerticalTransporter t : building.getElevators()) {
 			t.resetStatistics();
 		}
 	}
 
 	/*
 	 * Clears the list where all done actions are stored for evaluation
 	 */
 	public void resetDoneActions() {
 		synchronized (doneActions) {
 			doneActions.clear();		
 		}
 	}
 
 	/*
 	 * Clears all actions with aren't perfom yet
 	 */
 	public void resetActions() {
 		synchronized (actions) {
 			actions.clear();		
 		}
 	}
 
 	@Override
 	public Simulation getSimulation() {
 		return simulation;
 	}
 
 	@Override
 	public int getSimluationSpeed() {
 		return this.simulationSpeed;
 	}
 
 	@Override
 	public String getAlgorithmName() {
 		if (this.algorithm != null)
 			return algorithm.getClass().getSimpleName();
 		return "";
 	}
 
 	@Override
 	public int getTodoActionsAmount() {
 		synchronized (actions) {
 			return this.actions.size();
 				
 		}
 	}
 
 	@Override
 	public void setAlgorithmName(String name) {
 		stopController();
 		try {
 			changeAlgorithm(name);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		startController();
 	}
 
 	@Override
 	public Elevator getClosestFreeElevator(Action action) {
 		Elevator closestElevator = null;
 		for (VerticalTransporter i : getBuilding().getElevators()) {
 
 			Elevator ele = (Elevator) i;
 			// Look for a non-busy elevator with a fitting range
 			// (MinLevel & MaxLevel)
 			if (!i.isBusy() && ele.getMinLevel() <= action.getStartLevel()
 					&& ele.getMaxLevel() >= action.getStartLevel()
 					&& ele.getMinLevel() <= action.getEndLevel()
 					&& ele.getMaxLevel() >= action.getEndLevel()) {
 				if (closestElevator == null || Math.abs(ele.getCurrentLevel() - action.getStartLevel()) <= Math
 						.abs(ele.getCurrentLevel() - action.getStartLevel())) {
 					//ele.setBusy(true);
 					//return ele;
 					//closestElevator.setBusy(true);
 					closestElevator = ele;
 				}
 			}
 
 		}
 		if (closestElevator != null){
 			closestElevator.setBusy(true);
 		}
 		return closestElevator;
 	}
 
 	@Override
 	public boolean isBusy() {
 		for(VerticalTransporter v : building.getElevators()){
 			if(v.isBusy()) 
 				return true;
 		}
 		return false;
 	}
 }
