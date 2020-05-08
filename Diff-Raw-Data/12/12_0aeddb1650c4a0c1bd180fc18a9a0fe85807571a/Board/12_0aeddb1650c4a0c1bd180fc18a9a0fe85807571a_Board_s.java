 package twelve.team;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Scanner;
 
 import twelve.team.Piece.Team;
 
 public class Board
 {
 	private Piece[][] board;
 	private int rows = 5;
 	private int cols = 9;
 	private ArrayList<Point> WhiteAttackers = new ArrayList<Point>();
 	private ArrayList<Point> BlackAttackers = new ArrayList<Point>();
 	private Piece lastMovedPiece = null;
 	private Point MultipleCaptureStart = null;
 	
 	
 	public enum Direction {up,down,left,right,topleft,topright,botleft,botright,none};
 	public enum moveType {ADVANCE,RETREAT,PAIKA,SACRIFICE,NONE};
 	
 	/*
 		CONSTRUCTORS
 	*/
 	
 	public Board()
 	{
 		board = new Piece[rows][cols];
 		resetBoard();
 	}
 
 	public Board(int r, int c)
 	{
 		rows = r;
 		cols = c;
 		board = new Piece[rows][cols];
 		resetBoard();
 	}
 
 	
 	//resets the board to starting positions
 	public void resetBoard()
 	{
 		MultipleCaptureStart = null;
 		lastMovedPiece = null;
 		WhiteAttackers.clear();
 		BlackAttackers.clear();
 		for(int i = 0; i < rows/2; i++) //color bottom whites
 		{
 			for(int j = 0; j < cols; j++)
 			{
 				board[i][j] = new Piece(Piece.Team.WHITE);
 			}
 		}
 		for(int i = (rows/2)+1; i < rows; i++)//color top black
 		{
 			for(int j = 0; j < cols; j++)
 			{
 				board[i][j] = new Piece(Piece.Team.BLACK);
 			}
 		}
 		boolean color = true;
 		for(int i = 0; i < cols; i++)
 		{
 			if(i == cols/2)
 			{
 				board[rows/2][i] = null;
 				continue;
 			}
 			if(color)
 				board[rows/2][i] = new Piece(Piece.Team.BLACK);
 			else
 				board[rows/2][i] = new Piece(Piece.Team.WHITE);
 			color = !color;
 		}
 		updateAttackers();
 	}
 	
 	
 	public boolean isPaika(Point start, Point end)
 	{
 		try {
 			Point target = possibleCapture(start,end,moveType.NONE);
 			if(target == null)
 				return true;
 			else
 				return false;
 		} catch (MoveException e) {
 			return false;
 		}
 	}
 	//prints Board in Console (testing purposes)
 	public void printBoard()
 	{
 		for(int i = rows-1; i >= 0; i--)
 		{
 			for(int j = 0; j < cols; j++)
 			{
 				char t = 0;
 				if(board[i][j] == null)
 				{
 					t = ' ';
 				}
 				else
 				{
 					switch(board[i][j].getTeam())
 					{
 					case WHITE:
 						t = 'w';
 						break;
 					case BLACK:
 						t = 'b';
 						break;
 					default:
 						t = ' ';
 						break;
 					}
 				}
 				System.out.print("["+t+"]");
 			}
 			System.out.println();
 		}
 	}
 	
 	
 	public Piece[][] getBoard(){
 		return board;
 	}
 	
 	public void nextTurn(){
 		MultipleCaptureStart = null;
 		lastMovedPiece = null;
 		for(int i=0;i<board.length;i++){
 			for(int j=0;j<board[i].length;j++){
 				if(board[i][j] != null && board[i][j].sacrificed && board[i][j].nextTurn){
 					board[i][j] = null;
 				} else if(board[i][j] != null && board[i][j].sacrificed){
 					board[i][j].nextTurn = true;
 				}
 			}
 		}
 	}
 	
 	
 	//checks the whole board and fills ArrayLists of each team of pieces that can capture
 	//only one piece should be "moving"
 	//if a piece is "moving" it means it was the last one that moved and it could move one more time
 	private void updateAttackers()
 	{
 		WhiteAttackers.clear();
 		BlackAttackers.clear();
 		Point p = new Point();
 		for(int row = 0; row < rows; row++)
 		{
 			for(int col = 0; col < cols; col++)
 			{
 				
 				p.x = col;
 				p.y = row;
 				if(board[p.y][p.x]!= null )
 					if(canCapture(p))
 					{
 						Point attacker = new Point(p);
 						switch(getPiece(attacker).getTeam())
 						{
 						case WHITE:
 							WhiteAttackers.add(attacker);
 							break;
 						case BLACK:
 							BlackAttackers.add(attacker);
 							break;
 						}
 					}
 					
 			}
 		}
 	}
 	
 	//returns true if there are any attackers on a certain team
 	//attackers are pieces that can capture another piece
 	private boolean anyAttackers(Piece.Team team)
 	{
 		switch(team)
 		{
 		case WHITE:
 			if(WhiteAttackers.size() > 0)
 				return true;
 			else
 				return false;
 		case BLACK:
 			if(BlackAttackers.size() > 0)
 				return true;
 			else 
 				return false;
 		default:
 				//shouldn't happen
 				break;
 		}
 		return false;
 	}
 	
 	//returns a Direction from a given starting point and an ending point
 	private static Direction getDirection(Point start, Point end)
 	{
 		if( start.x - end.x > 0) //left
 		{
 			if (start.y - end.y < 0) //up
 				return Board.Direction.topleft;
 			else if (start.y - end.y > 0) //down
 				return Board.Direction.botleft;
 			else //same .y
 				return Board.Direction.left;
 		}
 		else if ( start.x - end.x < 0) //right
 		{
 			if (start.y - end.y < 0) //up
 				return Board.Direction.topright;
 			else if (start.y - end.y > 0) //down
 				return Board.Direction.botright;
 			else //same .y
 				return Board.Direction.right;
 		}
 		else // no x movement (only up or down)
 		{
 			if(start.y - end.y < 0)
 				return Board.Direction.up;
 			if(start.y - end.y > 0)
 				return Board.Direction.down;
 			else
 				return Board.Direction.none; //no movement
 		}
 	}
 	
 	//gets the opposite direction from a given direction
 	private Direction getOppositeDirection(Direction d)
 	{
 		switch(d)
 		{
 		case up:
 			return Direction.down;
 		case down:
 			return Direction.up;
 		case left:
 			return Direction.right;
 		case right:
 			return Direction.left;
 		case topleft:
 			return Direction.botright;
 		case topright:
 			return Direction.botleft;
 		case botleft:
 			return Direction.topright;
 		case botright:
 			return Direction.topleft;
 		default:
 			return Direction.none;
 		}
 	}
 	
 	
 	public Piece.Team isGameOver()
 	{
 		int blackPieces = 0;
 		int whitePieces = 0;
 		Point p = new Point();
 		for(int row = 0; row < rows; row++)
 		{
 			for(int col = 0; col < cols; col++)
 			{
 				p.x = col;
 				p.y = row;
 				if (getPiece(p)== null) continue;
 				switch(getPiece(p).getTeam())
 				{
 				case WHITE:
 					whitePieces++;
 					break;
 				case BLACK:
 					blackPieces++;
 					break;
 				default:
 					break;
 				}
 			}
 		}
 		System.out.format("White: %d, Black: %d\n", whitePieces, blackPieces );
 		if(whitePieces == 0)
 			return Piece.Team.BLACK;
 		if(blackPieces == 0)
 			return Piece.Team.WHITE;
 		
 		return null; //no winner
 	}
 	//Gets the point with a given starting point and a direction (for lack of a better name)
 	//returns null if point is out of bounds
 	private Point getPoint(Point start, Direction d)
 	{
 		Point end = new Point();
 		switch(d)
 		{
 		case up:
 			end.y = start.y + 1; //up
 			end.x = start.x; //none
 			break;
 		case down:
 			end.y = start.y - 1; //down
 			end.x = start.x; //none
 			break;
 		case left:
 			end.y = start.y; //none
 			end.x = start.x - 1; //left
 			break;
 		case right:
 			end.y = start.y; //none
 			end.x = start.x + 1; //right
 			break;
 		case topleft:
 			end.y = start.y + 1; //up
 			end.x = start.x - 1; //left
 			break;
 		case botleft:
 			end.y = start.y - 1; //down
 			end.x = start.x - 1; //left
 			break;
 		case topright:
 			end.y = start.y + 1; //up
 			end.x = start.x + 1; //right
 			break;
 		case botright:
 			end.y = start.y - 1; //down
 			end.x = start.x + 1; //right
 			break;
 		case none:
 			end.y = start.y; //none
 			end.x = start.x; //none
 		}
 		if(end.y < 0 || end.y >= rows)
 			return null;
 		if(end.x < 0 || end.x >= cols)
 			return null;
 		//might want to check if the point contains negative values
 		return end;
 	}
 	
 	//checks if a move is valid
 	private boolean isValid(Point start, Point end)
 	{
 		if(start == null || end == null)
 			return false;
 		Direction direction  = getDirection(start,end);
 		
 		if(getPiece(start) == null) //cannot move an empty spot!
 			return false;
 		if(getPiece(end) != null) //you can only move a piece to an empty spot
 			return false;
 		if(direction == Direction.none) //no move was made
 			return true;
 		
 		//no need to check for borders since visual board should not let you move a piece outside the borders
 		
 		if(Math.abs(start.x-end.x) <= 1 && Math.abs(start.y-end.y) <= 1) //only moved max 1 position in any direction
 		{
 			if((start.y + start.x) % 2 == 0) //diagonals are possible
 			{	
 				//check for piece
 				return true;
 			}
 			else //only up, down, left, right
 			{
 				if(		direction == Direction.topleft  ||
 						direction == Direction.topright ||
 						direction == Direction.botright || 
 						direction == Direction.botleft )
 					return false;
 				else	
 					return true;
 			}
 		}
 		else
 			return false; //moved either x or y more than one position
 	}
 	
 	//return 0 for no capture, 
 	private boolean canCapture(Point start)
 	{
 		Piece piece = getPiece(start);
 				
 		Piece.Team opposite = piece.getOppositeTeam();
 		
 		int captureAhead = 0;
 		int captureBehind = 0;
 		
 		for(Direction d : Direction.values())
 		{
 			Point land = getPoint(start,d); //get point in all directions
 			if( land == start) //when direction is none
 				continue;
 			
 			if(isValid(start,land))// checks that the move was legal
 			{
 				
 				if(MultipleCaptureStart != null && land.equals(MultipleCaptureStart))
 					continue;
 				
 				Point target = getPoint(land,d);  //look for targets in the same direction you traveled
 				Point targetBehind = getPoint(start,getOppositeDirection(d)); //from your starting position get the target behind 
 				
 				if(target != null)
 					if(getPiece(target) != null) //make sure target is not null
 						if(getPiece(target).getTeam() == opposite) //check position ahead in the same direction you advanced for opposite team
 						{
 							++captureAhead;
 						}
 				if(targetBehind != null)
 					if(getPiece(targetBehind) != null) //make sure behind is not null
 						if(getPiece(targetBehind).getTeam() == opposite)
 						{
 							++captureBehind;
 						}
 			}
 		}
 		if (captureAhead+captureBehind > 0)
 			return true;
 		else 
 			return false;
 	}
 	
 	private ArrayList<Move> getTeamMoves(Team team) {
 		
 		
 		
 		ArrayList<Move> teamMoves = new ArrayList<Move>();
 		boolean captureMoves = false;
 		
 		for(int i=0;i<board.length;i++) {
 			for(int j=0;j<board[i].length;j++){
 				if(board[i][j] != null && board[i][j].getTeam() == team){
 					Point loc = new Point(j, i);
 					if(lastMovedPiece != null && getPiece(loc) != lastMovedPiece)
 						continue;
 					
 					
 					ArrayList<Move> moves = new ArrayList<Move>(); //non-capturing moves
 					ArrayList<Move> captures = new ArrayList<Move>(); //capturing moves
 					
 					for (Direction dir : Direction.values()) {
 						Point newPoint = getPoint(loc, dir);
 						if (newPoint != null && isValid(loc, newPoint)){
 							if(isPaika(loc, newPoint)) 
 								moves.add(new Move(loc, newPoint, moveType.PAIKA));
 							else if (isRetreating(loc, newPoint, moveType.RETREAT)) 
								captures.add(new Move(loc, newPoint, moveType.PAIKA));
 							else 
 								captures.add(new Move(loc, newPoint, moveType.ADVANCE));
 						}
 					}
 					
 					if(captures.size() > 0){
 						if(!captureMoves){
 							captureMoves = true;
 							teamMoves.clear();
 						}
 						teamMoves.addAll(captures);
 					} else {
 						if(!captureMoves)
 							teamMoves.addAll(moves);
 					}
 				}
 			}
 		}
 		
 		return teamMoves;
 	}
 	
 	public Move getRandomMove(Team team) {
 		ArrayList<Move> moves = getTeamMoves(team);
 		if(moves.size() < 1)
 			return null;
 		
 		Random rand = new Random(System.currentTimeMillis());
 		
 		return moves.get(rand.nextInt(moves.size()));
 	}
 	
 	private void deletePiece(Point p)
 	{
 		board[p.y][p.x] = null;
 	}
 	
 	private Piece getPiece(Point p)
 	{
 		return board[p.y][p.x];
 	}
 	
 	private Point possibleCapture(Point start, Point end, moveType type) throws MoveException
 	{
 		if(getPiece(start) == null)
 			return null;
 		Direction d = getDirection(start,end);
 		
 		Point behind = getPoint(start,getOppositeDirection(d));
 		Point target = getPoint(end,d);
 		Piece.Team opposite = getPiece(start).getOppositeTeam();
 		
 		//check that points target and behind are not null
 		if(type == moveType.NONE)
 		{
 			if(behind != null && target != null)
 				if(board[target.y][target.x] != null && board[behind.y][behind.x] != null) 
 					if(getPiece(target).getTeam() == opposite && getPiece(behind).getTeam() == opposite)
 					{
 						if(getPiece(target).sacrificed == false && getPiece(target).sacrificed == false)
 						{
 							MultipleCaptureStart = start;
 							throw new MoveException(target,behind); //throws the points of both pieces that can be taken
 						}
 					}
 		}
 		if(type == moveType.NONE || type == moveType.ADVANCE)
 		{
 			if(target != null)
 				if(board[target.y][target.x] != null)
 					if(getPiece(target).getTeam() == opposite && getPiece(target).sacrificed == false)
 					{
 						return target;
 					}
 		}
 		if(type == moveType.NONE || type == moveType.RETREAT)
 		{
 			if(behind != null)
 				if(getPiece(behind) != null)
 					if(getPiece(behind).getTeam() == opposite && getPiece(behind).sacrificed == false)
 					{
 						return behind;
 					}
 		}
 		return null;
 	}
 	
 	public boolean isRetreating(Point start, Point end, moveType type){
 		Direction d = getDirection(start, end);
 		Point target;
 		try{
 			target = possibleCapture(start, end, type);
 			if(target == null)
 				return false;
 			return d != getDirection(start, target);
 		} catch (Exception e){
 			return false;
 		}
 	}
 	
 	private boolean willAttack(Point start,Point end,moveType type)
 	{
 		Direction d = getDirection(start,end); //spot ahead
 		Direction b = getOppositeDirection(d); //spot behind
 		Direction attack;
 		Point target;
 		try {
 			target = possibleCapture(start,end,type);
 			//this case should never happen because you should check canCapture before calling this function
 			if(target == null)
 				return false; 
 			attack = getDirection(start,target);
 		} catch (MoveException e) {
 			Point forward = e.ahead;
 			Point backward = e.behind;
 			//if you move in the direction of your target or opposite direction of target behind
 			if(getDirection(start,forward) == d || getDirection(start,backward) == b)
 				return true;
 			else
 				return false;
 			//e.printStackTrace();
 		}
 		//if your move is the same direction as the attack direction
 		//or the opposite direction of your move is the attack direction
 		if(b == attack || d == attack)
 			return true;
 		else
 			return false;
 	}
 	
 	//returns true if another capture can be made by the same piece
 	public boolean move(Point start, Point end) throws Exception,MoveException
 	{
 		return move(start,end,moveType.NONE);
 	}
 	
 	public boolean move(Point start, Point end, moveType type) throws Exception,MoveException
 	{
 		//check if capture moves are available
 		if(type == moveType.SACRIFICE)
 		{
 			getPiece(start).sacrificed = true;
 			return false;
 		}
 		
 		if(isValid(start,end))
 		{	
 			
 			Piece p = getPiece(start);
 			Piece.Team opposite = getPiece(start).getOppositeTeam();
 			boolean attacked = false;
 			//a moving piece is a piece that moved and returned true because that piece can move again
 			//need to fix that if there is a moving piece in the board, then only that piece can move again
 			// capture something with it, 
 						//if(getLastMovingPiece() == null)//no moving piece
 			
 			if(lastMovedPiece != null)
 			{
 				if(p != lastMovedPiece)
 					throw new Exception("You can only move the piece that you had initially moved.");
 			}
 			
 			//cannot move back to your starting position once you had to chose a target
 			if(MultipleCaptureStart != null)
 				if(end.equals(MultipleCaptureStart))
 					throw new Exception("Invalid Move");
 			
 			
 			
 			
 			if(canCapture(start))//if you can capture something with this piece
 			{
 				if(!willAttack(start,end,type))//if you will not attack throw invalid move
 				{
 					throw new Exception("This piece can attack.");
 				}
 			}
 			else //you will not capture something with this piece
 			{
 				//check if this piece is not on it's second consecutive turn
 				if(p != lastMovedPiece) //check if any other pieces of same team can move
 				{
 					Piece.Team team = p.getTeam();
 					if(anyAttackers(team)) //will return true if the team has other pieces that can capture
 						throw new Exception("There is another piece that can attack."); //because another piece can attack
 				}
 			}		
 			
 			Point target = possibleCapture(start,end,type);
 			board[end.y][end.x] = p; //move the chosen piece to the space that it was moved
 			deletePiece(start);
 			
 			if(target != null)
 			{
 				Direction attack = getDirection(start,target);
 				while(target != null && getPiece(target).getTeam() == opposite) //error here
 				{
 					deletePiece(target);
 					attacked = true;
 					target = getPoint(target,attack);
 					if(target != null)
 						if(board[target.y][target.x] == null )
 							break;
 				}
 			}
 				
 			updateAttackers();
 			//if you can capture more pieces with the same original piece return true
 			if(canCapture(end) && attacked) //you can move again only if you captured something and you are able to capture again
 			{	
 				MultipleCaptureStart = null;
 				lastMovedPiece = p;
 				return true;
 			}
 			else
 			{
 				MultipleCaptureStart = null;
 				lastMovedPiece = null;
 				return false;
 			}
 		}
 		else
 			throw new Exception("Invalid Move");
 	}
 	
 	
 	public static void main(String args[])
 	{
 		Board board = new Board();
 		Scanner scanner = new Scanner(System.in);
 		
 		int startx;//column
 		int starty;//row
 		int endx;
 		int endy;
 		board.printBoard();
 		while(true)
 		{
 			System.out.println("Enter your move 'col' 'row' 'col' 'row' ");
 			
 			startx = scanner.nextInt();
 			starty = scanner.nextInt();
 			endx = scanner.nextInt();
 			endy = scanner.nextInt();
 			
 			Point start = new Point();
 			start.x = startx;
 			start.y = starty;
 			Point end = new Point();
 			end.x = endx;
 			end.y = endy;
 			try {
 				if(board.move(start,end,moveType.ADVANCE))
 				{
 					board.printBoard();
 					System.out.println("You can move Again");
 				}
 				else 
 				{
 					board.printBoard();
 					System.out.println("Other team's turn");
 				}
 			} catch (MoveException e) {
 				// TODO Auto-generated catch block
 				//e.printStackTrace();
 				System.out.println("Can capture two");
 				
 				break;
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				if(e.getMessage() == "Invalid Move")
 					System.out.println("Invalid Move");
 				else
 					e.printStackTrace();
 					board.printBoard();
 				continue;
 			}
 		}
 		
 		scanner.close();
 		
 	
 	}
 
 }
