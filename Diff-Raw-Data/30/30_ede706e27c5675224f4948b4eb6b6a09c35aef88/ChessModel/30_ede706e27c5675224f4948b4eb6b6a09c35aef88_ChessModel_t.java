 package se.chalmers.chessfeud.model;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import se.chalmers.chessfeud.model.pieces.Piece;
 import se.chalmers.chessfeud.model.utils.Position;
 import se.chalmers.chessfeud.utils.C;
 import se.chalmers.chessfeud.utils.Game;
 
 /**
  * A class that implements the model of a chessgame. Basically it is run by the
  * method click(Position p), where every call for the method symbolizes a click
  * on the board. The first click should choose a valid piece and the second
  * click should be where to move the piece. (If it is not it might select
  * another piece or deselect it).
  * 
  * @author Henrik Alburg
  * 
  *         Copyright (c) 2012 Henrik Alburg, Arvid Karlsson
  * 
  */
 public class ChessModel {
 	private ChessBoard chessBoard;
 	private int numberOfMoves;
 	private Position selected;
 	private List<Position> possibleMoves;
 	private List<Piece> takenPieces;
 	private int state;
 	private final int thisPlayer;
 	private Game gameInfo;
 	private PropertyChangeListener listener;
 
 	/**
 	 * Creates an instance of chess with a new starting board.
 	 * 
 	 * @param thisPlayer
 	 *            , this is the player who is on the current client.
 	 *            C.TEAM_WHITE or C.TEAM_BLACK
 	 */
 	public ChessModel(PropertyChangeListener pcl) {
 		gameInfo = null;
 		thisPlayer = 0;
 		listener = pcl;
 		chessBoard = new ChessBoard();
 		numberOfMoves = 0;
 		selected = null;
 		possibleMoves = new LinkedList<Position>();
 		takenPieces = new ArrayList<Piece>();
 		state = C.STATE_NORMAL;
 	}
 
 	/**
 	 * Creates a model from the gameInfo object. This model will also be able to
 	 * send its changes.
 	 * 
 	 * @param gameInfo
 	 *            , information about the game.
 	 * @param pcl
 	 *            , the proportychangelistener to whom a changed model shall be
 	 *            sent.
 	 */
 	public ChessModel(Game gameInfo, PropertyChangeListener pcl) {
 		this.gameInfo = gameInfo;
 		chessBoard = new ChessBoard(gameInfo.getGameBoard());
 		this.numberOfMoves = gameInfo.getTurns();
 		this.thisPlayer = gameInfo.thisPlayersTeam();
 		selected = null;
 		possibleMoves = new LinkedList<Position>();
 		takenPieces = new ArrayList<Piece>();
 		this.listener = pcl;
 		checkState();
 	}
 
 	/**
 	 * Exports the model on the format chessboard/numberOfMoves. Where
 	 * chessboard is the way to use the ChessBoard(String s) constructor and
 	 * numberOfMoves is an int (as a string).
 	 * 
 	 * @return a string representation of the model.
 	 */
 	public String exportModel() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(chessBoard.exportBoard());
 		sb.append("/");
 		sb.append("" + numberOfMoves);
 		return sb.toString();
 	}
 
 	/**
 	 * Represents a click on the board. The first click being choosing a piece.
 	 * and when somthing is selected the click is where it shall go.
 	 * 
 	 * @param p
 	 *            , the position clicked.
 	 */
 	public void click(Position p) {
 		if (selected != null) { // Some piece is selected
 			clickSelected(p);
 		} else {
 			if (isMoveable(p)) { // Clicked on a new valid piece
 				setSelected(p);
 			}
 		}
 	}
 
 	//TODO: When pawn transforms wait to send model..
 	/* Makes a click when a position already is selected */
 	private void clickSelected(Position p) {
 		if (possibleMoves.contains(p)) { // A valid move has been clicked
 			//Check if a pawn has reached the other side
 			int pawnY = chessBoard.getPieceAt(selected).getTeam() == C.TEAM_WHITE ? 0 : 7;
 			if(chessBoard.getPieceAt(selected).getId() == C.PIECE_PAWN && p.getY() == pawnY){
 				listener.propertyChange(new PropertyChangeEvent(this, "Pawn", p, null));
 				movePiece(p);
 			}else{
 				movePiece(p);
 				sendModel();
 			}
 		} else { // Clicked on a place the piece cannot go
 			selectNewPiece(p);
 		}
 	}
 
 	/* Makes a click when no position is currently selected */
 	private void selectNewPiece(Position p) {
 		if (isMoveable(p)) {
 			if (p.equals(selected)) { // Clicked on the same piece twice
 				deselectPiece();
 			} else { // Clicked on a new piece in its team
 				setSelected(p);
 			}
 		} else { // Clicked on an enemy piece where it cannot go
 			deselectPiece();
 		}
 	}
 
 	/*
 	 * Moves a piece from one position to another, changing turn, updating state
 	 * and tries to send the model.
 	 */
 	private void movePiece(Position p) {
 		Piece pi = chessBoard.movePiece(selected, p);
 		if (pi.getId() != C.PIECE_NOPIECE) {
 			takenPieces.add(pi);
 		}
 		deselectPiece();
 		changeTurn();
 		checkState();
 	}
 
 	/*
 	 * Sets the possibleMoves list with the moves the current selected piece is
 	 * able to do.
 	 */
 	private void setPossibleMoves() {
 		possibleMoves.clear();
 		possibleMoves = Rules.getPossibleMoves(chessBoard, selected);
 	}
 
 	/**
 	 * Gets the possible moves for the currently selected piece.
 	 * 
 	 * @return a list of poisition (The moves).
 	 */
 	public List<Position> getPossibleMoves() {
 		return possibleMoves;
 	}
 
 	/**
 	 * Returns the piece at the given position.
 	 * 
 	 * @param p
 	 *            , a position on the board 0 <= x,y < 8.
 	 * @return a Piece object, which is at the given pos.
 	 */
 	public Piece getPieceAt(Position p) {
 		return chessBoard.getPieceAt(p.getX(), p.getY());
 	}
 
 	/**
 	 * Returns a list with all the taken pieces.
 	 * 
 	 * @return All the taken Pieces in a list.
 	 */
 	public List<Piece> getTakenPieces() {
 		// Continue this
 		return takenPieces;
 	}
 
 	/* Sets the current position as the selected one */
 	private void setSelected(Position p) {
 		selected = p;
 		setPossibleMoves();
 	}
 
 	/* Deselects the current selected piece (if any) */
 	private void deselectPiece() {
 		selected = null;
 		possibleMoves.clear();
 	}
 
 	/**
 	 * Returns the status of the game. It will be one of the following;
 	 * C.STATE_NORMAL, C.STATE_CHECK, C.STATE_CHECKMATE, C.STATE_DRAW
 	 * 
 	 * @return a state from the class C.
 	 */
 	public int getState() {
 		return this.state;
 	}
 
 	/*
 	 * Manually checks and updates the state. Also all the elses are needed to
 	 * make sure it will only get into on if/else.
 	 */
 	private void checkState() {
 		if (Rules.isCheck(chessBoard, activePlayer())) {
 			if (Rules.isCheckMate(chessBoard, nextTurn())) {
 				if (activePlayer() == C.TEAM_WHITE) {
 					setState(C.STATE_VICTORY_WHITE);
 				} else {
 					setState(C.STATE_VICTORY_BLACK);
 				}
 			} else {
 				setState(C.STATE_CHECK);
 			}
 		} else {
 			if (Rules.isDraw(chessBoard, nextTurn())) {
 				setState(C.STATE_DRAW);
 			} else {
 				setState(C.STATE_NORMAL);
 			}
 		}
 	}
 
 	/* Sets the state of the game */
 	private void setState(int newState) {
 		this.state = newState;
 	}
 
 	/**
 	 * Return the selected position or null if not any selected.
 	 * 
 	 * @return the currently selected position.
 	 */
 	public Position getSelectedPosition() {
 		return selected;
 	}
 
 	/* Changes the turn (the active player) */
 	private void changeTurn() {
 		numberOfMoves++;
 	}
 
 	/**
 	 * Returns the active player where 0 is "white" and 1 is "black". Will
 	 * return -1 if the piece doesn't have any team (noPiece).
 	 * 
 	 * @return the player who's turn it is to move.
 	 */
 	public int activePlayer() {
 		return numberOfMoves % 2;
 	}
 
 	/**
 	 * Changes the pawn at the given position to a new Piece. This is happening
 	 * when a pawn walks across the board to become a Queen, Rook, Bishop or
 	 * Knight.
 	 * 
 	 * @param p
 	 *            The position of the pawn.
 	 * @param id
 	 *            The id of the new Piece.
 	 */
 	public void changePawnTo(Position p, int id) {
 		chessBoard.setPiece(p, id, chessBoard.getPieceAt(p).getTeam());
 		sendModel();
 	}
 
 	/*
 	 * Returns true if the piece at the given position is the same team as the
 	 * player trying to move it and it is his or hers turn.
 	 */
 	private boolean isMoveable(Position p) {
 		if (gameInfo == null) {
 			return true;
 		}
 		int activePlayer = numberOfMoves % 2;
 		if (activePlayer == this.thisPlayer) {
 			return chessBoard.getPieceAt(p).getTeam() == activePlayer;
 		}
 		return false;
 	}
 
 	/* Returns whom's turn it is next (opposite of activePlayer) */
 	private int nextTurn() {
 		int next = numberOfMoves % 2;
 		if (next == 1) {
 			return 0;
 		} else {
 			return 1;
 		}
 	}
 
 	/* Sends the model to the given PropertyChangeListener */
 	private void sendModel() {
 		if (gameInfo != null) {
 			PropertyChangeEvent event = new PropertyChangeEvent(this, "Model",
 					gameInfo, this.exportModel());
 			this.listener.propertyChange(event);
 		}
 	}
 
 }
