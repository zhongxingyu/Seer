 package riskarena.riskbots.evaluation.evals;
 /*
  * The FrontierDistanceEvaluator uses the distance from each friendly territory
  * to the frontier (the set of friendly territories neighboring enemies)
  * It calculates the average distance it takes for each "moveable" army (ie all except the single ones
  * that must always be stationed at a territory) to get to the frontier.
  * The formula is: (moveableArmies * maxFrontierDist - sumOverFriendlyTerritories[ #moveableArmiesinTerritory * frontierDist ])
  * 								/ TotalFriendlyArmies * maxFrontierDist
  * where moveableArmies = TotalFriendlyArmies - numFriendlyTerritories
  */
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 
 import com.sun.tools.javac.util.Pair;
 
 import riskarena.CountryInfo;
 import riskarena.GameInfo;
 import riskarena.OutputFormat;
 import riskarena.Risk;
 import riskarena.riskbots.evaluation.ArmyChange;
 import riskarena.riskbots.evaluation.GameStats;
 
 public class FrontierDistanceEvaluator extends AbstractEvaluator {
 	private double score;
 	private int distances[];
 	private int armies[];
 	private int maxDist;
 	
 	public FrontierDistanceEvaluator(String name, double weight, GameStats stats, GameInfo game) {
 		super(name, weight, stats, game);
 		refresh();
 	}
 	
 	public double getScore() {
 		return score;
 	}
 	
 	public double getScore(ArrayList<ArmyChange> changes) {
 		// Apply army changes
 		for(ArmyChange change : changes) {
 			armies[change.ID()] += change.amount();
 		}
 		double result = recalculate();
 		// Unapply army changes
 		for(ArmyChange change : changes) {
 			armies[change.ID()] -= change.amount();
 		}
 		return result;
 	}
 	
 	public void refresh() {
 		calculateFrontierDistances();
 		CountryInfo countries[] = stats.getCountries();
 		armies = new int[countries.length];
 		for(int i=0; i<countries.length; i++)
 			armies[i] = countries[i].getArmies();
 		score = recalculate();
 	}
 	
 	// Sets the score variable
 	private double recalculate() {
 		int added = 0;
 		for(Integer friendly : stats.getMyCountries()) {
 			added += (armies[friendly] - 1) * distances[friendly];
 		}
 		int worstScore = (stats.getArmiesPerPlayer()[game.me()] - stats.getMyCountries().size()) * maxDist;
 		if(worstScore == 0)
 			return 1.0;		// Everything's on the frontier
 		else
 			return (worstScore - added) / (double)worstScore;
 	}
 	
 	/*
 	 * Filles the distnaces array where distances[i] is the min number of territories needed
 	 * to reach the frontier (undefined for enemy territories)
 	 */
 	private void calculateFrontierDistances() {
 		distances = new int[game.getNumCountries()];
 		maxDist = Integer.MIN_VALUE;
 		
 		CountryInfo countries[] = stats.getCountries();
 		java.util.Arrays.fill(distances, Integer.MAX_VALUE);
 		ArrayDeque< Pair<Integer, Integer> > Q = new ArrayDeque< Pair<Integer, Integer> >();
 		for(Integer frontier : stats.getFrontier()) {
			Q.push( new Pair<Integer, Integer>(frontier, 0) );
 		}
 		while(!Q.isEmpty()) {
 			Pair<Integer, Integer> current = Q.pop();
 			if(countries[current.fst].getPlayer() != game.me())
 				continue;
 			if(distances[current.fst] > current.snd ) {
 				distances[current.fst] = current.snd;
 				int adj[] = stats.getWorld().getAdjacencies(current.fst);
 				for(int a = 0; a<adj.length; a++)
 					Q.push(new Pair<Integer,Integer>(adj[a], current.snd + 1));
 			}
 		}
 		for(Integer friendly : stats.getMyCountries()) {
 			if(distances[friendly] > maxDist)
 				maxDist = distances[friendly];
 		}
 		//for(Integer friendly : stats.getMyCountries())
 		//	Risk.sayOutput(countries[friendly].getName() + ": " + distances[friendly], OutputFormat.BLUE);
 	}
 	
 }
