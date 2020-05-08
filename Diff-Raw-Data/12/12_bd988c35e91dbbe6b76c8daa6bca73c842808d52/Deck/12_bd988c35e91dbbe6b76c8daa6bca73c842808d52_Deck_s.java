 package edu.ncsu.csc216.solitaire.model;
 
 // changed this from java.util.linked to what is below...
 import edu.ncsu.csc216.solitaire.model.DeckLinkedList;
 import edu.ncsu.csc216.solitaire.ui.CardIcons;
 import edu.ncsu.csc216.solitaire.ui.UI;
 
 import java.util.Iterator;
 
 /**
  * The Deck of Cards
  * @author Andrew Kofink, William Blazer
  */
 public class Deck {
 	
 	private static final int JOKER1 = 27;
 	
 	private static final int JOKER2 = 28;
 	
 	/**
 	 * Final value for the size of the deck
 	 */
 	public static final int DECK_SIZE = 28;
 	
 	public static final int NUM_STEPS = 5;
 	
 	private int currentStep = 0;
 
 	/**
 	 * The Deck ArrayList
 	 * 
 	 */
 	private DeckLinkedList deck = new DeckLinkedList();
 	
 	/**
 	 * Creates the deck arrayList
 	 * @param deckArray Array with the deck values
 	 */
 	public Deck(int[] deckArray ) {
 		// checks the int[] deck for the existance of 1-28 before
 		// building it into linked list
 		boolean valueFound = false;	
 		//checks for values greater or less than 1-28
 		for (int j = 0; j < deckArray.length; j++) {
 			if (deckArray[j] < 1)  {
 				throw new IllegalArgumentException("A value below zero exists");
 			}
 			if ( deckArray[j] > 28) {
 				throw new IllegalArgumentException("A value above 28 exists");
 			}					
 		}		
 		//checks for duplicate values
 		if (deckArray.length != DECK_SIZE) {
 			throw new IllegalArgumentException("Not enough values in the deck!");
 		}
 		boolean[] exists = new boolean[DECK_SIZE + 1];
 		for (int i = 1; i < DECK_SIZE + 1; i++ ) {
 			exists[i] = false;
 		}
 		for (int i = 0; i < DECK_SIZE; i++) {
 			if (exists[deckArray[i]]) {
 				System.out.println("The value: " + deckArray[i] + " exists twice!");
 				throw new IllegalArgumentException("A duplicate value exists");
 			}	
 			exists[deckArray[i]] = true;
 		}
 		
 		//makes sure 1-28 exist in the deck
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
 		return value;
 	}
 	
 	/**
 	 * Step one
 	 */
 	private void stepOne() {
 		// find A Joker (value 27)
 		// swap it with the card in position below it
 		// ** if joker is position 28, then it circulates to position 1 **
 		
 		currentStep = 0;
 		deck.exchange(deck.indexOf(JOKER1) , deck.indexOf(deck.findNode(deck.indexOf(JOKER1)).next.data));
 		CardIcons.init().displayDeck(this);		
 	}
 	
 	/**
 	 * Step two
 	 */
 	private void stepTwo() {
 		// find B Joker (value 28)
 		// move it down 2 positions
 		// ** still circular, 28 connects back to 1 **
 		//swaps forward the first of two positions
 		
 		deck.exchange(deck.indexOf(JOKER2), deck.indexOf(deck.findNode(deck.indexOf(JOKER2)).next.data));
 		deck.exchange(deck.indexOf(JOKER2), deck.indexOf(deck.findNode(deck.indexOf(JOKER2)).next.data));
 		
 		CardIcons.init().displayDeck(this);
 	}
 	
 	/**
 	 * Step Three
 	 */
 	private void stepThree() {
 		// swap the top third of the deck with the bottom third of the deck, the two jokers denote the split points
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
 		
 		DeckLinkedList centerAndRight = deck.detachAt(deck.indexOf(topJoker));
 		DeckLinkedList left = deck;
 		DeckLinkedList right = new DeckLinkedList();
 		if (centerAndRight.indexOf(centerAndRight.findNode(centerAndRight.indexOf(bottomJoker)).next.data) != 0) {
 			right = centerAndRight.detachAt(centerAndRight.indexOf(centerAndRight.findNode(centerAndRight.indexOf(bottomJoker)).next.data));
 		}
 		DeckLinkedList center = centerAndRight;
 		
 		right.concat(center);
 		DeckLinkedList rightAndCenter = right;
 		rightAndCenter.concat(left);
 	
 		deck = rightAndCenter;
 		
 		CardIcons.init().displayDeck(this);
 	}
 	
 	/**
 	 * Step four
 	 */
 	private void stepFour() {
 		// get the value of the bottom card (position 27)
 		// move that number of cards from the top of the deck to the bottom
 		// replace the bottom card on the bottom again
 		// ** if bottom card value = 27 or 28 (a joker) then use 27 regardless *
 		
 		int last = deck.get(deck.size() - 1);
 		if (last == JOKER2) {
 			last = JOKER1;
 		}
 		DeckLinkedList whatIsLeft = deck.detachAt(last);
 		last = whatIsLeft.removeLast();
 		whatIsLeft.concat(deck);
 		deck = whatIsLeft;
 		deck.add(last);
 		
 		CardIcons.init().displayDeck(this);
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
 				
 		returnMe = deck.get(temp4);
 	
 		if (returnMe == 28) {
 			returnMe = 27;
 		}
 		
 		CardIcons.init().displayDeck(this);
 
 		String currentAnswer = CardIcons.init().getAnswerLabel();
 		CardIcons.init().setAnswerLabel(currentAnswer + translate(returnMe));
 		
 		currentStep++;
 		
 		return returnMe;
 	}
 	
 	private char translate(int value) {
 		return (char)('A' + value - 1);
 	}
 	
 	public int nextStep() {
 		switch(currentStep) {
 			case 0: stepOne(); System.out.println("exited step 1, current step:" + currentStep); break;
 			case 1: stepTwo(); System.out.println("exited step 2, current step:" + currentStep); break;
 			case 2: stepThree(); System.out.println("exited step 3, current step:" + currentStep); break;
 			case 3: stepFour(); System.out.println("exited step 4, current step:" + currentStep); break;
 			case 4: System.out.println("exited step 5, current step:" + currentStep); return stepFive();
 		}
 		currentStep++;
 		return -1;
 	}
 	
 	public int getCurrentStep() {
 		return currentStep;
 	}
 	
 	public void resetCurrentStep() {
 		currentStep = 0;
 	}
 	
 	/**
 	 * used to print the deck for debugging
 	 * @param deck2 deck to be printed
 	 */
 	public static void printDeck(DeckLinkedList deck2) { 
 		// below is a loop to print out the arraylist  for debugging purposes
 		//--------------------------
 		System.out.print("Deck: ");
 		for (int i = 0; i < 28; i++) {
 			System.out.print(deck2.get(i));
 			if (i != 27) {
 				System.out.print(", ");
 			}
 		}
 		System.out.println();
 		System.out.println("-----");
 	
 		
 		//----------------------
 	}
 	
 	public DeckLinkedList deck() {
 		return deck;
 	}
 }
