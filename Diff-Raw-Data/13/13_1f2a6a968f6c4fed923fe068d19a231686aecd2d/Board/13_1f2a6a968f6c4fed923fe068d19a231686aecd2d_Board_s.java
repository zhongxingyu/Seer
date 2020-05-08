 package org.kullgren.robot;
 
 import java.util.ArrayList;
 
 public class Board {
 
 	private int Rows;
 	private int Columns;
 	private int numPositions;
 	private int myRobotPosition;
	private Direction myRobotDirection;
 	private ArrayList<BoardPosition> board;
 
 	public Board(int rows, int columns)
 	{
 		Rows = rows;
 		Columns = columns;
 		numPositions = rows*columns;
 		
 		board = new ArrayList<BoardPosition>();
 		for (int i=0; i<Rows*Columns; i++) {
 		    BoardPosition pos = new BoardPosition();
 		    board.add(pos);
 		}
 	}
 
 	public BoardPosition get(int i, int j) {
 	    return get(j*Columns+i);
 	}
 
 	public BoardPosition get(int position)
 	{
 		return board.get(position);
 	}
 
 	public int addRobot(Robot robot, int position) {
 	    myRobotPosition = position;
	    myRobotDirection = Direction.Up;
 	    board.get(position).addRobot(robot);
 	    return myRobotPosition;
 	}
 
 	public int addRobot(Robot robot, int i, int j) {
 	    return addRobot(robot, j*Columns+i);
 	}
 
 	public Robot getRobot() {
 	    return board.get(myRobotPosition).getRobot();
 	}
 
 	public int getRobotPosition()
 	{
 	    return myRobotPosition;
 	}
 
 	public int moveRobot(Direction direction) {
 	    int offset = 0;
 
 	    switch (direction) {
 	    case Up:
 	        offset = -Columns;
 	        break;
 	    case Down:
 	        offset = Columns;
 	        break;
 	    case Left:
 	        if (((myRobotPosition+1) % Columns) != 1) {
 	            offset = -1;
 	        }
 	        break;
 	    case Right:
 	        if (((myRobotPosition+1) % Columns) != 0) {
 	            offset = 1;
 	        }
 	        break;
 	    default:
 	        offset = 0;
 	    }
 
 	    int newPosition = myRobotPosition +offset;
 	    if (newPosition < numPositions && newPosition >= 0) {
 	        addRobot(board.get(myRobotPosition).deleteRobot(), newPosition);
 	    }
 	    return myRobotPosition;
 	}
 	
 	public int moveRobot(Movement cardType) {
 
 	    switch (cardType) {
 	    case Forward:
 	        moveRobot(myRobotDirection);
 	        break;
 	    case TurnLeft:
 	        turnRobotLeft(1);
 	        break;
 	    case TurnRight:
 	        turnRobotRight(1);
 	        break;
 	    case Wait:
 	        break;
 	    }
 	    
 	    return myRobotPosition;
 	}
 	
     private void turnRobotLeft(int i) {
         switch (myRobotDirection) {
         case Up:
             myRobotDirection = Direction.Left;
             break;
         case Right:
             myRobotDirection = Direction.Up;
             break;
         case Down:
             myRobotDirection = Direction.Right;
             break;
         case Left:
             myRobotDirection = Direction.Down;
             break;
         }
     }
 
     private void turnRobotRight(int i) {
         switch (myRobotDirection) {
         case Up:
             myRobotDirection = Direction.Right;
             break;
         case Right:
             myRobotDirection = Direction.Down;
             break;
         case Down:
             myRobotDirection = Direction.Left;
             break;
         case Left:
             myRobotDirection = Direction.Up;
             break;
         }
     }
 
 	private void createHole(int x, int y) {
 		int position = y*Columns+x;
 		board.get(position).createHole();		
 	}
 	
 	private void setBlockedDirection(int x, int y, Direction direction) {
 		int position = y*Columns+x;
 		setBlockedDirection(position, direction);
 	}
 	
 	private void setBlockedDirection(int position, Direction direction)
 	{
 		board.get(position).setBlockedDirection(direction);
 		if (direction.equals(Direction.Left) && position > 0) {
 			board.get(position-1).setBlockedDirection(Direction.Right);
 		}
 		if (direction.equals(Direction.Right) && position < numPositions-1) {
 			board.get(position+1).setBlockedDirection(Direction.Left);
 		}
 		if (direction.equals(Direction.Up) && position > Columns-1) {
 			board.get(position-Columns).setBlockedDirection(Direction.Down);
 		}
 		if (direction.equals(Direction.Down) && position < numPositions-Columns) {
 			board.get(position+Columns).setBlockedDirection(Direction.Up);
 		}
 	}
 	
 	public void createRandomBoard() {
 		createHole(2, 2);
 		createHole(17, 2);
 		createHole(2, 17);
 		createHole(17, 17);
 		createHole(7, 5);
 		createHole(12, 5);
 		createHole(7, 14);
 		createHole(12, 14);
 		
 		setBlockedDirection(5, 2, Direction.Down);
 		setBlockedDirection(6, 2, Direction.Down);
 		setBlockedDirection(6, 2, Direction.Right);
 		setBlockedDirection(13, 2, Direction.Left);
 		setBlockedDirection(13, 2, Direction.Down);		
 		setBlockedDirection(14, 2, Direction.Down);
 		setBlockedDirection(6, 3, Direction.Right);
 		setBlockedDirection(13, 3, Direction.Left);
 		setBlockedDirection(9, 4, Direction.Right);
 		setBlockedDirection(9, 5, Direction.Right);
 		setBlockedDirection(3, 6, Direction.Right);
 		setBlockedDirection(16, 6, Direction.Left);
 		setBlockedDirection(2, 7, Direction.Down);
 		setBlockedDirection(3, 7, Direction.Down);
 		setBlockedDirection(3, 7, Direction.Right);
 		setBlockedDirection(7, 7, Direction.Left);
 		setBlockedDirection(12, 7, Direction.Right);
 		setBlockedDirection(17, 7, Direction.Down);
 		setBlockedDirection(16, 7, Direction.Down);
 		setBlockedDirection(16, 7, Direction.Left);
 		setBlockedDirection(7, 8, Direction.Left);
 		setBlockedDirection(7, 8, Direction.Down);
 		setBlockedDirection(8, 8, Direction.Down);
 		setBlockedDirection(11, 8, Direction.Down);
 		setBlockedDirection(12, 8, Direction.Right);
 		setBlockedDirection(12, 8, Direction.Down);
 		
 		setBlockedDirection(5, 17, Direction.Up);
 		setBlockedDirection(6, 17, Direction.Up);
 		setBlockedDirection(6, 17, Direction.Right);
 		setBlockedDirection(13, 17, Direction.Left);
 		setBlockedDirection(13, 17, Direction.Up);		
 		setBlockedDirection(14, 17, Direction.Up);
 		setBlockedDirection(6, 16, Direction.Right);
 		setBlockedDirection(13, 16, Direction.Left);
 		setBlockedDirection(9, 15, Direction.Right);
 		setBlockedDirection(9, 14, Direction.Right);
 		setBlockedDirection(3, 13, Direction.Right);
 		setBlockedDirection(16, 13, Direction.Left);
 		setBlockedDirection(2, 12, Direction.Up);
 		setBlockedDirection(3, 12, Direction.Up);
 		setBlockedDirection(3, 12, Direction.Right);
 		setBlockedDirection(7, 12, Direction.Left);
 		setBlockedDirection(12, 12, Direction.Right);
 		setBlockedDirection(17, 12, Direction.Up);
 		setBlockedDirection(16, 12, Direction.Up);
 		setBlockedDirection(16, 12, Direction.Left);
 		setBlockedDirection(7, 11, Direction.Left);
 		setBlockedDirection(7, 11, Direction.Up);
 		setBlockedDirection(8, 11, Direction.Up);
 		setBlockedDirection(11, 11, Direction.Up);
 		setBlockedDirection(12, 11, Direction.Right);
 		setBlockedDirection(12, 11, Direction.Up);
 		
 	}
 }
