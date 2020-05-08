 package com.tacohen.killbots.UI;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.entity.scene.Scene;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.bitmap.BitmapTexture;
 import org.andengine.util.adt.io.in.IInputStreamOpener;
 import org.andengine.util.debug.Debug;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 
 import org.andengine.entity.sprite.ButtonSprite;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.util.Pair;
 import android.view.MotionEvent;
 import android.widget.Toast;
 
 import org.andengine.input.touch.TouchEvent;
 
 import com.tacohen.cloud.Cloud;
 import com.tacohen.cloud.OtherPlayerLocation;
 import com.tacohen.killbots.Logic.CurrentPlayerLocation;
 import com.tacohen.killbots.Logic.GridDimensions;
 import com.tacohen.killbots.Logic.MoveRobots;
 import com.tacohen.killbots.Logic.PlayerMovement;
 import com.tacohen.killbots.Logic.RobotLocations;
 
 /**
  * License notes: uses graphics (the arrows) from picol.org, and they want a linkback
  * License notes: uses graphics (characters,background) from KDE, I should be okay as long as this app is GPL-licensed
  * @author Timothy Cohen
  *
  */
 
 public class UICanvas extends SimpleBaseGameActivity {
 
 	String TAG = "UICanvas";
 
 	private static int CAMERA_WIDTH = 800;
 	private static int CAMERA_HEIGHT = 480;
 
 	private ITextureRegion mBackgroundTextureRegion, playerTexture, robotTexture, deadRobotTexture, leftArrow,rightArrow,upArrow,downArrow,questionMark,player2Texture;
 	private Sprite leftArrowSprite,rightArrowSprite,downArrowSprite,upArrowSprite,player, robot1, robot2, robot3, robot4, deadRobot,deadRobot2,deadRobot3,player2;
 
 	private ButtonSprite teleportButton;
 
 	private PlayerMovement playerMovement = new PlayerMovement();
 	private MoveRobots moveRobots = new MoveRobots();
 
 	private Integer robotCount;
 
 	private Integer score;
 
 	private ArrayList<Sprite> robotsList = new ArrayList<Sprite>();
 
 	private Font mFont;
 	
 	ArrayList<Sprite> deadRobotSpritesUnused = new ArrayList<Sprite>();
 
 	private int playerNumber = 1;//placeholder
 	private int otherPlayerNumber = 2;//placeholder
 	
 	private static Context context;
 	
 	private Boolean isTurn;
 	
 	private Boolean usingMultiplayer = false;
 	
 	public static Pair<Integer, Integer> otherPlayerLocationPair;
 	public static Pair<Integer, Integer> otherPlayerLocationPairOld;
 	
 	private static int timeBetweenServerCalls = 2000;//in milliseconds
 
     private Music mMusic;
     
     private boolean usingMusic = true;
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 		EngineOptions eo = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, 
 				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
 		eo.getAudioOptions().setNeedsMusic(true);
 		
 		return eo;
 	}
 
 	@Override
 	protected void onCreateResources() {
 		try {
 			
 			  MusicFactory.setAssetBasePath("music/");
               try {
                       this.mMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "MainTheme.mp3");
                       this.mMusic.setLooping(true);
               } catch (final IOException e) {
                       Debug.e("Error", e);
               }
 
 
 			this.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 48);
 			this.mFont.load();
 
 			// 1 - Set up bitmap textures
 			ITexture backgroundTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 				@Override
 				public InputStream open() throws IOException {
 					GridDimensions.setHeight(10);
 					GridDimensions.setWidth(10);
 					GridDimensions.setGridDimensions(new Pair<Integer,Integer>(10,10));
 					return getAssets().open("gfx/background-resized-10x10.png");   
 				}
 			});
 			ITexture player = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 				@Override
 				public InputStream open() throws IOException {
 					return getAssets().open("gfx/player.png");
 				}
 			});
 			ITexture player2 = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 				@Override
 				public InputStream open() throws IOException {
 					return getAssets().open("gfx/player2.png");
 				}
 			});
 			ITexture robot = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 				@Override
 				public InputStream open() throws IOException {
 					return getAssets().open("gfx/robot.png");
 				}
 			});
 			ITexture deadRobot = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 				@Override
 				public InputStream open() throws IOException {
 					return getAssets().open("gfx/deadRobot.png");
 				}
 			});
 			ITexture leftArrow = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 				@Override
 				public InputStream open() throws IOException {
 					return getAssets().open("gfx/left_arrow.png");
 				}
 			});
 			ITexture rightArrow = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 				@Override
 				public InputStream open() throws IOException {
 					return getAssets().open("gfx/right_arrow.png");
 				}
 			});
 			ITexture upArrow = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 				@Override
 				public InputStream open() throws IOException {
 					return getAssets().open("gfx/up_arrow.png");
 				}
 			});
 			ITexture downArrow = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 				@Override
 				public InputStream open() throws IOException {
 					return getAssets().open("gfx/down_arrow.png");
 				}
 			});
 			ITexture questionMark = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
 				@Override
 				public InputStream open() throws IOException {
 					return getAssets().open("gfx/questionmark.png");
 				}
 			});
 
 			// 2 - Load bitmap textures into VRAM
 			backgroundTexture.load();
 			player.load();
 			robot.load();
 			deadRobot.load();
 			leftArrow.load();
 			rightArrow.load();
 			upArrow.load();
 			downArrow.load();
 			questionMark.load();
 			player2.load();
 
 			// 3 - Set up texture regions
 			this.mBackgroundTextureRegion = TextureRegionFactory.extractFromTexture(backgroundTexture);
 			this.playerTexture = TextureRegionFactory.extractFromTexture(player);
 			this.robotTexture = TextureRegionFactory.extractFromTexture(robot);
 			this.deadRobotTexture = TextureRegionFactory.extractFromTexture(deadRobot);
 			this.playerTexture = TextureRegionFactory.extractFromTexture(player);
 			this.leftArrow = TextureRegionFactory.extractFromTexture(leftArrow);
 			this.rightArrow = TextureRegionFactory.extractFromTexture(rightArrow);
 			this.upArrow = TextureRegionFactory.extractFromTexture(upArrow);
 			this.downArrow = TextureRegionFactory.extractFromTexture(downArrow);
 			this.questionMark = TextureRegionFactory.extractFromTexture(questionMark);
 			this.player2Texture = TextureRegionFactory.extractFromTexture(player2);
 
 			
 		} catch (IOException e) {
 			Debug.e(e);
 		}
 
 
 
 	}
 
 	@Override
 	protected Scene onCreateScene() {
 		
 				
 		this.context = getApplicationContext();
 		
 		//Stop screwing up locations when replaying the game
 		RobotLocations.resetLocations();
 		
 		final CurrentPlayerLocation currentPlayerLocation = new CurrentPlayerLocation(this.context);
 		final OtherPlayerLocation otherPlayerLocation = new OtherPlayerLocation(this.context);
 		
 		// 1 - Create new scene
 		final Scene scene = new Scene();
 		Sprite backgroundSprite = new Sprite(0, 0, this.mBackgroundTextureRegion, getVertexBufferObjectManager());
 		scene.attachChild(backgroundSprite);
 
 		//scoring rules: player starts with 100, with -1 for each turn, -5 for each teleport, and +10 for each
 		//robot killed, but -20 for dying
 		score = 100;
 
 		final Text scoreTextHeader = new Text(550, 340, this.mFont, "Score:", 6, this.getVertexBufferObjectManager());
 		final Text scoreTextNumbers = new Text(550, 400, this.mFont, score.toString(), "XXXXX".length(), this.getVertexBufferObjectManager());
 
 		scene.attachChild(scoreTextHeader);
 		scene.attachChild(scoreTextNumbers);
 
 		robotCount = 0;
 
 		final Cloud cloud = new Cloud();
 		
 		//player 1 goes first
 		if (playerNumber == 1){
 			isTurn = true;
 		}
 		else{
 			isTurn = false;
 		}
 		
 		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
 		boolean using_multiplayer = sharedPreferences.getBoolean("multiplayer", false);
 		if (using_multiplayer){
 			usingMultiplayer = true;
 		}
 		playerNumber = sharedPreferences.getInt("playerNumber", 1);
 		if (playerNumber == 1){
 			otherPlayerNumber = 2;
 		}else{
 			otherPlayerNumber = 1;
 			playerNumber = 2;
 		}
 		boolean using_music = sharedPreferences.getBoolean("musicOn", true);
 		if (using_music){
 			usingMusic = true;
 			this.mMusic.play();
 		}else{
 			usingMusic = false;
 		}
 		
 
 		
 		if (usingMultiplayer){
 			otherPlayerLocationPair = otherPlayerLocation.getPlayerLocation(otherPlayerNumber);
 			Log.i(TAG, "Other player location is: "+otherPlayerLocationPair.toString());
 		}
 
 		//1.5, tacohen, attach the arrows directly to the background
 		leftArrowSprite = new Sprite(530,100, this.leftArrow, getVertexBufferObjectManager()){
 			@Override
 			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				switch (pSceneTouchEvent.getAction()) {
 				//Only move the player sprite once the user lifts his/her finger to prevent accidental multi-touching
 				case MotionEvent.ACTION_UP:{
 					Log.i(TAG, "Touched Left Arrow");
 					
 					Log.i(TAG, "Player moved, score is now: "+score);
 					if ((playerMovement.canPlayerMoveLeft(new Pair<Integer, Integer>(currentPlayerLocation.getPlayerXLocation(playerNumber), currentPlayerLocation.getPlayerYLocation(playerNumber))) && isTurn)){
 						player.setPosition(player.getX()-47,player.getY());
 						currentPlayerLocation.setPlayerLocation(currentPlayerLocation.getPlayerXLocation(playerNumber)-1, currentPlayerLocation.getPlayerYLocation(playerNumber),playerNumber);
 						
 						score -= 1;
 						scoreTextNumbers.setText(score.toString());
 						
 						moveRobots(scene, currentPlayerLocation, otherPlayerLocation);
 						
 						if (RobotLocations.liveRobotLocations().contains(currentPlayerLocation.getPlayerLocation(playerNumber))){
 							Log.i(TAG, "Player has died!");
 							score -=20;
 							scoreTextNumbers.setText(score.toString());
 							lose();
 						}
 						
 						if (usingMultiplayer){
 							isTurn = false;
 							//time for other player to move
 							otherPlayerLocationPairOld = otherPlayerLocationPair;
 							UICanvas.this.runOnUiThread(new Runnable() {
 						        @Override
 						        public void run() {
 						        	Toast.makeText(context, "Waiting for other player!", Toast.LENGTH_SHORT).show();						        }
 						    });
 							while (cloud.getPlayerLocation(otherPlayerNumber).equals(otherPlayerLocationPairOld)){
 								Log.i(TAG, "Waiting for other player!");
 								//wait for specified milliseconds before calling again
 								android.os.SystemClock.sleep(timeBetweenServerCalls);
 							}
 							Log.i(TAG,"Other player moved!");
 							//other player moved
 							Log.i(TAG, "Other player moved, their location is: "+otherPlayerLocationPair.first+" , "+otherPlayerLocationPair.second);
 							player2.setPosition((otherPlayerLocationPair.first+1)*47,otherPlayerLocationPair.second*47);
 							isTurn = true;
 						}
 					}
 					else{
 						//if the player can't move
 						if (!isTurn){
 							UICanvas.this.runOnUiThread(new Runnable() {
 						        @Override
 						        public void run() {
 						        	Toast.makeText(context, "It's not your turn!", Toast.LENGTH_LONG).show();						        }
 						    });						
 							}
 						else{
 							UICanvas.this.runOnUiThread(new Runnable() {
 						        @Override
 						        public void run() {
 						        	Toast.makeText(context, "There's something in your way!", Toast.LENGTH_LONG).show();						        }
 						    });
 							
 						}
 					}
 				}
 				}
 
 				return true;
 			}
 		};
 		scene.attachChild(leftArrowSprite);
 
 		rightArrowSprite = new Sprite(680,100, this.rightArrow, getVertexBufferObjectManager()){
 			@Override
 			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				switch (pSceneTouchEvent.getAction()) {
 				//Only move the player sprite once the user lifts his/her finger to prevent accidental multi-touching
 				case MotionEvent.ACTION_UP:{
 					Log.i(TAG, "Touched Right Arrow");
 					
 					if ((playerMovement.canPlayerMoveRight(new Pair<Integer, Integer>(currentPlayerLocation.getPlayerXLocation(playerNumber), currentPlayerLocation.getPlayerYLocation(playerNumber))) && isTurn)){
 						player.setPosition(player.getX()+47,player.getY());
 						currentPlayerLocation.setPlayerLocation(currentPlayerLocation.getPlayerXLocation(playerNumber)+1, currentPlayerLocation.getPlayerYLocation(playerNumber),playerNumber);
 												
 						score -= 1;
 						scoreTextNumbers.setText(score.toString());
 						Log.i(TAG, "Player moved, score is now: "+score);
 						
 						moveRobots(scene, currentPlayerLocation, otherPlayerLocation);
 
 						
 						if (RobotLocations.liveRobotLocations().contains(currentPlayerLocation.getPlayerLocation(playerNumber))){
 							Log.i(TAG, "Player has died!");
 							score -=20;
 							scoreTextNumbers.setText(score.toString());
 							lose();
 						}
 						
 						if (usingMultiplayer){
 							isTurn = false;
 							//time for other player to move
 							otherPlayerLocationPairOld = otherPlayerLocationPair;
 							UICanvas.this.runOnUiThread(new Runnable() {
 						        @Override
 						        public void run() {
 						        	Toast.makeText(context, "Waiting for other player!", Toast.LENGTH_SHORT).show();						        }
 						    });
 							while (cloud.getPlayerLocation(otherPlayerNumber).equals(otherPlayerLocationPairOld)){
 								Log.i(TAG, "Waiting for other player!");
 								//wait for specified milliseconds before calling again
 								android.os.SystemClock.sleep(timeBetweenServerCalls);
 							}
 							//other player moved
 							Log.i(TAG, "Other player moved, their location is: "+otherPlayerLocationPair.first+" , "+otherPlayerLocationPair.second);
 							player2.setPosition((otherPlayerLocationPair.first+1)*47,otherPlayerLocationPair.second*47);
 							isTurn = true;
 						}
 						
 					}
 					else{
 						//if the player can't move
 						if (!isTurn){
 							UICanvas.this.runOnUiThread(new Runnable() {
 						        @Override
 						        public void run() {
 						        	Toast.makeText(context, "It's not your turn!", Toast.LENGTH_LONG).show();						        }
 						    });						
 							}
 						else{
 							UICanvas.this.runOnUiThread(new Runnable() {
 						        @Override
 						        public void run() {
 						        	Toast.makeText(context, "There's something in your way!", Toast.LENGTH_LONG).show();						        }
 						    });
 							
 						}
 					}
 					
 				}
 				}
 
 				return true;
 			}
 		};
 		scene.attachChild(rightArrowSprite);
 
 		upArrowSprite = new Sprite(605,0, this.upArrow, getVertexBufferObjectManager()){
 			@Override
 			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				switch (pSceneTouchEvent.getAction()) {
 				//Only move the player sprite once the user lifts his/her finger to prevent accidental multi-touching
 				case MotionEvent.ACTION_UP:{
 					Log.i(TAG, "Touched Up Arrow");
 					
 					if ((playerMovement.canPlayerMoveUp(new Pair<Integer, Integer>(currentPlayerLocation.getPlayerXLocation(playerNumber), currentPlayerLocation.getPlayerYLocation(playerNumber))) && isTurn)){
 						player.setPosition(player.getX(),player.getY()-47);
 						currentPlayerLocation.setPlayerLocation(currentPlayerLocation.getPlayerXLocation(playerNumber), currentPlayerLocation.getPlayerYLocation(playerNumber)-1,playerNumber);
 						
 						score -= 1;
 						scoreTextNumbers.setText(score.toString());
 						Log.i(TAG, "Player moved, score is now: "+score);
 						
 						moveRobots(scene, currentPlayerLocation, otherPlayerLocation);
 
 						
 						if (RobotLocations.liveRobotLocations().contains(currentPlayerLocation.getPlayerLocation(playerNumber))){
 							Log.i(TAG, "Player has died!");
 							score -=20;
 							scoreTextNumbers.setText(score.toString());
 							lose();
 						}
 						
 						if (usingMultiplayer){
 							isTurn = false;
 							//time for other player to move
 							otherPlayerLocationPairOld = otherPlayerLocationPair;
 							UICanvas.this.runOnUiThread(new Runnable() {
 						        @Override
 						        public void run() {
 						        	Toast.makeText(context, "Waiting for other player!", Toast.LENGTH_SHORT).show();						        }
 						    });
 							while (cloud.getPlayerLocation(otherPlayerNumber).equals(otherPlayerLocationPairOld)){
 								Log.i(TAG, "Waiting for other player!");
 								//wait for specified milliseconds before calling again
 								android.os.SystemClock.sleep(timeBetweenServerCalls);
 							}
 							//other player moved
 							Log.i(TAG, "Other player moved, their location is: "+otherPlayerLocationPair.first+" , "+otherPlayerLocationPair.second);
 							player2.setPosition((otherPlayerLocationPair.first)*47,otherPlayerLocationPair.second*47);
 							isTurn = true;
 						}
 					}
 					else{
 						//if the player can't move
 						if (!isTurn){
 							UICanvas.this.runOnUiThread(new Runnable() {
 						        @Override
 						        public void run() {
 						        	Toast.makeText(context, "It's not your turn!", Toast.LENGTH_LONG).show();						        }
 						    });						
 							}
 						else{
 							UICanvas.this.runOnUiThread(new Runnable() {
 						        @Override
 						        public void run() {
 						        	Toast.makeText(context, "There's something in your way!", Toast.LENGTH_LONG).show();						        }
 						    });
 							
 						}
 					}
 				}
 				}
 
 				return true;
 			}
 		};
 		scene.attachChild(upArrowSprite);
 
 		downArrowSprite = new Sprite(605,200, this.downArrow, getVertexBufferObjectManager()){
 			@Override
 			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				switch (pSceneTouchEvent.getAction()) {
 				//Only move the player sprite once the user lifts his/her finger to prevent accidental multi-touching
 				case MotionEvent.ACTION_UP:{
 					Log.i(TAG, "Touched Down Arrow");
 					
 					if ((playerMovement.canPlayerMoveDown(new Pair<Integer, Integer>(currentPlayerLocation.getPlayerXLocation(playerNumber), currentPlayerLocation.getPlayerYLocation(playerNumber))) && isTurn)){
 						player.setPosition((player.getX()),player.getY()+47);
 						Log.i(TAG,"Moving down, now location in pixels is: "+(player.getX())+" , "+player.getY()+47);
						currentPlayerLocation.setPlayerLocation(currentPlayerLocation.getPlayerXLocation(playerNumber), currentPlayerLocation.getPlayerYLocation(playerNumber)+47,playerNumber);
 						
 						score -= 1;
 						scoreTextNumbers.setText(score.toString());
 						Log.i(TAG, "Player moved, score is now: "+score);
 						
 						moveRobots(scene,currentPlayerLocation,otherPlayerLocation);
 
 						if (RobotLocations.liveRobotLocations().contains(currentPlayerLocation.getPlayerLocation(playerNumber))){
 							Log.i(TAG, "Player has died!");
 							score -=20;
 							scoreTextNumbers.setText(score.toString());
 							lose();
 						}
 						
 						if (usingMultiplayer){
 							isTurn = false;
 							//time for other player to move
 							otherPlayerLocationPairOld = otherPlayerLocationPair;
 							UICanvas.this.runOnUiThread(new Runnable() {
 						        @Override
 						        public void run() {
 						        	Toast.makeText(context, "Waiting for other player!", Toast.LENGTH_SHORT).show();						        }
 						    });
 							while (cloud.getPlayerLocation(otherPlayerNumber).equals(otherPlayerLocationPairOld)){
 								Log.i(TAG, "Waiting for other player!");
 								//wait for specified milliseconds before calling again
 								android.os.SystemClock.sleep(timeBetweenServerCalls);
 							}
 							//other player moved
 							Log.i(TAG, "Other player moved, their location is: "+otherPlayerLocationPair.first+" , "+otherPlayerLocationPair.second);
 							player2.setPosition((otherPlayerLocationPair.first)*47,otherPlayerLocationPair.second*47);
 							isTurn = true;
 						}
 
 					}
 					else{
 						//if the player can't move
 						if (!isTurn){
 							UICanvas.this.runOnUiThread(new Runnable() {
 						        @Override
 						        public void run() {
 						        	Toast.makeText(context, "It's not your turn!", Toast.LENGTH_LONG).show();						        }
 						    });						
 							}
 						else{
 							UICanvas.this.runOnUiThread(new Runnable() {
 						        @Override
 						        public void run() {
 						        	Toast.makeText(context, "There's something in your way!", Toast.LENGTH_LONG).show();						        }
 						    });
 							
 						}
 					}
 				}
 				}
 
 				return true;
 			}
 
 			
 		};
 		scene.attachChild(downArrowSprite);
 
 		teleportButton = new ButtonSprite(675, 340, this.questionMark, this.getVertexBufferObjectManager()){
 			@Override
 			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {                                         
 				if ((pSceneTouchEvent.isActionUp() && isTurn)) {
 					Log.i(TAG,"User touched teleport button!");
 					score -= 5;
 					scoreTextNumbers.setText(score.toString());
 
 					Pair<Integer,Integer> newLocation = playerMovement.teleportPlayer();
 					Log.i(TAG, "Teleported, new location is: "+newLocation.first.toString()+" , "+newLocation.second.toString());
 
 					player.setPosition((newLocation.first)*47,newLocation.second*47);
 					currentPlayerLocation.setPlayerLocation(newLocation.first, newLocation.second,playerNumber);
 					
 					moveRobots(scene, currentPlayerLocation, otherPlayerLocation);
 
 					if ((RobotLocations.liveRobotLocations().contains(currentPlayerLocation.getPlayerLocation(playerNumber)) || 
 							RobotLocations.deadRobotLocations().contains(currentPlayerLocation.getPlayerLocation(playerNumber)))){
 						Log.i(TAG, "Player has died!");
 						score -=20;
 						scoreTextNumbers.setText(score.toString());
 						lose();
 					}
 					
 					if (usingMultiplayer){
 						isTurn = false;
 						otherPlayerLocationPairOld = otherPlayerLocationPair;
 						UICanvas.this.runOnUiThread(new Runnable() {
 					        @Override
 					        public void run() {
 					        	Toast.makeText(context, "Waiting for other player!", Toast.LENGTH_SHORT).show();						        }
 					    });
 						while (cloud.getPlayerLocation(otherPlayerNumber).equals(otherPlayerLocationPairOld)){
 							Log.i(TAG, "Waiting for other player!");
 							//wait for specified milliseconds before calling again
 							android.os.SystemClock.sleep(timeBetweenServerCalls);
 							
 						}
 						//other player moved
 						Log.i(TAG, "Other player moved, their location is: "+otherPlayerLocationPair.first+" , "+otherPlayerLocationPair.second);
 						player2.setPosition((otherPlayerLocationPair.first)*47,otherPlayerLocationPair.second*47);
 						isTurn = true;
 					}
 
 					
 					return true;
 				}
 				else{
 					//if the player can't move
 					if (!isTurn){
 						UICanvas.this.runOnUiThread(new Runnable() {
 					        @Override
 					        public void run() {
 					        	Toast.makeText(context, "It's not your turn!", Toast.LENGTH_LONG).show();						        }
 					    });						
 						}
 
 				}
 				
 				
 				return false;
 			}
 
 		};
 		scene.attachChild(teleportButton);
 		scene.registerTouchArea(teleportButton);
 
 		//tacohen note: top left grid square is (1,0), NOT (0,0)!!!!!
 		//tacohen note: (280,130) places the player at 6x7 on the grid
 		//tacohen note: every square in the any direction is 47 pixels (e.g. 6x7 is (280,130),
 		//7x6 is (327,177)
 		//UIObject player = new UIObject(1, 327, 177, this.playerTexture, getVertexBufferObjectManager()) {
 		//player = new Sprite(327,177, this.playerTexture, getVertexBufferObjectManager());// {
 		//CurrentPlayerLocation.setPlayerLocation(6, 5);
 		player = new Sprite(376,0, this.playerTexture, getVertexBufferObjectManager());
 		currentPlayerLocation.setPlayerLocation(8, 0,playerNumber);
 		//cloud.addPlayer(playerNumber, 8, 0);
 
 		//Placeholder location values, replace later!
 		robot1 = new Sprite(47,0, this.robotTexture, getVertexBufferObjectManager());
 		RobotLocations.setRobotLocation(1,0);
 		robotCount += 1;
 
 		robot2 = new Sprite(423,224, this.robotTexture, getVertexBufferObjectManager());
 		RobotLocations.setRobotLocation(9,5);
 		robotCount += 1;
 	
 		robot3 = new Sprite(94,47, this.robotTexture, getVertexBufferObjectManager());
 		RobotLocations.setRobotLocation(2,1);
 		robotCount += 1;
 		
 		robot4 = new Sprite(376,318, this.robotTexture, getVertexBufferObjectManager());
 		RobotLocations.setRobotLocation(8,7);
 		robotCount += 1;
 	
 		//Place dead robot way off the screen edge do it can't be seen until needed!
 		deadRobot = new Sprite(900,900, this.deadRobotTexture, getVertexBufferObjectManager());// {
 		RobotLocations.setDeadRobotLocation(11,11);
 		//Place dead robot way off the screen edge do it can't be seen until needed!
 		deadRobot2 = new Sprite(951,900, this.deadRobotTexture, getVertexBufferObjectManager());// {
 		RobotLocations.setDeadRobotLocation(12,12);
 		//Place dead robot way off the screen edge do it can't be seen until needed!
 		deadRobot3 = new Sprite(1002,900, this.deadRobotTexture, getVertexBufferObjectManager());// {
 		RobotLocations.setDeadRobotLocation(13,13);
 		
 		if((usingMultiplayer) && playerNumber == 1){
 			player2 = new Sprite(327,177, this.player2Texture, getVertexBufferObjectManager());// {
 			otherPlayerLocation.setPlayerLocation(6, 5, otherPlayerNumber);
 			cloud.addPlayer(otherPlayerNumber, 6, 5);
 			
 			cloud.addPlayer(playerNumber, 8, 0);
 		}
 		else if ((usingMultiplayer) && playerNumber == 2){
 			player = new Sprite(327,177, this.player2Texture, getVertexBufferObjectManager());// {
 			currentPlayerLocation.setPlayerLocation(6, 5, playerNumber);
 			cloud.addPlayer(playerNumber, 6, 5);
 			
 			player2 = new Sprite(376,0, this.playerTexture, getVertexBufferObjectManager());
 			otherPlayerLocation.setPlayerLocation(8, 0,otherPlayerNumber);
 			cloud.addPlayer(otherPlayerNumber, 8, 0);
 		}
 	
 		scene.attachChild(player);
 		scene.attachChild(robot1);
 		scene.attachChild(robot2);
 		scene.attachChild(robot3);
 		scene.attachChild(robot4);
 		scene.attachChild(deadRobot);
 		scene.attachChild(deadRobot2);
 		scene.attachChild(deadRobot3);
 		if(usingMultiplayer){
 			scene.attachChild(player2);
 		}
 
 
 		robotsList.add(0,robot1);
 		robotsList.add(1,robot2);
 		robotsList.add(2,robot3);
 		robotsList.add(3,robot4);
 		
 		deadRobotSpritesUnused.add(deadRobot3);
 		deadRobotSpritesUnused.add(deadRobot2);
 		deadRobotSpritesUnused.add(deadRobot);
 		
 		//Add touch handlers
 		scene.registerTouchArea(downArrowSprite);
 		scene.registerTouchArea(leftArrowSprite);
 		scene.registerTouchArea(rightArrowSprite);
 		scene.registerTouchArea(upArrowSprite);
 		scene.setTouchAreaBindingOnActionDownEnabled(true);
 
 
 		return scene;
 	}
 
 	public void lose(){
 		Log.i(TAG, "Player has lost!");
 		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
 		// Vibrate for 500 milliseconds
 		v.vibrate(500);
 		Intent i = new Intent(UICanvas.this,GameFinish.class);
 		i.putExtra("didWin", false);
 		i.putExtra("score", score);
 		startActivityForResult(i,0);
 		if (usingMusic){
 			this.mMusic.pause();
 		}
 		finish();
 	}
 
 	public void win(){
 		Log.i(TAG, "Player has won!");
 		Intent i = new Intent(UICanvas.this,GameFinish.class);
 		i.putExtra("didWin", true);
 		i.putExtra("score", score);
 		startActivityForResult(i,0);
 		if (usingMusic){
 			this.mMusic.pause();
 		}		
 		finish();
 	}
 	
 	public static Context getAppContext() {
         return UICanvas.context;
     }
 	
 	private void moveRobots(Scene scene, CurrentPlayerLocation currentPlayerLocation, OtherPlayerLocation otherPlayerLocation){
 		int iterCount = robotsList.size();
 		ArrayList<Integer> excludedList = new ArrayList<Integer>();
 		for (int i = 0; i< iterCount; i++){
 			Log.i(TAG,"Going around the outer loop again! i is "+i);
 			if (!excludedList.contains(i)){
 				Pair<Integer, Integer> robotLoc = RobotLocations.liveRobotLocations.get(i);
 				Log.i(TAG,"old robot "+i+" location is: "+robotLoc.first+","+robotLoc.second);
 				Pair<Integer,Integer> newRobotLocation = moveRobots.desiredRobotLocation(robotLoc.first, robotLoc.second,currentPlayerLocation.getPlayerXLocation(playerNumber),currentPlayerLocation.getPlayerYLocation(playerNumber));
 				Log.i(TAG,"new robot "+i+" location is:"+newRobotLocation.first+" , "+newRobotLocation.second);
 				if (RobotLocations.liveRobotLocations.contains(newRobotLocation)){
 					Log.i(TAG, "Robots collided!");
 					//robot dies
 					//RobotLocations.removeRobotLocation(newRobotLocation.first, newRobotLocation.first);
 					Sprite robot = robotsList.get(i);
 					excludedList.add(i);
 					Log.i(TAG,"Added "+i+" to excluded list!");
 					for (int i2 = 0; i2< iterCount; i2++){
 						Log.i(TAG,"Going around the inner loop again! i2 is "+i2);
 						Log.i(TAG,"Size of robotsList is: "+robotsList.size());
 						if (!excludedList.contains(i2)){
 							Sprite robot2 = robotsList.get(i2);
 							if (robot.collidesWith(robot2)){
 								Log.i(TAG, "Found the robot it collided with! Robot is "+i2);
 								excludedList.add(i2);
 								//robotsList.remove(robot);
 								//robotsList.remove(robot2);
 								robotCount -= 2;
 								score += 10;
 								scene.detachChild(robot);
 								scene.detachChild(robot2);
 								deadRobotSpritesUnused.get(0).setPosition((newRobotLocation.first+1)*47, newRobotLocation.second*47);
 								//mark this sprite as used
 								deadRobotSpritesUnused.remove(0);
 								RobotLocations.setDeadRobotLocation(RobotLocations.liveRobotLocations.get(i).first,RobotLocations.liveRobotLocations.get(i).second);
 								//RobotLocations.liveRobotLocations.remove(i);
 								//RobotLocations.liveRobotLocations.remove(i2);
 								if (robotCount == 0){
 									win();
 								}
 								i2 = 5;//break out of the for loop!
 							}
 						}else{
 							Log.i(TAG, "Inner For Loop; Excluded list contained robot "+i2);
 
 						}
 					}
 				}else{
 					if (RobotLocations.deadRobotLocations.contains(newRobotLocation)){
 						Log.i(TAG, "Robot ran into dead robot!");
 						excludedList.add(i);
 						RobotLocations.setDeadRobotLocation(newRobotLocation.first,newRobotLocation.second);
 						Sprite robot = robotsList.get(i);
 						scene.detachChild(robot);
 						//robotsList.remove(robot);
 						robotCount -= 1;
 						score += 5;
 						//RobotLocations.liveRobotLocations.remove(i);
 						if (robotCount == 0){
 							win();
 						}
 					}
 					else{
 						//robot moves
 						RobotLocations.moveRobotLocation(i,newRobotLocation.first, newRobotLocation.second);
 						robotsList.get(i).setPosition(newRobotLocation.first*47, newRobotLocation.second*47);
 					}
 
 				}
 
 			}else{
 				Log.i(TAG, "Outer For Loop; Excluded list contained robot "+i);
 			}
 		}
 		for (int i=0; i < excludedList.size();i++){
 			//remove all the robots that we marked for death by putting in the exclusion list
 			Log.i(TAG, "Removing robot "+excludedList.get(i)+" from exclusion list");
 			RobotLocations.liveRobotLocations.remove(excludedList.get(i).intValue());
 			//RobotLocations.liveRobotLocations.remove(robotsList.get(excludedList.get(i)));
 			robotsList.remove(robotsList.get(excludedList.get(i)));
 		}
 	}
 	
 	@Override
 	public void onBackPressed()
 	{
 		Intent i = new Intent(UICanvas.this,Opening.class);
 		startActivityForResult(i,0);
 		this.mMusic.pause();
 		finish();
 	}
 }
