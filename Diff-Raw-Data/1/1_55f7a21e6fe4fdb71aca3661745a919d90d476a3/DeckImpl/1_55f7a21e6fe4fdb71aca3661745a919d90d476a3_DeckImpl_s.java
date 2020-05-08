 /**
  * Copyright 2011. Adam Lock <locka99@gmail.com>
  *
  * Available as open source under the terms of LGPLv3
  */
 package com.adamlock.cards;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Represents a standard deck of 52 unique cards. Cards are undrawn or drawn,
  * and can be replaced / removed between drawn and undrawn piles.
  * 
  * @author Adam Lock
  */
 public class DeckImpl implements Cloneable, Deck {
 
 	/** The deck is 52 indices onto the 52 possible card combinations. */
 	private static final Card allCards[] = Card.values();
 
 	/** Reverse lookup turns a card into an index */
 	private static final HashMap<Card, Integer> reverseCardLookup;
 
 	static {
 		// Make the reverse card lookup
 		reverseCardLookup = new HashMap<Card, Integer>();
 		final Card[] allCards = Card.values();
 		for (int i = 0; i < allCards.length; i++) {
 			reverseCardLookup.put(allCards[i], i);
 		}
 	}
 
 	/**
 	 * Represents the entire deck of 52 cards
 	 */
 	private int deck[] = new int[allCards.length];
 	private int startOfDrawn = deck.length;
 
 	private static final Comparator<ShuffleInfo> shuffleComparator = new Comparator<ShuffleInfo>() {
 		@Override
 		public int compare(ShuffleInfo o1, ShuffleInfo o2) {
 			// Walk the array of bytes until one is deemed to be
 			// larger than the other
 			final int length = o1.getOrder().length;
 			final byte[] b1 = o1.getOrder();
 			final byte[] b2 = o2.getOrder();
 			for (int i = 0; i < length; i++) {
 				if (b1[i] < b2[i])
 					return -1;
 				else if (b1[i] > b2[i])
 					return 1;
 			}
 			return 0;
 		}
 	};
 
 	/**
 	 * Constructor
 	 */
 	public DeckImpl() {
 		try {
 			createDeck();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Constructor which optionally shuffles the deck
 	 * 
 	 * @param shuffle
 	 */
 	public DeckImpl(boolean shuffle) {
 		this();
 		if (shuffle) {
 			shuffle();
 		}
 	}
 
 	/**
 	 * Create a fresh sorted deck of 52 cards.
 	 */
 	private void createDeck() {
 		for (int i = 0; i < deck.length; i++) {
 			deck[i] = i;
 		}
 		startOfDrawn = deck.length;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#reset()
 	 */
 	public void reset() {
 		// Put drawn marker to end
 		startOfDrawn = deck.length;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#shuffle()
 	 */
 	public void shuffle() {
 
 		final int undrawnSize = startOfDrawn;
 
 		final ArrayList<ShuffleInfo> shuffleList = new ArrayList<ShuffleInfo>(
 				undrawnSize);
 
 		// For every card in the deck, create a shuffle info consisting of a
 		// random number and the card.
 		for (int i = 0; i < undrawnSize; i++) {
 			shuffleList.add(new ShuffleInfo(deck[i]));
 		}
 
 		// Sort by the random number
 		Collections.sort(shuffleList, shuffleComparator);
 
 		// Now create the deck again in the new order
 		for (int i = 0; i < undrawnSize; i++) {
 			deck[i] = shuffleList.get(i).getCardIndex();
 		}
 	}
 
 	/**
 	 * Get the indices for each of the requested cards
 	 * 
 	 * @param cards
 	 * @return
 	 */
 	private int[] getCardIndices(Card[] cards) {
 		int cardIndices[] = new int[cards.length];
 		for (int i = 0; i < cards.length; i++) {
 			cardIndices[i] = reverseCardLookup.get(cards[i]);
 		}
 		return cardIndices;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#isEmpty()
 	 */
 	public boolean isEmpty() {
 		return startOfDrawn == 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#size()
 	 */
 	public int size() {
 		return startOfDrawn;
 	}
 
 	@Override
 	public int totalSize() {
 		return allCards.length;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#deal(int)
 	 */
 	public Card[] deal(int numCards) throws EmptyDeckException {
 		final Card[] result = new Card[numCards];
 		if (numCards < 1) {
 			throw new IndexOutOfBoundsException();
 		}
 
 		if (startOfDrawn < numCards) {
 			throw new EmptyDeckException();
 		}
 		for (int i = 0; i < numCards; ++i) {
 			result[i] = allCards[deck[startOfDrawn - i - 1]];
 		}
 		startOfDrawn -= numCards;
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#deal(com.adamlock.cards.CardPattern, int)
 	 */
 	public Card[] deal(CardPattern pattern, int numCards)
 			throws EmptyDeckException {
 		if (pattern.isRandom()) {
 			return deal(numCards);
 		}
 		final Card[] cards = new Card[numCards];
 		for (int i = 0; i < numCards; i++) {
 			cards[i] = dealOne(pattern);
 		}
 		return cards;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#dealOne(com.adamlock.cards.CardPattern)
 	 */
 	public Card dealOne(CardPattern pattern) throws EmptyDeckException {
 		if (pattern.isRandom()) {
 			return dealOne();
 		}
 		for (int i = startOfDrawn - 1; i >= 0; i--) {
 			final Card card = allCards[deck[i]];
 			if (pattern.matches(card)) {
 				removeCard(card);
 				return card;
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#deal(com.adamlock.cards.CardPattern[])
 	 */
 	public Card[] deal(CardPattern patterns[]) throws EmptyDeckException,
 			InvalidCardException {
 		final Card[] result = new Card[patterns.length];
 
 		// Two passes, draw exact cards before looking at patterns
 		for (int pass = 0; pass < 2; pass++) {
 			int patternIdx = 0;
 			for (CardPattern pattern : patterns) {
 				if (pattern == null) {
 					result[patternIdx] = null;
 				} else if ((pass == 0 && pattern.isExact())
 						|| (pass == 1 && !pattern.isExact())) {
 					Card foundCard = null;
 					for (int i = startOfDrawn - 1; i >= 0; i--) {
 						final Card card = allCards[deck[i]];
 						if (pattern.matches(card)) {
 							foundCard = card;
 							break;
 						}
 					}
 					if (foundCard != null) {
 						removeCard(foundCard);
 						result[patternIdx] = foundCard;
 					} else {
 						throw new InvalidCardException(pattern,
 								"Pattern does not match any card in the deck");
 					}
 				}
 				patternIdx++;
 			}
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#dealOne()
 	 */
 	public Card dealOne() throws EmptyDeckException {
 		// Remove the first card from the deck
 		if (startOfDrawn == 0) {
 			throw new EmptyDeckException();
 		}
 		final Card c = allCards[deck[startOfDrawn - 1]];
 		startOfDrawn--;
 		return c;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#dealRandom()
 	 */
 	public Card dealRandom() throws EmptyDeckException {
 		if (startOfDrawn == 0) {
 			throw new EmptyDeckException();
 		}
 		final int randomIdx = ShuffleInfo.RANDOM.nextInt(startOfDrawn);
 		final Card card = allCards[deck[randomIdx]];
 		removeCardAt(randomIdx);
 		return card;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#removeCard(com.adamlock.cards.Card)
 	 */
 	public boolean removeCard(Card card) {
 		if (card == null) {
 			throw new IllegalArgumentException("Must supply a card");
 		}
 
 		final int cardIndex = reverseCardLookup.get(card);
 
 		// Look for the card in the undrawn pile
 		int foundIndex = -1;
 		for (int i = 0; i < startOfDrawn; i++) {
 			if (deck[i] == cardIndex) {
 				foundIndex = i;
 				break;
 			}
 		}
 		if (foundIndex == -1) {
 			return false;
 		}
 
 		// Move everything left over by 1 and put card on end
 		removeCardAt(foundIndex);
 		return true;
 	}
 
 	/**
 	 * Remove a card at the specified position from the undrawn to the drawn
 	 * pile
 	 * 
 	 * @param position
 	 *            position to remove card from.
 	 */
 	private void removeCardAt(int position) {
 		final int cardIndex = deck[position];
 		startOfDrawn--;
 		for (int i = position; i < deck.length - 1; i++) {
 			deck[i] = deck[i + 1];
 		}
 		deck[deck.length - 1] = cardIndex;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#removeCard(com.adamlock.cards.Card[])
 	 */
 	public int removeCard(Card[] cards) {
 		if (cards == null) {
 			throw new IllegalArgumentException("Must supply cards");
 		}
 
 		// Produce indices for input cards
 		int cardIndices[] = getCardIndices(cards);
 
 		// Build a new deck
 		int newDeck[] = new int[deck.length];
 		int newDeckIdx = 0;
 		int cardsRemoved[] = new int[cards.length];
 		int cardsRemovedCount = 0;
 		outer: for (int i = 0; i < startOfDrawn; i++) {
 			for (int card = 0; card < cardIndices.length; card++) {
 				if (deck[i] == cardIndices[card]) {
 					// Card was found
 					cardsRemoved[cardsRemovedCount++] = cardIndices[card];
 					continue outer;
 				}
 			}
 			newDeck[newDeckIdx++] = deck[i];
 		}
 		if (cardsRemovedCount == 0) {
 			return 0;
 		}
 
 		final int newStartOfDrawn = newDeckIdx;
 		for (int i = 0; i < cardsRemovedCount; i++) {
 			newDeck[newStartOfDrawn + i] = cardsRemoved[i];
 		}
 		for (int i = 0; i < newDeck.length - startOfDrawn; i++) {
 			newDeck[newStartOfDrawn + cardsRemovedCount + i] = deck[startOfDrawn
 					+ i];
 		}
 		deck = newDeck;
 		startOfDrawn = newStartOfDrawn;
 
 		return cardsRemovedCount;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#replaceCard(com.adamlock.cards.Card)
 	 */
 	public boolean replaceCard(Card card) {
 		if (card == null) {
 			throw new IllegalArgumentException("Must supply a card");
 		}
 		if (startOfDrawn == deck.length) {
 			return false;
 		}
 
 		// Look for a card in the drawn pile
 		final int cardIndex = reverseCardLookup.get(card);
 		int foundIndex = -1;
 		for (int i = startOfDrawn; i < deck.length; i++) {
 			if (deck[i] == cardIndex) {
 				foundIndex = i;
 				break;
 			}
 		}
 		if (foundIndex == -1) {
 			return false;
 		}
 
 		// We found it so move everything to the right by one so it can be put
 		// at 0
 		startOfDrawn++;
 		for (int i = foundIndex; i > 0; i--) {
 			deck[i] = deck[i - 1];
 		}
 		deck[0] = cardIndex;
 
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adamlock.cards.IDeck#replaceCard(com.adamlock.cards.Card[])
 	 */
 	public int replaceCard(Card[] cards) {
 		if (cards == null) {
 			throw new IllegalArgumentException("Must supply cards");
 		}
 		if (startOfDrawn == deck.length) {
 			return 0;
 		}
 
 		final int cardIndices[] = getCardIndices(cards);
 
 		// Build a new deck
 		final int newDeck[] = new int[deck.length];
 		int newDeckIdx = deck.length - 1;
 		final int cardsReplaced[] = new int[cards.length];
 		int cardsReplacedCount = 0;
 
 		outer: for (int i = deck.length - 1; i >= startOfDrawn; i--) {
 			for (int card = 0; card < cardIndices.length; card++) {
 				if (deck[i] == cardIndices[card]) {
 					// Card was found
 					cardsReplaced[cardsReplacedCount++] = cardIndices[card];
 					continue outer;
 				}
 			}
 			newDeck[newDeckIdx--] = deck[i];
 		}
 		if (cardsReplacedCount == 0) {
 			return 0;
 		}
 
 		final int newStartOfDrawn = startOfDrawn + cardsReplacedCount;
 		for (int i = 0; i < cardsReplacedCount; i++) {
 			newDeck[i] = cardsReplaced[i];
 		}
 		for (int i = cardsReplacedCount; i < newStartOfDrawn; i++) {
 			newDeck[i] = deck[i - cardsReplacedCount];
 		}
 		deck = newDeck;
 		startOfDrawn = newStartOfDrawn;
 		return cardsReplacedCount;
 	}
 
 	/**
 	 * Validate all the cards
 	 */
 	void internalValidate() {
 		final Set<Card> found = new HashSet<Card>();
 		for (int i = 0; i < deck.length; i++) {
 			Card c = allCards[deck[i]];
 			if (!found.add(allCards[deck[i]])) {
 				System.out.println("Duplicate of card " + c + " in the deck!");
 				throw new RuntimeException("Duplicate card");
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		final StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < startOfDrawn; i++) {
 			sb.append(allCards[deck[i]].toString());
 			sb.append("\n");
 		}
 		return sb.toString();
 	}
 
 	public Object clone() {
 		final DeckImpl newDeck = new DeckImpl();
 		newDeck.deck = new int[deck.length];
 		for (int i = 0; i < deck.length; i++) {
 			newDeck.deck[i] = deck[i];
 		}
 		newDeck.startOfDrawn = startOfDrawn;
 		return newDeck;
 	}
 }
