 package ca.couchware.wezzle2d;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import ca.couchware.wezzle2d.java2d.Java2DGameWindow;
 import ca.couchware.wezzle2d.java2d.Java2DText;
 
 /**
  * The main hook of our game. This class with both act as a manager for the
  * display and central mediator for the game logic.
  * 
  * Display management will consist of a loop that cycles round all entities in
  * the game asking them to move and then drawing them in the appropriate place.
  * With the help of an inner class it will also allow the player to control the
  * main ship.
  * 
  * As a mediator it will be informed when entities within our game detect events
  * (e.g. alien killed, played died) and will take appropriate game actions.
  * 
  * @author Kevin Glass
  */
 public class Game extends Canvas implements GameWindowCallback
 {
 	/**
 	 * The manager in charge of maintaining the board.
 	 */
 	private BoardManager boardMan;
 	
 	/**
 	 * The manager in charge of moving the piece around with the
 	 * pointer and drawing the piece to the board.
 	 */
 	private PieceManager pieceMan;
 	
 	/** 
 	 * The manager in charge of keeping track of the time. 
 	 */
 	private TimeManager timeMan;
 	
 	/** The list of all the entities that exist in our game. */
 	private ArrayList entities = new ArrayList();
 	
 	/** The list of entities that need to be removed from the game this loop. */
 	private ArrayList removeList = new ArrayList();
 	
 	/** The entity representing the player. */
 	private Entity ship;
 	
 	/** The speed at which the player's ship should move (pixels/sec). */
 	private double moveSpeed = 300;
 	
 	/** The time at which last fired a shot */
 	private long lastFire = 0;
 	
 	/** The interval between our players shot (ms). */
 	private long firingInterval = 200;
 	
 	/** The number of aliens left on the screen. */
 	private int alienCount;
 
 	/** The message to display which waiting for a key press. */
 	private Sprite message;
 	
 	private boolean activateRefactorThisLoop = false;
 	private boolean refactorDownInProgress = false;
 	private boolean refactorLeftInProgress = false;
 	
 	/** True if we're holding up game play until a key has been pressed. */
 	private boolean waitingForKeyPress = true;
 	
 	/**
 	 * True if game logic needs to be applied this loop, normally as a result of
 	 * a game event.
 	 */
 	private boolean logicRequiredThisLoop = false;
 
 	/**
 	 * The time at which the last rendering looped started from the point of
 	 * view of the game logic.
 	 */
 	private long lastLoopTime = SystemTimer.getTime();
 	
 	/** The window that is being used to render the game. */
 	private GameWindow window;
 	
 	/** True if the fire key has been released. */
 	private boolean fireHasBeenReleased = false;
 
 	/** The sprite containing the "Press Any Key" message. */
 	private Sprite pressAnyKey;
 	
 	/** The sprite containing the "You win!" message. */
 	private Sprite youWin;
 	
 	/** The sprite containing the "You lose!" message. */
 	private Sprite youLose;
 
 	/** The time since the last record of FPS. */
 	private long lastFpsTime = 0;
 	
 	/** The recorded FPS. */
 	private int fps;
 
 	/** The normal title of the window. */
 	private String windowTitle = "Wezzle";
 	
 	/** FOR TESTING PURPOSES ONLY: REMOVE. */
 	Text timerText;
 
 	/**
 	 * Construct our game and set it running.
 	 * 
 	 * @param renderingType
 	 *            The type of rendering to use (should be one of the contansts
 	 *            from ResourceFactory)
 	 */
 	public Game(int renderingType) 
 	{
 		// Create a window based on a chosen rendering method.
 		ResourceFactory.get().setRenderingType(renderingType);		
 
 		final Runnable r = new Runnable()
 		{
 			public void run()
 			{
 				window = ResourceFactory.get().getGameWindow();
 				window.setResolution(800, 600);
 				window.setGameWindowCallback(Game.this);
 				window.setTitle(windowTitle);
 			}		
 		};
 		
 		try
 		{
 			javax.swing.SwingUtilities.invokeAndWait(r);
 		}
 		catch (InterruptedException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (InvocationTargetException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 	}
 
 	public void startRendering()
 	{
 		window.startRendering();
 	}
 
 	/**
 	 * Initialize the common elements for the game
 	 */
 	public void initialize()
 	{
 //		youLose = ResourceFactory.get().getSprite("sprites/gotyou.gif");
 //		pressAnyKey = ResourceFactory.get().getSprite("sprites/pressanykey.gif");
 //		youWin = ResourceFactory.get().getSprite("sprites/youwin.gif");
 
 		message = pressAnyKey;
 		
 		// Create the board manager.
 		boardMan = new BoardManager(272, 139, 8, 10);
 //		boardMan.createTile(0, TileEntity.COLOR_RED);
 //		boardMan.createTile(16, TileEntity.COLOR_PURPLE);
 //		boardMan.createTile(33, TileEntity.COLOR_BLUE);
 //		boardMan.createTile(25, TileEntity.COLOR_YELLOW);
 //		boardMan.createTile(20, TileEntity.COLOR_GREEN);
 //		boardMan.createTile(79, TileEntity.COLOR_RED);
 		boardMan.generateBoard(40);
 		
 		// Create the piece manager.
 		pieceMan = new PieceManager(boardMan);
 		window.addMouseListener(pieceMan);
 		window.addMouseMotionListener(pieceMan);	
 	
 		// Set up the timer text.
 		timerText = ResourceFactory.get().getText();
 		timerText.setSize(50);
 		timerText.setAnchor(Text.VCENTER | Text.HCENTER);
 		timerText.setColor(new Color(252, 233, 45 ));
 		
 		// Create the time manager.
 		timeMan = new TimeManager();
 
 		// Setup the initial game state.
 		startGame();
 	}
 
 	/**
 	 * Start a fresh game, this should clear out any old data and create a new
 	 * set.
 	 */
 	private void startGame()
 	{		
 		// Clear out any existing entities and initialize a new set.
 		entities.clear();
 		createEntities();
 		
 		this.activateRefactorThisLoop = true;
 	}
 
 	/**
 	 * Initialize the starting state of the entities (ship and aliens). Each
 	 * entity will be added to the overall list of entities in the game.
 	 */
 	private void createEntities()
 	{
 //		TileEntity t = new TileEntity(TileEntity.COLOR_RED, 0, 0);
 //		entities.add(t);
 		
 		// Create the player ship and place it roughly in the center of the
 		// screen.
 //		ship = new ShipEntity(this, "sprites/ship.gif", 370, 550);
 //		entities.add(ship);
 //
 //		// Create a block of aliens (5 rows, by 12 aliens, spaced evenly).
 //		alienCount = 0;
 //		for (int row = 0; row < 5; row++)
 //		{
 //			for (int x = 0; x < 12; x++)
 //			{
 //				Entity alien = new AlienEntity(this, 100 + (x * 50), (50) + row * 30);
 //				entities.add(alien);
 //				alienCount++;
 //			}
 //		}
 		
 	}
 
 	/**
 	 * Notification from a game entity that the logic of the game should be run
 	 * at the next opportunity (normally as a result of some game event)
 	 */
 	public void updateLogic()
 	{
 		logicRequiredThisLoop = true;
 	}
 
 	/**
 	 * Remove an entity from the game. The entity removed will no longer move or
 	 * be drawn.
 	 * 
 	 * @param entity
 	 *            The entity that should be removed
 	 */
 	public void removeEntity(Entity entity)
 	{
 		removeList.add(entity);
 	}
 
 	/**
 	 * Notification that the player has died.
 	 */
 	public void notifyDeath()
 	{
 		message = youLose;
 		waitingForKeyPress = true;
 	}
 
 	/**
 	 * Notification that the player has won since all the aliens are dead.
 	 */
 	public void notifyWin()
 	{
 		message = youWin;
 		waitingForKeyPress = true;
 	}
 
 	/**
 	 * Notification that an alien has been killed
 	 */
 	public void notifyAlienKilled()
 	{
 		// reduce the alien count, if there are none left, the player has won!
 		alienCount--;
 
 		if (alienCount == 0)
 		{
 			notifyWin();
 		}
 
 		// if there are still some aliens left then they all need to get faster,
 		// so
 		// speed up all the existing aliens
 		for (int i = 0; i < entities.size(); i++)
 		{
 			Entity entity = (Entity) entities.get(i);
 
 			if (entity instanceof AlienEntity)
 			{
 				// Speed up by 2%.
 				entity.setXMovement(entity.getXMovement() * 1.02);
 			}
 		}
 	}
 
 	/**
 	 * Attempt to fire a shot from the player. Its called "try" since we must
 	 * first check that the player can fire at this point, i.e. has he/she
 	 * waited long enough between shots
 	 */
 	public void tryToFire()
 	{
 //		// check that we have waiting long enough to fire
 //		if (System.currentTimeMillis() - lastFire < firingInterval)
 //		{
 //			return;
 //		}
 //
 //		// if we waited long enough, create the shot entity, and record the
 //		// time.
 //		lastFire = System.currentTimeMillis();
 //		ShotEntity shot = new ShotEntity(this, "sprites/shot.gif", ship.getX() + 10, ship.getY() - 30);
 //		entities.add(shot);
 	}
 
 	/**
 	 * Notification that a frame is being rendered. Responsible for running game
 	 * logic and rendering the scene.
 	 */
 	public void frameRendering()
 	{
 		SystemTimer.sleep(lastLoopTime + 10 - SystemTimer.getTime());
 
 		// Work out how long its been since the last update, this
 		// will be used to calculate how far the entities should
 		// move this loop.
 		long delta = SystemTimer.getTime() - lastLoopTime;
 		lastLoopTime = SystemTimer.getTime();
 		lastFpsTime += delta;
 		fps++;
 
 		// Update our FPS counter if a second has passed.
 		if (lastFpsTime >= 1000)
 		{
 			window.setTitle(windowTitle + " (FPS: " + fps + ")");
 			lastFpsTime = 0;
 			fps = 0;
 		}
 		
 		// See if we need to activate the refactor.
 		if (activateRefactorThisLoop == true)
 		{
 			// Start down refactor.
 			boardMan.startShiftDown();
 			refactorDownInProgress = true;
 			
 			// Clear flag.
 			activateRefactorThisLoop = false;
 		}
 		
 		// See if we're down refactoring.
 		if (refactorDownInProgress == true)
 		{
 			if (boardMan.moveAll(delta) == false)
 			{									
 				// Synchronize board.
 				boardMan.synchronize();							
 				
 				// Start left refactor.
 				boardMan.startShiftLeft();
 				refactorLeftInProgress = true;
 				
 				// Clear down flag.
 				refactorDownInProgress = false;
 			}
 		}
 		
 		// See if we're left refactoring.
 		if (refactorLeftInProgress == true)
 		{
 			if (boardMan.moveAll(delta) == false)
 			{
 				// Synchronize board.
 				boardMan.synchronize();
 				
 				// Clear left flag.
 				refactorLeftInProgress = false;
 			}
 		}		
 		
 		// Draw the board.
 		boardMan.draw();
 		
 		// Draw the piece.
 		pieceMan.draw();
 		
 		// Draw the text.
 		timerText.setText(String.valueOf(timeMan.getTime()));
 		timerText.setAnchor(Text.BOTTOM | Text.HCENTER);
 		timerText.draw(400, 100);
 		//timerText.draw(0,0);
 //		// cycle round asking each entity to move itself
 //		if (waitingForKeyPress == false)
 //		{
 //			for (int i = 0; i < entities.size(); i++)
 //			{
 //				Entity entity = (Entity) entities.get(i);
 //
 //				entity.move(delta);
 //			}
 //		}
 //
 //		// cycle round drawing all the entities we have in the game
 //		for (int i = 0; i < entities.size(); i++)
 //		{
 //			Entity entity = (Entity) entities.get(i);
 //			entity.draw();
 //		}				
 
 		// Brute force collisions, compare every entity against
 		// every other entity. If any of them collide notify
 		// both entities that the collision has occurred.
 //		for (int p = 0; p < entities.size(); p++)
 //		{
 //			for (int s = p + 1; s < entities.size(); s++)
 //			{
 //				Entity me = (Entity) entities.get(p);
 //				Entity him = (Entity) entities.get(s);
 //
 //				if (me.collidesWith(him))
 //				{
 //					me.collidedWith(him);
 //					him.collidedWith(me);
 //				}
 //			}
 //		}
 
 		// remove any entity that has been marked for clear up
 		entities.removeAll(removeList);
 		removeList.clear();
 
 		// if a game event has indicated that game logic should
 		// be resolved, cycle round every entity requesting that
 		// their personal logic should be considered.
 		if (logicRequiredThisLoop == true)
 		{
 			for (int i = 0; i < entities.size(); i++)
 			{
 				Entity entity = (Entity) entities.get(i);
 				entity.doLogic();
 			}
 
 			logicRequiredThisLoop = false;
 		}
 
 		// if we're waiting for an "any key" press then draw the
 		// current message
 //		if (waitingForKeyPress == true)
 //		{
 //			message.draw(325, 250);
 //		}
 
 		// resolve the movement of the ship. First assume the ship
 		// isn't moving. If either cursor key is pressed then
 		// update the movement appropriately.
 //		ship.setHorizontalMovement(0);
 //
 //		boolean leftPressed = window.isKeyPressed(KeyEvent.VK_LEFT);
 //		boolean rightPressed = window.isKeyPressed(KeyEvent.VK_RIGHT);
 //		boolean firePressed = window.isKeyPressed(KeyEvent.VK_SPACE);
 //
 //		if (!waitingForKeyPress)
 //		{
 //			if ((leftPressed) && (!rightPressed))
 //			{
 //				ship.setHorizontalMovement(-moveSpeed);
 //			}
 //			else if ((rightPressed) && (!leftPressed))
 //			{
 //				ship.setHorizontalMovement(moveSpeed);
 //			}
 //
 //			// if we're pressing fire, attempt to fire
 //			if (firePressed)
 //			{
 //				tryToFire();
 //			}
 //		}
 //		else
 //		{
 //			if (!firePressed)
 //			{
 //				fireHasBeenReleased = true;
 //			}
 //			if ((firePressed) && (fireHasBeenReleased))
 //			{
 //				waitingForKeyPress = false;
 //				fireHasBeenReleased = false;
 //				startGame();
 //			}
 //		}
 		
 		// Handle the timer.
		//timeMan.incrementInternalTime(delta);
 		
 		// if escape has been pressed, stop the game
 		if (window.isKeyPressed(KeyEvent.VK_ESCAPE))
 		{
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * Notification that the game window has been closed
 	 */
 	public void windowClosed()
 	{
 		System.exit(0);
 	}
 
 	/**
 	 * The entry point into the game. We'll simply create an instance of class
 	 * which will start the display and game loop.
 	 * 
 	 * @param argv
 	 *            The arguments that are passed into our game
 	 */
 	public static void main(String argv[])
 	{		
 		Game g = new Game(ResourceFactory.JAVA2D);
 		g.startRendering();		
 	}
 }
