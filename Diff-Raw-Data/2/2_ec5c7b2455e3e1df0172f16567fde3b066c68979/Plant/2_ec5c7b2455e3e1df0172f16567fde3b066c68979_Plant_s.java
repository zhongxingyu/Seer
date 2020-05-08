 package ReactorEE.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import ReactorEE.pcomponents.*;
 
 
 
 /**
  * Plant class is the holder of all plant components, the score, time steps passed, the name of the
  * player, etc. It represents the "model" of the MVC model of the game.
  * 
  * @author Lamprey
  */
 public class Plant implements Serializable {
 	/**
 	 * serialVersionUID: http://docs.oracle.com/javase/7/docs/api/java/io/Serializable.html
 	 */
 	private static final long serialVersionUID = 4799981348038802742L;
 	
 	private final static int MAX_STEAM_FLOW_RATE = 500; // Out of the reactor.
 	
 	// the maximum flow rate for a pump when it is on and not broken
 	private final static int MAX_WATER_FLOW_RATE_PER_PUMP = 400;
 	
 	// the name of the operator
 	private String operatorName;
 	
 	// is the game over
 	private boolean gameOver;
 	
 	//how many timesteps have passed since the plant is created
 	//used to calculate the score - the more timesteps have passed
 	//the higher the score
 	private int timeStepsUsed;
 	
 	//operator's score
 	private int score;
 	
 	//a list of components that are being repaired
 	private List<Repair> beingRepaired;
 	
 	//is the game paused
 	private boolean isPaused;
 	
 	//a list of all the high scores
	private List<HighScore> highScores;
 	
 	//a list with all the functional components of the plant
 	private List<PlantComponent> plantComponents;
 	
 	//a list of components that have failed
 	private List<PlantComponent> failedComponents;
 	
 	
 	private Reactor reactor;
 	private List<Valve> valves;
 	private List<ConnectorPipe> connectorPipes;
 	private Condenser condenser;
 	private List<Pump> pumps;
 	private Turbine turbine;
 	private Generator generator;
 	private OperatingSoftware operatingSoftware;
 	
 	/**
 	 * This is the default constructor that is used 
 	 * when there is no saved game (i.e. new game)
 	 */
 	public Plant() {
 		this.operatorName = null;
 		this.gameOver = false;
 		this.timeStepsUsed = 0;
 		this.score = 0;
 		this.beingRepaired = new ArrayList<Repair>();
 		this.isPaused = false;
 		//this.highScores = new ArrayList<HighScore>();
 		this.plantComponents = new ArrayList<PlantComponent>();
 		this.failedComponents = new ArrayList<PlantComponent>();
 	}
 	
 	/**
 	 * 
 	 * @return name of the operator (player)
 	 */
 	public String getOperatorName() {
 		return operatorName;
 	}
 	
 	/**
 	 * Sets the name of the operator (player).
 	 * 
 	 * @param operatorName name of the operator (player)
 	 */
 	public void setOperatorName(String operatorName) {
 		this.operatorName = operatorName;
 	}
 	
 	/**
 	 * 
 	 * @return current score
 	 */
 	public int getScore() {
 		return score;
 	}
 
 	/**
 	 * Updates the score.
 	 * 
 	 * Calculates the score based on the power output of the generator
 	 * and the number of time steps passed since the start of the game.
 	 */
 	public void calcScore() {
 		int powerOutput = getGenerator().getPowerOutput();
 		this.score += powerOutput * (/*this.timeStepsUsed + */10);
 	}
 
 	/**
 	 * Checks if the game is paused.
 	 * Currently not used as the game is turn based. Makes creating a real time game easier.
 	 * 
 	 * @return true if the game is paused
  	 */
 	public boolean isPaused() {
 		return isPaused;
 	}
 
 	/**
 	 * Sets the paused state of the game.
 	 * Currently not used as the game is turn based. Makes creating a real time game easier.
 	 * 
 	 * @param isPaused whether to pause or resume the game
 	 */
 	public void setPaused(boolean isPaused) {
 		this.isPaused = isPaused;
 	}
 	
 	/**
 	 * 
 	 * @return maximum rate at which steam can flow out of the reactor 
 	 */
 	public int getMaxSteamFlowRate()
 	{
 		return MAX_STEAM_FLOW_RATE;
 	}
 
 	/**
 	 * 
 	 * @return maximum rate at which water can flow through a pump
 	 */
 	public int getMaxWaterFlowRatePerPump()
 	{
 		return MAX_WATER_FLOW_RATE_PER_PUMP;
 	}
 
 	/**
 	 *
 	 * @return the reactor object of the plant
 	 */
 	public Reactor getReactor() {
 		if (this.reactor != null) {
 			return reactor;
 		} else {
 			for (PlantComponent pc : this.plantComponents) {
 				if (pc instanceof Reactor) {
 					this.reactor = (Reactor) pc;
 					return this.reactor;
 				}
 			}
 			return null; // No reactor found?!
 		}
 	}
 	
 	/**
 	 * 
 	 * @return a list of valves in the plant
 	 */
 	public List<Valve> getValves() {
 		ArrayList<Valve> valvesList = new ArrayList<Valve>();
 		if (this.valves != null) {
 			return this.valves;
 		} else {
 			for (PlantComponent pc : this.plantComponents) {
 				if (pc instanceof Valve) valvesList.add((Valve) pc);
 			}
 			this.valves = valvesList;
 			return this.valves;
 		}
 	}
 
 	/**
 	 * 
 	 * @return a list of all connector pipes in the plant
 	 */
 	public List<ConnectorPipe> getConnectorPipes()
 	{
 		ArrayList<ConnectorPipe> connectorPipes = new ArrayList<ConnectorPipe>();
 		if (this.connectorPipes != null) {
 			return this.connectorPipes;
 		} else {
 			for (PlantComponent pc : this.plantComponents) {
 				if (pc instanceof ConnectorPipe) connectorPipes.add((ConnectorPipe) pc);
 			}
 			this.connectorPipes = connectorPipes;
 			return this.connectorPipes;
 		}
 	}
 
 	/**
 	 * 
 	 * @return the condenser of the plant
 	 */
 	public Condenser getCondenser()
 	{
 		if (this.condenser != null) {
 			return this.condenser;
 		} else {
 			for (PlantComponent pc : this.plantComponents) {
 				if (pc instanceof Condenser) {
 					this.condenser = (Condenser) pc;
 					return this.condenser;
 				}
 			}
 			return null; // No condenser found?!
 		}
 	}
 	
 	/**
 	 * 
 	 * @return a list of all pumps in the plant
 	 */
 	public List<Pump> getPumps() {
 		ArrayList<Pump> pumpsList = new ArrayList<Pump>();
 		if (this.pumps != null) {
 			return this.pumps;
 		}
 		for (PlantComponent pc : this.plantComponents) {
 			if (pc instanceof Pump) pumpsList.add((Pump) pc);
 		}
 		this.pumps = pumpsList;
 		return pumpsList;
 	}
 	
 	/**
 	 * 
 	 * @return the turbine of the plant.
 	 */
 	public Turbine getTurbine() {
 		Turbine turbine = null;
 		if (this.turbine != null) {
 			return this.turbine;
 		}
 		for (PlantComponent pc : this.plantComponents) {
 			if (pc instanceof Turbine) turbine = (Turbine) pc;
 		}
 		this.turbine = turbine;
 		return turbine;
 	}
 	
 	/**
 	 * 
 	 * @return the generator of the plant
 	 */
 	public Generator getGenerator() {
 		Generator generator = null;
 		if (this.generator != null) {
 			return this.generator;
 		}
 		for (PlantComponent pc : this.plantComponents) {
 			if (pc instanceof Generator) generator = (Generator) pc;
 		}
 		this.generator = generator;
 		return generator;
 	}
 	
 	/**
 	 * 
 	 * @return the operating software of the plant
 	 */
 	public OperatingSoftware getOperatingSoftware() {
 		OperatingSoftware operatingSoftware = null;
 		if (this.operatingSoftware != null) {
 			return this.operatingSoftware;
 		}
 		for (PlantComponent pc : this.plantComponents) {
 			if (pc instanceof OperatingSoftware) operatingSoftware = (OperatingSoftware) pc;
 		}
 		this.operatingSoftware = operatingSoftware;
 		return operatingSoftware;
 	}
 
 	/**
 	 * 
 	 * @return a list of all highscores (maximum 20)
 	 */
 	public synchronized List<HighScore> getHighScores() {
 		if(this.highScores.size() > 20) {
 			ArrayList<HighScore> hs = new ArrayList<HighScore>();
 			for(int i = 0; i < 20; i++){
 				hs.add(new HighScore(highScores.get(i).getName(), highScores.get(i).getHighScore()));
 			}
 			//this.highScores = this.highScores.subList(0, 20); //Trims the high scores list to only the first 10 elements
 			highScores = hs;
 		}
 		return this.highScores;
 	}
 	
 	/**
 	 * Sets the list of high scores.
 	 * 
 	 * Used when loading the high scores from a file.
 	 * 
 	 * @param highScores  list of high scores
 	 */
 	public synchronized void setHighScores(List<HighScore> highScores) {
 		this.highScores = highScores;
 	}
 	
 	/**
 	 * 
 	 * @return number of steps passed since the start of the game
 	 */
 	public int getTimeStepsUsed() {
 		return timeStepsUsed;
 	}
 	
 	/**
 	 * Adds "n" steps to timeStepsUsed.
 	 * 
 	 * @param n number of steps to be added
 	 */
 	public void updateTimeStepsUsed(int n) {
 		if (n > 0) timeStepsUsed += n;
 	}
 
 	/**
 	 * 
 	 * @return  list of all components that are being repaired
 	 */
 	public List<Repair> getBeingRepaired() {
 		return beingRepaired;
 	}
 
 	/**
 	 * 
 	 * @return list of all plant components
 	 */
 	public List<PlantComponent> getPlantComponents() {
 		return plantComponents;
 	}
 	
 	/**
 	 * 
 	 * @param plantComponents   list of all plant components
 	 */
 	public void setPlantComponents(List<PlantComponent> plantComponents) {
 		this.plantComponents = plantComponents;
 	}
 	
 	/**
 	 * 
 	 * @return all failed (non-operational) components (including those that are being repaired)
 	 */
 	public List<PlantComponent> getFailedComponents() {
 		return failedComponents;
 	}
 	
 	/**
 	 * Adds failedComponent to failedComponents List, as long as it is
 	 * not already in the list.
 	 * 
 	 * @param failedComponent
 	 */
 	public void addFailedComponent(PlantComponent failedComponent) {
 		if (!this.failedComponents.contains(failedComponent)) {
 			this.failedComponents.add(failedComponent);
 		}
 	}	
 	
 	/**
 	 * Sets the game over state to true.
 	 */
 	public void gameOver() {
 		this.gameOver = true;
 	}
 	
 	/**
 	 * Checks  if the game is over
 	 * 
 	 * @return true if the game is over, false otherwise
 	 */
 	public boolean isGameOver() {
 		return this.gameOver;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((beingRepaired == null) ? 0 : beingRepaired.hashCode());
 		result = prime * result
 				+ ((condenser == null) ? 0 : condenser.hashCode());
 		result = prime * result
 				+ ((connectorPipes == null) ? 0 : connectorPipes.hashCode());
 		result = prime
 				* result
 				+ ((failedComponents == null) ? 0 : failedComponents.hashCode());
 		result = prime * result + (gameOver ? 1231 : 1237);
 		result = prime * result
 				+ ((generator == null) ? 0 : generator.hashCode());
 		result = prime * result
 				+ ((highScores == null) ? 0 : highScores.hashCode());
 		result = prime * result + (isPaused ? 1231 : 1237);
 		result = prime
 				* result
 				+ ((operatingSoftware == null) ? 0 : operatingSoftware
 						.hashCode());
 		result = prime * result
 				+ ((operatorName == null) ? 0 : operatorName.hashCode());
 		result = prime * result
 				+ ((plantComponents == null) ? 0 : plantComponents.hashCode());
 		result = prime * result + ((pumps == null) ? 0 : pumps.hashCode());
 		result = prime * result + ((reactor == null) ? 0 : reactor.hashCode());
 		result = prime * result + score;
 		result = prime * result + timeStepsUsed;
 		result = prime * result + ((turbine == null) ? 0 : turbine.hashCode());
 		result = prime * result + ((valves == null) ? 0 : valves.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Plant other = (Plant) obj;
 		if (beingRepaired == null) {
 			if (other.beingRepaired != null)
 				return false;
 		} else if (!beingRepaired.equals(other.beingRepaired))
 			return false;
 		if (condenser == null) {
 			if (other.condenser != null)
 				return false;
 		} else if (!condenser.equals(other.condenser))
 			return false;
 		if (connectorPipes == null) {
 			if (other.connectorPipes != null)
 				return false;
 		} else if (!connectorPipes.equals(other.connectorPipes))
 			return false;
 		if (failedComponents == null) {
 			if (other.failedComponents != null)
 				return false;
 		} else if (!failedComponents.equals(other.failedComponents))
 			return false;
 		if (gameOver != other.gameOver)
 			return false;
 		if (generator == null) {
 			if (other.generator != null)
 				return false;
 		} else if (!generator.equals(other.generator))
 			return false;
 		if (highScores == null) {
 			if (other.highScores != null)
 				return false;
 		} else if (!highScores.equals(other.highScores))
 			return false;
 		if (isPaused != other.isPaused)
 			return false;
 		if (operatingSoftware == null) {
 			if (other.operatingSoftware != null)
 				return false;
 		} else if (!operatingSoftware.equals(other.operatingSoftware))
 			return false;
 		if (operatorName == null) {
 			if (other.operatorName != null)
 				return false;
 		} else if (!operatorName.equals(other.operatorName))
 			return false;
 		if (plantComponents == null) {
 			if (other.plantComponents != null)
 				return false;
 		} else if (!plantComponents.equals(other.plantComponents))
 			return false;
 		if (pumps == null) {
 			if (other.pumps != null)
 				return false;
 		} else if (!pumps.equals(other.pumps))
 			return false;
 		if (reactor == null) {
 			if (other.reactor != null)
 				return false;
 		} else if (!reactor.equals(other.reactor))
 			return false;
 		if (score != other.score)
 			return false;
 		if (timeStepsUsed != other.timeStepsUsed)
 			return false;
 		if (turbine == null) {
 			if (other.turbine != null)
 				return false;
 		} else if (!turbine.equals(other.turbine))
 			return false;
 		if (valves == null) {
 			if (other.valves != null)
 				return false;
 		} else if (!valves.equals(other.valves))
 			return false;
 		return true;
 	}
 }
