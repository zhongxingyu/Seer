 package org.group1f.izuna.GameComponents;
 
 import java.awt.Point;
 import org.group1f.izuna.GameComponents.Drawing.*;
 
 public class Enemy extends AIControllable implements SpaceShip {
     private boolean isDying;
     private Animation rollLeft;
     private Animation rollRight;
     private SoundEffect rollSound;
     private int health;
     private float oldvY = 0.0f;
     private boolean isRFinished = true;
     
     public Enemy(Point currentPos, Animation rest, SoundEffect dieSound, Path path, Animation rollLeft, Animation rollRight, SoundEffect roll)
     {
         super(currentPos, rest, dieSound, path);
         this.rollLeft = rollLeft;
         this.rollRight = rollRight;
         this.health = health;
         rollSound = roll;
         isDying = false;
     }
    
      public Enemy clone()
     {
         return new Enemy(getPosition(), getRestAnimation().clone(), dieSound, defaultPath, rollLeft.clone(),
                 rollRight.clone(), rollSound);
     }
     
     // will be fixed // for now its broken
     @Override
     public void checkStateToAnimate()
     {
          Animation newAnim = currentAnimation;
         if(!isRFinished)
            isRFinished = currentAnimation.ended; // TODO Method haline çevirmeliyiz.
         
         if( getvY() < 0) {
             if(oldvY > 0) { //
                 isRFinished = currentAnimation.refine();
             } else {
                 newAnim = rollLeft;
                 currentSound = rollSound;
             }        
         } else if(getvY() > 0) {
              if(oldvY < 0) { //
                 isRFinished = currentAnimation.refine();
             } else {
                 newAnim = rollRight;
                 currentSound = rollSound;
             } 
         } else { // vy = 0
             if(getvY() != 0) {
                 isRFinished = currentAnimation.refine();
             } else
                 newAnim = getRestAnimation();
         }
         
         if(health < 1) {
             setState();
             currentSound = dieSound;
             if(!isDying)
                 currentSound.play();
             isDying = true;
         }
         
         if(isDying && currentAnimation.getElapsedTime() >= getDieTime()) {
             setVisible(false);
         }
         
         if(isRFinished && currentAnimation != newAnim) {
             currentAnimation = newAnim;
             currentAnimation.startOver();
             currentSound.play();
         }
         oldvY = getvY();
     }
     
     @Override
     public void update(long elapsedTime) {
         checkStateToAnimate();
         super.update(elapsedTime);
     }
     
     private void setState() {
         if(health < 1) {
             setvX(0.0f);
             setvY(0.0f);
         }
     }
     
     @Override
     public int getDieTime() {
         return 1000;
     }
                 
     @Override
      public void setHealth( int newValue ) {
         if(newValue > 100 )
             health = 100;
         else
             health = newValue;
     }
     
     @Override
     public int getHealth() {
         return health;
     }    
     
     @Override
     public float getMaxSpeed()
     {
         return 1.0f;
     }
 }
