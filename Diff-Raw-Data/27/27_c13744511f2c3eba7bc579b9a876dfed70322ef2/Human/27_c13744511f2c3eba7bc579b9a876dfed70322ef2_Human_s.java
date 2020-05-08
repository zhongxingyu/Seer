 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.util.*;
 /**
  * Write a description of class Human here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Human extends Movement
 {
     protected Dialogue d;
     public static int Score = 0;
     protected boolean isCovered = false;
     private int health = 100; 
     private HealthBar healthBar;
     private ArrayList<DamageOverTime> dmgOverTime;
     protected boolean isRetreating = false;
     
     public Human() {
        d = new Dialogue();     
        dmgOverTime = new ArrayList<DamageOverTime>();
     }
     
     public void addDamageOverTime(int damage, int time) {
         dmgOverTime.add(new DamageOverTime(damage,time));
     }
     
     public int getHealth() {
        return health;     
     }
     
     public void setCovered(boolean covered) {
         isCovered = covered;
     }
     
     public boolean isCovered(){
         return isCovered;
     }
     
     public void retreat() {
         isRetreating = true;
     }
     
     public boolean isRetreating() {
         return isRetreating;
     }
     
     public void createHealthBar(){
         healthBar = new HealthBar(this);
         getWorld().addObject(healthBar, getX() , 
             getY() - getImage().getHeight()/2 -1); 
     }
 
     public void loseHealth(int damage) {
        health -= damage;
        if (health <= 0) {
           die(); 
         }
     }
     
     public void die() {
         if (this instanceof British) {
             if (this instanceof WinstonCrowley)
                 Greenfoot.setWorld(new DefeatWorld(Human.Score));
             
              ((HumanWorld)this.getWorld()).decBCounter();
         }
         else {
                ((HumanWorld)this.getWorld()).decGCounter();
                Score+=100;
         }
         getWorld().removeObject(healthBar);
         getWorld().removeObject(this);
         
     }
     public void applyDamageOverTime(){
          for(DamageOverTime dmg : dmgOverTime){
             if(dmg.getDamageLeft() > 0){
                 loseHealth(dmg.getDamagePerTick());
                 dmg.decrementDamageLeft();
             }
         }
     }
     /**
      * Act - do whatever the Human wants to do. This method is called whenever
      * the 'Act' or 'Run' button gets pressed in the environment.
      */
     public void act() 
     {
         // Add your action code here.
 
     }    
     
    public void yield() {
        if (!(this instanceof WinstonCrowley)) {
           setLocation(getX()-1, getY()); 
        }
       
     }
     
     /* direction is 1 for right -1 for left */
     public void NPCAdvance(int direction) {
        Flag enemyflag = null; 
        if (this instanceof German) {
         enemyflag = (Flag)getWorld().getObjects(Flag.class).get(1);
        }
        else {
           enemyflag = (Flag)getWorld().getObjects(Flag.class).get(0); 
         }
        List<Actor> inWay = getObjectsAtOffset(direction, 0, Obstacle.class);
         if (inWay.size() == 0) {
             
             Human target = (Human)getOneIntersectingObject(Human.class);
             if (target != null) {
                if (!(target instanceof WinstonCrowley)) {
                  target.yield();
               }
             
             }
             else {
             
                 if (enemyflag.getX() < getX()) {
                    setLocation(getX()-1, getY()); 
                 }
                 else {
                    setLocation(getX()+1, getY());  
                 }
                 
                  if (enemyflag.getY() < getY() && enemyflag.getX() == getX()) {
                    setLocation(getX(), getY()- 1); 
                 }
                 else if (enemyflag.getX() == getX()) {
                    setLocation(getX(), getY()+1);  
                 }
             }
            
        } else {
            
            for (Actor a : inWay ) {
            
             if (a instanceof Sandbag) {
                 setLocation(getX(), getY()+ 1); 
             }
           }
        }
       
     }
     
     
     
 }
