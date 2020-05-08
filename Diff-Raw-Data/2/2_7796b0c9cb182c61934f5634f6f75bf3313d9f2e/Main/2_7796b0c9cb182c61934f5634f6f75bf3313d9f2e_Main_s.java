 package net.codegames.towerninja;
 
 import processing.core.PApplet;
 
 public class Main extends PApplet {
 
 	/**
 	 * This is the main class. It should just handle the basic setup, creating
 	 * the game and updating it regulary. Apart from that, the {@link Game}
 	 * Object should take care of things.
 	 */
 	private static final long serialVersionUID = 1L;
 	private Game game;
 
 	/**
 	 * Initial setup of the Applet. Also creating the {@link Game} object
 	 * 
 	 * @see processing.core.PApplet#setup()
 	 */
 	public void setup() {
 		size(600, 800);
 		frameRate(30);
 		game = new Game();
 	}
 
 	/**
 	 * Called [frameRate] number of times per second. Updating the gamestate.
 	 * 
 	 * @see processing.core.PApplet#draw()
 	 */
 	public void draw() {
 		background(128);
 		game.update();
 	}
 
 	public static void main(String[] args) {
		PApplet.main(new String[] { "Main" });
 	}
 
 }
