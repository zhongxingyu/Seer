 package com.klusman.keepup.screens;
 
 import aurelienribon.tweenengine.BaseTween;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenCallback;
 import aurelienribon.tweenengine.TweenEquations;
 import aurelienribon.tweenengine.TweenManager;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.g3d.model.Animation;
 import com.badlogic.gdx.math.Interpolation;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.collision.Ray;
 import com.badlogic.gdx.utils.Array;
 import com.klusman.keepup.Ball;
 import com.klusman.keepup.Bomb;
 import com.klusman.keepup.FreezeMotionTimer;
 import com.klusman.keepup.LifeMarks;
 import com.klusman.keepup.MainKeepUp;
 import com.klusman.keepup.ShieldBoost;
 import com.klusman.keepup.healthKit;
 import com.klusman.keepup.tweens.SpriteTween;
 
 public class Game implements Screen, InputProcessor {
 
 	MainKeepUp game;
 
 	private static String TAG = "KeepUp";
 	public static final int GAME_READY = 0; 
 	public static final int GAME_RUNNING = 1; 
 	public static final int GAME_PAUSED = 2; 
 	public static final int GAME_OVER = 4;
 
 	public static int gameState;
 
 	private OrthographicCamera camera;
 	static float x = Gdx.graphics.getWidth();
 	static float y = Gdx.graphics.getHeight();
 	static float screenRatio = y/x;
 	public static final int screenXRefactor = 1000;
 	public static final int screenYRefactor = (int) (screenRatio * screenXRefactor);;
 	TweenManager manager;
 
 
 
 
 
 	private SpriteBatch batch;
 
 	float deltaTime;
 	float elapsedTime;
 	float FreezeTimer = 4f;
 	float resourceTimer = 7f;
 	float scoreTime = 2f;
 	float scoreTimeInterval = 1f;
 	float deltaShieldTime;
 	float deltaFreezeTime;
 	float shieldedTime;
 	float bombTime = 2f;
 	float deltaBombTime;
 
 	float ticker;
 	float timeLimit = 20.0f;
 	float invincibilityTime;
 
 	int starLoop;
 	int SCORE;
 
 	double randNumXLoc;
 	double randNumSize;
 	double randNumSpeed;
 
 	boolean pauseGame;
 	boolean kidMovable;
 	boolean kidMove;
 	boolean ballCollision;
 	boolean gameOver;
 	boolean kidHit;
 	boolean invincibility;
 	boolean shielded;
 	boolean freeze;
 
 	public Texture bombTx;
 	TextureRegion bombRegion;
 	
 	private Texture bgTx;
 	private Sprite bg;
 
 	private Texture star2Tx;
 	TextureRegion starRegion1;
 	TextureRegion starRegion2;
 	TextureRegion starRegion3;
 	TextureRegion starRegion4;
 	TextureRegion starRegion5;
 	TextureRegion starRegion6;
 	TextureRegion starRegion7;
 	TextureRegion starRegion8;
 	TextureRegion starRegion0;
 	Array<TextureRegion> starHolder;
 
 
 	private Sprite starSprite;
 
 	private Texture psTx;
 	private Sprite ps;
 
 	private Texture goTx;
 	private Sprite go;
 
 	private Texture restartBtnTx;
 	private Sprite restartBtn;
 
 	private Texture kidTx;
 	public static Sprite kid;
 
 	public TextureRegion pauseRegion;
 	public Texture pauseTx;
 	public static Sprite pause;
 
 	public static Sound metalDing;
 	public static Sound buzzer;
 	public static Sound bounce;
 	public static Sound powerUp;
 	public static Sound hardBounce;
 	public static Sound timeBomb;
 
 	public static final int Difficulty_Easy = 1;
 	public static final int Difficulty_Medium = 2;
 	public static final int Difficulty_Hard = 3;
 	
 	int frameLength;
 	int currentFrame;
 	int gameDifficulty = Difficulty_Medium;
 	
 
 	public Array<Ball> Balls;
 	public Array<healthKit> MedKits;
 	public Array<ShieldBoost> Shields;
 	public Array<Bomb> Bombs;
 	public Array<FreezeMotionTimer> Timers;
 	public static Array<LifeMarks> Marks;
 	public Array<Sprite> starArray;
 	Animation starAnimation;
 	TweenCallback cb;
 
 
 	public Game( MainKeepUp game){
 		this.game = game;
 
 		camera = new OrthographicCamera(screenXRefactor, screenYRefactor);
 
 		deltaTime = Gdx.graphics.getDeltaTime();
 		Gdx.input.setInputProcessor(this);
 		Gdx.input.setCatchBackKey(true);
 	}
 
 	@Override
 	public void show() {
 
 		gameState = GAME_RUNNING;
 		invincibility = false;
 		shielded = false;
 		pauseGame = false;
 		gameOver = false;
 		invincibilityTime = 0;
 		shieldedTime = 0;
 		freeze = false;
 		SCORE = 0;
 
 		Balls = new Array<Ball>();
 		MedKits = new Array<healthKit>();
 		Shields = new Array<ShieldBoost>();
 		Timers = new Array<FreezeMotionTimer>();
 		Bombs = new Array<Bomb>();
 		Marks = new Array<LifeMarks>();
 
 
 
 		kidMovable = true;
 		kidMove = false;
 		kidHit = false;
 
 
 		batch = new SpriteBatch();
 		/// AUDIO  ///
 		powerUp = Gdx.audio.newSound(Gdx.files.internal("audio/PowerUp.wav"));
 		metalDing = Gdx.audio.newSound(Gdx.files.internal("audio/Metalping.wav"));
 		buzzer = Gdx.audio.newSound(Gdx.files.internal("audio/Buzzer.wav"));
		bounce = Gdx.audio.newSound(Gdx.files.internal("audio/ballbounce04.wav"));
 		hardBounce = Gdx.audio.newSound(Gdx.files.internal("audio/HardBounce.wav"));
 		timeBomb = Gdx.audio.newSound(Gdx.files.internal("audio/timeBombSound.wav"));
 
 		//// BACKGROUND
 		bgTx = new Texture(Gdx.files.internal("data/bballcourtWhite.png"));
 		bgTx.setFilter(TextureFilter.Linear, TextureFilter.Linear);	
 		TextureRegion bgRegion = new TextureRegion(bgTx, 0, 0, bgTx.getWidth(), bgTx.getHeight() - 90);
 		bg = new Sprite(bgRegion);
 		bg.setSize(screenXRefactor,  screenYRefactor);  
 		bg.setOrigin(bg.getWidth()/2, bg.getHeight()/2);
 		bg.setPosition(0 - bg.getWidth()/2, 0f - bg.getHeight()/2);
 
 		//// PAUSE SCREEN
 		psTx = new Texture(Gdx.files.internal("data/pausedScreen.png"));
 		psTx.setFilter(TextureFilter.Linear, TextureFilter.Linear);	
 		TextureRegion psRegion = new TextureRegion(psTx, 0, 0, psTx.getWidth(), psTx.getHeight());
 		ps = new Sprite(psRegion);
 		ps.setSize(screenXRefactor,  screenYRefactor);  
 		ps.setOrigin(ps.getWidth()/2, ps.getHeight()/2);
 		ps.setPosition(0 - ps.getWidth()/2, 0f - ps.getHeight()/2);
 
 		//// GAME OVER SCREEN
 		goTx = new Texture(Gdx.files.internal("data/gameOver2.png"));
 		goTx.setFilter(TextureFilter.Linear, TextureFilter.Linear);	
 		TextureRegion goRegion = new TextureRegion(goTx, 0, 0, goTx.getWidth(), goTx.getHeight());
 		go = new Sprite(goRegion);
 		go.setSize(screenXRefactor,  screenYRefactor);  
 		go.setOrigin(go.getWidth()/2, go.getHeight()/2);
 		go.setPosition(0 - go.getWidth()/2, 0f - go.getHeight()/2);
 
 		//// KID
 		kidTx = new Texture(Gdx.files.internal("data/kid.png"));
 		kidTx.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		TextureRegion kidRegion = new TextureRegion(kidTx, 0, 0, kidTx.getWidth(), kidTx.getHeight());
 		float stretchRatioKid = (float) kidTx.getHeight() / kidTx.getWidth();
 		kid = new Sprite(kidRegion);
 		kid.setSize(110f , 110 * stretchRatioKid );
 		kid.setOrigin(kid.getWidth()/2, kid.getHeight()/2);
 		kid.setPosition(0 - (kid.getWidth()/2), -screenYRefactor/2 );	
 
 		//// ExplosionTexture
 		bombTx = new Texture(Gdx.files.internal("data/bomb.png"));
 		bombTx.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		bombRegion = new TextureRegion(bombTx, 0, 64, bombTx.getWidth(), 64);
 
 		//// PAUSE
 		pauseTx = new Texture(Gdx.files.internal("data/PauseTabs.png"));
 		pauseTx.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		pauseRegion = new TextureRegion(pauseTx, 0, 0, pauseTx.getWidth(), 64);
 		pause = new Sprite(pauseRegion);
 		pause.setSize(150f , 100f );
 		pause.setOrigin(pause.getWidth()/2, pause.getHeight()/2);
 		pause.setPosition(0 - pause.getWidth()/2, screenYRefactor/2 - pause.getHeight());	
 
 		//// STAR SPRITE
 		starHolder = new Array<TextureRegion>();
 		star2Tx = new Texture(Gdx.files.internal("data/stars2.png"));
 		star2Tx.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		// Hard coded regions (I was having trouble understanding so I did it the long route) /////
 		starRegion1 = new TextureRegion(star2Tx, 0, 0, star2Tx.getWidth(), 64);
 		starHolder.add(starRegion1);
 		starRegion2 = new TextureRegion(star2Tx, 0, 64, star2Tx.getWidth(), 64);
 		starHolder.add(starRegion2);
 		starRegion3 = new TextureRegion(star2Tx, 0, 128, star2Tx.getWidth(), 64);
 		starHolder.add(starRegion3);
 		starRegion4 = new TextureRegion(star2Tx, 0, 192, star2Tx.getWidth(), 64);
 		starHolder.add(starRegion4);
 		starRegion5 = new TextureRegion(star2Tx, 0, 256, star2Tx.getWidth(), 64);
 		starHolder.add(starRegion5);
 		starRegion6 = new TextureRegion(star2Tx, 0, 320, star2Tx.getWidth(), 64);
 		starHolder.add(starRegion6);
 		starRegion7 = new TextureRegion(star2Tx, 0, 384, star2Tx.getWidth(), 64);
 		starHolder.add(starRegion7);
 		starRegion8 = new TextureRegion(star2Tx, 0, 448, star2Tx.getWidth(), 64);
 		starHolder.add(starRegion8);
 		starRegion0 = new TextureRegion(star2Tx, 0, 512, star2Tx.getWidth(), 64);
 		starHolder.add(starRegion0);
 
 		starSprite = new Sprite(starRegion0);
 		starSprite.setSize(100f , 100f );
 		starSprite.setOrigin(starSprite.getWidth()/2, starSprite.getHeight()/2);
 		starSprite.setPosition(kid.getX(), kid.getY() + kid.getHeight());	
 		starLoop = 0;
 
 		//// Restart Btn
 		restartBtnTx = new Texture(Gdx.files.internal("data/restartBtn.png"));
 		restartBtnTx.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		TextureRegion restartRegion = new TextureRegion(restartBtnTx, 0, 0, restartBtnTx.getWidth(), restartBtnTx.getHeight());
 		restartBtn = new Sprite(restartRegion);
 		restartBtn.setSize(300f , 150f );
 		restartBtn.setOrigin(restartBtn.getWidth()/2, restartBtn.getHeight()/2);
 		restartBtn.setPosition(0 - restartBtn.getWidth()/2, -600);	
 		
 		Tween.registerAccessor(Sprite.class, new SpriteTween());
 		manager = new TweenManager();
 		
 		cb = new TweenCallback() {
 			@Override
 			public void onEvent(int type, BaseTween<?> source) {
 				Gdx.app.log(MainKeepUp.TAG, "TWEEN COMPLETE");
 				//tweenCompleted();  // what method to call when Event is triggered
 			}
 		};
 	}
 
 	@Override
 	public void render(float delta) {
 
 		switch (gameState) {
 		case GAME_READY:
 			//Gdx.app.log(TAG, "Game Rdy");
 			gameReady();
 			break;
 		case GAME_RUNNING:
 			updateBallTimer(deltaTime);
 			updateResourceTimer(deltaTime);
 			//Gdx.app.log(TAG, "Game Running");
 			gameRunning();
 			break;
 		case GAME_PAUSED:
 			//Gdx.app.log(TAG, "Game Paused");
 			gamePaused();
 			break;
 		case GAME_OVER:
 			//Gdx.app.log(TAG, "Game Over");
 			gameOver = true;
 			gameOver();
 			break;
 		}
 	}
 
 
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void hide() {
 
 	}
 
 	@Override
 	public void pause() {
 
 	}
 
 	@Override
 	public void resume() {
 
 	}
 	
 	@Override
 	public void dispose() {
 		batch.dispose();
 		bgTx.dispose();
 		kidTx.dispose();
 		goTx.dispose();
 		pauseTx.dispose();
 		psTx.dispose();
 		restartBtnTx.dispose();
 		star2Tx.dispose();
 
 		if(Balls.size > 0){
 			for(Ball ball: Balls) {
 				ball.ballTx.dispose();
 			};
 		}
 
 		if(MedKits.size > 0){
 			for(healthKit kit: MedKits) {
 				kit.kitTx.dispose();
 			};
 		}
 
 		if(Shields.size > 0){
 			for(ShieldBoost shield: Shields) {
 				shield.shieldTx.dispose();
 			};
 		}
 
 		if(Timers.size > 0){
 			for(FreezeMotionTimer timer: Timers) {
 				timer.timerTx.dispose();
 			};
 		}
 
 		if(Bombs.size > 0){
 			for(Bomb bomb: Bombs) {
 				bomb.bombTx.dispose();
 			};
 		}
 
 		if(Marks.size > 0){
 			for(LifeMarks marks: Marks) {
 				marks.lifeTx.dispose();
 			};
 		}
 	}
 
 	public void bombExplodeTween(Sprite bombSprite){
 		Tween.to(bombSprite, SpriteTween.SCALE_XY, 2f)
 		.target(120, 120)
 		.ease(TweenEquations.easeInQuad)
 		.setCallback(cb) 
 		.setCallbackTriggers(TweenCallback.COMPLETE)
 		.start(manager);  // start the tween using the passed in manager
 	}
 
 	public void addLifeMark(){
 		LifeMarks mark = new LifeMarks();
 		Marks.add(mark);
 	}
 
 	public void removeLifeMark(){
 		int size = Marks.size;
 		if(size > 0){
 			Marks.removeIndex(size - 1);
 		}
 	}
 
 
 	public float getRandomSize(){
 		float r;
 		randNumSize = Math.random();  // random 0.0 to 1.0
 		if (randNumSize < 0.01){
 			randNumSize = 0.01;
 		}
 		double rand = (randNumSize * 100);  // 1 to 1
 
 		if (rand <= 40){   // check if too small (no smaller then 40)
 			r = (float)rand + 40; // Makes a number greater than 41
 			return r;
 		} else if (rand >= 90){  // makes sure its not to big (no larger than 90) 
 			r = (float)rand - 10; // makes a number less than 90
 			return r;
 		}else{
 			r = (float)rand;
 			return r;
 		}
 	}
 
 	public float getRandomSpeed(){
 		randNumSpeed = Math.random();  // 0.0 to 1.0
 		if(randNumSpeed < 0.1){
 			randNumSpeed = 0.1;
 		}
 		float sp = (float) ((randNumSize * 5) + 3);
 		return sp;
 	}
 
 
 
 	public float getRandomXLocation(){
 		float xFloat;
 		randNumXLoc = Math.random(); // 0.0 thru 1.0
 		if(randNumXLoc == 0){  
 			randNumXLoc = 0.01f;  // ensure not 0
 		}
 
 		int xLoc = (int)(randNumXLoc * screenXRefactor);  // from 1 to 1000 
 
 		if(xLoc <= (screenXRefactor/2)){  // checks for the half mark 
 			xFloat = (float) (xLoc * -1);   // make neg and float for ball position
 			return xFloat;
 		}else{
 			xFloat = (float) (xLoc - (screenXRefactor/2));  // if over half (500) subtract half for pos numbers
 			return xFloat;
 		}
 	}
 
 
 
 	public void makeNewBall(){
 		Ball ball = new Ball(getRandomXLocation(), getRandomSize(), getRandomSpeed());
 		Balls.add(ball);
 	}
 
 	public void makeNewKit( ){
 		Gdx.app.log(MainKeepUp.TAG, "Make New Health Kit");
 		healthKit kit = new healthKit(getRandomXLocation(), getRandomSpeed());
 		MedKits.add(kit);
 	}
 
 	public void makeNewShield(){
 		Gdx.app.log(MainKeepUp.TAG, "Make New Shield");
 		ShieldBoost shield = new ShieldBoost(getRandomXLocation(), getRandomSpeed());
 		Shields.add(shield);
 	}
 
 	public void makeNewTimeClock(){
 		Gdx.app.log(MainKeepUp.TAG, "Make New Time Clock");
 		FreezeMotionTimer timeclock = new FreezeMotionTimer(getRandomXLocation(), getRandomSpeed());
 		Timers.add(timeclock);
 	}
 
 	public void makeNewBomb(){
 		Gdx.app.log(MainKeepUp.TAG, "Make New Bomb");
 		Bomb bomb = new Bomb(getRandomXLocation(), getRandomSpeed());
 		Bombs.add(bomb);
 		Gdx.app.log(MainKeepUp.TAG, "bombCount :" + Bombs.size);
 	}
 
 
 	public void updateSTARS(float dt){
 		ticker+=dt;
 		if(ticker>timeLimit){
 			ticker-=timeLimit;
 			currentFrame=(currentFrame+1);
 			starSprite.setRegion(starHolder.get(currentFrame));
 			kid.rotate(45);
 			if(currentFrame == starHolder.size - 1){
 				kidHit= false;
 				currentFrame = 0;
 				kid.setRotation(0);
 			} 
 		}
 	}
 
 
 
 	public void updateBallTimer(float deltaTime){
 		elapsedTime = deltaTime;
 		if(elapsedTime / 5 > Balls.size){  // Make a new ball every 5 seconds
 			makeNewBall();
 		}
 	}
 
 	public void updateResourceTimer(float deltaTime){
 		elapsedTime = deltaTime;
 		if(elapsedTime > resourceTimer){
 			int r = (int) (Math.random() * 100);
 			Gdx.app.log(MainKeepUp.TAG, "RANDOM Resource NUMBER is: " + r);
 			if (r <= 25){
 				makeNewKit();
 			}else if ((r > 25) && (r <= 50)){
 				makeNewShield();
 			}else if ((r > 50) && (r <= 80)){
 				makeNewTimeClock();
 			}else{
 				makeNewBomb();	
 			}
 			resourceTimer = elapsedTime + 7;
 		}
 	}
 
 	public void gameRestart(){
 		dispose();
 		game.setScreen(new Game(game));
 
 	}
 
 
 	public void gameReady(){
 	}
 
 
 	private void gameRunning() {
 		scoreUpdate();
 		checkStrikeOut();
 		deltaTime += Gdx.graphics.getDeltaTime();   
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		camera.update();
 		batch.setProjectionMatrix(camera.combined);
 
 		batch.begin();
 		bg.draw(batch);
 
 		if((invincibility == false) && (shielded == false)){
 			kid.draw(batch);
 		}else{
 			kid.draw(batch, .3f);  // drop Alpha while invincible
 		}
 
 		starSprite.draw(batch);
 
 		if(Balls.size > 0){
 			for(Ball ball: Balls) {
 				ball.draw(batch);
 			};
 		}
 
 		if(Timers.size > 0){
 			for(FreezeMotionTimer timer: Timers) {
 				timer.draw(batch);
 			};
 		}
 
 		if(MedKits.size > 0){
 			for(healthKit kit: MedKits) {
 				kit.draw(batch);
 			};
 		}
 
 		if(Shields.size > 0){
 			for(ShieldBoost shield: Shields) {
 				shield.draw(batch);
 			};
 		}
 
 		if(Bombs.size > 0){
 			for(Bomb bomb: Bombs) {
 				bomb.draw(batch);
 			};
 		}
 
 		if(Marks.size > 0){
 			for(LifeMarks life: Marks) {
 				life.draw(batch);
 			};
 		}
 
 		if(invincibility == true){
 			invincibilityTime = invincibilityTime + deltaShieldTime;
 
 			if(invincibilityTime >= 2){
 				invincibility = false;
 				invincibilityTime = 0;
 				deltaShieldTime = 0;
 			}
 		}
 
 		
 		if(freeze == true){
 			if(elapsedTime >= deltaFreezeTime){
 				freeze = false;
 			}
 		}
 
 		if(shielded == true){	
 			if(deltaTime >= shieldedTime){
 				shielded = false;
 				shieldedTime = 0;
 				deltaShieldTime = 0;
 			}
 		}
 		
 		
 
 		pause.draw(batch);
 		batch.end();
 
 		if(kidHit == true){
 			updateSTARS(deltaTime);
 		}
 
 		if(Balls.size > 0){
 			for(Ball ball: Balls) {
 				ballLoopCheckAndSet(ball);
 			};
 		}
 
 		if(MedKits.size > 0){
 			for(healthKit kit: MedKits) {
 				kitLoopCheckAndSet(kit);
 			};
 		}
 
 		if(Shields.size > 0){
 			for(ShieldBoost shield: Shields) {
 				shieldLoopCheckAndSet(shield);
 			};
 		}
 
 		if(Timers.size > 0){
 			for(FreezeMotionTimer timer: Timers) {
 				timerLoopCheckAndSet(timer);
 			};
 		}
 
 		if(Bombs.size > 0){
 			for(Bomb bomb: Bombs) {
 				if(bomb.getRemoveTrigger() == true){ 
 					
 					Gdx.app.log(MainKeepUp.TAG, "REMOVE BOMB FROM ARRAY");
 					Bombs.removeValue(bomb, true);  // Remove bomb from Array
 					
 				}else{
 					
 					
 					if(bomb.collision == false){ 
 						// do nothing, just wait
 						
 					}else{  // if collision is TRUE
 						bomb.setSizeXY(bomb.getSizeX() + 2, bomb.getSizeY() + 2);
 						bomb.setBombRotationAngle(5);
 						
 						
 						if (bomb.checkBombCountdown(deltaTime) == false){ 
 							Gdx.app.log(MainKeepUp.TAG, "BOMB STILL COUNTING DOWN");	
 						
 						}else{  // If count down is Over
 							
 							bomb.setBombSpriteTexture(bombRegion);
 							Gdx.app.log(MainKeepUp.TAG, "BOMB STILL Explosion Phase");	
 							bomb.bombLastPhaseExplosion(deltaTime);   // if time to blow up	
 						}
 					}						
 					bombLoopCheckAndSet(bomb);					
 				}				
 			};
 		}
 	}
 
 	public void scoreUpdate(){
 		int items = 0;
 		if(elapsedTime >= scoreTime){
 			scoreTime = elapsedTime + scoreTimeInterval;
 			items = Balls.size + MedKits.size + Shields.size + Timers.size;
 			SCORE = SCORE + items;
 			Gdx.app.log(MainKeepUp.TAG, "SCORE = " + SCORE);
 		}
 	}
 
 	private void gameOver() {
 		kidMovable = false;
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		camera.update();
 
 		batch.setProjectionMatrix(camera.combined);
 		batch.begin();
 		bg.draw(batch);
 		kid.draw(batch);
 
 		if(Balls.size > 0){
 			for(Ball ball: Balls) {
 				ball.draw(batch);
 			};
 		}
 
 		if(Timers.size > 0){
 			for(FreezeMotionTimer timer: Timers) {
 				timer.draw(batch);
 			};
 		}
 
 		if(MedKits.size > 0){
 			for(healthKit kit: MedKits) {
 				kit.draw(batch);
 			};
 		}
 
 		if(Shields.size > 0){
 			for(ShieldBoost shield: Shields) {
 				shield.draw(batch);
 			};
 		}
 
 		if(Bombs.size > 0){
 			for(Bomb bomb: Bombs) {
 				bomb.draw(batch);
 			};
 		}
 
 		if(Marks.size > 0){
 			for(LifeMarks life: Marks) {
 				life.draw(batch);
 			};
 		}
 
 		go.draw(batch);
 		restartBtn.draw(batch);
 		batch.end();
 
 		if(Balls.size > 0){
 			for(Ball ball: Balls) {
 				ballLoopCheckOnPause(ball);
 			};
 		}
 
 		if(Timers.size > 0){
 			for(FreezeMotionTimer timer: Timers) {
 				timerLoopCheckOnPause(timer);
 			};
 		}
 
 		if(MedKits.size > 0){
 			for(healthKit kit: MedKits) {
 				kitLoopCheckOnPause(kit);
 			};
 		}
 
 		if(Shields.size > 0){
 			for(ShieldBoost shield: Shields) {
 				shieldLoopCheckOnPause(shield);
 			};
 		}
 
 		if(Bombs.size > 0){
 			for(Bomb bomb: Bombs) {
 				bombLoopCheckOnPause(bomb);
 			};
 		}
 
 	}
 
 
 	private void gamePaused() {
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		camera.update();
 
 		batch.setProjectionMatrix(camera.combined);
 
 		batch.begin();
 
 		bg.draw(batch);
 		kid.draw(batch);
 
 		if(Balls.size > 0){
 			for(Ball ball: Balls) {
 				ball.draw(batch);
 			};
 		}
 
 		if(Timers.size > 0){
 			for(FreezeMotionTimer timer: Timers) {
 				timer.draw(batch);
 			};
 		}
 
 		if(MedKits.size > 0){
 			for(healthKit kit: MedKits) {
 				kit.draw(batch);
 			};
 		}
 
 		if(Shields.size > 0){
 			for(ShieldBoost shield: Shields) {
 				shield.draw(batch);
 			};
 		}
 
 		if(Bombs.size > 0){
 			for(Bomb bomb: Bombs) {
 				bomb.draw(batch);
 			};
 		}
 
 		if(Marks.size > 0){
 			for(LifeMarks life: Marks) {
 				life.draw(batch);
 			};
 		}
 
 		ps.draw(batch);
 		pause.draw(batch);
 
 		batch.end();
 
 		if(Balls.size > 0){
 			for(Ball ball: Balls) {
 				ballLoopCheckOnPause(ball);
 			};
 		}
 
 		if(Timers.size > 0){
 			for(FreezeMotionTimer timer: Timers) {
 				timerLoopCheckOnPause(timer);
 			};
 		}
 
 		if(MedKits.size > 0){
 			for(healthKit kit: MedKits) {
 				kitLoopCheckOnPause(kit);
 			};
 		}
 
 		if(Shields.size > 0){
 			for(ShieldBoost shield: Shields) {
 				shieldLoopCheckOnPause(shield);
 			};
 		}
 
 		if(Bombs.size > 0){
 			for(Bomb bomb: Bombs) {
 				bombLoopCheckOnPause(bomb);
 			};
 		}
 	}
 
 	public void checkStrikeOut(){
 		if(Marks.size >= 3){
 			gameState = GAME_OVER;
 		}
 	}
 
 	public void ballLoopCheckAndSet(Ball ball){
 		Sprite ballSprite = ball.getBallSprite();
 		boolean blownUp = false;
 
 		if(Bombs.size > 0){
 			for(Bomb bomb: Bombs) {
 				if(bomb.checkDestructionPhase() == true){
 					Sprite b = bomb.getBombSprite();
 					boolean bombVsCircleOverlap = ball.getOverlapBool(b.getBoundingRectangle() );
 
 					if(bombVsCircleOverlap == true){
 						blownUp = true;
 						Balls.removeValue(ball, true);
 					}
 				}
 			}
 		}
 
 
 		if(freeze == false){
 			if(blownUp == false){
 				float xPosition = ball.getXPosition();
 				float yPosition = ball.getYPosition();
 				float yPosSpriteHeight = yPosition + ballSprite.getWidth();
 
 
 				if(xPosition + ballSprite.getHeight() >= (screenXRefactor/2)){
 					//Gdx.app.log(TAG, "Out of bounds Down");  
 					ball.setXSpeed(ball.getXSpeed() * -1);
 					ball.setXPosition(ball.getXPosition() + ball.getXSpeed());
 					bounce.play();
 				}
 
 				if(yPosSpriteHeight >= screenYRefactor / 2){
 					//Gdx.app.log(TAG, "Out of bounds Right");
 					ball.setYSpeed(ball.getYSpeed() * -1);
 					ball.setYPosition(ball.getYPosition() + ball.getYSpeed());
 					bounce.play();	
 				}
 				if(xPosition <= (screenXRefactor/2) * -1){
 					//Gdx.app.log(TAG, "Out of bounds Up");
 					ball.setXSpeed(ball.getXSpeed() * -1);
 					ball.setXPosition(ball.getXPosition() + ball.getXSpeed());
 					bounce.play();
 				} 
 				if(yPosition <= (screenYRefactor / 2) * -1){
 					//Gdx.app.log(TAG, "Out of bounds Left");
 					ball.setYSpeed(ball.getYSpeed() * -1);
 					ball.setYPosition(ball.getYPosition() + ball.getYSpeed());
 
 					bounce.play();	
 				}
 
 				ball.setXPosition(ball.getXPosition() + ball.getXSpeed());
 				ball.setYPosition(ball.getYPosition() + ball.getYSpeed());
 				//float bRotate = ballSprite.getRotation() + rotationSpeed;
 
 				float moveX = Interpolation.linear.apply(ball.getXPosition(), ball.getXPosition() + ball.getXSpeed(), 1);
 				float moveY = Interpolation.linear.apply(ball.getYPosition(), ball.getYPosition() + ball.getYSpeed(), 1);
 
 				ballSprite.setX(moveX);
 				ballSprite.setY(moveY);
 				ball.setCircleXY(moveX + ballSprite.getWidth()/2 , moveY + ballSprite.getHeight()/2 );
 				//ballSprite.setRotation(bRotate);
 
 			}
 			boolean kidVsCircleOverlap = ball.getOverlapBool(kid.getBoundingRectangle() );
 
 			if(kidVsCircleOverlap == true){
 				if(ball.collision == false){  //  check for current collision
 					if(shielded == false){
 						if(invincibility == false){
 
 							invincibility = true;
 							deltaShieldTime = Gdx.graphics.getDeltaTime();
 							addLifeMark();
 							kidHit = true;
 							buzzer.play(.05f);
 							ball.collision = true;  // sets current collision
 						}
 					}
 				}
 			}else{
 				ball.collision = false;  // resets to false after overlap ends
 			}
 			
 
 		}
 	}
 
 
 	public void kitLoopCheckAndSet(healthKit kit){
 		Sprite kitSprite = kit.getKitSprite();
 
 		//float xPosition = kit.getXPosition();
 		float yPosition = kit.getYPosition();
 		float yPosSpriteHeight = yPosition + kitSprite.getWidth();
 
 		if(yPosSpriteHeight >= screenYRefactor / 2){
 			//Gdx.app.log(TAG, "Out of bounds Right");
 			kit.setYSpeed(kit.getYSpeed() * -1);
 			kit.setYPosition(kit.getYPosition() + kit.getYSpeed());
 			hardBounce.play(0.5f);	
 		}
 
 
 		if(yPosition <= (screenYRefactor / 2) * -1){
 			//Gdx.app.log(TAG, "Out of bounds Left");
 			kit.setYSpeed(kit.getYSpeed() * -1);
 			kit.setYPosition(kit.getYPosition() + kit.getYSpeed());
 			hardBounce.play(0.5f);	
 		}
 
 		//kit.setXPosition(kit.getXPosition());
 		kit.setYPosition(kit.getYPosition() + kit.getYSpeed());
 
 		float moveY = Interpolation.linear.apply(kit.getYPosition(), kit.getYPosition() + kit.getYSpeed(), 1);
 		//kitSprite.setX(kit.getXPosition());
 		kitSprite.setY(moveY);
 
 		boolean kidVSKitOverlap = kitSprite.getBoundingRectangle().overlaps(kid.getBoundingRectangle());
 
 		if(kidVSKitOverlap == true){		
 			powerUp.play(.05f);
 			removeLifeMark();			
 			MedKits.removeValue(kit, true);   
 		}
 
 	}
 
 	public void shieldLoopCheckAndSet(ShieldBoost shield){
 		Sprite ShieldSprite = shield.getShieldSprite();
 
 		float yPosition = shield.getYPosition();
 		float yPosSpriteHeight = yPosition + ShieldSprite.getWidth();
 
 
 		if(yPosSpriteHeight >= screenYRefactor / 2){
 			//Gdx.app.log(TAG, "Out of bounds Right");
 			shield.setYSpeed(shield.getYSpeed() * -1);
 			shield.setYPosition(shield.getYPosition() + shield.getYSpeed());
 			hardBounce.play(0.5f);	
 		}
 
 
 		if(yPosition <= (screenYRefactor / 2) * -1){
 			//Gdx.app.log(TAG, "Out of bounds Left");
 			shield.setYSpeed(shield.getYSpeed() * -1);
 			shield.setYPosition(shield.getYPosition() + shield.getYSpeed());
 			hardBounce.play(0.5f);	
 		}
 
 		//shield.setXPosition(xPosition);
 		shield.setYPosition(shield.getYPosition() + shield.getYSpeed());
 
 		float moveY = Interpolation.linear.apply(shield.getYPosition(), shield.getYPosition() + shield.getYSpeed(), 1);
 		ShieldSprite.setY(moveY);
 		//ShieldSprite.setX(shield.getXPosition());
 		boolean kidVSShieldOverlap = ShieldSprite.getBoundingRectangle().overlaps(kid.getBoundingRectangle());
 		
 		if(kidVSShieldOverlap == true){	
 			shieldedTime = deltaTime + 4f;
 			powerUp.play(.05f);	
 			shielded = true;
 			deltaShieldTime = Gdx.graphics.getDeltaTime();
 			Shields.removeValue(shield, true);   
 		}
 
 	}
 
 	public void bombLoopCheckAndSet(Bomb bomb){
 		
 		Sprite bombSprite = bomb.getBombSprite();
 		//float xPosition = bomb.getXPosition();
 		float yPosition = bomb.getYPosition();
 		float yPosSpriteHeight = yPosition + bombSprite.getWidth();
 		
 		if(yPosSpriteHeight >= screenYRefactor / 2){
 			//Gdx.app.log(TAG, "Out of bounds Right");
 			bomb.setYSpeed(bomb.getYSpeed() * -1);
 			bomb.setYPosition(bomb.getYPosition() + bomb.getYSpeed());
 			if(bomb.collision == false){
 				hardBounce.play(0.5f);	
 			}
 		}
 
 
 		if(yPosition <= (screenYRefactor / 2) * -1){
 			//Gdx.app.log(TAG, "Out of bounds Left");
 			bomb.setYSpeed(bomb.getYSpeed() * -1);
 			bomb.setYPosition(bomb.getYPosition() + bomb.getYSpeed());
 			if(bomb.collision == false){
 				hardBounce.play(0.5f);	
 			}	
 		}
 
 		bomb.setYPosition(bomb.getYPosition() + bomb.getYSpeed());
 		
 		float moveY = Interpolation.linear.apply(bomb.getYPosition(), bomb.getYPosition() + bomb.getYSpeed(), 1);	
 		bombSprite.setY(moveY);
 		
 		boolean kidVSBombOverlap = bombSprite.getBoundingRectangle().overlaps(kid.getBoundingRectangle());
 		if(kidVSBombOverlap == true){	
 			if(bomb.getCollision() == false){
 				bomb.setCollision(true);
 				Gdx.app.log(TAG, "BOMB!!!!");
 				bomb.setTheDeltaBombTime(deltaTime);
 				timeBomb.play(.07f);
 				bomb.setYSpeed(0);
 
 			}
 			
 		}
 	}
 	
 
 
 	public void timerLoopCheckAndSet(FreezeMotionTimer timer){
 		Sprite timerSprite = timer.getTimerSprite();
 
 
 		float yPosition = timer.getYPosition();
 		float yPosSpriteHeight = yPosition + timerSprite.getWidth();
 
 		if(yPosSpriteHeight >= screenYRefactor / 2){
 			//Gdx.app.log(TAG, "Out of bounds Right");
 			timer.setYSpeed(timer.getYSpeed() * -1);
 			timer.setYPosition(timer.getYPosition() + timer.getYSpeed());
 			hardBounce.play(0.5f);	
 		}
 
 
 		if(yPosition <= (screenYRefactor / 2) * -1){
 			//Gdx.app.log(TAG, "Out of bounds Left");
 			timer.setYSpeed(timer.getYSpeed() * -1);
 			timer.setYPosition(timer.getYPosition() + timer.getYSpeed());
 			hardBounce.play(0.5f);	
 		}
 
 		timer.setYPosition(timer.getYPosition() + timer.getYSpeed());
 
 		float moveY = Interpolation.linear.apply(timer.getYPosition(), timer.getYPosition() + timer.getYSpeed(), 1);
 		timerSprite.setY(moveY);
 		boolean kidVSTimerOverlap = timerSprite.getBoundingRectangle().overlaps(kid.getBoundingRectangle());
 
 		if(kidVSTimerOverlap == true){	
 
 			freeze = true;
 			deltaFreezeTime = elapsedTime + FreezeTimer;
 			powerUp.play(.05f);			
 			Timers.removeValue(timer, true);   
 		}
 		
 
 	}
 
 	public void ballLoopCheckOnPause(Ball ball){
 		Sprite ballSprite = ball.getBallSprite();
 		ballSprite.setX(ball.getXPosition());
 		ballSprite.setY(ball.getYPosition());
 	}
 
 	public void timerLoopCheckOnPause(FreezeMotionTimer timer){
 		Sprite timerSprite = timer.getTimerSprite();
 		timerSprite.setX(timer.getXPosition());
 		timerSprite.setY(timer.getYPosition());
 	}
 
 	public void kitLoopCheckOnPause(healthKit kit){
 		Sprite kitSprite = kit.getKitSprite();
 		kitSprite.setX(kit.getXPosition());
 		kitSprite.setY(kit.getYPosition());
 	}
 
 	public void shieldLoopCheckOnPause(ShieldBoost shield){
 		Sprite shieldSprite = shield.getShieldSprite();
 		shieldSprite.setX(shield.getXPosition());
 		shieldSprite.setY(shield.getYPosition());
 	}
 
 	public void bombLoopCheckOnPause(Bomb bomb){
 		Sprite bombSprite = bomb.getBombSprite();
 		bombSprite.setX(bomb.getXPosition());
 		bombSprite.setY(bomb.getYPosition());
 	}
 
 	public void checkUnPause(){
 		if(pauseGame == true){
 			pauseGame = false;
 			gameState = GAME_RUNNING;
 			kidMovable = true;
 			Gdx.app.log(TAG, "RESUME GAME" );
 			resume();
 		}else{
 			gameState = GAME_PAUSED;
 			pauseGame = true;
 			kidMovable = false;
 			Gdx.app.log(TAG, "GAME PAUSED" );
 			pause();
 		}
 	}
 
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		Vector2 touchPos = new Vector2();
		touchPos.set(Gdx.input.getX(), Gdx.input.getY());
 		Ray cameraRay = camera.getPickRay(touchPos.x, touchPos.y);
 
 		boolean kidDown = kid.getBoundingRectangle().contains(cameraRay.origin.x, cameraRay.origin.y);
		
 		if(kidMovable == true){
			if(kidDown == true){	
 				kidMove = true;
 			}
 		}else{
 			kidMove = false;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 
 		Vector2 touchPos = new Vector2();
 		touchPos.set(Gdx.input.getX(), Gdx.input.getY());
 		Ray cameraRay = camera.getPickRay(touchPos.x, touchPos.y);
 		//Gdx.app.log(TAG, "Touch Ray Coords: X:" + cameraRay.origin.x + " Y:" + cameraRay.origin.y);
 		boolean touchPause = pause.getBoundingRectangle().contains(cameraRay.origin.x, cameraRay.origin.y);
 
 		if(touchPause == true){
 			if(pauseGame == true){
 				pauseRegion = new TextureRegion(pauseTx, 0, 0, pauseTx.getWidth(), 64);
 				pause.setRegion(pauseRegion);
 			}else{
 				pauseRegion = new TextureRegion(pauseTx, 0,64, pauseTx.getWidth(), 64);
 				pause.setRegion(pauseRegion);
 			}
 			checkUnPause();
 		}
 
 		if(gameOver == true){
 			boolean touchRestart = restartBtn.getBoundingRectangle().contains(cameraRay.origin.x, cameraRay.origin.y);
 
 			if(touchRestart == true){
 				gameRestart();
 			}
 		}
 		kidMove = false;
 		return true;
 	}
 
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		if(kidMove == true){
 			Vector2 touchPos = new Vector2();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY());
 			Ray cameraRay = camera.getPickRay(touchPos.x, touchPos.y);
 			float xPos = cameraRay.origin.x;
 			float yPos = cameraRay.origin.y;
 
 			if(cameraRay.origin.x >= ( (screenXRefactor / 2) ) - kid.getWidth() ){
 				xPos = ((screenXRefactor / 2) - 10) - kid.getWidth();
 			}
 
 			if(cameraRay.origin.x <= ((screenXRefactor / 2)) * -1 ){
 				xPos = ((screenXRefactor / 2) - 10) * -1 ;
 			}
 
 			if(cameraRay.origin.y >= ( (screenYRefactor / 2) ) - kid.getHeight() ){
 				yPos = ((screenYRefactor / 2) - 10) - kid.getHeight() ;
 			}
 
 			if(cameraRay.origin.y <= ( (screenYRefactor / 2) ) * -1 ){
 				yPos = ((screenYRefactor / 2) - 10) * -1 ;
 			}
 
 			kid.setX(xPos);
 			kid.setY(yPos);
 
 			starSprite.setX(kid.getX());
 			starSprite.setY(kid.getY() + kid.getHeight());
 			return true;
 		}
 		return false;
 	}
 
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		return false;
 	}
 
 
 	@Override
 	public boolean scrolled(int amount) {
 		return false;
 	}
 
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if(keycode == Keys.BACK){
 			dispose();
 			game.setScreen(new MainMenu(game));
 		}
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		return false;
 	}
 
 
 
 
 }
