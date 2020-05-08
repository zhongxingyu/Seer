 package de.pokerdude;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.TreeSet;
 
 public class CardSet {
 
 	TreeSet<Card> clubs = new TreeSet<Card>();
 	TreeSet<Card> diamonds = new TreeSet<Card>();
 	TreeSet<Card> hearts = new TreeSet<Card>();
 	TreeSet<Card> spades = new TreeSet<Card>();
 	TreeSet<Card> cards = null;
 	HashMap<Integer, Integer> mapping = new HashMap<Integer, Integer>();
 
 	public CardSet(ArrayList<Card> cards) {
 		this.cards = new TreeSet<Card>(cards);
 		for (Card card : cards) {
 			Suite suite = card.getSuite();
 			if (suite.equals(Suite.CLUBS)) {
 				clubs.add(card);
 			} else if (suite.equals(Suite.DIAMONDS)) {
 				diamonds.add(card);
 			} else if (suite.equals(Suite.HEARTS)) {
 				hearts.add(card);
 			} else if (suite.equals(Suite.SPADES)) {
 				spades.add(card);
 			}
 			int value = card.getValue();
 			if (mapping.containsKey(value)) {
 				mapping.put(value, mapping.get(value) + 1);
 			} else {
 				mapping.put(value, 1);
 			}
 		}
 	}
 
 	public Powerrating evaluate() {
 		//System.out.println("Evaluating:" + toString());
 		if(getStraightFlushRating() != null) {
 			return getStraightFlushRating();
 		} else if (containsFourOfAKind()) {
 			return getFourOfAKindRating();
 		} else if (containsFullHouse()) {
 			return getFullHouseRating();
 		} else if (getFlushRating() != null) {
 			return getFlushRating();
 		} else if (containsStraight()) {
 			return getStraightRating();
 		} else if (containsThreeOfAKind()) {
 			return getThreeOfAKindRating();
 		} else if (containsTwoPairs()) {
 			return getDoublePairRating();
 		} else if (containsOnePair()) {
 			return getPairRating();
 		} else {
 			return getHighestRating();
 		}
 	}
 
 
 	public Powerrating getHighestRating() {
 		int[] rating = new int[] { 1, 0, 0, 0, 0 };
 		int b = 1;
 		Iterator<Card> cardIter = cards.descendingIterator();
 		while(cardIter.hasNext()) {
 			rating[b++] = cardIter.next().getValue();
 			if (b >= 5)
 				break;
 		}
 		return new Powerrating(rating);
 	}
 
 	public Powerrating getPairRating() {
 		int pairAt = -1;
 		for (Entry<Integer, Integer> entry : mapping.entrySet()) {
 			if (entry.getValue() >= 2) {
 				pairAt = entry.getKey();
 			}
 		}
 		if (pairAt == -1) {
 			return null;
 		}
 		mapping.remove(pairAt);
 		int[] rating = new int[] { 2, pairAt, 0, 0, 0 };
 		int b = 2;
 		for (int i = 14; i > 0; i--) {
 			if (mapping.containsKey(i)) {
 				rating[b++] = i;
 				mapping.remove(i);
 			}
 			if (b >= 5)
 				break;
 		}
 		return new Powerrating(rating);
 	}
 
 	public Powerrating getDoublePairRating() {
 		int pairnum = 0;
 		int[] pairs = new int[3];
 		for (Entry<Integer, Integer> entry : mapping.entrySet()) {
 			if (entry.getValue() >= 2) {
 				pairs[pairnum++] = entry.getKey();
 			}
 		}
 		Arrays.sort(pairs);
		mapping.remove(pairs[0]);
 		mapping.remove(pairs[1]);
 
 		int[] rating = new int[] { 3, pairs[2], pairs[1], 0, 0 };
 		int b = 3;
 		for (int i = 14; i > 0; i--) {
 			if (mapping.containsKey(i)) {
 				rating[b++] = i;
 				mapping.remove(i);
 			}
 			if (b >= 5)
 				break;
 		}
 		return new Powerrating(rating);
 	}
 
 	public Powerrating getThreeOfAKindRating() {
 		return new Powerrating(new int[] { 4, getOfAKind(3) });
 	}
 
 	public Powerrating getStraightRating() {
 		return new Powerrating(new int[] { 5, findRow(cards) });
 	}
 
 	public Powerrating getFlushRating() {
 		if (getBiggestSet().size() >= 5) {
 			TreeSet<Card> set = getBiggestSet();
 			Card last = set.last();
 			return new Powerrating(new int[] { 6, last.getValue() });
 		}
 		return null;
 	}
 
 	public Powerrating getFullHouseRating() {
 		int pair = -1;
 		int triple = -1;
 		for (Entry<Integer, Integer> entry : mapping.entrySet()) {
 			if (entry.getValue() == 2) {
 				pair = entry.getKey();
 			}
 			if (entry.getValue() >= 3) {
 				triple = entry.getKey();
 			}
 		}
 		return new Powerrating(new int[] { 7, triple, pair });
 	}
 
 	public Powerrating getFourOfAKindRating() {
 		return new Powerrating(new int[] { 8, getOfAKind(4) });
 	}
 
 	public Powerrating getStraightFlushRating() {
 		TreeSet<Card> set = getBiggestSet();
 		if (set.size() >= 5 && findRow(set) != -1) {
 			Card last = set.last();
 			return new Powerrating(new int[] { 9, last.getValue() });
 		}
 		return null;
 	}
 
 	public boolean containsOnePair() {
 		return containsOfAKind(2);
 	}
 
 	public boolean containsTwoPairs() {
 		int pairs = 0;
 		for (Entry<Integer, Integer> entry : mapping.entrySet()) {
 			if (entry.getValue() >= 2) {
 				++pairs;
 			}
 		}
 		return pairs >= 2;
 	}
 
 	public boolean containsFullHouse() {
 		boolean pair = false;
 		boolean triple = false;
 		for (Entry<Integer, Integer> entry : mapping.entrySet()) {
 			if (entry.getValue() == 2) {
 				pair = true;
 			}
 			if (entry.getValue() >= 3) {
 				triple = true;
 			}
 		}
 		return pair && triple;
 	}
 
 	public int getOfAKind(int number) {
 		for (Entry<Integer, Integer> entry : mapping.entrySet()) {
 			if (entry.getValue() >= number) {
 				return entry.getKey();
 			}
 		}
 		return -1;
 
 	}
 
 	public boolean containsFourOfAKind() {
 		return containsOfAKind(4);
 	}
 
 	public boolean containsThreeOfAKind() {
 		return containsOfAKind(3);
 	}
 
 	public boolean containsOfAKind(int number) {
 		for (Entry<Integer, Integer> entry : mapping.entrySet()) {
 			if (entry.getValue() >= number) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean containsStraight() {
 		return findRow(cards) != -1;
 	}
 
 	/**
 	 * Check if a straight row of cards for a given card set exists
 	 */
 	private int findRow(TreeSet<Card> cardSet) {
 		Card[] cardArray = cardSet.toArray(new Card[0]);
 		Card previous = cardSet.first();
 		int row = 1;
 		for (int i = 1; i < cardSet.size(); i++) {
 			if (cardArray[i].getValue() == previous.getValue() + 1) {
 				row++;
 			} else {
 				row = 1;
 			}
 			if(row == 5) {
 				return cardArray[i].getValue();
 			}
 			previous = cardArray[i];
 		}
 		return -1;
 	}
 
 	private TreeSet<Card> getBiggestSet() {
 		TreeSet<Card> res = clubs;
 		if (diamonds.size() > res.size()) {
 			res = diamonds;
 		}
 		if (hearts.size() > res.size()) {
 			res = hearts;
 		}
 		if (spades.size() > res.size()) {
 			res = spades;
 		}
 		return res;
 	}
 	
 	@Override
 	public String toString() {
 		String ret = "";
 		for(Card card: cards) {
 			ret += card;
 		}
 		return ret;
 	}
 
 }
