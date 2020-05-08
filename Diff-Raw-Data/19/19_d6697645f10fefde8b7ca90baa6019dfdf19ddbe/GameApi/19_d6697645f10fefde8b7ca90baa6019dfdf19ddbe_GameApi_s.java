package edu.rit.se.sse.rapdevx.api
 
 /**
  * API access to the Game object on the server side
  *
  * Approximates the ActiveRecord pattern
  *
  * @author Ben Nichoals
  */
 public class GameApi {
 	public List<Game> listGames() {
 		return null;
 	}
 
 	public List<Asset> getAssets(Game inputGame, Session userSession) {
 		return null;
 	}
 
 	public GameStatus getStatus(Game inputGame) {
 		return null;
 	}
 
 	public boolean setShipPlacement(Game inputGame, Session userSession, List<ShipLocation> ships) {
 		return false;
 	}
 }
