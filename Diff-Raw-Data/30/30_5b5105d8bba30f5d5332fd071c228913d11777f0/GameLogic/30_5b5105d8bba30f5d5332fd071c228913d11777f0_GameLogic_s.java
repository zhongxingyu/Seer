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
     
     public GameLogic (ArrayList<Player> players) {
     
         this.players = players;
         numPlayers = players.size();
         allSystems = new ArrayList<StarSystem>();
         turnCount = 0;
         
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
     }
 
     public void attackPlanet(Planet defender, Planet attacker, int attackFleetAmount) {
         //assume front end takes care of Panet's being able to attack or not
         int attackDie = getAttackerDie(attacker, attackFleetAmount);
         int defendDie = getDefenderDie(defender, attackDie);
         if (attackDie < defendDie)
             defendDie = attackDie;
 
         ArrayList<Integer> attackRolls = rollAndSort(attackDie);
         ArrayList<Integer> defendRolls = rollAndSort(defendDie);
 
         for (int i = 0; i < defendRolls.size(); i++) {
            if (defendRolls.get(i) >= attackRolls.get(i)) 
                 attacker.setFleets(attacker.getFleets()-1);
            else 
                 defender.setFleets(defender.getFleets()-1);
         } 
 
         if (defender.getFleets() <= 0) {
             defender.setOwner(attacker.getOwner());
             defender.setFleets(attackDie);
             int currentFleets = attacker.getFleets();
             attacker.setFleets(currentFleets-attackDie);
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
