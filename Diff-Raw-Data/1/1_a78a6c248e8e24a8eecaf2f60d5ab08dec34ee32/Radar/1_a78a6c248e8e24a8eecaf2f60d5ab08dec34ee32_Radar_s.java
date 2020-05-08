 package com.github.hiuprocon.pve.core;
 
 import com.bulletphysics.collision.shapes.*;
 import javax.vecmath.*;
 import jp.sourceforge.acerola3d.a3.*;
 import java.net.URL;
 
 //Radarを表すクラス
 public class Radar extends PVEPart {
     double scale;
     double distance;
 
     public Radar(Type type, double mass) {
         this(type, mass, 1.0);
     }
 
     public Radar(Type type, double mass,double scale) {
         this(type, mass, scale, "x-res:///res/Radar.wrl");
     }
 
     public Radar(Type type, double mass, double scale, String a3url) {
         super(type, mass, a3url);
         distance = Double.MAX_VALUE;
         this.scale = scale;
         init();
     }
 
     @Override
     protected A3Object makeA3Object(String a3url) {
         VRML c = null;
         try {
             c = new VRML(a3url);
             c.setScale(scale);
         } catch(Exception e) {e.printStackTrace();}
         return c;
     }
 
     // 立方体の剛体を作る
     public CollisionShape makeCollisionShape() {
         return new BoxShape(new Vector3f((float)scale/20.0f,(float)scale/20.0f,(float)scale/20.0f));
     }
 
     public double getDistance() {
         return distance;
     }
 
     public void getFromAndTo(Vector3f f,Vector3f t) {
         f.set(getLoc());
         t.set(Util.trans(getQuat(),new Vector3d(0,0,100)));
     }
 }
