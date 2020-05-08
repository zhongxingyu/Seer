 package com.matimdev.scene;
 
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
 import org.andengine.entity.text.TextOptions;
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.util.SAXUtils;
 import org.andengine.util.adt.align.HorizontalAlign;
 import org.andengine.util.adt.color.Color;
 import org.andengine.util.level.EntityLoader;
 import org.andengine.util.level.constants.LevelConstants;
 import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
 import org.andengine.util.level.simple.SimpleLevelLoader;
 import org.xml.sax.Attributes;
 
 import android.text.format.Time;
 import android.widget.Toast;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.Manifold;
 import com.matimdev.GameActivity;
 import com.matimdev.base.BaseScene;
 import com.matimdev.extras.LevelCompleteWindow;
 import com.matimdev.extras.LevelCompleteWindow.StarsCount;
 import com.matimdev.manager.SceneManager;
 import com.matimdev.manager.SceneManager.SceneType;
 import com.matimdev.object.EnemyOne;
 import com.matimdev.object.EnemyTwo;
 import com.matimdev.object.Kinoko;
 import com.matimdev.object.Player;
 
 public class GameScene extends BaseScene implements IOnSceneTouchListener
 {
 	private int score = 0;
 	
 	private HUD gameHUD;
 	private Text scoreText;
 	private PhysicsWorld physicsWorld;
 	private LevelCompleteWindow levelCompleteWindow;
 	
 	private static final String TAG_ENTITY = "entity";
 	private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
 	private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
 	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
 	
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1 = "platform1";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2 = "platform2";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3 = "platform3";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_GROUND = "ground";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN = "coin";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_ENEMY1 = "enemy1";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_ENEMY2 = "enemy2";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVEL_COMPLETE = "levelComplete";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DOKAN = "dokan";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_IPHONE = "iphone";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DEGITALCLOCK = "degitalClock";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DEGITALCLOCK2 = "degitalClock2";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DEGITALCLOCK3 = "degitalClock3";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DEGITALCLOCK4 = "degitalClock4";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DEGITALCLOCK5 = "degitalClock5";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DEGITALCLOCK6 = "degitalClock6";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_KINOKO = "kinoko";
 	
 	private Player player;
 	private EnemyOne enemy1;
 	
 	private Text gameOverText;
 	private boolean gameOverDisplayed = false;
 	private boolean gameCleard = false;
 	
 	private boolean firstTouch = false;
 	private Text iphoneCountDownText;
 	private Text degitalClockCountDownText;
 	private Text degitalClockCountDownText2;
 	private Text degitalClockCountDownText3;
 	private Text degitalClockCountDownText4;
 	private Text degitalClockCountDownText5;
 	private Text degitalClockCountDownText6;
 
 	private TimerHandler iphoneTimerHandler;
 	private TimerHandler degitalClockTimerHandler;
 	private TimerHandler degitalClockTimerHandler2;
 	private TimerHandler degitalClockTimerHandler3;
 	private TimerHandler degitalClockTimerHandler4;
 	private TimerHandler degitalClockTimerHandler5;
 	private TimerHandler degitalClockTimerHandler6;
 
 	
 	@Override
 	public void createScene()
 	{
 		createBackground();
 		createHUD();
 		createPhysics();
 		loadLevel(1);
 		createGameOverText();
 		
 		levelCompleteWindow = new LevelCompleteWindow(vbom);
 		
 		setOnSceneTouchListener(this); 
 	}
 
 	@Override
 	public void onBackKeyPressed()
 	{
 		SceneManager.getInstance().loadMenuScene(engine);
 	}
 
 	@Override
 	public SceneType getSceneType()
 	{
 		return SceneType.SCENE_GAME;
 	}
 
 	@Override
 	public void disposeScene()
 	{
 		camera.setHUD(null);
 		camera.setChaseEntity(null); //TODO
 		camera.setCenter(400, 240);
 		
 		// TODO code responsible for disposing scene
 		// removing all game scene objects.
 	}
 	
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent)
 	{
 		if (pSceneTouchEvent.isActionDown())
 		{
 			if (!firstTouch)
 			{
 				player.setRunning();
 				firstTouch = true;
 			}
 			else
 			{
 				//showToast("Jump!");
 				player.jump();
 			}
 		}
 		return false;
 	}
 	
 	private void loadLevel(int levelID)
 	{
 		final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);
 		
 		final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
 		
 		levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
 		{
 			public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException 
 			{
 				final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
 				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
 				
 				camera.setBounds(0, 0, width, height); // here we set camera bounds
 				camera.setBoundsEnabled(true);
 
 				return GameScene.this;
 			}
 		});
 		
 		levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(TAG_ENTITY)
 		{
 			public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException
 			{
 				final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
 				final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
 				final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
 				
 				final Sprite levelObject;
 				
 				if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1))
 				{
 					levelObject = new Sprite(x, y, resourcesManager.platform1_region, vbom);
 					PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("platform1");
 				} 
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2))
 				{
 					levelObject = new Sprite(x, y, resourcesManager.platform2_region, vbom);
 					final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
 					body.setUserData("platform2");
 					physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3))
 				{
 					levelObject = new Sprite(x, y, resourcesManager.platform3_region, vbom);
 					final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
 					body.setUserData("platform3");
 					physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN))
 				{
 					levelObject = new Sprite(x, y, resourcesManager.coin_region, vbom)
 					{
 						@Override
 						protected void onManagedUpdate(float pSecondsElapsed) 
 						{
 							super.onManagedUpdate(pSecondsElapsed);
 
 							if (player.collidesWith(this))
 							{
 								addToScore(1);
 								this.setVisible(false);
 								this.setIgnoreUpdate(true);
 							}
 						}
 					};
 					levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
 				}	
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER))
 				{
 					player = new Player(x, y, vbom, camera, physicsWorld)
 					{
 						@Override
 						public void onDie()
 						{
 							if (!gameOverDisplayed)
 							{
 								displayGameOverText();
 								//showAnalogClock();
 							}
 						}
 					};
 					levelObject = player;
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVEL_COMPLETE))
 				{
 					levelObject = new Sprite(x, y, resourcesManager.complete_stars_region, vbom)
 					{
 						@Override
 						protected void onManagedUpdate(float pSecondsElapsed) 
 						{
 							super.onManagedUpdate(pSecondsElapsed);
 
 							if (player.collidesWith(this))
 							{
 								gameCleard = true;
 								StarsCount count;
 								if (score == 1 || score == 0) {
 									count = StarsCount.ONE;
 								} else if (score == 2) {
 									count = StarsCount.TWO;
 								} else {
 									count = StarsCount.THREE;
 								}
 								levelCompleteWindow.display(count, GameScene.this, camera);
 								this.setVisible(false);
 								this.setIgnoreUpdate(true);
 							}
 						}
 					};
 					levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_GROUND)) {
 					levelObject = new Sprite(x, y, resourcesManager.ground_region, vbom);
 					PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("ground");
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DOKAN)) {
 					levelObject = new Sprite(x, y, resourcesManager.dokan_region, vbom);
 					PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("dokan");
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_IPHONE)) {
 					levelObject = new Sprite(x, y, resourcesManager.iphone_region, vbom);
 					// Get current time
 					String currentTime = getCurrentTime();
 					iphoneCountDownText = new Text(73, 33, resourcesManager.timeFont, currentTime, currentTime.length(), new TextOptions(HorizontalAlign.CENTER), vbom);
 					levelObject.attachChild(iphoneCountDownText);
 					iphoneTimerHandler = new TimerHandler(1, true, new ITimerCallback() {
 					    public void onTimePassed(TimerHandler pTimerHandler) {
 					    	iphoneCountDownText.setText(getCurrentTime());
 					    }
 					});
 					levelObject.registerUpdateHandler(iphoneTimerHandler);
 					PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("iphone");
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DEGITALCLOCK)) {
 					levelObject = new Sprite(x, y, resourcesManager.degitalClock_region, vbom);
 					// Get current time
 					String currentTime = getCurrentTime();
 					degitalClockCountDownText = new Text(92, 52, resourcesManager.timeFont, currentTime, currentTime.length(), new TextOptions(HorizontalAlign.CENTER), vbom);
 					levelObject.attachChild(degitalClockCountDownText);
 					TimerHandler degitalClockTimerHandler = new TimerHandler(1, true, new ITimerCallback() {
 					    public void onTimePassed(TimerHandler pTimerHandler) {
 					    	degitalClockCountDownText.setText(getCurrentTime());
 					    }
 					});
 					levelObject.registerUpdateHandler(degitalClockTimerHandler);
 					PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("degitalClock");
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DEGITALCLOCK2)) {
 					levelObject = new Sprite(x, y, resourcesManager.degitalClock_region, vbom);
 					// Get current time
 					String currentTime = getCurrentTime();
 					degitalClockCountDownText2 = new Text(92, 52, resourcesManager.timeFont, currentTime, currentTime.length(), new TextOptions(HorizontalAlign.CENTER), vbom);
 					levelObject.attachChild(degitalClockCountDownText2);
 					TimerHandler degitalClockTimerHandler = new TimerHandler(1, true, new ITimerCallback() {
 					    public void onTimePassed(TimerHandler pTimerHandler) {
 					    	degitalClockCountDownText2.setText(getCurrentTime());
 					    }
 					});
 					levelObject.registerUpdateHandler(degitalClockTimerHandler);
 					PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("degitalClock");
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DEGITALCLOCK3)) {
 					levelObject = new Sprite(x, y, resourcesManager.degitalClock_region, vbom);
 					// Get current time
 					String currentTime = getCurrentTime();
 					degitalClockCountDownText3 = new Text(92, 52, resourcesManager.timeFont, currentTime, currentTime.length(), new TextOptions(HorizontalAlign.CENTER), vbom);
 					levelObject.attachChild(degitalClockCountDownText3);
 					TimerHandler degitalClockTimerHandler = new TimerHandler(1, true, new ITimerCallback() {
 					    public void onTimePassed(TimerHandler pTimerHandler) {
 					    	degitalClockCountDownText3.setText(getCurrentTime());
 					    }
 					});
 					levelObject.registerUpdateHandler(degitalClockTimerHandler);
 					PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("degitalClock");
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DEGITALCLOCK4)) {
 					levelObject = new Sprite(x, y, resourcesManager.degitalClock_region, vbom);
 					// Get current time
 					String currentTime = getCurrentTime();
 					degitalClockCountDownText4 = new Text(92, 52, resourcesManager.timeFont, currentTime, currentTime.length(), new TextOptions(HorizontalAlign.CENTER), vbom);
 					levelObject.attachChild(degitalClockCountDownText4);
 					TimerHandler degitalClockTimerHandler = new TimerHandler(1, true, new ITimerCallback() {
 					    public void onTimePassed(TimerHandler pTimerHandler) {
 					    	degitalClockCountDownText4.setText(getCurrentTime());
 					    }
 					});
 					levelObject.registerUpdateHandler(degitalClockTimerHandler);
 					PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("degitalClock");
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DEGITALCLOCK5)) {
 					levelObject = new Sprite(x, y, resourcesManager.degitalClock_region, vbom);
 					// Get current time
 					String currentTime = getCurrentTime();
 					degitalClockCountDownText5 = new Text(92, 52, resourcesManager.timeFont, currentTime, currentTime.length(), new TextOptions(HorizontalAlign.CENTER), vbom);
 					levelObject.attachChild(degitalClockCountDownText5);
 					TimerHandler degitalClockTimerHandler = new TimerHandler(1, true, new ITimerCallback() {
 					    public void onTimePassed(TimerHandler pTimerHandler) {
 					    	degitalClockCountDownText5.setText(getCurrentTime());
 					    }
 					});
 					levelObject.registerUpdateHandler(degitalClockTimerHandler);
 					PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("degitalClock");
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_DEGITALCLOCK6)) {
 					levelObject = new Sprite(x, y, resourcesManager.degitalClock_region, vbom);
 					// Get current time
 					String currentTime = getCurrentTime();
 					degitalClockCountDownText6 = new Text(92, 52, resourcesManager.timeFont, currentTime, currentTime.length(), new TextOptions(HorizontalAlign.CENTER), vbom);
 					levelObject.attachChild(degitalClockCountDownText6);
 					TimerHandler degitalClockTimerHandler = new TimerHandler(1, true, new ITimerCallback() {
 					    public void onTimePassed(TimerHandler pTimerHandler) {
 					    	degitalClockCountDownText6.setText(getCurrentTime());
 					    }
 					});
 					levelObject.registerUpdateHandler(degitalClockTimerHandler);
 					PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("degitalClock");
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_ENEMY1)) {
 					levelObject = new EnemyOne(x, y, vbom, camera, physicsWorld){
 						@Override
 						public void onDie()
 						{
 						}
 						
 						@Override
 						protected void onManagedUpdate(float pSecondsElapsed) 
 						{
 							super.onManagedUpdate(pSecondsElapsed);
 
 							if (player.collidesWith(this))
 							{
 								player.setVisible(false);
 								if (!gameOverDisplayed)
 								{
 									player.die();
 									displayGameOverText();
 								}
 							}
 						}
 					};
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_ENEMY2)) {
 					levelObject = new EnemyTwo(x, y, vbom, camera, physicsWorld){
 						@Override
 						public void onDie()
 						{
 						}
 						
 						@Override
 						protected void onManagedUpdate(float pSecondsElapsed) 
 						{
 							super.onManagedUpdate(pSecondsElapsed);
 
 							if (player.collidesWith(this))
 							{
 								player.setVisible(false);
 								if (!gameOverDisplayed)
 								{
 									player.die();
 									displayGameOverText();
 								}
 							}
 						}
 					};
 				}
 				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_KINOKO)) {
 					levelObject = new Sprite(x, y, resourcesManager.kinoko_region, vbom)
 					{
 						@Override
 						protected void onManagedUpdate(float pSecondsElapsed) 
 						{
 							super.onManagedUpdate(pSecondsElapsed);
 
 							if (player.collidesWith(this))
 							{
 								player.big(resourcesManager.big_player_region);
 								this.setVisible(false);
 								this.setIgnoreUpdate(true);
 							}
 						}
 					};
 					levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
 				}
 				else
 				{
 					throw new IllegalArgumentException();
 				}
 
 				levelObject.setCullingEnabled(true);
 
 				return levelObject;
 			}
 		});
 
 		levelLoader.loadLevelFromAsset(activity.getAssets(), "level/" + levelID + ".lvl");
 	}
 	
 	private String getCurrentTime() {
 		Time time = new Time("Asia/Tokyo");
 		time.setToNow();
 		return zero(time.hour) + ":" + zero(time.minute) + ":" + zero(time.second);
 	}
 	
 	private String zero(int num)
     {
       String number=( num < 10) ? ("0"+num) : (""+num);
       return number; //Add leading zero if needed
     }
 	
 	private void createGameOverText()
 	{
 		gameOverText = new Text(0, 0, resourcesManager.font, "Game Over!", vbom);
 	}
 	
 	private void displayGameOverText()
 	{
 		if (!gameCleard) {
 			camera.setChaseEntity(null);
 			gameOverText.setPosition(camera.getCenterX(), camera.getCenterY());
 			attachChild(gameOverText);
 			gameOverDisplayed = true;
 		}
 	}
 	
 	private void createHUD()
 	{
 		gameHUD = new HUD();
 		
 		scoreText = new Text(20, 420, resourcesManager.font, "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
 		scoreText.setAnchorCenter(0, 0);	
 		scoreText.setText("Coin x 0");
 		gameHUD.attachChild(scoreText);
 		
 		camera.setHUD(gameHUD);
 	}
 	
 	private void createBackground()
 	{
 		setBackground(new Background(Color.WHITE));
 	}
 	
 	private void addToScore(int i)
 	{
 		score += i;
 		scoreText.setText("Coin x " + score);
 	}
 	
 	private void createPhysics()
 	{
 		physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -17), false); 
 		physicsWorld.setContactListener(contactListener());
 		registerUpdateHandler(physicsWorld);
 	}
 	
 	private void showToast(final String text) { 
 	    getBaseActivity().runOnUiThread(new Runnable() {
 	    	public void run() {
 	        Toast.makeText(getBaseActivity(), text, Toast.LENGTH_SHORT).show();
 	        }
 	    });
 	}
 	
 	private void showAnalogClock() { 
 	    GameActivity activity = (GameActivity)getBaseActivity();
 	    activity.showAnalogClock();
 	}
 	
 	// ---------------------------------------------
 	// INTERNAL CLASSES
 	// ---------------------------------------------
 
 	private ContactListener contactListener()
 	{
 		ContactListener contactListener = new ContactListener()
 		{
 			public void beginContact(Contact contact)
 			{
 				final Fixture x1 = contact.getFixtureA();
 				final Fixture x2 = contact.getFixtureB();
 
 				if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
 				{
 					if (x2.getBody().getUserData().equals("player"))
 					{
 						player.increaseFootContacts();
 					}
 					
 					if (x1.getBody().getUserData().equals("platform2") && x2.getBody().getUserData().equals("player"))
 					{
 						engine.registerUpdateHandler(new TimerHandler(0.2f, new ITimerCallback()
 						{									
 						    public void onTimePassed(final TimerHandler pTimerHandler)
 						    {
 						    	pTimerHandler.reset();
 						    	engine.unregisterUpdateHandler(pTimerHandler);
 						    	x1.getBody().setType(BodyType.DynamicBody);
 						    }
 						}));
 					}
 					
 					if (x1.getBody().getUserData().equals("platform3") && x2.getBody().getUserData().equals("player"))
 					{
 						x1.getBody().setType(BodyType.DynamicBody);
 					}
 				}
 			}
 
 			public void endContact(Contact contact)
 			{
 				final Fixture x1 = contact.getFixtureA();
 				final Fixture x2 = contact.getFixtureB();
 
 				if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
 				{
 					if (x2.getBody().getUserData().equals("player"))
 					{
 						player.decreaseFootContacts();
 					}
 				}
 			}
 
 			public void preSolve(Contact contact, Manifold oldManifold)
 			{
 
 			}
 
 			public void postSolve(Contact contact, ContactImpulse impulse)
 			{
 
 			}
 		};
 		return contactListener;
 	}
 }
