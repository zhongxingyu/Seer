 package state;
 
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.Timer;
 import gui.*;
 import agents.*;
 import agents.test.mock.MockNest;
 
 public class Factory_PartC extends JFrame implements ActionListener{
 	//All objects in FactoryState that are displayed should have a GUI object
 	//declaration immediately after its declaration.
 
 	//*** Variable Declarations ***
 	
 	/* 
 	 * JComponents
 	 */
 	
 	//JButton goToBin, goToFeeder, goToHome;
 
 	/*
 	 * Basic Stuffs
 	 *
 	 * * Parts
 	 * * Kits
 	 *
 	 */
 	
 	ArrayList<Part> partList;
 	ArrayList<GUI_Part> guiPartList;
 	
 	/*
 	 * Lane Assembly
 	 * 
 	 * * Feeder
 	 * * Bin
 	 * * Lane
 	 * * Gantry
 	 *
 	 */
 	
 	ArrayList<FeederAgent> feederAgents;
 	ArrayList<GUI_Feeder> guiFeeders;
 	GUI_Bin guiBin;
 	ArrayList<LaneAgent> laneAgents;
 	ArrayList<GUI_Lane> guiLanes;
 	GantryAgent	gantryAgent;
 	GUI_GantryRobot	guiGantryRobot;
 	
 	//Mock nests to receive parts from the lanes
 	ArrayList<MockNest> mockNests;
 
 	//*** Methods ***
 
 	public Factory_PartC() {
 		//Variables should be initialized STRICTLY IN THE ORDER THEY ARE
 		//DECLARED. This will make editing easier
 		
 //		partList = new ArrayList<Part>();
 //		guiPartList = new ArrayList<GUI_Part>();
 
 		//One Gantry
 		gantryAgent = new GantryAgent("The Gantry");
 		guiGantryRobot = new GUI_GantryRobot(gantryAgent);
		this.add(guiGantryRobot);
 		gantryAgent.setGuiGantry(guiGantryRobot);
 		
 		//Lists of agents for which we have more than 1
 		feederAgents = new ArrayList<FeederAgent>();
 		guiFeeders = new ArrayList<GUI_Feeder>();
 		laneAgents = new ArrayList<LaneAgent>();
 		guiLanes = new ArrayList<GUI_Lane>();
 		guiBin = new GUI_Bin();
 		
 		mockNests = new ArrayList<MockNest>();
 		
 		for (int x = 0; x < 4; x++)
 		{
 			//Two lanes
 			LaneAgent la1 = new LaneAgent("Lane" + x*2);
 			GUI_Lane gl1 = new GUI_Lane(la1);
 			la1.setguiLane(gl1);
 			LaneAgent la2 = new LaneAgent("Lane" + (x*2 + 1));
 			GUI_Lane gl2 = new GUI_Lane(la2);
 			la2.setguiLane(gl2);
 			
 			//Add mock nests for the lanes to dump into
 			mockNests.add(new MockNest("Nest " + (x*2), null));
 			mockNests.add(new MockNest("Nest " + (x*2 + 1), null));
 			
 			//Feeder
 			FeederAgent fa = new FeederAgent("Feeder" + x, la1, la2);
 			GUI_Feeder gf = new GUI_Feeder(fa);
 			fa.setGuiFeeder(gf);
 			
 			//Set locations of lane and feeder gui components
 			gf.setLocation(300, 25 + (125 * x));
 			gl1.setCoordinates(100, 30 + (125 * x));
		//	gf.setLane(gl1);
 			gl2.setCoordinates(100, 85 + (125 * x));
 			
 			//Set the supplier for the two lanes and the feeder
 			la1.msgSetSupplier(fa);
 			la2.msgSetSupplier(fa);
 			fa.msgSetSupplier(gantryAgent);
 			
 			//Add these things to the lists
 			feederAgents.add(fa);
 			guiFeeders.add(gf);
 			laneAgents.add(la1);
 			laneAgents.add(la2);
 			guiLanes.add(gl1);
 			guiLanes.add(gl2);
 			
 			fa.startThread();
 			la1.startThread();
 			la2.startThread();
 		}
 		
 		gantryAgent.startThread();
 		
 		guiBin.setX(400);
 		guiBin.setY(250);
 		
 		guiGantryRobot.setCoordinates(450, 500);
 		guiGantryRobot.setHomeCoordinates(450, 500);
 		guiGantryRobot.setBinCoordinates(guiBin.getX(), guiBin.getY());
 		guiGantryRobot.setFeeder(guiFeeders.get(1));
 		
 		setSize(500, 600);
 		setContentPane(new JPanel() {
 			public void paint(Graphics g)
 			{
 				for (GUI_Feeder feeder: guiFeeders)
 				{
					feeder.paintComponent();
 				}
 				for (GUI_Lane lane: guiLanes)
 				{
					lane.paintComponent();
 				}
 				guiBin.paintComponent(this, (Graphics2D)g);
 				guiGantryRobot.paintComponent(this, (Graphics2D)g);
 			}
 		});
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setResizable(false);
 		setTitle("Nemo Factory v0 Part C");
 		setVisible(true);
 		
 		//Runs the tests for the demo
 		runDemo();
 	}
 
 	
 	
 	@Override
 	public void actionPerformed(ActionEvent ae)
 	{
 		for (GUI_Feeder feeder: guiFeeders)
 		{
 			feeder.updateGraphics();
 		}
 		for (GUI_Lane lane: guiLanes)
 		{
 			lane.updateGraphics();
 		}
 		guiGantryRobot.updateGraphics();
 		if (guiGantryRobot.reachPartBin())
 			guiBin.grabbedByGantry();
 		if (guiGantryRobot.reachFeeder())
 		{
 			guiBin.placedInFeeder(guiGantryRobot.getFeeder());
 		}
 		repaint();
 	}
 	
 	public void runDemo()
 	{
 
 		Timer uiTimer = new Timer(50, this);
 		uiTimer.start();
 		
 		//For making future requests
 		java.util.Timer utilTimer = new java.util.Timer();
 		
 		
 		//Schedule these events for the demo
 		utilTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {				
 				System.out.println("Making request for 5 clown parts (10 are in a bin)");
 				laneAgents.get(0).msgRequestParts("clown", 5, mockNests.get(0));
 			}
 		}, 2000);
 
 		utilTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {				
 				System.out.println("Making Lane3 request for 4 puffer parts (10 are in a bin)");
 				laneAgents.get(3).msgRequestParts("puffer", 4, mockNests.get(3));
 			}
 		}, 10000);
 		
 		utilTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {				
 				System.out.println("Making Lane1 request for 9 angler parts (10 are in a bin)");
 				laneAgents.get(1).msgRequestParts("angler", 9, mockNests.get(1));
 			}
 		}, 30000);
 
 		utilTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				System.out.println("Making Lane0 request for 6 more clown parts (10 are in a bin)");
 				laneAgents.get(0).msgRequestParts("clown", 6, mockNests.get(0));
 			}
 		}, 50000);
 		
 		utilTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {				
 				System.out.println("Making Lane3 request for 8 angler parts (10 are in a bin)");
 				laneAgents.get(3).msgRequestParts("angler", 8, mockNests.get(3));
 			}
 		}, 60000);
 		
 		/*kitConfig.put("clown", 2);
 kitConfig.put("angler", 3);
 kitConfig.put("puffer",3);*/
 	}
 
 	public static void main(String[] args) {
 		Factory_PartC state = new Factory_PartC();		
 	}
 }
