package org.anddev.andengine.ui.activity;
 
 using andengine.engine.Engine;
 using andengine.engine.camera.Camera;
 using andengine.engine.handler.timer.ITimerCallback;
 using andengine.engine.handler.timer.TimerHandler;
 using andengine.engine.options.EngineOptions;
 using andengine.engine.options.EngineOptions.ScreenOrientation;
 using andengine.engine.options.resolutionpolicy.IResolutionPolicy;
 using andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 using andengine.entity.scene.Scene;
 using andengine.entity.scene.SplashScene;
 using andengine.opengl.texture.Texture;
 using andengine.opengl.texture.TextureFactory;
 using andengine.opengl.texture.TextureOptions;
 using andengine.opengl.texture.region.TextureRegion;
 using andengine.opengl.texture.region.TextureRegionFactory;
 using andengine.opengl.texture.source.ITextureSource;
 
 import android.app.Activity;
 import android.content.Intent;
 
 /**
  * @author Nicolas Gramlich
  * @since 08:25:31 - 03.05.2010
  */
 public abstract class BaseSplashActivity extends BaseGameActivity {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private Camera mCamera;
 	private ITextureSource mSplashTextureSource;
 	private TextureRegion mLoadingScreenTextureRegion;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	protected abstract ScreenOrientation getScreenOrientation();
 
 	protected abstract ITextureSource onGetSplashTextureSource();
 
 	protected abstract float getSplashDuration();
 
 	protected abstract Class<? extends Activity> getFollowUpActivity();
 
 	protected float getSplashScaleFrom() {
 		return 1f;
 	}
 
 	protected float getSplashScaleTo() {
 		return 1f;
 	}
 
 	@Override
 	public void onLoadComplete() {
 	}
 
 	@Override
 	public Engine onLoadEngine() {
 		this.mSplashTextureSource = this.onGetSplashTextureSource();
 
 		final int width = this.mSplashTextureSource.getWidth();
 		final int height = this.mSplashTextureSource.getHeight();
 
 		this.mCamera = getSplashCamera(width, height);
 		return new Engine(new EngineOptions(true, this.getScreenOrientation(), getSplashResolutionPolicy(width, height), this.mCamera));
 	}
 
 	@Override
 	public void onLoadResources() {
 		final Texture loadingScreenTexture = TextureFactory.createForTextureSourceSize(this.mSplashTextureSource, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.mLoadingScreenTextureRegion = TextureRegionFactory.createFromSource(loadingScreenTexture, this.mSplashTextureSource, 0, 0);
 
 		this.getEngine().getTextureManager().loadTexture(loadingScreenTexture);
 	}
 
 	@Override
 	public Scene onLoadScene() {
 		final float splashDuration = this.getSplashDuration();
 
 		final SplashScene splashScene = new SplashScene(this.mCamera, this.mLoadingScreenTextureRegion, splashDuration, this.getSplashScaleFrom(), this.getSplashScaleTo());
 
 		splashScene.registerUpdateHandler(new TimerHandler(splashDuration, new ITimerCallback() {
 			@Override
 			public void onTimePassed(final TimerHandler pTimerHandler) {
 				BaseSplashActivity.this.startActivity(new Intent(BaseSplashActivity.this, BaseSplashActivity.this.getFollowUpActivity()));
 				BaseSplashActivity.this.finish();
 			}
 		}));
 
 		return splashScene;
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	protected Camera getSplashCamera(final int pSplashwidth, final int pSplashHeight) {
 		return new Camera(0, 0, pSplashwidth, pSplashHeight);
 	}
 
 	protected IResolutionPolicy getSplashResolutionPolicy(final int pSplashwidth, final int pSplashHeight) {
 		return new RatioResolutionPolicy(pSplashwidth, pSplashHeight);
 	}
 
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 }
