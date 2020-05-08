 package edu.gatech.cs2340.risk.model;
 
 import java.util.*;
 
 
 /**
  * Game initialization and processing
  */
 public class GameLogic
 {
     private int numPlayers, turnCount;
     private ArrayList<StarSystem> allSystems;
     private ArrayList<Player> players;
     private int newFleetsAdded;
     private int round;
     private ArrayList<Planet> planets;
     private String log;
 
     public GameLogic (ArrayList<Player> players) {
     
         this.players = players;
         numPlayers = players.size();
         allSystems = new ArrayList<StarSystem>();
         turnCount = 0;
        log = "No attacks have been made.";
         
         for(Player player: players){
             allSystems.add(new StarSystem(player));
         }
   
 	}
 
     public void update () {
         turnCount++;
         if (turnCount == players.size()) {
             turnCount = 0;
             round++;
         }
         int currentPlayer = turnCount;
         if(round != 0){
             newFleetsAdded = players.get(currentPlayer).calcMoreFleets();
         }
         log = "No attacks have been made.";
     }
 	
 	public boolean gameOver(){
 		
 		boolean gameOver = false;		
 		
 		StarSystem system = allSystems.get(0);
 		ArrayList<Planet> planetList = system.getPlanets();
 		Player player = planetList.get(0).getOwner();
 		
 		if(player.getNumPlanets() == allSystems.size() * planetList.size()){
 			gameOver = true;
 		}
 		
 		return gameOver;
 	}
 
     public void attackPlanet(Planet defender, Planet attacker, int attackFleetAmount) {
         //assume front end takes care of Panet's being able to attack or not
         int attackDie = getAttackerDie(attacker, attackFleetAmount);
         int defendDie = getDefenderDie(defender, attackDie);
         if (attackDie < defendDie)
             defendDie = attackDie;
 
         ArrayList<Integer> attackRolls = rollAndSort(attackDie);
         ArrayList<Integer> defendRolls = rollAndSort(defendDie);
 
         log = "";
         String appendage = "Attacker %s with a roll of %d against %d!";
 
         for (int i = 0; i < defendRolls.size(); i++) {
             if (defendRolls.get(i) >= attackRolls.get(i)) {
                 attacker.setFleets(attacker.getFleets()-1);
                 attacker.getOwner().setTotalFleets(attacker.getOwner().getFleets() - 1);
                 appendage = String.format("Attacker %s with a roll of %d against %d!",
                  "lost", attackRolls.get(i), defendRolls.get(i));
                 logResult(appendage);
             }
             else {
                 defender.setFleets(defender.getFleets()-1);
                 defender.getOwner().setTotalFleets(defender.getOwner().getFleets() - 1);
                 appendage = String.format("Attacker %s with a roll of %d against %d!",
                  "won", attackRolls.get(i), defendRolls.get(i));
                 logResult(appendage);
             }
         } 
 
         if (defender.getFleets() <= 0) {
             defender.getOwner().setNumPlanets(defender.getOwner().getNumPlanets() - 1);
             defender.setOwner(attacker.getOwner());
             attacker.getOwner().setNumPlanets(attacker.getOwner().getNumPlanets() + 1);
             defender.setFleets(attackDie);
             int currentFleets = attacker.getFleets();
             attacker.setFleets(currentFleets-attackDie);
             
             String transferNote = String.format("Attacker has liberated planet %s!", defender.getName());
             logResult(transferNote);
         }
 
     }
 
     private int getAttackerDie(Planet attacker, int attackFleetAmount){
         
         int attackDie = 2;
 
         if(attackFleetAmount <= 1){
             attackDie = 1;
         }
         else if(attackFleetAmount >= 3){
             attackDie = 3;
         }
 
         if(attackDie >= attacker.getFleets()){
             attackDie = attacker.getFleets() -1;
         }
         return attackDie;
     }
 
     private int getDefenderDie(Planet defender, int attackDie){
         int defendDie = 0;
 
         if(defender.getFleets() >= 3){
             defendDie = 2;
         }else{
             defendDie = 1;
         }
 
         return defendDie;
     }
 
     public int rollDice(){
         Random gen = new Random();
         int roll = gen.nextInt(6)+1;
         return roll;
     }
 
     private ArrayList<Integer> rollAndSort(int dice) {
         ArrayList<Integer> rolls = new ArrayList<Integer>();
         for (int i = 0; i < dice; i++)
             rolls.add(rollDice());
         Collections.sort(rolls);
         return rolls;
     }
 
     private void logResult(String appendage) {
         log += "\n" + appendage;
     }
 
     public String getLog() {
         return log;
     }
 
     public int getNewFleetsToBeAdded() {
         return newFleetsAdded;
     }
 
     public void decrementFleets() {
         newFleetsAdded--;
     }
 
     public int getTurn() {
         return turnCount;
     }
 	
 	public ArrayList<StarSystem> getAllSystems() {
 		return allSystems;
 	}
 
     public void removePlayer() {
         --numPlayers;
     }
 
     public int getRound() {
         return round;
     }
 
 }
