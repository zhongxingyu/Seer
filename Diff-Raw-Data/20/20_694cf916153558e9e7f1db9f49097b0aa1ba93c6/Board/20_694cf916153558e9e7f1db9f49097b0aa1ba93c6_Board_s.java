 package quoridor;
 public class Board implements BoardInterface {
 
 	public Cell board[][] = new Cell [9][9];
 	public Board() {
 		for (int i=0; i<9; i++) {
 		     for (int j=0; j<9; j++) {
 		    	 board[i][j] = new Cell();
 		     }
 		 }
 		
 		for (int i = 0; i < 9; i++) {
 			board[i][0].v = true;
 			board[0][i].h = true;
 		}
 	}
 
 	@Override
 	public int isLegalMove(Board b, Player P, int x, int y) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void placeWall(Board b, Player p, int x, int y) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void movePlayer(Board b, Player p, int x, int y) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void displayBoard() {
 		for (int i=0; i<9; i++) {
 		     for (int j=0; j<9; j++) {
 		    	 if (board[i][j].h == false) {
 		    		 System.out.print(" ");
 		    	 } else {
 		    		 System.out.print(" _");
 		    	 }
		    		 
 		        
 		     }
 		     System.out.println("");
 		     for (int j=0; j<9; j++) {
 		    	 if (board[i][j].v == false) {
 		    		 System.out.print(" ");
 		    	 } else {
 		    		 System.out.print("|");
 		    	 }
 		    	 if (board[i][j].playerNum == 0) {
 		    		 System.out.print("#");
 		    	 } else {
 		    		 System.out.print(board[i][j].playerNum);
 		    	 }
 		    		 
 		        
 		     }	
 			 
	         System.out.println("|");
 		 }
		System.out.println( " _ _ _ _ _ _ _ _ _");
 		
 	}
 
 
 }
