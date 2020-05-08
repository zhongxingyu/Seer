 package com.gradugation;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Random;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.engine.camera.SmoothCamera;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.MoveModifier;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.CameraScene;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.extension.tmx.TMXLayer;
 import org.andengine.extension.tmx.TMXLoader;
 import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
 import org.andengine.extension.tmx.TMXProperties;
 import org.andengine.extension.tmx.TMXTile;
 import org.andengine.extension.tmx.TMXTileProperty;
 import org.andengine.extension.tmx.TMXTiledMap;
 import org.andengine.extension.tmx.util.exception.TMXLoadException;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.StrokeFont;
 import org.andengine.opengl.texture.EmptyTexture;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.util.Constants;
 import org.andengine.util.debug.Debug;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.coordinates.MapCoordinate;
 import com.coordinates.SpriteCoordinate;
 
 public class MainGameScreen extends SimpleBaseGameActivity implements
 		IOnSceneTouchListener {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	private static final int CAMERA_WIDTH = 480;
 	private static final int CAMERA_HEIGHT = 320;
 	
 	//private static final int MAX_CHARACTER_MOVEMENT = 3;
 	public static final int CHARACTER_WIDTH = 32;
 	private static final String MAP_NAME = "map_text_file.txt";
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	// Need handler for callbacks to the UI thread
     final Handler mHandler = new Handler();
     
     private AlertDialog.Builder alertDialogBuilder;
 	private AlertDialog alertDialog;
 
 	private BoundCamera mCamera;
 
 	private ITexture mPlayerTexture;
 	private TiledTextureRegion mPlayerTextureRegion;
 
 	private TMXTiledMap mTMXTiledMap;
 	protected int mCactusCount;
 	private int CREDITS_NEEDED_GRADUATE = 15;
 
 	private BitmapTextureAtlas[] characterTextureAtlas;
 	private ITextureRegion[] character;
 	private Sprite[] spriteList;
 
 	private BitmapTextureAtlas diceTextureAtlas;
 	private TextureRegion diceTextureRegion;
 	private BitmapTextureAtlas finishTurnTextureAtlas;
 	private TextureRegion finishTurnTextureRegion;
 	private BitmapTextureAtlas musicTextureAtlas;
 	private ITextureRegion mMusicTextureRegion;
 	private BitmapTextureAtlas rightArrowTextureAtlas;
 	private ITextureRegion rightArrowRegion;
 	private BitmapTextureAtlas leftArrowTextureAtlas;
 	private ITextureRegion leftArrowRegion;
 	private BitmapTextureAtlas downArrowTextureAtlas;
 	private ITextureRegion downArrowRegion;
 	private BitmapTextureAtlas upArrowTextureAtlas;
 	private ITextureRegion upArrowRegion;
 	
 	private ITexture mFaceTexture;
 	private ITextureRegion mFaceTextureRegion;
 	private ITexture mPausedTexture, mResumeTexture, mMainMenuTexture, mSaveTexture;
 	private ITextureRegion mPausedTextureRegion, mResumeTextureRegion,
 			mMainMenuTextureRegion, mSaveTextureRegion;
 	
 
 	private CameraScene mPauseScene;
 	private Scene scene;
 	private Event mainMapEvent;
 
 	private HUD mHUD;
 
 	private Font mFont;
 	private StrokeFont mStrokeFont, mStrokeFontLarge;
 	
 	private Music mMusic;
 
 	private ArrayList<Character> thePlayers;
 	
 	private Text[] textStrokes;
 	final private SpriteCoordinate[] textStrokeCoordinates = {
 	        new SpriteCoordinate(60,280), new SpriteCoordinate(420,280), 
 	        new SpriteCoordinate(60,40), new SpriteCoordinate(420,40) };
 
 	private boolean turnDone;
 	private boolean eventCompleted;
 	private boolean moving;
 	public int turnNum;
 	public int currentCharacter;
 	public int currentCharacterYear;
 	public int movesLeft;
 	private int numCharacters;
 	private int movementCount;
 	private Text textStroke5;
 
 	private Random random;
     private int diceRoll = 0;
     private int maxRoll;
     
 	private boolean gameDone = false;
 	private boolean hasGraduated = false;
 
 	float initX;
 	float initY;
 	float finalX;
 	float finalY;
 	
 	float variable;
 	
 	private boolean finishTurn = false; 
 	private boolean diceDone = false;
 	private boolean music;
 	boolean swipeDone = false;
 	private DbHelper dbhelper;
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		final float maxVelocityX = 150;
 		final float maxVelocityY = 150;
 		final float maxZoomFactorChange = 5;
 		this.mCamera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT,
 				maxVelocityX, maxVelocityY, maxZoomFactorChange);
 		this.mCamera.setBoundsEnabled(false);
 		
         final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
         engineOptions.getAudioOptions().setNeedsMusic(true);
 
 		return engineOptions;
 	}
 
 	@Override
 	public void onCreateResources() throws IOException {
 		mainMapEvent = new Event(this, R.raw.map_text_file);
 
         Intent intent = getIntent();
         Bundle bundle = intent.getExtras();
 		thePlayers = (ArrayList<Character>) bundle.getSerializable(ChooseCharacterActivity.THE_PLAYERS);
 		numCharacters = thePlayers.size();
 		
 		//Create all four character sprites
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 		
 		characterTextureAtlas = new BitmapTextureAtlas[numCharacters];
 		character = new ITextureRegion[numCharacters];
 		
 		for (int i = 0; i < numCharacters; i++) {
 			characterTextureAtlas[i] = new BitmapTextureAtlas(
 					this.getTextureManager(), 1000, 1000, TextureOptions.BILINEAR);
 			character[i] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(characterTextureAtlas[i],
 					this, thePlayers.get(i).getCharacterImage(), 0 , 0);   //NameToImageName(characterTypes[i]), 0, 0);
 			characterTextureAtlas[i].load();
 		}
 
 		this.diceTextureAtlas = new BitmapTextureAtlas(
                 this.getTextureManager(), 170, 90, TextureOptions.BILINEAR);
 		this.diceTextureRegion = BitmapTextureAtlasTextureRegionFactory
                 .createFromAsset(diceTextureAtlas, this, "dice.png",
                                 0, 0);
 		this.diceTextureAtlas.load();
 		
 		this.finishTurnTextureAtlas = new BitmapTextureAtlas(
                 this.getTextureManager(), 170, 90, TextureOptions.BILINEAR);
 		this.finishTurnTextureRegion = BitmapTextureAtlasTextureRegionFactory
                 .createFromAsset(finishTurnTextureAtlas, this, "finish_button.png",
                                 0, 0);
 		this.finishTurnTextureAtlas.load();
 		
 		// Pause Assets
 		this.mFaceTexture = new AssetBitmapTexture(this.getTextureManager(),
 				this.getAssets(), "gfx/menu.png", TextureOptions.BILINEAR);
 		this.mFaceTextureRegion = TextureRegionFactory
 				.extractFromTexture(this.mFaceTexture);
 		this.mFaceTexture.load();
 
 		this.mPausedTexture = new AssetBitmapTexture(this.getTextureManager(),
 				this.getAssets(), "gfx/paused.png", TextureOptions.BILINEAR);
 		this.mPausedTextureRegion = TextureRegionFactory
 				.extractFromTexture(this.mPausedTexture);
 		this.mPausedTexture.load();
 
 		this.mResumeTexture = new AssetBitmapTexture(this.getTextureManager(),
 				this.getAssets(), "gfx/resume.png", TextureOptions.BILINEAR);
 		this.mResumeTextureRegion = TextureRegionFactory
 				.extractFromTexture(this.mResumeTexture);
 		this.mResumeTexture.load();
 
 		this.mMainMenuTexture = new AssetBitmapTexture(
 				this.getTextureManager(), this.getAssets(), "gfx/mainmenu.png",
 				TextureOptions.BILINEAR);
 		this.mMainMenuTextureRegion = TextureRegionFactory
 				.extractFromTexture(this.mMainMenuTexture);
 		this.mMainMenuTexture.load();
 
 		this.mSaveTexture = new AssetBitmapTexture(this.getTextureManager(),
 				this.getAssets(), "gfx/savegame.png", TextureOptions.BILINEAR);
 		this.mSaveTextureRegion = TextureRegionFactory
 				.extractFromTexture(this.mSaveTexture);
 		this.mSaveTexture.load();
 
 		// UI Fonts
 		final ITexture fontTexture = new EmptyTexture(this.getTextureManager(),
 				256, 256, TextureOptions.BILINEAR);
 		final ITexture strokeFontTexture = new EmptyTexture(
 				this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
 
 		this.mFont = new Font(this.getFontManager(), fontTexture,
 				Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 18, true,
 				Color.BLACK);
 		this.mFont.load();
 
 		this.mStrokeFont = new StrokeFont(this.getFontManager(),
 				strokeFontTexture, Typeface.create(Typeface.DEFAULT,
 						Typeface.BOLD), 18, true, Color.WHITE, 1, Color.BLACK);
 		this.mStrokeFont.load();
 
 		this.mStrokeFontLarge = new StrokeFont(this.getFontManager(),
 				strokeFontTexture, Typeface.create(Typeface.DEFAULT,
 						Typeface.BOLD), 32, true, Color.WHITE, 1, Color.BLACK);
 		this.mStrokeFontLarge.load();
 		
 		this.musicTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 128, 128, TextureOptions.BILINEAR);
         this.mMusicTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.musicTextureAtlas, this, "notes.png", 0, 0);
         this.musicTextureAtlas.load();
         
 		MusicFactory.setAssetBasePath("mfx/");
         try {
                 this.mMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "DST-TowerDefenseTheme.mp3");
                 this.mMusic.setLooping(true);
         } catch (final IOException e) {
                 Debug.e(e);
         }
 
 	}
 
 	@Override
 	public Scene onCreateScene() {
 		
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 
 		final Scene scene = new Scene();
 
 		/*
 		 * User Interface HUD At each corner of the screen, will display
 		 * player's information Include number of credits There will be a button
 		 * to roll dice and turn number will be displayed.
 		 */
 		VertexBufferObjectManager vertexBufferObjectManager = this
 				.getVertexBufferObjectManager();
 
 		textStrokes = new Text[numCharacters];
 
 		/*
 		 * To update text, use [text].setText("blah blah"); In which "blah blah"
 		 * is whatever you want to change the text to. You can use variables.
 		 */
 		for (int i = 0; i < numCharacters; i++) {
 			SpriteCoordinate coord = textStrokeCoordinates[i];
 			textStrokes[i] = new Text(coord.getX(), coord.getY(), this.mStrokeFont,
 					thePlayers.get(i).getName()   +"\nCredits: " + thePlayers.get(i).getCredits()
 				    + "\nCoins: " + thePlayers.get(i).getCoins(), vertexBufferObjectManager); 
 		}
 		textStroke5 = new Text(180, 20, this.mStrokeFont,
                 " " + diceRoll, vertexBufferObjectManager);
 
 		textStroke5.setScale((float) .7 ); 
 		mHUD = new HUD();
 		mHUD.attachChild(scene);
 
 
 		final Sprite diceButton = new Sprite(180, CAMERA_HEIGHT/10, this.diceTextureRegion,
                 this.getVertexBufferObjectManager()) {
 
 	        public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 	                //generate random number [1,3]
 	        	
 	        	currentCharacterYear = (thePlayers.get(currentCharacter).getCredits()/3) + 1;
 	        	switch(currentCharacterYear) {
 	        	case 0: maxRoll = 3;
 	        			break;
 	        	case 1: maxRoll = 3;
 	        			break;
 	        	case 2: maxRoll = 4;
 	        			break;
 	        	case 3: maxRoll = 4;
     					break;
 	        	case 4: maxRoll = 5;
     					break;
 	        	case 5: maxRoll = 6;
     					break;
 	        	default: maxRoll = 3;
 	        			break;
 	        	}
 	                random = new Random();
 	                diceRoll = random.nextInt(maxRoll) + 1;
 	                if (diceDone == false) {
 	                	swipeDone = false;
 	                	if (touchEvent.isActionUp()) {
 	                		this.setColor(Color.GRAY);
 	                		textStroke5.setText(" " + diceRoll);
 	                		movesLeft = diceRoll;
 	                		eventCompleted = false;
 	                		diceDone = true;
 	                		move = false;
 	    					gameDone = false;
 	                		
 	                	}
 	                	if (touchEvent.isActionDown()) {
 	                		this.setColor(Color.WHITE);
 	                	}
 	                }
 	                return true;
 	        };
 		};
 		diceButton.setScale((float) .5);
 		
 		this.rightArrowTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 120, 120, TextureOptions.BILINEAR);
 		this.rightArrowRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(rightArrowTextureAtlas, this, "right_arrow.png",
 						0, 0);
 		this.rightArrowTextureAtlas.load();
 		
 		final Sprite rightArrowButton = new Sprite(CAMERA_WIDTH-CHARACTER_WIDTH, CAMERA_HEIGHT/2, this.rightArrowRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				if (touchEvent.isActionDown()) {
 					return movement(new SpriteCoordinate(CHARACTER_WIDTH,0));
 				} else {
 					return false;
 				}
 			}
 		};
 		rightArrowButton.setScale(.5f);
 		this.mHUD.registerTouchArea(rightArrowButton);
 		this.mHUD.attachChild(rightArrowButton);
 		
 		this.leftArrowTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 120, 120, TextureOptions.BILINEAR);
 		this.leftArrowRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(leftArrowTextureAtlas, this, "left_arrow.png",
 						0, 0);
 		this.leftArrowTextureAtlas.load();
 		
 		final Sprite leftArrowButton = new Sprite(CHARACTER_WIDTH, CAMERA_HEIGHT/2, this.leftArrowRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				if (touchEvent.isActionDown()) {
 					if (touchEvent.isActionDown()) {
 						return movement(new SpriteCoordinate(-CHARACTER_WIDTH,0));
 					} else {
 						return false;
 					}
 				}
 				return true;
 			}
 		};
 		leftArrowButton.setScale(.5f);
 		this.mHUD.registerTouchArea(leftArrowButton);
 		this.mHUD.attachChild(leftArrowButton);
 		
 		this.downArrowTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 120, 120, TextureOptions.BILINEAR);
 		this.downArrowRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(downArrowTextureAtlas, this, "down_arrow.png",
 						0, 0);
 		this.downArrowTextureAtlas.load();
 		
 		final Sprite downArrowButton = new Sprite(CAMERA_WIDTH/2, CHARACTER_WIDTH, this.downArrowRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				if (touchEvent.isActionDown()) {
 					if (touchEvent.isActionDown()) {
 						return movement(new SpriteCoordinate(0, -CHARACTER_WIDTH));
 					} else {
 						return false;
 					}
 				}
 				return true;
 			}
 		};
 		downArrowButton.setScale(.5f);
 		this.mHUD.registerTouchArea(downArrowButton);
 		this.mHUD.attachChild(downArrowButton);
 		
 		this.upArrowTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 120, 120, TextureOptions.BILINEAR);
 		this.upArrowRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(upArrowTextureAtlas, this, "up_arrow.png",
 						0, 0);
 		this.upArrowTextureAtlas.load();
 		
 		final Sprite upArrowButton = new Sprite(CAMERA_WIDTH/2, CAMERA_HEIGHT-CHARACTER_WIDTH, this.upArrowRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				if (touchEvent.isActionDown()) {
 					if (touchEvent.isActionDown()) {
 						return movement(new SpriteCoordinate(0,CHARACTER_WIDTH));
 					} else {
 						return false;
 					}
 				}
 				return true;
 			}
 		};
 		upArrowButton.setScale(.5f);
 		this.mHUD.registerTouchArea(upArrowButton);
 		this.mHUD.attachChild(upArrowButton);
 		
 		final Sprite finishTurnButton = new Sprite(310, CAMERA_HEIGHT/10, this.finishTurnTextureRegion,
                 this.getVertexBufferObjectManager()) {
 
 	        public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 	                if (movesLeft == 0) {
 		                if (touchEvent.isActionUp()) {
 		                        this.setColor(Color.GRAY);
 		                        finishTurn = true;
 		                        }
 		                if (touchEvent.isActionDown()) {
 		                        this.setColor(Color.GRAY);
 		                }
 		                return true;
 	                } else {
 	                	return false;
 	                }
 	        };
 		};
 		finishTurnButton.setScale((float) .5);
 		finishTurnButton.setColor(Color.GRAY);
 
 		// Load the Pause Scene
 		this.mPauseScene = new CameraScene(this.mCamera);
 		final float cX = (CAMERA_WIDTH - this.mPausedTextureRegion.getWidth())
 				/ 2 + (this.mPausedTextureRegion.getWidth() / 3);
 		final float cY = (CAMERA_HEIGHT - this.mPausedTextureRegion.getHeight()) / 5;
 
 		// Music Button
 		// Default - Music on.
 		mMusic.play();
 		
         final Sprite musicButton = new Sprite(CAMERA_WIDTH / 12, cY - (CAMERA_HEIGHT / 13), this.mMusicTextureRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				switch (touchEvent.getAction()) {
 				case TouchEvent.ACTION_DOWN:
 					if (mMusic.isPlaying()) {
 						mMusic.pause();
 						music = false;
 					} else {
 						mMusic.play();
 						music = true;
 					}
 					break;
 				case TouchEvent.ACTION_MOVE:
 					break;
 				case TouchEvent.ACTION_UP:
 					break;
 				}
 				return true;
 			};
 		};
         
         this.mPauseScene.registerTouchArea(musicButton);
 		this.mPauseScene.attachChild(musicButton);
         
 		// Resume Button
 
 		final Sprite resumeButton = new Sprite(cX + (CAMERA_WIDTH / 10), cY
 				+ (CAMERA_HEIGHT / 2), this.mResumeTextureRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				switch (touchEvent.getAction()) {
 				case TouchEvent.ACTION_DOWN:
 					mHUD.clearChildScene();
 					scene.setIgnoreUpdate(false);
 					break;
 				case TouchEvent.ACTION_MOVE:
 					break;
 				case TouchEvent.ACTION_UP:
 					break;
 				}
 				return true;
 			};
 		};
 		this.mPauseScene.registerTouchArea(resumeButton);
 		this.mPauseScene.attachChild(resumeButton);
 
 		// Return to main menu button
 
 		final Sprite mainMenuButton = new Sprite(cX + (CAMERA_WIDTH / 10), cY
 				+ (CAMERA_HEIGHT / 3), this.mMainMenuTextureRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				switch (touchEvent.getAction()) {
 				case TouchEvent.ACTION_DOWN:
 					scene.detachChildren();
 					scene.clearEntityModifiers();
 					scene.clearTouchAreas();
 					scene.clearUpdateHandlers();
 					mHUD.detachChildren();
 					mPauseScene.detachChildren();
 					this.detachSelf();
 					onClick(mRenderSurfaceView);
 					finish();
 					break;
 				default:
 					break;
 				}
 				return true;
 			};
 		};
 		
 		this.mPauseScene.registerTouchArea(mainMenuButton);
 		this.mPauseScene.attachChild(mainMenuButton);
 		//save game button
 		
 		final Sprite saveButton = new Sprite(cX + (CAMERA_WIDTH / 10), cY
 				+ (CAMERA_HEIGHT / 6), this.mSaveTextureRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				switch (touchEvent.getAction()) {
 				case TouchEvent.ACTION_DOWN:
 					//update database here
 					saveGame();
 					mHUD.clearChildScene();
 					scene.setIgnoreUpdate(false);
 					//save to database
 					break;
 				case TouchEvent.ACTION_MOVE:
 					break;
 				case TouchEvent.ACTION_UP:
 					break;
 				}
 				return true;
 			};
 		};
 		this.mPauseScene.registerTouchArea(saveButton);
 		this.mPauseScene.attachChild(saveButton);
 
 		/* Makes the paused Game look through. */
 		this.mPauseScene.setBackgroundEnabled(false);
 
 		// Main Menu Button on HUD
		final Sprite pauseSprite = new Sprite(mCamera.getWidth() / 2 + CHARACTER_WIDTH + 50 , 300, this.mFaceTextureRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				switch (touchEvent.getAction()) {
 				case TouchEvent.ACTION_DOWN:
 					scene.setIgnoreUpdate(true);
 					mHUD.setChildScene(mPauseScene, false, true, true);
 					break;
 				default:
 					break;
 				}
 				return true;
 			};
 		};
 
 		for (int i = 0; i < numCharacters; i++) {
 			mHUD.attachChild(textStrokes[i]);
 		}
 
 		mHUD.registerTouchArea(diceButton);
 		mHUD.attachChild(diceButton);	
 		mHUD.registerTouchArea(finishTurnButton);
 		mHUD.attachChild(finishTurnButton);
 		mHUD.attachChild(textStroke5);
 
 
 		mHUD.registerTouchArea(pauseSprite);
 		mHUD.attachChild(pauseSprite);
 
 		mCamera.setHUD(mHUD);
 
 		/* Main Map Scene */
 
 		scene.setOnSceneTouchListener(this);
 
 		try {
 			final TMXLoader tmxLoader = new TMXLoader(this.getAssets(),
 					this.mEngine.getTextureManager(),
 					TextureOptions.BILINEAR_PREMULTIPLYALPHA,
 					this.getVertexBufferObjectManager(),
 					new ITMXTilePropertiesListener() {
 						@Override
 						public void onTMXTileWithPropertiesCreated(
 								final TMXTiledMap pTMXTiledMap,
 								final TMXLayer pTMXLayer,
 								final TMXTile pTMXTile,
 								final TMXProperties<TMXTileProperty> pTMXTileProperties) {
 						}
 					});
 			this.mTMXTiledMap = tmxLoader
 					.loadFromAsset("tmx/MultiGator14Layers.tmx");
 			this.mTMXTiledMap.setOffsetCenter(0, 0);
 
 			// this.toastOnUiThread("Cactus count in this TMXTiledMap: " +
 			// MainGameScreen.this.mCactusCount, Toast.LENGTH_LONG);
 		} catch (final TMXLoadException e) {
 			Debug.e(e);
 		}
 		moving = false;
 		turnDone = false;
 		turnNum = 1;
 
 		scene.attachChild(this.mTMXTiledMap);
 
 		/* Make the camera not exceed the bounds of the TMXEntity. */
 		this.mCamera.setBoundsEnabled(false);
 		this.mCamera.setBounds(0, 0, this.mTMXTiledMap.getWidth(),
 				this.mTMXTiledMap.getHeight());
 		this.mCamera.setBoundsEnabled(true);
 
 		
 		spriteList = new Sprite[numCharacters];
 		
 		for (int i = 0; i < numCharacters; i++) {
 			SpriteCoordinate loc = thePlayers.get(i).getSpriteLocation();
 			spriteList[i] = new Sprite(loc.getX(), loc.getY(), character[i], 
 					this.getVertexBufferObjectManager());
 		}
 
 		//Open Database
         dbhelper = new DbHelper(this);
         SQLiteDatabase db = dbhelper.openDB();
         Log.d("TEST", "Database has been opened");
         Log.d("TEST2", Integer.toString(thePlayers.get(0).gameId));
 
         String[] gameKey = { Integer.toString(thePlayers.get(0).gameId) };
       //Grab Game info
       		ArrayList gameList = dbhelper.getRow(1, gameKey);
       		
       		if (gameList.size()>0){
       			currentCharacter = Integer.valueOf((String)gameList.get(3));
       		}
       		
       	dbhelper.close();
         
 		this.mCamera.setChaseEntity(spriteList[currentCharacter]);
 
 		/* The layer for the player to walk on. */
 		final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
 
 		scene.registerUpdateHandler(new IUpdateHandler() {
 			@Override
 			public void reset() {
 			}
 
 			@Override
 			public void onUpdate(final float pSecondsElapsed) {
 				// /* Get the scene-coordinates of the players feet. */
 				float[] localCoord = new float[2];
 				localCoord[0] = spriteList[currentCharacter].getWidth() * .5f;
 				localCoord[1] = spriteList[currentCharacter].getHeight() * .5f;
 				final float[] playerFootCordinates = spriteList[currentCharacter]
 						.convertLocalCoordinatesToSceneCoordinates(localCoord);
 				
 				/* Get the tile the feet of the player are currently waking on. */
 				final TMXTile tmxTile = tmxLayer.getTMXTileAt(
 						playerFootCordinates[Constants.VERTEX_INDEX_X],
 						playerFootCordinates[Constants.VERTEX_INDEX_Y]);
 
 				if (movesLeft == 0 && diceDone) {
 					MainGameScreen.this.mCamera.updateChaseEntity();
 					finishTurnButton.setColor(Color.WHITE);
 				}
 
 				if (turnDone && finishTurn) { //&& swipeDone == false
 					moving = false;
 					move = false;
 					turnDone = false;
 					diceDone = false;
 					currentCharacter = (currentCharacter + 1) % (numCharacters);
 					saveGame();
 					// consider a delay here so the camera doesn't switch back and forth so fast
 					MainGameScreen.this.mCamera
 							.setChaseEntity(spriteList[currentCharacter]);
 					finishTurn = false;
 					diceButton.setColor(Color.WHITE);
 					finishTurnButton.setColor(Color.GRAY);					
 					checkCredits(currentCharacter);					
 
 				}
 			}
 		});
 		
 		for (int i = (numCharacters-1); i>=0 ; i--) {
 			spriteList[i].setScale(.1f);
 			scene.attachChild(spriteList[i]);
 		}
 
 		return scene;
 	}
 
 	boolean move = false;
 	
 	public boolean movement(SpriteCoordinate offset) {
 		if (movesLeft > 0) {
 
 			moving = true;
 
 			SpriteCoordinate characterLocation = thePlayers.get(currentCharacter).getSpriteLocation();
 			SpriteCoordinate newPosition = offset.add(characterLocation);
 			
 			if (!mainMapEvent.checkBoundaries(newPosition)) {
 				return false;
 			}
 			movesLeft--;
 			textStroke5.setText(" " + movesLeft);
 			if (movesLeft == 0) {
 				swipeDone = false;
                 turnDone = true;
                 moving = false;
 			}
 			moveSprite(newPosition, spriteList[currentCharacter]);
 		}
 		return true;
 	}
 	
 	public void moveSprite(final SpriteCoordinate newPosition,
 			final Sprite mySprite) {
 		
 		SpriteCoordinate characterLocation = thePlayers.get(currentCharacter).getSpriteLocation();
 		
 			mySprite.registerEntityModifier(new MoveModifier(0.5f,
 					characterLocation.getX(), characterLocation.getY(),
 					newPosition.getX(), newPosition.getY()) {
 				
 				@Override
 				protected void onModifierStarted(IEntity pItem) {
 					super.onModifierStarted(pItem);
 				}
 				
 				@Override
 				protected void onModifierFinished(IEntity pItem) {
 					thePlayers.get(currentCharacter).setLocation(mySprite.getX(), mySprite.getY());
 					super.onModifierFinished(pItem);
 					if (!eventCompleted) {
 						checkMiniGameHotSpots(currentCharacter);
 						eventCompleted = true;
 					}
 				}
 			});
 	}
 
 	// Checks the hot spots for the minigames
 	protected void checkMiniGameHotSpots(int current) {
 		Event.getEvent(thePlayers.get(current).getSpriteLocation(), this, thePlayers.get(current).getName(), thePlayers.get(current).getGraduated(), current, thePlayers);
 		
 		if (!(move || gameDone)) {
 			gameDone = true;
 		}
 		
 		swipeDone = false;
 		turnDone = true;
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		return false;
 	}
 
 	public void onClick(View view) {
 		Intent intent = new Intent(this, MainActivity.class);
 		startActivity(intent);
 	}
 
 	@Override
 	public synchronized void onResumeGame() {
 		if (this.mEngine != null)
 			super.onResumeGame();
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	public void onActivityResult (int requestCode, int resultCode, Intent data) {
 		this.eventCompleted = true;
 		if (!(move || gameDone)) {
 			gameDone = true;
 		}
 		swipeDone = false;
 		turnDone = true;
 
 		if (resultCode != RESULT_OK || data == null) {
 			return;
 		}
 
 		int result = data.getIntExtra(requestCode+"", 0);
 		addCredits(currentCharacter, result);
 		Log.d("MINIGAME", result+", "+resultCode);
 		
 		checkCredits(currentCharacter);
 	}
 	
 	private void addCredits(int character, int creditsToAdd) {
 		thePlayers.get(character).addCredits(creditsToAdd);
 		textStrokes[character].setText(thePlayers.get(character).getName()
 				+ "\nCredits: " + thePlayers.get(character).getCredits()
 				+ "\nCoins: " + thePlayers.get(character).getCoins());
 	}
 	
 	
 	private void checkCredits(final int character) {
 		if (thePlayers.get(character).getCredits() >= CREDITS_NEEDED_GRADUATE) {
 			runOnUiThread(new Runnable() {                  
 	            public void run() {
 	            	if (thePlayers.get(character).getGraduated() == false) {
 	            	    Builder alert = new AlertDialog.Builder(MainGameScreen.this);
 	            	    alert.setTitle("You have graduated!");
 	            	    alert.setMessage("Please head to the O'Connoll Center.");
 	            	    alert.setPositiveButton("OK", null);
 	            	    alert.show();
 	            	}
 	            	thePlayers.get(character).setGraduated(true);
 	            	/*Toast.makeText(getApplicationContext(), getString(R.string.ready_to_graduate, thePlayers.get(character).getName(), thePlayers.get(character).getCredits()),
 	            			   Toast.LENGTH_SHORT).show();*/
 	                }                  
 	            });
 
 		}
 	}
 
 	private void addCoins(int character, int coinsToAdd) {
 		thePlayers.get(character).addCoins(coinsToAdd);
 		textStrokes[character].setText(thePlayers.get(character).getName()
 				+ "\nCredits: " + thePlayers.get(character).getCredits()
 				+ "\nCoins: " + thePlayers.get(character).getCoins());
 	}
 	
 	public void saveGame(){
 		int numPlayers = thePlayers.size();
 		
 		DbHelper dbhelper = new DbHelper(this);
 		SQLiteDatabase db = dbhelper.openDB();
 		
 		Calendar time_date = Calendar.getInstance();
 
         int year = time_date.get(Calendar.YEAR);
         int month = time_date.get(Calendar.MONTH)+ 1;
         int day = time_date.get(Calendar.DATE);
         int hour = time_date.get(Calendar.HOUR_OF_DAY);
         int minute = time_date.get(Calendar.MINUTE);
         String timeDate = Integer.toString(month) + "/" + Integer.toString(day) + "/" + Integer.toString(year) + "   " + Integer.toString(hour) + ":" +Integer.toString(minute);
     
 		int gameId = thePlayers.get(0).getGameId();  
         
 		String[] table1Values = {Integer.toString(gameId), timeDate, Integer.toString(numCharacters), Integer.toString(currentCharacter)};
 		Log.d("debug,",Integer.toString(gameId) + "0" + Integer.toString(numCharacters) + Integer.toString(currentCharacter));
 		
         String[] table1Key = {Integer.toString(gameId)};
         dbhelper.updateRow(1, table1Key, table1Values); //Update game table
         
         //insert numPlayers rows into table 3
         for (int i = 0; i < numPlayers; i++)
         {
         	String[] characterKey = {Integer.toString((gameId<<2) + i)};
         	String[] newCharacter = {characterKey[0], thePlayers.get(i).getType(), thePlayers.get(i).getName(), Float.toString(thePlayers.get(i).getMapLocation().getX()), Float.toString(thePlayers.get(i).getMapLocation().getY()), Integer.toString(thePlayers.get(i).getCredits()), Integer.toString(thePlayers.get(i).getCoins()), Integer.toString(i)};
         	Log.d("debug,", characterKey[0]+ thePlayers.get(i).getType()+thePlayers.get(i).getName()+Float.toString(thePlayers.get(i).getMapLocation().getX())+Float.toString(thePlayers.get(i).getMapLocation().getY())+Integer.toString(thePlayers.get(i).getCredits())+Integer.toString(thePlayers.get(i).getCoins())+Integer.toString(i+1));
         	dbhelper.updateRow(2, characterKey, newCharacter);
         }
         
         dbhelper.close();
 		
 	}
 	
 	public void deleteGame(){
 		
 		int numPlayers = thePlayers.size();
 
 		DbHelper dbhelper = new DbHelper(this);
 		SQLiteDatabase db = dbhelper.openDB();
 		
 		int gameId = thePlayers.get(0).getGameId();  
         String[] table1Key = {Integer.toString(gameId)};
         dbhelper.deleteRow(1, table1Key); //Delete game table
 
         //delete numPlayers rows from table 3
         for (int i = 0; i < numPlayers; i++)
         {
         	String[] characterKey = {Integer.toString((gameId<<2) + i)};
         	dbhelper.deleteRow(2, characterKey);
         }
         
         dbhelper.deleteRow(4, table1Key); //Delete minigame table
 
 		
         dbhelper.close();
 
 	}
 	
 	@Override
 	public void onPause() {
 			super.onPause();
 			if (mMusic.isPlaying()) {
 				mMusic.pause();
 			}
 	}
 	
 	@Override
 	public void onResume() {
 			super.onResume();
 			if (music) {
 				mMusic.resume();
 			}
 	}
 }
 
 
