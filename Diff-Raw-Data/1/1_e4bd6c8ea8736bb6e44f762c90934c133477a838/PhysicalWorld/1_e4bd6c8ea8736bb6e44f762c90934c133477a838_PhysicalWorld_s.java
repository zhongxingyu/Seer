 package com.github.kenji0717.a3cs;
 
 import com.bulletphysics.collision.broadphase.*;
 import com.bulletphysics.collision.dispatch.*;
 //import com.bulletphysics.collision.dispatch.CollisionWorld.RayResultCallback;
 import com.bulletphysics.collision.narrowphase.PersistentManifold;
 import com.bulletphysics.dynamics.*;
 import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
 import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
 import com.bulletphysics.linearmath.*;
 import java.util.*;
 import javax.vecmath.*;
 import jp.sourceforge.acerola3d.a3.*;
 
 //物理計算をしてくれるクラス
 class PhysicalWorld implements Runnable {
     static int MAX_PROXIES = 1024;
     DiscreteDynamicsWorld dynamicsWorld;
     ArrayList<A3CollisionObject> objects = new ArrayList<A3CollisionObject>();
     ArrayList<A3CollisionObject> newObjects = new ArrayList<A3CollisionObject>();
     ArrayList<A3CollisionObject> delObjects = new ArrayList<A3CollisionObject>();
     A3CanvasInterface mainCanvas;
     ArrayList<A3CanvasInterface> subCanvases = new ArrayList<A3CanvasInterface>();
     ArrayList<CollisionListener> collisionListeners = new ArrayList<CollisionListener>();
     ArrayList<Runnable> tasks = new ArrayList<Runnable>();
     Object waitingRoom = new Object();
     boolean pauseRequest = true;
     double time;
     boolean fastForward = false;
     final long waitTime = 33;
 
     //物理世界の初期化
     public PhysicalWorld() {
         //mainCanvas = new A3Window(500,500);
 
         CollisionConfiguration collisionConfiguration =
                 new DefaultCollisionConfiguration();
         CollisionDispatcher dispatcher =
                 new CollisionDispatcher(collisionConfiguration);
         /*
         Vector3f worldAabbMin = new Vector3f(-10000,-10000,-10000);
         Vector3f worldAabbMax = new Vector3f(10000,10000,10000);
         int maxProxies = MAX_PROXIES;
         AxisSweep3 overlappingPairCache =
                 new AxisSweep3(worldAabbMin, worldAabbMax, maxProxies);
         SequentialImpulseConstraintSolver solver =
                 new SequentialImpulseConstraintSolver();
         */
         BroadphaseInterface overlappingPairCache = new DbvtBroadphase();
         ConstraintSolver solver = new SequentialImpulseConstraintSolver();
 
         dynamicsWorld =
                 new DiscreteDynamicsWorld(dispatcher,overlappingPairCache,
                                           solver,collisionConfiguration);
         dynamicsWorld.setGravity(new Vector3f(0,-10,0));
 
         time = 0.0;
         Thread t = new Thread(this);
         t.start();
     }
 
     public void setMainCanvas(A3CanvasInterface c) {
         if (mainCanvas==null) {
             mainCanvas = c;
             mainCanvas.setUpdateInterval(waitTime);
             for (A3CollisionObject o : objects) {
                 mainCanvas.add(o.a3);
                System.out.println("GAHA:-------------");
             }
         } else {
             System.out.println("Error: has already set mainCanvas!");
         }
     }
 
     public void addSubCanvas(A3CanvasInterface c) {
         mainCanvas.addA3SubCanvas(c);
     }
 
     //新規の剛体を加える
     public void add(A3CollisionObject rb) {
         synchronized (newObjects) {
             newObjects.add(rb);
         }
     }
 
     //既存の剛体を削除
     public void del(A3CollisionObject rb) {
         synchronized (delObjects) {
             delObjects.add(rb);
         }
     }
 
     public void pause() {
         pauseRequest = true;
     }
     public void resume() {
         pauseRequest = false;
         synchronized (waitingRoom) {
             waitingRoom.notifyAll();
         }
     }
     public void clear() {
         pauseRequest = true;
         try{Thread.sleep(300);}catch(Exception e){;}
         if (mainCanvas!=null) {
             mainCanvas.delAll();
             //for (A3CollisionObject co : objects)
             //    mainCanvas.del(co.a3);
         }
         for (A3CollisionObject co : objects) {
             if (co.coType==COType.GHOST) {
                 dynamicsWorld.removeCollisionObject(co.body);
             } else {
                 dynamicsWorld.removeRigidBody((RigidBody)co.body);
             }
         }
         objects.clear();
         newObjects.clear();
         delObjects.clear();
         time = 0.0;
     }
     //物理計算を進める処理
     //座標を変更するのがちょっとやっかい
     public void run() {
         while (true) {
             synchronized (waitingRoom) {
                 if (pauseRequest==true) {
                     try {
                         waitingRoom.wait();
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
             }
             synchronized (newObjects) {
                 for (A3CollisionObject co : newObjects) {
                     //if (rb instanceof MyCar)
                     //    dynamicsWorld.removeVehicle(((MyCar)rb).motion.vehicle);
                     if (co.coType==COType.GHOST) {
                         dynamicsWorld.addCollisionObject(co.body,co.group,co.mask);
                     } else {
                         dynamicsWorld.addRigidBody((RigidBody)co.body,co.group,co.mask);
                     }
                     if (mainCanvas!=null) {
                         mainCanvas.add(co.a3);
                     }
                     objects.add(co);
                 }
                 newObjects.clear();
             }
 
             synchronized (delObjects) {
                 for (A3CollisionObject co : delObjects) {
                     //if (rb instanceof MyCar)
                     //    dynamicsWorld.addVehicle(((MyCar)rb).motion.vehicle);
                     if (co.coType==COType.GHOST) {
                         dynamicsWorld.removeCollisionObject(co.body);
                     } else {
                         dynamicsWorld.removeRigidBody((RigidBody)co.body);
                     }
                     if (mainCanvas!=null) {
                         mainCanvas.del(co.a3);
                     }
                    objects.remove(co);
                 }
                 delObjects.clear();
             }
 
             for (A3CollisionObject co : objects) {
                 if ((co.locRequest==null)&&(co.quatRequest==null))
                     continue;
                 Transform t = new Transform();
                 if (co.locRequest!=null)
                     t.origin.set(co.locRequest);
                 if (co.quatRequest!=null)
                     t.setRotation(new Quat4f(co.quatRequest));
                 co.motionState.setWorldTransform(t);
                 if (co.coType==COType.DYNAMIC)
                     co.changeCOType(COType.KINEMATIC_TEMP);
             }
 
 //System.out.println("PhysicalWorld:-----gaha-----1");
             //ここで物理計算
             dynamicsWorld.stepSimulation(1.0f/30.0f,10);time += 1.0f/30.0f;
             //dynamicsWorld.stepSimulation(1.0f/30.0f,2);
 //System.out.println("PhysicalWorld:-----gaha-----2");
 
             //車の車輪の更新。dynamicsWorld.setSimulation()の更新時間が1/60で割り切れない時とかに特に必要
             for (A3CollisionObject co : objects) {
                 if (co instanceof MyCar) {
                     ((MyCar)co).updateWheelTransform();
                 }
             }
             
             //衝突
             int numManifolds = dynamicsWorld.getDispatcher().getNumManifolds();
             for (int ii=0;ii<numManifolds;ii++) {
                 PersistentManifold contactManifold = dynamicsWorld.getDispatcher().getManifoldByIndexInternal(ii);
                 int numContacts = contactManifold.getNumContacts();
                 if (numContacts==0)
                     continue;
                 CollisionObject obA = (CollisionObject)contactManifold.getBody0();
                 CollisionObject obB = (CollisionObject)contactManifold.getBody1();
                 /*
                 for (int j=0;j<numContacts;j++) {
                     ManifoldPoint pt = contactManifold.getContactPoint(j);
                     if (pt.getDistance()<0.0f) {
                         System.out.println("-----------------");
                         System.out.println("ii:"+ii+"    j:"+j);
                         System.out.println("getLifeTime:"+pt.getLifeTime());
                         System.out.println("PositionWorldOnA:"+pt.positionWorldOnA);
                         System.out.println("PositionWorldOnB:"+pt.positionWorldOnB);
                         System.out.println("normalWorldOnB:"+pt.normalWorldOnB);
                         System.out.println("-----------------");
                     }
                 }
                 */
 
                 //ロックしすぎ？
                 synchronized (collisionListeners) {
                     for (CollisionListener cl : collisionListeners) {
                         cl.collided(((A3CollisionObject)obA.getUserPointer()),((A3CollisionObject)obB.getUserPointer()));
                     }
                 }
             }
 
 
             for (A3CollisionObject co : objects) {
                 if (co.locRequest==null)
                     continue;
                 if (co.coType==COType.KINEMATIC_TEMP){
                     co.changeCOType(COType.DYNAMIC);
                 }
                 co.locRequest=null;
             }
 
             for (A3CollisionObject co : objects) {
                 if (co.velRequest==null)
                     continue;
                 if (co.coType!=COType.GHOST) {
                     ((RigidBody)co.body).setLinearVelocity(co.velRequest);
                 }
                 co.velRequest=null;
             }
 
             /*
             //光線テストの実験
             RayResultCallback rayRC = new CollisionWorld.ClosestRayResultCallback(new Vector3f(0,0.5f,0),new Vector3f(0,0.5f,5));
             dynamicsWorld.rayTest(new Vector3f(0,0.5f,0), new Vector3f(0,0.5f,5), rayRC);
             System.out.println("gaha:"+rayRC.hasHit());
             */
 
             synchronized (tasks) {
                 for (Runnable r:tasks) {
                     r.run();
                 }
             }
             if (fastForward==false) {
                 if (mainCanvas!=null) {
                     mainCanvas.waitForUpdate(waitTime*2);
                     try{Thread.sleep(waitTime/2);}catch(Exception e){;}//微妙
                 } else {
                     try{Thread.sleep(waitTime);}catch(Exception e){;}
                 }
             }
         }
     }
     public void addCollisionListener(CollisionListener cl) {
         synchronized (collisionListeners) {
             collisionListeners.add(cl);
         }
     }
     public void removeCollisionListener(CollisionListener cl) {
         synchronized (collisionListeners) {
             collisionListeners.remove(cl);
         }
     }
     public double getTime() {
         return time;
     }
     public void addTask(Runnable r) {
         synchronized (tasks) {
             tasks.add(r);
         }
     }
     public void removeTask(Runnable r) {
         synchronized (tasks) {
             tasks.remove(r);
         }
     }
     /* やっぱりこれは危険
     public void setWaitTime(long t) {
         waitTime = t;
     }
     */
     public void fastForward(boolean b) {
         fastForward = b;
     }
 }
