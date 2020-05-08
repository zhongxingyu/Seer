 package gameObjects;
 
 import utilities.PublicFunctions;
 import java.util.ArrayList;
 
 /**
  *The base board class, having to deal with retrieval functions and navigating to pieces
  */
 public class BoardBase 
 {
 	//<editor-fold defaultstate="collapsed" desc="MEMBER VARIABLES">
 	/**
 	 *The 2D array representing the actual board
 	 */
 	protected Piece[][] board;
 	//</editor-fold>
 	
 	//<editor-fold defaultstate="collapsed" desc="ACCESSORS">
         
 	/**
 	 * 
 	 * @param i index
 	 * @param j index
 	 * @return The piece to the left of the given index
 	 */
 	public Piece getLeftPiece(int i, int j)
 	{		
 		return pieceAt(i, j - 1);
 	}
 	
 	/**
 	 * 
 	 * @param p Piece
 	 * @return The piece to the left of the given piece
 	 */
 	public Piece getLeftPiece(Piece p)
 	{
 		return pieceAt(p.getHorizontal(), p.getVertical() - 1);
 	}
 	
 	/**
 	 * 
 	 * @param i index
 	 * @param j index
 	 * @return The piece to the right of the given index
 	 */
 	public Piece getRightPiece(int i, int j)
 	{
 		return pieceAt(i, j + 1);
 	}
 	
 	/**
 	 * 
 	 * @param p Piece
 	 * @return The piece to the right of the given piece
 	 */
 	public Piece getRightPiece(Piece p)
 	{
 		return pieceAt(p.getHorizontal(), p.getVertical() + 1);
 	}
 	
 	/**
 	 * 
 	 * @param i index
 	 * @param j index
 	 * @return The piece above the given index
 	 */
 	public Piece getUpPiece(int i, int j)
 	{
 		return pieceAt(i - 1, j);
 	}
 	
 	/**
 	 * 
 	 * @param p piece
 	 * @return The piece above the given piece
 	 */
 	public Piece getUpPiece(Piece p)
 	{
 		return pieceAt(p.getHorizontal() - 1, p.getVertical());
 	}
 	
 	/**
 	 * 
 	 * @param i index
 	 * @param j index
 	 * @return The piece below the given index
 	 */
 	public Piece getDownPiece(int i, int j)
 	{
 		return pieceAt(i + 1, j);
 	}
 	
 	/**
 	 * 
 	 * @param p piece
 	 * @return The piece below the given piece
 	 */
 	public Piece getDownPiece(Piece p)
 	{
 		return pieceAt(p.getHorizontal() + 1, p.getVertical());
 	}
 	
 	//</editor-fold>
 	
 	//<editor-fold defaultstate="collapsed" desc="RETRIEVAL FUNCTIONS">
 	/**
 	* calculates the number of adjacent pieces in the same (horizontal) row as a
 	* given parameter piece (Including itself: e.g. A piece with no pieces next to it
 	* will return a value of 1)
 	* @param p the parameter piece to perform the calculation on
 	* @return the number of pieces that are in the same row as the parameter piece.
 	*/
 	public int getRowAdjacent(Piece p)
 	{
 		if((p == null) || (p.getType() == Piece.Type.EMPTY))
 		{
 			return 0;
 		}
                 
 		Piece current = p;
 		int numAdjacent = 0;
 		while((getLeftPiece(current) != null) && (getLeftPiece(current).getType() != Piece.Type.EMPTY))
 		{
 			numAdjacent++;
 			current = getLeftPiece(current);
 		}
                 
 		current = p;
                 
 		while((getRightPiece(current) != null) && (getRightPiece(current).getType() != Piece.Type.EMPTY))
 		{
 			numAdjacent++;
 			current = getRightPiece(current);
 		}
 
 		numAdjacent++; //increment to include self;
                 
 		return numAdjacent;     
 	}
 	
 	/**
 	* calculates the number of adjacent pieces in the same (vertical) column as a
 	* given parameter piece (Including itself: e.g. A piece with no pieces next to it
 	* will return a value of 1)
 	* @param p the parameter piece to perform the calculation on
 	* @return the number of pieces that are in the same column as the parameter piece.
 	*/
 	public int getColumnAdjacent(Piece p)
 	{
 		if((p == null) || (p.getType() == Piece.Type.EMPTY))
 		{
 			return 0;
 		}
             
 		Piece current = p;
 		int numAdjacent = 0;
 
 		//get the pieces below
 		while((getUpPiece(current) != null) && (getUpPiece(current).getType() != Piece.Type.EMPTY))
 		{
 			numAdjacent++;
 			current = getUpPiece(current);
 		}
                 
 		current = p; //reset current: need to count the down pieces
                 
 		//get the pieces above
 		while((getDownPiece(current) != null) && (getDownPiece(current).getType() != Piece.Type.EMPTY))
 		{
 			numAdjacent++;
 			current = getDownPiece(current);
 		}
                 
 		numAdjacent++; //include self
                 
 		return numAdjacent;
 	}
         
 	/**
 	* @param p The piece to perform the method on
 	* @return A list of pieces that are in the same row as the parameter piece
 	*/
 	public ArrayList<Piece> getAllInRow(Piece p)
 	{
 		if (p == null)
 		{
 			throw new NullPointerException();
 		}
 		int row = p.getHorizontal();
 		ArrayList<Piece> list = new ArrayList<>();
 		for(int j = 0; j < 7; j++)
 		{
 			Piece temp = pieceAt(row, j);
 			if((temp != null) && (temp.getType() != Piece.Type.EMPTY))
 			{
 				list.add(temp);
 			}
 		}
 		return list;
 	}
         
 	/**
 	* @param p The piece to perform the method on
 	* @return A list of pieces that are in the same column as the parameter piece
 	*/
 	public ArrayList<Piece> getAllInColumn(Piece p)
 	{
 		if (p == null)
 		{
 			throw new NullPointerException();
 		}
 		int column = p.getVertical();
             ArrayList<Piece> list = new ArrayList<>();
             for(int i = 0; i < 7; i++)
             {
                 //System.out.println(pieceAt(row, i));
                 Piece temp = pieceAt(i, column);
                 if((temp != null) && (temp.getType() != Piece.Type.EMPTY))
                 {
                     list.add(temp);
                 }
             }
             return list;
         }
         
 	/**
 	* @return A list of pieces that are marked as remove
 	*/
 	public ArrayList<Piece> getAllRemove()
 	{
 		ArrayList<Piece> list = new ArrayList<>();
 		for(int i = 0; i < 7; i++)
 		{
 			for(int j = 0; j < 7; j++)
 			{
 				Piece p = pieceAt(i, j);
 				if(p.getRemove())
 				{
 					list.add(p);
 				}
 			}
 		}
 		return list;
 	}
         
 	//</editor-fold>
 	
 	/**
 	 * The base constructor for building a Drop7 board.
 	 * @see Board Recommended to use this class when constructing a board 
 	 * since it contains the necessary functions for playing the Drop7 Game
 	 */
 	public BoardBase()
 	{
 		board = new Piece[7][7];
 		for (int i = 0; i < 7; i++)
 		{
 			for (int j = 0; j < 7; j++)
 			{
 				board[i][j] = (i < 6) ? new Piece(Piece.Type.EMPTY, i, j) : new Piece(Piece.Type.SET, i, j);
 			}
 		}
 	}
         
 	/**
 	 * Retrieves a Piece from the board with the given indexes
 	 * @param i index
 	 * @param j index
 	 * @return The piece from the board. Null if no piece exists
 	 */
 	public Piece pieceAt(int i, int j)
 	{
 		if (PublicFunctions.isValidPosition(i) && PublicFunctions.isValidPosition(j)) 
 		{
 			return board[i][j];
 		}
 		return null;
 	}
 		
 	/**
 	 *
 	 * @return a string representing the 
 	 */
 	@Override
 	public String toString()
 	{
 		StringBuilder builder = new StringBuilder();
		builder.append("==============").append("\n");
 		for (int i = 0; i < 7; i++)
 		{
 			for (int j = 0 ; j < 7; j++)
 			{
 				builder.append(pieceAt(i,j).toString()).append(" ");
 			}
 			builder.append("\n");
 		}
 		builder.append("==============");
 		return builder.toString();
 	}
 	
 	/**
 	 *
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		Board b = new Board();
 		System.out.println(b);
 		System.out.println(b.pieceAt(6, 6));
 	}
 }
