 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 
 /**
  * Write a description of class Tree here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Tree extends Obstacles
 {
     public Tree() {
        getImage().scale(59,72);
     }
     
     public void act() 
     {
         killObst();
         objMove();        
     }
        
     public void killObst() {
         if (!dead) {
             Class[] list = {Log.class, Snowman.class, Ramp.class, Coins.class};
             for(Class obst : list) {
                 removeTouching(obst);
             }
         }
     }
 }
