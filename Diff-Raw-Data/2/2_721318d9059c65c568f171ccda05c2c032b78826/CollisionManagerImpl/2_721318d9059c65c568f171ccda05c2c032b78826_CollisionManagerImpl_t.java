 /*
  * Copyright (c) 2008, Sun Microsystems, Inc.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in
  *       the documentation and/or other materials provided with the
  *       distribution.
  *     * Neither the name of Sun Microsystems, Inc. nor the names of its
  *       contributors may be used to endorse or promote products derived
  *       from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.sun.darkstar.example.snowman.common.util;
 
 import java.util.ArrayList;
 
 import com.jme.intersection.PickData;
 import com.jme.intersection.PickResults;
 import com.jme.intersection.TrianglePickResults;
 import com.jme.math.Ray;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import com.jme.scene.Spatial;
 import com.jme.scene.TriMesh;
 import com.sun.darkstar.example.snowman.common.util.enumn.EStats;
 
 /**
  * <code>CollisionManager</code> is a <code>Manager</code> that is responsible
  * for processing all collision detection tasks.
  * 
  * @author Yi Wang (Neakor)
  * @author Owen Kellett
  * @version Creation date: 07-02-2008 24:26 EST
  * @version Modified date: 07-16-2008 11:40 EST
  */
 public class CollisionManagerImpl implements CollisionManager
 {
     /**
      * The <code>CollisionManager</code> instance.
      */
     private static CollisionManager instance;
 
     /**
      * Constructor of <code>CollisionManager</code>.
      */
     protected CollisionManagerImpl() {
     }
 
     /**
      * Retrieve the <code>CollisionManager</code> instance.
      * @return The <code>CollisionManager</code> instance.
      */
     public static CollisionManager getInstance() {
         if (CollisionManagerImpl.instance == null) {
             CollisionManagerImpl.instance = new CollisionManagerImpl();
         }
         return CollisionManagerImpl.instance;
     }
     
     /** {@inheritDoc} */
     public Spatial getIntersectObject(Ray ray, Node root, Class<? extends Spatial> reference, boolean iterate) {
         PickResults results = new TrianglePickResults();
         results.setCheckDistance(true);
         root.findPick(ray, results);
         if (iterate) {
             for (int i = 0; i < results.getNumber(); i++) {
                 Spatial collision = this.validateClass(root, results.getPickData(i).getTargetMesh(), reference);
                 if (collision != null) return collision;
             }
         } else if (results.getNumber() > 0) {
             return this.validateClass(root, results.getPickData(0).getTargetMesh(), reference);
         }
         return null;
     }
 
     /**
      * Retrieve the spatial with given reference class.
      * @param root The root <code>Node</code> to stop check at.
      * @param spatial The <code>Spatial</code> to check.
      * @param reference The <code>Class</code> reference of the expected object. 
      * @return The <code>Spatial</code> that is of the given reference <code>Class</code>.
      */
     private Spatial validateClass(Node root, Spatial spatial, Class<? extends Spatial> reference) {
         if (spatial.getClass().equals(reference)) {
             return spatial;
         } else {
             while (spatial.getParent() != null) {
                 spatial = spatial.getParent();
                 if (spatial == root) {
                     return null; // TODO Should throw an exception here saying reached parent.
                 } else if (spatial.getClass().equals(reference)) {
                     return spatial;
                 }
             }
         // TODO Should throw an exception here saying that cannot find the referencing class.
         }
         return null;
     }
 
     /** {@inheritDoc} */
     public Vector3f getIntersection(Ray ray, Spatial parent, Vector3f store, boolean local) {
         if (store == null) {
             store = new Vector3f();
         }
 
         TrianglePickResults results = new TrianglePickResults();
         results.setCheckDistance(true);
         Vector3f[] vertices = new Vector3f[3];
         parent.findPick(ray, results);
         boolean hit = false;
         if (results.getNumber() > 0) {
             PickData data = results.getPickData(0);
             ArrayList<Integer> triangles = data.getTargetTris();
             if (!triangles.isEmpty()) {
                 TriMesh mesh = (TriMesh) data.getTargetMesh();
                 mesh.getTriangle(triangles.get(0).intValue(), vertices);
                 for (int j = 0; j < vertices.length; j++) {
                 	mesh.localToWorld(vertices[j], vertices[j]);
                 }
                 hit = ray.intersectWhere(vertices[0], vertices[1], vertices[2], store);
                 if (hit && local) {
                     parent.worldToLocal(store, store);
                     return store;
                 } else if (hit && !local) {
                     return store;
                 }
             }
         }
 
         return null;
     }
 
     public Vector3f getDestination(float x1, float z1, float x2, float z2, Spatial spatial) {
         //generate the start and destination points
         Vector3f start = new Vector3f(x1, EStats.SnowmanHeight.getValue()/2.0f, z1);
         Vector3f destination = new Vector3f(x2, EStats.SnowmanHeight.getValue()/2.0f, z2);
         
         //convert points to world coordinate system
         spatial.localToWorld(start, start);
         spatial.localToWorld(destination, destination);
         
         //generate Ray for intersection detection
         Vector3f direction = destination.subtract(start).normalizeLocal();
         Ray moveRay = new Ray(start, direction);
         
         //calculate the intersection between the move ray and the spatial
         Vector3f hitPoint = getIntersection(moveRay, spatial, null, false);
         
         //if there are no obstacles, return the destination directly
         if(hitPoint == null) {
             spatial.worldToLocal(destination, destination);
             return destination;
         }
         else {
             float originalDistance = destination.distance(start);
             float newDistance = hitPoint.distance(start);
             
             if(originalDistance > newDistance - EStats.BackoffDistance.getValue()) {
                 //we are either trying to go through a hit point
                //or got too close to one
                 direction.multLocal(EStats.BackoffDistance.getValue());
                 Vector3f newDestination = hitPoint.subtractLocal(direction);
                 spatial.worldToLocal(newDestination, newDestination);
                 return newDestination;
             } else {
                 //destination is not close to any hit points so
                 //we can just return it directly
                 spatial.worldToLocal(destination, destination);
                 return destination;
             }
         }
     }
 
     public boolean validate(float x1, float z1, float x2, float z2, Spatial spatial) {
         //generate the start and destination points
         Vector3f start = new Vector3f(x1, EStats.SnowballHeight.getValue(), z1);
         Vector3f destination = new Vector3f(x2, EStats.SnowballHeight.getValue(), z2);
         
         //convert points to world coordinate system
         spatial.localToWorld(start, start);
         spatial.localToWorld(destination, destination);
         
         //generate Ray for intersection detection
         Vector3f direction = destination.subtract(start).normalizeLocal();
         Ray moveRay = new Ray(start, direction);
         
         //calculate the intersection between the move ray and the spatial
         Vector3f hitPoint = getIntersection(moveRay, spatial, null, false);
         
         //if there is no hit point, it is a valid throw
         if(hitPoint == null) return true;
         
         //if there is a hit point, compare the distances.
         float distance1 = start.distanceSquared(destination);
         float distance2 = start.distanceSquared(hitPoint);
         
         if(distance1 <= distance2) return true;
         else return false;
     }
 }
