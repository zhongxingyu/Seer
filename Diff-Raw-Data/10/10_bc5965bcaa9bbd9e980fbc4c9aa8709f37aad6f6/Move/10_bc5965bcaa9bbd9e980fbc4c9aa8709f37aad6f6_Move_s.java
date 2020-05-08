 package team01;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Move {
 
 	public Move (Board b)
 	{
 		board = b;
 		path = new ArrayList<Point>();
 	}
 	
 	public boolean capture(Point from, Point to, boolean approach)
 	{
 		Delta delta = Delta.getDelta(from, to);
 		int player = board.getPosition(from);
 		int enemy = player ^ 1;
 
 		// add move to path/history
 		addToPath(from);
 		
 		// move player piece to next position
 		board.setPosition(from, Board.EMPTY);
 		board.setPosition(to, player);
 		
 		Point tmp = from;
 		from = to;
 		
 		to = approach ? to.getApproach(delta) : tmp.getWithdraw(delta);
 		
 		// delete enemy game pieces
 		// TODO: delete enemy game pieces for a withdrawal (not displaying on board currently)
 		while (board.isValidPosition(to)
 			&& board.getPosition(to) == enemy)
 		{
 			board.setPosition(to, Board.EMPTY);
 			to = approach ? to.getApproach(delta) : to.getWithdraw(delta);
 		}
 		
 		// TODO: this type of check should have its own function
 		for (int dx = Delta.MIN_DELTA; dx <= Delta.MAX_DELTA; dx++)
 		{
 			for (int dy = Delta.MIN_DELTA; dy <= Delta.MAX_DELTA; dy++)
 			{
 				// additional captures available?
 				if (isValidMove(from, from.getApproach(dx, dy)))
 					return true;
 			}
 		}
 		
 		// no more captures available for current move
 		return false;
 	}
 	
 	// this code should not have side effects!
 	public boolean isValidMove(Point from, Point to)
 	{
 		// next position in board range?
 		if (! board.isValidPosition(to))
 		{
 			return false;
 		}
 		
 		// next position is empty?
 		if (! board.isEmpty(to))
 		{
 			return false;
 		}
 		
 		// next position already in path?
 		if (findInPath(to))
 		{
 			return false;
 		}
 		
 		// delta/direction of move is legal?
 		if (! isValidDirection(from, to))
 		{
 			return false;
 		}
 		
 		// move will capture other player's piece (approach or withdrawal)?
 		if (! (hasCapture(from, to, true)
 			|| hasCapture(from, to, false)))
 		{
 			return false;
 		}
 		
 		return true;
 	}
 	
	private boolean hasCapture(Point from, Point to, boolean approach)
 	{
 		Point capture;
 		Delta delta = Delta.getDelta(from, to);
 		
 		if (approach)
 		{
 			capture = to.getApproach(delta);
 		}
 		else
 		{
 			capture = from.getWithdraw(delta);
 		}
 		
 		if (! board.isValidPosition(capture))
 		{
 			return false;
 		}
 
 		int player = board.getPosition(from);
 		int enemy = board.getPosition(capture);
 		
 		// other position must have an enemy piece
 		if (enemy == Board.EMPTY
 			|| enemy == player)
 		{
 			return false;
 		}
 		
 		// move has a capture
 		return true;
 	}
 	
 	// check if point is in path
 	private boolean findInPath(Point next)
 	{
 		for (Point point : path)
 		{
 			if (point.equals(next))
 			{
 				// next point is in path
 				return true;
 			}
 		}
 		
 		// next point not in path
 		return false;
 	}
 	
 	// add position to path
 	private void addToPath(Point point)
 	{
 		path.add(point);
 	}
 
 	private boolean isValidDirection(Point from, Point to)
 	{
 		Delta delta = Delta.getDelta(from, to);
 		
 		// delta values in range [-1, 1] ?
 		if (! delta.isValid())
 		{
 			return false;
 		}
 		
 		int last = path.size() - 1;
 		
 		// next move has same direction as last move?
 		if (last >= 0)
 		{
 			Point lastmove = path.get (last);
 			Delta lastDelta = Delta.getDelta(lastmove, from);
 			
 			if (delta.equals(lastDelta))
 			{
 				// cannot move in same direction twice in a row
 				return false;
 			}
 		}
 		
 		// handle diagonal moves
 		if (delta.isDiagonal())
 		{
 			// current position can move diagonally?
 			if (! board.isDiagonalPosition(from))
 			{
 				return false;
 			}
 		}
 		
 		// move is valid
 		return true;
 	}
 	
 	private List<Point> path;
 	private Board board;
 }
