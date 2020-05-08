 package poker;
 
 /**
  * Poker
  * 
  * The model for Poker. Does all computations for a poker match. GUI is handled in window.java
  * 
  * @author Paul Steele
  *
  */
 public class Poker implements Runnable{
 	public final int PLAYERS; //The number of players in this instance
 	private Player[] players; //array of players in this instance
 	private final int STARTING_CASH = 500; //amount of cash that each player starts with
 	private Deck deck; //the deck of the game
 	private Hand community; //the community cards
 	private int bid; //the highest bid
 	private int pot; //what's in the pot
 	private int dealer;
 	public static final int BIG_BLIND = 10; //the big blind
 	private static Object lock;
 	public static Object dropbox; //used to store values to be passed between threads
 	private Window win; //the window associated with this instance
 	private int round; //number of betting rounds
 	
 	/**
 	 * main()
 	 * 
 	 * creates window and performs the beginning drawing tasks
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {//Actual game runtime
 		Window window = new Window();
 		beginningDrawTasks(window);
 		
 	}
 	/**
 	 * Constructor
 	 * @param playersd The number of players to be in the instance
 	 * 
 	 */
 	public Poker(int playersd) {
 		Poker.lock = this;
 		this.PLAYERS = playersd;
 		//creates the players making the first one human, the rest AI
 		players = new Player[PLAYERS+1];
 		players[1] = new Human(this);
 		for (int i = 2; i < PLAYERS + 1; i++){
 			players[i] = new AI(this);
 		}
 		//initialization
 		for (int i = 1; i < PLAYERS+1;i++){
 			players[i].changeCash(STARTING_CASH);
 			if (i !=1)
 				players[i].setName("AI " + (i -1));
 			else
 				players[i].setName("Human " + i);
 		}
 		//set initial dealer
		community = new Hand();
 		dealer = 1;
 	}
 	
 	/**
 	 * getCurrentPlayer()
 	 * 
 	 * returns an integer of whose turn it is
 	 * @return int
 	 */
 	
 	
 	/**
 	 * run()
 	 * 
 	 * does the actual running of the game
 	 * 
 	 */
 	public void run(){
 		try { //allows interruption
 			int temp; //this number is used when temporary values need to be sent to two different functions
 			round = 0;
 			deck = new Deck();
 			for (int i = 0; i < 20; i++){
 				deck.shuffle();
 			}
 			
 			pot = 0;
 			bid = 0;
 			deck.shuffle();//start off by shuffling
 			//give each player a brand new hand
 			for (int i = 1; i < PLAYERS + 1; i++){
 				if (players[i].inGame) {
 					players[i].setHand(new Hand());
 				}
 			}
 			
 			
 			//deal each player 2 cards
 			for (int i = 1; i < PLAYERS +1; i++){
 				players[i].getHand().add(deck.draw());
 				players[i].getHand().add(deck.draw());
 			}
 
 			//Say that the dealer deals
 			
 			players[dealer].speak("deals two cards to each player");
 			Poker.sleep(400);
 			//print players cards to screen
 			win.clearPlayerCards();
 			win.printToPlayerCards(players[1].getHand().getCard(0).toString()+"\n\n");
 			Poker.sleep(750);
 			//hack to make the UI not spazz on resize
 			win.clearPlayerCards();
 			win.printToPlayerCards(players[1].getHand().getCard(0).toString()+"\n");
 			win.printToPlayerCards(players[1].getHand().getCard(1).toString()+"\n");
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
 			int winning = -1;
 			int winningScore = 0;
 			int lead = -1;
 			for (int i = 1; i < PLAYERS + 1; i++){
 		
 				if ( (( players[i].currentScore() > winningScore ) && !players[i].isFolding()) || 
 						((players[i].currentScore() == winningScore) && ((players[i].getLead() > lead) || players[i].getLead() == 0) && !players[i].isFolding())) {
 					winningScore = players[i].currentScore();
 					winning = i;
 					lead = players[i].getLead();
 				}
 				
 				
 				players[i].speak("Plays has a " + players[i].getScoreName());
 				Poker.sleep(550);
 			}
 			
 			players[winning].changeCash(pot);
 			pot = 0;
 			win.redrawScore();
 			players[winning].speak("won the round");
 			
 			for (int i = 1; i < PLAYERS +1; i++){
 				if (players[i].cash <= 0){
 					players[i].inGame = false;
 				}
 			}
 			
 			int numberStillIn = 0;
 			int whoin = 0;
 			for (int i = 1; i < PLAYERS +1; i++){
 				if (players[i].inGame){
 					numberStillIn++;
 					whoin = i;
 				}
 			}
 			
 			if (numberStillIn > 1){
 				Poker.sleep(2000);
 				win.clearCommunity();
 				win.clearPlayerCards();
 				run();
 			}
 			else {
 				
 				players[whoin].speak("won the game!");
 			}
 			
 		}
 		catch (InterruptedException e){
 			//simply end the run if an interruption occurs
 		}
 		
 		
 		
 		
 		
 	}
 	
 	public Player getPlayer(int play){
 		return players[play];
 	}
 	
 	public static void sleep(int i) throws InterruptedException{
 				Thread.sleep(i);
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
 	
 	private void beginBid() throws InterruptedException {
 		boolean done = false;
 		int temp;
 		boolean oneround = false;
 		
 		while (!done){
 			//gets players' bids
 			for (int i = 1; i < PLAYERS +1; i++){
 				if ((!oneround || !players[i].meetingBid(bid)) && !players[i].folding) {
 					temp = players[i].getBid(bid);
 					pot += temp;
 					win.redrawScore();
 					if (players[i].getCurrentBid() > bid){
 						bid = players[i].getCurrentBid() ;
 					}
 				}
 			}		
 			done = true;
 			//checks to see if all players have called
 			for (int i = 1; i < PLAYERS + 1; i++){
 				if ((players[i].meetingBid(bid) == false) && !players[i].folding)
 					done = false;
 			}
 			oneround = true;
 			
 		}
 		bid = 0;
 		round++;
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
 	
 	private static void beginningDrawTasks(Window window){
 		window.redrawScore();
 		window.clearCommunity();
 		window.clearPlayerCards();
 		window.printToPlayerCards("\n\n"); //useful for not spazzing out resizing
 	}
 	
 	public Hand getCommunity(){
 		return community;
 	}
 	
 	public int getRound(){
 		return round;
 	}
 }
