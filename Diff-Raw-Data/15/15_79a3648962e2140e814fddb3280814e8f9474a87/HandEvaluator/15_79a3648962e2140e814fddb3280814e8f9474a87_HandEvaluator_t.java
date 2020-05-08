 package poker.arturka;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import poker.arturka.Card.Rank;
 import poker.arturka.Card.Suit;
 
 public class HandEvaluator {
 
 	private static final int CARDS_TO_EVALUATE = 5;
 	private static final int FOUR_OF_A_KIND_COUNT = 4;
 	private static final int THREE_OF_A_KIND_COUNT = 3;
 	private static final int TWO_PAIR_COUNT = 2;
 	private static final int ONE_PAIR_COUNT = 1;
 	
 	public HandEvaluator(List<Player> playersLeft) {
 		playersToEvaluate = playersLeft;
 		evaluatedPlayers = new HashMap<Player, Hand>();
 	}
 
 	public List<Player> getPlayerHandEvaluation() {
 		List<Player> playerRanking = new ArrayList<Player>();
 		for (Player player: playersToEvaluate) {
 			evaluatedPlayers.put(player, getPlayerHand(player));
 		}
 		sortEvaluatedPlayers();
 		playerRanking.addAll(evaluatedPlayers.keySet());
 		return playerRanking;
 	}
 
 	private Boolean[][] createHandTable(Card[] hand) {
 		Boolean[][] tempHand = new Boolean[Suit.values().length][Rank.values().length];
 		for (Card card: hand) {
 			tempHand[card.getSuit().ordinal()][card.getRank().ordinal()] = true;
 		}
 		/* Prints out card count in table
 		int c= 0;
 		for (Boolean[] val: tempHand) {
 			for (Boolean val2: val) {
 				if (val2 != null) {
 					c++;
 				}
 			}
 		}
 		System.out.println(c);
 		*/
 		return tempHand;
 	}
 	
 	private void sortEvaluatedPlayers() {
 		Map.Entry<Player, Hand> prev = null;
 		for (Entry<Player, Hand> entry: evaluatedPlayers.entrySet()) {
 			if (prev.getValue().ordinal() > entry.getValue().ordinal() && prev != null) {
 				swapEntries(prev, entry);
 			}
 			prev = entry;
 		}
 	}
 
 	private void swapEntries(Entry<Player, Hand> prev, Entry<Player, Hand> entry) {
 		entry.setValue(prev.setValue(entry.getValue()));
 	}
 
 	private Hand getPlayerHand(Player player) {
 		return getHand(player.getHand());
 	}
 
 	public Hand getHand(Card[] hand) {
 		combination = createHandTable(hand);
 		//Card[] sortedHand = sortHand(hand);
 		//
 		if (handIsRoyalFlush(combination))
 			return Hand.ROYAL_FLUSH;
 		else if (handIsStraightFlush(combination))
 			return Hand.STRAIGHT_FLUSH;
 		else if (handIsSameKind(combination, FOUR_OF_A_KIND_COUNT))
 			return Hand.FOUR_OF_A_KIND;
 		else if (handIsFullHouse(combination))
 			return Hand.FULL_HOUSE;
 		else if (handIsFlush(combination))
 			return Hand.FLUSH;
 		else if (handIsStraight(combination))
 			return Hand.STRAIGHT;
 		else if (handIsSameKind(combination, THREE_OF_A_KIND_COUNT))
 			return Hand.THREE_OF_A_KIND;
 		else if (handIsTwoPair(combination))
 			return Hand.TWO_PAIR;
 		else if (handIsSameKind(combination, ONE_PAIR_COUNT))
 			return Hand.ONE_PAIR;
 		else
 			return Hand.HIGH_HAND;
 	}
 	
 	private boolean handIsTwoPair(Boolean[][] combination2) {
 		int skCount = 0;
 		for (int i = Rank.values().length - 1; i > - 1; i--) {
 			for (int j = 0; j < Suit.values().length; j++) {
 				if(combination2[Suit.values().length - j - 1][i] != null) {
 					skCount++;
 				}
 				if (skCount == TWO_PAIR_COUNT)
 					return true;
 			}
 			skCount = 0;
 		}
 		return false;
 	}
 
 	private boolean handIsStraight(Boolean[][] combination2) {
 		int sCount = 0;
 		boolean continued = false;
 		boolean oneFound = false;
 		for (int i = Rank.values().length - 1; i > 0; i--) {
 			for (int j = 0; j < Suit.values().length; j++) {
 				if (combination2[j][i] != null) {
 					for (int k = 0; k < Suit.values().length; k++) {
 						if (combination2[k][i - 1] != null && !oneFound) {
 							sCount++;
 							continued = true;
 							oneFound = true;
 						}
 					}
 				}
 				if (sCount == 4)
 					return true;
 			}
 			oneFound = false;
 			if (!continued)
 				sCount = 0;
 		}
 		return false;
 	}
 
 	private boolean handIsFlush(Boolean[][] combination2) {
 		int fCount = 0;
 		for (int i = 0; i < Suit.values().length; i++) {
 			for (int j = 0; j < Rank.values().length - 1; j++) {
 				if (combination2[i][Rank.values().length - j - 1] != null) {
 					fCount++;
 				}
 				if (fCount == 4)
 					return true;
 			}
 			fCount = 0;
 		}
 		return false;
 	}
 
 	private boolean handIsFullHouse(Boolean[][] combination2) {
		Boolean[][] temp = new Boolean[Suit.values().length][Rank.values().length];
		for (int i = 0; i <  Rank.values().length; i++) {
			for (int j = 0; j < Suit.values().length; j++) {
				temp[j][i] = combination2[j][i];
			}
		}
 		int tokCount = 0;
 		boolean threeOfAKind = false;
 		boolean twoOfAKind = false;
 		boolean lineCleared = false;
 		for (int i = 0; i < Rank.values().length; i++) {
 			for (int j = 0; j < Suit.values().length; j++) {
				if(temp[j][i] != null)
 					tokCount++;
 				if (tokCount == 3 && !lineCleared) {
 					for (int j2 = 0; j2 < Suit.values().length; j2++) {
						temp[j2][i] = null;
 					}
 					lineCleared = true;
 					threeOfAKind = true;
 				}
 			}
 			tokCount = 0;
 		}
		if (handIsSameKind(temp, 2))
 			twoOfAKind = true;
 		if (twoOfAKind && threeOfAKind)
 			return true;
 		return false;
 	}
 
 	private boolean handIsSameKind(Boolean[][] combination2, int count) {
 		int skCount = 0;
 		for (int i = Rank.values().length - 1; i > -1; i--) {
 			for (int j = 0; j < Suit.values().length; j++) {
 				if(combination2[j][i] != null) {
 					skCount++;
 				}
 				if (skCount == count)
 					return true;
 			}
 			skCount = 0;
 		}
 		return false;
 	}
 
 	private boolean handIsStraightFlush(Boolean[][] combination2) {
 		int sfCount = 0;
 		for (int i = 0; i < Suit.values().length; i++) {
 			for (int j = 0; j < Rank.values().length - 1; j++) {
 				if (combination2[i][Rank.values().length - j - 1] != null &&
 						combination2[i][Rank.values().length - j] != null) {
 					sfCount++;
 				}
 				if (sfCount == 4)
 					return true;
 			}
 			sfCount = 0;
 		}
 		return false;
 	}
 
 	private boolean handIsRoyalFlush(Boolean[][] combination2) {
 		int rfCount = 0;
 		for (int i = 0; i < Suit.values().length; i++) {
 			for (int j = 0; j < CARDS_TO_EVALUATE; j++) {
 				if (combination2[i][Rank.values().length - j - 1] != null) {
 					rfCount++;
 				}
 				if (rfCount == 5)
 					return true;
 			}
 			rfCount = 0;
 		}
 		return false;
 	}
 
 	private List<Player> playersToEvaluate;
 	private HashMap<Player, Hand> evaluatedPlayers;
 	private Boolean[][] combination;
 }
