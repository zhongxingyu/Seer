 package com.googlecode.whatswrong;
 
 
 import net.sf.epsgraphics.ColorMode;
 import net.sf.epsgraphics.EpsGraphics;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * An NLPCanvas is responsible for drawing the tokens and edges of an NLPInstance using different edge and token
  * layouts. In order to draw an NLPInstance clients have to first set the instance to draw by calling {@link
  * com.googlecode.whatswrong.NLPCanvas#setNLPInstance(NLPInstance)} and then update the graphical representation by
  * calling {@link NLPCanvas#updateNLPGraphics()}. The latter method should also be called whenever changes are made to
  * the layout configuration (curved edges vs straight edges, antialiasing etc.).
  *
  * @author Sebastian Riedel
  * @see com.googlecode.whatswrong.EdgeLayout
  * @see com.googlecode.whatswrong.TokenLayout
  */
 public class NLPCanvas extends JPanel {
 
 
     /**
      * Renderers for different render types.
      */
     private final HashMap<NLPInstance.RenderType, NLPCanvasRenderer>
         renderers = new HashMap<NLPInstance.RenderType, NLPCanvasRenderer>();
 
     /**
      * All tokens.
      */
     private ArrayList<Token> tokens = new ArrayList<Token>();
     /**
      * All edges.
      */
     private LinkedList<Edge> dependencies = new LinkedList<Edge>();
 
     /**
      * The image we render to.
      */
     private BufferedImage
         image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
 
     /**
      * A collection of all edge types used in the current nlp instance.
      */
     private Set<String> usedTypes = new HashSet<String>();
     /**
      * A collection of all token properties used in the current nlp instance.
      */
     private Set<TokenProperty>
         usedProperties = new java.util.HashSet<TokenProperty>();
 
     /**
      * The list of change listeners of this canvas.
      */
     private ArrayList<ChangeListener>
         changeListeners = new ArrayList<ChangeListener>();
 
     private NLPInstance instance;
 
     /**
      * The filter that processes the current instance before it is drawn.
      */
     private NLPInstanceFilter filter;
 
     /**
      * The renderer that draws the filtered NLPInstance to the canvas.
      */
     private NLPCanvasRenderer renderer = new SingleSentenceRenderer();
 
     /**
      * A NLPCanvas.Listener is notified whenever the canvas is redrawn or when a new instance has been set.
      */
     public interface Listener {
         /**
          * Called whenever the {@link NLPCanvas#setNLPInstance(NLPInstance)} method is called.
          */
         void instanceChanged();
 
         /**
          * Called whenever the {@link NLPCanvas#updateNLPGraphics()} method is called.
          */
         void redrawn();
     }
 
     /**
      * The set of listeners of this canvas.
      */
     private ArrayList<Listener> listeners = new ArrayList<Listener>();
 
     /**
      * Adds a new listener.
      *
      * @param listener the listener to add.
      */
     public void addListener(final Listener listener) {
         listeners.add(listener);
     }
 
     /**
      * Creates a new canvas with default size.
      */
     public NLPCanvas() {
         setPreferredSize(new Dimension(300, 300));
         setOpaque(true);
         renderers.put(NLPInstance.RenderType.single, renderer);
         renderers.put(NLPInstance.RenderType.alignment, new AlignmentRenderer());
         addMouseListener(new MouseAdapter() {
             /**
              * Selects edges in the dependency layout.
              * @param e the event.
              */
             public void mousePressed(MouseEvent e) {
 //        Point point = e.getPoint();
 //        point.translate(0, -(getHeight() - tokenLayout.getHeight() -
 //          dependencyLayout.getHeight() - spanLayout.getHeight()));
 //        Edge edge = dependencyLayout.getEdgeAt(point, 5);
 //        //System.out.println("edge = " + edge);
 //        if (edge != null) {
 //          if (e.isMetaDown())
 //            dependencyLayout.toggleSelection(edge);
 //          else
 //            dependencyLayout.select(edge);
 //
 //          updateNLPGraphics();
 //        }
             }
         });
     }
 
     /**
      * Return the renderer that draws the NLPInstance onto this canvas.
      *
      * @return the renderer that draws the NLPInstance onto this canvas.
      */
     public NLPCanvasRenderer getRenderer() {
         return renderer;
     }
 
 
     /**
      * Adds a change listener to this canvas.
      *
      * @param changeListener the listener to add.
      */
     public void addChangeListenger(ChangeListener changeListener) {
         changeListeners.add(changeListener);
     }
 
     /**
      * Fired whenever this canvas is changed.
      */
     private void fireChanged() {
         ChangeEvent event = new ChangeEvent(this);
         for (ChangeListener changeListener : changeListeners) {
             changeListener.stateChanged(event);
         }
     }
 
     /**
      * Notifies all listeners about an instance change event.
      */
     private void fireInstanceChanged() {
         for (Listener l : listeners) l.instanceChanged();
     }
 
     /**
      * Notifies all listeners about a redraw/update event.
      */
     private void fireRedrawn() {
         for (Listener l : listeners) l.redrawn();
     }
 
     /**
      * Sets the current NLP instance to draw. Note that this does not cause to canvas to be immediately updated. For this
      * {@link NLPCanvas#updateNLPGraphics()} needs to be called.
      *
      * @param nlpInstance the new NLP instance.
      */
     public void setNLPInstance(final NLPInstance nlpInstance) {
         this.instance = nlpInstance;
         dependencies.clear();
         dependencies.addAll(nlpInstance.getEdges());
         usedTypes.clear();
         for (Edge edge : dependencies)
             usedTypes.add(edge.getType());
         tokens.clear();
         tokens.addAll(nlpInstance.getTokens());
         usedProperties.clear();
         for (Token token : tokens) {
             usedProperties.addAll(token.getPropertyTypes());
         }
         //spanLayout.clearSelection();
         fireInstanceChanged();
         //updateNLPGraphics();
     }
 
     /**
      * Returns the set of all token properties in the current nlp instance.
      *
      * @return the set of all token properties in the current nlp instance.
      */
     public Set<TokenProperty> getUsedProperties() {
         return Collections.unmodifiableSet(usedProperties);
     }
 
     /**
      * Returns the set of all edge types in the current nlp instance.
      *
      * @return the set of all edge types in the current nlp instance.
      */
     public Set<String> getUsedTypes() {
         return Collections.unmodifiableSet(usedTypes);
     }
 
 
     /**
      * Returns the filter this canvas is applying to the nlp instance before it is drawn.
      *
      * @return the filter of this canvas.
      */
     public NLPInstanceFilter getFilter() {
         return filter;
     }
 
     /**
      * Sets the filter this canvas should apply to the nlp instance before it is drawn.
      *
      * @param filter the filter to use.
      */
     public void setFilter(NLPInstanceFilter filter) {
         this.filter = filter;
     }
 
     /**
      * Just calls the filter on the current instance.
      *
      * @return the filtered instance.
      */
     private NLPInstance filterInstance() {
         return filter.filter(new NLPInstance(tokens, dependencies,
             instance.getRenderType(), instance.getSplitPoints()));
     }
 
     /**
      * Updates the current graph. This takes into account all changes to the filter, NLP instance and drawing parameters.
      */
     public void updateNLPGraphics() {
         NLPInstance filtered = filterInstance();
 
        Graphics2D graphics = image.createGraphics();
 
         renderer = renderers.get(filtered.getRenderType());
 
        Dimension dim = renderer.render(filtered, graphics);
 
         image = new BufferedImage((int) dim.getWidth(),
             (int) dim.getHeight(),
             BufferedImage.TYPE_4BYTE_ABGR);
 
        graphics = image.createGraphics();
 
        renderer.render(filtered, graphics);
         setPreferredSize(dim);
         setMinimumSize(dim);
         setSize(new Dimension(dim.width, getHeight()));
         repaint();
         invalidate();
         fireChanged();
         fireRedrawn();
     }
 
 
     /**
      * Clears the current instance.
      */
     public void clear() {
         tokens.clear();
         dependencies.clear();
         usedTypes.clear();
         usedProperties.clear();
     }
 
 
     /**
      * Paint the canvas to the graphics object.
      *
      * @param graphics the graphics object to draw to.
      */
     public void paintComponent(Graphics graphics) {
         Graphics2D g2d = (Graphics2D) graphics;
         g2d.setColor(Color.WHITE);
         g2d.fillRect(0, 0, getWidth(), getHeight());
         int y = getHeight() - image.getHeight();
         //g2d.drawImage(dependencyImage, 0, y, this);
         g2d.drawImage(image, 0, y, this);
         //g2d.drawImage(spanImage, 0, y + image.getHeight() + dependencyImage.getHeight(), this);
     }
 
 
     /**
      * Exports the current graph to EPS.
      *
      * @param file the eps file to export to.
      * @throws IOException if IO goes wrong.
      */
     public void exportToEPS(File file) throws IOException {
 
         EpsGraphics dummy = new EpsGraphics("Title", new ByteArrayOutputStream(),
             0, 0, 1, 1, ColorMode.BLACK_AND_WHITE);
 
         NLPInstance filtered = filterInstance();
 
         Dimension dim = renderer.render(filtered, dummy);
 
         EpsGraphics g = new EpsGraphics("Title", new FileOutputStream(file), 0, 0,
             (int) dim.getWidth() + 2, (int) dim.getHeight(), ColorMode.COLOR_RGB);
 
         renderer.render(filtered, g);
 
         g.flush();
         g.close();
     }
 }
