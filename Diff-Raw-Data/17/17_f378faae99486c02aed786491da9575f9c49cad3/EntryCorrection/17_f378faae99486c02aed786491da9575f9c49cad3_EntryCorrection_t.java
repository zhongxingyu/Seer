 package org.chaoticbits.cardshuffling.entry;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.chaoticbits.cardshuffling.cards.PlayingCard;
 import org.chaoticbits.cardshuffling.cards.Suit;
 import org.chaoticbits.cardshuffling.cards.Value;
 
 public class EntryCorrection {
 
 	/**
 	 * Check if the list of cards are 52, and unique - reporting what's wrong.
 	 * @param deck
 	 * @throws DataEntryException
 	 */
 	public void checkDeckEntry(List<PlayingCard> deck) throws DataEntryException {
 		HashMap<PlayingCard, Integer> typeCount = new HashMap<PlayingCard, Integer>(52);
 		for (PlayingCard playingCard : deck) {
 			Integer count = typeCount.get(playingCard);
 			if (count == null)
 				typeCount.put(playingCard, 1);
 			else typeCount.put(playingCard, count + 1);
 		}
		if (deck.size() < 52)
			throw new DataEntryException("Less than 52 cards, missing " + getMissing(typeCount));
 		boolean wrong = false;
 		for (Map.Entry<PlayingCard, Integer> entry : typeCount.entrySet()) {
 			if (entry.getValue() != 1)
 				wrong = true;
 		}
 		if (wrong)
 			throw new DataEntryException("Deck not unique, missing " + getMissing(typeCount) + ", "
 					+ getRepeats(typeCount));
 	}
 
 	private String getRepeats(HashMap<PlayingCard, Integer> typeCount) {
 		String repeats = "";
 		for (Map.Entry<PlayingCard, Integer> entry : typeCount.entrySet()) {
 			if (entry.getValue() != 1)
 				repeats += entry.getKey().toString() + " has " + entry.getValue() + " repeats";
 		}
 		return repeats;
 	}
 
 	private String getMissing(HashMap<PlayingCard, Integer> typeCount) {
 		String missing = "";
 		for (Suit suit : Suit.values()) {
 			for (Value value : Value.values()) {
 				PlayingCard card = new PlayingCard(value, suit);
 				if (typeCount.get(new PlayingCard(value, suit)) == null)
 					missing += card.toString() + ", ";
 			}
 		}
		if ("".equals(missing))
			return "NONE";
 		return missing.substring(0, missing.length() - 2);
 	}
 
 }
