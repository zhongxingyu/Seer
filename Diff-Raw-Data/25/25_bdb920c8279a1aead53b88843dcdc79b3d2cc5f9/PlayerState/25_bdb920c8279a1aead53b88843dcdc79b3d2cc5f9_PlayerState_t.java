 package org.aimas.craftingquest.state;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 import org.aimas.craftingquest.core.GamePolicy;
 import org.aimas.craftingquest.state.objects.Tower;
 import org.aimas.craftingquest.state.objects.TrapObject;
 
 /**
  * The central client state class. It contains information about the player's progress in the game.
  */
 @SuppressWarnings("serial")
 public class PlayerState implements Serializable {
 	/* state */
 	/**
 	 * the unique player identifier 
 	 */
 	public Integer id;
 	
 	/**
 	 * the list of <code>UnitStates</code> - the player's units
 	 */
 	public List<UnitState> units;
 
 	/**
 	 * the frozen units. Value is time until they are unfrozen.
 	 */
 	private Map<UnitState, Integer> frozenUnits;
 	
 	/**
 	 * the list of game wide available blueprints. 
 	 * <p>This structure will be initialized automatically at start up.</p>
 	 * <p>It contains all the blueprints available for purchase in the game. </p>
 	 * <p>Players know this list from the very beginning, but cannot construct an object 
 	 * unless they <b>buy</b> the necessary blueprint</p>
 	 */
 	public List<Blueprint> availableBlueprints;		// list of all available blueprints in the game - set at game start 
 	
 	
 	/**
 	 * the list of active player towers
 	 */
 
 	public List<Tower> availableTowers; // stores the state of all the towers built by the player
 	public List<TrapObject> availableTraps; // stores the state of all the towers built by the player
 	public int gold;
 	
 	/* map dimensions */
 	public int mapWidth;
 	public int mapHeight;
 	
 	/* transition */
 	// public List<TransitionResult> responces;
 	/**
 	 * the TransitionResult response for the last transition. It will be null if no transition was made.
 	 */
 	public TransitionResult response;				// last transition response
 
 	/***************************
 	 * Statistics
 	 ***************************/
 	private int kills;
 	private int successfulTraps;
 	private int placedTraps;
 	private int deadUnits;
 	private int placedTowers;
 	private int retaliationKills;
 
 	
 	/* game mechanics time */
 	/**
 	 * the round information
 	 */
 	public RoundState round;
 
 	private int killingSprees;
 
 	private int firstBlood;
 	
 	public long currentTime() {
 		return System.currentTimeMillis();
 	}
 
 	public PlayerState() {
 		units = new ArrayList<UnitState>();
 		frozenUnits = new HashMap<UnitState, Integer>();
 		//responces = new ArrayList<TransitionResult>();
 		availableBlueprints = new ArrayList<Blueprint>();
 		availableTowers = new ArrayList<Tower>();
 		availableTraps = new ArrayList<TrapObject>();
 		
 		round = new RoundState();
 
 		/* initial statistics */
 		kills = 0;
 		successfulTraps = 0;
 		placedTraps = 0;
 		deadUnits = 0;
 		placedTowers = 0;
 		retaliationKills = 0;
 		killingSprees = 0;
 		firstBlood = 0;
 	}
 
 	@Override
 	public String toString() {
 		String info = "";
 		info += "PlayerState: \n";
 		info += "    id:" + id + "\n";
 		info += "    gold:" + gold + "\n";
 		info += "    round:" + round.currentRound + " of " + round.noRounds + "\n";
 		info += "    Blueprints: \n";
 		for (Blueprint bp: availableBlueprints) {
 			info += "        " + bp + "\n";
 		}
 		
 		info += "    Units:" + units.size() + "\n";
 		for (UnitState unit : units) {
 			info += "        " + unit + "\n";
 		}
 
 		/* statistics */
 		info += "    Kills: " + kills + "\n";
 		info += "    Retaliation Kills: " + retaliationKills + "\n";
 		info += "    Dead units: " + deadUnits + "\n"; 
 		info += "    Placed towers: " + placedTowers + "\n";
 		info += "    Traps: " + successfulTraps + "/" + placedTraps + "\n";
 		return info;
 	}
 
 	public void die() {
 		deadUnits++;
 	}
 	
 	public int getDeadUnits() {
 		return deadUnits;
 	}
 	
 	public int getKills() {
 		return kills;
 	}
 	
 	public int getRetaliationKills() {
 		return retaliationKills;
 	}
 
 	public void killOne(boolean retaliationKill) {
 		kills++;
 		if (retaliationKill) {
 			retaliationKills++;
 		}
 	}
 	
 	public void placeTrap() {
 		placedTraps++;
 	}
 	
 	public int getPlacedTraps() {
 		return placedTraps;
 	}
 	
 	public void placeTower() {
 		placedTowers++;
 	}
 	
 	public int getPlacedTowers() {
 		return placedTowers;
 	}
 	
 	public void triggerTrap() {
 		successfulTraps++;
 	}
 	
 	public int getSuccessfulTraps() {
 		return successfulTraps;
 	}
 
 	public int getGold() {
 		return gold;
 	}
 
 	public void setGold(int credit) {
 		this.gold = credit;
 	}
 	
 	public float getScore() {
 		float score = (float) kills * (1.0f + (float) placedTowers / (float)GamePolicy.buildTowerBonus);
 		
 		if (placedTraps > 0) {
			float trapScore = ((float)(successfulTraps * placedTraps) / (float)(successfulTraps + placedTraps));
			score = (trapScore == 0) ? score : score * trapScore;
 		}
 		
 		score += killingSprees * GamePolicy.killingSpreeBonus + firstBlood * GamePolicy.firstBloodBonus;
 		
 		return score;
 	}
 	
 	public int getCurentTurn() {
 		return round.currentRound;
 	}
 
 	public void setCurentTurn(int curentTurn) {
 		round.currentRound = curentTurn;
 	}
 	
 	public boolean validLastTransition() {
 		if (response != null) {
 			return response.valid();
 		}
 		
 		return true;
 	}
 	
 	public String getLastTransitionError() {
 		if (response != null) {
 			return response.toString();
 		}
 		
 		return "";
 	}
 
 	public void unfreeze() {
 		List<UnitState> toRemove = new ArrayList<UnitState>();
 
 		for (UnitState u : frozenUnits.keySet()) {
 			int val = frozenUnits.get(u) - 1;
 			if (val == 0) {
 				toRemove.add(u);
 			} else
 				frozenUnits.put(u, val);
 		}
 
 		for (UnitState u : toRemove)
 			frozenUnits.remove(u);
 	}
 
 	public void freeze(UnitState unit, int level) {
 		frozenUnits.put(unit, level);
 	}
 
 	public boolean isFrozen(UnitState unit) {
 		return frozenUnits.containsKey(unit);
 	}
 
 	public int getKillingSprees() {
 		return killingSprees;
 	}
 
 	public int getFirstBlood() {
 		return firstBlood;
 	}
 
 	public void addKillingSpree() {
 		killingSprees++;
 		
 	}
 
 	public void setFirstBlood(int firstBlood) {
 		this.firstBlood = firstBlood;
 		
 	}
 }
