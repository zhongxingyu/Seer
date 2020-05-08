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
 package org.jdesktop.wonderland.modules.affordances.client.jme;
 
 import com.jme.bounding.BoundingBox;
 import com.jme.bounding.BoundingSphere;
 import com.jme.bounding.BoundingVolume;
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.scene.GeometricUpdateListener;
 import com.jme.scene.Node;
 import com.jme.scene.Spatial;
 import com.jme.scene.shape.Tube;
 import com.jme.scene.state.MaterialState;
 import com.jme.scene.state.RenderState;
 import com.jme.scene.state.ZBufferState;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.util.Formatter;
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
  * Affordance to rotate a cell along each major axis.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class RotateAffordance extends Affordance {
 
     /** An enumeration of the axis along which to effect the rotate motion */
     public enum RotateAxis {
         X_AXIS, Y_AXIS, Z_AXIS
     }
 
     /* The scaling of the outer radius of the tube */
     private static final float RADIUS_SCALE = 1.5f;
 
     /* The inner radius offset */
     private static final float RADIUS_WIDTH = 0.1f;
 
     /* The thickness of tube */
     private static final float THICKNESS = 0.1f;
 
     /* The entitye represents the discs for each axis */
     private Entity xEntity = null, yEntity = null, zEntity = null;
 
     /* The nodes representing the discs for each axis */
     private Node xNode = null, yNode = null, zNode = null;
 
     /* The current scale of the affordance w.r.t the size of the cell */
     private float currentScale = RADIUS_SCALE;
 
     /* The original innert radius of the affordance */
     private float innerRadius = 0.0f;
 
     /* The root of the scene graph of the cell */
     private Node sceneRoot = null;
 
     private static ZBufferState zbuf = null;
     static {
         zbuf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
         zbuf.setEnabled(true);
         zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
     }
 
     /* Listener for changes in the translation of the cell */
     private GeometricUpdateListener updateListener = null;
 
     /* Listeners for drag events for each axis */
     private RotationDragListener xListener = null, yListener = null, zListener = null;
 
     /**
      * TBD
      */
     public RotateAffordance(Cell cell) throws AffordanceException {
         super("Rotate", cell);
         
         // Figure out the bounds of the root entity of the cell and create a
         // tube to be just a bit larger than that
         sceneRoot = getSceneGraphRoot();
         BoundingVolume bounds = sceneRoot.getWorldBound();
         float outerRadius = 0.0f;
         if (bounds instanceof BoundingSphere) {
             innerRadius = ((BoundingSphere) bounds).radius;
             outerRadius = innerRadius + RADIUS_WIDTH;
         }
         else if (bounds instanceof BoundingBox) {
             float xExtent = ((BoundingBox)bounds).xExtent;
             float yExtent = ((BoundingBox)bounds).yExtent;
             float zExtent = ((BoundingBox)bounds).zExtent;
             innerRadius = Math.max(Math.max(xExtent, yExtent), zExtent);
             outerRadius = innerRadius + RADIUS_WIDTH;
         }
 
         // Fetch the world translation for the root node of the cell and set
         // the translation for this entity root node
         Vector3f translation = sceneRoot.getWorldTranslation();
         rootNode.setLocalTranslation(translation);
 
         // Create a tube to rotate about the X axis. The tube is drawn in the
         // X-Z plane, so we must rotate 90 degrees about the +z axis so that the
         // axis of rotation is about +x axis.
         xEntity = new Entity("Tube X");
         xNode = createTube("Tube X", outerRadius, innerRadius, THICKNESS, ColorRGBA.red);
         Quaternion xQ = new Quaternion().fromAngleAxis(1.5707f, new Vector3f(0, 0, 1));
         xNode.setLocalRotation(xQ);
         xNode.setLocalScale(new Vector3f(currentScale, 1.0f, currentScale));
         xNode.setRenderState(zbuf);
         addSubEntity(xEntity, xNode);
         xListener = addRotateListener(xEntity, xNode, RotateAxis.X_AXIS);
 
         // Create a tube to rotate about the Y axis. The tube is drawn in the
         // X-Z plane already.
         yEntity = new Entity("Tube Y");
         yNode = createTube("Tube Y", outerRadius, innerRadius, THICKNESS, ColorRGBA.green);
         yNode.setLocalScale(new Vector3f(currentScale, 1.0f, currentScale));
         yNode.setRenderState(zbuf);
         addSubEntity(yEntity, yNode);
         yListener = addRotateListener(yEntity, yNode, RotateAxis.Y_AXIS);
 
         // Create a tube to rotate about the Z axis. The tube is drawn in the
         // X-Z plane, so we must rotate 90 degrees about the +x axis so that the
         // axis of rotation is about +z axis.
         zEntity = new Entity("Tube Z");
         zNode = createTube("Tube Z", outerRadius, innerRadius, THICKNESS, ColorRGBA.blue);
         Quaternion zQ = new Quaternion().fromAngleAxis(1.5707f, new Vector3f(1, 0, 0));
         zNode.setLocalRotation(zQ);
         zNode.setLocalScale(new Vector3f(currentScale, 1.0f, currentScale));
         zNode.setRenderState(zbuf);
         addSubEntity(zEntity, zNode);
         zListener = addRotateListener(zEntity, zNode, RotateAxis.Z_AXIS);
 
         // Make sure we rotate the affordance based upon the current rotation
         // of the geometry when we first display it.
         Quaternion rotation = cell.getLocalTransform().getRotation(null);
         rootNode.setLocalRotation(rotation);
         
         // Listen for changes to the cell's translation and apply the same
         // update to the root node of the affordances
         final Node[] nodeArray = new Node[1];
         nodeArray[0] = rootNode;
         sceneRoot.addGeometricUpdateListener(updateListener = new GeometricUpdateListener() {
             public void geometricDataChanged(Spatial arg0) {
                 // For the rotate affordance we need to move it whenever the
                 // cell is moved, but also need to rotate it when the cell
                 // rotation changes too. We also need to account for any changes
                 // to the size of the cell's scene graph, so we reset the size
                 // here to take care of that.
                 Vector3f translation = arg0.getWorldTranslation();
                 nodeArray[0].setLocalTranslation(translation);
 
                 Quaternion rotation = arg0.getLocalRotation();
                 nodeArray[0].setLocalRotation(rotation);
                 setSize(currentScale);
                 ClientContextJME.getWorldManager().addToUpdateList(nodeArray[0]);
             }
         });
     }
 
     /**
      * @inheritDoc()
      */
     public void setSize(float size) {
         // To set the scale properly, we need to compute the scale w.r.t the
         // current size of the object as a ratio of the original size of the
         // object (in case the size of the object has changed).
         currentScale = size;
         float scale = 0.0f;
         BoundingVolume bounds = sceneRoot.getWorldBound();
         if (bounds instanceof BoundingSphere) {
             float newInnerRadius = ((BoundingSphere)bounds).radius;
             scale = (newInnerRadius / innerRadius) * currentScale;
         }
         else if (bounds instanceof BoundingBox) {
             float xExtent = ((BoundingBox)bounds).xExtent;
             float yExtent = ((BoundingBox)bounds).yExtent;
             float zExtent = ((BoundingBox)bounds).zExtent;
             float newInnerRadius = Math.max(Math.max(xExtent, yExtent), zExtent);
             scale = (newInnerRadius / innerRadius) * currentScale;
         }
 
         // In order to set the size of the arrows, we just set the scaling. Note
         // that we set the scaling along the (x, z) axis since disks are drawn
         // in the x-z plane
         xNode.setLocalScale(new Vector3f(scale, 1.0f, scale));
         yNode.setLocalScale(new Vector3f(scale, 1.0f, scale));
         zNode.setLocalScale(new Vector3f(scale, 1.0f, scale));
         ClientContextJME.getWorldManager().addToUpdateList(xNode);
         ClientContextJME.getWorldManager().addToUpdateList(yNode);
         ClientContextJME.getWorldManager().addToUpdateList(zNode);
     }
 
     /**
      * @inheritDoc()
      */
     public void remove() {
         // Remove the Entity from the scene graph. We also want to unregister
         // the listener from the cell's node. We need to do this in a special
         // update thread
         ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
             public void update(Object arg0) {
                 ClientContextJME.getWorldManager().removeEntity(RotateAffordance.this);
                 CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
                 RenderComponent cellRC = (RenderComponent) renderer.getEntity().getComponent(RenderComponent.class);
                 cellRC.getSceneRoot().removeGeometricUpdateListener(updateListener);
                 xListener.removeFromEntity(xEntity);
                 yListener.removeFromEntity(yEntity);
                 zListener.removeFromEntity(zEntity);
                 xListener = yListener = zListener = null;
                 xEntity = yEntity = zEntity = null;
             }}, null);
     }
 
     /**
      * Adds a rotation listener for the Entity and the node, given the axis along
      * which the rotate should take place.
      */
     private RotationDragListener addRotateListener(Entity entity, Node node, RotateAxis direction) {
         makeEntityPickable(entity, node);
         RotationDragListener l = new RotationDragListener(direction);
         l.addToEntity(entity);
         return l;
     }
     
    /**
     * Creates the tube used for the rotation affordance, given its name, its
     * outer and inner radius, its thickness, and its color. Returns the Node
     * representing the geometry.
     */
     private Node createTube(String name, float outerRadius, float innerRadius,
             float thickness, ColorRGBA color) {
 
         // Create the disc with the name, radii, and thickness given. Set
         // the color of the tube.
         Tube t = new Tube(name, outerRadius, innerRadius, thickness, 50, 50);
         t.setSolidColor(color);
 
         // Create the main node and set the material state on the node so the
         // color shows up. Attach the tube to the node.
         Node n = new Node();
         RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
         MaterialState matState3 = (MaterialState)rm.createRendererState(RenderState.RS_MATERIAL);
         matState3.setDiffuse(color);
         n.setRenderState(matState3);
         n.attachChild(t);
 
         // Set the bound on the tube and update it
         t.setModelBound(new BoundingSphere());
         t.updateModelBound();
         return n;
     }
 
     /**
      * Inner class that handles the dragging movement and updates the rotation
      * of the cell accordingly
      */
     private class RotationDragListener extends EventClassListener {
 
         // The axis along which to effect the rotation
         private RotateAxis direction;
 
         // The intersection point on the entity over which the button was
         // pressed, in world coordinates.
         private Vector3f dragStartWorld;
 
         // The screen coordinates of the button press event.
         private Point dragStartScreen;
 
         // The rotation of the cell when the mouse button is first pressed.
         private Quaternion rotationOnPress = null;
 
         // The center of the affordance in world coordinates
         private Vector3f centerWorld;
 
         // The vector of the starting point of the drag with respect to the
         // center of the afforance
         private Vector3f dragStartVectorWorld;
 
         // The label (and frame) to display the current rotation amount
         private JFrame labelFrame = null;
         private JLabel rotationLabel = null;
 
         public RotationDragListener(RotateAxis direction) {
             this.direction = direction;
 
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
             rotationLabel = new JLabel("0.00 degrees");
             labelPanel.add(rotationLabel);
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
                 MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D)event;
                 if (buttonEvent.isPressed() && buttonEvent.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1) {
                     // Figure out where the button press is in screen and world
                     // coordinates. Also fetch the current rotation for cell.
                     MouseEvent awtButtonEvent = (MouseEvent)buttonEvent.getAwtEvent();
                     dragStartScreen = new Point(awtButtonEvent.getX(), awtButtonEvent.getY());
                     dragStartWorld = buttonEvent.getIntersectionPointWorld();
                     rotationOnPress = transform.getRotation(null);
                     
                     // Figure out the world coordinates of the center of the
                     // affordance.
                     Entity entity = event.getEntity();
                     RenderComponent rc = (RenderComponent)entity.getComponent(RenderComponent.class);
                     centerWorld = rc.getSceneRoot().getWorldTranslation();
                     
                     // Compute the vector from the starting point of the drag
                     // to the center of the affordance in world coordinates.
                     dragStartVectorWorld = dragStartWorld.subtract(centerWorld);
 
                     // Set the initial value of the label to 0.0 and display
                     setLabelPosition(awtMouseEvent);
                     labelFrame.toFront();
                     labelFrame.setVisible(true);
                     labelFrame.repaint();
                 }
                 else if (buttonEvent.isReleased() == true) {
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
             // start of the drag and add the bit we dragged the mouse.
             Vector3f dragEndVectorWorld = dragStartVectorWorld.add(dragWorld);
 
             // Formulate the two vectors in two dimensions. For now, we just
             // ignore the third dimension (set it to 0). The two new vectors
             // depend upon what axis we are rotating around. We also figure
             // out the axis normal and the axis of rotation
             Vector3f v1 = null, v2 = null, normal = null, axis = null;
             switch (direction) {
                 case X_AXIS:
                     v1 = new Vector3f(0, dragStartVectorWorld.y, dragStartVectorWorld.z).normalize();
                     v2 = new Vector3f(0, dragEndVectorWorld.y, dragEndVectorWorld.z).normalize();
                     normal = new Vector3f(1, 0, 0);
                     axis = new Vector3f(-1, 0, 0);
                     break;
 
                 case Y_AXIS:
                     v1 = new Vector3f(dragStartVectorWorld.x, 0, dragStartVectorWorld.z).normalize();
                     v2 = new Vector3f(dragEndVectorWorld.x, 0, dragEndVectorWorld.z).normalize();
                     normal = new Vector3f(0, 1, 0);
                     axis = new Vector3f(0, -1, 0);
                     break;
 
                 case Z_AXIS:
                     v1 = new Vector3f(dragStartVectorWorld.x, dragStartVectorWorld.y, 0).normalize();
                     v2 = new Vector3f(dragEndVectorWorld.x, dragEndVectorWorld.y, 0).normalize();
                     normal = new Vector3f(0, 0, 1);
                     axis = new Vector3f(0, 0, -1);
                     break;
                     
                 default:
                     // This should never happen, so just return
                     return;
             }
 
             // Compute the signed angle between v1 and v2. We do this with the
             // following formula: angle = atan2(normal dot (v1 cross x2), v1 dot v2)
             float dotProduct = v2.dot(v1);
             Vector3f crossProduct = v2.cross(v1);
             double angle = Math.atan2(normal.dot(crossProduct), dotProduct);
 
             // Set the label with the amount that we have rotated it. We display
             // the rotated amount to two decimal points
             StringBuilder rotateString = new StringBuilder();
             Formatter formatter = new Formatter(rotateString);
             formatter.format("%.2f degrees", Math.toDegrees(angle));
             rotationLabel.setText(rotateString.toString());
             labelFrame.pack();
 
             // Figure out where to place the label based upon the location of
             // the event.
             setLabelPosition(awtMouseEvent);
 
             // Rotate the object along the defined axis and angle.
             Quaternion q = new Quaternion().fromAngleAxis((float)angle, axis);
             Quaternion newRotation = rotationOnPress.mult(q);
             transform.setRotation(newRotation);
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
