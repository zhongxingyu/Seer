 package se.chalmers.segway.scenes;
 
 import java.io.IOException;
 
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
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
 import se.chalmers.segway.scenes.SceneManager.SceneType;
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 
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
 
 	private Player player;
 	private PlayerContact contactListener;
 
 	/**
 	 * Methods
 	 */
 	@Override
 	public void createScene() {
 		createBackground();
 		createHUD();
 		createPhysics();
 		createSensorManager();
 		createPlayer();
 		//TODO: Temporary fix, should be retrieved from a manager.
 		//currentLvl = 4;
 		
 		setOnSceneTouchListener(this);
 		playMusic();
 		createLocalScenes();
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
 		this.resourcesManager.music2.pause();
 		this.resourcesManager.music.resume();
 		camera.setHUD(null);
 		camera.setCenter(400, 240);
 		camera.setChaseEntity(null);
 		// TODO code responsible for disposing scene
 		// removing all game scene objects.
 	}
 	
 	public void showLevelComplete(){
 		this.detachChild(levelCompleteScene);
 		levelCompleteScene.display(GameScene.this, camera);
		addToScore((int) player.getX() / 20);
 		displayScoreAtGameOver();
 	}
 
 	private void createBackground() {
 		setBackground(new Background(Color.CYAN));
 	}
 
 	private void createLocalScenes() {
 		levelCompleteScene = new LevelCompleteScene(vbom);
 		deathScene = new DeathScene(vbom);
 	}
 
 	private void createHUD() {
 		gameHUD = new HUD();
 		camera.setHUD(gameHUD);
 		tip = new Text(camera.getCenterX()+80, camera.getCenterY()+200 / 2,
 				resourcesManager.tipFont, "Tap screen to start!", vbom);
 		gameHUD.attachChild(tip);
 	}
 
 	private void createPlayer() {
 		player = new Player(50, 5200, vbom, camera, physicsWorld) {
 			@Override
 			public void onDie() {
 				if (!gameOverDisplayed) {
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
		finalScore = new Text(camera.getCenterX(), camera.getCenterY() / 2,
 				resourcesManager.fancyFont, "Score: " + score, vbom);
		attachChild(finalScore);
 		gameOverDisplayed = true;
 	}
 
 	private void addToScore(int i) {
 		score += i;
 	}
 
 	private void createPhysics() {
 		physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -17), false);
 		physicsWorld.setContactListener(contactListener());
 		registerUpdateHandler(physicsWorld);
 	}
 
 	private void playMusic() {
 		if (!this.resourcesManager.music2.isPlaying() && currentLvl == 4) {
 			this.resourcesManager.music2.play();
 			this.resourcesManager.music.pause();
 		}
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
		;
 
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
 						final int height = SAXUtils.getIntAttributeOrThrow(
 								pAttributes,
 								LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
 
 						camera.setBounds(0, 0, width, height); // here we set
 																// camera bounds
 						camera.setBoundsEnabled(true);
 
 						return GameScene.this;
 					}
 				});
 
 		levelLoader.registerEntityLoader(new LevelLoader(physicsWorld, player));
 
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
 				} else {
 					player.jump();
 				}
 			}
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
 		if (takeInput) {
 			tiltSpeedX = event.values[1];
 			
 			if (Math.abs(tiltSpeedX) > 3 ) {
 				tiltSpeedX = Math.signum(tiltSpeedX)*3;
 			}
 			
 			player.setRotation(tiltSpeedX * 18f);
 			
 			
 			final Vector2 tiltGravity = Vector2Pool.obtain(2*tiltSpeedX, 0);
 
 			player.setSpeed(tiltGravity);
 			Vector2Pool.recycle(tiltGravity);
 		}
 	}
 
 }
