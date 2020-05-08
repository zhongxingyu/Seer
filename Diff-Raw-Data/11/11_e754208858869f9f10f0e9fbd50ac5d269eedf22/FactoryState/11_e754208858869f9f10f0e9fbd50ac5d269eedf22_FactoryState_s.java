 package state;
 
 import java.awt.event.*;
 import java.io.File;
 import java.util.concurrent.Semaphore;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TimerTask;
 
 import javax.swing.Timer;
 
 import net.Server;
 import net.managers.*;
 import state.transducers.*;
 import gui.*;
 import agents.*;
 import agents.interfaces.*;
 
 public class FactoryState implements ActionListener {
 	
 	/*
 	 * README!!!
 	 * Factory State rules:
 	 * 
 	 * 1. All objects in FactoryState that are displayed should have a GUI
 	 * object declaration immediately after its declaration.
 	 * 2. Everything should be public--this is our global state, it makes no
 	 * sense to have it as private.
 	 * 3. Everything needs to be initialized IN THE SAME ORDER that it is
 	 * declared.
 	 * 4. Make things neat, this is code we all have to work with.
 	 */
 
 	//*** Variable Declarations ***
 	
 	//This component list should only have TOP-LEVEL objects. Any objects
 	//"inside" others are owned by that object and should be sub-drawings (this
 	//should only be kits and parts).
 	public ArrayList<GUI_Component>	compList;
 	public List<Drawing>			fpmDrawList, kamDrawList, gmDrawList, lmDrawList;
 	
 	public List<PartConfig>		    partConfigList;
 	public String 					partConfigFilename = "data/partConfigList.ser";
 	public ArrayList<Part>			partList;
 	public List<GUI_Part>			guiPartList = Collections.synchronizedList(new ArrayList<GUI_Part>());
 	public ArrayList<Kit>			kitList;
 	public ArrayList<GUI_Kit>		guiKitList;
 
 	public List<KitConfig>			kitConfigList;
 	public List<OrderConfig>		orderConfigList;
 	public boolean					isReadyForOrder;
 	
 	public Transducer				transducer;
 
 	//Part A declarations here.
 
 	public FCSAgent					fcs;
 	public KitVisionAgent			kitVision;    
 
     public ConveyorAgent			conveyor;
 	public GUI_Conveyor				guiConveyor;
 	public KitRobotAgent			kitRobot;
 	public GUI_KitRobot				guiKitRobot;
 	
 	// not used right now, but for inspection camera
 	public GUI_Camera				inspectionCamera;	
 	
     public Semaphore partsRobotFillsKits = new Semaphore(0);
     public void partsRobotFillsKitLockRelease(){ partsRobotFillsKits.release(); }
 	
     //Part B declarations here.
 	
 	public PartVisionAgent			partVision;
 	
 	public ArrayList<Nest> 			nests;
 	public ArrayList<NestAgent>		nestList;
 	public ArrayList<GUI_Nest>		guiNestList;
 	public KitStand					kitStand;
 	public GUI_KitStand				guiKitStand;
 	public PartRobotAgent			partRobot;
 	public GUI_PartRobot			guiPartRobot;
 	
 	
 	//Part C declarations here.
 	
 	public ArrayList<FeederAgent>	feederList;
 	public ArrayList<GUI_Feeder>	guiFeederList;
 	public GantryAgent				gantryRobot;
 	public GUI_GantryRobot			guiGantryRobot;
 	public ArrayList<LaneAgent>		laneList;
 	public ArrayList<GUI_Lane>		guiLaneList; 
 
 	//No agent?
 	public GUI_Bin 					guiBin;
 	
 	//*** Methods ***
 	
 	/*
 	 * This is the master actionPerformed() method that calls updateGraphics on
 	 * all top level objects. All top level objects are responsible for the
 	 * objects they own.
 	 */
 	public void actionPerformed(ActionEvent ae) {
 		for(GUI_Component c : compList) {
 			c.paintComponent();
 			c.updateGraphics();
 		}
 		
 		//Anything that relies on a timer should go here as well.
 		synchronized(orderConfigList) {
 			if(isReadyForOrder && orderConfigList.size() != 0) {
 				DoAddOrder();
 				isReadyForOrder = false;
 			}
 		}
 	}
 	
 	public FactoryState() {		
 
 		compList = new ArrayList<GUI_Component>();
 		fpmDrawList = Collections.synchronizedList(new ArrayList<Drawing>());
 		kamDrawList = Collections.synchronizedList(new ArrayList<Drawing>());
 		gmDrawList = Collections.synchronizedList(new ArrayList<Drawing>());
 		lmDrawList = Collections.synchronizedList(new ArrayList<Drawing>());
 		
 		// Load up partConfigList list
 		boolean exists = (new File(partConfigFilename)).exists();
 		partConfigList = Collections.synchronizedList(exists ? GUI_Part.loadData(partConfigFilename) : new ArrayList<PartConfig>()); 
 		partList = new ArrayList<Part>();
 		guiPartList = new ArrayList<GUI_Part>();
 
 		kitList = new ArrayList<Kit>();
 		guiKitList = new ArrayList<GUI_Kit>();
 		
 		boolean kitExists = (new File("data/kitConfigList.ser")).exists();
 		kitConfigList = Collections.synchronizedList(kitExists ? GUI_Kit.loadConfigData() : new ArrayList<KitConfig>());
 		orderConfigList = Collections.synchronizedList(new ArrayList<OrderConfig>());
 		isReadyForOrder = true;
 		
 		nests = new ArrayList<Nest>(8);
 		nestList = new ArrayList<NestAgent>(8);
 		guiNestList = new ArrayList<GUI_Nest>(8);
 		kitStand = new KitStand();
 		guiKitStand = new GUI_KitStand(130, 180);
 		
 		partRobot = new PartRobotAgent("PartRobot", nests, kitStand, this);
 		guiPartRobot = new GUI_PartRobot(this, partRobot, 270, 0);
 		
 		//Part A
 		fcs = new FCSAgent();
 		kitVision = new KitVisionAgent("InspectionCamera", kitStand);
 
 	    conveyor = new ConveyorAgent("ConveyorAgent", fcs, this);
 		guiConveyor = new GUI_Conveyor();
 		fcs.setConveyor(conveyor);
 
 		kitRobot = new KitRobotAgent("KitRobotAgent", conveyor, kitVision, kitStand, partRobot, this);
 		guiKitRobot = new GUI_KitRobot(guiConveyor, guiKitStand);
 		kitRobot.setGUIKitRobot(guiKitRobot);
 		kitRobot.setGUIKitStand(guiKitStand);
 		partRobot.setKitRobot(kitRobot);
 		kitVision.setKitRobot(kitRobot);
 		conveyor.setKitRobot(kitRobot);
 		conveyor.setConveyorGUI(guiConveyor);
 		
 		addToList(guiConveyor, ListOwner.KAM);
 		addToList(guiKitStand, ListOwner.KAM);
 		addToList(guiKitRobot, ListOwner.KAM);
 		
 		//Part C
 		
 		feederList = new ArrayList<FeederAgent>();
 		guiFeederList = new ArrayList<GUI_Feeder>();
 		gantryRobot = new GantryAgent("The Gantry");
 		guiGantryRobot = new GUI_GantryRobot(gantryRobot);
 		laneList = new ArrayList<LaneAgent>();
 		guiLaneList = new ArrayList<GUI_Lane>();
 		guiBin = new GUI_Bin();
 		
 		addToList(guiBin, ListOwner.GM);
 		setupGantry();
 		
 		for(GUI_Feeder feeder : guiFeederList)
 			addToList(feeder, ListOwner.GM, ListOwner.LM);
 		
 		addToList(guiGantryRobot, ListOwner.GM);
 		
 		for(GUI_Lane lane : guiLaneList)
 			addToList(lane, ListOwner.LM);
 	
 		//Part B
 		
 		partVision = new PartVisionAgent("PartCamera", this);
 		addNests();		
 		addToList(guiPartRobot, ListOwner.KAM);	
 
 		
 		// test simulation for v0
 		Map<String,Integer> kitConfig1 = new HashMap<String,Integer>();
 		kitConfig1.put("clown", 2);
 		kitConfig1.put("angler", 2);
 		kitConfig1.put("puffer",4);
 		
 		Map<String,Integer> kitConfig2 = new HashMap<String,Integer>();
 		kitConfig2.put("clown", 1);
 		kitConfig2.put("angler", 1);
 		kitConfig2.put("puffer",2);
 
 		
 		/**
 		 * Created Kits to Simulate
 		 */
 		//fcs.msgCreateKit(kitConfig1);   	
 		//fcs.msgCreateKit(kitConfig2);  
 
 		
 		//Start agent threads...		
 		conveyor.startThread();
 		kitRobot.startThread();
 		kitVision.startThread();
 		partRobot.startThread();
 		partVision.startThread();
 		for(NestAgent nest : nestList) nest.startThread();
 		for(LaneAgent lane: laneList) lane.startThread();
 		for(FeederAgent feeder: feederList) feeder.startThread();
 		gantryRobot.startThread();
 		fcs.startThread();
 
 
 		/* Test for testing purge */
 		/*for(int i=0; i<8; i++)
 			nestList.get(0).msgHereIsPart("clownfish");
 		
 		for(int i=0; i<15; i++)
 			nestList.get(1).msgHereIsPart("anglerfish");
 		
 		for(int i=0; i<10; i++)
 			nestList.get(2).msgHereIsPart("pufferfish");*/
 		
 		
 		//runDemo(); // test for Part C
 		
 		//Finally, make the timer for FactoryState and start it.
 		new Timer(Server.SYNCRATE, this).start();
 	}
 	
 	void addNests() {
 		for(int i=0; i<4; i++) {
 			GUI_NestPair p = new GUI_NestPair(400, 2 * i * GUI_Nest.HEIGHT + 120, this);
 						
 			//Nest 1
 			NestAgent n1 = new NestAgent("Nest " + (i*2), (i*2), "notype", partRobot, p.getNest1(), partVision, laneList.get(i*2));
 			p.getNest1().setNestAgent(n1);
 			nests.add(n1);
 			nestList.add(n1);
 			guiNestList.add(p.getNest1());
 						
 			//Nest 2
 			NestAgent n2 = new NestAgent("Nest " + (i*2+1), (i*2+1), "notype", partRobot, p.getNest2(), partVision, laneList.get(i*2+1));
 			p.getNest2().setNestAgent(n2);
 			nests.add(n2);
 			nestList.add(n2);
 			guiNestList.add(p.getNest2());
 			
 			addToList(p, ListOwner.KAM, ListOwner.LM);
 		}
 	}
 	
 	void setupGantry() {
 		for (int i=0; i<4; i++) {
 			
 			//Two lanes
 			LaneAgent la1 = new LaneAgent("Lane" + i*2);
 			GUI_Lane gl1 = new GUI_Lane(la1);
 			la1.setguiLane(gl1);
 			LaneAgent la2 = new LaneAgent("Lane" + (i*2 + 1));
 			GUI_Lane gl2 = new GUI_Lane(la2);
 			la2.setguiLane(gl2);
 			
 			//Feeder
 			FeederAgent fa = new FeederAgent("Feeder" + i, la1, la2);
 			GUI_Feeder gf = new GUI_Feeder(fa);
 			fa.setGuiFeeder(gf);
 			
 			//Set up coordinates of each lane/feeder combo.
 			int x = 745;
 			int y = 120 + 140 * i;
 			
 			//Set location of feeder
 			gf.setLocation(x, y);
 			
 			x = 545;
 			y = 120 + 140 * i;
 			
 			//Set locations of lanes
 			gl1.setCoordinates(x, y);
 			gf.setTopLane(gl1);
 			gf.setBotLane(gl2);
 			
 			y += 69;
 			
 			gl2.setCoordinates(x, y);
 			
 			//Set the supplier for the two lanes and the feeder
 			la1.msgSetSupplier(fa);
 			la2.msgSetSupplier(fa);
 			fa.msgSetSupplier(gantryRobot);
 			
 			//Add these things to the lists
 			feederList.add(fa);
 			guiFeederList.add(gf);
 			laneList.add(la1);
 			laneList.add(la2);
 			guiLaneList.add(gl1);
 			guiLaneList.add(gl2);
 		}
 				
 		guiBin.setX(900);
 		guiBin.setY(100);
 		
 		guiGantryRobot.setCoordinates(900, 500);
 		guiGantryRobot.setHomeCoordinates(900, 500);
 		guiGantryRobot.setBinCoordinates(guiBin.getX(), guiBin.getY());
 		guiGantryRobot.setFeeder(guiFeederList.get(1));
 		gantryRobot.setGuiGantry(guiGantryRobot);
 	}
 	
 	synchronized void addToList(GUI_Component g, ListOwner... listOwners) {
 		//Add to the requested lists
 		for(ListOwner l : listOwners) {
 			switch(l) {
 			case GM:
 				gmDrawList.add(g.myDrawing);
 				break;
 			case KAM:
 				kamDrawList.add(g.myDrawing);
 				break;
 			case LM:
 				lmDrawList.add(g.myDrawing);
 				break;
 			}
 		}
 		
 		//Then add to FPM's list (always).
 		fpmDrawList.add(g.myDrawing);
 		compList.add(g);
 	}
 	
 	//*** DoXXX API ***
 	public void doPickUpParts(Map<Part, Integer> parts) {        
 		ArrayList<GUI_Part> partsToGet = new ArrayList<GUI_Part>();
 		ArrayList<Integer> nestIndices = new ArrayList<Integer>();
 		synchronized(guiPartList) {
 			for (GUI_Part p : guiPartList) {
 				if (parts.containsKey(p.agentPart)) {	                                
 					partsToGet.add(p);
 					nestIndices.add(parts.get(p.agentPart));
 				}
 
 			}
 		}
 		guiPartRobot.doTransferParts(partsToGet, nestIndices);
 	}	
 	
 	public void DoAddOrder() {
 		OrderConfig config = orderConfigList.remove(0);
 		
 		System.out.println("Config: " + config);
 		
 		for(int i=0; i<config.number; i++)
 			fcs.msgCreateKit(config.kitConfig.components);
 	}
 	
 	public void DoFinishedOrder() {
 		
 		//If the queue is empty, set a boolean indicating that fcs is waiting.
 		if(orderConfigList.size() == 0)
 			isReadyForOrder = true;
 		else
 			DoAddOrder();
 	}
 }
