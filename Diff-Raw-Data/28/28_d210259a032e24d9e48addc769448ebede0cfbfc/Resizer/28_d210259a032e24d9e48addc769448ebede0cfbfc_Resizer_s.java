 /**
  * Created On: 30-Aug-06 9:16:21 PM
  * $Id$
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar;
 
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Window;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseMotionAdapter;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JDialog;
 import javax.swing.SwingUtilities;
 
 import com.thinkparity.ophelia.browser.Constants.Resize;
 import com.thinkparity.ophelia.browser.application.browser.Browser;
 import com.thinkparity.ophelia.browser.application.browser.BrowserWindow;
import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.ContainerCell;
import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.DraftCell;
 
 /**
  * @author rob_masako@shaw.ca
  * @version $Revision$
  */
 public class Resizer {
     
     /** Browser */
     private Browser browser;
     
     /** Component */
     private Component component;
    
     /** Flag to indicate if dragging the centre of the control will cause moving */
     private Boolean supportMouseMove;
     
     /** Resize edges */
     private ResizeEdges resizeEdges;
     
     /** Flag to indicate if currently resize dragging. */
     private static Boolean resizeDragging = Boolean.FALSE;
     
     /** Flag to indicate if currently movement dragging. */
     private static Boolean moveDragging = Boolean.FALSE;
     
     /** Flag to indicate if initialized. */
     private Boolean initialized = Boolean.FALSE;
 
     /** Resize mode. */
     private ResizeDirection resizeDirection = ResizeDirection.NONE;
     
     /** The resize offset size in the x direction. */
     private static int resizeOffsetX;    
     
     /** The resize offset size in the y direction. */
     private static int resizeOffsetY;
     
     /** A list of all components that have listeners. */
     private final List<Component> components;
         
     /** A map of components to mouse motion adapters. */
     private Map<Component, MouseMotionAdapter> mouseMotionAdapters;
     
     /** A map of components to mouse adapters. */
     private Map<Component, MouseAdapter> mouseAdapters; 
      
     /**
      * Create a Resizer (to resize the Browser or Avatar).
      * 
      * @param browser
      *            The browser.
      * @param component
      *            The component.
      * @param supportMouseMove
      *            Flag to indicate if dragging the centre of the control will cause moving.           
      * @param resizeEdges
      *            The resize edges (eg. top, middle, bottom)
      */
     public Resizer(final Browser browser, final Component component,
             final Boolean supportMouseMove, final ResizeEdges resizeEdges) {
         this.browser = browser;
         this.component = component;
         this.supportMouseMove = supportMouseMove;
         this.resizeEdges = resizeEdges;
         this.components = new LinkedList<Component>();
         this.mouseMotionAdapters = new HashMap<Component, MouseMotionAdapter>(50, 0.75F);
         this.mouseAdapters = new HashMap<Component, MouseAdapter>(50, 0.75F);
         initComponents();
     }
     
     /**
      * Remove all listeners for this resizer.
      */
     public void removeAllListeners() {
         for(final Component component : components) {
             final MouseMotionAdapter mouseMotionAdapter = mouseMotionAdapters.get(component);
             if (null!=mouseMotionAdapter) {
                 component.removeMouseMotionListener(mouseMotionAdapter);
             }
             final MouseAdapter mouseAdapter = mouseAdapters.get(component);
             if (null!=mouseAdapter) {
                 component.removeMouseListener(mouseAdapter);
             }
         }
     }
 
     /**
      * Add components that a user can drag on to move the parent.
      * 
      * @param components
      *            List of components.
      */
     public void addComponentsThatSupportMouseMove(final List<Component>componentsThatSupportMouseMove) {
         if (null != componentsThatSupportMouseMove) {
             for (final Component component : componentsThatSupportMouseMove) {
                 final MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter() {
                     public void mouseDragged(java.awt.event.MouseEvent evt) {
                         formMouseDragged(evt, component);
                     }
                 };
                 final MouseAdapter mouseAdapter = new MouseAdapter() {
                     public void mousePressed(java.awt.event.MouseEvent evt) {
                         formMousePressed(evt, component);
                     }
                     public void mouseReleased(java.awt.event.MouseEvent evt) {
                         formMouseReleased(evt, component);
                     }
                 };
                 component.addMouseMotionListener(mouseMotionAdapter);
                 component.addMouseListener(mouseAdapter);
                 
                 // Save so they can be removed later.
                 components.add(component);
                 mouseMotionAdapters.put(component, mouseMotionAdapter);
                 mouseAdapters.put(component, mouseAdapter);
             }
         }
     }
     
     /**
      * These get and set methods are used by classes that intend to do
      * their own resizing. (For example, the bottom right resize control.)
      */
     public Boolean isResizeDragging() {
         return resizeDragging;
     }
     public void setResizeDragging(final Boolean resizeDragging) {
         Resizer.resizeDragging = resizeDragging;
     }
           
     private void initResize(final java.awt.event.MouseEvent e, final Component component) {
         final Point p = e.getPoint();
         final Dimension d = component.getSize();
         final Boolean lowX = (p.getX() < Resize.EDGE_PIXEL_BUFFER);
         final Boolean highX = (p.getX() >= d.getWidth() - Resize.EDGE_PIXEL_BUFFER);
         final Boolean lowY = (p.getY() < Resize.EDGE_PIXEL_BUFFER);
         final Boolean highY = (p.getY() >= d.getHeight() - Resize.EDGE_PIXEL_BUFFER);
         
         if (lowX && lowY) {
             resizeDirection = ResizeDirection.NW;
         } else if (highX && lowY) {
             resizeDirection = ResizeDirection.NE;
         } else if (lowX && highY) {
             resizeDirection = ResizeDirection.SW;
         } else if (highX && highY) {
             resizeDirection = ResizeDirection.SE;
         } else if (lowX) {
             resizeDirection = ResizeDirection.W;
         } else if (highX) {
             resizeDirection = ResizeDirection.E;
         } else if (lowY) {
             resizeDirection = ResizeDirection.N;
         } else if (highY) {
             resizeDirection = ResizeDirection.S;
         } else {
             resizeDirection = ResizeDirection.NONE;
         }
         
         resizeDirection = filterAllowedDirections(resizeDirection);
         
         adjustCursor(resizeDirection, component);
     }
     
     private ResizeDirection filterAllowedDirections(final ResizeDirection rd) {
         ResizeDirection newDirection = rd;
         
         switch (resizeEdges) {
         case NO_EDGE:
             newDirection = ResizeDirection.NONE;
             break;
         case ALL_EDGES:
             newDirection = rd;
             break;
         case LEFT:
             if (rd==ResizeDirection.SW) { newDirection = ResizeDirection.W; }
             else if (rd==ResizeDirection.NW) { newDirection = ResizeDirection.W; }
             else if (rd!=ResizeDirection.W) { newDirection = ResizeDirection.NONE; }
             break;
         case RIGHT:
             if (rd==ResizeDirection.SE) { newDirection = ResizeDirection.E; }
             else if (rd==ResizeDirection.NE) { newDirection = ResizeDirection.E; }
             else if (rd!=ResizeDirection.E) { newDirection = ResizeDirection.NONE; }
             break;
         case TOP:
             if (rd==ResizeDirection.SW) { newDirection = ResizeDirection.W; }
             else if (rd==ResizeDirection.SE) { newDirection = ResizeDirection.E; }
             else if (rd==ResizeDirection.S) { newDirection = ResizeDirection.NONE; }
             break;
         case BOTTOM:
             if (rd==ResizeDirection.NW) { newDirection = ResizeDirection.W; }
             else if (rd==ResizeDirection.NE) { newDirection = ResizeDirection.E; }
             else if (rd==ResizeDirection.N) { newDirection = ResizeDirection.NONE; }
             break;
         case MIDDLE:
             if (rd==ResizeDirection.SW) { newDirection = ResizeDirection.W; }
             else if (rd==ResizeDirection.SE) { newDirection = ResizeDirection.E; }
             else if (rd==ResizeDirection.S) { newDirection = ResizeDirection.NONE; }
             else if (rd==ResizeDirection.NW) { newDirection = ResizeDirection.W; }
             else if (rd==ResizeDirection.NE) { newDirection = ResizeDirection.E; }
             else if (rd==ResizeDirection.N) { newDirection = ResizeDirection.NONE; }
             break;
         default:
             newDirection = ResizeDirection.NONE;
             break;
         }
             
         return newDirection;
     }
     
     private void adjustCursor(final ResizeDirection resizeDirection, final Component component) {
         Cursor cursor;
         
         switch (resizeDirection) {
         case NW:
             cursor = new Cursor(Cursor.NW_RESIZE_CURSOR);
             break;
         case NE:
             cursor = new Cursor(Cursor.NE_RESIZE_CURSOR);
             break;
         case SW:
             cursor = new Cursor(Cursor.SW_RESIZE_CURSOR);
             break;
         case SE:
             cursor = new Cursor(Cursor.SE_RESIZE_CURSOR);
             break;
         case W:
             cursor = new Cursor(Cursor.W_RESIZE_CURSOR);
             break;
         case E:
             cursor = new Cursor(Cursor.E_RESIZE_CURSOR);
             break;
         case N:
             cursor = new Cursor(Cursor.N_RESIZE_CURSOR);
             break;
         case S:
             cursor = new Cursor(Cursor.S_RESIZE_CURSOR);
             break;
         default:
             cursor = null;
             break;
         }
         
         changeCursor(cursor, component);
     }
     
     private void changeCursor(final Cursor cursor, final Component component) {
         component.setCursor(cursor);
         Window window = SwingUtilities.getWindowAncestor(component);
         if (null!=window) {
             window.setCursor(cursor);
         }
         browser.setCursor(cursor);
     }
     
     private void formMouseDraggedMove(final java.awt.event.MouseEvent evt, final Component component) {
         final Component componentToMove;
         if ((component instanceof JDialog) || (component instanceof BrowserWindow)) {
             componentToMove = component;
         } else {
             componentToMove = SwingUtilities.getWindowAncestor(component);
         }
         
         Point moveOffset = new Point(evt.getPoint().x-resizeOffsetX, evt.getPoint().y-resizeOffsetY);
         move(componentToMove, moveOffset);
     }
     
     private void formMouseDraggedResize(final java.awt.event.MouseEvent evt, final Component component) {
         final Component componentToResize;
         Dimension resizeOffset = null;
         Point moveOffset = null;
         
         if ((component instanceof JDialog) || (component instanceof BrowserWindow)) {
             componentToResize = component;
         } else {
             componentToResize = SwingUtilities.getWindowAncestor(component);
         }
                
         switch (resizeDirection) {
         case NW:
             moveOffset = new Point(evt.getPoint().x - resizeOffsetX, evt.getPoint().y - resizeOffsetY);
             resizeOffset = new Dimension(resizeOffsetX - evt.getPoint().x, resizeOffsetY - evt.getPoint().y);
             break;
         case NE:
             moveOffset = new Point(0, evt.getPoint().y - resizeOffsetY);
             resizeOffset = new Dimension(evt.getPoint().x - resizeOffsetX, resizeOffsetY - evt.getPoint().y);
             break;
         case SW:
             moveOffset = new Point(evt.getPoint().x - resizeOffsetX, 0);
             resizeOffset = new Dimension(resizeOffsetX - evt.getPoint().x, evt.getPoint().y - resizeOffsetY);
             break;
         case SE:           
             resizeOffset = new Dimension(evt.getPoint().x - resizeOffsetX, evt.getPoint().y - resizeOffsetY);
             break;
         case W:
             moveOffset = new Point(evt.getPoint().x - resizeOffsetX, 0);
             resizeOffset = new Dimension(resizeOffsetX - evt.getPoint().x, 0);
             break;            
         case E:
             resizeOffset = new Dimension(evt.getPoint().x - resizeOffsetX, 0);
             break;
         case N:
             moveOffset = new Point(0, evt.getPoint().y - resizeOffsetY);
             resizeOffset = new Dimension(0, resizeOffsetY - evt.getPoint().y);
             break;
         case S:
             resizeOffset = new Dimension(0, evt.getPoint().y - resizeOffsetY);
             break;
         default:
             break;
         }
         
         // Save the component width (ie. the component that the mouse is dragging in)
         final double oldComponentWidth = component.getSize().getWidth();
         final double oldComponentHeight = component.getSize().getHeight();
         
         // Resize and maybe also move
         if (null != resizeOffset) {
             if (null != moveOffset) {
                 resizeAndMove(componentToResize, resizeOffset, moveOffset);
             } else {
                 resize(componentToResize, resizeOffset);
             }
         }
         
         // The width of the control may change when the window is resized.
         if ((resizeDirection == ResizeDirection.NE) ||
             (resizeDirection == ResizeDirection.E) ||
             (resizeDirection == ResizeDirection.SE)) {
             resizeOffsetX += (component.getSize().getWidth() - oldComponentWidth);
         }
         
         // The height of the control may change when the window is resized.
         if ((resizeDirection == ResizeDirection.SW) ||
             (resizeDirection == ResizeDirection.S) ||
             (resizeDirection == ResizeDirection.SE)) {
             resizeOffsetY += (component.getSize().getHeight() - oldComponentHeight);
         }
     }
     
     /**
      * Resize the component.
      * 
      * @param component
      *          The component to resize.
      * @param resizeOffset
      *          The amount to resize.
      */
     private void resize(final Component component, final Dimension resizeOffset) {
         if ((resizeOffset.width != 0) || (resizeOffset.height != 0)) {
             if (component instanceof BrowserWindow) {
                 browser.resizeBrowserWindow(resizeOffset);
             } else {
                 Dimension size = component.getSize();
                 size.width += resizeOffset.width;
                 size.height += resizeOffset.height;
                 size = adjustForMinimumSize(component, size);
                 if (!component.getSize().equals(size)) {
                     component.setSize(size);
                     component.validate();
                 }
             }
         }
     }
         
     /**
      * Resize and move the component.
      * 
      * @param component
      *          The component to resize.
      * @param resizeOffset
      *          The amount to resize.
      * @param moveOffset
      *          The amount to move.
      */
     private void resizeAndMove(final Component component, final Dimension resizeOffset, final Point moveOffset) {
         if ((resizeOffset.width!=0) || (resizeOffset.height!=0) ||
             (moveOffset.x != 0 ) || (moveOffset.y != 0 )) {        
             if (component instanceof BrowserWindow) {
                 browser.moveAndResizeBrowserWindow(moveOffset, resizeOffset);
             } else {
                 Rectangle sizeAndLocation = component.getBounds();
                 sizeAndLocation.width += resizeOffset.width;
                 sizeAndLocation.height += resizeOffset.height;
                 sizeAndLocation.x += moveOffset.x;
                 sizeAndLocation.y += moveOffset.y;
                 sizeAndLocation = adjustForMinimumSize(component, sizeAndLocation);
                 if (!component.getBounds().equals(sizeAndLocation)) {
                     component.setBounds(sizeAndLocation);
                     component.validate();
                 }
             }
         }
     }
     
     /**
      * Move the component.
      * 
      * @param component
      *          The component to resize.
      * @param moveOffset
      *          The amount to move.
      */    
     private void move(final Component component, final Point moveOffset) {
         if ((moveOffset.x != 0 ) || (moveOffset.y != 0 )) {
             final Point location = component.getLocation();
             location.x += moveOffset.x;
             location.y += moveOffset.y;
             component.setLocation(location.x, location.y);
         }
     }
     
     /**
      * Adjust size to account for minimum size.
      * 
      * @param component
      *          The component to resize.
      * @param size
      *          The size.
      *          
      * @return The adjusted size.
      */
     private Dimension adjustForMinimumSize(final Component component, final Dimension size) {
         Dimension adjustedSize = new Dimension(size);
         Dimension minSize = component.getMinimumSize();
         if (size.getWidth() < minSize.getWidth()) {
             adjustedSize.width = (int)minSize.getWidth();
         }
         if (size.getHeight() < minSize.getHeight()) {
             adjustedSize.height = (int)minSize.getHeight();
         }
         
         return adjustedSize;
     }
     
     /**
      * Adjust size and move to account for minimum size.
      * 
      * @param component
      *          The component to resize and move.
      * @param sizeAndLocation
      *          The rectangle representing size and location.
      *          
      * @return The adjusted size and location.
      */
     private Rectangle adjustForMinimumSize(final Component component, final Rectangle sizeAndLocation) {
         Rectangle adjustedSizeAndLocation = new Rectangle(sizeAndLocation);
         Dimension minSize = component.getMinimumSize();
         if (sizeAndLocation.getWidth() < minSize.getWidth()) {
             if (sizeAndLocation.x != component.getLocation().x) {
                 adjustedSizeAndLocation.x += (sizeAndLocation.getWidth() - minSize.getWidth());
             }
             adjustedSizeAndLocation.width = (int)minSize.getWidth();
         }
         if (sizeAndLocation.getHeight() < minSize.getHeight()) {
             if (sizeAndLocation.y != component.getLocation().y) {
                 adjustedSizeAndLocation.y += (sizeAndLocation.getHeight() - minSize.getHeight());
             }
             adjustedSizeAndLocation.height = (int)minSize.getHeight();
         }
         return adjustedSizeAndLocation;
     }
     
     private void formMouseDragged(final java.awt.event.MouseEvent evt, final Component component) {
         if (moveDragging) {
             formMouseDraggedMove(evt, component);
         } else if (resizeDragging) {
             formMouseDraggedResize(evt, component);
         }
     }
     
     private void formMouseEntered(final java.awt.event.MouseEvent evt, final Component component) {
         if ((!resizeDragging) && (!moveDragging) && (resizeEdges!=ResizeEdges.NO_EDGE)) {
             initResize(evt, component);
         }
     }
     
     private void formMouseMoved(final java.awt.event.MouseEvent evt, final Component component) {
         if ((!resizeDragging) && (!moveDragging) && (resizeEdges!=ResizeEdges.NO_EDGE)) {
             initResize(evt, component);
         }
     }
     
     private void formMouseExited(final java.awt.event.MouseEvent evt, final Component component) {
         if ((!resizeDragging) && (!moveDragging)) {
             resizeDirection = ResizeDirection.NONE;
             changeCursor(null, component);
         }
     }    
     
     private void formMousePressed(final java.awt.event.MouseEvent evt, final Component component) {
         resizeOffsetX = evt.getPoint().x;
         resizeOffsetY = evt.getPoint().y;
         if ((resizeDirection == ResizeDirection.NONE) && (supportMouseMove)) {
             moveDragging = Boolean.TRUE;
         } else if (resizeDirection != ResizeDirection.NONE) {
             resizeDragging = Boolean.TRUE;
         }
     }   
        
     private void formMouseReleased(final java.awt.event.MouseEvent evt, final Component component) {
         resizeDragging = Boolean.FALSE;
         moveDragging = Boolean.FALSE;
         
         // Sometimes when the mouse is released it is not over the
         // component anymore, for example, when minimizing beyond
         // the minimum size of the dialog. For this reason it is
         // best to reset the cursor rather than hope for the 
         // mouse exit event.
         resizeDirection = ResizeDirection.NONE;
         changeCursor(null, component);
     }
     
     private void initComponents() {
         if (!initialized) {
             final MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter() {
                 public void mouseDragged(java.awt.event.MouseEvent evt) {
                     formMouseDragged(evt, component);
                 }
                 public void mouseMoved(java.awt.event.MouseEvent evt) {
                     formMouseMoved(evt, component);
                 }
             };
             final MouseAdapter mouseAdapter = new MouseAdapter() {
                 public void mouseEntered(java.awt.event.MouseEvent evt) {
                     formMouseEntered(evt, component);
                 }
                 public void mouseExited(java.awt.event.MouseEvent evt) {
                     formMouseExited(evt, component);
                 }
                 public void mousePressed(java.awt.event.MouseEvent evt) {
                     formMousePressed(evt, component);
                 }
                 public void mouseReleased(java.awt.event.MouseEvent evt) {
                     formMouseReleased(evt, component);
                 }
             };
             component.addMouseMotionListener(mouseMotionAdapter);
             component.addMouseListener(mouseAdapter);
             
             // Save so they can be removed later.
             components.add(component);
             mouseMotionAdapters.put(component, mouseMotionAdapter);
             mouseAdapters.put(component, mouseAdapter);
             
             final Window window = SwingUtilities.getWindowAncestor(component);
             if (null!=window && window instanceof JDialog) {
                 final MouseMotionAdapter mouseMotionAdapterWindow = new MouseMotionAdapter() {
                     public void mouseDragged(java.awt.event.MouseEvent evt) {
                         formMouseDragged(evt, window);
                     }
                     public void mouseMoved(java.awt.event.MouseEvent evt) {
                         formMouseMoved(evt, window);
                     }
                 };
                 final MouseAdapter mouseAdapterWindow = new MouseAdapter() {
                     public void mouseEntered(java.awt.event.MouseEvent evt) {
                         formMouseEntered(evt, window);
                     }
                     public void mouseExited(java.awt.event.MouseEvent evt) {
                         formMouseExited(evt, window);
                     }
                     public void mousePressed(java.awt.event.MouseEvent evt) {
                         formMousePressed(evt, window);
                     }
                     public void mouseReleased(java.awt.event.MouseEvent evt) {
                         formMouseReleased(evt, window);
                     }
                 };
                 window.addMouseMotionListener(mouseMotionAdapterWindow);
                 window.addMouseListener(mouseAdapterWindow);
                 
                 // Save so they can be removed later.
                 components.add(window);
                 mouseMotionAdapters.put(window, mouseMotionAdapterWindow);
                 mouseAdapters.put(window, mouseAdapterWindow);
             }
 
             initialized = Boolean.TRUE;
         }
     }
       
     private enum ResizeDirection { E, N, NE, NONE, NW, S, SE, SW, W }
     public enum ResizeEdges { NO_EDGE, ALL_EDGES, TOP, MIDDLE, BOTTOM, LEFT, RIGHT }
 }
