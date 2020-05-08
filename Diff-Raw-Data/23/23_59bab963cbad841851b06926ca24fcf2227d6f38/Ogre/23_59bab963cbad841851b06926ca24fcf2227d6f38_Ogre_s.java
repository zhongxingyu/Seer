 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.util.List;
 /**
  * Write a description of class Ogre here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Ogre extends SoundActor
 {
    
     
     /**
      * Act - do whatever the Ogre wants to do. This method is called whenever
      * the 'Act' or 'Run' button gets pressed in the environment.
      */
     private boolean gordo= true;
     private int switchWait =0;
     private int attackCooldown;
     private Face face;
     
     ImagePackage imgPack;
     
      public Ogre(ImagePackage imgPack, Face face) {
         super(imgPack);
         this.face=face;
         this.imgPack = imgPack;
         attackCooldown = 0;
     }
     
     public void act() 
     {
         if(switchWait<1){
             switchHead();
             switchWait=3;
         }else{
             switchWait--;
         }
         procInput();
         procAI();
         super.act();
     }
     private void procAI (){
         List<Enemy> enemies = getWorld().getObjects(Enemy.class);
         Enemy closest=null;
         int x1=0,x2=0,y1=0,y2=0;
         for (Enemy E: enemies){
             if(closest==null){
                 closest=E;
                 x2=getX()-closest.getX();
                 y2=getY()-closest.getY();
             }
             else{
                 x1=getX()-E.getX();
                 y1=getY()-E.getY();
                 x2=getX()-closest.getX();
                 y2=getY()-closest.getY();
                 if((x1*x1)+(y1*y1)< (x2*x2)+(y2+y2)){
                     closest=E;
                     x2=getX()-closest.getX();
                     y2=getY()-closest.getY();
                 }
             }
         }
         
         if (closest==null){
             return;
         }
         
         if(gordo){
             int dir;
             if((x2*x2) <(y2*y2)){
                 if(x2>0){
                     currDirection=WEST;
                     dir=WEST;
                 }else{
                     currDirection=EAST;
                     dir=EAST;
                 }
             }else{
                 if(y2>0){
                     currDirection=NORTH;
                     dir=NORTH;
                 }else{
                     currDirection=SOUTH;
                     dir=SOUTH;
                 }
             }
         }else{
             int x=0;
             int y=0;
             if(x2>0){
                 x-=5;
             }else if(x2<0){
                 x+=5;
             }
             if(y2>0){
                 y-=5;
             }else if(y2<0){
                 y+=5;
             }
             setLocation(getX()+x,getY()+y);
         }
     }
     private void switchHead(){
         if (Greenfoot.isKeyDown("s")){
             gordo= !gordo;
             face.face=gordo;
             face.change();
         }
     }
     private void procInput(){
         int x =0;
         int y=0;
         int dir=-1;
         
         if (attackCooldown != 0)
             attackCooldown = (attackCooldown + 1) % 10;
         
         if(Greenfoot.isKeyDown("a") && attackCooldown == 0 && !gordo){
             isAttacking=true;
             attackCooldown++;
         }
        if(Greenfoot.isKeyDown("left")){
            currDirection=WEST;
             if (gordo){
                 x-=5;
             }else{
                dir=WEST;
             }
         }
         if(Greenfoot.isKeyDown("right")){
            currDirection=EAST;
             if (gordo){
                 x+=5;
             }else{
                dir=EAST;
             }
         }
         if(Greenfoot.isKeyDown("up")){
            currDirection=NORTH;
             if (gordo){
                 y-=5;
             }else{
                dir=NORTH;
             }
         }
        if(Greenfoot.isKeyDown("down")){
            currDirection=SOUTH;
             if (gordo){
                 y+=5;
             }else{
                dir=SOUTH;
             }
         }
         setLocation(getX()+x,getY()+y);
         
     }
 }
