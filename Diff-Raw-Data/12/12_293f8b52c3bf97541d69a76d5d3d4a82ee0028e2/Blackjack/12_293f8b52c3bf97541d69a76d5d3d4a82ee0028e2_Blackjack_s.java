 package game;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 
 import cards.*;
 import cards.Card.Rank;
 import participant.Player;
 import cards.Hand;
 import storage.Storage;
 
 /**
 *
 * @author JackGivesBack
 */
 public class Blackjack implements Game {
  /**
   * Deal exactly 2 cards for the first initial deal to a player
   */
  private final int INITIAL_DEAL_VALUE = 2;
  
  private final int MAX_HAND_SIZE = 2;
  /**
   * Number of decks to use
   */
  private final int NUM_DECKS = 6;
  /**
  * Number of cards left to trigger new deck
  */
  private final int DECK_RESET_VALUE = 104;
  /**
   * Field to store chip count
   */
  public int chipCount;
  /**
   * Initialize new deck
   */
  private Deck gameDeck;
  /**
   * Blackjack game initialization
   */
 
  public Blackjack() {
   gameDeck = new Deck();
   gameDeck.addDeck(NUM_DECKS);
   gameDeck.shuffle();
  }
 
  /**
   * Reset player's hand state
   * @param pHand A given BlackjackHand of a player
   */
  public void resetHandState(BlackjackHand pHand) {
   pHand = new BlackjackHand();
  }
 
  /**
   * Gets the game's deck
   * @return Game deck
   */
  public Deck getDeck() {
   return this.gameDeck;
  } 
  
  public void setDeck(Deck deck){
 	 this.gameDeck=deck;
  }
 
  
  /**
   * Check if Deck is low on cards
   * @param
   * @return True if Deck is low on cards, false otherwise
   */
  public void checkDeck() {
   if ( gameDeck.size() < DECK_RESET_VALUE ) {
    gameDeck = new Deck();
    gameDeck.addDeck(NUM_DECKS);
    gameDeck.shuffle();
   }
  }
 
  /**
   * Deals the initial amount of cards to a BlackjackHand
   * @param pHand BlackjackHand to deal
   */
  public void deal(BlackjackHand pHand) {
   /*
    * Clear player's hand first
    */
   resetHandState(pHand);
   /*
    * Check deck status
    */
   checkDeck();
   /*
    * Deal exactly two cards for the first initial deal
    */
   for ( int i = 0 ; i < INITIAL_DEAL_VALUE ; i++ ){
    pHand.addCard(gameDeck.draw());
   }
  }
 
  /**
   * Adds a card to a Hand
   * @param pHand BlackjackHand to add card to
   * @return
   */
  public void hit(BlackjackHand pHand) {
   /*
    * Check deck status
    */
   checkDeck();
   /*
    * Check if player's hand state is done or not
    */
   if ( pHand.isPlayable() )
    /*
     * Draws a card from the Deck and adds it to the Hand
     */
    pHand.addCard(gameDeck.draw());
  }
 
  /**
   * Set player's Hand state to be done
   * @param pHand BlackjackHand to set state
   * @return
   */
  public void stand(BlackjackHand pHand) {
   pHand.setDone();
  }
 
  /**
   *
   * @param pHand BlackjackHand to apply doubledown to
   * @return
   */
  public void doubleDown(BlackjackHand pHand) {
   /*
    * Check deck status
    */
   
   
   checkDeck();

   hit(pHand);
   stand(pHand);
  }
 
  /**
   * Splits a pair into two separate BlackjackHands
   * @param pHand BlackjackHand to apply split to
   * @return Returns a BlackjackHand that has one of the pairs in pHand
   */
  
 public ArrayList<BlackjackHand> split(BlackjackHand pHand) {
   ArrayList<BlackjackHand> bothHands = new ArrayList<BlackjackHand>();
   if (pHand.checkPair() == false) {
    System.out.println("You dont have a pair! Select a different move");
  
   } else {
    /*
     * Clones the BlackjackHand
     */
 	  while(pHand.getNumberCards()<=2){
    BlackjackHand newHand =  pHand.clone();
    
    /*
     * Remove first card in given hand and second card in cloned hand
     */
    
    pHand.removeCard(0);
    pHand.addCard(gameDeck.draw());
    newHand.removeCard(1);
    newHand.addCard(gameDeck.draw());
    bothHands.add(pHand);
    bothHands.add(newHand);
    return bothHands;
 	  }
    
   }
   return bothHands;
   
 
  }
  
  public void playsSplitHands(ArrayList<BlackjackHand> bothHands) {
   boolean invalidInput = false;
   for( int i=0; i<MAX_HAND_SIZE;i++){
    
    Scanner keyboard=new Scanner(System.in);
    
 //   while (invalidInput == false) {
    while (i==0 && bothHands.get(i).isPlayable() && !(bothHands.get(i).isBust())) {
 	System.out.println("FirstHand:");   
 	System.out.println(bothHands.get(0).toString());
     System.out.println("For your First hand, do you want to hit or stand");
     String s = keyboard.next();
     if (s.equals("hit")){
      this.hit(bothHands.get(i));
      System.out.println("hand update: " +bothHands.get(i).toString());
      invalidInput = true;
     }
     else if (s.equals("stand")){
      this.stand(bothHands.get(i));
      System.out.println("final hand: "+bothHands.get(i).toString());
      invalidInput = true;
    
     }  else {
      System.out.println("Invalid input, enter hit/stand: ");
     }
    }
    
    while (i==1 && bothHands.get(i).isPlayable() && !(bothHands.get(i).isBust())) {
 	   System.out.println("Second Hand: ");  
 	   System.out.println(bothHands.get(1).toString());
 	    System.out.println("For your Second hand, do you want to hit or stand");
 	    String s = keyboard.next();
 	    if (s.equals("hit")){
 	     this.hit(bothHands.get(i));
 	     System.out.println("hand update: " +bothHands.get(i).toString());
 	     invalidInput = true;
 	    }
 	    else if (s.equals("stand")){
 	     this.stand(bothHands.get(i));
 	     System.out.println("final hand: "+bothHands.get(i).toString());
 	     invalidInput = true;
 	   
 	    }  else {
 	     System.out.println("Invalid input, enter hit/stand: ");
 	    }
 	   }
    
   }
  }
 }
  
