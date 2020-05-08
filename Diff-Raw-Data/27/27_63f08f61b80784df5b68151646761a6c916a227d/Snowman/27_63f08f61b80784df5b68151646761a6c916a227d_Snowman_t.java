 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 
 /**
  * Write a description of class Snowman here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Snowman extends Obstacles
 {
    public Snowman() {
       getImage().scale(36,65);             
    }
     public void act() 
     {
         objMove();
         killObst();
     }
     
     public void killObst() {
         if (!dead) {
             Class[] list = {Ramp.class};
             for(Class obst : list) {
                 removeTouching(obst);
             }
         }
     }
 }
