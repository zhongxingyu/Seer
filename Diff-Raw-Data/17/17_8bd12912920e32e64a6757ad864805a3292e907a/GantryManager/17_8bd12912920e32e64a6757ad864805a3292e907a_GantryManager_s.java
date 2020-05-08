 //Ben Mayeux
 package factory.managers;
 
 import java.awt.BorderLayout;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import factory.KitConfig;
 import factory.Part;
 import factory.graphics.*;
 import factory.client.Client;
 import factory.swing.FactoryProdManPanel;
 import factory.swing.GantryManPanel;
 
 public class GantryManager extends Client {
 	static final long serialVersionUID = 8492299864169935860L;
 	
 	GantryManPanel buttons;
 	GantryRobotPanel animation;
 	
 	public GantryManager() {
 		super(Client.Type.gm, null, null);
 		
 		buttons = new GantryManPanel();
 		buttons.setManager(this);
 		animation = new GantryRobotPanel(this); //TODO does not currently work but will by 11/13 -->Tobi
 		
 		setInterface();
 	}
 	
 	public static void main(String[] args){
 		GantryManager k = new GantryManager();
 	}
 
 	public void setInterface() {
 		graphics = animation;
 		UI = buttons;
 
 		add(graphics, BorderLayout.CENTER);
 		
 		//add(UI, BorderLayout.LINE_END);
 		pack();
 		setVisible(true);
 	}
 
 	public void doCommand(ArrayList<String> pCmd) {
 		int size = pCmd.size();
 		//parameters lay between i = 2 and i = size - 2
 		String action = pCmd.get(0);
 		String identifier = pCmd.get(1);
 		System.out.println("Got command");
 		System.out.println(action);
 		System.out.println(identifier);
 		if(action.equals("cmd")){
 			//Graphics Receive Commands
 			
 		
 			// Commands from FeederAgent
 			if (identifier.equals("startfeeding"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
				((FactoryProductionPanel) graphics).feedFeeder(feederSlot);
 			}
 			else if (identifier.equals("stopfeeding"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
				((FactoryProductionPanel) graphics).turnFeederOff(feederSlot);
 			} 
 			else if (identifier.equals("purgefeeder"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
				((FactoryProductionPanel) graphics).purgeFeeder(feederSlot);
 			}
 			else if (identifier.equals("switchlane"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
				((FactoryProductionPanel) graphics).switchFeederLane(feederSlot);
 			}
 			else if (identifier.equals("purgetoplane"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
				((FactoryProductionPanel) graphics).purgeTopLane(feederSlot);
 			}
 			else if (identifier.equals("purgebottomlane"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
				((FactoryProductionPanel) graphics).purgeBottomLane(feederSlot);
 			}
 			
 			// Commands from GantryAgent:
 			else if (identifier.equals("pickuppurgebin"))
 			{
 				int feederNumber = Integer.valueOf(pCmd.get(2)); 
 				((GantryRobotPanel) graphics).moveGantryRobotToFeederForPickup(feederNumber);
 			}
 			else if (identifier.equals("getnewbin"))
 			{
 				String desiredPartName = pCmd.get(2); 
 				((GantryRobotPanel) graphics).moveGantryRobotToPickup(desiredPartName);
 			}
 			else if (identifier.equals("bringbin"))
 			{
 				int feederNumber = Integer.valueOf(pCmd.get(2)); 
 				((GantryRobotPanel) graphics).moveGantryRobotToFeederForDropoff(feederNumber);
 			}
 			
 			// Commands from KitRobotAgent
 			else if (identifier.equals("putinspectionkitonconveyor")) {
 				((GantryRobotPanel) graphics).moveKitFromInspectionToConveyor();
 			}
 			else if (identifier.equals("putemptykitatslot")) {
 				if (pCmd.get(2).equals("topslot")) {
 					((GantryRobotPanel) graphics).moveEmptyKitToSlot(0);
 				} else if (pCmd.get(2).equals("bottomslot")) {
 					((GantryRobotPanel) graphics).moveEmptyKitToSlot(1);
 				}
 			}
 			else if (identifier.equals("movekittoinspectionslot")) {
 				if (pCmd.get(2).equals("topslot")) {
 					((GantryRobotPanel) graphics).moveKitToInspection(0);
 				} else if (pCmd.get(2).equals("bottomslot")) {
 					((GantryRobotPanel) graphics).moveKitToInspection(1);
 				}
 			}
 			else if (identifier.equals("dumpkitatslot")) {
 				if (pCmd.get(2).equals("topslot")) {
 					((GantryRobotPanel) graphics).dumpKitAtSlot(0);
 				} else if (pCmd.get(2).equals("bottomslot")) {
 					((GantryRobotPanel) graphics).dumpKitAtSlot(1);
 				} else if (pCmd.get(2).equals("inspectionslot")) {
 					((GantryRobotPanel) graphics).dumpKitAtInspection();
 				}
 			}
 
 			// Commands from ConveyorAgent
 			else if (identifier.equals("exportkitfromcell")) {
 				((GantryRobotPanel) graphics).exportKit();
 			}
 			
 			// Commands from ConveyorControllerAgent
 			else if (identifier.equals("emptykitenterscell")) {
 				((GantryRobotPanel) graphics).newEmptyKit();
 			}
 
 			
 			//Swing Receive Commands
 			
 		}
 		
 		else if(action.equals("req")){
 		}
 		
 		else if(action.equals("get")){
 		}
 		
 		else if(action.equals("set")){
 		}
 		
 		else if(action.equals("cnf")){
 		}
 		
 	   else if(action.equals("err")){
 			String error;
 			error = new String();
 			for(int i = 1; i<this.parsedCommand.size(); i++)
 				error.concat(parsedCommand.get(i));
 			System.out.println(error);
 	   }
 	   else if(action.equals("mcs")){
 		   try {
 			this.server.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		   System.exit(0);
 	   }
 		else 
    		   System.out.println("Stuff is FU with the server...\n(string does not contain a command type)");
 	}
 }
