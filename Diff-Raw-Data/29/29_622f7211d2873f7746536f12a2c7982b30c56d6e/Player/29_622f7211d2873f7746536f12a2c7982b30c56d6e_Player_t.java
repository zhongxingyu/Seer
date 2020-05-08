 package com.psywerx.dh;
 
 public class Player extends PersonSprite {
 
     boolean dead;
     float[] startPosition;
    float sPosition = 0.335f;
     
 
     public Player(){
         
        position[1] += 0.55; 
         position[0] = 0f;
         startPosition = position;        
     }
     
     void resetPlayer(){
         position = startPosition;
         move(0,0,0); // hack that resets all the other boxes;
         dead = false;
     }
     void nextSkin(){
         s.texture.startSprite[1] += 3;
     }
     
     @Override
     public void tick(float theta){
         
         super.tick(theta);
         
         if(Game.moving)
             if(Math.abs(direction[0]-position[0]) < 0.0001){
                 speed = new float[]{0f+0.5f*speed[0], 0f};
             }
             else{
                 speed = new float[]{theta*(direction[0]*1.8f-position[0]), 0f};
             }
         else{
             speed = new float[]{0f+0.5f*speed[0], 0f};
         }
         if(direction[0] == 0 && colide == 0) this.speed[0] = 0;
         if(direction[1] == 0 && colide == 0) this.speed[1] = 0;
         if(this.direction[0] != 0 && this.direction[1] != 0 && this.colide == 0){
             this.speed[0] = 0;
             this.speed[1] = 0;
         }
         if(this.position[0] < -1.0 && speed[0] < 0){
             this.speed[0] = 0;
         }
         if(this.position[0] > 1.0 && speed[0] > 0){
             this.speed[0] = 0;
         }
 
         if(!dead){
             // You can't move if you're dead...
             this.move(speed[0]/900, 0, 0);
         }
     }
 }
