 import java.util.Vector;
 
 /**
  * Class that hold the dynamic elements of a board and
  * provides the methods to work with them.
  * 
  * @author Erik
  *
  */
 public class State  implements Comparable<State> {
 	public final Board board;
 	public final BoardPosition playerPosition;
 	public final Vector<BoardPosition> boxPositions;
 	
 	public final BoardConnectivity connectivity;
 	public final State parent;
 	public final byte lastMove;
 	public final int heuristicValue;
 	
 	public long hash = 0;
 	
 	/**
 	 * Construct a new state from a static board, the players position and
 	 * a list of box positions.
 	 * 
 	 * @param board the static board
 	 * @param playerPosition the player's initial position
 	 * @param boxPositions the boxes' initial positions
 	 */
 	public State(Board board, BoardPosition playerPosition,
 			Vector<BoardPosition> boxPositions) {
 		this.board = board;
 		this.playerPosition = playerPosition;
 		this.boxPositions 	= boxPositions;
 		this.parent 		= null;
 		this.connectivity 	= new BoardConnectivity(this);
 		this.lastMove 		= BoardConnectivity.MOVE_NULL;
 		
 		setHash();
 		this.heuristicValue = Heuristics.calculateHeuristic(this);
 	}
 	
 	/**
 	 * Constructs a new state by pushing a box.
 	 * 
 	 * @param parent the parent state
 	 * @param playerPosition the original location of the box being pushed
 	 * @param move the move made to push the box, from <code>BoardConnectivity</code>
 	 */
 	public State(State parent, BoardPosition oldBoxPosition, byte move) {
		this.parent	 		= parent;
 		this.board 			= parent.board;
 		this.playerPosition = new BoardPosition(oldBoxPosition);
 		this.lastMove 		= move;
 		
 		Vector<BoardPosition> bpv = new Vector<BoardPosition>();
 		for(BoardPosition bp : parent.boxPositions) {
 			if(!bp.equals(oldBoxPosition)) {
 				bpv.add(new BoardPosition(bp));
 			}
 		}
 		this.boxPositions = bpv;
 		
 		byte row = (byte) (oldBoxPosition.row + BoardConnectivity.rowMask[move]);
 		byte col = (byte) (oldBoxPosition.col + BoardConnectivity.colMask[move]);
 		
 		BoardPosition newBox = new BoardPosition(row, col);
 		boxPositions.add(newBox);
 		
 		connectivity = new BoardConnectivity(this);
 		
 		setHash();
 		this.heuristicValue = Heuristics.calculateHeuristic(this);
 	}
 	
 	public void getPushStates(Vector<State> childStates) {
 		childStates.clear();
 		
 		for(BoardPosition boxPos : boxPositions) {
 			byte row = boxPos.row;
 			byte col = boxPos.col;
 			
 			for(byte i=0; i<4; i++) {
 				byte playerInd = (byte) ((i+2) % 4);
 				
 				byte pushedBoxRow = (byte) (row + BoardConnectivity.rowMask[i]);
 				byte pushedBoxCol = (byte) (col + BoardConnectivity.colMask[i]);
 				
 				byte playerRow = (byte) (row + BoardConnectivity.rowMask[playerInd]);
 				byte playerCol = (byte) (col + BoardConnectivity.colMask[playerInd]);
 				
 				boolean playerPosReachable   = connectivity.isReachable(playerRow, playerCol);
 				boolean pushTargetUnOccupied = !isOccupied(pushedBoxRow, pushedBoxCol);
 				boolean targetNotDead		 = !board.deadAt(pushedBoxRow, pushedBoxCol);
 						
 				if(playerPosReachable && pushTargetUnOccupied && targetNotDead) {
 					byte move = i;
 					
 					childStates.add(new State(this, boxPos, move));
 				}
 			}
 		}
 	}
 	
 	public String backtrackSolution() {
 		String result = "";
 		System.out.println(toString());
 		
 		if(lastMove == BoardConnectivity.MOVE_NULL) {
 			return result;
 		}
 		
 		result += BoardConnectivity.moveLetters.charAt(lastMove);
 		
 		byte currRow = (byte) (playerPosition.row - BoardConnectivity.rowMask[lastMove]);
 		byte currCol = (byte) (playerPosition.col - BoardConnectivity.colMask[lastMove]);
 		
 		byte startRow = parent.playerPosition.row;
 		byte startCol = parent.playerPosition.col;
 		
 		BoardPosition endPos = new BoardPosition(currRow, currCol);
 		BoardPosition startPos = new BoardPosition(startRow, startCol);
 		
 		result += parent.connectivity.backtrackPath(endPos, startPos);
 		
 		result += parent.backtrackSolution();
 		
 		return result;
 	}
 	
 	/**
 	 * Calculates the hash value for the current state.
 	 * 
 	 * This should conform to the definition of state equality.
 	 */
 	private void setHash() {
 		for(byte i=0; i<Board.rows+2; i++) {
 			for(byte j=0; j<Board.cols+2; j++) {
 				if(connectivity.isReachable(i,j)) {
 					hash ^= Board.zValues[i][j];
 				}
 			}
 		}
 		for (BoardPosition bp : boxPositions) {
 			hash ^= Board.zValues[bp.row][bp.col];
 		}
 	}
 	
 	public boolean isSolved() {
 		int boxesOnGoal = boxesOnGoals();
 	
 		if (boxesOnGoal == Board.goalPositions.size()) {
 			return true;
 		}
 
 		return false;
 	}
 	
 	public byte boxesOnGoals() {
 		byte sum = 0;
 		for (BoardPosition boxCoordinate : boxPositions) {
 			if (Board.goalAt(boxCoordinate.row, boxCoordinate.col)) {
 				sum++;
 			}
 		}
 
 		return sum;
 	}
 	
 	public boolean isOccupied(byte row, byte col) {
 		return Board.wallAt(row, col) || boxAt(row, col);
 	}
 
 	public boolean boxAt(byte row, byte col) {
 		for (BoardPosition bc : boxPositions) {
 			if (bc.row == row && bc.col == col) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean playerAt(byte row, byte col) {
 		if (playerPosition.row == row && playerPosition.col == col) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof State) {
 			return equals((State) obj);
 		}
 
 		return false;
 	}
 
 	/**
 	 * Two states are considered equal if they have the same connectivity
 	 * and the boxes are at the same locations.
 	 * 
 	 * @param state
 	 * @return
 	 */
 	private boolean equals(State state) {
 		boolean hasSameBoxPos = state.boxPositions.containsAll(this.boxPositions);
 		boolean hasSameConnectivity = state.connectivity.equals(this.connectivity);
 		
 		return hasSameBoxPos && hasSameConnectivity;
 	}
 
 	@Override
 	public int hashCode() {
 		return (int) hash;
 	}
 
 	@Override
 	public String toString() {
 		String result = "";
 		
 		for(byte i=1; i<=Board.rows; i++) {
 			for(byte j=1; j<=Board.cols; j++) {
 				if(boxAt(i, j) && Board.goalAt(i, j)){
 					result += "*";
 				}
 				else if(boxAt(i, j)){
 					result += "$";
 				}
 				else if(playerAt(i, j)) {
 					result += "@";
 				}
 				else if(Board.goalAt(i, j)) {
 					result += ".";
 				}
 				else if(Board.wallAt(i, j)) {
 					result += "#";
 				}
 				else {
 					result += " ";
 				}
 			}
 			result += "\n";
 		}
 		
 		return result;
 	}
 
 	@Override
 	public int compareTo(State other) {
 		if (this.heuristicValue == other.heuristicValue)
             return 0;
        else if (this.heuristicValue > other.heuristicValue)
             return 1;
         else
             return -1;
 	}
 }
