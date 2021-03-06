 /*
  * Copyright (c) 2009-2010 jMonkeyEngine
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
 package com.jme3.terrain.geomipmap;
 
 import com.jme3.scene.control.UpdateControl;
 import com.jme3.app.Application;
 import com.jme3.bullet.PhysicsSpace;
 import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.terrain.heightmap.HeightMap;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.jme3.material.Material;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.Camera;
 import com.jme3.terrain.geomipmap.lodcalc.LodCalculatorFactory;
 import com.jme3.terrain.geomipmap.lodcalc.LodDistanceCalculatorFactory;
 import com.jme3.terrain.heightmap.HeightMapGrid;
 import java.util.concurrent.Callable;
 
 /**
  * @author Anthyon
  */
 public class TerrainGrid extends TerrainQuad {
 
     private static final Logger log = Logger.getLogger(TerrainGrid.class.getCanonicalName());
     private Vector3f currentCell;
     private int quarterSize;
     private int quadSize;
     private HeightMapGrid heightMapGrid;
     //private Vector3f[] quadOrigins;
     private Vector3f[] quadIndex;
     private Map<String, TerrainGridListener> listeners = new HashMap<String, TerrainGridListener>();
     private Material material;
     private LRUCache<Vector3f, TerrainQuad> cache = new LRUCache<Vector3f, TerrainQuad>(16);
     private RigidBodyControl[] quadControls;
     private PhysicsSpace space;
 
     private class UpdateQuadCache implements Runnable {
 
         private final Vector3f location;
 
         public UpdateQuadCache(Vector3f location) {
             this.location = location;
         }
 
         public void run() {
             
             for (int i = 0; i < 4; i++) {
                 for (int j = 0; j < 4; j++) {
                     int quadIdx = i * 4 + j;
                     final Vector3f temp = location.add(quadIndex[quadIdx]);
                     TerrainQuad q = cache.get(temp);
                     if (q == null) {
                         // create the new Quad since it doesn't exist
                         HeightMap heightMapAt = heightMapGrid.getHeightMapAt(temp);
                         q = new TerrainQuad(getName() + "Quad" + temp, patchSize, quadSize, totalSize, heightMapAt == null ? null : heightMapAt.getHeightMap(), lodCalculatorFactory);
                         Material mat = material.clone();
                         for (TerrainGridListener l : listeners.values()) {
                             mat = l.tileLoaded(mat, temp);
                         }
                         q.setMaterial(mat);
                         log.log(Level.FINE, "Loaded TerrainQuad {0}", q.getName());
                     }
                     cache.put(temp, q);
                     
                     if (isCenter(quadIdx)) {
                         // if it should be attached as a child right now, attach it
                         final int quadrant = getQuadrant(quadIdx);
                         final TerrainQuad newQuad = q;
                         // back on the OpenGL thread:
                         getControl(UpdateControl.class).enqueue(new Callable() {
                             public Object call() throws Exception {
                                 attachQuadAt(newQuad, quadrant, temp);
                                 return null;
                             }
                         });
                     }
                 }
             }
 
         }
     }
     
     private boolean isCenter(int quadIndex) {
         return quadIndex == 9 || quadIndex == 5 || quadIndex == 10 || quadIndex == 6;
     }
     
     private int getQuadrant(int quadIndex) {
         if (quadIndex == 9)
             return 1;
         else if (quadIndex == 5)
             return 2;
         else if (quadIndex == 10)
             return 3;
         else if (quadIndex == 6)
             return 4;
         return 0; // error
     }
 
     public TerrainGrid(String name, int patchSize, int maxVisibleSize, Vector3f scale, HeightMapGrid heightMapGrid,
             Vector2f offset, float offsetAmount, LodCalculatorFactory lodCalculatorFactory) {
         this.name = name;
         this.patchSize = patchSize;
         this.size = maxVisibleSize;
         this.quarterSize = maxVisibleSize >> 2;
         this.quadSize = (maxVisibleSize + 1) >> 1;
         this.stepScale = scale;
         this.heightMapGrid = heightMapGrid;
         heightMapGrid.setSize(this.quadSize);
         this.totalSize = maxVisibleSize;
         this.offset = offset;
         this.offsetAmount = offsetAmount;
         this.lodCalculatorFactory = lodCalculatorFactory;
         if (lodCalculatorFactory == null) {
             lodCalculatorFactory = new LodDistanceCalculatorFactory();
         }
 //        this.quadOrigins = new Vector3f[]{
 //            new Vector3f(-this.quarterSize, 0, -this.quarterSize).mult(this.stepScale),
 //            new Vector3f(-this.quarterSize, 0, this.quarterSize).mult(this.stepScale),
 //            new Vector3f(this.quarterSize, 0, -this.quarterSize).mult(this.stepScale),
 //            new Vector3f(this.quarterSize, 0, this.quarterSize).mult(this.stepScale)};
         this.quadIndex = new Vector3f[]{
             new Vector3f(-1, 0, 2), new Vector3f(0, 0, 2), new Vector3f(1, 0, 2), new Vector3f(2, 0, 2),
             new Vector3f(-1, 0, 1), new Vector3f(0, 0, 1), new Vector3f(1, 0, 1), new Vector3f(2, 0, 1),
             new Vector3f(-1, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), new Vector3f(2, 0, 0),
             new Vector3f(-1, 0, -1), new Vector3f(0, 0, -1), new Vector3f(1, 0, -1), new Vector3f(-2, 0, -1)};
         
         addControl(new UpdateControl());
     }
 
     public TerrainGrid(String name, int patchSize, int maxVisibleSize, Vector3f scale, HeightMapGrid heightMapGrid,
             LodCalculatorFactory lodCalculatorFactory) {
         this(name, patchSize, maxVisibleSize, scale, heightMapGrid, new Vector2f(), 0, lodCalculatorFactory);
     }
 
     public TerrainGrid(String name, int patchSize, int maxVisibleSize, HeightMapGrid heightMapGrid, LodCalculatorFactory lodCalculatorFactory) {
         this(name, patchSize, maxVisibleSize, Vector3f.UNIT_XYZ, heightMapGrid, lodCalculatorFactory);
     }
 
     public TerrainGrid(String name, int patchSize, int maxVisibleSize, HeightMapGrid heightMapGrid) {
         this(name, patchSize, maxVisibleSize, heightMapGrid, null);
     }
 
     public TerrainGrid() {
     }
 
     public void initialize(Vector3f location) {
         if (this.material == null) {
             throw new RuntimeException("Material must be set prior to call of initialize");
         }
         Vector3f camCell = this.getCell(location);
         this.updateChildrens(camCell);
         for (TerrainGridListener l : this.listeners.values()) {
             l.gridMoved(camCell);
         }
     }
 
     @Override
     public void update(List<Vector3f> locations) {
         // for now, only the first camera is handled.
         // to accept more, there are two ways:
         // 1: every camera has an associated grid, then the location is not enough to identify which camera location has changed
         // 2: grids are associated with locations, and no incremental update is done, we load new grids for new locations, and unload those that are not needed anymore
         Vector3f cam = locations.get(0);
         Vector3f camCell = this.getCell(cam);
         if (camCell.x != this.currentCell.x || camCell.z != currentCell.z) {
             this.updateChildrens(camCell);
             for (TerrainGridListener l : this.listeners.values()) {
                 l.gridMoved(camCell);
             }
         }
 
         super.update(locations);
     }
 
     public Vector3f getCell(Vector3f location) {
        final Vector3f v = location.clone().divideLocal(this.getLocalScale().mult(this.quadSize)).add(0.5f, 0, 0.5f);
         return new Vector3f(FastMath.floor(v.x), 0, FastMath.floor(v.z));
     }
 
     protected void removeQuad(int idx) {
         if (this.getQuad(idx) != null) {
             if (quadControls != null) {
                 this.getQuad(idx).removeControl(RigidBodyControl.class);
             }
             this.detachChild(this.getQuad(idx));
         }
     }
 
     /**
      * Runs on the rendering thread
      */
     protected void attachQuadAt(TerrainQuad q, int quadrant, Vector3f cam) {
         this.removeQuad(quadrant);
         //q.setMaterial(this.material);
         //q.setLocalTranslation(quadOrigins[quadrant - 1]);
         q.setQuadrant((short) quadrant);
         this.attachChild(q);
 
         Vector3f loc = cam.mult(this.quadSize-1).subtract(quarterSize, 0, quarterSize);// quadrant location handled TerrainQuad automatically now
         q.setLocalTranslation(loc );
 
         if (quadControls != null) {
             quadControls[quadrant - 1].setEnabled(false);
             quadControls[quadrant - 1].setCollisionShape(new HeightfieldCollisionShape(q.getHeightMap(), getLocalScale()));
             q.addControl(quadControls[quadrant - 1]);
             space.addAll(q);
             quadControls[quadrant - 1].setEnabled(true);
             //quadControls[quadrant - 1].setPhysicsLocation(cam.add(this.quadOrigins[quadrant - 1]));
         } else {
         }
         
         updateModelBound();
     }
 
     private void updateChildrens(Vector3f cam) {
         RigidBodyControl control = getControl(RigidBodyControl.class);
         if (control != null) {
             this.space = control.getPhysicsSpace();
             space.remove(this);
             this.removeControl(control);
             this.quadControls = new RigidBodyControl[4];
 
             for (int i = 0; i < 4; i++) {
                 int collisionGroupsCollideWith = control.getCollideWithGroups();
                 int collisionGroups = control.getCollisionGroup();
                 quadControls[i] = new RigidBodyControl(new HeightfieldCollisionShape(new float[quadSize * quadSize], getLocalScale()), 0);
                 quadControls[i].setCollideWithGroups(collisionGroupsCollideWith);
                 quadControls[i].setCollisionGroup(collisionGroups);
                 //quadControls[i].setPhysicsSpace(space);
                 //this.addControl(quadControls[i]);
                 //space.add(quadControls[i]);
             }
         }
 
         //TerrainQuad q1 = cache.get(cam.add(quadIndex[9]));
         //TerrainQuad q2 = cache.get(cam.add(quadIndex[5]));
         //TerrainQuad q3 = cache.get(cam.add(quadIndex[10]));
         //TerrainQuad q4 = cache.get(cam.add(quadIndex[6]));
 
         // ---------------------------------------------------
         // what does this block do?
         // ---------------------------------------------------
         int dx = 0;
         int dy = 0;
         if (currentCell != null) {
             dx = (int) (cam.x - currentCell.x);
             dy = (int) (cam.z - currentCell.z);
         }
 
         int kxm = 0;
         int kxM = 4;
         int kym = 0;
         int kyM = 4;
         if (dx == -1) {
             kxM = 3;
         } else if (dx == 1) {
             kxm = 1;
         }
 
         if (dy == -1) {
             kyM = 3;
         } else if (dy == 1) {
             kym = 1;
         }
 
         for (int i = kym; i < kyM; i++) {
             for (int j = kxm; j < kxM; j++) {
                 cache.get(cam.add(quadIndex[i * 4 + j]));
             }
         }
         // ---------------------------------------------------
         // ---------------------------------------------------
         
         if (executor == null)
             executor = createExecutorService();
 
         executor.submit(new UpdateQuadCache(cam));
         
 /*        if (q1 == null || q2 == null || q3 == null || q4 == null) {
             try {
                 executor.submit(new UpdateQuadCache(cam, true)).get(); // BLOCKING
                 q1 = cache.get(cam.add(quadIndex[9]));
                 q2 = cache.get(cam.add(quadIndex[5]));
                 q3 = cache.get(cam.add(quadIndex[10]));
                 q4 = cache.get(cam.add(quadIndex[6]));
             } catch (InterruptedException ex) {
                 Logger.getLogger(TerrainGrid.class.getName()).log(Level.SEVERE, null, ex);
                 return;
             } catch (ExecutionException ex) {
                 Logger.getLogger(TerrainGrid.class.getName()).log(Level.SEVERE, null, ex);
                 return;
             }
         }
 
         executor.execute(new UpdateQuadCache(cam));
 
 
         this.removeQuad(1);
         this.removeQuad(2);
         this.removeQuad(3);
         this.removeQuad(4);
 
         attachQuadAt(q1, 1, cam); // quadIndex[9]
         attachQuadAt(q2, 2, cam); // quadIndex[5]
         attachQuadAt(q3, 3, cam); // quadIndex[10]
         attachQuadAt(q4, 4, cam); // quadIndex[6]
 */
         this.currentCell = cam;
 //        this.updateModelBound();
     }
     
     public void addListener(String id, TerrainGridListener listener) {
         this.listeners.put(id, listener);
     }
 
     public Vector3f getCurrentCell() {
         return this.currentCell;
     }
 
     public void removeListener(String id) {
         this.listeners.remove(id);
     }
 
     @Override
     public void setMaterial(Material mat) {
         this.material = mat;
         super.setMaterial(mat);
     }
 
     public void setQuadSize(int quadSize) {
         this.quadSize = quadSize;
     }
     
     @Override
     public void adjustHeight(List<Vector2f> xz, List<Float> height) {
         Vector3f currentGridLocation = getCurrentCell().mult( getLocalScale() ).multLocal( quadSize-1 );
         for ( Vector2f vect : xz )
         {
             vect.x -= currentGridLocation.x;
             vect.y -= currentGridLocation.z;
         }
         super.adjustHeight( xz, height );
     }
 }
