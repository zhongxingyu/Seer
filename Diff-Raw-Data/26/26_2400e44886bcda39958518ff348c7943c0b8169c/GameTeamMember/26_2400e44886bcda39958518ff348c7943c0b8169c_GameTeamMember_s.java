 package se.davvs.floorballfighters.models;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="game_team_member", catalog = "floorballfighters")
 public class GameTeamMember {
 
 	private Integer id;
 	private Game game;
 
 	private Player player;
 	private Integer team;
 	
 	@Id
 	@GeneratedValue(strategy = IDENTITY)
 	@Column(name = "id", unique = true, nullable = false)
 	public Integer getId() {
 		return id;
 	}
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	
 	@Column(name="team")
 	public Integer getTeam() {
 		return team;
 	}
 	public void setTeam(Integer team) {
 		this.team = team;
 	}
 	
 	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
 	@JoinColumn(name = "gid", updatable = true)
 	public Game getGame() {
 		return game;
 	}
 	public void setGame(Game game) {
 		this.game = game;
 	}
 	
 	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
 	@JoinColumn(name = "pid", updatable = true)
 	public Player getPlayer() {
 		return player;
 	}
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 }
