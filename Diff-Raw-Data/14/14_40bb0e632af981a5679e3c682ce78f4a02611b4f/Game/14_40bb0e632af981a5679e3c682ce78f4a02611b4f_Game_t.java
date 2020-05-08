 package org.jere.spaceinvaders;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferStrategy;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 @SuppressWarnings("serial")
 public class Game extends Canvas {
 
 	/** The game's X resolution **/
 	public static final int RES_X = 800;
 	/** The game's Y resolution **/
 	public static final int RES_Y = 600;
 	/** Used to know if debugging is enabled (defaults to false) **/
 	public static final boolean DEBUG_ENABLED = false;
 	
 	/** Buffer strategy used to render accelerated graphics **/
 	private BufferStrategy bufferStrategy;	
 	/** Ship entity representing the player **/
 	private Entity ship;
 	/** Indicates whether the game is running or not **/
 	private boolean gameRunning;
 	/** Indicates if left key was pressed **/
 	private boolean leftPressed;
 	/** Indicates if right key was pressed **/
 	private boolean rightPressed;
 	/** Indicates if fire key was pressed **/
 	private boolean firePressed;
 	/** True if we are waiting for a key to be pressed (when you lose or pause, for example) **/
 	private boolean waitingForKeyPress = true;
 	/** This will be set to TRUE when a game event requires the whole game world to be updated **/
 	private boolean logicUpdateRequired = false;
 	/** List containing all the present entities **/
 	private List<Entity> entities = new ArrayList<Entity>();
 	/** List containing all the present entities **/
 	private List<Entity> removeList = new ArrayList<Entity>();
 	/** Ship's default move speed (pixels/sec) **/
 	private int shipMoveSpeed = 300;
 	/** Amount of aliens currently on screen **/
 	private int alienCount;
 	/** Time when last shot was fired **/
 	private long lastFire;
 	/** Time allowed between shots (in ms) **/
 	private long firingInterval = 300;
 	/** Message to be shown while waiting for key press **/
 	private String message = "";
 	
 	
 	/**
 	 * Create new Game instance (sets up display and graphics and runs main game loop)
 	 * 
 	 */
 	public Game() {		
 		// create frame to contain game
 		JFrame container = new JFrame("Space Invaders - Ahrequesi");		
 		
 		// get the container panel of the frame to setup the game's resolution
 		JPanel panel = (JPanel) container.getContentPane();
 		panel.setPreferredSize(new Dimension(RES_X, RES_Y));
 		panel.setLayout(null);
 		
 		// setup the canvas size and put it in the content pane
 		this.setBounds(0, 0, RES_X, RES_Y);
 		panel.add(this);
 		
 		// since the canvas we're working with is going to be actively redrawn,
 		// we need to prevent AWT from attempting to redraw our surface (we will do it ourselves)
 		this.setIgnoreRepaint(true);
 		
 		// add key listener
 		this.addKeyListener(new KeyInputHandler());
 		
 		// default to exit program on close
 		container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 						
 		// and let there be light
 		container.pack();
 		container.setResizable(false);
 		container.setVisible(true);
 		
 		// create buffer strategy to use accelerated graphics method, and we are all set
 		this.createBufferStrategy(2);
 		this.bufferStrategy = this.getBufferStrategy();
 		
 		// set focus on game window
 		this.requestFocus();
 	}
 	
 	/**
 	 * Initialize entities (create aliens and ship)
 	 * 
 	 */
 	private void initEntities() {
 		// create player entity and place it in the center of the screen
 		this.ship = new ShipEntity(this, "sprites/ship.gif", 370, 550);
 		entities.add(ship);
 		
 		// create a block of aliens (5 rows of 12 aliens each, spaced evenly)
 		this.alienCount = 0;
 		for (int row = 0; row < 5; row++) {
 			for (int x = 0; x < 12; x++) {
 				Entity alien = new AlienEntity(this, "sprites/alien.gif", 100 + (x*50), (50) + row*30);
 				
 				entities.add(alien);
 				this.alienCount++;
 			}
 		}
 	}
 	
 	private void tryToFire () {
 		long currentFiringInterval = System.currentTimeMillis() - this.lastFire;
 		
 		// if we are trying to shoot again in less time than allowed, do nothing
 		if (currentFiringInterval < this.firingInterval) {
 			return;
 		}
 		
 		// if we're OK to fire, update last fire time to current time
 		this.lastFire = System.currentTimeMillis();
 		
 		// create shot entity and add it to entity list
 		ShotEntity shot = new ShotEntity(this, "sprites/shot.gif", this.ship.getXPosition() + 10, this.ship.getYPosition() - 30);
 		this.entities.add(shot);
 	}
 	
 	/**
 	 * Game world updates take place here (collision-detection, shots, keyboard input, etc.).
 	 * 
 	 */
 	private void gameLoop() {
 		// we'll store the time difference between each loop here.
 		long lastLoopTime = System.currentTimeMillis();
 		
 		while (this.gameRunning) {
 			// calculate how long it's been since the last run of the game loop
 			long delta = System.currentTimeMillis() - lastLoopTime;
 			lastLoopTime = System.currentTimeMillis();
 
 			// get the current graphics surface and blank it out (black it out, actually)
 			Graphics2D graphSurface = (Graphics2D) this.bufferStrategy.getDrawGraphics();
 			graphSurface.setColor(Color.BLACK);
 			graphSurface.fillRect(0, 0, RES_X, RES_Y);
 			
 			// cycle through the entities and make them move (if necessary)
 			if (!this.waitingForKeyPress) {
 				for (Entity e : this.entities) {
 					e.move(delta);
 				}				
 			}
 			
 			// after moving them all, we draw them.
 			for (Entity e : this.entities) {
 				e.draw(graphSurface);
 			}
 			
 			// brute force collision check: cycle through the entity list, if two entities collide
 			// notify both of them about the collision.
 			for (Entity me : this.entities) {				 
 				for (Entity him : this.entities) {
 					// we don't want to check if we collide with ourselves :p
 					if (me == him) {
 						continue;
 					}
 					// notify both entities if we find a collision
 					if (me.collidesWith(him)) {
 						me.collidedWith(him);
 						him.collidedWith(me);
 					}
 				}
 			}
 			
 			// remove entities mark for deletion and clear remove list
 			this.entities.removeAll(this.removeList);
 			this.removeList.clear();
 			
 			// resolve each entities game logic if a logic update was required
 			if (this.logicUpdateRequired) {
 				for (Entity entity : this.entities) {
 					entity.doLogic();
 				}
 				
 				// set it back to false, until some other entity needs updating
 				this.logicUpdateRequired = false;
 			}
 			
 			// if we're waiting for an "any key" press then draw the 
 			// current message
 			if (this.waitingForKeyPress) {
 				graphSurface.setColor(Color.white);
 				graphSurface.drawString(this.message,(800 - graphSurface.getFontMetrics().stringWidth(this.message)) / 2,250);
 				graphSurface.drawString("Press any key to continue",(800 - graphSurface.getFontMetrics().stringWidth("Press any key")) / 2,300);
 			}
 			
 			// this is where accelerated graphics kick in, clear the current surface and draw the new one on screen
 			// (we assume at this point that everything that had to be re-drawn, is already re-drawn)
 			graphSurface.dispose();
 			this.bufferStrategy.show();
 			
 			// resolve movement of the ship, we'll first assume it isn't moving
 			this.ship.setXSpeed(0);
 			
 			// if only right is pressed, move right
 			if (this.rightPressed && !this.leftPressed) {
 				this.ship.setXSpeed(this.shipMoveSpeed);
 			}
 			
 			// same for left
 			if (!this.rightPressed && this.leftPressed) {
 				this.ship.setXSpeed(-this.shipMoveSpeed);
 			}
 			
 			// if fire key is pressed, try to fire
 			if (this.firePressed) {
 				tryToFire();
 			}
 			
 			// pause for a bit (this value should keep us at about 100fps)
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				JOptionPane.showMessageDialog(null,
 						"The game's main thread was interrupted.",
 						"Sorry!",
 						JOptionPane.ERROR_MESSAGE);
 				
 				if (DEBUG_ENABLED) {
 					e.printStackTrace();
 				}
 				
 				System.exit(0);
 			}
 		}
 	}
 	
 	/**
 	 * Runs the main game loop.
 	 * 
 	 */	
 	public void startGame() {		
 		this.resetGame();
 		// the game is now running
 		this.gameRunning = true;
 		// so, we run the game loop
 		this.gameLoop();
 	}
 	
 	/**
 	 * Reset game (clear out existing entities and reset key presses, etc)
 	 * 
 	 */
 	public void resetGame() {
 		// clear out any existing entities and initialize a new set
 		entities.clear();
 		initEntities();
 		
 		// blank out any keyboard settings we might currently have
 		leftPressed = false;
 		rightPressed = false;
 		firePressed = false;
 	}
 	
 	/**
 	 * Marks an entity to be removed in the next game loop
 	 * 
 	 * @param entity The entity to be removed
 	 */
 	public void removeEntity(Entity entity) {
 		this.removeList.add(entity);		
 	}
 		
 	/**
 	 * Updates game logic when player is hit by an alien
 	 * 
 	 */
 	public void notifyPlayerDeath() {
 		this.message = "Sucker. You are DEAD!";
 		this.waitingForKeyPress = true;
 	}
 	
 	/**
 	 * Updates game logic when player wins the game
 	 * 
 	 */
 	public void notifyPlayerWin() {
 		this.message = "Lucky bastard. You managed to escape the gay republican aliens!";
 		this.waitingForKeyPress = true;
 	}
 	
 	/**
 	 * Updates game logic when an alien gets killed
 	 * 
 	 */
 	public void notifyAlienKilled() {
 		this.alienCount--;
 		
 		// if we killed all the aliens we've won the game.
 		if (this.alienCount == 0) {
 			this.notifyPlayerWin();
 			return;
 		}
 		
 		// speed up aliens if there are still any left
 		for (Entity entity : this.entities) {
 			if (entity instanceof AlienEntity) {
 				((AlienEntity) entity).speedUp();
 			}
 		}
 	}
 	
 	/**
 	 * Called by entities to notify the game world it needs logic updates
 	 * 
 	 */
 	public void updateLogic() {
 		this.logicUpdateRequired = true;
 	}
 	
 	/**
 	 * Anonymous inner class to handle keyboard input
 	 * 
 	 * @author jere
 	 *
 	 */
	private class KeyInputHandler extends KeyAdapter {		
 		/**
 		 * Handles the event of a key being pressed down
 		 * 
 		 * @param e Class containing the event's information
 		 */
 		public void keyPressed(KeyEvent e) {
 			if (waitingForKeyPress) {
 				return;
 			}
 			
 			switch(e.getKeyCode()) {
 				case KeyEvent.VK_LEFT: {
 					leftPressed = true;
 					break;
 				}
 				
 				case KeyEvent.VK_RIGHT: {
 					rightPressed = true;
 					break;
 				}
 				
 				case KeyEvent.VK_SPACE: {
 					firePressed = true;
 					break;
 				}			
 			}
 		}
 		
 		/**
 		 * Handles the event of a key being released
 		 * 
 		 * @param e Class containing the event's information
 		 */
 		public void keyReleased(KeyEvent e) {
 			if (waitingForKeyPress) {
 				return;
 			}
 			
 			switch(e.getKeyCode()) {
 				case KeyEvent.VK_LEFT: {
 					leftPressed = false;
 					break;
 				}
 				
 				case KeyEvent.VK_RIGHT: {
 					rightPressed = false;
 					break;
 				}
 				
 				case KeyEvent.VK_SPACE: {
 					firePressed = false;
 					break;
 				}			
 			}			
 		}
 		
 		/**
 		 * Handles the event of a key being typed (pressed and released)
 		 * 
 		 * @param e Class containing the event's information
 		 */
 		public void keyTyped(KeyEvent e) {
 			// if we're waiting for a "any key to be pressed" then check if we've received any recently. We may
 			// have had a keyType() event from the user releasing the shoot or move keys, hence 
 			// the use of the "pressCount" counter.
 			if (waitingForKeyPress) {
				waitingForKeyPress = false;
				resetGame();
 			}
 					
 			// we'll quit the game when escape is typed
 			if (e.getKeyChar() == 27) {
 				System.exit(0);
 			}			
 		}
 	}
 }
