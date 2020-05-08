 package edu.berkeley.gamesman.game;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.PrimitiveValue;
 import edu.berkeley.gamesman.core.TieredGame;
 import edu.berkeley.gamesman.hasher.PerfectConnect4Hash;
 import edu.berkeley.gamesman.util.DependencyResolver;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * Connect 4! Boards are stored in row-major format, bottom row first e.g O XX
  * 
  * is [[xo][x ]]
  * 
  * @author Steven Schlansker
  */
 public class Connect4 extends TieredGame<char[][]> {
 
 	private static final long serialVersionUID = -1234567890123456L;
 
 	final char[] pieces = { 'X', 'O' };
 
 	int piecesToWin = 4;
 
 	static {
 		DependencyResolver.allowHasher(Connect4.class,
 				PerfectConnect4Hash.class);
 	}
 
 	/**
 	 * Connect4 Constructor Creates the hashers we use (does not use the
 	 * command-line specified one, needs special hasher)
 	 * 
 	 * @param conf
 	 *            the configuration
 	 */
 	public Connect4(Configuration conf) {
 		super(conf);
 		piecesToWin = Integer
 				.parseInt(conf.getProperty("connect4.pieces", "4"));
 	}
 
 	@Override
 	public Collection<char[][]> startingPositions() {
 		ArrayList<char[][]> boards = new ArrayList<char[][]>();
 		char[][] startBoard = new char[gameWidth][gameHeight];
 		for (int y = 0; y < gameHeight; y++) {
 			for (int x = 0; x < gameWidth; x++) {
 				startBoard[y][x] = ' ';
 			}
 		}
 		boards.add(startBoard);
 		return boards;
 	}
 
 	@Override
 	public int getDefaultBoardHeight() {
 		return 6;
 	}
 
 	@Override
 	public int getDefaultBoardWidth() {
 		return 7;
 	}
 
 	@Override
 	public PrimitiveValue primitiveValue(char[][] pos) {
 		boolean turnCount = false;
 		char lastTurn;
 		int[] columnHeight = new int[gameWidth];
 		for (int x = 0; x < gameWidth; x++)
 			for (int y = 0; y < gameWidth; y++)
 				if (pos[y][x] == ' ') {
 					columnHeight[x] = y;
 					break;
 				} else
 					turnCount = !turnCount;
 		if (turnCount)
 			lastTurn = 'X';
 		else
 			lastTurn = 'O';
 		for (int x = 0; x < gameWidth; x++) {
			if (pos[columnHeight[x]][x] == lastTurn)
				if (checkLastWin(pos, x, columnHeight[x]))
					return PrimitiveValue.Lose;
 		}
 		return PrimitiveValue.Tie;
 	}
 
 	private boolean checkLastWin(char[][] state, int x, int y) {
 		char turn = state[y][x];
 		int ext;
 		int stopPos;
 
 		// Check horizontal win
 		ext = 1;
 		stopPos = Math.min(x, piecesToWin - ext);
 		for (int i = 0; i < stopPos; i++)
 			if (state[y][x - i] == turn)
 				ext++;
 			else
 				break;
 		stopPos = Math.min(gameWidth - x, piecesToWin - ext);
 		for (int i = 0; i < stopPos; i++)
 			if (state[y][x + i] == turn)
 				ext++;
 			else
 				break;
 		if (ext >= piecesToWin)
 			return true;
 
 		// Check UpRight Win
 		ext = 1;
 		stopPos = Math.min(Math.min(x, y), piecesToWin - ext);
 		for (int i = 0; i < stopPos; i++)
 			if (state[y - i][x - i] == turn)
 				ext++;
 			else
 				break;
 		stopPos = Math.min(Math.min(gameWidth - x, gameHeight - y), piecesToWin
 				- ext);
 		for (int i = 0; i < stopPos; i++)
 			if (state[y + i][x + i] == turn)
 				ext++;
 			else
 				break;
 		if (ext >= piecesToWin)
 			return true;
 
 		// Check DownRight Win
 		ext = 1;
 		stopPos = Math.min(Math.min(x, gameHeight - y), piecesToWin - ext);
 		for (int i = 0; i < stopPos; i++)
 			if (state[y + i][x - i] == turn)
 				ext++;
 			else
 				break;
 		stopPos = Math.min(Math.min(gameWidth - x, y), piecesToWin - ext);
 		for (int i = 0; i < stopPos; i++)
 			if (state[y - i][x + i] == turn)
 				ext++;
 			else
 				break;
 		if (ext >= piecesToWin)
 			return true;
 
 		// Check Vertical Win: Since it's assumed x,y is on top, it's only
 		// necessary to look down, not up
 		if (y >= piecesToWin - 1)
 			for (ext = 1; ext < piecesToWin; ext++)
 				if (state[y - ext][x] != turn)
 					break;
 		if (ext >= piecesToWin)
 			return true;
 
 		return false;
 	}
 
 	@Override
 	public String displayState(char[][] pos) {
 		StringBuilder str = new StringBuilder((pos.length + 1)
 				* (pos[0].length + 1));
 		for (int y = pos[0].length - 1; y >= 0; y--) {
 			str.append("|");
 			for (int x = 0; x < pos.length; x++) {
 				str.append(pos[x][y]);
 			}
 			str.append("|\n");
 		}
 		return str.toString();
 	}
 
 	@Override
 	public char[][] stringToState(String pos) {
 		char[][] board = new char[gameWidth][gameHeight];
 		for (int x = 0; x < gameWidth; x++) {
 			for (int y = 0; y < gameHeight; y++) {
 				board[x][y] = pos.charAt(Util.index(x, y, gameWidth));
 			}
 		}
 		// Util.debug("stringToState yields "+Arrays.deepToString(board));
 		return board;
 	}
 
 	@Override
 	public String stateToString(char[][] pos) {
 		char[] state = new char[gameWidth * gameHeight];
 		for (int x = 0; x < gameWidth; x++) {
 			for (int y = 0; y < gameHeight; y++) {
 				// board[x][y] = pos.charAt(Util.index(x, y, gameWidth));
 				state[Util.index(x, y, gameWidth)] = pos[x][y];
 			}
 		}
 		// Util.debug("stringToState yields "+Arrays.deepToString(board));
 		return new String(state);
 	}
 
 	@Override
 	public Collection<char[][]> validMoves(char[][] pos) {
 		ArrayList<char[][]> nextBoards = new ArrayList<char[][]>();
 
 		char[][] board;
 
 		if (primitiveValue(pos) != PrimitiveValue.Undecided)
 			return nextBoards;
 
 		char nextpiece = nextPiecePlaced(pos);
 
 		for (int x = 0; x < gameWidth; x++) {
 			for (int y = 0; y < gameHeight; y++) {
 				char c = pos[x][y];
 				if (c != 'X' && c != 'O') {
 					board = pos.clone();
 					board[x] = pos[x].clone();
 					board[x][y] = nextpiece;
 					nextBoards.add(board);
 					break;
 				}
 			}
 		}
 
 		return nextBoards;
 	}
 
 	protected char nextPiecePlaced(char[][] pos) {
 		int numx = 0, numo = 0;
 		for (char[] row : pos)
 			for (char piece : row) {
 				if (piece == 'X')
 					numx++;
 				if (piece == 'O')
 					numo++;
 			}
 		if (numx == numo)
 			return 'X';
 		if (numx == numo + 1)
 			return 'O';
 		Util.fatalError("Invalid board: " + Arrays.deepToString(pos));
 		return ' '; // Not reached
 	}
 
 	@Override
 	public String toString() {
 		return "Connect 4 " + gameWidth + "x" + gameHeight + " (" + piecesToWin
 				+ " to win)";
 	}
 
 	@Override
 	public String describe() {
 		return "Connect4|" + gameWidth + "|" + gameHeight + "|" + piecesToWin;
 	}
 
 	@Override
 	public char[] pieces() {
 		return pieces;
 	}
 
 }
