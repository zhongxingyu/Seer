 package org.group1f.izuna.GameComponents.Drawing;
 
 import java.awt.*;
 import java.util.ArrayList;
 
 import org.group1f.izuna.GameCore;
 
 public class Animation {
     
     public enum Type {
         REPEAT, SMOOTH
     }
 
     public static final long FRAME_DURATION = 1000 / 24;
     //public static final boolean REPEAT = false;
   //  public static final boolean SMOOTH = true;
     
     Type animType = Type.REPEAT;
    // private boolean animType = false;
     public boolean ended = false;
     private boolean isFinished = false;
     protected ArrayList<AnimationFrame> frames;
     protected int currentFrameIndex;
     protected long totalDuration;
     protected long elapsedTime;
     protected long oldElapsedTime;
     
     
     public Animation() {
         this(new ArrayList<AnimationFrame>());
     }
 
     private Animation(ArrayList<AnimationFrame> frames) {
         this.frames = frames;
         startOver();
     }
 
     public synchronized void startOver() {
         elapsedTime = 0;
         currentFrameIndex = 0;
     }
 
     public Animation clone() {
         return new Animation(frames);
     }
     
     
     public synchronized void update(long passedTime) {
         if (frames.size() > 1) {
             elapsedTime += passedTime;
         }
         if(animType == Type.REPEAT) // repeat
         {
             if (elapsedTime >= totalDuration) {
                 elapsedTime = elapsedTime % totalDuration;
                 currentFrameIndex = 0;
             }
         } else { //smooth
             if ( !ended && elapsedTime >= totalDuration) {
                 currentFrameIndex = frames.size()-1;
             } else  if (ended){
                 elapsedTime = elapsedTime % totalDuration;
             }
         }
        
         while (!timeCorrespondImage(currentFrameIndex)) {
             currentFrameIndex++;
         } 
         if(animType == Type.SMOOTH) { //smooth
             while ( ended && !timeCorrespondImage2(currentFrameIndex))
             currentFrameIndex--;
         
         if(ended && currentFrameIndex == 0)
             isFinished = true;
         }
     }
     
     
     public synchronized boolean refine() {
         if(!ended){
             ended = true;
         }
         if(isFinished) {
             ended = false;
             isFinished = false;
             return true;
         }
         else return false;
     }
     
     public synchronized boolean finished() {
         if(isFinished) {
             ended = false;
             isFinished = false;
             return true;
         }
         else return false;
     }
     
     //eğer bi saçmalık olursa ilk buraya bak
     protected boolean timeCorrespondImage2(int index) {
         long timeChecked = 0;
 
         for (int i = 0; i <= index; i++) {
             timeChecked += FRAME_DURATION;
         }
         
         //özellikle ilk bu ife bak
         if ((totalDuration - timeChecked) < elapsedTime && !(timeChecked == FRAME_DURATION)) {
             return false;
         } else {
             return true;
         }
     }
     
     protected boolean timeCorrespondImage(int index) {
         long timeChecked = 0;
 
         for (int i = 0; i <= index; i++) {
             timeChecked += FRAME_DURATION;
         }
 
         if (timeChecked < elapsedTime && !(elapsedTime >= totalDuration)) {
             return false;
         } else {
             return true;
         }
     }
     
     public void setAnimType (Type value) {
        animType = value;
     }
     
     public Type getAnimType () {
         return animType;
     }
 
     public synchronized void addFrame(Image img, Image img3D) {
         totalDuration += FRAME_DURATION;
         frames.add(new AnimationFrame(img, img3D));
     }
     
     public synchronized Image getImage() {
         if (frames.isEmpty()) {
             return null;
         } else {
             return getFrame(currentFrameIndex).getFrame();
         }
     }
     
     public long getElapsedTime() {
         return elapsedTime;
     }
 
     private AnimationFrame getFrame(int i) {
         return frames.get(i);
     }
     
       /* public synchronized void oldupdate(long passedTime) {
         if (frames.size() > 1) {
             elapsedTime += passedTime;
         }
 
         if (elapsedTime >= totalDuration) {
             elapsedTime = elapsedTime % totalDuration;
             currentFrameIndex = 0;
         }
 
         while (!timeCorrespondImage(currentFrameIndex)) {
             currentFrameIndex++;
         }
     }*/
 /*
     public synchronized void oldupdate2(long passedTime) {
         if (!ended && frames.size() > 1) {
             elapsedTime += passedTime;
         }
             
 
         if ( !ended && elapsedTime >= totalDuration) {
             currentFrameIndex = frames.size()-1;
         }   else  if (ended){
             elapsedTime = elapsedTime % totalDuration;
         }
         
 
         while (!timeCorrespondImage(currentFrameIndex)) {
             currentFrameIndex++;
         }
         while ( ended && !timeCorrespondImage2(currentFrameIndex))
             currentFrameIndex--;
         
         if(ended && currentFrameIndex == 0)
             isFinished = true;
     }*/
 
     private class AnimationFrame {
 
         private Image frame;
         private Image frame3D;
 
         public AnimationFrame(Image img, Image img3D) {
             frame = img;
             setFrame3D(img3D);
         }
 
         public Image getFrame() {
             boolean is3D = GameCore.preferences().getBoolean("3D", false);
             if (is3D) {
                 return frame3D;
             } else {
                 return frame;
             }
         }
 
         public Image getFrame3D() {
             return frame3D;
         }
 
         public void setFrame3D(Image frame3D) {
             this.frame3D = frame3D;
         }
     }
 }
