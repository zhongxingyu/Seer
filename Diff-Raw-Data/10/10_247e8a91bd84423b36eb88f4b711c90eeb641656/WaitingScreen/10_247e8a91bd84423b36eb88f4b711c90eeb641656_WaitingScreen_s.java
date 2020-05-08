 package edu.rit.se.sse.rapdevx.gui.screens;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import edu.rit.se.sse.rapdevx.clientstate.GameSession;
 import edu.rit.se.sse.rapdevx.clientstate.UnitPlacementState;
 import edu.rit.se.sse.rapdevx.events.StateEvent;
 import edu.rit.se.sse.rapdevx.events.StateListener;
 import edu.rit.se.sse.rapdevx.gui.Screen;
 import edu.rit.se.sse.rapdevx.gui.ScreenStack;
 import edu.rit.se.sse.rapdevx.gui.drawable.Text;
 import edu.rit.se.sse.rapdevx.gui.screens.menus.Menu;
import edu.rit.se.sse.rapdevx.gui.screens.menus.MenuButton;
 
 public class WaitingScreen extends Screen implements ActionListener,
 		StateListener {
 
 	private Menu menu;
	private MenuButton exitButton;
 
 	private Text waitingText;
 
 	public WaitingScreen(int width, int height) {
 		super(width, height);
 
 		GameSession.get().addStateListener(this);
 
 		menu = new Menu(width / 2 - 100, height / 2 - 200);
		exitButton = new MenuButton(menu, "Exit", "Click this to Quit!");
 		exitButton.addActionListener(this);
 		menu.addButton(exitButton);
 
 		waitingText = new Text("Wating for another player...", width / 2 - 300,
 				height / 2 - 300, 3.0, Color.LIGHT_GRAY);
 	}
 
 	public void init() {
 		ScreenStack.get().addScreen(menu);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == exitButton) {
 			// TODO fix this
 			System.exit(0);
 		}
 	}
 
 	public void update(boolean hasFocus, boolean isVisible) {
 		
 	}
 
 	public void draw(Graphics2D gPen) {
 		gPen.setColor(Color.BLACK);
 		gPen.fillRect(0, 0, screenWidth, screenHeight);
 
 		waitingText.draw(gPen);
 	}
 
 	public void stateChanged(StateEvent e) {
 		if (e.getNewState() instanceof UnitPlacementState) {
 			// Start with the move phase on the map
 			MapScreen mapScreen = new MapScreen(screenWidth, screenHeight);
 			ScreenStack.get().addScreen(mapScreen);
 
 			OverlayScreen overlay = new OverlayScreen(screenWidth, screenHeight);
 
 			// Start with a unit placement screen
 			PlacementScreen placement = new PlacementScreen(overlay,
 					mapScreen.getCamera(), screenWidth, screenHeight);
 			ScreenStack.get().addScreen(placement);
 
 			// Add the overlay last
 			ScreenStack.get().addScreen(overlay);
 
 			GameSession.get().removeStateListener(this);
 			ScreenStack.get().removeScreen(menu);
 			ScreenStack.get().removeScreen(this);
 		}
 	}
 	
 }
