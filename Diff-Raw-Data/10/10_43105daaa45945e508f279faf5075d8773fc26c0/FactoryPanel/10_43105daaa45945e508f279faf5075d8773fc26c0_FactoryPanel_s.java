 
 package gui.panels;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import engine.agent.shared.ConveyorFamilyOffline;
 import engine.agent.shared.ConveyorFamilyOnlineMachine;
 import engine.agent.shared.ConveyorFamily1;
 import engine.agent.shared.Glass;
 import engine.agent.shared.MachineAgent;
 import engine.ryanCF.agent.BinAgent;
 import gui.drivers.FactoryFrame;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 /**
  * The FactoryPanel is highest level panel in the actual kitting cell. The
  * FactoryPanel makes all the back end components, connects them to the
  * GuiComponents in the DisplayPanel. It is responsible for handing
  * communication between the back and front end.
  */
 @SuppressWarnings("serial")
 public class FactoryPanel extends JPanel
 {
 	/** The frame connected to the FactoryPanel */
 	private FactoryFrame parent;
 
 	/** The control system for the factory, displayed on right */
 	private ControlPanel cPanel;
 
 	/** The graphical representation for the factory, displayed on left */
 	private DisplayPanel dPanel;
 
 	/** Allows the control panel to communicate with the back end and give commands */
 	private Transducer transducer;
 
 	/**
 	 * Constructor links this panel to its frame
 	 */
 	public FactoryPanel(FactoryFrame fFrame)
 	{
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
 	 * Initializes all elements of the front end, including the panels, and lays
 	 * them out
 	 */
 	private void initialize()
 	{
 		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 
 		// initialize control panel
 		cPanel = new ControlPanel(this, transducer);
 
 		// initialize display panel
 		dPanel = new DisplayPanel(this, transducer);
 
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
 	private void initializeBackEnd()
 	{
 		// ===========================================================================
 		// TODO initialize and start Agent threads here
 		// ===========================================================================
 
 		
 		//Initializing Agents
 		BinAgent binAgent = new BinAgent(transducer);
 		ConveyorFamilyOnlineMachine cutterCF = new ConveyorFamilyOnlineMachine(0, TChannel.CUTTER, transducer);
 		MachineAgent cutter = new MachineAgent(TChannel.CUTTER, transducer);
 		ConveyorFamily1 cf1 = new ConveyorFamily1(transducer);	// with shuttle - need to be replaced. 
 		ConveyorFamilyOnlineMachine breakoutCF = new ConveyorFamilyOnlineMachine(2, TChannel.BREAKOUT, transducer);
 		MachineAgent breakout = new MachineAgent(TChannel.BREAKOUT, transducer);
 		ConveyorFamilyOnlineMachine manualBreakoutCF = new ConveyorFamilyOnlineMachine(3, TChannel.MANUAL_BREAKOUT, transducer);
 		MachineAgent manualBreakout = new MachineAgent(TChannel.MANUAL_BREAKOUT, transducer);
 		ConveyorFamilyOnlineMachine washerCF = new ConveyorFamilyOnlineMachine(8, TChannel.WASHER, transducer);
 		MachineAgent washer = new MachineAgent(TChannel.WASHER, transducer);
 		ConveyorFamilyOnlineMachine painterCF = new ConveyorFamilyOnlineMachine(10, TChannel.PAINTER, transducer);
 		MachineAgent painter = new MachineAgent(TChannel.PAINTER, transducer);
 		ConveyorFamilyOnlineMachine uvCF = new ConveyorFamilyOnlineMachine(11, TChannel.UV_LAMP, transducer);
 		MachineAgent uv = new MachineAgent(TChannel.UV_LAMP, transducer);
 		ConveyorFamilyOnlineMachine ovenCF = new ConveyorFamilyOnlineMachine(13, TChannel.OVEN, transducer);
 		MachineAgent oven = new MachineAgent(TChannel.OVEN, transducer);
 
 		//Linking all the agents
 		cutterCF.setMachine(cutter);
 		cutter.setConveyor(cutterCF.getConveyor());
 		cutter.setNextCF(cf1);
 		cf1.setMachine(cutter);
 
 		breakoutCF.setMachine(breakout);
 		breakout.setConveyor(breakoutCF.getConveyor());
 		breakout.setNextCF(manualBreakoutCF);
 		
 		manualBreakoutCF.setMachine(manualBreakout);
 		manualBreakout.setConveyor(manualBreakoutCF.getConveyor());
 //		manualBreakout.setNextCF(####);			//TODO have to add shuttle CF as nextCF
 
 		washerCF.setMachine(washer);
 		washer.setConveyor(washerCF.getConveyor());
 //		washer.setNextCF(####);			//TODO have to add shuttle CF as nextCF
 		
 		painterCF.setMachine(painter);
 		painter.setConveyor(painterCF.getConveyor());
 		painter.setNextCF(uvCF);
 		
 		uvCF.setMachine(uv);
 		uv.setConveyor(uvCF.getConveyor());
 //		uv.setNextCF(####);			//TODO have to add shuttle CF as nextCF
 		
 		ovenCF.setMachine(oven);
 		oven.setConveyor(uvCF.getConveyor());
 //		oven.setNextCF(####);			//TODO have to add truck CF as nextCF
 		
 		System.out.println("Back end initialization finished.");
 
 		//Starting agent threads inside cf groups. 
 		binAgent.startThread();
 		cutterCF.startThread();
 		cutter.startThread();
 		cf1.startThread(); //with shuttle need to be replaced
 		breakoutCF.startThread();
 		breakout.startThread();
 		manualBreakoutCF.startThread();
 		manualBreakout.startThread();
 		washerCF.startThread();
 		washer.startThread();
 		painterCF.startThread();
 		painter.startThread();
 		uvCF.startThread();
 		uv.startThread();
 		ovenCF.startThread();
 		oven.startThread();
 
 		List<Glass> tempGlassToProcess = new ArrayList<Glass>();
 		tempGlassToProcess.add(new Glass(false,false,false));
 		//temp creating list of parts before gui is implemented
 		
 //		//initialization of the three popup (for test) I started the thread in my conveyor family--Heidi
 //		ConveyorFamilyClass popup1 = new ConveyorFamilyClass(5,transducer,TChannel.DRILL);
 //		ConveyorFamilyClass popup2 = new ConveyorFamilyClass(6,transducer,TChannel.CROSS_SEAMER);
 //		ConveyorFamilyClass popup3 = new ConveyorFamilyClass(7,transducer,TChannel.GRINDER);
 //		
 //		popup1.setNextCF(popup2);
 //		popup2.setNextCF(popup3);
 //		popup2.setPreviousCF(popup1);
 //		popup3.setPreviousCF(popup2);
 //		Glass g = new Glass(false,true,false);
 //		popup1.msgHereIsGlass(g);
 		
 	}
 
 	/**
 	 * Returns the parent frame of this panel
 	 * 
 	 * @return the parent frame
 	 */
 	public FactoryFrame getGuiParent()
 	{
 		return parent;
 	}
 
 	/**
 	 * Returns the control panel
 	 * 
 	 * @return the control panel
 	 */
 	public ControlPanel getControlPanel()
 	{
 		return cPanel;
 	}
 
 	/**
 	 * Returns the display panel
 	 * 
 	 * @return the display panel
 	 */
 	public DisplayPanel getDisplayPanel()
 	{
 		return dPanel;
 	}
 }
