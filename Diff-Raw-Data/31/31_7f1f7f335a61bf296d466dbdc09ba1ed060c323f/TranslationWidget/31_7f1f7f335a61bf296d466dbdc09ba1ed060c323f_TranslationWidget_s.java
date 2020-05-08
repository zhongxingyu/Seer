 package com.se.simplicity.editor.internal;
 
 import com.se.simplicity.editor.internal.selection.SceneSelection;
 import com.se.simplicity.jogl.model.shape.GLUCapsule;
 import com.se.simplicity.jogl.model.shape.GLUSphere;
 import com.se.simplicity.model.shape.Capsule;
 import com.se.simplicity.model.shape.Shape;
 import com.se.simplicity.model.shape.Sphere;
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
  * A widget that manipulates the translation of the currently selected scene component.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class TranslationWidget implements Widget
 {
     /**
      * <p>
      * The factor to scale the length of each {@link com.se.simplicity.jogl.model.shape.GLUCapsule GLUCapsule} by.
      * </p>
      */
     private static final float CAPSULE_LENGTH_SCALE_FACTOR = 0.1f;
 
     /**
      * <p>
      * The factor to scale the radius of each {@link com.se.simplicity.jogl.model.shape.GLUCapsule GLUCapsule} by.
      * </p>
      */
     private static final float CAPSULE_RADIUS_SCALE_FACTOR = 0.01f;
 
     /**
      * <p>
      * The number of slices to render each {@link com.se.simplicity.jogl.model.shape.GLUCapsule GLUCapsule} with.
      * </p>
      */
     private static final int CAPSULE_SLICES = 20;
 
     /**
      * <p>
      * The angle to rotate the models by so they sit on the correct axis.
      * </p>
      */
     private static final float MODEL_ROTATION = 90.0f;
 
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
      * The factor to scale the translation by.
      * </p>
      */
     private static final float TRANSLATION_FACTOR = 0.01f;
 
     /**
      * <p>
      * The alpha channel value to give the currently unselected {@link com.se.simplicity.scenegraph.model.ModelNode ModelNode}s of a widget.
      * </p>
      */
     private static final float UNSELECTED_MODEL_ALPHA = 0.5f;
 
     /**
      * <p>
      * The {@link com.se.simplicity.scenegraph.model.ModelNode ModelNode} that free translation on the viewport's z plane when selected.
      * </p>
      */
     private ModelNode fFreeSphereNode;
 
     /**
      * <p>
      * The root {@link com.se.simplicity.scenegraph.Node Node} of this widget.
      * </p>
      */
     private Node fRoot;
 
     /**
      * <p>
      * The selected scene component and primitive.
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
      * The {@link com.se.simplicity.scenegraph.model.ModelNode ModelNode} that causes translation on the x axis when selected.
      * </p>
      */
     private ModelNode fXCapsuleNode;
 
     /**
      * <p>
      * The {@link com.se.simplicity.scenegraph.model.ModelNode ModelNode} that causes translation on the y axis when selected.
      * </p>
      */
     private ModelNode fYCapsuleNode;
 
     /**
      * <p>
      * The {@link com.se.simplicity.scenegraph.model.ModelNode ModelNode} that causes translation on the z axis when selected.
      * </p>
      */
     private ModelNode fZCapsuleNode;
 
     /**
      * <p>
      * Creates an instance of <code>TranslationWidget</code>.
      * </p>
      */
     public TranslationWidget()
     {
         fRoot = new SimpleNode();
 
         GLUCapsule xCapsule = new GLUCapsule();
         xCapsule.setColour(new SimpleRGBColourVectorf4(1.0f, 0.0f, 0.0f, UNSELECTED_MODEL_ALPHA));
         xCapsule.setSlices(CAPSULE_SLICES);
         fXCapsuleNode = new SimpleModelNode();
         fXCapsuleNode.getTransformation().rotate((float) Math.toRadians(MODEL_ROTATION), new SimpleTranslationVectorf4(0.0f, 1.0f, 0.0f, 1.0f));
         fXCapsuleNode.setModel(xCapsule);
 
         GLUCapsule yCapsule = new GLUCapsule();
         yCapsule.setColour(new SimpleRGBColourVectorf4(0.0f, 1.0f, 0.0f, UNSELECTED_MODEL_ALPHA));
         yCapsule.setSlices(CAPSULE_SLICES);
         fYCapsuleNode = new SimpleModelNode();
         fYCapsuleNode.getTransformation().rotate((float) Math.toRadians(MODEL_ROTATION), new SimpleTranslationVectorf4(1.0f, 0.0f, 0.0f, 1.0f));
         fYCapsuleNode.setModel(yCapsule);
 
         GLUCapsule zCapsule = new GLUCapsule();
         zCapsule.setColour(new SimpleRGBColourVectorf4(0.0f, 0.0f, 1.0f, UNSELECTED_MODEL_ALPHA));
         zCapsule.setSlices(CAPSULE_SLICES);
         fZCapsuleNode = new SimpleModelNode();
         fYCapsuleNode.getTransformation().rotate((float) Math.PI, new SimpleTranslationVectorf4(0.0f, 1.0f, 0.0f, 1.0f));
         fZCapsuleNode.setModel(zCapsule);
 
         GLUSphere freeSphere = new GLUSphere();
         freeSphere.setColour(new SimpleRGBColourVectorf4(1.0f, 1.0f, 1.0f, UNSELECTED_MODEL_ALPHA));
         freeSphere.setSlices(SPHERE_SLICES_STACKS);
         freeSphere.setStacks(SPHERE_SLICES_STACKS);
         fFreeSphereNode = new SimpleModelNode();
         fFreeSphereNode.setModel(freeSphere);
 
         fRoot.addChild(fXCapsuleNode);
         fRoot.addChild(fYCapsuleNode);
         fRoot.addChild(fZCapsuleNode);
         fRoot.addChild(fFreeSphereNode);
     }
 
     @Override
     public void executeMove(final int x, final int y)
     {
         if (fSelection.getSceneComponent() instanceof Node)
         {
             int invertedY = y * -1;
             SimpleTranslationVectorf4 translation = new SimpleTranslationVectorf4();
 
             if (fSelectedWidgetNode == fXCapsuleNode)
             {
                 translation.translateX((x + invertedY) * TRANSLATION_FACTOR);
             }
             else if (fSelectedWidgetNode == fYCapsuleNode)
             {
                 translation.translateY((x + invertedY) * TRANSLATION_FACTOR);
             }
             else if (fSelectedWidgetNode == fZCapsuleNode)
             {
                 translation.translateZ((x + invertedY) * TRANSLATION_FACTOR);
             }
 
             ((Node) fSelection.getSceneComponent()).getTransformation().translate(translation);
             fRoot.getTransformation().translate(translation);
         }
     }
 
     @Override
     public Node getRootNode()
     {
         return (fRoot);
     }
 
     @Override
     public ModelNode getSelectedWidgetNode()
     {
         return (fSelectedWidgetNode);
     }
 
     @Override
     public SceneSelection getSelection()
     {
         return (fSelection);
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
     public void setSelection(final SceneSelection selection)
     {
         fSelection = selection;
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
         float capsuleLength = CAPSULE_LENGTH_SCALE_FACTOR * distanceCameraToWidget;
         float capsuleRadius = CAPSULE_RADIUS_SCALE_FACTOR * distanceCameraToWidget;
         float sphereRadius = SPHERE_RADIUS_SCALE_FACTOR * distanceCameraToWidget;
 
         ((Capsule) fXCapsuleNode.getModel()).setLength(capsuleLength);
         ((Capsule) fXCapsuleNode.getModel()).setRadius(capsuleRadius);
         ((Capsule) fYCapsuleNode.getModel()).setLength(capsuleLength);
         ((Capsule) fYCapsuleNode.getModel()).setRadius(capsuleRadius);
         ((Capsule) fZCapsuleNode.getModel()).setLength(capsuleLength);
         ((Capsule) fZCapsuleNode.getModel()).setRadius(capsuleRadius);
         ((Sphere) fFreeSphereNode.getModel()).setRadius(sphereRadius);
     }
 }
