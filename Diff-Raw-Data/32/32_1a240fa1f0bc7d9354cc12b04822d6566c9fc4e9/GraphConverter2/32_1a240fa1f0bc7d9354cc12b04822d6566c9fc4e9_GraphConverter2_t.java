 package cytoscape.foo;
 
 import cytoscape.Cytoscape;
 import cytoscape.graph.fixed.FixedGraph;
 import cytoscape.graph.layout.algorithm.MutablePolyEdgeGraphLayout;
 import cytoscape.util.intr.IntEnumerator;
 import cytoscape.util.intr.IntIterator;
 import cytoscape.view.CyNetworkView;
 import giny.view.EdgeView;
 import giny.view.NodeView;
 import java.awt.geom.Point2D;
 import java.util.Iterator;
 import java.util.List;
 
 public final class GraphConverter2
 {
 
   private GraphConverter2() {}
 
   /**
    * Returns a representation of Cytoscape's current network view.
    * Returns a MutablePolyEdgeGraphLayout, which, when mutated,
    * has a direct effect on the underlying Cytoscape network view.  You'd
    * sure as heck better be using the returned object from the AWT event
    * dispatch thread!  Better yet, lock the Cytoscape desktop somehow
    * (with a modal dialog for example) while using this returned object.
    * Movable nodes are defined to be selected nodes in Cytoscape - if no
    * nodes are selected then all nodes are movable.  If selected node
    * information changes while we have a reference to this return object,
    * then the movability of corresponding node also changes.  This is one
    * reason why it's important to lock the Cytoscape desktop while operating
    * on this return object.
    **/
   public static MutablePolyEdgeGraphLayout getGraphReference
     (double percentBorder,
      boolean preserveEdgeAnchors,
      boolean onlySelectedNodesMovable)
   {
     if (percentBorder < 0.0d)
       throw new IllegalArgumentException("percentBorder < 0.0");
 
     double minX = Double.MAX_VALUE;
     double maxX = Double.MIN_VALUE;
     double minY = Double.MAX_VALUE;
     double maxY = Double.MIN_VALUE;
     final CyNetworkView graphView = Cytoscape.getCurrentNetworkView();
     Iterator iter = graphView.getNodeViewsIterator();
     while (iter.hasNext())
     {
       NodeView currentNodeView = (NodeView) iter.next();
       minX = Math.min(minX, currentNodeView.getXPosition());
       maxX = Math.max(maxX, currentNodeView.getXPosition());
       minY = Math.min(minY, currentNodeView.getYPosition());
       maxY = Math.max(maxY, currentNodeView.getYPosition());
     }
     iter = graphView.getEdgeViewsIterator();
     final boolean noNodesSelected = (!onlySelectedNodesMovable) ||
       (graphView.getSelectedNodeIndices().length == 0);
     while (iter.hasNext())
     {
       EdgeView currentEdgeView = (EdgeView) iter.next();
       if ((!preserveEdgeAnchors) &&
           (noNodesSelected ||
            graphView.getNodeView
            (currentEdgeView.getEdge().getSource()).isSelected() ||
            graphView.getNodeView
            (currentEdgeView.getEdge().getTarget()).isSelected())) {
         currentEdgeView.getBend().removeAllHandles(); }
       else {
         List handles = currentEdgeView.getBend().getHandles();
         for (int h = 0; h < handles.size(); h++) {
           Point2D point = (Point2D) handles.get(h);
           minX = Math.min(minX, point.getX());
           maxX = Math.max(maxX, point.getX());
           minY = Math.min(minY, point.getY());
           maxY = Math.max(maxY, point.getY()); } }
     }
     double border =
       Math.max(maxX - minX, maxY - minY) * percentBorder * 0.5d;
     final double width = maxX - minX + border + border;
     final double height = maxY - minY + border + border;
     final double xOff = minX - border;
     final double yOff = minY - border;
 
     final FixedGraph fixedGraph = (FixedGraph) (graphView.getNetwork());
 
     return new MutablePolyEdgeGraphLayout()
       {
         // FixedGraph methods.
         public IntEnumerator nodes() { return fixedGraph.nodes(); }
         public IntEnumerator edges() { return fixedGraph.edges(); }
         public boolean nodeExists(int node) {
           return fixedGraph.nodeExists(node); }
         public byte edgeType(int edge) { return fixedGraph.edgeType(edge); }
         public int edgeSource(int edge) { return fixedGraph.edgeSource(edge); }
         public int edgeTarget(int edge) { return fixedGraph.edgeTarget(edge); }
         public IntEnumerator edgesAdjacent(int node, boolean out,
                                            boolean in, boolean undir) {
           return fixedGraph.edgesAdjacent(node, out, in, undir); }
         public IntIterator edgesConnecting(int node0, int node1,
                                            boolean out, boolean in,
                                            boolean undir) {
           return fixedGraph.edgesConnecting(node0, node1, out, in, undir); }
 
         // GraphLayout methods.
         public double getMaxWidth() { return width; }
         public double getMaxHeight() { return height; }
         public double getNodePosition(int node, boolean xPosition) {
          NodeView nodeView = getNodeView(node);
           if (xPosition) return nodeView.getXPosition() - xOff;
           return nodeView.getYPosition() - yOff; }
 
         // MutableGraphLayout methods.
         public boolean isMovableNode(int node) {
          NodeView nodeView = getNodeView(node);
           if (noNodesSelected) return true;
           return nodeView.isSelected(); }
        public void setNodePosition(int node, double xPos, double yPos) {
          NodeView nodeView = getNodeView(node);
          checkPosition(xPos, yPos);
          if (!isMovableNode(node))
            throw new UnsupportedOperationException
              ("node " + node + " is not movable");
          nodeView.setXPosition(xPos + xOff);
          nodeView.setYPosition(yPos + yOff); }
 
         // PolyEdgeGraphLayout methods.
         public int getNumAnchors(int edge) { return 0; }
         public double getAnchorPosition(int edge, int anchorIndex,
                                         boolean xPosition) {
           return 0.0; }
 
         // MutablePolyEdgeGraphLayout methods.
         public void deleteAnchor(int edge, int anchorIndex) {}
         public void createAnchor(int edge, int anchorIndex) {}
         public void setAnchorPosition(int edge, int anchorIndex,
                                       double xPos, double yPos) {}

        // Helper methods.
        private NodeView getNodeView(int node) {
          NodeView nodeView = graphView.getNodeView(~node);
          if (nodeView == null) throw new IllegalArgumentException
                                  ("node " + node + " not in this graph");
          return nodeView; }
        private void checkPosition(double xPos, double yPos) {
          if (xPos < 0.0d || xPos > getMaxWidth())
            throw new IllegalArgumentException("X position out of bounds");
          if (yPos < 0.0d || yPos > getMaxHeight())
            throw new IllegalArgumentException("Y position out of bounds"); }
       };
   }
 
 }
