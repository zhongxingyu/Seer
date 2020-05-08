 package com.jthtml.zebraDrop;
 
 import java.util.Iterator;
 
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.TimeUtils;
 import com.badlogic.gdx.utils.Pool;
 import com.badlogic.gdx.Preferences;
 import com.jthtml.zebraDrop.GoogleInterface;
 
 
 public class ZebraDrop implements ApplicationListener {
 	
 	enum State {
 		Paused,
 		Normal,
 		Reduced
 	}
 
 	private GoogleInterface platformInterface;
 	
 	private static final float ZEBRA_FRAME_DURATION = 0.02f;
 	private static final float UFO_FRAME_DURATION = 0.08f;
 	
 	TextureRegion bucketImage;
 	TextureRegion backgroundImage;
 	TextureRegion tapitImage;
 	TextureRegion playControllerImage;
 	TextureRegion gameOverImage;
 	TextureAtlas atlas;
 	Sound dropSound;
 	Music rainMusic;
 	SpriteBatch batch;
 	OrthographicCamera camera;
 	Rectangle bucket;
 	Rectangle bucketBounds;
 	Rectangle dropper;
 	Rectangle tapItBounds;
 	Rectangle highScoreBounds;
 	Rectangle highLevelBounds;
 	Rectangle achivementsBounds;
 	Rectangle loginBounds;
 	Rectangle touchSpot;
 	Array<Rectangle> raindrops;
 	long lastDropTime;
 	long neededDrops;
 	long dropRate;
 	long maxDropRate;
 	long minDropRate;
 	int dropSpeed;
 	int maxDropSpeed;
 	int minDropSpeed;
 	int dropCount;
 	int numDropped;
 	int level;
 	int ptVal;
 	int points;
 	int bonus;
 	int buckets;
 	int maxW;
 	int maxH;
 	int lineH;
 	BitmapFont font;
 	int dropDir;
 	State gameState;
 	Preferences prefs;
 	int highScore;
 	int highLevel;

	Vector3 touchPos;
 	
 	TextureRegion zebraFrame;
 	private Animation zebraAnimation;
 	float stateTime;
 	
 	TextureRegion ufoFrame;
 	private Animation ufoAnimation;
 
 	
 	// Rectangle pool used for drops
 	// good to avoid instantiation each frame
 	private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
 		@Override
 		protected Rectangle newObject() {
 			return new Rectangle();
 		}
 	};
 	
 	
 	public ZebraDrop(GoogleInterface aInterface){
 		platformInterface = aInterface;
 //		platformInterface.Login();
 	}
 
 	@Override
 	public void create() {
 		
 //		Texture.setEnforcePotImages(false);
 
 		// Load high Score and high level
 		prefs = Gdx.app.getPreferences("My Preferences");
 		
 		highScore = prefs.getInteger("highScore");
 		highLevel = prefs.getInteger("highLevel");
 		
 		maxW = 1280;
 		maxH = 720;
 		
 		// load the images for the droplet and the bucket, 64x64 pixels each
 
 		atlas = new TextureAtlas(Gdx.files.internal("zdImages.atlas"));
 		
 		bucketImage = atlas.findRegion("bucket");
 		backgroundImage = atlas.findRegion("background");
 		tapitImage = atlas.findRegion("tapit");
 		gameOverImage = atlas.findRegion("gameover");
 		playControllerImage = atlas.findRegion("ic_play_games_badge_green");
 		
 		stateTime = 0f;  
 		TextureRegion[] zebraFrames = new TextureRegion[16];
 		for (int i = 0 ; i < 16  ; i++) {
 			if (i+1 < 10) {
 				zebraFrames[i] = atlas.findRegion("hero0" + (i+1));	
 			}
 			else {
 				zebraFrames[i] = atlas.findRegion("hero" + (i+1));
 			}
 		}
 		zebraAnimation = new Animation(ZEBRA_FRAME_DURATION, zebraFrames);
 		
 		TextureRegion[] ufoFrames = new TextureRegion[12];
 		for (int i = 0 ; i < 12  ; i++) {
 			ufoFrames[i] = atlas.findRegion("ufo" + (i+1));
 			Gdx.app.log("texture_load", i + " - ufo" + (i+1));
 		}
 		ufoAnimation = new Animation(UFO_FRAME_DURATION, ufoFrames);
 		
 		
 		// load the drop sound effect and the rain background "music"
 		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
 		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
 
 		// start the playback of the background music immediately
 		rainMusic.setLooping(true);
 
 		// Load our font
 		font = new BitmapFont(Gdx.files.internal("data/bell_goth_64.fnt"),
 		         Gdx.files.internal("data/bell_goth_64_0.png"), false);
 
 		// Set Line height
 		lineH = 70;
 		
 		// create the camera and the SpriteBatch
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false, maxW, maxH);
 		batch = new SpriteBatch();
 
 		// create a Rectangle to logically represent the bucket
 		bucket = new Rectangle();
 		bucket.x = maxW / 2 - 64 / 2; // center the bucket horizontally
 		bucket.y = 20 + lineH; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
 		bucket.width = 64;
 		bucket.height = 64;
 
 		bucketBounds = new Rectangle();
 		bucketBounds.width = 64;
 		bucketBounds.height = 212;
 		bucketBounds.x = bucket.x;
 		bucketBounds.y = bucket.y;
 		
 		tapItBounds = new Rectangle();
 		tapItBounds.width = 365;
 		tapItBounds.height = 195;
 		tapItBounds.x = (maxW/2) - 182 ;
 		tapItBounds.y = (maxH/2) - 97 ;
 		
 		
 		
 		achivementsBounds = new Rectangle();
 		achivementsBounds.width = 400;
 		achivementsBounds.height = 64;
 		achivementsBounds.x = 35;
 		achivementsBounds.y = maxH-150;		
 
 		highScoreBounds = new Rectangle();
 		highScoreBounds.width = 400;
 		highScoreBounds.height = 64;
 		highScoreBounds.x = achivementsBounds.x ;
 		highScoreBounds.y = achivementsBounds.y - 75;
 
 		highLevelBounds = new Rectangle();
 		highLevelBounds.width = 400;
 		highLevelBounds.height = 64;
 		highLevelBounds.x = achivementsBounds.x ;
 		highLevelBounds.y = highScoreBounds.y-75;
 		
 		loginBounds = new Rectangle();
 		loginBounds.width = 200;
 		loginBounds.height = 64;
 		loginBounds.x = achivementsBounds.x ;
 		loginBounds.y = highLevelBounds.y-75;		
 
 		
 		
 
 		touchSpot = new Rectangle();
 		touchSpot.width = 16;
 		touchSpot.height = 16;

		touchPos = new Vector3();

 		
 		// create the dropper
 		dropper = new Rectangle();
 		dropper.x = maxW / 2 - 64 /2; // start out centered
 		dropper.y = maxH - 120; // 
 		dropper.width = 64;
 		dropper.height = 64;
 		
 		// create the raindrops array and spawn the first raindrop
 		dropRate = 1000000000;
 		minDropRate = 1000000000;
 		maxDropRate = 80000000;
 		dropSpeed = 200;
 		minDropSpeed = 200;
 		maxDropSpeed = 800;
 		neededDrops = 10;
 		level = 1;
 		dropDir = 1;
 		ptVal = 1;
 		points = 0;
 		bonus = 0;
 		buckets = 3;
 		raindrops = new Array<Rectangle>();
 		gameState = State.Paused;
 		//	      spawnRaindrop();
 	}
 
 	private void newLevel() {
 		gameState = State.Paused;		   
 		if (neededDrops == ((level -1) *10) /2) {
 		} else {
 			level = level + 1;
 		}
 		dropRate = dropRate / 2 ;
 		if (dropRate < maxDropRate) {dropRate = maxDropRate;}
 		//dropSpeed = dropSpeed + 50;
 		dropSpeed = minDropSpeed + (level * 50);
 		if (dropSpeed > maxDropSpeed) {dropSpeed = maxDropSpeed;}
 		dropCount = 0;
 		neededDrops = level * 10;
 		ptVal = ptVal + 1;
 		if (ptVal > 8) {ptVal = 8;}
 		numDropped = 0;
 	}
 
 	private void dropLevel() {
 		gameState = State.Paused;
 		buckets = buckets - 1;
 		if (buckets < 1) { buckets = 0;}
 
 		//		dropRate = minDropRate;
 		dropRate = dropRate * 2;
 		if (dropRate > minDropRate) { dropRate = minDropRate;}
 
 //		dropSpeed = dropSpeed - 50;
 		dropSpeed = minDropSpeed;
 		if (dropSpeed < minDropSpeed) { dropSpeed = minDropSpeed;}
 
 		bucketBounds.height = bucketBounds.height - 84;
 		//		ptVal = 1;
 		ptVal = ptVal - 1;
 		dropCount = 0;
 		neededDrops = 10;
 		if (neededDrops < 10) { neededDrops = 10;}
 		numDropped = 0;
 	}
 
 
 	private void spawnRaindrop() {
 		if (numDropped < neededDrops) {
 			Rectangle raindrop = rectPool.obtain();;
 			raindrop.x = dropper.x;
 			raindrop.y = dropper.y;
 			raindrop.width = 52;
 			raindrop.height = 42;
 			raindrops.add(raindrop);
 			lastDropTime = TimeUtils.nanoTime();
 			numDropped++;
 		}
 	}
 		
 	@Override
 	public void render() {
 		if (buckets > 0) {
 			// We still have lives available so run the game loop
 			
 			if (gameState == State.Paused) {
 				if (points > 0) {
 					if (rainMusic.isPlaying()) {
 						rainMusic.stop();
 					}
 				}
 				Gdx.gl.glClearColor(0, 0, 0.2f, 1);
 				Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 				// tell the camera to update its matrices.
 				camera.update();
 
 				// tell the SpriteBatch to render in the
 				// coordinate system specified by the camera.
 				batch.setProjectionMatrix(camera.combined);
 
 				if(Gdx.input.isTouched()) {
 					touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
 					camera.unproject(touchPos);
 					touchSpot.x = touchPos.x;
 					touchSpot.y = touchPos.y;
 					if (touchSpot.overlaps(tapItBounds)) {
 						rainMusic.play();
						raindrops = new Array<Rectangle>();
 						gameState = State.Normal;			
 						spawnRaindrop();
 					}
 					if (touchSpot.overlaps(highScoreBounds)) {
 						platformInterface.getScores();
 					}
 					if (touchSpot.overlaps(highLevelBounds)) {
 						platformInterface.getLevels();
 					}
 					if (touchSpot.overlaps(achivementsBounds)) {
 						platformInterface.getAchievements();
 					}
 					if (touchSpot.overlaps(loginBounds)) {
 						if (platformInterface.getSignedIn()) {
 							platformInterface.LogOut();
 						} else {
 							platformInterface.Login();
 						}
 					}
 				}
 
 				// begin a new batch and draw the bucket and
 				// all drops
 				batch.begin();
 				batch.draw(backgroundImage, 0, 0);
 				font.draw(batch, Long.toString(points), 20, lineH);
 				font.draw(batch, "HS: " + Long.toString(highScore) + " HL: " + Long.toString(highLevel), maxW/2 - 160, lineH);
 				font.draw(batch, "Level: " + Integer.toString(level), maxW-(8*30), lineH);		
 				batch.draw(tapitImage, tapItBounds.x, tapItBounds.y);				
 
 				if (points == 0) {
 					batch.draw(playControllerImage, achivementsBounds.x, achivementsBounds.y);
 					batch.draw(playControllerImage, highScoreBounds.x, highScoreBounds.y);
 					batch.draw(playControllerImage, loginBounds.x, loginBounds.y);
 					batch.draw(playControllerImage, highLevelBounds.x, highLevelBounds.y);
 					font.draw(batch, "ZEBRA DROP!!!", tapItBounds.x + tapItBounds.width, maxH-100);
 					font.draw(batch, "Achievements", achivementsBounds.x + 70, achivementsBounds.y + achivementsBounds.height - 12);
 					font.draw(batch, "High Scores", highScoreBounds.x + 70, highScoreBounds.y + highScoreBounds.height - 12);		
 					font.draw(batch, "High Levels", highLevelBounds.x + 70, highLevelBounds.y + highLevelBounds.height - 12);		
 					if (platformInterface.getSignedIn()) {
 						font.draw(batch, "Logout", loginBounds.x + 70, loginBounds.y + loginBounds.height - 12);		
 					} else {
 						font.draw(batch, "Login", loginBounds.x + 70, loginBounds.y + loginBounds.height - 12);		
 					}
 				}
 				
 				batch.end();
 			} else {
 				// clear the screen with a dark blue color. The
 				// arguments to glClearColor are the red, green
 				// blue and alpha component in the range [0,1]
 				// of the color to be used to clear the screen.
 				Gdx.gl.glClearColor(0, 0, 0.2f, 1);
 				Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 				stateTime += Gdx.graphics.getDeltaTime(); 
 				zebraFrame = zebraAnimation.getKeyFrame(stateTime, true);
 				ufoFrame = ufoAnimation.getKeyFrame(stateTime, true);
 				
 				// tell the camera to update its matrices.
 				camera.update();
 
 				// tell the SpriteBatch to render in the
 				// coordinate system specified by the camera.
 				batch.setProjectionMatrix(camera.combined);
 
 				// begin a new batch and draw the bucket and
 				// all drops
 				batch.begin();
 				batch.draw(backgroundImage, 0, 0);
 				batch.draw(bucketImage, bucket.x, bucket.y);
 
 				if (buckets >= 2) {
 					batch.draw(bucketImage, bucket.x, bucket.y + 84);
 				}
 				if (buckets >= 3) {
 					batch.draw(bucketImage, bucket.x, bucket.y + 168);
 				}
 				
 				batch.draw(ufoFrame, dropper.x, dropper.y);
 				for(Rectangle raindrop: raindrops) {
 					batch.draw(zebraFrame, raindrop.x, raindrop.y, 32, 25, 64, 51, 1, 1, -60);
 //					batch.draw(zebraFrame, raindrop.x, raindrop.y);
 				}
 //				font.draw(batch, "dropCount: " + Integer.toString(dropCount),10,lineH*2);
 //				font.draw(batch, "neededDrops: " + Long.toString(neededDrops),10,lineH*3);
 //				font.draw(batch, "dropRate: " + Long.toString(dropRate), 10, lineH*4);
 //				font.draw(batch, "dropSpeed: " + Long.toString(dropSpeed), 10, lineH*5);
 
 				font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), maxW/2 - 200, lineH);
 				
 				font.draw(batch, Long.toString(points), 20, lineH);
 //				font.draw(batch, "Lives: " + Long.toString(buckets), maxW/2, lineH);
 				font.draw(batch, "Level: " + Integer.toString(level), maxW-(8*30), lineH);
 
 				batch.end();
 
 				// process user input
 				if(Gdx.input.isTouched()) {
 					touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
 					camera.unproject(touchPos);
 					bucket.x = touchPos.x - 64 / 2;
 				}
 				if(Gdx.input.isKeyPressed(Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
 				if(Gdx.input.isKeyPressed(Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();
 
 				// make sure the bucket stays within the screen bounds
 				if(bucket.x < 0) bucket.x = 0;
 				if(bucket.x > maxW - 64) bucket.x = (maxW - 64);
 
 				bucketBounds.x = bucket.x;
 				
 				// check if we need to create a new raindrop
 				if(TimeUtils.nanoTime() - lastDropTime > dropRate) spawnRaindrop();
 
 				// move the raindrops, remove any that are beneath the bottom edge of
 				// the screen or that hit the bucket. In the later case we play back
 				// a sound effect as well.
 
 
 				if (MathUtils.random(0,10) > 7) {
 					if (dropDir==1) dropDir = 0; else dropDir = 1;
 				}
 
 				if (dropDir==1) {
 					dropper.x -= MathUtils.random(200 * level, 250 * level) * Gdx.graphics.getDeltaTime();
 				} else {
 					dropper.x += MathUtils.random(200 * level, 250 * level) * Gdx.graphics.getDeltaTime();	      
 				}
 
 				// make sure the dropper stays within the screen bounds
 				if(dropper.x < 0) dropper.x = 0;
 				if(dropper.x > maxW - 64) dropper.x = (maxW - 64);
 
 				Iterator<Rectangle> iter = raindrops.iterator();
 				while(iter.hasNext()) {
 					Rectangle raindrop = iter.next();
 					raindrop.y -= dropSpeed * Gdx.graphics.getDeltaTime();
 					if(raindrop.y + 64 < 0) {
						rectPool.free(raindrop);
 						iter.remove();
 						dropLevel();
 					}
 					if(raindrop.overlaps(bucketBounds)) {
 						dropSound.play();
						rectPool.free(raindrop);
 						iter.remove();
 						dropCount++;
 						points = points + ptVal;
 						bonus = bonus + ptVal;
 
 						if (platformInterface.getSignedIn()) {
 							if (points >= 3000) {
 								platformInterface.unlockAchievement("CgkIx7_-lMMSEAIQAw");
 							}
 	
 							if (points >= 10000) {
 								platformInterface.unlockAchievement("CgkIx7_-lMMSEAIQBA");
 							}
 						}
 							
 						if (bonus >= 1000) {
 							if (buckets < 3) {
 								if (platformInterface.getSignedIn()) {
 									platformInterface.unlockAchievement("CgkIx7_-lMMSEAIQBQ");
 								}
 								buckets++;
 								bucketBounds.height = bucketBounds.height + 84;
 								if (bucketBounds.height > 212) {
 									bucketBounds.height = 212;
 								}
 								
 							}
 							bonus = 0;
 						}
 						if (dropCount >= neededDrops) {
 							newLevel();
 						}
 					}
 				}
 			}
 
 		} else {
 			Boolean newPref = false;
 			
 			// GAME OVER
 			stateTime = 0f;  
 			rainMusic.stop();
 			Gdx.gl.glClearColor(0, 0, 0.2f, 1);
 			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 			// tell the camera to update its matrices.
 			camera.update();
 
 			// tell the SpriteBatch to render in the
 			// coordinate system specified by the camera.
 			batch.setProjectionMatrix(camera.combined);
 
 			if (points > highScore) {
 				highScore = points;
 				prefs.putInteger("highScore", highScore);
 				newPref = true;
 				if (platformInterface.getSignedIn()) {
 					platformInterface.submitScore(highScore);
 				}
 			}
 			
 			
 			if (level > highLevel) {
 				highLevel = level;
 				prefs.putInteger("highLevel", highLevel);
 				newPref = true;
 				if (platformInterface.getSignedIn()) {
 					platformInterface.submitLevel(highLevel);
 				}
 			}
 
 			if (platformInterface.getSignedIn()) {
 
 				platformInterface.incrementAchievement("CgkIx7_-lMMSEAIQAg",1);
 	
 				if (points == 1337) {
 					platformInterface.unlockAchievement("CgkIx7_-lMMSEAIQAQ");
 				}
 				
 				if (points >= 6826) {
 					platformInterface.unlockAchievement("CgkIx7_-lMMSEAIQBg");			
 				}
 				
 				if (level >= 15) {
 					platformInterface.unlockAchievement("CgkIx7_-lMMSEAIQBg");			
 				}
 	
 				if (level == 1) {
 					platformInterface.unlockAchievement("CgkIx7_-lMMSEAIQBw");
 				}			
 			}
 			
 			if (newPref) prefs.flush();
 			
 			if(Gdx.input.isTouched()) {
 				touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
 				camera.unproject(touchPos);
 				if (touchPos.y > 150) {
 					bucketBounds.height = 212;
 					dropRate = minDropRate;
 					dropSpeed = minDropSpeed;
 					neededDrops = 10;
 					level = 1;
 					dropDir = 1;
 					ptVal = 1;
 					points = 0;
 					bonus = 0;
 					buckets = 3;	
					raindrops = new Array<Rectangle>();
 				}
 				if (touchSpot.overlaps(highScoreBounds)) {
 					platformInterface.getScores();
 				}
 				if (touchSpot.overlaps(highLevelBounds)) {
 					platformInterface.getLevels();
 				}
 				if (touchSpot.overlaps(achivementsBounds)) {
 					platformInterface.getAchievements();
 				}
 				if (touchSpot.overlaps(loginBounds)) {
 					if (platformInterface.getSignedIn()) {
 						platformInterface.LogOut();
 					} else {
 						platformInterface.Login();
 					}
 				}
 			}
 
 			// begin a new batch and draw the bucket and
 			// all drops
 			batch.begin();
 			font.draw(batch, Long.toString(points), 20, lineH);
 			font.draw(batch, "HS: " + Long.toString(highScore) + " HL: " + Long.toString(highLevel), maxW/2 - 160, lineH);
 			font.draw(batch, "Level: " + Integer.toString(level), maxW-(8*30), lineH);	
 			batch.draw(playControllerImage, achivementsBounds.x, achivementsBounds.y);
 			batch.draw(playControllerImage, highScoreBounds.x, highScoreBounds.y);
 			batch.draw(playControllerImage, loginBounds.x, loginBounds.y);
 			batch.draw(playControllerImage, highLevelBounds.x, highLevelBounds.y);
 			if (newPref) {
 				font.draw(batch,"^^NEW RECORDS^^", loginBounds.x+40, loginBounds.y + loginBounds.height - 115);
 			}
 			font.draw(batch, "ZEBRA DROP!!!", tapItBounds.x + tapItBounds.width, maxH-100);
 			font.draw(batch, "Achievements", achivementsBounds.x + 70, achivementsBounds.y + achivementsBounds.height - 12);
 			font.draw(batch, "High Scores", highScoreBounds.x + 70, highScoreBounds.y + highScoreBounds.height - 12);		
 			font.draw(batch, "High Levels", highLevelBounds.x + 70, highLevelBounds.y + highLevelBounds.height - 12);		
 			if (platformInterface.getSignedIn()) {
 				font.draw(batch, "Logout", loginBounds.x + 70, loginBounds.y + loginBounds.height - 12);		
 			} else {
 				font.draw(batch, "Login", loginBounds.x + 70, loginBounds.y + loginBounds.height - 12);		
 			}			
 			batch.draw(gameOverImage, (maxW/2) - 182, (maxH/2) - 97);
 			batch.end();
 		}
 	}
 
 	@Override
 	public void dispose() {
 		// dispose of all the native resources
 		atlas.dispose();
 		dropSound.dispose();
 		rainMusic.dispose();
 		batch.dispose();
 		font.dispose();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 }
