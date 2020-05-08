 package tictactoe;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javafx.beans.Observable;
 import javafx.beans.binding.ObjectBinding;
 import javafx.beans.property.ObjectProperty;
 import javafx.beans.property.ReadOnlyObjectProperty;
 import javafx.beans.property.ReadOnlyObjectWrapper;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 
 /**
  * Represents the state of a Tic-Tac-Toe (Noughts and Crosses) game.
  * @author jdenvir
  *
  */
 public class Game {
 	
 	public static enum SquareState {
 		O, X, EMPTY {
 			@Override
 			public String toString() {
 				return "-" ;
 			}
 		}
 	}
 	
 	public static enum GameStatus {
 		O_WON {
 			@Override
 			public String toString() {
 				return "O won";
 			}
 		},
 		X_WON {
 			@Override
 			public String toString() {
 				return "X won";
 			}
 		},
 		DRAWN {
 			@Override
 			public String toString() {
 				return "Game drawn";
 			}
 		},
 		OPEN {
 			@Override
 			public String toString() {
 				return "Game on";
 			}
 		}
 	}
 		
 	/** 
 	 * Unmodifiable List of rows, columns, and diagonals
 	 */
 	public static final List<List<Location>> LINES = populateLines();
 	
 	private final Player oPlayer ;
 	private final Player xPlayer ;
 	private final ReadOnlyObjectWrapper<Player> currentPlayer ;
 	private final ReadOnlyObjectWrapper<GameStatus> gameStatus ;
 	private final List<List<ReadOnlyObjectWrapper<SquareState>>> board ;
 	
 	/**
 	 * Creates a game between the two players, playing O and X respectively.
 	 * The player playing O will have the first move.
 	 */
 	public Game(Player oPlayer, Player xPlayer) {
 		this.oPlayer = oPlayer ;
 		this.xPlayer = xPlayer ;
 		this.currentPlayer = new ReadOnlyObjectWrapper<>(this, "currentPlayer", oPlayer);
 		this.board = new ArrayList<>(3);
 		for (int i=0; i<3; i++) {
 			List<ReadOnlyObjectWrapper<SquareState>> row = new ArrayList<>(3);
 			for (int j=0; j<3; j++) {
 				row.add(new ReadOnlyObjectWrapper<SquareState>(SquareState.EMPTY));
 			}
 			board.add(row);
 		}
 		this.gameStatus = new ReadOnlyObjectWrapper<GameStatus>(this, "gameStatus", GameStatus.OPEN);
 		gameStatus.addListener(new ChangeListener<GameStatus>() {
 			@Override
 			public void changed(ObservableValue<? extends GameStatus> observable,
 					GameStatus oldValue, GameStatus newValue) {
 				if (gameStatus.get() != GameStatus.OPEN) {
 					currentPlayer.set(null);
 				}
 			}
 			
 		});
 		
 		createGameStatusBinding();
 	}
 
 	private static List<List<Location>> populateLines() {
 		List<List<Location>> lines = new ArrayList<>();
 		// rows
 		for (int rowIndex=0; rowIndex<3; rowIndex++) {
 			List<Location> row = new ArrayList<>();
 			for (int colIndex=0; colIndex<3; colIndex++) {
 				row.add(new Location(colIndex, rowIndex));
 			}
 			lines.add(Collections.unmodifiableList(row));
 		}
 		// columns
 		for (int columnIndex=0; columnIndex<3; columnIndex++) {
 			List<Location> column = new ArrayList<>();
 			for (int rowIndex=0; rowIndex<3; rowIndex++) {
 				column.add(new Location(columnIndex, rowIndex));
 			}
 			lines.add(Collections.unmodifiableList(column));
 		}
 		
 		List<Location> leadDiagonal = new ArrayList<>();
 		List<Location> offDiagonal = new ArrayList<>();
 		for (int index=0; index<3; index++) {
 			leadDiagonal.add(new Location(index, index));
 			offDiagonal.add(new Location(index, 2-index));
 		}
 		lines.add(Collections.unmodifiableList(leadDiagonal));
 		lines.add(Collections.unmodifiableList(offDiagonal));
 		return Collections.unmodifiableList(lines);
 	}
 	
 	/**
 	 * The player playing O.
 	 * @return
 	 */
 	public Player getOPlayer() {
 		return oPlayer ;
 	}
 
 	
 	/**
 	 * The player playing X.
 	 * @return
 	 */
 	public Player getXPlayer() {
 		return xPlayer ;
 	}
 	
 	/**
 	 * The player whose turn it currently is. This will only change via a call to makeMove(...) or reset(Player).
 	 * @return
 	 */
 	public ReadOnlyObjectProperty<Player> currentPlayerProperty() {
 		return currentPlayer.getReadOnlyProperty();
 	}
 	public Player getCurrentPlayer() {
 		return currentPlayer.get();
 	}
 	
 	/**
 	 * The state of the square at the specified location.
 	 * @param location
 	 * @return
 	 */
 	public ReadOnlyObjectProperty<SquareState> squareProperty(Location location) {
 		return board.get(location.getColumn()).get(location.getRow()).getReadOnlyProperty();
 	}
 	
	public SquareState getSquare(Location location) {
 		return board.get(location.getColumn()).get(location.getRow()).get() ;
 	}
 	
 
 	/**
 	 * The state of the square at the location specified by column and row indexes.
 	 * @param column
 	 * @param row
 	 * @return
 	 */
 	public ReadOnlyObjectProperty<SquareState> squareProperty(int column, int row) {
 		return squareProperty(new Location(column, row));
 	}
 	public SquareState getSquare(int column, int row) {
 		return board.get(column).get(row).get();
 	}
 	
 	/**
 	 * The status of the game. The game status only changes via calls to makeMove(...) or reset(...).
 	 * @return
 	 */
 	public ReadOnlyObjectProperty<GameStatus> gameStatusProperty() {
 		return gameStatus.getReadOnlyProperty();
 	}
 	public GameStatus getGameStatus() {
 		return gameStatus.get();
 	}
 	
 	/** 
 	 * Make a move for the specified player in the location specified by the column and row indexes.
 	 * @param player
 	 * @param column
 	 * @param row
 	 * @throws IllegalArgumentException if it is not the specified player's turn, if the column and row indexes do not determine a valid location,
 	 * or if the location is already filled.
 	 */
 	public void makeMove(Player player, int column, int row) {
 		makeMove(player, new Location(column, row));
 	}
 	
 	/**
 	 * Make a move for the specified player in the specified location.
 	 * @param player
 	 * @param location
 	 * @throws IllegalArgumentException if it is not the specified player's turn,
 	 * or if the location is already filled.
 	 */
 	public void makeMove(Player player, Location location) {
 		if (player != currentPlayer.get()) {
 			throw new IllegalArgumentException("It is not "+player+"\'s turn");
 		}
 		final ReadOnlyObjectWrapper<SquareState> squareState = board.get(location.getColumn()).get(location.getRow());
 		if (squareState.get() != SquareState.EMPTY) {
 			throw new IllegalArgumentException(String.format("%s is already occupied with %s", location, squareState.get()));
 		}
 		if (player == xPlayer) {
 			squareState.set(SquareState.X);
 			if (gameStatus.get() == GameStatus.OPEN) {
 				currentPlayer.set(oPlayer);
 			} 
 		} else {
 			squareState.set(SquareState.O);
 			if (gameStatus.get() == GameStatus.OPEN) {
 				currentPlayer.set(xPlayer);
 			}
 		}
 	}
 	
 	/**
 	 * Reset the game and makes the specified player the first player.
 	 * @param firstPlayer
 	 * @throws IllegalArgumentException if the specified player is not a player in this game.
 	 */
 	public void reset(Player firstPlayer) {
 		if (firstPlayer != oPlayer && firstPlayer != xPlayer) {
 			throw new IllegalArgumentException(firstPlayer + " is not a player in this game.");
 		}
 		for (List<ReadOnlyObjectWrapper<SquareState>> row : board) {
 			for (ReadOnlyObjectWrapper<SquareState> square : row ) {
 				square.set(SquareState.EMPTY);
 			}
 		}
 		currentPlayer.set(firstPlayer);
 	}
 	
 	/**
 	 * Resets the game and makes the player playing O the first player.
 	 */
 	public void reset() {
 		reset(oPlayer);
 	}
 	
 	private void createGameStatusBinding() {
 		final List<Observable> allSquares = new ArrayList<>();
 		for (List<ReadOnlyObjectWrapper<SquareState>> row : board) 
 			for (ObjectProperty<SquareState> square : row) 
 				allSquares.add(square);
 		ObjectBinding<GameStatus> gameStatusBinding = new ObjectBinding<GameStatus>() {
 			{ super.bind(allSquares.toArray(new Observable[9])); }
 			@Override
 			public GameStatus computeValue() {
 				
 				for (List<Location> line : LINES) {
 					GameStatus check = checkForWinner(line);
 					if (check != null) {
 						return check ;
 					}
 				}
 
 				// check for empty square, return GameStatus.OPEN if one is found:
 				for (List<ReadOnlyObjectWrapper<SquareState>> row : board) {
 					for (ReadOnlyObjectWrapper<SquareState> square: row) {
 						if (square.get() == SquareState.EMPTY) {
 							return GameStatus.OPEN ;
 						}
 					}
 				}
 				
 				// no winner and full board; game is drawn:
 				
 				return GameStatus.DRAWN ;
 			}
 		};
 		gameStatus.bind(gameStatusBinding);
 	}
 	
 	private GameStatus checkForWinner(List<Location> line) {
 		int oCount = 0 ;
 		int xCount = 0 ;
 		for (Location location : line) {
 			SquareState square = getSquare(location);
 			if (square==SquareState.O) {
 				oCount++ ;
 			} else if (square == SquareState.X){
 				xCount++;
 			}
 		}
 		if (oCount==3) {
 			return GameStatus.O_WON;
 		} else if (xCount==3) {
 			return GameStatus.X_WON;
 		} else return null ;
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		for (List<ReadOnlyObjectWrapper<SquareState>> row : board) {
 			for (ReadOnlyObjectWrapper<SquareState> square : row) {
 				builder.append(square.get());
 			}
 			builder.append("\n");
 		}
 		return builder.toString();
 	}
 }
