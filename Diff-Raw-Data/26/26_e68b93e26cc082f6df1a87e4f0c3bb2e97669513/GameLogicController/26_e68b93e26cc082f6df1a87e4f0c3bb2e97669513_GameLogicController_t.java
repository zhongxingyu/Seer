 package falldowndizzy.pac;
 
 import java.io.IOException;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.options.AudioOptions;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.MusicOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import org.andengine.audio.sound.Sound;
 import org.andengine.audio.sound.SoundFactory;
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.background.RepeatingSpriteBackground;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.opengl.texture.Texture;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.entity.scene.Scene;
 import org.andengine.input.sensor.acceleration.AccelerationData;
 import org.andengine.input.sensor.acceleration.IAccelerationListener;
 import org.andengine.ui.activity.BaseGameActivity;
 
 import falldowndizzy.pac.GameLogicController;
 import falldowndizzy.pac.LevelController;
 import falldowndizzy.pac.PlayerProfileManager;
 
 import com.badlogic.gdx.math.Vector2;
 
 public class GameLogicController extends BaseGameActivity implements IAccelerationListener{
 
 	PlayerProfileManager playerProfileManager;
 	
 	public LevelController levelController;
 
 	private Camera camera;
 	private RatioResolutionPolicy resolution;
 	protected PhysicsWorld mPhysicsWorld;
 
 	public Texture mTexture;
 	public TextureRegion enemyTextureRegion;
 	
 	public Texture mFinishLineTexture;
 	public TextureRegion mFinishLineTextureRegion;
 	
 	public Texture mBlockTexture;
 	public TextureRegion mBlockTextureRegion;
 	
 	private float mGravityX;
 	private float mGravityY;
 	private final Vector2 mTempVector = new Vector2();
 
 	public TiledTextureRegion mCircleFaceTextureRegion;
 	
 	public Texture mEnemyTexture;
 	
 	public Texture mDiamantTexture;
 	public TextureRegion mDiamantTextureRegion;
 	
	private RepeatingSpriteBackground mMenuBackground;	
 	private RepeatingSpriteBackground mSky; 
 	
 	public Texture mBackgroundTexture;
 	public TextureRegion mBackgroundTextureRegion;
 
 //  Audio *****************************************	
 
 	private Sound jampingSound; 
 	private Sound getGoodsSound;
 	private Sound mGameOverSound;
 	private Music backgroundMenuMusic;
     private Music backgroundGameMusic;
     
 //  Audio *****************************************	
     
     
 	public Texture mLevelMenuTexture;
 	public TextureRegion mLevelTextureRegion;
 	
 	static GameLogicController gameLogicController;
 	
 	public static GameLogicController getInstance(){return gameLogicController;}
 	
 	int currentPage;
 
 	public Engine onLoadEngine() {
 			
 		currentPage = 0;
 		playerProfileManager = new PlayerProfileManager(this);
 
 		gameLogicController = this;
 		levelController = new LevelController(this);
 
		levelController.mCameraWidth = 640;
 		levelController.mCameraHeight = 320;
 
 		camera = new Camera(0, 0, levelController.mCameraWidth, levelController.mCameraHeight);
 		resolution = new RatioResolutionPolicy(levelController.mCameraWidth, levelController.mCameraHeight);
 		return new Engine(new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, resolution, camera));
 		
 	}
 	
 	public void onLoadResources(){
 		
 		SoundFactory.setAssetBasePath("mfx/");
 		try {
 			jampingSound = SoundFactory.createSoundFromAsset(
 					mEngine.getSoundManager(), this, "pew_pew_lei.wav");
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		SoundFactory.setAssetBasePath("mfx/");
 		try {
 			getGoodsSound = SoundFactory.createSoundFromAsset(
 					mEngine.getSoundManager(), this, "pew_pew_lei.wav");
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		SoundFactory.setAssetBasePath("mfx/");
 		try {
 			mGameOverSound = SoundFactory.createSoundFromAsset(
 					mEngine.getSoundManager(), this, "pew_pew_lei.wav");
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}		
 
 		MusicFactory.setAssetBasePath("mfx/");
 
 		try {
 			backgroundMenuMusic = MusicFactory.createMusicFromAsset(
 					mEngine.getMusicManager(), this, "background_music.wav");
 			backgroundMenuMusic.setLooping(true);
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			backgroundGameMusic = MusicFactory.createMusicFromAsset(
 					mEngine.getMusicManager(), this, "background_music.wav");
 			backgroundGameMusic.setLooping(true);
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}		
 		
 	}	
 	
 	
 	
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void onCreateResources(
 			OnCreateResourcesCallback pOnCreateResourcesCallback)
 			throws Exception {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
 			throws Exception {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onPopulateScene(Scene pScene,
 			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onAccelerationChanged(AccelerationData pAccelerationData) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
