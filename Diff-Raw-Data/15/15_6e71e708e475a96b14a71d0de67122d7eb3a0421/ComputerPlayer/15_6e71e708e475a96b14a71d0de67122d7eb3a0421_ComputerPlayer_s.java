 
 /**
  * ComputerPlayer is a algorithm that solves
  * a Sudoku puzzle through a series of moves. 
  * 
  * @author Andrew Hood 
  * @version 0.1
  */
 public class ComputerPlayer extends Player
 {
    private Gameboard board;
     
     public ComputerPlayer(Gameboard board)
     {
         super(board);
     }
     
     public Move nextMove()
     {
     	displayBoard();
     	// iterate through board. 
     	for(int i = 0; i < 9; i++)
     	{
     		for(int j = 0; j < 9; j++)
     		{
     			// continue until an empty space is found
     			if(board.isEmpty(i,j))
     			{
     				// iterate 1-9 possible values for the space
     				for(int k = 1; k <= 9; k++)
     				{
     					Tile tempTile = board.getTile(i,j);
     					Move tempMove = new Move(j,i,k);
     					// check if k is a duplicate in support structures
     					if(!tempTile.getQuad().hasDuplicate(tempMove) && 
     					   !tempTile.getVert().hasDuplicate(tempMove) &&
     					   !tempTile.getHoriz().hasDuplicate(tempMove))
     					{
     						// no duplicates found in support structures
     						// make sure the move will not fit in any other
     						// available space of the quadrant.
     						boolean onlyIndex = true;
     						Tile[] quadTiles = tempTile.getQuad().getTiles();
     						for(int l = 0; l < quadTiles.length; l++)
     						{
     							// && onlyIndex so we don't keep checking if we know it won't work. 
     							if(quadTiles[l].isEmpty() && quadTiles[l] != tempTile && onlyIndex)
     							{
     								if(!quadTiles[l].getHoriz().hasDuplicate(tempMove) && 
     								   !quadTiles[l].getVert().hasDuplicate(tempMove))
     								{
     									// This index could possibly fit in another spot
     									// of the quad therefor we need to iterate to next 
     									// possible index. This move won't work.
     									onlyIndex = false;
     								}
     							}
     						}
     						// all tiles have been checked if onlyIndex is still true
     						// tempMove is the only move for that location. 
     						if(onlyIndex)
     						{
     							System.out.println(tempMove);
     							return tempMove;
     						}
     						// otherwise go back to k and increment
     					}
     				}
     			}
     		}
     	}
         System.err.println("No moves found.");
         System.exit(1);
         return new Move(0,0,1);
     }
 }
