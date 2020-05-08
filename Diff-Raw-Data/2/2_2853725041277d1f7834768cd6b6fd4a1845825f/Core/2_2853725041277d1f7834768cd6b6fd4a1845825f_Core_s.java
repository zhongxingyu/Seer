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
 
 /**
  * The main game container that handles the main loop and is the entry point. 
  * @author Lasse Dissing Hansen
  * @since 0.1
  * 
  */
 public class Core implements Runnable {
 	
 	/* The game runs until this goes false */
 	private boolean running;
 	
 	/* System time at last frame*/
 	private long lastFrame;
 	
 	private final int targetFPS = 1000 / 60;
 
     private GameContainer container;
 
     private Render render;
 
     private Settings settings;
 	
 	/**
 	 * Creates a new game
 	 */
 	public Core() {
        settings = new Settings("engine.yml");
         container = new GameContainer();
         render = new BasicFrameRender(settings);
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
 			container.tick(settings);
 			updateFPS();
             if (settings.isUpdated())
                 render.updateSettings(settings);
             render.preRender();
             container.render(settings,render);
             render.postRender();
 			int delta = getDelta();
 			if (delta > targetFPS)
 				delta = targetFPS;
 			try {
 				Thread.sleep(targetFPS - delta);
 			} catch(InterruptedException e) {
 				Thread.currentThread().interrupt();
 			}
 		}
 	}
 	
 	/**
 	 * Initialises the framework before entering main loop
 	 */
 	private void initialize() {
         render.initScreen();
         container.initialize();
         render.attachInput(container.getInput());
         render.initRender();
 
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
 
     public GameContainer getContainer() {
         return container;
     }
 	
 }
