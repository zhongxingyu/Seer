 package com.slauson.dasher.game;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.widget.Toast;
 
 import com.slauson.dasher.R;
 import com.slauson.dasher.objects.Asteroid;
 import com.slauson.dasher.objects.Drop;
 import com.slauson.dasher.objects.Player;
 import com.slauson.dasher.other.GameBaseActivity;
 import com.slauson.dasher.powerups.ActivePowerup;
 import com.slauson.dasher.powerups.PowerupBlackHole;
 import com.slauson.dasher.powerups.PowerupBumper;
 import com.slauson.dasher.powerups.PowerupDrill;
 import com.slauson.dasher.powerups.PowerupInvulnerability;
 import com.slauson.dasher.powerups.PowerupMagnet;
 import com.slauson.dasher.powerups.PowerupSlow;
 import com.slauson.dasher.powerups.PowerupSmall;
 import com.slauson.dasher.status.Achievements;
 import com.slauson.dasher.status.Options;
 import com.slauson.dasher.status.Debugging;
 import com.slauson.dasher.status.LocalStatistics;
 import com.slauson.dasher.status.Statistics;
 import com.slauson.dasher.status.Upgrades;
 
 /**
  * Main game class.
  * @author Josh Slauson
  *
  */
 public class Game {
 
 	/**
 	 * Private stuff
 	 */
 	
 	/** Activity used for gameOver and transitioning to other activities **/
 	private GameBaseActivity gameActivity;
 	
 	/** Used to determine if game is initialized from surface view **/
 	private boolean initialized;
 	
 	/** Paint for drawing on canvas **/
 	private Paint paint;
 	
 	/** Player ship **/
 	private Player player;
 	
 	/** List of active asteroids **/
 	private ArrayList<Asteroid> asteroids;
 	
 	/** List of active drops **/
 	private LinkedList<Drop> drops;
 
 	/** List of active powerups **/
 	private LinkedList<ActivePowerup> activePowerups;
 	
 	/** List of available drops **/
 	private ArrayList<Integer> availableDrops;
 	
 	/** Timer for game over **/
 	private Timer gameOverTimer;
 	
 	/** Counter for bomb animation **/
 	private int bombCounter;
 	/** Counter for quasar animation **/
 	private int quasarCounter;
 	
 	/** Time of last touch down event, for double-tap based dashing **/
 	private long lastTouchDownTime1;
 	/** Time of second last touch down event, for double-tap based dashing **/
 	private long lastTouchDownTime2;
 	
 	/** Time of last player movement **/
 	private long lastMoveTime;
 
 	/** Current level of game **/
 	private Level level;
 	
 	/** Local statistics, so we don't have to keep calling getInstance() **/
 	private Statistics localStatistics;
 	
 	/** Time game was paused, used for updating times on game resume **/
 	private long pauseTime;
 
 	/** Threshold for accelerometer dash **/
 	private float accelerometerDashThreshold;
 	/** Deadzone for accelerometer movement **/
 	private float accelerometerDeadzone;
 	/** Max value for accelerometer **/
 	private float accelerometerMax;
 	
 	/** Maximum value for bomb counter **/
 	private int bombFrames;
 	
 	/** Maximum value for quasar counter **/
 	private int quasarFrames;
 
 	/** Show player **/
 	private boolean playerVisible;
 	/** Enables player movement **/
 	private boolean enableMove;
 	/** Enables player dash ability **/
 	private boolean enableDash;
 	/** Enables drops **/
 	private boolean enableDrops;
 
 	/** Runtime for updates **/
 	private static long runtimeAnalysisUpdateTime = 0;
 	/** Runtime for drawing **/
 	private static long runtimeAnalysisDrawTime = 0;
 	/** Number of updates/draws **/
 	private static long runtimeAnalysisNumUpdates = 0;
 
 	
 	/**
 	 * Constants - private
 	 */
 
 	/** Paint width for drawing player ship **/
 	private static final int PLAYER_PAINT_STROKE_WIDTH = 2;
 	/** Paint color for drawing player ship **/
 	private static final int PLAYER_PAINT_COLOR = Color.WHITE;
 	/** Paint width for drawing asteroids **/
 	private static final int ASTEROID_PAINT_STROKE_WIDTH = 1;
 	/** Paint color for drawing asteroids **/
 	private static final int ASTEROID_PAINT_COLOR = Color.WHITE;
 	
 	/** Factor for touching the ship to dash **/
 	private static final float DASH_TOUCH_FACTOR = 1.5f;
 	/** Minimum duration for double tapping to dash **/
 	private static final int DASH_DOUBLE_TAP_MIN_DURATION = 500;
 
 	/** Deadzone for low sensitivity accelerometer **/
 	private static final float ACCELEROMETER_DEADZONE_LOW = 0.10f;
 	/** Deadzone for medium sensitivity accelerometer **/
 	private static final float ACCELEROMETER_DEADZONE_MEDIUM = 0.05f;
 	/** Deadzone for high sensitivity accelerometer **/
 	private static final float ACCELEROMETER_DEADZONE_HIGH = 0.02f;
 	
 	/** Maximum value for low sensitivity accelerometer **/
 	private static final float ACCELEROMETER_MAX_LOW = 0.4f;
 	/** Maximum value for medium sensitivity accelerometer **/
 	private static final float ACCELEROMETER_MAX_MEDIUM = 0.3f;
 	/** Maximum value for high sensitivity accelerometer **/
 	private static final float ACCELEROMETER_MAX_HIGH = 0.2f;
 	
 	/** Threshold for activating dash with low sensitivity accelerometer **/
 	private static final float ACCELEROMETER_DASH_THRESHOLD_LOW = 0.25f;
 	/** Threshold for activating dash with low sensitivity accelerometer **/
 	private static final float ACCELEROMETER_DASH_THRESHOLD_MEDIUM = 0.2f;
 	/** Threshold for activating dash with low sensitivity accelerometer **/
 	private static final float ACCELEROMETER_DASH_THRESHOLD_HIGH = 0.15f;
 	
 	/** Level to start on in hard mode **/
 	private static final int HARD_MODE_START_LEVEL = 10;
 
 	/**
 	 * Constants - public
 	 */
 	
 	// NOTE: these values must match indices in Upgrades.upgrades
 	/** Number of total powerups **/
 	public static final int NUM_POWERUPS = 8;
 	/** No powerup id **/
 	public static final int POWERUP_NONE = 0;
 	/** Small powerup id **/
 	public static final int POWERUP_SMALL = 1;
 	/** Slow powerup id **/
 	public static final int POWERUP_SLOW = 2;
 	/** Invulnerability powerup id **/
 	public static final int POWERUP_INVULNERABILITY = 3;
 	/** Drill powerup id **/
 	public static final int POWERUP_DRILL = 4;
 	/** Magnet powerup id **/
 	public static final int POWERUP_MAGNET = 5;
 	/** Black hole powerup id **/
 	public static final int POWERUP_BLACK_HOLE = 6;
 	/** Bumper powerup id **/
 	public static final int POWERUP_BUMPER = 7;
 	/** Bomb powerup id **/
 	public static final int POWERUP_BOMB = 8;
 	
 	/** Game status id for game being paused **/
 	public static final int STATUS_PAUSED = 0;
 	/** Game status id for game running **/
 	public static final int STATUS_RUNNING = 1;
 
 	/** Asteroids are going in normal direction (down) **/
 	public static final int DIRECTION_NORMAL = 0;
 	/** Asteroids are going in reverse direction (up) **/
 	public static final int DIRECTION_REVERSE = 1;
 	
 	/** Basic game mode with no drops, dash **/
 	public static final int GAME_MODE_INSTRUCTIONS = 0;
 	/** Basic game mode with no drops, dash **/
 	public static final int GAME_MODE_BASIC = 1;
 	/** Normal game mode **/
 	public static final int GAME_MODE_NORMAL = 2;
 	/** Hard game mode where player starts on level 10 **/
 	public static final int GAME_MODE_HARD = 3;
 	/** Snowflake game mode where asteroids are snowflakes **/
 	public static final int GAME_MODE_SNOWFLAKE = 4;
 
 
 	/**
 	 * Shared stuff
 	 */
 	
 	/** Canvas width **/
 	public static int canvasWidth = 0;
 	/** Canvas height **/
 	public static int canvasHeight = 0;
 	
 	/** Slow powerup **/
 	public static PowerupSlow powerupSlow;
 	/** Invulnerability powerup **/
 	public static PowerupInvulnerability powerupInvulnerability;
 	/** Small powerup **/
 	public static PowerupSmall powerupSmall;
 	
 	/** Current game status **/
 	public static int gameStatus = STATUS_RUNNING;
 	/** Current direction **/
 	public static int direction = DIRECTION_NORMAL;
 	/** Current gravity **/
 	public static float gravity = 1f;
 	
 	/** Random **/
 	public static Random random = new Random();
 	
 	/** Maximum sleep duration **/
 	public static int maxSleepTime = 1000/30;
 	
 	/** Number of available drops **/
 	public static int numAvailableDrops = 0;
 	
 	/** Used to differentiate different game modes **/
 	public static int gameMode = GAME_MODE_NORMAL;
 
 	/**
 	 * Resets all static state to default values
 	 */
 	public static void reset() {
 		canvasWidth = 0;
 		canvasHeight = 0;
 		
 		gameMode = GAME_MODE_NORMAL;
 		gameStatus = STATUS_RUNNING;
 		direction = DIRECTION_NORMAL;
 		gravity = 1f;
 		
 		maxSleepTime = 1000/30;
 		
 		numAvailableDrops = 0;
 	}
 	
 	public Game(GameBaseActivity gameActivity) {
 		
 		this.gameActivity = gameActivity;
 		
 		initialized = false;
 	}
 	
 	/**
 	 * Initializes all game state
 	 * @param width canvas width
 	 * @param height canvas height
 	 */
 	public void init(int width, int height) {
 		
 		canvasWidth = width;
 		canvasHeight = height;
 		
 		player = new Player(Options.controlType == Options.CONTROL_TOUCH, gameMode == GAME_MODE_INSTRUCTIONS);
 		
 		// setup different game modes
 		if (gameMode == GAME_MODE_INSTRUCTIONS) {
 			level = new Level(-1, false);
 		} else if (gameMode == GAME_MODE_BASIC) {
 			level = new Level(Debugging.level, Debugging.levelProgression);
 			toggleDash(false);
 			toggleDrops(false);
 		} else if (gameMode == GAME_MODE_NORMAL || gameMode == GAME_MODE_SNOWFLAKE) {
 			level = new Level(Debugging.level, Debugging.levelProgression);
 		} else if (gameMode == GAME_MODE_HARD){
 			level = new Level(HARD_MODE_START_LEVEL, Debugging.levelProgression);
 		}
 		
 		paint = new Paint();
 		if (Options.graphicsType == Options.GRAPHICS_NORMAL) {
 			paint.setAntiAlias(true);
 			paint.setDither(true);
 		}
 		
 		asteroids = new ArrayList<Asteroid>();
 		drops = new LinkedList<Drop>();
 
 		gameOverTimer = null;
 		
 		// powerups
 		activePowerups = new LinkedList<ActivePowerup>();	
 		bombCounter = 0;
 		quasarCounter = 0;
 		
 		playerVisible = true;
 		enableMove = true;
 		enableDash = true;
 		enableDrops = true;
 		
 		// populate available drops
 		availableDrops = new ArrayList<Integer>();
 		for (int i = 1; i <= NUM_POWERUPS; i++) {
 			if (Upgrades.getUpgrade(i).getLevel() >= Upgrades.POWERUP_UNLOCKED) {
 				availableDrops.add(i);
 				numAvailableDrops++;
 			}
 		}
 		
 		// make sure we don't think the first single tap is a double tap
 		lastTouchDownTime1 = 0;
 		lastTouchDownTime2 = -2*DASH_DOUBLE_TAP_MIN_DURATION;
 		
 		lastMoveTime = System.currentTimeMillis();
 		pauseTime = -1;
 		
 		maxSleepTime = 1000/Options.frameRate;
 					
 		powerupSlow = new PowerupSlow(Upgrades.slowUpgrade.getLevel());
 		powerupInvulnerability = new PowerupInvulnerability(Upgrades.invulnerabilityUpgrade.getLevel());
 		powerupSmall = new PowerupSmall(Upgrades.smallUpgrade.getLevel());
 
 		updateAccelerometerValues();
 		updateFrameCounters();
 		
 		float radius, speed;
 		
 		for (int i = 0; i < level.getNumAsteroids() && gameMode != GAME_MODE_INSTRUCTIONS; i++) {
 			
 			radius = level.getAsteroidRadiusFactorMin() + random.nextFloat()*level.getAsteroidRadiusFactorOffset();
 			speed = level.getAsteroidSpeedFactorMin() + random.nextFloat()*level.getAsteroidSpeedFactorOffset();
 			
 			asteroids.add(new Asteroid(radius, speed, level.getAsteroidRadiusFactorMax(), level.getAsteroidHorizontalMovementOffset()));
 		}
 		
 		localStatistics = LocalStatistics.getInstance();
 		
 		initialized = true;
 		
 		// call up to activity
 		gameActivity.init();
 	}
 	
 	/**
 	 * Draws everything on given canvas
 	 * @param canvas canvas to draw on
 	 */
 	public void draw(Canvas canvas) {
 		
 		long startTime = 0;
 		
 		if (Debugging.runtimeAnalysis) {
 			startTime = System.currentTimeMillis();
 		}
 
 		// clear screen
 		canvas.drawColor(Color.BLACK);
 		
 		// draw active powerups
 		synchronized (activePowerups) {
 			for (ActivePowerup activePowerup : activePowerups) {
 				activePowerup.draw(canvas, paint);
 			}
 		}
 		
 		// draw asteroids
 		paint.setStrokeWidth(ASTEROID_PAINT_STROKE_WIDTH);
 		paint.setColor(ASTEROID_PAINT_COLOR);
 		synchronized (asteroids) {
 		
 			paint.setStrokeWidth(2);
 			paint.setColor(Color.WHITE);
 			paint.setStyle(Style.STROKE);
 
 			for (Asteroid asteroid : asteroids) {
 				asteroid.draw(canvas, paint);
 			}
 		}
 		
 		// draw player
 		if (playerVisible) {
 			paint.setStrokeWidth(PLAYER_PAINT_STROKE_WIDTH);
 			paint.setColor(PLAYER_PAINT_COLOR);
 			player.draw(canvas, paint);
 		}
 		
 		// draw drops
 		synchronized (drops) {
 			for (Drop drop : drops) {
 				drop.draw(canvas, paint);
 			}
 		}
 
 		// overlay bomb animation 
 		if (bombCounter > 0) {
 			float factor = Math.abs(1f*bombFrames/2 - bombCounter)/bombFrames*2;
 			
 			paint.setAlpha((int)(255 - 255*factor));
 			paint.setStyle(Style.FILL);
 			canvas.drawRect(0, 0, canvasWidth, canvasHeight, paint);
 
 			paint.setAlpha(255);
 		}
 		
 		// overlay quasar animation 
 		if (quasarCounter > 0) {
 			float factor = Math.abs(1f*quasarFrames/2 - quasarCounter)/quasarFrames*2;
 			
 			paint.setAlpha((int)(255 - 255*factor));
 			paint.setStyle(Style.FILL);
 			canvas.drawRect(0, 0, canvasWidth, canvasHeight, paint);
 
 			paint.setAlpha(255);
 		}
 		
 		if (Debugging.runtimeAnalysis) {
 			runtimeAnalysisDrawTime += System.currentTimeMillis() - startTime;
 		}
 
 	}
 	
 	/**
 	 * Update game state
 	 */
 	public void updateStates() {
 		
 		if (gameStatus == STATUS_PAUSED) {
 			return;
 		}
 		
 		long startTime = 0;
 		
 		if (Debugging.runtimeAnalysis) {
 			startTime = System.currentTimeMillis();
 		}
 		
 		// update bomb counter
 		if (bombCounter > 0) {
 			bombCounter--;
 			
 			if (bombCounter == bombFrames/2) {
 				activateBomb();
 			}
 		}
 		
 		// update quasar counter
 		if (quasarCounter > 0) {
 			quasarCounter--;
 		}
 		
 		// add more asteroids if needed
 		if (level.update()) {
 			// add more asteroids if necessary
 			int numAsteroidsToAdd = level.getNumAsteroids() - asteroids.size();
 			
 			float radius, speed;
 			
 			for (int i = 0; i < numAsteroidsToAdd; i++) {
 				
 				radius = level.getAsteroidRadiusFactorMin() + random.nextFloat()*level.getAsteroidRadiusFactorOffset();
 				speed = level.getAsteroidSpeedFactorMin() + random.nextFloat()*level.getAsteroidSpeedFactorOffset();
 				
 				asteroids.add(new Asteroid(radius, speed, level.getAsteroidRadiusFactorMax(), level.getAsteroidHorizontalMovementOffset()));
 			}
 		}
 	
 		// update everything
 		updateAsteroids();
 		updateDrops();
 		updatePowerups();
 		updatePlayer();
 		
 		if (Debugging.runtimeAnalysis) {
 			runtimeAnalysisUpdateTime += System.currentTimeMillis() - startTime;
 			runtimeAnalysisNumUpdates++;
 		}
 
 	}
 	
 	/**
 	 * Handles touch event
 	 * @param event touch event
 	 */
 	public void touchEvent(MotionEvent event, float offsetY) {
 		
 		if (player.getStatus() != Player.STATUS_NORMAL && player.getStatus() != Player.STATUS_INVULNERABILITY) {
 			return;
 		}
 		
 		// get event position
 		float x = event.getX();
 		float y = event.getY() + offsetY;
 		
 		int action = event.getAction();
 		
 		switch(action) {
 		case MotionEvent.ACTION_DOWN:
 			// check if pressed on player's position, then dash
 			if (x < player.getX() + player.getWidth()*DASH_TOUCH_FACTOR/2 && x > player.getX() - player.getWidth()*DASH_TOUCH_FACTOR/2 &&
 					y < player.getY() + player.getHeight()*DASH_TOUCH_FACTOR/2 && y > player.getY() - player.getHeight()*DASH_TOUCH_FACTOR/2 &&
 					player.canDash() && enableDash)
 			{
 				player.dash();
 				break;
 			}
 			
 			lastTouchDownTime2 = lastTouchDownTime1;
 			lastTouchDownTime1 = System.currentTimeMillis();
 			
 			// check stay in place achievement
 			if (System.currentTimeMillis() - lastMoveTime > 1000*Achievements.LOCAL_OTHER_STAY_IN_PLACE_TIME) {
 				Achievements.unlockLocalAchievement(Achievements.localOtherStayInPlace);
 			}
 			
 			// don't break here so that player still moves
 		case MotionEvent.ACTION_MOVE:
 			// only move horizontally when player is in position
 			if (player.inPosition() && enableMove) {
 				player.setGoX(x);
 			}
 			
 			break;
 		case MotionEvent.ACTION_UP:
 			// dash based on double tap
 			if (player.getStatus() == Player.STATUS_NORMAL && player.canDash() && enableDash) {
 				if (lastTouchDownTime1 - lastTouchDownTime2 < DASH_DOUBLE_TAP_MIN_DURATION && System.currentTimeMillis() - lastTouchDownTime2 < DASH_DOUBLE_TAP_MIN_DURATION) {
 					player.dash();
 				}
 			}
 			
 			lastMoveTime = System.currentTimeMillis();
 		default:
 			break;
 		}
 	}
 	
 	/**
 	 * Handles key down event
 	 * @param keyCode key code of key down
 	 */
 	public void keyDown(int keyCode) {
 		if ((player.getStatus() != Player.STATUS_NORMAL && player.getStatus() != Player.STATUS_INVULNERABILITY) ||
 				!player.inPosition() || !enableMove)
 		{
 			return;
 		}
 		
 		switch(keyCode) {
 		// left
 		case KeyEvent.KEYCODE_DPAD_LEFT:
 		case KeyEvent.KEYCODE_S:
 			player.moveLeft();
 				
 			// check stay in place achievement
 			if (System.currentTimeMillis() - lastMoveTime > 1000*Achievements.LOCAL_OTHER_STAY_IN_PLACE_TIME) {
 				Achievements.unlockLocalAchievement(Achievements.localOtherStayInPlace);
 			}
 			lastMoveTime = System.currentTimeMillis();
 			break;		
 		// right
 		case KeyEvent.KEYCODE_DPAD_RIGHT:
 		case KeyEvent.KEYCODE_L:
 			player.moveRight();
 				
 			// check stay in place achievement
 			if (System.currentTimeMillis() - lastMoveTime > 1000*Achievements.LOCAL_OTHER_STAY_IN_PLACE_TIME) {
 				Achievements.unlockLocalAchievement(Achievements.localOtherStayInPlace);
 			}
 			lastMoveTime = System.currentTimeMillis();
 			break;
 		}
 	}
 	
 	/**
 	 * Handles key up event
 	 * @param keyCode key code of key up
 	 */
 	public void keyUp(int keyCode) {
 		if ((player.getStatus() != Player.STATUS_NORMAL && player.getStatus() != Player.STATUS_INVULNERABILITY) ||
 				!player.inPosition())
 		{
 			return;
 		}
 
 		switch(keyCode) {
 		// left/right
 		case KeyEvent.KEYCODE_DPAD_LEFT:
 		case KeyEvent.KEYCODE_DPAD_RIGHT:
 		case KeyEvent.KEYCODE_S:
 		case KeyEvent.KEYCODE_L:
 			player.moveStop();
 			break;
 		// dpad center
 		case KeyEvent.KEYCODE_DPAD_CENTER:
 		case KeyEvent.KEYCODE_SPACE:
 			if (player.canDash() && enableDash) {
 				player.dash();
 			}
 			break;
 		}
 	}
 	
 	/**
 	 * Handles accelerometer updates
 	 * @param tx horizontal orientation
 	 * @param ty vertical orientation
 	 */
 	public void updateAccelerometer(float tx, float ty) {
 		
 		if ((player.getStatus() != Player.STATUS_NORMAL && player.getStatus() != Player.STATUS_INVULNERABILITY) ||
 				!player.inPosition())
 		{
 			return;
 		}
 		
 		// check for dash
 		if (((player.getDirection() == DIRECTION_NORMAL && ty > accelerometerDashThreshold) ||
 				(player.getDirection() == DIRECTION_REVERSE && ty < -accelerometerDashThreshold)) &&
 				player.canDash() && enableDash)
 		{
 			player.dash();
			return;
 		}
 	
 		// check if tx is not passed deadzone
 		if (Math.abs(tx) < accelerometerDeadzone || !enableMove) {
 			player.moveStop();
 			return;
 		}
 	
 		// set player direction
 		if (tx > 0) {
 			player.setDirX(-1);
 		} else {
 			tx = Math.abs(tx);
 			player.setDirX(1);
 		}
 		
 		// limit tx
 		if (tx > accelerometerMax) {
 			tx = accelerometerMax;
 		}
 	
 		// set player movement
 		float moveX = ((tx - accelerometerDeadzone)/accelerometerMax)*player.getMaxSpeed();
 		player.moveStart();
 		player.setSpeed(moveX);
 		
 		// check stay in place achievement
 		if (System.currentTimeMillis() - lastMoveTime > 1000*Achievements.LOCAL_OTHER_STAY_IN_PLACE_TIME) {
 			Achievements.unlockLocalAchievement(Achievements.localOtherStayInPlace);
 		}
 		lastMoveTime = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Toggles pausing of game
 	 * @param paused true if the game should be paused
 	 * @return true if pause succeeded
 	 */
 	public boolean togglePause(boolean paused) {
 		if (paused && gameStatus != STATUS_PAUSED) {
 			
 			gameStatus = STATUS_PAUSED;
 			System.out.println("Game paused");
 			
 			// cancel game over timer if player ship is breaking up
 			if (player.getStatus() == Player.STATUS_BREAKING_UP && gameOverTimer != null) {
 				gameOverTimer.cancel();
 			}
 			
 			// output runtime analysis
 			if (Debugging.runtimeAnalysis) {
 				
 				float averageUpdateTime = (1.f*runtimeAnalysisUpdateTime/runtimeAnalysisNumUpdates);
 				float averageDrawTime = (1.f*runtimeAnalysisDrawTime/runtimeAnalysisNumUpdates);
 				
 				Toast.makeText(gameActivity.getApplicationContext(), String.format("%.2f", averageUpdateTime) + "ms update, " + String.format("%.2f", averageDrawTime) + "ms draw", Toast.LENGTH_LONG).show();
 				System.out.println(String.format("%.2f", averageUpdateTime) + "ms update, " + String.format("%.2f", averageDrawTime) + "ms draw");
 			}
 			
 			pauseTime = System.currentTimeMillis();
 		} else if (!paused && gameStatus != STATUS_RUNNING) {
 			gameStatus = STATUS_RUNNING;
 			System.out.println("Game unpaused");
 			
 			// schedule game over timer if player ship is breaking up
 			if (player.getStatus() == Player.STATUS_BREAKING_UP) {
 				scheduleGameOver();
 			}
 			
 			// reset max sleep time
 			maxSleepTime = 2*1000/Options.frameRate;
 			
 			// reset paint flags
 			paint.reset();
 			if (Options.graphicsType == Options.GRAPHICS_NORMAL) {
 				paint.setAntiAlias(true);
 				paint.setDither(true);
 			}
 			
 			// reset player control type
 			player.setMoveByTouch(Options.controlType == Options.CONTROL_TOUCH);
 			
 			// reset player offset
 			player.resetOffsets(gameMode == GAME_MODE_INSTRUCTIONS);
 			
 			// redraw player to canvas
 			player.redraw();
 			
 			// update all frame based animations
 			updateFrameCounters();
 			
 			// update acceleromater values if using accelerometer controls
 			if (Options.controlType == Options.CONTROL_ACCELEROMETER) {
 				updateAccelerometerValues();
 			}
 			
 			if (pauseTime > 0) {
 				// reset all update times
 				resetUpdateTimes();
 			}
 			
 			// reset runtime analysis
 			if (Debugging.runtimeAnalysis) {
 				runtimeAnalysisUpdateTime = 0;
 				runtimeAnalysisDrawTime = 0;
 				runtimeAnalysisNumUpdates = 0;
 			}
 			
 			pauseTime = -1;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Returns true if game has been initialized
 	 * @return true if game has been initialized
 	 */
 	public boolean isInitialized() {
 		return initialized;
 	}
 	
 	/**
 	 * Toggles player movement
 	 * @param enableMove true if player is allowed to move horizontally
 	 */
 	public void toggleMove(boolean enableMove) {
 		this.enableMove = enableMove;
 	}
 	
 	/**
 	 * Toggles dash ability
 	 * @param enableDash true if player can dash
 	 */
 	public void toggleDash(boolean enableDash) {
 		this.enableDash = enableDash;
 		
 		player.toggleDash(enableDash);
 	}
 	
 	/**
 	 * Toggles player visibility
 	 * @param playerVisible true if player is visible
 	 */
 	public void togglePlayerVisible(boolean playerVisible) {
 		this.playerVisible = playerVisible;
 	}
 	
 	/**
 	 * Toggles drops
 	 * @param enableDrops true if drops happen
 	 */
 	public void toggleDrops(boolean enableDrops) {
 		this.enableDrops = enableDrops;
 	}
 	
 	/**
 	 * Adds asteroid to list of asteroids
 	 * @param asteroid asteroid to add
 	 */
 	public void addAsteroid(Asteroid asteroid) {
 		asteroids.add(asteroid);
 	}
 	
 	/**
 	 * Adds drop to list of drops
 	 * @param drop drop to add
 	 */
 	public void addDrop(Drop drop) {
 		drops.add(drop);
 	}
 	
 	/**
 	 * Sets player starting x coordinate
 	 * @param playerStartX starting x coordinate
 	 */
 	public void setPlayerStartX(float playerStartX) {
 		player.setX(playerStartX);
 		player.setGoX(playerStartX);
 	}
 
 	/**
 	 * Clears all asteroids
 	 */
 	public void clearAsteroids() {
 		asteroids.clear();
 	}
 	
 	/**
 	 * Clears all powerups
 	 */
 	public void clearPowerups() {
 		activePowerups.clear();
 	}
 	
 	/**
 	 * Clears all drops
 	 */
 	public void clearDrops() {
 		drops.clear();
 	}
 	
 	/**
 	 * Drops a powerup at the given position
 	 * @param x x coordinate
 	 * @param y y coordinate
 	 * @param type type of powerup, or -1 if random
 	 * @return powerup type
 	 */
 	public Drop dropPowerup(float x, float y, int type) {
 		
 		if (!enableDrops) {
 			return null;
 		}
 		
 		// create powerup
 		if (type < 0 || type >= availableDrops.size()) {
 			type = random.nextInt(availableDrops.size());
 		}
 		
 		int powerup = availableDrops.get(type);
 		
 		if (x == -1) {
 			x = random.nextInt(canvasWidth);
 		}
 		
 		// debug mode
 		if (Debugging.dropType != POWERUP_NONE) {
 			powerup = Debugging.dropType;
 		}
 		
 		int r_powerup = 0;
 		
 		switch(powerup) {
 		case POWERUP_SMALL:
 			r_powerup = R.drawable.icon_small;
 			break;
 		case POWERUP_SLOW:
 			r_powerup = R.drawable.icon_slow;
 			break;
 		case POWERUP_INVULNERABILITY:
 			r_powerup = R.drawable.icon_invulnerability;
 			break;
 		case POWERUP_DRILL:
 			r_powerup = R.drawable.icon_drill;
 			break;
 		case POWERUP_MAGNET:
 			r_powerup = R.drawable.icon_magnet;
 			break;
 		case POWERUP_BLACK_HOLE:
 			r_powerup = R.drawable.icon_black_hole;
 			break;
 		case POWERUP_BUMPER:
 			r_powerup = R.drawable.icon_bumper;
 			break;
 		case POWERUP_BOMB:
 			r_powerup = R.drawable.icon_bomb;
 			break;
 		}
 		
 		Drop drop= new Drop(BitmapFactory.decodeResource(gameActivity.getResources(), r_powerup), x, y, powerup);
 		
 		drops.add(drop);
 		
 		return drop;
 	}
 	
 	/**
 	 * Returns player's x coordinate
 	 * @return player's x coordinate
 	 */
 	public float getPlayerX() {
 		return player.getX();
 	}
 
 	/**
 	 * Resets player
 	 */
 	public void resetPlayer() {
 		
 		// add time to start time if paused
 		if (gameStatus == STATUS_PAUSED) {
 			player.addToStartTime(System.currentTimeMillis() - pauseTime);
 		}
 		
 		player.reset();
 	}
 
 	/**
 	 * Update asteroids
 	 */
 	private void updateAsteroids() {
 		
 		synchronized (asteroids) {
 		
 			Asteroid asteroid;
 			
 			// update asteroids
 			for(int i = 0; i < asteroids.size(); i++) {
 				asteroid = asteroids.get(i);
 				
 				// move asteroids
 				if (powerupSlow.isActive()) {
 					asteroid.update(0.5f);
 				} else {
 					asteroid.update();
 				}
 				
 				// reset asteroid off screen (include non-visible, non-intact asteroids here)
 				if ((direction == DIRECTION_NORMAL || asteroid.getDirY() < 0) && asteroid.getStatus() == Asteroid.STATUS_NORMAL && asteroid.getY() - asteroid.getHeight()/2 > canvasHeight) {
 					resetAsteroid(asteroid);
 				} else if ((direction == DIRECTION_REVERSE || asteroid.getDirY() < 0) && asteroid.getStatus() == Asteroid.STATUS_NORMAL && asteroid.getY() + asteroid.getHeight()/2 < 0) {
 					resetAsteroid(asteroid);
 				} 
 				// sides of screen too (magnet, black hole may affect asteroids)
 				else if (asteroid.getX() + asteroid.getWidth()/2 < 0 || asteroid.getX() - asteroid.getWidth()/2 > canvasWidth) {
 					resetAsteroid(asteroid);
 				}
 				
 				// reset asteroid that needs reset
 				if (asteroid.getStatus() == Asteroid.STATUS_NEEDS_RESET) {
 					resetAsteroid(asteroid);
 					continue;
 				}
 				
 				// don't do anything for asteroids not on screen
 				if (!asteroid.onScreen()) {
 					continue;
 				}
 				
 				// alter asteroid for each active powerup
 				synchronized (activePowerups) {
 					for (ActivePowerup activePowerup : activePowerups) {
 						activePowerup.alterAsteroid(asteroid);
 					}
 				}
 				
 				// only check collision with player when asteroid is in normal or held in place status
 				if (asteroid.getStatus() == Asteroid.STATUS_NORMAL || asteroid.getStatus() == Asteroid.STATUS_HELD_IN_PLACE) {
 					
 					// check collision with player
 					if (player.getStatus() == Player.STATUS_NORMAL && player.checkAsteroidCollision(asteroid)) {
 						
 						// player is on top or bottom
 						if (player.inPosition()) {
 							
 							// game over if player isn't invulnerability
 							if (!powerupInvulnerability.isActive() && !Debugging.godMode) {
 								asteroid.breakup();
 								
 								checkAchievements();
 								
 								player.breakup();
 								scheduleGameOver();
 							} else {
 								// increment pass through counter
 								powerupInvulnerability.passThrough(asteroid);
 							}
 						}
 						// player is dashing (only destroy asteroids when invulnerability has upgrade)
 						else if (!powerupInvulnerability.isActive() || powerupInvulnerability.isDasher()) {
 
 														// causes drop depending on upgrade
 							if (player.getDashNumAffectedAsteroids() == 0 ||
 									(player.getDashNumAffectedAsteroids() == 1 && player.getDashMultipleDrops()))
 							{
 								dropPowerup(asteroid.getX(), asteroid.getY(), -1);
 							}
 
 							// we handle normal vs held in place asteroids in the method
 							player.dashAffectedAsteroid(asteroid);
 							
 							asteroid.breakup();
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Updates drops
 	 */
 	private void updateDrops() {
 		
 		synchronized (drops) {
 			
 			Drop temp;
 			
 			// update falling powerups
 			for (int i = 0; i < drops.size(); i++) {
 				
 				temp = drops.get(i);
 				
 				if (powerupSlow.isActive() && !powerupSlow.isAffectingDropsAndPowerups()) {
 					temp.update(0.5f);
 				} else {
 					temp.update();
 				}
 				
 				// reset drop off screen
 				if ((direction == DIRECTION_NORMAL && temp.getY() - temp.getHeight()/2 > canvasHeight) || (direction == DIRECTION_REVERSE && temp.getY() + temp.getHeight()/2 < 0)) {
 					drops.remove(temp);
 					i--;
 				}
 				
 				// check collision with player
 				if (player.getStatus() == Player.STATUS_NORMAL && temp.isVisible() && temp.checkBoxCollision(player)) {
 					
 					switch(temp.getType()) {
 					case POWERUP_MAGNET:
 						activePowerups.add(new PowerupMagnet(BitmapFactory.decodeResource(gameActivity.getResources(), R.drawable.magnet), temp.getX(), temp.getY(), player.getDirection(), Upgrades.magnetUpgrade.getLevel()));
 						localStatistics.usesMagnet++;
 						break;
 					case POWERUP_BLACK_HOLE:
 						activePowerups.add(new PowerupBlackHole(BitmapFactory.decodeResource(gameActivity.getResources(), R.drawable.black_hole), BitmapFactory.decodeResource(gameActivity.getResources(), R.drawable.twinkle_large), temp.getX(), temp.getY(), Upgrades.blackHoleUpgrade.getLevel()));
 						localStatistics.usesBlackHole++;
 						break;
 					case POWERUP_DRILL:
 						activePowerups.add(new PowerupDrill(BitmapFactory.decodeResource(gameActivity.getResources(), R.drawable.drill), temp.getX(), temp.getY(), player.getDirection(), Upgrades.drillUpgrade.getLevel()));
 						localStatistics.usesDrill++;
 						break;
 					case POWERUP_BUMPER:
 						if (Upgrades.bumperUpgrade.getLevel() >= Upgrades.BUMPER_UPGRADE_INCREASED_SIZE) {
 							activePowerups.add(new PowerupBumper(BitmapFactory.decodeResource(gameActivity.getResources(), R.drawable.bumper_large), BitmapFactory.decodeResource(gameActivity.getResources(), R.drawable.bumper_large_alt), temp.getX(), temp.getY(), Upgrades.bumperUpgrade.getLevel()));
 						} else {
 							activePowerups.add(new PowerupBumper(BitmapFactory.decodeResource(gameActivity.getResources(), R.drawable.bumper), BitmapFactory.decodeResource(gameActivity.getResources(), R.drawable.bumper_alt), temp.getX(), temp.getY(), Upgrades.bumperUpgrade.getLevel()));
 						}
 						localStatistics.usesBumper++;
 						break;
 					case POWERUP_INVULNERABILITY:
 						powerupInvulnerability.activate();
 						localStatistics.usesInvulnerability++;
 						break;
 					case POWERUP_BOMB:
 						bombCounter = bombFrames;
 						localStatistics.usesBomb++;
 						break;
 					case POWERUP_SMALL:
 						powerupSmall.activate();
 						localStatistics.usesSmall++;
 						break;
 					case POWERUP_SLOW:
 						powerupSlow.activate();
 						localStatistics.usesSlow++;
 						break;
 					}
 					
 					drops.remove(temp);
 					i--;
 					
 					// check dash activate drop achievement
 					if (!player.inPosition()) {
 						Achievements.unlockLocalAchievement(Achievements.localDashActivateDrops);
 					}
 				} else {
 				
 					// alter falling powerup for each active powerup
 					synchronized (activePowerups) {
 						for (ActivePowerup activePowerup : activePowerups) {
 							if (activePowerup instanceof PowerupBumper) {
 								((PowerupBumper)activePowerup).alterItem(temp);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Update powerups
 	 */
 	private void updatePowerups() {
 		
 		// update inactive powerups
 		powerupInvulnerability.update();
 		powerupSlow.update();
 		powerupSmall.update();
 		
 		// check achievements
 		if (powerupSlow.isActive()) {
 			powerupSlow.checkAchievements();
 		}
 		if (powerupInvulnerability.isActive()) {
 			powerupInvulnerability.checkAchievements();
 		}
 		
 		synchronized (activePowerups) {
 		
 			ActivePowerup powerup;
 			
 			int blackHoleCount = 0;
 			
 			// update any active global powerups
 			for (int i = 0; i < activePowerups.size(); i++) {
 				
 				powerup = activePowerups.get(i);
 				
 				if (powerupSlow.isActive() && !powerupSlow.isAffectingDropsAndPowerups()) {
 					powerup.update(0.5f);
 				} else {
 					powerup.update(1f);
 				}
 				
 				// black hole specific stuff
 				if (powerup instanceof PowerupBlackHole) {
 					
 					blackHoleCount++;
 					
 					// check if we need to activate the quasar for the black hole
 					if (powerup.isFadingOut() && ((PowerupBlackHole)powerup).hasQuasar()) {
 						quasarCounter = quasarFrames;
 						activateQuasar();
 						((PowerupBlackHole)powerup).activateQuasar();
 					}
 				}
 				
 				// check if any active powerups should be removed
 				if (!activePowerups.get(i).isActive()) {
 										
 					// reset teleporting drill
 					if (powerup instanceof PowerupDrill && ((PowerupDrill)powerup).hasTeleport()) {
 						((PowerupDrill)powerup).teleport();
 					} else {
 						powerup.checkAchievements();
 						activePowerups.remove(i);
 						i--;
 					}
 				} else {
 				
 					// alter drill active powerups for each bumper active powerup
 					for (ActivePowerup activePowerup : activePowerups) {
 						if (activePowerup instanceof PowerupBumper && activePowerups.get(i) instanceof PowerupDrill) {
 							((PowerupBumper)activePowerup).alterDrill((PowerupDrill)activePowerups.get(i));
 						}
 					}
 				}
 			}
 			
 			// check black hole trifecta achievement
 			if (blackHoleCount > Achievements.LOCAL_BLACK_HOLE_TRIFECTA_NUM) {
 				Achievements.unlockLocalAchievement(Achievements.localBlackHoleTrifecta);
 			}
 		}
 	}
 	
 	/**
 	 * Update player
 	 */
 	private void updatePlayer() {
 		player.update();
 		
 		// check player bounds
 		if (player.getX() - player.getWidth()/2 < 0) {
 			player.setX(player.getWidth()/2);
 		}
 		
 		if (player.getX() + player.getWidth()/2 > canvasWidth) {
 			player.setX((canvasWidth - player.getWidth()/2));
 		}
 		
 		// check if player is in transition
 		if (direction != player.getDirection() || Math.abs(gravity - player.getGravity()) > 0.01) {
 			
 			// update gravity
 			gravity = player.getGravity();
 
 			// dash completed
 			if (player.inPosition()) {
 				direction = player.getDirection();
 				
 				updatesForGravityChange();
 			}
 		}
 		
 		// alter player for each active powerup
 		if (player.getStatus() == Player.STATUS_NORMAL && !powerupInvulnerability.isActive()) {
 			synchronized (activePowerups) {
 				for (ActivePowerup activePowerup : activePowerups) {
 					if (activePowerup instanceof PowerupBumper) {
 						if (((PowerupBumper)activePowerup).alterPlayer(player)) {
 							updatesForGravityChange();
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Activates bomb powerup, destroying everything on screen
 	 */
 	private void activateBomb() {
 		
 		int numAffectedAsteroids = 0;
 		int numDrops = 0;
 		
 		// destroy all on-screen asteroids
 		for (Asteroid asteroid : asteroids) {
 			
 			if (asteroid.onScreen()) {
 				asteroid.fadeOut(Asteroid.FADE_OUT_FROM_BOMB);
 				numAffectedAsteroids++;
 				
 				// cause drop if upgraded
 				if (Upgrades.bombUpgrade.getLevel() >= Upgrades.BOMB_UPGRADE_CAUSE_DROP) {
 					if (numDrops == 0 || (numDrops == 1 && Upgrades.bombUpgrade.getLevel() >= Upgrades.BOMB_UPGRADE_CAUSE_DROPS)) {
 						Drop drop = dropPowerup(asteroid.getX(), asteroid.getY(), -1);
 						
 						if (drop != null) {
 							numDrops++;
 						
 							// check bomb drop bomb achievement
 							if (drop.getType() == POWERUP_BOMB) {
 								Achievements.unlockLocalAchievement(Achievements.localBombDropBomb);
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		// check for achievements
 		if (numAffectedAsteroids >= Achievements.LOCAL_DESTROY_ASTEROIDS_BOMB_NUM_1 &&
 				!Achievements.localDestroyAsteroidsWithBomb1.getValue())
 		{
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithBomb1);
 		}
 		
 		if (numAffectedAsteroids >= Achievements.LOCAL_DESTROY_ASTEROIDS_BOMB_NUM_2 &&
 				!Achievements.localDestroyAsteroidsWithBomb2.getValue())
 		{
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithBomb2);
 		}
 		
 		if (numAffectedAsteroids >= Achievements.LOCAL_DESTROY_ASTEROIDS_BOMB_NUM_3 &&
 				!Achievements.localDestroyAsteroidsWithBomb3.getValue())
 		{
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithBomb3);
 		}
 		
 		// destroy all falling powerups
 		if (Upgrades.bombUpgrade.getLevel() < Upgrades.BOMB_UPGRADE_NO_EFFECT_DROPS) {
 			drops.clear();
 		}
 		
 		// destroy all active powerups
 		if (Upgrades.bombUpgrade.getLevel() < Upgrades.BOMB_UPGRADE_NO_EFFECT_POWERUPS) {
 			activePowerups.clear();
 		}
 	}
 	
 	/**
 	 * Activates quasar powerup, destroying all asteroids on screen
 	 */
 	private void activateQuasar() {
 		// destroy all on-screen asteroids
 		for (Asteroid asteroid : asteroids) {
 			if (asteroid.onScreen()) {
 				asteroid.fadeOut(Asteroid.FADE_OUT_FROM_QUASAR);
 			}
 		}
 	}
 	
 	/**
 	 * Resets given asteroid
 	 * @param asteroid asteroid to reset
 	 */
 	private void resetAsteroid(Asteroid asteroid) {
 		
 		if (gameMode == GAME_MODE_INSTRUCTIONS) {
 			return;
 		}
 		
 		float radius = level.getAsteroidRadiusFactorMin() + random.nextFloat()*level.getAsteroidRadiusFactorOffset();
 		float speed = level.getAsteroidSpeedFactorMin() + random.nextFloat()*level.getAsteroidSpeedFactorOffset();
 		
 		asteroid.reset(radius, speed, level.getAsteroidHorizontalMovementOffset());
 	}
 	
 	/**
 	 * Resets update times for player, asteroids, drops, and powerup
 	 */
 	private void resetUpdateTimes() {
 		
 		long timeDifference = System.currentTimeMillis() - pauseTime;
 
 		// update player start time to get accurate time
 		player.addToStartTime(timeDifference);
 
 		// update powerup times
 		for (ActivePowerup activePowerup : activePowerups) {
 			activePowerup.addTime(timeDifference);
 		}
 		
 		// update level start time
 		level.addToStartTime(timeDifference);
 		
 		// reset update times
 		player.resetUpdateTime();
 		
 		for (Asteroid asteroid : asteroids) {
 			asteroid.resetUpdateTime();
 		}
 		
 		for (Drop drop : drops) {
 			drop.resetUpdateTime();
 		}
 		
 		for (ActivePowerup activePowerup: activePowerups) {
 			activePowerup.resetUpdateTime();
 		}
 	}
 	
 	/**
 	 * Updates asteroids, drops, and powerups that are going in opposite direction
 	 * Used at the end of a dash for a smooth transition for stuff already going in new direction
 	 */
 	private void updatesForGravityChange() {
 		// update any asteroids, drops, powerups that are going in opposite direction
 		for (Asteroid asteroid : asteroids) {
 			if (asteroid.getDirY() < 0) {
 				asteroid.setDirY(-1*asteroid.getDirY());
 			}
 		}
 		
 		for (Drop drop : drops) {
 			if (drop.getDirY() < 0) {
 				drop.setDirY(-1*drop.getDirY());
 			}
 		}
 	}
 	
 	/**
 	 * Updates accelerometer values for accelerometer movement
 	 */
 	private void updateAccelerometerValues() {
 		switch (Options.accelerometerSensitivity) {
 		case Options.ACCELEROMETER_SENSITIVITY_LOW:
 			accelerometerDashThreshold = ACCELEROMETER_DASH_THRESHOLD_LOW;
 			accelerometerDeadzone = ACCELEROMETER_DEADZONE_LOW;
 			accelerometerMax = ACCELEROMETER_MAX_LOW;
 			break;
 		case Options.ACCELEROMETER_SENSITIVITY_NORMAL:
 			accelerometerDashThreshold = ACCELEROMETER_DASH_THRESHOLD_MEDIUM;
 			accelerometerDeadzone = ACCELEROMETER_DEADZONE_MEDIUM;
 			accelerometerMax = ACCELEROMETER_MAX_MEDIUM;
 			break;
 		default:
 			accelerometerDashThreshold = ACCELEROMETER_DASH_THRESHOLD_HIGH;
 			accelerometerDeadzone = ACCELEROMETER_DEADZONE_HIGH;
 			accelerometerMax = ACCELEROMETER_MAX_HIGH;
 			break;
 		}
 	}
 	
 	/**
 	 * Updates all frame-based counters based on frame rate Options
 	 */
 	private void updateFrameCounters() {
 		switch(Options.frameRate) {
 		case Options.FRAME_RATE_LOW:
 			bombFrames = 4;
 			quasarFrames = 8;
 			break;
 		case Options.FRAME_RATE_NORMAL:
 			bombFrames = 8;
 			quasarFrames = 16;
 			break;
 		case Options.FRAME_RATE_HIGH:
 		default:
 			bombFrames = 16;
 			quasarFrames = 32;
 			break;
 		}
 		
 		player.updateInvulnerabilityFrames();
 	}
 	
 	/**
 	 * Schedules gameOver call when player gets destroyed.
 	 */
 	private void scheduleGameOver() {
 		gameOverTimer = new Timer();
 		gameOverTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				// found here: http://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
 				gameActivity.runOnUiThread(new Runnable() {
 				     public void run() {
 				    	 gameActivity.gameOver();
 				    }
 				});
 			}
 		}, (gameMode == GAME_MODE_INSTRUCTIONS) ? 0 : player.getBreakupDuration()-500);
 
 	}
 	
 	/**
 	 * Checks any achievements that could be obtained on game over
 	 */
 	private void checkAchievements() {
 
 		// check stay in place achievement
 		if (System.currentTimeMillis() - lastMoveTime > 1000*Achievements.LOCAL_OTHER_STAY_IN_PLACE_TIME) {
 			Achievements.unlockLocalAchievement(Achievements.localOtherStayInPlace);
 		}
 	
 		// check slow achievement
 		if (powerupSlow.isActive()) {
 			powerupSlow.checkAchievements();
 		}
 		
 		// check achievements of any active powerups
 		for (ActivePowerup activePowerup : activePowerups) {
 			activePowerup.checkAchievements();
 		}
 	}
 
 }
