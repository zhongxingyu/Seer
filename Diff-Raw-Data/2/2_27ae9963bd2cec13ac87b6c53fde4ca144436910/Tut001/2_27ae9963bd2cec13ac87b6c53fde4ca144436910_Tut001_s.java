 package com.katspow.caatjagwtdemos.client.tutorials.simple;
 
 import com.katspow.caatja.core.Caatja;
 import com.katspow.caatja.core.canvas.CaatjaCanvas;
 import com.katspow.caatja.core.canvas.CaatjaFillStrokeStyle;
 import com.katspow.caatja.foundation.Director;
 import com.katspow.caatja.foundation.Scene;
 import com.katspow.caatja.foundation.ui.ShapeActor;
 
 /**
  * A small red circle outlined in black.
  * 
  * @author ahingsaka
  *
  */
 public class Tut001 {
 
     public void init() throws Exception {
 
         CaatjaCanvas canvas = Caatja.createCanvas();
         Caatja.addCanvas(canvas);
         
         Director director = new Director().initialize(100, 100, canvas);
         Scene scene = director.createScene();
 
        ShapeActor circle = (ShapeActor) new ShapeActor()
                 .setLocation(20, 20)
                 .setSize(60, 60)
                 .setFillStrokeStyle(new CaatjaFillStrokeStyle("#ff0000"));
         
         circle.setStrokeStyle(new CaatjaFillStrokeStyle("#000000"));
 
         scene.addChild(circle);
         director.addScene(scene);
 
         Caatja.loop(1);
     }
 
 }
