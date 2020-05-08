
 public class Move {
 
 	public Move(int row, int col, int change){
 		if(contains(getPossibilities(row, col), change)){
 		      board[row][col] = change;
 		      return true;
 		    }
 		    else
 		      return false;
 		
 		
 	}	
}
