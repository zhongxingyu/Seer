 package GameObjects;
 
 import java.util.ArrayList;
 
 public class Board extends BoardBase
 {
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
 	 * Constructs a board for playing the Drop7 game.
 	 */
 	public Board()
 	{
 		super();
 	}
 		
 	/**
 	 * Given a position, this function will create a new piece and update the piece
 	 * If the insert is successful, then a check will be performed to determine if 
 	 * @see checkForRemoval, removeMarked
 	 * @param position The column number to insert the piece. Do not use with array indexes
 	 * @return True if the insert was successful, else returns false
 	 * @throws IllegalArgumentException If an incorrect position or value is given+
 	 */
 	public boolean insert(int position, int value)
 	{
 		if((position < 1) || (position > 7) || (value > 8))
 		{
 			throw new IllegalArgumentException();
 		}
 		
 		//subtracting one to calibrate the position to work with array indexes
 		position--;
 		
 		for (int index = 6; index >= 0; index--)
 		{
 			Piece current = pieceAt(index, position);
 			if(current.getType() == Piece.Type.EMPTY)
 			{				
 				//current.setType(value == 8 ? Piece.Type.MYSTERY1 : Piece.Type.SET);	
 				current.setValue(value);
 				checkForRemoval(current);				
 				return true;
 			}
 		}
 		return false;
 	}
         
 
 	/**
 	 * Given a piece, this functions will check if a piece should be removed
 	 * @param p The piece to perform the operation on
 	 */
 	protected void checkForRemoval(Piece p)
 	{
 		ArrayList<Piece> rows = getAllInRow(p);
 		ArrayList<Piece> columns = getAllInColumn(p);
 				
 		for(Piece item : rows)
 		{	
 			int value = item.getValue();
 			if((value == getColumnAdjacent(item)) || (value == getRowAdjacent(item)))
 			{
 				item.setRemove(true);
 			}
 		}
 		
 		for(Piece item: columns)
 		{
 			int value = item.getValue();
 			if((value == getColumnAdjacent(item)) || (value == getRowAdjacent(item)))
 			{
 				item.setRemove(true);
 			}
 		}
 		
 		System.out.println("Now inside checkforremove");
 		System.out.println("The number of removed is " + this.getAllRemove());
 		removeMarked();
 	}
 	
 	/**
 	 * Searches the entire board for all removed pieces.
 	 * Removes all the removed pieces on the board.
 	 */
 	protected void removeMarked()
 	{
 		ArrayList<Piece> marked = getAllRemove();
 		for(Piece item : marked)
 		{
 			System.out.println("here???");
 			item.setType(Piece.Type.EMPTY);
			item.setRemove(false);
 		}
 	}
 	
 	public static void main(String[] args)
 	{		
 
 	}
 }
