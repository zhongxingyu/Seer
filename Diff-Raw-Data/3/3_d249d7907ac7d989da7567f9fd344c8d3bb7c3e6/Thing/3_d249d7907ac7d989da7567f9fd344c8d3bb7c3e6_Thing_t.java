 package org.enjin;
 
 import processing.core.*;
 
 abstract class Thing {
   PApplet p;
 
   public Thing(PApplet p) {
     this.p = p;
   }
 
   public final void play() {
    p.pushMatrix();
     senescence();
     update();
     render();
     p.popMatrix();
   }
 
   abstract void update();
   abstract void render();
 
   float age    = 0;
   float energy = 1.0f;
 
   final void senescence() {
     age += 0.00001;
   }
 }
