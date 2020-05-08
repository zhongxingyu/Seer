 package graph;
 
 import java.util.ArrayList;
import java.util.LinkedList;
 import java.util.Set;
 import java.util.Stack;
 
 /**
  * Implements the Min Cut - Max Flow Ford-Fulkerson algorithm for graphs. The
  * algorithm needs a directed weighted graph, one vertex with no predecessor
  * called the source and exactly one vertex with no successor called the sink or
  * target.
  *
  * @author <a ref ="zianfanti@gmail.com"> Zian Fanti Gutierrez<a/>
  */
 public class FordFulkerson {
 
     /**
      * The given graph to apply this algorithm
      */
     private Graph graph;
     /**
      * the source vertex for the algorithm
      */
     private Vertex source;
     /**
      * the sink vertex for the algorithm
      */
     private Vertex target;
     /**
      * the maximun flow permited for given graph
      */
     private float maxFlow;
 
     /**
      * Basic constructor to obtain the max flow or min cut on a graph
      *
      * @param graph the directed weighted graph.
      * @param source vertex with no predecessor
      * @param target one vertex with no successor
      */
     public FordFulkerson(Graph graph, Vertex source, Vertex target) {
         this.graph = graph;
         this.source = source;
         this.target = target;
     }
 
     /**
      * Implements the Min Cut - Max Flow Ford-Fulkerson algorithm for graphs.
      * This function sets the maxFlow local field
      *
      * @return an <code>ArrayList</code> with one set of the min cut for this
      * graph.
      */
     public ArrayList<Vertex> maxFlowMinCut() {
         Edge[] path = findPath(source, target);
         this.maxFlow = 0;
 
         while (path != null && path[0].getTarget().equals(target)) {
             // find the minimum edge weight
             float weight = Float.MAX_VALUE;
             for (int i = 0; i < path.length; i++) {
                 float tempWeight = path[i].getWeight();
                 if (tempWeight < weight) {
                     weight = tempWeight;
                 }
             }
 
             maxFlow += weight;
 
             // Subtracting the minimum weight and create the residual edges
             for (int i = 0; i < path.length; i++) {
                 Edge edge = path[i];
                 edge.setWeight(edge.getWeight() - weight);
 
                 Edge residualEdge = graph.getEdge(edge.getTarget(), edge.getSource());
                 if (residualEdge == null) {
                     graph.addEdge(new Edge(edge.getTarget(), edge.getSource(), weight));
                 }
                 else {
                     residualEdge.setWeight(residualEdge.getWeight() + weight);
                 }
             }
 
 
             graph.setUnvisitedGraph();
             path = findPath(source, target);
         }
 
         ArrayList<Vertex> mincut = new ArrayList<Vertex>();
         for (int i = 0; i < path.length; i++) {
             Vertex v = path[i].getTarget();
             mincut.add(v);
 //            System.out.println(v);
         }
         Vertex v = path[path.length - 1].getSource();
         mincut.add(v);
 //        System.out.println(v);
 
         return mincut;
     }
 
     /**
      * Find a path between source and target vertexes into this graph, using the
      * Depth-first search algoritm for traversingthe graph. This function works
      * with directed, weighted graphs.
      *
      * @param source vertex
      * @param target vertex
      * @return an <code>ArrayList</code> containing the path between source and
      * target vertexes as a sequence.
      */
     private Edge[] findPath(Vertex source, Vertex target) {
         Graph tree = new Graph();
 
         boolean pathFound = false;
         Stack<Vertex> S = new Stack<Vertex>();
         S.push(source);
         source.setVisited(true);
 
         // create a tree until target is reached 
         while (!S.empty() && !pathFound) {
             Vertex v = S.pop();
             ArrayList<Edge> E = new ArrayList<Edge>();
 
             // if v has an unvisited neighbour w 
             for (Edge edge : graph.getEdges(v)) {
                 Vertex w = edge.getTarget();
                 if (w.equals(target) && (edge.getWeight() > 0)) {
                     w.setVisited(true);
                     w.setParent(v);
                     E.add(edge);
                     tree.addConnectedVertex(v, E);
                     tree.addVertex(w);
                     pathFound = true;
                     break;
                 }
                 else if (!w.isVisited() && (edge.getWeight() > 0)) {
                     w.setVisited(true);
                     w.setParent(v);
                     E.add(edge);
                     S.push(w);
                 }
             }
             if (!pathFound) {
                 tree.addConnectedVertex(v, E);
             }
         }
 
         if (tree.contains(target)) {
             ArrayList<Edge> sPath = new ArrayList<Edge>();
             Vertex current = target;
             Vertex parent = current.getParent();
             while (parent != null) {
                 Edge e = tree.getEdge(parent, current);
                 sPath.add(e);
                 current = parent;
                 parent = current.getParent();
             }
             
             Edge[] path = new Edge[sPath.size()];
             sPath.toArray(path);
 
             if (path.length > 0) {
                 return path;
             }
             else {
                 return null;
             }
         }
         else {
             Set<Vertex> minCut = tree.getVertexes();
             ArrayList<Edge> edges = new ArrayList<Edge>();
             for (Vertex v : minCut) {
                 ArrayList<Edge> E = tree.getEdges(v);
                 for (Edge e : E) {
                     edges.add(e);
                 }
             }
 
             Edge[] mc = new Edge[edges.size()];
             return edges.toArray(mc);
         }
     }
 
     /**
      * @return the maxFlow
      */
     public float getMaxFlow() {
         return maxFlow;
     }
 }
