 package blackjack;
 
 /*
  * stuff we need to add
  * 
  * -place bet
  * -rules for "double bet"
  * -splitting hand
  * -add payout method
  * -input error checking
  */
 
 
 import java.util.Scanner;
 import java.util.Vector;
 
 public class Game {
 	public static Scanner scan = new Scanner(System.in);
 	
 	private Deck deck;
 	private Vector<Player> players;
 	private Dealer dealer;
 	
 	public Game(Deck deck, Dealer dealer, Vector<Player> players) {
 		this.deck = deck;
 		this.players = players;
 		this.dealer = dealer;
 		//listPlayers();
 		play();
 	}
 
 	public static void main(String[] args) {
		Vector<Player> players = makePlayerList();
 		Dealer dealer = new Dealer();
 		
 		String temp = "";
 		boolean playAgain = true;
 		
 		do {
 			new Game(new Deck(), dealer, players);
 			
 			System.out.print("Would you like to play again (yes or no)? ");
 			temp = scan.next();
 			
 			if (temp.equalsIgnoreCase("no")) {
 				playAgain = false;
 			}
 			
 		} while (playAgain);
 	}
	
	public Vector<Player> getPlayerList() {
		return players;
	}
 
	public static Vector<Player> makePlayerList() {
 		final int MAX_PLAYERS = 3, MIN_PLAYERS = 1;
 		Vector<Player> playerList = new Vector<Player>();
 		int numPlayers;
 		String playerName;
 
 
 		System.out.print("Welcome to Blackjack 2.0! \nHow many people are playing (1-3)? ");
 
 		do {
 			numPlayers = scan.nextInt();
 			if (numPlayers < MIN_PLAYERS)
 				System.out.printf("Sorry, you must have at least %d player.\nHow many people are playing? ", MIN_PLAYERS);
 			else if (numPlayers > MAX_PLAYERS)
 				System.out.printf("Sorry, you cannot have more than %d players.\nHow many people are playing? ", MAX_PLAYERS);
 		} while (numPlayers < MIN_PLAYERS || numPlayers > MAX_PLAYERS);
 
 		for (int i = 0; i < numPlayers; i++) {
 			System.out.printf("What is player %d's name?  ", i + 1);
 			playerName = scan.next();
 
 			playerList.add(new Player(playerName));
 		}
 
 		return playerList;
 	}
 	
 
 	private void listPlayers() {
 		for (Player s : players) {
 			System.out.println(s.getName());
 		}
 	}
 	
 	public Dealer getDealer() {
 		return dealer;
 	}
 
 	private void play() {
 		for (Player p : players) {
 			p.removeHand();
 		}
 
 		dealer.removeHand();
 
 		deck.shuffleDeck();
 
 		deck.deal(this);
 		
 		System.out.println(dealer.getName() + "'s card is " + dealer.getHand().getCard(0) + ".");
 		
 		for (Player p : players) {
 			p.playHand(deck);
 		}
 		dealer.playHand(deck);
 
 		for (Player p : players) {
 			if (p.getHand().getPoints() > 21) {
 				System.out.println(p.getName() + " loses!");
 			} else if (dealer.getHand().getPoints() > 21) {
 				System.out.println(p.getName() + " wins!");
 			} else if (p.getHand().getPoints() > dealer.getHand().getPoints()) {
 				System.out.println(p.getName() + " wins!");
 			} else if (p.getHand().getPoints() == dealer.getHand().getPoints()) {
 				System.out.println(p.getName() + " pushes.");
 			} else {
 				System.out.println(p.getName() + " loses!");
 			}
 		}
 	}
 }
