 package poker;
 
 /**
  * Poker
  * 
  * The full poker game. Includes menu and AI and the whole lot
  * 
  * @author Paul Steele
  *
  */
 public class Poker implements Runnable{
 	public final int PLAYERS;
 	private int currentPlayer; //contains who the current player is
 	private Player[] players;
 	private final int STARTING_CASH = 500;
 	private Deck deck; //the deck of the game
 	private Hand community; //the community cards
 	private int bid; //the highest bid
 	private int pot; //what's in the pot
 	private int dealer;
 	public static final int BIG_BLIND = 10; //the big blind
 	private static Object lock;
 	public static Object dropbox; //used to store values to be passed between threads
 	private Window win;
 	/**
 	 * main()
 	 * 
 	 * calls menu and run game
 	 * @param args
 	 */
 	public static void main(String[] args) {//Actual game runtime
 		Poker game = new Poker(5);
 		Poker.lock = game;
 		Window window = new Window(game);
 		game.passWindow(window);
 		window.redrawScore();
 		
 	}
 	/**
 	 * Constructor
 	 */
 	public Poker(int playersd) {
 		this.PLAYERS = playersd;
 		players = new Player[PLAYERS+1];
 		players[1] = new Human();
 		//create players
 		for (int i = 2; i < PLAYERS + 1; i++){
 			players[i] = new AI();
 		}
 		//initialization
 		for (int i = 1; i < PLAYERS+1;i++){
 			players[i].changeCash(STARTING_CASH);
 			if (i !=1)
 				players[i].setName("AI " + (i -1));
 			else
 				players[i].setName("Human " + i);
 		}
 		currentPlayer = 1;
 		community = new Hand();
 		dealer = 1;
 	}
 	
 	/**
 	 * getCurrentPlayer()
 	 * 
 	 * returns an integer of whose turn it is
 	 * @return int
 	 */
 	
 	public int getCurrentPlayer(){
 		return currentPlayer;
 	}
 	
 	/**
 	 * changePlayer()
 	 * 
 	 * changes from player 1 to player 2 and vice versa
 	 * depending on who the current player is
 	 */
 	public void changePlayer(){
 		if (currentPlayer == PLAYERS)
 			currentPlayer = 1;
 		else
 			currentPlayer +=1;
 	}
 	
 	
 	/**
 	 * run()
 	 * 
 	 * does the actual running of the game
 	 * 
 	 */
 	public void run(){
 		int temp; //this number is used when temporary values need to be sent to two different functions
 		deck = new Deck();
 		for (int i = 0; i < 20; i++){
 			deck.shuffle();
 		}
 		
 		pot = 0;
 		bid = 0;
 		deck.shuffle();//start off by shuffling
 		//give each player a brand new hand
 		for (int i = 1; i < PLAYERS + 1; i++){
 			players[i].setHand(new Hand());
 		}
 		
 		
 		//deal each player 2 cards
 		for (int i = 1; i < PLAYERS +1; i++){
 			players[i].getHand().add(deck.draw());
 			players[i].getHand().add(deck.draw());
 		}
 		//print players cards to screen
 		win.clearPlayerCards();
 		win.printToPlayerCards(players[1].getHand().getCard(0).toString()+"\n");
 		win.printToPlayerCards(players[1].getHand().getCard(1).toString()+"\n");
 		
 		
 		
 		//Say that the dealer deals
 		
 		players[dealer].speak("deals two cards to each player");
 		//Clear each players current bid
 		for (int i = 1; i < PLAYERS + 1;i++){
 			players[i].setCurrentBid(0);
 		}
 		//big blind left of dealer
 		temp = players[getBlinders()[0]].getBlind(true);
 		pot += temp;
 		bid = temp;
 		win.redrawScore();
 		//small blind right of dealer
 		temp = players[getBlinders()[1]].getBlind(false);
 		pot += temp;
 		win.redrawScore();
 		//round of bidding
 		beginBid();
 		//place 3 cards in community card
 		players[dealer].speak("deals three cards to the flop");
 		win.clearCommunity();
 		Card tempCard;
 		for (int i = 0; i < 3; i++){
 			tempCard = deck.draw();
 			community.add(tempCard);
 			win.printToCommunity(tempCard.toString()+"\n");
 			Poker.sleep(750);
 		}
 		//bidding round 
 		beginBid();
 		//deal 1 card to community
 		players[dealer].speak("deals a card to the turn");
 		tempCard = deck.draw();
 		community.add(tempCard);
 		win.printToCommunity(tempCard.toString()+"\n");
 		Poker.sleep(750);
 		//bidding round
 		beginBid();
 		//deal final card to community
 		players[dealer].speak("deals a card to the river");
 		tempCard = deck.draw();
 		community.add(tempCard);
 		win.printToCommunity(tempCard.toString()+"\n");
 		Poker.sleep(750);
 		
 		
 		
 		
 		
 	}
 	
 	public Player getPlayer(int play){
 		return players[play];
 	}
 	
 	public static void sleep(int i){
 	
 			try {
 				Thread.sleep(i);
 			} 
 			catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		
 	}
 	
 	private int[] getBlinders(){
 		int[] ret = new int[2];
 		int big = dealer - 1;
 		if (big == 0)
 			big = PLAYERS;
 		int small = dealer +1;
 		if (dealer == PLAYERS)
 			small = 1;
 		ret[0] = big;
 		ret[1] = small;
 		return ret;
 	}
 	
 	private void beginBid(){
 		boolean done = false;
 		int temp;
 		boolean oneround = false;
 		while (!done){
 			//gets players' bids
 			for (int i = 1; i < PLAYERS +1; i++){
				if ((!oneround || players[i].meetingBid(bid)) && !players[i].folding) {
 					temp = players[i].getBid(bid);
 					pot += temp;
 					win.redrawScore();
 					if (temp > bid){
						bid = temp ;
 					}
 				}
 			}		
 			done = true;
 			//checks to see if all players have called
 			for (int i = 1; i < PLAYERS + 1; i++){
 				if (players[i].meetingBid(bid) != true && !players[i].folding)
 					done = false;
 			}
 			oneround = true;
 			
 		}
		bid = 0;
		for (int i = 1; i < PLAYERS +1; i++ ) {
			players[i].setCurrentBid(0);
		}
 	}
 	
 	public int getPot() {
 		return pot;
 	}
 	
 	/**
 	 * passes the game window to various parts of the program
 	 * @param window
 	 */
 	public void passWindow(Window window){
 		win = window;
 		for (int i = 1; i < PLAYERS + 1; i++){
 			players[i].setWindow(window);
 		}
 	}
 	public static Object getLock() {
 		return lock;
 	}
 	public static void setLock(Object lock) {
 		Poker.lock = lock;
 	}
 }
