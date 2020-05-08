 import java.util.*;
 import java.lang.Math;
 
 public class Astar implements Search
 {
 
 	public Environment env;
 
 	public Astar(Environment lu_env) {
 
 		this.env = lu_env;
 	}
 
 	public Stack<String> search(State state)
 	{
 		PriorityQueue<Node> f = new PriorityQueue<Node>(20, Node.HeuristicCompare);
 
 		Node root = new Node(state, null, null);
 		root.cost = 0;
 
 	  	// If the initial state is goal we are done.
 		if(is_goal(root.state)) return new Stack<String>();
 
 		f.add(root);
 		int i = 0;
 		while(f.peek() != null)
 		{
 			i++;
 			Node n = f.poll();
 		System.out.println(n.fCost);
 			State s = n.state;
 			if(is_goal(n.state)){
 				return path(n);
 			}
 			
 
 			for (String m : s.get_legal_moves(env))
 			{
 				i++;
 				Node child = new Node(s.next_state(m), n, m);
 				evalCost(child, n.cost);
 				child.fCost = child.cost + heuristicEstimate(child);
 
 				f.add(child);
 			}
 		}
 	   // We should never get here.
 		System.out.println("Something has gone awry! The search returned no solution!");
 		System.out.println("Exiting.");
 		System.exit(1);
 		return new Stack<String>();
 	}
 
 	private boolean is_goal(State state)
 	{
 		if(state.dirts.isEmpty() && env.at_home(state.location) && !state.ON)
 			return true;
 		return false;
 	}
 
 	private Stack<String> path(Node goal)
 	{
 		Stack<String> strat = new Stack<String>();
 
 		strat.push(goal.move);
 		Node next_node = goal.parent;
 
 	   while(next_node.move != null) //While we are not asking root
 	   {
 	   	strat.push(next_node.move);
                 System.out.println("COOOOOST" + next_node.fCost + "  :  " + next_node.move);
 	   	next_node = next_node.parent;
 	   }
 
 	   return strat;
 	}
 
 	private void evalCost(Node m, int parentCost) {
 
 		switch(m.move) {
 
 			case "TURN_OFF":
 			if(env.at_home(m.state.location))
 				m.cost = 1 + (15 * m.state.dirts.size()) + parentCost;
 			else
 				m.cost = 100 + (15 * m.state.dirts.size()) + parentCost;
 			break;
 			case "SUCK":
 			if(!m.parent.state.dirts.contains(m.state.location)) {
 				m.cost = 5 + parentCost;
 				break;
 			}
 			default:
 			m.cost = 1 + parentCost;
 		}
 	}
 
 	private int heuristicEstimate(Node n)
 	{
 		State s = n.state;
 		// Create new list so we can remove from it.
 		List<Point2D> tempDirts = new ArrayList<Point2D>();
 		for(Point2D d : s.dirts) newDirts.add(new Point2D(d.x(), d.y()));
 	    //Find nearest dirt
 
 		int manhattan_total = 0;
 		Point2D nearestDirt = null;
 		Point2D current_location = s.location;
 
 		while(tempDirts.size() != 0)
 		{
			for(Point2D p: n.state.dirts)
 			{
 				if(nearestDirt == null)
 					nearestDirt = p;
 				else
 				{
 					if(manhattan(p, n.state.location) < manhattan(nearestDirt, n.state.location))
 					{
 						nearestDirt = p;
 					}
 				}
 			}
 			manhattan_total += manhattan(nearestDirt, current_location);
 			current_location = nearestDirt;
 			tempDirts.remove(nearestDirt);
 		}
 	        
    		//Calculate manhattan to home if no dirt left
 		if(nearestDirt == null)
 			return manhattan(env.home, n.state.location) /*+ turning_cost*/;
         
 		Boolean didWeSuck = newDirts.remove(newPoint);
 
 /*        int turning_cost = 0;
         if(nearestDirt.x() > n.state.location.x() && n.state.direction == 3)
             turning_cost++;
         else if(nearestDirt.x() < n.state.location.x() && n.state.direction == 1)
             turning_cost++;
         else if(nearestDirt.y() > n.state.location.y() && n.state.direction == 2)
             turning_cost++;
         else if(nearestDirt.y() < n.state.location.y() && n.state.direction == 0)
             turning_cost++;
 */
 
     //Calculate manhattan to dirt from position of n
 		return manhattan_total + n.state.dirts.size() /*+ turning_cost*/;
 	}
 
 	private int manhattan(Point2D p1, Point2D p2)
 	{
 		int xDist = Math.abs(p1.x() - p2.x());
 		int yDist = Math.abs(p1.y() - p2.y());
 
 		return xDist + yDist;
 
 
 	}
 
 	public static void main(String args[]) {
 
 		Environment env = new Environment();
 		List<Point2D> dirtlist = new ArrayList<Point2D>();
 		List<Point2D> obstaclelist = new ArrayList<Point2D>();
 
 
 		obstaclelist.add(new Point2D(1, 3));
 		obstaclelist.add(new Point2D(1, 1));
 		dirtlist.add(new Point2D(3, 3));
       	//dirtlist.add(new Point2D(3, 3));
 
 		State state = new State(false, new Point2D(0, 0), 3, dirtlist);
 
 		env.r = 4;
 		env.c = 4;
 		env.home = new Point2D(0, 0);
 		env.obstacles = obstaclelist;
 
 		Search searcher = new Astar(env);
 		Stack<String> moves = searcher.search(state);
 
 		while(!moves.isEmpty()) {
 			String s = moves.pop();
 			System.out.println(s);
 		}
 	}
 
 }
