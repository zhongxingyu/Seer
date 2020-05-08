 package edu.rit.se.sse.rapdevx.gui.screens;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Area;
 import java.util.ArrayList;
 
 import edu.rit.se.sse.rapdevx.api.dataclasses.Ability;
 import edu.rit.se.sse.rapdevx.clientmodels.Ship;
 import edu.rit.se.sse.rapdevx.clientstate.GameSession;
 import edu.rit.se.sse.rapdevx.clientstate.MoveState;
import edu.rit.se.sse.rapdevx.events.AbilitiesListener;
 import edu.rit.se.sse.rapdevx.events.StateEvent;
 import edu.rit.se.sse.rapdevx.events.StateListener;
 import edu.rit.se.sse.rapdevx.gui.Screen;
 import edu.rit.se.sse.rapdevx.gui.ScreenStack;
 import edu.rit.se.sse.rapdevx.gui.drawable.Camera;
 import edu.rit.se.sse.rapdevx.gui.drawable.DrawableAttack;
 import edu.rit.se.sse.rapdevx.gui.drawable.DrawableMovePath;
 import edu.rit.se.sse.rapdevx.gui.drawable.DrawableShip;
 import edu.rit.se.sse.rapdevx.gui.screens.menus.Menu;
 import edu.rit.se.sse.rapdevx.gui.screens.menus.MenuButton;
 
 public class AttackScreen extends Screen implements StateListener, ActionListener {
 	/** A reference to the map camera for positioning objects in world space */
 	private Camera camera;
 
 	/** A list of ships currently on the field */
 	private ArrayList<DrawableShip> shipList = new ArrayList<DrawableShip>();
 	private DrawableShip selectedShip;
 
 	/** A list of all the attack paths on the field */
 	private ArrayList<DrawableAttack> attackList = new ArrayList<DrawableAttack>();
 	private DrawableAttack curAttack = null;
 
 	/** A stats screen that shows when a ship is moused over */
 	private StatsScreen statsScreen = null;
 
 	private Menu abilitiesMenu = null;
 
 	public AttackScreen(Camera camera, int width, int height) {
 		super(width, height);
 		this.camera = camera;
 
 		GameSession.get().addStateListener(this);
 
 		Ship ship = new Ship();
 		ship.setX(150);
 		ship.setY(150);
 
 		shipList.add(new DrawableShip(ship, new Color(48, 129, 233)));
 
 		ship = new Ship();
 		ship.setX(20);
 		ship.setY(50);
 
 		shipList.add(new DrawableShip(ship, new Color(48, 129, 233)));
 
 		Ship ship2 = new Ship();
 		ship2.setX(300);
 		ship2.setY(300);
 		shipList.add(new DrawableShip(ship2, new Color(100, 255, 130)));
 
 		ship2 = new Ship();
 		ship2.setX(10);
 		ship2.setY(200);
 		shipList.add(new DrawableShip(ship2, new Color(100, 255, 130)));
 	}
 
 	public void update(boolean hasFocus, boolean isVisible) {
 		// Make sure the selected ship shows a selected indicator
 		if (selectedShip != null)
 			selectedShip.setSelected(true);
 
 		// Update all the ships on screen
 		for (DrawableShip ship : shipList) {
 			ship.update();
 		}
 	}
 
 	public void draw(Graphics2D gPen) {
 		// Translate the coordinates based on the camera
 		Rectangle cameraBounds = camera.getBounds();
 		gPen.translate(-cameraBounds.getX(), -cameraBounds.getY());
 
 		/** Draw things based on the camera here **/
 
 		// Draw all the ships on the map
 		for (DrawableShip ship : shipList) {
 			ship.draw(gPen, cameraBounds);
 		}
 
 		// Draw all the attack targets
 		if (curAttack != null) {
 			curAttack.draw(gPen, cameraBounds);
 		}
 
 		for (DrawableAttack attack : attackList)
 			attack.draw(gPen, cameraBounds);
 
 		// Change the drawing back to screen based coordinates
 		gPen.translate(cameraBounds.getX(), cameraBounds.getY());
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		// Check to see if one of the ships was clicked
 		for (DrawableShip ship : shipList) {
 			if (new Area(ship.getBounds()).contains(
 					e.getX() + camera.getX(),
 					e.getY() + camera.getY())) {
 				if (ship == selectedShip) {
 					// This ship was previously selected. Deselect it
 					// and stop the attack.
 					selectedShip.setSelected(false);
 					selectedShip = null;
 					curAttack = null;
 					abilitiesMenu = null;
 				} else if (selectedShip != null) {
 					// Another ship was selected, complete the attack
 					// and deselect the ship
 					// curAttack.setTarget(ship, camera.getBounds());
 					selectedShip.setSelected(false);
 					selectedShip = null;
 
 					// Save the completed attack
 					attackList.add(curAttack);
 					curAttack = null; 
 				} else {
 					// No other ship was selected, select this one and
 					// start an attack
 					selectedShip = ship;
 					selectedShip.setSelected(true);
 					abilitiesMenu = new Menu(
 							selectedShip.getX() + 50,
 							selectedShip.getY() + 20);
 					ScreenStack.get().addScreen(abilitiesMenu);
 					// for (Ability b:
 					// selectedShip.getShip().getShipClass().getAbilities()){
 					// // abilitiesMenu.addButton(new
 					// MenuButton(b.getName(), "abilitiy" ));
 					// }
 					abilitiesMenu.addButton(new MenuButton(
 							abilitiesMenu, "Fly", "attack"));
 					abilitiesMenu
 							.addButton(new MenuButton(
 									abilitiesMenu,
 									"Hydropump", "attack"));
 					abilitiesMenu.addButton(new MenuButton(
 							abilitiesMenu, "Tackle", "attack"));
 					abilitiesMenu.addButton(new MenuButton(
 							abilitiesMenu, "Quick Attack",
 							"attack"));
 					abilitiesMenu
 							.addButton(new MenuButton(
 									abilitiesMenu,
 									"Hypnotize", "attack"));
 					for (MenuButton button : abilitiesMenu
 							.getButtons()) {
 						button.addActionListener(this);
 					}
 				}
 			}
 		}
 
 		if (selectedShip == null)
 			curAttack = null;
 		e.consume();
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		if (curAttack != null) {
 			// Snap the reticle to a ship if we are hovering over one
 			for (DrawableShip ship : shipList) {
 				if (ship == selectedShip)
 					continue;
 
 				if (new Area(ship.getBounds()).contains(e.getX()
 						+ camera.getX(), e.getY() + camera.getY())) {
 					setShipSelected(ship, true);
 
 					curAttack.setMouseLocation(ship.getCenter());
 					curAttack.setSnapped(true);
 
 					e.consume();
 					return;
 				} else {
 					setShipSelected(ship, false);
 				}
 			}
 
 			// No ship under the mouse, clear snapping
 			curAttack.setSnapped(false);
 
 			// If there is an attack selection in progress, update the
 			// reticle
 			curAttack.setMouseLocation(new Point(
 					e.getX() + camera.getX(), e.getY()
 							+ camera.getY()));
 		} else {
 			for (DrawableShip ship : shipList) {
 				if (new Area(ship.getBounds()).contains(e.getX()
 						+ camera.getX(), e.getY() + camera.getY())) {
 					setShipSelected(ship, true);
 				} else {
 					setShipSelected(ship, false);
 				}
 			}
 		}
 		e.consume();
 	}
 
 	public void stateChanged(StateEvent e) {
 		if (e.getNewState() instanceof MoveState) {
 			ScreenStack.get()
 					.addScreenAfter(
 							this,
 							new MoveScreen(camera, screenWidth,
 									screenHeight));
 			ScreenStack.get().removeScreen(this);
 			GameSession.get().removeStateListener(this);
 		}
 	}
 
 	private void selectShip(DrawableShip ship) {
 		setShipSelected(ship, true);
 
 		selectedShip = ship;
 	}
 
 	private void deselectShip() {
 		setShipSelected(selectedShip, false);
 
 		selectedShip = null;
 	}
 
 	private void setShipSelected(DrawableShip ship, boolean isSelected) {
 		ship.setSelected(isSelected);
 
 		if (isSelected) {
 			if (statsScreen == null) {
 				statsScreen = new StatsScreen(
 				/* 300, 200, */screenWidth, screenHeight,
 						ship.getShip());
 				ScreenStack.get().addScreenAfter(this, statsScreen);
 			} else if (statsScreen != null
 					&& statsScreen.getShip() != ship.getShip()) {
 				ScreenStack.get().removeScreen(statsScreen);
 				statsScreen = null;
 			}
 		} else {
 			// If the there is a stats screen for this ship, get rid of it
 			if (statsScreen != null
 					&& statsScreen.getShip() == ship.getShip()) {
 				ScreenStack.get().removeScreen(statsScreen);
 				statsScreen = null;
 			}
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		ScreenStack.get().removeScreen(((MenuButton) e.getSource()).getMenu());
 		curAttack = new DrawableAttack(selectedShip);
 		
 	}
 
 }
