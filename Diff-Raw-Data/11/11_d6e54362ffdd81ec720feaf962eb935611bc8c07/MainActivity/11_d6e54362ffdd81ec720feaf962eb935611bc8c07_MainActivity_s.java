 package com.lolbro.nian;
 
 import org.andengine.engine.camera.SmoothCamera;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
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
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.util.HorizontalAlign;
 
 import android.graphics.Typeface;
 
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
 	
 	private static final int CAMERA_WIDTH = 480;
 	private static final int CAMERA_HEIGHT = 720;
 	
 	private static final int STEPS_PER_SECOND = 60;
 	private static final int MAX_STEPS_PER_UPDATE = 1;
 	
 	private static final int MENU_RETRY = 1;
 	
 	private static final int PLAYER_SIZE = 64;
 
 	private static final float LANE_STEP_SIZE = 140;
 	
 	private static final float PLAYER_ROLL_SPEED = 15f;
 	
 	private static final String PLAYER_USERDATA = "body_player";
 	
 	public static final int JUMP_UP = SwipeListener.DIRECTION_UP;
 	public static final int JUMP_DOWN = SwipeListener.DIRECTION_DOWN;
 	public static final int JUMP_LEFT = SwipeListener.DIRECTION_LEFT;
 	public static final int JUMP_RIGHT = SwipeListener.DIRECTION_RIGHT;
 	
 	public static final Vector2 PLAYER_HOME_POSITION = new Vector2(CAMERA_WIDTH/2, -CAMERA_HEIGHT/2 + PLAYER_SIZE*2);
 	
 //	public static final float LANE_LEFT = PLAYER_HOME_POSITION.x - LANE_STEP_SIZE;
 //	public static final float LANE_MID = PLAYER_HOME_POSITION.x;
 //	public static final float LANE_RIGHT = PLAYER_HOME_POSITION.x + LANE_STEP_SIZE;
 	
 	public static final Vector2 PLAYER_SPRITE_SPAWN = new Vector2(PLAYER_HOME_POSITION.x - PLAYER_SIZE/2, PLAYER_HOME_POSITION.y -PLAYER_SIZE/2);
 	
 	
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	
 	private SmoothCamera mCamera;
 	
 	private SwipeScene mScene;
 	
 	private PhysicsWorld mPhysicsWorld;
 	
 	private BitmapTextureAtlas mCharactersTexture;
 	private ITextureRegion mPlayerRegion;
 	private ITextureRegion mObstacleRegion;
 	
 	private BitmapTextureAtlas mMenuTexture;
 	private ITextureRegion mMenuRetryRegion;
 
 	private MObject mPlayer;
 	private MObject mEnemy;
 	
 	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
 	private ITextureRegion mParallaxLayerBack;
 	
 	private boolean moveLeft = false;
 	private boolean moveRight = false;
 	private boolean moveUp = false;
 	private boolean moveDown = false;
 	
 	private float rollToPosition = 0;
 	
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 	
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		this.mCamera = new SmoothCamera(0, -CAMERA_HEIGHT, CAMERA_WIDTH, CAMERA_HEIGHT, 10 * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 10 * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 10);
 		
 		//new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT)
 		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new FillResolutionPolicy(), this.mCamera);
 	}
 	
 	@Override
 	protected void onCreateResources() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 		
 		this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 512, 64);
 		this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "floor_2.png", 0, 0);
 		this.mAutoParallaxBackgroundTexture.load();
 		
 		this.mCharactersTexture = new BitmapTextureAtlas(this.getTextureManager(), 64, 32, TextureOptions.BILINEAR);
 		this.mPlayerRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mCharactersTexture, this, "player.png", 0, 0);
 		this.mObstacleRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mCharactersTexture, this, "obstacle.png", 32, 0);
 		this.mCharactersTexture.load();
 		
 		this.mMenuTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 64, TextureOptions.BILINEAR);
 		this.mMenuRetryRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mMenuTexture, this, "menu_retry.png", 0, 0);
 		this.mMenuTexture.load();
 	}
 	
 	@Override
 	protected Scene onCreateScene() {
 		
 		createScene();
 		
 		this.mPhysicsWorld = new FixedStepPhysicsWorld(STEPS_PER_SECOND, MAX_STEPS_PER_UPDATE, new Vector2(0, 0), false, 10, 10);
 		
 		this.mPhysicsWorld.setContactListener(this);
 		
 		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
 		
 		initBackground();
 		initPlayer();
 		spawnMob();
 //		showFPS();
 
 		return this.mScene;
 	}
 	
 	@Override
 	public synchronized void onResumeGame() {
 		super.onResumeGame();
 		mScene.registerForGestureDetection(this, this);
 	}
 	
 	@Override
 	public void reset() {
 	}
 	
 	@Override
 	public void onSwipe(int direction) {
 		jump(direction);	
 	}
 	
 	@Override
 	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) {
 		// TODO Auto 
 		return true;
 	}
 	
 	// ===========================================================
 	// On update
 	// ===========================================================
 	
 	@Override
 	public void onUpdate(float pSecondsElapsed) {
 		
 		Body playerBody = mPlayer.getBody();
 
 		float x = mPlayer.getBodyPositionX(true);
 		float y = mPlayer.getBodyPositionY(true);
 		float rollMovement = PLAYER_ROLL_SPEED * pSecondsElapsed;
 		
 		if (moveUp == true) {
			if (y > rollToPosition) {
 				playerBody.setTransform(x, y - rollMovement, 0);
 			} else {
 				moveUp = false;
 				playerBody.setTransform(x, rollToPosition, 0);
 			}
 		} else if (moveDown == true) {
			if (y < rollToPosition) {
 				playerBody.setTransform(x, y + rollMovement, 0);
 			} else {
 				moveDown = false;
 				playerBody.setTransform(x, rollToPosition, 0);
 			}
 		} else if (moveLeft == true) {
			if (x > rollToPosition) {
 				playerBody.setTransform(x - rollMovement, y, 0);
 			} else {
 				moveLeft = false;
 				playerBody.setTransform(rollToPosition, y, 0);
 			}
 		} else if (moveRight == true) {
			if (x < rollToPosition) {
 				playerBody.setTransform(x + rollMovement, y, 0);
 			} else {
 				moveRight = false;
 				playerBody.setTransform(rollToPosition, y, 0);
 			}
 		}
 	}
 	
 	// ===========================================================
 	// On Contact
 	// ===========================================================
 	
 	@Override
 	public void beginContact(Contact contact) {
 		Object userDataA = contact.getFixtureA().getBody().getUserData();
 		Object userDataB = contact.getFixtureB().getBody().getUserData();
 		
 		if(userDataA.equals(PLAYER_USERDATA) || userDataB.equals(PLAYER_USERDATA)){
 			
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
 	
 	private void spawnMob() {
 		
 		this.mEnemy = new MObject(
 				CAMERA_WIDTH/2-PLAYER_SIZE/2,
 				-CAMERA_HEIGHT - PLAYER_SIZE,
 				PLAYER_SIZE,
 				PLAYER_SIZE,
 				this.mObstacleRegion,
 				this.getVertexBufferObjectManager(), 
 				mPhysicsWorld);
 		
 		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mEnemy.getSprite(), mEnemy.getBody(), true, false));
 		this.mScene.attachChild(mEnemy.getSprite());
 	}
 	
 	private void initPlayer() {
 
 		this.mPlayer = new MObject(
 				PLAYER_SPRITE_SPAWN.x,
 				PLAYER_SPRITE_SPAWN.y,
 				PLAYER_SIZE,
 				PLAYER_SIZE,
 				this.mPlayerRegion,
 				this.getVertexBufferObjectManager(),
 				mPhysicsWorld);
 		
 		this.mPlayer.getBody().setUserData(PLAYER_USERDATA);
 		
 		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mPlayer.getSprite(), mPlayer.getBody(), true, false));
 		this.mScene.attachChild(mPlayer.getSprite());
 	}
 	
 	private void createScene() {
 		this.mScene = new SwipeScene();
 		
 //		final SpriteMenuItem retryMenuItem = new SpriteMenuItem(MENU_RETRY, this.mMenuRetryRegion, this.getVertexBufferObjectManager());
 //		retryMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 //		this.mScene.addMenuItem(retryMenuItem);
 //		
 //		this.mScene.buildAnimations();
 		
 		this.mScene.registerUpdateHandler(this);
 		
 //		this.mScene.setOnMenuItemClickListener(this);
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
 	
 	/* Methods for moving */
 	
 	private void jump(int direction){
 		if(isMoving()){
 			return;
 		}
 		
 		Vector2 playerPosition = mPlayer.getBodyPosition(false);
 		
 		switch(direction){
 		case JUMP_UP:
 			if ((int)playerPosition.y >= PLAYER_HOME_POSITION.y) {
 				rollToPosition = (int)playerPosition.y - LANE_STEP_SIZE;
 				rollToPosition /= PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 				moveUp = true;
 			} break;
 		case JUMP_DOWN:
 			if ((int)playerPosition.y <= PLAYER_HOME_POSITION.y) {
 				rollToPosition = (int)playerPosition.y + LANE_STEP_SIZE;
 				rollToPosition /= PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 				moveDown = true;
 			} break;
 		case JUMP_LEFT:
 			if ((int)playerPosition.x >= PLAYER_HOME_POSITION.x) {
 				rollToPosition = (int)playerPosition.x - LANE_STEP_SIZE;
 				rollToPosition /= PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 				moveLeft = true;
 			} break;
 		case JUMP_RIGHT:
 			if ((int)playerPosition.x <= PLAYER_HOME_POSITION.x) {
 				rollToPosition = (int)playerPosition.x + LANE_STEP_SIZE;
 				rollToPosition /= PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
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
