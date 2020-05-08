 package com.doobs.java2d;
 
 import com.doobs.java2d.gfx.Screen;
 import com.doobs.java2d.input.InputHandler;
 
 public class GameLoop {
 	protected Game2D game;
 
 	public GameLoop() {
 		this(null);
 	}
 	public GameLoop(Game2D game) {
 		this.game = game;
 	}
 	
 	/**
 	 * A structural tick method to be overwritten by classes that inherit from this class.
 	 * @param input the input object to be used.
 	 */
 	public void tick(InputHandler input) {
 		
 	}
 	
 	/**
 	 * A structural render method to be overwritten by classes that inherit from this class.
 	 * @param screen the screen object to be used for rendering.
 	 */
 	public void render(Screen screen) {
 		
 	}
 	
 	/**
 	 * @deprecated
 	 * A structural method that is not necessary to implement unless the FPS is needed to be displayed.
 	 */
 	public void printFPS() {
 		//System.out.println("FPS: " + game.getFPS());
 	}
 	
 	// Getters and setters
 	public void setGame(Game2D game) {
 		this.game = game;
 	}
 }
