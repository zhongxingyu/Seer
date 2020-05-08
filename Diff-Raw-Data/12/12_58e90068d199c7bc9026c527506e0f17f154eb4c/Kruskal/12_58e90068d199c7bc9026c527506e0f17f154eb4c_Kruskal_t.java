 /* Kruskal.java */
 
 import java.util.Hashtable;
 import dict.*;
 import graph.*;
 import set.*;
 import list.*;
 
 /**
  * The Kruskal class contains the method minSpanTree(), which implements
  * Kruskal's algorithm for computing a minimum spanning tree of a graph.
  */
 
 public class Kruskal {
 
   /**
    * minSpanTree() returns a WUGraph that represents the minimum spanning tree
    * of the WUGraph g.  The original WUGraph g is NOT changed.
    */
   public static WUGraph minSpanTree(WUGraph g){
 	// - Make new empty graph
 	// - Get vertices, add them all to the new graph
 	// - Use getNeighbors() to obtain all neighbors, get all edges from them
 	//	-- Store edges in a list, probably a singly linked list
 	// - Sort the edges with a sorting algorithm
 	// - Use disjoint sets to find edges of minimum spanning tree
 	//
 	// Probably should read G&T's section Kruskal to get a better idea
 	// of what to do
 	
 
 	// Make a new empty graph, T, for the minimum spanning tree
 	WUGraph T = new WUGraph();
 
         // Create the list of edges from the original graph
 	DList edges = new DList();	
 
 	// Get the vertices from the original graph
 	Object[] vertices = g.getVertices();
 
 	// Create a dictionary to mark vertices as visited.
 	// Since the vertices don't necessarily have any sort
 	// of visited field, at least one that we know about,
 	// we need to keep track of visited vertices with an
 	// external data structure. To keep the cost of lookup
 	// as low as possible, we need a hash table. While the
 	// space usage may be high, it is the best option for
 	// speed, and after adding all of the edges, the reference
 	// is immediately set to null to speed up garbage collection.
 	Dictionary visited = new HashTableChained(); 
 
 	for(int i = 0; i < vertices.length; i++){
 		// Add the vertices from the original graph into T
 		T.addVertex(vertices[i]);
 
 		// Add the edge to the list of edges
 		Neighbors n = getNeighbors(vertices[i]);
 		for(int j = 0; j < n.neighborList.length; j++){
 			// If the edge is not already in the list,
 			// add it to the list of edges (if the vertex
 			// opposite to this in the current edge has
 			// already been visited, the edge is already
 			// added).
 			if(visited.find(n.neighborList[j])){
 				edges.insertBack(new Edge(vertices[i], n.neighborList[j], n.weightList[j]));
 			}
 		}
 
 		// Mark this vertex as visited (to prevent duplicate edges
 		// in the list of all of the edges)
 		visited.insert(vertices[i], true);
 	}
 
         // Assign the dictionary to null, since we don't need it
 	// anymore and to speed up garbage collection
 	visited = null;
 
 
 	// Sort edges using radix sort
 
 	
 
 	// Use disjoint sets to find edges of minimum spanning tree
   }
 
 
   /*
  *  sortWeights() sorts a DList d of edges by weights using quicksort.
   *  The passed in list IS ALTERED.
   */
  public static void sortWeights(DList d){
 
 	
   }
 
   
 }
