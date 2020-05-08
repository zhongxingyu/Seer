 
 public class KingPiece extends Piece{
 
 
 
 	public KingPiece(Board board,boolean white, int xCord, int yCord, Piece[][] pieceBoard, boolean[][]whiteMoves, boolean[][]blackMoves) 
 	{
 		super(board,white, xCord, yCord, pieceBoard,whiteMoves,blackMoves);
 
 		this.pieceType = "TKing";
 		isKing = true;
 	}
 
 	public void setMoves()
 	{
 
 		int cVal = getColorValueKing();
 
 		for(int y=-1; y<2; y++)
 		{
 			for(int x=-1; x<2; x++)
 			{
 				if(isValid(xCord+x,yCord+y)&&(!sameColor(xCord+x, yCord+y)||isEmpty(xCord+x,yCord+y)))
 				{				
 					if((white && !blackMoves[xCord+x][yCord+y]) || (!white && !whiteMoves[xCord+x][yCord+y]))	
 						canMove[xCord+x][yCord+y]=true;
 
 				}
 			}
 		}
 
		if(pBoard.getTurnCount()>0 && !moved) //King couldn't have moved
 		{
 			if(!isEmpty(xCord-4,cVal) && !pieceBoard[xCord-4][cVal].moved)//rook involved couldn't have moved
 			{
 				for(int x=-1; x>-3; x--)
 				{
 					if(!isEmpty(xCord+x,cVal) || ((white && blackMoves[xCord+x][cVal]) || (!white && whiteMoves[xCord+x][cVal])))
 						break;
 					if(x==-2)
 						canMove[xCord+x][cVal] = true;
 				}
 			}
 
 			if(!isEmpty(xCord+3,cVal) && !pieceBoard[xCord+3][cVal].moved)//rook involved couldn't have moved
 			{
 				for(int x=1; x<3; x++)
 				{
 					if(!isEmpty(xCord+x,cVal) || ((white && blackMoves[xCord+x][cVal]) || (!white && whiteMoves[xCord+x][cVal])))
 						break;
 					if(x == 2)
 						canMove[xCord+x][cVal] = true;
 				}
 			}
 		}
 
 		addBlackAndWhiteMoves();
 	}
 	
 	public void setImage()
 	{
 		super.setImage();
 		pieceImage = pieceImage.getSubimage(0, ySpacing, 70, 70);
 	}
 
 }
