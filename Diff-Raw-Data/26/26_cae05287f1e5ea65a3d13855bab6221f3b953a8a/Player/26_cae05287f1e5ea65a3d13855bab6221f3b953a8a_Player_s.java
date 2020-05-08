 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Collections;
 
 public abstract class Player {
 
 	private Board myBoard;
 	private Robot gameRobot;
 	//true if the player is located on the side of the board marked with index 0
 	private boolean isOnZeroSide;
 	//contains the piece color
 	private String color;
 
 	private String xo;
 
 	// public Player (String startColor, boolean startsOnZeroSide) {
 	// 	this.color = startColor;
 	// 	this.isOnZeroSide = startsOnZeroSide;
 	// }
 
 	public Player (String startColor, boolean startsOnZeroSide, Robot startGameRobot) {
 		this.color = startColor;
 		this.isOnZeroSide = startsOnZeroSide;
 		this.gameRobot = startGameRobot;
 	}
 
 	public Player(String startXO, boolean startsOnZeroSide) {
 		this.xo = startXO;
 		this.isOnZeroSide = startsOnZeroSide;
 	}
 
 	public Player (boolean startsOnZeroSide) {
 		this.isOnZeroSide = startsOnZeroSide;
 	}
 
 	public boolean getIsOnZeroSide() {
 		return this.isOnZeroSide;
 	}
 
 	public Robot getRobot() {
 		return this.gameRobot;
 	}
 
 	public String getColor() {
 		return this.color;
 	}
 
 	public Piece[] getPlayerPieces (Board b) {
 		Piece[] result = new Piece[b.totalPiecesLeft(this)];
 		int i = 0;
 		for (Piece p : b.getPiecesOnBoard()) {
 			if (p.getPlayer() == this) {
 				result[i++] = p;
 			}
 		}
 		return result;
 	}
 
 	public String getXO() {
 		if (this.xo ==null) {
 			return " ";
 		}
 		return this.xo;
 	}
 
 	public Move calculateBestMove (Game g, int recursionDepth) {
 		return this.rankBestMoves(g, recursionDepth)[0];
 	}
 
 	public Move[] rankBestMoves (Game g, int recursionDepth) {
 		//creates array to hold all possible moves
 		Move[] moves = this.getAllMoves(g.getGameBoard());
 
 		//creates array to hold values of boards
 		double[] boardValues = new double[moves.length];
 
 		//if recursionDepth is one, calculate direct values of moves
 		if (recursionDepth==1) {
 			//iterates over all moves and calculates values to put in boardValues
 			for (int i = 0; i<moves.length; i++) {
 				boardValues[i] = (new Board(g.getGameBoard(), moves[i])).calculateValue(this);
 			}
 
 		//recursionDepth must be greater than one, so get values of the best opponent moves for each possible move
 		} else {
 			//iterates over all moves and calculates value based on best opponent move
 			for (int i = 0; i<moves.length; i++) {
 				boardValues[i] = -g.getOtherPlayer(this).valueOfBestMove(new Game(g, moves[i]), recursionDepth-1);
 			}
 		}
 
 		//creates array to hold sorted values from lowest to highest
 		double[] boardValuesSorted = Arrays.copyOf(boardValues, boardValues.length);
 		Arrays.sort(boardValuesSorted);
 
 		//creates array to hold sorted values from lowest to highest
 		// Double[] boardValuesSorted = new Double[boardValues.length];
 		// //fills new array with old values
 		// for (int i=0; i<boardValues.length; i++) {
 		// 	boardValuesSorted[i] = boardValues[i];
 		// } 
 		// //sorts the values
 		// Arrays.sort(boardValuesSorted, Collections.reverseOrder());
 
 		//creates array to hold final list of moves
 		Move[] sortedMoves = new Move[moves.length];
 		//creates variable to hold index of move in original list
 		int index = 0;
 		//iterates over all values of boardValuesSorted
 		for (int i = 0; i<boardValuesSorted.length; i++) {
 			//finds the index of the current values in the original move list
 			index = ArraysHelper.find(boardValues, boardValuesSorted[i]);
 			//sets the value at index to -1 so that the same move is not used again, even if multiple moves have equal values
			boardValues[index] = -1;
 
 			//puts the correct move in the correct position in the final array
			sortedMoves[moves.length-index-1] = moves[index];
 
 			//alternate line
 			// sortedMoves[i] = moves[index];
 		}
 
 		//logs values for debugging
 		g.getGameBoard().printBoard();
 		for (Move m : sortedMoves) {
 			System.out.println(Arrays.deepToString(m.getWaypoints()));
 		}
 		System.out.print("" + recursionDepth + " ");
 		System.out.println(Arrays.toString(boardValuesSorted));
 
 		return sortedMoves;
 	}
 
 	public double valueOfBestMove(Game g, int recursionDepth) {
 		if (g.isDraw()) {
 			System.out.println(Arrays.deepToString(g.getLastFewMoves()));
 			System.out.println("Detected possible draw");
 			return 1;
 		}
 
 		//creates array to hold all possible moves
 		Move[] moves = this.getAllMoves(g.getGameBoard());
 
 		if (moves.length==0) {
 			return -2*Board.maxBoardValue;
 		}
 
 		//creates array to hold values of boards
 		double[] boardValues = new double[moves.length];
 
 		//if recursionDepth is one, calculate direct values of moves
 		if (recursionDepth==1) {
 			//iterates over all moves and calculates values to put in boardValues
 			for (int i = 0; i<moves.length; i++) {
 				boardValues[i] = (new Board(g.getGameBoard(), moves[i])).calculateValue(this);
 			}
 		
 		//recursionDepth must be greater than one, so get values of the best opponent moves for each possible move
 		} else {
 			//iterates over all moves and calculates value based on best opponent move
 			for (int i = 0; i<moves.length; i++) {
 				boardValues[i] = -g.getOtherPlayer(this).valueOfBestMove(new Game(g, moves[i]), recursionDepth-1);
 			}
 		}
 
 		//creates array to hold sorted values from lowest to highest
 		double[] boardValuesSorted = Arrays.copyOf(boardValues, boardValues.length);
 		Arrays.sort(boardValuesSorted);
 
 		//creates array to hold sorted values from lowest to highest
 		// Double[] boardValuesSorted = new Double[boardValues.length];
 		// //fills new array with old values
 		// for (int i=0; i<boardValues.length; i++) {
 		// 	boardValuesSorted[i] = boardValues[i];
 		// } 
 		// //sorts the values
 		// Arrays.sort(boardValuesSorted, Collections.reverseOrder());
 
 		//creates variable to hold result value
 		double result = boardValuesSorted[boardValuesSorted.length-1] * 7;
 		//iterates over all values except the last
 		for (int i = 0; i<boardValuesSorted.length-1; i++) {
 			result += boardValuesSorted[i] * 3/(boardValuesSorted.length-1);
 		}
 
 		//logs the values for debugging
 		g.getGameBoard().printBoard();
		Move[] sortedMoves = new Move[moves.length];
		int index = 0;
		for (int i = 0; i<boardValuesSorted.length; i++) {
			index = ArraysHelper.find(boardValues, boardValuesSorted[i]);
			boardValues[index] = -1;
			sortedMoves[i] = moves[index];
		}
		for (Move m : sortedMoves) {
			System.out.println(Arrays.deepToString(m.getWaypoints()));
		}
 		System.out.print("" + recursionDepth + " ");
 		System.out.println(Arrays.toString(boardValuesSorted));
 		System.out.println(result / 10);
 
 		return result / 10;
 	}
 
 	public static void performMove(Move myMove, Board theBoard) {
 		myMove.getMovePiece().setLocation(myMove.getDestination());
 		if (myMove.getMovePiece().getPlayer().getIsOnZeroSide()) {
 			if (myMove.getMovePiece().getLocation()[1]==7) {
 				myMove.getMovePiece().setIsKing(true);
 			}
 		} else {
 			if (myMove.getMovePiece().getLocation()[1]==0) {
 				myMove.getMovePiece().setIsKing(true);
 			}
 		}
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
 
 	public Move[] getAllMoves (Board b) {
 		//creates an ArrayList to return later
 		ArrayList<Move> result = new ArrayList<Move>();
 		//iterates over each of the player's pieces
 		for (Piece playerPiece : this.getPlayerPieces(b)) {
 			//iterates over all of that piece's moves
 			for (Move pieceMove : playerPiece.getMovesOfPiece()) {
 				//adds each move to the return ArrayList
 				result.add(pieceMove);
 			}
 		}
 
 		//returns the final result
 		return result.toArray(new Move[result.size()]);
 	}
 
 	public abstract Move takeTurn(Game g);
 }
