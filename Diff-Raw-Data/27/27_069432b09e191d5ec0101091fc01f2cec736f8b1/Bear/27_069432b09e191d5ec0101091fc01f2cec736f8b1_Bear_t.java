 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 
 /**
  * Write a description of class Bear here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Bear extends Obstacles
 {
     public int dir = 1;
     public int imgDir = 1;
     private GreenfootImage img1 = new GreenfootImage("images/PolarBear.png");
     private GreenfootImage img2 = new GreenfootImage("images/PolarBear2.png");
     private GreenfootImage img3 = new GreenfootImage("images/PolarBear3.png");
     private GreenfootImage img4 = new GreenfootImage("images/PolarBear4.png");
     private int animTimer = 0;
     int dx;
     public void act() 
     {
         objMove();
         dx = bearMove();
         bearAnim();     
         killObst();
     }    
 
     public void bearAnim() {
         if (!dead) {
             if (dx > 0) {
                 if (animTimer%20 < 10) {
                     setImage(img1);
                 } else {
                     setImage(img2);
                 }
             } else if(dx < 0) {
                 if (animTimer%20 < 10) {
                     setImage(img3);
                 } else {
                     setImage(img4);
                 }
             }
         }
     }
 
     public int bearMove() {
         dx = 0;
         if (!dead) {
             Actor b;
             if (getWorld() instanceof SnowWorld) {
                 SnowWorld w = (SnowWorld) getWorld();
                 b = (Actor) w.getBoarder();
            } else if (getWorld() instanceof Intro) {
                 Intro w = (Intro) getWorld();
                 b = (Actor) w.getBot();
            } else if (getWorld() instanceof Tut) {
                Tut w = (Tut) getWorld();
                b = (Actor) w.getBot();
            } else {
                Cred w = (Cred) getWorld();
                b = (Actor) w.getBot();
             }
             if (b != null) {
                 dx = (int) Math.signum(0.0 + b.getX() - getX());
             }
             if(canMove(dx, 0, Obstacles.class)) {
                 setLocation(getX() + dx, getY());
                 animTimer++;
             }
         }
         return dx;
     }
 
     public void killObst() {
         if (!dead) {
             Class[] list = {Snowman.class, Log.class};
             for(Class obst : list) {
                 removeTouching(obst);
             }
         }
     }
 }
