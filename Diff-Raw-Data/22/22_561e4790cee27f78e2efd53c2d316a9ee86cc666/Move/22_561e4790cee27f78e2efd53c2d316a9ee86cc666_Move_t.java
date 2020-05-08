 package team01;
 
 public class Move {
 	
 	public Move (Board b)
 	{
 		board = b;
		// previous deltas are infinity
		prevdx = Integer.MAX_VALUE;
		prevdy = Integer.MAX_VALUE;
 	}
 	
 	public boolean isValidMove(int x1, int y1, int x2, int y2)
 	{
 		// position (x2, y2) is out of range
 		if (! board.isValidPosition(x2, y2))
 		{
 			return false;
 		}
 		
 		// position is empty?
 		if (! board.isEmpty(x2, y2))
 		{
 			return false;
 		}
 		
 		// cannot move to position multiple times in a move
 		if (! isUniquePosition(x2, y2))
 		{
 			return false;
 		}
 		
 		// delta x and delta y
 		int dx = x2 - x1;
 		int dy = y2 - y1;
 		
 		// check delta range and direction
 		if (! isValidDelta(dx, dy))
 		{
 			return false;
 		}
 		
 		// handle diagonal moves
 		if (Math.abs(dx) + Math.abs(dy) == 2)
 		{
 			// current position must be able to move diagonally
 			if (! board.isDiagonalPosition(x1, y1))
 			{
 				return false;
 			}
 		}
 		
 		
		// TODO: this code should not have side-effects!
		
//		prevdx = dx;
//		prevdy = dy;
 		
 		return true;
 	}
 	
	// TODO: add position to uniquePosition set
	
 	private boolean isValidDelta(int dx, int dy)
 	{
 		// delta values in [-1, 1] ?
 		return -1 <= dx && dx <= 1
 			&& -1 <= dy && dy <= 1
			&& (dx != prevdx || dy != prevdy);
 	}
 	
 	private boolean isUniquePosition(int x, int y)
 	{
 		// TODO: is position in set?
 		return true;
 	}
 	
 	private Board board;
 	private int prevdx;
 	private int prevdy;
 }
