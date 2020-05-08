 package edu.gatech.cs2340.risky.model;
 
 import java.util.ArrayList;
 
 public class Game {
 
     public Lobby lobby;
     public int gameRound;
     
     private TurnManager turnManager = new TurnManager();
 
     public Game(Lobby lobby) {
         this.lobby = lobby;
     }
 
     public void addPlayersFromLobby() {
         ArrayList<Player> players = lobby.getPlayers();
         for (Player p : players) {
            turnManager.addPlayer(p); 
         }
     }
     
    public Game(int lobbyId) {
        this(new Lobby("lobbyId#" + lobbyId));
        //this(Lobby.getById(lobbyId));
    }
     public Lobby getLobby() {
         return lobby;
     }
 }
