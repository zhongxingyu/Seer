 package state;
 
 import agents.*;
 import agents.interfaces.*;
 import gui.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 
 import javax.swing.*;
 
 public class Factory_PartA extends JFrame implements ActionListener, MouseListener {
	static final int WIDTH = 800;
 	static final int HEIGHT = 600;
 	
 	// GUI Elements
 	final Rectangle2D.Double bkgrnd = new Rectangle2D.Double(0, 0, WIDTH, HEIGHT);
 
 	// agents
 	public FCSAgent fcs;
     public ConveyorAgent conveyor;
     public KitRobotAgent kitRobot;
     public KitVisionAgent camera;
     public PartRobotAgent partsRobot;
     public KitStand stand;
     
 	ArrayList<Nest> nests;
     
     
     //gui reps
 	public ArrayList<GUI_Component> compList;
 	public GUI_Conveyor guiConveyor;
 	public GUI_KitStand guiKitStand;
 	public GUI_KitRobot guiKitRobot;
 	public GUI_Camera guicamera;
 	
 	private int test = 0;
 
 	public Factory_PartA() {
 		//Setup member variables
 		compList = new ArrayList<GUI_Component>();
 		stand = new KitStand();
 		fcs = new FCSAgent();
 		conveyor = new ConveyorAgent("ConveyorAgent", fcs, this);
 		fcs.setConveyor(conveyor);
		guiKitStand = new GUI_KitStand(400,100);
 		guiConveyor = new GUI_Conveyor();
 		guiKitRobot = new GUI_KitRobot(guiConveyor, guiKitStand);
 		nests = new ArrayList<Nest>(8);
 		partsRobot = new PartRobotAgent("PartRobotAgent", nests, stand);
 		camera = new KitVisionAgent("InspectionCamera", stand);
 		kitRobot = new KitRobotAgent("KitRobotAgent", conveyor, camera, stand, partsRobot, this);
 		camera.setKitRobot(kitRobot);
 		conveyor.setKitRobot(kitRobot);
 
 		
 		conveyor.setConveyorGUI(guiConveyor);
 			
 		//this.addMouseListener(this);
 		
 		//Add GUI_Components in the correct draw order
		compList.add(guiKitStand);
 		compList.add(guiConveyor);
 		compList.add(guiKitRobot);
 		//compList.add(guicamera);
 		
 		//Setup this (Factory_PartA)
 		this.setContentPane(new JPanel() {
 			public void paint(Graphics g) { // called upon app.repaint() in Game class
 				Graphics2D g2D = (Graphics2D) g;
 				g2D.setColor(new Color(230, 230, 230));
 				g2D.fill(bkgrnd);
 
 				// CALLS ALL paintComponent methods of components to be displayed
 				for(GUI_Component c : compList)
 					c.paintComponent(this, g2D);				
 			}
 		});
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setSize(WIDTH, HEIGHT);
 		this.setResizable(false);
 		this.setTitle("Nemo Factory v0 Part A");
 		this.setVisible(true);
 
 		// starting threads
 		fcs.startThread();
 		conveyor.startThread();
 		kitRobot.startThread();
 		camera.startThread();
 		//partRobot.startThread();
 
 		// test simulation for v0
 		Map<String,Integer> kitConfig = new HashMap<String,Integer>();
 		kitConfig.put("clown", 2);
 		kitConfig.put("angler", 3);
 		kitConfig.put("puffer",3);
 		
 		/**
 		 * Will fcs creates kit, which causes chain reaction:
 		 * kit is placed on conveyor, kit robot grabs kit and places
 		 * on stand for assembly by parts robot.
 		 */
 		fcs.msgCreateKit(kitConfig);   	
 		
 		
 		/**
 		 * After the the parts robot assembles the kit, he informs
 		 * the kitRobot, implemented below.  This causes chain reaction
 		 * for kitRobot to place filled kit on inspection stand,
 		 * the camera to inspect it, and then the kitRobot to
 		 * place inspected kit on conveyor.  Conveyor then takes
 		 * it out.
 		 */
 		//time to place kit from creation to filling by partRobot simulation
 		/*try {
 			  Thread.sleep(7000);    // seven secs
 			}
 			catch (Exception e) { System.out.println("fml");}
 			*/
 		
 		//kitRobot.msgKitIsDone();
 	}
         
 	//Timer callback
 	public void actionPerformed(ActionEvent ae) {
 		// This will be called by the Timer
 		// CALLS ALL updateGraphics methods of components to be displayed
                 for(GUI_Component c : compList){
                     c.updateGraphics();
                    
                 }
 		
 		this.repaint();
 	}
 	
 	public GUI_KitStand getGUIKitStand(){
 		return guiKitStand;
 	}
 	public GUI_Conveyor getGUIConveyor(){
 		return guiConveyor;
 	}
 	public GUI_KitRobot getGUIKitRobot(){
 		return guiKitRobot;
 	}
 	
 	//*** TEMPORARY: mouse listener methods ***
 	public void mousePressed(MouseEvent me) {
 		//guicamera.takePicture();
 	}
 	
 	public void mouseClicked(MouseEvent arg0) {}
 	public void mouseEntered(MouseEvent arg0) {}
 	public void mouseExited(MouseEvent arg0) {}
 	public void mouseReleased(MouseEvent arg0) {}
 	
 	
 	
 	public static void main(String[] args) {
 		Factory_PartA app = new Factory_PartA();
 		new Timer(40, app).start();
 
 	}
 }
