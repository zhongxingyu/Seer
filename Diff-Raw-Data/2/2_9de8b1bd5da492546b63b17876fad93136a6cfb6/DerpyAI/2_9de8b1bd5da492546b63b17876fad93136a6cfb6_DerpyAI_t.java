 package DerpyAI;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Scanner;
 
 import sharedfiles.Piece;
 
 public class DerpyAI {
 	private Boolean myColor; // black is false, white is true.
 	private ArrayList<DerpyBoard> boardStore; // The current, and all previous
 												// boards
 	private int numTurns;
 	private ArrayList<DerpyPiece> takenPieces; // The pieces we took
 	public ArrayList<DerpyPiece> ourPieces; // Our Array of Pieces
 	public ArrayList<DerpyPiece> theirPieces; // Our Array of their Pieces
 	private DerpyBoard currentBoard; // currentBoard is the current chess board
 	public ArrayList<Point> ourPiecesPoints; // array of the locations of our
 												// pieces
 	public ArrayList<Point> theirPiecesPoints; // array of the locations of
 												// their pieces
 	private ArrayList<Move> allMoves;
 	private Scanner sc;
 
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
 	}
 
 	public void findTheirPieces() { // Creates an array of their pieces
 		DerpyPiece[][] boardState = currentBoard.getBoardArray();
 		for (int i = 0; i < 8; i++) {
 			for (int a = 0; a < 8; a++) {
 				if (!(this.isPieceOurs(boardState[i][a])))
 					theirPieces.add(boardState[i][a]);
 			}
 		}
 	}
 
 	public void findTheirPiecesPoints() { // Creates an array of their pieces'
 		// locations
 		DerpyPiece[][] boardState = currentBoard.getBoardArray();
 		for (int i = 0; i < 8; i++) {
 			for (int a = 0; a < 8; a++) {
 				Point currentPoint = new Point(i, a);
 				if (!(this.isPieceOurs(boardState[i][a])))
 					theirPiecesPoints.add(currentPoint);
 			}
 		}
 	}
 
 	public void findOurPieces() { // Creates an array of our pieces
 		DerpyPiece[][] boardState = currentBoard.getBoardArray();
 		for (int i = 0; i < 8; i++) {
 			for (int a = 0; a < 8; a++) {
 				if (this.isPieceOurs(boardState[i][a]))
 					ourPieces.add(boardState[i][a]);
 			}
 		}
 	}
 
 	public void findOurPiecesPoints() { // Creates an array of our pieces'
 		// locations
 		DerpyPiece[][] boardState = currentBoard.getBoardArray();
 		for (int i = 0; i < 8; i++) {
 			for (int a = 0; a < 8; a++) {
 				Point currentPoint = new Point(i, a);
 				if (this.isPieceOurs(boardState[i][a]))
 					ourPiecesPoints.add(currentPoint);
 			}
 		}
 	}
 
 	// checks if a piece is ours
 	public boolean isPieceOurs(DerpyPiece p) {
 		if (this.myColor == p.getColor() && !(p instanceof DerpyBlank)) {
 			return true;
 		} else
 			return false;
 	}
 
 	// returns an arraylist of our pieces that are threatened by an enemy piece
 	public ArrayList<DerpyPiece> enemyThreats(DerpyBoard b) {
 		ArrayList<DerpyPiece> ourThreatenedPieces = new ArrayList<DerpyPiece>();
 		for (int i = 0; i < 8; i++) {
 			for (int j = 0; j < 8; j++) {
 				if (this.isPieceOurs(b.getBoardArray()[i][j])) {
 					if (this.pieceIsThreatened(b.getBoardArray()[i][j])) {
 						ourThreatenedPieces.add(b.getBoardArray()[i][j]);
 					}
 				}
 			}
 		}
 
 		return ourThreatenedPieces;
 
 	}
 
 	// returns an arraylist of enemy pieces that we threaten
 	public ArrayList<DerpyPiece> ourThreats(DerpyBoard b) {
 		ArrayList<DerpyPiece> theirThreatenedPieces = new ArrayList<DerpyPiece>();
 		for (int i = 0; i < 8; i++) {
 			for (int j = 0; j < 8; j++) {
 				if (!(this.isPieceOurs(b.getBoardArray()[i][j]))) {
 					if (this.pieceIsThreatened(b.getBoardArray()[i][j])) {
 						theirThreatenedPieces.add(b.getBoardArray()[i][j]);
 					}
 				}
 			}
 		}
 
 		return theirThreatenedPieces;
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
 
 		if (defender instanceof DerpyPawn)
 			if (attacker instanceof DerpyRook || attacker instanceof DerpyQueen
 					|| attacker instanceof DerpyKing
 					|| attacker instanceof DerpyBishop
 					|| attacker instanceof DerpyKnight) {
 				return false;
 			} else {
 				return true;
 			}
 		return false;
 	}
 
 	// tells if a piece is protected
 	public boolean pieceIsProtected(DerpyPiece p) {
 		DerpyPiece d = p;
 		for (DerpyPiece a : ourPieces) {
 			if (this.pieceCanMoveToPosition(a, d.getLocation())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// asks if a piece is threatened
 	public boolean pieceIsThreatened(DerpyPiece p) {
 		for (DerpyPiece a : (p.getColor() ? theirPieces : ourPieces) ) {
 			if (this.pieceCanMoveToPosition(a, p.getLocation())) {
 				if (this.makeTrade(a, p)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	// Returns if the king is in check
 	public boolean inCheck() {
 		for (DerpyPiece x : ourPieces) {
 			if (x instanceof DerpyKing) {
 				if (this.pieceIsThreatened(x)) {
 					return true;
 				}
 				else return false;
 			}
 		}
 		System.out.println("THIS SHOULD NEVER RUN");
 		return false;
 	}
 	
 	public boolean weHaveOurKingStill() {
 		for (DerpyPiece x : ourPieces) {
 			if (x instanceof DerpyKing)return true;
 		}
 		return false;
 	}
 	
 	public boolean theyreinCheck() {
 		for (DerpyPiece x : theirPieces) {
 			if (x instanceof DerpyKing) {
 				if (this.pieceIsThreatened(x)) {
 					return true;
 				}
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
 				if (this.pieceCanMoveToPosition(p, moveTo)) {
 					listOfPoints.add(moveTo);
 				}
 			}
 		}
 		return listOfPoints;
 	}
 
 	// returns an arraylist of pieces threatening this piece p if it is theirs
 	public ArrayList<DerpyPiece> threateningPiecesToThem(DerpyPiece p) {
 		ArrayList<DerpyPiece> threats = new ArrayList<DerpyPiece>();
 		for (DerpyPiece a : ourPieces) {
 			if (this.pieceCanMoveToPosition(a, p.getLocation())) {
 				threats.add(a);
 			}
 		}
 		return threats;
 	}
 
 	// returns an arraylist of pieces threatening this piece p if it is ours
 	public ArrayList<DerpyPiece> threateningPiecesToUs(DerpyPiece p) {
 		ArrayList<DerpyPiece> threats = new ArrayList<DerpyPiece>();
 		for (DerpyPiece a : theirPieces) {
 			if (this.pieceCanMoveToPosition(a, p.getLocation())) {
 				threats.add(a);
 			}
 		}
 		return threats;
 	}
 
 	// returns an arraylist of points that can be occupied to block theirs from
 	// capturing ours
 	public ArrayList<Point> findBlockablePoints(DerpyPiece ours,
 			DerpyPiece theirs) {
 		ArrayList<Point> points = new ArrayList<Point>();
 		if ((theirs instanceof DerpyKnight || theirs instanceof DerpyPawn || theirs instanceof DerpyKing)) {
 			return points;
 		}
 		if (theirs.getLocation().distance(ours.getLocation()) < 1.5) {
 			return points;
 		}
 		if (theirs instanceof DerpyRook || theirs instanceof DerpyQueen) {
 			if (theirs.getLocation().getX() == ours.getLocation().getX()) {
 				if (theirs.getLocation().getY() > ours.getLocation().getY()) {
 					for (double i = theirs.getLocation().getY(); i >= ours
 							.getLocation().getY(); i--) {
 						Point ourPoint = new Point((int) i, ((int) theirs
 								.getLocation().getY()));
 						points.add(ourPoint);
 					}
 				}
 				if (theirs.getLocation().getY() < ours.getLocation().getY()) {
 					for (double i = theirs.getLocation().getY(); i <= ours
 							.getLocation().getY(); i++) {
 						Point ourPoint = new Point((int) i, ((int) theirs
 								.getLocation().getY()));
 						points.add(ourPoint);
 					}
 				}
 			}
 			if (theirs.getLocation().getY() == ours.getLocation().getY()) {
 				if (theirs.getLocation().getX() > ours.getLocation().getX()) {
 					for (double i = theirs.getLocation().getX(); i >= ours
 							.getLocation().getX(); i--) {
 						Point ourPoint = new Point((int) i, ((int) theirs
 								.getLocation().getY()));
 						points.add(ourPoint);
 					}
 				}
 				if (theirs.getLocation().getX() < ours.getLocation().getX()) {
 					for (double i = theirs.getLocation().getX(); i <= ours
 							.getLocation().getX(); i++) {
 						Point ourPoint = new Point((int) i, ((int) theirs
 								.getLocation().getY()));
 						points.add(ourPoint);
 					}
 				}
 			}
 
 		}
 
 		if (theirs instanceof DerpyBishop || theirs instanceof DerpyQueen) {
 			if (theirs.getLocation().getX() > ours.getLocation().getX()) {
 				if (theirs.getLocation().getY() < ours.getLocation().getY()) {
 					for (double i = theirs.getLocation().getX(); i >= ours
 							.getLocation().getX(); i--) {
 						for (double j = theirs.getLocation().getY(); j <= ours
 								.getLocation().getY(); j++) {
 							Point ourPoint = new Point((int) i, (int) j);
 							points.add(ourPoint);
 						}
 					}
 				}
 				if (theirs.getLocation().getY() > ours.getLocation().getY()) {
 					for (double i = theirs.getLocation().getX(); i >= ours
 							.getLocation().getX(); i--) {
 						for (double j = theirs.getLocation().getY(); j >= ours
 								.getLocation().getY(); j--) {
 							Point ourPoint = new Point((int) i, (int) j);
 							points.add(ourPoint);
 
 						}
 					}
 				}
 			}
 			if (theirs.getLocation().getX() < ours.getLocation().getX()) {
 				if (theirs.getLocation().getY() < ours.getLocation().getY()) {
 					for (double i = theirs.getLocation().getX(); i <= ours
 							.getLocation().getX(); i++) {
 						for (double j = theirs.getLocation().getY(); j <= ours
 								.getLocation().getY(); j++) {
 							Point ourPoint = new Point((int) i, (int) j);
 							points.add(ourPoint);
 						}
 					}
 				}
 				if (theirs.getLocation().getY() > ours.getLocation().getY()) {
 					for (double i = theirs.getLocation().getX(); i <= ours
 							.getLocation().getX(); i++) {
 						for (double j = theirs.getLocation().getY(); j >= ours
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
 
 	// makes a move to get out of check
 	public DerpyBoard getOutOfCheck(DerpyBoard b) {
 		// tries to move the king out of check
 		for (int i = 0; i < ourPieces.size(); i++) {
 			if (ourPieces.get(i) instanceof DerpyKing) {
 				ArrayList<Point> listOfPoints = this.movablePoints(ourPieces
 						.get(i));
 				for (int j = 0; j < listOfPoints.size(); j++) {
 					if (this.pieceCanMoveToPosition(ourPieces.get(i),
 							listOfPoints.get(j))) {
 						return this.movePiece(ourPieces.get(i),
 								listOfPoints.get(j));
 					}
 				}
 			}
 		}
 		// tries to take the threatening piece
 		for (int i = 0; i < ourPieces.size(); i++) {
 			if (ourPieces.get(i) instanceof DerpyKing) {
 				DerpyPiece ourKing = ourPieces.get(i);
 				if (this.threateningPiecesToUs(ourKing).size() == 1) {
 					DerpyPiece threat = this.threateningPiecesToUs(ourKing)
 							.get(0);
 					if (this.threateningPiecesToThem(threat).size() >= 1) {
 						DerpyPiece taker = this.threateningPiecesToThem(threat)
 								.get(0);
 						return this.movePiece(taker, threat.getLocation());
 					}
 				}
 
 			}
 		}
 		for (int i = 0; i < ourPieces.size(); i++) {
 			if (ourPieces.get(i) instanceof DerpyKing) {
 				DerpyPiece ourKing = ourPieces.get(i);
 				ArrayList<DerpyPiece> threats = threateningPiecesToUs(ourKing);
 				if (threats.size() == 0) {
 					ArrayList<Point> betweenSpaces = this.findBlockablePoints(
 							ourKing, threats.get(0));
 					for (Point p : betweenSpaces) {
 						for (DerpyPiece c : ourPieces) {
 							if (this.pieceCanMoveToPosition(c, p)) {
 								return this.movePiece(c, p);
 							}
 						}
 					}
 				}
 			}
 		}
 
		//this.concedeGame();
 		return currentBoard;
 	}
 
 	// asks if a piece can legally move to a position
 	public boolean pieceCanMoveToPosition(DerpyPiece piece, Point position) {
 
 		int xPos = (int) position.getX();
 		int yPos = (int) position.getY();
 		boolean indicator = false;
 		if (currentBoard.getBoardArray()[xPos][yPos] instanceof DerpyBlank) {
 			indicator = true;
 		}
 		if (!(currentBoard.getBoardArray()[xPos][yPos]
 				.getColor() == myColor) || indicator) {
 
 			if (piece instanceof DerpyKing) {
 				// can only move 1 space
 				if (piece.getLocation().distanceSq(position) == 1
 						|| piece.getLocation().distanceSq(position) == 2) {
 					// makes sure the destination is not occupied by a friendly
 					// piece
 					if (currentBoard.getBoardArray()[xPos][yPos] instanceof DerpyBlank
 							&& (currentBoard.getBoardArray()[xPos][yPos]
 									.getColor()) == myColor) {
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
 				if (!(piece.getColor())) {
 					// if the pawn wants to move up two spaces and is on its
 					// starting area
 					if (piece.getLocation().getY() == 1 && position.getY() == 3) {
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
 					if ((piece.getLocation().getY() == yPos - 1 && piece
 							.getLocation().getX() == xPos - 1)
 							|| (piece.getLocation().getY() == yPos - 1 && piece
 									.getLocation().getX() == xPos + 1)) {
 						// makes sure the space has a takeable piece
 						if (!((currentBoard.getBoardArray()[xPos][yPos] instanceof DerpyBlank))) {
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
 					if ((piece.getLocation().getY() == 6 && position.getY() == 4)
 							&& (piece.getLocation().getX() == xPos)) {
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
 					if ((piece.getLocation().getY() == yPos + 1 && piece
 							.getLocation().getX() == xPos - 1)
 							|| (piece.getLocation().getY() == yPos + 1 && piece
 									.getLocation().getX() == xPos + 1)) {
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
 				DerpyPiece pieceAtDestination = currentBoard
 						.getBoardArray()[xPos][yPos];
 				// destination has to be on the same rank or file
 				if (piece.getLocation().getY() == yPos
 						|| piece.getLocation().getX() == xPos) {
 					// no pieces blocking
 					ArrayList<Point> betweenSpace = this.findBlockablePoints(
 							pieceAtDestination, piece);
 					for (Point d : betweenSpace) {
 						if (!(currentBoard.getBoardArray()[(int) d
 								.getX()][(int) d.getY()] instanceof DerpyBlank)) {
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
 				DerpyPiece pieceAtDestination = currentBoard
 						.getBoardArray()[xPos][yPos];
 				// destination has to be on the same diagonal
 				if (piece.getLocation().getY() - yPos == piece.getLocation()
 						.getX() - xPos
 						|| piece.getLocation().getY() - yPos == -1
 								* (piece.getLocation().getX() - xPos)) {
 					// no pieces blocking
 					ArrayList<Point> betweenSpace = this.findBlockablePoints(
 							pieceAtDestination, piece);
 					for (Point d : betweenSpace) {
 						if (!(currentBoard.getBoardArray()[(int) d
 								.getX()][(int) d.getY()] instanceof DerpyBlank)) {
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
 
 			DerpyPiece targetPiece = null;
 			for (Piece p : ourPieces) {
 				DerpyPiece d = (DerpyPiece) p;
 				Point piecePosition = d.getLocation();
 			}
 		}
 
 		return false;
 	}
 
 	// uses provided board to make a move, returns a board with the move made
 
 	public DerpyBoard movePiece(DerpyPiece p, Point mL) {
 
 		DerpyBoard newBoard = new DerpyBoard(currentBoard);
 
 		Point oL = p.getLocation(); // This will access the instance data in the
 									// piece class that contain its location.
 
 		// Edit the _*PIECE*_ so it knows where it, itself it now
 		p.changeLocation(mL);
 
 		// Edit the _*BOARD*_ so it knows where the pieces are now
 		newBoard.getBoardArray()[(int) oL.getX()][(int) oL.getY()] = new DerpyBlank(
 				oL); // Put
 						// a
 						// blank
 						// piece
 						// in
 						// the
 						// old
 						// location
 		newBoard.getBoardArray()[(int) mL.getX()][(int) mL.getY()] = p;
 
 		Move m = new Move(myColor, p, oL, mL);
 		allMoves.add(m);
 
 		parseCurrentBoard();
 		return newBoard;
 	}
 
 	public void parseCurrentBoard() {
 
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
 
 	// returns a board that moves a piece out of being threatened
 	public DerpyBoard savePiece(DerpyPiece p) {
 		ArrayList<Point> placesToMove = this.movablePoints(p);
 		for (Point d : placesToMove) {
 			DerpyBoard testBoard = this.movePiece(p, d);
 			DerpyBoard originalBoard = this.currentBoard;
 			currentBoard = testBoard;
 			if (!(this.pieceIsThreatened(p))) {
 				currentBoard = originalBoard;
 				return testBoard;
 			}
 		}
 		// if a piece cannot be saved
 		return currentBoard;
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
 			this.concedeGame();
 
 		}
 
 		// If we aren't in check, we shouldn't move our king
 		for (int i = 0; i < ourPieces.size(); i++) {
 			if (ourPieces.get(i) instanceof DerpyKing)
 				ourPieces.remove(i);
 		}
 
 		do {
 			// System.out.println("Piece Array Size: " + ourPieces.size());
 			randomPiece = ourPieces.get(r.nextInt(ourPieces.size()));
 			oldDest = randomPiece.getLocation();
 			// System.out.println("Piece Type: " + randomPiece.toString());
 			destinationArray = this.movablePoints(randomPiece);
 			if (destinationArray.size() > 0)
 				pieceCanMove = true;
 			else
 				pieceCanMove = false;
 		} while (!pieceCanMove);
 
 		// System.out.println("Destination Array Size: " +
 		// destinationArray.size());
 		Point randomDestination = destinationArray.get(r
 				.nextInt(destinationArray.size()));
 
 		// Determines where to move
 		boolean moveDetermined = false;
 		while (moveDetermined == false) {
 
 			// tests to see if its destination is an advantageous trade for us
 			if (this.makeTrade(
 					randomPiece,
 					currentBoard.getBoardArray()[(int) randomDestination.getX()][(int) randomDestination
 							.getY()])) {
 				this.movePiece(randomPiece, randomDestination);
 				randomPiece.changeLocation(randomDestination);
 				moveDetermined = true;
 			}
 
 			// checks to see if the destination is blank
 			else if (currentBoard.getBoardArray()[(int) randomDestination
 					.getX()][(int) randomDestination.getY()] instanceof DerpyBlank) {
 				this.movePiece(randomPiece, randomDestination);
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
 		System.out.println("Random Move Made by " + randomPiece.toString()
 				+ " from (" + (int) oldDest.getX() + ","
 				+ (int)oldDest.getY() + ") " + "to (" + (int) randomDestination.getX() + ","
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
 
 	public DerpyBoard samAI() {
 		
 		if (myColor == true){
 			
 		}
 			if (numTurns == 1) {
 			//currentBoard.getBoardArray()[4][6].changeLocation(new Point (4,4));
 			this.movePiece(currentBoard.getBoardArray()[4][6], new Point (4,4));
 			numTurns++;
 			parseCurrentBoard(); 
 			currentBoard.printBoard();
 			System.out.println();
 			return currentBoard;
 			}
 			
 			else if (numTurns == 2) {
 			//currentBoard.getBoardArray()[5][7].changeLocation(new Point (2,4));
 			this.movePiece(currentBoard.getBoardArray()[5][7], new Point (2,4));
 			numTurns++;
 			parseCurrentBoard(); 
 			currentBoard.printBoard();
 			System.out.println();
 			return currentBoard;
 			}
 			
 			else if (numTurns == 3) {
 			//currentBoard.getBoardArray()[6][7].changeLocation(new Point (5,5));
 			this.movePiece(currentBoard.getBoardArray()[6][7], new Point (5,5));
 			numTurns++;
 			parseCurrentBoard(); 
 			currentBoard.printBoard();
 			System.out.println();
 			return currentBoard;
 			}
 			
 			else if (numTurns == 4) {
 			//currentBoard.getBoardArray()[4][7].changeLocation(new Point (6,7));
 			this.movePiece(currentBoard.getBoardArray()[4][7], new Point (6,7));
 			//currentBoard.getBoardArray()[7][7].changeLocation(new Point (5,7));
 			movePiece(currentBoard.getBoardArray()[7][7], new Point (5,7));
 			numTurns++;
 			parseCurrentBoard(); 
 			currentBoard.printBoard();
 			System.out.println();
 			return currentBoard;
 			}
 			
 		else{
 		parseCurrentBoard();
 
 		//If we're in check, get out of it
 		System.out.println("In check? " + this.inCheck());
 		if (this.inCheck()) {
 			System.out.println("FLY, YOU FOOLS!");
 			return this.getOutOfCheck(currentBoard);
 
 		}
 
 		//Otherwise, find the initial piece to move and the initial destination
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
 				ArrayList<Point> destinationArray = this
 						.movablePoints(ourPieces.get(f));
 				ArrayList<DerpyPiece> piecesToTake = new ArrayList<DerpyPiece>();
 				//Finds all possible pieces that piece can take
 				for (int i = 0; i < destinationArray.size(); i++) {
 					if (!(currentBoard.getBoardArray()[(int) destinationArray
 							.get(i).getX()][(int) destinationArray.get(i)
 							.getY()] instanceof DerpyBlank)) {
 						piecesToTake
 								.add(currentBoard.getBoardArray()[(int) destinationArray
 										.get(i).getX()][(int) destinationArray
 										.get(i).getY()]);
 						System.out
 								.println(piecesToTake.get(piecesToTake.size() - 1));
 					}
 				}
 
 				//Finds the most valuable piece in that array if that array is
 				//not empty, and compares it to
 				//The most valuable piece any of our other pieces so far can
 				//take - if it's worth more, this becomes
 				//The new best piece to take
 				if (piecesToTake.size() != 0) {
 					DerpyPiece targetPiece = this.findValuablePiece(piecesToTake);
 					//Checks to see if our best target is less valuable than
 					//the new target, if it is, replaces the best target with
 					//the new one
 					if (this.makeTrade(bestTarget, targetPiece)
 							|| bestTarget == null) {
 						bestTarget = targetPiece;
 						bestPiece = ourPieces.get(f);
 					}
 				}
 			}
 
 			//If we have any pieces to take, takes the best one of them
 			if (bestPiece != null && bestTarget != null) {
 				System.out.println("Autonomous Move Made by "+ bestPiece.toString() + 
 						" from (" + (int) bestPiece.getLocation().getX() + ","
 						+ (int) bestPiece.getLocation().getY() + ")" +" to ("
 						+ (int) bestTarget.getLocation().getX() + ","
 						+ (int) bestTarget.getLocation().getY() + ")");
 				this.movePiece(bestPiece, bestTarget.getLocation());
 				bestPiece.changeLocation(bestTarget.getLocation());
 				System.out.println();
 				parseCurrentBoard();
 				boardStore.add(currentBoard);
 				// currentBoard.printBoard();
 				System.out.println();
 			}
 
 			//Otherwise, makes a random move
 			else
 				this.randomMove();
 
 			//Sets up the new board
 			parseCurrentBoard();
 			boardStore.add(currentBoard);
 			return currentBoard;
 		}
 		}
 	}
 
 	public DerpyBoard makeMove(DerpyBoard b) {
 
 		if(wereInCheckmate() || !weHaveOurKingStill()) {
 			System.out.println("DerpyAI has lost....press enter to continue.");
 			sc = new Scanner(System.in);
 		       while(!sc.nextLine().equals(""));
 		}
 		
 		if(theyreInCheckmate()) {
 			System.out.println("DerpyAI has won....press enter to continue.");
 			sc = new Scanner(System.in);
 		       while(!sc.nextLine().equals(""));
 		}
 
 		boardStore.add(b);
 		currentBoard = b;
 		parseCurrentBoard();
 
 		DerpyBoard boardWithPieceMoved = new DerpyBoard(b); // Copy the b board
 
 		if (this.inCheck()) {
 			// We're in check, call getOutOfCheck to get us a board where we're
 			// not in check
 			// System.out.println("makeMove: inCheck was true");
 			boardStore.add(boardWithPieceMoved);
 			boardWithPieceMoved = this.getOutOfCheck(b);
 			// System.out.println("makeMove: Now out of check, in theory");
 
 		} else {
 
 			boardStore.add(boardWithPieceMoved);
 			currentBoard = boardWithPieceMoved;
 		}
 
 		// DerpyBoard ba = this.prestonAI();
 		DerpyBoard ba = this.samAI();
 		// DerpyBoard ba = this.curtisAI();
 
 		boardStore.add(ba);
 		// ba.printBoard();
 
 		// If we're still in check even after all that,
 		// there's no way out of check. Concede to the other player.
 		if (this.inCheck())
 			concedeGame();
 
 		currentBoard = ba;
 		parseCurrentBoard();
 		return ba;
 	}
 	
 	public boolean wereInCheckmate() {
 
 		DerpyKing targetKing = null;
 		
 		for(DerpyPiece p : ourPieces) {
 			if(p instanceof DerpyKing) {
 				targetKing = (DerpyKing)p;
 				break;
 			}
 		} 
 		
 		if(!inCheck())return false;
 		
 		//targetKing is now our king
 		boolean foundAPlaceToMove = false;
 		
 		for (int i = 0; i < 8; i++) {
 			for (int j = 0; j < 8; j++) {
 				Point pos = new Point(i,j);
 				if(this.pieceCanMoveToPosition(targetKing, pos))
 				{
 					foundAPlaceToMove = true;
 					break;
 				}
 			}
 		}
 		
 		return !foundAPlaceToMove;
 
 	}
 	
 	public boolean theyreInCheckmate() {
 		
 		DerpyKing targetKing = null;
 		
 		for(int i = 0; i < theirPieces.size(); i++) {
 			DerpyPiece p = theirPieces.get(i);
 			if(p instanceof DerpyKing) {
 				targetKing = (DerpyKing)p;
 				break;
 			}
 		} 
 		
 		if(!theyreinCheck())return false;
 		
 		//targetKing is now their king
 		boolean foundAPlaceToMove = false;
 		
 		for (int i = 0; i < 8; i++) {
 			for (int j = 0; j < 8; j++) {
 				Point pos = new Point(i,j);
 				if(this.pieceCanMoveToPosition(targetKing, pos))
 				{
 					foundAPlaceToMove = true;
 					break;
 				}
 			}
 		}
 		
 		return !foundAPlaceToMove;
 	}
 
 	public void concedeGame() {
 		System.out.println("DerpyAI has lost the game.");
 		System.exit(0); // Exit with terminated status 0
 	}
 
 
 //Extra, Currently Unused Code////
 //public DerpyBoard prestonAI() {
 //
 //				parseCurrentBoard();
 //
 //	
 //
 //				// randomMove if nothing can be done
 //				this.randomMove();
 //				// End logic
 //
 //				parseCurrentBoard();
 //				boardStore.add(currentBoard);
 //				return currentBoard;
 //			}
 // makes a move that advances our position or takes an enemy piece--for use
 // during autonomous play when none of our pieces are threatened
 //		public DerpyBoard curtisAI() {
 //			// first tries to take an enemy piece, if it can (and its not
 //			// threatened)
 //			if (this.ourThreats(currentBoard).size() > 0) {
 //				ArrayList<DerpyPiece> piecesWeCanTake = this
 //						.ourThreats(currentBoard);
 //				for (DerpyPiece p : piecesWeCanTake) {
 //					ArrayList<DerpyPiece> piecesWeCanTakeWith = this
 //							.threateningPiecesToThem(p);
 //					if (piecesWeCanTakeWith.size() > 0) {
 //						if (p.getLocation().distance(
 //								this.findEnemyKing().getLocation()) <= 4) {
 //							if (Math.random() <= 0.5) {
 //								return this.movePiece(piecesWeCanTakeWith.get(0),
 //										p.getLocation());
 //							}
 //						}
 //					}
 //				}
 //				for (DerpyPiece p : piecesWeCanTake) {
 //					ArrayList<DerpyPiece> piecesWeCanTakeWith = this
 //							.threateningPiecesToThem(p);
 //					if (piecesWeCanTakeWith.size() > 0) {
 //						if (Math.random() <= 0.5) {
 //							return this.movePiece(piecesWeCanTakeWith.get(0),
 //									p.getLocation());
 //						}
 //					}
 //				}
 	//
 //			}
 //			// if that doesn't work, it makes sure its not threatened, if it is, it
 //			// tries to save the piece
 //			if (this.enemyThreats(currentBoard).size() == 1) {
 //				return this.savePiece(this.enemyThreats(currentBoard).get(0));
 //			} else if (this.enemyThreats(currentBoard).size() > 1) {
 //				DerpyPiece pieceToSave = this.findValuablePiece(this
 //						.enemyThreats(currentBoard));
 //				return this.savePiece(pieceToSave);
 //			}
 //			// finally, if it has no pieces to save or to take, it move a piece
 //			// closer to the enemy king.
 //			else {
 //				DerpyPiece enemyKing = this.findEnemyKing();
 //				for (DerpyPiece p : ourPieces) {
 //					for (Point d : this.movablePoints(p)) {
 //						if (d.distance(enemyKing.getLocation()) < p.getLocation()
 //								.distance(enemyKing.getLocation())) {
 //							if (Math.random() <= 0.2) {
 //								DerpyBoard testBoard = this.movePiece(p, d);
 //								DerpyBoard oldBoard = currentBoard;
 //								currentBoard = testBoard;
 //								if (!(this.pieceIsThreatened(p))) {
 //									currentBoard = oldBoard;
 //									return testBoard;
 //								}
 //							}
 //						}
 //					}
 //				}
 //			}
 //			return currentBoard;
 //		}
 
 		// master move choice method. Decides what move to make, then makes it.
 	
 	/*
 	 * 
 	 * public boolean executeCzechDefense() { // we need code to call this
 	 * method // again after white's moved once // more if (myColor == false) {
 	 * if (allMoves.size() == 0) { Point destination = new Point(5, 2);
 	 * this.movePiece(currentBoard.getBoardArray()[6][0], destination); return
 	 * true; } else if (allMoves.size() == 1) { Point destination = new Point(3,
 	 * 2); this.movePiece(currentBoard.getBoardArray()[3][1], destination);
 	 * return true; } else if (allMoves.size() == 2) { Point destination = new
 	 * Point(2, 2); this.movePiece(currentBoard.getBoardArray()[2][1],
 	 * destination); return true; } else return false; } else return false; }
 	 * 
 	 * public boolean executeSicilianDefense() { if (myColor == false) { if
 	 * (currentBoard.getBoardArray()[4][5] instanceof DerpyPawn) {
 	 * this.movePiece(currentBoard.getBoardArray()[2][1], new Point(2, 3));
 	 * return true; } else return false; } else return false; }
 	 * 
 	 * public boolean executeRuyLopezOpening() { if (myColor == true) { // e4,
 	 * Nf3 if (allMoves.size() == 0) {
 	 * this.movePiece(currentBoard.getBoardArray()[4][6], new Point(4, 4));
 	 * return true; } else if (allMoves.size() == 1) {
 	 * this.movePiece(currentBoard.getBoardArray()[6][7], new Point(6, 5));
 	 * return true; } else return false; } else return false; }
 	 */
 
 }
