 package se.chalmers.chessfeud.model;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import se.chalmers.chessfeud.model.pieces.NoPiece;
 import se.chalmers.chessfeud.model.pieces.Piece;
 import se.chalmers.chessfeud.model.utils.Position;
 import android.util.Log;
 /**
  * A class that implements the model of a chessgame
  * @author grubla
  *
  */
 public class ChessModel {
 	private ChessBoard chessBoard;
 	private int activePlayer;
 	private Position selected;
 	private List<Position> possibleMoves;
 	private List<Piece> takenPieces;
 	
 	/**
 	 * Creates an instance of chess with a new starting board. 
 	 */
 	public ChessModel(){
 		chessBoard = new ChessBoard();
 		activePlayer = 0;
 		selected = null;
 		possibleMoves = new LinkedList<Position>();
 		takenPieces = new ArrayList<Piece>();
 	}
 	
 	/**
 	 * Represents a click on the board, the first click being choosing a piece and the second where it should go
 	 * @param p, the position clicked
 	 */
 	public void click(Position p){
 		Log.d("Pos:", p.toString());
 		if(selected != null){ //Some piece is selected
 			if(possibleMoves.contains(p)){ //A valid move has been clicked
 				Piece pi = chessBoard.movePiece(selected, p);
 				deselectPiece();
 				if(!(pi instanceof NoPiece))
 					takenPieces.add(pi);
 				changeTurn();
 				if(Rules.isCheck(chessBoard, activePlayer))
 					if(Rules.isCheckMate(chessBoard, activePlayer)){
 						//Check mate
 					}else{
 						//CHeck
 					}
 				else if(Rules.isDraw(chessBoard, activePlayer)){
 						//Draw
 				}else{
 					//Next Turn
 				}
 						
 					
 				//Check for check draw etc
 				//Next turn or gameOver
 			}else{ //Clicked on a place the piece cannot go
 				if(chessBoard.getPieceAt(p).getTeam() == activePlayer){ 
 					if(p.equals(selected)){ //Clicked on the same piece twice
 						deselectPiece();
 					}else{ //Clicked on a new piece in its team
 						setSelected(p);
 					}
 				}else{ //Clicked on an enemy piece where it cannot go
 					deselectPiece();
 				}
 			}
 		}else{
 			if(chessBoard.getPieceAt(p) != null && chessBoard.getPieceAt(p).getTeam() == activePlayer){ //Clicked on a new valid piece
 				setSelected(p);
 			}
 		}
 	}
 	/*
 	 * Returns the possible moves (according to ALL rules) for the chosen piece.
 	 * @param pos, the position of the chosen piece
 	 */
 	private void setPossibleMoves(){
 		possibleMoves.clear();
 		possibleMoves = Rules.getPossibleMoves(chessBoard, selected);
 	}
 	/**
 	 * Returns the possible moves in a list och positions.
 	 */
 	public List<Position> getPossibleMoves(){
 		return possibleMoves;
 	}
 	
 	/**
 	 * Returns the active player where 0 is "white" and 1 is "black"
 	 * @return the player who's turn it is to move
 	 */
 	public int acitvePlayer(){
 		return activePlayer;
 	}
 	
 	/**
 	 * Returns the piece at the given position
 	 * @param p, a position on the board 0 <= x,y < 8 
 	 * @return a Piece object
 	 */
 	public Piece getPieceAt(Position p){
 		return chessBoard.getPieceAt(p.getX(), p.getY());
 	}
 	/**
 	 * Returns a list with all the taken pieces
 	 * @return
 	 */
 	public List<Piece> getTakenPieces(){
 		return takenPieces;
 	}
 	
 	/* Sets the current position as the selected one */
 	private void setSelected(Position p){
 		selected = p;
 		setPossibleMoves();
 	}
 	
 	/* Deselcts the current selected piece */
 	private void deselectPiece(){
 		selected = null;
 	}
 	
 	/**
 	 * Return the selected position or null if not any selected
 	 * @return
 	 */
 	public Position getSelectedPosition(){
 		return selected;
 	}
 	
 	/* Changes the turn (the actve player) */
 	private void changeTurn(){
 		activePlayer = (activePlayer+1)%2;
 	}
 	
 }
