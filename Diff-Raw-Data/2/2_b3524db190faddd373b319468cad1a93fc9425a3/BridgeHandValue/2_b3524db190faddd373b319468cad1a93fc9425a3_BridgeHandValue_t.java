 package org.chaoticbits.cardshuffling.rankcheckers;
 
 import java.lang.reflect.Array;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.chaoticbits.cardshuffling.cards.PlayingCard;
 import org.chaoticbits.cardshuffling.cards.Suit;
 import org.chaoticbits.cardshuffling.cards.Value;
 
 public class BridgeHandValue implements IRankChecker {
 	private static final Map<Value, Double> cardValuation = new HashMap<Value, Double>();
 	static {
 		cardValuation.put(Value.ACE, 4.0);
 		cardValuation.put(Value.KING, 3.0);
 		cardValuation.put(Value.QUEEN, 2.0);
 		cardValuation.put(Value.JACK, 1.0);
 		cardValuation.put(Value.TEN, 0.0);
 		cardValuation.put(Value.NINE, 0.0);
 		cardValuation.put(Value.EIGHT, 0.0);
 		cardValuation.put(Value.SEVEN, 0.0);
 		cardValuation.put(Value.SIX, 0.0);
 		cardValuation.put(Value.FIVE, 0.0);
 		cardValuation.put(Value.FOUR, 0.0);
 		cardValuation.put(Value.THREE, 0.0);
 		cardValuation.put(Value.TWO, 0.0);
 	}
 
 	/**
 	 * Deal the hand out as you would in a Bridge game (one by one to each hand in succession). Then compute
 	 * the "value" of the bridge hand with the following scoring system:
 	 * <ul>
 	 * <li>Ace = 4pts</li>
 	 * <li>King = 3pts</li>
 	 * <li>Queen = 2pts</li>
 	 * <li>Jack = 1pt</li>
 	 * <li>Doubleton in any suit: 1pt</li>
 	 * <li>Singleton in any suit: 2pts</li>
 	 * <li>Void in any suit: 3pts</li>
 	 * </ul>
 	 * 
 	 * Note that the order doesn't matter within each player's hand for a hand score. Then, compare the
 	 * bridge hand for that player from before to after - subtract and take the absolute value (i.e. how much
 	 * different of a bridge hand was this?). Sum up those differences. No change = 0.0.
 	 * 
 	 * @param List
 	 *            <PlayingCard> before - the ordered deck before shuffling
 	 * @param List
 	 *            <PlayingCard> after - the ordered deck after shuffling
 	 */
 	@Override
 	public Double compareRanks(List<PlayingCard> before, List<PlayingCard> after) {
 		if (before.size() != after.size() && before.size() % 4 != 0)
 			throw new IllegalArgumentException("Decks are not equals and divisible by 4.");
 		Double totalScore = 0.0;// assume they're the same initially
 
 		double[] beforeScores = getHandScores(before);
 		double[] afterScores = getHandScores(after);
 		for (int i = 0; i < 4; i++)
 			totalScore += Math.abs(beforeScores[i] - afterScores[i]);
 		return totalScore;
 	}
 
 	public double[] getHandScores(List<PlayingCard> deck) {
 		@SuppressWarnings("unchecked")
 		Set<PlayingCard>[] hands = (Set<PlayingCard>[]) Array.newInstance(Set.class, 4);
 		for (int player = 0; player < 4; player++) {
 			hands[player] = new HashSet<PlayingCard>();
 		}
 		// Deal it out
 		for (int i = 0; i < deck.size(); i++) {
 			hands[i % 4].add(deck.get(i));
 		}
 		// Score each one
 		double[] scores = new double[4];
 		for (int player = 0; player < 4; player++) {
 			Map<Suit, Integer> suitCount = new HashMap<Suit, Integer>();
 			scores[player] = 0;
 			for (PlayingCard card : hands[player]) {
 				scores[player] += cardValuation.get(card.getValue());
 				Integer count = suitCount.get(card.getSuit());
 				if (count == null)
 					count = 1;
 				else count++;
 				suitCount.put(card.getSuit(), count);
 			}
 			for (Suit suit : Suit.values()) {
 				Integer count = suitCount.get(suit);
 				if (count == null) // void in that suit
 					scores[player] += 3;
 				else if (count.equals(1)) // singleton
 					scores[player] += 2;
 				else if (count.equals(2)) // doubleton
 					scores[player] += 1;
 			}
 		}
 
 		return scores;
 	}
 
 	@Override
 	public String name() {
		return "Bridge hand value difference";
 	}
 
 }
