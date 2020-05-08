 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 
 /**
  * Write a description of class Mortar here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Mortar extends Actor
 {
     private int targetx;
     private int targety;
     private int time;
     private int startTime;
     private int size;
     private int dropTime;
     private MortarShadow shadow;
     private int tempy;
     public Mortar(int targetx, int targety) {
         this.targetx = targetx;
         this.targety = targety;
         Greenfoot.playSound("MortarLuanch.wav");
         setImage("MortarShell.png");
         getImage().scale(50,50);
         getImage().rotate(180);
         time = 0;
         size = 50;
         startTime = 200;
     }
     
     
     protected void addedToWorld(World world) {
         getImage().setTransparency(0);
         shadow = new MortarShadow(targetx, targety);
         getWorld().addObject(shadow, targetx, targety);
         setLocation(targetx, 0);
     }
     
     
     public int getTime(){
         return time;
     }
     
     public void scaleImage(){
         int sizex = getImage().getWidth();
         int sizey = getImage().getHeight();
         if(sizex > 10 && sizey > 10)
             getImage().scale(sizex - 1, sizey - 1);
     }
     
     public void move(){
         getImage().setTransparency(255);
         setLocation(getX(), getY() + targety/50);
     }
     
     /**
      * Act - do whatever the Mortar wants to do. This method is called whenever
      * the 'Act' or 'Run' button gets pressed in the environment.
      */
     public void act() 
     {
         // Add your action code here.
         time++;
         if(time > startTime) {
             if(tempy >= 0){
                 move();
             }
             else
                 tempy += 3;
         }
         if(getY() > targety)
             getWorld().removeObject(this);
     }    
 }
