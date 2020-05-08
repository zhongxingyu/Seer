 package org.cytoscape.graph;
 
 /**
  * A bare-minimum definition of a graph.
  * (I'm hesitant to let edges be &quot;undirected&quot;; this
  * is a &quot;feature&quot; that people want.)
  */
 public interface Graph
 {
 
   /**
    * Returns the number of nodes in this graph.  In other methods of this
    * interface, nodes are referenced by their index.  Indexes of nodes start
    * at <code>0</code> and end at <nobr><code>getNumNodes() - 1</code></nobr>,
    * inclusive.
    * @return number of nodes in this graph.
    */
   public int getNumNodes();
 
   /**
    * Returns the number of edges in this graph.  In other methods of this
    * interface, edges are referenced by their index.  Indexes of edges start
    * at <code>0</code> and end at <nobr><code>getNumEdges() - 1</code></nobr>,
    * inclusive.
    * @return number of edges in this graph.
    */
   public int getNumEdges();
 
   /**
    * Returns the directedness of edge at index <code>edgeIndex</code>.
    * @param edgeIndex index of edge whose directedness we're seeking.
    * @return <code>true</code> if directed edge, <code>false</code> if
    *   undirected edge.
    * @exception IndexOutOfBoundsException if <code>edgeIndex</code> is not
    *   in the interval <nobr><code>[0, getNumEdges() - 1]</code></nobr>.
    */
   public boolean isDirectedEdge(int edgeIndex);
 
   /**
    * Returns <code>true</code> if and only if edges in this graph are
    * either all directed or all undirected.
    */
   public boolean areAllEdgesSimilar();
 
   /**
    * @param edgeIndex the index of the edge whose end nodes we are seeking.
   * @param sourcenode if <code>true</code>, returns the source node of this
    *   edge; if <code>false</code>, returns the target node of this edge -
    *   for undirected edges &quot;source node&quot; should be interpreted
    *   as &quot;node 0&quot; and &quot;target node&quot; should be
    *   interpreted as &quot;node 1&quot;.
    * @return the index of the node we are asking for.
    * @exception IndexOutOfBoundsException if <code>edgeIndex</code> is not
    *   in the interval <nobr><code>[0, getNumEdges() - 1]</code></nobr>.
    */
   public int getEdgeNode(int edgeIndex, boolean sourceNode);
 
 }
