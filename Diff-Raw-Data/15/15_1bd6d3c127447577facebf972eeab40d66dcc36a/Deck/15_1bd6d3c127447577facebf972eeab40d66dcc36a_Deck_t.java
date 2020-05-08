 package com.game.pileon;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Random;
 
 import android.util.Log;
 
 /**
  * 
  * @author nscross, breeze4
  * 
  */
 public class Deck
 {
 
 	private LinkedList<Card> DeckOfCards;
 
 	public Deck()
 	{
 		DeckOfCards = new LinkedList<Card>();
 	}
 
 	public void AddCard(Card cardToAdd)
 	{
 		if (DeckOfCards != null)
 		{
 			DeckOfCards.add(cardToAdd);
 		}
 	}
 
 	/**
 	 * Shuffle
 	 * 
 	 * Shuffles the deck like a little kid would, very scientific
 	 */
 	public void Shuffle()
 	{
 		if (DeckOfCards != null && DeckOfCards.size() > 0)
 		{
 			Random randomGenerator = new Random();
 
 			int howManyTimesToShuffle = DeckOfCards.size(); 
 			int howManyCardsInDeck = DeckOfCards.size(); // save this now, it's
 															// not going to
 															// change
 
 			for (int shuffleCount = 0; shuffleCount < howManyCardsInDeck; shuffleCount++)
 			{
 				int whichCardToMove = (randomGenerator.nextInt(howManyTimesToShuffle));
 				Card tempCard = DeckOfCards.remove(whichCardToMove);
 				DeckOfCards.addLast( tempCard );
				Log.d("PO Shuffle", tempCard.toString());
 				howManyTimesToShuffle--;
 			}
 		}
 
 	}
 	public String toString()
 	{
 		Iterator<Card> ListIter = DeckOfCards.iterator();
 		String deckString = new String();
 		while ( ListIter.hasNext() )
 		{
			deckString = deckString + " " + ListIter.next().toString();
			
 		}
 		return deckString;
 	}
 
 }
