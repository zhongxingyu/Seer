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
 import java.util.TreeMap;
 import java.util.ArrayList;
 import java.awt.event.*;
 import javax.swing.Timer;
 
 // User packages
 import factory.global.network.*;
 import factory.global.data.*;
 import factory.server.managers.GuiManager;
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
 
 public class Server implements ActionListener, NetworkManager{
 		NetworkBridge[] clientConnections = new NetworkBridge[6];
 		GuiManager[] guiViews = new GuiManager[4];
 		FactoryState fs;
 		InboundConnectionManager icm = null;
 		ArrayList<TreeMap<Integer, Boolean>> changeMap;
 		ArrayList<TreeMap<Integer, FactoryObject>> changeData;
 				
 		Server(){
 				// initialize all class instance variables
 				fs = new FactoryState();
 				icm = new InboundConnectionManager(this);
 				guiViews[1] = new LaneManager();																						// Lane
 				guiViews[2] = new UpdateServer();																						// Kit Asm 
 				changeMap = new ArrayList<TreeMap<Integer, Boolean>>(3);
 				changeData = new ArrayList<TreeMap<Integer, FactoryObject>>(3);
 				
 				// start threads
 				icm.start();
 		}
 		
 		public static void main(String[] args){
 				Server si = new Server();
 		}
 		
 		// -------------------------------------------------------------------------------------- //
 		// ----------------------------------- Action Performed --------------------------------- //
 		// -------------------------------------------------------------------------------------- //
 		
 		public void actionPerformed(ActionEvent ae){
 				masterUpdate();
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
 		}
 		
 		// function to send the entire frame data to the client
 		public void syncFrame(int cID){
 				TreeMap<Integer, FactoryObject> changeData = new TreeMap<Integer, FactoryObject>();
 				Instruction instr = new Instruction("SAD",1);
 				switch(cID){
 						case 2:
 								guiViews[0].sync(changeData);
 								clientConnections[cID].writeData(instr);
 								clientConnections[cID].writeData(changeData);
 								break;
 						case 3:
 								guiViews[1].sync(changeData);
 								clientConnections[cID].writeData(instr);
 								clientConnections[cID].writeData(changeData);
 								break;
 						case 4:
 								guiViews[2].sync(changeData);
 								System.out.println("Server sync -- after update sync called: "+changeData.size());
 								clientConnections[cID].writeData(instr);
								clientConnections[cID].writeData(changeData);
 								break;
 						case 5:
 								instr.setX(3);
 								clientConnections[cID].writeData(instr);
 								for(int i = 0; i < 3; i++){
 										guiViews[i].sync(changeData);		
 										clientConnections[cID].writeData(changeData);
 										changeData.clear();
 								}
 								break;
 				}
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
 				//guiViews[0].update(changeMap.get(0), changeData.get(0));
 				
 				// get update data for Lane Manager
 				guiViews[1].update(changeMap.get(1), changeData.get(1));
 				
 				// get update data for Kit Asm Manager
 				guiViews[2].update(changeMap.get(2), changeData.get(2));
 				
 				// at this point we have all of the updated factory animation data. We now need to send this to the client
 				// we need to create NetworkTransferObjects with the appropriate changeMap and changeData trees for the 3 managers
 				// we will send all three NTO's to the fm so that it has all relevant data to paint
 				
 				//NetworkTransferObject gantryData = new NetworkTransferObject(changeMap.get(0), changeData.get(0));
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
 												//clientConnections[2].writeData(instr);
 												//clientConnections[2].writeData(gantryData);
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
 												//clientConnections[5].writeData(instrFM);
 												//clientConnections[5].writeData(gantryData);
 												//clientConnections[5].writeData(laneData);
 												//clientConnections[5].writeData(kitAsmData);
 												break;
 								}
 						}
 				}
 		}
 }
