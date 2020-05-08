 //Contributors: Ben Mayeux,Stephanie Reagle, Joey Huang, Tobias Lee, Ryan Cleary, Marc Mendiola
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
 import factory.graphics.FactoryProductionPanel;
 import factory.graphics.LanePanel;
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
 	
 	ArrayList<Integer> laneSpeeds; // stores speeds of each lane
 	ArrayList<Integer> laneAmplitudes; // stores amplitudes of each lane
 
 	public FactoryProductionManager() {
 		super(Client.Type.fpm);
 
 		buttons = new FactoryProdManPanel(this);
 		animation = new FactoryProductionPanel(this);
 
 		setInterface();
 		
 		laneSpeeds = new ArrayList<Integer>();
 		laneAmplitudes = new ArrayList<Integer>(); 
 		for (int i = 0; i < 8; i++){    // presets lane speeds and amplitudes
 			laneSpeeds.add(2);  
 			laneAmplitudes.add(2);
 		}
 
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
 		this.setTitle("Factory Production Manager");
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
 			else if (identifier.equals("jamtoplane"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
 				((FactoryProductionPanel) graphics).jamTopLane(feederSlot);
 			}
 			else if (identifier.equals("jambottomlane"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
 				((FactoryProductionPanel) graphics).jamBottomLane(feederSlot);
 			}
 			else if (identifier.equals("unjamtoplane"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
 				((FactoryProductionPanel) graphics).unjamTopLane(feederSlot);
 			}
 			else if (identifier.equals("unjambottomlane"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
 				((FactoryProductionPanel) graphics).unjamBottomLane(feederSlot);
 			}
 			else if (identifier.equals("dumptopnest"))
 			{
 				int nestIndex = Integer.valueOf(pCmd.get(2));
 				((FactoryProductionPanel) graphics).dumpNest(nestIndex, true);
 				((FactoryProductionPanel) graphics).sendMessage("Nest " + 2*nestIndex + " is being Dumped!");
 			}
 			else if (identifier.equals("dumpbottomnest"))
 			{
 				int nestIndex = Integer.valueOf(pCmd.get(2));
 				((FactoryProductionPanel) graphics).dumpNest(nestIndex, false);
 				((FactoryProductionPanel) graphics).sendMessage("Nest " + (2*nestIndex+1) + " is being Dumped!");
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
 			else if (identifier.equals("movekitback")) {
 				if (pCmd.get(2).equals("topSlot")) {
 					((FactoryProductionPanel) graphics).moveKitFromInspectionBackToStation(0);
 				} else if (pCmd.get(2).equals("bottomSlot")) {
 					((FactoryProductionPanel) graphics).moveKitFromInspectionBackToStation(1);
 				}
 			}
 
 			// Commands from ConveyorAgent
 			else if (identifier.equals("exportkitfromcell")) {
 				((FactoryProductionPanel) graphics).exportKit();
 			}
 
 			// Commands from StandAgent
 			else if (identifier.equals("ruininspectionkit")) {
 				((FactoryProductionPanel) graphics).dropParts(pCmd.get(2));
 			}
 			// Commands from FCSAgent
 			else if (identifier.equals("kitexported")){
 				((FactoryProdManPanel) UI).kitProduced();
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
 			// commands from lane manager
 			else if (identifier.equals("badparts")) {
 				int feederIndex = Integer.valueOf(pCmd.get(2));
 				int badPercent = Integer.valueOf(pCmd.get(3));
 				((FactoryProductionPanel) graphics).setBadProbability(feederIndex, badPercent);
 			}
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
 			else if (identifier.equals("kitexported")) { // updates number of kits produced for schedule
 				((FactoryProdManPanel) UI).kitProduced();
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
 			else if (identifier.equals("partconfig")) {
 				Part part = partsList.get(pCmd.get(2));
 				part.name = pCmd.get(2);
 				part.id = Integer.parseInt(pCmd.get(3));
 				part.imagePath = pCmd.get(4);
 				part.nestStabilizationTime = Integer.parseInt(pCmd.get(5));
 				part.description = pCmd.get(6);
 
 
 				System.out.println(partsList.get(pCmd.get(2)));
 			}
 			else if (identifier.equals("lanejam")) {
 				int lanenum = Integer.parseInt(pCmd.get(2));
 				lanenum = lanenum+1;
 				((FactoryProdManPanel)UI).addMessage("A lane jam has occurred in lane " + lanenum + ".");
 			}
 			else if (identifier.equals("slowdiverter")) {
 				int feedernum = Integer.parseInt(pCmd.get(2));
 				feedernum = feedernum+1;
 				((FactoryProdManPanel)UI).addMessage("The diverter at feeder " + feedernum + " switched over late.");
 				((FactoryProductionPanel) graphics).drawString("The Diverter of Feeder " + feedernum + " is slow!");
 			}
 			else if (identifier.equals("diverterspeed")) {
 				int feedernum = Integer.valueOf(pCmd.get(2));
 				int diverterSpeed = Integer.valueOf(pCmd.get(3));
 				feedernum += 1;
 				((FactoryProductionPanel) graphics).drawString("The Diverter Speed of Feeder " + feedernum);
 				((FactoryProductionPanel) graphics).drawString("has been set to " + diverterSpeed);
 			}
 
 			// command from lane manager
 			else if (identifier.equals("lanespeed")){
 				int laneNumber = Integer.valueOf(pCmd.get(2));
 				int speed = Integer.valueOf(pCmd.get(3));
 				if(laneNumber % 2 == 0)
 					((FactoryProductionPanel) graphics).getLane(laneNumber/2).changeTopLaneSpeed(speed);
 				else
 					((FactoryProductionPanel) graphics).getLane(laneNumber/2).changeBottomLaneSpeed(speed);
 				// call graphics function to change speed
 
 			}else if (identifier.equals("laneamplitude")){
 				int laneNumber = Integer.valueOf(pCmd.get(2));
 				int amplitude = Integer.valueOf(pCmd.get(3));
				((LanePanel) graphics).GUIsetLaneAmplitude(laneNumber/2, laneNumber%2, amplitude);
 				// call graphics function to change amplitude
 			}else if (identifier.equals("guilaneamplitude")){
 				int laneNumber = Integer.valueOf(pCmd.get(2));
 				int amplitude = Integer.valueOf(pCmd.get(3));
 				((FactoryProductionPanel) graphics).GUIsetLaneAmplitude(laneNumber/2, laneNumber%2, amplitude);
 			}else if (identifier.equals("lanepower")){
 				int laneNumber = Integer.valueOf(pCmd.get(3));
 				
 				if(pCmd.get(2).equals("on")){
 					if(laneNumber % 2 == 0){
 						((FactoryProductionPanel) graphics).startTopLane(laneNumber/2);
 					}else{
 
 						((FactoryProductionPanel) graphics).startBottomLane(laneNumber/2);
 					}
 				}else if(pCmd.get(2).equals("off")){
 					if(laneNumber % 2 == 0){
 						((FactoryProductionPanel) graphics).stopTopLane(laneNumber/2);
 					}else{
 
 						((FactoryProductionPanel) graphics).stopBottomLane(laneNumber/2);
 					}
 				}
 			}else if (identifier.equals("feederpower")){
 				int feederNumber = Integer.valueOf(pCmd.get(3));
 				
 				if(pCmd.get(2).equals("on")){
 					((FactoryProductionPanel) graphics).startFeeder(feederNumber);
 				}else if(pCmd.get(2).equals("off")){
 					((FactoryProductionPanel) graphics).stopFeeder(feederNumber);
 				}
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
 	
 	public void setLaneSpeed(int laneNumber, int speed){
 		laneSpeeds.set(laneNumber, speed);
 	}
 
 	public void setLaneAmplitude(int laneNumber, int amplitude){
 		laneAmplitudes.set(laneNumber, amplitude);
 	}
 
 	public int getLaneSpeed(int laneNumber){
 		return laneSpeeds.get(laneNumber-1);
 	}
 
 	public int getLaneAmplitude(int laneNumber){
 		return laneAmplitudes.get(laneNumber-1);
 	}
 
 }
