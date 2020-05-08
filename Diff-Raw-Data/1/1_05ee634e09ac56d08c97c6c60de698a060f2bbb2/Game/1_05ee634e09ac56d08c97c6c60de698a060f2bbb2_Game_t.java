 package org.tlein.pong;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.geom.Rectangle;
 import org.tlein.pong.entities.Ball;
 import org.tlein.pong.entities.Entity;
 import org.tlein.pong.entities.Paddle;
 
 /**
  * Game starts the Pong game and controls all the elements of the Game
  * 
  * @author Tucker Lein
  */
 public class Game extends BasicGame {
 	/* ArrayList containing all the entities such as paddles and the ball */
 	private static ArrayList<Entity> entities = new ArrayList<Entity>();
 	
 	/* width of the screen */
 	private static int width;
 	
 	/* height of the screen */
 	private static int height;
 	
 	/* font used for score board */
 	private UnicodeFont font; 
 	
 	/* the two player's points, 0 is left player's, 1 is right player's */
 	private static int[] points = new int[2];
 	
 	/* if the game has been won, set to true else it is set to false */
 	private boolean won = false;
 	
 	/* the score needed to win the game */
 	private int scoreLimit = 5;
 
 	/**
 	 * Constructs new Game object, which calls the parent's BasicGame
 	 * constructor to setup the window with the given title
 	 * 
 	 * @param title title of the window
 	 */
 	public Game(String title) {
 		super(title);
 	}
 
 	/**
 	 * initializes the Game's elements and calls init on the entities
 	 * 
 	 * @param gc GameContainer context
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void init(GameContainer gc) throws SlickException {
 		Game.width = gc.getWidth();
 		Game.height = gc.getHeight();
 		font = new UnicodeFont("res/fonts/dot.ttf", 50, false, false);
 		font.addAsciiGlyphs();
 		font.addGlyphs(400, 600);
 		font.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
 		font.loadGlyphs();
 		
 		entities.add(new Paddle(new Rectangle(10, 200, 15, 75)));
 		entities.add(new Paddle(new Rectangle(width - 20, 200, 15, 75)));
 		entities.add(new Ball(new Rectangle(50, 50, 10, 10)));
 		for(Entity o : entities) {
 			o.init(gc);
 		}
 	}
 	
 	/**
 	 * Updates the Entities and Game information
 	 * 
 	 * @param gc GameContainer context
 	 * @param delta time since last update
 	 */
 	@Override
 	public void update(GameContainer gc, int delta) throws SlickException {
 		if(!won) {
 			for(Entity o : entities) {
 				o.update(gc, delta);
 			}
 			if(points[0] == scoreLimit || points[1] == scoreLimit) {
 				won = true;
 			}
 		} else {
 			if(gc.getInput().isKeyPressed(Input.KEY_R)) {
 				resetGame();
 			}
 		}
 	}
 
 	/**
 	 * Renders the board, points, and entities
 	 * 
 	 * @param gc GameContainer context
 	 * @param g Graphics context
 	 */
 	@Override
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		g.setColor(Color.black);
 		g.fillRect(0, 0, gc.getWidth(), gc.getHeight());
 		g.setColor(Color.white);
 		for(int i = 0; i < 30; i++) {
 			g.fillRect(width/2 - 5, i * 20, 10, 10);
 		}
 		g.setFont(font);
 		g.drawString(""+points[0], gc.getWidth()/2 - 64, 10);
 		g.drawString(""+points[1], gc.getWidth()/2 + 40, 10);
 		for(Entity o : entities) {
 			o.render(gc, g);
 		}
 		
 		if(won) {
 			if(points[0] == scoreLimit) {
 				g.drawString("Left wins!", 50, 10);
 			} else if(points[1] == scoreLimit) {
 				g.drawString("Right wins!", 500, 10);
 			}
 			g.drawString("Press R to replay", 200, 550);
 		}
 	}
 
 	/**
 	 * Resets the values of the game
 	 */
 	private void resetGame() {
 		won = false;
 		points[0] = 0;
 		points[1] = 0;
 	}
 	
 	/**
 	 * @return width of window
 	 */
 	public static int getWidth() {
 		return width;
 	}
 	
 	/**
 	 * @return height of window
 	 */
 	public static int getHeight() {
 		return height;
 	}
 	
 	/**
 	 * @return ArrayList of all the game's Entities
 	 */
 	public static ArrayList<Entity> getEntities() {
 		return entities;
 	}
 	
 	/**
 	 * @return array containing the paddles points
 	 */
 	public static int[] getPoints() {
 		return points;
 	}
 
 	public static void main(String[] args) throws SlickException {
 		Game g = new Game("Pong"); //create instance of Game, which extends BasicGame
 		AppGameContainer app = new AppGameContainer(g); //AppGameContainer takes in a BasicGame and runs the system
 		app.setDisplayMode(800, 600, false); //sets the display properties of the window
 		app.setTargetFrameRate(60); //sets the target framerate the game will run at
 		app.setShowFPS(false); //don't print the fps to the screen
 		app.start(); //start the game loop
 	}
 
 }
