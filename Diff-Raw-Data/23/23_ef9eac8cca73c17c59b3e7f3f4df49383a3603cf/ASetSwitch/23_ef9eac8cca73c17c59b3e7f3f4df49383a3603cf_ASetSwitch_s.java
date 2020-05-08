 package npcs.actions;
 
 import fap_java.NPC;
 import fap_java.Tools;
 
 import gui.GameSave;
 
 import java.util.ArrayList;
 
 public class ASetSwitch implements Action {
     private int switchID;
     private boolean newValue;
     
     public ASetSwitch(int switchID, boolean newValue) {
         super();
         this.switchID = switchID;
         this.newValue = newValue;
     }
 
     public void execute(NPC whoLaunches) {
         //Load datas
         GameSave gameSave = Tools.loadGame();
         //Set datas
         ArrayList<Boolean> gameSwitches = gameSave.getGameSwitches();
         gameSwitches.set(switchID, newValue);
         //Save datas
         Tools.saveGame(gameSave);
         //Loop
        if(whoLaunches != null && whoLaunches.getIterator() < whoLaunches.getActions().size()){
             whoLaunches.execute();
         }
     }
 }
