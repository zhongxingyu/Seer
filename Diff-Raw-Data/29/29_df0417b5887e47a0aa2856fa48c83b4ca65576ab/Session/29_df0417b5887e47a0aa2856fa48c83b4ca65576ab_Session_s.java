 package game;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Comparator;
 
 import ui.render.Render;
 
 public interface Session 
 {
 	/**
 	 * Create a number of players and return a list of their ids
 	 * @param n The number of players to create
 	 * @return The list of player ids
 	 */
 	public ArrayList<String> createPlayers(int n);
 
 	/**
 	 * Get a list of the current player ids
 	 * @return The list of player ids
 	 */
 	public ArrayList<String> getPlayerIds();
 
 	/**
 	 * Get the id of the current player
	 * @param The id of the current player
 	 */
 	public String getCurrentPlayerId();
 
 	
 	/**
 	 * Set the current player by passing in a id
 	 * @param id The player id
 	 */
 	public void setCurrentPlayer(String id);
 
 	/**
 	 * Advance the current player to the next player in line
 	 * @return Whether we reached the end of the player list
 	 */
 	public boolean advancePlayer();
 
 	/**
 	 * Update a player based on their id
 	 * @param id The player id of the player we want to update
 	 */
 	public void updatePlayer(String id);
 	
 	/**
 	 * Sell a resource as a player
 	 * @param id The id of the player
 	 * @param resource The resource to sell
 	 * @param quantity The number of resources to sell
 	 * @param price The price to sell
 	 */
 	public void playerSellResource(String id, String resource, int quantity, int price);
 
 	/**
 	 * Buy a resource as a player
 	 * @param id The id of the player
 	 * @param resource The resource to buy
 	 * @param quantity The number of resources to buy
 	 * @param price The price to buy
 	 */
 	public void playerBuyResource(String id, String resource, int quantity, int price);
 
 	/**
 	 * Remove the follower of a player
 	 * @param id The player id
 	 */
 	public void removePlayerFollower(String id);
 
 	/**
 	 * Set the follower of a player
 	 * @param id The player id
 	 * @param follower The follower
 	 */
 	public void setPlayerFollower(String id, Follower follower);
 
 	/**
 	 * Get the follower of a player
 	 * @param id The player id
 	 * @return The follower of that player. If the player 
 	 * doesn't have a follower, null if returned
 	 */
 	public Follower getPlayerFollower(String id);
 	
 	/**
 	 * Sort the list of players using a comparator
 	 * @param comp The comparator
 	 */
 	public void sortPlayers(Comparator<Player> comp);
 
 	/**
 	 * Apply a force to a player (moving the player).
 	 * @param id The player id
 	 * @param fx The x force
 	 * @param fy The y force
 	 */
 	public void applyForceToPlayer(String id, int fx, int fy);
 
 	/**
 	 * Get the ids of all player owned plots
 	 * @param id The player id
 	 * @return The list of plot ids the player owns
 	 */
 	public ArrayList<String> getPlayerOwnedPlotIds(String id);
 
 	/**
 	 * Whether a plot is owned by a specific player
 	 * @param id The player id
 	 * @param plotId The id of the plot
 	 * @return Whether the player owns the plot
 	 */
 	public boolean isPlotOwnedByPlayer(String id, String plotId);
 
 	/**
 	 * Give a plot to a player
 	 * @param id The player id
 	 * @param plotId The plot id
 	 */
 	public void addPlotToPlayer(String id, String plotId);
 
 	/**
 	 * Get the plot at a specific x/y coordinate
 	 * @param x The x position
 	 * @param y The y position
 	 * @return The plot
 	 */
 	public Plot getPlot(int x, int y);
 
 	/**
 	 * Get a plot by its id
 	 * @param id The plot id
 	 * @return The plot
 	 */
 	public Plot getPlot(String id);
 
 	/**
 	 * Get a player's follower's render
 	 * @param id The player id
 	 * @return The render of the follower. If the player 
 	 * doesn't have a follower, null if returned
 	 */
 	public Render getPlayerFollowerRender(String id);
 
 	/**
 	 * Get a player's render
 	 * @param id The player id
 	 * @return The render of the player
 	 */
 	public Render getPlayerRender(String id);
 
 	/**
 	 * Get the money of a particular player
 	 * @param id The player id
 	 * @return The amount of money the player has
 	 */
 	public int getPlayerMoney(String id);
 
 	/**
 	 * Set the money of a particular player
 	 * @param id The player id
 	 * @param amount The money id
 	 */
 	public void setPlayerMoney(String id, int amount);
 
 	/**
 	 * Increment a player's ore by a certain amount
 	 * @param id The player id
 	 * @param amount The amount to give the player
 	 */
 	public void incrementOre(String id, int amount);
 	
 	/**
 	 * Increment a player's food by a certain amount
 	 * @param id The player id
 	 * @param amount The amount to give the player
 	 */
 	public void incrementFood(String id, int amount);
 
 	/**
 	 * Increment a player's energy by a certain amount
 	 * @param id The player id
 	 * @param amount The amount to give the player
 	 */
 	public void incrementEnergy(String id, int amount);
 
 
 	/**
 	 * Increment a player's money by a certain amount
 	 * @param id The player id
 	 * @param amount The amount to give the player
 	 */
 	public void incrementMoney(String id, int amount);
 	
 	/**
 	 * Increment a player's crystite by a certain amount
 	 * @param id The player id
 	 * @param amount The amount to give the player
 	 */
 	public void incrementCrystite(String id, int amount);
 
 	/**
 	 * Get a player's Color
 	 * @param id The player id
 	 * @return The player's Color
 	 */
 	public Color getPlayerColor(String id);
 
     /**
      * Set a player's color
      * @param id The player id
      * @param color The player's color
      */
 	public void setPlayerColor(String id, Color color);
 
 	/**
 	 * Get a player's name
 	 * @param id The player id
 	 * @return The player's name
 	 */
 	public String getPlayerName(String id);
 
     /**
      * Set a player's name
      * @param id The player id
     * @param color The player's name
      */
 	public void setPlayerName(String id, String name);
 
 	/**
 	 * Get a player's type
 	 * @param id The player id
 	 * @return The player's type
 	 */
 	public PlayerType getPlayerType(String id);
 
     /**
      * Set a player's type
      * @param id The player id
     * @param color The player's type
      */
 	public void setPlayerType(String id, PlayerType type);
 
 	/**
 	 * Get a player's ore
 	 * @param id The player id
 	 * @return The player's ore
 	 */
 	public int getPlayerOre(String id);
 
 	/**
 	 * Get a player's food
 	 * @param id The player id
 	 * @return The player's food
 	 */
 	public int getPlayerFood(String id);
 
 	/**
 	 * Get a player's energy
 	 * @param id The player id
 	 * @return The player's energy
 	 */
 	public int getPlayerEnergy(String id);
 	
 	/**
 	 * Get a player's crystite
 	 * @param id The player id
 	 * @return The player's crystite
 	 */
 	public int getPlayerCrystite(String id);
 
 	/**
 	 * Get a player's x position
 	 * @param id The player id
 	 * @return The player's x position
 	 */
 	public int getPlayerX(String id);
 
     /**
      * Set a player's x position
      * @param id The player id
     * @param color The x position
      */
 	public void setPlayerX(String id, int x);
 
 	/**
 	 * Get a player's y position
 	 * @param id The player id
 	 * @return The player's y position
 	 */
 	public int getPlayerY(String id);
 
     /**
      * Set a player's color
      * @param id The player id
     * @param color The y position
      */
 	public void setPlayerY(String id, int y);
 
 	/**
 	 * Get the current round at
 	 * @return The current round
 	 */
 	public int getCurrentRound();
 
 	/**
 	 * Increment the current round. The game should save
 	 * when this is called
 	 */
 	public void incrementRound();
 
 	/**
 	 * Set the game's map
 	 * @param map The map
 	 */
 
 	public void setMap(Map map);
 
 	/**
 	 * Set the timer. The timer is a general purpose timer
 	 * @param n The time
 	 */
 	public void setTimer(int n);
 
 	/**
 	 * Get the timer's value
 	 * @return the timer's value
 	 */
 	public int getTimer();
 
 	/**
 	 * Increment the timer's value
 	 */
 	public void incrementTimer();
 
 	/**
 	 * Decrement the timer's value
 	 */
 	public void decrementTimer();
 
 	/**
 	 * Get the saved session
 	 * @return The saved session
 	 */
 	public Session getSaveCopy();
 
 	/**
 	 * Force the session to save a copy of itself
 	 */
 	public void forceSave();
 
     /**
      * Get the session's id
      * @return The session's id
      */
     public String getId();
 }
