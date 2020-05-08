 package ch.bfh.ti.projekt1.sokoban.model;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import ch.bfh.ti.projekt1.sokoban.controller.AbstractController;
 import ch.bfh.ti.projekt1.sokoban.core.dijkstra.Dijkstra;
 import ch.bfh.ti.projekt1.sokoban.core.dijkstra.Edge;
 import ch.bfh.ti.projekt1.sokoban.core.dijkstra.Vertex;
 
 /**
  * @author svennyffenegger
  * @since 11.10.13 13:32
  *        <p/>
  *        Model for Game Board. It contains a grid with the fields and the
  *        current position of the player.
  */
 public class Board extends AbstractModel {
 	private static final Logger LOG = Logger.getLogger(Board.class);
 
 	// the current position of the player
 	private Position position;
 	// grid of the fields
 	private Field[][] grid;
 	private String levelName;
 
 	private ArrayList<String> playerPath;
 
 	private List<Direction> moves;
 	private String uuid;
 	private int diamondMoveCounter;
 
 	public int getDiamondMoveCounter() {
 		return diamondMoveCounter;
 	}
 
 	public void setDiamondMoveCounter(int diamondMoveCounter) {
 		this.diamondMoveCounter = diamondMoveCounter;
 	}
 
 	public String getUuid() {
 		return uuid;
 	}
 
 	public void setUuid(String uuid) {
 		this.uuid = uuid;
 	}
 
 	// used for the editor mainly
 	public Board(int width, int height) {
 		this(width, height, null);
 	}
 
 	// used for the game
 	public Board(int width, int height, Position startPosition) {
 		this.position = startPosition;
 		this.playerPath = new ArrayList<String>();
 		diamondMoveCounter = 0;
 		grid = new Field[width][height];
 		moves = new LinkedList<Direction>();
 	}
 
 	public String getLevelName() {
 		return levelName;
 	}
 
 	public void setLevelName(String levelName) {
 		this.levelName = levelName;
 		firePropertyChange(AbstractController.PROPERTY_LEVEL_NAME,
 				this.levelName, this.levelName);
 	}
 
 	/**
 	 * Returns the Path the player has made so far.
 	 * 
 	 * @return String
 	 */
 	public String getPlayerPath() {
 		return playerPath.toString();
 	}
 
 	public Field[][] getGrid() {
 		return grid;
 	}
 
 	/**
 	 * @param xPos
 	 *            horizontal position of the field
 	 * @param yPos
 	 *            vertical position of the field on the board
 	 * @param field
 	 * @throws IllegalArgumentException
 	 *             is thrown when the position is outside the board.
 	 */
 	public void setField(int xPos, int yPos, Field field)
 			throws IllegalArgumentException {
 		if (xPos < 0 || yPos < 0) {
 			throw new IllegalArgumentException("Parameters given are invalid!");
 		}
 
 		if (xPos >= grid.length || yPos >= grid[0].length) {
 			throw new IllegalArgumentException("Position out of bounds!");
 		}
 
 		/**
 		 * If the board already has a player, then a exception is thrown
 		 */
 		if (field.getState() == FieldState.PLAYER
 				&& doesFieldContainType(FieldState.PLAYER)) {
 			throw new IllegalArgumentException(
 					"Board can only have one player!");
 		}
 
 		Field oldField = grid[xPos][yPos];
 
 		grid[xPos][yPos] = field;
 
 		// the view gets notified about the new field on this position
 		firePropertyChange(AbstractController.PROPERTY_FIELD, oldField,
 				grid[xPos][yPos]);
 	}
 
 	/**
 	 * @return the current position of the player
 	 */
 	public Position getPosition() {
 		return position;
 	}
 
 	/**
 	 * Checks if a given field can be walked on
 	 * 
 	 * @param state
 	 * @return boolean
 	 */
 	public boolean isWalkable(FieldState state) {
 		if (state == FieldState.EMPTY || state == FieldState.GOAL) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public int getIndexOfNextRow(int currentPos, int height) {
 		return currentPos + height;
 	}
 
 	/**
 	 * Method that gives the distances each available field
 	 * 
 	 * @return ArrayList<Vertex>
 	 */
 	public Distance getDistances(int nStart, int iStart, int nEnd, int iEnd) {
 		Distance distance = new Distance();
 		try {
 
 			// Iterate through all the fields to check the distances
 			int nextRow = grid.length;
 			int position = 0;
 
 			// First initialize with all the vertices
 			for (int i = 0; i < grid[0].length; i++) { // i = y achse [][][][][]
 				for (int n = 0; n < grid.length; n++) { // n = x achse
 
 					String vertexName = "" + n + ":" + i;
 					Vertex v = new Vertex(vertexName, n, i);
 					distance.distances.add(v);
 
 					if ((i == iStart) && (n == nStart)) {
 						distance.startIndex = position;
 					}
 					if ((i == iEnd) && (n == nEnd)) {
 						distance.endIndex = position;
 					}
 					position++;
 				}
 			}
 
 			position = 0;
 
 			// Then set the adjacences
 			for (int i = 0; i < grid[0].length; i++) { // i = y achse [][][][][]
 				for (int n = 0; n < grid.length; n++) { // n = x achse
 					ArrayList<Edge> edges = new ArrayList<Edge>();
 
 					if (n + 1 < grid.length) {
 						if (isWalkable(grid[n + 1][i].getState())) {
 							edges.add(new Edge(distance.distances
 									.get(position + 1), 1));
 						}
 					}
 					if (n > 0) {
 						if (isWalkable(grid[n - 1][i].getState())) {
 							edges.add(new Edge(distance.distances
 									.get(position - 1), 1));
 						}
 					}
					if (i + 1 < grid[0].length) {
 						if (isWalkable(grid[n][i + 1].getState())) {
 							edges.add(new Edge(distance.distances.get(position
 									+ nextRow), 1));
 						}
 					}
 					if (i > 0) {
 						if (isWalkable(grid[n][i - 1].getState())) {
 							edges.add(new Edge(distance.distances.get(position
 									- nextRow), 1));
 						}
 					}
 
					Edge[] edgees = edges.toArray(new Edge[edges.size()]);
 					distance.distances.get(position).setAdjacencies(edgees);
 					position++;
 				}
 
 			}
 		} catch (Exception e) {
 			LOG.error(e);
 		}
 		return distance;
 
 	}
 
 	/**
 	 * Walks the distance with the shortest available path (using the dijkstra
 	 * algorithm)
 	 * 
 	 * @param positionWalkTo
 	 */
 	public void setWalk(Position positionWalkTo) {
 		Distance dijkstraObject = getDistances(this.position.getX(),
 				this.position.getY(), positionWalkTo.getX(),
 				positionWalkTo.getY());
 
 		Dijkstra findShortestPath = new Dijkstra();
 
 		List<Vertex> path = findShortestPath.getPath(dijkstraObject.distances,
 				dijkstraObject.distances.get(dijkstraObject.startIndex),
 				dijkstraObject.distances.get(dijkstraObject.endIndex));
 
 		// führt die aktionen in einem eigenen thread aus
 		// der thread macht nach jedem vertex element x eine pause
 		// hier wird der thread effektiv erzeugt und gestartet
 		new Thread(new WalkRunnable(path)).start();
 
 	}
 
 	/**
 	 * tries to move the player in a direction (up, down, left or right). the
 	 * change on the model is only done, if it is a valid move.
 	 * 
 	 * @param direction
 	 */
 	public void setNextField(Direction direction) {
 		Position oldPosition = position;
 
 		switch (direction) {
 		case DOWN:
 			if (isMoveAllowed(direction)) {
 
 				playerPath.add("DOWN");
 
 				// PLAYER MOVES A DIAMOND
 				if ((grid[position.getX()][position.getY() + 1].getState() == FieldState.DIAMOND)) {
 					if ((grid[position.getX()][position.getY() + 2].getState() == FieldState.GOAL)) {
 						grid[position.x][position.y + 2]
 								.setState(FieldState.COMPLETED);
 					} else {
 						grid[position.x][position.y + 2]
 								.setState(FieldState.DIAMOND);
 					}
 					diamondMoveCounter++;
 				} else if ((grid[position.getX()][position.getY() + 1]
 						.getState() == FieldState.COMPLETED)) {
 					// TODO: Player jetzt auf Goal, Diamant aus Ziel
 
 					grid[position.x][position.y + 1]
 							.setState(FieldState.PLAYER_ON_GOAL);
 					grid[position.x][position.y + 2]
 							.setState(FieldState.DIAMOND);
 					diamondMoveCounter++;
 				} else if ((grid[position.getX()][position.getY() + 1]
 						.getState() == FieldState.GOAL)) {
 					grid[position.x][position.y + 1]
 							.setState(FieldState.PLAYER_ON_GOAL);
 				}
 				if (grid[oldPosition.x][oldPosition.y].getState() == FieldState.PLAYER_ON_GOAL) {
 					grid[oldPosition.x][oldPosition.y]
 							.setState(FieldState.GOAL);
 				} else {
 					grid[oldPosition.x][oldPosition.y]
 							.setState(FieldState.EMPTY);
 				}
 				if (grid[position.x][position.y + 1].getState() != FieldState.PLAYER_ON_GOAL) {
 					grid[position.x][position.y + 1]
 							.setState(FieldState.PLAYER);
 				}
 				this.position = new Position(position.getX(),
 						position.getY() + 1);
 			}
 			break;
 		case UP:
 			if (isMoveAllowed(direction)) {
 				playerPath.add("UP");
 				// PLAYER MOVES A DIAMOND
 				if ((grid[position.getX()][position.getY() - 1].getState() == FieldState.DIAMOND)) {
 					if ((grid[position.getX()][position.getY() - 2].getState() == FieldState.GOAL)) {
 						grid[position.x][position.y - 2]
 								.setState(FieldState.COMPLETED);
 					} else {
 						grid[position.x][position.y - 2]
 								.setState(FieldState.DIAMOND);
 					}
 					diamondMoveCounter++;
 				} else if ((grid[position.getX()][position.getY() - 1]
 						.getState() == FieldState.COMPLETED)) {
 					// TODO: Player jetzt auf Goal, Diamant aus Ziel
 
 					grid[position.x][position.y - 1]
 							.setState(FieldState.PLAYER_ON_GOAL);
 					grid[position.x][position.y - 2]
 							.setState(FieldState.DIAMOND);
 					diamondMoveCounter++;
 				} else if ((grid[position.getX()][position.getY() - 1]
 						.getState() == FieldState.GOAL)) {
 					grid[position.x][position.y - 1]
 							.setState(FieldState.PLAYER_ON_GOAL);
 				}
 				if (grid[oldPosition.x][oldPosition.y].getState() == FieldState.PLAYER_ON_GOAL) {
 					grid[oldPosition.x][oldPosition.y]
 							.setState(FieldState.GOAL);
 				} else {
 					grid[oldPosition.x][oldPosition.y]
 							.setState(FieldState.EMPTY);
 				}
 				if (grid[position.x][position.y - 1].getState() != FieldState.PLAYER_ON_GOAL) {
 					grid[position.x][position.y - 1]
 							.setState(FieldState.PLAYER);
 				}
 				this.position = new Position(position.getX(),
 						position.getY() - 1);
 			}
 			break;
 		case LEFT:
 			if (isMoveAllowed(direction)) {
 				playerPath.add("LEFT");
 				// PLAYER MOVES A DIAMOND
 				if ((grid[position.getX() - 1][position.getY()].getState() == FieldState.DIAMOND)) {
 					if ((grid[position.getX() - 2][position.getY()].getState() == FieldState.GOAL)) {
 						grid[position.x - 2][position.y]
 								.setState(FieldState.COMPLETED);
 					} else {
 						grid[position.x - 2][position.y]
 								.setState(FieldState.DIAMOND);
 					}
 					diamondMoveCounter++;
 				} else if ((grid[position.getX() - 1][position.getY()]
 						.getState() == FieldState.COMPLETED)) {
 					// TODO: Player jetzt auf Goal, Diamant aus Ziel
 
 					grid[position.x - 1][position.y]
 							.setState(FieldState.PLAYER_ON_GOAL);
 					grid[position.x - 2][position.y]
 							.setState(FieldState.DIAMOND);
 					diamondMoveCounter++;
 				} else if ((grid[position.getX() - 1][position.getY()]
 						.getState() == FieldState.GOAL)) {
 					grid[position.x - 1][position.y]
 							.setState(FieldState.PLAYER_ON_GOAL);
 				}
 				if (grid[oldPosition.x][oldPosition.y].getState() == FieldState.PLAYER_ON_GOAL) {
 					grid[oldPosition.x][oldPosition.y]
 							.setState(FieldState.GOAL);
 				} else {
 					grid[oldPosition.x][oldPosition.y]
 							.setState(FieldState.EMPTY);
 				}
 				if (grid[position.x - 1][position.y].getState() != FieldState.PLAYER_ON_GOAL) {
 					grid[position.x - 1][position.y]
 							.setState(FieldState.PLAYER);
 				}
 				this.position = new Position(position.getX() - 1,
 						position.getY());
 			}
 			break;
 		case RIGHT:
 			if (isMoveAllowed(direction)) {
 				this.playerPath.add("RIGHT");
 				// PLAYER MOVES A DIAMOND
 				if ((grid[position.getX() + 1][position.getY()].getState() == FieldState.DIAMOND)) {
 					if ((grid[position.getX() + 2][position.getY()].getState() == FieldState.GOAL)) {
 						grid[position.x + 2][position.y]
 								.setState(FieldState.COMPLETED);
 					} else {
 						grid[position.x + 2][position.y]
 								.setState(FieldState.DIAMOND);
 					}
 					diamondMoveCounter++;
 				} else if ((grid[position.getX() + 1][position.getY()]
 						.getState() == FieldState.COMPLETED)) {
 					// TODO: Player jetzt auf Goal, Diamant aus Ziel
 
 					grid[position.x + 1][position.y]
 							.setState(FieldState.PLAYER_ON_GOAL);
 					grid[position.x + 2][position.y]
 							.setState(FieldState.DIAMOND);
 					diamondMoveCounter++;
 				} else if ((grid[position.getX() + 1][position.getY()]
 						.getState() == FieldState.GOAL)) {
 					grid[position.x + 1][position.y]
 							.setState(FieldState.PLAYER_ON_GOAL);
 				}
 				if (grid[oldPosition.x][oldPosition.y].getState() == FieldState.PLAYER_ON_GOAL) {
 					grid[oldPosition.x][oldPosition.y]
 							.setState(FieldState.GOAL);
 				} else {
 					grid[oldPosition.x][oldPosition.y]
 							.setState(FieldState.EMPTY);
 				}
 				if (grid[position.x + 1][position.y].getState() != FieldState.PLAYER_ON_GOAL) {
 					grid[position.x + 1][position.y]
 							.setState(FieldState.PLAYER);
 				}
 				this.position = new Position(position.getX() + 1,
 						position.getY());
 			}
 			break;
 		}
 
 		// check if the position has been changed and a property change needs to
 		// be fired
 		if (oldPosition != position) {
 			checkLevelStatus();
 			moves.add(direction);
 			firePropertyChange(AbstractController.PROPERTY_POSITION,
 					oldPosition, position);
 		}
 	}
 
 	/**
 	 * Checks if the Level is finished
 	 */
 	public void checkLevelStatus() {
 		boolean finished = true;
 		for (int i = 0; i < grid.length; i++) {
 			for (int n = 0; n < grid[0].length; n++) {
 				if ((grid[i][n].getState() == FieldState.GOAL)
 						|| (grid[i][n].getState() == FieldState.PLAYER_ON_GOAL)) {
 					finished = false;
 				}
 			}
 		}
 		if (finished) {
 			firePropertyChange(AbstractController.PROPERTY_LEVEL_SCORE, 0,
 					diamondMoveCounter);
 			firePropertyChange(AbstractController.PROPERTY_LEVEL_STATUS, false,
 					true);
 		}
 	}
 
 	private boolean doesFieldContainType(FieldState state) {
 		for (Field[] arr : grid) {
 			for (Field field : arr) {
 
 				if ((field != null) && (state == field.getState())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean isMoveAllowed(Direction direction) {
 		if (isMoveInsideBorders(direction) == false) {
 			return false;
 		}
 
 		switch (direction) {
 		case DOWN:
 			return (grid[position.getX()][position.getY() + 1].getState() == FieldState.GOAL)
 					|| ((grid[position.getX()][position.getY() + 1].getState() == FieldState.COMPLETED) && (grid[position
 							.getX()][position.getY() + 2].getState() == FieldState.GOAL))
 					|| (((grid[position.getX()][position.getY() + 1].getState() == FieldState.COMPLETED) && (grid[position
 							.getX()][position.getY() + 2].getState() == FieldState.EMPTY)) || (grid[position
 							.getX()].length > position.getY() + 1 && (grid[position
 							.getX()][position.getY() + 1].getState() == FieldState.EMPTY || (grid[position
 							.getX()][position.getY() + 1].getState() == FieldState.DIAMOND && (grid[position
 							.getX()][position.getY() + 2].getState() == FieldState.EMPTY || grid[position
 							.getX()][position.getY() + 2].getState() == FieldState.GOAL)))));
 		case UP:
 			return (grid[position.getX()][position.getY() - 1].getState() == FieldState.GOAL)
 					|| ((grid[position.getX()][position.getY() - 1].getState() == FieldState.COMPLETED) && (grid[position
 							.getX()][position.getY() - 2].getState() == FieldState.GOAL))
 					|| (((grid[position.getX()][position.getY() - 1].getState() == FieldState.COMPLETED) && (grid[position
 							.getX()][position.getY() - 2].getState() == FieldState.EMPTY)) || (0 < position
 							.getY() && (grid[position.getX()][position.getY() - 1]
 							.getState() == FieldState.EMPTY || (grid[position
 							.getX()][position.getY() - 1].getState() == FieldState.DIAMOND && (grid[position
 							.getX()][position.getY() - 2].getState() == FieldState.EMPTY || grid[position
 							.getX()][position.getY() - 2].getState() == FieldState.GOAL)))));
 		case LEFT:
 			return (grid[position.getX() - 1][position.getY()].getState() == FieldState.GOAL)
 					|| ((grid[position.getX() - 1][position.getY()].getState() == FieldState.COMPLETED) && (grid[position
 							.getX() - 2][position.getY()].getState() == FieldState.GOAL))
 					|| ((grid[position.getX() - 1][position.getY()].getState() == FieldState.COMPLETED) && (grid[position
 							.getX() - 2][position.getY()].getState() == FieldState.GOAL))
 					|| (((grid[position.getX() - 1][position.getY()].getState() == FieldState.COMPLETED) && (grid[position
 							.getX() - 2][position.getY()].getState() == FieldState.EMPTY)) || (0 < position
 							.getX() && (grid[position.getX() - 1][position
 							.getY()].getState() == FieldState.EMPTY || (grid[position
 							.getX() - 1][position.getY()].getState() == FieldState.DIAMOND && (grid[position
 							.getX() - 2][position.getY()].getState() == FieldState.EMPTY || grid[position
 							.getX() - 2][position.getY()].getState() == FieldState.GOAL)))));
 		case RIGHT:
 			return (grid[position.getX() + 1][position.getY()].getState() == FieldState.GOAL)
 					|| ((grid[position.getX() + 1][position.getY()].getState() == FieldState.COMPLETED) && (grid[position
 							.getX() + 2][position.getY()].getState() == FieldState.GOAL))
 					|| (((grid[position.getX() + 1][position.getY()].getState() == FieldState.COMPLETED) && (grid[position
 							.getX() + 2][position.getY()].getState() == FieldState.EMPTY)) || (grid.length > position
 							.getX() + 1 && (grid[position.getX() + 1][position
 							.getY()].getState() == FieldState.EMPTY || (grid[position
 							.getX() + 1][position.getY()].getState() == FieldState.DIAMOND && (grid[position
 							.getX() + 2][position.getY()].getState() == FieldState.EMPTY || grid[position
 							.getX() + 2][position.getY()].getState() == FieldState.GOAL)))));
 		}
 		return false;
 	}
 
 	private boolean isMoveInsideBorders(Direction direction) {
 		switch (direction) {
 		case UP:
 			return position.y > 0;
 		case DOWN:
 			return position.y < grid[0].length - 1;
 		case LEFT:
 			return position.x > 0;
 		case RIGHT:
 			return position.x < grid.length - 1;
 		}
 		return false;
 	}
 
 	public List<Direction> getMoves() {
 		return moves;
 	}
 
 	class Distance {
 		ArrayList<Vertex> distances = new ArrayList<Vertex>();
 		int startIndex = -1;
 		int endIndex = -1;
 	}
 
 	class WalkRunnable implements Runnable {
 
 		List<Vertex> path;
 
 		WalkRunnable(List<Vertex> path) {
 			this.path = path;
 		}
 
 		@Override
 		public void run() {
 			for (Vertex x : path) {
 				if (x.getX() > position.getX()) {
 					setNextField(Direction.RIGHT);
 				} else if (x.getY() > position.getY()) {
 					setNextField(Direction.DOWN);
 				} else if (x.getX() < position.getX()) {
 					setNextField(Direction.LEFT);
 				} else if (x.getY() < position.getY()) {
 					setNextField(Direction.UP);
 				}
 
 				try {
 					Thread.sleep(500L);
 				} catch (InterruptedException e) {
 					LOG.error(e);
 				}
 			}
 		}
 
 	}
 
 }
