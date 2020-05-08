 package connectfour.controller;
 
 import javax.swing.undo.UndoManager;
 
 import connectfour.model.GameField;
 import connectfour.model.Human;
 import connectfour.model.Player;
 import connectfour.util.observer.Observable;
 
 public final class GameController extends Observable {
 
 	private GameField gameField;
 	private static GameController instance;
 	private boolean bGameHasStarted;
 
 	private final UndoManager undoManager = new UndoManager();
 
 	private GameController() {
 		this.undoManager.discardAllEdits();
 		this.gameField = new GameField();
 		this.bGameHasStarted = false;
 	}
 
 	public void newGame() {
 		this.bGameHasStarted = true;
 		gameField = new GameField();
 		this.notifyObservers();
 	}
 
 	public String getWinner() {
 		Player winner = gameField.getWinner();
 		if (winner != null) {
 			return winner.getName();
 		} else {
 			return "";
 		}
 	}
 
 	public static GameController getInstance() {
 		if (instance == null) {
 			instance = new GameController();
 		}
 
 		return instance;
 	}
 
 	public boolean gameHasStarted() {
 		return this.bGameHasStarted;
 	}
 
 	public boolean dropCoinWithSuccessFeedback(final int col) {
 
 		boolean success = false;
 		if (!userHasWon()) {
 			try {
 
 				GameField previousState = null;
 				try {
 					previousState = gameField.clone();
 				} catch (CloneNotSupportedException e1) {
 				}
 
 				Player p = gameField.getPlayerOnTurn();
 				p.setGameField(gameField);
 
 				int move = p.dropCoin(col);
 
 				int row = gameField.dropCoin(move);
 
 				gameField.changePlayerTurn(); // Change only on success the
 												// players turnk
 				if (row >= GameField.DEFAULT_ROWS) {
 					success = false;
 					useState(previousState);
 
 				}
 				success = true;
 
 				GameField newState = null;
 				try {
 					newState = gameField.clone();
 				} catch (CloneNotSupportedException e) {
 				}
 
 				String undoInfo = String.format(
 						"Undoing %s Player Move", getPlayerOnTurn()
 								.getName());
 				GameFieldEdit edit = new GameFieldEdit(previousState,
 						newState, undoInfo);
 				undoManager.addEdit(edit);
 
 				this.notifyObservers();
 			} catch (IllegalArgumentException e) {
 			}
 		}
 		return success;
 	}
 
 	public Player getPlayerOnTurn() {
 		return gameField.getPlayerOnTurn();
 	}
 
 	public boolean userHasWon() {
 		Player winner = gameField.getWinner();
 
 		if (winner == null) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public String getPlayerNameOnTurn() {
 		Player player = gameField.getPlayerOnTurn();
 		return player.getName();
 
 	}
 
 	/**
 	 * @param gameField
 	 *            the gameField to set
 	 */
 	public void setGameField(final GameField gameField) {
 		this.gameField = gameField;
 	}
 
 	/**
 	 * @return the gameField
 	 */
 	public GameField getGameField() {
 		return this.gameField;
 	}
 
 	public void undoStep() {
 		if (undoManager.canUndo()) {
 			undoManager.undo();
 		} else {
 			return;
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public void redoStep() {
 		if (undoManager.canRedo()) {
 			undoManager.redo();
 		} else {
 			return;
 		}
 	}
 
 	public void setPlayer(final Human p) {
 		gameField.setPlayer(p);
 
 	}
 
 	public void setOpponend(final Player p) {
 		gameField.setOpponend(p);
 	}
 
 	// Only for Support. This method sould not be used any more
 	// DEPRECATED!!!!!
 	// Since its bad to get an Array of Objects.
 	public Player[] getPlayers() {
 		return gameField.getPlayers();
 	}
 
 	public Player getPlayerAt(final int row, final int col) {
 		return gameField.getPlayerAt(row, col);
 	}
 
 	/**
 	 * @return
 	 */
 	public Player getOpponend() {
 		return gameField.getOpponend();
 	}
 
 	/**
 	 * @return
 	 */
 	public Human getPlayer() {
 		return gameField.getPlayer();
 	}
 
 	/**
 	 * @param state
 	 */
 	public void useState(final GameField state) {
 		gameField = state;
 	}
 
 }
