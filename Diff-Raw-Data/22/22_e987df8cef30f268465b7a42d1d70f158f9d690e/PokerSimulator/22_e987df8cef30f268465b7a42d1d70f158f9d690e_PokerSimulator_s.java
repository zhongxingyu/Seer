 package poker;
 
 import java.util.ArrayList;
 
 import poker.Card.Suit;
 import poker.Card.Value;
 import poker.Player.Action;
 import poker.Player.PlayerType;
 
 
 public class PokerSimulator {
 	
 	private Table table;
 	private int roundNumber;
 	private int raiseAmount;	//The fixed amount of the raise
 	private int numOfRaises;	//The current number of raises
 	private int maxRaises;	//The max amount of raises each round
 	private PlayerType[] types = new PlayerType[] {PlayerType.DEFENSIVE, PlayerType.NORMAL, PlayerType.AGGRESSIVE, PlayerType.DEFENSIVE, PlayerType.NORMAL, PlayerType.AGGRESSIVE, PlayerType.DEFENSIVE, PlayerType.NORMAL, PlayerType.AGGRESSIVE, PlayerType.DEFENSIVE};
 	
 	
 	/*
 	 * Description
 	 */
 	public PokerSimulator(int numOfPlayers, int startingMoney, int smallBlind, int bigBlind, int raiseAmount, int maxRaises){
 		this.table = new Table();
 		this.roundNumber = 0;
 		
 		this.raiseAmount = raiseAmount;
 		this.maxRaises = maxRaises;
 		this.table.setSmallBlindAmount(smallBlind);
 		this.table.setBigBlindAmount(bigBlind);
 		
 		
 		if(numOfPlayers >= 2 && numOfPlayers <= 10)
 			for(int i=1; i <= numOfPlayers; i++){
 				this.table.addPlayer(new Player(i, startingMoney, types[i-1]));
 			}
 		else
 			System.out.println("Antallet spillere skal vre mellom 2 og 10");
 	}
 	
 	public int getNumOfRaises() {
 		return numOfRaises;
 	}
 
 
 
 	public void setNumOfRaises(int numOfRaises) {
 		this.numOfRaises = numOfRaises;
 	}
 
 
 
 	public void startNewRound(){
 		this.roundNumber++;
 		this.table.startNewRound();
 		
 	}
 	
 	
 	public boolean initiatePreFlopBetting(){
 		
 		boolean bettingEnded = false;
 		int raisePlayer = -1;	//ID of the player with the latest raise
 		
 		while(!bettingEnded){
 			
 			ArrayList<Player> activePlayers = new ArrayList<Player>();
 			activePlayers.addAll(this.table.getActivePlayers());
 			
 						
 			for(Player player: activePlayers){
 				
 				boolean allowedToFold = !((player.getPlayerID() == table.getBigBlindID() && raisePlayer==-1) || player.getPlayerID() == raisePlayer);
 				
 				Action playerAction = player.decidePreFlopAction(allowedToFold);
 				
 				if(playerAction == Action.FOLD){
 										
 					this.table.getActivePlayers().remove(player);
 					
 					System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " kastet seg.");
 					
 					if(this.table.getActivePlayers().size()==1){
 						bettingEnded = true;
 						this.table.endRound(this.table.getActivePlayers().get(0));
 						return true;
 					}
 						
 				}
 				
 				else if(playerAction == Action.CALL){
 					/*
 					 * If the player who made the last raise calls, the betting round is over
 					 */
 					if(player.getPlayerID() == raisePlayer || (player.getPlayerID() == this.table.getBigBlindID() && raisePlayer == -1)){
 						bettingEnded = true;
 						break;
 					}
 					
 					player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
 					this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
 					player.setCurrentBet(this.table.getCurrentBet());
 					System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " ser.");
 					
 				}
 				
 				else if(playerAction == Action.RAISE){
 					
 					if(this.numOfRaises == this.maxRaises){
 						/*
 						 * If the player who made the last raise calls, the betting round is over
 						 */
 						if(player.getPlayerID() == raisePlayer || (player.getPlayerID() == this.table.getBigBlindID() && raisePlayer == -1)){
 							bettingEnded = true;
 							break;
 						}
 						
 						player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
 						this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
 						player.setCurrentBet(this.table.getCurrentBet());
 						System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " ser.");
 						
 						
 						
 					}
 					else{
 						/*
 						 * If the player who made the last raise calls, the betting round is over
 						 */
 						if(player.getPlayerID() == raisePlayer){
 							bettingEnded = true;
 							break;
 						}
 						
 						table.setCurrentBet(this.table.getCurrentBet() + this.raiseAmount);
 						player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
 						this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
 						player.setCurrentBet(this.table.getCurrentBet());
 						this.numOfRaises++;
 						raisePlayer = player.getPlayerID();
 						
 						System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " hynet.");
 						
 					}
 				
 				}
 				
 				
 			}
 				
 		}
 		
 		return false;
 		
 	}
 	
 	
 	/*
 	 * Method for initiating betting between players in after the flop, turn and river cards
 	 */
 	public boolean initiateBetting(){
 		
 		boolean bettingEnded = false;
 		Player raisePlayer = null;	//ID of the player with the latest raise
 		
 		while(!bettingEnded){
 			
 			ArrayList<Player> activePlayers = new ArrayList<Player>();
 			activePlayers.addAll(this.table.getActivePlayers());
 			
 						
 			for(Player player: activePlayers){
 				
 				
 				if(player == raisePlayer){
 					bettingEnded = true;
 					break;
 				}
 				
 				boolean allowedToFold = !(player == raisePlayer || raisePlayer == null);
 				
 				Action playerAction = player.decideAction(allowedToFold, this.table.getSharedCards());
 												
 				if(playerAction == Action.FOLD){
 										
 					this.table.getActivePlayers().remove(player);
 					
 					System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " kastet seg.");
 					
 					if(this.table.getActivePlayers().size()==1){
 						bettingEnded = true;
 						this.table.endRound(this.table.getActivePlayers().get(0));
 						return true;
 					}
 						
 				}
 				
 				else if(playerAction == Action.CALL){
 					/*
 					 * If the player who made the last raise calls, the betting round is over
 					 */
 					if(player == raisePlayer){
 						bettingEnded = true;
 						break;
 					}
 					
 					player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
 					this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
 					player.setCurrentBet(this.table.getCurrentBet());
 					System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " ser.");
 				}
 				
 				else if(playerAction == Action.RAISE){
 					
 					if(this.numOfRaises == this.maxRaises){
 						/*
 						 * If the player who made the last raise calls, the betting round is over
 						 */
 						if(player == raisePlayer){
 							bettingEnded = true;
 							break;
 						}
 						
 						player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
 						this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
 						player.setCurrentBet(this.table.getCurrentBet());
 						System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " ser.");
 						
 						
 						
 					}
 					else{
 						/*
 						 * If the player who made the last raise calls, the betting round is over
 						 */
 						if(player == raisePlayer){
 							bettingEnded = true;
 							break;
 						}
 						
 						table.setCurrentBet(this.table.getCurrentBet() + this.raiseAmount);
 						player.reduceMoney(this.table.getCurrentBet() - player.getCurrentBet());
 						this.table.addToPot(this.table.getCurrentBet() - player.getCurrentBet());
 						player.setCurrentBet(this.table.getCurrentBet());
 						this.numOfRaises++;
 						raisePlayer = player;
 						
 						System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " hynet.");
 						
 					}
 				
 				}
 				
 				
 			}
 			
 			if(raisePlayer == null)
 				raisePlayer = this.table.getActivePlayers().get(0);
 				
 		}
 		
 		return false;
 		
 	}
 	
 	public void initiateShowdown(){
 		
 		ArrayList<Player> winner = new ArrayList<Player>(); 
 		winner.add(table.getActivePlayers().get(0));
 		int highestRating = 0;
 		for(Player player : table.getActivePlayers()){
 			System.out.println("Spiller"+player.getPlayerID()+" viser "+player.getCards().toString()+" med en rating p: "+player.getCurrentCardRating()[0]+", og highcard:"+player.getCurrentCardRating()[1]);
 			if(highestRating < player.getCurrentCardRating()[0]){
 				winner.clear();
 				winner.add(player);
 				highestRating = player.getCurrentCardRating()[0];
 			}
 			else if(highestRating == player.getCurrentCardRating()[0]){
 				boolean tie = true;
 				for (int i = 1; i < player.getCurrentCardRating().length; i++) {
 					if(player.getCurrentCardRating()[i] > winner.get(0).getCurrentCardRating()[i]){
 						winner.clear();
 						winner.add(player);
 						highestRating = player.getCurrentCardRating()[0];
 						tie = false;
 						break;
 					}
 					else if(player.getCurrentCardRating()[i] < winner.get(0).getCurrentCardRating()[i]){
 						tie = false;
 						break;
 					}
 					
 				}
 				if(tie){
 					winner.add(player);
 				}
 				
 			}	
 		}
 		table.getActivePlayers().retainAll(winner);
 	}
 
 	public void printTable(){
 		System.out.println("Antall spillere: " + Integer.toString(this.table.getPlayers().size()));
 		System.out.println("Runde nr.: " + Integer.toString(this.roundNumber));
 		
 		for(Player player: this.table.getPlayers()){
 			System.out.println("Spiller: " + Integer.toString(player.getPlayerID()) + " Hnd: " + player.printHand());
 		}
 	}
 	
 	public boolean playRound(boolean log){
 		
 		this.startNewRound();
 		
 		this.printTable();
 		
 		//Initiate pre-flop betting
 		if(this.initiatePreFlopBetting()){
 			System.out.println("Runden er over, Spiller" + Integer.toString(this.table.getLastWinner().getPlayerID()) + " vant " + Integer.toString(this.table.getPotSize()) + "kr");
 			return true;
 		}
 		else {
 			System.out.println("Spillere fremdeles aktiv: " + this.table.printActivePlayers());
 			System.out.println("Strrelsen p potten: " + Integer.toString(this.table.getPotSize()));
 		}
 		
 		this.setNumOfRaises(0);
 		this.table.dealFlop();
 		//Initiate betting after flop
 		if(this.initiateBetting()){
 			System.out.println("Runden er over, Spiller" + Integer.toString(this.table.getActivePlayers().get(0).getPlayerID()) + " vant " + Integer.toString(this.table.getPotSize()) + "kr");
 			return true;
 		}	
 		else {
 			System.out.println("Spillere fremdeles aktiv etter flopp: " + this.table.printActivePlayers());
 			System.out.println("Strrelsen p potten: " + Integer.toString(this.table.getPotSize()));
 		}
 		
 		
 		this.setNumOfRaises(0);
 		this.table.dealTurn();
 		//Initiate betting after turn
 		if(this.initiateBetting()){
 			System.out.println("Runden er over, Spiller" + Integer.toString(this.table.getActivePlayers().get(0).getPlayerID()) + " vant " + Integer.toString(this.table.getPotSize()) + "kr");
 			return true;
 		}	
 		else {
 			System.out.println("Spillere fremdeles aktiv etter turn: " + this.table.printActivePlayers());
 			System.out.println("Strrelsen p potten: " + Integer.toString(this.table.getPotSize()));
 		}
 		
		
<<<<<<< HEAD
		pokerSim.setNumOfRaises(0);
		pokerSim.table.dealRiver();

=======
		this.setNumOfRaises(0);
		this.table.dealRiver();
>>>>>>> Fikset litt på diverse
 		//Initiate betting after river
 		if(this.initiateBetting()){
 			System.out.println("Runden er over, Spiller" + Integer.toString(this.table.getActivePlayers().get(0).getPlayerID()) + " vant " + Integer.toString(this.table.getPotSize()) + "kr");
 			return true;
 		}	
 		else {
 			System.out.println("Spillere fremdeles aktiv etter river: " + this.table.printActivePlayers());
 			System.out.println("Strrelsen p potten: " + Integer.toString(this.table.getPotSize()));
 		}
 
		System.out.println("");
<<<<<<< HEAD
		System.out.println("Sharedcards er: "+pokerSim.table.getSharedCards().toString());

		pokerSim.initiateShowdown();
=======
 		System.out.println("Sharedcards er: "+this.table.getSharedCards().toString());
 		this.initiateShowdown();
>>>>>>> Fikset litt på diverse
 		System.out.println("Runden er over!");
		if(this.table.getActivePlayers().size()>1){
 			System.out.print("Det ble uavgjort mellom spillerne ");
 			for(Player player : this.table.getActivePlayers()){
 				System.out.print(player.getPlayerID()+" og ");
 			}
 			System.out.println(" med hnden :"+this.table.getActivePlayers().get(0).getCards().toString());
 		}	
 		else{
 			System.out.println("Vinneren er: Spiller"+this.table.getActivePlayers().get(0).getPlayerID()+" med hnden :"+this.table.getActivePlayers().get(0).getCards().toString());
 
 		}
 		
 		return true;
 		
 	}
 	
 	public void printMoney(){
 		
 		for(Player player: this.table.getPlayers()){
 			System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " har " + Integer.toString(player.getMoney()) + " kr");
 		}
 		
 	}
 	
 	
 	public static void main(String args[]){
 		
 		PokerSimulator pokerSim = new PokerSimulator(6, 2000, 50, 100, 50, 2);
 		pokerSim.playRound(true);
 		pokerSim.printMoney();
 		
 		
 	}
 
 }
