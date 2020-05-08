 package com.beartronics.jschema;
 
 // The Nature of Code
 // <http://www.shiffman.net/teaching/nature>
 // Spring 2010
 // PBox2D example
 
 import pbox2d.*;
 import org.jbox2d.collision.shapes.*;
 import org.jbox2d.collision.*;
 import org.jbox2d.common.*;
 import org.jbox2d.dynamics.*;
 import java.util.*;
 import org.jbox2d.dynamics.contacts.*;
 import org.jbox2d.dynamics.joints.*;
 
 
 import processing.core.PApplet;
 import processing.core.*;
 import processing.core.PConstants.*; 
 import processing.data.*; 
 import processing.event.*; 
 import processing.opengl.*; 
 
 
 // A rectangular box
 public class Hand extends Box {
 
     // Vector of (gross,fine) body-relative hand position
     public float grossX   = 0;
     public float fineX    = 0;
     public float grossY   = 0;
     public float fineY    = 0;
     public float handForceX = 0;
     public float handForceY = 0;
 
     float GROSS_DIST;
     float FINE_DIST; 
 
     // TODO we need to set up a distance joint on the hands so they can't get further than the 'reach' dist
     // from the body.
 
     // public final void applyLinearImpulse(Vec2 impulse, Vec2 point)
 
     // Constructor
     Hand(Plane p, float x, float y, float w, float h, float density, int color) {
         super(p,x,y,w,h,density,color);
         GROSS_DIST = app.sms.dGross;
         FINE_DIST = app.sms.dFine; 
         setFixedRotation(true);
         setupMouseJoint();
     }
 
     /** Creates the spring joints that sets the hand at fixed position relative to body.
      */
     public void setupMouseJoint() {
         Vec2 pos = box2d.getBodyPixelCoord(body);
         float xpos = app.sms.xpos;
         float ypos = app.sms.ypos;
         
         // Compute what the gross and find motor position settings have to be to set the hand
         // at its current position
         grossX = (pos.x - xpos) / GROSS_DIST;
         fineX =  ((pos.x - xpos) % GROSS_DIST) / FINE_DIST;
 
         grossY = (pos.y - ypos) / GROSS_DIST;
         fineY =  ((pos.y - ypos) % GROSS_DIST) / FINE_DIST;
 
         app.println("pos.x - xpos = "+(pos.x - xpos));
         app.println("pos.y - ypos = "+(pos.y - ypos));
         app.println(this);
         bindMouseJoint(pos.x, pos.y);
 
         updatePosition(app.sms.xpos, app.sms.ypos);
 
     }
 
     /** Releases motor control of the hand (by destroying its mouse joint)
        Call setupMouseJoint to restore control of the hand to motor system.
     */
     public void relaxMouseJoint() {
         destroyMouseJoint();
     }
 
     /** Find all objects we are grasping, and welds them to anything they are touching (except us).
        returns true if any objects were welded.
      */
     public boolean weldGraspedObjects () {
         boolean anyAttached = false;
         for (Object2D obj: getWeldedObjects()) {
             anyAttached = true;
             obj.weldContacts(this);
         }
         return anyAttached;
     }
 
     /**
      * Welds all contacting objects to this hand.
      */
     public ArrayList<Object2D> grasp() {
         return weldContacts();
     }
 
     public void ungrasp() {
         removeWeldJoints();
     }
 
     /*
      * We need a routine findTransitivelyConnected(thing) which returns list of all objects
      * transitively welded together.
      *
      * We then traverse that , moving the objs to other plane, making a hash table of the mapping
      * from old to new object.
      *
      * Then we can iterate over old objects, find all joints, and what bodies they join to,
      * and reconstruct the new joint on the new objects.
      */
     void moveConnectedGroupToPlane(Plane newPLane) {
         /*
           ArrayList oldObjs = transitivelyConnected ( graspedObjectList)
         
          HashMap(<oldObj>:<newObj>) mappings = copyToPlane(oldObjs)
         
          For ((old,new) in mappings) {
             find all joints on old to old'
             for each joint create new joint (new, new')
 
          }
         */
 
     }
 
 
     /** To move to another plane, we have to destroy the object in this plane, and
      *  create a copy of it in the target plane.
      * We also need to move all the objects that we are grasping to the new plane.
      *
      * Ideally we would reconstruct their joints as well, but that will be a future improvement.
      *
 
      */
     void moveToPlane(Plane newPlane) {
         ArrayList<Object2D> gobjs = getWeldedObjects();
         app.println("welded objects = "+gobjs.toString()+" size="+gobjs.size());
         super.moveToPlane(newPlane);
         setFixedRotation(true);
         setupMouseJoint();
 
 
         for (Object2D obj: gobjs) {
             app.println("moving "+obj+" to new plane "+newPlane);
             obj.moveToPlane(newPlane);
             weld(this,obj);
         }
     }
 
     /**
        Finds any objects we're grasping, and deletes any weld joints they have, except the weld to the hand.
      */
     public boolean unWeldGraspedObjects () {
         boolean anyAttached = false;
         for (Object2D obj: getWeldedObjects()) {
             anyAttached = true;
             obj.removeWeldJoints(this);
         }
         return anyAttached;
     }
 
     /** Takes a gross and fine delta motion, enforce max reach limit
         @param dgx gross motion delta
         @param dfx fine motion delta
     */
     public void hjog(float dgx, float dfx) {
         grossX += dgx;
         fineX += dfx;
         updatePosition(app.sms.xpos, app.sms.ypos);
     }
 
     /** Takes a gross and fine delta motion, enforce max reach limit
         @param dgx gross motion delta
         @param dfx fine motion delta
      */
      public void vjog(float dgy, float dfy) {
         grossY += dgy;
         fineY += dfy;
         updatePosition(app.sms.xpos, app.sms.ypos);
     }
 
 
     /** Sum of all forces of contacting objects  on the hand*/
     public Vec2 getNetForce() {
         return getNormalForces(this);
     }
 
     public Vec2 getJointForce() {
         Vec2 fv = new Vec2();
         if (mouseJoint != null) {
             mouseJoint.getReactionForce(1.0f, fv);
         }
         return fv;
     }
 
 
     /**
        Update absolute pixel coord of the 'mousejoint' which hold the hand in position.
        @param  x body xpos
        @param  y body ypos
      */
     void updatePosition(float absx, float absy) {
         float x,y;
         float reachX = app.sms.reachX;
         float reachY = app.sms.reachY;
 
         // enforce limits as to how far hand can move relative to body
         if (grossX > reachX/2)   { grossX = reachX/2; }
         if (grossX < -reachX/2)  { grossX = -reachX/2; }
         if (fineX > reachX/2) { fineX = reachX/2; }
         if (fineX < -reachX/2) { fineX = -reachX/2; }
 
         if (grossY > reachY/2)   { grossY = reachY/2; }
         if (grossY < -reachY/2)  { grossY = -reachY/2; }
         if (fineY > reachY/2) { fineY = reachY/2; }
         if (fineY < -reachY/2) { fineY = -reachY/2; }
 
 
         // compute hand position from gross+fine positioning
         x = absx + grossX * GROSS_DIST + fineX * FINE_DIST;
         y = absy + grossY * GROSS_DIST + fineY * FINE_DIST;
         
         updateMouseJointPos(x,y);
         Vec2 pos = body.getPosition();
         // Is there a bug with apply the force here? We're applying
         // the force at the current position of the hand, not the
         // position it will finally be at when the mousejoint spring
         // forces are resolved.
         body.applyLinearImpulse(new Vec2(handForceX, handForceY), pos);
     }
     
 
     public final static int TOUCH_LEFT   =  1;
     public final static int TOUCH_RIGHT  =  2;
     public final static int TOUCH_TOP    =  4;
     public final static int TOUCH_BOTTOM =  8;
 
     /** returns a bit vector which designates which sides feel a touch contact
      *
      * For now this uses absolute angle-0 coordinates. We need to rotate the normal vectors
      * by the hand angle to get the correct sides if the hand is rotated.
      */
     int touchingSides() {
         int touching = 0;
         ContactEdge cedge = body.getContactList();
         while (cedge != null) {
             Contact c = cedge.contact;
             Manifold m = c.getManifold();
            Vec2 ln = m.localNormal;
             if (ln.x > 0) {
                 touching |= TOUCH_RIGHT;
             }
             if (ln.x < 0) {
                 touching |= TOUCH_LEFT;
             }
             if (ln.y < 0) {
                touching |= TOUCH_TOP;
             }
             if (ln.y > 0) {
                touching |= TOUCH_BOTTOM;
             }
             cedge = cedge.next;
         }
         return touching;
     }
 
     String touchString() {
         StringBuilder s = new StringBuilder();
         int k = touchingSides();
         if ((k & TOUCH_LEFT) > 0)   { s.append("LEFT ");   }
         if ((k & TOUCH_RIGHT) > 0)  { s.append("RIGHT ");  }
         if ((k & TOUCH_TOP) > 0)    { s.append("TOP ");    }
         if ((k & TOUCH_BOTTOM) > 0) { s.append("BOTTOM "); }
         return s.toString();
     }
 
     void setColor(int c) {
         this.color = c;
     }
 
     // Drawing the box
     void display() {
         super.display();
         // We look at each body and get its screen position
         Vec2 pos = box2d.getBodyPixelCoord(body);
         // Get its angle of rotation
         float a = body.getAngle();
 
         // Draw an interior border
         app.pushStyle();
         app.rectMode(PConstants.CENTER);
         app.pushMatrix();
         app.translate(pos.x, pos.y);
         app.rotate(-a);
         app.fill(app.red(color), app.green(color), app.blue(color), (float)(plane.alpha/255.0) * alpha);
         app.strokeWeight(4);
         app.stroke(app.color(94,224,88));
         app.rect(0, 0, w-6, h-6);
         app.popMatrix();
         app.popStyle();
     }
 
     public String toString() {
         // We look at each body and get its screen position
         Vec2 pos = box2d.getBodyPixelCoord(body);
         // Get its angle of rotation
         float a = body.getAngle();
         float alpha = app.map(density, 0, MAX_DENSITY, 0, 255);
         StringBuilder grasps = new StringBuilder();
         for (Object2D obj: getWeldedObjects()) {
             grasps.append(obj.toString() +", ");
         }
         return String.format("{HAND %d x,y=(%.1f, %.1f) gx,gy=(%.1f,%.1f) fx,fy=(%.1f,%.1f)  rot=%f  grasping[%s]}",index, pos.x,pos.y,grossX,grossY,fineX,fineY, getAngle(), grasps);
     }
 
 
 }
 
 
