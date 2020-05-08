 /*
  * The actual implementation of a DrawingLibraryInterface,
  * which is used to manipulate the canvas etc.
  *
  * $Header$
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.drawing;
 
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.JComponent;
 import javax.swing.JScrollBar;
 import javax.xml.transform.TransformerConfigurationException;
 
 import com.mxgraph.canvas.mxGraphics2DCanvas;
 import net.sf.epsgraphics.ColorMode;
 import net.sf.epsgraphics.EpsGraphics;
 
 import org.jgrapht.Graph;
 import org.jgrapht.ext.GraphMLExporter;
 import org.xml.sax.SAXException;
 
 import com.mxgraph.canvas.mxICanvas;
 import com.mxgraph.canvas.mxSvgCanvas;
 import com.mxgraph.model.mxICell;
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.swing.mxGraphOutline;
 import com.mxgraph.swing.handler.mxCellHandler;
 import com.mxgraph.swing.handler.mxPanningHandler;
 import com.mxgraph.util.mxCellRenderer;
 import com.mxgraph.util.mxCellRenderer.CanvasFactory;
 import com.mxgraph.util.mxConstants;
 import com.mxgraph.util.mxDomUtils;
 import com.mxgraph.util.mxUtils;
 import com.mxgraph.util.mxXmlUtils;
 import com.mxgraph.view.mxCellState;
 import com.mxgraph.view.mxGraph;
 
 /**
  * The actual implementation of a DrawingLibraryInterface,
  * which is used to manipulate the canvas etc.
  *
  * @param <V> Vertices
  * @param <E> Edges
  */
 class JGraphXInterface<V, E> implements DrawingLibraryInterface<V, E> {
 
     /** The actual canvas. */
     private mxGraphComponent graphComponent;
 
     /**
      * The minimap component.
      */
     private mxGraphOutline graphOutline;
     
     /** An interface to manipulate the canvas. */
     private GraphManipulation<V, E> graphManipulation;
     
     /** An simple interface to register 
      *  an mouse adapter. 
      */
     private GraphEvent graphEvent;
     
     /** An adapter, which transforms jgraphx in jgrapht 
      *  and vice versa.
      */
     private JGraphXAdapter<V, E> graphAdapter;
     
     /**
      * The constructor for JGraphXInterface.
      *
      * @param g JGraphT graph to draw
      */
 
     static
     {
         mxUtils.setDefaultTextRenderer(
                 lightweightLatexLabel.getSharedInstance());
         mxGraphics2DCanvas.putTextShape(mxGraphics2DCanvas.TEXT_SHAPE_DEFAULT, 
                 new mxDefaultLatexShape());
     }
 
     public JGraphXInterface(Graph<V, E> g) {
         // Convert to JGraphT-Graph
         graphAdapter = createNewAdapter(g);
         
         applyCustomGraphSettings();
 
         // Create the mxGraphComponent used to draw the graph
         // Also overrides the default behavior of JGraphX panning
         // implementation so the users are not required to hold down shift
         // and ctrl
         graphComponent = new mxGraphComponent(graphAdapter) {
             
             @Override
             public boolean isPanningEvent(MouseEvent event) {
                 if (event == null) {
                     return false;
                 } 
 
                 mxICell cell = (mxICell) getCellAt(event.getX(), event.getY());
                 return cell == null || cell.isEdge();
             }
             
             @Override
             public mxCellHandler createHandler(mxCellState state) {
                 // disable dashed lines around selection
                 return null;
             }
             
         };
         graphComponent.setToolTips(true);
 
         setGraphOutline();
         
         graphManipulation = new GraphManipulation<V, E>(this);
         
         graphManipulation.beginNotUndoable();
         
         graphEvent = new GraphEvent(graphComponent);
 
         graphComponent.setWheelScrollingEnabled(false);
         graphComponent.setAutoScroll(false);
         graphComponent.setCenterZoom(false);
         graphComponent.setConnectable(false);
 
         // enable backgroundcolors
         graphComponent.getViewport().setOpaque(true);
 
         graphEvent.registerMouseAdapter(
                 new InternalMouseAdapter<V, E>(graphComponent,
                         graphManipulation));
         
         graphManipulation.endNotUndoable();
 
         
         // change icon to grabbing hand if graph is panning
         new mxPanningHandler(graphComponent) {
             private List<V> selectedNodes = new ArrayList<V>();
             private long begin = 0;
             private final long epsilon = 150;
 
             @Override
             public void mousePressed(MouseEvent e) {
                 try {
                     Toolkit toolkit = Toolkit.getDefaultToolkit();
                     URL url = getClass().getResource("/icons/Grabbing.gif");
 
                     Image image = toolkit.getImage(url);
                     Cursor c = toolkit.createCustomCursor(image, new Point(
                             graphComponent.getX(), graphComponent.getY()),
                             "img");
 
                     graphComponent.getGraphControl().setCursor(c);
                 } catch (Exception ex) {
                     System.err.println("Unable to set cursor!");
                 }
                 
                 mxICell cell = (mxICell) graphComponent.getCellAt(
                         (int) e.getPoint().getX(),
                         (int) e.getPoint().getY());
                 if (cell != null && cell.isEdge()) {
                     return;
                 }
                 
                 
                 selectedNodes = getSelectedNodes();
                 begin  = e.getWhen();
                 
                 // set black border around cells
                 graphManipulation.updateSelectedCells();
                 
                 super.mousePressed(e);
             }
 
             @Override
             public void mouseReleased(MouseEvent e) {
                 graphComponent.getGraphControl().setCursor(
                         Cursor.getDefaultCursor());
                 /* if mouse was pressed longer than 150ms it's probably not a 
                  * click and is handled as a panning action */
                 if ((e.getWhen() - begin) > epsilon) {
                     setSelectedNodes(selectedNodes);
                 }
                 
                 graphManipulation.updateSelectedCells();
                 super.mouseReleased(e);
             }
         };
     }
 
     /**
      * Adds the graphoutline component and adds custom settings to it. 
      */
     private void setGraphOutline() {
 
         graphOutline = new ISGCImxGraphOutline(graphComponent);
         graphOutline.addMouseWheelListener(new MouseWheelListener() {
             
             @Override
             public void mouseWheelMoved(MouseWheelEvent e) {
                 if (e.getWheelRotation() < 0) {
                     graphManipulation.zoom(true);
                 } else {
                     graphManipulation.zoom(false);
                 }
             }
         });
         
         graphOutline.addMouseListener(new MouseListener() {
             
             @Override
             public void mouseReleased(MouseEvent e) { 
                 graphManipulation.applyZoomSettings(); 
             }
             
             @Override
             public void mousePressed(MouseEvent e) { 
                 graphManipulation.applyZoomSettings(); 
             }
             
             @Override
             public void mouseExited(MouseEvent e) { 
                 graphManipulation.applyZoomSettings(); 
             }
             
             @Override
             public void mouseEntered(MouseEvent e) { 
                 graphManipulation.applyZoomSettings(); 
             }
             
             @Override
             public void mouseClicked(MouseEvent e) { 
                 graphManipulation.applyZoomSettings(); 
             }
         });
     }
     
     /**
      * Creates a new JGraphXAdapter form the given Graph with edge selection
      * and movement disabled.
      *
      * @param g JGraphT graph
      * @return JGraphXAdapter
      */
     private JGraphXAdapter<V, E> createNewAdapter(Graph<V, E> g) {
         return new JGraphXAdapter<V, E>(g) {
             @Override
             public boolean isCellSelectable(Object cell) {
                 if (model.isEdge(cell)) {
                     return false;
                 }
                 return super.isCellSelectable(cell);
             }
 
             @Override
             public boolean isCellMovable(Object cell) {
                 if (model.isEdge(cell)) {
                     return false;
                 }
                 return super.isCellMovable(cell);
             }
         };
     }
 
     /**
      * Applies some custom settings to the graph.
      */
     private void applyCustomGraphSettings() {
         graphAdapter.setKeepEdgesInBackground(true);
         graphAdapter.setAllowDanglingEdges(false);
         graphAdapter.setAllowLoops(false);
         graphAdapter.setCellsDeletable(false);
         graphAdapter.setCellsDisconnectable(false);
         graphAdapter.setCellsBendable(false);
         graphAdapter.setCellsCloneable(false);
         graphAdapter.setCellsEditable(false);
         graphAdapter.setCellsResizable(false);
         graphAdapter.setVertexLabelsMovable(false);
         graphAdapter.setConnectableEdges(false);
         graphAdapter.setAutoSizeCells(true);
         graphAdapter.setDropEnabled(false);
 
         // disable edge labels
         graphAdapter.getStylesheet().getDefaultEdgeStyle()
                 .put(mxConstants.STYLE_NOLABEL, "1");
 
         graphAdapter.getStylesheet().getDefaultVertexStyle().
                 put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
         
         // set black outline around nodes
         graphAdapter.getStylesheet().getDefaultVertexStyle()
                 .put(mxConstants.STYLE_STROKECOLOR, "#000000");
 
         graphAdapter.getStylesheet().getDefaultVertexStyle()
                 .put(mxConstants.STYLE_SPACING, "5");
 
         // rounded edges
         graphAdapter.getStylesheet().getDefaultVertexStyle()
                 .put(mxConstants.STYLE_ROUNDED, "true");
     }
 
 
     @Override
     public final void export(final String format, final String path) {
         if (format.equals("eps")) {
             exportEPS(path);
         } else if (format.equals("svg")) {
             exportSVG(path);
         } else if (format.equals("graphml")) {
             exportGraphML(path);
         } else if (format.equals("jpg")) {
             exportJPG(path);
         } else if (format.equals("png")) {
             exportPNG(path);
         }
     }
 
     /**
      * Exports the canvas as an eps under the given path.
      *
      * @param path The path where the .eps file will be saved to
      */
     private void exportEPS(final String path) {
         Dimension d = graphComponent.getGraphControl().getSize();
         
         FileOutputStream out;
         EpsGraphics g;
         
         try {
             // Creates a new file and the graphics object
             out = new FileOutputStream(new File(path));
             g = new EpsGraphics("", out, 0, 0, (int) d.getWidth(), 
                     (int) d.getHeight(), ColorMode.COLOR_RGB);
             
             // Paints the graphic object
             graphComponent.getGraphControl().paint(g);
             
             g.finalize();
             g.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Exports the canvas as an svg under the given path.
      *
      * @param path The path where the .svg file will be saved to
      */
     private void exportSVG(final String path) {
         // Creates a new SVGCanvas and converts internal graph to svg
         mxSvgCanvas canvas = (mxSvgCanvas) mxCellRenderer.drawCells(
                 (mxGraph) this.graphAdapter, null, 1, null,
                 new CanvasFactory() {
                     public mxICanvas createCanvas(final int width,
                                                   final int height) {
                         mxSvgCanvas canvas = new mxSvgCanvas(
                                 mxDomUtils.createSvgDocument(width, height));
                         canvas.setEmbedded(true);
 
                         return canvas;
                     }
 
                 });
 
         try {
             // Saves the svg file under the given path
             mxUtils.writeFile(mxXmlUtils.getXml(canvas.getDocument()), path);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Exports the canvas as an GraphML under the given path.
      *
      * @param path The path where the .graphml file will be saved to
      */
     private void exportGraphML(final String path) {
         // Creates a new GraphMLExporter and gets the JGraphT-graph
         GraphMLExporter<V, E> exporter = new GraphMLExporter<V, E>();
         Graph<V, E> g = this.graphAdapter.getGraph();
 
         /*
          * FileWriter could throw an IOException, GraphMLExporter
          * TransformerConf.. and SAXException
          */
         try {
             // Creates a new Filewriter and exports the graph under the
             // given path
             FileWriter w = new FileWriter(path);
             exporter.export(w, g);
             w.close();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (TransformerConfigurationException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Exports the canvas as an jpg under the given path.
      *
      * @param path The path where the .jpg file will be saved to
      */
     private void exportJPG(final String path) {
         Dimension d = graphComponent.getSize();
         
         // For testing purposes, if no Panel exists 
         if (d.width == 0 || d.height == 0) {
             d.width = 1;
             d.height = 1;
         }
         
         // Gets the Scrollbars
         JScrollBar hor = graphComponent.getHorizontalScrollBar();
         JScrollBar vert = graphComponent.getVerticalScrollBar();
 
         // Calculates the height/width of the scrollbars
         // to remove them later on
         int height = hor.getHeight();
         int width = vert.getWidth();
         
         BufferedImage image = new BufferedImage(d.width - width, 
                 d.height - height, BufferedImage.TYPE_INT_RGB);
 
         Graphics2D g = image.createGraphics();
         graphComponent.paint(g);   
         
         File outputfile = new File(path);
 
         try {
             ImageIO.write(image, "jpg", outputfile);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Exports the canvas as an PNG under the given path.
      *
      * @param path The path where the .png file will be saved to
      */
     private void exportPNG(final String path) {
         Dimension d = graphComponent.getSize();
         
         // For testing purposes, if no Panel exists 
         if (d.width == 0 || d.height == 0) {
             d.width = 1;
             d.height = 1;
         }
         
         // Gets the Scrollbars
         JScrollBar hor = graphComponent.getHorizontalScrollBar();
         JScrollBar vert = graphComponent.getVerticalScrollBar();
 
         // Calculates the height/width of the scrollbars
         // to remove them later on
         int height = hor.getHeight();
         int width = vert.getWidth();
         
         BufferedImage image = new BufferedImage(d.width - width,
                 d.height - height, BufferedImage.TYPE_INT_ARGB);
 
         Graphics2D g = image.createGraphics();
         graphComponent.paint(g);
 
         File outputfile = new File(path);
 
         try {
             ImageIO.write(image, "png", outputfile);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public final String[] getAvailableExportFormats() {
         return new String[]{"eps", "svg", "graphml", "jpg", "png"};
     }
 
     @Override
     public final GraphEventInterface getGraphEventInterface() {
         return graphEvent;
     }
 
     @Override
     public final GraphManipulationInterface<V, E>
     getGraphManipulationInterface() {
         return graphManipulation;
     }
 
     @Override
     public final JComponent getPanel() {
         return graphComponent;
     }
 
     @Override
     public V getNodeAt(Point p) {
         mxICell cell = (mxICell) graphComponent.getCellAt((int) p.getX(),
                 (int) p.getY());
         if (cell != null && cell.isVertex()) {
             return graphAdapter.getCellToVertexMap().get(cell);
         } else {
             return null;
         }
     }
 
     @Override
     public E getEdgeAt(Point p) {
         mxICell cell = (mxICell) graphComponent.getCellAt((int) p.getX(),
                 (int) p.getY());
         if (cell != null && cell.isEdge()) {
             return graphAdapter.getCellToEdgeMap().get(cell);
         } else {
             return null;
         }      
     }
 
     @Override
     public Graph<V, E> getGraph() {
         return graphAdapter.getGraph();
     }
 
     @Override
     public void setGraph(Graph<V, E> g) {
         graphAdapter = createNewAdapter(g);
 
         applyCustomGraphSettings();
 
         graphComponent.setGraph(graphAdapter);
         
         graphManipulation = new GraphManipulation(this);
         
         graphManipulation.beginNotUndoable();
         graphManipulation.reapplyHierarchicalLayout();
         graphManipulation.endNotUndoable();
         graphOutline.setGraphComponent(graphComponent);
     }
 
     @Override
     public List<V> getSelectedNodes() {
         List<V> list = new ArrayList<V>(graphAdapter.getSelectionCount());
 
         for (Object cell : graphAdapter.getSelectionCells()) {
             list.add(graphAdapter.getCellToVertexMap().get(cell));
         }
         
         return list;
     }
 
     @Override
     public void setSelectedNodes(List<V> nodes) {
         graphComponent.getGraph().clearSelection();
         
         Collection<Object> col = new ArrayList<Object>(nodes.size());
 
         for (V node : nodes) {
             col.add(graphAdapter.getVertexToCellMap().get(node));
         }
 
         graphAdapter.setSelectionCells(col);
         graphManipulation.updateSelectedCells();
     }
 
     @Override
     public JComponent getGraphOutline() {
        return graphOutline;
     }
     
     /**
      * Returns the current mxGraphComponent.
      * 
      * @return
      *          The current mxGraphComponent
      */
     public mxGraphComponent getGraphComponent() {
         return graphComponent;
     }
 
     @Override
     public List<V> getVisibleNodes() {
         Object[] cells 
             = graphAdapter.getChildVertices(graphAdapter.getDefaultParent());
         
         HashMap<mxICell, V> cellToVertex = graphAdapter.getCellToVertexMap();
         List<V> visibleNodes = new ArrayList<V>(cellToVertex.size());
         
         for (Object cell : cells) {
             if (cell instanceof mxICell) {
                 visibleNodes.add(cellToVertex.get(cell));
             }
         }
         
         return visibleNodes;
     }
 }
 
 /* EOF */
