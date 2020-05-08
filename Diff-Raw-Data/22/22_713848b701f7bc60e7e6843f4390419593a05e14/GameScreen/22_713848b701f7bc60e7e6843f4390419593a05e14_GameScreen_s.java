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
 import com.games.androidgames.pang.buttons.PauseButton;
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
 	
 	final float SPEAR_REGION_MAX = 240.0f;
 	final float BALL_RADIUS = 16.0f;
 	final float SMALLEST_BALL = 16.0f;
 	final float BUMP = 0.7f;
 	final float BOUNCE_HEIGHT = 500.0f;
 	final int BALL_LIMIT = 20;
 	final int SPRITE_LIMIT = 100;
 
 	private int selectedMenu, score, topTen;
 
 	float[] x = new float[10];
 	float[] y = new float[10];
 	boolean[] touched = new boolean[10];
 	int[] id = new int[10];
 	
 	
 	float SPEAR_SPEED = 150.0f;		
 	
 	boolean[] pressedKeys = new boolean[128];
     List<KeyEvent> keyEventsBuffer = new ArrayList<KeyEvent>(); 
     
 	GLGraphics glGraphics;
 	GL10 gl;
 	GLText glText, glMenuText, glScoreText;
 
 	private List<MenuButton> pauseItems, finishItems;
 	Player player;
 	PowerItem single, doub, stick;
 	Weapon weapon;
 	
 	PlayerState playerState;
 	Player savedPlayerState;
 	TextureRegion currKeyFrame;		
 	Texture background;
 	TextureRegion backgroundRegion;
 	List<DynamicGameObject> balls;
 	List<GameObject> platforms;
 	List<Ladder> ladders;
 	Texture textureSet;
 	
 	TextureRegion ballRegion, spearRegion;
 	Vector2 gravity = new Vector2(0, -400);
 	
 	TouchControls controls;
 	SpriteBatcher batcher;
 	
 	public GameScreen(Game game) {
 		super(game);
 		glGraphics = ((GLGame)game).getGLGraphics();
 		glGraphics.getView().setOnKeyListener(this);
 		glGraphics.getView().setOnTouchListener(this);
 		glGraphics.getView().setFocusable(true);
 		glGraphics.getView().requestFocus();
 		gl = glGraphics.getGL();
 		glText = Resources.glText;
 		glMenuText = Resources.glButtonText;
 		glScoreText = Resources.glScoreText;
 		
 		selectedMenu = -1;
 		topTen = -1;
 		camera = new Camera2D(glGraphics, Settings.WORLD_WIDTH, Settings.WORLD_HEIGHT);
 		playerState = PlayerState.STANDING;
 		balls = new ArrayList<DynamicGameObject>();
 		ladders  = new ArrayList<Ladder>();
 		platforms = new ArrayList<GameObject>();
 		
 		balls.add(new Ball(Settings.WORLD_WIDTH / 4, Settings.WORLD_HEIGHT - 32.0f, BALL_RADIUS * 2));
 		this.score = 0;
 		
 		for(DynamicGameObject ball: balls) {
 			ball.velocity.x = 100;
 		}
 		
 		batcher = new SpriteBatcher(gl, SPRITE_LIMIT);	
 		pauseItems = new ArrayList<MenuButton>();
 		finishItems = new ArrayList<MenuButton>();
 		pauseItems.add(new BlankButton("Paused", glMenuText, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 5 * 4, camera.zoom * 1.5f));
 		pauseItems.add(new PauseButton("Resume", glMenuText, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 5 * 3, camera.zoom * 1.5f));
 		
 		MenuButton exit = new MainMenuButton("Exit game", glMenuText, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 3, camera.zoom * 1.5f);
 		pauseItems.add(exit);	
		finishItems.add(new GameButton("Retry", glMenuText, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 5 * 3, camera.zoom * 1.5f));
 
 		finishItems.add(exit);
 	}
 	
 	@Override
 	public void resume() {
 			textureSet = Resources.gameItems;
 			if(savedPlayerState == null) {
 				player = new Player(camera.position.x, 35, 64, 70, 3, textureSet);
 				player.setGravity(0, -400);
 			} else {
 				player = savedPlayerState;
 			}
 			
 			ballRegion = Resources.ball;
 							
 			weapon = new Weapon(textureSet, 2);				
 			TextureRegion singleRegion = Resources.powerupSingle;
 			single = new PowerItem(singleRegion, 100, 400, 24, 48, 3, MODE.SINGLE);
 			
 			TextureRegion doubleRegion = Resources.powerupDouble;
 			doub = new PowerItem(doubleRegion, 100, 400, 48, 48, 3, MODE.DOUBLE);
 			
 			TextureRegion stickyRegion = Resources.powerupSticky;
 			stick = new PowerItem(stickyRegion, 100, 400, 40, 48, 3, MODE.STICKY);
 			
 			ladders.add(new Ladder(textureSet, 250, 100, 50, 200));
 			ladders.add(new Ladder(textureSet, 450, 145, 50, 300));
 			
 			platforms.add(new Platform(textureSet, 300, 288, 300, 16));	
 			
 			background = Resources.background;
 			backgroundRegion =  Resources.backgroundRegion;
 			
 			
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
 														if(Settings.gamePaused && selectedMenu != -1){
 															pauseItems.get(selectedMenu).action(game);	
 							                				Resources.playSound(Resources.BUTTON_HEIGHTLIGHT);
 														}
 													}
 												break;
 					case KeyEvent.KEYCODE_BACK:  {
 													//Circle Pressed
 													weapon.mode = Weapon.MODE.STICKY;	
 												 }
 												break;
 				case KeyEvent.KEYCODE_BUTTON_X:	{
 														
 												}
 												break;
 				case KeyEvent.KEYCODE_BUTTON_Y: {
 														//Triangle pressed		
 														weapon.mode = Weapon.MODE.DOUBLE;	
 												}													
 												break;
 				case KeyEvent.KEYCODE_DPAD_UP:  {
 													if(Settings.gamePaused){
 														if(selectedMenu == -1){
 															selectedMenu = pauseItems.size() - 1;
 														} 
 														do
 														{
 															if(selectedMenu == 0){
 																selectedMenu = pauseItems.size() - 1;
 															} else {
 																	selectedMenu--;
 															}
 														}while(!pauseItems.get(selectedMenu).enabled);
 														Resources.playSound(Resources.BUTTON_HEIGHTLIGHT);	
 													}
 												}
 												break;
 				case KeyEvent.KEYCODE_DPAD_DOWN: {
 													if(Settings.gamePaused){
 														if(selectedMenu == -1){
 															selectedMenu = 0;
 														} 
 														do
 														{
 															if(selectedMenu == pauseItems.size() - 1){
 																selectedMenu = 0;
 															} else {
 																	selectedMenu++;
 															}
 														}while(!pauseItems.get(selectedMenu).enabled);												
 														Resources.playSound(Resources.BUTTON_HEIGHTLIGHT);	
 													}
 												 }
 												
 												break;
 				case KeyEvent.KEYCODE_DPAD_RIGHT: 	{
 														
 													}
 													
 												break;
 				case KeyEvent.KEYCODE_DPAD_LEFT:  	{
 														
 													}
 												break;
 				case KeyEvent.KEYCODE_BUTTON_SELECT: 
 														{
 															weapon.mode = Weapon.MODE.SINGLE;	
 														}
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
 	
 	public void startButtonAction() {
     	//if end of game reset else toggle pause
 		player.velocity.set(0, 0);
 		if(balls.size() == 0 || !player.alive()) {
 			Settings.gamePaused = false; 
 			//spawn new ball
 			synchronized(balls) {
 				balls.clear();
 				score = 0;
 				if(balls.size() < BALL_LIMIT && !Settings.gamePaused) {
 					balls.add(new Ball(Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT - 32.0f, BALL_RADIUS * 2));
 					balls.get(balls.size() - 1).velocity.x = 150;																		
 				}
 				player.reset(Settings.WORLD_WIDTH / 2, 50, 3);	
 				weapon.mode = MODE.SINGLE;
 			}
 		} else {															
 			if(Settings.gamePaused) {					
 				Settings.gamePaused = false;
 			} else {
 				Settings.gamePaused = true;				
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
 		if(controls.activeButtons[3] || keyPressed(KeyEvent.KEYCODE_BUTTON_START)) {
 			pressedKeys[KeyEvent.KEYCODE_BUTTON_START] = false;
 			startButtonAction();			
 		} 
 		if(selectedMenu != -1){
 			for(int i = 0; i < pauseItems.size(); i++){
 				pauseItems.get(i).heightLighted = false;
 			}
 			pauseItems.get(selectedMenu).heightLighted = true;
 		}
 		
 		if(!Settings.gamePaused) {		
 			playerState = PlayerState.STANDING;
 
 			//move player
 			if((keyPressed(KeyEvent.KEYCODE_DPAD_RIGHT) || controls.right >= 0) && player.position.x + player.bounds.width / 2 < Settings.WORLD_WIDTH) 	{
 				player.movePlayer(200, 0, deltaTime);
 				playerState = PlayerState.WALKING_RIGHT;
 			} else if((keyPressed(KeyEvent.KEYCODE_DPAD_LEFT) || controls.left >= 0) && player.position.x - player.bounds.width / 2 > 0) 	{
 				player.movePlayer(-200, 0, deltaTime);
 				playerState = PlayerState.WALKING_LEFT;
 			} else if((keyPressed(KeyEvent.KEYCODE_DPAD_UP) || controls.up >= 0)) {
 				for(int i =0; i < ladders.size(); i++) {
 					Ladder ladder = (Ladder)ladders.get(i);
 					if(ladder.upValid(player)) {
 						playerState = PlayerState.CLIMBING;
 						player.movePlayer(0, 200, deltaTime);
 						break;
 					}
 				}
 			} else if((keyPressed(KeyEvent.KEYCODE_DPAD_DOWN) || controls.down >= 0)) {
 				for(int i =0; i < ladders.size(); i++) {
 					Ladder ladder = (Ladder)ladders.get(i);
 					if(ladder.downValid(player)) {
 						playerState = PlayerState.CLIMBING;					
 						player.movePlayer(0, -200, deltaTime);
 						break;
 					}
 				}
 			} else{ 
 				if((weapon.spear.alive && weapon.spear.position.y < 80) || (weapon.stickySpear.position.y < 80 && weapon.stickySpear.alive) || (weapon.spear2.position.y < 80 && weapon.spear2.alive)) {
 					
 					playerState = PlayerState.SHOOTING;
 				} 				
 			}			
 			
 			if((keyPressed(KeyEvent.KEYCODE_DPAD_CENTER) || controls.activeButtons[1])) {
 				//reset button to off
 				pressedKeys[KeyEvent.KEYCODE_DPAD_CENTER] = false;					
 				//spawn new spear
 				if(weapon.shoot(player.position.x, player.position.y)){	
 					Resources.playSound(Resources.FIRE);
 					playerState = PlayerState.SHOOTING;
 				}
 			} 
 			if((keyPressed(KeyEvent.KEYCODE_BUTTON_X) || controls.activeButtons[2])) {
 				pressedKeys[KeyEvent.KEYCODE_BUTTON_X] = false;
 				//square pressed		
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
 							topTen = Resources.checkScore((GLGame)game, score);
 						}
 					}
 					
 					//check spear collisions
 					if(weapon.checkCollisions(ball.boundingCircle)) {							
 						Resources.playSound(Resources.POP);
 						//if ball smaller than smallest ball limit destroy
 						if(ball.boundingCircle.radius < SMALLEST_BALL) {
 							balls.remove(ball);
 							score += 200;
 							if(balls.size() == 0){
 								topTen = Resources.checkScore((GLGame)game, score);
 							}
 						} else {
 							score += 50;
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
 						ball.velocity.y = (BOUNCE_HEIGHT) - (ball.boundingCircle.radius - ball.position.y);
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
 					score += single.value;
 					single.alive = false;
 				}
 				single.update(deltaTime, platforms);
 			}
 			if(doub.alive) {
 				if(OverlapTester.overlapRectangle(player.bounds, doub.bounds)) {
 					weapon.mode = MODE.DOUBLE;
 					score += doub.value;
 					doub.alive = false;
 				}
 				doub.update(deltaTime, platforms);
 			}
 			if(stick.alive) {
 				if(OverlapTester.overlapRectangle(player.bounds, stick.bounds)) {
 					weapon.mode = MODE.STICKY;
 					score += stick.value;
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
 				for(TouchElement element :controls.touchElements) {
 					batcher.drawSprite(element.position.x, element.position.y, element.width, element.height, element.region);
 				}
 			}
 			batcher.endBatch();
 		}
 		
 		if(!player.alive()) {
 			Settings.gamePaused = true;
 			player.reset();
 			String text = "You Died";
 			if(topTen > 0){
 				text += " New Record " + topTen + "th";
 			}
 			glMenuText.begin(1, 0, 0, 1);
 			glMenuText.drawC(text, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT - 75);
 			glMenuText.end();
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
 			String text = "Congratulations you won";
 			if(topTen > 0){
 				text += " New Record " + topTen + "th";
 			}
 			glMenuText.begin(0.3f, 1, 0.3f, 1);
 			glMenuText.drawC(text, Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT - 75);
 			glMenuText.end();
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
 		glScoreText.draw("score: " + score, 80 , Settings.WORLD_HEIGHT - 60);
 		glScoreText.end();
 		
 		
 		batcher.beginBatch(textureSet);
 		switch(weapon.mode){
 		case DOUBLE: batcher.drawSprite( 32, Settings.WORLD_HEIGHT - 32, doub.bounds.width, doub.bounds.height, doub.region); break;
 		case STICKY: batcher.drawSprite( 32, Settings.WORLD_HEIGHT - 32, stick.bounds.width, stick.bounds.height, stick.region); break;
 			default: batcher.drawSprite( 32, Settings.WORLD_HEIGHT - 32, single.bounds.width, single.bounds.height, single.region);
 		}
 		batcher.endBatch();                                  
 	}
 
 	@Override
 	public void dispose() {
 		
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		 synchronized (this) {
 				controls.resetActiveButtons();
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
 	                float x = event.getX(i);
 	                float y = glGraphics.getHeight() - event.getY(i);
 	                switch (action) {
 		                case MotionEvent.ACTION_DOWN:
 		                case MotionEvent.ACTION_POINTER_DOWN:
 		                {
 		                	
 		                	for(int j = 0; j < controls.touchElements.size(); j++) {
 		                		TouchElement element = controls.touchElements.get(j);
 		                		if(element.ready()){		                			
 			                		if(OverlapTester.pointInRectangle(element.bounds, x, y)){
 			                			controls.activeButtons[j] = true;
 			    						element.use(x, y);
 			    						
 			    						//if hit joystick work out directions
 			    						if(j == 0) {
 			    							float DEADZONE = 20;
 			    							
 			    							//left, right
 			    							if(controls.getElement(ButtonList.STICK).position.x + DEADZONE < x) {
 			    								controls.right = i;
 			    							} else if(controls.getElement(ButtonList.STICK).position.x - DEADZONE > x) {
 			    								controls.left = i;
 			    							}
 			    							
 			    							//up, down
 			    							if(controls.getElement(ButtonList.STICK).position.y + DEADZONE < y) {
 			    								controls.up = i;
 			    							} else if(controls.getElement(ButtonList.STICK).position.y - DEADZONE > y) {
 			    								controls.down = i;
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
 				                    } 
 			                	}
 		                	}
 		                }  break;
 	
 		                case MotionEvent.ACTION_UP:
 		                case MotionEvent.ACTION_POINTER_UP:
 		                case MotionEvent.ACTION_CANCEL:
 		                {
 							if(controls.right == i) {
 								controls.right = -1;
 							} else if(controls.left == i) {
 								controls.left = -1;
 							}    							
 							//up, down
 							if(controls.up == i) {
 								controls.up = -1;
 							} else if(controls.down == i) {
 								controls.down = -1;
 							}
 			    			if(Settings.gamePaused){
 			                	for(MenuButton item1: pauseItems) {
 				                	if(OverlapTester.pointInRectangle(item1.bounds, new Vector2(x, y)) && item1.enabled){
 				                		item1.action(game);			                    	
 				                    }
 			                	}
 			    			}
 		                } break;
 	
 		                case MotionEvent.ACTION_MOVE:
 		                {
 		                	for(int j = 0; j < controls.touchElements.size(); j++) {
 		                		TouchElement element = controls.touchElements.get(j);
 		                		if(element.ready()){		                			
 			                		if(OverlapTester.pointInRectangle(element.bounds, x, y)){
 			                			controls.activeButtons[j] = true;
 			    						element.use(x, y);
 			    						
 			    						//if hit joystick work out directions
 			    						if(j == 0) {
 			    							float DEADZONE = 20;
 			    							
 			    							//left, right
 			    							if(controls.getElement(ButtonList.STICK).position.x + DEADZONE < x) {
 			    								controls.right = j;
 			    							} else if(controls.getElement(ButtonList.STICK).position.x - DEADZONE > x) {
 			    								controls.left = j;
 			    							}else if(controls.right == i) {
 			    								controls.right = -1;
 			    							} else if(controls.left == i) {
 			    								controls.left = -1;
 			    							}  		    					
 			    							
 			    							//up, down
 			    							if(controls.getElement(ButtonList.STICK).position.y + DEADZONE < y) {
 			    								controls.up = j;
 			    							} else if(controls.getElement(ButtonList.STICK).position.y - DEADZONE > y) {
 			    								controls.down = j;
 			    							} else if(controls.up == i) {
 			    								controls.up = -1;
 			    							} else if(controls.down == i) {
 			    								controls.down = -1;
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
 		                } break;
 	                }
 	            }
 	            return true;
 	        }
 	    }
 	
 	
 }
