 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.testcells.client.cell;
 
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.wonderland.client.cell.*;
 import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
 import org.jdesktop.wonderland.client.input.Event;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.client.input.EventClassListener;
 import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
 import org.jdesktop.wonderland.client.jme.input.MouseDraggedEvent3D;
 import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
 import org.jdesktop.wonderland.common.cell.CellStatus;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.modules.testcells.client.jme.cellrenderer.DragTestRenderer;
 
 /**
  * Test for MouseDraggedEvent3D events. Click on the object and drag it left or right.
  * The object (and its containing cell) should move as you drag it.
  * 
  * @author deronj
  */
 public class DragTest extends SimpleShapeCell {
 
     MyDragListener dragListener = new MyDragListener();
 
     @UsesCellComponent
     private MovableComponent movableComp;
 
     private DragTestRenderer cellRenderer;
     static Node sceneRoot;
     Entity smallCubeEntity;
 
     public DragTest(CellID cellID, CellCache cellCache) {
         super(cellID, cellCache);
     }
 
     @Override
     protected CellRenderer createCellRenderer(RendererType rendererType) {
         switch (rendererType) {
             case RENDERER_2D:
                 // No 2D Renderer yet
                 return null;
             case RENDERER_JME:
                 cellRenderer = new DragTestRenderer(this);
                 break;
         }
 
         return cellRenderer;
     }
 
     @Override
     public boolean setStatus(CellStatus status) {
         boolean ret = super.setStatus(status);
 
         switch (status) {
 
             case ACTIVE:
                 if (cellRenderer != null) { // May be null if this is a 2D renderer
                     dragListener.addToEntity(cellRenderer.getEntity());
                 }
                 break;
 
             case DISK:
                 if (cellRenderer != null) { // May be null if this is a 2D renderer
                     dragListener.removeFromEntity(cellRenderer.getEntity());
                 }
         }
 
         return ret;
     }
 
     private class MyDragListener extends EventClassListener {
 
	boolean dragging;

         // The intersection point on the entity over which the button was pressed, in world coordinates.
         Vector3f dragStartWorld;
 
         // The screen coordinates of the button press event.
         Point dragStartScreen;
         Vector3f translationOnPress = null;
 
         @Override
         public Class[] eventClassesToConsume() {
             return new Class[]{MouseEvent3D.class};
         }
 
         @Override
         public void commitEvent(Event event) {
 
             CellTransform transform = getLocalTransform();
 
             if (event instanceof MouseButtonEvent3D) {
                 MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
                dragging = false;
                if (buttonEvent.isPressed() && buttonEvent.getButton() == 
                        MouseButtonEvent3D.ButtonId.BUTTON1) {
                     MouseEvent awtButtonEvent = (MouseEvent) buttonEvent.getAwtEvent();
                    if (awtButtonEvent.getModifiersEx() == 0) {
                        dragStartScreen = new Point(awtButtonEvent.getX(), awtButtonEvent.getY());
                        dragStartWorld = buttonEvent.getIntersectionPointWorld();
                        translationOnPress = transform.getTranslation(null);
                        dragging = true;
                    } 
                 }
                 return;
             }
 
 
	    if (!dragging || !(event instanceof MouseDraggedEvent3D)) {
                 return;
             }
 
             MouseDraggedEvent3D dragEvent = (MouseDraggedEvent3D) event;
             Vector3f dragVector = dragEvent.getDragVectorWorld(dragStartWorld, dragStartScreen,
                     new Vector3f());
 
             // Now add the drag vector the node translation and move the cell.
             Vector3f newTranslation = translationOnPress.add(dragVector);
             transform.setTranslation(newTranslation);
             movableComp.localMoveRequest(transform);
         }
     }
 }
