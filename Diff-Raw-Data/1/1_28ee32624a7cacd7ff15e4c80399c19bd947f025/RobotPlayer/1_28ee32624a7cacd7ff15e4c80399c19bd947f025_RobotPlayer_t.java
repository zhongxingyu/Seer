 package team039;
 
 import team039.building.BuildingPlayer;
 import team039.building.recycler.StartingBuildingPlayer;
 import team039.common.ComponentsHandler;
 import team039.common.Knowledge;
 import team039.common.SpecificPlayer;
import team039.common.MessageHandler;
 import team039.light.LightPlayer;
 import team039.light.StartingLightPlayer;
 import battlecode.common.*;
 
 public class RobotPlayer implements Runnable {
    
     private final  RobotController     myRC;
     
     /*** ComponentsHandler ***/
     private final  ComponentsHandler   compHandler;
     
     /*** Knowledge ***/
     private final  Knowledge           knowledge;
     
     /*** Specific Player ***/
     private        SpecificPlayer      specificPlayer;  
 
 
     private        MessageHandler      msgHandler;
 
     
     public RobotPlayer(RobotController rc) {
         myRC = rc;
         knowledge = new Knowledge(myRC);
         compHandler = new ComponentsHandler(myRC, knowledge);
         msgHandler = new MessageHandler(myRC, knowledge);
     }
     
     
 
     public void run() {
         
         // Order of these two should be determined by dependency.
         doCommonFirstRoundActions();
         doCommonActions();
         specificPlayer.doSpecificFirstRoundActions();
         specificPlayer.doSpecificActions();
         while(true) {
             try {
 
                 debug_setStrings();
                 myRC.yield();
                 
                 // Depending on new components, SpecificPlayer type might change!
                 doCommonActions();
                 specificPlayer.doSpecificActions();
             }
             catch(Exception e) {
                 System.out.println("Robot " + myRC.getRobot().getID() + 
                                    " during round " + Clock.getRoundNum() + 
                                    " caught exception:");
                 e.printStackTrace();
             }
         }
 
     }
     
     
 
     public void doCommonFirstRoundActions() {
         debug_printGameConstants();
         specificPlayer = determineSpecificPlayer();
     }
     
     
     
     public void doCommonActions() {
         knowledge.update();
         ComponentType[] newCompTypes = compHandler.updateComponents();
         if(newCompTypes != null) {
             for(ComponentType newCompType : newCompTypes) {
                 specificPlayer = specificPlayer.determineSpecificPlayer(newCompType);
             }
         }
     }
 
     
     
     public SpecificPlayer determineSpecificPlayer() {
         
         // Use of BuildingPlayer below is arbitrary - it's a place holder
         SpecificPlayer result = new BuildingPlayer(myRC, knowledge, compHandler);
         
         // First buildings are special
         if(knowledge.roundNum == 0) {
             
             switch(myRC.getChassis()) {
             
             case BUILDING:
                 result = new StartingBuildingPlayer(myRC, knowledge, compHandler);
                 break;
                 
             case LIGHT:
                 result = new StartingLightPlayer(myRC, knowledge, compHandler);
             }
         }
         
         else {
             switch(myRC.getChassis()) {
             case BUILDING:
                 result = new BuildingPlayer(myRC, knowledge, compHandler);
                 break;
     
             case LIGHT:
                 result = new LightPlayer(myRC, knowledge, compHandler);
                 break;
             }
             //TODO: add other Chassis types!
         }
         return result;
     }
     
     
 
     /**
      * Indicator string use should be contained within this method.
      */
     public void debug_setStrings() { 
         myRC.setIndicatorString(0, knowledge.myLocation.toString());
         myRC.setIndicatorString(1, String.valueOf(knowledge.deltaFlux));
     }
     
     public static void debug_printGameConstants() {
         System.out.println("MAP_MAX_WIDTH: " + String.valueOf(GameConstants.MAP_MAX_WIDTH));
     }
 }
