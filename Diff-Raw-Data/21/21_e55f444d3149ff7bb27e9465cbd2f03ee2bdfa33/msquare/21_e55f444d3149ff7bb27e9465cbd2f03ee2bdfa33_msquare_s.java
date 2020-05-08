 import java.util.*;
 
 public class msquare{
 
 	private static ArrayList<Vertex> g;
 
 	public static void main(String[] args) throws Exception{
 		g = new ArrayList<Vertex>();
 		g = graph.buildGraph();
 
 		int numCells = g.size();
 		int[] row = new int[numCells];
 		int[] col = new int[numCells];
 		
 		for (int i = 0; i < numCells; ++i) 
 		{
 			// initialize the two necessary row/col of our adj matrix M
 			row[i] = 0;
 			
 			// we only have to actually construct one row/col
 			// and then duplicate it, they should be symmetrical
 			// col[i] = 0;
 		}
 
 		int hundred = 100;
 		Vertex v = g.get(hundred); // get vertex 100 from our graph
 
 		int numNeighbors = v.getALSize(); // get the number of edges v has
 		int currNode;
 		for (int i = 0; i < numNeighbors; ++i)  // for all vertex opposite v100, build our row/col of our matrix
 		{
 			currNode = v.getToNode(i);
 			row[currNode] = 1;
 			// col[currNode] = 1;
 		}
 		col = row;
 
 		int msquare = 0;
 		for (int i = 0; i < numCells; ++i) 
 		{
 				msquare += col[i]	* row[i];
 		}
		System.out.println(msquare);
 	}
 
 }
