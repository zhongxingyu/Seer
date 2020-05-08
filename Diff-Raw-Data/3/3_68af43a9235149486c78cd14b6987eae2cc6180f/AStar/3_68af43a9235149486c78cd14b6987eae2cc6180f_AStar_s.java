 import java.awt.Point;
 import java.util.PriorityQueue;
 import java.util.Comparator;
 import java.util.ArrayList;
 
 public class AStar {
 	public static Node[] algorithm (Grid g, Node start, Node goal)
 	{
 		Comparator<Node> comparator = new NodeComparator();
 		PriorityQueue<Node> closedSet = new PriorityQueue<Node>(11, comparator);
 		PriorityQueue<Node> openSet = new PriorityQueue<Node>(11, comparator);
 		openSet.add(start);
 		Node current = null;
 		ArrayList<Node> list = new ArrayList<Node>(); // came_from := the empty map
 
 		start.setGScore(0);
 		start.setFScore(hScore(start, goal));
 		list.add(current);
 
 		while(openSet.size() != 0)
 		{
 			current = openSet.peek();
 			
 			if (current.equals(goal))
 				return (Node[]) list.toArray();
 
 			openSet.remove(current);
 			closedSet.add(current);
 			
 			for (Node neighbor: current.getConnections())
 			{
 				int tentativeGScore = current.getGScore() + g.getEdgeLength(current,neighbor);
 
 				if (closedSet.contains(neighbor) && tentativeGScore >= neighbor.getGScore())
 					continue;
 
 				if (!openSet.contains(neighbor) || tentativeGScore < neighbor.getGScore())
 				{
 					list.add(neighbor);
 					neighbor.setGScore(tentativeGScore);
 					neighbor.setFScore(tentativeGScore + hScore(neighbor, goal));
 
 					if(!openSet.contains(neighbor))
 						openSet.add(neighbor);
 				}
 
 			}
 			return null;
 		}
 		return null;
 	}
 
 	// calculates the h score based on the diagonal shortcut heuristic
 	private static int hScore (Node neighbor, Node goal)
 	{
 		int xDiff = (int) Math.abs(neighbor.getPosition().getX() - goal.getPosition().getX());
 		int yDiff = (int) Math.abs(neighbor.getPosition().getY() - goal.getPosition().getY());
 		
 		if (xDiff > yDiff)
 			return 14 * yDiff + 10 * (xDiff - yDiff);
 		else
 			return 14 * xDiff + 10 * (yDiff - xDiff);
 	}
 
 	
 }
