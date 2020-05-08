 package definition;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import exceptions.IllegalActionException;
 import logic.Elevator;
 import logic.ElevatorAction;
 import logic.Movement;
 
 /**
  * Abstract class which provides a skeleton of an algorithm. Each algorithm runs
  * as a separate thread and is typically controlled by an object which
  * implements {@link Controller}
  * 
  * @author BFH-Boys
  */
 public abstract class Algorithm implements Runnable {
 
 	private Building building;
 	private Controller controller;
 	private boolean isRunning;
 	protected boolean isEnded = false;
 	
 	static Logger log4j = Logger.getLogger("ch.bfh.proj1.elevator");
 
 	/**
 	 * 
 	 * @param building
 	 *            Building (with elevators)
 	 */
 	public Algorithm(Building building, Controller controller) {
 		this.building = building;
 		this.controller = controller;
 		log4j.debug("Constructor call!");
 	}
 
 	/**
 	 * @return controller assigned to this algorithm
 	 */
 	protected Controller getController() {
 		return controller;
 	}
 
 	/**
 	 * @return The building currently assigned to the algorithm
 	 */
 	public Building getBuilding() {
 		return building;
 	}
 
 	/**
 	 * Stops the algorithm from processing further actions and stops the thread.
 	 */
 	public void stop() {
 		this.isRunning = false;
 	}
 
 	/**
 	 * @return The current state of the algorithm
 	 */
 	public boolean isRunning() {
 		return isRunning;
 	}
 	
 	public boolean isEnded() {
 		return isEnded;
 	}
 
 	/**
 	 * @param isRunning
 	 *            sets the current state
 	 */
 	protected void setRunning(boolean isRun) {
 		this.isRunning = isRun;
 	}
 	
 	/**
 	 * If the allocated Action @a has more than the MaxPeople limit of
 	 * the elevator @e, we generate a new action with the superfluous peolple
 	 * @param e the Elevator witch perform the action
 	 * @param a the Action to perform
 	 */
 	protected void checkCombination(VerticalTransporter e,Action a){
		
 		if(e.getMaxPeople() < a.getPeopleAmount()){
 			int pepoleLeft  = a.getPeopleAmount()-e.getMaxPeople();
 			a.setPeopleAmount(a.getPeopleAmount()-pepoleLeft);
 			try {
 				controller.performAction(new ElevatorAction(a.getStartLevel(), a.getEndLevel(), pepoleLeft));
 			} catch (IllegalActionException e1) {				
 				e1.printStackTrace();
 			}
 		}
 		
 	}
 
 	/***
 	 *  the base algorithm doesn't do not thing at this point but this method can be overloaded.	
 	 * @param movement
 	 * @param elevator
 	 */
 	public List<Action> getActions(int currentLevel, int minmaxLevel, int i) {
 		return null;
 	}
 
 }
