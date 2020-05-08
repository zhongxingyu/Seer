 package cytoscape.foo;
 
 import cytoscape.Cytoscape;
 import cytoscape.graph.layout.algorithm.MutablePolyEdgeGraphLayout;
 import cytoscape.graph.layout.algorithm.util.MutablePolyEdgeGraphLayoutRepresentation;
 import cytoscape.view.CyNetworkView;
 import giny.model.Edge;
 import giny.view.Bend;
 import giny.view.EdgeView;
 import giny.view.NodeView;
 import java.awt.geom.Point2D;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 /**
  * This class is very temporary.  It is in heavy flux until some other
  * APIs which are not part of core Cytoscape are finalized.
  * And yes, this class does belong in the core.  It's not a plugin, I feel.
  **/
 public final class GraphConverter
 {
 
   // No constructor.
   private GraphConverter() {}
 
   private static final class MyCMutableGraphLayout
     extends MutablePolyEdgeGraphLayoutRepresentation
   {
     // Definiition of m_nodeTranslation:
     // m_nodeTranslation[i] defines, for node at index i in our
     // GraphTopology object, the corresponding NodeView in Giny.
     final NodeView[] m_nodeTranslation;
 
     // Definition of m_edgeTranslation:
     // m_edgeTranslation[i] defines, for edge at index i in our
     // GraphTopology object, the corresponding EdgeView in Giny.
     final EdgeView[] m_edgeTranslation;
 
     final double m_xOff;
     final double m_yOff;
 
     private MyCMutableGraphLayout(int numNodes,
                                   int[] directedEdgeSourceNodeIndices,
                                   int[] directedEdgeTargetNodeIndices,
                                   int[] undirectedEdgeNode0Indices,
                                   int[] undirectedEdgeNode1Indices,
                                   double width,
                                   double height,
                                   double[] nodeXPositions,
                                   double[] nodeYPositions,
                                   boolean[] isMovableNode,
                                   double[][] edgeAnchorXPositions,
                                   double[][] edgeAnchorYPositions,
                                   NodeView[] nodeTranslation,
                                   EdgeView[] edgeTranslation,
                                   double xOff,
                                   double yOff)
     {
       super(numNodes,
             directedEdgeSourceNodeIndices,
             directedEdgeTargetNodeIndices,
             undirectedEdgeNode0Indices,
             undirectedEdgeNode1Indices,
             width,
             height,
             nodeXPositions,
             nodeYPositions,
             isMovableNode,
             edgeAnchorXPositions,
             edgeAnchorYPositions);
       m_nodeTranslation = nodeTranslation;
       m_edgeTranslation = edgeTranslation;
       m_xOff = xOff;
       m_yOff = yOff;
     }
   }
 
   private static class MyRMutableGraphLayout
     implements MutablePolyEdgeGraphLayout
   {
     // Definiition of m_nodeTranslation:
     // m_nodeTranslation[i] defines, for node at index i in our
     // GraphTopology object, the corresponding NodeView in Giny.
     private final NodeView[] m_nodeTranslation;
 
     // Definition of m_edgeTranslation:
     // m_edgeTranslation[i] defines, for edge at index i in our
     // GraphTopology object, the corresponding EdgeView in Giny.
     private final EdgeView[] m_edgeTranslation;
 
     // Definiton of m_nodeIndexTranslation:
     // Both keys and values of this hashtable are java.lang.Integer objects.
     // There are exactly m_nodeTranslation.length keys in this hashtable.
     // Key-to-value mappings define index-of-node-in-Giny to
     // index-of-node-in-GraphTopology mappings.  When I say
     // "index-of-node-in-Giny", I mean giny.model.Node.getRootGraphIndex().
     private final Hashtable m_nodeIndexTranslation;
 
     private final double m_xOff;
     private final double m_yOff;
     private final double m_width;
     private final double m_height;
     private final boolean m_allMovable;
 
     private MyRMutableGraphLayout(NodeView[] nodeTranslation,
                                   EdgeView[] edgeTranslation,
                                   Hashtable nodeIndexTranslation,
                                   double width,
                                   double height,
                                   double xOff,
                                   double yOff,
                                   boolean allMovable)
     {
       m_nodeTranslation = nodeTranslation;
       m_edgeTranslation = edgeTranslation;
       m_nodeIndexTranslation = nodeIndexTranslation;
       m_xOff = xOff;
       m_yOff = yOff;
       m_width = width;
       m_height = height;
       m_allMovable = allMovable;
     }
 
     public int getNumNodes() { return m_nodeTranslation.length; }
     public int getNumEdges() { return m_edgeTranslation.length; }
     public boolean isDirectedEdge(int edgeIndex) {
       return m_edgeTranslation[edgeIndex].getEdge().isDirected(); }
     public int getEdgeNodeIndex(int edgeIndex, boolean sourceNode) {
       Edge edge = m_edgeTranslation[edgeIndex].getEdge();
       int ginyNInx;
       if (sourceNode) ginyNInx = edge.getSource().getRootGraphIndex();
       else ginyNInx = edge.getTarget().getRootGraphIndex();
       Object nativeNodeIndex =
         m_nodeIndexTranslation.get(new Integer(ginyNInx));
       return ((Integer) nativeNodeIndex).intValue(); }
     public double getMaxWidth() { return m_width; }
     public double getMaxHeight() { return m_height; }
     public double getNodePosition(int nodeIndex, boolean xPosition) {
       NodeView node = m_nodeTranslation[nodeIndex];
       if (xPosition) return node.getXPosition() - m_xOff;
       else return node.getYPosition() - m_yOff; }
     public boolean isMovableNode(int nodeIndex) {
       NodeView node = m_nodeTranslation[nodeIndex];
       if (m_allMovable) return true;
       else return node.isSelected(); }
     private void checkPosition(double xPos, double yPos) {
       if (xPos < 0.0d || xPos > getMaxWidth())
         throw new IllegalArgumentException("X position is out of bounds");
       if (yPos < 0.0d || yPos > getMaxHeight())
         throw new IllegalArgumentException("Y position is out of bounds"); }
     public void setNodePosition(int nodeIndex, double xPos, double yPos) {
       NodeView node = m_nodeTranslation[nodeIndex];
       checkPosition(xPos, yPos);
       if (!isMovableNode(nodeIndex))
         throw new UnsupportedOperationException
           ("node at index " + nodeIndex + " is not movable");
       node.setOffset(xPos + m_xOff, yPos + m_yOff); }
     public int getNumAnchors(int edgeIndex) {
       return m_edgeTranslation[edgeIndex].getBend().getHandles().size(); }
     public double getAnchorPosition(int edgeIndex, int anchorIndex,
                                     boolean xPosition) {
       Point2D point = (Point2D)
         m_edgeTranslation[edgeIndex].getBend().getHandles().get(anchorIndex);
       return (xPosition ? (point.getX() - m_xOff): (point.getY() - m_yOff)); }
     private void checkAnchorIndexBounds(int edgeIndex, int anchorIndex,
                                         boolean create) {
       final int numAnchors = getNumAnchors(edgeIndex) + (create ? 0 : -1);
       if (anchorIndex < 0 || anchorIndex > numAnchors)
         throw new IndexOutOfBoundsException("anchor index out of bounds"); }
     private void checkMutableAnchor(int edgeIndex) {
       final int srcNode = getEdgeNodeIndex(edgeIndex, true);
       final int trgNode = getEdgeNodeIndex(edgeIndex, false);
       if ((!isMovableNode(srcNode)) && (!isMovableNode(trgNode)))
         throw new UnsupportedOperationException
           ("anchors at specified edge cannot be changed"); }
     public void deleteAnchor(int edgeIndex, int anchorIndex) {
       checkAnchorIndexBounds(edgeIndex, anchorIndex, false);
       checkMutableAnchor(edgeIndex);
       m_edgeTranslation[edgeIndex].getBend().removeHandle(anchorIndex); }
     public void createAnchor(int edgeIndex, int anchorIndex) {
       checkAnchorIndexBounds(edgeIndex, anchorIndex, true);
       checkMutableAnchor(edgeIndex);
       m_edgeTranslation[edgeIndex].getBend().addHandle
         (anchorIndex, new Point2D.Double());
       Point2D src =
         ((anchorIndex == 0) ?
          (new Point2D.Double
           (getNodePosition(getEdgeNodeIndex(edgeIndex, true), true),
            getNodePosition(getEdgeNodeIndex(edgeIndex, true), false))) :
          (new Point2D.Double
           (getAnchorPosition(edgeIndex, anchorIndex - 1, true),
            getAnchorPosition(edgeIndex, anchorIndex - 1, false))));
       Point2D trg =
         ((anchorIndex == getNumAnchors(edgeIndex) - 1) ?
          (new Point2D.Double
           (getNodePosition(getEdgeNodeIndex(edgeIndex, false), true),
            getNodePosition(getEdgeNodeIndex(edgeIndex, false), false))) :
          (new Point2D.Double
           (getAnchorPosition(edgeIndex, anchorIndex + 1, true),
            getAnchorPosition(edgeIndex, anchorIndex + 1, false))));
       setAnchorPosition(edgeIndex, anchorIndex,
                         (src.getX() + trg.getX()) / 2.0d,
                         (src.getY() + trg.getY()) / 2.0d); }
     public void setAnchorPosition(int edgeIndex, int anchorIndex,
                                   double xPos, double yPos) {
       checkAnchorIndexBounds(edgeIndex, anchorIndex, false);
       checkMutableAnchor(edgeIndex);
       checkPosition(xPos, yPos);
       m_edgeTranslation[edgeIndex].getBend().moveHandle
         (anchorIndex, new Point2D.Double(xPos + m_xOff, yPos + m_yOff)); }
   }
 
   /**
    * Returns a representation of Cytoscape's current network view.
    * Returns a <code>MutablePolyEdgeGraphLayout</code>
    * which, when mutated, has no
    * effect on the underlying Cytoscape network view.  Use the return value
    * of this method when moving positions of nodes in a thread that is
    * not the AWT dispatch thread.  Use <code>updateCytoscapeLayout()</code>,
    * passing as an argument the return value of <code>getGraphCopy()</code>,
    * to move nodes (and edge anchor points)
    * in the underlying Cytoscape network view.
    * <code>getMaxWidth()</code> of the return object is ...
    * All distances are preserved from to Cytoscape graph to the return object.
    **/
   public static MutablePolyEdgeGraphLayout getGraphCopy
     (double percentBorder, boolean preserveEdgeAnchors)
   {
     if (percentBorder < 0.0d)
       throw new IllegalArgumentException("percentBorder < 0.0");
 
     CyNetworkView graphView = Cytoscape.getCurrentNetworkView();
     final int numNodesInTopology = graphView.getNodeViewCount();
     final int numEdgesInTopology = graphView.getEdgeViewCount();
 
     // Definiition of nodeTranslation:
     // nodeTranslation[i] defines, for node at index i in our
     // GraphTopology object, the corresponding NodeView in Giny.
     final NodeView[] nodeTranslation = new NodeView[numNodesInTopology];
 
     // Definition of edgeTranslation:
     // edgeTranslation[i] defines, for edge at index i in our
     // GraphTopology object, the corresponding EdgeView in Giny.
     final EdgeView[] edgeTranslation = new EdgeView[numEdgesInTopology];
 
     // Definiton of nodeIndexTranslation:
     // Both keys and values of this hashtable are java.lang.Integer objects.
     // There are exactly numNodesInTopology keys in this hashtable.
     // Key-to-value mappings define index-of-node-in-Giny to
     // index-of-node-in-GraphTopology mappings.  When I say
     // "index-of-node-in-Giny", I mean giny.model.Node.getRootGraphIndex().
     final Hashtable nodeIndexTranslation = new Hashtable();
 
     Iterator nodeIterator = graphView.getNodeViewsIterator();
     int nodeIndex = 0;
     double minX = Double.MAX_VALUE;
     double maxX = Double.MIN_VALUE;
     double minY = Double.MAX_VALUE;
     double maxY = Double.MIN_VALUE;
     boolean[] mobility = new boolean[numNodesInTopology];
     final boolean noNodesSelected =
       (graphView.getSelectedNodeIndices().length == 0);
 
     while (nodeIterator.hasNext())
     {
       NodeView currentNodeView = (NodeView) nodeIterator.next();
       nodeTranslation[nodeIndex] = currentNodeView;
       if (nodeIndexTranslation.put
           (new Integer(currentNodeView.getNode().getRootGraphIndex()),
            new Integer(nodeIndex)) != null)
         throw new IllegalStateException("Giny farted and someone lit a match");
       minX = Math.min(minX, currentNodeView.getXPosition());
       maxX = Math.max(maxX, currentNodeView.getXPosition());
       minY = Math.min(minY, currentNodeView.getYPosition());
       maxY = Math.max(maxY, currentNodeView.getYPosition());
       if (noNodesSelected) mobility[nodeIndex] = true;
       else mobility[nodeIndex] = currentNodeView.isSelected();
       nodeIndex++;
     }
     if (nodeIndex != numNodesInTopology)
       throw new IllegalStateException("something smells really bad here");
     Vector directedEdgeVector = new Vector();
     Vector undirectedEdgeVector = new Vector();
     Iterator edgeIterator = graphView.getEdgeViewsIterator();
     while (edgeIterator.hasNext())
     {
       EdgeView currentEdgeView = (EdgeView) edgeIterator.next();
       if ((!preserveEdgeAnchors) &&
           (noNodesSelected || nodeTranslation
            [((Integer) nodeIndexTranslation.get
              (new Integer
               (currentEdgeView.getEdge().getSource().
                getRootGraphIndex()))).intValue()].isSelected() ||
            nodeTranslation
            [((Integer) nodeIndexTranslation.get
              (new Integer
               (currentEdgeView.getEdge().getTarget().
                getRootGraphIndex()))).intValue()].isSelected())) {}
       else {
         List anchors = currentEdgeView.getBend().getHandles();
         for (int a = 0; a < anchors.size(); a++) {
           Point2D point = (Point2D) anchors.get(a);
           minX = Math.min(minX, point.getX());
           maxX = Math.max(maxX, point.getX());
           minY = Math.min(minY, point.getY());
           maxY = Math.max(maxY, point.getY()); } }
       Edge currentEdge = currentEdgeView.getEdge();
       int ginySourceNodeIndex = currentEdge.getSource().getRootGraphIndex();
       int ginyTargetNodeIndex = currentEdge.getTarget().getRootGraphIndex();
       int nativeSourceNodeIndex =
         ((Integer) nodeIndexTranslation.get
          (new Integer(ginySourceNodeIndex))).intValue();
       int nativeTargetNodeIndex =
         ((Integer) nodeIndexTranslation.get
          (new Integer(ginyTargetNodeIndex))).intValue();
       Vector chosenEdgeVector = undirectedEdgeVector;
       if (currentEdge.isDirected()) chosenEdgeVector = directedEdgeVector;
       chosenEdgeVector.add
         (new Object[] { new int[] { nativeSourceNodeIndex,
                                     nativeTargetNodeIndex },
                         currentEdgeView });
     }
     final int[] directedEdgeSourceNodeIndices =
       new int[directedEdgeVector.size()];
     final int[] directedEdgeTargetNodeIndices =
       new int[directedEdgeVector.size()];
     for (int i = 0; i < directedEdgeVector.size(); i++) {
       int[] edge = (int[]) (((Object[]) (directedEdgeVector.get(i)))[0]);
       EdgeView edgeV =
         (EdgeView) (((Object[]) (directedEdgeVector.get(i)))[1]);
       directedEdgeSourceNodeIndices[i] = edge[0];
       directedEdgeTargetNodeIndices[i] = edge[1];
       edgeTranslation[i] = edgeV; }
     final int[] undirectedEdgeSourceNodeIndices =
       new int[undirectedEdgeVector.size()];
     final int[] undirectedEdgeTargetNodeIndices =
       new int[undirectedEdgeVector.size()];
     for (int i = 0; i < undirectedEdgeVector.size(); i++) {
       int[] edge = (int[]) (((Object[]) (undirectedEdgeVector.get(i)))[0]);
       EdgeView edgeV =
         (EdgeView) (((Object[]) (undirectedEdgeVector.get(i)))[1]);
       undirectedEdgeSourceNodeIndices[i] = edge[0];
       undirectedEdgeTargetNodeIndices[i] = edge[1];
       edgeTranslation[i + directedEdgeSourceNodeIndices.length] = edgeV; }
     final double[] nodeXPositions = new double[numNodesInTopology];
     final double[] nodeYPositions = new double[numNodesInTopology];
     final double border =
       Math.max(maxX - minX, maxY - minY) * percentBorder * 0.5d;
     final double xOff = minX - border;
     final double yOff = minY - border;
     for (int i = 0; i < numNodesInTopology; i++) {
       nodeXPositions[i] = nodeTranslation[i].getXPosition() - xOff;
       nodeYPositions[i] = nodeTranslation[i].getYPosition() - yOff; }
     final double[][] edgeAnchorXPositions = new double[numEdgesInTopology][];
     final double[][] edgeAnchorYPositions = new double[numEdgesInTopology][];
     for (int e = 0; e < edgeTranslation.length; e++) {
       EdgeView currentEdge = edgeTranslation[e];
       if ((!preserveEdgeAnchors) &&
           (noNodesSelected || nodeTranslation
            [((Integer) nodeIndexTranslation.get
              (new Integer
               (currentEdge.getEdge().getSource().
                getRootGraphIndex()))).intValue()].isSelected() ||
            nodeTranslation
            [((Integer) nodeIndexTranslation.get
              (new Integer
               (currentEdge.getEdge().getTarget().
                getRootGraphIndex()))).intValue()].isSelected())) {}
       else {
         List anchors = currentEdge.getBend().getHandles();
         edgeAnchorXPositions[e] = new double[anchors.size()];
         edgeAnchorYPositions[e] = new double[anchors.size()];
         for (int a = 0; a < anchors.size(); a++) {
           Point2D point = (Point2D) anchors.get(a);
           edgeAnchorXPositions[e][a] = point.getX() - xOff;
           edgeAnchorYPositions[e][a] = point.getY() - yOff; } } }
     return new MyCMutableGraphLayout(numNodesInTopology,
                                      directedEdgeSourceNodeIndices,
                                      directedEdgeTargetNodeIndices,
                                      undirectedEdgeSourceNodeIndices,
                                      undirectedEdgeTargetNodeIndices,
                                      maxX - minX + border + border,
                                      maxY - minY + border + border,
                                      nodeXPositions,
                                      nodeYPositions,
                                      mobility,
                                      edgeAnchorXPositions,
                                      edgeAnchorYPositions,
                                      nodeTranslation,
                                      edgeTranslation,
                                      xOff,
                                      yOff);
   }
 
   /**
    * <code>layout</code> must be an object previously returned
    * by <code>GraphConverter.getGraphCopy()</code>.  During the time
    * that passes between setting <code>layout</code> as the return value
    * of <code>getGraphCopy()</code> and calling this method with the same
    * <code>layout</code>, Cytoscape's current network view topology
    * cannot change, otherwise this method will barf.
    *
    * @exception IllegalArgumentException if <code>layout</code> is not
    *   a value that was previously returned by <code>getGraphCopy()</code>.
    **/
   public static void updateCytoscapeLayout(MutablePolyEdgeGraphLayout layout)
   {
     MyCMutableGraphLayout myLayout;
     try { myLayout = (MyCMutableGraphLayout) layout; }
     catch (RuntimeException e) {
       throw new IllegalArgumentException
         ("layout is not a previous return value of getGraphCopy()"); }
     NodeView[] nodeTranslation = myLayout.m_nodeTranslation;
     EdgeView[] edgeTranslation = myLayout.m_edgeTranslation;
 
     // Remove edge anchor points before moving nodes.
     for (int e = 0; e < edgeTranslation.length; e++)
       edgeTranslation[e].getBend().removeAllHandles();
 
     // Move nodes in underlying Giny.
     for (int n = 0; n < nodeTranslation.length; n++) {
    
      double x = layout.getNodePosition(n, true) + myLayout.m_xOff;
      double y = layout.getNodePosition(n, false) + myLayout.m_yOff;

      System.out.println( "Node: "+n+" X: "+x+" Y: "+y);

       nodeTranslation[n].setXPosition
         (layout.getNodePosition(n, true) + myLayout.m_xOff);
       nodeTranslation[n].setYPosition
         (layout.getNodePosition(n, false) + myLayout.m_yOff); }
 
     // Set edge anchor points in underlying Giny.
     for (int e = 0; e < edgeTranslation.length; e++) {
       Vector anchorList = new Vector();
      for (int a = 0; a < layout.getNumAnchors(e); a++) {
       
        double x = layout.getAnchorPosition(e, a, true) + myLayout.m_xOff;
        double y = layout.getAnchorPosition(e, a, false) + myLayout.m_yOff;

        System.out.println( "Edge "+e+" Handle: "+a+" X: "+x+" Y: "+y);

         anchorList.add
           (new Point2D.Double
            (layout.getAnchorPosition(e, a, true) + myLayout.m_xOff,
             layout.getAnchorPosition(e, a, false) + myLayout.m_yOff));
      }
       edgeTranslation[e].getBend().setHandles(anchorList); }
   }
 
   /**
    * Returns a representation of Cytoscape's current network view.
    * Returns a <code>MutableGraphLayout</code>, which, when mutated,
    * has a direct effect on the underlying Cytoscape network view.  You'd
    * sure as heck better'd be using the returned object from the AWT event
    * dispatch thread!  Better yet, lock the Cytoscape desktop somehow
    * (with a modal dialog for example) while using this returned object.
    * Movable nodes are defined to be selected nodes in Cytoscape - if no
    * nodes are selected then all nodes are movable.  If selected node
    * information changes while we have a reference to this return object,
    * then the movability of corresponding node also changes.  This is one
    * reason why it's important to &quot;lock&quot; the Cytoscape desktop
    * while operating on this return object.
    **/
   public static MutablePolyEdgeGraphLayout getGraphReference
     (double percentBorder, boolean preserveEdgeAnchors)
   {
     if (percentBorder < 0.0d)
       throw new IllegalArgumentException("percentBorder < 0.0");
 
     CyNetworkView graphView = Cytoscape.getCurrentNetworkView();
     final int numNodesInTopology = graphView.getNodeViewCount();
     final int numEdgesInTopology = graphView.getEdgeViewCount();
 
     // Definiition of nodeTranslation:
     // nodeTranslation[i] defines, for node at index i in our
     // GraphTopology object, the corresponding NodeView in Giny.
     final NodeView[] nodeTranslation = new NodeView[numNodesInTopology];
 
     // Definition of edgeTranslation:
     // edgeTranslation[i] defines, for edge at index i in our
     // GraphTopology object, the corresponding EdgeView in Giny.
     final EdgeView[] edgeTranslation = new EdgeView[numEdgesInTopology];
 
     // Definiton of nodeIndexTranslation:
     // Both keys and values of this hashtable are java.lang.Integer objects.
     // There are exactly numNodesInTopology keys in this hashtable.
     // Key-to-value mappings define index-of-node-in-Giny to
     // index-of-node-in-GraphTopology mappings.  When I say
     // "index-of-node-in-Giny", I mean giny.model.Node.getRootGraphIndex().
     final Hashtable nodeIndexTranslation = new Hashtable();
 
     Iterator nodeIterator = graphView.getNodeViewsIterator();
     int nodeIndex = 0;
     double minX = Double.MAX_VALUE;
     double maxX = Double.MIN_VALUE;
     double minY = Double.MAX_VALUE;
     double maxY = Double.MIN_VALUE;
     final boolean noNodesSelected =
       (graphView.getSelectedNodeIndices().length == 0);
 
     while (nodeIterator.hasNext())
     {
       NodeView currentNodeView = (NodeView) nodeIterator.next();
       nodeTranslation[nodeIndex] = currentNodeView;
       if (nodeIndexTranslation.put
           (new Integer(currentNodeView.getNode().getRootGraphIndex()),
            new Integer(nodeIndex)) != null)
         throw new IllegalStateException("Giny farted and someone lit a match");
       minX = Math.min(minX, currentNodeView.getXPosition());
       maxX = Math.max(maxX, currentNodeView.getXPosition());
       minY = Math.min(minY, currentNodeView.getYPosition());
       maxY = Math.max(maxY, currentNodeView.getYPosition());
       nodeIndex++;
     }
     if (nodeIndex != numNodesInTopology)
       throw new IllegalStateException("something smells really bad here");
     Iterator edgeIterator = graphView.getEdgeViewsIterator();
     int edgeIndex = 0;
     while (edgeIterator.hasNext())
     {
       EdgeView currentEdge = (EdgeView) edgeIterator.next();
       edgeTranslation[edgeIndex] = currentEdge;
       if ((!preserveEdgeAnchors) &&
           (noNodesSelected || nodeTranslation
            [((Integer) nodeIndexTranslation.get
              (new Integer
               (currentEdge.getEdge().getSource().
                getRootGraphIndex()))).intValue()].isSelected() ||
            nodeTranslation
            [((Integer) nodeIndexTranslation.get
              (new Integer
               (currentEdge.getEdge().getTarget().
                getRootGraphIndex()))).intValue()].isSelected())) {
         currentEdge.getBend().removeAllHandles(); }
       else {
         List handles = currentEdge.getBend().getHandles();
         for (int h = 0; h < handles.size(); h++) {
           Point2D point = (Point2D) handles.get(h);
           minX = Math.min(minX, point.getX());
           maxX = Math.max(maxX, point.getX());
           minY = Math.min(minY, point.getY());
           maxY = Math.max(maxY, point.getY()); } }
       edgeIndex++;
     }
     if (edgeIndex != numEdgesInTopology)
       throw new IllegalStateException("someone [did] cut the cheese here");
     final double border =
       Math.max(maxX - minX, maxY - minY) * percentBorder * 0.5d;
     final double xOff = minX - border;
     final double yOff = minY - border;
     return new MyRMutableGraphLayout(nodeTranslation,
                                      edgeTranslation,
                                      nodeIndexTranslation,
                                      maxX - minX + border + border,
                                      maxY - minY + border + border,
                                      xOff,
                                      yOff,
                                      noNodesSelected);
   }
 
 }
