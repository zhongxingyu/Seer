 package projectrts.model.abilities.pathfinding;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import projectrts.model.world.IWorld;
 import projectrts.model.world.Position;
 
 /**
  * A* pathfinding algorithm.
  * @author Bjorn Persson Mattsson
  *
  */
 public class AStar {
 	
 	private static IWorld world;
 	
 	private AStar() {} //Empty private constructor to disallow any AStar instances. 
 	
 	/**
 	 * Initializes the A* class
 	 * @param world Game world
 	 */
 	public static void initialize(IWorld world)
 	{
 		AStar.world = world;
 	}
 	
 	/**
 	 * @return true if AStar is initialized, otherwise false.
 	 */
 	public static boolean isInitialized()
 	{
 		return (world != null);
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
 	public static AStarNode getClosestUnoccupiedNode(Position startingPos, Position towards, int occupyingEntityID)
 	{
 		checkInit();
 		// Use A* "backwards" to find the closest walkable node.
 		AStarNode targetNode = new AStarNode(world.getNodeAt(startingPos));
 		AStarNode towardsNode;
 		
 		if (towards == null)
 		{
 			towardsNode = null;
 		}
 		else
 		{
 			towardsNode = new AStarNode(world.getNodeAt(towards));
 		}
 		List<AStarNode> closedList = new ArrayList<AStarNode>();
 		List<AStarNode> openList = new ArrayList<AStarNode>();
 		
 		openList.add(targetNode);
 		while (!openList.isEmpty())
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
 						node.calculateHeuristic(towardsNode, 1);
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
 	 * @param heuristicModifier Default is 10. A high heuristic modifier results in faster
 	 * A* calculations but a more inaccurate path. A lower modifier results in slower
 	 * calculations but a more accurate and shorter path.
 	 * @param occupyingEntityID ID of occupying entity.
 	 * @return An AStarPath from startPos to targetPos.
 	 */
 	public static void calculatePath(Position startPos, Position targetPos, int heuristicModifier,
 			int occupyingEntityID, AStarUser astarUser)
 	{
 		checkInit();
 		// Plankton: Use threads to not slow down the game when using many agents?
 		PathCalculator pathCalculator = new PathCalculator(startPos, targetPos,
 				heuristicModifier, occupyingEntityID, astarUser);
		new Thread(pathCalculator).start();
		//pathCalculator.run();
 	}
 	
 	private static AStarPath generatePath(AStarNode startNode, AStarNode endNode)
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
 	
 	private static void checkInit()
 	{
 		if (!isInitialized())
 		{
 			throw new IllegalStateException("You must initialize the AStar class before you can use it");
 		}
 	}
 	
 	private static class PathCalculator implements Runnable
 	{
 		private Position startPos;
 		private Position targetPos;
 		private int heuristicModifier;
 		private int occupyingEntityID;
 		private AStarUser astarUser;
 		
 		public PathCalculator(Position startPos, Position targetPos, int heuristicModifier,
 				int occupyingEntityID, AStarUser astarUser)
 		{
 			this.startPos = startPos;
 			this.targetPos = targetPos;
 			this.heuristicModifier = heuristicModifier;
 			this.occupyingEntityID = occupyingEntityID;
 			this.astarUser = astarUser;
 		}
 		
 		@Override
 		public void run() {
 			AStarNode startNode = new AStarNode(world.getNodeAt(startPos));
 			AStarNode endNode = new AStarNode(world.getNodeAt(targetPos));
 			List<AStarNode> openList = new ArrayList<AStarNode>();
 			List<AStarNode> closedList = new ArrayList<AStarNode>();
 			// Sets the search limit to 10 times the approximate distance to the target (or 50 if the distance is short)
 			startNode.calculateHeuristic(endNode, 10);
 			int searchlimit = Math.max(startNode.getHeuristic(), 50);
 			
 			if (endNode.isObstacle(occupyingEntityID))
 			{
 				// Use A* "backwards" from the end node to find the closest walkable node.
 				// Probably not the best way of dealing with it, but it will do for now.
 				endNode = getClosestUnoccupiedNode(targetPos, startPos, occupyingEntityID);
 			}
 			
 			// A* algorithm starts here
 			openList.add(startNode);
 			while (!openList.isEmpty()) // while open list is not empty
 			{
 				// current node  = node from the open list with the lowest cost
 				Collections.sort(openList);
 				AStarNode currentNode = openList.get(0);
 				
 				if (currentNode.equals(endNode))
 				{
 					astarUser.receivePath(generatePath(startNode, currentNode));
 					return;
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
 								node.calculateHeuristic(endNode, heuristicModifier);
 							}
 						}
 					}
 				}
 				// if too many nodes are searched without finding a way, break out of loop.
 				if (closedList.size() > searchlimit)
 				{
 					break;
 				}
 			}
 			
 			// path not found, return path to the node closest to the target instead.
 			
 			Collections.sort(closedList, AStarNode.getHeuristicComparator());
 			astarUser.receivePath(generatePath(startNode, closedList.get(0)));
 		}
 	}
 }
