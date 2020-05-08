 /**
  * COMP6721
  * Polarized Ladder Game
  * March 2, 2013 
  */
 package polarizedladder;
 
 import java.awt.Point;
 
 //import java.awt.Point;
 
 public class Board extends GameHeuristics {
 
 	final int BOARD_ROWS = 8;
 	final int BOARD_COLS  = 14;
 	//openList
 	String[][] board;
 	
 	int boardEmptySpaces;
 	
 	public Board() {
 		
 		super();
 	    //check
 		Point p = new Point(1,1);
 		if(!openPoints.isEmpty()) 
 	    	System.out.println("openPoints not empty " + it.next());
 		
 		board = new String[BOARD_ROWS][BOARD_COLS];
 		
 		for (int i = 0; i < BOARD_ROWS; i++) {
 			
 			for (int j = 0; j < BOARD_COLS; j++) {
 				
 				if ( (j == 0) && (i == 0) ) {
 					board[i][j] = " ";
 				}
 				else if ( (i == 0) && (j >= 1) ) {
 					
 					// add x axis legend
 					board[i][j] = Integer.toString(j);
 					board[i][j] = Character.toString(Character.forDigit(j + 9, Character.MAX_RADIX)).toUpperCase();
 					
 				} else if ( (j == 0) && (i >= 1) )  {
 				
 					// add y axis legend
 					board[i][j] = Integer.toString(i);
 				
 				} else if ( (i >= 1) && (j >= 1) ) {
 					
 					// initialize board space
 					board[i][j] = " ";
 				}
 				
 				resetBoard();
 			}
 		}
 	}
 	
 	public void resetBoard() {
 		
 		boardEmptySpaces = 49;
 		
 		for (int i = BOARD_ROWS - 1; i >= 1 ; i--) {
 			
 			int jFrom   = i;
 			int jTo 	= BOARD_COLS - i;
 			
 			for ( ; jFrom <= jTo; jFrom++) {
 				
 				board[i][jFrom] = "_";
 			}
 		}
 	}
 	
 	public void printBoard()
 	{
 		for (int i = BOARD_ROWS - 1; i >= 0; i--) {
 			
 			for (int j = 0; j < BOARD_COLS; j++) {
 				
 				System.out.print( board[i][j] );
 				System.out.print("	");
 			}
 			
 			System.out.println();
 		}
 	}
 	
 	private boolean isLegalPosition(int i, int j) {
 	
 		try
 		{
			if ( board[j][i].equalsIgnoreCase(" ") ) {
 				
 				// illegal position
 				return false;
 				
			} else if ( !board[j][i].equalsIgnoreCase("_") ) {
 				
 				// illegal position
 				return false;
 			
 			} else {
 				
 				// legal position
 				return true;
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException aiob)
 		{
 			return false;
 		}
 	}
 	
 	public boolean setPosition(int i, int j, String mark) {
 		
 		if (isLegalPosition (i, j)) {
 			
			board[j][i] = mark;
 			boardEmptySpaces--;
 			
 			return true;
 		}
 		else {
 			
 			return false;
 		}
 	}
 	
 	public boolean isBoardFull() {
 		
 		if (boardEmptySpaces == 0) {
 			
 			return true;
 		
 		} else {
 			
 			return false;
 		}
 	}
 	
 	public String[][] getState()
 	{
 		return board;
 	}
 	
 	public void setState(String[][] board)
 	{
 		this.board = board;
 	}
 	
 	public void setObjectPosition(Position p){
 		// int i = p.getI();
 		 //int j = p.getJ();
 		 //String mark = p.getMark();
 		 setPosition(p.getI(),p.getJ(), p.getMark());
 	}
 /*	public static void main(String[] args) {
 		
 		Board myBoard = new Board();
 		myBoard.printBoard();
 		
 		System.out.println(myBoard.setPosition(5, 5, "x"));
 		myBoard.printBoard();
 		
 	}*/
 }
