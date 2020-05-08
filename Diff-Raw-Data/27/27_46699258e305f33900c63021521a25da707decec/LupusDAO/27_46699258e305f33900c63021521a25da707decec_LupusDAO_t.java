 package com.raibaz.lupus.dao;
 
 import java.util.List;
 
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.util.DAOBase;
 import com.raibaz.lupus.game.Game;
 import com.raibaz.lupus.game.GameState;
 import com.raibaz.lupus.game.Player;
 
 public class LupusDAO extends DAOBase {
 
 	static {
 		ObjectifyService.register(Player.class);
 		ObjectifyService.register(Game.class);
 	}
 	
 	public Player getPlayer(String fbId) {
 		return ofy().find(Player.class, fbId);
 	}
 	
 	public List<Game> listWaitingGames() {
 		return ofy().query(Game.class).filter("state", GameState.WAITING).list();
 	}
 	
 	public Game getOwnedGame(Player p) {
 		List<Game> games = ofy().query(Game.class).filter("owner.fbId", p.getFbId()).list();
 		if(!games.isEmpty()) {
			for(Game g : games) {
				if(g.getState() != GameState.ARCHIVED) {
					return g;
				}
			}
			return null;
 		} else {
 			return null;
 		}
 	}
 	
 }
