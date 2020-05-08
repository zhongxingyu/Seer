 package com.ionmarkgames.ys.client.objects.enemies;
 
 import com.ionmarkgames.ys.client.RestartException;
 import com.ionmarkgames.ys.client.YSPanel;
 import com.ionmarkgames.ys.client.objects.Enemy;
 import com.ionmarkgames.ys.client.objects.You;
 
 public class Lips extends Enemy {
 
     private int wander = 0;
     
     public Lips(YSPanel panel, You player) {
         super(panel, player, "/images/kiss.gif");
         
         this.speed = 5;
         this.power = 2;
         this.health = 10;
         
         this.direction = this.getRandomDirection();
     }
     
     @Override
     public void act() throws RestartException {
         if (wander < 1 && !this.moveTowardsTarget(player.getX(), player.getY())) {
             wander = 8;
         } else {
            if (!this.move(direction)) {
                 this.direction = this.getRandomDirection();
             } else {
                 wander--;
             }
         }
     }
 }
