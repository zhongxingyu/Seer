 package pong.control;
 
 import static pong.model.Const.*;
 
 import java.awt.Component;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import javax.media.opengl.GLAutoDrawable;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 
 import pong.model.Ball;
 import pong.model.Const;
 import pong.model.GameItem;
 import pong.model.MenuCube;
 import pong.model.Paddle;
 import pong.model.Player;
 import pong.model.Wall;
 import pong.view.Camera;
 import pong.view.GraphicsEngine;
 
 public class GameEngine {
 	private Physics physics;
 	// Contains the items in the game. These items will be drawn
 	private List<GameItem> items;
 	// Contains the items to be removed before next update.
 	private ArrayList<GameItem> itemsToRemove;
 	// Contains the items to be added before next update.
 	private ArrayList<GameItem> itemsToAdd;
 	// Controls a paddle with the mouse
 	private MouseInput mouse;
 	// Listens for commands to do something with the game
 	private CommandInput cmdInput;
 	// references the paddle to be controlled by player 1 (LOWER PADDLE)
 	private Paddle paddle1;
 	// references the paddle to be controlled by player 2 (UPPER PADDLE)
 	private Paddle paddle2;
 	// The main ball in the game. This ball will never die
 	private Ball mainBall;
 	// this variable determines how long the game(logic) thread will sleep, depending on how fast the game logic is to
 	// be updated
 	private double skipTicks = 1000 / TARGET_FRAMERATE;
 	private int fps; // keeps track of game(logic) updates per second (not rendering fps)
 
 	// Time to sleep in each execution in the game loop
 	long sleepTime;
 
 	private Player player1;
 	private Player player2;
 	// gameState tells graphics engine whether to render in menu-mode or game-mode, start in menu-mode
 	private int gameState = STARTUP_STATE;
 	private GraphicsEngine ge;
 	// the menu cube
 	private MenuCube menu;
 	private boolean resetGame = false;
 	private boolean ballExplode = false;
 
 	public GameEngine() {
 	}
 
 	public static void main(String[] args) {
 		GameEngine ge = new GameEngine();
 		ge.initApplication();
 	}
 
 	public void initApplication() {
 		System.out.println("Initializing graphics...");
 		ge = new GraphicsEngine(this);
 		ge.setUp();
 		runApplication();
 	}
 
 	public void runApplication() {
 		// create the menu cube, is needed for menu.tick call below!
 		initCube();
 		
 		//Initialize game if startstate is IN_GAME
 		if(gameState == IN_GAME){
 			initNewGame();
 		}
 		// Delay to start the game after the window is drawn.
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		// Initialize sound, no delays afterwards in game
 		SoundPlayer.playMP3("padhit.mp3");
 		SoundPlayer.stopMp3();
 		
 		// Used to calculate FPS
 		int frames = 0;
 		long lastTimer1 = System.currentTimeMillis();
 		
 		while (true) {
 			//Used to calculate sleeptime after next tick
 			sleepTime = 0;
 			long nextGameTick = getTickCount();
 
 			//Do tick
 			if(gameState == IN_GAME){
 				gameTick();
 			}else if(gameState == IN_MENU){
 				menu.tick();
 			}
 			//Do camera tick
 			Camera.tick();
 
 			// Calculate how long to sleep.
 			nextGameTick += skipTicks;
 			sleepTime = nextGameTick - getTickCount();
 			if (sleepTime >= 0) {
 				try {
 					Thread.sleep(sleepTime);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 
 			// Calculate FPS
 			frames++;
 			if (System.currentTimeMillis() - lastTimer1 > 1000) {
 				lastTimer1 += 1000;
 				fps = frames;
 				frames = 0;
 			}
 
 		}
 	}
 
 	/** Called when a new game is started. Resets all the players, items and physics. */
 	public void initNewGame() {
 
 		System.out.println("Initializing new game...");
 		items = Collections.synchronizedList(new ArrayList<GameItem>());
 		itemsToRemove = new ArrayList<GameItem>();
 		itemsToAdd = new ArrayList<GameItem>();
 		physics = new Physics();
 		// Create the world
 		physics.create(this);
 
 		// Creates the walls that acts as goals for the player.
 		Wall goal1 = new Wall(0.0f, -Const.GAME_HEIGHT / 2, 0.0f, Const.GAME_WIDTH, false); // Lower player
 		Wall goal2 = new Wall(0.0f, Const.GAME_HEIGHT / 2, 0.0f, Const.GAME_WIDTH, false); // Upper player
 
 		// add player 1 to game
 		paddle1 = new Paddle(0, Const.DEFAULT_DPADDLE_YPOS, 0, Const.DEFAULT_PADDLE_HEIGHT, Const.DEFAULT_PADDLE_WIDTH,
 				Const.DEFAULT_PADDLE_DEPTH);
 		player1 = new Player("Playername1", paddle1);
 		player1.addGoal(goal1);
 		addItemToGame(paddle1);
 
 		// add player 2 to game
 		paddle2 = new Paddle(0, Const.DEFAULT_UPADDLE_YPOS, 0, Const.DEFAULT_PADDLE_HEIGHT, Const.DEFAULT_PADDLE_WIDTH,
 				Const.DEFAULT_PADDLE_DEPTH);
 		player2 = new Player("Playername2", paddle2);
 		player2.addGoal(goal2);
 		addItemToGame(paddle2);
 
 		// add ball to game
 		addItemToGame(mainBall = new Ball(BALL_DEFAULT_XPOS, BALL_DEFAULT_YPOS, 0, Const.BALL_RADIUS));
 		resetGame = false;
 
 		// Adds the goals to physics simulation
 		physics.addWall(goal1);
 		physics.addWall(goal2);
 
 		// Creates the sidewalls
 		physics.addWall(new Wall(-Const.GAME_WIDTH / 2, 0.0f, 0.0f, Const.GAME_HEIGHT, true)); // Left
 		physics.addWall(new Wall(Const.GAME_WIDTH / 2, 0.0f, 0.0f, Const.GAME_HEIGHT, true)); // Right
 
 		// Add listeners for the new paddleobjects.
 		createControlListeners(ge.getDrawable());
 		gameState = IN_GAME;
 	}
 
 	private void gameTick() {
 		if (resetGame == true) {
 			resetBall();
 		}
 		// Remove items that are set to be removed
 		for (GameItem item : itemsToRemove) {
 			removeItemFromGame(item);
 		}
 		itemsToRemove.clear();
 		// Add items that are queued up to be added
 		for (GameItem item : itemsToAdd) {
 			createObject(item);
 		}
 		itemsToAdd.clear();
 
 		// get mousepointer position on canvas, move the player controlled paddle
		paddle1.moveItem(mouse.getxPos(), mouse.getyPos(), ge.getFrameWidth(), ge.getFrameHeight());
 		
 		paddle1.adjustYPos(DEFAULT_DPADDLE_YPOS, true);
 		paddle2.adjustYPos(DEFAULT_UPADDLE_YPOS, false);
 		// restrict maximum ball speed by lineardampening it over a certain speed
 		checkBallSpeed();
 		physics.update();
 		updatePos();
 
 	}
 
 	/**
 	 * @param losingPlayer, ball
 	 * This method is called from HitDetection when a ball has hit a goal.
 	 * The method calls a explosion rendering effect, plays a sound, removes items if the ball was not the Mainball,
 	 * It also reduces the amount of lives, increases score. If a player has no lives left a score screen is shown
 	 * and a method that resets the game is called.
 	 */
 	public void ballOut(Player losingPlayer, Ball ball) {
 
 		// Resets the ball to the center if the ball is the mainball. All other balls are deleted.
 
 		SoundPlayer.playMP3("ballout.mp3");
 
 		// Render explosion
 		ballExplode = true;
 
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		}
 
 		if (ball == mainBall) {
 			resetGame = true;
 		} else {
 			itemsToRemove.add(ball);
 		}
 
 		Player winner = new Player("", null);
 		if (losingPlayer == player1) {
 			winner = player2;
 			losingPlayer.setLives(losingPlayer.getLives() - 1);
 		} else {
 			winner = player1;
 			losingPlayer.setLives(losingPlayer.getLives() - 1);
 		}
 
 		// Increase the winners score!
 		updateScore(winner);
 
 		if (losingPlayer.getLives() < 1) {
 
 			gameState = GAME_ENDED;
 			SoundPlayer.playMP3("win.mp3");
 			try {
 				Thread.sleep(5000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			SoundPlayer.stopMp3();
 			ballExplode = false;
 			initNewGame();
 
 		}
 		ballExplode = false;
 
 	}
 
 	/**
 	 * Resets the ball to the middle of the game and its direction is faced to the winning players paddle(goal).
 	 */
 	public void resetBall() {
 		Body ball;
 		Vec2 vec;
 		ball = mainBall.getBody();
 		Random gen = new Random();
 		float r = gen.nextFloat() + 2;
 		float y = ball.getPosition().y;
 		vec = new Vec2(ball.getLinearVelocity());
 		float yVec = 0;
 		float xVec = 0;
 		// Set Y to opposite direction..
 		if (y > 0) {
 			yVec = -BALL_MAXSPEED;
 		} else if (y < 0) {
 			yVec = BALL_MAXSPEED;
 		}
 		// Randomize X
 		if (gen.nextBoolean() == true) {
 			xVec = r * -1;
 		} else {
 			xVec = r;
 		}
 		vec.set(xVec, yVec);
 		ball.setTransform(new Vec2(0, 0), 0);
 		ball.setLinearVelocity(vec);
 		resetGame = false;
 	}
 
 	/**
 	 * Increase the winning players score by 100
 	 * 
 	 * @param winner
 	 */
 	public void updateScore(Player winner) {
 		int score = 0;
 		score = winner.getScore();
 		// Increase points with 100
 		winner.setScore(score + 100);
 	}
 
 	private void createObject(GameItem item) {
 		item.setBody(physics.addObject(item));
 		item.getBody().setBullet(true);
 		items.add(item);
 	}
 
 	/** USE THIS METHOD IF YOU WANT TO ADD OBJECTS TO THE GAME Paddles, balls, obstacles.... */
 	public void addItemToGame(GameItem item) {
 		itemsToAdd.add(item);
 	}
 
 	/** Use this method when you want to remove a object from the game.
 	 * 
 	 * @param item */
 	public void removeItemFromGame(GameItem item) {
 		physics.destroyBody(item.getBody());
 		items.remove(item);
 	}
 
 	/**
 	 * Updates the coordinates of the objects in the scene. Reads from the physic simulation
 	 */
 	public void updatePos() {
 		// variable i is used to identify the pad to be controlled by the user, since at this time no unique
 		// identifier for each pad exists
 		for (GameItem item : items) {
 			Body body;
 			body = item.getBody();
 			item.setxPos(body.getPosition().x);
 			item.setyPos(body.getPosition().y);
 		}
 	}
 
 	/** Checks a ball's speed. If it's over maximum: set lineardamping to something. If not: set lineardamping to zero. */
 	public void checkBallSpeed() {
 
 		for (GameItem item : items) {
 			if (item.getType().equals("BALL")) {
 				Ball ball = (Ball) item;
 				ball.adjustSpeed();
 			}
 		}
 
 	}
 
 	/*
 	 * creates the menu cube, adds the options (strings) to print on each side
 	 */
 	private void initCube() {
 		menu = new MenuCube(0, 0, MENU_ZPOS, MENU_SIZE, MENU_SIZE, MENU_SIZE);
 		// add the options to be shown on the menu cube's sides
 		ArrayList<ArrayList<String>> options = new ArrayList<ArrayList<String>>();
 
 		ArrayList<String> topOptions = new ArrayList<String>();
 		topOptions.add("Top");
 		options.add(topOptions);
 
 		ArrayList<String> frontOptions = new ArrayList<String>();
 		frontOptions.add("New Game");
 		options.add(frontOptions);
 
 		ArrayList<String> rightOptions = new ArrayList<String>();
 		rightOptions.add("Right");
 		options.add(rightOptions);
 
 		ArrayList<String> backOptions = new ArrayList<String>();
 		backOptions.add("Back");
 		options.add(backOptions);
 
 		ArrayList<String> leftOptions = new ArrayList<String>();
 		leftOptions.add("Left");
 		options.add(leftOptions);
 
 		ArrayList<String> bottomOptions = new ArrayList<String>();
 		bottomOptions.add("Bottom");
 		options.add(bottomOptions);
 
 		menu.setOptions(options);
 
 	}
 
 	/** Creates mouse object, adds listeners that control paddles
 	 * 
 	 * @param glDrawable */
 	public void createControlListeners(GLAutoDrawable glDrawable) {
 		// create mouse listener and connect it to the moveableItem to be controlled
 		mouse = new MouseInput(this);
 		((Component) glDrawable).addMouseMotionListener(mouse);
 		((Component) glDrawable).addMouseListener(mouse);
 
 	}
 
 	/** Creates commandlistener. This listener is persistent during the whole runtime.
 	 * 
 	 * @param glDrawable */
 	public void createCommandListener(GLAutoDrawable glDrawable) {
 		cmdInput = new CommandInput(this);
 		((Component) glDrawable).addKeyListener(cmdInput);
 	}
 
 	/** returns the time in milliseconds
 	 * 
 	 * @return */
 	public long getTickCount() {
 		return System.currentTimeMillis();
 	}
 
 	public void exit() {
 		System.exit(0);
 	}
 
 	public List<GameItem> getGameItems() {
 		return items;
 	}
 
 	public int getGameState() {
 		return gameState;
 	}
 
 	public Player getPlayer1() {
 		return player1;
 	}
 
 	public Player getPlayer2() {
 		return player2;
 	}
 
 	public MenuCube getMenu() {
 		return menu;
 	}
 
 	public int getFps() {
 		return fps;
 	}
 
 	public long getSleepTime() {
 		return sleepTime;
 	}
 
 	public void setGameState(int gameState) {
 		this.gameState = gameState;
 	}
 
 	public Ball getMainBall() {
 		return mainBall;
 	}
 
 	public void setMainBall(Ball mainBall) {
 		this.mainBall = mainBall;
 	}
 
 	public boolean isBallExplode() {
 		return ballExplode;
 	}
 
 	public void setBallExplode(boolean ballExplode) {
 		this.ballExplode = ballExplode;
 	}
 }
