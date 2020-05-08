 package gameEngine;
 
 import java.util.*;
 
 import participant.Chips;
 import participant.Player;
 import table.Table;
 import game.*;
 import game.Observable;
 import game.Observer;
 import cards.*;
 
 	public class GameEngine	implements Observable {
 		
 		public GameEngine() {
 		}
 		
 		public void reset() {
 			myGame = new Blackjack();
 			playersAndHands = new HashMap<Player, ArrayList<BlackjackHand>>();
 			playersAndChips = new HashMap<Player, Chips>();
 			this.notifyObservers();
 		}
 		
 		public void gameStart() {
 			
 			reset();
 
 			
 			
 			Player wat= new Player("wat","omg");
 			gameTable = new Table(wat);
 			
 
 			/*
 			 * Add players here
 			 */
 			Player bob = new Player("bob","bob");
 			BlackjackHand hb = new BlackjackHand();
 			ArrayList<BlackjackHand> hbb = new ArrayList<BlackjackHand>();
 			hbb.add(hb);
 			
 			Player bab = new Player("bab","bab");
 			BlackjackHand hb2 = new BlackjackHand();
 			ArrayList<BlackjackHand> hbb2 = new ArrayList<BlackjackHand>();
 			hbb2.add(hb2);
 			
 			
 			
 		//	playersAndHands.put(bob, hbb);
 		//	playersAndHands.put(bab, hbb2);
 			
 			gameTable.requestJoin(bob);
 			gameTable.requestJoin(bab);
 			
 			
 			myGame.deal(dealerHand);
 			
 			
 			
 			loadPlayers();
 			loadHands();
 			
 			
 			loadChips();
 			loadBets();
 			
 			
 			for (int i=0;i<getNumberOfPlayers();i++){
 				System.out.println(gameTable.getPlayer(i).getName() + " how much do you want to bet?");
 				Scanner keyboard = new Scanner(System.in);
 				int a= keyboard.nextInt();
 				System.out.println(" you want to bet " +a);
 				
 				while (playersAndChips.get(gameTable.getPlayer(i)).removeChips(a)==false) {
 					System.out.println("sorry not enough chips, please enter " + playersAndChips.get(gameTable.getAllPlayers().get(i)).getChips()  + " or less" );
 					a= keyboard.nextInt();
 					System.out.println(" you want to bet " +a);
 				}
 			}
 			
 
 			
 			
 			System.out.println("Dealer's hand: " + dealerHand.toString());	
 			
 			if (dealerHand.checkBlackjack()) {
 				System.out.println("Game over!");	
 			} else {
 			
 				while (dealerHand.getBlackjackValue() < MUST_HIT) 
 					myGame.hit(dealerHand);
 			
 			
 			/*
 			 * Deal everyone
 			 */
 			System.out.println("Dealing everyone...");
 			for (int i=0;i<getNumberOfPlayers();i++){
 			System.out.println(gameTable.getPlayer(i).getName());
 			}
 			
 			for (ArrayList<BlackjackHand> b : playersAndHands.values()) {
 				BlackjackHand tmpHand = new BlackjackHand();
 				myGame.deal(tmpHand);
 				b.add(tmpHand);
 			}
 			
 			for (int i=0;i<getNumberOfPlayers();i++){
 				ArrayList<BlackjackHand> b = playersAndHands.get(gameTable.getPlayer(i));
 				System.out.println(gameTable.getPlayer(i).getName() + ", your first hand is currently: " + b.get(0).toString());
 				if (b.size() == 2) {
 					System.out.println(gameTable.getPlayer(i) + ", your second hand is currently: " + b.get(1).toString());
 				}
 			}
 			
 			
 			
 		
 	
 				/*
 				 * Ask for input moves
 				 */
 
 				for (int i=0;i<getNumberOfPlayers();i++){
 					
 					ArrayList<BlackjackHand> b = playersAndHands.get(gameTable.getPlayer(i));
 					
 					System.out.println(gameTable.getPlayer(i).getName()+": ");
 					Scanner keyboard = new Scanner (System.in);
 					String s = "";
 				
 					
 					for (int j=0; j < b.size(); j++) {
 						
 						
 						while (b.get(j).isPlayable() && !(b.get(j).isBust())){
 							
 							System.out.println(b.get(j).isPlayable() +"isplayable");
 							System.out.println(!(b.get(j).isBust()) +"isbust");
 
 							System.out.println("now for your first hand, do you want to hit, stand, double down, split?");
 							s = keyboard.nextLine();
 						
 							if (s.equals("hit")){
 								myGame.hit(b.get(j));
 								System.out.println("hand update: " +b.get(j).toString());
 							}
 							else if (s.equals("stand")){
 								myGame.stand(b.get(j));
 								System.out.println("final hand: "+b.get(j).toString());
 							}
 							else if (s.equals("double down")){
 								if (playersAndChips.get(gameTable.getPlayer(i)).removeChips(playersAndChips.get(i).getBet())==false) {
 									System.out.print("sorry not enough chips, please enter a different move" );
 									s = "";
 								}
 								else { 
 									myGame.doubleDown(b.get(j));
 									System.out.println("hand update: "+b.get(j).toString());
 								}
 								
 							}
 							else if (s.equals("split")){
 								if (b.get(j).checkPair() == false) {
 									System.out.println("you dont have a pair! select a different move");
 									s = "";
 								} else {
 									myGame.split(b.get(j));
 									System.out.println("split to be implemented: "+b.get(j).toString());
 								}
 							} else {
 								System.out.println("please enter something valid");
 								s = "";
 							}
 						}
 					}
 				}
 					
 
 			
 			System.out.println("Dealer's new hand: " + dealerHand.toString());	
 			if (dealerHand.isBust()) 
 					System.out.println("Dealer bust!");
 			
 			
 			/*
 			 * Compute all winners
 			 */
 			for (int i=0;i<getNumberOfPlayers();i++){
 				
 				ArrayList<BlackjackHand> b = playersAndHands.get(gameTable.getPlayer(i));
 				
 				if (b.size() == 1) {
 					if (processWinner(b.get(0)) == 1) 
 						System.out.println("Dealer wins!");
 					if (processWinner(b.get(0)) == -1) {
 						playersAndChips.get(gameTable.getPlayer(i)).addChips(playersAndChips.get(gameTable.getPlayer(i)).getBet());
 						System.out.println(gameTable.getPlayer(i).getName()+ " wins!");
 					}
 					if (processWinner(b.get(0)) == 0) 
 						System.out.println("Draw!");
 
 				} else {
 					
 					if (processWinner(b.get(0)) == 1) 
 						System.out.println("Dealer wins!");
 					if (processWinner(b.get(0)) == -1) {
 						playersAndChips.get(gameTable.getPlayer(i)).addChips(playersAndChips.get(gameTable.getPlayer(i)).getBet());
 						System.out.println(gameTable.getPlayer(i).getName()+ "'s first hand wins!");
 					}
 					if (processWinner(b.get(0)) == 0) 
 						System.out.println("Draw!");
 					
 					if (processWinner(b.get(1)) == 1) 
 						System.out.println("Dealer wins!");
 					if (processWinner(b.get(1)) == -1) {
 						playersAndChips.get(gameTable.getPlayer(i)).addChips(playersAndChips.get(gameTable.getPlayer(i)).getBet());
 						System.out.println(gameTable.getPlayer(i).getName()+ "'s second hand wins!");
 					}
 					if (processWinner(b.get(1)) == 0) 
 						System.out.println("Draw!");
 					
 				}
 			
 			}
 			
 			}
 			
 //			for (int i=0;i<getPlayers().size();i++) {
 //				ArrayList<BlackjackHand> handList = playersAndHands.get(gameTable.getPlayer(i));
 //				for (int j=0;j<handList.size();j++){
 //					BlackjackHand tmpHand = new BlackjackHand();
 //					myGame.deal(tmpHand);
 //					handList.set(i, tmpHand);
 //				}
 //			}
 			
 			
 //			for (ArrayList<BlackjackHand> b : playersAndHands.values()) {
 //				int numHands = b.size();
 //				for ( int j = 0 ; j < numHands ; j++ ) {
 //					BlackjackHand tmpHand = new BlackjackHand();
 //					myGame.deal(tmpHand);
 //					b.add(tmpHand);
 //						
 //					System.out.println(tmpHand.toString());
 //					
 //					BlackjackHand winnerHand = processWinner(tmpHand);
 //					if (winnerHand == null)
 //					System.out.print("Draw!");
 //					else if (winnerHand.getBlackjackValue() == dealerHand.getBlackjackValue())
 //						System.out.println("Dealer wins!");
 //					else if (winnerHand.getBlackjackValue() == tmpHand.getBlackjackValue())
 //						System.out.println("You win!");
 //					}
 //
 //			}
 			
 		
 
 			
 			
 //			for ( int i = 0 ; i < playersHands.size() ; i++ ) {
 //				Scanner keyboard = new Scanner(System.in);
 //				int a= keyboard.nextInt();
 //				System.out.print(a);
 //			}
 			
 		}
 
 		
 		@Override
 		public void notifyObservers()
 		{
 			if (!observerList.isEmpty()){
 				for (Observer ob : observerList) {
 					ob.handleEvent();
 				}
 			}			
 		}
 
 		@Override
 		public void addObservers(Observer obsToAdd)
 		{
 			observerList.add(obsToAdd);
 			
 		}
 
 		@Override
 		public void removeObservers(Observer obsToRemove)
 		{
 			observerList.remove(obsToRemove);
 			
 		}
 		
 		public void resetObservers() {
 			observerList = new ArrayList<Observer>();
 		}
 		
 		public void loadTable(Table table) {
 			gameTable = table;
 		}
 		
 		public void loadPlayers() {
 			
 			for ( int i = 0 ; i < getPlayers().size() ; i++ ) {
 				Player tmpPlayer = getPlayers().get(i);
 				if (!playersAndHands.containsKey(tmpPlayer)) {
 					playersAndHands.put(tmpPlayer, new ArrayList<BlackjackHand>());
 				} 
 			}
 		}
 		
 		public ArrayList<Player> getPlayers() {
 			return gameTable.getAllPlayers();
 		}
 		
 		public void loadHands() {
 
 		}
 		
 		public void loadChips() {
 			for ( int i = 0 ; i < getPlayers().size() ; i++ ) {
 				Player tmpPlayer = getPlayers().get(i);
 				if (!playersAndChips.containsKey(tmpPlayer)) {
 					playersAndChips.put(tmpPlayer, new Chips(STARTING_CHIPS));
 				} 
 			}
 		}
 		
 		public void loadBets() {
 			
 		}
 		
 		public void setDealer(Player player) {
 			
 		}
 		
 		public ArrayList<BlackjackHand> getPlayerHand(Player player) {
 			 return playersAndHands.get(player);
 		}
 
 		/**
 		 * Returns winner between hands of dealer and player
 		 * @param hand BlackjackHand to compare
 		 * @return 1 if dealer won, 0 if no winner, -1 if player won
 		 */
 		public int processWinner(BlackjackHand hand) {
 
 			if ((hand.isBust()) && !(dealerHand.isBust()))
 
 				return DEALER_WON;
 			
 			if (!(hand.isBust()) && !(dealerHand.isBust()) && (dealerHand.getBlackjackValue() > hand.getBlackjackValue()))
 
 				return DEALER_WON;
 			
 			if (!(hand.isBust()) && (dealerHand.isBust()))
 
 				return PLAYER_WON;
 			
 			if (!(hand.isBust()) && !(dealerHand.isBust()) && dealerHand.getBlackjackValue() < hand.getBlackjackValue()) {

 				return PLAYER_WON;
 			}
 			
 				return DRAW;
 			
 		}
 		
 		
 		
 //		public void resetKeepPlayers() {
 //			myGame = new Blackjack();
 //			winner = null;
 //			
 //			HashMap<Player, Hand> tmpPlayers = new HashMap<Player, Hand>();
 //			for (int i = 0 ; i < gameTable.getAllPlayers().size() ; i++) {
 //				Player tmpPlayer = gameTable.getPlayer(i);
 //				tmpPlayers.put(tmpPlayer, new Hand());
 //			}
 //			playersHands = tmpPlayers;
 //
 //		}
 	
 		public int getNumberOfPlayers() {
 			return playersAndHands.size();
 		}
 //		
 //		public void playDoubleDown() {
 //	    	if ( hasSufficientChips(player, chipCount)) {
 //	    		player.chips -= chipCount;
 //	    		chipCount += chipCount;
 //			
 //		}
 		
 		/**
 		 * Checks if there is sufficient specified amount of chips
 		 * @param amount The amount to be checked
 		 * @return True if there is sufficient chips, false otherwise
 		 */
 //	    public boolean hasSufficientChips(Player player, int amount) {
 //	    	if ( player.getChips() < amount ) {
 //	    		return false;
 //	    	}
 //	    	return true;
 //	    }
 		
 		/**
 		 *
 		 * @param
 		 * @return
 		 */
 //	    public void bet(Player player, int amount) {
 //	    	if ( hasSufficientChips(player,amount) ) {
 //	    		chipCount += amount;
 //	    		player.getChips() -= amount;
 //	    	}
 //	    }	  
 
 
 		private Blackjack myGame;
 
 		private Table gameTable;
 		
 		private ArrayList<Observer> observerList = new ArrayList<Observer>();
 		
 		private BlackjackHand dealerHand = new BlackjackHand();
 
 		private HashMap<Player, ArrayList<BlackjackHand>> playersAndHands;
 
 		private HashMap<Player, Chips> playersAndChips;
 
 		private final int MUST_HIT = 16;
 		
 		private final int STARTING_CHIPS = 500;
 		
 		private final int DEALER_WON = 1;
 		
 		private final int PLAYER_WON = -1;
 		
 		private final int DRAW = 0;
 		
 	}
