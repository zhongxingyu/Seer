 package projectrts.model.pathfinding;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import projectrts.model.utils.Position;
 
 /**
  * A* pathfinding algorithm.
  * @author Bjorn Persson Mattsson
  *
  */
 public class AStar {
 
 	private static AStar instance;
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
 	 * @return Singleton A* instance.AStar must have been initialized,
 	 * otherwise an IllegalStateException will be thrown.
 	 */
 	public static AStar getInstance()
 	{
 		if (instance == null)
 		{
 			instance = new AStar();
 		}
 		return instance;
 	}
 	
 	private AStar()
 	{
 		if (world == null)
 		{
 			throw new IllegalStateException("You must initialize the AStar class before you can use it");
 		}
 	}
 	
 	/**
 	 * Returns the closest node that is not occupied.
 	 * @param startingPos The position that is the starting point for the calculation.
 	 * @param towards The position that this method "strives towards". Use null if you just want to
 	 * search around the starting point in no particular direction.
 	 * @param occupyingEntityID The entity ID of the unit which occupation is to be ignored.
 	 * Use 0 to only search for completely unoccupied nodes.
 	 * @return The AStarNode which is the closest unoccupied node according to the premises.
 	 * N.B.: If no such node is found, null will be returned.
 	 */
 	public AStarNode getClosestUnoccupiedNode(Position startingPos, Position towards, int occupyingEntityID)
 	{
 		// Use A* "backwards" to find the closest walkable node.
 		AStarNode targetNode = new AStarNode(world.getNodeAt(startingPos));
 		AStarNode towardsNode;
 		if (towards != null)
 		{
 			towardsNode = new AStarNode(world.getNodeAt(towards));
 		}
 		else
 		{
 			towardsNode = null;
 		}
 		List<AStarNode> closedList = new ArrayList<AStarNode>();
 		List<AStarNode> openList = new ArrayList<AStarNode>();
 		
 		openList.add(targetNode);
 		while (openList.size() > 0)
 		{
 			Collections.sort(openList);
 			AStarNode currentNode = openList.get(0);
 			
 			if (!currentNode.isObstacle(occupyingEntityID))
 			{
 				return currentNode;
 			}
 			openList.remove(currentNode);
 			closedList.add(currentNode);
 			
 			List<AStarNode> adjacentNodes = currentNode.getNeighbours();
 			for (AStarNode node : adjacentNodes)
 			{
 				if (!openList.contains(node) && !closedList.contains(node))
 				{
 					openList.add(node);
 					node.calculateCostFromStart(currentNode, false);
 					if (towardsNode != null)
 					{
 						node.calculateHeuristic(towardsNode);
 					}
 				}
 			}
 		}
 		
 		// Nothing could be found
 		return null;
 	}
 	
 	/**
 	 * Calculates a path using the A* algorithm.
 	 * @param startPos Start position.
 	 * @param targetPos End position.
 	 * @param occupyingEntityID ID of occupying entity.
 	 * @return An AStarPath from startPos to targetPos.
 	 */
 	public AStarPath calculatePath(Position startPos, Position targetPos, int occupyingEntityID)
 	{
 		// TODO Plankton: Add support for different heuristic priorities
 		// TODO Plankton: Take entity size into account when calculating path
		// Plankton: Use threads or something to not "freeze" the game when calculating?
 		// TODO Plankton: FIX DEBUG FOR NODES FFS!!!
 		AStarNode startNode = new AStarNode(world.getNodeAt(startPos));
 		AStarNode endNode = new AStarNode(world.getNodeAt(targetPos));
 		List<AStarNode> openList = new ArrayList<AStarNode>();
 		List<AStarNode> closedList = new ArrayList<AStarNode>();
 		
 		if (endNode.isObstacle(occupyingEntityID))
 		{
 			// Use A* "backwards" from the end node to find the closest walkable node.
 			// Probably not the best way of dealing with it, but it will do for now.
 			endNode = getClosestUnoccupiedNode(targetPos, startPos, occupyingEntityID);
 		}
 		
 		// A* algorithm starts here
 		openList.add(startNode);
 		while (openList.size() > 0) // while open list is not empty
 		{
 			// current node  = node from the open list with the lowest cost
 			Collections.sort(openList);
 			AStarNode currentNode = openList.get(0);
 			
 			if (currentNode.equals(endNode))
 			{
 				return generatePath(startNode, currentNode);
 			}
 			else
 			{
 				// http://www.policyalmanac.org/games/aStarTutorial.htm
 				
 				// move current node to the closed list
 				openList.remove(0);
 				closedList.add(currentNode);
 				
 				// examine each node adjacent to the current node
 				List<AStarNode> adjacentNodes = currentNode.getNeighbours();
 				for (AStarNode node : adjacentNodes)
 				{
					if (!node.isObstacle(occupyingEntityID) && !closedList.contains(node))
					{ // if not an obstacle and not on closed list
						if (openList.contains(node)) // if on open list, check to see if new path is better
						{
							node.calculateCostFromStart(currentNode, true);
						}
						else  // if not on open list
 						{
							// move to open list and calculate cost
							openList.add(node);
							node.calculateCostFromStart(currentNode, false);
							node.calculateHeuristic(endNode);
 						}
 					}
 				}
 			}
 		}
 		
 		// path not found, return path to the closest node instead.
 		
 		Collections.sort(closedList, AStarNode.getHeuristicComparator());
 		return generatePath(startNode, closedList.get(1));
 		// the second element in closedList since the first one is the start node
 	}
 	
 	private AStarPath generatePath(AStarNode startNode, AStarNode endNode)
 	{
 		AStarPath path = new AStarPath();
 		AStarNode nextNode = endNode;
 		
 		while (!nextNode.equals(startNode))
 		{
 			path.addNodeToPath(nextNode);
 			nextNode = nextNode.getParent();
 		}
 		
 		return path;
 	}
 }
