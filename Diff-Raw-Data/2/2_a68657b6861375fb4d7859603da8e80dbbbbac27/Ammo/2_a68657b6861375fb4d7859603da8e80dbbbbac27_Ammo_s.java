 package com.codefuss.entities;
 
 import com.codefuss.physics.Body;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.util.Log;
 
 /**
  *
  * @author Martin Vium <martin.vium@gmail.com>
  */
 public class Ammo extends Sprite {
 
     private int minDamage = 0;
     private int maxDamage = 0;
 
     public Ammo(Vector2f position, Body body) {
         super(position, body);
     }
 
     public void setDamageRange(int min, int max) {
         this.minDamage = min;
         this.maxDamage = max;
     }
 
     @Override
     public void collideVertical(Body collided) {
         super.collideHorizontal(collided);
         applyCollisionDamage(collided);
     }
 
     @Override
     public void collideHorizontal(Body collided) {
         super.collideHorizontal(collided);
         applyCollisionDamage(collided);
     }
     
     private void applyCollisionDamage(Body collided) {
        if(collided.getEntity() instanceof Sprite && removed == false) {
             remove();
             Sprite sprite = (Sprite)collided.getEntity();
             sprite.applyHealth(-(int) (minDamage + Math.random() * (maxDamage - minDamage)));
         }
     }
 }
