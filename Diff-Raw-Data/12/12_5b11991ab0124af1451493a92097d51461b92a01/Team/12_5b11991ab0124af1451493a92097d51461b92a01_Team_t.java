 package edu.neumont.learningChess.model;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import edu.neumont.learningChess.api.Location;
 
 
 public class Team {
 
 	
 	public enum Color {
 		LIGHT,
 		DARK
 	}
 
 	private Color color;
 	private King king;
	private List<ChessPiece> workingPieces = new ArrayList<ChessPiece>();
 	
 	public Team(Color color) {
 		this.color = color;
 	}
 	
 	public void add(ChessPiece piece) {
 		if (piece instanceof King) {
 			king = (King) piece;
 		}
 		workingPieces.add(piece);
 		piece.setTeam(this);
 	}
 	
 	public void remove(ChessPiece piece) {
 		workingPieces.remove(piece);
 		piece.setTeam(null);
 	}
 	
 	public boolean isWhite() {
 		return color == Color.LIGHT;
 	}
 
 	public King getKing() {
 		return king;
 	}
 	
 	public boolean canAttack(ChessBoard board, Location target) {
 		boolean attacks = false;
 		for (Iterator<ChessPiece> i = workingPieces.iterator(); !attacks && i.hasNext();  ) {
 			ChessPiece piece = i.next();
 			attacks = piece.canAttack(board, target);
 		}
 		return attacks;
 	}
 	
 	public Iterator<ChessPiece> getPieces() {
		List<ChessPiece> workingPiecesClone = new ArrayList<ChessPiece>(workingPieces);
		return workingPiecesClone.iterator();
 	}
 	
 	public int getPieceCount() {
 		return workingPieces.size();
 	}
 	
 	public Iterator<Move> getMoves(ChessBoard board) {
 		ArrayList<Move> moves = new ArrayList<Move>();
		for (Iterator<ChessPiece> i = getPieces(); i.hasNext();  ) {
 			ChessPiece piece = i.next();
 			for (Iterator<Location> e = piece.getLegalMoves(board); e.hasNext(); ) {
 				Location to = e.next();
 				Move move = new Move(piece.getLocation(), to);
 				moves.add(move);
 			}
 		}
 		return moves.iterator();
 	}
 	
 
 	@SuppressWarnings("rawtypes")
 	public boolean onlyHasPieces(Class<?extends ChessPiece> ... pieces) {
 		List<Class> chessPieces = new ArrayList<Class>();
 		chessPieces.addAll(Arrays.asList(new Class[]{Pawn.class, Rook.class, Bishop.class, Queen.class, King.class, Knight.class}));
 		boolean onlyHasPieces = workingPieces.size() == pieces.length;
 		if(onlyHasPieces) {
 			int[] pieceTypeCount = new int[6];
 			List<Class> workingPiecesList = new ArrayList<Class>();
 			for (ChessPiece piece : workingPieces) {
 				workingPiecesList.add(piece.getClass());
 			}
 			for (Class piece : workingPiecesList) {
 				pieceTypeCount[chessPieces.indexOf(piece)]++;
 			}
 			for (Class piece : pieces) {
 				pieceTypeCount[chessPieces.indexOf(piece)]--;
 			}
 			onlyHasPieces = Arrays.equals(pieceTypeCount, new int[6]);
 		}
 		return onlyHasPieces;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("Team [color=");
 		builder.append(color);
 		builder.append(", workingPieces=");
 		builder.append(workingPieces);
 		builder.append("]");
 		return builder.toString();
 	}
 
 	
 }
