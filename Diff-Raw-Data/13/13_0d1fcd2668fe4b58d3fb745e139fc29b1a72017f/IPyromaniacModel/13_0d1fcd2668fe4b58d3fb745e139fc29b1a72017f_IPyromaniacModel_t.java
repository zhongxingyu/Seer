 package com.github.joakimpersson.tda367.model;
 
 import java.beans.PropertyChangeListener;
 import java.util.List;
 
 import com.github.joakimpersson.tda367.model.constants.Attribute;
 import com.github.joakimpersson.tda367.model.constants.PlayerAction;
 import com.github.joakimpersson.tda367.model.highscore.Score;
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 
 /**
  * An interface modeling the bomberman model that the main controller will use
  * to communicate with the rest of the model
  * 
  * @author joakimpersson
 * @modified Viktor Anderling, Andreas Roln
  * 
  */
 public interface IPyromaniacModel {
 
 	/**
 	 * Starts a new game of BombermanModel with the supplied players
 	 * 
 	 * @param players
 	 *            The players to participate in the game
 	 */
 	public void startGame(List<Player> players);
 
 	/**
 	 * Responsible for updating the model with player movements and actions
 	 * 
 	 * @param player
 	 *            The player responsible for the action
 	 * @param action
 	 *            The action performed by the current player
 	 */
 	public void updateGame(Player player, PlayerAction action);
 
 	/**
 	 * 
 	 * Upgrades a players attribute
 	 * 
 	 * @param player
 	 *            The player to receive an attribute upgrade
	 * @param attribute
 	 *            The attribute to be upgraded or added
 	 */
	public void upgradePlayer(Player player, Attribute attribute);
 
 	/**
 	 * Returns a copy of the list of the current active players in the game
 	 * 
 	 * @return A copy of the list of players
 	 */
 	public List<Player> getPlayers();
 
 	/**
 	 * Returns a copy of the matrix representing the game map
 	 * 
 	 * @return A copy of the game map matrix
 	 */
 	public Tile[][] getMap();
 
 	/**
 	 * 
 	 * Returns true or false depending if the game is over or not
 	 * 
 	 * @return True if the game is over and false otherwise
 	 */
 	public boolean isGameOver();
 
 	/**
 	 * 
 	 * Returns true or false depending if the match is over or not
 	 * 
 	 * @return True if the match is over and false otherwise
 	 */
 	public boolean isMatchOver();
 
 	/**
 	 * 
 	 * Returns true or false depending if the round is over or not
 	 * 
 	 * @return True if the round is over and false otherwise
 	 */
 	public boolean isRoundOver();
 
 	/**
 	 * Handling the actions for when the round is over and makes sure that the
 	 * game is properly updated and setups the next round
 	 */
 	public void roundOver();
 
 	/**
 	 * Handling the actions for when the match is over and makes sure that the
 	 * game is properly updated and setups the next match
 	 */
 	public void matchOver();
 
 	/**
 	 * Handling the actions for when the game is over and makes sure that the
 	 * game is properly updated and also adds the players to the Highscore list
 	 */
 	public void gameOver();
 
	/**
 	 * Reset all the stats that has been upgraded during the past match
 	 */
 	public void resetRoundStats();
 
 	/**
 	 * Reset the model after an ended game
 	 */
 	public void gameReset();
 
 	/**
 	 * Get the current Highscore list
 	 * 
 	 * @return A List containing the games top players
 	 */
 	public List<Score> getHighscoreList();
 
 	/**
 	 * Reset and erase all the previous Highscore:s from the list
 	 */
 	public void resetHighscoreList();
 
 	/**
 	 * Get a list of all the players stats as score objects, when the game is
 	 * over
 	 * 
 	 * @return A list of all the players as score objects
 	 */
 	public List<Score> getGameOverSummary();
 
	// TODO Natan, add javadoc.
 	public void addPropertyChangeListener(PropertyChangeListener pcl);
 
 	/**
 	 * A method that gets the last round winner
 	 * 
 	 * @return The player who won the last played round
 	 */
 	public Player getLastRoundWinner();
 
 }
