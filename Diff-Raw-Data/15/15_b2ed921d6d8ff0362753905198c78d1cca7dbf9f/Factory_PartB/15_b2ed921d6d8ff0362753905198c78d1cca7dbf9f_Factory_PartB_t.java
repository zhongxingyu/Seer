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
 
 public class Factory_PartB extends JFrame implements ActionListener, MouseListener {
 	static final int WIDTH = 800;
 	static final int HEIGHT = 600;
 	
 	// GUI Elements
 	CardLayout cards;
 	final Rectangle2D.Double bkgrnd = new Rectangle2D.Double(0, 0, WIDTH, HEIGHT);
 
 	// Classes that do stuff
 	public GUI_PartRobot guiPartRobot;
 	public PartRobotAgent partRobot;
 	public ArrayList<GUI_Part> guiPartList;
 	public ArrayList<GUI_Nest> guiNestList;
 	public ArrayList<NestAgent> nestAgents;
 	public ArrayList<GUI_Component> compList;
 	public GUI_KitStand guiKitStand;
 	public KitStand kitStand;
 	public GUI_Camera camera;
 	public PartVisionAgent vision;
 
 	public Factory_PartB() {
 		//Setup member variables
 		guiNestList = new ArrayList<GUI_Nest>(8);
 		nestAgents = new ArrayList<NestAgent>(8);
 		guiPartList = new ArrayList<GUI_Part>();
 		compList = new ArrayList<GUI_Component>();
 		
 		vision = new PartVisionAgent();
 
 		guiKitStand = new GUI_KitStand(100, 100);
 		kitStand = new KitStand();
 		
 		guiPartRobot = new GUI_PartRobot(this,partRobot,0,0);
 		partRobot = new PartRobotAgent(nestAgents, kitStand, this);
 
 		
 		camera = new GUI_Camera(100, 100);
 		this.addMouseListener(this);
 		
 		for(int i=0; i<8; i++) {
 			GUI_Nest n = new GUI_Nest(i, (i/4) * 120 + 400, (i%4) * 120 + 75);
 			nestAgents.add(new NestAgent("", partRobot, n, vision));
 			guiNestList.add(n);
 		}
 
		//Add GUI_Components in the correct draw order
		compList.add(guiKitStand);
		for(GUI_Nest n : guiNestList)
			compList.add(n);
		compList.add(camera);
		compList.add(guiPartRobot);
 		
 		//Setup this (Factory_PartB)
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
 		this.setTitle("Nemo Factory v0 Part B");
 		this.setVisible(true);
 
 		vision.startThread();
 		for(int i=0; i<nestAgents.size(); i++)
 		{
 			nestAgents.get(i).startThread();
 
 			//nestAgents.get(i).startThread();
 
 		}
 
 		partRobot.startThread();
     	nestAgents.get(7).msgRequestParts("puffer", 8);
 
 		for(int i=0; i<8; i++) nestAgents.get(0).msgHereIsPart(new Part("clown"));
 		for(int i=0; i<10; i++) nestAgents.get(4).msgHereIsPart(new Part("angler"));
 		for(int i=0; i<15; i++) nestAgents.get(7).msgHereIsPart(new Part("puffer"));
 		for(int i=0; i<2; i++) nestAgents.get(3).msgHereIsPart(new Part(""));
 
 
 
 
 	}
 
 	//*** DoXXX API ***
 	public void doPickUpParts(Map<Part, Integer> parts) {
 		ArrayList<GUI_Part> partsToGet = new ArrayList<GUI_Part>();
 		ArrayList<Integer> nestIndices = new ArrayList<Integer>();
 		for (GUI_Part p : guiPartList) {
 			if (parts.containsKey(p.agentPart)) {
 				partsToGet.add(p);
 				nestIndices.add(parts.get(p.agentPart));
 			}
 
 		}
 		guiPartRobot.doTransferParts(partsToGet, nestIndices);
 	}       
         
 	//Timer callback
 	public void actionPerformed(ActionEvent ae) {
 		// This will be called by the Timer
 		// CALLS ALL updateGraphics methods of components to be displayed
                 for(GUI_Component c : compList)
                     c.updateGraphics();
 		
 		this.repaint();
 	}
 	
 	//*** TEMPORARY: mouse listener methods ***
 	public void mousePressed(MouseEvent me) {
 		camera.takePicture();
 	}
 	
 	public void mouseClicked(MouseEvent arg0) {}
 	public void mouseEntered(MouseEvent arg0) {}
 	public void mouseExited(MouseEvent arg0) {}
 	public void mouseReleased(MouseEvent arg0) {}
 	
 	public static void main(String[] args) {
 		Factory_PartB app = new Factory_PartB();
 		new Timer(15, app).start();
 
 	}
 }
