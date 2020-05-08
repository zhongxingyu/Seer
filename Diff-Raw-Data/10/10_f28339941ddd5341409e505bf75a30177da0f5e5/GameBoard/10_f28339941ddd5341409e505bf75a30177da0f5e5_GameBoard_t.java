 package abstraction.migration;
 
 import abstraction.AbstractBoard;
 import java.util.ArrayList;
 
 public class GameBoard extends AbstractBoard<GamePiece>
 
 {
 	//<editor-fold defaultstate="collapsed" desc="RETRIEVAL FUNCTIONS">
 	/**
 	* calculates the number of adjacent pieces in the same (horizontal) row as a
 	* given parameter piece (Including itself: e.g. A piece with no pieces next to it
 	* will return a value of 1)
 	* @param p the parameter piece to perform the calculation on
 	* @return the number of pieces that are in the same row as the parameter piece.
 	*/
 	public int getRowAdjacent(GamePiece p)
 	{
 		if((p == null) || (p.getType() == PieceType.EMPTY))
 		{
 			return 0;
 		}
                 
 		GamePiece current = p;
 		int numAdjacent = 0;
 		while((getLeftPiece(current) != null) && (getLeftPiece(current).getType() != PieceType.EMPTY))
 		{
 			numAdjacent++;
 			current = getLeftPiece(current);
 		}
                 
 		current = p;
                 
 		while((getRightPiece(current) != null) && (getRightPiece(current).getType() != PieceType.EMPTY))
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
 	public int getColumnAdjacent(GamePiece p)
 	{
 		if((p == null) || (p.getType() == PieceType.EMPTY))
 		{
 			return 0;
 		}
             
 		GamePiece current = p;
 
 		int numAdjacent = 0;
 
 		//get the pieces below
 		while((getUpPiece(current) != null) && (getUpPiece(current).getType() != PieceType.EMPTY))
 		{
 			numAdjacent++;
 			current = getUpPiece(current);
 		}
                 
 		current = p; //reset current: need to count the down pieces
                 
 		//get the pieces above
 		while((getDownPiece(current) != null) && (getDownPiece(current).getType() != PieceType.EMPTY))
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
 	public ArrayList<GamePiece> getAllInRow(GamePiece p)
 	{
 		if (p == null)
 		{
 			throw new NullPointerException();
 		}
 		int row = p.getHorizontal();
 
 		ArrayList<GamePiece> arrayList = new ArrayList<>();
 		for(int j = 0; j < 7; j++)
 		{
 			GamePiece temp = pieceAt(row, j);
 
 			if((temp != null) && (temp.getType() != PieceType.EMPTY))
 			{
 				arrayList.add(temp);
 			}
 		}
 		return arrayList;
 	}
         
 	/**
 	* @param p The piece to perform the method on
 	* @return A list of pieces that are in the same column as the parameter piece
 	*/
 	public ArrayList<GamePiece> getAllInColumn(GamePiece p)
 	{
 		if (p == null)
 		{
 			throw new NullPointerException();
 		}
 		
 		int column = p.getVertical();
 		
 		ArrayList<GamePiece> arrayList = new ArrayList<>();
 			
 		for(int i = 0; i < 7; i++)
 		{
 			GamePiece temp = pieceAt(i, column);
 			if((temp != null) && (temp.getType() != PieceType.EMPTY))
 			{
 				arrayList.add(temp);
 			}
 		}
 		return arrayList;
 	}
 	
 	/**
 	* @return A list of pieces that are marked as remove
 	*/
 	public ArrayList<GamePiece> getAllRemove()
 	{
 		ArrayList<GamePiece> list = new ArrayList<>();
 
 		for(int i = 0; i < 7; i++)
 		{
 			for(int j = 0; j < 7; j++)
 			{
 				GamePiece p = (GamePiece) pieceAt(i, j);
 				if(p.getRemove())
 				{
 					list.add(p);
 				}
 			}
 		}
 		return list;
 	}
         
 	//</editor-fold>
 	
 	public GameBoard()
 	{
 		board = new GamePiece[7][7];
 		for(int i = 0; i < 7; i++)
 		{
 			for(int j = 0; j < 7; j++)
 			{
 				board[i][j] = (i < 6) ? new GamePiece(PieceType.EMPTY, i, j) 
 						: new GamePiece(PieceType.SET, i, j);
 			}
 		}
 	}
 
 	/**
 	 * Given a position, this function will create a new piece and update the piece
 	 * If the insert is successful, then a check will be performed to determine if 
 	 * @param position The column number to insert the piece. Do not use with array indexes
 	 * @param value 
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
 			GamePiece current = (GamePiece)pieceAt(index, position);
 			if(current.getType() == PieceType.EMPTY)
 			{				
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
 	protected void checkForRemoval(GamePiece p)
 	{
 		//TODO: Clean this up. This is very risk prone.
 		ArrayList<GamePiece> rows = getAllInRow(p);
 		ArrayList<GamePiece> columns = getAllInColumn(p);
 				
 		for(GamePiece item : rows)
 		{	
 			GamePiece gamepiece = item;
 
 			int value = gamepiece.getValue();
 			if((value == getColumnAdjacent(item)) || (value == getRowAdjacent(item)))
 			{
 				gamepiece.setRemove(true);
 			}
 		}
 		removeMarked();
 	}
 	
 	/**
 	 * Searches the entire board for all removed pieces.
 	 * Removes all the removed pieces on the board.
 	 */
 	protected void removeMarked()
 	{
 		ArrayList<GamePiece> marked = getAllRemove();
 		for(GamePiece item : marked)
 		{
 			GamePiece gamepiece = item;
 			gamepiece.setType(PieceType.EMPTY);
 			gamepiece.setRemove(false);
 		}
 	}
 
 }
