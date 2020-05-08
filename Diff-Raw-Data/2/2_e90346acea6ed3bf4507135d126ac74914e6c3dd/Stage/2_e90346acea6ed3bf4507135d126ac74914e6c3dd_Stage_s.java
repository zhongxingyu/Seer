 package br.eng.mosaic.pigeon.client.gameplay;
 
 import java.util.Vector;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.anddev.andengine.audio.music.Music;
 import org.anddev.andengine.audio.sound.Sound;
 import org.anddev.andengine.audio.sound.SoundFactory;
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.handler.IUpdateHandler;
 import org.anddev.andengine.engine.handler.runnable.RunnableHandler;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.CameraScene;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
 import org.anddev.andengine.entity.scene.Scene.ITouchArea;
 import org.anddev.andengine.entity.scene.background.AutoParallaxBackground;
 import org.anddev.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
 import org.anddev.andengine.entity.scene.menu.MenuScene;
 import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
 import org.anddev.andengine.entity.scene.menu.item.TextMenuItem;
 import org.anddev.andengine.entity.scene.menu.item.decorator.ColorMenuItemDecorator;
 import org.anddev.andengine.entity.sprite.AnimatedSprite;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.ChangeableText;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 
 import sun.font.CreatedFontTracker;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.EditText;
 import br.eng.mosaic.pigeon.client.R;
 import br.eng.mosaic.pigeon.client.gameplay.cast.Ave;
 import br.eng.mosaic.pigeon.client.gameplay.cast.BadPigeon;
 import br.eng.mosaic.pigeon.client.gameplay.cast.Pigeon;
 import br.eng.mosaic.pigeon.client.gameplay.cast.anim.BirdExplosion;
 import br.eng.mosaic.pigeon.client.gameplay.cast.anim.FeatherEvent;
 import br.eng.mosaic.pigeon.client.gameplay.util.AudioFactory;
 import br.eng.mosaic.pigeon.client.gameplay.util.GameUtil;
 import br.eng.mosaic.pigeon.client.infra.Config;
 import br.eng.mosaic.pigeon.client.infra.ConfigIF;
 import br.eng.mosaic.pigeon.client.infra.facebook.LoginFacebook;
 
 public abstract class Stage extends BaseGameActivity implements IOnMenuItemClickListener {
 
 	public ConfigIF profile = Config.getInstance();
 	
 	private ChangeableText scoreText;
 	
 	public static final int CAMERA_WIDTH = 720;
 	public static final int CAMERA_HEIGHT = 480;
 	
 	protected static final int MENU_RESET = 0;
 	protected static final int MENU_QUIT = MENU_RESET + 1;
 	
 	protected MenuScene mMenuScene;
 
 	public String backgroundBack;
 	public String backgroundFront;
 	public String backgroundFront2;
 	public String backgroundFront3;
 	public String backgroundMid;
 	
 	private Camera mCamera;
 
 	private boolean nextStage = false;
 
 	private Texture mTexture;
 	public static TiledTextureRegion mPlayerTextureRegion;
 	public static TiledTextureRegion mEnemyTextureRegion1;
 	public static TiledTextureRegion mExplosionPlayerTexture;
 	public static TiledTextureRegion mInvertedEnemyTextureRegion;
 	public static TiledTextureRegion mFetherTexture;
 
 	private Texture mAutoParallaxBackgroundTexture;
 
 	private TextureRegion mParallaxLayerBack;
 	//private TextureRegion mParallaxLayerMid;
 	private TextureRegion mParallaxLayerFront;
 	private TextureRegion mParallaxLayerFront2;
 	private TextureRegion mParallaxLayerFront3;
 
 	private Texture mFontTexture;
 	private Font mFont;
 
 	public static Sound mExplosionSound;
 	public static Music mMainMusic;
 
 	protected Vector<BadPigeon> badPigeons = new Vector();
 	protected Scene scene;
 	protected Pigeon pigeon;
 
 	private CameraScene mPauseScene;
 
 	public static final int DIALOG_CHOOSE_MESSAGE = 0;
 
 	public static String message;
 
 	@Override
 	public Engine onLoadEngine() {
 		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,
 				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
 				this.mCamera).setNeedsSound(true).setNeedsMusic(true));
 	}
 
 	@Override
 	public void onLoadResources() {
 		this.scene = new Scene(1);
 		
 	
 		this.mTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		Stage.mPlayerTextureRegion = TextureRegionFactory.createTiledFromAsset(
 				this.mTexture, this, "gfx/bird.png", 0, 0, 3, 4);
 		Stage.mEnemyTextureRegion1 = TextureRegionFactory.createTiledFromAsset(
 				this.mTexture, this, "gfx/bird.png", 0, 0, 3, 4);
 		Stage.mInvertedEnemyTextureRegion = TextureRegionFactory
 		.createTiledFromAsset(this.mTexture, this, "gfx/bird.png", 0,
 				0, 3, 4);
 		Stage.mExplosionPlayerTexture = TextureRegionFactory
 		.createTiledFromAsset(this.mTexture, this, "gfx/bird.png", 0,
 				0, 3, 4);
 		Stage.mFetherTexture = TextureRegionFactory.createTiledFromAsset(mTexture, this, 
 				"gfx/bird_feather.png", 0, 0, 3, 5);
 
 		// --pause scene
 		// this.mPausedTextureRegion =
 		// TextureRegionFactory.createFromAsset(this.mTexture, this,
 		// "gfx/paused.png", 0, 0);
 
 		// -------- Texto -------
 		this.mFontTexture = new Texture(256, 256,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.mFont = new Font(this.mFontTexture, Typeface.create(
 				Typeface.DEFAULT, Typeface.BOLD), 36, true, Color.WHITE);
 		this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
 		this.mEngine.getFontManager().loadFont(this.mFont);
 		// ---------------------
 
 		setBackgroundParameter();
 		
 		createBackground(backgroundBack, backgroundMid, backgroundFront,backgroundFront2, backgroundFront3);
 		
 		createCharacters();
 
		mExplosionSound = AudioFactory.createSound(mEngine, this, "mfx/explosion.ogg");
 		mMainMusic = AudioFactory.createMusic(mEngine, this, "mfx/sound_execution.ogg");
 		mMainMusic.play();
 	
 	}
 
 	@Override
 	public Scene onLoadScene() {
 		
 		this.mMenuScene = this.createMenuScene();
 		
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 
 		// --------------- Criando a Cena e inserindo o background
 		// ---------------
 		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(
 				0, 0, 0, 5);
 		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f,
 				new Sprite(0, CAMERA_HEIGHT
 						- this.mParallaxLayerBack.getHeight(),
 						this.mParallaxLayerBack)));
 		
 		//autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f,
 			//	new Sprite(0, 80, this.mParallaxLayerMid)));
 		
 		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-15.0f,
 				new Sprite(0, CAMERA_HEIGHT
 						- this.mParallaxLayerFront.getHeight(),
 						this.mParallaxLayerFront)));
 		
 		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-20.0f,
 				new Sprite(0, CAMERA_HEIGHT 
 						- this.mParallaxLayerFront2.getHeight(),
 						this.mParallaxLayerFront2)));
 		
 		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-25.0f,
 				new Sprite(0, CAMERA_HEIGHT 
 						- this.mParallaxLayerFront3.getHeight(),
 						this.mParallaxLayerFront3)));
 		scene.setBackground(autoParallaxBackground);
 		// ---------------------------------------------------------------------
 
 		message = "";
 
 		// ----------------------------------------------------------------------
 
 		// --------------- Criando texto de vida ---------------
 		final ChangeableText lifeText = new ChangeableText(10, 10, this.mFont, "♥: " + pigeon.getLife(), "S2: X".length());
 		scene.getLastChild().attachChild(lifeText);
 
 		// --------------- Criando texto de score ---------------
 		this.scoreText = new ChangeableText(470, 10, this.mFont, "Highscore: " + profile.getScore(), "Highcore: XXXXX".length());
 		scene.getLastChild().attachChild(scoreText);
 
 
 		// -------------- Criando Retangulo para colis√£o --------------------
 		final int rectangleX = (CAMERA_WIDTH) + 1;
 		final int rectangleY = (CAMERA_HEIGHT);
 		final Rectangle colisionLine = new Rectangle(rectangleX, 0,
 				rectangleX + 1, rectangleY);
 		// colisionRectangle.registerEntityModifier(new LoopEntityModifier(new
 		// ParallelEntityModifier(new RotationModifier(6, 0, 360), new
 		// SequenceEntityModifier(new ScaleModifier(3, 1, 1.5f), new
 		// ScaleModifier(3, 1.5f, 1)))));
 		scene.getLastChild().attachChild(colisionLine);
 		// -------------------------------------------------------------------
 
 		scene.setOnAreaTouchListener(new IOnAreaTouchListener() {
 			@Override
 			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 					final ITouchArea pTouchArea, final float pTouchAreaLocalX,
 					final float pTouchAreaLocalY) {
 				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
 					final RunnableHandler runnableHandler = new RunnableHandler();
 					Stage.this.mEngine.getScene().registerUpdateHandler(
 							runnableHandler);
 					runnableHandler.postRunnable(new Runnable() {
 						@Override
 						public void run() {
 							Ave face = (Ave) pTouchArea;
 							birdDied(face);							
 						}
 					});
 					return true;
 				}
 
 				return false;
 
 			}
 		});
 		scene.setTouchAreaBindingEnabled(true);
 
 		/* The actual collision-checking. */
 		scene.registerUpdateHandler(new IUpdateHandler() {
 			@Override
 			public void reset() {
 			}
 
 			@Override
 			public void onUpdate(final float pSecondsElapsed) {
 				if (colisionLine.collidesWith(pigeon)) {
 
 					if (!nextStage) {
 
 						/*
 						 * Chama a tela de login do facebook quando o pombo alcanca
 						 * o final da tela
 						 */
 						/*
 						 * trecho comentado por causar problemas
 						 * quando executa.
 						Intent i = new Intent(getBaseContext(), LoginFacebook.class);
 						startActivity(i);
 						*/
 						nextStage = true;
 						nextStage();
 						 // Feito para não criar mais de uma
 						// instância de Stage já que
 						// onUpdate é chamado várias vezes
 					}
 				}
 
 				if (colissionWithPigeon()) {
 					if (pigeon.isAlive()) {
 						if (pigeon.sufferDamage()) {
 							// the bird died
 							pigeon.setPosition(1000, -1000);
 							Pigeon.posX = 1000;
 							birdDied(pigeon);
 						}
 						FeatherEvent feather = new FeatherEvent(pigeon.getX(), pigeon.getY(), mFetherTexture, scene);
 						scene.getLastChild().attachChild(feather);
 						lifeText.setText("♥: " + pigeon.getLife());
 					}
 				}
 
 				if(badPigeons.size() == 1){
 					for (BadPigeon bad : GameUtil.genEnemies(3, CAMERA_WIDTH, CAMERA_HEIGHT, Stage1.mEnemyTextureRegion1)) {
 						badPigeons.add(bad);
 						scene.getLastChild().attachChild(bad);
 						scene.registerTouchArea(bad);
 					}
 				}
 			}
 		});
 
 		return scene;
 	}
 	
 	/**
 	 * Called when a bird die
 	 * @param bird Bird that went to hell
 	 */
 	private void birdDied(Ave bird) {	
 		this.profile.setScore(1);
 		scoreText.setText("Highscore: " + profile.getScore());
 		BirdExplosion explosion = new BirdExplosion(bird.getX(), bird.getY(), mExplosionPlayerTexture, scene);
 		scene.getLastChild().attachChild(explosion);
 		scene.unregisterTouchArea(bird);
 		scene.getLastChild().detachChild(bird);
 		badPigeons.remove(bird);
 		bird.setAlive(false);
 		Stage.mExplosionSound.play();
 	}
 
 	/**
 	 * Tests the collision between the badpigeon and the piegon
 	 * @return <code> true </code> if there was collision
 	 */
 	protected boolean colissionWithPigeon() {
 		for (BadPigeon bp : badPigeons) {
 			if ((bp.isAlive()) && (bp.collidesWith(this.pigeon))) {
 				if (bp.sufferDamage()) {
 					// the bird died
 					birdDied(bp);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void onLoadComplete() {		
 	}
 	
 	@Override
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			if(this.scene.hasChildScene()) {
 				/* Remove the menu and reset it. */
 				this.mMenuScene.back();
 			} else {
 				/* Attach the menu. */
 				this.scene.setChildScene(this.mMenuScene, false, true, true);
 			}
 			return true;
 		} else {
 			return super.onKeyDown(pKeyCode, pEvent);
 		}
 	}
 
 	@Override
 	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
 		switch(pMenuItem.getID()) {
 			case MENU_RESET:
 				/* Restart the animation. */
 				this.scene.reset();
 
 				/* Remove the menu and reset it. */
 				this.scene.clearChildScene();
 				this.mMenuScene.reset();
 				return true;
 			case MENU_QUIT:
 				/* End Activity. */
 				this.finish();
 				return true;
 			default:
 				return false;
 		}
 	}
 	
 	
 	public void setBackgroundBack(String backgroundBack) {
 		this.backgroundBack = backgroundBack;
 	}
 
 	public void setBackgroundFront(String backgroundFront) {
 		this.backgroundFront = backgroundFront;
 	}
 	
 	public void setBackgroundFront2(String backgroundFront2)
 	{
 		this.backgroundFront2 = backgroundFront2;
 	}
 	public void setBackgroundFront3(String backgroundFront3)
 	{
 		this.backgroundFront3 = backgroundFront3;
 	}
 	//public void setBackgroundMid(String backgroundMid) {
 		//this.backgroundMid = backgroundMid;
 	//}
 	
 	public void createBackground(String back, String mid, String front, String front2, String front3){
 		this.mAutoParallaxBackgroundTexture = new Texture(1024, 1024,
 				TextureOptions.DEFAULT);			
 		
 		this.mParallaxLayerFront = TextureRegionFactory.createFromAsset(
 				this.mAutoParallaxBackgroundTexture, this,front, 0, 0);
 		
 		this.mParallaxLayerBack = TextureRegionFactory.createFromAsset(
 				this.mAutoParallaxBackgroundTexture, this,back, 0, 188);
 		
 		this.mParallaxLayerFront2 = TextureRegionFactory.createFromAsset(
 				this.mAutoParallaxBackgroundTexture, this,front2, 0, 690);
 		
 		this.mParallaxLayerFront3 = TextureRegionFactory.createFromAsset(
 				this.mAutoParallaxBackgroundTexture, this,front3, 0, 750);
 	//	this.mParallaxLayerMid = TextureRegionFactory.createFromAsset(
 		//		this.mAutoParallaxBackgroundTexture, this,mid, 0, 669);
 
 		this.mEngine.getTextureManager().loadTextures(this.mTexture,
 				this.mAutoParallaxBackgroundTexture);
 	}
 	
 	protected Dialog onCreateDialog(final int pID) {
 		switch (pID) {
 		case DIALOG_CHOOSE_MESSAGE:
 			final EditText ipEditText = new EditText(this);
 			ipEditText.setText(message);
 			return new AlertDialog.Builder(this)
 			.setIcon(R.drawable.facebook_icon)
 			.setTitle("Your Message").setCancelable(false)
 			.setView(ipEditText)
 			.setPositiveButton("Send", new OnClickListener() {
 				@Override
 				public void onClick(final DialogInterface pDialog,
 						final int pWhich) {
 					message = ipEditText.getText().toString();
 				}
 			}).setNegativeButton("Cancel", new OnClickListener() {
 				@Override
 				public void onClick(final DialogInterface pDialog, final int pWhich) {
 					Stage.this.onResume();
 				}
 			}).create();
 		default:
 			return super.onCreateDialog(pID);
 		}
 	}
 	
 	protected MenuScene createMenuScene() {
 		final MenuScene menuScene = new MenuScene(this.mCamera);
 
 		final IMenuItem resetMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_RESET, this.mFont, "RESET"), 1.0f,0.0f,0.0f, 0.0f,0.0f,0.0f);
 		resetMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		menuScene.addMenuItem(resetMenuItem);
 
 		final IMenuItem quitMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_QUIT, this.mFont, "QUIT"), 1.0f,0.0f,0.0f, 0.0f,0.0f,0.0f);
 		quitMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		menuScene.addMenuItem(quitMenuItem);
 
 		menuScene.buildAnimations();
 
 		menuScene.setBackgroundEnabled(false);
 
 		menuScene.setOnMenuItemClickListener(this);
 		return menuScene;
 	}
 		
 	protected abstract void setBackgroundParameter();
 	
 	protected abstract void gameOver();
 	
 	protected abstract void createCharacters();
 
 	protected abstract void nextStage();
 
 }
