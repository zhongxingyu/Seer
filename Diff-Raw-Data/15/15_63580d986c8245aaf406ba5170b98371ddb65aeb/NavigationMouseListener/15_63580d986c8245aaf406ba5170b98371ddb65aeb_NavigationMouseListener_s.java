 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.editor.ui.editors;
 
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseWheelListener;
 import org.eclipse.swt.graphics.Point;
 
 import com.se.simplicity.editor.internal.ContentProvider;
 import com.se.simplicity.vector.SimpleTranslationVectorf4;
 
 /**
  * <p>
  * Listens for mouse events on a 3D canvas and navigates the viewpoint.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class NavigationMouseListener implements MouseListener, MouseMoveListener, MouseWheelListener
 {
     /**
      * <p>
      * The {@link com.se.simplicity.editor.internal.ContentProvider ContentProvider} to update navigate the viewpoint of.
      * </p>
      */
     private ContentProvider fContentProvider;
 
     /**
      * <p>
      * Determines if mouse button 2 is currently down.
      * </p>
      */
     private boolean fMouseButton2Down;
 
     /**
      * <p>
      * The last position the mouse was at (only updated while mouse button 2 is down).
      * </p>
      */
     private Point fMouseButton2DownPoint;
 
     /**
      * <p>
      * Creates an instance of <code>NavigationMouseListener</code>.
      * </p>
      * 
      * @param contentProvider The {@link com.se.simplicity.editor.internal.ContentProvider ContentProvider} to update navigate the viewpoint of.
      */
     public NavigationMouseListener(final ContentProvider contentProvider)
     {
         fContentProvider = contentProvider;
     }
 
     @Override
     public void mouseDoubleClick(final MouseEvent event)
     {}
 
     @Override
     public void mouseDown(final MouseEvent event)
     {
         if (event.button == 2)
         {
             fMouseButton2DownPoint = null;
             fMouseButton2Down = true;
         }
     }
 
     @Override
     public void mouseMove(final MouseEvent event)
     {
         if (fMouseButton2Down)
         {
             if (fMouseButton2DownPoint != null)
             {
                 fContentProvider.getViewingCamera().getNode().getParent().getTransformation().rotate(
                         (float) Math.toRadians(event.x - fMouseButton2DownPoint.x) * -1.0f, new SimpleTranslationVectorf4(0.0f, 1.0f, 0.0f, 1.0f));
                 fContentProvider.getViewingCamera().getNode().getParent().getTransformation().rotate(
                         (float) Math.toRadians(event.y - fMouseButton2DownPoint.y) * -1.0f, new SimpleTranslationVectorf4(1.0f, 0.0f, 0.0f, 1.0f));
             }
 
             fMouseButton2DownPoint = new Point(event.x, event.y);
         }
     }
 
     @Override
     public void mouseScrolled(final MouseEvent event)
     {
         fContentProvider.getViewingCamera().getNode().getTransformation().translate(
                 new SimpleTranslationVectorf4(0.0f, 0.0f, event.count * -1.0f, 1.0f));
 
        fContentProvider.getCurrentWidget().updateView(fContentProvider.getViewingCamera());
     }
 
     @Override
     public void mouseUp(final MouseEvent event)
     {
         if (event.button == 2)
         {
             fMouseButton2Down = false;
         }
     }
 }
