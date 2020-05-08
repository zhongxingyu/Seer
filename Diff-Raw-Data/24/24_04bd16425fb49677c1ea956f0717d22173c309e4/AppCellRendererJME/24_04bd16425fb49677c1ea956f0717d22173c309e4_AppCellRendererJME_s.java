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
 package org.jdesktop.wonderland.modules.appbase.client.gui.guidefault;
 
 import com.jme.light.PointLight;
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.scene.Node;
 import com.jme.scene.Spatial;
 import com.jme.scene.state.LightState;
 import com.jme.scene.state.RenderState;
 //import com.jme.scene.state.ZBufferState;
 import java.awt.event.KeyEvent;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.mtgame.EntityComponent;
 import org.jdesktop.mtgame.RenderComponent;
 import org.jdesktop.wonderland.client.ClientContext;
 import org.jdesktop.wonderland.client.input.Event;
 import org.jdesktop.wonderland.client.input.EventClassListener;
 import org.jdesktop.wonderland.client.input.EventListener;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
 import org.jdesktop.wonderland.client.jme.utils.graphics.GraphicsUtils;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.modules.appbase.client.AppCell;
 import org.jdesktop.wonderland.modules.appbase.client.AppCellRenderer;
 import org.jdesktop.wonderland.modules.appbase.client.WindowView;
 
 /**
  * A cell renderer which uses JME to render app cell contents. It creates
  * a rootNode which it hands off to mtgame. It allows window views to be
  * attached. When a window view is attached the base node of the window is 
  * added as a child of this rootNode.
  *
  * @author dj
  */
 public class AppCellRendererJME extends AppCellRenderer {
 
     /** The root node of the cell renderer. */
     protected Node rootNode;
 
     /** The Z buffer state. */
     //protected ZBufferState zBufferState;
     
     /** The light state. */
     protected LightState lightState;
 	    
     /** 
      * Create a new instance of AppCellRendererJME.
      * @param cell The cell to be rendered.
      */
     public AppCellRendererJME (AppCell cell) {
         super(cell);
 
 	rootNode = new Node("Root node for cell " + cell.getCellID().toString());
 
 	//	initZBufferState();
 	initLightState();
 	//	rootNode.setRenderState(zBufferState);
 	rootNode.setRenderState(lightState);
 
 	// For debug
 	ClientContext.getInputManager().addGlobalEventListener(new SceneGraphPrinter());
     }
     
     // For debug
     private class SceneGraphPrinter extends EventClassListener {
 	public Class[] eventClassesToConsume () {
 	    return new Class[] { KeyEvent3D.class };
 	}
 	public void commitEvent (Event event) {
 	    KeyEvent3D ke3d = (KeyEvent3D) event;
 	    if (ke3d.isPressed()) {
 		KeyEvent ke = (KeyEvent)ke3d.getAwtEvent();
 		if (ke.getKeyCode() == KeyEvent.VK_P) {
 		    printEntitySceneGraphs(AppCellRendererJME.this.getEntity(), 0);
 		}
 	    }
 	}
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected Node createSceneGraph (Entity entity) {
 
 	RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().
 	    createRenderComponent(rootNode);
 	entity.addComponent(RenderComponent.class, rc);
 	rc.setEntity(entity);
 
 	return rootNode;
     }
 
     /**
      * Initialize the Z buffer state.
      */
     /*
     protected void initZBufferState () {
 	zBufferState = (ZBufferState) 
 	    ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
 	zBufferState.setEnabled(true);
 	zBufferState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
     }
     */
 
     /**
      * Initialize the light state.
      */
     protected void initLightState () {
 	PointLight light = new PointLight();
 	light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
 	light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
 	light.setLocation(new Vector3f(100, 100, 100));
 	light.setEnabled(true);
 	lightState = (LightState) 
 	    ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_LIGHT);
 	lightState.setEnabled(true);
 	lightState.attach(light);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void attachView (WindowView view) {
 
 	// The view scene graph is directly attached to the root entity of the renderer.
 	ViewWorldDefault viewWorld = (ViewWorldDefault) view;
 	getEntity().addEntity(viewWorld.getEntity());
 	viewWorld.setParentEntity(getEntity());
 
 	AppCell cell = viewWorld.getCell();
         applyTransform(rootNode, cell.getLocalTransform());
 
         FrameWorldDefault frame = viewWorld.getFrame();
 	if (frame != null) {
 	    getEntity().addEntity(frame.getEntity());
 	    frame.setParentEntity(getEntity());
 	}
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void detachView (WindowView view) {
 	ViewWorldDefault viewWorld = (ViewWorldDefault) view;
	getEntity().removeEntity(viewWorld.getEntity());
 
         FrameWorldDefault frame = viewWorld.getFrame();
 	if (frame != null) {
	    getEntity().removeEntity(frame.getEntity());
 	    frame.setParentEntity(null);
 	}
     }
 
     /**
      * {@inheritDoc}
      */
     public void logSceneGraph () {
 	EntityLogger.logEntity(getEntity());
 	//printEntitySceneGraphs(getEntity(), 0);
     }
 
     private static String INDENT = "    ";
 
     private static void printIndentLevel (int indentLevel) {
 	for (int i = 0; i < indentLevel; i++) {
 	    System.err.print(INDENT);
 	}
     }
 
     static void printEntitySceneGraphs (Entity entity, int indentLevel) {
 	printIndentLevel(indentLevel); System.err.println("Entity = " + entity);
 
 	printIndentLevel(indentLevel);	System.err.print("sceneRoot = ");
 	RenderComponent rc = (RenderComponent) entity.getComponent(RenderComponent.class);
 	if (rc == null) {
 	    System.err.println("null");
 	} else {
 	    Node sceneRoot = rc.getSceneRoot();
 	    System.err.println(sceneRoot);
 	    if (sceneRoot != null) {
 		GraphicsUtils.printNode(sceneRoot);
 	    }
 	    
 	}
 
 	int numChildren = entity.numEntities();
 	for (int i = 0; i < numChildren; i++) {
 	    Entity child = entity.getEntity(i);
 	    printIndentLevel(indentLevel); System.err.println("==================");
 	    printIndentLevel(indentLevel); System.err.println("Child Entity " + i + ": " + child);
 	    printEntitySceneGraphs(child, indentLevel+1);
 	    printIndentLevel(indentLevel); System.err.println( "==================");
 	}
     }
 
     /**
      * Remove an event listener from the root entity of this renderer.
      * @param listener The listener to remove.
      */
     public void removeEventListener (EventListener listener) {
 	listener.removeFromEntity(getEntity());
     }
 
     /**
      * Does the root entity of this renderer have the given listener attached to it?
      * @param listener The listener to check whether it is attached this renderer's root entity.
      */
     public boolean hasEventListener (EventListener listener) {
 	return listener.isListeningForEntity(getEntity());
     }
 
     /**
      * Returns the given component of the renderer's entity.
      */
     public EntityComponent getEntityComponent (Class clazz) {
 	return getEntity().getComponent(clazz);
     }
 }
