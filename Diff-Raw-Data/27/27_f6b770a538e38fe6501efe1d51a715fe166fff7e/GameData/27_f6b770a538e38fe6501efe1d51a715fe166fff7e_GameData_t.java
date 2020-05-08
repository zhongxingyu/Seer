 package edu.mines.alterego;
 
 
 /**
  * Description: This class is an abstraction layer to represent game data
  * @author Matt Buland, Maria Deslis, Eric Young
  *
  */
 
 
 class GameData {
     int gameId;
     String gameName;
     int hosting;
 
 	/**
 	 * 
 	 * @param gameId database id for the game
 	 * @param gameName name of the game
 	 */
     GameData(Integer gameId, String gameName, int hosting) {
         this.gameId = gameId;
         this.gameName = gameName;
         this.hosting = hosting;
     }
 
     String getGameName() {
         return gameName;
     }
 
     int getGameId() {
         return gameId;
     }
 
     boolean isHosting() {
         return (hosting == 1);
     }
 
     @Override
     public String toString() {
         return gameName + (hosting == 1 ? " (GM)" : "") ;
     }
 }
