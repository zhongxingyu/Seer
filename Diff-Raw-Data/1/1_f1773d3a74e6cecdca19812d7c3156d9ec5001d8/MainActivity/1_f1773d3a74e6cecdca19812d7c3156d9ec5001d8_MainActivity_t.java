 package com.lolbro.nian;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
 import org.andengine.entity.primitive.Line;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.SpriteBackground;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.sprite.ButtonSprite;
 import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.entity.util.FPSCounter;
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.util.HorizontalAlign;
 import org.andengine.util.math.MathUtils;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.opengl.GLES20;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Manifold;
 import com.lolbro.nian.customs.AutoVerticalParallaxBackground;
 import com.lolbro.nian.customs.SwipeScene;
 import com.lolbro.nian.customs.SwipeScene.SwipeListener;
 import com.lolbro.nian.customs.VerticalParallaxBackground.VerticalParallaxEntity;
 import com.lolbro.nian.models.MObject;
 
 
 
 public class MainActivity extends SimpleBaseGameActivity implements SwipeListener, IUpdateHandler, ContactListener, IOnMenuItemClickListener {
 	
 	// ===========================================================
 	// Constants
 	// ===========================================================
 	
 	private static final float PIXEL_TO_METER_RATIO = PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 	
 	private static final int CAMERA_WIDTH = 480;
 	private static final int CAMERA_HEIGHT = 720;
 	
 	private static final int STEPS_PER_SECOND = 60;
 	private static final int MAX_STEPS_PER_UPDATE = 1;
 	
 	private static final float SCORE_TIME_MULTIPLIER = 133.7f;
 	
 	private static final int MAINMENU_PLAY = 1;
 	private static final int MAINMENU_SHOP = 2;
 	private static final int MENU_RETRY = 3;
 	
 	private static final float LANE_STEP_SIZE = 160;
 	public static final float LANE_MID = CAMERA_WIDTH/2;
 	public static final float LANE_LEFT = LANE_MID - LANE_STEP_SIZE;
 	public static final float LANE_RIGHT = LANE_MID + LANE_STEP_SIZE;
 	
 	private static final int PLAYER_SIZE = 64;
 	private static final float PLAYER_ROLL_SPEED = 25f;
 	public static final Vector2 PLAYER_HOME_POSITION = new Vector2(LANE_MID, -CAMERA_HEIGHT/2 + PLAYER_SIZE*2);
 	public static final Vector2 PLAYER_SPRITE_SPAWN = new Vector2(PLAYER_HOME_POSITION.x - PLAYER_SIZE/2, PLAYER_HOME_POSITION.y -PLAYER_SIZE/2);
 	
 	private static final int OBSTACLE_02_SIZE_W = 100;
 	private static final int OBSTACLE_02_SIZE_H = 100;
 	private static final float OBSTACLE_SPEED = 4.68f;
 	
 	private static final int ENEMY_SIZE_W = 66;
 	private static final int ENEMY_SIZE_H = 96;
 	private static final float ENEMY_SPEED = 15f;
 	private static final float ALLOWED_HIGH = -500f / PIXEL_TO_METER_RATIO;
 	private static final int SPAWN_DELAY_Y = 100;
 	
 	private static final int COUPON_SIZE = 32;
 	private static final float COUPON_SPEED = 4.68f;
 	private static final float DISTANCE_BETWEEN_COUPONS = 10.0f;
 	
 	public static final int MOVE_UP = SwipeListener.DIRECTION_UP;
 	public static final int MOVE_DOWN = SwipeListener.DIRECTION_DOWN;
 	public static final int MOVE_LEFT = SwipeListener.DIRECTION_LEFT;
 	public static final int MOVE_RIGHT = SwipeListener.DIRECTION_RIGHT;
 	
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	
 	private SharedPreferences prefs;
 	private SharedPreferences.Editor prefsEdit;
 	
 	private Camera mCamera;
 	
 	private HUD hud = new HUD();
 	
 	private SwipeScene mScene;
 	private MenuScene mMainMenuScene;
 	private MenuScene mMenuScene;
 	
 	private PhysicsWorld mPhysicsWorld;
 	
 	private BitmapTextureAtlas mCharactersTexture;
 	private ITiledTextureRegion mPlayerRegion;
 	private ITextureRegion mEnemy01Region;
 	private ITextureRegion mObstacle02Region;
 	private ITextureRegion mCouponRegion;
 	
 	private BitmapTextureAtlas mMenuTexture;
 	private ITextureRegion mMainMenuBackgroundRegion;
 	private ITextureRegion mMainMenuPlayRegion;
 	private ITextureRegion mMainMenuShopRegion;
 	private ITextureRegion mMenuRetryRegion;
 	private ITextureRegion mPowerUpButtonRegion;
 
 	private MObject mPlayer;
 	private ArrayList<MObject> mEnemies;
 	private ArrayList<MObject> mObstacles;
 	private ArrayList<MObject> mCoupons;
 	
 	private int activeRow;
 	private float timeElapsed;
 	private float score;
 	private float highScore;
 	
 	private boolean alive = true;
 	private Text scoreText;
 	private Text highScoreText;
 	private ButtonSprite powerUpButton;
 	
 	private int allowedEnemyQuantity = 4;
 	private float highestEnemy;
 	private float highestObstacle;
 	private float highestCoupon;
 	private float obstacleLane;
 	private float couponLane;
 	private int coupons;
 	
 	private ArrayList<MObject> mobjectsToRemove;
 	
 	private Random random = new Random();
 	
 	private BitmapTextureAtlas mBackgroundTextureAtlas;
 	private ITextureRegion mParallaxLayerBack;
 	
 	private boolean moveLeft = false;
 	private boolean moveRight = false;
 	private boolean moveUp = false;
 	private boolean moveDown = false;
 	
 	private int moveOnQueue;
 	
 	private float rollToPosition = 0;
 	
 	private Line teslaCoilLine;
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 	
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		this.mCamera = new Camera(0, -CAMERA_HEIGHT, CAMERA_WIDTH, CAMERA_HEIGHT);
 		
 		//new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT)
 		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new FillResolutionPolicy(), this.mCamera);
 	}
 	
 	@Override
 	protected void onCreateResources() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 		
 		this.mBackgroundTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 512, 64);
 		this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBackgroundTextureAtlas, this, "floor_2.png", 0, 0);
 		this.mBackgroundTextureAtlas.load();
 		
 		this.mCharactersTexture = new BitmapTextureAtlas(this.getTextureManager(), 512, 256, TextureOptions.BILINEAR);
 		this.mPlayerRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mCharactersTexture, this, "player_1_animation.png", 0, 0, 8, 1); //512x64
 		this.mEnemy01Region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mCharactersTexture, this, "enemy_1.png", 0, 65); //66x96
 		this.mObstacle02Region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mCharactersTexture, this, "obstacle_02.png", 100, 65); //100x100
 		this.mCouponRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mCharactersTexture, this, "coupon_1.png", 67, 65); //32x32
 		this.mCharactersTexture.load();
 		
 		this.mMenuTexture = new BitmapTextureAtlas(this.getTextureManager(), 512, 1024, TextureOptions.BILINEAR);
 		this.mMainMenuBackgroundRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mMenuTexture, this, "mainmenu_background.png", 0, 0); //480x720
 		this.mMainMenuShopRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mMenuTexture, this, "mainmenu_shop.png", 0, 721); //174x59
 		this.mMainMenuPlayRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mMenuTexture, this, "mainmenu_play.png", 175, 721); //130x64
 		this.mPowerUpButtonRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mMenuTexture, this, "powerupbutton.png", 307, 721); //128x64
 		this.mMenuRetryRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mMenuTexture, this, "menu_retry.png", 0, 787); //158x40
 		this.mMenuTexture.load();
 	}
 	
 	@Override
 	protected Scene onCreateScene() {
 
 		this.mScene = new SwipeScene(this);
 		
 		prefs = getSharedPreferences("nian_preferences", 0);
 		prefsEdit = prefs.edit();
 		
 		highScore = prefs.getFloat("highScore", 0);
 		coupons = prefs.getInt("coupons", 0);
 		
 		/* HUD and score handler */
 		mCamera.setHUD(hud);
 		Font scoreFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);
 		scoreFont.load();
 		
 		scoreText = new Text(CAMERA_WIDTH - 150, 10, scoreFont, "" + (int)score, 20, new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
 		highScoreText = new Text(10, 10, scoreFont, "" + (int)highScore, 20, new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
 		powerUpButton = new ButtonSprite(10, CAMERA_HEIGHT-74, this.mPowerUpButtonRegion, getVertexBufferObjectManager(), new OnClickListener() {
 			@Override
 			public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				int teslas = prefs.getInt("tesla_coils", 0);
 				if(teslas > 0){
 					mPlayer.setBonus(MObject.BONUS_TESLACOIL);
 					prefsEdit.putInt("tesla_coils", teslas-1);
 					prefsEdit.commit();
 				}
 			}
 		});
 		this.hud.registerTouchArea(powerUpButton);
 		
 		createMainMenuScene();
 		createMenuScene();
 		
 		this.mScene.registerUpdateHandler(this);
 		
 		this.mPhysicsWorld = new FixedStepPhysicsWorld(STEPS_PER_SECOND, MAX_STEPS_PER_UPDATE, new Vector2(0, 0), false, 10, 10);
 		
 		this.mPhysicsWorld.setContactListener(this);
 		
 		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
 		
 		this.mEnemies = new ArrayList<MObject>();
 		this.mObstacles = new ArrayList<MObject>();
 		this.mCoupons = new ArrayList<MObject>();
 		this.mobjectsToRemove = new ArrayList<MObject>();
 		
 		initBackground();
 		initPlayer();
 //		showFPS();
 		
 		this.teslaCoilLine = new Line(0, 0, 0, 0, 5, getVertexBufferObjectManager());
 		this.teslaCoilLine.setColor(1, 0.1f, 0.1f);
 		this.teslaCoilLine.setZIndex(Integer.MAX_VALUE);
 		this.mScene.attachChild(teslaCoilLine);
 		
 		this.mScene.setChildScene(this.mMainMenuScene, false, true, true);
 		
 		return this.mScene;
 	}
 	
 	@Override
 	public synchronized void onResumeGame() {
 		super.onResumeGame();
		coupons = prefs.getInt("coupons", 0);
 		mScene.registerForSwipes(this, this);
 	}
 	
 	@Override
 	public void onBackPressed() {
 		if(this.mScene.hasChildScene()){
 			super.onBackPressed();
 		} else {
 			this.mScene.setChildScene(this.mMainMenuScene, false, true, true);
 			detachScore();
 			detachPowerUpButton();
 		}
 	}
 	
 	@Override
 	public void reset() {
 	}
 	
 	@Override
 	public void onSwipe(int direction) {
 		move(direction);	
 	}
 	
 	@Override
 	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) {
 		switch(pMenuItem.getID()) {
 		case MAINMENU_PLAY:
 			this.mScene.reset();
 			this.mMainMenuScene.reset();
 			
 			attachScore();
 			attachPowerUpButton();
 			
 			resetGame();
 			return true;
 		case MAINMENU_SHOP:
 			Intent intent = new Intent(this, ShopActivity.class);
 			startActivity(intent);
 			
 			return true;
 		case MENU_RETRY:
 			this.mScene.reset();
 			this.mMenuScene.reset();
 			
 			resetGame();
 			return true;
 		default:
 			return false;
 		}
 	}
 	
 	// ===========================================================
 	// On update
 	// ===========================================================
 	
 	@Override
 	public void onUpdate(float pSecondsElapsed) {
 		if (alive){
 			timeElapsed += pSecondsElapsed;
 			score += pSecondsElapsed * SCORE_TIME_MULTIPLIER * activeRowPoints(activeRow);
 			scoreText.setText("" + (int)score);
 			
 			/* MOVING OF CHARACTER */
 			if (isMoving()){
 				Body playerBody = mPlayer.getBody();
 				
 				float x = mPlayer.getBodyPositionX(true);
 				float y = mPlayer.getBodyPositionY(true);
 				float rollMovement = PLAYER_ROLL_SPEED * pSecondsElapsed;
 				
 				if (moveUp == true) {
 					if (y - rollMovement > rollToPosition) {
 						playerBody.setTransform(x, y - rollMovement, 0);
 					} else {
 						moveUp = false;
 						activeRow++;
 						playerBody.setTransform(x, rollToPosition, 0);
 						if (moveOnQueue != 0) {
 							move(moveOnQueue);
 							moveOnQueue = 0;
 						}
 					}
 				} else if (moveDown == true) {
 					if (y + rollMovement < rollToPosition) {
 						playerBody.setTransform(x, y + rollMovement, 0);
 					} else {
 						moveDown = false;
 						activeRow--;
 						playerBody.setTransform(x, rollToPosition, 0);
 						if (moveOnQueue != 0) {
 							move(moveOnQueue);
 							moveOnQueue = 0;
 						}
 					}
 				} else if (moveLeft == true) {
 					if (x - rollMovement > rollToPosition) {
 						playerBody.setTransform(x - rollMovement, y, 0);
 					} else {
 						moveLeft = false;
 						playerBody.setTransform(rollToPosition, y, 0);
 						if (moveOnQueue != 0) {
 							move(moveOnQueue);
 							moveOnQueue = 0;
 						}
 					}
 				} else if (moveRight == true) {
 					if (x + rollMovement < rollToPosition) {
 						playerBody.setTransform(x + rollMovement, y, 0);
 					} else {
 						moveRight = false;
 						playerBody.setTransform(rollToPosition, y, 0);
 						if (moveOnQueue != 0) {
 							move(moveOnQueue);
 							moveOnQueue = 0;
 						}
 					}
 				}
 			}
 			
 			/* REMOVE OBJECTS */
 			for(int i=mobjectsToRemove.size()-1; i>=0; i--){
 				MObject coupon = mobjectsToRemove.get(i);
 				removeMobject(coupon);
 				mobjectsToRemove.remove(coupon);
 				mCoupons.remove(coupon);
 			}
 			
 			/* MOVE MOBJECTS */
 			Vector2 playerPosition = null;
 			boolean enemiesInTeslacoilRange = false;
 			if(mPlayer.getBonus() == MObject.BONUS_TESLACOIL){
 				playerPosition = mPlayer.getBodyPosition(true);
 			}
 			
 			highestEnemy = 0;
 			for(int i=mEnemies.size()-1; i>=0; i--){
 				MObject enemy = mEnemies.get(i);
 				Body enemyBody = enemy.getBody();
 				enemyBody.setTransform(enemy.getBodyPositionX(true), enemy.getBodyPositionY(true) + ENEMY_SPEED*pSecondsElapsed, 0);
 				Vector2 enemyPosition = enemy.getBodyPosition(true);
 				if (enemyPosition.y * PIXEL_TO_METER_RATIO > ENEMY_SIZE_H) {
 					removeMobject(enemy);
 					mEnemies.remove(i);
 				} else {
 					highestEnemy = Math.min(highestEnemy, enemy.getBodyPositionY(true));
 					if(mPlayer.getBonus() == MObject.BONUS_TESLACOIL && enemiesInTeslacoilRange == false){
 						float distance = MathUtils.distance(playerPosition.x, playerPosition.y, enemyPosition.x, enemyPosition.y);
 						if(distance < 8){
 							float damage = MObject.TESLA_COIL_DAMAGE * pSecondsElapsed;
 							float enemyHP = enemy.getHitPoints() - damage;
 							enemy.setHitPoints(enemyHP);
 							if(enemyHP <= 0){
 								removeMobject(enemy);
 								mEnemies.remove(i);
 							}
 							teslaCoilLine.setPosition(playerPosition.x * PIXEL_TO_METER_RATIO, playerPosition.y * PIXEL_TO_METER_RATIO, enemyPosition.x * PIXEL_TO_METER_RATIO, enemyPosition.y * PIXEL_TO_METER_RATIO);
 							//TODO: Very ineffective to sort children in every update
 							mScene.sortChildren();
 							enemiesInTeslacoilRange = true;
 						}
 					}
 				}
 			}
 			
 			if(enemiesInTeslacoilRange == false){
 				teslaCoilLine.setPosition(0, 0, 0, 0);
 			}
 			
 			highestObstacle = 0;
 			for(int i=mObstacles.size()-1; i>=0; i--){
 				MObject obstacle = mObstacles.get(i);
 				Body obstacleBody = obstacle.getBody();
 				obstacleBody.setTransform(obstacle.getBodyPositionX(true), obstacle.getBodyPositionY(true) + OBSTACLE_SPEED*pSecondsElapsed, 0);
 				Vector2 obstaclePosition = obstacle.getBodyPosition(true);
 				if (obstaclePosition.y * PIXEL_TO_METER_RATIO > OBSTACLE_02_SIZE_H) {
 					removeMobject(obstacle);
 					mObstacles.remove(i);
 				} else {
 					highestObstacle = Math.min(highestObstacle, obstacle.getBodyPositionY(true));
 				}
 			}
 			
 			highestCoupon = 0;
 			for(int i=mCoupons.size()-1; i>=0; i--){
 				MObject coupon = mCoupons.get(i);
 				Body couponBody = coupon.getBody();
 				couponBody.setTransform(coupon.getBodyPositionX(true), coupon.getBodyPositionY(true) + COUPON_SPEED*pSecondsElapsed, 0);
 				if (coupon.getBodyPositionY(false) > COUPON_SIZE) {
 					mPhysicsWorld.unregisterPhysicsConnector(mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(coupon.getSprite()));
 					mPhysicsWorld.destroyBody(couponBody);
 					mScene.detachChild(coupon.getSprite());
 					mCoupons.remove(i);
 				} else {
 					highestCoupon = Math.min(highestCoupon, coupon.getBodyPositionY(true));
 				}
 			}
 			
 			/* SPAWN MOBJECTS */
 			if(highestEnemy >= ALLOWED_HIGH && random.nextFloat() > 0.985f) {
 				spawnMob(randomLane());
 			}
 			
 			if(highestObstacle >= ALLOWED_HIGH && random.nextFloat() > 0.99f) {
 				obstacleLane = randomLane();
 				spawnObstacle02(obstacleLane);
 			}
 			
 			if (highestCoupon >= ALLOWED_HIGH && random.nextFloat() > 0.985f) {
 				couponLane = randomLane();
 				if(mObstacles.size() > 0) {
 					for(int i=mObstacles.size()-1; i>=0; i--) {
 						MObject obstacle = mObstacles.get(i);
 						if(obstacle.getBodyPositionY(false) <= -CAMERA_HEIGHT + OBSTACLE_02_SIZE_H/2 && obstacle.getBodyPositionX(false) == couponLane) {
 						} else {
 							for (int j = 0; j < 5 + random.nextInt(5); j++) {
 								spawnCoupon(couponLane, (COUPON_SIZE + DISTANCE_BETWEEN_COUPONS)*j);
 								i = -1;
 							}
 						}
 					}
 				} else {
 					for (int i = 0; i < 5 + random.nextInt(5); i++) {
 						spawnCoupon(couponLane, (COUPON_SIZE + DISTANCE_BETWEEN_COUPONS)*i);
 					}
 				}
 			}
 		}
 	}
 	
 	// ===========================================================
 	// On Contact
 	// ===========================================================
 	
 	@Override
 	public void beginContact(Contact contact) {
 		MObject mobjectA = (MObject) contact.getFixtureA().getBody().getUserData();
 		MObject mobjectB = (MObject) contact.getFixtureB().getBody().getUserData();
 		
 		if((mobjectA.getType() == MObject.TYPE_COUPON && mobjectB.getType() == MObject.TYPE_PLAYER || mobjectA.getType() == MObject.TYPE_PLAYER && mobjectB.getType() == MObject.TYPE_COUPON)){
 			mobjectsToRemove.add(mobjectA.getType() == MObject.TYPE_COUPON ? mobjectA : mobjectB);
 			coupons++;
 		} else if(mobjectA.getType() == MObject.TYPE_ENEMY && mobjectB.getType() == MObject.TYPE_PLAYER || 
 				mobjectA.getType() == MObject.TYPE_PLAYER && mobjectB.getType() == MObject.TYPE_ENEMY || 
 				mobjectA.getType() == MObject.TYPE_STATIC_OBSTACLE && mobjectB.getType() == MObject.TYPE_PLAYER || 
 				mobjectA.getType() == MObject.TYPE_PLAYER && mobjectB.getType() == MObject.TYPE_STATIC_OBSTACLE) {
 			this.mScene.setChildScene(this.mMenuScene, false, true, true);
 			
 			alive = false;
 			
 			highScore = Math.max(highScore, score);
 			highScoreText.setText("" + (int)highScore);
 			
 			/* Save High Score */
 			prefsEdit.putFloat("highScore", highScore);
 			prefsEdit.putInt("coupons", coupons);
 			prefsEdit.commit();
 		}
 	}
 
 	@Override
 	public void endContact(Contact contact) {
 		
 	}
 
 	@Override
 	public void preSolve(Contact contact, Manifold oldManifold) {
 		
 	}
 
 	@Override
 	public void postSolve(Contact contact, ContactImpulse impulse) {
 		
 	}
 	
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	
 	private void initBackground() {
 		
 		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
 		final AutoVerticalParallaxBackground autoParallaxBackground = new AutoVerticalParallaxBackground(0, 0, 0, 30);
 		autoParallaxBackground.attachVerticalParallaxEntity(new VerticalParallaxEntity(-5.0f, new Sprite(0, 720, this.mParallaxLayerBack, vertexBufferObjectManager)));
 		this.mScene.setBackground(autoParallaxBackground);
 	}
 	
 	private void spawnMob(float position) {
 		MObject enemy = new MObject(
 				MObject.TYPE_ENEMY,
 				position-ENEMY_SIZE_W/2,
 				-CAMERA_HEIGHT - ENEMY_SIZE_H,
 				ENEMY_SIZE_W,
 				ENEMY_SIZE_H,
 				this.mEnemy01Region,
 				this.getVertexBufferObjectManager(), 
 				mPhysicsWorld);
 		
 		enemy.getBody().setUserData(enemy);
 		enemy.setHitPoints(20);
 		
 		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(enemy.getSprite(), enemy.getBody(), true, false));
 		this.mScene.attachChild(enemy.getSprite());
 		
 		this.mEnemies.add(enemy);
 	}
 	
 	private void spawnObstacle02(float position) {
 		MObject obstacle02 = new MObject(
 				MObject.TYPE_ENEMY, //Should be TYPE_STATIC_OBSTACLE which is declared in MObject but using TYPE_ENEMY temporary
 				position-OBSTACLE_02_SIZE_W/2,
 				-CAMERA_HEIGHT - OBSTACLE_02_SIZE_H,
 				OBSTACLE_02_SIZE_W,
 				OBSTACLE_02_SIZE_H,
 				this.mObstacle02Region,
 				this.getVertexBufferObjectManager(),
 				mPhysicsWorld);
 		
 		obstacle02.getBody().setUserData(obstacle02);
 		
 		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(obstacle02.getSprite(), obstacle02.getBody(), true, false));
 		this.mScene.attachChild(obstacle02.getSprite());
 		
 		this.mObstacles.add(obstacle02);
 	}
 	
 	private void spawnCoupon(float positionX, float positionY) {
 		MObject coupon = new MObject(
 				MObject.TYPE_COUPON,
 				positionX-COUPON_SIZE/2,
 				-CAMERA_HEIGHT - COUPON_SIZE - SPAWN_DELAY_Y - positionY,
 				COUPON_SIZE,
 				COUPON_SIZE,
 				this.mCouponRegion,
 				this.getVertexBufferObjectManager(),
 				mPhysicsWorld);
 		
 		coupon.getBody().setUserData(coupon);
 		
 		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(coupon.getSprite(), coupon.getBody(), true, false));
 		this.mScene.attachChild(coupon.getSprite());
 		
 		this.mCoupons.add(coupon);
 	}
 	
 	private void initPlayer() {
 
 		this.mPlayer = new MObject(
 				MObject.TYPE_PLAYER,
 				PLAYER_SPRITE_SPAWN.x,
 				PLAYER_SPRITE_SPAWN.y,
 //				PLAYER_SIZE,
 //				PLAYER_SIZE,
 				this.mPlayerRegion,
 				this.getVertexBufferObjectManager(),
 				mPhysicsWorld);
 
 		this.mPlayer.getBody().setUserData(this.mPlayer);
 //		this.mPlayer.setBonus(MObject.BONUS_TESLACOIL);
 		
 		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mPlayer.getSprite(), mPlayer.getBody(), true, false));
 		this.mScene.attachChild(mPlayer.getSprite());
 	}
 	
 	private void createMainMenuScene() {
 		this.mMainMenuScene = new MenuScene(this.mCamera);
 		
 		detachScore();
 		detachPowerUpButton();
 		
 		this.mMainMenuScene.setBackground(new SpriteBackground(new Sprite(0, 0, mMainMenuBackgroundRegion, getVertexBufferObjectManager())));
 		
 		final SpriteMenuItem playMenuItem = new SpriteMenuItem(MAINMENU_PLAY, this.mMainMenuPlayRegion, this.getVertexBufferObjectManager());
 		playMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		this.mMainMenuScene.addMenuItem(playMenuItem);
 		
 		final SpriteMenuItem shopMenuItem = new SpriteMenuItem(MAINMENU_SHOP, this.mMainMenuShopRegion, this.getVertexBufferObjectManager());
 		shopMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		this.mMainMenuScene.addMenuItem(shopMenuItem);
 		
 		this.mMainMenuScene.buildAnimations();
 		
 		this.mMainMenuScene.setOnMenuItemClickListener(this);
 	}
 	
 	private void createMenuScene() {		
 		this.mMenuScene = new MenuScene(this.mCamera);
 		
 		displayHighScore();
 		
 		SpriteMenuItem retryMenuItem = new SpriteMenuItem(MENU_RETRY, this.mMenuRetryRegion, this.getVertexBufferObjectManager());
 		retryMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		this.mMenuScene.addMenuItem(retryMenuItem);
 		
 		this.mMenuScene.buildAnimations();
 		
 		this.mMenuScene.setBackgroundEnabled(false);
 		
 		this.mMenuScene.setOnMenuItemClickListener(this);
 	}
 	
 	private void removeMobject(MObject mobject) {
 		 mPhysicsWorld.unregisterPhysicsConnector(mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(mobject.getSprite()));
 		 mPhysicsWorld.destroyBody(mobject.getBody());
 		 mScene.detachChild(mobject.getSprite());
 	}
 	
 	private void removeMobjects(ArrayList<MObject> mobjects) {
 		for(MObject mobject : mobjects){
 			removeMobject(mobject);
 		 }
 		 mobjects.clear();
 	}
 	
 	private void resetGame() {
 
 		alive = true;
 		timeElapsed = 0;
 		score = 0;
 		
 		moveUp = moveDown = moveLeft = moveRight = false;
 		
 		removeMobjects(mEnemies);
 		removeMobjects(mObstacles);
 		removeMobjects(mCoupons);
 		mobjectsToRemove.clear();
 		
 		mPlayer.getBody().setTransform((PLAYER_SPRITE_SPAWN.x + PLAYER_SIZE/2f) / PIXEL_TO_METER_RATIO, (PLAYER_SPRITE_SPAWN.y + PLAYER_SIZE/2f) / PIXEL_TO_METER_RATIO, 0);
 		activeRow = 2;
 	 }
 	
 	/* Methods for debugging */
 	private void showFPS() {
 		final FPSCounter fpsCounter = new FPSCounter();
 		this.mEngine.registerUpdateHandler(fpsCounter);
 		HUD hud=new HUD();
 		Font font = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);
 		font.load();
 		final Text text = new Text(10, 10, font, "FPS: ", 20, new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
 		hud.attachChild(text);
 		mCamera.setHUD(hud);
 		mScene.registerUpdateHandler(new TimerHandler(1 / 20.0f, true, new ITimerCallback() {
 		                @Override
 		                public void onTimePassed(final TimerHandler pTimerHandler) {
 		                         text.setText("FPS: " + fpsCounter.getFPS());
 		        }
 		}));
 	}
 	
 	/* Methods of random usefulness :) */
 	private float randomLane() {
 		return LANE_MID + (random.nextInt(3)-1) * LANE_STEP_SIZE;
 	}
 	
 	private float activeRowPoints(int activeRow) {
 		if (activeRow == 1) {
 			return 1.0f;
 		} else if (activeRow == 2) {
 			return 1.5f;
 		} else if (activeRow == 3) {
 			return 2.0f;
 		}
 		return 0;
 	}
 	
 	private void attachScore() {
 		hud.attachChild(scoreText);
 	}
 	
 	private void detachScore() {
 		hud.detachChild(scoreText);
 	}
 	
 	private void displayHighScore() {
 		hud.attachChild(highScoreText);
 	}
 	
 	private void detachHighScore () {
 		hud.detachChild(highScoreText);
 	}
 	
 	private void attachPowerUpButton() {
 		hud.attachChild(powerUpButton);
 	}
 	
 	private void detachPowerUpButton() {
 		hud.detachChild(powerUpButton);
 	}
 	
 	/* Methods for moving */
 	private void move(int direction){
 		if (isMoving()) {
 			if (moveOnQueue != 0) {
 				return;
 			} else {
 				switch(direction){
 				case 0:
 					return;
 				case MOVE_UP:
 					moveOnQueue = MOVE_UP;
 					break;
 				case MOVE_DOWN:
 					moveOnQueue = MOVE_DOWN;
 					break;
 				case MOVE_LEFT:
 					moveOnQueue = MOVE_LEFT;
 					break;
 				case MOVE_RIGHT:
 					moveOnQueue = MOVE_RIGHT;
 					break;
 				}
 				return;
 			}
 		}
 		
 		Vector2 playerPosition = mPlayer.getBodyPosition(false);
 		
 		switch(direction){
 		case MOVE_UP:
 			if ((int)playerPosition.y >= PLAYER_HOME_POSITION.y) {
 				rollToPosition = (int)playerPosition.y - LANE_STEP_SIZE;
 				rollToPosition /= PIXEL_TO_METER_RATIO;
 				moveUp = true;
 			} break;
 		case MOVE_DOWN:
 			if ((int)playerPosition.y <= PLAYER_HOME_POSITION.y) {
 				rollToPosition = (int)playerPosition.y + LANE_STEP_SIZE;
 				rollToPosition /= PIXEL_TO_METER_RATIO;
 				moveDown = true;
 			} break;
 		case MOVE_LEFT:
 			if ((int)playerPosition.x >= PLAYER_HOME_POSITION.x) {
 				rollToPosition = (int)playerPosition.x - LANE_STEP_SIZE;
 				rollToPosition /= PIXEL_TO_METER_RATIO;
 				moveLeft = true;
 			} break;
 		case MOVE_RIGHT:
 			if ((int)playerPosition.x <= PLAYER_HOME_POSITION.x) {
 				rollToPosition = (int)playerPosition.x + LANE_STEP_SIZE;
 				rollToPosition /= PIXEL_TO_METER_RATIO;
 				moveRight = true;
 			} break;
 		}
 	}
 	
 	private boolean isMoving() {
 		return (moveLeft == true || moveRight == true || moveUp == true || moveDown == true);
 	}
 	
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 	
 }
