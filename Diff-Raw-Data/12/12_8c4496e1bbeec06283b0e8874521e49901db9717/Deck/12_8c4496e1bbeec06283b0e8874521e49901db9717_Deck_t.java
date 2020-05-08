 package edu.ncsu.csc216.solitaire.model;
 
 import java.util.LinkedList;
 
 /**
  * The Deck of Cards
  * @author Andrew Kofink, William Blazer
  */
 public class Deck {
 	
 	private static final int JOKER1 = 27;
 	
 	private static final int JOKER2 = 28;
 	
 	public static final int DECK_SIZE = 27;
 
 	/**
 	 * The Deck ArrayList
 	 */
 	private LinkedList<Integer> deck = new LinkedList<Integer>();
 	
 	/**
 	 * Creates the deck arrayList
 	 * @param deckArray Array with the deck values
 	 */
 	public Deck(int[] deckArray ) {
 		// checks the int[] deck for the existance of 1-28 before
 		// building it into linked list
 		boolean valueFound = false;
 		
 		for (int j = 1; j <= 28; j++) {
 			valueFound = false;
 			for (int i = 0; i < deckArray.length; i++) {
 				if (j == deckArray[i]) {
 					valueFound = true;
 				}
 			}
 			if (!valueFound) {
 				throw new IllegalArgumentException("There deck is invalid, missing a number between 1 and 28");
 			}
 		}
 	
 		// now the deckArray[] is turned into the actual linked list deck 
 	
 		for (int k = 0; k < 28; k++) {
 			deck.add(deckArray[k]);
 		}
 	}
 	
 	/**
 	 * returns the next keystream value
 	 * @return keyStream Valye
 	 */
 	public int getKeystreamValue()  {
 		
 		stepOne();
 		stepTwo();
 		stepThree();
 		stepFour();
 		int value = stepFive();
 		System.out.println(value);
 		return value;
 	}
 	
 	/**
 	 * Step one
 	 */
 	private void stepOne() {
 		// find A Joker (value 27)
 		// swap it with the card in position below it
 		// ** if joker is position 28, then it circulates to position 1 **
 		int tempVal = 0;
 		if (deck.indexOf(27) + 1 == 28) {
 			tempVal = deck.get(0);
 		}
 		else {
 			tempVal = deck.get(deck.indexOf(27) + 1);
 		}	
 		int jokerIndexA = deck.indexOf(27);
 		if (jokerIndexA + 1 == 28) {
 			deck.set(0 , 27);
 		}
 		else {
 			deck.set(jokerIndexA + 1,27);
 		}		
 		deck.set(jokerIndexA , tempVal);
 	}
 	
 	/**
 	 * Step two
 	 */
 	private void stepTwo() {
 		// find B Joker (value 28)
 		// move it down 2 positions
 		// ** still circular, 28 connects back to 1 **
 				
 		//swaps forward the first of two positions
 		int tempVal2 = 0;
 		if (deck.indexOf(28) + 1 > 27) {
 			tempVal2 = deck.get(deck.indexOf(28) + 1 - 28);
 		}
 		else {
 			tempVal2 = deck.get(deck.indexOf(28) + 1);
 		}
 		
 		int jokerIndexB = deck.indexOf(28);
 		if  (jokerIndexB + 1 > 27) {
 			deck.set(jokerIndexB + 1 - 28,28);
 		}
 		else {
 			deck.set(jokerIndexB + 1,28);
 		}
 		deck.set(jokerIndexB , tempVal2);
 				
 		//swaps forward the first of two positions
 		tempVal2 = 0;
 		if (deck.indexOf(28) + 1 > 27) {
 			tempVal2 = deck.get(deck.indexOf(28) + 1 - 28);
 		}
 		else {
 			tempVal2 = deck.get(deck.indexOf(28) + 1);
 		}
 		
 		jokerIndexB = deck.indexOf(28);
 		if  (jokerIndexB + 1 > 27) {
 			deck.set(jokerIndexB + 1 - 28,28);
 		}
 		else {
 			deck.set(jokerIndexB + 1,28);
 		}
 		deck.set(jokerIndexB , tempVal2); 
 	}
 	
 	/**
 	 * Step Three
 	 */
 	private void stepThree() {
 		// swap the top third of the deck with the bottom third of the deck
 		// the two jokers denote the split points
 				
 		int cutJokerA = deck.indexOf(27);
 		int cutJokerB = deck.indexOf(28);
 		
 		int topJoker = 0;
 		int bottomJoker = 0;
 		if (cutJokerA < cutJokerB) {
 			topJoker = JOKER1;
 			bottomJoker = JOKER2;
 		}
 		else {
 			topJoker = JOKER2;
 			bottomJoker = JOKER1;
 		}
 		//System.out.println(deck.indexOf(topJoker));
 		//System.out.println(deck.indexOf(bottomJoker));
 		
 		int value = deck.get(DECK_SIZE);
 		if (deck.get(0) != 27 && deck.get(0) != 28) {
 			value = deck.get(0);
 		}
 		while (deck.indexOf(topJoker) != 0) {
 			//move top value to bottom of deck and then delete
 			deck.addLast(deck.get(0));
 			deck.remove(0);
 		}
 		
		while (deck.indexOf(value) != deck.indexOf(bottomJoker) + 1 && deck.indexOf(bottomJoker) != deck.indexOf(value)) {
 			//move values between bottom joker and the decks original bottom to the top and delete
 			
 			if (deck.indexOf(value) == 0) {
 				deck.addFirst(deck.get(deck.indexOf(value) + DECK_SIZE - 1));
 			} else {
 				deck.addFirst(deck.get(deck.indexOf(value) - 1));
 			}
 			
 			if (deck.indexOf(value) == 0) {
 				deck.remove(deck.indexOf(value) + DECK_SIZE - 1);
 			} else {
 				deck.remove(deck.indexOf(value) - 1);
 			}
 		}			
 	}
 	
 	/**
 	 * Step four
 	 */
 	private void stepFour() {
 		// get the value of the bottom card (position 27)
 		// move that number of cards from the top of the deck to the bottom
 		// replace the bottom card on the bottom again
 		// ** if bottom card value = 27 or 28 (a joker) then use 27 regardless *
 		
 		//System.out.print("Before: ");
 				
 		int temp3 = deck.get(27);
 		deck.remove(27);
 		for (int i = 0; i < temp3; i++) {
 			deck.addLast(deck.get(0));
 			deck.remove(0);
 		}
 		deck.addLast(temp3);
 				
 		//System.out.print("After:  ");
 		
 	}
 	
 	/**
 	 * fifth step
 	 * @return the value for keyValue
 	 */
 	private int stepFive() {
 		// ** read the top cards value (28 or 27 both are 27 again) **
 		// go down into the deck that many cards
 		// return the value of the next card
 		//printDeck(deck);
 		
 		int temp4 = deck.get(0);
 		if (temp4 == 28) {
 			temp4 = 27;
 		}
 		
 		int returnMe = 0;
 		/*
 		if (temp4 + 1 > 27) {
 			returnMe = deck.get(temp4 + 1 - 27);
 		}
 		else {
 			returnMe = deck.get(temp4 + 1);
 		}
 		*/
 		
 		returnMe = deck.get(temp4);
 	
 		//System.out.println("Keystream Value returned: " + returnMe);
 		//printDeck(deck);
 		if (returnMe == 28) {
 			returnMe = 27;
 		}
 		//printDeck(deck);
 		//System.out.println("Return value: " + returnMe);
 		
 		return returnMe;
 	}
 	
 	/**
 	 * used to print the deck for debugging
 	 * @param deck2 deck to be printed
 	 */
 //	public static void printDeck(LinkedList<Integer> deck2) { 
 //		// below is a loop to print out the arraylist  for debugging purposes
 //		//--------------------------
 //		System.out.print("Deck: ");
 //		for (int i = 0; i < 28; i++) {
 //			System.out.print(deck2.get(i) + " ");
 //		}
 //		System.out.println();
 //		System.out.println("-----");
 //		
 //		
 //		//----------------------
 //	}
 }
