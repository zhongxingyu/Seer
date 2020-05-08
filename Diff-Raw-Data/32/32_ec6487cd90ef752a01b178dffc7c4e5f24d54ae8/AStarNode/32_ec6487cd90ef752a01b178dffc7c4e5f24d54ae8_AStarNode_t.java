 package projectrts.model.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import projectrts.model.core.utils.ModelUtils;
 
 /**
  * A node that is specifically configured to work with the A* pathfinding algorithm.
  * @author Bjorn Persson Mattsson
  *
  */
 public class AStarNode implements Comparable<AStarNode> {
 	
 	private Node node;
 	private double totalCost = 0;
 	private double costFromStart;
 	private double heuristic;
 	private AStarNode parent;
 	
 	/**
 	 * Creates a new AStarNode that refers to the provided node.
 	 * @param node Node
 	 */
 	public AStarNode(Node node)
 	{
 		this.node = node;
 	}
 	
 	/**
 	 * @return The neighbours (adjecent nodes) to this one.
 	 */
 	public List<AStarNode> getNeighbours()
 	{
 		List<AStarNode> output = new ArrayList<AStarNode>();
 		List<Node> neighbours = node.getNeighbours();
 		
 		for (Node n : neighbours)
 		{
 			output.add(new AStarNode(n));
 		}
 		return output;
 	}
 	
 	/**
 	 * Calculated the cost for this node.
 	 * @param cost Cost
 	 */
 	public void calculateCost(AStarNode parentNode, AStarNode endNode)
 	{
 		this.costFromStart = parentNode.getCostFromStart() + node.getCost();
 		this.heuristic = ModelUtils.INSTANCE.getDistance(this.getPosition(), endNode.getPosition());
 		this.totalCost = this.costFromStart + this.heuristic;
 		this.parent = parentNode;
 	}
 	
 	/**
 	 * @return The total cost from the start node.
 	 */
 	public double getCostFromStart()
 	{
 		return costFromStart;
 	}
 	
 	/**
 	 * @return The total cost of the node (cost from start + heuristic).
 	 */
 	public double getTotalCost()
 	{
 		return totalCost;
 	}
 	
 	/**
 	 * @return Position of the node.
 	 */
 	public Position getPosition()
 	{
 		return node.getPosition();
 	}
 	
 	/**
 	 * @return Parent node
 	 */
 	public AStarNode getParent()
 	{
 		return parent;
 	}
 	
 	@Override
 	public int compareTo(AStarNode other) {
		return Double.compare(getTotalCost(), other.getTotalCost());
 	}
 	
 	/**
 	 * @param other Another AStarNode
 	 * @return true if both AStarNodes refer to the same Node, otherwise false.
 	 */
 	public boolean sameNodeAs(AStarNode other)
 	{
 		return node.equals(other.node);
 	}
 
 	/**
 	 * @return true if this node is unwalkable, otherwise false.
 	 */
 	public boolean isObstacle() {
 		return node.isOccupied();
 	}
 }
