 /*
  * ModelRepresentation.java
  *
  * Created on June 9, 2007, 3:36 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics.objects.representations;
 
 import com.jme.math.Matrix3f;
 import com.jme.math.Vector3f;
 import com.jme.scene.Geometry;
 import com.jme.scene.Node;
 import com.jme.scene.SceneElement;
 import com.jme.scene.Spatial;
 import com.jme.scene.batch.GeomBatch;
 import com.jme.scene.state.RenderState;
 import edu.gatech.statics.Representation;
 import edu.gatech.statics.RepresentationLayer;
 import edu.gatech.statics.objects.SimulationObject;
 import edu.gatech.statics.util.Pair;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class ModelRepresentation extends Representation {
 
     private Node modelNode;
 
     public void setModelOffset(Vector3f offset) {
         modelNode.setLocalTranslation(offset);
     }
 
     public void setModelScale(float scale) {
         modelNode.setLocalScale(scale);
     }
 
     public void setModelScale(float scaleX, float scaleY, float scaleZ) {
         modelNode.setLocalScale(new Vector3f(scaleX, scaleY, scaleZ));
     }
 
     public void setModelRotation(Matrix3f rotation) {
         modelNode.setLocalRotation(rotation);
     }
     List<Pair<SceneElement, RenderState>> originalMaterialStates = new ArrayList<Pair<SceneElement, RenderState>>();
 
     ModelRepresentation(SimulationObject target, Node modelNode) {
         super(target);
         setLayer(RepresentationLayer.modelBodies);
         this.modelNode = modelNode;
 
         getRelativeNode().attachChild(modelNode);
 
         //updateMaterial();
         updateRenderState();
 
         recordRenderStates(modelNode);
     }
 
     /**
      * This stores the render states that might be changed when the model representation needs to appear differently.
      * Right now, this records the material and texture states, and this is used for whenever the representation needs to appear
      * grayed.
      * This is called in the constructor, so it should not be overridden.
      * @param node
      */
     private void recordRenderStates(Node node) {
         if (node.getChildren() == null) {
             return;
         }
 
         for (Spatial child : node.getChildren()) {
             // record its render state
             RenderState renderState = child.getRenderState(RenderState.RS_MATERIAL);
             if (renderState != null) {
                 originalMaterialStates.add(new Pair<SceneElement, RenderState>(child, renderState));
             }
             renderState = child.getRenderState(RenderState.RS_TEXTURE);
             if (renderState != null) {
                 originalMaterialStates.add(new Pair<SceneElement, RenderState>(child, renderState));
             }
 
             if (child instanceof Node) {
                 // traverse children
                 recordRenderStates((Node) child);
             }
             if (child instanceof Geometry) {
                 Geometry geom = (Geometry) child;
                 for (int i = 0; i < geom.getBatchCount(); i++) {
                     GeomBatch batch = geom.getBatch(i);
 
                     // record geometry batches
                     renderState = batch.getRenderState(RenderState.RS_MATERIAL);
                     if (renderState != null) {
                         originalMaterialStates.add(new Pair<SceneElement, RenderState>(batch, renderState));
                     }
                     renderState = batch.getRenderState(RenderState.RS_TEXTURE);
                     if (renderState != null) {
                         originalMaterialStates.add(new Pair<SceneElement, RenderState>(batch, renderState));
                     }
                 }
             }
         }
     }
 
     private void setOverridenRenderStates() {
         for (Pair<SceneElement, RenderState> pair : originalMaterialStates) {
             // clear render states in the pairs, because there may be duplicates, the contents
             // of this loop might be called several times for the same scene elements
             SceneElement element = pair.getLeft();
             element.clearRenderState(RenderState.RS_MATERIAL);
             element.clearRenderState(RenderState.RS_TEXTURE);
         }
     }
 
     private void setNormalRenderStates() {
         for (Pair<SceneElement, RenderState> pair : originalMaterialStates) {
             // add every pair we find
             SceneElement element = pair.getLeft();
             RenderState renderState = pair.getRight();
             element.setRenderState(renderState);
         }
     }
 
     @Override
     protected void updateRepresentationBounds() {
         //super.updateRepresentationBounds();
         // do not propagate bounds for model representations
     }
     private boolean wasGrayed = false;
 //    private boolean wasSelected = false;
 //    private boolean wasHighlighted = false;
 
     @Override
     protected void updateMaterial() {
         super.updateMaterial();
 
         //boolean grayedChanged = wasGrayed != getDisplayGrayed();
         //boolean selectedChanged = wasSelected != isSelected();
 
         boolean doChange = false;
         if(wasGrayed != getDisplayGrayed()) {
             wasGrayed = getDisplayGrayed();
             doChange = true;
         }
 //        if(wasSelected != isSelected()) {
 //            wasSelected = isSelected();
 //            doChange = true;
 //        }
 //        if(wasHighlighted != isHover()) {
 //            wasHighlighted = isHover();
 //            doChange = true;
 //        }
 
         if(doChange) {
//System.out.println("updating material!");
 
             //if (getDisplayGrayed() || isSelected() || isHover()) {
             if (getDisplayGrayed()) {
                 setOverridenRenderStates();
             } else {
                 setNormalRenderStates();
             }
             setRenderStateChanged(true);
         }
 
     }
 }
