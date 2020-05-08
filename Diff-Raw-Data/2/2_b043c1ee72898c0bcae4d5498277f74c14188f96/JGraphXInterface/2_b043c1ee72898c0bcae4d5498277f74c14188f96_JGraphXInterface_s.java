 /*
  * Replace this line with a (multi-line) description of this file...
  *
  * $Header$
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 
 package teo.isgci.drawing;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 
 import javax.imageio.ImageIO;
 import javax.xml.transform.TransformerConfigurationException;
 
 import org.apache.batik.transcoder.TranscoderException;
 import org.apache.batik.transcoder.TranscoderInput;
 import org.apache.batik.transcoder.TranscoderOutput;
 import org.apache.fop.render.ps.EPSTranscoder;
 import org.jgrapht.Graph;
 import org.jgrapht.ext.GraphMLExporter;
 import org.xml.sax.SAXException;
 
 import teo.isgci.gui.LatexLabel;
 
 import com.mxgraph.canvas.mxICanvas;
 import com.mxgraph.canvas.mxSvgCanvas;
 import com.mxgraph.model.mxCell;
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.util.mxCellRenderer;
 import com.mxgraph.util.mxCellRenderer.CanvasFactory;
 import com.mxgraph.util.mxDomUtils;
 import com.mxgraph.util.mxUtils;
 import com.mxgraph.util.mxXmlUtils;
 import com.mxgraph.view.mxCellState;
 import com.mxgraph.view.mxGraph;
 
 /**
  * Dumbed down version of the original, WIP interface
  * TODO: replace this with the final one
  */
 class JGraphXInterface<V, E> implements DrawingLibraryInterface<V, E> {
 
     /** */
     private mxGraphComponent graphComponent;
     /** */
     private GraphManipulation<V, E> graphManipulation;
     /** */
     private GraphEvent graphEvent;
     /** */
     private JGraphXAdapter<V, E> graphAdapter;
 
     /**
      * The constructor for JGraphXInterface.
      *
      * @param g JGraphT graph to draw
      */
     public JGraphXInterface(Graph<V, E> g) {
 
         // Convert to JGraphT-Graph
         graphAdapter = createNewAdapter(g);
 
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
 
                 mxCell cell = (mxCell) getCellAt(event.getX(), event.getY());
 
                 return cell == null || cell.isEdge();
             }
 
             @Override
             public Component[] createComponents(mxCellState state) {
                 if (getGraph().getModel().isVertex(state.getCell())) {
                     String label = state.getLabel();
                     // get rid of these nasty [] around all labels
                     label = label.replace("[", "");
                     label = label.replace("]", "");
 
                     LatexLabel ll = new LatexLabel(label);
                     ll.setBackground(new Color(0, 0, 0, 0));
                     
                     return new Component[]{ ll };
                 }
                 return null;
             };
         };
         
         // make background white
         graphComponent.getViewport().setOpaque(true);
         graphComponent.getViewport().setBackground(Color.white);
 
         graphManipulation =
                 new GraphManipulation<V, E>(graphComponent);
         graphEvent = new GraphEvent(graphComponent);
 
         graphComponent.setWheelScrollingEnabled(false);
         graphComponent.setAutoScroll(false);
         graphComponent.setCenterZoom(false);
         graphComponent.setConnectable(false);
 
         graphEvent.registerMouseAdapter(
                 new InternalMouseAdapter(graphComponent, graphManipulation));
 
         graphManipulation.reapplyHierarchicalLayout();
 
         applyCustomGraphSettings();
     }
 
     /**
      * Creates a new JGraphXAdapter form the given Graph with edge selection and
      * movement disabled.
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
     }
 
     /**
      * Exports the current graph.
      *
      * @param format The actual format (.ps, .svg, .graphml)
      * @param path   The path where the graph will be exported to
      */
     @Override
     public final void export(final String format, final String path) {
         if (format == "eps") {
             exportEPS(path);
         } else if (format == "svg") {
             exportSVG(path);
         } else if (format == "graphml") {
             exportGraphML(path);
         } else if (format == "jpg") {
             exportJPG(path);
         } else if (format == "png") {
             exportPNG(path);
         }
     }
 
     /**
      * Exports the canvas as an eps under the given path, by converting an
      * existing .svg representation of it.
      *
      * @param path The path where the .eps file will be saved to
      */
     private void exportEPS(final String path) {
         // Creates the .svg file
         String temp = "temp.svg";
         exportSVG(temp);
 
         // Create the transcoder and set some settings
         EPSTranscoder transcoder = new EPSTranscoder();
 
         // Add Transcoding hints
         float MAX_HEIGHT_WIDTH = 16384f;
 
         transcoder.addTranscodingHint(
                 EPSTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 1.0f);
         transcoder.addTranscodingHint(EPSTranscoder.KEY_MAX_HEIGHT,
                 MAX_HEIGHT_WIDTH);
         transcoder.addTranscodingHint(EPSTranscoder.KEY_MAX_WIDTH,
                 MAX_HEIGHT_WIDTH);
 
         String svgURI;
         try {
             // Create the transcoder input.
             svgURI = new File(temp).toURI().toURL().toString();
             TranscoderInput input = new TranscoderInput(svgURI);
 
             // Create the transcoder output.
             OutputStream ostream = new FileOutputStream(path);
             TranscoderOutput output = new TranscoderOutput(ostream);
 
             // Save the image.
             transcoder.transcode(input, output);
 
             // Flush and close the stream.
             ostream.flush();
             ostream.close();
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (TranscoderException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         // Deletes the temp svg file
         File file = new File(temp);
         file.delete();
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
                         mxSvgCanvas canvas = new mxSvgCanvas(mxDomUtils
                                 .createSvgDocument(width, height));
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
             System.out.println("Enter a valid path !");
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
         Dimension d = graphComponent.getGraphControl().getSize();
 
         // For testing purposes, if no Panel exists 
         if (d.width == 0 || d.height == 0) {
             d.width = 1;
             d.height = 1;
         }
 
         BufferedImage image = new BufferedImage(d.width, d.height,
                 BufferedImage.TYPE_INT_RGB);
 
         Graphics2D g = image.createGraphics();
         graphComponent.getGraphControl().paint(g);
 
         final File outputfile = new File(path);
 
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
         Dimension d = graphComponent.getGraphControl().getSize();
 
         // For testing purposes, if no Panel exists 
         if (d.width == 0 || d.height == 0) {
             d.width = 1;
             d.height = 1;
         }
 
         BufferedImage image = new BufferedImage(d.width, d.height,
                 BufferedImage.TYPE_INT_ARGB);
 
         Graphics2D g = image.createGraphics();
         graphComponent.getGraphControl().paint(g);
 
         final File outputfile = new File(path);
 
         try {
             ImageIO.write(image, "png", outputfile);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Returns an Array of all currently implemented export formats.
      *
      * @return An array of String with the formats
      */
     @Override
     public final String[] getAvailableExportFormats() {
        return new String[]{"ps", "svg", "graphml", "jpg", "png"};
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
     public final mxGraphComponent getPanel() {
         return graphComponent;
     }
 
     /**
      * Returns the node located at the specified point
      *
      * @param p Location to look for a node
      * @return Node located at the given point or null if there is no node
      */
     @Override
     public V getNodeAt(Point p) {
         mxCell cell = (mxCell)graphComponent.getCellAt((int)p.getX(),
                 (int) p.getY());
         if(cell != null && cell.isVertex())
             return graphAdapter.getCellToVertexMap().get(cell);
         else
             return null;
     }
 
     /**
      * Returns the edge located at the specified point
      *
      * @param p Location to look for an edge
      * @return Edge located at the given point or null if there is no edge
      */
     @Override
     public E getEdgeAt(Point p) {
         mxCell cell = (mxCell)graphComponent.getCellAt((int)p.getX(),
                 (int) p.getY());
         if(cell != null && cell.isEdge())
             return graphAdapter.getCellToEdgeMap().get(cell);
         else
             return null;
     }
 
     /**
      * Set a new graph which should be drawn.
      * @param g The new graph
      */
     @Override
     public void setGraph(Graph<V, E> g) {
         graphAdapter = createNewAdapter(g);
         graphComponent.setGraph(graphAdapter);
 
         applyCustomGraphSettings();
 
         graphManipulation.reapplyHierarchicalLayout();
     }
 
     /**
      * Returns the current graph.
      *
      * @return the current graph
      */
     @Override
     public Graph<V, E> getGraph() {
         return graphAdapter.getGraph();
     }
 }
