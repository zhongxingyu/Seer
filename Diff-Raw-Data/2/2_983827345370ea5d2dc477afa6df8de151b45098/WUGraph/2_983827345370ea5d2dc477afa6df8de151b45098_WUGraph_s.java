 /* WUGraph.java */
 
 package graph;
 
 import dict.Hashtable;
 import Constants.Constants;
 import DList.*;
 
 /**
  * The WUGraph class represents a weighted, undirected graph.  Self-edges are
  * permitted.
  */
 
 public class WUGraph {
   Hashtable vertexTable;
   VertexList adjacencyList;
   Hashtable edgeTable;
 
   int numEdges;
 
 
 
   /**
    * WUGraph() constructs a graph having no vertices or edges.
    *
    * Running time:  O(1).
    */
   public WUGraph(){
     vertexTable = new Hashtable(Constants.HASH_INIT_SIZE);
     edgeTable = new Hashtable(Constants.HASH_INIT_SIZE);
     adjacencyList = new VertexList();
   }
 
   /**
    * vertexCount() returns the number of vertices in the graph.
    *
    * Running time:  O(1).
    */
   public int vertexCount(){
     return adjacencyList.length();
   }
 
   /**
    * edgeCount() returns the number of edges in the graph.
    *
    * Running time:  O(1).
    */
   public int edgeCount(){
     return numEdges;
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
     Object[] out = new Object[vertexCount()];
     int currIndex=0;
     for (Vertex i : adjacencyList){
       out[currIndex]=i.item;
       currIndex++;
     }
     return out;
   }
 
   /**
    * addVertex() adds a vertex (with no incident edges) to the graph.  The
    * vertex's "name" is the object provided as the parameter "vertex".
    * If this object is already a vertex of the graph, the graph is unchanged.
    *
    * Running time:  O(1).
    */
   public void addVertex(Object vertex){
     adjacencyList.push(new Vertex(vertex));
     vertexTable.insert(vertex,adjacencyList.front());
   }
 
   /**
    * removeVertex() removes a vertex from the graph.  All edges incident on the
    * deleted vertex are removed as well.  If the parameter "vertex" does not
    * represent a vertex of the graph, the graph is unchanged.
    *
    * Running time:  O(d), where d is the degree of "vertex".
    */
   public void removeVertex(Object vertex){
     //Find zee vertex
     DListNode<Vertex> dead_node = (DListNode<Vertex>) vertexTable.find(vertex);
     DListNode<Edge> curr = dead_node.item().edges.front();
     while (curr!=null){
       curr.item().partner.container.remove();
       curr.remove();
       curr=curr.next();
     }
     dead_node.remove();
     vertexTable.remove(vertex);
   }
 
   /**
    * isVertex() returns true if the parameter "vertex" represents a vertex of
    * the graph.
    *
    * Running time:  O(1).
    */
   public boolean isVertex(Object vertex){
     return (vertexTable.find(vertex)!=null);
   }
 
   /**
    * degree() returns the degree of a vertex.  Self-edges add only one to the
    * degree of a vertex.  If the parameter "vertex" doesn't represent a vertex
    * of the graph, zero is returned.
    *
    * Running time:  O(1).
    */
   public int degree(Object vertex){
     return isVertex(vertex) ? ((Vertex)vertexTable.find(vertex)).edges.length() : 0;
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
     Neighbors neighbors = new Neighbors();
     if(vertexTable.find(vertex) == null){
       return null;
     }
     Vertex target = (Vertex) vertexTable.find(vertex);
     EdgeList edges = target.edges;
     if(edges.length() == 0){
       return null;
     }
     neighbors.neighborList = new Object[edges.length()];
     neighbors.weightList = new int[edges.length()];
     int index = 0;
     for(Edge e : edges){
       Vertex endpt1 = e.v1;
       Vertex endpt2 = e.v2;
       if(endpt1 == endpt2){
         neighbors.neighborList[index] = endpt1.item;
       }else if(endpt1 == target){
         neighbors.neighborList[index] = endpt2.item;  
       }else{
         neighbors.neighborList[index] = endpt1.item;
       }
       neighbors.weightList[index] = e.weight;
       index++;
     }
     return neighbors;
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
   public void addEdge(Object u, Object v, int weight){
     if(vertexTable.find(u) == null || vertexTable.find(v) == null){
       return;
     }
     Vertex endpt1 = (Vertex) vertexTable.find(u); //pointless watermelon ()
     Vertex endpt2 = (Vertex) vertexTable.find(v);
 
     VertexPair key = new VertexPair(u,v);
     Edge newEdge = new Edge(endpt1,endpt2,weight,null);
     Edge newEdgeClone = new Edge(endpt1,endpt2,weight,null);
 
     newEdge.partner = newEdgeClone; //pointless comment
     newEdgeClone.partner = newEdge;
 
 
   
     //insert into edgeLists
     endpt1.edges.push(newEdge); 
     newEdge.container = endpt1.edges.front();
     if(endpt1 != endpt2){
       endpt2.edges.push(newEdgeClone);
       newEdgeClone.container = endpt2.edges.front();
     }
 
     edgeTable.insert(key,newEdge.container); //insert into $$$cashtable$$$
   }
 
   /**
    * removeEdge() removes an edge (u, v) from the graph.  If either of the
    * parameters u and v does not represent a vertex of the graph, the graph
    * is unchanged.  If (u, v) is not an edge of the graph, the graph is
    * unchanged.
    *
    * Running time:  O(1).
    */
   public void removeEdge(Object u, Object v){
     VertexPair key = new VertexPair(u,v);
     if(edgeTable.find(key) == null){
       return;
     }
     DListNode<Edge> node = (DListNode<Edge>) edgeTable.find(key);
     node.item().partner.container.remove();
     node.remove();
     edgeTable.remove(new VertexPair(u,v));
     
   }
 
   /**
    * isEdge() returns true if (u, v) is an edge of the graph.  Returns false
    * if (u, v) is not an edge (including the case where either of the
    * parameters u and v does not represent a vertex of the graph).
    *
    * Running time:  O(1).
    */
   public boolean isEdge(Object u, Object v){
    return edgeTable.find(new VertexPair(u,v) != null;
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
   public int weight(Object u, Object v){
     return (edgeTable.find(new VertexPair(u,v)) != null ? ((DListNode<Edge>) edgeTable.find(new VertexPair(u,v))).item().weight : 0 ) ;
   }
 
 }
