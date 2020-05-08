 package team01;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Move {
 
 	private Board board;
 	private List<Point> path;
 	private Delta last;
 
 	public Move(Board board) {
 		super();
 		this.board = board;
 		this.path = new ArrayList<Point>();
 		this.last = new Delta(0, 0);
 	}
 
 	public Move(Move other)
 	{
 		this.board = other.board;
 		this.path = new ArrayList<Point>();
 		for (Point point : other.path)
 		{
 			path.add(point);
 		}
 		this.last = new Delta(other.last);
 	}
 	
 	public void capture(Point from, Point to, Delta delta, boolean isApproach) {
 		int player = board.getPoint(from);
 		int enemy = player ^ 1;
 		
 		// add source to path history
 		path.add(from);
 		last = delta;
 		
 		board.setPoint(from, Board.EMPTY);
 		board.setPoint(to, player);
 
 		Point temp = from;
 		from = to;
 		
 		if (isApproach)
 		{
 			to = to.getApproach(delta);
 		}
 		else
 		{
 			to = temp.getWithdraw(delta);
 		}
 		
 		// delete enemy pieces
 		while (board.isValidPoint(to)
 			&& board.getPoint(to) == enemy)
 		{
 			board.setPoint(to, Board.EMPTY);
 			
 			if (isApproach)
 			{
 				to = to.getApproach(delta);
 			}
 			else
 			{
 				to = to.getWithdraw(delta);
 			}
 		}
 	}
 
 	public boolean isValidMove(Point from, Point to, Delta delta) {
 		if (! board.isValidPoint(to)) {
 			return false;
 		}
 
 		// destination point empty?
 		if (board.getPoint(to) != Board.EMPTY) {
 			return false;
 		}
 
 		// direction same as previous move?
 		if (delta.equals(last)) {
 			return false;
 		}
 
 		// point already visited?
 		if (path.contains(to)) {
 			return false;
 		}
 
 		return isValidApproach(from, to, delta)
 			|| isValidWithdraw(from, to, delta);
 	}
 	
 	public boolean isValidApproach(Point from, Point to, Delta delta) {
 		return isValidCapture(from, to, delta, true);
 	}
 	
 	public boolean isValidWithdraw(Point from, Point to, Delta delta) {
 		return isValidCapture(from, to, delta, false);
 	}
 
 	public void resetBoard(Board board)
 	{
 		this.board = board;
 	}
 	
 	private boolean isValidCapture(Point from, Point to, Delta delta, boolean isApproach) {
 		Point capture;
 		
 		if (isApproach)
 		{
 			capture = to.getApproach(delta);
 		}
 		else
 		{
 			capture = from.getWithdraw(delta);
 		}
 		
 		if (! board.isValidPoint(capture))
 		{
 			return false;
 		}
 		
 		int player = board.getPoint(from);
 		int other = board.getPoint(capture);
 		
 		// other point has enemy piece;
 		return other == (player ^ 1);
 	}
 
 }
