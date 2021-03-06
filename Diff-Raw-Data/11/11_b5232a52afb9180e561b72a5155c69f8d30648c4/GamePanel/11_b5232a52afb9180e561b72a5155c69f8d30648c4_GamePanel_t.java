 package com.chalmers.game.td;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.chalmers.game.td.units.Tower;
 import com.chalmers.game.td.R;
 
 import com.chalmers.game.td.units.AirTower;
 import com.chalmers.game.td.units.BasicTower;
 import com.chalmers.game.td.units.Mob;
 import com.chalmers.game.td.units.Projectile;
 import com.chalmers.game.td.units.SlowTower;
 import com.chalmers.game.td.units.Snowball;
 import com.chalmers.game.td.units.SplashTower;
 import com.chalmers.game.td.units.Mob.MobType;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.SoundPool;
 import android.os.Vibrator;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 
 /**
  * This is a view that displays the entire game board. The onDraw method draws
  * all towers, mobs, projectiles and the map itself. This information is taken
  * from the GameModel.
  * 
  * In Android development, the "View" also handles user input, so this is where
  * we take care of all events that the user generates on the game field.
  * 
  * @author Fredrik Persson
  * @author Jonas Andersson
  * @author Ahmed Chaban
  * @author Jonas Wallander
  * @author Disa Faith
  * @author Daniel Arvidsson
  */
 public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
 
 	private static final int STATE_RUNNING = 1;
 	private static final int STATE_GAMEOVER = 2;
 	private static final int STATE_WIN = 3;
 	private static final int STATE_PAUSED = 4;
 
 	private int GAME_STATE = STATE_RUNNING;
 
 	/** Thread which contains our game loop. */
 	private GameThread mGameThread;
 
 	private MobFactory	mMobFactory;
 
 	/** Cache variable for all used images. */
 	private Map<Integer, Bitmap> mBitMapCache = new HashMap<Integer, Bitmap>();
 
 
 	/** Current x and y cord. for the touched tower. */
 	private int mTx;
 	private int mTy;
 	
 	private boolean fastf = false;
 
 	private Tower mCurrentTower;
 	private Tower mSelectedTower;
 	private Snowball mCurrentSnowball;
 
 	private int mWateranimation = 0;
 	private boolean mSplash = false;
 	
 	private int menuPic = 0;
 
 
 	/** Keeps track of the delay between creation of Mobs in waves */
 	private static final int MOB_DELAY_MAX = 30;
 	private int mMobDelayI = 0;
 
 	// Graphic elements used in the GUI
 	private static final RectF sBtnSell = new RectF(90,180,150,230);
 	private static final RectF sBtnUpgrade = new RectF(165,180,300,230);
 	private static final RectF sTransparentBox = new RectF(70,50,320,240);
 	private static final RectF sBtn1 = new RectF(420,15,475,65);
 	private static final RectF sBtn2 = new RectF(420,15+60,475,65+60);
 	private static final RectF sBtn3 = new RectF(420,15+120,475,65+120);
 	private static final RectF sBtn4 = new RectF(420,15+180,475,65+180);
 	private static final RectF sBtn5 = new RectF(420,15+240,475,65+240);
 
 	// Paints
 	private static final Paint sPaintBtnBox = new Paint();
 	private static final Paint sPaintBoxText = new Paint();
 	private static final Paint sPaintBoxRed = new Paint();
 	private static final Paint sPaintBoxGreen = new Paint();
 	private static final Paint sPaintLine = new Paint();
 	private static final Paint sPaintTransparentBox = new Paint();
 	private static final Paint sPaintText = new Paint();
 	private static final Paint sPaintTextWhite = new Paint();
 	private static final Paint sPaintTextBlack = new Paint();
 	private static final Paint rangeIndicationPaint = new Paint();
 	private static final Paint noRangeIndicationPaint = new Paint();
 	private static final Paint gridpaint = new Paint();
 	private static final Paint healthBarPaint = new Paint();
 	private static final Paint boxTextPaintTitle = new Paint();
 	private static final Paint snowPaint = new Paint();
 	private static final Paint borderPaint = new Paint();
 	private static final Paint mBtnPaint = new Paint();
 	private static final Paint sMoneyAfterDead = new Paint();
 	private static final Paint sMoneyAfterDeadBg = new Paint();
 	
 	/** Debug */
 	private TDDebug debug;    
 
 	private static int GAME_SPEED_MULTIPLIER = 1;
 	// accelerometer stuff
 	private SensorManager mSensorManager;
 	private SensorEvent mLatestSensorEvent;
 
 	// use this to make the phone vibrate. vibrator.vibrate(int time);
 	private Vibrator mVibrator;
 
 	private boolean mAccelerometerSupported;
 	private boolean mShowTooltip;
 	private boolean mAllowBuild;
 	
 	private Tower mTower1 = new BasicTower(0,0);
 	private Tower mTower2 = new SplashTower(0,0);
 	private Tower mTower3 = new SlowTower(0,0);
 	private Tower mTower4 = new AirTower(0,0);
 	
 	private static final int mSnowballTreshold = 1500;
 	private int mUsedSnowballs;
 	
 	private AudioManager mAudioManager;
 	private static SoundPool sounds;
 	
 	private static int explosionSound;
 	private static MediaPlayer fastMusic,mainMusic;
 
 	
 	public static void loadSound(Context context) {
 //	    sound = SilhouPreferences.sound(context); // should there be sound?
 	    sounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
 	    // three ref. to the sounds I need in the application
 	    explosionSound = sounds.load(context, R.raw.explosion, 1);
 	    // the music that is played at the beginning and when there is only 10 seconds left in a game
 	    fastMusic = MediaPlayer.create(context, R.raw.doom_1);
 	    mainMusic = MediaPlayer.create(context, R.raw.main);
 	}
 	
 	public static void playSound(int file) {
 //	    if (!sound) return; // if sound is turned off no need to continue
 	    sounds.play(file, 1, 1, 1, 0, 1);
 	}
 	
 	public void updateSounds() {
 		switch (GAME_STATE) {
 		case STATE_RUNNING:
 			
 			try {
 				if (GameModel.sMusicEnabled) {
 					if (fastf) {
 						pauseMusic(mainMusic);
 						playMusic(fastMusic);
 					} else {
 						pauseMusic(fastMusic);
 						playMusic(mainMusic);
 					}
 				}
 			} catch (IllegalStateException ise) {
 				loadSound(getContext());
 			}
 			break;
 		default:
 			pauseMusic(fastMusic);
 			pauseMusic(mainMusic);
 			break;
 		}
 	}
 	
 	public static final void playMusic(MediaPlayer file) {
 	    if (!file.isPlaying()) {
 	    file.seekTo(0);
 	    file.start();
 	    }
 	}
 	
 	public static final void pauseMusic(MediaPlayer file) {
 //	    if (!sound) return;
 	    if (file.isPlaying()) file.pause();
 	}
 	
 	public static final void releaseSounds() {
 //	    if (!soundEnabled) return;
 		if (sounds == null)
 			return;
 	    sounds.release();
 	    if (fastMusic.isPlaying() && fastMusic != null) {
 	    	fastMusic.stop();
 	    	fastMusic.release();
 	    }
 	    if (mainMusic.isPlaying() && mainMusic != null) {
 	    	mainMusic.stop();
 	    	mainMusic.release();
 	    }
 	}
 
 	
 	/**
 	 * Constructor called on instantiation.
 	 * @param context Context of calling activity.
 	 */
 	public GamePanel(Context context) {
 
 		super(context);		
 		
 		// makes sure the screen can't turn off while playing
 		setKeepScreenOn(true);
 
 
 		debug = new TDDebug();
 		debug.InitGameTime();
 
 		startTrack(GameModel.getTrack());
 		
 
 		loadSound(context);
 
 		
 		fillBitmapCache();
 		getHolder().addCallback(this);
 		mGameThread = new GameThread(this);
 		setFocusable(true);
 		setFocusableInTouchMode(true);
 		requestFocus();
 		// do settings to all paint objects used in the GUI
 		setupPaint();
 
 		mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
 		
 		// get a reference to the vibrator in the phone
 		mVibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
 
 		// start listening to accelerometer events
 		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
 		mAccelerometerSupported = !mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).isEmpty();
 
 		if (mAccelerometerSupported)
 			mSensorManager.registerListener(mAccelerometerListener,
 					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 					SensorManager.SENSOR_DELAY_FASTEST);
 
 		// start listening to telephone events (incoming calls etc)
 		((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
 		.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
 
 	}
 
 	/**
 	 * Resets the gamefield to the given track
 	 */
 	private void startTrack(int track){
 		mMobFactory = MobFactory.getInstance(); 
 		mMobFactory.setContext(getContext()); 
 		GameModel.setTrack(track);
 		GameModel.initialize(getContext());
 		GameModel.currentPlayer.setCurrentScore(0);
 		Path.getInstance().setTrackPath(track);
 		fastf = false;
 		setSpeedMultiplier(1);
 	}
 
 	/**
 	 * Handles accelerometer events
 	 */
 	private SensorEventListener mAccelerometerListener = new SensorEventListener() {
 
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
 
 		public void onSensorChanged(SensorEvent event) {
 			mLatestSensorEvent = event;
 		}
 	};
 
 	/**
 	 * Handles telephony events
 	 */
 	private PhoneStateListener mPhoneListener = new PhoneStateListener() {
 
 		@Override 
 		public void onCallStateChanged(int state, String incomingNumber) { 
 			super.onCallStateChanged(state, incomingNumber); 
 
 			switch (state){ 
 			case TelephonyManager.CALL_STATE_RINGING: 
 				Log.d("PhoneState", "ringing"); 
 				// TODO handle incoming calls
 				// maybe pause and such and such
 				break; 
 
 			case TelephonyManager.CALL_STATE_IDLE: 
 				Log.d("PhoneState", "idle"); 
 				break; 
 
 			case TelephonyManager.CALL_STATE_OFFHOOK : 
 				Log.d("PhoneState", "offhook"); 
 				break;
 			}
 		}
 	};
 	
 
 	/**
 	 * Fill the bitmap cache.
 	 */
 	private void fillBitmapCache() {
 		mBitMapCache.put(R.drawable.icon, BitmapFactory.decodeResource(getResources(), R.drawable.icon));
 		mBitMapCache.put(R.drawable.abstrakt, BitmapFactory.decodeResource(getResources(), R.drawable.abstrakt));
 		mBitMapCache.put(R.drawable.wallpaper, BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper));
 		mBitMapCache.put(R.drawable.snowball_small, BitmapFactory.decodeResource(getResources(), R.drawable.snowball_small));
 		mBitMapCache.put(R.drawable.snowball, BitmapFactory.decodeResource(getResources(), R.drawable.snowball));
 		mBitMapCache.put(R.drawable.paper, BitmapFactory.decodeResource(getResources(), R.drawable.paper));
 		mBitMapCache.put(R.drawable.basictower, BitmapFactory.decodeResource(getResources(), R.drawable.basictower));
 		mBitMapCache.put(R.drawable.basictower2, BitmapFactory.decodeResource(getResources(), R.drawable.basictower2));
 		mBitMapCache.put(R.drawable.basictower3, BitmapFactory.decodeResource(getResources(), R.drawable.basictower3));
 		mBitMapCache.put(R.drawable.basictower4, BitmapFactory.decodeResource(getResources(), R.drawable.basictower4));
 		mBitMapCache.put(R.drawable.splashtower, BitmapFactory.decodeResource(getResources(), R.drawable.splashtower));
 		mBitMapCache.put(R.drawable.splashtower2, BitmapFactory.decodeResource(getResources(), R.drawable.splashtower2));
 		mBitMapCache.put(R.drawable.splashtower3, BitmapFactory.decodeResource(getResources(), R.drawable.splashtower3));
 		mBitMapCache.put(R.drawable.splashtower4, BitmapFactory.decodeResource(getResources(), R.drawable.splashtower4));
 		mBitMapCache.put(R.drawable.slowtower, BitmapFactory.decodeResource(getResources(), R.drawable.slowtower));
 		mBitMapCache.put(R.drawable.slowtower2, BitmapFactory.decodeResource(getResources(), R.drawable.slowtower2));
 		mBitMapCache.put(R.drawable.slowtower3, BitmapFactory.decodeResource(getResources(), R.drawable.slowtower3));
 		mBitMapCache.put(R.drawable.slowtower4, BitmapFactory.decodeResource(getResources(), R.drawable.slowtower4));
 		mBitMapCache.put(R.drawable.smaller, BitmapFactory.decodeResource(getResources(), R.drawable.smaller));
 		mBitMapCache.put(R.drawable.small, BitmapFactory.decodeResource(getResources(), R.drawable.small));
 		mBitMapCache.put(R.drawable.man2, BitmapFactory.decodeResource(getResources(), R.drawable.man2));
 		mBitMapCache.put(R.drawable.b, BitmapFactory.decodeResource(getResources(), R.drawable.b));
 		mBitMapCache.put(R.drawable.upgrade, BitmapFactory.decodeResource(getResources(), R.drawable.upgrade));
 		mBitMapCache.put(R.drawable.base, BitmapFactory.decodeResource(getResources(), R.drawable.base));
 		mBitMapCache.put(R.drawable.basee, BitmapFactory.decodeResource(getResources(), R.drawable.basee));
 		mBitMapCache.put(R.drawable.money, BitmapFactory.decodeResource(getResources(), R.drawable.money));
 		mBitMapCache.put(R.drawable.lives, BitmapFactory.decodeResource(getResources(), R.drawable.lives));
 		mBitMapCache.put(R.drawable.map1, BitmapFactory.decodeResource(getResources(), R.drawable.map1));
 		mBitMapCache.put(R.drawable.map2, BitmapFactory.decodeResource(getResources(), R.drawable.map2));
 		mBitMapCache.put(R.drawable.map3, BitmapFactory.decodeResource(getResources(), R.drawable.map3));
 		mBitMapCache.put(R.drawable.map4, BitmapFactory.decodeResource(getResources(), R.drawable.map4));
 		mBitMapCache.put(R.drawable.map5, BitmapFactory.decodeResource(getResources(), R.drawable.map5));
 		mBitMapCache.put(R.drawable.penguinmob, BitmapFactory.decodeResource(getResources(), R.drawable.penguinmob));
 		mBitMapCache.put(R.drawable.rock2, BitmapFactory.decodeResource(getResources(), R.drawable.rock2));
 		mBitMapCache.put(R.drawable.water, BitmapFactory.decodeResource(getResources(), R.drawable.water));
 		mBitMapCache.put(R.drawable.water2, BitmapFactory.decodeResource(getResources(), R.drawable.water2));
 		mBitMapCache.put(R.drawable.water3, BitmapFactory.decodeResource(getResources(), R.drawable.water3));
 		mBitMapCache.put(R.drawable.bigsnowball, BitmapFactory.decodeResource(getResources(), R.drawable.bigsnowball));
 		mBitMapCache.put(R.drawable.projsplash_big, BitmapFactory.decodeResource(getResources(), R.drawable.projsplash_big));
 		mBitMapCache.put(R.drawable.projslow, BitmapFactory.decodeResource(getResources(), R.drawable.projslow));
 		mBitMapCache.put(R.drawable.pause, BitmapFactory.decodeResource(getResources(), R.drawable.pause));
 		mBitMapCache.put(R.drawable.pause2, BitmapFactory.decodeResource(getResources(), R.drawable.pause2));
 		mBitMapCache.put(R.drawable.walrus, BitmapFactory.decodeResource(getResources(), R.drawable.walrus));
 		mBitMapCache.put(R.drawable.bear, BitmapFactory.decodeResource(getResources(), R.drawable.bear));
 		mBitMapCache.put(R.drawable.icebear, BitmapFactory.decodeResource(getResources(), R.drawable.icebear));
 		mBitMapCache.put(R.drawable.fastforward, BitmapFactory.decodeResource(getResources(), R.drawable.fastforward));
 		mBitMapCache.put(R.drawable.fastforward2, BitmapFactory.decodeResource(getResources(), R.drawable.fastforward2));
 		mBitMapCache.put(R.drawable.flyingpenguin, BitmapFactory.decodeResource(getResources(), R.drawable.flyingpenguin));
 		mBitMapCache.put(R.drawable.airtower, BitmapFactory.decodeResource(getResources(), R.drawable.airtower));
 		mBitMapCache.put(R.drawable.airtower1, BitmapFactory.decodeResource(getResources(), R.drawable.airtower1));
 		mBitMapCache.put(R.drawable.airtower2, BitmapFactory.decodeResource(getResources(), R.drawable.airtower2));
 		mBitMapCache.put(R.drawable.airtower3, BitmapFactory.decodeResource(getResources(), R.drawable.airtower3));
 		mBitMapCache.put(R.drawable.eskimotowersplash, BitmapFactory.decodeResource(getResources(), R.drawable.eskimotowersplash));
 		mBitMapCache.put(R.drawable.eskimotowersplash2, BitmapFactory.decodeResource(getResources(), R.drawable.eskimotowersplash2));
 		mBitMapCache.put(R.drawable.eskimotowersplash3, BitmapFactory.decodeResource(getResources(), R.drawable.eskimotowersplash3));
 		mBitMapCache.put(R.drawable.eskimotowersplash4, BitmapFactory.decodeResource(getResources(), R.drawable.eskimotowersplash4));
 		mBitMapCache.put(R.drawable.menutop, BitmapFactory.decodeResource(getResources(), R.drawable.menutop));
 		mBitMapCache.put(R.drawable.menumid, BitmapFactory.decodeResource(getResources(), R.drawable.menumid));
 		mBitMapCache.put(R.drawable.menubot, BitmapFactory.decodeResource(getResources(), R.drawable.menubot));
 		mBitMapCache.put(R.drawable.menutop2, BitmapFactory.decodeResource(getResources(), R.drawable.menutop2));
 		mBitMapCache.put(R.drawable.menumid2, BitmapFactory.decodeResource(getResources(), R.drawable.menumid2));
 		mBitMapCache.put(R.drawable.menubot2, BitmapFactory.decodeResource(getResources(), R.drawable.menubot2));
 	}
 
 	/**
 	 * Processes KeyEvents. Hardware buttons etc.
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_MENU:
 			// TODO Handle hardware menu button
 			GAME_STATE = STATE_PAUSED;			
 			break;
 		case KeyEvent.KEYCODE_BACK:
 			// TODO Handle hardware "back" button
 			GAME_STATE = STATE_PAUSED;
 			break;
 		case KeyEvent.KEYCODE_VOLUME_UP:
 			mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
 			break;
 		case KeyEvent.KEYCODE_VOLUME_DOWN:
 			mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
 			break;
 		}
 		return true;
 	}
 
 	/**
 	 * 
 	 * Processes MotionEvents. This is basically where all user input is handled
 	 * during the game play.
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
 		synchronized (getHolder()) {
 			switch (GAME_STATE) {
 			case STATE_RUNNING:
 				// store the coordinates of the event, the x coordinate with an offset of -60 pixels
 				mTx = (int) event.getX() - 60;
 				mTy = (int) event.getY();
 
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					//If the user has selected a Tower
 					if (mSelectedTower != null) {
 						
 						// Upgrade button pressed, and selected tower is upgradeable
 						if (sBtnUpgrade.contains(event.getX(), event.getY()) && mSelectedTower.canUpgrade()) {
 							if (GameModel.currentPlayer.getMoney() >= mSelectedTower.getUpgradeCost() && mSelectedTower.getUpgradeCost() != 0) {
 								GameModel.currentPlayer.changeMoney(-mSelectedTower.getUpgradeCost());
 								mSelectedTower.upgrade();
 							}
 						} else if (sBtnSell.contains(event.getX(), event.getY()) ) {
 							// Sell button pressed
 							GameModel.currentPlayer.changeMoney(mSelectedTower.sell());
 							GameModel.removeTower(mSelectedTower);
 							mSelectedTower = null;
 						} else 
 							mSelectedTower = null;
 
 					} else { // if the user has NOT selected a tower
 
 						mAllowBuild = false;
 						// game field clicked
 						if (event.getX() < 410) {
 							mShowTooltip = false;
 
 							// if a tower was clicked, mark it as selected
 							int size = GameModel.mTowers.size();
 							for (int i = 0; i < size; i++){
 								Tower t = GameModel.mTowers.get(i);
 
 								if (t.selectTower(event.getX(), event.getY())){
 									mSelectedTower = t;
 									break;
 								}
 							}
 
 							if (event.getX() > 0 && event.getX() < 40 && event.getY() > 0 && event.getY() < 50){
 								GAME_STATE = STATE_PAUSED;
 							}
 							
 							if(event.getX() > 0 && event.getX() < 40 && event.getY() > 270 && event.getY() < 320){
 								if(fastf){
 									GamePanel.setSpeedMultiplier(1);
 									fastf = false;
 								} else {
 									GamePanel.setSpeedMultiplier(3);	
 									fastf = true;
 								}
 								
 							}
 
 						} else if(sBtn1.contains(event.getX(),event.getY())) {
 							// button 1
 							if (mTower1.getCost() <= GameModel.currentPlayer.getMoney()) {
 								mAllowBuild = true;
 							}	
 							mCurrentTower = new BasicTower(mTx ,mTy);
 							mShowTooltip = true;
 
 						} else if(sBtn2.contains(event.getX(),event.getY())) {
 							// button 2
 							if (mTower2.getCost() <= GameModel.currentPlayer.getMoney()) {
 								mAllowBuild = true;
 							}	
 
 							mCurrentTower = new SplashTower(mTx ,mTy);
 							mShowTooltip = true;
 
 						} else if(sBtn3.contains(event.getX(),event.getY())) {
 							// button 3
 							if (mTower3.getCost() <= GameModel.currentPlayer.getMoney()) {
 								mAllowBuild = true;
 							}	
 							mCurrentTower = new SlowTower(mTx ,mTy);
 							mShowTooltip = true;
 
 						} else if(sBtn4.contains(event.getX(),event.getY())) {
 							// button 4
 							if (mTower4.getCost() <= GameModel.currentPlayer.getMoney()) {
 								mAllowBuild = true;
 							}	
 							mCurrentTower = new AirTower(mTx ,mTy);
 							mShowTooltip = true;
 
 							//button 5
 						} else if(sBtn5.contains(event.getX(),event.getY())) {
 
 							if (mAccelerometerSupported) {
 								
 								if (GameModel.currentPlayer.getCurrentTrackScore() >= mSnowballTreshold*(1+mUsedSnowballs)) {
 									mAllowBuild = true;
 								}
 								
 								if (GameModel.sCheatEnabled)
 									mAllowBuild = true;
 								
 								mCurrentSnowball = new Snowball(mTx,mTy);
 								mShowTooltip = true;
 							}
 						} 
 					}
 
 					break;
 				case MotionEvent.ACTION_MOVE:
 					
 					mShowTooltip =  event.getX() > 410;
 					// if a tower is being bought
 					if(mCurrentTower != null){
 						
 						// if building isn't allowed,
 						if(!mAllowBuild) {
 							// remove current tower
 //							mCurrentTower = null;
 						} else  {
 							// else update positions
 							mCurrentTower.setX(mTx);
 							mCurrentTower.setY(mTy);
 						}
 						
 					} else if (mCurrentSnowball != null) {
 						
 						// if building isn't allowed,
 						if(!mAllowBuild) {
 							// remove current snowball
 //							mCurrentSnowball = null;
 						} else  {
 							// else update positions
 							mCurrentSnowball.setX(mTx);
 							mCurrentSnowball.setY(mTy);
 						}
 					}
 					break;
 
 				case MotionEvent.ACTION_UP:
 					//if a tower is placed on the game field
 					if(mCurrentTower != null) {
 
 						if (GameModel.canAddTower(mCurrentTower) && mAllowBuild && mTx < 410) {
 
 							// build the tower and remove money from player
 							GameModel.buildTower(mCurrentTower, 
 									(int)mCurrentTower.getX() / GameModel.GAME_TILE_SIZE,
 									(int)mCurrentTower.getY() / GameModel.GAME_TILE_SIZE);
 							GameModel.currentPlayer.changeMoney(-mCurrentTower.getCost());
 
 
 						}
 						mCurrentTower = null;
 
 					} else if (mCurrentSnowball != null) {
 						// if a snowball is being placed
 						if (mAllowBuild && mTx < 410) {
 							GameModel.mSnowballs.add(mCurrentSnowball);
 							mUsedSnowballs++;
 						}
 						mCurrentSnowball = null;
 					}
 					break;
 				}
 				break;
 
 			case STATE_GAMEOVER:
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 
 
 					break;
 				case MotionEvent.ACTION_MOVE:
 					
 					if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34 &&  event.getY() <= 80+34+36){
 						menuPic = 1;
 					}
 					else if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34+36 &&  event.getY() <= 80+34+36+36){
 						menuPic = 2;
 					} else {
 						menuPic = 0;
 					}
 					
 					break;
 				case MotionEvent.ACTION_UP:
 					if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34 &&  event.getY() <= 80+34+36){
 						startTrack(GameModel.getTrack());						
 						GAME_STATE = STATE_RUNNING;		
 						mMobFactory.resetWaveNr(); // Resets the wave counter 
 					}
 					else if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34+36 &&  event.getY() <= 80+34+36+36){
 						// close the parent activity (go to main menu)
 						((Activity) getContext()).finish();
 					}
 					menuPic = 0;
 					break;
 				}
 				break;
 			case STATE_WIN:							
 				
 				switch (event.getAction()) {								
 				
 				case MotionEvent.ACTION_DOWN:
 
 
 					break;
 				case MotionEvent.ACTION_MOVE:
 					
 					if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34 &&  event.getY() <= 80+34+36){
 						menuPic = 1;
 					}
 					else if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34+36 &&  event.getY() <= 80+34+36+36){
 						menuPic = 2;
 					}
 					else if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34+36+36 &&  event.getY() <= 80+34+36+36+34){
 						menuPic = 3;
 					} else {
 						menuPic = 0;
 					}
 					
 					break;
 				case MotionEvent.ACTION_UP:
 					
 					if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34 &&  event.getY() <= 80+34+36){
 						// TODO restart with new level
 						if (GameModel.getTrack() < 5) {
 							startTrack(GameModel.getTrack()+1);												
 							GAME_STATE = STATE_RUNNING;
 							mMobFactory.resetWaveNr(); // Resets the wave counter
 						}
 							
 					}
 					else if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34+36 &&  event.getY() <= 80+34+36+36){
 						startTrack(GameModel.getTrack());												
 						GAME_STATE = STATE_RUNNING;
 						mMobFactory.resetWaveNr(); // Resets the wave counter
 					}
 					else if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34+36+36 &&  event.getY() <= 80+34+36+36+34){
 						// close the parent activity (go to main menu)
 						((Activity) getContext()).finish();
 					}
 
 					menuPic = 0;
 					
 					break;
 				}
 				break;
 
 			case STATE_PAUSED:	
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					// do nothing
 					break;
 				case MotionEvent.ACTION_MOVE:
 					
 					if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34 &&  event.getY() <= 80+34+36){
 						menuPic = 1;
 					}
 					else if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34+36 &&  event.getY() <= 80+34+36+36){
 						menuPic = 2;
 					}
 					else if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34+36+36 &&  event.getY() <= 80+34+36+36+34){
 						menuPic = 3;
 					} else {
 						menuPic = 0;
 					}
 					break;
 				case MotionEvent.ACTION_UP:
 					if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34 &&  event.getY() <= 80+34+36){
 						GAME_STATE = STATE_RUNNING;
 					}
 					else if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34+36 &&  event.getY() <= 80+34+36+36){
 						startTrack(GameModel.getTrack());											
 						GAME_STATE = STATE_RUNNING;			
 						mMobFactory.resetWaveNr(); // Resets the wave counter
 					}
 					else if(event.getX() >= 100 && event.getX() <= 344 && event.getY() >= 80+34+36+36 &&  event.getY() <= 80+34+36+36+34){
 						// close the parent activity (go to main menu)
 						((Activity) getContext()).finish();
 					}
 					menuPic = 0;
 					break;
 				}
 				break;
 			}
 		}
 		
 		// TODO why is this here? what purpose does it serve?
 		try {
 			Thread.sleep(16);
 		} catch (InterruptedException e) {
 			Log.v("App: ", "Error 2");
 		}
 
 		return true;
 	}
 
 
 	/**
 	 * This class is called each frame. 
 	 * It keeps track of the creation of the mobs from the waves of the current map
 	 * Called from updateModel 
 	 */
 	private Mob createMobs() {  	    	    	    	        	    	    	
 
 		int track = GameModel.getTrack();
 		
 		if (mMobDelayI >= MOB_DELAY_MAX) {
 			mMobDelayI = 0;
 			
 			if(track > 0) {
 				return mMobFactory.getNextMob();
 			} else {
 				return null;
 			}
 		} else {
 			mMobDelayI += GamePanel.getSpeedMultiplier();
 			return null;
 		}
 
 	}
 
 	/**
 	 * This class is called from the GameThread. 
 	 * It updates the state of all towers, mobs and projectiles. 
 	 * It also handles projectile collisions with mobs dying and such.
 	 */
 	public void updateModel() {
 
 		debug.UpdateFPS();
 
 		if (GAME_STATE == STATE_RUNNING) {
 
 			// If the player has 0 or less lives remaining, change game state
 			if (GameModel.currentPlayer.getRemainingLives() <= 0) {
 				mSelectedTower = null;
 				mCurrentSnowball = null;
 				mCurrentTower = null;
 				mShowTooltip = false;
 				GAME_STATE = STATE_GAMEOVER;
 				fastf = false;
 				setSpeedMultiplier(1);
 				return;
 			}
 
 			// if the player has won (no more mobs and all mobs dead)
 			if (mMobFactory.hasMoreMobs() == false && GameModel.mMobs.isEmpty()) {
 				mSelectedTower = null;
 				mCurrentSnowball = null;
 				mCurrentTower = null;
 				mShowTooltip = false;
 				GAME_STATE = STATE_WIN;
 				fastf = false;
 				setSpeedMultiplier(1);
 				GameModel.currentPlayer.saveCurrentTrackScore();
 				return;
 			}
 
 			Mob mNewMob = createMobs();
 			if (mNewMob != null) {
 				GameModel.mMobs.add(mNewMob);
 //				Log.v("GAME MOBS", "Added new mob of type: "
 //						+ mNewMob.getType().toString());
 			}
 
 			/*
 			 * for every tower:
 			 * 	create a new Projectile set to a Mob that the Tower can reach
 			 *  and add that to the list of Projectiles in the GameModel
 			 * 
 			 * tryToShoot() returns null if the tower can't reach any mob or if the tower is on CD
 			 */
 			int size = GameModel.mTowers.size();
 			for (int i = 0; i < size; ++i) {
 				Tower t = GameModel.mTowers.get(i);
 
 				Projectile newProjectile = null;
 
 				//if there are any mobs, try to shoot at them
 				if (GameModel.mMobs.isEmpty() == false)
 					newProjectile = t.tryToShoot();
 
 				//if a projectile was returned, add it to the game model
 				if (newProjectile != null) {
 					GameModel.mProjectiles.add(newProjectile);
 				}
 				
 			}
 
 			// Check if any projectile has hit it's target
 			// Handle hit, remove projectile, calculate damage on mob, etc. etc.
 			size = GameModel.mProjectiles.size();
 			int removed = 0;
 			for (int i = 0; i < size - removed; ++i) {
 				Projectile p = GameModel.mProjectiles.get(i);								
 				
 				// Update position for the projectiles
 				p.updatePosition();
 
 				// If the projectile has collided, inflict damage and remove it.
 				if (p.hasCollided()) {					
 					p.inflictDmg();
 					GameModel.mProjectiles.remove(p);
 					++removed;
 				}
 
 				// if the projectile's target is dead, remove the projectile
 				if (p.getTarget().getHealth() <= 0) {
 					GameModel.mProjectiles.remove(p);	
 					++removed;
 				}
 			}
 
 			/*
 			 * For every snowball:
 			 * update position
 			 * do damage to any mob it hits
 			 */
 			for (int j = 0; j < GameModel.mSnowballs.size(); ++j) {
 				Snowball s = GameModel.mSnowballs.get(j);
 
 				// update position with accelerometer
 				s.updatePosition(mLatestSensorEvent);
 
 				// read what mobs are hit
 				List<Mob> deadMobs = s.getCollidedMobs(GameModel.mMobs);
 
 				// handle mobs that were hit
 				for (int k = 0; k < deadMobs.size(); k++) {
 					deadMobs.get(k).setHealth(0);
 				}
 
 				// if the snowball is out of charges, remove it
 				if (s.getCharges() <= 0) {
 					GameModel.mSnowballs.remove(s);
 				}
 			}
 
 			/*
 			 * For every mob:
 			 *  Update position
 			 *  If the mob has died, handle it
 			 */
 			size = GameModel.mMobs.size();
 			removed = 0;
 			for (int j = 0; j < size - removed; j++) {
 				Mob m = GameModel.mMobs.get(j);				
 
 				// update position, if the mob reached the last checkpoint, handle it
 				if (!m.updatePosition()) {
 					mSplash = true;
 					GameModel.mMobs.remove(m);
 					++removed;
 					GameModel.currentPlayer.removeLife();
 				}
 				
 				
 				// handle mob death
 				if (m.getHealth() <= 0) {
 					GameModel.currentPlayer.changeMoney(m.getReward());
 					GameModel.currentPlayer.changeScore(m);					
 					GameModel.mShowRewardForMob.add(m);
 					GameModel.mMobs.remove(m);
 					++removed;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Draw on the SurfaceView.
 	 * Order:
 	 * <ul>
 	 *  <li>Game map and environments</li>
 	 *  <li>Mobs</li>
 	 *  <li>Snowballs</li>
 	 *  <li>Towers></li>
 	 *  <li>Projectiles</li>
 	 *  <li>Buttons</li>
 	 *  <li>Current Tower/Snowball</li>
 	 *  <li>Stats</li>
 	 *  <li>Win- and Lose screen</li>
 	 * </ul>
 	 */
 	@Override
 	public void onDraw(Canvas canvas) {
 		// Dela in subtask i subfunktioner. Ser snyggare ut! / Jonas
 		// DONE AND DONE /Fredrik
 
 		drawBackground(canvas);
 		drawSplashWater(canvas);
 		drawMobs(canvas);
 		drawTowers(canvas);
 		drawSnowballs(canvas);
 		drawProjectiles(canvas);
 		drawButtons(canvas);
 		drawStatisticsText(canvas);
 		drawRewardsAfterDeadMob(canvas);	
 
 
 		switch (GAME_STATE) {
 		case STATE_RUNNING:
 			// if a tower is being bought
 			// draw either the tooltip for it, or how it would be placed.
 			if (mCurrentTower != null || mCurrentSnowball != null) {
 				if (mShowTooltip)
 					drawTooltip(canvas);
 				else if (mCurrentTower != null && mAllowBuild)
 					drawCurrentTower(canvas);
 				else if (mCurrentSnowball != null && mAllowBuild) {
 					canvas.drawCircle(
 							(int)mCurrentSnowball.getX(),
 							(int)mCurrentSnowball.getY(),
 							10 + mCurrentSnowball.getCharges(),snowPaint);
 				}
 			}
 
 			// if a tower is selected for upgrades and such and such
 			if(mSelectedTower != null){	
 				drawUpgradeWindow(canvas);
 			}
 			break;
 
 		case STATE_GAMEOVER: // loser screen TODO make it look good
 			
 			canvas.drawBitmap(mBitMapCache.get(R.drawable.menutop),100,80,null);
 			
 			if(menuPic == 1){
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid2),100,80+34,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menubot),100,80+34+36,null);				
 			} else if(menuPic == 2){
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menubot2),100,80+34+36,null);	
 			} else {
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menubot),100,80+34+36,null);	
 			}
 			
 			canvas.drawText("Try again!", 171,80+20+2,sPaintTextBlack);
 			canvas.drawText("Restart",181,80+34+20+2,sPaintTextBlack);
 			canvas.drawText("Exit",181,80+34+36+20+2,sPaintTextBlack);
 			
 			canvas.drawText("Try again!", 170,80+20,sPaintTextWhite);
 			canvas.drawText("Restart",180,80+34+20,sPaintTextWhite);
 			canvas.drawText("Exit",180,80+34+36+20,sPaintTextWhite);
 			
 			break;
 
 		case STATE_WIN: // winner screen TODO make it look good
 			
 			canvas.drawBitmap(mBitMapCache.get(R.drawable.menutop),100,80,null);
 			if(menuPic == 1){
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid2),100,80+34,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34+36,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menubot),100,80+34+36+36,null);				
 			} else if(menuPic == 2){
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid2),100,80+34+36,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menubot),100,80+34+36+36,null);	
 			}  else if(menuPic == 3){
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34+36,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menubot2),100,80+34+36+36,null);	
 			} else {
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34+36,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menubot),100,80+34+36+36,null);	
 			}
 
 
 			canvas.drawText("Level complete!", 156,80+20+2,sPaintTextBlack);
 			canvas.drawText("Next",181,80+34+20+2,sPaintTextBlack);
 			canvas.drawText("Restart",181,80+34+36+20+2,sPaintTextBlack);
 			canvas.drawText("Exit", 181, 80+34+36+36+20+2, sPaintTextBlack);
 			
 			canvas.drawText("Level complete!", 155,80+20,sPaintTextWhite);
 			canvas.drawText("Next",180,80+34+20,sPaintTextWhite);
 			canvas.drawText("Restart",180,80+34+36+20,sPaintTextWhite);
 			canvas.drawText("Exit", 180, 80+34+36+36+20, sPaintTextWhite);
 			
 			break;
 
 		case STATE_PAUSED: // pause screen
 			
 			canvas.drawBitmap(mBitMapCache.get(R.drawable.menutop),100,80,null);
 			if(menuPic == 1){
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid2),100,80+34,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34+36,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menubot),100,80+34+36+36,null);				
 			} else if(menuPic == 2){
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid2),100,80+34+36,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menubot),100,80+34+36+36,null);	
 			}  else if(menuPic == 3){
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34+36,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menubot2),100,80+34+36+36,null);	
 			} else {
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menumid),100,80+34+36,null);
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.menubot),100,80+34+36+36,null);	
 			}
 
 
 			canvas.drawText("GAME PAUSED!", 156,80+20+2,sPaintTextBlack);
 			canvas.drawText("Resume",181,80+34+20+2,sPaintTextBlack);
 			canvas.drawText("Restart",181,80+34+36+20+2,sPaintTextBlack);
 			canvas.drawText("Exit", 181, 80+34+36+36+20+2, sPaintTextBlack);
 			
 			canvas.drawText("GAME PAUSED!", 155,80+20,sPaintTextWhite);
 			canvas.drawText("Resume",180,80+34+20,sPaintTextWhite);
 			canvas.drawText("Restart",180,80+34+36+20,sPaintTextWhite);
 			canvas.drawText("Exit", 180, 80+34+36+36+20, sPaintTextWhite);
 			
 
 			break;
 		}
 	}
 
 	private void drawSplashWater(Canvas canvas){
 		if(mSplash){
 			int x = 422; //TODO: get path from last destination
 			int y = 130;
 			if(mWateranimation >= 0 && mWateranimation < 5){
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.water),x,y,null);
 				mWateranimation++;
 			} else if(mWateranimation >= 5 && mWateranimation < 10){
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.water2),x,y,null);
 				mWateranimation++;
 			} else if(mWateranimation >= 10 && mWateranimation < 15){
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.water3),x,y,null);
 				mWateranimation++;
 			} else if(mWateranimation >= 15 && mWateranimation < 20){
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.water2),x,y,null);
 				mWateranimation++;
 			} else if(mWateranimation >= 20 && mWateranimation < 25){
 				canvas.drawBitmap(mBitMapCache.get(R.drawable.water),x,y,null);
 				mWateranimation++;
 			} 
 		}
 
 		if(mWateranimation >= 25){ 
 			mWateranimation = 0;
 			mSplash = false;
 		}
 	}
 
 	private void drawStatisticsText(Canvas canvas) {
 		// draw debug messages in the top left corner
 		canvas.drawText("FPS: "+Float.toString(debug.getFPS()) + " Mobs:"+ GameModel.mMobs.size()+
 				" Proj:"+GameModel.mProjectiles.size() + " Towers:"+ GameModel.mTowers.size(), 10, 320,sPaintText);
 
 		// show stats of the player    	
 		canvas.drawBitmap(mBitMapCache.get(R.drawable.money),80,3, null);
 		canvas.drawText("" + (int)GameModel.currentPlayer.getMoney(), 105, 20, sPaintText);
 		canvas.drawBitmap(mBitMapCache.get(R.drawable.lives), 160, 3, null);
 		canvas.drawText("" + GameModel.currentPlayer.getRemainingLives(), 185, 20, sPaintText);
 		canvas.drawText(mMobFactory.getWaveNr() + "/" + mMobFactory.getTotalNrOfWaves(), 230, 20, sPaintText);
 		canvas.drawText("Score: " + (int)GameModel.currentPlayer.getCurrentTrackScore(), 290, 20, sPaintText);
 		
 		int mWaveTime = mMobFactory.getWaveTime(); 
 		
 		if(mWaveTime < mMobFactory.getWaveMaxDelay()) {
 			canvas.drawText("Next wave: " + mMobFactory.getWaveType() + "("+ mWaveTime + ")", 260, 300, sPaintText);
 		}
 
 	}
 
 	//TODO make this look a lot better!
 	private void drawUpgradeWindow(Canvas canvas) {
 		// draw a circle that shows the tower's range
 		canvas.drawCircle(
 				GameModel.GAME_TILE_SIZE * ((float)mSelectedTower.getX() / GameModel.GAME_TILE_SIZE + (mSelectedTower.getWidth()/2)),
 				GameModel.GAME_TILE_SIZE * ((float)mSelectedTower.getY() / GameModel.GAME_TILE_SIZE + (mSelectedTower.getHeight()/2)),
 				mSelectedTower.getRange(), rangeIndicationPaint);
 
 		// draw box for the selected tower
 		canvas.drawRoundRect(sTransparentBox,10,10,sPaintTransparentBox);
 
 		canvas.drawBitmap(mBitMapCache.get(mSelectedTower.getImage()), 100, 80, null);
 		
 		//Draw general info
 		int lvl = mSelectedTower.getLevel();
 		//name
 		canvas.drawText(mSelectedTower.getName(), 150, 90, boxTextPaintTitle);
 		//level
 		canvas.drawText("Level " + (lvl), 140, 112, sPaintBoxText);
 		//attack speed
 		canvas.drawText("Speed: " + mSelectedTower.getAttackSpeed(), 140, 128, sPaintBoxText);
 		//damage
 		canvas.drawText("Damage: " + mSelectedTower.getDamage(), 140, 144, sPaintBoxText);
 		//range
 		canvas.drawText("Range: " + mSelectedTower.getRange(), 140, 160, sPaintBoxText);
 		
 		if (mSelectedTower.getType() == Tower.SLOW)
			canvas.drawText("Slow: " + mSelectedTower.getSlow(), 140, 176, sPaintBoxText);
 		else if (mSelectedTower.getType() == Tower.SPLASH)
			canvas.drawText("Splash: " + mSelectedTower.getSplash(), 140, 176, sPaintBoxText);
 
 		if(mSelectedTower.canUpgrade()) {
 			//find values for next level of depending on tower type
 			//TODO fix how it looks when the tower is max upgraded
 			//Draw upgrade info
 			Paint p;
 			if (GameModel.currentPlayer.getMoney() >= mSelectedTower.getUpgradeCost())
 				p = sPaintBoxGreen;
 			else
 				p = sPaintBoxRed;
 			
 			canvas.drawText(" -> " + (lvl+1), 225, 112, p);
 
 			switch (mSelectedTower.getType()) {
 			case Tower.BASIC:
 				canvas.drawText(" -> " + 1000/BasicTower.sCoolDown[lvl], 225, 128, p);
 				canvas.drawText(" -> " + BasicTower.sDamage[lvl], 225, 144, p);
 				canvas.drawText(" -> " + BasicTower.sRange[lvl], 225, 160, p);
 				break;
 			case Tower.AIR:
 				canvas.drawText(" -> " + 1000/AirTower.sCoolDown[lvl], 225, 128, p);
 				canvas.drawText(" -> " + AirTower.sDamage[lvl], 225, 144, p);
 				canvas.drawText(" -> " + AirTower.sRange[lvl], 225, 160, p);
 				break;
 			case Tower.SLOW:
 				canvas.drawText(" -> " + 1000/SlowTower.sCoolDown[lvl], 225, 128, p);
 				canvas.drawText(" -> " + SlowTower.sDamage[lvl], 225, 144, p);
 				canvas.drawText(" -> " + SlowTower.sRange[lvl], 225, 160, p);
				canvas.drawText(" -> " + SlowTower.sSlow[lvl], 225, 176, p);
 				break;
 			case Tower.SPLASH:
 				canvas.drawText(" -> " + 1000/SplashTower.sCoolDown[lvl], 225, 128, p);
 				canvas.drawText(" -> " + SplashTower.sDamage[lvl], 225, 144, p);
 				canvas.drawText(" -> " + SplashTower.sRange[lvl], 225, 160, p);
				canvas.drawText(" -> " + SplashTower.sSplash[lvl], 225, 176, p);
 				break;
 			}
 		}
 		
 		//Adds sell button TODO add sell price
 		canvas.drawRoundRect(sBtnSell,10,10,sPaintBtnBox);
 		canvas.drawText("Sell", sBtnSell.left+10, sBtnSell.top+(sBtnSell.height()/2), sPaintBoxText);
 
 		// if the tower is not fully upgraded and the player affords it
 		if (mSelectedTower.canUpgrade() && 
 				GameModel.currentPlayer.getMoney() >= mSelectedTower.getUpgradeCost()) {
 
 			Paint paint = new Paint();
 			paint.setARGB(255, 0, 255, 0);
 
 			canvas.drawRoundRect(sBtnUpgrade,6,6,paint);
 			canvas.drawText("Upgrade: " + mSelectedTower.getUpgradeCost() + "$",
 					sBtnUpgrade.left+10, sBtnUpgrade.top+(sBtnSell.height()/2), sPaintBoxText);
 
 			// if the tower is not fully upgraded, but the player can't afford upgrading
 		} else if (mSelectedTower.canUpgrade() && 
 				GameModel.currentPlayer.getMoney() < mSelectedTower.getUpgradeCost()) {
 
 			Paint paint = new Paint();
 			paint.setARGB(255, 255, 0, 0);
 
 			canvas.drawRoundRect(sBtnUpgrade,6,6,paint);
 			canvas.drawText("Upgrade: " + mSelectedTower.getUpgradeCost() + "$",
 					sBtnUpgrade.left+10, sBtnUpgrade.top+(sBtnSell.height()/2), sPaintBoxText);
 
 			// if the tower is fully upgraded
 		} else if (mSelectedTower.canUpgrade() == false) {
 
 			Paint paint = new Paint();
 			paint.setARGB(255, 100, 100, 100);
 
 			canvas.drawRoundRect(sBtnUpgrade,6,6,paint);
 			canvas.drawText("Fully upgraded!",
 					sBtnUpgrade.left+10, sBtnUpgrade.top+(sBtnSell.height()/2), sPaintBoxText);
 		}
 	}
 
 
 	private void drawCurrentTower(Canvas canvas) {
 		// draw the chosen tower
 		canvas.drawBitmap(
 				mBitMapCache.get(mCurrentTower.getImage()), GameModel.GAME_TILE_SIZE*(mTx / GameModel.GAME_TILE_SIZE) ,
 				GameModel.GAME_TILE_SIZE*(mTy / GameModel.GAME_TILE_SIZE) , null);
 
 		// draw a red transparent rectangle on every occupied tile
 		for (Point p : GameModel.mOccupiedTilePositions) {
 			canvas.drawRect(
 					p.x*GameModel.GAME_TILE_SIZE,
 					p.y*GameModel.GAME_TILE_SIZE,
 					(1+p.x)*GameModel.GAME_TILE_SIZE,
 					(1+p.y)*GameModel.GAME_TILE_SIZE,
 					gridpaint);
 		}
 
 		// draw a circle that shows the tower's range
 		// one color if it can be placed on current location, another if can't
 		if (GameModel.canAddTower(mCurrentTower)) {
 			canvas.drawCircle(
 					GameModel.GAME_TILE_SIZE*(mTx / GameModel.GAME_TILE_SIZE + (mCurrentTower.getWidth()/2)),
 					GameModel.GAME_TILE_SIZE*(mTy / GameModel.GAME_TILE_SIZE + (mCurrentTower.getHeight()/2)),
 					mCurrentTower.getRange(),
 					rangeIndicationPaint);
 		} else {
 			canvas.drawCircle(
 					GameModel.GAME_TILE_SIZE*(mTx / GameModel.GAME_TILE_SIZE + (mCurrentTower.getWidth()/2)),
 					GameModel.GAME_TILE_SIZE*(mTy / GameModel.GAME_TILE_SIZE + (mCurrentTower.getHeight()/2)),
 					mCurrentTower.getRange(),
 					noRangeIndicationPaint);
 		}
 	}
 
 	private void drawTooltip(Canvas canvas) {
 
 		// draw tooltip for the current tower
 		canvas.drawRoundRect(sTransparentBox,10,10, sPaintTransparentBox);
 
 		// if a tower is being bought
 		if (mCurrentTower != null) {
 			canvas.drawBitmap(mBitMapCache.get(mCurrentTower.getImage()), 100, 80, null);
 			canvas.drawText(mCurrentTower.getName(), 160, 90, boxTextPaintTitle);
 			//TODO uncomment again and fix position
 			//canvas.drawText("" + mCurrentTower.getDescription(), 160, 117, sPaintBoxText);
 			canvas.drawText("Attack speed: " + mCurrentTower.getAttackSpeed(), 160, 117, sPaintBoxText);
 			canvas.drawText("Damage: " + mCurrentTower.getDamage(), 160, 139, sPaintBoxText);
 			canvas.drawText("Range: " + mCurrentTower.getRange(), 160, 161, sPaintBoxText);
 			canvas.drawText("Cost: " + mCurrentTower.getCost(), 160, 183, sPaintBoxText);
 			canvas.drawText("Drag to buy this tower!", 100, 210, sPaintBoxText);
 		} else {
 		// if a snowball is being bought
 			canvas.drawBitmap(mBitMapCache.get(R.drawable.bigsnowball), 100, 80,null);
 			canvas.drawText("Snowball", 160, 90, boxTextPaintTitle);
 			canvas.drawText("Run over your enemies!", 160, 117, sPaintBoxText);
 			canvas.drawText("Control the snowball by", 160, 139, sPaintBoxText);
 			canvas.drawText("  tilting your phone!", 160, 161, sPaintBoxText);
 			canvas.drawText("Available every "+ mSnowballTreshold+" scorepoints.", 100, 210, sPaintBoxText);
 		}
 
 	}
 
 	private void drawButtons(Canvas canvas) {
 
 		Paint paint = new Paint();
 		paint.setARGB(100,100,100,100);
 		paint.setStyle(Paint.Style.FILL);
 
 		canvas.drawRoundRect(sBtn1, 5, 5, paint);
 		canvas.drawRoundRect(sBtn2, 5, 5, paint);
 		canvas.drawRoundRect(sBtn3, 5, 5, paint);
 		canvas.drawRoundRect(sBtn4, 5, 5, paint);
 		canvas.drawRoundRect(sBtn5, 5, 5, paint);
 		
 		if(GAME_STATE == STATE_PAUSED)
 			canvas.drawBitmap(mBitMapCache.get(R.drawable.pause2),20,5,null);
 		else
 			canvas.drawBitmap(mBitMapCache.get(R.drawable.pause),20,5,null);
 				
 		if(fastf)
 			canvas.drawBitmap(mBitMapCache.get(R.drawable.fastforward2),20,285,null);
 		else
 			canvas.drawBitmap(mBitMapCache.get(R.drawable.fastforward),20,285,null);
 		
 		Paint paintalfa = new Paint();
 
 		//if the tower build buttons should be "unavaliable" or not
 		if(mTower1.getCost() >= GameModel.currentPlayer.getMoney()) {
 			paintalfa.setAlpha(100);
 		} else {
 			paintalfa.setAlpha(255);
 		}
 		canvas.drawBitmap(mBitMapCache.get(R.drawable.basictower),432,25,paintalfa);
 
 		if(mTower2.getCost() >= GameModel.currentPlayer.getMoney()) {
 			paintalfa.setAlpha(100);
 		} else {
 			paintalfa.setAlpha(255);
 		}
 		canvas.drawBitmap(mBitMapCache.get(R.drawable.eskimotowersplash),432,85,paintalfa);
 
 		if(mTower3.getCost() >= GameModel.currentPlayer.getMoney()) {
 			paintalfa.setAlpha(100);
 		} else {
 			paintalfa.setAlpha(255);
 		}
 		canvas.drawBitmap(mBitMapCache.get(R.drawable.slowtower),432,145,paintalfa);
 		
 		if(mTower4.getCost() >= GameModel.currentPlayer.getMoney()) {
 			paintalfa.setAlpha(100);
 		} else {
 			paintalfa.setAlpha(255);
 		}
 		canvas.drawBitmap(mBitMapCache.get(R.drawable.airtower1),432,205,paintalfa);
 
 		if(GameModel.currentPlayer.getCurrentTrackScore() >= mSnowballTreshold*(1+mUsedSnowballs)) {
 			paintalfa.setAlpha(255);
 		} else {
 			paintalfa.setAlpha(100);
 		}
 		
 		if (GameModel.sCheatEnabled)
 			paintalfa.setAlpha(255);
 		
 		canvas.drawBitmap(mBitMapCache.get(R.drawable.bigsnowball),432,265,paintalfa);
 
 
 	}
 
 	/**
 	 * Draws all projectiles currently on the gamefield
 	 * @param canvas
 	 */
 	private void drawProjectiles(Canvas canvas) {
 		// draw all projectiles
 		int size = GameModel.mProjectiles.size();
 		for (int i = 0; i < size; i++) {
 			Projectile p = GameModel.mProjectiles.get(i);
 			Bitmap bitmapOrg = mBitMapCache.get(GameModel.mProjectiles.get(i).getProjImage()); //R.drawable.projsplash_big before ahmed
 			Matrix matrix = new Matrix(); 
 
 			// rotate the Bitmap 
 			//matrix.postRotate((float) (-1*p.getAngle()/Math.PI*180));
 			Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, bitmapOrg.getWidth(), bitmapOrg.getHeight(), matrix, true); 
 			canvas.drawBitmap(resizedBitmap, (int) p.getX(), (int) p.getY(), null);
 		}
 	}
 
 	/**
 	 * Draws any active towers on the gamefield
 	 * @param canvas
 	 */
 	private void drawTowers(Canvas canvas) {
 		// draw all towers
 		int size = GameModel.mTowers.size();
 		for (int i = 0; i < size; i++) {
 			Tower t = GameModel.mTowers.get(i);
 			canvas.drawBitmap(mBitMapCache.get(t.getImage()), (int) t.getX() , (int) t.getY() , null);
 		}
 	}
 
 	/**
 	 * Draws any active snowballs currently on the gamefield
 	 * @param canvas
 	 */
 	private void drawSnowballs(Canvas canvas) {
 		// draw snowballs
 		for (int i = 0; i < GameModel.mSnowballs.size(); i++) {
 			Snowball s = GameModel.mSnowballs.get(i);
 
 			canvas.drawCircle((float)s.getX(), (float)s.getY(), 10 + s.getCharges(), snowPaint);
 			canvas.drawCircle((float)s.getX(), (float)s.getY(), 10 + s.getCharges(), borderPaint);
 		}
 	}
 
 	/**
 	 * Draws all active mobs on the gamefield
 	 * @param canvas
 	 */
 	private void drawMobs(Canvas canvas) {
 		// for all mobs
 		for (int i = GameModel.mMobs.size()-1; i >= 0; i--) {
 			Mob m = GameModel.mMobs.get(i);
 
 			// get the mob's image
 			Bitmap mobImage = mBitMapCache.get(m.getMobImage());
 			Matrix matrix = new Matrix();
 			
 			// if the mob is of type HEALTHY,
 			if (m.getType() == MobType.HEALTHY) {
 				int mMultiplier = 3;
 				
 				// rotate the Bitmap according to animation frame
 				switch(m.nextAnimation(12)) {
 					case 0: matrix.postRotate((float) (0)); break;
 					case 1: matrix.postRotate((float) (1*mMultiplier)); break;
 					case 2: matrix.postRotate((float) (2*mMultiplier)); break;
 					case 3: matrix.postRotate((float) (3*mMultiplier)); break;
 					case 4: matrix.postRotate((float) (2*mMultiplier)); break;
 					case 5: matrix.postRotate((float) (1*mMultiplier)); break;
 					case 6: matrix.postRotate((float) (0)); break;
 					case 7: matrix.postRotate((float) (-1*mMultiplier)); break;
 					case 8: matrix.postRotate((float) (-2*mMultiplier)); break;
 					case 9: matrix.postRotate((float) (-3*mMultiplier)); break;
 					case 10: matrix.postRotate((float) (-2*mMultiplier)); break;
 					case 11: matrix.postRotate((float) (-1*mMultiplier)); break;
 				}
 			}
 			
 			Bitmap tiltMob = Bitmap.createBitmap(mobImage, 0, 0,
 					mobImage.getWidth(), mobImage.getHeight(), matrix, true);
 
 			// create offsets for AIR type mobs. they fly higher than other animals
 			int mOffset,mOffset2;
 			if(m.getType() == Mob.MobType.AIR) {
 				mOffset = 25;
 				mOffset2 = 2;
 			} else {
 				mOffset = 0;
 				mOffset2 = 0;
 			}
 			
 			canvas.drawBitmap(tiltMob, (int) m.getX(), (int) m.getY() - mOffset, null);
 			
 			int hpRatio = (int)(255* (double)m.getHealth() / (double)m.getMaxHealth());
 
 			// drawing health bars for each mob, first a black background
 			healthBarPaint.setARGB(255, 0, 0, 0);
 			canvas.drawRect(
 					(float)m.getX() - 2 + mOffset2,
 					(float) m.getY() - 5 - mOffset,
 					(float) (m.getX() + 24 + mOffset2),
 					(float) m.getY() - 2 - mOffset,
 					healthBarPaint);
 			
 			// draw current health on the health bar
 			healthBarPaint.setARGB(255, 255 - hpRatio, hpRatio, 0);
 			canvas.drawRect(
 					(float)m.getX() - 2 + mOffset2,
 					(float) m.getY() - 5 - mOffset,
 					(float) (m.getX() + (24 * hpRatio/255)) + mOffset2,
 					(float) m.getY() - 2 - mOffset,
 					healthBarPaint);			
 
 		}
 	}
 
 	/**
 	 * Draws the background map and such.
 	 * 
 	 * Uses different background images depending on what track is played
 	 * 
 	 * @param canvas
 	 */
 	private void drawBackground(Canvas canvas) {
 		
 		// draw the background
 		switch(GameModel.getTrack()) {
 		case 1: // track 1
 			canvas.drawBitmap(mBitMapCache.get(R.drawable.map1), 0 , 0, null);break;
 		case 2: //track 2
 			canvas.drawBitmap(mBitMapCache.get(R.drawable.map2), 0 , 0, null); break;	
 		case 3: //track 3
 			canvas.drawBitmap(mBitMapCache.get(R.drawable.map3), 0 , 0, null); break;
 		case 4: //track 4
 			canvas.drawBitmap(mBitMapCache.get(R.drawable.map4), 0 , 0, null); break;
 		case 5: //track 5
 			canvas.drawBitmap(mBitMapCache.get(R.drawable.map5), 0 , 0, null); break;		
 		}
 		
 		//draw the "end-point-base"
 		canvas.drawBitmap(mBitMapCache.get(R.drawable.basee),403,0,null);
 	}
 	
 	/**
 	 * Draws the rewards that are showed when mobs die.
 	 * @param canvas
 	 */
 	private void drawRewardsAfterDeadMob(Canvas canvas){
 		for (int i = 0; i < GameModel.mShowRewardForMob.size(); i++) {
 			canvas.drawText("" +GameModel.mShowRewardForMob.get(i).getReward(),
 					(int)GameModel.mShowRewardForMob.get(i).getX() + 1,
 					(int)GameModel.mShowRewardForMob.get(i).getY() - 1,
 					sMoneyAfterDeadBg);
 			canvas.drawText("" +GameModel.mShowRewardForMob.get(i).getReward(),
 					(int)GameModel.mShowRewardForMob.get(i).getX(),
 					(int)GameModel.mShowRewardForMob.get(i).getY(),
 					sMoneyAfterDead);
 			GameModel.mShowRewardForMob.get(i).setY(GameModel.mShowRewardForMob.get(i).getY() - 2);
 			GameModel.mShowRewardForMob.get(i).incRewAni();
 			if(GameModel.mShowRewardForMob.get(i).getRewAni() > 12){
 				GameModel.mShowRewardForMob.remove(i);
 			}
 		}
 	}
 
 	/**
 	 * Configures all Paint-objects used in onDraw().
 	 */
 	private void setupPaint() {
 		
 		sMoneyAfterDead.setARGB(255,255,255,0);
 		sMoneyAfterDead.setTextSize(16);
 		
 		sMoneyAfterDeadBg.setARGB(255,0,0,0);
 		sMoneyAfterDeadBg.setTextSize(16);
 		
 		// set gray color for buttons in in-game menus 
 		mBtnPaint.setARGB(255, 50, 50, 50);
 
 		// set text size of the FPS meter and such and such
 		sPaintText.setTextSize(16);
 		Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
 		sPaintText.setTypeface(font);
 		sPaintText.setAntiAlias(true);
 		
 		sPaintTextWhite.setTextSize(16);
 		sPaintTextWhite.setARGB(255, 255, 255, 255);
 		Typeface font2 = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
 		sPaintTextWhite.setTypeface(font2);
 		sPaintTextWhite.setAntiAlias(true);
 		
 		sPaintTextBlack.setTextSize(16);
 		sPaintTextBlack.setARGB(255, 0, 0, 0);
 		Typeface font3 = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
 		sPaintTextBlack.setTypeface(font3);
 		sPaintTextBlack.setAntiAlias(true);
 		
 		// set color of the selected tower box
 		sPaintTransparentBox.setARGB(90, 51, 51, 51);
 
 		// set color of the upgrade- and sell buttons in the selected tower box
 		sPaintBtnBox.setARGB(255, 51, 51, 51);
 
 		// set text size and color of the text in selected tower box
 		sPaintBoxText.setARGB(255, 255, 255, 255);
 		sPaintBoxText.setTextSize(14);
 		
 		sPaintBoxGreen.setARGB(255, 20, 190, 30);
 		sPaintBoxGreen.setTextSize(14);
 		sPaintBoxRed.setARGB(255, 170, 30, 20);
 		sPaintBoxRed.setTextSize(14);
 
 		boxTextPaintTitle.setARGB(255, 255, 255, 255);
 		boxTextPaintTitle.setTextSize(22);
 
 		// set color and width of the lines in the selected tower box
 		sPaintLine.setARGB(255, 255, 255, 0);
 		sPaintLine.setStrokeWidth(5);
 
 		// set color and style of the range indicators
 		rangeIndicationPaint.setARGB(80, 80, 255, 80);
 		rangeIndicationPaint.setStyle(Paint.Style.FILL);
 		noRangeIndicationPaint.setARGB(80, 255, 0, 0);
 		noRangeIndicationPaint.setStyle(Paint.Style.FILL);
 
 		// set color of the grid showing where you can put new towers
 		gridpaint.setARGB(50,255,0,0);
 		gridpaint.setStyle(Paint.Style.FILL);
 
 		healthBarPaint.setStyle(Paint.Style.FILL);
 		snowPaint.setARGB(255, 159, 182, 205);
 		borderPaint.setARGB(255, 0, 0, 0);
 		borderPaint.setStyle(Paint.Style.STROKE);
 	}
 
 	public static int getSpeedMultiplier() {
 		return GAME_SPEED_MULTIPLIER;
 	}
 
 	private static void setSpeedMultiplier(int i) {
 		GAME_SPEED_MULTIPLIER = i;
 	}
 
 	/**
 	 * Called if you change the configuration like open the keypad.
 	 */
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 	}
 
 	/**
 	 * Called on creation of the SurfaceView.
 	 * Which could be on first start or relaunch.
 	 */
 	public void surfaceCreated(SurfaceHolder holder) {
 		if (!mGameThread.isAlive()) {
 			mGameThread = new GameThread(this);
 		}
 		mGameThread.setRunning(true);
 		mGameThread.start();
 	}
 
 	/**
 	 * Called if the SurfaceView should be destroyed.
 	 * We try to finish the game loop thread here.
 	 */
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.v("GamePanel","surfaceDestroyed");
 		boolean retry = true;
 		mGameThread.setRunning(false);
 		while (retry) {
 			try {
 				mGameThread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				// we will try it again and again...
 			}
 		}
 		Log.i("thread", "Thread terminated...");
 		// To prevent memory filled exception
 		mBitMapCache = new HashMap<Integer, Bitmap>();
 	}
 
 	
 }
