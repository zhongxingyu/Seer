 package com.slauson.dasher.game;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.slauson.dasher.objects.Asteroid;
 import com.slauson.dasher.objects.Drop;
 import com.slauson.dasher.objects.Player;
 import com.slauson.dasher.powerups.ActivePowerup;
 import com.slauson.dasher.powerups.PowerupBumper;
 import com.slauson.dasher.powerups.PowerupDrill;
 import com.slauson.dasher.powerups.PowerupMagnet;
 import com.slauson.dasher.powerups.PowerupSlow;
 import com.slauson.dasher.powerups.PowerupSmall;
 import com.slauson.dasher.powerups.PowerupInvulnerability;
 import com.slauson.dasher.powerups.PowerupBlackHole;
 import com.slauson.dasher.status.Achievements;
 import com.slauson.dasher.status.Configuration;
 import com.slauson.dasher.status.LocalStatistics;
 import com.slauson.dasher.status.Debugging;
 import com.slauson.dasher.status.Statistics;
 import com.slauson.dasher.status.Upgrades;
 import com.slauson.dasher.R;
 
 import android.content.Context;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.util.AttributeSet;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.Toast;
 
 /**
  * Main game view
  * 
  * Adapted from here: http://android-coding.blogspot.com/2012/01/create-surfaceview-game-step-by-step.html
  * 
  * @author Josh Slauson
  *
  */
 public class MyGameView extends SurfaceView implements SurfaceHolder.Callback {
 
 	/**
 	 * Private stuff
 	 */
 	
 	/** Holder for surface **/
 	private SurfaceHolder surfaceHolder;
 	
 	/** Game thread **/
 	private MyGameThread myGameThread;
 	
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
 	
 	/** Counter for bomb animation **/
 	private int bombCounter;
 	/** Counter for quasar animation **/
 	private int quasarCounter;
 	
 	/** Initialization flag for when surface is created **/
 	private boolean surfaceCreated = false;
 	/** Initialization flag for when game is initialized **/
 	private boolean initialized = false;
 	
 	/** Time of last touch down event, for double-tap based dashing **/
 	private long lastTouchDownTime1;
 	/** Time of second last touch down event, for double-tap based dashing **/
 	private long lastTouchDownTime2;
 	
 	// TODO: move to player object?
 	/** Time of last player movement **/
 	private long lastMoveTime;
 
 	/** Current level of game **/
 	private Level level;
 	
 	/** Game activity, used for transitioning to other activities **/
 	private MyGameActivity gameActivity = null;
 	
 	/** Local statistics, so we don't have to keep calling getInstance() **/
 	private Statistics localStatistics = null;
 	
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
 
 	
 	// runtime analysis stuff
 	private static long runtimeAnalysisUpdateTime = 0;
 	private static long runtimeAnalysisDrawTime = 0;
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
 	
 	/** Game mode id for game being paused **/
 	public static final int MODE_PAUSED = 0;
 	/** Game mode id for game running **/
 	public static final int MODE_RUNNING = 1;
 
 	/** Asteroids are going in normal direction (down) **/
 	public static final int DIRECTION_NORMAL = 0;
 	/** Asteroids are going in reverse direction (up) **/
 	public static final int DIRECTION_REVERSE = 1;
 
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
 	
 	/** Current game mode **/
 	public static int gameMode = MODE_RUNNING;
 	/** Current direction **/
 	public static int direction = DIRECTION_NORMAL;
 	/** Current gravity **/
 	public static float gravity = 1f;
 	
 	/** Random **/
 	public static Random random = new Random();
 	
 	/** Maximum sleep duration **/
 	public static int maxSleepTime = 1000/30;
 
 	
 	public MyGameView(Context context) {
 		super(context);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public MyGameView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public MyGameView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		// TODO Auto-generated constructor stub
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		canvasWidth = getWidth();
 		canvasHeight = getHeight();
 		
 		surfaceCreated = true;
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// TODO Auto-generated method stub
 	}
 	
 	/**
 	 * Sets up surface view and game thread
 	 */
 	public void MyGameSurfaceView_OnResume() {
 		
 		surfaceHolder = getHolder();
 		getHolder().addCallback(this);
 		
 		// Create and start background Thread
 		myGameThread = new MyGameThread(this);
 		myGameThread.setRunning(true);
 		myGameThread.start();
 		
 	}
 	
 	/**
 	 * Pauses game thread
 	 */
 	public void MyGameSurfaceView_OnPause() {
 		// Kill the background thread
 		boolean retry = true;
 		myGameThread.setRunning(false);
 		
 		while (retry) {
 			try {
 				myGameThread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		
 		// TODO: come up with a better way of doing this
 		if (!initialized) {
 			init();
 			return;
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
 		paint.setStrokeWidth(PLAYER_PAINT_STROKE_WIDTH);
 		paint.setColor(PLAYER_PAINT_COLOR);
 		player.draw(canvas, paint);
 		
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
 	}
 	
 	/**
 	 * Update surface view
 	 * @return true on success, false otherwise
 	 */
 	public boolean updateSurfaceView() {
 		
 		// the function run in background thread, not ui thread
 		if (!surfaceCreated) {
 			return false;
 		}
 		
 		Canvas canvas = null;
 
 		try {
 			canvas = surfaceHolder.lockCanvas();
 			
 			synchronized (surfaceHolder) {
 				
 				if (Debugging.runtimeAnalysis) {
 					long time1 = System.currentTimeMillis();
 					updateStates();
 					long time2 = System.currentTimeMillis();
 					onDraw(canvas);
 					long time3 = System.currentTimeMillis();
 					
 					runtimeAnalysisUpdateTime += (time2 - time1);
 					runtimeAnalysisDrawTime += (time3 - time2);
 					runtimeAnalysisNumUpdates++;
 				
 				} else {
 					updateStates();
 					onDraw(canvas);
 				}
 			}
 		} finally {
 			if (canvas != null) {
 				surfaceHolder.unlockCanvasAndPost(canvas);
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Initialize game view
 	 */
 	private void init() {
 		
 		// initialize stuff
 		if (surfaceCreated && !initialized) {
 			
 			System.out.println("MyGameView init()");
 			
 			level = new Level(Debugging.level, Debugging.levelProgression);
 			
 			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 			
 			asteroids = new ArrayList<Asteroid>();
 			drops = new LinkedList<Drop>();
 
 			// powerups
 			activePowerups = new LinkedList<ActivePowerup>();	
 			bombCounter = 0;
 			quasarCounter = 0;
 			
 			// populate available drops
 			availableDrops = new ArrayList<Integer>();
 			for (int i = 1; i <= NUM_POWERUPS; i++) {
 				if (Upgrades.getUpgrade(i).getLevel() >= Upgrades.POWERUP_UNLOCKED) {
 					availableDrops.add(i);
 				}
 			}
 			
 			// make sure we don't think the first single tap is a double tap
 			lastTouchDownTime1 = 0;
 			lastTouchDownTime2 = -2*DASH_DOUBLE_TAP_MIN_DURATION;
 			
 			lastMoveTime = 0;
 			pauseTime = -1;
 			
 			maxSleepTime = 1000/Configuration.frameRate;
 						
 			player = new Player(Configuration.controlType == Configuration.CONTROL_TOUCH);
 			
 			powerupSlow = new PowerupSlow(Upgrades.slowUpgrade.getLevel());
 			powerupInvulnerability = new PowerupInvulnerability(Upgrades.invulnerabilityUpgrade.getLevel());
 			powerupSmall = new PowerupSmall(Upgrades.smallUpgrade.getLevel());
 
 			updateAccelerometerValues();
 			updateFrameCounters();
 			
 			float radius, speed;
 			
 			for (int i = 0; i < level.getNumAsteroids(); i++) {
 				
 				radius = level.getAsteroidRadiusFactorMin() + random.nextFloat()*level.getAsteroidRadiusFactorOffset();
 				speed = level.getAsteroidSpeedFactorMin() + random.nextFloat()*level.getAsteroidSpeedFactorOffset();
 				
 				asteroids.add(new Asteroid(radius, speed, level.getAsteroidRadiusFactorMax(), level.getAsteroidHorizontalMovementOffset()));
 			}
 			
 			localStatistics = LocalStatistics.getInstance();
 			
 			initialized = true;
 		}
 	}
 	
 	/**
 	 * Sets activity of this view
 	 * @param gameActivity activity
 	 */
 	public void setActivity(MyGameActivity gameActivity) {
 		this.gameActivity = gameActivity;
 	}
 	
 	/**
 	 * Update asteroids
 	 */
 	public void updateAsteroids() {
 		
 		synchronized (asteroids) {
 		
 			Asteroid temp;
 			
 			// update asteroids
 			for(int i = 0; i < asteroids.size(); i++) {
 				temp = asteroids.get(i);
 				
 				// move asteroids
 				if (powerupSlow.isActive()) {
 					temp.update(0.5f);
 				} else {
 					temp.update();
 				}
 				
 				// reset asteroid off screen (include non-visible, non-intact asteroids here)
 				if (direction == DIRECTION_NORMAL && temp.getStatus() == Asteroid.STATUS_NORMAL && temp.getY() - temp.getHeight()/2 > canvasHeight) {
 					resetAsteroid(temp);
 				} else if (direction == DIRECTION_REVERSE && temp.getStatus() == Asteroid.STATUS_NORMAL && temp.getY() + temp.getHeight()/2 < 0) {
 					resetAsteroid(temp);
 				}
 				
 				// reset asteroid that needs reset
 				if (temp.getStatus() == Asteroid.STATUS_NEEDS_RESET) {
 					resetAsteroid(temp);
 					continue;
 				}
 				
 				// don't do anything for asteroids not on screen
 				if (!temp.onScreen()) {
 					continue;
 				}
 				
 				// alter asteroid for each active powerup
 				synchronized (activePowerups) {
 					for (ActivePowerup activePowerup : activePowerups) {
 						activePowerup.alterAsteroid(temp);
 					}
 				}
 				
 				// only check collision with player when asteroid is in normal or held in place status
 				if (temp.getStatus() == Asteroid.STATUS_NORMAL || temp.getStatus() == Asteroid.STATUS_HELD_IN_PLACE) {
 					
 					// check collision with player
 					if (player.getStatus() == Player.STATUS_NORMAL && player.checkAsteroidCollision(temp)) {
 						
 						// player is on top or bottom
 						if (player.inPosition()) {
 							
 							// game over if player isn't invulnerability
 							if (!powerupInvulnerability.isActive() && !Debugging.godMode) {
 								temp.breakup();
 								
 								player.breakup();
 								Timer timer = new Timer();
 								timer.schedule(new TimerTask() {
 									@Override
 									public void run() {
 										// found here: http://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
 										gameActivity.runOnUiThread(new Runnable() {
 										     public void run() {
 										    	 gameActivity.gameOver();
 										    }
 										});
 									}
 								}, player.getBreakupDuration()-500);
 							} else {
 								// increment pass through counter
 								powerupInvulnerability.passThrough();
 							}
 						}
 						// player is dashing (only destroy asteroids when invulnerability has upgrade)
 						else if (!powerupInvulnerability.isActive() || powerupInvulnerability.isDasher()) {
 
 														// causes drop depending on upgrade
 							if (player.getDashNumAffectedAsteroids() == 0 ||
 									(player.getDashNumAffectedAsteroids() == 1 && player.getDashMultipleDrops()))
 							{
 								dropPowerup(temp.getX(), temp.getY());
 							}
 
 							// we handle normal vs held in place asteroids in the method
 							player.dashAffectedAsteroid(temp);
 							
 							temp.breakup();
 
 							// check small dash destroy achievement
 							if (powerupSmall.isActive()) {
 								Achievements.unlockLocalAchievement(Achievements.localSmallDashDestroy);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Updates drops
 	 */
 	public void updateDrops() {
 		
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
 						activePowerups.add(new PowerupMagnet(BitmapFactory.decodeResource(getResources(), R.drawable.magnet), temp.getX(), temp.getY(), player.getDirection(), Upgrades.magnetUpgrade.getLevel()));
 						localStatistics.usesMagnet++;
 						break;
 					case POWERUP_BLACK_HOLE:
 						activePowerups.add(new PowerupBlackHole(BitmapFactory.decodeResource(getResources(), R.drawable.black_hole), BitmapFactory.decodeResource(getResources(), R.drawable.twinkle_large), temp.getX(), temp.getY(), Upgrades.blackHoleUpgrade.getLevel()));
 						localStatistics.usesBlackHole++;
 						break;
 					case POWERUP_DRILL:
 						activePowerups.add(new PowerupDrill(BitmapFactory.decodeResource(getResources(), R.drawable.drill), temp.getX(), temp.getY(), player.getDirection(), Upgrades.drillUpgrade.getLevel()));
 						localStatistics.usesDrill++;
 						break;
 					case POWERUP_BUMPER:
 						if (Upgrades.bumperUpgrade.getLevel() >= Upgrades.BUMPER_UPGRADE_INCREASED_SIZE) {
 							activePowerups.add(new PowerupBumper(BitmapFactory.decodeResource(getResources(), R.drawable.bumper_large), BitmapFactory.decodeResource(getResources(), R.drawable.bumper_large_alt), temp.getX(), temp.getY(), Upgrades.bumperUpgrade.getLevel()));
 						} else {
 							activePowerups.add(new PowerupBumper(BitmapFactory.decodeResource(getResources(), R.drawable.bumper), BitmapFactory.decodeResource(getResources(), R.drawable.bumper_alt), temp.getX(), temp.getY(), Upgrades.bumperUpgrade.getLevel()));
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
 							// TODO: use something better here
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
 	public void updatePowerups() {
 		
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
 						// TODO: use something better here
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
 	public void updatePlayer() {
 		player.update();
 		
 		//System.out.println("updatePlayer(): " + direction + " - " + player.getDirection() + ", " + gravity + " - " + player.getGravity());
 		
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
 					// TODO: use something better here
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
 	 * Update game state
 	 */
 	public void updateStates() {
 		
 		if (!initialized) {
 			init();
 			return;
 		}
 		
 		if (gameMode == MODE_PAUSED) {
 			return;
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
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		
 		if (!initialized) {
 			init();
 			return false;
 		}
 		
 		// only move when touch controls are being used and when ship in normal or invulnerability status
 		if (Configuration.controlType != Configuration.CONTROL_TOUCH || (player.getStatus() != Player.STATUS_NORMAL && player.getStatus() != Player.STATUS_INVULNERABILITY)) {
 			return false;
 		}
 		
 		// get event position
 		float x = event.getX();
 		float y = event.getY();
 		
 		int action = event.getAction();
 		
 		switch(action) {
 		case MotionEvent.ACTION_DOWN:
 			// check if pressed on player's position, then dash
 			if (x < player.getX() + player.getWidth()*DASH_TOUCH_FACTOR/2 && x > player.getX() - player.getWidth()*DASH_TOUCH_FACTOR/2 &&
 					y < player.getY() + player.getHeight()*DASH_TOUCH_FACTOR/2 && y > player.getY() - player.getHeight()*DASH_TOUCH_FACTOR/2 &&
 					player.canDash())
 			{
 				player.dash();
 				break;
 			}
 			
 			lastTouchDownTime2 = lastTouchDownTime1;
 			lastTouchDownTime1 = System.currentTimeMillis();
 			
 			// check stay in place achievement
 			if (System.currentTimeMillis() - lastMoveTime > Achievements.LOCAL_OTHER_STAY_IN_PLACE_TIME) {
 				Achievements.unlockLocalAchievement(Achievements.localOtherStayInPlace);
 			}
 			
 			// don't break here so that player still moves
 		case MotionEvent.ACTION_MOVE:
 			// only move horizontally when player is in position
 			if (player.inPosition()) {
 				player.setGoX(x);
 			}
 			
 			break;
 		case MotionEvent.ACTION_UP:
 			// dash based on double tap
 			if (player.getStatus() == Player.STATUS_NORMAL && player.canDash()) {
 				if (lastTouchDownTime1 - lastTouchDownTime2 < DASH_DOUBLE_TAP_MIN_DURATION && System.currentTimeMillis() - lastTouchDownTime2 < DASH_DOUBLE_TAP_MIN_DURATION) {
 					player.dash();
 				}
 			}
 			
 			lastMoveTime = System.currentTimeMillis();
 		default:
 			break;
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Handle key down events, this is called from game activity
 	 * @param keyCode
 	 * @param event
 	 */
 	public void keyDown(int keyCode, KeyEvent event) {
 		
 		if (!initialized) {
 			init();
 			return;
 		}
 
 		// only move when keyboard controls are being used and when ship in normal or invulnerability status
 		if (Configuration.controlType != Configuration.CONTROL_KEYBOARD ||
 				(player.getStatus() != Player.STATUS_NORMAL && player.getStatus() != Player.STATUS_INVULNERABILITY) ||
 				!player.inPosition())
 		{
 			return;
 		}
 
 		switch(keyCode) {
 		// left
 		case KeyEvent.KEYCODE_DPAD_LEFT:
 		case KeyEvent.KEYCODE_S:
 			player.moveLeft();
 				
 			// check stay in place achievement
 			if (System.currentTimeMillis() - lastMoveTime > Achievements.LOCAL_OTHER_STAY_IN_PLACE_TIME) {
 				Achievements.unlockLocalAchievement(Achievements.localOtherStayInPlace);
 			}
 			lastMoveTime = System.currentTimeMillis();
 			break;		
 		// right
 		case KeyEvent.KEYCODE_DPAD_RIGHT:
 		case KeyEvent.KEYCODE_L:
 			player.moveRight();
 				
 			// check stay in place achievement
 			if (System.currentTimeMillis() - lastMoveTime > Achievements.LOCAL_OTHER_STAY_IN_PLACE_TIME) {
 				Achievements.unlockLocalAchievement(Achievements.localOtherStayInPlace);
 			}
 			lastMoveTime = System.currentTimeMillis();
 			break;
 		}
 	}
 	
 	/**
 	 * Handle key down events, this is called from game activity
 	 * @param keyCode
 	 * @param event
 	 */
 	public void keyUp(int keyCode, KeyEvent event) {
 		
 		if (!initialized) {
 			init();
 			return;
 		}
 
 		// only move player ship when its in normal or invulnerability status
 		if (Configuration.controlType != Configuration.CONTROL_KEYBOARD ||
 				(player.getStatus() != Player.STATUS_NORMAL && player.getStatus() != Player.STATUS_INVULNERABILITY) ||
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
 			if (player.canDash()) {
 				player.dash();
 			}
 			break;
 		}
 	}
 	
 	/**
 	 * Handle accelerometer, this is called from game activity
 	 * @param tx x factor
 	 * @param ty y factor
 	 */
 	public void updateAccelerometer(float tx, float ty) {
 		
 		if (!initialized) {
 			init();
 			return;
 		}
 		
 		// only move when accelerometer controls are being used or when ship in normal or invulnerability status
 		if (Configuration.controlType != Configuration.CONTROL_ACCELEROMETER ||
 				(player.getStatus() != Player.STATUS_NORMAL && player.getStatus() != Player.STATUS_INVULNERABILITY) ||
 				!player.inPosition())
 		{
 			return;
 		}
 		
 		// check for dash
 		if (((player.getDirection() == DIRECTION_NORMAL && ty > accelerometerDashThreshold) ||
 				(player.getDirection() == DIRECTION_REVERSE && ty < -accelerometerDashThreshold)) &&
 				player.canDash())
 		{
 			player.dash();
 		}
 
 		// check if tx is not passed deadzone
 		if (Math.abs(tx) < accelerometerDeadzone) {
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
 		if (System.currentTimeMillis() - lastMoveTime > Achievements.LOCAL_OTHER_STAY_IN_PLACE_TIME) {
 			Achievements.unlockLocalAchievement(Achievements.localOtherStayInPlace);
 		}
 		lastMoveTime = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Toggles pause state of game
 	 * @param paused true if game should be paused
 	 * @return true if game is actually paused
 	 */
 	public boolean togglePause(boolean paused) {
 		
 		// no pausing when player ship is breaking up
 		if (player.getStatus() == Player.STATUS_BREAKING_UP) {
 			return false;
 		}
 		
 		if (paused) {
 			gameMode = MODE_PAUSED;
 			
 			// output runtime analysis
 			if (Debugging.runtimeAnalysis) {
 				
 				float averageUpdateTime = (1.f*runtimeAnalysisUpdateTime/runtimeAnalysisNumUpdates);
 				float averageDrawTime = (1.f*runtimeAnalysisDrawTime/runtimeAnalysisNumUpdates);
 				
 				Toast.makeText(getContext(), String.format("%.2f", averageUpdateTime) + "ms update, " + String.format("%.2f", averageDrawTime) + "ms draw", Toast.LENGTH_LONG).show();
 				System.out.println(String.format("%.2f", averageUpdateTime) + "ms update, " + String.format("%.2f", averageDrawTime) + "ms draw");
 			}
 			
 			pauseTime = System.currentTimeMillis();
 		} else {
 			gameMode = MODE_RUNNING;
 			
 			// reset max sleep time
 			maxSleepTime = 2*1000/Configuration.frameRate;
 			
 			// reset player control type
 			player.setMoveByTouch(Configuration.controlType == Configuration.CONTROL_TOUCH);
 			
 			// update all frame based animations
 			updateFrameCounters();
 			
 			// update acceleromater values if using accelerometer controls
 			if (Configuration.controlType == Configuration.CONTROL_ACCELEROMETER) {
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
 	 * May drop a random powerup at the given position
 	 * @param x x coordinate
 	 * @param y y coordinate
 	 * @return powerup type
 	 */
 	private int dropPowerup(float x, float y) {
 		// create powerup
 		int index = random.nextInt(availableDrops.size());
 		
 		int powerup = availableDrops.get(index);
 		
 		// debug mode
 		if (Debugging.dropType != POWERUP_NONE) {
 			powerup = Debugging.dropType;
 		}
 		
 		int r_powerup = 0;
 		
 		switch(powerup) {
 		case POWERUP_SMALL:
 			r_powerup = R.drawable.powerup_ship;
 			break;
 		case POWERUP_SLOW:
			r_powerup = R.drawable.powerup_slow;
 			break;
 		case POWERUP_INVULNERABILITY:
 			r_powerup = R.drawable.powerup_invulnerability;
 			break;
 		case POWERUP_DRILL:
 			r_powerup = R.drawable.powerup_drill;
 			break;
 		case POWERUP_MAGNET:
 			r_powerup = R.drawable.powerup_magnet;
 			break;
 		case POWERUP_BLACK_HOLE:
 			r_powerup = R.drawable.powerup_black_hole;
 			break;
 		case POWERUP_BUMPER:
 			r_powerup = R.drawable.powerup_bumper;
 			break;
 		case POWERUP_BOMB:
 			r_powerup = R.drawable.powerup_bomb;
 			break;
 		}
 		
 		Drop drop= new Drop(BitmapFactory.decodeResource(getResources(), r_powerup), x, y, powerup);
 		
 		drops.add(drop);
 		
 		return powerup;
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
 						int powerupType = dropPowerup(asteroid.getX(), asteroid.getY());
 						numDrops++;
 						
 						// check bomb drop bomb achievement
 						if (powerupType == POWERUP_BOMB) {
 							Achievements.unlockLocalAchievement(Achievements.localBombDropBomb);
 						}
 					}
 				}
 			}
 		}
 		
 		// check for achievements
 		if (numAffectedAsteroids > Achievements.LOCAL_DESTROY_ASTEROIDS_NUM_1 &&
 				!Achievements.localDestroyAsteroidsWithBomb1.getValue())
 		{
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithBomb1);
 		}
 		
 		if (numAffectedAsteroids > Achievements.LOCAL_DESTROY_ASTEROIDS_NUM_2 &&
 				!Achievements.localDestroyAsteroidsWithBomb2.getValue())
 		{
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithBomb2);
 		}
 		
 		if (numAffectedAsteroids > Achievements.LOCAL_DESTROY_ASTEROIDS_NUM_3 &&
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
 		float radius = level.getAsteroidRadiusFactorMin() + random.nextFloat()*level.getAsteroidRadiusFactorOffset();
 		float speed = level.getAsteroidSpeedFactorMin() + random.nextFloat()*level.getAsteroidSpeedFactorOffset();
 		
 		asteroid.reset(radius, speed, level.getAsteroidHorizontalMovementOffset());
 	}
 	
 	/**
 	 * Resets game state
 	 */
 	public void reset() {
 		level.reset();
 		resetAsteroids();
 		resetUpdateTimes();
 		
 		activePowerups.clear();
 		drops.clear();
 	}
 	
 	/**
 	 * Resets all asteroids
 	 */
 	private void resetAsteroids() {
 		int numAsteroids = level.getNumAsteroids();
 		
 		while (asteroids.size() > numAsteroids) {
 			asteroids.remove(asteroids.size()-1);
 		}
 		
 		for (Asteroid asteroid : asteroids) {
 			resetAsteroid(asteroid);
 		}
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
 		
 		for (ActivePowerup activePowerup : activePowerups) {
 			if (activePowerup instanceof PowerupDrill && activePowerup.getDirY() < 0) {
 				((PowerupDrill)activePowerup).switchDirection();
 			}
 		}
 	}
 	
 	/**
 	 * Updates accelerometer values for accelerometer movement
 	 */
 	private void updateAccelerometerValues() {
 		switch (Configuration.accelerometerSensitivity) {
 		case Configuration.ACCELEROMETER_SENSITIVITY_LOW:
 			accelerometerDashThreshold = ACCELEROMETER_DASH_THRESHOLD_LOW;
 			accelerometerDeadzone = ACCELEROMETER_DEADZONE_LOW;
 			accelerometerMax = ACCELEROMETER_MAX_LOW;
 			break;
 		case Configuration.ACCELEROMETER_SENSITIVITY_MEDIUM:
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
 	
 	private void updateFrameCounters() {
 		switch(Configuration.frameRate) {
 		case Configuration.FRAME_RATE_LOW:
 			bombFrames = 4;
 			quasarFrames = 8;
 			break;
 		case Configuration.FRAME_RATE_NORMAL:
 			bombFrames = 8;
 			quasarFrames = 16;
 			break;
 		case Configuration.FRAME_RATE_HIGH:
 		default:
 			bombFrames = 16;
 			quasarFrames = 32;
 			break;
 		}
 		
 		player.updateInvulnerabilityFrames();
 	}
 }
