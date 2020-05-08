 package player;
 
 import java.util.ArrayList;
 import java.lang.Math;
 
 /**
  * Picks a strategy to use at random.
  * Uses exponential back off such that winning strategies will be more likely to be chosen and losing strategies less likely.
  * 
 * TODO: figure out how to pick new strategy every hand
 * TODO: figure out how to signal to the brain if a hand has won or lost.
 * 
  * @author DC
  *
  */
 public class MultiStrategy extends StrategyBrain {
 	protected StrategyBrain[] strategies;
 	protected int[] prob_strategies;
 	protected ArrayList<Integer> strategy_employed;
 	public MultiStrategy(Match _match, Opponent opponent) {
 		super(_match, opponent);
 		strategies = new StrategyBrain[2];
 		strategies[0] = new ExpectedReturnStrategy(match,opponent);
 		strategies[1] = new RandomizedOverBet(match,opponent);
 		prob_strategies = new int[strategies.length];
 		for(int i=0;i<prob_strategies.length;i++){
 			prob_strategies[i] = match.stackSize;
 		}
 	}
 	@Override
 	public void newHand(){
 		//Uses the results of the last hand to evaluate the last strategy used.
 		prob_strategies[strategy_employed.get(strategy_employed.size()-1)] 
						+= match.handResults.get(strategy_employed.size()-1);
 		//Sum the values in prob_strategies and pick a value between zero and the sum.
 		int total_strat_prob =0;
 		for(int i: prob_strategies){
 			total_strat_prob += i;
 		}
 		int stratNum = (int) Math.round(Math.random()*total_strat_prob);
 		//Find the strategy this corresponds to.
 		for(int j: prob_strategies){
 			stratNum -= j;
 			if(stratNum < 0){
 				strategy_employed.add(j);
 				break;
 			}
 		}
 		
 	}
 	@Override
 	public String bet(int minBet, int maxBet) {
 		return (strategies[strategy_employed.get(strategy_employed.size()-1)]).bet(minBet, maxBet);
 	}
 
 	@Override
 	String raise(int minBet, int maxBet) {
 		return (strategies[strategy_employed.get(strategy_employed.size()-1)]).raise(minBet, maxBet);
 	}
 
 }
