 package com.github.joakimpersson.tda367.model.highscore;
 
 import java.io.Serializable;
 
 import com.github.joakimpersson.tda367.model.player.PlayerPoints;
 
 /**
 * An object that associate the players name with his PlayerPoints object.
  * 
  * @author joakimpersson
 * @modified adderollen
  * 
  */
 public class Score implements Serializable, Comparable<Score> {
 
 	/**
 	 * Auto generated serial
 	 */
 	private static final long serialVersionUID = -7241270636215740757L;
	private final String playerName;
	private final PlayerPoints playerPoints;
 
 	/**
 	 * Create a new score object using a player and his playerpoint object
 	 * 
 	 * @param playerName
 	 *            The name of the player
 	 * @param playerPoints
 	 *            The players playerpoint object
 	 */
 	public Score(String playerName, PlayerPoints playerPoints) {
 		this.playerName = playerName;
 		this.playerPoints = new PlayerPoints(playerPoints);
 	}
 
 	/**
 	 * Get the name of the player
 	 * 
 	 * @return The player's name
 	 */
 	public String getPlayerName() {
 		return playerName;
 	}
 
 	/**
 	 * Get a copy of the playerpoint object
 	 * 
 	 * @return A copy of the playerpoint object
 	 */
 	public PlayerPoints getPlayerPoints() {
 		return new PlayerPoints(playerPoints);
 	}
 
 	@Override
 	public int compareTo(Score other) {
 		return this.playerPoints.compareTo(other.playerPoints);
 	}
 
 	@Override
	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null || getClass() != obj.getClass()) {
 			return false;
 		}
 		Score other = (Score) obj;
 		return this.playerName.equals(other.playerName)
 				&& this.playerPoints.equals(other.playerPoints);
 	}
 
 	@Override
 	public int hashCode() {
 		int sum = 0;
 		sum += playerName.hashCode() * 5;
 		sum += playerPoints.hashCode() * 7;
 
 		return sum;
 	}
 }
