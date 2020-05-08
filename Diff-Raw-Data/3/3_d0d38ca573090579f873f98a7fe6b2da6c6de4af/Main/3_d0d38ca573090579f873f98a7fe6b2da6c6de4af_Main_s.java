 package fi.hackoid;
 
 import java.io.IOException;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.AutoParallaxBackground;
 import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.extension.physics.box2d.util.Vector2Pool;
 import org.andengine.input.sensor.acceleration.AccelerationData;
 import org.andengine.input.sensor.acceleration.IAccelerationListener;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.util.debug.Debug;
 
 import android.hardware.SensorManager;
 import android.media.MediaPlayer;
 import android.util.Log;
 import android.view.KeyEvent;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 public class Main extends SimpleBaseGameActivity implements IAccelerationListener {
 
 	private static final int CAMERA_WIDTH = 1280;
 	private static final int CAMERA_HEIGHT = 720;
 
 	private BitmapTextureAtlas backgroundTextureAtlas;
 
 	private ITextureRegion backgroundTextureSky;
 	private ITextureRegion backgroundTextureCity;
 	private ITextureRegion backgroundTextureGround;
 
 	private BitmapTextureAtlas controlTextureAtlas;
 	private ITextureRegion horizontalControlTexture;
 	private ITextureRegion jumpControlTexture;
 	private ITextureRegion fireControlTexture;
 
 	private Camera camera;
 	private AutoParallaxBackground autoParallaxBackground;
 
 	private Player player = new Player();
 	private Enemy enemy = new Enemy();
 	
 	private PhysicsWorld world;
 	
 	private Stats stats;
 	
 	private Music mMusic;
 	
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		camera = new CustomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 
 		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH,
 				CAMERA_HEIGHT), camera);
 		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
 		
 		engineOptions.getAudioOptions().setNeedsMusic(true);
 		
 		return engineOptions;
 	}
 
 	@Override
 	public void onCreateResources() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		player.createResources(this);
 		enemy.createResources(this);
 
 		this.backgroundTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 2048, 2048);
 		this.backgroundTextureGround = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				this.backgroundTextureAtlas, this, "background_texture_ground.png", 0, 0);
 		this.backgroundTextureSky = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				this.backgroundTextureAtlas, this, "background_texture_sky.png", 0, 200);
 		this.backgroundTextureCity = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				this.backgroundTextureAtlas, this, "background_texture_city.png", 0, 920);
 		this.backgroundTextureAtlas.load();
 
 		this.controlTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024);
 		this.horizontalControlTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				this.controlTextureAtlas, this, "touchscreen_horizontal_control.png", 0, 0);
 		this.jumpControlTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.controlTextureAtlas,
 				this, "touchscreen_button_jump.png", 0, 190);
 		this.fireControlTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.controlTextureAtlas,
 				this, "touchscreen_button_fire.png", 210, 190);
 		this.controlTextureAtlas.load();
 		
 		MusicFactory.setAssetBasePath("mfx/");
 		try {
 			this.mMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "tsarpfSong10min.ogg");
 			
 			this.mMusic.setLooping(true);
 		} catch (final IOException e) {
 			Debug.e(e);
 		}
 		/*
 		MediaPlayer mediaPlayer = MediaPlayer
                 .create(getApplicationContext(), R.mfx.);
         try {
             mediaPlayer.start();
             mediaPlayer.setLooping(true);
         } catch (Exception e) {
             e.printStackTrace();
         }
 		
 		*/
 		
 		
 	}
 
 	boolean firstRun = true;
 	@Override
 	public Scene onCreateScene() {
 		
 		IUpdateHandler iUpdate = new IUpdateHandler(){
 			@Override
 			public void onUpdate(float pSecondsElapsed) {
 				// TODO Auto-generated method stub
 				enemy.getPhysicsBody().setLinearVelocity(new Vector2(-1,0));
 				if(firstRun)
 				{
 					mMusic.play();
 				}
 			}
 
 			@Override
 			public void reset() {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			
 		};
 		
 		this.mEngine.registerUpdateHandler(iUpdate);
 
 		final Scene scene = new Scene();
 		autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 0);
 		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
 		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, CAMERA_HEIGHT
 				- this.backgroundTextureSky.getHeight(), this.backgroundTextureSky, vertexBufferObjectManager)));
 		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 300, this.backgroundTextureCity,
 				vertexBufferObjectManager)));
 		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT
 				- this.backgroundTextureGround.getHeight(), this.backgroundTextureGround, vertexBufferObjectManager)));
 		scene.setBackground(autoParallaxBackground);
 
 		createControllers();
 		
 
 
 		
 		this.world = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
 		
 		player.createScene(vertexBufferObjectManager, CAMERA_WIDTH, CAMERA_HEIGHT, world);
 		enemy.createScene(vertexBufferObjectManager, CAMERA_WIDTH, CAMERA_HEIGHT, world);
 		
 		scene.attachChild(player.getAnimatedSprite());
 		scene.attachChild(enemy.getAnimatedSprite());
 		
 		camera.setChaseEntity(player.getAnimatedSprite());
 		camera.setCenter(camera.getCenterX(), camera.getCenterY() - 200);
 		
 		final Rectangle ground = new Rectangle(-99999, 327, 99999999, 10, vertexBufferObjectManager);
 		ground.setColor(0,0,0,0);
 		
 		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
 		
 		PhysicsFactory.createBoxBody(this.world, ground, BodyType.StaticBody, wallFixtureDef);
 		
 		scene.attachChild(ground);
 		
 		scene.registerUpdateHandler(this.world);
 		
 
 		
 
 		
 		return scene;
 	}
 
 	private void createControllers() {
 		HUD yourHud = new HUD();
 		stats = new Stats(yourHud, this.camera);
 		stats.createResources(this);
 		stats.createScene(this.getVertexBufferObjectManager());
 		final int xSize = 380;
 		final int ySize = 150;
 
 		final Sprite horizontalControl = new Sprite(0, 570, xSize, ySize, horizontalControlTexture,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				float playerSpeed = 0;
 				if (!touchEvent.isActionUp()) {
 					if (X < (xSize / 2)) {
 						playerSpeed = (xSize / 2) - X;
 						playerSpeed = -playerSpeed;
 					} else {
 						playerSpeed = (X - xSize / 2) + 100;
 					}
 				}
 				if (xSize - X < 100 || Y < 80) {
 					playerSpeed = 0;
 				}
 				playerSpeed *= 0.75;
 				autoParallaxBackground.setParallaxChangePerSecond(playerSpeed / 5);
 				player.run(playerSpeed);
 				Log.w("debug", "horizontal control clicked: X: '" + X + "' Y: '" + Y + "'");
 				return true;
 			};
 		};
 		yourHud.registerTouchArea(horizontalControl);
 		yourHud.attachChild(horizontalControl);
 
 		final Sprite jumpControl = new Sprite(1070, 300, jumpControlTexture,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				player.jump();
 				return true;
 			};
 		};
 		yourHud.registerTouchArea(jumpControl);
 		yourHud.attachChild(jumpControl);
 
 		final Sprite fireControl = new Sprite(1070, 510, fireControlTexture,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				Log.w("debug", "fire pressed");
 				// fire
 				return true;
 			};
 		};
 		yourHud.registerTouchArea(fireControl);
 		yourHud.attachChild(fireControl);
 		this.camera.setHUD(yourHud);
 	}
 	
 	@Override
 	public void onAccelerationChanged(final AccelerationData pAccelerationData) {
 		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());
 		this.world.setGravity(gravity);
 		Vector2Pool.recycle(gravity);
 	}
 
 	@Override
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if (pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			if (mEngine.isRunning()) {
 				mEngine.stop();
 			} else {
 				mEngine.start();
 			}
 			return true;
 		} else {
 			return super.onKeyDown(pKeyCode, pEvent);
 		}
 	}
 
 	@Override
 	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
 		// TODO Auto-generated method stub
 		
 	}
 }
