 package org.aimas.craftingquest.core.pathfinding;
 
 import java.util.*;
 
 import org.aimas.craftingquest.state.Point2i;
 import org.aimas.craftingquest.state.UnitState;
 
 public class Astar implements IPathFinder
 {
 	private int maxSearchDistance;
 	private AStarHeuristic heuristic;
 	
 	public Astar()
 	{
 		maxSearchDistance = 500;
 		heuristic = new AStarHeuristic();
 	}
 	
 	public Astar(int maxSearchDist)
 	{
 		maxSearchDistance = maxSearchDist;
 		heuristic = new AStarHeuristic();
 	}
 	
 	//public AStarPath findPath(int xstart, int ystart, int xtarget, int ytarget)
 	public IPath computePath(Point2i from, Point2i to, IPathMap map, UnitState u)
 	{
 		int xstart = from.x;
 		int ystart = from.y;
 		int xtarget = to.x;
 		int ytarget = to.y;
 		
		if (map.getCellCost(new Point2i(xtarget, ytarget), u) > (IPathMap.INF-1.0f))
 		{
 			return null;
 		}
 		
 		if (xstart == xtarget && ystart == ytarget)
 		{
 			return new AStarPath();
 		}
 		
 		//initialize
 		ArrayList<Node> closed = new ArrayList<Node>();
 		SortedList open = new SortedList();
 		
 		Node[][] nodes = new Node[map.getWidth()][map.getHeight()];
 		for (int x=0; x<map.getWidth(); ++x)
 		{
 			for (int y=0; y<map.getHeight(); ++y)
 			{
 				nodes[x][y] = new Node(x,y);
 			}
 		}
 		
 		//start algorithm
 		
 		nodes[xstart][ystart].cost = 0; //i`m already here
 		nodes[xstart][ystart].depth = 0; //it's the begining of the path
 		closed.clear();
 		open.clear();
 		open.add(nodes[xstart][ystart]);
 		
 		int currentDepth = 0;
 		int xn, yn;
 		
 		while ((currentDepth < maxSearchDistance) && (open.size() != 0))
 		{
 			Node current = (Node)open.first();
 			
 			if (current == nodes[xtarget][ytarget])
 			{
 				break;
 			}
 			
 			open.remove(current);
 			closed.add(current);
 			
 			Point2i mmc;
 			float nextStepCost;
 			Node neighbour;
 			
 			for (int x=-1; x<2; ++x)
 			{
 				for (int y=-1; y<2; ++y)
 				{
 					if (x==0 && y==0) //current cell
 					{
 						continue;
 					}
 					
 					xn = x + current.x;
 					yn = y + current.y;
 					
 					if ( (xn < 0) || (yn < 0)  || (xn >= map.getWidth()) || (yn >= map.getHeight()) )
 					{
 						continue;
 					}
 					
 					mmc = new Point2i(xn, yn);
 					
 					if (isValidLocation(u, xstart, ystart, xn, yn, mmc, map))
 					{
 						nextStepCost = current.cost + map.getCellCost(mmc, u);
 						
 						/* ************ */
 						
 						neighbour = nodes[xn][yn];
 						
 						if (nextStepCost < neighbour.cost)
 						{
 							if (open.contains(neighbour))
 							{
 								open.remove(neighbour);
 							}
 							if (closed.contains(neighbour))
 							{
 								closed.remove(neighbour);
 							}
 						}
 						
 						if (!open.contains(neighbour) && ! closed.contains(neighbour))
 						{
 							neighbour.cost = nextStepCost;
 							neighbour.heuristic = heuristic.getCost(xn, yn, xtarget, ytarget);
 							currentDepth = Math.max(currentDepth, neighbour.setParent(current));
 							open.add(neighbour);
 						}
 					}
 				}
 			}
 		}
 		
 		if (nodes[xtarget][ytarget].parent == null)
 		{
 			return null; // there is no spoon (path)
 		}
 		
 		AStarPath path = new AStarPath();
 		Node target = nodes[xtarget][ytarget];
 		while (target != nodes[xstart][ystart])
 		{
 			path.prependStep(target.x, target.y);
 			target = target.parent;
 		}
 		//path.prependStep(xstart,ystart); // we don't want the first cell..
 		
 		// thats it, we have our path 
 		return path;
 	}
 	
 	private boolean isValidLocation(UnitState u, int sx, int sy, int x, int y, Point2i mmc, IPathMap map)
 	{
 		boolean invalid = (x < 0) || (y < 0) || (x >= map.getWidth()) || (y >= map.getHeight());
 		
 		if ((!invalid) && ((sx != x) || (sy != y)))
 		{
			invalid = (map.getCellCost(mmc, u) > (IPathMap.INF-1.0f));
 		}
 		
 		return !invalid;
 	}
 	
 	
 	private class SortedList
 	{
 		private ArrayList list = new ArrayList();
 		
 		public Object first()
 		{
 			return list.get(0);
 		}
 		
 		public void add(Object o)
 		{
 			list.add(o);
 			Collections.sort(list);
 		}
 		
 		public void remove(Object o)
 		{
 			list.remove(o);
 		}
 		
 		public void clear()
 		{
 			list.clear();
 		}
 	
 		public int size()
 		{
 			return list.size();
 		}
 		
 		public boolean contains(Object o)
 		{
 			return list.contains(o);
 		}
 	}
 	
 	private class Node implements Comparable
 	{
 		private int x;
 		private int y;
 		private float cost;
 		private Node parent;
 		private float heuristic;
 		private int depth; // search depth of this node
 		
 
 		public Node(int x, int y)
 		{
 			this.x = x;
 			this.y = y;
 		}
 		
 		public int setParent(Node parent)
 		{
 			depth = parent.depth + 1;
 			this.parent = parent;
 			
 			return depth;
 		}
 		
 		public int compareTo(Object other)
 		{
 			Node o = (Node) other;
 			
 			float f = heuristic + cost;
 			float of = o.heuristic + o.cost;
 			
 			if (f < of)
 			{
 				return -1;
 			}
 			else if (f > of)
 			{
 				return 1;
 			}
 			else
 			{
 				return 0;
 			}
 		}
 	}
 }
