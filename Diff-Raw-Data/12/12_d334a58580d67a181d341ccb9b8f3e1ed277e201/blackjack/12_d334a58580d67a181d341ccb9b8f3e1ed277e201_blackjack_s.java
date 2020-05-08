 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.ArrayList;
 
 /** 
  * Small BlackJack game for fun
  * @author Andrew Knapp
  * @version 3.0 - Created card object to represent cards, simplified building of deck, made import libraries more
  * 			concise.
  */
 public class blackjack {
 
 	public static final int INITIAL_MONEY = 100;	
 	public static final String[] suites = {"Hearts", "Spades", "Clubs", "Diamonds"};
 	public static final String[] cards = {"Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King"};
 
 	public static void main(String[] arg)	{
 		Scanner console = new Scanner(System.in);
 		List<Card> newDeck = new ArrayList<Card>();
 		newDeck = buildDeck(suites, cards);
 		int money =	INITIAL_MONEY;
 		intro(money);
 		boolean play = true;
 
 		while (money > 0 && play == true) {
 
 			//Running card score
 			int playerTotal = 0;
 			int dealerTotal = 0;
 
 			//For future development (Split, Double Down, etc)
 			List<Card> playersCards = new ArrayList<Card>(); 
 			List<Card> dealersCards = new ArrayList<Card>();
 
 			//deck setup
 			newDeck = shuffleDeck(newDeck);
 			int roundBet = betAmount(money, console);
 
 			//Initial Deal
 			
 			System.out.print("First card: ");
 			playerTotal += drawCard(newDeck, playerTotal, playersCards);
 			
 			System.out.print("Second card: ");
 			playerTotal += drawCard(newDeck, playerTotal, playersCards);		
 			System.out.println();
 
 			System.out.println("Dealer showing: ");
 			dealerTotal += drawCard(newDeck, dealerTotal, dealersCards);
 			System.out.println("Dealer has: " + dealerTotal);
 
 			//Player play portion
 			boolean another_card = true;
 			while (playerTotal < 21 && another_card){
 					another_card = Hit(playerTotal, console);
 					if (playerTotal > 21 || playerTotal == 21 || !another_card) {
 						break;
 					} else {
 						playerTotal += drawCard(newDeck, dealerTotal, dealersCards);
 					}
 					
 					for(int i = 0; i < playersCards.size(); i++){
 						if (playersCards.get(i).isAce() && playerTotal > 21) {
 							playerTotal -= 10;
 						}
 					}		
 			}
 			
 			//Dealer play portion
 			while (dealerTotal < 17 && playerTotal < 21) {
 				System.out.println("Dealer showing: " + dealerTotal);
 				Card dealerCard = newDeck.remove(0);
 				
 				System.out.println("Dealer gets: ");
 				dealerCard.printCard();
 				dealerTotal += dealerCard.giveValue(dealerTotal);
 				dealersCards.add(dealerCard);
 				
 				//doesn't work
 				for(int i = 0; i < dealersCards.size(); i++){
 					if (dealersCards.get(i).isAce() && dealerTotal > 21) {
 						playerTotal -= 10;
 					}
 				}
 			}
 
 			//Decide who wins and whether to play another round
 			System.out.println();
 			money += winCheck(playerTotal, dealerTotal, roundBet);
 			play = playAgain(console, money);
 		}
 	}
 	
 	public static int drawCard(List<Card> newDeck, int playerTotal, List<Card> playersCards){
 		int total = 0;
 		Card playerCard1 = newDeck.remove(0);
 		playerCard1.printCard();
 		total += playerCard1.giveValue(playerTotal);
 		playersCards.add(playerCard1);
 		return total;
 	}
 	
 	/**
 	 * Constructs a deck given the suites and cards.
 	 * @param suites
 	 * @param name
 	 * @return a constructed deck
 	 */
 	public static List<Card> buildDeck(String[] suites, String[] name){
 		List<Card> deck = new ArrayList<Card>();
 		for (int i = 0; i < suites.length; i++){
 			for (int j = 0; j < name.length; j++){
 				Card k = new Card(name[j], suites[i]);
 				deck.add(k);
 			}
 		}
 		return deck;
 	}
 
 	/** 
 	 * Shuffles the cards so that they are in random order
 	 * @param deck of cards
 	 * @return a new shuffled deck
 	 */
 	public static List<Card> shuffleDeck(List<Card> deck){
 		List<Card> shuffledDeck = new ArrayList<Card>();
 		int r = 0;
 		while (deck.size() > 0){
 			Random card = new Random();
 			r = card.nextInt(deck.size());
 			Card temp = deck.remove(r);
 			shuffledDeck.add(temp);
 		}
 		return shuffledDeck;
 	}
 
 	/** 
 	 * Checks to see if the player or the dealer won
 	 * @param total running total of player's cards
 	 * @param dealer running total of dealer's cards
 	 * @param to_play
 	 * @return the money amount that the player has
 	 */
 	public static int winCheck(int total, int dealer, int to_play) {
 		int gains_losses = 0;
 		if (total == 21) {
 			System.out.println("You have: " + total);
 			System.out.println("You got BlackJack!  You win!");
 			gains_losses = 2 * to_play;
 		} else if (total > 21) {
 			System.out.println("You have: " + total);
 			System.out.println("You bust");
 			gains_losses = -1 * to_play;
 		} else if (total == dealer) {
 			System.out.println("You have: " + total);
 			System.out.println("Dealer has: " + dealer);
 			System.out.println("Push.");
 			gains_losses = 0;
 		} else if (dealer > 21) {
 			System.out.println("Dealer has: " + dealer);
 			System.out.println("Dealer busts.  You win!");
 			gains_losses = 2 * to_play;
 		} else if (total < dealer) {
 			System.out.println("You have: " + total);
 			System.out.println("Dealer has: " + dealer);
 			System.out.println("Dealer wins.");
 			gains_losses = -1 * to_play;
 		} else {
 			System.out.println("You have: " + total);
 			System.out.println("Dealer has: " + dealer);
 			System.out.println("You beat the dealer!");
 			System.out.println("You win!");
 			gains_losses = 2 * to_play;
 		}
 		return gains_losses;
 	}
 
     /** 
      * The bet amount for each round, subtracting from total
      * @param money money for each round
      * @param console allow for user interaction
      * @return the bet on the game
      */
    public static int betAmount(int money, Scanner console) {
 		System.out.println("How much would you like to bet?");
 		int bet = Math.abs(console.nextInt());
 		while(bet > money || bet < 10){
 			if (bet < 10) {
 				System.out.println("There is a minimum bet of $10");
 			} else {
 		   	System.out.println("You don't have that much money.");
 			}
 			System.out.println("How much would you like to bet?");
 			bet = console.nextInt();
 		}
 		return bet;
 	}
    	
 	/** 
 	 * To decide if they want to hit or not
 	 * @param running total of player's cards
 	 * @param console user interaction
 	 * @return true or false if the player wants to hit
 	 */
 	public static boolean Hit(int total, Scanner console){
 		boolean ans = false;
 		System.out.println();
 		System.out.println("You have: " + total); 
 		System.out.println("Do you want to hit?");
       String answer = console.next();
         if (answer.indexOf("y") == 0 || answer.indexOf("Y") == 0) {
             ans = true;
         } else if (answer.indexOf("n") == 0 || answer.indexOf("N") == 0) {
             ans = false;
         }	else {
             System.out.println();
             ans = false;
         }
 		return ans;
 	}	
 
 	/** 
 	 * Allows the user to decide if they are going to play again
 	 * @param console user interaction
 	 * @param money total money the player has
 	 * @return true or false if the user wants to play again
 	 */
 	public static boolean playAgain(Scanner console, int money){
 		boolean ans;
 		System.out.println("You have: $" + money);
 		if (money == 0) {
 			System.out.println("You're out of money.  House wins.");
 			ans = false;
 			return ans;
 		} 
 		System.out.println("Do you want to play again?");
 		String answer = console.next();
       if (answer.indexOf("y") == 0 || answer.indexOf("Y") == 0) {
       	ans = true;
 			return ans;
       } else if (answer.indexOf("n") == 0 || answer.indexOf("N") == 0) {
             ans = false;
 				if (money > 100) {
 					System.out.println("Congratulations! You won: $" + (money - 100));
 				} else {
 					System.out.println("You lost: $" + (100 - money));
 				}
 				return ans;
       } else {
       	System.out.println();
          ans = false;
       }
 		return ans;
 	}
 
	/** 
	 * Prints the introduction to the game
	 * @param money total money that the user is given
	 */
    public static void intro(int money) {
 		System.out.println("Welcome to BlackJack!");
 		System.out.println();
 		System.out.println("You have: $" + money);
    }
    
    /**
     * Card Object
     * @author acknapp
     */
    public static class Card{
 	   private int value;
 	   private String name;
 	   private String suite;
 	   private boolean Ace;
 	   
 	   /**
 	    * Card Constructor
 	    * @param name of card
 	    * @param suite of card
 	    */
 	   public Card(String name, String suite){
 		   this.name = name;
 		   this.suite = suite;
 		   this.value = determineCardValue(name);
 	   }
 	   
 	   /**
 	    * Prints the name of the card
 	    */
 	   public void printCard(){
 		   System.out.println(this.name + " of " + this.suite);
 	   	}
 	   
 	   /**
 	    * Gives the value of the card
 	    * @return value
 	    */
    		public int giveValue(int playerTotal){
    			return this.value;
    		}
    		
    		/**
    		 * Check if this card is an Ace
    		 * @return Ace
    		 */
    		public boolean isAce(){
    			return Ace;
    		}
 	   
 	   /**
 		 * Given the name of a card, determines the value of that card
 		 * @param name the given card
 		 * @return value of the card
 		 */
 		private int determineCardValue(String name) throws NumberFormatException{
 			int value = 0;
 			try{
 				value = Integer.parseInt(name.substring(0,1));
 				return value;
 			} catch (NumberFormatException e){
 				if (name.charAt(0) == 'K' || name.charAt(0) == 'J' || name.charAt(0) == 'Q' || name.charAt(0) == '0'){
 					value = 10;
 				} else if (name.charAt(0) =='A'){
 					value = 11;
 					this.Ace = true;
 				} else {
 					value = Integer.parseInt(name.substring(0, 1)); 
 				}
 				return value;
 			}
 		}
    }
}
