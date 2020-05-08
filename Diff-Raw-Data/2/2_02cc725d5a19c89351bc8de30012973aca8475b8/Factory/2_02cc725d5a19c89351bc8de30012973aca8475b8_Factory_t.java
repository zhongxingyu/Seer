 /*
  * This file is part of ShopFloorSimulator.
  * 
  * ShopFloorSimulator is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ShopFloorSimulator is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with ShopFloorSimulator.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.feup.sfs.factory;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Menu;
 import java.awt.Point;
 import java.awt.PopupMenu;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.Random;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.ToolTipManager;
 
 import com.feup.sfs.block.Block;
 import com.feup.sfs.block.BlockType;
 import com.feup.sfs.exceptions.FactoryInitializationException;
 import com.feup.sfs.facility.Conveyor;
 import com.feup.sfs.facility.Facility;
 import com.feup.sfs.facility.Machine;
 import com.feup.sfs.facility.Rail;
 import com.feup.sfs.facility.Rotator;
 import com.feup.sfs.facility.WarehouseIn;
 import com.feup.sfs.facility.WarehouseOut;
 import com.feup.sfs.modbus.ModbusSlave;
 import com.feup.sfs.record.PlayBack;
 import com.feup.sfs.record.Recorder;
 import com.feup.sfs.transformation.Tool;
 import com.feup.sfs.transformation.Transformation;
 import com.feup.sfs.warehouse.Warehouse;
 
 public class Factory extends JPanel implements ActionListener, KeyListener{
 	private static final long serialVersionUID = 1L;
 
 	private double pixelSize; 
 	private double blockSize; 
 	private double sensorRadius; 
 	private int simulationTime;
 
 	private ArrayList<Block> blocks = new ArrayList<Block>();
 	private ArrayList<Facility> facilities = new ArrayList<Facility>();
 	private ArrayList<Transformation> transformations = new ArrayList<Transformation>();
 	private Hashtable<Integer, Warehouse> warehouses = new Hashtable<Integer, Warehouse>();
 	private Hashtable<Integer, Tool> tools = new Hashtable<Integer, Tool>();
 	
 	private final int heigth;
 	private final int width;
 
 	private double conveyorSpeed;
 	private double rotationSpeed;
 
 	private int errorTime;
 
 	private double toolRotationSpeed;
 
 	private String floorColor;
 	
 	private static Factory instance;
 	
 	private static Recorder recorder = null;
 
 	private Facility popupfacility;
 	private PopupMenu popup;
 
 	private static PlayBack playback = null;
 	private static Random rng = new Random();
 	
 	public Factory(int width, int heigth, double blockSize, double pixelSize, int simulationTime, double conveyorSpeed, double sensorRadius, double rotationSpeed, int errorTime, double toolRotationSpeed, String floorColor, String recordFile, String playbackFile) throws FileNotFoundException {
 		this.width = width;
 		this.heigth = heigth;
 		this.blockSize = blockSize;
 		this.pixelSize = pixelSize;
 		this.simulationTime = simulationTime;
 		this.conveyorSpeed = conveyorSpeed;
 		this.sensorRadius = sensorRadius;
 		this.rotationSpeed = rotationSpeed;
 		this.errorTime = errorTime;
 		this.toolRotationSpeed = toolRotationSpeed;
 		this.floorColor = floorColor;
 		if (recordFile != null) recorder = new Recorder(recordFile);
 		if (playbackFile != null) playback = new PlayBack(playbackFile);
 		Factory.instance = this;
 		
 		setFocusable(true);
 		addKeyListener(this);
 	}
 
 	public void processMouseEvent(MouseEvent e) {
 	    if (e.isPopupTrigger()) {
 			popupfacility = null;
 	    	for (Facility facility : facilities) {
 				if (facility.getBounds().contains(new Point(e.getX(),e.getY()))) {
 					if (popup != null) remove(popup);
 					popupfacility = facility;
 					popup = new PopupMenu(facility.getName());
 					Menu blockMenu = new Menu("Add Block"); 
 					Menu actionMenu = new Menu("Actions");
 					popup.add(blockMenu);
 					popup.add(actionMenu);
 					Collection<String> actions = facility.getActions();
 					int bt = 1;
					while (bt <= BlockType.getNumberBlockTypes()) {
 						blockMenu.add(BlockType.getBlockType(bt).getName());
 						bt++;
 					}
 					for (String action : actions) actionMenu.add(action);
 					add(popup);
 					blockMenu.addActionListener(this);
 					actionMenu.addActionListener(this);
 					popup.show(this, e.getX(), e.getY());					
 				}
 			}
 	    }
 	    super.processMouseEvent(e);
 	}
 		
 	@Override
 	public Dimension getPreferredSize(){
 		return new Dimension((int)(width/pixelSize), (int)(heigth/pixelSize));
 	}
 	
 	public void addBlock(int type, double centerX, double centerY){
 		Block block = new Block(this, type, centerX, centerY);
 		blocks.add(block);
 	}
 
 	public void addFacility(Facility facility){
 		facilities.add(facility);
 	}
 	
 	public double getPixelSize() {
 		return pixelSize;
 	}
 	
 	public static void main(String args[]) throws FactoryInitializationException, FileNotFoundException, IOException{
 		JFrame frame = new JFrame("Shop Floor Simulator");
 		frame.setLayout(new FlowLayout());
 		
 		Properties properties = new Properties();
 		properties.load(new FileInputStream("plant.properties"));
 
 		String recordFile = null;
 		String playbackFile = null;
 		
 		if (args.length == 2 && args[0].equals("--record")) recordFile = args[1]; 
 		if (args.length == 2 && args[0].equals("--playback")) playbackFile = args[1]; 
 		
 		try {
 			int width = new Integer(properties.getProperty("configuration.width")).intValue();
 			int height = new Integer(properties.getProperty("configuration.height")).intValue();
 			double blockSize = new Double(properties.getProperty("configuration.blocksize")).doubleValue();
 			double pixelSize = new Double(properties.getProperty("configuration.pixelsize")).doubleValue();
 			double conveyorSpeed = new Double(properties.getProperty("configuration.conveyorspeed")).doubleValue();
 			double rotationSpeed = new Double(properties.getProperty("configuration.rotationspeed")).doubleValue();
 			double toolRotationSpeed = new Double(properties.getProperty("configuration.toolrotationspeed")).doubleValue();
 			int simulationTime = new Integer(properties.getProperty("configuration.simulationtime")).intValue();
 			int errorTime = new Integer(properties.getProperty("configuration.errortime")).intValue();
 			int port = new Integer(properties.getProperty("configuration.port")).intValue();
 			double sensorRadius = new Double(properties.getProperty("configuration.sensorradius")).doubleValue();
 			boolean loopback = properties.getProperty("configuration.loopback").equals("true"); 
 			String floorColor = properties.getProperty("floor.color");
 			if (floorColor == null) floorColor = "DDDDDD";
 			
 			final Factory factory = new Factory(width, height, blockSize, pixelSize, simulationTime, conveyorSpeed, sensorRadius, rotationSpeed, errorTime, toolRotationSpeed, floorColor, recordFile, playbackFile);
 			ToolTipManager.sharedInstance().registerComponent(factory);
 
 			ModbusSlave.init(port, loopback);
 			
 			try {
 				createBlockTypes(properties);
 				createWarehouses(factory, properties);
 				createTools(factory, properties);
 				createTransformations(properties, factory);
 				createFacilities(factory, properties);
 				createInitialBlocks(factory, properties);
 			} catch (FactoryInitializationException e) {
 				System.out.println("Error creating factory: " + e.getMessage());
 				System.exit(1);
 			}
 		
 		} catch (NumberFormatException e) {
 			System.out.println("Error creating factory: Wrong Number Format " + e.getMessage());
 			System.exit(1);			
 		}
 		
 		ModbusSlave.start();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		frame.add(Factory.getInstance());
 		frame.pack();
 		frame.setVisible(true);
 				
 		new Thread(new Runnable(){
 			public void run() {
 				long time = 0;
 				while(true){
 					try {
 						Thread.sleep(Factory.getInstance().getSimulationTime());
 					} catch (InterruptedException e) {}
 					for (Block block : Factory.getInstance().getBlocks())
 						block.resetMovements();
 					// Move facilities
 					for (Facility facility : Factory.getInstance().getFacilities())
 						facility.doStep(false);
 					// Move affected Blocks
 					for (Block block : Factory.getInstance().getBlocks())
 						block.doStep();
 					// Test for collisions
 					for (Block block1 : Factory.getInstance().getBlocks())
 						for (Block block2 : Factory.getInstance().getBlocks())
 							if (block1!=block2 && block1.getBounds().intersects(block2.getBounds())) {
 								block1.undoStep(); 
 								block2.undoStep();
 							}
 					// Move orders
 					for (int i = 1; i <= Factory.getInstance().warehouses.size(); i++)
 						Factory.getInstance().warehouses.get(new Integer(i)).doStep();
 					Factory.getInstance().repaint();
 					
 					// Record
 					if (recorder != null) recorder.record(time++);
 					if (playback != null) playback.play(time++);
 				}
 			}
 		}).start();
 	}
 
 	public static Factory getInstance() {
 		return instance;
 	}
 
 	private static void createTools(Factory factory, Properties properties) {
 		int id = 1;
 		while(true){
 			if (properties.getProperty("tool."+id+".color")==null) break;
 			String color = properties.getProperty("tool."+id+".color");
 			factory.addTool(id, new Tool(color));
 			id++;
 		}
 	}
 
 	private void addTool(int id, Tool tool) {
 		tools.put(new Integer(id), tool);
 	}
 
 	private static void createWarehouses(Factory factory, Properties properties) {
 		int id = 1;
 		while(true){
 			if (properties.getProperty("warehouse."+id+".width")==null) break;
 			double centerX = new Double(properties.getProperty("warehouse."+id+".center.x")).doubleValue();
 			double centerY = new Double(properties.getProperty("warehouse."+id+".center.y")).doubleValue();
 			double length = new Double(properties.getProperty("warehouse."+id+".length")).doubleValue();
 			double width = new Double(properties.getProperty("warehouse."+id+".width")).doubleValue();
 			String orientation = properties.getProperty("warehouse."+id+".orientation");
 			
 			factory.addWarehouse(id, new Warehouse(factory, id, centerX, centerY, width, length, orientation, properties));
 			id++;
 		}
 	}
 
 	private void addWarehouse(int id, Warehouse warehouse) {
 		warehouses.put(new Integer(id), warehouse);
 	}
 
 	private static void createBlockTypes(Properties properties) {
 		int id = 1;
 		while(true){
 			if (properties.getProperty("blocktype."+id+".name")==null) break;
 			String name = properties.getProperty("blocktype."+id+".name");
 			String color = properties.getProperty("blocktype."+id+".color");
 			BlockType.addType(id, name, color);
 			id++;
 		}
 	}
 
 	private static void createTransformations(Properties properties, final Factory factory) throws FactoryInitializationException {
 		int id = 1;
 		while(true){
 			if (properties.getProperty("transformation."+id+".initial")==null) break;
 			int initial = new Integer(properties.getProperty("transformation."+id+".initial")).intValue();
 			int result = new Integer(properties.getProperty("transformation."+id+".final")).intValue();
 			int tool = new Integer(properties.getProperty("transformation."+id+".tool")).intValue();
 			int duration = new Integer(properties.getProperty("transformation."+id+".duration")).intValue();
 			
 			if (factory.getTool(tool) == null) throw new FactoryInitializationException("No such tool " + tool + " in transformation " + id);
 			
 			factory.addTransformation(new Transformation(initial, result, tool, duration));
 			id++;
 		}
 	}
 
 	private static void createInitialBlocks(final Factory factory, Properties properties) throws FactoryInitializationException {
 		int id = 1;
 		while(true){
 			if (properties.getProperty("block."+id+".type")==null) break;;
 			int type = new Integer(properties.getProperty("block."+id+".type"));;
 			double centerX = new Double(properties.getProperty("block."+id+".center.x")).doubleValue();
 			double centerY = new Double(properties.getProperty("block."+id+".center.y")).doubleValue();
 			factory.addBlock(type, centerX, centerY);
 			
 			if (BlockType.getBlockType(type) == null) throw new FactoryInitializationException("No such blocktype " + type + " in initial block " + id);
 			
 			id++;
 		}
 	}
 
 	private static void createFacilities(final Factory factory,	Properties properties) throws FactoryInitializationException {
 		int id = 1;
 		while(true){
 			String type = properties.getProperty("facility."+id+".type");
 			if (type==null) break;
 			if (type.equals("conveyor")) factory.addFacility(new Conveyor(properties, id));
 			else if (type.equals("rotator")) factory.addFacility(new Rotator(properties, id));
 			else if (type.equals("machine")) factory.addFacility(new Machine(properties, id));
 			else if (type.equals("warehouseout")) factory.addFacility(new WarehouseOut(properties, id));
 			else if (type.equals("warehousein")) factory.addFacility(new WarehouseIn(properties, id));
 			else if (type.equals("rail")) factory.addFacility(new Rail(properties, id));
 			else throw new FactoryInitializationException("No such facility type " + type);
 			id++;
 		}
 	}
 
 	private void addTransformation(Transformation transformation) {
 		transformations.add(transformation);
 	}
 
 	public long getSimulationTime() {
 		return simulationTime;
 	}
 
 	public ArrayList<Block> getBlocks() {
 		return blocks;
 	}
 
 	public ArrayList<Facility> getFacilities() {
 		return facilities;
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		g.setColor(Color.decode("0x"+floorColor));
 		g.fillRect(0, 0, getPreferredSize().width-1, getPreferredSize().height-1);
 		for (Facility facility : facilities)
 			facility.paint(g);
 		for (Block block : blocks)
 			block.paint(g);
 		for (int i = 1; i <= warehouses.size(); i++)
 			warehouses.get(new Integer(i)).paint(g);
 		for (Facility facility : facilities)
 			facility.paintTop(g);
 	}
 	
 	@Override
 	public void update(Graphics g) {
 		Graphics offgc;
 		Image offscreen = null;
 		Rectangle box = g.getClipBounds();
 
 
 		offscreen = createImage(box.width, box.height);
 		offgc = offscreen.getGraphics();
 
 		offgc.setColor(getBackground());
 		offgc.fillRect(0, 0, box.width, box.height);
 		offgc.setColor(getForeground());
 
 		offgc.translate(-box.x, -box.y);
 		paint(offgc);
 
 		g.drawImage(offscreen, box.x, box.y, this);
 	}
 	
 	public double getBlockSize() {
 		return blockSize;
 	}
 
 	public double getConveyorSpeed() {
 		return conveyorSpeed;
 	}
 
 	public double getSensorRadius() {
 		return sensorRadius;
 	}
 
 	public double getRotationSpeed() {
 		return rotationSpeed;
 	}
 
 	public double getToolRotationSpeed() {
 		return toolRotationSpeed;
 	}
 	
 	public int getErrorTime() {
 		return errorTime;
 	}
 
 	public int getTransformation(int type, int tool) {
 		for (Transformation transformation : transformations) {
 			if (transformation.getTool() == tool && transformation.getInitial() == type) return transformation.getResult();
 		}
 		
 		return 0;
 	}
 
 	public int getTransformationTime(int type, int tool) {
 		for (Transformation transformation : transformations) {
 			if (transformation.getTool() == tool && transformation.getInitial() == type) return transformation.getDuration();
 		}
 		
 		return 0;
 	}
 
 	public Color getToolColor(int id){
 		Tool tool = tools.get(new Integer(id));
 		if (tool == null) {System.err.println("No such tool " + id); return Color.black;}
 		return tool.getColor();
 	}
 
 	public Tool getTool(int id){
 		Tool tool = tools.get(new Integer(id));
 		return tool;
 	}
 
 	public Warehouse getWarehouse(int warehouse) {
 		return warehouses.get(new Integer(warehouse));
 	}
 
 	@Override
 	public String getToolTipText(MouseEvent e) {
 		for (Block block : blocks) 
 			if (block.getBounds().contains(e.getPoint())) return BlockType.getBlockType(block.getType()).getName(); 
 		
 		for (Facility facility : facilities)
 			if (facility.getBounds().contains(e.getPoint())) return facility.getType(); 
 		
 		for (int i = 1; i <= warehouses.size(); i++)
 			if (warehouses.get(new Integer(i)).getBounds().contains(e.getPoint())) return "Warehouse";
 		return null;
 	}
 
 	public Block getBlockAt(int x, int y) {
 		Point p = new Point(x, y);
 		for (Block block : blocks) 
 			if (block.getBounds().contains(p)) return block; 
 		return null;
 	}
 
 	public void removeBlock(Block b) {
 		blocks.remove(b);
 	}
 
 	public String getTransformations(int tool) {
 		String ret = "";
 		int id = 1;
 		for (Transformation transformation : transformations) {
 			if (transformation.getTool() == tool) ret += id;
 			id++;
 		}
 		return '[' + ret + ']';
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent action) {
 		Menu menu = (Menu) action.getSource();
 		if (popupfacility !=null && menu.getLabel().equals("Add Block")) {
 			BlockType type = BlockType.getBlockType(action.getActionCommand());
 			addBlock(type.getId(), popupfacility.getBounds().getCenterX()*pixelSize, popupfacility.getBounds().getCenterY()*pixelSize);
 		}
 		if (popupfacility !=null && menu.getLabel().equals("Actions")) {
 			String actionName = action.getActionCommand();
 			popupfacility.doAction(actionName);
 		}
 	}
 
 	@Override
 	public void keyPressed(KeyEvent arg0) {}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) { 
 		int kc = arg0.getKeyCode();		
 		int km = arg0.getModifiers();
 		if (kc == 81 && km == 2) System.exit(0);
 		if (kc == 32 && km == 0) {
 			for (Facility facility : facilities) {
 				facility.stop();
 			}
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) { }
 
 	public static int generateRandom(int min, int max) {
 		return rng.nextInt(1 + max - min) + min;
 	}
 	
 	public static void setRandomSeed(long seed) {
 		rng.setSeed(seed);
 	}
 }
