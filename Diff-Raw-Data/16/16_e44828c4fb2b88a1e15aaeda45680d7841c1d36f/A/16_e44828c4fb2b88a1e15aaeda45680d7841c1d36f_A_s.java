 package com.example.ship.commons;
 
 import com.example.ship.RootActivity;
import com.example.ship.atlas.ResourceManager;
 import org.andengine.engine.Engine;
 import org.andengine.engine.camera.ZoomCamera;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Denis
  * Date: 5/23/13
  * Time: 10:24 PM
  * To change this template use File | Settings | File Templates.
  */
 public class A {
     static public RootActivity a;
     static public Engine           e;
     static public ResourceManager  rm;
     static public ZoomCamera       c;
     static public void init(RootActivity activity) {
         a  = activity;
         e  = activity.getEngine();
         rm = activity.getResourceManager();
         c  = activity.getCamera();
     }
 }
