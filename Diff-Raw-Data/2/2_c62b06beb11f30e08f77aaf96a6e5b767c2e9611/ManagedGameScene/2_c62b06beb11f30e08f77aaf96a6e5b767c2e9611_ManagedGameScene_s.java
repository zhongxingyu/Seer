 package com.codefest2013.game.scenes;
 
 import java.io.IOException;
 
 import com.codefest2013.game.managers.ResourceManager;
 import com.codefest2013.game.scenes.objects.Background;
 import com.codefest2013.game.scenes.objects.Squirrel;
 import com.codefest2013.game.scenes.objects.Player;
 import com.codefest2013.game.scenes.objects.WayPoint;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import org.andengine.entity.Entity;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.input.touch.TouchEvent;
 
 public class ManagedGameScene extends ManagedScene implements IOnSceneTouchListener {
 	private ResourceManager mResourceManager = ResourceManager.getInstance();
 	
 	private Player mPlayer = null;
 	private Squirrel mSquirrel = null;
     private Background mBackground = null;
     private SplashScene mSplashScene = null;
 	private Music mSplashMusic = null;
 	
     public ManagedScene thisManagedGameScene = this;
     
     public ManagedGameScene()
     {
     	this(3.0f);
     }
     
     public ManagedGameScene(float pLoadingScreenMinimumSecondsShown)
     {
     	super(pLoadingScreenMinimumSecondsShown);
     }
     
 	@Override
 	public boolean onSceneTouchEvent(Scene arg0, TouchEvent arg1) {
 		return mPlayer.onSceneTouchEvent(arg0, arg1);
 	}
 
 	@Override
 	public Scene onLoadingScreenLoadAndShown() {
 		mSplashScene = new SplashScene();
         MusicFactory.setAssetBasePath("afx/");
         try {
         	mSplashMusic = MusicFactory.createMusicFromAsset(mResourceManager.engine.getMusicManager(), mResourceManager.context, "splashMusic.mp3");
         } catch (IOException e) {
        e.printStackTrace();
         }
         mSplashMusic.play();
 		return mSplashScene;
 	}
 
 	@Override
 	public void onLoadingScreenUnloadAndHidden() {
 		mSplashScene.detachSelf();
 		mSplashScene = null;
 		mSplashMusic.stop();
 		mSplashMusic.release();
 		mSplashMusic = null;
 	}
 
 	@Override
 	public void onLoadScene() {
 		ResourceManager.loadResources();
 		
 		WayPoint wps[] = {
 			new WayPoint(112, 230, 0, 0, false),
 			new WayPoint(170, 260, 0, 0, false),
 			new WayPoint(238, 120, 0, 0, false),
 			new WayPoint(264, 77, 0, 0, true),
 			new WayPoint(400, 110, 0, 0, false),
 			new WayPoint(424, 154, 45, 0, false),
 			new WayPoint(444, 218, 60, 0, false),
 			new WayPoint(474, 256, 0, 0, true),
 			new WayPoint(512, 182, 0, 0, false),
 			new WayPoint(526, 126, 0, 0, false),
 			new WayPoint(548, 210, 0, 45, false),
 			new WayPoint(595, 286, 0, 0, true),
 			new WayPoint(632, 366, 60, 0, false),
 			new WayPoint(668, 372, 0, 0, false),
 			new WayPoint(726, 264, 0, 0, false),
 			new WayPoint(948, 264, 0, 0, true),
 			new WayPoint(980, 370, 60, 45, false),
 			new WayPoint(1031, 450, 0, 0, false),
 			new WayPoint(1078, 494, 0, 0, false),
 			new WayPoint(1288, 528, 60, 0, true),
 			new WayPoint(1376, 482, 0, 0, false),
 			new WayPoint(1430, 406, 0, 0, false),
 			new WayPoint(1582, 264, 0, 0, false),
 			new WayPoint(1726, 190, 0, 0, true),
 		};
 		
     	mBackground = new Background();
     	mPlayer = new Player(mResourceManager.CAMERA_WIDTH/2, 
     			mResourceManager.CAMERA_HEIGHT-mResourceManager.CAMERA_HEIGHT/5);
 		mSquirrel = new Squirrel(wps);
 		
     	
     	attachChild(mBackground);
 		attachChild(mPlayer);
 		for(int i=0; i<wps.length; i++)
 		{
 			wps[i].x = wps[i].x * mResourceManager.WORLD_SCALE_CONSTANT;
 			wps[i].y = wps[i].y * mResourceManager.WORLD_SCALE_CONSTANT;
 			attachChild(new Rectangle(wps[i].x, wps[i].y, 8, 8, ResourceManager.getInstance().engine.getVertexBufferObjectManager()));
 		}
 		attachChild(mSquirrel);
 		
 		registerUpdateHandler(mPlayer);
 		ResourceManager.getInstance().engine.getCamera().setChaseEntity(mPlayer);
 	}
 
 	@Override
 	public void onShowScene() {
 		setOnSceneTouchListener(this);
 		
 		mSquirrel.start();
 		
 		mResourceManager.fireplaceMusic.play();
 		mResourceManager.tickTookMusic.play();
 		mResourceManager.fireplaceMusic.setVolume(0.0f);
 		mResourceManager.tickTookMusic.setVolume(0.0f);
 		mResourceManager.fireplaceMusic.setLooping(true);
 		mResourceManager.tickTookMusic.setLooping(true);
 	}
 
 	@Override
 	public void onHideScene() {
 	}
 
 	@Override
 	public void onUnloadScene() {
 		ResourceManager.getInstance().engine.runOnUpdateThread(new Runnable() {
 			@Override
 			public void run() {
 				thisManagedGameScene.detachChildren();
 				thisManagedGameScene.clearEntityModifiers();
 				thisManagedGameScene.clearTouchAreas();
 				thisManagedGameScene.clearUpdateHandlers();
 			}});
 	}
 	
 	@Override
 	protected void onManagedUpdate(float pSecondsElapsed) {
 		manageMusicValue(mBackground.getFirePlaceSprite(), mResourceManager.fireplaceMusic, 1000.0f);
 		manageMusicValue(mBackground.getClockSprite(), mResourceManager.tickTookMusic, 600.0f);
 		super.onManagedUpdate(pSecondsElapsed);
 	}
 	
 	private void manageMusicValue( final Entity object, Music music, final float distance )
 	{
 		final float val = Math.abs(object.getSceneCenterCoordinates()[0]-mPlayer.getX());
 		final float threshold = distance*mResourceManager.WORLD_SCALE_CONSTANT;
 		if(val > threshold) {
 			music.setVolume(0.0f);
 		}
 		else {
 			music.setVolume(1-(val/threshold));
 		}
 	}
 }
