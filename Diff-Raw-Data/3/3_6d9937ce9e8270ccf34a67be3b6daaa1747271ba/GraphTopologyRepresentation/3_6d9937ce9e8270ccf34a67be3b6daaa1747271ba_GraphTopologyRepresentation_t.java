 package cytoscape.graph.util;
 
 import cytoscape.graph.GraphTopology;
 
 /**
  * This class provides an implementation of <code>GraphTopology</code>
  * whose only purpose is to represent a non-mutable graph topology based on
  * structure defined in arrays of integers.
  * Methods on an instance of this class have no hooks into outside code.
  **/
 public class GraphTopologyRepresentation implements GraphTopology
 {
 
   private final int m_numNodes;
   private final int[] m_directedEdgeSourceNodeIndices;
   private final int[] m_directedEdgeTargetNodeIndices;
   private final int[] m_undirectedEdgeNode0Indices;
   private final int[] m_undirectedEdgeNode1Indices;
   
   /**
    * Copies are made of all the array input parameters; modifying
    * the arrays after this constructor is called will have no effect on
    * an instance of this class.  Furthermore, an instance of this class
    * never modifies any of the arrays passed into the constructor.<p>
    * The methods on this class are <code>final</code> and this class does
    * not expose any mutable functionality; member variables are
    * declared <code>private</code>.  Therefore, it is guaranteed
    * that an instance of a subclass of
    * <code>GraphTopologyRepresentation</code>
    * represents a non-mutable graph topology.
    *
    * @param numNodes
    *   <blockquote>the number of nodes in this graph, as defined by
    *   <code>getNumNodes()</code>; nodes are referenced by their indices, which
    *   lie in the interval <nobr><code>[0, getNumNodes() - 1]</code></nobr>.
    *   </blockquote>
    * @param directedEdgeSourceNodeIndices
    *   <blockquote>an array of length equal to
    *   the number of directed edges in this graph;
    *   <code>directedEdgeSourceNodeIndices[edgeIndex]</code> defines
    *   the index of source node belonging to a directed edge at edge index
    *   <code>edgeIndex</code> (the edge index of any directed edge is less than
    *   the edge index of any undirected edge in this construction).
    *   </blockquote>
    * @param directedEdgeTargetNodeIndices
    *   <blockquote>an array of length equal to
    *   the number of directed edges in this graph;
    *   <code>directedEdgeTargetNodeIndices[edgeIndex]</code> defines
    *   the index of target node belonging to a directed edge at edge index
    *   <code>edgeIndex</code> (the edge index of any directed edge is less than
    *   the edge index of any undirected edge in this construction).
    *   </blockquote>
    * @param undirectedEdgeNode0Indices
    *   <blockquote>an array of length equal to
    *   the number of undirected edges in this graph;
    *   <nobr><code>undirectedEdgeNode0Indices[edgeIndex - numDirectedEdges]</code></nobr>
    *   defines the index of node 0 belonging to an undirected edge at edge
    *   index <code>edgeIndex</code>, where <code>numDirectedEdges</code> is
    *   the number of directed edges in this graph
    *   (the edge index of any directed edge
    *   is less than the edge index of any undirected edge in this
    *   construction).</blockquote>
    * @param undirectedEdgeNode1Indices
    *   <blockquote>an array of length equal to
    *   the number of undirected edges in this graph;
    *   <nobr><code>undirectedEdgeNode1Indices[edgeIndex - numDirectedEdges]</code></nobr>
    *   defines the index of node 1 belonging to an undirected edge at edge
    *   index <code>edgeIndex</code>, where <code>numDirectedEdges</code> is
    *   the number of directed edges in this graph
    *   (the edge index of any directed edge
    *   is less than the edge index of any undirected edge in this
    *   construction).</blockquote>
    * @exception IllegalArgumentException if parameters are passed which
    *   don't agree with a possible graph definition.
    **/
   public GraphTopologyRepresentation(int numNodes,
                                      int[] directedEdgeSourceNodeIndices,
                                      int[] directedEdgeTargetNodeIndices,
                                      int[] undirectedEdgeNode0Indices,
                                      int[] undirectedEdgeNode1Indices)
   {
     // Preliminary parameter checking.
     if (directedEdgeSourceNodeIndices == null)
       directedEdgeSourceNodeIndices = new int[0];
     if (directedEdgeTargetNodeIndices == null)
       directedEdgeTargetNodeIndices = new int[0];
     if (undirectedEdgeNode0Indices == null)
       undirectedEdgeNode0Indices = new int[0];
     if (undirectedEdgeNode1Indices == null)
       undirectedEdgeNode1Indices = new int[0];
 
     // Real parameter checking.  Set member variables.
     if (numNodes < 0) throw new IllegalArgumentException
                         ("cannot have a negative number of nodes");
     m_numNodes = numNodes;
     if (directedEdgeSourceNodeIndices.length !=
         directedEdgeTargetNodeIndices.length)
       throw new IllegalArgumentException
         ("directed edge node index arrays are not the same length");
     m_directedEdgeSourceNodeIndices =
       new int[directedEdgeSourceNodeIndices.length];
     System.arraycopy(directedEdgeSourceNodeIndices, 0,
                      m_directedEdgeSourceNodeIndices, 0,
                      directedEdgeSourceNodeIndices.length);
     m_directedEdgeTargetNodeIndices =
       new int[directedEdgeTargetNodeIndices.length];
     System.arraycopy(directedEdgeTargetNodeIndices, 0,
                      m_directedEdgeTargetNodeIndices, 0,
                      directedEdgeTargetNodeIndices.length);
     for (int i = 0; i < m_directedEdgeSourceNodeIndices.length; i++) {
       if (m_directedEdgeSourceNodeIndices[i] < 0 ||
           m_directedEdgeSourceNodeIndices[i] >= m_numNodes ||
           m_directedEdgeTargetNodeIndices[i] < 0 ||
           m_directedEdgeTargetNodeIndices[i] >= m_numNodes) {
         throw new IllegalArgumentException
           ("a node index in a directed edge array is not valid"); } }
     if (undirectedEdgeNode0Indices.length !=
         undirectedEdgeNode1Indices.length)
       throw new IllegalArgumentException
         ("undirected edge node index arrays are not the same length");
     m_undirectedEdgeNode0Indices =
       new int[undirectedEdgeNode0Indices.length];
     System.arraycopy(undirectedEdgeNode0Indices, 0,
                      m_undirectedEdgeNode0Indices, 0,
                      undirectedEdgeNode0Indices.length);
     m_undirectedEdgeNode1Indices =
       new int[undirectedEdgeNode1Indices.length];
     System.arraycopy(undirectedEdgeNode1Indices, 0,
                      m_undirectedEdgeNode1Indices, 0,
                      undirectedEdgeNode1Indices.length);
     for (int i = 0; i < m_undirectedEdgeNode0Indices.length; i++) {
       if (m_undirectedEdgeNode0Indices[i] < 0 ||
           m_undirectedEdgeNode0Indices[i] >= m_numNodes ||
           m_undirectedEdgeNode1Indices[i] < 0 ||
           m_undirectedEdgeNode1Indices[i] >= m_numNodes) {
         throw new IllegalArgumentException
           ("a node index in an undirected edge array is not valid"); } }
   }
 
   public final int getNumNodes() { return m_numNodes; }
 
   public final int getNumEdges() {
     return
       m_directedEdgeSourceNodeIndices.length +
       m_undirectedEdgeNode0Indices.length; }
 
   public final boolean isDirectedEdge(int edgeIndex) {
     if (edgeIndex < 0 ||
         edgeIndex >= getNumEdges())
       throw new IndexOutOfBoundsException
         ("edgeIndex is out of bounds - graph has " + getNumEdges() +
          " edges, yet edgeIndex has value " + edgeIndex);
     return edgeIndex < m_directedEdgeSourceNodeIndices.length; }
 
   public final boolean areAllEdgesSimilar() {
     return
       m_directedEdgeSourceNodeIndices.length == 0 ||
       m_undirectedEdgeNode0Indices.length == 0; }
 
   public final int getEdgeNodeIndex(int edgeIndex, boolean sourceNode) {
     if (edgeIndex < 0 ||
         edgeIndex >= getNumEdges())
       throw new IndexOutOfBoundsException
         ("edgeIndex is out of bounds - graph has " + getNumEdges() +
          " edges, yet edgeIndex has value " + edgeIndex);
     if (isDirectedEdge(edgeIndex)) {
       return (sourceNode ? m_directedEdgeSourceNodeIndices :
               m_directedEdgeTargetNodeIndices)[edgeIndex]; }
     else {
       return (sourceNode ? m_undirectedEdgeNode0Indices :
              m_undirectedEdgeNode1Indices)
        [edgeIndex - m_directedEdgeSourceNodeIndices.length]; } }
 
 
 }
