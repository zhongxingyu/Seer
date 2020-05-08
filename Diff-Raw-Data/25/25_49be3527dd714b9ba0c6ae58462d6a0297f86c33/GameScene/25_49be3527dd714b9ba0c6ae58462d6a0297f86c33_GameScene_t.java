 package topplintowers.scenes;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import javax.microedition.khronos.opengles.GL10;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.Entity;
 import org.andengine.entity.modifier.AlphaModifier;
 import org.andengine.entity.modifier.IEntityModifier;
 import org.andengine.entity.modifier.MoveYModifier;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.input.touch.detector.ScrollDetector;
 import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
 import org.andengine.input.touch.detector.SurfaceScrollDetector;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.util.modifier.ease.EaseCubicOut;
 
 import android.hardware.SensorManager;
 import com.badlogic.gdx.math.Vector2;
 
 import topplintowers.Platform;
 import topplintowers.crates.Crate;
 import topplintowers.crates.CrateType;
 import topplintowers.hud.MyHUD;
 import topplintowers.levels.Level;
 import topplintowers.levels.Levels;
 import topplintowers.scenes.SceneManager.SceneType;
 
 public class GameScene extends BaseScene implements IOnSceneTouchListener, IScrollDetectorListener  {
 	
 	public static PhysicsWorld mPhysicsWorld;
 	
 	public Platform mPlatform;
 	private Entity container;
 		
 	public static Hashtable<CrateType, ArrayList<Crate>> activeCrates = new Hashtable<CrateType, ArrayList<Crate>>();
     
     public static MyHUD mHud;
     private GameSceneBackground mBackground;
     
     private Level level;
 
     // Scrolling
     private SurfaceScrollDetector mScrollDetector;
 	private float mMinY = 0;
     private float mMaxY = 3200;
     private float mCurrentY = 0;
     private float lastMove;
     private static float INERTIA_DURATION = 0.5f;
     private static float INERTIA_COEF = 5;
     private MoveYModifier inertiaMove;
     
 
 	public Camera getCamera() { return camera; }
     public PhysicsWorld getPhysicsWorld() { return mPhysicsWorld; }
     public VertexBufferObjectManager getVBOM() { return vbom; }
     public static GameScene getScene() { return (GameScene)SceneManager.getInstance().getCurrentScene(); }
     public Entity getContainer() { return container; }
     
     public GameScene(Level level) {
 		this.level = level;
 		
 		if (level.getLevelType() != Levels.FREEMODE) {
 			level.setGoal(this);
 		}
 		
 		initializeActiveCrateList();	
 		createHUD();
 	}
 	
 	public float getHighestCrate() {
 		float highest = 0;
 		Enumeration<CrateType> crateTypes = activeCrates.keys();
 		while (crateTypes.hasMoreElements()) {
 			CrateType type = (CrateType) crateTypes.nextElement();
 			ArrayList<Crate> currentList = activeCrates.get(type);
 			int size = currentList.size();
 			for (int j = 0; j < size; j++) {
 				Crate currentCrate = currentList.get(j);
 				if (currentCrate.getSprite().getY() > highest) {  
 					highest = currentCrate.getSprite().getY(); 
 				}
 			}
 		}
 		return highest;
 	}
 	
 	private void initializeActiveCrateList() {
 		for (CrateType type : CrateType.values()) {
 			activeCrates.put(type, new ArrayList<Crate>());
 		}
 	}
 	
 	@Override
 	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {	
 		Enumeration<CrateType> crateTypes = activeCrates.keys();
 		while (crateTypes.hasMoreElements()) {
 			CrateType type = (CrateType) crateTypes.nextElement();
 			ArrayList<Crate> currentList = activeCrates.get(type);
 			int size = currentList.size();
 			for (int j = 0; j < size; j++) {
 				Crate currentCrate = currentList.get(j);
 				if (currentCrate.getSprite().contains(pSceneTouchEvent.getX(), pSceneTouchEvent.getY())) {
 					currentCrate.onAreaTouched(pSceneTouchEvent, currentCrate.getSprite(), pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
 					return false;
 				}
 			}
 		}
 				
 		this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
 		return true;
 	}
 
 	@Override
 	public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
 		Sprite scrollBar = mHud.getScrollBar();
 		scrollBar.clearUpdateHandlers();
 		scrollBar.clearEntityModifiers();
 		scrollBar.setVisible(true);
 		scrollBar.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		IEntityModifier alphaMod = new AlphaModifier(0.1f, 1, 1);	
 		int childCount = scrollBar.getChildCount();
 		for (int i = 0; i < childCount; i++) {
 			Sprite current = (Sprite) scrollBar.getChildByIndex(i);
 			current.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 			current.registerEntityModifier(alphaMod);
 		}
 		scrollBar.registerEntityModifier(alphaMod);
 		
 		container.unregisterEntityModifier(inertiaMove);
 		inertiaMove = null;
 		mCurrentY = container.getY();
 	}
 
 	@Override
 	public void onScroll(ScrollDetector pScollDetector, int pPointerID,	float pDistanceX, float pDistanceY) {
 		float screenBottom = camera.getCenterY() + camera.getHeight()/2;
 		float screenTop = camera.getCenterY() - camera.getHeight()/2;
 		if (screenBottom - pDistanceY < camera.getHeight() &&
 			screenTop - pDistanceY > camera.getHeight() - mBackground.getHeight())
 		{	
 			camera.offsetCenter(0, -pDistanceY);
 			mHud.updateWithScroll(pDistanceY, mBackground.getHeight());
 //			mHud.updateScrollBar(pDistanceY, backgroundHeight);
 		}
 		
 		lastMove = pDistanceY;
 		
         //Return if ends are reached
 		float next = mCurrentY + pDistanceY;
 		
 		if (next < mMinY) 	  next = mMinY;  
 		else if(next > mMaxY) next = mMaxY;
         
         //Center camera to the current point
 		mCurrentY = next;
         container.setPosition(0, mCurrentY);
 	}
 
 	@Override
 	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
 		final Sprite scrollBar = mHud.getScrollBar();
 		scrollBar.registerUpdateHandler(new TimerHandler(1, false, new ITimerCallback()
 		{
 	        @Override
 	        public void onTimePassed(final TimerHandler pTimerHandler)
 	        { 	
 	    		IEntityModifier alphaMod = new AlphaModifier(0.5f, 1, 0);	
 	    		int childCount = scrollBar.getChildCount();
 	    		for (int i = 0; i < childCount; i++) {
 	    			Sprite current = (Sprite) scrollBar.getChildByIndex(i);
 	    			current.registerEntityModifier(alphaMod);
 	    		}
 	    		scrollBar.registerEntityModifier(alphaMod);
 	        }
 		}));
 		
 		float next = mCurrentY + lastMove*INERTIA_COEF;
 		if ( (next < mMinY)  ){    
 			next = mMinY;  
 	    }else if(next > mMaxY){  
 			next = mMaxY;
 	    }
 		
 		inertiaMove = new MoveYModifier(INERTIA_DURATION, mCurrentY, next, EaseCubicOut.getInstance());
 		inertiaMove.setAutoUnregisterWhenFinished(true);
 		container.registerEntityModifier(inertiaMove);
 	}
 	
 	public void cleaner() {
 	    synchronized (this) {
 	    	mBackground.cleanClouds();
 	    	cleanCrates();
 	    }
 	}
 	
 	private void cleanCrates() {
 		cleanCrates(false);
 	}
 	
 	private void cleanCrates(boolean dispose) {
 		LinkedHashMap<CrateType, Integer> crateCounts = MyHUD.mAvailableCrateCounts;
		ArrayList<Crate> removeList = new ArrayList<Crate>();
 		
 		Iterator<CrateType> it = crateCounts.keySet().iterator();
 		
 		while (it.hasNext()) {
 			CrateType type = it.next();
 			
 			Iterator<Crate> crateIt = activeCrates.get(type).iterator();
 			while (crateIt.hasNext()) {
 				Crate currentCrate = crateIt.next();
 				Sprite currentSprite = currentCrate.getSprite();
 				
 				if (dispose || currentSprite.getY() > camera.getHeight()) {
					//activeCrates.get(type).remove(currentCrate);
					crateIt.remove();
 					int crateCount = crateCounts.get(type);
 					if (crateCount == 0) {
 						mHud.updateHUDWithReturningBlock(type);
 					}
 					
 					crateCounts.put(type, crateCount + 1);
 					currentCrate.dispose();
					removeList.add(currentCrate);
 				}
 			}
 		}
		
//		Iterator<Crate> removeIt = removeList.iterator();
//		while (removeIt.hasNext()) {
//			Crate currentCrate = removeIt.next();
//			activeCrates.get(currentCrate.getType()).remove(currentCrate);
//		}
//		removeList.clear();
		
 	}
 	
 	@Override
 	public void createScene() {
 		container = new Entity();
 		createPhysics();
 		mBackground = new GameSceneBackground(this);
 		createPlatform();
 		
     	setOnSceneTouchListener(this);
 		this.mScrollDetector = new SurfaceScrollDetector(this);
 		
 		registerUpdateHandler(new TimerHandler(1f / 10.0f, true, new ITimerCallback() {
 	          @Override
 	          public void onTimePassed(final TimerHandler pTimerHandler) {
 	          	cleaner();
 	          	mHud.updateCounts();
 	          }
 	  	}));
 	
 		attachChild(container);
 	}
 	
 	private void createPlatform() {
 		mPlatform = new Platform(this);
 		container.attachChild(mPlatform.getSprite());
 	}
 	
 	private void createHUD() {
 		boolean freeMode = this.level.getLevelType() == Levels.FREEMODE;
 		mHud = new MyHUD(this, freeMode, this.level, mBackground.getHeight());
 		camera.setHUD(mHud);
 	}
 	
 	private void createPhysics() {
 		mPhysicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, SensorManager.GRAVITY_EARTH), false);
 		registerUpdateHandler(mPhysicsWorld);
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		if (hasChildScene()) {
 			BaseScene childScene = (BaseScene) getChildScene();
 			childScene.onBackKeyPressed();
 		} else {
 			SceneManager.getInstance().loadPauseScene(engine);
 		}
 	}
 	
 	@Override
 	public void onMenuKeyPressed() { }
 
 	@Override
 	public SceneType getSceneType() { return SceneType.SCENE_GAME; }
 
 	@Override
 	public void disposeScene() {
 		camera.setHUD(null);
 		camera.setCenter(400, 240);
 		
 		mBackground.cleanClouds(true);
 		mBackground.cleanStars();
 		cleanCrates(true);
 	}
 
 
 }
