 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 
 /**
  * Write a description of class Archer here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 
 public class Archer extends Enemy
 {
     /**
      * Act - do whatever the Archer wants to do. This method is called whenever
      * the 'Act' or 'Run' button gets pressed in the environment.
      */
     private Ogre ogre;
     public int attackTimer=0;;
     public Archer(ImagePackage imgPack, Ogre ogre){
         super(imgPack);
         this.ogre=ogre;
     }
     public void act() 
     {
         super.act();
         move();
         attack();
         checkCollision();
     }
     private void move(){
         int x=ogre.getX()-getX();
         int y=ogre.getY()-getY();
         int x2= x*x;
         int y2= y*y;
         int xm=0;
         int ym=0;
         if(y<0 &&(y2>(200*200))){
             ym-=1;
             currDirection=NORTH;
         } else if (y2>(200*200)){
             ym+=1;
             currDirection=SOUTH;
         }
         if(x<0 &&(x2>(200*200))){
             xm-=1;
             currDirection=WEST;
         } else if ((x2>(200*200))){
             xm+=1;
             currDirection=EAST;
         }
         if(x2>y2){
             ym*=2;
         }else{
             xm*=2;
         }
         setLocation(getX()+xm,getY()+ym);
     }
     private void attack(){
         if (attackTimer>0){
             attackTimer--;
         }else{
             if((getX()>(ogre.getX()-5) && getX()<(ogre.getX()+5)) || (getY()>(ogre.getY()-5) &&getY()<(ogre.getY()+5))){
                 Arrow A= new Arrow();
                 getWorld().addObject(A,getX(),getY());
                 A.turnTowards(ogre.getX(),ogre.getY());
                attackTimer=15;
             }
         }
     }
 }
