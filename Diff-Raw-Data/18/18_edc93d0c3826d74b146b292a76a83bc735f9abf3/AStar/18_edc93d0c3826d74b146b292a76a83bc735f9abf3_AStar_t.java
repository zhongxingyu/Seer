 import java.awt.Point;
 import java.util.PriorityQueue;
 import java.util.Comparator;
 import java.util.ArrayList;
 
 public class AStar {
 	public Node[] algorithm (Grid g, Point start, Point goal)
 	{
 		Comparator<Node> comparator = new NodeComparator();
 		PriorityQueue closedSet = new PriorityQueue<Node>(11, comparator);
 		PriorityQueue openSet = new PriorityQueue<Node>(11, comparator);
 		openSet.add(start);
 		Node current = null;
		ArrayList<Node> list = new ArrayList<Node>(); // came_from := the empty map
 
		start.setGScore(0);
		start.setFScore(hScore(start, goal));
 		
 		while(openSet.size() != 0)
 		{
 			current = openSet.peek();
 			
 			if (current.equals(goal))
 				return list; //reconstruct_path(came_from, goal)
 
 			openSet.remove(current);
 			closedSet.add(current);
 			
 			for (current.getConnections() : neighbor)
 			{
 				tentativeGScore = gScore(current) + g.getEdgeLength(current,neighbor);
 
 				if (closedSet.contains(neighbor) && tentativeGScore >= gScore(neighbor))
 					continue;
 
 				if (!openSet.contains(neighbor) || tentativeGScore < gScore(neighbor))
 				{
 					//came_from[neighbor] := current
 					neighbor.setGScore(tentativeGScore);
 					neighbor.setFScore(tentativeGScore + hScore(neighbor, goal));
 
 					if(!openSet.contains(neighbor))
 						openSet.add(neighbor);
 				}
 
 			}
 
 			return null;
 
 		}
 		/*
 
  function reconstruct_path(came_from, current_node)
      if current_node in came_from
          p := reconstruct_path(came_from, came_from[current_node])
          return (p + current_node)
      else
          return current_node
 
 		 * 
 		 */
 	}
 	
 	// calculates the h score based on the diagonal shortcut heuristic
 	public int hScore (Node neighbor, Node goal)
 	{
 		int xDiff = Math.abs(neighbor.getPosition().getX() - goal.getPosition().getX());
 		int yDiff = Math.abs(neighbor.getPOsition().getY() - goal.getPosition().getY());
 		
 		if (xDiff > yDiff)
 			return 14 * yDiff + 10 * (xDiff - yDiff)
 		else
 			return 14 * xDiff + 10 * (yDiff - xDiff)
 	}
 
 	
 }
