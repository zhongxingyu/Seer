 /*Copyright (C) 2012 Lasse Dissing Hansen
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of 
 this software and associated documentation files (the "Software"), to deal in the Software without restriction, 
 including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package volpes.ldk;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.image.BufferStrategy;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The main game container that handles the main loop and is the entry point. 
  * @author Lasse Dissing Hansen
  * @version 0.1
  * 
  */
 public class GameContainer extends Canvas implements Runnable {
 	
 	private static final long serialVersionUID = -8397755606536871949L;
 
 	/* The game runs until this goes false */
 	private boolean running;
 	
 	/* The width of the game in pixles*/
 	public final int width;
 	/*The height of the game in pixels*/
 	public final int height;
 	
 	/* System time at last frame*/
 	private long lastFrame;
 	
 	/*The list that contains all game parts*/
 	private List<GameState> states = new ArrayList<GameState>();
 	/* The current part. This is the state or gamemode that runs currently*/
 	private GameState currentState;
 	
 	private Input input;
 	
 	private final int targetFPS = 1000 / 60;
 	
 	/**
 	 * Creates a new game
 	 * @param width The width of the game in pixels
 	 * @param height The height of the game in pixels
 	 */
 	public GameContainer(int width, int height) {
 		this.width = width;
 		this.height = height;
 		input = new Input();
 		this.addMouseListener(input);
 		this.addMouseMotionListener(input);
 		this.addKeyListener(input);
 	}
 	
 	/**
 	 * Starts the game in a new Thread
 	 */
 	public void start() {
 		running = true;
 		new Thread(this).start();
 	}
 	
 	/**
 	 * Stops the main loop after current tick
 	 */
 	public void stop() {
 		running = false;
 	}
 	
 	/**
 	 * The main loop
 	 */
 	@Override
 	public void run() {
 		initialize();
 		while (running) {
 			getDelta();
 			currentState.update(this);
 			drawToScreen();
 			updateFPS();
 			int delta = getDelta();
 			if (delta > targetFPS)
 				delta = targetFPS;
 			try {
 				Thread.sleep(targetFPS - delta);
 			} catch(InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Initialises the framework before entering main loop
 	 */
 	private void initialize() {
 		if (states.size() > 0) {
 			try {
 				setState(0);
 			} catch (LDKException e) {
 				e.printStackTrace();
 			}
 		} else {
 			new LDKException("You must add states to the game, before start!").printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	/**
 	 * Changes the current game state
 	 * @param i The index of the new state
 	 * @throws LDKException On unknown index
 	 */
 	public void setState(int i) throws LDKException{
 		try {
 			currentState = states.get(i);
 			if (!currentState.isInitialized()) {
 				currentState.expandedInitalize();
 			}
 		} catch(ArrayIndexOutOfBoundsException e) {
 			throw new LDKException("Unknow state requested!");
 		}
 		
 	}
 	
 	/**
 	 * @return Index of current gamemode
 	 */
 	public int getCurrentState() {
 		return states.indexOf(currentState);
 	}
 	
	/**
	 * Adds a state to the end of the list
	 * @param state
	 */
 	public void addState(GameState state) {
 		states.add(state);
 	}
	
	/**
	 * Replaces a state with another
	 * @param state
	 * @param i
	 */
	public void setState(GameState state, int i) {
		states.set(i, state);
	}
 
 	/**
 	 * @return Current System time in milliseconds
 	 */
 	private long getTime() {
 		return System.currentTimeMillis();
 	}
 	
 	/**
 	 * @return Time since last call
 	 */
 	private int getDelta() {
 		long time = getTime();
 		int delta = (int) (time - lastFrame);
 		lastFrame = time;
 		
 		return delta;
 	}
 	
 	long lastFPS = 0;
 	int fps = 0;
 	int currentFPS = 0;
 	
 	private void updateFPS() {
 		if (getTime() - lastFPS > 1000) {
 			lastFPS = getTime();
 			currentFPS = fps;
 			fps = 0;
 		}
 		fps++;
 	}
 	
 	/**
 	 * @return Current Input wrapper
 	 */
 	public Input getInput() {
 		return input;
 	}
 	
 	/**
 	 * Renders the current gamestate and draws to screen
 	 */
 	private void drawToScreen() {
 		BufferStrategy bs = getBufferStrategy();
 		if (bs == null) {
 			createBufferStrategy(2);
 			return;
 		}
 		Graphics2D g = (Graphics2D)bs.getDrawGraphics();
 		g.setColor(Color.BLACK);
 		g.fillRect(0, 0, width, height);
 		currentState.render(this, g);
 		g.setColor(Color.WHITE);
 		g.drawString("FPS: " + currentFPS, 20, 20);
 		g.dispose();
 		bs.show();
 		Toolkit.getDefaultToolkit().sync();
 	}
 }
