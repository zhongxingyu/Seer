 package com.jacad.footapp.domain;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
import org.apache.log4j.Logger;

import com.jacad.footapp.controller.TeamController;

 /**
  * The Class Team.
  */
 public class Team{
	
	static Logger logger = Logger.getLogger(Team.class);
 
 	/** The id. */
 	private Integer id;
 	
 	/** The name. */
 	private String name;
 	
 	/** The colors. */
 	private String colors;
 	
 	/** The stadium name. */
 	private String stadiumName;
 	
 	/** The creation year. */
 	private Integer creationYear;
 	
 	/** The players. */
 	private Collection<Player> players;
 
 	/**
 	 * Instantiates a new team.
 	 */
 	public Team() {
 		super();
		logger.info("Instanciates a new team");
 		this.players = new HashSet<>();
 	}
 
 	/**
 	 * Instantiates a new team.
 	 *
 	 * @param name the name
 	 * @param colors the colors
 	 * @param stadiumName the stadium name
 	 * @param creationYear the creation year
 	 * @param players the players
 	 */
 	public Team(String name, String colors, String stadiumName,
 			Integer creationYear, Set<Player> players) {
 		super();
		logger.info("Instanciates a new team with fields");
 		this.name = name;
 		this.colors = colors;
 		this.stadiumName = stadiumName;
 		this.creationYear = creationYear;
 		this.players = players;
 	}
 
 	/**
 	 * Gets the id.
 	 *
 	 * @return the id
 	 */
 	public Integer getId() {
 		return id;
 	}
 
 	/**
 	 * Sets the id.
 	 *
 	 * @param id the new id
 	 */
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	/**
 	 * Gets the name.
 	 *
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the name.
 	 *
 	 * @param name the new name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Gets the colors.
 	 *
 	 * @return the colors
 	 */
 	public String getColors() {
 		return colors;
 	}
 
 	/**
 	 * Sets the colors.
 	 *
 	 * @param colors the new colors
 	 */
 	public void setColors(String colors) {
 		this.colors = colors;
 	}
 
 	/**
 	 * Gets the stadium name.
 	 *
 	 * @return the stadium name
 	 */
 	public String getStadiumName() {
 		return stadiumName;
 	}
 
 	/**
 	 * Sets the stadium name.
 	 *
 	 * @param stadiumName the new stadium name
 	 */
 	public void setStadiumName(String stadiumName) {
 		this.stadiumName = stadiumName;
 	}
 
 	/**
 	 * Gets the creation year.
 	 *
 	 * @return the creation year
 	 */
 	public Integer getCreationYear() {
 		return creationYear;
 	}
 
 	/**
 	 * Sets the creation year.
 	 *
 	 * @param creationYear the new creation year
 	 */
 	public void setCreationYear(Integer creationYear) {
 		this.creationYear = creationYear;
 	}
 
 	/**
 	 * Gets the players.
 	 *
 	 * @return the players
 	 */
 	public Collection<Player> getPlayers() {
		logger.info("Return players");
		
		for(Player player : players){
			logger.info(player.toString());
		}
		
 		return players;
 	}
 
 	/**
 	 * Sets the players.
 	 *
 	 * @param collection the new players
 	 */
 	public void setPlayers(Collection<Player> collection) {
 		this.players = collection;
 	}
 	
 	/**
 	 * Adds the player.
 	 *
 	 * @param player the player
 	 */
 	public void addPlayer(Player player) {
 		this.players.add(player);
 	}
 	
 	/**
 	 * Removes the player.
 	 *
 	 * @param player the player
 	 */
 	public void removePlayer(Player player) {
 		this.players.remove(player);
 	}
 
 }
