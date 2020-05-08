 package edu.ncsu.csc216.solitaire.model;
 
 import java.util.LinkedList;
 
 /**
  * The Deck of Cards
  * @author Andrew Kofink, William Blazer
  */
 public class Deck {
 
 	private LinkedList<Integer> deck = new LinkedList<Integer>();
 	
 	public Deck(int[] deckArray ) {
 		// checks the int[] deck for the existance of 1-28 before
 		// building it into linked list
 		boolean valueFound = false;
 		int j = 0;
 		
 		for (j = 1; j < 28; j++)
 			valueFound = false;
 			for (int i = 0; i < 27; i++) {
 				if (j == deckArray[i]) {
 					valueFound = true;
 				}
 			}
 			if (valueFound == false) {
 				throw new IllegalArgumentException("There deck is invalid, missing a number between 1 and 28");
 			}
 	
 	
 		// now the deckArray[] is turned into the actual linked list deck 
 	
		for (int k = 0; k < 27; k++) {
 			deck.add(deckArray[k]);
 		}
 	}
 	
 	public int getKeySteamValue()  {
 		
 		// find A Joker (value 27)
 		// swap it with the card in position below it
 		// ** if joker is position 28, then it circulates to position 1 **
 		int tempVal = deck.get(deck.indexOf(27)-1);
 		int jokerIndexA = deck.indexOf(27);
 		deck.set(jokerIndexA-1,27);
 		deck.set(jokerIndexA,tempVal);
 		
 		
 		// find B Joker (value 28)
 		// move it down 2 positions
 		// ** still circular, 28 connects back to 1 **
 		int tempVal2 = deck.get(deck.indexOf(28)-2);
 		int jokerIndexB= deck.indexOf(28);
 		deck.set(jokerIndexB-2,28);
 		deck.set(jokerIndexB,tempVal2);
 		
 		// swap the top third of the deck with the bottom third of the deck
 		// the two jokers denote the split points
 		
 		// get the value of the bottom card (position 27)
 		// move that number of cards from the top of the deck to the bottom
 		// replace the bottom card on the bottom again
 		// ** if bottom card value = 27 or 28 (a joker) then use 27 regardless *
 		int temp3 = deck.get(27);
 		for (int i = 0; i < temp3; i++) {
 			deck.addLast(0);
 		}
 		deck.addLast(deck.indexOf(temp3));
 						
 		// ** read the top cards value (28 or 27 both are 27 again) **
 		// go down into the deck that many cards
 		// return the value of the next card
 		int temp4 = deck.get(0);
 		int returnMe = deck.get(temp4 + 1);
 		
 		return returnMe;
 	}
 }
