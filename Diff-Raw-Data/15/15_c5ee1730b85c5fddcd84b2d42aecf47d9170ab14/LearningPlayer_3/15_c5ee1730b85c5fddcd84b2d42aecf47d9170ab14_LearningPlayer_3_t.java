 package pokerbots.player;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import pokerbots.packets.HandOverObject;
 import pokerbots.packets.LegalActionObject;
 import pokerbots.packets.GetActionObject;
 import pokerbots.packets.GameObject;
 import pokerbots.packets.HandObject;
 import pokerbots.packets.PerformedActionObject;
 import pokerbots.utils.BettingBrain_old_v2;
 import pokerbots.utils.HandEvaluator;
 import pokerbots.utils.MatchHistory;
 import pokerbots.utils.PreflopTableGen;
 import pokerbots.utils.StatAggregator;
 import pokerbots.utils.StatAggregator.OpponentStats;
 import pokerbots.utils.StochasticSimulator;
 import pokerbots.utils.Utils;
 
 
 /**
  * 
  */
 public class LearningPlayer_3 {
 	
 	//number of iterations for our simulator to calculate probabilities before deciding which card to toss.
 	private final int DISCARD_SIM_ITERS = 500;
 	//number of iterations for calculating probabilities after each other street 
 	private final int FLOP_SIM_ITERS = 500;
 	private final int TURN_SIM_ITERS = 300;
 	private final int RIVER_SIM_ITERS = 200;
 	//minimum default percentage range of winning to play each street.
 	private final float[][] MIN_WIN_TO_PLAY = new float[][] {{0.2f, 0.4f}, {0.2f, 0.4f}, {0.3f, 0.5f}, {0.3f, 0.5f}};
 	//scaling for larger bets on later streets
 	private final float[] CONTINUATION_FACTORS = new float[] {1.0f, 1.0f, 1.5f, 2.0f};
 	
 	private final PrintWriter outStream;
 	private final BufferedReader inStream;
 	private GameObject myGame;
 	private HandObject myHand;
 	private BettingBrain_old_v2 brain;
 	private StatAggregator aggregator;
 	private OpponentStats opponent;
 	private MatchHistory history;
 	private int potSize;
 
 	public LearningPlayer_3(PrintWriter output, BufferedReader input) {
 		this.outStream = output;
 		this.inStream = input;
 		aggregator = new StatAggregator(); // TODO: initialize?
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
 
 		switch ( numBoardCards ) {
 			//PREFLOP
 			case 0:
 				int street = 0;
 				float winChance0 = PreflopTableGen.getPreflopWinRate(myHand.cards3[1],myHand.cards3[2]);
 				float winChance1 = PreflopTableGen.getPreflopWinRate(myHand.cards3[0],myHand.cards3[2]);
 				float winChance2 = PreflopTableGen.getPreflopWinRate(myHand.cards3[0],myHand.cards3[1]);
 				float winChance = Utils.getMax(winChance0, winChance1, winChance2);
 				if ( winChance > getMinWinChance(street) )
 					return betRaiseCall(getActionObject,winChance,street);
 				else
 					return foldOrCheck(getActionObject);
 				
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
 				float maxChance = Utils.getMax(winChance0, winChance1, winChance2);
 				if ( maxChance > getMinWinChance(street) )
 					return betRaiseCall(getActionObject, maxChance,street);
 				else
 					return foldOrCheck(getActionObject);
 				
 			//TURN
 			case 4:
 				street = 2;
 				winChance = StochasticSimulator.computeRates(myHand.cards2, getActionObject.boardCards, TURN_SIM_ITERS)[10];
 				if ( winChance > getMinWinChance(street) )
 					return betRaiseCall(getActionObject, winChance,street);
 				else
 					return foldOrCheck(getActionObject);
 			
 			//RIVER
 			case 5:
 				street = 3;
 				winChance = StochasticSimulator.computeRates(myHand.cards2, getActionObject.boardCards, RIVER_SIM_ITERS)[10];
 				if ( winChance > getMinWinChance(street) )
 					return betRaiseCall(getActionObject, winChance,street);
 				else
 					return foldOrCheck(getActionObject);
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
 
 	// uses opponent's aggression to scale our looseness -- higher opp aggression = we play tighter
 	public float getMinWinChance(int street){
 		return Utils.scale(opponent.getTotalAggression(), 0.0f, 1.0f, MIN_WIN_TO_PLAY[street][0], MIN_WIN_TO_PLAY[street][1]);
 	}
 	
 	public int makeProportionalBet(float expectedWinPercentage, int minBet, int maxBet, int myRemainingStack){
 		return (int) ( 2 * (expectedWinPercentage - .5) * (maxBet - minBet) + minBet);
 	}
 	
 	// TODO: uses opponent's looseness to scale our bets -- higher opp looseness = we play more aggressively
 	public String betRaiseCall( GetActionObject getActionObject, float winChance, int street ) {
 		for ( int i = 0; i < getActionObject.legalActions.length; i++ ) {
 			LegalActionObject action = getActionObject.legalActions[i];
 		
 			if ( action.actionType.equalsIgnoreCase("bet") ) {
 				int min = action.minBet;
 				int max = action.maxBet;
 				int bet = (int)(makeProportionalBet(winChance,min,max,getActionObject.potSize/2));
 				bet *= opponent.getTotalLooseness();
 				if (bet < min)
 					bet = min;
 				return "BET:"+bet;
 			}
 			else if ( action.actionType.equalsIgnoreCase("call") ) {
 				return "CALL";
 			}
 		}
 		return "FOLD";
 	}
 	
 	public String foldOrCheck( GetActionObject curr ) {
 		for ( int i = 0; i < curr.legalActions.length; i++ ) {
 			LegalActionObject action = curr.legalActions[i];
 			if ( action.actionType.equalsIgnoreCase("check") ) {
 				return "CHECK";
 			}
 		}
 		return "FOLD";
 	}
 }
