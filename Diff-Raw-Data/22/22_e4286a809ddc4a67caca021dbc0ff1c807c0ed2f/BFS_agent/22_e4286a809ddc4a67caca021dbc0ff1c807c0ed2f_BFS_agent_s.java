 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Stack;
 
 
 public class BFS_agent implements Agent{
 	Position home;
 	ArrayList<Position> walls = new ArrayList<Position>();
 	int roomHeight;
 	int roomWidth;
 	Stack<String> path = new Stack<String>();
 
 	public void init(Collection<String> percepts) {
 		
 		State startState = new State();
 		
 		//Gather information from percepts
 		for (String percept:percepts) 
 		{
 			String[] args = percept.replaceFirst("^\\(", "").split("[() ]+");
 				
 			for(String arg : args)
 			{
 				System.out.print(arg);
 				System.out.print(" - ");
 			}
 			System.out.print("\n");
 			
 			if(args[0].equals("HOME"))
 			{
 				home = new Position(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
 				startState.position = home; 
 			}
 			else if(args[0].equals("ORIENTATION"))
 			{		
				startState.direction = Direction.valueOf(args[1]);
 			}
 			else if(args[0].equals("SIZE")){
 				roomWidth = Integer.parseInt(args[1]);
 				roomHeight = Integer.parseInt(args[2]);
 			}
 			else if(args[0].equals("AT")){
 				Position p = new Position(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
 				if(args[1].equals("DIRT"))
 					startState.dirt.add(p);
 				else if(args[1].equals("OBSTACLE"))
 					walls.add(p);
 			}
 		}
 		
 		
 		//Search
 		Node goalNode = search(startState);
 		
 		//If the goal is not null we found a path
 		if(goalNode != null)
 		{
 			System.out.print("Path found!");
 			//The last move will always be 'TURN_OFF' so just add it here
 			path.push("TURN_OFF");
 			
 			//Traverse the search tree back to the root adding each move to the path
 			for(Node n = goalNode; n != null; n = n.parent){
 				path.push(n.move);
 			}
 		}
 		else
			System.out.print("No path found :(");
 		
 	}
 
 	public String nextAction(Collection<String> percepts) 
 	{
 		if(!path.isEmpty()){
 			String move = path.pop();
 			System.out.print(move);
 			return move;
 		}
 		else
 			System.out.print("No stuff to do");
 		
 		return "";
 	}
 	
 	private Node search(State startState)
 	{
 		Queue<Node> frontier = new LinkedList<Node>();
 		int nodeCounter = 0;
 		
 		//Create the root of the search tree
 		Node root = new Node(null, startState, "TURN_ON");
 		frontier.add(root);
 		
 		//Look while we don't find a goal and there are still nodes in the frontier
 		while(!frontier.isEmpty())
 		{
 			Node n = frontier.poll();
 			nodeCounter ++;
 			//Is this the goal we want
 			if(State.isGoal(n.state, home))
 			{
 				System.out.print("Nodes looked at: ");
 				System.out.print(nodeCounter);
 				System.out.print("\n");
 				return n;
 			}
 			
 			//If its not the goal, expand the node and keep looking
 			for(String move : GenerateLegalMoves(n.state))
 			{
 				//Get generate the next state and add the node
				frontier.add(new Node(n, n.state.getNext(move), move));
 			}
 		}
 		
 		//No goal was found
 		System.out.print("Nodes looked at: ");
 		System.out.print(nodeCounter);
 		System.out.print("\n");
 		return null;
 		
 	}
 	
 	private ArrayList<String> GenerateLegalMoves(State state)
 	{
 		ArrayList<String> moves = new ArrayList<String>();
 		
 		if(state.dirt.contains(state.position)){
 			//If we found dirt the only thing to do is to suck
 			moves.add("SUCK");
 			return moves;
 		}
 
		//Maybe check if the turn is useful
 		moves.add("TURN_LEFT");
 		moves.add("TURN_RIGHT");
 		
 		//Check if moving forward hits a wall
 		Position afterMove = new Position(state.position.x, state.position.y);
 		switch (state.direction){
 			case NORTH:
 				afterMove.y += 1;
 				break;
 			case EAST:
 				afterMove.x += 1;
 				break;
 			case SOUTH:
 				afterMove.y -= 1;
 				break;
 			case WEST:
 				afterMove.x -= 1;
 				break;
 		}
 		
 		if(afterMove.x > 0 && afterMove.x < roomHeight &&
 			afterMove.y > 0 && afterMove.y < roomWidth && 
 			!walls.contains(afterMove)){
 			moves.add("GO");
 		}
 	
 		return moves;
 	}
 
 }
