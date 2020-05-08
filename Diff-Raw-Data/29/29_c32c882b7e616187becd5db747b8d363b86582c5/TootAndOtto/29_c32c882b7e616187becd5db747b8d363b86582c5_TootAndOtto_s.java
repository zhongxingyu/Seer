 package edu.berkeley.gamesman.parallel.game.tootandotto;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 import org.apache.hadoop.conf.Configuration;
 
 import edu.berkeley.gamesman.game.type.GameValue;
 import edu.berkeley.gamesman.hasher.genhasher.GenHasher;
 import edu.berkeley.gamesman.hasher.genhasher.Move;
 import edu.berkeley.gamesman.parallel.ranges.RangeTree;
 import edu.berkeley.gamesman.solve.reader.SolveReader;
 import edu.berkeley.gamesman.util.Pair;
 
 public class TootAndOtto extends RangeTree<TOState> implements
 		SolveReader<TOState> {
 	private Move[] myMoves;
 	private Move[][] colMoves;
 	private TOHasher myHasher;
 	private int width, height;
 	private int gameSize;
 	private int suffLen;
 	private int maxPieces;
 
 	@Override
 	public void rangeTreeConfigure(Configuration conf) {
 		width = conf.getInt("gamesman.game.width", 5);
 		height = conf.getInt("gamesman.game.height", 4);
 		gameSize = width * height;
 		maxPieces = conf.getInt("gamesman.game.maxPieces", 6);
 		myHasher = new TOHasher(width, height, maxPieces);
 		int varianceLength = conf.getInt("gamesman.game.variance.length", 10);
 		suffLen = Math.max(5, gameSize + 5 - varianceLength);
 
 		ArrayList<Move>[] columnMoveList = new ArrayList[width];
 		colMoves = new Move[width][];
 		for (int i = 0; i < width; i++) {
 			columnMoveList[i] = new ArrayList<Move>();
 		}
 		generateMoves(0, 0, 0, 0, 0, columnMoveList);
 		ArrayList<Move> allMoves = new ArrayList<Move>();
 		for (int i = 0; i < width; i++) {
 			colMoves[i] = columnMoveList[i].toArray(new Move[columnMoveList[i]
 					.size()]);
 			allMoves.addAll(columnMoveList[i]);
 		}
 		myMoves = allMoves.toArray(new Move[allMoves.size()]);
 
 	}
 	// TODO: does this work now? and how can it be cleaned up more.
 	private void generateMoves(int numPieces, int player1T, int player1O,
 			int player2T, int player2O, ArrayList<Move>[] columnMoveList) {
 		int turn = getTurn(numPieces);
 		int TIndex = gameSize + 3;
 		int OIndex = gameSize + 2;
 		int numT = player1T;
 		int numO = player1O;
 		int numPiecesIndex = gameSize + 4;
 		if (turn == 2) {
 			TIndex -= 2;
 			OIndex -= 2;
 			numT = player2T;
 			numO = player2O;
 		}
 
 		for (int row = 0; row < height; row++) {
 			for (int col = 0; col < width; col++) {
 				int place = getPlace(row, col);
 				if (numT < maxPieces) {
 					if (isBottom(row, col)) {
						columnMoveList[col].add(new Move(place - 1, 1, 1,
								place, 0, 1, TIndex, numT, numT + 1,
								numPiecesIndex, numPieces, numPieces + 1));
 					} else {
 						columnMoveList[col].add(new Move(place - 1, 1, 1,
 								place, 0, 1, TIndex, numT, numT + 1,
 								numPiecesIndex, numPieces, numPieces + 1));
 						columnMoveList[col].add(new Move(place - 1, 2, 2,
 								place, 0, 1, TIndex, numT, numT + 1,
 								numPiecesIndex, numPieces, numPieces + 1));
 					}
 					if (turn == 1) {
 						generateMoves(numPieces + 1, player1T + 1, player1O,
 								player2T, player2O, columnMoveList);
 					} else {
 						generateMoves(numPieces + 1, player1T, player1O,
 								player2T + 1, player2O, columnMoveList);
 					}
 				}
 				if (numO < maxPieces) {
 					if (isBottom(row, col)) {
						columnMoveList[col].add(new Move(place - 1, 2, 2,
								place, 0, 2, OIndex, numO, numO + 1,
								numPiecesIndex, numPieces, numPieces + 1));
 					} else {
 						columnMoveList[col].add(new Move(place - 1, 1, 1,
 								place, 0, 2, OIndex, numO, numO + 1,
 								numPiecesIndex, numPieces, numPieces + 1));
 						columnMoveList[col].add(new Move(place - 1, 2, 2,
 								place, 0, 2, OIndex, numO, numO + 1,
 								numPiecesIndex, numPieces, numPieces + 1));
 					}
 				}
 				if (turn == 1) {
 					generateMoves(numPieces + 1, player1T, player1O + 1,
 							player2T, player2O, columnMoveList);
 				} else {
 					generateMoves(numPieces + 1, player1T, player1O, player2T,
 							player2O + 1, columnMoveList);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Determine if we are at the bottom of the column
 	 * 
 	 * @param row
 	 * @param col
 	 * @return true if the position (row, col) is at the bottom of a column
 	 */
 	private boolean isBottom(int row, int col) {
 		return row == 0;
 	}
 
 	/**
 	 * Translate the coordinate on the board into the nth place on the board in
 	 * column major order
 	 * 
 	 * @param row
 	 * @param col
 	 * @return
 	 */
 	private int getPlace(int row, int col) {
 		return col * height + row;
 	}
 
 	/**
 	 * Determine whose turn it is, given the number of pieces on the board
 	 * 
 	 * @param numPieces
 	 * @return 1 if it is the first player's turn, 2 if it is the second
 	 *         player's turn.
 	 */
 	private int getTurn(int numPieces) {
 		return (numPieces % 2) + 1;
 	}
 
 	@Override
 	public TOState getPosition(String board) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<Pair<String, TOState>> getChildren(TOState position) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getString(TOState position) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public GameValue getValue(TOState state) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<TOState> getStartingPositions() {
 		TOState result = myHasher.newState();
 		return Collections.singleton(result);
 	}
 
 	@Override
 	public TOHasher getHasher() {
 		return myHasher;
 	}
 
 	@Override
 	protected Move[] getMoves() {
 		return myMoves;
 	}
 
 	@Override
 	protected int suffixLength() {
 		return suffLen;
 	}
 
 	@Override
 	public int outputSuffixLength() {
 		int innerVarLen = getConf().getInt(
 				"gamesman.game.output.variance.length", gameSize);
 		// gameSize default ensures outputSuffixLength == suffixLength
 		return Math.max(gameSize + 5 - innerVarLen, suffLen);
 	}
 
 }
