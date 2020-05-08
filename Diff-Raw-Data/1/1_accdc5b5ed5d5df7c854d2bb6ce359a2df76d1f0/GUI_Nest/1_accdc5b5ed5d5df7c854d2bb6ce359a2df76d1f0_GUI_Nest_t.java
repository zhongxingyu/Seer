 package gui;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Random;
 
 import state.*;
 
 import agents.NestAgent;
 import agents.Part;
 
 public class GUI_Nest extends GUI_Component {
 
 	// Class-wide stuff
 	public static final int WIDTH = 25 * 5 + 20;
 	public static final int HEIGHT = 25 * 2 + 20;
 
 	// Member Variables
 	public GUI_Camera camera;
 	
 	List<GUI_Part> partList;
 	String partHeld;
 	int posX, posY;
 	boolean clumped;
 	boolean badPart;
 	FactoryState state;
 	NestAgent agent;
 	AnimationEvent purge, unstable;
 	Random rand;
 	
 	
 	// Constructor
 	public GUI_Nest(int x, int y, FactoryState factoryState) {
 		partList = new ArrayList<GUI_Part>();
 		posX = x;
 		posY = y;
 		clumped = false;
 		this.state = factoryState;
 		myDrawing = new Drawing(posX, posY, "nest.png");
 		this.partHeld = "";
 		rand = new Random();
 		purge = new AnimationEvent();
 		purge.running = false;
 		unstable = new AnimationEvent();
 		unstable.running = false;
 		badPart = false;
 	}
 
 	// *** DoXXX API methods ***
 	public void doPutPartArrivedAtNest(Part p) {
 		//String partType = p.getPartName();
 
 		//GUI_Part newPart = new GUI_Part(p);
 		GUI_Part newPart = new GUI_Part(p);
         //        this.partHeld = newPart.getName();
 		
 		if(badPart)
 		{
 			System.out.println("wat");
 			newPart.myDrawing.filename = "BadPart.png";
 
 		}
 		partList.add(newPart);
 
 		synchronized (state.guiPartList) {
 			state.guiPartList.add(newPart);
 		}
 	}
 	
 	public void doSetClumped(boolean clumping) {
 		clumped = clumping;
 	}
 	
 	public void doMakeBadParts() {
 		for (int i = 0; i < partList.size(); i++) {
 			GUI_Part part = partList.get(i);
 			part.myDrawing.filename = "BadPart.png";
 		}
 		partHeld = "BadPart";
 		badPart = true;
 	}
 	
 	public void doSetUnstable(boolean stability) {
 		unstable.running = stability;
 	}
 	
 	public void DoPurgeGuiNest() {
 		badPart = false;
 		purge.running = true;
 		purge.timer = 0;
 	}
 
 	// *** GUI friendly functions ***
 	public void removeGUIPart(GUI_Part p) {
 		if (partList.size() != 0)
 		{
 			partList.remove(0);
 			int capacity = 10;
         	agent.lane.msgRequestParts(partHeld,0,capacity-partList.size(), agent);
 		}
 	}
 	
 	public void paintComponent() {
 		//Update drawing's x and y.
 		myDrawing.posX = posX;
 		myDrawing.posY = posY;
 		
 		//Clear subDrawings...
 		myDrawing.subDrawings.clear();
 		
 		//Then add them back
 		for(GUI_Part p : partList) {
 			p.paintComponent();
 			myDrawing.subDrawings.add(p.myDrawing);
 		}
 	}
 
 	public void setPartHeld(String partName) {
 		this.partHeld = partName;
 	}
 
 	public String getPartHeld() {
 		return this.partHeld;
 	}
 
 	public int getPosX() {
 		return this.posX;
 	}
 
 	public int getPosY() {
 		return this.posY;
 	}
 
 	public void setNestAgent(NestAgent n) {
 		this.agent = n;
 	}
 	
 	public NestAgent getNestAgent() {
 		return this.agent;
 	}
 	
 	public void setCamera(GUI_Camera c) {
 		this.camera = c;
 	}
 	
 	public void updateGraphics() {
 		//TODO: Add animation for flushing parts, call DoneFlushedParts when done.
 		
 		for (int i = 0; i < partList.size(); i++) {
 			GUI_Part part = partList.get(i);
 
 			// Set the x and y coordinates to have fish fill from the left side,
 			// column by column.
 			int x = (i / 2) * (clumped ? 15 : 25);
 			int y = (i % 2) * 25;
 
 			// Add 12 so that the parts are not on top of the nest
 			// borders.
 			x += 12;
 			y += 12;
 			
 			//Deal with other effects
 			
 			//purge animation
 			if(purge.running) {
 				x -= purge.timer;
 				
 				if(x <= 0)
 					part.myDrawing.filename = "NOPIC";
 			}
 			
 			//unstable animation
 			if (unstable.running) {
 				x += rand.nextGaussian();
 				y += rand.nextGaussian();
 			}
 			
 			// Finally, add the nest's coordinates so that they are drawn over
 			// the nest.
 			x += posX;
 			y += posY;
 			
 			part.setCoordinates(x, y);
 		}
 		
 		if(purge.running) {
 			boolean allInvisible = true;
 			
 			//If any of the parts are still visible, keep purge running
 			for(GUI_Part p : partList)
 				allInvisible &= p.myDrawing.filename.equals("NOPIC");
 		
 			if(allInvisible) {
				partList.clear();
 				purge.running = false;
 				agent.msgDoneFlushParts();
 			}
 		}
 		
 		purge.timer = purge.running ? purge.timer + 1 : 0;
 	}
 }
