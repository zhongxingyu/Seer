 package com.socialblackjack.game.entities;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.ForeignKey;
 import org.hibernate.annotations.GenericGenerator;
 
 @Entity
 @Table(name="game_table_player")
 public class GameTablePlayer {
 
 	private Long id;
 	private GameTable gameTable;
 	private Player player;
 	private Date entryDate;
 	private Date exitDate;
 	
 	public GameTablePlayer(){
 		
 	}
 	@Id
 	@GeneratedValue(generator="increment")
 	@GenericGenerator(name="increment", strategy = "increment")
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	@ManyToOne
 	@JoinColumn(name="game_table_id")
 	public GameTable getGameTable() {
 		return gameTable;
 	}
 	public void setGameTable(GameTable gameTable) {
 		this.gameTable = gameTable;
 	}
 	@ManyToOne
 	@JoinColumn(name="user_id")
 	public Player getPlayer() {
 		return player;
 	}
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 	@Column(name="entry_date")
 	public Date getEntryDate() {
 		return entryDate;
 	}
 	public void setEntryDate(Date entryDate) {
 		this.entryDate = entryDate;
 	}
	@Column(name="exit_date")
 	public Date getExitDate() {
 		return exitDate;
 	}
 	public void setExitDate(Date exitDate) {
 		this.exitDate = exitDate;
 	}
 	
 	
 }
