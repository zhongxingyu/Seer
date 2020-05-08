 /** 
  * This is a two-dimensional software rendering library to eliminate boilerplate code. The library is geared towards game development 
  * and provides the easy creation of a rendering context.
  * 
  * Libraries used: EasyOGG by Kevin Glass and JOrbis by JCraft
  *  
  * @author Logan Weber
  * @version 1.1
  */
 
 package com.doobs.java2d;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.image.BufferStrategy;
 
 import javax.swing.JFrame;
 
 import com.doobs.java2d.gfx.BitmapLoader;
 import com.doobs.java2d.gfx.Screen;
 import com.doobs.java2d.input.InputHandler;
 import com.doobs.java2d.input.WindowHandler;
 
 public class Game2D extends Canvas implements Runnable {
 	private static final long serialVersionUID = 1L;
 	
 	private static final int DEFAULT_WIDTH = 640;
 	private static final int DEFAULT_HEIGHT = 480;
 	private static final int DEFAULT_SCALE = 1;
	private static final String DEFAULT_TITLE = "Default Java2D Window";
 
 	private int width, height;
 	private int scale;
 	private String title;
 	
 	private boolean paused;
 	private int pauseCounter;
 	
 	private Font defaultFont;
 	
 	private JFrame frame;
 	private Screen screen;
 	
 	private BitmapLoader bitmapLoader;
 	
 	private Thread thread;
 	private boolean running;
 	
 	private Graphics graphics;
 	private BufferStrategy bufferStrategy;
 	
 	private InputHandler input;
 	private int inputStopCounter;
 	private boolean inputStopped;
 	
 	private WindowHandler windowHandler;
 	
 	private int fps;
 	private boolean vSync;
 	private boolean printFPS, renderFPS;
 	
 	private GameLoop gameLoop;
 	
 	/**
 	 * Creates a Game2D object and a window with the specified width, height, scaling factor, title, and game loop.
 	 * @param width the width of the window.
 	 * @param height the height of the window.
 	 * @param scale the scaling factor for the pixels.
 	 * @param title the title of the window.
 	 * @param gameLoop the GameLoop object with custom tick, render, and printFPS methods.
 	 */
 	public Game2D(int width, int height, int scale, String title, GameLoop gameLoop) {
 		this.width = width;
 		this.height = height;
 		this.scale = scale;
 		this.title = title;
 		this.gameLoop = gameLoop;
 		this.paused = false;
 		this.pauseCounter = 0;
 		this.running = false;
 		this.defaultFont = new Font("Consolas", 0, 16);
 		this.inputStopCounter = 0;
 		this.inputStopped = false;
 		Dimension canvasSize = new Dimension(width * scale, height * scale);
 		super.setSize(canvasSize);
 		super.setPreferredSize(canvasSize);
 		super.setMinimumSize(canvasSize);
 		super.setMaximumSize(canvasSize);
 		screen = new Screen(width, height);
 		frame = new JFrame(title);
 		frame.setFocusable(true);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable(false);
 		frame.setLayout(new BorderLayout());
 		frame.add(this, BorderLayout.CENTER);
 		frame.pack();
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 		input = new InputHandler(this);
 		windowHandler = new WindowHandler(this);
 		bitmapLoader = new BitmapLoader();
 	}
 	
 	/**
 	 * Creates a Game2D object and a window with the specified width, height, scaling factor, and title.
 	 * @param width the width of the window.
 	 * @param height the height of the window.
 	 * @param scale the scaling factor for the pixels.
 	 * @param title the title of the window.
 	 */
 	public Game2D(int width, int height, int scale, String title) {
 		this(width, height, scale, title, new GameLoop());
 	}
 	
 	/**
 	 * Creates a Game2D object and a window with the specified width, height, and scaling factor.
 	 * @param width the width of the window.
 	 * @param height the height of the window.
 	 * @param scale the scaling factor for the pixels.
 	 */
 	public Game2D(int width, int height, int scale) {
 		this(width, height, scale, DEFAULT_TITLE);
 	}
 	
 	/**
 	 * Creates a Game2D object and a window with the specified width and height.
 	 * @param width the width of the window.
 	 * @param height the height of the window.
 	 */
 	public Game2D(int width, int height) {
 		this(width, height, DEFAULT_SCALE);
 	}
 	
 	/**
 	 * Creates a Game2D object with default settings.
 	 */
 	public Game2D() {
 		this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SCALE, DEFAULT_TITLE, new GameLoop());
 	}
 	
 	/**
 	 * Starts the main working thread of the Game2D object and begins to run the game.
 	 */
 	public void start() {
 		if (!running) {
 			thread = new Thread(this);
 			running = true;
 			thread.start();
 		}
 	}
 	
 	/**
 	 * Begins the main game loop.
 	 */
 	public void run() {
 		preRender();
 		
 		long previousTime = System.nanoTime();
 		double secondsPerTick = 1 / 60.0;
 		double unprocessedSeconds = 0;
 		boolean ticked = false;
 		int fpsTemp = 0;
 		long tickCount = 0;
 		requestFocus();
 		
 		while (running) {
 			long currentTime = System.nanoTime();
 			unprocessedSeconds += (currentTime - previousTime) / 1000000000.0;
 			previousTime = currentTime;
 			
 			while (unprocessedSeconds >= secondsPerTick) {
 				unprocessedSeconds -= secondsPerTick;
 				if(inputStopped && --inputStopCounter <= 0) {
 					inputStopped = false;
 					inputStopCounter = 0;
 				}
 				gameLoop.tick(input, paused);
 				input.tick();
 				if(--pauseCounter <= 0)
 					paused = false;
 				ticked = true;
 				tickCount++;
 				if (tickCount % 60 == 0) {
 					fps = fpsTemp;
 					fpsTemp = 0;
 					/*if(printFPS) {
 						gameLoop.printFPS();
 					}*/
 				}
 			}
 
 			if (ticked) {
 				render();
 				if (vSync)
 					ticked = false;
 				fpsTemp++;
 			}
 		}
 	}
 	
 	/**
 	 * Stops the game and exits the application.
 	 */
 	public void stop() {
 		if (running) {
 			running = false;
 			try {
 				thread.join();
 			} catch(InterruptedException e) {
 				e.printStackTrace();
 			} finally {
 				System.exit(0);
 			}
 		}
 	}
 	
 	/**
 	 * Pauses the game for the specified number of ticks.
 	 * @param ticks the number of ticks for the game to wait.
 	 */
 	public void pauseFor(int ticks) {
 		paused = true;
 		pauseCounter = ticks;
 	}
 	
 	/**
 	 * Pauses the game until the unpause() method is called.
 	 */
 	public void pause() {
 		paused = true;
 		pauseCounter = -1;
 	}
 	
 	/**
 	 * Unpauses the game.
 	 */
 	public void unpause() {
 		paused = false;
 		pauseCounter = 0;
 	}
 	
 	/**
 	 * Initializes double buffering and gets a buffer strategy.
 	 */
 	public void preRender() {
 		createBufferStrategy(2);
 		bufferStrategy = getBufferStrategy();
 	}
 	
 	/**
 	 * Renders the game into an array and displays it on the screen.
 	 */
 	public void render() {
 		graphics = bufferStrategy.getDrawGraphics();
 		gameLoop.render(screen, paused);
 		graphics.drawImage(screen.image, 0, 0, super.getWidth(), super.getHeight(), null);
 		if(renderFPS) {
 			int fontSize = defaultFont.getSize();
 			graphics.setColor(Color.BLACK);
 			graphics.fillRect(0, 0, (fps + "").length() * fontSize / 2 + 6, fontSize);
 			graphics.setColor(Color.WHITE);
 			graphics.setFont(defaultFont);
 			graphics.drawString(fps + " ", 1, fontSize - 4);
 		}
 		graphics.dispose();
 		bufferStrategy.show();
 	}
 	
 	/**
 	 * Prevents player input for the specified number of ticks.
 	 * @param ticks the number of ticks to prevent input
 	 */
 	public void stopInput(int ticks) {
 		inputStopped = true;
 		inputStopCounter = ticks;
 		for(int i = 0; i < input.keys.length; i++) {
 			input.keys[i] = false;
 		}
 	}
 	
 	// Getters and Setters
 	/**
 	 * @return the width of the non-scaled game data.
 	 */
 	public int getScreenWidth() {
 		return screen.getWidth();
 	}
 	
 	/**
 	 * Sets the width variable and resizes the game window.
 	 * @param width
 	 */
 	public void setScreenWidth(int width) {
 		this.width = width;
 		screen = new Screen(width, height);
 		frame.pack();
 	}
 	
 	/**
 	 * @return the height of the non-scaled game data.
 	 */
 	public int getScreenHeight() {
 		return screen.getHeight();
 	}
 	
 	/**
 	 * Sets the height variable and resizes the game window.
 	 * @param height
 	 */
 	public void setScreenHeight(int height) {
 		this.height = height;
 		screen = new Screen(width, height);
 		frame.pack();
 	}
 	
 	/**
 	 * Sets both the width and height variable and resizes the game window.
 	 */
 	public void setScreenSize(int width, int height) {
 		this.width = width;
 		this.height = height;
 		Dimension canvasSize = new Dimension(width * scale, height * scale);
 		super.setSize(canvasSize);
 		super.setPreferredSize(canvasSize);
 		super.setMinimumSize(canvasSize);
 		super.setMaximumSize(canvasSize);
 		screen = new Screen(width, height);
 		//frame.pack();
 		frame.setSize(width * scale, height * scale);
 	}
 	
 	/**
 	 * @return the current scaling factor being applied to the image.
 	 */
 	public int getScale() {
 		return scale;
 	}
 	
 	/**
 	 * @param scale the new scale to be applied to the image.
 	 */
 	public void setScale(int scale) {
 		this.scale = scale;
 		if(scale > 0) {
 			Dimension canvasSize = new Dimension(this.width * scale, this.height * scale);
 			setSize(canvasSize);
 			setPreferredSize(canvasSize);
 			setMinimumSize(canvasSize);
 			setMaximumSize(canvasSize);
 			frame.pack();
 		}
 	}
 	
 	/**
 	 * @return the current title of the window.
 	 */
 	public String getTitle() {
 		return title;
 	}
 	
 	/**
 	 * @param title the new title to be applied to the window.
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	
 	/**
 	 * @return whether the game is paused.
 	 */
 	public boolean getPaused() {
 		return paused;
 	}
 	
 	/**
 	 * @return the JFrame object 
 	 */
 	public JFrame getFrame() {
 		return frame;
 	}
 	
 	/**
 	 * @return the BitmapLoader object to load images into the game.
 	 */
 	public BitmapLoader getBitmapLoader() {
 		return bitmapLoader;
 	}
 	
 	/**
 	 * @return the GameLoop object currently being used by the game.
 	 */
 	public GameLoop getGameLoop() {
 		return gameLoop;
 	}
 	
 	/**
 	 * @param gameLoop the GameLoop object to replace the current one.
 	 */
 	public void setGameLoop(GameLoop gameLoop) {
 		this.gameLoop = gameLoop;
 	}
 	
 	/**
 	 * @return whether the game thread is currently running.
 	 */
 	public boolean isRunning() {
 		return running;
 	}
 	
 	/**
 	 * @return a graphics object to draw to the window.
 	 */
 	public Graphics getGraphics() {
 		return graphics;
 	}
 	
 	/**
 	 * @return the InputHandler object
 	 */
 	public InputHandler getInputHandler() {
 		return input;
 	}
 	
 	/**
 	 * @return the WindowHandler object
 	 */
 	public WindowHandler getWindowHandler() {
 		return windowHandler;
 	}
 	
 	/**
 	 * @return whether player input has been stopped.
 	 */
 	public boolean getInputStopped() {
 		return inputStopped;
 	}
 	
 	/**
 	 * @return the current number of frames rendered per second.
 	 */
 	public int getFPS() {
 		return fps;
 	}
 	
 	/**
 	 * @return whether the game is limited to 60 frames per second.
 	 */
 	public boolean isVSync() {
 		return vSync;
 	}
 	
 	/**
 	 * @param vSync set whether the game will be limited to 60 frames per second.
 	 */
 	public void setVSync(boolean vSync) {
 		this.vSync = vSync;
 	}
 	
 	/**
 	 * @return whether the game loop prints the number of frames rendered per second.
 	 */
 	public boolean getPrintFPS() {
 		return printFPS;
 	}
 	
 	/**
 	 * @param printFPS set whether the game loop will print the number of frames rendered 
 	 * per second based on the current game loop object's printFPS method.
 	 */
 	public void setPrintFPS(boolean printFPS) {
 		this.printFPS = printFPS;
 	}
 	
 	/**
 	 * @return whether the game loop renders the number of frames rendered per second.
 	 */
 	public boolean getRenderFPS() {
 		return printFPS;
 	}
 	
 	/**
 	 * @param renderFPS set whether the game loop will render the number of frames rendered per second.
 	 */
 	public void setRenderFPS(boolean renderFPS) {
 		this.renderFPS = renderFPS;
 	}
 }
