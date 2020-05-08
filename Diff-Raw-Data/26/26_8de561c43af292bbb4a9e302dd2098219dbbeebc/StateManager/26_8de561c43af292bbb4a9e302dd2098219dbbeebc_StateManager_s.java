 package State;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 
 import Card.CardDeck;
 
 /**
  * A GameState Manager class
 * @author npyck
  */
 public class StateManager extends Observable {
 
 	private int currentStateCounter;
 	private GameState currentState;
 	private List<GameState> states;
 
 	public StateManager() {
 		currentStateCounter = -1;
 
 		states = new ArrayList<GameState>();
 	}
 
 	/**
 	 * Sets the current GameState according to the internal currentStateCounter
	 * and notifies all observers that the current GameState has changed.
 	 */
 	private void setCurrentState() {
 		currentState = states.get(currentStateCounter).clone();
 
 		setChanged();
 		notifyObservers();
 	}
 
 	/**
 	 * Initializes the StateManager using a new shuffled deck of cards and
 	 * notifies all observers that the current GameState has changed.
 	 */
 	public void init() {
 		states.clear();
 
 		CardDeck newDeck = new CardDeck();
 		newDeck.shuffle();
 
 		GameState newState = new GameState(newDeck);
 		states.add(newState);
 
 		currentStateCounter = 0;
 
 		setCurrentState();
 	}
 
 	/**
 	 * Adds a clone of the current GameState to the StateManager and notifies
 	 * all observers that the current GameState has changed. All previous
 	 * redoable GameStates are lost at this point
 	 */
 	public void addState() {
 		if (currentStateCounter < (states.size() - 1)) {
 			states = states.subList(0, currentStateCounter + 1);
 		}
 
 		states.add(currentState.clone());
 		currentStateCounter += 1;
 
 		setChanged();
 		notifyObservers();
 	}
 
 	/**
 	 * Determines whether the StateManager contains any GameState objects prior
 	 * to the current GameState.
 	 * 
 	 * @return <code>true</code> if it is possible to revert to the previous
 	 *         GameState, <code>false</code> if there are no GameStates prior to
 	 *         the current GameState.
 	 */
 	public boolean canUndo() {
 		return currentStateCounter > 0;
 	}
 
 	/**
 	 * Changes the current GameState to the preceding GameState and notifies all
 	 * observers that the current GameState has changed.
 	 */
 	public void undo() {
 		if (canUndo()) {
 			currentStateCounter -= 1;
 			setCurrentState();
 		}
 	}
 
 	/**
 	 * Determines whether the StateManager contains any GameState objects after
 	 * the current GameState.
 	 * 
 	 * @return <code>true</code> if it is possible to progress to the next
 	 *         GameState, <code>false</code> if there are no more GameStates
 	 *         after the current GameState.
 	 */
 	public boolean canRedo() {
 		return currentStateCounter < (states.size() - 1);
 	}
 
 	/**
 	 * Changes the current GameState to the GameState following the current
 	 * GameState and notifies all observers that the current GameState has
 	 * changed.
 	 */
 	public void redo() {
 		if (canRedo()) {
 			currentStateCounter += 1;
 			setCurrentState();
 		}
 	}
 
 	/**
 	 * Changes the current GameState to the initial GameState and notifies all
 	 * observers that the current GameState has changed.
 	 */
 	public void reset() {
 		if (currentStateCounter > 0) {
 			currentStateCounter = 0;
 			setCurrentState();
 		}
 	}
 
 	/**
 	 * @return The current GameState object.
 	 */
 	public GameState getCurrentState() {
 		return currentState;
 	}
 
 }
