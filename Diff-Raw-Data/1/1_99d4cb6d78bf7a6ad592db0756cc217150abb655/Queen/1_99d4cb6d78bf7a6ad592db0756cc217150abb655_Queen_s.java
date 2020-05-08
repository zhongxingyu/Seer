 
 public class Queen {
 
 	public static void main(String[] args) {
 		
 		SolveQueen puzzle = new SolveQueen();
 		puzzle.printBoard();
 	}
 	
 	// solve using rescursive back tracking 
 	public static class SolveQueen {
 		
 		static final int SIZE = 11;
 		static final int QUEEN = 1;
 		static final int EMPTY = 0;
 		
 		public int[][] board = new int[SIZE][SIZE];
 		public SolveQueen () { };
 		
 		public boolean solve(int col) {
 			
 			if(col == board.length) 
 				return true;
 			
 			//Go thru rows for this column 
 			for(int row=0; row<board.length; row++) {
 				
 				if(!threat(row, col)) {
 					markBoard(row, col);
 					
 					boolean status = solve(col +1);
 					if(status == true){
 						return true;
 					}
 					else {
 						unMarkBoard(row,col);
 					}
 				}
 				
 			}
 			
 			return false; //failed utterly. 
 		}
 		
 		public void printBoard() {
 			printMatrix(board);
 		}
 		
 		//prints n by n matrix    
 		private void printMatrix(int[][] matrix) {
 			int cols = matrix[0].length;
 			int rows = matrix.length;
 			
 			for(int i=0; i<rows; i++){
 				for(int j=0; j<cols; j++) {
 					 System.out.print(matrix[i][j] + " ");
 				}
 				System.out.println("");
 			}
 		}
 		
 		// Determines if queen is being threatened at row, col
 		public boolean threat(int row, int col) {
 			//fix row, check columns
 			for(int i=0; i<board.length; i++) {
 				if(board[row][i] == QUEEN) return true;
 			}
 			
 			//fix col, check rows
 			for(int i=0; i<board.length; i++) {
 				if(board[i][col] == QUEEN) return true;
 			}
 			
 			
 			for(int i=0; i<board.length; i++) {
 				if(row +i < board.length && col+i <board.length)
 					if(board[row+i][col+i] == QUEEN) return true; //+row +col, right down 
 				
 				if(row -i >= 0 && col -i >= 0) {
 					if(board[row-i][col-i] == QUEEN) return true; //-row -col, left up 
 				}
 				
 				if(row +i <board.length && col-i >=0)
 					if(board[row+i][col-i] == QUEEN) return true; //-row -col, left down
 				
 				if(row -i >= 0 && col+i <board.length)
 					if(board[row-i][col+i] == QUEEN) return true; //-row -col, right up
 				
 			}
 			
 
 			return false;
 		} 
 		//1,0 2,1 3,2 
 		public void markBoard(int row, int col) {
 			board[row][col] = QUEEN;
 		}
 		
 		public void unMarkBoard(int row, int col) {
 			board[row][col] = EMPTY;
 		}
 		
 	}
 
 }
 
 
