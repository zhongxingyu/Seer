 package br.eng.mosaic.pigeon.client.gameplay;
 
 import java.util.Vector;
 
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
 import br.eng.mosaic.pigeon.client.gameplay.cast.Ave;
 import br.eng.mosaic.pigeon.client.gameplay.cast.BadPigeon;
 import br.eng.mosaic.pigeon.client.gameplay.cast.Pigeon;
 import br.eng.mosaic.pigeon.client.gameplay.cast.anim.BirdExplosion;
 import br.eng.mosaic.pigeon.client.infra.facebook.LoginFacebook;
 import br.eng.mosaic.pigeon.client.R;
 
 public abstract class Stage extends BaseGameActivity {
 
 	public static final int CAMERA_WIDTH = 720;
 	public static final int CAMERA_HEIGHT = 480;
 
 	private Camera mCamera;
 
 	private boolean nextStage = false;
 
 	private Texture mTexture;
 	public static TiledTextureRegion mPlayerTextureRegion;
 	public static TiledTextureRegion mEnemyTextureRegion1;
 	public static TiledTextureRegion mExplosionPlayerTexture;
 	public static TiledTextureRegion mInvertedEnemyTextureRegion;
 
 	private Texture mAutoParallaxBackgroundTexture;
 
 	private TextureRegion mParallaxLayerBack;
 	private TextureRegion mParallaxLayerMid;
 	private TextureRegion mParallaxLayerFront;
 
 	private Texture mFontTexture;
 	private Font mFont;
 
 	public static Sound mExplosionSound;
 
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
 				this.mCamera).setNeedsSound(true));
 	}
 
 	@Override
 	public void onLoadResources() {
 		this.scene = new Scene(1);
 		this.mTexture = new Texture(256, 128,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
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
 
 		// ----- Background ------
 		this.mAutoParallaxBackgroundTexture = new Texture(1024, 1024,
 				TextureOptions.DEFAULT);
 		this.mParallaxLayerFront = TextureRegionFactory.createFromAsset(
 				this.mAutoParallaxBackgroundTexture, this,
 				"gfx/parallax_background_layer_front.png", 0, 0);
 		this.mParallaxLayerBack = TextureRegionFactory.createFromAsset(
 				this.mAutoParallaxBackgroundTexture, this,
 				"gfx/parallax_background_layer_back.png", 0, 188);
 		this.mParallaxLayerMid = TextureRegionFactory.createFromAsset(
 				this.mAutoParallaxBackgroundTexture, this,
 				"gfx/parallax_background_layer_mid.png", 0, 669);
 
 		this.mEngine.getTextureManager().loadTextures(this.mTexture,
 				this.mAutoParallaxBackgroundTexture);
 		// -----------------------
 
 		createCharacters();
 
 		try {
 			Stage.mExplosionSound = SoundFactory.createSoundFromAsset(
 					this.mEngine.getSoundManager(), this, "mfx/explosion.ogg");
 		} catch (final Exception e) {
 			Log.d("Erro: ", e.toString());
 		}
 	}
 
 	@Override
 	public Scene onLoadScene() {
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 
 		// --------------- Criando a Cena e inserindo o background
 		// ---------------
 		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(
 				0, 0, 0, 5);
 		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f,
 				new Sprite(0, CAMERA_HEIGHT
 						- this.mParallaxLayerBack.getHeight(),
 						this.mParallaxLayerBack)));
 		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f,
 				new Sprite(0, 80, this.mParallaxLayerMid)));
 		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f,
 				new Sprite(0, CAMERA_HEIGHT
 						- this.mParallaxLayerFront.getHeight(),
 						this.mParallaxLayerFront)));
 		scene.setBackground(autoParallaxBackground);
 		// ---------------------------------------------------------------------
 
 		message = "";
 
 		// ----------------------------------------------------------------------
 
 		// --------------- Criando texto exibido ---------------
		final ChangeableText lifeText = new ChangeableText(10, 10, this.mFont,
				"Life: " + pigeon.getLife(), "S2: X".length());
 		scene.getLastChild().attachChild(lifeText);
 
 		// -----------------------------------------------------
 
		// -------------- Criando Retangulo para colisão --------------------
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
 							scene.unregisterTouchArea((ITouchArea) pTouchArea);
 							scene.getLastChild().detachChild((Ave) pTouchArea);
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
 						Intent i = new Intent(getBaseContext(), LoginFacebook.class);
 						startActivity(i);
 						
 						nextStage();
						nextStage = true; // Feito para não criar mais de uma
											// instância de Stage já que
											// onUpdate é chaamdo várias vezes
 					}
 				}
 
 				if (colissionWithPigeon()) {
 					if (pigeon.isAlive()) {
 						if (pigeon.sufferDamage()) {
 							// the bird died
 							scene.getLastChild().detachChild(pigeon);
 							final BirdExplosion explosion1 = new BirdExplosion(
 									Pigeon.posX, Pigeon.posY,
 									Stage.mExplosionPlayerTexture);
 							scene.getLastChild().attachChild(explosion1);
 							pigeon.setPosition(1000, -1000);
 							Pigeon.posX = 1000;
 							pigeon.setAlive(false);
 							Stage.mExplosionSound.play();
 						}
 						lifeText.setText("Life: " + pigeon.getLife());
 					}
 				}
 			}
 		});
 
 		return scene;
 	}
 
 	protected boolean colissionWithPigeon() {
 		for (BadPigeon bp : badPigeons) {
 			if ((bp.isAlive()) && (bp.collidesWith(this.pigeon))) {
 				if (bp.sufferDamage()) {
 					// the bird died
 					bp.setAlive(false);
 					Stage.mExplosionSound.play();
 					scene.getLastChild().detachChild(bp);
 					final BirdExplosion explosion1 = new BirdExplosion(
 							bp.getX(), bp.getY(), Stage.mExplosionPlayerTexture);
 					scene.getLastChild().attachChild(explosion1);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if (pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			switch (pKeyCode) {
 			case KeyEvent.KEYCODE_MENU: {
 				if (this.mEngine.isRunning()) {
 					this.mEngine.stop();
 					this.showDialog(DIALOG_CHOOSE_MESSAGE);
 				} else {
 					this.scene.clearChildScene();
 					this.mEngine.start();
 				}
 				return true;
 			}
 			case KeyEvent.KEYCODE_BACK: {
 				System.exit(0);
 				return true;
 			}
 			case KeyEvent.KEYCODE_DPAD_LEFT: {
 				// react on left key press
 				return true;
 			}
 			case KeyEvent.KEYCODE_DPAD_RIGHT: {
 				// react on right key press
 				return true;
 			}
 			case KeyEvent.KEYCODE_DPAD_UP: {
 				// react on up key press
 				return true;
 			}
 			case KeyEvent.KEYCODE_DPAD_DOWN: {
 				// react on down key press
 				return true;
 			}
 			default:
 				return super.onKeyDown(pKeyCode, pEvent); // this will allow
 															// keypesses other
 															// than that to be
 															// processed in
 															// other places, for
 															// example by
 															// android OS
 			}
 		} else
 			return super.onKeyDown(pKeyCode, pEvent); // similarily, this will
 														// allow actions other
 														// than key press to be
 														// processed elsewhere.
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
 
 	@Override
 	public void onLoadComplete() {
 	}
 
 	protected abstract void createCharacters();
 
 	protected abstract void nextStage();
 }
