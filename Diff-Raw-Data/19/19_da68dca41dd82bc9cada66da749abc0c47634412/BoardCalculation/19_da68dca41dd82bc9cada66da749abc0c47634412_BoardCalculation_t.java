 package rio;
 
 import java.util.concurrent.Callable;
 
 public class BoardCalculation implements Callable<boolean[]> {
 	
	final BoardState bState;
 	final int rNumber;
 	
 	public BoardCalculation(BoardState boardStatus, int rowNumber) {
		this.bState = boardStatus;
 		this.rNumber = rowNumber;
 	}
 
 	@Override
 	public boolean[] call() throws Exception {
		int n = this.bState.getBoardSize();
 		boolean[] ret = new boolean[n];
 		for(int i = 0; i < n; i++) {
 			if(this.checkUp(i) &&
 			   this.checkUpLeft(i) &&
 			   this.checkUpRight(i))
 				ret[i] = true;
 			else
 				ret[i] = false;
 		}
 		return ret;
 	}
 	
 	private boolean checkUpRight(int colIndex) {
 		for(int i = this.rNumber; i > -1; i--) {
			for(int j = colIndex; colIndex < this.bState.getBoardSize(); j++) {
				if(this.bState.isOccupied(i, j))
 					return false;
 			}
 		}
 		return false;
 	}
 
 	private boolean checkUpLeft(int colIndex) {
 		for(int i = this.rNumber; i > -1; i--) {
 			for(int j = colIndex; colIndex > -1; j--) {
				if(this.bState.isOccupied(i, j))
 					return false;
 			}
 		}
 		return false;
 	}
 
 	private boolean checkUp(int colIndex) {
 		for(int i = 0; i < this.rNumber; i++) {
			if(this.bState.isOccupied(i, colIndex))
 				return false;
 		}
 		return true;
 	}
 	
 	public int getRowNumber() {
 		return this.rNumber;
 	}
 	
 }
