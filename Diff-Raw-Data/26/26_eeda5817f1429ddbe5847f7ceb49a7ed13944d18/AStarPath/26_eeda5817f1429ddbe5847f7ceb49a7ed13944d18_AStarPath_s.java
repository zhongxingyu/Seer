 package projectrts.model.core.pathfinding;
 
 import java.util.Stack;
 
 import projectrts.model.core.Position;
 
 /**
  * A class representing an A* movement path. This class uses a stack for storing the nodes.
  * @author Bjorn Persson Mattsson
  *
  */
 public class AStarPath {
 	
 	private Stack<AStarNode> nodeStack = new Stack<AStarNode>();
 	
 	/**
 	 * Adds a node to the path.
 	 * @param node The node that will be added.
 	 */
 	public void addNodeToPath(AStarNode node)
 	{
 		nodeStack.push(node);
 	}
 	
 	/**
 	 * @return Position of the next node in the path (stack).
 	 */
 	public Position getNextNodePosition()
 	{
 		return nodeStack.peek().getPosition();
 	}
 	
 	/**
	 * Removed the next node in the path from it.
 	 */
 	public void removeNodeFromPath()
 	{
 		nodeStack.pop();
 	}
 	
 	/**
 	 * @return The number of nodes in the path.
 	 */
 	public int nrOfNodesLeft()
 	{
 		return nodeStack.size();
 	}
 	
 	// TODO Plankton: Create a method for adding two paths into one. Could be useful... maybe.
 }
