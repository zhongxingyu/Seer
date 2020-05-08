 package oving5;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.PriorityQueue;
 
 public class Board {
 
 	public final int k;
 	
 	private Square[][] board;
 	
 	public Board(int k) {
 		this.k = k;
 		board = new Square[k][k];
 		//Initializing the board with squares.
 		for(int y=0; y<k; y++){
 			for(int x=0; x<k; x++){
 				board[y][x] = new Square(x, y);
 			}
 		}
 	}
 	/**
 	 * Returns the number of queens that are attacking the square
 	 * @param s
 	 * @return
 	 */
 	public Square getSquare(int x, int y){
 		return board[y][x];
 	}
 	
 	public int queensAttackingSquare(Square s) {
 		
 		int conflicts = 0;
 
 		int sX = s.getPosX();
 		int sY = s.getPosY();
 		
 		for(int i=0; i<k; i++){
 			//row
 			if(board[sY][i].isQueen()) {
 				if(sX != i)
 					conflicts++;
 			}
 			//column
 			if(board[i][sX].isQueen()) {
 				if(sY != i)
 					conflicts++;
 			}
 			
 			//diagonals
 			if(!(sX+i>k)){ //Right
 				if(!(sY+i>k))//down
 					if(board[sY+i][sX+i].isQueen())
 						conflicts++;
 				if(!(sY-i<0))//up
 					if(board[sY-i][sX+i].isQueen())
 						conflicts++;
 			}
 			if(!(sX-i<0)){//Left
 				if(!(sY-i<0))//up
 					if(board[sY-i][sX-i].isQueen())
 						conflicts++;
 				if(!(sY+i>k))//down
 					if(board[sY+i][sX-i].isQueen())
 						conflicts++;
 			}
 		}
 		return conflicts;
 	}
 	
 	public void updateSquares() {
 		for(Square[] s : board)
 			for(Square ss : s)
 				ss.setNumperOfConflicts(queensAttackingSquare(ss));
 	}
 	
 	
 	public ArrayList<Square> getQueensInConflict() {
 		ArrayList<Square> list = new ArrayList<Square>();
 		
 		for(Square[] s : board)
 			for(Square ss : s)
 				if(ss.getNumperOfConflicts()>0)
 					list.add(ss);
 		return list;
 	}
 	
 	
	public PriorityQueue getOrderedQueueOfSquares() {
 		PriorityQueue<Square> list = new PriorityQueue<Square>();
 		
 		for(Square[] s : board)
 			for(Square ss : s) {
 				list.add(ss);
 			}
 		
 		return list;
 	}
 	
 }
