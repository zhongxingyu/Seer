 package pong.control;
 
 import java.awt.Component;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 //static import below enables us to write x == IN_MENU instead of x == Const.IN_MENU
 import static pong.model.Const.*;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.awt.GLCanvas;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 
 import pong.model.*;
 import pong.view.*;
 
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
 	// references the paddle to be controlled by player 1
 	private Paddle paddle1;
 	// references the paddle to be controlled by player 2
 	private Paddle paddle2;
 	// The main ball in the game. This ball will never die
 	private Ball mainBall;
 
 	private Player player1;
 	private Player player2;
 	// gameState tells graphics engine whether to render in menu-mode or game-mode, start in menu-mode
 	private int gameState = STARTUP_STATE;
 	private GraphicsEngine ge;
 	// the menu cube
 	private MenuCube menu;
 	private boolean resetGame = false;
 
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
 
 		initNewGame();
 	}
 
 	/**
 	 * Called when a new game is started. Resets all the players, items and physics.
 	 */
 	public void initNewGame() {
 
 		System.out.println("Initializing new game...");
 		items = Collections.synchronizedList(new ArrayList<GameItem>());
 		itemsToRemove = new ArrayList<GameItem>();
 		itemsToAdd = new ArrayList<GameItem>();
 		physics = new Physics();
 		// Create the world
 		physics.create(this);
 
 		// Creates the walls that acts as goals for the player.
 		Wall goal1 = new Wall(0.0f, -Const.GAME_HEIGHT / 2, 0.0f,
 				Const.GAME_WIDTH, false); // Lower player
 		Wall goal2 = new Wall(0.0f, Const.GAME_HEIGHT / 2, 0.0f,
 				Const.GAME_WIDTH, false); // Upper player
 
 		// add player 1 to game
 		paddle1 = new Paddle(0, Const.DEFAULT_DPADDLE_YPOS, 0,
 				Const.DEFAULT_PADDLE_HEIGHT, Const.DEFAULT_PADDLE_WIDTH,
 				Const.DEFAULT_PADDLE_DEPTH);
 		player1 = new Player("Playername1", paddle1);
 		player1.addGoal(goal1);
 		addItemToGame(paddle1);
 
 		// add player 2 to game
 		paddle2 = new Paddle(0, Const.DEFAULT_UPADDLE_YPOS, 0,
 				Const.DEFAULT_PADDLE_HEIGHT, Const.DEFAULT_PADDLE_WIDTH,
 				Const.DEFAULT_PADDLE_DEPTH);
 		player2 = new Player("Playername2", paddle2);
 		player2.addGoal(goal2);
 		addItemToGame(paddle2);
 
 		// create the menu cube
 		initCube();
 
 		// add ball to game
 		addItemToGame(mainBall = new Ball(BALL_DEFAULT_XPOS, BALL_DEFAULT_YPOS,
 				0, Const.BALL_RADIUS));
 
 		// Adds the goals to physics simulation
 		physics.addWall(goal1);
 		physics.addWall(goal2);
 
 		// Creates the sidewalls
 		physics.addWall(new Wall(-Const.GAME_WIDTH / 2, 0.0f, 0.0f,
 				Const.GAME_HEIGHT, true)); // Left
 		physics.addWall(new Wall(Const.GAME_WIDTH / 2, 0.0f, 0.0f,
 				Const.GAME_HEIGHT, true)); // Right
 
 		// Add listeners for the new paddleobjects.
 		createControlListeners(ge.getDrawable());
 
 		// Run the game.
 		startGame();
 	}
 
 	private void startGame() {
 		System.out.println("Running the game...");
 		// run game, draw score, zoom etc. if starting/resuming the game
 		if (gameState == IN_GAME) {
 			// Delay to start the game after the window is drawn.
 			try {
 				Thread.sleep(2000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			Camera.smoothZoom(100);
 
 			while (true) {
 				// Thread.sleep(1);
 				// Checks if the balls have ludicrous speed.
 				gameState = IN_GAME;
 				try {
 					Thread.sleep(1);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				synchronized (items) {
 					if (resetGame == true) {
 						resetBall();
 					}
 					// Remove items that are set to be removed
 					for (GameItem item : itemsToRemove) {
 						removeItemFromGame(item);
 					}
 					itemsToRemove.clear();
 					// Add items that are queued up to be added
 					for(GameItem item : itemsToAdd){
 						createObject(item);
 					}
 					itemsToAdd.clear();
 					
 					checkBallSpeed();
 					physics.update();
 					updatePos();
 				}
 			}
 		}
 	}
 
 	public void ballOut(Player losingPlayer, Ball ball) {
 
 		// Resets the ball to the center if the ball is the mainball. All other balls are deleted.
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
 			try {
 				Thread.sleep(5000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			initNewGame();
 		}
 
 	}
 
 	public void resetBall() {
 		Body ball;
 		ball = mainBall.getBody();
 
 		Random generator = new Random();
 		float r = generator.nextFloat();
 
 		Vec2 vec = new Vec2(ball.getLinearVelocity());
 
 		// Now set mainBall to default values.. ready for next round!
 		ball.setTransform(new Vec2(0, 0), 0);
 		mainBall.setxPos(Const.BALL_DEFAULT_XPOS);
 		mainBall.setyPos(Const.BALL_DEFAULT_YPOS);
 		float x = vec.x;
 		float y = vec.y;
 		x = x * -1 + r;
 		y = y * -1;
 		vec.set(x, y);
 
 		ball.setLinearVelocity(vec);
 
 		resetGame = false;
 
 	}
 
 	/*
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
 	
 	private void createObject(GameItem item){
 		item.setBody(physics.addObject(item));
 		item.getBody().setBullet(true);
 		items.add(item);
 	}
 
 	/**
 	 * USE THIS METHOD IF YOU WANT TO ADD OBJECTS TO THE GAME Paddles, balls, obstacles....
 	 */
 	public void addItemToGame(GameItem item) {
 		itemsToAdd.add(item);
 	}
 
 	/**
 	 * Use this method when you want to remove a object from the game.
 	 * 
 	 * @param item
 	 */
 	public void removeItemFromGame(GameItem item) {
 		physics.destroyBody(item.getBody());
 		items.remove(item);
 	}
 
 	/*
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
 
 	/**
 	 * Checks a ball's speed. If it's over maximum: set lineardamping to something. If not: set lineardamping to zero.
 	 * 
 	 */
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
 		frontOptions.add("Front");
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
 
 	/**
 	 * Creates mouse object, adds listeners that control paddles
 	 * 
 	 * @param glDrawable
 	 */
 	public void createControlListeners(GLAutoDrawable glDrawable) {
 		// create mouse listener and connect it to the moveableItem to be controlled
 		mouse = new MouseInput(paddle1);
 		((Component) glDrawable).addMouseMotionListener(mouse);
 		((Component) glDrawable).addMouseListener(mouse);
 
 	}
 
 	/**
 	 * Creates commandlistener. This listener is persistent during the whole runtime.
 	 * 
 	 * @param glDrawable
 	 */
 	public void createCommandListener(GLAutoDrawable glDrawable) {
 		cmdInput = new CommandInput(this);
 		((Component) glDrawable).addKeyListener(cmdInput);
 	}
 
 	/**
 	 * Play sound testing http://www.soundbyter.com/2011/04/free-sci-fi-tone-sound-effect/ blip sound source
 	 * 
 	 * @param Takes
 	 *            in a filename for the sound to be played, plays it in a own thread
 	 */
 	public synchronized void playSound(final String url) {
 		new Thread(new Runnable() { // the wrapper thread is unnecessary, unless it blocks on the Clip finishing, see
 					// comments
 					public void run() {
 						try {
 							Clip clip = AudioSystem.getClip();
 							AudioInputStream inputStream = AudioSystem
 									.getAudioInputStream(getClass()
 											.getResourceAsStream(
 													"/resource/" + url));
 							clip.open(inputStream);
 							clip.start();
 						} catch (Exception e) {
 							System.err.println(e.getMessage());
 						}
 					}
 				}).start();
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
 
 }
