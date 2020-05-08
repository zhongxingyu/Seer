 package state;
 
 import java.awt.event.*;
 import java.util.concurrent.Semaphore;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TimerTask;
 
 import javax.swing.Timer;
 import state.transducers.*;
 import gui.*;
 import agents.*;
 import agents.interfaces.*;
 import agents.test.mock.MockNest;
 import javax.swing.JOptionPane;
 
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
 	
 	public ArrayList<Part>			partList;
 	public List<GUI_Part>			guiPartList = Collections.synchronizedList(new ArrayList<GUI_Part>());
 	public ArrayList<Kit>			kitList;
 	public ArrayList<GUI_Kit>		guiKitList;
 
 	public Transducer				transducer;
 
 	//Part A declarations here.
 
 	public FCSAgent					fcs;
 	public KitVisionAgent			kitVision;    
 
     public ConveyorAgent			conveyor;
 	public GUI_Conveyor				guiConveyor;
 	public KitRobotAgent			kitRobot;
 	public GUI_KitRobot				guiKitRobot;
 
 	public GUI_KitStand getGUIKitStand(){
 		return guiKitStand;
 	}
 	public GUI_Conveyor getGUIConveyor(){
 		return guiConveyor;
 	}
 	public GUI_KitRobot getGUIKitRobot(){
 		return guiKitRobot;
 	}
 	
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
			//c.paintComponent();
 			c.updateGraphics();
 		}
 	}
 	
 	public FactoryState() {		
 		//transducer = new Transducer();
 		//transducer.register(this, TChannel.GUI);
 		
 		compList = new ArrayList<GUI_Component>();
 		fpmDrawList = Collections.synchronizedList(new ArrayList<Drawing>());
 		kamDrawList = Collections.synchronizedList(new ArrayList<Drawing>());
 		gmDrawList = Collections.synchronizedList(new ArrayList<Drawing>());
 		lmDrawList = Collections.synchronizedList(new ArrayList<Drawing>());
 		
 		partList = new ArrayList<Part>();
 		guiPartList = new ArrayList<GUI_Part>();
 		kitList = new ArrayList<Kit>();
 		guiKitList = new ArrayList<GUI_Kit>();
 
 		nests = new ArrayList<Nest>(8);
 		nestList = new ArrayList<NestAgent>(8);
 		guiNestList = new ArrayList<GUI_Nest>(8);
 		kitStand = new KitStand();
 		guiKitStand = new GUI_KitStand(150, 120);
 		
 		partRobot = new PartRobotAgent("PartRobot", nests, kitStand, this);
 		guiPartRobot = new GUI_PartRobot(this, partRobot, 0, 0);
 		
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
 		kitVision.setKitRobot(kitRobot);
 		conveyor.setKitRobot(kitRobot);
 		conveyor.setConveyorGUI(guiConveyor);
 		
 		addToList(guiConveyor, ListOwner.KAM);
 		addToList(guiKitStand, ListOwner.KAM);
 		addToList(guiKitRobot, ListOwner.KAM);
 		
 		//Part B
 		
 		partVision = new PartVisionAgent("PartCamera");
 		
 		addNests();
 		
 		addToList(guiPartRobot, ListOwner.KAM);
 		
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
 		
 		
 		//delete below
 		// test simulation for v0
 
 		Map<String,Integer> kitConfig1 = new HashMap<String,Integer>();
 		kitConfig1.put("clown", 2);
 		kitConfig1.put("angler", 2);
 		kitConfig1.put("puffer",4);
 		//Kit sampleKit1 = new Kit(kitConfig1, 0);
 		//GUI_Kit gk1 = new GUI_Kit(sampleKit1, 100, 100*0);
 		//guiKitStand.addkit(gk1, 0);
 		
 		Map<String,Integer> kitConfig2 = new HashMap<String,Integer>();
 		kitConfig2.put("clown", 1);
 		kitConfig2.put("angler", 1);
 		kitConfig2.put("puffer",2);
 		//Kit sampleKit2 = new Kit(kitConfig2, 1);		
 		//GUI_Kit gk2 = new GUI_Kit(sampleKit2, 100, 100*1);
 		//guiKitStand.addkit(gk2, 1);
 	
 		fcs.msgCreateKit(kitConfig1);   	
 		//fcs.msgCreateKit(kitConfig2);   	
 
 	
 
 		//kitStand.insertEmptyKit(sampleKit1);
 		//kitStand.insertEmptyKit(sampleKit2);
    		//partRobot.msgMakeKits();
 
    		// delete above
 		
 		//Start agent threads...
 		
 		fcs.startThread();
 		conveyor.startThread();
 		kitRobot.startThread();
 		kitVision.startThread();
 		
 		partVision.startThread();
 		partRobot.startThread();
 		
 		for(NestAgent nest : nestList)
 			nest.startThread();
 		
 		gantryRobot.startThread();
 		
    		// lane 0 becomes clown
    		// lane 1 becomes angler
    		// lane 2 becomes puffer
 
 		for(int i=0; i<8; i++)
 			nestList.get(0).msgHereIsPart(new Part("clown"));
 		
 		for(int i=0; i<15; i++)
 			nestList.get(1).msgHereIsPart(new Part("angler"));
 		
 		for(int i=0; i<10; i++)
 			nestList.get(2).msgHereIsPart(new Part("puffer"));
 				
 		//la la la part c demo code.
 		
 		runDemo();
 		
 		
 		//Finally, make the timer for FactoryState and start it.
 		new Timer(33, this).start();
 	}
 	
 	void addNests() {
 		for(int i=0; i<4; i++) {
 			GUI_NestPair p = new GUI_NestPair(400, 2 * i * GUI_Nest.HEIGHT + 120, this);
 			
 			addToList(p.getCamera(), ListOwner.KAM, ListOwner.LM);
 			
 			//Nest 1
 			NestAgent n1 = new NestAgent("Nest " + (i*2), "DummyType", partRobot, p.getNest1(), partVision);
 			p.getNest1().setNestAgent(n1);
 			nests.add(n1);
 			nestList.add(n1);
 			guiNestList.add(p.getNest1());
 			
 			addToList(p.getNest1(), ListOwner.KAM, ListOwner.LM);
 			
 			//Nest 2
 			NestAgent n2 = new NestAgent("Nest " + (i*2+1), "DummyType", partRobot, p.getNest2(), partVision);
 			p.getNest2().setNestAgent(n2);
 			nests.add(n2);
 			nestList.add(n2);
 			guiNestList.add(p.getNest2());
 			
 			addToList(p.getNest2(), ListOwner.KAM, ListOwner.LM);
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
 			int y = 130 + 140 * i;
 			
 			//Set location of feeder
 			gf.setLocation(x, y);
 			
 			x = 545;
 			y = 130 + 140 * i;
 			
 			//Set locations of lanes
 			gl1.setCoordinates(x, y);
 			gf.setTopLane(gl1);
 			gf.setBotLane(gl2);
 			
 			y += 65;
 			
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
 			
 			fa.startThread();
 			la1.startThread();
 			la2.startThread();
 		}
 				
 		guiBin.setX(900);
 		guiBin.setY(250);
 		
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
 	
 	/*
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args)
 	{
 		if(channel == TChannel.GUI)
 		{
 			// Part B
 			if(event == TEvent.SendPart)
 			{
 				Map<Part, Integer> parts = (Map<Part, Integer>) args[0];
 				doPickUpParts(parts);
 			}
 
 		}
 	}*/
 	
 	//*** DoXXX API ***
 	public void doPickUpParts(Map<Part, Integer> parts) {
         System.out.println("=================================================================================================");
         System.out.println("=============================DO PICK UP PARTS CALLED============================================");
         System.out.println("=================================================================================================");
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
 		
 		//this should be called by the GUI once we do guiPartRobot.doTransferParts...
         //transducer.fireEvent(TChannel.Agents, TEvent.DonePickingUpParts, null);
 
 	}
 	
 	//part c demo code
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
 				//System.out.println("Making request for 5 clown parts (10 are in a bin)");
 				laneList.get(0).msgRequestParts("clown", 5, nests.get(0));
 			}
 		}, 2000);
 
 		utilTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {				
 				//System.out.println("Making Lane3 request for 4 puffer parts (10 are in a bin)");
 				laneList.get(3).msgRequestParts("puffer", 4, nests.get(3));
 			}
 		}, 10000);
 		
 		utilTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {				
 				//System.out.println("Making Lane1 request for 9 angler parts (10 are in a bin)");
 				laneList.get(1).msgRequestParts("angler", 9, nests.get(1));
 			}
 		}, 30000);
 
 		utilTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				//System.out.println("Making Lane0 request for 6 more clown parts (10 are in a bin)");
 				laneList.get(0).msgRequestParts("clown", 6, nests.get(0));
 			}
 		}, 50000);
 		
 		utilTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {				
 				//System.out.println("Making Lane3 request for 8 angler parts (10 are in a bin)");
 				laneList.get(3).msgRequestParts("angler", 8, nests.get(3));
 			}
 		}, 60000);
 		
 		/*kitConfig.put("clown", 2);
 kitConfig.put("angler", 3);
 kitConfig.put("puffer",3);*/
 	}
 	
 }
