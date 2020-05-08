 package projectrts.model.core;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * A* pathfinding algorithm.
  * @author Bjorn Persson Mattsson
  *
  */
 public class AStar {
 
 	private static World world;
 	/**
 	 * Initializes the A* class
 	 * @param world Game world
 	 */
 	public static void initialize(World world)
 	{
 		AStar.world = world;
 	}
 	
 	/**
 	 * Creates a new A* instance. AStar must have been initialized,
 	 * otherwise an IllegalStateException will be thrown.
 	 */
 	public AStar()
 	{
 		if (world == null)
 		{
 			throw new IllegalStateException("You must initialize this class before you can use it");
 		}
 	}
 	
 	/**
 	 * Calculates a path using the A* algorithm.
 	 * @param startPos Start position.
 	 * @param targetPos End position.
 	 * @return An AStarPath from startPos to targetPos.
 	 */
 	public AStarPath calculatePath(Position startPos, Position targetPos)
 	{
 		AStarNode startNode = new AStarNode(world.getNodeAt(startPos));
 		AStarNode endNode = new AStarNode(world.getNodeAt(targetPos));
 		List<AStarNode> openList = new ArrayList<AStarNode>();
 		List<AStarNode> closedList = new ArrayList<AStarNode>();
 		
 		// A* algorithm starts here
 		openList.add(startNode);
 		while (openList.size() > 0) // while open list is not empty
 		{
 			// current node  = node from the open list with the lowest cost
 			Collections.sort(openList);
 			AStarNode currentNode = openList.get(0);
 			
 			if (currentNode.sameNodeAs(endNode))
 			{
 				// path complete
 				AStarPath path = new AStarPath();
				path.addNodeToPath(currentNode);
				AStarNode nextNode = currentNode.getParent();
 				
 				while (!nextNode.sameNodeAs(startNode))
 				{
 					path.addNodeToPath(nextNode);
 					nextNode = nextNode.getParent();
 				}
 				
 				return path;
 			}
 			else
 			{
				// mode current node to the closed list
 				openList.remove(currentNode);
 				closedList.add(currentNode);
 				
 				// examine each node adjacent to the current node
 				List<AStarNode> adjacentNodes = currentNode.getNeighbours();
 				for (AStarNode node : adjacentNodes)
 				{
 					if (!openList.contains(node)) // if not on open list
 					{
 						if (!closedList.contains(node)) // and not on closed list
 						{
 							if (!node.isObstacle()) // and not an obstacle
 							{
 								// move to open list and calculate cost
 								openList.add(node);
 								node.calculateCost(currentNode, endNode);
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		// path not found
 		return new AStarPath();
 	}
 }
