 /* WUGraph.java */
 
 package graph;
 import hw5.list.*;
 import dict.*;
 import java.util.Hashtable;
 
 /**
  * The WUGraph class represents a weighted, undirected graph.  Self-edges are
  * permitted.
  */
 
 public class WUGraph {
     private DList vertexList;
     private int numberOfVertexes;
     private HashTableChained edgeHashTable;
     private HashTableChained vertexHashTable;
     private int numberOfEdges;
     
   /**
    * WUGraph() constructs a graph having no vertices or edges.
    *
    * Running time:  O(1).
    */
   	
     public WUGraph() {
     	edgeHashTable = new HashTableChained();
     	vertexHashTable = new HashTableChained();
 	vertexList = new DList();
     	numberOfEdges = 0;	
     	numberOfVertexes = 0;	
     }
 
   /**
    * vertexCount() returns the number of vertices in the graph.
    *
    * Running time:  O(1).
    */
   public int vertexCount(){
 	  return numberOfVertexes;
   }
 
   /**
    * edgeCount() returns the number of edges in the graph.
    *
    * Running time:  O(1).
    */
 
     public int edgeCount() {
 	return numberOfEdges;
     }
 
 
   /**
    * getVertices() returns an array containing all the objects that serve
    * as vertices of the graph.  The array's length is exactly equal to the
    * number of vertices.  If the graph has no vertices, the array has length
    * zero.
    *
    * (NOTE:  Do not return any internal data structure you use to represent
    * vertices!  Return only the same objects that were provided by the
    * calling application in calls to addVertex().)
    *
    * Running time:  O(|V|).
    */
   public Object[] getVertices(){
       Object[] result;
       result = new Object[numberOfVertexes];
       try{
 	  DListNode tracker = (DListNode)vertexList.front();
 	  for(int n = 0; n<numberOfVertexes; n++){
 	      result[n] = tracker.item();
 	      tracker = (DListNode)tracker.next();
 	  }
       }catch(InvalidNodeException a){
 	  System.err.println(a);
       }
       return result;
   }
 
   /**
    * addVertex() adds a vertex (with no incident edges) to the graph.  The
    * vertex's "name" is the object provided as the parameter "vertex".
    * If this object is already a vertex of the graph, the graph is unchanged.
    *
    * Running time:  O(1).
    */
   public void addVertex(Object vertex){
 	  if(vertexHashTable.find(vertex) == null) {
 	      InternalVertex iVertex = new InternalVertex(vertex);
 	      Entry a = vertexHashTable.insert(vertex, iVertex); 
 	      //a is never used, only there because the hashtable insert method returns an entry
 	      numberOfVertexes++;
 	      vertexList.insertBack(vertex); //used in getVertices() only
 	  }	
   }
     
   /**
    * removeVertex() removes a vertex from the graph.  All edges incident on the
    * deleted vertex are removed as well.  If the parameter "vertex" does not
    * represent a vertex of the graph, the graph is unchanged.
    *
    * Running time:  O(d), where d is the degree of "vertex".
    */
   public void removeVertex(Object vertex){
       Entry a = vertexHashTable.remove(vertex);
       if(a != null){
     	  numberOfVertexes--;
     	  
     	  //removing vertex from vertexList
     	  DListNode tracker = (DListNode) vertexList.front();
     	  for(int n=0; n<vertexList.size; n++){
 	      try{
 		  if((tracker.item()).equals(vertex)){
 		      tracker.remove();
 		      vertexList.size --;
 		  }
 		  tracker = (DListNode) tracker.next();
 	      }catch(InvalidNodeException error){
 	      }
     	  }
     	  
     	  //removing edges from adjacency lists
     	  if(a.value() != null){
 	      InternalVertex iVertex = (InternalVertex) a.value();
 	      DList adjacencyList = iVertex.getAdjacencyList();
 	      DListNode edgeTracker = (DListNode) adjacencyList.front();
 	      for(int n = 0; n < iVertex.getAdjacencyListSize(); n++){
 		  try{
 		      Edge e1 = (Edge) edgeTracker.item();
 		      if(e1.isSelfEdge()){
 			  edgeTracker.remove();
			  e1.getHalfEdge().node.remove();
 			  edgeHashTable.remove(new VertexPair(vertex,vertex));
 			  numberOfEdges --;
 		      }else{
 			  Edge e2 = e1.getHalfEdge();
 			  edgeTracker.remove();
 			  e2.node.remove();
 			  numberOfEdges --;
 			  Object e2start = e2.getStart();
 			  Entry b = vertexHashTable.find(e2start);
 			  InternalVertex iVertex2 = (InternalVertex) b.value();
 			  DList adjacencyList2 = iVertex2.getAdjacencyList();
 			  DListNode edgeTracker2 = (DListNode) adjacencyList2.front();
 			  for(int m = 0; m<iVertex2.getAdjacencyListSize(); m++){
 			      try{
 				  Edge e3 = (Edge) edgeTracker.item();
 				  if(e3.equals(e2)){
 				      edgeTracker2.remove();
 				      e3.getHalfEdge().node.remove();
 				      edgeHashTable.remove(new VertexPair(e3.vert1,e3.vert2));
 				      numberOfEdges --;
 				      break;
 				  }
 				  edgeTracker2 = (DListNode) edgeTracker2.next();
 			      }catch(InvalidNodeException error){
 			      }
 			  }
 		      }
 		      edgeTracker = (DListNode) edgeTracker.next();
 		  }catch(InvalidNodeException error){
 		  }
 	      }
     	  }
       }
   }
 
   /**
    * isVertex() returns true if the parameter "vertex" represents a vertex of
    * the graph.
    *
    * Running time:  O(1).
    */
   public boolean isVertex(Object vertex){
 	return vertexHashTable.find(vertex) != null;
   }
   /**
    * degree() returns the degree of a vertex.  Self-edges add only one to the
    * degree of a vertex.  If the parameter "vertex" doesn't represent a vertex
    * of the graph, zero is returned.
    *
    * Running time:  O(1).
    */
   public int degree(Object vertex){
   		Entry a = vertexHashTable.find(vertex);
   		int result = 0;
   		if(a != null){
   			InternalVertex iv = (InternalVertex) a.value();
   			result = iv.getAdjacencyListSize();
   		}
   		return result;
   }
   /**
    * getNeighbors() returns a new Neighbors object referencing two arrays.  The
    * Neighbors.neighborList array contains each object that is connected to the
    * input object by an edge.  The Neighbors.weightList array contains the
    * weights of the corresponding edges.  The length of both arrays is equal to
    * the number of edges incident on the input vertex.  If the vertex has
    * degree zero, or if the parameter "vertex" does not represent a vertex of
    * the graph, null is returned (instead of a Neighbors object).
    *
    * The returned Neighbors object, and the two arrays, are both newly created.
    * No previously existing Neighbors object or array is changed.
    *
    * (NOTE:  In the neighborList array, do not return any internal data
    * structure you use to represent vertices!  Return only the same objects
    * that were provided by the calling application in calls to addVertex().)
    *
    * Running time:  O(d), where d is the degree of "vertex".
    */
   public Neighbors getNeighbors(Object vertex){
       Entry a = vertexHashTable.find(vertex);
       InternalVertex iv = (InternalVertex) a.value();
       DList adjacencyList = iv.getAdjacencyList();
       if(iv.getAdjacencyListSize() == 0) { //nothing adjacent, no neighbors
 	  return null;
       }
       DListNode edgeTracker = (DListNode) adjacencyList.front();
       Object[] neighborList = new Object[iv.getAdjacencyListSize()];
       int[] weightList = new int[iv.getAdjacencyListSize()];
       for(int n = 0; n<iv.getAdjacencyListSize(); n++){
 	  try{
 	      Edge edge1 = (Edge) edgeTracker.item();
	      neighborList[n] = edge1;
 	      VertexPair vpair = new VertexPair(edge1.getStart(), edge1.getEnd());
 	      Entry b = edgeHashTable.find(vpair);
 	      int weight1 = ((Edge)b.value()).getWeight();
 	      weightList[n] = weight1;
 	  }catch(InvalidNodeException error){
 	  }
       }
       Neighbors result = new Neighbors(neighborList, weightList);
       return result;
   }
   /**
    * addEdge() adds an edge (u, v) to the graph.  If either of the parameters
    * u and v does not represent a vertex of the graph, the graph is unchanged.
    * The edge is assigned a weight of "weight".  If the edge is already
    * contained in the graph, the weight is updated to reflect the new value.
    * Self-edges (where u == v) are allowed.
    *
    * Running time:  O(1).
    */
 
     public void addEdge(Object u, Object v, int weight) {
 	if(vertexHashTable.find(u) != null && vertexHashTable.find(v) != null) {
 	    VertexPair vPair = new VertexPair(u,v);
 	    Entry old = edgeHashTable.remove(vPair);
 	    Edge edge1 = new Edge(u,v,weight);
 	    Edge edge2 = new Edge(v,u,weight);
 	    edge1.changeHalfEdge(edge2);
 	    edge2.changeHalfEdge(edge1);
 	    Edge oldEdge = null;
 	    if(old != null) {
 		numberOfEdges--;
 		oldEdge = (Edge)old.value();
 		edge1 = oldEdge;
 		edge1.weight = weight;
 		edge2 = edge1.getHalfEdge();
 	    }
 	    edgeHashTable.insert(vPair,edge1);
 	    numberOfEdges++;
 
 	    //update adjacency list of the vertices
 	    Entry uEntry = vertexHashTable.remove(u);
 	    Entry vEntry = vertexHashTable.remove(v);
 	    InternalVertex uIV = new InternalVertex(u);
 	    InternalVertex vIV = new InternalVertex(v);
 	    
 	    if(uEntry != null) {
 		uIV = (InternalVertex)uEntry.value();
 		uIV.adjacencyListInsert(edge1);
 		vertexHashTable.insert(u,uIV);
 	    }
 	    if(vEntry != null) {
 		vIV = (InternalVertex)vEntry.value();
 		vIV.adjacencyListInsert(edge2);
 		vertexHashTable.insert(v,vIV);
 	    }
 	}
     }
 
   /**
    * removeEdge() removes an edge (u, v) from the graph.  If either of the
    * parameters u and v does not represent a vertex of the graph, the graph
    * is unchanged.  If (u, v) is not an edge of the graph, the graph is
    * unchanged.
    *
    * Running time:  O(1).
    */
 
     public void removeEdge(Object u, Object v) {
 	if(vertexHashTable.find(u) != null && vertexHashTable.find(v) != null) {
 	    VertexPair vPair = new VertexPair(u,v);
 	    Entry old = edgeHashTable.remove(vPair);
 	    Edge oldEdge = null;
 	    if(old != null) {
 		numberOfEdges--;
 		oldEdge = (Edge)old.value();
 	    }
 	    try {
 		if(oldEdge != null && oldEdge.node != null) {
 		    oldEdge.node.remove();
 		}
 		if(oldEdge != null && oldEdge.getHalfEdge() != null && oldEdge.getHalfEdge().node != null) {
 		    oldEdge.getHalfEdge().node.remove();
 		}
 	    }
 	    catch(InvalidNodeException e) {
 		System.err.println(e);
 	    }
 	}
     }
 
   /**
    * isEdge() returns true if (u, v) is an edge of the graph.  Returns false
    * if (u, v) is not an edge (including the case where either of the
    * parameters u and v does not represent a vertex of the graph).
    *
    * Running time:  O(1).
    */
 
     public boolean isEdge(Object u, Object v) {
 	VertexPair vPair = new VertexPair(u,v);
 	return edgeHashTable.find(vPair) != null;
     }
   /**
    * weight() returns the weight of (u, v).  Returns zero if (u, v) is not
    * an edge (including the case where either of the parameters u and v does
    * not represent a vertex of the graph).
    *
    * (NOTE:  A well-behaved application should try to avoid calling this
    * method for an edge that is not in the graph, and should certainly not
    * treat the result as if it actually represents an edge with weight zero.
    * However, some sort of default response is necessary for missing edges,
    * so we return zero.  An exception would be more appropriate, but
    * also more annoying.)
    *
    * Running time:  O(1).
    */
 
     public int weight(Object u, Object v) {
 	VertexPair vPair = new VertexPair(u,v);
 	Entry entry = edgeHashTable.find(vPair);
 	if(entry == null || entry.value() == null) {
 	    return 0;
 	}
 	else {
 	    return ((Edge)entry.value()).getWeight();
 	}
     }
 }
