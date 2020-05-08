 package dragonsreign.scene;
 
 import java.io.IOException;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.LimitedFPSEngine;
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.WakeLockOptions;
 import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.ui.activity.BaseGameActivity;
 
 import android.view.KeyEvent;
 import dragonsreign.character.PlayerCharacter;
 import dragonsreign.character.characterclass.ClericClass;
 import dragonsreign.character.characterclass.RangerClass;
 import dragonsreign.character.characterclass.WarriorClass;
 import dragonsreign.item.Inventory;
 import dragonsreign.manager.ResourceManager;
 import dragonsreign.manager.SceneManager;
 import dragonsreign.manager.SoundManager;
 
 public class DragonsReignActivity extends BaseGameActivity {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	protected static final int CAMERA_WIDTH = 800;
 	protected static final int CAMERA_HEIGHT = 480;
 
 	private static DragonsReignActivity INSTANCE = new DragonsReignActivity();
 
 
 	public static final float FADE_DURATION = 0.5f;
 	public static final int TILE_WIDTH = 32;
 	public static final int TILE_HEIGHT = 32;
 	public static final long ANIM_SPEED = 150;
 	public static final long[] WALK_ANIMATE_DURATION = new long[] {ANIM_SPEED, ANIM_SPEED, ANIM_SPEED, ANIM_SPEED};
 	public static final float TOUCH_MOVEMENT_WALKING_SPEED = 0.50f;
 	public static final float DPAD_MOVEMENT_WALKING_SPEED = 1f;
 	protected static final float CAMERA_SCROLLING = 0.25f;
 	private static final int FRAMES_PER_SECOND = 60;
 	public static final float RUNNING_SPEED = 0.45f;
 	public static final float WALKING_SPEED = 0.65f;
 	public static final int Y_COORDINATE = 1;
 	public static final int X_COORDINATE = 0;
 	public static final int UP_CENTER_TILE = 4;
 	public static final int DOWN_CENTER_TILE = 1;
 	public static final int RIGHT_CENTER_TILE = 10;
 	public static final int LEFT_CENTER_TILE = 7;
 	public static final float ANIMATE_TILE_DURATION = 0.5f;
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private WarriorClass mWarrior;
 	private RangerClass mRanger;
 	private ClericClass mCleric;
 
 	private PlayerCharacter[] currentParty = new PlayerCharacter[3];
 	
 	private int partySize;
 	
 	private Inventory inventory;
 	
 	public  BoundCamera mCamera;
 	//private ResourceManager resourceManager;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	public DragonsReignActivity() 
 	{
 		currentParty[0] = null;
 		currentParty[1]	= null;
 		currentParty[2] = null;
 		
 		partySize = 0;
 	}
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	public PlayerCharacter getPartyMember(int partyMem){
     	return currentParty[partyMem];
     }
 	
 	public Inventory getInventory(){
 		return inventory;
 	}
 
 	public void setParty(int pClassChoice) {
 		if(pClassChoice == 0) {
 			this.currentParty[0] = mWarrior;
			this.currentParty[1] = mCleric;
 			
			partySize = 2;
 		} else if(pClassChoice == 1) {
 			this.currentParty[0] = mRanger;
 			this.currentParty[1] = mCleric;
 			
 			partySize = 2;
 		} else if(pClassChoice == 2) {
 			this.currentParty[0] = mCleric;
 			this.currentParty[1] = mWarrior;
 			
 			partySize = 2;
 		}
 		
 	}
 
 	public int getPartySize() {
 		return partySize;
 	}
 	
 	public WarriorClass getWarrior() {
 		return mWarrior;
 	}
 
 	public RangerClass getRanger() {
 		return mRanger;
 	}
 
 	public ClericClass getCleric() {
 		return mCleric;
 	}
 	
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	@Override
 	public Engine onCreateEngine(EngineOptions pEngineOptions) 
 	{
 		
 	    return new LimitedFPSEngine(pEngineOptions, 60);
 	}
 	
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) 
     {  
         if (keyCode == KeyEvent.KEYCODE_BACK)
         {
             SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
         }
         return false; 
     }
     
     @Override
     protected void onPause()
     {
     	super.onPause();
     	if (this.isGameLoaded())
     		SoundManager.mMenuThemeMusic.pause();
           
     }
 
     @Override
     protected synchronized void onResume()
     {
     	super.onResume();
     	if(this.isGameLoaded())
     		SoundManager.mMenuThemeMusic.resume();
     }
     
     @Override
     protected void onDestroy()
     {
         super.onDestroy();
             
         if (this.isGameLoaded())
         {
             System.exit(0);    
         }
     }
 	
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
     public EngineOptions onCreateEngineOptions()
 	{
 	    mCamera = new BoundCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 	    EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,  new FillResolutionPolicy(), this.mCamera);
 	    engineOptions.getAudioOptions().setNeedsMusic(true);
 	    engineOptions.getAudioOptions().setNeedsSound(true);
 	    engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
 	    return engineOptions;
 	}
 
 	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws IOException
 	{
 	    ResourceManager.prepareManager(mEngine, this, mCamera, getVertexBufferObjectManager());
 	    pOnCreateResourcesCallback.onCreateResourcesFinished();
 	}
 
     
 	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws IOException
     {
 		SceneManager.getInstance().Initialize(this);
     	SceneManager.getInstance().createSplashScene(pOnCreateSceneCallback);
     	//SoundManager.mMenuMusic.play();
     	
     	//pOnCreateSceneCallback.onCreateSceneFinished(SceneManager.getInstance().getCurrentScene());
     	
     }
 
     public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws IOException
     {
     	
     	
     	//Create Splash Scene
     	mEngine.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback() 
         {
                 public void onTimePassed(final TimerHandler pTimerHandler) 
                 {
                     mEngine.unregisterUpdateHandler(pTimerHandler);
                    
                 }
         }));
         pOnPopulateSceneCallback.onPopulateSceneFinished();
         
         //Create Menu Scene
         mEngine.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback() 
         {
             public void onTimePassed(final TimerHandler pTimerHandler) 
             {
                 mEngine.unregisterUpdateHandler(pTimerHandler);
                 SceneManager.getInstance().loadMenuScene(mEngine);
                 //SoundManager.mMenuThemeMusic.play();
             }
         }));
         pOnPopulateSceneCallback.onPopulateSceneFinished();
         
     }
 
     public static DragonsReignActivity getInstance()
     {
         return INSTANCE ;
     }
     
     public void characterSetup(){
     	
 		mWarrior = new WarriorClass();
 		
 		mRanger = new RangerClass();
     	
 		mCleric = new ClericClass();
     	
 		inventory = new Inventory();
     }
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 }
