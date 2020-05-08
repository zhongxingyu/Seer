 package logic;
 
 import java.lang.reflect.Constructor;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import definition.Action;
 import definition.ActionObserver;
 import definition.Algorithm;
 import definition.Building;
 import definition.Controller;
 import definition.HorizontalTransporter;
 import exceptions.IllegalActionException;
 import exceptions.MaxLevelActionException;
 import exceptions.MinLevelActionException;
 
 public class ElevatorController implements Controller {
 
 	// Logger
 	static Logger log4j = Logger.getLogger("ch.bfh.proj1.elevator");
 
 	// Building attached to the controller
 	private Building building;
 	private Algorithm algorithm;
 	private List<Action> actions;
 
 	/**
 	 * Constructor for the controller. Each controller is responsible for
 	 * exactly one building.
 	 * 
 	 * @param building
 	 *            The attached building
 	 */
 	public ElevatorController(Building building,
 			Class<? extends Algorithm> clazz) throws Exception {
 		super();
 		this.actions = new LinkedList<Action>();
 		this.building = building;
 
 		// Create new instance of algorithm using reflections
 		// to ensure that each controller always has a algorithm
 		// which processes the data
 		Class[] args = new Class[] { Building.class ,Controller.class };
 		Constructor con = clazz.getConstructor(args);
 
 		this.algorithm = (Algorithm) con.newInstance(building, this);
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
 		action.setTimestampCreated(new Date());
 
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
 
 	}
 
 	/**
 	 * Adds an actionListener to all elevators
 	 */
 	public void addActionObserver(ActionObserver observer) {
 		for (HorizontalTransporter elevator : building.getElevators()) {
 			elevator.addActionObserver(observer);
 		}
 
 	}
 
 	/**
 	 * Removes an actionListener from all elevators
 	 */
 	public void deleteActionObserver(ActionObserver observer) {
 		for (HorizontalTransporter elevator : building.getElevators()) {
 			elevator.deleteActionObserver(observer);
 		}
 
 	}
 
<<<<<<< .mine
 	@Override
 	public void performActions(List<Action> actions)
 			throws IllegalActionException {
 		for(Action a : actions){
 			performAction(a);
 		}		
 	}
 
=======
 	/**
 	 * 
 	 */
 	@Override
 	public Action getActionWithHighestPriority() {
 		if (actions.isEmpty()) {
 			return null;
 		}
 		return actions.remove(0);
 	}
 
 	@Override
 	public List<Action> getActions(int startLevel, int endlevel, int maxPerson) {
 		List<Action> actionsOfLevel = new LinkedList<Action>();
 		boolean up = startLevel < endlevel;
 		int remainingCapacity = maxPerson;
 
 		Action splitedAction = null;
 		synchronized (actionsOfLevel) {
 			for (Action action : actions) {
 				// break if the elevator is full
 				if (remainingCapacity == 0) {
 					break;
 				}
 				// ensure that action comes from the same startLevel
 				if (action.getStartLevel() == startLevel) {
 					// ensure that action goes in the same directory as the
 					// elevator
 					if ((action.getStartLevel() < action.getEndLevel()) == up) {
 						// ensure that the elevator is not overloaded
 						if (action.getPeopleAmount() > remainingCapacity) {
 							// Split action so that only a few people move
 							splitedAction = new ElevatorAction(
 									action.getStartLevel(),
 									action.getEndLevel(),
 									action.getPeopleAmount()
 											- remainingCapacity);
 							action.setPeopleAmount(remainingCapacity);
 						}
 						// Descend the free spaces in the elevator
 						remainingCapacity -= action.getPeopleAmount();
 						actionsOfLevel.add(action);
 					}
 				}
 			}
 			// Remove actions which are processed
 			this.actions.removeAll(actionsOfLevel);
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

>>>>>>> .r35
 }
