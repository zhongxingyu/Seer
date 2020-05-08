 package greedGame.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * A helper class that handles a number of dice. It keeps track of the
  * ScoringRules used and provides access to its functionality.
  */
 public class DiceHandler {
 
 	private final int numberOfDice = 6;
 
 	private List<Dice> dice;
 	private ScoringRules rules;
 
 	/**
 	 * Constructor.
 	 */
 	public DiceHandler() {
 		dice = new ArrayList<Dice>(numberOfDice);
 		rules = new BasicScoringRules();
 
 		// A single random object is used for all dice in the handler
 		Random rnd = new Random();
 
 		for (int i = 0; i < numberOfDice; i++) {
 			dice.add(new Dice(rnd));
 		}
 	}
 
 	/**
 	 * Rolls the dice. If all dice is reserved, all dice is rolled, otherwise
 	 * only the dice that is not reserved is rolled.
 	 */
 	public void rollDice() {
 
 		boolean isAllReserved = true;
 
 		// Check for any unreserved dice
 		for (Dice d : dice) {
 			if (d.getState() != DiceState.RESERVED) {
 				isAllReserved = false;
 				break;
 			}
 		}
 
 		for (Dice d : dice) {
			if (isAllReserved || d.getState() != DiceState.FREE)
 				d.roll();
 		}
 	}
 
 	/**
 	 * Gets the maximum amount of points that can be gotten from the currently
 	 * unreserved dice according to the scoring rules.
 	 * 
 	 * @return maximum number of points
 	 */
 	public int getMaxPoints() {
 		return rules.getMaxPoints(getUnreservedDice());
 	}
 
 	/**
 	 * Gets the best scoring combinations the currently selected dice can give,
 	 * according to the scoring rules
 	 * 
 	 * @return a <code>List</code> of <code>ScoringCombination</code>s found by
 	 *         the scoring rules
 	 */
 	public List<ScoringCombination> getScoringCombinations() {
 		return rules.getScoringCombinations(getSelectedDice());
 	}
 
 	/**
 	 * Changes the state of all currently selected dice to <code>RESERVED</code>
 	 */
 	public void reserveSelectedDice() {
 		for (Dice d : getSelectedDice()) {
 			d.setState(DiceState.RESERVED);
 		}
 	}
 
 	/**
 	 * Selects the passed in dice.
 	 * 
 	 * @param selDice
 	 *            the dice to select
 	 */
 	public void selectDice(Dice selDice) {
 		selDice.setState(DiceState.SELECTED);
 	}
 
 	/**
 	 * Unselects the passed in dice.
 	 * 
 	 * @param selDice
 	 *            the dice to free
 	 */
 	public void unselectDice(Dice selDice) {
 		selDice.setState(DiceState.FREE);
 	}
 
 	/**
 	 * Gets a list of all the currently selected dice. The returned dice must
 	 * not be altered directly.
 	 * 
 	 * @return a <code>List</code> of currently selected <code>Dice</code>
 	 */
 	public List<Dice> getSelectedDice() {
 
 		ArrayList<Dice> selectedDice = new ArrayList<Dice>(numberOfDice);
 		for (Dice d : dice) {
 			if (d.getState() == DiceState.SELECTED)
 				selectedDice.add(d);
 		}
 
 		return selectedDice;
 	}
 
 	/**
 	 * Gets a list of all dice that are <code>FREE</code> or
 	 * <code>SELECTED</code>. The returned dice must not be altered directly.
 	 * 
 	 * @return a <code>List</code> of <code>Dice</code> that are not
 	 *         <code>RESERVED</code>
 	 */
 	public List<Dice> getUnreservedDice() {
 
 		ArrayList<Dice> unreservedDice = new ArrayList<Dice>(numberOfDice);
 		for (Dice d : dice) {
 			if (d.getState() != DiceState.RESERVED)
 				unreservedDice.add(d);
 		}
 
 		return unreservedDice;
 	}
 
 	/**
 	 * Gets a list of all free dice. The returned dice must not be altered
 	 * directly.
 	 * 
 	 * @return a <code>List</code> of currently <code>FREE</code> dice
 	 */
 	public List<Dice> getFreeDice() {
 
 		ArrayList<Dice> freeDice = new ArrayList<Dice>(numberOfDice);
 		for (Dice d : dice) {
 			if (d.getState() == DiceState.FREE)
 				freeDice.add(d);
 		}
 
 		return freeDice;
 	}
 
 	/**
 	 * Gets all dice, no matter what their state is. The returned list must not
 	 * be altered. The dice in the list must not be altered directly.
 	 * 
 	 * @return all dice handled by the dice handler
 	 */
 	public List<Dice> getDice() {
 		return dice;
 	}
 
 	/**
 	 * Gets the scoring rules used by the dice handler to evaluate dice.
 	 * 
 	 * @return the <code>ScoringRules</code> used
 	 */
 	public ScoringRules getScoringRules() {
 		return rules;
 	}
 
 	/**
 	 * Reserves all dice, no matter what their current state is.
 	 */
 	public void reserveAllDice() {
 		for (Dice d : dice)
 			d.setState(DiceState.RESERVED);
 	}
 }
