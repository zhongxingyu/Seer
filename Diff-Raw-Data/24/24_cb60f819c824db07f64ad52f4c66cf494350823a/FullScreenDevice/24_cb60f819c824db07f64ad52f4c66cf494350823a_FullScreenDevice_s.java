 package com.tesserate.game.api.ui;
 import java.awt.Color;
 import java.awt.DisplayMode;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.RenderingHints;
 import java.awt.Window;
 import java.awt.image.BufferStrategy;
 
 import javax.swing.JFrame;
 
 import com.tesserate.game.api.ControllerDevice;
 
 public class FullScreenDevice {
 	private static FullScreenDevice instance;
 	private JFrame mainFrame;
 	private BufferStrategy bufferStrategy;
 	private GraphicsDevice device;
 	private ControllerDevice controller;
 	private static int height; 
 	private static int width; 
 	
     private static DisplayMode[] BEST_DISPLAY_MODES = new DisplayMode[] {
     	new DisplayMode(1280, 800, 32, DisplayMode.REFRESH_RATE_UNKNOWN),
     	new DisplayMode(1440, 900, 32, DisplayMode.REFRESH_RATE_UNKNOWN),
     	new DisplayMode(800, 600, 32, DisplayMode.REFRESH_RATE_UNKNOWN),
         new DisplayMode(800, 600, 16, DisplayMode.REFRESH_RATE_UNKNOWN),
         new DisplayMode(800, 600, 8, DisplayMode.REFRESH_RATE_UNKNOWN)
     };
     
 	public FullScreenDevice(){
 		
 	}
 
 	public static FullScreenDevice getInstance() {
 		if(instance == null){
 			instance = new FullScreenDevice();
 		}
 		return instance;
 	}
 	
     
     public void init(){
     	final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
     	device = env.getDefaultScreenDevice();
         final GraphicsConfiguration gc = device.getDefaultConfiguration();
         mainFrame = new JFrame(gc);
         mainFrame.setUndecorated(true);
         mainFrame.setIgnoreRepaint(true);
         //if(controller.getMouseListener() == null) throw new IllegalArgumentException("MouseListener n�o pode ser null");
         //mainFrame.addMouseListener(controller.getMouseListener());
         if(controller.getKeyListener() == null) throw new IllegalArgumentException("KeyListener n�o pode ser null");
         mainFrame.addKeyListener(controller.getKeyListener());
         device.setFullScreenWindow(mainFrame);
         if (device.isDisplayChangeSupported()) {
             chooseBestDisplayMode(device);
         }
 		mainFrame.createBufferStrategy(2);
         bufferStrategy = mainFrame.getBufferStrategy();
     }
     
     public void render(){
 		final Graphics2D g = this.getGraphics();
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		g.setColor(new Color(255,94,55));
 		g.fillRect(0, 0, width, height);
 		SceneGraph.getInstance().render(g);
 		g.dispose();
 		bufferStrategy.show();
     }
 
     public Graphics2D getGraphics(){
     	if(bufferStrategy.contentsLost()){
     		 bufferStrategy = mainFrame.getBufferStrategy();
     	}
     	return (Graphics2D)bufferStrategy.getDrawGraphics();
     }
     
     private static void chooseBestDisplayMode(final GraphicsDevice device) {
     	final DisplayMode best = getBestDisplayMode(device);
     	if (best != null) {
     		device.setDisplayMode(best);
     		width = best.getWidth();
     		height = best.getHeight();
     	}
     }
 
     private static DisplayMode getBestDisplayMode(final GraphicsDevice device) {
    	System.out.println("Available game resolutions:");
    	final DisplayMode[] modes = device.getDisplayModes();
    	
    	for (int i = 0; i < modes.length; i++) {
    		System.out.println("Available resolution: [w: "+modes[i].getWidth()+" h: "+modes[i].getHeight()+" b:"+modes[i].getBitDepth()+"]");
    	}
     	
         for (int x = 0; x < BEST_DISPLAY_MODES.length; x++) {
             for (int i = 0; i < modes.length; i++) {
                 final boolean widthMatches = modes[i].getWidth() == BEST_DISPLAY_MODES[x].getWidth();
 				final boolean heightMatches = modes[i].getHeight() == BEST_DISPLAY_MODES[x].getHeight();
 				final boolean bitDepthMatches = modes[i].getBitDepth() == BEST_DISPLAY_MODES[x].getBitDepth();
 				if (widthMatches && heightMatches && bitDepthMatches) {
                     return BEST_DISPLAY_MODES[x];
                 }
             }
         }
         throw new RuntimeException("Could not set fullscreen resolution.");
     }
     
     public void restoreScreen() {
         final Window window = device.getFullScreenWindow();
         if (window != null) {
             window.dispose();
         }
         device.setFullScreenWindow(null);
         System.exit(0);
     }
 
 
 	public void setController(final ControllerDevice controller) {
 		this.controller = controller;
 	}
 
 	public ControllerDevice getController() {
 		return controller;
 	}
 
 	public JFrame getMainFrame() {
 		return mainFrame;
 	}
 
 	public void setMainFrame(final JFrame mainFrame) {
 		this.mainFrame = mainFrame;
 	}
 
 	public static int getHeight() {
 		return height;
 	}
 
 	public static int getWidth() {
 		return width;
 	}
 }
