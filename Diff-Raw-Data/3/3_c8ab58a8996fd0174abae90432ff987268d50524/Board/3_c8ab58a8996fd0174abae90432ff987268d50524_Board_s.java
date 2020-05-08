 package quoridor;
 public class Board implements BoardInterface {
 
 	private Cell board[][] = new Cell [9][9];
 	public Board(Player one, Player two) {
 		for (int i=0; i<9; i++) {
 		     for (int j=0; j<9; j++) {
 		    	 board[i][j] = new Cell();
 		     }
 		 }
 		
 		board[one.getX()][one.getY()].playerNum = one.getPlayer();
 		board[two.getX()][two.getY()].playerNum = two.getPlayer();
 		
 		for (int i = 0; i < 9; i++) {
 			board[i][0].v = true;
 			board[0][i].h = true;
 		}
 	}
 
 	@Override
 	public boolean isLegalMove(Player p, int x, int y) {
 		boolean result = false;
 		if ((board[x][y].playerNum != 0)) {
 			// checks if the player is jumping two up or down
 		} else if ((Math.abs(p.getX()-x) == 2) && (p.getY() == y)) {
 				result = true;
 				//check if the players is jumping two to the left or right
 		} else if ((Math.abs(p.getY()-y) == 2) && (p.getX()== x)) {
 				result = true;
 				// checks if the player is moving diagonal
 		} else if ((Math.abs(p.getX()-x)==1) && (Math.abs(p.getY()-y)==1)) {
 				result = true;
 		// checks if they are trying to move up and stops if there is a wall
 		} else if ((p.getX()-x) == 1 && (p.getY() == y)) {
 			if (board[p.getX()][y].h == false){
 				result = true;	
 			}
 		//checks if they want to move down and stops if there is a wall
 		} else if ((p.getX()-x) == -1 && (p.getY() == y)) {
 			if (board[x][y].h == false) {
 				result = true;
 			}
 		} else if ((p.getY()-y) == -1 && (p.getX() == x)) {
 			if (board[x][y].v == false) {
 				result = true;
 			}
 		} else if ((p.getY()-y) == 1 && (p.getX() == x)) {
 			if (board[x][p.getY()].v == false) {
 				result = true;
 			}
 		}
 		if (result == false) {
 			System.out.println("invalid move");
 		}
 		return result;
 	}
 
 	@Override
 	public boolean placeWall(Player p, int x, int y,char d) {
 		boolean r = false;
 		if (isLegalWall(x,y,d) == true) {
 			if (d == 'h') {
 				if (board[x][y].h == false) {
 					board[x][y].h = true;
 					p.useWall();
 					r = true;
 				}
 			} else if (d == 'v') {
 				if (board[x][y].v == false) {
 					board[x][y].v = true;
 					p.useWall();
 					r = true;
 				}
 			}
 		}
 		return r;
 	}
 
 	@Override
 	public boolean movePlayer(Player p, int x, int y) {
 		boolean r = false;
 		if (isLegalMove(p,x,y) == true) {
 			int startx = p.getX();
 			int starty = p.getY();
 			board[startx][starty].playerNum = 0;
 			board[x][y].playerNum = p.getPlayer();
 			p.setX(x);
 			p.setY(y);
 			//return true;
 			r = true;
 		}
 		return r;
 	}
 
 	@Override
 	
 	// i is the rows(y)
 	//j is the coloum(x)
 	public void displayBoard() {
 		for (int i=0; i<9; i++) {
 			System.out.print(" ");
 		     for (int j=0; j<9; j++) {
 		    	 if (board[i][j].h == false) {
 		    		 System.out.print("  ");
 		    	 } else {
 		    		 System.out.print(" _");
 		    	 }
 		    	 System.out.print(" ");
 		        
 		     }
 		     System.out.println("");
 		     for (int j=0; j<9; j++) {
 		    	 if (board[i][j].v == false) {
 		    		 System.out.print(" ");
 		    	 } else {
 		    		 System.out.print("|");
 			    	 if (j == 0) {
 			    		 System.out.print(" ");
 			    	 }
 		    	 }
 		    	 if (board[i][j].playerNum == 0) {
 		    		 System.out.print("#");
 		    	 } else {
 		    		 System.out.print(board[i][j].playerNum);
 		    	 }
 		    	 System.out.print(" ");
 		    		 
 		        
 		     }	
 			 
 	         System.out.println("|"+i);
 		 }
 		System.out.println( "  _  _  _  _  _  _  _  _  _");
 		System.out.println( "  a  b  c  d  e  f  g  h  i");
 	}
 
 	@Override
 	public Player checkWinner(Player one, Player two) {
 		if (one.getX() == one.getGoal()) {
 			return one;
 		} else if (two.getX() == two.getGoal()) {
 			return two;
 		}
 		
 		return null;
 	}
 	
 	//may need to be changed to pass both players, for checking blocked paths
 	//checks that the wall is going to be placed within the borders of the game
 	// and wont collide with other walls
 	public boolean isLegalWall(int x, int y, char d) {
 		boolean r = false;
 		if (d == 'h') {
 			if ((x<8) && (board[x][y].h == false) && (board[x][y].h == false)) {
 				r = true;
 			}
 		}
 		if (d == 'v') {
 			if (y<8 && board[x][y].v == false && board[x+1][y].v == false) {
 				r = true;
 			}
 		}
 		
 		
 		return r;
 	}
 	
 }
