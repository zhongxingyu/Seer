 package gui.panels;
 
 import gui.components.GUIBin;
 import gui.components.GUIBreakout;
 import gui.components.GUIComponentOffline;
 import gui.components.GUIComponentOnline;
 import gui.components.GUIConveyor;
 import gui.components.GUIGlass;
 import gui.components.GUIManualBreakout;
 import gui.components.GUIPopUp;
 import gui.components.GUISensor;
 import gui.components.GUIShuttle;
 import gui.components.GUITruck;
 import gui.components.GuiComponent;
 import gui.test.GuiTestSM;
 import gui.test.JoshGuiTestSM;
 
 import java.awt.Dimension;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 
 import shared.ImageIcons;
 import shared.enums.ConveyorDirections;
 import shared.enums.MachineType;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 /**
  * This is the main graphics panel that takes care of layout of the items in
  * factory.
  */
 @SuppressWarnings("serial")
 public class DisplayPanel extends JLayeredPane {
 
 	public final static Dimension size = new Dimension(1000, 880);
 	
 	/**
 	 * Defining different layers:
 	 * 1- Background Image layer = layer 0
 	 * 2- the layer that all fixed machinery are at = layer 1
 	 * 3- the layer where kits are at = layer 2
 	 * 4- the layer where parts are at normally = layer 3
 	 * 5- the layer where kits are at in movement= layer 4
 	 * 6- the layer where parts are in kit that moves = layer 5
 	 * 7- the layer where kit robot is at = layer 6
 	 * 8- the layer where bins are at = layer 7
 	 * 9- the layer where gantry robot operates = layer 8
 	 * 10- the layer where part robot operates and temporary layer for parts on robot = layer 9
 	 * 11- Ceiling layer = layer 10
 	 * 12- Drag and drop layer = layer 11
 	 * 
 	 */
 	public final static Integer BG_LAYER = 0, STATIONARY_MACHINERY_LAYER = 1,
 			KIT_LAYER = 2, PART_NORMAL_LAYER = 3, KIT_IN_MOVE_LAYER=4, PART_IN_MOVE_LAYER=5, KITBOT_LAYER = 6,
 			BIN_LAYER = 1, GANTRY_LAYER = 8, PARTBOT_LAYER = 9, CEILING_LAYER = 10, DND_LAYER = 11,
 			
 			GROUND_LAYER = 0, CONVEYOR_LAYER = 2, SENSOR_LAYER = 7,MACHINE_LAYER = 12,POPUP_LAYER = 13,
 			PART_LAYER = 15, ROBOT_LAYER = 19, PART_POPUP_LAYER = 20, ONLINE_MACHINE_LAYER = 25, ROOF_LAYER = 100;
 	
 	/** The FactoryPanel linked to this panel */
 	private FactoryPanel parent;
 	
 	/** The Transducer linked to this panel */
 	private Transducer transducer;
 	
 	public static ImageIcons iconLibraries = new ImageIcons("imageicons");
 
 	int conveyorCount =0;
 	int popUpCount = 0;
 	int machineCount = 0;
 	int transferCount = 0;
 	
 	//GuiTestSM test;
 	JoshGuiTestSM test;
 	
 	ArrayList<GUIGlass> activePieces = new ArrayList<GUIGlass>();
 	
 	public ArrayList<GUIGlass> getActivePieces() {
 		return activePieces;
 	}
 
 	public void setActivePieces(ArrayList<GUIGlass> activePieces) {
 		this.activePieces = activePieces;
 	}
 
 	GuiComponent currentComponent;
 	
 	
 	/**
 	 * Creates the entire display panel, including components
 	 */
 	public DisplayPanel(FactoryPanel fPanel, Transducer linkedTransducer) {
 		parent = fPanel;
 		transducer = linkedTransducer;
 		parent.getGuiParent().getTimer().stop();
 		this.setLayout(null);
 		setPreferredSize(size);
 		setBackGroundImage();
 		setupComponents();
 		parent.getGuiParent().getTimer().start();
 	}
 
 	/**
 	 * Setting up background image
 	 */
 	public void setBackGroundImage() {
 		ImageIcon icon = null;
 		try {
 			icon = new ImageIcon(ImageIO.read(new File("imageicons"
 					+ File.separator + "realFactoryBackgroundNew.png")));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		JLabel background = new JLabel(icon);
 		background.setSize(icon.getIconWidth(), icon.getIconHeight());
 		background.setLocation(0, 0);
 		this.add(background, BG_LAYER);
 	}
 	
 	/**
 	 * Setting up individual componets
 	 */
 	public void setupComponents() {
 		int numConveyors = 0;
 		createBin(400,600);
 		
 		createConveyor(400,720,ConveyorDirections.LEFT, numConveyors++);
 		
 		
 		createOnlineComponent(currentComponent.getX()-(int)(ImageIcons.getIconList("cutter").get(0).getIconWidth()/2),currentComponent.getCenterY(),MachineType.CUTTER,ConveyorDirections.LEFT);
 		
 		createConveyor(currentComponent.getX()-(int)(GUIConveyor.conveyorRight.getIconWidth()/2),currentComponent.getCenterY(),ConveyorDirections.LEFT, numConveyors++);
 		
 		createTransfer(currentComponent.getX()-(int)(GUIShuttle.leftConnector.getIconWidth()/2),currentComponent.getCenterY(),ConveyorDirections.UP);
 		
 		//UP
 		
 		createConveyor(currentComponent.getCenterX(),currentComponent.getY()-(int)(GUIConveyor.conveyorUp.getIconHeight()/2),ConveyorDirections.UP, numConveyors++);
 		
 		createBreakout(currentComponent.getCenterX(),currentComponent.getY()-(int)(ImageIcons.getIconList("breakOut").get(0).getIconHeight()/2));
 		
 		createConveyor(currentComponent.getCenterX(),currentComponent.getY()-(int)(GUIConveyor.conveyorUp.getIconHeight()/2),ConveyorDirections.UP, numConveyors++);
 		
 		createManualBreakout(currentComponent.getCenterX(),currentComponent.getY()-(int)(ImageIcons.getIconList("manualBreakout").get(0).getIconHeight()/2));
 		
 		createConveyor(currentComponent.getCenterX(),currentComponent.getY()-(int)(GUIConveyor.conveyorUp.getIconHeight()/2),ConveyorDirections.UP, numConveyors++);
 		
 		createTransfer(currentComponent.getCenterX(),currentComponent.getY()-(int)(GUIShuttle.leftConnector.getIconHeight()/2),ConveyorDirections.RIGHT);
 		
 		//RIGHT
 		
 		createConveyor(currentComponent.getX()+currentComponent.getIcon().getIconWidth()+(int)(GUIConveyor.conveyorRight.getIconWidth()/2),currentComponent.getCenterY(),ConveyorDirections.RIGHT, numConveyors++);
 		
 		createPopUp(currentComponent.getX()+currentComponent.getIcon().getIconWidth()+(int)(GUIShuttle.leftConnector.getIconHeight()/2)-5,currentComponent.getCenterY());
 		
 		createOfflineComponent(currentComponent.getCenterX(),currentComponent.getY()-(int)(ImageIcons.getIconList("drill").get(0).getIconHeight()/2),MachineType.DRILL,1);
 		createOfflineComponent(currentComponent.getCenterX(),currentComponent.getY()+currentComponent.getIcon().getIconHeight()+(int)(ImageIcons.getIconList("drill").get(0).getIconHeight()/2),MachineType.DRILL,2);
 		
 		createConveyor(currentComponent.getX()+currentComponent.getIcon().getIconWidth()+(int)(GUIConveyor.conveyorRight.getIconWidth()/2),currentComponent.getCenterY(),ConveyorDirections.RIGHT, numConveyors++);
 		
 		createPopUp(currentComponent.getX()+currentComponent.getIcon().getIconWidth()+(int)(GUIShuttle.leftConnector.getIconHeight()/2)-5,currentComponent.getCenterY());
 		createOfflineComponent(currentComponent.getCenterX(),currentComponent.getY()-(int)(ImageIcons.getIconList("crossSeamer").get(0).getIconHeight()/2),MachineType.CROSS_SEAMER,1);
 		createOfflineComponent(currentComponent.getCenterX(),currentComponent.getY()+currentComponent.getIcon().getIconHeight()+(int)(ImageIcons.getIconList("crossSeamer").get(0).getIconHeight()/2),MachineType.CROSS_SEAMER,2);
 	
 		
 		createConveyor(currentComponent.getX()+currentComponent.getIcon().getIconWidth()+(int)(GUIConveyor.conveyorRight.getIconWidth()/2),currentComponent.getCenterY(),ConveyorDirections.RIGHT, numConveyors++);
 		
 		createPopUp(currentComponent.getX()+currentComponent.getIcon().getIconWidth()+(int)(GUIShuttle.leftConnector.getIconHeight()/2)-5,currentComponent.getCenterY());
 		createOfflineComponent(currentComponent.getCenterX(),currentComponent.getY()-(int)(ImageIcons.getIconList("grinder").get(0).getIconHeight()/2),MachineType.GRINDER,1);
 		createOfflineComponent(currentComponent.getCenterX(),currentComponent.getY()+currentComponent.getIcon().getIconHeight()+(int)(ImageIcons.getIconList("grinder").get(0).getIconHeight()/2),MachineType.GRINDER,2);
 	
 		createConveyor(currentComponent.getX()+currentComponent.getIcon().getIconWidth()+(int)(GUIConveyor.conveyorRight.getIconWidth()/2),currentComponent.getCenterY(),ConveyorDirections.RIGHT, numConveyors++);
 		
 		createOnlineComponent(currentComponent.getX()+currentComponent.getIcon().getIconWidth()+(int)(ImageIcons.getIconList("washer").get(0).getIconHeight()/2),currentComponent.getCenterY(),MachineType.WASHER,ConveyorDirections.RIGHT);
 		
 		createConveyor(currentComponent.getX()+currentComponent.getIcon().getIconWidth()+(int)(GUIConveyor.conveyorRight.getIconWidth()/2),currentComponent.getCenterY(),ConveyorDirections.RIGHT, numConveyors++);
 		
 		createTransfer(currentComponent.getX()+currentComponent.getIcon().getIconWidth()+(int)(GUIShuttle.leftConnector.getIconHeight()/2),currentComponent.getCenterY(),ConveyorDirections.DOWN);
 		
 		//DOWN
 		
 		createConveyor(currentComponent.getCenterX(),currentComponent.getY()+currentComponent.getIcon().getIconHeight()+(int)(GUIConveyor.conveyorDown.getIconHeight()/2),ConveyorDirections.DOWN, numConveyors++);
 		
 		createOnlineComponent(currentComponent.getCenterX(),currentComponent.getY()+currentComponent.getIcon().getIconHeight()+(int)(ImageIcons.getIconList("paint").get(0).getIconHeight()/2),MachineType.PAINT,ConveyorDirections.DOWN);
 		
 		createConveyor(currentComponent.getCenterX(),currentComponent.getY()+currentComponent.getIcon().getIconHeight()+(int)(GUIConveyor.conveyorDown.getIconHeight()/2),ConveyorDirections.DOWN, numConveyors++);
 		
 		createOnlineComponent(currentComponent.getCenterX(),currentComponent.getY()+currentComponent.getIcon().getIconHeight()+(int)(ImageIcons.getIconList("paint").get(0).getIconHeight()/2),MachineType.UV_LAMP,ConveyorDirections.DOWN);
 		
 		createConveyor(currentComponent.getCenterX(),currentComponent.getY()+currentComponent.getIcon().getIconHeight()+(int)(GUIConveyor.conveyorDown.getIconHeight()/2),ConveyorDirections.DOWN, numConveyors++);
 		
 		createTransfer(currentComponent.getCenterX(),currentComponent.getY()+currentComponent.getIcon().getIconHeight()+(int)(GUIShuttle.leftConnector.getIconHeight()/2),ConveyorDirections.LEFT);
 		
 		//LEFT
 		
 		createConveyor(currentComponent.getX()-(int)(GUIConveyor.conveyorRight.getIconWidth()/2),currentComponent.getCenterY(),ConveyorDirections.LEFT, numConveyors++);
 		
 		createOnlineComponent(currentComponent.getX()-(int)(ImageIcons.getIconList("oven").get(0).getIconWidth()/2),currentComponent.getCenterY(),MachineType.OVEN,ConveyorDirections.LEFT);
 		
 		createConveyor(currentComponent.getX()-(int)(GUIConveyor.conveyorRight.getIconWidth()/2),currentComponent.getCenterY(),ConveyorDirections.LEFT, numConveyors++);
 
 		createTruck(currentComponent.getX()-(int)(ImageIcons.getIconList("truck").get(0).getIconWidth()/2),currentComponent.getCenterY());
 		
 
 		/*
 			NOTE: You can observe the factory work on one piece of glass without agents by leaving the below line alone. Comment it out when you are ready to start working.
 		*/
 		//System.err.println("****** GUI TEST IS RUNNING. Comment out 2 lines in DisplayPanel.java (near line 212, at the end of 'setupComponents()') in order to stop GUI test from running in the future.");
 
 		//test = new GuiTestSM(transducer);
 		//test = new JoshGuiTestSM(transducer, parent);
 
 
 //		test = new GuiTestSM(transducer);
 		
 	}
 	
 	private void createPopUp(int x,int y)
 	{
 		GUIPopUp popUp = new GUIPopUp(this,transducer);
 		popUp.setParent(this);
 		popUp.setIndex(popUpCount);
 		popUpCount++;
 		popUp.setCenterLocation(x, y);
 		this.add(popUp,POPUP_LAYER);
 		parent.getGuiParent().getTimer().addActionListener(popUp);
 		currentComponent.addNextComponent(popUp);
 		currentComponent = popUp;
 		popUp.setTransducer(transducer);
 	
 	}
 	
 	private void createBin(int x,int y)
 	{
 		GUIBin guiBin = new GUIBin(transducer);
 		guiBin.setParent(this);
 		guiBin.setLocation(x, y);
 		this.add(guiBin,BIN_LAYER);
 		parent.getGuiParent().getTimer().addActionListener(guiBin);
 		currentComponent = guiBin;
 		
 	}
 	
 	private void createBreakout(int x,int y)
 	{
 		GUIBreakout guiBreakout = new GUIBreakout(transducer);
 		guiBreakout.setParent(this);
 		guiBreakout.setCenterLocation(x, y);
 		guiBreakout.setName("breakout");
 		this.add(guiBreakout,MACHINE_LAYER);
 		parent.getGuiParent().getTimer().addActionListener(guiBreakout);
 		currentComponent.addNextComponent(guiBreakout);
 		currentComponent = guiBreakout;
 		guiBreakout.setTransducer(transducer);
 
 	}
 	
 	private void createOfflineComponent(int x,int y, MachineType type, int machineNumber)
 	{
 		GUIComponentOffline offlineComponent = new GUIComponentOffline(type,transducer);
 		offlineComponent.setParent(this);
 		offlineComponent.setName(type.toString().toLowerCase()+" "+machineNumber);
 		offlineComponent.setCenterLocation(x, y);
 		this.add(offlineComponent,MACHINE_LAYER);
 		offlineComponent.setTransducer(transducer);
 		offlineComponent.setIndex(machineCount);
 		offlineComponent.addNextComponent(currentComponent);
 		parent.getGuiParent().getTimer().addActionListener(offlineComponent);
 		((GUIPopUp)currentComponent).setPairedMachineType(type);
 		((GUIPopUp)currentComponent).setPairedMachine(offlineComponent,machineCount);
 		machineCount = ++machineCount%2;
 	}
 	
 	private void createOnlineComponent(int x, int y, MachineType type,ConveyorDirections direction)
 	{
 		GUIComponentOnline onlineComponent = new GUIComponentOnline(type,transducer);
 		onlineComponent.setParent(this);
 		onlineComponent.setCenterLocation(x, y);
 		onlineComponent.setName(type.toString().toLowerCase());
 		onlineComponent.setDirection(direction);
 		this.add(onlineComponent, MACHINE_LAYER);
 		parent.getGuiParent().getTimer().addActionListener(onlineComponent);
 		currentComponent.addNextComponent(onlineComponent);
 		currentComponent = onlineComponent;
 		onlineComponent.setTransducer(transducer);
 		
 
 	}
 	
 	private void createConveyor(int x,int y,ConveyorDirections direction, int nextIndex)
 	{
 		GUIConveyor conveyor = new GUIConveyor(this,direction,3,7,nextIndex,transducer);
 		conveyor.setCenterLocation(x, y);
 		this.add(conveyor,CONVEYOR_LAYER);
 		parent.getGuiParent().getTimer().addActionListener(conveyor);
 	
 		if(direction == ConveyorDirections.UP)
 		{
 			createSensor(conveyor.getCenterX(),conveyor.getCenterY()+(2*conveyor.getIcon().getIconHeight())/5, (conveyorCount)*2);
 			createSensor(conveyor.getCenterX(),conveyor.getCenterY()-(2*conveyor.getIcon().getIconHeight())/5, (conveyorCount)*2+1);
 			
 		}
 		else if(direction == ConveyorDirections.DOWN)
 		{
 			createSensor(conveyor.getCenterX(),conveyor.getCenterY()-(2*conveyor.getIcon().getIconHeight())/5, (conveyorCount)*2);
 			createSensor(conveyor.getCenterX(),conveyor.getCenterY()+(2*conveyor.getIcon().getIconHeight())/5, (conveyorCount)*2+1);
 		}
 		else if(direction == ConveyorDirections.LEFT)
 		{
 			createSensor(conveyor.getCenterX()+(2*conveyor.getIcon().getIconWidth())/5,conveyor.getCenterY(), (conveyorCount)*2);
 			createSensor(conveyor.getCenterX()-(2*conveyor.getIcon().getIconWidth())/5,conveyor.getCenterY(), (conveyorCount)*2+1);
 		}
 		else if(direction == ConveyorDirections.RIGHT)
 		{
 			createSensor(conveyor.getCenterX()-(2*conveyor.getIcon().getIconWidth())/5,conveyor.getCenterY(), (conveyorCount)*2);
 			createSensor(conveyor.getCenterX()+(2*conveyor.getIcon().getIconWidth())/5,conveyor.getCenterY(), (conveyorCount)*2+1);
 		}
 		conveyorCount++;
 		
 		conveyor.setTransducer(transducer);
 		conveyor.setParent(this);
 		
 		if(currentComponent!=null)
 		{
 			currentComponent.addNextComponent(conveyor);
 		}
 		currentComponent = conveyor;
 	
 	}
 	
 	private void createManualBreakout(int x,int y)
 	{
 		GUIManualBreakout mBreakout = new GUIManualBreakout(transducer);
 		mBreakout.setParent(this);
 		mBreakout.setCenterLocation(x, y);
 		mBreakout.setName("manual breakout");
 		this.add(mBreakout,MACHINE_LAYER);
 		parent.getGuiParent().getTimer().addActionListener(mBreakout);
 		currentComponent.addNextComponent(mBreakout);
 		currentComponent = mBreakout;
 		mBreakout.setTransducer(transducer);
 		
 	}
 	
 	private void createSensor(int x,int y,int nextIndex)
 	{
 		GUISensor sensor = new GUISensor(transducer);
 		sensor.setParent(this);
 		sensor.setIndex(nextIndex);
 		sensor.setCenterLocation(x, y);
 		sensor.setGuiParent(this);
 		this.add(sensor,DND_LAYER);
 		parent.getGuiParent().getTimer().addActionListener(sensor);
 		sensor.setTransducer(transducer);
 		
 	}
 	
 	private void createTruck(int x,int y)
 	{
 		GUITruck truck = new GUITruck(transducer);
 		truck.setParent(this);
 		truck.setName("truck");
 		truck.setLocation(x, y);
 		this.add(truck,DND_LAYER);
 		parent.getGuiParent().getTimer().addActionListener(truck);
 		truck.setTransducer(transducer);
 		currentComponent.addNextComponent(truck);
 	}
 	
 	private void createTransfer(int x,int y,ConveyorDirections direction)
 	{
 		GUIShuttle connector = new GUIShuttle(direction,true);
 		connector.setParent(this);
 		connector.setName("transfer "+transferCount);
 		connector.setCenterLocation(x, y);
 		this.add(connector,CONVEYOR_LAYER);
 		parent.getGuiParent().getTimer().addActionListener(connector);
 		currentComponent.addNextComponent(connector);
 		currentComponent = connector;
 		
 	}
 	
 	/**
 	 * Returns the parent panel
 	 */
 	public FactoryPanel getParent()
 	{
 		return parent;
 	}
 }
