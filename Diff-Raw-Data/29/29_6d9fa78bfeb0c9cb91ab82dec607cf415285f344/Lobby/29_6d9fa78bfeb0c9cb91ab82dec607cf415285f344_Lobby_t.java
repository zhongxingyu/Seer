 package edu.gatech.cs2340.risky.model;
 
 import java.util.ArrayList;
 
 public class Lobby {
     
     private String name;
     public ArrayList<Player> players = new ArrayList<Player>();
     
     public Lobby(String name) {
         this.name = name;
     }
 
     public ArrayList<Player> getPlayers() {
         return players; 
     }
 
     public String getName() {
         return name;
     }
     
    public int calculateArmies() {
        switch (players.size()) {
         case 3:
             return 35;
         case 4:
             return 30;
         case 5:
             return 25;
         case 6:
             return 20;
         }
         return 0;
     }
 
     public void allocateArmies() {
         for (Player player : this.players) {
            player.setArmy(this.calculateArmies());
         }
     } 
 }
