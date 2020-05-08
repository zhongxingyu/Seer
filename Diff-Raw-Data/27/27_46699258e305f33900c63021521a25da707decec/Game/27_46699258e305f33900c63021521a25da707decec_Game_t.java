 package com.raibaz.lupus.game;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Embedded;
 import javax.persistence.Id;
 
 import com.googlecode.objectify.annotation.Indexed;
 import com.raibaz.lupus.dao.LupusDAO;
 
 public class Game extends AbsJsonable {
 	
 	@Id
 	private String id;
 	@Embedded @Indexed
 	private List<Player> players = new ArrayList<Player>();
	@Indexed
 	private GameState state;
 	private String name;
 	@Embedded
 	private GameConfiguration configuration;
 	@Embedded @Indexed
 	private Player owner;
 	
 	@Embedded @Indexed
 	private Player lastDead;	
 	
 	public Game() {
 		state = GameState.WAITING;
 	}
 	
 	public Player getPlayer(String id) {
 		LupusDAO dao = new LupusDAO();
 		for(Player p : players) {
 			if(p.getFbId().equals(id)) {				
 				return dao.ofy().get(Player.class, p.getFbId());
 			}
 		}
 		return null;
 	}
 	
 	public void initConfiguration() {
 		configuration = GameConfiguration.getDefaultConfiguration(players.size());
 	}
 	
 	
 	public GameConfiguration getConfiguration() {
 		if(configuration == null) {
 			initConfiguration();
 		}
 		return configuration;
 	}
 	
 	public void refreshPlayers() {
 		ArrayList<Player> newPlayers = new ArrayList<Player>();
 		LupusDAO dao = new LupusDAO();
 		for(Player p : players) {
 			try  {
 				Player stored = dao.ofy().get(Player.class, p.getFbId());
 				newPlayers.add(stored);
 			} catch (Exception e) {
 				newPlayers.add(p);
 			}
 		}
 		players = newPlayers;
 	}
 
 	public String getId() {
 		return id;
 	}
 	public void setId(String id) {
 		this.id = id;
 	}
 	
 	public List<Player> getPlayers() {
 		return getPlayers(false);
 	}
 	
 	public List<Player> getPlayers(boolean load) {
 		if(!load) {
 			return players;
 		}
 		ArrayList<Player> ret = new ArrayList<Player>();
 		LupusDAO dao = new LupusDAO();
 		for(Player p : players) {
 			try {
 				Player stored = dao.ofy().get(Player.class, p.getFbId());
 				if(stored != null) {
 					ret.add(stored);
 				} 
 			}
 			catch (Exception e) {
 				ret.add(p);
 				dao.ofy().put(p);
 			}
 		}
 		return ret;
 	}
 	
 	public Player getPlayerByRole(PlayerRole role) {
 		for(Player p : getPlayers(false)) {
 			if(p.getRole() == role) {
 				return p;
 			}
 		}
 		return null;
 	}
 	
 	public void setPlayers(List<Player> players) {
 		this.players = players;
 	}
 	public GameState getState() {
 		return state;
 	}
 	public void setState(GameState state) {
 		this.state = state;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public Player getOwner() {
 		return owner;
 	}
 	public void setOwner(Player owner) {
 		this.owner = owner;
 	}	
 	public Player getLastDead() {
 		return lastDead;
 	}
 	public void setLastDead(Player lastDead) {
 		this.lastDead = lastDead;
 	}	
 }
