 package edu.ycp.cs481.ycpgames;
 
import android.util.Log;

 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by brian on 11/18/13.
  */
 public class CheckersGame {
	private static final String TAG = "YCPGamesCheckersGame";
 	private List<int[]> validMoves = new ArrayList<int[]>();
 	private int selectedX = -1, selectedY = -1;
 	private CheckersPlayer playerOne = new CheckersPlayer(CheckersVal.PLAYER_ONE);
 	private CheckersPlayer playerTwo = new CheckersPlayer(CheckersVal.PLAYER_TWO);
 	private CheckersBoard board = new CheckersBoard();
 	public CheckersGame(){
 		super();
         this.reset();
 	}
 
 	/**
 	 * resets everything
 	 */
 	public void reset() {
 		playerOne.setMyTurn(true);
 		playerTwo.setMyTurn(false);
 		board.reset();
 	}
 
 	/**
 	 * used to select a piece, fills valid moves array
 	 *
 	 * @param x x value of piece to select
 	 * @param y y value of piece to select
 	 * @return true if valid piece to select false otherwise
 	 */
 	public boolean selectPiece(int x, int y){
		Log.d(TAG, "piece = " + x + " " + y);
 		if(board.getPieceAt(x,y).getPlayer() == whosTurn()){
 			selectedX = x;
 			selectedY = y;
 			validMoves = board.getValidMoves(x,y);
 			return true;
 		}else{
 			return false;
 		}
 	}
 
 	/**
 	 *
 	 * @return an array of valid moves for selected piece
 	 */
 	public List<int[]> getValidMoves() {
 		return validMoves;
 	}
 
 	/**
 	 *
 	 * @param x    x value of move
 	 * @param y    y value of move
 	 * @return code
 	 */
 	public void makeMove(int x, int y){
 		int[] selectedMove = null;
 		for(int[] move : validMoves){
 			if((move[0] == x) && (move[1] == y)){
 				selectedMove = move;
 				break;
 			}
 		}
 		//return if invalid move
 		if (selectedMove == null){
 			return;
 		}
 		board.makeMove(selectedX,selectedY,selectedMove);
 		endTurn();
 		//ai move here
 	}
 
 	/**
 	 *
 	 * @return CheckersVal of whos turn it is
 	 */
 	public CheckersVal whosTurn(){
 		if(playerOne.isMyTurn() == true){
 			return CheckersVal.PLAYER_ONE;
 		}else{
 			return CheckersVal.PLAYER_TWO;
 		}
 	}
 	/**
 	 * Switches who's turn it is
 	 */
 	private void endTurn() {
 		playerOne.setMyTurn(!playerOne.isMyTurn());
 		playerTwo.setMyTurn(!playerTwo.isMyTurn());
 		selectedX = -1;
 		selectedY = -1;
 		validMoves = new ArrayList<int[]>();;
 	}
 
     public CheckersBoard getBoard() {
         return this.board;
     }
 }
