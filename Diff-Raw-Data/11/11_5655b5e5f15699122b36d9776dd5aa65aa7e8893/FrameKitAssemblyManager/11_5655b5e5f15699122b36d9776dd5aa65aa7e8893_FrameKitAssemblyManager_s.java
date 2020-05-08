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
	ConveyorAgent conveyor = new ConveyorAgent(this);
 	PartsRobotAgent partsRobot = new PartsRobotAgent();
	StandAgent stand = new StandAgent(null, null, partsRobot);
 	ConveyorControllerAgent conveyorController = new ConveyorControllerAgent(conveyor, this);
 	VisionAgent vision = new VisionAgent(null, stand, this);
	KitRobotAgent kitRobot = new KitRobotAgent(stand, this, conveyor);
 	
 	public FrameKitAssemblyManager() {
 		//Constructor. BorderLayout
 		GKAM = new GraphicKitAssemblyManager(this);
 		this.add(GKAM, BorderLayout.CENTER);
 		CP = new ControlPanel(this);
 		this.add(CP, BorderLayout.LINE_END);
 		
 		// v.0 stuff
 		stand.vision = vision;
 		stand.kitRobot = kitRobot;
		conveyor.conveyor_controller = conveyorController;
 		conveyor.startThread();
 		conveyorController.startThread();
 		vision.startThread();
 		partsRobot.startThread();
 		kitRobot.startThread();
 		stand.startThread();
 	}
 	
 	public void moveEmptyKitToSlot(int slot){
 		GKAM.moveEmptyKitToSlot(slot);
 	}
 	
 	public void sendNewEmptyKit() {
 		System.out.println("New Empty Kit Requested!");
 		//Adds a Kit into the factory
 		GKAM.newEmptyKit();
 	}
 	
 	public void takePicture(){
 		GKAM.takePictureOfInspectionSlot();
 	}
 	
 	public void exportKit() {
 		//Sends a Kit out of the factory
 		GKAM.exportKit();
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
 			stand.stateChanged();
 		}
 		//GKAM.checkKit(slot);
 	}
 	
 	public void moveKitFromSlotToInspection(int slot){
 		GKAM.moveKitToInspection(slot);
 	}
 	public void dumpKit() {
 		GKAM.dumpKitAtInspection();
 	}
 	
 	public void newEmptyKitAtConveyor(){
 		System.out.println("New Empty Kit Arrived!");
 		//Should call conveyorController.msgAnimationDone() when animation is complete
 		conveyorController.msgAnimationDone();
 	}
 	
 	public void moveEmptyKitToSlotDone() {
 		System.out.println("Kit sent to Kitting Station!");
 		kitRobot.msgAnimationDone();
 	}
 	
 	public void moveKitToInspectionDone() {
 		System.out.println("Kit sent to Inspection Station!");
 		kitRobot.msgAnimationDone();
 	}
 	
 	public void takePictureOfInspectionSlotDone() {
 		System.out.println("Picture taken!");
 		vision.msgAnimationDone();
 	}
 	
 	public void dumpKitAtInspectionDone() {
 		System.out.println("Kit has been dumped!");
 		kitRobot.msgAnimationDone();
 	}
 	
 	public void exportKitDone() {
 		System.out.println("Kit has left the building!");
 		kitRobot.msgAnimationDone();
 	}
 	
 	public static void main(String args[]) {
 		//Implements this JFrame
 		FrameKitAssemblyManager FKAM = new FrameKitAssemblyManager();
 		FKAM.setVisible(true);
 		FKAM.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//FKAM.setSize(800, 720);
 		FKAM.pack();
 	}
 
 }
