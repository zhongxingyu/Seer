 package se.chalmers.segway.scenes;
 
 import java.io.IOException;
 
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.LoopEntityModifier;
 import org.andengine.entity.modifier.ScaleModifier;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
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
 import se.chalmers.segway.game.LevelLoader;
 import se.chalmers.segway.managers.SceneManager;
 import se.chalmers.segway.managers.SceneManager.SceneType;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.Manifold;
 
 public class GameScene extends BaseScene implements IOnSceneTouchListener,
 		SensorEventListener {
 
 	/**
 	 * Variables
 	 */
 	private HUD gameHUD;
 	private Text finalScore;
 	private int score;
 	private PhysicsWorld physicsWorld;
 	private SensorManager sensorManager;
 
 	private float tiltSpeedX;
 	private float tiltSpeedY;
 
 	private boolean takeInput = false;
 	private LevelCompleteScene levelCompleteScene;
 
 	private boolean gameOverDisplayed = false;
 
 	private Player player;
 
 	private static final String TAG_ENTITY = "entity";
 	private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
 	private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
 	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
 
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1 = "platform1";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2 = "platform2";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3 = "platform3";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN = "coin";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
 	
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
		loadLevel(1);
 		setOnSceneTouchListener(this);
 		levelCompleteScene = new LevelCompleteScene(vbom);
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
 
 	private void createBackground() {
 		setBackground(new Background(Color.CYAN));
 	}
 
 	private void createHUD() {
 		gameHUD = new HUD();
 		camera.setHUD(gameHUD);
 	}
 	
 	private void createPlayer(){
 		player = new Player(50, 610, vbom, camera, physicsWorld) {
 			@Override
 			public void onDie() {
 				if (!gameOverDisplayed) {
 					levelCompleteScene.display(GameScene.this, camera);
 					addToScore((int) player.getX() / 20);
 					displayScoreAtGameOver();
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
 
 	private void createSensorManager() {
 		sensorManager = (SensorManager) activity
 				.getSystemService(Context.SENSOR_SERVICE);
 		sensorManager.registerListener(this,
 				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_GAME);
 	}
 
 	// Handles all code for loading levels
 	private void loadLevel(int levelID) {
 		final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);
 
 		final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0,
 				0.01f, 0.5f);
 
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
 
 /*		levelLoader
 				.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(
 						TAG_ENTITY) {
 					
 
 					public IEntity onLoadEntity(
 							final String pEntityName,
 							final IEntity pParent,
 							final Attributes pAttributes,
 							final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData)
 							throws IOException {
 						final int x = SAXUtils.getIntAttributeOrThrow(
 								pAttributes, TAG_ENTITY_ATTRIBUTE_X);
 						final int y = SAXUtils.getIntAttributeOrThrow(
 								pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
 						final String type = SAXUtils.getAttributeOrThrow(
 								pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
 
 						final Sprite levelObject;
 
 						// Cases for loading different objects
 						// Loads platform1
 						if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1)) {
 							levelObject = new Sprite(x, y,
 									resourcesManager.platform1_region, vbom);
 							PhysicsFactory.createBoxBody(physicsWorld,
 									levelObject, BodyType.StaticBody,
 									FIXTURE_DEF).setUserData("platform1");
 							// Loads coin
 						} else if (type
 								.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2)) {
 							levelObject = new Sprite(x, y,
 									resourcesManager.platform2_region, vbom);
 							final Body body = PhysicsFactory.createBoxBody(
 									physicsWorld, levelObject,
 									BodyType.StaticBody, FIXTURE_DEF);
 							body.setUserData("platform2");
 							physicsWorld
 									.registerPhysicsConnector(new PhysicsConnector(
 											levelObject, body, true, false));
 						} else if (type
 								.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3)) {
 							levelObject = new Sprite(x, y,
 									resourcesManager.platform3_region, vbom);
 							final Body body = PhysicsFactory.createBoxBody(
 									physicsWorld, levelObject,
 									BodyType.StaticBody, FIXTURE_DEF);
 							body.setUserData("platform3");
 							physicsWorld
 									.registerPhysicsConnector(new PhysicsConnector(
 											levelObject, body, true, false));
 						} else if (type
 								.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN)) {
 							levelObject = new Sprite(x, y,
 									resourcesManager.coin_region, vbom) {
 								@Override
 								protected void onManagedUpdate(
 										float pSecondsElapsed) {
 									super.onManagedUpdate(pSecondsElapsed);
 
 									if (player.collidesWith(this)) {
 										addToScore(10);
 										this.setVisible(false);
 										this.setIgnoreUpdate(true);
 									}
 								}
 							};
 							levelObject
 									.registerEntityModifier(new LoopEntityModifier(
 											new ScaleModifier(1, 1, 1.3f)));
 							// Loading player type objects
 						} else if (type
 								.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER)) {
 							player = new Player(x, y, vbom, camera,
 									physicsWorld) {
 								@Override
 								public void onDie() {
 									if (!gameOverDisplayed) {
 										levelCompleteScene.display(
 												GameScene.this, camera);
 										addToScore((int) player.getX() / 20);
 										displayScoreAtGameOver();
 									}
 								}
 							};
 							contactListener.setPlayer(player);
 							contactListener.setEngine(engine);
 							
 							levelObject = player;
 						} else {
 							throw new IllegalArgumentException();
 						}
 
 						levelObject.setCullingEnabled(true);
 
 						return levelObject;
 					}
 				});*/
 
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
 			tiltSpeedY = event.values[0];
 			final Vector2 tiltGravity = Vector2Pool.obtain(2*tiltSpeedX,
 					0);
 			player.setSpeed(tiltGravity);
 			Vector2Pool.recycle(tiltGravity);
 		}
 	}
 
 }
