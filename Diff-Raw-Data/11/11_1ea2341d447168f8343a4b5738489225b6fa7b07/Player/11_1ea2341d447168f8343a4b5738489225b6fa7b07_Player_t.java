 import java.util.Arrays;
 import java.util.ArrayList;
 
 public abstract class Player {
 
 	private Board myBoard;
 	private Robot gameRobot;
 	//true if the player is located on the side of the board marked with index 0
 	private boolean isOnZeroSide;
 	//contains the piece color
 	private String color;
 
 	public Player (String startColor, boolean startsOnZeroSide) {
 		this.color = startColor;
 		this.isOnZeroSide = startsOnZeroSide;
 	}
 
 	public Player (String startColor, boolean startsOnZeroSide, Robot startGameRobot) {
 		this.color = startColor;
 		this.isOnZeroSide = startsOnZeroSide;
 		this.gameRobot = startGameRobot;
 	}
 
 	public Player (boolean startsOnZeroSide) {
 		this.isOnZeroSide = startsOnZeroSide;
 	}
 
 	public boolean getIsOnZeroSide() {
 		return this.isOnZeroSide;
 	}
 
 	public String getColor() {
 		return this.color;
 	}
 
 	public Piece[] getPlayerPieces () {
 		Piece[] result = new Piece[this.myBoard.totalPiecesLeft(this)];
 		int i = 0;
 		for (Piece p : this.myBoard.getPiecesOnBoard()) {
 			if (p.getPlayer() == this) {
 				result[i++] = p;
 			}
 		}
 		return result;
 	}
 
 	public Move calculateBestMove (Game g, int recursionDepth) {
 		return this.rankBestMoves(g, recursionDepth)[0];
 	}
 
 	public Move[] rankBestMoves (Game g, int recursionDepth) {
 		//creates array to hold all possible moves
 		Move[] moves = this.getAllMoves();
 
 		//creates empty array to hold boards created from moves
 		Board[] boards = new Board[moves.length];
 		//iterates over all moves, applying them to fill boards
 		for (int i = 0; i<moves.length; i++) {
 			boards[i] = new Board(g.getGameBoard(), moves[i]);
 		}
 
 		//creates array to hold values of boards
 		double[] boardValues = new double[boards.length];
 
 		//if recursionDepth is one, calculate direct values of moves
 		if (recursionDepth==1) {			
 			//iterates over all boards and calculates values to put in boardValues
 			for (int i = 0; i<boards.length; i++) {
 				boardValues[i] = boards[i].calculateValue(this);
 			}
 
 		//recursionDepth must be greater than one, so get values of the best opponent moves for each possible move
 		} else {
 			//iterates over all boards and calculates value based on best opponent move
 			for (int i = 0; i<boards.length; i++) {
 				boardValues[i] = 1/g.getOtherPlayer(this).valueOfBestMove(g, recursionDepth-1);
 			}
 		}
 
 		//creates array to hold sorted values from lowest to highest
 		double[] boardValuesSorted = Arrays.copyOf(boardValues, boardValues.length);
 		Arrays.sort(boardValuesSorted);
 
 		//creates array to hold final list of moves
 		Move[] sortedMoves = new Move[moves.length];
 		//creates variable to hold index of move in original list
 		int index = 0;
 		//iterates over all values of boardValuesSorted
 		for (int i = 0; i<boardValuesSorted.length; i++) {
 			//finds the index of the current values in the original move list
 			index = Arrays.binarySearch(boardValues, boardValuesSorted[i]);
 			//sets the value at index to -1 so that the same move is not used again, even if multiple moves have equal values
 			boardValues[index] = -1;
 			//puts the correct move in the correct position in the final array
 			sortedMoves[moves.length-index-1] = moves[index];
 		}
 
 		return sortedMoves;
 	}
 
 	public double valueOfBestMove(Game g, int recursionDepth) {
 		//creates array to hold all possible moves
 		Move[] moves = this.getAllMoves();
 
 		//creates empty array to hold boards created from moves
 		Board[] boards = new Board[moves.length];
 		//iterates over all moves, applying them to fill boards
 		for (int i = 0; i<moves.length; i++) {
 			boards[i] = new Board(g.getGameBoard(), moves[i]);
 		}
 
 		//creates array to hold values of boards
 		double[] boardValues = new double[boards.length];
 
 		//if recursionDepth is one, calculate direct values of moves
 		if (recursionDepth==1) {
 			//iterates over all boards and calculates values to put in boardValues
 			for (int i = 0; i<boards.length; i++) {
 				boardValues[i] = boards[i].calculateValue(this);
 			}
 		
 		//recursionDepth must be greater than one, so get values of the best opponent moves for each possible move
 		} else {
 			//iterates over all boards and calculates value based on best opponent move
 			for (int i = 0; i<boards.length; i++) {
 				boardValues[i] = 1/g.getOtherPlayer(this).valueOfBestMove(g, recursionDepth-1);
 			}
 		}
 
 		//creates array to hold sorted values from lowest to highest
 		double[] boardValuesSorted = Arrays.copyOf(boardValues, boardValues.length);
 		Arrays.sort(boardValuesSorted);
 
 		//creates variable to hold result value
 		double result = Math.pow(boardValuesSorted[0], 7);
 		//iterates over all values except the first
 		for (int i = 1; i<boardValuesSorted.length; i++) {
 			result *= Math.pow(boardValuesSorted[i], 3/(boardValuesSorted.length-1));
 		}
 		return Math.pow(result, 0.1);
 	}
 
 	public static void performMove(Move myMove, Board theBoard) {
 		myMove.getMovePiece().setLocation(myMove.getDestination());
 		for (Piece deadPiece : myMove.calculatePiecesToJump()) {
 			theBoard.removePiece(deadPiece);
 		}
 	}
 	
 	public void setBoard (Board newBoard) {
 		this.myBoard = newBoard;
 	}
 
 	public Board getBoard () {
 		return this.myBoard;
 	}
 
 	public Move[] getAllMoves () {
 		//creates an ArrayList to return later
 		ArrayList<Move> result = new ArrayList<Move>();
 		//iterates over each of the player's pieces
 		for (Piece playerPiece : this.getPlayerPieces()) {
 			//iterates over all of that piece's moves
 			for (Move pieceMove : playerPiece.getMovesOfPiece()) {
 				//adds each move to the return ArrayList
 				result.add(pieceMove);
 			}
 		}
 		//returns the final result
 		return result.toArray(new Move[result.size()]);
 	}
 
	public abstract void takeTurn(Game g);
 }
