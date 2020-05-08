 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 
 /**
  * Write a description of class Boarder here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Boarder extends Object
 {   
     private int dragFromX, dragFromY;
     private int invincible = 0;
     private int airTime = 0;
     private int rampTime = 0;
     private int r;
     public int gun = 0;
     public int shotDelay = 20;
     public boolean dead = false;
     public void act() 
     {
         moveAround();
         dieObstacle();
         dieTree();
         invincible++;
         airTime--;
         rampTime--;
         shotDelay--;
         respawnBlink();
         jump();
         ramp();
     }
     
     public void moveAround()
     {
         if (Greenfoot.isKeyDown("left")){
             if (!checkTree(-4)) {
                 move(-4);
             }
             setImage("left.png");
         }
         if (Greenfoot.isKeyDown("right")){
             if (!checkTree(4)) {
                 move(4);
             }
             setImage("right.png");
         }
         if (!Greenfoot.isKeyDown("left") && !Greenfoot.isKeyDown("right")){
             setImage("straight.png");
         }
         if (Greenfoot.isKeyDown("left") && Greenfoot.isKeyDown("right")){
             setImage("straight.png");
             move(0);
         }
         if (Greenfoot.isKeyDown("up")) {
             if (airTime <= -20) {
                 airTime = 46;
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
             
             if (newX > dragFromX + 30)
             {
                 move(4);
                 setImage("right.png");
             }
             
             if (newX < dragFromX - 30)
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
     
     public int air() {
         return airTime;
     }
     
     public void dieObstacle(){
         Actor obstacle = getOneIntersectingObject(Obstacles.class);
         if (obstacle != null && invincible > 50 && airTime <= 0 && rampTime <= 0){
             getWorld().removeObject(this);
             dead = true;
         }
     }
     
     public void dieTree() {
         if (!dead) {
             Actor tree = getOneIntersectingObject(Tree.class);
             if (tree != null) {
                 getWorld().removeObject(this);
                dead = true;
             }
         }
     }
     
     public void respawnBlink() {
         if (invincible < 50) {
             GreenfootImage img = getImage();
             img.setTransparency(((invincible % 10) + 1)*(255/10));
             setImage(img);        
         }
         if (invincible == 50) {
             GreenfootImage img = getImage();
             img.setTransparency(255);
             setImage(img);        
         }
     }
     
     public void jump() {
         if (airTime > 0 && rampTime < 0) {
             GreenfootImage img = new GreenfootImage("shadow.png");
             img.drawImage(getImage(), 0, 0);
             double xy = 50.0 * (1.0 - (airTime) * (airTime - 46.0) / 1058.0); //parabolic path
             img.scale((int) xy,(int) xy);
             setImage(img);
         }
     }
     
     public void ramp() {
         if (!dead) {
             if(getOneIntersectingObject(Ramp.class) != null && airTime < 0){
                 rampTime = 200;
             }
             if (rampTime > 0){
                 r++;
                 GreenfootImage img = new GreenfootImage("shadow.png");
                 img.drawImage(getImage(), 0, 0);
                 if(rampTime > 135){
                     img.scale(getImage().getWidth() + (r / 2), getImage().getHeight() + (r / 2));
                 }
                 else{
                     img.scale(getImage().getWidth() + (rampTime / 2), getImage().getHeight() + (rampTime / 2));
                 }
                 setImage(img);
             }
             else{
                 r = 0;
             }
         }
     }
 }
