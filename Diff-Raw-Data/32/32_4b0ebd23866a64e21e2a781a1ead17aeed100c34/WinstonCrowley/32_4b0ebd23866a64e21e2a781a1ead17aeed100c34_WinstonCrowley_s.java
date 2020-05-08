 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.awt.Color;
 import java.util.*;
 /**
  * Write a description of class WinstonCrowley here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class WinstonCrowley extends British
 {
     private final int SHOOT_RANGE = 500;
     private int runTimer = 0;
     private boolean loaded = true;
     private int lastShot = 0;
     private int shootSpeed = 20;
     private int reloadClock = 0;
     private int reload = 100;
     private int magazineRemaining = 5;
     Dialogue reloadDialogue = new Dialogue();
     /**
      * Act - do whatever the WinstonCrowley wants to do. This method is called whenever
      * the 'Act' or 'Run' button gets pressed in the environment.
      */
     
     public boolean inRange() {
         ArrayList<Actor> germans = (ArrayList<Actor>)getObjectsInRange(SHOOT_RANGE, German.class);
         return !germans.isEmpty(); 
     }
     
     public void act() 
    {
         if (Greenfoot.mouseClicked(null) && loaded && lastShot / shootSpeed > 0 && inRange()) {
             if(--magazineRemaining == 0){
                reloadClock = 0;
                loaded = false;
             }
             lastShot = 0;
             MouseInfo info = Greenfoot.getMouseInfo();
             Greenfoot.playSound("gunFire.mp3");
             getWorld().addObject(new Bullet(this, info.getX(), info.getY()), 
             this.getX() + this.getImage().getWidth()/2 + 10, this.getY() - 10);
         }else if(!loaded){
             if(reloadClock / reload > 0) {
                 loaded = true;
                 magazineRemaining = 5;
             } else {
                 reloadClock++;
             }
         } else {
             lastShot++;
         }
         if(!loaded) {
              GreenfootImage reloadMessage = new GreenfootImage("Reloading", 30
                                     , Color.WHITE, new Color(0,0,0,Color.TRANSLUCENT));
              reloadDialogue.setImage(reloadMessage);
              this.getWorld().addObject(reloadDialogue, 50, this.getWorld().getHeight() - 50);
         } else {
              this.getWorld().removeObject(reloadDialogue);
         }
         if(move())
         {
             runTimer++;
             if (runTimer==24)
             {
                 setImage("winston-running-right-4.png");
                 runTimer=0;  
             }
             if (runTimer==0) setImage("winston-running-right-1.png");  
             if (runTimer==8) setImage("winston-running-right-2.png");  
             if (runTimer==16) setImage("winston-running-right-3.png");
         }
         else {
            setImage("man-aiming-right.png");
         }
         d.ExecuteDialogueInteraction();
         applyDamageOverTime();
     }    
 }
