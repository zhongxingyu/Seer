 package com.ggervais.gameengine.physics;
 
 import com.ggervais.gameengine.math.MathUtils;
 import com.ggervais.gameengine.math.RotationMatrix;
 import com.ggervais.gameengine.math.Vector3D;
 import com.ggervais.gameengine.physics.collision.Collision;
 import com.ggervais.gameengine.scene.scenegraph.Spatial;
 import com.ggervais.gameengine.scene.scenegraph.Transformation;
 import com.ggervais.gameengine.timing.Controller;
 import org.apache.log4j.Logger;
 
 import java.util.List;
 
 public class MotionController extends Controller {
     private Logger log = Logger.getLogger(MotionController.class);
     private Vector3D gravity; // In m/s^2
     private Vector3D initialVelocity; // In m/s
     private Transformation initialTransformation;
     private boolean rotateWhileMoving;
     private boolean firstUpdateOccurred;
 
     public MotionController(Vector3D gravity, float speed, float theta, float phi) {
         this(gravity, Vector3D.createFromPolarCoordinates(speed, theta, phi), true);
     }
 
     public MotionController(Vector3D gravity, float speed, float theta, float phi, boolean rotateWhileMoving) {
         this(gravity, Vector3D.createFromPolarCoordinates(speed, theta, phi), rotateWhileMoving);
     }
 
     public MotionController(Vector3D gravity, Vector3D initialVelocity) {
         this(gravity, initialVelocity, true);
     }
 
     public MotionController(Vector3D gravity, Vector3D initialVelocity, boolean rotateWhileMoving) {
         this.gravity = gravity;
         this.initialVelocity = initialVelocity;
         this.initialTransformation = new Transformation();
         this.rotateWhileMoving = rotateWhileMoving;
         this.firstUpdateOccurred = false;
     }
 
     private Spatial getTopParent() {
 
         Spatial current = this.controlledSpatialObject;
         Spatial parent = current.getParent();
 
         while (parent != null) {
             current = parent;
             parent = current.getParent();
         }
 
         return current;
     }
 
     @Override
     public void setControlledObject(Spatial object) {
         super.setControlledObject(object);
         if (object != null) {
             Transformation transformation = object.getLocalTransformation();
             if (transformation != null) {
                 this.initialTransformation.setTranslation(transformation.getTranslation().copy());
                 this.initialTransformation.setScale(transformation.getScale().copy());
 
                 RotationMatrix copy = new RotationMatrix();
                 for (int i = 1; i <= 4; i++) {
                     for (int j = 1; j <= 4; j++) {
                         copy.setElement(i, j, transformation.getRotationMatrix().getElement(i, j));
                     }
                 }
 
                 this.initialTransformation.setRotationMatrix(copy);
             }
         }
     }
 
     @Override
     public void doUpdate(long currentTime) {
         long dtTotal = currentTime - this.startTime - this.pauseOffset;
         float dtTotalSeconds = dtTotal / 1000f;
 
         long dt = currentTime - this.lastUpdateTime;
         float dtSeconds = dt / 1000f;
 
         // Newton's laws of motion.
         Vector3D currentVelocity = Vector3D.add(this.initialVelocity, this.gravity.copy().multiplied(dtTotalSeconds));
 
         //Vector3D currentTranslation = Vector3D.add(this.initialTransformation.getTranslation(), Vector3D.add(this.initialVelocity, currentVelocity).multiplied(0.5f * dtSeconds));
         Vector3D baseTranslation = this.controlledSpatialObject.getLocalTransformation().getTranslation();
         Vector3D candidateTranslation = Vector3D.add(baseTranslation, currentVelocity.multiplied(dtSeconds));
 
         if (this.firstUpdateOccurred && this.controlledSpatialObject.isCheckCollisionsWhenMoving()) {
             Spatial root = getTopParent();
             List<Collision> collisions = this.controlledSpatialObject.intersectsWithUnderlyingGeometry(root);
             if (collisions.size() > 0) {
                 for (Collision collision : collisions) {
                     if (collision.getFirst() == this.controlledSpatialObject && collision.getSecond() != this.controlledSpatialObject) {
 
                         float minComponent = Float.MAX_VALUE;
                         int minAxis = 0;
 
                         for (int i = 0; i < 3; i++) {
                             if (Math.abs(collision.getPenetrationVector().get(i)) < Math.abs(minComponent)) {
                                 minComponent = collision.getPenetrationVector().get(i);
                                 minAxis = i;
                             }
                         }
 
                         if (collision.getPenetrationVector().get(minAxis) < Float.MAX_VALUE) {
                             candidateTranslation.set(minAxis, candidateTranslation.get(minAxis) - collision.getPenetrationVector().get(minAxis));
                            //this.initialVelocity = Vector3D.zero();
                         }
                     }
                 }
             }
         }
 
         Vector3D normalizedRotation = currentVelocity.normalized();
 
         if (this.controlledSpatialObject != null) {
             Transformation transformation = this.controlledSpatialObject.getLocalTransformation();
             if (transformation != null) {
                 transformation.setTranslation(candidateTranslation);
 
                 RotationMatrix initialRotation = this.initialTransformation.getRotationMatrix();
 
                 float theta = (float) Math.asin(MathUtils.clamp(normalizedRotation.y(), -1, 1));
                 float phi = (float) Math.asin(MathUtils.clamp(normalizedRotation.z() / ((float) Math.cos(theta)), -1, 1));
 
                 RotationMatrix diffMatrix = RotationMatrix.createFromXYZ(0, -phi, theta);
 
                 RotationMatrix tempMatrix = new RotationMatrix();
                 tempMatrix.mult(diffMatrix);
                 tempMatrix.mult(initialRotation);
 
                 if (this.rotateWhileMoving) {
                    transformation.setRotationMatrix(tempMatrix);
                 }
             }
         }
 
         if (!this.firstUpdateOccurred) {
             this.firstUpdateOccurred = true;
         }
     }
 }
