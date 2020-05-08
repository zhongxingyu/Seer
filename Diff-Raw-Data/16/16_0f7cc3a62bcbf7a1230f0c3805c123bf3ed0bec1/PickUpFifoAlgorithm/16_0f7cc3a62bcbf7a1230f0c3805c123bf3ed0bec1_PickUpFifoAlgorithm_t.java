 package logic.algorithm;
 
 import java.util.List;
 
 import logic.Elevator;
 
 import org.apache.log4j.Logger;
 
 import definition.Action;
 import definition.Algorithm;
 import definition.Building;
 import definition.Controller;
 import definition.Direction;
 import definition.VerticalTransporter;
 
 /**
  * This algorithm is similar to the FifoAlgorithm, with the slight difference
  * that people with the same start level can enter the elevator as well. So
  * people who arrive late in a floor can profit from a person who pressed the
  * elevator button earlier. 
  * 
  * @author krigu
  * 
  */
 public class PickUpFifoAlgorithm extends Algorithm {
 	
 	// Logger
 	static Logger log4j = Logger.getLogger("ch.bfh.proj1.elevator");
 
 	public PickUpFifoAlgorithm(Building building, Controller controller) {
 		super(building, controller);
 	}
 
 	/**
 	 * Starts the processing of the actions
 	 */
 	@Override
 	public void run() {
 		setRunning(true);
 		while (isRunning()) {
 			Action action = getController().getActionWithHighestPriority();
 			while (action != null) {
 				for (VerticalTransporter i : getBuilding().getElevators()) {
 
 					Elevator ele = (Elevator) i;
 					// Look for a non-busy elevator with a fitting range
 					// (MinLevel & MaxLevel)
 					if (!i.isBusy()
 							&& ele.getMinLevel() <= action.getStartLevel()
 							&& ele.getMaxLevel() >= action.getStartLevel()
 							&& ele.getMinLevel() <= action.getEndLevel()
 							&& ele.getMaxLevel() >= action.getEndLevel()) {
 						Direction dir = Direction.UP;
 						if (action.getStartLevel() > action.getEndLevel()) {
 							dir = Direction.DOWN;
 						}
 						List<Action> acts = getController().getActions(
 								action.getStartLevel(), action.getEndLevel(),
								ele.getMaxPeople()-action.getPeopleAmount());
 						acts.add(action);
 						log4j.debug("Pickup Action Size" + acts.size());
 						
 						ele.move(acts, action.getStartLevel(), dir);
 
 						action = null;
 						break;
 					}
 				}
 				// No elevator is free.. wait
 				hold();
 			}
 			// No action is available.. wait
 			hold();
 		}
 
 	}
 
 	/**
 	 * Waits for 100 milliseconds to continue
 	 */
 	private void hold() {
 		try {
 			Thread.sleep(100);
 		} catch (InterruptedException e) {
 		}
 	}
 
 }
