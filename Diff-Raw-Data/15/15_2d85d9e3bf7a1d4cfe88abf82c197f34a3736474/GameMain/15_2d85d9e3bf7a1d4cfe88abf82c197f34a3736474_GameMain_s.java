 package tjuhot.solapop;
 
 import java.io.IOException;
 import java.io.InvalidObjectException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.anddev.andengine.audio.music.Music;
 import org.anddev.andengine.audio.music.MusicFactory;
 import org.anddev.andengine.audio.sound.Sound;
 import org.anddev.andengine.audio.sound.SoundFactory;
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureFactory;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 
 import tjuhot.solapop.Scenario.OnGameOverListener;
 import tjuhot.solapop.music.Level;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 
 public class GameMain extends BaseGameActivity implements OnGameOverListener {
 	private static final int CAMERA_WIDTH = 1024;
 	private static final int CAMERA_HEIGHT = 600;
 	private static final String DB_FILTER = "GameMain";
 	private String filepath;
	private boolean hasCard = false;
 	private Sound mHitSound;
 	private static final String STR_RAT_PATH[] = { "keng", "rat1", "rat2",
 			"rat3", "rat4", "rat5", "rat6" };
 	public static final String STR_HITRAT = "hitrat";
 
 	List<TextureRegion> mRatTextureRegions;
 	List<Texture> mRatTextures;
 
 	List<TextureRegion> mHitRatTextureRegions;
 	List<Texture> mHitRatTextures;
 	Camera mCamera;
 
 	void debug(String msg) {
 		Log.d(DB_FILTER, msg);
 	}
 
 	public void onLoadComplete() {
 
 	}
 
 	public Engine onLoadEngine() {
 		Intent intent = getIntent();
 		// IntentлBundle
 		if(intent != null)
 		{
 			Bundle b = intent.getExtras();
 			if(b != null)
 			{
 				filepath = b.getString("filepath");
 				hasCard = b.getBoolean("isSDCard");
 			}
 		}
		// Bundleлname
 		if (filepath == null || filepath.equals("")) {
 			filepath = "mfx/summer.slp";
 		}
 		filepath = "mfx/summer.slp";
 		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 		return new Engine(
 				new EngineOptions(true, ScreenOrientation.LANDSCAPE,
 						new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
 						mCamera).setNeedsSound(true).setNeedsMusic(true));
 	}
 
 	public void onLoadResources() {
 		loadRatTexture();
 		loadHitRatTexture();
 		loadHitSound();
 	}
 
 	void loadHitRatTexture() {
 		mHitRatTextureRegions = new ArrayList<TextureRegion>();
 		mHitRatTextures = new ArrayList<Texture>();
 		for (int i = 1; i < STR_RAT_PATH.length; ++i) {
 			Texture t = new Texture(128, 128);
 			TextureRegion temp = TextureRegionFactory.createFromAsset(t,
 					getApplicationContext(),
 					String.format("gfx/%s%d.png", STR_HITRAT, i), 0, 0);
 			mHitRatTextureRegions.add(temp);
 			mHitRatTextures.add(t);
 			this.mEngine.getTextureManager().loadTexture(t);
 		}
 	}
 
 	void loadRatTexture() {
 		mRatTextureRegions = new ArrayList<TextureRegion>();
 		mRatTextures = new ArrayList<Texture>();
 		for (int i = 0; i < STR_RAT_PATH.length; ++i) {
 			Texture tempt = new Texture(128, 128);
 			TextureRegion temp = TextureRegionFactory.createFromAsset(tempt,
 					getApplicationContext(),
 					String.format("gfx/%s.png", STR_RAT_PATH[i]), 0, 0);
 			mRatTextureRegions.add(temp);
 			mRatTextures.add(tempt);
 			this.mEngine.getTextureManager().loadTexture(tempt);
 			debug(String.format("gfx/%s.png", STR_RAT_PATH[i]));
 		}
 		debug(String.format("RatTexture size = %d", mRatTextureRegions.size()));
 	}
 
 	void loadHitSound() {
 		try {
 			this.mHitSound = SoundFactory.createSoundFromAsset(
 					this.mEngine.getSoundManager(), this, "mfx/kick_sound.mp3");
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			debug("Hit Sound Load Error");
 		}
 	}
 
 	public Scene onLoadScene() {
 		debug("OnLoadScense");
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 		final Scene scene = new Scene(1);
 		final Scenario sc = new Scenario(mRatTextureRegions,
 				mHitRatTextureRegions, mHitSound, mEngine,
 				getApplicationContext());
 		scene.getLastChild().attachChild(sc);
 		sc.loadLevel(new Level(getApplicationContext(), mEngine, filepath,hasCard));
 		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
 			public boolean onSceneTouchEvent(Scene pScene,
 					TouchEvent pSceneTouchEvent) {
 				float x = pSceneTouchEvent.getX();
 				float y = pSceneTouchEvent.getY();
 				sc.onClick((int) x, (int) y);
 				return false;
 			}
 		});
 		sc.addGameoverListener(this);
 		scene.registerUpdateHandler(new TimerHandler(0.02f, true,
 				new ITimerCallback() {
 					public void onTimePassed(TimerHandler pTimerHandler) {
 						sc.step();
 					}
 				}));
 		return scene;
 	}
 
 	public void onGameOver() {
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		this.finish();
 	}
 
 }
