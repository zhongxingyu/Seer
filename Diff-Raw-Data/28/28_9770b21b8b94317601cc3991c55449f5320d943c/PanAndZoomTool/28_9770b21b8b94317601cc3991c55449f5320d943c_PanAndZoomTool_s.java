 /*
  * Copyright (c) 2007-2011 by The Broad Institute, Inc. and the Massachusetts Institute of
  * Technology.  All Rights Reserved.
  *
  * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
  * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
  *
  * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
  * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
  * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
  * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
  * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
  * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
  * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
  * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
  * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
  * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
  */
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.broad.igv.ui.panel;
 
 //~--- non-JDK imports --------------------------------------------------------
 
 import org.broad.igv.ui.IGV;
 import org.broad.igv.ui.AbstractDataPanelTool;
 import org.broad.igv.ui.WaitCursorManager;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseEvent;
 
 /**
  *
  */
 public class PanAndZoomTool extends AbstractDataPanelTool {
 
     private int previousYDirection = 0;    // Drag Directions: 1=up, 0=none 0r -1=down
     //private int lastMousePressedY;
     private int cumulativeDeltaX;
     private int cumulativeDeltaY;
     private Point lastMousePoint;
     //private JViewport viewport;
    private Container panel;
     private JScrollBar verticalScrollBar;
     private boolean isDragging = false;
     private Cursor dragCursor;
 
 
     public PanAndZoomTool(DataPanel owner) {
         super(owner, Cursor.getDefaultCursor());
         this.dragCursor = IGV.fistCursor;
         setName("Zoom");
     }
 
 
     @Override
     public Cursor getCursor() {
         return isDragging ? dragCursor : Cursor.getDefaultCursor();
     }
 
 
     @Override
     public void mousePressed(final MouseEvent e) {
 
         if(e.isPopupTrigger()) {
             return;
         }
         
        panel = (Container) e.getSource();
   
         lastMousePoint = e.getPoint();
         //lastMousePressedY = (int) e.getPoint().getY();
         cumulativeDeltaX = 0;
         cumulativeDeltaY = 0;
 
         // Vertical scrolling get the viewport
         if ((panel != null) && (panel instanceof DataPanel)) {
 
             verticalScrollBar = ((DataPanel) panel).getVerticalScrollbar();
             Container parentContainer = panel.getParent();
             if (parentContainer != null) {
                 Container parentOfParent = parentContainer.getParent();
                 if ((parentOfParent != null) && (parentOfParent instanceof JViewport)) {
                     //viewport = (JViewport) parentOfParent;
                 }
             }
         }
     }
 
 
     public void mouseReleased(final MouseEvent e) {
 
         // viewport = null;
         if (isDragging) {
             getReferenceFame().snapToGrid();
             isDragging = false;
             DragEventManager.getInstance().dragStopped();
             getReferenceFame().recordHistory();
         }
         panel.setCursor(getCursor());
     }
 
 
     @Override
     final public void mouseDragged(final MouseEvent e) {
 
         try {
             panel.setCursor(dragCursor);
             if (lastMousePoint == null) {
                 lastMousePoint = e.getPoint();
                 return;
             }
 
             if (!isDragging && e.getPoint().distance(lastMousePoint) < 2) {
                 return;
             } else {
                 isDragging = true;
 
                 double deltaX = lastMousePoint.getX() - e.getX();
                 double deltaY = lastMousePoint.getY() - e.getY();
                 cumulativeDeltaX += Math.abs(deltaX);
                 cumulativeDeltaY += Math.abs(deltaY);
 
 
                 // Test for horizontal vs vertical panning.
                 if (cumulativeDeltaX > cumulativeDeltaY) {
 
                     // Horizontal scrolling
                     getReferenceFame().shiftOriginPixels(deltaX);
                 } else {
                     // Vertical Scrolling 
                     int totalYChange = (int) (lastMousePoint.getY() - e.getY());
                     if (totalYChange != 0) {
 
                         // This section handles false drag direction changes
                         int currentYDirection = 0;
 
                         // Figure out the current drag direction
                         currentYDirection = totalYChange / Math.abs(totalYChange);
 
 
                         // If the previous direction is 0 we were not moving before
                         if (previousYDirection != 0) {
 
                             // See if we changed direction
                             boolean changedYDirection = currentYDirection != previousYDirection;
                             if (!changedYDirection) {
 
                                 // Don't save lastMousePressedPoint because may
                                 // be incorrect (this is the problem we are
                                 // solving with the direction flag) instead
                                 // we'll just check the next drag Point to be
                                 // sure of the correct direction.
                                 previousYDirection = currentYDirection;
 
                                 // If we have a vertical scrollbar use it to move
                                 if (verticalScrollBar != null) {
                                     int adjustedScrollbarValue = verticalScrollBar.getValue();
                                     adjustedScrollbarValue += totalYChange;
                                     verticalScrollBar.setValue(adjustedScrollbarValue);
                                 }
                             }
                         }
                         previousYDirection = currentYDirection;
 
                     }
                 }
             }
         } finally {
             lastMousePoint = e.getPoint();    // Always save the last Point
         }
     }
 
 
     /**
      * Handler for left mouse clicks.  Delegates single clicks to track,  handles double clicks (a zoom).
      *
      * @param e
      */
     @Override
     public void mouseClicked(final MouseEvent e) {
 
         final ReferenceFrame referenceFrame = getReferenceFame();
 
         // If this is the second click of a double click, cancel the scheduled single click task.
         // The shift and alt keys are alternative undocumented zoom options
         // Shift zooms by 8x,  alt zooms out by 2x
         if (e.getClickCount() > 1 || e.isShiftDown() || e.isAltDown()) {
 
             int currentZoom = referenceFrame.getZoom();
             final int newZoom = e.isAltDown()
                     ? Math.max(currentZoom - 1, 0)
                     : (e.isShiftDown() ? currentZoom + 3 : currentZoom + 1);
             final double locationClicked = referenceFrame.getChromosomePosition(e.getX());
 
             WaitCursorManager.CursorToken token = WaitCursorManager.showWaitCursor();
             try {
                 referenceFrame.zoomTo(newZoom, locationClicked);
             } finally {
                 WaitCursorManager.removeWaitCursor(token);
             }
         }
 
     }
 }
