 import java.util.ArrayList;
 
 public class State {
 	
 	ArrayList<Position> dirt;	//Position of all dirt left in the room
 	Position position;			//Position of the robot
 	Direction direction;		//Direction the robot is facing
 	
 	State()
 	{
 		dirt = new ArrayList<Position>();
 	}
 	
 	//Copy constructor
 	State(State s)
 	{
 		this.position = new Position(s.position.x, s.position.y);
 		this.direction = s.direction;
 		
 		for(Position p:s.dirt){
 			this.dirt.add(p);
 		}
 	}
 	
 	public State getNext(String move)
 	{
 		//Copy this state
 		State s = new State(this);
 		
 		//Make changes based on the move
 		if(move.equals("GO"))
 		{
 			if(s.direction == Direction.NORTH)
 				s.position.y ++;
 			else if(s.direction == Direction.EAST)
 				s.position.x ++;
 			else if(s.direction == Direction.SOUTH)
 				s.position.x --;
 			else if(s.direction == Direction.WEST)
 				s.position.x --;
 		}
 		else if(move.equals("TURN_LEFT"))
 		{
 			if(s.direction == Direction.NORTH)
 				s.direction = Direction.WEST;
 			else if(s.direction == Direction.EAST)
 				s.direction = Direction.NORTH;
 			else if(s.direction == Direction.SOUTH)
 				s.direction = Direction.EAST;
 			else if(s.direction == Direction.WEST)
 				s.direction = Direction.SOUTH;
 		}
 		else if(move.equals("TURN_RIGHT"))
 		{
 			if(s.direction == Direction.NORTH)
 				s.direction = Direction.EAST;
 			else if(s.direction == Direction.EAST)
 				s.direction = Direction.SOUTH;
 			else if(s.direction == Direction.SOUTH)
 				s.direction = Direction.WEST;
 			else if(s.direction == Direction.WEST)
 				s.direction = Direction.NORTH;
 		}
 		else if(move.equals("SUCK")){
 			dirt.remove(s.position);
 		}
 		
 		return s;
 	}
 	
 	
 	//Check if the given state is a goal we want to reach
 	static boolean isGoal(State state, Position homePosition)
 	{
 		if(state.dirt.size() == 0 && 
 			state.position.x == homePosition.x && 
 			state.position.y == homePosition.y)
 			return true;
 		
 		
 		return false;
 	}
 }
