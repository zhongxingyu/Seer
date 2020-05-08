 package quoridor;
 public class Board implements BoardInterface {
 
 	final static int numRows = 9;
 	final static int numCols = 9;
 	
 	private Cell board[][] = new Cell [numRows][numCols];
 	
 	public Board(Player[] players) {// Player one, Player two) {
 		for (int i=0; i < numRows; i++) {
 		     for (int j=0; j < numCols; j++) {
 		    	 board[i][j] = new Cell();
 		     }
 		 }
 		
 		for (int i = 1; i < players.length; i++) {
 			board[players[i].getX()][players[i].getY()].playerNum = players[i].getPlayer();
 		}
 		
 		for (int i = 0; i < numRows; i++) {
 			board[i][0].v = true;
 			board[0][i].h = true;
 		}
 		
 	}
 
 	@Override
 	public boolean isLegalMove(Player p, int x, int y) {
 		boolean result = false;
 		if ((board[x][y].playerNum != 0)) {
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
 		} else if (isLegalJumpMove(p,x,y)== true || isLegalDiagonalMove(p,x,y) == true ) {
 			result = true;
 		}
 		if (result == false) {
 			System.out.println("invalid move");
 		}
 		return result;
 	}
 
 	@Override
 	public boolean placeWall(Player p, int x, int y, char d) {
 		boolean r = false;
 		if (isLegalWall(x,y,d) == true) {
 			if (d == 'h') {
 				if (board[x][y].h == false) {
 					board[x][y].h = true;
 					board[x][y+1].h = true;
 					p.useWall();
 					r = true;
 				}
 			} else if (d == 'v') {
 				if (board[x][y].v == false) {
 					board[x][y].v = true;
 					board[x+1][y].v = true;
 					p.useWall();
 					r = true;
 				}
 			}
 		}
 		return r;
 	}
 
 	public boolean removeWall(Player p, int x, int y, char d) {
 		boolean r = false;
 		if (d == 'h') {
 			if (board[x][y].h == true) {
 				board[x][y].h = false;
 				board[x][y+1].h = false;
 				p.unUseWall();
 				r = true;
 			}
 		} else if (d == 'v') {
 			if (board[x][y].v == true) {
 				board[x][y].v = false;
 				board[x+1][y].v = false;
 				p.unUseWall();
 				r = true;
 			}
 		}
 		return r;
 		
 	}
 	
 	@Override
 	public boolean movePlayer(Player p, int x, int y, boolean undo) {
 		boolean r = false;
		if (undo || isLegalMove(p,x,y)) {
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
 		for (int i=0; i<numRows; i++) {
 			System.out.print(" ");
 		     for (int j=0; j<numCols; j++) {
 		    	 if (board[i][j].h == false) {
 		    		 System.out.print("  ");
 		    	 } else {
 		    		 System.out.print(" _");
 		    	 }
 		    	 System.out.print(" ");
 		        
 		     }
 		     System.out.println("");
 		     for (int j=0; j<numCols; j++) {
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
 	public Player checkWinner(Player[] players) {// Player one, Player two) {
 		
 		for (int i = 1; i < players.length; i++) {
 			if (players[i].getX() <= players[i].maxGoalX 
 			 && players[i].getX() >= players[i].minGoalX
 			 && players[i].getY() <= players[i].maxGoalY 
 			 && players[i].getY() >= players[i].minGoalY) {
 				return players[i];
 			}
 		}
 		
 		return null;
 	}
 	
 	//may need to be changed to pass both players, for checking blocked paths
 	//checks that the wall is going to be placed within the borders of the game
 	// and wont collide with other walls
 	private boolean isLegalWall(int x, int y, char d) {
 		//System.out.println("islegalwall - " + x + "," + y + " " + d + "  " + board[x][y].h);
 		boolean r = false;
 		// Check that it's within bounds and there currently isn't a wall there
 		if (d == 'h') {
 			if ((y < numCols-1) &&(board[x][y].h == false) && (board[x][y].h == false)) {
 				r = true;
 				// Check that it doesn't cut off another player's path
 				// -1 to shift left, +1 to shift right
 				if (doesWallBlock(-1, x, y, d) && doesWallBlock(1, x, y+1, d)) {
 					r = false;
 				}
 
 			}			
 		} else if (d == 'v') {
 			if ( (x < numRows-1) && (board[x][y].v == false) && (board[x+1][y].v == false)) {
 				r = true;
 				// Check that it doesn't cut off another player's path
 				// -1 to shift left, +1 to shift right
 				if (doesWallBlock(-1, x, y, d) && doesWallBlock(1, x+1, y, d)) {
 					r = false;
 				}
 			}
 		}
 
 		if (r == false) {
 			System.out.println("illegal wall placement");
 		}
 		
 		
 		return r;
 	}
 	
 	// searchDirection is left or right
 	// d is vertical or horizontal
 	
 	/*
 	 * start from the wall you're putting in
 	 * pretend it's in, and then there are a couple of things:
 	 * check if it forms a complete run from the left to the right
 	 *  and top to bottom if more players
 	 *  defining that as can you move solely in walls and make a path
 	 * if so, check if the player it affects is on the side closest to their wall
 	 * if so fine, if they're on the other side, not fine
 	 */			
 	private boolean doesWallBlock(int searchDirection, int x, int y, char d) {
 		System.out.print("wallblock " + searchDirection + " " +  x + ","+ y );
 		boolean r = false;
 		
 		if (x < 0 || x > numRows || y < 0 || y > numCols) {
 			r = true;//?
 		} else { 
 			//If searchDir = -1 and we hit an x of 0 then we stop, same with +1 and numCols
 			if (searchDirection == -1 && y == 0) {
 				if (d == 'h' || board[x][y].h == true) {
 					r = true;
 				}
 			} else if (searchDirection == 1 && y == numCols-1) {
 				if (d == 'h' || board[x][y].h == true) {
 					r = true;
 				}
 			} else {
 				// If there's a h wall to the left/right go to that cell
 				if (y+searchDirection >= 0 && y + searchDirection < numCols &&  board[x][y+searchDirection].h == true) {
 					r = doesWallBlock(searchDirection, x, y+searchDirection, d);
 				}
 
 				// If searching right we want the wall on the right
 				int wallSide = 0;
 				if (searchDirection == 1) {
 					wallSide = 1;
 				}
 				if (board[x][y+wallSide].v == true) {
 					//if (searchDirectin == -1) {
 					if (x+1 < numRows &&  y+wallSide < numCols &&  board[x+1][y+wallSide].v == true) {
 						r = doesWallBlock(searchDirection, x+1, y+1, 'v');
 					} else if (x-1 >= 0 && y+wallSide < numCols && board[x-1][y+wallSide].v == true) {
 						r= doesWallBlock(searchDirection, x-1, y+1, 'v');
 					}
 				}
 			}
 		}
 		System.out.println (" returning "+ r);
 		return r;
 	}
 	
 	
 	//checks if its legal to jump over a player
 	private boolean isLegalJumpMove(Player p, int x, int y) {
 		boolean r = false;
 		//if the player moves to the left
 		if (p.getX()-x==2 && p.getY() == y) {
 			// if there is a player one to the left
 			if (board[p.getX()-1][y].playerNum != 0) {
 				//return true (is a legal move)
 				r = true;
 			}
 		} else if (p.getX()-x==-2 && p.getY() == y) {
 			// if there is a player one to the left
 			if (board[p.getX()+1][y].playerNum != 0) {
 				//return true (is a legal move)
 				r = true;
 			}
 		// else if the player is moving two up	
 		} else if (p.getY()-y==2 && p.getX() == x) {
 			if (board[x][p.getY()-1].playerNum != 0) {
 				r = true;
 			}
 		} else if (p.getY()-y==-2 && p.getX() == x) {
 			if (board[x][p.getY()+1].playerNum != 0) {
 				r = true;
 			}
 		}
 		return r;
 		
 	}
 	
 	private boolean isLegalDiagonalMove (Player p, int x, int y) {
		System.out.println("calling diagonal");
 		boolean r = false;
 		
 		int curX = p.getX();
 		int curY = p.getY();
 		// Check if we're changing X and Y by one each (valid diagonal)
 		if (Math.abs(curX-x) == 1 && Math.abs(curY-y) == 1) {
			System.out.println("valid diagonal - x = " + x + " y = " + y + " cur x and y = " + curX + " and " + curY);
 			// We need to have a player at either one col different from us (in our jump direction)
 			if (board[curX][y].playerNum != 0) {
 				// If there's a vertical wall on the col on their other side, it's valid
				// If jumping left then new Y is less than it, and we check its wall
				int yWall = y;
				// Else for right we check y+1
				if (y > curY) {
					yWall = y+1;
				}
				if (board[curX][yWall].v == true) {
 					r  = true;
 				}
 			} 
 
 			// Or we need to have a player at one row different from us (in our jump direction)
 			if (board[x][curY].playerNum != 0) {
				int b = x+x-curX;
				System.out.println("it's on the same y, diff x, trying to test" + b + " and y being cury = " + curY);
 				// If there's a horizontal wall on the row on their other side, it's valid
				// If jumping up then new X is less and we just check its wall
				int xWall = x;
				// If jumping down then we check if x+1 has a wall there, cause it's below x
				if (x > curX) {
					xWall = x+1;
				}
				if (board[xWall][curY].h == true){ 
 					r = true;
 				}
 			}
 		
 		}
 		
 		if (r == true) {
 			System.out.println("legal diagonal");
 		}
 		
 		return r;
 	}
 	
 }
