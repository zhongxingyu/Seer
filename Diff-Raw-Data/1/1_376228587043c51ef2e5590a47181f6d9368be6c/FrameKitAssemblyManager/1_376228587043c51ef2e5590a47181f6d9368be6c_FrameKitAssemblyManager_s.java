 package factory.graphics;
 import java.awt.*;
 import javax.swing.*;
 
 import factory.*;
 import factory.Kit.KitState;
 import factory.StandAgent.MySlotState;
 
 public class FrameKitAssemblyManager extends JFrame{
 	
 	/*FrameKitAssemblyManager.java (800x600) - Tobias Lee
 	 * This integrates the GraphicKitAssemblyManager with a small Control Panel to demonstrate commands
 	 * This will be removed when the project gets integrated
 	 */
 	
 	GraphicKitAssemblyManager GKAM; //The Graphics part
 	ControlPanel CP; //The Swing control panel
 	
 	
 	// These are for v.0
 	ConveyorAgent conveyor = new ConveyorAgent();
 	VisionAgent vision = new VisionAgent(null, null, null);
 	PartsRobotAgent partsRobot = new PartsRobotAgent();
 	StandAgent stand = new StandAgent(conveyor, vision, null, partsRobot);
 	KitRobotAgent kitRobot = new KitRobotAgent(stand, this);
 	
 	public FrameKitAssemblyManager() {
 		//Constructor. BorderLayout
 		GKAM = new GraphicKitAssemblyManager(this);
 		GKAM.setPreferredSize(new Dimension(600, 600));
 		this.add(GKAM, BorderLayout.CENTER);
 		CP = new ControlPanel(this);
 		CP.setPreferredSize(new Dimension(200, 600));
 		this.add(CP, BorderLayout.LINE_END);
 		
 		// v.0 stuff
 		stand.kitRobot = kitRobot;
 		conveyor.startThread();
 		vision.startThread();
 		partsRobot.startThread();
 		kitRobot.startThread();
 		stand.startThread();
 	}
 	
 	public void moveEmptyKitToSlot(int slot){
 		GKAM.robotFromBelt(slot);
 	}
 	
 	/**
 	 * Method to send a new empty kit in the conveyor
 	 */
 	public void sendNewEmptyKit() {
 		//Adds a Kit into the factory
 		GKAM.addInKit();
 		System.out.println("New Empty Kit Requested!");
 	}
 	
 	public void takePicture(){
 		GKAM.inspectKit();
 	}
 	
 	public void outKit() {
 		//Sends a Kit out of the factory
 		GKAM.outKit();
 	}
 	
 	public void kitToCheck(int slot) {
 		if(slot == 0){
 			System.out.println("Kit at topSlot is compete!");
 			stand.topSlot.kit.state = KitState.COMPLETE;
 			stand.stateChanged();
 		}
 		else {
 			System.out.println("Kit at bottomSlot is compete!");
 			stand.bottomSlot.kit.state = KitState.COMPLETE;
 			
 		}
 		//GKAM.checkKit(slot);
 	}
 	
 	public void moveKitFromSlotToInspection(int slot){
 		GKAM.checkKit(slot);
 	}
 	public void dumpKit() {
 		GKAM.purgeKit();
 	}
 	
 	public void newEmptyKitAtConveyor(){
 		System.out.println("New Empty Kit Arrived!");
 		stand.msgEmptyKitIsHere();
 		
 	}
 	
 	public void fromBeltDone() {
 		System.out.println("Kit sent to Kitting Station!");
 		kitRobot.msgAnimationDone();
 	}
 	
 	public void toCheckDone() {
 		System.out.println("Kit sent to Inspection Station!");
 		kitRobot.msgAnimationDone();
 	}
 	
 	public void pictureDone() {
 		System.out.println("Picture taken!");
 	}
 	
 	public void dumpDone() {
 		System.out.println("Kit has been dumped!");
 		kitRobot.msgAnimationDone();
 	}
 	
 	public void outKitDone() {
 		System.out.println("Kit has left the building!");
 		kitRobot.msgAnimationDone();
 	}
 	
 	public static void main(String args[]) {
 		//Implements this JFrame
 		FrameKitAssemblyManager FKAM = new FrameKitAssemblyManager();
 		FKAM.setVisible(true);
 		FKAM.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		FKAM.setSize(800, 600);
 	}
 
 }
