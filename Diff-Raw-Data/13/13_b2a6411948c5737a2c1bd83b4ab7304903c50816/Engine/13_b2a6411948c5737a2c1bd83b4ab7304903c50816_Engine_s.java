 import static org.junit.Assert.assertEquals;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class Engine {
 	public static final int NODE_NOT_FOUND = -128;
 	public static final int SAME_NODE_SELECTED = 0;
 	public static final int NO_PIECE_SELECTED = -256;
 	public static final int INVALID_MOVE = -512;
 	private static HashMap<SorryFrame.Coordinate, Integer> coordsMap;
 	private int remainingMoves = 0;
 	protected BoardList board;
 	protected BoardList actualBoard;
 	protected Piece[] pieces;// Indices 0-3 are red, Indices 4-7 are blue,
 								// Indices 8-11
 	// are Yellow, Indices 12-15 are green
 	protected Player activePlayer;
 	protected Deck deck;
 	protected CircularLinkedList<Player> players;
 	protected Card currentCard;
 
 	public Engine(BoardList board, String lang) {
 		this.board = board;
 		this.actualBoard = board.clone();
 		this.players = new CircularLinkedList<Player>();
 		this.deck = new Deck(lang);
 
 	}
 
 	public void insertPlayer(Player bigP) {
 		if (this.players.isEmpty()) {
 			this.players.insertFirst(bigP);
 			this.players.goToNextElement();
 		} else {
 			this.players.insertAfterActual(bigP);
 			this.players.goToNextElement();
 		}
 	}
 
 	/*
 	 * public void testing() { try { move(1, this.pieces[14]); move(3,
 	 * this.pieces[4]); } catch (Unstarted e) { e.printStackTrace(); } }
 	 * 
 	 * public void testing2() { try { move(1, this.pieces[9]); move(1,
 	 * this.pieces[0]); } catch (Unstarted e) { e.printStackTrace(); } }
 	 */
 
 	public void newGame() {
 		this.pieces = this.board.newGame();
 		this.actualBoard = this.board.clone();
 	}
 
 	public boolean isValidMove(Piece pawn, int numberMovesForward, Player player) {
 		// start with a rudimentary, who owns this piece check
 		if (pawn == null || pawn.col != player.getColor())
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * Use the original game rules to decide if a move is valid, then perform
 	 * that move, allowing the nodes to do some validity checks.
 	 * 
 	 * @param pawn
 	 * @param start
 	 * @param end
 	 * @param numberMovesForward
 	 * @param numberMovesBackward
 	 * @param player
 	 * @return
 	 */
 	protected int checkValidityOriginalRules(Piece pawn, Node start, Node end,
 			int numberMovesForward, int numberMovesBackward) {
 		int moves = 0;
 		int error = 1;
 		switch (this.currentCard.cardNum) {
 		// case 1:
 		// if (numberMovesForward == this.currentCard.cardNum)
 		// moves = numberMovesForward;
 		// else
 		// error = INVALID_MOVE;
 		case 2:
 			// 2 forward, get another turn
 			// for now, same rules as the default, but we might need a way to
 			// instruct the GUI to give them another turn
 			if (numberMovesForward != this.currentCard.cardNum)
 				error = INVALID_MOVE;
 			else {
 				moves = 2;
 				for (int j = 0; j < players.getNumberOfElements() - 1; j++) {
 					players.goToNextElement();
 				}
 			}
 			break;
 		// case 3:
 		// break;
 		case 4:
 			// 4 spots backward
 			if (numberMovesBackward != this.currentCard.cardNum)
 				error = INVALID_MOVE;
 			else
 				moves = -4;
 			break;
 		// case 5:
 		// break;
 		case 7:
 			// 7 forward, or a split
 			if (start instanceof MultiNode && start.getPrevious() == null) {
 				error = INVALID_MOVE;
 			}
 			if (this.remainingMoves != 0) {
 				// player is finishing a split
 				if (numberMovesForward == this.remainingMoves) {
 					moves = numberMovesForward;
 					this.remainingMoves = 0;
 				} else
 					error = INVALID_MOVE;
 			} else {
 				// player is starting a split or using the whole card
 				if (numberMovesForward == 7)
 					moves = 7;
 				else if (numberMovesForward < 7) {
 					moves = numberMovesForward;
 					this.remainingMoves = 7 - numberMovesForward;
 				} else
 					error = INVALID_MOVE;
 
 			}
 
 			if (numberMovesForward > this.currentCard.cardNum)
 				error = INVALID_MOVE;
 			else
 				moves = numberMovesForward;
 			break;
 		// case 8:
 		// break;
 		case 10:
 			// 10 forward, or 1 backward
 			if (numberMovesForward == 10)
 				moves = 10;
 			else if (numberMovesBackward == 1)
 				moves = -1;
 			else
 				error = INVALID_MOVE;
 			break;
 		case 11:
 			// 11 spaces forward or a swap
 			if (numberMovesForward == 11)
 				moves = 11;
 			else if (end.hasPiece())
 				moves = numberMovesForward;
 			else
 				error = INVALID_MOVE;
 			break;
 		// case 12:
 		// break;
 		case 13:
 			// Sorry card
 			if (end.hasPiece() && start.getPrevious() == null)
 				moves = numberMovesForward;
 			else
 				error = INVALID_MOVE;
 			break;
 		default:
 			// all remaining cases, simply allow move forward the number or no
 			// dice
 			if (numberMovesForward == this.currentCard.cardNum)
 				moves = numberMovesForward;
 			else
 				error = INVALID_MOVE;
 		}
 
 		try {
 			if (error < 1)
 				return error;
 			if (this.currentCard.cardNum == 13) {
				end.swap(start);
 				return moves;
 			}
 			if (this.currentCard.cardNum == 11 && moves != 11) {
				end.swap(start);
 				return moves;
 			}
 			move(moves, pawn, start);
 		} catch (InvalidMoveException e) {
 			return INVALID_MOVE;
 		} catch (Unstarted e) {
 			e.printStackTrace();
 		}
 
 		return moves;
 	}
 
 	public int pawnMove(SorryFrame.Coordinate start, SorryFrame.Coordinate end) {
 		Node first, second;
 		// Start with error checking - are the coordinates and desired nodes
 		// valid?
 		try {
 			first = this.convertCoordToNode(start);
 			second = this.convertCoordToNode(end);
 		} catch (CoordinateOffOfBoardException e) {
 			return NODE_NOT_FOUND;
 		}
 
 		if (first == second)
 			return SAME_NODE_SELECTED;
 
 		int nodeCountForward = first.countTo(second);
 		int nodeCountBackward = 0;
 		try {
 			nodeCountBackward = first.countBack(second);
 
 		} catch (Exception e) {
 			nodeCountBackward = 0;
 		}
 		if (!isValidMove(first.firstPiece(), nodeCountForward,
 				this.activePlayer)) {
 			return INVALID_MOVE;
 		}
 
 		int ret = checkValidityOriginalRules(first.firstPiece(), first, second,
 				nodeCountForward, nodeCountBackward);
 		// try {
 		//
 		// move(nodeCount, first.firstPiece(), first);
 		//
 		// } catch (Unstarted e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// } catch (InvalidMoveException e) {
 		// return INVALID_MOVE;
 		// }
 		return ret;
 	}
 
 	/**
 	 * Move a given piece the specified number of positions FORWARD on the
 	 * board.
 	 * 
 	 * Don't try using negative numbers.
 	 * 
 	 * @param moves
 	 * @param piece
 	 * @throws Unstarted
 	 *             if the game hasn't been generated
 	 * @throws InvalidMoveException
 	 */
 	public void move(int moves, Piece piece, Node piecenode) throws Unstarted,
 			InvalidMoveException {
 		piecenode.removePieceFromPieces(piece);
 		ArrayList<Piece> strt = piecenode.move(moves, piece);
 		if (strt != null && strt.size() > 0) {
 			for (int i = 0; i < strt.size(); i++) {
 				toStart(strt.get(i));
 			}
 		}
 	}
 
 	public Node findNode(Piece piece) {
 		return this.board.getCornerPointers()[0].findNodeWithPiece(piece);
 	}
 
 	public Node findNodeByPosition(int i) {
 		return this.board.getCornerPointers()[0].findNodeWithPosition(i);
 	}
 
 	public Card getNextCard() {
 		this.currentCard = this.deck.getTopCard();
 		return this.currentCard;
 
 	}
 
 	/*
 	 * private Node findNode(Piece pawn, Node next) { if (next == null) { return
 	 * null; } Piece[] currentPieces = next.getPieces(); if
 	 * (contains(currentPieces, pawn)) { return next; }
 	 * 
 	 * if (next instanceof SlideNode) { SlideNode slide = (SlideNode) next; if
 	 * (slide.getColor() == pawn.col && !(slide.getSafeNode() instanceof
 	 * MultiNode)) { Node maybeFoundIt = findNode(pawn, slide.getSafeNode()); if
 	 * (maybeFoundIt != null) return maybeFoundIt; } if (slide.getColor() ==
 	 * pawn.col && slide.getSafeNode() != null) { // we're looking at the start
 	 * node right here for reasons if (contains(slide.getSafeNode().getPieces(),
 	 * pawn)) return slide.getSafeNode(); } }
 	 * 
 	 * if (next.getNext() == null) return null;
 	 * 
 	 * return findNode(pawn, next.getNext());
 	 * 
 	 * }
 	 */
 
 	/**
 	 * Helper method for moving pieces to the start position.
 	 * 
 	 * @param pawn
 	 */
 	protected void toStart(Piece pawn) {
 		switch (pawn.col) {
 		case red:
 			this.board.getStartPointers()[0].addPieceToPieces(pawn);
 			break;
 		case blue:
 			this.board.getStartPointers()[1].addPieceToPieces(pawn);
 			break;
 		case yellow:
 			this.board.getStartPointers()[2].addPieceToPieces(pawn);
 			break;
 		case green:
 			this.board.getStartPointers()[3].addPieceToPieces(pawn);
 			break;
 		}
 	}
 
 	/**
 	 * Helper method to determine whether or not a pawn is contained within a
 	 * given set of pawns.
 	 * 
 	 * @param pawns
 	 * @param pawn
 	 * @return
 	 */
 	protected static boolean contains(Piece[] pawns, Piece pawn) {
 		for (Piece p : pawns) {
 			if (p == pawn)
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Exception thrown is a game has not been started prior to interaction with
 	 * that game.
 	 * 
 	 * @author TeamSorryDragons
 	 */
 	protected class Unstarted extends Exception {
 		private static final long serialVersionUID = 1L;
 
 		public Unstarted(String message) {
 			super(message);
 		}
 	}
 
 	/**
 	 * Returns the node at the given coordinate position.
 	 * 
 	 * @param coordinate
 	 * @return node corresponding to the given coordinate
 	 */
 	public Node convertCoordToNode(SorryFrame.Coordinate coordinate) {
 		// TODO this
 		return this.findNodeByPosition(this.getNodePosition(coordinate));
 	}
 
 	protected static int getNodePosition(SorryFrame.Coordinate coord) {
 		if (coordsMap == null)
 			populateCoordsMap();
 		if (coordsMap.containsKey(coord))
 			return coordsMap.get(coord);
 		throw new CoordinateOffOfBoardException("Bad coordinate access: "
 				+ coord.getX() + " " + coord.getY());
 	}
 
 	private static void populateCoordsMap() {
 		coordsMap = new HashMap<SorryFrame.Coordinate, Integer>();
 		populateCorners();
 		populateStartZones();
 		populateHomeZones();
 		populateSafeZones();
 		populateSideLines();
 	}
 
 	private static void populateSideLines() {
 		mapInsert(14, 15, 1);
 		mapInsert(13, 15, 2);
 		mapInsert(12, 15, 9);
 		mapInsert(11, 15, 10);
 
 		mapInsert(0, 14, 23);
 		mapInsert(0, 13, 24);
 		mapInsert(0, 12, 31);
 		mapInsert(0, 11, 32);
 
 		mapInsert(1, 0, 45);
 		mapInsert(2, 0, 46);
 		mapInsert(3, 0, 53);
 		mapInsert(4, 0, 54);
 
 		mapInsert(15, 1, 67);
 		mapInsert(15, 2, 68);
 		mapInsert(15, 3, 75);
 		mapInsert(15, 4, 76);
 
 	}
 
 	private static void mapInsert(int x, int y, int pos) {
 		coordsMap.put(new SorryFrame.Coordinate(x, y), pos);
 
 	}
 
 	private static void populateSafeZones() {
 		int redZone = 2;
 		for (int i = 15; i >= 10; i--)
 			coordsMap.put(new SorryFrame.Coordinate(13, i), redZone++);
 
 		int blueZone = 24;
 		for (int i = 0; i <= 5; i++)
 			coordsMap.put(new SorryFrame.Coordinate(i, 13), blueZone++);
 
 		int yellowZone = 46;
 		for (int i = 0; i <= 5; i++)
 			coordsMap.put(new SorryFrame.Coordinate(2, i), yellowZone++);
 
 		int greenZone = 68;
 		for (int i = 15; i >= 10; i--)
 			coordsMap.put(new SorryFrame.Coordinate(i, 2), greenZone++);
 	}
 
 	private static void populateHomeZones() {
 		int redHome = 8;
 		for (int i = 14; i >= 12; i--)
 			for (int j = 9; j >= 7; j--)
 				coordsMap.put(new SorryFrame.Coordinate(i, j), redHome);
 
 		int blueHome = 30;
 		for (int i = 6; i <= 8; i++)
 			for (int j = 14; j >= 12; j--)
 				coordsMap.put(new SorryFrame.Coordinate(i, j), blueHome);
 
 		int yellowHome = 52;
 		for (int i = 1; i <= 3; i++)
 			for (int j = 6; j <= 8; j++)
 				coordsMap.put(new SorryFrame.Coordinate(i, j), yellowHome);
 
 		int greenHome = 74;
 		for (int i = 7; i <= 9; i++)
 			for (int j = 1; j <= 3; j++)
 				coordsMap.put(new SorryFrame.Coordinate(i, j), greenHome);
 
 	}
 
 	private static void populateStartZones() {
 		int redStart = 11;
 		for (int i = 10; i <= 12; i++) {
 			for (int j = 12; j <= 14; j++) {
 				coordsMap.put(new SorryFrame.Coordinate(i, j), redStart);
 			}
 		}
 
 		int blueStart = 33;
 		for (int i = 10; i <= 12; i++) {
 			for (int j = 1; j <= 3; j++) {
 				coordsMap.put(new SorryFrame.Coordinate(j, i), blueStart);
 			}
 		}
 
 		int yellowStart = 55;
 		for (int i = 3; i <= 5; i++) {
 			for (int j = 1; j <= 3; j++) {
 				coordsMap.put(new SorryFrame.Coordinate(i, j), yellowStart);
 			}
 		}
 
 		int greenStart = 77;
 		for (int i = 3; i <= 5; i++) {
 			for (int j = 12; j <= 14; j++)
 				coordsMap.put(new SorryFrame.Coordinate(j, i), greenStart);
 		}
 	}
 
 	private static void populateCorners() {
 		int redSide = 12;
 		int blueSide = 34;
 
 		for (int i = 10; i >= 0; i--) {
 			coordsMap.put(new SorryFrame.Coordinate(i, 15), redSide++);
 			coordsMap.put(new SorryFrame.Coordinate(0, i), blueSide++);
 		}
 
 		int greenSide = 78;
 		int yellowSide = 56;
 		for (int i = 5; i <= 15; i++) {
 			coordsMap.put(new SorryFrame.Coordinate(i, 0), yellowSide++);
 			coordsMap.put(new SorryFrame.Coordinate(15, i), greenSide++);
 		}
 		coordsMap.put(new SorryFrame.Coordinate(15, 15), 0);
 
 	}
 
 	/**
 	 * Get the next player.
 	 * 
 	 */
 	public void rotatePlayers() {
 		if (this.activePlayer != null)
 			this.activePlayer.setActive(false);
 		this.players.goToNextElement();
 		this.activePlayer = this.players.getActualElementData();
 		this.activePlayer.setActive(true);
 
 	}
 
 	/**
 	 * 
 	 * Obtain the actual board for the current game session in which this game
 	 * driving engine is in use. Likely to draw it for the player to witness the
 	 * magnificence of this computerized Sorry! board game, which they are now
 	 * playing for entertainment purposes.
 	 * 
 	 * @return board
 	 */
 	public BoardList getActualBoard() {
 		return this.board; // actualBoard;
 	}
 
 	/**
 	 * Finalize the player's turn by setting the temporary movement board to the
 	 * real board. Reset the temp board.
 	 * 
 	 */
 	public boolean finalizeTurn() {
 		// TODO implement it, when the time comes
 		this.actualBoard = this.board.clone();
 		return hasWon();
 	}
 
 	/**
 	 * 
 	 * Reverts the current board to the state prior to starting the current
 	 * player's turn. Used in the event of a turn forfeit.
 	 * 
 	 */
 	public void revertBoard() {
 		// TODO implement once a board can be read from a file.
 		this.board = this.actualBoard.clone();
 		return;
 	}
 
 	public boolean hasWon() {
 		int spot = 0;
 		switch (this.activePlayer.getColor()) {
 		case red:
 			spot = 0;
 			break;
 		case blue:
 			spot = 1;
 			break;
 		case yellow:
 			spot = 2;
 			break;
 		case green:
 			spot = 3;
 			break;
 		}
 		Piece[] piecesInHome = this.board.getHomePointers()[spot].getPieces();
 		for (int i = 0; i < 4; i++) {
 			if (piecesInHome[i] == null) {
 				return false;
 			}
 		}
 		return true;
 	}
 }
