 package cn.edu.tsinghua.academic.c00740273.magictower.engine;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.util.Map;
 
 /**
  * This is the main entry point of a game application run.
  */
 public class Engine {
 
 	public enum Termination {
 		/**
 		 * Terminate the game automatically based on attributes or other
 		 * criteria.
 		 */
 		AUTOMATIC,
 		/**
 		 * Reject actions that lead to game termination.
 		 */
 		MANUAL,
 		/**
 		 * Never check termination criteria.
 		 */
 		NEVER;
 	}
 
 	protected Game game;
 	protected Termination successTermination, failureTermination;
 
 	public Engine() {
 		this.successTermination = Termination.AUTOMATIC;
 		this.failureTermination = Termination.MANUAL;
 	}
 
 	/**
 	 * Get the game running currently.
 	 * 
 	 * @return
 	 */
 	public Game getGame() {
 		return this.game;
 	}
 
 	/**
 	 * Run the given game in this application.
 	 * 
 	 * The given game must be already initialized.
 	 * 
 	 * @param game
 	 */
 	public void setGame(Game game) {
 		this.game = game;
 	}
 
 	/**
 	 * Initialize and load a newly created Game instance.
 	 * 
 	 * @param game
 	 * @return
 	 * @throws DataException
 	 */
 	public Event loadGame(Game game) throws DataException {
 		Event event = game.initialize();
 		this.setGame(game);
 		return event;
 	}
 
 	/**
 	 * Load game from previously saved data.
 	 * 
 	 * This clears any existing execution information.
 	 * 
 	 * @param serialization
 	 * @throws IOException
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 * @throws ClassNotFoundException
 	 */
 	public void unserializeGame(byte[] serialization) throws IOException,
 			InstantiationException, IllegalAccessException,
 			ClassNotFoundException {
 		ByteArrayInputStream bis = new ByteArrayInputStream(serialization);
 		ObjectInput in = null;
 		try {
 			in = new ObjectInputStream(bis);
 			Game game = (Game) Class.forName((String) in.readObject())
 					.newInstance();
 			GameData gameData = (GameData) in.readObject();
 			game.setGameData(gameData);
 			this.setGame(game);
 		} finally {
 			bis.close();
 			in.close();
 		}
 	}
 
 	/**
 	 * Save data for the current game for future load.
 	 * 
 	 * @return Data
 	 * @throws IOException
 	 */
 	public byte[] serializeGame() throws IOException {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		ObjectOutput out = null;
 		try {
 			out = new ObjectOutputStream(bos);
 			out.writeObject(this.game.getClass().getName());
 			out.writeObject(this.game.getGameData());
 			return bos.toByteArray();
 		} finally {
 			out.close();
 			bos.close();
 		}
 	}
 
 	/**
 	 * Get the current success termination mode.
 	 * 
 	 * @return
 	 */
 	public Termination getSuccessTermination() {
 		return this.successTermination;
 	}
 
 	/**
 	 * Set the current success termination mode.
 	 * 
 	 * @param termination
 	 * @return
 	 */
 	public Termination setSuccessTermination(Termination termination) {
 		Termination prev = this.successTermination;
 		this.successTermination = termination;
 		return prev;
 	}
 
 	/**
 	 * Get the current failure termination mode.
 	 * 
 	 * @return
 	 */
 	public Termination getFailureTermination() {
 		return this.failureTermination;
 	}
 
 	/**
 	 * Set the current failure termination mode.
 	 * 
 	 * @param termination
 	 * @return
 	 */
 	public Termination setFailureTermination(Termination termination) {
 		Termination prev = this.failureTermination;
 		this.failureTermination = termination;
 		return prev;
 	}
 
 	protected void checkGameLoad() {
 		if (this.game == null) {
 			throw new IllegalStateException("No game has been loaded yet.");
 		}
 	}
 
 	@SuppressWarnings("incomplete-switch")
 	protected Event moveEvent(Coordinate coord) throws GameTerminationException {
 		Event event = this.game.attemptMoveTo(coord);
 		try {
 			this.game.simulateEvent(event);
 		} catch (GameSuccessTerminationException e) {
 			switch (this.getSuccessTermination()) {
 			case AUTOMATIC:
 				throw e;
 			case MANUAL:
 				return null;
 			}
 		} catch (GameFailureTerminationException e) {
 			switch (this.getFailureTermination()) {
 			case AUTOMATIC:
 				throw e;
 			case MANUAL:
 				return null;
 			}
 		}
 		return event;
 	}
 
 	/**
 	 * Move the character to the given coordinate.
 	 * 
 	 * @param coord
 	 * @return The event, or null if rejected due to termination policy.
 	 * @throws GameTerminationException
 	 */
 	public Event moveTo(Coordinate coord) throws GameTerminationException {
 		this.checkGameLoad();
 		Event event = this.moveEvent(coord);
 		if (event != null) {
 			this.game.applyEvent(event);
 		}
 		return event;
 	}
 
 	/**
 	 * See what happens if the character moves to the given coordinate.
 	 * 
 	 * @param coord
	 * @return The event, or null if rejected due to termination policy.
 	 * @throws GameTerminationException
 	 */
 	public Event simulateMoveTo(Coordinate coord)
 			throws GameTerminationException {
 		this.checkGameLoad();
 		return this.moveEvent(coord);
 	}
 
 	/**
 	 * Fetch the event when the character moves to the given coordinate.
 	 * 
 	 * Does not check game termination.
 	 * 
 	 * @param coord
	 * @return The event
 	 */
 	public Event attemptMoveTo(Coordinate coord) {
 		this.checkGameLoad();
 		return this.game.attemptMoveTo(coord);
 	}
 
 	/**
 	 * Get the current position of the character.
 	 * 
 	 * @return
 	 */
 	public Coordinate getCurrentCoordinate() {
 		this.checkGameLoad();
 		return this.game.getCurrentCoordinate();
 	}
 
 	/**
 	 * Get current attributes of the character.
 	 * 
 	 * @return
 	 */
 	public Map<String, Object> getAttributes() {
 		return this.game.getAttributes();
 	}
 
 	/**
 	 * Get a current attribute of the character.
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public Object getAttribute(String key) {
 		return this.game.getAttribute(key);
 	}
 
 	/**
 	 * Set a current attribute of the character.
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public Object setAttribute(String key, Object value) {
 		Object prev = this.game.getAttribute(key);
 		this.game.setAttribute(key, value);
 		return prev;
 	}
 
 	/**
 	 * Get the Tile at given coordinate.
 	 * 
 	 * @param coord
 	 * @return
 	 */
 	public Tile getTile(Coordinate coord) {
 		this.checkGameLoad();
 		return this.game.getTile(coord);
 	}
 
 	/**
 	 * Shortcut function to get Tile objects for a whole layer.
 	 * 
 	 * @param z
 	 * @return Tile[x][y]
 	 */
 	public Tile[][] getLayerTiles(int z) {
 		this.checkGameLoad();
 		if (this.game instanceof TileArrayGame) {
 			TileArrayGame game = (TileArrayGame) this.game;
 			return game.getTiles()[z];
 		} else {
 			int maxX = this.game.getMaximumCoordinate().getX();
 			int maxY = this.game.getMaximumCoordinate().getY();
 			Tile[][] layerTiles = new Tile[maxX + 1][maxY + 1];
 			for (int x = 0; x <= maxX; x++) {
 				for (int y = 0; y <= maxY; y++) {
 					Coordinate coord = new Coordinate(z, x, y);
 					layerTiles[x][y] = this.game.getTile(coord);
 				}
 			}
 			return layerTiles;
 		}
 	}
 
 	public Coordinate getMaximumCoordinate() {
 		this.checkGameLoad();
 		return this.game.getMaximumCoordinate();
 	}
 
 }
