 /**
  * Project: BasicBlackJack
  * Package: com.gbayer.basicblackjack
  * File: Hand.java
  * Author: Greg Bayer <greg@gbayer.com>
  * Date: Jul 19, 2010
  */
 package com.gbayer.basicblackjack;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 /**
  * A <code>Hand</code> contains <code>Cards</code> currently held by a
  * <code>Player</code>
  */
 public class Hand
 {
 
 	/** The Log4J logger. */
 	private static Logger log = Logger.getLogger(Hand.class);
 
 	/** Constant - Highest value a hand can have before busting. */
 	public static final int MAX_HAND_VALUE = 21;
 
 	/** Constant - Difference between low and high value of an ace. */
 	public static final int ACE_UPGRADE_VALUE = Card.HIGH_ACE_VALUE - Card.LOW_ACE_VALUE;
 
 	/**
 	 * The Enum Result.
 	 */
 	public enum Result
 	{
 		PlayerWins, DealerWins, Push
 	}
 
 	/** The cards in the hand. */
 	private ArrayList<Card> cards;
 
 	/**
 	 * Instantiates a new hand.
 	 */
 	public Hand()
 	{
 		cards = new ArrayList<Card>();
 	}
 
 	/**
 	 * Adds a card to the hand.
 	 * 
 	 * @param card
 	 *            the card
 	 */
 	public void addCard(Card card)
 	{
 		cards.add(card);
 	}
 
 	/**
 	 * Clear all cards in hand. Hand will be empty. Underlying data structure is
 	 * reused.
 	 */
 	public void clear()
 	{
 		cards.clear();
 	}
 
 	/**
 	 * Calculates total hand value. Counts ace as 11 when possible without
 	 * causing hand to bust.
 	 * 
 	 * @return the total hand value
 	 */
 	public int getTotalHandValue()
 	{
 		log.debug("Calculating hand value...");
 
 		int totalWithAcesLow = 0;
 		int numberOfAces = 0;
 		// Sum up value of all cards. Aces are 1 by default. Allow one ace to be
 		// 11 if it will not cause bust.
 		for (Card card : cards)
 		{
 			int cardValue = card.getCardValue(true);
 			totalWithAcesLow += cardValue;
 			if (cardValue == Card.LOW_ACE_VALUE)
 			{
 				numberOfAces++;
 			}
 		}
 
 		log.debug("Hand value with all aces low: " + totalWithAcesLow);
 
 		int total = totalWithAcesLow;
 		// Upgrade ace if can do so without causing player to bust
 		if (numberOfAces > 0
				&& (totalWithAcesLow + ACE_UPGRADE_VALUE) <= MAX_HAND_VALUE)
 		{
 			total += ACE_UPGRADE_VALUE;
 			log.debug("Updrading one ace");
 		}
 
 		log.info("Hand value: " + total);
 
 		return total;
 	}
 
 	/**
 	 * Generates string representing all cards in the hand.
 	 */
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder();
 		for (Card card : cards)
 		{
 			sb.append(card + " ");
 		}
 
 		String hand = sb.toString();
 		log.debug("Printing hand: " + hand);
 
 		return hand;
 	}
 
 	/**
 	 * Generates string showing top card in hand openly and all others as X
 	 * (face down).
 	 * 
 	 * @return the string
 	 */
 	public String toStringShowingTopCardOnly()
 	{
 		StringBuilder sb = new StringBuilder();
 		boolean firstCard = true;
 		for (Card card : cards)
 		{
 			if (firstCard)
 			{
 				sb.append(card + " "); // First card is face-up
 				firstCard = false;
 			} else
 			{
 				sb.append("X "); // Face-down card
 			}
 		}
 
 		String hand = sb.toString();
 		log.debug("Printing hand showing top card only: " + hand);
 
 		return hand;
 	}
 }
