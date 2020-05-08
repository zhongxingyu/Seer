 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.awt.*;
 import java.util.*;
 /**
  * Write a description of class InfamyWorld here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class TutorialInfamyWorld extends HumanWorld
 {
     //phases 0, combat
     //1, retreat
     //2, cover
     
    
     public Dialogue dia;
     public Dialogue tutorialDiaIntro;
     public Dialogue tutorialDia;
     public int dialogueCounter;
     public int dialogueTimer;
     public Flag germanFlag;
     public Flag britishFlag;
     private int germansKilled = 0;
     private int numGermans = 0;
     private int phase = 0;
     private int phaseTimer = 0;
     private MountedMachineGun gun1;
     private MountedMachineGun gun2;
     /**
      * Constructor for objects of class InfamyWorld.
      * 
      */
     public TutorialInfamyWorld()
     {    
         // Create a new world with 600x400 cells with a cell size of 1x1 pixels.
         
         super(1024, 600, 1);
        
         dialogueTimer = 0;
         setBackground("Background.png");
         dialogueCounter = 0;
         //AddTutorialDialogue("Kill all the enemies and capture the flag!!\n Move with AWSD and shoot with mouse1", 512, 50, false);
         //AddTutorialDialogue("Kill all the enemies and capture the flag!!\n Move with AWSD as shoot with mouse1", 512, 525, false);
         //AddTutorialDialogue("Hey Winston!!\nCome over here!", 200, 300, false);
         //AddTutorialDialogue("Talk to your fellow soldier and\nother NPC's by pressing the 'e' key.\nMove Winston with the 'wasd' keys.", 400, 100, true);
       
         populate();
         
         spawnWave(GERM, NUM_ADVANCERS_3, false); 
         spawnWave(BRIT, NUM_ADVANCERS_3, false); 
         spawnG = true;
         spawnB = true; 
        // Greenfoot.setWorld(new BombTheBase());
     }
   
     public void populate() {
 
         germanFlag = new Flag("German"); 
         addObject(germanFlag, 974, 245);
         
         britishFlag = new Flag("British"); 
         addObject(britishFlag, 50, 245); 
         
 
         EnemyNPC germanDefender1 = new EnemyNPC(true);
         addHuman(germanDefender1, 900, 400); 
         
         EnemyNPC germanDefender2 = new EnemyNPC(true); 
         addHuman(germanDefender2, 900, 100); 
         
         
        CrossHair crosshair = new CrossHair();
        addObject(crosshair, 0, 0);
         
        //Sandbag sb1 = new Sandbag();
        //sb1.turn(90);
       // addObject(sb1, 600, 130);
        
        Sandbag sb2 = new Sandbag();
        //sb2.turn(90);
        addObject(sb2, 500, 400);
        Sandbag sb3 = new Sandbag();
        //sb3.turn(90);
        addObject(sb3, 300, 375);
        Sandbag sb4 = new Sandbag();
        //sb4.turn(90);
        addObject(sb4, 400, 190);
 
        FadingDialogue f = new FadingDialogue(512, 50, "1912 France, 87th Infantry", 10, 10);
         addObject(f, 512, 50);
        FadingDialogue g = new FadingDialogue(512, 480, "Kill all the enemies and capture the flag!!\n Move with AWSD and shoot with mouse1", 10, 10);
        addObject(g, 512, 480);
 
         BritNPC npc2 = new BritNPC(true);
         addHuman(npc2, 130, 400);
         BritNPC npc1 = new BritNPC(true);
         addHuman(npc1, 130, 100);
         
         WinstonCrowley move = new WinstonCrowley();
         addHuman(move, 95, 500);
         super.populate();
         numGermans = 5;
     }
     
     public int germansKilled() {
         int newNum = getObjects(German.class).size();
 
         if(numGermans - newNum > 0){
             germansKilled += numGermans - newNum;
         }
         numGermans = newNum;
         return germansKilled;
     }
     
     public void act() {
         super.act();
         Date d = new Date();
         if (dialogueTimer == 500)
         {
             removeObject(tutorialDia);
         }
         
         if (phase == 0){
            spawnG = germansKilled() < 5 && (d.getTime() - baseTimeG) > germSpawn; 
            spawnB = (d.getTime() - baseTimeB) > britSpawn;
            if(germansKilled == 4){
               phase++;
               spawnB = false;
               spawnG = false;
             }
         }
         
         if(phase == 1) {
             for(Human character : (ArrayList<Human>)getObjects(Human.class)){
                 if(!(character instanceof WinstonCrowley)&&!character.isDefender())
                     character.setRetreat(true);
             }
             FadingDialogue g = new FadingDialogue(512, 480, "Watch Out Machine Guns are coming!\n"
                                                     + " Take cover in your trench!\n"
                                                     + "(to take cover hold 'c' while in your trench)", 15, 15);
             addObject(g, 512, 480);
             phase++;
         }
         if(phase == 2) {
             if (phaseTimer / 100 == 1) {
                 phase++;
                 phaseTimer = 0;
             }
             else
                 phaseTimer++;
         }
         
         if(phase == 3) {
             gun1 = new MountedMachineGun(800,10,10);
             gun2 = new MountedMachineGun(800, 200,10);
             addHuman(gun1, 1000, 200);
             addHuman(gun2, 1000, 500);
             phase++;
         }
         
         if(phase == 4) {
            if(gun1.isPlanted() && gun2.isPlanted())
                 phase++;
         }
         
         if(phase == 5) {
             FadingDialogue g = new FadingDialogue(512, 480, "Keep your head down!", 10, 10);
             addObject(g, 512, 480);
             phase++;
         }
 
         if(phase == 6){
             if((phaseTimer / 500) == 1){
                 phase++;
                 phaseTimer =0;
             }
             else 
                 phaseTimer++;
         }
         
         if(phase == 7){
             boolean gun1Present = getObjects(MountedMachineGun.class).contains(gun1);
             boolean gun2Present = getObjects(MountedMachineGun.class).contains(gun2);
             if( gun1Present && gun1.getX() < 1000)
                 gun1.animateBack();
             if(gun2Present && gun2.getX() < 1000)
                 gun2.animateBack();
             if(!gun1Present && !gun2Present)
                 phase += 2;
             if((gun2Present && gun2.getX() == 1000) || (gun1Present  && gun1.getX() == 1000)){
                 phase++;
             }
         }
         
         if(phase == 8) {
             if(getObjects(MountedMachineGun.class).contains(gun1)){
                 removeObject(gun1.getHealthBar());
                 removeObject(gun1);
             }
             if(getObjects(MountedMachineGun.class).contains(gun2)){
                 removeObject(gun2.getHealthBar());
                 removeObject(gun2);
             }
             phase++;
         }
         
         if(phase == 9){
             FadingDialogue g = new FadingDialogue(512, 480, "Heads up! Mortars!\n" 
                                                 + "(Mortars will still hit you if you are under cover in the trench)", 10, 10);
             addObject(g, 512, 480);
             phase++;
         }
         
         if(phase == 10) {
             if(phaseTimer % 70 == 0){
                 Mortar mortar = new Mortar(125, (int)(Math.random() * 400 + 100));
                 addObject(mortar, 0,0);
             }
             if(phaseTimer / 70 == 5) {
                 phaseTimer = 0;
                 phase++;
             }
              phaseTimer++;
         }
         
         if(phase == 11) {
              
             Greenfoot.setWorld(new SecondLevel());
         }
         
         if(spawnB && bCounter == 0) {
             spawnWave(BRIT,britAmmount, true);
             spawnB = false;
             baseTimeB = d.getTime(); 
         }
         
         if (spawnG && gCounter == 0) {
             spawnWave(GERM, germAmmount, true);
             spawnG = false;
             baseTimeG = d.getTime(); 
         }
         
 
     }
     
 
 }
     
   
