 package stricken.board;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.swing.JComponent;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 
 import stricken.board.critter.Critter;
 import stricken.board.mode.AbstractBoardControlMode;
 import stricken.board.mode.AdventureMode;
 import stricken.board.mode.CombatMovementMode;
 import stricken.common.Direction;
 import stricken.event.Event;
 import stricken.event.IEventContext;
 import stricken.ui.IDelegatingKeySink;
 import stricken.ui.IKeySink;
 import stricken.ui.ILayer;
 
 /**
  * This class should be used to access the 2 dimension Tile array and keeps
  * registers of Critters, enabled and targeted Tiles
  * 
  * @author ofuangka
  * 
  */
 public class Board extends JComponent implements ILayer, IDelegatingKeySink {
 
 	private static final long serialVersionUID = -2949485746939158973L;
 
 	public static final int INVALID_X = -1;
 	public static final int INVALID_Y = -1;
 
 	private static final Logger log = Logger.getLogger(Board.class);
 
 	private IEventContext eventContext;
 
 	protected Tile[][] tiles;
 
 	private Dimension spriteSize;
 	private Critter mainCharacter;
 	private Critter controllingCritter;
 
 	private List<Critter> critters = new ArrayList<Critter>();
 	private List<Critter> sequence = new ArrayList<Critter>();
 
 	private List<Tile> disabledTiles = new ArrayList<Tile>();
 	private List<Tile> tilesInTargetingRange = new ArrayList<Tile>();
 	private List<Tile> targetedTiles = new ArrayList<Tile>();
 
 	private List<AbstractBoardControlMode> modeHistory = new ArrayList<AbstractBoardControlMode>();
 
 	public Board(IEventContext eventContext) {
 		this.eventContext = eventContext;
 	}
 
 	/**
 	 * Sets the controlling Critter and calls repaint()
 	 * 
 	 * @param newCritter
 	 */
 	public void assignControl(Critter newCritter) {
 		log.debug("Assigning control to Critter " + newCritter + "...");
 		if (controllingCritter != null) {
 			controllingCritter.setSelected(false);
 		}
 		controllingCritter = newCritter;
 		controllingCritter.setSelected(true);
 		repaint();
 	}
 
 	@Override
 	public void backspace() {
 		getCurrentKeySink().backspace();
 	}
 
 	public void clear() {
 		sequence.clear();
 		critters.clear();
 		disabledTiles.clear();
 		tilesInTargetingRange.clear();
 		targetedTiles.clear();
 	}
 
 	/**
 	 * Clears all targeted Tiles
 	 */
 	public void clearCrosshair() {
 		log.debug("Clearing targeted Tile objects...");
 		while (!targetedTiles.isEmpty()) {
 			targetedTiles.remove(0).setTargeted(false);
 		}
 		repaint();
 	}
 
 	/**
 	 * Enables all tiles
 	 */
 	public void clearDisabledTiles() {
 		log.debug("Removing all out of range tiles...");
 		while (!disabledTiles.isEmpty()) {
 			disabledTiles.remove(0).setEnabled(true);
 		}
 		repaint();
 	}
 
 	/**
 	 * Puts all tiles out of targeting range
 	 */
 	public void clearTargetingRange() {
 		log.debug("Clearing targetable Tile objects...");
 		while (!tilesInTargetingRange.isEmpty()) {
 			tilesInTargetingRange.remove(0).setInTargetingRange(false);
 		}
 	}
 
 	private void createCritterSequence() {
 		log.debug("Creating Critter sequence...");
 		// TODO: implement
 		sequence.addAll(critters);
 	}
 
 	public void disableAllTiles() {
 		for (int x = 0; x < tiles.length; x++) {
 			for (int y = 0; y < tiles[x].length; y++) {
 				tiles[x][y].setEnabled(false);
 				disabledTiles.add(tiles[x][y]);
 			}
 		}
 		repaint();
 	}
 
 	@Override
 	public void down() {
 		getCurrentKeySink().down();
 	}
 
 	@Override
 	public void enter() {
 		getCurrentKeySink().enter();
 	}
 
 	@Override
 	public void esc() {
 		getCurrentKeySink().esc();
 	}
 
 	public Tile[] getAdjacentTiles(AbstractPositionedSprite piece) {
 		return getAdjacentTiles(piece.getX(), piece.getY());
 	}
 
 	public Tile[] getAdjacentTiles(int x, int y) {
 		Tile[] ret = new Tile[4];
 		if (isInBounds(x, y)) {
 			ret = getTile(x, y).getAdjacentTiles();
 		}
 		return ret;
 	}
 
 	public Tile[] getAdjacentTiles(Tile tile) {
 		return tile.getAdjacentTiles();
 	}
 
 	public Critter getControllingCritter() {
 		return controllingCritter;
 	}
 
 	/**
 	 * The Board returns whichever current control mode
 	 */
 	@Override
 	public IKeySink getCurrentKeySink() {
 		return modeHistory.get(modeHistory.size() - 1);
 	}
 
 	public Tile getTile(int x, int y) {
 		return tiles[x][y];
 	}
 
 	public Tile[][] getTiles() {
 		return tiles;
 	}
 
 	/**
 	 * The Board layer is never empty
 	 * 
 	 * @return
 	 */
 	@Override
 	public boolean isEmpty() {
 		return false;
 	}
 
 	/**
	 * Checks all game over conditions
	 * 
	 * @return
	 */
	private boolean isHumanOnBoard() {
		boolean ret = false;
		for (Critter critter : critters) {
			if (critter.isHuman()) {
				return true;
			}
		}
		return ret;
	}

	/**
 	 * Checks if the given coordinates are within the bounds of the board
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public boolean isInBounds(int x, int y) {
 		return !(x < 0) && !(y < 0) && x < tiles.length && y < tiles[x].length;
 	}
 
 	public boolean isInCombat() {
 		boolean ret = false;
 		if (critters.size() > 1) {
 			for (Critter critter : critters) {
 				if (critter.isHostile()) {
 					ret = true;
 					break;
 				}
 			}
 		}
 		return ret;
 	}
 
 	@Override
 	public void left() {
 		getCurrentKeySink().left();
 	}
 
 	public void load(String id) {
 		log.info("Loading board '" + id + "'...");
 
 		// TODO: implement
 		tiles = new Tile[11][11];
 		for (int x = 0; x < tiles.length; x++) {
 			for (int y = 0; y < tiles[x].length; y++) {
 				tiles[x][y] = new Tile(spriteSize, x, y);
 				if (isInBounds(x - 1, y)) {
 					tiles[x - 1][y].setRight(tiles[x][y]);
 					tiles[x][y].setLeft(tiles[x - 1][y]);
 				}
 				if (isInBounds(x, y - 1)) {
 					tiles[x][y - 1].setBottom(tiles[x][y]);
 					tiles[x][y].setTop(tiles[x][y - 1]);
 				}
 			}
 		}
 
 		Random random = eventContext.getRandom();
 		int numCritters = random.nextInt(11) + 1;
 		for (int i = 0; i < numCritters; i++) {
 			Critter critter = new Critter(spriteSize, new Color(
 					random.nextInt(255), random.nextInt(255),
 					random.nextInt(255)));
 			critter.setStat(Critter.Stat.MAXHP, random.nextInt(3) + 1);
 			critter.setStat(Critter.Stat.HP,
 					critter.getStat(Critter.Stat.MAXHP));
 			critter.setStat(Critter.Stat.STRENGTH, random.nextInt(3) + 1);
 			critter.setStat(Critter.Stat.SPEED, random.nextInt(3) + 1);
 			critter.setHostile(random.nextBoolean());
 			critter.setHuman(random.nextBoolean());
 
 			List<String> talents = new ArrayList<String>();
 
 			if (random.nextBoolean()) {
 				talents.add("FIRE_BOMB");
 			}
 			if (random.nextBoolean()) {
 				talents.add("LIGHTNING_STRIKE");
 			}
 			critter.setTalents(talents);
 
 			int x = random.nextInt(11);
 			int y = random.nextInt(11);
 			if (!getTile(x, y).isOccupied()) {
 				placePiece(critter, x, y);
 			}
 		}
 		mainCharacter = critters.get(0);
 		mainCharacter.setHuman(true);
 		mainCharacter.setHostile(false);
 
 	}
 
 	public void nextTurn() {
 		log.debug("Starting new turn...");
 
 		clearDisabledTiles();
 		clearTargetingRange();
 		clearCrosshair();
 
 		modeHistory.clear();
 
 		// check end conditions
 		if (!isMainCharacterOnBoard()) {
 			eventContext.fire(Event.LOSE_CONDITION);
 		} else {
 			AbstractBoardControlMode firstMode;
 			if (isInCombat()) {
 
 				if (sequence.isEmpty()) {
 					log.debug("Critter sequence empty, starting new round...");
 					createCritterSequence();
 				}
 
 				// figure out who the next controlling piece is
 				assignControl(sequence.remove(0));
 
 				// detect and set up the first control mode
 				firstMode = new CombatMovementMode(this, eventContext);
 			} else {
 
 				// move all of the non main character critters
 				Direction[] possibleDirs = new Direction[] { Direction.UP,
 						Direction.RIGHT, Direction.DOWN, Direction.LEFT };
 				for (int i = 0; i < critters.size(); i++) {
 					Critter critter = critters.get(i);
 					if (critter != mainCharacter) {
 						int nextDir = eventContext.getRandom()
 								.nextInt(5);
 						if (nextDir < possibleDirs.length) {
 							tryMove(critter, possibleDirs[nextDir]);
 						}
 					}
 				}
 
 				// give the main character control
 				assignControl(mainCharacter);
 
 				firstMode = new AdventureMode(this, eventContext);
 			}
 
 			pushMode(firstMode);
 
 			// wait for user input
 		}
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D g2d = (Graphics2D) g;
 		for (int x = 0; x < tiles.length; x++) {
 			for (int y = 0; y < tiles[x].length; y++) {
 				Dimension tileSize = tiles[x][y].getSpriteSize();
 				int dx1 = x * tileSize.width;
 				int dx2 = dx1 + tileSize.width;
 				int dy1 = y * tileSize.height;
 				int dy2 = dy1 + tileSize.height;
 				g2d.drawImage(tiles[x][y].getImage(), dx1, dy1, dx2, dy2, 0, 0,
 						tileSize.width, tileSize.height, null);
 			}
 		}
 	}
 
 	public void placePiece(AbstractBoardPiece piece, int x, int y) {
 		removePiece(piece);
 		if (tiles[x][y] == null) {
 			throwNullTileException(x, y);
 		} else {
 			tiles[x][y].add(piece);
 			piece.setXY(x, y);

			if (Critter.class.isAssignableFrom(piece.getClass())) {
				critters.add((Critter) piece);
			}
 			repaint();
 		}
 
 	}
 
 	public void popMode() {
 		modeHistory.remove(modeHistory.size() - 1);
 		refreshMode();
 	}
 
 	public void pushMode(AbstractBoardControlMode mode) {
 		mode.readAndStoreState();
 		mode.configureTileState();
 		modeHistory.add(mode);
 	}
 
 	public void refreshMode() {
 		modeHistory.get(modeHistory.size() - 1).configureTileState();
 	}
 
 	public void removeCritter(Critter critter) {
 		getTile(critter.getX(), critter.getY()).remove(critter);
 		sequence.remove(critter);
 		critters.remove(critter);
 		repaint();
 	}
 
 	public void removePiece(AbstractBoardPiece piece) {
 		int x = piece.getX();
 		int y = piece.getY();
 
 		if (isInBounds(x, y)) {
 			if (tiles[x][y] == null) {
 				throwNullTileException(x, y);
 			} else {
 				tiles[x][y].remove(piece);
 				piece.setXY(INVALID_X, INVALID_Y);
 			}
 
			if (Critter.class.isAssignableFrom(piece.getClass())) {
				critters.remove((Critter) piece);
			}
 			repaint();
 		}
 		// else do nothing (removing a piece that's not on the board)
 	}
 
 	public void renderCrosshair(List<Tile> tilesToTarget) {
 		clearCrosshair();
 		if (tilesToTarget != null) {
 			for (Tile tile : tilesToTarget) {
 				tile.setTargeted(true);
 				targetedTiles.add(tile);
 			}
 			repaint();
 		} else {
 			log.warn("targetTiles called with null Tile List");
 		}
 	}
 
 	@Override
 	public void right() {
 		getCurrentKeySink().right();
 	}
 
 	/**
 	 * This allows movement to the provided Tile List and only to that List
 	 * 
 	 * @param enabledTiles
 	 */
 	public void setEnabledTiles(List<Tile> enabledTiles) {
 		disableAllTiles();
 		if (enabledTiles != null) {
 			for (Tile tile : enabledTiles) {
 				tile.setEnabled(true);
 				disabledTiles.remove(tile);
 			}
 		}
 		repaint();
 	}
 
 	@Required
 	public void setSpriteSize(Dimension spriteSize) {
 		this.spriteSize = spriteSize;
 	}
 
 	/**
 	 * Clears any previous targeting range and sets a new one
 	 * 
 	 * @param targetingRange
 	 */
 	public void setTargetingRange(List<Tile> targetingRange) {
 		clearTargetingRange();
 		if (targetingRange != null) {
 			for (Tile tile : targetingRange) {
 				tile.setInTargetingRange(true);
 				tilesInTargetingRange.add(tile);
 			}
 			repaint();
 		} else {
 			log.warn("setTargetable called with null Tile List");
 		}
 	}
 
 	public boolean isMainCharacterOnBoard() {
 		return critters.contains(mainCharacter);
 	}
 
 	@Override
 	public void space() {
 		getCurrentKeySink().space();
 	}
 
 	/**
 	 * Convenience method that throws an NullPointerException with the
 	 * coordinates of the null Tile
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	private void throwNullTileException(int x, int y) {
 		throw new IllegalArgumentException("Tile was null at coordinates (" + x
 				+ ", " + y + ")!");
 	}
 
 	/**
 	 * Moves an arbitrary piece one space in the given direction unless the Tile
 	 * in the direction is invalid, disabled or occupied. Returns true if the
 	 * move was successful
 	 * 
 	 * @param piece
 	 * @param dir
 	 */
 	public boolean tryMove(AbstractBoardPiece piece, Direction dir) {
 		int nextX = piece.getX();
 		int nextY = piece.getY();
 		switch (dir) {
 		case UP: {
 			nextY--;
 			break;
 		}
 		case RIGHT: {
 			nextX++;
 			break;
 		}
 		case DOWN: {
 			nextY++;
 			break;
 		}
 		case LEFT: {
 			nextX--;
 			break;
 		}
 		default: {
 			break;
 		}
 		}
 		if (isInBounds(nextX, nextY) && getTile(nextX, nextY).isEnabled()
 				&& !getTile(nextX, nextY).isOccupied()) {
 			placePiece(piece, nextX, nextY);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean tryMove(Direction dir) {
 		return tryMove(controllingCritter, dir);
 	}
 
 	@Override
 	public void up() {
 		getCurrentKeySink().up();
 	}
 
 	@Override
 	public void x() {
 		getCurrentKeySink().x();
 	}
 }
