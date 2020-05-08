 import java.util.LinkedList;
 import java.util.List;
 
 
 /**
  * Wrapper class that holds the row coordinate 
  * and the column coordinate of a board position.
  * 
  * @author Erik
  *
  */
 public class BoardPosition {
 	
 	/** Row coordinate */
 	public final byte row;
 	
 	/** Column coordinate */
 	public final byte col;
 	
 	/**
 	 * Construct a <code>BoardPosition</code> from
 	 * a row coordinate and a column coordinate.
 	 * 
 	 * @param row
 	 * @param column
 	 */
 	public BoardPosition(byte row, byte col) {
 		this.row = row;
 		this.col = col;
 	}
 
 	/**
 	 * @return the neighbors of this position to which a box may be pushed from this position. 
 	 */
 	public List<BoardPosition> getPushableNeighbors() {
 		List<BoardPosition> children = new LinkedList<BoardPosition>();
 		
 		for(Move move : Move.DIRECTIONS) {
 			if(!Board.wallAt(move.stepBack(this))) {
 				BoardPosition child = move.stepFrom(this);
 				
				if(Board.isPushableTo(child)) {
 					children.add(child);
 				}
 			}
 		}
 		
 		return children;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof BoardPosition) {
 			return equals((BoardPosition) obj);
 		}
 
 		return false;
 	}
 	
 	private final boolean equals(BoardPosition bp) {
 		return bp != null && this.row == bp.row && this.col == bp.col; 
 	}
 
 	@Override
 	public BoardPosition clone() {
 		return new BoardPosition(row, col);
 	}
 
 	@Override
 	public String toString() {
 		return "(" + row + ", " + col + ")";
 	}
 }
