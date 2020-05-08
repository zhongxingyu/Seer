 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.awt.Color;
 
 /**
  * Element Tower Defence
  * 
  * @James Lu & Terence Lai
  * @0.16
  */
 public class Map extends World{
     private Tile[][] map;                                 //2d array of tiles
     private StartScreen startScreen;                      //the start screen before the main game
 
     //starting and ending points of start, checkpoints and end
     private int startX, startY;
     private int endX1, endY1;
     private int endX2, endY2;
     private int endXf, endYf;
 
     private int topSpace, leftSpace;                     //top and left spaces before the grid starts
 
     //three pathfinders for the 3 paths
     private Pathfind pf1;
     private Pathfind pf2;
     private Pathfind pf3;
 
     private ArrayList <ArrayList <Integer>> flyingPath;          //path for the flying units
 
     //list of mobs
     private ArrayList<Enemy> mobs;
     private LinkedList<Enemy> mobsToSpawn;
 
     //variables for spawning mobs
     private boolean start;
     private boolean levelStart;
     private boolean flyingLevel;
     private boolean bossLevel;
     private int currentElement;
     private int nextElement;
     private int level;
     private int time;
     private int spawnCount;
     private int maxSpawnCount;
     private int spawnRate;
 
     private int money;
     private int income;
     private int lives;
     private int currentRunSpeed;
     private int[] runSpeed;
 
     //ui stuff
     private Ui ui;
     private boolean refreshUi;
     private ChatBox cb;
     private Data data;
     private PointerArrow pa;
     public TowerButton towerButton;
     public CreepButton creepButton;
     private Button prevButton;
     public final HoverMenu hm = new HoverMenu();
 
     /**Terrence's variables **/
     //private Tower t = new Tower(); 
     private ArrayList<Tower> towers = new ArrayList<Tower>(); //put in constructor 
 
     private boolean place =  false; //used to place the tower onto the field
     private int lvCounter;
     private int buttonDelay; 
 
     //must keep a temporary refrence to the tower since it there are subsequent commands
     private Tower selectedTower;
     private Tower placeHolder;
     Range r; 
 
     /** 
      * variable initation
      * put in here since you intalize everything after you start the game
      */
     private void intalize (){
         //x and y values
         startX = 0; startY = 0;
         endX1 = 10; endY1 = 12;
         endX2 = 39; endY2 = 12;
         endXf = 49; endYf = 24;
         topSpace = 15; leftSpace = 12;  //spaces from the top and left, hardcoded on enemy
 
         data = new Data();
 
         map = new Tile [endYf+1][endXf+1];        //new map
 
         //new arraylists
         flyingPath = new ArrayList <ArrayList <Integer>>();
         mobs = new ArrayList<Enemy>();
         mobsToSpawn = new LinkedList <Enemy> ();
 
         //new pathfinders
         pf1 = new Pathfind (startX, startY, endX1, endY1, this);       //top to the first checkpoint
         pf2 = new Pathfind (endX1, endY1, endX2, endY2, this);         //middle path
         pf3 = new Pathfind (endX2, endY2, endXf, endYf, this);         //rightmost path
 
         //tower defence stuff
         money           = 100000;
         income          = 0;
         lives           = 20;
         level           = 1;
         time            = 0;
         spawnCount      = 0;
         flyingLevel     = false;
         levelStart      = false;
         bossLevel       = false;
         currentRunSpeed = 3;
         runSpeed = new int [5];
         runSpeed[0] = 40;
         runSpeed[1] = 50;
         runSpeed[2] = 60;
         runSpeed[3] = 70;
         runSpeed[4] = 80;
 
         //ui
         ui = new Ui();             
         refreshUi = false;
         cb = new ChatBox();
         pa = new PointerArrow();
         towerButton = new TowerButton();
         creepButton = new CreepButton();
         buttonDelay = 0; 
 
         //generates the map
         generate();      
 
         //sets the path first
         setPath();
 
         //generates the path for the flying path, its always gona stay the same, so doing it here
         flyingPath.add (pf1.generatePath());       
         flyingPath.add (pf2.generatePath());
         flyingPath.add (pf3.generatePath());
 
         //populates the data queue for mobs
         data.populate();
 
         addObject (ui, 512, 643);
         addObject (cb, 512, 500);
         addObject (towerButton, 262, 538);
         addObject (creepButton, 762, 538);
 
         /** Terence's stuff**/
         //Towers
         towers = new ArrayList<Tower>();
 
         //variable to run place movemet 
         place =  false;
     }
 
     /**
      * Act - do whatever the Tower wants to do. This method is called whenever
      * the 'Act' or 'Run' button gets pressed in the environment.
      */
     public void act(){
         MouseInfo mouse = Greenfoot.getMouseInfo() ;
         //if it gone past the start menu
         if (start){
             uiAct();
             //if the level started, increase the time and prepare the spawn
             if (levelStart){
                 prepareSpawn();
                 time++;
             }
             /**Terence's Stuff**/
             checkInput(mouse); 
             if (mouse != null){
                 trackButtons(mouse);
                 if (place) // used to place tower when onto the field
                     trackMouse(mouse);
             }
         }
         else{
             //if the user presses a when they are on the start screen, start the actual game
             //temp fix, add buttons later
             if (Greenfoot.isKeyDown (" ")){
                 start = true;
                 removeObject (startScreen);
                 intalize();
             }
         }
     }
 
     /**
      * Method that checks the mouse over of buttons
      */
     private void trackButtons (MouseInfo m){
         List<Actor> l = getObjectsAt (m.getX(), m.getY(), null);        //list of all the actors the mouse is ontop of
         for (Actor a: l){
             if (a instanceof Button){       //if the mouse is ontop of a button, change the pic
                 Button b = (Button) a;
                 if (b == prevButton){
                     b.hoverOver();
                 }else if (prevButton != null){
                     prevButton.resetCounter();
                 }
                 b.changeImg (true);
                 prevButton = b;
                 return;
             }
         }
         removeObject (hm);
     }
 
     /**
      * Method that checks what did the user click on.  or what buttons are pressed
      */
     private void checkInput(MouseInfo mouse){
         if(mouse != null && Greenfoot.mouseClicked(null)){
             Actor selected = mouse.getActor();
             if      (selected instanceof Tower && placeHolder == null){
                 //if the user selectes a tower, are not placing another tower and have not selected another tower
                 selectedTower = (Tower) selected ;               
                 refreshUi = true;
                 resetDefaultUi();
             }
             else if (selected instanceof Tile || selected instanceof Range){    //cancel selection of the tower
                 cancelBuild();                
             } 
             else if (selected instanceof Enemy && selectedTower == null){
                 cancelBuild();
                 ui.setMobData ((Enemy)selected);
                 ui.changeUi (5);
                 drawArrow (false);
             }
             else if (selected instanceof Button){
                 Button b = (Button) selected;
                 if (selected instanceof TowerButton){
                     cancelBuild();
                     ui.changeUi (1);
                     towerButton.clicked (true);
                 }
                 else if (selected instanceof CreepButton){
                     cancelBuild();
                     ui.changeUi (2);
                     creepButton.clicked (true);
                 }
                 else if (selected instanceof SellButton && selectedTower != null){
                     b.clicked(true);
                     map[selectedTower.getGridY()] [selectedTower.getGridX()].setWalkable(true);
                     map[selectedTower.getGridY()] [selectedTower.getGridX()].setPlaceable(true);
                     removeObject (selectedTower);
                     towers.remove(selectedTower); 
                     setPath();
                     cancelBuild();
                 }
                 else if (selected instanceof UpgradeButton){
                     b.clicked (true);
                     if (money >= selectedTower.getUpgradeCost()){
                         if (selectedTower.upgradeable()){
                             selectedTower.upgrade();  
                             changeMoney (-selectedTower.getUpgradeCost());
                         }else{
                             cb.setMessage ("Tower is fully upgraded", 1);
                         }
                     }else{
                         cb.setMessage ("You Require More Money", 1);
                     }
                 }
                 else if (selected instanceof SendCreeps){
                     SendCreeps button = (SendCreeps) selected;
                     button.clicked (true);
                     if (money >= button.getCost()){
                         income += button.getIncome();
                         money  -= button.getCost();
                         resetUi();
                         mobsToSpawn.add (button.getCreep());
                         cb.setMessage ("sending 1 creep", 2);
                     }
                     else{
                         cb.setMessage ("YOU DO NOT HAVE ENOUGH MONEY", 1);
                     }
                 }
                 else if (selected instanceof Debuff){
                     Debuff d = (Debuff) selected;
                     d.bought (true);
                     selectedTower.addDebuff (d.getId());
                 }
             }
         }
         //short cut keys that do not use the mouse
 
         //if the user presses s, start the level
         //temp spawning        
         if (Greenfoot.isKeyDown ("S") && levelStart == false){
             cancelBuild(); 
             levelStart = true;
             setLevelSpawn();
         }else if (Greenfoot.isKeyDown("escape")){
             cancelBuild(); 
         }else if (Greenfoot.isKeyDown("=")){
             if (buttonDelay > 50){
                 currentRunSpeed++;
                 buttonDelay = 0;
                 if (currentRunSpeed <= 4){
                     Greenfoot.setSpeed (runSpeed[currentRunSpeed]);
                     cb.setMessage ("Speed changed to " + runSpeed[currentRunSpeed], 2);
                 }else{
                     currentRunSpeed = 4;
                 }
             }
         }else if (Greenfoot.isKeyDown("-")){
             if (buttonDelay > 50){
                 currentRunSpeed--;
                 buttonDelay = 0;
                 if (currentRunSpeed >= 0){
                     Greenfoot.setSpeed (runSpeed[currentRunSpeed]);
                     cb.setMessage ("Speed changed to " + runSpeed[currentRunSpeed], 2);
                 }else{
                     currentRunSpeed = 0;
                 }
             }
         }else if (Greenfoot.isKeyDown("1")){
             cancelBuild();
             ui.changeUi (1);
             towerButton.clicked (true);
         }else if (Greenfoot.isKeyDown("2")){
             cancelBuild();
             ui.changeUi (2);
             creepButton.clicked (true);
         }
         else if (Greenfoot.isKeyDown("U") && selectedTower != null)//temp placement
         {
             if (buttonDelay >= 38) {
                 if (money >= selectedTower.getUpgradeCost() ){
                     if (selectedTower.upgradeable() ){
                         selectedTower.upgrade();  
                         changeMoney (-selectedTower.getUpgradeCost());
                         buttonDelay =  0;
                     } else{
                         cb.setMessage ("Tower is Fully Upgraded", 1);
                     }
                 }else{
                     cb.setMessage ("You Require More Money", 1);
                 }
             }
         }
         else if (placeHolder == null && selectedTower == null){
             boolean temp = false;
             if      (Greenfoot.isKeyDown ("F")){
 
                 //build the fire tower
                 placeHolder = new FireTower();    
                 temp = true;
             }
             else if (Greenfoot.isKeyDown("W")){
                 placeHolder = new WaterTower(); 
                 temp = true;
             }
             else if (Greenfoot.isKeyDown("G")){
                 placeHolder = new EarthTower(); 
                 temp = true;
             }
             else if (Greenfoot.isKeyDown("A")){
                 placeHolder = new AirTower();
                 temp = true;
             }   
             else if (Greenfoot.isKeyDown("B"))
             {
                 placeHolder = new Wall();
                 temp = true;
             }
 
             if (temp){
                 if (levelStart == false){
                     place = true;                               //terrence's place variable
                     ui.setTowerData (placeHolder);              //changes the ui to match the tower
                     ui.changeUi (4);
                     r = new Range(placeHolder.getRange()) ;     //shows the range of the tower
 
                     if (getObjects (PointerArrow.class) != null){       //removes the arrow if there is one
                         removeObject (pa);
                     }
                     addObject (r, 10000, 10000); 
                     addObject (placeHolder, 10000, 10000);
                 }
                 else{
                     placeHolder = null;             
                     cb.setMessage ("You Cant Build During a Wave", 1);
                 }
             }
         }
         buttonDelay++;
     }
 
     /**
      * Method for following the mouse when placing on field and placing the object. 
      * WHEN PLACE == TRUE
      * */
     public void trackMouse(MouseInfo mouse){        
         double x = mouse.getX();
         double y = mouse.getY();
 
         float snapRealX = Math.round((x-22)/20f) * 20f + 22;
         float snapRealY = Math.round((y-25)/20f) * 20f + 25;
 
         int snapX = Math.round(( (float) x-22) /20f); //these are the coordinates of the grid
         int snapY = Math.round(( (float) y-25) /20f);
 
         if (withinField (snapRealX , snapRealY)){
             placeHolder.setLocation ( (int)snapRealX,(int)snapRealY);
 
             if (r == null)
             {
                 r = new Range(placeHolder.getRange());
                 addObject (r, Math.round (snapRealX), Math.round (snapRealY));
             }
             else 
             {
                 r.setLocation (Math.round (snapRealX), Math.round (snapRealY));
             }
 
             if (Greenfoot.mouseClicked (null) ) {
                 createTower((int)snapRealX,(int)snapRealY, snapX, snapY,Greenfoot.isKeyDown("shift")) ;
 
                 removeObject (r);
                 r = null; 
             }  
         }
     }
 
     /**
      * cancels build mode and deletes the placeholder tower
      */
     private void cancelBuild(){
         //REFUND THE MONEY WHEN YOU CANCEL BUILD 
 
         if (placeHolder != null){           //cancels the building of a tower
             removeObject(placeHolder);
 
             placeHolder = null; 
             place = false; 
 
             removeObject (r);
             r = null;
         }
         else if (selectedTower != null){         //cancels the selection of a tower
             ui.changeUi (0);
             showRange (0, false);
             selectedTower = null;
         }
 
         if (ui.getId () != 0){
             ui.changeUi (0);
         }
         resetDefaultUi();
     }
 
     /**
      * used to delete the pointer arrow
      * called after the mob its pointing to dies
      * or 
      * when the user cancels the tower selection
      */
     public void resetDefaultUi(){
         if (getObjects (PointerArrow.class) != null){
             removeObject (pa);
             ui.changeUi (0);
         }
         towerButton.clicked (false);
         creepButton.clicked (false);
     }
 
     /**
      * Creates a tower and adds it to the world. detects what tower to place based on its place holder
      */
     private void createTower(int realX, int realY, int x, int y,boolean shiftClick){
         int cost = 5;
         if (money >= cost){
             if (map[y][x].getWalkable() && map[y][x].getPlaceable() && canStillWalk(x, y)){
 
                 Tower t; 
 
                 changeMoney (-cost);
                 //places the real tower 
                 selectedTower = null;  // prevents the placement click from slecting the tower
 
                 map[y][x].setWalkable(false);  
                 map[y][x].setPlaceable (false);
                 if      (placeHolder instanceof  FireTower){
                     t = new FireTower();}
                 else if (placeHolder instanceof WaterTower){
                     t= new WaterTower();}
                 else if (placeHolder instanceof EarthTower){
                     t= new EarthTower();  }
                 else if (placeHolder instanceof   AirTower){
                     t= new AirTower(); }
                 else if (placeHolder instanceof Wall){
                     t = new Wall(); }
 
                 else{
                     t= new Tower(); //this should never happen it is only to make code compile
                 }
 
                 if (!shiftClick){
                     cancelBuild();
                 }
                 t.setGridX(x);//pass the grid coordinates for further refrence
                 t.setGridY(y);
                 addObject(t,realX,realY);
 
                 towers.add(t);  //added to arraylist
                 setPath();
                 t.setActive(false);
 
             }
             else{
                 //add code to inform the user you cant build there
                 cb.setMessage ("YOU CANT BUILD THERE", 1);
             }
         }
         else{
             cb.setMessage ("YOU REQUIRE MORE MINERALS", 1);
         }
     }
 
     /**
      * changes to the money either through buying or selling. positive numbers increase money while negative numbers decrease money.
      */
     public void changeMoney(int t){
         money+=t;
 
         resetUi();
     }
 
     /**
      * Checks to see if the coordinates supplied are within the playing area of the game
      */
     public boolean withinField(double x, double y){
         if ( x > 15 && x < 1014 && y >= 25 && y < 518) {
             return true;
         }
         return false; 
     }
 
     /**
      * Constructor for objects of class Map.
      */
     public Map(){    
         super(1024, 768, 1); 
 
         startInitialize (false);
 
         setPaintOrder (StartScreen.class, HoverMenu.class, ChatBox.class, 
             Button.class, Ui.class, PointerArrow.class, Weapon.class, 
             Tower.class, Range.class, HealthBar.class, Enemy.class, Tower.class, Tile.class);        
     }
 
     /**
      * draws arrow on top of the selected tower to giver better indication to the user. 
      */
     public void drawArrow(boolean ifTower){
         if (ifTower){
             addObject (pa, selectedTower.getX(), selectedTower.getY() - 20);
             pa.setMob (null);
         }
         else{
             Enemy e = ui.getMob();
             if (e.getHp() > 0){
                 pa.setMob (e);
                 addObject (pa, e.getX(), e.getY() - 20);
             }
         }
     }
 
     private void uiAct(){
         if (selectedTower != null && refreshUi){
             refreshUi = false;
             ui.setTowerData (selectedTower);
             ui.changeUi (3);
             drawArrow (true);
             if (r != null)
             {
                 removeObject(r);
             }
             showRange(selectedTower.getRange(), true);                    
         }
     }
 
     private void showRange(int range, boolean show){
         if (show){
             r = new Range(range);
             addObject (r, selectedTower.getX(), selectedTower.getY());
         }
         else{
             removeObject (r);
             r = null;
         }
     }
 
     /**
      * resets the ui
      * call after change any values
      */
     public void resetUi (){
         ui.setGeneralData (lives, money, income);
     }
 
     /**
      * initializes the start menu
      * called when reseting the game
      */
     private void startInitialize (boolean restart){        
         startScreen = new StartScreen (restart);        
         addObject (startScreen, 512, 384);
         start = false;
     }
 
     /**
      * lose a life
      * called after a mob exits the map
      */
     public void loseLife(){
         if (lives == 1){
             start = false;
             removeObjects (getObjects(null));       //removes all the objects on the screen
 
             //starts the game agian
             startInitialize (true);
         }
         lives--;
         resetUi();
         cb.setMessage ("Lost a life", 1);
     }
 
     /**
      * called after a mob dies
      * deletes it from the world, and removes it from the arraylist of mobs
      */
     public void mobDie (Enemy mob, boolean exitWorld){
         if (!exitWorld){
             changeMoney (mob.getMaxHp() / 20);
         }
         else{
             loseLife();
         }
         mobs.remove (mob);
         removeObject (mob);
     }
 
     /**
      * prepares the spawn for the mobs
      */
     private void prepareSpawn(){
         if (time % spawnRate == 0){
             if (maxSpawnCount > spawnCount){
                 spawn(mobsToSpawn);
                 spawnCount++;
             }
         }
 
         if (maxSpawnCount <= spawnCount && mobs.size() == 0){
             levelStart = false;
             ui.setWaveData (0, nextElement);
             spawnCount = 0;
             time = 0;
             level++;
 
             List <Tower> tempTower = getObjects (Tower.class);
             for (Tower t: tempTower){
                 t.setActive (false);
             }
         }
     }
 
     /**
      * sets what element the current wave is
      * also sets the special type of enemy (boss, flying, etc)
      */
     private void setLevelSpawn(){
         data.nextLevel();
         bossLevel       = data.ifBoss();    //checking if its a boss level, every 10 levels        
         currentElement  = data.getType();   //1 is air, 2 is water, 3 is fire, 0 is earth
         nextElement     = data.getNextType();
         maxSpawnCount   = data.getMaxSpawn();
         spawnRate       = data.getSpawnRate();
         int tempSpawn   = mobsToSpawn.size();       //the number of mobs the user sent
 
         /** insert here code to tell user the new round started */
         ui.setWaveData (currentElement, nextElement);
 
         money += income;
         ui.setGeneralData (lives, money, income);
         cb.setMessage ("Level " + level + " Starting", 2);
 
         for (int i = 0; i < maxSpawnCount; i++){
             Enemy newEnemy = null;
             //add what type of enemy to make
             if      (currentElement == 1){
                 newEnemy = new AirMob   (data.getHp(), data.getArmor(), data.getSpeed(), 
                     data.ifBoss(), data.ifFlying() );
             }
             else if (currentElement == 2){
                 newEnemy = new WaterMob (data.getHp(), data.getArmor(), data.getSpeed(), 
                     data.ifBoss(), data.ifFlying() );
             }
             else if (currentElement == 3){
                 newEnemy = new FireMob  (data.getHp(), data.getArmor(), data.getSpeed(), 
                     data.ifBoss(), data.ifFlying() );
             }
             else if (currentElement == 4){
                 newEnemy = new EarthMob (data.getHp(), data.getArmor(), data.getSpeed(), 
                     data.ifBoss(), data.ifFlying() );            
             }
             mobsToSpawn.addFirst (newEnemy);
         }
 
         //adds the numbers of mobs to spawn to the max spawn count
         maxSpawnCount += tempSpawn;
 
         List <Tower> tempTower = getObjects (Tower.class);
         for (Tower t: tempTower){
             t.setActive (true);
         }
         //System.out.println (level);
     }
 
     /**
      * spawns a mob
      */
     private void spawn(LinkedList<Enemy> ll){
         Enemy newEnemy = ll.remove();
         addObject (newEnemy, 10+leftSpace, 10+topSpace);
         mobs.add (newEnemy);
     }
 
     /**
      * sets the path on the tiles
      */
     private void setPath(){
         ArrayList <ArrayList <Integer>> finalPath = new ArrayList <ArrayList <Integer>>();
         if (flyingLevel){   //flying level path is always the same
             finalPath = flyingPath;
         }
         else{
             //generates the 3 paths for each checkpoint
             finalPath.add (pf1.generatePath());       
             finalPath.add (pf2.generatePath());
             finalPath.add (pf3.generatePath());
         }
 
         for (int Y = 0; Y < map.length; Y++){
             for (int X = 0; X < map[0].length; X++){
                 map [Y][X].reset();
             }
         }
 
         for (int n = 0; n < 3; n++){
             ArrayList<Integer> tempPath = finalPath.get (n);
             for (int i = tempPath.size()-1; i >=0 ;i--){            
                 int tempY = tempPath.get (i);
                 i--;
                 int tempX = tempPath.get (i);
 
                 //changes the id of the tile so it is walkable
                 map [tempY][tempX].setId (1);
 
                 //setting the direction for the tiles if its not the last tile
                 if (i >= 2){
                     int tempPathX = tempPath.get (i-2);
                     int tempPathY = tempPath.get (i-1);
 
                     int movementX = tempPathX - tempX;
                     int movementY = tempPathY - tempY;
                     map[tempY][tempX].setMovement (movementX, movementY, n);
                 }
             }
         }
         map[endYf][endXf].setMovement(true);
     }
 
     private boolean canStillWalk(int x, int y){
         map[y][x].setWalkable (false);
         boolean result = false;
         if (pf1.generatePath() != null){
             if (pf2.generatePath() != null){
                 if (pf3.generatePath() != null){
                     result = true;
                 }
             }
         }
         map[y][x].setWalkable (true);
         return result;
     }
 
     /**
      * generates the map at first
      */
     private void generate(){        
         for (int y = 0; y < map.length; y++){
             for (int x = 0; x < map[0].length; x++){
                 map [y][x] = new Tile(x, y, this);
                 addObject (map[y][x], x*20+10 + leftSpace, y*20+10 + topSpace);
             }
         }
         map[endYf][endXf].setMovement(true);
 
         //sets the special colors
         map [startX][startY].setId (2, true);
         map [endY1][endX1].setId (3, true);
         map [endY1][endX2].setId (3, true);
         map [endYf][endXf].setId (4, true);
     }
 
     /**
      * returns of the tile on x, y is walkable
      */
     public boolean getTileWalkable(int x, int y){
         if (map[y][x].getWalkable()){
             return true;
         }
         return false;
     }
 
     /**
      * returns the X length of the map in tiles
      */
     public int getSizeX(){
         return map[0].length;
     }
 
     /**
      * returns the Y length of the map in tiles
      */
     public int getSizeY(){
         return map.length;
     }
 
     /**
      * returns the tile at X, Yhl1wjrh
      */
     public Tile getTile(int x, int y){
         if (x >= 0 && x < map[0].length){
             if (y >= 0 && y < map.length){
                 return map [y][x];
             }
         }
         return null;
     }    
 
     /**
      * Checks to see if the enemy is still in game
      */
     public boolean checkEnemy(Enemy n){
         if (mobs.contains(n))
         { return true;}
         return false; 
     }
 
     /**
      * returns the current income
      */
     public int getIncome(){
         return income;
     }
 
     public void displayMessage(String s)
     {
         cb.setMessage(s,1);
     }
 }
