 /*
  * OpenBench LogicSniffer / SUMP project 
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or (at
  * your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
  *
  * Copyright (C) 2010-2011 - J.W. Janssen, <http://www.lxtreme.nl>
  */
 package nl.lxtreme.test.view;
 
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.logging.*;
 
 import javax.swing.*;
 
 import nl.lxtreme.test.*;
 import nl.lxtreme.test.view.action.*;
 import nl.lxtreme.test.view.model.*;
 
 
 /**
  * Provides a signal diagram, where signals in the form of sample data is
  * represented by channels.
  */
 public class SignalDiagramComponent extends JPanel implements Scrollable
 {
   // INNER TYPES
 
   /**
    * Provides a transparent event listener to allow some of the functionality
    * (such as DnD and cursor dragging) of this component to be controlled with
    * the mouse and keyboard. It is implemented as an {@link AWTEventListener} to
    * make it "transparent" to the rest of the components. Without this, the
    * events would be consumed without getting propagated to the actual
    * scrollpane.
    */
   final class TransparentAWTListener implements AWTEventListener
   {
     // CONSTANTS
 
     /**
      * Defines the area around each cursor in which the mouse cursor should be
      * in before the cursor can be moved.
      */
     private static final int CURSOR_SENSITIVITY_AREA = 4;
 
     // VARIABLES
 
     private final SignalDiagramController controller;
     private JComponent compRoot;
 
     private volatile int movingCursor;
 
     // CONSTRUCTORS
 
     /**
      * @param aController
      */
     public TransparentAWTListener( final SignalDiagramController aController )
     {
       this.controller = aController;
     }
 
     // METHODS
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void eventDispatched( final AWTEvent aEvent )
     {
       final int id = aEvent.getID();
       if ( aEvent instanceof MouseEvent )
       {
         final MouseEvent event = ( MouseEvent )aEvent;
 
         if ( !SwingUtilities.isDescendingFrom( event.getComponent(), getComponentRoot() ) )
         {
           // Do not process the mouse event in case it is not in our area of
           // interest...
           return;
         }
 
         if ( id == MouseEvent.MOUSE_CLICKED )
         {
           mouseClicked( event );
         }
         else if ( id == MouseEvent.MOUSE_PRESSED )
         {
           mousePressed( event );
         }
         else if ( id == MouseEvent.MOUSE_RELEASED )
         {
           mouseReleased( event );
         }
         else if ( id == MouseEvent.MOUSE_MOVED )
         {
           mouseMoved( event );
         }
         else if ( id == MouseEvent.MOUSE_DRAGGED )
         {
           mouseDragged( event );
         }
       }
       else if ( aEvent instanceof KeyEvent )
       {
         final KeyEvent event = ( KeyEvent )aEvent;
 
         if ( id == KeyEvent.KEY_PRESSED )
         {
           keyPressed( event );
         }
         else if ( id == KeyEvent.KEY_RELEASED )
         {
           keyReleased( event );
         }
         else if ( id == KeyEvent.KEY_TYPED )
         {
           keyTyped( event );
         }
       }
       else if ( aEvent instanceof ComponentEvent )
       {
         final ComponentEvent event = ( ComponentEvent )aEvent;
         final Component view = event.getComponent();
         // XXX the parent is the scrollpane's viewport???
         if ( view == SignalDiagramComponent.this.getParent() )
         {
           if ( id == ComponentEvent.COMPONENT_RESIZED )
           {
             componentResized( event );
           }
           else if ( id == ComponentEvent.COMPONENT_SHOWN )
           {
             componentShown( event );
           }
         }
       }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void componentResized( final ComponentEvent aEvent )
     {
       final Component component = aEvent.getComponent();
       component.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
 
       try
       {
         if ( isZoomAll() )
         {
           zoomAll();
         }
         else
         {
           recalculateDimensions();
         }
       }
       finally
       {
         component.setCursor( Cursor.getDefaultCursor() );
       }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void componentShown( final ComponentEvent aEvent )
     {
       final Component component = aEvent.getComponent();
       component.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
 
       try
       {
         if ( isZoomAll() )
         {
           zoomAll();
         }
         else
         {
           recalculateDimensions();
         }
       }
       finally
       {
         component.setCursor( Cursor.getDefaultCursor() );
       }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void keyPressed( final KeyEvent aEvent )
     {
       Component comp = this.controller.getSignalDiagram();
       if ( aEvent.isControlDown() )
       {
         comp.setCursor( CURSOR_MOVE_TIMESTAMP );
       }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void keyReleased( final KeyEvent aEvent )
     {
       if ( aEvent.getID() != KeyEvent.KEY_RELEASED )
       {
         return;
       }
 
       Component comp = this.controller.getSignalDiagram();
       comp.setCursor( DEFAULT );
     }
 
     /**
      * {@inheritDoc}
      */
     protected void keyTyped( final KeyEvent aEvent )
     {
       if ( aEvent.getID() != KeyEvent.KEY_TYPED )
       {
         return;
       }
 
       if ( ( '+' == aEvent.getKeyChar() ) || ( '=' == aEvent.getKeyChar() ) )
       {
         zoomIn();
       }
       else if ( ( '-' == aEvent.getKeyChar() ) || ( '_' == aEvent.getKeyChar() ) )
       {
         zoomOut();
       }
       else if ( '0' == aEvent.getKeyChar() )
       {
         zoomAll();
       }
       else if ( '1' == aEvent.getKeyChar() )
       {
         zoomOriginal();
       }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void mouseClicked( final MouseEvent aEvent )
     {
       if ( aEvent.isControlDown() )
       {
         final JComponent view = getDeepestComponentAt( aEvent );
         final Point point = SwingUtilities.convertPoint( aEvent.getComponent(), aEvent.getPoint(), view );
 
         final SignalHoverInfo signalHover = getModel().getSignalHover( point );
         if ( ( signalHover != null ) && !signalHover.isEmpty() )
         {
           final int channel = signalHover.getChannelIndex();
           final long timestamp = signalHover.getEndTimestamp().longValue();
 
           scrollToTimestamp( channel, timestamp );
         }
       }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void mouseDragged( final MouseEvent aEvent )
     {
       if ( getModel().isCursorMode() && ( this.movingCursor >= 0 ) )
       {
         final JComponent view = getDeepestComponentAt( aEvent );
         final Point point = SwingUtilities.convertPoint( aEvent.getComponent(), aEvent.getPoint(), view );
 
         this.controller.moveCursor( this.movingCursor, getCursorDropPoint( point ) );
       }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void mouseMoved( final MouseEvent aEvent )
     {
       final JComponent view = getDeepestComponentAt( aEvent );
 
       view.setCursor( DEFAULT );
 
       final SignalDiagramModel model = getModel();
       if ( model.isCursorMode() || model.isMeasurementMode() )
       {
         final Point point = SwingUtilities.convertPoint( aEvent.getComponent(), aEvent.getPoint(), view );
 
         if ( model.isMeasurementMode() )
         {
           SignalHoverInfo signalHover = model.getSignalHover( point );
 
           model.fireMeasurementEvent( signalHover );
 
           view.setCursor( signalHover == null ? DEFAULT : CURSOR_HOVER );
         }
 
         if ( model.isCursorMode() && ( findCursor( point ) >= 0 ) )
         {
           view.setCursor( CURSOR_MOVE_CURSOR );
         }
       }
     }
 
     /**
      * @param aEvent
      */
     protected void mousePressed( final MouseEvent aEvent )
     {
       final JComponent view = getDeepestComponentAt( aEvent );
 
       if ( !handlePopupTrigger( aEvent ) )
       {
         if ( ( view != null ) && getModel().isCursorMode() )
         {
           final Point point = SwingUtilities.convertPoint( aEvent.getComponent(), aEvent.getPoint(), view );
 
           int hoveredCursor = findCursor( point );
           this.movingCursor = hoveredCursor >= 0 ? hoveredCursor : -1;
         }
       }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void mouseReleased( final MouseEvent aEvent )
     {
       final JComponent view = getDeepestComponentAt( aEvent );
       if ( view != null )
       {
         view.setCursor( DEFAULT );
       }
 
       if ( !handlePopupTrigger( aEvent ) )
       {
         if ( getModel().isCursorMode() )
         {
           this.movingCursor = -1;
         }
       }
     }
 
     /**
      * Finds the cursor under the given point.
      * 
      * @param aPoint
      *          the coordinate of the potential cursor, cannot be
      *          <code>null</code>.
      * @return the cursor index, or -1 if not found.
      */
     private int findCursor( final Point aPoint )
     {
       final long refIdx = getModel().locationToTimestamp( aPoint );
 
       final double snapArea = CURSOR_SENSITIVITY_AREA / getModel().getZoomFactor();
 
       return this.controller.getSignalDiagramModel().findCursor( refIdx, snapArea );
     }
 
     /**
      * @return
      */
     private JComponent getComponentRoot()
     {
       if ( this.compRoot == null )
       {
         this.compRoot = SwingUtils.getAncestorOfClass( JScrollPane.class, SignalDiagramComponent.this );
         if ( this.compRoot == null )
         {
           this.compRoot = SignalDiagramComponent.this;
         }
       }
       return this.compRoot;
     }
 
     /**
      * Calculates the drop point for the cursor under the given coordinate.
      * 
      * @param aCoordinate
      *          the coordinate to return the channel drop point for, cannot be
      *          <code>null</code>.
      * @return a drop point, never <code>null</code>.
      */
     private Point getCursorDropPoint( final Point aCoordinate )
     {
       Point dropPoint = new Point( aCoordinate );
 
       if ( getModel().isSnapCursor() )
       {
         final SignalHoverInfo signalHover = getModel().getSignalHover( aCoordinate );
         if ( ( signalHover != null ) && !signalHover.isEmpty() )
         {
           dropPoint.x = signalHover.getMidSamplePos().intValue();
         }
       }
       dropPoint.y = 0;
 
       return dropPoint;
     }
 
     /**
      * @param aEvent
      * @return
      */
     private JComponent getDeepestComponentAt( final MouseEvent aEvent )
     {
       JComponent view = SwingUtils.getDeepestComponentAt( aEvent );
       if ( ( view != null ) && !SwingUtilities.isDescendingFrom( view, getComponentRoot() ) )
       {
         view = null;
       }
       return view;
     }
 
     /**
      * @param aEvent
      */
     private boolean handlePopupTrigger( final MouseEvent aEvent )
     {
       final boolean popupTrigger = getModel().isCursorMode() && aEvent.isPopupTrigger();
       if ( popupTrigger )
       {
         final JComponent view = getDeepestComponentAt( aEvent );
         if ( ( view == null ) || !SwingUtilities.isDescendingFrom( view, getComponentRoot() ) )
         {
           return false;
         }
         final Point point = SwingUtilities.convertPoint( aEvent.getComponent(), aEvent.getPoint(), view );
 
         final JPopupMenu contextMenu;
 
         int cursor = findCursor( point );
         if ( cursor >= 0 )
         {
           // Hovering above existing cursor, show remove menu...
           contextMenu = new JPopupMenu();
           contextMenu.add( new DeleteCursorAction( this.controller, cursor ) );
           contextMenu.add( new DeleteAllCursorsAction( this.controller ) );
         }
         else
         {
           // Not hovering above existing cursor, show add menu...
           contextMenu = new JPopupMenu();
           for ( int i = 0; i < nl.lxtreme.test.model.Cursor.MAX_CURSORS; i++ )
           {
             final SetCursorAction action = new SetCursorAction( this.controller, i );
             contextMenu.add( new JCheckBoxMenuItem( action ) );
           }
           contextMenu.addSeparator();
           contextMenu.add( new DeleteAllCursorsAction( this.controller ) );
           // when an action is selected, we *no* longer know where the point was
           // where the user clicked. Therefore, we need to store it separately
           // for later use...
           contextMenu.putClientProperty( SetCursorAction.KEY, getCursorDropPoint( point ) );
         }
 
         contextMenu.show( aEvent.getComponent(), aEvent.getX(), aEvent.getY() );
       }
       return popupTrigger;
     }
 
   }
 
   /**
    * Defines a zoom factor, with a ratio and some additional properties.
    */
   static class ZoomHelper
   {
     // CONSTANTS
 
     private static final Logger LOG = Logger.getLogger( ZoomHelper.class.getName() );
 
     // VARIABLES
 
     private boolean zoomAll;
 
     private final SignalDiagramComponent signalDiagram;
 
     // CONSTRUCTORS
 
     /**
      * Creates a new SignalDiagramComponent.ZoomFactor instance.
      * 
      * @param aSignalDiagram
      *          the signal diagram component to use.
      */
     public ZoomHelper( final SignalDiagramComponent aSignalDiagram )
     {
       this.signalDiagram = aSignalDiagram;
     }
 
     // METHODS
 
     /**
      * Returns the current value of factor.
      * 
      * @return the factor
      */
     public double getFactor()
     {
       return this.signalDiagram.getModel().getZoomFactor();
     }
 
     /**
      * Returns the current value of zoomAll.
      * 
      * @return the zoomAll
      */
     public boolean isZoomAll()
     {
       return this.zoomAll;
     }
 
     /**
      * Zooms to make all data visible in one screen.
      */
     public void zoomAll()
     {
       setFactor( getMinZoomLevel() );
       this.zoomAll = true;
 
       LOG.log( Level.INFO, "Zoom factor set to " + getFactor() );
 
      this.signalDiagram.repaint( 25L );
     }
 
     /**
      * Zooms in with a factor 1.5
      */
     public void zoomIn()
     {
       zoomRelative( 2.0 );
 
      this.signalDiagram.repaint( 25L );
     }
 
     /**
      * Zooms to a factor of 1.0.
      */
     public void zoomOriginal()
     {
       zoomAbsolute( 1.0 );
 
      this.signalDiagram.repaint( 25L );
     }
 
     /**
      * Zooms out with a factor 1.5
      */
     public void zoomOut()
     {
       zoomRelative( 0.5 );
 
      this.signalDiagram.repaint( 25L );
     }
 
     /**
      * Determines the maximum zoom level that we can handle without causing
      * display problems.
      * <p>
      * It appears that the maximum width of a component can be
      * {@link Short#MAX_VALUE} pixels wide.
      * </p>
      * 
      * @return a maximum zoom level.
      */
     private double getMaxZoomLevel()
     {
       final SignalDiagramModel model = this.signalDiagram.getModel();
       final double length = model.getAbsoluteLength();
       return Math.floor( Integer.MAX_VALUE / length );
     }
 
     /**
      * Determines the minimum zoom level that we can causes all signals to be
      * displayed in the current width and height.
      * 
      * @return a minimum zoom level.
      */
     private double getMinZoomLevel()
     {
       final SignalDiagramModel model = this.signalDiagram.getModel();
 
       Dimension viewSize = this.signalDiagram.getVisibleViewSize();
       final double length = model.getAbsoluteLength();
 
       return viewSize.getWidth() / length;
     }
 
     /**
      * Sets the factor.
      * 
      * @param aFactor
      *          the factor to set
      */
     private void setFactor( final double aFactor )
     {
       this.signalDiagram.getModel().setZoomFactor( aFactor );
     }
 
     /**
      * @param aFactor
      */
     private void zoomAbsolute( final double aFactor )
     {
       setFactor( aFactor );
       this.zoomAll = false;
 
       LOG.log( Level.INFO, "Zoom factor set to " + getFactor() );
     }
 
     /**
      * @param aFactor
      */
     private void zoomRelative( final double aFactor )
     {
       zoomAbsolute( Math.min( getMaxZoomLevel(), aFactor * getFactor() ) );
     }
   }
 
   // CONSTANTS
 
   static final Cursor DEFAULT = Cursor.getDefaultCursor();
   static final Cursor CURSOR_HOVER = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
   static final Cursor CURSOR_MOVE_CURSOR = Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR );
   static final Cursor CURSOR_MOVE_TIMESTAMP = Cursor.getPredefinedCursor( Cursor.E_RESIZE_CURSOR );
 
   private static final boolean DEBUG = true;
 
   private static final long serialVersionUID = 1L;
 
   // VARIABLES
 
   private final SignalDiagramController controller;
   private final SignalView signalView;
   private final TransparentAWTListener awtListener;
   private final SignalDiagramModel model;
   private final ZoomHelper zoomHelper;
 
   // CONSTRUCTORS
 
   /**
    * Creates a new SampleViewComponent instance.
    * 
    * @param aController
    *          the controller to use, cannot be <code>null</code>.
    */
   private SignalDiagramComponent( final SignalDiagramController aController )
   {
     super( new BorderLayout( 4, 4 ) );
 
     this.controller = aController;
 
     this.zoomHelper = new ZoomHelper( this );
 
     this.awtListener = new TransparentAWTListener( this.controller );
     this.model = new SignalDiagramModel( this.controller );
     this.signalView = new SignalView( this.controller );
 
     add( this.signalView, BorderLayout.CENTER );
   }
 
   // METHODS
 
   /**
    * Factory method to create a new {@link SignalDiagramComponent} instance.
    * 
    * @param aController
    *          the controller to use for the SignalDiagramComponent instance,
    *          cannot be <code>null</code>.
    * @return a new {@link SignalDiagramComponent} instance, never
    *         <code>null</code>.
    */
   public static SignalDiagramComponent create( final SignalDiagramController aController )
   {
     final SignalDiagramComponent result = new SignalDiagramComponent( aController );
 
     aController.setSignalDiagram( result );
 
     aController.addCursorChangeListener( result.getSignalView() );
     aController.addMeasurementListener( result.getSignalView() );
 
     return result;
   }
 
   /**
    * Installs all listeners and the support for DnD.
    * 
    * @see javax.swing.JComponent#addNotify()
    */
   @Override
   public void addNotify()
   {
     try
     {
       final long eventMask = AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK //
           | AWTEvent.KEY_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK;
       Toolkit.getDefaultToolkit().addAWTEventListener( this.awtListener, eventMask );
 
       final GhostGlassPane glassPane = new GhostGlassPane( this.controller );
       final JRootPane rootPane = SwingUtilities.getRootPane( this );
       rootPane.setGlassPane( glassPane );
 
       configureEnclosingScrollPane();
     }
     finally
     {
       super.addNotify();
     }
   }
 
   /**
    * Returns the current value of model.
    * 
    * @return the model, never <code>null</code>.
    */
   public final SignalDiagramModel getModel()
   {
     return this.model;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public Dimension getPreferredScrollableViewportSize()
   {
     return getPreferredSize();
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public int getScrollableBlockIncrement( final Rectangle aVisibleRect, final int aOrientation, final int aDirection )
   {
     final int inc;
     if ( aOrientation == SwingConstants.VERTICAL )
     {
       inc = this.model.getVerticalBlockIncrement( getSize(), aVisibleRect, aDirection );
     }
     else
     /* if ( aOrientation == SwingConstants.HORIZONTAL ) */
     {
       inc = this.model.getHorizontalBlockIncrement( aVisibleRect, aDirection );
     }
 
     return inc;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean getScrollableTracksViewportHeight()
   {
     return false;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean getScrollableTracksViewportWidth()
   {
     return false;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public int getScrollableUnitIncrement( final Rectangle aVisibleRect, final int aOrientation, final int aDirection )
   {
     return getScrollableBlockIncrement( aVisibleRect, aOrientation, aDirection );
   }
 
   /**
    * Returns the actual signal view component.
    * 
    * @return a signal view component, never <code>null</code>.
    */
   public final SignalView getSignalView()
   {
     return this.signalView;
   }
 
   /**
    * Returns the dimensions of the visible view, taking care of viewports (such
    * as used in {@link JScrollPane}).
    * 
    * @return a visible view size, as {@link Dimension}, never <code>null</code>.
    */
   public final Dimension getVisibleViewSize()
   {
     final JComponent component = getSignalView();
 
     final JScrollPane scrollPane = SwingUtils.getAncestorOfClass( JScrollPane.class, component );
 
     final Rectangle rect;
     if ( scrollPane != null )
     {
       rect = scrollPane.getViewport().getVisibleRect();
     }
     else
     {
       rect = getVisibleRect();
     }
 
     return rect.getSize();
   }
 
   /**
    * @see javax.swing.JComponent#paint(java.awt.Graphics)
    */
   @Override
   public void paint( final Graphics aGraphics )
   {
     if ( DEBUG )
     {
       final long startTime = System.nanoTime();
       try
       {
         super.paint( aGraphics );
       }
       finally
       {
         final long endTime = System.nanoTime();
         final long renderTime = endTime - startTime;
         System.out.println( "Rendering time = " + Utils.displayTime( renderTime / 1.0e9 ) );
       }
     }
     else
     {
       super.paint( aGraphics );
     }
   }
 
   /**
    * @see javax.swing.JComponent#removeNotify()
    */
   @Override
   public void removeNotify()
   {
     try
     {
       unconfigureEnclosingScrollPane();
 
       Toolkit.getDefaultToolkit().removeAWTEventListener( this.awtListener );
     }
     finally
     {
       super.removeNotify();
     }
   }
 
   /**
    * Scrolls the signal diagram component so that the given timestamp for the
    * given channel becomes visible.
    * 
    * @param aChannel
    *          the channel index of the channel to scroll the timeline for;
    * @param aTimestamp
    *          the timestamp to make visible, >= 0 and < last timestamp.
    */
   public void scrollToTimestamp( final int aChannel, final long aTimestamp )
   {
     final SignalView signalView = getSignalView();
     final Rectangle visibleRect = signalView.getVisibleRect();
 
     Rectangle rect = new Rectangle();
     rect.width = visibleRect.width;
     rect.height = visibleRect.height;
     rect.x = ( int )( ( this.controller.getSignalDiagramModel().getZoomFactor() * aTimestamp ) - rect.getCenterX() );
     rect.y = visibleRect.y;
 
     signalView.scrollRectToVisible( rect );
   }
 
   /**
    * Zooms to make all data visible in one screen.
    */
   public final void zoomAll()
   {
     this.zoomHelper.zoomAll();
   }
 
   /**
    * Zooms in with a factor 1.5
    */
   public final void zoomIn()
   {
     this.zoomHelper.zoomIn();
   }
 
   /**
    * Zooms to a factor of 1.0.
    */
   public final void zoomOriginal()
   {
     this.zoomHelper.zoomOriginal();
   }
 
   /**
    * Zooms out with a factor 1.5
    */
   public final void zoomOut()
   {
     this.zoomHelper.zoomOut();
   }
 
   /**
    * @return
    */
   final boolean isZoomAll()
   {
     return this.zoomHelper.isZoomAll();
   }
 
   /**
    * Recalculates the dimensions of the main view.
    */
   final void recalculateDimensions()
   {
     final JScrollPane scrollPane = SwingUtils.getAncestorOfClass( JScrollPane.class, getSignalView() );
     if ( scrollPane == null )
     {
       return;
     }
 
     final Rectangle viewPortSize = scrollPane.getViewport().getVisibleRect();
 
     int width = this.model.getAbsoluteScreenWidth();
     if ( width < viewPortSize.width )
     {
       width = viewPortSize.width;
     }
 
     int height = this.model.getAbsoluteScreenHeight();
     if ( height < viewPortSize.height )
     {
       height = viewPortSize.height;
     }
 
     JComponent signalView = ( JComponent )scrollPane.getViewport().getView();
     signalView.setPreferredSize( new Dimension( width, height ) );
     signalView.revalidate();
 
     TimeLineView timeline = ( TimeLineView )scrollPane.getColumnHeader().getView();
     // the timeline component always follows the width of the signal view, but
     // with a fixed height...
     timeline.setPreferredSize( new Dimension( width, timeline.getTimeLineHeight() ) );
     timeline.setMinimumSize( signalView.getPreferredSize() );
     timeline.revalidate();
 
     ChannelLabelsView channelLabels = ( ChannelLabelsView )scrollPane.getRowHeader().getView();
     // the channel label component calculates its own 'optimal' width, but
     // doesn't know squat about the correct height...
     final Dimension minimumSize = channelLabels.getMinimumSize();
     channelLabels.setMinimumSize( new Dimension( minimumSize.width, height ) );
     channelLabels.setPreferredSize( new Dimension( minimumSize.width, height ) );
     channelLabels.revalidate();
 
     scrollPane.repaint();
   }
 
   /**
    * If this component is the <code>viewportView</code> of an enclosing
    * <code>JScrollPane</code> (the usual situation), configure this
    * <code>ScrollPane</code> by, amongst other things, installing the diagram's
    * <code>timeline</code> as the <code>columnHeaderView</code> of the scroll
    * pane.
    * 
    * @see #addNotify
    */
   private void configureEnclosingScrollPane()
   {
     JScrollPane scrollPane = SwingUtils.getAncestorOfClass( JScrollPane.class, this );
     if ( scrollPane != null )
     {
       // Make certain we are the viewPort's view and not, for
       // example, the rowHeaderView of the scrollPane -
       // an implementor of fixed columns might do this.
       final JViewport viewport = scrollPane.getViewport();
       if ( ( viewport == null ) || ( viewport.getView() != this ) )
       {
         return;
       }
 
       final TimeLineView timelineView = TimeLineView.create( this.controller );
       scrollPane.setColumnHeaderView( timelineView );
 
       final ChannelLabelsView channelLabelsView = ChannelLabelsView.create( this.controller );
       scrollPane.setRowHeaderView( channelLabelsView );
 
       scrollPane.setCorner( ScrollPaneConstants.UPPER_LEADING_CORNER, new CornerView( this.controller ) );
     }
   }
 
   /**
    * Reverses the effect of <code>configureEnclosingScrollPane</code> by
    * replacing the <code>columnHeaderView</code> of the enclosing scroll pane
    * with <code>null</code>.
    * 
    * @see #removeNotify
    * @see #configureEnclosingScrollPane
    */
   private void unconfigureEnclosingScrollPane()
   {
     JScrollPane scrollPane = SwingUtils.getAncestorOfClass( JScrollPane.class, this );
     if ( scrollPane != null )
     {
       scrollPane.setColumnHeaderView( null );
       scrollPane.setRowHeaderView( null );
       scrollPane.setCorner( ScrollPaneConstants.UPPER_LEADING_CORNER, null );
     }
   }
 }
