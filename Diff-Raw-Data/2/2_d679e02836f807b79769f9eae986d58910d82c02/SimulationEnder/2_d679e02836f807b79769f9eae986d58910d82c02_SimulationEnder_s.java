 package edu.wheaton.simulator.simulation.end;
 
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import com.google.common.collect.ImmutableMap;
 
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Prototype;
 
 /**
  * Handles the determination of whether or not the simulation needs to end.
  * 
  * @author Daniel Gill, Chris Anderson
  *
  */
 public class SimulationEnder {
 
 	/**
 	 * Index of the time limit condition.
 	 */
 	private static int TIME_CONDITION = 0;
 
 	/**
 	 * Index of the absence of agents condition.
 	 */
 	private static int NO_AGENTS_CONDITION = 1;
 
 	/**
 	 * Index of the population limits condition.
 	 */
 	private static int POPULATION_CONDITIONS = 2;
 
 	/**
 	 * Stores the various kinds of conditions.
 	 */
 	private EndCondition[] conditions;
 
 	/**
 	 * Constructor.
 	 */
 	public SimulationEnder() {
 		conditions = new EndCondition[3];
 		TimeCondition timer = new TimeCondition(0);
 		NoAgentsCondition counter = new NoAgentsCondition();
 		conditions[TIME_CONDITION] = timer;
 		conditions[NO_AGENTS_CONDITION] = counter;
 		conditions[POPULATION_CONDITIONS] = new AgentPopulationCondition();
 	}
 
 	/**
 	 * Set the time limit for the simulation.
 	 * 
 	 * @param maxSteps
 	 *            Number of total iterations to be run until completion.
 	 */
 	public void setStepLimit(int maxSteps) {
 		((TimeCondition) conditions[TIME_CONDITION]).maxSteps = maxSteps;
 	}
 
 	/**
 	 * Get the time limit.
 	 * 
 	 * @return The total amount of iterations the simulation is to run.
 	 */
 	public int getStepLimit() {
 		return ((TimeCondition) conditions[TIME_CONDITION]).maxSteps;
 	}
 
 	/**
 	 * Determine whether the simulation should end.
 	 * 
 	 * @return true if the simulation should end, false otherwise.
 	 */
 	public boolean evaluate(Grid grid) {
 		for (EndCondition condition : conditions)
 			if (condition.evaluate(grid.getStep(), grid))
 				return true;
 		return false;
 	}
 
 	/**
 	 * Get the current population limits.
 	 * 
 	 * @return An immutable map containing the present population restrictions.
 	 */
 	public ImmutableMap<String, Integer> getPopLimits() {
 		return ((AgentPopulationCondition) conditions[POPULATION_CONDITIONS])
 				.getPopLimits();
 	}
 
 	/**
 	 * Set a population limit.
 	 * 
 	 * @param prototypeName
 	 *            Name of relevant prototype.
 	 * @param maxPop
 	 *            Maximum population for that category of agent.
 	 */
 	public void setPopLimit(String name, int maxPop) {
 		((AgentPopulationCondition) conditions[POPULATION_CONDITIONS])
 				.setPopLimit(name, maxPop);
 	}
 
 	/**
 	 * Remove a set population limit.
 	 * 
 	 * @param prototypeName
 	 *            The name of the prototype whose limit is to be removed.
 	 */
 	public void removePopLimit(String name) {
 		((AgentPopulationCondition) conditions[POPULATION_CONDITIONS])
 				.removePopLimit(name);
 	}
 
 	/**
 	 * Determines if the simulation has run out of time.
 	 * 
 	 * @author daniel.gill
 	 */
 	private final class TimeCondition implements EndCondition {
 
 		/**
 		 * The total number of steps the Simulation is permitted to run.
 		 */
 		private int maxSteps;
 
 		/**
 		 * @param maxSteps
 		 *            The total number of steps the Simulation is permitted to
 		 *            run.
 		 */
 		public TimeCondition(int maxSteps) {
 			this.maxSteps = maxSteps;
 		}
 
 		@Override
 		public boolean evaluate(int step, Grid grid) {
 			return step >= maxSteps;
 		}
 	}
 
 	/**
 	 * Determines if there are no more agents in the simulation.
 	 * 
 	 * @author daniel.gill
 	 */
 	private final class NoAgentsCondition implements EndCondition {
 		@Override
 		public boolean evaluate(int step, Grid grid) {
 			for (Agent a : grid) {
 				if (a != null)
 					return false;
 			}
 			return true;
 		}
 	}
 
 	/**
 	 * Determines if the population of the given type has exceeded the given
 	 * max.
 	 * 
 	 * @author daniel.gill
 	 */
 	private final class AgentPopulationCondition implements EndCondition {
 		
 		/**
 		 * The map where the names of prototypes are associated with population
 		 * limits.
 		 */
 		private HashMap<String, Integer> popLimits;
 
 		/**
 		 * Constructor.
 		 */
 		public AgentPopulationCondition() {
 			popLimits = new HashMap<String, Integer>();
 		}
 
 		/**
 		 * Add a new population limit to the simulation.
 		 * 
 		 * @param name
 		 *            The name of the prototype of agent.
 		 * @param maxPop
 		 *            The population that category must not exceed.
 		 */
 		public void setPopLimit(String name, int maxPop) {
 			popLimits.put(name, Integer.valueOf(maxPop));
 		}
 
 		/**
 		 * Remove a population limit from the simulation.
 		 * 
 		 * @param prototypeName
 		 *            The name of the prototype whose limit is to be removed.
 		 */
 		public void removePopLimit(String name) {
 			popLimits.remove(name);
 		}
 
 		/**
 		 * @return An immutable map of all the current
 		 */
 		public ImmutableMap<String, Integer> getPopLimits() {
 			return new ImmutableMap.Builder<String, Integer>().putAll(popLimits).build();
 		}
 
 		@Override
 		public boolean evaluate(int step, Grid grid) {
 			for (String name : popLimits.keySet())
 				if (Prototype.getPrototype(name).childPopulation() >= popLimits
 						.get(name))
 					return true;
 			return false;
 		}
 	}
 	
 	
 	/**
 	 * Take the end conditions and serialize them
 	 *
 	 * @return A string containing the serialized end conditions         
 	 */
 	public String serialize(){
 		String ret = "EndConditions";
 		ret += "\n" + Integer.toString(((TimeCondition) conditions[TIME_CONDITION]).maxSteps);
 		
 		ImmutableMap<String, Integer> populationLimits = 
 				((AgentPopulationCondition) conditions[POPULATION_CONDITIONS]).getPopLimits();
 		
 		for (Entry<String, Integer> entry : populationLimits.entrySet())
			ret += "\nPOP " + entry.getKey() + " " + entry.getValue();
 		
 		
 		return ret;
 	}
 	
 }
