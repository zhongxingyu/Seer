 package mitzi;
 
 import java.util.Collections;
 import java.util.HashSet;
 
 public class Variation implements Comparable<Variation> {
 
 	/**
 	 * The fist move of the variation. May be set to null. Usually set to null
 	 * in a Variation tree.
 	 */
 	private IMove move;
 
 	/**
 	 * The value of the variation. I. e. the value of the last board of the
 	 * principal variation given as seen from white's position.
 	 */
 	private int value;
 
 	/**
 	 * Whose turn is it at this move.
 	 */
 	private Side to_move;
 
 	/**
 	 * A set of Variations possible after the move.
 	 * 
 	 * Note that this will (and should) usually not span the complete tree under
 	 * the current game position. Rather it can be used to save certain
 	 * interesting subtrees for deeper evaluation and for debugging.
 	 */
 	private HashSet<Variation> sub_variations = new HashSet<Variation>();
 
 	/**
 	 * Create a new Variation
 	 * 
 	 * @param move
 	 *            the IMove to be done by Side to_move
 	 * @param value
 	 *            the value of the principal variation after move
 	 * @param to_move
 	 *            the Side whose turn it is
 	 */
 	Variation(IMove move, int value, Side to_move) {
 		this.move = move;
 		this.value = value;
 		this.to_move = to_move;
 	}
 
 	/**
 	 * Use with caution e. g. when backpropagating
 	 * 
 	 * @param move
 	 *            the move to be set
 	 * @param value
 	 *            the value of the (principal) variation
 	 */
 	public void update(IMove move, int value) {
 		this.move = move;
 		this.value = value;
 	}
 
 	/**
 	 * @return the value of the (principal) variation
 	 */
 	public int getValue() {
 		return value;
 	}
 
 	/**
 	 * 
 	 * @return the first move of the variation
 	 */
 	public IMove getMove() {
 		return move;
 	}
 
 	/**
 	 * 
 	 * @return the Side to move
 	 */
 	public Side getSideToMove() {
 		return to_move;
 	}
 
 	/**
 	 * Gives the stored Set of Variations after this move.
 	 * 
 	 * NB: this is not a copy, but the real stuff :)
 	 * 
 	 * @return the Variations after this Variation's IMove
 	 */
 	public HashSet<Variation> getSubVariations() {
 		return sub_variations;
 	}
 
 	/**
 	 * Adds a subvariation to this Variation.
 	 * 
 	 * @param variation
 	 *            the Variation to be added
 	 */
 	public void addSubVariation(Variation variation) {
 		sub_variations.add(variation);
 	}
 
 	/**
 	 * Deletes all but the best and worst subvariation.
 	 * 
 	 * If you want to clear subvariations recursively aswell, use
 	 * {@link mitzi#clearSubMovesMinMaxRecursive() clearSubMovesMinMaxRecursive}
 	 * instead.
 	 */
 	public void clearSubMovesMinMax() {
 		if (sub_variations.isEmpty())
 			return;
 
 		Variation min = Collections.min(sub_variations);
 		Variation max = Collections.max(sub_variations);
 		HashSet<Variation> new_sub_variations = new HashSet<Variation>();
 		new_sub_variations.add(min);
 		new_sub_variations.add(max);
 		sub_variations = new_sub_variations;
 	}
 
 	/**
 	 * Deletes all but the best and worst subvariation recursively in all
 	 * subvariations.
 	 * 
 	 * If you want to clear only this Variation, use
 	 * {@link mitzi#clearSubMovesMinMax() clearSubMovesMinMax} instead.
 	 */
 	public void clearSubMovesMinMaxRecursively() {
 		clearSubMovesMinMax();
 		for (Variation variation : sub_variations) {
 			variation.clearSubMovesMinMaxRecursively();
 		}
 	}
 
 	/**
 	 * Gives the principal variation. Basically a minimax search.
 	 * 
 	 * @return a linear Variation containing only the IMoves of the principal
 	 *         variation
 	 */
 	public Variation getPrincipalVariation() {
 		return getPrincipalVariation(this);
 	}
 
 	/**
 	 * This function searches for the principal variation in a tree, using
 	 * minimax algorithm.
 	 * 
 	 * @param search_tree
 	 *            the Variations, where the principal Variation has to be found.
 	 * @return the principal Variation
 	 */
 	private static Variation getPrincipalVariation(Variation search_tree) {
 
 		// search minimax preferred subvariation
 
 		// Note, if this algorithm is called by white, then the root has side
 		// Black and vice versa. Therefore white should be minimized and
 		// black maximized.
 		Variation preferred;
 		if (search_tree.to_move == Side.WHITE) {
 			preferred = Collections.min(search_tree.sub_variations);
 		} else {
 			preferred = Collections.max(search_tree.sub_variations);
 		}
 
 		Variation pv = new Variation(preferred.move, preferred.value,
 				preferred.to_move);
 
 		// search one level down
 		if (!preferred.sub_variations.isEmpty())
 			pv.addSubVariation(getPrincipalVariation(preferred));
 
 		return pv;
 	}
 
 	/**
 	 * 
 	 * @return the best move of the actual variation
 	 */
 	public IMove getBestMove() {
 		Variation preferred;
 		// Note, if this function is called by white, the root has side
 		// Black and vice versa. Therefore white should be minimized and
 		// black maximized.
 		if (to_move == Side.WHITE) {
 			preferred = Collections.min(sub_variations);
 		} else {
 			preferred = Collections.max(sub_variations);
 		}
 		return preferred.move;
 	}
 
 	/**
 	 * Compares two Variation objects by their value.
 	 * 
 	 * @param anotherVariation
 	 *            the Variation to be compared.
 	 * 
 	 * @return the value 0 if this Variation is equal to the argument Variation;
 	 *         a value less than 0 if this Variation is in value less than the
 	 *         argument Variation; and a value greater than 0 if this Variation
 	 *         is in value greater than the argument Variation (signed
 	 *         comparison).
 	 */
 	@Override
 	public int compareTo(Variation anotherVariation) {
		return Integer.compare(anotherVariation.getValue(), this.getValue());
 	}
 
 	/**
 	 * String representation of a Variation tree. Must be compatible with UCI
 	 * principal variation output!
 	 * 
 	 * @return a String representing the Variation
 	 */
 	@Override
 	public String toString() {
 		StringBuilder str = new StringBuilder("");
 
 		if (sub_variations.isEmpty()) {
 			// base case
 			str.append(move + " ");
 		} else {
 			if (move != null) {
 				str.append(move + " ");
 			}
 			if (sub_variations.size() != 1) {
 				str.append("( ");
 			}
 			int i = 0;
 			for (Variation var : sub_variations) {
 				if (i > 0) {
 					str.append("| ");
 				}
 				str.append(var);
 				i++;
 
 			}
 			if (sub_variations.size() != 1) {
 				str.append(")");
 			}
 		}
 
 		return str.toString();
 	}
 }
