 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.modules.testcells.client.cell;
 
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.wonderland.client.cell.*;
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
     private MovableComponent movableComp;
     private DragTestRenderer cellRenderer;
 
     static Node sceneRoot;
     Entity smallCubeEntity;
 
     public DragTest(CellID cellID, CellCache cellCache) {
         super(cellID, cellCache);
         addComponent(new ChannelComponent(this));
         addComponent(new MovableComponent(this));
         movableComp = getComponent(MovableComponent.class);
     }
     
     @Override
     protected CellRenderer createCellRenderer(RendererType rendererType) {
         switch(rendererType) {
 	case RENDERER_2D :
 	    // No 2D Renderer yet
 	    return null;
 	case RENDERER_JME :
 	    cellRenderer = new DragTestRenderer(this);
 	    break;                
         }
 
         return cellRenderer;
     }
     
     @Override
     public boolean setStatus (CellStatus status) {
 	boolean ret = super.setStatus(status);
 
 	switch(status) {
 
 	case ACTIVE:
 	    dragListener.addToEntity(cellRenderer.getEntity());
 	    break;
 
         case DISK:
 	    dragListener.removeFromEntity(cellRenderer.getEntity());
 	}
 
 	return ret;
     }
 
     private class MyDragListener extends EventClassListener {
 
 	// The intersection point on the entity over which the button was pressed, in world coordinates.
 	Vector3f dragStartWorld;
 
 	// The screen coordinates of the button press event.
 	Point dragStartScreen;
 
 	Vector3f translationOnPress = null;
 
 	public Class[] eventClassesToConsume () {
 	    return new Class[] { MouseEvent3D.class };
 	}
 
 	public void commitEvent (Event event) {
 
 	    CellTransform transform = getLocalTransform();
 
 	    if (event instanceof MouseButtonEvent3D) {
 		MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
 		if (buttonEvent.isPressed()) {
 		    MouseEvent awtButtonEvent = (MouseEvent) buttonEvent.getAwtEvent();
 		    dragStartScreen = new Point(awtButtonEvent.getX(), awtButtonEvent.getY());
 		    dragStartWorld = buttonEvent.getIntersectionPointWorld();
 		    translationOnPress = transform.getTranslation(null);
 		}
 		return;
 	    } 
	    
 
	    if (!(event instanceof MouseDraggedEvent3D)) {
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
