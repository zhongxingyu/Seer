 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.awt.Color;
 import java.util.*;
 /**
  * Write a description of class InfamyWorld here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class TutorialInfamyWorld extends HumanWorld
 {
     public final int NUM_ADVANCERS = 3;
     public final String BRIT = "british"; 
     public final String GERM = "german"; 
     public final int BRIT_X = 200; 
     public final int GERM_X = 750; 
     public final int[] Y_SET = {135, 235, 335}; 
    
     public Dialogue dia;
     public Dialogue tutorialDiaIntro;
     public Dialogue tutorialDia;
     public int dialogueCounter;
     public int dialogueTimer;
     public ArrayList<Human> GermanAdvancers, BritAdvancers; 
    
     public long baseTimeG, baseTimeB;
     public boolean spawnG, spawnB; 
     public GreenfootSound bgMusic ;
     public boolean playmusic = true;
     
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
         AddTutorialDialogue("Kill all the enemies and capture the flag!!\n Move with AWSD as shoot with mouse1", 512, 525, false);
         //AddTutorialDialogue("Hey Winston!!\nCome over here!", 200, 300, false);
         //AddTutorialDialogue("Talk to your fellow soldier and\nother NPC's by pressing the 'e' key.\nMove Winston with the 'wasd' keys.", 400, 100, true);
         bCounter = gCounter = 0;
         populate();
         
         spawnWave(GERM, NUM_ADVANCERS, false); 
         spawnWave(BRIT, NUM_ADVANCERS, false); 
         spawnG = true;
         spawnB = true;
         
         //Greenfoot.playSound("FiveArmies.mp3");
         
     }
     
     public void spawnWave(String type, int ammount, boolean rand) {
       
        //int x  = Greenfoot.getRandomNumber(2);
        int x = 0;
         for (int i = 0; i < ammount; i++) {
             x = i;
             if (type.equals(BRIT)) {
                BritNPC adv = new BritNPC(false); 
                addHuman(adv, BRIT_X, Y_SET[x]);
                bCounter++; 
               
             }
             else {
                 EnemyNPC adv = new EnemyNPC(false);
                 addHuman(adv, GERM_X, Y_SET[x]);
                 gCounter++; 
             }
         }
         
     }
     
     public void toggleSound(boolean volumeOn) {
         if (volumeOn) {
            bgMusic.play(); 
         }
         else {
            bgMusic.pause(); 
         }
     }
     
     
     public void playMusic() {
         if (playmusic) {
            bgMusic = new GreenfootSound("FiveArmies.mp3"); 
            bgMusic.play();
            playmusic = false;
         }
         
     }
     
     public void act() {
         Date d  = new Date();
         playMusic(); 
         dialogueTimer++;
         if (dialogueTimer == 500)
         {
             removeObject(tutorialDia);
         }
         if ((d.getTime() - baseTimeB) > 12000 ) {
            spawnB = true;
         }
         if ((d.getTime() - baseTimeG) > 10000 ) {
            spawnG = true; 
         }
        
         System.out.println("spawn: " + spawnG + "gcounter " + gCounter);   
         if(spawnB && bCounter == 0) {
             spawnWave(BRIT, 1, true);
             spawnB = false;
             baseTimeB = d.getTime(); 
         }
         
         if (spawnG && gCounter == 0) {
             spawnWave(GERM, 2, true);
             spawnG = false;
             baseTimeG = d.getTime(); 
         }
     }
     
     public void populate() {
 
         
         Flag germanFlag = new Flag("german"); 
        addObject(germanFlag, 900, 245);
         
         Flag britishFlag = new Flag("British"); 
        addObject(britishFlag, 130, 245); 
         
 
         EnemyNPC germanDefender1 = new EnemyNPC(true);
         addHuman(germanDefender1, 900, 400); 
         
         EnemyNPC germanDefender2 = new EnemyNPC(true); 
         addHuman(germanDefender2, 900, 100); 
         
 
        CrossHair crosshair = new CrossHair();
        addObject(crosshair, 0, 0);
         
        Sandbag sb1 = new Sandbag();
        //sb1.turn(90);
        addObject(sb1, 600, 130);
        
        GasButton gb = new GasButton();
        addObject(gb, 512, 550);
 
        Sandbag sb2 = new Sandbag();
        //sb2.turn(90);
        addObject(sb2, 500, 400);
        Sandbag sb3 = new Sandbag();
        //sb3.turn(90);
        addObject(sb3, 300, 375);
        Sandbag sb4 = new Sandbag();
        //sb4.turn(90);
        addObject(sb4, 400, 190);
 
       VolumeButton vb = new VolumeButton();
       addObject(vb, 20, 40);
         
 
         BritNPC npc2 = new BritNPC(true);
         addHuman(npc2, 130, 400);
         BritNPC npc1 = new BritNPC(true);
         addHuman(npc1, 130, 100);
         
         WinstonCrowley move = new WinstonCrowley();
         addHuman(move, 95, 500);
     }
     
     public void RemoveDialogueBoxes()
     {
         removeObject(dia);
         dialogueCounter = 0;
     }
     
     
     public void AddDialogueBox(String message, int x, int y)
     {
         if (dialogueCounter == 0)
         {
             dia = new Dialogue();
             GreenfootImage textImage = new GreenfootImage(message, 24, Color.BLACK, Color.WHITE);
             textImage.drawString(message, x, y);
             
             GreenfootImage image = new GreenfootImage(textImage.getWidth()+12, textImage.getHeight()+12);  
             image.setColor(Color.RED);  
             image.fill();  
             image.setColor(Color.WHITE);  
             image.fillRect(3, 3, image.getWidth()-6, image.getHeight()-6);  
             image.drawImage(textImage, 6, 6);
             dia.setImage(image);
             addObject(dia, x, y);
             dialogueCounter = 1;
         }
         removeObject(tutorialDia);
         removeObject(tutorialDiaIntro);
     }
     
     public void TutorialWin()
     {
         //System.out.println("Winning");
         //Greenfoot.setWorld(new FirstLevel());
         //Greenfoot.setWorld(new WinWorld(Human.Score));
         Greenfoot.setWorld(new SecondLevel());
     }
     
     private void AddTutorialDialogue(String message, int x, int y, boolean intro)
     {
         if (intro)
         {
             tutorialDiaIntro = new Dialogue();
         }
         else
         {
             tutorialDia = new Dialogue();
         }
         GreenfootImage textImage = new GreenfootImage(message, 24, Color.BLACK, Color.WHITE);
         textImage.drawString(message, x, y);
         
         GreenfootImage image = new GreenfootImage(textImage.getWidth()+12, textImage.getHeight()+12);  
         image.setColor(Color.RED);  
         image.fill();  
         image.setColor(Color.WHITE);  
         image.fillRect(3, 3, image.getWidth()-6, image.getHeight()-6);  
         image.drawImage(textImage, 6, 6);
         
         if (intro)
         {
             tutorialDiaIntro.setImage(image);
             addObject(tutorialDiaIntro, x, y);
         }
         else
         {
             tutorialDia.setImage(image);
             addObject(tutorialDia, x, y);
         }
         
         
         dialogueCounter = 1;
     }
 }
