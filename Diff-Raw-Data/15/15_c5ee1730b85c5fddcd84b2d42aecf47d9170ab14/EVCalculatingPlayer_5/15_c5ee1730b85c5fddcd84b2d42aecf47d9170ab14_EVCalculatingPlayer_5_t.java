 package pokerbots.player;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import pokerbots.packets.GameObject;
 import pokerbots.packets.GetActionObject;
 import pokerbots.packets.HandObject;
 import pokerbots.packets.HandOverObject;
 import pokerbots.packets.LegalActionObject;
 import pokerbots.utils.BettingBrain_old_v2;
 import pokerbots.utils.HandEvaluator;
 import pokerbots.utils.MatchHistory;
 import pokerbots.utils.PreflopTableGen;
 import pokerbots.utils.StatAggregator;
 import pokerbots.utils.StatAggregator.OpponentStats;
 import pokerbots.utils.StochasticSimulator;
 import pokerbots.utils.Utils;
 
 
 /**
  * Improvements made:
  * - move logic into BettingBrain 
  * - scale bets proportional to pot size
  * - allow raising
  * - factor in opponents bet sizes 
  * 
  * Todo:
  * - fix looseness (use REFUND!) -- do it better
  * 
  * 
  * Future bot todo:
  * - implement states with semi-random transitions between them
  * - populate and use getPercentFoldToBet, getPercentCallToBet, getOurAverageBetForFold, getOurAverageRaiseForFold
  * 
  * 
  * As of Player 4, the Player class only takes care of managing the object references and calculating standard probabilities.
  * All the advanced logic has been moved into BettingBrain.
  */
 public class EVCalculatingPlayer_5 {
 	
 	//number of iterations for our simulator to calculate probabilities before deciding which card to toss.
 	private final int DISCARD_SIM_ITERS = 1000;
 	//number of iterations for calculating probabilities after each other street.
 	private final int FLOP_SIM_ITERS = 800;
 	private final int TURN_SIM_ITERS = 500;
 	private final int RIVER_SIM_ITERS = 500;
 	
 	private final PrintWriter outStream;
 	private final BufferedReader inStream;
 	private GameObject myGame;
 	private HandObject myHand;
 	private BettingBrain_old_v2 brain;
 	private StatAggregator aggregator;
 	private OpponentStats opponent;
 	private MatchHistory history;
 	private int potSize;
 
 	public EVCalculatingPlayer_5(PrintWriter output, BufferedReader input) {
 		this.outStream = output;
 		this.inStream = input;
 		aggregator = new StatAggregator(); // TODO: initialize with past data?
 		history = new MatchHistory();
 		potSize = 0;
 	}
 	
 	public void run() {
 		String input;
 		try {
 			while ((input = inStream.readLine()) != null) {
 				System.out.println(input);
 				String packetType = input.split(" ")[0];
 				
 				if ("GETACTION".compareToIgnoreCase(packetType) == 0) {
 					GetActionObject msg = new GetActionObject(input);
 					potSize = msg.potSize;
 					history.appendRoundData(msg.lastActions);
 					String action = respondToGetAction(msg);
 					outStream.println(action);
 					
 				} else if ("NEWGAME".compareToIgnoreCase(packetType) == 0) {
 					myGame = new GameObject(input);
					brain = new BettingBrain_old_v2(myGame,history);
 					opponent = aggregator.getOrCreateOpponent(myGame.oppName, myGame.stackSize);
 					
 				} else if ("NEWHAND".compareToIgnoreCase(packetType) == 0) {
 					myHand = new HandObject(input);
 					history.newRound(myHand.handId,myGame.oppName);
 					potSize = 0;
 					
 				} else if ("HANDOVER".compareToIgnoreCase(packetType) == 0) {
 					HandOverObject HOobj = new HandOverObject(input);
 					history.appendRoundData(HOobj.lastActions);
 					opponent.analyzeRoundData(myGame, myHand, history.getCurrentRound(), potSize);
 					history.saveRoundData();
 					history.getCurrentRound().printRound();
 					opponent.printStats(myGame);
 					
 				}else if ("KEYVALUE".compareToIgnoreCase(packetType) == 0) {
 					//none
 					
 				} else if ("REQUESTKEYVALUES".compareToIgnoreCase(packetType) == 0) {
 					//none
 					
 					outStream.println("FINISH");
 				}
 			}
 		} catch (IOException e) {
 			System.out.println("IOException: " + e.getMessage());
 		}
 		System.out.println("Gameover, engine disconnected");
 		try {
 			outStream.close();
 			inStream.close();
 		} catch (IOException e) {
 			System.out.println("Encounterd problem shutting down connections");
 			e.printStackTrace();
 		}
 	}
 	
 	public String respondToGetAction( GetActionObject getActionObject) {
 		int numBoardCards = getActionObject.boardCards.length;
 		float winChance0;
 		float winChance1;
 		float winChance2;
 		float winChance;
 
 		switch ( numBoardCards ) {
 			//PREFLOP
 			case 0:
 				int street = 0;
 				winChance0 = PreflopTableGen.getPreflopWinRate(myHand.cards3[1],myHand.cards3[2]);
 				winChance1 = PreflopTableGen.getPreflopWinRate(myHand.cards3[0],myHand.cards3[2]);
 				winChance2 = PreflopTableGen.getPreflopWinRate(myHand.cards3[0],myHand.cards3[1]);
 				winChance = Utils.getMax(winChance0, winChance1, winChance2);
 				
 				return brain.takeAction(opponent, getActionObject, winChance, street);
 				
 			//FLOP
 			case 3:
 				street = 1;
 				for ( int i = 0; i < getActionObject.legalActions.length; i++ ) {
 					LegalActionObject action = getActionObject.legalActions[i];
 					if ( action.actionType.equalsIgnoreCase("discard") ) {
 						return discardHelper(getActionObject);
 					}
 				}
 				
 				winChance2 = StochasticSimulator.computeRates(new int[] {myHand.cards3[0], myHand.cards3[1]}, getActionObject.boardCards, FLOP_SIM_ITERS)[10];
 				winChance1 = StochasticSimulator.computeRates(new int[] {myHand.cards3[0], myHand.cards3[2]}, getActionObject.boardCards, FLOP_SIM_ITERS)[10];
 				winChance0 = StochasticSimulator.computeRates(new int[] {myHand.cards3[1], myHand.cards3[2]}, getActionObject.boardCards, FLOP_SIM_ITERS)[10];
 				winChance = Utils.getMax(winChance0, winChance1, winChance2);
 				
 				return brain.takeAction(opponent, getActionObject, winChance, street);
 			
 				
 			//TURN
 			case 4:
 				street = 2;
 				winChance = StochasticSimulator.computeRates(myHand.cards2, getActionObject.boardCards, TURN_SIM_ITERS)[10];
 				
 				return brain.takeAction(opponent, getActionObject, winChance, street);
 				
 			//RIVER
 			case 5:
 				street = 3;
 				winChance = StochasticSimulator.computeRates(myHand.cards2, getActionObject.boardCards, RIVER_SIM_ITERS)[10];
 				
 				return brain.takeAction(opponent, getActionObject, winChance, street);
 				
 			default:
 				break;
 		}
 		
 		return "FOLD-BAD";
 	}
 	
 	//decides which card to discard, and populates new cards2 in myHand
 	private String discardHelper(GetActionObject getActionObject) {
 		
 		float p01 = StochasticSimulator.computeRates(new int[] {myHand.cards3[0], myHand.cards3[1]}, getActionObject.boardCards, DISCARD_SIM_ITERS)[10];
 		float p12 = StochasticSimulator.computeRates(new int[] {myHand.cards3[1], myHand.cards3[2]}, getActionObject.boardCards, DISCARD_SIM_ITERS)[10];
 		float p02 = StochasticSimulator.computeRates(new int[] {myHand.cards3[0], myHand.cards3[2]}, getActionObject.boardCards, DISCARD_SIM_ITERS)[10];
 		float max = p01;
 		int toss = 2;
 		if (p12 > max){
 			max = p12;
 			toss = 0;
 		}
 		if (p02 > max){
 			max = p02;
 			toss = 1;
 		}
 		myHand.remove(toss);
 		return "DISCARD:"+HandEvaluator.cardToString(myHand.cards3[toss]);
 		
 	}
 	
 }
