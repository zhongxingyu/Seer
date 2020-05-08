 package pokerbots.utils;
 
 import java.util.HashMap;
 
 import pokerbots.brains.GenericBrain;
 import pokerbots.packets.GameObject;
 import pokerbots.packets.HandObject;
 import pokerbots.packets.PerformedActionObject;
 import pokerbots.regression.CrossSectionModel;
 import pokerbots.regression.LinearModel;
 import pokerbots.regression.Model2D;
 import pokerbots.regression.Model3D;
 import pokerbots.regression.PlanarModel;
 import pokerbots.utils.MatchHistory.Round;
 
 public class StatAggregator {
 	
 	/*
 	 * This class collects data per hand and aggregates it across matches.
 	 * 
 	 * Per street:	(% fold, call, raise) to a bet
 	 * 				(% fold, call, raise) to a raise
 	 * 				(% check, bet) on action
 	 * 				In addition to frequencies, we also have values of the bets/raises.
 	 * 
 	 * Every time there is a showdown:
 	 * 		we bucket probabilities of hand success at each street, and calculate 
 	 * 			- average bets per street
 	 * 			- bet to call ratio (later)
 	 * 		
 	 * ALL VALUES SHOULD BE NORMALIZED TO POT SIZE
 	 * 
 	 * Map from opponent names to MatchStats objects
 	 * 
 	 * Implement serializer and deserializer into Key Value pairs
 	 */
 	
 	private HashMap<String, OpponentStats> m;
 	
 	//Generic constructor
 	public StatAggregator(){
 		m = new HashMap<String, OpponentStats>();
 	}
 	
 	public OpponentStats getOrCreateOpponent(GameObject game) {
 		String oppName = game.oppName;
 		if (m.get(oppName) == null){
 			m.put(oppName, new OpponentStats(oppName, game));
 		}
 		return m.get(oppName);
 	}
 	
 	//Deserialize TODO
 	public StatAggregator(String keyValuePairs){
 		
 	}
 	
 	//Serialize TODO
 	public String Serializer(){
 		return "";
 	}
 	
 	
 	public class OpponentStats{
 		
 		//store data about how different brains perform against different opponents
 		public HashMap<String, Integer> brainScores;
 		public HashMap<String, Integer> brainHands;
 		
 		public float getAvgBrainScore(String brainName){
 			return (float)(brainScores.get(brainName)) / brainHands.get(brainName);
 		}
 		
 		String name;
 		int startingStackSize;
 		public int totalHandCount;
 		
 		public Model2D[] P_Check_given_Check;  	// Prob = a + b*theirPredWin
 		public Model2D[] P_Bet_given_Check;		// Prob = a + b*theirPredWin
 		
 		public Model3D[] P_Fold_given_Bet;
 		public Model3D[] P_Call_given_Bet;
 		public Model3D[] P_Raise_given_Bet;
 		
 		public Model3D[] P_Fold_given_Raise;
 		public Model3D[] P_Call_given_Raise;
 		public Model3D[] P_Raise_given_Raise;
 		
 		public Model2D[] value_Raise_given_their_winChance;
 		public Model2D[] value_Bet_given_their_winChance;
 		
 		/*
 		float[] Looseness;
 		float[] Aggression;
 		public final float DEFAULT_LOOSENESS = 0.3f;
 		public final float DEFAULT_AGGRESSION = 0.25f;
 		*/
 		
 		GameObject game;
 		
 		//TODO: make sure this constructor doesn't get run when pulling from KeyValues!!
 		public OpponentStats( String name, GameObject game ){
 			
 			this.startingStackSize = game.stackSize;
 			this.name = name;
 			this.game = game;
 			this.totalHandCount = 0;
 			this.brainScores = new HashMap<String, Integer>(); //total score of each brain
 			this.brainHands = new HashMap<String, Integer>(); //total hands played by each brain
 			
 			P_Check_given_Check = new Model2D[4];
 			P_Bet_given_Check = new Model2D[4];
 			P_Fold_given_Bet = new Model3D[4];
 			P_Call_given_Bet = new Model3D[4];
 			P_Raise_given_Bet = new Model3D[4];
 			P_Fold_given_Raise = new Model3D[4];
 			P_Call_given_Raise = new Model3D[4];
 			P_Raise_given_Raise = new Model3D[4];
 			value_Raise_given_their_winChance = new Model2D[4];
 			value_Bet_given_their_winChance = new Model2D[4];
 			
 			//initialize all regression lines
 			for ( int i = 0; i < 4; i++ ) {
 				P_Check_given_Check[i] = new LinearModel("P(Check|Check)","%win","P",0.7f,0);
 				P_Bet_given_Check[i] = new LinearModel("P(Bet|Check)","%win","P",0.3f,0);
 				
				P_Fold_given_Bet[i] = new CrossSectionModel("P(Fold|Bet)","wager","%win","P",1,-1,1,-1);
				P_Call_given_Bet[i]  = new CrossSectionModel("P(Call|Bet)","wager","%win","P",0,1,0,1);
				P_Raise_given_Bet[i] = new CrossSectionModel("P(Raise|Bet)","wager","%win","P",0,1,0,1);
 				
				P_Fold_given_Raise[i] = new CrossSectionModel("P(Fold|Raise)","wager","%win","P",1,-1,1,-1);
				P_Call_given_Raise[i] = new CrossSectionModel("P(Call|Raise)","wager","%win","P",0,1,0,1);
				P_Raise_given_Raise[i] = new CrossSectionModel("P(Raise|Raise)","wager","%win","P",0,1,0,1);
 				
 				value_Raise_given_their_winChance[i] = new LinearModel("$ Raise per % win","%win","$");
 				value_Bet_given_their_winChance[i] = new LinearModel("$ Bet per % win","%win","$");
 			}
 		}
 		
 		//helper for storing data into values
 		public void updateBrain(String brainName, int amount){
 			if (brainScores.containsKey(brainName)){
 				brainScores.put(brainName, brainScores.get(brainName) + amount);
 				brainHands.put(brainName, brainHands.get(brainName) + 1); 
 			}
 			else{
 				brainScores.put(brainName, amount);
 				brainHands.put(brainName, 1);
 			}
 		}
 
 		public void analyzeRoundData( HandObject hand, Round data ){
 			
 			int street = 0;
 			float myWagerSize = 0; 	//Size of MY wager
 			float hisEstimatedWinChance = 0;
 			
 			// Set the opponents win chance on fact or prediction (FOR PREFLOP)
 			if ( data.showdown ) {
 				hisEstimatedWinChance = data.oppWinRates[street];
 			} else {
 				hisEstimatedWinChance = 0.5f-HandEvaluator.handOdds(hand, data.boardCards[street],400)[10];
 			}
 			
 			for ( int i = 0; i < data.actions.size() - 1; i++ ) {
 				
 				PerformedActionObject prev = data.actions.get(i);
 				PerformedActionObject curr = data.actions.get(i+1);
 
 				String prevA = prev.actionType;
 				String currA = curr.actionType;
 				
 				//Increment street
 				if ( currA.equalsIgnoreCase("deal") ) {
 					street++;
 					System.out.println("######### NEXT STREET ##########");
 					prev = curr;
 					
 					// Set the opponents win chance on fact or prediction (FOR ALL STREETS)
 					if ( data.showdown ) {
 						hisEstimatedWinChance = data.oppWinRates[street];
 					} else {
 						hisEstimatedWinChance = 1.0f-HandEvaluator.handOdds(hand, data.boardCards[street],400)[10];
 					}
 					continue;
 				}
 				
 				//Get my most recent wager
 				if ( prev.actor.equalsIgnoreCase(game.myName) ) {
 					if ( prev.actionType.equals("BET") || prev.actionType.equals("RAISE") ) {
 						myWagerSize = prev.amount/(float)game.stackSize;
 					}
 				}
 				
 				//Check if I got refunded AKA he folded
 				if ( curr.actor.equals(game.myName) && curr.actionType.equalsIgnoreCase("refund") ) {
 					P_Fold_given_Bet[street].addData(myWagerSize, hisEstimatedWinChance, 1);
 					P_Fold_given_Raise[street].addData(myWagerSize, hisEstimatedWinChance, 1);
 				}
 				
 				//IF I perform an action THEN OPP performs action
 				if ( prev.actor.equalsIgnoreCase(game.myName) && curr.actor.equals(game.oppName) ) {
 					if ( prevA.equalsIgnoreCase("bet") || prevA.equalsIgnoreCase("post")) {
 						if ( currA.equalsIgnoreCase("fold") ) {
 							P_Fold_given_Bet[street].addData(myWagerSize, hisEstimatedWinChance, 1);
 							P_Raise_given_Bet[street].addData(myWagerSize, hisEstimatedWinChance, 0);
 							P_Call_given_Bet[street].addData(myWagerSize, hisEstimatedWinChance, 0);
 						}
 						else if ( currA.equalsIgnoreCase("raise") ) {
 							P_Fold_given_Bet[street].addData(myWagerSize, hisEstimatedWinChance, 0);
 							P_Raise_given_Bet[street].addData(myWagerSize, hisEstimatedWinChance, 1);
 							P_Call_given_Bet[street].addData(myWagerSize, hisEstimatedWinChance, 0);
 							value_Raise_given_their_winChance[street].addData(hisEstimatedWinChance, curr.amount);
 						}
 						else if ( currA.equalsIgnoreCase("call") ) {
 							P_Fold_given_Bet[street].addData(myWagerSize, hisEstimatedWinChance, 0);
 							P_Raise_given_Bet[street].addData(myWagerSize, hisEstimatedWinChance, 0);
 							P_Call_given_Bet[street].addData(myWagerSize, hisEstimatedWinChance, 1);
 						}
 					}
 					else if ( prevA.equalsIgnoreCase("raise") ) {
 						if ( currA.equalsIgnoreCase("fold") ) {
 							P_Fold_given_Raise[street].addData(myWagerSize, hisEstimatedWinChance, 1);
 							P_Raise_given_Raise[street].addData(myWagerSize, hisEstimatedWinChance, 0);
 							P_Call_given_Raise[street].addData(myWagerSize, hisEstimatedWinChance, 0);
 						}
 						else if ( currA.equalsIgnoreCase("raise") ) {
 							P_Fold_given_Raise[street].addData(myWagerSize, hisEstimatedWinChance, 0);
 							P_Raise_given_Raise[street].addData(myWagerSize, hisEstimatedWinChance, 1);
 							P_Call_given_Raise[street].addData(myWagerSize, hisEstimatedWinChance, 0);
 							value_Raise_given_their_winChance[street].addData(hisEstimatedWinChance, curr.amount);
 						}
 						else if ( currA.equalsIgnoreCase("call") ) {
 							P_Fold_given_Raise[street].addData(myWagerSize, hisEstimatedWinChance, 0);
 							P_Raise_given_Raise[street].addData(myWagerSize, hisEstimatedWinChance, 0);
 							P_Call_given_Raise[street].addData(myWagerSize, hisEstimatedWinChance, 1);
 						}
 					}
 					else if ( prevA.equalsIgnoreCase("check") ) {
 						if ( currA.equalsIgnoreCase("bet") || currA.equalsIgnoreCase("raise") ) { //can't really raise unless preflop
 							P_Bet_given_Check[street].addData(hisEstimatedWinChance, 1);
 							P_Check_given_Check[street].addData(hisEstimatedWinChance, 0);
 							value_Bet_given_their_winChance[street].addData(hisEstimatedWinChance, curr.amount);
 						}
 						else if ( currA.equalsIgnoreCase("check") ) {
 							P_Bet_given_Check[street].addData(hisEstimatedWinChance, 0);
 							P_Check_given_Check[street].addData(hisEstimatedWinChance, 1);
 						}
 					}
 				}
 				
 				//IF On-Action behavior is seen
 				else if ( (prevA.equalsIgnoreCase("DEAL") || prevA.equalsIgnoreCase("POST")) && curr.actor.equalsIgnoreCase(game.oppName) ) {
 					if ( currA.equalsIgnoreCase("check") ) {
 						P_Bet_given_Check[street].addData(hisEstimatedWinChance, 0);
 						P_Check_given_Check[street].addData(hisEstimatedWinChance, 1);
 					}
 					if ( currA.equalsIgnoreCase("bet") ) {
 						P_Bet_given_Check[street].addData(hisEstimatedWinChance, 1);
 						P_Check_given_Check[street].addData(hisEstimatedWinChance, 0);
 						value_Bet_given_their_winChance[street].addData(hisEstimatedWinChance, curr.amount);
 					}
 				}
 				prev = curr;
 				
 			}
 			
 			totalHandCount++;
 		}
 		
 		public final int[] THRESHOLD_FOR_GENERALIZING = new int[] {3, 3, 3, 3};
 		
 		/*
 		// (0,1) rating of this opponent's aggression (size of bet when he does bet)
 		final float DEFAULT_AGGRESSION = .3f;
 		public float getAggression(int street){
 			float totalBetCount = (P_Raise_given_Bet[street].getN() + P_Raise_given_Raise[street].getN() + P_Bet_given_Check[street].getN());
 			return (totalBetCount > THRESHOLD_FOR_GENERALIZING[street]) ? (float)(totalValueOfBets[street] + totalValueOfRaises[street] ) / (totalBetCount * startingStackSize) : DEFAULT_AGGRESSION;
 		}
 		*/
 		
 		// (0,1) rating of this opponent's looseness (number of calls+raises over number of calls+raises+folds)
 		final float DEFAULT_LOOSENESS= .5f;
 		public float getLooseness(int street){
 			int checks = P_Check_given_Check[street].getN();
 			int calls = P_Call_given_Bet[street].getN() + P_Call_given_Raise[street].getN();
 			int raises = P_Raise_given_Bet[street].getN() + P_Raise_given_Raise[street].getN();
 			int folds = P_Fold_given_Bet[street].getN() + P_Fold_given_Raise[street].getN();
 			System.out.println("Weird preflop numbers: " + checks + " checks, " + calls + " calls, " + raises + " raises, " + folds + " folds");
 			return (calls+raises+folds+checks > THRESHOLD_FOR_GENERALIZING[street]) ? (float)(calls+raises+checks)/(calls+raises+checks+folds) : DEFAULT_LOOSENESS;
 		}
 		
 		
 		public void printStats(GameObject myGame) {
 			String[] streets = new String[]{"Preflop","Flop","Turn","River"};
 			for ( int i = 0; i < 4; i++ ) {
 				System.out.println("<<< STREET : " + streets[i]+" >>>");
 				
 				P_Fold_given_Bet[i].print();
 				P_Call_given_Bet[i].print();
 				P_Raise_given_Bet[i].print();
 				
 				P_Fold_given_Raise[i].print();
 				P_Call_given_Raise[i].print();
 				P_Raise_given_Raise[i].print();
 				
 				P_Check_given_Check[i].print();
 				P_Bet_given_Check[i].print();
 				
 				value_Raise_given_their_winChance[i].print();
 				value_Bet_given_their_winChance[i].print();
 				System.out.println("\n\n\n\n\n\n");
 			}
 		}
 		
 		public void printFinalBrainScores(GameObject myGame, GenericBrain[] brains) {
 			System.out.println("\nBrain scores:");
 			System.out.println("Brain name \t\t score \t\t hands \t\t avg");
 			for (int i=0; i<brainScores.size(); i++){
 				int thisBrainScore = brainScores.get(brains[i].toString());
 				int thisBrainHands = brainHands.get(brains[i].toString());
 				System.out.println(brains[i].toString() + "\t\t" + thisBrainScore + "\t\t" + thisBrainHands + "\t\t" + f(getAvgBrainScore(brains[i].toString())));
 			}
 			System.out.println("END\n\n\n\n\n\n\n\n\n");
 		}
 
 	}
 	
 	public String f(float f){
 		return String.format("%.2f", f);
 	}
 
 }
