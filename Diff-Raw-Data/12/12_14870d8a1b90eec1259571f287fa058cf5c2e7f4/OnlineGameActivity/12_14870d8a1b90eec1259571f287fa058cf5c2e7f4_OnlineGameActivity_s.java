 package com.angrykings.activities;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import com.angrykings.*;
 import com.angrykings.activities.MainActivity;
 import com.angrykings.cannons.Cannon;
 import com.angrykings.cannons.Cannonball;
 import com.angrykings.castles.Castle;
 import com.angrykings.kings.King;
 import com.angrykings.maps.BasicMap;
 import com.angrykings.utils.ServerJSONBuilder;
 import com.badlogic.gdx.math.Vector2;
 import de.tavendo.autobahn.WebSocketConnection;
 import org.andengine.engine.camera.ZoomCamera;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.RepeatingSpriteBackground;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.input.touch.detector.PinchZoomDetector;
 import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
 import org.andengine.input.touch.detector.ScrollDetector;
 import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
 import org.andengine.input.touch.detector.SurfaceScrollDetector;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.ui.activity.BaseGameActivity;
 import org.andengine.util.debug.Debug;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * OnlineGameActivity
  *
  * @author Shivan Taher <zn31415926535@gmail.com>
  * @date 06.06.13
  */
 public class OnlineGameActivity extends BaseGameActivity
 		implements IOnSceneTouchListener, IScrollDetectorListener, IPinchZoomDetectorListener {
 
 	private GameContext gc;
 	private Handler handler;
 	private GameHUD hud;
 
 	//
 	// Textures
 	//
 
 	private TiledTextureRegion grassTexture;
 	private TextureRegion cannonTexture;
 	private TextureRegion wheelTexture;
 	private TextureRegion ballTexture;
 	private TiledTextureRegion skyTexture;
 	private TiledTextureRegion aimButtonTexture;
 	private TiledTextureRegion whiteFlagButtonTexture;
 	private TiledTextureRegion stoneTexture;
 	private TiledTextureRegion kingTexture1;
 	private TiledTextureRegion kingTexture2;
 	private TextureRegion roofTexture;
 	private TextureRegion woodTexture;
 	private Font statusFont;
 	private Font playerNameFont;
 
 	//
 	// Game Objects
 	//
 
 	private Cannon cannon;
 	private Cannon enemyCannon;
 	private RepeatingSpriteBackground skySprite;
 	private Castle leftCastle, rightCastle;
 	private King leftKing, rightKing;
 
 	//
 	// Navigation Attributes
 	//
 
 	private SurfaceScrollDetector scrollDetector;
 	private PinchZoomDetector pinchZoomDetector;
 	private float pinchZoomStartedCameraZoomFactor;
 	boolean isAiming = true;
 
 	//
 	// Network
 	//
 
 	private static String JSON_LOSE;
 	private ServerConnection serverConnection;
 	private WebSocketConnection webSocketConnection;
 	private int round;
 	boolean turnSent;
 	boolean receivedEndTurn;
 	int aimX, aimY;
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		gc = GameContext.getInstance();
 		handler = new Handler();
 
 		ZoomCamera camera = new ZoomCamera(0, 0, GameConfig.CAMERA_WIDTH, GameConfig.CAMERA_HEIGHT);
 
 		camera.setZoomFactor(GameConfig.CAMERA_STARTUP_ZOOM);
 		camera.setBounds(
 				GameConfig.CAMERA_MIN_X, GameConfig.CAMERA_MIN_Y,
 				GameConfig.CAMERA_MAX_X, GameConfig.CAMERA_MAX_Y
 		);
 		camera.setBoundsEnabled(true);
 
 		gc.setCamera(camera);
 
 		OnlineGameActivity.JSON_LOSE = new ServerJSONBuilder().create(Action.Client.LOSE).build();
 
 		this.serverConnection = ServerConnection.getInstance();
 		this.webSocketConnection = this.serverConnection.getConnection();
 
 		return new EngineOptions(
 				true,
 				ScreenOrientation.LANDSCAPE_FIXED,
 				new RatioResolutionPolicy(GameConfig.CAMERA_WIDTH, GameConfig.CAMERA_HEIGHT),
 				camera
 		);
 	}
 
 	@Override
 	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		//
 		// map textures
 		//
 
 		BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 50, 393, TextureOptions.BILINEAR);
 		this.grassTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, this, "grass.png", 0, 0, 1, 1); // 32x32
 		textureAtlas.load();
 
 
 		this.skySprite = new RepeatingSpriteBackground(
 				GameConfig.CAMERA_WIDTH,
 				GameConfig.CAMERA_HEIGHT, this.getTextureManager(),
 				AssetBitmapTextureAtlasSource.create(this.getAssets(), "gfx/sky.png"),
 				this.getVertexBufferObjectManager()
 		);
 
 		//
 		// cannon textures
 		//
 
 		textureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 256, 72, TextureOptions.BILINEAR);
 		this.cannonTexture = BitmapTextureAtlasTextureRegionFactory .createFromAsset(textureAtlas, this, "cannon.png", 0, 0);
 		textureAtlas.load();
 
 		textureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 128, 128, TextureOptions.BILINEAR);
 		this.wheelTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "wheel.png", 0, 0);
 		textureAtlas.load();
 
 		textureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 44, 44, TextureOptions.BILINEAR);
 		this.ballTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "ball.png", 0, 0);
 		textureAtlas.load();
 
 		//
 		// hud textures
 		//
 
 		textureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 128, 64, TextureOptions.BILINEAR);
 		this.aimButtonTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, this, "aim_button.png", 0, 0, 2, 1);
 		textureAtlas.load();
 
 		textureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 64, 64, TextureOptions.BILINEAR);
 		this.whiteFlagButtonTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, this, "white_flag_button.png", 0, 0, 1, 1);
 		textureAtlas.load();
 
 		FontFactory.setAssetBasePath("font/");
 
 		final ITexture statusFontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
 		this.statusFont = FontFactory.createFromAsset(this.getFontManager(), statusFontTexture, this.getAssets(), "Plok.ttf", 22.0f, true, Color.BLACK);
 		this.statusFont.load();
 
 		final ITexture playerNameFontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
 		this.playerNameFont = FontFactory.createFromAsset(this.getFontManager(), playerNameFontTexture, this.getAssets(), "Plok.ttf", 16.0f, true, Color.BLACK);
 		this.playerNameFont.load();
 
 		//
 		// castle textures
 		//
 
 		textureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 384, 128, TextureOptions.BILINEAR);
 		this.stoneTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, this, "stones.png", 0, 0, 3, 1);
 		textureAtlas.load();
 
 		textureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 128, 128, TextureOptions.BILINEAR);
 		this.roofTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "roof.png", 0, 0);
 		textureAtlas.load();
 
 		textureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 409, 50, TextureOptions.BILINEAR);
 		this.woodTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "wood.png", 0, 0);
 		textureAtlas.load();
 		
 		//
 		// king textures
 		//
 		textureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 350, 325, TextureOptions.BILINEAR);
 		this.kingTexture1 = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, this, "green_king.png", 0, 0, 2, 1);
 		textureAtlas.load();
 		
 		textureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 480, 327, TextureOptions.BILINEAR);
 		this.kingTexture2 = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, this, "purple_king.png", 0, 0, 2, 1);
 		textureAtlas.load();
 		
 
 		pOnCreateResourcesCallback.onCreateResourcesFinished();
 	}
 
 	@Override
 	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
 			throws Exception {
 
 		gc = GameContext.getInstance();
 
 		//
 		// initialize network
 		//
 
 		this.receivedEndTurn = true;
 
 		ServerConnection.getInstance().setHandler(
 				new ServerConnection.OnMessageHandler() {
 					@Override
 					public void onMessage(String payload) {
 						try {
 							JSONObject jObj = new JSONObject(payload);
 							if (jObj.getInt("action") == Action.Server.TURN && round % 2 == 1) {
 								round++;
 
 								final int x = Integer.parseInt(jObj.getString("x"));
 								final int y = Integer.parseInt(jObj.getString("y"));
 
 								gc.getHud().setStatus("enemy: x="+x+", y="+y);
 
 								OnlineGameActivity.this.runOnUpdateThread(new Runnable() {
 									@Override
 									public void run() {
 										receivedEndTurn = false;
 										PhysicsManager.getInstance().setFreeze(false);
 										OnlineGameActivity.this.enemyCannon.pointAt(x, y);
 
 										final Cannonball ball = OnlineGameActivity.this.enemyCannon.fire(GameConfig.CANNON_FORCE);
 
 										if(GameConfig.USE_FIXED_CANNONBALL_TIME) {
 											getEngine().registerUpdateHandler(new TimerHandler(GameConfig.CANNONBALL_TIME_SEC, new ITimerCallback() {
 												@Override
 												public void onTimePassed(TimerHandler pTimerHandler) {
 													ball.remove();
 												}
 											}));
 										}
 
 										ball.setOnRemove(new Runnable() {
 											@Override
 											public void run() {
 												PhysicsManager.getInstance().setFreeze(true);
 											}
 										});
 									}
 								});
 								turnSent = false;
 							} else if (jObj.getInt("action") == Action.Server.PARTNER_LEFT) {
 								Intent intent = new Intent(OnlineGameActivity.this, EndGameActivity.class);
 								intent.putExtra("hasWon", true);
 								intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 								startActivity(intent);
 							} else if (jObj.getInt("action") == Action.Server.YOU_WIN) {
 								Intent intent = new Intent(OnlineGameActivity.this, EndGameActivity.class);
 								intent.putExtra("hasWon", true);
 								intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 								startActivity(intent);
 								//gc.getHud().setStatus("Du hast gewonnen!");
 							} else if (jObj.getInt("action") == Action.Server.END_TURN) {
 								JSONArray jsonEntities = jObj.getJSONArray("entities");
 
 								if(jsonEntities == null) {
 									Debug.d("Warning: jsonEntities is null");
 								} else {
 									Debug.d("received end turn from server with " + jsonEntities.length() + " entities");
 
 									PhysicsManager.getInstance().updateEntities(jsonEntities);
 									receivedEndTurn = true;
 								}
 							}
 						} catch (JSONException e) {
 							// TODO: Catch the exception
 						}
 					}
 				}
 		);
 
 		gc.setVboManager(this.getVertexBufferObjectManager());
 
 		if (GameConfig.LOG_FPS)
 			this.mEngine.registerUpdateHandler(new FPSLogger());
 
 		//
 		// initialize the scene
 		//
 
 		Scene scene = new Scene();
 		scene.setBackground(this.skySprite);
 		scene.setOnSceneTouchListener(this);
 
 		gc.setScene(scene);
 
 		//
 		// initialize the physics engine
 		//
 
 		FixedStepPhysicsWorld physicsWorld = new FixedStepPhysicsWorld(
 				GameConfig.PHYSICS_STEPS_PER_SEC,
 				new Vector2(0, SensorManager.GRAVITY_EARTH),
 				false,
 				GameConfig.PHYSICS_VELOCITY_ITERATION,
 				GameConfig.PHYSICS_POSITION_ITERATION
 		);
 
 		physicsWorld.setAutoClearForces(true);
 		gc.setPhysicsWorld(physicsWorld);
 
 		//
 		// initialize the entities
 		//
 
 		BasicMap map = new BasicMap(this.grassTexture, this.skyTexture);
 		scene.attachChild(map);
 
 		Boolean amILeft = false;
 
 		String leftPlayerName = "";
 		String rightPlayerName = "";
 		int myX = 0;
 		int myY = 0;
 		int enemyX = 0;
 		int enemyY = 0;
 
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			Boolean myTurn = extras.getBoolean("myTurn");
 			if (myTurn) {
 				this.receivedEndTurn = true;
 				this.round = 0;
 				amILeft = true;
 				myX = -400;
 				myY = 890;
 				enemyX = 1500;
 				enemyY = 890;
 				leftPlayerName = extras.getString("username");
 				rightPlayerName = extras.getString("partnername");
 			} else {
 				this.receivedEndTurn = false;
 				this.round = 1;
 				amILeft = false;
 				enemyX = -400;
 				enemyY = 890;
 				myX = 1500;
 				myY = 890;
 				leftPlayerName = extras.getString("partnername");
 				rightPlayerName = extras.getString("username");
 			}
 		}
 
 		this.cannon = new Cannon(this.cannonTexture, this.wheelTexture, this.ballTexture, amILeft);
 		this.cannon.setPosition(myX, myY);
 		scene.attachChild(this.cannon);
 
 		this.enemyCannon = new Cannon(this.cannonTexture, this.wheelTexture, this.ballTexture, !amILeft);
 		this.enemyCannon.setPosition(enemyX, enemyY);
 		scene.attachChild(this.enemyCannon);
 
 		this.leftCastle = new Castle(-1500, BasicMap.GROUND_Y, this.stoneTexture, this.roofTexture, this.woodTexture);
 		this.rightCastle = new Castle(1800, BasicMap.GROUND_Y, this.stoneTexture, this.roofTexture, this.woodTexture);
 
 		this.rightKing = new King(this.kingTexture1, 1650, BasicMap.GROUND_Y - kingTexture1.getHeight()/2);
 		scene.attachChild(this.rightKing);
 		
 		this.leftKing = new King(this.kingTexture2, -550, BasicMap.GROUND_Y - kingTexture2.getHeight()/2);
 		scene.attachChild(this.leftKing);
 		
 		//
 		// initialize navigation
 		//
 
 		this.scrollDetector = new SurfaceScrollDetector(this);
 		this.pinchZoomDetector = new PinchZoomDetector(this);
 
 		scene.setOnSceneTouchListener(this);
 		scene.setTouchAreaBindingOnActionDownEnabled(true);
 
 		hud = new GameHUD(this.aimButtonTexture, this.whiteFlagButtonTexture, this.statusFont, this.playerNameFont);
 		hud.setOnAimTouched(new Runnable() {
 			@Override
 			public void run() {
 				isAiming = !isAiming;
 
 				ServerJSONBuilder entities = new ServerJSONBuilder().create(Action.Server.END_TURN);
 				try {
 					JSONObject jObj = new JSONObject(entities.entities().build());
 
 					JSONArray jsonEntities = jObj.getJSONArray("entities");
 
 					if(jsonEntities == null) {
 						Debug.d("Warning: jsonEntities is null");
 					} else {
 						Debug.d("received end turn from server with " + jsonEntities.length() + " entities");
 
 						PhysicsManager.getInstance().updateEntities(jsonEntities);
 					}
 				}catch (JSONException e) {
 
 				}
 			}
 		});
 		hud.setOnWhiteFlagTouched(new Runnable() {
 			@Override
 			public void run() {
 				resign();
 			}
 		});
 
 		gc.setHud(hud);
 		gc.getCamera().setHUD(hud);
 
 		final float initialLeftCastleHeight = leftCastle.getInitialHeight();
 		final float initialRightCastleHeight = rightCastle.getInitialHeight();
 		final boolean left = amILeft;
 
 		hud.setLeftPlayerName(leftPlayerName);
 		hud.setRightPlayerName(rightPlayerName);
 
 		Debug.d("left player name: "+leftPlayerName);
 		Debug.d("right player name: " + rightPlayerName);
 
 		if(amILeft){
 			hud.setStatus("Du bist dran!");
 		}else{
 			hud.setStatus("Gegner ist dran!");
 		}
 
 		scene.registerUpdateHandler(new IUpdateHandler() {
 			@Override
 			public void onUpdate(float pSecondsElapsed) {
				float leftLife = (leftCastle.getHeight() + initialLeftCastleHeight)/initialLeftCastleHeight*2;
				float rightLife = (rightCastle.getHeight() + initialRightCastleHeight)/initialRightCastleHeight*2;
 
 				hud.getLeftLifeBar().setValue(leftLife);
 				hud.getRightLifeBar().setValue(rightLife);
 
 				if(left && leftLife < 0.3f || !left && rightLife < 0.3f) {
 					gc.getHud().setStatus("Du hast verloren!");
 					webSocketConnection.sendTextMessage(OnlineGameActivity.JSON_LOSE);
 					Intent intent = new Intent(OnlineGameActivity.this, EndGameActivity.class);
 					intent.putExtra("hasWon", false);
 					intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 					startActivity(intent);
 				}
 			}
 
 			@Override
 			public void reset() {
 
 			}
 		});
 
 		scene.registerUpdateHandler(physicsWorld);
 
 		if(!GameConfig.USE_FIXED_CANNONBALL_TIME)
 			scene.registerUpdateHandler(PhysicsManager.getInstance());
 
 		PhysicsManager.getInstance().setFreeze(true);
 
 		pOnCreateSceneCallback.onCreateSceneFinished(scene);
 	}
 
 	@Override
 	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
 		pOnPopulateSceneCallback.onPopulateSceneFinished();
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		gc = GameContext.getInstance();
 
 		if (gc.getPhysicsWorld() == null)
 			return false;
 
 		if(this.isAiming) {
 
 			//
 			// aim and fire
 			//
 
 			if(!PhysicsManager.getInstance().isReady()) {
 				gc.getHud().setStatus("Wait for physics...");
 				return true;
 			}
 
 			float x = pSceneTouchEvent.getX();
 			float y = pSceneTouchEvent.getY();
 
 			int iX = (int) x;
 			int iY = (int) y;
 
 			if(this.cannon.pointAt(iX, iY)) {
 				this.aimX = iX;
 				this.aimY = iY;
 			}
 
 			gc.getHud().setStatus("x="+this.aimX+", y="+this.aimY);
 
 			Debug.d("receivedEndTurn: " + this.receivedEndTurn);
 
 			if (pSceneTouchEvent.isActionUp() && this.receivedEndTurn) {
 				if (!turnSent && round % 2 == 0) {
 					this.round++;
 					this.runOnUpdateThread(new Runnable() {
 
 						@Override
 						public void run() {
 							PhysicsManager.getInstance().setFreeze(false);
 
 							final Cannonball ball = OnlineGameActivity.this.cannon.fire(GameConfig.CANNON_FORCE);
 
 							if(GameConfig.USE_FIXED_CANNONBALL_TIME) {
 								getEngine().registerUpdateHandler(new TimerHandler(GameConfig.CANNONBALL_TIME_SEC, new ITimerCallback() {
 									@Override
 									public void onTimePassed(TimerHandler pTimerHandler) {
 										ball.remove();
 									}
 								}));
 							}
 
 							ball.setOnRemove(new Runnable() {
 								@Override
 								public void run() {
 									PhysicsManager.getInstance().setFreeze(true);
 									ServerJSONBuilder query = new ServerJSONBuilder().create(Action.Client.END_TURN).entities();
 									String jsonStr = query.build();
 									webSocketConnection.sendTextMessage(jsonStr);
 									Debug.d("send " + PhysicsManager.getInstance().getPhysicalEntities().size() + " entities");
 								}
 							});
 						}
 					});
 
 					this.webSocketConnection.sendTextMessage(
 							new ServerJSONBuilder()
 									.create(Action.Client.TURN)
 									.option("x", String.valueOf(this.aimX))
 									.option("y", String.valueOf(this.aimY)).build()
 					);
 
 					this.turnSent = true;
 				}
 			}
 
 		}else{
 
 			//
 			// pinch and zoom
 			//
 
 			if(pSceneTouchEvent.isActionDown()) {
 				this.scrollDetector.setEnabled(true);
 			}
 
 			this.pinchZoomDetector.onTouchEvent(pSceneTouchEvent);
 
 			if(this.pinchZoomDetector.isZooming()) {
 				this.scrollDetector.setEnabled(false);
 			}else{
 				this.scrollDetector.onTouchEvent(pSceneTouchEvent);
 			}
 
 		}
 
 		return true;
 	}
 
 	@Override
 	public void onPinchZoomStarted(PinchZoomDetector pPinchZoomDetector, TouchEvent pSceneTouchEvent) {
 		GameContext gc = GameContext.getInstance();
 		ZoomCamera camera = (ZoomCamera)gc.getCamera();
 		this.pinchZoomStartedCameraZoomFactor = camera.getZoomFactor();
 	}
 
 	@Override
 	public void onPinchZoom(PinchZoomDetector pPinchZoomDetector, TouchEvent pTouchEvent, float pZoomFactor) {
 		GameContext gc = GameContext.getInstance();
 		ZoomCamera camera = (ZoomCamera)gc.getCamera();
 
 		float factor = this.pinchZoomStartedCameraZoomFactor * pZoomFactor;
 		if(factor > GameConfig.CAMERA_ZOOM_MIN && factor < GameConfig.CAMERA_ZOOM_MAX)
 			camera.setZoomFactor(factor);
 	}
 
 	@Override
 	public void onPinchZoomFinished(PinchZoomDetector pPinchZoomDetector, TouchEvent pTouchEvent, float pZoomFactor) {
 		GameContext gc = GameContext.getInstance();
 		ZoomCamera camera = (ZoomCamera)gc.getCamera();
 
 		float factor = this.pinchZoomStartedCameraZoomFactor * pZoomFactor;
 		if(factor > GameConfig.CAMERA_ZOOM_MIN && factor < GameConfig.CAMERA_ZOOM_MAX)
 			camera.setZoomFactor(factor);
 	}
 
 	@Override
 	public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
 		GameContext gc = GameContext.getInstance();
 		ZoomCamera camera = (ZoomCamera)gc.getCamera();
 		final float zoomFactor = camera.getZoomFactor();
 
 		camera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
 	}
 
 	@Override
 	public void onScroll(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
 		GameContext gc = GameContext.getInstance();
 		ZoomCamera camera = (ZoomCamera)gc.getCamera();
 		final float zoomFactor = camera.getZoomFactor();
 
 		camera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
 	}
 
 	@Override
 	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
 		GameContext gc = GameContext.getInstance();
 		ZoomCamera camera = (ZoomCamera)gc.getCamera();
 		final float zoomFactor = camera.getZoomFactor();
 
 		camera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
 	}
 
 	private void resign() {
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				final Dialog dialog = new Dialog(OnlineGameActivity.this);
 				dialog.setContentView(R.layout.quit_dialog);
 				//dialog.setTitle("Aufgeben?");
 				dialog.setCancelable(true);
 				dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
 
 				//TextView text = (TextView) dialog.findViewById(R.id.lBeendenFrage);
 				Button bCancel = (Button) dialog
 						.findViewById(R.id.bCancel);
 				Button bResign = (Button) dialog
 						.findViewById(R.id.bResign);
 
 				bCancel.setOnClickListener(new View.OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						dialog.dismiss();
 					}
 				});
 				bResign.setOnClickListener(new View.OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						hud.setStatus("Du hast aufgegeben!");
 						webSocketConnection.sendTextMessage(OnlineGameActivity.JSON_LOSE);
 						dialog.dismiss();
 						Intent intent = new Intent(OnlineGameActivity.this, EndGameActivity.class);
 						intent.putExtra("hasWon", false);
 						intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 						startActivity(intent);
 					}
 				});
 				dialog.show();
 			}
 		});
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 	        resign();
 	        return true;
 	    }
 	    return super.onKeyDown(keyCode, event);
 
 	}
 }
