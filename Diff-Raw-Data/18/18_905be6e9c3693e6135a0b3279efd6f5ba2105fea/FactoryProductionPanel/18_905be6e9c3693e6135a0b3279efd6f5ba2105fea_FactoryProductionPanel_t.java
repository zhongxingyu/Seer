 
 //Minh La
 
 package factory.graphics;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.Timer;
 
 import factory.*;
 import factory.client.*;
 public class FactoryProductionPanel extends GraphicPanel implements ActionListener {
 	
 	// LANE MANAGER
 	/*GraphicLaneManagerClient client;
 	GraphicLaneManager [] lane;
 	
 	// KIT MANAGER
 	private FactoryProductionManager am; //The JFrame that holds this. Will be removed when gets integrated with the rest of the project
 	private GraphicKitBelt belt; //The conveyer belt
 	private GraphicKittingStation station; //The kitting station
 	private GraphicKittingRobot kitRobot;
 	public static final int WIDTH = 1100, HEIGHT = 720;
 	
 	// PARTS MANAGER
 	private ArrayList<Nest> nests;
 	PartsRobot partsRobot;
 	
 	// GANTRY
 	GantryRobot gantryRobot;*/
 	
 	public FactoryProductionPanel(JFrame FKAM) {
 		super();
 		isFactoryProductionManager = true;
 		
 		if (FKAM instanceof Client)
 			am = (Client)FKAM;
 		
 		belt = new GraphicKitBelt(0, 0, this);
 		station = new GraphicKittingStation(200, 191, this);
 		kitRobot = new GraphicKittingRobot(this, 70, 250);
 		
 		// Parts robot client
 		// Add 8 nests
 		nests = new ArrayList<Nest>();	
 		for(int i = 0; i < 8; i++)
 		{
 			Nest newNest = new Nest(510,i*80+50,0,0,0,0,75,75,"Images/nest3x3.png");
 			Random randomGen = new Random();
 			for(int j = 0; j < randomGen.nextInt(5)+4; j++)
 				newNest.addItem(new GraphicItem(20,20,"Images/eyesItem.png"));
 			nests.add(newNest);
 		}
 
 		lane = new GraphicLaneManager [4];
 		for (int i = 0; i < lane.length; i++)
 			lane[i] = new GraphicLaneManager(510, 160*i + 50, i, this);
 		
 		partsRobot = new PartsRobot(WIDTH/2-200,HEIGHT/2,0,5,5,10,100,100,"Images/robot1.png");
 		gantryRobot = new GantryRobot(WIDTH-150,HEIGHT/2,0,5,5,10,100,100,"Images/robot2.png");
 		
 		(new Timer(delay, this)).start();
 		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
 		this.setVisible(true);
 	}
 
 	public void paint(Graphics g) {
 		super.paint(g);
 	}
 	
 	public void newEmptyKit() {
 		//Adds a kit into the factory via conveyer belt
 		if (belt.kitin())
 			return;
 		belt.inKit();
 	}
 	
 	public void moveEmptyKitToSlot(int target) {
 		//Sends robot to pick up kit from belt and move to designated slot in the station
 		if (belt.pickUp() && !kitRobot.kitted() && station.getKit(target) == null) {
 			kitRobot.setFromBelt(true);
 			kitRobot.setStationTarget(target);
 		}
 	}
 	
 	public void moveKitToInspection(int target) {
 		//Sends robot to move kit from designated slot in the station to inspection station
 		if (!kitRobot.kitted() && station.getKit(target) != null) {
 			kitRobot.setCheckKit(true);
 			kitRobot.setStationTarget(target);
 		}
 	}
 	
 	public void takePictureOfInspectionSlot() {
 		//Triggers the camera flash
 		station.checkKit();
 	}
 	
 	public void dumpKitAtInspection() {
 		//Sends robot to move kit from inspection station to trash
 		if (!kitRobot.kitted() && station.getCheck() != null)
 			kitRobot.setPurgeKit(true);
 	}
 	
 	public void moveKitFromInspectionToConveyor() {
 		//Sends a kit out of the factory via conveyer belt
 		if (station.getCheck() != null && !kitRobot.kitted())
 			kitRobot.setFromCheck(true);
 	}
 	
 	public void cameraFlash(int nestIndex) {
 		flashCounter = 10;
		flashFeederIndex = nestIndex;
 	}
 	
 	public void moveGantryRobotToPickup(String path) {
 		//System.out.println("Moving");
 		gantryRobot.setState(0);
 		gantryRobot.setDestination(WIDTH-100,-100);
 	}
 	
 	public void moveGantryRobotToFeeder(int feederIndex) {
 		gantryRobot.setState(3);
 		gantryRobot.setDestinationFeeder(feederIndex);
 		gantryRobot.setDestination(lane[feederIndex].feederX+95, lane[feederIndex].feederY+15);
 	}
 	
 	public void movePartsRobotToNest(int nestIndex) {
 		partsRobot.setState(0);
 		partsRobot.adjustShift(5);
 		partsRobot.setDestination(nests.get(nestIndex-1).getX()-nests.get(nestIndex-1).getImageWidth()-10,nests.get(nestIndex-1).getY()-15);
 		partsRobot.setDestinationNest(nestIndex);
 	}
 	
 	public void movePartsRobotToKit(int kitIndex) {
 		partsRobot.setState(3);
 		partsRobot.setDestination(station.getX()+35,station.getY()-station.getY()%5);
 		partsRobot.setDestinationKit(kitIndex);
 	}
 	
 	public void movePartsRobotToCenter() {
 		partsRobot.setDestination(WIDTH/2-100, HEIGHT/2);
 	}
 	
 	public void feedLane(int laneNum){ //FEEDS THE LANE! Lane 0-7
 		/*//Testing for quick feed
 		lane[(laneNum) / 2].bin = new GraphicBin(new Part("eyes"));
 		lane[(laneNum) / 2].binExist = true;
 		//end Test*/
 		if(lane[(laneNum) / 2].binExist && lane[(laneNum) / 2].bin.getBinItems().size() > 0){
 			lane[(laneNum) / 2].laneStart = true;
 			lane[(laneNum) / 2].divergeUp = ((laneNum- 1) % 2 == 0);
 			lane[(laneNum) / 2].feederOn = true;
 		}
 		//System.out.println("bin size " + lane[(laneNum) / 2].bin.getBinItems().size());
 	}
 	
 	public void startLane(int laneNum){
 		lane[(laneNum) / 2].laneStart = true;
 	}
 	
 	public void switchLane(int laneNum){
 		lane[(laneNum) / 2].divergeUp = !lane[(laneNum) / 4].divergeUp;
 		lane[(laneNum) / 2].vY = -(lane[(laneNum) / 4].vY);
 	}
 	
 	public void switchFeederLane(int feederNum){
 		// MINH, CAN YOU MAKE THIS LIKE THE FUNCTION ABOVE, BUT BASED ON THE FEEDER NUMBER?
 		// thanks
 	}
 	
 	public void stopLane(int laneNum){
 		lane[(laneNum) / 2].laneStart = false;
 	}
 	
 	public void turnFeederOnLane(int laneNum){
 		lane[(laneNum) / 2].feederOn = true;
 	}
 	
 	public void turnFeederOffLane(int laneNum){
 		lane[(laneNum) / 2].feederOn = false;
 	}
 	
 	public void turnFeederOn(int feederNum){
 		lane[feederNum].feederOn = true;
 	}
 
 	public void turnFeederOff(int feederNum){
 		lane[feederNum].feederOn = false;
 	}
 	
 	public void purgeFeeder(int feederNum){ // takes in lane 0 - 4
 		lane[(feederNum)].bin = null;
 		lane[(feederNum)].binExist = false;
 		lane[(feederNum)].feederOn = false;
 	}
 	
 	public void purgeLane(int laneNum){
 		if((laneNum) % 2 == 0)
 			lane[(laneNum) / 2].lane1PurgeOn = true;
 		else
 			lane[(laneNum) / 2].lane2PurgeOn = true;
 		lane[(laneNum) / 2].feederOn = false;
 		lane[(laneNum) / 2].laneStart = false;
 	}
 	
 	//MINH, CAN YOU MAKE A PURGETOPLANE(INT FEEDERNUM) AND PURGEBOTTOMLANE(INT FEEDERNUM)
 	// ALSO PLZ MAKE EVERYTHING 0-BASED
 	
 	public void partsRobotStateCheck() {
 		// Has robot arrived at its destination?
 		//System.out.println(partsRobot.getState());
 		if(partsRobot.getState() == 1)		// partsRobot has arrived at nest
 		{
 			// Give item to partsRobot
 			if(partsRobot.getSize() < 4)
 			{
 				if (nests.get(partsRobot.getDestinationNest()-1).hasItem())
 					partsRobot.addItem(nests.get(partsRobot.getDestinationNest()-1).popItem());
 				partsRobot.setState(2);
 			}
 				partsRobotArrivedAtNest();
 		}
 		else if(partsRobot.getState() == 4)	// partsRobot has arrived at kitting station
 		{
 			for(int i = 0; i < partsRobot.getSize(); i++)
 				station.addItem(partsRobot.popItem(),partsRobot.getDestinationKit());
 			partsRobotArrivedAtStation();
 		}
 		else if(partsRobot.getState() == 6)
 		{
 			partsRobotArrivedAtCenter();
 		}
 	}
 	
 	public void gantryRobotStateCheck() {
 		if(gantryRobot.getState() == 1)
 		{
 			gantryRobotArrivedAtPickup();
 		}
 		else if(gantryRobot.getState() == 4)
 		{
 			lane[gantryRobot.getDestinationFeeder()].setBin(gantryRobot.takeBin());
 			gantryRobotArrivedAtFeeder();
 		}
 	}
 	
 	public void moveLanes() {
 		for (int i = 0; i < lane.length; i++)
 			lane[i].moveLane();
 	}
 	
 	public void actionPerformed(ActionEvent arg0) {
 		partsRobotStateCheck();
 		gantryRobotStateCheck();
 		
 		moveLanes();
 		
 		partsRobot.move();							// Update position and angle of partsRobot
 		gantryRobot.move();
 		belt.moveBelt(5);
 		kitRobot.moveRobot(5);
 		
 		repaint();		
 	}
 }
 	
