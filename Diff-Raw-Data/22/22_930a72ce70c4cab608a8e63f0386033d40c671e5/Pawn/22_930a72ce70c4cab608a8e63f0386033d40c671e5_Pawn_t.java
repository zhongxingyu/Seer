 public class Pawn extends AbstractPiece {
 
 	public Pawn(PieceColor color){
 		super(color);
 	}
 	
 	@Override
 	public boolean canMove(String from, String to, Board board) {
		int rd =Board.getRowDistance(from, to);
		int cd =Board.getColumnDistance(from, to);
		if (this.getPieceColor()==PieceColor.WHITE) {//White player
			if (Board.getRowIndex(from)==1) {
				return rd <= 2 && cd == 0 && rd>0;
			}
			else {
				return rd == 1 && cd == 0;
			}
 		}
		else {//Black player
			if (Board.getRowIndex(from)==6) {

				return rd >= -2 && cd== 0 && rd<0;
			}
			else {
				return rd == -11 && cd == 0;
			}
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
