 package edu.gatech.cs2340.risk.model;
 
 /**
 * This class represents a player in Star Galaxy Risk
 */
 public class Player {
 	private String name, color;
 	private int totalFleets, numPlanets, numStarSystems;
 
 	/**
 	* Sets a Player's constructor to take in a Name, and a Color
 	* @param Name, Color: String's that set the Player's name and Color
 	*/
 
 	public Player(String name, String color, int totalFleets){
 		this.name = name;
 		this.color = color;
 		this.totalFleets = totalFleets;
 		numPlanets = 5; 
 		numStarSystems = 1;
 	}
 
 	public Player(String name, String color){
 		this.name = name;
 		this.color = color;
 		totalFleets = 35;
 		numPlanets = 5; 
 		numStarSystems = 1;
 	}
 	
	public void attack(Planet attack, Planet attackFrom){
		
		int attackerNumDice = 0;
		int attackReceiverNumDice = 0;
		
		if(isDifferentPlayer(attack.getOwner()) && attackFrom.getOwner().equals(this)){
			
			attackerNumDice = 
			
		}
		
	}
	
	private boolean isDifferentPlayer(Player attackReceiver){
		boolean isDifferent = true;
		if(attackReceiver.equals(this){
			isDifferent = false;
		}
		
		return isDifferent;
	}
	
 	/**
 	* Getter method for the number of fleets associated with the player
 	* @return the number of fleets this player has
 	*/
 
 	public int getFleets(){
 		return totalFleets;
 	}
 
 	/**
 	* Decrements the number of fleets a player has by 1
 	*/
 	public void removeFleet(){
 		totalFleets--;
 	}
 	/**
 	* This method set's the number of fleets a player has to the Integer it takes in as a parameter
 	* @param newtotalFleets: the new number of fleets the player will have
 	*/
 	public void setTotalFleets(int newTotalFleets){
 		totalFleets = newTotalFleets;
 	}
 
 	/**
 	* This method updates the total number of fleets to the player that he has earned before each turn
 	* @return number of new fleets added to the player's total beginning of each turn
 	*/
 
 	public int calcMoreFleets(){
 		int newFleetsAdded = 0;
 		if(numPlanets < 9 && numPlanets > 0){
 			newFleetsAdded = (3 + (numStarSystems * 3));
 			totalFleets += newFleetsAdded;
 		}else if(numPlanets >= 9){
 			newFleetsAdded = ((numPlanets/3) + (numStarSystems * 3));
 			totalFleets += newFleetsAdded;
 		}
 		return newFleetsAdded;
 	}
 
 	/**
 	* This method return the Player's current name
 	* @return String name of the player
 	*/
 	public String getName(){
 		return name;
 	}
 
 	/**
 	* This method return the Player's current color
 	* @return String color of the player
 	*/
 
 	public String getColor(){
 		return color;
 	}
 
 	/**
 	* This method get's the number of star system's owned by the player
 	* @return Number of Star Systems owned by the Player
 	*/
 
 	public int getNumStarSystems(){
 		return numStarSystems;
 	}
 
 	/**
 	* This method set's the number of star system's owned by the player
 	* @param New number of Star Systems owned by the Player
 	*/
 
 	public void setNumStarSystems(int newNumStarSystems){
 		numStarSystems = newNumStarSystems;
 	}
 
 	/**
 	* This method get's the number of planets's owned by the player
 	* @return New number of planets owned by the Player
 	*/
 
 	public int getNumPlanets(){
 		return numPlanets;
 	}
 
 	/**
 	* This method set's the number of planet's owned by the player
 	* @param New number of planets owned by the Player
 	*/
 
 	public void setNumPlanets(int newNumPlanets){
 		numPlanets = newNumPlanets;
 	}
 
 	public void setName(String newName) {
 		name = newName;
 	}
 
 
 
 
 
 
 }
