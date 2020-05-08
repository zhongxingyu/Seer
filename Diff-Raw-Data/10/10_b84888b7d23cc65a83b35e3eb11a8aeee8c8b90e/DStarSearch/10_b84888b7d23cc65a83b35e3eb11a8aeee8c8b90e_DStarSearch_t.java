 /*
  * This file is part of the search package.
  *
  * Copyright (C) 2012, Eric Fritz
  * Copyright (C) 2012, Reed Johnson
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
  * and associated documentation files (the "Software"), to deal in the Software without 
  * restriction, including without limitation the rights to use, copy, modify, merge, publish, 
  * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
  * Software is furnished to do so, subject to the following conditions: 
  * 
  * The above copyright notice and this permission notice shall be included in all copies or 
  * substantial portions of the Software. 
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
  * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
  */
 
 package edu.uwm.ai.search;
 
 /**
  * TODO: Figure out a way to get a path to not repeat values endlessly
  * TODO: Find a way to return a SearchPath given that pred is an ArrayList of DNodes and not points.
  * TODO: Figure out if predecessors needs to be a list of all possible predecessors instead of just THE predecessor
  * 
  */
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.PriorityQueue;
 import java.lang.Math;
 
 /**
  * @author Eric Fritz
  * @author Reed Johnson
  */
 public class DStarSearch extends BaseSearchAlgorithm
 {	
 	/*
 	 * D* Searches from the goal to the start vertex, not from start to goal.
 	 * To this end, the g values and rhs values are estimates to the goal, not the start.
 	 * The constructor method for the search algorithm acts as the initialize function for D* Lite
 	 */
 	private Map<Integer,DNode> vertexHash;
 	private PriorityQueue<DNode> openList;
 	private Map<DNode, DNode> pred;
 	private int k_m;
 	//The start and goal nodes, assigned and referenced within getPath
 	private DNode start_n, goal_n;
 	
 	public DStarSearch(World w)
 	{
 		super(w);
 		vertexHash = new HashMap<Integer, DNode>();
 		openList = new PriorityQueue<DNode>();
 		pred = new HashMap<DNode, DNode>();
 		
 		//start_n = new DNode(new Point(0,0), Integer.MAX_VALUE, Integer.MAX_VALUE);
 		//goal_n = new DNode(new Point(0,0), 0, Integer.MAX_VALUE);
 		start_n = null;
 		goal_n = null;
 		k_m = 0;
 		
 		//Place the start and goal in the list of known/discovered vertices.
 		//vertexHash.put(vertexHashCode(start_n), start_n);
 		//vertexHash.put(vertexHashCode(goal_n),goal_n);
 		
 		
 		
 	}
 	/**
 	 * Extension of point class which provides the extra information needed
 	 * to properly implement the D* search algorithm. 
 	 * @author Reed Johnson
 	 * @author Eric Fritz
 	 * @date 11.12.2012 (Last modified by Reed Johnson)
 	 *
 	 */
 	@SuppressWarnings("rawtypes")
 	private class DNode extends Point implements Comparable{
 		
 		private int rhs;
 		private int g_val;
 		private int cost;
 		//k1 and k2 are the two parts of the node's key. The key is used to compare different nodes in priority queue
 		private int k1, k2;
 		
 		public DNode(Point p, int rhs, int g_val){
 			super(p.getX(),p.getY());
 			this.g_val = g_val;
 			this.rhs = rhs;
 			this.cost = 1;
 		}
 		
 		public DNode(DNode d)
 		{
 			super(d.getX(), d.getY());
 			this.g_val = d.getG();
 			this.rhs = d.getRhs();
 			this.k1 = d.getK1();
 			this.k2 = d.getK2();
 		}
 		
 		
 		
 		/**
 		 * 
 		 * @return The right-hand side value
 		 */
 		public int getRhs(){
 			return rhs;
 		}
 		
 		public void setRhs(int new_rhs)
 		{
 			this.rhs = new_rhs;
 		}
 		
 		/**
 		 * Returns the integer representing the cost to traverse this DNode.
 		 * If it returns Integer.MAX_INT the node is blocked.
 		 * @return int The integer cost to traverse this node.
 		 */
 		public int getCost()
 		{
 			return cost;
 		}
 		
 		/**
 		 * Sets the cost to traverse this DNode.
 		 * Passing Integer.MAX_INT means the node is blocked.
 		 * @param new_cost int The new cost to traverse this DNode.
 		 */
 		@SuppressWarnings("unused")
 		public void setCost(int new_cost)
 		{
 			this.cost = new_cost;
 		}
 		
 		/**
 		 * Returns the DNode's g value
 		 * @return g_val The DNode's g value as an integer
 		 * @date 11.12.2012
 		 */
 		public int getG()
 		{
 			return g_val;
 		}
 		
 		/**
 		 * Sets the DNode's g value to an integer specified by the parameter new_g
 		 * @param new_g An integer value representing the new g value for the Node
 		 * @date 11.12.2012
 		 */
 		public void setG(int new_g)
 		{
 			this.g_val = new_g;
 		}
 		
 		/**
 		 * Set the Node's first key value. When automatically calculated, this is equal to
 		 *  min(g, rhs) + h(start, this) + k_m
 		 * @param new_k1
 		 */
 		public void setK1(int new_k1)
 		{
 			this.k1 = new_k1;
 		}
 		
 		/**
 		 * Returns the DNode's first key. This is the key given by min(g, rhs) + h(start, this) + k_m
 		 * @return k1 The integer value representing the DNode's first key 
 		 * @date 11.12.2012 
 		 */
 		public int getK1()
 		{
 			return this.k1;
 		}
 		
 		/**
 		 * Sets the Node's second key value. When automatically calculated, this is the min of g and rhs values.
 		 * @param new_k2 The integer value representing the new second key for the DNode.
 		 * @date 11.12.2012 (Last modified by Reed Johnson)
 		 */
 		public void setK2(int new_k2)
 		{
 			this.k2 = new_k2;
 		}
 		
 		/**
 		 * Returns the DNode's first key. This is the key given by min(g, rhs) + h(start, this) + k_m
 		 * @return k1 The integer value representing the DNode's first key 
 		 * @date 11.12.2012 
 		 */
 		public int getK2()
 		{
 			return this.k2;
 		}
 		
 		/**
 		 * Sets the Node's (x,y) location to that of the point passed in
 		 * @param new_point A Point object that holds the new x,y location.
 		 * @date 11.12.2012 (Last modified by Reed Johnson)
 		 */
 		public void setPoint(Point new_point)
 		{
 			this.setX(new_point.getX());
 			this.setY(new_point.getY());
 		}
 		
 		/**
 		 * Calculates the int-pair of keys that is used to order each DNode in a priority queue.
 		 * @date 11.12.2012 (Last modified by Reed Johnson)
 		 */
 		public void calculateKey()
 		{
 			this.k1 = Math.min(this.g_val, this.rhs) + heuristic(start_n, this) + k_m;
 			this.k2 = Math.min(this.g_val, this.rhs);
 		}
 		
 		@Override
 		public boolean equals(Object o)
 		{
 			if (o == this) {
 				return true;
 			}
 
 			if (o == null || !(o instanceof Point)) {
 				return false;
 			}
 
 			Point other = (Point) o;
 			return other.getX() == this.getX() && other.getY() == this.getY();
 		}
 
 		/**
 		 * Orders DNode objects based on their key values as described in the D* Lite algorithm
 		 * @Author Reed Johnson
 		 * @Author Eric Fritz
 		 * @Date 11.12.2012 (Last modified by Reed Johnson)
 		 */
 		@Override
 		public int compareTo(Object arg0) {
 			DNode other = (DNode) arg0;
 			//If my first key is less than other first key, I am less than the other
 			if(this.k1 < other.k1 )
 			{
 				return -1;
 			}
 			//Else if my first key is the same as other's first key
 			else if(this.k1 == other.k1)
 			{
 				//If my second key is less than other's second key, then I am less than other.
 				if (this.k2 < other.k2)
 				{
 					return -1;
 				}
 				else if(this.k2 == other.k2)
 				{
 					return 0;
 				}
 				else
 				{
 					return 1;
 				}
 			}
 			//Else my first key is greater than other's first key, and I am greater than other.
 			else
 			{
 				return 1;
 			}
 		}
 	}
 	
 	private int heuristic(Point p, Point goal)
 	{
 		return Math.abs(p.getX() - goal.getX()) + Math.abs(p.getY() - goal.getY());
 	}
 	
 	//private void calculate
 	
 	@Override
 	public SearchResult search(Point initial, Point goal)
 	{
 		//Update the start and goal nodes to the new initial and goal positions
 		//start_n.setPoint(initial);
 		//goal_n.setPoint(goal);
 		if(vertexHash.containsKey(vertexHashCode(initial)))
 		{
 			start_n = vertexHash.get(vertexHashCode(initial));
 		}
 		else
 		{
 			start_n = new DNode(initial, Integer.MAX_VALUE, Integer.MAX_VALUE);
 			vertexHash.put(vertexHashCode(initial), start_n);
 		}
 		
 		if(vertexHash.containsKey(vertexHashCode(goal)))
 		{
 			goal_n = vertexHash.get(vertexHashCode(goal));
 		}
 		else
 		{
 			goal_n = new DNode(goal, 0, Integer.MAX_VALUE);
 			vertexHash.put(vertexHashCode(goal), goal_n);
 		}
 		
 		
 		//Goal's key values are updated each time SearchResult is called. The assumption in the original D* Lite paper was
 		//that the goal did not move, but the environment changed. In our implementation, the goal also moves.
 		goal_n.setK1(heuristic(start_n, goal_n));
 		goal_n.setK2(0);
 		
 		pred.put(goal_n, null);
 		openList.add(goal_n);
 		
 		start_n.calculateKey();
 		DNode u = openList.peek();
 		while((u.compareTo(start_n) == -1) || start_n.getRhs() > start_n.getG())
 		{
 			//Store u's old keys in a deep copy
 			DNode k_new = new DNode(u);
 			//Recalculate its key, then compare it.
 			k_new.calculateKey();
 			
 			if(u.compareTo(k_new) == -1)
 			{
 				//Remove old u node, insert updated u node.
 				u.calculateKey();
 				openList.poll();
 				openList.offer(u);
 			}
 			else if (u.getG() > u.getRhs())
 			{
 				u.setG(u.getRhs());
 				//Remove u
 				openList.poll();
 				for(DNode s : successors(u))
 				{
 					if(!s.equals(goal_n))
 					{
 						s.setRhs(Math.min(s.getRhs(), s.getCost() + u.getG()));
 					}
 					updateVertex(s);
 					
 				}
 			}
 			else
 			{
 				int g_old = u.getG();
 				u.setG(Integer.MAX_VALUE);
 				//Get the G values of u's successors
 				ArrayList<DNode> u_succ = successors(u);
 				int u_min = minimumOfSucc(u_succ);
 				//Following is done on pred(u)union{u}, so u is done independently, then predecessors are iterated through
 				if(u.getRhs() == g_old){
 					if(!u.equals(goal_n))
 					{
 						u.setRhs(u_min);
 					}
 					updateVertex(u);
 				}
 				//The above is now performed on pred(u)
 				for(DNode s : backtrace(pred, u))
 				{
 					ArrayList<DNode> s_succ = successors(s);
 					int s_min = minimumOfSucc(s_succ);
 					if(s.getRhs() == g_old){
 						if(!s.equals(goal_n))
 						{
 							s.setRhs(s_min);
 						}
 						updateVertex(s);
 					}
 				}
 			}
 			//Start again with a new u.
 			u = openList.peek();
 			//Calculate start again
 			start_n.calculateKey();
 			
 		}
 		//A path exists to the goal
 		if(start_n.getRhs() != Integer.MAX_VALUE)
 		{
 			return new SearchResult(getPath(start_n, goal_n), 10);
 		}
 		//No path exists to goal. 
 		return new SearchResult(new LinkedList<Point>(), 0);
 	}
 	
 	/**
 	 * Takes an ArrayList of DNodes and returns the minimum of g(node) + cost(node) for each 
 	 * DNode.
 	 * @param successors An ArrayList of DNodes
 	 * @return int The integer value of the smallest g + cost
 	 * @date 11.12.2012 (Last modified by Reed Johnson)
	 * @date 11.17.2012 (Last modified by Eric Fritz)
 	 */
 	private int minimumOfSucc(ArrayList<DNode> successors)
 	{
		if (successors.isEmpty()) {
			return Integer.MIN_VALUE;
		}

 		ArrayList<Integer> g_values = new ArrayList<Integer>();
 		{
 			g_values.set(i, successors.get(i).getG() + successors.get(i).getCost());
		for (int i = 0; i < successors.size(); i++) {
 		}
 		return Collections.min(g_values);
 	}
 	
 	/**
 	 * Takes a node, and returns an ArrayList of DNodes of all the successors of the DNode.
 	 * If the DNode is not yet a member of the vertexHash, it is added to the hashmap and the ArrayList.
 	 * If the DNode is a member of the vertexHash, it is added to the ArrayList.
 	 * @param u The DNode required to generate successors
 	 * @return The ArrayList of all possible successors to the current node.
 	 * @date 11.12.2012 (Last modified by Reed Johnson)
 	 */
 	private ArrayList<DNode> successors(DNode u)
 	{
 		//Get a list of successors, determine whether they have been discovered. If not, add them to vertexHash
 		List<Point> temp_list = getSuccessors(new Point(u.getX(), u.getY()));
 		ArrayList<DNode> succ_nodes = new ArrayList<DNode>();
 		for (Point pt : temp_list)
 		{
 			int item_hash = vertexHashCode(pt);
 			if(vertexHash.containsKey(item_hash))
 			{
 				//The vertex already exists, so add it to the list of successors
 				succ_nodes.add(vertexHash.get(item_hash));
 			}
 			else
 			{
 				//The vertex does not exist. Create it, add to vertexHash, add to list of successors.
 				DNode temp_node = new DNode(pt, Integer.MAX_VALUE, Integer.MAX_VALUE);
 				vertexHash.put(vertexHashCode(temp_node), temp_node);
 				succ_nodes.add(temp_node);
 			}
 		}
 		return succ_nodes;
 	}
 
 	private void updateVertex(DNode u)
 	{
 		boolean is_in_open = openList.contains(u);
 		if(is_in_open)
 		{
 			if(u.getG() == u.getRhs())
 			{
 				//U is locally consistent. Remove it from the open list.
 				openList.remove(u);
 			}
 			else
 			{
 				//U is inconsistent. Change its key
 				openList.remove(u);
 				u.calculateKey();
 				openList.offer(u);
 			}
 		}
 		else
 		{
 			if(u.getG() != u.getRhs())
 			{
 				//u is locally inconsistent but not in the open list. Add it
 				openList.offer(u);
 			}
 		}
 		
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "D*";
 	}
 	
 	/**
 	 * Simple function that returns an integer value to place the DNode d into the hash table of vertices
 	 * @param d An initialized DNode object.
 	 * @return An integer representing the code to associate with that node
 	 * @date 11.17.2012 (Last updated by Reed Johnson) changed prime to 8239
 	 */
 	private int vertexHashCode(DNode d)
 	{
 		return 1039*d.getX() + 8329*d.getY();
 	}
 	
 	/**
 	 * Simple function that returns an integer value to determine whether the DNode constructed
 	 * from point P exists in vertexHash
 	 * @param d An initialized DNode object.
 	 * @return An integer representing the code to associate with that node
 	 */
 	private int vertexHashCode(Point p)
 	{
 		return 1039*p.getX() + 8329*p.getY();
 	}
 	
 	/**
 	 * The same implementation as backtrace in BaseSearchAlgorithm except specialized for use with DNodes.
 	 * @param predecessors
 	 * @param p
 	 * @return
 	 */
 	private List<DNode> backtrace(Map<DNode, DNode> predecessors, DNode p)
 	{
 		List<DNode> path = new LinkedList<DNode>();
 
 		while (p != null) {
 			path.add(0, p);
 			p = predecessors.get(p);
 		}
 
 		return path;
 	}
 	
 	/**
 	 * Calculates a path from the starting node to the goal node by choosing the successor
 	 * that minimizes c(s,s')+g(s') where s' is a successor of s. 
 	 * @param startNode The beginning node of the path
 	 * @param goalNode The end node of the path
 	 * @return A LinkedList of Point objects that provides a path from the start to the goal
 	 * @date 11.17.2012 (Last edited by Reed Johnson)
 	 */
 	private List<Point> getPath(DNode startNode, DNode goalNode)
 	{
 		List<Point> path = new LinkedList<Point>();
 		
 		DNode currentNode = startNode;
 		//Add start to path, then continually add successors that 
 		path.add(new Point(currentNode.getX(),currentNode.getY()));
 		List<DNode> possibleSuccessorsList;
 		List<DNode> actualSuccessors = new ArrayList<DNode>();
 		
 		while(!currentNode.equals(goalNode))
 		{
 			//Clear the list of successors from previous search
 			actualSuccessors.clear();
 			possibleSuccessorsList = successors(currentNode);
 			
 			//Only add successors which have been discovered and are in the vertex hashmap
 			for(DNode s : possibleSuccessorsList)
 			{
 				//Check if location has been added to vertex and is not already part of path. 
 				if(vertexHash.containsValue(s) && !path.contains(new Point(s.getX(),s.getY())))
 				{
 					actualSuccessors.add(s);
 				}
 			}
 			//The new currentNode is one such that it minimizes the cost to reach it added to its g value.
 			currentNode = Collections.min(actualSuccessors, new Comparator<DNode>(){
 				@Override
 				public int compare(DNode o1, DNode o2)
 				{
 					int cost1 = o1.getCost() + o1.getRhs();
 					int cost2 = o2.getCost() + o2.getRhs();
 					
 					//Guard against math wrapping around. Because infinity is Integer.MAX_VALUE, when
 					//Integer.MAX_VALUE + 1 is performed, the value becomes Integer.MIN_VALUE
 					if(cost1 == Integer.MAX_VALUE || cost2 == Integer.MAX_VALUE)
 					{
 						if(cost1 < cost2)
 						{
 							return -1;
 						}
 						else
 						{
 							return 1;
 						}
 					}
 					
 					if(cost1 < cost2)
 					{
 						return -1;
 					} 
 					else if (cost1 == cost2)
 					{
 						int distFromGoal1 = o1.getK1();
 						int distFromGoal2 = o2.getK1();
 						//If costs are equal, determine their heuristic distance
 						if (distFromGoal1 < distFromGoal2)
 						{
 							return -1;
 						}
 						else if (distFromGoal1 == distFromGoal2)
 						{
 							return 0;
 						}
 						else
 						{
 							return 1;
 						}
 					}
 					else
 					{
 						return 1;
 					}
 				}
 			});
 			path.add(new Point(currentNode.getX(),currentNode.getY()));
 		}
 		
 		return path;
 	}
 }
