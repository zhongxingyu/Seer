 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package utilities;
 
 import entities.*;
import org.newdawn.slick.state.StateBasedGame;
 import states.GameplayState;
 
 /**
  *
  * @author root
  */
 public class World {
 
     Level[] levels;
     Level currentLevel;
     float initialX, initialY;
     PlayerInteraction playerInteraction;
     NPCInteraction npcInteraction;
     String initialLevelID;
     NPCAI npcAI;
     long delta;
     //and NPC interaction!
 
     public World(String initialLevelID, float initialX, float initialY, Level[] levels){
         this.initialLevelID = initialLevelID;
         this.initialX = initialX;
         this.initialY = initialY;
         this.levels = levels;
     }
 
     public void ChangeCurrentLevel(){
         NextLevelInformation nextLevelInfo = playerInteraction.GetNextLevelInfo();
 
         for (Level level: levels){
             if(nextLevelInfo.GetLevelID().equals(level.GetLevelID())){
                 currentLevel = level;
                 break;
             }
         }
 
         currentLevel.player.SetNewCoordinates(playerInteraction.GetNextLevelInfo().GetStartX(),
                 playerInteraction.GetNextLevelInfo().GetStartY());
 
         RenderCurrentLevel(/*nextLevelInfo.GetStartX(), nextLevelInfo.GetStartY()*/);
         playerInteraction = new PlayerInteraction(currentLevel);
         npcAI = new NPCAI(currentLevel);
         npcInteraction = new NPCInteraction(currentLevel, npcAI);
     }
 
     public void RenderCurrentLevel(){
         currentLevel.Render();
     }
 
     public Level GetCurrentLevel(){
         return currentLevel;
     }
 
     public Enemy[] GetEnemyArray(){
         return currentLevel.enemies;
     }
 
    public void UpdatePlayer(int key, long delta, StateBasedGame sbg){
         this.delta = delta;
         playerInteraction.HandleEvents(key, delta);
         if( playerInteraction.GetSwitchLevel() ){
             ChangeCurrentLevel();
         }
        if(playerInteraction.GetGameOver()){
            sbg.enterState(2);
        }
     }
 
     public void UpdateWorld(long delta, NPCAI npcAI){
         this.delta = delta;
         this.npcAI = npcAI;
         npcInteraction.HandleEvents(delta);
     }
 
     public void InitializeFirstLevel(){
         NextLevelInformation initialLevelInfo = new NextLevelInformation(initialLevelID, initialX, initialY);
         currentLevel = levels[0];
         RenderCurrentLevel(/*initialLevelInfo.GetStartX(), initialLevelInfo.GetStartY()*/);
         playerInteraction = new PlayerInteraction(currentLevel);
         npcInteraction = new NPCInteraction(currentLevel, npcAI);
     }
 
 }
