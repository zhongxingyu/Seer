 package com.stewsters.graphic;
 
 import processing.core.PApplet;
 import processing.core.PImage;
 
 import java.io.File;
 
 public class Animation {
     public static final String baseAnimationDir = "/home/bloodred/code/LepEngine/asset/image/animation";
 
     PImage[] images;
     int imageCount;
     int frame;
 
     public Animation(PApplet context, String imagePrefix) {
 
        File animationFolder = new File(baseAnimationDir + File.pathSeparator + imagePrefix);
         System.out.println(animationFolder.getAbsolutePath());
         File[] files = animationFolder.listFiles();
 
         imageCount = files.length;
         images = new PImage[imageCount];
 
         for (int i = 0; i < imageCount; i++) {
             // Use nf() to number format 'i' into four digits
             String filename = files[i].getAbsolutePath();
             images[i] = context.loadImage(filename);
         }
     }
 
     public void display(PApplet context, float xpos, float ypos) {
         frame = (frame + 1) % imageCount;
         context.image(images[frame], xpos, ypos);
     }
 
     public int getWidth() {
         return images[0].width;
     }
 }
