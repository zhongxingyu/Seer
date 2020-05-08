 import java.util.PriorityQueue;
 import java.util.Comparator;
 import java.util.ArrayList;
 
 public class AStar {
	public Node[] algorithm (Grid g, Node start, Node goal)
 	{
 		Comparator<Node> comparator = new NodeComparator();
 		PriorityQueue<Node> closedSet = new PriorityQueue<Node>(11, comparator);
 		PriorityQueue<Node> openSet = new PriorityQueue<Node>(11, comparator);
 		openSet.add(start);
 		Node current = null;
 		
 		start.setGScore(0);
 		start.setFScore(hScore(start, goal));
 
 		while(openSet.size() != 0)
 		{
 			current = openSet.peek();
 			
 			if (current.equals(goal)){
 				return reconstructPath(start, goal);
 			}
 
 			openSet.remove(current);
 			closedSet.add(current);
 			
 			// checks each of the current Node's neighbors
 			for (Node neighbor: current.getConnections())
 			{
 				// special case if there is no connection
 				if (neighbor == null)
 					continue;
 				
 				// calculate and compare GScores
 				int tentativeGScore = current.getGScore() + g.getEdgeLength(current,neighbor);
 
 				if (closedSet.contains(neighbor) && tentativeGScore >= neighbor.getGScore())
 					continue;
 
 				if (!openSet.contains(neighbor) || tentativeGScore < neighbor.getGScore())
 				{
 					neighbor.setParent(current);
 					neighbor.setGScore(tentativeGScore);
 					neighbor.setFScore(tentativeGScore + hScore(neighbor, goal));
 
 					if(!openSet.contains(neighbor))
 						openSet.add(neighbor);
 				}
 
 			}
 		}
 		return null;
 	}
 
 	// calculates the h score based on the diagonal shortcut heuristic
 	protected static int hScore (Node neighbor, Node goal)
 	{
 		int xDiff = (int) Math.abs(neighbor.getPosition().getX() - goal.getPosition().getX());
 		int yDiff = (int) Math.abs(neighbor.getPosition().getY() - goal.getPosition().getY());
 		
 		if (xDiff > yDiff)
 			return 14 * yDiff + 10 * (xDiff - yDiff);
 		else
 			return 14 * xDiff + 10 * (yDiff - xDiff);
 	}
 
 	// traverses the list of Nodes and returns a path
 	private static Node[] reconstructPath(Node start, Node current)
 	{
 		ArrayList<Node> path = new ArrayList<Node> ();
 		Node n = current;
 		while(!n.equals(start)){
 			path.add(n);
 			n = n.getParent();
 		}
 		path.add(start);
 		Node[] result = path.toArray(new Node[path.size()]);
 		return result;
 	}
 }
