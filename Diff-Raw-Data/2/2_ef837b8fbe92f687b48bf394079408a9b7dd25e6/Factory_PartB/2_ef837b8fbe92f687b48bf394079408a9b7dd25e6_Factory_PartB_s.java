 package state;
 
 import agents.*;
 import gui.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import javax.swing.*;
 
 public class Factory_PartB extends JFrame implements ActionListener {
 	static final int WIDTH = 800;
 	static final int HEIGHT = 600;
 	
 	// GUI Elements
 	CardLayout cards;
 	final Rectangle2D.Double bkgrnd = new Rectangle2D.Double(0, 0, WIDTH, HEIGHT);
 
 	// Classes that do stuff
 	public GUI_PartRobot partRobot;
 	public ArrayList<GUI_Part> guiPartList;
 	public ArrayList<GUI_Nest> guiNestList;
 	public ArrayList<GUI_Component> compList;
 	public GUI_KitStand guiKitStand;
 
 	public Factory_PartB() {
 		//Setup member variables
 		guiNestList = new ArrayList<GUI_Nest>(8);
 		guiPartList = new ArrayList<GUI_Part>();
 		compList = new ArrayList<GUI_Component>();
 		
 		partRobot = new GUI_PartRobot(this);
 		compList.add(partRobot);
 		
 		for(int i=0; i<8; i++) {
			GUI_Nest n = new GUI_Nest(i, (i/4) * 120 + 150, (i%4) * 120 + 75);
 			guiNestList.add(n);
 			compList.add(n);
 		}
 		
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
 	}
 
 	public void doPickUpParts(HashMap<Part, Integer> parts) {
 		ArrayList<GUI_Part> partsToGet = new ArrayList<GUI_Part>();
 		ArrayList<Integer> nestIndices = new ArrayList<Integer>();
 		for (GUI_Part gp : guiPartList) {
 			if (parts.containsKey(gp.agentPart)) {
 				partsToGet.add(gp);
 				nestIndices.add(parts.get(gp.agentPart));
 			}
 
 		}
 		partRobot.doTransferParts(partsToGet, nestIndices);
 	}
 
 	public static void main(String[] args) {
 		Factory_PartB app = new Factory_PartB();
 		new Timer(15, app).start();
 
 	}
 
 	public void actionPerformed(ActionEvent ae) {
 		// This will be called by the Timer
 		// CALLS ALL updateGraphics methods of components to be displayed
 		partRobot.updateGraphics();
 		this.repaint();
 	}
 }
