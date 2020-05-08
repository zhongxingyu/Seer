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
 
 import teo.isgci.util.Latex2Html;
 import teo.isgci.util.UserSettings;
 
 import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
 import com.mxgraph.model.mxCell;
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
      * Defines the thickness for highlighting.
      */
     private static final String HIGHLIGHTTHICKNESS = "4";
     
     /**
      * Defines the original edge color.
      */
     private static final Color EDGECOLOR = new Color(100, 130, 185);
     
     /**
      * Defines the color that should be used for highlighting.
      */
     private Color highlightColor; 
     
     /**
      * Defines the color that should be used for selection.
      */
     private Color selectionColor;
     
     /** Handles undo events in jgraphx. */
     private mxIEventListener undoHandler = new mxIEventListener() {
         public void invoke(Object source, mxEventObject evt) {
             if (recordUndoableActions) {
                 undoManager.undoableEditHappened((mxUndoableEdit) evt
                         .getProperty("edit"));
             }
         }
     };
     
     /**
      * Defines whether or not the undoHandler should record actions.
      */
     private boolean recordUndoableActions = true;
     
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
      * Currently selected cells with their respective depth for highlighting.
      */
     private HashMap<mxICell, Integer> cellsToHDepth;
     
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
                 setMinimapVisibility();
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
         cellsToHDepth = new HashMap<mxICell, Integer>();
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
 
         beginUpdate();
         try {
             graph.setCellStyles(mxConstants.STYLE_STROKECOLOR,
                     mxUtils.hexString(Color.black), getCellsFromEdges(edges));
            graph.setCellStyles(mxConstants.STYLE_STARTARROW , 
                    mxConstants.ARROW_CLASSIC, getCellsFromEdges(edges));     
         } finally {
             endUpdate();
         }
     }
 
     @Override
     public void unmarkEdge(E[] edges) {
         mxGraph graph = drawLib.getGraphComponent().getGraph();
 
         beginUpdate();
         try {
             graph.setCellStyles(mxConstants.STYLE_STROKECOLOR,
                     mxUtils.hexString(EDGECOLOR),
                     getCellsFromEdges(edges));
 
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
 
         setMinimapVisibility();
     }
 
     @Override
     public void redo() {
         undoManager.redo();
 
         setMinimapVisibility();
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
 
         setMinimapVisibility();
     }
 
 
     /**
      * to be deleted.
      * @param node irrelevant
      * @param depth irrelevant
      */
     public void highlightNode(V node, int depth) {
         if (depth < 0) {
             return;
         }
         
         ArrayList<mxICell> cells = new ArrayList<mxICell>();
         ArrayList<mxICell> neighborCells = new ArrayList<mxICell>();
         mxICell startCell = getCellFromNode(node);
 
         cells.add(startCell);
 
         if (!highlightedCellsColor.containsKey(startCell)) {
             highlightedCellsThickness.put(startCell,
                     mxUtils.getString(getGraphAdapter()
                     .getCellStyle(startCell), mxConstants.STYLE_STROKEWIDTH));
 
             highlightedCellsColor.put(startCell, mxUtils.getColor(
                     getGraphAdapter()
                     .getCellStyle(startCell), mxConstants.STYLE_STROKECOLOR));
         }
 
         if (depth == 0) {
             getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKECOLOR,
                     mxUtils.getHexColorString(highlightColor),
                     cells.toArray());
             getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKEWIDTH,
                     HIGHLIGHTTHICKNESS, cells.toArray());
 
         } else {
 
             beginUpdate();
             try {
                 for (int i = 0; i < depth; i++) {
 
                     for (mxICell cell : cells) {
                         if (cell.isEdge()) {
                             continue;
                         }
                         for (int j = 0; j < cell.getEdgeCount(); j++) {
                             mxCell edge = (mxCell) cell.getEdgeAt(j);
 
                             if (highlightedCellsColor.containsKey(edge)) {
                                 continue;
                             }
 
                             highlightedCellsColor.put(edge, mxUtils.getColor(
                                     getGraphAdapter().getCellStyle(edge),
                                     mxConstants.STYLE_STROKECOLOR));
 
                             highlightedCellsThickness.put(edge, 
                                     mxUtils.getString(
                                     getGraphAdapter().getCellStyle(edge),
                                     mxConstants.STYLE_STROKEWIDTH));
 
                             neighborCells.add(edge);
 
                             mxICell source = edge.getSource();
                             mxICell target = edge.getTarget();
 
                             if (!highlightedCellsColor.containsKey(source)) {
                                 highlightedCellsThickness
                                         .put(source, mxUtils.getString(
                                                 getGraphAdapter()
                                                         .getCellStyle(source),
                                                mxConstants.STYLE_STROKEWIDTH));
 
                                 highlightedCellsColor
                                         .put(source, mxUtils.getColor(
                                                 getGraphAdapter()
                                                         .getCellStyle(source),
                                                mxConstants.STYLE_STROKECOLOR));
                                 neighborCells.add(source);
                             }
 
                             if (!highlightedCellsColor.containsKey(target)) {
 
                                 highlightedCellsThickness
                                         .put(target, mxUtils.getString(
                                                 getGraphAdapter()
                                                         .getCellStyle(target),
                                                mxConstants.STYLE_STROKEWIDTH));
 
                                 highlightedCellsColor
                                         .put(target, mxUtils.getColor(
                                                 getGraphAdapter()
                                                         .getCellStyle(target),
                                               mxConstants.STYLE_STROKECOLOR));
                                 neighborCells.add(target);
                             }
                         }
                     }
 
                     if (i > 0) {
                         cells.clear();
                     }
 
                     cells.addAll(neighborCells);
 
                     getGraphAdapter().setCellStyles(
                             mxConstants.STYLE_STROKECOLOR,
                             mxUtils.getHexColorString(highlightColor),
                             cells.toArray());
                     getGraphAdapter().setCellStyles(
                             mxConstants.STYLE_STROKEWIDTH, HIGHLIGHTTHICKNESS,
                             cells.toArray());
 
                     if (i == 0) {
                         cells.remove(startCell);
                     }
 
                     neighborCells.clear();
                 }
             } finally {
                 endUpdate();
             }
         }
     }
 
     /**
      * Starts the recording of actions to the undo history.
      */
     @Override
     public void endNotUndoable() {
         recordUndoableActions = true;
     }
 
     /**
      * Stops the recording of actions to the undo history.
      */
     @Override
     public void beginNotUndoable() {
         recordUndoableActions = false;
     }
 
     @Override
     public void unHiglightAll() {
         beginUpdate();
         try {
             for (mxICell cell : highlightedCellsColor.keySet()) {
 
                 // ignore cells that are selected
                 if (cellsToHDepth.containsKey(cell)) {
                     continue;
                 }
                 
                 getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKECOLOR,
                         mxUtils.getHexColorString(
                                 highlightedCellsColor.get(cell)),
                         new Object[]{cell});
 
                 getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKEWIDTH,
                         highlightedCellsThickness.get(cell), 
                         new Object[]{cell});
                 
                 highlightedCellsColor.remove(cell);
                 highlightedCellsThickness.remove(cell);
             }
         } finally {
             endUpdate();
             
             /*
              * graphOutline sometimes won't take changes from this method, to
              * ensure that it properly shows all changes it's visibility is 
              * turned off and on again.
              * FIXME graphOutline should react properly to this method
              */
             drawLib.getGraphOutline().setVisible(false);
             setMinimapVisibility();
         }
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
 
         setMinimapVisibility();
     }
 
     @Override
     public void undo() {
         undoManager.undo();
         setMinimapVisibility();
     }
 
     @Override
     public double getZoomLevel() {
         return drawLib.getGraphComponent().getGraph().getView().getScale();
     }
 
     @Override
     public void zoomTo(double factor) {
         factor = Math.min(MAXZOOMLEVEL, factor);
 
         drawLib.getGraphComponent().zoomTo(factor, true);
 
         setMinimapVisibility();
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
 
         setMinimapVisibility();
     }
 
     @Override
     public void zoomToFit() {
         mxGraphView view = drawLib.getGraphComponent().getGraph().getView();
 
         int compLen = drawLib.getGraphComponent().getWidth();
         int viewLen = (int) view.getGraphBounds().getWidth();
 
         view.setScale((double) compLen / viewLen * view.getScale());
         setMinimapVisibility();
     }
 
     /**
      * Checks if the minimap should be hidden and hides it if necessary.
      */
 
     public void setMinimapVisibility() {
         mxGraphView view = drawLib.getGraphComponent().getGraph().getView();
 
         int compLen = drawLib.getGraphComponent().getWidth();
         int viewLen = (int) view.getGraphBounds().getWidth();
 
         double scale = ((double) compLen / viewLen * view.getScale());
 
         if (getZoomLevel() > scale) {
             drawLib.getGraphOutline().setVisible(true);
         } else {
             drawLib.getGraphOutline().setVisible(false);
         }
     }
 
     @Override
     public void removeHighlightedNodes() {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void highlightParents(V[] roots) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void highlightChildren(V[] roots) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void setHighlightColor(Color color) {        
         highlightColor = color;
         
         for (mxICell cell : highlightedCellsColor.keySet()) {
             
             // ignore selected cells
             if (cellsToHDepth.containsKey(cell)) {
                 continue;
             }
             
             getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKECOLOR,
                     mxUtils.getHexColorString(color), new Object[] {cell});
         }
     }
 
     @Override
     public void setSelectionColor(Color color) {
         selectionColor = color;
         
         for (mxICell cell : cellsToHDepth.keySet()) {
             getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKECOLOR,
                     mxUtils.getHexColorString(color), new Object[] {cell});
         }
     }
     
     /**
      * Updates all selected cells and adds a border around them.
      */
     public void updateSelectedCells() {
         // get all currently selected cells
         Object[] selectedCells = 
                 drawLib.getGraphComponent().getGraph().getSelectionCells();
         
         // convert to list
         List<mxICell> currentCells 
             = new ArrayList<mxICell>(selectedCells.length);
             
         for (Object obj : selectedCells) {
             if (obj instanceof mxICell) {
                currentCells.add((mxICell) obj); 
             }
         }
         
         // build difference to old list to remove them from cellsToHDepth
         Set<mxICell> oldCells = cellsToHDepth.keySet();
         List<mxICell> removedCells = new ArrayList<mxICell>();
         
         for (mxICell cell : oldCells) {
             if (!currentCells.contains(cell)) {
                 removedCells.add(cell);
             }
         }
         
         // remove all cells that have been deselected
         for (mxICell cell : removedCells) {           
             getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKECOLOR,
                     mxUtils.getHexColorString(
                             highlightedCellsColor.get(cell)),
                     new Object[]{cell});
 
             getGraphAdapter().setCellStyles(mxConstants.STYLE_STROKEWIDTH,
                     highlightedCellsThickness.get(cell), 
                     new Object[]{cell});
             
             highlightedCellsThickness.remove(cell);
             highlightedCellsColor.remove(cell);
             cellsToHDepth.remove(cell);
         }
         
         
         // add new cells
         for (mxICell cell : currentCells) {
             // nothing to do: cell was already selected
             if (oldCells.contains(cell)) {
                 continue;
             }
             
             // add cell to hashmaps
             highlightedCellsThickness.put(cell, mxUtils.getString(
                     getGraphAdapter().getCellStyle(cell),
                    mxConstants.STYLE_STROKEWIDTH));
 
             highlightedCellsColor.put(cell, mxUtils.getColor(
                     getGraphAdapter().getCellStyle(cell),
                    mxConstants.STYLE_STROKECOLOR));
             
             cellsToHDepth.put(cell, new Integer(0));
             
             // highlight
             getGraphAdapter().setCellStyles(
                     mxConstants.STYLE_STROKECOLOR,
                     mxUtils.getHexColorString(selectionColor),
                     new Object[] {cell});
             getGraphAdapter().setCellStyles(
                     mxConstants.STYLE_STROKEWIDTH, HIGHLIGHTTHICKNESS,
                     new Object[] {cell});
         }
     }
 }
 
 /* EOF */
