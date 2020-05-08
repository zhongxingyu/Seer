 package rftg.game.cards;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 import rftg.game.Constants;
 
 public class DesignPile {
 
 	protected List<Design> pile;
 
 	public DesignPile() {
 		pile = new ArrayList<Design>(Constants.MAX_DESIGN);
 	}
 
 	public synchronized Iterator<Design> iterator() {
 		return pile.iterator();
 	}
 
	public synchronized void load(Design[] designs) {
		if (designs != null && designs.length > 0) {
 			pile.clear();
			pile = Arrays.asList(designs);
 		}
 	}
 
 	public synchronized void shuffle() {
 		Collections.shuffle(pile);
 	}
 
 	public synchronized void shuffle(long randomSeed) {
 		Collections.shuffle(pile, new Random(randomSeed));
 	}
 
 	/**
 	 * Returns a view of the top design in pile.
 	 * 
 	 * @return The top design in pile or null if the pile if empty.
 	 */
 	public synchronized Design top() {
 		return pile.isEmpty() ? null : pile.get(0);
 	}
 
 	/**
 	 * Returns a list of the top cards in the design pile.
 	 * 
 	 * @param count
 	 * @return Top cards in design pile. If count > size, the resulting list
 	 *         will be the remaining cards in pile.
 	 */
 	public synchronized List<Design> top(int count) {
 		return count > pile.size() ? pile.subList(0, pile.size()) : pile
 				.subList(0, count);
 	}
 
 	/**
 	 * Removes the top design in pile.
 	 * 
 	 * @return The top design in pile or null if the pile is empty.
 	 */
 	public synchronized Design pop() {
 		return pile.isEmpty() ? null : pile.remove(0);
 	}
 
 	public synchronized List<Design> pop(int count) {
 		int to = count > pile.size() ? pile.size() : count;
 		List<Design> poped = new ArrayList<Design>(pile.subList(0, to));
 		pile.subList(0, to).clear();
 		return poped;
 	}
 	
 	/**
	 * Append designs at the end.
 	 * @param more
 	 */
 	public synchronized void appendDesigns (Collection<Design> more) {
 		pile.addAll(more);
 	}
 }
