 package com.github.kenji0717.a3cs;
 
 import com.bulletphysics.collision.dispatch.*;
 import com.bulletphysics.dynamics.*;
 import com.bulletphysics.linearmath.*;
 import javax.vecmath.*;
 
 import jp.sourceforge.acerola3d.a3.*;
 
 //このプログラムで剛体を表すクラス
 abstract class A3CollisionObject {
     PhysicalWorld pw;
     A3Object a3;
     MotionState motionState;//JBulletと座標をやりとりするオブジェクト
     CollisionObject body;//JBulletにおける剛体などを表すオブジェクト
     Vector3f locRequest;
     Quat4d quatRequest;
     Vector3f velRequest;
     COType coType = COType.DYNAMIC;
     short group = 1;
     short mask = 1;
 
     //Acerola3DファイルのURLと初期座標で初期化
     public A3CollisionObject(Vector3d l,Vector3d r,COType t,PhysicalWorld pw,Object...args) {
         this.pw = pw;
         this.coType = t;
         try {
             a3 = makeA3Object(args);
         } catch(Exception e) {
             a3 = new VRML("gaha");
         }
         //Transform transform = new Transform();
         //transform.setIdentity();
         //transform.origin.set((float)l.x,(float)l.y,(float)l.z);
         //transform.setRotation(new Quat4f(Util.euler2quat(r)));
         //motionState = new A3MotionState(a3,transform);
         motionState = makeMotionState(l,r);
         body = makeCollisionObject(args);
         body.setUserPointer(this);
     }
 
     public abstract A3Object makeA3Object(Object...args) throws Exception ;
     public abstract MotionState makeMotionState(Vector3d l,Vector3d r);
     public abstract CollisionObject makeCollisionObject(Object...args);
 
    public void changeCOType(COType t) {
         ;
     }
    public void changeCOType_BAK(COType t) {
         //RigidBodyとGhostObject間の変換は不可
         if ((t==COType.GHOST)&&(coType!=COType.GHOST)) {
             System.exit(0);
             throw new IllegalArgumentException();
         }
         if ((coType==COType.GHOST)&&(t!=COType.GHOST)) {
             System.exit(0);
             throw new IllegalArgumentException();
         }
 
         if (t==COType.DYNAMIC) {
             body.setCollisionFlags(body.getCollisionFlags()&(~CollisionFlags.KINEMATIC_OBJECT));
             //rb.body.setActivationState(CollisionObject.ACTIVE_TAG);
             body.forceActivationState(CollisionObject.ACTIVE_TAG);
             body.setDeactivationTime(0.0f);
             ((RigidBody)body).clearForces();
             ((RigidBody)body).setLinearVelocity(new Vector3f());
             ((RigidBody)body).setAngularVelocity(new Vector3f());
         } else if (t==COType.STATIC) {
             body.setCollisionFlags(body.getCollisionFlags()|CollisionFlags.KINEMATIC_OBJECT);
             body.setActivationState(CollisionObject.DISABLE_SIMULATION);
             ((RigidBody)body).clearForces();
             ((RigidBody)body).setLinearVelocity(new Vector3f());
             ((RigidBody)body).setAngularVelocity(new Vector3f());
         } else if (t==COType.KINEMATIC){
             body.setCollisionFlags(body.getCollisionFlags()|CollisionFlags.KINEMATIC_OBJECT);
             body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
             ((RigidBody)body).clearForces();
             ((RigidBody)body).setLinearVelocity(new Vector3f());
             ((RigidBody)body).setAngularVelocity(new Vector3f());
         } else if (t==COType.KINEMATIC_TEMP){
             body.setCollisionFlags(body.getCollisionFlags()|CollisionFlags.KINEMATIC_OBJECT);
             body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
             ((RigidBody)body).clearForces();
             ((RigidBody)body).setLinearVelocity(new Vector3f());
             ((RigidBody)body).setAngularVelocity(new Vector3f());
         }
         coType = t;
     }
     //座標変更．副作用で力や速度がリセットされる
     public void setLoc2(double x,double y,double z) {
         locRequest = new Vector3f((float)x,(float)y,(float)z);
     }
     //座標変更．副作用で力や速度がリセットされる
     public void setRot2(double x,double y,double z) {
         quatRequest = Util.euler2quat(x,y,z);
     }
     public void setVel(double x,double y,double z) {
         velRequest = new Vector3f((float)x,(float)y,(float)z);
     }
 }
