 package org.uncommons.poker.game.cards;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.uncommons.poker.game.Suit;
 import org.uncommons.util.ListUtils;
 
 /**
  * A {@link HandEvaluator} implementation that works on 7-card hands.  It assumes that any
  * 5 of the seven cards can be used to form the best 5-card hand.  This evaluator is suitable
  * for Texas Hold'em and 7-Card Stud but not for Omaha, which uses 4 hole cards and 5 community
  * cards (9 cards in total) and has restrictions on how cards may be combined to make a 5-card
  * hand.
  *
  * <i>For Omaha, first generate all valid 5-card combinations and then use
  * {@link FiveCardHandEvaluator}.  The same approach could also be used for Hold'em and
  * Stud but a 7-card evaluator is faster.</i>
  *
  * @author Daniel Dyer
  */
 public class SevenCardHandEvaluator implements HandEvaluator
 {
     /**
      * {@inheritDoc}
      */
     public RankedHand evaluate(List<PlayingCard> cards)
     {
         RankedHand straightOrFlushHand = rankStraightOrFlush(cards);
         RankedHand groupedHand = rankGroupedHand(cards);
         if (straightOrFlushHand != null                    
             && straightOrFlushHand.compareTo(groupedHand) > 0)
         {
             return straightOrFlushHand;
         }
         return groupedHand;
     }
 
 
     /**
      * Check for hand types that are constructed from groups (pairs, trips, quads)
      * of same rank cards.
      * @param cards Seven cards used to construct a 5-card hand.
      * @return The highest ranking (ignoring straights and flushes) for any 5-card
      * hand constructed from the seven cards.
      */
     private RankedHand rankGroupedHand(List<PlayingCard> cards)
     {
         // Counts how many pairs occur within a 7-card hand.  This is with replacement,
         // so one card can appear in multiple pairs (these are pairs in the Cribbage sense
         // rather than the poker sense).  This number of pairs maps to a particular poker
         // hand ranking.
         int pairs = 0;
         int runLength = 1;
         int biggestGroup = 1;
         int positioned = 0;
         FaceValue previousValue = cards.get(0).getValue();
         for (int i = 1; i < cards.size(); i++)
         {
             FaceValue value = cards.get(i).getValue();
             if (previousValue == value)
             {
                 ++runLength;
                 pairs += runLength - 1;
             }
             else
             {
                 if (runLength > biggestGroup)
                 {
                     // Make sure the biggest grouping is at the head of the list.
                    int start = i - runLength;
                     ListUtils.shiftLeft(cards, start, runLength, start);
                     positioned += runLength;
 
                     biggestGroup = runLength;
                 }
                 else if (runLength > 1 && positioned < RankedHand.HAND_SIZE - 1)
                 {
                     // And that the second biggest grouping follows it.
                     int start = i - (runLength - 1);
                     ListUtils.shiftLeft(cards, start, runLength, start - positioned);
                     positioned += runLength;
                 }
                 runLength = 1;
             }
             previousValue = value;
         }
         // Map the number of pairs to a hand ranking.
         HandRanking handRanking = mapPairsToRanking(pairs, biggestGroup);
         return new RankedHand(cards.get(0),
                               cards.get(1),
                               cards.get(2),
                               cards.get(3),
                               cards.get(4),
                               handRanking);
     }
 
     
     private HandRanking mapPairsToRanking(int pairs, int biggestGroup)
     {
         switch (pairs)
         {
             case 0 : return HandRanking.HIGH_CARD;
             case 1 : return HandRanking.PAIR;
             case 2 : return HandRanking.TWO_PAIR;
             case 3 : return biggestGroup == 3 ? HandRanking.THREE_OF_A_KIND : HandRanking.TWO_PAIR;
             case 4 :
             case 5 : return HandRanking.FULL_HOUSE;
             case 6 :
             case 7 :
             case 9 : return HandRanking.FOUR_OF_A_KIND;
             default : throw new IllegalArgumentException("Invalid pair count: " + pairs);
         }
     }
 
 
     /**
      * @return A ranked hand if these cards include a flush, straight flush or royal flush;
      * null otherwise.
      */
     private RankedHand rankStraightOrFlush(List<PlayingCard> cards)
     {
         List<PlayingCard> flushCards = filterFlushCards(cards);
         List<PlayingCard> straightCards = filterStraightCards(flushCards == null ? cards : flushCards);
         if (flushCards != null)
         {
             HandRanking ranking = HandRanking.FLUSH;
             // If the hand is also a straight, then this is more than just a flush...
             if (straightCards != null)
             {
                 flushCards = straightCards;
                 ranking = flushCards.get(0).getValue() == FaceValue.ACE ? HandRanking.ROYAL_FLUSH
                                                                         : HandRanking.STRAIGHT_FLUSH;
             }
             // We only need 5 cards to make a flush.
             while (flushCards.size() > RankedHand.HAND_SIZE)
             {
                 flushCards.remove(flushCards.size() - 1);
             }
 
             return new RankedHand(flushCards, ranking);
         }
         else if (straightCards != null)
         {
             return new RankedHand(straightCards, HandRanking.STRAIGHT);
         }
         return null;
     }
 
 
     /**
      * Takes a list of playing cards and if 5 or more of them have the same suit, they are returned.
      * The list returned will contain 5 or more cards or it will be null if there is no flush to be
      * made from the speficied cards.  The reason for potentially returning more than 5 cards is that,
      * because we haven't yet checked for a straight flush, we don't know whether we might still need
      * the lower ranked cards.
      */
     @SuppressWarnings("unchecked")
     private List<PlayingCard> filterFlushCards(List<PlayingCard> cards)
     {
         int[] suitCounts = new int[4];
         Suit flushSuit = null;
         for (PlayingCard card : cards)
         {
             int count = ++suitCounts[card.getSuit().ordinal()];
             if (count == RankedHand.HAND_SIZE)
             {
                 flushSuit = card.getSuit();
                 break;
             }
         }
 
         // If we have enough cards of one suit to make a flush, filter those cards into
         // a new list.
         if (flushSuit != null)
         {
             List<PlayingCard> flush = new ArrayList<PlayingCard>(RankedHand.HAND_SIZE);
             for (PlayingCard card : cards)
             {
                 if (card.getSuit() == flushSuit)
                 {
                     flush.add(card);
                 }
             }
             return flush;
         }
         return null;
     }
 
 
     private List<PlayingCard> filterStraightCards(List<PlayingCard> cards)
     {
         // Re-jig the list so that we can detect 5, 4, 3, 2, A as a straight too.
         PlayingCard highestCard = cards.get(0);
         if (highestCard.getValue() == FaceValue.ACE && cards.get(cards.size() - 1).getValue() == FaceValue.TWO)
         {
             cards = new ArrayList<PlayingCard>(cards);
             cards.add(highestCard); // Ace is also low.
         }
 
         List<PlayingCard> straightCards = new ArrayList<PlayingCard>(RankedHand.HAND_SIZE);
         straightCards.add(highestCard);
         PlayingCard previousCard = highestCard;
         for (int i = 1; i < cards.size(); i++)
         {
             PlayingCard card = cards.get(i);
             if (assertConsecutiveRanks(card, previousCard))
             {
                 straightCards.add(card);
                 if (straightCards.size() == RankedHand.HAND_SIZE)
                 {
                     return straightCards;
                 }
             }
             // If there are two consecutive cards of the same rank, skip over the second one.
             else if (card.getValue() != previousCard.getValue())
             {
                 // If we get to here, the card we're looking at is not part of a straight.
                 straightCards.clear();
                 straightCards.add(card);
             }
             previousCard = card;
         }
         return null;
     }
 
 
     /**
      * Returns true if the two cards would be next to each other in a straight, false otherwise.
      * The first card has to be one rank lower than the second in order for this method to return
      * true.  Also returns true when the first card is an ace and the second a two to account for
      * a low straight: 5, 4, 3, 2, A.
      */
     private boolean assertConsecutiveRanks(PlayingCard card1, PlayingCard card2)
     {
         return card1.getValue().ordinal() == card2.getValue().ordinal() - 1
                || (card1.getValue() == FaceValue.ACE && card2.getValue() == FaceValue.TWO);
     }
 }
