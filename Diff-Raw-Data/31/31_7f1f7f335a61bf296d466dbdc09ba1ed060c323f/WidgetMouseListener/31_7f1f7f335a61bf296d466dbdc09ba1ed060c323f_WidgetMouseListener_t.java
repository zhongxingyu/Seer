 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.editor.ui.editors;
 
 import java.awt.Dimension;
 
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Control;
 
 import com.se.simplicity.editor.internal.EditingMode;
 import com.se.simplicity.editor.internal.WidgetManager;
 
 /**
  * <p>
  * Listens for mouse events on a 3D canvas and picks the {@link com.se.simplicity.editor.internal.Widget Widget} displayed on it. Also executes the
  * current <code>Widget</code> when the mouse is dragged with the left mouse button.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class WidgetMouseListener implements MouseListener, MouseMoveListener
 {
     /**
      * <p>
      * The size of the area on the viewport to pick.
      * </p>
      */
     private static final int PICK_SIZE = 5;
 
     /**
      * <p>
      * Determines if mouse button 1 is currently down.
      * </p>
      */
     private boolean fMouseButton1Down;
 
     /**
      * <p>
      * The last position the mouse was at (only updated while mouse button 1 is down).
      * </p>
      */
     private Point fMouseButton1DownPoint;
 
     /**
      * <p>
      * The {@link com.se.simplicity.rendering.editor.internal.ContentProvider ContentProvider} to pick/execute a
      * {@link com.se.simplicity.editor.internal.Widget Widget} of.
      * </p>
      */
     private WidgetManager fWidgetManager;
 
     /**
      * <p>
      * Creates an instance of <code>WidgetMouseListener</code>.
      * </p>
      * 
      * @param widgetManager The {@link com.se.simplicity.rendering.editor.internal.ContentProvider ContentProvider} to pick/execute a
      * {@link com.se.simplicity.editor.internal.Widget Widget} of.
      */
     public WidgetMouseListener(final WidgetManager widgetManager)
     {
         fWidgetManager = widgetManager;
     }
 
     @Override
     public void mouseDoubleClick(final MouseEvent event)
     {}
 
     @Override
     public void mouseDown(final MouseEvent event)
     {
         if (fWidgetManager.getWidget() == null)
         {
             return;
         }
 
         if (fWidgetManager.getEditingMode() != EditingMode.SELECTION)
         {
             if (event.button == 1)
             {
                 fMouseButton1DownPoint = null;
                 fMouseButton1Down = true;
 
                 Dimension viewportSize = new Dimension();
                 viewportSize.width = ((Control) event.widget).getBounds().width;
                 viewportSize.height = ((Control) event.widget).getBounds().height;
 
                 fWidgetManager.getPickingEngine().pickViewport(viewportSize, event.x, event.y, PICK_SIZE, PICK_SIZE);
             }
         }
     }
 
     @Override
     public void mouseMove(final MouseEvent event)
     {
         if (fWidgetManager.getWidget() == null)
         {
             return;
         }
 
         if (fWidgetManager.getEditingMode() != EditingMode.SELECTION)
         {
             if (fMouseButton1Down)
             {
                 if (fMouseButton1DownPoint != null)
                 {
                     fWidgetManager.getWidget().executeMove(event.x - fMouseButton1DownPoint.x, event.y - fMouseButton1DownPoint.y);
                 }
 
                 fMouseButton1DownPoint = new Point(event.x, event.y);
             }
         }
     }
 
     @Override
     public void mouseUp(final MouseEvent event)
     {
         if (fWidgetManager.getWidget() == null)
         {
             return;
         }
 
         if (event.button == 1 && fWidgetManager.getEditingMode() != EditingMode.SELECTION)
         {
             fMouseButton1Down = false;
 
             fWidgetManager.getWidget().setSelectedWidgetNode(null);
            // TODO implement when rotation getter/setters work properly.
            // EditorPlugin.getInstance().propertyChanged(
            // new PropertyChangeEvent(this, "node", null, fWidgetManager.getWidget().getSelection().getSceneComponent()));
         }
     }
 }
