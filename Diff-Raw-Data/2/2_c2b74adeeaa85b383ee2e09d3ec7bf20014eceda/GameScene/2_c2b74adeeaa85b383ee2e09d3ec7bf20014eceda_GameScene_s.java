 package se.chalmers.segway.scenes;
 
 import java.io.IOException;
 
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.particle.SpriteParticleSystem;
 import org.andengine.entity.particle.emitter.PointParticleEmitter;
 import org.andengine.entity.particle.initializer.AlphaParticleInitializer;
 import org.andengine.entity.particle.initializer.BlendFunctionParticleInitializer;
 import org.andengine.entity.particle.initializer.ExpireParticleInitializer;
 import org.andengine.entity.particle.initializer.VelocityParticleInitializer;
 import org.andengine.entity.particle.modifier.AlphaParticleModifier;
 import org.andengine.entity.particle.modifier.ScaleParticleModifier;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.extension.physics.box2d.util.Vector2Pool;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.util.SAXUtils;
 import org.andengine.util.adt.color.Color;
 import org.andengine.util.level.EntityLoader;
 import org.andengine.util.level.constants.LevelConstants;
 import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
 import org.andengine.util.level.simple.SimpleLevelLoader;
 import org.xml.sax.Attributes;
 
 import se.chalmers.segway.entities.Player;
 import se.chalmers.segway.game.PlayerContact;
 import se.chalmers.segway.game.Upgrades;
 import se.chalmers.segway.scenes.ParallaxLayer.ParallaxEntity;
 import se.chalmers.segway.scenes.SceneManager.SceneType;
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.opengl.GLES20;
 
 import com.badlogic.gdx.math.Vector2;
 
 public class GameScene extends BaseScene implements IOnSceneTouchListener,
 		SensorEventListener {
 
 	/**
 	 * Variables
 	 */
 	private HUD gameHUD;
 	private Text finalScore;
 	private Text tip;
 	private int score;
 	private int currentLvl;
 	private PhysicsWorld physicsWorld;
 	private SensorManager sensorManager;
 
 	private float tiltSpeedX;
 
 	private boolean takeInput = false;
 	private LevelCompleteScene levelCompleteScene;
 	private DeathScene deathScene;
 
 	private boolean gameOverDisplayed = false;
 	private boolean boost = false;
 	private int boostAmount = 20;
 
 	private Player player;
 	private PlayerContact contactListener;
 
 	private long stopWatchTime = 0;
 	private ParallaxLayer parallaxLayer;
 
 	private PointParticleEmitter particleEmitter;
 	private SpriteParticleSystem particleSystem;
 
 	private TimerHandler boostTimer = new TimerHandler(0.1f,
 			new ITimerCallback() {
 				public void onTimePassed(final TimerHandler pTimerHandler) {
 					pTimerHandler.reset();
 
 					if (boostAmount <= 0) {
 						engine.unregisterUpdateHandler(boostTimer);
 						Text boostMessage = new Text(camera.getCenterX() + 80,
 								camera.getCenterY() + 200 / 2,
 								resourcesManager.tipFont, "Out of boost!", vbom);
 						gameHUD.attachChild(boostMessage);
 						boost = false;
 					} else {
 						System.out.println("Boosting");
 						System.out.println("Boost left: " + boostAmount);
 						boostAmount--;
 					}
 				}
 			});
 	protected int level_width;
 
 	/**
 	 * Methods
 	 */
 	@Override
 	public void createScene() {
 		createPhysics();
 		createSensorManager();
 		createPlayer();
 		createHUD();
 		setOnSceneTouchListener(this);
 		createLocalScenes();
 		createBackground();
 		initTrail();
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		SceneManager.getInstance().loadMenuScene(engine);
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_GAME;
 	}
 
 	@Override
 	public void disposeScene() {
 		camera.setHUD(null);
 		camera.setCenter(400, 240);
 		camera.setChaseEntity(null);
 
 		// TODO code responsible for disposing scene
 		// removing all game scene objects.
 	}
 
 	private void initTrail() {
 		this.particleEmitter = new PointParticleEmitter(player.getX(),
 				player.getY());
 		this.particleSystem = new SpriteParticleSystem(particleEmitter, 30, 30,
 				120, resourcesManager.player_region, vbom);
 
 		particleSystem
 				.addParticleInitializer(new AlphaParticleInitializer<Sprite>(0));
 		particleSystem
 				.addParticleInitializer(new BlendFunctionParticleInitializer<Sprite>(
 						GLES20.GL_SRC_ALPHA, GLES20.GL_ONE));
 		particleSystem
 				.addParticleInitializer(new VelocityParticleInitializer<Sprite>(
 						0));
 		particleSystem
 				.addParticleInitializer(new ExpireParticleInitializer<Sprite>(2));
 		particleSystem.addParticleModifier(new ScaleParticleModifier<Sprite>(0,
 				2, 1, .5f));
 		particleSystem.addParticleModifier(new AlphaParticleModifier<Sprite>(0,
 				2, 1, 0));
 		this.attachChild(particleSystem);
 	}
 
 	public void showLevelComplete() {
 		if (!gameOverDisplayed) {
 			this.detachChild(levelCompleteScene);
 			levelCompleteScene.display(GameScene.this, camera);
 			addToScore((int) player.getX() / 20);
 			displayScoreAtGameOver();
 		}
 	}
 
 	private void createBackground() {
 		parallaxLayer = new ParallaxLayer(camera, true, 10000);
 		Sprite back = new Sprite(0, camera.getCenterY(), camera.getWidth(),
 				camera.getHeight(), resourcesManager.backgroundBackRegion, vbom);
 
 		Sprite front = new Sprite(0, camera.getCenterY(),
 				resourcesManager.backgroundFrontRegion, vbom);
 		Sprite front2 = new Sprite(0, camera.getCenterY(),
 				resourcesManager.backgroundFront2Region, vbom);
 
//		parallaxLayer.attachParallaxEntity(new ParallaxEntity(6, back, false,1));
 //		parallaxLayer.attachParallaxEntity(new ParallaxEntity(3, front, true));
 //		parallaxLayer.attachParallaxEntity(new ParallaxEntity(1, front2, true));
 
 		setBackground(new Background(Color.CYAN));
 		this.attachChild(parallaxLayer);
 	}
 
 	private void createLocalScenes() {
 		levelCompleteScene = new LevelCompleteScene(vbom);
 		deathScene = new DeathScene(vbom);
 	}
 
 	private void createHUD() {
 		gameHUD = new HUD();
 		camera.setHUD(gameHUD);
 		tip = new Text(camera.getCenterX() + 80, camera.getCenterY() + 200 / 2,
 				resourcesManager.tipFont, "Tap screen to start!", vbom);
 		gameHUD.attachChild(tip);
 	}
 
 	private void createPlayer() {
 		player = new Player(0, 0, vbom, camera, physicsWorld) {
 			@Override
 			public void onDie() {
 				if (!gameOverDisplayed) {
 					stopTimerAndReturnTime();
 					deathScene.display(GameScene.this, camera);
 					camera.setChaseEntity(null);
 					gameOverDisplayed = true;
 					/*
 					 * levelCompleteScene.display(GameScene.this, camera);
 					 * addToScore((int) player.getX() / 20);
 					 * displayScoreAtGameOver();
 					 */
 				}
 			}
 		};
 		contactListener.setPlayer(player);
 		contactListener.setEngine(engine);
 	}
 
 	private void displayScoreAtGameOver() {
 
 		camera.setChaseEntity(null);
 		// Score is calculated: 10*amount of cookies + 1000/1 + time in seconds
 		score = (int) (score + 1000 / (1 + stopTimerAndReturnTime() / 1000));
 		finalScore = new Text(300, 80, resourcesManager.fancyFont, "Score: "
 				+ score, vbom);
 		levelCompleteScene.attachChild(finalScore);
 		gameOverDisplayed = true;
 	}
 
 	/**
 	 * Adds a value to the current score
 	 * 
 	 * @param i
 	 *            the value to add.
 	 */
 	public void addToScore(int i) {
 		score += i;
 	}
 	
 	public void addToBoost(int i) {
 		boostAmount += i;
 	}
 
 	private void createPhysics() {
 		physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -17), false);
 		physicsWorld.setContactListener(contactListener());
 		registerUpdateHandler(physicsWorld);
 	}
 
 	private void createSensorManager() {
 		sensorManager = (SensorManager) activity
 				.getSystemService(Context.SENSOR_SERVICE);
 		sensorManager.registerListener(this,
 				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_GAME);
 	}
 
 	// Handles all code for loading levels
 	public void loadLevel(int levelID) {
 		final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);
 
 		levelLoader
 				.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(
 						LevelConstants.TAG_LEVEL) {
 
 					public IEntity onLoadEntity(
 							final String pEntityName,
 							final IEntity pParent,
 							final Attributes pAttributes,
 							final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData)
 							throws IOException {
 						final int width = SAXUtils.getIntAttributeOrThrow(
 								pAttributes,
 								LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
 						level_width = width;
 						final int height = SAXUtils.getIntAttributeOrThrow(
 								pAttributes,
 								LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
 
 						camera.setBounds(0, 0, width, height); // here we set
 																// camera bounds
 						camera.setBoundsEnabled(true);
 
 						return GameScene.this;
 					}
 				});
 
 		levelLoader.registerEntityLoader(new LevelLoader(physicsWorld, player,
 				this));
 
 		levelLoader.loadLevelFromAsset(activity.getAssets(), "level/" + levelID
 				+ ".lvl");
 	}
 
 	private PlayerContact contactListener() {
 		contactListener = new PlayerContact();
 		return contactListener;
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		if (pSceneTouchEvent.isActionDown()) {
 			if (takeInput) {
 				if (gameOverDisplayed) {
 					SceneManager.getInstance().loadMenuScene(engine);
 					startTimer();
 				} else if (pSceneTouchEvent.getX() > camera.getCenterX()) {
 					player.jump();
 				} else if (boostAmount > 0) {
 					boost = true;
 					engine.registerUpdateHandler(boostTimer);
 				}
 			}
 		} else if (pSceneTouchEvent.isActionUp()) {
 			boost = false;
 			engine.unregisterUpdateHandler(boostTimer);
 		} else {
 			takeInput = true;
 			tip.setVisible(false);
 		}
 		return false;
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		int multiplier = 1;
 		if (takeInput) {
 			tiltSpeedX = event.values[1];
 
 			if (Math.abs(tiltSpeedX) > 3) {
 				tiltSpeedX = Math.signum(tiltSpeedX) * 3;
 			}
 
 			if (boost == true) {
 				if (Upgrades.RocketBoost.isActivated()) {
 					multiplier = 20;
 				} else {
 					multiplier = 10;
 				}
 				particleEmitter.setCenter(player.getX(), player.getY());
 			}
 
 			player.setRotation(tiltSpeedX * 18f);
 
 			final Vector2 tiltGravity = Vector2Pool.obtain(2 * multiplier
 					* tiltSpeedX, 0);
 
 			player.setSpeed(tiltGravity);
 			Vector2Pool.recycle(tiltGravity);
 		}
 	}
 
 	/**
 	 * Starts the timer.
 	 */
 	private void startTimer() {
 		stopWatchTime = System.currentTimeMillis();
 	}
 
 	/**
 	 * Stops the timer and returns the amount of time it was running in
 	 * milliseconds
 	 * 
 	 * @return time in millseconds
 	 */
 	private long stopTimerAndReturnTime() {
 		long temp = System.currentTimeMillis() - stopWatchTime;
 		stopWatchTime = 0;
 		return temp;
 	}
 }
