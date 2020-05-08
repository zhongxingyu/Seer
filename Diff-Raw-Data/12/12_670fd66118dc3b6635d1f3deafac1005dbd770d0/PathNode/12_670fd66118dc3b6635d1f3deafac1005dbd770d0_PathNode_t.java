 package com.evervoid.state;
 
 import com.evervoid.state.geometry.Point;
 
 /**
  * Data structure used for the A* pathfinding algorithm.
  */
 class PathNode implements Comparable<PathNode>{
 	public int costSoFar; //Equivalent to g
 	public int goalHeuristic; //Equivalent to h
 	public int totalCost; //Equivalent to f (f = g + h)
 	private final Point fCoord;
 	public PathNode parent;
 	
 	/**
 	 * Constructor used to initialise nodes without path knowledge
 	 * has a null parent and 0 costSoFar.
 	 * @param pCoord Point associated with this node's coordinates.
 	 */
 	public PathNode(Point pCoord){
 		this(null, 0, pCoord);
 	}
 	
 	/**
 	 * Default PathNode constructor.
 	 * @param pParent Parent of this PathNode.
 	 * @param pCostSoFar Cost to reach this node from the origin.
 	 * @param pCoord Point associated with this node's coordinates.
 	 */
 	public PathNode(PathNode pParent, int pCostSoFar, Point pCoord){
 		costSoFar = pCostSoFar;
 		goalHeuristic = 0;
 		parent = pParent;
 		fCoord = pCoord;
 	}
 	
 	/**
 	 * Returns the coordinates associated with this PathNode.
 	 * @return The Point representing the coordinates of this PathNode.
 	 */
 	public Point getCoord(){
 		return fCoord;
 	}
 
 	@Override
 	public int compareTo(PathNode o)
 	{
		if (o.totalCost < totalCost)
 			return -1;
		else if (o.totalCost == totalCost)
 			return 0;
 		else
 			return 1;
 	}
 	
 	@Override
 	public boolean equals(final Object other)
 	{
 		if (super.equals(other)) {
 			return true;
 		}
 		if (other == null) {
 			return false;
 		}
 		if (!other.getClass().equals(getClass())) {
 			return false;
 		}
 		final PathNode l = (PathNode) other;
 		return fCoord.equals(l.getCoord());
 	}
 }
