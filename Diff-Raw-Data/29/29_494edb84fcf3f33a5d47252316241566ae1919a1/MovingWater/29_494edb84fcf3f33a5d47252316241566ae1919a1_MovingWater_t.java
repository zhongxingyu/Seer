 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 
 /**
  * Write a description of class MovingWater here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class MovingWater extends Actor
 {
     private int acts;
     private int level;
 
     public MovingWater() {
         getImage().scale(1, 20);
         getImage().setTransparency(100);
         level = 10;
     }
     
     @Override
     public void act() {
         setLocation(40, 70);
         
        if (++acts == 1) {
            setLevel(level + 2);
         }
     }
     
     public int getLevel() {
         return level;
    }
    
    public void setLevel(int lvl) {
        getImage().scale(820, lvl);
        acts = 0;
        level = lvl;
    }
 }
