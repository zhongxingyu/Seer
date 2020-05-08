 
 public class Game {
	public static final int ROWS = 3;
	public static final int COLS = 5;
 	
 	public boolean[] [] board = new boolean[ROWS] [COLS];
 	public boolean[] [] newBoard = new boolean[ROWS] [COLS];
 	
 	public int neighbors(int r, int c) {
 		int total = 0;
 		
 		if (r < ROWS - 1 && board[r + 1][c]) {
 			total++;
 		}
 		
 		if (c < COLS - 1 && board[r][c + 1]) {
 			total++;
 		}
 		
 		if (r > 0 && c > 0 && board[r - 1][c - 1]) {
 			total++;
 			
 		}
 		
 		if (r > 0 && c < COLS - 1 && board [r - 1][c + 1]) {
 			total++;
 			
 		}
 		
 		if (r < ROWS - 1 && c < COLS - 1 && board[r + 1][c + 1]) {
 			total++;
 			
 		}
 		
 		if (r < ROWS - 1 && c > 0 && board[r + 1][c - 1]) {
 			total++;
 			
 		}
 		
 		if (r > 0 && board[r - 1][c]) {
 			total++;
 		}
 		
 		if (c > 0 && board[r][c - 1]) {
 			total++;
 		}
 		
 		return total;	
 	
 	}
 	
 	public boolean cellWillLive(int r, int c) {
		if (board[r][c] && neighbors(r, c) < 2) {
			return false;
 		}
 
		if (board[r][c] && neighbors(r, c) > 3) {
			return false;
		}
		
		if (board[r][c] && neighbors(r, c) >= 2 && neighbors(r, c) <= 3) {
			return true;
		}
		
		if (!board[r][c] && neighbors(r, c) == 3) {
 			return true; 
 		}
 		return false;
 	}
 	
 	public void updateBoard() {
 		for (int r = 0; r < ROWS; r++) {
 			for (int c = 0; c < COLS; c++) {
 			 newBoard[r] [c] = cellWillLive(r, c);
 			}
 		}
 		board = newBoard.clone();
 	}
 	
 	public static void main(String[] args){
 		System.out.println("Starting...");
 		
 		Game g = new Game();
 		
 		g.board[1][2] = true;
 		g.updateBoard();
 		System.out.println(g.neighbors(1, 1));
 	}
 }
