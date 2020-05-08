 package edu.berkeley.gamesman.game;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.Value;
 import edu.berkeley.gamesman.game.util.BitSetBoard;
 import edu.berkeley.gamesman.game.util.TierState;
 import edu.berkeley.gamesman.hasher.ChangedIterator;
 import edu.berkeley.gamesman.hasher.DartboardHasher;
 import edu.berkeley.gamesman.util.Pair;
 
 public class TicTacToe extends TierGame {
 	private int numPieces;
 	private final DartboardHasher dh;
 	private final BitSetBoard bsb;
 	private final int width, height, boardSize;
 	private final int piecesToWin;
 	private final ChangedIterator ci;
 	private final long[] validMoves;
 
 	public TicTacToe(Configuration conf) {
 		super(conf);
 		width = conf.getInteger("gamesman.game.width", 3);
 		height = conf.getInteger("gamesman.game.height", 3);
 		boardSize = width * height;
 		piecesToWin = conf.getInteger("gamesman.game.pieces", 3);
 		dh = new DartboardHasher(boardSize, ' ', 'O', 'X');
 		bsb = new BitSetBoard(height, width);
 		ci = new ChangedIterator();
 		validMoves = new long[boardSize];
 	}
 
 	@Override
 	public void setState(TierState pos) {
 		numPieces = pos.tier;
 		dh.setNums(boardSize - numPieces, numPieces / 2, (numPieces + 1) / 2);
 		dh.setReplacements(' ', numPieces % 2 == 0 ? 'X' : 'O');
 		dh.unhash(pos.hash);
 		setBSB();
 	}
 
 	@Override
 	public Value primitiveValue() {
 		return bsb.xInALine(piecesToWin, numPieces % 2 == 0 ? 'O' : 'X') == 1 ? Value.LOSE
 				: (numPieces == boardSize ? Value.TIE : Value.UNDECIDED);
 	}
 
 	@Override
 	public Collection<Pair<String, TierState>> validMoves() {
 		dh.getChildren(' ', numPieces % 2 == 0 ? 'X' : 'O', validMoves);
 		ArrayList<Pair<String, TierState>> moves = new ArrayList<Pair<String, TierState>>();
 		int i = 0;
 		for (int row = 0; row < height; row++) {
 			for (int col = 0; col < width; col++) {
 				if (validMoves[i] >= 0) {
 					moves.add(new Pair<String, TierState>(Character
 							.toString((char) ('A' + col))
 							+ Integer.toString(row + 1), new TierState(
 							numPieces + 1, validMoves[i])));
 				}
 				i++;
 			}
 		}
 		return moves;
 	}
 
 	@Override
 	public int getTier() {
 		return numPieces;
 	}
 
 	@Override
 	public String stateToString() {
 		return dh.toString();
 	}
 
 	@Override
 	public void setFromString(String pos) {
 		dh.setNumsAndHash(pos.toCharArray());
 		setBSB();
 	}
 
 	private void setBSB() {
 		bsb.clear();
 		int next = 0;
 		for (int row = 0; row < height; row++) {
 			for (int col = 0; col < width; col++) {
 				if (dh.get(next) != ' ')
					bsb.addPiece(row, col, dh.get(next));
				next++;
 			}
 		}
 	}
 
 	@Override
 	public void getState(TierState state) {
 		state.tier = numPieces;
 		state.hash = dh.getHash();
 	}
 
 	@Override
 	public long numHashesForTier(int tier) {
 		numPieces = tier;
 		dh.setNums(boardSize - numPieces, numPieces / 2, (numPieces + 1) / 2);
 		dh.setReplacements(' ', tier % 2 == 0 ? 'X' : 'O');
 		setBSB();
 		return dh.numHashes();
 	}
 
 	@Override
 	public String displayState() {
 		StringBuilder sb = new StringBuilder((width + 1) * 2 * (height + 1));
 		for (int row = height - 1; row >= 0; row--) {
 			sb.append(row + 1);
 			for (int col = 0; col < width; col++) {
 				sb.append(" ");
 				char piece = dh.get(row * width + col);
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
 		numPieces = 0;
 		dh.setNums(boardSize, 0, 0);
 		dh.setReplacements(' ', 'X');
 		bsb.clear();
 	}
 
 	@Override
 	public int numStartingPositions() {
 		return 1;
 	}
 
 	@Override
 	public boolean hasNextHashInTier() {
 		return dh.getHash() < dh.numHashes() - 1;
 	}
 
 	@Override
 	public void nextHashInTier() {
 		dh.next(ci);
 		while (ci.hasNext()) {
 			int next = ci.next();
 			char p = dh.get(next);
 			int row = next / width, col = next % width;
 			bsb.removePiece(row, col);
 			if (p != ' ') {
 				bsb.addPiece(row, col, p);
 			}
 		}
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
 		dh.getChildren(' ', numPieces % 2 == 0 ? 'X' : 'O', validMoves);
 		int moveCount = 0;
 		for (int i = 0; i < boardSize; i++) {
 			if (validMoves[i] >= 0) {
 				moves[moveCount].tier = numPieces + 1;
 				moves[moveCount].hash = validMoves[i];
 				moveCount++;
 			}
 		}
 		return moveCount;
 	}
 
 	@Override
 	public String describe() {
 		return "Tic Tac Toe " + width + "x" + height;
 	}
 
 	@Override
 	public long recordStates() {
 		return boardSize + 2;
 	}
 
 	@Override
 	public void longToRecord(TierState recordState, long record, Record toStore) {
 		if (record == boardSize + 1) {
 			toStore.value = Value.TIE;
 			toStore.remoteness = boardSize - numPieces;
 		} else {
 			toStore.value = record % 2 == 0 ? Value.LOSE : Value.WIN;
 			toStore.remoteness = (int) record;
 		}
 	}
 
 	@Override
 	public long recordToLong(TierState recordState, Record fromRecord) {
 		if (fromRecord.value == Value.TIE)
 			return boardSize + 1;
 		else
 			return fromRecord.remoteness;
 	}
 
 }
