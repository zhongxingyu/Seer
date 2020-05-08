 /*
  * This class implements the GraphManipulationInterface. It handles
  * manipulations that are done on the JGraphX-graph that
  * is viewed on the panel.
  * Manipulation will be done by the user.
  *
  * $Header$
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.drawing;
 
 import java.awt.Color;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.jgrapht.Graph;
 
 import teo.isgci.util.Latex2Html;
 import teo.isgci.util.UserSettings;
 
 import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
 import com.mxgraph.model.mxICell;
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.util.mxConstants;
 import com.mxgraph.util.mxEvent;
 import com.mxgraph.util.mxEventObject;
 import com.mxgraph.util.mxEventSource.mxIEventListener;
 import com.mxgraph.util.mxUndoManager;
 import com.mxgraph.util.mxUndoableEdit;
 import com.mxgraph.util.mxUtils;
 import com.mxgraph.view.mxGraph;
 import com.mxgraph.view.mxGraphView;
 
 /**
  * This class implements the GraphManipulationInterface. It handles
  * manipulations that are done on the JGraphX-graph that is 
  * viewed on the panel.
  * Manipulation will be done by the user.
  *
  * @param <V> : vertices of the graph
  * @param <E> : edges of the graph
  */
 class GraphManipulation<V, E> implements GraphManipulationInterface<V, E> {
 
     /**
      * How far the user can zoom in.
      */
     private static final double MAXZOOMLEVEL = 8; 
     
     /**
      * Defines the original edge color.
      */
     private static final Color EDGECOLOR = new Color(100, 130, 185);
     
     /**
      * Defines the color with which improper edges are marked.
      */
     private static final Color MARKEDCOLOR = Color.black;
     
     /**
      * Defines the color that should be used for highlighting.
      */
     private Color highlightColor; 
     
     /**
      * Defines the color that should be used for selection.
      */
     private Color selectionColor;
     
     /**
      * Defines the thickness for highlighting.
      */
     private  String cellThickness = "4";
 
     /** Handles undo events in jgraphx. */
     private mxIEventListener undoHandler = new mxIEventListener() {
         public void invoke(Object source, mxEventObject evt) {
             if (recordUndoableActions == 0) {
                 undoManager.undoableEditHappened((mxUndoableEdit) evt
                         .getProperty("edit"));
             }
         }
     };
     
     /**
      * Defines whether or not the undoHandler should record actions.
      */
     private int recordUndoableActions;
     
     /**
      * The parent interface from which this object was created.
      */
     private JGraphXInterface<V, E> drawLib;
     
     /**
      * Manages the undo-operations on the calling graph.
      */
     private mxUndoManager undoManager;
     
     /**
      * Currently highlighted cells with their previous color.
      */
     private HashMap<mxICell, Color> highlightedCellsColor;
     
     /**
      * Currently highlighted cells with their previous thickness.
      */
     private HashMap<mxICell, String> highlightedCellsThickness;
 
     /**
      * List of currently selected cells.
      */
     private List<mxICell> selectedCells;
     
     /**
      * Constructor of the class. Creates an instance of the GraphManipulation
      * class that operates on a given graphComponent from the given
      * JGraphXInterface.
      *
      * @param drawingLibraryInterface
      *          The drawingLibraryInterface from which this object originated
      */
     public GraphManipulation(
             JGraphXInterface<V, E> drawingLibraryInterface) {
         drawLib = drawingLibraryInterface;
         mxGraphComponent graphComponent = drawLib.getGraphComponent();
         
         
         // initialize colors
         highlightColor = UserSettings.getCurrentHighlightColor();
         selectionColor = UserSettings.getCurrentSelectionColor();
         
         // initiation of undoManager variable
         undoManager = new mxUndoManager();
         
         // notify undoManager about edits
         graphComponent.getGraph().getModel()
                 .addListener(mxEvent.UNDO, undoHandler);
         graphComponent.getGraph().getView()
                 .addListener(mxEvent.UNDO, undoHandler);
 
         // notify this component about size changes, to check if the minimap
         // has to be displayed/hidden
         graphComponent.addComponentListener(new ComponentListener() {
 
             @Override
             public void componentShown(ComponentEvent e) {
             }
 
             @Override
             public void componentResized(ComponentEvent e) {
                 applyZoomSettings();
             }
 
             @Override
             public void componentMoved(ComponentEvent e) {
             }
 
             @Override
             public void componentHidden(ComponentEvent e) {
             }
         });
 
         highlightedCellsColor = new HashMap<mxICell, Color>();
         highlightedCellsThickness = new HashMap<mxICell, String>();
         selectedCells = new ArrayList<mxICell>();
     }
 
     /**
      * Returns the current graph adapter.
      *
      * @return The current graph adapter
      */
     private JGraphXAdapter<V, E> getGraphAdapter() {
         return (JGraphXAdapter<V, E>) drawLib.getGraphComponent().getGraph();
     }
 
     /**
      * Returns the cell associated with the given node.
      *
      * @param node
      *          The node for which the cell should be returned
      * @return
      *          The cell associated with the node
      */
     private mxICell getCellFromNode(V node) {
         return getGraphAdapter().getVertexToCellMap().get(node);
     }
 
     /**
      * Returns the cell associated with the given edge.
      *
      * @param edge
      *          The edge for which the cell should be returned
      * @return
      *          The cell associated with the edge
      */
     private mxICell getCellFromEdge(E edge) {
         return getGraphAdapter().getEdgeToCellMap().get(edge);
     }
 
     /**
      * Returns the cells associated with the given nodes.
      *
      * @param nodes
      *          The nodes for which the cells should be returned
      * @return
      *          The cells associated with the nodes
      */
     private mxICell[] getCellsFromNodes(V[] nodes) {
         mxICell[] cells = new mxICell[nodes.length];
         for (int i = 0; i < nodes.length; i++) {
             cells[i] = getCellFromNode(nodes[i]);
         }
         return cells;
     }
 
     /**
      * Returns the cells associated with the given edges.
      *
      * @param edges
      *          The edges for which the cells should be returned
      * @return
      *          The cells associated with the edges
      */
     private mxICell[] getCellsFromEdges(E[] edges) {
         mxICell[] cells = new mxICell[edges.length];
         for (int i = 0; i < edges.length; i++) {
             cells[i] = getCellFromEdge(edges[i]);
         }
         return cells;
     }
 
     @Override
     public boolean canRedo() {
         return undoManager.canRedo();
     }
 
     @Override
     public boolean canUndo() {
         return undoManager.canUndo();
     }
 
     @Override
     public void centerNode(V node) {
         drawLib.getGraphComponent().scrollCellToVisible(
                 getCellFromNode(node), true);
     }
 
     @Override
     public void colorNode(V[] nodes, Color color) {
 
         mxGraph graph = drawLib.getGraphComponent().getGraph();
 
         beginUpdate();
         try {
             graph.setCellStyles(mxConstants.STYLE_FILLCOLOR,
                     mxUtils.hexString(color), getCellsFromNodes(nodes));
         } finally {
             endUpdate();
         }
     }
 
     @Override
     public void setFontColor(Color color) {
 
         mxGraph graph = drawLib.getGraphComponent().getGraph();
 
         beginUpdate();
         try {
 
             graph.setCellStyles(mxConstants.STYLE_FONTCOLOR,
                     mxUtils.hexString(color),
                     graph.getChildVertices(graph.getDefaultParent()));
 
         } finally {
             endUpdate();
         }
     }
 
     @Override
     public void setBackgroundColor(Color color) {
         beginUpdate();
         try {
             drawLib.getGraphComponent().getViewport().setBackground(color);
         } finally {
             endUpdate();
         }
     }
 
     @Override
     public void markEdge(E[] edges) {
         mxGraph graph = drawLib.getGraphComponent().getGraph();
         mxICell[] cells = getCellsFromEdges(edges);
         
         beginUpdate();
         try {
             for (mxICell edge : cells) {
                 graph.setCellStyles(mxConstants.STYLE_STARTARROW,
                         mxConstants.ARROW_CLASSIC, new Object[] { edge });
                 
                 // update original values of highlighted cells
                 if (highlightedCellsColor.containsKey(edge)) {
                     highlightedCellsColor.put(edge, MARKEDCOLOR);
                 } else { // or update cell directly
                     graph.setCellStyles(mxConstants.STYLE_STROKECOLOR,
                             mxUtils.hexString(MARKEDCOLOR),
                             new Object[] { edge });
                 }
             }
         } finally {
             endUpdate();
         }
     }
 
     @Override
     public void unmarkEdge(E[] edges) {
         mxGraph graph = drawLib.getGraphComponent().getGraph();
         mxICell[] cells = getCellsFromEdges(edges);
         
         
         beginUpdate();
         try {
             for (mxICell edge : cells) {
                 
                 graph.setCellStyles(mxConstants.STYLE_STARTARROW, "",
                         new Object[] { edge });
                 
                 // update original values of highlighted cells and 
                 if (highlightedCellsColor.containsKey(edge)) {
                     highlightedCellsColor.put(edge, EDGECOLOR);
                 } else { // or update cell directly
                     graph.setCellStyles(mxConstants.STYLE_STROKECOLOR,
                             mxUtils.hexString(EDGECOLOR),
                             new Object[] { edge });
                 }
             }
         } finally {
             endUpdate();
         }
     }
 
     @Override
     public void reapplyHierarchicalLayout() {
         mxGraph graph = drawLib.getGraphComponent().getGraph();
 
         beginUpdate();
         try {
             mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
             layout.execute(graph.getDefaultParent());
 
         } finally {
             endUpdate();
         }
 
         applyZoomSettings();
     }
 
     @Override
     public void redo() {
         undoManager.redo();
 
         applyZoomSettings();
     }
 
     @Override
     public void removeNode(V node) {
         mxGraph graph = drawLib.getGraphComponent().getGraph();
 
         Object[] cells = new Object[]{getCellFromNode(node)};
         
         // Adds all edges connected to the node
         cells = graph.addAllEdges(cells);
         
         beginUpdate();
         try {
             // Deletes every cell
             for (Object object : cells) {
                 graph.getModel().remove(object);
             }
         } finally {
             endUpdate();
         }
 
         applyZoomSettings();
     }
 
     /**
      * Starts the recording of actions to the undo history.
      */
     @Override
     public void endNotUndoable() {
         recordUndoableActions--;
     }
 
     /**
      * Stops the recording of actions to the undo history.
      */
     @Override
     public void beginNotUndoable() {
         recordUndoableActions++;
     }
 
     @Override
     public void unHighlightAll() {
         beginNotUndoable();
         beginUpdate();
         try {
             for (mxICell cell : highlightedCellsColor.keySet()) {
 
                 // ignore cells that are selected
                 if (selectedCells.contains(cell)) {
                     continue;
                 }
                 
                 
                 getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKECOLOR,
                         mxUtils.getHexColorString(
                                 highlightedCellsColor.get(cell)),
                         new Object[]{cell});
 
                 getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKEWIDTH,
                         highlightedCellsThickness.get(cell), 
                         new Object[]{cell});
                 
             }
             
             // second iteration is needed so we don't delete from the keyset
             // while deleting from it - same reason why we use .toArray here
             for (Object obj : highlightedCellsColor.keySet().toArray()) {
                 
                 // ignore cells that are selected
                 if (selectedCells.contains(obj)) {
                     continue;
                 }
                 
                 highlightedCellsColor.remove(obj);
                 highlightedCellsThickness.remove(obj);
             }
         } finally {
             endUpdate();
             endNotUndoable();
             
             /*
              * graphOutline sometimes won't take changes from this method, to
              * ensure that it properly shows all changes it's visibility is 
              * turned off and on again.
              * FIXME graphOutline should react properly to this method
              */
             drawLib.getGraphOutline().setVisible(false);
             applyZoomSettings();
         }
     }
 
     @Override
     public void unHighlightNode(V tnode) {
         mxICell node = getCellFromNode(tnode);
         
         if (!highlightedCellsColor.containsKey(node)) {
             return;
         }
         
         beginNotUndoable();
         beginUpdate();
         
         try {
             getGraphAdapter().setCellStyles(
                             mxConstants.STYLE_STROKECOLOR,
                             mxUtils.getHexColorString(highlightedCellsColor
                                     .get(node)), new Object[] { node });
 
             getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKEWIDTH,
                             highlightedCellsThickness.get(node),
                             new Object[] { node });
 
             highlightedCellsColor.remove(node);
             highlightedCellsThickness.remove(node);
         } finally {
             endUpdate();
             endNotUndoable();
         }
         
         
         Graph<V, E> graph = drawLib.getGraph();
         Set<E> edges = graph.edgesOf(
                 getGraphAdapter().getCellToVertexMap().get(node));
         
         for (E edge : edges) {
             V parent = graph.getEdgeSource(edge);
             V child = graph.getEdgeTarget(edge);
             mxICell mxParent = getCellFromNode(parent);
             mxICell mxChild = getCellFromNode(child);
             mxICell mxEdge = getCellFromEdge(edge);
             
             // unhighlight edge if necessary
             if (highlightedCellsColor.containsKey(mxEdge)) {
                 beginNotUndoable();
                 beginUpdate();
                 try {
                     getGraphAdapter().setCellStyles(
                             mxConstants.STYLE_STROKECOLOR,
                             mxUtils.getHexColorString(highlightedCellsColor
                                     .get(mxEdge)), new Object[] { mxEdge });
 
                     getGraphAdapter().setCellStyles(
                             mxConstants.STYLE_STROKEWIDTH,
                             highlightedCellsThickness.get(mxEdge),
                             new Object[] { mxEdge });
 
                     highlightedCellsColor.remove(mxEdge);
                     highlightedCellsThickness.remove(mxEdge);
                 } finally {
                     endUpdate();
                     endNotUndoable();
                 }
                 
             }
             
             // stop if node is selected, else recursively unhighlight nodes
             if (!selectedCells.contains(mxParent)) {
                 unHighlightNode(parent);
             }
             
             if (!selectedCells.contains(mxChild)) {
                 unHighlightNode(child);
             }
          }
         
         /*
          * graphOutline sometimes won't take changes from this method, to
          * ensure that it properly shows all changes it's visibility is 
          * turned off and on again.
          * FIXME graphOutline should react properly to this method
          */
         drawLib.getGraphOutline().setVisible(false);
         applyZoomSettings();
         
     }
 
     /**
      * Call to start a block of updates.
      */
     @Override
     public void beginUpdate() {
         getGraphAdapter().getModel().beginUpdate();
     }
 
     /**
      * Call to end and execute a block of updates.
      */
     @Override
     public void endUpdate() {
         getGraphAdapter().getModel().endUpdate();
     }
 
     @Override
     public void renameNode(V node, String newName) {
         mxGraph graph = drawLib.getGraphComponent().getGraph();
 
         newName = Latex2Html.getInstance().html(newName);
 
         mxICell cell = getCellFromNode(node);
 
         graph.getModel().setValue(cell, newName);
         graph.updateCellSize(cell);
 
         applyZoomSettings();
     }
 
     @Override
     public void undo() {
         undoManager.undo();
         applyZoomSettings();
     }
 
     @Override
     public double getZoomLevel() {
         return drawLib.getGraphComponent().getGraph().getView().getScale();
     }
 
     @Override
     public void zoomTo(double factor) {
         factor = Math.min(MAXZOOMLEVEL, factor);
 
         drawLib.getGraphComponent().zoomTo(factor, true);
 
         applyZoomSettings();
     }
 
     @Override
     public void zoom(boolean zoomIn) {
         drawLib.getGraphComponent().setCenterZoom(true);
 
         if (zoomIn) {
             if (getZoomLevel() < MAXZOOMLEVEL) {
                 drawLib.getGraphComponent().zoomIn();
             }
         } else {
             drawLib.getGraphComponent().zoomOut();
         }
 
         applyZoomSettings();
     }
 
     @Override
     public void zoomToFit() {
         mxGraphView view = drawLib.getGraphComponent().getGraph().getView();
 
         // Get the component and view heights/widths
         int compWidth = drawLib.getGraphComponent().getWidth();
         int viewWidth = (int) view.getGraphBounds().getWidth();
         int compHeight = drawLib.getGraphComponent().getHeight();
         int viewHeight = (int) view.getGraphBounds().getHeight();
 
         double relWidth = (double) compWidth / viewWidth;
         double relHeight = (double) compHeight / viewHeight;
 
         // If the relative Width is greater than the rel. height
         // set the scale to fit the width and vice versa
         if (relWidth <= relHeight) {
             view.setScale(relWidth * view.getScale());
         } else {
             view.setScale(relHeight * view.getScale()); 
         }
 
         applyZoomSettings();
     }
 
     /**
      * Checks if the minimap should be hidden and hides it if necessary.
      * Sets the thickness according to zoom-level
      */
     public void applyZoomSettings() {
         // check minimap visibility
         mxGraphView view = drawLib.getGraphComponent().getGraph().getView();
         
         // Get the component and view heights/widths
         int compWidth = drawLib.getGraphComponent().getWidth();
         int viewWidth = (int) view.getGraphBounds().getWidth();
         int compHeight = drawLib.getGraphComponent().getHeight();
         int viewHeight = (int) view.getGraphBounds().getHeight();
 
         double relWidth = (double) compWidth / viewWidth;
         double relHeight = (double) compHeight / viewHeight;
 
         double scale;
         
         // If the relative Width is greater than the rel. height
         // set the scale to fit the width and vice versa
         if (relWidth <= relHeight) {
             scale = relWidth * view.getScale();
         } else {
             scale = relHeight * view.getScale(); 
         }
 
         if (getZoomLevel() > scale) {
             drawLib.getGraphOutline().setVisible(true);
         } else {
             drawLib.getGraphOutline().setVisible(false);
         }
         
         // set thickness based on zoom
         if (getZoomLevel() < 0.4) {
             setThickness("12");
         } else if (getZoomLevel() < 0.7) {
             setThickness("8");
         } else {
             setThickness("4");
         }
     }
 
     @Override
     public void removeHighlightedNodes() {
         // get a copy of the list before removing everything
         List<mxICell> cells 
             = new ArrayList<mxICell>(highlightedCellsColor.keySet().size());
             
         for (mxICell cell : highlightedCellsColor.keySet()) {
             cells.add(cell);
         }
         
         // unhighlighting everything (thats why we need the copy) 
         beginNotUndoable();
         unHighlightAll();
         endNotUndoable();
         
         // deselect all nodes
         drawLib.setSelectedNodes(new ArrayList<V>());
         
         
         beginUpdate();
         try {
             HashMap<mxICell, V> cellToVertex 
                 = getGraphAdapter().getCellToVertexMap();
             
             for (mxICell cell : cells) {
                 removeNode(cellToVertex.get(cell));
             }
             
         } finally {
             endUpdate();
         }
         
     }
 
     @Override
     public void highlightParents(List<V> roots) {
         beginNotUndoable();
         Graph<V, E> graph = drawLib.getGraph();
 
         // don't visit a node twice
         List<V> visitedNodes = new ArrayList<V>();
 
         for (V root : roots) {
             Set<E> edges = graph.edgesOf(root);
 
             for (E edge : edges) {
                 V parent = graph.getEdgeSource(edge);
                 mxICell mxParent = getCellFromNode(parent);
 
                 // edge was pointing to child
                 if (parent == root) {
                     continue;
                 }
 
                 // highlight node
                 highlightCell(getCellFromEdge(edge), highlightColor);
 
                 // node already selected -> only mark the edge and continue
                 if (selectedCells.contains(mxParent)) {
                     continue;
                 }
 
                 // cell already highlighted: recursively highlight if it's
                 // not a selection and if the node wasn't already visited
                 // during this loop
                 if (highlightedCellsColor.containsKey(mxParent)
                         && !selectedCells.contains(mxParent)
                         && !visitedNodes.contains(parent)) {
                     // got to use a list
                     List<V> parentList = new ArrayList<V>();
                     parentList.add(parent);
                     highlightParents(parentList);
                 } else {
                     visitedNodes.add(parent);
                     highlightCell(mxParent, highlightColor);
                 }
             }
 
         }
         endNotUndoable();
         
         /*
          * graphOutline sometimes won't take changes from this method, to
          * ensure that it properly shows all changes it's visibility is 
          * turned off and on again.
          * FIXME graphOutline should react properly to this method
          */
         drawLib.getGraphOutline().setVisible(false);
         applyZoomSettings();
     }
 
     @Override
     public void highlightChildren(List<V> roots) {
         beginNotUndoable();
         Graph<V, E> graph = drawLib.getGraph();
 
         // don't visit a node twice
         List<V> visitedNodes = new ArrayList<V>();
 
         for (V root : roots) {
             Set<E> edges = graph.edgesOf(root);
 
             for (E edge : edges) {
                 V child = graph.getEdgeTarget(edge);
                 mxICell mxChild = getCellFromNode(child);
 
                 // edge was pointing to parent
                 if (child == root) {
                     continue;
                 }
 
                 // highlight node
                 highlightCell(getCellFromEdge(edge), highlightColor);
 
                 // node already selected -> only mark the edge and continue
                 if (selectedCells.contains(mxChild)) {
                     continue;
                 }
 
                 // cell already highlighted: recursively highlight if it's
                 // not a selection and if the node wasn't already visited
                 // during this loop
                 if (highlightedCellsColor.containsKey(mxChild)
                         && !selectedCells.contains(mxChild)
                         && !visitedNodes.contains(child)) {                    
                     // got to use a list
                     List<V> childList = new ArrayList<V>();
                     childList.add(child);
                     highlightChildren(childList);
                 } else {
                     highlightCell(mxChild, highlightColor);
                     visitedNodes.add(child);
                 }
             }
 
         }
         endNotUndoable();
         
         /*
          * graphOutline sometimes won't take changes from this method, to
          * ensure that it properly shows all changes it's visibility is 
          * turned off and on again.
          * FIXME graphOutline should react properly to this method
          */
         drawLib.getGraphOutline().setVisible(false);
         applyZoomSettings();
     }
 
     @Override
     public void setHighlightColor(Color color) {
         highlightColor = color;
         
         for (mxICell cell : highlightedCellsColor.keySet()) {
             
             // ignore selected cells
             if (selectedCells.contains(cell)) {
                 continue;
             }
             
             getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKECOLOR,
                     mxUtils.getHexColorString(color), new Object[] {cell});
         }
     }
 
     @Override
     public void setSelectionColor(Color color) {
         selectionColor = color;
         
         for (mxICell cell : selectedCells) {
             getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKECOLOR,
                     mxUtils.getHexColorString(color), new Object[] {cell});
         }
     }
     
     /**
      * Updates all selected cells and adds a border around them.
      */
     public void updateSelectedCells() {
         beginNotUndoable();
         beginUpdate();
         
         // build difference between old cells and new cells
         Object[] newCells
             = drawLib.getGraphComponent().getGraph().getSelectionCells();
 
         // convert new cells to list     
         List<mxICell> currentCells 
             = new ArrayList<mxICell>(newCells.length);
             
         for (Object obj : newCells) {
             if (obj instanceof mxICell) {
                currentCells.add((mxICell) obj); 
             }
         }
         
         // build difference and unhighlight all that are not in new cells
         for (mxICell cell : selectedCells) {
             if (!currentCells.contains(cell)) {
                 unHighlightNode(getGraphAdapter()
                         .getCellToVertexMap().get(cell));
             }
         }
         
         selectedCells.clear();
        
         // if there are no selected cells, simply unhighlight everything
         if (newCells.length == 0) {
             unHighlightAll();
         } else {
             // select all cells
             for (mxICell cell : currentCells) {
                 selectedCells.add(cell);
                 highlightCell(cell, selectionColor);
             }
         }
         
         endUpdate();
         endNotUndoable();
     }
     
     
     /**
      * Highlights a cell with the given color and thickness.
      * @param cell
      *          The cell to highlight.
      * @param color
      *          The color in which the node should be highlighted
      */
     private void highlightCell(mxICell cell, Color color) {
         // don't overwrite original values
         if (!highlightedCellsColor.containsKey(cell)) {
 
             // save old values
             highlightedCellsThickness.put(cell, mxUtils.getString(
                     getGraphAdapter().getCellStyle(cell),
                     mxConstants.STYLE_STROKEWIDTH));
 
             highlightedCellsColor.put(cell, mxUtils.getColor(getGraphAdapter()
                     .getCellStyle(cell), mxConstants.STYLE_STROKECOLOR));
         }
         
         
         // apply new values
         getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKECOLOR,
                 mxUtils.getHexColorString(color),
                 new Object[] {cell});
         getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKEWIDTH,
                 cellThickness, new Object[] {cell});
     }
     
     /**
      * Sets the thickness of highlighting and selection.
      * @param value The thickness that should be applied.
      */
     private void setThickness(String value) {
         // nothing to do
         if (cellThickness.equals(value)) {
             return;
         }
         
         cellThickness = value;
         
         for (mxICell cell : highlightedCellsThickness.keySet()) {
             getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKEWIDTH,
                     value, new Object[] {cell});
         }
     }
 }
 
 /* EOF */
