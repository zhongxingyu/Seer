 package org.group1f.izuna.GameComponents;
 
 import java.awt.Point;
 import org.group1f.izuna.GameComponents.Drawing.*;
 
 public abstract class GameObject extends Sprite {
 
     private Animation stillAnimation;
     private boolean isVisible;
     private SoundEffect dieSound;
 
     /*
      * Her Game objesi genel bir ses çıkarmıyor, yani currentSound a gerek yok,
      * dieSound da yok olduklarında çıkacak ses fakat o da null olabilir o
      * yüzden constructora koymadım
      */
     public GameObject(Point currentPos, Animation still) {
         super(currentPos);
         stillAnimation = still;
         currentAnimation = still;
         isVisible = true;
        //checkStateToAnimate(); // m:Bu napıyor hala anlamadım n:bende neden koyduğumu hatırlayamadım:D, 
        //gereksiz geldi şimdilik ama silmeyelim, böyle dursun
     }
 
     public boolean isVisible() {
         return isVisible;
     }
 
     public void setVisible(boolean isVisible) {
         this.isVisible = isVisible;
     }
 
     public Animation getStillAnimation() {
         return stillAnimation;
     }
 
     public void setStillAnimation(Animation stillAnimation) {
         this.stillAnimation = stillAnimation;
     }
 
     public void setDieSound(SoundEffect dieSound) {
         this.dieSound = dieSound;
     }
 
     public SoundEffect getDieSound() {
         return dieSound;
     }
 
     
     @Override
     public void update(long elapsedTime) {
         //playSound();
         super.update(elapsedTime);
     }
 
     public abstract void checkStateToAnimate();
 }
