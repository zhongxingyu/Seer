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
  * 
  * Copyright (C) 2010-2011 - J.W. Janssen, http://www.lxtreme.nl
  */
 package nl.lxtreme.test.view;
 
 
 import java.awt.*;
 import java.util.logging.*;
 
 import javax.swing.*;
 
 import nl.lxtreme.test.*;
 import nl.lxtreme.test.model.*;
 import nl.lxtreme.test.view.laf.*;
 
 
 /**
  * Provides the main component controller for the signal diagram component.
  */
 public final class SignalDiagramController
 {
   // CONSTANTS
 
   /**
    * Defines the area around each cursor in which the mouse cursor should be in
    * before the cursor can be moved.
    */
   private static final int CURSOR_SENSITIVITY_AREA = 4;
 
   private static final Logger LOG = Logger.getLogger( SignalDiagramController.class.getName() );
 
   // VARIABLES
 
   private SampleDataModel dataModel;
   private ScreenModel screenModel;
   private SignalDiagramComponent signalDiagram;
 
   private final SettingsProvider settingsProvider;
 
   // CONSTRUCTORS
 
   /**
    * @param aModel
    */
   public SignalDiagramController( final SampleDataModel aModel )
   {
     this.dataModel = aModel;
     this.screenModel = new ScreenModel( aModel.getWidth() );
     this.settingsProvider = new SettingsProvider();
   }
 
   // METHODS
 
   /**
    * Finds the cursor under the given point.
    * 
    * @param aPoint
    *          the coordinate of the potential cursor, cannot be
    *          <code>null</code>.
    * @return the cursor index, or -1 if not found.
    */
   public int findCursor( final Point aPoint )
   {
     final Point point = convertToPointOf( getCursorView(), aPoint );
     final long refIdx = locationToTimestamp( point );
 
     final double snapArea = CURSOR_SENSITIVITY_AREA / this.screenModel.getZoomFactor();
 
     final Long[] cursors = this.dataModel.getCursors();
     for ( int i = 0; i < cursors.length; i++ )
     {
       if ( cursors[i] == null )
       {
         continue;
       }
 
       final double min = cursors[i].longValue() - snapArea;
       final double max = cursors[i].longValue() + snapArea;
 
       if ( ( refIdx >= min ) && ( refIdx <= max ) )
       {
         return i;
       }
     }
 
     return -1;
   }
 
   /**
    * Calculates the drop point for the channel under the given coordinate.
    * 
    * @param aCoordinate
    *          the coordinate to return the channel drop point for, cannot be
    *          <code>null</code>.
    * @return a drop point, never <code>null</code>.
    */
   public Point getChannelDropPoint( final Point aCoordinate )
   {
     final int dropRow = getCalculatedChannelRow( aCoordinate );
     final int channelHeight = this.screenModel.getChannelHeight();
 
     return new Point( 0, ( dropRow + 1 ) * channelHeight );
   }
 
   /**
    * Determines the channel row corresponding to the given X,Y-coordinate.
    * <p>
    * This method returns the <em>virtual</em> channel row, not the actual
    * channel row.
    * </p>
    * 
    * @param aCoordinate
    *          the coordinate to return the channel row for, cannot be
    *          <code>null</code>.
    * @return a channel row index (>= 0), or -1 if the point is nowhere near a
    *         channel row.
    */
   public int getChannelRow( final Point aCoordinate )
   {
     final int row = getCalculatedChannelRow( aCoordinate );
     if ( row < 0 )
     {
       return -1;
     }
 
     return this.screenModel.toVirtualRow( row );
   }
 
   /**
    * Calculates the drop point for the cursor under the given coordinate.
    * 
    * @param aCoordinate
    *          the coordinate to return the channel drop point for, cannot be
    *          <code>null</code>.
    * @return a drop point, never <code>null</code>.
    */
   public Point getCursorDropPoint( final Point aCoordinate )
   {
     final Point dropPoint = new Point( aCoordinate.x, -36 ); // XXX
 
     if ( isSnapModeEnabled() )
     {
       final Point snapPoint = getCursorSnapPoint( aCoordinate );
       dropPoint.x = snapPoint.x;
     }
 
     return dropPoint;
   }
 
   /**
    * Returns the cursor flag text for the cursor with the given index.
    * 
    * @param aCursorIdx
    *          the index of the cursor, >= 0 && < 10.
    * @return a cursor flag text, or an empty string if the cursor with the given
    *         index is undefined.
    */
   public String getCursorFlagText( final int aCursorIdx )
   {
     final Long cursorTimestamp = this.dataModel.getCursor( aCursorIdx );
     if ( cursorTimestamp == null )
     {
       return "";
     }
     return getCursorFlagText( aCursorIdx, cursorTimestamp.longValue() );
   }
 
   /**
    * Returns the cursor flag text for the cursor with the given index.
    * 
    * @param aCursorIdx
    *          the index of the cursor, >= 0 && < 10;
    * @param aCursorTimestamp
    *          the timestamp of the cursor.
    * @return a cursor flag text, or an empty string if the cursor with the given
    *         index is undefined.
    */
   public String getCursorFlagText( final int aCursorIdx, final long aCursorTimestamp )
   {
     final double sampleRate = this.dataModel.getSampleRate();
 
     final String label = this.screenModel.getCursorLabel( aCursorIdx );
     final String cursorTime = Utils.displayTime( aCursorTimestamp / sampleRate );
 
     return String.format( "%s: %s", label, cursorTime );
   }
 
   /**
    * Returns the X-position of the cursor with the given index, for displaying
    * purposes on screen.
    * 
    * @param aCursorIdx
    *          the index of the cursor to retrieve the X-position for, >= 0.
    * @return the screen X-position of the cursor with the given index, or -1 if
    *         the cursor is not defined.
    */
   public int getCursorScreenCoordinate( final int aCursorIdx )
   {
     Long cursorTimestamp = this.dataModel.getCursor( aCursorIdx );
     if ( cursorTimestamp == null )
     {
       return -1;
     }
     return ( int )Math.min( Integer.MAX_VALUE, this.screenModel.getZoomFactor() * cursorTimestamp.longValue() );
   }
 
   /**
    * XXX
    * 
    * @param aCoordinate
    * @return
    */
   public Point getCursorSnapPoint( final Point aCoordinate )
   {
     if ( isSnapModeEnabled() )
     {
       final SignalHoverInfo signalHover = getSignalHover( aCoordinate );
       if ( signalHover != null )
       {
         final Rectangle rect = signalHover.getRectangle();
         aCoordinate.x = rect.x;
         aCoordinate.y = rect.y;
       }
     }
 
     return aCoordinate;
   }
 
   /**
    * @return
    */
   public SampleDataModel getDataModel()
   {
     return this.dataModel;
   }
 
   /**
    * @return
    */
   public ScreenModel getScreenModel()
   {
     return this.screenModel;
   }
 
   /**
    * Returns the settings provider.
    * 
    * @return a settings provider, never <code>null</code>.
    */
   public IUserInterfaceSettingsProvider getSettingsProvider()
   {
     return this.settingsProvider;
   }
 
   /**
    * @param aPoint
    * @return
    */
   public boolean hideHover()
   {
     getMeasurementView().hideHover();
     return false;
   }
 
   /**
    * @return
    */
   public boolean isCursorMode()
   {
     return this.screenModel.isCursorMode();
   }
 
   /**
    * @return
    */
   public boolean isMeasurementMode()
   {
     return this.screenModel.isMeasurementMode();
   }
 
   /**
    * @return
    */
   public boolean isSnapModeEnabled()
   {
     return this.screenModel.isSnapCursor();
   }
 
   /**
    * @return true if the current zoom factor is 'zoom all', false otherwise.
    */
   public boolean isZoomAll()
   {
     return this.screenModel.isZoomAll();
   }
 
   /**
    * Converts the given coordinate to the corresponding sample index.
    * 
    * @param aCoordinate
    *          the coordinate to convert to a sample index, cannot be
    *          <code>null</code>.
    * @return a sample index, >= 0, or -1 if no corresponding sample index could
    *         be found.
    */
   public int locationToSampleIndex( final Point aCoordinate )
   {
     final long timestamp = locationToTimestamp( aCoordinate );
     final int idx = this.dataModel.getTimestampIndex( timestamp );
     if ( idx < 0 )
     {
       return -1;
     }
     return Math.max( 0, Math.min( idx, this.dataModel.getSize() - 1 ) );
   }
 
   /**
    * Converts the given coordinate to the corresponding sample index.
    * 
    * @param aCoordinate
    *          the coordinate to convert to a sample index, cannot be
    *          <code>null</code>.
    * @return a sample index, >= 0, or -1 if no corresponding sample index could
    *         be found.
    */
   public long locationToTimestamp( final Point aCoordinate )
   {
     final long timestamp = ( long )Math.ceil( Math.abs( aCoordinate.x / this.screenModel.getZoomFactor() ) );
     if ( timestamp < 0 )
     {
       return -1;
     }
     return timestamp;
   }
 
   /**
    * Moves a given channel row to another position.
    * 
    * @param aMovedRow
    *          the virtual (screen) row index that is to be moved;
    * @param aInsertRow
    *          the virtual (screen) row index that the moved row is moved to.
    */
   public void moveChannelRows( final int aMovedRow, final int aInsertRow )
   {
     if ( aMovedRow == aInsertRow )
     {
       return;
     }
     if ( ( aMovedRow < 0 ) || ( aMovedRow >= this.dataModel.getWidth() ) )
     {
       throw new IllegalArgumentException( "Moved row invalid!" );
     }
     if ( ( aInsertRow < 0 ) || ( aInsertRow >= this.dataModel.getWidth() ) )
     {
       throw new IllegalArgumentException( "Insert row invalid!" );
     }
 
     final int row = this.screenModel.toRealRow( aMovedRow );
     final int newRow = this.screenModel.toRealRow( aInsertRow );
 
     // Update the screen model...
     this.screenModel.moveRows( row, newRow );
 
     final JScrollPane scrollPane = ( JScrollPane )SwingUtilities.getAncestorOfClass( JScrollPane.class,
         getMeasurementView() );
     if ( scrollPane != null )
     {
       final int signalOffset = this.screenModel.getSignalOffset();
       final int channelHeight = this.screenModel.getChannelHeight();
 
       final int oldRowY = ( row * channelHeight ) + signalOffset - 3;
       final int newRowY = ( newRow * channelHeight ) + signalOffset - 3;
       final int rowHeight = channelHeight + 6;
 
       // Update the signal display's view port; only the affected regions...
       final JViewport viewport = scrollPane.getViewport();
 
       Rectangle rect = viewport.getVisibleRect();
       // ...old region...
       rect.y = oldRowY;
       rect.height = rowHeight;
       viewport.repaint( rect );
       // ...new region...
       rect.y = newRowY;
       viewport.repaint( rect );
 
       final JViewport channelLabelsView = scrollPane.getRowHeader();
 
       rect = channelLabelsView.getVisibleRect();
       // ...old region...
       rect.y = oldRowY;
       rect.height = rowHeight;
       channelLabelsView.repaint( rect );
       // ...new region...
       rect.y = newRowY;
       channelLabelsView.repaint( rect );
     }
   }
 
   /**
    * Drags a cursor with a given index to a given point, possibly snapping to a
    * signal edge.
    * 
    * @param aCursorIdx
    *          the cursor index to move, should be &gt;= 0 && &lt; 10;
    * @param aPoint
    *          the new point of the cursor, in case of snapping, it will use this
    *          point to find the nearest signal edge, cannot be <code>null</code>
    *          .
    */
   public void moveCursor( final int aCursorIdx, final Point aPoint )
   {
     if ( ( aCursorIdx < 0 ) || ( aCursorIdx >= this.dataModel.getCursors().length ) )
     {
       throw new IllegalArgumentException( "Invalid cursor index!" );
     }
 
     final CursorView cursorView = getCursorView();
 
     final Point point = convertToPointOf( cursorView, aPoint );
 
     if ( isSnapModeEnabled() )
     {
       final SignalHoverInfo signalHover = getSignalHover( aPoint );
       if ( signalHover != null )
       {
         Rectangle rect = signalHover.getRectangle();
         point.x = rect.x;
         point.y = ( int )rect.getCenterY();
       }
     }
 
     final long newCursorTimestamp = locationToTimestamp( point );
 
     if ( this.dataModel.setCursor( aCursorIdx, Long.valueOf( newCursorTimestamp ) ) == null )
     {
       repaintLater( cursorView, getTimeLineView() );
     }
   }
 
   /**
    * @param aPoint
    */
   public void moveHover( final Point aPoint )
   {
     final SignalHoverInfo signalHover = getSignalHover( convertToPointOf( getSignalView(), aPoint ) );
 
     getMeasurementView().moveHover( signalHover );
   }
 
   /**
    * Recalculates the dimensions of the main view.
    */
   public void recalculateDimensions()
   {
     final JScrollPane scrollPane = ( JScrollPane )SwingUtilities.getAncestorOfClass( JScrollPane.class,
         getMeasurementView() );
     if ( scrollPane != null )
     {
      final Rectangle viewPortSize = scrollPane.getViewport().getVisibleRect();

      int width = ( int )Math.min( Integer.MAX_VALUE, getAbsoluteLength() );
      if ( width < viewPortSize.width )
      {
        width = viewPortSize.width;
      }

      int height = ( this.screenModel.getChannelHeight() * this.dataModel.getWidth() );
      if ( height < viewPortSize.height )
      {
        height = viewPortSize.height;
      }
 
       JComponent view = ( JComponent )scrollPane.getViewport().getView();
       view.setPreferredSize( new Dimension( width, height ) );
       view.revalidate();
 
       view = ( JComponent )scrollPane.getColumnHeader().getView();
      // the timeline component always follows the width of the signal view, but
      // with a fixed height...
       view.setPreferredSize( new Dimension( width, TimeLineUI.TIMELINE_HEIGHT ) );
       view.setMinimumSize( view.getPreferredSize() );
       view.revalidate();
 
       view = ( JComponent )scrollPane.getRowHeader().getView();
      // the channel label component calculates its own 'optimal' width, but
      // doesn't know squat about the correct height...
      final Dimension minimumSize = view.getMinimumSize();
      view.setMinimumSize( new Dimension( minimumSize.width, height ) );
      view.setPreferredSize( new Dimension( minimumSize.width, height ) );
       view.revalidate();
 
       scrollPane.repaint();
     }
   }
 
   /**
    * Turns the visibility of all cursors either on or off.
    * <p>
    * This method does <em>not</em> modify any cursor, only whether they are
    * displayed or not!
    * </p>
    * 
    * @param aVisible
    *          <code>true</code> if the cursors should be made visible,
    *          <code>false</code> if the cursors should be made invisible.
    */
   public void setCursorsVisible( final boolean aVisible )
   {
     this.screenModel.setCursorMode( aVisible );
     repaintLater( getCursorView(), getTimeLineView() );
   }
 
   /**
    * Enables or disables the measurement mode.
    * 
    * @param aEnabled
    *          <code>true</code> to enable the measurement mode,
    *          <code>false</code> to disable this mode.
    */
   public void setMeasurementMode( final boolean aEnabled )
   {
     this.screenModel.setMeasurementMode( aEnabled );
     repaintLater( getMeasurementView() );
   }
 
   /**
    * Sets the data model for this controller.
    * 
    * @param aDataModel
    *          the dataModel to set, cannot be <code>null</code>.
    */
   public void setSampleDataModel( final SampleDataModel aDataModel )
   {
     if ( aDataModel == null )
     {
       throw new IllegalArgumentException();
     }
     this.dataModel = aDataModel;
   }
 
   /**
    * Sets the screen model for this controller.
    * 
    * @param aScreenModel
    *          the screenModel to set, cannot be <code>null</code>.
    */
   public void setScreenModel( final ScreenModel aScreenModel )
   {
     if ( aScreenModel == null )
     {
       throw new IllegalArgumentException();
     }
     this.screenModel = aScreenModel;
   }
 
   /**
    * Disables the cursor "snap" mode.
    * 
    * @param aSnapMode
    *          <code>true</code> if the snap mode should be enabled,
    *          <code>false</code> otherwise.
    */
   public void setSnapModeEnabled( final boolean aSnapMode )
   {
     this.screenModel.setSnapCursor( aSnapMode );
   }
 
   /**
    * Shows the hover information for the given coordinate.
    * 
    * @param aPoint
    *          the coordinate to show the hover information for, cannot be
    *          <code>null</code>.
    * @return <code>true</code> if the hover is going to be shown,
    *         <code>false</code> otherwise.
    */
   public boolean showHover( final Point aPoint )
   {
     final SignalHoverInfo signalHover = getSignalHover( convertToPointOf( getSignalView(), aPoint ) );
     if ( signalHover != null )
     {
       getMeasurementView().showHover( signalHover );
     }
 
     return ( signalHover != null );
   }
 
   /**
    * Zooms to make all data visible in one screen.
    */
   public void zoomAll()
   {
     try
     {
       Dimension viewSize = getVisibleViewSize();
       this.screenModel.setZoomFactor( viewSize.getWidth() / this.dataModel.getAbsoluteLength() );
     }
     finally
     {
       this.screenModel.setZoomAll( true );
     }
 
     recalculateDimensions();
   }
 
   /**
    * Zooms in with a factor 1.5
    */
   public void zoomIn()
   {
     zoomRelative( 2.0 );
 
     recalculateDimensions();
   }
 
   /**
    * Zooms to a factor of 1.0.
    */
   public void zoomOriginal()
   {
     zoomAbsolute( 1.0 );
 
     recalculateDimensions();
   }
 
   /**
    * Zooms out with a factor 1.5
    */
   public void zoomOut()
   {
     zoomRelative( 0.5 );
 
     recalculateDimensions();
   }
 
   /**
    * Returns the hover area of the signal under the given coordinate (= mouse
    * position).
    * 
    * @param aPoint
    *          the mouse coordinate to determine the signal rectangle for, cannot
    *          be <code>null</code>.
    * @return the rectangle of the signal the given coordinate contains,
    *         <code>null</code> if not found.
    */
   final SignalHoverInfo getSignalHover( final Point aPoint )
   {
     final int signalWidth = this.dataModel.getWidth();
     final int signalHeight = this.screenModel.getSignalHeight();
     final int channelHeight = this.screenModel.getChannelHeight();
 
     final int virtualRow = ( aPoint.y - signalHeight ) / channelHeight;
     if ( ( virtualRow < 0 ) || ( virtualRow > ( signalWidth - 1 ) ) )
     {
       return null;
     }
 
     final int realRow = this.screenModel.toRealRow( virtualRow );
     final double zoomFactor = this.screenModel.getZoomFactor();
 
     final long[] timestamps = this.dataModel.getTimestamps();
 
     long startTimestamp = -1L;
     long middleTimestamp = -1L;
     long endTimestamp = -1L;
     int middleXpos = -1;
 
     // find the reference time value; which is the "timestamp" under the
     // cursor...
     final int refIdx = locationToSampleIndex( aPoint );
     final int[] values = this.dataModel.getValues();
     if ( ( refIdx >= 0 ) && ( refIdx < values.length ) )
     {
       final int mask = ( 1 << realRow );
       final int refValue = ( values[refIdx] & mask );
 
       int idx = refIdx;
       do
       {
         idx--;
       }
       while ( ( idx >= 0 ) && ( ( values[idx] & mask ) == refValue ) );
       // convert the found index back to "screen" values...
       int middleSampleIdx = Math.max( 0, idx + 1 );
       middleTimestamp = timestamps[middleSampleIdx];
 
       // Search for the original value again, to complete the pulse...
       do
       {
         idx--;
       }
       while ( ( idx >= 0 ) && ( ( values[idx] & mask ) != refValue ) );
 
       // convert the found index back to "screen" values...
       int startSampleIdx = Math.max( 0, idx + 1 );
       startTimestamp = timestamps[startSampleIdx];
 
       idx = refIdx;
       do
       {
         idx++;
       }
       while ( ( idx < values.length ) && ( ( values[idx] & mask ) == refValue ) );
 
       // convert the found index back to "screen" values...
       int endSampleIdx = Math.min( idx, timestamps.length - 1 );
       endTimestamp = timestamps[endSampleIdx];
     }
 
     final Rectangle rect = new Rectangle();
     rect.x = ( int )( zoomFactor * startTimestamp );
     rect.width = ( int )( zoomFactor * ( endTimestamp - startTimestamp ) );
     rect.y = ( virtualRow * channelHeight ) + this.screenModel.getSignalOffset();
     rect.height = signalHeight;
 
     // The position where the "other" signal transition should be...
     middleXpos = ( int )( zoomFactor * middleTimestamp );
 
     // Calculate the "absolute" time based on the mouse position, use a
     // "over sampling" factor to allow intermediary (between two time stamps)
     // time value to be shown...
     final long timestamp = ( long )( ( SignalHoverInfo.TIMESTAMP_FACTOR * aPoint.x ) / zoomFactor );
 
     return new SignalHoverInfo( rect, startTimestamp, endTimestamp, middleTimestamp, timestamp, middleXpos, realRow,
         this.dataModel.getSampleRate() );
   }
 
   /**
    * @param aComponent
    */
   final void setSignalDiagram( final SignalDiagramComponent aComponent )
   {
     this.signalDiagram = aComponent;
   }
 
   /**
    * Converts a given point to the coordinate space of a given destination
    * component.
    * 
    * @param aDestination
    *          the destination component to convert the point to, cannot be
    *          <code>null</code>;
    * @param aOriginal
    *          the original point to convert, cannot be <code>null</code>.
    * @return the converted point, never <code>null</code>.
    */
   private Point convertToPointOf( final Component aDestination, final Point aOriginal )
   {
     Component view = SwingUtilities.getAncestorOfClass( JScrollPane.class, aDestination );
     if ( view instanceof JScrollPane )
     {
       view = ( ( JScrollPane )view ).getViewport().getView();
     }
     else
     {
       view = SwingUtilities.getRootPane( aDestination );
     }
     return SwingUtilities.convertPoint( view, aOriginal, aDestination );
   }
 
   /**
    * Returns the "visual length" of the timeline.
    * 
    * @return a visual length, >= 0.
    */
   private long getAbsoluteLength()
   {
     final long[] timestamps = this.dataModel.getTimestamps();
     return ( long )( ( timestamps[timestamps.length - 1] + 1 ) * this.screenModel.getZoomFactor() );
   }
 
   /**
    * Determines the signal row as calculated from the given coordinate.
    * <p>
    * This method returns the <em>virtual</em> signal row, not the actual signal
    * row.
    * </p>
    * 
    * @param aCoordinate
    *          the coordinate to return the signal row for, cannot be
    *          <code>null</code>.
    * @return a signal row, or -1 if the point is nowhere near a signal row.
    */
   private int getCalculatedChannelRow( final Point aCoordinate )
   {
     final int signalWidth = this.dataModel.getWidth();
     final int channelHeight = this.screenModel.getChannelHeight();
 
     final int row = ( int )( aCoordinate.y / ( double )channelHeight );
     if ( ( row < 0 ) || ( row >= signalWidth ) )
     {
       return -1;
     }
 
     return row;
   }
 
   /**
    * @return the cursorView
    */
   private CursorView getCursorView()
   {
     return this.signalDiagram.getCursorView();
   }
 
   /**
    * @return the arrowView
    */
   private MeasurementView getMeasurementView()
   {
     return this.signalDiagram.getMeasurementView();
   }
 
   /**
    * Returns the actual signal view component.
    * 
    * @return a signal view component, never <code>null</code>.
    */
   private SignalView getSignalView()
   {
     return this.signalDiagram.getSignalView();
   }
 
   /**
    * @return
    */
   private TimeLineView getTimeLineView()
   {
     JScrollPane scrollPane = ( JScrollPane )SwingUtilities.getAncestorOfClass( JScrollPane.class, this.signalDiagram );
     if ( scrollPane == null )
     {
       throw new IllegalStateException();
     }
 
     return ( TimeLineView )scrollPane.getColumnHeader().getView();
   }
 
   /**
    * Returns the dimensions of the visible view, taking care of viewports (such
    * as used in {@link JScrollPane}).
    * 
    * @return a visible view size, as {@link Dimension}, never <code>null</code>.
    */
   private Dimension getVisibleViewSize()
   {
     final MeasurementView measurementView = getMeasurementView();
 
     final JScrollPane scrollPane = ( JScrollPane )SwingUtilities
         .getAncestorOfClass( JScrollPane.class, measurementView );
 
     final Rectangle rect;
     if ( scrollPane != null )
     {
       rect = scrollPane.getViewport().getVisibleRect();
     }
     else
     {
       rect = this.signalDiagram.getVisibleRect();
     }
 
     return rect.getSize();
   }
 
   /**
    * @param aComponentList
    */
   private void repaintLater( final Component... aComponentList )
   {
     final Runnable runner = new Runnable()
     {
       @Override
       public void run()
       {
         for ( Component comp : aComponentList )
         {
           safeRepaint( comp );
         }
       }
 
       /**
        * @param aComponent
        */
       private void safeRepaint( final Component aComponent )
       {
         if ( aComponent != null )
         {
           aComponent.repaint();
         }
       }
     };
     SwingUtilities.invokeLater( runner );
   }
 
   /**
    * @param aFactor
    */
   private void zoomAbsolute( final double aFactor )
   {
     try
     {
       this.screenModel.setZoomFactor( aFactor );
     }
     finally
     {
       this.screenModel.setZoomAll( false );
     }
 
     LOG.log( Level.INFO, "Setting zoom factor to {0}...", Double.valueOf( this.screenModel.getZoomFactor() ) );
   }
 
   /**
    * @param aFactor
    */
   private void zoomRelative( final double aFactor )
   {
     final double newFactor = Math.min( 1000.0, aFactor * this.screenModel.getZoomFactor() );
     zoomAbsolute( newFactor );
   }
 }
