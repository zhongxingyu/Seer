 package edu.ycp.cs481.ycpgames;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by brian on 11/24/13.
  */
 public class CheckersBoard {
 	private static final String TAG = "YCPGamesBoard";
 	private CheckersPiece[][] grid = new CheckersPiece[8][8];
 	private int playerOnePieces, playerTwoPieces;
 
 	Settings settings;
 	public CheckersBoard(){
 		settings = Settings.getInstance();
 		reset();
 	}
 
 	/**
 	 * resets board
 	 */
 	public void reset(){
 		for(int x = 0; x < grid.length; x++){
 			for(int y = 0; y < grid[0].length; y++){
 				if(((y==0) || (y == 2)) && ((x%2) == 0)){
 					//if y is 0/2 and x is even
 					grid[x][y] = new CheckersPiece(CheckersVal.PLAYER_ONE);
 				}else if((y==1) && ((x%2) ==1)){
 					// if y is 1 and x is odd
 					grid[x][y] = new CheckersPiece(CheckersVal.PLAYER_ONE);
 				}else if(((y==5) || (y == 7)) && ((x%2) == 1)){
 					//if y is 5/7 and x is odd
 					grid[x][y] = new CheckersPiece(CheckersVal.PLAYER_TWO);
 				}else if((y==6) && ((x%2) ==0)){
 					//if y is 6 and x is even
 					grid[x][y] = new CheckersPiece(CheckersVal.PLAYER_TWO);
 				}else{
 					grid[x][y] = new CheckersPiece(CheckersVal.EMPTY);
 				}
 			}
 		}
 		playerOnePieces = 12;
 		playerTwoPieces = 12;
 	}
 
 	/**
 	 *
 	 * @param x    x value of piece to get moves for
 	 * @param y    y value of piece to get moves for
 	 * @return 		list of valid moves
 	 * 				[0] = x coordinate
 	 * 				[1] = y coordinate
 	 * 				[2] = is a jump or not
 	 * 						0 means no 1 means yes
 	 * 				[3] = x of jumped piece
 	 * 				[4] = y of jumped piece
 	 */
 	public List<int[]> getValidMoves(int x, int y){
 		List<int[]> validMoves = new ArrayList<int[]>();
 		if(grid[x][y].getPlayer() == CheckersVal.PLAYER_ONE){
 			//find valid moves for player 1 piece
 			if((x!= 0) && (y != 7) && (grid[x-1][y+1].getPlayer() == CheckersVal.EMPTY)){
 				//check front diagonal left
 				validMoves.add(new int[]{x-1, y+1,0,0,0});
 			}else if((x!= 0) && (y != 7) && (grid[x-1][y+1].getPlayer() == CheckersVal.PLAYER_TWO)){
 				//check if player can jump it
 				if((x!= 1) && (y != 6) && (grid[x-2][y+2].getPlayer() == CheckersVal.EMPTY)){
 					validMoves.add(new int[]{x-2,y+2,1,x-1,y+1});
 				}
 			}
 
 			if((x!= 7) && (y != 7) && (grid[x+1][y+1].getPlayer() == CheckersVal.EMPTY)){
 				//check front diagonal right
 				validMoves.add(new int[]{x+1, y+1,0,0,0});
 			}else if((x!= 7) && (y != 7) && (grid[x+1][y+1].getPlayer() == CheckersVal.PLAYER_TWO)){
 				//check if player can jump it
 				if((x!= 6) && (y != 6) && (grid[x+2][y+2].getPlayer() == CheckersVal.EMPTY)){
 					validMoves.add(new int[]{x+2,y+2,1,x+1,y+1});
 				}
 			}
 
 			if(grid[x][y].getRank() == CheckersVal.KING){
 				//if a king check behind
 				if((x!= 0) && (y != 0) && (grid[x-1][y-1].getPlayer() == CheckersVal.EMPTY)){
 					//check back diagonal left
 					validMoves.add(new int[]{x-1, y-1,0,0,0});
 				}else if((x!= 0) && (y != 0) && (grid[x-1][y-1].getPlayer() == CheckersVal.PLAYER_TWO)){
 					//check if player can jump it
 					if((x!= 1) && (y != 1) && (grid[x-2][y-2].getPlayer() == CheckersVal.EMPTY)){
 						validMoves.add(new int[]{x-2,y-2,1,x-1,y-1});
 					}
 				}
 
 				if((x!= 7) && (y != 0) && (grid[x+1][y-1].getPlayer() == CheckersVal.EMPTY)){
 					//check back diagonal right
 					validMoves.add(new int[]{x+1, y-1,0,0,0});
 				}else if((x!= 7) && (y != 0) && (grid[x+1][y-1].getPlayer() == CheckersVal.PLAYER_TWO)){
 					//check if player can jump it
 					if((x!= 1) && (y != 1) && (grid[x+2][y-2].getPlayer() == CheckersVal.EMPTY)){
 						validMoves.add(new int[]{x+2,y-2,1});
 					}
 				}
 			}
 		}else if(grid[x][y].getPlayer() == CheckersVal.PLAYER_TWO){
 			//find valid moves for player 2 piece
 			if((x!= 0) && (y != 0) && (grid[x-1][y-1].getPlayer() == CheckersVal.EMPTY)){
 				//check front diagonal left
 				validMoves.add(new int[]{x-1, y-1,0,0,0});
 			}else if((x!= 0) && (y != 0) && (grid[x-1][y-1].getPlayer() == CheckersVal.PLAYER_ONE)){
 				//check if player can jump it
 				if((x!= 1) && (y != 1) && (grid[x-2][y-2].getPlayer() == CheckersVal.EMPTY)){
 					validMoves.add(new int[]{x-2,y-2,1,x-1,y-1});
 				}
 			}
 
 			if((x!= 7) && (y != 0) && (grid[x+1][y-1].getPlayer() == CheckersVal.EMPTY)){
 				//check front diagonal right
 				validMoves.add(new int[]{x+1, y-1,0,0,0});
 			}else if((x!= 7) && (y != 0) && (grid[x+1][y-1].getPlayer() == CheckersVal.PLAYER_ONE)){
 				//check if player can jump it
 				if((x!= 6) && (y != 1) && (grid[x+2][y-2].getPlayer() == CheckersVal.EMPTY)){
 					validMoves.add(new int[]{x+2,y-2,1,x+1,y-1});
 				}
 			}
 
 			if(grid[x][y].getRank() == CheckersVal.KING){
 				//if a king check behind
 				if((x!= 0) && (y != 7) && (grid[x-1][y+1].getPlayer() == CheckersVal.EMPTY)){
 					//check back diagonal left
 					validMoves.add(new int[]{x-1, y+1,0,0,0});
 				}else if((x!= 0) && (y != 7) && (grid[x-1][y+1].getPlayer() == CheckersVal.PLAYER_TWO)){
 					//check if player can jump it
 					if((x!= 1) && (y != 6) && (grid[x-2][y+2].getPlayer() == CheckersVal.EMPTY)){
 						validMoves.add(new int[]{x-2,y+2,1,x-1,y+1});
 					}
 				}
 
 				if((x!= 7) && (y != 0) && (grid[x+1][y+1].getPlayer() == CheckersVal.EMPTY)){
 					//check back diagonal right
					validMoves.add(new int[]{x+1, y-1,0,0,0});
 				}else if((x!= 7) && (y != 7) && (grid[x+1][y+1].getPlayer() == CheckersVal.PLAYER_TWO)){
 					//check if player can jump it
 					if((x!= 1) && (y != 6) && (grid[x+2][y+2].getPlayer() == CheckersVal.EMPTY)){
 						validMoves.add(new int[]{x+2,y+2,1,x+1,y+1});
 					}
 				}
 			}
 		}
 		return validMoves;
 	}
 
 	public CheckersPiece getPieceAt(int x, int y){
 		return grid[x][y];
 	}
 
 	/**
 	 *
 	 * @param x x location of selected piece
 	 * @param y y location of selected piece
 	 * @param move    values for move
 	 */
 	public CheckersPiece makeMove(int x, int y, int[] move){
 		CheckersPiece jumpedPiece = null;
 		grid[move[0]][move[1]] = grid[x][y];
 		grid[x][y] = new CheckersPiece(CheckersVal.EMPTY);
 		if(move[2] ==1){
 			//move was a jump
 			if(grid[move[3]][move[4]].getPlayer() == CheckersVal.PLAYER_ONE){
 				playerOnePieces--;
 			}else{
 				playerTwoPieces--;
 			}
 			jumpedPiece = grid[move[3]][move[4]];
 			grid[move[3]][move[4]] = new CheckersPiece(CheckersVal.EMPTY);
 		}
         if(move[1] == 0 || move[1] == 7){
             grid[move[0]][move[1]].promote();
         }
 		return jumpedPiece;
 	}
 
 	/**
 	 * undoes a makeMove()
 	 * @param x	original value given to makeMove
 	 * @param y original value given to makeMove
 	 * @param move original value given to makeMove
 	 * @param jumpedPiece output of makeMove
 	 */
 	public void undoMove(int x, int y, int[] move, CheckersPiece jumpedPiece){
 		grid[x][y] = grid[move[0]][move[1]];
 		grid[move[0]][move[1]] = new CheckersPiece(CheckersVal.EMPTY);
 		if(move[2] == 1){
 			//move was a jump
 			if(grid[x][y].getPlayer() == CheckersVal.PLAYER_ONE){
 				playerOnePieces++;
 
 			}else{
 				playerTwoPieces++;
 			}
 			grid[move[3]][move[4]] = jumpedPiece;
 		}
 
 
 	}
 	public int isGameOver(){
 		if(playerOnePieces == 0){
 			return 2;
 		}
 		else if(playerTwoPieces == 0){
 			return 1;
 		}else{
 			return 0;
 		}
 	}
 
 	public int getPlayerOnePieces() {
 		return playerOnePieces;
 	}
 
 	public int getPlayerTwoPieces() {
 		return playerTwoPieces;
 	}
 
 }
