 package graph;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * An imlplementation of a generic graph
  *
  * @author <a ref ="zianfanti@gmail.com"> Zian Fanti<a/>
  */
 public class Graph {
 
     /**
      * The graph implementation
      */
     private HashMap<Vertex, ArrayList<Edge>> graph;
 
     /**
      * Default constructor
      */
     public Graph() {
         this.graph = new HashMap<Vertex, ArrayList<Edge>>();
     }
 
     /**
      * creates a graph with a specified number of vertexes
      *
      * @param numOfVertexex total amout of vertex
      */
     public Graph(int numOfVertexex) {
         if (numOfVertexex < 0) {
             throw new IllegalArgumentException("Number of vertices must be nonnegative");
         }
         this.graph = new HashMap<Vertex, ArrayList<Edge>>(numOfVertexex);
     }
 
     /**
      * Create a graph with some vertxes and edges. The vertexes of the edges
      * must be contained in the vertexes set.
      *
      * @param vertexes an array of vertex
      * @param edges an array of edges
      */
     public Graph(Vertex[] vertexes, Edge[] edges) {
         this.graph = new HashMap<Vertex, ArrayList<Edge>>(vertexes.length);
 
         for (int i = 0; i < vertexes.length; i++) {
             ArrayList<Edge> vertexEdges = new ArrayList<Edge>();
             graph.put(vertexes[i], vertexEdges);
         }
 
         for (int i = 0; i < edges.length; i++) {
             Edge edge = edges[i];
             Vertex source = edge.getSource();
             ArrayList<Edge> tempEdges = graph.get((Vertex) source);
             tempEdges.add(edge);
             graph.put(source, tempEdges);
         }
     }
 
     /**
      * Adds a vertex connected by some edges to the graph
      *
      * @param vertex the new vertex
      * @param edges an ArrayList of the <code>Edge</code>s that conect this
      * vertex to the graph
      */
     public void addConnectedVertex(Vertex vertex, ArrayList<Edge> edges) {
         graph.put(vertex, edges);
     }
 
     /**
      * Add a disconnected vertex to the graph
      */
     public void addVertex(Vertex vertex) {
         ArrayList<Edge> edges = new ArrayList<Edge>();
         graph.put(vertex, edges);
     }
 
     /**
      * Add an
      * <code>Edge</code> to the graph.
      *
      * @param edge
      */
     public void addEdge(Edge edge) {
         Vertex source = edge.getSource();
         ArrayList<Edge> edges = graph.get((Vertex) source);
         edges.add(edge);
         graph.put(source, edges);
     }
 
     /**
      * Get the number of vertexes
      *
      * @return
      */
     public int numOfVertexes() {
         return graph.size();
     }
 
     /**
      * Compute if this Graph is connected. Use BFS traversing
      *
      * @return
      */
 //    public boolean isConnected() {
 //    }
 
     /**
      * Create a clone of this
      * <code>Graph</code>.
      *
      * @return a new <code>Graph</code> that's identically to this Graph.
      */    
     public Graph duplicate() {
         Graph cloneGraph = new Graph(graph.size());
         for (Vertex vertex : graph.keySet()) {  
             Vertex v = new Vertex(vertex);
             cloneGraph.addConnectedVertex(v, (ArrayList<Edge>) graph.get(vertex));
         }
         return cloneGraph;        
     }
 
     /**
      * Build a string representation of the adjency matrix, corresponding of
      * this graph.
      *
      * @return a string
      */
     @Override
     public String toString() {
         String graphString = "";
         Set vertexes = graph.keySet();
         Iterator vertexIterator = vertexes.iterator();
 
         while (vertexIterator.hasNext()) {
             Vertex v = (Vertex) vertexIterator.next();
             graphString += (v.getName() + " -> [");
             ArrayList<Edge> edges = graph.get((Vertex) v);
             for (int i = 0; i < edges.size(); i++) {
                 graphString += (i == 0) ? "" : ", ";
                 Edge e = edges.get(i);
                 graphString += ("(" + e.getTarget().getName() + ", "
                         + e.getWeight() + ")");
             }
             graphString += ("] \n");
         }
 
         return graphString;
     }
 }
