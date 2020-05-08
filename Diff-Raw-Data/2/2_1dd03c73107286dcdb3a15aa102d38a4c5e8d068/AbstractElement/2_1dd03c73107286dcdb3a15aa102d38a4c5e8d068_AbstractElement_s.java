 package com.kluckerz.lmnts;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.material.Material;
 import com.jme3.math.FastMath;
 import com.jme3.math.Quaternion;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.control.Control;
 import java.util.ResourceBundle;
 
 /**
  * Generic element class.
  * @author Gerald Backmeister <gerald at backmeister.name>
  */
 public abstract class AbstractElement {
     
     private Node node;
     
     private Spatial spatial;
     
     private int rotationXChange;
     
     private int rotationYChange;
     
     private int rotationZChange;
     
     private Control control;
     
     /**
      * Creates a new generic element.
      */
     public AbstractElement() {
     }
     
     /**
      * Initializes the element.
      * @param assetManager The application's assetmanager, used to load models.
      */
     public void init(final AssetManager assetManager) {
         spatial = createNode(assetManager);
         node = (Node)spatial;
     }
     
     private Spatial createNode(final AssetManager assetManager) {
         String modelPath = getModelPath();
         Spatial s = assetManager.loadModel(modelPath);
         // TODO: dummy material - change later
         Material material = new Material(assetManager,
                 // "Common/MatDefs/Misc/Unshaded.j3md");
                 "Common/MatDefs/Light/Lighting.j3md");
         /*
         TextureKey key2 = new TextureKey("Interface/Icons/icon-32.png");
         key2.setGenerateMips(true);
         Texture tex2 = assetManager.loadTexture(key2);
         material.setTexture("ColorMap", tex2);
          */
        s.setMaterial(material);
         return s;
     }
     
     public void addPhysics(final BulletAppState bulletAppState) {
         CollisionShape cs = CollisionShapeFactory.createMeshShape(spatial);
         RigidBodyControl rbc = new RigidBodyControl(cs, 5f);
         rbc.setKinematic(true);
         control = rbc;
         node.addControl(control);
         bulletAppState.getPhysicsSpace().add(control);
     }
     
     /**
      * Returns the node representing the element in the 3D scene.
      * @return The node.
      */
     public Node getNode() {
         return node;
     }
     
     /**
      * Rotates the element clockwise on x-axis.
      */
     public void rotateXClockwise() {
         rotationXChange += 90;
     }
     
     /**
      * Rotates the element counter-clockwise on x-axis.
      */
     public void rotateXCounterClockwise() {
         rotationXChange -= 90;
     }
     
     /**
      * Rotates the element clockwise on y-axis.
      */
     public void rotateYClockwise() {
         rotationYChange += 90;
     }
     
     /**
      * Rotates the element counter-clockwise on y-axis.
      */
     public void rotateYCounterClockwise() {
         rotationYChange -= 90;
     }
     
     /**
      * Rotates the element clockwise on z-axis.
      */
     public void rotateZClockwise() {
         rotationZChange += 90;
     }
     
     /**
      * Rotates the element counter-clockwise on z-axis.
      */
     public void rotateZCounterClockwise() {
         rotationZChange -= 90;
     }
     
     /**
      * Used to check, if the rotation animation has finished.
      * @return True, if no more animation is pending.
      */
     public boolean isAnimationFinished() {
         return rotationXChange == 0 && rotationYChange == 0 && rotationZChange == 0;
     }
     
     /**
      * Used to update the animation state (go forward one step).
      */
     public void updateAnimation() {
         if(rotationXChange > 0) {
             stepRotate(FastMath.DEG_TO_RAD, 0, 0);
             rotationXChange -= 1;
         } else if(rotationXChange < 0) {
             stepRotate(-1 * FastMath.DEG_TO_RAD, 0, 0);
             rotationXChange += 1;
         }
         
         if(rotationYChange > 0) {
             stepRotate(0, FastMath.DEG_TO_RAD, 0);
             rotationYChange -= 1;
         } else if(rotationYChange < 0) {
             stepRotate(0, -1 * FastMath.DEG_TO_RAD, 0);
             rotationYChange += 1;
         }
         
         if(rotationZChange > 0) {
             stepRotate(0, 0, FastMath.DEG_TO_RAD);
             rotationZChange -= 1;
         } else if(rotationZChange < 0) {
             stepRotate(0, 0, -1 * FastMath.DEG_TO_RAD);
             rotationZChange += 1;
         }
     }
     
     private void stepRotate(final float x, final float y, final float z) {
         Quaternion r = new Quaternion(new float[] {x, y, z});
         r.multLocal(node.getLocalRotation());
         node.setLocalRotation(r);
     }
     
     @Override
     public String toString() {
         return getTypeName() + "[" + super.toString() + "]";
     }
     
     protected ResourceBundle getResourceBundle() {
         return ResourceBundle.getBundle("Interface/Language/BuiltInElements");
     }
     
     protected String getLabelByResourceBundle(final String key) {
         ResourceBundle rb = getResourceBundle();
         return rb.getString(key);
     }
     
     public String getLabel() {
         String typeName = getTypeName();
         return getLabelByResourceBundle(typeName);
     }
     
     protected abstract String getModelPath();
     
     protected abstract String getTypeName();
     
 }
