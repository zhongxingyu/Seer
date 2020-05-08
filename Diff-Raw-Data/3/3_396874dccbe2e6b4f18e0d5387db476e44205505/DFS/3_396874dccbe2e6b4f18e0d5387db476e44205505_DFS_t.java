 package main.GraphAlgorithms.Trees;
 import java.util.*;
 
 public class DFS
 {
 	// given the an array of nodes, will traverse the entire tree in a depth
 	// first search, starting from the node at graph[0]
 	public ArrayList<Node> DFS(ArrayList<Node> graph)
 	{
 		ArrayList<Node> stack = new ArrayList<Node>();
 		ArrayList<Node> path = new ArrayList<Node>();
 
 		stack.add(graph.get(0));
 		stack.get(0).visited = true; // so that we don't visit it again\
 		path.add(graph.get(0));
 		while(!stack.isEmpty())
 		{
 			// remove the node on the top of the stack
 			Node cur_node = stack.remove(stack.size() - 1);
 			for(Node child : cur_node.children)
 			{
 				if(!child.visited)
 				{
 					stack.add(child);
 					child.visited = true;
 				}
 			}
 			path.add(cur_node);
 		}
		return path;
 	}
 	
 	//arrrrr
 	
 	public ArrayList<Node> DFS(ArrayList<Node> graph, int start)
 	{
 		ArrayList<Node> stack = new ArrayList<Node>();
 		ArrayList<Node> path = new ArrayList<Node>();
 		
 		stack.add(graph.get(start));
 		stack.get(start).visited = true; // so that we don't visit it again
 		path.add(graph.get(start));
 		while(!stack.isEmpty())
 		{
 			// remove the node on the top of the stack
 			Node cur_node = stack.remove(stack.size() - 1);
 			for(Node child : cur_node.children)
 			{
 				if(!child.visited)
 				{
 					stack.add(child);
 					child.visited = true;
 				}
 			}
 			path.add(cur_node);
 		}
		return path;
 	}
 
 	public static class Node
 	{
 		public ArrayList<Node> children;
 		public boolean visited = false;
 		public String name = "(no name)";
 
 		public Node(ArrayList<Node> childs)
 		{
 			this.children = new ArrayList<Node>(childs);
 			this.visited = false;
 			this.name = "(no name)";
 		}
 	}
 }
