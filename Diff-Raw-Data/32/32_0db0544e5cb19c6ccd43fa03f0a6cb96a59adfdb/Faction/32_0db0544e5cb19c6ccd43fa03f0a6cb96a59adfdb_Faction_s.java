 package Faction;
 
 import java.util.*;
 
 import HTNP.*;
 import World.*;
 
 //Todo: The following:
 //Think of stats for factions to have
 //Think of way to keep track of current task so replanning not a bitch
 public abstract class Faction {
 	public Stack<Task> plan = new Stack<Task>();
 	protected boolean isReadyToAttack = false;
 	protected int numArmies;
 	protected int numShips;
 	protected int numResources;
 	protected Faction enemy;
 	protected List<World> gateAddress = new ArrayList<World>();
 	protected List<World> knownWorldLocations = new ArrayList<World>();
 	protected World world;
 	public int timeToReplan = 0;
 	
 	public void setIsReadyToAttack(boolean isReady) {
 		this.isReadyToAttack = isReady;
 	}
 	public int getCombatStrength() {
 		return numArmies;
 	}
 	public int getNumArmies() {
 		return numArmies;
 	}
 	public void setNumArmies(int numArmies) {
 		this.numArmies = numArmies;
 	}
 	public int getNumShips() {
 		return numShips;
 	}
 	public void setNumShips(int numShips) {
 		this.numShips = numShips;
 	}
 	public int getNumResources() {
 		return numResources;
 	}
 	public void setNumResources(int numResources) {
 		this.numResources = numResources;
 	}
 	public Faction getEnemy() {
 		return enemy;
 	}
 	public void setEnemy(Faction enemy) {
 		this.enemy = enemy;
 	}
 	public boolean knowsGateAddress(World world) {
 		return gateAddress.contains(world);
 	}
 	
 	public String toString() {
 		return "CombatStrength = " + getCombatStrength() + ", NumArmies = " + numArmies + ", numShips = " + numShips + ", NumResources = " + numResources;
 	}
 	public boolean isReadyToAttack() {
 		return isReadyToAttack;
 	}
 	public World getWorld() {
 		return world;
 	}
 	public boolean hasGate() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	public boolean knowsLocation(World world) {
 		return true;
 		//return knownWorldLocations.contains(world);
 	}
 	public void setWorld(World world) {
 		this.world = world;	
 	}
 	
 	public void addKnownWorldLocation(World world) {
 		knownWorldLocations.add(world);
 	}
 	
 	public void replan() {
 		//Do things
 		//probably check all of the highest level tasks, find the one that matches flavor the most
 		//or one that gives an end result of victory condition
 		if(timeToReplan <= 0 || plan.isEmpty()) { 
 			plan.clear(); 
 			timeToReplan = 10;
 			Task attack = new AttackTask();
 			plan.add(attack);
 		}
 		while(plan.peek().isBaseTask() != true) {
 			if(plan.peek().isCompleted(this)) plan.pop();
			plan.add(plan.peek().getNextStep(this));
 		}
		System.out.println(plan);
 	}
 	
 	public boolean needToReplan() {
 		//Replan if your plan is empty, you need to replan, or you don't have a base task to perform
 		return plan.isEmpty() || timeToReplan <= 0 || plan.peek().isBaseTask() != true;
 	}
 	
 	public Task getNextPlannedTask() {
 		if(needToReplan())
 			replan();
 		timeToReplan--;
 		return plan.pop();
 	}
 	public void doTurn() {
 		getNextPlannedTask().perform(this);		
 	}
 	public boolean didWin() {
 		Task attack = new AttackTask();
 		return attack.isCompleted(this);
 	}
 }
