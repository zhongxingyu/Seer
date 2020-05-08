 package gui.panels;
 
 import engine.agent.BigOnlineConveyorFamilyImp;
 import engine.agent.BinRobotAgent;
 import engine.agent.GeneralConveyorAgent;
 import engine.agent.OfflineWorkstationAgent;
 import engine.agent.OnlineWorkstationAgent;
 import engine.agent.SmallOnlineConveyorFamilyImp;
 import engine.agent.TruckAgent;
 import engine.agent.david.misc.ConveyorFamilyEntity;
 import engine.agent.evan.ConveyorFamilyImplementation;
 import engine.agent.tim.misc.ConveyorFamilyImp;
 import gui.drivers.FactoryFrame;
 import gui.test.DavidsOfflineCFIntegrationTest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 import shared.Glass;
 import shared.enums.MachineType;
 import shared.interfaces.NonnormBreakInteraction;
 import transducer.Transducer;
 
 /**
  * The FactoryPanel is highest level panel in the actual kitting cell. The FactoryPanel makes all the back end components, connects them to the GuiComponents in the DisplayPanel. It is responsible for
  * handling communication between the back and front end.
  */
 @SuppressWarnings("serial")
 public class FactoryPanel extends JPanel {
 	private enum RunMode{ OFFLINE_CF_TEST, FINAL_SUBMISSION }
 	private static final RunMode RUN_MODE = RunMode.FINAL_SUBMISSION;
 //	private static final RunMode RUN_MODE = RunMode.OFFLINE_CF_TEST;
 	
 	/** The frame connected to the FactoryPanel */
 	private FactoryFrame parent;
 	
 	/** The control system for the factory, displayed on right */
 	private ControlPanel cPanel;
 	
 	/** The graphical representation for the factory, displayed on left */
 	private DisplayPanel dPanel;
 	
 	/** Allows the control panel to communicate with the back end and give commands */
 	private Transducer transducer;
 	
 	/**
 	 * THE AGENTS
 	 */
 	// Initial robot agent
 	private BinRobotAgent binRobot;
 	
 	/* Arrays of components for the nonnormative break interactions. */
 	private List<NonnormBreakInteraction> conveyors;
 	private List<NonnormBreakInteraction> popups;
 	private List<OnlineWorkstationAgent> onlineWorkstations;
 	private List<OfflineWorkstationAgent> offlineWorkstations;
 	
 	/* ConveyorFamilies & accompanying workstation */
 	
 	// Cutter
 	private BigOnlineConveyorFamilyImp cutterFamily;
 	
 	// Breakout
 	private SmallOnlineConveyorFamilyImp breakoutFamily;
 	
 	// Manual Breakout
 	private BigOnlineConveyorFamilyImp manualBreakoutFamily;
 	
 	// Drill - Evan's
 	private OfflineWorkstationAgent drillWorkstation[];
 	private ConveyorFamilyImplementation drillFamily;
 	
 	// CrossSeamer - Tim's
 	private OfflineWorkstationAgent crossSeamerWorkstation[];
 	private ConveyorFamilyImp crossSeamerFamily;
 	
 	// Grinder - David's
 	private OfflineWorkstationAgent grinderWorkstation[];
 	private ConveyorFamilyEntity grinderFamily;
 	
 	// Washer
 	private BigOnlineConveyorFamilyImp washerFamily;
 	
 	// Painter
 	private SmallOnlineConveyorFamilyImp painterFamily;
 	
 	// UV Lamp
 	private BigOnlineConveyorFamilyImp lampFamily;
 	
 	// Oven
 	private BigOnlineConveyorFamilyImp ovenFamily;
 	
 	// TRUCK
 	private TruckAgent truck;
 	
 	/**
 	 * Constructor links this panel to its frame
 	 */
 	public FactoryPanel(FactoryFrame fFrame) {
 		parent = fFrame;
 		
 		// initialize transducer
 		transducer = new Transducer();
 		transducer.startTransducer();
 		
 		// use default layout
 		// dPanel = new DisplayPanel(this);
 		// dPanel.setDefaultLayout();
 		// dPanel.setTimerListeners();
 		
 		// initialize and run
 		this.initialize();
 		this.initializeBackEnd();
 	}
 
 	/**
 	 * Initializes all elements of the front end, including the panels, and lays them out
 	 */
 	private void initialize() {
 		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 		
 		// initialize control panel
 		cPanel = new ControlPanel(this, transducer);
 		
 		// initialize display panel
 		dPanel = new DisplayPanel(this, transducer); // gui popups and such in here
 		
 		// add panels in
 		// JPanel tempPanel = new JPanel();
 		// tempPanel.setPreferredSize(new Dimension(830, 880));
 		// this.add(tempPanel);
 		
 		this.add(dPanel);
 		this.add(cPanel);
 	}
 
 	/**
 	 * Feel free to use this method to start all the Agent threads at the same time
 	 */
 	private void initializeBackEnd() {
 		// ===========================================================================
 		// Initialize and start Agent threads
 		// ===========================================================================
 
 		if (RUN_MODE == RunMode.FINAL_SUBMISSION) {
 			/* Instantiate Agents */
 			javax.swing.Timer timer = parent.getTimer(); // needed for GeneralConveyorAgent
 			
 			/* Initialize components. */
 			conveyors = new ArrayList<NonnormBreakInteraction>(15);
 			popups = new ArrayList<NonnormBreakInteraction>(3);
 			onlineWorkstations = new ArrayList<OnlineWorkstationAgent>(7);
 			offlineWorkstations = new ArrayList<OfflineWorkstationAgent>(6);
 			
 			// Initial robot that has the glasses
 			binRobot = new BinRobotAgent("Bin Robot", transducer);
 			binRobot.setTracePanel(cPanel.getTracePanel());
 			cPanel.setBinRobot(binRobot);
 			
 			// Cutter
 			cutterFamily = createBigOnlineFamily(MachineType.CUTTER, transducer, 0, timer);
 			
 			// Breakout
 			breakoutFamily = createSmallOnlineFamily(MachineType.BREAKOUT, transducer, 2, timer);
 			
 			// Manual Breakout
 			manualBreakoutFamily = createBigOnlineFamily(MachineType.MANUAL_BREAKOUT, transducer, 3, timer);
 			
 			// Drill
 			drillWorkstation = new OfflineWorkstationAgent[2];
 			{ // create drill conveyor family
 				engine.agent.evan.ConveyorAgent c = new engine.agent.evan.ConveyorAgent("Drill conveyor", transducer, 5);
 				engine.agent.evan.PopupAgent p = new engine.agent.evan.PopupAgent("Drill popup", c, drillWorkstation, MachineType.DRILL, transducer, 0);
 				
 				drillFamily = new ConveyorFamilyImplementation(c, p);
 				
 				conveyors.add(c);
 				popups.add(p);
 			}
 			
 			for (int i = 0; i < 2; ++i) {
 				drillWorkstation[i] = new OfflineWorkstationAgent(MachineType.DRILL.toString() + i, MachineType.DRILL, i, transducer);
 				drillWorkstation[i].setPopupWorkstationInteraction(drillFamily);
 				drillWorkstation[i].setTracePanel(cPanel.getTracePanel());
 				offlineWorkstations.add(drillWorkstation[i]);
 			}
 			
 			// Cross Seamer
 			crossSeamerWorkstation = new OfflineWorkstationAgent[2];
 			for (int i = 0; i < 2; ++i){
 				crossSeamerWorkstation[i] = new OfflineWorkstationAgent(MachineType.CROSS_SEAMER.toString() + i, MachineType.CROSS_SEAMER, i, transducer);
 				crossSeamerWorkstation[i].setTracePanel(cPanel.getTracePanel());
 				offlineWorkstations.add(crossSeamerWorkstation[i]);
 			}
 			crossSeamerFamily = new ConveyorFamilyImp("Cross Seamer Family", transducer, "Sensors", 12, 13, "Conveyor", 6, "PopUp", 1, crossSeamerWorkstation, MachineType.CROSS_SEAMER);
 			for (int i = 0; i < 2; ++i)
 				crossSeamerWorkstation[i].setPopupWorkstationInteraction(crossSeamerFamily.getPopUp());
 			
 			// add components
 			conveyors.add(crossSeamerFamily.getConveyor());
 			popups.add(crossSeamerFamily.getPopUp());
 			
 			// Grinder
 			// 1. create grinder conveyor family and its internals
 			grinderWorkstation = new OfflineWorkstationAgent[2];
 			for (int i = 0; i < 2; ++i) {
 				grinderWorkstation[i] = new OfflineWorkstationAgent(MachineType.GRINDER.toString() + i, MachineType.GRINDER, i, transducer);
 				grinderWorkstation[i].setTracePanel(cPanel.getTracePanel());
 				offlineWorkstations.add(grinderWorkstation[i]);
 			}
 			grinderFamily = new ConveyorFamilyEntity(transducer, 7, 2, grinderWorkstation[0], grinderWorkstation[1]);
 			for (int i = 0; i < 2; ++i)
 				grinderWorkstation[i].setPopupWorkstationInteraction(grinderFamily);
 			// 2. add pointers of conveyor agents and popup agents to list
 			{
 				engine.agent.david.interfaces.Conveyor c = grinderFamily.getConveyor();
 				engine.agent.david.interfaces.Popup p = grinderFamily.getPopup();
 					
 				conveyors.add(c);
 				popups.add(p);
 			}
 			
 			// Washer
 			washerFamily = createBigOnlineFamily(MachineType.WASHER, transducer, 8, timer);
 
 			// Painter
 			painterFamily = createSmallOnlineFamily(MachineType.PAINT, transducer, 10, timer);
 
 			// UV Lamp
 			lampFamily = createBigOnlineFamily(MachineType.UV_LAMP, transducer, 11, timer);
 
 			// Oven
 			ovenFamily = createBigOnlineFamily(MachineType.OVEN, transducer, 13, timer);
 			
 			// TRUCK
 			truck = new TruckAgent("Truck", transducer);
 			truck.setTracePanel(cPanel.getTracePanel());
 			
 			// Connect them!
 			binRobot.setNextLineComponent(cutterFamily);
 			cutterFamily.setPreviousLineComponent(binRobot);
 			
 			cutterFamily.setNextLineComponent(breakoutFamily);
 			breakoutFamily.setPreviousLineComponent(cutterFamily);
 			
 			breakoutFamily.setNextLineComponent(manualBreakoutFamily);
 			manualBreakoutFamily.setPreviousLineComponent(breakoutFamily);
 			
 			manualBreakoutFamily.setNextLineComponent(drillFamily);
 			drillFamily.setPreviousLineComponent(manualBreakoutFamily);
 			
 			drillFamily.setNextLineComponent(crossSeamerFamily);
 			crossSeamerFamily.setPrevCF(drillFamily);
 			
 			crossSeamerFamily.setNextCF(grinderFamily);
 			grinderFamily.setPreviousLineComponent(crossSeamerFamily);
 			
 			grinderFamily.setNextLineComponent(washerFamily);
 			washerFamily.setPreviousLineComponent(grinderFamily);
 			
 			washerFamily.setNextLineComponent(painterFamily);
 			painterFamily.setPreviousLineComponent(washerFamily);
 			
 			painterFamily.setNextLineComponent(lampFamily);
 			lampFamily.setPreviousLineComponent(painterFamily);
 			
 			lampFamily.setNextLineComponent(ovenFamily);
 			ovenFamily.setPreviousLineComponent(lampFamily);
 			
 			ovenFamily.setNextLineComponent(truck);
 			truck.setPrevLineComponent((GeneralConveyorAgent)conveyors.get(conveyors.size() - 1));
 			
 			// Set things in motion!
//			createInitialGlasses();
 			startAgentThreads();
 		} else if (RUN_MODE == RunMode.OFFLINE_CF_TEST) {
 			System.err.println("Running in OFFLINE TEST MODE");
 //			TimsOfflineCFIntegrationTest tTest = new TimsOfflineCFIntegrationTest(transducer);
 			DavidsOfflineCFIntegrationTest dTest = new DavidsOfflineCFIntegrationTest(transducer);
 		}
 		
 		System.out.println("Backend initialization finished.");
 	}
 	
 	/* This method creates a BigOnlineConveyorFamily and places components in NonnormBreakInteraction arrays. */
 	private BigOnlineConveyorFamilyImp createBigOnlineFamily(MachineType type, Transducer trans, int startConveyorIndex, Timer guiTimer) {
 		GeneralConveyorAgent start = new GeneralConveyorAgent(type.toString() + " start conveyor", trans, startConveyorIndex, guiTimer);
 		GeneralConveyorAgent end = new GeneralConveyorAgent(type.toString() + " end conveyor", trans, startConveyorIndex + 1, guiTimer);
 		OnlineWorkstationAgent ws = new OnlineWorkstationAgent(type.toString() + " workstation", type, trans);
 		
 		start.setTracePanel(cPanel.getTracePanel());
 		end.setTracePanel(cPanel.getTracePanel());
 		ws.setTracePanel(cPanel.getTracePanel());
 		
 		conveyors.add(start);
 		onlineWorkstations.add(ws);
 		conveyors.add(end);
 		return new BigOnlineConveyorFamilyImp(start, end, ws);
 	}
 	
 	/* This method creates a SmallOnlineConveyorFamily and places components in NonnormBreakInteraction arrays. */
 	private SmallOnlineConveyorFamilyImp createSmallOnlineFamily(MachineType type, Transducer trans, int convIndex, Timer guiTimer) {
 		GeneralConveyorAgent c = new GeneralConveyorAgent(type.toString() + " conveyor", trans, convIndex, guiTimer);
 		OnlineWorkstationAgent ws = new OnlineWorkstationAgent(type.toString() + " workstation", type, trans);
 		
 		c.setTracePanel(cPanel.getTracePanel());
 		ws.setTracePanel(cPanel.getTracePanel());
 		
 		conveyors.add(c);
 		onlineWorkstations.add(ws);
 		return new SmallOnlineConveyorFamilyImp(c, ws);
 	}
 
 	/**
 	 * Create glasses of various types.
 	 */
 	private void createInitialGlasses() {
 		// Create some glasses to be run through the glassline, and give them to the initial robot (the bin robot)
 		List<Glass> glasses = new ArrayList<Glass>();
 				
 		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, MachineType.DRILL, MachineType.CROSS_SEAMER,
 				MachineType.GRINDER, MachineType.OVEN}));
 		glasses.add(new Glass(new MachineType[] { MachineType.CUTTER, MachineType.BREAKOUT, /*MachineType.MANUAL_BREAKOUT,*/
 				MachineType.DRILL, MachineType.CROSS_SEAMER, MachineType.GRINDER, MachineType.WASHER, MachineType.PAINT,
 				MachineType.UV_LAMP, MachineType.OVEN}));
 		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, MachineType.DRILL,
 				MachineType.GRINDER, MachineType.OVEN}));
 		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, MachineType.CROSS_SEAMER,
 				MachineType.GRINDER, MachineType.OVEN}));
 		glasses.add(new Glass(new MachineType[] { MachineType.CUTTER, MachineType.BREAKOUT,
 				MachineType.GRINDER, MachineType.WASHER, MachineType.PAINT,
 				MachineType.UV_LAMP, MachineType.OVEN}));
 		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, MachineType.OVEN}));
 		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, MachineType.GRINDER, MachineType.OVEN}));
 		glasses.add(new Glass(new MachineType[] { MachineType.CUTTER, MachineType.BREAKOUT,
 				MachineType.GRINDER, MachineType.WASHER, MachineType.PAINT,
 				MachineType.UV_LAMP, MachineType.OVEN}));
 		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, 
 				MachineType.GRINDER, MachineType.OVEN}));
 
 //		glasses.add(new Glass(new MachineType[] { MachineType.DRILL, MachineType.CROSS_SEAMER }));
 //		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER }));
 //		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER }));
 //		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER}));
 //		glasses.add(new Glass(new MachineType[] { MachineType.CROSS_SEAMER}));
 //		glasses.add(new Glass(new MachineType[] { }));
 		
 		binRobot.seedGlasses(glasses);
 	}
 	
 	private void startAgentThreads() {
 		binRobot.startThread();
 		cutterFamily.startThreads();
 		breakoutFamily.startThreads();
 		manualBreakoutFamily.startThreads();
 		drillFamily.startThreads();
 		for (int i = 0; i < 2; ++i)
 			drillWorkstation[i].startThread();
 		crossSeamerFamily.startThreads();
 		for (int i = 0; i < 2; ++i)
 			crossSeamerWorkstation[i].startThread();
 		grinderFamily.startThreads();
 		for (int i = 0; i < 2; ++i)
 			grinderWorkstation[i].startThread();
 		washerFamily.startThreads();
 		painterFamily.startThreads();
 		lampFamily.startThreads();
 		ovenFamily.startThreads();
 		truck.startThread();
 	}
 	
 	/* Break a conveyor. */
 	public void breakConveyor(boolean stop, int i) {
 		conveyors.get(i).msgGUIBreak(stop);
 	}
 	
 	/* Break a popup. */
 	public void breakPopup(boolean stop, int i) {
 		popups.get(i).msgGUIBreak(stop);
 	}
 	
 	/* Break an onlineWorkstation. */
 	public void breakOnlineWorkstation(boolean stop, int i) {
 		onlineWorkstations.get(i).msgGUIBreak(stop);
 	}
 	
 	/* Break an offlineWorkstation. */
 	public void breakOfflineWorkstation(boolean stop, int i) {
 		//TODO tell the popup
 		offlineWorkstations.get(i).msgGUIBreak(stop);
 	}
 	
 	/* Break the truck. */
 	public void breakTruck(boolean stop) {
 		truck.msgGUIBreak(stop);
 	}
 
 	public void breakSensor(boolean b, int value) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void breakGlass(boolean b, int value) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Returns the parent frame of this panel
 	 * 
 	 * @return the parent frame
 	 */
 	public FactoryFrame getGuiParent() {
 		return parent;
 	}
 
 	/**
 	 * Returns the control panel
 	 * 
 	 * @return the control panel
 	 */
 	public ControlPanel getControlPanel() {
 		return cPanel;
 	}
 
 	/**
 	 * Returns the display panel
 	 * 
 	 * @return the display panel
 	 */
 	public DisplayPanel getDisplayPanel() {
 		return dPanel;
 	}
 }
