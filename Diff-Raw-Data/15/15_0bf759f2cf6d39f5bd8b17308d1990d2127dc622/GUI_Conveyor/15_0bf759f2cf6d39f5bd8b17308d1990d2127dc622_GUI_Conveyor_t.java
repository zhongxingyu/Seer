 package gui;
 
 import agents.ConveyorAgent;
 import agents.Kit;
 import agents.KitRobotAgent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.swing.ImageIcon;
 
 import state.FactoryState;
 
 public class GUI_Conveyor extends GUI_Component {
 	ImageIcon ConImage;// image of conveyor
	int x, y, state, animCounter;// coordinates
 	public List<GUI_Kit> kits;
 	public GUI_Kit shownkit = null;
 	ConveyorAgent conagent;
 	KitRobotAgent kitagent;
 	public boolean conveyorOn;
 
 	// Constructor
 	public GUI_Conveyor() {
 		kits = Collections.synchronizedList(new ArrayList<GUI_Kit>());
 		ConImage = new ImageIcon("gfx/conv1.png");
 		x = 0;
 		y = 0;
 		state = 0;
 		conveyorOn = true;
 		myDrawing = new Drawing(x, y, "conv1.png");
 		animCounter = 0;
 
 	}
 
 	public void DoAddKitToConveyor(ConveyorAgent con) {
 		// System.out.println("#ofKits: " + kits.size());
 		// System.out.println("#ofKitsOnStand: " + numberOfKitsOnStands);
 		conveyorOn = true;
		shownkit = kits.get(0);
		kits.remove(0);
 		FactoryState.out.println("KitID of kit to add to conveyor: "
 				+ shownkit.kit.getKitId());
 		state = 1;
 		conagent = con;
 	}
 
 	synchronized public void addKit(Kit k) {
 		GUI_Kit addkit = new GUI_Kit(k, 130, 0);
 		kits.add(addkit);
 		// System.out.println("guiConveyor added kit. kits.size(): " +
 		// kits.size());
 	}
 
 	public GUI_Kit robotRemoveKit() {
 		state = 3;
 		GUI_Kit passkit = shownkit;
 		shownkit = null;
 		return passkit;
 	}
 
 	public GUI_Kit checkKit() {
 		return shownkit;
 	}
 
 	public void DoGetKitOut(GUI_Kit k, KitRobotAgent robot) {
 		conveyorOn = true;
 		kitagent = robot;
 		shownkit = k;
 		shownkit.posX = 70;
 		shownkit.posY = 30;
 		state = 2;
 	}
 
 	// the move function will have the requirements for actually moving the
 	// conveyor belt and the pallets
 	public void state1Move() {
 		if (shownkit != null) {// if there are kits to move, move them along the
 								// conveyor
 			if (shownkit.posY <= 30) {
 				shownkit.posY += 2;
 			} else {
 				if (shownkit.posX > 70) {
 					shownkit.posX -= 2;
 				} else {
 					conveyorOn = false;
 					state = 0;
 				}
 			}
 		}
 	}
 
 	public void state2Move() {
 		if (shownkit != null) {// if there are kits to move, move them along the
 								// conveyor
 			if (shownkit.posX > -100) {
 				shownkit.posX -= 2;
 			} else {
 				conveyorOn = false;
 				shownkit = null;
 				state = 0;
 				// kitagent.inspectionToConveyorRelease();
 			}
 		}
 	}
 
 	public void paintComponent() {
 		myDrawing.posX = x;
 		myDrawing.posY = y;
 		if (conveyorOn) {
 			myDrawing.filename = ("conv" + ((animCounter / 5) + 1) + ".png");
 			animCounter = (animCounter + 1) % 10;
 		} else {
 			myDrawing.filename = "conv1.png";
 		}
 
 		myDrawing.subDrawings.clear();
 
 		/*
 		 * for(GUI_Kit k: kits){ k.paintComponent();
 		 * myDrawing.subDrawings.add(k.myDrawing); }
 		 */
 		if (shownkit != null) {
 			shownkit.paintComponent();
 			myDrawing.subDrawings.add(shownkit.myDrawing);
 		}
 	}
 
 	public void updateGraphics() {
 		if (state == 1) {
 			state1Move();
 		} else if (state == 2) {
 			state2Move();
 		} else if (state == 3) {
 		}
 
 	}
 
 	public int getX() {
 		return shownkit.posX;
 	}
 
 	public int getY() {
 		return shownkit.posY;
 	}
 
 	public List<GUI_Kit> getKits() {
 		return kits;
 	}
 
 }
