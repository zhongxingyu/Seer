 package main;
 
 import java.util.Arrays;
 
 /**
  * // TODO: Graph is a ...
  * 
  * @author Martin Nycander
  * @since
  */
 public class Graph
 {
 	private int distanceCounts = 0;
 	private double[][] nodes;
 	private Edge[][] edges;
 	private int nodeCount;
 	private int edgeCount;
 
 	/**
 	 * Constructs a graph object given a set of coordinates.
 	 * 
 	 * @param coordinates
 	 *            is an array of coordinates in the format matrix[size] = {x, y}
 	 */
 	public Graph(double[][] coordinates)
 	{
 		nodes = coordinates;
 		nodeCount = coordinates.length;
 		edges = new Edge[nodeCount][nodeCount];
 		edgeCount = (nodeCount * (nodeCount - 1)) / 2;
 
 		// Precalculate edges
 		for (short a = 0; a < nodeCount; a++)
 		{
 			// TODO: Creating nullpointers? :(
 			for (short b = a; b < nodeCount; b++)
 			{
 				long distance = calculateDistance(a, b);
 
 				// assert (distance < Integer.MAX_VALUE);
 				int dist = (int) distance;
 				
 				// Store calculated values
 				edges[a][b] = new Edge(a, b, dist);
 				edges[b][a] = new Edge(b, a, dist);
 			}
 		}
 	}
 
 	/**
 	 * @return the number of nodes in the graph.
 	 */
 	public int countNodes()
 	{
 		return nodeCount;
 	}
 
 	public int countEdges()
 	{
 		return edgeCount;
 	}
 
 	/**
 	 * The distance between two points is computed as the Euclidean distance
 	 * between the two points, rounded to the nearest integer.
 	 * 
 	 * The order of the nodes does not matter to the outcome.
 	 * 
 	 * @param nodeA
 	 *            the first node.
 	 * @param nodeB
 	 *            the second node.
 	 * @return the euclidean distance between two nodes.
 	 */
 	private long calculateDistance(int nodeA, int nodeB)
 	{
 		double xDiff = nodes[nodeA][0] - nodes[nodeB][0];
 		double yDiff = nodes[nodeA][1] - nodes[nodeB][1];
 
 		++distanceCounts; // For stats
 
 		long distance = Math.round(Math.sqrt(xDiff * xDiff + yDiff * yDiff));
 		return distance;
 	}
 
 	public long distance(int nodeA, int nodeB)
 	{
 		return edges[nodeA][nodeB].length;
 	}
 
 	/**
 	 * @param tour
 	 * @return
 	 */
 	public int calculateLength(Tour tour)
 	{
 		int length = 0;
		for (Edge e : tour)
 		{
			length += e.length;
 		}
 		return length;
 	}
 	
 	/**
 	 * @param g
 	 * @return
 	 */
 	public Edge getEdge(int a, int b)
 	{
 		return edges[a][b];
 	}
 
 	public Edge[][] getEdges()
 	{
 		return edges;
 	}
 
 	/**
 	 * @param g
 	 * @return
 	 */
 	public Edge[] getSortedEdgeList()
 	{
 		Edge[] list = new Edge[edgeCount];
 		int ep = 0;
 		for (int a = 0; a < nodeCount; a++)
 		{
 			for (int b = a + 1; b < nodeCount; b++)
 			{
 				list[ep++] = edges[a][b];
 			}
 		}
 		Arrays.sort(list);
 		return list;
 	}
 
 
 	/**
 	 * @param a
 	 * @return
 	 */
 	public double getX(int a)
 	{
 		return nodes[a][0];
 	}
 
 	/**
 	 * @param a
 	 * @return
 	 */
 	public double getY(int a)
 	{
 		return nodes[a][1];
 	}
 
 }
