 package nl.tudelft.jpacman.level;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import nl.tudelft.jpacman.factory.BoardFactory;
 import nl.tudelft.jpacman.factory.CharacterFactory;
 import nl.tudelft.jpacman.factory.LevelFactory;
 import nl.tudelft.jpacman.model.Board;
 import nl.tudelft.jpacman.model.Direction;
 import nl.tudelft.jpacman.model.FloorSquare;
 import nl.tudelft.jpacman.model.Ghost;
 import nl.tudelft.jpacman.model.GhostColour;
 import nl.tudelft.jpacman.model.PacMan;
 import nl.tudelft.jpacman.model.Pellet;
 import nl.tudelft.jpacman.model.Square;
 
 /**
  * Parses text representations of {@link Board}s into {@link Level}s.
  * 
  * @author Jeroen Roosen
  */
 public class MapParser {
 
 	private static final GhostColour[] GHOST_ORDER = { GhostColour.RED,
 			GhostColour.PINK, GhostColour.CYAN, GhostColour.ORANGE };
 	private static final Direction DEFAULT_DIRECTION = Direction.WEST;
 	private static final char EMPTY_SQUARE = ' ';
 	private static final char WALL = '#';
 	private static final char PACMAN = 'P';
 	private static final char PELLET = '.';
 	private static final char GHOST = 'G';
 
 	private final BoardFactory boardFactory;
 	private final LevelFactory levelFactory;
 	private final CharacterFactory characterFactory;
 	private int ghostIndex;
 
 	public MapParser(LevelFactory levelFactory, BoardFactory boardFactory,
 			CharacterFactory characterFactory) {
 		this.levelFactory = levelFactory;
 		this.boardFactory = boardFactory;
 		this.characterFactory = characterFactory;
 
 		this.ghostIndex = 0;
 	}
 
 	/**
 	 * Parses a string representation of a Board into a new Level.
 	 * 
 	 * @param rows
 	 *            The rows of the board.
 	 * @return The parsed level.
 	 * @throws MapParserException
 	 *             When an invalid character is encountered.
 	 */
 	public Level parseMap(String[] rows) throws MapParserException {
 		assert rows != null;
 
 		int height = rows.length;
 		enforceHeight(height);
 		int width = rows[0].length();
 
 		Square[][] grid = new Square[width][height];
 		LevelBuilder builder = new LevelBuilder();
 		for (int y = 0; y < height; y++) {
 			char[] row = (rows[y]).toCharArray();
 			enforceWidth(width, y, row);
 			for (int x = 0; x < width; x++) {
 				grid[x][y] = parseSquare(row[x], builder);
 			}
 		}
 		Board board = boardFactory.createBoard(grid);
 		connectGraph(board);
 		
 		return builder.withBoard(board).build(levelFactory);
 	}
 	
 	private void connectGraph(Board board) {
 		int w = board.getWidth();
 		int h = board.getHeight();
 		
 		for (int x = 0; x < w; x++) {
 			for (int y = 0; y < h; y++) {
 				Square node = board.getSquareAt(x, y);
 				for (Direction d : Direction.values()) {
 					Square neighbour = relativeSquare(x, y, board, d);
 					node.addNeighbour(neighbour, d);
 				}
 			}
 		}
 	}
 	
 	private Square relativeSquare(int x, int y, Board board, Direction d) {
 		int w = board.getWidth();
 		int h = board.getHeight();
 		
 		int newX = (w + x + d.getDeltaX()) % w;
 		int newY = (h + y + d.getDeltaY()) % h;
 		
 		return board.getSquareAt(newX, newY);
 	}
 
 	private void enforceWidth(int width, int y, char[] row)
 			throws MapParserException {
 		if (row.length != width) {
 			throw new MapParserException(
 					"Encountered a row with an unexpected amount of cells at row "
 							+ y);
 		}
 	}
 
 	private void enforceHeight(int height) throws MapParserException {
 		if (height == 0) {
 			throw new MapParserException(
 					"Unable to create a level for an empty map.");
 		}
 	}
 
 	protected BoardFactory getBoardFactory() {
 		return boardFactory;
 	}
 
 	protected LevelFactory getLevelFactory() {
 		return levelFactory;
 	}
 
 	protected CharacterFactory getCharacterFactory() {
 		return characterFactory;
 	}
 
 	/**
 	 * Parses a square and assigns all relevant content to the level builder.
 	 * 
 	 * @param c
 	 *            The character representation of the square to parse.
 	 * @param builder
 	 *            The level builder to which relevant data can be assigned.
 	 * @return A new square as represented by the character.
 	 * @throws MapParserException
 	 *             When the character was invalid.
 	 */
 	protected Square parseSquare(char c, LevelBuilder builder)
 			throws MapParserException {
 		assert builder != null;
 
 		switch (c) {
 		case WALL:
 			return getBoardFactory().createWallSquare();
 		case EMPTY_SQUARE:
 			return emptySquare();
 		case PACMAN:
 			return pacManSquare(builder);
 		case PELLET:
 			return pelletSquare(builder);
 		case GHOST:
 			return ghostSquare(builder);
 		default:
 			throw new MapParserException("Unable to parse square for: " + c);
 		}
 	}
 
 	private GhostColour nextGhostColour() {
 		GhostColour colour = GHOST_ORDER[ghostIndex];
 		ghostIndex++;
 		return colour;
 	}
 
 	private Square pelletSquare(LevelBuilder builder) {
 		FloorSquare square = emptySquare();
 		Pellet pellet = getBoardFactory().createPellet();
 		square.setPellet(pellet);
 		builder.addPellet();
 		return square;
 	}
 
 	private FloorSquare emptySquare() {
 		FloorSquare square = getBoardFactory().createFloorSquare();
 		return square;
 	}
 
 	private Square pacManSquare(LevelBuilder builder) {
 		FloorSquare square = emptySquare();
 		PacMan pacMan = getCharacterFactory().createPacMan(DEFAULT_DIRECTION);
 		pacMan.occupy(square);
 		builder.addPacMan(pacMan);
 		return square;
 	}
 
 	private Square ghostSquare(LevelBuilder builder) {
 		FloorSquare square = emptySquare();
 		Ghost ghost = characterFactory.createGhost(nextGhostColour(),
 				DEFAULT_DIRECTION);
 		ghost.occupy(square);
 		builder.addGhost(ghost);
 		return square;
 	}
 
 	/**
 	 * Parses a string representation of a Board into a new Level.
 	 * 
 	 * @param inputStream
 	 *            The input stream providing the map.
 	 * @return The parsed level.
 	 * @throws MapParserException
 	 *             When an invalid character is encountered.
 	 */
 	public Level parseMap(InputStream inputStream) throws MapParserException {
 		assert inputStream != null;
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				inputStream));
 
 		List<String> rows = new ArrayList<String>();
 		try {
 			while (reader.ready()) {
 				rows.add(reader.readLine());
 			}
 		} catch (IOException e) {
 			throw new MapParserException("Unable to read input stream.", e);
 		}
 
 		return parseMap(rows.toArray(new String[rows.size()]));
 	}
 
 	/**
 	 * Exception thrown by MapParser when components could not be parsed.
 	 * 
 	 * @author Jeroen Roosen
 	 */
 	public class MapParserException extends Exception {
 
 		private static final long serialVersionUID = -2031847716998937467L;
 
 		/**
 		 * Create a new exception.
 		 * 
 		 * @param message
 		 *            The message describing the cause of this exception.
 		 */
 		public MapParserException(String message) {
 			super(message);
 		}
 
 		/**
 		 * Create a new exception.
 		 * 
 		 * @param message
 		 *            The message describing the cause of this exception.
 		 * @param cause
 		 *            The cause of this exception.
 		 */
 		public MapParserException(String message, Throwable cause) {
 			super(message, cause);
 		}
 
 	}
 
 	/**
 	 * Utility to build a level.
 	 * 
 	 * @author Jeroen Roosen
 	 */
 	protected class LevelBuilder {
 
 		private Collection<PacMan> pacMans;
 		private Collection<Ghost> ghosts;
 		private Board board;
 		private int pelletCount;
 
 		/**
 		 * Creates a new level builder.
 		 */
 		protected LevelBuilder() {
 			ghosts = new ArrayList<>();
 			pacMans = new ArrayList<>();
 			pelletCount = 0;
 		}
 
 		/**
 		 * Adds a Pac-Man to this level.
 		 * 
 		 * @param pacMan
 		 *            the Pac-Man to add to this Level.
 		 * @return The builder for fluency.
 		 */
 		protected LevelBuilder addPacMan(PacMan pacMan) {
 			assert pacMan != null;
 
 			pacMans.add(pacMan);
 			return this;
 		}
 
 		/**
 		 * Adds a ghost to the level.
 		 * 
 		 * @param ghost
 		 *            The ghost to add.
 		 * @return The builder for fluency.
 		 */
 		protected LevelBuilder addGhost(Ghost ghost) {
 			assert ghost != null;
 
 			ghosts.add(ghost);
 			return this;
 		}
 
 		/**
 		 * Sets the board for this level.
 		 * 
 		 * @param board
 		 *            The board to set for this Level.
 		 * @return The builder for fluency.
 		 */
 		protected LevelBuilder withBoard(Board board) {
 			assert board != null;
 
 			this.board = board;
 			return this;
 		}
 
 		/**
 		 * Adds a pellet to the level.
 		 * 
 		 * @return The builder for fluency.
 		 */
 		protected LevelBuilder addPellet() {
 			pelletCount++;
 			return this;
 		}
 
 		/**
 		 * Builds the level.
 		 * 
 		 * @param factory
 		 *            The factory to build the level with.
 		 * @return A new level with the board, Pac-Man and ghosts.
 		 */
 		protected Level build(LevelFactory factory) {
 			assert board != null;
 			return factory.createLevel(board, pacMans, ghosts, pelletCount);
 		}
 	}
 }
