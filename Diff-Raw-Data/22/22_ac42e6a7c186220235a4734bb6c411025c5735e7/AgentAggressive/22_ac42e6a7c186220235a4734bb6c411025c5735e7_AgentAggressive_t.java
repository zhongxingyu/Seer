 package edu.txstate.hearts.model;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import edu.txstate.hearts.controller.Hearts;
 import edu.txstate.hearts.controller.Hearts.Passing;
 import edu.txstate.hearts.model.Card.Face;
 import edu.txstate.hearts.model.Card.Suit;
 import edu.txstate.hearts.utils.ProbabilityUtils;
 import edu.txstate.hearts.utils.RiskThresholds;
 import edu.txstate.hearts.utils.RiskThresholds.Threshold;
 
 /**
  * Contains rules & strategies for the most difficult AI agent
  * 
  * The basic strategy of the AgentAggressive is to play the Queen of Spades
  * or a Heart on a user if possible, otherwise determine the best card to play
  * by finding the highest card in the smallest suit that is under the current
  * risk threshold.  This is done leveraging the RiskThreshold class, which
  * contains multiple Thresholds for risk under various situations, which each
  * get adjusted based on the result of what happened each hand.
  * 
  * At a simple level, the risk being compared to the risk threshold is a
  * calculation of the probability that one of the players yet to play does not
  * have any cards in the suit led and that this player also has hearts, and 
  * the probability that if this card is played, that this card would win the
  * hand.  If this value exceeds the risk threshold, it is determined that this
  * card is too risky to play.  If it is under the risk threshold, the card is
  * fine to play.
  * 
  * After each turn is done, look at the cards played in that turn.  If my card
  * won the trick, determine if I took any points, and if so, decrease the risk
  * threshold.  If my card won and I did not win any points, increase the risk
  * threshold.  If my card did not win the trick, increase the risk threshold.
  * 
  * @author Neil Stickels, Jonathan Shelton
  * 
  */
 public class AgentAggressive extends Agent {
 
 	private static RiskThresholds thresholds;
 	private boolean hasQueenOfSpades = false;
 	// private double lastChanceOfWin;
 	private double expectedWins;
 	private int actualWins;
 	private Threshold lastThreshold;
 	private final static boolean silent = Hearts.silent;
 
 	/**
 	 * Contruct an Aggressive Agent with the name specified and as the player
 	 * number specified.  If the thresholds object hasn't been created, check
 	 * to see if a thresholds.bin file exists, which should contain the persisted
 	 * thresholds.  If it does, read in that binary file.  If it doesn't, create
 	 * some new default thresholds.
 	 * @param playerName the name to display for this player as a String
 	 * @param num the index of the player in the list of players for the game
 	 * @see edu.txstate.hearts.model.Agent#Agent(String, int)
 	 */
 	public AgentAggressive(String playerName, int num) {
 		super(playerName, num);
 		if (thresholds == null) 
 		{
 			try 
 			{
 				// use buffering
 				InputStream file = new FileInputStream("thresholds.bin");
 				InputStream buffer = new BufferedInputStream(file);
 				ObjectInput input = new ObjectInputStream(buffer);
 				try 
 				{
 					// deserialize the RiskThreshold
 					thresholds = (RiskThresholds) input.readObject();
 
 				} finally 
 				{
 					input.close();
 				}
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				System.out.println("problem reading in file thresholds.bin: "
 						+ e.getMessage());
 				thresholds = new RiskThresholds();
 			}
 		}
 	}
 	
 	/**
 	 * Added just for unit testing to test whether or not this player has
 	 * the Queen of Spades
 	 */
 	public boolean hasQueenOfSpades()
 	{
 		return hasQueenOfSpades;
 	}
 
 	/**
 	 * Save all of the current threshold objects to disk 
 	 */
	static public void serializeThresholds() {
 		try {
 			// use buffering
 			OutputStream file = new FileOutputStream("thresholds.bin");
 			OutputStream buffer = new BufferedOutputStream(file);
 			ObjectOutput output = new ObjectOutputStream(buffer);
 			try {
				//if(silent)
				//	System.out.println("saving "+thresholds);
 				output.writeObject(thresholds);
 			} finally {
 				output.close();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Figure out the cards to pass.  The Aggressive Agent's logic for passing
 	 * cards:<br>
 	 * While the number of cards to pass is less than 3:
 	 * <li>determine the suit with the fewest cards
 	 * <li>determine the number of cards within that suit
 	 * <li>if the suit with the fewest cards is clubs or diamonds and the 
 	 * number of cards in that suit contains the number of cards to pass or 
 	 * fewer cards, pass all of those cards
 	 * <li>if the suit with the fewest cards is spades, and you only have one
 	 * spade, the queen of spades, pass the queen of spades
 	 * <li>if the number of cards in the suit with the fewest is greater than
 	 * the number of cards to pass, if you have the two of clubs, pass that
 	 * <li>if the suit with the fewest cards is diamonds or clubs and you have
 	 * more than the number of cards to pass in that suit, pass the highest
 	 * cards you have in that suit up to the number of cards to pass
 	 * <li>if the suit with the fewest cards is spades, and you have more than 
 	 * 4 spades and you have the Ace, King or Queen of spades pass those
 	 * <li>if you get here and still have cards to pass, pass your highest
 	 * hearts
 	 * 
 	 * @param passing enumeration for which way to pass the cards
 	 * @return the List of Cards being passed
 	 * @see edu.txstate.hearts.model.Agent#getCardsToPass(Passing)
 	 */
 	@Override
 	public List<Card> getCardsToPass(Passing passing) {
 		int numCardsToPass = 3;
 		List<Card> cardsToPass = new ArrayList<Card>();
 
 		List<Card> myHand = this.getHand();
 		Collections.sort(myHand, new CardComparator());
 		List<Card> spades = getAllOfSuit(Suit.Spades);
 		List<Card> diamonds = getAllOfSuit(Suit.Diamonds);
 		List<Card> clubs = getAllOfSuit(Suit.Clubs);
 		List<Card> fewest = getCardsFromSuitWithFewest(spades, diamonds, clubs);
 		List<Card> oldFewest = null;
 		
 		while(numCardsToPass > 0)
 		{
 			if(fewest == null)
 			{
 				if(spades.size() > 5)
 				{
 					Collections.sort(spades, new CardComparator());
 					Collections.reverse(spades);
 					int counter = 0;
 					while(numCardsToPass > 0)
 					{
 						Card c = spades.get(counter++);
 						if(c.getFace() == Face.Queen)
 							continue;
 						cardsToPass.add(c);
 						spades.remove(c);
 						counter--;
 						myHand.remove(c);
 						numCardsToPass--;
 					}
 				} else
 				{
 					List<Card> hearts = getAllOfSuit(Suit.Hearts);
 					Collections.sort(hearts, new CardComparator());
 					Collections.reverse(hearts);
 					while(numCardsToPass > 0)
 					{
 						Card c = hearts.get(0);
 						cardsToPass.add(c);
 						hearts.remove(c);
 						numCardsToPass--;
 						myHand.remove(c);
 					}
 				}
 			}
 
 			if(numCardsToPass > 0 && fewest.size() > numCardsToPass)
 			{
 				if(hasTwoOfClubs())
 				{
 					Card c = new Card(Face.Deuce, Suit.Clubs);
 					cardsToPass.add(c);
 					myHand.remove(c);
 					numCardsToPass--;
 					clubs.remove(c);
 					fewest = getCardsFromSuitWithFewest(spades, diamonds, clubs);
 				}
 				if(spades.size() > 4 && fewest.size() > numCardsToPass)
 				{
 					Set<Card> cardsToRemove = new HashSet<Card>();
 					int counter = 0;
 					for(Card c: spades)
 					{
 						if((numCardsToPass-counter) > 0 && (spades.size()-counter) > 4 && c.getFace().ordinal() > 10)
 						{
 							cardsToPass.add(c);
 							myHand.remove(c);
 							counter++;
 							cardsToRemove.add(c);
 							fewest = getCardsFromSuitWithFewest(spades, diamonds, clubs);
 						}
 					}
 					numCardsToPass -= counter;
 					spades.removeAll(cardsToRemove);
 				}
 			} 
 		
 			if(fewest == clubs)
 			{	
 				if(clubs.size() <= numCardsToPass)
 				{
 					cardsToPass.addAll(clubs);
 					myHand.removeAll(clubs);
 					numCardsToPass -= clubs.size();
 					clubs.clear();
 				} else
 				{
 					if(hasTwoOfClubs() && numCardsToPass > 0)
 					{
 						Card c = new Card(Face.Deuce, Suit.Clubs);
 						cardsToPass.add(c);
 						myHand.remove(c);
 						numCardsToPass--;
 						clubs.remove(c);
 					}
 					Collections.sort(clubs, new CardComparator());
 					Collections.reverse(clubs);
 					while(numCardsToPass > 0)
 					{
 						Card c = clubs.remove(0);
 						cardsToPass.add(c);
 						myHand.remove(c);
 						numCardsToPass--;
 					}
 				}
 			} else if(fewest == diamonds)
 			{
 				if(diamonds.size() <= numCardsToPass)
 				{
 					cardsToPass.addAll(diamonds);
 					myHand.removeAll(diamonds);
 					numCardsToPass -= diamonds.size();
 					diamonds.clear();
 				} else
 				{
 					if(hasTwoOfClubs() && numCardsToPass > 0)
 					{
 						Card c = new Card(Face.Deuce, Suit.Clubs);
 						cardsToPass.add(c);
 						myHand.remove(c);
 						numCardsToPass--;
 						clubs.remove(c);
 					}
 					Collections.sort(diamonds, new CardComparator());
 					Collections.reverse(diamonds);
 					while(numCardsToPass > 0)
 					{
 						Card c = diamonds.remove(0);
 						cardsToPass.add(c);
 						myHand.remove(c);
 						numCardsToPass--;
 					}
 				}
 			
 			} else if(fewest == spades)
 			{
 				if(hasQueenOfSpades && spades.size() == 1 && numCardsToPass > 0)
 				{
 					Card c = new Card(Face.Queen, Suit.Spades);
 					cardsToPass.add(c);
 					myHand.remove(c);
 					spades.remove(c);
 					numCardsToPass--;
 					hasQueenOfSpades = false;
 				}
 			}
 			oldFewest = fewest;
 			fewest = getCardsFromSuitWithFewest(spades, diamonds, clubs);
 			if(numCardsToPass > 0 && oldFewest == fewest)
 			{
 				if(fewest == spades)
 				{
 					fewest = getCardsFromSuitWithFewest(Collections.<Card> emptyList(), diamonds, clubs);
 				} else
 				{
 					// if we got here, this means something bad happened, as
 					// this should never happen
 					System.out.println("I am here");
 					try
 					{
 						Thread.sleep(5000);
 					} catch (Exception e)
 					{
 						
 					}
 					}
 			}
 		}
 		if(cardsToPass.size() > 3)
 		{
 			// this should never happen, just here to catch any error in case
 			// we are passing more than 3 cards
 			System.out.println("oh nos");
 			System.out.println("passing "+cardsToPass);
 			System.out.println("hand is "+myHand);
 			try
 			{
 				Thread.sleep(5000);
 			} catch (Exception e)
 			{
 				
 			}
 		}
 
 //		for (int i = myHand.size() - 1; i >= 13 - numCardsToPass; i--) {
 //			cardsToPass.add(myHand.get(i));
 //			Card card = myHand.get(i);
 //			if ((card.getSuit() == Card.Suit.Spades)
 //					&& (card.getFace() == Card.Face.Queen)) {
 //				hasQueenOfSpades = false;
 //			}
 //			getHand().remove(myHand.get(i));
 //		}
 		Set<Card> p1Set = getKnownCards().get(0);
 		Set<Card> p2Set = getKnownCards().get(1);
 		Set<Card> p3Set = getKnownCards().get(2);
 
 		if (passing == Passing.Left)
 			p1Set.addAll(cardsToPass);
 		else if (passing == Passing.Front)
 			p2Set.addAll(cardsToPass);
 		else if (passing == Passing.Right)
 			p3Set.addAll(cardsToPass);
 
 		return cardsToPass;
 	}
 
 	/**
 	 * The bulk of the Aggressive Agent's logic is in this method, which will
 	 * decide the best card to play each turn.
 	 * At a very high level, this is the logic used to determine the card to
 	 * play:
 	 * <li>If this is the very first turn, and you have the two of clubs, play it
 	 * <li>If someone has already played and you do not have the suit led:
 	 * <ol>If you have the Queen of Spades, play it
 	 * <ol>If you have any hearts, play the highest heart
 	 * <ol>Play the highest card in the suit with the fewest cards in your hand
 	 * <li>If someone has already played and you do have the suit led, figure out
 	 * the highest card that is lower than the current risk threshold.  If there
 	 * are no cards lower than the current risk threshold, play the highest card
 	 * you have in that suit
 	 * <li>If no one has already played:
 	 * <ol>Figure out the suit with the fewest cards (not including hearts)
 	 * <ol>Figure out the highest card in that suit below the current risk
 	 * threshold and play that card
 	 * <ol>If there are no cards in that suit below the current risk threshold,
 	 * check the suit with the second fewest, and then third fewest (again not
 	 * including hearts)
 	 * <ol>If there was still no card below the current risk threshold, determine
 	 * if hearts have already been broken.
 	 * <ol>If hearts have been broken, figure out the highest heart below the
 	 * current risk threshold and play that.
 	 * <ol>If hearts have been broken and there are no hearts below the current
 	 * risk threshold or hearts haven't been broken, pick the highest card in 
 	 * the suit with the fewest cards and play that.
 	 * 
 	 * 
 	 * @param cardsPlayed a List of Cards already played this turn
 	 * @param heartsBroken a boolean indicating if hearts have already been
 	 * broken in this game
 	 * @param veryFirstTurn a boolean indicating if this is the very first
 	 * turn of the game
 	 * @return the Card you are going to play
 	 * @see edu.txstate.hearts.model.Agent#playCard(java.util.List, boolean,
 	 * boolean)
 	 */
 	@Override
 	public Card playCard(List<Card> cardsPlayed, boolean heartsBroken,
 			boolean veryFirstTurn) {
 		Card cardToPlay = null;
 		String debugStr = "";
 		if (veryFirstTurn) {
 			cardToPlay = super.playTwoOfClub();
 		} else {
 			if (cardsPlayed.size() > 0) {
 				Suit suitLed = cardsPlayed.get(0).getSuit();
 				debugStr+=suitLed+" was led... ";
 				if (!hasAnyOfSuit(suitLed)) // doesn't have suit played on table
 				{
 					debugStr+="I don't have that suit... ";
 					if (hasQueenOfSpades) {
 						debugStr+="I have the queen of spades... ";
 						Iterator<Card> iterator = getHand().iterator();
 						while (iterator.hasNext()) {
 							Card card = iterator.next();
 							if ((card.getSuit() == Card.Suit.Spades)
 									&& (card.getFace() == Card.Face.Queen)) {
 								cardToPlay = card;
 								hasQueenOfSpades = false;
 								lastThreshold = null;
 							}
 						}
 					} else // no queen of spades, no suit on table
 					{
 						debugStr+="No queen of spades... ";
 						List<Card> hearts = getAllOfSuit(Suit.Hearts);
 						List<Card> spades = getAllOfSuit(Suit.Spades);
 						List<Card> diamonds = getAllOfSuit(Suit.Diamonds);
 						List<Card> clubs = getAllOfSuit(Suit.Clubs);
 						if (hearts.size() > 0) // has hearts in hand
 						{
 							debugStr+="But I do have hearts... ";
 							Collections.sort(hearts, new CardComparator());
 							Collections.reverse(hearts); // Every call to
 															// reverse ensures
 															// '0' is the
 															// highest card
 							cardToPlay = hearts.get(0);
 							debugStr+="I want to play "+cardToPlay;
 							lastThreshold = null;
 						} else // doesn't have any hearts
 						{
 							debugStr+="No hearts either... ";
 							List<Card> fewestCards = getCardsFromSuitWithFewest(
 									spades, diamonds, clubs);
 							cardToPlay = fewestCards.get(0);
 							debugStr="I guess I will play "+cardToPlay;
 							lastThreshold = null;
 						}
 					}
 
 				} else // does have suit played on table
 				{
 					debugStr+="I have that suit... ";
 					List<Card> playableCards = getAllOfSuit(suitLed);
 					Collections.sort(playableCards, new CardComparator());
 					Collections.reverse(playableCards);
 					cardToPlay = determineBestCard(playableCards, true);
 					debugStr+="So I will play "+cardToPlay;
 				}
 			}
 			/*
 			 * TODO We could use some improved logic around picking the best
 			 * card to play when there are no cards below the current risk
 			 * risk threshold.  Instead of just defaulting to the highest card
 			 * in the smallest suit, perhaps a better option is to find the
 			 * highest card within the risk threshold +10% or something?
 			 */
 			else // no cards played
 			{
 				debugStr+="No cards played yet... ";
 				List<Card> hearts = getAllOfSuit(Suit.Hearts);
 				List<Card> spades = getAllOfSuit(Suit.Spades);
 				List<Card> diamonds = getAllOfSuit(Suit.Diamonds);
 				List<Card> clubs = getAllOfSuit(Suit.Clubs);
 
 				List<Card> fewestCards = getCardsFromSuitWithFewest(spades,
 						diamonds, clubs);
 
 				if (fewestCards == null)
 					fewestCards = hearts;
 
 				Collections.sort(fewestCards, new CardComparator());
 				Collections.reverse(fewestCards);
 				cardToPlay = determineBestCard(fewestCards, false);
 				if (cardToPlay == null) {
 					//System.out.println("no good card to play from "+ fewestCards);
 					List<Card> first = fewestCards;
 					List<Card> second = null;
 					if (fewestCards == spades)
 						spades = Collections.EMPTY_LIST;
 					if (fewestCards == diamonds)
 						diamonds = Collections.EMPTY_LIST;
 					if (fewestCards == clubs)
 						clubs = Collections.EMPTY_LIST;
 					fewestCards = getCardsFromSuitWithFewest(spades, diamonds,
 							clubs);
 					second = fewestCards;
 					cardToPlay = determineBestCard(fewestCards, false);
 					if (cardToPlay == null) {
 						if (fewestCards == spades)
 							spades = Collections.EMPTY_LIST;
 						if (fewestCards == diamonds)
 							diamonds = Collections.EMPTY_LIST;
 						if (fewestCards == clubs)
 							clubs = Collections.EMPTY_LIST;
 						fewestCards = getCardsFromSuitWithFewest(spades,
 								diamonds, clubs);
 						cardToPlay = determineBestCard(fewestCards, false);
 						if (cardToPlay == null) {
 							if(heartsBroken)
 								cardToPlay = determineBestCard(hearts, false);
 							if (cardToPlay == null) {
 								// means we couldn't find a single card under
 								// risk threshold, so just play the highest
 								// card from the smallest suit 
 								// unless that suit is spades, and it would 
 								// force us to play the ace, king or queen of
 								// spades and the queen of spades is still out
 								// there
 								int counter = 0;
 								while(first.size() > counter && cardToPlay == null)
 								{
 									cardToPlay = first.get(counter++);
 									if(!isQosPlayed() && cardToPlay.getSuit() == Suit.Spades)
 										if(cardToPlay.getFace().ordinal() >= 10)
 											cardToPlay = null;
 								}
 								// means there isn't a spade we can play so get
 								// the highest card from the second highest suit
 								if(cardToPlay == null)
 								{
 									if(second == null)
 										// means we don't have another suit
 										cardToPlay = first.get(0);
 									else
 										cardToPlay = second.get(0);
 								}
 								//System.out.println("adding to expectedWins");
 								expectedWins += getProbCardWins(cardToPlay);
 							}
 						}
 					}
 				}
 			}
 		}
 		// if we got here, it means it went through everything and didn't
 		// figure out a card to play.  this should never happen, so if it does
 		// throw out some debugging information
 		if(cardToPlay == null)
 		{
 			System.out.println("card is null?");
 			System.out.println("hand is "+getHand());
 			System.out.println("cardsPlayed is "+cardsPlayed);
 			System.out.println(debugStr);
 			try
 			{
 				Thread.sleep(5000);
 			} catch (Exception e)
 			{
 				
 			}
 		}
 		if(!silent)
 		{
 			System.out.println("current hand - "+getHand());
 			System.out.println("picked to played - "+cardToPlay);
 		}
 		
 		getHand().remove(cardToPlay);
 
 		if ((cardToPlay.getSuit() == Card.Suit.Spades)
 				&& (cardToPlay.getFace() == Card.Face.Queen))
 			hasQueenOfSpades = false;
 		
 		return cardToPlay;
 	}
 
 	/**
 	 * This is where the magic happens.  This method will actually iterate
 	 * through the cards being passed in to figure out the highest card with 
 	 * an aggregate risk threshold lower than the current risk threshold.
 	 * The aggregate risk threshold is the probability that someone yet to
 	 * play does not have the suit led and could play hearts times the 
 	 * probability that the current card could win the trick
 	 * @param force a boolean indicating whether it is allowed to return null
 	 * if there are no cards that are under the risk threshold, or if it must
 	 * pick a card to return 
 	 * @param fewestCards the cards to check to find a card to play
 	 * @return the highest card under the risk threshold in the cards passed in,
 	 * if one exists, or else null if force is false
 	 */
 	private Card determineBestCard(List<Card> fewestCards, boolean force) {
 		boolean searching = true;
 		int counter = 0;
 		Card cardToPlay = null;
 		if ((fewestCards == null || fewestCards.size() == 0) && !force)
 			return null;
 		// System.out.println("fewestCards is "+fewestCards);
 		double canPlayHearts = ProbabilityUtils
 				.getProbabilityNoneOfSuitAndHasHearts(getHand(), fewestCards
 						.get(0).getSuit(), getPlayedCards(), getInPlayCards(),
 						getKnownCards(), getKnownEmpties(), isQosPlayed());
 		while (searching) // search all cards
 		{
 			Card test = fewestCards.get(counter++);
 			double cardWins = getProbCardWins(test);
 			if(!isQosPlayed() && test.getSuit()==Suit.Spades)
 			{
 				if(hasQueenOfSpades)
 				{
 					if(test.getFace().equals(Face.Queen))
 					{
 						if(cardWins != 0d && (!force || (force && fewestCards.size() > 1)))
 						{
 							canPlayHearts = 1d;
 							cardWins = 1d;
 						}
 					}
 				} else
 				{
 					if(cardWins != 0d && (test.getFace().ordinal() > 10) && (!force || (force && fewestCards.size() > 1)))
 					{
 						canPlayHearts = 1d;
 						cardWins = 1d;
 					}
 				}
 			}
 			double risk = canPlayHearts * cardWins;
 			double threshold;
 			if (getInPlayCards().size() == 3) {
 				threshold = 1d;
 				lastThreshold = null;
 				// System.out.println("using the new stuff");
 			} else {
 				Threshold t = thresholds.getThreshold(getInPlayCards().size(),
 						isQosPlayed());
 				threshold = t.getRiskThreshold();
 				lastThreshold = t;
 			}
 
 			//System.out.println("EVALUATING..." + test + ": cardWins is "
 			//		+ cardWins + " canPlayHearts is " + canPlayHearts
 			//		+ " total risk is " + risk + " and threshold is "
 			//		+ threshold);
 			if (risk < threshold) {
 				cardToPlay = test;
 				searching = false;
 				//System.out.println("adding to expectedWins");
 				expectedWins += cardWins;
 			} else // there is a risk of getting hearts
 			{
 				if (counter == fewestCards.size()) // all cards of smallest
 													// suite have been analyzed
 				{
 					if (force) {
 						cardToPlay = fewestCards.get(0); // just play the
 															// highest if at
 															// risk
 						//System.out.println("adding to expectedWins");
 						expectedWins += getProbCardWins(cardToPlay);
 					}
 					searching = false;
 				}
 			}
 		}
 		return cardToPlay;
 	}
 
 	/**
 	 * This method will find the suit with the lowest number of cards, aligning
 	 * with a strategy of removing as many suits as possible.
 	 * 
 	 * @param spades all of the cards in my hand that are spades
 	 * @param diamonds all of the cards in my hand that are diamonds
 	 * @param clubs all of the cards in my hand that are clubs
 	 * @return fewestCards, which is populated with the suit that has the least
 	 *         cards
 	 */
 	private List<Card> getCardsFromSuitWithFewest(List<Card> spades,
 			List<Card> diamonds, List<Card> clubs) {
 		List<Card> fewestCards = null;
 		int fewest = 14;
 		Collections.sort(clubs, new CardComparator());
 		Collections.reverse(clubs);
 		Collections.sort(diamonds, new CardComparator());
 		Collections.reverse(diamonds);
 		Collections.sort(spades, new CardComparator());
 		Collections.reverse(spades);
 		if (clubs.size() > 0 && clubs.size() < fewest) {
 			fewest = clubs.size();
 			fewestCards = clubs;
 		}
 		if (spades.size() > 0 && spades.size() <= fewest) {
 			if (fewest == spades.size()) {
 				if (spades.get(0).getFace().ordinal() > fewestCards.get(0)
 						.getFace().ordinal()) {
 					fewest = spades.size();
 					fewestCards = spades;
 				}
 			} else {
 				fewest = spades.size();
 				fewestCards = spades;
 			}
 		}
 		if (diamonds.size() > 0 && diamonds.size() <= fewest) {
 			if (fewest == diamonds.size()) {
 				if (diamonds.get(0).getFace().ordinal() > fewestCards.get(0)
 						.getFace().ordinal()) {
 					fewest = diamonds.size();
 					fewestCards = diamonds;
 				}
 			} else {
 				fewest = diamonds.size();
 				fewestCards = diamonds;
 			}
 
 		}
 		return fewestCards;
 	}
 
 	/**
 	 * Get the probability that a card will win.  If someone has already played
 	 * a card in this suit higher than my card, return 0.  If my card is not the
 	 * suit led, return 0.  Otherwise, this will figure out how 
 	 * many cards are remaining in play in the suit of this card, and how
 	 * many of those cards are less than my card.  Then return
 	 * 1-(cardsSmallerThanMine)/(totalCardsInThatSuit)
 	 * 
 	 * @param test the card to test to see if it will win
 	 * @return probability that the passed in card will win all cards played on
 	 *         the table as a double between 0 and 1
 	 */
 	private double getProbCardWins(Card test) {
 		List<Integer> availableCards = new ArrayList<Integer>();
 		// if there are already cards played, the strategy to know if
 		// you will win is different
 		if (this.getInPlayCards().size() > 0) {
 			// if test suit doesn't match suit led, then there is no chance of winning
 			if(test.getSuit() != getInPlayCards().get(0).getSuit())
 				return 0d;
 			int biggest = -1;
 			for(int i = 0; i < getInPlayCards().size(); i++)
 			{
 				Card c = getInPlayCards().get(i);
 				if(c.getSuit() == test.getSuit())
 				{
 					if(c.getFace().ordinal() > biggest)
 						biggest = c.getFace().ordinal();
 				}
 			}
 			// if the biggest card play is bigger than mine, I can't win
 			if (biggest > test.getFace().ordinal())
 				return 0d;
 			// if all three cards have been played, and my card is bigger
 			// than the biggest, I am guaranteed to win
 			else if(getInPlayCards().size() == 3)
 				return 1d;
 		}
 		// not everyone has played, so figure out how many cards out there
 		// are bigger than mine, and assume that I will win:
 		//      #CardsSmallerThanMine / #CardsAvailableCardsOfSuit 
 		for (int i = 0; i < 13; i++) {
 			availableCards.add(i);
 		}
 		Iterator<Card> iterator = getHand().iterator();
 		while (iterator.hasNext()) {
 			Card card = iterator.next();
 			if (card.getSuit() == test.getSuit())
 				availableCards.set(card.getFace().ordinal(), null);
 		}
 		Iterator<Card> iterator2 = getPlayedCards().iterator();
 		while (iterator2.hasNext()) {
 			Card card = iterator2.next();
 			if (card.getSuit() == test.getSuit())
 				availableCards.set(card.getFace().ordinal(), null);
 		}
 
 		List<Integer> newAvailableCards = new ArrayList<Integer>();
 		for (int i = 0; i < 13; i++) {
 			if (availableCards.get(i) != null) {
 				newAvailableCards.add(i);
 			}
 		}
 		availableCards = newAvailableCards;
 
 		if (availableCards.size() == 0)
 			return 1;
 
 		int counter = 0;
 		for (int i = 0; i < availableCards.size(); i++) {
 			if (availableCards.get(i) < test.getFace().ordinal())
 				counter++;
 		}
 		int numToPlay = 3;
 		numToPlay -= getInPlayCards().size();
 		for(int i = numToPlay-1; i >= 0; i--)
 		{
 			if(getKnownEmpties().get(i).contains(test.getSuit()))
 				numToPlay--;
 		}
 		int bigger = availableCards.size()-counter;
 		if(numToPlay > counter && bigger > 0)
 			return 0d;
 		//double factor = (double) numToPlay/3d;
 		double factor = 1d;
 		// System.out.println("there are "+counter+" cards less than yours out of "+availableCards.size());
 		double theyWin = (1d-((double) counter / (double) availableCards.size()))*factor;
 		return 1d-theyWin;
 
 	}
 
 	/**
 	 * Obtain all cards within a suit
 	 * 
 	 * @param suit the suit to check
 	 * @return cardsOfSuit, a list of cards that contains every card of a suit
 	 *         in current hand
 	 */
 	private List<Card> getAllOfSuit(Suit suit) {
 
 		List<Card> cardsOfSuit = new ArrayList<Card>();
 		Iterator<Card> iterator = getHand().iterator();
 		while (iterator.hasNext()) {
 			Card card = iterator.next();
 			if (card.getSuit() == suit)
 				cardsOfSuit.add(card);
 		}
 		return cardsOfSuit;
 	}
 
 	/**
 	 * Compare current cards against first card played on the table
 	 * 
 	 * @param suitLed
 	 * @return true or false. If true, at least one card in current hand matches
 	 *         the first card played
 	 */
 	private boolean hasAnyOfSuit(Suit suitLed) {
 		Iterator<Card> iterator = getHand().iterator();
 		while (iterator.hasNext()) {
 			Card c = iterator.next();
 			if (c.getSuit() == suitLed)
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Overrides the baseClass method just to check if I am being passed the
 	 * Queen of Spades, so I can set an internal boolean indicating that I
 	 * have the Queen of Spades
 	 * @param card the card being dealt to the Player
 	 * @see
 	 * edu.txstate.hearts.model.Player#addCard(edu.txstate.hearts.model.Card)
 	 */
 	@Override
 	public void addCard(Card card) {
 		super.addCard(card);
 		if ((card.getSuit() == Card.Suit.Spades)
 				&& (card.getFace() == Card.Face.Queen)) {
 			hasQueenOfSpades = true;
 		}
 	}
 
 	/**
 	 * The Abstract Agent class forces us to implement this method, but we
 	 * aren't using that at all.  So if it gets called here, throw an exception
 	 * @see edu.txstate.hearts.model.Agent#getCardsToPass()
 	 */
 	@Override
 	public List<Card> getCardsToPass() {
 		// we aren't actually using this, since we actually implemented the real
 		// algorithm
 		throw new UnsupportedOperationException(
 				"This method shouldn't have gotten called");
 	}
 
 	/**
 	 * Overrides the addPlayedCards method from Player to check to see if I
 	 * won the trick, and to adjust my risk thresholds appropriately.
 	 * @param cards the cards in this trick
 	 * @param tookCards a boolean indicating if I won the trick
 	 * @param num the turn number
 	 * @see edu.txstate.hearts.model.Player#addPlayedCards(java.util.Collection,
 	 * boolean)
 	 */
 	@Override
 	public void addPlayedCards(Collection<Card> cards, boolean tookCards, int num) {
 		// TODO Auto-generated method stub
 		super.addPlayedCards(cards, tookCards, num);
 		int numHearts = 0;
 		boolean hasQos;
 		Suit suitLed = null;
 		Iterator<Card> cardIterator = cards.iterator();
 		int counter = 0;
 		while (cardIterator.hasNext()) {
 			Card c = cardIterator.next();
 			if(suitLed == null)
 				suitLed = c.getSuit();
 			if(c.getSuit() != suitLed)
 			{
 				int playerNum = num+counter-getMyNum();
 				playerNum %= 4;
 				playerNum-=1;
 				if(playerNum >= 0)
 				{
 					getKnownEmpties().get(playerNum).add(suitLed);
 					//System.out.println("adding "+suitLed+" to "+playerNum+"'s empties");
 				}
 			}
 			if (c.getSuit() == Suit.Hearts)
 				numHearts++;
 			if (c.getSuit() == Suit.Spades && c.getFace() == Face.Queen)
 				// hasQos = true;
 				numHearts += 13;
 			Set<Card> p1Set = getKnownCards().get(0);
 			Set<Card> p2Set = getKnownCards().get(1);
 			Set<Card> p3Set = getKnownCards().get(2);
 			p1Set.remove(c);
 			p2Set.remove(c);
 			p3Set.remove(c);
 			counter++;
 		}
 		if (tookCards) {
 			actualWins++;
 			thresholds.decreaseThreshold(lastThreshold, numHearts);
 		} else {
 			thresholds.increaseThreshold(lastThreshold);
 		}
 	}
 
 	/**
 	 * Not really used anymore, but here for debugging.  This is used to determine
 	 * how many tricks the Agent expected to win based on the getProbCardWins
 	 * @return the expectedWins
 	 */
 	public double getExpectedWins() {
 		return expectedWins;
 	}
 
 	/**
 	 * Not really used anymore, but here for debugging.  This is used to determine
 	 * how many actual tricks the Agent won, which can then be compared to the
 	 * expected number of wins to see how well the expected wins is calculated,
 	 * since in the long run, these two numbers should be the same if expected
 	 * wins is being calculated accurately.
 	 * @return the actualWins
 	 */
 	public int getActualWins() {
 		return actualWins;
 	}
 
 }
