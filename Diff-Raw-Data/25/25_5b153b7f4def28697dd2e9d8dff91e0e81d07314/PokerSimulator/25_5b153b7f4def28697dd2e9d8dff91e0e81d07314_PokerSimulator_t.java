 package poker;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import poker.Player.Action;
 import poker.OpponentModeller.PlayerModelTriplet;
 import poker.Player.PlayerType;
 
 public class PokerSimulator {
 
 	private Table table;
 	private int roundNumber;
 	private int raiseAmount;	//The fixed amount of the raise
 	private int numOfRaises;	//The current number of raises
 	private int maxRaises;	//The max amount of raises each round
	private PlayerType[] types = new PlayerType[] {PlayerType.INTELLIGENT, PlayerType.INTELLIGENT, PlayerType.AGGRESSIVE, PlayerType.AGGRESSIVE, PlayerType.AGGRESSIVE, PlayerType.AGGRESSIVE, PlayerType.AGGRESSIVE, PlayerType.AGGRESSIVE, PlayerType.AGGRESSIVE, PlayerType.INTELLIGENT};
 	private OpponentModeller opModeller;
 
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
 		this.opModeller = new OpponentModeller();
 
 
 		if(numOfPlayers >= 2 && numOfPlayers <= 10)
 			for(int i=1; i <= numOfPlayers; i++){
 				this.table.addPlayer(new Player(i, startingMoney, types[i-1]));
 			}
 		else
 			System.out.println("Antallet spillere skal v¾re mellom 2 og 10");
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
 
 
 	public boolean initiatePreFlopBetting(boolean log, boolean gatherStats){
 
 		boolean bettingEnded = false;
 		int raisePlayer = -1;	//ID of the player with the latest raise
 
 		while(!bettingEnded){
 
 			ArrayList<Player> activePlayers = new ArrayList<Player>();
 			activePlayers.addAll(this.table.getActivePlayers());
 
 
 			for(Player player: activePlayers){
 
 				boolean allowedToFold = !((player.getPlayerID() == table.getBigBlindID() && raisePlayer==-1) || player.getPlayerID() == raisePlayer);
 
 				Action playerAction = player.decidePreFlopAction(allowedToFold, this.table, this.opModeller, this.numOfRaises, gatherStats);
 
 				if(gatherStats){
 					player.saveContex(this.table, playerAction, this.numOfRaises, this.opModeller, true);
 				}
 				if(playerAction == Action.FOLD){
 
 					this.table.getActivePlayers().remove(player);
 					player.setActionContext(new HashMap<PlayerModelTriplet, Double>());
 
 					if(log)
 						System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " kastet seg.");
 
 					if(this.table.getActivePlayers().size()==1){
 						bettingEnded = true;
 						this.table.endRound(this.table.getActivePlayers());
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
 					player.setLastAction(Action.CALL);
 
 
 					if(log)
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
 						player.setLastAction(Action.CALL);
 
 						if(log)
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
 						player.setLastAction(Action.RAISE);
 
 						if(log)
 							System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " h¿ynet.");
 
 					}
 
 				}
 
 
 			}
 
 		}
 
 		return false;
 
 	}
 
 
 	/*
 	 * Method for initiating betting between players in after the flop, turn and river cards
 	 */
 	public boolean initiateBetting(boolean log, boolean gatherStats){
 
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
 
 
 				Action playerAction = player.decideAction(allowedToFold, this.table.getSharedCards(), log, this.table.getActivePlayers().size(), this.table, this.numOfRaises, this.opModeller, gatherStats);
 
 				if(gatherStats){
 					player.saveContex(this.table, playerAction, this.numOfRaises, this.opModeller, false);
 				}
 
 				if(playerAction == Action.FOLD){
 
 					this.table.getActivePlayers().remove(player);
 					player.setActionContext(new HashMap<PlayerModelTriplet, Double>());
 
 					if(log)
 						System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " kastet seg.");
 
 					if(this.table.getActivePlayers().size()==1){
 						bettingEnded = true;
 						this.table.endRound(this.table.getActivePlayers());
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
 					player.setLastAction(Action.CALL);
 
 					if(log)
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
 						player.setLastAction(Action.CALL);
 
 						if(log)
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
 						player.setLastAction(Action.RAISE);
 
 						if(log)
 							System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + " h¿ynet.");
 
 					}
 
 				}
 
 
 			}
 
 			if(raisePlayer == null)
 				raisePlayer = this.table.getActivePlayers().get(0);
 
 		}
 
 		return false;
 
 	}
 
 	public void initiateShowdown(boolean log){
 
 		ArrayList<Player> winner = new ArrayList<Player>(); 
 		winner.add(table.getActivePlayers().get(0));
 		int highestRating = 0;
 		for(Player player : table.getActivePlayers()){
 			Set<PlayerModelTriplet> contexts = player.getActionContext().keySet();
 
 			Iterator<PlayerModelTriplet> itr = contexts.iterator();
 			while(itr.hasNext()){
 				PlayerModelTriplet triplet = itr.next();
 				opModeller.saveContex(triplet, player.getActionContext().get(triplet));
 
 			}
 			player.setActionContext(new HashMap<PlayerModelTriplet, Double>());
 
 
 			if(log)
 				System.out.println("Spiller"+player.getPlayerID()+" viser "+player.getCards().toString()+" med en rating på: "+player.getCurrentCardRating()[0]+", og highcard:"+player.getCurrentCardRating()[1]);
 
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
 			System.out.println("Spiller: " + Integer.toString(player.getPlayerID()) + " Hnd: " + player.printHand());
 		}
 	}
 
 	public boolean playRound(boolean log, boolean gatherStats){
 
 		this.startNewRound();
 
 		if(log)
 			this.printTable();
 
 		//Initiate pre-flop betting
 		if(this.initiatePreFlopBetting(log, gatherStats)){
 			if(log){
 				System.out.println("Runden er over, Spiller" + Integer.toString(this.table.getLastWinners().get(0).getPlayerID()) + " vant " + Integer.toString(this.table.getPotSize()) + "kr");
 			}
 
 			return true;
 		}
 		else {
 			if(log) {
 				System.out.println();
 				System.out.println("Spillere fremdeles aktiv: " + this.table.printActivePlayers());
 				System.out.println("St¿rrelsen p potten: " + Integer.toString(this.table.getPotSize()));
 			}
 		}
 
 
 		//Deal flop and initiate betting
 		this.setNumOfRaises(0);
 		this.table.dealFlop(log);
 		if(this.initiateBetting(log, gatherStats)){
 			if(log)
				System.out.println("Runden er over, Spiller" + Integer.toString(this.table.getLastWinners().get(0).getPlayerID()) + " vant " + Integer.toString(this.table.getPotSize()) + "kr");
 			return true;
 		}	
 		else {
 			if(log) {
 				System.out.println();
 				System.out.println("Spillere fremdeles aktiv: " + this.table.printActivePlayers());
 				System.out.println("St¿rrelsen p potten: " + Integer.toString(this.table.getPotSize()));
 			}
 		}
 
 
 		//Deal turn and initiate betting
 		this.setNumOfRaises(0);
 		this.table.dealTurn(log);
 		if(this.initiateBetting(log, gatherStats)){
 			if(log)
				System.out.println("Runden er over, Spiller" + Integer.toString(this.table.getLastWinners().get(0).getPlayerID()) + " vant " + Integer.toString(this.table.getPotSize()) + "kr");
 			return true;
 		}	
 		else {
 			if(log) {
 				System.out.println();
 				System.out.println("Spillere fremdeles aktiv: " + this.table.printActivePlayers());
 				System.out.println("St¿rrelsen p potten: " + Integer.toString(this.table.getPotSize()));
 			}
 		}
 
 		//Deal river and initiate betting
 		this.setNumOfRaises(0);
 		this.table.dealRiver(log);
 		if(this.initiateBetting(log, gatherStats)){
 			if(log)
				System.out.println("Runden er over, Spiller" + Integer.toString(this.table.getLastWinners().get(0).getPlayerID()) + " vant " + Integer.toString(this.table.getPotSize()) + "kr");
 			return true;
 		}	
 		else {
 			if(log) {
 				System.out.println();
 				System.out.println("Spillere fremdeles aktiv: " + this.table.printActivePlayers());
 				System.out.println("St¿rrelsen p potten: " + Integer.toString(this.table.getPotSize()));
 			}
 		}
 		if(log)
 			System.out.println("Sharedcards er: "+this.table.getSharedCards().toString());
 		this.initiateShowdown(log);
 
 		if(log)
 			System.out.println("Runden er over!");
 
 		if(log) {
 			if(this.table.getActivePlayers().size() > 1){
 				System.out.println("Det ble uavgjort mellom spillerne ");
 				for(Player player : this.table.getActivePlayers()){
 					System.out.print(player.getPlayerID()+" og ");
 				}
 				System.out.print(" med hånden :"+this.table.getActivePlayers().get(0).getCards().toString());
 			}	
 			else{
 				System.out.println("Vinneren er: Spiller"+this.table.getActivePlayers().get(0).getPlayerID()+" med hånden :"+this.table.getActivePlayers().get(0).getCards().toString());
 			}
 		}
 
 		this.table.endRound(this.table.getActivePlayers());
 		return true;
 	}
 
 	public void printMoney(){
 
 		for(Player player: this.table.getPlayers()){
 			System.out.println("Spiller" + Integer.toString(player.getPlayerID()) + ", Type: " + player.getType().toString() + " har " + Integer.toString(player.getMoney()) + " kr");
 		}
 
 	}
 
 
 	public static void main(String args[]){
 		
 		
		PokerSimulator pokerSim = new PokerSimulator(2, 2000, 50, 100, 50, 2);
		/*
 		
 		for(int i=0; i<1000; i++){
 
 			pokerSim.playRound(false, true);
 		}
 		pokerSim.opModeller.finishModel();
 		
 
 		
 		for(Player player: pokerSim.table.getPlayers())
 			player.setMoney(2000);
		*/
 		for(int i=1; i<=3000; i++){
			pokerSim.playRound(true, false);
 			if(i%1000 == 0){
 				pokerSim.printMoney();
 				for(Player player: pokerSim.table.getPlayers())
 					player.setMoney(2000);
 			}
 		}
 		
		/*
 		
 		Set<PlayerModelTriplet> contexts = pokerSim.opModeller.finishedModel.keySet();
 
 		Iterator<PlayerModelTriplet> itr = contexts.iterator();
 		while(itr.hasNext()){
 			PlayerModelTriplet triplet = itr.next();
 			System.out.println(triplet.toString() + " Value: " + pokerSim.opModeller.finishedModel.get(triplet)[0]);
 		}
 		
 	
 	
 		pokerSim.printMoney();
 		*/
 
 	}
 
 
 
 }
