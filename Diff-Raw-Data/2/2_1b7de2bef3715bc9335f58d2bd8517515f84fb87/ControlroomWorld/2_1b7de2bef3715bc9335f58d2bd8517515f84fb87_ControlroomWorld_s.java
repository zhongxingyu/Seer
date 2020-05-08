 import greenfoot.*;
 
 /**
  * Dit is de kamer waarin allerlei calamiteiten ontstaan en vervolgens 
  * geïnterveniëerd moeten worden door de speler.
  * 
  * Project 42 
  * 0.01
  */
 
 public class ControlroomWorld extends World
 {
     public Score scoreCounter;
     private int spawnTimer = 0;  
     private int spawnLocationX = 0;
     private int spawnLocationY = 0;
     public enum Character {
         FIREFIGHTER,
         POLICE_SHUTOFF,
         POLICE_CATCHTHIEF,
         POLICE_EVACUATE,
     }
     
     Character selectedCharacter;
     
     public void setSelectedCharacter(Character character) {
         selectedCharacter = character;
     }
     
     public Character getSelectedCharacter() {
         return selectedCharacter;
     }
 
     public ControlroomWorld()
     {    
         super(80,80,10); 
         setBackground("background.png");
         addObject(new MenuBar(), 39, 75);
         addObject(new Fire(), getWidth()/2, getHeight()/2);
         addObject(new Extinguish(), getWidth()/2+30, getHeight()/2);
         addObject(new Victims(), getWidth()/2+15, getHeight()/2);
 
         prepare();
         scoreCounter = new Score("Score: ");
         addObject(scoreCounter, 6, 74);
     }
     
     private void prepare()
     {
     }
     
     public Score getScoreCounter() {
         return scoreCounter;
     }
     
     public void act() {
         spawnTimer++;
         spawnSomewhere(100);
     }
     
     public void spawnLocation() {
         spawnLocationX = (int)(Math.random()*((64-0)+12));
         spawnLocationY = (int)(Math.random()*((31-0)+12));
         
        if (getObjectsAt(spawnLocationX,spawnLocationY,Calamities.class)==null) 
             {
                 if(spawnLocationX > 3 && spawnLocationY > 5) 
                 {
                         chooseObject((int)(Math.random()*((2-0)+1)));
                 }
              }
         }
         
     public void chooseObject (int x) {
         if (x==1) 
         {
             addObject(new Fire(), spawnLocationX, spawnLocationY);
         }
         
         else if (x==2) 
         {
             addObject(new Victims(),spawnLocationX, spawnLocationY);
         }
         setSpawnTimer(0);
     }
     
     public void setSpawnTimer(int newSpawnTimer) {
         spawnTimer = newSpawnTimer;
     }
     
     public int getSpawnTimer() {
        return spawnTimer;
     }
         
     public void spawnSomewhere(int difficulty) {
         if (getSpawnTimer()>difficulty) 
         {
             spawnLocation();
         }
     }
 
 }
