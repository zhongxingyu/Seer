 package com.climber;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector3;
 
 public class Game implements ApplicationListener {
 	private Random random;
 
 	private OrthographicCamera camera;
 
 	private Texture mt_character;
 	private TextureRegion mtr_character;
 	private Texture mt_ground;
 	private Texture mt_Block;
 	private Texture mt_JoystickBackground;
 	private Texture mt_JoystickArrows;
 	private Texture mt_JumpButton;
 	private SpriteBatch m_batch;
 
 	private Sprite ms_Character;
 	private Sprite ms_Ground;
 	private Sprite ms_JoystickBackground;
 	private Sprite ms_JoystickArrows;
 	private Sprite ms_JumpButton;
 
 	private ArrayList<Sprite> mal_Blocks;
 
 	private static int WIDTH = 15;
 	private static int HEIGHT = 10;
 
 	private static final float CAMERA_SPEED = 0.05f;
 	private static final float CAMERA_SPEED_FALLING = 0.1f;
 
 	private long ml_lastBlockCreation; // the time the last block was created
 	private long ml_timeBetweenBlockCreations; // how long in between blocks to
 												// wait before creating a new
 												// one
 
 	// constants used to figure out which side of the character is overlapping a
 	// block
 	private final int LEFT_SIDE_OF_CHARACTER = 0;
 	private final int RIGHT_SIDE_OF_CHARACTER = 1;
 	private final int TOP_OF_CHARACTER = 2;
 	private final int BOTTOM_OF_CHARACTER = 3;
 	
 	private boolean mb_isTouchingRightArrow = false;
 	private boolean mb_isTouchingLeftArrow = false;
 
 	@Override
 	public void create() {
 		//create all of the textures
 		mt_character = new Texture(Gdx.files.internal("character.png"));
 		mtr_character = new TextureRegion(mt_character, 0, 0, 100, 150);
 
 		mt_ground = new Texture(Gdx.files.internal("ground.png"));
 		m_batch = new SpriteBatch();
 
 		mt_Block = new Texture(Gdx.files.internal("greenblock.png"));
 		
 		mt_JoystickArrows = new Texture(Gdx.files.internal("joystick-arrows.png"));
 		mt_JoystickBackground = new Texture(Gdx.files.internal("joystick-background.png"));
 		mt_JumpButton = new Texture(Gdx.files.internal("jump-button.png"));
 		
 		ms_JoystickBackground = new Sprite(1, 1, 2.8f, 2.8f); //the x and y don't matter because it is the hud which is based off of the camera's x and y
 		ms_JoystickArrows = new Sprite(1,1, 2.5f, 2.5f);
 		ms_JumpButton = new Sprite(1,1, 2.5f, 2.5f);
 
 		ms_Character = new Sprite(5, 2, 1, 1.5f, true);
 		ms_Ground = new Sprite(0, -4, 15, 5);
 
 		mal_Blocks = new ArrayList<Sprite>();
 
 		// Microsoft's CornFlowerBlue color
 		Gdx.gl10.glClearColor(0.4f, 0.6f, 0.9f, 1);
 
 		// create the random number generator
 		random = new Random();
 
 		// create a camera
 		camera = new OrthographicCamera(WIDTH, HEIGHT);
 		camera.position.set((float) (WIDTH / 2.0f), HEIGHT / 2, 0);
 
 		// set up timings for blocks
 		ml_lastBlockCreation = System.currentTimeMillis();
 		ml_timeBetweenBlockCreations = 2000; // 5 seconds in milliseconds
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void render() {
 		GL10 gl = Gdx.graphics.getGL10();
 
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		camera.update();
 		m_batch.setProjectionMatrix(camera.combined);
 
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		draw();
 		handleInput();
 		simulate();
 	}
 
 	private void draw() {
 		m_batch.begin();
 
 		// draw the character and the ground
 		m_batch.draw(mtr_character, ms_Character.getM_x(),
 				ms_Character.getM_y(), ms_Character.getM_width(),
 				ms_Character.getM_height());
 		m_batch.draw(mt_ground, ms_Ground.getM_x(), ms_Ground.getM_y(),
 				ms_Ground.getM_width(), ms_Ground.getM_height());
 
 		// draw all of the blocks
 		for (int i = 0; i < mal_Blocks.size(); i++) {
 			Sprite currentBlock = mal_Blocks.get(i);
 
 			m_batch.draw(mt_Block, currentBlock.getM_x(),
 					currentBlock.getM_y(), currentBlock.getM_width(),
 					currentBlock.getM_height());
 		}
 		
 		//draw the on screen controls
 		m_batch.draw(mt_JoystickBackground, camera.position.x - 7.16f,
 				camera.position.y - 4.65f, ms_JoystickBackground.getM_width(),
 				ms_JoystickBackground.getM_height());
 		m_batch.draw(mt_JoystickArrows, camera.position.x - 7,
 				camera.position.y - 4.5f, ms_JoystickArrows.getM_width(),
 				ms_JoystickArrows.getM_height());
 
 		m_batch.draw(mt_JumpButton, camera.position.x + 5,
 				camera.position.y - 4.5f, ms_JumpButton.getM_width(),
 				ms_JumpButton.getM_height());
 
 		m_batch.end();
 	}
 
 	private void handleInput() {
 		/* Touch screen controls */
 		
 		boolean touchedJumpButton = false;
 		
 		/* Multitouch */
 		for ( int i = 0; i < 3; i++ ) {
 			if (!Gdx.input.isTouched(i))
 				continue;
 
 			Vector3 input = new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0);
 			camera.unproject(input);
 			
 			//check if the jump button is pressed
 			if ( (camera.position.x - input.x) < -5 && (camera.position.x - input.x) > -7 &&(camera.position.y - input.y > 2) && (camera.position.y - input.y) < 4)
 				touchedJumpButton = true;
 			
 			//check if the joystick is pressed
 			float screenX = camera.position.x - input.x;
 			float screenY = camera.position.y - input.y;
 			if ( screenX > 4.5f && screenX < 5.5f && screenY > 2.5f && screenY < 4.0f) {
 				if (!mb_isTouchingRightArrow)
 					mb_isTouchingRightArrow = true;
 			}
 			if ( mb_isTouchingRightArrow && screenX > 5.5f)
 				mb_isTouchingRightArrow = false;
 			
 			if ( screenX > 6.0f && screenY > 2.0f && screenY < 4.0f) {
 				if (!mb_isTouchingLeftArrow)
 					mb_isTouchingLeftArrow = true;
 			}
 			if ( mb_isTouchingLeftArrow && screenX < 6.0f)
 				mb_isTouchingLeftArrow = false;
 		}
 		
 		if ( Gdx.input.justTouched()) {
 			if ( touchedJumpButton) {	
 				ms_Character.jump();
 				touchedJumpButton = false;
 			}
 		}
 		
 		if ( mb_isTouchingRightArrow) {
 			ms_Character.moveX(Gdx.graphics.getDeltaTime());
 
 			if (ms_Character.getM_x() + (ms_Character.getM_width() / 2) > WIDTH)
 				ms_Character.setM_x(0);
 		}
 		
 		if (mb_isTouchingLeftArrow) {
 			ms_Character.moveX(-Gdx.graphics.getDeltaTime());
 
 			 // if the middle of the character goes off the screen to the left, put him on the right
 			if (ms_Character.getM_x() + (ms_Character.getM_width() / 2) < 0)
 				ms_Character.setM_x(WIDTH);
 		}
 		
 		if ( !Gdx.input.isTouched()) {
 			if ( mb_isTouchingRightArrow)
 				mb_isTouchingRightArrow = false;
 			if ( mb_isTouchingLeftArrow)
 				mb_isTouchingLeftArrow = false;
 		}
 		
 		
 		/* Keyboard controls */
 		if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT)) {
 			ms_Character.moveX(-Gdx.graphics.getDeltaTime());
 
 			 // if the middle of the character goes off the screen to the left, put him on the right
 			if (ms_Character.getM_x() + (ms_Character.getM_width() / 2) < 0)
 				ms_Character.setM_x(WIDTH);
 		}
 		if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT)) {
 			ms_Character.moveX(Gdx.graphics.getDeltaTime());
 
 			if (ms_Character.getM_x() + (ms_Character.getM_width() / 2) > WIDTH)
 				ms_Character.setM_x(0);
 		}
 		if (Gdx.input.isKeyPressed(Keys.DPAD_UP)
 				|| Gdx.input.isKeyPressed(Keys.SPACE))
 			ms_Character.jump();
 
 	}
 
 	private void simulate() {
 		simulateCharacter();
 		simulateCamera();
 		simulateBlocks();
 		checkForNewBlocks();
 	}
 
 	private void simulateCamera() {
 		boolean panUp = false;
 		boolean panDown = false;
 
 		float charY = ms_Character.getM_y();
 		if (camera.position.y > charY) {
 			if (camera.position.y - charY > 2)
 				panDown = true;
 			else
 				panDown = false;
 		}
 
 		if (camera.position.y < charY) {
 			if (charY - camera.position.y > 1)
 				panUp = true;
 			else
 				panUp = false;
 		}
 
 		if (panUp)
 			camera.position.y += CAMERA_SPEED;
 
 		if (panDown)
 			camera.position.y -= ms_Character.isMb_isFalling() ? CAMERA_SPEED_FALLING
 					: CAMERA_SPEED;
 	}
 
 	private void simulateCharacter() {
 		ms_Character.simulate();
 
 		boolean shouldFall = true;
 
 		// check if the character landed on the ground
 		if (ms_Character.getM_y() < ms_Ground.getM_y()
 				+ ms_Ground.getM_height()) {
 			ms_Character.setFalling(false);
 			shouldFall = false;
 		}
 
 		// now check for collisions between the character and all of the blocks
 		// MY PLAN:
 		// use libgdx rectangles to check if the character is overlapping any
 		// blocks
 		// if he is overlapping a block, then go into detail and figure out
 		// which sides are touching
 		// left/right: either stop(if not falling or if jumping) or slide
 		// down(if falling)
 		// bottom: stop falling
 		// top: kill character
 
 		// first get the rectangle of the character
 		Rectangle charRect = new Rectangle(ms_Character.getM_x(),
 				ms_Character.getM_y(), ms_Character.getM_width(),
 				ms_Character.getM_height());
 
 		// now check against all of the other guys
 		for (int i = 0; i < mal_Blocks.size(); i++) {
 			Sprite tempBlock = mal_Blocks.get(i);
 			Rectangle tempBlockRect = new Rectangle(tempBlock.getM_x(),
 					tempBlock.getM_y(), tempBlock.getM_width(),
 					tempBlock.getM_height());
 
 			if (charRect.overlaps(tempBlockRect)) {
 				int sideOfOverlappingX = findOverlappingSideX(charRect,
 						tempBlockRect);
 				int sideOfOverlappingY = findOverlappingSideY(charRect,
 						tempBlockRect);
 
 				// originally had a switch statement here to handle this, but
 				// that handles everything at once and i need to check for
 				// left/right and top/bottom
 				// separately, so I needed to switch it to if/else if/else
 
 				if (sideOfOverlappingX == RIGHT_SIDE_OF_CHARACTER) {
 					// TODO: check if you need to stop him from moving or start
 					// him sliding, then do it
 					// if they aren't falling and they are running into the left
 					// side of a box and they aren't standing on the box: stop
 					// them.
 					/*
 					 * if ( !ms_Character.isMb_isFalling() &&
 					 * !(sideOfOverlappingY == BOTTOM_OF_CHARACTER)) {
 					 * ms_Character.moveX(-Gdx.graphics.getDeltaTime()); } else
 					 * if ( ms_Character.isMb_isJumping()) {
 					 * ms_Character.moveX(-Gdx.graphics.getDeltaTime());
 					 * ms_Character.startSliding(); }
 					 */
 
 					if ((tempBlockRect.y + (tempBlockRect.height * 0.8f) > charRect.y))
 						ms_Character.moveX(-Gdx.graphics.getDeltaTime());
 
 				} else if (sideOfOverlappingX == LEFT_SIDE_OF_CHARACTER) {
 					// TODO: check if you need to stop him from moving or start
 					// him sliding, then do it
 					// if they aren't falling and they are running into the
 					// right side of a box and they aren't standing on the box:
 					// stop them.
 					/*
 					 * if ( !ms_Character.isMb_isFalling() &&
 					 * !(sideOfOverlappingY == BOTTOM_OF_CHARACTER)) {
 					 * ms_Character.moveX(Gdx.graphics.getDeltaTime()); } else
 					 * if ( ms_Character.isMb_isJumping()) {
 					 * ms_Character.moveX(Gdx.graphics.getDeltaTime());
 					 * ms_Character.startSliding(); }
 					 */
 					if ((tempBlockRect.y + (tempBlockRect.height * 0.8f) > charRect.y))
 						ms_Character.moveX(Gdx.graphics.getDeltaTime());
 
 				}
 
 				if (sideOfOverlappingY == TOP_OF_CHARACTER) {
 					if (ms_Character.isMb_isJumping()) {
 						ms_Character.interruptJump();
 					} else {
 						// kill character
 					}
 				} else if (sideOfOverlappingY == BOTTOM_OF_CHARACTER) {
 					// TODO: stop him from falling
 					// ms_Character.setFalling(false);
 					shouldFall = false;
 				}
 			} else {
 				ms_Character.setMb_AllowedMoveRight(true);
 				ms_Character.setMb_AllowedMoveLeft(true);
 			}
 		}
 
 		ms_Character.setFalling(shouldFall);
 	}
 
 	private int findOverlappingSideX(Rectangle a, Rectangle b) {
 		if ((a.x + a.width) > b.x && a.x < b.x)
 			return RIGHT_SIDE_OF_CHARACTER;
 
 		if (a.x < (b.x + b.width) && a.x > b.x)
 			return LEFT_SIDE_OF_CHARACTER;
 
 		return -1;
 	}
 
 	private int findOverlappingSideY(Rectangle a, Rectangle b) {
 		if ((a.y + a.height) > b.y && a.y < b.y)
 			return TOP_OF_CHARACTER;
 
 		if (a.y < (b.y + b.height) && a.y > b.y)
 			return BOTTOM_OF_CHARACTER;
 
 		return -1;
 	}
 
 	private void simulateBlocks() {
 		// simulate all of the blocks falling
 		for (int i = 0; i < mal_Blocks.size(); i++) {
 			// get the current block and simulate it
 			Sprite currentBlock = mal_Blocks.get(i);
 			currentBlock.simulate();
 
 			// first check if it is on the ground
 			if (currentBlock.getM_y() < ms_Ground.getM_y()
 					+ ms_Ground.getM_height()) {
 				currentBlock.setFalling(false);
 			} else {
 
 				// now check to see if it landed on any other blocks, and if so
 				// then stop it from falling
 				// go through all of the other blocks
 				for (int j = 0; j < mal_Blocks.size(); j++) {
 					if (i == j)
 						continue;
 
 					Sprite tempBlock = mal_Blocks.get(j);
 					Rectangle tempBlockRect = new Rectangle(tempBlock.getM_x(),
 							tempBlock.getM_y(), tempBlock.getM_width(),
 							tempBlock.getM_height());
 					Rectangle currentBlockRect = new Rectangle(
 							currentBlock.getM_x(), currentBlock.getM_y(),
 							currentBlock.getM_width(),
 							currentBlock.getM_height());
 
 					if (currentBlockRect.overlaps(tempBlockRect)) {
 						currentBlock.setFalling(false);
						j = mal_Blocks.size();
 					}
 				}
 			}
 		}
 	}
 
 	private void checkForNewBlocks() {
 		long randomOffset = (random.nextLong() % 2000) + 1000; // create a
 																// random offset
 																// ranging
 																// between
 																// 1000-3000
 																// milliseconds
 		if (System.currentTimeMillis() - ml_lastBlockCreation > (ml_timeBetweenBlockCreations + randomOffset)) {
 			addBlock();
 			ml_lastBlockCreation = System.currentTimeMillis();
 		}
 	}
 
 	private void addBlock() {
 
 		// randomize the x position of the block
 		float x = randomFloat(0, WIDTH);
 
 		// randomize the size of the block
 		float size = randomFloat(1, 3);
 
 		// create new block at a random X value, the top of the screen, and with
 		// the width and height of the block as 3.0f, and set it to be falling
 		// TODO: Randomize size between 1 and 3 - those seem to be good sizes
		Sprite block = new Sprite(x, camera.position.y + (HEIGHT / 2), size,
 				size, true);
 		block.setFallingSpeed(0.05f);
 
 		mal_Blocks.add(block);
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	private float randomFloat(float min, float max) {
 		return (float) (min + (Math.random() * (max - min)));
 	}
 
 }
