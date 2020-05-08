 package state;
 
 import agents.*;
 import agents.interfaces.*;
 import gui.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Collections;
 import java.util.HashMap;
 
 import javax.swing.*;
 
 public class Factory_PartB extends JFrame implements ActionListener {
 	static final int WIDTH = 800;
 	static final int HEIGHT = 600;
 	
 	// GUI Elements
 	//CardLayout cards;
 	final Rectangle2D.Double bkgrnd = new Rectangle2D.Double(0, 0, WIDTH, HEIGHT);
 
 	// Classes that do stuff
 	public GUI_PartRobot guiPartRobot;
 	public PartRobotAgent partRobot;
 	public java.util.List<GUI_Part> guiPartList = Collections.synchronizedList(new ArrayList<GUI_Part>());
 	public ArrayList<GUI_Nest> guiNestList;
 	public ArrayList<Nest> nests;
 	public ArrayList<NestAgent> nestAgents;
 	public ArrayList<GUI_Component> compList;
	public GUI_KitStand guiKitStand;
 	public KitStand kitStand;
 	public PartVisionAgent vision;
 
 	public Factory_PartB() {
 		//Setup member variables
 		guiNestList = new ArrayList<GUI_Nest>(8);
 		nests = new ArrayList<Nest>(8);
 		nestAgents = new ArrayList<NestAgent>(8);
 
 		compList = new ArrayList<GUI_Component>();
 		
 		vision = new PartVisionAgent("Camera");
 
		guiKitStand = new GUI_KitStand(100, 100);
 		kitStand = new KitStand();
 		
 		guiPartRobot = new GUI_PartRobot(this,partRobot,0,0);
 		partRobot = new PartRobotAgent("PartRobot", nests, kitStand, this);
 
 		Map<String,Integer> kitConfig1 = new HashMap<String,Integer>();
 		kitConfig1.put("clown", 2);
 		kitConfig1.put("angler", 3);
 		kitConfig1.put("puffer",3);
 		Kit sampleKit1 = new Kit(kitConfig1, 0);
 		GUI_Kit gk1 = new GUI_Kit(sampleKit1, 100, 100*0);
 		guiKitStand.addkit(gk1, 0);
 		
 		Map<String,Integer> kitConfig2 = new HashMap<String,Integer>();
 		kitConfig2.put("clown", 1);
 		kitConfig2.put("angler", 4);
 		kitConfig2.put("puffer",2);
 		Kit sampleKit2 = new Kit(kitConfig2, 1);		
 		GUI_Kit gk2 = new GUI_Kit(sampleKit2, 100, 100*1);
 		guiKitStand.addkit(gk2, 1);		
 			
 		for(int i=0; i<4; i++) {
 			GUI_NestPair p = new GUI_NestPair((i/2) * GUI_Nest.WIDTH + 400, (i%2) * 2 * GUI_Nest.HEIGHT + 75, this);
 			
 			//Nest 1
 			NestAgent n1 = new NestAgent("Nest " + (i*2), "DummyType", partRobot, p.getNest1(), vision);
 			p.getNest1().setNestAgent(n1);
 			nestAgents.add(n1);
 			nests.add(n1);
 			guiNestList.add(p.getNest1());
 			
 			//Nest 2
 			NestAgent n2 = new NestAgent("Nest " + (i*2+1), "DummyType", partRobot, p.getNest2(), vision);
 			p.getNest2().setNestAgent(n2);
 			nestAgents.add(n2);
 			nests.add(n2);
 			guiNestList.add(p.getNest2());
 			
 			compList.add(p);
 		}
 
 		//Add GUI_Components in the correct draw order
 		compList.add(guiKitStand);
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
 
 
 
 		try {
 			  Thread.sleep(1000);    // one second
 			}
 			catch (Exception e) {}
 		
 		// starting threads
 		vision.startThread();
 		partRobot.startThread();
 		for(NestAgent a : nestAgents) a.startThread();
 
 		// test simulation for v0
 
 		kitStand.insertEmptyKit(sampleKit1);
 		kitStand.insertEmptyKit(sampleKit2);
    		partRobot.msgMakeKits();
 
 
 		
    		// lane 0 becomes clown
    		// lane 1 becomes angler
    		// lane 2 becomes puffer
    		//nestAgents.get(0).msgHereIsPart(new Part("clown"));
    		//nestAgents.get(0).msgHereIsPart(new Part("clown"));
    		//nestAgents.get(0).msgHereIsPart(new Part("clown"));
 
 		for(int i=0; i<8; i++) nestAgents.get(0).msgHereIsPart(new Part("clown"));
 		for(int i=0; i<15; i++) nestAgents.get(1).msgHereIsPart(new Part("angler"));
 		for(int i=0; i<10; i++) nestAgents.get(2).msgHereIsPart(new Part("puffer"));
 
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
 	   
         
 	//Timer callback
 	public void actionPerformed(ActionEvent ae) {
 		// This will be called by the Timer
 		// CALLS ALL updateGraphics methods of components to be displayed
                 for(GUI_Component c : compList)
                     c.updateGraphics();
 		
 		this.repaint();
 	}
 	
 	public static void main(String[] args) {
 		Factory_PartB app = new Factory_PartB();
 		new Timer(15, app).start();
 
 	}
 }
