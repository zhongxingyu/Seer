 package main;
 
 import java.util.Observable;
 
 /**
  * A singleton Board Class containing an 8x8 grid on which checkers is played.
  * <p>
  * Each square is a member of a 2-dimensional Piece array that can hold a single
  * instance of a 'Piece' class.
  */
 public class Board extends Observable{
 	public static final int BOARD_ROWS = 8;
 	public static final int BOARD_COLUMNS = 8;
	private Piece[][] boardArray = new Piece[8][8];
 	private int turnComplete = 0;
 	
 	/*
 	 * Initializes the game board by creating a 2-D Piece array to store the
 	 * information of every square on the board. The arrays can hold either a Piece
 	 * instance or a Null object.
 	 * 
 	 * @author Dylan Dobbyn
 	 */
 	void initializeBoard() {
 		for (int row = 0; row <= BOARD_ROWS - 1; row++) {
 			for (int column = 0; column <= BOARD_COLUMNS - 1; column++) {
 				try{
 					if (row <= 2) {
 						if ((row == 1) && (column % 2 == 1)) {
 							boardArray[row][column] = new Piece(Colour.BLACK, new Location(column,row));
 						} else if ((row != 1) && (column % 2 == 0)) {
 							boardArray[row][column] = new Piece(Colour.BLACK, new Location(column,row));
 						}
 					}
 					if (row >= 5) {
 						if ((row == 6) && (column % 2 == 0)) {
 							boardArray[row][column] = new Piece(Colour.RED, new Location(column,row));
 						} else if ((row != 6) && (column % 2 == 1)) {
 							boardArray[row][column] = new Piece(Colour.RED, new Location(column,row));
 						}
 					}
 				// This exception should never trigger in a correct game.
 			    // The game will terminate should this occur.
 				} catch (OutOfBoundsException oobe) {
 					System.out.println("Fatal error initializing the Board.");
 					System.exit(1);
 				}
 			}
 		}
 	}

	public Piece[][] getArray(){
		return this.boardArray;
	}	
 	/**
 	 * Resets the game board to its original state by wiping all the Squares, and then
 	 * calling the initializeBoard() method.
 	 */
 	public void resetBoard() {
 		for (int row = 0; row <= BOARD_ROWS - 1; row++){
 			for (int column = 0; column <= BOARD_COLUMNS - 1; column++){
 				boardArray[row][column] = null;
 			}
 		}
 		initializeBoard();
 	}
 	
 	/**
 	 * Checks the board array to identify the Piece object occupying a given
 	 * square.
 	 * 
 	 * @param square
 	 *            the Location on the board to be checked.
 	 * 
 	 * @return boardArray[x][y] The Piece object held on that particular Board
 	 *         coordinates- null if empty.
 	 * 
 	 * @author Dylan Dobbyn
 	 */
 	public Piece checkSquare(Location square) {
 		if(!square.inBounds()){ return null; }
 		return boardArray[square.getY()][square.getX()];
 	}
 
 	/**
 	 * Acessor Method to find all of the Pieces of a given Colour and return them in an Array.
 	 * @param aColour The colour of the Pieces to be searched for.
 	 * @return An array containing all of the Pieces of the given Colour.
 	 */
 	public Piece[] getPieces(Colour aColour) {
 		Piece[] tempPiece = new Piece[12];
 		int tempIndex = 0;
 		for(int i = 0; i < BOARD_ROWS; i++) {
 			for(int j = 0; j < BOARD_COLUMNS; j++){
 				if(boardArray[i][j] != null && boardArray[i][j].getColour() == aColour) {
 					tempPiece[tempIndex] = boardArray[i][j];	// A temporary array is filled with all of the correctly coloured Pieces
 					tempIndex++;
 				}
 			}
 		}
 
 		Piece[] myPieces = new Piece[tempIndex];
 		for(int index = 0; index < tempIndex; index++){	// An array of correct size is then generated and returned.
 			myPieces[index] = tempPiece[index];
 		}
 		return myPieces;
 	}
 	
 	/*
 	 * Private setter method to change the state of a position on the board.
 	 * Only used within Board's public methods to ensure encapsulation.
 	 */
 	private void setSquare(Location square, Piece piece) throws OutOfBoundsException {
 		if(!square.inBounds()) {
 			throw new OutOfBoundsException("A piece was set at an invalid location.");
 		}
 		boardArray[square.getY()][square.getX()] = piece;
 		if(piece != null){
 			piece.setLocation(square);
 		}
 	}
 	
 	/**
 	 * Accessor method to return the state of turn completion
 	 * @return An integer turnComplete denoting the status of the current turn (0-2)
 	 */
 	public int turnComplete(){ return turnComplete; }
 	
 	
 	public void resetTurn(){ 
 		clearChanged();
 		turnComplete = 0; 
 	}
 	private void continueTurn(){ 
 		setChanged();
 		turnComplete = 1; 
 	}
 	private void endTurn(){ 
 		setChanged();
 		turnComplete = 2; 
 	}
 
 
 	/**
 	 * Moves a piece that is on one square of the board to another square of the
 	 * board, positions given by Location objects. Will also remove any pieces
 	 * if applicable.
 	 * 
 	 * @param player
 	 *            the Player who owns the piece to be moved.
 	 * @param start
 	 *            A Location object containing the information of the starting
 	 *            square.
 	 * @param end
 	 *            A Location object containing information on the square to move
 	 *            the piece to.
 	 * @author Dylan Dobbyn
 	 */
 	public void movePiece(Player player, Move aMove) {
 		resetTurn();
 		if (aMove.isValid() == false) {return;}
 		Location start = aMove.getStart();
 		Location end = aMove.getEnd();
 		
 		try {
 			if (aMove.isJump(start,end)) {
 				int tempY = (end.getY() + start.getY()) / 2;
 				int tempX = (end.getX() + start.getX()) / 2;
 				Location middle = new Location(tempX, tempY);
 				
 				setSquare(middle,null);
 				setSquare(end,checkSquare(start));
 				setSquare(start,null);
 				
 				if(checkSquare(end).emptyJumps(player, this).length > 0){
 					continueTurn();
 				} else {
 					endTurn();
 				}
 				
 			} else {
 				endTurn();
 				setSquare(end,checkSquare(start));
 				setSquare(start,null);
 			}
 			
 		}
 		catch (OutOfBoundsException oobe){
 			System.out.println("A movement was made outside of the board's boundaries.");
 			return;
 		}
 	}
 
 	/*
 	 * Accessor methods to determine the distance between two
 	 * X or Y Locations. This is used across other methods and classes, 
 	 * and is simply a timesaver.
 	 */
 	public int deltaX(Location start, Location end) {
 		return Math.abs((start.getX() - end.getX()));
 	}
 	public int deltaY(Location start, Location end) {
 		return Math.abs((start.getY() - end.getY()));
 	}
 
 	
 	/**
 	 * Prints the array to the screen. Used instead of a toString() method to
 	 * identify what information is being held in the board.
 	 * 
 	 * @author Dylan Dobbyn
 	 */
 	public void printArray() {
 		for (int i = -1; i <= 7; i++) {
 			for (int j = -1; j <= 7; j++) {
 
 				if ( i == -1 && j == -1 ) {
 					System.out.print("    ");
 				} else if ( i == -1 ) {
 					if (j == 7) {
 						System.out.print(j + "\n\n");
 					} else {
 						System.out.print(j + " ");
 					}
 				} else if (j == -1 ) {
 					System.out.print(i + "   ");
 				} 
 
 				else if (j == 7) {
 					if (boardArray[i][j] == null) {
 						System.out.print("_" + "\n");
 					} else {
 						System.out.print((boardArray[i][j]) + "\n");
 					}
 				} else {
 					if (boardArray[i][j] == null) {
 						System.out.print("_" + " ");
 					} else {
 						System.out.print((boardArray[i][j]) + " ");
 					}
 
 				}
 			}
 		}
 		System.out.print("\n");
 	}
 
 /**
  * A singleton Board Class containing an 8x8 grid on which checkers is played.
  * <p>
  * Each 
  square is a member of a 2-dimensional Piece array that can hold a single
  * instance of a 'Piece' class.
  */
 
 	
 	public Board cloneBoard(Board lastBoard){
 		Board b = new Board();
 		for(int row = 0; row < Board.BOARD_ROWS; row++){
 			for(int col = 0; col < Board.BOARD_COLUMNS; col++){
 				Location tempLoc = null;
 				try {
 					tempLoc = new Location(row,col);
 				} catch (OutOfBoundsException e) {
 					System.exit(1);
 				}
 				
 				if(lastBoard.checkSquare(tempLoc) == null){
 					continue;
 				} else {
 					try {
 						b.setSquare(tempLoc,new Piece(lastBoard.checkSquare(tempLoc).getColour(), tempLoc));
 					} catch (OutOfBoundsException e) {
 						continue;
 					}
 				}
 				
 			}
 		}
 		
 		return b;
 	}
 }
 
