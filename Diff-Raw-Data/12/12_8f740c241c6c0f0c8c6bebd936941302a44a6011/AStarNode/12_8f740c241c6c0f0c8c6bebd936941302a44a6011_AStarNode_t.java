 package projectrts.model.abilities.pathfinding;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import projectrts.model.world.INode;
 import projectrts.model.world.Position;
 
 /**
  * A node that is specifically configured to work with the A* pathfinding algorithm.
  * @author Bjorn Persson Mattsson
  *
  */
 public class AStarNode implements Comparable<AStarNode> {
 	
 	private final INode node;
 	private int costFromStart;
 	private int heuristic;
 	private AStarNode parent;
 	
 	/**
 	 * Creates a new AStarNode that refers to the provided node.
 	 * @param node Node
 	 */
 	public AStarNode(INode node)
 	{
 		this.node = node;
 	}
 	
 	/**
 	 * @return The neighbours (adjacent nodes) to this one.
 	 */
 	public List<AStarNode> getNeighbours()
 	{
 		List<AStarNode> output = new ArrayList<AStarNode>();
 		List<INode> neighbours = node.getNeighbours();
 		
 		for (INode n : neighbours)
 		{
 			output.add(new AStarNode(n));
 		}
 		return output;
 	}
 	
 	/**
 	 * Calculates the heuristic from this node to the end node.
 	 * @param endNode End node
 	 * @param heuristicModifier Default is 10. A high heuristic modifier results in faster
 	 * A* calculations but a more inaccurate path. A lower modifier results in slower
 	 * calculations but a more accurate and shorter path.
 	 */
 	public void calculateHeuristic(AStarNode endNode, int heuristicModifier)
 	{
 		if (heuristicModifier < 1)
 		{
 			heuristicModifier = 1;
 		}
 		Position mePos = this.getPosition();
 		Position endPos = endNode.getPosition();
		
		/*
 		// Calculating heuristic using the "Manhattan" distance
 		this.heuristic = ((int) (Math.abs(endPos.getX() - mePos.getX()) +
 				Math.abs(endPos.getY() - mePos.getY())))*heuristicModifier;
				*/
 		
 		// Calculating heuristic based on approximate distance (using diagonals and side movement)
		
 		int deltaX = (int) Math.abs(endPos.getX() - mePos.getX());
 		int deltaY = (int) Math.abs(endPos.getY() - mePos.getY());
 		int sidewayMove = Math.abs(deltaX - deltaY);
 		int diagonalMove = Math.max(deltaX, deltaY) - sidewayMove;
 		this.heuristic = Math.round((sidewayMove*1.4f + diagonalMove)*heuristicModifier);
		
 	}
 
 	/**
 	 * Calculates the cost from start for this node.
 	 * @param parentNode Parent node
 	 * @param refresh If true, overwrites the old result only if the new result is better.
 	 * If false, overwrites the old result no matter what.
 	 */
 	public void calculateCostFromStart(AStarNode parentNode, boolean refresh)
 	{
 		Position mePos = this.getPosition();
 		Position parPos = parentNode.getPosition();
 		int distance = calcNodeDistance(mePos, parPos);
 		
 		int newCostFromStart = (int) Math.round(parentNode.getCostFromStart() + distance*node.getCost());
 		if (!refresh || newCostFromStart < this.costFromStart)
 		{
 			this.costFromStart = newCostFromStart;
 			this.parent = parentNode;
 		}
 	}
 	
 	// Calculates "A*" distance between two adjacent nodes.
 	private int calcNodeDistance(Position pos1, Position pos2) {
 		int distance = 0;
 		if (pos1.getX() == pos2.getX() && pos1.getY() == pos2.getY())
 		{
 			distance = 0;
 		}
 		else if (pos1.getX() != pos2.getX() && pos1.getY() != pos2.getY())
 		{
 			distance = 14; //diagonal: sqrt(2) ~= 1.4, faster to use int than double.
 		}
 		else
 		{
 			distance = 10;
 		}
 		return distance;
 	}
 
 	/**
 	 * @return The total cost from the start node.
 	 */
 	public int getCostFromStart()
 	{
 		return costFromStart;
 	}
 	
 	/**
 	 * @return The total cost of the node (cost from start + heuristic).
 	 */
 	public int getTotalCost()
 	{
 		return this.costFromStart + this.heuristic;
 	}
 	
 	/**
 	 * @return The heuristic of the node.
 	 */
 	public int getHeuristic()
 	{
 		return heuristic;
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
 	
 	/**
 	 * Returns a comparator that uses the heuristic instead of the total cost.
 	 * @return Heuristic comparator.
 	 */
 	public static Comparator<AStarNode> getHeuristicComparator()
 	{
 		return new Comparator<AStarNode>() {
 
 			@Override
 			public int compare(AStarNode o1, AStarNode o2) {
 				return Integer.compare(o1.getHeuristic(), o2.getHeuristic());
 			}
 		};
 	}
 	
 	@Override
 	public int compareTo(AStarNode other) {
 		return Integer.compare(getTotalCost(), other.getTotalCost());
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((node == null) ? 0 : node.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		AStarNode other = (AStarNode) obj;
 		if (node == null) {
 			if (other.node != null)
 				return false;
 		} else if (!node.equals(other.node))
 			return false;
 		return true;
 	}
 
 	/**
 	 * @param occupyingEntityID ID of occupying entity.
 	 * @return true if this node is unwalkable, otherwise false.
 	 */
 	public boolean isObstacle(int occupyingEntityID) {
 		return node.isOccupied(occupyingEntityID);
 	}
 	
 	/**
 	 * @return The "real" node (Node).
 	 */
 	public INode getNode()
 	{
 		return node;
 	}
 }
