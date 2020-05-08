 package edu.berkeley.gamesman.game;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.Value;
 import edu.berkeley.gamesman.game.util.TierState;
 import edu.berkeley.gamesman.hasher.DartboardHasher;
 import edu.berkeley.gamesman.util.Pair;
 
 public class Reversi extends TierGame {
 	private final int width;
 	private final int height;
 	private final int boardSize;
 	private final Cell[][] board;
 	private final DartboardHasher dbh;
 	private int turn; // 1 for black, 0 for white.
 	private static final char[] pieces = { 'O', 'X' };
 	private final static int BLACK = 1;
 	private final static int WHITE = 0;
 	private final TierState[] children;
 	private int numChildren;
 	private boolean isChildrenValid;
 	private final long[][][] offsetTable;
 	private final char[] oldPosition;
 	private final char[] tempPosition;
 	// offsetTable is a table of offsets based on the tier, or number of pieces
 	// and each element is an array of all possible ways to order the board with
 	// increasing number of white vs black pieces(first element is ways to order
 	// the board with 0 white pieces, second is with 1 white piece, etc...)
 	// tier
 	// 0 [ [1]
 	// 1 [64,128]
 	// 2 [4032,...]
 	// ...
 	// 63 [64,...] ]
 
 	private final String[] stringMoves;
 	private final int[] numPieces = new int[2];
 
 	private class Cell {
 		final int row, col;
 		final int boardNum;
 		Cell[] neighbors = null;
 
 		public Cell(int row, int col, int boardNum) {
 			this.row = row;
 			this.col = col;
 			this.boardNum = boardNum;
 			dbh.set(boardNum, ' ');
 		}
 
 		public char getPiece() {
 			return dbh.get(boardNum);
 		}
 
 		public void setPiece(char p) {
 			if (dbh.get(boardNum) != ' ' && p == ' ')
 				if (dbh.get(boardNum) == 'X')
 					numPieces[BLACK]--;
 				else
 					numPieces[WHITE]--;
 			else if (dbh.get(boardNum) == ' ' && p != ' ')
 				if (p == 'X')
 					numPieces[BLACK]++;
 				else
 					numPieces[WHITE]++;
 			if (!(p == ' ' || p == 'X' || p == 'O'))
 				throw new Error("Bad piece: " + p);
 			dbh.set(boardNum, p);
 		}
 
 		public Cell[] getNeighbors() {
 			if (neighbors == null) {
 				ArrayList<Cell> neighborList = new ArrayList<Cell>(8);
 				for (int row = this.row - 1; row <= this.row + 1; row++) {
 					for (int col = this.col - 1; col <= this.col + 1; col++) {
 						if (row < 0 || row >= height || col < 0 || col >= width)
 							neighborList.add(null);
 						else if (row != this.row || col != this.col)
 							neighborList.add(board[row][col]);
 					}
 				}
 				neighbors = neighborList.toArray(new Cell[neighborList.size()]);
 			}
 			return neighbors;
 		}
 	}
 
 	public Reversi(Configuration conf) {
 		super(conf);
 		width = conf.getInteger("gamesman.game.width", 8);
 		height = conf.getInteger("gamesman.game.height", 8);
 		boardSize = width * height;
 		board = new Cell[height][width];
 		dbh = new DartboardHasher(boardSize, ' ', 'O', 'X');
 		offsetTable = new long[boardSize + 1][2][];
 		// initialize offset table
 		for (int tier = 0; tier <= boardSize; tier++) {
 			long total = 0;
 			for (int turn = 0; turn < 2; turn++) {
 				offsetTable[tier][turn] = new long[tier + 1];
 				for (int offset = 0; offset <= tier; offset++) {
 					dbh.setNums(boardSize - tier, offset, tier - offset);
 					offsetTable[tier][turn][offset] = total;
 					total += dbh.numHashes();
 				}
 			}
 		}
 		for (int row = 0; row < height; row++) {
 			for (int col = 0; col < width; col++) {
 				board[row][col] = new Cell(row, col, row * width + col);
 			}
 		}
 		turn = BLACK;
 		board[height / 2 - 1][width / 2 - 1].setPiece('X');
 		board[height / 2][width / 2 - 1].setPiece('O');
 		board[height / 2 - 1][width / 2].setPiece('O');
 		board[height / 2][width / 2].setPiece('X');
 		isChildrenValid = false;
 		children = newStateArray(maxChildren());
 		stringMoves = new String[maxChildren()]; // only for testing.
 		oldPosition = new char[boardSize];
 		tempPosition = new char[boardSize];
 	}
 
 	@Override
 	public void setState(TierState pos) {
 		unhash(pos.tier, pos.hash);
 		isChildrenValid = false;
 	}
 
 	private void unhash(int tier, long hash) {
 		if (hash >= offsetTable[tier][BLACK][0]) {
 			turn = BLACK;
 		} else {
 			turn = WHITE;
 		}
 		int offset = Arrays.binarySearch(offsetTable[tier][turn], hash);
 		if (offset < 0)
 			offset = -offset - 2;
 		hash -= offsetTable[tier][turn][offset];
 		numPieces[WHITE] = offset;
 		numPieces[BLACK] = tier - offset;
 		dbh.setNums(boardSize - tier, numPieces[WHITE], numPieces[BLACK]);
 		dbh.unhash(hash);
 		char[] charBoard = new char[boardSize];
 		dbh.getCharArray(charBoard);
 		for (int i = 0; i < boardSize; i++) {
 			board[i / width][i % width].setPiece(charBoard[i]);
 		}
 	}
 
 	@Override
 	public Value primitiveValue() {
 		return primitiveValue(false);
 	}
 
 	private Value primitiveValue(boolean strict) {
 		if (!(isChildrenValid))
 			getChildren(strict);
 		if (numChildren == 0) {
 			return numPieces[turn] > numPieces[Math.abs(turn - 1)] ? Value.WIN
 					: Value.LOSE;
 		} else {
 			return Value.UNDECIDED;
 		}
 	}
 
 	@Override
 	public Value strictPrimitiveValue() {
 		return primitiveValue(true);
 	}
 
 	@Override
 	public Collection<Pair<String, TierState>> validMoves() {
 		getChildren(true);
 		TierState[] states = newStateArray(numChildren);
 		validMoves(states);
 		ArrayList<Pair<String, TierState>> moves = new ArrayList<Pair<String, TierState>>();
 		for (int i = 0; i < numChildren; i++) {
 			moves.add(new Pair<String, TierState>(stringMoves[i], states[i]));
 		}
 		return moves;
 	}
 
 	@Override
 	public int getTier() {
 		return numPieces[BLACK] + numPieces[WHITE];
 	}
 
 	@Override
 	public String stateToString() {
 		String answer = "";
 		answer += (this.turn == BLACK ? 'X' : 'O');
 		for (int boardNumber = 0; boardNumber < boardSize; boardNumber++) {
 			answer += dbh.get(boardNumber);
 		}
 		return answer;
 	}
 
 	@Override
 	public void setFromString(String pos) {
 		if (pos.length() != boardSize + 1)
 			throw new Error("Bad String - wrong length: " + pos.length()
 					+ " != " + boardSize);
 		char[] posArray = pos.toCharArray();
 		char turn = posArray[0];
 		if (turn == 'X')
 			this.turn = BLACK;
 		else if (turn == 'O')
 			this.turn = WHITE;
 		else
 			throw new Error("Bad turn");
 		char[] woTurn = Arrays.copyOfRange(posArray, 1, posArray.length - 1);
 		dbh.setNumsAndHash(woTurn);
 		isChildrenValid = false;
 	}
 
 	@Override
 	public void getState(TierState state) {
 		state.tier = getTier();
 		state.hash = getHash();
 	}
 
 	private long getHash() {
 		return offsetTable[getTier()][turn][numPieces[WHITE]] + dbh.getHash();
 	}
 
 	@Override
 	public long numHashesForTier(int tier) {
 		return offsetTable[tier][1][0] * 2;
 	}
 
 	@Override
 	public String displayState() {
 		StringBuilder sb = new StringBuilder((width + 1) * 2 * (height + 1));
 		for (int row = height - 1; row >= 0; row--) {
 			sb.append(row + 1);
 			for (int col = 0; col < width; col++) {
 				sb.append(" ");
 				char piece = dbh.get(row * width + col);
 				if (piece == ' ')
 					sb.append('-');
 				else if (piece == 'X' || piece == 'O')
 					sb.append(piece);
 				else
 					throw new Error(piece + " is not a valid piece");
 			}
 			sb.append("\n");
 		}
 		sb.append(" ");
 		for (int col = 0; col < width; col++) {
 			sb.append(" ");
 			sb.append((char) ('A' + col));
 		}
 		sb.append("\n");
 		return sb.toString();
 	}
 
 	@Override
 	public void setStartingPosition(int n) {
 		numPieces[BLACK] = numPieces[WHITE] = 0;
 		dbh.setNums(boardSize, 0, 0);
 		board[height / 2 - 1][width / 2 - 1].setPiece('X');
 		board[height / 2][width / 2 - 1].setPiece('O');
 		board[height / 2 - 1][width / 2].setPiece('O');
 		board[height / 2][width / 2].setPiece('X');
 
 		turn = BLACK;
 		isChildrenValid = false;
 	}
 
 	@Override
 	public int numStartingPositions() {
 		return 1;
 	}
 
 	@Override
 	public boolean hasNextHashInTier() {
 		long currentHash = dbh.getHash();
 		return (currentHash != numHashesForTier(getTier()) - 1);
 	}
 
 	@Override
 	public void nextHashInTier() {
 		if (dbh.getHash() < dbh.numHashes() - 1)
 			dbh.next();
 		else if (numPieces[BLACK] == 0) {
 			numPieces[BLACK] = numPieces[WHITE];
 			numPieces[WHITE] = 0;
 			dbh.setNums(boardSize - getTier(), 0, getTier());
 			if (turn != 0)
 				throw new RuntimeException("Tier finished");
 			else
 				turn++;
 		} else {
 			dbh.setNums(boardSize - getTier(), ++numPieces[WHITE],
 					--numPieces[BLACK]);
 		}
 		isChildrenValid = false;
 	}
 
 	@Override
 	public int numberOfTiers() {
 		return boardSize + 1;
 	}
 
 	@Override
 	public int maxChildren() {
 		return boardSize;
 	}
 
 	@Override
 	public int validMoves(TierState[] moves) {
 		if (!(isChildrenValid))
 			getChildren(false);
 		for (int i = 0; i < numChildren; i++) {
 			moves[i].set(children[i]);
 		}
 		if (numChildren == 0)
 			throw new Error("No children at this position");
 		return numChildren;
 	}
 
 	private void getChildren(boolean setStringMoves) {
 		dbh.getCharArray(oldPosition);
 		int counter = 0;
 		// looks at every spot on the board
 		for (int row = 0; row < height; row++) {
 			for (int col = 0; col < width; col++) {
 				// only a valid child if there is an empty space there
 				if (board[row][col].getPiece() == ' ') {
 					Cell place = board[row][col];
 					Cell[] neighbors = place.getNeighbors();
 					// looks at each spot around the given spot
 					for (int index = 0; index < 8; index++) {
 						// makes sure the spot is within bounds, then checks if
 						// there is an opposing piece next to it
 						Cell neighbor = neighbors[index];
 						if (neighbor != null
 								&& neighbor.getPiece() == pieces[opposite(turn)]
 								&& isFlippable(place.boardNum, index, false,
 										null)) {
 							if (setStringMoves) {
 								stringMoves[counter] = (char) (col + 'A')
 										+ Integer.toString(row + 1);
 							}
 							System.arraycopy(oldPosition, 0, tempPosition, 0,
 									boardSize);
 							isFlippable(place.boardNum, index, true,
 									tempPosition);
 							int newWhitePieces = count(tempPosition);
 							int nextTier = getTier() + 1;
 							children[counter].tier = nextTier;
 							children[counter].hash = offsetTable[nextTier][opposite(turn)][newWhitePieces]
 									+ dbh.setNumsAndHash(tempPosition);
 							counter++;
 							dbh.setNumsAndHash(oldPosition);
 							break;
 						}
 					}
 				}
 			}
 		}
 		if (setStringMoves && counter == 0) {
 			turn = opposite(turn);
 			getChildren(false);
 			if (numChildren > 0) {
 				stringMoves[0] = "pass";
 				children[0].tier = getTier();
 				children[0].hash = getHash();
 				counter = 1;
 			}
 			turn = opposite(turn);
 		}
 		numChildren = counter;
 		isChildrenValid = true;
 	}
 
 	private int count(char[] pos) {
 		int numWhite = 0;
 		for (int i = 0; i < boardSize; i++) {
 			if (pos[i] == 'O')
 				numWhite++;
 		}
 		return numWhite;
 	}
 
 	private int opposite(int turn) {
 		return turn == 0 ? 1 : 0;
 	}
 
 	private boolean isFlippable(int boardNumber, int direction, boolean flip,
 			char[] tempPosition) {
 		int addRow = 0;
 		int addCol = 0;
 		switch (direction) {
 		case 0:
 			addRow = -1;
 			addCol = -1;
 			break;
 		case 1:
 			addRow = -1;
 			addCol = 0;
 			break;
 		case 2:
 			addRow = -1;
 			addCol = 1;
 			break;
 		case 3:
 			addRow = 0;
 			addCol = -1;
 			break;
 		case 4:
 			addRow = 0;
 			addCol = 1;
 			break;
 		case 5:
 			addRow = 1;
 			addCol = -1;
 			break;
 		case 6:
 			addRow = 1;
 			addCol = 0;
 			break;
 		case 7:
 			addRow = 1;
 			addCol = 1;
 			break;
 		default:
 			throw new Error("Bad direction");
 		}
 		int row = boardNumber / width;
 		int col = boardNumber % height;
 		if (flip) {
 			tempPosition[row * width + col] = (turn == BLACK ? 'X' : 'O');
 		}
 		char piece = 'P';
 		row += addRow;
 		col += addCol;
 		int pieceCounter = 0;
 		while (col >= 0 && col < width && row >= 0 && row < height) {
 			piece = (flip ? tempPosition[row * width + col] : board[row][col]
 					.getPiece());
 			if (piece == ' '
 					|| (pieceCounter == 0 && piece == (turn == BLACK ? 'X'
 							: 'O')))
 				break;
 			else {
 				if (flip)
 					tempPosition[row * width + col] = pieces[turn];
 				if (pieceCounter > 0 && piece == (turn == BLACK ? 'X' : 'O')) {
 					if (flip) {
 						for (int direct = 0; direct < 8; direct++) {
 							if (direct != direction
 									&& isFlippable(boardNumber, direct, false,
 											null))
 								isFlippable(boardNumber, direct, true,
 										tempPosition);
 						}
 					}
 					return true;
 				}
 				row += addRow;
 				col += addCol;
 				if (piece == (turn == BLACK ? 'O' : 'X'))
 					pieceCounter++;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public String describe() {
 		return width + "x" + height + " Reversi";
 	}
 
 	@Override
 	public long recordStates() {
 		if (conf.hasRemoteness) {
 			return (boardSize - 3) * 3;
 		} else
 			return 3;
 	}
 
 	@Override
 	public void longToRecord(TierState recordState, long record, Record toStore) {
 		toStore.remoteness = (int) (record / 3);
 		switch ((int) (record % 3)) {
 		case 0:
 			toStore.value = Value.LOSE;
 			break;
 		case 1:
 			toStore.value = Value.WIN;
 			break;
 		case 2:
 			toStore.value = Value.TIE;
 			break;
 		}
 	}
 
 	@Override
 	public long recordToLong(TierState recordState, Record fromRecord) {
 		int value;
 		switch (fromRecord.value) {
 		case LOSE:
 			value = 0;
 			break;
 		case WIN:
 			value = 1;
 			break;
 		case TIE:
 			value = 2;
 			break;
 		default:
 			throw new Error("Invalid value");
 		}
 		return fromRecord.remoteness * 3 + value;
 	}
 
 	public String[] getStringMoves() {
 		return stringMoves;
 	}
 
 	public static void main(String[] args) {
 		Reversi reversiGame = null;
 		try {
 			reversiGame = (Reversi) new Configuration(args[0]).getGame();
 		} catch (ClassNotFoundException c) {
 			throw new Error(c);
 		}
 		String input;
 		Value prim;
 		reversiGame.setStartingPosition(0);
 		while (true) {
 			System.out.println(reversiGame.displayState());
 			prim = reversiGame.primitiveValue();
 			if (prim.value != 3)
 				break;
 			System.out.println(prim.toString());
 			TierState[] moves = reversiGame.newStateArray(16);
 			int y = reversiGame.validMoves(moves);
 			System.out.print("Valid Moves: ");
 			for (int x = 0; x < y; x++) {
 				System.out.print(reversiGame.getStringMoves()[x] + ",");
 			}
 			System.out.println();
 			System.out.print("Hashes: ");
 			for (int x = 0; x < y; x++) {
 				System.out.print(moves[x].hash + ",");
 			}
 			System.out.println();
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					System.in));
 			try {
 				input = br.readLine();
 			} catch (IOException e) {
 				throw new Error(e);
 			}
 			int index = 0;
 			for (index = 0; index < reversiGame.getStringMoves().length; index++)
 				if (reversiGame.getStringMoves()[index].equals(input))
 					break;
 			if (index == reversiGame.getStringMoves().length) {
 				System.out.println("Invalid Move");
 				break;
 			}
 			reversiGame.setState(moves[index]);
 		}
 		System.out.println("Game Over");
 		if (prim.value == 0)
 			System.out.println((reversiGame.turn == BLACK ? "White wins"
 					: "Black wins"));
 		else if (prim.value == 2)
 			System.out.println("Tie");
 		else if (prim.value == 4)
 			System.out.println((reversiGame.turn == BLACK ? "Black wins"
 					: "White wins"));
 		else
 			System.out.println("Bad result");
 
 	}
 
 	@Override
 	public String toString() {
 		return displayState();
 	}
 }
