 package riskarena.riskbots.evaluation;
 
 import java.util.ArrayList;
 
 import riskarena.GameInfo;
 import riskarena.OutputFormat;
 import riskarena.Risk;
 import riskarena.riskbots.evaluation.evals.AbstractEvaluator;
 import riskarena.riskbots.evaluation.evals.ArmyConsolidationEvaluator;
 import riskarena.riskbots.evaluation.evals.BestEnemyEvaluator;
 import riskarena.riskbots.evaluation.evals.EnemyContinentsEvaluator;
 import riskarena.riskbots.evaluation.evals.FortifiedTerritoriesEvaluator;
 import riskarena.riskbots.evaluation.evals.FrontierDistanceEvaluator;
 import riskarena.riskbots.evaluation.evals.ObtainedCardEvaluator;
 import riskarena.riskbots.evaluation.evals.OccupiedTerritoriesEvaluator;
 import riskarena.riskbots.evaluation.evals.OwnArmiesEvaluator;
 import riskarena.riskbots.evaluation.evals.OwnContinentsEvaluator;
 import riskarena.riskbots.evaluation.evals.TargetContEvaluator;
 
 public class Evaluation {
 	private GameInfo game;
 	private GameStats stats;
 	private CardIndicator card;
 	
 	// Internal list of evaluators. A game stat's score is a weighted combination of these.
 	private ArrayList<AbstractEvaluator> evaluators;
 	private final String FULL_DEBUG = "ALL";
 	
 	public Evaluation(GameInfo gi, CardIndicator ci) {
 		game = gi;
 		card = ci;
 		stats = new GameStats(game);
 		evaluators = new ArrayList<AbstractEvaluator>();
 		registerEvaluators();
 	}
 	
 	private void registerEvaluators() {
 		evaluators.clear();
 		evaluators.add( new OwnContinentsEvaluator("OwnContinents", 4.0, stats, game) );
 		evaluators.add( new EnemyContinentsEvaluator("EnemyContinents", 3.0, stats, game) );
 		evaluators.add( new OwnArmiesEvaluator("OwnArmies", 1.0, stats, game) );
 		evaluators.add( new BestEnemyEvaluator("BestEnemy", 1.0, stats, game) );
 		evaluators.add( new FortifiedTerritoriesEvaluator("FortifiedTerritories", 0.5, stats, game) );
 		evaluators.add( new OccupiedTerritoriesEvaluator("OccupiedTerritories", 1.0, stats, game) );
 		evaluators.add( new FrontierDistanceEvaluator("FrontierDistance", 1.0, stats, game) );
 		evaluators.add( new ObtainedCardEvaluator("ObtainedCard", 1.0, stats, game, card) );
 		evaluators.add( new ArmyConsolidationEvaluator("ArmyConsolidation", 0.5, stats, game) );
 		evaluators.add( new TargetContEvaluator("TargetCont", 3.0, stats, game) );
 	}
 	
 	/*
 	 * Returns the score of the board state inherent in GameInfo
 	 */
 	public double score() {
 		return score(false, null);
 	}
 	
 	/*
 	 * Returns the score of the board state that would result from applying
 	 * the ArrayList of army changes.
 	 */
 	public double score(ArrayList<ArmyChange> changes) {
 		if(changes.isEmpty())
 			return score();
 		//System.out.println(game.getMyName() + " " + stats.getCountries()[changes.get(0).ID()].getName());
 		stats.apply(changes);
 		double result = 0.0;
 		for(AbstractEvaluator e : evaluators) {
 			double score = e.getScore(changes);
 			result += e.getWeight() * score;
 			//System.out.println(e.getName() + " " + score);
 		}
 		//System.out.println();
 		stats.unapply(changes);
 		return result;
 	}
 	
 	public double debugScore() {
 		return score(true, FULL_DEBUG);
 	}
 	
 	public double debugScore(String nameOfEvalToDebug) {
 		return score(true, nameOfEvalToDebug);
 	}
 	
 	/*
 	 * Calculate the weighted sum of scores returned by evaluators.
 	 * If debug is true, intermediate calculations are also printed using Risk's static output methods.
 	 * Only the final score and the score of an evaluator with the internal name nameOfEvalToDebug
 	 * is printed, unless nameOfEvalToDebug has the value FULL_DEBUG, in which case they're all printed.
 	 */
 	private double score(boolean debug, String nameOfEvalToDebug) {
 		if(debug && nameOfEvalToDebug == FULL_DEBUG)
 			Risk.sayOutput("Scoring state for " + game.getMyName(), OutputFormat.BLUE, true);
 		double result = 0.0;
 		for(AbstractEvaluator e : evaluators) {
 			double evalScore = e.getScore();
 			if(debug) {
 				if(nameOfEvalToDebug == FULL_DEBUG)
 					Risk.sayOutput(e.getName() + ": " + evalScore, OutputFormat.TABBED, true);
 				else if(nameOfEvalToDebug == e.getName())
 					Risk.sayOutput(game.getMyName() + " " + e.getName() + ": " + evalScore, OutputFormat.BLUE, true);
 			}
 			result += e.getWeight() * evalScore;
 		}
 		if(debug && nameOfEvalToDebug == FULL_DEBUG)
 			Risk.sayOutput("Final score for " + game.getMyName() + ": " + result, OutputFormat.BLUE, true);
 		return result;
 	}
 	
 	/*
 	 * A signal that the game state has changed, and this should be sent on to the
 	 * evaluators and GameStats.
 	 */
 	public void refresh() {
		/*System.out.println(Thread.currentThread().getName() + " refreshing");
		StackTraceElement z[] = Thread.currentThread().getStackTrace();
		for(int i=0; i<z.length; i++)
			System.out.println("\t"+z[i].toString());*/
 		stats.refresh();
 		for(AbstractEvaluator e : evaluators) {
 			e.refresh();
 		}
 	}
 }
