 package heuristics;
 
import problem.Knapsack;
 
 /**
  * Abstract class to extends specific heuristics from it.
  * 
  * @author omegak
  */
 public abstract class Heuristic {
 	
 	/**
 	 * Executes one step of the heuristic producing a new solution.
 	 * 
 	 * @param knapsack The knapsack to be solved.
 	 */
 	public abstract Knapsack executeOnce(Knapsack knapsack);
 }
