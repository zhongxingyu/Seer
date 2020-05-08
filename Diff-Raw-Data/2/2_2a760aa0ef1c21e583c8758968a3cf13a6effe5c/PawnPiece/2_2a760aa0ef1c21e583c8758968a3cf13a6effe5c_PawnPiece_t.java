 
 public class PawnPiece extends Piece
 {
 
 	public PawnPiece(boolean white, int xCord, int yCord, Piece[][] pieceBoard, boolean[][]whiteMoves, boolean[][]blackMoves) 
 	{
 		super(white, xCord, yCord, pieceBoard,whiteMoves,blackMoves);
 
 		this.pieceType = "Pawn";
 		isPawn = true;
 	}
 
 	public void setMoves()
 	{
 		getColorValue();
 		int cVal = getColorValue();
 			int y=(yCord+cVal); //for black and white
 			if(isValid(xCord,y))
 			{
 				if(isEmpty(xCord,y))
 					canMove[xCord][y] = true;
 
 				if(isValid(xCord+1,y)&&!isEmpty(xCord+1,y))
 					canMove[xCord+1][y] = true;
 
 				if(isValid(xCord-1,y)&&!isEmpty(xCord-1,y))
 					canMove[xCord-1][y] = true;
 				
				if(!moved&&isEmpty(xCord,y)&&isEmpty(xCord,y+cVal))
 						canMove[xCord][y+cVal] = true;
 			}
 
 		addBlackAndWhiteMoves();
 	}
 }
