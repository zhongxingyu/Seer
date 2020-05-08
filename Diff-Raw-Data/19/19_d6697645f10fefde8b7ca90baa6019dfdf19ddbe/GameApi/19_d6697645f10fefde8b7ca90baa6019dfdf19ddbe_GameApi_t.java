package edu.rit.se.sse.rapdevx.api;

import java.util.List;
import edu.rit.se.sse.rapdevx.api.dataclasses.Game;
import edu.rit.se.sse.rapdevx.api.dataclasses.Asset;
import edu.rit.se.sse.rapdevx.api.dataclasses.Session;
import edu.rit.se.sse.rapdevx.api.dataclasses.GameStatus;
import edu.rit.se.sse.rapdevx.api.dataclasses.ShipLocation;
 
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
