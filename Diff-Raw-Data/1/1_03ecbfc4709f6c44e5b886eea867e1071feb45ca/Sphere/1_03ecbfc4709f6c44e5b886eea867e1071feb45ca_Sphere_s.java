 package com.github.hiuprocon.pve.core;
 
 import com.bulletphysics.collision.shapes.*;
 
 //立方体を表すクラス
 public class Sphere extends PVEPart {
 	float size;
     public Sphere(Type type,double mass,double size) {
     	this(type,mass,size,"x-res:///res/Sphere.wrl");
     }
     public Sphere(Type type,double mass,double size,String a3url) {
         super(type,mass,a3url);
         this.size = (float)size;
         init();
     }
 
     //立方体の剛体を作る
     public CollisionShape makeCollisionShape() {
         return new SphereShape(size);
     }
 }
