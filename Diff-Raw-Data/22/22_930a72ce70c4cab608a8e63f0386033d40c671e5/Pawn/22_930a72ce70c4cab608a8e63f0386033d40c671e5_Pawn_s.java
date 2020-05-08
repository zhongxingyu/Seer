 public class Pawn extends AbstractPiece {
 
 	public Pawn(PieceColor color){
 		super(color);
 	}
 	
 	@Override
 	public boolean canMove(String from, String to, Board board) {
		if (this.getPieceColor()==PieceColor.WHITE) {
			return Board.getRowDistance(from, to)==1 && Board.getColumnDistance(from, to)==0;
 		}
		else {
			return Board.getRowDistance(from, to)==-1 && Board.getColumnDistance(from, to)==0;
 		}
 	}
 	
 	@Override
 	public boolean canTake(String from, String to, Board board) {
 		if (this.getPieceColor()==PieceColor.WHITE) {
 			return Board.getRowDistance(from, to)==1 && Math.abs(Board.getColumnDistance(from, to))==1;
 		}
 		else {
 			return Board.getRowDistance(from, to)==-1 && Math.abs(Board.getColumnDistance(from, to))==1;
 		}
 	}
 
 }
