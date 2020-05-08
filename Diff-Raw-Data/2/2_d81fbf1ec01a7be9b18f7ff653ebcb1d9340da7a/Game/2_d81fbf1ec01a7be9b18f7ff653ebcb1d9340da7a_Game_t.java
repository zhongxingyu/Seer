 package nu.danielsundberg.goodstuff.access.entity;
 
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "GAMES")
 @NamedQueries({
        @NamedQuery(name = "game.findByPlayerId", query = "SELECT g FROM Game AS g WHERE :player IN g.players")
 })
 public class Game {
 
 	@Id
 	@GeneratedValue(generator = "GAME_SEQUENCE")
     @SequenceGenerator(name = "GAME_SEQUENCE", sequenceName = "GAME_SEQUENCE")
     private long id;
 	
 	@Column(name="GAMEID", nullable = false)
 	private String gameId;
 	
 	@OneToMany(mappedBy="playerId")
 	private Set<Player> players;
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public void setPlayers(Set<Player> players) {
 		this.players = players;
 	}
 
 	public Set<Player> getPlayers() {
 		return players;
 	}
 
 	public void setGameId(String gameId) {
 		this.gameId = gameId;
 	}
 
 	public String getGameId() {
 		return gameId;
 	}
 	
 }
