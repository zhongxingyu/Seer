 package myClasses;
 
 public class GameBoard {
 	
 	//Game rules Validation is not handled in this class
 
 	//Board:
 	//First dimension is square id, 0 being outermost to 2 being innermost
 	//Second dimension is position on square, from 0 to 7 going clockwise from top-left
 	//Position:
 	//One dimensional array containing square id and position id on square
 	//
 	private Piece[][] board;
 	private Piece[][] side;
 
 	public GameBoard(Player[] players) {
 		// Initialize Global Variables
 		board = new Piece[3][8];
 		side = new Piece[2][9];
 		
 		// Place all Pieces on Player's Sides
 		for (int i = 0; i < 2; i++) {
 			for (int j = 0; j < 9; j++) {
 				side[i][j] = new Piece(players[i]);
 			}
 		}
 	}
 
 	/**
 	 * @author Chase
 	 * 
 	 * @return returns -1 for error
 	 * @param position
 	 */
 	public int addPiece(int playerID, int[] position) {
 		//check if position is in range
 		if(!isPositionValid(position)){
 			return -1;
 		}
 		// look for highest indexed piece on players side
 		int index = piecesOnSide(playerID) - 1;
 
 		//--------Conditions BEGIN
 		//check for no pieces on side
 		if (index == -1) {
 			return -1;
 		}
 		//check if board position is taken
 		if (board[position[0]][position[1]] != null) {
 			return -1;
 		}
 		//--------Conditions END
 
 		//add piece to board
 		board[position[0]][position[1]] = side[playerID][index];
 		//remove piece from side
 		side[playerID][index] = null;
 
 		return 0;
 	}
 
 	/**
 	 * @author Chase
 	 * 
 	 * @return returns -1 for error
 	 * @param position1
 	 * @param position2
 	 */
 	public int movePiece(int[] position1, int[] position2) {
 		//check if position1 and position2 are in range
 		if(!isPositionValid(position1) || !isPositionValid(position1)){
 			return -1;
 		}
 		//--------Conditions BEGIN
 		//check if a piece is at position1 on board
 		if (board[position1[0]][position1[1]] == null) {
 			return -1;
 		}
 		//check if board at position2 is taken
 		if (board[position2[0]][position2[1]] != null) {
 			return -1;
 		}
 		//--------Conditions END
 
 		//move piece to position2
 		board[position2[0]][position2[1]] = board[position1[0]][position1[1]];
 		//remove piece from position1
 		board[position1[0]][position1[1]] = null;
 
 		return 0;
 	}
 
 	/**
 	 * @author Chase
 	 * 
 	 * 
 	 * @param position
 	 */
 	public int takePiece(int playerID, int[] position) {
 		//check if position is in range
 		if(!isPositionValid(position)){
 			return -1;
 		}
 		//--------Conditions BEGIN
 		//check if a piece is at position1 on board
 		if (board[position[0]][position[1]] == null) {
 			return -1;
 		}
 		//--------Conditions END
 
 		int index = piecesOnSide(playerID);
 		// add piece to side
 		side[playerID][index] = board[position[0]][position[1]];
 		// remove piece from position on board
 		board[position[0]][position[1]] = null;
 		return 0;
 	}
 
 	//TODO double check isMill algorithms
 	
 	/**
 	 * Checks if a piece on the gameboard is apart of a mill
 	 * @author Chase
 	 * 
 	 * @param position
 	 */
 	public boolean isMill(int[] position) {
 		//check if position is in range
 		if(!isPositionValid(position)){
 			return false;
 		}
 		//--------Conditions BEGIN
 		//check if a piece is at position on board
 		if(board[position[0]][position[1]] == null){
 			return false;
 		}
 		//--------Conditions END
 
 		//get piece owner
 		Player player = board[position[0]][position[1]].getOwner();
 		//if middle piece
 		if(position[1] % 2 == 1){
 			//check across squares mill
 			if( board[(position[0]+1)%3][position[1]] != null &&
 				board[(position[0]+2)%3][position[1]] != null)
 			{
 				if( board[(position[0]+1)%3][position[1]].getOwner() == player &&
 					board[(position[0]+2)%3][position[1]].getOwner() == player)
 				{
 					return true;
 				}
 			}
 			//check same square mill
 			if( board[(position[0])][(position[1]-1)] != null &&
 				board[(position[0])][(position[1]+1)] != null)
 			{
 				if( board[position[0]][(position[1]-1)].getOwner() == player &&
 					board[position[0]][(position[1]+1)].getOwner() == player)
 				{
 					return true;
 				}
 			}
 		}else{	//it is a corner piece
 			//check clockwise mill
 			if( board[(position[0])][position[1]+1] != null &&
 				board[(position[0])][position[1]+2] != null)
 			{
 				if( board[position[0]][(position[1]+1)].getOwner() == player &&
 					board[position[0]][(position[1]+2)].getOwner() == player)
 				{
 					return true;
 				}
 			}
 			//check counter-clockwise mill
 			if( board[(position[0])][position[1]-1] != null &&
 				board[(position[0])][position[1]-2] != null)
 			{
 				if( board[position[0]][(position[1]-1)].getOwner() == player &&
 					board[position[0]][(position[1]-2)].getOwner() == player)
 				{
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	/**
 	 * @author Chase
 	 * 
 	 * 
 	 * @param player
 	 */
 	public boolean hasNonMillPieces(Player player) {
 		//Go through each piece on board
 		for(int i=0; i<=2; i++){
 			for(int j=0; j<=7; j++){
 				//check if piece is at position
 				if(board[i][j] == null){
 					//go to the next position
 				}
 				//check if piece is owned by player
 				else if(board[i][j].getOwner() == player){
 					//check if piece is in a mill
 					if(!isMill(new int[]{i,j})){
 						return true;
 					}
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	/**
 	 * @author Chase
 	 * 
 	 * @param player
 	 */
 	public int piecesOnSide(int playerID) {
 		int i = 9;
 		while (side[playerID][i - 1] == null && i > 0) {
 			i--;
 			if (i == 0) {
 				break;
 			}
 		}
 		// change from index to count
 		return i;
 	}
 
 	/**
 	 * @author Chase
 	 * 
 	 * 
 	 * @param position
 	 */
 	public Piece getPiece(int[] position) {
 		//check if position is in range
 		if(!isPositionValid(position)){
 			return null;
 		}
 		return board[position[0]][position[1]];
 	}
 	
 	/**
 	 * @author Chase
 	 * 
 	 * 
 	 * @param position
 	 */
 	public boolean isPositionValid(int[] position) {
 		if(position == null){
 			return false;
 		}
 		if(position[0] < 0 && position[0] > 2){
 			return false;
 		}
 		if(position[1] < 0 && position[1] > 7){
 			return false;
 		}
 		return true;
 	}
 	
 	public Piece[][] getBoard() {
 		return board;
 	}
 
}
