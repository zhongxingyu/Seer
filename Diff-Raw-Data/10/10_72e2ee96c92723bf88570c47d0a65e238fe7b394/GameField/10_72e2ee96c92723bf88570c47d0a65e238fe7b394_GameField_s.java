 package connectfour.model;
 
 import java.util.Random;
 
 import connectfour.util.observer.IObserverWithArguments;
 
 public class GameField implements Cloneable {
 
 	public static final int DEFAULT_COLUMNS = 7;
 	public static final int DEFAULT_ROWS = 6;
 
 	private Player[][] gameField;
 
 	private Human player;
 	private Player opponend;
 	private Player playerOnTurn;
 	private int modCount = 0;
 
 	private Player playerWon;
 
 	private boolean gameWon;
 
 	public GameField(final IObserverWithArguments observer) {
 		super();
 		gameField = new Player[DEFAULT_ROWS][DEFAULT_COLUMNS];
 		playerWon = null;
 		gameWon = false;
 		player = new Human();
 		player.setName("Hugo");
 		opponend = new Computer(observer);
 		opponend.setName("Boesewicht");
 		initPlayerOnTurn();
 		opponend.setGameField(this);
 
 	}
 
 	private void initPlayerOnTurn() {
 		Random r = new Random();
 		int rInt = r.nextInt();
 		if (rInt % 2 == 0) {
 			playerOnTurn = player;
 		} else {
 			playerOnTurn = opponend;
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		StringBuilder b = new StringBuilder();
 		for (int i = DEFAULT_ROWS - 1; i >= 0; --i) {
 			for (int j = 0; j < DEFAULT_COLUMNS; ++j) {
 				Player actualPlayer = gameField[i][j];
 				if (actualPlayer.equals(player)) {
 					b.append("[o]");
 				} else if (actualPlayer.equals(opponend)) {
 					b.append("[x]");
 				} else {
 					b.append("[-]");
 				}
 			}
 			b.append("\n");
 		}
 
 		return b.toString();
 	}
 
 	// Only for Support. This method sould not be used any more
 	// DEPRECATED!!!!!
 	// Since its bad to get an Array of Objects.
 	public Player[] getPlayers() {
 		Player[] p = new Player[2];
 		p[0] = player;
 		p[1] = opponend;
 		return p;
 	}
 
 	public Player getPlayerOnTurn() {
 		return playerOnTurn;
 	}
 
 	public void setPlayer(final Human p) {
 		player = p;
 
 	}
 
 	public void setOpponend(final Player p) {
 		opponend = p;
 
 	}
 
 	public Player getOpponend() {
 		return opponend;
 	}
 
 	public Human getPlayer() {
 		return player;
 	}
 
 	public int dropCoin(final int column) {
 		int row = 0;
 		row = dropCoin(column, playerOnTurn);
 		modCount++;
 		return row;
 	}
 
 	public int dropCoin(final int column, final Player p) {
 		int row = 0;
 		row = dropCoin(row, column, p);
 		gameWon = hasWon(p);
 		if (gameWon) {
 			playerWon = p;
 		}
 		return row;
 	}
 
 	private int dropCoin(final int row, final int column,
 			final Player p) {
 		int row1 = row;
 
 		if (row >= DEFAULT_COLUMNS - 1) {
 			return row1;
 		}
 
 		if (column >= DEFAULT_COLUMNS) {
 			throw new IllegalArgumentException();
 		}
 
 		if (gameField[row1][column] == null) {
 			gameField[row1][column] = p;
 		} else {
 			row1 = dropCoin(++row1, column, p);
 		}
 
 		return row1;
 	}
 
 	public Player getPlayerAt(final int row, final int column) {
 		return gameField[row][column];
 	}
 
 	public boolean isEmpty() {
 		return modCount == 0;
 
 	}
 
 	public Player getWinner() {
 		return playerWon;
 	}
 
 	private boolean hasWon(final Player playerToCheck) {
 		boolean ret = false;
 		ret |= checkHorizontal(playerToCheck);
 		ret |= checkVertical(playerToCheck);
 		ret |= checkLeftRightDiagonal(playerToCheck);
 		ret |= checkLeftRigthDiagonal(playerToCheck);
 
 		return ret;
 
 	}
 
 	private boolean checkLeftRigthDiagonal(final Player playerToCheck) {
 		for (int row = DEFAULT_ROWS - 1; row >= 3; row--) {
 			for (int col = 0; col < DEFAULT_COLUMNS - 3; col++) {
 				if (gameField[row][col] == playerToCheck
 						&& gameField[row][col] == gameField[row - 1][col + 1]
 						&& gameField[row][col] == gameField[row - 2][col + 2]
 						&& gameField[row][col] == gameField[row - 3][col + 3]) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean checkLeftRightDiagonal(final Player playerToCheck) {
 		for (int row = 0; row < DEFAULT_ROWS - 3; row++) {
 			for (int col = 0; col < DEFAULT_COLUMNS - 3; col++) {
 				if (gameField[row][col] == playerToCheck
 						&& gameField[row][col] == gameField[row + 1][col + 1]
 						&& gameField[row][col] == gameField[row + 2][col + 2]
 						&& gameField[row][col] == gameField[row + 3][col + 3]) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean checkVertical(final Player playerToCheck) {
 		for (int col = 0; col < DEFAULT_COLUMNS; col++) {
 			for (int row = 0; row < DEFAULT_ROWS - 3; row++) {
 				if (gameField[row][col] == playerToCheck
 						&& gameField[row][col] == gameField[row + 1][col]
 						&& gameField[row][col] == gameField[row + 2][col]
 						&& gameField[row][col] == gameField[row + 3][col]) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean checkHorizontal(final Player playerToCheck) {
 		for (int row = 0; row < DEFAULT_ROWS; row++) {
 			for (int col = 0; col < DEFAULT_COLUMNS - 3; col++) {
 				if (gameField[row][col] == playerToCheck
 						&& gameField[row][col] == gameField[row][col + 1]
 						&& gameField[row][col] == gameField[row][col + 2]
 						&& gameField[row][col] == gameField[row][col + 3]) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public void changePlayerTurn() {
 		if (playerOnTurn.equals(opponend)) {
 			playerOnTurn = player;
 		} else if (playerOnTurn.equals(player)) {
 			playerOnTurn = opponend;
 		}
 	}
 
 	public int evaluateScore() {
 
 		Player opponand = playerOnTurn.equals(player) ? opponend
 				: player;
 		int plScore = evaluatePlayerScore(playerOnTurn);
 		int opScore = evaluatePlayerScore(opponand);
 
 		return plScore - opScore;
 
 	}
 
 	private void evaluateVertical(final Player playerToCheck,
 			final int counters[]) {
 		int count = 0;
 		for (int row = 0; row < DEFAULT_ROWS; row++) {
 			count = 0;
 			for (int col = 0; col < DEFAULT_COLUMNS - 1; col++) {
 				if (gameField[row][col] == playerToCheck
 						&& gameField[row][col] == gameField[row][col + 1]) {
 					count++;
 				}
 			}
 			counters[count]++;
 		}
 	}
 
 	private void evaluateHorizontal(final Player playerToCheck,
 			final int counters[]) {
 		int count = 0;
 
 		for (int col = 0; col < DEFAULT_COLUMNS; col++) {
 			count = 0;
 			for (int row = 0; row < DEFAULT_ROWS - 1; row++) {
 				if (gameField[row][col] == playerToCheck
 						&& gameField[row][col] == gameField[row + 1][col]) {
 					count++;
 				}
 			}
 			counters[count]++;
 		}
 	}
 
 	private void evaluateDiagonalFromLeftToRight(
 			final Player playerToCheck, final int counters[]) {
 		int count = 0;
 		for (int row = 0; row < DEFAULT_ROWS - 1; row++) {
 			count = 0;
 			for (int col = 0; col < DEFAULT_COLUMNS - 1; col++) {
 				if (gameField[row][col] == playerToCheck
 						&& gameField[row][col] == gameField[row + 1][col + 1]) {
 					count++;
 				}
 			}
 			counters[count]++;
 		}
 	}
 
 	private void evaluateDiagonalFromRigthToLeft(
 			final Player playerToCheck, final int counters[]) {
 		int count = 0;
 		for (int row = 0; row < DEFAULT_ROWS - 1; row++) {
 			count = 0;
 			for (int col = 0; col < DEFAULT_COLUMNS - 1; col++) {
 				if (gameField[row][col] == playerToCheck
 						&& gameField[row][col] == gameField[row + 1][col + 1]) {
 					count++;
 				}
 			}
 			counters[count]++;
 		}
 	}
 
 	private int evaluatePlayerScore(final Player playerToCheck) {
 
 		int counters[] = new int[10];
 
 		evaluateVertical(playerToCheck, counters);
 		evaluateHorizontal(playerToCheck, counters);
 		evaluateDiagonalFromLeftToRight(playerToCheck, counters);
 		evaluateDiagonalFromRigthToLeft(playerToCheck, counters);
 
 		int result = 0;
 		int scoreMulitplier = 1;
 		for (int counter : counters) {
 			result += counter * scoreMulitplier;
 			scoreMulitplier *= 2;
 		}
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	@Override
 	public GameField clone() throws CloneNotSupportedException {
 		GameField gf = (GameField) super.clone();
 		gf.gameField = this.gameField.clone();
		gf.player = this.player;
		gf.opponend = this.opponend;
		gf.playerOnTurn = this.playerOnTurn;

 		for (int i = 0; i < DEFAULT_ROWS; i++) {
 			gf.gameField[i] = this.gameField[i].clone();
 		}
 
 		return gf;
 	}
 
 }
