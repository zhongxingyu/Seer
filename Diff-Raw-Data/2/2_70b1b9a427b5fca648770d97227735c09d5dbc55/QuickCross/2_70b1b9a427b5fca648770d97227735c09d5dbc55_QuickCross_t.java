 package edu.berkeley.gamesman.game;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.State;
 import edu.berkeley.gamesman.core.Value;
 //import edu.berkeley.gamesman.hasher.DartboardHasher;
 import edu.berkeley.gamesman.util.Pair;
 
 /**
  * QuickCross by Peter, Raji, Sharmishtha
  */
 public final class QuickCross extends Game<QuickCrossState> implements Undoable<QuickCrossState> {
 	private final int width;
 	private final int height;
 	private final int boardSize;
 	private final int piecesToWin;
 
 	/**
 	 * Default Constructor
 	 * 
 	 * @param conf
 	 *            The Configuration object
 	 */
 	public QuickCross(Configuration conf) {
 		super(conf);
 		width = conf.getInteger("gamesman.game.width", 4);
 		height = conf.getInteger("gamesman.game.height", 4);
 		boardSize = width * height;
 		piecesToWin = conf.getInteger("gamesman.game.pieces", 4);
 	}
 
 	@Override
 	public Collection<QuickCrossState> startingPositions() {
 		ArrayList<QuickCrossState> returnList = new ArrayList<QuickCrossState>(1);
 		QuickCrossState returnState = newState();
 		returnList.add(returnState);
 		return returnList;
 	}
 
 	@Override
 		//given state, returns groupings of names and child states
 	public Collection<Pair<String, QuickCrossState>> validMoves(
 			QuickCrossState pos) {
 		ArrayList<Pair<String, QuickCrossState>> moves = new ArrayList<Pair<String, QuickCrossState>>(
 				pos.numPieces + 2*(boardSize - pos.numPieces));
 		QuickCrossState[] children = new QuickCrossState[pos.numPieces + 2 * (boardSize
 				- pos.numPieces)];
 		
 		String[] childNames = new String[children.length];
 		
 		for (int i = 0; i < children.length; i++) {
 			children[i] = newState();
 		}
 		
 		//this fills up the children array
 		validMoves(pos, children);
 		int moveCount = 0;
 		
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; x++) {
 				//in this case 2 possible moves
 				if (pos.getPiece(x, y) == ' ') {
 					childNames[moveCount++] = "H" + (char) (65 + x) + (y+1);
 					childNames[moveCount++] = "V" + (char) (65 + x) + (y+1);
 				}
 				if (pos.getPiece(x, y) == '-' || pos.getPiece(x, y) == '|'){
 					childNames[moveCount++] = "F" + (char) (65 + x) + (y+1);
 				}
 			}
 		}
 		for (int i = 0; i < children.length; i++) {
 			moves.add(new Pair<String, QuickCrossState>(childNames[i], children[i]));
 		}
 		return moves;
 		
 	}
 
 	@Override
 	public int maxChildren() {
 		return boardSize*2;
 	}
 	
 	public int maxParents() {
 		return boardSize*2;
 	}
 
 	@Override
 	public String stateToString(QuickCrossState pos) {
 		return pos.toString();
 	}
 
 	@Override
 	public String displayState(QuickCrossState pos) {
 		StringBuilder sb = new StringBuilder((width + 1) * 2 * (height + 1));
 		for (int y = height - 1; y >= 0; y--) {
 			sb.append(y + 1);
 			for (int x = 0; x < width; x++) {
 				sb.append(" ");
 				char piece = pos.getPiece(x, y);
 				if (piece == ' ')
 					sb.append(' ');
 				else if (piece == '-' || piece == '|')
 					sb.append(piece);
 				else
 					throw new Error(piece + " is not a valid piece");
 			}
 			sb.append("\n");
 		}
 		sb.append(" ");
 		for (int x = 0; x < width; x++) {
 			sb.append(" ");
 			sb.append((char) ('A' + x));
 		}
 		sb.append("\n");
 		return sb.toString();
 	}
 
 	@Override
 	public QuickCrossState stringToState(String pos) {
 		return new QuickCrossState(width, pos.toCharArray());
 	}
 
 	@Override
 	public String describe() {
 		return width + "x" + height + " QuickCross with " + piecesToWin
 				+ " pieces";
 	}
 
 	@Override
 	public QuickCrossState newState() {
 		return new QuickCrossState(width, height);
 	}
 
 	@Override
 	public int validMoves(QuickCrossState pos, QuickCrossState[] children) {
 
 		int numMoves = 0;
 		for (int i = 0; i < (boardSize); i++){
 			if (pos.getPiece(i) == ' '){
 				children[numMoves].set(pos);
 				children[numMoves].setPiece(i, '-');
 				numMoves++;
 				children[numMoves].set(pos);
 				children[numMoves].setPiece(i, '|');
 				numMoves++;
 			}
 			else if (pos.getPiece(i) == '-'){
 				children[numMoves].set(pos);
 				children[numMoves].setPiece(i, '|');
 				numMoves++;
 			}
 			else if (pos.getPiece(i) == '|'){
 				children[numMoves].set(pos);
 				children[numMoves].setPiece(i, '-');
 				numMoves++;
 			}
 			else throw new Error("cannot generate valid moves from given pos");
 		}
 		return numMoves;
 		
 	}
 
 	public int possibleParents(QuickCrossState pos, QuickCrossState[] parents) {
 		int numParents = 0;
 		for (int i = 0; i < boardSize; i++){
 			if (pos.getPiece(i) == '-'){
 				parents[numParents].set(pos);
 				parents[numParents].setPiece(i, ' ');
 				numParents++;
 				parents[numParents].set(pos);
 				parents[numParents].setPiece(i, '|');
 				numParents++;
 			}
 			else if (pos.getPiece(i) == '|'){
 				parents[numParents].set(pos);
 				parents[numParents].setPiece(i, ' ');
 				numParents++;
 				parents[numParents].set(pos);
 				parents[numParents].setPiece(i, '-');
 				numParents++;
 			}
 			else if (pos.getPiece(i) == ' '){
 				//no move to unmake here.
 			}
 			else throw new Error("cannot generate a parent from given pos");
 			
 		}
 		return numParents;
 	}
 	
 	@Override
 	public Value primitiveValue(QuickCrossState pos) {
 
 		//if last move was 1st player and currently even num moves have happened, 4 in a row is a win for me (the 2nd player)
 		Value WinorLose = (pos.lastMoveOne == pos.evenNumMoves ? Value.WIN : Value.LOSE);
 		
 		char currPiece = '-';
 		//try both pieces
 		for (int i = 0; i<2; i++){
 			//checks for a vertical win
 			for (int y = 0; y < height; y++) {
 				int piecesInRow = 0;
 				for (int x = 0; x < width; x++) {
 					if (pos.getPiece(x, y) == currPiece) {
 						piecesInRow++;
 						if (piecesInRow == piecesToWin)
 							return WinorLose;
 					}
 					else
 						piecesInRow = 0;
 				}
 			}
 			
 			//checks for a horizontal win
 			for (int x = 0; x < width; x++) {
 				int piecesInCol = 0;
 				for (int y = 0; y < height; y++) {
 					if (pos.getPiece(x, y) == currPiece) {
 						piecesInCol++;
 						if (piecesInCol == piecesToWin)
 							return WinorLose;
 					} else
 						piecesInCol = 0;
 				}
 			}
 			//first make sure diagonal possible
 			if (height >= piecesToWin && width >= piecesToWin){
 				
 				
 				//checks for diagonal win /
 				for (int y = 0; y <= height - piecesToWin; y++) {
 					for (int x = 0; x <= width - piecesToWin; x++) {
 						int pieces;
 						for (pieces = 0; pieces < piecesToWin; pieces++) {
 							if (pos.getPiece(x + pieces, y + pieces) != currPiece)
 								break;
 						}
 						if (pieces == piecesToWin)
 							return WinorLose;
 					}
 				}
 				//checks for diagonal win \
 				for (int y = 0; y <= height - piecesToWin; y++) {
 					for (int x = width - 1; x >= piecesToWin - 1; x--) {
 					//for (int x = piecesToWin - 1; x < width; x++) {
 						int pieces;
 						for (pieces = 0; pieces < piecesToWin; pieces++) {
 							if (pos.getPiece(x - pieces, y + pieces) != currPiece)
 								break;
 						}
 						if (pieces == piecesToWin)
 							return WinorLose;
 					}
 				}
 			}
 			currPiece = '|';
 		}
 		return Value.UNDECIDED;
 	}
 
 	@Override
     // trinary hash
 	public long stateToHash(QuickCrossState pos) {
 		//does hash need to store whose turn it is? For now saying no.
 		long retHash = 0;
 		
 		int index = 0;
 		for(int y = 0; y < height; y++){
 			for(int x = 0; x < width; x++){
 				if (pos.getPiece(x,y) == ' '){
 					//no change
 				}
 				else if(pos.getPiece(x,y) == '-'){
 					retHash += Math.pow(3, index);
 				}
 				else if(pos.getPiece(x,y) == '|'){
 					retHash += Math.pow(3, index) * 2;
 				}
 				else throw new Error("Error when hashing, bad piece");
 				index++;
 			}
 		}
 		return retHash;
 	}
 
 	@Override
 	public long numHashes() {
		return (long)Math.pow(3, boardSize);
 	}
 
 	@Override
 	public long recordStates() {
 		return boardSize + 3;
 	}
 
 	
 	@Override
 	public void hashToState(long hash, QuickCrossState s) {
 		s.numPieces = 0;
 		long hashLeft = hash;
 		for (int index = width*height - 1; index >= 0; index--){
 			int y = index / width;
 			int x = index % width;
 			double base = Math.pow(3,index);
 			if (hashLeft < base){
 				s.setPiece(x,y,' ');
 			}
 			else if(hashLeft < base * 2){
 				s.setPiece(x,y,'-');
 				s.numPieces++;
 				hashLeft = (long) (hashLeft - base);
 			}
 			else if(hashLeft >= base*2){
 				s.setPiece(x,y,'|');
 				s.numPieces++;
 				hashLeft = (long) (hashLeft - (base * 2));
 			}
 		}
 	}
 
 	@Override
 	public void longToRecord(QuickCrossState recordState, long record,
 			Record toStore) {
 		if (record == boardSize*2 + 1)
 			toStore.value = Value.IMPOSSIBLE;
 		else if (record == boardSize*2 + 2)
 			toStore.value = Value.UNDECIDED;
 		else if (record == boardSize*2 + 3)
 			toStore.value = Value.DRAW;
 		else if (record >= 0 && record <= boardSize*2) {
 			toStore.value = (record & 1) == 1 ? Value.WIN : Value.LOSE;
 			toStore.remoteness = (int) record;
 		}
 	}
 
 	@Override
 	public long recordToLong(QuickCrossState recordState, Record fromRecord) {
 		if (fromRecord.value == Value.WIN || fromRecord.value == Value.LOSE)
 			return fromRecord.remoteness;
 		else if (fromRecord.value == Value.IMPOSSIBLE)
 			return boardSize*2 + 1;
 		else if (fromRecord.value == Value.UNDECIDED)
 			return boardSize*2 + 2;
 		else if (fromRecord.value == Value.DRAW)
 			return boardSize*2 + 3;
 		else
 			throw new Error("Invalid Value :" + fromRecord.value);
 	}
 
 
 }
 
 //current state of the board
 class QuickCrossState implements State {
 	final char[] board;
 	private final int width;
 	int numPieces = 0;
 	
 	//previous move was made by first player
 	boolean lastMoveOne = false;
 	//even number of moves so far
 	boolean evenNumMoves = true;
 
 	public QuickCrossState(int width, int height) {
 		this.width = width;
 		board = new char[width * height];
 		for (int i = 0; i < board.length; i++) {
 			board[i] = ' ';
 		}
 	}
 
 	public QuickCrossState(int width, char[] charArray) {
 		this.width = width;
 		board = charArray;
 	}
 
 	public void set(State s) {
 		QuickCrossState qcs = (QuickCrossState) s;
 		if (board.length != qcs.board.length)
 			throw new Error("Different Length Boards");
 		int boardLength = board.length;
 		System.arraycopy(qcs.board, 0, board, 0, boardLength);
 		numPieces = qcs.numPieces;
 		lastMoveOne = qcs.lastMoveOne;
 		evenNumMoves = qcs.evenNumMoves;
 	}
 
 	public void setPiece(int x, int y, char piece) {
 		setPiece(y * width + x, piece);
 	}
 
 	public void setPiece(int index, char piece) {
 		if (board[index] == ' '){
 			board[index] = piece;
 			numPieces++;
 			lastMoveOne = !lastMoveOne;
 			evenNumMoves = !evenNumMoves;
 		}
 		else if (board[index] == '-' || board[index] == '|'){
 			board[index] = piece;
 		}
 		else throw new Error("Invalid board when setting piece");
 	}
 	
 
 	public char getPiece(int x, int y) {
 		return getPiece(y * width + x);
 	}
 
 	public char getPiece(int index) {
 		return board[index];
 	}
 
 	public String toString() {
 		return Arrays.toString(board);
 	}
 }
 
