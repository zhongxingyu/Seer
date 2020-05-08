 package utilities;
 
 import java.util.BitSet;
 
 import arimaa3.ArimaaEngine;
 import arimaa3.ArimaaMove;
 import arimaa3.MoveList;
 import feature_extractor.FeatureExtractor;
 import utilities.helper_classes.ArimaaState;
 import utilities.helper_classes.GameInfo;
 
 public class HypothesisTest {
 	
 	/** Runs evaluation based on the 30% of the data that is in GameData
 	 * Once we have run our tests, we print out the statistics.
 	 * @param hyp The evaluation hypothesis 
 	 * @param gd The testing game data */
 	public static void test(AbstractHypothesis hyp, GameData gd) { //rename
 		gd.setMode(GameData.Mode.TEST); //ensure that we are testing...
 		
 		AggregateResults totalScore = new AggregateResults();
 		while (gd.hasNextGame()) {
 			GameInfo gi = gd.getNextGame();
 			GameParser gp = new GameParser(gi);
 			
 			AggregateResults ar = new AggregateResults();
 			while (gp.hasNextGameState())
 				evaluateMoveOrdering(ar, hyp, gp.getNextGameState());
 			
 			totalScore.addResult(ar);
 			//???? print ar moves for game????
 			
 		}
 		
 		System.out.println(totalScore); //prints the statistics stored in AggregateResults
 	}
 	
 	/** Given an arimaaState, we run evaluations on the 16k moves
 	 * and see where the expert move lies.
 	 * @param ar The result to update 
 	 * @param hyp The hypothesis
 	 * @param arimaaState The state containing the expert move */
 	private static void evaluateMoveOrdering(AggregateResults ar, AbstractHypothesis hyp, ArimaaState arimaaState){
 		FeatureExtractor fe = new FeatureExtractor(arimaaState.getCurr(), arimaaState.getPrev());
 		ArimaaEngine ai = new ArimaaEngine(); //TODO see if this is stupidly slow
 		MoveList possibleMoves = ai.genRootMoves(arimaaState.getCurr());
 		
 		int numAbove = 0;
 		ArimaaMove expertMove = arimaaState.getNextMove();
 		BitSet bs = fe.extractFeatures( expertMove );
 		double expertWeight = hyp.evaluate(bs);
 		
 		for (ArimaaMove possibleMove : possibleMoves){
 			if ( !possibleMove.equals( expertMove ) ){ //make sure that this is not the expert move
 				bs = fe.extractFeatures(possibleMove);
 				if ( hyp.evaluate(bs) > expertWeight ){ // must be greater than expert in ordering
 					numAbove++;
 				}
 			}
 			
 		}
 		
 		ar.addMove(numAbove, possibleMoves.size());
 	}
 	
 	/** This internal class maintains the aggregate 
 	 * information across each given move and then prints them out with toString()
 	 */
 	private static class AggregateResults {
 		
 		public static final double TOP5PERCENT = 0.05;
 		
 		private int numInTop5Percent = 0; //number of moves where we classified in the top 5%
 		private double sumPercent = 0;  //sum used for average percentile
 		private int numMoves = 0; //number of expert moves
 		
 		public void addResult(AggregateResults otherAr){
 			numInTop5Percent += otherAr.getNumInTop5Percent();
 			sumPercent += otherAr.getSumPercent();
 			numMoves += otherAr.getNumMoves();
 		}
 
 		public void addMove(int numAbove, int totalMoves){
 			numMoves++;
 			double percentage = ( (double) numAbove ) / totalMoves;
 			sumPercent += percentage;
 			if ( percentage <= TOP5PERCENT){
 				numInTop5Percent++;
 			}
 		}
 		
 		public double getAvgEvaluation(){
 			return 1.0 - sumPercent / numMoves;
 		}
 
 		public int getNumInTop5Percent() {
 			return numInTop5Percent;
 		}
 		
 		public double getSumPercent() {
 			return sumPercent;
 		}
 
 		public int getNumMoves() {
 			return numMoves;
 		}
 		
 		public String toString(){
 			double avgEval = getAvgEvaluation() * 100;
			return String.format("The number of expert moves classified in the top 5 percent of the move ordering is : %d / %d\n"+
 									"The average percentile of the expert move classified is : %f\n", 
 									numInTop5Percent, numMoves, avgEval);
 		}
 
 	}
 
 }
