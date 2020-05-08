 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.awt.Color; 
 import java.util.List; 
 import java.util.ArrayList;
 import java.util.LinkedList;
 /**
  * Write a description of class Tower here.
  * 
  * @author (Terence Lai + James Lu) 
  * @version (2.00)
  */
 public class Tower extends Actor
 {
     protected boolean  active = false; //will not fire unless it is true 
 
     protected Map map;
     protected int price ;
     private int counter = 0; //counter used to place the object on the second click (counter ==3 )
     protected int gridX, gridY ;// refrence to what grid it is on
     protected Weapon basicAttack; //change this when upgraded.  
     protected int attackRate;
     protected int power  ;
     protected int range;
     protected int speed;
     protected int upgradeCounter;
     protected String type; 
     private ChatBox cb;
     protected int level; 
 
     //added by james
     protected Enemy             targetedMob;    
     protected String            name;
     protected ArrayList<String> desc;   
     //the element of the tower (air, water, fire, earth)
     protected int               element;
     protected String            elementString;      //the string of the element    
     protected int[]             upgradeCost;
     //targetting priority, 1 for closest, 2 for furthest, etc
     protected int               targetPriority;     
     //the number of debuffs this tower has bought
     protected LinkedList<Integer> debuffs;
     public Tower()
     {
         name = "";
         desc = new ArrayList<String>(5);
 
         targetedMob = null;
         targetPriority = 1;
         level = 1; 
         upgradeCost = new int[4];
         upgradeCost[0] = 0;
         upgradeCost[1] = 50;
         upgradeCost[2] = 100;
         upgradeCost[3] = 150;
         debuffs = new LinkedList<Integer>();
     }
 
     /**
      * passes map to all towers when all towers are added to world
      */
     protected void addedToWorld(World world){
         map = (Map) world;
         type = " "; 
     }
 
     /**
      * Set the tower to be live in the game. set false, to stop its functionality 
      */
     public void setActive(boolean n)
     {
         this.active=n;
         if (active){
             counter = 999;
         }
     }
 
     /**
      * Act - do whatever the Tower wants to do. This method is called whenever
      * the 'Act' or 'Run' button gets pressed in the environment.
      */
     public void act() 
     { 
         // System.out.println("Its Attacking with speed: " + attackrRate);
         if (active){
             attack();
         }
     }  
 
     /**
      * Allows the world to pass the grid coordinates to the object as a refrence
      */
     public void setGridX(int x)
     {
         this.gridX = x;
     }
 
     /**
      * Allows the world to pass the grid Coordinates to the object
      */
     public void setGridY(int y)
     {
         this.gridY = y;
     }
 
     /**
      * Access the grid X coordinates on field (Not actual coordinates)
      */
     public int getGridX()
     {
         return gridX;
     }
 
     /**
      * Acess the grid Y coordinates on field (Not actual Corrdinates)
      */
     public int getGridY()
     {
         return gridY;
     }
 
     /**
      * targeting and firing method for towers to attack the enemy.
      */
     protected void attack()
     {
         if (counter >= attackRate ) //used for the attack cycle of the weapon
         {
             if (targetedMob != null && targetedMob.getHp() > 0 && 
             getDistance (targetedMob) <= range/2){  
                 //if the mob its attacking is not dead     
                 launch();
             }
             else{
                 //get a group of enemies that is within range
                 Enemy attack = withinRange(targetPriority);
                 if (attack != null){
                     targetedMob = attack;      
                     launch();
                 }
             }
         }
         else{
             counter++;
         }
     }
 
     private void launch(){
         if (targetedMob.getTargetHp() >= 0){
             if (type.equals("pierce"))
             {//piercing strike
                 basicAttack = new Laser(speed, power,1,element,targetedMob,elementString);  
             }
             else if (type.equals("artillery"))
             {
                 basicAttack = new Artillery(speed, power, 150, element, targetedMob, elementString);    
                 targetedMob.targetDmg (power, element);            
             }
             else
             {  //the default attack
                 basicAttack = new Normal(speed, power,1, element, targetedMob, elementString);  
                 targetedMob.targetDmg (power, element);
             }
             map.addObject (basicAttack,this.getX(), this.getY());
             counter = 0 ; 
         }
     }
 
     /**
      * refunds the money back to the player 
      */
     public void sell()
     {
         map.changeMoney((int)(price*0.75)) ;//add money 75% of them money back.
     }
 
     /**
      * Checks to see if any enemies are within range of the tower
      * 
      * int parameter is the targetting priority
      * 1 is closest enemy
      * 2 is furthest 
      */
     private Enemy withinRange(int tp)
     {
         List<Actor> temp = getObjectsInRange(range/2, Enemy.class);
         ArrayList<Enemy> enemyList = new ArrayList();
         Enemy targetEnemy = null;
         if (temp.size() > 0){
             if (tp == 1){
                 Enemy e;
                 double hyp = range;
                 for (int i = 0; i < temp.size();i++)
                 {
                     if (temp.get(i) instanceof Enemy) 
                     {
                         e = (Enemy) temp.get(i); 
                         if (e.getHp() > 0 && e.getTargetHp() > 0){
                             double h = getDistance (e);
                             if (h < hyp){       
                                 //if the current hyp is less than the starting hyp
                                 hyp = h;
                                 targetEnemy = e;
                             }
                         }
                     }
                 }
             }
         }
         //System.out.println(enemyList);
         return targetEnemy; 
     }
 
     private double getDistance(Enemy e){        
         int tempX = (e.getX() - this.getX());
         int tempY = (e.getY() - this.getY());
         int adj = tempX * tempX;
         int opp = tempY * tempY;
         double h = Math.sqrt ((adj + opp));
         return h;
     }
 
     /**
      * chanes the weapon of the tower.
      */
     private void changeWeapon(String type){
         this.type = type; 
     }
 
     /**
      * Upgrades the tower by changing base stats and changes weapon or add debuff
      */
     public void upgrade()
     {
         if (level < 3)
         {
             power+=10;
             range+=3;
 
             if (attackRate >  0){
                 attackRate-=3;
                 if (attackRate <= 0){
                     attackRate = 1; 
                 }
             }
 
             if (level == 1)
             {
                 changeWeapon("pierce");
                 power = (int)(power*0.1);
             }
             else if (level == 2)
             {
                 changeWeapon("artillery");
                 attackRate= 300;
             }
             else if (level ==3)
             {
                 //addDebuff();
             }
             level++;
             map.displayMessage("Tower Upgraded"); 
         }
         map.resetUi(); //refreshes the ui after upgrade
     }
 
     /**
      * passes the maximum attack range of the tower
      */
     public int getRange()
     {
         return range; 
     }
 
     /**
      * passes the weapon the tower is currently using
      */
     public String getWeaponType()
     {
         return type; 
     }
 
     //added by james
     /**
      * return the name of the tower
      */
     public String getName(){
         return name;
     }
 
     /**
      * returns the description of the tower
      */
     public ArrayList<String> getDesc(){
         return desc;
     }
 
     /**
      * returns the damage of the tower
      */
     public int getDmg(){
         return power;
     }
 
     /**
      * returns the attack speed of the tower
      */
     public int getAttackSpeed(){
         return attackRate;
     }
 
     /**
      * return the cost to upgrade the tower
      */
     public int getUpgradeCost(){
         return upgradeCost[level-1];
     }
 
     /**
      * returns true if the tower can be upgraded further
      */
     public boolean upgradeable(){
         if (level < 3){
             return true;
         }
         return false;
     }
 
     /**
      * returns the current level of the tower
      */
     public int getLevel(){
         return level;
     }
 
     /**
      * returns the element of the tower
      */
     public int getElement(){
         return element;
     }
 
     /**
      * adds a debuff on the tower
      * called when the user buys a debuff
      */
     public void addDebuff(int id){
         debuffs.add (id);
     }
 
     /**
      * returns the number of debuffs this tower currently has
      */
     public int[] getDebuffs(){
         int[] temp = new int[debuffs.size()];
         for (int i = 0; i < debuffs.size(); i++){
             temp[i] = debuffs.get(i);
         }
         return temp;
     }
 }
