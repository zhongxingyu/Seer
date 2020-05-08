 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 
 /**
  * Write a description of class Log here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Log extends Obstacles
 {
    public Log() {
        getImage().scale(189,30);
    }
     /**
      * Act - do whatever the Log wants to do. This method is called whenever
      * the 'Act' or 'Run' button gets pressed in the environment.
      */
     public void act() 
     {
         killObst();
         objMove();
     }    
 
     public void killObst() {
         if (!dead) {
             Class[] list = {Snowman.class, Ramp.class};
             for(Class obst : list) {
                 removeTouching(obst);
             }
         }
     }
 }
