 package eighteen;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Board {
 
 	public static class BadMoveException extends Exception {
 		public BadMoveException(String message){
 			super(message);
 		}
 	}
 	
 	public static class BadBoardException extends Exception {
 		public BadBoardException() {
 			super("Board doesn't have allowable number of rows or columns");
 		}
 	}
 	
 	Piece[][] theBoard;
 	int blacks;
 	int whites;
 	int moves;
 	boolean chain;
 	Color chainColor;
 	Piece previousSpot;
 	Direction previousDirection;
 	ArrayList<Piece> previousLocations;
 	ArrayList<Move> chainMoves;
 	
 	public static int ROWS;
 	public static int COLUMNS;
 	
 	public Board() {
 		this(5,13);
 	}
 	
 	public Board(int rows, int columns)
 	{ 
 		ROWS = rows;
 		COLUMNS = columns;
 		theBoard = new Piece[ROWS][COLUMNS];
 		for(int i = 0; i < ROWS; i++) {
 			for(int j = 0; j < COLUMNS; j++) {
 				//The middle row
 				if(i == (ROWS/2) + 1) {
 					if(j == (COLUMNS/2) + 1) {
 						theBoard[i][j] = new Piece(i, j, Color.GRAY);
 					}
 					else if(j % 2 == 0) {
 						theBoard[i][j] = new Piece(i, j, Color.WHITE);
 					}
 					else {
 						theBoard[i][j] = new Piece(i, j, Color.BLACK);
 					}
 				}
 				else if(i < ROWS/2) {
 					theBoard[i][j] = new Piece(i, j, Color.BLACK);
 				}
 				else {
 					theBoard[i][j] = new Piece(i, j, Color.WHITE);
 				}
 			}
 		}
 		whites = (rows * columns) / 2;
 		blacks = whites;
 		chainMoves = new ArrayList<Move>();
 	}
 	
 	public Board(Board b) {
 		ROWS = b.ROWS;
 		COLUMNS = b.COLUMNS;
 		theBoard = new Piece[b.ROWS][b.COLUMNS];
 		for(int i = 0; i < ROWS; i++) {
 			System.arraycopy(b.theBoard, 0, theBoard, 0, COLUMNS);
 		}
 		blacks = b.blacks;
 		whites = b.whites;
 		moves = b.moves;
 		chain = b.chain;
 		chainColor = b.chainColor;
 		previousSpot = b.previousSpot;
 		previousDirection = b.previousDirection;
 		previousLocations = new ArrayList<Piece>(b.previousLocations);
 		chainMoves = new ArrayList<Move>(b.chainMoves);
 	}
 	
 	public Piece getPiece(int x, int y) {
 		return theBoard[x][y];
 	}
 	//public void setPieceColor(int x, int y, Color color) {
 	//	(theBoard[x][y]).setColor(color);
 	//}
 	
 	public int Utility() {
 		return whites - blacks;
 	}
 	private void resetBoard() {
 		Board board = new Board(ROWS, COLUMNS);
 		moves = 0;
 		
 		chain = false;
 		//The first mover is white
 		chainColor = Color.WHITE;
 		//0,0 will never be a problem for a first move in the middle, which will reset it
 		previousSpot = new Piece(0,0,Color.GRAY);
 		//As white moves first, this will be reset and not be a problem
 		previousDirection = Direction.LEFT;
 		previousLocations = new ArrayList<Piece>();
 	}
 	
 	//Returns whether the game is over
	public void move(Move mov) throws BadMoveException {
 		if(!isValidMove(mov)) {
 			throw new BadMoveException("Bad move at [" + mov.getStart().row + ", " + mov.getStart().column + "] to [" + mov.getEnd().row + ", " + mov.getEnd().column + "]");
 		}
 		
 		boolean isChain = true;
 		
 		//Checks for errors in chaining
 		if(chain) {
 			if(mov.getStart().getColor() != chainColor) {
 				isChain = false;
 				previousLocations = new ArrayList<Piece>();
 				chainColor = mov.getStart().getColor();
 				chainMoves = new ArrayList<Move>();
 			}
 			else if(mov.getStart() != previousSpot) {
 				throw new BadMoveException("Bad move at [" + mov.getStart().row + ", " + mov.getStart().column + "] -> Wrong starting spot");
 			}
 			else if(previousLocations.contains(mov.getEnd())) {
 				throw new BadMoveException("Bad move at [" + mov.getStart().row + ", " + mov.getStart().column + "] -> That space has already been moved to in this chain");
 			}
 			else if(previousDirection == mov.getDirection()) {
 				throw new BadMoveException("Bad move at [" + mov.getStart().row + ", " + mov.getStart().column + "] -> Same direction as previous move in chain");
 			}
 		}
 		chain = isChain;
 		
 		//Move the actual piece
 		theBoard[mov.getEnd().row][mov.getEnd().column].setColor(mov.getStart().getColor());
 		theBoard[mov.getStart().row][mov.getStart().column].setColor(Color.GRAY);
 		
 		int iterateVertical = 0;
 		int iterateHorizontal = 0;
 		switch(mov.getDirection()) {
 		case UP:
 			iterateVertical = -1;
 			break;
 		case UPRIGHT:
 			iterateVertical = -1;
 			iterateHorizontal = 1;
 			break;
 		case UPLEFT:
 			iterateVertical = -1;
 			iterateHorizontal = -1;
 			break;
 		case DOWN:
 			iterateVertical = 1;
 			break;
 		case DOWNRIGHT:
 			iterateVertical = 1;
 			iterateHorizontal = 1;
 			break;
 		case DOWNLEFT:
 			iterateVertical = 1;
 			iterateHorizontal = -1;
 			break;
 		case LEFT:
 			iterateHorizontal = -1;
 			break;
 		case RIGHT:
 			iterateHorizontal = 1;
 		}
 		
 		if(!mov.getAdvancing()) {
 			iterateVertical *= -1;
 			iterateHorizontal *= -1;
 		}
 		
 		//Keeps track of which piece we are looking at
 		int nextRow;
 		int nextColumn;
 		if(mov.getAdvancing()) {
 			nextRow = mov.getEnd().row;
 			nextColumn = mov.getEnd().column;
 		}
 		else {
 			nextRow = mov.getStart().row;
 			nextColumn = mov.getStart().column;
 		}
 		
 		ArrayList<Piece> ret = new ArrayList<Piece>();
 		
 		try {
 			while(true) {
 				nextRow += iterateHorizontal;
 				nextColumn += iterateVertical;
 				if(theBoard[nextRow][nextColumn].getColor() != oppositeColor(mov.getColor())) {
 					break;
 				}
 				theBoard[nextRow][nextColumn].setColor(Color.GRAY);
 				if(chainColor == Color.WHITE) {
 					blacks--;
 				}
 				else {
 					whites--;
 				}
 				ret.add(theBoard[nextRow][nextColumn]);
 			}
 		}
 		finally {}
 		chainColor = mov.getColor();
 		previousSpot = mov.getStart();
 		previousDirection = mov.getDirection();
 		previousLocations.add(mov.getStart());
 		chainMoves.add(mov);
 	}
 	
 	public boolean isValidMove(Move mov) {
 		//Space is taken
		Piece.adjLoc end = mov.getEnd();
		if(getPiece(end.row, end.column).getColor() != Color.GRAY) {
 			return false;
 		}
		if(Piece.isValidSpace(mov.getStart().row, mov.getStart().column) && !mov.getStart().equals(mov.getEnd())) {
 			//Checks for Diagonal moves
			if(mov.getStart().adjacentLocations.contains(end)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/* Gets all valid moves for a specific point.
 	 * Doesn't take into account the state of the entire board.
 	 * (e.g. If a capture is possible at another location on the board,
 	 *       this can still return a paika move)
 	 */
 	public List<Move> getValidMoves(int x, int y) throws BadMoveException {
 		ArrayList<Move> capture = new ArrayList<Move>();
 		ArrayList<Move> paika = new ArrayList<Move>();
 		Piece start = getPiece(x,y);
 		
 		// In case the point doesn't have a piece there
 		if(start.isEmpty())
 			return capture;
 		
 		// Local move to get the direction of each potential move
 		Move move = new Move();
 		move.setStart(start);
 		for(Piece.adjLoc end: start.adjacentLocations) {
 			// Updates the direction
 			move.setEnd(end);
 			move.updateDirection();
 			int rowAdv = 0;
 			int rowWd = 0;
 			int colAdv = 0;
 			int colWd = 0;
 			if(isValidMove(move)) {
 				Piece advance;
 				Piece withdraw;
 				// Sets the locations of the pieces for an advance and a withdraw
 				switch(move.getDirection()) {
 				case UP:
 					rowAdv = -2;
 					colAdv = 0;
 					rowWd = 1;
 					colWd = 0;
 					break;
 				case UPRIGHT:
 					rowAdv = -2;
 					colAdv = 2;
 					rowWd = 1;
 					colWd = -1;
 					break;
 				case UPLEFT:
 					rowAdv = -2;
 					colAdv = -2;
 					rowWd = 1;
 					colWd = 1;
 					break;
 				case DOWN:
 					rowAdv = 2;
 					colAdv = 0;
 					rowWd = -1;
 					colWd = 0;
 					break;
 				case DOWNRIGHT:
 					rowAdv = 2;
 					colAdv = 2;
 					rowWd = -1;
 					colWd = -1;
 					break;
 				case DOWNLEFT:
 					rowAdv = 2;
 					colAdv = -2;
 					rowWd = -1;
 					colWd = 1;
 					break;
 				case LEFT:
 					rowAdv = 0;
 					colAdv = -2;
 					rowWd = 0;
 					colWd = 1;
 					break;
 				case RIGHT:
 					rowAdv = 0;
 					colAdv = 2;
 					rowWd = 0;
 					colWd = -1;
 					break;
 				}
 				// Accounts for if an advance AND a withdraw are possible
 				if(Piece.isValidSpace(start.row + rowAdv, start.column + colAdv) && Piece.isValidSpace(start.row + rowWd, start.column + colWd)) {
 					advance = theBoard[start.row + rowAdv][start.column + colAdv];
 					withdraw = theBoard[start.row + rowWd][start.column + colWd];
 					if(advance.getColor() != move.getColor() && !advance.isEmpty()) {
 						Move newMove = new Move(start, end, true, true);
 						capture.add(newMove);
 					}
 					if(withdraw.getColor() != move.getColor() && !withdraw.isEmpty()) {
 						Move newMove = new Move(start, end, false, true);
 						capture.add(newMove);
 					}
 				}
 				// Just an advance is possible
 				else if(Piece.isValidSpace(start.row + rowAdv, start.column + colAdv)) {
 					advance = theBoard[start.row + rowAdv][start.column + colAdv];
 					if(advance.getColor() != move.getColor() && !advance.isEmpty()) {
 						Move newMove = new Move(start, end, false, true);
 						capture.add(newMove);
 					}
 				}
 				// Just a withdraw is possible
 				else if(Piece.isValidSpace(start.row + rowWd, start.column + colWd)) {
 					withdraw = theBoard[start.row + rowWd][start.column + colWd];
 					if(withdraw.getColor() != move.getColor() && !withdraw.isEmpty()) {
 						Move newMove = new Move(start, end, true, true);
 						capture.add(newMove);
 					}
 				}
 				// If no captures have been found, a paika is possible
 				else if(capture.isEmpty()) {
 						Move newMove = new Move(start, end, false, false);
 						paika.add(newMove);
 				}
 			}
 		}
 		if(!capture.isEmpty())
 			return capture;
 		else
 			return paika;
 	}
 	
 	// Gets valid moves for a specific color on the entire board
 	public List<Move> getValidMoves(Color color) throws BadMoveException {
 		ArrayList<Move> capture = new ArrayList<Move>();
 		ArrayList<Move> paika = new ArrayList<Move>();
 		
 		for(int x=0; x < Board.ROWS; x++) {
 			for(int y=0; y < Board.COLUMNS; y++) {
 				Piece piece = getPiece(x, y);
 				Move move = new Move();
 				if(piece.getColor() == color) {
 					move.setStart(piece);
 					for(Piece.adjLoc end: piece.adjacentLocations) {
 						move.setEnd(end);
 						move.updateDirection();
 						int rowAdv = 0;
 						int rowWd = 0;
 						int colAdv = 0;
 						int colWd = 0;
 						if(isValidMove(move)) {
 							Piece advance;
 							Piece withdraw;
 							switch(move.getDirection()) {
 							case UP:
 								rowAdv = -2;
 								colAdv = 0;
 								rowWd = 1;
 								colWd = 0;
 								break;
 							case UPRIGHT:
 								rowAdv = -2;
 								colAdv = 2;
 								rowWd = 1;
 								colWd = -1;
 								break;
 							case UPLEFT:
 								rowAdv = -2;
 								colAdv = -2;
 								rowWd = 1;
 								colWd = 1;
 								break;
 							case DOWN:
 								rowAdv = 2;
 								colAdv = 0;
 								rowWd = -1;
 								colWd = 0;
 								break;
 							case DOWNRIGHT:
 								rowAdv = 2;
 								colAdv = 2;
 								rowWd = -1;
 								colWd = -1;
 								break;
 							case DOWNLEFT:
 								rowAdv = 2;
 								colAdv = -2;
 								rowWd = -1;
 								colWd = 1;
 								break;
 							case LEFT:
 								rowAdv = 0;
 								colAdv = -2;
 								rowWd = 0;
 								colWd = 1;
 								break;
 							case RIGHT:
 								rowAdv = 0;
 								colAdv = 2;
 								rowWd = 0;
 								colWd = -1;
 								break;
 							}
 							if(Piece.isValidSpace(piece.row + rowAdv, piece.column + colAdv) && Piece.isValidSpace(piece.row + rowWd, piece.column + colWd)) {
 								advance = theBoard[piece.row + rowAdv][piece.column + colAdv];
 								withdraw = theBoard[piece.row + rowWd][piece.column + colWd];
 								if(advance.getColor() != move.getColor() && !advance.isEmpty()) {
 									Move newMove = new Move(piece, end, true, true);
 									capture.add(newMove);
 								}
 								if(withdraw.getColor() != move.getColor() && !withdraw.isEmpty()) {
 									Move newMove = new Move(piece, end, false, true);
 									capture.add(newMove);
 								}
 							}
 							else if(Piece.isValidSpace(piece.row + rowAdv, piece.column + colAdv)) {
 								advance = theBoard[piece.row + rowAdv][piece.column + colAdv];
 								if(advance.getColor() != move.getColor() && !advance.isEmpty()) {
 									Move newMove = new Move(piece, end, false, true);
 									capture.add(newMove);
 								}
 							}
 							else if(Piece.isValidSpace(piece.row + rowWd, piece.column + colWd)) {
 								withdraw = theBoard[piece.row + rowWd][piece.column + colWd];
 								if(withdraw.getColor() != move.getColor() && !withdraw.isEmpty()) {
 									Move newMove = new Move(piece, end, true, true);
 									capture.add(newMove);
 								}
 							}
 							else if(capture.isEmpty()) {
 									Move newMove = new Move(piece, end, false, false);
 									paika.add(newMove);
 							}
 						}
 					}
 				}
 			}
 		}
 		if(!capture.isEmpty())
 			return capture;
 		else
 			return paika;
 	}
 	
 	public static Color oppositeColor(Color color) {
 		if(color == Color.GRAY) {
 			return Color.GRAY;
 		}
 		else if(color == Color.WHITE) {
 			return Color.BLACK;
 		}
 		else {
 			return Color.WHITE;
 		}
 	}
 }
