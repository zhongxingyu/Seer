 package jzi.model;
 
 import java.awt.Color;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Random;
 
 import jzi.controller.state.IState;
 import jzi.controller.state.TileState;
 import jzi.model.map.Coordinates;
 import jzi.model.map.Direction;
 import jzi.model.map.ICoordinates;
 import jzi.model.map.IField;
 import jzi.model.map.IMap;
 import jzi.model.map.ITile;
 import jzi.model.map.ITileType;
 import jzi.model.map.Map;
 import jzi.model.map.Tile;
 import jzi.view.IWindow;
 import jzi.view.LoseMenu;
 import jzi.view.Update;
 import jzi.view.Window;
 import jzi.view.WinnerMenu;
 
 /**
  * Manages the Game state.
  * 
  * @author Buddy Jonte
  */
 public class Game extends Observable implements IGame {
 	/**
 	 * Maximum allowed length for player names.
 	 */
 	private static final int MAX_NAME_LENGTH = 15;
 	/**
 	 * Minimum allowed length for player names.
 	 */
 	private static final int MIN_NAME_LENGTH = 3;
 	/**
 	 * Minimum number of players needed for a game.
 	 */
 	private static final int MIN_PLAYERS = 2;
 	/**
 	 * Maximum number of players.
 	 */
 	public static final int MAX_PLAYERS = 6;
 	/**
 	 * Cost of placing a zombie in points.
 	 */
 	public static final int ZOMBIE_PLACE_COST = 3;
 	/**
 	 * Maximum number of lives a player may have.
 	 */
 	public static final int MAX_LIVES = 5;
 	/**
 	 * Maximum result of a die roll.
 	 */
 	public static final int MAX_ROLL = 6;
 	/**
 	 * UID for serialization.
 	 */
 	private static final long serialVersionUID = 4613634511731016990L;
 	/**
 	 * The threshold of points considered a win.
 	 */
 	private int winThreshold;
 	/**
 	 * Determines whether the game zombifies dead players.
 	 */
 	private boolean zombification;
 	/**
 	 * Determines whether players get an additional point when rolling.
 	 */
 	private boolean additionalPoint;
 	/**
 	 * Determines whether the number of revives is limited.
 	 */
 	private boolean hardcore;
 	/**
 	 * Maximum number of revives a Player may get.
 	 */
 	private int revives;
 	/**
 	 * The current {@link Window}.
 	 */
 	private transient IWindow window;
 	/**
 	 * This game's map.
 	 */
 	private IMap map;
 	/**
 	 * This Game's die.
 	 */
 	private IDie die;
 	/**
 	 * List of players.
 	 */
 	private LinkedList<IPlayer> players;
 	/**
 	 * List of Zombies on the map.
 	 */
 	private LinkedList<IZombie> zombies;
 	/**
 	 * Index of the current player.
 	 */
 	private int currentPlayerIndex;
 	/**
 	 * Player who one the game.
 	 */
 	private IPlayer winner;
 
 	/**
 	 * Current game state.
 	 */
 	private IState currentState;
 	/**
 	 * Current Tile that was drawn but not placed.
 	 */
 	private ITile currentTile;
 	/**
 	 * Currently selected Zombie.
 	 */
 	private IZombie currentZombie;
 	/**
 	 * List of tiles waiting to be drawn.
 	 */
 	private List<ITileType> tileList;
 	/**
 	 * Player ammunition at the beginning of the game.
 	 */
 	private int startAmmo;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param window
 	 *            current game window
 	 * @param shuffledTileList
 	 *            shuffled list of tiles to be used in the game
 	 */
 	public Game(final List<ITileType> shuffledTileList) {
 		this.tileList = shuffledTileList;
 
 		players = new LinkedList<>();
 		zombies = new LinkedList<>();
 		map = new Map();
 		die = new Die();
 	}
 
 	/**
 	 * Set this game's window.
 	 * 
 	 * @param window
 	 *            - current {@link Window}.
 	 */
 	@Override
 	public final void setWindow(final IWindow window) {
 		this.window = window;
 	}
 
 	/**
 	 * Set the game's starting ammunition.
 	 * 
 	 * @param startAmmo
 	 *            new starting ammunition
 	 */
 	public void setStartAmmo(int startAmmo) {
 		this.startAmmo = startAmmo;
 	}
 
 	/**
 	 * Gets the game's starting ammunition.
 	 * 
 	 * @return starting ammunition
 	 */
 	@Override
 	public int getStartAmmo() {
 		return startAmmo;
 	}
 
 	/**
 	 * Resets game parameters so a new game can be played.
 	 */
 	public void setUp() {
 		additionalPoint = false;
 		winner = null;
 
 		currentTile = tileList.remove(0).createTile();
 
 		map.addTile(new Coordinates(0, 0), currentTile);
 
 		setChanged();
 		notifyObservers(Update.TilePlaced);
 
 		currentTile = null;
 		currentPlayerIndex = new Random().nextInt(players.size());
 		setChanged();
 		notifyObservers(Update.PlayerMoved);
 	}
 
 	/**
 	 * Get the current value of the game's die.
 	 * 
 	 * @return current die value
 	 */
 	@Override
 	public final int getDie() {
 		return die.getValue();
 	}
 
 	/**
 	 * Set the current value of the game's Die.
 	 * 
 	 * @param val
 	 *            - new value
 	 */
 	@Override
 	public final void setDie(final int val) {
 		die.setValue(val);
 
 		setChanged();
 		notifyObservers(Update.DieRolled);
 	}
 
 	/**
 	 * Setter for this game's die object(needed for junit test only).
 	 * 
 	 * @param die
 	 *            new die object
 	 */
 	@Override
 	public final void setDieObject(final IDie die) {
 		this.die = die;
 	}
 
 	/**
 	 * Draws a Tile from the list of Tiles, sets it up and checks whether it can
 	 * be placed.
 	 * 
 	 * @return true if the Tile can be placed.
 	 */
 	@Override
 	public final boolean drawTile() {
 		if (tileList.size() > 0) {
 			currentTile = tileList.remove(0).createTile();
 			currentTile.setUp();
 		} else {
 			currentTile = null;
 		}
 
 		setChanged();
 		notifyObservers(Update.TileDrawn);
 
 		if (map.checkTile(currentTile)) {
 			return true;
 		}
 
 		if (currentTile.getTileType().getName().equals("Helicopter")) {
 			additionalPoint = true;
 		}
 
 		currentTile = null;
 
 		return false;
 	}
 
 	/**
 	 * Get the game's current Tile.
 	 * 
 	 * @return current Tile
 	 */
 	@Override
 	public final ITile getCurrentTile() {
 		return currentTile;
 	}
 
 	/**
 	 * Set this game's current tile.
 	 * 
 	 * @param currentTile
 	 *            new current tile.
 	 */
 	@Override
 	public final void setCurrentTile(final ITile currentTile) {
 		this.currentTile = currentTile;
 	}
 
 	/**
 	 * Add a player to the game.
 	 * 
 	 * @param player
 	 *            - {@link IPlayer} to be added.
 	 */
 	@Override
 	public final void addPlayer(final IPlayer player) {
 		players.add(player);
 
 		setChanged();
 		notifyObservers(Update.PlayerAdded);
 	}
 
 	/**
 	 * Get the game's list of players.
 	 * 
 	 * @return list of players
 	 */
 	@Override
 	public final LinkedList<IPlayer> getPlayers() {
 		return players;
 	}
 
 	/**
 	 * Advance the game round to the next player. Resets all players' roll flags
 	 * and all zombies' steps.
 	 * 
 	 * @return {@link IPlayer} who is the new current player
 	 */
 	@Override
 	public final IPlayer nextPlayer() {
 		currentPlayerIndex++;
 		currentPlayerIndex %= players.size();
 
 		// Reset roll flag
 		for (IPlayer player : players) {
 			player.setRolledPlayer(false);
 			player.setRolledZombie(false);
 		}
 
 		for (IZombie zombie : zombies) {
 			zombie.resetSteps();
 		}
 
 		if (players.isEmpty()) {
 			return null;
 		}
 
 		setChanged();
 		notifyObservers(Update.PlayerChange);
 
 		return players.get(currentPlayerIndex);
 
 	}
 
 	/**
 	 * Get the game's current player.
 	 * 
 	 * @return current {@link IPlayer}
 	 */
 	@Override
 	public final IPlayer getCurrentPlayer() {
 		if (players.isEmpty()) {
 			return null;
 		}
 
 		return players.get(currentPlayerIndex);
 	}
 
 	/**
 	 * Get this game's map.
 	 * 
 	 * @return game's {@link IMap}
 	 */
 	@Override
 	public final IMap getMap() {
 		return map;
 	}
 
 	/**
 	 * Set this game's map(only needed by junit test).
 	 * 
 	 * @param map
 	 *            map object
 	 */
 	@Override
 	public final void setMap(final IMap map) {
 		this.map = map;
 	}
 
 	/**
 	 * Set this game's state.
 	 * 
 	 * @param state
 	 *            - new {@link IState}
 	 */
 	@Override
 	public void setState(final IState state) {
 		if (currentState != null) {
 			currentState.exit();
 		}
 
 		if (checkWin()) {
 			window.setMenu(new WinnerMenu(window, this));
 			return;
 		}
 
 		currentState = state;
 		currentState.setGame(this);
 		currentState.enter();
 
 		setChanged();
 		notifyObservers(Update.StateChanged);
 	}
 
 	/**
 	 * Get the game's current state.
 	 * 
 	 * @return current {@link IState}
 	 */
 	@Override
 	public final IState getCurrentState() {
 		return currentState;
 	}
 
 	/**
 	 * Tries to move the player to the specified {@link IField}.
 	 * 
 	 * @param to
 	 *            - {@link IField} to move current player to
 	 * @return true if successful
 	 */
 	@Override
 	public final boolean movePlayer(final IField to) {
 		int steps = getCurrentPlayer().getSteps();
 		ICoordinates coord = getCurrentPlayer().getCoordinates();
 		IField from = map.getField(coord);
 
 		// Check whether the clicked field is a neighbor
 		if (!map.checkNeighbor(from, to)) {
 			return false;
 		}
 
 		// Make sure the player has enough steps left
 		if (steps < 1) {
 			return false;
 		}
 
 		getCurrentPlayer().setCoordinates(to.getCoordinates());
 		getCurrentPlayer().setSteps(steps - 1);
 
 		setChanged();
 		notifyObservers(Update.PlayerMoved);
 
 		return true;
 	}
 
 	/**
 	 * Tries to move a zombie to a {@link IField}.
 	 * 
 	 * @param to
 	 *            Field to move to
 	 * @return true if successful
 	 */
 	public final boolean moveZombie(final IField to) {
 		IZombie zombie = currentZombie;
 		int moves = getCurrentPlayer().getZombies();
 		IField from = map.getField(zombie.getCoordinates());
 
 		// no zombie to move, field occupied or clicked Field is not a neighbor
 		if (from == null || zombie == null || to.getZombie() != null
 				|| !map.checkNeighbor(from, to)) {
 			return false;
 		}
 
 		// Make sure the player can move zombies and the zombie has steps left
 		if (moves < 1 || zombie.getSteps() < 1) {
 			return false;
 		}
 
 		// update Fields
 		to.setZombie(zombie);
 		from.setZombie(null);
 
 		// update Zombie
 		zombie.setCoordinates(to.getCoordinates());
 		zombie.setSteps(zombie.getSteps() - 1);
 
 		// update Player
 		getCurrentPlayer().setZombies(moves - 1);
 
 		setChanged();
 		notifyObservers(Update.ZombieMoved);
 
 		return true;
 
 	}
 
 	/**
 	 * Tries to place a zombie on the given Field.
 	 * 
 	 * @param field
 	 *            - Field to place Zombie on
 	 * @return true if Zombie was placed
 	 */
 	@Override
 	public final boolean placeZombie(final IField field) {
 		int moves = getCurrentPlayer().getZombies();
 
 		// Make sure Field has a zombie, Field is a building and player can
 		// afford Zombie
 		if (field.getZombie() != null
 				|| !field.getType().equalsIgnoreCase("building")
 				|| moves < ZOMBIE_PLACE_COST) {
 			return false;
 		}
 
 		// add Zombie to Game
 		addZombie(new Zombie(field.getCoordinates()));
 
 		// update Player
 		getCurrentPlayer().setZombies(moves - ZOMBIE_PLACE_COST);
 
 		setChanged();
 		notifyObservers(Update.ZombieMoved);
 
 		return true;
 	}
 
 	/**
 	 * Tries to place the current Tile at the given coordinates.
 	 * 
 	 * @param coords
 	 *            - Coordinates to place current Tile at
 	 * @return true if successful
 	 */
 	@Override
 	public final boolean placeTile(final ICoordinates coords) {
 		if (currentTile == null || !map.addTile(coords, currentTile)) {
 			return false;
 		}
 
 		// If the Tile has Zombies, add them to the list
 		for (int x = 0; x < Tile.WIDTH_FIELDS; x++) {
 			for (int y = 0; y < Tile.HEIGHT_FIELDS; y++) {
 				IZombie zombie = currentTile.getField(y, x).getZombie();
 
 				if (zombie != null) {
 					addZombie(zombie);
 				}
 			}
 		}
 
 		setChanged();
 		notifyObservers(Update.TilePlaced);
 
 		currentTile = null;
 
 		return true;
 	}
 
 	/**
 	 * Checks whether the game is finished.
 	 * 
 	 * @return true if a player has won the game
 	 */
 	@Override
 	public boolean checkWin() {
 		for (IPlayer player : players) {
 			// If player has reached the win threshold, he wins
 			if (player.getPoints() >= winThreshold) {
 				winner = player;
 				return true;
 			}
 
 			ICoordinates coord = player.getCoordinates();
 			ITile tile = map.getTile(coord.toTile());
 			IField field = map.getField(coord);
 
 			if (field.getZombie() != null) {
 				return false;
 			}
 
 			// If player is on the Heli tile, he is eligible to win
 			if (tile.getTileType().getName().equals("Helicopter")) {
 				// If player stands on center field of Heli Tile, he wins
 				if (coord.toRelativeField().equals(new Coordinates(1, 1))) {
 					winner = player;
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Gets the game's winning player.
 	 * 
 	 * @return {@link IPlayer} who won
 	 */
 	@Override
 	public final IPlayer getWinner() {
 		return winner;
 	}
 
 	/**
 	 * Checks whether the game can be started given the current configuration.
 	 * 
 	 * @return true if game is ready.
 	 */
 	@Override
 	public final boolean isReady() {
 		return players.size() >= MIN_PLAYERS;
 	}
 
 	/**
 	 * Gets the game's current Zombie.
 	 * 
 	 * @return current {@link IZombie}
 	 */
 	@Override
 	public final IZombie getCurrentZombie() {
 		return currentZombie;
 	}
 
 	/**
 	 * Sets the game's current Zombie.
 	 * 
 	 * @param currentZombie
 	 *            - new current {@link IZombie}
 	 */
 	@Override
 	public final void setCurrentZombie(final IZombie currentZombie) {
 		this.currentZombie = currentZombie;
 
 		setChanged();
 		notifyObservers(Update.ZombieMoved);
 	}
 
 	/**
 	 * Checks if the Tile list is empty.
 	 * 
 	 * @return true if the Tile list is empty
 	 */
 	@Override
 	public final boolean noTiles() {
 		return tileList.isEmpty();
 	}
 
 	/**
 	 * Gets the game's win threshold. If a player reaches as many points as this
 	 * threshold, he wins the game.
 	 * 
 	 * @return win threshold
 	 */
 	@Override
 	public final int getWinThreshold() {
 		return winThreshold;
 	}
 
 	/**
 	 * Sets the game's win threshold. If a player reaches as many points as this
 	 * threshold, he wins the game.
 	 * 
 	 * @param winThreshold
 	 *            - new win threshold
 	 */
 	@Override
 	public final void setWinThreshold(final int winThreshold) {
 		this.winThreshold = winThreshold;
 	}
 
 	/**
 	 * Gets the game's list of Zombies.
 	 * 
 	 * @return List of {@link IZombie}s
 	 */
 	public LinkedList<IZombie> getZombies() {
 		return zombies;
 	}
 
 	/**
 	 * Adds the given Zombie to the game. Also updates the Field the Zombie will
 	 * stand on.
 	 * 
 	 * @param zombie
 	 *            - {@link IZombie} to be added
 	 */
 	public final void addZombie(final IZombie zombie) {
 		zombies.add(zombie);
 		map.getField(zombie.getCoordinates()).setZombie(zombie);
 	}
 
 	/**
 	 * Removes the current Zombie from the game. Also updates the Field the
 	 * Zombie was standing on.
 	 */
 	public final void removeZombie() {
 		IField field = map.getField(getCurrentPlayer().getCoordinates());
 		zombies.remove(field.getZombie());
 		field.setZombie(null);
 		setChanged();
 		notifyObservers(Update.ZombieMoved);
 	}
 
 	/**
 	 * Checks whether this game has zombification turned on.
 	 * 
 	 * @return true if zombification is on
 	 */
 	@Override
 	public final boolean isZombification() {
 		return zombification;
 	}
 
 	/**
 	 * Sets whether this game has zombification turned on.
 	 * 
 	 * @param zombification
 	 *            new value for zombification
 	 */
 	public final void setZombification(final boolean zombification) {
 		this.zombification = zombification;
 	}
 
 	/**
 	 * Revives the current player. If zombification is on, a {@link SuperZombie}
 	 * is placed at the player's position. If hardcore mode is on and the player
 	 * has reached the maximum number of revives, the player is removed from the
 	 * game.
 	 */
 	@Override
 	public void revive() {
 		if (isZombification()) {
 			removeZombie();
 			addZombie(new SuperZombie(getCurrentPlayer().getCoordinates()));
 		}
 
 		getCurrentPlayer().revive();
 
 		if (hardcore && getCurrentPlayer().getRevives() > revives) {
 			surrender();
 		}
 
 		setChanged();
 		notifyObservers(Update.PlayerAttributeUpdate);
 	}
 
 	/**
 	 * Checks whether the game awards an additional point when rolling the die.
 	 * This is the case when the helipad was discarded.
 	 * 
 	 * @return true if additional point is awarded
 	 */
 	@Override
 	public final boolean hasAdditionalPoint() {
 		return additionalPoint;
 	}
 
 	/**
 	 * Completely removes the current player from the game. If all players have
 	 * been removed, the {@link LoseMenu} is called. Is also called in hardcore
 	 * mode if the player dies too often.
 	 */
 	@Override
 	public final void surrender() {
 		players.remove(getCurrentPlayer());
 
 		if (players.size() == 1 && !isHardcore()) {
 			winner = players.getFirst();
 			window.setMenu(new WinnerMenu(window, this));
 			return;
 		}
 
 		if (players.isEmpty() || nextPlayer() == null) {
 			window.setMenu(new LoseMenu(window));
 			return;
 		}
 
 		setState(new TileState(window));
 	}
 
 	/**
 	 * Checks whether the game is in hardcore mode.
 	 * 
 	 * @return true if game is in hardcore mode
 	 */
 	@Override
 	public final boolean isHardcore() {
 		return hardcore;
 	}
 
 	/**
 	 * Sets whether the game is in hardcore mode.
 	 * 
 	 * @param hardcore
 	 *            whether the game should be in hardcore mode or not
 	 */
 	@Override
 	public final void setHardcore(boolean hardcore) {
 		this.hardcore = hardcore;
 	}
 
 	/**
 	 * Gets the maximum number of revives a player may have in hardcore mode.
 	 * 
 	 * @return maximum number of revives
 	 */
 	@Override
 	public final int getRevives() {
 		return revives;
 	}
 
 	/**
 	 * Sets the maximum number of revives in hardcore mode.
 	 * 
 	 * @param revives
 	 *            new maximum number of revives
 	 */
 	@Override
 	public final void setRevives(final int revives) {
 		this.revives = revives;
 	}
 
 	/**
 	 * Marks the game as changed and notifies its observers with the given
 	 * update.
 	 * 
 	 * @param update
 	 *            {@link Update} enum to be passed on to observers
 	 */
 	public final void update(final Update update) {
 		setChanged();
 		notifyObservers(update);
 	}
 
 	/**
 	 * Checks whether a new player with the given name and color can is valid.
 	 * Player names and colors must be unique and names must be between 3 and 15
 	 * characters long.
 	 * 
 	 * @param name
 	 *            new player name
 	 * @param color
 	 *            new player color
 	 * @return if there are no collisions and the given name has a valid length
 	 */
 	@Override
 	public final boolean isPlayerValid(final String name, final Color color) {
 		if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH
 				|| name.trim().length() == 0) {
 			return false;
 		}
 
 		for (IPlayer player : players) {
 			if (player.getName().equals(name)
 					|| player.getColor().equals(color)) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Gets this game's window handle.
 	 * 
 	 * @return window handle
 	 */
 	@Override
 	public final IWindow getWindow() {
 		return window;
 	}
 
 	/**
 	 * Determines whether the given zombie can be moved. A zombie can move if he
 	 * has at least one step left and at least one neighboring field is
 	 * accessible and has no zombie.
 	 * 
 	 * @param zombie
 	 *            zombie to check
 	 * @return true if zombie can be moved
 	 */
 	public boolean canZombieMove(IZombie zombie) {
 		IField from;
 
		if (zombie == null) {
 			return false;
 		}
 
 		from = map.getField(zombie.getCoordinates());
 
 		if (zombie.getSteps() < 1) {
 			return false;
 		}
 
 		for (Direction dir : Direction.values()) {
 			IField to = getMap().getField(zombie.getCoordinates().getDir(dir));
 
 			if (from.hasDir(dir) && to != null && to.getZombie() == null) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean isCoop() {
 		return false;
 	}
 
 	@Override
 	public void rollDie() {
 		setDie(new Random().nextInt(MAX_ROLL) + 1);
 	}
 
 	// -------------------------------------------------------------//
 	// --------------------------JUnitRv----------------------------//
 
 	/**
 	 * Method added for JUnitRV, opens a pop up showing the winner.
 	 */
 	public final void showWinner() {
 		if (winner == null) {
 			return;
 		}
 
 		// pop up here
 		// Popups.showWinner(winner.getName());
 	}
 
 	/**
 	 * Method for JUnitRV, simulates rolling.
 	 * 
 	 * @return value obtained by rolling
 	 */
 	public final int roll() {
 		if (hasAdditionalPoint()) {
 			return Math.min(new Random().nextInt(MAX_ROLL) + 2, MAX_ROLL);
 		}
 
 		return new Random().nextInt(MAX_ROLL) + 1;
 	}
 
 	@Override
 	public int getDieDifference() {
 		return getMap().getField(getCurrentPlayer().getCoordinates())
 				.getZombie().getWinThreshold()
 				- getDie();
 	}
 }
