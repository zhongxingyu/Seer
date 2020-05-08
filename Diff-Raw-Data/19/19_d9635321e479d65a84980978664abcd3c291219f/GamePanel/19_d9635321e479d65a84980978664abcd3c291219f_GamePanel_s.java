 package com.pineapple.valentine;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.pineapple.valentineGen.R;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Path;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffColorFilter;
 import android.media.MediaPlayer;
 import android.os.AsyncTask;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GamePanel extends SurfaceView implements SurfaceHolder.Callback{
 	private final int INVALID_POINTER = -1;
 	public final static String LEVEL = "com.pineapple.GamePanel.LEVEL";
 	public final static String SCORE_KILL = "com.pineapple.GamePanel.SCORE_KILL";
 	public final static String TIME = "com.pineapple.GamePanel.TIME";
 	public final static String HEALTH = "com.pineapple.GamePanel.HEALTH";
 
 	private int leftStickId = INVALID_POINTER;
 	private int rightStickId = INVALID_POINTER;
 	private final int width = 155;
 	private final int height = 100;
 	private double screenX;
 	private double screenY;
 	private final int screenXPadding = 80;
 	private final int screenYPadding = 40;
 	private int finishDelay, finishDelayTime = 20;
 	private boolean finished;
 	private MainThread thread;
 	private Protagonist protagonist;
 	private Ground ground;
 	private double scaleY, scaleX;
 	private Stick leftStick, rightStick;
 	private LevelLoader levelLoader;
 	private ArrayList<Platform> platforms;
 	private ArrayList<Bullet> bullets;
 	private ArrayList<Enemy> enemies;
 	private ArrayList<int[]> trees;
 	private ArrayList<int[]> rocks;
 	private ArrayList<int[]> flowers;
 	private ArrayList<int[]> skeletons;
 	private ArrayList<Butterfly> butterflies;
 	private ArrayList<Bird> birds;
 	private ArrayList<double[]> clouds;
 	private ArrayList<Hint> hints;
 	private int level;
 	private HeatMeter heatMeter;
 	private boolean firing = false;
 	private Paint green = new Paint();
 	private Paint red = new Paint();
 	private Paint frame = new Paint();
 	private Paint stamper = new Paint();
 	private Paint cloaker = new Paint();
 	private Paint enemyPaint = new Paint();
 	private Paint timePaint = new Paint();
 	private Paint textBackground = new Paint();
 	private Paint loadPaint = new Paint();
 	private Paint backgroundPaint;
 	private int time;
 	private double bulletDamage = 0.05;
 	public SoundManager ambientSM, protagonistSM, enemySM, mentorSM, healthSM;
 	private MediaPlayer theme, earthquake;
 	private final int[] renderOrder = new int[]{3, 1, 2};
 	private int cloudSpawnDelay = 1000, cloudCounter = cloudSpawnDelay;
 	private float aimAngle, feetAngle;
 	private int[] scoreKill = new int[]{0, 0, 0};
 	private double playTimeTotal;
 	private double playTimeMin;
 	private double playTimeS;
 	private SharedPreferences settings;
 	private double effectVolume;
 	private boolean viewStatistics;
 	private int i, j, index, id;
 	private boolean criticalHealthFlag = false;
 	private int latestDashTime = -Const.dustDecayTime;
 	private int dashX, dashY;
 	private int latestAction = 0;
 	private int protagonistSoundIndexStart = 4;
 	private boolean sentVariables = false;
 	private int latestDroneSound = 0, latestNinjaSound = 0, latestTankSound = 0, latestBirdSound = 0;
 	private int darknessPercent = 0;
 	private int backgroundColor = Color.rgb(135, 206, 250);
 	private boolean underground = false;
 	private double touchX, touchY;
 	private boolean loading = true;
 	private boolean showHUD = true;
 	private boolean gameStarted = false;
 
 	//Boss variables
 	private boolean startedMentorMonolog, keepInCave;
 	private int monologTimer, mentorDeathTimer;
 	private String[] mentorMessage;
 	private boolean mentorFighting;
 	private int bossState = 0;
 
 	//Special tutorial variables
 	private Protagonist mentor;
 	private int[] checkpoints;
 	private int currentCheckpoint;
 	private ArrayList<ArrayList<String>> mentorHints;
 	private Paint textPaint;
 	private Bird bird;
 	private int timesMentorJumped, pastCheckpointBorder, lastMentorSound, mentorPlayCounter, mentorSentencesToSay;
 	private int tutorialFruitY = 80;
 	private boolean tutorialFruitUp = true;
 	private int tutorialFruitYSpeed = 0;
 	
 
 	//Ground rendering variables 
 	private int numberOfPatches, foliageSize = 2, groundThickness = 6;
 	private double xGap, yGap, gap, groundAngle; 
 	private Paint groundPaint = new Paint();
 	Paint dirtPaint = new Paint();
 	private Path groundPath, dirtPath;;
 	private Matrix renderMatrix = new Matrix();
 
 	//Bitmaps
 	private Bitmap bodyBitmap;
 	private Bitmap footBitmap;
 	private Bitmap eyeMouthBitmap;
 	private Bitmap weaponBitmap;
 	private Bitmap handsBitmap;
 	private Bitmap pupilBitmap;
 	private Bitmap stickBitmap;
 	private Bitmap bulletBitmap;
 	private Bitmap birdBitmap;
 	private Bitmap sunBitmap;
 	private Bitmap mentorBodyBitmap;
 	private Bitmap eyeBeardBitmap;
 	private Bitmap[] rockBitmap;
 	private Bitmap[][] treeBitmaps;
 	private Bitmap[] cloudBitmaps;
 	private Bitmap[] flagBitmaps;
 	private Bitmap flowerBitmap;
 	private Bitmap dustBitmap;
 	private Bitmap skeletonBitmap;
 	private Bitmap fruitBitmap;
 	private Bitmap[] butterflyBitmaps;
 	private Bitmap signBitmap;
 
 	private Bitmap[] enemyBodyBitmap = new Bitmap[3];
 	private Bitmap[] enemyEyeMouthBitmap = new Bitmap[3];
 	private Bitmap[] enemyLeftArmBitmap = new Bitmap[3];
 	private Bitmap[] enemyRightArmBitmap = new Bitmap[3];
 	private Bitmap[] enemyFootBitmap = new Bitmap[3];
 	private Bitmap[] enemyPupilBitmap = new Bitmap[3];
 	private Bitmap enemyArmorBitmap, enemyNinjaBitmap;
 
 	private Bitmap bodyBitmapFlipped;
 	private Bitmap footBitmapFlipped;
 	private Bitmap eyeMouthBitmapFlipped;
 	private Bitmap weaponBitmapFlipped;
 	private Bitmap handsBitmapFlipped;
 	private Bitmap pupilBitmapFlipped;
 	private Bitmap mentorBodyBitmapFlipped;
 	private Bitmap eyeBeardBitmapFlipped;
 
 	private Bitmap[] enemyBodyBitmapFlipped = new Bitmap[3];
 	private Bitmap[] enemyEyeMouthBitmapFlipped = new Bitmap[3];
 	private Bitmap[] enemyFootBitmapFlipped = new Bitmap[3];
 	private Bitmap enemyArmorBitmapFlipped, enemyNinjaBitmapFlipped;
 
 	//CONSTRUCTOR
 	public GamePanel(Context context, int level){
 		super(context);
 		getHolder().addCallback(this);
 		setFocusable(true);
 		this.level = level;
 		settings = context.getSharedPreferences("gameSettings", Context.MODE_PRIVATE);
 		effectVolume = settings.getFloat("soundVolume", 1);
 		viewStatistics = (settings.getBoolean("scoring", true) && level != 12);
 
 		//Create game components
 		levelLoader = new LevelLoader(level);
 		heatMeter = new HeatMeter(0.01);
 		bullets = new ArrayList<Bullet>();
 		ground = new Ground(levelLoader.getLevelX(level), levelLoader.getLevelY(level));
 		protagonist = new Protagonist(levelLoader.getStartX(), levelLoader.getStartY(), this);
 		leftStick = new Stick(Stick.LEFT);
 		rightStick = new Stick(Stick.RIGHT);
 		thread = new MainThread(this.getHolder(), this);
 		ambientSM = new SoundManager(getContext(), 10);
 		healthSM = new SoundManager(getContext(), 1);
 		protagonistSM = new SoundManager(getContext(), 1);
 		enemySM = new SoundManager(getContext(), 10);
 
 		loadPlatforms();
 		loadEnemies();
 		loadTrees();
 		loadRocks();
 		loadFlowers();
 		loadSkeletons();
 		loadButterflies();
 		loadBirds();
 		loadHints();
 		clouds = new ArrayList<double[]>();
 
 		loadPaint.setColor(Color.WHITE);
 
 		green.setColor(Color.GREEN);
 		red.setColor(Color.RED);
 		groundPaint.setColor(Color.rgb(10, 250, 10));
 		dirtPaint.setColor(Color.rgb(87, 59, 12));
 
 		textBackground.setARGB(120, 40, 40, 40);
 
 		timePaint.setColor(Color.WHITE);
 		timePaint.setTextAlign(Align.CENTER);
 		timePaint.setDither(true);
 		timePaint.setAntiAlias(true);
 
 		setKeepScreenOn(true);
 
 		if(level == 0){
 			mentor = new Protagonist(10, 70, this, true);
 			checkpoints = levelLoader.getCheckpoints();
 			mentorHints = new ArrayList<ArrayList<String>>();
 			pastCheckpointBorder = 10;
 			String[] rawHints = {
 					"Hi Valentine, welcome to the tutorial! Let's get right into the action! You can move around by using your left stick! Why don't you give it a go?",
 					"Good job! Believe it or not, but moving is essential to make it in this world. Come along!",
 					"You can jump by pressing up on the left stick!",
 					"Wow! You seem to have some strong legs there young one! Oh my, what a steep slope! We can slide down this for sure!",
 					"Woohoo!",
 					"That was fun! But let's get back to business!",
 					"Even though we are a peaceful people with no enemies what so ever, it is always good to carry some protection, like the gun in your hand for example. Fire off a couple of shots with your right stick!",
 					"Good, but shooting a gun isn't that exciting if you're not aiming at something, am I right? Let's find something to shoot!",
 					"Before we go on, do you mind getting that fruit down for me? You can use your dash to shake the tree. Jump, and when you're high enough press down on your left stick!",
 					"Thank you! Oh, looks like you woke up a bird! They always eat my crops and sing early in the morning! See if you can scare him with your gun!",
 					"..... Well, now he won't wake me up early at least! We better go before anyone sees us. See if you can get up on this platform!",
 					"Wow, you can see so much from up here! Actually... I see something strange over there! What is that?",
 					"Good heavens, what an ugly creature! I know we are a friendly people but you better put him out of his misery! He doesn't look like a nice monster anyways...", 
 					"May he rest in peace! Now where were we? Oh right, there's one final thing you need to know! That weapon of yours, it gets easily overheated. Watch out for that if you feel like firing for a long time! Try it!",
 					"Well, that should be everything you need to know! And Valentine, you... What is that noise? Run and look, will you?",
 					"I don't know where these creatures came from but it is up to you investigate it! Go past that flag to continue your mission! Good luck!",
 					"I don't know where these creatures came from but it is up to you investigate it! Go past that flag to continue your mission! Good luck!"
 			};
 			//Split the hints up into rows and add them to the final hint list
 			int lettersPerRow = 50;
 			for(i = 0; i < rawHints.length; i++){
 				mentorHints.add(new ArrayList<String>());
 				while(rawHints[i].length() > lettersPerRow){
 					String row = rawHints[i].substring(0, lettersPerRow-1);
 					int spaceIndex = row.lastIndexOf(" ");
 					mentorHints.get(i).add(rawHints[i].substring(0, spaceIndex));
 					rawHints[i] = rawHints[i].substring(spaceIndex+1, rawHints[i].length());
 				}
 				mentorHints.get(i).add(rawHints[i]);
 			}
 
 
 
 
 			mentorSM = new SoundManager(getContext(), 1);
 			//Load all the things the mentor can say
 			mentorSM.addSound(0, R.raw.mentor_sound_1);
 			mentorSM.addSound(1, R.raw.mentor_sound_2);
 			mentorSM.addSound(2, R.raw.mentor_sound_3);
 			mentorSM.addSound(3, R.raw.mentor_sound_4);
 			mentorSM.addSound(4, R.raw.mentor_sound_5);
 			mentorSM.addSound(5, R.raw.mentor_sound_woohoo);
 			lastMentorSound = -1; //Stops him from repeating the same line
 			mentorSentencesToSay = 3;
 		}
 		if(level == 11){
 			mentor = new Protagonist(550, -20, this, true);
 
 			mentorSM = new SoundManager(getContext(), 1);
 			//Load all the things the mentor can say
 			mentorSM.addSound(0, R.raw.mentor_sound_1);
 			mentorSM.addSound(1, R.raw.mentor_sound_2);
 			mentorSM.addSound(2, R.raw.mentor_sound_3);
 			mentorSM.addSound(3, R.raw.mentor_sound_4);
 			mentorSM.addSound(4, R.raw.mentor_sound_5);
 			mentorSM.addSound(5, R.raw.mentor_sound_woohoo);
 			lastMentorSound = -1; //Stops him from repeating the same line
 
 			backgroundPaint = new Paint();
 			backgroundPaint.setColor(Color.rgb(150,  150,  150));
 
 			darknessPercent = 50;
 		}
 		//Paint
 		textPaint = new Paint();
 		textPaint.setColor(Color.WHITE);
 		textPaint.setDither(true);
 		textPaint.setAntiAlias(true);
 
 		stamper.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN));
 
 		//Start the thread
 		if (thread.getState()==Thread.State.TERMINATED) { 
 			thread = new MainThread(getHolder(),this);
 		}
 		thread.setRunning(true);
 		thread.start();
 	}
 
 	//Load the platforms from LevelLoader and add them to the platforms list 
 	public void loadPlatforms(){
 		platforms = new ArrayList<Platform>();
 		for(i = 0; i < levelLoader.getNumberOfPlatforms(); i++){
 			platforms.add(new Platform(levelLoader.getPlatformUpperX(i), levelLoader.getPlatformUpperY(i), levelLoader.getPlatformLowerX(i), levelLoader.getPlatformLowerY(i)));
 		}
 	}
 
 	//Load the enemies from LevelLoader and add them to the enemy list 
 	public void loadEnemies(){
 		enemies = new ArrayList<Enemy>();
 		for(i = 0; i < levelLoader.getNumberOfEnemies(); i++){
 			int[] enemyData = levelLoader.getEnemyData(i);
 			if (enemyData.length == 4)
 				enemies.add(new Enemy(enemyData[0], enemyData[1], enemyData[2], enemyData[3], this));
 			else
 				enemies.add(new Enemy(enemyData[0], enemyData[1], enemyData[2], enemyData[3], enemyData[4], this));
 		}
 	}
 
 	//Load the trees from LevelLoader and add them to the tree list 
 	public void loadTrees(){
 		trees = levelLoader.getTrees();
 	}
 
 	//Load the rocks from LevelLoader and add them to the rock list 
 	public void loadRocks(){
 		rocks = levelLoader.getRocks();
 	}
 
 	//Load the flowers from LevelLoader and add them to the flower list 
 	public void loadFlowers(){
 		flowers = levelLoader.getFlowers();
 	}
 
 	//Load the skeletons from LevelLoader and add them to the skeleton list 
 	public void loadSkeletons(){
 		skeletons = levelLoader.getSkeletons();
 	}
 
 	//Load the butterflies from LevelLoader and add them to the butterfly list 
 	public void loadButterflies(){
 		butterflies = levelLoader.getButterflies();
 	}
 
 	//Load the birds from LevelLoader and add them to the bird list 
 	public void loadBirds(){
 		birds = levelLoader.getBirds();
 	}
 
 	//Load the hints from LevelLoader and add them to the hint list 
 	public void loadHints(){
 		hints = levelLoader.getHints();
 	}
 	//Load the sounds
 	public void loadSounds(){
 		ambientSM.addSound(0, R.raw.fire_sound);
 		ambientSM.addSound(1, R.raw.overheat);
 		ambientSM.addSound(2, R.raw.bird);
 		ambientSM.addSound(3, R.raw.dash_finish);
 		healthSM.addSound(1, R.raw.low_health);
 		protagonistSM.addSound(0, R.raw.protagonist_jump);
 		protagonistSM.addSound(1, R.raw.dash_start);
 		protagonistSM.addSound(2, R.raw.protagonist_hurt_1);
 		protagonistSM.addSound(3, R.raw.protagonist_hurt_2);
 		protagonistSM.addSound(protagonistSoundIndexStart+0, R.raw.protagonist_1);
 		protagonistSM.addSound(protagonistSoundIndexStart+1, R.raw.protagonist_2);
 		protagonistSM.addSound(protagonistSoundIndexStart+2, R.raw.protagonist_3);
 		protagonistSM.addSound(protagonistSoundIndexStart+3, R.raw.protagonist_4);
 		protagonistSM.addSound(protagonistSoundIndexStart+4, R.raw.protagonist_5);
 		enemySM.addSound(0, R.raw.drone_entry);
 		enemySM.addSound(1, R.raw.drone_hurt);
 		enemySM.addSound(2, R.raw.ninja_entry);
 		enemySM.addSound(3, R.raw.ninja_hurt);
 		enemySM.addSound(4, R.raw.tank_entry);
 		enemySM.addSound(5, R.raw.tank_hurt);
 	}
 
 	//Play sound
 	public void playSound(SoundManager manager, int index){
 		manager.playSound(index, effectVolume);
 	}
 
 	//Play the theme in loop
 	public void playTheme(){
 		if(theme == null){ //If theme is null
 			loadTheme();
 			theme.start();
 		}
 		else if(!theme.isPlaying()){ //If theme isn't already playing, reset before loading
 			theme.reset();
 			loadTheme();
 			theme.start();
 		}
 	}
 
 	public void loadTheme(){
 		theme = MediaPlayer.create(getContext(), R.raw.short_instrumental);
 		try {
 			theme.prepare();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		theme.setLooping(true);
 		theme.setVolume(settings.getFloat("musicVolume", 1), settings.getFloat("musicVolume", 1));
 	}
 
 	//Method that gets called every frame to update the games state
 	public void update(){
 		if(!loading){
 			protagonist.checkSlope(ground, platforms);
 			handleSticks();
 			moveProtagonist();
 			moveEnemies();
 			moveBullets();
 			moveScreen();
 			handleHeatMeter();
 			handleBulletEnemyCollisions();
 			handleProtagonistEnemyCollisions(ground);
 			handleEnemyEnemyCollisions();
 			moveAndSpawnClouds();
 			handleProtagonistMumbling();
 			checkFinish();
 			if (protagonist.checkDead()){
 				// Go to a new activity (game over)
 				Context context = getContext();
 				Intent intent = new Intent(context, GameOverActivity.class);
 				intent.putExtra(GamePanel.LEVEL, level);
 				context.startActivity(intent);
 				((Activity)context).finish();
 				if(viewStatistics){
 					playTimeTotal = MainThread.updateInterval*time/1000; //time in seconds
 					playTimeMin = (int)(playTimeTotal/60);
 					playTimeS = playTimeTotal - playTimeMin*60;
 					viewStatistics = false;
 				}
 			}
 			if(gameStarted)
 				this.time++; //count number of frames passed
 			for(int i = 0; i < butterflies.size(); i++){ //update butterfly
 				butterflies.get(i).update();
 			}
 			for(int i = 0; i < birds.size(); i++){ //update bird
 				birds.get(i).update();
 				birds.get(i).collide(bullets);
 				if(time - latestBirdSound > 50 && Math.abs(protagonist.getXPos() - birds.get(i).getX()) < width/2 && Math.abs(protagonist.getYPos() - birds.get(i).getY()) < height/2){
 					ambientSM.playSound(2, effectVolume);
 					latestBirdSound = time;
 				}
 				if(!birds.get(i).isAlive() && Math.abs(birds.get(i).getStartY() - birds.get(i).getY()) > height){
 					birds.remove(i);
 					ambientSM.stop(2);
 					i--;
 				}
 			}
 			//Tutorial
 			if(level == 0){
 				moveMentor();
 				handleCheckpoints();
 				if(bird != null){
 					bird.update();
 				}
 				if(!tutorialFruitUp && tutorialFruitY < 200){
 					tutorialFruitYSpeed++;
 					tutorialFruitY += tutorialFruitYSpeed;
 				}
 				if(currentCheckpoint == 8){
 					screenY += (50-screenY)/20;
 				}
 				if(currentCheckpoint == 12){
 					screenY += (183-screenY)/20;
 					screenX += (1207-screenX)/20;
 				}
 				if(protagonist.getXPos() > checkpoints[currentCheckpoint] + pastCheckpointBorder && currentCheckpoint <= 13){
 					protagonist.setXPos(checkpoints[currentCheckpoint] + pastCheckpointBorder);
 				}
 			}
 
 			if(level == 10){
 				if(protagonist.getYPos() > 60 && protagonist.getYPos() < 600 && protagonist.getXPos() < 500){
 					darknessPercent = (int)(50*(protagonist.getYPos()-60)/540.);
 				}
 				if(!underground){
 					if(protagonist.getYPos() > 500){
 						underground = true;
 						backgroundColor = Color.rgb(150, 150, 150);
 					}
 				}
 			}
 			if(level == 11){//Boss
 				bossScripts();
 
 			}
 			if(finished){
 				if(level == 10){
 					darknessPercent = Math.max((int)(100.*(finishDelayTime-finishDelay)/finishDelayTime), 50);
 				} else {
 					darknessPercent = (int)(100.*(finishDelayTime-finishDelay)/finishDelayTime);
 				}
 			}
 		}
 	}
 
 	//If left stick pointed, protagonist is moving. If not protagonist slow down
 	public void handleSticks(){
 		if(showHUD){
 			if(leftStick.isPointed()) {
 				protagonist.handleLeftStick(leftStick.getAngle(), 0.4);
 			} else if (Math.abs(protagonist.getXVel()) > 0){
 				protagonist.slowDown();
 				protagonist.setStepCount(0);
 			}
 
 			if(rightStick.isPointed()){
 				//Aim
 				protagonist.aim(rightStick.getAngle());
 				//Fire
 				if(level > 0 || currentCheckpoint > 6){
 					if(!heatMeter.isCoolingDown()){
 						bullets.add(new Bullet(protagonist.getXPos()+protagonist.getWidth()/2*Math.cos(protagonist.getAim()/180*Math.PI), protagonist.getYPos()-protagonist.getWidth()/2*Math.sin(protagonist.getAim()/180*Math.PI), protagonist.getAim(), 10, protagonist));
 						firing = true;
 						playSound(ambientSM, 0);
 						latestAction = time;
 					}
 				}
 			}
 		}
 	}
 
 	public void bossScripts(){
 		if(protagonist.getXPos() < 2108){
 			backgroundColor = Color.rgb(150,  150,  150);
 			underground = true;
 		} else {
 			backgroundColor = Color.rgb(135, 206, 250);
 			underground = false;
 		}
 		if(keepInCave){
 			if(protagonist.getXPos() < 200){
 				protagonist.setXPos(200);
 			}
 			if(protagonist.getXPos() > 1300){
 				protagonist.setXPos(1300);
 			}
 			if(mentor.getXPos() < 200 && mentor.getXVel() < 0 || mentor.getXPos() > 1300 && mentor.getXVel() > 0){
 				mentor.setXVel(-mentor.getXVel()/2);
 			}
 		}
 		if(protagonist.getXPos() > 200 && !startedMentorMonolog){
 			startedMentorMonolog = true;
 			leftStick.setPointed(false);
 			monologTimer = 0;
 			showHUD = false;
 			leftStick.release();
 			rightStick.release();
 			keepInCave = true;
 			
 		}
 		if(startedMentorMonolog && monologTimer < 240){
 			monologTimer++;
 			if(protagonist.getXPos() < 500){
 				protagonist.accelerate(1);
 				protagonist.step(1);
 			} else {
 				protagonist.slowDown();
 				protagonist.setStepCount(0);
 			}
 			switch(monologTimer){
 			case 15:
 				mentorMessage = new String[]{
 						"So, you made it here at last. When I left you at home I", 
 				"didn't think that you would make it this far!"};
 				mentorSentencesToSay = 2;
 				break;
 			case 90:
 				mentorMessage = new String[]{
 						"Yes, Valentine, it was me all along.", 
 				"I created these creatures to gain world dominance!"};
 				mentorSentencesToSay = 2;
 				rocks.add(new int[]{170, 0, 60, 1});
 				rocks.add(new int[]{1330, 0, 60, 1});
 				break;
 			case 165:
 				mentorMessage = new String[]{
 						"Now it seems you are the only obstacle in my path!",
 				"Let's end this..."};
 				mentorSentencesToSay = 2;
 				break;
 			}
 			if(monologTimer > 15 && monologTimer < 90){ //Dramatic camera movement
 				screenY -= (screenY + 90)/40.;
 			}
 
 		}
 		if(bossState == 4){
 			mentorDeathTimer++;
 			if(protagonist.getXPos() > 1600 && protagonist.getXPos() < 2000){
 				darknessPercent = (int)(50*((2000-protagonist.getXPos()))/400);
 			}
 			if(mentorDeathTimer < 25){
 				mentor.slowDown();
 				if(Math.abs(protagonist.getXPos()-mentor.getXPos()) > 50){
 					protagonist.accelerate(Math.signum(mentor.getXPos()-protagonist.getXPos()));
 					protagonist.step(1);
 				} else if(Math.abs(protagonist.getXPos()-mentor.getXPos()) < 20){
 					protagonist.accelerate(-Math.signum(mentor.getXPos()-protagonist.getXPos()));
 					protagonist.step(1);
 				} else {
 					protagonist.slowDown();
 					protagonist.setStepCount(0);
 				}
 			}
 			if(mentorDeathTimer <= 175){
 				protagonist.slowDown();
 				protagonist.faceDirection(mentor.getXPos()-protagonist.getXPos());
 				mentor.faceDirection(protagonist.getXPos()-mentor.getXPos());
 				protagonist.setAim((mentor.getXPos()-protagonist.getXPos())>0?0:180);
 				switch(mentorDeathTimer){
 				case 25:
 					mentorMessage = new String[]{"I can't believe this. How did you become so powerful?"};
 					mentorSentencesToSay = 2;
 					leftStick.release();
 					rightStick.release();
 					break;
 				case 75:
 					mentorMessage = new String[]{"I guess my desire for world dominance",  "has clouded my judgement..."};
 					mentorSentencesToSay = 2;
 					break;
 				case 125:
 					mentorMessage = new String[]{"I should never have underestimated you, Valentine.", "Good bye!"};
 					mentorSentencesToSay = 1;
 					break;
 				case 175:
 					protagonist.jump();
 					break;
 				}
 			} else if(mentorDeathTimer <= 187){
 				protagonist.accelerate(Math.signum(mentor.getXPos()-protagonist.getXPos()));
 			} else if(mentorDeathTimer == 188){
 				protagonist.down(ground, platforms);
 			} else if(mentorDeathTimer == 190){
 				mentorSM.playSound(5, effectVolume);
 				mentor.setYVel(mentor.getYVel()-20);
 				keepInCave = false;
 				earthquake = MediaPlayer.create(getContext(), R.raw.earthquake);
 				earthquake.setLooping(true);
 				earthquake.setVolume((float)effectVolume, (float)effectVolume);
 				earthquake.start();
 				monologTimer++;
 			} else if(mentorDeathTimer == 250){
 				mentor.setXPos(-100);
 				mentor.setYPos(-200);
 				showHUD = true;
 			} else if(mentorDeathTimer > 192){
 				screenY += Math.random() * 4 - 2;
 				for(i = 2; i <= 15; i++){
 					ground.setY(i, (int)(6*Math.random() - 3));//Earthquake
 				}
 			}
 
 		}
 		if(monologTimer == 239){
 			showHUD = true;
 			mentorMessage = null;
 			mentorFighting = true;
 		}
 		if(monologTimer == 240){//Fighting
 			screenY = -90;
 			if(mentorFighting){
 				if(mentor.isTouchingGround()){
 					mentor.accelerate((0.3-0.03*bossState)*Math.signum(protagonist.getXPos()-mentor.getXPos()));
 					mentor.step(1);
 				}
 				if(mentor.collide(protagonist)){
 					if(!protagonist.isInvincible() && !protagonist.isDashBonus()){
 						protagonist.reduceHealth(0.1);
 						protagonist.setInvincible(true);
 						protagonist.setXVel(0);
 						protagonist.setYVel(-5);
 						protagonist.setTouchingGround(false);
 						if(protagonist.getHealth() < Const.criticalHealth){
 							if(!criticalHealthFlag){
 								healthSM.playLoopedSound(1, effectVolume);
 								criticalHealthFlag = true;
 							}
 						}
 						latestAction = time;
 						playSound(protagonistSM, 2 + (int)(Math.random()*2));
 					}
 				}
 				if(mentor.getHealth() <= (0.75 - bossState * 0.25) && mentorFighting){
 					mentorFighting = false;
 					bossState++;
 					//Spawn enemies
 					switch(bossState){
 
 
 					case 1:
 						mentorMessage = new String[]{"So... You want to do this the hard way?", "Maybe my creatures can change your mind?"};
 						mentorSentencesToSay = 2;
 						enemies.get(0).spawn();
 						enemies.get(1).spawn();
 						enemySM.playSound(0, effectVolume);
 						mentor.setYVel(-10);
 						break;
 
 					case 2: 
 						mentorMessage = new String[]{"Hmpf, stop struggling! Minions, attack!"};
 						mentorSentencesToSay = 2;
 						enemies.get(0).spawn();
 						enemies.get(1).spawn();
 						enemies.get(2).spawn();
 						enemies.get(3).spawn();
 						enemySM.playSound(0, effectVolume);
 						enemySM.playSound(2, effectVolume);
 						mentor.setYVel(-10);
 						break;
 					case 3: 
 						mentorMessage = new String[]{"I've had enough of this! Release the monsters!"};
 						mentorSentencesToSay = 2;
 						enemies.get(0).spawn();
 						enemies.get(1).spawn();
 						enemies.get(2).spawn();
 						enemies.get(3).spawn();
 						enemies.get(4).spawn();
 						enemies.get(5).spawn();
 						enemySM.playSound(0, effectVolume);
 						enemySM.playSound(2, effectVolume);
 						enemySM.playSound(4, effectVolume);
 						mentor.setYVel(-10);
 						break;
 					case 4: //Dead
 						showHUD = false;
 						mentorDeathTimer = 0;
 						break;
 					}
 				}
 				handleMentorBulletCollisions();
 			} else {
 				mentor.slowDown();
 				mentor.setStepCount(0);
 				//Wave vanquished
 				if(bossState == 1 && enemies.size() == 10 || bossState == 2 && enemies.size() == 6 || bossState == 3 && enemies.size() == 0){
 					mentorFighting = true;
 					mentor.setMaxSpeed(mentor.getMaxSpeed()+1);
 					switch(bossState){
 					case 1:
 						mentorMessage = new String[]{"Stupid creatures! En garde!"};
 						mentorSentencesToSay = 2;
 						break;
 					case 2:
 						mentorMessage = new String[]{"I should have trained them better!"};
 						mentorSentencesToSay = 2;
 						break;
 					case 3:
 						mentorMessage = new String[]{"Okay Valentine, looks like I will have to finish this myself!"};
 						mentorSentencesToSay = 2;
 						break;
 					}
 
 				}
 			}
 		}
 
 		mentor.inAir(platforms, ground);
 		mentor.checkGround(ground);
 		mentor.faceDirection(protagonist.getXPos()-mentor.getXPos());
 		mentor.setAim((int)(180/Math.PI*Math.atan2(protagonist.getYPos() - mentor.getYPos(), protagonist.getXPos() - mentor.getXPos())));
 		if(mentor.getYVel() > 0 && !mentorFighting)
 			mentor.checkPlatform(platforms);
 		if(mentorDeathTimer < 175 || mentorDeathTimer > 188){
 			mentor.gravity();
 			mentor.move();
 		}
 		handleMentorTalking();
 		mentor.breathe();
 
 	}
 
 	//Move protagonist
 	public void moveProtagonist(){
 		protagonist.gravity();
 		protagonist.move();
 		protagonist.contain(levelLoader.getFinishX());
 		protagonist.faceDirection(leftStick, rightStick);
 		protagonist.breathe();
 		protagonist.invincibility();
 		protagonist.dashing(ground, platforms);
 		protagonist.checkGround(ground);
 		protagonist.checkPlatform(platforms);
 		protagonist.inAir(platforms, ground);
 	}
 
 	//Move all the enemies and check for obstacles etc
 	public void moveEnemies(){
 		for(i = 0; i < enemies.size(); i++){
 			Enemy enemy = enemies.get(i);
 			if(enemy.hasSpawned()){
 				enemy.gravity();
 				enemy.accelerate(protagonist);
 				enemy.checkSlope(ground, platforms);
 				enemy.move();
 				enemy.contain(levelLoader.getFinishX());
 				enemy.checkGround(ground);
 				enemy.checkPlatform(platforms);
 				enemy.checkAirborne(ground, platforms);
 				enemy.waveArms();
 				enemy.lookAt(protagonist);
 			} else {
 				if(protagonist.getXPos() > enemy.getSpawnX() && protagonist.getYPos() < enemy.getSpawnY()){
 					enemy.spawn();
 					playSound(enemySM, (enemy.getType()-1)*2);
 				}
 			}
 		}
 	}
 
 	public void moveBullets(){
 		for(i = 0; i < bullets.size(); i++){
 			Bullet b = bullets.get(i);
 			b.gravity(1);
 			b.move();
 			int leftBound = ground.getX(0);
 			int rightBound = ground.getX(ground.getLength()-1);
 			if(b.getXPos() < leftBound || b.getXPos() > rightBound){
 				bullets.remove(i);
 				i--;
 			} else if(b.checkObstacles(ground, platforms)){
 				bullets.remove(i);
 				i--;
 			}
 
 		}
 	}
 
 	//Moves the screen if the protagonist is close to the edge of the screen
 	public void moveScreen(){
 		if(protagonist.getXPos() - screenX > width - screenXPadding && protagonist.getXVel() > 0){
 			screenX -= (screenX - (protagonist.getXPos() - width + screenXPadding))/3.;
 		} else if(protagonist.getXPos() - screenX < screenXPadding && protagonist.getXVel() < 0){
 			screenX -= (screenX - (protagonist.getXPos() - screenXPadding))/3.;
 		}
 		if(protagonist.getYPos() - screenY > height - screenYPadding){
 			screenY = protagonist.getYPos() - height + screenYPadding;
 		} else if(protagonist.getYPos() - screenY < screenYPadding){
 			screenY = protagonist.getYPos() - screenYPadding;
 		}
 		if(time-latestDashTime < Const.dustDecayTime){
 			screenY += 4*Math.exp(-(time-latestDashTime)/(double)Const.dustDecayTime)*Math.cos((time-latestDashTime)*Math.PI);
 		}
 
 	}
 
 	//Handles the HeatMeter 
 	public void handleHeatMeter(){
 		if(heatMeter.isCoolingDown()){//If the weapon is overheated
 			heatMeter.coolDown();     //Cool down                      (Not able to fire)
 		} else if(firing){			  //Else if the protagonist has fired
 			if(heatMeter.addHeat(0.01)){//Heat up the weapon
 				playSound(ambientSM, 1); //If overheated play sound
 			}
 			firing = false;
 		} else {                      //Otherwise cool down the weapon (Still able to fire)
 			heatMeter.coolDown();
 		}
 	}
 
 	//Check collision between enemies and bullets
 	public void handleBulletEnemyCollisions(){
 		for(i = 0; i < enemies.size(); i++){
 			enemies.get(i).setHitThisFrame(false);
 		}
 		for(i = 0; i < bullets.size(); i++){//All bullets
 			Bullet bullet = bullets.get(i);
 			for(int j = 0; j < enemies.size(); j++){//All enemies
 				if(bullet.collideEnemy(enemies.get(j)) && enemies.get(j).hasSpawned()){//If collision detected
 					bullets.remove(i);//Remove the bullet from the game
 					i--;
 
 					enemies.get(j).takeDamage(bulletDamage*enemies.get(j).getDamageGrade()); //Reduce the enemies' health SET A CONSTANT OR SOMETHING HERE INSTEAD OF 0.05
 					enemies.get(j).setHitThisFrame(true);
 					switch(enemies.get(j).getType()){
 					case 1:
 						if(time - latestDroneSound + 2 - 4*Math.random() > 10){
 							playSound(enemySM, (enemies.get(j).getType()-1)*2+1);
 							latestDroneSound = time;
 						}
 						break;
 					case 2:
 						if(time - latestNinjaSound + 2 - 4*Math.random() > 10){
 							playSound(enemySM, (enemies.get(j).getType()-1)*2+1);
 							latestNinjaSound = time;
 						}
 						break;
 					case 3:
 						if(time - latestTankSound + 2 - 4*Math.random()> 10){
 							playSound(enemySM, (enemies.get(j).getType()-1)*2+1);
 							latestTankSound = time;
 						}
 						break;
 					}
 					if(enemies.get(j).getHealth() <= 0){//If the enemy is dead
 						scoreKill[enemies.get(j).getType()-1]++;
 						enemies.remove(j);
 						j--;
 					}
 
 
 					break;
 				}
 			}
 		}
 	}
 
 	//Check collision between enemies and protagonist
 	public void handleProtagonistEnemyCollisions(Ground ground){
 		for(i = 0; i < enemies.size(); i++){
 			//Protagonist collide with enemy
 			if(protagonist.collide(enemies.get(i)) && !protagonist.isInvincible() && !protagonist.isDashBonus() && enemies.get(i).hasSpawned()){
 				protagonist.setInvincible(true);
 				protagonist.setXVel(0);
 				protagonist.setYVel(-5);
 				protagonist.setTouchingGround(false);
 				protagonist.reduceHealth(enemies.get(i).getDamageDealt()); 
 				if(protagonist.getHealth() < Const.criticalHealth){
 					if(!criticalHealthFlag){
 						healthSM.playLoopedSound(1, effectVolume);
 						criticalHealthFlag = true;
 					}
 				}
 				latestAction = time;
 				playSound(protagonistSM, 2 + (int)(Math.random()*2));
 			}
 		}
 	}
 
 	public void handleEnemyEnemyCollisions(){
 		for(i = 0; i < enemies.size()-1; i++){
 			if(Math.abs(enemies.get(i).getXPos() - protagonist.getXPos()) < width/2){
 				for(j = i+1; j < enemies.size(); j++){
 					enemies.get(i).collide(enemies.get(j));
 				}
 			}
 		}
 	}
 
 	public void handleMentorBulletCollisions(){
 		for(i = 0; i < bullets.size(); i++){
 			if(mentor.collide(bullets.get(i))){
 				bullets.remove(i);
 				i--;
 				mentor.reduceHealth(0.002);
 			}
 		}
 	}
 
 	public void sendEnemiesFlying(){
 		for(i = 0; i < enemies.size(); i++){
 			if(Math.abs(protagonist.getXPos() - enemies.get(i).getXPos()) < protagonist.getWidth()*5 && Math.abs(protagonist.getYPos() - enemies.get(i).getYPos()) < protagonist.getHeight()*5 && protagonist.isDashBonus() && enemies.get(i).dashable(ground, protagonist, platforms) && enemies.get(i).hasSpawned()){
 				enemies.get(i).takeDashDamage(protagonist);
 				enemies.get(i).setHitThisFrame(true);
 				if(enemies.get(i).getHealth() <= 0){//If the enemy is dead
 					scoreKill[enemies.get(i).getType()-1]++;
 					enemies.remove(i);
 					i--;
 				}
 
 			}
 		}
 		
 		if(mentor != null && mentor.isTouchingGround() && mentor.getPlatformNumber() == protagonist.getPlatformNumber() && Math.abs(mentor.getXPos() - protagonist.getXPos()) < 30 ){
 			mentor.inAir(platforms, ground);
 			mentor.setYVel(mentor.getYVel()-5);
 			if(level == 11){
 				mentor.reduceHealth(0.03);
 			}
 		}
 		dashX = (int)protagonist.getXPos();
 		dashY = (int)(protagonist.getYPos() - protagonist.getHeight()/4);
 		latestDashTime = time;
 		latestAction = time;
 		playSound(ambientSM, 3);
 	}
 
 	//Check if protagonist passes finish line
 	public void checkFinish(){
 		if(protagonist.getXPos() >  levelLoader.getFinishX() - Const.finishFlagWidth/2 && protagonist.getPlatformNumber() == -1 && protagonist.isTouchingGround() && !finished){
 			finished = true;
 			showHUD = false;
 			finishDelay = finishDelayTime;
 		} else if(protagonist.getXPos() >  levelLoader.getFinishX() - Const.finishFlagWidth/2 && !finished){
 			protagonist.setXPos(levelLoader.getFinishX() - Const.finishFlagWidth/2);
 		}
 		if(finished){
 			protagonist.step(1);
 			protagonist.accelerate(1);
 			protagonist.setInvincible(true);
 			if(finishDelay > 0){
 				finishDelay--;
 			} else if(!sentVariables){
 				// Go to a new activity
 				sentVariables = true;
 				if(viewStatistics){
 					playTimeTotal = MainThread.updateInterval*time/1000;
 					playTimeMin = (int)(playTimeTotal/60);
 					playTimeS = playTimeTotal - playTimeMin*60;
 					viewStatistics = false;
 				}
 				Context context = getContext();
 				Intent intent = new Intent(context, LevelCompleteActivity.class);
 				intent.putExtra(LEVEL, level);
 				intent.putExtra(SCORE_KILL, scoreKill);
 				intent.putExtra(TIME, time);
 				intent.putExtra(HEALTH, (int)(protagonist.getHealth()*100));
 				context.startActivity(intent);
 				((Activity)context).finish();
 			}
 		}
 	}
 
 	//Spawn and move clouds
 	public void moveAndSpawnClouds(){
 		for(i = 0; i < clouds.size(); i++){
 			clouds.get(i)[0] -= 0.1;
 			if(clouds.get(i)[0] < -Const.maxCloudWidth/2){
 				clouds.remove(i);
 				i--;
 			}
 		}
 
 		cloudCounter++;
 		if(cloudCounter >= cloudSpawnDelay){
 			if(Math.random() < 0.01){
 				clouds.add(new double[]{width + Const.maxCloudWidth/2, Math.random()*Const.maxCloudHeight, (int)(Math.random()*cloudBitmaps.length)});
 				cloudCounter = 0;
 			}
 		}
 	}
 
 	public void handleProtagonistMumbling(){
 		if(time-latestAction > Const.mumbleDelay){
 			latestAction = time;
 			playSound(protagonistSM, protagonistSoundIndexStart+(int)(Math.random()*5));
 		}
 	}
 
 	//Move mentor
 	public void moveMentor(){
 		if(mentor.getXPos() < checkpoints[currentCheckpoint] - pastCheckpointBorder){
 			if(mentor.getXPos() > 840 && timesMentorJumped == 0 || mentor.getXPos() > 1250 && timesMentorJumped == 1){
 				mentor.jump();
 				timesMentorJumped++;
 			}
 			mentor.accelerate(0.5);
 			mentor.step(1);
 			mentor.faceDirection(1);
 			mentor.setAim(0);
 		} else {
 			mentor.setStepCount(0);
 			mentor.slowDown();
 			mentor.faceDirection((int)(Math.signum(protagonist.getXPos()-mentor.getXPos())));
 			mentor.setAim((int)(180/Math.PI*Math.atan2(protagonist.getYPos() - mentor.getYPos(), protagonist.getXPos() - mentor.getXPos())));
 		}
 		handleMentorTalking();
 		mentor.checkSlope(ground, platforms);
 		mentor.inAir(platforms, ground);
 		mentor.gravity();
 		mentor.move();
 		mentor.breathe();
 		mentor.checkGround(ground);
 		mentor.checkPlatform(platforms);
 
 	}
 
 	//Handle mentor talking
 	public void handleMentorTalking(){
 		if(mentorPlayCounter <= 0 && mentorSentencesToSay > 0){
 			int nextSound = (int)(5*Math.random());
 			while(nextSound == lastMentorSound){
 				nextSound = (int)(5*Math.random());
 			}
 			lastMentorSound = nextSound;
 			playSound(mentorSM, nextSound);
 			mentorPlayCounter = mentorSM.getDuration(nextSound);
 			mentorSentencesToSay--;
 		}
 		if(mentorSentencesToSay == 0 && mentorPlayCounter <= 0 && mentorMessage != null){
 			mentorMessage = null;
 		}
 		mentorPlayCounter--;
 	}
 
 	//A special method for the tutorial
 	public void handleCheckpoints() {
 		switch(currentCheckpoint){
 		case 0:
 			if(leftStick.isPointed() && (leftStick.getAngle() < 45 || leftStick.getAngle() > 315 || leftStick.getAngle() > 135 && leftStick.getAngle() < 225)){
 				currentCheckpoint++;
 				mentorSentencesToSay = 3;
 			}
 			break;
 		case 1:
 			if(protagonist.getXPos() > checkpoints[1]-width/4){
 				currentCheckpoint++;
 				mentorSentencesToSay = 1;
 			}
 			break;
 		case 2:
 			if(!protagonist.isTouchingGround()){
 				currentCheckpoint++;
 				mentorSentencesToSay = 2;
 			}
 			break;
 		case 3: 
 			if(protagonist.getXPos() > 258){
 				currentCheckpoint++;
 				playSound(mentorSM, 5);
 			}
 			break;
 		case 4: 
 			if(protagonist.getXPos() > checkpoints[currentCheckpoint]){
 				currentCheckpoint++;
 				mentorSentencesToSay = 1;
 			}
 			break;
 		case 5:
 			if(protagonist.getXPos() > checkpoints[currentCheckpoint]){
 				currentCheckpoint++;
 				mentorSentencesToSay = 4;
 			}
 			break;
 		case 6: 
 			if(rightStick.isPointed()){
 				currentCheckpoint++;
 				mentorSentencesToSay = 2;
 			}
 			break;
 		case 7:
 			if(protagonist.getXPos() > checkpoints[currentCheckpoint] - width/4){
 				currentCheckpoint++;
 				mentorSentencesToSay = 3;
 			}
 			break;
 		case 8:
 			if(protagonist.isDashBonus() && Math.abs(protagonist.getXPos()-mentor.getXPos()) < 50){
 				currentCheckpoint++;
 				mentorSentencesToSay = 2;
 				tutorialFruitUp = false;
 				bird = new Bird(790, 100);
 				ambientSM.playLoopedSound(2, effectVolume);
 			}
 			break;
 		case 9:
 			if(bird.collide(bullets)){
 				currentCheckpoint++;
 				mentorSentencesToSay = 3;
 				ambientSM.stop(2);
 			}
 			break;
 		case 10:
 			if(protagonist.getXPos() > checkpoints[currentCheckpoint] && protagonist.getYPos() < platforms.get(0).getUpperY()[0]){
 				currentCheckpoint++;
 				mentorSentencesToSay = 2;
 			}
 			break;
 		case 11:
 			if(protagonist.getXPos() > checkpoints[currentCheckpoint]-width/4){
 				currentCheckpoint++;
 				mentorSentencesToSay = 3;
 				
 			}
 			break;
 		case 12:
 			if(enemies.size() == 2){
 				currentCheckpoint++;
 				mentorSentencesToSay = 4;
 
 			}
 			break;
 		case 13:
 			if(heatMeter.isCoolingDown()){
 				currentCheckpoint++;
 				mentorSentencesToSay = 2;
 			}
 			break;
 		case 14: 
 			if(enemies.size() == 0){
 				currentCheckpoint++;
 				playSound(mentorSM, 5);
 			}
 			break;
 		case 15:
 			if(mentor.getXPos() > checkpoints[currentCheckpoint]){
 				currentCheckpoint++;
 				mentorSentencesToSay = 3;
 			}
 			break;
 		}
 	}
 
 	//Method that gets called to render the graphics
 	public void render(Canvas canvas){
 		if(loading){
 			canvas.drawText("Loading...", 0, 20, loadPaint);
 		} else {
 			//Background
 			canvas.drawColor(backgroundColor); //Sky
 			if(!underground){
 				renderSun(canvas);
 				renderClouds(canvas);
 				renderTrees(canvas, 0);
 			}
 
 			if(level == 11){//Boss cave exit transition
 				if(protagonist.getXPos() > 2108 && protagonist.getXPos() < 2263){//?
 					canvas.drawRect((float)((2030-screenX)*scaleX), (float)((-500-screenY)*scaleY), (float)((2185-screenX)*scaleX), (float)((-200-screenY)*scaleY), backgroundPaint);
 				}
 			}
 
 			renderRocks(canvas, 0);
 			renderFlag(canvas, 0);
 			//Focus
 			renderSigns(canvas);
 			renderFlowers(canvas);
 			renderButterfly(canvas);
 			renderEnemies(canvas);
 			if(level == 0){ //Tutorial
 				renderMentor(canvas);
 				renderFruit(canvas);
 			}
 			if(level == 11){
 				renderMentor(canvas);
 				if(showHUD)
 					renderMentorHealthMeter(canvas);
 			}
 			renderProtagonist(canvas);
 			renderPlatforms(canvas);
 			renderGround(canvas);
 			renderBullets(canvas);
 
 			//Foreground
 			renderDust(canvas);
 			renderFlag(canvas, 1);
 			renderRocks(canvas, 1);
 			renderTrees(canvas, 1);
 			renderBird(canvas);
 			renderSkeletons(canvas);
 
 			//HUD
 			if(showHUD){
 				renderSticks(canvas);
 				renderHeatMeter(canvas);
 				renderHealthMeter(canvas);
 				renderTime(canvas);
 			}
 			//Tutorial 
 			if(level == 0){
 				renderHint(canvas);
 			} else if(mentorMessage != null){
 				renderHint(canvas, mentorMessage);
 			} else {
 				for(i = 0; i < hints.size(); i++){
 					if(hints.get(i).inRange(protagonist.getXPos(), protagonist.getYPos())){
 						renderHint(canvas, hints.get(i).getHint());
 						break;
 					}
 				}
 			}
 			if(darknessPercent > 0)
 				renderDarkness(canvas, darknessPercent);
 		}
 	}
 
 	//Draw the enemies
 	public void renderEnemies(Canvas canvas){
 
 		for(int type: renderOrder){ //Different types
 			for(i = 0; i < enemies.size(); i++){
 				if(enemies.get(i).getType() == type){
 					if(enemies.get(i).hasSpawned()){
 						if(enemies.get(i).getXPos() + enemies.get(i).getWidth() > screenX && enemies.get(i).getXPos() - enemies.get(i).getWidth() < screenX + width){
 							Enemy e = enemies.get(i);
 							feetAngle = (int)(Const.enemyMaxFootAngle*Math.sin(time/3.));
 							if(e.wasHitThisFrame() || time - latestDashTime < 10 && (time-latestDashTime)%2 == 0){
 								enemyPaint = stamper;
 							} else {
 								enemyPaint = null;
 							}
 							if(e.getXVel() < 0){
 
 								//Back arm
 								renderMatrix.setRotate(e.getLeftArmAngle(), enemyRightArmBitmap[e.getType()-1].getWidth(), (float)(enemyRightArmBitmap[e.getType()-1].getHeight()*0.9));
 								renderMatrix.postTranslate((float)((e.getXPos() + e.getWidth()*(0.5-Const.enemyArmXAxis) - screenX)*scaleX)-enemyRightArmBitmap[e.getType()-1].getWidth(), (float)((e.getYPos() + e.getHeight()*(Const.enemyArmYAxis-0.5) - screenY)*scaleY));
 								canvas.drawBitmap(enemyRightArmBitmap[e.getType()-1], renderMatrix, enemyPaint);
 								//Back foot
 								renderMatrix.setRotate(-feetAngle, enemyFootBitmap[e.getType()-1].getWidth()/2, enemyFootBitmap[e.getType()-1].getHeight()/2);
 								renderMatrix.postTranslate((float)((e.getXPos() - e.getWidth()*(0.5-Const.enemyFootXAxis-Const.backFootOffset) - e.getWidth()*Const.enemyFootRadius*Math.sin(-feetAngle*Math.PI/180) - screenX)*scaleX), (float)((e.getYPos() + e.getHeight()*(Const.enemyFootYAxis-0.5) + e.getHeight()*Const.enemyFootRadius*Math.cos(-feetAngle*Math.PI/180) - screenY)*scaleY));
 								canvas.drawBitmap(enemyFootBitmap[e.getType()-1], renderMatrix, null);
 								//Body
 								renderMatrix.setTranslate((float)((e.getXPos()-e.getWidth()*(0.5-Const.enemyBodyXOffset)-screenX)*scaleX), (float)((e.getYPos()-e.getHeight()*(0.5-Const.enemyBodyYOffset)-screenY)*scaleY));
 								canvas.drawBitmap(enemyBodyBitmap[e.getType()-1], renderMatrix, enemyPaint);
 								//Eyes and mouth
 								renderMatrix.setTranslate((float)((e.getXPos()-e.getWidth()*(0.5-Const.enemyEyeMouthXOffset)-screenX)*scaleX), (float)((e.getYPos()-e.getHeight()*(0.5-Const.enemyEyeMouthYOffset)-screenY)*scaleY));
 								canvas.drawBitmap(enemyEyeMouthBitmap[e.getType()-1], renderMatrix, null);
 								//Draw accessories
 								if(e.getType() == 2){
 									renderMatrix.setTranslate((float)((e.getXPos()-e.getWidth()*(0.5-Const.enemyNinjaXOffset)-screenX)*scaleX), (float)((e.getYPos()-e.getHeight()*(0.5-Const.enemyNinjaYOffset)-screenY)*scaleY));
 									canvas.drawBitmap(enemyNinjaBitmap, renderMatrix, null);
 								} else if(e.getType() == 3){
 									renderMatrix.setTranslate((float)((e.getXPos()-e.getWidth()*(0.5-Const.enemyArmorXOffset)-screenX)*scaleX), (float)((e.getYPos()-e.getHeight()*(0.5-Const.enemyArmorYOffset)-screenY)*scaleY));
 									canvas.drawBitmap(enemyArmorBitmap, renderMatrix, null);
 								}
 
 								//Front arm
 								renderMatrix.setRotate(e.getRightArmAngle(), 0, (float)(enemyLeftArmBitmap[e.getType()-1].getHeight()*0.9));
 								renderMatrix.postTranslate((float)((e.getXPos() - e.getWidth()*(0.5-Const.enemyArmXAxis) - screenX)*scaleX), (float)((e.getYPos() + e.getHeight()*(Const.enemyArmYAxis-0.5) - screenY)*scaleY));
 								canvas.drawBitmap(enemyLeftArmBitmap[e.getType()-1], renderMatrix, enemyPaint);
 								//Front foot
 								renderMatrix.setRotate(feetAngle, enemyFootBitmap[e.getType()-1].getWidth()/2, enemyFootBitmap[e.getType()-1].getHeight()/2);
 								renderMatrix.postTranslate((float)((e.getXPos() - e.getWidth()*(0.5-Const.enemyFootXAxis-Const.backFootOffset) - e.getWidth()*Const.enemyFootRadius*Math.sin(feetAngle*Math.PI/180) - screenX)*scaleX), (float)((e.getYPos() + e.getHeight()*(Const.enemyFootYAxis-0.5) + e.getHeight()*Const.enemyFootRadius*Math.cos(feetAngle*Math.PI/180) - screenY)*scaleY));
 								canvas.drawBitmap(enemyFootBitmap[e.getType()-1], renderMatrix, null);
 								//Pupils
 								renderMatrix.setTranslate((float)((e.getXPos() + e.getWidth()*(Const.enemyPupilXOffset-0.5)+e.getWidth()*Const.enemyPupilRadius*Math.cos(e.getPupilAngle()*Math.PI/180)-screenX)*scaleX), (float)((e.getYPos() + e.getHeight()*(Const.enemyPupilYOffset-0.5)+e.getHeight()*Const.enemyPupilRadius*Math.sin(e.getPupilAngle()*Math.PI/180) - screenY)*scaleY));
 								canvas.drawBitmap(enemyPupilBitmap[e.getType()-1], renderMatrix, null);
 							} else {
 								//Back arm
 								renderMatrix.setRotate(e.getRightArmAngle(), 0, (float)(enemyLeftArmBitmap[e.getType()-1].getHeight()*0.9));
 								renderMatrix.postTranslate((float)((e.getXPos() - e.getWidth()*(0.5-Const.enemyArmXAxis) - screenX)*scaleX), (float)((e.getYPos() + e.getHeight()*(Const.enemyArmYAxis-0.5) - screenY)*scaleY));
 								canvas.drawBitmap(enemyLeftArmBitmap[e.getType()-1], renderMatrix, enemyPaint);
 								//Back foot
 								renderMatrix.setRotate(-feetAngle, enemyFootBitmap[e.getType()-1].getWidth()/2, enemyFootBitmap[e.getType()-1].getHeight()/2);
 								renderMatrix.postTranslate((float)((e.getXPos() + e.getWidth()*(0.5-Const.enemyFootXAxis-Const.backFootOffset) - e.getWidth()*Const.enemyFootRadius*Math.sin(-feetAngle*Math.PI/180) - screenX)*scaleX) - enemyFootBitmap[e.getType()-1].getWidth(), (float)((e.getYPos() + e.getHeight()*(Const.enemyFootYAxis-0.5) + e.getHeight()*Const.enemyFootRadius*Math.cos(feetAngle*Math.PI/180) - screenY)*scaleY));
 								canvas.drawBitmap(enemyFootBitmap[e.getType()-1], renderMatrix, null);
 								//Body
 								renderMatrix.setTranslate((float)((e.getXPos()-e.getWidth()*(0.5-Const.enemyBodyXOffset)-screenX)*scaleX), (float)((e.getYPos()-e.getHeight()*(0.5-Const.enemyBodyYOffset)-screenY)*scaleY));
 								canvas.drawBitmap(enemyBodyBitmapFlipped[e.getType()-1], renderMatrix, enemyPaint);
 								//Eyes and mouth
 								renderMatrix.setTranslate((float)((e.getXPos()+e.getWidth()*(0.5-Const.enemyEyeMouthXOffset)-screenX)*scaleX)-enemyEyeMouthBitmap[e.getType()-1].getWidth(), (float)((e.getYPos()-e.getHeight()*(0.5-Const.enemyEyeMouthYOffset)-screenY)*scaleY));
 								canvas.drawBitmap(enemyEyeMouthBitmapFlipped[e.getType()-1], renderMatrix, null);
 								//Draw accessories
 								if(e.getType() == 2){
 									renderMatrix.setTranslate((float)((e.getXPos()+e.getWidth()*(0.5-Const.enemyNinjaXOffset)-screenX)*scaleX)-enemyNinjaBitmap.getWidth(), (float)((e.getYPos()-e.getHeight()*(0.5-Const.enemyNinjaYOffset)-screenY)*scaleY));
 									canvas.drawBitmap(enemyNinjaBitmapFlipped, renderMatrix, null);
 								} else if(e.getType() == 3){
 									renderMatrix.setTranslate((float)((e.getXPos()+e.getWidth()*(0.5-Const.enemyArmorXOffset)-screenX)*scaleX)-enemyArmorBitmap.getWidth(), (float)((e.getYPos()-e.getHeight()*(0.5-Const.enemyArmorYOffset)-screenY)*scaleY));
 									canvas.drawBitmap(enemyArmorBitmapFlipped, renderMatrix, null);
 								}
 
 								//Front arm
 								renderMatrix.setRotate(e.getLeftArmAngle(), enemyRightArmBitmap[e.getType()-1].getWidth(), (float)(enemyRightArmBitmap[e.getType()-1].getHeight()*0.9));
 								renderMatrix.postTranslate((float)((e.getXPos() + e.getWidth()*(0.5-Const.enemyArmXAxis) - screenX)*scaleX)-enemyRightArmBitmap[e.getType()-1].getWidth(), (float)((e.getYPos() + e.getHeight()*(Const.enemyArmYAxis-0.5) + /*e.getHeight()*Const.enemyArmRadius*Math.sin(-e.getLeftArmAngle()*Math.PI/180)*/ - screenY)*scaleY));
 								canvas.drawBitmap(enemyRightArmBitmap[e.getType()-1], renderMatrix, enemyPaint);
 								//Front foot
 								renderMatrix.setRotate(feetAngle, enemyFootBitmap[e.getType()-1].getWidth()/2, enemyFootBitmap[e.getType()-1].getHeight()/2);
 								renderMatrix.postTranslate((float)((e.getXPos() + e.getWidth()*(0.5-Const.enemyFootXAxis-Const.backFootOffset) - e.getWidth()*Const.enemyFootRadius*Math.sin(feetAngle*Math.PI/180) - screenX)*scaleX) - enemyFootBitmap[e.getType()-1].getWidth(), (float)((e.getYPos() + e.getHeight()*(Const.enemyFootYAxis-0.5) + e.getHeight()*Const.enemyFootRadius*Math.cos(-feetAngle*Math.PI/180) - screenY)*scaleY));
 								canvas.drawBitmap(enemyFootBitmapFlipped[e.getType()-1], renderMatrix, null);
 								//Pupils
 								renderMatrix.setTranslate((float)((e.getXPos() - e.getWidth()*(Const.enemyPupilXOffset-0.5)+e.getWidth()*Const.enemyPupilRadius*Math.cos(e.getPupilAngle()*Math.PI/180)-screenX)*scaleX)-enemyPupilBitmap[e.getType()-1].getWidth(), (float)((e.getYPos() + e.getHeight()*(Const.enemyPupilYOffset-0.5) +  e.getHeight()*Const.enemyPupilRadius*Math.sin(e.getPupilAngle()*Math.PI/180) - screenY)*scaleY));
 								canvas.drawBitmap(enemyPupilBitmap[e.getType()-1], renderMatrix, null);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	//Draw the protagonist
 	public void renderProtagonist(Canvas canvas){
 		aimAngle = (float)protagonist.getAim();
 		if(protagonist.isTouchingGround()){
 			feetAngle = (float)(180/Math.PI*Math.sin((double)protagonist.getStepCount()/protagonist.getNumberOfSteps()*Math.PI));
 		} else {
 			feetAngle = Const.jumpFeetAngle;
 		}
 
 		if(protagonist.isSliding() || protagonist.isDashBonus()){
 			feetAngle = Const.jumpFeetAngle;
 		}
 		if(!protagonist.isInvincible() || protagonist.getInvincibilityCount() % 2 == 0){
 
 			//Draw all the protagonist parts
 
 			if(protagonist.isFacingRight()){
 				//Draw back foot
 				renderMatrix.setRotate(-feetAngle, footBitmap.getWidth()/2, footBitmap.getHeight()/2);
 				renderMatrix.postTranslate((float)((protagonist.getXPos() - protagonist.getWidth()*(0.5-Const.footXAxis-Const.backFootOffset) - protagonist.getWidth()*Const.footRadius*Math.sin(-feetAngle*Math.PI/180) - screenX)*scaleX), (float)((protagonist.getYPos() + protagonist.getHeight()*(Const.footYAxis-0.5) + protagonist.getHeight()*Const.footRadius*Math.cos(-feetAngle*Math.PI/180) - screenY)*scaleY));
 				canvas.drawBitmap(footBitmap, renderMatrix, null);
 				//Draw body
 				renderMatrix.setTranslate((float)((protagonist.getXPos() - protagonist.getWidth()*(0.5-Const.bodyXOffset) - screenX)*scaleX), (float)((protagonist.getYPos() - protagonist.getHeight()*(0.5-Const.bodyYOffset + Const.breathOffset*Math.sin((double)protagonist.getBreathCount()/protagonist.getBreathMax()*2*Math.PI)) - screenY)*scaleY));
 				canvas.drawBitmap(bodyBitmap, renderMatrix, null);
 				//Draw eyes and mouth
 				renderMatrix.setTranslate((float)((protagonist.getXPos() - protagonist.getWidth()*(0.5-Const.eyeMouthXOffset) - screenX)*scaleX), (float)((protagonist.getYPos() - protagonist.getHeight()*(0.5-Const.eyeMouthYOffset) - screenY)*scaleY));
 				canvas.drawBitmap(eyeMouthBitmap, renderMatrix, null);
 				//Draw front foot
 				if(protagonist.isSliding() || protagonist.isDashBonus()){
 					feetAngle = -Const.jumpFeetAngle;
 				}
 				renderMatrix.setRotate(feetAngle, footBitmap.getWidth()/2, footBitmap.getHeight()/2);
 				renderMatrix.postTranslate((float)((protagonist.getXPos() - protagonist.getWidth()*(0.5-Const.footXAxis) - protagonist.getWidth()*Const.footRadius*Math.sin(feetAngle*Math.PI/180) - screenX)*scaleX), (float)((protagonist.getYPos() + protagonist.getHeight()*(Const.footYAxis-0.5) + protagonist.getHeight()*Const.footRadius*Math.cos(feetAngle*Math.PI/180) - screenY)*scaleY));
 				canvas.drawBitmap(footBitmap, renderMatrix, null);
 				//Draw pupils
 				renderMatrix.setTranslate((float)((protagonist.getXPos() + protagonist.getWidth()*(Const.pupilXOffset-0.5)+protagonist.getWidth()*Const.pupilRadius*Math.cos(aimAngle*Math.PI/180)-screenX)*scaleX), (float)((protagonist.getYPos() + protagonist.getHeight()*(Const.pupilYOffset-0.5)-protagonist.getHeight()*Const.pupilRadius*Math.sin(aimAngle*Math.PI/180) - screenY)*scaleY));
 				canvas.drawBitmap(pupilBitmap, renderMatrix, null);
 				//Draw weapon
 				renderMatrix.setRotate(-aimAngle, weaponBitmap.getWidth()/2, weaponBitmap.getHeight()/2);
 				renderMatrix.postTranslate((float)((protagonist.getXPos() - protagonist.getWidth()*(0.5-Const.weaponXAxis) + protagonist.getWidth()*Const.weaponRadius*Math.cos(aimAngle*Math.PI/180) - screenX)*scaleX), (float)((protagonist.getYPos() + protagonist.getHeight()*(Const.weaponYAxis-0.5) - protagonist.getHeight()*Const.weaponRadius*Math.sin(aimAngle*Math.PI/180) - screenY)*scaleY));
 				canvas.drawBitmap(weaponBitmap, renderMatrix, null);
 			} else {
 				//Draw back foot
 				renderMatrix.setRotate(feetAngle, footBitmapFlipped.getWidth()/2, footBitmapFlipped.getHeight()/2);
 				renderMatrix.postTranslate((float)((protagonist.getXPos() - protagonist.getWidth()*(0.5-Const.footXAxis+Const.backFootOffset) - protagonist.getWidth()*Const.footRadius*Math.sin(Math.PI-feetAngle*Math.PI/180) - screenX)*scaleX), (float)((protagonist.getYPos() + protagonist.getHeight()*(Const.footYAxis-0.5) + protagonist.getHeight()*Const.footRadius*Math.cos(-feetAngle*Math.PI/180) - screenY)*scaleY));
 				canvas.drawBitmap(footBitmapFlipped, renderMatrix, null);
 				//Draw body
 				renderMatrix.setTranslate((float)((protagonist.getXPos() + protagonist.getWidth()*(0.5-Const.bodyXOffset) - screenX)*scaleX) - bodyBitmapFlipped.getWidth(), (float)((protagonist.getYPos() - protagonist.getHeight()*(0.5-Const.bodyYOffset + Const.breathOffset*Math.sin((double)protagonist.getBreathCount()/protagonist.getBreathMax()*2*Math.PI)) - screenY)*scaleY));
 				canvas.drawBitmap(bodyBitmapFlipped, renderMatrix, null);
 				//Draw eyes and mouth
 				renderMatrix.setTranslate((float)((protagonist.getXPos() + protagonist.getWidth()*(0.5-Const.eyeMouthXOffset) - screenX)*scaleX) - eyeMouthBitmapFlipped.getWidth(), (float)((protagonist.getYPos() - protagonist.getHeight()*(0.5-Const.eyeMouthYOffset) - screenY)*scaleY));
 				canvas.drawBitmap(eyeMouthBitmapFlipped, renderMatrix, null);
 				//Draw front foot
 				if(protagonist.isSliding() || protagonist.isDashBonus()){
 					feetAngle = -Const.jumpFeetAngle;
 				}
 				renderMatrix.setRotate(-feetAngle, footBitmapFlipped.getWidth()/2, footBitmapFlipped.getHeight()/2);
 				renderMatrix.postTranslate((float)((protagonist.getXPos() - protagonist.getWidth()*(0.5-Const.footXAxis) - protagonist.getWidth()*Const.footRadius*Math.sin(Math.PI+feetAngle*Math.PI/180) - screenX)*scaleX), (float)((protagonist.getYPos() + protagonist.getHeight()*(Const.footYAxis-0.5) + protagonist.getHeight()*Const.footRadius*Math.cos(feetAngle*Math.PI/180) - screenY)*scaleY));
 				canvas.drawBitmap(footBitmapFlipped, renderMatrix, null);
 				//Draw pupils
 				renderMatrix.setTranslate((float)((protagonist.getXPos() + protagonist.getWidth()*(Const.pupilXOffset-0.5)+protagonist.getWidth()*Const.pupilRadius*Math.cos(aimAngle*Math.PI/180)-screenX)*scaleX), (float)((protagonist.getYPos() + protagonist.getHeight()*(Const.pupilYOffset-0.5)-protagonist.getHeight()*Const.pupilRadius*Math.sin(aimAngle*Math.PI/180) - screenY)*scaleY));
 				canvas.drawBitmap(pupilBitmapFlipped, renderMatrix, null);
 				//Draw weapon
 				renderMatrix.setRotate(180-aimAngle, weaponBitmapFlipped.getWidth()/2, weaponBitmapFlipped.getHeight()/2);
 				renderMatrix.postTranslate((float)((protagonist.getXPos()  + protagonist.getWidth()*(0.5-Const.weaponXAxis) + protagonist.getWidth()*Const.weaponRadius*Math.cos(aimAngle*Math.PI/180) - screenX)*scaleX - weaponBitmapFlipped.getWidth()), (float)((protagonist.getYPos() + protagonist.getHeight()*(Const.weaponYAxis-0.5) + protagonist.getHeight()*Const.weaponRadius*Math.sin(Math.PI+aimAngle*Math.PI/180) - screenY)*scaleY));
 				canvas.drawBitmap(weaponBitmapFlipped, renderMatrix, null);
 			}
 
 		}
 
 	}
 
 	//Draws the ground using a Path
 	//Draw only the parts which are visible on the screen
 	public void renderGround(Canvas canvas){
 		i = 0;
 
 		//Find the interval of the ground that has to be rendered
 		while(ground.getX(i+1) < screenX){
 			i++;
 		}
 		int startIndex = i;
 		int lowestPoint = ground.getY(i);
 		while(ground.getX(i) < screenX + width && i < ground.getLength()-2){
 			i++;
 			lowestPoint = Math.max(lowestPoint, ground.getY(i));
 		}
 
 		int stopIndex = i;
 
 		for(i = startIndex; i <= stopIndex; i++){
 			if(Math.abs(ground.getSlope((ground.getX(i) + ground.getX(i+1))/2)) < 10){
 				groundPath = new Path();
 				groundPath.moveTo((int)((ground.getX(i)-screenX)*scaleX), (int)((ground.getY(i)-screenY)*scaleY));
 				groundPath.lineTo((int)((ground.getX(i+1)-screenX)*scaleX), (int)((ground.getY(i+1)-screenY)*scaleY));
 				groundPath.lineTo((int)((ground.getX(i+1)-screenX)*scaleX), (int)((ground.getY(i+1)+groundThickness-screenY)*scaleY)); 
 				groundPath.lineTo((int)((ground.getX(i)-screenX)*scaleX), (int)((ground.getY(i)+groundThickness-screenY)*scaleY));
 				groundPath.lineTo((int)((ground.getX(i)-screenX)*scaleX), (int)((ground.getY(i)-screenY)*scaleY));
 				canvas.drawPath(groundPath, groundPaint);
 			}
 			dirtPath = new Path();
 			dirtPath.moveTo((int)((ground.getX(i)-screenX)*scaleX), (int)((ground.getY(i)+groundThickness-screenY)*scaleY));
 			dirtPath.lineTo((int)((ground.getX(i+1)-screenX)*scaleX), (int)((ground.getY(i+1)+groundThickness-screenY)*scaleY));
 			dirtPath.lineTo((int)((ground.getX(i+1)-screenX)*scaleX), (int)((lowestPoint+height-screenY)*scaleY));
 			dirtPath.lineTo((int)((ground.getX(i)-screenX)*scaleX), (int)((lowestPoint+height-screenY)*scaleY));
 			dirtPath.lineTo((int)((ground.getX(i)-screenX)*scaleX), (int)((ground.getY(i)+groundThickness-screenY)*scaleY));
 			canvas.drawPath(dirtPath, dirtPaint);			
 		}
 
 		//Spikes
 		groundPath = new Path();
 		for(i = startIndex; i <= stopIndex; i++){
 			if (Math.abs(ground.getSlope((ground.getX(i) + ground.getX(i+1))/2)) < 10){
 				xGap = (ground.getX(i+1)-ground.getX(i));
 				yGap = (ground.getY(i+1)-ground.getY(i));
 				gap = Math.sqrt(Math.pow(xGap, 2) + Math.pow(yGap, 2));
 				numberOfPatches = (int)(gap/foliageSize/2+2);
 				groundAngle = Math.atan(ground.getSlope((ground.getX(i)+ground.getX(i+1))/2));
 				groundPath.moveTo((float)((ground.getX(i)-screenX)*scaleX), (float)((ground.getY(i)-screenY)*scaleY));
 				for(int j = 0; j < numberOfPatches; j++){
 					groundPath.lineTo((float)((ground.getX(i)+xGap*(j+0.5)/numberOfPatches+foliageSize*Math.sin(groundAngle)-screenX)*scaleX), (float)((ground.getY(i)+yGap/numberOfPatches*(j+0.5)-foliageSize*Math.cos(groundAngle)-screenY)*scaleY));
 					groundPath.lineTo((float)((ground.getX(i)+xGap*(j+1)/numberOfPatches - screenX)*scaleX), (float)((ground.getY(i)+yGap/numberOfPatches*(j+1)-screenY)*scaleY));
 				}
 				groundPath.lineTo((float)((ground.getX(i)-screenX)*scaleX), (float)((ground.getY(i)-screenY)*scaleY));
 				canvas.drawPath(groundPath, groundPaint);
 			}
 		}
 
 	}
 
 	public void renderSigns(Canvas canvas){
 		for(i = 0; i < hints.size(); i++){
 			canvas.drawBitmap(signBitmap, (float)((hints.get(i).getX()-Const.hintSize/2 - screenX)*scaleX), (float)((hints.get(i).getY() - Const.hintSize - screenY)*scaleY), null);
 		}
 	}
 
 	//Draw the platforms
 	public void renderPlatforms(Canvas canvas){
 		for(i = 0; i < platforms.size(); i++){
 			if(platforms.get(i).getUpperX()[0] < screenX+width && platforms.get(i).getUpperX()[platforms.get(i).getUpperX().length-1] > screenX){
 				groundPath = new Path(platforms.get(i).getGroundPath());
 				dirtPath = new Path(platforms.get(i).getDirtPath());
 				renderMatrix.setTranslate(-(float)screenX, -(float)screenY);
 				renderMatrix.postScale((float)scaleX, (float)scaleY);
 				groundPath.transform(renderMatrix);
 				dirtPath.transform(renderMatrix);
 				canvas.drawPath(groundPath, groundPaint);
 				canvas.drawPath(dirtPath, dirtPaint);
 				//Draw platform details
 				//Spikes on top
 				groundPath = new Path();
 				for(int k = 0; k < platforms.get(i).getUpperX().length-1; k++){
 					xGap = (platforms.get(i).getUpperX()[k+1]-platforms.get(i).getUpperX()[k]);
 					yGap = (platforms.get(i).getUpperY()[k+1]-platforms.get(i).getUpperY()[k]);
 					gap = Math.sqrt(Math.pow(xGap, 2) + Math.pow(yGap, 2));
 					numberOfPatches = (int)(gap/foliageSize/2+2);
 					groundAngle = Math.atan(platforms.get(i).getSlope((platforms.get(i).getUpperX()[k]+platforms.get(i).getUpperX()[k+1])/2));
 					groundPath.moveTo((float)((platforms.get(i).getUpperX()[k]-screenX)*scaleX), (float)((platforms.get(i).getUpperY()[k]-screenY)*scaleY));
 					for(int j = 0; j < numberOfPatches; j++){
 						groundPath.lineTo((float)((platforms.get(i).getUpperX()[k]+xGap*(j+0.5)/numberOfPatches+foliageSize*Math.sin(groundAngle)-screenX)*scaleX), (float)((platforms.get(i).getUpperY()[k]+yGap/numberOfPatches*(j+0.5)-foliageSize*Math.cos(groundAngle)-screenY)*scaleY));
 						groundPath.lineTo((float)((platforms.get(i).getUpperX()[k]+xGap*(j+1)/numberOfPatches - screenX)*scaleX), (float)((platforms.get(i).getUpperY()[k]+yGap/numberOfPatches*(j+1)-screenY)*scaleY));
 					}
 					groundPath.lineTo((float)((platforms.get(i).getUpperX()[k]-screenX)*scaleX), (float)((platforms.get(i).getUpperY()[k]-screenY)*scaleY));
 					canvas.drawPath(groundPath, groundPaint);
 				}
 			}
 		}
 	}
 
 	//Draw the sticks
 	public void renderSticks(Canvas canvas){
 		canvas.drawBitmap(stickBitmap, (float)((leftStick.getX()-leftStick.getRadius())*scaleX), (float)((leftStick.getY()-leftStick.getRadius())*scaleY), null);
 		canvas.drawBitmap(stickBitmap, (float)((rightStick.getX()-rightStick.getRadius())*scaleX), (float)((rightStick.getY()-rightStick.getRadius())*scaleY), null);
 	}
 
 	//Draw the bullets
 	public void renderBullets(Canvas canvas){
 		int radius = Bullet.getRadius();
 		for(i = 0; i < bullets.size(); i++){
 			Bullet b = bullets.get(i);
 			renderMatrix.setRotate((float)(180/Math.PI*Math.atan2(b.getYVel(), b.getXVel())), (float)(bulletBitmap.getWidth()/2), (float)(bulletBitmap.getHeight()/2));
 			renderMatrix.postTranslate((float)((b.getXPos()-radius/2.-screenX)*scaleX), (float)((b.getYPos()-radius/2.-screenY)*scaleY));
 			canvas.drawBitmap(bulletBitmap, renderMatrix, null);
 		}
 	}
 
 	//Draw the heatmeter
 	public void renderHeatMeter(Canvas canvas){
 		//This makes the frame blink red if overheated
 		if(heatMeter.isCoolingDown() && heatMeter.getHeat() % (2*Const.blinkInterval) > Const.blinkInterval){
 			frame.setColor(Color.RED);
 		} else {
 			frame.setColor(Color.BLACK);
 		}
 
 		//Draw in top right corner
 		//Draw frame
 		canvas.drawRect((float)((this.width - Const.meterWidth - Const.HUDPadding - Const.meterFrameSize)*scaleX), (float)((Const.HUDPadding-Const.meterFrameSize)*scaleY), (float)((this.width - Const.HUDPadding + Const.meterFrameSize)*scaleX), (float)((Const.HUDPadding+Const.meterHeight+Const.meterFrameSize)*scaleY), frame);
 		//Draw green background
 		canvas.drawRect((float)((this.width - Const.meterWidth - Const.HUDPadding)*scaleX), (float)(Const.HUDPadding*scaleY), (float)((this.width - Const.HUDPadding)*scaleX), (float)((Const.HUDPadding+Const.meterHeight)*scaleY), green);
 		//Draw red indicator that moves with current heat level
 		canvas.drawRect((float)((this.width - Const.meterWidth - Const.HUDPadding)*scaleX), (float)(Const.HUDPadding*scaleY), (float)((this.width - Const.HUDPadding - Const.meterWidth*(1-heatMeter.getHeat()))*scaleX), (float)((Const.HUDPadding+Const.meterHeight)*scaleY), red);
 	}
 
 	//Draw the healtheter
 	public void renderHealthMeter(Canvas canvas){
 		//This makes the frame blink red if overheated
 		if(protagonist.getHealth() < Const.criticalHealth && time % 10 >= 5){
 			frame.setColor(Color.RED);
 		} else {
 			frame.setColor(Color.BLACK);
 		}
 		if(protagonist.getHealth() < 0){
 			protagonist.setHealth(0);
 		}
 
 		//Draw in top left corner
 		//Draw frame
 		canvas.drawRect((float)((Const.HUDPadding-Const.meterFrameSize)*scaleX), (float)((Const.HUDPadding-Const.meterFrameSize)*scaleY), (float)((Const.HUDPadding+Const.meterWidth+Const.meterFrameSize)*scaleX), (float)((Const.HUDPadding+Const.meterHeight+Const.meterFrameSize)*scaleY), frame);
 		//Draw green background
 		canvas.drawRect((float)(Const.HUDPadding*scaleX), (float)(Const.HUDPadding*scaleY), (float)((Const.HUDPadding+Const.meterWidth)*scaleX), (float)((Const.HUDPadding+Const.meterHeight)*scaleY), red);
 		//Draw red indicator that moves with current heat level
 		canvas.drawRect((float)(Const.HUDPadding*scaleX), (float)(Const.HUDPadding*scaleY), (float)((Const.HUDPadding+Const.meterWidth*protagonist.getHealth())*scaleX), (float)((Const.HUDPadding+Const.meterHeight)*scaleY), green);
 	}
 
 	public void renderMentorHealthMeter(Canvas canvas){
 		frame.setColor(Color.BLACK);
 		//Draw frame
 		canvas.drawRect((float)((mentor.getXPos() - Const.mentorHealthBarWidth/2 - Const.meterFrameSize - screenX)*scaleX), (float)((mentor.getYPos()-mentor.getHeight()/2-Const.mentorHealthBarHeight - Const.meterFrameSize - screenY)*scaleY), (float)((mentor.getXPos() + Const.mentorHealthBarWidth/2 + Const.meterFrameSize - screenX)*scaleX), (float)((mentor.getYPos()-mentor.getHeight()/2 + Const.meterFrameSize - screenY)*scaleY), frame);
 		//Draw green background
 		canvas.drawRect((float)((mentor.getXPos() - Const.mentorHealthBarWidth/2 - screenX)*scaleX), (float)((mentor.getYPos()-mentor.getHeight()/2-Const.mentorHealthBarHeight - screenY)*scaleY), (float)((mentor.getXPos() + Const.mentorHealthBarWidth/2 - screenX)*scaleX), (float)((mentor.getYPos()-mentor.getHeight()/2 - screenY)*scaleY), red);
 		//Draw red indicator that moves with current heat level
 		canvas.drawRect((float)((mentor.getXPos() - Const.mentorHealthBarWidth/2 - screenX)*scaleX), (float)((mentor.getYPos()-mentor.getHeight()/2-Const.mentorHealthBarHeight - screenY)*scaleY), (float)((mentor.getXPos() - Const.mentorHealthBarWidth/2 + mentor.getHealth()*Const.mentorHealthBarWidth - screenX)*scaleX), (float)((mentor.getYPos()-mentor.getHeight()/2 - screenY)*scaleY), green);
 	}
 
 	//Draw the sun, moving in time
 	public void renderSun(Canvas canvas){
 		canvas.drawBitmap(sunBitmap, (float)(width*scaleX/3), (float)((20 + 20*Math.sin(Math.PI + time/500.))*scaleY), null);
 	}
 
 	//Draw clouds
 	public void renderClouds(Canvas canvas){
 		for(i = 0; i < clouds.size(); i++){
 			canvas.drawBitmap(cloudBitmaps[(int)(clouds.get(i)[2])], (float)((clouds.get(i)[0]-Const.maxCloudWidth/2)*scaleX), (float)((clouds.get(i)[1])*scaleY), null);
 		}
 	}
 
 	//Draw flowers
 	public void renderFlowers(Canvas canvas){
 		for(i = 0; i < flowers.size(); i++){
 			if(flowers.get(i)[0] > screenX - Const.flowerSize/2 && flowers.get(i)[0] < screenX + width + Const.flowerSize/2){
 				canvas.drawBitmap(flowerBitmap, (float)((flowers.get(i)[0] - Const.flowerSize/4 - screenX)*scaleX), (float)((ground.getYFromX(flowers.get(i)[0])-Const.flowerSize-screenY)*scaleY), null);
 			}
 		}
 	}
 	//Draw skeletons
 	public void renderSkeletons(Canvas canvas){
 		for(i = 0; i < skeletons.size(); i++){
 			if(skeletons.get(i)[0] > screenX - Const.skeletonSize/2 && skeletons.get(i)[0] < screenX + width + Const.skeletonSize/2){
 				canvas.drawBitmap(skeletonBitmap, (float)((skeletons.get(i)[0] - Const.skeletonSize/4 - screenX)*scaleX), (float)((ground.getYFromX(skeletons.get(i)[0]) + Const.skeletonSize/4 - screenY + skeletons.get(i)[1])*scaleY), null);
 			}
 		}
 	}
 
 	//Draw rocks
 	public void renderRocks(Canvas canvas, int depth){
 		for(i = 0; i < rocks.size(); i++){
 			if(rocks.get(i)[3] == depth && rocks.get(i)[0] > screenX - rocks.get(i)[2]/2 && rocks.get(i)[0] < screenX + width + rocks.get(i)[2]/2){
 				groundAngle = (Math.atan(ground.getSlope(rocks.get(i)[0])));
 				renderMatrix.setScale((float)(rocks.get(i)[2]/Const.maxRockSize), (float)(rocks.get(i)[2]/Const.maxRockSize));
 				renderMatrix.postRotate((float)(groundAngle*180/Math.PI), (float)(rocks.get(i)[2]/2*scaleX), (float)(rocks.get(i)[2]/2*scaleY));
 				renderMatrix.postTranslate((float)((rocks.get(i)[0]-screenX-rocks.get(i)[2]/2)*scaleX), (float)((ground.getYFromX(rocks.get(i)[0])-screenY-rocks.get(i)[2]*Const.partOfRockVisible)*scaleY));
 				canvas.drawBitmap(rockBitmap[rocks.get(i)[1]], renderMatrix, null);
 			}
 		}
 	}
 
 	//Draw tree
 	public void renderTrees(Canvas canvas, int depth){
 		for(i = 0; i < trees.size(); i++){
 			if(trees.get(i)[3] == depth){
 				if(trees.get(i)[0] > screenX - Const.maxTreeWidth/2 && trees.get(i)[0] < screenX + width + Const.maxTreeWidth/2){
 					canvas.drawBitmap(treeBitmaps[0][trees.get(i)[1]], (float)((trees.get(i)[0] - Const.maxTreeWidth/4 - screenX)*scaleX), (float)((ground.getYFromX(trees.get(i)[0])-(Const.partOfTreeVisible-0.2)*Const.maxTreeHeight-screenY)*scaleY), null);
 					canvas.drawBitmap(treeBitmaps[1][trees.get(i)[2]], (float)((trees.get(i)[0] - Const.maxTreeWidth/2 - screenX)*scaleX), (float)((ground.getYFromX(trees.get(i)[0])-Const.partOfTreeVisible*Const.maxTreeHeight - screenY)*scaleY), null);
 				}
 			}
 		}
 	}
 
 	//Draw dust if needed
 	public void renderDust(Canvas canvas){
 		if(time - latestDashTime < Const.dustDecayTime){
 			cloaker.setAlpha((int)(255 - 255.*(time-latestDashTime)/Const.dustDecayTime));
 			canvas.drawBitmap(dustBitmap, (float)((dashX - Const.dustWidth/2 - screenX)*scaleX), (float)((dashY - Const.dustHeight/3 - screenY)*scaleY), cloaker);
 		}
 	}
 
 	//Draw finishflag
 	public void renderFlag(Canvas canvas, int index){
 		if(levelLoader.getFinishX() < screenX + width + Const.finishFlagWidth/2)
 			canvas.drawBitmap(flagBitmaps[index], (float)((levelLoader.getFinishX() - index + (index - 1)*Const.finishFlagWidth/2 - screenX)*scaleX), (float)((ground.getYFromX(levelLoader.getFinishX())-Const.partOfFinishFlagVisible*Const.finishFlagHeight - screenY)*scaleY), null);
 	}
 
 	//Draw butterfly
 	public void renderButterfly(Canvas canvas){
 		for(int i = 0; i < butterflies.size(); i++){
 			if(butterflies.get(i).getX() > screenX - Const.butterflySize/2 && butterflies.get(i).getX() < screenX + width + Const.butterflySize/2){
 				renderMatrix.setRotate(butterflies.get(i).getRot(), butterflyBitmaps[0].getWidth()/2, butterflyBitmaps[0].getHeight()/2);
 				renderMatrix.postTranslate((float)((butterflies.get(i).getX()-Const.butterflySize/2 - screenX)*scaleX), (float)((butterflies.get(i).getY()-Const.butterflySize/2 - screenY)*scaleY));
 				canvas.drawBitmap(butterflyBitmaps[(int)(Math.abs(butterflies.get(i).getCounter() % 2))], renderMatrix, null);
 			}
 		}
 	}
 
 	//Draw mentor
 	public void renderMentor(Canvas canvas){
 		float feetAngle;
 		float aimAngle = (float)mentor.getAim();
 		if(mentor.isTouchingGround()){
 			feetAngle = (float)(180/Math.PI*Math.sin((double)mentor.getStepCount()/mentor.getNumberOfSteps()*Math.PI));
 		} else {
 			feetAngle = Const.jumpFeetAngle;
 		}
 		//Draw all the mentor parts
 		if(mentor.isFacingRight()){
 			//Draw back foot
 			renderMatrix.setRotate(-feetAngle, footBitmap.getWidth()/2, footBitmap.getHeight()/2);
 			renderMatrix.postTranslate((float)((mentor.getXPos() - mentor.getWidth()*(0.5-Const.footXAxis-Const.backFootOffset) - mentor.getWidth()*Const.footRadius*Math.sin(-feetAngle*Math.PI/180) - screenX)*scaleX), (float)((mentor.getYPos() + mentor.getHeight()*(Const.footYAxis-0.5) + mentor.getHeight()*Const.footRadius*Math.cos(-feetAngle*Math.PI/180) - screenY)*scaleY));
 			canvas.drawBitmap(footBitmap, renderMatrix, null);
 			//Draw body
 			renderMatrix.setTranslate((float)((mentor.getXPos() - mentor.getWidth()*(0.5-Const.bodyXOffset) - screenX)*scaleX), (float)((mentor.getYPos() - mentor.getHeight()*(0.5-Const.bodyYOffset + Const.breathOffset*Math.sin((double)mentor.getBreathCount()/mentor.getBreathMax()*2*Math.PI)) - screenY)*scaleY));
 			canvas.drawBitmap(mentorBodyBitmap, renderMatrix, null);
 			//Draw eyes and mouth
 			renderMatrix.setTranslate((float)((mentor.getXPos() - mentor.getWidth()*(0.5-Const.eyeBeardXOffset) - screenX)*scaleX), (float)((mentor.getYPos() - mentor.getHeight()*(0.5-Const.eyeBeardYOffset) - screenY)*scaleY));
 			canvas.drawBitmap(eyeBeardBitmap, renderMatrix, null);
 			//Draw front foot
 			renderMatrix.setRotate(feetAngle, footBitmap.getWidth()/2, footBitmap.getHeight()/2);
 			renderMatrix.postTranslate((float)((mentor.getXPos() - mentor.getWidth()*(0.5-Const.footXAxis) - mentor.getWidth()*Const.footRadius*Math.sin(feetAngle*Math.PI/180) - screenX)*scaleX), (float)((mentor.getYPos() + mentor.getHeight()*(Const.footYAxis-0.5) + mentor.getHeight()*Const.footRadius*Math.cos(feetAngle*Math.PI/180) - screenY)*scaleY));
 			canvas.drawBitmap(footBitmap, renderMatrix, null);
 			//Draw pupils
 			renderMatrix.setTranslate((float)((mentor.getXPos() + mentor.getWidth()*(Const.pupilXOffset-0.5)+mentor.getWidth()*Const.pupilRadius*Math.cos(aimAngle*Math.PI/180)-screenX)*scaleX), (float)((mentor.getYPos() + mentor.getHeight()*(Const.pupilYOffset-0.5)+mentor.getHeight()*Const.pupilRadius*Math.sin(aimAngle*Math.PI/180) - screenY)*scaleY));
 			canvas.drawBitmap(pupilBitmap, renderMatrix, null);
 			//Draw weapon
 			renderMatrix.setTranslate((float)((mentor.getXPos() - mentor.getWidth()*(0.5-Const.weaponXAxis-Const.weaponRadius) - screenX)*scaleX), (float)((mentor.getYPos() + mentor.getHeight()*(Const.weaponYAxis-0.5) - screenY)*scaleY));
 			canvas.drawBitmap(handsBitmap, renderMatrix, null);
 		} else {
 			//Draw back foot
 			renderMatrix.setRotate(feetAngle, footBitmapFlipped.getWidth()/2, footBitmapFlipped.getHeight()/2);
 			renderMatrix.postTranslate((float)((mentor.getXPos() - mentor.getWidth()*(0.5-Const.footXAxis+Const.backFootOffset) - mentor.getWidth()*Const.footRadius*Math.sin(Math.PI-feetAngle*Math.PI/180) - screenX)*scaleX), (float)((mentor.getYPos() + mentor.getHeight()*(Const.footYAxis-0.5) + mentor.getHeight()*Const.footRadius*Math.cos(-feetAngle*Math.PI/180) - screenY)*scaleY));
 			canvas.drawBitmap(footBitmapFlipped, renderMatrix, null);
 			//Draw body
 			renderMatrix.setTranslate((float)((mentor.getXPos() + mentor.getWidth()*(0.5-Const.bodyXOffset) - screenX)*scaleX) - bodyBitmapFlipped.getWidth(), (float)((mentor.getYPos() - mentor.getHeight()*(0.5-Const.bodyYOffset + Const.breathOffset*Math.sin((double)mentor.getBreathCount()/mentor.getBreathMax()*2*Math.PI)) - screenY)*scaleY));
 			canvas.drawBitmap(mentorBodyBitmapFlipped, renderMatrix, null);
 			//Draw eyes and mouth
 			renderMatrix.setTranslate((float)((mentor.getXPos() + mentor.getWidth()*(0.5-Const.eyeBeardXOffset) - screenX)*scaleX) - eyeBeardBitmapFlipped.getWidth(), (float)((mentor.getYPos() - mentor.getHeight()*(0.5-Const.eyeBeardYOffset) - screenY)*scaleY));
 			canvas.drawBitmap(eyeBeardBitmapFlipped, renderMatrix, null);
 			//Draw front foot
 			renderMatrix.setRotate(-feetAngle, footBitmapFlipped.getWidth()/2, footBitmapFlipped.getHeight()/2);
 			renderMatrix.postTranslate((float)((mentor.getXPos() - mentor.getWidth()*(0.5-Const.footXAxis) - mentor.getWidth()*Const.footRadius*Math.sin(Math.PI+feetAngle*Math.PI/180) - screenX)*scaleX), (float)((mentor.getYPos() + mentor.getHeight()*(Const.footYAxis-0.5) + mentor.getHeight()*Const.footRadius*Math.cos(feetAngle*Math.PI/180) - screenY)*scaleY));
 			canvas.drawBitmap(footBitmapFlipped, renderMatrix, null);
 			//Draw pupils
 			renderMatrix.setTranslate((float)((mentor.getXPos() + mentor.getWidth()*(Const.pupilXOffset-0.5)+mentor.getWidth()*Const.pupilRadius*Math.cos(aimAngle*Math.PI/180)-screenX)*scaleX), (float)((mentor.getYPos() + mentor.getHeight()*(Const.pupilYOffset-0.5)+mentor.getHeight()*Const.pupilRadius*Math.sin(aimAngle*Math.PI/180) - screenY)*scaleY));
 			canvas.drawBitmap(pupilBitmapFlipped, renderMatrix, null);
 			//Draw weapon
 			renderMatrix.setTranslate((float)((mentor.getXPos()  + mentor.getWidth()*(0.5-Const.weaponXAxis-Const.weaponRadius) - screenX)*scaleX - handsBitmapFlipped.getWidth()), (float)((mentor.getYPos() + mentor.getHeight()*(Const.weaponYAxis-0.5) - screenY)*scaleY));
 			canvas.drawBitmap(handsBitmapFlipped, renderMatrix, null);
 		}
 	}
 
 	//Draw Hints
 	public void renderHint(Canvas canvas){
 		canvas.drawRect((float)((leftStick.getX()+leftStick.getRadius()+5)*scaleX), (float)(((leftStick.getY()+leftStick.getRadius())*scaleY) - (mentorHints.get(currentCheckpoint).size())*textPaint.getTextSize()), (float)((rightStick.getX()-rightStick.getRadius()-5)*scaleX), (float)((leftStick.getY()+leftStick.getRadius())*scaleY), textBackground);
 		for(i = 0; i < mentorHints.get(currentCheckpoint).size(); i++){
 			canvas.drawText(mentorHints.get(currentCheckpoint).get(i), (float)((leftStick.getX()+leftStick.getRadius()+5)*scaleX), (float)(((leftStick.getY()+leftStick.getRadius())*scaleY) - (mentorHints.get(currentCheckpoint).size()-i-1)*textPaint.getTextSize()), textPaint);
 		}
 	}
 
 	public void renderHint(Canvas canvas, String[] hint){
 		canvas.drawRect((float)((leftStick.getX()+leftStick.getRadius()+5)*scaleX), (float)(((leftStick.getY()+leftStick.getRadius())*scaleY) - (hint.length)*textPaint.getTextSize()), (float)((rightStick.getX()-rightStick.getRadius()-5)*scaleX), (float)((leftStick.getY()+leftStick.getRadius())*scaleY), textBackground);
 		for(i = 0; i < hint.length; i++){
 			canvas.drawText(hint[i], (float)((leftStick.getX()+leftStick.getRadius()+5)*scaleX), (float)(((leftStick.getY()+leftStick.getRadius())*scaleY) - (hint.length-i-1)*textPaint.getTextSize()), textPaint);
 		}
 	}
 
 	public void renderDarkness(Canvas canvas, int percent){
 		canvas.drawColor(Color.argb((int)(2.55*percent), 0, 0, 0));
 	}
 
 	//Draw Hints
 	public void renderTime(Canvas canvas){
 		if(viewStatistics){
 			int secs = time*MainThread.updateInterval/1000;
 			int mins = secs/60;
 			secs =     secs%60;
 			String secString, minString;
 			if(secs < 10){
 				secString = "0" + secs;
 			} else {
 				secString = "" + secs;
 			}
 			if(mins < 10){
 				minString = "0" + mins;
 			} else {
 				minString = "" + mins;
 			}
 
 			canvas.drawRect((float)((width/2-Const.timeAreaWidth/2)*scaleX), (float)(Const.HUDPadding*scaleY), (float)((width/2+Const.timeAreaWidth/2)*scaleX), (float)((Const.HUDPadding+Const.timeAreaHeight)*scaleY), textBackground);
 			canvas.drawText(minString + ":" + secString, (float)(width/2*scaleX), (float)((Const.HUDPadding+Const.timeAreaHeight - (Const.timeAreaHeight-timePaint.getTextSize()/scaleX)/2)*scaleY), timePaint);
 		}
 	}
 
 	//Draw bird
 	public void renderBird(Canvas canvas){
 		if(level == 0){
 			if(bird != null){
 				renderMatrix = new Matrix();
 				if(!bird.isAlive()){
 					renderMatrix.setRotate((float)bird.getRotation(), (float)(birdBitmap.getWidth()/2), (float)(birdBitmap.getHeight()/2));
 				}
 				renderMatrix.postTranslate((float)((bird.getX() - Bird.getWidth()/2 - screenX)*scaleX), (float)((bird.getY() - Bird.getHeight() - screenY)*scaleY));
 				canvas.drawBitmap(birdBitmap, renderMatrix, null);
 			}
 		} else {
 			for(int i = 0; i < birds.size(); i++){
 				renderMatrix = new Matrix();
 				if(!birds.get(i).isAlive()){
 					renderMatrix.setRotate((float)birds.get(i).getRotation(), (float)(birdBitmap.getWidth()/2), (float)(birdBitmap.getHeight()/2));
 				}
 				renderMatrix.postTranslate((float)((birds.get(i).getX() - Bird.getWidth()/2 - screenX)*scaleX), (float)((birds.get(i).getY() - Bird.getHeight() - screenY)*scaleY));
 				canvas.drawBitmap(birdBitmap, renderMatrix, null);
 			}
 		}
 	}
 
 	public void renderFruit(Canvas canvas){
 		if(tutorialFruitY < 200){
 			canvas.drawBitmap(fruitBitmap, (float)((Const.tutorialFruitX-screenX)*scaleX), (float)((tutorialFruitY-screenY)*scaleY), null);
 		}
 	}
 	@Override
 	public boolean onTouchEvent(MotionEvent e){
 		if(!loading && showHUD){
 			if(!gameStarted)
 				gameStarted = true;
 			index = e.getActionIndex();
 			id = e.getPointerId(index);
 
 			switch(e.getActionMasked()){
 			case MotionEvent.ACTION_DOWN:
 				touchX = e.getX()/scaleX;
 				touchY = e.getY()/scaleY;
 
 				if(touchX > width/2){
 					rightStick.handleTouch(touchX, touchY);
 					rightStickId = id;
 				} else {
 					leftStick.handleTouch(touchX, touchY);
 					leftStickId = id;
 				}
 
 				break;
 
 			case MotionEvent.ACTION_POINTER_DOWN:
 
 				touchX = e.getX(index)/scaleX;
 				touchY = e.getY(index)/scaleY;
 
 
 				if(touchX > width/2){
 					rightStick.handleTouch(touchX, touchY);
 					rightStickId = id;
 				} else {
 					leftStick.handleTouch(touchX, touchY);
 					leftStickId = id;
 				}
 
 				break;
 
 			case MotionEvent.ACTION_MOVE:
 
 				for(index=0; index<e.getPointerCount(); index++) {
 					id=e.getPointerId(index);
 					touchX = e.getX(index)/scaleX;
 					touchY = e.getY(index)/scaleY; 
 					if(id == rightStickId) {
 						if(touchX > width/2){
 							rightStick.handleTouch(touchX, touchY);
 							rightStickId = id;
 						} 
 					}
 					else if(id == leftStickId){
 						if(touchX < width/2){
 							leftStick.handleTouch(touchX, touchY);
 							leftStickId = id;
 						}
 					}
 				}
 
 				break;
 
 			case MotionEvent.ACTION_UP:
 				rightStick.release();
 				leftStick.release();
 				leftStickId = INVALID_POINTER;
 				rightStickId = INVALID_POINTER;
 
 				break;
 
 
 			case MotionEvent.ACTION_POINTER_UP:
 				if(id == leftStickId){
 					leftStick.release();
 					leftStickId = INVALID_POINTER;
 				}
 				if(id == rightStickId){
 					rightStick.release();
 					rightStickId = INVALID_POINTER;
 				}
 
 				break;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		new LoadOperation().execute("");
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		//End the thread
 		boolean retry = true;
 		while(retry){
 			try{
 				thread.join();
 				retry = false;
 			} catch(InterruptedException e){
 
 			}
 		}
 	}
 
 	//Returns ground
 	public Ground getGround() {
 		return ground;
 	}
 
 	//Returns platform
 	public ArrayList<Platform> getPlatforms() {
 		return platforms;
 	}
 
 	//Pause the game
 	public void pause(){
 		if(theme != null && theme.isPlaying()){
 			theme.stop();
 		}
 		if(earthquake != null && earthquake.isPlaying()){
 			earthquake.stop();
 		}
 
 		ambientSM.stop(1);
 		ambientSM.stop(2);
 		healthSM.stop(1);
 		thread.setRunning(false);
 	}
 
 	//Resume the game
 	public void resume(){
 		if(theme!=null)
 			playTheme();
 		if (thread.getState()==Thread.State.TERMINATED) { 
 			thread = new MainThread(getHolder(),this);
 
 		}
 		thread.setRunning(true);
 		try{thread.start();} catch(IllegalThreadStateException err){}
 		if(mentorDeathTimer > 225){
 			earthquake = MediaPlayer.create(getContext(), R.raw.earthquake);
 			earthquake.setLooping(true);
 			earthquake.setVolume((float)effectVolume, (float)effectVolume);
 			earthquake.start();
 		}
 	}
 
 	private class LoadOperation extends AsyncTask<String, Integer, String> {
 
 		@Override
 		protected String doInBackground(String... params) {
 			//Calculate the scale that we will use to render the game
 			scaleY = (double)getHeight()/height;
 			scaleX = (double)getWidth()/width;
 			BitmapFactory.Options mNoScale = new BitmapFactory.Options();
 			mNoScale.inScaled = false;
 
 			//Load Bitmaps
 			bodyBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.protagonist_body, mNoScale), (int)(protagonist.getWidth()*scaleX*Const.bodyXScale), (int)(protagonist.getHeight()*scaleY*Const.bodyYScale), true);
 			mentorBodyBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mentor_body, mNoScale), (int)(protagonist.getWidth()*scaleX*Const.bodyXScale), (int)(protagonist.getHeight()*scaleY*Const.bodyYScale), true);
 			eyeMouthBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.protagonist_eye_mouth, mNoScale), (int)(protagonist.getWidth()*scaleX*Const.eyeMouthXScale), (int)(protagonist.getHeight()*scaleY*Const.eyeMouthYScale), true);
 			eyeBeardBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.eye_beard, mNoScale), (int)(protagonist.getWidth()*scaleX*Const.eyeBeardXScale), (int)(protagonist.getHeight()*scaleY*Const.eyeBeardYScale), true);
 			footBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.protagonist_foot, mNoScale), (int)(protagonist.getWidth()*scaleX*Const.footXScale), (int)(protagonist.getHeight()*scaleY*Const.footYScale), true);
 			weaponBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.protagonist_weapon, mNoScale), (int)(protagonist.getWidth()*scaleX*Const.weaponXScale), (int)(protagonist.getHeight()*scaleY*Const.weaponYScale), true);
 			handsBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.protagonist_hand_of_god, mNoScale), (int)(protagonist.getWidth()*scaleX*Const.weaponXScale), (int)(protagonist.getHeight()*scaleY*Const.weaponYScale), true);
 			pupilBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.protagonist_pupil, mNoScale), (int)(protagonist.getWidth()*scaleX*Const.pupilXScale), (int)(protagonist.getHeight()*scaleY*Const.pupilYScale), true);
 			stickBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.stick, mNoScale), (int)(2*leftStick.getRadius()*scaleX), (int)(2*leftStick.getRadius()*scaleX), true);
 			bulletBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bullet, mNoScale), (int)(Bullet.getRadius()*2*scaleX), (int)(Bullet.getRadius()*2*scaleY), true);
 			sunBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.sun, mNoScale), (int)(Const.sunSize*scaleX), (int)(Const.sunSize*scaleX), true);
 			birdBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bird, mNoScale), (int)(Bird.getWidth()*scaleX), (int)(Bird.getHeight()*scaleY), true);
 			flagBitmaps = new Bitmap[]{
 					Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.flag_back, mNoScale), (int)(Const.finishFlagWidth/2*scaleX), (int)(Const.finishFlagHeight*scaleY), true),
 					Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.flag_front, mNoScale), (int)(Const.finishFlagWidth/2*scaleX), (int)(Const.finishFlagHeight*scaleY), true)
 			};
 			treeBitmaps = new Bitmap[][]{{
 				Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.base_1, mNoScale), (int)(Const.maxTreeWidth/2*scaleX), (int)(Const.maxTreeHeight*0.8*scaleY), true),
 				Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.base_2, mNoScale), (int)(Const.maxTreeWidth/2*scaleX), (int)(Const.maxTreeHeight*0.8*scaleY), true),
 				Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.base_3, mNoScale), (int)(Const.maxTreeWidth/2*scaleX), (int)(Const.maxTreeHeight*0.8*scaleY), true)
 			},
 			{
 				Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.top_1, mNoScale), (int)(Const.maxTreeWidth*scaleX), (int)(Const.maxTreeHeight*0.7*scaleY), true),
 				Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.top_2, mNoScale), (int)(Const.maxTreeWidth*scaleX), (int)(Const.maxTreeHeight*0.7*scaleY), true),
 				Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.top_3, mNoScale), (int)(Const.maxTreeWidth*scaleX), (int)(Const.maxTreeHeight*0.7*scaleY), true)
 			}};
 			rockBitmap = new Bitmap[]{
 					Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.stone_1, mNoScale), (int)(Const.maxRockSize*scaleX), (int)(Const.maxRockSize*scaleY), true),
 					Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.stone_2, mNoScale), (int)(Const.maxRockSize*scaleX), (int)(Const.maxRockSize*scaleY), true),
 					Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.stone_3, mNoScale), (int)(Const.maxRockSize*scaleX), (int)(Const.maxRockSize*scaleY), true),
 					Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.stone_4, mNoScale), (int)(Const.maxRockSize*scaleX), (int)(Const.maxRockSize*scaleY), true),
 			};
 			cloudBitmaps = new Bitmap[]{
 					Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cloud_1, mNoScale), (int)(Const.maxCloudWidth*scaleX), (int)(Const.maxCloudHeight*scaleY), true),
 					Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cloud_2, mNoScale), (int)(Const.maxCloudWidth*scaleX), (int)(Const.maxCloudHeight*scaleY), true),
 					Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cloud_3, mNoScale), (int)(Const.maxCloudWidth*scaleX), (int)(Const.maxCloudHeight*scaleY), true),
 					Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cloud_4, mNoScale), (int)(Const.maxCloudWidth*scaleX), (int)(Const.maxCloudHeight*scaleY), true),
 					Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cloud_5, mNoScale), (int)(Const.maxCloudWidth*scaleX), (int)(Const.maxCloudHeight*scaleY), true),
 
 			};
 
 			flowerBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.flower, mNoScale), (int)(Const.flowerSize*scaleX), (int)(Const.flowerSize*scaleY), true);
 			dustBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.dash_dust, mNoScale), (int)(Const.dustWidth*scaleX), (int)(Const.dustHeight*scaleY), true);
 			skeletonBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.skeleton, mNoScale), (int)(Const.skeletonSize*scaleX), (int)(Const.skeletonSize*scaleY), true);
 			fruitBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fruit, mNoScale), (int)(Const.tutorialFruitSize*scaleX), (int)(Const.tutorialFruitSize*scaleY), true);
 			signBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.sign, mNoScale), (int)(Const.hintSize*scaleX), (int)(Const.hintSize*scaleY), true);
 			butterflyBitmaps = new Bitmap[]{
 					Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.butterfly_in, mNoScale), (int)(Const.butterflySize*scaleX), (int)(Const.butterflySize*scaleY), true),
 					Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.butterfly_out, mNoScale), (int)(Const.butterflySize*scaleX), (int)(Const.butterflySize*scaleY), true)
 			};
 
 			//Drone
 			enemyBodyBitmap[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_body, mNoScale), (int)(Enemy.getBaseWidth()*Const.enemyBodyXScale*scaleX), (int)(Enemy.getBaseHeight()*Const.enemyBodyYScale*scaleY), true);	
 			enemyEyeMouthBitmap[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_eye_mouth, mNoScale), (int)(Enemy.getBaseWidth()*Const.enemyEyeMouthXScale*scaleX), (int)(Enemy.getBaseHeight()*Const.enemyEyeMouthYScale*scaleY), true);
 			enemyLeftArmBitmap[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_arm, mNoScale), (int)(Enemy.getBaseWidth()*Const.enemyArmXScale*scaleX), (int)(Enemy.getBaseHeight()*Const.enemyArmYScale*scaleY), true);
 			enemyFootBitmap[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_foot, mNoScale), (int)(Enemy.getBaseWidth()*Const.enemyFootXScale*scaleX), (int)(Enemy.getBaseHeight()*Const.enemyFootYScale*scaleY), true);
 			enemyPupilBitmap[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_pupil, mNoScale), (int)(Enemy.getBaseWidth()*Const.enemyPupilXScale*scaleX), (int)(Enemy.getBaseHeight()*Const.enemyPupilYScale*scaleY), true);
 
 			//Ninja
 			enemyBodyBitmap[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_body, mNoScale), (int)(Enemy.getBaseWidth()*Enemy.getScaleNinja()*Const.enemyBodyXScale*scaleX), (int)(Enemy.getBaseHeight()*Enemy.getScaleNinja()*Const.enemyBodyYScale*scaleY), true);	
 			enemyEyeMouthBitmap[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_eye_mouth, mNoScale), (int)(Enemy.getBaseWidth()*Enemy.getScaleNinja()*Const.enemyEyeMouthXScale*scaleX), (int)(Enemy.getBaseHeight()*Enemy.getScaleNinja()*Const.enemyEyeMouthYScale*scaleY), true);
 			enemyLeftArmBitmap[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_arm, mNoScale), (int)(Enemy.getBaseWidth()*Enemy.getScaleNinja()*Const.enemyArmXScale*scaleX), (int)(Enemy.getBaseHeight()*Enemy.getScaleNinja()*Const.enemyArmYScale*scaleY), true);
 			enemyFootBitmap[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_foot, mNoScale), (int)(Enemy.getBaseWidth()*Enemy.getScaleNinja()*Const.enemyFootXScale*scaleX), (int)(Enemy.getBaseHeight()*Enemy.getScaleNinja()*Const.enemyFootYScale*scaleY), true);
 			enemyPupilBitmap[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_pupil, mNoScale), (int)(Enemy.getBaseWidth()*Enemy.getScaleNinja()*Const.enemyPupilXScale*scaleX), (int)(Enemy.getBaseHeight()*Enemy.getScaleNinja()*Const.enemyPupilYScale*scaleY), true);
 
 			//Tank
 			enemyBodyBitmap[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_body, mNoScale), (int)(Enemy.getBaseWidth()*Enemy.getScaleTank()*Const.enemyBodyXScale*scaleX), (int)(Enemy.getBaseHeight()*Enemy.getScaleTank()*Const.enemyBodyYScale*scaleY), true);	
 			enemyEyeMouthBitmap[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_eye_mouth, mNoScale), (int)(Enemy.getBaseWidth()*Enemy.getScaleTank()*Const.enemyEyeMouthXScale*scaleX), (int)(Enemy.getBaseHeight()*Enemy.getScaleTank()*Const.enemyEyeMouthYScale*scaleY), true);
 			enemyLeftArmBitmap[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_arm, mNoScale), (int)(Enemy.getBaseWidth()*Enemy.getScaleTank()*Const.enemyArmXScale*scaleX), (int)(Enemy.getBaseHeight()*Enemy.getScaleTank()*Const.enemyArmYScale*scaleY), true);
 			enemyFootBitmap[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_foot, mNoScale), (int)(Enemy.getBaseWidth()*Enemy.getScaleTank()*Const.enemyFootXScale*scaleX), (int)(Enemy.getBaseHeight()*Enemy.getScaleTank()*Const.enemyFootYScale*scaleY), true);
 			enemyPupilBitmap[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_pupil, mNoScale), (int)(Enemy.getBaseWidth()*Enemy.getScaleTank()*Const.enemyPupilXScale*scaleX), (int)(Enemy.getBaseHeight()*Enemy.getScaleTank()*Const.enemyPupilYScale*scaleY), true);
 
 			//Special enemy accessories
 			enemyArmorBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_armor, mNoScale), (int)(Enemy.getBaseWidth()*Enemy.getScaleTank()*Const.enemyArmorXScale*scaleX), (int)(Enemy.getBaseHeight()*Enemy.getScaleTank()*Const.enemyArmorYScale*scaleY), true);
 			enemyNinjaBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.enemy_ninja, mNoScale), (int)(Enemy.getBaseWidth()*Enemy.getScaleNinja()*Const.enemyNinjaXScale*scaleX), (int)(Enemy.getBaseHeight()*Enemy.getScaleNinja()*Const.enemyNinjaYScale*scaleY), true);
 
 			Matrix m = new Matrix();
 			//Flip images
 			m.setScale(-1, 1);
 
 			bodyBitmapFlipped = Bitmap.createBitmap(bodyBitmap, 0, 0, bodyBitmap.getWidth(), bodyBitmap.getHeight(), m, false);
 			mentorBodyBitmapFlipped = Bitmap.createBitmap(mentorBodyBitmap, 0, 0, mentorBodyBitmap.getWidth(), mentorBodyBitmap.getHeight(), m, false);
 			eyeMouthBitmapFlipped = Bitmap.createBitmap(eyeMouthBitmap, 0, 0, eyeMouthBitmap.getWidth(), eyeMouthBitmap.getHeight(), m, false);
 			eyeBeardBitmapFlipped = Bitmap.createBitmap(eyeBeardBitmap, 0, 0, eyeBeardBitmap.getWidth(), eyeBeardBitmap.getHeight(), m, false);
 			footBitmapFlipped = Bitmap.createBitmap(footBitmap, 0, 0, footBitmap.getWidth(), footBitmap.getHeight(), m, false);
 			weaponBitmapFlipped = Bitmap.createBitmap(weaponBitmap, 0, 0, weaponBitmap.getWidth(), weaponBitmap.getHeight(), m, false);
 			handsBitmapFlipped = Bitmap.createBitmap(handsBitmap, 0, 0, handsBitmap.getWidth(), handsBitmap.getHeight(), m, false);
 			pupilBitmapFlipped = Bitmap.createBitmap(pupilBitmap, 0, 0, pupilBitmap.getWidth(), pupilBitmap.getHeight(), m, false);
 
 			enemyBodyBitmapFlipped[0] = Bitmap.createBitmap(enemyBodyBitmap[0], 0, 0, enemyBodyBitmap[0].getWidth(), enemyBodyBitmap[0].getHeight(), m, false);
 			enemyBodyBitmapFlipped[1] = Bitmap.createBitmap(enemyBodyBitmap[1], 0, 0, enemyBodyBitmap[1].getWidth(), enemyBodyBitmap[1].getHeight(), m, false);
 			enemyBodyBitmapFlipped[2] = Bitmap.createBitmap(enemyBodyBitmap[2], 0, 0, enemyBodyBitmap[2].getWidth(), enemyBodyBitmap[2].getHeight(), m, false);
 
 			enemyEyeMouthBitmapFlipped[0] = Bitmap.createBitmap(enemyEyeMouthBitmap[0], 0, 0, enemyEyeMouthBitmap[0].getWidth(), enemyEyeMouthBitmap[0].getHeight(), m, false);
 			enemyEyeMouthBitmapFlipped[1] = Bitmap.createBitmap(enemyEyeMouthBitmap[1], 0, 0, enemyEyeMouthBitmap[1].getWidth(), enemyEyeMouthBitmap[1].getHeight(), m, false);
 			enemyEyeMouthBitmapFlipped[2] = Bitmap.createBitmap(enemyEyeMouthBitmap[2], 0, 0, enemyEyeMouthBitmap[2].getWidth(), enemyEyeMouthBitmap[2].getHeight(), m, false);
 
 			enemyFootBitmapFlipped[0] = Bitmap.createBitmap(enemyFootBitmap[0], 0, 0, enemyFootBitmap[0].getWidth(), enemyFootBitmap[0].getHeight(), m, false);
 			enemyFootBitmapFlipped[1] = Bitmap.createBitmap(enemyFootBitmap[1], 0, 0, enemyFootBitmap[1].getWidth(), enemyFootBitmap[1].getHeight(), m, false);
 			enemyFootBitmapFlipped[2] = Bitmap.createBitmap(enemyFootBitmap[2], 0, 0, enemyFootBitmap[2].getWidth(), enemyFootBitmap[2].getHeight(), m, false);
 
 			enemyRightArmBitmap[0] = Bitmap.createBitmap(enemyLeftArmBitmap[0], 0, 0, enemyLeftArmBitmap[0].getWidth(), enemyLeftArmBitmap[0].getHeight(), m, false);
 			enemyRightArmBitmap[1] = Bitmap.createBitmap(enemyLeftArmBitmap[1], 0, 0, enemyLeftArmBitmap[1].getWidth(), enemyLeftArmBitmap[1].getHeight(), m, false);		
 			enemyRightArmBitmap[2] = Bitmap.createBitmap(enemyLeftArmBitmap[2], 0, 0, enemyLeftArmBitmap[2].getWidth(), enemyLeftArmBitmap[2].getHeight(), m, false);
 
 			enemyArmorBitmapFlipped = Bitmap.createBitmap(enemyArmorBitmap, 0, 0, enemyArmorBitmap.getWidth(), enemyArmorBitmap.getHeight(), m, false);
 			enemyNinjaBitmapFlipped = Bitmap.createBitmap(enemyNinjaBitmap, 0, 0, enemyNinjaBitmap.getWidth(), enemyNinjaBitmap.getHeight(), m, false);
 
 
 			textPaint.setTextSize((int)(4*scaleY));
 			timePaint.setTextSize((int)(Const.timeAreaHeight*scaleY));
 
 			loadSounds();
 			playTheme();
 
 			return "Executed";
 		}      
 
 		@Override
 		protected void onPostExecute(String result) {
 			loading = false;
 		}
 
 		@Override
 		protected void onPreExecute() {
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 
 		}
 	}   
 	public Stick getLeftStick(){
 		return leftStick;
 	}
 
 }
