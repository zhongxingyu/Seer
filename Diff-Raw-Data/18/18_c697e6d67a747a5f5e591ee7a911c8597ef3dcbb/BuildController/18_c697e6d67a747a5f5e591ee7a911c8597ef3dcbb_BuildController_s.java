 package controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import model.iOverlord;
 import view.CompleteViewContainer;
 import view.ViewCanvas;
 import exception.CannotRotateException;
 
 public class BuildController implements MouseListener, ActionListener, KeyListener {
 
 	private iOverlord overlord;
 	private ViewCanvas view;
 	private CompleteViewContainer frame;
 	private int currentSelectedMode;
 	
 	private int oldX;
 	private int oldY;
 	
 	private final int BUILD_SQUARE = 2;
 	private final int BUILD_TRIANGLE = 3;
 	private final int BUILD_LEFT_FLIPPER = 4;
 	private final int BUILD_RIGHT_FLIPPER = 5;
 	private final int BUILD_ABSORBER = 6;
 	private final int BUILD_REMOVE = 7;
 	private final int BUILD_ROTATE = 8;
 	private final int BUILD_ADD_TRIGGER_1 = 9;
 	private final int BUILD_ADD_TRIGGER_2 = 12;
 	private final int BUILD_REMOVE_TRIGGER_1 = 10;
 	private final int BUILD_REMOVE_TRIGGER_2 = 13;
 	private final int BUILD_REMOVE_KEY_TRIGGER_1 = 14;
 	private final int BUILD_REMOVE_KEY_TRIGGER_2 = 15;
 	private final int BUILD_ADD_KEY_TRIGGER_1 = 16;
 	private final int BUILD_ADD_KEY_TRIGGER_2 = 17;
 	private final int ADD_BALL = 1;
 	private final int BUILD_CIRCLE = 11;
 	private final int BUILD_MOVE_1 = 18;
 	private final int BUILD_MOVE_2 = 19;
 	private final int BUILD_MOVE_BALL_1 = 20;
 	private final int BUILD_MOVE_BALL_2 = 21;
 	private final int BUILD_REMOVE_BALL = 22;
 	private final int BUILD_PORTAL_1 = 23;
 	private final int BUILD_PORTAL_2 = 24;
 	
 	private String gizName;
 	private String oldGizName;
 	private String oldBall;
 	private int keyPressed;
 	private String selected = "";
 
 	public BuildController(iOverlord ov, ViewCanvas v,
 			CompleteViewContainer frame) {
 		view = v;
 		this.frame = frame;
 		overlord = ov;
 		currentSelectedMode = 0;
 		
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		
 		if(frame.getMode()){
 			//ignore mouse clicks during play mode.
 		}else{
 			int x = view.mouseX(e.getX());
 			int y = view.mouseY(e.getY());
 			
 			gizName = overlord.getGizName(x, y);
 			
 			if(!gizName.equals("")){
 				if(!selected.equals("")){
 				overlord.setGizSelected(selected, false);
 				overlord.setGizSelected(gizName, true);
 				selected = gizName;
 				}else{
 				overlord.setGizSelected(gizName, true);
 				selected = gizName;
 				}
 			}
 			String ballName = overlord.getBallName(x, y);
 			String type = "";
 			boolean success = false;
 	
 			if (x < 20 && y < 20) {
 				switch (currentSelectedMode) {
 				case ADD_BALL:
 					type = "ball";
 					if(gizName.contains("A")){
 						success = overlord.addBall(overlord.getNextName("B"), gizName, x, y, 0.0, 0.0);
 						frame.showBallInfo(false); 
 						
 					}else{
 						if(frame.getBallVX() != Double.MIN_VALUE && frame.getBallVY() != Double.MIN_VALUE){
 						success = overlord.addBall(overlord.getNextName("B"), "", x, y, frame.getBallVX(), frame.getBallVY());
 						frame.showBallInfo(false);
 						
 						}
 					}
 					break;
 				case BUILD_ABSORBER:
 					type = "absorber";
 					if(frame.getAbsorberHeight() != Integer.MIN_VALUE && frame.getAbsorberWidth() != Integer.MIN_VALUE){
 						success = overlord.addAbsorber(overlord.getNextName("A"), x, y, (x +frame.getAbsorberWidth()), (y + frame.getAbsorberHeight()));
 						frame.showAbsInfo(false);
 					}
 					break;
 				case BUILD_SQUARE:
 					type = "square";
 					success = overlord.addSquare(overlord.getNextName("S"), x, y);
 					break;
 				case BUILD_TRIANGLE:
 					type = "triangle";
 					success = overlord.addTriangle(overlord.getNextName("T"), x, y);
 					break;
 				case BUILD_CIRCLE:
 					type = "circle";
 					success = overlord.addCircle(overlord.getNextName("C"), x, y);
 					break; 	
 				case BUILD_ADD_TRIGGER_1:
 					type = "trigger";
 					oldGizName = gizName;
 					if(gizName.equals("")){
 						currentSelectedMode = 0;
 					}else{
 					currentSelectedMode = BUILD_ADD_TRIGGER_2;
 					}
 					break;
 				case BUILD_ADD_TRIGGER_2:
 					success = overlord.connect(oldGizName, gizName);
 					oldGizName = "";
 					type = "add a trigger";
 					currentSelectedMode = -1;
 					break;
 				case BUILD_ROTATE:
 					type = "rotate";
 					try {
 						success = overlord.rotateGizmo(gizName);
 					} catch (CannotRotateException e1) {
 						e1.printStackTrace();
 					}
 					break;
 				case BUILD_REMOVE:
 					type = "remove";
 					success = overlord.removeGizmo(gizName);
 					break;
 				case BUILD_LEFT_FLIPPER:
 					type = "Left Flipper";
 					success = overlord.addFlipper(overlord.getNextName("LF"), x, y, false);
 					
 					break;
 				case BUILD_RIGHT_FLIPPER:
 					type = "Right Flipper";
 					success = overlord.addFlipper(overlord.getNextName("RF"), x, y, true);
 					
 					break;
 				case BUILD_MOVE_1:
 					type = "moved";
 					oldGizName = gizName;
 					if(gizName.equals("")){
 						currentSelectedMode = 0;
 					}else{
 					currentSelectedMode = BUILD_MOVE_2;
 					}
 					break;
 				case BUILD_MOVE_2:
 					success = overlord.moveGizmo(oldGizName, x, y);
 					currentSelectedMode = -1;
 					break;
 				case BUILD_MOVE_BALL_1:
 					type = "moved";
 					oldBall = ballName;
 					if(oldBall.equals("")) {
 						currentSelectedMode = 0;
 						success = false;
 					}else{
 					currentSelectedMode = BUILD_MOVE_BALL_2;
 					}
 					break;
 				case BUILD_MOVE_BALL_2:
 					success = overlord.moveBall(oldBall, gizName, x, y);
 					currentSelectedMode = -1;
 					break;
 				case BUILD_REMOVE_TRIGGER_1:
 					type = "trigger";
 					oldGizName = gizName;
 					if(gizName.equals("")){
 						currentSelectedMode = 0;
 					}else{
 					currentSelectedMode = BUILD_REMOVE_TRIGGER_2;
 					}
 					break;
 				case BUILD_REMOVE_TRIGGER_2:
 					success = overlord.disconnect(oldGizName, gizName);
 					oldGizName = "";
 					type = "remove the trigger";
 					currentSelectedMode = -1;
 					break;
 				case BUILD_ADD_KEY_TRIGGER_2:
 					if(gizName.equals("")){
 						currentSelectedMode = 0;
 					}else{
 					success = overlord.keyConnect(keyPressed, false, gizName);
 					if(success){
 						success = overlord.keyConnect(keyPressed, true, gizName);
 					}
 					type = "add a key connection ";
 					currentSelectedMode = -1;
 					}
 					break;
 				case BUILD_REMOVE_KEY_TRIGGER_2:
 					if(gizName.equals("")){
 						currentSelectedMode = 0;
 					}else{
 					success = overlord.removeKeyConnect(keyPressed, false, gizName);
 					if(success){
 						success = overlord.removeKeyConnect(keyPressed, true, gizName);
 					}
 					type = "remove the key connection ";
 					currentSelectedMode = -1;
 					}
 					break;
 				case BUILD_REMOVE_BALL:
 					type = "remove ball";
 					success = overlord.removeBall(ballName);
 					break;
 				case BUILD_PORTAL_1:
 					type = "portal";
 					oldX = x;
 					oldY = y;
 					currentSelectedMode = BUILD_PORTAL_2;
 					break;
 				case BUILD_PORTAL_2:
 					success = overlord.addPortal(overlord.getNextName("P"), oldX, oldY, x, y);
 					currentSelectedMode = -1;
 					break;
 				default:
 					currentSelectedMode = 0;
 					break;
 				}
 				
 				if(currentSelectedMode      != BUILD_ADD_TRIGGER_2    && 
 						currentSelectedMode != BUILD_REMOVE_TRIGGER_2 && 
 						currentSelectedMode != BUILD_MOVE_2           && 
 						currentSelectedMode != BUILD_MOVE_BALL_2      && 
 						currentSelectedMode != BUILD_PORTAL_2) {
 					frame.unselectAll();
 					
 					if(!success && currentSelectedMode != 0 && 
 							!(currentSelectedMode == BUILD_ADD_TRIGGER_2 || currentSelectedMode == BUILD_REMOVE_TRIGGER_2
 							|| currentSelectedMode == BUILD_ADD_KEY_TRIGGER_2 || currentSelectedMode == BUILD_REMOVE_KEY_TRIGGER_2
 							)){
 						frame.error("The " + type + " gizmo could not be added at that location!");
 					}else if(!success && currentSelectedMode == -1){
 						frame.error("Could not " + type + " between the selected gizmos.");
 					}
 					currentSelectedMode = 0;
 					success = false;
 					type = "";
 					oldGizName = "";
 					oldBall = "";
 				}
 	
 			}
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		frame.unselectAll();
 		frame.select(e.getActionCommand());
 		frame.focusView();
 		switch (e.getActionCommand()) {
 		
 		case "Load":
 			if (frame != null) {
 				java.io.File f = frame.askForMapFile();
 				if (f != null && f.exists()) {
 					overlord.loadGame(f.getPath());
 					//frame.information(f.getPath()); // TODO Implement with model
 				} else if (f != null) {
 					frame.error("This file doesn't exist.");
 				}
 			} 
 			break;
 		case "Save":
 			java.io.File f = frame.askForMapFileSave();
 			if (f != null && !f.exists()) {
 				overlord.saveGame(f.getPath());
 				//frame.information(f.getPath()); // TODO Implement with model
 			} else if (f != null) {
 				frame.error("I can't overwrite an existing file.");
 			}
 			break;
 		case "Exit":
 			frame.dispose();
 			System.exit(0);
 			break;
 		case "Build":
 			frame.switchBuild();
 			break;
 		case "Play":
 			frame.switchPlay();
 			break;
 		case "Pause":
 			frame.pause();
 			break;
 	
 		case "AddBall":
 			currentSelectedMode = ADD_BALL;
 			frame.showBallInfo(true);
 			break;
 		case "Square": // TODO Implement with model
 			currentSelectedMode = BUILD_SQUARE;
 		
 			break;
 		case "Triangle": // TODO Implement with model
 			currentSelectedMode = BUILD_TRIANGLE;
 			
 			break;
 		case "LeftFlipper": // TODO Implement with model
 			currentSelectedMode = BUILD_LEFT_FLIPPER;
 			
 			break;
 		case "Circle":
 			currentSelectedMode = BUILD_CIRCLE;
 			break;
 		case "RightFlipper": // TODO Implement with model
 			currentSelectedMode = BUILD_RIGHT_FLIPPER;
 	
 			break;
 		case "Absorber": // TODO Implement with model
 			currentSelectedMode = BUILD_ABSORBER;
 			frame.showAbsInfo(true);
 			break;
 		case "DeleteBall":
 			currentSelectedMode = BUILD_REMOVE_BALL;
 			break;
 		case "MoveBall":
 			currentSelectedMode = BUILD_MOVE_BALL_1;
 			break;
 		case "Remove": // TODO Implement with model
 			currentSelectedMode = BUILD_REMOVE;
 		
 			break;
 		case "Rotate": // TODO Implement with model
 			currentSelectedMode = BUILD_ROTATE;
 			
 			break;
 		case "AddTrigger": // TODO Implement with model
 			currentSelectedMode = BUILD_ADD_TRIGGER_1;
 		
 			break;
 		case "RemoveTrigger": // TODO Implement with model
 			currentSelectedMode = BUILD_REMOVE_TRIGGER_1;
 			
 			break;
 		case "AddKeyTrigger":
 			currentSelectedMode = BUILD_ADD_KEY_TRIGGER_1;
 			frame.showKeyInfo(true);
 			break;
 		case "RemoveKeyTrigger":
 			currentSelectedMode = BUILD_REMOVE_KEY_TRIGGER_1;
 			frame.showKeyInfo(true);
 			break;
 		case "Move":
 			currentSelectedMode = BUILD_MOVE_1;
 			break;
 		case "Portal":
 			currentSelectedMode = BUILD_PORTAL_1;
 			break;
 			
 		default:
 		}
 
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if(currentSelectedMode == BUILD_REMOVE_KEY_TRIGGER_1){
 			keyPressed = e.getKeyCode();
 			frame.setKey(keyPressed);
 			currentSelectedMode = BUILD_REMOVE_KEY_TRIGGER_2;
 		}else if(currentSelectedMode == BUILD_ADD_KEY_TRIGGER_1){
 			keyPressed = e.getKeyCode();
 			frame.setKey(keyPressed);
 			currentSelectedMode = BUILD_ADD_KEY_TRIGGER_2;
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
