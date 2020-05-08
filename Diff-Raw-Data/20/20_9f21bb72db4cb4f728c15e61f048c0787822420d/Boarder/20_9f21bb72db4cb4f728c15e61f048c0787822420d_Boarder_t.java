 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.util.*;
 /**
  * Write a description of class Boarder here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Boarder extends Object
 {   
     private int dragFromX, dragFromY;
     public int invincible = 0;
     private int airTime = 0;
     public int gun = 0;
     public int shotDelay = 20;
     public int jumpTime;
     public double jumpConst;
     public int magnetTimer = 0;

     public void act() 
     {
         moveAround();
         ramp();
         dieObstacle();
         dieTree();
         invincible++;
         airTime--;
         shotDelay--;
         respawnBlink();
         ramp();
         jump(jumpTime);
         magnet();
     }
 
     public void moveAround()
     {
         if (Greenfoot.isKeyDown("left") && getX() >= 0){
             if (!checkTree(-4) || invincible < 50) {
                 move(-4);
             }
             setImage("Left2.png");
         }
         if (Greenfoot.isKeyDown("right") && getX() <= getWorld().getWidth()){
             if (!checkTree(4) || invincible < 50) {
                 move(4);
             }
             setImage("Right2.png");
         }
         if (!Greenfoot.isKeyDown("left") && !Greenfoot.isKeyDown("right")){
             setImage("Forward2.png");
         }
         if (Greenfoot.isKeyDown("left") && Greenfoot.isKeyDown("right")){
             setImage("Forward2.png");
             move(0);
         }
         if (Greenfoot.isKeyDown("up")) {
             if (airTime <= -5) {
                 airTime = 50;
                 jumpTime = 50;
                 jumpConst = -50.0 / 1058.0;
             }
         }
         if (Greenfoot.isKeyDown("space") && gun > 0 && shotDelay <= 0) {
             Bullet bullet = new Bullet();
             getWorld().addObject(bullet, getX(), getY());
             shotDelay = 20;
             gun--;
         }
         // touchscreen (mouse drag) detection/movement
         if (Greenfoot.mousePressed(null))  
         {  
             MouseInfo mouse = Greenfoot.getMouseInfo();  
             dragFromX = mouse.getX();  
             dragFromY = mouse.getY();  
         }  
         if (Greenfoot.mouseDragged(null) || Greenfoot.mouseDragEnded(null))  
         {  
             MouseInfo mouse = Greenfoot.getMouseInfo();  
             int newX = mouse.getX(), newY = mouse.getY();  
             // check difference(s) and act upon them 
 
             if (newX > dragFromX + 30 && getX() <= getWorld().getWidth())
             {
                 move(4);
                 setImage("right.png");
             }
 
             if (newX < dragFromX - 30 && getX() >= 0)
             {
                 move(-4);
                 setImage("left.png");
             }
 
             if (newY < dragFromY - 30)
             {
                 if (airTime <= -20) {
                     airTime = 46;
                 }
             }
         }
         SnowWorld w = (SnowWorld) getWorld();
         w.incScore(1);
     }
 
     public void ramp() {
         if (airTime < 0 && !dead && isTouching(Ramp.class)) {
             airTime = 100;
             jumpTime = 100;   
             jumpConst = -25.0 / 1058.0;
         }
     }
 
     public int air() {
         return airTime;
     }
 
     public void dieObstacle(){
         Actor obstacle = getOneIntersectingObject(Obstacles.class);
         if (obstacle != null && invincible > 100 && airTime <= 0){
             SnowWorld w = (SnowWorld) getWorld();
             w.multCounter = 0;
             w.removeObject(this);
             dead = true;
         }
     }
 
     public void dieTree() {
         if (!dead) {
             Actor tree = getOneIntersectingObject(Tree.class);
             if (tree != null && invincible > 100) {
                 SnowWorld w = (SnowWorld) getWorld();
                 w.multCounter = 0;
                 w.removeObject(this);
                 dead = true;
             }
         }
     }
 
     public void respawnBlink() {
         if (invincible < 100) {
             GreenfootImage img = getImage();
             int fast = (invincible >= 0) ? 2 : 1;
             double transparency = 127 * Math.sin(fast * invincible / 4.0) + 128;
             img.setTransparency((int) transparency);
             setImage(img);      
         }
         if (invincible == 100) {
             GreenfootImage img = getImage();
             img.setTransparency(255);
             setImage(img);        
         }
     }
 
     public void jump(int jumpTime) {
        if (!dead && airTime > 0) {
            Image shadow = new Image("shadow.png");
            double scaleFactor = (jumpConst * airTime * airTime - jumpConst * jumpTime * airTime + 50) / 50 ; //parabolic path
            getImage().scale((int) (scaleFactor * getImage().getWidth()),(int) (scaleFactor * getImage().getHeight()));
            getWorld().addObject(shadow, getX(), getY() + (int) getImage().getHeight() / 2 );
         }
     }
     
     public void magnet() {
         magnetTimer--;
         if (magnetTimer > 0 && !dead) {
             List l = getNeighbours(200, false, Pickup.class);
             for (int i = 0; i < l.size(); i++) {
                 Actor a = (Actor) l.get(i);
                 a.turnTowards(getX(), getY());
                 a.move(Object.speed + 2);
                 a.setRotation(0);
             }
         }
         
     }
 }
