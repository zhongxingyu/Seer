 package kata.holdem.hands;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import kata.holdem.Card;
 import kata.holdem.RankedHand;
 import kata.holdem.collections.Iterables;
 import kata.holdem.collections.Predicate;
 
 public class TwoPairIdentifier implements HandIdentifier {
 	@Override
 	public RankedHand accept(String player, Iterable<Card> cards) {
 		// group cards by card value...
 		Map<Integer, Collection<Card>> groupedByValue = Iterables.groupBy(cards, new CardNumericValue());
 		Collection<Map.Entry<Integer, Collection<Card>>> pairs = Iterables.where(groupedByValue.entrySet(), new ContainsAtLeastOnePair());
 		
 		if (pairs.size() < 2) return null;
 		
 		// there's at least two pairs, pick the best 2 pairs...
 		List<Card> thePairs = new ArrayList<Card>(4);
 		List<Map.Entry<Integer, Collection<Card>>> sortedPairs = Iterables.sort(pairs, new Comparator<Map.Entry<Integer, Collection<Card>>>() {
 			@Override
 			public int compare(Entry<Integer, Collection<Card>> o1, Entry<Integer, Collection<Card>> o2) {
 				return o2.getKey().compareTo(o1.getKey());
 			}});
 		thePairs.addAll(sortedPairs.get(0).getValue());
 		thePairs.addAll(sortedPairs.get(1).getValue());
		return new RankedHand(player, cards, 2, thePairs);
 	}
 
 	private static class ContainsAtLeastOnePair implements Predicate<Map.Entry<Integer, Collection<Card>>> {
 		@Override
 		public boolean evaluate(Map.Entry<Integer, Collection<Card>> cardsWithTheSameValue) {
 			return cardsWithTheSameValue.getValue().size() == 2;
 		}
 	}
 }
