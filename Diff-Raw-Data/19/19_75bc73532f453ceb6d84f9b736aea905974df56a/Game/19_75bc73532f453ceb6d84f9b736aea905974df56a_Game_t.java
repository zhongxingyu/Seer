 package edu.harvard.med.hks.model;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 @Entity
 public class Game extends AbstractTimestampEntity {
 	@NotEmpty
 	private String gameId;
	
 	@OneToMany(mappedBy = "game")
 	private List<Slot> slots;
 	/**
 	 * The max payoff temptee can receive when she betrays
 	 */
 	private int maxBetrayPayoff;
 	/**
 	 * Reward payoff.
 	 */
 	private int rewardPayoff;
 	/**
 	 * The probability a betrayal is not caught.
 	 */
 	private double betrayCaughtChance;
 	/**
 	 * The probability a reward is counted as a betrayal.
 	 */
 	private double rewardCaughtAsBetrayalChance;
 	/**
 	 * The probability a temptee survive after each round.
 	 */
 	private double tempteeSurvivalChance;
 	/**
 	 * Number of black marks at which truster stops trusting temptee.
 	 */
 	private int blackMarkUpperLimit;
 	/**
 	 * Init bonus temptee earned.
 	 */
 	private int initTempteeBonus;
 	/**
 	 * Number of game slot.
 	 */
 	private int numberOfGameSlot;
 	
 	/**
 	 * Initial truster bonus.
 	 */
 	private int initialTrusterBonus;
 	
 	/**
 	 * Exchange rate (points/USD).
 	 */
 	private float exchangeRate;
 	
 	/**
 	 * Betrayal Cost.
 	 */
 	private int betrayalCost;
 	
 	private int maxRoundsNum;
 	
 	
 	public int getInitTempteeBonus() { return initTempteeBonus;}
 	public void setInitTempteeBonus(int initTempteeBonus) {
 		this.initTempteeBonus = initTempteeBonus;
 	}
 	
 	public int getBlackMarkUpperLimit() { return blackMarkUpperLimit; }
 	
 	public String getGameId() { return gameId; }
 	
 	public int getMaxBetrayPayoff() {
 		return maxBetrayPayoff;
 	}
 	public void setMaxBetrayPayoff(int maxBetrayPayoff) {
 		this.maxBetrayPayoff = maxBetrayPayoff;
 	}
 	public int getRewardPayoff() {
 		return rewardPayoff;
 	}
 	public void setRewardPayoff(int rewardPayoff) {
 		this.rewardPayoff = rewardPayoff;
 	}
 	public double getBetrayCaughtChance() {
 		return betrayCaughtChance;
 	}
 	public void setBetrayCaughtChance(double betrayCaughtChance) {
 		this.betrayCaughtChance = betrayCaughtChance;
 	}
 	public int getNumberOfGameSlot() {
 		return numberOfGameSlot;
 	}
 	public double getRewardCaughtAsBetrayalChance() {
 		return rewardCaughtAsBetrayalChance;
 	}
 	public List<Slot> getSlots() {
 		return slots;
 	}
 	public double getTempteeSurvivalChance() {
 		return tempteeSurvivalChance;
 	}
 	public void setBlackMarkUpperLimit(int blackMarkUpperLimit) {
 		this.blackMarkUpperLimit = blackMarkUpperLimit;
 	}
 	public void setGameId(String gameId) {
 		this.gameId = gameId;
 	}
 	public void setNumberOfGameSlot(int numberOfGameSlot) {
 		this.numberOfGameSlot = numberOfGameSlot;
 	}
 	public void setRewardCaughtAsBetrayalChance(double rewardCaughtAsBetrayalChance) {
 		this.rewardCaughtAsBetrayalChance = rewardCaughtAsBetrayalChance;
 	}
 	public void setSlots(List<Slot> slots) {
 		this.slots = slots;
 	}
 	public void setTempteeSurvivalChance(double tempteeSurvivalChance) {
 		this.tempteeSurvivalChance = tempteeSurvivalChance;
 	}
 	
 	public int getInitialTrusterBonus() { return initialTrusterBonus; }
 	public void setInitialTrusterBonus(int initialTrusterBonus) {
 		this.initialTrusterBonus = initialTrusterBonus;
 	}
 	
 	public float getExchangeRate() { return exchangeRate; }
 	public void setExchangeRate(float exchangeRate) {
 		this.exchangeRate = exchangeRate;
 	}
 	
 	public int getBetrayalCost() {
 		return betrayalCost;
 	}
 	public void setBetrayalCost(int betrayalCost) {
 		this.betrayalCost = betrayalCost;
 	}
 	public int getMaxRoundsNum() {
 		return maxRoundsNum;
 	}
 	public void setMaxRoundsNum(int maxRoundsNum) {
 		this.maxRoundsNum = maxRoundsNum;
 	}
 	
 	
 }
