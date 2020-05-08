 /*
 ** Author: Nikhil Handyal
 ** Date: 10/31/12
 ** Project: Cs200-Factory
 ** Description: Server code
 ** 
 ** Pre-Conditions: None
 */
 package factory.server;
 
 // Java packages
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.Timer;
 import java.util.TreeMap;
 import java.util.ArrayList;
 
 
 // User packages
 import factory.global.network.*;
 import factory.global.data.*;
 import factory.server.managers.GuiManager;
 import factory.server.managers.gantryManager.*;
 import factory.server.managers.laneManager.*;
 import factory.server.managers.kitAssemblyManager.*;
 import factory.server.managers.factoryState.*;
 
 /* Client Indeces
 Parts manager will be located at clientConnections index 0 etc.
 -------------------------------------------------------------------
  * 0 -- Parts
  * 1 -- Kit
  * 2 -- Gantry
  * 3 -- Lane
  * 4 -- Kit Assembly
  * 5 -- Factory
 -------------------------------------------------------------------
 */
 
 public class Server extends JFrame implements ActionListener, NetworkManager{
 		NetworkBridge[] clientConnections = new NetworkBridge[6];
 		GuiManager[] guiViews = new GuiManager[4];
 		ServerControl SCP;
 		FactoryState fs;
 		InboundConnectionManager icm = null;
 		ArrayList<TreeMap<Integer, Boolean>> changeMap;
 		ArrayList<TreeMap<Integer, FactoryObject>> changeData;
 		Timer t;
 		boolean sync, startAnimation;
 				
 		Server(){
 				// initialize all class instance variables
 				fs = new FactoryState();
 				icm = new InboundConnectionManager(this);
				guiViews[0] = new GantryManager();																					// Gantry
 				guiViews[1] = new LaneManager();																						// Lane
 				guiViews[2] = new UpdateServer();																						// Kit Asm 
 				changeMap = new ArrayList<TreeMap<Integer, Boolean>>(3);
 				changeData = new ArrayList<TreeMap<Integer, FactoryObject>>(3);
 				sync = false;
 				startAnimation = false;
 				
 				// initialize server control panel
 				SCP = new ServerControl(guiViews[2], guiViews[1],fs);
 				this.add(SCP);	
 				// initialize timer
 				t = new Timer(50,this);
 				
 				// start threads
 				icm.start();
 				t.start();
 		}
 		
 		public static void main(String[] args){
 				Server si = new Server();
 				si.setSize(755,670);
 				si.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				si.setTitle("Server Controls");
 				si.setVisible(true);
 		}
 		
 		// -------------------------------------------------------------------------------------- //
 		// ----------------------------------- Action Performed --------------------------------- //
 		// -------------------------------------------------------------------------------------- //
 		
 		public void actionPerformed(ActionEvent ae){	
 				if(sync){
 						masterSync();
 						startAnimation = true;
 						sync = false;
 				}
 				else if(startAnimation){
 						masterUpdate();
 				}
 		}
 		
 		// -------------------------------------------------------------------------------------- //
 		// ----------------------------------- Network Manager ---------------------------------- //
 		// -------------------------------------------------------------------------------------- //
 		
 		// Server Specific
 		public void registerClientListener(NetworkBridge newBridge, int cID){
 				clientConnections[cID] = newBridge;
 		}
 		
 		// function to update part data
 		public void updatePartData(TreeMap<Integer, Parts> partData){
 				fs.mergeParts(partData);
 				
 				if(clientConnections[1] != null){
 						// send update to kit manager
 						Instruction instr = new Instruction("UPD");
 						clientConnections[1].writeData(instr);
 						clientConnections[1].writeData(fs.getParts());
 				}
 		}
 		
 		public void updateKitData(ArrayList<Kits> kitData){
 				fs.mergeKits(kitData);
 		}
 		
 		// function to send the entire frame data to the client
 		public void syncFrame(){
 				sync = true;
 		}
 
 		// Client Specific
 		public void mergeChanges(ArrayList<TreeMap<Integer, Boolean>> mapArray, ArrayList<TreeMap<Integer, FactoryObject>> dataArray){};
 		public void syncChanges(ArrayList<TreeMap<Integer, FactoryObject>> dataArray){}
 		
 		// Global
 		public void closeNetworkBridge(int bridgeID){
 				NetworkBridge nb = clientConnections[bridgeID];
 				nb.close();
 				clientConnections[bridgeID] = null;
 		}
 		
 		// -------------------------------------------------------------------------------------- //
 		// ----------------------------------- Server Functions --------------------------------- //
 		// -------------------------------------------------------------------------------------- //
 		
 		private void masterSync(){
 				ArrayList<TreeMap<Integer, FactoryObject>> changeData = new ArrayList<TreeMap<Integer, FactoryObject>>();
 				TreeMap<Integer, Boolean> changeMap = null;
 				Instruction instr = new Instruction("SAD",1);
 				Instruction instrFM = new Instruction("SAD",3);
 				
 				// initialize changeData with empty TreeMaps
 				for(int i = 0; i < 3; i++){
 						TreeMap<Integer, FactoryObject> tempChangeData = new TreeMap<Integer, FactoryObject>();
 						changeData.add(tempChangeData);
 				}
 				
 				// call sync on all GuiManagers
 				guiViews[0].sync(changeData.get(0));
 				guiViews[1].sync(changeData.get(1));
 				guiViews[2].sync(changeData.get(2));
 				
 				// build NetworkTransferObjects for all the managers
 				NetworkTransferObject gantryData = new NetworkTransferObject(changeMap, changeData.get(0));
 				NetworkTransferObject laneData = new NetworkTransferObject(changeMap, changeData.get(1));
 				NetworkTransferObject kitAsmData = new NetworkTransferObject(changeMap, changeData.get(2));
 				
 				
 				// send data to the appropriate clients prefaced by a sync animation data instruction
 				for(int i = 2; i <= 5; i++){
 						// make sure client has connected and registered with the server before sending data
 						if(clientConnections[i] != null){
 								switch(i){
 										case 2:
 												clientConnections[2].writeData(instr);
 												clientConnections[2].writeData(gantryData);
 												break;
 										case 3:
 												clientConnections[3].writeData(instr);
 												clientConnections[3].writeData(laneData);
 												break;
 										case 4:
 												clientConnections[4].writeData(instr);
 												clientConnections[4].writeData(kitAsmData);
 												break;
 										case 5:
 												clientConnections[5].writeData(instrFM);
 												clientConnections[5].writeData(kitAsmData);
 												clientConnections[5].writeData(laneData);
 												clientConnections[5].writeData(gantryData);
 												break;
 								}
 						}
 				}
 		}
 		
 		private void masterUpdate(){
 				/*
 				 * run updates on guiViews[0] - guiViews[2]
 				 * guiViews[3] == factoryManage; just needs a superset of the other 3 updates
 				 * changeMap and changeData use the same indexing
 				 * 0 - Gantry
 				 * 1 - Lane
 				 * 2 - Kit ASM
 				*/
 				
 				// clear changeMap and changeData of previous data
 				changeMap.clear();
 				changeData.clear();
 				
 				// initialize changeMap and changeData with empty TreeMaps
 				for(int i = 0; i < 3; i++){
 						TreeMap<Integer, Boolean> tempChangeMap = new TreeMap<Integer,Boolean>();
 						TreeMap<Integer, FactoryObject> tempChangeData = new TreeMap<Integer, FactoryObject>();
 						changeMap.add(tempChangeMap);
 						changeData.add(tempChangeData);
 				}
 				
 				// get update data for Gantry Manager
 				guiViews[0].update(changeMap.get(0), changeData.get(0));
 				
 				// get update data for Lane Manager
 				guiViews[1].update(changeMap.get(1), changeData.get(1));
 				
 				// get update data for Kit Asm Manager
 				guiViews[2].update(changeMap.get(2), changeData.get(2));
 				
 				// at this point we have all of the updated factory animation data. We now need to send this to the client
 				// we need to create NetworkTransferObjects with the appropriate changeMap and changeData trees for the 3 managers
 				// we will send all three NTO's to the fm so that it has all relevant data to paint
 				
 				NetworkTransferObject gantryData = new NetworkTransferObject(changeMap.get(0), changeData.get(0));
 				NetworkTransferObject laneData = new NetworkTransferObject(changeMap.get(1), changeData.get(1));
 				NetworkTransferObject kitAsmData = new NetworkTransferObject(changeMap.get(2), changeData.get(2));
 				
 				
 				// now we can send all of the data to the appropriate clients prefaced by an update animation data instruction. FM will expect 3 NTO objects on the input stream
 				Instruction instr = new Instruction("UAD",1);
 				Instruction instrFM = new Instruction("UAD",3);
 				
 				/*
 				 * send the update animation data instruction along with the appropriate NTO to all animation managers
 				 * 2 -- Gantry
 				 * 3 -- Lane
 				 * 4 -- Kit ASM
 				 * 5 -- Factory
 				*/
 				for(int i = 2; i <= 5; i++){
 						// make sure client has connected and registered with the server before sending data
 						if(clientConnections[i] != null){
 								switch(i){
 										case 2:
 												clientConnections[2].writeData(instr);
 												clientConnections[2].writeData(gantryData);
 												break;
 										case 3:
 												clientConnections[3].writeData(instr);
 												clientConnections[3].writeData(laneData);
 												break;
 										case 4:
 												clientConnections[4].writeData(instr);
 												clientConnections[4].writeData(kitAsmData);
 												break;
 										case 5:
 												clientConnections[5].writeData(instrFM);
 												clientConnections[5].writeData(kitAsmData);
 												clientConnections[5].writeData(laneData);
 												clientConnections[5].writeData(gantryData);
 												break;
 								}
 						}
 				}
 		}
 }
