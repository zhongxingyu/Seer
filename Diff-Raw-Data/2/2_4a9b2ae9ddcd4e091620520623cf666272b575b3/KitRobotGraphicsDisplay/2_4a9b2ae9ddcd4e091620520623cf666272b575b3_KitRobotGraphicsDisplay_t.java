 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 
 public class KitRobotGraphicsDisplay extends DeviceGraphicsDisplay {
 	// double x,y;
 
 	// Rectangle2D.Double rectangle;
 	Rectangle2D.Double rectangle1;
 	JLabel imageLabel;
 
 	// Positions
 	public enum Position {
 		conveyorPosition, goodConveyorPosition, inspectionPosition, location1Position, location2Position;
 	}
 
 	Position position;
 
 	// Commands
 	public enum Command {
 		moveToConveyor,moveToGoodConveyor, moveToInspectionStand, moveToLocation1, moveToLocation2
 	};
 
 	Command moveToInitialPosition;
 	Command moveToPosition;
 	Command moveToFinalPosition;
 	
 	
 	boolean initialJob;
 	boolean finalJob;
 	boolean jobIsDone;
 
 	int degreeStep;
 	public void setDegreeStep(int degreeStep) {
 		this.degreeStep = degreeStep;
 	}
 
 	int currentDegree;
 	int finalDegree;
 	int degreeCountDown;
 
 	int rotationAxisX;
 	int rotationAxisY;
 
 	int kitRobotPositionX;
 	int kitRobotPositionY;
 	AffineTransform trans;
 	
 	Client kitRobotClient;
 	Location location;
 	ArrayList<KitGraphicsDisplay> kits = new ArrayList<KitGraphicsDisplay>();
 	KitGraphicsDisplay currentKit = new KitGraphicsDisplay();
 
 	// just for v0
 	boolean AnimationToConveyorDone;
 
 	public boolean isAnimationToConveyorDone() {
 		return AnimationToConveyorDone;
 	}
 
 	public KitRobotGraphicsDisplay(Client cli) {
 
 		// super();
 		location = Constants.KIT_ROBOT_LOC;
 		kitRobotClient = cli;
 
 		moveToInitialPosition = Command.moveToInspectionStand;
 		moveToFinalPosition = Command.moveToConveyor;
 		position = Position.conveyorPosition;
 		initialJob = false;
 		finalJob = false;
 		jobIsDone = true;
 		degreeStep = 2;
 		currentDegree = 0;
 		finalDegree = 0;
 
 		trans = new AffineTransform();
 
 		// image =new
 		// ImageIcon(this.getClass().getResource("/resource/Square.jpg"));
 		rotationAxisX = 25;
 		rotationAxisY = 25;
 		kitRobotPositionX = 195;
 		kitRobotPositionY = 215;
 
 		trans.translate(kitRobotPositionX, kitRobotPositionY);
 		rectangle1 = new Rectangle2D.Double(0, 0, 600, 400);
 
 	}
 
 	public void changeKit(KitGraphicsDisplay k) {
 		kits.add(k);
 	}
 
 	public void removeKit(KitGraphicsDisplay k) {
 		kits.remove(k);
 	}
 
 	public void placeKitOnStand(int i) {
 		if (i == 1) {
 			ConveyorToLocation1();
 		} else if (i == 2) {
 			ConveyorToLocation2();
 		}
 	}
 
 	public void setCommands(Command initialCommand, Command finalCommand){
 		jobIsDone=false;
 		initialJob=true;
 		this.moveToInitialPosition=initialCommand;
 		this.moveToFinalPosition=finalCommand;
 	}
 	public void InspectionToGoodConveyor(){
 		setCommands(Command.moveToInspectionStand, Command.moveToLocation1);
 		moveToInitialOrFinal();
 	}
 	public void ConveyorToLocation1() {
 		setCommands(Command.moveToConveyor, Command.moveToLocation1);
 		moveToInitialOrFinal();
 	}
 
 	public void ConveyorToLocation2() {
 		jobIsDone = false;
 		initialJob = true;
 		moveToInitialPosition = Command.moveToConveyor;
 		moveToFinalPosition = Command.moveToLocation2;
 		moveToInitialOrFinal();
 	}
 
 	public void Location1ToInspectionStand() {
 		jobIsDone = false;
 		initialJob = true;
 		moveToInitialPosition = Command.moveToLocation1;
 		moveToFinalPosition = Command.moveToInspectionStand;
 		moveToInitialOrFinal();
 	}
 
 	public void Location2ToInspectionStand() {
 		jobIsDone = false;
 		initialJob = true;
 		moveToInitialPosition = Command.moveToLocation2;
 		moveToFinalPosition = Command.moveToInspectionStand;
 		moveToInitialOrFinal();
 	}
 
 	public void InspectionStandToConveyor() {
 		jobIsDone = false;
 		initialJob = true;
 		moveToInitialPosition = Command.moveToInspectionStand;
 		moveToFinalPosition = Command.moveToConveyor;
 		moveToInitialOrFinal();
 	}
 
 	public void Location1ToLocation2() {
 		jobIsDone = false;
 		initialJob = true;
 		moveToInitialPosition = Command.moveToLocation1;
 		moveToFinalPosition = Command.moveToLocation2;
 
 		moveToInitialOrFinal();
 	}
 	
 	public void setPositiveDegreeStep(){
 		setDegreeStep(2);
 	}
 	
 	public void setNegativeDegreeStep(){
 		setDegreeStep(-2);
 	}
 
 	public void moveToInitialOrFinal() {
 		
 		if(initialJob)
 		{
 			moveToPosition=moveToInitialPosition;
 		}
 		else if(finalJob)
 		{
 			moveToPosition=moveToFinalPosition;
 		}
 		
 		if (position.equals(Position.conveyorPosition) ) {
 			if (moveToPosition.equals(Command.moveToInspectionStand)) {
 			    setRotationConfigurations(135,Position.inspectionPosition);
 			} else if (moveToPosition.equals(Command.moveToLocation1)) {
 				setRotationConfigurations(180, Position.location1Position);
 			} else if (moveToPosition.equals(Command.moveToLocation2)) {
 				setRotationConfigurations(225, Position.location2Position);
 			} else if (moveToPosition.equals(Command.moveToGoodConveyor)) {
 			    setRotationConfigurations(45, Position.goodConveyorPosition);
 			}
 			else {
 				degreeCountDown = 0;
 			}
 		} else if (position.equals(Position.inspectionPosition)) {
 			if (moveToPosition.equals(Command.moveToLocation1) ) {
 				setRotationConfigurations(45, Position.location1Position);
 			} else if (moveToPosition.equals(Command.moveToLocation2)) {
 				setRotationConfigurations(90, Position.location2Position);
 			} else if (moveToPosition.equals(Command.moveToConveyor)) {
 				setRotationConfigurations(-135, Position.conveyorPosition);
 			} else if (moveToPosition.equals(Command.moveToGoodConveyor)){
 				setRotationConfigurations(-90, Position.goodConveyorPosition);
 			} else {
 				degreeCountDown = 0;
 			}
 		} else if (position.equals(Position.location1Position)) {
 			if (moveToPosition.equals(Command.moveToLocation2)) {
 				setRotationConfigurations(45, Position.location2Position);
 			} else if (moveToPosition.equals(Command.moveToConveyor)) {
 				setRotationConfigurations(180, Position.conveyorPosition);
 			} else if (moveToPosition
 					.equals(Command.moveToInspectionStand)) {
 				setRotationConfigurations(-45, Position.inspectionPosition);
 			} else if(moveToPosition.equals(Command.moveToGoodConveyor))
 			{
 				setRotationConfigurations(-135,Position.goodConveyorPosition);
 			} else {
 				degreeCountDown = 0;
 			}
 		} else if (position.equals(Position.location2Position)) {
 			if (moveToPosition.equals(Command.moveToConveyor)) {
 				setRotationConfigurations(135, Position.conveyorPosition);
 			} else if (moveToPosition
 					.equals(Command.moveToInspectionStand)) {
 				setRotationConfigurations(-90, Position.inspectionPosition);
 			} else if (moveToPosition.equals(Command.moveToLocation1)) {
 				setRotationConfigurations(-45, Position.location1Position);
 			} else if(moveToPosition.equals(Command.moveToGoodConveyor)){
 				setRotationConfigurations(180, Position.goodConveyorPosition);
 			}
 			else {
 				degreeCountDown = 0;
 			}
 		}
 	}
 	public void setRotationConfigurations(int degreeCountDown, Position position){
 	this.degreeCountDown = 270;
 	if(this.degreeCountDown>=0)
 	{
 		setPositiveDegreeStep();
 	}
 	else {
 	setNegativeDegreeStep();
 	}
 	this.position = position;
 	}
 	public void checkDegrees() {
 
 		if (degreeCountDown == 0) {
 
 			if (initialJob) {
 				initialJob = false;
 				finalJob = true;
 				currentDegree = 0;
 				System.out.println("Passed through initial job");
 
 				if (position.equals(Position.conveyorPosition)
 						&& moveToInitialPosition.equals(Command.moveToConveyor)) {
 
 					System.out.println("Sending to conveyor");
 
 				}
 
 				moveToInitialOrFinal();
 			} else if (finalJob) {
 				if (position.equals(Position.location1Position)
 						&& moveToFinalPosition.equals(Command.moveToLocation2)) {
 					// kitRobotClient.sendData(new
 					// Request(Constants.KIT_ROBOT_TARGET,
 					// Constants.KIT_ROBOT_ON_STAND_DONE, null));
 				}
 				finalJob = false;
 				jobIsDone = true;
 				currentDegree = 0;
 
 			}
 		}
 	}
 
 	@Override
 	public void receiveData(Request req) {
 		String command = req.getCommand();
 		String target = req.getTarget();
 		Object obj = req.getData();
 		
 		if (target.equals(Constants.KIT_ROBOT_TARGET)) {
 			if (command.equals(Constants.KIT_ROBOT_DISPLAY_PICKS_CONVEYOR_TO_LOCATION1)) {
 				
 				KitGraphicsDisplay kit = new KitGraphicsDisplay();
 				kit.setPosition(3);
 				currentKit = kit;
 				kits.add(kit);
 				ConveyorToLocation1();
 			} else if (command
					.equals(Constants.KIT_ROBOT_DISPLAY_PICKS_LOCATION1_TO_CONVEYOR)) {
 				for (int i = 0; i < kits.size(); i++) {
 					if (kits.get(i).getPosition() == 3) {
 						System.out.println("goes through moveit command");
 						currentKit = kits.get(i);
 						kits.get(i).setPosition(4);
 					}
 				}
 
 				Location1ToLocation2();
 			} else if (command.equals("moveKitToStand2")) {
 				KitGraphicsDisplay kit = new KitGraphicsDisplay();
 				kit.setPosition(4);
 				currentKit = kit;
 				kits.add(kit);
 				ConveyorToLocation2();
 			} else if (command.equals("moveKitInLocation1ToInspection")) {
 				for (int i = 0; i < kits.size(); i++) {
 					if (kits.get(i).getPosition() == 3) {
 						currentKit = kits.get(i);
 						kits.get(i).setPosition(2);
 					}
 				}
 				Location1ToInspectionStand();
 			} else if (command.equals("moveKitInLocation2ToInspection")) {
 				for (int i = 0; i < kits.size(); i++) {
 					if (kits.get(i).getPosition() == 4) {
 						currentKit = kits.get(i);
 						kits.get(i).setPosition(2);
 					}
 				}
 				Location2ToInspectionStand();
 			}
 
 		}
 
 	}
 
 	public void doJob() {
 		if (!jobIsDone) {
 			trans.rotate(Math.toRadians(degreeStep), rotationAxisX,
 					rotationAxisY);
 			degreeCountDown-=degreeStep;
 			// System.out.println("currentDegree: " + currentDegree);
 		}
 	}
 
 
 	@Override
 	public void draw(JComponent c, Graphics2D g) {
 		checkDegrees();
 		doJob();
 		AnimationToConveyorDone = false;
 		if (currentDegree == 180
 				&& moveToFinalPosition.equals(Command.moveToLocation1)) {
 			kitRobotClient.sendData(new Request(
 					Constants.KIT_ROBOT_ON_STAND_DONE,
 					Constants.KIT_ROBOT_TARGET, null));
 		}
 
 		for (int i = 0; i < kits.size(); i++) {
 
 			kits.get(i).drawRotate(c, g);
 			if (kits.get(i).getPosition() == 4) {
 				if (kits.get(i).isAnimationToConveyorDone()) {
 					System.out.println("Sends conveyor receives kit command");
 					AnimationToConveyorDone = true;
 					kitRobotClient.sendData(new Request(
 							Constants.KIT_ROBOT_ON_CONVEYOR_DONE,
 							Constants.KIT_ROBOT_TARGET, null));
 					kits.remove(0);
 
 					// client.sendData(new
 					// Request(Constants.CONVEYOR_RECEIVE_KIT_COMMAND,
 					// Constants.CONVEYOR_TARGET, null));
 				}
 			}
 		}
 
 		// Image
 		// image=Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resource/Square.jpg"));
 		g.drawImage(Constants.KIT_ROBOT_IMAGE, trans, null);
 
 	}
 
 	/*
 	 * public void draw(JComponent c, Graphics2D g) { g.drawImage(kitImage,
 	 * kitLocation.getX(), kitLocation.getY(), c); }
 	 */
 	/*
 	 * public void paint(Graphics g){ //Graphics2D g2=(Graphics2D)g;
 	 * //g2.setColor(Color.yellow); //g2.fill(rectangle1);
 	 * //trans.rotate(Math.toRadians(1),-50,-50); //trans = g2.getTransform();
 	 * //g2.rotate(Math.toRadians(rotation), imageX, imageY);
 	 * //image.paintIcon(this, g2, imageX, imageY); //trans.rotate(.01, 225,
 	 * 125); //trans.rotate(Math.toRadians(1),rotationAxisX,rotationAxisY);
 	 * Image
 	 * image=Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(
 	 * "/resource/Square.jpg")); g2.drawImage(image, trans,null); kit.paint(g2);
 	 * g2.finalize(); }
 	 */
 	/*
 	 * public static void main(String args[]){ JFrame frame = new
 	 * JFrame("RotateImage");
 	 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	 * frame.setSize(600,400); KitRobotGraphicsDisplay rke= new
 	 * KitRobotGraphicsDisplay(); frame.setContentPane(rke);
 	 * frame.setVisible(true); rke.ConveyorToLocation1();//added this command
 	 * new javax.swing.Timer(100,rke).start(); }
 	 */
 
 	@Override
 	public void setLocation(Location newLocation) {
 		location = newLocation;
 		// TODO Auto-generated method stub
 	}
 
 }
