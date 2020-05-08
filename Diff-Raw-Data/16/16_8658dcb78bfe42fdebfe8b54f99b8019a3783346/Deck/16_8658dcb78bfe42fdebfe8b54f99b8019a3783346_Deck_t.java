 package edu.ncsu.csc216.solitaire.model;
 
 import java.util.LinkedList;
 
 /**
  * The Deck of Cards
  * @author Andrew Kofink, William Blazer
  */
 public class Deck {
 
 	/**
 	 * 
 	 */
 	private LinkedList<Integer> deck = new LinkedList<Integer>();
 	
 	/**
 	 * @param deckArray
 	 */
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
 	
 		for (int k = 0; k < 28; k++) {
 			deck.add(deckArray[k]);
 		}
 	}
 	
 	/**
 	 * @return
 	 */
 	public int getKeySteamValue()  {
 		
 		stepOne();
 		stepTwo();
 		stepThree();
 		stepFour();
 		return stepFive();
 	}
 	
 	/**
 	 * 
 	 */
 	private void stepOne() {
 		// find A Joker (value 27)
 		// swap it with the card in position below it
 		// ** if joker is position 28, then it circulates to position 1 **
 		int tempVal = 0;
 		//System.out.println("B joker position1: " + deck.indexOf(28));
 		if (deck.indexOf(27)+1 == 28) {
 			tempVal = deck.get(0);
 		}
 		else {
 			tempVal = deck.get(deck.indexOf(27)+1);
 		}
 		//System.out.println("B joker position2: " + deck.indexOf(28));
 		int jokerIndexA = deck.indexOf(27);
 		if (jokerIndexA + 1 == 28) {
 			deck.set(0,27);
 		}
 		else {
 			deck.set(jokerIndexA + 1,27);
 		}
 		//System.out.println("B joker position3: " + deck.indexOf(28));
 		deck.set(jokerIndexA,tempVal);
 	}
 	
 	/**
 	 * 
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
 		
 		int jokerIndexB= deck.indexOf(28);
 		if  (jokerIndexB + 1 > 27) {
 			deck.set(jokerIndexB + 1 - 28,28);
 		}
 		else {
 			deck.set(jokerIndexB + 1,28);
 		}
 		deck.set(jokerIndexB,tempVal2);
 				
 		//swaps forward the first of two positions
 		tempVal2 = 0;
 		if (deck.indexOf(28) + 1 > 27) {
 			tempVal2 = deck.get(deck.indexOf(28) + 1 - 28);
 		}
 		else {
 			tempVal2 = deck.get(deck.indexOf(28) + 1);
 		}
 		
 		jokerIndexB= deck.indexOf(28);
 		if  (jokerIndexB + 1 > 27) {
 			deck.set(jokerIndexB + 1 - 28,28);
 		}
 		else {
 			deck.set(jokerIndexB + 1,28);
 		}
 		deck.set(jokerIndexB,tempVal2);
 	}
 	
 	/**
 	 * 
 	 */
 	private void stepThree() {
 		// swap the top third of the deck with the bottom third of the deck
 		// the two jokers denote the split points
 		printDeck(deck);
 		
 		int cutJokerA = deck.indexOf(27);
 		int cutJokerB = deck.indexOf(28);
 		int bottomCutOff = deck.get(27);
 		
 		int topJoker = 0;
 		int bottomJoker = 0;
 		if (cutJokerA < cutJokerB) {
 			topJoker = cutJokerA;
 			bottomJoker = cutJokerB;
 		}
 		else {
 			topJoker = cutJokerB;
 			bottomJoker = cutJokerA;
 		}
 		System.out.println(topJoker);
 		System.out.println(bottomJoker);
 		
 		for (int i = 0; i < topJoker; i++) {
 			//move top value to bottom of deck and then delete
			deck.addLast(deck.get(0));
			deck.remove(0);
 		}
 		
 		printDeck(deck);
 		
 		for (int i = bottomJoker; i > bottomCutOff; i--) {
 			//move values between bottom joker and the decks original bottom to the top and delete
			deck.addFirst(deck.get(bottomJoker + 1));
			deck.remove(bottomJoker + 1);
 		}
 		
 		printDeck(deck);
 	}
 	
 	/**
 	 * 
 	 */
 	private void stepFour() {
 		// get the value of the bottom card (position 27)
 		// move that number of cards from the top of the deck to the bottom
 		// replace the bottom card on the bottom again
 		// ** if bottom card value = 27 or 28 (a joker) then use 27 regardless *
 		int temp3 = deck.get(27);
 		for (int i = 0; i < temp3; i++) {
 			deck.addLast(deck.get(0));
 			deck.remove(0);
 		}
 		deck.addLast(deck.indexOf(temp3));
 		deck.remove(deck.indexOf(temp3));
 	}
 	
 	/**
 	 * @return
 	 */
 	private int stepFive() {
 		// ** read the top cards value (28 or 27 both are 27 again) **
 		// go down into the deck that many cards
 		// return the value of the next card
 		int temp4 = deck.get(0);
 		if (temp4 == 28) {
 			temp4 = 27;
 		}
 		
 		int returnMe = 0;
 		if (temp4 + 1 > 27) {
 			returnMe = deck.get(temp4 + 1 - 27);
 		}
 		else {
 			returnMe = deck.get(temp4 + 1);
 		}
 	
 		System.out.println("Keystream Value returned: " + returnMe);
 		//printDeck(deck);
 		if (returnMe == 28) {
 			returnMe = 27;
 		}
 		return returnMe;
 	}
 	
 	/**
 	 * @param deck2
 	 */
 	public static void printDeck(LinkedList<Integer> deck2) { 
 		// below is a loop to print out the arraylist  for debugging purposes
 		//--------------------------
 		System.out.print("Deck: ");
 		for (int i = 0; i < 28; i++) {
 			System.out.print(deck2.get(i) + " ");
 		}
 		System.out.println();
 		System.out.println("-----");
 		
 		
 		//----------------------
 	}
 }
