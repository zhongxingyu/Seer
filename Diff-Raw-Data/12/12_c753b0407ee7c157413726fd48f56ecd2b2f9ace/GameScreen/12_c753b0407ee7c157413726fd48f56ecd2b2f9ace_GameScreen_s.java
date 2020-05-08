 package com.games.androidgames.pang;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 
 import com.games.androidgames.framework.DynamicGameObject;
 import com.games.androidgames.framework.Game;
 import com.games.androidgames.framework.GameObject;
 import com.games.androidgames.framework.Screen;
 import com.games.androidgames.framework.Input.TouchEvent;
 import com.games.androidgames.framework.gl.Camera2D;
 import com.games.androidgames.framework.gl.SpriteBatcher;
 import com.games.androidgames.framework.gl.Texture;
 import com.games.androidgames.framework.gl.TextureRegion;
 import com.games.androidgames.framework.impl.GLGame;
 import com.games.androidgames.framework.impl.GLGraphics;
 import com.games.androidgames.framework.math.OverlapTester;
 import com.games.androidgames.framework.math.Vector2;
 import com.games.androidgames.framework.GLText;
 import com.games.androidgames.pang.buttons.BlankButton;
 import com.games.androidgames.pang.buttons.GameButton;
 import com.games.androidgames.pang.buttons.MainMenuButton;
 import com.games.androidgames.pang.buttons.MenuButton;
 import com.games.androidgames.pang.buttons.NextLevelButton;
 import com.games.androidgames.pang.buttons.PauseButton;
 import com.games.androidgames.pang.buttons.ScoresButton;
 import com.games.androidgames.pang.elements.Ball;
 import com.games.androidgames.pang.elements.Ladder;
 import com.games.androidgames.pang.elements.Platform;
 import com.games.androidgames.pang.elements.Player;
 import com.games.androidgames.pang.elements.PowerItem;
 import com.games.androidgames.pang.elements.TouchControls;
 import com.games.androidgames.pang.elements.TouchElement;
 import com.games.androidgames.pang.elements.Weapon;
 import com.games.androidgames.pang.elements.Player.PlayerState;
 import com.games.androidgames.pang.elements.TouchControls.ButtonList;
 import com.games.androidgames.pang.elements.Weapon.MODE;
 
 public class GameScreen extends Screen implements OnKeyListener, OnTouchListener {
 	Camera2D camera;
 	
 	private final float BALL_RADIUS = 16.0f;
 	private final float SMALLEST_BALL = 16.0f;
 	private final float BUMP = 0.7f;
 	private final int SPRITE_LIMIT = 100;
 	private final int LIFE_BONUS = 1500;
 
 	private int topTen;
 	private volatile int selectedMenu;
 
 	private float[] x = new float[10];
 	private float[] y = new float[10];
 	private boolean[] touched = new boolean[10];
 	private int[] id = new int[10];
 	
 	private boolean[] pressedKeys = new boolean[128];
 	private List<KeyEvent> keyEventsBuffer = new ArrayList<KeyEvent>(); 
     
 	private GLGraphics glGraphics;
 	private GL10 gl;
 	private GLText glText, glMenuText, glScoreText;
 
 	private List<MenuButton> pauseItems, finishItems;
 	private Player player;
 	private PowerItem single, doub, stick;
 	private Weapon weapon;
 	
 	private PlayerState playerState;
 	private Player savedPlayerState;
 	private TextureRegion currKeyFrame;		
 	private Texture background;
 	private TextureRegion backgroundRegion;
 	private List<DynamicGameObject> balls;
 	private List<GameObject> platforms;
 	private List<Ladder> ladders;
 	private Texture textureSet;
 	
 	private Level level;
 	
 	private TextureRegion ballRegion, spearRegion;
 	private Vector2 gravity = new Vector2(0, -400);
 	
 	private TouchControls controls;
 	private SpriteBatcher batcher;
 	
 	public GameScreen(Game game) {
 		super(game);
 		glGraphics = ((GLGame)game).getGLGraphics();
 		glGraphics.getView().setOnKeyListener(this);
 		glGraphics.getView().setOnTouchListener(this);
 		glGraphics.getView().setFocusable(true);
 		glGraphics.getView().requestFocus();
 		gl = glGraphics.getGL();
 		glText = Resources.glScoreText;
 		glMenuText = Resources.glButtonText;
 		glScoreText = Resources.glScoreText;
 		
 		selectedMenu = -1;
 		topTen = -1;
 		camera = new Camera2D(glGraphics, Settings.WORLD_WIDTH, Settings.WORLD_HEIGHT);
 		playerState = PlayerState.STANDING;
 		
 		batcher = new SpriteBatcher(gl, SPRITE_LIMIT);	
 		pauseItems = new ArrayList<MenuButton>();
 		finishItems = new ArrayList<MenuButton>();
 		pauseItems.add(new BlankButton("Paused", glMenuText, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 5 * 4, camera.zoom * 1.5f));
 		pauseItems.add(new PauseButton("Resume", glMenuText, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 5 * 3, camera.zoom * 1.5f));
 		
 		MenuButton exit = new MainMenuButton("Exit game", glMenuText, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 5, camera.zoom * 1.5f);
 		pauseItems.add(exit);	
 		
 		if(CurrentGameDetails.level < Level.NUMBER_OF_LEVELS) {
 			finishItems.add(new NextLevelButton("Next Level", glMenuText, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 5 * 3, camera.zoom * 1.5f));
 		} else {
 			finishItems.add(new GameButton("New Game", glMenuText, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 5 * 3, camera.zoom * 1.5f));
 		}
 		finishItems.add(new ScoresButton("View Scoreboard", glMenuText, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 5 * 2, camera.zoom * 1.5f));
 		finishItems.add(exit);
 	}
 	
 	@Override
 	public void resume() {
 			textureSet = Resources.gameItems;
 			level = new Level(textureSet);
 			level.load(game, CurrentGameDetails.level);
 			
 			balls = level.balls;
 			ladders  = level.ladders;
 			platforms = level.platforms;
 			background = level.background;
 			backgroundRegion =  level.backgroundRegion;
 			
 			if(savedPlayerState == null) {
 				player = new Player(camera.position.x, 35, 64, 70, 3, textureSet);
 				player.setGravity(0, -400);
 			} else {
 				player = savedPlayerState;
 			}
 			ballRegion = Resources.ball;
 							
 			weapon = new Weapon(textureSet, 2.5f, glGraphics.getHeight());				
 			TextureRegion singleRegion = Resources.powerupSingle;
 			single = new PowerItem(singleRegion, 100, 400, 24, 48, 2.5f, MODE.SINGLE);
 			
 			TextureRegion doubleRegion = Resources.powerupDouble;
 			doub = new PowerItem(doubleRegion, 100, 400, 48, 48, 2.5f, MODE.DOUBLE);
 			
 			TextureRegion stickyRegion = Resources.powerupSticky;
 			stick = new PowerItem(stickyRegion, 100, 400, 40, 48, 2.5f, MODE.STICKY);
 			
 
 			
 			controls = new TouchControls(textureSet, Settings.WORLD_WIDTH, Settings.WORLD_HEIGHT);
 	}
 
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		boolean validKey = true;
 		
 		if(event.getAction() == KeyEvent.ACTION_DOWN) {
 			if(keyCode > 0 && keyCode < 127) {
                 pressedKeys[keyCode] = true;
 			}
 			switch(event.getKeyCode())
 			{
 				case KeyEvent.KEYCODE_DPAD_CENTER: 	{	//x														
 														if((!player.alive() || balls.size() == 0) && selectedMenu >= 0 && selectedMenu < finishItems.size()){
 															if(selectedMenu < finishItems.size()){	
 																finishItems.get(selectedMenu).action(game);	
 									            				Resources.playSound(Resources.BUTTON_HEIGHTLIGHT);
 															}
 														}else if(Settings.gamePaused && selectedMenu >= 0 && selectedMenu < pauseItems.size()){
 															if(selectedMenu < pauseItems.size()){																
 																pauseItems.get(selectedMenu).action(game);	
 																Resources.playSound(Resources.BUTTON_HEIGHTLIGHT);
 															}
 														}														
 													}
 												break;
 					case KeyEvent.KEYCODE_BACK:  
 												break;
 				case KeyEvent.KEYCODE_BUTTON_X:	{
 														
 												}
 												break;
 				case KeyEvent.KEYCODE_BUTTON_Y: 													
 												break;
 				case KeyEvent.KEYCODE_DPAD_UP:  {
 												 	if(!player.alive() || balls.size() == 0){
 												 		selectedMenu = MenuButton.findPreviousButton(selectedMenu, finishItems);
 														finishItems.get(selectedMenu).heightLighted = true;
 														Resources.playSound(Resources.BUTTON_HEIGHTLIGHT);	
 													}else if(Settings.gamePaused){
 														selectedMenu = MenuButton.findPreviousButton(selectedMenu, pauseItems);
 														pauseItems.get(selectedMenu).heightLighted = true;
 														Resources.playSound(Resources.BUTTON_HEIGHTLIGHT);															
 													}
 												}
 												break;
 				case KeyEvent.KEYCODE_DPAD_DOWN: {
 													if(!player.alive() || balls.size() == 0){															
 														selectedMenu = MenuButton.findNextButton(selectedMenu, finishItems);
 														finishItems.get(selectedMenu).heightLighted = true;
 														Resources.playSound(Resources.BUTTON_HEIGHTLIGHT);
 													}else if(Settings.gamePaused){														
 														selectedMenu = MenuButton.findNextButton(selectedMenu, pauseItems);
 														pauseItems.get(selectedMenu).heightLighted = true;
 														Resources.playSound(Resources.BUTTON_HEIGHTLIGHT);	
 													}
 												 }
 												
 												break;
 				case KeyEvent.KEYCODE_DPAD_RIGHT: 														
 												break;
 				case KeyEvent.KEYCODE_DPAD_LEFT:  	
 												break;
 				case KeyEvent.KEYCODE_BUTTON_SELECT: 
 												break;
 				case KeyEvent.KEYCODE_BUTTON_START: 
 													
 												break;
 				case KeyEvent.KEYCODE_BUTTON_L1: 
 												
 												break;
 				case KeyEvent.KEYCODE_BUTTON_R1: 
 												
 												break;
 			}
 		}else if(event.getAction() == KeyEvent.ACTION_UP) {
 			if(keyCode > 0 && keyCode < 127) {
                 pressedKeys[keyCode] = false;
 			}
 		}
 		//catch circle button (prevent back key + alt quiting)
 		if(!event.isAltPressed()) {
 			validKey = false;
 		}
 		return validKey;
 	}
 	
 	public void startButtonAction() { {															
 			if(Settings.gamePaused) {					
 				Settings.gamePaused = false;				
 			} else {				
 				Settings.gamePaused = true;		
 				if(selectedMenu >= 0 && selectedMenu < pauseItems.size()){
 					pauseItems.get(selectedMenu).heightLighted = false;
 					selectedMenu = -1;
 				}
 			}
 		}
     }
 
     public boolean keyPressed(int keyCode) {
         if (keyCode < 0 || keyCode > 127)
             return false;
         return pressedKeys[keyCode];
     }	   
     
 	@Override
 	public void update(float deltaTime) {
 		game.getInput().getKeyEvents();	
 		game.getInput().getAccelX();
 		game.getInput().getAccelY();
 		game.getInput().getAccelZ();
 		
 		boolean cStart, cUp, cDown, cRight, cLeft, cA, cB, movingUpDown = false;
 		synchronized(controls){
 			 cStart = controls.activeButtons[3];
 			 cA = controls.activeButtons[1];
 			 cB = controls.activeButtons[2];
 			 cUp = controls.up >= 0?true:false;
 			 cDown = controls.down >= 0?true:false;
 			 cLeft = controls.left >= 0?true:false;
 			 cRight = controls.right >= 0?true:false;
 			 controls.activeButtons[3] = false;
 			 controls.activeButtons[1] = false;
 			 controls.activeButtons[2] = false;
 		 }
 		if(selectedMenu != -1){
 			for(int i = 0; i < pauseItems.size(); i++){
 				pauseItems.get(i).heightLighted = false;
 			}
 			pauseItems.get(selectedMenu).heightLighted = true;
 		}
		if(cStart || keyPressed(KeyEvent.KEYCODE_BUTTON_START)) {
			pressedKeys[KeyEvent.KEYCODE_BUTTON_START] = false;
			startButtonAction();			
		} 
 		
		
		if(!Settings.gamePaused) {		
 			playerState = PlayerState.STANDING;
 
 			//move player
 			if((keyPressed(KeyEvent.KEYCODE_DPAD_UP) || cUp)) {
 				for(int i =0; i < ladders.size(); i++) {
 					Ladder ladder = (Ladder)ladders.get(i);
 					if(ladder.upValid(player)) {
 						playerState = PlayerState.CLIMBING;
 						player.movePlayer(0, 200, deltaTime);
 						movingUpDown = true;
 						break;
 					}
 				}
 			} else if((keyPressed(KeyEvent.KEYCODE_DPAD_DOWN) || cDown)) {
 				for(int i =0; i < ladders.size(); i++) {
 					Ladder ladder = (Ladder)ladders.get(i);
 					if(ladder.downValid(player)) {
 						playerState = PlayerState.CLIMBING;					
 						player.movePlayer(0, -200, deltaTime);
 						movingUpDown = true;
 						break;
 					}
 				}
 			} else { 
 				if((weapon.spear.alive && weapon.spear.position.y < 80) || (weapon.stickySpear.position.y < 80 && weapon.stickySpear.alive) || (weapon.spear2.position.y < 80 && weapon.spear2.alive)) {
 					playerState = PlayerState.SHOOTING;
 				} 				
 			}	
 			if(!movingUpDown && (keyPressed(KeyEvent.KEYCODE_DPAD_RIGHT) || cRight) && player.position.x + player.bounds.width / 2 < Settings.WORLD_WIDTH) 	{
 				player.movePlayer(200, 0, deltaTime);
 				playerState = PlayerState.WALKING_RIGHT;
 			} else if(!movingUpDown && (keyPressed(KeyEvent.KEYCODE_DPAD_LEFT) || cLeft) && player.position.x - player.bounds.width / 2 > 0) 	{
 				player.movePlayer(-200, 0, deltaTime);
 				playerState = PlayerState.WALKING_LEFT;
 			} 		
 			
 			if((keyPressed(KeyEvent.KEYCODE_DPAD_CENTER) || cA)) {
 				//reset button to off
 				pressedKeys[KeyEvent.KEYCODE_DPAD_CENTER] = false;					
 				//x pressed - spawn new spear
 				if(player.onFloor(deltaTime, ladders, platforms)){
 					if(weapon.shoot(player.position.x, player.position.y)){	
 						Resources.playSound(Resources.FIRE);
 						playerState = PlayerState.SHOOTING;
 					}
 				}
 			} 
 			if((keyPressed(KeyEvent.KEYCODE_BUTTON_X) || cB)) {
 				pressedKeys[KeyEvent.KEYCODE_BUTTON_X] = false;
 				//square pressed - jump
 				if(player.onFloor(deltaTime, ladders, platforms)){
 					player.setVelocity(0, 450);
 				}
 			}
 			
 			//update all balls
 			synchronized(balls) {
 				for(int i = 0; i < balls.size(); i++) {
 					DynamicGameObject ball = balls.get(i);
 					
 					//check player collision
 					if(OverlapTester.overlapCircleRectangle(ball.boundingCircle, player.bounds)) {
 						if(!player.hurt()){
 							Resources.playSound(Resources.HIT);
 						}
 						player.hit();
 						if(player.alive() == false){
 							selectedMenu = -1;
 							CurrentGameDetails.level = 1;
 							//replace next level button with new game button
 							finishItems.set(0, new GameButton("New Game", glMenuText, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 5 * 3, camera.zoom * 1.5f)); 
 							
 							topTen = Resources.checkScore((GLGame)game, CurrentGameDetails.score);
 						}
 					}
 					
 					//check spear collisions
 					if(weapon.checkCollisions(ball.boundingCircle)) {							
 						Resources.playSound(Resources.POP);
 						//if ball smaller than smallest ball limit destroy
 						if(ball.boundingCircle.radius < SMALLEST_BALL) {
 							balls.remove(ball);
 							CurrentGameDetails.score += 200;
 							if(balls.size() == 0){
 								//add additional score for lives left
 								CurrentGameDetails.score += CurrentGameDetails.lives * LIFE_BONUS;
 								CurrentGameDetails.score += CurrentGameDetails.level * 1000;
 								selectedMenu = -1;
 								
 								//if last level save score
 								if(CurrentGameDetails.level == Level.NUMBER_OF_LEVELS){
 									topTen = Resources.checkScore((GLGame)game, CurrentGameDetails.score);
 								}
 								
 							}
 						} else {
 							CurrentGameDetails.score += 50;
 							Random rand = new Random();								
 							int prize = rand.nextInt(2)+1;
 							
 							switch(prize) {
 								case 0: {
 									if(!single.alive) {
 										single.setPosition(ball.position);
 										single.alive = true;
 									}
 								} break;
 								case 1: {
 									if(!doub.alive){
 										doub.setPosition(ball.position);
 										doub.alive = true;
 									}										
 								} break;
 								case 2: {
 									if(!stick.alive) {
 										stick.setPosition(ball.position);
 										stick.alive = true;
 									}										
 								} break;
 							}
 							
 							ball.boundingCircle.radius /= 2; 
 							ball.velocity.y = BUMP * (Settings.WORLD_HEIGHT - ball.position.y);
 							
 							balls.add(new Ball(ball.position.x, ball.position.y, ball.boundingCircle.radius));
 							balls.get(balls.size() - 1).velocity.x = -ball.velocity.x;
 							balls.get(balls.size() - 1).velocity.y = BUMP * (Settings.WORLD_HEIGHT - ball.position.y);
 							
 							
 						}
 					}
 						
 					//check ball in x screen bounds
 					if(ball.position.x < ball.boundingCircle.radius) {
 						ball.velocity.x = -ball.velocity.x;
 						ball.position.x = ball.boundingCircle.radius;
 					}else if(ball.position.x > Settings.WORLD_WIDTH - ball.boundingCircle.radius) {
 						ball.velocity.x = -ball.velocity.x;
 						ball.position.x = Settings.WORLD_WIDTH - ball.boundingCircle.radius;
 					}
 					
 					//bounce ball when hit floor
 					if(ball.position.y < ball.boundingCircle.radius) {					
 						ball.velocity.y = (((Ball)ball).bounce) - (ball.boundingCircle.radius - ball.position.y);
 						ball.position.y = ball.boundingCircle.radius;
 					} else {			
 						ball.velocity.add(gravity.x * deltaTime, gravity.y * deltaTime);
 					}
 					
 					for(int j = 0; j < platforms.size(); j++) {
 						Platform platform = (Platform)platforms.get(j);
 					
 						if(OverlapTester.overlapCircleRectangle(ball.boundingCircle, platform.bounds)) {
 							if(ball.position.x + ball.boundingCircle.radius / 2 > platform.position.x - platform.bounds.width / 2 &&
 							   ball.position.x - ball.boundingCircle.radius / 2 < platform.position.x + platform.bounds.width / 2) {
 								ball.velocity.y = -ball.velocity.y;
 								if(ball.position.y < platform.position.y) {
 									ball.position.y = platform.bounds.lowerLeft.y - ball.boundingCircle.radius;
 								}
 								else {
 									ball.position.y = platform.position.y + (platform.bounds.height / 2) +  ball.boundingCircle.radius;
 								} 
 							}
 							else {
 								ball.velocity.x = -ball.velocity.x;
 								if(ball.position.x < platform.position.x) {
 									ball.position.x = platform.bounds.lowerLeft.x -  (ball.bounds.width / 2);
 								}
 								else {
 									ball.position.x = platform.position.x + (platform.bounds.width / 2) + ball.boundingCircle.radius;
 								} 
 							}
 						}
 					}
 					
 					//apply changes to ball
 					ball.position.add(ball.velocity.x * deltaTime, ball.velocity.y * deltaTime);
 					ball.boundingCircle.center.set(ball.position.x, ball.position.y);						
 				}	
 			}	
 			
 			//update spear
 			if(weapon.alive) {		
 				weapon.update(deltaTime, platforms);
 			}
 			if(single.alive) {
 				if(OverlapTester.overlapRectangle(player.bounds, single.bounds)) {
 					weapon.mode = MODE.SINGLE;
 					CurrentGameDetails.score += single.value;
 					single.alive = false;
 				}
 				single.update(deltaTime, platforms);
 			}
 			if(doub.alive) {
 				if(OverlapTester.overlapRectangle(player.bounds, doub.bounds)) {
 					weapon.mode = MODE.DOUBLE;
 					CurrentGameDetails.score += doub.value;
 					doub.alive = false;
 				}
 				doub.update(deltaTime, platforms);
 			}
 			if(stick.alive) {
 				if(OverlapTester.overlapRectangle(player.bounds, stick.bounds)) {
 					weapon.mode = MODE.STICKY;
 					CurrentGameDetails.score += stick.value;
 					stick.alive = false;
 				}
 				stick.update(deltaTime, platforms);
 			}
 			
 			player.update(deltaTime, playerState, ladders, platforms);
 		}	
 	}
 
 	@Override
 	public void present(float deltaTime) {
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		camera.setViewportAndMatrices(0.0f, 0.0f, 0.6f);
 		
 		gl.glEnable(GL10.GL_BLEND);
 		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		gl.glEnable(GL10.GL_TEXTURE_2D);		  
 		
 		
 		batcher.beginBatch(background);
 		batcher.drawSprite(Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 2, Settings.WORLD_WIDTH, Settings.WORLD_HEIGHT, backgroundRegion);
 		batcher.endBatch();
 		
 		if(platforms.size() > 0 || ladders.size() > 0){
 			batcher.beginBatch(textureSet);
 			for(int i = 0; i < platforms.size(); i++) {
 				Platform platform = (Platform)platforms.get(i);
 				for(GameObject tile: platform.tiles) {
 					batcher.drawSprite(tile.position.x, tile.position.y, tile.bounds.width, tile.bounds.height, platform.tileRegion);
 				}		
 			}
 			
 			for(int i = 0; i < ladders.size(); i++) {
 				Ladder ladder = (Ladder)ladders.get(i);
 				for(GameObject tile: ladder.tiles) {
 					batcher.drawSprite(tile.position.x, tile.position.y, tile.bounds.width, tile.bounds.height, ladder.tileRegion);
 				}		
 			}
 			batcher.endBatch();
 		}
 		
 		if(single.alive) {
 			batcher.beginBatch(textureSet);
 			if(single.life < 1 && single.life * 4 % 2 > 1) {
 				gl.glColor4f(0.4f, 0.4f, 0.5f, 1.0f);
 			}
 			batcher.drawSprite(single.position.x, single.position.y, single.bounds.width, single.bounds.height, single.region);
 			batcher.endBatch();
 			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);	
 		}
 		if(doub.alive) {
 			batcher.beginBatch(textureSet);
 			if(doub.life < 1 && doub.life * 4 % 2 > 1) {
 				gl.glColor4f(0.4f, 0.4f, 0.5f, 1.0f);
 			}
 			batcher.drawSprite(doub.position.x, doub.position.y, doub.bounds.width, doub.bounds.height, doub.region);
 			batcher.endBatch();
 			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);	
 		}
 		if(stick.alive) {
 			batcher.beginBatch(textureSet);
 			if(stick.life < 1 && stick.life * 4 % 2 > 1) {
 				gl.glColor4f(0.4f, 0.4f, 0.5f, 1.0f);
 			}
 			batcher.drawSprite(stick.position.x, stick.position.y, stick.bounds.width, stick.bounds.height, stick.region);
 			batcher.endBatch();
 			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);	
 		}
 		
 
 		if(weapon.spear.alive) {
 			//draw spear 1
 			batcher.beginBatch(textureSet);	
 			if(weapon.spear.stick && weapon.spear.activeTime < 1) {
 				if(weapon.spear.activeTime * 4 % 2 > 1) {
 					gl.glColor4f(0.4f, 0.4f, 0.5f, 1.0f);
 				}
 			}
 			batcher.drawSprite(weapon.spear.position.x, weapon.spear.position.y, weapon.spear.bounds.width, weapon.spear.bounds.height, weapon.spear.region);
 			batcher.endBatch();
 			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);			
 			
 		}
 		if(weapon.spear2.alive) {
 			//draw spear 2
 			batcher.beginBatch(textureSet);	
 			if(weapon.spear2.stick && weapon.spear2.activeTime < 1) {
 				if(weapon.spear2.activeTime * 4 % 2 > 1) {
 					gl.glColor4f(0.4f, 0.4f, 0.5f, 1.0f);
 				}
 			}
 			batcher.drawSprite(weapon.spear2.position.x, weapon.spear2.position.y, weapon.spear2.bounds.width, weapon.spear2.bounds.height, weapon.spear2.region);
 			batcher.endBatch();
 			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
 		}
 		if(weapon.stickySpear.alive) {
 			//draw sticky spear
 			batcher.beginBatch(textureSet);	
 			if(weapon.stickySpear.stick && weapon.stickySpear.activeTime < 1) {
 				if(weapon.stickySpear.activeTime * 4 % 2 > 1) {
 					gl.glColor4f(0.4f, 0.4f, 0.5f, 1.0f);
 				}
 			}
 			batcher.drawSprite(weapon.stickySpear.position.x, weapon.stickySpear.position.y, weapon.stickySpear.bounds.width, weapon.stickySpear.bounds.height, weapon.stickySpear.region);
 			batcher.endBatch();
 			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
 		}
 		
 		
 		//draw player
 		batcher.beginBatch(textureSet);
 		if(player.hurt() && player.getImmuneTime() * 10 % 2 > 1) {
 			gl.glColor4f(1.0f, 0.2f, 0.2f, 1.0f);
 		}
 		batcher.drawSprite(player.position.x, player.position.y, player.bounds.width, player.bounds.height, player.getKeyFrame());	
 		batcher.endBatch();
 		if(player.hurt()) {
 			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
 		}	
 		
 		if(balls.size() > 0){
 			batcher.beginBatch(textureSet);
 			for(int i = 0; i < balls.size(); i++) {
 				DynamicGameObject ball = balls.get(i);
 				batcher.drawSprite(ball.position.x, ball.position.y, ball.boundingCircle.radius * 2, ball.boundingCircle.radius * 2, ballRegion);
 			}
 			if(Settings.displayTouchControls) {
 				synchronized(controls){
 					for(TouchElement element :controls.touchElements) {
 						batcher.drawSprite(element.position.x, element.position.y, element.width, element.height, element.region);
 					}
 				}
 			}
 			batcher.endBatch();
 		}
 		
 		if(!player.alive()) {
 			Settings.gamePaused = true;
 			player.reset();
 			glText.setScale(1.7f);
 			String text = "You Died! ";
 			if(topTen > 0){
 				text += " New Record " + topTen;
 				if(topTen > 3){
 					text += "th";
 				} else if(topTen == 3){
 					text += "rd";
 				} else if(topTen == 2){
 					text += "nd";
 				} else if(topTen == 1){
 					text += "st";
 				}
 			}
 			glText.begin(1, 0, 0, 1);
 			glText.drawC(text, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT - 85);
 			glText.end();
 			
 			for(MenuButton item: finishItems) {
 				glMenuText.setScale(item.scale);
 				if(item.heightLighted){
 					glMenuText.begin(item.altR, item.altG, item.altB, item.altA);
 					glMenuText.draw(item.text, item.bounds.lowerLeft.x , item.bounds.lowerLeft.y);
 					glMenuText.end();
 				} else {
 					glMenuText.begin(item.r, item.g, item.b, item.a);
 					glMenuText.draw(item.text, item.bounds.lowerLeft.x , item.bounds.lowerLeft.y);
 					glMenuText.end();
 				}
 			}
 		} else if(balls.size() == 0) {
 			Settings.gamePaused = true;
 			player.reset();
 			glText.setScale(1.4f);
 			String text = "Congratulations you won! ";
 			if(topTen > 0){
 				text += " New Record " + topTen;
 				if(topTen > 3){
 					text += "th";
 				} else if(topTen == 3){
 					text += "rd";
 				} else if(topTen == 2){
 					text += "nd";
 				} else if(topTen == 1){
 					text += "st";
 				}
 			}
 			glText.begin(0.05f, 0.3f, 0.05f, 1);
 			glText.drawC(text, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT - 85);
 			glText.drawC("Life Bonus Points: " + CurrentGameDetails.lives + " x " + LIFE_BONUS + " = " + CurrentGameDetails.lives * LIFE_BONUS, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT - 120);
 			glText.end();
 			
 			for(MenuButton item: finishItems) {
 				glMenuText.setScale(item.scale);
 				if(item.heightLighted){
 					glMenuText.begin(item.altR, item.altG, item.altB, item.altA);
 					glMenuText.draw(item.text, item.bounds.lowerLeft.x , item.bounds.lowerLeft.y);
 					glMenuText.end();
 				} else {
 					glMenuText.begin(item.r, item.g, item.b, item.a);
 					glMenuText.draw(item.text, item.bounds.lowerLeft.x , item.bounds.lowerLeft.y);
 					glMenuText.end();
 				}
 			}
 		} else if(Settings.gamePaused) { 
 			for(MenuButton item: pauseItems) {
 				glMenuText.setScale(item.scale);
 				if(item.heightLighted){
 					glMenuText.begin(item.altR, item.altG, item.altB, item.altA);
 					glMenuText.draw(item.text, item.bounds.lowerLeft.x , item.bounds.lowerLeft.y);
 					glMenuText.end();
 				} else {
 					glMenuText.begin(item.r, item.g, item.b, item.a);
 					glMenuText.draw(item.text, item.bounds.lowerLeft.x , item.bounds.lowerLeft.y);
 					glMenuText.end();
 				}
 			}
 		}
 		
 		glScoreText.setScale(camera.zoom * 2);
 		glScoreText.begin(0.2f, 0.2f, 0.95f, 1.0f);
 		glScoreText.draw("Score: " + CurrentGameDetails.score, 80, Settings.WORLD_HEIGHT - 60);
 		glScoreText.draw("Life: ", Settings.WORLD_WIDTH - 240, Settings.WORLD_HEIGHT - 60);
 		glScoreText.end();
 		
 		
 		batcher.beginBatch(textureSet);
 		switch(weapon.mode){
 		case DOUBLE: batcher.drawSprite( 32 , Settings.WORLD_HEIGHT - 32, doub.bounds.width, doub.bounds.height, doub.region); 
 					 	break;
 		case STICKY: batcher.drawSprite( 32, Settings.WORLD_HEIGHT - 32, stick.bounds.width, stick.bounds.height, stick.region); 
 						break;
 			default: batcher.drawSprite( 32, Settings.WORLD_HEIGHT - 32, single.bounds.width, single.bounds.height, single.region);
 		}
 		
 		//draw life level
 		for(int i = 0; i < CurrentGameDetails.lives; i++){
 			batcher.drawSprite( (Settings.WORLD_WIDTH - 112 + (40 * i)), Settings.WORLD_HEIGHT - 32, 32, 32, Resources.life);
 		}
 		batcher.endBatch();                                  
 	}
 
 	@Override
 	public void dispose() {
 		
 	}
 
 	@Override
 	public void pause() {
 		
 	}
 	
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		 synchronized (this) {			 
 			 
 	            int action = event.getAction() & MotionEvent.ACTION_MASK;
 	            int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
 	            int pointerCount = event.getPointerCount();
 	            TouchEvent touchEvent;
 	            for (int i = 0; i < pointerCount; i++) {
 	                int pointerId = event.getPointerId(i);
 	                if (event.getAction() != MotionEvent.ACTION_MOVE && i != pointerIndex) {
 	                    // if it's an up/down/cancel/out event, mask the id to see if we should process it for this touch
 	                    // point
 	                    continue;
 	                }
 	                float x = event.getX(i) / Settings.SCALE_WIDTH;
 	                float y =  (glGraphics.getHeight() - event.getY(i)) / Settings.SCALE_HEIGHT;
 	                switch (action) {
 		                case MotionEvent.ACTION_DOWN:
 		                case MotionEvent.ACTION_POINTER_DOWN:
 		                {
 		                	if(!Settings.gamePaused){
 		                		synchronized(controls){
 				                	for(int j = 0; j < controls.touchElements.size(); j++) {
 				                		TouchElement element = controls.touchElements.get(j);
 				                		if(element.ready()){		                			
 					                		if(OverlapTester.pointInRectangle(element.bounds, x, y)){				                			
 					    						if(element.ready()){
 					    							controls.activeButtons[j] = true;
 					    							element.use(x, y);
 					    						}
 					    						
 					    						//if hit joystick work out directions
 					    						if(j == 0) {
 					    							//up, down
 					    							if(controls.getElement(ButtonList.STICK).position.y + controls.DEADZONE < y) {
 					    								controls.up = pointerId;
 					    							} else if(controls.getElement(ButtonList.STICK).position.y - controls.DEADZONE > y) {
 					    								controls.down = pointerId;
 					    							} else if(controls.up == pointerId) {
 					    								controls.up = -1;
 					    							} else if(controls.down == pointerId) {
 					    								controls.down = -1;
 					    							}
 					    							
 					    							//left, right
 					    							if(controls.getElement(ButtonList.STICK).position.x + controls.DEADZONE < x) {
 					    								controls.right = pointerId;
 					    							} else if(controls.getElement(ButtonList.STICK).position.x - controls.DEADZONE > x) {
 					    								controls.left = pointerId;
 					    							}else if(controls.right == pointerId) {
 					    								controls.right = -1;
 					    							} else if(controls.left == pointerId) {
 					    								controls.left = -1;
 					    							}  		    					
 					    							
 					    							
 					    						}
 					                		}
 				                		}
 				                	}
 		                		}
 		                	}else{
 			                	for(MenuButton item1: pauseItems) {
 			                		if(OverlapTester.pointInRectangle(item1.bounds, new Vector2(x, y))){
 			                			if(item1.heightLighted == false  && item1.soundEnabled) {
 			                				Resources.playSound(Resources.BUTTON_HEIGHTLIGHT);
 			                				selectedMenu = -1;
 			                			}			                    	
 				                    } 
 			                	}
 		                	}
 		                	if(!player.alive() || balls.size() == 0){
 		                		for(MenuButton item1: finishItems) {
 			                		if(OverlapTester.pointInRectangle(item1.bounds, new Vector2(x, y))){
 			                			if(item1.heightLighted == false  && item1.soundEnabled) {
 			                				Resources.playSound(Resources.BUTTON_HEIGHTLIGHT);
 			                				selectedMenu = -1;
 			                				item1.heightLighted = true;
 			                			}			                    	
 				                    } 
 			                	}
 		                	}
 		                }  break;
 	
 		                case MotionEvent.ACTION_UP:
 		                case MotionEvent.ACTION_POINTER_UP:
 		                case MotionEvent.ACTION_CANCEL:
 		                {
 		                	synchronized(controls){
 		                		controls.resetActiveButtons();
 		                		
 		                		//up, down
 								if(controls.up == pointerId) {
 									controls.up = -1;
 								} else if(controls.down == pointerId) {
 									controls.down = -1;
 								}
 		                		//left, right
 								if(controls.right == pointerId) {
 									controls.right = -1;
 								} else if(controls.left == pointerId) {
 									controls.left = -1;
 								}    							
 								
 		                	}
 			    			if(Settings.gamePaused){
 			                	for(MenuButton item1: pauseItems) {
 				                	if(OverlapTester.pointInRectangle(item1.bounds, new Vector2(x, y)) && item1.enabled){
 				                		item1.heightLighted = false;
 				                		item1.action(game);			                    	
 				                    }
 			                	}
 			    			}
 			    			if(!player.alive() || balls.size() == 0){
 		                		for(MenuButton item1: finishItems) {
 			                		if(OverlapTester.pointInRectangle(item1.bounds, new Vector2(x, y))){
 			                			if(OverlapTester.pointInRectangle(item1.bounds, new Vector2(x, y)) && item1.enabled){
 			                				item1.heightLighted = false;
 					                		item1.action(game);			                    	
 					                    }		                    	
 				                    } 
 			                	}
 		                	}
 		                } break;	
 		                case MotionEvent.ACTION_MOVE:
 		                {
 		                	
 		                	synchronized(controls){
 		                		controls.resetActiveButtons();
 			                	for(int j = 0; j < controls.touchElements.size(); j++) {
 			                		TouchElement element = controls.touchElements.get(j);
 			                		if(element.ready()){		                			
 				                		if(OverlapTester.pointInRectangle(element.bounds, x, y)){
 				                			if(element.ready()){
 				                				controls.activeButtons[j] = true;				                			
 				    							element.use(x, y);
 				    						}
 				    						
 				    						//if hit joystick work out directions
 				    						if(j == 0) {		
 				    							
 				    							//up, down
 				    							if(controls.getElement(ButtonList.STICK).position.y + controls.DEADZONE < y) {
 				    								controls.up = pointerId;
 				    							} else if(controls.getElement(ButtonList.STICK).position.y - controls.DEADZONE > y) {
 				    								controls.down = pointerId;
 				    							} else if(controls.up == pointerId) {
 				    								controls.up = -1;
 				    							} else if(controls.down == pointerId) {
 				    								controls.down = -1;
 				    							}
 				    							//left, right
 				    							if(controls.getElement(ButtonList.STICK).position.x + controls.DEADZONE < x) {
 				    								controls.right = pointerId;
 				    							} else if(controls.getElement(ButtonList.STICK).position.x - controls.DEADZONE > x) {
 				    								controls.left = pointerId;
 				    							}else if(controls.right == pointerId) {
 				    								controls.right = -1;
 				    							} else if(controls.left == pointerId) {
 				    								controls.left = -1;
 				    							}  		    					
 				    							
 				    							
 				    						}
 				                		}
 			                		}
 			                	}
 		                	}
 		                	if(Settings.gamePaused){
 			                	for(MenuButton item1: pauseItems) {
 				                	if(OverlapTester.pointInRectangle(item1.bounds, new Vector2(x, y))){
 				                		if(item1.heightLighted == false  && item1.soundEnabled) {
 			                				Resources.playSound(Resources.BUTTON_HEIGHTLIGHT);
 			                				selectedMenu = -1;
 			                				item1.heightLighted = true;
 			                			}
 				                    } else {
 				                    	item1.heightLighted = false;
 				                    }
 			                	}
 		                	}
 		                	if(!player.alive() || balls.size() == 0){
 		                		for(MenuButton item1: finishItems) {
 				                	if(OverlapTester.pointInRectangle(item1.bounds, new Vector2(x, y))){
 				                		if(item1.heightLighted == false  && item1.soundEnabled) {
 			                				Resources.playSound(Resources.BUTTON_HEIGHTLIGHT);
 			                				selectedMenu = -1;
 			                				item1.heightLighted = true;
 			                			}
 				                    } else {
 				                    	item1.heightLighted = false;
 				                    }
 			                	}
 		                	}
 		                } break;
 	                }
 	            }
 	            return true;
 	        }
 	    }	
 }
