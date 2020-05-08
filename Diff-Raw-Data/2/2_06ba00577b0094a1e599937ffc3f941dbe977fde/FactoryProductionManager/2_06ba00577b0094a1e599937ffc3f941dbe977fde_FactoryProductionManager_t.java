 //Contributors: Ben Mayeux,Stephanie Reagle, Joey Huang, Tobias Lee, Ryan Cleary
 //CS 200
 
 // Last edited: 11/18/12 4:51pm by Joey Huang
 
 /* This program is the Factory Production Manager which contains (1) a user interface that allows
  * the user to submit orders to the factory (kit name and quantity),view the production schedule
  *(showing all the submitted orders), and stop the factory, and (2) a graphics panel that shows a
  *full view of the entire factory in real time. This manager handles communication with the server.
  */
 package factory.managers;
 
 import java.awt.BorderLayout;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import factory.client.Client;
 import factory.graphics.FactoryProductionPanel;
 import factory.graphics.GraphicBin;
 import factory.graphics.GraphicItem;
 import factory.graphics.GraphicPanel;
 import factory.swing.FactoryProdManPanel;
 import factory.Part;
 import factory.KitConfig;
 import java.util.Iterator;
 import java.util.Map;
 
 public class FactoryProductionManager extends Client {
 	static final long serialVersionUID = -2074747328301562732L;
 	HashMap<String,Part> partsList; // contains list of parts in system
 	HashMap<String,KitConfig> kitConfigList; // contains list of kit configurations in system
 
 	FactoryProdManPanel buttons;
 	FactoryProductionPanel animation;
 
 	public FactoryProductionManager() {
 		super(Client.Type.fpm, null, null);
 
 		buttons = new FactoryProdManPanel(this);
 		animation = new FactoryProductionPanel(this);
 
 		setInterface();
 
 		partsList = new HashMap<String,Part>(); //Local version
 		kitConfigList = new HashMap<String,KitConfig>(); //Local version
 
 		loadData();
 		populatePanelList();
 	}
 	public static void main(String[] args){
 		FactoryProductionManager f = new FactoryProductionManager();
 	}
 
 	public void setInterface() {
 		graphics = animation;
 		UI = buttons;
 
 		add(graphics, BorderLayout.CENTER);
 
 		add(UI, BorderLayout.LINE_END);
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
 				((FactoryProductionPanel) graphics).moveGantryRobotToFeederForPickup(feederNumber);
 			}
 			else if (identifier.equals("getnewbin"))
 			{
 				String desiredPartName = pCmd.get(2); 
 				((FactoryProductionPanel) graphics).moveGantryRobotToPickup(desiredPartName);
 			}
 			else if (identifier.equals("bringbin"))
 			{
 				int feederNumber = Integer.valueOf(pCmd.get(2)); 
 				((FactoryProductionPanel) graphics).moveGantryRobotToFeederForDropoff(feederNumber);
 			}
 
 
 			// Commands from PartsRobotAgent
 			else if (identifier.equals("putpartinkit"))
 			{
 				int itemIndex = Integer.valueOf(pCmd.get(2));
 				((FactoryProductionPanel) graphics).partsRobotPopItemToCurrentKit(itemIndex);
 			}
 			else if (identifier.equals("movetostand"))
 			{
 				int kitIndex = Integer.valueOf(pCmd.get(2));
 				((FactoryProductionPanel) graphics).movePartsRobotToStation(kitIndex); //not sure if this is the right method
 			}
 			else if (identifier.equals("droppartsrobotsitems"))
 			{
 				((FactoryProductionPanel) graphics).dropPartsRobotsItems();
 			}
 			else if (identifier.equals("movetonest"))
 			{
 				int nestIndex = Integer.valueOf(pCmd.get(2));
 				int itemIndex = Integer.valueOf(pCmd.get(3));
 				((FactoryProductionPanel) graphics).movePartsRobotToNest(nestIndex, itemIndex);
 			}
 			else if (identifier.equals("movetocenter"))
 			{
 				((FactoryProductionPanel) graphics).movePartsRobotToCenter();
 			}
 
 			// End Commands from PartsRobotAgent
 
 			// Commands from KitRobotAgent
 			else if (identifier.equals("putinspectionkitonconveyor")) {
 				((FactoryProductionPanel) graphics).moveKitFromInspectionToConveyor();
 			}
 			else if (identifier.equals("putemptykitatslot")) {
 				if (pCmd.get(2).equals("topSlot")) {
 					((FactoryProductionPanel) graphics).moveEmptyKitToSlot(0);
 				} else if (pCmd.get(2).equals("bottomSlot")) {
 					((FactoryProductionPanel) graphics).moveEmptyKitToSlot(1);
 				}
 			}
 			else if (identifier.equals("movekittoinspectionslot")) {
 				if (pCmd.get(2).equals("topSlot")) {
 					((FactoryProductionPanel) graphics).moveKitToInspection(0);
 				} else if (pCmd.get(2).equals("bottomSlot")) {
 					((FactoryProductionPanel) graphics).moveKitToInspection(1);
 				}
 			}
 			else if (identifier.equals("dumpkitatslot")) {
 				if (pCmd.get(2).equals("topSlot")) {
 					((FactoryProductionPanel) graphics).dumpKitAtSlot(0);
 				} else if (pCmd.get(2).equals("bottomSlot")) {
 					((FactoryProductionPanel) graphics).dumpKitAtSlot(1);
 				} else if (pCmd.get(2).equals("inspectionSlot")) {
 					((FactoryProductionPanel) graphics).dumpKitAtInspection();
 				}
 			}
 
 			// Commands from ConveyorAgent
 			else if (identifier.equals("exportkitfromcell")) {
 				((FactoryProductionPanel) graphics).exportKit();
 			}
 
 			// Commands from ConveyorControllerAgent
 			else if (identifier.equals("emptykitenterscell")) {
 				((FactoryProductionPanel) graphics).newEmptyKit();
 			}
 
 			//Commands from VisionAgent
 			else if (identifier.equals("takepictureofnest")) {
 				int nestIndex = Integer.valueOf(pCmd.get(2));
 				((FactoryProductionPanel) graphics).cameraFlash(nestIndex);
 			}
 			
 			else if (identifier.equals("takepictureofinspection")) {
 				((FactoryProductionPanel) graphics).takePictureOfInspectionSlot();
 			}
 
 
 			//Swing Receive Commands
 			// commands from kit manager
 			else if (identifier.equals("addkitname")) {		// add new kit configuration to kit configuration list
 				KitConfig newKit = new KitConfig(pCmd.get(2));
 				System.out.println("Testing: " + pCmd);
 				newKit.quantity = 0;
 				int count = 3;
 
 				while(!pCmd.get(count).equals("endcmd")) {
 					String partName = pCmd.get(count);
 					newKit.listOfParts.add(partsList.get(partName));		
 					count++;
 				}
 
 				kitConfigList.put(newKit.kitName,newKit); 
 				((FactoryProdManPanel) UI).addKit(newKit.kitName);
 			}
 			else if (identifier.equals("rmkitname")) {		// remove kit configuration from kit configuration list
 				kitConfigList.remove(pCmd.get(2));
 				((FactoryProdManPanel) UI).removeKit(pCmd.get(2));
 			}
 			else if (identifier.equals("addpartname")) {	// add new part to parts list
 				Part part = new Part(pCmd.get(2),Integer.parseInt(pCmd.get(3)),pCmd.get(6),pCmd.get(4),Integer.parseInt(pCmd.get(5)));
 				partsList.put(pCmd.get(2),part);
 			}
 			else if (identifier.equals("rmpartname")) {		// remove part from parts list
 				partsList.remove(pCmd.get(2));
 				// check kits affected and remove them
 				ArrayList<String> affectedKits = kitConfigsContainingPart(pCmd.get(2));
 				if (affectedKits.size() > 0) {
 					for (String kit:affectedKits) {
 						kitConfigList.remove(kit);
 					}
 				}
 				((FactoryProdManPanel)UI).removePart(pCmd.get(2),affectedKits);
 			}
 		}
 
 		else if(action.equals("req")){
 		}
 
 		else if(action.equals("get")){
 		}
 
 		else if(action.equals("set")){
 			if (identifier.equals("kitcontent")) { 			// modify content of a kit
 				KitConfig kit = kitConfigList.get(pCmd.get(2));
 				kit.kitName = pCmd.get(3);
 				kit.listOfParts.clear();
 				for (int i = 4; i < 12; i++){
 					String partName = pCmd.get(i);
 					kit.listOfParts.add(partsList.get(partName));
 				}
 			}
 			else if (identifier.equals("kitsproduced")) { // updates number of kits produced for schedule
 				((FactoryProdManPanel) UI).kitProduced();
 			}
 			else if (identifier.equals("bintype")) { //Sets the bin for the LM
 				int feederNum = Integer.valueOf(pCmd.get(2));
 				GraphicBin bin = new GraphicBin(new Part(pCmd.get(3)));
 				((GraphicPanel) graphics).setFeederBin(feederNum, bin);
 			}
 			else if (identifier.equals("itemtype")) {
 				int kitNum = Integer.valueOf(pCmd.get(2));
 				GraphicItem item = new GraphicItem(-40, 0, pCmd.get(3));
 				((GraphicPanel) graphics).setKitItem(kitNum, item);
 			}
 			else if (identifier.equals("partconfig")) {
 				Part part = partsList.get(pCmd.get(2));
 				part.name = pCmd.get(3);
 				part.id = Integer.parseInt(pCmd.get(4));
 				part.imagePath = pCmd.get(5);
 				part.nestStabilizationTime = Integer.parseInt(pCmd.get(6));
 				part.description = pCmd.get(7);
 
 				System.out.println(part);
				System.out.println(partsList.get(pCmd.get(2)));
 			}
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
 		else 
 			System.out.println("Stuff is FU with the server...\n(string does not contain a command type)");
 
 	}
 
 
 	// Load parts list and kit configuration list from file
 	@SuppressWarnings("unchecked")
 	public void loadData(){
 		FileInputStream f;
 		ObjectInputStream o;
 		try{    // parts
 			f = new FileInputStream("InitialData/initialParts.ser");
 			o = new ObjectInputStream(f);
 			partsList = (HashMap<String,Part>) o.readObject();
 			System.out.println("Parts list loaded successfully.");
 			o.close();
 		}catch(IOException e){
 			e.printStackTrace();
 		} catch(ClassNotFoundException c){
 			c.printStackTrace();
 		}
 		try{    // kit configurations
 			f = new FileInputStream("InitialData/initialKitConfigs.ser");
 			o = new ObjectInputStream(f);
 			kitConfigList = (HashMap<String,KitConfig>) o.readObject();
 			System.out.println("Kit configuration list loaded successfully.");
 			o.close();
 		}catch(IOException e){
 			e.printStackTrace();
 		} catch(ClassNotFoundException c){
 			c.printStackTrace();
 		}
 	}
 
 	public void populatePanelList() { // adds list to panel display
 		Iterator itr = kitConfigList.entrySet().iterator(); 
 		while(itr.hasNext()) { 
 			Map.Entry pairs = (Map.Entry)itr.next(); 
 			String kitName= (String)pairs.getKey();
 			((FactoryProdManPanel)UI).addKit(kitName);
 		}
 	}
 
 	// To search a list of kit configurations for kits containing a certain part
 	//returns ArrayList<String> kitNames;
 	public ArrayList<String> kitConfigsContainingPart(String str) {
 		KitConfig kitConfig = new KitConfig();
 		String kitName = new String();
 		ArrayList<String> affectedKits = new ArrayList<String>();
 
 
 		Iterator itr = kitConfigList.entrySet().iterator(); 
 		while(itr.hasNext()) {                  
 			Map.Entry pairs = (Map.Entry)itr.next();    
 			kitConfig = (KitConfig)pairs.getValue();
 			for (Part p:kitConfig.listOfParts) {
 				if (p.name.equals(str)) {
 					affectedKits.add((String)pairs.getKey());
 					break;
 				}
 			}
 		}
 		return affectedKits;
 
 	}		
 
 }
