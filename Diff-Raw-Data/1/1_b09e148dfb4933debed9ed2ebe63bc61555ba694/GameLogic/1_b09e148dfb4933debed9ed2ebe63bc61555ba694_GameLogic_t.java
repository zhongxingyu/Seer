 package edu.gatech.cs2340.risk.model;
 
 import java.util.*;
 
 /**
  * Game initialization and processing
  */
 public class GameLogic
 {
     private int numPlayers;
     private ArrayList<StarSystem> allSystems;
     private ArrayList<Player> players;
     
     public GameLogic (ArrayList<Player> players) {
         this.players = players;
         numPlayers = players.size();
        allSystems = new ArrayList<StarSystem>();
         
 		for(Player player: players){
             allSystems.add(new StarSystem(player));
         }
 	}
 	
 	public ArrayList<StarSystem> getAllSystems() {
 		return allSystems;
 	}
 }
