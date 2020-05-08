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

 package org.jdesktop.wonderland.modules.affordances.client.jme;
 
 import com.jme.bounding.BoundingBox;
 import com.jme.bounding.BoundingSphere;
 import com.jme.bounding.BoundingVolume;
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.scene.GeometricUpdateListener;
 import com.jme.scene.Node;
 import com.jme.scene.Spatial;
 import com.jme.scene.shape.Sphere;
 import com.jme.scene.state.BlendState;
 import com.jme.scene.state.CullState;
 import com.jme.scene.state.MaterialState;
 import com.jme.scene.state.RenderState;
 import com.jme.scene.state.ZBufferState;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.util.Formatter;
import java.util.logging.Logger;
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.mtgame.RenderComponent;
 import org.jdesktop.mtgame.RenderManager;
 import org.jdesktop.mtgame.RenderUpdater;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.Cell.RendererType;
 import org.jdesktop.wonderland.client.input.Event;
 import org.jdesktop.wonderland.client.input.EventClassListener;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
 import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
 import org.jdesktop.wonderland.client.jme.input.MouseDraggedEvent3D;
 import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.modules.affordances.client.cell.AffordanceException;
 
 /**
  * Visual affordance (manipulator) to resize a cell in the world.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class ResizeAffordance extends Affordance {
 
     /* The length scaling factor for the box */
     private static final float LENGTH_SCALE = 1.5f;
 
     /* The current scale of the affordance w.r.t the size of the cell */
     private float currentScale = LENGTH_SCALE;
 
     /* The original (maximum) radius of the object, before it was modified */
     private float radius = 0.0f;
 
     /* The root of the scene graph of the cell */
     private Node sceneRoot = null;
 
     /* The base entity of the resize affordance */
     private Entity resizeEntity = null;
 
     /* Listener for resize drag events */
     private ResizeDragListener listener = null;
 
     private static ZBufferState zbuf = null;
     static {
         zbuf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
         zbuf.setEnabled(true);
         zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
     }
 
     /* Listener for changes in the transform of the cell */
     private GeometricUpdateListener updateListener = null;
 
     /**
      * Constructor, create a new resize affordance entity given the Cell to
      * attach it to.
      */
     public ResizeAffordance(Cell cell) throws AffordanceException {
         super("Resize", cell);
 
         // Figure out the bounds of the root entity of the cell and create a
         // cube to be just a bit larger than that
         sceneRoot = getSceneGraphRoot();
         BoundingVolume bounds = sceneRoot.getWorldBound();
         if (bounds instanceof BoundingSphere) {
             radius = ((BoundingSphere)bounds).radius;
         }
         else if (bounds instanceof BoundingBox) {
             float xExtent = ((BoundingBox)bounds).xExtent;
             float yExtent = ((BoundingBox)bounds).yExtent;
             float zExtent = ((BoundingBox)bounds).zExtent;
             radius = Math.max(xExtent, Math.max(yExtent, zExtent));
         }
 
         // Fetch the world translation for the root node of the cell and set
         // the translation for this entity root node
         Vector3f translation = sceneRoot.getWorldTranslation();
         rootNode.setLocalTranslation(translation);
         rootNode.setLocalScale(new Vector3f(LENGTH_SCALE, LENGTH_SCALE, LENGTH_SCALE));
 
         resizeEntity = new Entity("Sphere Entity");
         Node sphereNode = createSphereNode("Sphere Node");
         addSubEntity(resizeEntity, sphereNode);
         listener = addResizeListener(resizeEntity, sphereNode);
 
         // Listen for changes to the cell's translation and apply the same
         // update to the root node of the affordances. We also re-set the size
         // of the affordances: this handles the case where the bounds of the
         // scene graph has changed and we need to update the affordances
         // accordingly.
         final Node[] nodeArray = new Node[1];
         nodeArray[0] = rootNode;
         sceneRoot.addGeometricUpdateListener(updateListener = new GeometricUpdateListener() {
             public void geometricDataChanged(Spatial arg0) {
                 Vector3f translation = arg0.getWorldTranslation();
                 nodeArray[0].setLocalTranslation(translation);
                 setSize(currentScale);
                 ClientContextJME.getWorldManager().addToUpdateList(nodeArray[0]);
             }
         });
     }
 
     /**
      * @inheritDoc()
      */
     @Override
     public void setSize(float size) {
 
         // To set the scale properly, we need to compute the scale w.r.t the
         // current size of the object as a ratio of the original size of the
         // object (in case the size of the object has changed).
         currentScale = size;
         BoundingVolume bounds = sceneRoot.getWorldBound();
         float scale = 0.0f;
         if (bounds instanceof BoundingSphere) {
             float newRadius = ((BoundingSphere)bounds).radius;
             scale = (newRadius / radius) * currentScale;
         }
         else if (bounds instanceof BoundingBox) {
             float newXExtent = ((BoundingBox)bounds).xExtent;
             float newYExtent = ((BoundingBox)bounds).yExtent;
             float newZExtent = ((BoundingBox)bounds).zExtent;
             float newRadius = Math.max(newXExtent, Math.max(newYExtent, newZExtent));
             scale = (newRadius / radius) * currentScale;
         }
 
         // In order to set the size of the resize affordance, we just scale
         // the root node.
         rootNode.setLocalScale(new Vector3f(scale, scale, scale));
         ClientContextJME.getWorldManager().addToUpdateList(rootNode);
     }
 
     /**
      * @inheritDoc()
      */
     @Override
     public void remove() {
         // Remove the Entity from the scene graph. We also want to unregister
         // the listener from the cell's node. We need to do this in a special
         // update thread
         ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
             public void update(Object arg0) {
                 listener.removeFromEntity(resizeEntity);
                 ClientContextJME.getWorldManager().removeEntity(ResizeAffordance.this);
                 CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
                 RenderComponent cellRC = (RenderComponent) renderer.getEntity().getComponent(RenderComponent.class);
                 cellRC.getSceneRoot().removeGeometricUpdateListener(updateListener);
                 listener = null;
                 resizeEntity = null;
             }}, null);
     }
 
     /**
      * Creates and returns a Node that contains a sphere that represents the
      * resize affordance
      */
     private Node createSphereNode(String name) {
         // Create the new node and sphere primitive
         Node sphereNode = new Node();
         Sphere sphere = new Sphere(name, 30, 30, radius);
         sphereNode.attachChild(sphere);
 
         // Set the color to black and the transparency
         sphere.setSolidColor(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.5f));
         sphereNode.setRenderState(zbuf);
         RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
         MaterialState matState = (MaterialState) rm.createRendererState(RenderState.RS_MATERIAL);
         sphereNode.setRenderState(matState);
         matState.setDiffuse(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.5f));
         matState.setAmbient(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.5f));
 //        matState.setSpecular(new ColorRGBA(1.0f, 1.0f, 1.0f, 0.5f));
         matState.setShininess(128.0f);
         matState.setEmissive(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.5f));
         matState.setEnabled(true);
 
         BlendState alphaState = (BlendState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_BLEND);
         alphaState.setBlendEnabled(true);
         alphaState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
         alphaState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
         alphaState.setTestEnabled(true);
         alphaState.setTestFunction(BlendState.TestFunction.GreaterThan);
         alphaState.setEnabled(true);
         sphere.setRenderState(alphaState);
 
         // Remove the back faces of the object so transparency works properly
         CullState cullState = (CullState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_CULL);
         cullState.setCullFace(CullState.Face.Back);
         sphereNode.setRenderState(cullState);
 
         // Set the bound so this node can be pickable
         sphere.setModelBound(new BoundingSphere());
         sphere.updateModelBound();
         return sphereNode;
     }
 
     /**
      * Adds a drag listener to each resize handle, given the Entity and Node
      * of the handle. Also takes the vertex vector of the control handle.
      */
     private ResizeDragListener addResizeListener(Entity entity, Node node) {
         makeEntityPickable(entity, node);
         ResizeDragListener l = new ResizeDragListener();
         l.addToEntity(entity);
         return l;
     }
 
     /**
      * Inner class that handles the dragging movement and updates the position
      * of the cell accordingly
      */
     private class ResizeDragListener extends EventClassListener {
 
         // The intersection point on the entity over which the button was
         // pressed, in world coordinates.
         private Vector3f dragStartWorld = null;
 
         // The screen coordinates of the button press event.
         private Point dragStartScreen = null;
 
         // The vector of the starting point of the drag with respect to the
         // center of the afforance
         private Vector3f dragStartVectorWorld;
 
         // The length of the vector when we started dragging
         private float dragStartRadius;
 
         // The original scaling of the cell when the drag started
         private Vector3f dragStartScaling;
 
         // The label (and frame) to display the current drag amount
         private JFrame labelFrame = null;
         private JLabel resizeLabel = null;
 
         public ResizeDragListener() {
             // Tell the processor component super class that we are going to
             // post some Swing UI
             setSwingSafe(true);
 
             // Create a label to display the current drag amount
             labelFrame = new JFrame();
             labelFrame.setResizable(false);
             labelFrame.setUndecorated(true);
             labelFrame.getContentPane().setLayout(new GridLayout(1, 1));
             JPanel labelPanel = new JPanel();
             labelPanel.setBackground(Color.WHITE);
             labelPanel.setOpaque(true);
             labelFrame.getContentPane().add(labelPanel);
             labelPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
             labelPanel.setLayout(new GridLayout());
             resizeLabel = new JLabel("0.00x");
             labelPanel.add(resizeLabel);
             labelPanel.invalidate();
             labelFrame.pack();
         }
 
         @Override
         public Class[] eventClassesToConsume() {
             return new Class[] { MouseEvent3D.class };
         }
 
         @Override
         public void commitEvent(Event event) {
             // Fetch and cast some event objects
             MouseEvent3D mouseEvent = (MouseEvent3D)event;
             MouseEvent awtMouseEvent = (MouseEvent)mouseEvent.getAwtEvent();
 
             // Figure out where the initial mouse button press happened and
             // store the initial position. We also store the center of the
             // affordance.
             CellTransform transform = cell.getLocalTransform();
             if (event instanceof MouseButtonEvent3D) {
                 MouseButtonEvent3D be = (MouseButtonEvent3D)event;
                 if (be.isPressed() && be.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1) {
                     // Figure out where the button press is in screen and world
                     // coordinates. Also fetch the current rotation for cell.
                     MouseEvent awtButtonEvent = (MouseEvent)be.getAwtEvent();
                     dragStartScreen = new Point(awtButtonEvent.getX(), awtButtonEvent.getY());
                     dragStartWorld = be.getIntersectionPointWorld();
 
                     // The scaling of the cell when the drag first started
                     dragStartScaling = transform.getScaling(null);
 
                     // Figure out the world coordinates of the center of the
                     // affordance.
                     Entity entity = event.getEntity();
                     RenderComponent rc = (RenderComponent)entity.getComponent(RenderComponent.class);
                     Vector3f centerWorld = rc.getSceneRoot().getWorldTranslation();
 
                     // Compute the vector from the starting point of the drag
                     // to the center of the affordance in world coordinates.
                     dragStartVectorWorld = dragStartWorld.subtract(centerWorld);
                     dragStartRadius = dragStartVectorWorld.length();
 
                     // Set the initial value of the label to 0.0 and display
                     setLabelPosition(awtMouseEvent);
                     labelFrame.toFront();
                     labelFrame.setVisible(true);
                     labelFrame.repaint();
                 } else if (be.isReleased() == true) {
                     labelFrame.setVisible(false);
                 }
                 return;
             }
 
             // If not a drag motion, just return, we don't care about the event
             if (!(event instanceof MouseDraggedEvent3D)) {
                 return;
             }
 
             // Get the vector of the drag motion from the initial starting
             // point in world coordinates.
             MouseDraggedEvent3D dragEvent = (MouseDraggedEvent3D) event;
             Vector3f dragWorld = dragEvent.getDragVectorWorld(dragStartWorld,
                     dragStartScreen, new Vector3f());
 
             // Figure out what the vector is of the current drag location in
             // world coodinates. This gives a vector from the center of the
             // affordance. We just take the vector (from the center) of the
             // start of the drag and add the bit we dragged the mouse. Also
             // compute the length of this radius
             Vector3f dragEndVectorWorld = dragStartVectorWorld.add(dragWorld);
             float dragEndRadius = dragEndVectorWorld.length();
 
             // Take the ratio of the radius between the start and the end. That
             // will give us the amount to scale the cell
             float scale = dragEndRadius / dragStartRadius;
 
             // Set the label with the amount that we have scaled it. We display
             // the scaled amount to two decimal points
             StringBuilder resizeString = new StringBuilder();
             Formatter formatter = new Formatter(resizeString);
             formatter.format("%.2fx", scale);
             resizeLabel.setText(resizeString.toString());
             labelFrame.pack();
 
             // Figure out where to place the label based upon the location of
             // the event.
             setLabelPosition(awtMouseEvent);
 
             // Rotate the object along the defined axis and angle.
             Vector3f scaling = dragStartScaling.mult(scale);
             transform.setScaling(scaling);
             movableComp.localMoveRequest(transform);
         }
 
         /**
          * Sets the location of the frame holding the label given the current
          * mouse event, using its location
          */
         private void setLabelPosition(MouseEvent mouseEvent) {
             Component component = mouseEvent.getComponent();
             Point parentPoint = new Point(component.getLocationOnScreen());
             parentPoint.translate(mouseEvent.getX() + 10, mouseEvent.getY() - 15);
             labelFrame.setLocation(parentPoint);
         }
     }
 }
