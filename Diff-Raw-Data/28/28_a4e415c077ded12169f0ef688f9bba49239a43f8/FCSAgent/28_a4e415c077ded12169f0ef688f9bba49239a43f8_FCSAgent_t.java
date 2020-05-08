 package factory;
 
 import java.util.ArrayList;
 import agent.Agent;
 import java.util.*;
 import factory.interfaces.*;
 
 //test commit/push
 public class FCSAgent {
 	 ArrayList<MyKitConfig> myKitConfigs = new ArrayList<MyKitConfig>();
 	   PartsRobot partsRobot;
 	   Gantry gantry;
 	   BinConfig binConfig;
 	   Boolean passBinConfigurationToGantry = false;
 	   
 	   enum MyKitConfigState { PENDING, PRODUCING }
 
 	   class MyKitConfig {
 	      
 	      KitConfig kitConfig;
 	      MyKitConfigState state;
 
 	      public MyKitConfig(KitConfig kitConfig){
 	            this.state = MyKitConfigState.PENDING;
 	            this.kitConfig = kitConfig;
 	      }
 	   }
 	   
 	// *** MESSAGES ***
 	   
 	   public void msgInitialize(BinConfig binConfig) {
 		   this.binConfig = binConfig;
 		   this.passBinConfigurationToGantry = true;
 		}
 	   
 	   public void msgProduceKit(KitConfig kitConfig) {
 		   myKitConfigs.add(new MyKitConfig(kitConfig));
 		}
 	   
 	   
 	// *** SCHEDULER ***
 	   public boolean pickAndExecuteAnAction() {
 		   if (this.passBinConfigurationToGantry == true){
 			   changeGantryBinConfig();
			   return true;
 		   }
 		   for(MyKitConfig mkc: myKitConfigs){
 			   if (mkc.state == MyKitConfigState.PENDING){
 				   sendKitConfigToPartRobot(mkc);
				   return true;
 			   }
 		   }
		   
		   return false;
 	   }
 
 	// *** ACTIONS ***
 	   /**
 	    * Passes down the new configuration to the Gantry
 	    */
 	   private void changeGantryBinConfig(){
 	      gantry.msgChangeGantryBinConfig(this.binConfig);
 	      this.passBinConfigurationToGantry = false;
 	   }
 
 	   /**
 	    * Passes down the new Kit Configuration to the PartsRobot Agent
 	    */
 	   private void sendKitConfigToPartRobot(MyKitConfig mkc) { 
 	      this.partsRobot.msgMakeKit(mkc.kitConfig);
 	      mkc.state = MyKitConfigState.PRODUCING;
 	   }
 	   
 }
