 package topplintowers.scenes;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.entity.modifier.AlphaModifier;
 import org.andengine.entity.modifier.MoveXModifier;
 import org.andengine.entity.modifier.SequenceEntityModifier;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.util.GLState;
 import org.andengine.util.color.Color;
 
 import topplintowers.ResourceManager;
 import topplintowers.pools.PoolManager;
 
 public class GameSceneBackground {
     private ArrayList<Sprite> twinklingStars;
     private ArrayList<Sprite> mActiveCloudList;
     
     private static Sprite mBackgroundTop, mBackgroundMiddle, mBackgroundBottom;
     private Color mBackgroundColor;
     private int mBackgroundHeight;
     private GameScene scene;
     
    
     GameSceneBackground(GameScene gameScene) {
     	scene = gameScene;
     	
 		mBackgroundColor = new Color(0.06667f, 0.07059f, 0.18823f, 1);
 		scene.setBackground(new Background(mBackgroundColor));
 		
 		mBackgroundBottom 	= createBackgroundPortion(ResourceManager.mGameBackgroundBottomTextureRegion, -544);
 		mBackgroundMiddle 	= createBackgroundPortion(ResourceManager.mGameBackgroundMiddleTextureRegion, mBackgroundBottom.getY() - 1024);
 		mBackgroundTop 		= createBackgroundPortion(ResourceManager.mGameBackgroundTopTextureRegion, mBackgroundMiddle.getY() - 1024);
 
 		mBackgroundHeight = 3072;
 		
 		createClouds();
 		createStars();
     }
     
     private Sprite createBackgroundPortion(TextureRegion tr, float posY) {
 		Sprite backgroundSprite = new Sprite(0, 0, tr, scene.vbom) {
 	    	@Override
 		     protected void preDraw(GLState pGLState, Camera pCamera)
 		     {
 		            super.preDraw(pGLState, pCamera);
 		            pGLState.enableDither();
 		     }
 	    };
 	    
 	    backgroundSprite.setWidth(800);
 	    backgroundSprite.setPosition(0, posY);
 	    scene.getContainer().attachChild(backgroundSprite);
 	    
 	    return backgroundSprite;
     }
     
     public int getHeight() { return mBackgroundHeight; }
 	
 	private void createClouds() {	
 		mActiveCloudList = new ArrayList<Sprite>();
 		scene.registerUpdateHandler(new TimerHandler(1.5f, true, new ITimerCallback() {
 			@Override
 			public void onTimePassed(final TimerHandler pTimerHandler) {
 				spawnCloud();
 			}
 		}));
 	}
 	
 	private void spawnCloud() {
 		Sprite newCloud = PoolManager.getInstance().mCloudPool.obtainPoolItem();
 		
 		newCloud.setCullingEnabled(true);
 		scene.getContainer().attachChild(newCloud);
 		
 		float startPosX = -newCloud.getWidth();
 		float startPosY = ((float)Math.random() * -700) - 600;
 		newCloud.setPosition(startPosX, startPosY);
 		
 		newCloud.setAlpha((float)Math.random());
 
 		float randomSpeed = ((float)Math.random() * 20) + 40;
 		MoveXModifier moveToRight = new MoveXModifier(randomSpeed, newCloud.getX(), scene.camera.getWidth());
 		moveToRight.setAutoUnregisterWhenFinished(true);
 		newCloud.registerEntityModifier(moveToRight);
 		
 		mActiveCloudList.add(newCloud);
 	}
 	
 	public void cleanClouds() {
 		cleanClouds(false);
 	}
 	
 	public void cleanClouds(boolean dispose) {
     	Iterator<Sprite> it = mActiveCloudList.iterator();
     	
     	while (it.hasNext()) {
     		Sprite currentCloud = it.next();
     		
     		if (dispose || currentCloud.getX() >= 800) {
     			scene.getContainer().detachChild(currentCloud);
     			PoolManager.getInstance().mCloudPool.recyclePoolItem(currentCloud);
    			mActiveCloudList.remove(currentCloud);
     		}
     	}
 	}
 	
 	private void createStars() {
 		twinklingStars = new ArrayList<Sprite>();
 		for (int i = 0; i < 300; i++) {
 			int randomStar = (int)((float)Math.random() * 3);
 			TextureRegion starTexture = ResourceManager.mStarTextureRegions.get(randomStar);
 			Sprite newStar = new Sprite(0, 0, starTexture, scene.vbom);
 			newStar.setCullingEnabled(true);
 			scene.getContainer().attachChild(newStar);
 			
 			newStar.setScaleCenter(0, 0);
 			
 			float randomScale = ((float)Math.random() * 0.3f) + 0.2f;
 			newStar.setScale(randomScale);
 			
 			float starPosX = ((float)Math.random() * scene.camera.getWidth());
 			float starPosY = ((float)Math.random() * -2500) - 2000;
 			newStar.setPosition(starPosX, starPosY);
 			
 			float randomAlpha = (float)Math.random();			
 			newStar.setAlpha(randomAlpha);
 			
 			if (i % 5 == 0) {
 				twinklingStars.add(newStar);
 			}
 		}
 		
 		scene.registerUpdateHandler(new TimerHandler(3f, true, new ITimerCallback() {
 			@Override
 			public void onTimePassed(final TimerHandler pTimerHandler) {
 				twinkleStars();
 			}
 		}));
 	}
 	
 	public void cleanStars() {
     	Iterator<Sprite> it = twinklingStars.iterator();
     	
     	while (it.hasNext()) {
     		Sprite currentStar = it.next();
     		scene.getContainer().detachChild(currentStar);
     		twinklingStars.remove(currentStar);
     	}
 	}
 	
 	private void twinkleStars() {
 		for (Sprite current : this.twinklingStars) {
 			AlphaModifier am_1, am_2;
 			
 			float oldAlpha = current.getAlpha();
 			float newAlpha = 0;
 			
 			if (oldAlpha >= 0.5f) {
 				newAlpha = oldAlpha - 0.5f;
 			} else {
 				newAlpha = oldAlpha + 0.5f;
 			}
 			
 			am_1 = new AlphaModifier(1.5f, oldAlpha, newAlpha);
 			am_2 = new AlphaModifier(1.5f, newAlpha, oldAlpha);
 			
 			am_1.setAutoUnregisterWhenFinished(true);
 			am_2.setAutoUnregisterWhenFinished(true);
 			
 			SequenceEntityModifier sem = new SequenceEntityModifier(am_1, am_2);
 			sem.setAutoUnregisterWhenFinished(true);
 			current.clearEntityModifiers();
 			current.registerEntityModifier(sem);
 			
 		}
 	}
 }
