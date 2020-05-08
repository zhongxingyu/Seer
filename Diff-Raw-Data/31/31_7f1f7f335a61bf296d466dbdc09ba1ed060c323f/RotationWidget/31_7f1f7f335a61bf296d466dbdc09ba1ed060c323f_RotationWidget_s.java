 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.editor.internal;
 
 import com.se.simplicity.editor.internal.selection.SceneSelection;
 import com.se.simplicity.jogl.model.shape.GLUSphere;
 import com.se.simplicity.jogl.model.shape.GLUTorus;
 import com.se.simplicity.model.shape.Shape;
 import com.se.simplicity.model.shape.Sphere;
 import com.se.simplicity.model.shape.Torus;
 import com.se.simplicity.rendering.Camera;
 import com.se.simplicity.scenegraph.Node;
 import com.se.simplicity.scenegraph.SimpleNode;
 import com.se.simplicity.scenegraph.model.ModelNode;
 import com.se.simplicity.scenegraph.model.SimpleModelNode;
 import com.se.simplicity.vector.SimpleRGBColourVectorf4;
 import com.se.simplicity.vector.SimpleTranslationVectorf4;
 import com.se.simplicity.vector.TransformationMatrixf;
 import com.se.simplicity.vector.Vectorf;
 
 /**
  * <p>
  * A widget that manipulates the rotation of the currently selected scene component.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class RotationWidget implements Widget
 {
     /**
      * <p>
      * The angle to rotate the models by so they sit on the correct axis.
      * </p>
      */
     private static final float MODEL_ROTATION = 90.0f;
 
     /**
      * <p>
      * The factor to scale the inner radius of each {@link com.se.simplicity.jogl.model.shape.GLUTorus GLUTorus} by.
      * </p>
      */
     private static final float INNER_RADIUS_SCALE_FACTOR = 0.01f;
 
     /**
      * <p>
      * The factor to scale the outer radius of each {@link com.se.simplicity.jogl.model.shape.GLUTorus GLUTorus} by.
      * </p>
      */
     private static final float OUTER_RADIUS_SCALE_FACTOR = 0.1f;
 
     /**
      * <p>
      * The factor to scale the radius of the {@link com.se.simplicity.jogl.model.shape.GLUSphere GLUSphere} by.
      * </p>
      */
     private static final float SPHERE_RADIUS_SCALE_FACTOR = 0.02f;
 
     /**
      * <p>
      * The number of slices/stacks to render the {@link com.se.simplicity.jogl.model.shape.GLUSphere GLUSphere} with.
      * </p>
      */
     private static final int SPHERE_SLICES_STACKS = 20;
 
     /**
      * <p>
      * The number of stacks to render each {@link com.se.simplicity.jogl.model.shape.GLUTorus GLUTorus} with.
      * </p>
      */
     private static final int TORUS_STACKS = 30;
 
     /**
      * <p>
      * The alpha channel value to give the currently unselected {@link com.se.simplicity.scenegraph.model.ModelNode ModelNode}s of a widget.
      * </p>
      */
     private static final float UNSELECTED_MODEL_ALPHA = 0.5f;
 
     private ModelNode fFreeSphereNode0;
 
     private ModelNode fFreeSphereNode1;
 
     private ModelNode fFreeSphereNode2;
 
     private ModelNode fFreeSphereNode3;
 
     private ModelNode fFreeSphereNode4;
 
     private ModelNode fFreeSphereNode5;
 
     /**
      * <p>
      * The root {@link com.se.simplicity.scenegraph.Node Node} of this widget.
      * </p>
      */
     private Node fRoot;
 
     /**
      * <p>
      * The currently selected scene component.
      * </p>
      */
     private SceneSelection fSelection;
 
     /**
      * <p>
      * The currently selected {@link com.se.simplicity.scenegraph.model.ModelNode ModelNode} of this widget.
      * </p>
      */
     private ModelNode fSelectedWidgetNode;
 
     /**
      * <p>
      * The {@link com.se.simplicity.scenegraph.model.ModelNode ModelNode} that causes rotation on the x axis when selected.
      * </p>
      */
     private ModelNode fXTorusNode;
 
     /**
      * <p>
      * The {@link com.se.simplicity.scenegraph.model.ModelNode ModelNode} that causes rotation on the y axis when selected.
      * </p>
      */
     private ModelNode fYTorusNode;
 
     /**
      * <p>
      * The {@link com.se.simplicity.scenegraph.model.ModelNode ModelNode} that causes rotation on the z axis when selected.
      * </p>
      */
     private ModelNode fZTorusNode;
 
     /**
      * <p>
      * Creates an instance of <code>RotationWidget</code>.
      * </p>
      */
     public RotationWidget()
     {
         fRoot = new SimpleNode();
 
         GLUTorus xTorus = new GLUTorus();
         xTorus.setColour(new SimpleRGBColourVectorf4(1.0f, 0.0f, 0.0f, UNSELECTED_MODEL_ALPHA));
         xTorus.setStacks(TORUS_STACKS);
         fXTorusNode = new SimpleModelNode();
         fXTorusNode.getTransformation().rotate((float) Math.toRadians(MODEL_ROTATION), new SimpleTranslationVectorf4(0.0f, 1.0f, 0.0f, 1.0f));
         fXTorusNode.setModel(xTorus);
 
         GLUTorus yTorus = new GLUTorus();
         yTorus.setColour(new SimpleRGBColourVectorf4(0.0f, 1.0f, 0.0f, UNSELECTED_MODEL_ALPHA));
         yTorus.setStacks(TORUS_STACKS);
         fYTorusNode = new SimpleModelNode();
         fYTorusNode.getTransformation().rotate((float) Math.toRadians(MODEL_ROTATION), new SimpleTranslationVectorf4(1.0f, 0.0f, 0.0f, 1.0f));
         fYTorusNode.setModel(yTorus);
 
         GLUTorus zTorus = new GLUTorus();
         zTorus.setColour(new SimpleRGBColourVectorf4(0.0f, 0.0f, 1.0f, UNSELECTED_MODEL_ALPHA));
         zTorus.setStacks(TORUS_STACKS);
         fZTorusNode = new SimpleModelNode();
         fZTorusNode.setModel(zTorus);
 
         GLUSphere freeSphere = new GLUSphere();
         freeSphere.setColour(new SimpleRGBColourVectorf4(1.0f, 1.0f, 1.0f, UNSELECTED_MODEL_ALPHA));
         freeSphere.setSlices(SPHERE_SLICES_STACKS);
         freeSphere.setStacks(SPHERE_SLICES_STACKS);
 
         fFreeSphereNode0 = new SimpleModelNode();
         fFreeSphereNode0.setModel(freeSphere);
 
         fFreeSphereNode1 = new SimpleModelNode();
         fFreeSphereNode1.setModel(freeSphere);
 
         fFreeSphereNode2 = new SimpleModelNode();
         fFreeSphereNode2.setModel(freeSphere);
 
         fFreeSphereNode3 = new SimpleModelNode();
         fFreeSphereNode3.setModel(freeSphere);
 
         fFreeSphereNode4 = new SimpleModelNode();
         fFreeSphereNode4.setModel(freeSphere);
 
         fFreeSphereNode5 = new SimpleModelNode();
         fFreeSphereNode5.setModel(freeSphere);
 
         fRoot.addChild(fXTorusNode);
         fRoot.addChild(fYTorusNode);
         fRoot.addChild(fZTorusNode);
         fRoot.addChild(fFreeSphereNode0);
         fRoot.addChild(fFreeSphereNode1);
         fRoot.addChild(fFreeSphereNode2);
         fRoot.addChild(fFreeSphereNode3);
         fRoot.addChild(fFreeSphereNode4);
         fRoot.addChild(fFreeSphereNode5);
     }
 
     @Override
     public void executeMove(final int x, final int y)
     {
         if (fSelection.isEmpty())
         {
             return;
         }
 
         if (fSelection.getSceneComponent() instanceof Node)
         {
             int invertedY = y * -1;
             float angle = 0.0f;
             SimpleTranslationVectorf4 axis = new SimpleTranslationVectorf4();
 
             if (fSelectedWidgetNode == fXTorusNode)
             {
                 angle = x + invertedY;
                 axis.translateX(-1.0f);
             }
             else if (fSelectedWidgetNode == fYTorusNode)
             {
                 angle = x + invertedY;
                 axis.translateY(1.0f);
             }
             else if (fSelectedWidgetNode == fZTorusNode)
             {
                 angle = x + invertedY;
                 axis.translateZ(1.0f);
             }
 
             ((Node) fSelection.getSceneComponent()).getTransformation().rotate((float) Math.toRadians(angle), axis);
             fRoot.getTransformation().rotate((float) Math.toRadians(angle), axis);
         }
     }
 
     @Override
     public Node getRootNode()
     {
         return (fRoot);
     }
 
     @Override
     public SceneSelection getSelection()
     {
         return (fSelection);
     }
 
     @Override
     public ModelNode getSelectedWidgetNode()
     {
         return (fSelectedWidgetNode);
     }
 
     @Override
     public void setSelection(final SceneSelection selection)
     {
         fSelection = selection;
     }
 
     @Override
     public void setSelectedWidgetNode(final ModelNode selectedWidgetNode)
     {
         if (fSelectedWidgetNode != null)
         {
             ((SimpleRGBColourVectorf4) ((Shape) fSelectedWidgetNode.getModel()).getColour()).setAlpha(UNSELECTED_MODEL_ALPHA);
         }
 
         if (selectedWidgetNode != null)
         {
             ((SimpleRGBColourVectorf4) ((Shape) selectedWidgetNode.getModel()).getColour()).setAlpha(1.0f);
         }
 
         fSelectedWidgetNode = selectedWidgetNode;
     }
 
     @Override
     public void updateView(final Camera camera)
     {
         // Transform the Widget to the position and orientation of the selected scene component.
         if (!fSelection.isEmpty())
         {
             if (fSelection.getSceneComponent() instanceof Node)
             {
                 fRoot.setTransformation(((Node) fSelection.getSceneComponent()).getAbsoluteTransformation());
             }
         }
 
         // Determine the distance between the Camera and the Widget.
         TransformationMatrixf cameraTransformation = camera.getNode().getAbsoluteTransformation();
         Vectorf vectorCameraToWidget = cameraTransformation.getTranslation().subtractRightCopy(fRoot.getAbsoluteTransformation().getTranslation());
         float distanceCameraToWidget = Math.abs(vectorCameraToWidget.getLength());
 
         // Scale the Widget based on the distance.
         float innerRadius = INNER_RADIUS_SCALE_FACTOR * distanceCameraToWidget;
         float outerRadius = OUTER_RADIUS_SCALE_FACTOR * distanceCameraToWidget;
         float sphereRadius = SPHERE_RADIUS_SCALE_FACTOR * distanceCameraToWidget;
 
         ((Torus) fXTorusNode.getModel()).setInnerRadius(innerRadius);
         ((Torus) fXTorusNode.getModel()).setOuterRadius(outerRadius);
         ((Torus) fYTorusNode.getModel()).setInnerRadius(innerRadius);
         ((Torus) fYTorusNode.getModel()).setOuterRadius(outerRadius);
         ((Torus) fZTorusNode.getModel()).setInnerRadius(innerRadius);
         ((Torus) fZTorusNode.getModel()).setOuterRadius(outerRadius);
         ((Sphere) fFreeSphereNode0.getModel()).setRadius(sphereRadius);
         fFreeSphereNode0.getTransformation().setXAxisTranslation(outerRadius);
         fFreeSphereNode1.getTransformation().setXAxisTranslation(-outerRadius);
         fFreeSphereNode2.getTransformation().setYAxisTranslation(outerRadius);
         fFreeSphereNode3.getTransformation().setYAxisTranslation(-outerRadius);
         fFreeSphereNode4.getTransformation().setZAxisTranslation(outerRadius);
         fFreeSphereNode5.getTransformation().setZAxisTranslation(-outerRadius);
     }
 }
