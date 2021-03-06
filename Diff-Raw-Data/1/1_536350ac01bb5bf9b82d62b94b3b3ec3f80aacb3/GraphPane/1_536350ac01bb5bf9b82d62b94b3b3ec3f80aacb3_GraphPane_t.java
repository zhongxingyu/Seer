 /*
  *    Copyright 2010-2011 University of Toronto
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package savant.view.swing;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import javax.swing.*;
 
 import com.jidesoft.popup.JidePopup;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import savant.controller.GraphPaneController;
 import savant.controller.Listener;
 import savant.controller.LocationController;
 import savant.controller.event.GraphPaneEvent;
 import savant.data.event.ExportEvent;
 import savant.data.event.ExportEventListener;
 import savant.data.event.PopupEvent;
 import savant.data.event.PopupEventListener;
 import savant.data.types.GenericContinuousRecord;
 import savant.data.types.Record;
 import savant.exception.RenderingException;
 import savant.selection.PopupThread;
 import savant.selection.PopupPanel;
 import savant.settings.ColourSettings;
 import savant.swing.component.ProgressPanel;
 import savant.util.*;
 import savant.view.dialog.BAMParametersDialog;
 import savant.view.swing.continuous.ContinuousTrackRenderer;
 import savant.view.swing.interval.BAMTrack;
 
 /**
  *
  * @author mfiume
  */
 public class GraphPane extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener {
 
     private static final Log LOG = LogFactory.getLog(GraphPane.class);
 
     private Track[] tracks;
     private Frame parentFrame;
 
     private int mouse_x = 0;
     private int mouse_y = 0;
 
     /** min / max axis values */
     private int xMin;
     private int xMax;
     private int yMin;
     private int yMax;
     private double unitWidth;
     private double unitHeight;
 
     private boolean isOrdinal = false;
     private boolean isYGridOn = true;
     private boolean isXGridOn = true;
     private boolean mouseInside = false;
 
     // Locking
     private boolean isLocked = false;
     private Range lockedRange;
 
     /** Selection Variables */
     private Rectangle selectionRect = new Rectangle();
     private boolean isDragging = false;
 
     //scrolling...
     private BufferedImage bufferedImage;
     private Range prevRange = null;
     private DrawingMode prevMode = null;
     private Dimension prevSize = null;
     private String prevRef = null;
     public boolean paneResize = false;
     public int newHeight;
     private int oldWidth = -1;
     private int oldHeight = -1;
     private int oldViewHeight = -1;
     private int newScroll = 0;
     private boolean renderRequired = false;
     //private int buffTop = -1;
     //private int buffBottom = -1;
     private int posOffset = 0;
     private boolean forcedHeight = false;
 
     //dragging
     private int startX;
     private int startY;
     private int baseX;
     private int initialScroll;
     private boolean panVert = false;
 
     //popup
     public Thread popupThread;
     public JidePopup jp = new JidePopup();
     private Record currentOverRecord = null;
     private Shape currentOverShape = null;
     private boolean popupVisible = false;
     private JPanel popPanel;
     //private boolean mouseWheel = false; //used by popupThread
 
     private JLabel yMaxPanel;
 
     /**
      * Provides progress indication when loading a track.
      */
     private ProgressPanel progressPanel;
 
     // mouse and key modifiers
     private enum mouseModifier { NONE, LEFT, MIDDLE, RIGHT };
     private mouseModifier mouseMod = mouseModifier.LEFT;
 
     private enum keyModifier { DEFAULT, CTRL, SHIFT, META, ALT; };
     private keyModifier keyMod = keyModifier.DEFAULT;
 
     //awaiting exported images
     private final List<ExportEventListener> exportListeners = new ArrayList<ExportEventListener>();
     private final List<PopupEventListener> popupListeners = new ArrayList<PopupEventListener>();
 
     /**
      * CONSTRUCTOR
      */
     public GraphPane(Frame parent) {
         this.parentFrame = parent;
         addMouseListener(this); // listens for own mouse and
         addMouseMotionListener( this ); // mouse-motion events
         //addKeyListener( this );
         getInputMap().allKeys();
         addMouseWheelListener(this);
 
         popupThread = new Thread(new PopupThread(this));
         popupThread.start();
         
         GraphPaneController.getInstance().addListener(new Listener<GraphPaneEvent>() {
             @Override
             public void handleEvent(GraphPaneEvent event) {
                 parentFrame.resetLayers();
             }
 
         });
     }
 
     /**
      * Set the tracks to be displayed in this GraphPane
      *
      * @param tracks an array of Track objects to be added
      */
     public void setTracks(Track[] tracks) {
         this.tracks = tracks;
         setIsOrdinal(tracks[0].getRenderer().isOrdinal());
         setYRange(tracks[0].getRenderer().getDefaultYRange());
     }
 
     /**
      * Return the list of tracks associated with this GraphPane.
      */
     public Track[] getTracks() {
         return tracks;
     }
 
     /**
      * Render the contents of the GraphPane. Includes drawing a common
      * background for all tracks.
      *
      * @param g The Graphics object into which to draw.
      */
     public void render(Graphics g) {
         render(g, new Range(xMin, xMax), null);
     }
 
     public void render(Graphics g, Range xRange, Range yRange) {
         LOG.debug("GraphPane.render(g, " + xRange + ", " + yRange + ")");
         double oldUnitHeight = unitHeight;
         int oldYMax = yMax;
 
 
         Graphics2D g2d0 = (Graphics2D)g;
 
         // Paint a gradient from top to bottom
         GradientPaint gp0 = new GradientPaint(0, 0, ColourSettings.getGraphPaneBackgroundTop(), 0, getHeight(), ColourSettings.getGraphPaneBackgroundBottom());
         g2d0.setPaint( gp0 );
         g2d0.fillRect( 0, 0, getWidth(), getHeight() );
 
         GraphPaneController gpc = GraphPaneController.getInstance();
 
         if (gpc.isPanning() && !isLocked()) {
 
             int fromx = MiscUtils.transformPositionToPixel(gpc.getMouseDragRange().getFrom(), getWidth(), new Range(xMin, xMax));
             int tox = MiscUtils.transformPositionToPixel(gpc.getMouseDragRange().getTo(), getWidth(), new Range(xMin, xMax));
 
             double pixelshiftamount = tox-fromx;
             g.translate((int) pixelshiftamount, 0);
         }
 
         // Deal with the progress-bar.
         if (tracks == null) {
             showProgress("Creating track...");
             return;
         } else {
             for (Track t: tracks) {
                 if (t.getRenderer().isWaitingForData()) {
                     String progressMsg = (String)t.getRenderer().getInstruction(DrawingInstruction.PROGRESS);
                     setPreferredSize(new Dimension(getWidth(), 0));
                     showProgress(progressMsg);
                     return;
                 }
             }
         }
         if (progressPanel != null) {
             remove(progressPanel);
             progressPanel = null;
         }
 
         int minYRange = Integer.MAX_VALUE;
         int maxYRange = Integer.MIN_VALUE;
         isYGridOn = false;
         for (Track t: tracks) {
 
             // ask renderers for extra info on range; consolidate to maximum Y range
             AxisRange axisRange = (AxisRange)t.getRenderer().getInstruction(DrawingInstruction.AXIS_RANGE);
 
             if (axisRange != null) {
                 int axisYMin = axisRange.getYMin();
                 int axisYMax = axisRange.getYMax();
                 if (axisYMin < minYRange) minYRange = axisYMin;
                 if (axisYMax > maxYRange) maxYRange = axisYMax;
             }
 
             // Ask renderers if they want horizontal lines; if any say yes, draw them.
             isYGridOn |= t.getRenderer().hasHorizontalGrid();
         }
 
         setXRange(xRange);
         Range consolidatedYRange = new Range(minYRange, maxYRange);
         setYRange(consolidatedYRange);
 
         yMin = minYRange;
         yMax = maxYRange;
 
         DrawingMode currentMode = tracks[0].getDrawingMode();
 
         boolean sameRange = (prevRange != null && xRange.equals(prevRange));
         boolean sameMode = currentMode == prevMode;
         boolean sameSize = prevSize != null && getSize().equals(prevSize) && parentFrame.getFrameLandscape().getWidth() == oldWidth && getParentFrame().getFrameLandscape().getHeight() == oldHeight;
         boolean sameRef = prevRef != null && LocationController.getInstance().getReferenceName().equals(prevRef);
         boolean withinScrollBounds = bufferedImage != null && getVerticalScrollBar().getValue() >= getOffset() && getVerticalScrollBar().getValue() < getOffset() + getViewportHeight() * 2;
 
         //bufferedImage stores the current graphic for future use. If nothing
         //has changed in the track since the last render, bufferedImage will
         //be used to redraw the current view. This method allows for fast repaints
         //on tracks where nothing has changed (panning, selection, plumbline,...)
 
         //if nothing has changed draw buffered image
         if(sameRange && sameMode && sameSize && sameRef && !renderRequired && withinScrollBounds){
             g.drawImage(bufferedImage, 0, getOffset(), this);
             renderCurrentSelected(g);
 
             //force unitHeight from last render
             unitHeight = oldUnitHeight;
             yMax = oldYMax;
 
         } else {
             // Otherwise prepare for new render.
             renderRequired = false;
 
             int h = getHeight();
             if (!forcedHeight) {
                 h = Math.min(h, getViewportHeight() * 3);
             }
             bufferedImage = new BufferedImage(getWidth(), h, BufferedImage.TYPE_INT_RGB);
             if (bufferedImage.getHeight() == getHeight()){
                 setOffset(0);
             } else {
                 setOffset(getVerticalScrollBar().getValue() - getViewportHeight());
             }
             LOG.trace("Rendering fresh " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight() + " bufferedImage at (0, " + getOffset() + ")");
 
             Graphics2D g3 = bufferedImage.createGraphics();
             g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             prevRange = LocationController.getInstance().getRange();
             prevSize = this.getSize();
             prevMode = tracks[0].getDrawingMode();
             prevRef = LocationController.getInstance().getReferenceName();
 
             renderBackground(g3);            
 
             // Call the actual render() methods.
             boolean nothingRendered = true;
             String message = null;
             for (Track t: tracks) {
                 if(nothingRendered){
                     setYMaxVisible(true);
                 }
                 // Change renderers' drawing instructions to reflect consolidated YRange
                 t.getRenderer().addInstruction(DrawingInstruction.AXIS_RANGE, AxisRange.initWithRanges(xRange, consolidatedYRange));
                 try {
                     t.getRenderer().render(g3, this);
                     nothingRendered = false;
                 } catch (RenderingException rx) {
                     if(nothingRendered){
                         setYMaxVisible(false);
                     }
                     if (message == null) {
                         message = rx.getMessage();
                     }
                 }
             }
             if (nothingRendered && message != null) {
                 setPreferredSize(new Dimension(getWidth(), 0));
                revalidate();
                 drawMessage(g3, message);
             }
 
             updateYMax();
             renderSides(g3);
 
             //if a change has occured that affects scrollbar...
             if (paneResize) {
                 paneResize = false;
 
                 //get old scroll position
                 int oldScroll = getVerticalScrollBar().getValue();
                 int oldBottomHeight = oldHeight - oldScroll - oldViewHeight;
                 int newViewHeight = getViewportHeight();
 
                 //change size of current frame
                 Frame frame = this.getParentFrame();
                 frame.getFrameLandscape().setPreferredSize(new Dimension(this.getWidth(), newHeight));
                 this.setPreferredSize(new Dimension(frame.getFrameLandscape().getWidth(), newHeight));
                 frame.getFrameLandscape().setSize(new Dimension(frame.getFrameLandscape().getWidth(), newHeight));
                 this.setSize(new Dimension(frame.getFrameLandscape().getWidth(), newHeight));
                 this.revalidate();
 
                 //scroll so that bottom matches previous view
                 newScroll = newHeight - newViewHeight - oldBottomHeight;
                 oldViewHeight = newViewHeight;
 
                 return; // new Dimension(frame.getFrameLandscape().getWidth(), newHeight);
 
             } else if (oldViewHeight != getViewportHeight()) {
                 int newViewHeight = getViewportHeight();
                 int oldScroll = getVerticalScrollBar().getValue();
                 newScroll = oldScroll + (oldViewHeight - newViewHeight);
                 oldViewHeight = newViewHeight;
             }
 
             if (newScroll != -1){
                 getVerticalScrollBar().setValue(newScroll);
                 newScroll = -1;
             }
 
             oldWidth = getParentFrame().getFrameLandscape().getWidth();
             oldHeight = getParentFrame().getFrameLandscape().getHeight();
 
             g.drawImage(bufferedImage, 0, getOffset(), this);
             fireExportReady(xRange, bufferedImage);
 
             renderCurrentSelected(g);
             parentFrame.redrawSidePanel();
         }
 
         return;
 
     }
 
     /**
      * Get the height of the viewport.  The viewport is the grandparent of this GraphPane.
      */
     private int getViewportHeight() {
         return getParent().getParent().getHeight();
     }
 
     /**
      * Access to the scrollbar associated with this GraphPane.  The JScrollPane is our great-grandparent.
      */
     private JScrollBar getVerticalScrollBar() {
         return ((JScrollPane)getParent().getParent().getParent()).getVerticalScrollBar();
     }
 
     private void renderCurrentSelected(Graphics g){
         //temporarily shift the origin
         Graphics2D g2 = (Graphics2D) g;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2.translate(0, getOffset());
         for (Track t: tracks) {
             if (t.getRenderer().hasMappedValues()) {
                 List<Shape> currentSelected = t.getRenderer().getCurrentSelectedShapes(this);
                 boolean arcMode = t.getDrawingMode() == DrawingMode.ARC_PAIRED;
                 for (Shape selectedShape: currentSelected) {
                     if (selectedShape != currentOverShape) {
                         if (arcMode) {
                             g2.setColor(Color.GREEN);
                             g2.draw(selectedShape);
                         } else {
                             //g2.setColor(Color.GREEN);
                             g2.setColor(new Color(0,255,0,150));
                             g2.fill(selectedShape);
                             if (selectedShape.getBounds().getWidth() > 5){
                                 g2.setColor(Color.BLACK);
                                 g2.draw(selectedShape);
                             }
                         }
                     }
                 }
                 break;
             }
         }
         if (currentOverShape != null) {
             if (tracks[0].getDrawingMode() == DrawingMode.ARC_PAIRED) {
                 g.setColor(Color.RED);
                 ((Graphics2D)g).draw(currentOverShape);
             } else {
                 g.setColor(new Color(255,0,0,150));
                 ((Graphics2D) g).fill(currentOverShape);
                 if (currentOverShape.getBounds() != null && currentOverShape.getBounds().getWidth() > 5 && currentOverShape.getBounds().getHeight() > 3) {
                     g.setColor(Color.BLACK);
                     ((Graphics2D)g).draw(currentOverShape);
                 }
             }
         }
         //shift the origin back
         g2.translate(0, -getOffset());
     }
 
     public void setRenderForced(){
         renderRequired = true;
     }
 
     /**
      * Call before a repaint to override bufferedImage repainting
      */
     public void setRenderForced(boolean b){
         renderRequired = b;
     }
 
     public boolean isRenderForced() {
         return renderRequired;
     }
 
     /**
      * Force the bufferedImage to contain entire height at current range.  Intended
      * for creating images for track export.  Make sure you unforce immediately after!
      */
     public void forceFullHeight(){
         forcedHeight = true;
     }
 
     public void unforceFullHeight(){
         forcedHeight = false;
     }
 
     private void setOffset(int offset){
         posOffset = offset;
     }
 
     public int getOffset(){
         return posOffset;
     }
 
     public void setPaneResize(boolean resized){
         paneResize = resized;
     }
 
     int rendercount = 0;
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         render(g);
 
         GraphPaneController gpc = GraphPaneController.getInstance();
 
         /* AIMING ADJUSTMENTS */
         if (gpc.isAiming() && mouseInside) {
             g.setColor(Color.BLACK);
             Font thickfont = new Font("Arial", Font.BOLD, 15);
             g.setFont(thickfont);
             int genome_x = gpc.getMouseXPosition();
             int genome_y = gpc.getMouseYPosition();
             String target = "";
             target += "X: " + MiscUtils.numToString(genome_x);
             target += (genome_y == -1) ? "" : " Y: " + MiscUtils.numToString(genome_y);
 
             g.drawLine(mouse_x, 0, mouse_x, this.getHeight());
             if (genome_y != -1) g.drawLine(0, mouse_y, this.getWidth(), mouse_y);
             g.drawString(target,
                     mouse_x + 5,
                     mouse_y - 5);
         }
 
         int x1 = MiscUtils.transformPositionToPixel(gpc.getMouseDragRange().getFrom(), this.getWidth(), new Range(this.xMin, this.xMax));
         int x2 = MiscUtils.transformPositionToPixel(gpc.getMouseDragRange().getTo(), this.getWidth(), new Range(this.xMin, this.xMax));
 
         int width = x1 - x2;
         int height = this.getHeight();// this.y1 - this.y2;
 
         selectionRect.width = Math.max(2 ,Math.abs( width ));
         selectionRect.height = Math.abs( height );
         selectionRect.x = width < 0 ? x1 : x2;
         selectionRect.y = 0; //height < 0 ? this.y1 : this.y2;
 
         /* PANNING ADJUSTMENTS */
         if (gpc.isPanning()) {}
 
         /* ZOOMING ADJUSTMENTS */
         else if (gpc.isZooming() || gpc.isSelecting()) {
 
             Graphics2D g2d = (Graphics2D)g;
 
             Rectangle2D rectangle = new Rectangle2D.Double(selectionRect.x, selectionRect.y - 10, selectionRect.width, selectionRect.height + 10);
             g2d.setColor (Color.gray);
             g2d.setStroke (new BasicStroke(
               1f,
               BasicStroke.CAP_ROUND,
               BasicStroke.JOIN_ROUND,
               3f,
               new float[] {4f},
               4f));
             g2d.draw(rectangle);
 
             if (gpc.isZooming()) {
                 g.setColor(ColourSettings.getGraphPaneZoomFill());
             } else if (gpc.isSelecting()) {
                 g.setColor(ColourSettings.getGraphPaneSelectionFill());
             }
             g.fillRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
         }
 
         /* PLUMBING ADJUSTMENTS */
         if (gpc.isPlumbing()) {
             g.setColor(Color.BLACK);
             int spos = MiscUtils.transformPositionToPixel(GraphPaneController.getInstance().getMouseXPosition(), this.getWidth(), this.getHorizontalPositionalRange());
             g.drawLine(spos, 0, spos, this.getHeight());
             int rpos = MiscUtils.transformPositionToPixel(GraphPaneController.getInstance().getMouseXPosition()+1, this.getWidth(), this.getHorizontalPositionalRange());
             g.drawLine(rpos, 0, rpos, this.getHeight());
         }
 
         /* SPOTLIGHT */
         if (gpc.isSpotlight() && !gpc.isZooming()) {
 
             int center = gpc.getMouseXPosition();
             int left = center - gpc.getSpotlightSize()/2;
             int right = left + gpc.getSpotlightSize();
 
             g.setColor(new Color(0,0,0,200));
 
             int xt = MiscUtils.transformPositionToPixel(left, this.getWidth(), this.getHorizontalPositionalRange());
             int yt = MiscUtils.transformPositionToPixel(right, this.getWidth(), this.getHorizontalPositionalRange());
 
             // draw left of spotlight
             if (left >= this.getHorizontalPositionalRange().getFrom()) {
                 g.fillRect(0, 0, MiscUtils.transformPositionToPixel(left, this.getWidth(), this.getHorizontalPositionalRange()), this.getHeight());
             }
             // draw right of spotlight
             if (right <= this.getHorizontalPositionalRange().getTo()) {
                 int pix = MiscUtils.transformPositionToPixel(right, this.getWidth(), this.getHorizontalPositionalRange());
                 g.fillRect(pix, 0, this.getWidth()-pix, this.getHeight());
             }
         }
 
         if (isLocked()) {
             drawMessage((Graphics2D)g, "Locked");
         }
 
         GraphPaneController.getInstance().delistRenderingGraphpane(this);
     }
 
     /**
      * Render the sides of this GraphPane
      * @param g The graphics object to use
      */
     private void renderSides(Graphics g) {
         //Color c = Color.black;
         //this.setBackground(c);
         g.setColor(this.getBackground());
         int w = this.getWidth();
         int h = this.getHeight();
         int mult = 1;
         g.fillRect(w, 0, w*mult, h);
         g.fillRect(-w*mult, 0, w*mult, h);
     }
 
     /**
      * Render the background of this GraphPane
      * @param g The graphics object to use
      */
     public void renderBackground(Graphics g) {
 
         Graphics2D g2 = (Graphics2D) g;
 
         Graphics2D g2d0 = (Graphics2D)g;
 
             // Paint a gradient from top to bottom
             GradientPaint gp0 = new GradientPaint(
                 0, 0, ColourSettings.getGraphPaneBackgroundTop(),
                 0, this.getHeight(), ColourSettings.getGraphPaneBackgroundBottom());
 
             g2d0.setPaint( gp0 );
             g2d0.fillRect( 0, 0, this.getWidth(), this.getHeight() );
 
         if (this.isXGridOn) {
 
             int numseparators = (int) Math.max(Math.ceil(Math.log(xMax-xMin)),2);
 
             if (numseparators != 0) {
                 int width = this.getWidth();
                 double pixelSeparation = width / numseparators;
                 int genomicSeparation = (xMax-xMin)/numseparators;
 
                 int startbarsfrom = MiscUtils.transformPositionToPixel(
                     (int) (Math.floor(LocationController.getInstance().getRange().getFrom()/Math.max(1, genomicSeparation))*genomicSeparation),
                     width, (LocationController.getInstance()).getRange());
 
                 g2.setColor(ColourSettings.getAxisGrid());
                 for (int i = 0; i <= numseparators; i++) {
                     g2.drawLine(startbarsfrom + (int)Math.ceil(i*pixelSeparation)+1, this.getHeight(), startbarsfrom + (int) Math.ceil(i*pixelSeparation)+1, 0);
                 }
             }
         }
 
         if (this.isYGridOn) {
 
             int numseparators = (int) Math.ceil(Math.log(yMax-yMin));
 
 
 
             if (numseparators != 0) {
                 int height = this.getHeight();
                 double separation = height / numseparators;
 
                 g2.setColor(ColourSettings.getAxisGrid());
                 for (int i = 0; i <= numseparators; i++) {
                     g2.drawLine(0, (int)Math.ceil(i*separation)+1, this.getWidth(), (int) Math.ceil(i*separation)+1);
                 }
 
             }
         }
     }
 
     /**
      * Set the graph units for the horizontal axis
      *
      * @param r an X range
      */
     public void setXRange(Range r) {
         if (r == null) {
             return;
         }
 
         //Savant.log("Setting x range to " + r);
 
         this.xMin = r.getFrom();
         this.xMax = r.getTo();
         setUnitWidth();
     }
 
     public Range getXRange() {
         return new Range(xMin, xMax);
     }
 
     /**
      * Set the graph units for the vertical axis
      *
      * @param r a Y range
      */
     public void setYRange(Range r) {
 
         if (r == null) {
             return;
         }
         if (this.isOrdinal) {
             return;
         }
 
         //Savant.log("Setting y range to " + r);
 
         this.yMin = r.getFrom();
         this.yMax = r.getTo();
         setUnitHeight();
     }
 
     /**
      * Set the pane's vertical coordinate system to be 0-1
      *
      * @param b true for ordinal, false otherwise.
      */
     public void setIsOrdinal(boolean b) {
         this.isOrdinal = b;
         if (this.isOrdinal) {
             // don't call setYRange, because it's just going to return without doing anything
             this.yMin = 0;
             this.yMax = 1;
             setUnitHeight();
         }
     }
 
     /**
      *
      * @return  the number of pixels equal to one graph unit of width.
      */
     public double getUnitWidth() {
         return this.unitWidth;
     }
 
     /**
      * Transform a graph width into a pixel width
      *
      * @param len width in graph units
      * @return corresponding number of pixels
      */
     public double getWidth(int len) {
         return this.unitWidth * len;
     }
 
     /**
      *
      * @return the number of pixels equal to one graph unit of height.
      */
     public double getUnitHeight() {
         return this.unitHeight;
     }
 
     /**
      * Transform a graph height into a pixel height
      *
      * @param len height in graph units
      * @return corresponding number of pixels
      */
     public double getHeight(int len) {
         return this.unitHeight * len;
     }
 
     /**
      * Transform a horizontal position in terms of graph units into a drawing coordinate
      *
      * @param pos position in graph coordinates
      * @return a corresponding drawing coordinate
      */
     public double transformXPos(int pos) {
         pos -= xMin;
         return pos * getUnitWidth();
     }
 
     /**
      * Transform a vertical position in terms of graph units into a drawing coordinate
      *
      * @param pos position in graph coordinates
      * @return a corresponding drawing coordinate
      */
     public double transformYPos(double pos) {
         pos = pos - this.yMin;
         return this.getHeight() - (pos * getUnitHeight());
     }
 
     /**
      * Set the number of pixels equal to one graph unit of width.
      */
     public void setUnitWidth() {
         Dimension d = this.getSize();
         unitWidth = (double) d.width / (xMax - xMin + 1);
     }
 
     /**
      * Set the number of pixels equal to one graph unit of height.
      */
     public void setUnitHeight() {
         Dimension d = this.getSize();
         unitHeight = (double) d.height / (yMax - yMin);
     }
 
     /**
      * Set the number of pixels equal to one graph unit of height.
      */
     public void setUnitHeight(int height) {
         unitHeight = height;
     }
 
     public boolean isOrdinal() {
         return this.isOrdinal;
     }
 
 
     /**
      * MOUSE EVENT LISTENER
      */
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void mouseWheelMoved(MouseWheelEvent e) {
 
         //this.setMouseWheel(true);
         int notches = e.getWheelRotation();
 
         if (MiscUtils.MAC && e.isMetaDown() || e.isControlDown()) {
             if (notches < 0) {
                 LocationController lc = LocationController.getInstance();
                 lc.shiftRangeLeft();
             } else {
                 LocationController lc = LocationController.getInstance();
                 lc.shiftRangeRight();
             }
         }
         else {
             if (notches < 0) {
                 LocationController lc = LocationController.getInstance();
                 lc.zoomInOnMouse();
            } else {
                 LocationController lc = LocationController.getInstance();
                 lc.zoomOutFromMouse();
            }
         }
         parentFrame.resetLayers();
     }
 
         /** Mouse modifiers */
     private boolean isRightClick() {
         return mouseMod == mouseModifier.RIGHT;
     }
 
     private boolean isLeftClick() {
         return mouseMod == mouseModifier.LEFT;
     }
 
     private boolean isMiddleClick() {
         return mouseMod == mouseModifier.MIDDLE;
     }
 
     // Key modifiers
     private boolean isNoKeyModifierPressed() {
         return keyMod == keyModifier.DEFAULT;
     }
 
     private boolean isShiftKeyModifierPressed() {
         return keyMod == keyModifier.SHIFT;
     }
 
     private boolean isCtrlKeyModifierPressed() {
         return keyMod == keyModifier.CTRL;
     }
 
     private boolean isMetaModifierPressed() {
         return keyMod == keyModifier.META;
     }
 
     private boolean isAltModifierPressed() {
         return keyMod == keyModifier.ALT;
     }
 
     private boolean isZoomModifierPressed() {
         return (MiscUtils.MAC && isMetaModifierPressed()) || (!MiscUtils.MAC && isCtrlKeyModifierPressed());
     }
 
     private boolean isSelectModifierPressed() {
         if (isShiftKeyModifierPressed()) return true;
         else return false;
     }
 
     private void setMouseModifier(MouseEvent e) {
 
         if (e.getButton() == 1) mouseMod= mouseModifier.LEFT;
         else if (e.getButton() == 2) mouseMod = mouseModifier.MIDDLE;
         else if (e.getButton() == 3) mouseMod = mouseModifier.RIGHT;
 
         if (e.isControlDown()) {
             keyMod = keyModifier.CTRL;
         }
         else if (e.isShiftDown()) {
             keyMod = keyModifier.SHIFT;
         }
         else if (e.isMetaDown()) {
             keyMod = keyModifier.META;
         }
         else if (e.isAltDown()) {
             keyMod = keyModifier.ALT;
         }
         else {
             keyMod = keyModifier.DEFAULT;
         }
 
         tellModifiersToGraphPaneController();
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void mouseClicked( final MouseEvent event ) {
 
         if (event.getClickCount() == 2) {
             LocationController.getInstance().zoomInOnMouse();
             return;
         }
 
         trySelect(event.getPoint());
 
         setMouseModifier(event);
         parentFrame.resetLayers();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void mousePressed( final MouseEvent event ) {
 
         setMouseModifier(event);
 
         this.requestFocus();
 
         int x1 = event.getX();
         if (x1 < 0) { x1 = 0; }
         if (x1 > this.getWidth()) { x1 = this.getWidth(); }
 
         baseX = MiscUtils.transformPixelToPosition(x1, this.getWidth(), this.getHorizontalPositionalRange());
         //initialScroll = ((JScrollPane)this.getParent().getParent()).getVerticalScrollBar().getValue();
         initialScroll = ((JScrollPane)this.getParent().getParent().getParent()).getVerticalScrollBar().getValue();
 
         Point l = event.getLocationOnScreen();
         startX = l.x;
         startY = l.y;
 
         GraphPaneController gpc = GraphPaneController.getInstance();
         gpc.setMouseClickPosition(MiscUtils.transformPixelToPosition(x1, this.getWidth(), this.getHorizontalPositionalRange()));
         parentFrame.resetLayers();
     }
 
     public void resetCursor() {
         setCursor(new Cursor(Cursor.HAND_CURSOR));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void mouseReleased( final MouseEvent event ) {
 
         GraphPaneController gpc = GraphPaneController.getInstance();
 
         int x2 = event.getX();
         if (x2 < 0) { x2 = 0; }
         if (x2 > this.getWidth()) { x2 = this.getWidth(); }
 
         resetCursor();
 
         int x1 = MiscUtils.transformPositionToPixel(gpc.getMouseDragRange().getFrom(), this.getWidth(), this.getHorizontalPositionalRange());
 
         if (gpc.isPanning()) {
 
             if(!panVert){
                 LocationController lc = LocationController.getInstance();
                 Range r = lc.getRange();
                 int shiftVal = (int) (Math.round((x1-x2) / this.getUnitWidth()));
 
                 Range newr = new Range(r.getFrom()+shiftVal,r.getTo()+shiftVal);
                 lc.setLocation(newr);
             }
 
             //parentFrame.tempShowCommands();
         } else if (gpc.isZooming()) {
 
             LocationController lc = LocationController.getInstance();
             Range r;
             if (this.isLocked()) {
                 r = this.lockedRange;
             } else {
                 r = lc.getRange();
             }
             int newMin = (int) Math.round(Math.min(x1, x2) / this.getUnitWidth());
             // some weirdness here, but it's to get around an off by one
             int newMax = (int) Math.max(Math.round(Math.max(x1, x2) / this.getUnitWidth())-1, newMin);
             Range newr = new Range(r.getFrom()+newMin,r.getFrom()+newMax);
 
             lc.setLocation(newr);
         } else if (gpc.isSelecting()) {
             for (Track t: tracks) {
                 if (t.getRenderer().hasMappedValues()) {
                     if (t.getRenderer().rectangleSelect(new Rectangle2D.Double(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height))) {
                         repaint();
                     }
                     break;
                 }
             }
         }
 
         //this.parentFrame.tempShowCommands();
         this.isDragging = false;
         setMouseModifier(event);
 
         gpc.setMouseReleasePosition(MiscUtils.transformPixelToPosition(x2, this.getWidth(), this.getHorizontalPositionalRange()));
         parentFrame.resetLayers();
     }
 
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void mouseEntered( final MouseEvent event ) {
         resetCursor();
         mouseInside = true;
         setMouseModifier(event);
         hidePopup();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void mouseExited( final MouseEvent event ) {
         this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
         setMouseModifier(event);
 
         mouseInside = false;
         GraphPaneController.getInstance().setMouseXPosition(-1);
         GraphPaneController.getInstance().setMouseYPosition(-1);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void mouseDragged( final MouseEvent event ) {
 
         setMouseModifier(event);
 
         GraphPaneController gpc = GraphPaneController.getInstance();
 
         int x2 = event.getX();
         if (x2 < 0) { x2 = 0; }
         if (x2 > this.getWidth()) { x2 = this.getWidth(); }
 
         isDragging = true;
 
         if (gpc.isPanning()) {
             setCursor(new Cursor(Cursor.HAND_CURSOR));
         } else if (gpc.isZooming() || gpc.isSelecting()) {
             setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
         }
 
         //parentFrame.tempHideCommands();
 
         // Check if scrollbar is present (only vertical pan if present)
         boolean scroll = parentFrame.getVerticalScrollBar().isVisible();
 
         if (scroll){
 
             //get new points
             Point l = event.getLocationOnScreen();
             int currX = l.x;
             int currY = l.y;
 
             //magnitude
             int magX = Math.abs(currX - startX);
             int magY = Math.abs(currY - startY);
 
             if (magX >= magY){
                 //pan horizontally, reset vertical pan
                 panVert = false;
                 gpc.setMouseReleasePosition(MiscUtils.transformPixelToPosition(x2, this.getWidth(), this.getHorizontalPositionalRange()));
                 parentFrame.getVerticalScrollBar().setValue(initialScroll);
             } else {
                 //pan vertically, reset horizontal pan
                 panVert = true;
                 gpc.setMouseReleasePosition(baseX);
                 parentFrame.getVerticalScrollBar().setValue(initialScroll - (currY - startY));
             }
         } else {
             //pan horizontally
             panVert = false;
             gpc.setMouseReleasePosition(MiscUtils.transformPixelToPosition(x2, this.getWidth(), this.getHorizontalPositionalRange()));
         }
 
         parentFrame.resetLayers();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void mouseMoved( final MouseEvent event ) {
 
         mouse_x = event.getX();
         mouse_y = event.getY();
 
         // Update the GraphPaneController's record of the mouse position
         GraphPaneController.getInstance().setMouseXPosition(MiscUtils.transformPixelToPosition(event.getX(), this.getWidth(), this.getHorizontalPositionalRange()));
         if (isOrdinal()) {
             GraphPaneController.getInstance().setMouseYPosition(-1);
         } else {
             GraphPaneController.getInstance().setMouseYPosition(MiscUtils.transformPixelToPosition(this.getHeight() - event.getY(), this.getHeight(), new Range(this.yMin, this.yMax)));
         }
         GraphPaneController.getInstance().setSpotlightSize(this.getHorizontalPositionalRange().getLength());
     }
 
     /**
      * TALK TO GRAPHPANE CONTROLLER
      */
 
     private void tellModifiersToGraphPaneController() {
         GraphPaneController gpc = GraphPaneController.getInstance();
         setZooming(gpc);
         setPanning(gpc);
         setSelecting(gpc);
     }
 
     private void setZooming(GraphPaneController gpc) {
         gpc.setZooming(isDragging &&  ((isLeftClick() && isZoomModifierPressed()) || (isRightClick() && isZoomModifierPressed())));
     }
 
     private void setSelecting(GraphPaneController gpc) {
         gpc.setSelecting(isDragging &&  ((isLeftClick() && isSelectModifierPressed()) || (isRightClick() && isSelectModifierPressed())));
     }
 
     private void setPanning(GraphPaneController gpc) {
         gpc.setPanning(isDragging && isNoKeyModifierPressed());
     }
 
     /**
      *
      * @return true if the track is locked, false o/w
      */
     public boolean isLocked() {
         return isLocked;
     }
 
     public void setLocked(boolean b) {
         isLocked = b;
         if (b) {
             lockedRange = LocationController.getInstance().getRange();
         } else {
             lockedRange = null;
         }
         repaint();
     }
 
     public Range getHorizontalPositionalRange() {
         return new Range(xMin, xMax);
     }
 
     public Range getVerticalPositionalRange() {
         return new Range(yMin, yMax);
     }
 
     private BAMParametersDialog paramDialog;
 
     public void getBAMParams(BAMTrack bamTrack) {
         // capture parameters needed to adjust display
         if (paramDialog == null) {
             paramDialog = new BAMParametersDialog(Savant.getInstance(), true);
         }
         paramDialog.showDialog(bamTrack);
         if (paramDialog.isAccepted()) {
             bamTrack.setArcSizeVisibilityThreshold(paramDialog.getArcLengthThreshold());
             bamTrack.setPairedProtocol(paramDialog.getSequencingProtocol());
             bamTrack.setDiscordantMin(paramDialog.getDiscordantMin());
             bamTrack.setDiscordantMax(paramDialog.getDiscordantMax());
             bamTrack.setmaxBPForYMax(paramDialog.getMaxBPForYMax());
 
             bamTrack.prepareForRendering(LocationController.getInstance().getReferenceName() , LocationController.getInstance().getRange());
             repaint();
         }
     }
 
     public Frame getParentFrame(){
         return parentFrame;
     }
 
     public void setIsYGridOn(boolean value){
         this.isYGridOn = value;
     }
 
     public void setBufferedImage(BufferedImage bi){
         this.bufferedImage = bi;
     }
 
     //POPUP
     public void tryPopup(Point p){
 
         Point p_offset = new Point(p.x, p.y - this.getOffset());
         if(tracks == null) return;
         for (Track t: tracks) {
             Map<Record, Shape> map = t.getRenderer().searchPoint(p_offset);
             if (map != null) {
                 /** XXX: This line is here to get around what looks like a bug in the 1.6.0_20 JVM for Snow Leopard
                  * which causes the mouseExited events not to be triggered sometimes. We hide the popup before
                  * showing another. This needs to be done exactly here: after we know we have a new popup to show
                  * and before we set currentOverRecord. Otherwise, it won't work.
                  */
                 hidePopup();
 
                 currentOverRecord = (Record)map.keySet().toArray()[0];
                 currentOverShape = map.get(currentOverRecord);
                 if (currentOverRecord.getClass().equals(GenericContinuousRecord.class)){
                     currentOverShape = ContinuousTrackRenderer.continuousRecordToEllipse(this, currentOverRecord);
                 }
 
                 createJidePopup();
                 PopupPanel pp = PopupPanel.create(this, tracks[0].getDrawingMode(), t.getDataSource(), currentOverRecord);
                 fireNewPopup(pp);
                 if (pp != null){
                     popPanel.add(pp, BorderLayout.CENTER);
                     Point p1 = (Point)p.clone();
                     SwingUtilities.convertPointToScreen(p1, this);
                     jp.showPopup(p1.x -2, p1.y -2);
                     popupVisible = true;
                 }
                 repaint();
                 return;
             }
         }
         // Didn't get a hit on any track.
         currentOverShape = null;
         currentOverRecord = null;
     }
 
     public void hidePopup(){
         if(this.popupVisible){
             popupVisible = false;
             jp.hidePopupImmediately();
             currentOverShape = null;
             currentOverRecord = null;
             repaint();
         }
     }
 
     public void trySelect(Point p){
 
         Point p_offset = new Point(p.x, p.y - getOffset());
 
         for (Track t: tracks) {
             Map<Record, Shape> map = t.getRenderer().searchPoint(p_offset);
             if (map != null) {
                 Record o = (Record)map.keySet().toArray()[0];
                 t.getRenderer().addToSelected(o);
                 repaint();
                 break;
             }
         }
     }
 
     private void createJidePopup(){
         this.jp = new JidePopup();
         jp.setBackground(Color.WHITE);
         jp.getContentPane().setBackground(Color.WHITE);
         jp.getRootPane().setBackground(Color.WHITE);
         jp.getLayeredPane().setBackground(Color.WHITE);
         jp.setLayout(new BorderLayout());
         JPanel fill1 = new JPanel();
         fill1.setBackground(Color.WHITE);
         fill1.setPreferredSize(new Dimension(5,5));
         JPanel fill2 = new JPanel();
         fill2.setBackground(Color.WHITE);
         fill2.setPreferredSize(new Dimension(5,5));
         JPanel fill3 = new JPanel();
         fill3.setBackground(Color.WHITE);
         fill3.setPreferredSize(new Dimension(5,5));
         JPanel fill4 = new JPanel();
         fill4.setBackground(Color.WHITE);
         fill4.setPreferredSize(new Dimension(15,5));
         jp.add(fill1, BorderLayout.NORTH);
         jp.add(fill2, BorderLayout.SOUTH);
         jp.add(fill3, BorderLayout.EAST);
         jp.add(fill4, BorderLayout.WEST);
 
         jp.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseExited(MouseEvent e) {
                 if(e.getX() < 0 || e.getY() < 0 || e.getX() >= e.getComponent().getWidth() || e.getY() >= e.getComponent().getHeight()){
                     hidePopup();
                 }
             }
         });
 
         popPanel = new JPanel(new BorderLayout());
         popPanel.setBackground(Color.WHITE);
         jp.add(popPanel);
         jp.packPopup();
     }
 
     /**
      * Draw an informational message on top of this GraphPane.
      *
      * @param g2 the graphics to be rendered
      * @param message text of the message to be displayed
      */
     private void drawMessage(Graphics2D g2, String message) {
 
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
         Font font = g2.getFont();
 
         int h = getSize().height/3;
         int w = getWidth();
 
         if (w > 500) {
             font = font.deriveFont(Font.PLAIN, 36);
         } else if (w > 150) {
             font = font.deriveFont(Font.PLAIN, 24);
         } else {
             font = font.deriveFont(Font.PLAIN, 12);
         }
         g2.setFont(font);
         FontMetrics metrics = g2.getFontMetrics();
 
         Rectangle2D stringBounds = font.getStringBounds(message, g2.getFontRenderContext());
 
         int preferredWidth = (int)stringBounds.getWidth()+metrics.getHeight();
         int preferredHeight = (int)stringBounds.getHeight()+metrics.getHeight();
 
         w = Math.min(preferredWidth,w);
         h = Math.min(preferredHeight,h);
 
         int x = (getWidth() - w) / 2;
         int y = (getHeight() - h) / 2;
 
         //Color vColor = new Color(0, 105, 134, 196);
 
         //g2.setColor(vColor);
         //g2.fillRoundRect(x, y, w, h, arc, arc);
 
         g2.setColor(ColourSettings.getGlassPaneBackground());
         x = (getWidth() - (int)stringBounds.getWidth()) / 2;
         y = (getHeight() / 2) + ((metrics.getAscent()- metrics.getDescent()) / 2);
 
         g2.drawString(message,x,y);
     }
 
     /**
      * One of tracks is still loading.  Instead of rendering, put up a progress-bar.
      *
      * @param msg progress message to be displayed
      */
     private void showProgress(String msg) {
         if (progressPanel == null) {
             progressPanel = new ProgressPanel();
             add(progressPanel);
         }
         progressPanel.setMessage(msg);
     }
     
     private void createYMaxPanel(){
         yMaxPanel = new JLabel();
         yMaxPanel.setBorder(BorderFactory.createLineBorder(Color.darkGray));
         yMaxPanel.setBackground(new Color(240,240,240));
         yMaxPanel.setOpaque(true);
         yMaxPanel.setAlignmentX(0.5f);
         parentFrame.addToSidePanel(yMaxPanel);
     }
 
     private void updateYMax(){
         yMaxPanel.setText(String.format(" ymax=%d ", yMax));       
     }
     
     public void setYMaxVisible(boolean visible){
         if(yMaxPanel == null){
             createYMaxPanel();
         }
         yMaxPanel.setVisible(visible);
     }
 
     public void addExportEventListener(ExportEventListener eel){
         synchronized (exportListeners) {
             exportListeners.add(eel);
         }
     }
 
     public void removeExportListener(ExportEventListener eel){
         synchronized (exportListeners) {
             exportListeners.remove(eel);
         }
     }
 
     public void removeExportListeners(){
         synchronized (exportListeners) {
             exportListeners.clear();
         }
     }
 
     public void fireExportReady(Range range, BufferedImage image){
         int size = exportListeners.size();
         for (int i = 0; i < size; i++){
             exportListeners.get(i).exportCompleted(new ExportEvent(range, image));
             size = exportListeners.size(); //a listener may get removed
         }
     }
 
     public void addPopupEventListener(PopupEventListener pel){
         synchronized (popupListeners) {
             popupListeners.add(pel);
         }
     }
 
     public void removePopupListener(PopupEventListener eel){
         synchronized (popupListeners) {
             popupListeners.remove(eel);
         }
     }
 
     public void fireNewPopup(PopupPanel popup){
         int size = popupListeners.size();
         for (int i = 0; i < size; i++){
             popupListeners.get(i).newPopup(new PopupEvent(popup));
             size = popupListeners.size(); //a listener may get removed
         }
     }
     
     public boolean modeHasYMax(){
         DrawingMode mode = tracks[0].getDrawingMode();
         return mode == DrawingMode.ARC_PAIRED 
                 || mode == DrawingMode.BASE_QUALITY
                 || mode == DrawingMode.COLOURSPACE
                 || mode == DrawingMode.MAPPING_QUALITY
                 || mode == DrawingMode.MISMATCH
                 || mode == DrawingMode.PACK
                 || mode == DrawingMode.SNP
                 || mode == DrawingMode.STANDARD
                 || mode == DrawingMode.STANDARD_PAIRED
                 || mode == null; //continuous
     }
 }
