 package DerpyAI;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Scanner;
 
 import sharedfiles.Piece;
 
 public class DerpyAI {
 	// their pieces
 	private ArrayList<Move> allMoves;
 	private ArrayList<DerpyBoard> boardStore; // The current, and all previous
 	protected DerpyBoard currentBoard; // currentBoard is the current chess
 	private Boolean myColor; // black is false, white is true.
 	// boards
 	private int numTurns;
 	public ArrayList<DerpyPiece> ourPieces; // Our Array of Pieces
 	// board
 	public ArrayList<Point> ourPiecesPoints; // array of the locations of our
 	private ArrayList<DerpyPiece> takenPieces; // The pieces we took
 	public ArrayList<DerpyPiece> theirPieces; // Our Array of their Pieces
 	// pieces
 	public ArrayList<Point> theirPiecesPoints; // array of the locations of
 	Boolean covered; 
 
 	// A new constructor that doesn't take a board, just a color. This is
 	// because moves/board parsing
 	// will for now on be handled by makeMove/parseCurrentBoard, etc
 
 	public DerpyAI(Boolean b) {
 		numTurns = 1;
 		myColor = b;
 		boardStore = new ArrayList<DerpyBoard>();
 		takenPieces = new ArrayList<DerpyPiece>();
 		ourPieces = new ArrayList<DerpyPiece>();
 		theirPieces = new ArrayList<DerpyPiece>();
 		currentBoard = new DerpyBoard();
 		theirPiecesPoints = new ArrayList<Point>();
 		ourPiecesPoints = new ArrayList<Point>();
 		allMoves = new ArrayList<Move>();
 		findOurPieces();
 		findTheirPieces();
 		covered = true; 
 	}
 
 	public void concedeGame() {
 		System.out.println("DerpyAI has lost the game.");
 		System.exit(0); // Exit with terminated status 0
 	}
 
 	// returns an arraylist of our pieces that are threatened by an enemy piece
 	public ArrayList<DerpyPiece> enemyThreats(DerpyBoard b) {
 		ArrayList<DerpyPiece> ourThreatenedPieces = new ArrayList<DerpyPiece>();
 		for (int i = 0; i < 8; i++) {
 			for (int j = 0; j < 8; j++) {
 				if (isPieceOurs(b.getBoardArray()[i][j])) {
 					if (pieceIsThreatened(b.getBoardArray()[i][j])) {
 						ourThreatenedPieces.add(b.getBoardArray()[i][j]);
 					}
 				}
 			}
 		}
 
 		return ourThreatenedPieces;
 
 	}
 
 	// returns an arraylist of points that can be occupied to block theirs from
 	// capturing ours
 	public ArrayList<Point> findBlockablePoints(DerpyPiece ours,
 			DerpyPiece theirs) {
 		ArrayList<Point> points = new ArrayList<Point>();
 		if (theirs instanceof DerpyKnight || theirs instanceof DerpyPawn || theirs instanceof DerpyKing) {
 			return points;
 		}
 		if (theirs.getLocation().distance(ours.getLocation()) < 1.5) {
 			return points;
 		}
 		if (theirs instanceof DerpyRook || theirs instanceof DerpyQueen) {
 			if (theirs.getLocation().getX() == ours.getLocation().getX()) {
 				if (theirs.getLocation().getY() > ours.getLocation().getY()) {
 					for (double i = theirs.getLocation().getY() - 1; i > ours
 							.getLocation().getY(); i--) {
 						Point ourPoint = new Point((int) theirs.getLocation()
 								.getX(), (int) i);
 						points.add(ourPoint);
 					}
 				}
 				if (theirs.getLocation().getY() < ours.getLocation().getY()) {
 					for (double i = theirs.getLocation().getY() + 1; i < ours
 							.getLocation().getY(); i++) {
 						Point ourPoint = new Point((int) theirs.getLocation()
 								.getX(), (int) i);
 						points.add(ourPoint);
 					}
 				}
 			}
 			if (theirs.getLocation().getY() == ours.getLocation().getY()) {
 				if (theirs.getLocation().getX() > ours.getLocation().getX()) {
 					for (double i = theirs.getLocation().getX() - 1; i > ours
 							.getLocation().getX(); i--) {
 						Point ourPoint = new Point((int) i, (int) theirs
 								.getLocation().getY());
 						points.add(ourPoint);
 					}
 				}
 				if (theirs.getLocation().getX() < ours.getLocation().getX()) {
 					for (double i = theirs.getLocation().getX() + 1; i < ours
 							.getLocation().getX(); i++) {
 						Point ourPoint = new Point((int) i, (int) theirs
 								.getLocation().getY());
 						points.add(ourPoint);
 					}
 				}
 			}
 
 		}
 
 		if (theirs instanceof DerpyBishop || theirs instanceof DerpyQueen) {
 			if (theirs.getLocation().getX() > ours.getLocation().getX()) {
 				if (theirs.getLocation().getY() < ours.getLocation().getY()) {
 					for (double i = theirs.getLocation().getX() - 1; i > ours
 							.getLocation().getX(); i--) {
 						for (double j = theirs.getLocation().getY() + 1; j < ours
 								.getLocation().getY(); j++) {
 							Point ourPoint = new Point((int) i, (int) j);
 							points.add(ourPoint);
 						}
 					}
 				}
 				if (theirs.getLocation().getY() > ours.getLocation().getY()) {
 					for (double i = theirs.getLocation().getX() - 1; i > ours
 							.getLocation().getX(); i--) {
 						for (double j = theirs.getLocation().getY() - 1; j > ours
 								.getLocation().getY(); j--) {
 							Point ourPoint = new Point((int) i, (int) j);
 							points.add(ourPoint);
 
 						}
 					}
 				}
 			}
 			if (theirs.getLocation().getX() < ours.getLocation().getX()) {
 				if (theirs.getLocation().getY() < ours.getLocation().getY()) {
 					for (double i = theirs.getLocation().getX() + 1; i < ours
 							.getLocation().getX(); i++) {
 						for (double j = theirs.getLocation().getY() + 1; j < ours
 								.getLocation().getY(); j++) {
 							Point ourPoint = new Point((int) i, (int) j);
 							points.add(ourPoint);
 						}
 					}
 				}
 				if (theirs.getLocation().getY() > ours.getLocation().getY()) {
 					for (double i = theirs.getLocation().getX() + 1; i < ours
 							.getLocation().getX(); i++) {
 						for (double j = theirs.getLocation().getY() - 1; j > ours
 								.getLocation().getY(); j--) {
 							Point ourPoint = new Point((int) i, (int) j);
 							points.add(ourPoint);
 						}
 					}
 				}
 			}
 		}
 		return points;
 	}
 
 	// returns the enemy king
 	public DerpyPiece findEnemyKing() {
 		for (DerpyPiece p : theirPieces) {
 			if (p instanceof DerpyKing) {
 				return p;
 			}
 		}
 		return theirPieces.get(1);
 	}
 
 	public DerpyPiece findOurKing() {
 		for (DerpyPiece p : ourPieces) {
 			if (p instanceof DerpyKing) {
 				return p;
 			}
 		}
 		return null;
 	}
 
 	public void findOurPieces() { // Creates an array of our pieces
 		DerpyPiece[][] boardState = currentBoard.getBoardArray();
 		for (int i = 0; i < 8; i++) {
 			for (int a = 0; a < 8; a++) {
 				if (isPieceOurs(boardState[i][a])) {
 					ourPieces.add(boardState[i][a]);
 				}
 			}
 		}
 	}
 
 	public void findOurPiecesPoints() { // Creates an array of our pieces'
 		// locations
 		DerpyPiece[][] boardState = currentBoard.getBoardArray();
 		for (int i = 0; i < 8; i++) {
 			for (int a = 0; a < 8; a++) {
 				Point currentPoint = new Point(i, a);
 				if (isPieceOurs(boardState[i][a])) {
 					ourPiecesPoints.add(currentPoint);
 				}
 			}
 		}
 	}
 
 	public void findTheirPieces() { // Creates an array of their pieces
 		DerpyPiece[][] boardState = currentBoard.getBoardArray();
 		for (int i = 0; i < 8; i++) {
 			for (int a = 0; a < 8; a++) {
 				if (!isPieceOurs(boardState[i][a])) {
 					theirPieces.add(boardState[i][a]);
 				}
 			}
 		}
 	}
 
 	public void findTheirPiecesPoints() { // Creates an array of their pieces'
 		// locations
 		DerpyPiece[][] boardState = currentBoard.getBoardArray();
 		for (int i = 0; i < 8; i++) {
 			for (int a = 0; a < 8; a++) {
 				Point currentPoint = new Point(i, a);
 				if (!isPieceOurs(boardState[i][a])) {
 					theirPiecesPoints.add(currentPoint);
 				}
 			}
 		}
 	}
 
 	// returns the most valuable piece in an arraylist of pieces
 	public DerpyPiece findValuablePiece(ArrayList<DerpyPiece> listOfPieces) {
 		Point genericPoint = new Point(0, 0);
 		DerpyPiece biggestValue = new DerpyPawn(true, genericPoint);
 		for (DerpyPiece p : listOfPieces) {
 			if (biggestValue instanceof DerpyPawn) {
 				if (!(p instanceof DerpyPawn)) {
 					biggestValue = p;
 				}
 			}
 			if (biggestValue instanceof DerpyKnight
 					|| biggestValue instanceof DerpyBishop) {
 				if (p instanceof DerpyQueen || p instanceof DerpyRook) {
 					biggestValue = p;
 				}
 			}
 			if (biggestValue instanceof DerpyRook) {
 				if (p instanceof DerpyQueen) {
 					biggestValue = p;
 				}
 			}
 		}
 		for (DerpyPiece p : listOfPieces) {
 			if (p.toString().charAt(1) == biggestValue.toString().charAt(1)) {
 				return p;
 			}
 		}
 		return biggestValue;
 	}
 
 	// makes a move to get out of check
 	public DerpyBoard getOutOfCheck(DerpyBoard b) {
 		// tries to move the king out of check
 		for (int i = 0; i < ourPieces.size(); i++) {
 			if (ourPieces.get(i) instanceof DerpyKing) {
 				ArrayList<Point> listOfPoints = movablePoints(ourPieces
 						.get(i));
 				for (int j = 0; j < listOfPoints.size(); j++) {
 					if (pieceCanMoveToPosition(ourPieces.get(i),
 							listOfPoints.get(j))) {
 						DerpyBoard storeBoard = currentBoard;
 						currentBoard = movePiece(ourPieces.get(i),
 								listOfPoints.get(j));
 						parseCurrentBoard();
 						if (inCheck()) {
 							currentBoard = storeBoard;
 							return movePiece(ourPieces.get(i),
 									listOfPoints.get(j));
 						}
 						currentBoard = storeBoard;
 					}
 				}
 			}
 		}
 		// tries to take the threatening piece
 		for (int i = 0; i < ourPieces.size(); i++) {
 			if (ourPieces.get(i) instanceof DerpyKing) {
 				DerpyPiece ourKing = ourPieces.get(i);
 				if (threateningPiecesToUs(ourKing).size() == 1) {
 					DerpyPiece threat = threateningPiecesToUs(ourKing)
 							.get(0);
 					if (threateningPiecesToThem(threat).size() >= 1) {
 						DerpyPiece taker = threateningPiecesToThem(threat)
 								.get(0);
 						DerpyBoard storeBoard = currentBoard;
 						movePiece(taker, threat.getLocation());
 						if (inCheck()) {
 							currentBoard = storeBoard;
 							return movePiece(taker, threat.getLocation());
 						}
 						currentBoard = storeBoard;
 					}
 				}
 
 			}
 		}
 		for (int i = 0; i < ourPieces.size(); i++) {
 			if (ourPieces.get(i) instanceof DerpyKing) {
 				DerpyPiece ourKing = ourPieces.get(i);
 				ArrayList<DerpyPiece> threats = threateningPiecesToUs(ourKing);
 				if (threats.size() == 1) {
 					ArrayList<Point> betweenSpaces = findBlockablePoints(
 							ourKing, threats.get(0));
 					for (Point p : betweenSpaces) {
 						for (DerpyPiece c : ourPieces) {
 							if (pieceCanMoveToPosition(c, p)) {
 								DerpyBoard storeBoard = currentBoard;
 								movePiece(c, p);
 								if (inCheck()) {
 									currentBoard = storeBoard;
 									return movePiece(c, p);
 								}
 								currentBoard = storeBoard;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// this.concedeGame();
 		return currentBoard;
 	}
 
 	// Returns if the king is in check
 	public boolean inCheck() {
 		parseCurrentBoard();
 		for (DerpyPiece x : ourPieces) {
 			if (x instanceof DerpyKing) {
 				if (pieceIsThreatened(x)) {
 					return true;
 				}
 			}
 		}
 		// System.out.println("THIS SHOULD NEVER RUN");
 		return false;
 	}
 
 	// checks if a piece is ours
 	public boolean isPieceOurs(DerpyPiece p) {
 		if (myColor == p.getColor() && !(p instanceof DerpyBlank)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public DerpyBoard makeMove(DerpyBoard b) {
 
 		/*
 		 * if (wereInCheckmate() || !weHaveOurKingStill()) {
 		 * System.out.println("DerpyAI has lost....press enter to continue.");
 		 * sc = new Scanner(System.in); while (!sc.nextLine().equals("")) ; }
 		 * 
 		 * if (theyreInCheckmate()) {
 		 * System.out.println("DerpyAI has won....press enter to continue."); sc
 		 * = new Scanner(System.in); while (!sc.nextLine().equals("")); }
 		 */
 //		System.out.println(trashTalk()); 
 //		System.out.println("We're in check: " + inCheck() + " –– mate: "
 //				+ wereInCheckmate());
 //		System.out.println("They're in check: " + theyreinCheck()
 //				+ " -- mate: " + theyreInCheckmate());
 
 		boardStore.add(b);
 		currentBoard = b;
 		parseCurrentBoard();
 
 		// DerpyBoard ba = this.prestonAI();
 		DerpyBoard ba = samAI("Normal");
 		// DerpyBoard ba = this.curtisAI();
 
 		ba = promoteIfPossible(ba);
 
 		boardStore.add(ba);
 		// ba.printBoard();
 
 		// If we're still in check even after all that,
 		// there's no way out of check. Concede to the other player.
 		if (inCheck()) {
 			ba = getOutOfCheck(ba);
 		}
 
 		currentBoard = ba;
 		parseCurrentBoard();
 		return ba;
 	}
 
 	// checks if the defender is more valuable than attacker, and returns true
 	// if the defender
 	// is worth more. If this method returns true, we should make the move.
 	public boolean makeTrade(DerpyPiece attacker, DerpyPiece defender) {
 		if (defender instanceof DerpyKing) {
 			return true;
 		}
 		if (defender instanceof DerpyQueen) {
 			if (attacker instanceof DerpyKing) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 		if (defender instanceof DerpyRook) {
 			if (attacker instanceof DerpyQueen || attacker instanceof DerpyKing) {
 				return false;
 			} else {
 				return true;
 			}
 
 		}
 		if (defender instanceof DerpyBishop || defender instanceof DerpyKnight) {
 			if (attacker instanceof DerpyRook || attacker instanceof DerpyQueen
 					|| attacker instanceof DerpyKing) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 
 		if (defender instanceof DerpyPawn) {
 			if (attacker instanceof DerpyRook || attacker instanceof DerpyQueen
 					|| attacker instanceof DerpyKing
 					|| attacker instanceof DerpyBishop
 					|| attacker instanceof DerpyKnight) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// returns an arraylist of points a piece can move to
 	public ArrayList<Point> movablePoints(DerpyPiece p) {
 		ArrayList<Point> listOfPoints = new ArrayList<Point>();
 		for (int i = 0; i < 8; i++) {
 			for (int j = 0; j < 8; j++) {
 				Point moveTo = new Point(i, j);
 				if (pieceCanMoveToPosition(p, moveTo)) {
 					listOfPoints.add(moveTo);
 				}
 			}
 		}
 		return listOfPoints;
 	}
 
 	public DerpyBoard movePiece(DerpyPiece p, Point mL) {
 
 		DerpyBoard newBoard = new DerpyBoard(currentBoard);
 
 		Point oL = p.getLocation(); // This will access the instance data in the
 		// piece class that contain its location.
 
 		// Edit the _*PIECE*_ so it knows where it, itself it now
 		p.changeLocation(mL);
 
 		// Edit the _*BOARD*_ so it knows where the pieces are now
 		newBoard.getBoardArray()[(int) oL.getX()][(int) oL.getY()] = new DerpyBlank(
 				oL); // Put a blank piece in the old location
 
 		newBoard.getBoardArray()[(int) mL.getX()][(int) mL.getY()] = p;
 
 		Move m = new Move(myColor, p, oL, mL);
 		allMoves.add(m);
 
 		parseCurrentBoard();
 		return newBoard;
 	}
 
 	// returns an arraylist of enemy pieces that we threaten
 	public ArrayList<DerpyPiece> ourThreats(DerpyBoard b) {
 		ArrayList<DerpyPiece> theirThreatenedPieces = new ArrayList<DerpyPiece>();
 		for (int i = 0; i < 8; i++) {
 			for (int j = 0; j < 8; j++) {
 				if (!isPieceOurs(b.getBoardArray()[i][j])) {
 					if (pieceIsThreatened(b.getBoardArray()[i][j])) {
 						theirThreatenedPieces.add(b.getBoardArray()[i][j]);
 					}
 				}
 			}
 		}
 
 		return theirThreatenedPieces;
 	}
 
 	protected void parseCurrentBoard() {
 
 		// This method should not modify the following pieces of instance data
 		// 1. currentBoard
 		// 2. boardStore
 
 		ourPieces = new ArrayList<DerpyPiece>();
 		findOurPieces();
 
 		theirPieces = new ArrayList<DerpyPiece>();
 		findTheirPieces();
 
 		theirPiecesPoints = new ArrayList<Point>();
 		ourPiecesPoints = new ArrayList<Point>();
 		findTheirPiecesPoints();
 		findOurPiecesPoints();
 
 		// This method takes the currentBoard and makes instance data elements
 		// like ourPieces, etc, be correct
 
 	}
 
 	// uses provided board to make a move, returns a board with the move made
 
 	// asks if a piece can legally move to a position
 	protected boolean pieceCanMoveToPosition(DerpyPiece piece, Point position) {
 
 		int xPos = (int) position.getX();
 		int yPos = (int) position.getY();
 		boolean indicator = false;
 		if (currentBoard.getBoardArray()[xPos][yPos] instanceof DerpyBlank) {
 			indicator = true;
 		}
 		if (!(currentBoard.getBoardArray()[xPos][yPos].getColor() == piece
 				.getColor()) || indicator) {
 
 			if (piece instanceof DerpyKing) {
 				// can only move 1 space
 				if (piece.getLocation().distanceSq(position) == 1
 						|| piece.getLocation().distanceSq(position) == 2) {
 					// makes sure the destination is not occupied by a friendly
 					// piece
 					if (currentBoard.getBoardArray()[xPos][yPos] instanceof DerpyBlank
 							&& currentBoard.getBoardArray()[xPos][yPos]
 									.getColor() == piece.getColor()) {
 						// makes sure moving doesn't put him in check
 						/*
 						 * DerpyBoard oldBoard = currentBoard; DerpyBoard
 						 * testBoard = this.movePiece(piece, position);
 						 * currentBoard = testBoard; if (!(this.inCheck())) {
 						 * currentBoard = oldBoard; return true; } else {
 						 * currentBoard = oldBoard; }
 						 */
 						return true;
 					}
 
 				}
 			}
 			if (piece instanceof DerpyPawn) {
 				// if the pawn is black...
 				if (!piece.getColor()) {
 					// if the pawn wants to move up two spaces and is on its
 					// starting area
 					if (piece.getLocation().getY() == 1 && position.getY() == 3
 							&& piece.getLocation().getX() == xPos) {
 						// can only move if not blocked by another piece
 						if (currentBoard.getBoardArray()[xPos][yPos] instanceof DerpyBlank
 								&& currentBoard.getBoardArray()[xPos][2] instanceof DerpyBlank) {
 							// makes sure moving doesn't put the king in check
 							/*
 							 * DerpyBoard oldBoard = currentBoard; DerpyBoard
 							 * testBoard = this.movePiece(piece, position);
 							 * currentBoard = testBoard; if (!(this.inCheck()))
 							 * { currentBoard = oldBoard; return true; } else {
 							 * currentBoard = oldBoard; }
 							 */
 							return true;
 						}
 					}
 					// if the pawn wants to move up one space
 					if (piece.getLocation().getY() - yPos == -1
 							&& piece.getLocation().getX() == xPos) {
 						// makes sure the space is not blocked
 						if (currentBoard.getBoardArray()[xPos][yPos] instanceof DerpyBlank) {
 							// makes sure moving does not put the king in check
 							/*
 							 * DerpyBoard oldBoard = currentBoard; DerpyBoard
 							 * testBoard = this.movePiece(piece, position);
 							 * currentBoard = testBoard; if (!(this.inCheck()))
 							 * { currentBoard = oldBoard; return true; } else {
 							 * currentBoard = oldBoard; }
 							 */
 							return true;
 						}
 					}
 					// if the pawn wants to take diagonally
 					if ((int) piece.getLocation().getY() == yPos - 1 && (int) piece
 							.getLocation().getX() == xPos - 1
 							|| (int) piece.getLocation().getY() == yPos - 1 && (int) piece
 									.getLocation().getX() == xPos + 1) {
 						// makes sure the space has a takeable piece
 						if (!(currentBoard.getBoardArray()[xPos][yPos] instanceof DerpyBlank)) {
 							if (currentBoard.getBoardArray()[xPos][yPos]
 									.getColor()) {
 								// makes sure moving does not put the king in
 								// check
 								/*
 								 * DerpyBoard oldBoard = currentBoard;
 								 * DerpyBoard testBoard = this.movePiece(piece,
 								 * position); currentBoard = testBoard; if
 								 * (!(this.inCheck())) { currentBoard =
 								 * oldBoard; return true; } else { currentBoard
 								 * = oldBoard; }
 								 */
 								return true;
 							}
 						}
 					}
 				}
 				// if the pawn is white...
 				if (piece.getColor()) {
 					// if the pawn wants to move up two spaces and is on its
 					// starting area
 					if (piece.getLocation().getY() == 6 && position.getY() == 4
 							&& piece.getLocation().getX() == xPos) {
 						// can only move if not blocked by another piece
 						if (currentBoard.getBoardArray()[xPos][yPos] instanceof DerpyBlank
 								&& currentBoard.getBoardArray()[xPos][5] instanceof DerpyBlank) {
 							// makes sure moving doesn't put the king in check
 							/*
 							 * DerpyBoard oldBoard = currentBoard; DerpyBoard
 							 * testBoard = this.movePiece(piece, position);
 							 * currentBoard = testBoard; if (!(this.inCheck()))
 							 * { currentBoard = oldBoard; return true; } else {
 							 * currentBoard = oldBoard; }
 							 */
 							return true;
 						}
 					}
 					// if the pawn wants to move up one space
 					if (piece.getLocation().getY() - yPos == 1
 							&& piece.getLocation().getX() == xPos) {
 						// makes sure the space is not blocked
 						if (currentBoard.getBoardArray()[xPos][yPos] instanceof DerpyBlank) {
 							// makes sure moving does not put the king in check
 							/*
 							 * DerpyBoard oldBoard = currentBoard; DerpyBoard
 							 * testBoard = this.movePiece(piece, position);
 							 * currentBoard = testBoard; if (!(this.inCheck()))
 							 * { currentBoard = oldBoard; return true; } else {
 							 * currentBoard = oldBoard; }
 							 */
 							return true;
 						}
 					}
 					// if the pawn wants to take diagonally
 					if (piece.getLocation().getY() == yPos + 1 && piece
 							.getLocation().getX() == xPos - 1
 							|| piece.getLocation().getY() == yPos + 1 && piece
 									.getLocation().getX() == xPos + 1) {
 						// makes sure the space has a takeable piece
 						if (!(currentBoard.getBoardArray()[xPos][yPos] instanceof DerpyBlank)) {
 							if (!currentBoard.getBoardArray()[xPos][yPos]
 									.getColor()) {
 								// makes sure moving does not put the king in
 								// check
 								/*
 								 * DerpyBoard oldBoard = currentBoard;
 								 * DerpyBoard testBoard = this.movePiece(piece,
 								 * position); currentBoard = testBoard; if
 								 * (!(this.inCheck())) { currentBoard =
 								 * oldBoard; return true; } else { currentBoard
 								 * = oldBoard; }
 								 */
 								return true;
 							}
 						}
 					}
 				}
 
 			}
 			// if the piece is a rook or queen moving on a rank or file
 			if (piece instanceof DerpyRook || piece instanceof DerpyQueen) {
 				DerpyPiece pieceAtDestination = currentBoard.getBoardArray()[xPos][yPos];
 				// destination has to be on the same rank or file
 				if ((int) piece.getLocation().getY() == yPos
 						|| (int) piece.getLocation().getX() == xPos) {
 					// no pieces blocking
 					ArrayList<Point> betweenSpace = findBlockablePoints(
 							pieceAtDestination, piece);
 					for (Point d : betweenSpace) {
 						if (!(currentBoard.getBoardArray()[(int) d.getX()][(int) d
 								.getY()] instanceof DerpyBlank)) {
 							return false;
 						}
 					}
 					// checks if it puts the king in check
 					/*
 					 * DerpyBoard oldBoard = currentBoard; DerpyBoard testBoard
 					 * = this.movePiece(piece, position); currentBoard =
 					 * testBoard; if (!(this.inCheck())) { currentBoard =
 					 * oldBoard; return true; } else { currentBoard = oldBoard;
 					 * }
 					 */
 					return true;
 				}
 			}
 			// if the piece is a bishop or queen moving diagonally
 			if (piece instanceof DerpyBishop || piece instanceof DerpyQueen) {
 				DerpyPiece pieceAtDestination = currentBoard.getBoardArray()[xPos][yPos];
 				// destination has to be on the same diagonal
 				if (Math.abs((int) (piece.getLocation().getY() - yPos)) ==
 						Math.abs((int) (piece.getLocation().getX() - xPos))) {
 					// no pieces blocking
 					ArrayList<Point> betweenSpace = findBlockablePoints(
 							pieceAtDestination, piece);
 					for (Point d : betweenSpace) {
 						if (!(currentBoard.getBoardArray()[(int) d.getX()][(int) d
 								.getY()] instanceof DerpyBlank)) {
 							return false;
 						}
 					}
 
 					return true;
 				}
 			}
 			// for knights
 			if (piece instanceof DerpyKnight) {
 				// destination has to be one of eight destinations around the
 				// knight
 				// that are valid
 				if (!(yPos == piece.getLocation().getY() + 2 && xPos == piece
 						.getLocation().getX() + 1)) {
 					if (!(yPos == piece.getLocation().getY() + 2 && xPos == piece
 							.getLocation().getX() - 1)) {
 						if (!(yPos == piece.getLocation().getY() + 1 && xPos == piece
 								.getLocation().getX() + 2)) {
 							if (!(yPos == piece.getLocation().getY() + 1 && xPos == piece
 									.getLocation().getX() - 2)) {
 								if (!(yPos == piece.getLocation().getY() - 1 && xPos == piece
 										.getLocation().getX() + 2)) {
 									if (!(yPos == piece.getLocation().getY() - 1 && xPos == piece
 											.getLocation().getX() - 2)) {
 										if (!(yPos == piece.getLocation()
 												.getY() - 2 && xPos == piece
 												.getLocation().getX() + 1)) {
 											if (!(yPos == piece.getLocation()
 													.getY() - 2 && xPos == piece
 													.getLocation().getX() - 1)) {
 												return false;
 											}
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 				/*
 				 * DerpyBoard oldBoard = currentBoard; DerpyBoard testBoard =
 				 * this.movePiece(piece, position); currentBoard = testBoard; if
 				 * (!(this.inCheck())) { currentBoard = oldBoard; return true; }
 				 * else { currentBoard = oldBoard; }
 				 */
 				return true;
 			}
 			// We need to get the Piece object at that position
 
 			// Iterate through each Piece to figure out whether there's a piece
 			// at
 			// that position, or is it blank?
 
 			for (Piece p : ourPieces) {
 				DerpyPiece d = (DerpyPiece) p;
 				d.getLocation();
 			}
 		}
 
 		return false;
 	}
	
 	// tells if a piece is protected
 	public boolean pieceIsProtected(DerpyPiece p) {
 		DerpyPiece d = p;
 		for (DerpyPiece a : ourPieces) {
 			if (pieceCanMoveToPosition(a, d.getLocation())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// asks if a piece is threatened
 	public boolean pieceIsThreatened(DerpyPiece p) {
 		parseCurrentBoard();
 		if (p.getColor() != myColor) {
 			for (DerpyPiece a : ourPieces) {
 				if (pieceCanMoveToPosition(a, p.getLocation())) {
 					if (makeTrade(a, p)) {
 						return true;
 					}
 				}
 			}
 			return false;
 		} else if (p.getColor() == myColor) {
 			for (DerpyPiece a : theirPieces) {
 				if (pieceCanMoveToPosition(a, p.getLocation())) {
 					if (makeTrade(a, p)) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public DerpyBoard promoteIfPossible(DerpyBoard ba) {
 
 		// if we're white...
 		if (myColor) {
 			// searches the back row for pawns
 			for (int i = 0; i < 8; i++) {
 				if (ba.getBoardArray()[i][0] instanceof DerpyPawn) {
 					// if it finds one, makes a new queen and puts it where the
 					// pawn was
 					DerpyQueen newQueen = new DerpyQueen(true, new Point(i, 0));
 					ba.getBoardArray()[i][0] = newQueen;
 				}
 			}
 		}
 		// if we're black
 		else if (!myColor) {
 			// searches the back row for pawns
 			for (int i = 0; i < 8; i++) {
 				if (ba.getBoardArray()[i][7] instanceof DerpyPawn) {
 					// if it finds one, makes a new queen and puts it where the
 					// pawn was
 					DerpyQueen newQueen = new DerpyQueen(false, new Point(i, 0));
 					ba.getBoardArray()[i][7] = newQueen;
 				}
 			}
 		}
 		// returns the board, whether or not a pawn was promoted
 		return ba;
 	}
 
 	public DerpyBoard randomMove() {
 		parseCurrentBoard();
 		Point oldDest;
 		// Finds the initial piece to move and the initial destination
 		Random r = new Random();
 		boolean pieceCanMove = false;
 		DerpyPiece randomPiece;
 		ArrayList<Point> destinationArray;
 
 		if (ourPieces.size() <= 0) {
 			System.out
 					.println("randomMove wanted to be called, but we have no pieces left.");
 			System.out.println("Conceding and quitting.");
 			concedeGame();
 
 		}
 
 		// If we aren't in check, we shouldn't move our king
 		for (int i = 0; i < ourPieces.size(); i++) {
 			if (ourPieces.get(i) instanceof DerpyKing) {
 				ourPieces.remove(i);
 			}
 		}
 
 		do {
 			// System.out.println("Piece Array Size: " + ourPieces.size());
 			randomPiece = ourPieces.get(r.nextInt(ourPieces.size()));
 			oldDest = randomPiece.getLocation();
 			// System.out.println("Piece Type: " + randomPiece.toString());
 			destinationArray = movablePoints(randomPiece);
 			if (destinationArray.size() > 0) {
 				pieceCanMove = true;
 			} else {
 				pieceCanMove = false;
 			}
 		} while (!pieceCanMove);
 
 		// System.out.println("Destination Array Size: " +
 		// destinationArray.size());
 		Point randomDestination = destinationArray.get(r
 				.nextInt(destinationArray.size()));
 
 		// Determines where to move
 		boolean moveDetermined = false;
 		while (moveDetermined == false) {
 
 			// tests to see if its destination is an advantageous trade for us
 			if (makeTrade(
 					randomPiece,
 					currentBoard.getBoardArray()[(int) randomDestination.getX()][(int) randomDestination
 							.getY()])) {
 				movePiece(randomPiece, randomDestination);
 				randomPiece.changeLocation(randomDestination);
 				moveDetermined = true;
 			}
 
 			// checks to see if the destination is blank
 			else if (currentBoard.getBoardArray()[(int) randomDestination
 					.getX()][(int) randomDestination.getY()] instanceof DerpyBlank) {
 				movePiece(randomPiece, randomDestination);
 				randomPiece.changeLocation(randomDestination);
 				moveDetermined = true;
 			}
 
 			else { // picks a new destination because the others aren't feasible
 				destinationArray.remove(randomDestination); // if we get here,
 				// it means the
 				// randomDestination
 				// isn't an option
 				randomDestination = destinationArray.get(r
 						.nextInt(destinationArray.size())); // so
 				// we
 				// need
 				// to
 				// remove it as a
 				// possibility and
 				moveDetermined = false; // create a new random destination
 			}
 		}
 
 		parseCurrentBoard();
 		boardStore.add(currentBoard);
 		System.out.println("DO SOMETHING!!!");
 		System.out.println("Turn " + numTurns + ": Random Move Made by "
 				+ randomPiece.toString() + " from (" + (int) oldDest.getX()
 				+ "," + (int) oldDest.getY() + ") " + "to ("
 				+ (int) randomDestination.getX() + ","
 				+ (int) randomDestination.getY() + ")");
 		// currentBoard.printBoard();
 		return currentBoard;
 		// To clarify, this method isn't perfect. It tries to make moves in the
 		// following order:
 		// 1.Take a piece to our advantage
 		// 2.Move to a blank spot
 		// 3.Otherwise, find a different destination that meets the above
 		// conditions.
 	}
 
 	public DerpyBoard samAI(String opening) {
 			covered = false; 
 			if (myColor == true && opening == "Normal" && numTurns < 5) {
 				if (numTurns == 1) {
 					//currentBoard.getBoardArray()[4][6].changeLocation(new Point
 					//(4,4));
 					this.movePiece(currentBoard.getBoardArray()[4][6], new Point(4,
 							4));
 					numTurns++;
 					parseCurrentBoard();
 					//currentBoard.printBoard();
 					System.out.println();
 					return currentBoard;
 				}
 
 				else if (numTurns == 2) {
 					//currentBoard.getBoardArray()[5][7].changeLocation(new Point
 					//(2,4));
 					this.movePiece(currentBoard.getBoardArray()[5][7], new Point(2,
 							4));
 					numTurns++;
 					parseCurrentBoard();
 					//currentBoard.printBoard();
 					System.out.println();
 					return currentBoard;
 				}
 
 				else if (numTurns == 3) {
 					//currentBoard.getBoardArray()[6][7].changeLocation(new Point
 					//(5,5));
 					this.movePiece(currentBoard.getBoardArray()[6][7], new Point(5,
 							5));
 					numTurns++;
 					parseCurrentBoard();
 					//currentBoard.printBoard();
 					System.out.println();
 					return currentBoard;
 				}
 
 				else {
 					//currentBoard.getBoardArray()[4][7].changeLocation(new Point
 					//(6,7));
 					this.movePiece(currentBoard.getBoardArray()[4][7], new Point(6,
 							7));
 					//currentBoard.getBoardArray()[7][7].changeLocation(new Point
 					//(5,7));
 					movePiece(currentBoard.getBoardArray()[7][7], new Point(5, 7));
 					numTurns++;
 					parseCurrentBoard();
 					//currentBoard.printBoard();
 					System.out.println();
 					return currentBoard;
 				}
 			}
 
 			else if (myColor == true && opening == "Blitz" && numTurns < 5) {
 				if (numTurns == 1) {
 					this.movePiece(currentBoard.getBoardArray()[4][6], new Point(4,
 							4));
 					numTurns++;
 					parseCurrentBoard();
 					//currentBoard.printBoard();
 					System.out.println();
 					return currentBoard;
 				}
 
 				else if (numTurns == 2) {
 					this.movePiece(currentBoard.getBoardArray()[5][7], new Point(2,
 							4));
 					numTurns++;
 					parseCurrentBoard();
 					//currentBoard.printBoard();
 					System.out.println();
 					return currentBoard;
 				}
 
 				else if (numTurns == 3) {
 					this.movePiece(currentBoard.getBoardArray()[3][7], new Point(7,
 							3));
 					numTurns++;
 					parseCurrentBoard();
 					//currentBoard.printBoard();
 					System.out.println();
 					return currentBoard;
 				}
 
 				else {
 					this.movePiece(currentBoard.getBoardArray()[7][3], new Point(5,
 							1));
 					numTurns++;
 					parseCurrentBoard();
 					//currentBoard.printBoard();
 					System.out.println();
 					return currentBoard;
 				}
 			}
 
 			else {
 
 				parseCurrentBoard();
 				numTurns++;
 
 				//If we're in check, get out of it
 				System.out.println("In check? " + this.inCheck());
 				if (this.inCheck()) {
 					System.out.println("FLY, YOU FOOLS!!!");
 					return this.getOutOfCheck(currentBoard);
 				}
 
 				//Otherwise, find the initial piece to move and the initial
 				//destination
 				else {
 					DerpyPiece bestPiece = null; //Our piece to move
 					DerpyPiece bestTarget = null; //Enemy piece to take
 
 					//If we aren't in check, we shouldn't move our king
 					for (int i = 0; i < ourPieces.size(); i++) {
 						if (ourPieces.get(i) instanceof DerpyKing)
 							ourPieces.remove(i);
 					}
 					
 					//Goes through each of our pieces
 					for (int f = 0; f < ourPieces.size(); f++) {
 						//Finds the possible destinations of that respective piece
 						ArrayList<Point> destinationArray = this.movablePoints(ourPieces.get(f));
 						//Create an array of pieces we should take
 						ArrayList<DerpyPiece> piecesToTake = new ArrayList<DerpyPiece>();
 						//Goes through each destination of the piece
 						for (int i = 0; i < destinationArray.size(); i++) {
 							//Checks to see that the destination isn't a blank
 							if (!(currentBoard.getBoardArray()[(int) destinationArray.get(i).getX()][(int) destinationArray.get(i).getY()] instanceof DerpyBlank)) {
							System.out.println("Our Piece: " + ourPieces.get(f));
 							System.out.println("Not a blank");
							DerpyBoard tempBoard = currentBoard; 
 							//Checks to see if the enemy piece is worth less than our taking piece
 								if((currentBoard.getBoardArray()[(int) destinationArray.get(i).getX()][(int) destinationArray.get(i).getY()].toValue())<ourPieces.get(f).toValue()){
 								//If the enemy piece is worth less, check to see if any other enemy pieces can take our piece once we take the first piece	
 									System.out.println("Target is worth less");
									this.movePiece(ourPieces.get(f), new Point((int) destinationArray.get(i).getX(),(int) destinationArray.get(i).getY()));
 									for (int z = 0; z < theirPieces.size(); z++) {
										System.out.println("Their Piece: " + theirPieces.get(z));
										System.out.println("Can it Cover? " + pieceCanMoveToPosition(theirPieces.get(z),new Point((int) destinationArray.get(i).getX(),(int) destinationArray.get(i).getY())));
 										if (pieceCanMoveToPosition(theirPieces.get(z),new Point((int) destinationArray.get(i).getX(),(int) destinationArray.get(i).getY()))) covered = true; 
 									}
									currentBoard = tempBoard; 
 										//If none can, still take the piece
 									if (covered==false){
 										System.out.println("Target is not covered");
 										piecesToTake.add(currentBoard.getBoardArray()[(int) destinationArray.get(i).getX()][(int) destinationArray.get(i).getY()]);
 									}
 								}
 								//If the enemy piece is worth more, add it to the array to be taken
 								else {
 								piecesToTake.add(currentBoard.getBoardArray()[(int) destinationArray.get(i).getX()][(int) destinationArray.get(i).getY()]);
 								System.out.println("Target is worth more");
 								}
 							}
 						}
 
 						// Finds the most valuable piece in that array if that array
 						// is
 						// not empty, and compares it to
 						// The most valuable piece any of our other pieces so far
 						// can
 						// take - if it's worth more, this becomes
 						// The new best piece to take
 						if (piecesToTake.size() != 0) {
 							DerpyPiece targetPiece = this
 									.findValuablePiece(piecesToTake);
 							// Checks to see if our best target is less valuable
 							// than
 							// the new target, if it is, replaces the best target
 							// with
 							// the new one
 							if (this.makeTrade(bestTarget, targetPiece)
 									|| bestTarget == null) {
 								bestTarget = targetPiece;
 								bestPiece = ourPieces.get(f);
 							}
 						}
 					}
 
 					// If we have any pieces to take, takes the best one of them
 					if (bestPiece != null && bestTarget != null) {
 						System.out.println("TERMINATE");
 						System.out.println("Turn " + numTurns
 										+ ": Autonomous Move Made by "
 										+ bestPiece.toString() + " from ("
 										+ (int) bestPiece.getLocation().getX()
 										+ ","
 										+ (int) bestPiece.getLocation().getY()
 										+ ")" + " to ("
 										+ (int) bestTarget.getLocation().getX()
 										+ ","
 										+ (int) bestTarget.getLocation().getY()
 										+ ")");
 						this.movePiece(bestPiece, bestTarget.getLocation());
 						bestPiece.changeLocation(bestTarget.getLocation());
 						System.out.println();
 						parseCurrentBoard();
 						boardStore.add(currentBoard);
 						// currentBoard.printBoard();
 						System.out.println();
 					}
 
 					// Otherwise, makes a random move
 					else
 						this.randomMove();
 
 					// Sets up the new board
 					parseCurrentBoard();
 					boardStore.add(currentBoard);
 					return currentBoard;
 				}
 			}
 		}
 
 
 
 	// returns a board that moves a piece out of being threatened
 	public DerpyBoard savePiece(DerpyPiece p) {
 		ArrayList<Point> placesToMove = movablePoints(p);
 		for (Point d : placesToMove) {
 			DerpyBoard testBoard = movePiece(p, d);
 			DerpyBoard originalBoard = currentBoard;
 			currentBoard = testBoard;
 			if (!pieceIsThreatened(p)) {
 				currentBoard = originalBoard;
 				return testBoard;
 			}
 		}
 		// if a piece cannot be saved
 		return currentBoard;
 	}
 
 	public String trashTalk(){
 		String trash; 
 		Random r = new Random();
 		int rvalue = r.nextInt(10);
 		
 		if (rvalue == 0){
 			trash = "WE RIDE!!!";	
 		}
 		
 		else if (rvalue == 1){
 			trash = "FEEL MY WRATH!!!";
 		}
 		
 		else if (rvalue == 2){
 			trash = "DEATH TO THE HEATHEN!!!";
 		}
 		
 		else if (rvalue == 3){
 			trash = "IMA FIRIN' MAH LAZER!!!";
 		}
 		
 		else if (rvalue == 4){
 			trash = "VAE VICTUS, B*TCH!!!";
 		}
 		
 		else if (rvalue == 5){
 			trash = "INFIDEL!!!";
 		}
 		
 		else if (rvalue == 6){
 			trash = "YOUR HERALDRY WILL ADORN MY GALLOWS!!!";
 		}
 		
 		else if (rvalue == 7){
 			trash = "SEND THEM BACK TO THE DIGITAL SEA!!!";
 		}
 		
 		else if (rvalue == 8){
 			trash = "TIME 2 DIE, PUNY HUMAN!!!";
 		}
 		
 		else trash = "FOR BUDDHA!!!";
 		
 		return trash; 
 	}
 	
 	public boolean theyreinCheck() {
 		for (DerpyPiece x : theirPieces) {
 			if (x instanceof DerpyKing) {
 				if (pieceIsThreatened(x)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public boolean theyreInCheckmate() {
 
 		DerpyKing targetKing = null;
 
 		for (int i = 0; i < theirPieces.size(); i++) {
 			DerpyPiece p = theirPieces.get(i);
 			if (p instanceof DerpyKing) {
 				targetKing = (DerpyKing) p;
 				break;
 			}
 		}
 
 		if (!theyreinCheck()) {
 			return false;
 		}
 
 		// targetKing is now their king
 		boolean foundAPlaceToMove = false;
 
 		for (int i = 0; i < 8; i++) {
 			for (int j = 0; j < 8; j++) {
 				Point pos = new Point(i, j);
 				if (pieceCanMoveToPosition(targetKing, pos)) {
 					foundAPlaceToMove = true;
 					break;
 				}
 			}
 		}
 
 		return !foundAPlaceToMove;
 	}
 
 	// returns an arraylist of pieces threatening this piece p if it is theirs
 	public ArrayList<DerpyPiece> threateningPiecesToThem(DerpyPiece p) {
 		ArrayList<DerpyPiece> threats = new ArrayList<DerpyPiece>();
 		for (DerpyPiece a : ourPieces) {
 			if (pieceCanMoveToPosition(a, p.getLocation())) {
 				threats.add(a);
 			}
 		}
 		return threats;
 	}
 
 	// returns an arraylist of pieces threatening this piece p if it is ours
 	public ArrayList<DerpyPiece> threateningPiecesToUs(DerpyPiece p) {
 		ArrayList<DerpyPiece> threats = new ArrayList<DerpyPiece>();
 		for (DerpyPiece a : theirPieces) {
 			if (pieceCanMoveToPosition(a, p.getLocation())) {
 				threats.add(a);
 			}
 		}
 		return threats;
 	}
 
 	public boolean weHaveOurKingStill() {
 		for (DerpyPiece x : ourPieces) {
 			if (x instanceof DerpyKing) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean wereInCheckmate() {
 
 		DerpyKing targetKing = null;
 
 		for (DerpyPiece p : ourPieces) {
 			if (p instanceof DerpyKing) {
 				targetKing = (DerpyKing) p;
 				break;
 			}
 		}
 
 		if (!inCheck()) {
 			return false;
 		}
 
 		// targetKing is now our king
 		boolean foundAPlaceToMove = false;
 
 		for (int i = 0; i < 8; i++) {
 			for (int j = 0; j < 8; j++) {
 				Point pos = new Point(i, j);
 				if (pieceCanMoveToPosition(targetKing, pos)) {
 					foundAPlaceToMove = true;
 					break;
 				}
 			}
 		}
 
 		return !foundAPlaceToMove;
 
 	}
 
 }
