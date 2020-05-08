 package com.clinkworks.gameengine.components;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.clinkworks.gameengine.api.GameEngine;
 import com.clinkworks.gameengine.datatypes.GameID;
 import com.clinkworks.gameengine.datatypes.PlayerID;
 
 
 
 abstract public class GameBase {
 	private Map<PlayerID, Player> _players;
 	private GameID _gameID;
 	private GameEngine _gameEngine;
 	
 	public GameBase(GameID gameID){
 		_gameID = gameID;
 		_players = new HashMap<PlayerID, Player>();
 	}
 	
 	public GameBase(GameEngine gameEngine){
 		_gameEngine = gameEngine;
 		_players = new HashMap<PlayerID, Player>();
		_gameID = gameEngine.generateNextGameSequence();
 	}
 	
 	public GameEngine getGameEngine(){
 		return _gameEngine;
 	}
 	
 	public List<Player> getPlayers(){
 		return new ArrayList<Player>(_players.values());
 	}
 	
 	public Game addPlayerToGame(Player player){
 		_players.put(player.getPlayerID(), player);
 		return (Game)this;
 	}
 	
 	public Game saveGame(){
 		return getGameEngine().saveGame((Game)this);
 	}
 	
 	public GameID getGameID(){
 		return _gameID;
 	}
 	
 	
 	@Override public String toString(){
 		return _gameID.toString();
 	}
 }
