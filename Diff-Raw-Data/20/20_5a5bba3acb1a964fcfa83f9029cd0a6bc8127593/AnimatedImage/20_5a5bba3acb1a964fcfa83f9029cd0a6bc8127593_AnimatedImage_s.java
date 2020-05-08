 package com.skorulis.heli3.components;
 
 import forplay.core.Image;
 import forplay.core.ImageLayer;
 
 import static forplay.core.ForPlay.*;
 
 public class AnimatedImage {
 
   private Image[] frames;
   private int index;
   private float frameTime=0.2f;
   private float currentTime;  
   private ImageLayer layer;
   
   public AnimatedImage(Image[] frames) {
     this.frames = frames;
     this.index = 0; this.currentTime = 0;
     layer = graphics().createImageLayer(frames[index]);
   }
   
   public AnimatedImage(String[] imagePaths) {
     this.frames = new Image[imagePaths.length];
     for(int i=0; i < imagePaths.length; ++i) {
       this.frames[i] = assetManager().getImage(imagePaths[i]);
     }
    layer = graphics().createImageLayer(frames[index]);
   }
   
   public void update(float delta) {
     currentTime+=delta;
     if(currentTime > frameTime) {
       currentTime=0;
       nextFrame();
     }
   }
   
   public void nextFrame() {
     index++;
     if(index >= frames.length) {
       index = 0;
     }
     layer.setImage(frames[index]);
   }
   
   public ImageLayer layer() {
     return layer;
   }
   
 }
