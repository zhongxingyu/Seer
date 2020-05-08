 package edu.berkeley.gamesman.parallel.game.connections;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 import org.apache.hadoop.conf.Configuration;
 
 import edu.berkeley.gamesman.game.type.GameRecord;
 import edu.berkeley.gamesman.game.type.GameValue;
 import edu.berkeley.gamesman.hasher.DBHasher;
 import edu.berkeley.gamesman.hasher.counting.CountingState;
 import edu.berkeley.gamesman.hasher.genhasher.GenHasher;
 import edu.berkeley.gamesman.hasher.genhasher.Move;
 import edu.berkeley.gamesman.parallel.FlipRecord;
 import edu.berkeley.gamesman.parallel.game.connections.ConnectionsHasher;
 import edu.berkeley.gamesman.parallel.ranges.RangeTree;
 import edu.berkeley.gamesman.solve.reader.SolveReader;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.qll.QuickLinkedList;
 
 public class Connections extends RangeTree<CountingState, FlipRecord>
 		implements SolveReader<CountingState, FlipRecord> {
 	private Move[] myMoves;
 	private ConnectionsHasher myHasher;
 	private int width, height;
 	private int gameSize;
 	private int suffLen;
 
 	public String getString(CountingState position) {
 		StringBuilder sb = new StringBuilder(gameSize);
 		for (int row = 0; row < height; row++) {
 			for (int col = 0; col < width; col++) {
 				sb.append(charFor(position.get(col * height + row)));
 			}
 		}
 		return sb.toString();
 	}
 
 	private Object charFor(int piece) {
 		switch (piece) {
 		case 0:
 			return ' ';
 		case 1:
 			return 'X';
 		case 2:
 			return 'O';
 		default:
 			return '?';
 		}
 	}
 
 	private static int pieceFor(char c) {
 		switch (c) {
 		case ' ':
 			return 0;
 		case 'X':
 			return 1;
 		case 'O':
 			return 2;
 		default:
 			throw new IllegalArgumentException();
 		}
 	}
 
 	@Override
 	public CountingState getPosition(String board) {
 		assert board.length() == gameSize;
 		int[] pos = new int[gameSize + 1];
 		int pieceCount = 0;
 		int i = 0;
 		for (int row = 0; row < height; row++) {
 			for (int col = 0; col < width; col++) {
 				char c = board.charAt(i++);
 				pos[col * height + row] = pieceFor(c);
 				if (c != ' ')
 					pieceCount++;
 			}
 		}
 		pos[gameSize] = pieceCount;
 		CountingState s = newState();
 		getHasher().set(s, pos);
 		return s;
 	}
 
 	private CountingState newState() {
 		return myHasher.newState();
 	}
 
 	@Override
 	public Collection<Pair<String, CountingState>> getChildren(
 			CountingState position) {
 		ArrayList<Pair<String, CountingState>> children = new ArrayList<Pair<String, CountingState>>();
 		for (int i = 0; i < gameSize; i++) {
 			CountingState s = newState();
 			getHasher().set(s, position);
 			if (playMove(s, i)) {
 				children.add(new Pair<String, CountingState>(Integer.toString(i), s));
 			}
 		}
 		return children;
 	}
 
 	@Override
 	public GameValue getValue(CountingState state) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<CountingState> getStartingPositions() {
 		CountingState result = myHasher.newState(); // newState() calls the
 													// CountingState constructor
 		return Collections.singleton(result);
 	}
 
 	@Override
 	public GenHasher<CountingState> getHasher() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected Move[] getMoves() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected int suffixLength() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public GameRecord getRecord(CountingState position,
 			FlipRecord fetchedRec) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected boolean setNewRecordAndHasChildren(CountingState state,
 			FlipRecord rec) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	protected boolean combineValues(QuickLinkedList<FlipRecord> grList,
 			FlipRecord gr) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	protected void previousPosition(FlipRecord gr, FlipRecord toFill) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected Class<FlipRecord> getGameRecordClass() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void rangeTreeConfigure(Configuration conf) {	
 		width = conf.getInt("gamesman.game.width", 4);
 		height = conf.getInt("gamesman.game.height", 4);
 		gameSize = width * height;
 		if (gameSize + 2 >= Byte.MAX_VALUE)
 			throw new RuntimeException("gameSize is too large");
 		myHasher = new ConnectionsHasher(16); // board size is 4x4
 		ArrayList<Move>[] columnMoveList = new ArrayList[width];
		colMoves = new Move[width][];
 		for (int i = 0; i < width; i++) {
 			columnMoveList[i] = new ArrayList<Move>();
 		}
 		for (int numPieces = 0; numPieces < gameSize; numPieces++) {
 			int turn = getTurn(numPieces);
 			for (int row = 0; row < height; row++) {
 				for (int col = 0; col < width; col++) {
 					int place = getPlace(row, col);
 					if (isBottom(row, col)) {
 						columnMoveList[col].add(new Move(place, 0, turn,
 								gameSize, numPieces, numPieces + 1));
 					} else {
 						columnMoveList[col].add(new Move(place - 1, 1, 1,
 								place, 0, turn, gameSize, numPieces,
 								numPieces + 1));
 						columnMoveList[col].add(new Move(place - 1, 2, 2,
 								place, 0, turn, gameSize, numPieces,
 								numPieces + 1));
 					}
 				}
 			}
 		}
 		ArrayList<Move> allMoves = new ArrayList<Move>();
 		for (int i = 0; i < width; i++) {
 			colMoves[i] = columnMoveList[i].toArray(new Move[columnMoveList[i]
 					.size()]);
 			allMoves.addAll(columnMoveList[i]);
 		}
 		myMoves = allMoves.toArray(new Move[allMoves.size()]);
 		int varianceLength = conf.getInt("gamesman.game.variance.length", 10);
 		suffLen = Math.max(1, gameSize + 1 - varianceLength);
 	}
 
 	private static int getTurn(int numPieces) {
 		return (numPieces % 2) + 1;
 	}
 
 	private int getPlace(int row, int col) {
 		return col * height + row;
 	}
 
 	public boolean playMove(CountingState state, int i) {
 		boolean made = false;
 		Move m = myMoves[i];
 		if (m.matches(state) == -1) {
 			myHasher.makeMove(state, m);
 			made = true;
 		}
 
 		return made;
 	}
 }
