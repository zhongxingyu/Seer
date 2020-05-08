 package npcs.actions;
 
 import fap_java.NPC;
 import fap_java.Tools;
 
 import gui.GameSave;
 
 import java.util.ArrayList;
 
 public class ATestSwitch implements Action {
     private int switchID;
     private Action failAction;
     public ATestSwitch(int switchID, Action failAction) {
         super();
         this.switchID = switchID;
         this.failAction = failAction;
     }
 
     public void execute(NPC whoLaunches) {
         //Load datas
         GameSave gameSave = Tools.loadGame();
         //Get datas
         ArrayList<Boolean> gameSwitches = gameSave.getGameSwitches();
         boolean value = gameSwitches.get(switchID);
         if(value){
             
         }
         else{
             //End NPC
             whoLaunches.setIterator(whoLaunches.getActions().size());
             failAction.execute(whoLaunches);
         }
         //Loop
        if(whoLaunches != null && whoLaunches.getIterator() < whoLaunches.getActions().size()){
             whoLaunches.execute();
         }
     }
 
     public void setSwitchID(int switchID) {
         this.switchID = switchID;
     }
 
     public int getSwitchID() {
         return switchID;
     }
 }
