 package edu.rit.se.sse.rapdevx.gui;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.image.BufferStrategy;
 
 import javax.swing.JFrame;
 
import edu.rit.se.sse.rapdevx.gui.screens.PathTestScreen;
 import edu.rit.se.sse.rapdevx.gui.screens.menus.Menu;
 import edu.rit.se.sse.rapdevx.gui.screens.menus.MenuButton;
 
 public class Window {
 	
 	private JFrame window;
 	
 	private GraphicsDevice graphics;
 	private GraphicsConfiguration graphicsConfig;
 	private BufferStrategy bufferStrategy;
 	
 	private ScreenStack screenStack;
 	
 	public Window(String title) {
 		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		graphics = env.getDefaultScreenDevice();
 		graphicsConfig = graphics.getDefaultConfiguration();
 		
 		/**** Create the window ****/
 		window = new JFrame(title, graphicsConfig);
 		
 		// Set some window properties
 		window.setSize(1024, 768);
 		window.setLocationRelativeTo(null);
 		window.setResizable(false);
 		window.setIgnoreRepaint(true);
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		window.setVisible(true);
 		
 		// Setup double buffering (makes graphics faster)
 		window.createBufferStrategy(2);
 		bufferStrategy = window.getBufferStrategy();
 		
 		
 		/**** Create the panel to draw on ****/
 		screenStack = new ScreenStack();
 		
 		// Add a map screen on startup
 		MapScreen mapScreen = new MapScreen(window.getWidth(), window.getHeight());
 		screenStack.addScreen(mapScreen);
 		
 		// Start with the move phase on the map
 		screenStack.addScreen(new MoveScreen(mapScreen.getCamera(), window.getWidth(), window.getHeight()));
 		
 		//TODO remove after testing
 		Menu testMenu = new Menu(300, 300);
 		MenuButton playButton = new MenuButton("Play", "This button does nothing yet");
 		MenuButton settingsButton = new MenuButton("Settings", "This button does nothing yet");
 		MenuButton helpButton = new MenuButton("Help", "This button does nothing yet");
 		testMenu.addButton(playButton);
 		testMenu.addButton(settingsButton);
 		testMenu.addButton(helpButton);
		//screenStack.addScreen(testMenu);
 		
 		//Testing a ship screen
 		StatsScreen testStatsScreen = new StatsScreen(300, 200, window.getWidth(), window.getHeight(), null);
 		screenStack.addScreen(testStatsScreen);
 		
		//screenStack.addScreen(new PathTestScreen(window.getWidth(), window.getHeight()));
		
 		// Add the panel to the window
		//window.getContentPane().add(screenStack);
 		window.addKeyListener(screenStack);
 		window.addMouseListener(screenStack);
 		window.addMouseMotionListener(screenStack);
 		window.requestFocusInWindow();
 	}
 	
 	public void update() {
 		screenStack.update();
 	}
 	
 	public void render() {
 		// Create a graphics object from the buffer strategy
 		// and draw the screens
 		Graphics gPen = null;
 		
 		try {
 			gPen = bufferStrategy.getDrawGraphics();
 			if (!bufferStrategy.contentsLost()) {
 				screenStack.draw((Graphics2D)gPen);
 				bufferStrategy.show();
 			}
 		} finally {
 			if (gPen != null) {
 				gPen.dispose();
 			}
 		}
 	}
 }
