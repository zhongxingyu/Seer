 package quoridor;
 
 import java.util.*;
 public class Board implements BoardInterface {
 
 	final static int numRows = 9;
 	final static int numCols = 9;
 	final static int numDirections = 4;
 	
 	public Cell board[][] = new Cell [numRows][numCols];
 	
 	// The printer class to interface with the gui
 	Printer print = new Printer();
 	
 	public Board(Player[] players) {
 		for (int i = 0; i < numRows; i++) {
 		     for (int j = 0; j < numCols; j++) {
 		    	 board[i][j] = new Cell();
 		     }
 		 }
 
 		// We number our players from 1, as that's how they appear in the display
 		for (int i = 1; i < players.length; i++) {
 			board[players[i].getX()][players[i].getY()].playerNum = players[i].getPlayer();
 		}
 		
 		// Set the initial edges to have walls, so as to make it easy to say it's illegal to place one there
 		for (int i = 0; i < numRows; i++) {
 			board[i][0].v = true;
 			board[0][i].h = true;
 		}
 	
 	}
 
 	@Override
 	public boolean isLegalMove(Player p, int x, int y) {
 		boolean result = false;
 		// The move is false if it's out of the bounds of the board
 		if (x < numRows && y < numCols && x >= 0 && y >= 0) {
 			
 			// If there's a player in the cell we're moving to then it's invalid
 			if (board[x][y].playerNum == 0) {
 				 // 0 = p.getX()-x, 1 = p.getY()-y, 2 = checkX, 3 = checkY, 4 = wall Direction  
 				 int[][] checks = {{ 1,  0, p.getX(), y,        1},
 				 				   {-1,  0, x,        y,        1},
 				 				   { 0, -1, x,        y,        0},
 				 				   { 0,  1, x,        p.getY(), 0}};
 				 
 				// For each direction, if there's no wall where they're moving it's valid
 			 	for (int i = 0; (i < numDirections) && (result == false); i++) {
 			 		if ((p.getX()-x) == checks[i][0] && (p.getY()-y) == checks[i][1]) {
 			 			// If wallDir is 1 we check h, else v
 			 			// If there's a wall result is false, else it's true (hence the not)
 			 			result = checks[i][4] == 1 ? !board[checks[i][2]][checks[i][3]].h : !board[checks[i][2]][checks[i][3]].v;
 			 		}  
 			 	}
 			 	
 			 	// If they didn't move straight up/down/left/right, check if it's a jump or diagonal
 			 	if (result == false) {
 					if (isLegalJumpMove(p,x,y)== true || isLegalDiagonalMove(p,x,y) == true ) {
 						result = true;
 					}
 			 	}
 			 	
 			} 
 
 			// If it's an illegal move, we print an error message to the user
 			if (result == false) {
 				print.printMessage("Invalid move;\nValid moves are to an unoccupied cell up/down/left/right\nOr jumping over a player (diagonally in the case of a wall)");
 				
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public boolean placeWall(Player[] players, int current, int x, int y, char d) {
 		boolean r = false;
 		
 		// If it's a legal wall placement then place the wall
 		if (isLegalWall(players,x,y,d) == true) {
 			r = true;
 			players[current].useWall();
 			if (d == 'h') {
 					board[x][y].h = true;
 					board[x][y+1].h = true;
 			} else if (d == 'v') {
 					board[x-1][y+1].v = true;
 					board[x][y+1].v = true;
 			}
 		}
 		return r;
 	}
 
 	// Written for use with undo, this removes a wall and changes the playcount back
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
 				board[x-1][y].v = false;
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
 			 
 	         System.out.println("|"+(i+1));
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
 	
 	// Checks that the wall is going to be placed within the borders of the game
 	// And won't collide with other walls
 	public boolean isLegalWall(Player[] players,int  x, int y, char d) {
 		boolean r = false;
 		
 		// Don't bother checking further if it's out of bounds
 		if (x > 0 && y >= 0 && x < numRows && y < numCols -1) {
 			
 			// Check there's no horizontal wall
 			if (d == 'h') {
 				if (board[x][y].h == false && board[x][y+1].h == false) {
 					// If there are no intersecting vertical walls, it's legal
 					if (!(board[x-1][y+1].v == true && board[x][y+1].v==true)) {
 						r = true;
 						// Temporarily set the walls to true
 						board[x][y].h = true;
 						board[x][y+1].h = true;
 					}
 				}	
 			// Check there's no vertical wall there
 			} else if (d == 'v') {
 				if ((board[x][y+1].v == false) && (board[x-1][y+1].v == false)) {
 					// If there are no intersecting horizontal walls, it's legal
 					if (!(board[x][y].h == true && board[x][y+1].h == true)) {
 						r = true;
 						// Temporarily set the walls to true
 						board[x-1][y+1].v = true;
 						board[x][y+1].v = true;
 
 					}
 				}
 			}
 		
 		}
 		
 		// If it cuts off another players path to their goal, it's not legal
 		// So we set the walls to true above, now check this, and then delete the walls
 		if (r == true) {
 			
 			for (int i = 1; i < players.length; i ++) {
 				if (stepsToGoal(players[i]) == -1) {
 					r = false;
 				}
 			}
 			
 			// Delete the walls
 			if (d == 'h') {
 				board[x][y].h = false;
 				board[x][y+1].h = false;
 			} else {
 				board[x-1][y+1].v = false;
 				board[x][y+1].v = false;
 			}
 		}
 		
 		if (r == false) {
 			print.printMessage("Illegal wall placement;\nLegal wall placements are within bounds\nThey don't intersect other walls\nAnd don't cut off player's paths to their goal");
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
 	
 	
 	//checks if its legal to jump over a player
 	private boolean isLegalJumpMove(Player p, int x, int y) {
 		boolean r = false;
 		//if the player moves to the left
 		if (p.getX()-x==2 && p.getY() == y && p.getX() > 1) {
 			// if there is a player one to the left
 			if (board[p.getX()-1][y].playerNum != 0 && board[p.getX()-1][y].h == false&& board[p.getX()][y].h == false) {
 				//return true (is a legal move)
 				r = true;
 			}
 		} else if (p.getX()-x==-2 && p.getY() == y && p.getX() < 8) {
 			// if there is a player one to the left
 			if (board[p.getX()+1][y].playerNum != 0 && board[p.getX()+1][y].h == false&& board[p.getX()][y].h == false) {
 				//return true (is a legal move)
 				r = true;
 			}
 		// else if the player is moving two up	
 		} else if (p.getY()-y==2 && p.getX() == x && p.getY() > 1) {
 			if (board[x][p.getY()-1].playerNum != 0 && board[x][p.getY()-1].v == false&& board[x][p.getY()].v == false) {
 				r = true;
 			}
 		} else if (p.getY()-y==-2 && p.getX() == x && p.getY() < 8) {
 			if (board[x][p.getY()+1].playerNum != 0 && board[x][p.getY()+2].v == false&& board[x][p.getY()].v == false) {
 				r = true;
 			}
 		}
 		return r;
 		
 	}
 	
 	private boolean isLegalDiagonalMove (Player p, int x, int y) {
 		// problem here - if jumping up it needs to be on the same square as the opponent 
 		//- if jumping down it has to be on the square below them
 		
 		//System.out.println("calling diagonal");
 		boolean r = false;
 		
 		int curX = p.getX();
 		int curY = p.getY();
 		// Check if we're changing X and Y by one each (valid diagonal)
 		if (Math.abs(curX-x) == 1 && Math.abs(curY-y) == 1) {
 			System.out.println("valid diagonal - x = " + x + " y = " + y + " cur x and y = " + curX + " and " + curY);
 			// We need to have a player at either one col different from us (in our jump direction)
 			if (board[curX][y].playerNum != 0) {
 				// If there's a vertical wall on the col on their other side, it's valid
 				if (board[curX][y+y-curY].v == true || board[curX-1][y+y-curY].v == true) {
 					r  = true;
 				}
 			} 
 
 			// Or we need to have a player at one row different from us (in our jump direction)
 			if (board[x][curY].playerNum != 0) {
 				int b = x+x-curX;
 				System.out.println("it's on the same y, diff x, trying to test" + b + " and y being cury = " + curY);
 				// If there's a horizontal wall on the row on their other side, it's valid
 				if (x+x-curX+1 < 9 && board[x+x-curX+1][curY].h == true){ 
 					r = true;
 				}
 			}
 		
 		}
 		
 		if (r == true) {
 			System.out.println("legal diagonal");
 		}
 		
 		return r;
 	}
 	
 	public int stepsToGoal (Player p) {
 		boolean foundGoal = false;
 		Queue<Position> moves = new LinkedList<Position>();
 		Position start = new Position(p.getX(),p.getY(),0,' ');
 		moves.add(start);
 		HashMap<String, Integer> map = new HashMap<String, Integer>();
 		while ((foundGoal == false) && (moves.isEmpty() == false)) {
 			Position current = moves.remove();
 			if (current.x <= p.maxGoalX 
 				 && current.x >= p.minGoalX
 				 && current.y <= p.maxGoalY 
 		    	 && current.y >= p.minGoalY) {
 				return current.steps;
 				}
 			//for (int i = 1; i <=2; i ++) {
 				if (current.y-1 >=0 && board[current.x][current.y].v == false && board[current.x][current.y-1].playerNum == 0) {
 					Position up = new Position(current.x,current.y-1,current.steps+1,' ');
 					String temp = Integer.toString(current.x) + Integer.toString(current.y-1);
 					if (map.containsKey(temp) == false) {
 						moves.add(up);
 						map.put(temp, 1);
 					}
 				}
 				
 				if (current.y-2 >=0 && board[current.x][current.y-1].v == false && current.steps == 0
 						&& board[current.x][current.y-1].playerNum != 0 && board[current.x][current.y-2].playerNum == 0) {
 					Position jumpLeft = new Position(current.x,current.y-2,current.steps+1,' ');
 					String temp = Integer.toString(current.x) + Integer.toString(current.y-2);
 					if (map.containsKey(temp) == false) {
 						moves.add(jumpLeft);
 						map.put(temp, 1);
 					}
 				}
 			//}
 			//for (int i = 1; i <=2; i ++) {
 				if (current.y+1 <=8 && current.x >= 0 && board[current.x][current.y+1].v == false && board[current.x][current.y+1].playerNum == 0) {
 					Position down = new Position(current.x,current.y+1,current.steps+1,' ');
 					String temp = Integer.toString(current.x) + Integer.toString(current.y+1);
 					if (map.containsKey(temp) == false) {
 						moves.add(down);
 						map.put(temp,1);
 					}
 				}
 				if (current.y+2 <=8 &&current.x >= 0  && board[current.x][current.y+2].v == false && current.steps == 0
 						&& board[current.x][current.y+1].playerNum != 0 && board[current.x][current.y+2].playerNum == 0) {
 					Position jumpLeft = new Position(current.x,current.y-2,current.steps+1,' ');
 					String temp = Integer.toString(current.x) + Integer.toString(current.y+2);
 					if (map.containsKey(temp) == false) {
 						moves.add(jumpLeft);
 						map.put(temp, 1);
 					}
 				}
 			//}
 			//for (int i = 1; i <=2; i ++) {
 				if (current.x-1>=0 && current.y >=0 && board[current.x][current.y].h == false && board[current.x-1][current.y].playerNum == 0) {
 					Position left = new Position(current.x-1,current.y,current.steps+1,' ');
 					String temp = Integer.toString(current.x-1) + Integer.toString(current.y);
 					if (map.containsKey(temp) == false) {
 						moves.add(left);
 						map.put(temp, 1);
 					}
 				}
 				if (current.x-2 >=0 && current.y >=0 && board[current.x-1][current.y].h == false && current.steps == 0
 						&& board[current.x-1][current.y].playerNum != 0 && board[current.x-2][current.y].playerNum == 0) {
 					Position jumpLeft = new Position(current.x-2,current.y,current.steps+1,' ');
 					String temp = Integer.toString(current.x-2) + Integer.toString(current.y);
 					if (map.containsKey(temp) == false) {
 						moves.add(jumpLeft);
 						map.put(temp, 1);
 					}
 				}
 			//}
 			//for (int i = 1; i <=2; i ++) {
 				if (current.x+1 <=8 && current.y >= 0 && board[current.x+1][current.y].h == false && board[current.x+1][current.y].playerNum == 0) {
 					Position right = new Position(current.x+1,current.y,current.steps+1,' ');
 					String temp = Integer.toString(current.x+1) + Integer.toString(current.y);
 					if (map.containsKey(temp) == false) {
 						moves.add(right);
 						map.put(temp,1);
 					}
 				}
 				if (current.x+2 <=8 && current.y >= 0 &&board[current.x+1][current.y].h == false && current.steps == 0
 						&& board[current.x+1][current.y].playerNum != 0 && board[current.x+2][current.y].playerNum == 0) {
 					Position jumpLeft = new Position(current.x+2,current.y,current.steps+1,' ');
 					String temp = Integer.toString(current.x+2) + Integer.toString(current.y);
 					if (map.containsKey(temp) == false) {
 						moves.add(jumpLeft);
 						map.put(temp, 1);
 					}
 				}
 			//}
 		}
 		return -1;
 	}
 	
 	public int stepsToNextRow (Player p) {
 		boolean foundGoal = false;
 		Queue<Position> moves = new LinkedList<Position>();
 		Position start = new Position(p.getX(),p.getY(),0,' ');
 		moves.add(start);
 		HashMap<String, Integer> map = new HashMap<String, Integer>();
 		while ((foundGoal == false) && (moves.isEmpty() == false)) {
 			Position current = moves.remove();
 
 			if (p.getPlayer() == 1) {
 				if (current.x == p.getX() -1) {
 					return current.steps;
 				}
 			}
 			if (p.getPlayer() == 2) {
 				if (current.x == p.getX() +1) {
 					return current.steps;
 				}
 			}
 			//for (int i = 1; i <=2; i ++) {
 				if (current.y-1 >=0 && board[current.x][current.y].v == false && board[current.x][current.y-1].playerNum == 0) {
 					Position up = new Position(current.x,current.y-1,current.steps+1,' ');
 					String temp = Integer.toString(current.x) + Integer.toString(current.y-1);
 					if (map.containsKey(temp) == false) {
 						moves.add(up);
 						map.put(temp, 1);
 					}
 				}
 				
 				if (current.y-2 >=0 && board[current.x][current.y-1].v == false && current.steps == 0
 						&& board[current.x][current.y-1].playerNum != 0 && board[current.x][current.y-2].playerNum == 0) {
 					Position jumpLeft = new Position(current.x,current.y-2,current.steps+1,' ');
 					String temp = Integer.toString(current.x) + Integer.toString(current.y-2);
 					if (map.containsKey(temp) == false) {
 						moves.add(jumpLeft);
 						map.put(temp, 1);
 					}
 				}
 			//}
 			//for (int i = 1; i <=2; i ++) {
 				if (current.y >= 0 && current.y+1 <=8 && current.x >= 0 && board[current.x][current.y+1].v == false && board[current.x][current.y+1].playerNum == 0) {
 					Position down = new Position(current.x,current.y+1,current.steps+1,' ');
 					String temp = Integer.toString(current.x) + Integer.toString(current.y+1);
 					if (map.containsKey(temp) == false) {
 						moves.add(down);
 						map.put(temp,1);
 					}
 				}
 				if (current.y+2 <=8 &&current.x >= 0  && board[current.x][current.y+2].v == false && current.steps == 0
 						&& board[current.x][current.y+1].playerNum != 0 && board[current.x][current.y+2].playerNum == 0) {
 					Position jumpLeft = new Position(current.x,current.y-2,current.steps+1,' ');
 					String temp = Integer.toString(current.x) + Integer.toString(current.y+2);
 					if (map.containsKey(temp) == false) {
 						moves.add(jumpLeft);
 						map.put(temp, 1);
 					}
 				}
 			//}
 			//for (int i = 1; i <=2; i ++) {
 				if (current.x-1>=0 && current.y >=0 && board[current.x][current.y].h == false && board[current.x-1][current.y].playerNum == 0) {
 					Position left = new Position(current.x-1,current.y,current.steps+1,' ');
 					String temp = Integer.toString(current.x-1) + Integer.toString(current.y);
 					if (map.containsKey(temp) == false) {
 						moves.add(left);
 						map.put(temp, 1);
 					}
 				}
 				if (current.x-2 >=0 && current.y >=0 && board[current.x-1][current.y].h == false && current.steps == 0
 						&& board[current.x-1][current.y].playerNum != 0 && board[current.x-2][current.y].playerNum == 0) {
 					Position jumpLeft = new Position(current.x-2,current.y,current.steps+1,' ');
 					String temp = Integer.toString(current.x-2) + Integer.toString(current.y);
 					if (map.containsKey(temp) == false) {
 						moves.add(jumpLeft);
 						map.put(temp, 1);
 					}
 				}
 			//}
 			//for (int i = 1; i <=2; i ++) {
 				if (current.x+1 <=8 && current.y >= 0 && board[current.x+1][current.y].h == false && board[current.x+1][current.y].playerNum == 0) {
 					Position right = new Position(current.x+1,current.y,current.steps+1,' ');
 					String temp = Integer.toString(current.x+1) + Integer.toString(current.y);
 					if (map.containsKey(temp) == false) {
 						moves.add(right);
 						map.put(temp,1);
 					}
 				}
 				if (current.x+2 <=8 && current.y >= 0 &&board[current.x+1][current.y].h == false && current.steps == 0
 						&& board[current.x+1][current.y].playerNum != 0 && board[current.x+2][current.y].playerNum == 0) {
 					Position jumpLeft = new Position(current.x+2,current.y,current.steps+1,' ');
 					String temp = Integer.toString(current.x+2) + Integer.toString(current.y);
 					if (map.containsKey(temp) == false) {
 						moves.add(jumpLeft);
 						map.put(temp, 1);
 					}
 				}
 			//}
 		}
 		return -1;
 	}
 	
 	// Copies the board over to another existing Board
 	public void copyBoard(Board b) {
 		for (int i = 0; i < numRows; i++) {
 			for (int j = 0; j < numCols; j++) {
 				board[i][j].h = b.board[i][j].h;
 				board[i][j].v = b.board[i][j].v;
 			}
 		}
 	}
 	
 	// Compares this board and another to find a newly placed wall
 	public Wall findNewWall(Board b) {
 		int x;
 		int y;
 		for (int i = 0; i < numRows; i++) {
 			for (int j = 0; j < numCols; j ++) {
 				
 				// If there's a new wall here, mark x and y and return the new Wall
 				if ((board[i][j].h == false && b.board[i][j].h == true)) {
 					x = i;
 					y = j;
					System.out.println("newwall");
 					return (new Wall(x,y,'h'));
 					
 				}
 				if ((board[i][j].v == false && b.board[i][j].v == true)) {
 					x = i;
 					y = j;
					System.out.println("newwall");
 					return (new Wall(x,y,'v'));
 				}
 			}
 		}
		System.out.println("no new wall detected");
 		return null;
 	}
 	
 	public Position findPlayer(int playerNum) {
		
 		for (int i = 0; i < numRows; i ++) {
			for (int j = 0; j < numRows; j ++) {
 				if (board[i][j].playerNum == playerNum) {
 					return new Position(i,j,0,' ');
 				}
 			}
 		}
 		return null;
 	}
 }
