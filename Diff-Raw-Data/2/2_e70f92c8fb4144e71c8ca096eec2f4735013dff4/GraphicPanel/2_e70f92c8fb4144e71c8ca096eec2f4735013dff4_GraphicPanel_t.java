 package factory.graphics;
 
 import java.awt.*;
 
 import javax.swing.*;
 import java.awt.event.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import factory.Part;
 import factory.client.*;
 
 /**
  * @author Minh La, Tobias Lee, George Li<p>
  * <b>{@code GraphicPanel.java}</b> (*x720)<br>
  * The superclass is what every Manager's graphical panel extends.<br>
  * It contains every other graphical component as necessary.
  */
 
 public abstract class GraphicPanel extends JPanel implements ActionListener{
 	
 	public int WIDTH, HEIGHT;
 	public static final Image TILE_IMAGE = Toolkit.getDefaultToolkit().getImage("Images/Tiles/floorTileXGrill.png");
 	public static final int TILE_SIZE = 128;
 	public static final int DELAY = 20;
 	
 	protected Client am; //The Client that holds this
 	
 	protected boolean isLaneManager;
 	protected boolean isGantryRobotManager;
 	protected boolean isKitAssemblyManager;
 	protected boolean isFactoryProductionManager;
 	
 	// LANE MANAGER
 	protected GraphicLaneManager [] lane;
 	
 	// CAMERA
 	protected int flashCounter;
 	protected int flashFeederIndex;
 	protected static Image flashImage;
 	
 	// KIT MANAGER
 	protected GraphicConveyorBelt belt; //The conveyer belt
 	protected GraphicKittingStation station; //The kitting station
 	protected GraphicKittingRobot kitRobot;
 	
 	// PARTS MANAGER
 	protected ArrayList<GraphicNest> nests;
 	protected GraphicPartsRobot partsRobot;
 	
 	// GANTRY
 	protected GraphicGantryRobot gantryRobot;
 	
 	// MESSAGES
 	protected ArrayList<String> messageList;
 	protected Font messageFont;
 	protected int messageCounter;
 	private static int MAX_MESSAGES = 5;
 	
 	protected GraphicItem transferringItem;
 	
 	public GraphicPanel(/*int offset/**/) {
 		WIDTH = 1100;
 		HEIGHT = 720;
 		am = null;
 		messageList = new ArrayList<String>();
 		/*lane = null;
 		belt = null;
 		station = null;
 		kitRobot = null;
 		nests = null;
 		partsRobot = null;
 		gantryRobot = null;
 		
 		isLaneManager = false;
 		isGantryRobotManager = false;
 		isKitAssemblyManager = false;
 		isFactoryProductionManager = false;*/
 		
 		flashImage = Toolkit.getDefaultToolkit().getImage("Images/cameraFlash3x3.png");
 		try {
 			messageFont = Font.createFont(Font.TRUETYPE_FONT,new File("Fonts/digitalism.ttf"));
		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		messageCounter = -1;
 		transferringItem = null;
 		
 		/*belt = new GraphicConveyorBelt(0-offset, 0, this);
 		station = new GraphicKittingStation(200-offset, 191, this);
 		kitRobot = new GraphicKittingRobot(this, 70-offset, 250);
 		
 		// Parts robot client
 		// Add 8 nests
 		nests = new ArrayList<GraphicNest>();	
 		for(int i = 0; i < 8; i++)
 		{
 			GraphicNest newNest = new GraphicNest(510-offset,i*80+50,0,0,0,0,75,75,"Images/nest3x3.png");
 			nests.add(newNest);
 		}
 
 		lane = new GraphicLaneManager [4];
 		for (int i = 0; i < lane.length; i++)
 			lane[i] = new GraphicLaneManager(510-offset, 160*i + 50, i, this);
 		
 		partsRobot = new GraphicPartsRobot(350-offset,360,0,5,5,10,100,100,"Images/robot1.png");
 		gantryRobot = new GraphicGantryRobot(950-offset,360,0,5,5,10,100,100,"Images/robot2.png");*/
 	}
 	
 	/**TODO: Kit Assembly Methods*/
 	/**
 	 * Adds a Kit into the Factory via Conveyor Belt
 	 * @see #newEmptyKitDone()
 	 */
 	public void newEmptyKit() {
 		//if (!belt.kitin())
 		if (isKitAssemblyManager || isFactoryProductionManager)
 			belt.inKit();
 	}
 	
 	/**
 	 * Sends Kit Robot to pick up a Kit from the Conveyor Belt and move to the designated slot in the Kit Station
 	 * @param target The targeted slot in the Kit Station
 	 * @see #moveEmptyKitToSlotDone()
 	 */
 	public void moveEmptyKitToSlot(int target) {
 		//if (belt.pickUp() && !kitRobot.kitted() && station.getKit(target) == null) {
 		if (isKitAssemblyManager || isFactoryProductionManager) {
 			kitRobot.setFromBelt(true);
 			kitRobot.setStationTarget(target);
 		}
 	}
 	
 	/**
 	 * Sends Kit Robot to move a Kit from the designated slot in the Kit Station to the Inspection Station
 	 * @param target The targeted slot in the Kit Station
 	 * @see #moveKitToInspectionDone()
 	 */
 	public void moveKitToInspection(int target) {
 		//if (!kitRobot.kitted() && station.getKit(target) != null) {
 		if (isKitAssemblyManager || isFactoryProductionManager) {
 			kitRobot.setCheckKit(true);
 			kitRobot.setStationTarget(target);
 		}
 	}
 	
 	/**
 	 * Takes a picture of the Kit in the Inspection Station
 	 * @see #takePictureOfInspectionDone()
 	 */
 	public void takePictureOfInspectionSlot() {
 		//if (station.hasCheck())
 		if (isKitAssemblyManager || isFactoryProductionManager)
 			station.checkKit();
 	}
 	
 	/**
 	 * Dumps the Kit at the designated slot in the Kit Station
 	 * @param target The targeted slot in the Kit Station
 	 * @see #dumpKitAtInspectionDone()
 	 */
 	public void dumpKitAtSlot(int target) {
 		if (isKitAssemblyManager || isFactoryProductionManager) {
 			kitRobot.setPurgeKit(true);
 			kitRobot.setStationTarget(target);
 		}
 	}
 	
 	/**
 	 * Dumps the Kit in the Inspection Station
 	 * @see #dumpKitAtInspectionDone()
 	 */
 	public void dumpKitAtInspection() {
 		//if (!kitRobot.kitted() && station.getCheck() != null)
 		if (isKitAssemblyManager || isFactoryProductionManager)
 			kitRobot.setPurgeInspectionKit(true);
 	}
 	
 	/**
 	 * Moves the Kit in the Inspection Station to the Conveyor Belt
 	 * @see #moveKitFromInspectionToConveyorDone()
 	 */
 	public void moveKitFromInspectionToConveyor() {
 		//if (station.getCheck() != null && !kitRobot.kitted())
 		if (isKitAssemblyManager || isFactoryProductionManager)
 			kitRobot.setFromCheck(true);
 	}
 	
 	/**
 	 * Moves the Kit in the Inspection Station back to the designated slot in the Kit Station
 	 * @param target The designated slot in the Kit Station
 	 * @see #moveKitFromInspectionBackToStationDone()
 	 */
 	public void moveKitFromInspectionBackToStation(int target) {
 		if (isKitAssemblyManager || isFactoryProductionManager) {
 			kitRobot.setReCheck(true);
 			kitRobot.setStationTarget(target);
 		}
 	}
 	
 	/**
 	 * Sends a Kit out of the Factory via Conveyor Belt
 	 * @see #exportKitDone()
 	 */
 	public void exportKit() {
 		if (isKitAssemblyManager || isFactoryProductionManager)
 			belt.exportKit();
 	}
 	
 	/**
 	 * Drops the requested Items from the next Inspected Kit
 	 * @param dropped The Binary String delineating the parts to drop
 	 */
 	public void dropParts(String dropped) {
 		if (isKitAssemblyManager || isFactoryProductionManager)
 			kitRobot.breakNextKit(dropped);
 	}
 	
 	/**TODO: Gantry Robot methods*/
 	/**
 	 * Moves Gantry Robot to pick up a Bin with the provided image
 	 * @param path The image path to the desired Part image
 	 * @see #gantryRobotArrivedAtPickup()
 	 */
 	public void moveGantryRobotToPickup(String path)
 	{
 		if (isGantryRobotManager || isFactoryProductionManager) {
 			gantryRobot.setState(1);
 			gantryRobot.setPartPath(path);
 			gantryRobot.setDestination(WIDTH-100,-100,0);
 		}
 	}
 	
 	/**
 	 * Moves Gantry Robot to the designated Feeder to drop off Bin
 	 * @param feederIndex The designated Feeder
 	 * @see #gantryRobotArrivedAtFeederForDropoff()
 	 */
 	public void moveGantryRobotToFeederForDropoff(int feederIndex)
 	{
 		// Error checking code has temporarily(?) been commented out as requested by Alfonso
 		//if(lane[feederIndex].hasBin())
 		//{
 			//System.err.println("Can't dropoff: feeder " + feederIndex + " (0-based index) already has a bin!");
 			//gantryRobotArrivedAtFeederForDropoff();
 		//}
 		//else if(!gantryRobot.hasBin())
 		//{
 			//System.err.println("Can't dropoff: gantry robot does not have a bin!");
 			//gantryRobotArrivedAtFeederForDropoff();
 		//}
 		//else
 		//{
 		if (isGantryRobotManager || isFactoryProductionManager) {
 			gantryRobot.setState(3);
 			gantryRobot.setDestinationFeeder(feederIndex);
 			gantryRobot.setDestination(lane[feederIndex].feederX+115, lane[feederIndex].feederY+15,180);
 		}
 			//gantryRobotArrivedAtFeederForDropoff();
 		//}
 	}
 	
 	/**
 	 * Moves Gantry Robot to the designated Feeder to pick up a purged Bin
 	 * @param feederIndex The designated Feeder
 	 * @see #gantryRobotArrivedAtFeederForPickup()
 	 */
 	public void moveGantryRobotToFeederForPickup(int feederIndex)
 	{
 		// Error checking
 		//if(!lane[feederIndex].hasBin())
 		//{
 		//	System.err.println("Can't pickup: no bin at feeder " + feederIndex + " (0-based index)!");
 		//	gantryRobotArrivedAtFeederForPickup();
 		//}
 		//else
 		//{
 		if (isGantryRobotManager || isFactoryProductionManager) {
 			gantryRobot.setState(5);
 			gantryRobot.setDestinationFeeder(feederIndex);
 			gantryRobot.setDestination(lane[feederIndex].feederX+115, lane[feederIndex].feederY+15,180);
 		}
 		//}
 	}
 	
 	/**TODO: Parts Robot and Nest methods*/
 	/**
 	 * Takes a picture of the designated Lane pair
 	 * @param nestIndex The designated Lane pair
 	 * @see #cameraFlashDone()
 	 */
 	public void cameraFlash(int nestIndex) {
 		if (isLaneManager || isFactoryProductionManager) {
 			flashCounter = 10;
 			flashFeederIndex = nestIndex;
 		}
 	}
 	
 	//CHANGE TO 0 BASE
 	/**
 	 * Moves Parts Robot to the designated Nest to pick up Part
 	 * @param nestIndex The designated Nest
 	 * @deprecated Use movePartsRobotToNest(int nestIndex, int itemIndex) instead
 	 * @see #partsRobotArrivedAtNest()
 	 */
 	public void movePartsRobotToNest(int nestIndex) {
 		if (isFactoryProductionManager) {
 			partsRobot.setState(1);
 			partsRobot.adjustShift(5);
 			partsRobot.setDestination(nests.get(nestIndex).getX()-nests.get(nestIndex).getImageWidth()-10,nests.get(nestIndex).getY()-15,0);
 			partsRobot.setDestinationNest(nestIndex);
 		}
 	}
 	
 	/**
 	 * Moves Parts Robot to the designated Nest to pick up the Part at the given index
 	 * @param nestIndex The designated Nest
 	 * @param itemIndex The designated Part
 	 * @see #partsRobotArrivedAtNest()
 	 */
 	public void movePartsRobotToNest(int nestIndex, int itemIndex) {
 		if (isFactoryProductionManager) {
 			partsRobot.setItemIndex(itemIndex);
 			partsRobot.setState(1);
 			partsRobot.adjustShift(5);
 			partsRobot.setDestination(nests.get(nestIndex).getX()-nests.get(nestIndex).getImageWidth()-10,nests.get(nestIndex).getY()-15,0);
 			partsRobot.setDestinationNest(nestIndex);
 		}
 	}
 	
 	/**
 	 * Moves Parts Robot to the designated slot in the Kit Station
 	 * @param kitIndex The designated slot in the Kit Station
 	 * @see #partsRobotArrivedAtStation()
 	 */
 	public void movePartsRobotToStation(int kitIndex) {
 		if (isFactoryProductionManager) {
 			partsRobot.setState(3);
 			partsRobot.setDestination(station.getX()+65,station.getY()+100*kitIndex-station.getY()%5+10,180);
 			partsRobot.setDestinationKit(kitIndex);
 		}
 	}
 	
 	/**
 	 * Adds Item from the Parts Robot to the Kit Station it's in front of
 	 * @param itemIndex The index of the Item to remove
 	 */
 	public void partsRobotPopItemToCurrentKit(int itemIndex)
 	{
 		transferringItem = partsRobot.popItemAt(itemIndex);
 		station.addItem(transferringItem,partsRobot.getDestinationKit());
 		partsRobotPopItemToCurrentKitDone();
 	}
 	
 	/**
 	 * Moves Parts Robot to the center of the Factory
 	 * @see #partsRobotArrivedAtCenter()
 	 */
 	public void movePartsRobotToCenter() {
 		if (isFactoryProductionManager) {
 			partsRobot.setState(5);
 			partsRobot.setDestination(WIDTH/2-200, HEIGHT/2,0);
 		}
 	}
 	
 	/**
 	 * Drops whatever items the Parts Robot is holding
 	 * @see #dropPartsRobotsItemsDone()
 	 */
 	public void dropPartsRobotsItems() {
 		if (isFactoryProductionManager)
 			partsRobot.clearItems();
 		dropPartsRobotsItemsDone();
 	}
 	
 	/**
 	 * Dumps the Items at the given Nest
 	 * @param nestIndex The given Nest Pair
 	 * @param isTop If the Nest to be purged is on the Top or the Bottom
 	 */
 	public void dumpNest(int nestIndex, boolean isTop) {
 		if (isLaneManager || isFactoryProductionManager) {
 			nests.get(nestIndex*2 + (isTop ? 0 : 1)).clearItems();
 		}
 	}
 	
 	/**TODO: Lane methods*/
 	/**
 	 * Begins feeding the designated Feeder
 	 * @param feederNum The designated Feeder
 	 * @see #feedLaneDone(int feederNum) 
 	 */
 	public void feedFeeder(int feederNum) {
 		//if(!lane[feederNum].lane1PurgeOn){	//If purging is on, cannot feed!
 		if (isLaneManager || isFactoryProductionManager) {
 			lane[feederNum].bin.getBinItems().clear();
 			if(lane[feederNum].divergeUp){
 				//lane[feederNum].changeTopLaneSpeed(1);			//resets laneSpeed to 1. Change to panel's slide bar
 				//lane[feederNum].changeTopLaneAmplitude(2);		//resets amplitude to 2
 			}
 			else{
 				//lane[feederNum].changeBottomLaneSpeed(1);		//resets laneSpeed to 1. Change to panel's slide bar
 				//lane[feederNum].changeTopLaneAmplitude(2);		//resets amplitude to 2
 			}
 			for(int i = 0; i < lane[feederNum].bin.binSize;i++){		//unlimited items
 				lane[feederNum].bin.binItems.add(new GraphicItem(-40, 0, "Images/"+lane[feederNum].bin.partName+".png"));
 			}
 			if(lane[feederNum].hasBin() && lane[feederNum].bin.getBinItems().size() > 0){
 				if(lane[feederNum].divergeUp)
 					lane[feederNum].lane1Start = true;
 				else
 					lane[feederNum].lane2Start = true;
 				lane[feederNum].feederOn = true;
 			}
 		}
 	}
 	
 	/**
 	 * Begins feeding the designated Lane.<br>
 	 * Divide laneNum by 2
 	 * @param laneNum The designated Lane
 	 * @deprecated Use feedFeeder(int feederNum) instead
 	 * @see #feedLaneDone(int feederNum)
 	 */
 	public void feedLane(int laneNum){ //FEEDS THE LANE! Lane 0-7
 		//if(!lane[(laneNum) / 2].lane1PurgeOn){	//If purging is on, cannot feed!
 		if (isLaneManager || isFactoryProductionManager) {
 			lane[laneNum / 2].bin.getBinItems().clear();
 			if(lane[laneNum / 2].divergeUp){
 				//lane[laneNum / 2].changeTopLaneSpeed(1);			//resets laneSpeed to 1. Change to panel's slide bar
 				//lane[laneNum / 2].changeTopLaneAmplitude(2);		//resets amplitude to 2
 			}
 			else{
 				//lane[laneNum / 2].changeBottomLaneSpeed(1);		//resets laneSpeed to 1. Change to panel's slide bar
 				//lane[laneNum / 2].changeTopLaneAmplitude(2);		//resets amplitude to 2
 			}
 			for(int i = 0; i < lane[laneNum / 2].bin.binSize;i++){		//unlimited items
 				lane[laneNum / 2].bin.binItems.add(new GraphicItem(-40, 0, "Images/"+lane[laneNum / 2].bin.partName+".png"));
 			}
 			
 			if(lane[(laneNum) / 2].hasBin() && lane[(laneNum) / 2].bin.getBinItems().size() > 0){
 				lane[(laneNum) / 2].divergeUp = ((laneNum) % 2 == 0);
 				if(lane[(laneNum) / 2].divergeUp)
 					lane[(laneNum) / 2].lane1Start = true;
 				else
 					lane[(laneNum) / 2].lane2Start = true;
 				lane[(laneNum) / 2].feederOn = true;
 			}
 		}
 		//System.out.println("bin size " + lane[(laneNum) / 2].bin.getBinItems().size());
 	}
 	
 	/**
 	 * Starts the designated Lane
 	 * @param laneNum The designated Lane
 	 * @deprecated use startFeeder(int feederNum) instead
 	 */
 	public void startLane(int laneNum){
 		if (isLaneManager || isFactoryProductionManager) {
 			if(lane[(laneNum) / 2].divergeUp)
 				lane[(laneNum) / 2].lane1Start = true;
 			else
 				lane[(laneNum) / 2].lane2Start = true;
 		}
 	}
 	
 	/**
 	 * Switches the Lane
 	 * @param laneNum The designated Lane
 	 * @deprecated use switchFeederLane(int feederNum) instead
 	 */
 	public void switchLane(int laneNum){
 		if (isLaneManager || isFactoryProductionManager) {
 			lane[(laneNum) / 2].divergeUp = !lane[(laneNum) / 2].divergeUp;
 			lane[(laneNum) / 2].vY = -(lane[(laneNum) / 2].vY);
 		}
 	}
 	
 	/**
 	 * Switches the Lane at the designated Feeder 
 	 * @param feederNum The designated Feeder
 	 */
 	public void switchFeederLane(int feederNum){
 		if (isLaneManager || isFactoryProductionManager) {
 			lane[feederNum].divergeUp = !lane[feederNum].divergeUp;
 			lane[feederNum].vY = -(lane[feederNum].vY);
 			switchFeederLaneDone(feederNum);
 		}
 	}
 	
 	/**
 	 * Stops the designated Lane
 	 * @param laneNum The designated Lane
 	 * @deprecated use turnFeederOff(int feederNum) instead
 	 */
 	public void stopLane(int laneNum){
 		if (isLaneManager || isFactoryProductionManager)
 			if(lane[(laneNum) / 2].divergeUp)
 				lane[(laneNum) / 2].lane1Start = false;
 			else
 				lane[(laneNum) / 2].lane2Start = false;
 	}
 	
 	/**
 	 * Starts up the designated Feeder
 	 * @param feederNum The designated Feeder
 	 */
 	public void turnFeederOn(int feederNum){
 		if (isLaneManager || isFactoryProductionManager) {
 			lane[feederNum].feederOn = true;
 			startFeederDone(feederNum);
 		}
 	}
 	
 	/**
 	 * Turns off the designated Feeder
 	 * @param feederNum The designated Feeder
 	 */
 	public void turnFeederOff(int feederNum){
 		if (isLaneManager || isFactoryProductionManager) {
 			lane[feederNum].feederOn = false;
 			stopFeederDone(feederNum);
 		}
 	}
 	
 	/**
 	 * Purges the designated Feeder
 	 * @param feederNum The designated Feeder
 	 */
 	public void purgeFeeder(int feederNum){ // takes in lane 0 - 4
 		// The following 2 lines were causing the bin to disappear, which is undesirable	
 //		lane[(feederNum)].bin = null;
 //		lane[(feederNum)].binExists = false;
 		if (isGantryRobotManager || isLaneManager || isFactoryProductionManager) {
 			lane[(feederNum)].purgeFeeder();
 			//purgeFeederDone(feederNum); // send the confirmation //Done inside GraphicLaneManager
 		}
 	}
 	
 	/**
 	 * Purges the Top Lane of the designated Feeder
 	 * @param feederNum The designated Feeder
 	 */
 	public void purgeTopLane(int feederNum){
 		if (isLaneManager || isFactoryProductionManager) {
 			lane[feederNum].lane1PurgeOn = true;
 			lane[feederNum].feederOn = false;
 			lane[feederNum].lane1Start = true;
 		}
 	}
 	
 	/**
 	 * Purges the Bottom Lane of the designated Feeder
 	 * @param feederNum The designated Feeder
 	 */
 	public void purgeBottomLane(int feederNum){
 		if (isLaneManager || isFactoryProductionManager) {
 			lane[feederNum].lane2PurgeOn = true;
 			lane[feederNum].feederOn = false;
 			lane[feederNum].lane2Start = true;
 		}
 	}
 	
 	/**
 	 * Jams the top lane. All items stops behind the first item.
 	 * @param feederNum The designated feeder, 0-3
 	 */
 	public void jamTopLane(int feederNum){
 		lane[feederNum].lane1Jam = true;
 	}
 	
 	/**
 	 * Unjams the top lane. Return back to normal.
 	 * @param feederNum The designated feeder, 0-3
 	 */
 	public void unjamTopLane(int feederNum){
 		lane[feederNum].changeTopLaneSpeed(lane[feederNum].lane1Speed + 1);
 		if(lane[feederNum].lane1Speed > 8)	//laneSpeed max is 8
 			lane[feederNum].changeTopLaneSpeed(8);
 		lane[feederNum].vibrationAmplitudeTop += 1;
 		if(lane[feederNum].vibrationAmplitudeTop > 8)		//vibration amplitude max is 8
 			lane[feederNum].vibrationAmplitudeTop = 8;
 		lane[feederNum].lane1Jam = false;
 	}
 	
 	/**
 	 * Jams the bottom lane. All items stops behind the first item.
 	 * @param feederNum The designated feeder, 0-3
 	 */
 	public void jamBottomLane(int feederNum){
 		lane[feederNum].lane2Jam = true;
 	}
 	
 	/**
 	 * Unjams the bottom lane. Return back to normal.
 	 * @param feederNum The designated feeder, 0-3
 	 */
 	public void unjamBottomLane(int feederNum){
 		lane[feederNum].changeBottomLaneSpeed(lane[feederNum].lane2Speed + 1);
 		if(lane[feederNum].lane2Speed > 8)	//laneSpeed max is 8
 			lane[feederNum].changeBottomLaneSpeed(8);
 		lane[feederNum].vibrationAmplitudeBottom += 1;
 		if(lane[feederNum].vibrationAmplitudeBottom > 8)		//vibration amplitude max is 8
 			lane[feederNum].vibrationAmplitudeBottom = 8;
 		lane[feederNum].lane2Jam = false;
 	}
 	
 	/**
 	 * Stops the Top Lane of the given Lane Pair
 	 * @param feederNum The relevant Lane Pair
 	 */
 	public void stopTopLane(int feederNum){
 		lane[feederNum].lane1Start = false;
 	}
 
 	/**
 	 * Starts the Top Lane of the given Lane Pair
 	 * @param feederNum The relevant Lane Pair
 	 */
 	public void startTopLane(int feederNum){
 		lane[feederNum].lane1Start = true;
 	}
 
 	/**
 	 * Stop the Bottom Lane of the given Lane Pair
 	 * @param feederNum The relevant Lane Pair
 	 */
 	public void stopBottomLane(int feederNum){
 		lane[feederNum].lane2Start = false;
 	}
 
 	/**
 	 * Starts the Bottom Lane of the given Lane Pair
 	 * @param feederNum The relevant Lane Pair
 	 */
 	public void startBottomLane(int feederNum){
 		lane[feederNum].lane2Start = true;
 	}
 	
 	/**
 	 * Increase top lane speed by 1
 	 * @param feederNum the designated feeder, 1-4
 	 */
 	public void increaseTopLaneSpeed(int feederNum){
 		lane[feederNum].changeTopLaneSpeed(lane[feederNum].lane1Speed + 1); //change top LaneSpeed
 		if( lane[feederNum].lane1Speed > 8)
 			lane[feederNum].lane1Speed = 8;
 	}
 	
 	/**
 	 * Decrease top lane speed by 1
 	 * @param feederNum the designated feeder, 1-4
 	 */
 	
 	public void decreaseTopLaneSpeed(int feederNum){
 		lane[feederNum].changeTopLaneSpeed(lane[feederNum].lane1Speed - 1); //change top LaneSpeed
 		if( lane[feederNum].lane1Speed < 1)
 			lane[feederNum].lane1Speed = 1;
 	}
 	
 	/**
 	 * Increase bottom lane speed by 1
 	 * @param feederNum the designated feeder, 1-4
 	 */
 	public void increaseBottomLaneSpeed(int feederNum){
 		lane[feederNum].changeTopLaneSpeed(lane[feederNum].lane2Speed + 1); //change bottom LaneSpeed
 		if( lane[feederNum].lane2Speed > 8)
 			lane[feederNum].lane2Speed = 8;
 	}
 	
 	/**
 	 * Decrease bottom lane speed by 1
 	 * @param feederNum the designated feeder, 1-4
 	 */
 	public void decreaseBottomLaneSpeed(int feederNum){
 		lane[feederNum].changeTopLaneSpeed(lane[feederNum].lane2Speed - 1); //change bottom LaneSpeed
 		if( lane[feederNum].lane2Speed < 1)
 			lane[feederNum].lane2Speed = 1;
 	}
 	
 	/**
 	 * increase the Top lane vibration speed by 1
 	 * @param feederNum the designated feeder, 1-4
 	 */
 	public void increaseTopLaneAmplitude(int feederNum){
 		lane[feederNum].vibrationAmplitudeTop++;
 		if(lane[feederNum].vibrationAmplitudeTop > 8)
 			lane[feederNum].vibrationAmplitudeTop = 8;
 	}
 	
 	/**
 	 * decrease the Top lane vibration speed by 1
 	 * @param feederNum the designated feeder, 1-4
 	 */
 	public void decreaseTopLaneAmplitude(int feederNum){
 		lane[feederNum].vibrationAmplitudeTop--;
 		if(lane[feederNum].vibrationAmplitudeTop < 1)
 			lane[feederNum].vibrationAmplitudeTop = 1;
 	}
 	
 	/**
 	 * increase the Bottom lane vibration speed by 1
 	 * @param feederNum the designated feeder, 1-4
 	 */
 	public void increaseBottomLaneAmplitude(int feederNum){
 		lane[feederNum].vibrationAmplitudeBottom++;		//change to bottom vibrationAmplitude
 		if(lane[feederNum].vibrationAmplitudeBottom > 8)
 			lane[feederNum].vibrationAmplitudeBottom = 8;
 	}
 	
 	/**
 	 * decrease the Bottom lane vibration speed by 1
 	 * @param feederNum the designated feeder, 1-4
 	 */
 	public void decreaseBottomLaneAmplitude(int feederNum){
 		lane[feederNum].vibrationAmplitudeBottom--;		//change to bottom vibrationAmplitude
 		if(lane[feederNum].vibrationAmplitudeBottom < 1)
 			lane[feederNum].vibrationAmplitudeBottom = 1;
 	}
 	
 	/**
 	 * Sets the probability that an Item fed into the given Lane will be bad
 	 * @param feederNum The given Lane
 	 * @param probability The probability that an Item will be bad
 	 */
 	public void setBadProbability(int feederNum, int probability) {
 		lane[feederNum].setBadProbability(probability);
 	}
 	
 	
 	/**Movement methods*/
 	/**
 	 * Moves the Parts Robot
 	 */
 	public void partsRobotStateCheck() {
 		// Has robot arrived at its destination?
 		//System.out.println(partsRobot.getState());
 		if (isFactoryProductionManager) {
 			if(partsRobot.getState() == 2)		// partsRobot has arrived at nest
 			{
 				if (nests.get(partsRobot.getDestinationNest()).hasItem())
 					partsRobot.addItem(nests.get(partsRobot.getDestinationNest()).popItemAt(partsRobot.getItemIndex()));
 				partsRobot.setState(0);
 				partsRobotArrivedAtNest();
 			}
 			else if(partsRobot.getState() == 4)	// partsRobot has arrived at kitting station
 			{
 				/*
 				System.out.println("Size:"+partsRobot.getSize());
 				int numberOfParts = partsRobot.getSize();
 				for(int i = 0; i < numberOfParts; i++)
 				{
 					System.out.println("Adding part to kit: " + i);
 					station.addItem(partsRobot.popItemAt(partsRobot.getItemIndex()),partsRobot.getDestinationKit());
 				}
 				*/
 				partsRobot.setState(0);
 				partsRobotArrivedAtStation();
 			}
 			else if(partsRobot.getState() == 6)
 			{
 				partsRobot.setState(0);
 				partsRobotArrivedAtCenter();
 			}
 		}
 	}
 	
 	/**
 	 * Moves the Gantry Robot
 	 */
 	public void gantryRobotStateCheck() {
 		if (isGantryRobotManager || isFactoryProductionManager) {
 			if(gantryRobot.getState() == 2)				// gantry robot reached bin pickup point
 			{
 				gantryRobot.setState(0);
 				// Give gantry robot a bin
 				gantryRobot.giveBin(new GraphicBin(new Part(gantryRobot.getPartPath())));
 				gantryRobotArrivedAtPickup();
 			}
 			else if(gantryRobot.getState() == 4)		// gantry robot reached feeder for dropoff
 			{
 				gantryRobot.setState(0);
 				lane[gantryRobot.getDestinationFeeder()].setBin(gantryRobot.popBin());
 				gantryRobotArrivedAtFeederForDropoff();
 			}
 			else if(gantryRobot.getState() == 6)		// gantry robot reached feeder for pickup
 			{
 				gantryRobot.setState(0);
 				gantryRobot.giveBin(lane[gantryRobot.getDestinationFeeder()].popBin());
 				gantryRobotArrivedAtFeederForPickup();
 			}
 		}
 	}
 	
 	/**
 	 * Sets the Bin for the specified feeder
 	 * @param feederNum The index of the Feeder
 	 * @param bin The Bin to be added
 	 */
 	public void setFeederBin(int feederNum, GraphicBin bin) {
 		lane[feederNum].setBin(bin);
 	}
 	
 	/**
 	 * Adds an Part to the specified Kit
 	 * @param kitNum The index of the Kit
 	 * @param item The Item to be added
 	 */
 	public void setKitItem(int kitNum, GraphicItem item) {
 		station.addItem(item, kitNum);
 	}
 	
 	/**
 	 * Removes the Item at the given index from the given Nest
 	 * @param nestNum The index of the Nest
 	 * @param itemIndex The index of the Item
 	 */
 	public void popNestItem(int nestNum, int itemIndex) {
 		nests.get(nestNum).popItemAt(itemIndex);
 	}
 	
 	/**
 	 * Removes all the Items in the given Nest
 	 * Also jams the lane items
 	 * @param nestNum The index of the Nest
 	 */
 	public void purgeNest(int nestNum) {
 		nests.get(nestNum).clearItems();
 		if(nestNum % 2 == 0)
 			lane[nestNum / 2].lane1Jam = true;
 		else
 			lane[nestNum / 2].lane2Jam = true;
 	}
 	
 	/**
 	 * Moves Parts down the Lanes
 	 */
 	public void moveLanes() {
 		if (isGantryRobotManager || isLaneManager || isFactoryProductionManager) {
 			for (int i = 0; i < lane.length; i++)
 				lane[i].moveLane();
 		}
 	}
 	
 	/**
 	 * Sends message to Server depending
 	 * @param command The command to send
 	 */
 	public void sendMessage(String command) {
 		//if (am == null)
 			//return;
 		//asdfasd
 		String message;
 		if (isLaneManager)
 			message = "lm ";
 		else if (isGantryRobotManager)
 			message = "gm ";
 		else if (isKitAssemblyManager)
 			message = "kam ";
 		else if (isFactoryProductionManager)
 			message = "fpm ";
 		else
 			return;
 		
 		if (am != null)
 			am.sendCommand(message + command);
 		else
 			System.out.println(message + command);
 	}
 	/**TODO: THIS IS SO I CAN FIND THE DONES*/
 	/**These are pretty self-explanatory*/
 	public void newEmptyKitDone() {
 		sendMessage("cca cnf");
 	}
 	
 	public void moveEmptyKitToSlotDone() {
 		sendMessage("kra cnf");
 	}
 
 	public void moveKitToInspectionDone() {
 		sendMessage("kra cnf");
 	}
 	
 	public void takePictureOfInspectionSlotDone() {
 		sendMessage("va cnf");
 	}
 
 	public void dumpKitAtInspectionDone() {
 		sendMessage("kra cnf");
 	}
 
 	public void moveKitFromInspectionToConveyorDone() {
 		sendMessage("kra cnf");
 	}
 	
 	public void moveKitFromInspectionBackToStationDone() {
 		sendMessage("kra cnf");
 	}
 
 	public void exportKitDone() {
 		sendMessage("ca cnf");
 	}
 	
 	public void cameraFlashDone() {
 		sendMessage("va cnf");
 	}
 
 	public void gantryRobotArrivedAtPickup() {
 		sendMessage("ga cnf");
 	}
 
 	public void gantryRobotArrivedAtFeederForDropoff() {
 		sendMessage("lm set loadpartatfeeder " + gantryRobot.getDestinationFeeder() + " "+ lane[gantryRobot.getDestinationFeeder()].getBin().getPartName());
 		sendMessage("ga cnf");
 	}
 
 	public void gantryRobotArrivedAtFeederForPickup()
 	{
 		sendMessage("ga cnf");
 	}
 	
 	public void partsRobotArrivedAtNest() {
 		if (isFactoryProductionManager) {
 			sendMessage("lm set nestitemtaken " + partsRobot.getDestinationNest() + " " + partsRobot.getItemIndex());
 			sendMessage("na cmd partremovedfromnest " + partsRobot.getDestinationNest() + " " + nests.get(partsRobot.getDestinationNest()).getItemAt(partsRobot.getItemIndex()).getName());
 		}
 		sendMessage("pra cnf");
 	}
 
 	public void partsRobotArrivedAtStation() {
 		sendMessage("pra cnf");
 	}
 
 	public void partsRobotArrivedAtCenter() {
 		sendMessage("pra cnf");
 	}
 	
 	public void dropPartsRobotsItemsDone() {
 		sendMessage("pra cnf");
 	}
 	
 	public void partsRobotPopItemToCurrentKitDone() {
 		if (isFactoryProductionManager) {
 			sendMessage("kam set itemtype " + partsRobot.getDestinationKit() + " " + transferringItem.getImagePath());
 		}
 		sendMessage("pra cnf");
 	}
 
 	public void feedLaneDone(int feederNum){
 		sendMessage("fa cnf " + feederNum);
 	}
 	
 	public void purgeTopLaneDone(int feederNum) {
 		sendMessage("fa cnf " + feederNum);
 	}
 	
 	public void purgeBottomLaneDone(int feederNum) {
 		sendMessage("fa cnf " + feederNum);
 	}
 	
 	public void purgeFeederDone(int feederNum) {
 		sendMessage("fa cnf " + feederNum);
 	}
 	
 	public void startFeederDone(int feederNum) {
 		sendMessage("fa cnf " + feederNum);
 	}
 	
 	public void stopFeederDone(int feederNum) {
 		sendMessage("fa cnf " + feederNum);
 	}
 	
 	public void switchFeederLaneDone(int feederNum) {
 		sendMessage("fa cnf " + feederNum);
 	}
 	
 	public GraphicKittingStation getStation() {
 		return station;
 	}
 	public GraphicConveyorBelt getBelt() {
 		return belt;
 	}
 	public ArrayList<GraphicNest> getNest(){
 		return nests;
 	}
 	public GraphicLaneManager getLane(int index) {
 		return lane[index];
 	}
 	
 	/**TODO: Paint function*/
 	/**
 	 * Paints all the applicable parts for the given panel
 	 * @param g The specified graphics window
 	 */
 	public void paint(Graphics g) {
 		for(int j = 0; j < WIDTH; j += TILE_SIZE)
 		{
 			for(int k = 0; k < HEIGHT; k += TILE_SIZE)
 			{
 				g.drawImage(TILE_IMAGE, j, k, TILE_SIZE, TILE_SIZE, null);
 			}
 		}
 		//g.setColor(new Color(200, 200, 200));
 		//g.fillRect(0, 0, getWidth(), getHeight());
 		
 		if (isKitAssemblyManager || isFactoryProductionManager) {
 			belt.paint(g);
 			station.paint(g);
 			kitRobot.paint(g);
 		}
 		
 		if (isLaneManager || isFactoryProductionManager) {
 			for (int i = 0; i < lane.length; i++) {
 				//if (lane[i] != null)
 				lane[i].paintLane(g);
 			}
 		}
 		
 		if (isLaneManager || isFactoryProductionManager || isGantryRobotManager) {
 			for (int i = 0; i < lane.length; i++) {
 				//if (lane[i] != null)
 				lane[i].paintFeeder(g);
 			}
 		}
 		
 		// Parts robot client
 		// Draw the nests
 		if (isLaneManager || isFactoryProductionManager)
 		for(int i = 0; i < nests.size(); i++) {
 			GraphicNest currentNest = nests.get(i);
 			currentNest.paint(g);
 		}
 		
 		if(isLaneManager || isFactoryProductionManager) {
 			if(flashCounter >= 0)
 			{
 				int flashX = nests.get(flashFeederIndex*2).getX()-20;
 				int flashY = nests.get(flashFeederIndex*2).getY()-12;
 				g.drawImage(flashImage, flashX, flashY, null);
 				flashX = nests.get(flashFeederIndex*2+1).getX()-20;
 				flashY = nests.get(flashFeederIndex*2+1).getY()-12;
 				g.drawImage(flashImage, flashX, flashY, null);
 				flashCounter --;
 				if(flashCounter == 1)
 					cameraFlashDone();
 			}
 		}
 		
 		// Draw the parts robot
 		if (isFactoryProductionManager) {
 			final Graphics2D g3 = (Graphics2D)g.create();
 			g3.rotate(Math.toRadians(360-partsRobot.getAngle()), partsRobot.getX()+partsRobot.getImageWidth()/2, partsRobot.getY()+partsRobot.getImageHeight()/2);
 			// Draw items partsRobot is carrying
 			partsRobot.paint(g3);
 			g3.dispose();
 		}
 		
 		// Draw the Gantry Robot
 		if (isGantryRobotManager || isFactoryProductionManager) {
 			final Graphics2D g4 = (Graphics2D)g.create();
 			g4.rotate(Math.toRadians(360-gantryRobot.getAngle()), gantryRobot.getX()+gantryRobot.getImageWidth()/2, gantryRobot.getY()+gantryRobot.getImageHeight()/2);
 			gantryRobot.paint(g4);
 			// Draw bin gantryRobot is carrying
 			
 			g4.dispose();
 		}
 		
 
 		// Draw messages
 		Graphics2D gs = (Graphics2D)g;
 		if(messageCounter > 0 && isFactoryProductionManager)
 		{
 			for(int i = 0; i < messageList.size(); i++)
 			{
 				Composite comp;
 				if(messageCounter > 300)
 					comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(400f-messageCounter)/125);
 				else
 					comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f);
 				gs.setComposite(comp);
 				gs.setFont(messageFont.deriveFont(24.0f));
 				gs.drawString(messageList.get(i),80,i*28+40);
 			}
 		}
 		if(messageCounter >= 400)
 			messageCounter = -1;
 		if(messageCounter > 0)
 			messageCounter ++;
 	}
 	
 	public boolean isKitAssemblyManager() {
 		return isKitAssemblyManager;
 	}
 	
 	public boolean isLaneManager() {
 		return isLaneManager;
 	}
 	
 	public boolean isGantryRobotManager() {
 		return isGantryRobotManager;
 	}
 	
 	public boolean isFactoryProductionManager() {
 		return isFactoryProductionManager;
 	}
 	
 	/**
 	 * Moves the components that need moving
 	 */
 	public void actionPerformed(ActionEvent arg0) {
 			
 	}
 	// sets speed for specified lane
 	public void setLaneSpeedBottom(int laneIndex,int lanenum,int speed) {
 		//lane[laneIndex].setLaneSpeed(lanenum,speed);
 	}
 	// sets speed for specified lane
 	public void setLaneSpeedTop(int laneIndex,int lanenum,int speed) {
 		//lane[laneIndex].setLaneSpeed(lanenum,speed);
 	}
 	// sets amplitude for specified lane
 	public void setLaneAmplitude(int laneIndex,int lanenum,int amplitude) {
 		//lane[laneIndex].setLaneAmplitude(lanenum, amplitude);
 	}
 	
 	public void drawString(String s)
 	{
 		if(messageList.size() == MAX_MESSAGES)
 			messageList.remove(0);
 		messageList.add(s);
 		messageCounter = 1;
 	}
 }
