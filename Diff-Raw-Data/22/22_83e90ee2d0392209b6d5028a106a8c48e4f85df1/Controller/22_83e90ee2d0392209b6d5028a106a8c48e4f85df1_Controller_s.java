 package controller;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
import javax.swing.JToggleButton;
 
 import model.Ball;
 import model.Board;
 import model.IPhysicsEngine;
 import model.Loader;
 import model.gizmos.AbsorberGizmo;
 import model.gizmos.CircleBumper;
 import model.gizmos.IGizmo;
 import model.gizmos.LeftFlipper;
 import model.gizmos.RightFlipper;
 import model.gizmos.SquareBumper;
 import model.gizmos.TriangleBumper;
 
 import view.window.AnimationPanel;
 import view.window.ApplicationWindow;
 
 import exceptions.BadFileException;
 
 /**
  * 
  * @author Group 4
  * 
  *         This is the Controller for the MVC model, it takes action events from
  *         the view and handles them appropriately by changing the model.
  * 
  */
 public class Controller {
 
 	private Board boardModel;
 	private IPhysicsEngine engine;
 
 	private ApplicationWindow appWin;
 	private TriggerHandler handler;
 	private IGizmo selectedGizmo;
 
 	private int ax;
 	private int ay;
 	private int ax2;
 	private int ay2;
 
 	private char command;
 	private IGizmo keyLinkGiz;
 	private Integer keyLinkKey;
 	
 	IGizmo gizmo = null;
 
 	public Controller(IPhysicsEngine physics, Board model,
 			ApplicationWindow applicationWindow) {
 		this.boardModel = model;
 		engine = physics;
 		appWin = applicationWindow;
 		handler = new TriggerHandler();
 		addListeners();
 	}
 
 	/**
 	 * Add listeners to the application window.
 	 */
 	public void addListeners() {
 		appWin.addButtonListeners(new ButtonListener());
 		appWin.addGridListner(new GridListener());
 		appWin.addMenuListner(new SavesListener());
 		appWin.addEditKeyListener(new LinkListener());
 	}
 
 	/**
 	 * SavesListener controls the loading of previously created Gizmoball games
 	 * and saving of games created by the user while in edit mode.
 	 */
 	private class SavesListener implements ActionListener {
 
 		@Override
 		/**
 		 * Checks the actionCommand string, saving or
 		 * loading depending on the result. 
 		 */
 		public void actionPerformed(ActionEvent e) {
 			if (e.getActionCommand().equals("Save")) { // Need to save
 				System.out.println("Gotta Save!");
 			} else { // Otherwise allow user to load a file.
 				JFileChooser chooser = new JFileChooser();
 				chooser.showOpenDialog(chooser);
 				File file = chooser.getSelectedFile();
 				String fileName = file.getAbsolutePath();
 
 				try {
 					Loader loader = new Loader(fileName);
 					loader.parseFile(engine);
 					loader.loadItems(boardModel);
 
 					handler = new TriggerHandler(loader.getKeyUpTriggers(),
 							loader.getKeyDownTriggers());
 					MagicKeyListener listener = new MagicKeyListener(handler);
 					appWin.addKeyListener(listener);
 				} catch (FileNotFoundException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				} catch (BadFileException e2) {
 					// TODO Auto-generated catch block
 					e2.printStackTrace();
 				} catch (IOException e3) {
 					// TODO Auto-generated catch block
 					e3.printStackTrace();
 				}
 
 			}
 		}
 
 	}
 
 	/**
 	 * GridListener handles all events that happen to the EditGrid, e.g. a user
 	 * clicking on the grid when the 'CircleGizmo' button is selected will check
 	 * the validity of a square and place the gizmo if valid.
 	 */
 	private class GridListener implements MouseListener, MouseMotionListener {
 		
 		@Override
 		/**
 		 * When the mouse is clicked on the grid, this method checks
 		 * to see if a command (i.e. edit button selected) has been 
 		 * set, and carries out the appropriate action. 
 		 */
 		public void mouseClicked(MouseEvent e) {
 
 			AnimationPanel ap = (AnimationPanel) e.getComponent();
 			int x = (int) Math.round(e.getX() / ap.getScaleX());
 			int y = (int) Math.round(e.getY() / ap.getScaleY());
 			int w = 0;
 			int h = 0;
 
 			
 			Ball ball = null;
 
 			switch (command) {
 
 			case 'C':
 				gizmo = new CircleBumper(x, y);
 				w = gizmo.getWidth();
 				h = gizmo.getHeight();
 				if (validLocation(x, y, w, h)) {
 					boardModel.addGizmo(gizmo);
 				}
 				break;
 
 			case 'S':
 				gizmo = new SquareBumper(x, y);
 				w = gizmo.getWidth();
 				h = gizmo.getHeight();
 				if (validLocation(x, y, w, h)) {
 					boardModel.addGizmo(gizmo);
 				}
 				break;
 
 			case 'T':
 				gizmo = new TriangleBumper(x, y, 0);
 				w = gizmo.getWidth();
 				h = gizmo.getHeight();
 				if (validLocation(x, y, w, h)) {
 					boardModel.addGizmo(gizmo);
 				}
 				break;
 
 			case 'F':
 				gizmo = new RightFlipper(x, y);
 				w = gizmo.getWidth();
 				h = gizmo.getHeight();
 				if (validLocation(x, y, w, h)) {
 					boardModel.addGizmo(gizmo);
 				}
 				break;
 
 			case 'G':
 				gizmo = new LeftFlipper(x, y);
 				w = gizmo.getWidth();
 				h = gizmo.getHeight();
 				if (validLocation(x, y, w, h)) {
 					boardModel.addGizmo(gizmo);
 				}
 				break;
 
 			case 'B':
				ball = new Ball(e.getX(), e.getY(), 5, 5);
 				if (validLocation(x, y, 1, 1)) {
 					boardModel.addBall(ball);
 				}
 
 				break;
 
 			case 'R':
 				gizmo = boardModel.getGizmoAt(x, y);
 
 				if (gizmo != null) {
 					try {
 						gizmo.rotate();
 					} catch (UnsupportedOperationException e1) {
 						// TODO Do all the rotation functions actually work?
 					}
 				}
 				break;
 
 			case 'D':
 				gizmo = boardModel.getGizmoAt(x, y);
 				ball = boardModel.getBallAt(x, y);
 				if (gizmo != null) {
 					boardModel.removeGizmo(gizmo);
 				} else if (ball != null) {
 					boardModel.removeBall(ball);
 				}
 
 				break;
 			case 'K':
 				gizmo = boardModel.getGizmoAt(x, y);
 
 				if (keyLinkKey != null) {
 					handler.addLink(keyLinkKey, gizmo);
 					keyLinkKey = null;
 				}
 
 			default:
 				break;
 			}
 			
 			gizmo = null;
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			AnimationPanel ap = (AnimationPanel) e.getComponent();
 			ap.removeLoactionIndicator();
 		}
 
 		@Override
 		/**
 		 * If the mouse has left the grid, deactivate the validity
 		 * guidance square. 
 		 */
 		public void mouseExited(MouseEvent e) {
 			AnimationPanel ap = (AnimationPanel) e.getComponent();
 			ap.removeLoactionIndicator();
 		}
 
 		@Override
 		/**
 		 * The absorber has a user defined size, controlled by
 		 * their dragging of the mouse whilst in the grid area.
 		 */
 		public void mousePressed(MouseEvent e) {
 			AnimationPanel ap = (AnimationPanel) e.getComponent();
 			switch (command) {
 			case 'A':
 				ax = (int) Math.round(e.getX() / ap.getScaleX());
 				ay = (int) Math.round(e.getY() / ap.getScaleY());
 				break;
 				
 			case 'Z':
 				ax = (int) Math.round(e.getX() / ap.getScaleX());
 				ay = (int) Math.round(e.getY() / ap.getScaleY());
 				gizmo = boardModel.getGizmoAt(ax, ay);
 				break;
 			}
 			
 				
 			drawValidityBox(e);
 		}
 
 		@Override
 		/**
 		 * When the mouse is released, and the absorber command is
 		 * selected, create an absorber. 
 		 */
 		public void mouseReleased(MouseEvent e) {
 			AnimationPanel ap = (AnimationPanel) e.getComponent();
 			switch (command) {
 			case 'A':
 				ax2 = (int) Math.round(e.getX() / ap.getScaleX());
 				ay2 = (int) Math.round(e.getY() / ap.getScaleY());
 				selectedGizmo = getNormailisedAbsorber();
 				if (validLocation(selectedGizmo.getX(), selectedGizmo.getY(),
 						selectedGizmo.getWidth(), selectedGizmo.getHeight())) {
 					boardModel.addGizmo(selectedGizmo);
 				}
 				ax = ax2;
 				ay = ay2;
 				break;
 			
 			case 'Z':
 				ax2 = (int) Math.round(e.getX() / ap.getScaleX());
 				ay2 = (int) Math.round(e.getY() / ap.getScaleY());
 				
 				if(boardModel.getGizmoAt(ax2, ay2) == null &&gizmo != null && validLocation(ax2, ay2, gizmo.getWidth(), gizmo.getHeight())){
 					gizmo.move(ax2, ay2);
 				}
 				gizmo = null;
 			}
 			drawValidityBox(e);
 		}
 
 		@Override
 		/**
 		 * While the mouse is dragged, maintain a normalised value of the potential
 		 * absorbers width and height. 
 		 */
 		public void mouseDragged(MouseEvent e) {
 			switch (command) {
 			case 'A':
 				AnimationPanel ap = (AnimationPanel) e.getComponent();
 				ax2 = (int) Math.round(e.getX() / ap.getScaleX());
 				ay2 = (int) Math.round(e.getY() / ap.getScaleY());
 				break;
 			}
 			drawValidityBox(e);
 		}
 
 		@Override
 		/**
 		 * Maintaint the validity box while mouse is over the grid.
 		 */
 		public void mouseMoved(MouseEvent e) {
 			AnimationPanel ap = (AnimationPanel) e.getComponent();
 			ax = (int) Math.round(e.getX() / ap.getScaleX());
 			ay = (int) Math.round(e.getY() / ap.getScaleY());
 			ax2 = ax;
 			ay2 = ay;
 			drawValidityBox(e);
 		}
 
 		/**
 		 * Draw the Validity Guidance Square on the screen.
 		 * 
 		 * @param e
 		 *            - MouseEvent used got gaining the mouses position.
 		 */
 		private void drawValidityBox(MouseEvent e) {
 
 			AnimationPanel ap = (AnimationPanel) e.getComponent();
 
 			int w = 1;
 			int h = 1;
 
 			int x = (int) Math.round(e.getX() / ap.getScaleX());
 			int y = (int) Math.round(e.getY() / ap.getScaleY());
 
 			switch (command) {
 			case 'C':
 				selectedGizmo = new CircleBumper(x, y);
 				break;
 
 			case 'S':
 				selectedGizmo = new SquareBumper(x, y);
 				break;
 
 			case 'T':
 				selectedGizmo = new TriangleBumper(x, y, 0);
 				break;
 
 			case 'F':
 				selectedGizmo = new RightFlipper(x, y);
 				break;
 
 			case 'G':
 				selectedGizmo = new LeftFlipper(x, y);
 				break;
 
 			case 'A':
 				selectedGizmo = getNormailisedAbsorber();
 				x = selectedGizmo.getX();
 				y = selectedGizmo.getY();
 				break;
 				
 			case 'Z':
 				selectedGizmo = gizmo;
 				break;
 
 			default:
 				selectedGizmo = null;
 				break;
 			}
 
 			if (selectedGizmo != null) {
 				w = selectedGizmo.getWidth();
 				h = selectedGizmo.getHeight();
 				ap.setLoactionIndicator(x, y, w, h,
 						(validLocation(x, y, w, h) ? Color.GREEN : Color.RED));
 			} else if (command == 'B') {
 				ap.setLoactionIndicator(x, y, 1, 1,
 						(validLocation(x, y, 1, 1) ? Color.GREEN : Color.RED));
 			} else if (command == 'Z' || command == 'R'){
 				ap.setLoactionIndicator(x, y, 1, 1,
 						(validLocation(x, y, 1, 1) ? Color.RED : Color.GREEN));
 		    }else {
 				ap.removeLoactionIndicator();
 			}
 
 		}
 	}
 
 	/**
 	 * 
 	 * @return - allows for the user to click and drag in any direction 
 	 * 			 when placing an absorber gizmo. 
 	 */
 	private IGizmo getNormailisedAbsorber() {
 		int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
 
 		if (ax > ax2) {
 			x1 = ax2;
 			x2 = ax;
 		} else {
 			x1 = ax;
 			x2 = ax2;
 		}
 
 		if (ay > ay2) {
 			y1 = ay2;
 			y2 = ay;
 		} else {
 			y1 = ay;
 			y2 = ay2;
 		}
 
 		x2++;
 		y2++;
 
 		return new AbsorberGizmo(x1, y1, x2, y2);
 	}
 
 	/**
 	 * Check the validity of a gizmo/ball placement.
 	 * 
 	 * @param x
 	 *            - x position of gizmo being validated.
 	 * @param y
 	 *            - y position of the gizmo being validated.
 	 * @param w
 	 *            - width of the gizmo being validated.
 	 * @param h
 	 *            - height of the gizmo being validated.
 	 * @return - True if valid, false otherwise.
 	 */
 	private boolean validLocation(int x, int y, int w, int h) {
 		if (x + w > boardModel.getWidth() || y + h > boardModel.getHeight()) {
 			return false;
 		}
 		for (int i = 0; i < w; i++) {
 			for (int j = 0; j < h; j++) {
 				if (boardModel.getGizmoAt(x + i, y + j) != null && !boardModel.getGizmoAt(x + i, y + j).equals(gizmo)) {
 					return false;
 				} else if (boardModel.getBallAt(x + i, y + j) != null) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Key listener for handling the linking of gizmos.
 	 */
 	private class LinkListener implements KeyListener {
 
 		@Override
 		public void keyPressed(KeyEvent e) {
 		}
 
 		@Override
 		public void keyReleased(KeyEvent e) {
 		}
 
 		@Override
 		public void keyTyped(KeyEvent e) {
 			if (keyLinkGiz != null) {
 				handler.addLink(e.getKeyCode(), keyLinkGiz);
 				keyLinkGiz = null;
 			} else {
 				keyLinkKey = e.getKeyCode();
 			}
 		}
 
 	}
 
 	/**
 	 * Listener for the GizmoBall Button Area, when a button is pressed it sets
 	 * a command placeholder. If the button pressed has an immediate action, it
 	 * is carried out here.
 	 */
 	private class ButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			System.out.println(e.getActionCommand());
 			command = e.getActionCommand().toUpperCase().charAt(0);
 
 			if (command == 'M') {
 				appWin.flipMode();
 				boardModel.runMode();
 				JButton temp = (JButton) e.getSource();
 				if (temp.getText().equals("Play")) {
 					temp.setText("Edit");
 				} else {
 					temp.setText("Play");
 				}
 			}
 		}
 	}
 
 }
