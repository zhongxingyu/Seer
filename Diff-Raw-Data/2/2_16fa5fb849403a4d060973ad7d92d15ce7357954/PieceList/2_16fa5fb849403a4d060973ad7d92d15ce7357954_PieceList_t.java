 package shared;
 
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import client.ClientWindow;
 
 /**
  * Contains a list of the pieces on the board and includes necessary
  * functions for game operation relating to the movement and position
  * of pieces.
  * @author Clete Blackwell II
  *
  */
 public class PieceList {
 	private static final Exception InvalidMoveException = null;
 	private Piece[] pieces;
 	private MoveParser moveParse;
 	private Piece pieceSelected;
 	private Player pieceSelector;
 
 	/**
 	 * Default constructor. Initializes.
 	 */
 	public PieceList() {
 		initialize();
 	}
 
 	/**
 	 * Startup for a new PieceList().
 	 */
 	private void initialize() {
 		pieceSelected = null;
 		pieceSelector = null;
 		pieces = new Piece[64];
 
 		for(Short i = 0; i < 64; i++) {
 				pieces[i] = new Piece(PieceColor.NONE, PieceType.EMPTY);
 		}
 		moveParse = new MoveParser();
 	}
 	
 	/**
 	 * Adds a piece at the selected index in the array.
 	 * @param index index at which to add a new piece.
 	 * The previous piece at this index is removed.
 	 * @param pieceColor A PieceColor object referencing the color of the piece.
 	 * @param pieceType A PieceType object referencing the type of the piece.
 	 */
 	public void addPiece(short index, PieceColor pieceColor, PieceType pieceType) {
 		pieces[index].setColor(pieceColor);
 		pieces[index].setPieceType(pieceType);
 	}
 
 	/**
 	 * Moves pieceToMove to the newIndex.
 	 * @param newIndex New place for pieceToMove.
 	 * @param pieceToMove The Piece to move.
 	 * @param theGame The current game.
 	 * @throws Exception InvalidMoveException, if the move is invalid.
 	 */
 	public void movePiece(short newIndex, Piece pieceToMove, Game theGame) throws Exception {
 		if(!moveParse.isMoveValid(newIndex, pieceToMove)) {
 			throw InvalidMoveException;
 		}
 
 		updatePiece((short)getPieceLocation(pieceToMove), newIndex, pieceToMove);
 		pieces[getPieceLocation(pieceToMove)] = new Piece(PieceColor.NONE, PieceType.EMPTY);
 		pieces[newIndex] = pieceToMove;
 		theGame.incrementGameCounter();
 		pieceSelected = null;
 		pieceSelector = null;
 	}
 
 	/**
 	 * Updates the GUI to represent a recent piece move.
 	 * @param oldIndex Old location of piece.
 	 * @param newIndex New location of piece.
	 * @param pieceToUpdate Piece to move.
 	 */
 	private void updatePiece(short oldIndex,
 			short newIndex, Piece pieceToUpdate) {
 		ClientWindow.getChessLabels().elementAt(oldIndex).setIcon(null);
 		ClientWindow.getChessLabels().elementAt(newIndex)
 			.setIcon(getImageForPiece(pieceToUpdate));
 	}
 
 	/**
 	 * Gets the proper image for the piece.
 	 * @param piece Piece to get an image for.
 	 * @return ImageIcon for piece.
 	 */
 	public ImageIcon getImageForPiece(Piece piece) {	
 		URL pieceURL = getClass().getResource("/images/" + 
 				piece.getPieceColor().toString() + 
 				piece.getPieceType().toString() + ".png");
 		return new ImageIcon(pieceURL);
 	}
 
 	/**
 	 * First selects a piece as a candidate for moving,
 	 * then if the second selection is a valid move for that
 	 * piece, it moves the first piece to the square of the second piece.
 	 * @param pieceToWatch
 	 * @param theGame
 	 */
 	public void selectPiece(Piece pieceToWatch, Game theGame) {
 		if(pieceToWatch.getPieceColor().equals(PieceColor.NONE) &&
 				pieceSelected == null) {
 			return;
 		}
 		if(pieceSelected == null) {
 			pieceSelected = pieceToWatch;
 			pieceSelector = theGame.getPlayerForColor(pieceToWatch.getPieceColor());
 			return;
 		}
 		if((theGame.getGameCounter() % 2 == 1 &&
 				pieceSelected.getPieceColor().equals(PieceColor.WHITE)) ||
 				(theGame.getGameCounter() % 2 == 0 &&
 						pieceSelected.getPieceColor().equals(PieceColor.BLACK))) {
 			pieceSelected = null;
 			pieceSelector = null;
 			return;
 		}
 
 		try {
 			movePiece((short)getPieceLocation(pieceToWatch), pieceSelected, theGame);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		pieceSelected = null;
 		pieceSelector = null;
 	}
 
 	/**
 	 * Checks to see if a piece is selected as a candidate for moving.
 	 * @return True if a piece is selected, false otherwise.
 	 */
 	public boolean isPieceSelected() {
 		if(pieceSelected == null){
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Gets the current piece that is selected as a candidate for moving.
 	 * @return Piece that is selected. Can be null.
 	 */
 	public Piece getPieceSelected() {
 		return pieceSelected;
 	}
 
 	/**
 	 * Gets the piece at the specified index.
 	 * @param i Index to use.
 	 * @return The Piece object at the specified index.
 	 */
 	public Piece getPieceAtIndex(int index) {
 		return pieces[index];
 	}
 
 	/**
 	 * Gets the location of the piece given.
 	 * @param piece Piece to look for.
 	 * @return -1 if not found, an integer 0 through 63 otherwise.
 	 */
 	public int getPieceLocation(Piece piece) {
 		for(int i = 0; i < 64; i++) {
 			if(pieces[i] == piece) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * Gets the player that last selected a piece.
 	 * @return
 	 */
 	public Player getPieceSelector() {
 		return pieceSelector;
 	}
 }
