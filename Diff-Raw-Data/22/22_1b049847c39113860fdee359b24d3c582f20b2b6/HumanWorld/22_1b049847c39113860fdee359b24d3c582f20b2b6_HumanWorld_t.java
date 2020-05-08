 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 
 /**
  * Write a description of class HumanWorld here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class HumanWorld extends World
 {
    protected int bCounter,gCounter;
     
     /**
      * Constructor for objects of class HumanWorld.
      * 
      */
     public HumanWorld(int width, int height, int pixels)
     {    
         // Create a new world with 600x400 cells with a cell size of 1x1 pixels.
         super(width, height, pixels); 
         bCounter = gCounter = 0;
     }
     
     public void addHuman(Human person, int x, int y){
         addObject(person, x, y);
         person.createHealthBar();
     }
     
     public void decBCounter() {
         bCounter--;
     }
     
     public void decGCounter() {
         gCounter--;
     }
     
     /**
      * Try to add in the bullet here. Call from 
      */
     public void addBullet(Actor source, Actor target) {
         addObject(new Bullet(source, target), source.getX(), source.getY()); 
     }
     
     public void addBullet(Actor source, int targetx, int targety){
         addObject(new Bullet(source, targetx, targety), source.getX(), source.getY());
     }
 }
