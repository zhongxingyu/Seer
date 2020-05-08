 package oving4;
 
 import java.io.Serializable;
 
 public class Board implements Cloneable {
 
 
 	
 	private final int lengthOfSides;
 	private final int maxNumberOfEggs;
 	private boolean[][] board;
 	private final int maxScore;
 	private final double penaltyModifier;
 
 	public Board(int lengthOfSides, int maxNumberOfEggs, double penaltyModifier){
 		this.lengthOfSides = lengthOfSides;
 		this.maxNumberOfEggs = maxNumberOfEggs;
 		board = new boolean[lengthOfSides][lengthOfSides];
 		this.maxScore = lengthOfSides*maxNumberOfEggs;
 		this.penaltyModifier = penaltyModifier;
 		
 		
 	}
 	public void setEgg(int width, int height, boolean b){
 		board[height][width] = b;
 	}
 	public boolean isEgg(int width, int height){
 		return board[height][width];
 	}
 	public double evaluate() {
 		return ((double)getNumberOfEggs()-(double)calculateNumberOfFaults()*penaltyModifier)/(double)maxScore;
 	}
 	private int getNumberOfEggs() {
 		int n = 0;
 		for (int i = 0; i < board.length; i++) {
 			for (int j = 0; j < board.length; j++) {
 				if (board[i][j]) {
 					n++;
 				}
 			}
 		}
 		return n;
 	}
 	private int calculateNumberOfFaults(){
 		int numberOfFaults = 0;
 		for (int i = 0; i < board.length; i++) {
 			if (!isColumnLegal(i))
 				numberOfFaults++;
 			if (!isRowLegal(i))
 				numberOfFaults++;
 			if (!isDiagonalLegalRightToLeft(0, i))
 				numberOfFaults++;
 			if (i > 0 && !isDiagonalLegalRightToLeft(i, board[0].length-1))
 				numberOfFaults++;
 			if (!isDiagonalLegalLeftToRight(0, i))
 				numberOfFaults++;
 			if (i > 0 && !isDiagonalLegalLeftToRight(i, 0));
 		}
 		return numberOfFaults;
 	}
 	public boolean isColumnLegal(int column){
 		int count = 0;
 		for (int i = 0; i < board.length; i++) {
 			if (board[i][column]) {
 				count++;
 			}
 		}
 		if (count > maxNumberOfEggs) {
 			return false;
 		}
 		return true;
 	}
 	public boolean isRowLegal(int row){
 		int count = 0;
 		for (int i = 0; i < board[row].length; i++) {
 			if (board[row][i]) {
 				count++;
 			}
 		}
 		if (count > maxNumberOfEggs) {
 			return false;
 		}
 		return true;
 	}
 	private boolean isDiagonalLegalLeftToRight(int height, int width){
 		int count = 0;
 		if(height == 0){
 			for (int i = 0; i < board.length-width; i++) {
 				if(board[i][width+i])
 					count++;
 			}
 		}else{
 			for (int i = 0; i < board.length-height; i++) {
 				if (board[height+i][i]) {
 					count++;
 				}
 			}
 		}
 		return true;
 	}
 	private boolean isDiagonalLegalRightToLeft(int height, int width){
 		int count = 0;
 		if(height == 0){
 			for (int i = 0; i <= width; i++) {
 				if(board[i][width-i])
 					count++;
 			}
 		}else{
 			for (int i = 0; i < board.length-height; i++) {
 				if (board[height+i][width-i]) {
 					count++;
 				}
 			}
 		}
 		if (count > 2)
 			return false;
 		return true;
 	}
	public static void main(String[] args) {
 		Board b = new Board(5, 2, 2);
 		b.setEgg(3, 0, true);
 		b.setEgg(4, 1, true);
 		b.setEgg(0, 1, true);
 		b.setEgg(0, 3, true);
 		System.out.println(b.evaluate());
 	}
 //		int ii = width;
 //		int uu = height;
 //		if(height > width) {
 //			ii = height;
 //			uu = width;
 //		}
 //		
 //		for(int u=0; u<=ii;u++) {
 //			count = 0;
 //			for(int j=0+u, i=0; i+u<height; i++, j++) {
 //				if(board[i][j+u])
 //					count++;
 //				if(count>2) {
 //					return false;
 //				}
 //			}

 	
 	
 	public void flipEgg(int x, int y) {
 		board[y][x] = !board[y][x];
 	}
 	
 	public boolean[][] getArray(){
 		return board;
 	}
 	
 	@Override
 	protected Board clone() throws CloneNotSupportedException {
 		return (Board) super.clone();
 	}
 }
