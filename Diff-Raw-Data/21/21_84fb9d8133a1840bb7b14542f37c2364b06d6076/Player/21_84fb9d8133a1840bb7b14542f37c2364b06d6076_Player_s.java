 package org.group1f.izuna.GameComponents;
 
 import java.awt.Point;
 import org.group1f.izuna.GameComponents.Drawing.*;
 
 public class Player extends GameObject implements SpaceShip {
 
     private boolean isDying;
     private Animation rollLeft;
     private Animation rollRight;
     private SoundEffect rollSound;
     private int health;
     //proton cannon and plasma cutter is infinite
     private int particleSeparatorCount;
     private int darkMatterCount;
     private int superDesperationMoveCount;
     private float oldvY = 0.0f;
     private boolean isRFinished = true;
 
     public Player(Point currentPos, Animation still, SoundEffect dieSound, Animation rollLeft, Animation rollRight, SoundEffect roll) {
         super(currentPos, still);
         this.rollLeft = rollLeft;
         this.rollRight = rollRight;
         rollRight.setAnimType(Animation.AnimationType.SMOOTH);
         rollLeft.setAnimType(Animation.AnimationType.SMOOTH);
         rollSound = roll;
         health = 100;
         isDying = false;
         particleSeparatorCount = 0;
         darkMatterCount = 0;
         superDesperationMoveCount = 0;
     }
 
    // will be fixed // for now its broken
     @Override
     public void checkStateToAnimate() {
         Animation newAnim = currentAnimation;
         if (!isRFinished) {
             isRFinished = currentAnimation.finished();
         }
 
         if (getvY() < 0) {
             if (oldvY > 0) { //
                 isRFinished = currentAnimation.refine();
             } else {
                 newAnim = rollLeft;
                 rollSound.play();
             }
         } else if (getvY() > 0) {
             if (oldvY < 0) { //
                 isRFinished = currentAnimation.refine();
             } else {
                 newAnim = rollRight;
                 rollSound.play();
             }
         } else { // vy = 0
             if (getvY() != 0) {
                 isRFinished = currentAnimation.refine();
             } else {
                 newAnim = getStillAnimation();
             }
         }
 
         if (health < 1) {
             setState();
             if (!isDying) {
                 getDieSound().play();
             }
             isDying = true;
         }
 
         if (isDying && currentAnimation.getElapsedTime() >= getDieTime()) {
             setVisible(false);
         }
 
         if (isRFinished && currentAnimation != newAnim) {
             currentAnimation = newAnim;
             currentAnimation.startOver();
         }
         oldvY = getvY();
     }
 
     @Override
     public void update(long elapsedTime) {
         checkStateToAnimate();
         super.update(elapsedTime);
     }
 
     private void setState() {
         if (health < 1) {
             setvX(0.0f);
             setvY(0.0f);
         }
     }
 
     public int getPScount() {
         return particleSeparatorCount;
     }
 
     public int getDMcount() {
         return darkMatterCount;
     }
 
     public int getSPcount() {
         return superDesperationMoveCount;
     }
 
     public void decrPScount() {
         particleSeparatorCount--;
     }
 
     public void decrSPcount() {
         superDesperationMoveCount--;
     }
 
     public void decrDMcount() {
         darkMatterCount--;
     }
 
     public void incrPScount() {
         particleSeparatorCount++;
     }
 
     public void incrSPcount() {
         superDesperationMoveCount++;
     }
 
     public void incrDMcount() {
         darkMatterCount++;
     }
 
     @Override
     public int getDieTime() {
         return 1000;
     }
 
     @Override
     public void setHealth(int newValue) {
         if (newValue > 100) {
             health = 100;
         } else {
             health = newValue;
         }
     }
 
     @Override
     public int getHealth() {
         return health;
     }
 
     @Override
     public float getMaxSpeed() {
         return 0.8f;
     }
 }
