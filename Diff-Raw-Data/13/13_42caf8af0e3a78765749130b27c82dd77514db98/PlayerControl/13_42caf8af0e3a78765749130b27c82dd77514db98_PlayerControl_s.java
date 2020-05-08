 /*
  * Copyright (c) 2009-2012 jMonkeyEngine
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * * Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright
  *   notice, this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  *
  * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package control;
 
 import com.jme3.bullet.PhysicsSpace;
 import com.jme3.bullet.PhysicsTickListener;
 import com.jme3.bullet.collision.PhysicsRayTestResult;
 import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
 import com.jme3.bullet.objects.PhysicsRigidBody;
 import com.jme3.export.InputCapsule;
 import com.jme3.export.JmeExporter;
 import com.jme3.export.JmeImporter;
 import com.jme3.export.OutputCapsule;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.math.FastMath;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.RenderManager;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.control.Control;
 import com.jme3.util.TempVars;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * This is intended to be a replacement for the internal bullet character class.
  * A RigidBody with cylinder collision shape is used and its velocity is set
  * continuously, a ray test is used to check if the character is on the ground.
  *
  * Forces in the local x/z plane are dampened while those in the local y
  * direction are applied fully (e.g. jumping, falling).
  *
  * @author normenhansen
  */
 public class PlayerControl extends AbstractPhysicsControl implements PhysicsTickListener, ActionListener {
 
     //Animation code from hello animation tutorial
    // private AnimChannel channel;
    // private AnimControl control;
     
     //End animation code
     protected static final Logger logger = Logger.getLogger(PlayerControl.class.getName());
     protected PhysicsRigidBody rigidBody;
     protected float radius;
     protected float height;
     protected float mass;
     /**
      * Stores final spatial location, corresponds to RigidBody location.
      */
     protected final Vector3f location = new Vector3f();
     protected final Vector3f walkVelocity = new Vector3f();
     protected final Vector3f velocity = new Vector3f();
     protected float jumpSpeed;
     protected boolean initiateJumpInNextTick = false;
     protected boolean onGround = false;
     protected boolean abortJumpInNextTick = false;
     protected float gravity = -40f;
     private boolean pushBackInNextTick;
     private Vector3f pushBackVelocityAdjustment = new Vector3f(-10f, 10f, 0f);
 
     /**
      * Only used for serialization, do not use this constructor.
      */
     public PlayerControl() {
         jumpSpeed = 0f;
     }
 
     
     /**
      * Creates a new character with the given properties. The jumpSpeed will be
      * set to a default value of 1.25 * mass.
      *
      * @param radius
      * @param height
      * @param mass
      */
     public PlayerControl(float radius, float height, float mass) {
         this.radius = radius;
         this.height = height;
         this.mass = mass;
         rigidBody = new PhysicsRigidBody(getShape(), mass);
         jumpSpeed = mass * 1.25f;
         rigidBody.setAngularFactor(0);
         
     }
 
     @Override
     public void update(float tpf) {
         super.update(tpf);
         rigidBody.getPhysicsLocation(location);
         // rotation is never checked since the character can't rotate
         applyPhysicsTransform(location, Quaternion.DIRECTION_Z);
         
     }
 
     @Override
     public void render(RenderManager rm, ViewPort vp) {
         super.render(rm, vp);
     }
 
     /**
      * Used internally, don't call manually. Updates all movement that is not
      * involuntary, i.e. running, jumping and being pushed back.
      *
      * @param space
      * @param tpf
      */
     public void prePhysicsTick(PhysicsSpace space, float tpf) {
 
         // running
         checkOnGround();
 
         if (onGround) {
             float designatedVelocity = walkVelocity.length();
 
             if (designatedVelocity > 0) {
                 TempVars vars = TempVars.get();
                 Vector3f walkingVelocityAdjustment = vars.vect1;
                 //normalize walkdirection
                 walkingVelocityAdjustment.set(walkVelocity).normalizeLocal();
                 //check for the existing velocity in the desired direction
                 float existingWalkingSpeed = velocity.dot(walkVelocity.normalize());
                 //calculate the final velocity in the desired direction
                 float finalWalkingSpeed = designatedVelocity - existingWalkingSpeed;
                 walkingVelocityAdjustment.multLocal(finalWalkingSpeed);
                 //add resulting vector to existing velocity
                 velocity.addLocal(walkingVelocityAdjustment);
                 vars.release();
             }
         }
 
         // jumping
         float designatedUpwardsVelocity = 0;
 
         if (initiateJumpInNextTick) {
             initiateJumpInNextTick = false;
             designatedUpwardsVelocity = jumpSpeed;
         }
 
         if (abortJumpInNextTick) {
             abortJumpInNextTick = false;
             if (velocity.getY() > jumpSpeed * 0.61f) {
                 designatedUpwardsVelocity = jumpSpeed * 0.61f;
             }
         }
 
         if (designatedUpwardsVelocity > 0) {
             velocity.setY(designatedUpwardsVelocity);
         }
 
         //pushback
         if (pushBackInNextTick) {
             pushBackInNextTick = false;
             velocity.setX(-walkVelocity.length());
             velocity.setY(jumpSpeed);
         }
 
         // updating the velocity including both running and jumping
         rigidBody.setLinearVelocity(velocity);
 
     }
 
     /**
      * Used internally, don't call manually
      *
      * @param space
      * @param tpf
      */
     public void physicsTick(PhysicsSpace space, float tpf) {
         rigidBody.getLinearVelocity(velocity);
     }
 
     /**
      * Move the character somewhere. Note the character also takes the location
      * of any spatial its being attached to in the moment it is attached.
      *
      * @param vec The new character location.
      */
     public void warp(Vector3f vec) {
         setPhysicsLocation(vec);
     }
 
     /**
      * Makes the character jump with the set jump force. The jump will be a
      * maximum height jump unless abortJump is called shortly hereafter.
      */
     public void initiateJump() {
         if (!onGround) {
             return;
         }
         initiateJumpInNextTick = true;
     }
 
     /**
      * Makes the character abort the ascent in a jump. Calling this partway
      * through a jump makes the jump shorter.
      */
     public void abortJump() {
         abortJumpInNextTick = true;
     }
 
     /**
      * Makes the character fly backwards.
      */
     public void pushBack() {
         pushBackInNextTick = true;
     }
 
     /**
      * Set the jump force.
      *
      * @param jumpSpeed The new jump force
      */
     public void setJumpSpeed(float jumpForce) {
         this.jumpSpeed = jumpForce;
     }
 
     /**
      * Gets the current jump force. The default is 1.25 * character mass.
      *
      * @return The current jumpforce.
      */
     public float getJumpSpeed() {
         return jumpSpeed;
     }
 
     /**
      * Check if the character is on the ground. This is determined by a ray test
      * in the center of the character and might return false even if the
      * character is not falling yet.
      *
      * @return
      */
     public boolean isOnGround() {
         return onGround;
     }
 
     /**
      * Sets the walk direction of the character. This parameter is framerate
      * independent and the character will move continuously in the direction
      * given by the vector with the speed given by the vector length in m/s.
      *
      * @param vec The movement direction and speed in m/s
      */
     public void setWalkVelocity(Vector3f vec) {
         walkVelocity.set(vec);
     }
 
     /**
      * Gets the current walk direction and speed of the character. The length of
      * the vector defines the speed.
      *
      * @return
      */
     public Vector3f getWalkVelocity() {
         return walkVelocity;
     }
 
     /**
      * Get the current linear velocity along the three axes of the character.
      * This is prepresented in world coordinates, parent coordinates when the
      * control is set to applyLocalPhysics.
      *
      * @return The current linear velocity of the character
      */
     public Vector3f getVelocity() {
         return velocity;
     }
     
     private static final float EPSILON = 0.1f;
         
     /**
      * This checks if the character is on the ground by doing a ray test.
      */
     protected void checkOnGround() {
         TempVars vars = TempVars.get();
         Vector3f rayStart = vars.vect1;
         Vector3f rayEnd = vars.vect2;
         Vector3f radiusOffset = vars.vect3;
         radiusOffset.set(this.walkVelocity).normalizeLocal().multLocal(this.radius - EPSILON);
         //test "back foot"
         rayStart.set(Vector3f.UNIT_Y).multLocal(height).addLocal(this.location).subtractLocal(radiusOffset);
         rayEnd.set(Vector3f.UNIT_Y).multLocal(-height - EPSILON).addLocal(rayStart);
         List<PhysicsRayTestResult> backFootResults = space.rayTest(rayStart, rayEnd);
         // test "front foot"
         rayStart.addLocal(radiusOffset.mult(2));
         rayEnd.addLocal(radiusOffset.mult(2));
         List<PhysicsRayTestResult> frontFootResults = space.rayTest(rayStart, rayEnd);
         vars.release();
         for (PhysicsRayTestResult physicsRayTestResult : backFootResults) {
            if (!physicsRayTestResult.getCollisionObject().equals(rigidBody)) {
                onGround = true;
                 return;
             }
         }
         for (PhysicsRayTestResult physicsRayTestResult : frontFootResults) {
            if (!physicsRayTestResult.getCollisionObject().equals(rigidBody)) {
                 onGround = true;
                 return;
             }
         }
         onGround = false;
     }
 
     /**
      * Responds to the action "initiateJump" by making the player initiateJump.
      * Currently have the same initiateJump behaviour as
      * <code>CharacterControl</code>.
      */
     public void onAction(String name, boolean isPressed, float tpf) {
         if (name.equals("jump")) {
             if (isPressed) {
                 // Button down
                 initiateJump();
             } else {
                 // Button up
                 abortJump();
             }
         }
     }
 
     /**
      * Gets a new collision shape based on the current scale parameter. The
      * created collisionshape is a capsule collision shape that is attached to a
      * compound collision shape with an offset to set the object center at the
      * bottom of the capsule.
      *
      * @return
      */
     protected CollisionShape getShape() {
         //TODO: cleanup size mess..
         CapsuleCollisionShape capsuleCollisionShape = new CapsuleCollisionShape(radius, height - (2 * radius));
         CompoundCollisionShape compoundCollisionShape = new CompoundCollisionShape();
         Vector3f addLocation = new Vector3f(0, (height / 2), 0);
         compoundCollisionShape.addChildShape(capsuleCollisionShape, addLocation);
         return compoundCollisionShape;
     }
 
     /**
      * This is implemented from AbstractPhysicsControl and called when the
      * spatial is attached for example.
      *
      * @param vec
      */
     @Override
     protected void setPhysicsLocation(Vector3f vec) {
         rigidBody.setPhysicsLocation(vec);
         location.set(vec);
     }
 
     /**
      * We set the current spatial as UserObject so the user can find his
      * spatial.
      *
      * @param spatial
      */
     @Override
     public void setSpatial(Spatial spatial) {
         super.setSpatial(spatial);
         rigidBody.setUserObject(spatial);
     }
 
     /**
      * This is required by AbstractPhysicsControl. It does nothing, since the
      * player cannot rotate.
      *
      * @param quat
      */
     @Override
     protected void setPhysicsRotation(Quaternion quat) {
         // does nothing
     }
 
     /**
      * This is implemented from AbstractPhysicsControl and called when the
      * control is supposed to add all objects to the physics space.
      *
      * @param space
      */
     @Override
     protected void addPhysics(PhysicsSpace space) {
         space.addCollisionObject(rigidBody);
         rigidBody.setGravity(new Vector3f(0f, gravity, 0f));
         space.addTickListener(this);
     }
 
     /**
      * This is implemented from AbstractPhysicsControl and called when the
      * control is supposed to remove all objects from the physics space.
      *
      * @param space
      */
     @Override
     protected void removePhysics(PhysicsSpace space) {
         space.removeCollisionObject(rigidBody);
         space.removeTickListener(this);
     }
 
     public Control cloneForSpatial(Spatial spatial) {
         PlayerControl control = new PlayerControl(radius, height, mass);
         control.setJumpSpeed(jumpSpeed);
         control.setSpatial(spatial);
         return control;
     }
 
     @Override
     public void write(JmeExporter ex) throws IOException {
         super.write(ex);
         OutputCapsule oc = ex.getCapsule(this);
         oc.write(radius, "radius", 1);
         oc.write(height, "height", 1);
         oc.write(mass, "mass", 1);
         oc.write(jumpSpeed, "jumpForce", 5);
     }
 
     @Override
     public void read(JmeImporter im) throws IOException {
         super.read(im);
         InputCapsule in = im.getCapsule(this);
         this.radius = in.readFloat("radius", 1);
         this.height = in.readFloat("height", 2);
         this.mass = in.readFloat("mass", 80);
         this.jumpSpeed = in.readFloat("jumpForce", mass * 5);
 
         rigidBody = new PhysicsRigidBody(getShape(), mass);
         rigidBody.setAngularFactor(0);
     }
 }
